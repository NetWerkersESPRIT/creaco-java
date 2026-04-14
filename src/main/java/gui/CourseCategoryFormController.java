package gui;

import entities.CourseCategory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import services.CourseCategoryService;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Locale;

public class CourseCategoryFormController {

    private final CourseCategoryService courseCategoryService = new CourseCategoryService();
    private CourseCategory category;

    @FXML
    private Label titleLabel;

    @FXML
    private Label subtitleLabel;

    @FXML
    private TextField nameField;

    @FXML
    private TextField slugField;

    @FXML
    private TextArea descriptionArea;

    public void setCategory(CourseCategory category) {
        this.category = category;
        boolean editing = category != null;
        titleLabel.setText(editing ? "Edit Category" : "Add Category");
        subtitleLabel.setText(editing ? "Update the selected category." : "Create a new course category.");

        if (editing) {
            populateForm();
        }
    }

    @FXML
    private void onCancel() {
        openCategoryList();
    }

    @FXML
    private void onSave() {
        CourseCategory target = category == null ? new CourseCategory() : category;
        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        String description = descriptionArea.getText() == null ? "" : descriptionArea.getText().trim();
        String slug = slugField.getText() == null || slugField.getText().isBlank() ? toSlug(name) : slugField.getText().trim();

        if (name.isBlank()) {
            throw new IllegalStateException("Category name is required.");
        }

        LocalDate today = LocalDate.now();
        target.setNom(name);
        target.setDescription(description);
        target.setSlug(slug);
        target.setDeletedAt(null);

        if (target.getDateDeCreation() == null) {
            target.setDateDeCreation(Date.valueOf(today));
        }
        target.setDateDeModification(Date.valueOf(today));

        try {
            if (category == null) {
                courseCategoryService.ajouter(target);
            } else {
                courseCategoryService.modifier(target.getId(), target);
            }
            openCategoryList();
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to save category.", exception);
        }
    }

    private void populateForm() {
        nameField.setText(category.getNom());
        slugField.setText(category.getSlug());
        descriptionArea.setText(category.getDescription());
    }

    private void openCategoryList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/category-list-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(new Scene(root, 1280, 760));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to return to the category list.", exception);
        }
    }

    private String toSlug(String value) {
        return value.toLowerCase(Locale.ROOT)
                .trim()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}
