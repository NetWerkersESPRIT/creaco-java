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

public class AddCollabRequestController {

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

    public void setListController(ListCollabRequestController listController) {
        this.listController = listController;
    }

    @FXML
    private void handleSave() {
        try {
            if (titleField.getText().isEmpty() || budgetField.getText().isEmpty()) {
                showAlert("Validation Error", "Title and Budget are required.", Alert.AlertType.ERROR);
                return;
            }

            CollabRequest request = new CollabRequest(
                    titleField.getText(),
                    descriptionArea.getText(),
                    new BigDecimal(budgetField.getText()),
                    startDatePicker.getValue() != null ? Date.from(startDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()) : null,
                    endDatePicker.getValue() != null ? Date.from(endDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()) : null,
                    statusField.getText(),
                    deliverablesArea.getText(),
                    paymentTermsArea.getText(),
                    Integer.parseInt(collaboratorIdField.getText())
            );

            requestService.insert(request);
            showAlert("Success", "Request added successfully.", Alert.AlertType.INFORMATION);
            if (listController != null) {
                listController.refreshTable();
            }
            closeWindow();
        } catch (SQLException e) {
            showAlert("Error", "Could not add request: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Please enter valid numeric values for Budget and Collaborator ID.", Alert.AlertType.ERROR);
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
