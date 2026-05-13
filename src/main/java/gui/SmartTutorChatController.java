package gui;

import entities.Course;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import utils.GroqService;

public class SmartTutorChatController {

    @FXML private VBox chatBox;
    @FXML private TextField chatInput;
    @FXML private ScrollPane chatScroll;
    @FXML private Label titleLabel;

    private Course currentCourse;

    public void setCourse(Course course) {
        this.currentCourse = course;
        if (titleLabel != null) {
            titleLabel.setText("✨ Smart Tutor: " + (course != null ? course.getTitre() : "General"));
        }
        
        if (chatBox.getChildren().isEmpty()) {
            addChatBubble("Tutor", "Hi! I'm your AI tutor for " + (course != null ? course.getTitre() : "this session") + ". How can I help you today?", false);
        }
    }

    @FXML
    private void sendChatMessage() {
        String msg = chatInput.getText().trim();
        if (msg.isEmpty()) return;

        addChatBubble("You", msg, true);
        chatInput.clear();

        String courseName = (currentCourse != null) ? currentCourse.getTitre() : "the current course";

        new Thread(() -> {
            String reply = GroqService.getSmartTutorResponse(courseName, msg);
            Platform.runLater(() -> addChatBubble("Tutor", reply, false));
        }).start();
    }

    private void addChatBubble(String sender, String text, boolean isUser) {
        VBox bubble = new VBox(5);
        bubble.setMaxWidth(350);
        
        Label senderLabel = new Label(sender);
        senderLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8;");
        
        Label msgLabel = new Label(text);
        msgLabel.setWrapText(true);
        msgLabel.setStyle("-fx-font-size: 13px; -fx-padding: 12; -fx-background-radius: 15; " + 
                         (isUser ? "-fx-background-color: linear-gradient(to right, #ec4899, #be185d); -fx-text-fill: white;" 
                                 : "-fx-background-color: #f1f5f9; -fx-text-fill: #334155; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);"));
        
        bubble.getChildren().addAll(senderLabel, msgLabel);
        
        HBox row = new HBox(bubble);
        row.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        
        chatBox.getChildren().add(row);
        
        Platform.runLater(() -> chatScroll.setVvalue(1.0));
    }

    @FXML
    private void closeDialog() {
        chatInput.getScene().getWindow().hide();
    }
}
