package gui;

import entities.Course;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import services.CourseService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Locale;

public class CourseFormController {

    private final CourseService courseService = new CourseService();

    @FXML
    private TextField titleField;

    @FXML
    private TextField categoryField;

    @FXML
    private TextField statusField;

    @FXML
    private TextField levelField;

    @FXML
    private TextField durationField;

    @FXML
    private TextField viewsField;

    @FXML
    private TextArea descriptionArea;

    private Course course;

    public void setCourse(Course course) {
        this.course = course;
        populateForm();
    }

    @FXML
    private void onSave() {
        if (course == null) {
            return;
        }

        course.setTitre(titleField.getText());
        course.setDescription(descriptionArea.getText());
        course.setStatut(statusField.getText());
        course.setNiveau(levelField.getText());
        course.setDateDeModification(LocalDateTime.now().toString());

        try {
            course.setCategorieId(Integer.parseInt(categoryField.getText().trim()));
        } catch (NumberFormatException ignored) {
            // Keep the previous category id when the field is invalid.
        }

        try {
            course.setDureeEstimee(Integer.parseInt(durationField.getText().trim()));
        } catch (NumberFormatException ignored) {
            course.setDureeEstimee(null);
        }

        try {
            course.setViews(Integer.parseInt(viewsField.getText().trim()));
        } catch (NumberFormatException ignored) {
            course.setViews(0);
        }

        course.setSlug(toSlug(course.getTitre()));

        try {
            courseService.modifier(course);
            openCoursesPage();
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to save the course changes.", exception);
        }
    }

    @FXML
    private void onCancel() {
        openCoursesPage();
    }

    private void populateForm() {
        if (course == null) {
            return;
        }

        titleField.setText(course.getTitre());
        categoryField.setText(String.valueOf(course.getCategorieId()));
        statusField.setText(course.getStatut());
        levelField.setText(course.getNiveau());
        durationField.setText(course.getDureeEstimee() == null ? "" : String.valueOf(course.getDureeEstimee()));
        viewsField.setText(course.getViews() == null ? "" : String.valueOf(course.getViews()));
        descriptionArea.setText(course.getDescription());
    }

    private void openCoursesPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/main-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.setScene(new Scene(root, 1280, 760));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to return to the courses page.", exception);
        }
    }

    private String toSlug(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT)
                .trim()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}
