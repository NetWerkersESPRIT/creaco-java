package gui.collab.manager;

import entities.CollabRequest;
import entities.Contract;
import entities.Users;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import services.CollabRequestService;
import services.ContractService;
import services.CollaboratorService;
import services.UsersService;
import entities.Collaborator;
import utils.SessionManager;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

public class RequestController {

    @FXML private ListView<CollabRequest> requestsListView;

    private final CollabRequestService requestService = new CollabRequestService();
    private final ContractService contractService = new ContractService();
    private final CollaboratorService collaboratorService = new CollaboratorService();
    private final UsersService usersService = new UsersService();
    private final ObservableList<CollabRequest> requestsList = FXCollections.observableArrayList();
    private List<Collaborator> partnersList;
    private List<Users> usersList;

    @FXML
    public void initialize() {
        setupListView();
        loadRequests();
    }

    private void setupListView() {
        requestsListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(CollabRequest item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setGraphic(createRequestCell(item));
                }
            }
        });
        requestsListView.setItems(requestsList);
    }

    private Node createRequestCell(CollabRequest req) {
        HBox cell = new HBox(15);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setPadding(new Insets(10, 0, 10, 0));

        // Request Icon Box
        VBox iconBox = new VBox();
        iconBox.setAlignment(Pos.CENTER);
        iconBox.setMinSize(40, 40);
        iconBox.setMaxSize(40, 40);
        iconBox.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 10;");
        Label iconLabel = new Label("📝");
        iconLabel.setStyle("-fx-font-size: 18px;");
        iconBox.getChildren().add(iconLabel);

        // Project Info
        String partnerName = "Unknown Partner";
        if (partnersList != null) {
            for (Collaborator col : partnersList) {
                if (col.getId() == req.getCollaboratorId()) {
                    partnerName = col.getCompanyName();
                    break;
                }
            }
        }
        VBox info = new VBox(2);
        info.setPrefWidth(280);
        Label title = new Label(req.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: -fx-text-main;");
        Label partner = new Label(partnerName);
        partner.setStyle("-fx-text-fill: -fx-text-muted; -fx-font-size: 11px;");
        info.getChildren().addAll(title, partner);

        // Creator
        String creatorName = "Unknown User";
        String creatorRole = "User";
        if (usersList != null) {
            for (Users u : usersList) {
                if (u.getId() == req.getCreatorId()) {
                    creatorName = u.getUsername();
                    creatorRole = u.getRole();
                    break;
                }
            }
        }
        VBox cInfo = new VBox(2);
        cInfo.setPrefWidth(180);
        Label cLabel = new Label(creatorName);
        cLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #475569;");
        Label subC = new Label(creatorRole);
        subC.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px;");
        cInfo.getChildren().addAll(cLabel, subC);

        // Budget
        Label budget = new Label((req.getBudget() != null ? req.getBudget() + " DT" : "0.00 DT"));
        budget.setPrefWidth(150);
        budget.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: -fx-text-main;");

        // Status Badge
        Label statusBadge = new Label(req.getStatus().toUpperCase());
        statusBadge.getStyleClass().add("status-badge");
        if ("PENDING".equalsIgnoreCase(req.getStatus())) {
            statusBadge.setStyle("-fx-background-color: #1e293b;");
        } else if ("APPROVED".equalsIgnoreCase(req.getStatus())) {
            statusBadge.getStyleClass().add("status-active");
        } else {
            statusBadge.setStyle("-fx-background-color: #94a3b8;");
        }
        statusBadge.setMinWidth(80);
        statusBadge.setAlignment(Pos.CENTER);

        // Date
        String dateStr = req.getCreatedAt() != null ? req.getCreatedAt().toString().split(" ")[0] : "-";
        Label dateLabel = new Label(dateStr);
        dateLabel.setPrefWidth(150);
        dateLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");

        // Actions
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        if ("PENDING".equalsIgnoreCase(req.getStatus())) {
            MenuButton reviewBtn = new MenuButton("REVIEW");
            reviewBtn.getStyleClass().add("action-icon-btn");
            reviewBtn.setStyle("-fx-text-fill: -fx-primary-pink; -fx-background-color: transparent; -fx-font-weight: bold;");

            MenuItem approveItem = new MenuItem("Approve");
            approveItem.setOnAction(e -> onApprove(req));

            MenuItem modifyItem = new MenuItem("Request Modification");
            modifyItem.setOnAction(e -> onModify(req));

            MenuItem rejectItem = new MenuItem("Reject");
            rejectItem.setOnAction(e -> onReject(req));

            reviewBtn.getItems().addAll(approveItem, modifyItem, rejectItem);
            actions.getChildren().add(reviewBtn);
        } else {
            Button viewBtn = new Button("VIEW ➔");
            viewBtn.getStyleClass().add("action-icon-btn");
            viewBtn.setStyle("-fx-text-fill: #94a3b8;");
            actions.getChildren().add(viewBtn);
        }

        cell.getChildren().addAll(iconBox, info, cInfo, budget, statusBadge, dateLabel, spacer, actions);
        return cell;
    }

    private void loadRequests() {
        try {
            partnersList = collaboratorService.afficher();
            usersList = usersService.afficher();
            Users manager = SessionManager.getInstance().getCurrentUser();
            if (manager != null) {
                requestsList.setAll(requestService.afficherByManager(manager.getId()));
            }
        } catch (SQLException e) {
            System.err.println("Error loading requests: " + e.getMessage());
        }
    }

    private void onApprove(CollabRequest req) {
        try {
            req.setStatus("APPROVED");
            requestService.modifier(req.getId(), req);

            // Generate Draft Contract
            Contract c = new Contract(
                    "CON-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                    "Contract for " + req.getTitle(),
                    req.getStartDate(),
                    req.getEndDate(),
                    req.getBudget(),
                    "DRAFT",
                    req.getDescription(),
                    req.getPaymentTerms(),
                    "Confidential",
                    "Standard cancellation",
                    UUID.randomUUID().toString(),
                    req.getId(),
                    req.getRevisorId(), // Manager is the creator of the contract
                    req.getCollaboratorId()
            );
            contractService.ajouter(c);

            loadRequests();
            showAlert("Success", "Request approved and draft contract generated.");
        } catch (SQLException e) {
            showAlert("Error", e.getMessage());
        }
    }

    private void onReject(CollabRequest req) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reject Request");
        dialog.setHeaderText("Reason for rejection:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(reason -> {
            try {
                req.setStatus("REJECTED");
                req.setRejectionReason(reason);
                requestService.modifier(req.getId(), req);
                loadRequests();
            } catch (SQLException e) {
                showAlert("Error", e.getMessage());
            }
        });
    }

    private void onModify(CollabRequest req) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Request Modification");
        dialog.setHeaderText("Comment for modification:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(comment -> {
            try {
                req.setStatus("MODIFICATION_REQUESTED");
                req.setRejectionReason(comment); // Reuse field for comment
                requestService.modifier(req.getId(), req);
                loadRequests();
            } catch (SQLException e) {
                showAlert("Error", e.getMessage());
            }
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
