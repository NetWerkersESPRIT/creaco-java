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

public class EditCollaboratorController {

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
    private Collaborator currentCollaborator;

    public void setListController(ListCollaboratorController listController) {
        this.listController = listController;
    }

    public void setCollaborator(Collaborator collaborator) {
        this.currentCollaborator = collaborator;
        nameField.setText(collaborator.getName());
        companyField.setText(collaborator.getCompanyName());
        emailField.setText(collaborator.getEmail());
        phoneField.setText(collaborator.getPhone());
        addressField.setText(collaborator.getAddress());
        websiteField.setText(collaborator.getWebsite());
        domainField.setText(collaborator.getDomain());
        descriptionArea.setText(collaborator.getDescription());
        logoField.setText(collaborator.getLogo());
        publicCheckBox.setSelected(collaborator.isPublic());
        statusField.setText(collaborator.getStatus());
    }

    @FXML
    private void handleSave() {
        if (nameField.getText().isEmpty() || emailField.getText().isEmpty()) {
            showAlert("Validation Error", "Name and Email are required.", Alert.AlertType.ERROR);
            return;
        }

        currentCollaborator.setName(nameField.getText());
        currentCollaborator.setCompanyName(companyField.getText());
        currentCollaborator.setEmail(emailField.getText());
        currentCollaborator.setPhone(phoneField.getText());
        currentCollaborator.setAddress(addressField.getText());
        currentCollaborator.setWebsite(websiteField.getText());
        currentCollaborator.setDomain(domainField.getText());
        currentCollaborator.setDescription(descriptionArea.getText());
        currentCollaborator.setLogo(logoField.getText());
        currentCollaborator.setPublic(publicCheckBox.isSelected());
        currentCollaborator.setStatus(statusField.getText());

        try {
            collaboratorService.update(currentCollaborator);
            showAlert("Success", "Collaborator updated successfully.", Alert.AlertType.INFORMATION);
            if (listController != null) {
                listController.refreshTable();
            }
            closeWindow();
        } catch (SQLException e) {
            showAlert("Error", "Could not update collaborator: " + e.getMessage(), Alert.AlertType.ERROR);
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
