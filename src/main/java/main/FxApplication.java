package main;

import gui.post.BackofficeController;
import gui.post.DisplayPostController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class FxApplication extends Application {

    private static BackofficeController backofficeControllerInstance;
    private static final java.util.List<DisplayPostController> forumControllers = new java.util.ArrayList<>();

    public static BackofficeController getBackofficeController() {
        return backofficeControllerInstance;
    }

    public static void setBackofficeController(BackofficeController instance) {
        backofficeControllerInstance = instance;
    }

    public static void registerForumController(DisplayPostController instance) {
        forumControllers.add(instance);
    }

    public static void unregisterForumController(DisplayPostController instance) {
        forumControllers.remove(instance);
    }

    public static void refreshAllForumWindows() {
        for (DisplayPostController ctrl : forumControllers) {
            ctrl.loadPosts();
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        // --- 1. Launch Forum (FrontOffice) ---
        FXMLLoader forumLoader = new FXMLLoader(FxApplication.class.getResource("/gui/main-view.fxml"));
        Scene forumScene = new Scene(forumLoader.load());
        stage.setTitle("Creaco - Forum (FrontOffice)");
        stage.setScene(forumScene);
        stage.show();

        // --- 2. Launch Moderation Panel (Backoffice) ---
        Stage adminStage = new Stage();
        FXMLLoader adminLoader = new FXMLLoader(FxApplication.class.getResource("/post/backoffice.fxml"));
        Scene adminScene = new Scene(adminLoader.load());
        
        // Capture the controller instance for sync
        backofficeControllerInstance = adminLoader.getController();
        
        adminStage.setTitle("Creaco - Admin Moderation Panel");
        adminStage.setScene(adminScene);
        adminStage.setX(stage.getX() + 920); // Position it to the right
        adminStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}