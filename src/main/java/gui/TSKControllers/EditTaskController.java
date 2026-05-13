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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import javafx.scene.layout.StackPane;
import javafx.scene.layout.BorderPane;

public class EditTaskController {
    @FXML private TextField txtTitle;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<String> comboState;
    @FXML private DatePicker dateTimeLimit;
    @FXML private TextField txtTime;
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
                String[] parts = task.getTime_limit().split(" ");
                dateTimeLimit.setValue(LocalDate.parse(parts[0]));
                if (parts.length > 1 && txtTime != null) {
                    // Extract HH:mm
                    String t = parts[1];
                    if (t.length() >= 5) {
                        txtTime.setText(t.substring(0, 5));
                    }
                }
            } catch (Exception e) {
                System.err.println("Error parsing time limit: " + e.getMessage());
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
            String timeStr = (txtTime != null && !txtTime.getText().isBlank()) ? txtTime.getText().trim() : "12:00";
            LocalTime time;
            try {
                time = LocalTime.parse(timeStr);
            } catch (DateTimeParseException e) {
                lblMessage.setText("❌ Invalid time format. Use HH:mm");
                lblMessage.setStyle("-fx-text-fill: red;");
                return;
            }

            LocalDateTime deadline = LocalDateTime.of(dateTimeLimit.getValue(), time);
            LocalDateTime limit = LocalDateTime.now().plusHours(1);

            if (deadline.isBefore(limit)) {
                lblMessage.setText("❌ Deadline must be at least 1 hour from now.");
                lblMessage.setStyle("-fx-text-fill: red;");
                return;
            }

            currentTask.setTitle(txtTitle.getText().trim());
            currentTask.setDescription(txtDescription.getText().trim());
            currentTask.setState(comboState.getValue());
            currentTask.setTime_limit(deadline.toString().replace("T", " "));

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
        switchScene("/TSK/Tasks.fxml");
    }

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
}
