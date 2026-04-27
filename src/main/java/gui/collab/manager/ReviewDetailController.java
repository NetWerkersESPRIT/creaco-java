package gui.collab.manager;

import entities.CollabRequest;
import entities.Users;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import services.CollaboratorService;
import services.CollabRequestService;
import services.UserService;
import java.text.SimpleDateFormat;
import java.sql.SQLException;

public class ReviewDetailController {

    @FXML private ImageView creatorAvatar;
    @FXML private Label titleLabel;
    @FXML private Label creatorInfoLabel;
    @FXML private Label statusBadge;
    @FXML private Label dateLabel;
    @FXML private Label partnerNameLabel;
    @FXML private Label budgetLabel;
    @FXML private Label startDateLabel;
    @FXML private Label endDateLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label deliverablesLabel;
    @FXML private Label paymentTermsLabel;
    @FXML private TextArea commentsArea;

    private final CollabRequestService requestService = new CollabRequestService();
    private final CollaboratorService partnerService = new CollaboratorService();
    private final UserService userService = new UserService();
    private final services.ContractService contractService = new services.ContractService();
    
    private CollabRequest currentRequest;
    private Runnable onBackRequested;

    private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private final SimpleDateFormat shortDf = new SimpleDateFormat("dd/MM/yyyy");

    public void setCallbacks(Runnable onBack) {
        this.onBackRequested = onBack;
    }

    public void setRequest(CollabRequest req) {
        this.currentRequest = req;
        
        titleLabel.setText(req.getTitle());
        statusBadge.setText(req.getStatus().toUpperCase());
        dateLabel.setText("Created: " + (req.getCreatedAt() != null ? df.format(req.getCreatedAt()) : "-"));
        
        budgetLabel.setText(req.getBudget() != null ? String.format("%,.2f DT", req.getBudget()) : "-");
        startDateLabel.setText(req.getStartDate() != null ? shortDf.format(req.getStartDate()) : "-");
        endDateLabel.setText(req.getEndDate() != null ? shortDf.format(req.getEndDate()) : "-");
        
        descriptionLabel.setText(req.getDescription());
        deliverablesLabel.setText(req.getDeliverables());
        paymentTermsLabel.setText(req.getPaymentTerms());
        commentsArea.setText(req.getRejectionReason());

        // Status styling
        statusBadge.getStyleClass().removeAll("status-active", "status-pending", "status-rejected");
        if ("APPROVED".equalsIgnoreCase(req.getStatus())) statusBadge.getStyleClass().add("status-active");
        else if ("PENDING".equalsIgnoreCase(req.getStatus())) statusBadge.getStyleClass().add("status-pending");
        else statusBadge.getStyleClass().add("status-rejected");

        // Load Partner & Creator
        try {
            partnerNameLabel.setText(partnerService.getById(req.getCollaboratorId()).getCompanyName());
            Users creator = userService.getUserById(req.getCreatorId());
            creatorInfoLabel.setText("Requested by " + creator.getUsername());
            
            String avatarUrl = creator.getImage();
            if (avatarUrl == null || avatarUrl.isEmpty()) {
                avatarUrl = "https://api.dicebear.com/7.x/avataaars/png?seed=" + creator.getUsername();
            }
            creatorAvatar.setImage(new Image(avatarUrl, true));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onApprove() {
        updateStatus("APPROVED");
    }

    @FXML
    private void onReject() {
        updateStatus("REJECTED");
    }

    @FXML
    private void onRequestModif() {
        updateStatus("MODIF_REQUESTED");
    }

    private void updateStatus(String status) {
        try {
            currentRequest.setStatus(status);
            currentRequest.setRejectionReason(commentsArea.getText());
            requestService.modifier(currentRequest.getId(), currentRequest);
            
            // Auto-generate contract if approved
            if ("APPROVED".equalsIgnoreCase(status)) {
                if (contractService.getByRequestId(currentRequest.getId()) == null) {
                    generateContract(currentRequest);
                }
            }
            
            setRequest(currentRequest); // Refresh UI
            
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Status Updated");
            alert.setHeaderText(null);
            alert.setContentText("The request has been " + status.toLowerCase() + " successfully.");
            alert.showAndWait();
            
        } catch (Exception e) {
            System.err.println("Error updating status: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void generateContract(CollabRequest req) throws SQLException {
        String contractNum = "CONT-" + java.time.LocalDate.now().getYear() + "-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        entities.Contract c = new entities.Contract(
                0, contractNum, req.getTitle(),
                req.getStartDate(), req.getEndDate(),
                req.getBudget(),
                "contracts/" + contractNum + ".pdf",
                "DRAFT",
                false, false,
                null, null,
                "Standard collaboration terms apply.", req.getPaymentTerms(),
                "Strict confidentiality required.", "30 days notice.",
                "TOKEN-" + contractNum,
                new java.util.Date(), null,
                req.getId(), req.getCreatorId(), req.getCollaboratorId()
        );
        contractService.ajouter(c);
    }

    @FXML
    private void onBack() {
        if (onBackRequested != null) onBackRequested.run();
    }
}
