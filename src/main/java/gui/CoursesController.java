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

    @FXML
    public void initialize() {
        if (mainContentBox != null) {
            new FadeInUp(mainContentBox).setSpeed(0.8).play();
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

        VBox infoBox = new VBox(12);
        infoBox.setStyle("-fx-padding: 15;");
        
        VBox titleBox = new VBox(2);
        Label titleLabel = new Label(course.getTitre());
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setWrapText(true);
        String catName = categoryNames.getOrDefault(course.getCategorieId(), "General").toUpperCase();
        Label categoryLabel = new Label(catName);
        categoryLabel.setStyle("-fx-text-fill: -fx-primary-pink; -fx-font-weight: bold; -fx-font-size: 10px;");
        titleBox.getChildren().addAll(categoryLabel, titleLabel);

        Label descriptionLabel = new Label(course.getDescription() != null ? course.getDescription() : "No description available.");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMinHeight(40);
        descriptionLabel.setMaxHeight(40);
        descriptionLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
        
        // Progress bar removed as requested
        
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);
        
        Label exploreLink = new Label("EXPLORE RESOURCES →");
        exploreLink.getStyleClass().add("card-action-link");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        HBox interactionBox = new HBox(8);
        interactionBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button likeBtn = new Button("👍 " + course.getLikes());
        likeBtn.getStyleClass().add("btn-action-light");
        likeBtn.setStyle("-fx-font-size: 11px; -fx-padding: 3 8; -fx-background-radius: 12;");
        likeBtn.setOnAction(e -> {
            e.consume(); // Prevent card click
            try {
                courseService.likeCourse(course.getId());
                course.setLikes(course.getLikes() + 1);
                likeBtn.setText("👍 " + course.getLikes());
            } catch (SQLException ex) { ex.printStackTrace(); }
        });
        
        Button dislikeBtn = new Button("👎 " + course.getDislikes());
        dislikeBtn.getStyleClass().add("btn-action-light");
        dislikeBtn.setStyle("-fx-font-size: 11px; -fx-padding: 3 8; -fx-background-radius: 12;");
        dislikeBtn.setOnAction(e -> {
            e.consume(); // Prevent card click
            try {
                courseService.dislikeCourse(course.getId());
                course.setDislikes(course.getDislikes() + 1);
                dislikeBtn.setText("👎 " + course.getDislikes());
            } catch (SQLException ex) { ex.printStackTrace(); }
        });
        
        interactionBox.getChildren().addAll(likeBtn, dislikeBtn);
        footer.getChildren().addAll(exploreLink, spacer, interactionBox);

        infoBox.getChildren().addAll(titleBox, descriptionLabel, footer);
        card.getChildren().addAll(visualContainer, infoBox);
        
        card.setCursor(javafx.scene.Cursor.HAND);
        card.setOnMouseClicked(event -> openCourse(course));

        // Hover animation
        card.setOnMouseEntered(e -> new Pulse(card).setSpeed(2.0).play());

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
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete this request?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait().ifPresent(type -> {
                if (type == ButtonType.YES) {
                    try {
                        ticketService.delete(t.getId());
                        loadTickets();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            });
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

}
