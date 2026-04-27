package gui;

import entities.Course;
import entities.HelpTicket;
import entities.Users;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;
import services.CourseService;
import services.HelpTicketService;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class HelpTicketFormController {

    @FXML private Label formTitle;
    @FXML private TextField subjectField;
    @FXML private ComboBox<Course> courseComboBox;
    @FXML private ComboBox<String> priorityComboBox;
    @FXML private HTMLEditor messageEditor;

    private final HelpTicketService ticketService = new HelpTicketService();
    private final CourseService courseService = new CourseService();
    private HelpTicket existingTicket;

    @FXML
    public void initialize() {
        // Setup Priority
        priorityComboBox.setItems(FXCollections.observableArrayList("Basse", "Moyenne", "Haute", "Urgent"));
        priorityComboBox.setValue("Moyenne");

        // Setup Courses
        try {
            List<Course> courses = courseService.afficherPublie();
            courseComboBox.setItems(FXCollections.observableArrayList(courses));
            
            // Custom cell factory to show course titles
            courseComboBox.setCellFactory(lv -> new ListCell<Course>() {
                @Override
                protected void updateItem(Course item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getTitre());
                }
            });
            courseComboBox.setButtonCell(new ListCell<Course>() {
                @Override
                protected void updateItem(Course item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getTitre());
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setTicket(HelpTicket t) {
        this.existingTicket = t;
        if (t != null) {
            formTitle.setText("Edit Help Request");
            subjectField.setText(t.getSubject());
            messageEditor.setHtmlText(t.getMessage());
            priorityComboBox.setValue(t.getPriority());
            
            if (t.getCourseId() != null) {
                for (Course c : courseComboBox.getItems()) {
                    if (c.getId() == t.getCourseId()) {
                        courseComboBox.setValue(c);
                        break;
                    }
                }
            }
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String subject = subjectField.getText().trim();
        String message = messageEditor.getHtmlText();
        String priority = priorityComboBox.getValue();
        Course selectedCourse = courseComboBox.getValue();

        if (subject.isEmpty() || message.isEmpty()) {
            showAlert("Error", "Please fill in both subject and message.");
            return;
        }

        Users user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        try {
            if (existingTicket == null) {
                HelpTicket t = new HelpTicket();
                t.setCreatorId(user.getId());
                t.setSubject(subject);
                t.setMessage(message);
                t.setPriority(priority);
                if (selectedCourse != null) t.setCourseId(selectedCourse.getId());
                ticketService.add(t);
            } else {
                existingTicket.setSubject(subject);
                existingTicket.setMessage(message);
                existingTicket.setPriority(priority);
                if (selectedCourse != null) existingTicket.setCourseId(selectedCourse.getId());
                ticketService.update(existingTicket);
            }
            
            goBack(event);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Could not save the ticket.");
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        goBack(event);
    }

    private void goBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/front-courses-grid-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        gui.util.AlertHelper.showCustomAlert(title, content, gui.util.AlertHelper.AlertType.ERROR);
    }
}
