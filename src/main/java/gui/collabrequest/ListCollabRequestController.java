package gui.collabrequest;

import entities.CollabRequest;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import services.CollabRequestService;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

public class ListCollabRequestController {

    @FXML private TableView<CollabRequest> requestTable;
    @FXML private TableColumn<CollabRequest, Integer> idColumn;
    @FXML private TableColumn<CollabRequest, String> titleColumn;
    @FXML private TableColumn<CollabRequest, BigDecimal> budgetColumn;
    @FXML private TableColumn<CollabRequest, String> statusColumn;
    @FXML private TableColumn<CollabRequest, Integer> collaboratorIdColumn;

    private final CollabRequestService requestService = new CollabRequestService();
    private final ObservableList<CollabRequest> requestList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        budgetColumn.setCellValueFactory(new PropertyValueFactory<>("budget"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        collaboratorIdColumn.setCellValueFactory(new PropertyValueFactory<>("collaboratorId"));

        refreshTable();
    }

    public void refreshTable() {
        try {
            requestList.setAll(requestService.getAll());
            requestTable.setItems(requestList);
        } catch (SQLException e) {
            showAlert("Error", "Could not fetch requests: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleAdd() {
        loadWindow("/collabrequest/AddCollabRequest.fxml", "Add Collaboration Request");
    }

    @FXML
    private void handleEdit() {
        CollabRequest selected = requestTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select a request to edit.", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/collabrequest/EditCollabRequest.fxml"));
            Parent root = loader.load();
            
            EditCollabRequestController controller = loader.getController();
            controller.setCollabRequest(selected);
            controller.setListController(this);

            Stage stage = new Stage();
            stage.setTitle("Edit Collaboration Request");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load edit screen.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDelete() {
        CollabRequest selected = requestTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select a request to delete.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete request " + selected.getTitle() + "?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                requestService.delete(selected.getId());
                showAlert("Success", "Request deleted successfully.", Alert.AlertType.INFORMATION);
                refreshTable();
            } catch (SQLException e) {
                showAlert("Error", "Could not delete request: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void loadWindow(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            
            Object controller = loader.getController();
            if (controller instanceof AddCollabRequestController) {
                ((AddCollabRequestController) controller).setListController(this);
            }

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load screen: " + fxml, Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
