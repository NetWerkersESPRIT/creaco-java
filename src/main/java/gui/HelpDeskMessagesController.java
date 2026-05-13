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
    
    private static HelpDeskMessagesController instance;

    public static HelpDeskMessagesController getInstance() {
        return instance;
    }
    
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
        instance = this;
        setupAdminInfo();
        // Optionally load a default view
        // onShowDashboard();
    }

    private void setupAdminInfo() {
        Users admin = SessionManager.getInstance().getCurrentUser();
        if (admin != null) {
            lblAdminName.setText(admin.getUsername());
            String avatarUrl = admin.getAvatarUrl();
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
        lblBreadcrumb.setText("Pages / Admin / Dashboard");
        lblTitle.setText("Admin Workspace");
        // Clear main container or load a welcome view
        mainContentContainer.getChildren().clear();
        Label welcome = new Label("Welcome to the Admin Panel");
        welcome.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        mainContentContainer.getChildren().add(welcome);
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

    public void loadSubViewWithTitle(String fxmlPath, String title) {
        if (lblTitle != null) lblTitle.setText(title);
        loadSubView(fxmlPath);
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
