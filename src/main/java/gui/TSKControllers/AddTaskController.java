package gui.TSKControllers;

import entities.Mission;
import entities.Tasks;
import services.MissionService;
import services.TskService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.sql.SQLException;

public class AddTaskController {
    @FXML private TextField txtTitle;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<String> comboState;
    @FXML private DatePicker dateLimit;
    @FXML private ComboBox<Mission> comboMission;
    @FXML private Label lblMessage;

    private final TskService tskService = new TskService();
    private final MissionService missionService = new MissionService();

    @FXML
    public void initialize() {
        try {
            if (comboState != null) {
                comboState.setItems(javafx.collections.FXCollections.observableArrayList("to do", "doing", "done"));
                comboState.getSelectionModel().selectFirst();
            }

            if (comboMission != null) {
                comboMission.setItems(javafx.collections.FXCollections.observableArrayList(missionService.afficher()));
                comboMission.setCellFactory(lv -> new ListCell<Mission>() {
                    @Override
                    protected void updateItem(Mission item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty ? "" : item.getTitle());
                    }
                });
                comboMission.setButtonCell(new ListCell<Mission>() {
                    @Override
                    protected void updateItem(Mission item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty ? "" : item.getTitle());
                    }
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML public void goToIdea()    throws Exception { switchScene("/TSK/Idea.fxml"); }
    @FXML public void goToMission() throws Exception { switchScene("/TSK/Mission.fxml"); }
    @FXML public void goToTasks()   throws Exception { switchScene("/TSK/Tasks.fxml"); }
    @FXML public void goToAdmin()   throws Exception { switchScene("/Users/Admin.fxml"); }

    private void switchScene(String fxml) throws Exception {
        StackPane contentArea = (StackPane) txtTitle.getScene().lookup("#contentArea");
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        javafx.scene.Parent root = loader.load();
        javafx.scene.Node view = root;

        if (root instanceof BorderPane) {
            view = ((BorderPane) root).getCenter();
        }

        if (contentArea != null) {
            contentArea.getChildren().setAll(view);
        } else {
            Stage stage = (Stage) txtTitle.getScene().getWindow();
            stage.getScene().setRoot(root);
        }
    }

    @FXML
    public void saveTask() {
        if (txtTitle.getText().isEmpty()) {
            lblMessage.setText("❌ Title is required.");
            lblMessage.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            Tasks task = new Tasks();
            task.setTitle(txtTitle.getText().trim());
            task.setDescription(txtDescription != null ? txtDescription.getText().trim() : "");
            task.setState(comboState != null && comboState.getValue() != null ? comboState.getValue() : "to do");
            
            if (dateLimit != null && dateLimit.getValue() != null) {
                task.setTime_limit(dateLimit.getValue().toString());
            } else {
                task.setTime_limit("0");
            }
            
            if (comboMission != null && comboMission.getValue() != null) {
                task.setBelong_to_id(comboMission.getValue().getId());
            } else {
                task.setBelong_to_id(0); // Or handle as required
            }

            tskService.ajouter(task);
            lblMessage.setText("✅ Task added successfully!");
            lblMessage.setStyle("-fx-text-fill: green;");
            clearFields();
        } catch (SQLException e) {
            lblMessage.setText("❌ Error: " + e.getMessage());
            lblMessage.setStyle("-fx-text-fill: red;");
        }
    }

    private void clearFields() {
        txtTitle.clear();
        if (txtDescription != null) txtDescription.clear();
        if (comboState != null) comboState.getSelectionModel().selectFirst();
        if (dateLimit != null) dateLimit.setValue(null);
        if (comboMission != null) comboMission.getSelectionModel().clearSelection();
    }

    @FXML
    public void goBack() throws Exception {
        goToTasks();
    }
}
