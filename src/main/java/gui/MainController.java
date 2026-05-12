package gui;

import entities.Course;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import services.CourseService;
import gui.post.DisplayPostController;
import utils.GroqService;
import javafx.application.Platform;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.scene.paint.Color;

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
        if (HelpDeskMessagesController.getInstance() != null) {
            HelpDeskMessagesController.getInstance().loadSubViewWithTitle(fxmlPath, title);
        } else {
            System.err.println("HelpDeskMessagesController instance is null. Falling back to internal loading.");
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent root = loader.load();
                if (contentArea != null) {
                    contentArea.getChildren().setAll(root);
                } else if (FrontMainController.getInstance() != null) {
                    FrontMainController.getInstance().setContent(root);
                }
            } catch (IOException e) { e.printStackTrace(); }
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
    private void onShowHelpDesk() {
        loadSubView("/gui/admin-support-hub.fxml", "Support Hub");
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
        VBox card = new VBox(0); // Zero spacing for the banner-to-content transition
        card.getStyleClass().add("card");
        card.setPrefWidth(345); 
        card.setMinWidth(345);
        card.setMaxWidth(345);
        card.setStyle("-fx-background-radius: 20; -fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 15, 0, 0, 5); -fx-overflow: hidden;");

        // 1. Top Banner / Image
        StackPane bannerContainer = new StackPane();
        bannerContainer.setPrefHeight(145);
        bannerContainer.setMinHeight(145);
        bannerContainer.setStyle("-fx-background-radius: 20 20 0 0; -fx-background-color: linear-gradient(to bottom right, #ce2d7c, #6c2db1);");

        if (course.getImage() != null && !course.getImage().isBlank()) {
            try {
                javafx.scene.image.ImageView img = new javafx.scene.image.ImageView(new javafx.scene.image.Image(course.getImage(), true));
                img.setFitWidth(260);
                img.setFitHeight(130);
                img.setPreserveRatio(true);
                
                // Round top corners of the image
                javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(260, 130);
                clip.setArcWidth(40);
                clip.setArcHeight(40);
                img.setClip(clip);
                
                bannerContainer.getChildren().add(img);
            } catch (Exception e) {
                Label placeholder = new Label("📚");
                placeholder.setStyle("-fx-font-size: 50px; -fx-text-fill: white;");
                bannerContainer.getChildren().add(placeholder);
            }
        } else {
            Label placeholder = new Label("📚");
            placeholder.setStyle("-fx-font-size: 50px; -fx-text-fill: white;");
            bannerContainer.getChildren().add(placeholder);
        }

        // Overlay Category Badge on Banner
        Label catBadge = new Label(resolveCategoryName(course.getCategorieId()).toUpperCase());
        catBadge.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-text-fill: -fx-primary-pink; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 5 12; -fx-background-radius: 10;");
        StackPane.setAlignment(catBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(catBadge, new Insets(15));
        bannerContainer.getChildren().add(catBadge);

        // 2. Content Area
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Title + Menu Row
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.TOP_LEFT);
        
        Label titleLabel = new Label(course.getTitre());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        titleLabel.setWrapText(true);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        MenuButton menuButton = new MenuButton("⋮");
        menuButton.setStyle("-fx-background-color: transparent; -fx-font-size: 20px; -fx-text-fill: #94a3b8; -fx-cursor: hand;");
        MenuItem editItem = new MenuItem("Edit Course");
        editItem.setOnAction(e -> onEditCourse(course));
        MenuItem deleteItem = new MenuItem("Delete Course");
        deleteItem.setStyle("-fx-text-fill: #ef4444;");
        deleteItem.setOnAction(e -> onDeleteCourse(course));
        menuButton.getItems().addAll(editItem, deleteItem);

        titleRow.getChildren().addAll(titleLabel, menuButton);

        // Description (Limited lines)
        Label descLabel = new Label(safeText(course.getDescription()));
        descLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");
        descLabel.setWrapText(true);
        descLabel.setMaxHeight(40);
        descLabel.setMinHeight(40);

        // Footer Row: Progress/Stats + Date
        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER_LEFT);
        
        Label resourcesLabel = new Label("📂 12 Resources"); // Mocked count or service call needed
        resourcesLabel.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-color: #f1f5f9; -fx-padding: 4 10; -fx-background-radius: 8;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label dateLabel = new Label("🕒 " + safeText(course.getDateDeModification()));
        dateLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");

        footer.getChildren().addAll(resourcesLabel, spacer, dateLabel);

        content.getChildren().addAll(titleRow, descLabel, footer);

        card.getChildren().addAll(bannerContainer, content);
        
        // Interactivity
        card.setCursor(javafx.scene.Cursor.HAND);
        card.setOnMouseClicked(event -> {
            if (event.getTarget() instanceof MenuButton || event.getTarget() instanceof MenuItem) return;
            openRessources(course, card);
        });

        // Hover effect: Gentle lift + shadow grow
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-radius: 20; -fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(206,45,124,0.15), 25, 0, 0, 10); -fx-translate-y: -5;");
        });
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-radius: 20; -fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 15, 0, 0, 5); -fx-translate-y: 0;");
        });

        return card;
    }

    private void openRessources(Course course, Node sourceNode) {
        if (FrontMainController.getInstance() != null) {
            FrontMainController.getInstance().openAdminCourse(course);
        } else {
            System.err.println("FrontMainController instance is null, cannot open course resources.");
        }
    }

    private void onEditCourse(Course course) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/create-course-form.fxml"));
            Parent root = loader.load();
            
            CourseFormController controller = loader.getController();
            controller.setCourse(course);
            
            if (FrontMainController.getInstance() != null) {
                FrontMainController.setNavbarText("Edit Course: " + course.getTitre(), "Pages / Courses / Edit");
                FrontMainController.getInstance().setContent(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
            gui.util.AlertHelper.showError("Navigation Error", "Could not open edit form: " + e.getMessage());
        }
    }

    private void onDeleteCourse(Course course) {
        if (gui.util.AlertHelper.confirmDelete("course \"" + course.getTitre() + "\"")) {
            try {
                courseService.supprimer(course.getId());
                loadCourses(); // Refresh the grid
                updateStats(); // Update stats after deletion
                gui.util.AlertHelper.showInfo("Success", "Course deleted successfully.");
            } catch (SQLException e) {
                e.printStackTrace();
                gui.util.AlertHelper.showError("Delete Error", "Could not delete course: " + e.getMessage());
            }
        }
    }

    private String resolveCategoryName(int categoryId) {
        return categoryNames.getOrDefault(categoryId, "Unassigned");
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    @FXML
    private void onShowAIDraft() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/ai-course-draft-modal.fxml"));
            Parent modal = loader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(modal);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            
            if (FrontMainController.getInstance() != null) {
                FrontMainController.getInstance().applyBlur();
            }
            
            stage.showAndWait();
            
            if (FrontMainController.getInstance() != null) {
                FrontMainController.getInstance().removeBlur();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void logout(javafx.event.ActionEvent event) {
        gui.SessionHelper.logout(event);
    }
}
