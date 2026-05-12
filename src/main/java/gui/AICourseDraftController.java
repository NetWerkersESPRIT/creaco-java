package gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import utils.GroqService;

public class AICourseDraftController {

    @FXML private TextField aiTopicField;
    @FXML private TextArea aiResultArea;
    @FXML private Button btnGenerateIdea;
    @FXML private Button btnDraftCourse;

    @FXML
    private void onGenerateCourseIdea() {
        String topic = aiTopicField.getText();
        if (topic == null || topic.trim().isEmpty()) {
            aiResultArea.setText("Please enter a topic first!");
            return;
        }
        
        aiResultArea.setText("✨ Generating your course draft for: " + topic + "...\nThis may take a few seconds.");
        btnGenerateIdea.setDisable(true);
        
        new Thread(() -> {
            try {
                String result = GroqService.generateCourseIdea(topic);
                Platform.runLater(() -> {
                    aiResultArea.setText(result);
                    btnGenerateIdea.setDisable(false);
                    btnDraftCourse.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    aiResultArea.setText("Error generating idea: " + e.getMessage());
                    btnGenerateIdea.setDisable(false);
                });
            }
        }).start();
    }

    @FXML
    private void onDraftCourse() {
        // Here you could potentially pass the content back to the course form
        // For now, let's just close it or show a message
        System.out.println("Draft course content: " + aiResultArea.getText());
        onClose();
    }

    @FXML
    private void onClose() {
        if (aiTopicField.getScene() != null) {
            Stage stage = (Stage) aiTopicField.getScene().getWindow();
            stage.close();
            
            // Remove blur effect if it was applied
            if (FrontMainController.getInstance() != null) {
                FrontMainController.getInstance().removeBlur();
            }
        }
    }
}
