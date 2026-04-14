package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class MainController {

    @FXML
    private Button forumNavButton;
    @FXML
    private Button coursesNavButton;
    @FXML
    private StackPane contentArea;

    @FXML
    private void initialize() {
        // Hide courses section button
        coursesNavButton.setVisible(false);
        coursesNavButton.setManaged(false);

        // Apply active style to forum button
        forumNavButton.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: #1f365c; "
                + "-fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 14;");

        showForum();
    }

    @FXML
    private void showForum() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/post/displayPost.fxml"));
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showCourses() {
        // Courses are disabled - just show forum instead
        showForum();
    }
}