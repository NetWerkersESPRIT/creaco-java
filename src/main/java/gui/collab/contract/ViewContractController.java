package gui.collab.contract;

import entities.Contract;
import entities.Collaborator;
import entities.Users;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import services.CollaboratorService;
import services.ContractService;
import services.DocuSignService;
import utils.EnvelopeStore;
import utils.SessionManager;
import services.NotificationService;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileOutputStream;
import javafx.scene.control.Alert.AlertType;

public class ViewContractController {

    @FXML private Label refLabel, protocolLabel, issuanceDateLabel, providerNameLabel, providerRepLabel, clientNameLabel;
    @FXML private Text termsText;
    @FXML private TextField partnerEmailField, partnerNameField;
    @FXML private Button btnSendDocuSign, btnSignContract, btnCheckStatus;
    @FXML private Label dsStatusLabel, dsEnvelopeIdLabel;
    @FXML private VBox dsSentBox, dsReadyBox, heroBanner;

    private final CollaboratorService collaboratorService = new CollaboratorService();
    private final ContractService contractService = new ContractService();
    private final DocuSignService docuSignService = new DocuSignService();
    private final NotificationService notificationService = new NotificationService();
    private final services.CollabRequestService requestService = new services.CollabRequestService();

    private Contract currentContract;
    private DocuSignPoller poller;
    private Runnable onBackRequested;

    public void setOnBack(Runnable callback) { this.onBackRequested = callback; }

    public void setContract(Contract contract) {
        this.currentContract = contract;
        refLabel.setText("REF: " + contract.getContractNumber());
        protocolLabel.setText("DIGITAL PROTOCOL #" + contract.getContractNumber());
        
        Users creator = SessionManager.getInstance().getCurrentUser();
        if (clientNameLabel != null) clientNameLabel.setText(creator != null ? creator.getUsername() : "Creator");
        if (termsText != null) termsText.setText(contract.getTerms() != null ? contract.getTerms() : "No terms defined.");
        if (issuanceDateLabel != null) issuanceDateLabel.setText(contract.getStartDate() != null ? contract.getStartDate().toString() : "N/A");

        try {
            Collaborator partner = collaboratorService.getById(contract.getCollaboratorId());
            if (partner != null) {
                providerNameLabel.setText(partner.getCompanyName());
                providerRepLabel.setText("Representative: " + partner.getName());
                partnerEmailField.setText(partner.getEmail());
                partnerNameField.setText(partner.getName());
            }
        } catch (Exception e) { e.printStackTrace(); }

        String envId = EnvelopeStore.get(contract.getId());
        if (envId != null) {
            showSentState(envId);
            startPoller(envId);
        } else {
            showReadyState();
        }

        // Generate AI Background
        applyAiBackground();
    }

    private void applyAiBackground() {
        new Thread(() -> {
            javafx.scene.image.Image bg = utils.GeminiImageService.generateHeroBackground("Signing a professional digital contract on a tablet in a premium office environment");
            if (bg != null) {
                Platform.runLater(() -> {
                    heroBanner.setBackground(new javafx.scene.layout.Background(
                        new javafx.scene.layout.BackgroundImage(bg, 
                            javafx.scene.layout.BackgroundRepeat.NO_REPEAT, 
                            javafx.scene.layout.BackgroundRepeat.NO_REPEAT, 
                            javafx.scene.layout.BackgroundPosition.CENTER, 
                            new javafx.scene.layout.BackgroundSize(100, 100, true, true, true, true))
                    ));
                });
            }
        }).start();
    }

