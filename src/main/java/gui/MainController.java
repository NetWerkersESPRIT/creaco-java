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

    private static final String NAV_ACTIVE   = "-fx-background-color: #f59e0b; -fx-text-fill: #1f365c; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 14;";
    private static final String NAV_INACTIVE = "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 14;";

    @FXML
    private void initialize() {
        // Hide courses section button
        coursesNavButton.setVisible(false);
        coursesNavButton.setManaged(false);

        setActiveNav(forumNavButton);
        showForum();
    }

    private void setActiveNav(Button active) {
        forumNavButton.setStyle(NAV_INACTIVE);
        active.setStyle(NAV_ACTIVE);
    }

    @FXML
    private void showForum() {
        try {
            setActiveNav(forumNavButton);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/post/displayPost.fxml"));
            Parent root = loader.load();
            gui.post.DisplayPostController controller = loader.getController();
            controller.setAdminMode(false);
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
