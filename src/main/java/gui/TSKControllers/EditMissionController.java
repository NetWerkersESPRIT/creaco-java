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

import javafx.scene.layout.StackPane;
import javafx.scene.layout.BorderPane;

public class EditMissionController {
    @FXML private TextField txtTitle;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<String> comboState;
    @FXML private Label lblMessage;

    private final MissionService missionService = new MissionService();
    private Mission currentMission;

    @FXML
    public void initialize() {
        gui.FrontMainController.setNavbarText("Edit Mission", "Missions / Management");
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
    public void onGenerateAI(javafx.event.ActionEvent event) {
        String title = txtTitle.getText().trim();

        if (title.isEmpty()) {
            lblMessage.setText("❌ Please enter a mission title first.");
            lblMessage.setStyle("-fx-text-fill: red;");
            return;
        }

        String currentDescription = txtDescription.getText().trim();
        String prompt;

        if (currentDescription.isEmpty() || currentDescription.equals("Generating description...")) {
            prompt = String.format(
                "Generate a professional and motivating description for a mission named '%s'. " +
                "Keep it concise (max 2 sentences).",
                title);
        } else {
            prompt = String.format(
                "I have a mission named '%s' with the following initial description: '%s'. " +
                "Improve this description to make it more professional and motivating. " +
                "Keep it concise (max 2 sentences).",
                title, currentDescription);
        }

        txtDescription.setText("Generating description...");
        
        new Thread(() -> {
            String response = utils.GeminiService.getGeminiResponse(prompt);
            javafx.application.Platform.runLater(() -> {
                txtDescription.setText(response);
            });
        }).start();
    }

    @FXML
    public void goBack() throws Exception {
        switchScene("/TSK/Mission.fxml");
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
