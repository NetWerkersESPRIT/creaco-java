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
import gui.post.DisplayPostController;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FrontMainController {

    @FXML private FlowPane coursesContainer;
    @FXML private TextField searchField;
    @FXML private StackPane contentArea;
    @FXML private Button floatingTutorBtn;
    private Runnable floatingButtonAction;
    @FXML private StackPane rootStackPane;
    @FXML private VBox dashboardView;
    @FXML private Label txtWelcome;
    @FXML private Label lblBreadcrumb;

    // Sidebar items
    @FXML private Label lblAdminHeader;
    @FXML private VBox boxAdmin;
    @FXML private HBox boxAdminActions;
    @FXML private Button btnMyTeam;
    @FXML private Button btnUserGroups;

    // User Profile in Navbar
    @FXML private Label lblUsername;
    @FXML private Label lblUserRole;
    @FXML private javafx.scene.image.ImageView imgNavAvatar;
    @FXML private Button btnNotifications;
    @FXML private javafx.scene.layout.StackPane notifBadge;
    @FXML private Label lblNotifCount;

    private javafx.stage.Popup notificationPopup;
    private gui.notification.NotificationListController notificationListController;
    private javafx.animation.Timeline notificationRefreshTimeline;

    private static FrontMainController instance;

    public static FrontMainController getInstance() {
        return instance;
    }

    @FXML
    private void initialize() {
        instance = this;

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

            updateNavbarProfile();

            boolean isAdmin = SessionManager.getInstance().isAdmin();
            lblAdminHeader.setVisible(isAdmin);
            lblAdminHeader.setManaged(isAdmin);
            boxAdmin.setVisible(isAdmin);
            boxAdmin.setManaged(isAdmin);
            
            if (boxAdminActions != null) {
                boxAdminActions.setVisible(isAdmin);
                boxAdminActions.setManaged(isAdmin);
            }

            boolean isContentCreator = SessionManager.getInstance().isContentCreator();
            if (btnMyTeam != null) {
                btnMyTeam.setVisible(isContentCreator);
                btnMyTeam.setManaged(isContentCreator);
            }

            boolean isTeamMember = SessionManager.getInstance().isManager() || "ROLE_MEMBER".equals(user.getRole());
            if (btnUserGroups != null) {
                btnUserGroups.setVisible(isTeamMember);
                btnUserGroups.setManaged(isTeamMember);
            }

            checkUnreadNotifications();
            startNotificationRefresh();
        }
    }

    private void checkUnreadNotifications() {
        Users user = SessionManager.getInstance().getCurrentUser();
        if (user != null && notifBadge != null) {
            services.NotificationService ns = new services.NotificationService();
            List<entities.Notification> notifications = ns.getNotificationsForUser(user.getId());
            long unreadCount = notifications.stream().filter(n -> !n.isRead()).count();
            
            if (unreadCount > 0) {
                lblNotifCount.setText(unreadCount > 9 ? "9+" : String.valueOf(unreadCount));
                notifBadge.setVisible(true);
                notifBadge.setManaged(true);
            } else {
                notifBadge.setVisible(false);
                notifBadge.setManaged(false);
            }
        }
    }

    private void startNotificationRefresh() {
        if (notificationRefreshTimeline != null) {
            notificationRefreshTimeline.stop();
        }

        notificationRefreshTimeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(4), e -> refreshNotificationsLive())
        );
        notificationRefreshTimeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        notificationRefreshTimeline.play();
    }

    private void refreshNotificationsLive() {
        checkUnreadNotifications();
        if (notificationPopup != null && notificationPopup.isShowing() && notificationListController != null) {
            notificationListController.refreshNotifications();
        }
    }

    @FXML
    private void onShowNotifications() {
        if (notificationPopup != null && notificationPopup.isShowing()) {
            notificationPopup.hide();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/notification/notification_list.fxml"));
            Parent root = loader.load();
            notificationListController = loader.getController();

            notificationPopup = new javafx.stage.Popup();
            notificationPopup.getContent().add(root);
            notificationPopup.setAutoHide(true);

            notificationListController.setOnCloseCallback(() -> {
                notificationPopup.hide();
                checkUnreadNotifications();
            });

            notificationPopup.setOnHidden(e -> {
                notificationListController = null;
                checkUnreadNotifications();
            });

            // Position popup below the button
            javafx.geometry.Bounds bounds = btnNotifications.localToScreen(btnNotifications.getBoundsInLocal());
            notificationPopup.show(btnNotifications, bounds.getMinX() - 360, bounds.getMaxY() + 10);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void refreshNavbar() {
        if (instance != null) {
            instance.updateNavbarProfile();
        }
    }

    public static void setNavbarText(String title, String breadcrumb) {
        if (instance != null) {
            if (instance.txtWelcome != null) instance.txtWelcome.setText(title);
            if (instance.lblBreadcrumb != null) instance.lblBreadcrumb.setText(breadcrumb);
        }
    }

    private Node floatingChatNode;
    private gui.chat.FloatingChatController floatingChatController;

    public void openFloatingChat(int conversationId) {
        if (floatingChatNode != null) {
            rootStackPane.getChildren().remove(floatingChatNode);
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/chat/floating_chat.fxml"));
            floatingChatNode = loader.load();
            floatingChatController = loader.getController();
            
            floatingChatController.setConversationId(conversationId);
            floatingChatController.setOnClose(() -> {
                rootStackPane.getChildren().remove(floatingChatNode);
                floatingChatNode = null;
                floatingChatController = null;
            });

            rootStackPane.getChildren().add(floatingChatNode);
            StackPane.setAlignment(floatingChatNode, Pos.BOTTOM_RIGHT);
            StackPane.setMargin(floatingChatNode, new javafx.geometry.Insets(0, 20, 20, 0));
            
            // Bring to front
            floatingChatNode.toFront();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showFloatingChat(int conversationId) {
        if (instance != null) {
            instance.openFloatingChat(conversationId);
        }
    }

    private void updateNavbarProfile() {
        Users user = SessionManager.getInstance().getCurrentUser();
        if (user != null && imgNavAvatar != null) {
            String displayName = user.getUsername() != null ? user.getUsername() : "User";
            lblUsername.setText(displayName);
            String avatarUrl = user.getImage();
            if (avatarUrl == null || avatarUrl.isEmpty()) {
                avatarUrl = "https://api.dicebear.com/7.x/avataaars/png?seed=" + user.getUsername();
            }
            imgNavAvatar.setImage(new javafx.scene.image.Image(avatarUrl, true));
        }
    }

    @FXML
    private void onGoToDashboard() {
        setFloatingButtonVisible(false, null);
        if (txtWelcome != null) txtWelcome.setText("Explore");
        if (lblBreadcrumb != null) lblBreadcrumb.setText("Pages / Dashboard");
        contentArea.getChildren().setAll(dashboardView);
    }

    @FXML public void onShowIdeas() { 
        if (txtWelcome != null) txtWelcome.setText("Ideas Hub");
        if (lblBreadcrumb != null) lblBreadcrumb.setText("Pages / Ideas");
        loadSubView("/TSK/Idea.fxml"); 
    }

    @FXML public void onShowMissions() { 
        if (txtWelcome != null) txtWelcome.setText("Missions");
        if (lblBreadcrumb != null) lblBreadcrumb.setText("Pages / Missions");
        loadSubView("/TSK/Mission.fxml"); 
    }

    @FXML public void onShowTasks() { 
        if (txtWelcome != null) txtWelcome.setText("Tasks");
        if (lblBreadcrumb != null) lblBreadcrumb.setText("Pages / Tasks");
        loadSubView("/TSK/Tasks.fxml"); 
    }

    @FXML public void onShowEvents() { 
        if (txtWelcome != null) txtWelcome.setText("Events");
        if (lblBreadcrumb != null) lblBreadcrumb.setText("Pages / Events");
        loadSubView("/events/Event.fxml");
    }

    @FXML public void onShowReservations() { 
        if (txtWelcome != null) txtWelcome.setText("Reservations");
        if (lblBreadcrumb != null) lblBreadcrumb.setText("Pages / Reservations");
        loadSubView("/events/Reservation.fxml");
    }

    @FXML public void onShowConnectedUsers() { 
        if (txtWelcome != null) txtWelcome.setText("User Management");
        if (lblBreadcrumb != null) lblBreadcrumb.setText("Pages / Admin / Users");
        loadSubView("/Users/Admin.fxml"); 
    }

    @FXML public void onShowPostModeration() { 
        if (txtWelcome != null) txtWelcome.setText("Post Moderation");
        if (lblBreadcrumb != null) lblBreadcrumb.setText("Pages / Admin / Forum");
        loadSubView("/post/postModeration.fxml"); 
    }

    @FXML public void onShowForumStats() {
        if (txtWelcome != null) txtWelcome.setText("Forum Analytics");
        if (lblBreadcrumb != null) lblBreadcrumb.setText("Pages / Admin / Analytics");
        loadSubView("/post/forumStats.fxml");
    }

    @FXML
    public void onShowForum() {
        if (txtWelcome != null) txtWelcome.setText("Forum");
        if (lblBreadcrumb != null) lblBreadcrumb.setText("Pages / Forum");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/post/displayPost.fxml"));
            Parent root = loader.load();
            gui.post.DisplayPostController controller = loader.getController();
            if (controller != null) {
                controller.setAdminMode(false);
            }
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load forum: " + e.getMessage());
        }
    }

    @FXML public void onShowCollaborations() { 
        if (txtWelcome != null) txtWelcome.setText("Collaborations Hub");
        if (lblBreadcrumb != null) lblBreadcrumb.setText("Pages / Collaborations");
        
        if (SessionManager.getInstance().isAdmin()) {
            loadSubView("/gui/collab/admin/dashboard.fxml");
        } else if (SessionManager.getInstance().isManager()) {
            loadSubView("/gui/collab/manager/dashboard.fxml");
        } else {
            loadSubView("/gui/collab/collab_dashboard.fxml");
        }
    }

    @FXML public void onShowMyTeam() {
        if (txtWelcome != null) txtWelcome.setText("Manage Team");
        if (lblBreadcrumb != null) lblBreadcrumb.setText("Pages / Team Management");
        loadSubView("/Users/GroupManagement.fxml");
    }

    @FXML public void onShowUserGroups() {
        if (txtWelcome != null) txtWelcome.setText("My Teams");
        if (lblBreadcrumb != null) lblBreadcrumb.setText("Pages / Joined Teams");
        loadSubView("/Users/UserGroupsDashboard.fxml");
    }

    @FXML
    public void onShowCourses() { 
        if (SessionManager.getInstance().isAdmin()) {
            loadSubView("/gui/admin-courses-view.fxml");
            setNavbarText("Admin Course Management", "Pages / Admin / Courses");
        } else if (SessionManager.getInstance().isContentCreator()) {
            loadSubView("/gui/front-courses-grid-view.fxml");
            setNavbarText("Content Creator Hub", "Pages / Courses");
        } else {
            onGoToDashboard(); 
        }
    }

    @FXML
    public void onManageCategories() {
        if (txtWelcome != null) txtWelcome.setText("Manage Categories");
        if (lblBreadcrumb != null) lblBreadcrumb.setText("Pages / Courses / Categories");
        loadSubView("/gui/category-list-view.fxml");
    }

    @FXML
    public void onAddCourse() {
        if (txtWelcome != null) txtWelcome.setText("Add New Course");
        if (lblBreadcrumb != null) lblBreadcrumb.setText("Pages / Courses / Add Course");
        try {
            var resource = getClass().getResource("/gui/create-course-form.fxml");
            if (resource == null) {
                System.err.println("Navigation error: Resource not found: /gui/create-course-form.fxml");
                return;
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            CourseFormController controller = loader.getController();
            if (controller != null) {
                controller.setCourse(null);
            }
            setContent(root);
        } catch (IOException exception) {
            System.err.println("Navigation error: " + exception.getMessage());
            exception.printStackTrace();
        }
    }

    @FXML
    private void onViewQuestions() {
        onShowHelpTicketForm(null);
    }

    public void onShowHelpTicketForm(entities.HelpTicket ticket) {
        if (txtWelcome != null) txtWelcome.setText(ticket == null ? "Send Question" : "Edit Question");
        if (lblBreadcrumb != null) lblBreadcrumb.setText("Pages / Courses / Help");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/help-ticket-form-view.fxml"));
            Parent root = loader.load();
            if (ticket != null) {
                // Assuming HelpTicketFormController is the name
                Object controller = loader.getController();
                if (controller instanceof gui.HelpTicketFormController) {
                    ((gui.HelpTicketFormController) controller).setTicket(ticket);
                }
            }
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onShowLeaderboard() {
        if (txtWelcome != null) txtWelcome.setText("Learning Leaderboard");
        if (lblBreadcrumb != null) lblBreadcrumb.setText("Pages / Courses / Leaderboard");
        loadSubView("/gui/front-leaderboard.fxml");
    }

    private void loadSubView(String fxmlPath) {
        setFloatingButtonVisible(false, null);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            setContent(root);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load sub-view: " + fxmlPath);
        }
    }

    public void setFloatingButtonVisible(boolean visible, Runnable action) {
        if (floatingTutorBtn != null) {
            floatingTutorBtn.setVisible(visible);
            floatingTutorBtn.setManaged(visible);
            this.floatingButtonAction = action;
        }
    }

    @FXML
    private void onFloatingTutorClick() {
        if (floatingButtonAction != null) {
            floatingButtonAction.run();
        }
    }

    // Dummy handlers to prevent LoadExceptions if stale FXML snippets are present in cache
    @FXML private void openTutorModal() {}
    @FXML private void closeTutorModal() {}
    @FXML private void sendChatMessage() {}
    @FXML private void closeResourceModal() {}

    public void setContent(Parent root) {
        setFloatingButtonVisible(false, null);
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
    }


    public void openCourse(Course course) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/front-resource-view.fxml"));
            Parent root = loader.load();
            FrontResourceController controller = loader.getController();
            
            if (controller != null) {
                controller.setCourse(course);
            }

            // Standard sub-view loading pattern
            if (root instanceof BorderPane) {
                contentArea.getChildren().setAll(((BorderPane) root).getCenter());
            } else {
                contentArea.getChildren().setAll(root);
            }

            setNavbarText("Resources: " + course.getTitre(), "Pages / Courses / " + course.getTitre());

        } catch (IOException e) { 
            e.printStackTrace(); 
            System.err.println("Error loading resource view: " + e.getMessage());
        }
    }

    public void openAdminCourse(Course course) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/admin-resource-view.fxml"));
            Parent root = loader.load();
            AdminResourceController controller = loader.getController();
            
            if (controller != null) {
                controller.setCourse(course);
            }

            // Standard sub-view loading pattern
            if (root instanceof BorderPane) {
                contentArea.getChildren().setAll(((BorderPane) root).getCenter());
            } else {
                contentArea.getChildren().setAll(root);
            }

            setNavbarText("Manage Resources: " + course.getTitre(), "Pages / Courses / Manage Resources");

        } catch (IOException e) { 
            e.printStackTrace(); 
            System.err.println("Error loading admin resource view: " + e.getMessage());
        }
    }

    @FXML public void logout(javafx.event.ActionEvent event) { gui.SessionHelper.logout(event); }

    @FXML
    private void onOpenProfile(javafx.scene.input.MouseEvent event) {
        if (txtWelcome != null) txtWelcome.setText("Edit Profile");
        if (lblBreadcrumb != null) lblBreadcrumb.setText("Pages / Profile");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Users/Profile.fxml"));
            Parent root = loader.load();
            contentArea.getChildren().setAll((javafx.scene.Node) root);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Could not load Profile view: " + e.getMessage());
        }
    }
}
