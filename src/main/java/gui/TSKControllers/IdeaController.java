package gui.TSKControllers;

import services.IdeaService;
import utils.SessionManager;
import entities.Idea;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.Pos;
import java.sql.SQLException;

public class IdeaController {
    @FXML private Button btnAdmin;
    @FXML private VBox ideasList;
    @FXML private Label lblNavUsername;
    @FXML private Label lblNavUserRole;

    private final IdeaService ideaService = new IdeaService();

    @FXML
    public void initialize() {
        // Hide Admin button if not admin
        boolean isAdmin = SessionManager.getInstance().isAdmin();
        if (btnAdmin != null) {
            btnAdmin.setVisible(isAdmin);
            btnAdmin.setManaged(isAdmin);
        }

        // Populate Navbar Profile
        entities.Users current = SessionManager.getInstance().getCurrentUser();
        if (current != null && lblNavUsername != null) {
            lblNavUsername.setText(current.getUsername());
            String role = current.getRole() != null ? current.getRole().replace("ROLE_", "") : "USER";
            lblNavUserRole.setText(role);
        }

        loadIdeas();
    }

    @FXML public void goToAdmin()   throws Exception { switchScene("/Users/Admin.fxml"); }
    @FXML public void goToIdea()    throws Exception { switchScene("/TSK/Idea.fxml"); }
    @FXML public void goToMission() throws Exception { switchScene("/TSK/Mission.fxml"); }
    @FXML public void goToTasks()   throws Exception { switchScene("/TSK/Tasks.fxml"); }

    private void switchScene(String fxml) throws Exception {
        StackPane contentArea = (StackPane) ideasList.getScene().lookup("#contentArea");
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        javafx.scene.Parent root = loader.load();
        javafx.scene.Node view = root;

        if (root instanceof BorderPane) {
            view = ((BorderPane) root).getCenter();
        }

        if (contentArea != null) {
            contentArea.getChildren().setAll(view);
        } else {
            javafx.stage.Stage stage = (javafx.stage.Stage) ideasList.getScene().getWindow();
            stage.getScene().setRoot(root);
        }
    }

    private void loadIdeas() {
        try {
            ideasList.getChildren().clear();
            for (Idea i : ideaService.afficher()) {
                ideasList.getChildren().add(buildIdeaRow(i));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private HBox buildIdeaRow(Idea i) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 15; -fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.02), 10, 0, 0, 2); -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");
        
        // Idea Info
        VBox ideaInfo = new VBox(5);
        ideaInfo.setPrefWidth(300);
        Label lblTitle = new Label(i.getTitle());
        lblTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-font-size: 14px;");
        Label lblId = new Label("#" + i.getId());
        lblId.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
        ideaInfo.getChildren().addAll(lblTitle, lblId);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Category Badge
        Label lblCategory = new Label(i.getCategory() != null ? i.getCategory().toUpperCase() : "GENERAL");
        lblCategory.setStyle("-fx-background-color: #ec489915; -fx-text-fill: #ec4899; -fx-padding: 5 12; -fx-background-radius: 10; -fx-font-weight: bold; -fx-font-size: 10px;");
        lblCategory.setMinWidth(150);
        lblCategory.setAlignment(Pos.CENTER);

        // Actions
        HBox actions = new HBox(10);
        actions.setPrefWidth(150);
        actions.setAlignment(Pos.CENTER_RIGHT);
        
        Button btnDelete = new Button("🗑");
        btnDelete.setStyle("-fx-background-color: #ef444415; -fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
        btnDelete.setOnAction(e -> {
            if (gui.util.AlertHelper.showCustomAlert("Delete Idea?", "Are you sure you want to delete this idea?", gui.util.AlertHelper.AlertType.CONFIRMATION)) {
                try {
                    ideaService.supprimer(i.getId());
                    loadIdeas();
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });
        
        actions.getChildren().add(btnDelete);

        row.getChildren().addAll(ideaInfo, spacer, lblCategory, actions);

        row.setOnMouseEntered(e -> row.setStyle("-fx-padding: 15; -fx-background-color: #f8fafc; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-padding: 15; -fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;"));

        return row;
    }

    @FXML
    public void handleAddIdea() throws Exception {
        StackPane contentArea = (StackPane) ideasList.getScene().lookup("#contentArea");
        if (contentArea != null) {
            contentArea.getChildren().setAll((javafx.scene.Node) FXMLLoader.load(getClass().getResource("/TSK/AddIdea.fxml")));
        } else {
            javafx.stage.Stage stage = (javafx.stage.Stage) ideasList.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(FXMLLoader.load(getClass().getResource("/TSK/AddIdea.fxml"))));
        }
    }

    @FXML
    public void goBack() throws Exception {
        javafx.stage.Stage stage = (javafx.stage.Stage) ideasList.getScene().getWindow();
        stage.setScene(new javafx.scene.Scene(FXMLLoader.load(getClass().getResource("/gui/front-main-view.fxml"))));
    }

    @FXML
    public void logout(javafx.event.ActionEvent event) {
        gui.SessionHelper.logout(event);
    }
}
