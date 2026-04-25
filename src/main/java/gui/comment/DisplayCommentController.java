package gui.comment;

import entities.Comment;
import entities.Post;
import entities.Users;
import gui.post.DisplayPostController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import services.forum.CommentService;
import services.UserService;
import services.forum.PostService;
import entities.ReactionType;
import java.util.Map;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

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

            // Set Author Avatar
            if (author != null && postAuthorAvatarContainer != null) {
                StackPane avatar = buildAvatar(author);
                // Adjust size for the header
                avatar.setPrefSize(45, 45);
                avatar.setMinSize(45, 45);
                avatar.setMaxSize(45, 45);
                // Update the clip if it was used (buildAvatar uses 16 for 32x32, so we need 22.5 for 45x45)
                if (!avatar.getChildren().isEmpty() && avatar.getChildren().get(0) instanceof javafx.scene.image.ImageView) {
                    javafx.scene.image.ImageView iv = (javafx.scene.image.ImageView) avatar.getChildren().get(0);
                    iv.setFitWidth(45);
                    iv.setFitHeight(45);
                    iv.setClip(new javafx.scene.shape.Circle(22.5, 22.5, 22.5));
                } else if (!avatar.getChildren().isEmpty() && avatar.getChildren().get(0) instanceof Label) {
                    Label l = (Label) avatar.getChildren().get(0);
                    l.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ce2d7c;");
                }
                postAuthorAvatarContainer.getChildren().setAll(avatar.getChildren());
                postAuthorAvatarContainer.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 12;");
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
            Users currentUser = utils.SessionManager.getInstance().getCurrentUser();
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
            repliesBadge.setText("💬 " + allComments.size() + " REPLIES");
            
            if (allComments.isEmpty()) {
                commentsContainer.getChildren().add(emptyState);
                emptyState.setVisible(true);
                return;
            }
            emptyState.setVisible(false);

            List<Comment> roots = allComments.stream()
                    .filter(c -> c.getParentCommentId() == null)
                    .sorted((c1, c2) -> c2.getCreatedAt().compareTo(c1.getCreatedAt()))
                    .collect(Collectors.toList());

            for (Comment root : roots) {
                renderCommentThread(root, allComments, 0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void renderCommentThread(Comment comment, List<Comment> allComments, int depth) {
        VBox card = createCommentCard(comment);
        if (depth > 0) {
            VBox indentedContainer = new VBox(card);
            indentedContainer.setPadding(new Insets(0, 0, 0, 50 * Math.min(depth, 3)));
            commentsContainer.getChildren().add(indentedContainer);
        } else {
            commentsContainer.getChildren().add(card);
        }

        List<Comment> replies = allComments.stream()
                .filter(c -> c.getParentCommentId() != null && c.getParentCommentId() == comment.getId())
                .sorted((c1, c2) -> c1.getCreatedAt().compareTo(c2.getCreatedAt()))
                .collect(Collectors.toList());

        for (Comment reply : replies) {
            renderCommentThread(reply, allComments, depth + 1);
        }
    }

    private VBox createCommentCard(Comment comment) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: transparent;");
        card.setPadding(new Insets(10, 0, 10, 0));

        Users user = userService.getUserById(comment.getUserId());
        String username = (user != null) ? user.getUsername() : "Unknown";
        String dateStr = formatDate(comment.getCreatedAt());

        // Header: Avatar + Username + Date
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        StackPane avatar = buildAvatar(user);
        Label userLabel = new Label(username);
        userLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d3748; -fx-font-size: 13px;");
        Label dateLabel = new Label(dateStr);
        dateLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");
        
        header.getChildren().addAll(avatar, userLabel, dateLabel);

        // Body: Content
        Label bodyLabel = new Label(safeText(comment.getBody()));
        bodyLabel.setWrapText(true);
        bodyLabel.setStyle("-fx-text-fill: #4a5568; -fx-font-size: 15px; -fx-padding: 0 0 0 40;");

        // Action Bar
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setPadding(new Insets(0, 0, 0, 40));

        Button replyBtn = createIconButton("↩ REPLY");
        Button editBtn = createIconButton("📝 EDIT");
        Button deleteBtn = createIconButton("🗑 DELETE");

        actions.getChildren().add(replyBtn);
        
        int currentUserId = utils.SessionManager.getInstance().getCurrentUser().getId();
        if (comment.getUserId() == currentUserId) {
            actions.getChildren().add(editBtn);
        }
        
        // Delete visibility: Author of comment OR Admin OR Author of the thread (Post)
        boolean isPostOwner = (currentPost != null && currentPost.getUserId() == currentUserId);
        if (comment.getUserId() == currentUserId || isAdminMode || isPostOwner) {
            actions.getChildren().add(deleteBtn);
        }

        // Inline Edit Section (Hidden by default)
        VBox inlineEditBox = new VBox(10);
        inlineEditBox.setVisible(false);
        inlineEditBox.setManaged(false);
        inlineEditBox.setPadding(new Insets(10, 0, 10, 40));
        
        VBox editAreaBox = new VBox(5);
        editAreaBox.setStyle("-fx-background-color: #fef2f2; -fx-background-radius: 15; -fx-border-color: #fca5a5; -fx-border-radius: 15; -fx-padding: 2;");
        
        TextArea editTextArea = new TextArea(comment.getBody());
        editTextArea.setPrefHeight(80);
        editTextArea.setWrapText(true);
        editTextArea.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent; -fx-font-size: 14px;");
        
        HBox editBtnBox = new HBox();
        editBtnBox.setAlignment(Pos.BOTTOM_RIGHT);
        editBtnBox.setPadding(new Insets(0, 10, 10, 0));
        Button saveEditBtn = new Button("SAVE CHANGES");
        saveEditBtn.setStyle("-fx-background-color: #ce2d7c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 8 20;");
        
        editBtnBox.getChildren().add(saveEditBtn);
        editAreaBox.getChildren().addAll(editTextArea, editBtnBox);
        inlineEditBox.getChildren().add(editAreaBox);

        // Inline Reply Section (Hidden by default)
        VBox inlineReplyBox = new VBox(10);
        inlineReplyBox.setVisible(false);
        inlineReplyBox.setManaged(false);
        inlineReplyBox.setPadding(new Insets(10, 0, 10, 40));
        
        VBox replyAreaBox = new VBox(5);
        replyAreaBox.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-color: #e2e8f0; -fx-border-radius: 15; -fx-padding: 2;");
        
        TextArea replyTextArea = new TextArea();
        replyTextArea.setPromptText("Your reply...");
        replyTextArea.setPrefHeight(80);
        replyTextArea.setWrapText(true);
        replyTextArea.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent; -fx-font-size: 14px;");
        
        HBox replyBtnBox = new HBox();
        replyBtnBox.setAlignment(Pos.BOTTOM_RIGHT);
        replyBtnBox.setPadding(new Insets(0, 10, 10, 0));
        Button postReplyBtn = new Button("REPLY");
        postReplyBtn.setStyle("-fx-background-color: -fx-primary-gradient; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 8 20;");
        
        replyBtnBox.getChildren().add(postReplyBtn);
        replyAreaBox.getChildren().addAll(replyTextArea, replyBtnBox);
        inlineReplyBox.getChildren().add(replyAreaBox);

        // Actions Logic
        editBtn.setOnAction(e -> {
            boolean visible = !inlineEditBox.isVisible();
            inlineEditBox.setVisible(visible);
            inlineEditBox.setManaged(visible);
            if (visible) {
                editTextArea.setText(comment.getBody());
                editTextArea.requestFocus();
            }
        });

        saveEditBtn.setOnAction(e -> {
            String newBody = editTextArea.getText();
            if (newBody != null && !newBody.trim().isEmpty()) {
                submitEdit(comment, newBody);
            }
        });

        replyBtn.setOnAction(e -> {
            boolean visible = !inlineReplyBox.isVisible();
            inlineReplyBox.setVisible(visible);
            inlineReplyBox.setManaged(visible);
            if (visible) replyTextArea.requestFocus();
        });

        postReplyBtn.setOnAction(e -> {
            String body = replyTextArea.getText();
            if (body != null && !body.trim().isEmpty()) {
                submitReply(comment, body);
            }
        });

        deleteBtn.setOnAction(e -> deleteComment(comment.getId()));
        
        card.getChildren().addAll(header, bodyLabel, actions, inlineEditBox, inlineReplyBox);
        return card;
    }

    private Button createIconButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 0;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: -fx-primary-pink; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 0;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 0;"));
        return btn;
    }

    private StackPane buildAvatar(entities.Users user) {
        String username = (user != null) ? user.getUsername() : "Unknown";
        String imageUrl = (user != null) ? user.getImage() : null;

        StackPane circle = new StackPane();
        circle.setPrefSize(32, 32);
        circle.setMinSize(32, 32);
        circle.setMaxSize(32, 32);
        circle.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 50;");

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
                        img = null;
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
            } catch (Exception e) {
                // Fallback to initials
            }
        }

        char initial = (username != null && !username.isEmpty()) ? Character.toUpperCase(username.charAt(0)) : '?';
        Label initLabel = new Label(String.valueOf(initial));
        initLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #ce2d7c;");
        circle.getChildren().add(initLabel);

        return circle;
    }

    @FXML
    private void addComment() {
        String body = commentArea.getText();
        if (body == null || body.trim().isEmpty()) return;

        Comment comment = new Comment();
        comment.setPostId(currentPost.getId());
        comment.setUserId(utils.SessionManager.getInstance().getCurrentUser().getId());
        comment.setBody(body);
        comment.setCreatedAt(LocalDateTime.now());

        try {
            commentService.ajouter(comment);
            commentArea.clear();
            loadCommentsByPost();
            // No alert popups as requested
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void submitEdit(Comment comment, String newBody) {
        try {
            comment.setBody(newBody);
            commentService.modifier(comment.getId(), comment);
            loadCommentsByPost();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void submitReply(Comment parent, String body) {
        Comment reply = new Comment();
        reply.setPostId(currentPost.getId());
        reply.setParentCommentId(parent.getId());
        reply.setUserId(utils.SessionManager.getInstance().getCurrentUser().getId());
        reply.setBody(body);
        reply.setCreatedAt(LocalDateTime.now());
        try {
            commentService.ajouter(reply);
            loadCommentsByPost();
        } catch (SQLException e) { e.printStackTrace(); }
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
    private void onSharePost() {
        // Dummy share logic
        gui.util.AlertHelper.showCustomAlert("Share", "Post link copied to clipboard!", gui.util.AlertHelper.AlertType.INFORMATION);
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

    private String formatDate(LocalDateTime date) {
        return (date != null) ? date.format(DISPLAY_DATE_FORMAT) : "-";
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
