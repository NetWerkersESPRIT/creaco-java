package gui.collab.admin.request;

import entities.CollabRequest;
import entities.Collaborator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import services.CollabRequestService;
import services.CollaboratorService;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class AdminRequestListController {

    @FXML private ListView<CollabRequest> requestsListView;
    @FXML private TextField searchField;

    private final CollabRequestService requestService = new CollabRequestService();
    private final CollaboratorService collaboratorService = new CollaboratorService();
    private ObservableList<CollabRequest> requestsMasterList = FXCollections.observableArrayList();
    private javafx.scene.layout.StackPane storedContentArea;

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

        FilteredList<CollabRequest> filteredData = new FilteredList<>(requestsMasterList, r -> true);
        requestsListView.setItems(filteredData);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(req -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lower = newValue.toLowerCase();
                String partnerName = getPartnerName(req.getCollaboratorId()).toLowerCase();
                return (req.getTitle() != null && req.getTitle().toLowerCase().contains(lower)) ||
                        partnerName.contains(lower);
            });
        });
    }

    private String getPartnerName(int collabId) {
        try {
            Collaborator partner = collaboratorService.getById(collabId);
            if (partner != null && partner.getCompanyName() != null) {
                return partner.getCompanyName();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown Partner";
    }

    private Node createRequestCell(CollabRequest req) {
        HBox cell = new HBox(15);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setPadding(new Insets(15, 0, 15, 0));
        cell.getStyleClass().add("list-row");

        // Icon Avatar
        VBox avatarBox = new VBox();
        avatarBox.setAlignment(Pos.CENTER);
        avatarBox.setMinSize(50, 50);
        avatarBox.setMaxSize(50, 50);
        avatarBox.setStyle("-fx-background-color: #fff7ed; -fx-background-radius: 14; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.03), 5, 0, 0, 1);");
        Label iconLabel = new Label("👥");
        iconLabel.setStyle("-fx-font-size: 20px;");
        avatarBox.getChildren().add(iconLabel);

        // Title
        VBox info = new VBox(2);
        info.setPrefWidth(185);
        Label title = new Label(req.getTitle());
        title.getStyleClass().add("list-item-title");
        Label subtitle = new Label("ID: " + req.getId());
        subtitle.getStyleClass().add("list-item-subtitle");
        info.getChildren().addAll(title, subtitle);

        // Partner
        Label partnerLabel = new Label(getPartnerName(req.getCollaboratorId()));
        partnerLabel.setPrefWidth(200);
        partnerLabel.getStyleClass().add("list-item-title");
        partnerLabel.setStyle("-fx-text-fill: #3b82f6;");

        // Budget
        Label budget = new Label(req.getBudget() != null ? String.format("%,.0f DT", req.getBudget()) : "N/A");
        budget.setPrefWidth(120);
        budget.getStyleClass().add("list-item-subtitle");
        budget.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");

        // Status
        Label statusBadge = new Label(req.getStatus() != null ? req.getStatus().toUpperCase() : "UNKNOWN");
        statusBadge.getStyleClass().add("status-pill");
        String statusStr = req.getStatus() == null ? "" : req.getStatus().toUpperCase();
        
        if ("APPROVED".equals(statusStr)) {
            statusBadge.getStyleClass().add("stat-badge-green");
        } else if ("REJECTED".equals(statusStr)) {
            statusBadge.getStyleClass().add("stat-badge-red");
        } else if ("MODIF_REQUESTED".equals(statusStr)) {
            statusBadge.getStyleClass().add("stat-badge-blue");
        } else {
            statusBadge.getStyleClass().add("stat-badge-orange"); // Pending
        }
        statusBadge.setMinWidth(110);
        statusBadge.setAlignment(Pos.CENTER);

        // Created At
        String dateStr = req.getCreatedAt() != null ? req.getCreatedAt().toString().split(" ")[0] : "N/A";
        Label dateLabel = new Label(dateStr);
        dateLabel.setPrefWidth(150);
        dateLabel.getStyleClass().add("list-item-subtitle");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Actions
        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button viewBtn = createIconButton("👁", "#6366f1", "Preview Details");
        viewBtn.setOnAction(e -> onViewRequest(req));

        actions.getChildren().add(viewBtn);

        cell.getChildren().addAll(avatarBox, info, partnerLabel, budget, statusBadge, dateLabel, spacer, actions);
        return cell;
    }

    private Button createIconButton(String icon, String color, String tooltip) {
        Button btn = new Button(icon);
        btn.setTooltip(new Tooltip(tooltip));
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + color + "; -fx-font-size: 18px; -fx-cursor: hand; -fx-padding: 5;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #f8fafc; -fx-text-fill: " + color + "; -fx-font-size: 18px; -fx-cursor: hand; -fx-padding: 5; -fx-background-radius: 8;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + color + "; -fx-font-size: 18px; -fx-cursor: hand; -fx-padding: 5;"));
        return btn;
    }

    public void loadRequestsData() {
        try {
            requestsMasterList.setAll(requestService.afficher());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void onViewRequest(CollabRequest req) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/collab/request/viewRequest.fxml"));
            Parent root = loader.load();
            gui.collab.request.ViewRequestController controller = loader.getController();
            
            controller.setRequest(req);
            // Re-route the callbacks to simply return to this list for the admin view
            controller.setCallbacks(
                this::refreshAndReturn, // Back button
                partner -> refreshAndReturn(), // Ignore partner view routing for admin preview
                this::showContract // Load the contract view
            );
            
            replaceContent(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showContract(entities.Contract contract) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/collab/contract/viewContract.fxml"));
            Parent root = loader.load();
            gui.collab.contract.ViewContractController controller = loader.getController();
            
            controller.setContract(contract);
            // On back from contract, return to the request list
            controller.setOnBack(this::refreshAndReturn);
            
            replaceContent(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void refreshAndReturn() {
        loadRequestsData();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/collab/admin/request/admin_request_list.fxml"));
            Parent root = loader.load();
            replaceContent(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void replaceContent(Parent root) {
        if (storedContentArea == null && requestsListView != null && requestsListView.getScene() != null) {
            storedContentArea = (javafx.scene.layout.StackPane) requestsListView.getScene().lookup("#contentArea");
        }
        
        if (storedContentArea != null) {
            storedContentArea.getChildren().setAll(root);
        } else {
            System.err.println("Could not find #contentArea to replace content in AdminRequestListController.");
        }
    }
}
