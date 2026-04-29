package gui.TSKControllers;

import utils.SessionManager;
import entities.Tasks;
import services.TskService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.Pos;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

public class TasksController {
    @FXML private Button btnAdmin;
    @FXML private VBox tasksList;
    @FXML private Label lblNavUsername;
    @FXML private Label lblNavUserRole;

    // New UI refs
    @FXML private TextField txtSearch;
    @FXML private Button btnViewKanban;
    @FXML private Button btnViewList;
    @FXML private HBox tableControls;
    @FXML private HBox kanbanPane;
    @FXML private HBox columnHeaders;
    @FXML private Button btnStatusTodo;
    @FXML private Button btnStatusDoing;
    @FXML private Button btnStatusDone;

    // Kanban columns
    @FXML private VBox kanbanTodo;
    @FXML private VBox kanbanDoing;
    @FXML private VBox kanbanDone;

    private final TskService tskService = new TskService();
    private final services.MissionService missionService = new services.MissionService();
    private List<Tasks> allTasks = new ArrayList<>();
    private final Set<String> activeStatuses = new HashSet<>();
    private boolean listMode = false; // default: kanban

    // ── Styles ────────────────────────────────────────────────────────────────
    private static final String BTN_SWITCH_ACTIVE   = "-fx-background-color: white; -fx-text-fill: #cb0c9f; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 9; -fx-padding: 7 18; -fx-effect: dropshadow(three-pass-box,rgba(0,0,0,0.07),6,0,0,2); -fx-cursor: hand;";
    private static final String BTN_SWITCH_INACTIVE = "-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 9; -fx-padding: 7 18; -fx-cursor: hand;";
    private static final String STATUS_BTN_BASE     = "-fx-background-color: #f8fafc; -fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 9; -fx-border-color: #e2e8f0; -fx-border-radius: 9; -fx-padding: 7 16; -fx-cursor: hand;";

    @FXML
    public void initialize() {
        gui.FrontMainController.setNavbarText("Tasks Management", "Pages / Workflow / Tasks");
        boolean isAdmin = SessionManager.getInstance().isAdmin();
        if (btnAdmin != null) { btnAdmin.setVisible(isAdmin); btnAdmin.setManaged(isAdmin); }

        entities.Users current = SessionManager.getInstance().getCurrentUser();
        if (current != null && lblNavUsername != null) {
            lblNavUsername.setText(current.getUsername());
            String role = current.getRole() != null ? current.getRole().replace("ROLE_", "") : "USER";
            lblNavUserRole.setText(role);
        }

        loadTasks();
        setupDragAndDrop();
        applyView(false); // Start on Kanban
    }

    // ── Drag & Drop Setup ─────────────────────────────────────────────────────
    private void setupDragAndDrop() {
        setupColumnForDrop(kanbanTodo, "todo");
        setupColumnForDrop(kanbanDoing, "in_progress");
        setupColumnForDrop(kanbanDone, "completed");
    }

