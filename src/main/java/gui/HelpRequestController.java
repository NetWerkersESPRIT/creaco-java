package gui;

import entities.Course;
import entities.HelpTicket;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import services.CourseService;
import services.HelpTicketService;

import java.sql.SQLException;
import java.util.List;

public class HelpRequestController {

    @FXML private TextField subjectField;
    @FXML private ComboBox<String> priorityCombo;
    @FXML private ComboBox<Course> courseCombo;
    @FXML private TextArea messageArea;

    private final HelpTicketService ticketService = new HelpTicketService();
    private final CourseService courseService = new CourseService();
    private Integer initialCourseId;
    private HelpTicket existingTicket;
    @FXML private Label titleLabel;

    @FXML
    public void initialize() {
        priorityCombo.getItems().addAll("Low", "Medium", "High");
        priorityCombo.setValue("Medium");

        setupCourseCombo();
    }

    private void setupCourseCombo() {
        try {
            List<Course> courses = courseService.afficherPublie();
            courseCombo.getItems().addAll(courses);
            
            courseCombo.setConverter(new StringConverter<Course>() {
                @Override
                public String toString(Course course) {
                    return course == null ? "" : course.getTitre();
                }

                @Override
                public Course fromString(String string) {
                    return courseCombo.getItems().stream()
                        .filter(c -> c.getTitre().equals(string))
                        .findFirst().orElse(null);
                }
            });

            // Search logic
            courseCombo.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null || newVal.isEmpty()) {
                    courseCombo.setItems(javafx.collections.FXCollections.observableArrayList(courses));
                } else {
                    List<Course> filtered = courses.stream()
                        .filter(c -> c.getTitre().toLowerCase().contains(newVal.toLowerCase()))
                        .toList();
                    courseCombo.setItems(javafx.collections.FXCollections.observableArrayList(filtered));
                    courseCombo.show();
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setCourseId(Integer courseId) {
        this.initialCourseId = courseId;
        selectCourseInCombo(courseId);
    }

    public void setTicket(HelpTicket ticket) {
        this.existingTicket = ticket;
        if (ticket != null) {
            titleLabel.setText("Edit Assistance Request");
            subjectField.setText(ticket.getSubject());
            messageArea.setText(ticket.getMessage());
            priorityCombo.setValue(ticket.getPriority());
            selectCourseInCombo(ticket.getCourseId());
        }
    }

    private void selectCourseInCombo(Integer courseId) {
        if (courseId != null) {
            courseCombo.getItems().stream()
                .filter(c -> c.getId() == courseId)
                .findFirst()
                .ifPresent(courseCombo::setValue);
        }
    }

    @FXML
    private void onSubmit() {
        if (subjectField.getText().isBlank() || messageArea.getText().isBlank()) {
            AlertHelper.showError("Validation Error", "Please fill in all required fields.");
            return;
        }

        HelpTicket ticket = (existingTicket != null) ? existingTicket : new HelpTicket();
        ticket.setCreatorId(1); // Placeholder
        
        Course selected = courseCombo.getValue();
        if (selected != null) {
            ticket.setCourseId(selected.getId());
        } else {
            ticket.setCourseId(initialCourseId);
        }

        ticket.setSubject(subjectField.getText().trim());
        ticket.setMessage(messageArea.getText().trim());
        ticket.setPriority(priorityCombo.getValue());

        try {
            if (existingTicket == null) {
                ticketService.createTicket(ticket);
                AlertHelper.showInfo("Success", "Your assistance request has been sent to the Mentor.");
            } else {
                ticketService.updateTicket(ticket);
                AlertHelper.showInfo("Success", "Your assistance request has been updated.");
            }
            closeWindow();
        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Unable to save request: " + e.getMessage());
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
