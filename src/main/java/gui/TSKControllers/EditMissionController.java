package gui.TSKControllers;

import entities.Mission;
import services.MissionService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.SQLException;

public class EditMissionController {
    @FXML private TextField txtTitle;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<String> comboState;
    @FXML private Label lblMessage;

    private final MissionService missionService = new MissionService();
    private Mission currentMission;

    @FXML
    public void initialize() {
        comboState.setItems(FXCollections.observableArrayList("new", "in_progress", "completed"));
    }

    public void setMission(Mission mission) {
        this.currentMission = mission;
        txtTitle.setText(mission.getTitle());
        txtDescription.setText(mission.getDescription());
        comboState.setValue(mission.getState());
    }

    @FXML
    public void updateMission() {
        if (txtTitle.getText().isEmpty() || comboState.getValue() == null) {
            lblMessage.setText("❌ Title and State are required.");
            lblMessage.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            currentMission.setTitle(txtTitle.getText().trim());
            currentMission.setDescription(txtDescription.getText().trim());
            currentMission.setState(comboState.getValue());

            missionService.modifier(currentMission);
            lblMessage.setText("✅ Mission updated successfully!");
            lblMessage.setStyle("-fx-text-fill: green;");
        } catch (SQLException e) {
            lblMessage.setText("❌ Error: " + e.getMessage());
            lblMessage.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    public void goBack() throws Exception {
        Stage stage = (Stage) txtTitle.getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/TSK/Mission.fxml"))));
    }
}
