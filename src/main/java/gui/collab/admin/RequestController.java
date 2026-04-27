package gui.collab.admin;

import entities.CollabRequest;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import services.CollabRequestService;
import java.sql.SQLException;

public class RequestController {

    @FXML private ListView<CollabRequest> requestsListView;

    private final CollabRequestService requestService = new CollabRequestService();
    private ObservableList<CollabRequest> requestsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupListView();
        loadAllRequests();
    }

    private void setupListView() {
        requestsListView.setCellFactory(param -> new ListCell<CollabRequest>() {
            @Override
            protected void updateItem(CollabRequest item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox cell = new VBox(5);
                    Label title = new Label(item.getTitle());
                    title.setStyle("-fx-font-weight: bold;");
                    Label details = new Label("Status: " + item.getStatus() + " | Budget: " + item.getBudget() + " DT");
                    cell.getChildren().addAll(title, details);
                    setGraphic(cell);
                }
            }
        });
        requestsListView.setItems(requestsList);

        // Read-only detail view on double click
        requestsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && requestsListView.getSelectionModel().getSelectedItem() != null) {
                showDetail(requestsListView.getSelectionModel().getSelectedItem());
            }
        });
    }

    private void loadAllRequests() {
        try {
            requestsList.setAll(requestService.afficher());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showDetail(CollabRequest req) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Request Details (Read-Only)");
        alert.setHeaderText(req.getTitle());
        alert.setContentText("Description: " + req.getDescription() + "\n\n" +
                "Status: " + req.getStatus() + "\n" +
                "Budget: " + req.getBudget() + " DT\n" +
                "Rejection Reason: " + (req.getRejectionReason() != null ? req.getRejectionReason() : "N/A"));
        alert.showAndWait();
    }
}
