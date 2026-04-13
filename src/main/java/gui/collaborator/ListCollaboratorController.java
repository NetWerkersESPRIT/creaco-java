package gui.collaborator;

import entities.Collaborator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import services.CollaboratorService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class ListCollaboratorController {

    @FXML
    private TableView<Collaborator> collaboratorTable;
    @FXML
    private TableColumn<Collaborator, Integer> idColumn;
    @FXML
    private TableColumn<Collaborator, String> nameColumn;
    @FXML
    private TableColumn<Collaborator, String> companyColumn;
    @FXML
    private TableColumn<Collaborator, String> emailColumn;
    @FXML
    private TableColumn<Collaborator, String> statusColumn;

    private final CollaboratorService collaboratorService = new CollaboratorService();
    private final ObservableList<Collaborator> collaboratorList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        companyColumn.setCellValueFactory(new PropertyValueFactory<>("companyName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        refreshTable();
    }

    public void refreshTable() {
        try {
            collaboratorList.setAll(collaboratorService.getAll());
            collaboratorTable.setItems(collaboratorList);
        } catch (SQLException e) {
            showAlert("Error", "Could not fetch collaborators: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleAdd() {
        loadWindow("/collaborator/AddCollaborator.fxml", "Add Collaborator");
    }

    @FXML
    private void handleEdit() {
        Collaborator selected = collaboratorTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select a collaborator to edit.", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/collaborator/EditCollaborator.fxml"));
            Parent root = loader.load();
            
            EditCollaboratorController controller = loader.getController();
            controller.setCollaborator(selected);
            controller.setListController(this);

            Stage stage = new Stage();
            stage.setTitle("Edit Collaborator");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load edit screen.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDelete() {
        Collaborator selected = collaboratorTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select a collaborator to delete.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete " + selected.getName() + "?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                collaboratorService.delete(selected.getId());
                showAlert("Success", "Collaborator deleted successfully.", Alert.AlertType.INFORMATION);
                refreshTable();
            } catch (SQLException e) {
                showAlert("Error", "Could not delete collaborator: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void loadWindow(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            
            // Pass this controller to AddCollaborator if needed
            Object controller = loader.getController();
            if (controller instanceof AddCollaboratorController) {
                ((AddCollaboratorController) controller).setListController(this);
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
