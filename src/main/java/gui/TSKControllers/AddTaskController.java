package gui.TSKControllers;

import entities.Tasks;
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
    @FXML private TextField txtTitle, txtState, txtTimeLimit;
    @FXML private TextArea txtDescription;
    @FXML private Label lblMessage;

    private final TskService tskService = new TskService();

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
            task.setState(txtState != null ? txtState.getText().trim() : "TODO");
            
            String limit = txtTimeLimit != null ? txtTimeLimit.getText().trim() : "0";
            task.setTime_limit(limit);

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
        if (txtState != null) txtState.clear();
        if (txtTimeLimit != null) txtTimeLimit.clear();
    }

    @FXML
    public void goBack() throws Exception {
        goToTasks();
    }
}
