package gui.contract;

import entities.Contract;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.ContractService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.Date;

public class EditContractController {

    @FXML private TextField contractNumberField;
    @FXML private TextField titleField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField amountField;
    @FXML private TextField statusField;
    @FXML private TextArea termsArea;
    @FXML private TextArea paymentScheduleArea;
    @FXML private TextArea confidentialityClauseArea;
    @FXML private TextArea cancellationTermsArea;
    @FXML private TextField signatureTokenField;
    @FXML private TextField collabRequestIdField;
    @FXML private TextField creatorIdField;
    @FXML private TextField collaboratorIdField;

    private final ContractService contractService = new ContractService();
    private ListContractController listController;
    private Contract currentContract;

    public void setListController(ListContractController listController) {
        this.listController = listController;
    }

    public void setContract(Contract contract) {
        this.currentContract = contract;
        contractNumberField.setText(contract.getContractNumber());
        titleField.setText(contract.getTitle());
        if (contract.getStartDate() != null) {
            startDatePicker.setValue(new java.sql.Date(contract.getStartDate().getTime()).toLocalDate());
        }
        if (contract.getEndDate() != null) {
            endDatePicker.setValue(new java.sql.Date(contract.getEndDate().getTime()).toLocalDate());
        }
        amountField.setText(contract.getAmount().toString());
        statusField.setText(contract.getStatus());
        termsArea.setText(contract.getTerms());
        paymentScheduleArea.setText(contract.getPaymentSchedule());
        confidentialityClauseArea.setText(contract.getConfidentialityClause());
        cancellationTermsArea.setText(contract.getCancellationTerms());
        signatureTokenField.setText(contract.getSignatureToken());
        collabRequestIdField.setText(String.valueOf(contract.getCollabRequestId()));
        creatorIdField.setText(contract.getCreatorId() != null ? String.valueOf(contract.getCreatorId()) : "");
        collaboratorIdField.setText(String.valueOf(contract.getCollaboratorId()));
    }

    @FXML
    private void handleSave() {
        try {
            if (contractNumberField.getText().isEmpty() || titleField.getText().isEmpty()) {
                showAlert("Validation Error", "Number and Title are required.", Alert.AlertType.ERROR);
                return;
            }

            currentContract.setContractNumber(contractNumberField.getText());
            currentContract.setTitle(titleField.getText());
            currentContract.setStartDate(startDatePicker.getValue() != null ? java.sql.Date.valueOf(startDatePicker.getValue()) : null);
            currentContract.setEndDate(endDatePicker.getValue() != null ? java.sql.Date.valueOf(endDatePicker.getValue()) : null);
            currentContract.setAmount(new BigDecimal(amountField.getText()));
            currentContract.setStatus(statusField.getText());
            currentContract.setTerms(termsArea.getText());
            currentContract.setPaymentSchedule(paymentScheduleArea.getText());
            currentContract.setConfidentialityClause(confidentialityClauseArea.getText());
            currentContract.setCancellationTerms(cancellationTermsArea.getText());
            currentContract.setSignatureToken(signatureTokenField.getText());
            currentContract.setCollabRequestId(Integer.parseInt(collabRequestIdField.getText()));
            currentContract.setCreatorId(creatorIdField.getText().isEmpty() ? null : Integer.parseInt(creatorIdField.getText()));
            currentContract.setCollaboratorId(Integer.parseInt(collaboratorIdField.getText()));

            contractService.update(currentContract);
            showAlert("Success", "Contract updated successfully.", Alert.AlertType.INFORMATION);
            if (listController != null) {
                listController.refreshTable();
            }
            closeWindow();
        } catch (SQLException e) {
            showAlert("Error", "Could not update contract: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Please enter valid numeric values.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) contractNumberField.getScene().getWindow();
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
