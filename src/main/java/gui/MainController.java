package gui;

import entities.Course;
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

public class MainController {

    private static final DateTimeFormatter DISPLAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final CourseService courseService = new CourseService();
    private List<Course> courses = Collections.emptyList();
    private Map<Integer, String> categoryNames = Collections.emptyMap();

    @FXML
    private VBox coursesContainer;

    @FXML
    private Label pageTitleLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private void initialize() {
        pageTitleLabel.setText("Courses");
        loadCourses();
    }

    @FXML
    private void onManageCategories() {
        // Placeholder: category management page will be connected later.
    }

    private void loadCourses() {
        try {
            categoryNames = courseService.getCategoryNames();
            courses = courseService.afficher();
            statusLabel.setText(courses.size() + " course(s) loaded from creaco.");
            renderCourses();
        } catch (SQLException exception) {
            courses = Collections.emptyList();
            categoryNames = Collections.emptyMap();
            statusLabel.setText("Database error: " + exception.getMessage());
            renderCourses();
        }
    }

    private void renderCourses() {
        coursesContainer.getChildren().clear();

        if (courses.isEmpty()) {
            Label emptyState = new Label("No courses available.");
            emptyState.setStyle("-fx-font-size: 15px; -fx-text-fill: #6b7280;");
            coursesContainer.getChildren().add(emptyState);
            return;
        }

        for (Course course : courses) {
            coursesContainer.getChildren().add(buildCourseCard(course));
        }
    }

    private Node buildCourseCard(Course course) {
        HBox row = new HBox(18);
        row.setPadding(new Insets(18, 20, 18, 20));
        row.setStyle("-fx-background-color: white; -fx-background-radius: 18; "
                + "-fx-border-color: #dbe4f0; -fx-border-radius: 18;");

        StackPane imagePlaceholder = new StackPane();
        imagePlaceholder.setMinSize(58, 58);
        imagePlaceholder.setPrefSize(58, 58);
        imagePlaceholder.setMaxSize(58, 58);
        imagePlaceholder.setStyle("-fx-background-color: #e8eef8; -fx-background-radius: 14;");
        Label imageLabel = new Label("IMG");
        imageLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #47638a;");
        imagePlaceholder.getChildren().add(imageLabel);

        VBox titleBox = new VBox(8);
        titleBox.setMinWidth(180);
        Label titleLabel = new Label(course.getTitre());
        titleLabel.setWrapText(true);
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #243b63;");
        Label categoryLabel = new Label(resolveCategoryName(course.getCategorieId()));
        categoryLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #64748b;");
        titleBox.getChildren().addAll(titleLabel, categoryLabel);

        VBox descriptionBox = new VBox(8);
        descriptionBox.setMinWidth(270);
        Label updatedLabel = new Label("Updated " + safeText(course.getDateDeModification()));
        updatedLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
        Label descriptionLabel = new Label(safeText(course.getDescription()));
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #475569;");
        descriptionBox.getChildren().addAll(updatedLabel, descriptionLabel);

        VBox viewsBox = buildInfoBox("Views", String.valueOf(course.getViews() == null ? 0 : course.getViews()));
        VBox durationBox = buildInfoBox("Duration", formatDuration(course.getDureeEstimee()));
        VBox statusBox = buildInfoBox("Status", safeText(course.getStatut()));
        VBox dateBox = buildInfoBox("Last update", formatDate(course.getDateDeModification()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox actionsBox = new VBox(10);
        actionsBox.setMinWidth(120);
        Button resourcesButton = createActionButton("Ressources", "#355388", "#e9f0fb");
        Button editButton = createActionButton("Edit", "#355388", "#eef3fb");
        Button deleteButton = createActionButton("Delete", "#c62828", "#fdecec");

        resourcesButton.setOnAction(event -> {
            // Placeholder: resources screen will be connected later.
        });
        editButton.setOnAction(event -> openEditForm(course, row));
        deleteButton.setOnAction(event -> {
            try {
                courseService.supprimer(course.getId());
                loadCourses();
            } catch (SQLException exception) {
                statusLabel.setText("Delete failed: " + exception.getMessage());
            }
        });

        actionsBox.getChildren().addAll(resourcesButton, editButton, deleteButton);

        row.getChildren().addAll(imagePlaceholder, titleBox, descriptionBox, viewsBox,
                durationBox, statusBox, dateBox, spacer, actionsBox);
        return row;
    }

    private VBox buildInfoBox(String labelText, String valueText) {
        VBox box = new VBox(8);
        box.setMinWidth(105);

        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8; -fx-font-weight: bold;");

        Label value = new Label(valueText);
        value.setWrapText(true);
        value.setStyle("-fx-font-size: 15px; -fx-text-fill: #334155; -fx-font-weight: bold;");

        box.getChildren().addAll(label, value);
        return box;
    }

    private Button createActionButton(String text, String textColor, String backgroundColor) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setStyle("-fx-background-color: " + backgroundColor + "; -fx-text-fill: " + textColor
                + "; -fx-background-radius: 12; -fx-padding: 10 16 10 16; -fx-font-weight: bold;");
        return button;
    }

    private void openEditForm(Course course, Node sourceNode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/course-edit-view.fxml"));
            Parent root = loader.load();

            CourseFormController controller = loader.getController();
            controller.setCourse(course);

            Stage stage = (Stage) sourceNode.getScene().getWindow();
            stage.setScene(new Scene(root, 1280, 760));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to open the course edit view.", exception);
        }
    }

    private String resolveCategoryName(int categoryId) {
        return categoryNames.getOrDefault(categoryId, "Unassigned");
    }

    private String formatDuration(Integer duration) {
        if (duration == null || duration <= 0) {
            return "-";
        }
        return duration + " min";
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
}
