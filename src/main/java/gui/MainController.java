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

    private static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final CourseService courseService = new CourseService();
    private List<Course> courses = Collections.emptyList();
    private Map<Integer, String> categoryNames = Collections.emptyMap();
    private List<Node> allCourseCards = new ArrayList<>();

    @FXML
    private FlowPane coursesContainer;
    @FXML
    private TextField searchField;
    @FXML
    private Label pageTitleLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label lblTotalCourses;
    @FXML
    private Label lblActiveWorkshops;
    @FXML
    private Label lblTotalCategories;

    @FXML
    private StackPane contentArea;
    @FXML
    private VBox dashboardView;

    private static MainController instance;

    public static MainController getInstance() {
        return instance;
    }

    @FXML
    private void initialize() {
        instance = this;
        if (pageTitleLabel != null)
            pageTitleLabel.setText("Courses Dashboard");
        loadCourses();
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> filterCourses(newVal));
        }
    }

    @FXML
    private void onGoToDashboard() {
        contentArea.getChildren().setAll(dashboardView);
        if (pageTitleLabel != null)
            pageTitleLabel.setText("Dashboard");
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
            if (pageTitleLabel != null)
                pageTitleLabel.setText("Forum");
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
    public void onShowCourses() {
        onGoToDashboard();
    }

    private void loadSubView(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            if (pageTitleLabel != null)
                pageTitleLabel.setText(title);

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
        if (coursesContainer == null)
            return;
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
        if (contentArea == null && FrontMainController.getInstance() != null) {
            FrontMainController.getInstance().onManageCategories();
            return;
        }
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
    private void onShowHelpDesk() {
        if (contentArea == null && FrontMainController.getInstance() != null) {
            FrontMainController.getInstance().loadSubViewPublic("/gui/admin-support-hub.fxml");
            FrontMainController.setNavbarText("Help Desk", "Pages / Courses / Help Desk");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/admin-support-hub.fxml"));
            Parent root = loader.load();
            if (pageTitleLabel != null)
                pageTitleLabel.setText("Help Desk");
            contentArea.getChildren().setAll(root);
        } catch (IOException exception) {
            System.err.println("Navigation error: " + exception.getMessage());
        }
    }

    @FXML
    private void onShowAIDraft() {
        if (contentArea == null && FrontMainController.getInstance() != null) {
            // Let FrontMainController handle the overlay
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/ai-course-draft-modal.fxml"));
                Parent modal = loader.load();
                AICourseDraftController ctrl = loader.getController();
                FrontMainController fmc = FrontMainController.getInstance();
                javafx.scene.layout.StackPane overlay = new javafx.scene.layout.StackPane(modal);
                overlay.setStyle("-fx-background-color: rgba(0,0,0,0.45);");
                fmc.getContentArea().getChildren().add(overlay);
                if (ctrl != null)
                    ctrl.setOnClose(() -> fmc.getContentArea().getChildren().remove(overlay));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/ai-course-draft-modal.fxml"));
            Parent modal = loader.load();
            if (contentArea != null) {
                javafx.scene.layout.StackPane overlay = new javafx.scene.layout.StackPane(modal);
                overlay.setStyle("-fx-background-color: rgba(0,0,0,0.45);");
                contentArea.getChildren().add(overlay);
                AICourseDraftController ctrl = loader.getController();
                if (ctrl != null)
                    ctrl.setOnClose(() -> contentArea.getChildren().remove(overlay));
            }
        } catch (IOException exception) {
            System.err.println("AI Draft modal error: " + exception.getMessage());
        }
    }



    @FXML
    private void onAddCourse() {
        if (contentArea == null && FrontMainController.getInstance() != null) {
            FrontMainController.getInstance().onAddCourse();
            return;
        }
        try {
            var resource = getClass().getResource("/gui/create-course-form.fxml");
            if (resource == null) {
                System.err.println("Navigation error: Resource not found: /gui/create-course-form.fxml");
                return;
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            CourseFormController controller = loader.getController();
            // Set the content first so HTMLEditor WebView is in a live scene
            contentArea.getChildren().setAll(root);
            if (pageTitleLabel != null)
                pageTitleLabel.setText("Add Course");
            if (controller != null) {
                controller.setCourse(null);
            }
        } catch (IOException exception) {
            System.err.println("Navigation error: " + exception.getMessage());
            exception.printStackTrace();
        }
    }

    private void loadCourses() {
        if (coursesContainer == null)
            return;
        allCourseCards.clear();
        try {
            categoryNames = courseService.getCategoryNames();
            courses = courseService.afficher();
            if (statusLabel != null)
                statusLabel.setText(courses.size() + " course(s) loaded.");
            renderCourses();
        } catch (SQLException exception) {
            courses = Collections.emptyList();
            categoryNames = Collections.emptyMap();
            if (statusLabel != null)
                statusLabel.setText("Database error.");
            renderCourses();
        }
    }

    private void renderCourses() {
        if (coursesContainer == null)
            return;
        coursesContainer.getChildren().clear();

        // Populate stat cards
        if (lblTotalCourses != null)
            lblTotalCourses.setText(String.valueOf(courses.size()));
        if (lblTotalCategories != null)
            lblTotalCategories.setText(String.valueOf(categoryNames.size()));
        long workshops = courses.stream()
                .filter(c -> "Workshop".equalsIgnoreCase(resolveCategoryName(c.getCategorieId()))).count();
        if (lblActiveWorkshops != null)
            lblActiveWorkshops.setText(String.valueOf(workshops));

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
        VBox card = new VBox(0);
        card.setPrefWidth(370);
        card.setMinWidth(340);
        card.setMaxWidth(410);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 12, 0, 0, 4);");
        card.setCursor(javafx.scene.Cursor.HAND);

        // ── IMAGE AREA ──────────────────────────────────────────
        StackPane imageArea = new StackPane();
        imageArea.setPrefHeight(210);
        imageArea.setMinHeight(210);
        imageArea.setMaxHeight(210);

        if (course.getImage() != null && !course.getImage().isBlank()) {
            try {
                String imagePath = course.getImage().trim();
                // If it's a local path and doesn't start with file:, add it
                if (!imagePath.startsWith("http") && !imagePath.startsWith("file:") && !imagePath.startsWith("data:")) {
                    imagePath = new java.io.File(imagePath).toURI().toString();
                }

                javafx.scene.image.Image fxImage = new javafx.scene.image.Image(imagePath, true);
                javafx.scene.image.ImageView img = new javafx.scene.image.ImageView(fxImage);
                
                img.setFitHeight(210);
                img.setPreserveRatio(false);
                
                // Bind image width to card width so it fits perfectly
                img.fitWidthProperty().bind(card.widthProperty());
                
                javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
                clip.widthProperty().bind(card.widthProperty());
                clip.setHeight(210);
                clip.setArcWidth(32); clip.setArcHeight(32);
                img.setClip(clip);
                
                imageArea.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 16 16 0 0;");
                imageArea.getChildren().add(img);

                // Fallback if image fails to load (async)
                fxImage.errorProperty().addListener((obs, oldVal, isError) -> {
                    if (isError) {
                        javafx.application.Platform.runLater(() -> {
                            imageArea.getChildren().remove(img);
                            imageArea.setStyle("-fx-background-color: linear-gradient(to bottom right, #e879f9, #818cf8); -fx-background-radius: 16 16 0 0;");
                        });
                    }
                });
            } catch (Exception e) {
                imageArea.setStyle("-fx-background-color: linear-gradient(to bottom right, #e879f9, #818cf8); -fx-background-radius: 16 16 0 0;");
            }
        } else {
            imageArea.setStyle("-fx-background-color: linear-gradient(to bottom right, #e879f9, #818cf8); -fx-background-radius: 16 16 0 0;");
        }

        // Category badge overlaid on image
        String catName = resolveCategoryName(course.getCategorieId());
        Label catBadge = new Label(catName.toUpperCase());
        catBadge.setStyle(
                "-fx-background-color: white;" +
                        "-fx-text-fill: #ec4899; -fx-font-weight: bold; -fx-font-size: 10px;" +
                        "-fx-padding: 6 16; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        StackPane.setAlignment(catBadge, javafx.geometry.Pos.TOP_RIGHT);
        StackPane.setMargin(catBadge, new Insets(15, 15, 0, 0));
        imageArea.getChildren().add(catBadge);

        // ── CONTENT AREA ─────────────────────────────────────────
        VBox content = new VBox(8);
        content.setPadding(new Insets(14, 16, 14, 16));

        // Title row + kebab menu
        HBox titleRow = new HBox(6);
        titleRow.setAlignment(javafx.geometry.Pos.TOP_LEFT);

        Label titleLabel = new Label(course.getTitre() != null ? course.getTitre() : "Untitled");
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(220);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        javafx.scene.control.MenuButton menuBtn = new javafx.scene.control.MenuButton("⋮");
        menuBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-font-size: 18px; -fx-padding: 0 4; -fx-cursor: hand;");
        menuBtn.setOnMouseClicked(javafx.event.Event::consume);

        javafx.scene.control.MenuItem editItem = new javafx.scene.control.MenuItem("✏  Edit Course");
        editItem.setOnAction(e -> openEditCourse(course));
        javafx.scene.control.MenuItem deleteItem = new javafx.scene.control.MenuItem("🗑  Delete Course");
        deleteItem.setStyle("-fx-text-fill: #dc2626;");
        deleteItem.setOnAction(e -> deleteCourse(course));
        menuBtn.getItems().addAll(editItem, deleteItem);

        titleRow.getChildren().addAll(titleLabel, menuBtn);

        // Description (stripped HTML)
        String descRaw = course.getDescription() != null ? course.getDescription() : "";
        String desc = stripHtml(descRaw);
        Label descLabel = new Label(desc.isBlank() ? "No description available." : desc);
        descLabel.setWrapText(true);
        descLabel.setPrefHeight(40);
        descLabel.setMaxHeight(40);
        descLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");

        // ── FOOTER ───────────────────────────────────────────────
        HBox footer = new HBox(8);
        footer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        footer.setPadding(new Insets(8, 0, 0, 0));

        int resCount = getResourceCount(course.getId());
        Label resLabel = new Label("📚 " + resCount + " Resources");
        resLabel.setStyle("-fx-text-fill: #ec4899; -fx-font-weight: bold; -fx-font-size: 11px;" +
                "-fx-background-color: #fdf2f8; -fx-padding: 4 10; -fx-background-radius: 8;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String dateStr = course.getDateDeModification() != null && !course.getDateDeModification().isBlank()
                ? course.getDateDeModification()
                : (course.getDateDeCreation() != null ? course.getDateDeCreation() : "");
        // Trim to datetime without nanos
        if (dateStr.length() > 19)
            dateStr = dateStr.substring(0, 19);
        Label dateLabel = new Label("🕐 " + dateStr);
        dateLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px;");

        footer.getChildren().addAll(resLabel, spacer, dateLabel);

        content.getChildren().addAll(titleRow, descLabel, footer);
        card.getChildren().addAll(imageArea, content);

        // Card click → open resources (but not when clicking menu)
        card.setOnMouseClicked(ev -> openResources(course, card));

        // Hover
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 16;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(236,72,153,0.22), 18, 0, 0, 6);"));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 16;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 12, 0, 0, 4);"));

        return card;
    }

    private String stripHtml(String html) {
        if (html == null)
            return "";
        return html.replaceAll("<[^>]*>", "").replace("&nbsp;", " ").replaceAll("\\s+", " ").trim();
    }

    private int getResourceCount(int courseId) {
        try {
            return new services.RessourceService().afficherParCours(courseId).size();
        } catch (Exception e) {
            return 0;
        }
    }

    private void openEditCourse(Course course) {
        if (contentArea == null && FrontMainController.getInstance() != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/create-course-form.fxml"));
                Parent root = loader.load();
                CourseFormController ctrl = loader.getController();
                FrontMainController.getInstance().setContent(root);
                FrontMainController.setNavbarText("Edit Course", "Pages / Courses / Edit");
                if (ctrl != null)
                    ctrl.setCourse(course);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/create-course-form.fxml"));
            Parent root = loader.load();
            CourseFormController ctrl = loader.getController();
            contentArea.getChildren().setAll(root);
            if (ctrl != null)
                ctrl.setCourse(course);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteCourse(Course course) {
        if (!gui.util.AlertHelper.confirmDelete("course \"" + course.getTitre() + "\""))
            return;
        try {
            courseService.supprimer(course.getId());
            loadCourses();
            gui.util.AlertHelper.showInfo("Success", "Course deleted successfully.");
        } catch (Exception e) {
            gui.util.AlertHelper.showError("Delete Error", "Could not delete course: " + e.getMessage());
        }
    }

    public void openResources(Course course, Node sourceNode) {
        if (contentArea == null && FrontMainController.getInstance() != null) {
            FrontMainController.getInstance().openCourse(course);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/front-resource-view.fxml"));
            Parent root = loader.load();
            FrontResourceController controller = loader.getController();
            if (controller != null) {
                controller.setCourse(course);
            }
            
            if (contentArea != null) {
                // If the view is a BorderPane, only take the center to avoid layout issues
                if (root instanceof BorderPane) {
                    contentArea.getChildren().setAll(((BorderPane) root).getCenter());
                } else {
                    contentArea.getChildren().setAll(root);
                }
                if (pageTitleLabel != null) pageTitleLabel.setText("Resources: " + course.getTitre());
            } else {
                // Last resort fallback
                Stage stage = (Stage) (sourceNode != null ? sourceNode.getScene().getWindow() : pageTitleLabel.getScene().getWindow());
                stage.getScene().setRoot(root);
            }
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
    public void logout(javafx.event.ActionEvent event) {
        gui.SessionHelper.logout(event);
    }
}

