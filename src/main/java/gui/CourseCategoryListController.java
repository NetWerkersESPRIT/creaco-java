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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
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
    private VBox categoriesContainer;

    @FXML
    private void initialize() {
        loadCategories();
    }

    @FXML
    private void onBackToCourses() {
        if (gui.FrontMainController.getInstance() != null) {
            gui.FrontMainController.getInstance().onShowCourses();
        } else {
            openScene("/gui/admin-courses-view.fxml", null);
        }
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
            renderCategories();
        } catch (SQLException exception) {
            categories = Collections.emptyList();
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

        // Add the "Add New Category" card first
        categoriesContainer.getChildren().add(buildAddCategoryRow());

        for (CourseCategory category : categories) {
            categoriesContainer.getChildren().add(buildCategoryRow(category));
        }
    }

    private Node buildAddCategoryRow() {
        StackPane row = new StackPane();
        row.getStyleClass().add("list-row");
        row.setPadding(new Insets(25));
        row.setStyle("-fx-background-color: #f8fafc; -fx-border-style: dashed; -fx-border-color: #cbd5e1; -fx-border-width: 2; -fx-background-radius: 15; -fx-border-radius: 15; -fx-cursor: hand;");

        VBox content = new VBox(5);
        content.setAlignment(javafx.geometry.Pos.CENTER);
        
        Label plusLabel = new Label();
        FontIcon plusIcon = new FontIcon("fas-plus");
        plusIcon.setIconSize(32);
        plusIcon.setIconColor(javafx.scene.paint.Color.web("#ce2d7c"));
        plusLabel.setGraphic(plusIcon);
        
        Label textLabel = new Label("Add a new course category");
        textLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #475569;");
        
        content.getChildren().addAll(plusLabel, textLabel);
        row.getChildren().add(content);
        StackPane.setAlignment(content, javafx.geometry.Pos.CENTER);

        row.setOnMouseClicked(event -> onAddCategory());
        
        // Hover effect
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #f1f5f9; -fx-border-style: dashed; -fx-border-color: -fx-primary-pink; -fx-border-width: 2; -fx-background-radius: 15; -fx-border-radius: 15; -fx-cursor: hand;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: #f8fafc; -fx-border-style: dashed; -fx-border-color: #cbd5e1; -fx-border-width: 2; -fx-background-radius: 15; -fx-border-radius: 15; -fx-cursor: hand;"));

        return row;
    }

    private Node buildCategoryRow(CourseCategory category) {
        HBox row = new HBox(18);
        row.getStyleClass().add("list-row");

        VBox nameBox = new VBox(8);
        nameBox.setPrefWidth(260);
        nameBox.setMinWidth(260);
        nameBox.setMaxWidth(260);
        Label nameLabel = new Label(safeText(category.getNom()));
        nameLabel.setWrapText(true);
        nameLabel.getStyleClass().add("card-title");
        Label slugLabel = new Label("Slug: " + safeText(category.getSlug()));
        slugLabel.getStyleClass().add("card-subtitle");
        nameBox.getChildren().addAll(nameLabel, slugLabel);

        Label descriptionLabel = new Label(stripHtmlTags(safeText(category.getDescription())));
        descriptionLabel.setWrapText(true);
        descriptionLabel.setPrefWidth(360);
        descriptionLabel.setMinWidth(360);
        descriptionLabel.setMaxWidth(360);
        descriptionLabel.getStyleClass().add("card-subtitle");

        VBox dateBox = new VBox(8);
        dateBox.setPrefWidth(150);
        dateBox.setMinWidth(150);
        dateBox.setMaxWidth(150);
        Label createdLabel = new Label(formatDate(category.getDateDeCreation()));
        createdLabel.getStyleClass().add("card-title");
        createdLabel.setStyle("-fx-font-size: 14px;"); // Slight override for date font size
        Label updatedLabel = new Label("Updated " + formatDate(category.getDateDeModification()));
        updatedLabel.getStyleClass().add("card-subtitle");
        dateBox.getChildren().addAll(createdLabel, updatedLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actionsBox = new HBox(10);
        actionsBox.setPrefWidth(120); // Give actions a fixed space too
        actionsBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        
        Button editButton = createIconButton("fas-edit", "btn-action-dark");
        Button deleteButton = createIconButton("fas-trash-alt", "btn-action-light");

        editButton.setOnAction(event -> openScene("/gui/category-edit-form-view.fxml", controller -> {
            CourseCategoryFormController formController = (CourseCategoryFormController) controller;
            formController.setCategory(category);
        }));
        deleteButton.setOnAction(event -> {
            if (!gui.util.AlertHelper.confirmDelete("category")) {
                return;
            }
            try {
                courseCategoryService.supprimer(category.getId());
                loadCategories();
                gui.util.AlertHelper.showInfo("Deleted", "Category deleted successfully.");
            } catch (SQLException exception) {
                gui.util.AlertHelper.showError("Delete failed", exception.getMessage());
            }
        });

        actionsBox.getChildren().addAll(editButton, deleteButton);
        row.getChildren().addAll(nameBox, descriptionLabel, dateBox, spacer, actionsBox);
        return row;
    }

    private Button createIconButton(String iconLiteral, String colorClass) {
        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(18);
        
        Button button = new Button();
        button.setGraphic(icon);
        button.getStyleClass().add("btn-standard");
        
        if ("btn-action-dark".equals(colorClass)) {
            button.getStyleClass().add("btn-standard-pink");
            icon.setIconColor(javafx.scene.paint.Color.WHITE);
        } else {
            button.getStyleClass().add("btn-standard-grey");
            icon.setIconColor(javafx.scene.paint.Color.web("#475569"));
        }
        
        button.setMinWidth(44);
        button.setMinHeight(44);
        button.setMaxWidth(44);
        button.setMaxHeight(44);
        
        return button;
    }



    private void openScene(String resourcePath, ControllerInitializer initializer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resourcePath));
            Parent root = loader.load();
            if (initializer != null) {
                initializer.initialize(loader.getController());
            }
            if (gui.FrontMainController.getInstance() != null) {
                gui.FrontMainController.getInstance().setContent(root);
            } else {
                Stage stage = (Stage) categoriesContainer.getScene().getWindow();
                stage.getScene().setRoot(root);
            }
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

    private String stripHtmlTags(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]*>", "").replace("&nbsp;", " ").trim();
    }

    @FunctionalInterface
    private interface ControllerInitializer {
        void initialize(Object controller);
    }
    @javafx.fxml.FXML
    public void logout(javafx.event.ActionEvent event) {
        gui.SessionHelper.logout(event);
    }
}
