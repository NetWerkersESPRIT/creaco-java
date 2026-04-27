package gui.collab.contract;

import entities.Contract;
import entities.Collaborator;
import services.CollaboratorService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import java.sql.SQLException;

public class ViewContractController {

    @FXML private Label refLabel;
    @FXML private Label protocolLabel;
    @FXML private Label issuanceDateLabel;
    @FXML private Label providerNameLabel;
    @FXML private Label providerRepLabel;
    @FXML private Label clientNameLabel;
    @FXML private Text termsText;
    @FXML private TextField urlField;

    private final CollaboratorService collaboratorService = new CollaboratorService();
    private Runnable onBackRequested;
    private Contract currentContract;

    public void setOnBack(Runnable callback) { this.onBackRequested = callback; }

    public void setContract(Contract contract) {
        this.currentContract = contract;
        refLabel.setText("REF: " + contract.getContractNumber());
        protocolLabel.setText("DIGITAL PROTOCOL #" + contract.getContractNumber());
        issuanceDateLabel.setText(contract.getCreatedAt() != null ? contract.getCreatedAt().toString().split(" ")[0] : "22/04/2026");
        
        termsText.setText(contract.getTerms());
        urlField.setText("http://127.0.0.1:8000/public/contract/" + contract.getSignatureToken());

        try {
            Collaborator partner = collaboratorService.getById(contract.getCollaboratorId());
            if (partner != null) {
                providerNameLabel.setText(partner.getCompanyName());
                providerRepLabel.setText("Representative: " + partner.getName());
            }
        } catch (Exception e) {
            System.err.println("Error loading collaborator for contract: " + e.getMessage());
        }
        
        // Mock client name - in real app would come from UserSession
        clientNameLabel.setText("creatorA");
    }

    @FXML
    private void onBack() {
        if (onBackRequested != null) onBackRequested.run();
    }

    @FXML
    private void onDownload() {
        // Implementation for download
        System.out.println("Downloading contract: " + currentContract.getContractNumber());
    }
}
