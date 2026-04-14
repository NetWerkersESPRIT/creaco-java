package gui.comment;

import entities.Comment;
import entities.Post;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import services.forum.CommentService;

import java.io.IOException;
import java.sql.SQLException;

public class AddCommentController {

    @FXML
    private TextArea bodyArea;
    @FXML
    private Label postTitleLabel;

    private final CommentService commentService = new CommentService();
    private Post currentPost;

    public void setPost(Post post) {
        this.currentPost = post;
        if (post != null) {
            postTitleLabel.setText("Replying to: " + post.getTitle());
        }
    }

    @FXML
    private void saveComment(ActionEvent event) {
        if (currentPost == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No post selected!");
            return;
        }

        String body = bodyArea.getText().trim();

        if (body.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Comment cannot be empty.");
            return;
        }

        if (body.length() < 2) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Comment must be at least 2 characters long.");
            return;
        }

        Comment c = new Comment();
        c.setBody(body);
        c.setStatus("Active");
        c.setPostId(currentPost.getId());
        c.setUserId(1); // Default user

        try {
            commentService.ajouter(c);
            goBack(event);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not save the comment.");
        }
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/comment/displayComment.fxml"));
            Parent root = loader.load();
            
            DisplayCommentController controller = loader.getController();
            controller.setPost(currentPost);
            
            StackPane contentArea = (StackPane) ((Node) event.getSource()).getScene().lookup("#contentArea");
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
