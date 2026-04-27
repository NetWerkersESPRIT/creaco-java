package gui.collab.admin.partner;

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
import services.CollaboratorService;
import java.io.IOException;
import java.sql.SQLException;

public class AdminPartnerListController {

    @FXML private ListView<Collaborator> partnersListView;
    @FXML private TextField searchField;

    private final CollaboratorService collaboratorService = new CollaboratorService();
    private ObservableList<Collaborator> partnersMasterList = FXCollections.observableArrayList();

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
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(collab -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lower = newValue.toLowerCase();
                return (collab.getCompanyName() != null && collab.getCompanyName().toLowerCase().contains(lower)) ||
                        (collab.getName() != null && collab.getName().toLowerCase().contains(lower)) ||
                        (collab.getEmail() != null && collab.getEmail().toLowerCase().contains(lower)) ||
                        (collab.getDomain() != null && collab.getDomain().toLowerCase().contains(lower));
            });
        });
    }

    private Node createPartnerCell(Collaborator collab) {
        HBox cell = new HBox(15);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setPadding(new Insets(15, 0, 15, 0));
        cell.getStyleClass().add("list-row");

        // Company Avatar
        VBox avatarBox = new VBox();
        avatarBox.setAlignment(Pos.CENTER);
        avatarBox.setMinSize(50, 50);
        avatarBox.setMaxSize(50, 50);
        avatarBox.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 14; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.03), 5, 0, 0, 1);");
        Label iconLabel = new Label(collab.getCompanyName().substring(0, 1).toUpperCase());
        iconLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #6366f1;");
        avatarBox.getChildren().add(iconLabel);

        // Info
        VBox info = new VBox(2);
        info.setPrefWidth(255);
        Label title = new Label(collab.getCompanyName());
        title.getStyleClass().add("list-item-title");
        Label email = new Label(collab.getEmail());
        email.getStyleClass().add("list-item-subtitle");
        info.getChildren().addAll(title, email);

        // Domain
        Label domain = new Label(collab.getDomain());
        domain.setPrefWidth(220);
        domain.getStyleClass().add("list-item-subtitle");
        domain.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold;");

        // Status Badge
        Label statusBadge = new Label(collab.getStatus().toUpperCase());
        statusBadge.getStyleClass().add("status-pill");
        if ("ACTIVE".equalsIgnoreCase(collab.getStatus())) {
            statusBadge.getStyleClass().add("stat-badge-green");
        } else {
            statusBadge.getStyleClass().add("stat-badge-gray");
        }
        statusBadge.setMinWidth(100);
        statusBadge.setAlignment(Pos.CENTER);

        // Created At
        String dateStr = collab.getCreatedAt() != null ? collab.getCreatedAt().toString().split(" ")[0] : "N/A";
        Label dateLabel = new Label(dateStr);
        dateLabel.setPrefWidth(180);
        dateLabel.getStyleClass().add("list-item-subtitle");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Admin Actions
        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button viewBtn = createIconButton("👁", "#6366f1", "View Details");
        viewBtn.setOnAction(e -> onViewPartner(collab));

        boolean isActive = "ACTIVE".equalsIgnoreCase(collab.getStatus());
        Button toggleBtn = createIconButton(isActive ? "🚫" : "✅", isActive ? "#ef4444" : "#10b981", isActive ? "Deactivate Partner" : "Activate Partner");
        toggleBtn.setOnAction(e -> onToggleStatus(collab));

        Button editBtn = createIconButton("✎", "#f59e0b", "Edit Partner");
        editBtn.setOnAction(e -> onEditPartner(collab));

        actions.getChildren().addAll(viewBtn, toggleBtn, editBtn);

        cell.getChildren().addAll(avatarBox, info, domain, statusBadge, dateLabel, spacer, actions);
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

    public void loadPartnersData() {
        try {
            partnersMasterList.setAll(collaboratorService.afficher());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onAddPartner() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/collab/partner/addPartner.fxml"));
            Parent root = loader.load();
            gui.collab.partner.AddPartnerController controller = loader.getController();
            controller.setOnCancel(this::refreshAndReturn);
            controller.setOnSave(this::refreshAndReturn);
            
            replaceContent(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onViewPartner(Collaborator collab) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/collab/partner/viewPartner.fxml"));
            Parent root = loader.load();
            gui.collab.partner.ViewPartnerController controller = loader.getController();
            controller.setPartner(collab);
            controller.setCallbacks(
                () -> onEditPartner(collab),
                () -> refreshAndReturn(),
                () -> {} // New request not needed here
            );
            
            replaceContent(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onEditPartner(Collaborator collab) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/collab/partner/updatePartner.fxml"));
            Parent root = loader.load();
            gui.collab.partner.UpdatePartnerController controller = loader.getController();
            controller.setPartner(collab);
            controller.setOnCancel(this::refreshAndReturn);
            controller.setOnSave(this::refreshAndReturn);
            
            replaceContent(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onToggleStatus(Collaborator collab) {
        String newStatus = "ACTIVE".equalsIgnoreCase(collab.getStatus()) ? "INACTIVE" : "ACTIVE";
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Status Update");
        confirm.setHeaderText(newStatus.equals("ACTIVE") ? "Reactivate Partner?" : "Deactivate Partner?");
        confirm.setContentText("Are you sure you want to change the status of " + collab.getCompanyName() + " to " + newStatus + "?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    collab.setStatus(newStatus);
                    collaboratorService.modifier(collab.getId(), collab);
                    loadPartnersData();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void refreshAndReturn() {
        loadPartnersData();
        // Since we are in the same view context or replacing content, just return to list
        // If we replaced content, we need to go back to this FXML
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/collab/admin/partner/admin_partner_list.fxml"));
            Parent root = loader.load();
            replaceContent(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private javafx.scene.layout.StackPane storedContentArea;

    private void replaceContent(Parent root) {
        if (storedContentArea == null && partnersListView != null && partnersListView.getScene() != null) {
            storedContentArea = (javafx.scene.layout.StackPane) partnersListView.getScene().lookup("#contentArea");
        }
        
        if (storedContentArea != null) {
            storedContentArea.getChildren().setAll(root);
        } else {
            System.err.println("Could not find #contentArea to replace content.");
        }
    }
}
