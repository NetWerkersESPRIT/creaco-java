package gui.collab.manager;

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
import services.ContractService;
import services.CollaboratorService;
import entities.Collaborator;
import utils.SessionManager;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class ContractController {

    @FXML private ListView<Contract> contractsListView;

    private final ContractService contractService = new ContractService();
    private final CollaboratorService collaboratorService = new CollaboratorService();
    private final ObservableList<Contract> contractsList = FXCollections.observableArrayList();
    private List<Collaborator> partnersList;

    @FXML
    public void initialize() {
        setupListView();
        loadContracts();
    }

    private void setupListView() {
        contractsListView.setCellFactory(param -> new ListCell<>() {
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
        contractsListView.setItems(contractsList);
    }

    private Node createContractCell(Contract c) {
        HBox cell = new HBox(15);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setPadding(new Insets(10, 0, 10, 0));

        // Contract Icon Box
        VBox iconBox = new VBox();
        iconBox.setAlignment(Pos.CENTER);
        iconBox.setMinSize(40, 40);
        iconBox.setMaxSize(40, 40);
        iconBox.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 10;");
        Label iconLabel = new Label("📕");
        iconLabel.setStyle("-fx-font-size: 18px;");
        iconBox.getChildren().add(iconLabel);

        // Contract Info
        VBox info = new VBox(2);
        info.setPrefWidth(245);
        Label number = new Label(c.getContractNumber());
        number.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: -fx-text-main;");
        Label title = new Label(c.getTitle());
        title.setStyle("-fx-text-fill: -fx-text-muted; -fx-font-size: 11px;");
        info.getChildren().addAll(number, title);

        // Partner
        String partnerName = "Unknown Partner";
        if (partnersList != null) {
            for (Collaborator col : partnersList) {
                if (col.getId() == c.getCollaboratorId()) {
                    partnerName = col.getCompanyName();
                    break;
                }
            }
        }
        VBox pInfo = new VBox(2);
        pInfo.setPrefWidth(180);
        Label pLabel = new Label(partnerName);
        pLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #475569;");
        Label subP = new Label("Trusted Partner");
        subP.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px;");
        pInfo.getChildren().addAll(pLabel, subP);

        // Amount
        Label amount = new Label((c.getAmount() != null ? c.getAmount() + " DT" : "0.00 DT"));
        amount.setPrefWidth(150);
        amount.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: -fx-text-main;");

        // Status Badge
        Label statusBadge = new Label(c.getStatus().toUpperCase());
        statusBadge.getStyleClass().add("status-badge");
        if ("ACTIVE".equalsIgnoreCase(c.getStatus())) {
            statusBadge.getStyleClass().add("status-active");
        } else if ("DRAFT".equalsIgnoreCase(c.getStatus())) {
            statusBadge.setStyle("-fx-background-color: #64748b;");
        } else {
            statusBadge.setStyle("-fx-background-color: #94a3b8;");
        }
        statusBadge.setMinWidth(80);
        statusBadge.setAlignment(Pos.CENTER);

        // Date
        String dateStr = c.getStartDate() != null ? c.getStartDate().toString().split(" ")[0] : "-";
        Label dateLabel = new Label(dateStr);
        dateLabel.setPrefWidth(150);
        dateLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");

        // Actions
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        if ("DRAFT".equalsIgnoreCase(c.getStatus())) {
            Button sendBtn = new Button("📤 Send");
            sendBtn.getStyleClass().add("action-icon-btn");
            sendBtn.setOnAction(e -> onSend(c));
            actions.getChildren().add(sendBtn);
        }

        Button detailsBtn = new Button("DETAILS 👁");
        detailsBtn.getStyleClass().add("action-icon-btn");
        detailsBtn.setStyle("-fx-text-fill: -fx-primary-pink;");

        actions.getChildren().add(detailsBtn);

        cell.getChildren().addAll(iconBox, info, pInfo, amount, statusBadge, dateLabel, spacer, actions);
        return cell;
    }

    private void loadContracts() {
        try {
            partnersList = collaboratorService.afficher();
            Users manager = SessionManager.getInstance().getCurrentUser();
            if (manager != null) {
                contractsList.setAll(contractService.afficherByManager(manager.getId()));
            }
        } catch (SQLException e) {
            System.err.println("Error loading contracts: " + e.getMessage());
        }
    }

    private void onSend(Contract c) {
        try {
            c.setStatus("SENT_TO_COLLABORATOR");
            c.setSentAt(new Date());
            contractService.modifier(c.getId(), c);
            loadContracts();
            showAlert("Notification", "Contract sent. Navigator: Notify Content Creator logic goes here.");
        } catch (SQLException e) {
            showAlert("Error", e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
