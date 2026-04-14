package gui;

import entities.Course;
import entities.CourseCategory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
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
    private Label titleLabel;

    @FXML
    private Label subtitleLabel;

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
    private CourseCategory returnCategory;

    public void setCourse(Course course) {
        this.course = course;
        this.returnCategory = null;
        updateModeTexts();
        populateForm();
    }

    public void setCourse(Course course, CourseCategory returnCategory) {
        this.course = course;
        this.returnCategory = returnCategory;
        updateModeTexts();
        populateForm();
    }

    @FXML
    private void onSave() {
        boolean creating = course == null;
        Course target = creating ? new Course() : course;

        target.setTitre(titleField.getText());
        target.setDescription(descriptionArea.getText());
        target.setStatut(statusField.getText());
        target.setNiveau(levelField.getText());
        target.setDateDeModification(LocalDateTime.now().toString());
        target.setImage(target.getImage());

        try {
            target.setCategorieId(Integer.parseInt(categoryField.getText().trim()));
        } catch (NumberFormatException ignored) {
            if (returnCategory != null) {
                target.setCategorieId(returnCategory.getId());
            }
        }

        try {
            target.setDureeEstimee(Integer.parseInt(durationField.getText().trim()));
        } catch (NumberFormatException ignored) {
            target.setDureeEstimee(null);
        }

        try {
            target.setViews(Integer.parseInt(viewsField.getText().trim()));
        } catch (NumberFormatException ignored) {
            target.setViews(0);
        }

        target.setSlug(toSlug(target.getTitre()));
        target.setImage(target.getImage() == null ? "" : target.getImage());
        if (creating) {
            target.setDateDeCreation(LocalDateTime.now().toString());
            target.setDeletedAt(null);
        }

        try {
            if (creating) {
                courseService.ajouter(target);
            } else {
                courseService.modifier(target);
            }
            openCoursesPage();
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to save the course.", exception);
        }
    }

    @FXML
    private void onCancel() {
        openCoursesPage();
    }

    private void populateForm() {
        if (returnCategory != null) {
            categoryField.setText(String.valueOf(returnCategory.getId()));
        }

        if (course == null) {
            statusField.setText("Published");
            levelField.setText("Intermediate");
            viewsField.setText("0");
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
            FXMLLoader loader;
            if (returnCategory != null) {
                loader = new FXMLLoader(getClass().getResource("/gui/category-courses-view.fxml"));
            } else {
                loader = new FXMLLoader(getClass().getResource("/gui/main-view.fxml"));
            }
            Parent root = loader.load();
            if (returnCategory != null) {
                CategoryCoursesController controller = loader.getController();
                controller.setCategory(returnCategory);
            }
            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.setScene(new Scene(root, 1280, 760));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to return to the courses page.", exception);
        }
    }

    private void updateModeTexts() {
        if (titleLabel == null || subtitleLabel == null) {
            return;
        }

        boolean creating = course == null;
        titleLabel.setText(creating ? "Add Course" : "Edit Course");
        if (creating && returnCategory != null) {
            subtitleLabel.setText("Create a new course in " + returnCategory.getNom() + ".");
        } else if (creating) {
            subtitleLabel.setText("Create a new course.");
        } else {
            subtitleLabel.setText("Update the selected course.");
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
