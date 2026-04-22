package gui.TSKControllers;

import services.MissionService;
import utils.SessionManager;
import entities.Mission;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class MissionController {
    @FXML private javafx.scene.control.Button btnAdmin;
    @FXML private TableView<Mission> missionTable;
    @FXML private TableColumn<Mission, Integer> colId;
    @FXML private TableColumn<Mission, String> colTitle;
    @FXML private TableColumn<Mission, String> colState;
    @FXML private TableColumn<Mission, String> colDate;
    @FXML private TableColumn<Mission, Void> colActions;

    private final MissionService missionService = new MissionService();

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
        colDate.setCellValueFactory(new PropertyValueFactory<>("mission_date"));
        
        addActionButtons();
        loadMissions();
    }

    @FXML public void goToAdmin()   throws Exception { switchScene("/Users/Admin.fxml"); }
    @FXML public void goToIdea()    throws Exception { switchScene("/TSK/Idea.fxml"); }
    @FXML public void goToMission() throws Exception { switchScene("/TSK/Mission.fxml"); }
    @FXML public void goToTasks()   throws Exception { switchScene("/TSK/Tasks.fxml"); }

    private void switchScene(String fxml) throws Exception {
        javafx.stage.Stage stage = (javafx.stage.Stage) missionTable.getScene().getWindow();
        stage.setScene(new javafx.scene.Scene(javafx.fxml.FXMLLoader.load(getClass().getResource(fxml))));
    }

    private void addActionButtons() {
        colActions.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            private final javafx.scene.control.Button btnEdit = new javafx.scene.control.Button("✏");
            private final javafx.scene.control.Button btnDelete = new javafx.scene.control.Button("🗑");
            private final javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(5, btnEdit, btnDelete);
            {
                btnEdit.setOnAction(e -> {
                    Mission m = getTableView().getItems().get(getIndex());
                    try {
                        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/TSK/EditMission.fxml"));
                        javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
                        EditMissionController ctrl = loader.getController();
                        ctrl.setMission(m);
                        ((javafx.stage.Stage) getTableView().getScene().getWindow()).setScene(scene);
                    } catch (Exception ex) { ex.printStackTrace(); }
                });
                btnDelete.setOnAction(e -> {
                    Mission m = getTableView().getItems().get(getIndex());
                    try {
                        missionService.supprimer(m.getId());
                        loadMissions();
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

    private void loadMissions() {
        try {
            missionTable.setItems(javafx.collections.FXCollections.observableArrayList(missionService.afficher()));
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAddMission() throws Exception {
        javafx.stage.Stage stage = (javafx.stage.Stage) missionTable.getScene().getWindow();
        stage.setScene(new javafx.scene.Scene(javafx.fxml.FXMLLoader.load(getClass().getResource("/TSK/AddMission.fxml"))));
    }

    @FXML
    public void goBack() throws Exception {
        javafx.stage.Stage stage = (javafx.stage.Stage) missionTable.getScene().getWindow();
        stage.setScene(new javafx.scene.Scene(javafx.fxml.FXMLLoader.load(getClass().getResource("/gui/front-main-view.fxml"))));
    }
}
