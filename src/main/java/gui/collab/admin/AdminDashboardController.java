package gui.collab.admin;

import entities.CollabRequest;
import entities.Collaborator;
import entities.Contract;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import services.CollabRequestService;
import services.CollaboratorService;
import services.ContractService;
import javafx.fxml.FXMLLoader;
import java.io.IOException;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class AdminDashboardController {

    @FXML private Label totalPartnersLabel;
    @FXML private Label activePartnersLabel;
    @FXML private Label inactivePartnersLabel;

    @FXML private Label totalContractsLabel;
    @FXML private Label signedContractsLabel;
    @FXML private Label pendingContractsLabel;
    @FXML private Label expiredContractsLabel;

    @FXML private Label totalRequestsLabel;
    @FXML private Label pendingRequestsLabel;
    @FXML private Label approvedRequestsLabel;
    @FXML private Label rejectedRequestsLabel;
    @FXML private Label revisionRequestsLabel;

    @FXML private Label totalRevenueLabel;
    @FXML private Label actionNeededCountLabel;
    @FXML private Label actionNeededDescLabel;

    @FXML private VBox partnersList;
    @FXML private VBox contractsList;
    @FXML private VBox requestsList;

    private final CollaboratorService partnerService = new CollaboratorService();
    private final ContractService contractService = new ContractService();
    private final CollabRequestService requestService = new CollabRequestService();

    @FXML
    public void initialize() {
        refreshDashboard();
    }

    public void refreshDashboard() {
        try {
            loadPartnerStats();
            loadContractStats();
            loadRequestStats();
            loadKPIs();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onViewPartners() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/collab/admin/partner/admin_partner_list.fxml"));
            javafx.scene.Parent root = loader.load();
            
            javafx.scene.layout.StackPane contentArea = (javafx.scene.layout.StackPane) totalPartnersLabel.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onViewContracts() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/collab/admin/contract/admin_contract_list.fxml"));
            javafx.scene.Parent root = loader.load();
            
            javafx.scene.layout.StackPane contentArea = (javafx.scene.layout.StackPane) totalContractsLabel.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onViewRequests() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/collab/admin/request/admin_request_list.fxml"));
            javafx.scene.Parent root = loader.load();
            
            javafx.scene.layout.StackPane contentArea = (javafx.scene.layout.StackPane) totalRequestsLabel.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPartnerStats() throws SQLException {
        List<Collaborator> partners = partnerService.afficher();
        totalPartnersLabel.setText(String.valueOf(partners.size()));
        
        long active = partners.stream().filter(p -> "ACTIVE".equalsIgnoreCase(p.getStatus())).count();
        activePartnersLabel.setText(String.valueOf(active));
        inactivePartnersLabel.setText(String.valueOf(partners.size() - active));

        // Populate Recent Partners List (last 4)
        partnersList.getChildren().clear();
        List<Collaborator> recent = partners.stream()
                .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                .limit(4)
                .collect(Collectors.toList());
        
        for (Collaborator p : recent) {
            partnersList.getChildren().add(createPartnerRow(p));
        }
    }

    private void loadContractStats() throws SQLException {
        List<Contract> contracts = contractService.afficher();
        totalContractsLabel.setText(String.valueOf(contracts.size()));

        long signed = contracts.stream().filter(c -> "SIGNED".equalsIgnoreCase(c.getStatus()) || "ACTIVE".equalsIgnoreCase(c.getStatus())).count();
        long pending = contracts.stream().filter(c -> "SENT_TO_PARTNER".equalsIgnoreCase(c.getStatus()) || "DRAFT".equalsIgnoreCase(c.getStatus())).count();
        long expired = contracts.size() - signed - pending;

        signedContractsLabel.setText(String.valueOf(signed));
        pendingContractsLabel.setText(String.valueOf(pending));
        expiredContractsLabel.setText(String.valueOf(expired));

        // Populate Recent Contracts List (last 4)
        contractsList.getChildren().clear();
        List<Contract> recent = contracts.stream()
                .sorted((c1, c2) -> c2.getCreatedAt().compareTo(c1.getCreatedAt()))
                .limit(4)
                .collect(Collectors.toList());

        for (Contract c : recent) {
            contractsList.getChildren().add(createContractRow(c));
        }
    }

    private void loadRequestStats() throws SQLException {
        List<CollabRequest> requests = requestService.afficher();
        totalRequestsLabel.setText(String.valueOf(requests.size()));

        long pending = requests.stream().filter(r -> "PENDING".equalsIgnoreCase(r.getStatus())).count();
        long approved = requests.stream().filter(r -> "APPROVED".equalsIgnoreCase(r.getStatus())).count();
        long rejected = requests.stream().filter(r -> "REJECTED".equalsIgnoreCase(r.getStatus())).count();
        long revision = requests.stream().filter(r -> "MODIF_REQUESTED".equalsIgnoreCase(r.getStatus())).count();

        pendingRequestsLabel.setText(String.valueOf(pending));
        approvedRequestsLabel.setText(String.valueOf(approved));
        rejectedRequestsLabel.setText(String.valueOf(rejected));
        revisionRequestsLabel.setText(String.valueOf(revision));

        // Populate Requests to Review (last 4 pending)
        requestsList.getChildren().clear();
        List<CollabRequest> toReview = requests.stream()
                .filter(r -> "PENDING".equalsIgnoreCase(r.getStatus()))
                .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                .limit(4)
                .collect(Collectors.toList());

        for (CollabRequest r : toReview) {
            requestsList.getChildren().add(createRequestRow(r));
        }
    }

    private void loadKPIs() throws SQLException {
        List<Contract> contracts = contractService.afficher();
        BigDecimal totalSecured = contracts.stream()
                .filter(c -> !"REJECTED".equalsIgnoreCase(c.getStatus()))
                .map(Contract::getAmount)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalRevenueLabel.setText(String.format("%,.0f DT", totalSecured));

        List<CollabRequest> requests = requestService.afficher();
        long pendingReq = requests.stream().filter(r -> "PENDING".equalsIgnoreCase(r.getStatus())).count();
        
        actionNeededCountLabel.setText(String.valueOf(pendingReq));
        actionNeededDescLabel.setText(pendingReq + " pending requests requiring decision.");
    }

    private javafx.scene.Node createPartnerRow(Collaborator p) {
        javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(15);
        row.getStyleClass().add("list-item-row");
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        javafx.scene.layout.StackPane avatar = new javafx.scene.layout.StackPane();
        avatar.getStyleClass().add("avatar-circle");
        avatar.setStyle("-fx-background-color: #e0e7ff;");
        Label initial = new Label(p.getCompanyName().substring(0, 1).toUpperCase());
        initial.getStyleClass().add("avatar-text");
        avatar.getChildren().add(initial);

        VBox details = new VBox(2);
        Label name = new Label(p.getCompanyName());
        name.getStyleClass().add("list-item-title");
        Label domain = new Label(p.getDomain());
        domain.getStyleClass().add("list-item-subtitle");
        details.getChildren().addAll(name, domain);
        javafx.scene.layout.HBox.setHgrow(details, javafx.scene.layout.Priority.ALWAYS);

        Label status = new Label(p.getStatus().toUpperCase());
        status.getStyleClass().addAll("status-pill", "ACTIVE".equalsIgnoreCase(p.getStatus()) ? "stat-badge-green" : "stat-badge-gray");

        row.getChildren().addAll(avatar, details, status);
        return row;
    }

    private javafx.scene.Node createContractRow(Contract c) {
        javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(15);
        row.getStyleClass().add("list-item-row");
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        javafx.scene.layout.StackPane iconBg = new javafx.scene.layout.StackPane();
        iconBg.getStyleClass().add("avatar-circle");
        iconBg.setStyle("-fx-background-color: #f0fdf4;");
        Label icon = new Label("📄");
        iconBg.getChildren().add(icon);

        VBox details = new VBox(2);
        Label ref = new Label(c.getContractNumber());
        ref.getStyleClass().add("list-item-subtitle");
        Label title = new Label(c.getTitle());
        title.getStyleClass().add("list-item-title");
        details.getChildren().addAll(ref, title);
        javafx.scene.layout.HBox.setHgrow(details, javafx.scene.layout.Priority.ALWAYS);

        Label status = new Label(c.getStatus().toUpperCase());
        String statusClass = "stat-badge-gray";
        if ("SIGNED".equalsIgnoreCase(c.getStatus())) statusClass = "stat-badge-green";
        else if ("SENT_TO_PARTNER".equalsIgnoreCase(c.getStatus())) statusClass = "stat-badge-orange";
        
        status.getStyleClass().addAll("status-pill", statusClass);

        row.getChildren().addAll(iconBg, details, status);
        return row;
    }

    private javafx.scene.Node createRequestRow(CollabRequest r) {
        javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(15);
        row.getStyleClass().add("list-item-row");
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        javafx.scene.layout.StackPane avatar = new javafx.scene.layout.StackPane();
        avatar.getStyleClass().add("avatar-circle");
        avatar.setStyle("-fx-background-color: #fff7ed;");
        Label initial = new Label(r.getTitle().substring(0, 1).toUpperCase());
        initial.setStyle("-fx-text-fill: #9a3412; -fx-font-weight: bold;");
        avatar.getChildren().add(initial);

        VBox details = new VBox(2);
        Label title = new Label(r.getTitle());
        title.getStyleClass().add("list-item-title");
        Label budget = new Label(String.format("%,.0f DT", r.getBudget()));
        budget.getStyleClass().add("list-item-subtitle");
        details.getChildren().addAll(title, budget);
        javafx.scene.layout.HBox.setHgrow(details, javafx.scene.layout.Priority.ALWAYS);

        Label status = new Label(r.getStatus().toUpperCase());
        status.getStyleClass().addAll("status-pill", "stat-badge-orange");

        row.getChildren().addAll(avatar, details, status);
        return row;
    }
}
