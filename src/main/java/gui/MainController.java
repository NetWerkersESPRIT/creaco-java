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
    private static MainController instance;

    @FXML private FlowPane coursesContainer;
    @FXML private TextField searchField;
    @FXML private Label pageTitleLabel;
    @FXML private Label statusLabel;
    
    @FXML private StackPane contentArea;
    @FXML private VBox dashboardView;

    @FXML private TextField aiTopicField;
    @FXML private Button btnGenerateIdea;
    @FXML private javafx.scene.control.TextArea aiResultArea;
    @FXML private Button btnDraftCourse;

    @FXML
    private void initialize() {
        instance = this;
        if (pageTitleLabel != null) pageTitleLabel.setText("Courses Dashboard");
        loadCourses();
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> filterCourses(newVal));
        }
    }

    public static void loadContentStatic(Node node) {
        if (instance != null) {
            instance.contentArea.getChildren().setAll(node);
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/create-course-form.fxml"));
            Parent root = loader.load();
            CourseFormController controller = loader.getController();
            controller.setCourse(null);
            Stage stage = (Stage) pageTitleLabel.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException exception) {
            System.err.println("Navigation error: " + exception.getMessage());
        }
    }

    @FXML
    private void onViewQuestions() {
        loadSubView("/gui/help-desk-messages.fxml", "Help Desk Support");
    }

    private void loadCourses() {
        if (coursesContainer == null) return;
        allCourseCards.clear();
        
        // Show Skeleton Loaders first
        coursesContainer.getChildren().clear();
        for (int i = 0; i < 6; i++) {
            coursesContainer.getChildren().add(buildSkeletonCard());
        }

        // Simulate tiny network delay for the skeleton loader to be visible
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(800));
        pause.setOnFinished(e -> {
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
        });
        pause.play();
    }

    private Node buildSkeletonCard() {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        card.setPrefWidth(390);
        card.setMinWidth(390);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5); -fx-padding: 20;");

        HBox header = new HBox(15);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Region iconSkeleton = new Region();
        iconSkeleton.setPrefSize(48, 48);
        iconSkeleton.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 12;");
        
        VBox titleBox = new VBox(8);
        Region titleSkeleton = new Region();
        titleSkeleton.setPrefSize(200, 15);
        titleSkeleton.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 4;");
        Region catSkeleton = new Region();
        catSkeleton.setPrefSize(100, 10);
        catSkeleton.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 4;");
        titleBox.getChildren().addAll(titleSkeleton, catSkeleton);
        header.getChildren().addAll(iconSkeleton, titleBox);
        
        Region descSkeleton1 = new Region();
        descSkeleton1.setPrefSize(350, 10);
        descSkeleton1.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 2;");
        Region descSkeleton2 = new Region();
        descSkeleton2.setPrefSize(250, 10);
        descSkeleton2.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 2;");
        
        card.getChildren().addAll(header, descSkeleton1, descSkeleton2);

        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(javafx.util.Duration.millis(800), card);
        fade.setFromValue(0.5);
        fade.setToValue(1.0);
        fade.setCycleCount(javafx.animation.Animation.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();

        return card;
    }

    private void renderCourses() {
        if (coursesContainer == null) return;
        coursesContainer.getChildren().clear();

        if (courses.isEmpty()) {
            VBox emptyStateBox = new VBox(15);
            emptyStateBox.setAlignment(javafx.geometry.Pos.CENTER);
            emptyStateBox.setStyle("-fx-padding: 50;");
            
            Label iconEmpty = new Label("🗂️");
            iconEmpty.setStyle("-fx-font-size: 60px;");
            
            Label textEmpty = new Label("No courses to manage yet.");
            textEmpty.setStyle("-fx-text-fill: #64748b; -fx-font-size: 18px; -fx-font-weight: bold;");
            
            Label subEmpty = new Label("Click '+ Add Course' to create your first content.");
            subEmpty.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px;");
            
            emptyStateBox.getChildren().addAll(iconEmpty, textEmpty, subEmpty);
            coursesContainer.getChildren().add(emptyStateBox);
            
            new animatefx.animation.FadeInUp(emptyStateBox).play();
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
        card.setPrefWidth(390);
        card.setMinWidth(390);

        HBox header = new HBox(15);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        StackPane iconContainer = new StackPane();
        iconContainer.getStyleClass().add("card-icon-container");
        iconContainer.setMinWidth(48);
        iconContainer.setMinHeight(48);
        iconContainer.setMaxWidth(48);
        iconContainer.setMaxHeight(48);

        if (course.getImage() != null && !course.getImage().isBlank()) {
            try {
                javafx.scene.image.ImageView courseImg = new javafx.scene.image.ImageView(new javafx.scene.image.Image(course.getImage(), true));
                courseImg.setFitWidth(48);
                courseImg.setFitHeight(48);
                courseImg.setPreserveRatio(false);
                
                javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(48, 48);
                clip.setArcWidth(12);
                clip.setArcHeight(12);
                courseImg.setClip(clip);
                
                iconContainer.getChildren().add(courseImg);
                iconContainer.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
            } catch (Exception e) {
                Label iconLabel = new Label("📦"); 
                iconLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
                iconContainer.getChildren().add(iconLabel);
            }
        } else {
            Label iconLabel = new Label("📦"); 
            iconLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
            iconContainer.getChildren().add(iconLabel);
        }

        VBox titleBox = new VBox(2);
        String titre = course.getTitre();
        if (titre == null) titre = "Untitled Course";
        Label titleLabel = new Label(titre);
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setWrapText(true);

        String catName = resolveCategoryName(course.getCategorieId());
        if (catName == null) catName = "Unassigned";
        Label categoryLabel = new Label(catName.toUpperCase());
        categoryLabel.setStyle("-fx-text-fill: -fx-primary-pink; -fx-font-weight: bold; -fx-font-size: 10px;");

        titleBox.getChildren().addAll(titleLabel, categoryLabel);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        // Status Badge (Draft/Published)
        boolean isPublished = "Published".equalsIgnoreCase(course.getStatut());
        Label statusBadge = new Label(isPublished ? "PUBLISHED" : "DRAFT");
        statusBadge.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE); // Prevent truncation
        String bgColor = "#e2e8f0"; // Light grey
        String textColor = "#475569"; // Dark grey text
        String shadowColor = "rgba(0,0,0,0.05)";
        statusBadge.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: " + textColor + "; -fx-font-weight: bold; -fx-font-size: 9px; -fx-padding: 4 8; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, " + shadowColor + ", 4, 0, 0, 2);");

        header.getChildren().addAll(iconContainer, titleBox, statusBadge);

        VBox contentBox = new VBox(6);
        Label updatedLabel = new Label("Updated " + safeText(course.getDateDeModification()));
        updatedLabel.getStyleClass().add("card-subtitle");

        Label descriptionLabel = new Label(stripHtmlTags(safeText(course.getDescription())));
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

        // Enhanced Hover Micro-animations
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(236,72,153,0.3), 15, 0, 0, 8);");
            javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(200), card);
            st.setToX(1.02); st.setToY(1.02); st.play();
        });
        
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");
            javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(200), card);
            st.setToX(1.0); st.setToY(1.0); st.play();
        });

        card.getChildren().addAll(header, contentBox, footer);
        return card;
    }

    private void openRessources(Course course, Node sourceNode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/resource-list-view.fxml"));
            Parent root = loader.load();
            RessourceListController controller = loader.getController();
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

    private String stripHtmlTags(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]*>", "").replace("&nbsp;", " ").trim();
    }

    @FXML
    private void onGenerateCourseIdea() {
        if (aiTopicField == null || aiTopicField.getText().trim().isEmpty()) {
            gui.AlertHelper.showError("Missing Topic", "Please enter a topic or keyword first.");
            return;
        }
        String topic = aiTopicField.getText().trim();
        btnGenerateIdea.setText("Generating...");
        btnGenerateIdea.setDisable(true);
        aiResultArea.setText("Thinking... Powered by Groq AI 🚀");
        
        new Thread(() -> {
            String result = utils.GroqService.generateCourseIdea(topic);
            javafx.application.Platform.runLater(() -> {
                aiResultArea.setText(result);
                btnGenerateIdea.setText("Generate Draft");
                btnGenerateIdea.setDisable(false);
                if (result != null && result.contains("TITLE:") && result.contains("DESCRIPTION:")) {
                    if (btnDraftCourse != null) btnDraftCourse.setDisable(false);
                } else {
                    if (btnDraftCourse != null) btnDraftCourse.setDisable(true);
                }
            });
        }).start();
    }

    @FXML
    private void onDraftCourse() {
        if (aiResultArea == null || aiResultArea.getText().trim().isEmpty()) return;
        String content = aiResultArea.getText();
        
        String title = "AI Draft Course";
        String description = content;
        
        try {
            int titleStart = content.indexOf("TITLE:");
            int descStart = content.indexOf("DESCRIPTION:");
            
            if (titleStart != -1 && descStart != -1) {
                title = content.substring(titleStart + 6, descStart).trim();
                description = content.substring(descStart + 12).trim();
            }
            
            Course newCourse = new Course();
            newCourse.setTitre(title);
            newCourse.setDescription(description);
            
            // Generate a slug from the title
            String slug = title.toLowerCase().replaceAll("[^a-z0-9\\s-]", "").replaceAll("\\s+", "-");
            newCourse.setSlug(slug);
            
            // Assign to first category by default if any exist
            if (categoryNames != null && !categoryNames.isEmpty()) {
                newCourse.setCategorieId(categoryNames.keySet().iterator().next());
            } else {
                newCourse.setCategorieId(1);
            }
            
            courseService.ajouter(newCourse);
            
            gui.AlertHelper.showInfo("Success", "Course drafted successfully! You can now edit it to add resources and an image.");
            
            // Refresh
            loadCourses();
            
            // Reset
            aiTopicField.clear();
            aiResultArea.clear();
            btnDraftCourse.setDisable(true);
            
        } catch (Exception e) {
            e.printStackTrace();
            gui.AlertHelper.showError("Error", "Failed to draft course: " + e.getMessage());
        }
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
