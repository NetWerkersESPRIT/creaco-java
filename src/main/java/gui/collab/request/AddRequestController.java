package gui.collab.request;

import entities.Collaborator;
import entities.CollabRequest;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import services.CollabRequestService;
import services.CollaboratorService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

public class AddRequestController {

    @FXML private TextField reqTitleField;
    @FXML private TextArea reqDescArea;
    @FXML private TextField reqBudgetField;
    @FXML private ComboBox<String> reqCurrencyBox;
    @FXML private DatePicker reqStartDatePicker;
    @FXML private DatePicker reqEndDatePicker;
    @FXML private TextArea reqDeliverablesArea;
    @FXML private TextArea reqPaymentTermsArea;
    @FXML private ComboBox<entities.Users> reqManagerBox;
    @FXML private ComboBox<Collaborator> reqPartnerBox;

    @FXML private Label reqTitleError;
    @FXML private Label reqBudgetError;
    @FXML private Label reqStartDateError;
    @FXML private Label reqEndDateError;
    @FXML private Label reqManagerError;
    @FXML private Label reqPartnerError;

    private final CollabRequestService requestService = new CollabRequestService();
    private final CollaboratorService collaboratorService = new CollaboratorService();

    private Runnable onCancelCallback;
    private Runnable onSaveCallback;

    public void setOnCancel(Runnable callback) { this.onCancelCallback = callback; }
    public void setOnSave(Runnable callback) { this.onSaveCallback = callback; }

    @FXML
    public void initialize() {
        reqCurrencyBox.setItems(FXCollections.observableArrayList("TND", "EUR"));
        reqCurrencyBox.getSelectionModel().selectFirst();

        setupPartnerBox();
        setupManagerBox();
        loadPartners();
        loadManagers();
    }

    private void setupManagerBox() {
        reqManagerBox.setCellFactory(p -> new ListCell<entities.Users>() {
            @Override
            protected void updateItem(entities.Users item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getUsername());
            }
        });
        reqManagerBox.setButtonCell(new ListCell<entities.Users>() {
            @Override
            protected void updateItem(entities.Users item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getUsername());
            }
        });
    }

    private void loadManagers() {
        try {
            services.UsersService usersService = new services.UsersService();
            reqManagerBox.setItems(FXCollections.observableArrayList(usersService.getManagers()));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setupPartnerBox() {
        reqPartnerBox.setCellFactory(p -> new ListCell<Collaborator>() {
            @Override
            protected void updateItem(Collaborator item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getCompanyName());
            }
        });
        reqPartnerBox.setButtonCell(new ListCell<Collaborator>() {
            @Override
            protected void updateItem(Collaborator item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getCompanyName());
            }
        });
    }

    private void loadPartners() {
        try {
            reqPartnerBox.setItems(FXCollections.observableArrayList(collaboratorService.afficher()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onSaveRequest() {
        resetErrors();
        if (!validate()) return;

        try {
            BigDecimal budget = new BigDecimal(reqBudgetField.getText().trim());
            Date startDate = java.sql.Date.valueOf(reqStartDatePicker.getValue());
            Date endDate = java.sql.Date.valueOf(reqEndDatePicker.getValue());
            int partnerId = reqPartnerBox.getSelectionModel().getSelectedItem().getId();
            int revisorId = reqManagerBox.getSelectionModel().getSelectedItem().getId();

            entities.Users currentUser = utils.SessionManager.getInstance().getCurrentUser();
            int creatorId = (currentUser != null) ? currentUser.getId() : 1;

            CollabRequest req = new CollabRequest(
                    reqTitleField.getText().trim(),
                    reqDescArea.getText().trim(),
                    budget,
                    startDate,
                    endDate,
                    "PENDING",
                    reqDeliverablesArea.getText().trim(),
                    reqPaymentTermsArea.getText().trim(),
                    partnerId
            );
            req.setCreatorId(creatorId);
            req.setRevisorId(revisorId);
            requestService.ajouter(req);

            if (onSaveCallback != null) onSaveCallback.run();
        } catch (Exception e) {
            showAlert("Save Error", "Could not save request: " + e.getMessage());
        }
    }

    @FXML
    private void onCancelRequest() {
        if (onCancelCallback != null) onCancelCallback.run();
    }

    private boolean validate() {
        boolean isValid = true;
        if (reqTitleField.getText().trim().isEmpty()) { showError(reqTitleError, "Title is required."); isValid = false; }

        try {
            BigDecimal budget = new BigDecimal(reqBudgetField.getText().trim());
            if (budget.compareTo(BigDecimal.ZERO) <= 0) { showError(reqBudgetError, "Budget must be positive."); isValid = false; }
        } catch(Exception e) {
            showError(reqBudgetError, "Invalid number."); isValid = false;
        }

        LocalDate sd = reqStartDatePicker.getValue();
        LocalDate ed = reqEndDatePicker.getValue();
        if (sd == null) { showError(reqStartDateError, "Start Date is required."); isValid = false; }
        if (ed == null) { showError(reqEndDateError, "End Date is required."); isValid = false; }
        if (sd != null && ed != null && !ed.isAfter(sd)) { showError(reqEndDateError, "End Date must be after Start Date."); isValid = false; }
        if (reqManagerBox.getSelectionModel().getSelectedItem() == null) { showError(reqManagerError, "Manager is required."); isValid = false; }
        if (reqPartnerBox.getSelectionModel().getSelectedItem() == null) { showError(reqPartnerError, "Partner is required."); isValid = false; }

        return isValid;
    }

    private void resetErrors() {
        reqTitleError.setVisible(false); reqTitleError.setManaged(false);
        reqBudgetError.setVisible(false); reqBudgetError.setManaged(false);
        reqStartDateError.setVisible(false); reqStartDateError.setManaged(false);
        reqEndDateError.setVisible(false); reqEndDateError.setManaged(false);
        reqManagerError.setVisible(false); reqManagerError.setManaged(false);
        reqPartnerError.setVisible(false); reqPartnerError.setManaged(false);
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
