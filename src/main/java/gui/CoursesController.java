package gui;

import entities.Course;
import entities.HelpTicket;
import entities.Users;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import services.CourseService;
import services.HelpTicketService;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import animatefx.animation.*;

public class CoursesController {

    private final CourseService courseService = new CourseService();
    private final HelpTicketService ticketService = new HelpTicketService();
    
    private List<Course> courses = Collections.emptyList();
    private Map<Integer, String> categoryNames = Collections.emptyMap();
    private List<Node> allCourseCards = new ArrayList<>();

    @FXML private Pane coursesContainer;
    @FXML private VBox questionsContainer;
    @FXML private TextField searchField;
    @FXML private HBox boxAdminActions;
    @FXML private VBox mainContentBox;
    @FXML private HBox gamificationCard;

    @FXML
    public void initialize() {
        if (mainContentBox != null) {
            new FadeInUp(mainContentBox).setSpeed(0.8).play();
        }
        
        if (gamificationCard != null) {
            new FadeInRight(gamificationCard).setDelay(javafx.util.Duration.millis(300)).play();
            gamificationCard.setOnMouseEntered(e -> new Pulse(gamificationCard).setSpeed(2.0).play());
        }
        

        loadCourses();
        loadTickets();

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                filterCourses(newVal);
            });
        }

        // Admin visibility for buttons
        Users user = SessionManager.getInstance().getCurrentUser();
        if (user != null && boxAdminActions != null) {
            boolean isAdmin = "ROLE_ADMIN".equals(user.getRole());
            boxAdminActions.setVisible(isAdmin);
            boxAdminActions.setManaged(isAdmin);
        }
    }

    private void loadCourses() {
        if (coursesContainer == null) return;
        
        // Show Skeleton Loaders first
        coursesContainer.getChildren().clear();
        for (int i = 0; i < 4; i++) {
            coursesContainer.getChildren().add(buildSkeletonCard());
        }

        // Simulate a tiny network delay for the skeleton loader to be visible
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(800));
        pause.setOnFinished(e -> {
            try {
                categoryNames = courseService.getCategoryNames();
                courses = courseService.afficherPublie();
                renderCourses();
            } catch (SQLException ex) {
                courses = Collections.emptyList();
                coursesContainer.getChildren().clear();
                Label error = new Label("Error loading courses: " + ex.getMessage());
                error.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
                coursesContainer.getChildren().add(error);
            }
        });
        pause.play();
    }

    private Node buildSkeletonCard() {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        card.setPrefWidth(300);
        card.setMinWidth(300);
        card.setStyle("-fx-padding: 0; -fx-overflow: hidden; -fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");

        Region imageSkeleton = new Region();
        imageSkeleton.setPrefHeight(160);
        imageSkeleton.setStyle("-fx-background-color: #e2e8f0;");
        
        VBox infoBox = new VBox(12);
        infoBox.setStyle("-fx-padding: 15;");
        
        Region titleSkeleton = new Region();
        titleSkeleton.setPrefSize(200, 20);
        titleSkeleton.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 4;");
        
        Region descSkeleton1 = new Region();
        descSkeleton1.setPrefSize(270, 10);
        descSkeleton1.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 2;");
        
        Region descSkeleton2 = new Region();
        descSkeleton2.setPrefSize(200, 10);
        descSkeleton2.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 2;");
        
        infoBox.getChildren().addAll(titleSkeleton, descSkeleton1, descSkeleton2);
        card.getChildren().addAll(imageSkeleton, infoBox);

        // Pulsing skeleton animation
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
        allCourseCards.clear();

        if (courses.isEmpty()) {
            VBox emptyStateBox = new VBox(15);
            emptyStateBox.setAlignment(Pos.CENTER);
            emptyStateBox.setStyle("-fx-padding: 50;");
            
            Label iconEmpty = new Label("🍃");
            iconEmpty.setStyle("-fx-font-size: 60px;");
            
            Label textEmpty = new Label("No courses available yet.");
            textEmpty.setStyle("-fx-text-fill: #64748b; -fx-font-size: 18px; -fx-font-weight: bold;");
            
            Label subEmpty = new Label("Check back later for exciting new content!");
            subEmpty.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px;");
            
            emptyStateBox.getChildren().addAll(iconEmpty, textEmpty, subEmpty);
            coursesContainer.getChildren().add(emptyStateBox);
            
            new FadeInUp(emptyStateBox).play();
            return;
        }

        for (Course course : courses) {
            Node card = buildCourseCard(course);
            String title = course.getTitre();
            card.setUserData(title != null ? title.toLowerCase() : "untitled");
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

    private void showFallbackIcon(StackPane container, Course course) {
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
        
        container.getStyleClass().add(iconClass);
        Label iconLabel = new Label(iconEmoji);
        iconLabel.setStyle("-fx-text-fill: white; -fx-font-size: 40px;"); // Larger for the big container
        container.getChildren().add(iconLabel);
    }

    private Node buildCourseCard(Course course) {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        card.setPrefWidth(300);
        card.setMinWidth(300);
        card.setStyle("-fx-padding: 0; -fx-overflow: hidden;"); // Clean edge for image

        StackPane visualContainer = new StackPane();
        visualContainer.setPrefHeight(160);
        visualContainer.setMinHeight(160);
        visualContainer.setMaxHeight(160);
        visualContainer.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 15 15 0 0;"); // Rounded top only

        if (course.getImage() != null && !course.getImage().isBlank()) {
            try {
                javafx.scene.image.ImageView courseImg = new javafx.scene.image.ImageView(new javafx.scene.image.Image(course.getImage(), true));
                courseImg.setFitWidth(300);
                courseImg.setFitHeight(160);
                courseImg.setPreserveRatio(false);
                
                javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(300, 160);
                clip.setArcWidth(30);
                clip.setArcHeight(30);
                courseImg.setClip(clip);
                
                visualContainer.getChildren().add(courseImg);
            } catch (Exception e) {
                showFallbackIcon(visualContainer, course);
            }
        } else {
            showFallbackIcon(visualContainer, course);
        }

        // Add NEW or UPDATED badge
        HBox badgeContainer = new HBox();
        badgeContainer.setAlignment(Pos.TOP_RIGHT);
        badgeContainer.setStyle("-fx-padding: 10;");
        
        boolean isNew = false;
        boolean isUpdated = false;
        
        try {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            
            if (course.getDateDeCreation() != null && !course.getDateDeCreation().isEmpty()) {
                String cleanDate = course.getDateDeCreation().replace(" ", "T");
                if (cleanDate.length() > 19) cleanDate = cleanDate.substring(0, 19);
                java.time.LocalDateTime created = java.time.LocalDateTime.parse(cleanDate);
                if (created.isAfter(now.minusDays(7))) {
                    isNew = true;
                }
            }
            
            if (!isNew && course.getDateDeModification() != null && !course.getDateDeModification().isEmpty()) {
                String cleanMod = course.getDateDeModification().replace(" ", "T");
                if (cleanMod.length() > 19) cleanMod = cleanMod.substring(0, 19);
                java.time.LocalDateTime modified = java.time.LocalDateTime.parse(cleanMod);
                if (modified.isAfter(now.minusDays(3))) {
                    isUpdated = true;
                }
            }
        } catch (Exception e) {
            // ignore parse errors
        }

        if (isNew) {
            Label newBadge = new Label("NEW ✨");
            newBadge.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 4 8; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(16,185,129,0.4), 5, 0, 0, 2);");
            badgeContainer.getChildren().add(newBadge);
            visualContainer.getChildren().add(badgeContainer);
        } else if (isUpdated) {
            Label upBadge = new Label("UPDATED");
            upBadge.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 4 8; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(59,130,246,0.4), 5, 0, 0, 2);");
            badgeContainer.getChildren().add(upBadge);
            visualContainer.getChildren().add(badgeContainer);
        }

        VBox infoBox = new VBox(12);
        infoBox.setStyle("-fx-padding: 15;");
        
        VBox titleBox = new VBox(2);
        String titre = course.getTitre();
        if (titre == null) titre = "Untitled Course";
        Label titleLabel = new Label(titre);
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setWrapText(true);
        String catName = categoryNames.getOrDefault(course.getCategorieId(), "General");
        if (catName == null) catName = "Unassigned";
        Label categoryLabel = new Label(catName.toUpperCase());
        categoryLabel.setStyle("-fx-text-fill: -fx-primary-pink; -fx-font-weight: bold; -fx-font-size: 10px;");
        titleBox.getChildren().addAll(categoryLabel, titleLabel);

        String descText = course.getDescription() != null ? stripHtmlTags(course.getDescription()) : "No description available.";
        if (descText.isBlank()) descText = "No description available.";
        Label descriptionLabel = new Label(descText);
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMinHeight(40);
        descriptionLabel.setMaxHeight(40);
        descriptionLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
        
        // Resource Completion Stats (X/Y)
        Users currentUser = SessionManager.getInstance().getCurrentUser();
        String stats = "0/0 Resources";
        if (currentUser != null) {
            try {
                services.UserCourseProgressService ps = new services.UserCourseProgressService();
                stats = ps.getCompletionStats(currentUser.getId(), course.getId());
            } catch (SQLException e) { e.printStackTrace(); }
        }

        HBox statsBox = new HBox(5);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        statsBox.setStyle("-fx-padding: 5 0;");
        Label statsLabel = new Label(stats);
        statsLabel.setStyle("-fx-text-fill: #ec4899; -fx-font-weight: bold; -fx-font-size: 13px;");
        Label iconLabel = new Label("📚");
        statsBox.getChildren().addAll(iconLabel, statsLabel);
        
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);
        
        Label exploreLink = new Label("EXPLORE RESOURCES →");
        exploreLink.getStyleClass().add("card-action-link");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        HBox interactionBox = new HBox(8);
        interactionBox.setAlignment(Pos.CENTER_RIGHT);
        
        boolean[] interactionState = {false, false}; // [0] is hasLiked, [1] is hasDisliked

        Button likeBtn = new Button("👍 " + course.getLikes());
        likeBtn.getStyleClass().add("btn-action-light");
        likeBtn.setStyle("-fx-font-size: 11px; -fx-padding: 3 8; -fx-background-radius: 12;");
        
        Button dislikeBtn = new Button("👎 " + course.getDislikes());
        dislikeBtn.getStyleClass().add("btn-action-light");
        dislikeBtn.setStyle("-fx-font-size: 11px; -fx-padding: 3 8; -fx-background-radius: 12;");

        likeBtn.setOnAction(e -> {
            e.consume(); // Prevent card click
            try {
                if (interactionState[0]) {
                    // Already liked -> Unlike
                    courseService.unlikeCourse(course.getId());
                    course.setLikes(course.getLikes() - 1);
                    interactionState[0] = false;
                    likeBtn.setStyle("-fx-font-size: 11px; -fx-padding: 3 8; -fx-background-radius: 12;");
                } else {
                    // Like it
                    courseService.likeCourse(course.getId());
                    course.setLikes(course.getLikes() + 1);
                    interactionState[0] = true;
                    likeBtn.setStyle("-fx-font-size: 11px; -fx-padding: 3 8; -fx-background-radius: 12; -fx-background-color: #fce7f3; -fx-text-fill: #db2777;");
                    
                    // If it was disliked, remove the dislike
                    if (interactionState[1]) {
                        courseService.undislikeCourse(course.getId());
                        course.setDislikes(course.getDislikes() - 1);
                        interactionState[1] = false;
                        dislikeBtn.setText("👎 " + course.getDislikes());
                        dislikeBtn.setStyle("-fx-font-size: 11px; -fx-padding: 3 8; -fx-background-radius: 12;");
                    }
                }
                likeBtn.setText("👍 " + course.getLikes());
            } catch (SQLException ex) { ex.printStackTrace(); }
        });
        
        dislikeBtn.setOnAction(e -> {
            e.consume(); // Prevent card click
            try {
                if (interactionState[1]) {
                    // Already disliked -> Undislike
                    courseService.undislikeCourse(course.getId());
                    course.setDislikes(course.getDislikes() - 1);
                    interactionState[1] = false;
                    dislikeBtn.setStyle("-fx-font-size: 11px; -fx-padding: 3 8; -fx-background-radius: 12;");
                } else {
                    // Dislike it
                    courseService.dislikeCourse(course.getId());
                    course.setDislikes(course.getDislikes() + 1);
                    interactionState[1] = true;
                    dislikeBtn.setStyle("-fx-font-size: 11px; -fx-padding: 3 8; -fx-background-radius: 12; -fx-background-color: #f1f5f9; -fx-text-fill: #475569;");
                    
                    // If it was liked, remove the like
                    if (interactionState[0]) {
                        courseService.unlikeCourse(course.getId());
                        course.setLikes(course.getLikes() - 1);
                        interactionState[0] = false;
                        likeBtn.setText("👍 " + course.getLikes());
                        likeBtn.setStyle("-fx-font-size: 11px; -fx-padding: 3 8; -fx-background-radius: 12;");
                    }
                }
                dislikeBtn.setText("👎 " + course.getDislikes());
            } catch (SQLException ex) { ex.printStackTrace(); }
        });
        
        interactionBox.getChildren().addAll(likeBtn, dislikeBtn);
        footer.getChildren().addAll(exploreLink, spacer, interactionBox);

        infoBox.getChildren().addAll(titleBox, descriptionLabel, statsBox, footer);
        card.getChildren().addAll(visualContainer, infoBox);
        
        card.setCursor(javafx.scene.Cursor.HAND);
        card.setOnMouseClicked(event -> openCourse(course));

        // Enhanced Hover animation
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-padding: 0; -fx-overflow: hidden; -fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(236,72,153,0.3), 15, 0, 0, 8);");
            javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(200), card);
            st.setToX(1.02); st.setToY(1.02); st.play();
        });
        
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-padding: 0; -fx-overflow: hidden; -fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");
            javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(200), card);
            st.setToX(1.0); st.setToY(1.0); st.play();
        });

        return card;
    }

    private void openCourse(Course course) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/front-resource-view.fxml"));
            Parent root = loader.load();
            FrontResourceController controller = loader.getController();
            controller.setCourse(course);
            
            if (FrontMainController.isPreviewModeActive()) {
                controller.setPreviewMode(true);
                if (root instanceof BorderPane) {
                    FrontMainController.loadContent(((BorderPane) root).getCenter());
                } else {
                    FrontMainController.loadContent(root);
                }
            } else {
                Stage stage = (Stage) coursesContainer.getScene().getWindow();
                stage.getScene().setRoot(root);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void loadTickets() {
        if (questionsContainer == null) return;
        try {
            Users currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) return;
            
            List<HelpTicket> myTickets = ticketService.getByCreator(currentUser.getId());
            renderTickets(myTickets);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void renderTickets(List<HelpTicket> tickets) {
        questionsContainer.getChildren().clear();
        if (tickets.isEmpty()) {
            Label empty = new Label("You haven't sent any help requests yet.");
            empty.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px; -fx-padding: 20;");
            questionsContainer.getChildren().add(empty);
            return;
        }

        for (HelpTicket t : tickets) {
            questionsContainer.getChildren().add(buildTicketCard(t));
        }
    }

    private Node buildTicketCard(HelpTicket t) {
        VBox card = new VBox(15);
        card.getStyleClass().add("question-card");

        // Header Row
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label(t.getSubject());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        HBox badges = new HBox(8);
        String status = t.getStatus() != null ? t.getStatus().toUpperCase() : "PENDING";
        
        Label statusBadge = new Label(status);
        statusBadge.getStyleClass().add(status.equals("RESOLVED") ? "badge-resolved" : "badge-pending");
        
        String priority = t.getPriority() != null ? t.getPriority() : "Moyenne";
        Label priorityBadge = new Label(priority);
        priorityBadge.getStyleClass().add(priority.equals("Urgent") || priority.equals("Haute") ? "badge-awaiting" : "badge-pending");
        
        badges.getChildren().addAll(statusBadge, priorityBadge);
        header.getChildren().addAll(title, spacer, badges);

        // Date & Course
        String dateStr = t.getCreatedAt() != null ? t.getCreatedAt().split(" ")[0] : "Recently";
        String courseInfo = "";
        if (t.getCourseId() != null) {
            String courseTitle = categoryNames.getOrDefault(t.getCourseId(), "Course #" + t.getCourseId());
            // Wait, categoryNames mapping is from loadCourses, it maps categoryId to categoryName.
            // I need course title. I'll just show the ID or skip for now to avoid complexity unless I fetch course title specifically.
            // Actually, I can use a simple trick: search in the courses list I already have.
            for(Course c : courses) { if(c.getId() == t.getCourseId()) { courseInfo = " • " + c.getTitre(); break; } }
        }
        Label infoLabel = new Label("Sent on " + dateStr + courseInfo);
        infoLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");

        // Content
        VBox contentBox = new VBox();
        contentBox.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 10; -fx-padding: 15;");
        Label content = new Label(t.getMessage());
        content.setWrapText(true);
        content.setStyle("-fx-text-fill: #334155; -fx-font-size: 13px;");
        contentBox.getChildren().add(content);

        card.getChildren().addAll(header, infoLabel, contentBox);

        // Admin Response
        if (t.getAdminResponse() != null && !t.getAdminResponse().isEmpty()) {
            VBox replyBox = new VBox(5);
            replyBox.getStyleClass().add("reply-box");
            Label adminLabel = new Label("ADMIN RESPONSE");
            adminLabel.getStyleClass().add("admin-reply-label");
            Label replyText = new Label(t.getAdminResponse());
            replyText.setWrapText(true);
            replyText.setStyle("-fx-text-fill: #065f46; -fx-font-size: 13px;");
            replyBox.getChildren().addAll(adminLabel, replyText);
            card.getChildren().add(replyBox);
        } else {
            Label noReply = new Label("No admin response yet — we'll notify you when they respond.");
            noReply.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px; -fx-font-style: italic;");
            card.getChildren().add(noReply);
        }

        // Actions
        HBox actions = new HBox(10);
        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("btn-action-light");
        editBtn.setOnAction(e -> onEditTicket(t));
        
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("btn-action-light");
        deleteBtn.setOnAction(e -> onDeleteTicket(t));
        
        actions.getChildren().addAll(editBtn, deleteBtn);
        card.getChildren().add(actions);

        return card;
    }

    @FXML
    private void onSendQuestion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/help-ticket-form-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) coursesContainer.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onEditTicket(HelpTicket t) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/help-ticket-form-view.fxml"));
            Parent root = loader.load();
            HelpTicketFormController controller = loader.getController();
            controller.setTicket(t);
            Stage stage = (Stage) coursesContainer.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onDeleteTicket(HelpTicket t) {
        try {
            if (!gui.util.AlertHelper.confirmDelete("request")) {
                return;
            }
            try {
                ticketService.delete(t.getId());
                loadTickets();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onManageCategories() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/category-list-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) coursesContainer.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @FXML
    private void onAddCourse() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/course-edit-view.fxml"));
            Parent root = loader.load();
            CourseFormController controller = loader.getController();
            controller.setCourse(null);
            Stage stage = (Stage) coursesContainer.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @FXML
    private void onViewQuestions() {
        loadTickets();
    }

    @FXML
    private void onGoToLeaderboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/front-leaderboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) coursesContainer.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String stripHtmlTags(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]*>", "").replace("&nbsp;", " ").trim();
    }
}
