package gui.post;

import entities.Post;
import gui.comment.DisplayCommentController;
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
import services.forum.PostService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class DisplayPostController {

    @FXML
    private VBox postsContainer;

    private PostService postService = new PostService();

    @FXML
    public void initialize() {
        loadPosts();
    }

    private void loadPosts() {
        postsContainer.getChildren().clear();
        try {
            List<Post> posts = postService.afficher();
            for (Post post : posts) {
                VBox card = createPostCard(post);
                postsContainer.getChildren().add(card);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not load posts.");
        }
    }

    private VBox createPostCard(Post post) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        Label titleLabel = new Label(post.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #2c3e50;");
        
        Label contentLabel = new Label(post.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e;");

        HBox buttonBox = new HBox(10);
        
        Button commentBtn = new Button("Open Comments");
        commentBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        commentBtn.setOnAction(e -> openComments(e, post));

        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold;");
        editBtn.setOnAction(e -> openEdit(e, post));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        deleteBtn.setOnAction(e -> deletePost(post));

        buttonBox.getChildren().addAll(commentBtn, editBtn, deleteBtn);
        
        card.getChildren().addAll(titleLabel, contentLabel, buttonBox);
        return card;
    }

    @FXML
    private void goAddPost(ActionEvent event) {
        switchView(event, "/post/addPost.fxml");
    }

    private void openEdit(ActionEvent event, Post post) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/post/updatePost.fxml"));
            Parent root = loader.load();
            UpdatePostController controller = loader.getController();
            controller.setPost(post);
            
            StackPane parentContainer = (StackPane) ((Node) event.getSource()).getScene().lookup("#contentArea");
            parentContainer.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openComments(ActionEvent event, Post post) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/comment/displayComment.fxml"));
            Parent root = loader.load();
            DisplayCommentController controller = loader.getController();
            controller.setPost(post);
            
            StackPane parentContainer = (StackPane) ((Node) event.getSource()).getScene().lookup("#contentArea");
            parentContainer.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deletePost(Post post) {
        try {
            postService.supprimer(post.getId());
            showAlert(Alert.AlertType.INFORMATION, "Deleted", "Post deleted successfully.");
            loadPosts(); // Refresh list
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not delete post.");
        }
    }

    private void switchView(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
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
