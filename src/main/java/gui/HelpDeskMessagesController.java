package gui;

import entities.Users;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import utils.SessionManager;

import java.io.IOException;
import java.util.List;

public class HelpDeskMessagesController {

    @FXML private StackPane rootStackPane;
    @FXML private StackPane mainContentContainer;
    
    @FXML private Label lblBreadcrumb;
    @FXML private Label lblTitle;
    @FXML private Label lblAdminName;
    @FXML private ImageView imgAdminAvatar;

    // Sidebar Buttons
    @FXML private Button btnDashboard;
    @FXML private Button btnUsers;
    @FXML private Button btnModeration;
    @FXML private Button btnForum;
    @FXML private Button btnCourses;

    @FXML
    public void initialize() {
        setupAdminInfo();
        // Optionally load a default view
        // onShowDashboard();
    }

    private void setupAdminInfo() {
        Users admin = SessionManager.getInstance().getCurrentUser();
        if (admin != null) {
            lblAdminName.setText(admin.getUsername());
            String avatarUrl = admin.getImage();
            if (avatarUrl == null || avatarUrl.isEmpty()) {
                avatarUrl = "https://api.dicebear.com/7.x/avataaars/png?seed=" + admin.getUsername();
            }
            try {
                imgAdminAvatar.setImage(new Image(avatarUrl, true));
            } catch (Exception e) {
                System.err.println("Failed to load admin avatar: " + e.getMessage());
            }
        }
    }

    @FXML
    public void onShowDashboard() {
        updateActiveButton(btnDashboard);
        lblBreadcrumb.setText("Pages / Admin / Support Hub");
        lblTitle.setText("Support & Mentorat Hub");
        loadSubView("/gui/admin-support-hub.fxml");
    }

    @FXML
    public void onShowUsers() {
        updateActiveButton(btnUsers);
        lblBreadcrumb.setText("Pages / Admin / Users");
        lblTitle.setText("User Management");
        loadSubView("/Users/Admin.fxml");
    }

    @FXML
    public void onShowModeration() {
        updateActiveButton(btnModeration);
        lblBreadcrumb.setText("Pages / Admin / Moderation");
        lblTitle.setText("Content Moderation");
        loadSubView("/post/postModeration.fxml");
    }

    @FXML
    public void onShowForum() {
        updateActiveButton(btnForum);
        lblBreadcrumb.setText("Pages / Admin / Forum");
        lblTitle.setText("Forum Administration");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/post/displayPost.fxml"));
            Parent root = loader.load();
            gui.post.DisplayPostController controller = loader.getController();
            if (controller != null) controller.setAdminMode(true);
            mainContentContainer.getChildren().setAll(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    public void onShowCourses() {
        updateActiveButton(btnCourses);
        lblBreadcrumb.setText("Pages / Admin / Courses");
        lblTitle.setText("Courses Dashboard");
        loadSubView("/gui/admin-courses-view.fxml");
    }

    @FXML
    public void logout(javafx.event.ActionEvent event) {
        gui.SessionHelper.logout(event);
    }

    private void loadSubView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            mainContentContainer.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading subview: " + fxmlPath);
        }
    }

    private void updateActiveButton(Button activeButton) {
        List<Button> buttons = List.of(btnDashboard, btnUsers, btnModeration, btnForum, btnCourses);
        for (Button btn : buttons) {
            if (btn == null) continue;
            btn.getStyleClass().remove("sidebar-item-active");
            // Also need to handle the StackPane inside the graphic if we want to be perfect
            // But let's stick to the main class for now
            if (btn == activeButton) {
                if (!btn.getStyleClass().contains("sidebar-item-active")) {
                    btn.getStyleClass().add("sidebar-item-active");
                }
            }
        }
    }
}
