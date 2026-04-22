package gui;

import entities.HelpTicket;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import services.HelpTicketService;

import java.sql.SQLException;

public class HelpRequestController {

    @FXML private TextField subjectField;
    @FXML private ComboBox<String> priorityCombo;
    @FXML private TextArea messageArea;

    private final HelpTicketService ticketService = new HelpTicketService();
    private Integer courseId;

    @FXML
    public void initialize() {
        priorityCombo.getItems().addAll("Low", "Medium", "High");
        priorityCombo.setValue("Medium");
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    @FXML
    private void onSubmit() {
        if (subjectField.getText().isBlank() || messageArea.getText().isBlank()) {
            AlertHelper.showError("Validation Error", "Please fill in all required fields.");
            return;
        }

        HelpTicket ticket = new HelpTicket();
        ticket.setCreatorId(1); // Placeholder: Should be current logged in user ID
        ticket.setCourseId(courseId);
        ticket.setSubject(subjectField.getText().trim());
        ticket.setMessage(messageArea.getText().trim());
        ticket.setPriority(priorityCombo.getValue());

        try {
            ticketService.createTicket(ticket);
            AlertHelper.showInfo("Success", "Your assistance request has been sent to the Mentor.");
            closeWindow();
        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Unable to send request: " + e.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) subjectField.getScene().getWindow();
        stage.close();
    }
}
