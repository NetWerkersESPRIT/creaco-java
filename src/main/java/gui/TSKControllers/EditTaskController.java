package gui.TSKControllers;

import entities.Tasks;
import services.TskService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.time.LocalDate;

public class EditTaskController {
    @FXML private TextField txtTitle;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<String> comboState;
    @FXML private DatePicker dateTimeLimit;
    @FXML private Label lblMessage;

    private final TskService tskService = new TskService();
    private Tasks currentTask;

    @FXML
    public void initialize() {
        gui.FrontMainController.setNavbarText("Edit Task", "Tasks / Management");
        comboState.setItems(FXCollections.observableArrayList("to do", "doing", "done"));
    }

    public void setTask(Tasks task) {
        this.currentTask = task;
        txtTitle.setText(task.getTitle());
        txtDescription.setText(task.getDescription());
        comboState.setValue(task.getState());
        
        if (task.getTime_limit() != null && !task.getTime_limit().isEmpty()) {
            try {
                // Formatting is yyyy-MM-dd HH:mm:ss, DatePicker needs yyyy-MM-dd
                String datePart = task.getTime_limit().split(" ")[0];
                dateTimeLimit.setValue(LocalDate.parse(datePart));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void updateTask() {
        if (txtTitle.getText().isEmpty() || comboState.getValue() == null || dateTimeLimit.getValue() == null) {
            lblMessage.setText("❌ Title, State and Time Limit are required.");
            lblMessage.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            currentTask.setTitle(txtTitle.getText().trim());
            currentTask.setDescription(txtDescription.getText().trim());
            currentTask.setState(comboState.getValue());
            currentTask.setTime_limit(dateTimeLimit.getValue().toString() + " 23:59:59");

            tskService.modifier(currentTask);
            lblMessage.setText("✅ Task updated successfully!");
            lblMessage.setStyle("-fx-text-fill: green;");
        } catch (SQLException e) {
            lblMessage.setText("❌ Error: " + e.getMessage());
            lblMessage.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    public void goBack() throws Exception {
        Stage stage = (Stage) txtTitle.getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/TSK/Tasks.fxml"))));
    }
}
