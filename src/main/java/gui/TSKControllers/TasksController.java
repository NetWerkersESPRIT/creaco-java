package gui.TSKControllers;

import database.SessionManager;
import entities.Tasks;
import services.TskService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.Pos;
import java.sql.SQLException;

public class TasksController {
    @FXML private Button btnAdmin;
    @FXML private VBox tasksList;
    @FXML private Label lblNavUsername;
    @FXML private Label lblNavUserRole;

    private final TskService tskService = new TskService();

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

        loadTasks();
    }

    @FXML public void goToAdmin()   throws Exception { switchScene("/Users/Admin.fxml"); }
    @FXML public void goToIdea()    throws Exception { switchScene("/TSK/Idea.fxml"); }
    @FXML public void goToMission() throws Exception { switchScene("/TSK/Mission.fxml"); }
    @FXML public void goToTasks()   throws Exception { switchScene("/TSK/Tasks.fxml"); }

    private void switchScene(String fxml) throws Exception {
        StackPane contentArea = (StackPane) tasksList.getScene().lookup("#contentArea");
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        javafx.scene.Parent root = loader.load();
        javafx.scene.Node view = root;

        if (root instanceof BorderPane) {
            view = ((BorderPane) root).getCenter();
        }

        if (contentArea != null) {
            contentArea.getChildren().setAll(view);
        } else {
            javafx.stage.Stage stage = (javafx.stage.Stage) tasksList.getScene().getWindow();
            stage.getScene().setRoot(root);
        }
    }

    private void loadTasks() {
        try {
            tasksList.getChildren().clear();
            for (Tasks t : tskService.afficher()) {
                tasksList.getChildren().add(buildTaskRow(t));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private HBox buildTaskRow(Tasks t) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 15; -fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.02), 10, 0, 0, 2); -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");
        
        // Task Info
        VBox taskInfo = new VBox(5);
        taskInfo.setPrefWidth(300);
        Label lblTitle = new Label(t.getTitle());
        lblTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-font-size: 14px;");
        Label lblId = new Label("#" + t.getId());
        lblId.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
        taskInfo.getChildren().addAll(lblTitle, lblId);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Status Badge
        Label lblStatus = new Label(t.getState() != null ? t.getState().toUpperCase() : "PENDING");
        String statusColor = "#94a3b8"; // Default gray
        if ("COMPLETED".equalsIgnoreCase(t.getState())) statusColor = "#10b981";
        else if ("IN_PROGRESS".equalsIgnoreCase(t.getState())) statusColor = "#3b82f6";
        else if ("TODO".equalsIgnoreCase(t.getState())) statusColor = "#f59e0b";
        
        lblStatus.setStyle("-fx-background-color: " + statusColor + "15; -fx-text-fill: " + statusColor + "; -fx-padding: 5 12; -fx-background-radius: 10; -fx-font-weight: bold; -fx-font-size: 10px;");
        lblStatus.setMinWidth(120);
        lblStatus.setAlignment(Pos.CENTER);

        // Deadline
        Label lblDeadline = new Label(t.getTime_limit() != null ? t.getTime_limit() : "No Deadline");
        lblDeadline.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");
        lblDeadline.setMinWidth(150);
        lblDeadline.setAlignment(Pos.CENTER);

        // Actions
        HBox actions = new HBox(10);
        actions.setPrefWidth(200);
        actions.setAlignment(Pos.CENTER_RIGHT);
        
        Button btnEdit = new Button("✎");
        btnEdit.setStyle("-fx-background-color: #3b82f615; -fx-text-fill: #3b82f6; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
        btnEdit.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/TSK/EditTask.fxml"));
                javafx.scene.Node root = loader.load();
                EditTaskController ctrl = loader.getController();
                ctrl.setTask(t);
                
                StackPane contentArea = (StackPane) tasksList.getScene().lookup("#contentArea");
                if (contentArea != null) contentArea.getChildren().setAll(root);
            } catch (Exception ex) { ex.printStackTrace(); }
        });
        
        Button btnDelete = new Button("🗑");
        btnDelete.setStyle("-fx-background-color: #ef444415; -fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
        btnDelete.setOnAction(e -> {
            if (gui.util.AlertHelper.showCustomAlert("Delete Task?", "Are you sure you want to delete this task?", gui.util.AlertHelper.AlertType.CONFIRMATION)) {
                try {
                    tskService.supprimer(t.getId());
                    loadTasks();
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });
        
        actions.getChildren().addAll(btnEdit, btnDelete);

        row.getChildren().addAll(taskInfo, spacer, lblStatus, lblDeadline, actions);

        row.setOnMouseEntered(e -> row.setStyle("-fx-padding: 15; -fx-background-color: #f8fafc; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-padding: 15; -fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;"));

        return row;
    }

    @FXML
    public void handleAddTask() throws Exception {
        StackPane contentArea = (StackPane) tasksList.getScene().lookup("#contentArea");
        if (contentArea != null) {
            contentArea.getChildren().setAll((javafx.scene.Node) FXMLLoader.load(getClass().getResource("/TSK/AddTask.fxml")));
        } else {
            javafx.stage.Stage stage = (javafx.stage.Stage) tasksList.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(FXMLLoader.load(getClass().getResource("/TSK/AddTask.fxml"))));
        }
    }

    @FXML
    public void goBack() throws Exception {
        javafx.stage.Stage stage = (javafx.stage.Stage) tasksList.getScene().getWindow();
        stage.setScene(new javafx.scene.Scene(FXMLLoader.load(getClass().getResource("/gui/front-main-view.fxml"))));
    }

    @FXML
    public void logout(javafx.event.ActionEvent event) {
        gui.SessionHelper.logout(event);
    }
}
