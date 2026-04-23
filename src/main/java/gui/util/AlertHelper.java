package gui.util;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class AlertHelper {

    public enum AlertType {
        CONFIRMATION, INFORMATION, ERROR, WARNING
    }

    public static boolean showCustomAlert(String title, String message, AlertType type) {
        AtomicBoolean confirmed = new AtomicBoolean(false);
        
        try {
            FXMLLoader loader = new FXMLLoader(AlertHelper.class.getResource("/gui/custom_alert.fxml"));
            StackPane root = loader.load();
            
            Label iconLabel = (Label) root.lookup("#iconLabel");
            Label titleLabel = (Label) root.lookup("#titleLabel");
            Label messageLabel = (Label) root.lookup("#messageLabel");
            Button cancelBtn = (Button) root.lookup("#cancelBtn");
            Button confirmBtn = (Button) root.lookup("#confirmBtn");
            
            titleLabel.setText(title);
            messageLabel.setText(message);
            
            // Customize based on type
            switch (type) {
                case CONFIRMATION:
                    iconLabel.setText("?");
                    confirmBtn.setText("Confirm");
                    break;
                case INFORMATION:
                    iconLabel.setText("i");
                    cancelBtn.setVisible(false);
                    cancelBtn.setManaged(false);
                    confirmBtn.setText("OK");
                    break;
                case ERROR:
                    iconLabel.setText("✕");
                    iconLabel.setStyle("-fx-text-fill: #e53e3e; -fx-font-size: 40px;");
                    cancelBtn.setVisible(false);
                    cancelBtn.setManaged(false);
                    confirmBtn.setText("Close");
                    confirmBtn.setStyle("-fx-background-color: #e53e3e; -fx-text-fill: white; -fx-background-radius: 12; -fx-font-weight: bold;");
                    break;
                case WARNING:
                    iconLabel.setText("!");
                    iconLabel.setStyle("-fx-text-fill: #d97706; -fx-font-size: 40px;");
                    confirmBtn.setText("Continue");
                    break;
            }
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);
            
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            
            // Animation logic
            VBox dialogBox = (VBox) root.getChildren().get(0);
            
            // Initial state for animation
            dialogBox.setScaleX(0.7);
            dialogBox.setScaleY(0.7);
            root.setOpacity(0);
            
            // Scale Transition
            ScaleTransition scale = new ScaleTransition(Duration.millis(300), dialogBox);
            scale.setToX(1.0);
            scale.setToY(1.0);
            
            // Fade Transition
            FadeTransition fade = new FadeTransition(Duration.millis(200), root);
            fade.setToValue(1.0);
            
            confirmBtn.setOnAction(e -> {
                confirmed.set(true);
                animateOut(stage, root, dialogBox);
            });
            
            cancelBtn.setOnAction(e -> {
                confirmed.set(false);
                animateOut(stage, root, dialogBox);
            });
            
            stage.setOnShown(e -> {
                scale.play();
                fade.play();
            });
            
            stage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return confirmed.get();
    }

    private static void animateOut(Stage stage, StackPane root, VBox dialogBox) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(200), dialogBox);
        scale.setToX(0.8);
        scale.setToY(0.8);
        
        FadeTransition fade = new FadeTransition(Duration.millis(150), root);
        fade.setToValue(0);
        
        fade.setOnFinished(e -> stage.close());
        
        scale.play();
        fade.play();
    }
}
