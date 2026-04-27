package gui;

import entities.CourseCategory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.web.HTMLEditor;
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
    private Label nameErrorLabel;

    @FXML
    private TextField slugField;

    @FXML
    private HTMLEditor descriptionEditor;

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
        String description = descriptionEditor.getHtmlText();
        String slug = slugField.getText() == null || slugField.getText().isBlank() ? toSlug(name) : slugField.getText().trim();

        if (!validateForm(name)) {
            return;
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
        descriptionEditor.setHtmlText(category.getDescription() != null ? category.getDescription() : "");
    }

    private boolean validateForm(String name) {
        nameErrorLabel.setText("");
        nameErrorLabel.setVisible(false);
        nameErrorLabel.setManaged(false);

        if (name.isBlank()) {
            nameErrorLabel.setText("Category name is required.");
            nameErrorLabel.setVisible(true);
            nameErrorLabel.setManaged(true);
            AlertHelper.showError("Validation error", "Please enter a category name before saving.");
            return false;
        }

        try {
            boolean exists = courseCategoryService.existsByNom(name);
            if (exists && (category == null || !name.equalsIgnoreCase(category.getNom()))) {
                nameErrorLabel.setText("A category with this name already exists.");
                nameErrorLabel.setVisible(true);
                nameErrorLabel.setManaged(true);
                AlertHelper.showError("Validation error", "Category name must be unique.");
                return false;
            }
        } catch (SQLException exception) {
            nameErrorLabel.setText("Unable to validate category name uniqueness.");
            nameErrorLabel.setVisible(true);
            nameErrorLabel.setManaged(true);
            AlertHelper.showError("Validation error", "Unable to verify if the category name is unique.");
            return false;
        }

        return true;
    }

    private void openCategoryList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/category-list-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.getScene().setRoot(root);
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
    @javafx.fxml.FXML
    public void goToPreview(javafx.event.ActionEvent event) {
        gui.PreviewHelper.goToPreview(event);
    }

    @javafx.fxml.FXML
    public void logout(javafx.event.ActionEvent event) {
        gui.SessionHelper.logout(event);
    }
}
