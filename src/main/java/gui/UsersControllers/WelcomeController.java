package gui.UsersControllers;

import javafx.animation.Animation;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class WelcomeController {

    @FXML private StackPane mainRoot;
    @FXML private StackPane heroVisual;
    @FXML private HBox badge1;
    @FXML private HBox badge2;

    @FXML
    public void initialize() {
        javafx.application.Platform.runLater(() -> {
            if (mainRoot != null && mainRoot.getScene() != null) {
                Stage stage = (Stage) mainRoot.getScene().getWindow();
                if (stage != null) stage.setMaximized(true);
            }
        });
        // Floating animations for visual elements
        addFloatingAnimation(heroVisual, 0);
        addFloatingAnimation(badge1, 500);
        addFloatingAnimation(badge2, 1000);
    }

    private void addFloatingAnimation(Node node, int delayMs) {
        TranslateTransition tt = new TranslateTransition(Duration.seconds(3), node);
        tt.setByY(-15);
        tt.setCycleCount(Animation.INDEFINITE);
        tt.setAutoReverse(true);
        tt.setDelay(Duration.millis(delayMs));
        tt.play();
    }

    @FXML
    private void handleSignIn() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Users/SignIn.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) mainRoot.getScene().getWindow();
            stage.getScene().setRoot(root); // keep same scene → stage stays maximized
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleExploreForum() {
        try {
            utils.SessionManager.getInstance().setVisitor(true);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/post/displayPost.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) mainRoot.getScene().getWindow();
            stage.getScene().setRoot(root); // keep same scene → stage stays maximized
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