    private void setupColumnForDrop(VBox column, String newState) {
        column.setOnDragOver((DragEvent event) -> {
            if (event.getGestureSource() != column && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        column.setOnDragEntered((DragEvent event) -> {
            if (event.getGestureSource() != column && event.getDragboard().hasString()) {
                column.setStyle(column.getStyle() + " -fx-effect: dropshadow(three-pass-box, #cb0c9f, 10, 0, 0, 0);");
            }
            event.consume();
        });

        column.setOnDragExited((DragEvent event) -> {
            // Remove dropshadow effect
            String style = column.getStyle();
            column.setStyle(style.replaceAll(" -fx-effect: dropshadow\\(three-pass-box, #cb0c9f, 10, 0, 0, 0\\);", ""));
            event.consume();
        });

        column.setOnDragDropped((DragEvent event) -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                try {
                    int taskId = Integer.parseInt(db.getString());
                    Tasks taskToUpdate = null;
                    for (Tasks t : allTasks) {
                        if (t.getId() == taskId) {
                            taskToUpdate = t;
                            break;
                        }
                    }
                    if (taskToUpdate != null) {
                        boolean proceed = true;
                        if ("completed".equals(newState)) {
                            proceed = gui.util.AlertHelper.showCustomAlert(
                                "Mark Task as Done?", 
                                "Are you sure you want to mark this task as done? Once done, it cannot be moved back.", 
                                gui.util.AlertHelper.AlertType.CONFIRMATION
                            );
                        }
                        if (proceed) {
                            taskToUpdate.setState(newState);
                            tskService.modifier(taskToUpdate);
                            success = true;
                            filterAndRender(); // Re-render to show updated status
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    // ── View Switching ─────────────────────────────────────────────────────────
    @FXML public void switchToKanban() { applyView(false); }
    @FXML public void switchToList()   { applyView(true);  }

    private void applyView(boolean list) {
        listMode = list;
        if (kanbanPane    != null) { kanbanPane.setVisible(!list);   kanbanPane.setManaged(!list);   }
        if (tasksList     != null) { tasksList.setVisible(list);     tasksList.setManaged(list);     }
        if (tableControls != null) { tableControls.setVisible(list); tableControls.setManaged(list); }
        if (columnHeaders != null) { columnHeaders.setVisible(list); columnHeaders.setManaged(list); }

        if (btnViewKanban != null) btnViewKanban.setStyle(list ? BTN_SWITCH_INACTIVE : BTN_SWITCH_ACTIVE);
        if (btnViewList   != null) btnViewList.setStyle(list   ? BTN_SWITCH_ACTIVE   : BTN_SWITCH_INACTIVE);

        filterAndRender();
    }

    // ── Status Filter Toggles ─────────────────────────────────────────────────
    @FXML public void toggleStatusTodo()  { toggleStatus("todo",        btnStatusTodo);  }
    @FXML public void toggleStatusDoing() { toggleStatus("in_progress", btnStatusDoing); }
    @FXML public void toggleStatusDone()  { toggleStatus("completed",   btnStatusDone);  }

    private void toggleStatus(String status, Button btn) {
        if (activeStatuses.contains(status)) {
            activeStatuses.remove(status);
            if (btn != null) btn.setStyle(STATUS_BTN_BASE);
        } else {
            activeStatuses.add(status);
            String activeStyle = STATUS_BTN_BASE;
            if      ("todo".equals(status))        activeStyle = "-fx-background-color: linear-gradient(to right,#7928CA,#cb0c9f); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 9; -fx-border-color: transparent; -fx-border-radius: 9; -fx-padding: 7 16; -fx-cursor: hand;";
            else if ("in_progress".equals(status)) activeStyle = "-fx-background-color: linear-gradient(to right,#3b82f6,#06b6d4); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 9; -fx-border-color: transparent; -fx-border-radius: 9; -fx-padding: 7 16; -fx-cursor: hand;";
            else if ("completed".equals(status))   activeStyle = "-fx-background-color: linear-gradient(to right,#22c55e,#84cc16); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 9; -fx-border-color: transparent; -fx-border-radius: 9; -fx-padding: 7 16; -fx-cursor: hand;";
            if (btn != null) btn.setStyle(activeStyle);
        }
        filterAndRender();
    }

    // ── Search ────────────────────────────────────────────────────────────────
    @FXML public void onSearch() { filterAndRender(); }

    // ── Data ──────────────────────────────────────────────────────────────────
    private void loadTasks() {
        try {
            allTasks = tskService.afficher();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        filterAndRender();
    }

    private void filterAndRender() {
        String search = (txtSearch != null) ? txtSearch.getText().toLowerCase().trim() : "";

        if (listMode) {
            renderListView(search);
        } else {
            renderKanbanView(search);
        }
    }

    // ── List View Render ──────────────────────────────────────────────────────
    private void renderListView(String search) {
        if (tasksList == null) return;
        tasksList.getChildren().clear();
        for (Tasks t : allTasks) {
            if (!matchesFilters(t, search)) continue;
            tasksList.getChildren().add(buildTableRow(t));
        }
        if (tasksList.getChildren().isEmpty()) {
            Label empty = new Label("No tasks found.");
            empty.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px; -fx-padding: 20;");
            tasksList.getChildren().add(empty);
        }
    }

    // ── Kanban View Render ────────────────────────────────────────────────────
    private void renderKanbanView(String search) {
        if (kanbanTodo == null) return;
        kanbanTodo.getChildren().clear();
        kanbanDoing.getChildren().clear();
        kanbanDone.getChildren().clear();

        for (Tasks t : allTasks) {
            if (!matchesSearch(t, search)) continue;
            VBox card = buildKanbanCard(t);
            String state = t.getState() != null ? t.getState().toLowerCase() : "todo";
            if ("completed".equals(state) || "done".equals(state))           kanbanDone.getChildren().add(card);
            else if ("in_progress".equals(state) || "doing".equals(state))   kanbanDoing.getChildren().add(card);
            else                                                              kanbanTodo.getChildren().add(card);
        }

        // Empty labels
        if (kanbanTodo.getChildren().isEmpty())  { Label l = noTasksLabel(); kanbanTodo.getChildren().add(l);  }
        if (kanbanDoing.getChildren().isEmpty()) { Label l = noTasksLabel(); kanbanDoing.getChildren().add(l); }
        if (kanbanDone.getChildren().isEmpty())  { Label l = noTasksLabel(); kanbanDone.getChildren().add(l);  }
    }

    private Label noTasksLabel() {
        Label l = new Label("No tasks");
        l.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12px; -fx-padding: 8;");
        return l;
    }

    // ── Match Helpers ─────────────────────────────────────────────────────────
    private boolean matchesFilters(Tasks t, String search) {
        String state = t.getState() != null ? t.getState().toLowerCase() : "todo";
        // Normalise state for filter comparison
        String normState = state;
        if ("done".equals(state)) normState = "completed";
        if ("doing".equals(state)) normState = "in_progress";
        boolean matchesStatus = activeStatuses.isEmpty() || activeStatuses.contains(normState);
        return matchesSearch(t, search) && matchesStatus;
    }

    private boolean matchesSearch(Tasks t, String search) {
        if (search.isEmpty()) return true;
        String title   = t.getTitle() != null ? t.getTitle().toLowerCase() : "";
        entities.Mission m = (t.getBelong_to_id() > 0) ? missionService.getMissionById(t.getBelong_to_id()) : null;
        String mission = m != null ? m.getTitle().toLowerCase() : "";
        return title.contains(search) || mission.contains(search);
    }

    // ── Kanban Card Builder ───────────────────────────────────────────────────
    private VBox buildKanbanCard(Tasks t) {
        VBox card = new VBox(8);
        boolean done = "completed".equalsIgnoreCase(t.getState()) || "done".equalsIgnoreCase(t.getState());

        String cardStyle = done
            ? "-fx-background-color: linear-gradient(to bottom right,#f8f9fa,#e9ecef); -fx-background-radius: 14; -fx-padding: 14; -fx-effect: dropshadow(three-pass-box,rgba(0,0,0,0.05),8,0,0,2); -fx-border-color: transparent; -fx-opacity: 0.8;"
            : "-fx-background-color: white; -fx-background-radius: 14; -fx-padding: 14; -fx-effect: dropshadow(three-pass-box,rgba(0,0,0,0.05),8,0,0,2); -fx-border-color: transparent; -fx-cursor: hand;";
        card.setStyle(cardStyle);

        // Header row (assigned user + task id)
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label lblId = new Label("#" + t.getId());
        lblId.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #adb5bd;");
        Region hspacer = new Region(); HBox.setHgrow(hspacer, Priority.ALWAYS);
        if (done) {
            Label lock = new Label("🔒");
            lock.setStyle("-fx-font-size: 10px;");
            header.getChildren().addAll(lblId, hspacer, lock);
        } else {
            header.getChildren().addAll(lblId, hspacer);
        }

        Label lblTitle = new Label(t.getTitle());
        lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #344767;");
        lblTitle.setWrapText(true);

        // Footer: deadline + mission badge
        HBox footer = new HBox(8);
        footer.setAlignment(Pos.CENTER_LEFT);
        entities.Mission assocMission = (t.getBelong_to_id() > 0) ? missionService.getMissionById(t.getBelong_to_id()) : null;
        String missionName = assocMission != null ? assocMission.getTitle() : "PROJECT";
        if (missionName.length() > 12) missionName = missionName.substring(0, 12) + "…";

        Label lblDeadline = new Label("⏰ " + (t.getTime_limit() != null ? t.getTime_limit() : "N/A"));
        lblDeadline.setStyle("-fx-font-size: 10px; -fx-text-fill: #67748e;");

        Label lblMission = new Label(missionName);
        lblMission.setStyle("-fx-background-color: #f8f9fa; -fx-text-fill: #64748b; -fx-font-size: 9px; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 6;");

        Region fspacer = new Region(); HBox.setHgrow(fspacer, Priority.ALWAYS);
        footer.getChildren().addAll(lblDeadline, fspacer, lblMission);

        card.getChildren().addAll(header, lblTitle, footer);

        if (!done) {
            card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 14; -fx-padding: 14; -fx-effect: dropshadow(three-pass-box,rgba(203,12,159,0.12),12,0,0,3); -fx-border-color: #cb0c9f; -fx-border-width: 1; -fx-border-radius: 14; -fx-cursor: hand;"));
            card.setOnMouseExited (e -> card.setStyle(cardStyle));
            
            // Setup Drag & Drop (source) only if not done
            card.setOnDragDetected(event -> {
                Dragboard db = card.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(String.valueOf(t.getId()));
                db.setContent(content);
                event.consume();
            });
        }

        return card;
    }

    // ── Table Row Builder ─────────────────────────────────────────────────────
    private HBox buildTableRow(Tasks t) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 15; -fx-background-color: white; -fx-background-radius: 12; " +
                     "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.02), 10, 0, 0, 2); " +
                     "-fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");

        // Task Info
        VBox taskInfo = new VBox(4);
        taskInfo.setPrefWidth(300);
        Label lblTitle = new Label(t.getTitle());
        lblTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-font-size: 14px;");

        entities.Mission assocMission = (t.getBelong_to_id() > 0) ? missionService.getMissionById(t.getBelong_to_id()) : null;
        String missionStr = assocMission != null ? "Mission: " + assocMission.getTitle() : "No Mission associated";
        Label lblSub = new Label("#" + t.getId() + " | " + missionStr);
        lblSub.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
        taskInfo.getChildren().addAll(lblTitle, lblSub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Status Badge
        String state = t.getState() != null ? t.getState().toLowerCase() : "todo";
        String badgeColor;
        String label;
        if      ("completed".equals(state) || "done".equals(state))            { badgeColor = "linear-gradient(to right,#22c55e,#84cc16)"; label = "DONE"; }
        else if ("in_progress".equals(state) || "doing".equals(state))         { badgeColor = "linear-gradient(to right,#3b82f6,#06b6d4)"; label = "DOING"; }
        else                                                                    { badgeColor = "linear-gradient(to right,#64748b,#94a3b8)"; label = "TO DO"; }

        Label lblStatus = new Label(label);
        lblStatus.setStyle("-fx-background-color: " + badgeColor + "; -fx-text-fill: white; " +
                           "-fx-padding: 5 12; -fx-background-radius: 10; " +
                           "-fx-font-weight: bold; -fx-font-size: 10px;");
        lblStatus.setMinWidth(130);
        lblStatus.setAlignment(Pos.CENTER);

        // Deadline
        Label lblDeadline = new Label(t.getTime_limit() != null ? t.getTime_limit() : "No Deadline");
        lblDeadline.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");
        lblDeadline.setMinWidth(150);
        lblDeadline.setAlignment(Pos.CENTER);

        // Actions
        HBox actions = new HBox(8);
        actions.setPrefWidth(200);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnEdit = new Button("✎ Edit");
        btnEdit.setStyle("-fx-background-color: #3b82f615; -fx-text-fill: #3b82f6; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 11px;");
        btnEdit.setDisable("completed".equals(state) || "done".equals(state));
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
            if (gui.util.AlertHelper.showCustomAlert("Delete Task?", "Are you sure you want to delete this task?",
                                                      gui.util.AlertHelper.AlertType.CONFIRMATION)) {
                try {
                    tskService.supprimer(t.getId());
                    allTasks.remove(t);
                    filterAndRender();
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });

        actions.getChildren().addAll(btnEdit, btnDelete);
        row.getChildren().addAll(taskInfo, spacer, lblStatus, lblDeadline, actions);

        row.setOnMouseEntered(e -> row.setStyle("-fx-padding: 15; -fx-background-color: #f8fafc; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;"));
        row.setOnMouseExited (e -> row.setStyle("-fx-padding: 15; -fx-background-color: white;  -fx-background-radius: 12; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;"));

        return row;
    }

    // ── Navigation ────────────────────────────────────────────────────────────
    @FXML public void goToAdmin()   throws Exception { switchScene("/Users/Admin.fxml"); }
    @FXML public void goToIdea()    throws Exception { switchScene("/TSK/Idea.fxml"); }
    @FXML public void goToMission() throws Exception { switchScene("/TSK/Mission.fxml"); }
    @FXML public void goToTasks()   throws Exception { switchScene("/TSK/Tasks.fxml"); }

    private void switchScene(String fxml) throws Exception {
        StackPane contentArea = (StackPane) tasksList.getScene().lookup("#contentArea");
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        javafx.scene.Parent root = loader.load();
        javafx.scene.Node view = root;
        if (root instanceof BorderPane) view = ((BorderPane) root).getCenter();
        if (contentArea != null) contentArea.getChildren().setAll(view);
        else {
            javafx.stage.Stage stage = (javafx.stage.Stage) tasksList.getScene().getWindow();
            stage.getScene().setRoot(root);
        }
    }

    @FXML
    public void handleAddTask() throws Exception {
        StackPane contentArea = (StackPane) tasksList.getScene().lookup("#contentArea");
        if (contentArea != null)
            contentArea.getChildren().setAll((javafx.scene.Node) FXMLLoader.load(getClass().getResource("/TSK/AddTask.fxml")));
        else {
            javafx.stage.Stage stage = (javafx.stage.Stage) tasksList.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(FXMLLoader.load(getClass().getResource("/TSK/AddTask.fxml"))));
        }
    }

    @FXML public void goBack()      throws Exception { goToTasks(); }
    @FXML public void onOpenProfile(javafx.scene.input.MouseEvent event) {
        try { switchScene("/Users/Profile.fxml"); } catch (Exception e) { e.printStackTrace(); }
    }
    @FXML public void logout(javafx.event.ActionEvent event) { gui.SessionHelper.logout(event); }
}
