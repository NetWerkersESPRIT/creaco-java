package gui.UsersControllers;

import javafx.animation.Animation;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
            
            // Get the current stage from the mainRoot
            Stage stage = (Stage) mainRoot.getScene().getWindow();
            Scene scene = new Scene(root);
            
            // Re-apply common stylesheet
            scene.getStylesheets().add(getClass().getResource("/gui/styles.css").toExternalForm());
            
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
