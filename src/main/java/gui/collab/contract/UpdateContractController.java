package gui.collab.contract;

import entities.Contract;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import services.ContractService;

public class UpdateContractController {

    @FXML private TextArea contractTermReasonArea;
    @FXML private Label contractTermReasonError;

    private final ContractService contractService = new ContractService();
    private Contract contractToTerminate;

    private Runnable onCancelCallback;
    private Runnable onSaveCallback;

    public void setOnCancel(Runnable callback) { this.onCancelCallback = callback; }
    public void setOnSave(Runnable callback) { this.onSaveCallback = callback; }

    public void setContract(Contract c) {
        this.contractToTerminate = c;
        contractTermReasonArea.clear();
    }

    @FXML
    private void onCancelTerminateContract() {
        if (onCancelCallback != null) onCancelCallback.run();
    }

    @FXML
    private void onConfirmTerminateContract() {
        if (contractToTerminate == null) return;
        resetError();

        String reason = contractTermReasonArea.getText().trim();
        if (reason.isEmpty()) {
            showError("Please provide a termination reason.");
            return;
        }
        contractToTerminate.setCancellationTerms(reason);
        contractToTerminate.setStatus("TERMINATED");
        try {
            contractService.modifier(contractToTerminate.getId(), contractToTerminate);
            if (onSaveCallback != null) onSaveCallback.run();
        } catch (Exception e) {
            showAlert("Error", "Could not terminate contract: " + e.getMessage());
        }
    }

    private void resetError() {
        contractTermReasonError.setVisible(false);
        contractTermReasonError.setManaged(false);
    }

    private void showError(String msg) {
        contractTermReasonError.setText("⚠ " + msg);
        contractTermReasonError.setVisible(true);
        contractTermReasonError.setManaged(true);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
