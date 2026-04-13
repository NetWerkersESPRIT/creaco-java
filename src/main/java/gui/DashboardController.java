package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class DashboardController {

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        showCollaborators();
    }

    @FXML
    private void showCollaborators() {
        loadView("/collaborator/ListCollaborator.fxml");
    }

    @FXML
    private void showRequests() {
        loadView("/collabrequest/ListCollabRequest.fxml");
    }

    @FXML
    private void showContracts() {
        loadView("/contract/ListContract.fxml");
    }

    private void loadView(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
