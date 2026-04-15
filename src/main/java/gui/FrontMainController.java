package gui;

import entities.Course;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import services.CourseService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FrontMainController {

    private final CourseService courseService = new CourseService();

    private List<Course> courses = Collections.emptyList();
    private List<Node> allCourseCards = new ArrayList<>();

    @FXML private TilePane coursesContainer;
    @FXML private TextField searchField;

    @FXML
    private void initialize() {
        loadCourses();

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterCourses(newVal);
        });
    }

    // ===================== LOAD =====================
    private void loadCourses() {
        try {
            courses = courseService.afficher();
            renderCourses();
        } catch (SQLException e) {
            courses = Collections.emptyList();
            coursesContainer.getChildren().clear();

            Label error = new Label("Error loading courses: " + e.getMessage());
            error.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
            coursesContainer.getChildren().add(error);
        }
    }

    // ===================== RENDER =====================
    private void renderCourses() {
        coursesContainer.getChildren().clear();
        allCourseCards.clear();

        if (courses.isEmpty()) {
            Label empty = new Label("No courses available.");
            empty.setStyle("-fx-text-fill: #64748b; -fx-font-size: 16px;");
            coursesContainer.getChildren().add(empty);
            return;
        }

        for (Course course : courses) {
            Node card = buildCourseCard(course);
            card.setUserData(course.getTitre().toLowerCase());
            coursesContainer.getChildren().add(card);
            allCourseCards.add(card);
        }
    }

    // ===================== FILTER =====================
    private void filterCourses(String keyword) {
        coursesContainer.getChildren().clear();

        if (keyword == null || keyword.trim().isEmpty()) {
            coursesContainer.getChildren().addAll(allCourseCards);
            return;
        }

        String lower = keyword.toLowerCase();

        for (Node card : allCourseCards) {
            if (card.getUserData() != null &&
                    card.getUserData().toString().contains(lower)) {
                coursesContainer.getChildren().add(card);
            }
        }
    }

    // ===================== CARD UI =====================
    private Node buildCourseCard(Course course) {

        VBox card = new VBox(10);
        card.setPrefWidth(280);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 16;" +
                        "-fx-border-color: #dbe4f0;" +
                        "-fx-border-radius: 16;"
        );

        Label title = new Label(course.getTitre());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #243b63;");

        Label desc = new Label(
                course.getDescription() == null ? "-" : course.getDescription()
        );
        desc.setWrapText(true);
        desc.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px;");

        Button openBtn = new Button("Open");
        openBtn.setStyle(
                "-fx-background-color: #1d4ed8;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 6 12;"
        );

        openBtn.setOnAction(e -> openCourse(course));

        card.getChildren().addAll(title, desc, openBtn);

        return card;
    }

    // ===================== ACTION =====================
    private void openCourse(Course course) {
        System.out.println("Opening course: " + course.getTitre());
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/gui/front-resource-view.fxml"));
            javafx.scene.Parent root = loader.load();
            
            FrontResourceController controller = loader.getController();
            controller.setCourse(course);
            
            javafx.stage.Stage stage = (javafx.stage.Stage) coursesContainer.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load front-resource-view.fxml: " + e.getMessage());
        }
    }

    // ===================== UTIL =====================
    private String safe(String value) {
        return (value == null || value.isBlank()) ? "-" : value;
    }
}