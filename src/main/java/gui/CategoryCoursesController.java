package gui;

import entities.Course;
import entities.CourseCategory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import services.CourseService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CategoryCoursesController {

    private static final DateTimeFormatter DISPLAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final CourseService courseService = new CourseService();

    private CourseCategory category;
    private List<Course> courses = Collections.emptyList();
    private Map<Integer, String> categoryNames = Collections.emptyMap();

    @FXML
    private Label titleLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private VBox coursesContainer;

    @FXML
    public void initialize() {
        gui.FrontMainController.setNavbarText("Courses in Category", "Pages / Categories / Courses");
    }

    public void setCategory(CourseCategory category) {
        this.category = category;
        titleLabel.setText("Courses in " + category.getNom());
        loadCourses();
    }

    @FXML
    private void onBackToCategories() {
        openScene("/gui/category-list-view.fxml", null);
    }

    @FXML
    private void onAddCourse() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/course-edit-view.fxml"));
            Parent root = loader.load();
            CourseFormController controller = loader.getController();
            controller.setCourse(null, category);
            Stage stage = (Stage) titleLabel.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to open the course creation view.", exception);
        }
    }

    private void loadCourses() {
        if (category == null) {
            return;
        }

        try {
            categoryNames = courseService.getCategoryNames();
            courses = courseService.afficherParCategorie(category.getId());
            statusLabel.setText(courses.size() + " courses in this category.");
            renderCourses();
        } catch (SQLException exception) {
            courses = Collections.emptyList();
            statusLabel.setText("Database error: " + exception.getMessage());
            renderCourses();
        }
    }

    private void renderCourses() {
        coursesContainer.getChildren().clear();

        if (courses.isEmpty()) {
            Label emptyState = new Label("No courses found for this category.");
            emptyState.setStyle("-fx-font-size: 15px; -fx-text-fill: #6b7280;");
            coursesContainer.getChildren().add(emptyState);
            return;
        }

        for (Course course : courses) {
            coursesContainer.getChildren().add(buildCourseRow(course));
        }
    }

    private Node buildCourseRow(Course course) {
        HBox row = new HBox(18);
        row.getStyleClass().add("list-row");

        StackPane iconBox = new StackPane();
        iconBox.getStyleClass().add("sidebar-icon-box");
        iconBox.setMinWidth(42);
        iconBox.setMaxWidth(42);
        iconBox.setMinHeight(42);
        iconBox.setMaxHeight(42);

        javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView();
        imageView.setFitWidth(32);
        imageView.setFitHeight(32);
        imageView.setPreserveRatio(true);

        String imagePath = course.getImage();
        if (imagePath != null && !imagePath.isBlank()) {
            try {
                imageView.setImage(new javafx.scene.image.Image(imagePath, true));
            } catch (Exception e) {
                iconBox.getChildren().add(new Label("C"));
            }
        } else {
            iconBox.getChildren().add(new Label("C"));
        }
        iconBox.getChildren().add(imageView);

        Label title = new Label(safeText(course.getTitre()));
        title.setMinWidth(240);
        title.setWrapText(true);
        title.getStyleClass().add("card-title");

        Label description = new Label(shorten(safeText(course.getDescription()), 95));
        description.setMinWidth(430);
        description.setWrapText(true);
        description.getStyleClass().add("card-subtitle");


        Label createdAt = new Label(formatDate(course.getDateDeCreation()));
        createdAt.setMinWidth(140);
        createdAt.getStyleClass().add("card-subtitle");
        createdAt.setStyle("-fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(10);
        Button resourcesButton = createIconButton("fas-layer-group", "btn-action-dark");
        Button editButton = createIconButton("fas-edit", "btn-action-dark");
        Button deleteButton = createIconButton("fas-trash-alt", "btn-action-light");
        
        deleteButton.setStyle("-fx-text-fill: #ef4444;");

        resourcesButton.setOnAction(event -> openResources(course, row));
        editButton.setOnAction(event -> openEditForm(course, row));
        deleteButton.setOnAction(event -> {
            if (!AlertHelper.confirmDelete("course")) {
                return;
            }
            try {
                courseService.supprimer(course.getId());
                loadCourses();
                statusLabel.setText("Course deleted successfully.");
                AlertHelper.showInfo("Deleted", "Course deleted successfully.");
            } catch (SQLException exception) {
                statusLabel.setText("Delete failed: " + exception.getMessage());
                AlertHelper.showError("Delete failed", exception.getMessage());
            }
        });

        actions.getChildren().addAll(resourcesButton, editButton, deleteButton);
        row.getChildren().addAll(iconBox, title, description, createdAt, spacer, actions);
        return row;
    }

    private Button createIconButton(String iconCode, String colorClass) {
        Button button = new Button();
        FontIcon icon = new FontIcon(iconCode);
        icon.setIconSize(18);
        
        if ("btn-action-dark".equals(colorClass)) {
            icon.setIconColor(Color.WHITE);
        }
        
        button.setGraphic(icon);
        button.getStyleClass().addAll("btn-action", colorClass);
        button.setPadding(new Insets(5, 10, 5, 10));
        return button;
    }

    private Button createActionButton(String text, String colorClass) {
        Button button = new Button(text);
        button.getStyleClass().addAll("btn-action", colorClass);
        return button;
    }



    private void openEditForm(Course course, Node sourceNode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/course-edit-view.fxml"));
            Parent root = loader.load();
            CourseFormController controller = loader.getController();
            controller.setCourse(course, category);
            Stage stage = (Stage) sourceNode.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to open the course edit view.", exception);
        }
    }

    private void openResources(Course course, Node sourceNode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/resource-list-view.fxml"));
            Parent root = loader.load();
            RessourceListController controller = loader.getController();
            controller.setCourse(course);
            Stage stage = (Stage) sourceNode.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to open the resource list view.", exception);
        }
    }

    private void openScene(String resourcePath, ControllerInitializer initializer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resourcePath));
            Parent root = loader.load();
            if (initializer != null) {
                initializer.initialize(loader.getController());
            }
            Stage stage = (Stage) titleLabel.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to open view: " + resourcePath, exception);
        }
    }

    private String formatDate(String date) {
        if (date == null || date.isBlank()) {
            return "-";
        }
        try {
            return LocalDateTime.parse(date).format(DISPLAY_DATE_FORMAT);
        } catch (Exception ignored) {
            return date;
        }
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String shorten(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength - 3) + "...";
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
