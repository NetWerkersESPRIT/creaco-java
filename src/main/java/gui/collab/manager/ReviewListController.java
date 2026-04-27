package gui.collab.manager;

import entities.CollabRequest;
import entities.Users;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import services.CollaboratorService;
import services.CollabRequestService;
import services.UserService;
import utils.SessionManager;
import java.text.SimpleDateFormat;
import java.util.List;

public class ReviewListController {

    @FXML private ListView<CollabRequest> reviewListView;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;

    private final CollabRequestService requestService = new CollabRequestService();
    private final CollaboratorService partnerService = new CollaboratorService();
    private final UserService userService = new UserService();
    
    private ObservableList<CollabRequest> masterData = FXCollections.observableArrayList();
    private java.util.function.Consumer<CollabRequest> onReviewRequested;

    public void setOnReviewRequested(java.util.function.Consumer<CollabRequest> callback) {
        this.onReviewRequested = callback;
    }

    @FXML
    public void initialize() {
        statusFilter.setItems(FXCollections.observableArrayList("All Statuses", "PENDING", "APPROVED", "REJECTED"));
        statusFilter.setValue("All Statuses");
        
        reviewListView.setCellFactory(lv -> new ReviewListCell());
        loadData();
        setupFiltering();
    }

    private void loadData() {
        try {
            Users manager = SessionManager.getInstance().getCurrentUser();
            if (manager == null) return;
            
            List<CollabRequest> requests = requestService.afficherByManager(manager.getId());
            masterData.setAll(requests);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupFiltering() {
        FilteredList<CollabRequest> filteredData = new FilteredList<>(masterData, p -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            updateFilter(filteredData, newVal, statusFilter.getValue());
        });

        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateFilter(filteredData, searchField.getText(), newVal);
        });

        reviewListView.setItems(filteredData);
    }

    private void updateFilter(FilteredList<CollabRequest> filteredData, String searchText, String status) {
        filteredData.setPredicate(req -> {
            boolean matchesSearch = true;
            if (searchText != null && !searchText.isEmpty()) {
                String lower = searchText.toLowerCase();
                matchesSearch = req.getTitle().toLowerCase().contains(lower);
                // Could add partner/creator search here too if services allow
            }

            boolean matchesStatus = true;
            if (status != null && !status.equals("All Statuses")) {
                matchesStatus = req.getStatus().equalsIgnoreCase(status);
            }

            return matchesSearch && matchesStatus;
        });
    }

    private class ReviewListCell extends ListCell<CollabRequest> {
        private final HBox root = new HBox(20);
        private final Label projectLabel = new Label();
        private final Label partnerLabel = new Label();
        private final Label creatorLabel = new Label();
        private final Label creatorRoleLabel = new Label();
        private final Label budgetLabel = new Label();
        private final Label statusBadge = new Label();
        private final Label dateLabel = new Label();
        private final Hyperlink reviewLink = new Hyperlink("REVIEW →");
        private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        public ReviewListCell() {
            root.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            root.setPadding(new Insets(15, 20, 15, 20));
            root.getStyleClass().add("list-row");

            VBox projectBox = new VBox(2);
            projectBox.setPrefWidth(250);
            projectLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");
            partnerLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
            projectBox.getChildren().addAll(projectLabel, partnerLabel);

            VBox creatorBox = new VBox(2);
            creatorBox.setPrefWidth(200);
            creatorLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");
            creatorRoleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
            creatorBox.getChildren().addAll(creatorLabel, creatorRoleLabel);

            budgetLabel.setPrefWidth(150);
            budgetLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");

            StackPane badgeContainer = new StackPane(statusBadge);
            badgeContainer.setPrefWidth(150);
            badgeContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            statusBadge.getStyleClass().add("status-badge");

            dateLabel.setPrefWidth(120);
            dateLabel.setStyle("-fx-text-fill: #64748b;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            reviewLink.getStyleClass().add("review-link");
            reviewLink.setOnAction(e -> {
                if (onReviewRequested != null) onReviewRequested.accept(getItem());
            });

            root.getChildren().addAll(projectBox, creatorBox, budgetLabel, badgeContainer, dateLabel, spacer, reviewLink);
        }

        @Override
        protected void updateItem(CollabRequest item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                projectLabel.setText(item.getTitle());
                budgetLabel.setText(item.getBudget() != null ? String.format("%,.2f DT", item.getBudget()) : "-");
                dateLabel.setText(item.getCreatedAt() != null ? df.format(item.getCreatedAt()) : "-");
                
                statusBadge.setText(item.getStatus().toUpperCase());
                statusBadge.getStyleClass().removeAll("status-active", "status-pending", "status-rejected");
                if ("APPROVED".equalsIgnoreCase(item.getStatus())) statusBadge.getStyleClass().add("status-active");
                else if ("PENDING".equalsIgnoreCase(item.getStatus())) statusBadge.getStyleClass().add("status-pending");
                else statusBadge.getStyleClass().add("status-rejected");

                // Background loading for partner/creator
                try {
                    partnerLabel.setText(partnerService.getById(item.getCollaboratorId()).getCompanyName());
                    Users creator = userService.getUserById(item.getCreatorId());
                    creatorLabel.setText(creator.getUsername());
                    creatorRoleLabel.setText("Content Creator");
                } catch (Exception e) {
                    partnerLabel.setText("Unknown Partner");
                    creatorLabel.setText("Unknown Creator");
                }

                setGraphic(root);
            }
        }
    }
}
