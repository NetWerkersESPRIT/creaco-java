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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class AddTaskController {
    @FXML private TextField txtTitle;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<String> comboState;
    @FXML private DatePicker dateLimit;
    @FXML private TextField txtTime;
    @FXML private ComboBox<Mission> comboMission;
    @FXML private Label lblMessage;

    private final TskService tskService = new TskService();
    private final MissionService missionService = new MissionService();

    @FXML
    public void initialize() {
        gui.FrontMainController.setNavbarText("Add New Task", "Tasks / New");
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

    public void setMission(Mission mission) {
        if (comboMission != null && mission != null) {
            // Find the mission in the combo box items by ID
            for (Mission m : comboMission.getItems()) {
                if (m.getId() == mission.getId()) {
                    comboMission.getSelectionModel().select(m);
                    break;
                }
            }
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
                String timeStr = (txtTime != null && !txtTime.getText().isBlank()) ? txtTime.getText().trim() : "12:00";
                LocalTime time;
                try {
                    time = LocalTime.parse(timeStr);
                } catch (DateTimeParseException e) {
                    lblMessage.setText("❌ Invalid time format. Use HH:mm");
                    lblMessage.setStyle("-fx-text-fill: red;");
                    return;
                }

                LocalDateTime deadline = LocalDateTime.of(dateLimit.getValue(), time);
                LocalDateTime limit = LocalDateTime.now().plusHours(1);

                if (deadline.isBefore(limit)) {
                    lblMessage.setText("❌ Deadline must be at least 1 hour from now.");
                    lblMessage.setStyle("-fx-text-fill: red;");
                    return;
                }

                task.setTime_limit(deadline.toString().replace("T", " "));
            } else {
                task.setTime_limit("No Deadline");
            }
            
            if (comboMission != null && comboMission.getValue() != null) {
                task.setBelong_to_id(comboMission.getValue().getId());
            } else {
                task.setBelong_to_id(0); // Or handle as required
            }

            entities.Users currentUser = utils.SessionManager.getInstance().getCurrentUser();
            if (currentUser != null) {
                task.setIssued_by_id(currentUser.getId());
                task.setAssumed_by_id(currentUser.getId());
            } else {
                task.setIssued_by_id(1); // Fallback
                task.setAssumed_by_id(1); // Fallback
            }

            tskService.ajouter(task);
            
            gui.util.AlertHelper.showCustomAlert(
                "Task Created", 
                "Task added successfully!", 
                gui.util.AlertHelper.AlertType.INFORMATION
            );
            
            goToTasks();
        } catch (SQLException e) {
            lblMessage.setText("❌ Error: " + e.getMessage());
            lblMessage.setStyle("-fx-text-fill: red;");
        } catch (Exception e) {
            e.printStackTrace();
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
