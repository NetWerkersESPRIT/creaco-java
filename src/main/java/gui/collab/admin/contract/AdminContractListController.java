package gui.collab.admin.contract;

import entities.Collaborator;
import entities.Contract;
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
import services.CollaboratorService;
import services.ContractService;
import java.io.IOException;
import java.sql.SQLException;

public class AdminContractListController {

    @FXML private ListView<Contract> contractsListView;
    @FXML private TextField searchField;

    private final ContractService contractService = new ContractService();
    private final CollaboratorService collaboratorService = new CollaboratorService();
    private ObservableList<Contract> contractsMasterList = FXCollections.observableArrayList();
    private javafx.scene.layout.StackPane storedContentArea;

    @FXML
    public void initialize() {
        setupContractsView();
        loadContractsData();
    }

    private void setupContractsView() {
        contractsListView.setCellFactory(param -> new ListCell<Contract>() {
            @Override
            protected void updateItem(Contract item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setGraphic(createContractCell(item));
                }
            }
        });

        FilteredList<Contract> filteredData = new FilteredList<>(contractsMasterList, c -> true);
        contractsListView.setItems(filteredData);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(contract -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lower = newValue.toLowerCase();
                String partnerName = getPartnerName(contract.getCollaboratorId()).toLowerCase();
                return (contract.getTitle() != null && contract.getTitle().toLowerCase().contains(lower)) ||
                        (contract.getContractNumber() != null && contract.getContractNumber().toLowerCase().contains(lower)) ||
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

    private Node createContractCell(Contract contract) {
        HBox cell = new HBox(15);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setPadding(new Insets(15, 0, 15, 0));
        cell.getStyleClass().add("list-row");

        // Icon
        VBox avatarBox = new VBox();
        avatarBox.setAlignment(Pos.CENTER);
        avatarBox.setMinSize(50, 50);
        avatarBox.setMaxSize(50, 50);
        avatarBox.setStyle("-fx-background-color: #f0fdf4; -fx-background-radius: 14; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.03), 5, 0, 0, 1);");
        Label iconLabel = new Label("📄");
        iconLabel.setStyle("-fx-font-size: 20px;");
        avatarBox.getChildren().add(iconLabel);

        // Ref & Title
        VBox info = new VBox(2);
        info.setPrefWidth(185);
        Label title = new Label(contract.getTitle());
        title.getStyleClass().add("list-item-title");
        Label ref = new Label(contract.getContractNumber());
        ref.getStyleClass().add("list-item-subtitle");
        info.getChildren().addAll(title, ref);

        // Partner Entity
        Label partnerLabel = new Label(getPartnerName(contract.getCollaboratorId()));
        partnerLabel.setPrefWidth(200);
        partnerLabel.getStyleClass().add("list-item-title");
        partnerLabel.setStyle("-fx-text-fill: #3b82f6;"); // Blue to indicate entity

        // Budget
        Label budget = new Label(contract.getAmount() != null ? String.format("%,.0f DT", contract.getAmount()) : "N/A");
        budget.setPrefWidth(150);
        budget.getStyleClass().add("list-item-subtitle");
        budget.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");

        // Status Badge
        Label statusBadge = new Label(contract.getStatus() != null ? contract.getStatus().toUpperCase() : "UNKNOWN");
        statusBadge.getStyleClass().add("status-pill");
        String statusStr = contract.getStatus() == null ? "" : contract.getStatus().toUpperCase();
        if ("SIGNED".equals(statusStr) || "ACTIVE".equals(statusStr)) {
            statusBadge.getStyleClass().add("stat-badge-green");
        } else if ("EXPIRED".equals(statusStr) || "CANCELLED".equals(statusStr)) {
            statusBadge.getStyleClass().add("stat-badge-red");
        } else {
            statusBadge.getStyleClass().add("stat-badge-orange"); // Pending
        }
        statusBadge.setMinWidth(100);
        statusBadge.setAlignment(Pos.CENTER);

        // Signatures Info
        VBox signs = new VBox(2);
        signs.setPrefWidth(120);
        signs.setAlignment(Pos.CENTER_LEFT);
        Label creatorSig = new Label("Creator: " + (contract.isSignedByCreator() ? "✅" : "⏳"));
        creatorSig.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
        Label collabSig = new Label("Partner: " + (contract.isSignedByCollaborator() ? "✅" : "⏳"));
        collabSig.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
        signs.getChildren().addAll(creatorSig, collabSig);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Admin Actions
        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button viewBtn = createIconButton("👁", "#6366f1", "View Contract Details");
        viewBtn.setOnAction(e -> onViewContract(contract));

        boolean isActive = !"EXPIRED".equals(statusStr) && !"CANCELLED".equals(statusStr);
        
        Button expireBtn = createIconButton("⏳", "#f59e0b", "Mark as Expired");
        expireBtn.setDisable(!isActive);
        expireBtn.setOnAction(e -> updateContractStatus(contract, "EXPIRED"));

        Button cancelBtn = createIconButton("🚫", "#ef4444", "Cancel Contract");
        cancelBtn.setDisable(!isActive);
        cancelBtn.setOnAction(e -> updateContractStatus(contract, "CANCELLED"));

        actions.getChildren().addAll(viewBtn, expireBtn, cancelBtn);

        cell.getChildren().addAll(avatarBox, info, partnerLabel, budget, statusBadge, signs, spacer, actions);
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

    public void loadContractsData() {
        try {
            contractsMasterList.setAll(contractService.afficher());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateContractStatus(Contract contract, String newStatus) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Update Contract Status");
        confirm.setHeaderText("Mark contract as " + newStatus + "?");
        confirm.setContentText("Are you sure you want to change the status of contract " + contract.getContractNumber() + " to " + newStatus + "?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    contract.setStatus(newStatus);
                    contractService.modifier(contract.getId(), contract);
                    loadContractsData();
                } catch (SQLException e) {
                    e.printStackTrace();
                    Alert err = new Alert(Alert.AlertType.ERROR, "Error updating status: " + e.getMessage());
                    err.show();
                }
            }
        });
    }

    private void onViewContract(Contract contract) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/collab/contract/viewContract.fxml"));
            Parent root = loader.load();
            gui.collab.contract.ViewContractController controller = loader.getController();
            
            controller.setContract(contract);
            controller.setOnBack(this::refreshAndReturn);
            
            replaceContent(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refreshAndReturn() {
        loadContractsData();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/collab/admin/contract/admin_contract_list.fxml"));
            Parent root = loader.load();
            replaceContent(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void replaceContent(Parent root) {
        if (storedContentArea == null && contractsListView != null && contractsListView.getScene() != null) {
            storedContentArea = (javafx.scene.layout.StackPane) contractsListView.getScene().lookup("#contentArea");
        }
        
        if (storedContentArea != null) {
            storedContentArea.getChildren().setAll(root);
        } else {
            System.err.println("Could not find #contentArea to replace content in AdminContractListController.");
        }
    }
}
