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
    @FXML private TextField contractSearchField;
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

        javafx.collections.transformation.FilteredList<Contract> filteredData = new javafx.collections.transformation.FilteredList<>(contractsMasterList, p -> true);
        contractsListView.setItems(filteredData);

        // Multi-filter logic
        java.util.function.BiConsumer<String, String> applyFilters = (search, status) -> {
            filteredData.setPredicate(c -> {
                boolean matchesSearch = true;
                if (search != null && !search.isEmpty()) {
                    String lower = search.toLowerCase();
                    matchesSearch = safeText(c.getContractNumber()).toLowerCase().contains(lower) ||
                                  safeText(c.getTitle()).toLowerCase().contains(lower);
                }

                boolean matchesStatus = true;
                if (status != null && !"All Statuses".equals(status)) {
                    matchesStatus = status.equalsIgnoreCase(c.getStatus());
                }

                return matchesSearch && matchesStatus;
            });
        };

        contractSearchField.textProperty().addListener((obs, old, val) -> applyFilters.accept(val, statusFilter.getValue()));
        statusFilter.valueProperty().addListener((obs, old, val) -> applyFilters.accept(contractSearchField.getText(), val));
    }

    private Node createContractCell(Contract c) {
        HBox cell = new HBox(15);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setPadding(new Insets(15, 0, 15, 0));
        cell.getStyleClass().add("list-row");

        // Contract Info
        VBox info = new VBox(2);
        info.setPrefWidth(285);
        Label title = new Label(safeText(c.getContractNumber()));
        title.getStyleClass().add("card-title");
        title.setStyle("-fx-font-size: 14px;");
        
        String partnerName = "-";
        String domain = "E-commerce";
        if (partnersList != null) {
            for (Collaborator partner : partnersList) {
                if (partner.getId() == c.getCollaboratorId()) {
                    partnerName = partner.getCompanyName();
                    domain = partner.getDomain();
                    break;
                }
            }
        }
        Label partner = new Label("Partner : " + partnerName);
        partner.getStyleClass().add("card-subtitle");
        info.getChildren().addAll(title, partner);

        // Domain
        Label domainLabel = new Label(domain);
        domainLabel.setPrefWidth(200);
        domainLabel.getStyleClass().add("form-label");
        domainLabel.setStyle("-fx-font-size: 13px;");

        // Status Badge
        Label statusBadge = new Label(safeText(c.getStatus()).toUpperCase());
        statusBadge.getStyleClass().add("status-badge");
        String status = c.getStatus().toUpperCase();
        if ("ACTIVE".equalsIgnoreCase(status)) {
            statusBadge.getStyleClass().add("status-active");
        } else if ("DRAFT".equalsIgnoreCase(status) || "SIGNED_BY_COLLABORATOR".equalsIgnoreCase(status)) {
            statusBadge.getStyleClass().add("status-pending");
        } else if ("TERMINATED".equalsIgnoreCase(status)) {
            statusBadge.getStyleClass().add("status-rejected");
        } else {
            statusBadge.setStyle("-fx-background-color: #94a3b8;");
        }
        
        statusBadge.setMinWidth(110);
        statusBadge.setAlignment(Pos.CENTER);

        // Effective Date
        Label dateLabel = new Label(c.getStartDate() != null ? c.getStartDate().toString().split(" ")[0] : "24/04/2026");
        dateLabel.setPrefWidth(200);
        dateLabel.getStyleClass().add("card-subtitle");
        dateLabel.setStyle("-fx-font-size: 13px;");

        // Actions
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Hyperlink viewBtn = new Hyperlink("View");
        viewBtn.getStyleClass().add("action-link");
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
