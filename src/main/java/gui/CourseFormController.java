package gui;

import entities.Course;
import entities.CourseCategory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TextField;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;
import services.CourseService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class CourseFormController {

    private final CourseService courseService = new CourseService();

    @FXML
    private Label titleLabel;

    @FXML
    private Label subtitleLabel;

    @FXML
    private TextField titleField;

    @FXML
    private Label titleErrorLabel;

    @FXML
    private ComboBox<String> categoryCombo;

    @FXML
    private Label categoryErrorLabel;

    @FXML
    private RadioButton publishedRadio;

    @FXML
    private RadioButton draftRadio;

    @FXML
    private ToggleGroup statusToggleGroup;

    @FXML
    private Label statusErrorLabel;

    @FXML
    private ComboBox<String> levelCombo;

    @FXML
    private Label levelErrorLabel;

    @FXML
    private TextField durationField;

    @FXML
    private Label durationErrorLabel;

    @FXML
    private HTMLEditor descriptionEditor;

    @FXML
    private javafx.scene.image.ImageView imagePreview;

    @FXML
    private TextField imagePathField;

    @FXML
    private Label noImageLabel;

    private Course course;
    private CourseCategory returnCategory;
    private final Map<String, Integer> categoryNameToId = new LinkedHashMap<>();

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
    private void initialize() {
        setupLevelCombo();
        try {
            loadCategories();
        } catch (SQLException exception) {
            // Ignore category loading errors; the form can still open
            System.err.println("Warning: Failed to load categories - " + exception.getMessage());
        }
        updateModeTexts();
    }

    private void setupLevelCombo() {
        levelCombo.getItems().setAll("Beginner", "Intermediate", "Advanced");
        levelCombo.setPromptText("Select level");
    }

    @FXML
    private void onSave() {
        if (!validateForm()) {
            return;
        }

        boolean creating = course == null;
        Course target = creating ? new Course() : course;

        target.setTitre(titleField.getText().trim());
        target.setDescription(descriptionEditor.getHtmlText());
        target.setStatut(publishedRadio.isSelected() ? "Published" : "Draft");
        target.setNiveau(levelCombo.getValue());
        target.setDateDeModification(LocalDateTime.now().toString());
        target.setImage(imagePathField.getText().trim());

        // Handle category selection
        String selectedCategory = categoryCombo.getValue();
        if (selectedCategory == null || selectedCategory.isBlank()) {
            selectedCategory = returnCategory != null ? returnCategory.getNom() : null;
        }

        Integer categoryId = categoryNameToId.get(selectedCategory);
        if (categoryId == null && returnCategory != null) {
            categoryId = returnCategory.getId();
        }
        target.setCategorieId(categoryId);

        // Handle duration
        String durationText = durationField.getText().trim();
        try {
            target.setDureeEstimee(durationText.isEmpty() ? null : Integer.parseInt(durationText));
        } catch (NumberFormatException ignored) {
            target.setDureeEstimee(null);
        }

        if (creating) {
            target.setViews(0);
            target.setDateDeCreation(LocalDateTime.now().toString());
            target.setDeletedAt(null);
        }

        target.setSlug(toSlug(target.getTitre()) + "-" + target.getCategorieId());

        try {
            if (creating) {
                courseService.ajouter(target);
            } else {
                courseService.modifier(target);
            }
            openCoursesPage();
        } catch (Exception exception) {
            AlertHelper.showError("Save Error", "Unable to save the course: " + exception.getMessage());
            exception.printStackTrace();
        }
    }

    private boolean validateForm() {
        clearValidationErrors();

        boolean valid = true;

        // Title validation
        if (titleField.getText() == null || titleField.getText().trim().isBlank()) {
            titleErrorLabel.setText("Course title is required.");
            titleErrorLabel.setVisible(true);
            titleErrorLabel.setManaged(true);
            valid = false;
        }

        // Category validation
        if (categoryCombo.getValue() == null || categoryCombo.getValue().trim().isBlank()) {
            categoryErrorLabel.setText("Please select a category for this course.");
            categoryErrorLabel.setVisible(true);
            categoryErrorLabel.setManaged(true);
            valid = false;
        }

        // Duplicate title in same category validation
        if (valid) {
            try {
                String title = titleField.getText().trim();
                String categoryName = categoryCombo.getValue();
                Integer categoryId = categoryNameToId.get(categoryName);
                
                if (categoryId != null && courseService.existeDeja(title, categoryId, course != null ? course.getId() : null)) {
                    titleErrorLabel.setText("A course with this name already exists in this category.");
                    titleErrorLabel.setVisible(true);
                    titleErrorLabel.setManaged(true);
                    valid = false;
                }
            } catch (SQLException e) {
                System.err.println("Error checking for duplicate course: " + e.getMessage());
            }
        }

        // Status validation
        if (!publishedRadio.isSelected() && !draftRadio.isSelected()) {
            statusErrorLabel.setText("Please choose Published or Draft.");
            statusErrorLabel.setVisible(true);
            statusErrorLabel.setManaged(true);
            valid = false;
        }

        // Level validation
        if (levelCombo.getValue() == null || levelCombo.getValue().trim().isBlank()) {
            levelErrorLabel.setText("Please select a level for this course.");
            levelErrorLabel.setVisible(true);
            levelErrorLabel.setManaged(true);
            valid = false;
        }

        // Duration validation (optional but must be valid number if provided)
        String durationText = durationField.getText().trim();
        if (!durationText.isBlank()) {
            try {
                Integer.parseInt(durationText);
            } catch (NumberFormatException e) {
                durationErrorLabel.setText("Duration must be a whole number.");
                durationErrorLabel.setVisible(true);
                durationErrorLabel.setManaged(true);
                valid = false;
            }
        }

        if (!valid) {
            AlertHelper.showError("Validation error", "Please correct the highlighted fields before saving.");
        }

        return valid;
    }

    private void clearValidationErrors() {
        // Title
        titleErrorLabel.setText("");
        titleErrorLabel.setVisible(false);
        titleErrorLabel.setManaged(false);

        // Category
        categoryErrorLabel.setText("");
        categoryErrorLabel.setVisible(false);
        categoryErrorLabel.setManaged(false);

        // Status
        statusErrorLabel.setText("");
        statusErrorLabel.setVisible(false);
        statusErrorLabel.setManaged(false);

        // Level
        levelErrorLabel.setText("");
        levelErrorLabel.setVisible(false);
        levelErrorLabel.setManaged(false);

        // Duration
        durationErrorLabel.setText("");
        durationErrorLabel.setVisible(false);
        durationErrorLabel.setManaged(false);
    }

    @FXML
    private void onCancel() {
        openCoursesPage();
    }

    @FXML
    private void onUploadImage() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Select Course Image");
        fileChooser.getExtensionFilters().addAll(
            new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        java.io.File selectedFile = fileChooser.showOpenDialog(imagePreview.getScene().getWindow());
        if (selectedFile != null) {
            String path = selectedFile.toURI().toString();
            imagePathField.setText(path);
            updateImagePreview(path);
        }
    }

    private void updateImagePreview(String imagePath) {
        if (imagePath != null && !imagePath.isBlank()) {
            try {
                javafx.scene.image.Image img = new javafx.scene.image.Image(imagePath, true);
                imagePreview.setImage(img);
                noImageLabel.setVisible(false);
            } catch (Exception e) {
                imagePreview.setImage(null);
                noImageLabel.setVisible(true);
            }
        } else {
            imagePreview.setImage(null);
            noImageLabel.setVisible(true);
        }
    }

    private void loadCategories() throws SQLException {
        categoryNameToId.clear();
        categoryCombo.getItems().clear();

        Map<Integer, String> categories = courseService.getCategoryNames();
        for (Map.Entry<Integer, String> entry : categories.entrySet()) {
            categoryNameToId.put(entry.getValue(), entry.getKey());
        }
        categoryCombo.getItems().addAll(categoryNameToId.keySet());
    }

    private void populateForm() {
        if (returnCategory != null) {
            categoryCombo.setValue(returnCategory.getNom());
        }

        if (course == null) {
            // New course
            publishedRadio.setSelected(true);
            levelCombo.setValue(null);           // Shows prompt text "Select level"
            durationField.clear();
            descriptionEditor.setHtmlText("");
            titleField.clear();
            return;
        }

        // Editing existing course
        titleField.setText(course.getTitre() != null ? course.getTitre() : "");
        descriptionEditor.setHtmlText(course.getDescription() != null ? course.getDescription() : "");

        // Set category
        String selectedCategory = categoryNameToId.entrySet().stream()
                .filter(entry -> entry.getValue().equals(course.getCategorieId()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
        categoryCombo.setValue(selectedCategory);

        // Set status
        if ("Draft".equalsIgnoreCase(course.getStatut())) {
            draftRadio.setSelected(true);
        } else {
            publishedRadio.setSelected(true);
        }

        // Set level
        levelCombo.setValue(course.getNiveau() != null && !course.getNiveau().isBlank()
                ? course.getNiveau()
                : null);

        // Set duration
        durationField.setText(course.getDureeEstimee() == null ? "" : String.valueOf(course.getDureeEstimee()));
        
        // Set image
        imagePathField.setText(course.getImage() != null ? course.getImage() : "");
        updateImagePreview(course.getImage());
    }

    private void openCoursesPage() {
        try {
            FXMLLoader loader;
            if (returnCategory != null) {
                loader = new FXMLLoader(getClass().getResource("/gui/category-courses-view.fxml"));
            } else {
                loader = new FXMLLoader(getClass().getResource("/gui/admin-courses-view.fxml"));
            }

            Parent root = loader.load();

            if (returnCategory != null) {
                CategoryCoursesController controller = loader.getController();
                controller.setCategory(returnCategory);
            }

            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.getScene().setRoot(root);

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
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
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
