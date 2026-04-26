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



    @FXML private StackPane contentArea;
    @FXML private VBox dashboardView;
    @FXML private javafx.scene.layout.HBox previewBanner;
    @FXML private HBox topNav;
    @FXML private Label txtWelcome;
    @FXML private Label lblBreadcrumb;


    // User Profile in Navbar
    @FXML private Label lblUsername;
    @FXML private Label lblUserRole;
    @FXML private javafx.scene.image.ImageView imgNavAvatar;

    private static FrontMainController instance;

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

            updateNavbarProfile();
        }
    }

    public void setPreviewMode(boolean isPreview) {
        if (previewBanner != null) {
            previewBanner.setVisible(isPreview);
            previewBanner.setManaged(isPreview);
        }
        if (topNav != null) {
            topNav.setVisible(!isPreview);
            topNav.setManaged(!isPreview);
        }
        if (isPreview) {
            loadPreviewContent();
        }
    }

    private void loadPreviewContent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/front-courses-grid-view.fxml"));
            Parent root = loader.load();
            if (root instanceof BorderPane) {
                Node center = ((BorderPane) root).getCenter();
                contentArea.getChildren().setAll(center);
            } else {
                contentArea.getChildren().setAll(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void refreshNavbar() {
        if (instance != null) {
            instance.updateNavbarProfile();
        }
    }

    public static boolean isPreviewModeActive() {
        return instance != null && instance.previewBanner != null && instance.previewBanner.isVisible();
    }

    public static void loadContent(Node node) {
        if (instance != null) {
            instance.contentArea.getChildren().setAll(node);
        }
    }

    public static void setNavbarText(String title, String breadcrumb) {
        if (instance != null) {
            if (instance.txtWelcome != null) instance.txtWelcome.setText(title);
            if (instance.lblBreadcrumb != null) instance.lblBreadcrumb.setText(breadcrumb);
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
        System.out.println("Events section - Coming soon"); 
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
        if (txtWelcome != null) txtWelcome.setText("Collaborations");
        if (lblBreadcrumb != null) lblBreadcrumb.setText("Pages / Collaborations");
        loadSubView("/collaborator/ListCollaborator.fxml"); 
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
        onGoToDashboard(); 
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

    @FXML
    private void exitPreview(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/admin-courses-view.fxml"));
            Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }
}
