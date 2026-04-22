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

    private final CommentService commentService = new CommentService();
    private final UserService userService = new UserService();
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
        
        StackPane avatar = buildAvatar(username);
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
        
        int currentUserId = isAdminMode ? 5 : 1;
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

    private StackPane buildAvatar(String username) {
        char initial = (username != null && !username.isEmpty()) ? Character.toUpperCase(username.charAt(0)) : '?';
        Label initLabel = new Label(String.valueOf(initial));
        initLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #ce2d7c;");

        StackPane circle = new StackPane(initLabel);
        circle.setPrefSize(32, 32);
        circle.setMinSize(32, 32);
        circle.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 50%;");
        return circle;
    }

    @FXML
    private void addComment() {
        String body = commentArea.getText();
        if (body == null || body.trim().isEmpty()) return;

        Comment comment = new Comment();
        comment.setPostId(currentPost.getId());
        comment.setUserId(isAdminMode ? 5 : 1);
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
        reply.setUserId(isAdminMode ? 5 : 1);
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

    private String formatDate(LocalDateTime date) {
        return (date != null) ? date.format(DISPLAY_DATE_FORMAT) : "-";
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
