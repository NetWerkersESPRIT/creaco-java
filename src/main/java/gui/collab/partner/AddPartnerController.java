package gui.collab.partner;

import entities.Collaborator;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import services.CollaboratorService;
import java.io.File;
import java.time.LocalDateTime;

public class AddPartnerController {

    @FXML private TextField pCompanyField;
    @FXML private TextField pNameField;
    @FXML private TextField pEmailField;
    @FXML private TextField pPhoneField;
    @FXML private TextField pDomainField;
    @FXML private TextArea pAddressArea;
    @FXML private TextField pWebsiteField;
    @FXML private TextField pLogoField;
    @FXML private TextArea pDescArea;

    @FXML private Label pCompanyError;
    @FXML private Label pNameError;
    @FXML private Label pEmailError;
    @FXML private Label pWebsiteError;

    private final CollaboratorService collaboratorService = new CollaboratorService();
    private Runnable onCancelCallback;
    private Runnable onSaveCallback;

    public void setOnCancel(Runnable callback) { this.onCancelCallback = callback; }
    public void setOnSave(Runnable callback) { this.onSaveCallback = callback; }

    @FXML
    private void onSavePartner() {
        resetErrors();
        boolean isValid = validate();
        if (!isValid) return;

        try {
            int currentUserId = 1;
            if (utils.SessionManager.getInstance().getCurrentUser() != null) {
                currentUserId = utils.SessionManager.getInstance().getCurrentUser().getId();
            }

            Collaborator collab = new Collaborator(
                    pNameField.getText().trim(),
                    pCompanyField.getText().trim(),
                    pEmailField.getText().trim(),
                    pPhoneField.getText().trim(),
                    pAddressArea.getText().trim(),
                    pWebsiteField.getText().trim(),
                    pDomainField.getText().trim(),
                    pDescArea.getText().trim(),
                    pLogoField.getText().trim(),
                    true, "ACTIVE", currentUserId
            );
            collab.setCreatedAt(java.sql.Timestamp.valueOf(LocalDateTime.now()));
            collaboratorService.ajouter(collab);
            if (onSaveCallback != null) onSaveCallback.run();
        } catch (Exception e) {
            showAlert("Save Error", "Could not save partner: " + e.getMessage());
        }
    }

    @FXML
    private void onCancelPartner() {
        if (onCancelCallback != null) onCancelCallback.run();
    }

    @FXML
    private void onBrowseLogo() {
        FileChooser fp = new FileChooser();
        fp.setTitle("Select Logo Image");
        fp.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File selected = fp.showOpenDialog(pCompanyField.getScene().getWindow());
        if (selected != null) {
            pLogoField.setText(selected.getName());
        }
    }

    private boolean validate() {
        boolean isValid = true;
        if (pCompanyField.getText().trim().isEmpty()) { showError(pCompanyError, "Company name is required."); isValid = false; }
        if (pNameField.getText().trim().isEmpty()) { showError(pNameError, "Contact name is required."); isValid = false; }

        String email = pEmailField.getText().trim();
        if (email.isEmpty()) { showError(pEmailError, "Email is required."); isValid = false; }
        else if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
            showError(pEmailError, "Invalid email format."); isValid = false;
        }

        String website = pWebsiteField.getText().trim();
        if (!website.isEmpty() && !website.matches("^(https?://)?([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}(/.*)?$")) {
            showError(pWebsiteError, "Invalid URL."); isValid = false;
        }
        return isValid;
    }

    private void resetErrors() {
        pCompanyError.setVisible(false); pCompanyError.setManaged(false);
        pNameError.setVisible(false); pNameError.setManaged(false);
        pEmailError.setVisible(false); pEmailError.setManaged(false);
        pWebsiteError.setVisible(false); pWebsiteError.setManaged(false);
    }

    private void showError(Label label, String msg) {
        label.setText("⚠ " + msg);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
