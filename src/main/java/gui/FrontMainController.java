package gui;

import entities.Course;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import services.CourseService;
import utils.SessionManager;
import entities.Users;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FrontMainController {

    private final CourseService courseService = new CourseService();

    private List<Course> courses = Collections.emptyList();
    private Map<Integer, String> categoryNames = Collections.emptyMap();
    private List<Node> allCourseCards = new ArrayList<>();

    @FXML private FlowPane coursesContainer;
    @FXML private TextField searchField;
    @FXML private StackPane contentArea;
    @FXML private VBox dashboardView;
    @FXML private javafx.scene.layout.HBox previewBanner;
    @FXML private Label txtWelcome;

    // Sidebar items
    @FXML private Label lblAdminHeader;
    @FXML private VBox boxAdmin;
    @FXML private HBox boxAdminActions;

    // User Profile in Navbar
    @FXML private Label lblUsername;
    @FXML private Label lblUserRole;

    @FXML
    private void initialize() {
        loadCourses();

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                filterCourses(newVal);
            });
        }

        // Role-based visibility and Personalization
        Users user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            String displayName = user.getUsername() != null ? user.getUsername() : "User";
            txtWelcome.setText("Welcome back, " + displayName + "! 👋");
            
            // Navbar Profile
            lblUsername.setText(displayName);
            String role = user.getRole() != null ? user.getRole().replace("ROLE_", "") : "USER";
            lblUserRole.setText(role);
            
            // Update badge color based on role
            if ("ADMIN".equals(role)) {
                lblUserRole.setStyle("-fx-background-color: #434a75;"); // Dark theme for admin
            }

            boolean isAdmin = "ROLE_ADMIN".equals(user.getRole());
            lblAdminHeader.setVisible(isAdmin);
            lblAdminHeader.setManaged(isAdmin);
            boxAdmin.setVisible(isAdmin);
            boxAdmin.setManaged(isAdmin);
            
            if (boxAdminActions != null) {
                boxAdminActions.setVisible(isAdmin);
                boxAdminActions.setManaged(isAdmin);
            }
        }
    }

    @FXML
    private void onGoToDashboard() {
        contentArea.getChildren().setAll(dashboardView);
    }

    @FXML
    private void onShowIdeas() { loadSubView("/TSK/Idea.fxml"); }

    @FXML
    private void onShowMissions() { loadSubView("/TSK/Mission.fxml"); }

    @FXML
    private void onShowTasks() { loadSubView("/TSK/Tasks.fxml"); }

    @FXML
    private void onShowEvents() { System.out.println("Events section - Coming soon"); }

    @FXML
    private void onShowConnectedUsers() { loadSubView("/Users/Admin.fxml"); }

    @FXML
    private void onShowPostModeration() { loadSubView("/post/postModeration.fxml"); }

    @FXML
    private void showForum() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/post/displayPost.fxml"));
            Parent root = loader.load();
            gui.post.DisplayPostController controller = loader.getController();
            controller.setAdminMode(false);
            contentArea.getChildren().setAll(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void onShowCollaborations() { loadSubView("/collaborator/ListCollaborator.fxml"); }

    @FXML
    private void onShowCourses() { onGoToDashboard(); }

    @FXML
    private void onManageCategories() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/category-list-view.fxml"));
            Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) contentArea.getScene().getWindow();
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
            javafx.stage.Stage stage = (javafx.stage.Stage) contentArea.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException exception) {
            System.err.println("Navigation error: " + exception.getMessage());
        }
    }

    @FXML
    private void onViewQuestions() {
        System.out.println("Mentoring Questions - Coming soon");
    }

    private void loadSubView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
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

    private void loadCourses() {
        if (coursesContainer == null) return;
        try {
            categoryNames = courseService.getCategoryNames();
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
        if (coursesContainer == null) return;
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
        if (coursesContainer == null) return;
        coursesContainer.getChildren().clear();

        if (keyword == null || keyword.trim().isEmpty()) {
            coursesContainer.getChildren().addAll(allCourseCards);
            return;
        }

        String lower = keyword.toLowerCase();
        for (Node card : allCourseCards) {
            if (card.getUserData() != null && card.getUserData().toString().contains(lower)) {
                coursesContainer.getChildren().add(card);
            }
        }
    }

    private Node buildCourseCard(Course course) {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        card.setPrefWidth(320);
        card.setMinWidth(320);

        // Header: Icon and Title
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane iconContainer = new StackPane();
        String catName = categoryNames.getOrDefault(course.getCategorieId(), "General").toLowerCase();
        String iconClass = "card-icon-pink";
        String iconEmoji = "📦";
        if (catName.contains("prog") || catName.contains("java") || catName.contains("tech")) {
            iconClass = "card-icon-purple";
            iconEmoji = "💻";
        } else if (catName.contains("design") || catName.contains("vfx") || catName.contains("video")) {
            iconClass = "card-icon-blue";
            iconEmoji = "🎨";
        }
        iconContainer.getStyleClass().addAll("card-icon-container", iconClass);
        Label iconLabel = new Label(iconEmoji);
        iconLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        iconContainer.getChildren().add(iconLabel);

        VBox titleBox = new VBox(2);
        Label titleLabel = new Label(course.getTitre());
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setWrapText(true);
        Label categoryLabel = new Label(catName.toUpperCase());
        categoryLabel.setStyle("-fx-text-fill: -fx-primary-pink; -fx-font-weight: bold; -fx-font-size: 10px;");
        titleBox.getChildren().addAll(titleLabel, categoryLabel);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        header.getChildren().addAll(iconContainer, titleBox);

        // Content: Subtitle and Description
        VBox contentBox = new VBox(6);
        Label updatedLabel = new Label("Updated " + (course.getDateDeModification() != null ? course.getDateDeModification() : "recently"));
        updatedLabel.getStyleClass().add("card-subtitle");
        Label descriptionLabel = new Label(course.getDescription() != null ? course.getDescription() : "No description available.");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMinHeight(60);
        descriptionLabel.getStyleClass().add("subtitle-label");
        contentBox.getChildren().addAll(updatedLabel, descriptionLabel);

        // Footer: Action Link
        Label exploreLink = new Label("EXPLORE RESOURCES →");
        exploreLink.getStyleClass().add("card-action-link");
        exploreLink.setOnMouseClicked(event -> openCourse(course));

        card.getChildren().addAll(header, contentBox, exploreLink);
        card.setCursor(javafx.scene.Cursor.HAND);
        card.setOnMouseClicked(event -> openCourse(course));

        return card;
    }

    private void openCourse(Course course) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/front-resource-view.fxml"));
            Parent root = loader.load();
            FrontResourceController controller = loader.getController();
            controller.setCourse(course);
            boolean isPrev = previewBanner != null && previewBanner.isVisible();
            controller.setPreviewMode(isPrev);
            javafx.stage.Stage stage = (javafx.stage.Stage) contentArea.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML public void logout(javafx.event.ActionEvent event) { gui.SessionHelper.logout(event); }

    @FXML
    private void exitPreview(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/main-view.fxml"));
            Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }
}
