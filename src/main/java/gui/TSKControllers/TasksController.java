package gui.TSKControllers;

import utils.SessionManager;
import entities.Tasks;
import services.TskService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.SQLException;

public class TasksController {
    @FXML private javafx.scene.control.Button btnAdmin;
    @FXML private TableView<Tasks> tasksTable;
    @FXML private TableColumn<Tasks, Integer> colId;
    @FXML private TableColumn<Tasks, String> colTitle;
    @FXML private TableColumn<Tasks, String> colState;
    @FXML private TableColumn<Tasks, String> colTimeLimit;
    @FXML private TableColumn<Tasks, Void> colActions;

    private final TskService tskService = new TskService();

    @FXML
    public void initialize() {
        // Hide Admin button if not admin
        boolean isAdmin = SessionManager.getInstance().isAdmin();
        if (btnAdmin != null) {
            btnAdmin.setVisible(isAdmin);
            btnAdmin.setManaged(isAdmin);
        }

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colState.setCellValueFactory(new PropertyValueFactory<>("state"));
        colTimeLimit.setCellValueFactory(new PropertyValueFactory<>("time_limit"));
        
        addActionButtons();
        loadTasks();
    }

    @FXML public void goToAdmin()   throws Exception { switchScene("/Users/Admin.fxml"); }
    @FXML public void goToIdea()    throws Exception { switchScene("/TSK/Idea.fxml"); }
    @FXML public void goToMission() throws Exception { switchScene("/TSK/Mission.fxml"); }
    @FXML public void goToTasks()   throws Exception { switchScene("/TSK/Tasks.fxml"); }

    private void switchScene(String fxml) throws Exception {
        javafx.stage.Stage stage = (javafx.stage.Stage) tasksTable.getScene().getWindow();
        stage.setScene(new javafx.scene.Scene(javafx.fxml.FXMLLoader.load(getClass().getResource(fxml))));
    }

    private void addActionButtons() {
        colActions.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            private final javafx.scene.control.Button btnEdit = new javafx.scene.control.Button("✏");
            private final javafx.scene.control.Button btnDelete = new javafx.scene.control.Button("🗑");
            private final javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(5, btnEdit, btnDelete);
            {
                btnEdit.setOnAction(e -> {
                    Tasks t = getTableView().getItems().get(getIndex());
                    try {
                        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/TSK/EditTask.fxml"));
                        javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
                        EditTaskController ctrl = loader.getController();
                        ctrl.setTask(t);
                        ((javafx.stage.Stage) getTableView().getScene().getWindow()).setScene(scene);
                    } catch (Exception ex) { ex.printStackTrace(); }
                });
                btnDelete.setOnAction(e -> {
                    Tasks t = getTableView().getItems().get(getIndex());
                    try {
                        tskService.supprimer(t.getId());
                        loadTasks();
                    } catch (Exception ex) { ex.printStackTrace(); }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
    }

    private void loadTasks() {
        try {
            tasksTable.setItems(FXCollections.observableArrayList(tskService.afficher()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAddTask() throws Exception {
        javafx.stage.Stage stage = (javafx.stage.Stage) tasksTable.getScene().getWindow();
        stage.setScene(new javafx.scene.Scene(javafx.fxml.FXMLLoader.load(getClass().getResource("/TSK/AddTask.fxml"))));
    }

    @FXML
    public void goBack() throws Exception {
        javafx.stage.Stage stage = (javafx.stage.Stage) tasksTable.getScene().getWindow();
        stage.setScene(new javafx.scene.Scene(javafx.fxml.FXMLLoader.load(getClass().getResource("/gui/front-main-view.fxml"))));
    }
}
