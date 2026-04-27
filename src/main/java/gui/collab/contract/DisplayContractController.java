package gui.collab.contract;
import entities.Contract;
import entities.Collaborator;
import services.ContractService;
import services.CollaboratorService;
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
import java.util.List;

public class DisplayContractController {

    @FXML private ListView<Contract> contractsListView;
    @FXML private ComboBox<String> statusFilter;

    private final ContractService contractService = new ContractService();
    private final CollaboratorService collaboratorService = new CollaboratorService();
    private ObservableList<Contract> contractsMasterList = FXCollections.observableArrayList();
    private List<Collaborator> partnersList;

    private java.util.function.Consumer<Contract> onViewRequested;

    public void setOnViewRequested(java.util.function.Consumer<Contract> callback) { this.onViewRequested = callback; }

    @FXML
    public void initialize() {
        setupContractsView();
        loadContractsData();
        
        statusFilter.setItems(FXCollections.observableArrayList("All Statuses", "DRAFT", "ACTIVE", "COMPLETED", "TERMINATED"));
        statusFilter.setValue("All Statuses");
    }

    private void setupContractsView() {
        contractsListView.setCellFactory(param -> new ListCell<Contract>() {
            @Override
            protected void updateItem(Contract item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setGraphic(createContractCell(item));
                    setStyle("-fx-background-color: transparent;");
                }
            }
        });
        contractsListView.setItems(contractsMasterList);
    }

    private Node createContractCell(Contract c) {
        HBox cell = new HBox(15);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setPadding(new Insets(12, 0, 12, 0));
        cell.getStyleClass().add("list-row");

        // Contract Info
        VBox info = new VBox(2);
        info.setPrefWidth(285);
        Label title = new Label(safeText(c.getContractNumber()));
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1e293b;");
        
        String partnerName = "-";
        String domain = "E-commerce"; // Default or lookup
        if (partnersList != null) {
            for (Collaborator partner : partnersList) {
                if (partner.getId() == c.getCollaboratorId()) {
                    partnerName = partner.getCompanyName();
                    domain = partner.getDomain();
                    break;
                }
            }
        }
        Label partner = new Label("Partner: " + partnerName);
        partner.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
        info.getChildren().addAll(title, partner);

        // Domain
        Label domainLabel = new Label(domain);
        domainLabel.setPrefWidth(150);
        domainLabel.setStyle("-fx-text-fill: #1e293b; -fx-font-size: 13px; -fx-font-weight: bold;");

        // Status Badge
        Label statusBadge = new Label(safeText(c.getStatus()).toUpperCase());
        statusBadge.getStyleClass().add("status-badge");
        String status = c.getStatus().toUpperCase();
        if ("ACTIVE".equalsIgnoreCase(status)) statusBadge.getStyleClass().add("status-active");
        else if ("DRAFT".equalsIgnoreCase(status) || "SIGNED_BY_COLLABORATOR".equalsIgnoreCase(status)) statusBadge.setStyle("-fx-background-color: #3b82f6;");
        else if ("TERMINATED".equalsIgnoreCase(status)) statusBadge.setStyle("-fx-background-color: #ef4444;");
        else statusBadge.setStyle("-fx-background-color: #94a3b8;");
        
        statusBadge.setMinWidth(120);
        statusBadge.setAlignment(Pos.CENTER);

        // Effective Date
        Label dateLabel = new Label(c.getStartDate() != null ? c.getStartDate().toString().split(" ")[0] : "-");
        dateLabel.setPrefWidth(200);
        dateLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");

        // Actions
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Hyperlink viewBtn = new Hyperlink("View");
        viewBtn.getStyleClass().add("action-link");
        viewBtn.setStyle("-fx-text-fill: #ce2d7c;"); // Pink "View" link as per mockup
        viewBtn.setOnAction(e -> {
            if (onViewRequested != null) onViewRequested.accept(c);
        });

        cell.getChildren().addAll(info, domainLabel, statusBadge, dateLabel, spacer, viewBtn);
        return cell;
    }

    public void loadContractsData() {
        try {
            partnersList = collaboratorService.afficher();
            contractsMasterList.setAll(contractService.afficher());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
