package gui.collaborator;

import entities.Collaborator;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import services.CollaboratorService;

import java.sql.SQLException;

public class AddCollaboratorController {

    @FXML private TextField nameField;
    @FXML private TextField companyField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private TextField websiteField;
    @FXML private TextField domainField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField logoField;
    @FXML private CheckBox publicCheckBox;
    @FXML private TextField statusField;

    private final CollaboratorService collaboratorService = new CollaboratorService();
    private ListCollaboratorController listController;

    public void setListController(ListCollaboratorController listController) {
        this.listController = listController;
    }

    @FXML
    private void handleSave() {
        if (nameField.getText().isEmpty() || emailField.getText().isEmpty()) {
            showAlert("Validation Error", "Name and Email are required.", Alert.AlertType.ERROR);
            return;
        }

        Collaborator collaborator = new Collaborator(
                nameField.getText(),
                companyField.getText(),
                emailField.getText(),
                phoneField.getText(),
                addressField.getText(),
                websiteField.getText(),
                domainField.getText(),
                descriptionArea.getText(),
                logoField.getText(),
                publicCheckBox.isSelected(),
                statusField.getText(),
                1 // addedByUserId placeholder
        );

        try {
            collaboratorService.insert(collaborator);
            showAlert("Success", "Collaborator added successfully.", Alert.AlertType.INFORMATION);
            if (listController != null) {
                listController.refreshTable();
            }
            closeWindow();
        } catch (SQLException e) {
            showAlert("Error", "Could not add collaborator: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
