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
        cell.setPadding(new Insets(12, 0, 12, 0));
        cell.getStyleClass().add("list-row");

        // Company Icon Box
        VBox iconBox = new VBox();
        iconBox.setAlignment(Pos.CENTER);
        iconBox.setMinSize(42, 42);
        iconBox.setMaxSize(42, 42);
        iconBox.setStyle("-fx-background-color: #cbd5e1; -fx-background-radius: 12;");
        Label iconLabel = new Label("🏢");
        iconLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: white;");
        iconBox.getChildren().add(iconLabel);

        // Collaborator Info
        VBox info = new VBox(2);
        info.setPrefWidth(245);
        Label title = new Label(safeText(collab.getCompanyName()));
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1e293b;");
        Label email = new Label(safeText(collab.getEmail()));
        email.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
        info.getChildren().addAll(title, email);

        // Domain
        Label domain = new Label(safeText(collab.getDomain()));
        domain.setPrefWidth(200);
        domain.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px;");

        // Status Badge
        Label statusBadge = new Label(safeText(collab.getStatus()).toUpperCase());
        statusBadge.getStyleClass().add("status-badge");
        if ("ACTIVE".equalsIgnoreCase(collab.getStatus())) {
            statusBadge.getStyleClass().add("status-active");
        } else {
            statusBadge.setStyle("-fx-background-color: #94a3b8;");
        }
        statusBadge.setMinWidth(85);
        statusBadge.setAlignment(Pos.CENTER);

        // Date
        String dateStr = collab.getCreatedAt() != null ? collab.getCreatedAt().toString().split(" ")[0] : "14/04/2026";
        Label dateLabel = new Label(dateStr);
        dateLabel.setPrefWidth(150);
        dateLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");

        // Actions
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Hyperlink viewBtn = new Hyperlink("👁 View");
        viewBtn.getStyleClass().add("action-link");
        viewBtn.setOnAction(e -> onViewPartner(collab));

        Label divider = new Label("/");
        divider.setStyle("-fx-text-fill: #cbd5e1;");

        Hyperlink editBtn = new Hyperlink("📝 Edit");
        editBtn.getStyleClass().add("action-link");
        editBtn.setOnAction(e -> { if(onEditRequested != null) onEditRequested.accept(collab); });

        actions.getChildren().addAll(viewBtn, divider, editBtn);

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
