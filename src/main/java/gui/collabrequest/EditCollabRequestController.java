package gui.collabrequest;

import entities.CollabRequest;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.CollabRequestService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.Date;

public class EditCollabRequestController {

    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField budgetField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField statusField;
    @FXML private TextArea deliverablesArea;
    @FXML private TextArea paymentTermsArea;
    @FXML private TextField collaboratorIdField;

    private final CollabRequestService requestService = new CollabRequestService();
    private ListCollabRequestController listController;
    private CollabRequest currentRequest;

    public void setListController(ListCollabRequestController listController) {
        this.listController = listController;
    }

    public void setCollabRequest(CollabRequest request) {
        this.currentRequest = request;
        titleField.setText(request.getTitle());
        descriptionArea.setText(request.getDescription());
        budgetField.setText(request.getBudget().toString());
        if (request.getStartDate() != null) {
            startDatePicker.setValue(request.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }
        if (request.getEndDate() != null) {
            endDatePicker.setValue(request.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }
        statusField.setText(request.getStatus());
        deliverablesArea.setText(request.getDeliverables());
        paymentTermsArea.setText(request.getPaymentTerms());
        collaboratorIdField.setText(String.valueOf(request.getCollaboratorId()));
    }

    @FXML
    private void handleSave() {
        try {
            if (titleField.getText().isEmpty() || budgetField.getText().isEmpty()) {
                showAlert("Validation Error", "Title and Budget are required.", Alert.AlertType.ERROR);
                return;
            }

            currentRequest.setTitle(titleField.getText());
            currentRequest.setDescription(descriptionArea.getText());
            currentRequest.setBudget(new BigDecimal(budgetField.getText()));
            currentRequest.setStartDate(startDatePicker.getValue() != null ? Date.from(startDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()) : null);
            currentRequest.setEndDate(endDatePicker.getValue() != null ? Date.from(endDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()) : null);
            currentRequest.setStatus(statusField.getText());
            currentRequest.setDeliverables(deliverablesArea.getText());
            currentRequest.setPaymentTerms(paymentTermsArea.getText());
            currentRequest.setCollaboratorId(Integer.parseInt(collaboratorIdField.getText()));

            requestService.update(currentRequest);
            showAlert("Success", "Request updated successfully.", Alert.AlertType.INFORMATION);
            if (listController != null) {
                listController.refreshTable();
            }
            closeWindow();
        } catch (SQLException e) {
            showAlert("Error", "Could not update request: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Please enter valid numeric values.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) titleField.getScene().getWindow();
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
