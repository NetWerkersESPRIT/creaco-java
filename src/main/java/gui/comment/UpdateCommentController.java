package gui.comment;

import entities.Comment;
import entities.Post;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import services.forum.CommentService;

import java.io.IOException;
import java.sql.SQLException;

public class UpdateCommentController {

    @FXML
    private TextArea bodyArea;

    private final CommentService commentService = new CommentService();
    private Comment commentToUpdate;
    private Post currentPost;
    private boolean isAdminMode = false;

    public void setAdminMode(boolean isAdminMode) {
        this.isAdminMode = isAdminMode;
    }

    public void setData(Post post, Comment comment) {
        this.currentPost = post;
        this.commentToUpdate = comment;
        if (comment != null) {
            bodyArea.setText(comment.getBody());
        }
    }

    @FXML
    private void updateComment(ActionEvent event) {
        if (commentToUpdate == null || currentPost == null) return;

        String body = bodyArea.getText().trim();

        if (body.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Comment cannot be empty.");
            return;
        }

        if (body.length() < 2) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Comment must be at least 2 characters long.");
            return;
        }

        commentToUpdate.setBody(body);

        try {
            commentService.modifier(commentToUpdate.getId(), commentToUpdate);
            goBack(event);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not update the comment.");
        }
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/comment/displayComment.fxml"));
            Parent root = loader.load();
            
            DisplayCommentController controller = loader.getController();
            controller.setAdminMode(this.isAdminMode);
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
