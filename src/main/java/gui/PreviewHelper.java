package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class PreviewHelper {
    public static void goToPreview(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(PreviewHelper.class.getResource("/gui/front-main-view.fxml"));
            Parent root = loader.load();
            
            FrontMainController controller = loader.getController();
            controller.setPreviewMode(true);
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1280, 760));
        } catch (IOException e) {
            e.printStackTrace();
            AlertHelper.showError("Preview Error", "Could not load preview: " + e.getMessage());
        }
    }
}
