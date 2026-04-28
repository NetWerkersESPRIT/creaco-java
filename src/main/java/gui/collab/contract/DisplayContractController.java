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
import java.util.function.Consumer;

public class DisplayContractController {

    @FXML private ListView<Contract> contractsListView;
    @FXML private TextField contractSearchField;
    @FXML private ComboBox<String> statusFilter;

    private final ContractService contractService = new ContractService();
    private final CollaboratorService collaboratorService = new CollaboratorService();
    private ObservableList<Contract> contractsMasterList = FXCollections.observableArrayList();
    private List<Collaborator> partnersList;

    private Consumer<Contract> onViewRequested;

    public void setOnViewRequested(Consumer<Contract> callback) { 
        this.onViewRequested = callback; 
    }

    @FXML
    public void initialize() {
        setupContractsView();
        loadContractsData();
        
        statusFilter.setItems(FXCollections.observableArrayList("All Statuses", "DRAFT", "SENT_TO_PARTNER", "SIGNED", "TERMINATED"));
        statusFilter.setValue("All Statuses");
    }

    private void setupContractsView() {
        contractsListView.setCellFactory(param -> new ListCell<Contract>() {
            @Override
            protected void updateItem(Contract item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
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

        // Filter Logic
        statusFilter.valueProperty().addListener((obs, old, val) -> applyFilters(filteredData));
        contractSearchField.textProperty().addListener((obs, old, val) -> applyFilters(filteredData));
    }

    private void applyFilters(javafx.collections.transformation.FilteredList<Contract> filteredData) {
        String search = contractSearchField.getText();
        String statusStr = statusFilter.getValue();
        
        filteredData.setPredicate(c -> {
            boolean matchesSearch = true;
            if (search != null && !search.isEmpty()) {
                String lower = search.toLowerCase();
                matchesSearch = safeText(c.getContractNumber()).toLowerCase().contains(lower) ||
                              safeText(c.getTitle()).toLowerCase().contains(lower);
            }
            boolean matchesStatus = true;
            if (statusStr != null && !"All Statuses".equals(statusStr)) {
                matchesStatus = statusStr.equalsIgnoreCase(c.getStatus());
            }
            return matchesSearch && matchesStatus;
        });
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
        
        String partnerName = "Unknown Partner";
        String domain = "Collaboration";
        if (partnersList != null) {
            for (Collaborator p : partnersList) {
                if (p.getId() == c.getCollaboratorId()) {
                    partnerName = p.getCompanyName();
                    domain = p.getDomain();
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
        String statusStr = c.getStatus() != null ? c.getStatus().toUpperCase() : "DRAFT";
        Label statusBadge = new Label(statusStr);
        statusBadge.getStyleClass().add("status-badge");
        
        if ("SIGNED".equals(statusStr)) {
            statusBadge.setStyle("-fx-background-color: #f0fdf4; -fx-text-fill: #166534;");
        } else if ("SENT_TO_PARTNER".equals(statusStr)) {
            statusBadge.setStyle("-fx-background-color: #fef9c3; -fx-text-fill: #854d0e;");
        } else {
            statusBadge.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569;");
        }
        
        statusBadge.setMinWidth(130);
        statusBadge.setAlignment(Pos.CENTER);

        // Date
        String dateStr = c.getStartDate() != null ? c.getStartDate().toString() : "N/A";
        Label dateLabel = new Label(dateStr);
        dateLabel.setPrefWidth(180);
        dateLabel.getStyleClass().add("card-subtitle");

        // Actions
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button viewBtn = new Button("View");
        viewBtn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #7c3aed; -fx-font-weight: bold; -fx-cursor: hand;");
        viewBtn.setOnAction(e -> {
            if (onViewRequested != null) {
                System.out.println("DisplayContractController: View requested for contract " + c.getContractNumber());
                onViewRequested.accept(c);
            } else {
                System.err.println("DisplayContractController: onViewRequested callback is NULL!");
            }
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
