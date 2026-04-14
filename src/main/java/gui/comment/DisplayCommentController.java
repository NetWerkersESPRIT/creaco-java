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

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
    private Post currentPost;

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
            List<Comment> comments = commentService.getCommentsByPost(currentPost.getId());
            statusLabel.setText(comments.size() + " comment(s) found.");
            
            if (comments.isEmpty()) {
                Label emptyLabel = new Label("No comments yet. Be the first to comment!");
                emptyLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic; -fx-font-size: 14px;");
                commentsContainer.getChildren().add(emptyLabel);
                return;
            }

            for (Comment comment : comments) {
                VBox card = createCommentCard(comment);
                commentsContainer.getChildren().add(card);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Database error: Could not load comments.");
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not load comments.");
        }
    }

    private VBox createCommentCard(Comment comment) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 14; "
                + "-fx-border-color: #dbe4f0; -fx-border-radius: 14;");

        Label bodyLabel = new Label(safeText(comment.getBody()));
        bodyLabel.setWrapText(true);
        bodyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #334155;");

        HBox footer = new HBox(10);
        
        // Display ONLY comment creation meta
        String metaText = "Posted " + formatDate(comment.getCreatedAt());
        Label meta = new Label(metaText);
        meta.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button editButton = createActionButton("Edit", "#355388", "#eef3fb");
        editButton.setOnAction(event -> openEdit(event, comment));
        
        Button deleteButton = createActionButton("Delete", "#c62828", "#fdecec");
        deleteButton.setOnAction(event -> deleteComment(comment.getId()));

        footer.getChildren().addAll(meta, spacer, editButton, deleteButton);
        card.getChildren().addAll(bodyLabel, footer);
        return card;
    }

    private Button createActionButton(String text, String textColor, String backgroundColor) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + backgroundColor + "; -fx-text-fill: " + textColor
                + "; -fx-background-radius: 12; -fx-padding: 8 14 8 14; -fx-font-weight: bold; -fx-font-size: 11px;");
        return button;
    }

    @FXML
    private void goAddComment(ActionEvent event) {
        if (currentPost == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/comment/addComment.fxml"));
            Parent root = loader.load();
            
            AddCommentController controller = loader.getController();
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
