package gui.collab.manager;

import entities.Contract;
import entities.Users;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import services.CollaboratorService;
import services.ContractService;
import utils.SessionManager;
import java.util.List;

public class ContractListController {

    @FXML private ListView<Contract> contractListView;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;

    private final ContractService contractService = new ContractService();
    private final CollaboratorService partnerService = new CollaboratorService();
    
    private ObservableList<Contract> masterData = FXCollections.observableArrayList();
    private java.util.function.Consumer<Contract> onConsultRequested;

    public void setOnConsultRequested(java.util.function.Consumer<Contract> callback) {
        this.onConsultRequested = callback;
    }

    @FXML
    public void initialize() {
        statusFilter.setItems(FXCollections.observableArrayList("All Statuses", "DRAFT", "SENT_TO_PARTNER", "SIGNED", "ACTIVE"));
        statusFilter.setValue("All Statuses");
        
        contractListView.setCellFactory(lv -> new ContractListCell());
        loadData();
        setupFiltering();
    }

    private void loadData() {
        try {
            Users manager = SessionManager.getInstance().getCurrentUser();
            if (manager == null) return;
            
            List<Contract> contracts = contractService.afficherByManager(manager.getId());
            masterData.setAll(contracts);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupFiltering() {
        FilteredList<Contract> filteredData = new FilteredList<>(masterData, p -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            updateFilter(filteredData, newVal, statusFilter.getValue());
        });

        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateFilter(filteredData, searchField.getText(), newVal);
        });

        contractListView.setItems(filteredData);
    }

    private void updateFilter(FilteredList<Contract> filteredData, String searchText, String status) {
        filteredData.setPredicate(c -> {
            boolean matchesSearch = true;
            if (searchText != null && !searchText.isEmpty()) {
                String lower = searchText.toLowerCase();
                matchesSearch = c.getContractNumber().toLowerCase().contains(lower) || 
                                c.getTitle().toLowerCase().contains(lower);
            }

            boolean matchesStatus = true;
            if (status != null && !status.equals("All Statuses")) {
                matchesStatus = c.getStatus().equalsIgnoreCase(status);
            }

            return matchesSearch && matchesStatus;
        });
    }

    private class ContractListCell extends ListCell<Contract> {
        private final HBox root = new HBox(20);
        private final Label refLabel = new Label();
        private final Label projectLabel = new Label();
        private final Label partnerLabel = new Label();
        private final Label amountLabel = new Label();
        private final Label statusBadge = new Label();
        private final Hyperlink consultLink = new Hyperlink("PREVIEW →");

        public ContractListCell() {
            root.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            root.setPadding(new Insets(15, 20, 15, 20));
            root.getStyleClass().add("list-row");

            refLabel.setPrefWidth(180);
            refLabel.setStyle("-fx-font-family: 'Monospaced'; -fx-font-weight: bold; -fx-text-fill: #64748b;");

            VBox projectBox = new VBox(2);
            projectBox.setPrefWidth(250);
            projectLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");
            partnerLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
            projectBox.getChildren().addAll(projectLabel, partnerLabel);

            amountLabel.setPrefWidth(150);
            amountLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");

            StackPane badgeContainer = new StackPane(statusBadge);
            badgeContainer.setPrefWidth(150);
            badgeContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            statusBadge.getStyleClass().add("status-badge");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            consultLink.getStyleClass().add("review-link");
            consultLink.setOnAction(e -> {
                if (onConsultRequested != null) onConsultRequested.accept(getItem());
            });

            root.getChildren().addAll(refLabel, projectBox, amountLabel, badgeContainer, spacer, consultLink);
        }

        @Override
        protected void updateItem(Contract item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                refLabel.setText(item.getContractNumber());
                projectLabel.setText(item.getTitle());
                amountLabel.setText(item.getAmount() != null ? String.format("%,.2f DT", item.getAmount()) : "-");
                
                statusBadge.setText(item.getStatus().toUpperCase());
                statusBadge.getStyleClass().removeAll("status-active", "status-pending", "status-rejected");
                if ("ACTIVE".equalsIgnoreCase(item.getStatus()) || "SIGNED".equalsIgnoreCase(item.getStatus())) 
                    statusBadge.getStyleClass().add("status-active");
                else if ("DRAFT".equalsIgnoreCase(item.getStatus()) || "SENT_TO_PARTNER".equalsIgnoreCase(item.getStatus())) 
                    statusBadge.getStyleClass().add("status-pending");
                else statusBadge.getStyleClass().add("status-rejected");

                try {
                    partnerLabel.setText(partnerService.getById(item.getCollaboratorId()).getCompanyName());
                } catch (Exception e) {
                    partnerLabel.setText("Unknown Partner");
                }

                setGraphic(root);
            }
        }
    }
}