    @FXML
    private void onSendForSignature() {
        Users creator = SessionManager.getInstance().getCurrentUser();
        if (creator == null) return;

        btnSendDocuSign.setDisable(true);
        dsStatusLabel.setText("Creating dual-signer envelope...");

        Thread t = new Thread(() -> {
            try {
                String envId = docuSignService.sendEnvelopeForSignature(
                    currentContract, 
                    partnerNameField.getText(), partnerEmailField.getText(),
                    creator.getUsername(), creator.getEmail()
                );
                EnvelopeStore.save(currentContract.getId(), envId);
                Platform.runLater(() -> {
                    showSentState(envId);
                    startPoller(envId);
                    // Notify Creator
                    notificationService.notifyContractSent(creator.getId(), currentContract.getContractNumber());
                });
            } catch (Exception e) {
                Platform.runLater(() -> dsStatusLabel.setText("Error: " + e.getMessage()));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void applyStatusSummary(String summary) {
        String newDbStatus = null;
        switch (summary) {
            case "BOTH_SIGNED" -> {
                dsStatusLabel.setText("✅ Signed");
                dsStatusLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                newDbStatus = "SIGNED";
            }
            case "PARTNER_SIGNED" -> {
                dsStatusLabel.setText("📝 Signed by Partner");
                dsStatusLabel.setStyle("-fx-text-fill: #0ea5e9;");
                newDbStatus = "SIGNED_BY_PARTNER";
            }
            case "CREATOR_SIGNED" -> {
                dsStatusLabel.setText("👤 Signed by You");
                dsStatusLabel.setStyle("-fx-text-fill: #7c3aed;");
                newDbStatus = "SIGNED_BY_CREATOR";
            }
            default -> {
                dsStatusLabel.setText("⏳ Awaiting signatures...");
                dsStatusLabel.setStyle("-fx-text-fill: #64748b;");
            }
        }

        // Update Database if status changed
        if (newDbStatus != null && !newDbStatus.equals(currentContract.getStatus())) {
            final String finalStatus = newDbStatus;
            String oldStatus = currentContract.getStatus();
            currentContract.setStatus(finalStatus);
            
            new Thread(() -> {
                try {
                    contractService.modifier(currentContract.getId(), currentContract);
                    System.out.println("Contract DB status updated to: " + finalStatus);
                    
                    // Trigger Notifications based on specific transitions
                    if ("SIGNED".equals(finalStatus)) {
                        notificationService.notifyContractCompleted(currentContract.getCreatorId(), currentContract.getContractNumber());
                        notifyManagerOfSignature(currentContract, "COMPLETED");
                    } else if ("SIGNED_BY_PARTNER".equals(finalStatus) && !"SIGNED".equals(oldStatus)) {
                        notificationService.notifyContractSignature(currentContract.getCreatorId(), currentContract.getContractNumber(), "PARTNER");
                        notifyManagerOfSignature(currentContract, "PARTNER");
                    } else if ("SIGNED_BY_CREATOR".equals(finalStatus) && !"SIGNED".equals(oldStatus)) {
                        notificationService.notifyContractSignature(currentContract.getCreatorId(), currentContract.getContractNumber(), "CREATOR");
                        notifyManagerOfSignature(currentContract, "CREATOR");
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }).start();
        }
    }

    private void notifyManagerOfSignature(Contract c, String type) {
        try {
            // Get the request to find the manager (revisor_id)
            java.util.List<entities.CollabRequest> allReqs = requestService.afficher();
            for (entities.CollabRequest req : allReqs) {
                if (req.getId() == c.getCollabRequestId()) {
                    if ("COMPLETED".equals(type)) {
                        notificationService.createNotification(req.getRevisorId(), "🎉 Collaboration " + c.getContractNumber() + " is now fully signed!", "CONTRACT_COMPLETED", null, "manager/contracts");
                    } else {
                        notificationService.createNotification(req.getRevisorId(), "📝 Contract " + c.getContractNumber() + " signature progress: " + type + " signed.", "CONTRACT_PROGRESS", null, "manager/contracts");
                    }
                    break;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void startPoller(String envId) {
        if (poller != null) poller.cancel();
        poller = new DocuSignPoller(envId);
        poller.setOnSucceeded(e -> applyStatusSummary(poller.getValue()));
        poller.start();
    }

    @FXML private void onCheckStatus() { if (poller != null) poller.restart(); }

    @FXML private void onSignContract() {
        Users creator = SessionManager.getInstance().getCurrentUser();
        String envId = EnvelopeStore.get(currentContract.getId());
        if (creator == null || envId == null) return;

        btnSignContract.setDisable(true);
        Thread t = new Thread(() -> {
            try {
                String url = docuSignService.getEmbeddedSigningUrl(envId, creator.getUsername(), creator.getEmail());
                Platform.runLater(() -> {
                    // Open browser
                    if (java.awt.Desktop.isDesktopSupported()) {
                        try {
                            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
                        } catch (Exception ex) { ex.printStackTrace(); }
                    }
                    btnSignContract.setDisable(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> btnSignContract.setDisable(false));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void showReadyState() {
        dsReadyBox.setVisible(true); dsReadyBox.setManaged(true);
        dsSentBox.setVisible(false); dsSentBox.setManaged(false);
    }

    private void showSentState(String envId) {
        dsReadyBox.setVisible(false); dsReadyBox.setManaged(false);
        dsSentBox.setVisible(true); dsSentBox.setManaged(true);
        dsEnvelopeIdLabel.setText(envId);
    }

    @FXML private void onBack() { 
        if (poller != null) poller.cancel();
        if (onBackRequested != null) onBackRequested.run(); 
    }

    @FXML
    private void onDownload() {
        String envelopeId = EnvelopeStore.get(currentContract.getId());
        if (envelopeId == null) {
            showAlert(javafx.scene.control.Alert.AlertType.WARNING, "Not Ready", "The DocuSign envelope for this contract is not yet initialized.");
            return;
        }

        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Save Signed Contract");
        fileChooser.setInitialFileName("Contract_" + currentContract.getContractNumber() + ".pdf");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("PDF Documents", "*.pdf"));
        
        java.io.File file = fileChooser.showSaveDialog(refLabel.getScene().getWindow());
        if (file != null) {
            new Thread(() -> {
                try {
                    byte[] pdfData = docuSignService.downloadCombinedDocument(envelopeId);
                    try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                        fos.write(pdfData);
                    }
                    Platform.runLater(() -> showAlert(javafx.scene.control.Alert.AlertType.INFORMATION, "Success", "Contract saved successfully to: " + file.getAbsolutePath()));
                } catch (Exception e) {
                    Platform.runLater(() -> showAlert(javafx.scene.control.Alert.AlertType.ERROR, "Download Failed", "Could not retrieve the document from DocuSign: " + e.getMessage()));
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void showAlert(javafx.scene.control.Alert.AlertType type, String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
