package gui;

import entities.Course;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import services.CourseService;
import utils.SessionManager;
import entities.Users;

import java.io.IOException;
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
    @FXML private StackPane contentArea;
    @FXML private VBox dashboardView;
    @FXML private javafx.scene.layout.HBox previewBanner;
    @FXML private Label txtWelcome;

    // Sidebar items
    @FXML private Label lblAdminHeader;
    @FXML private VBox boxAdmin;
    @FXML private Button btnAdmin;

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

            boolean isAdmin = "ROLE_ADMIN".equals(user.getRole());
            lblAdminHeader.setVisible(isAdmin);
            lblAdminHeader.setManaged(isAdmin);
            boxAdmin.setVisible(isAdmin);
            boxAdmin.setManaged(isAdmin);

            if (isAdmin) {
                onShowAdmin();
            }
        }
    }

    @FXML
    private void onGoToDashboard() {
        contentArea.getChildren().setAll(dashboardView);
    }

    @FXML
    private void onShowIdeas() {
        loadSubView("/TSK/Idea.fxml");
    }

    @FXML
    private void onShowMissions() {
        loadSubView("/TSK/Mission.fxml");
    }

    @FXML
    private void onShowTasks() {
        loadSubView("/TSK/Tasks.fxml");
    }

    @FXML
    private void onShowEvents() {
        System.out.println("Events section - Coming soon");
    }

    @FXML
    private void onShowAdmin() {
        loadSubView("/Users/Admin.fxml");
    }

    @FXML
    private void showForum() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/post/displayPost.fxml"));
            Parent root = loader.load();
            gui.post.DisplayPostController controller = loader.getController();
            controller.setAdminMode(false);
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onShowCollaborations() {
        loadSubView("/collaborator/ListCollaborator.fxml");
    }

    private void loadSubView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            // If the loaded FXML is a BorderPane (like our modernized TSK pages),
            // extract only the center content to avoid double sidebars.
            if (root instanceof javafx.scene.layout.BorderPane) {
                Node center = ((javafx.scene.layout.BorderPane) root).getCenter();
                // We need to ensure the center node fills the content area
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

    @FXML
    private void onShowCourses() {
        onGoToDashboard();
    }

    @FXML
    private void onShowProfile() {
        loadSubView("/Users/Profile.fxml");
    }

    private void loadCourses() {
        if (coursesContainer == null) return;
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
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/gui/front-resource-view.fxml"));
            javafx.scene.Parent root = loader.load();
            
            FrontResourceController controller = loader.getController();
            controller.setCourse(course);
            boolean isPrev = previewBanner != null && previewBanner.isVisible();
            controller.setPreviewMode(isPrev);
            
            javafx.stage.Stage stage = (javafx.stage.Stage) contentArea.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    @javafx.fxml.FXML
    public void logout(javafx.event.ActionEvent event) {
        gui.SessionHelper.logout(event);
    }
}
