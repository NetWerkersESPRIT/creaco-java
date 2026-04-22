package gui;

import entities.Course;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
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

    private void loadCourses() {
        try {
            courses = courseService.afficherPublie();
            renderCourses();
        } catch (SQLException e) {
            courses = Collections.emptyList();
            coursesContainer.getChildren().clear();

            Label error = new Label("Error loading courses: " + e.getMessage());
            error.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
            coursesContainer.getChildren().add(error);
        }
    }

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

    private Node buildCourseCard(Course course) {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        card.setPrefWidth(330);
        card.setMinWidth(330);

        Label title = new Label(course.getTitre());
        title.getStyleClass().add("card-title");

        Label desc = new Label(
                course.getDescription() == null ? "-" : course.getDescription()
        );
        desc.setWrapText(true);
        desc.setPrefHeight(60);
        desc.getStyleClass().add("card-subtitle");

        Button openBtn = new Button("Open");
        openBtn.getStyleClass().add("btn-primary");
        openBtn.setPrefWidth(120);
        openBtn.setPrefHeight(40);

        openBtn.setOnAction(e -> openCourse(course));

        card.getChildren().addAll(title, desc, openBtn);

        return card;
    }

    @FXML private javafx.scene.layout.HBox previewBanner;

    public void setPreviewMode(boolean isPreview) {
        if (previewBanner != null) {
            previewBanner.setVisible(isPreview);
            previewBanner.setManaged(isPreview);
        }
    }

    @FXML
    private void exitPreview(javafx.event.ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/gui/main-view.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void openCourse(Course course) {
        System.out.println("Opening course: " + course.getTitre());
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/gui/front-resource-view.fxml"));
            javafx.scene.Parent root = loader.load();
            
            FrontResourceController controller = loader.getController();
            controller.setCourse(course);
            boolean isPrev = previewBanner != null && previewBanner.isVisible();
            controller.setPreviewMode(isPrev);
            
            javafx.stage.Stage stage = (javafx.stage.Stage) coursesContainer.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load front-resource-view.fxml: " + e.getMessage());
        }
    }

    private String safe(String value) {
        return (value == null || value.isBlank()) ? "-" : value;
    }
    @javafx.fxml.FXML
    public void logout(javafx.event.ActionEvent event) {
        gui.SessionHelper.logout(event);
    }

    @FXML
    private void onRequestHelp() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/help-request-dialog.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Mentoring Help-Desk");
            stage.setScene(new Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.show();
        } catch (java.io.IOException e) {
            AlertHelper.showError("UI Error", "Could not open Help-Desk: " + e.getMessage());
        }
    }
}