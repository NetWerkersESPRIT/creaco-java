package gui.collab.manager;

import entities.Contract;
import entities.Collaborator;
import entities.Users;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import services.CollaboratorService;
import services.ContractService;
import services.UserService;
import java.text.SimpleDateFormat;

public class ContractConsultationController {

    @FXML private Label refLabel;
    @FXML private Label protocolLabel;
    @FXML private Label dateLabel;
    @FXML private Label partnerNameLabel;
    @FXML private Label partnerRepLabel;
    @FXML private Label creatorNameLabel;
    @FXML private Label statusLabel;
    @FXML private Button btnUpdate;
    @FXML private Button btnTransmit;

    private final ContractService contractService = new ContractService();
    private final CollaboratorService partnerService = new CollaboratorService();
    private final UserService userService = new UserService();
    
    private Contract currentContract;
    private Runnable onBackRequested;
    private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

    public void setCallbacks(Runnable onBack) {
        this.onBackRequested = onBack;
    }

    public void setContract(Contract contract) {
        this.currentContract = contract;
        
        refLabel.setText("REF: " + contract.getContractNumber());
        protocolLabel.setText("DIGITAL PROTOCOL #" + contract.getContractNumber());
        dateLabel.setText(contract.getCreatedAt() != null ? df.format(contract.getCreatedAt()) : df.format(new java.util.Date()));
        statusLabel.setText(contract.getStatus().toUpperCase());

        try {
            Collaborator partner = partnerService.getById(contract.getCollaboratorId());
            if (partner != null) {
                partnerNameLabel.setText(partner.getCompanyName());
                partnerRepLabel.setText("Representative: " + partner.getName());
            }

            Users creator = userService.getUserById(contract.getCreatorId());
            if (creator != null) {
                creatorNameLabel.setText(creator.getUsername());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Hide transmit button if already sent
        if (!"DRAFT".equalsIgnoreCase(contract.getStatus())) {
            btnTransmit.setVisible(false);
            btnUpdate.setDisable(true);
        }
    }

    @FXML
    private void onUpdateSpecifications() {
        // Mock logic for updating specs
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Feature Pending");
        alert.setHeaderText("Specification Editor");
        alert.setContentText("The interactive terms editor is currently under development.");
        alert.showAndWait();
    }

    @FXML
    private void onTransmitToPartner() {
        try {
            currentContract.setStatus("SENT_TO_PARTNER");
            currentContract.setSentAt(new java.util.Date());
            contractService.modifier(currentContract.getId(), currentContract);
            
            setContract(currentContract); // Refresh UI
            
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Contract Transmitted");
            alert.setContentText("The digital protocol has been securely transmitted to the partner for signature.");
            alert.showAndWait();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onBack() {
        if (onBackRequested != null) onBackRequested.run();
    }
}
