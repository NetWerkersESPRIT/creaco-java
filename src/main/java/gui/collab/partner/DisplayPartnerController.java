package gui.collab.partner;

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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import services.CollaboratorService;

public class DisplayPartnerController {

    @FXML private ListView<Collaborator> partnersListView;
    @FXML private TextField partnerSearchField;
    @FXML private StackPane topAreaContainer;
    @FXML private VBox bannerView;

    private final CollaboratorService collaboratorService = new CollaboratorService();
    private ObservableList<Collaborator> partnersMasterList = FXCollections.observableArrayList();

    // Callbacks for navigation
    private Runnable onAddRequested;
    private java.util.function.Consumer<Collaborator> onEditRequested;
    private java.util.function.Consumer<Collaborator> onViewRequested;

    public void setOnAddRequested(Runnable callback) { this.onAddRequested = callback; }
    public void setOnEditRequested(java.util.function.Consumer<Collaborator> callback) { this.onEditRequested = callback; }
    public void setOnViewRequested(java.util.function.Consumer<Collaborator> callback) { this.onViewRequested = callback; }

    @FXML
    public void initialize() {
        setupPartnersView();
        loadPartnersData();
    }

    private void setupPartnersView() {
        partnersListView.setCellFactory(param -> new ListCell<Collaborator>() {
            @Override
            protected void updateItem(Collaborator item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setGraphic(createPartnerCell(item));
                }
            }
        });

        FilteredList<Collaborator> filteredData = new FilteredList<>(partnersMasterList, p -> true);
        partnersListView.setItems(filteredData);
        partnerSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(collab -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lower = newValue.toLowerCase();
                return safeText(collab.getCompanyName()).toLowerCase().contains(lower) ||
                        safeText(collab.getName()).toLowerCase().contains(lower) ||
                        safeText(collab.getEmail()).toLowerCase().contains(lower) ||
                        safeText(collab.getDomain()).toLowerCase().contains(lower);
            });
        });
    }

    private Node createPartnerCell(Collaborator collab) {
        HBox cell = new HBox(15);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setPadding(new Insets(15, 0, 15, 0));
        cell.getStyleClass().add("list-row");

        // Company Icon Box
        VBox iconBox = new VBox();
        iconBox.setAlignment(Pos.CENTER);
        iconBox.setMinSize(45, 45);
        iconBox.setMaxSize(45, 45);
        iconBox.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 12;");
        Label iconLabel = new Label("🏢");
        iconLabel.setStyle("-fx-font-size: 22px;");
        iconBox.getChildren().add(iconLabel);

        // Collaborator Info
        VBox info = new VBox(2);
        info.setPrefWidth(245);
        Label title = new Label(safeText(collab.getCompanyName()));
        title.getStyleClass().add("card-title");
        title.setStyle("-fx-font-size: 14px;"); // Keep size slightly smaller for list
        Label email = new Label(safeText(collab.getEmail()));
        email.getStyleClass().add("card-subtitle");
        info.getChildren().addAll(title, email);

        // Domain
        Label domain = new Label(safeText(collab.getDomain()));
        domain.setPrefWidth(200);
        domain.getStyleClass().add("card-subtitle");
        domain.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px;");

        // Status Badge
        Label statusBadge = new Label(safeText(collab.getStatus()).toUpperCase());
        statusBadge.getStyleClass().add("status-badge");
        if ("ACTIVE".equalsIgnoreCase(collab.getStatus())) {
            statusBadge.getStyleClass().add("status-active");
        } else if ("PENDING".equalsIgnoreCase(collab.getStatus())) {
            statusBadge.getStyleClass().add("status-pending");
        } else {
            statusBadge.getStyleClass().add("status-rejected");
        }
        statusBadge.setMinWidth(90);
        statusBadge.setAlignment(Pos.CENTER);

        // Date
        String dateStr = collab.getCreatedAt() != null ? collab.getCreatedAt().toString().split(" ")[0] : "14/04/2026";
        Label dateLabel = new Label(dateStr);
        dateLabel.setPrefWidth(150);
        dateLabel.getStyleClass().add("card-subtitle");

        // Actions
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Hyperlink viewBtn = new Hyperlink("View");
        viewBtn.getStyleClass().add("action-link");
        viewBtn.setOnAction(e -> onViewPartner(collab));

        Hyperlink editBtn = new Hyperlink("Edit");
        editBtn.getStyleClass().add("action-link");
        editBtn.setOnAction(e -> { if(onEditRequested != null) onEditRequested.accept(collab); });

        actions.getChildren().addAll(viewBtn, editBtn);

        cell.getChildren().addAll(iconBox, info, domain, statusBadge, dateLabel, spacer, actions);
        return cell;
    }

    public void loadPartnersData() {
        try {
            partnersMasterList.setAll(collaboratorService.afficher());
        } catch (Exception e) {
            System.err.println("Error loading partners: " + e.getMessage());
        }
    }

    @FXML
    private void onAddPartner() {
        if (onAddRequested != null) onAddRequested.run();
    }

    private void onViewPartner(Collaborator collab) {
        if (onViewRequested != null) {
            onViewRequested.accept(collab);
        } else {
            // Fallback: This part was old logic, better handled by the callback
            System.out.println("No view callback set for " + collab.getCompanyName());
        }
    }

    private void onDeletePartner(Collaborator collab) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setContentText("Are you sure you want to delete this partner?");
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    collaboratorService.supprimer(collab.getId());
                    loadPartnersData();
                } catch (Exception e) {
                    showAlert("Delete Error", "Could not delete partner: " + e.getMessage());
                }
            }
        });
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
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
