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
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import services.CourseService;
import gui.post.DisplayPostController;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MainController {

    private static final DateTimeFormatter DISPLAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final CourseService courseService = new CourseService();
    private List<Course> courses = Collections.emptyList();
    private Map<Integer, String> categoryNames = Collections.emptyMap();
    private List<Node> allCourseCards = new ArrayList<>();

    @FXML private FlowPane coursesContainer;
    @FXML private TextField searchField;
    @FXML private Label pageTitleLabel;
    @FXML private Label statusLabel;
    
    @FXML private StackPane contentArea;
    @FXML private VBox dashboardView;

    @FXML
    private void initialize() {
        if (pageTitleLabel != null) pageTitleLabel.setText("Courses Dashboard");
        loadCourses();
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> filterCourses(newVal));
        }
    }

    @FXML
    private void onGoToDashboard() {
        contentArea.getChildren().setAll(dashboardView);
        if (pageTitleLabel != null) pageTitleLabel.setText("Dashboard");
    }

    @FXML
    private void onShowUsers() {
        loadSubView("/Users/Admin.fxml", "Connected Users");
    }

    @FXML
    private void onShowModeration() {
        loadSubView("/post/postModeration.fxml", "Post Moderation");
    }

    @FXML
    private void onShowIdeas() {
        loadSubView("/TSK/Idea.fxml", "Ideas");
    }

    @FXML
    private void onShowMissions() {
        loadSubView("/TSK/Mission.fxml", "Missions");
    }

    @FXML
    private void onShowTasks() {
        loadSubView("/TSK/Tasks.fxml", "Tasks");
    }

    @FXML
    private void onShowEvents() {
        System.out.println("Events section - Coming soon");
    }

    @FXML
    public void onShowForum() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/post/displayPost.fxml"));
            Parent root = loader.load();
            gui.post.DisplayPostController controller = loader.getController();
            if (controller != null) {
                controller.setAdminMode(true);
            }
            contentArea.getChildren().setAll(root);
            if (pageTitleLabel != null) pageTitleLabel.setText("Forum");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load forum: " + e.getMessage());
        }
    }

    @FXML
    private void onShowCollaborations() {
        loadSubView("/collaborator/ListCollaborator.fxml", "Collaborations");
    }

    @FXML
    private void onShowCourses() {
        onGoToDashboard();
    }

    private void loadSubView(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            if (pageTitleLabel != null) pageTitleLabel.setText(title);

            if (root instanceof javafx.scene.layout.BorderPane) {
                Node center = ((javafx.scene.layout.BorderPane) root).getCenter();
                if (center instanceof VBox) {
                    ((VBox) center).setPrefWidth(Double.MAX_VALUE);
                    ((VBox) center).setPrefHeight(Double.MAX_VALUE);
                }
                contentArea.getChildren().setAll(center);
            } else {
                contentArea.getChildren().setAll(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load sub-view: " + fxmlPath);
        }
    }

    private void saveAllCards() {
        allCourseCards = new ArrayList<>(coursesContainer.getChildren());
    }

    private void filterCourses(String keyword) {
        if (coursesContainer == null) return;
        coursesContainer.getChildren().clear();
        if (keyword == null || keyword.trim().isEmpty()) {
            coursesContainer.getChildren().addAll(allCourseCards);
        } else {
            for (Node card : allCourseCards) {
                if (card.getUserData() != null &&
                        card.getUserData().toString().toLowerCase()
                                .contains(keyword.toLowerCase())) {
                    coursesContainer.getChildren().add(card);
                }
            }
        }
    }

    @FXML
    private void onManageCategories() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/category-list-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) pageTitleLabel.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException exception) {
            System.err.println("Navigation error: " + exception.getMessage());
        }
    }

    @FXML
    private void onAddCourse() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/course-edit-view.fxml"));
            Parent root = loader.load();
            CourseFormController controller = loader.getController();
            controller.setCourse(null);
            Stage stage = (Stage) pageTitleLabel.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException exception) {
            System.err.println("Navigation error: " + exception.getMessage());
        }
    }

    private void loadCourses() {
        if (coursesContainer == null) return;
        allCourseCards.clear();
        try {
            categoryNames = courseService.getCategoryNames();
            courses = courseService.afficher();
            if (statusLabel != null) statusLabel.setText(courses.size() + " course(s) loaded.");
            renderCourses();
        } catch (SQLException exception) {
            courses = Collections.emptyList();
            categoryNames = Collections.emptyMap();
            if (statusLabel != null) statusLabel.setText("Database error.");
            renderCourses();
        }
    }

    private void renderCourses() {
        if (coursesContainer == null) return;
        coursesContainer.getChildren().clear();

        if (courses.isEmpty()) {
            Label emptyState = new Label("No courses available.");
            emptyState.setStyle("-fx-font-size: 15px; -fx-text-fill: #6b7280;");
            coursesContainer.getChildren().add(emptyState);
            saveAllCards();
            return;
        }

        for (Course course : courses) {
            Node card = buildCourseCard(course);
            card.setUserData(course.getTitre());
            coursesContainer.getChildren().add(card);
        }

        saveAllCards();
    }

    private Node buildCourseCard(Course course) {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        card.setPrefWidth(320);
        card.setMinWidth(320);

        HBox header = new HBox(15);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        StackPane iconContainer = new StackPane();
        iconContainer.getStyleClass().add("card-icon-container");
        Label iconLabel = new Label("📦"); 
        iconLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        iconContainer.getChildren().add(iconLabel);

        VBox titleBox = new VBox(2);
        Label titleLabel = new Label(course.getTitre());
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setWrapText(true);

        Label categoryLabel = new Label(resolveCategoryName(course.getCategorieId()).toUpperCase());
        categoryLabel.setStyle("-fx-text-fill: -fx-primary-pink; -fx-font-weight: bold; -fx-font-size: 10px;");

        titleBox.getChildren().addAll(titleLabel, categoryLabel);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        header.getChildren().addAll(iconContainer, titleBox);

        VBox contentBox = new VBox(6);
        Label updatedLabel = new Label("Updated " + safeText(course.getDateDeModification()));
        updatedLabel.getStyleClass().add("card-subtitle");

        Label descriptionLabel = new Label(safeText(course.getDescription()));
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMinHeight(60);
        descriptionLabel.getStyleClass().add("subtitle-label");

        contentBox.getChildren().addAll(updatedLabel, descriptionLabel);

        HBox footer = new HBox();
        footer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label exploreLink = new Label("EXPLORE RESOURCES →");
        exploreLink.getStyleClass().add("card-action-link");
        exploreLink.setStyle("-fx-text-fill: -fx-primary-pink; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand;");

        exploreLink.setOnMouseClicked(event -> {
            openRessources(course, card);
            event.consume();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        footer.getChildren().addAll(exploreLink, spacer);

        card.setOnMouseClicked(event -> openRessources(course, card));
        card.setCursor(javafx.scene.Cursor.HAND);

        card.getChildren().addAll(header, contentBox, footer);
        return card;
    }

    private void openRessources(Course course, Node sourceNode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/front-resource-view.fxml"));
            Parent root = loader.load();
            FrontResourceController controller = loader.getController();
            controller.setCourse(course);
            Stage stage = (Stage) sourceNode.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private String resolveCategoryName(int categoryId) {
        return categoryNames.getOrDefault(categoryId, "Unassigned");
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "-" : value;
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
