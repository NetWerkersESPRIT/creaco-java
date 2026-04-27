package gui.collab.request;

import entities.Collaborator;
import entities.CollabRequest;
import entities.Contract;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import services.CollabRequestService;
import services.CollaboratorService;
import services.ContractService;
import java.util.List;

public class DisplayRequestController {

    @FXML private ListView<CollabRequest> requestsListView;
    @FXML private TextField requestSearchField;

    private final CollabRequestService requestService = new CollabRequestService();
    private final CollaboratorService collaboratorService = new CollaboratorService();
    private final ContractService contractService = new ContractService();

    private ObservableList<CollabRequest> requestsMasterList = FXCollections.observableArrayList();
    private List<Collaborator> partnersList;

    private Runnable onAddRequested;
    private java.util.function.Consumer<CollabRequest> onEditRequested;
    private java.util.function.Consumer<CollabRequest> onViewRequested;

    public void setOnAddRequested(Runnable callback) { this.onAddRequested = callback; }
    public void setOnEditRequested(java.util.function.Consumer<CollabRequest> callback) { this.onEditRequested = callback; }
    public void setOnViewRequested(java.util.function.Consumer<CollabRequest> callback) { this.onViewRequested = callback; }

    @FXML
    public void initialize() {
        setupRequestsView();
        loadRequestsData();
    }

    private void setupRequestsView() {
        requestsListView.setCellFactory(param -> new ListCell<CollabRequest>() {
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

        javafx.collections.transformation.FilteredList<CollabRequest> filteredData = new javafx.collections.transformation.FilteredList<>(requestsMasterList, p -> true);
        requestsListView.setItems(filteredData);

        if (requestSearchField != null) {
            requestSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(req -> {
                    if (newValue == null || newValue.isEmpty()) return true;
                    String lower = newValue.toLowerCase();
                    return safeText(req.getTitle()).toLowerCase().contains(lower) ||
                            safeText(req.getStatus()).toLowerCase().contains(lower);
                });
            });
        }
    }

    private Node createRequestCell(CollabRequest req) {
        HBox cell = new HBox(15);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setPadding(new Insets(15, 0, 15, 0));
        cell.getStyleClass().add("list-row");

        // Request Icon Box
        VBox iconBox = new VBox();
        iconBox.setAlignment(Pos.CENTER);
        iconBox.setMinSize(45, 45);
        iconBox.setMaxSize(45, 45);
        iconBox.setStyle("-fx-background-color: #fef3c7; -fx-background-radius: 12;");
        Label iconLabel = new Label("🚀");
        iconLabel.setStyle("-fx-font-size: 22px;");
        iconBox.getChildren().add(iconLabel);

        // Project Info
        VBox info = new VBox(2);
        info.setPrefWidth(245);
        Label title = new Label(safeText(req.getTitle()));
        title.getStyleClass().add("card-title");
        title.setStyle("-fx-font-size: 14px;");

        String partnerName = "-";
        if (partnersList != null) {
            for (Collaborator c : partnersList) {
                if (c.getId() == req.getCollaboratorId()) {
                    partnerName = c.getCompanyName();
                    break;
                }
            }
        }
        Label partner = new Label("Partner: " + partnerName);
        partner.getStyleClass().add("card-subtitle");
        info.getChildren().addAll(title, partner);

        // Budget
        Label budget = new Label((req.getBudget() != null ? req.getBudget() + " TND" : "-"));
        budget.setPrefWidth(150);
        budget.getStyleClass().add("form-label");
        budget.setStyle("-fx-font-size: 13px;");

        // Status Badge
        Label statusBadge = new Label(safeText(req.getStatus()).toUpperCase());
        statusBadge.getStyleClass().add("status-badge");
        String status = req.getStatus().toUpperCase();
        if ("PENDING".equals(status)) {
            statusBadge.getStyleClass().add("status-pending");
        } else if ("APPROVED".equals(status) || "ACCEPTED".equals(status)) {
            statusBadge.getStyleClass().add("status-active");
        } else if ("REJECTED".equals(status)) {
            statusBadge.getStyleClass().add("status-rejected");
        } else {
            statusBadge.setStyle("-fx-background-color: #94a3b8;");
        }

        statusBadge.setMinWidth(95);
        statusBadge.setAlignment(Pos.CENTER);

        // Period
        String start = req.getStartDate() != null ? req.getStartDate().toString().split(" ")[0] : "?";
        String end = req.getEndDate() != null ? req.getEndDate().toString().split(" ")[0] : "?";
        Label periodLabel = new Label(start + " - " + end);
        periodLabel.setPrefWidth(200);
        periodLabel.getStyleClass().add("card-subtitle");

        // Actions
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Hyperlink viewBtn = new Hyperlink("View");
        viewBtn.getStyleClass().add("action-link");
        viewBtn.setOnAction(e -> onViewRequest(req));

        Hyperlink editBtn = new Hyperlink("Edit");
        editBtn.getStyleClass().add("action-link");
        editBtn.setOnAction(e -> { if(onEditRequested != null) onEditRequested.accept(req); });

        actions.getChildren().addAll(viewBtn, editBtn);

        cell.getChildren().addAll(iconBox, info, budget, statusBadge, periodLabel, spacer, actions);
        return cell;
    }

    public void loadRequestsData() {
        try {
            partnersList = collaboratorService.afficher();
            requestsMasterList.setAll(requestService.afficher());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onAddRequest() {
        if (onAddRequested != null) onAddRequested.run();
    }

    private void onViewRequest(CollabRequest req) {
        if (onViewRequested != null) onViewRequested.accept(req);
    }

    private void onDeleteRequest(CollabRequest req) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setContentText("Are you sure you want to delete this request?");
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    requestService.supprimer(req.getId());
                    loadRequestsData();
                } catch (Exception e) {
                    showAlert("Delete Error", "Could not delete request: " + e.getMessage());
                }
            }
        });
    }

    private void onGenerateContract(CollabRequest req) {
        try {
            String contractNum = "CNTR-" + System.currentTimeMillis() % 10000;
            Contract c = new Contract(
                    0, contractNum, req.getTitle(),
                    req.getStartDate(), req.getEndDate(),
                    req.getBudget(),
                    "contracts/" + contractNum + ".pdf",
                    "SIGNED_BY_COLLABORATOR",
                    false, true,
                    null, new java.util.Date(),
                    "Standard terms apply.", req.getPaymentTerms(),
                    "Strict confidentiality.", null,
                    "TOKEN-" + contractNum,
                    new java.util.Date(), null,
                    req.getId(), req.getCreatorId(), req.getCollaboratorId()
            );
            contractService.ajouter(c);
            showAlert("Success", "Contract generated successfully.");
        } catch (Exception e) {
            showAlert("Error", "Could not generate contract: " + e.getMessage());
        }
    }

    private Button createActionButton(String text, String textColor, String backgroundColor) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + backgroundColor + "; -fx-text-fill: " + textColor
                + "; -fx-background-radius: 12; -fx-padding: 10 16 10 16; -fx-font-weight: bold;");
        return button;
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
