package gui.collab.request;

import entities.CollabRequest;
import entities.Collaborator;
import entities.Contract;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import services.CollaboratorService;
import services.ContractService;
import java.text.SimpleDateFormat;

public class ViewRequestController {

    @FXML private Label titleLabel;
    @FXML private Label createdAtLabel;
    @FXML private Label statusBadge;
    @FXML private Label budgetLabel;
    @FXML private Label periodLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label deliverablesLabel;
    @FXML private Label commentsLabel;
    @FXML private VBox commentsContainer;
    
    @FXML private Label partnerNameLabel;
    @FXML private Label revisorNameLabel;
    @FXML private VBox contractCard;

    private final CollaboratorService partnerService = new CollaboratorService();
    private final ContractService contractService = new ContractService();
    
    private CollabRequest currentRequest;
    private Runnable onBackRequested;
    private java.util.function.Consumer<Collaborator> onViewPartnerRequested;
    private java.util.function.Consumer<Contract> onConsultContractRequested;

    private final SimpleDateFormat fullDate = new SimpleDateFormat("dd/MM/yyyy 'at' HH:mm");
    private final SimpleDateFormat shortDate = new SimpleDateFormat("dd/MM/yy");

    public void setCallbacks(Runnable onBack, java.util.function.Consumer<Collaborator> onViewPartner, java.util.function.Consumer<Contract> onConsultContract) {
        this.onBackRequested = onBack;
        this.onViewPartnerRequested = onViewPartner;
        this.onConsultContractRequested = onConsultContract;
    }

    public void setRequest(CollabRequest req) {
        this.currentRequest = req;
        
        titleLabel.setText(safe(req.getTitle()));
        createdAtLabel.setText("Created on " + (req.getCreatedAt() != null ? fullDate.format(req.getCreatedAt()) : "N/A"));
        
        String status = safe(req.getStatus()).toUpperCase();
        statusBadge.setText(status);
        statusBadge.getStyleClass().removeAll("status-active", "status-pending", "status-rejected");
        
        if ("APPROVED".equals(status) || "ACCEPTED".equals(status)) {
            statusBadge.getStyleClass().add("status-active");
        } else if ("PENDING".equals(status)) {
            statusBadge.getStyleClass().add("status-pending");
        } else if ("REJECTED".equals(status)) {
            statusBadge.getStyleClass().add("status-rejected");
        }

        budgetLabel.setText(req.getBudget() != null ? String.format("%,.3f DT", req.getBudget()).replace(',', ' ') : "-");
        
        String start = req.getStartDate() != null ? shortDate.format(req.getStartDate()) : "?";
        String end = req.getEndDate() != null ? shortDate.format(req.getEndDate()) : "?";
        periodLabel.setText(start + " to " + end);

        descriptionLabel.setText(safe(req.getDescription()));
        deliverablesLabel.setText(safe(req.getDeliverables()));
        
        if (req.getRejectionReason() != null && !req.getRejectionReason().isBlank()) {
            commentsLabel.setText("\"" + req.getRejectionReason() + "\"");
            commentsContainer.setVisible(true);
            commentsContainer.setManaged(true);
        } else {
            commentsContainer.setVisible(false);
            commentsContainer.setManaged(false);
        }

        // Load Partner
        try {
            Collaborator p = partnerService.getById(req.getCollaboratorId());
            if (p != null) {
                partnerNameLabel.setText(p.getCompanyName());
            } else {
                partnerNameLabel.setText("No partner linked");
            }
        } catch (Exception e) {
            partnerNameLabel.setText("Error loading partner");
        }

        // Check for Contract
        try {
            Contract c = contractService.getByRequestId(req.getId());
            if (c != null) {
                contractCard.setVisible(true);
                contractCard.setManaged(true);
            } else {
                contractCard.setVisible(false);
                contractCard.setManaged(false);
            }
        } catch (Exception e) {
            contractCard.setVisible(false);
            contractCard.setManaged(false);
        }
    }

    @FXML
    private void onBack() {
        if (onBackRequested != null) onBackRequested.run();
    }

    @FXML
    private void onViewPartner() {
        try {
            Collaborator p = partnerService.getById(currentRequest.getCollaboratorId());
            if (p != null && onViewPartnerRequested != null) onViewPartnerRequested.accept(p);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onConsultContract() {
        try {
            Contract c = contractService.getByRequestId(currentRequest.getId());
            if (c != null && onConsultContractRequested != null) onConsultContractRequested.accept(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
