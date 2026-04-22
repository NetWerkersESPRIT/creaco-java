package gui.TSKControllers;

import entities.Tasks;
import services.TskService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.SQLException;

public class AddTaskController {
    @FXML private TextField txtTitle;
    @FXML private DatePicker dateTimeLimit;
    @FXML private TextArea txtDescription;
    @FXML private Label lblMessage;

    private final TskService tskService = new TskService();

    @FXML
    public void saveTask() {
        if (txtTitle.getText().isEmpty() || dateTimeLimit.getValue() == null) {
            lblMessage.setText("❌ Title and Time Limit are required.");
            lblMessage.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            Tasks task = new Tasks();
            task.setTitle(txtTitle.getText().trim());
            task.setDescription(txtDescription.getText().trim());
            
            // Format as yyyy-MM-dd HH:mm:ss (setting time to end of day 23:59:59 or just date)
            task.setTime_limit(dateTimeLimit.getValue().toString() + " 23:59:59");

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
        txtDescription.clear();
        dateTimeLimit.setValue(null);
    }

    @FXML
    public void goBack() throws Exception {
        Stage stage = (Stage) txtTitle.getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/TSK/Tasks.fxml"))));
    }
}
