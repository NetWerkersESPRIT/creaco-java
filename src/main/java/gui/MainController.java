package gui;

import entities.Course;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
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

    // Stats
    @FXML private Label lblTotalCourses;
    @FXML private Label lblActiveWorkshops;
    @FXML private Label lblTotalCategories;

    // AI Studio
    @FXML private TextField aiTopicField;
    @FXML private Button btnGenerateIdea;
    @FXML private TextArea aiResultArea;
    @FXML private Button btnDraftCourse;

    @FXML
    private void initialize() {
        if (pageTitleLabel != null) pageTitleLabel.setText("Courses Dashboard");
        loadCourses();
        updateStats();
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> filterCourses(newVal));
        }
    }

    private void updateStats() {
        if (lblTotalCourses != null) lblTotalCourses.setText(String.valueOf(courses.size()));
        if (lblActiveWorkshops != null) lblActiveWorkshops.setText(String.valueOf(courses.size())); // Simplified
        if (lblTotalCategories != null) lblTotalCategories.setText(String.valueOf(categoryNames.size()));
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
        loadSubView("/Event.fxml", "Events");
    }

    @FXML
    private void onShowReservations() {
        loadSubView("/Reservation.fxml", "Reservations");
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
        if (utils.SessionManager.getInstance().isAdmin()) {
            loadSubView("/gui/collab/admin/dashboard.fxml", "Collaboration Backoffice");
        } else {
            loadSubView("/gui/collab/collab_dashboard.fxml", "Collaborations");
        }
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
        if (FrontMainController.getInstance() != null) {
            FrontMainController.getInstance().onManageCategories();
        } else {
            System.err.println("FrontMainController instance is null, cannot manage categories.");
        }
    }

    @FXML
    private void onAddCourse() {
        if (FrontMainController.getInstance() != null) {
            FrontMainController.getInstance().onAddCourse();
        } else {
            System.err.println("FrontMainController instance is null, cannot add course.");
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
        VBox card = new VBox(18);
        card.getStyleClass().add("card");
        card.setPrefWidth(380); // Slightly smaller card
        card.setMinWidth(380);
        card.setPadding(new Insets(25));
        card.setStyle("-fx-background-radius: 25; -fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 15, 0, 0, 5);");

        // Top Row: Icon/Image + Title/Category + Status
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        // Icon/Image Container
        StackPane visualContainer = new StackPane();
        visualContainer.setPrefSize(70, 70);
        visualContainer.setMinSize(70, 70);
        visualContainer.setStyle("-fx-background-radius: 18; -fx-background-color: linear-gradient(to bottom right, #ce2d7c, #6c2db1);");

        if (course.getImage() != null && !course.getImage().isBlank()) {
            try {
                javafx.scene.image.ImageView img = new javafx.scene.image.ImageView(new javafx.scene.image.Image(course.getImage(), true));
                img.setFitWidth(70);
                img.setFitHeight(70);
                img.setPreserveRatio(false);
                javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(70, 70);
                clip.setArcWidth(36);
                clip.setArcHeight(36);
                img.setClip(clip);
                visualContainer.getChildren().add(img);
            } catch (Exception e) {
                Label icon = new Label("📦");
                icon.setStyle("-fx-text-fill: white; -fx-font-size: 24px;");
                visualContainer.getChildren().add(icon);
            }
        } else {
            Label icon = new Label("📦");
            icon.setStyle("-fx-text-fill: white; -fx-font-size: 24px;");
            visualContainer.getChildren().add(icon);
        }

        VBox titleBox = new VBox(2);
        Label titleLabel = new Label(course.getTitre());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        titleLabel.setWrapText(true);

        Label categoryLabel = new Label(resolveCategoryName(course.getCategorieId()).toUpperCase());
        categoryLabel.setStyle("-fx-text-fill: -fx-primary-pink; -fx-font-weight: bold; -fx-font-size: 11px;");

        titleBox.getChildren().addAll(titleLabel, categoryLabel);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        // Status Badge
        Label statusBadge = new Label("PUBLISHED"); // Assuming published for now, could be dynamic
        statusBadge.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #475569; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 12;");

        header.getChildren().addAll(visualContainer, titleBox, statusBadge);

        // Updated Date
        Label updatedLabel = new Label("Updated " + safeText(course.getDateDeModification()));
        updatedLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");

        // Description
        Label descriptionLabel = new Label(safeText(course.getDescription()));
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMinHeight(50);
        descriptionLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px;");

        // Explore Link
        Label exploreLink = new Label("EXPLORE RESOURCES →");
        exploreLink.setStyle("-fx-text-fill: -fx-primary-pink; -fx-font-weight: bold; -fx-font-size: 12px; -fx-cursor: hand;");
        exploreLink.setOnMouseClicked(event -> {
            openRessources(course, card);
            event.consume();
        });

        card.setOnMouseClicked(event -> openRessources(course, card));
        card.setCursor(javafx.scene.Cursor.HAND);

        card.getChildren().addAll(header, updatedLabel, descriptionLabel, exploreLink);
        
        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-radius: 25; -fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(206,45,124,0.1), 20, 0, 0, 8); -fx-translate-y: -2;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-radius: 25; -fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 15, 0, 0, 5); -fx-translate-y: 0;"));

        return card;
    }

    private void openRessources(Course course, Node sourceNode) {
        if (FrontMainController.getInstance() != null) {
            FrontMainController.getInstance().openCourse(course);
        } else {
            System.err.println("FrontMainController instance is null, cannot open course resources.");
        }
    }

    private String resolveCategoryName(int categoryId) {
        return categoryNames.getOrDefault(categoryId, "Unassigned");
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    

    @FXML
    public void logout(javafx.event.ActionEvent event) {
        gui.SessionHelper.logout(event);
    }

    @FXML
    private void onGenerateCourseIdea() {
        String topic = aiTopicField.getText();
        if (topic == null || topic.trim().isEmpty()) {
            aiResultArea.setText("Please enter a topic first!");
            return;
        }
        aiResultArea.setText("Generating outline for: " + topic + "...\n(AI Integration pending)");
        btnDraftCourse.setDisable(false);
    }

    @FXML
    private void onDraftCourse() {
        // Logic to transition to course-edit-view with the generated content
        aiResultArea.setText("Drafting course...");
    }
}

