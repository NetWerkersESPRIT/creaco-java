package gui.Events;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import java.io.IOException;

public class MainController {

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        loadView("/Event.fxml");
    }

    @FXML
    void showEvents() {
        loadView("/Event.fxml");
    }

    @FXML
    void showReservations() {
        loadView("/Reservation.fxml");
    }

    private void loadView(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
