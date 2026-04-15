package gui.comment;

import entities.Comment;
import entities.Post;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import services.forum.CommentService;
import services.UserService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class DisplayCommentController {

    private static final DateTimeFormatter DISPLAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    private VBox commentsContainer;
    
    @FXML
    private Label postTitleLabel;
    
    @FXML
    private Label postContentLabel;
    
    @FXML
    private Label statusLabel;

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
            loadCommentsByPost();
        }
    }

    private void loadCommentsByPost() {
        if (currentPost == null) return;
        
        commentsContainer.getChildren().clear();
        try {
            List<Comment> allComments = commentService.getCommentsByPost(currentPost.getId());
            statusLabel.setText(allComments.size() + " comment(s) found.");
            
            if (allComments.isEmpty()) {
                Label emptyLabel = new Label("No comments yet. Be the first to comment!");
                emptyLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic; -fx-font-size: 14px;");
                commentsContainer.getChildren().add(emptyLabel);
                return;
            }

            // --- THREADING LOGIC ---
            // 1. Get Root Comments (parent == null)
            List<Comment> roots = allComments.stream()
                    .filter(c -> c.getParentCommentId() == null)
                    .sorted((c1, c2) -> c2.getCreatedAt().compareTo(c1.getCreatedAt())) // Newest first
                    .collect(Collectors.toList());

            for (Comment root : roots) {
                renderCommentThread(root, allComments, 0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Database error: Could not load comments.");
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not load comments.");
        }
    }

    private void renderCommentThread(Comment comment, List<Comment> allComments, int depth) {
        VBox card = createCommentCard(comment);
        
        // Add indentation for replies
        if (depth > 0) {
            VBox indentedContainer = new VBox(card);
            indentedContainer.setPadding(new Insets(0, 0, 0, 30 * Math.min(depth, 3))); // Max indentation
            commentsContainer.getChildren().add(indentedContainer);
        } else {
            commentsContainer.getChildren().add(card);
        }

        // Find replies to this comment
        List<Comment> replies = allComments.stream()
                .filter(c -> c.getParentCommentId() != null && c.getParentCommentId() == comment.getId())
                .sorted((c1, c2) -> c1.getCreatedAt().compareTo(c2.getCreatedAt())) // Replies in chronological order
                .collect(Collectors.toList());

        for (Comment reply : replies) {
            renderCommentThread(reply, allComments, depth + 1);
        }
    }

    private VBox createCommentCard(Comment comment) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 14; "
                + "-fx-border-color: #dbe4f0; -fx-border-radius: 14;");

        // ── Author row: avatar + username ──────────────────────
        entities.Users author = userService.getUserById(comment.getUserId());
        String username = (author != null) ? author.getUsername() : "Unknown";
        javafx.scene.layout.StackPane avatar = buildAvatar(username);
        Label usernameLabel = new Label(username);
        usernameLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #355388;");
        HBox authorRow = new HBox(8);
        authorRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        authorRow.getChildren().addAll(avatar, usernameLabel);

        // Add (admin) tag if applicable
        boolean isAdmin = (comment.getUserId() == 5) || 
                          (author != null && author.getRole() != null && author.getRole().toLowerCase().contains("admin"));
        if (isAdmin) {
            Label adminLabel = new Label("(admin)");
            adminLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
            authorRow.getChildren().add(adminLabel);
        }

        Label bodyLabel = new Label(safeText(comment.getBody()));
        bodyLabel.setWrapText(true);
        bodyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #334155;");

        HBox footer = new HBox(10);
        
        String metaText = "Posted " + formatDate(comment.getCreatedAt());
        Label meta = new Label(metaText);
        meta.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

        Button replyButton = createActionButton("Reply", "#94a3b8", "transparent");
        replyButton.setOnAction(event -> handleReply(event, comment));

        Button editButton = createActionButton("Edit", "#94a3b8", "transparent");
        editButton.setOnAction(event -> openEdit(event, comment));
        
        Button deleteButton = createActionButton("Delete", "#94a3b8", "transparent");
        deleteButton.setOnAction(event -> deleteComment(comment.getId()));

        footer.getChildren().addAll(meta, replyButton, editButton, deleteButton);
        card.getChildren().addAll(authorRow, bodyLabel, footer);
        return card;
    }

    private javafx.scene.layout.StackPane buildAvatar(String username) {
        String[] colors = {"#355388", "#1e8a5e", "#7c3aed", "#c2410c", "#0369a1",
                           "#047857", "#b45309", "#9d174d", "#1d4ed8", "#065f46"};
        char initial = (username != null && !username.isEmpty()) ? Character.toUpperCase(username.charAt(0)) : '?';
        String color = colors[Math.abs(initial) % colors.length];

        Label initLabel = new Label(String.valueOf(initial));
        initLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: white;");

        javafx.scene.layout.StackPane circle = new javafx.scene.layout.StackPane(initLabel);
        circle.setPrefSize(30, 30);
        circle.setMinSize(30, 30);
        circle.setMaxSize(30, 30);
        circle.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 50%;");
        return circle;
    }

    private Button createActionButton(String text, String textColor, String backgroundColor) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + backgroundColor + "; -fx-text-fill: " + textColor
                + "; -fx-background-radius: 8; -fx-padding: 2 6; -fx-cursor: hand; -fx-font-size: 12px;");
        return button;
    }

    @FXML
    private void handleReply(ActionEvent event, Comment parentComment) {
        if (currentPost == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/comment/addComment.fxml"));
            Parent root = loader.load();
            
            AddCommentController controller = loader.getController();
            controller.setAdminMode(this.isAdminMode);
            controller.setPost(currentPost, parentComment.getId());
            
            StackPane contentArea = (StackPane) ((Node) event.getSource()).getScene().lookup("#contentArea");
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goAddComment(ActionEvent event) {
        if (currentPost == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/comment/addComment.fxml"));
            Parent root = loader.load();
            
            AddCommentController controller = loader.getController();
            controller.setAdminMode(this.isAdminMode);
            controller.setPost(currentPost);
            
            StackPane contentArea = (StackPane) ((Node) event.getSource()).getScene().lookup("#contentArea");
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openEdit(ActionEvent event, Comment comment) {
        if (currentPost == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/comment/updateComment.fxml"));
            Parent root = loader.load();
            
            UpdateCommentController controller = loader.getController();
            controller.setData(currentPost, comment);
            
            StackPane contentArea = (StackPane) ((Node) event.getSource()).getScene().lookup("#contentArea");
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteComment(int commentId) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setContentText("Are you sure you want to delete this comment?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    commentService.supprimer(commentId);
                    loadCommentsByPost(); // Refresh list dynamically
                } catch (SQLException exception) {
                    exception.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Could not delete the comment.");
                }
            }
        });
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/post/displayPost.fxml"));
            StackPane contentArea = (StackPane) ((Node) event.getSource()).getScene().lookup("#contentArea");
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String formatDate(LocalDateTime date) {
        if (date == null) return "-";
        return date.format(DISPLAY_DATE_FORMAT);
    }
    
    private String safeText(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
