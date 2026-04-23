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

public class AddContractController {

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

    public void setListController(ListContractController listController) {
        this.listController = listController;
    }

    @FXML
    private void handleSave() {
        try {
            if (contractNumberField.getText().isEmpty() || titleField.getText().isEmpty()) {
                showAlert("Validation Error", "Number and Title are required.", Alert.AlertType.ERROR);
                return;
            }

            Integer creatorId = creatorIdField.getText().isEmpty() ? null : Integer.parseInt(creatorIdField.getText());

            Contract contract = new Contract(
                    contractNumberField.getText(),
                    titleField.getText(),
                    startDatePicker.getValue() != null ? Date.from(startDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()) : null,
                    endDatePicker.getValue() != null ? Date.from(endDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()) : null,
                    new BigDecimal(amountField.getText()),
                    statusField.getText(),
                    termsArea.getText(),
                    paymentScheduleArea.getText(),
                    confidentialityClauseArea.getText(),
                    cancellationTermsArea.getText(),
                    signatureTokenField.getText(),
                    Integer.parseInt(collabRequestIdField.getText()),
                    creatorId,
                    Integer.parseInt(collaboratorIdField.getText())
            );

            contractService.insert(contract);
            showAlert("Success", "Contract added successfully.", Alert.AlertType.INFORMATION);
            if (listController != null) {
                listController.refreshTable();
            }
            closeWindow();
        } catch (SQLException e) {
            showAlert("Error", "Could not add contract: " + e.getMessage(), Alert.AlertType.ERROR);
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
