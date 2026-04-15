package gui;

import entities.CourseCategory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.CourseCategoryService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class CourseCategoryListController {

    private static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final CourseCategoryService courseCategoryService = new CourseCategoryService();
    private List<CourseCategory> categories = Collections.emptyList();

    @FXML
    private Label titleLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private VBox categoriesContainer;

    @FXML
    private void initialize() {
        titleLabel.setText("Course Categories");
        loadCategories();
    }

    @FXML
    private void onBackToCourses() {
        openScene("/gui/main-view.fxml", null);
    }

    @FXML
    private void onAddCategory() {
        openScene("/gui/category-edit-form-view.fxml", controller -> {
            CourseCategoryFormController formController = (CourseCategoryFormController) controller;
            formController.setCategory(null);
        });
    }

    private void loadCategories() {
        try {
            categories = courseCategoryService.afficher();
            statusLabel.setText(categories.size() + " category(ies) loaded from creaco.");
            renderCategories();
        } catch (SQLException exception) {
            categories = Collections.emptyList();
            statusLabel.setText("Database error: " + exception.getMessage());
            renderCategories();
        }
    }

    private void renderCategories() {
        categoriesContainer.getChildren().clear();

        if (categories.isEmpty()) {
            Label emptyState = new Label("No categories available.");
            emptyState.setStyle("-fx-font-size: 15px; -fx-text-fill: #6b7280;");
            categoriesContainer.getChildren().add(emptyState);
            return;
        }

        for (CourseCategory category : categories) {
            categoriesContainer.getChildren().add(buildCategoryRow(category));
        }
    }

    private Node buildCategoryRow(CourseCategory category) {
        HBox row = new HBox(18);
        row.setPadding(new Insets(18, 20, 18, 20));
        row.setStyle("-fx-background-color: white; -fx-background-radius: 18; "
                + "-fx-border-color: #e2e8f0; -fx-border-radius: 18;");

        VBox nameBox = new VBox(8);
        nameBox.setMinWidth(260);
        Label nameLabel = new Label(safeText(category.getNom()));
        nameLabel.setWrapText(true);
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #243b63;");
        Label slugLabel = new Label("Slug: " + safeText(category.getSlug()));
        slugLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        nameBox.getChildren().addAll(nameLabel, slugLabel);

        Label descriptionLabel = new Label(safeText(category.getDescription()));
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMinWidth(360);
        descriptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #475569;");

        VBox dateBox = new VBox(8);
        dateBox.setMinWidth(150);
        Label createdLabel = new Label(formatDate(category.getDateDeCreation()));
        createdLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #334155; -fx-font-weight: bold;");
        Label updatedLabel = new Label("Updated " + formatDate(category.getDateDeModification()));
        updatedLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        dateBox.getChildren().addAll(createdLabel, updatedLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actionsBox = new HBox(10);
        Button coursesButton = createActionButton("Courses", "#1d4ed8", "#dbeafe");
        Button editButton = createActionButton("Edit", "#1d4ed8", "#dbeafe");
        Button deleteButton = createActionButton("Delete", "#b91c1c", "#fee2e2");

        coursesButton.setOnAction(event -> openScene("/gui/category-courses-view.fxml", controller -> {
            CategoryCoursesController coursesController = (CategoryCoursesController) controller;
            coursesController.setCategory(category);
        }));
        editButton.setOnAction(event -> openScene("/gui/category-edit-form-view.fxml", controller -> {
            CourseCategoryFormController formController = (CourseCategoryFormController) controller;
            formController.setCategory(category);
        }));
        deleteButton.setOnAction(event -> {
            if (!AlertHelper.confirmDelete("category")) {
                return;
            }
            try {
                courseCategoryService.supprimer(category.getId());
                loadCategories();
                statusLabel.setText("Category deleted successfully.");
                AlertHelper.showInfo("Deleted", "Category deleted successfully.");
            } catch (SQLException exception) {
                statusLabel.setText("Delete failed: " + exception.getMessage());
                AlertHelper.showError("Delete failed", exception.getMessage());
            }
        });

        actionsBox.getChildren().addAll(coursesButton, editButton, deleteButton);
        row.getChildren().addAll(nameBox, descriptionLabel, dateBox, spacer, actionsBox);
        return row;
    }

    private Button createActionButton(String text, String textColor, String backgroundColor) {
        Button button = new Button(text);
        button.setStyle(commonButtonStyle(textColor, backgroundColor));
        button.setMinWidth(90);
        return button;
    }

    private String commonButtonStyle(String textColor, String backgroundColor) {
        return "-fx-background-color: " + backgroundColor + "; -fx-text-fill: " + textColor + ";"
                + " -fx-background-radius: 22; -fx-padding: 10 20 10 20; -fx-font-size: 14px;"
                + " -fx-font-weight: bold; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 8, 0, 0, 2);";
    }

    private void openScene(String resourcePath, ControllerInitializer initializer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resourcePath));
            Parent root = loader.load();
            if (initializer != null) {
                initializer.initialize(loader.getController());
            }
            Stage stage = (Stage) titleLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 1280, 760));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to open view: " + resourcePath, exception);
        }
    }

    private String formatDate(java.sql.Date date) {
        if (date == null) {
            return "-";
        }
        return DISPLAY_DATE_FORMAT.format(LocalDate.parse(date.toString()));
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    @FunctionalInterface
    private interface ControllerInitializer {
        void initialize(Object controller);
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
