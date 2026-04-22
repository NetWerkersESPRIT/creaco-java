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
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import entities.HelpTicket;
import services.HelpTicketService;
import services.CourseService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FrontMainController {

    private final CourseService courseService = new CourseService();

    private List<Course> courses = Collections.emptyList();
    private List<Node> allCourseCards = new ArrayList<>();

    @FXML private HBox coursesContainer;
    @FXML private VBox ticketsContainer;
    @FXML private TextField searchField;
    @FXML private Button btnFilterAll;
    @FXML private Button btnFilterPending;
    private final HelpTicketService ticketService = new HelpTicketService();

    @FXML
    private void initialize() {
        loadCourses();
        loadTickets();

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterCourses(newVal);
        });
    }

    @FXML
    private void handleEditTicket(HelpTicket t) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/help-request-dialog.fxml"));
            Parent root = loader.load();
            
            HelpRequestController controller = loader.getController();
            controller.setTicket(t);
            
            Stage stage = new Stage();
            stage.setTitle("Edit Assistance Request");
            stage.setScene(new Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            loadTickets(); // Refresh list
        } catch (java.io.IOException e) {
            AlertHelper.showError("UI Error", "Could not open edit dialog: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteTicket(HelpTicket t) {
        if (AlertHelper.showConfirmation("Delete Request", "Are you sure you want to delete this assistance request?")) {
            try {
                ticketService.deleteTicket(t.getId());
                loadTickets(); // Refresh list
            } catch (SQLException e) {
                AlertHelper.showError("Database Error", "Unable to delete request: " + e.getMessage());
            }
        }
    }

    @FXML
    private void filterAllTickets() {
        btnFilterAll.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 6 20;");
        btnFilterPending.setStyle("-fx-background-color: transparent; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-padding: 6 20; -fx-text-fill: #4a5568;");
        loadTickets();
    }

    @FXML
    private void filterPendingTickets() {
        btnFilterPending.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 6 20;");
        btnFilterAll.setStyle("-fx-background-color: transparent; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-padding: 6 20; -fx-text-fill: #4a5568;");
        try {
            List<HelpTicket> tickets = ticketService.getTicketsByCreator(1); // Placeholder
            ticketsContainer.getChildren().clear();
            for (HelpTicket t : tickets) {
                if (t.getStatus().equalsIgnoreCase("Pending")) {
                    ticketsContainer.getChildren().add(buildTicketCard(t));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadTickets() {
        try {
            List<HelpTicket> tickets = ticketService.getTicketsByCreator(1); // Placeholder: use SessionHelper.getUserId()
            ticketsContainer.getChildren().clear();
            for (HelpTicket t : tickets) {
                ticketsContainer.getChildren().add(buildTicketCard(t));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Node buildTicketCard(HelpTicket t) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-width: 1;");

        // Header: Subject + Status Badges
        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label subject = new Label(t.getSubject());
        subject.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1a1a1a;");
        
        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);
        
        Label statusBadge = new Label(t.getStatus());
        statusBadge.setStyle("-fx-background-color: #eff6ff; -fx-text-fill: #3b82f6; -fx-padding: 4 12; -fx-background-radius: 10; -fx-font-size: 11px; -fx-font-weight: bold;");
        
        Label replyBadge = new Label(t.getAdminResponse() != null ? "Replied" : "Awaiting reply");
        String replyBg = t.getAdminResponse() != null ? "#f0fdf4" : "#fff7ed";
        String replyText = t.getAdminResponse() != null ? "#16a34a" : "#c2410c";
        replyBadge.setStyle("-fx-background-color: " + replyBg + "; -fx-text-fill: " + replyText + "; -fx-padding: 4 12; -fx-background-radius: 10; -fx-font-size: 11px; -fx-font-weight: bold;");
        
        header.getChildren().addAll(subject, hSpacer, statusBadge, replyBadge);

        Label date = new Label("Sent on " + t.getCreatedAt().substring(0, 10));
        date.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");

        // User Message Box
        Label message = new Label(t.getMessage());
        message.setWrapText(true);
        message.setMaxWidth(800);
        message.setStyle("-fx-background-color: #f8fafc; -fx-padding: 15; -fx-background-radius: 8; -fx-text-fill: #334155; -fx-font-size: 14px;");

        card.getChildren().addAll(header, date, message);

        // Admin Reply Section
        if (t.getAdminResponse() != null) {
            VBox replyBox = new VBox(8);
            Label replyHeader = new Label("ADMIN REPLY");
            replyHeader.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #16a34a;");
            
            Label replyContent = new Label(t.getAdminResponse());
            replyContent.setWrapText(true);
            replyContent.setStyle("-fx-background-color: #f0fdf4; -fx-padding: 15; -fx-background-radius: 8; -fx-text-fill: #166534; -fx-font-size: 14px;");
            
            replyBox.getChildren().addAll(replyHeader, replyContent);
            card.getChildren().add(replyBox);
        } else {
            Label noReply = new Label("No admin reply yet — we'll notify you when they respond.");
            noReply.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px; -fx-font-style: italic;");
            card.getChildren().add(noReply);
        }

        // Action Buttons
        HBox actions = new HBox(10);
        
        if (t.getStatus().equalsIgnoreCase("Pending")) {
            Button editBtn = new Button("Edit");
            editBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-padding: 6 20; -fx-font-weight: bold; -fx-cursor: hand;");
            editBtn.setOnAction(e -> handleEditTicket(t));
            actions.getChildren().add(editBtn);
        }
        
        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-padding: 6 20; -fx-font-weight: bold; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> handleDeleteTicket(t));
        
        actions.getChildren().add(deleteBtn);
        card.getChildren().add(actions);

        return card;
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
        card.setPadding(new Insets(24));
        card.setPrefWidth(280);
        card.setMinHeight(380);
        // Match project background color and add a subtle border/shadow
        card.setStyle("-fx-background-color: #f4f7fe; -fx-background-radius: 20; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");

        // Category Tag
        Label tag = new Label("Course"); 
        tag.setStyle("-fx-background-color: white; -fx-text-fill: #4f46e5; -fx-padding: 4 15; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold; -fx-border-color: #e2e8f0; -fx-border-radius: 12;");
        
        Label title = new Label(course.getTitre());
        title.setWrapText(true);
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2d3748;"); // Dark text
        title.setMinHeight(50);

        Label desc = new Label(course.getDescription() == null ? "-" : course.getDescription());
        desc.setWrapText(true);
        VBox.setVgrow(desc, Priority.ALWAYS);
        desc.setStyle("-fx-text-fill: #718096; -fx-font-size: 13px; -fx-line-spacing: 3;"); // Muted dark text
        desc.setMaxHeight(100);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button openBtn = new Button("Open");
        openBtn.setMaxWidth(Double.MAX_VALUE);
        openBtn.setPrefHeight(45);
        // Applying the vibrant gradient from the image
        openBtn.setStyle("-fx-background-color: linear-gradient(to right, #ce2d7c, #6c2db1); " +
                        "-fx-background-radius: 15; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand; " +
                        "-fx-font-size: 14px;");
        
        openBtn.setOnAction(e -> openCourse(course));
        
        // Hover effects with slightly brighter gradient
        openBtn.setOnMouseEntered(e -> openBtn.setStyle("-fx-background-color: linear-gradient(to right, #e13a8c, #7d35ce); " +
                                                        "-fx-background-radius: 15; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 14px;"));
        openBtn.setOnMouseExited(e -> openBtn.setStyle("-fx-background-color: linear-gradient(to right, #ce2d7c, #6c2db1); " +
                                                       "-fx-background-radius: 15; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 14px;"));

        card.getChildren().addAll(tag, title, desc, spacer, openBtn);
        card.setUserData(course.getTitre().toLowerCase());

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
            stage.showAndWait();
            loadTickets(); // Refresh the list after the dialog is closed
        } catch (java.io.IOException e) {
            AlertHelper.showError("UI Error", "Could not open Help-Desk: " + e.getMessage());
        }
    }
}