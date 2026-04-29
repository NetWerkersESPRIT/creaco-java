package gui.comment;

import entities.Comment;
import entities.Post;
import entities.Users;
import gui.forum.EmojiPickerModalController;
import gui.post.DisplayPostController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.text.TextFlow;
import javafx.scene.layout.*;
import services.forum.CommentService;
import services.UserService;
import services.forum.PostService;
import entities.ReactionType;
import java.util.Map;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.scene.Scene;
import gui.forum.GifPickerModalController;
import services.NotificationService;
import utils.TextCorrectionService;
import utils.DetectBadWordService;
import javafx.concurrent.Task;
import javafx.application.Platform;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class DisplayCommentController {

    private static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private VBox commentsContainer;
    @FXML private Label postTitleLabel;
    @FXML private Label postMetaLabel;
    @FXML private Label postContentLabel;
    @FXML private Label repliesBadge;
    @FXML private TextArea commentArea;
    @FXML private VBox emptyState;
    @FXML private StackPane postAuthorAvatarContainer;
    @FXML private Label likesBadge;
    @FXML private Button btnEditPost;
    @FXML private Button btnDeletePost;
    @FXML private HBox attachmentPreview;
    @FXML private ImageView gifPreview;
    @FXML private StackPane currentUserAvatarContainer;

    private final CommentService commentService = new CommentService();
    private final UserService userService = new UserService();
    private final PostService postService = new PostService();
    private Post currentPost;
    private boolean isAdminMode = false;

    public void setAdminMode(boolean isAdminMode) {
        this.isAdminMode = isAdminMode;
    }

    public void setPost(Post post) {
        this.currentPost = post;
        if (post != null) {
            postTitleLabel.setText(safeText(post.getTitle()));
            postContentLabel.setText(safeText(post.getContent()));
            
            Users author = userService.getUserById(post.getUserId());
            String username = (author != null) ? author.getUsername() : "Unknown";
            String date = (post.getCreatedAt() != null) ? post.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-";
            postMetaLabel.setText("Posted by " + username + " • " + date);

            // Set Author Avatar with Ring
            if (author != null && postAuthorAvatarContainer != null) {
                StackPane avatarWithRing = buildAvatarWithRing(author, 45);
                postAuthorAvatarContainer.getChildren().setAll(avatarWithRing);
                postAuthorAvatarContainer.setStyle("-fx-background-color: transparent;");
            }

            // Set Current User Avatar in Input Bar
            Users currentUser = utils.SessionManager.getInstance().getCurrentUser();
            if (currentUser != null && currentUserAvatarContainer != null) {
                StackPane currentAvatar = buildAvatar(currentUser);
                currentAvatar.setPrefSize(32, 32);
                currentAvatar.setMinSize(32, 32);
                currentAvatar.setMaxSize(32, 32);
                if (!currentAvatar.getChildren().isEmpty() && currentAvatar.getChildren().get(0) instanceof ImageView) {
                    ImageView iv = (ImageView) currentAvatar.getChildren().get(0);
                    iv.setFitWidth(32); iv.setFitHeight(32);
                    iv.setClip(new javafx.scene.shape.Circle(16, 16, 16));
                }
                currentUserAvatarContainer.getChildren().setAll(currentAvatar);
            }

            // Set Likes Count
            if (likesBadge != null) {
                try {
                    Map<ReactionType, Integer> counts = postService.getReactionCounts(post.getId());
                    int total = counts.values().stream().mapToInt(Integer::intValue).sum();
                    likesBadge.setText("👍 LIKE " + total);
                } catch (SQLException e) {
                    likesBadge.setText("👍 LIKE 0");
                }
            }

            // Visibility for Edit/Delete
            int currentUserId = (currentUser != null) ? currentUser.getId() : -1;
            boolean isOwner = (post.getUserId() == currentUserId);
            
            if (btnEditPost != null) {
                btnEditPost.setVisible(isOwner);
                btnEditPost.setManaged(isOwner);
            }
            if (btnDeletePost != null) {
                btnDeletePost.setVisible(isOwner || isAdminMode);
                btnDeletePost.setManaged(isOwner || isAdminMode);
            }
            
            loadCommentsByPost();
        }
    }

    private void loadCommentsByPost() {
        if (currentPost == null) return;
        
        commentsContainer.getChildren().clear();
        try {
            List<Comment> allComments = commentService.getCommentsByPost(currentPost.getId());
            
            Users currentUser = utils.SessionManager.getInstance().getCurrentUser();
            int curUserId = (currentUser != null) ? currentUser.getId() : -1;
            boolean isPostOwner = (currentPost.getUserId() == curUserId);

            // Filter comments: Hide "HIDDEN" comments unless you are the owner or post owner
            List<Comment> visibleComments = allComments.stream()
                    .filter(c -> !"HIDDEN".equals(c.getStatus()) || c.getUserId() == curUserId || isPostOwner || isAdminMode)
                    .collect(Collectors.toList());

            repliesBadge.setText("• " + visibleComments.size() + " comments");
            
            if (visibleComments.isEmpty()) {
                commentsContainer.getChildren().add(emptyState);
                emptyState.setVisible(true);
                return;
            }
            emptyState.setVisible(false);

            List<Comment> roots = visibleComments.stream()
                    .filter(c -> c.getParentCommentId() == null)
                    .sorted((c1, c2) -> c2.getCreatedAt().compareTo(c1.getCreatedAt()))
                    .collect(Collectors.toList());

            for (Comment root : roots) {
                renderCommentThread(root, visibleComments, 0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void renderCommentThread(Comment comment, List<Comment> allComments, int depth) {
        VBox card = createCommentCard(comment, depth);
        commentsContainer.getChildren().add(card);

        List<Comment> replies = allComments.stream()
                .filter(c -> c.getParentCommentId() != null && c.getParentCommentId() == comment.getId())
                .sorted((c1, c2) -> c1.getCreatedAt().compareTo(c2.getCreatedAt()))
                .collect(Collectors.toList());

        for (Comment reply : replies) {
            renderCommentThread(reply, allComments, depth + 1);
        }
    }

    // ── Instagram-style comment card ────────────────────────────────────────────
    private VBox createCommentCard(Comment comment, int depth) {
        // ── Layout ───────────────────────────────────────────────────────────────
        double leftPad = depth > 0 ? 52.0 * Math.min(depth, 3) : 0.0;
        VBox card = new VBox(0);
        card.setId("comment-card-" + comment.getId()); // Set ID for scrolling/lookup
        card.setStyle("-fx-background-color: transparent;");
        card.setPadding(new Insets(10, 0, 2, leftPad));

        Users user = userService.getUserById(comment.getUserId());
        String username = (user != null) ? user.getUsername() : "Unknown";
        String dateStr = formatDate(comment.getCreatedAt());

        // ── Parse body ───────────────────────────────────────────────────────────
        String bodyText = safeText(comment.getBody());
        String textContent = bodyText;
        String gifUrl = null;
        if (bodyText.contains("[GIF]")) {
            String[] parts = bodyText.split("\\[GIF\\]");
            textContent = parts[0].trim();
            if (parts.length > 1) gifUrl = parts[1].trim();
        } else if (bodyText.startsWith("http") && (bodyText.contains("klipy.com") || bodyText.endsWith(".gif"))) {
            gifUrl = bodyText;
            textContent = "";
        }

        // ── Avatar ───────────────────────────────────────────────────────────────
        int avatarSize = depth > 0 ? 28 : 36;
        StackPane avatarPane = buildAvatarWithRing(user, avatarSize);

        // ── Main row: avatar | content-col | heart ───────────────────────────────
        HBox mainRow = new HBox(10);
        mainRow.setAlignment(Pos.TOP_LEFT);

        // Content column
        VBox contentCol = new VBox(4);
        HBox.setHgrow(contentCol, Priority.ALWAYS);

        // Username + text inline via TextFlow
        javafx.scene.text.TextFlow commentLine = new javafx.scene.text.TextFlow();
        commentLine.setMaxWidth(Double.MAX_VALUE);
        javafx.scene.text.Text uTxt = new javafx.scene.text.Text(username + " ");
        uTxt.setStyle("-fx-font-weight: bold; -fx-fill: #1a1a2e; -fx-font-size: 13px;");
        
        String finalContent = textContent;
        if ("HIDDEN".equals(comment.getStatus())) {
            finalContent = "[HIDDEN BY POST OWNER] " + textContent;
        }
        
        javafx.scene.text.Text bTxt = new javafx.scene.text.Text(finalContent);
        if ("HIDDEN".equals(comment.getStatus())) {
            bTxt.setStyle("-fx-fill: #94a3b8; -fx-font-style: italic; -fx-font-size: 13px;");
        } else {
            bTxt.setStyle("-fx-fill: #4a5568; -fx-font-size: 13px;");
        }
        commentLine.getChildren().addAll(uTxt, bTxt);
        contentCol.getChildren().add(commentLine);

        // GIF (if any)
        if (gifUrl != null) {
            ImageView gifView = new ImageView();
            gifView.setFitWidth(350); // Final size for better impact
            gifView.setPreserveRatio(true);
            gifView.setSmooth(true);
            
            // Placeholder background
            gifView.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 15;");
            
            // Use background loading
            Image img = new Image(gifUrl, true);
            gifView.setImage(img);

            // Rounded corners clip
            javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
            clip.setArcWidth(20);
            clip.setArcHeight(20);
            
            // Sync clip size
            gifView.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
                clip.setWidth(newBounds.getWidth());
                clip.setHeight(newBounds.getHeight());
            });
            
            gifView.setClip(clip);
            
            // Spacing
            VBox.setMargin(gifView, new Insets(8, 0, 8, 0));
            contentCol.getChildren().add(gifView);
        }

        // ── Meta row: time · likes · Reply [· Edit] [· Delete] ──────────────────
        HBox metaRow = new HBox(12);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        metaRow.setPadding(new Insets(2, 0, 0, 0));

        String metaStyle = "-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 0;";
        String metaHover = "-fx-background-color: transparent; -fx-text-fill: #475569; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 0;";

        Label timeLabel = new Label(dateStr);
        timeLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
        metaRow.getChildren().add(timeLabel);

        // Reply button
        int curUserId = utils.SessionManager.getInstance().getCurrentUser().getId();
        boolean isPostOwner = (currentPost != null && currentPost.getUserId() == curUserId);

        Button replyBtn = new Button("Reply");
        replyBtn.setStyle("-fx-font-weight: bold; " + metaStyle);
        replyBtn.setOnMouseEntered(e -> replyBtn.setStyle("-fx-font-weight: bold; " + metaHover));
        replyBtn.setOnMouseExited(e -> replyBtn.setStyle("-fx-font-weight: bold; " + metaStyle));
        metaRow.getChildren().add(replyBtn);

        // Edit button (owner only)
        TextArea editTextArea = new TextArea(comment.getBody());
        VBox inlineEditBox = new VBox(8);
        if (comment.getUserId() == curUserId) {
            Button editBtn = new Button("Edit");
            editBtn.setStyle("-fx-font-weight: bold; " + metaStyle);
            editBtn.setOnMouseEntered(e -> editBtn.setStyle("-fx-font-weight: bold; " + metaHover));
            editBtn.setOnMouseExited(e -> editBtn.setStyle("-fx-font-weight: bold; " + metaStyle));
            metaRow.getChildren().add(editBtn);

            // Build inline edit box
            inlineEditBox.setVisible(false);
            inlineEditBox.setManaged(false);
            inlineEditBox.setPadding(new Insets(6, 0, 0, 0));
            editTextArea.setPrefHeight(70); editTextArea.setWrapText(true);
            editTextArea.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 12; -fx-text-fill: #1a1a2e; -fx-font-size: 13px; -fx-border-color: transparent;");
            HBox editBtnRow = new HBox(8); editBtnRow.setAlignment(Pos.CENTER_RIGHT);
            Button cancelE = new Button("Cancel");
            cancelE.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-cursor: hand;");
            Button saveE = new Button("Save");
            saveE.setStyle("-fx-background-color: #ce2d7c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 5 16; -fx-cursor: hand;");
            editBtnRow.getChildren().addAll(cancelE, saveE);
            inlineEditBox.getChildren().addAll(editTextArea, editBtnRow);

            editBtn.setOnAction(e -> { boolean v = !inlineEditBox.isVisible(); inlineEditBox.setVisible(v); inlineEditBox.setManaged(v); if (v) { editTextArea.setText(comment.getBody()); editTextArea.requestFocus(); } });
            cancelE.setOnAction(e -> { inlineEditBox.setVisible(false); inlineEditBox.setManaged(false); });
            saveE.setOnAction(e -> { String nb = editTextArea.getText(); if (nb != null && !nb.trim().isEmpty()) submitEdit(comment, nb); });
        }

        // Delete button (Only Comment Owner can delete now)
        if (comment.getUserId() == curUserId) {
            Button deleteBtn = new Button("Delete");
            deleteBtn.setStyle("-fx-font-weight: bold; -fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 0;");
            deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle("-fx-font-weight: bold; -fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 0;"));
            deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle("-fx-font-weight: bold; -fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 0;"));
            deleteBtn.setOnAction(e -> deleteComment(comment.getId()));
            metaRow.getChildren().add(deleteBtn);
        }

        // Hide/Unhide button (Only Post Owner can hide)
        if (isPostOwner && comment.getUserId() != curUserId) {
            boolean isHidden = "HIDDEN".equals(comment.getStatus());
            Button hideBtn = new Button(isHidden ? "Unhide" : "Hide");
            hideBtn.setStyle("-fx-font-weight: bold; " + metaStyle);
            hideBtn.setOnMouseEntered(e -> hideBtn.setStyle("-fx-font-weight: bold; " + metaHover));
            hideBtn.setOnMouseExited(e -> hideBtn.setStyle("-fx-font-weight: bold; " + metaStyle));
            hideBtn.setOnAction(e -> toggleHideComment(comment));
            metaRow.getChildren().add(hideBtn);
        }
        contentCol.getChildren().add(metaRow);

        // ── Heart button (per-user like toggle) ──────────────────────────────────
        int curUserIdForLike = utils.SessionManager.getInstance().getCurrentUser().getId();

        // Check from DB if this user already liked this comment
        boolean alreadyLiked = false;
        try { alreadyLiked = commentService.hasUserLiked(comment.getId(), curUserIdForLike); } catch (Exception ignored) {}

        final boolean[] liked = {alreadyLiked};
        final int[] likeCount = {comment.getLikes()};

        VBox heartBox = new VBox(1);
        heartBox.setAlignment(Pos.TOP_CENTER);

        Button heartBtn = new Button(liked[0] ? "♥" : "♡");
        heartBtn.setStyle(liked[0]
            ? "-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-font-size: 15px; -fx-cursor: hand; -fx-padding: 0;"
            : "-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-font-size: 15px; -fx-cursor: hand; -fx-padding: 0;");

        Label likeCountLbl = new Label(likeCount[0] > 0 ? String.valueOf(likeCount[0]) : "");
        likeCountLbl.setStyle(liked[0]
            ? "-fx-text-fill: #ef4444; -fx-font-size: 10px;"
            : "-fx-text-fill: #94a3b8; -fx-font-size: 10px;");

        heartBox.getChildren().addAll(heartBtn, likeCountLbl);

        heartBtn.setOnAction(e -> {
            try {
                likeCount[0] = commentService.toggleCommentLike(comment.getId(), curUserIdForLike);
                liked[0] = commentService.hasUserLiked(comment.getId(), curUserIdForLike);
                
                // Notify Comment Owner
                if (liked[0] && comment.getUserId() != curUserIdForLike) {
                    Users liker = utils.SessionManager.getInstance().getCurrentUser();
                    new NotificationService().notifyCommentLike(comment.getUserId(), liker.getUsername(), comment.getId(), currentPost.getId());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (liked[0]) {
                heartBtn.setText("♥");
                heartBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-font-size: 15px; -fx-cursor: hand; -fx-padding: 0;");
                likeCountLbl.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 10px;");
            } else {
                heartBtn.setText("♡");
                heartBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-font-size: 15px; -fx-cursor: hand; -fx-padding: 0;");
                likeCountLbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px;");
            }
            likeCountLbl.setText(likeCount[0] > 0 ? String.valueOf(likeCount[0]) : "");
        });

        heartBtn.setOnMouseEntered(e -> {
            if (!liked[0]) heartBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-font-size: 15px; -fx-cursor: hand; -fx-padding: 0;");
        });
        heartBtn.setOnMouseExited(e -> {
            if (!liked[0]) heartBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-font-size: 15px; -fx-cursor: hand; -fx-padding: 0;");
        });

        mainRow.getChildren().addAll(avatarPane, contentCol, heartBox);

        // ── Inline reply box ─────────────────────────────────────────────────────
        VBox inlineReplyBox = new VBox(0);
        inlineReplyBox.setVisible(false);
        inlineReplyBox.setManaged(false);
        inlineReplyBox.setPadding(new Insets(6, 0, 0, avatarSize + 10));
        HBox replyInputRow = new HBox(8);
        replyInputRow.setAlignment(Pos.CENTER_LEFT);
        replyInputRow.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 18; -fx-padding: 7 12;");
        TextField replyField = new TextField();
        replyField.setPromptText("Add a reply...");
        replyField.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-text-fill: #1a1a2e; -fx-font-size: 13px; -fx-prompt-text-fill: #94a3b8;");
        HBox.setHgrow(replyField, Priority.ALWAYS);
        Button postReplyBtn = new Button("Post");
        postReplyBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #3897f0; -fx-font-weight: bold; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 0;");
        replyInputRow.getChildren().addAll(replyField, postReplyBtn);
        inlineReplyBox.getChildren().add(replyInputRow);

        replyBtn.setOnAction(e -> { boolean v = !inlineReplyBox.isVisible(); inlineReplyBox.setVisible(v); inlineReplyBox.setManaged(v); if (v) replyField.requestFocus(); });
        postReplyBtn.setOnAction(e -> { String b = replyField.getText(); if (b != null && !b.trim().isEmpty()) submitReply(comment, b); });

        // ── Thin separator ───────────────────────────────────────────────────────
        javafx.scene.control.Separator sep = new javafx.scene.control.Separator();
        sep.setStyle("-fx-background-color: #f1f5f9; -fx-opacity: 0.4; -fx-pref-height: 1px; -fx-max-height: 1px;");
        sep.setPadding(new Insets(3, 0, 0, 0));

        card.getChildren().addAll(mainRow, inlineEditBox, inlineReplyBox, sep);
        return card;
    }

    private Button createIconButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #636366; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 0;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #aeaeb2; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 0;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #636366; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 0;"));
        return btn;
    }

    /** Builds a circular avatar with an Instagram-style gradient ring. */
    private StackPane buildAvatarWithRing(Users user, int size) {
        // Outer gradient ring
        StackPane ring = new StackPane();
        ring.setPrefSize(size + 4, size + 4);
        ring.setMinSize(size + 4, size + 4);
        ring.setMaxSize(size + 4, size + 4);
        ring.setStyle("-fx-background-radius: 50; " +
                "-fx-background-color: linear-gradient(from 0% 100% to 100% 0%, #f09433 0%, #e6683c 25%, #dc2743 50%, #cc2366 75%, #bc1888 100%);");
        // Inner avatar (2px inset)
        StackPane inner = buildAvatar(user);
        inner.setPrefSize(size, size);
        inner.setMinSize(size, size);
        inner.setMaxSize(size, size);
        if (!inner.getChildren().isEmpty() && inner.getChildren().get(0) instanceof ImageView) {
            ImageView iv = (ImageView) inner.getChildren().get(0);
            iv.setFitWidth(size); iv.setFitHeight(size);
            iv.setClip(new javafx.scene.shape.Circle(size / 2.0, size / 2.0, size / 2.0));
        }
        // Dark gap between ring and photo (1 px)
        StackPane gap = new StackPane(inner);
        gap.setPrefSize(size + 2, size + 2);
        gap.setMinSize(size + 2, size + 2);
        gap.setMaxSize(size + 2, size + 2);
        gap.setStyle("-fx-background-radius: 50; -fx-background-color: white;");
        ring.getChildren().add(gap);
        return ring;
    }

    private StackPane buildAvatar(entities.Users user) {
        String username = (user != null) ? user.getUsername() : "Unknown";
        String imageUrl = (user != null) ? user.getImage() : null;

        StackPane circle = new StackPane();
        circle.setPrefSize(32, 32);
        circle.setMinSize(32, 32);
        circle.setMaxSize(32, 32);
        circle.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 50;");

        if (imageUrl == null || imageUrl.isEmpty()) {
            imageUrl = "https://api.dicebear.com/7.x/avataaars/png?seed=" + username;
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                javafx.scene.image.Image img;
                if (imageUrl.startsWith("http")) {
                    img = new javafx.scene.image.Image(imageUrl, true);
                } else {
                    java.io.File file = new java.io.File("src/main/resources/uploads/images/" + imageUrl);
                    if (file.exists()) {
                        img = new javafx.scene.image.Image(file.toURI().toString());
                    } else {
                        // If local file not found, use dicebear as fallback instead of initials
                        img = new javafx.scene.image.Image("https://api.dicebear.com/7.x/avataaars/png?seed=" + username, true);
                    }
                }

                if (img != null) {
                    javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(img);
                    imageView.setFitWidth(32);
                    imageView.setFitHeight(32);
                    imageView.setPreserveRatio(true);
                    
                    // Clip to circle
                    javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(16, 16, 16);
                    imageView.setClip(clip);
                    
                    circle.getChildren().add(imageView);
                    return circle;
                }
            } catch (Exception e) {}
        }

        // Final fallback: Initials (should rarely be reached now)
        char initial = (username != null && !username.isEmpty()) ? Character.toUpperCase(username.charAt(0)) : '?';
        Label initLabel = new Label(String.valueOf(initial));
        initLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #ce2d7c;");
        circle.getChildren().add(initLabel);

        return circle;
    }

    @FXML
    private void addComment() {
        String body = commentArea.getText().trim();
        String attachedGif = (String) gifPreview.getUserData();
        
        if (body.isEmpty() && attachedGif == null) return;

        // Combine text and GIF
        String tempBody = body;
        if (attachedGif != null) {
            tempBody = body + " [GIF]" + attachedGif;
        }
        final String finalBodyToProcess = tempBody;

        Task<Comment> task = new Task<>() {
            @Override
            protected Comment call() throws Exception {
                // 1. Correct spelling
                String corrected = TextCorrectionService.correctText(finalBodyToProcess);
                
                // 2. Moderate
                DetectBadWordService.ModerationResult mod = DetectBadWordService.moderate(corrected).join();
                
                Comment comment = new Comment();
                comment.setPostId(currentPost.getId());
                
                Users currentUser = utils.SessionManager.getInstance().getCurrentUser();
                if (currentUser != null) {
                    comment.setUserId(currentUser.getId());
                } else {
                    comment.setUserId(1);
                }
                
                comment.setBody(mod.moderatedText);
                comment.setProfane(mod.isProfane);
                comment.setProfaneWords(mod.profaneWordsCount);
                comment.setGrammarErrors(mod.grammarErrorsCount);
                
                if (mod.isProfane) {
                    comment.setStatus("FLAGGED");
                } else {
                    comment.setStatus("APPROVED");
                }
                
                comment.setCreatedAt(LocalDateTime.now());
                commentService.ajouter(comment);
                return comment;
            }
        };

        task.setOnSucceeded(e -> {
            Comment comment = task.getValue();
            // Notify Post Owner
            Users currentUser = utils.SessionManager.getInstance().getCurrentUser();
            if (currentUser != null && currentPost.getUserId() != currentUser.getId()) {
                new NotificationService().notifyComment(currentPost.getUserId(), currentUser.getUsername(), currentPost.getId());
            }

            commentArea.clear();
            removeAttachment();
            loadCommentsByPost();
        });

        new Thread(task).start();
    }

    private void submitEdit(Comment comment, String newBody) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                String corrected = TextCorrectionService.correctText(newBody);
                DetectBadWordService.ModerationResult mod = DetectBadWordService.moderate(corrected).join();
                
                comment.setBody(mod.moderatedText);
                comment.setProfane(mod.isProfane);
                comment.setProfaneWords(mod.profaneWordsCount);
                comment.setGrammarErrors(mod.grammarErrorsCount);
                
                if (mod.isProfane) {
                    comment.setStatus("FLAGGED");
                } else {
                    comment.setStatus("APPROVED");
                }
                
                commentService.modifier(comment.getId(), comment);
                return null;
            }
        };
        task.setOnSucceeded(e -> loadCommentsByPost());
        new Thread(task).start();
    }

    private void submitReply(Comment parent, String body) {
        Task<Comment> task = new Task<>() {
            @Override
            protected Comment call() throws Exception {
                String corrected = TextCorrectionService.correctText(body);
                DetectBadWordService.ModerationResult mod = DetectBadWordService.moderate(corrected).join();
                
                Comment reply = new Comment();
                reply.setPostId(currentPost.getId());
                reply.setParentCommentId(parent.getId());
                reply.setUserId(utils.SessionManager.getInstance().getCurrentUser().getId());
                
                reply.setBody(mod.moderatedText);
                reply.setProfane(mod.isProfane);
                reply.setProfaneWords(mod.profaneWordsCount);
                reply.setGrammarErrors(mod.grammarErrorsCount);
                
                if (mod.isProfane) {
                    reply.setStatus("FLAGGED");
                } else {
                    reply.setStatus("APPROVED");
                }
                
                reply.setCreatedAt(LocalDateTime.now());
                commentService.ajouter(reply);
                return reply;
            }
        };

        task.setOnSucceeded(e -> {
            Comment reply = task.getValue();
            // Notify Comment Owner
            if (parent.getUserId() != reply.getUserId()) {
                Users replier = utils.SessionManager.getInstance().getCurrentUser();
                new NotificationService().notifyReply(parent.getUserId(), replier.getUsername(), reply.getId(), currentPost.getId());
            }
            loadCommentsByPost();
        });
        
        new Thread(task).start();
    }

    private void toggleHideComment(Comment comment) {
        String action = "HIDDEN".equals(comment.getStatus()) ? "Unhide" : "Hide";
        if (gui.util.AlertHelper.showCustomAlert(action + "?", "Are you sure you want to " + action.toLowerCase() + " this comment?", gui.util.AlertHelper.AlertType.CONFIRMATION)) {
            try {
                if ("HIDDEN".equals(comment.getStatus())) {
                    comment.setStatus("APPROVED");
                } else {
                    comment.setStatus("HIDDEN");
                }
                commentService.modifier(comment.getId(), comment);
                loadCommentsByPost();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void deleteComment(int id) {
        if (gui.util.AlertHelper.showCustomAlert("Delete?", "Remove this comment?", gui.util.AlertHelper.AlertType.CONFIRMATION)) {
            try {
                commentService.supprimer(id);
                loadCommentsByPost();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @FXML
    public void goBackToPosts(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/post/displayPost.fxml"));
            Parent root = loader.load();
            DisplayPostController controller = loader.getController();
            controller.setAdminMode(this.isAdminMode);
            StackPane contentArea = (StackPane) ((Node) event.getSource()).getScene().lookup("#contentArea");
            if (contentArea != null) contentArea.getChildren().setAll(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void onSharePost(ActionEvent event) {
        if (currentPost == null) return;
        
        Button sourceBtn = (Button) event.getSource();
        ContextMenu shareMenu = new ContextMenu();
        shareMenu.setStyle("-fx-background-radius: 10; -fx-padding: 5;");

        MenuItem twitterItem = new MenuItem("Share on Twitter");
        twitterItem.setOnAction(e -> openSocialLink("https://twitter.com/intent/tweet?text="));

        MenuItem whatsappItem = new MenuItem("Share on WhatsApp");
        whatsappItem.setOnAction(e -> openSocialLink("https://wa.me/?text="));

        MenuItem copyItem = new MenuItem("Copy Link");
        copyItem.setOnAction(e -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            String postUrl = "https://creaco.com/post/" + currentPost.getId();
            content.putString(postUrl);
            clipboard.setContent(content);
            gui.util.AlertHelper.showCustomAlert("Copied", "Post link copied to clipboard!", gui.util.AlertHelper.AlertType.INFORMATION);
        });

        shareMenu.getItems().addAll(twitterItem, whatsappItem, copyItem);
        shareMenu.show(sourceBtn, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    private void openSocialLink(String baseUrl) {
        try {
            String postUrl = "https://creaco.com/post/" + currentPost.getId();
            String encodedUrl = URLEncoder.encode(postUrl, StandardCharsets.UTF_8);
            String url = baseUrl + encodedUrl;

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception e) {
            e.printStackTrace();
            gui.util.AlertHelper.showCustomAlert("Error", "Could not open share link.", gui.util.AlertHelper.AlertType.ERROR);
        }
    }

    @FXML
    private void onPinPost() {
        if (currentPost == null) return;
        try {
            int userId = utils.SessionManager.getInstance().getCurrentUser().getId();
            String msg = postService.handleTogglePin(currentPost, isAdminMode, userId);
            if (msg != null) {
                gui.util.AlertHelper.showCustomAlert("Pin Status", msg, gui.util.AlertHelper.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            gui.util.AlertHelper.showCustomAlert("Error", e.getMessage(), gui.util.AlertHelper.AlertType.WARNING);
        }
    }

    @FXML
    private void onLockPost() {
        if (currentPost == null) return;
        try {
            currentPost.setCommentLocked(!currentPost.isCommentLocked());
            postService.modifier(currentPost.getId(), currentPost);
            String status = currentPost.isCommentLocked() ? "LOCKED" : "UNLOCKED";
            gui.util.AlertHelper.showCustomAlert("Post Status", "Comments are now " + status, gui.util.AlertHelper.AlertType.INFORMATION);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onEditPost(ActionEvent event) {
        if (currentPost == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/post/updatePost.fxml"));
            Parent root = loader.load();
            gui.post.UpdatePostController controller = loader.getController();
            controller.setPost(currentPost);
            StackPane contentArea = (StackPane) ((Node) event.getSource()).getScene().lookup("#contentArea");
            if (contentArea != null) contentArea.getChildren().setAll(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void onDeletePost(ActionEvent event) {
        if (currentPost == null) return;
        if (gui.util.AlertHelper.showCustomAlert("Delete?", "Are you sure you want to delete this post?", gui.util.AlertHelper.AlertType.CONFIRMATION)) {
            try {
                postService.supprimer(currentPost.getId());
                goBackToPosts(event);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void openGifPicker() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/post/gif_picker.fxml"));
            Parent root = loader.load();
            
            GifPickerModalController controller = loader.getController();
            controller.setOnGifSelected(gifUrl -> {
                gifPreview.setImage(new Image(gifUrl, true));
                gifPreview.setUserData(gifUrl);
                
                // Add rounded corners to preview
                javafx.scene.shape.Rectangle previewClip = new javafx.scene.shape.Rectangle(gifPreview.getFitWidth(), gifPreview.getFitHeight());
                previewClip.setArcWidth(12);
                previewClip.setArcHeight(12);
                gifPreview.setClip(previewClip);
                
                attachmentPreview.setVisible(true);
                attachmentPreview.setManaged(true);
            });

            Stage stage = new Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setTitle("Select a GIF");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            gui.util.AlertHelper.showCustomAlert("Error", "Could not open GIF picker.", gui.util.AlertHelper.AlertType.ERROR);
        }
    }

    @FXML
    private void openEmojiPicker() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/post/emoji_picker.fxml"));
            Parent root = loader.load();
            
            EmojiPickerModalController controller = loader.getController();
            controller.setOnEmojiSelected(emoji -> {
                commentArea.appendText(emoji);
            });

            Stage stage = new Stage();
            stage.initModality(javafx.stage.Modality.NONE);
            stage.setTitle("Emojis");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void removeAttachment() {
        gifPreview.setImage(null);
        gifPreview.setUserData(null);
        attachmentPreview.setVisible(false);
        attachmentPreview.setManaged(false);
    }

    public void scrollToComment(int commentId) {
        javafx.application.Platform.runLater(() -> {
            Node target = commentsContainer.lookup("#comment-card-" + commentId);
            if (target != null) {
                // Highlight effect
                String originalStyle = target.getStyle();
                target.setStyle(originalStyle + "; -fx-background-color: #fff1f2;"); // Light pink highlight
                
                // Fade out highlight after 3 seconds
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
                pause.setOnFinished(e -> target.setStyle(originalStyle));
                pause.play();

                // Scroll to target (find the ScrollPane first)
                Node parent = commentsContainer.getParent();
                while (parent != null && !(parent instanceof ScrollPane)) {
                    parent = parent.getParent();
                }
                
                if (parent instanceof ScrollPane) {
                    ScrollPane scrollPane = (ScrollPane) parent;
                    double scrollHeight = commentsContainer.getBoundsInLocal().getHeight();
                    double nodeY = target.getBoundsInParent().getMinY();
                    scrollPane.setVvalue(nodeY / scrollHeight);
                }
            }
        });
    }

    private String formatDate(LocalDateTime date) {
        return (date != null) ? date.format(DISPLAY_DATE_FORMAT) : "-";
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
