package gui.collab.partner;

import entities.Collaborator;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import services.CollaboratorService;
import java.io.File;

public class UpdatePartnerController {

    @FXML private Label partnerFormLabel;
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
    private Collaborator editingPartner;
    private Runnable onCancelCallback;
    private Runnable onSaveCallback;

    public void setOnCancel(Runnable callback) { this.onCancelCallback = callback; }
    public void setOnSave(Runnable callback) { this.onSaveCallback = callback; }

    public void setPartner(Collaborator partner) {
        this.editingPartner = partner;
        pCompanyField.setText(partner.getCompanyName());
        pNameField.setText(partner.getName());
        pEmailField.setText(partner.getEmail());
        pPhoneField.setText(partner.getPhone());
        pDomainField.setText(partner.getDomain());
        pAddressArea.setText(partner.getAddress());
        pWebsiteField.setText(partner.getWebsite());
        pLogoField.setText(partner.getLogo());
        pDescArea.setText(partner.getDescription());
    }

    @FXML
    private void onSavePartner() {
        resetErrors();
        if (!validate()) return;

        try {
            editingPartner.setName(pNameField.getText().trim());
            editingPartner.setCompanyName(pCompanyField.getText().trim());
            editingPartner.setEmail(pEmailField.getText().trim());
            editingPartner.setPhone(pPhoneField.getText().trim());
            editingPartner.setAddress(pAddressArea.getText().trim());
            editingPartner.setWebsite(pWebsiteField.getText().trim());
            editingPartner.setDomain(pDomainField.getText().trim());
            editingPartner.setDescription(pDescArea.getText().trim());
            editingPartner.setLogo(pLogoField.getText().trim());

            collaboratorService.modifier(editingPartner.getId(), editingPartner);
            if (onSaveCallback != null) onSaveCallback.run();
        } catch (Exception e) {
            showAlert("Save Error", "Could not update partner: " + e.getMessage());
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
