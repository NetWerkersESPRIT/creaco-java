package gui.collab.request;

import entities.Collaborator;
import entities.CollabRequest;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import services.CollabRequestService;
import services.CollaboratorService;
import services.AiAssistLogService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

public class UpdateRequestController {

    @FXML private Label requestFormLabel;
    @FXML private TextField reqTitleField;
    @FXML private TextArea reqDescArea;
    @FXML private TextField reqBudgetField;
    @FXML private ComboBox<String> reqCurrencyBox;
    @FXML private DatePicker reqStartDatePicker;
    @FXML private DatePicker reqEndDatePicker;
    @FXML private TextArea reqDeliverablesArea;
    @FXML private TextArea reqPaymentTermsArea;
    @FXML private TextField reqManagerUsernameField;
    @FXML private ComboBox<Collaborator> reqPartnerBox;

    @FXML private Label reqTitleError;
    @FXML private Label reqBudgetError;
    @FXML private Label reqStartDateError;
    @FXML private Label reqEndDateError;
    @FXML private Label reqPartnerError;

    private final CollabRequestService requestService = new CollabRequestService();
    private final CollaboratorService collaboratorService = new CollaboratorService();
    private final AiAssistLogService aiAssistLogService = new AiAssistLogService();

    private CollabRequest editingRequest;
    private Runnable onCancelCallback;
    private Runnable onSaveCallback;

    public void setOnCancel(Runnable callback) { this.onCancelCallback = callback; }
    public void setOnSave(Runnable callback) { this.onSaveCallback = callback; }

    @FXML
    public void initialize() {
        reqCurrencyBox.setItems(FXCollections.observableArrayList("TND", "EUR"));
        setupPartnerBox();
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

    public void setRequest(CollabRequest req) {
        this.editingRequest = req;
        requestFormLabel.setText("Edit Request: " + req.getTitle());
        reqTitleField.setText(req.getTitle());
        reqDescArea.setText(req.getDescription());
        if (req.getBudget() != null) reqBudgetField.setText(req.getBudget().toString());
        if (req.getStartDate() != null) reqStartDatePicker.setValue(((java.sql.Date)req.getStartDate()).toLocalDate());
        if (req.getEndDate() != null) reqEndDatePicker.setValue(((java.sql.Date)req.getEndDate()).toLocalDate());
        reqDeliverablesArea.setText(req.getDeliverables());
        reqPaymentTermsArea.setText(req.getPaymentTerms());
        reqManagerUsernameField.setText(String.valueOf(req.getRevisorId()));

        try {
            reqPartnerBox.setItems(FXCollections.observableArrayList(collaboratorService.afficher()));
            for (Collaborator c : reqPartnerBox.getItems()) {
                if (c.getId() == req.getCollaboratorId()) {
                    reqPartnerBox.getSelectionModel().select(c);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onSaveRequest() {
        resetErrors();
        if (!validate()) return;

        try {
            editingRequest.setTitle(reqTitleField.getText().trim());
            editingRequest.setDescription(reqDescArea.getText().trim());
            editingRequest.setBudget(new BigDecimal(reqBudgetField.getText().trim()));
            editingRequest.setStartDate(java.sql.Date.valueOf(reqStartDatePicker.getValue()));
            editingRequest.setEndDate(java.sql.Date.valueOf(reqEndDatePicker.getValue()));
            editingRequest.setDeliverables(reqDeliverablesArea.getText().trim());
            editingRequest.setPaymentTerms(reqPaymentTermsArea.getText().trim());
            editingRequest.setCollaboratorId(reqPartnerBox.getSelectionModel().getSelectedItem().getId());

            try {
                editingRequest.setRevisorId(Integer.parseInt(reqManagerUsernameField.getText().trim()));
            } catch (Exception ignored) {}

            requestService.modifier(editingRequest.getId(), editingRequest);
            if (onSaveCallback != null) onSaveCallback.run();
        } catch (Exception e) {
            showAlert("Save Error", "Could not update request: " + e.getMessage());
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
        if (reqPartnerBox.getSelectionModel().getSelectedItem() == null) { showError(reqPartnerError, "Partner is required."); isValid = false; }

        return isValid;
    }

    private void resetErrors() {
        reqTitleError.setVisible(false); reqTitleError.setManaged(false);
        reqBudgetError.setVisible(false); reqBudgetError.setManaged(false);
        reqStartDateError.setVisible(false); reqStartDateError.setManaged(false);
        reqEndDateError.setVisible(false); reqEndDateError.setManaged(false);
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

    @FXML
    private void onAssistDescription() {
        String original = reqDescArea.getText();
        if (original == null || original.trim().isEmpty()) return;
        
        reqDescArea.setDisable(true);
        reqDescArea.setPromptText("Generating formal business text...");
        
        new Thread(() -> {
            String rephrased = utils.GroqAssistService.getFormalBusinessText("description", original);
            javafx.application.Platform.runLater(() -> {
                reqDescArea.setText(rephrased);
                reqDescArea.setDisable(false);
            });
            if (!original.equals(rephrased) && !rephrased.startsWith("Failed to parse") && !rephrased.startsWith("Groq API Error")) {
                aiAssistLogService.logUsage("description", original, rephrased);
            }
        }).start();
    }

    @FXML
    private void onAssistDeliverables() {
        String original = reqDeliverablesArea.getText();
        if (original == null || original.trim().isEmpty()) return;
        
        reqDeliverablesArea.setDisable(true);
        reqDeliverablesArea.setPromptText("Generating formal deliverables text...");
        
        new Thread(() -> {
            String rephrased = utils.GroqAssistService.getFormalBusinessText("deliverables", original);
            javafx.application.Platform.runLater(() -> {
                reqDeliverablesArea.setText(rephrased);
                reqDeliverablesArea.setDisable(false);
            });
            if (!original.equals(rephrased) && !rephrased.startsWith("Failed to parse") && !rephrased.startsWith("Groq API Error")) {
                aiAssistLogService.logUsage("deliverables", original, rephrased);
            }
        }).start();
    }

    @FXML
    private void onAssistPaymentTerms() {
        String original = reqPaymentTermsArea.getText();
        if (original == null || original.trim().isEmpty()) return;
        
        reqPaymentTermsArea.setDisable(true);
        reqPaymentTermsArea.setPromptText("Generating formal payment terms...");
        
        new Thread(() -> {
            String rephrased = utils.GroqAssistService.getFormalBusinessText("payment_terms", original);
            javafx.application.Platform.runLater(() -> {
                reqPaymentTermsArea.setText(rephrased);
                reqPaymentTermsArea.setDisable(false);
            });
            if (!original.equals(rephrased) && !rephrased.startsWith("Failed to parse") && !rephrased.startsWith("Groq API Error")) {
                aiAssistLogService.logUsage("payment_terms", original, rephrased);
            }
        }).start();
    }
}
