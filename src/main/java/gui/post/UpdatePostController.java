package gui.post;

import entities.Post;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import services.forum.PostService;

import java.io.IOException;
import java.sql.SQLException;

public class UpdatePostController {

    @FXML
    private TextField titleField;
    @FXML
    private TextArea contentArea;

    private PostService postService = new PostService();
    private Post postToUpdate;

    public void setPost(Post post) {
        this.postToUpdate = post;
        titleField.setText(post.getTitle());
        contentArea.setText(post.getContent());
    }

    @FXML
    private void updatePost(ActionEvent event) {
        if (postToUpdate == null) return;

        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();

        if (title.isEmpty() || content.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "All fields are required.");
            return;
        }
        
        if (title.length() < 3) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Title must be at least 3 characters.");
            return;
        }

        postToUpdate.setTitle(title);
        postToUpdate.setContent(content);

        try {
            postService.modifier(postToUpdate.getId(), postToUpdate);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Post updated successfully!");
            goBack(event);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not update the post.");
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
