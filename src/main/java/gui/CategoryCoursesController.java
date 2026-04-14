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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
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
            stage.setScene(new Scene(root, 1280, 760));
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
            statusLabel.setText(courses.size() + " course(s) in this category.");
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
        row.setPadding(new Insets(18, 20, 18, 20));
        row.setStyle("-fx-background-color: white; -fx-background-radius: 18; "
                + "-fx-border-color: #e2e8f0; -fx-border-radius: 18;");

        StackPane iconBox = new StackPane();
        iconBox.setMinSize(42, 42);
        iconBox.setPrefSize(42, 42);
        iconBox.setMaxSize(42, 42);
        iconBox.setStyle("-fx-background-color: #e8eef8; -fx-background-radius: 12;");
        Label iconLabel = new Label("C");
        iconLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #47638a;");
        iconBox.getChildren().add(iconLabel);

        Label title = new Label(safeText(course.getTitre()));
        title.setMinWidth(240);
        title.setWrapText(true);
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #243b63;");

        Label description = new Label(shorten(safeText(course.getDescription()), 95));
        description.setMinWidth(430);
        description.setWrapText(true);
        description.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");

        Label views = new Label(String.valueOf(course.getViews() == null ? 0 : course.getViews()));
        views.setMinWidth(70);
        views.setStyle("-fx-font-size: 15px; -fx-text-fill: #334155; -fx-font-weight: bold;");

        Label createdAt = new Label(formatDate(course.getDateDeCreation()));
        createdAt.setMinWidth(140);
        createdAt.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(10);
        Button resourcesButton = createActionButton("Resources", "#355388", "#e9f0fb");
        Button editButton = createActionButton("Edit", "#355388", "#eef3fb");
        Button deleteButton = createActionButton("Delete", "#c62828", "#fdecec");

        resourcesButton.setOnAction(event -> openResources(course, row));
        editButton.setOnAction(event -> openEditForm(course, row));
        deleteButton.setOnAction(event -> {
            try {
                courseService.supprimer(course.getId());
                loadCourses();
            } catch (SQLException exception) {
                statusLabel.setText("Delete failed: " + exception.getMessage());
            }
        });

        actions.getChildren().addAll(resourcesButton, editButton, deleteButton);
        row.getChildren().addAll(iconBox, title, description, views, createdAt, spacer, actions);
        return row;
    }

    private Button createActionButton(String text, String textColor, String backgroundColor) {
        Button button = new Button(text);
        button.setStyle(commonButtonStyle());
        return button;
    }

    private String commonButtonStyle() {
        return "-fx-background-color: #3f5f98; -fx-text-fill: white; -fx-background-radius: 18; "
                + "-fx-padding: 10 18 10 18; -fx-font-size: 14px; -fx-font-weight: bold;";
    }

    private void openEditForm(Course course, Node sourceNode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/course-edit-view.fxml"));
            Parent root = loader.load();
            CourseFormController controller = loader.getController();
            controller.setCourse(course, category);
            Stage stage = (Stage) sourceNode.getScene().getWindow();
            stage.setScene(new Scene(root, 1280, 760));
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
            stage.setScene(new Scene(root, 1280, 760));
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
            stage.setScene(new Scene(root, 1280, 760));
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
}
