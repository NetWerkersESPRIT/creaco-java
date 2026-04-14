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
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import services.forum.CommentService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class DisplayCommentController {

    @FXML
    private VBox commentsContainer;
    @FXML
    private Label postTitleLabel;
    @FXML
    private Label postContentLabel;

    private CommentService commentService = new CommentService();
    private Post currentPost;

    public void setPost(Post post) {
        this.currentPost = post;
        if (post != null) {
            postTitleLabel.setText(post.getTitle());
            postContentLabel.setText(post.getContent());
            loadCommentsByPost();
        }
    }

    private void loadCommentsByPost() {
        if (currentPost == null) return;
        
        commentsContainer.getChildren().clear();
        try {
            List<Comment> comments = commentService.getCommentsByPost(currentPost.getId());
            
            if (comments.isEmpty()) {
                Label emptyLabel = new Label("No comments yet. Be the first to comment!");
                emptyLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");
                commentsContainer.getChildren().add(emptyLabel);
                return;
            }

            for (Comment comment : comments) {
                VBox card = createCommentCard(comment);
                commentsContainer.getChildren().add(card);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not load comments.");
        }
    }

    private VBox createCommentCard(Comment comment) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 5; -fx-border-color: #e0e0e0; -fx-border-radius: 5;");

        Label bodyLabel = new Label(comment.getBody());
        bodyLabel.setWrapText(true);
        bodyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");

        HBox buttonBox = new HBox(10);
        
        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        editBtn.setOnAction(e -> openEdit(e, comment));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        deleteBtn.setOnAction(e -> deleteComment(comment));

        buttonBox.getChildren().addAll(editBtn, deleteBtn);
        
        card.getChildren().addAll(bodyLabel, buttonBox);
        return card;
    }

    @FXML
    private void goAddComment(ActionEvent event) {
        if (currentPost == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/comment/addComment.fxml"));
            Parent root = loader.load();
            AddCommentController controller = loader.getController();
            controller.setPost(currentPost);
            
            StackPane parentContainer = (StackPane) ((Node) event.getSource()).getScene().lookup("#contentArea");
            parentContainer.getChildren().setAll(root);
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
            
            StackPane parentContainer = (StackPane) ((Node) event.getSource()).getScene().lookup("#contentArea");
            parentContainer.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteComment(Comment comment) {
        try {
            commentService.supprimer(comment.getId());
            showAlert(Alert.AlertType.INFORMATION, "Deleted", "Comment deleted successfully.");
            loadCommentsByPost(); // Refresh list
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not delete comment.");
        }
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/post/displayPost.fxml"));
            StackPane parentContainer = (StackPane) ((Node) event.getSource()).getScene().lookup("#contentArea");
            parentContainer.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
