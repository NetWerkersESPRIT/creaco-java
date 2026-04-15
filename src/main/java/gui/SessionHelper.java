package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SessionHelper {
    public static void logout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(SessionHelper.class.getResource("/gui/choose-role.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            AlertHelper.showError("Logout Error", "Could not load choose-role: " + e.getMessage());
        }
    }
}
