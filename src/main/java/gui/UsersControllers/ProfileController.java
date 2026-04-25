package gui.UsersControllers;

import entities.Users;
import utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class ProfileController {

    @FXML private Label lblUsername, lblEmail, lblNumtel, lblPoints, lblCreatedAt;
    @FXML private Label lblUserRole;
    @FXML private ImageView imgAvatar;

    private Users currentUser;

    @FXML
    public void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            String name = currentUser.getUsername() != null ? currentUser.getUsername() : "Unknown";
            lblUsername.setText(name);
            lblEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "-");
            lblNumtel.setText(currentUser.getNumtel() != null && !currentUser.getNumtel().isEmpty()
                    ? currentUser.getNumtel() : "Not set");
            lblPoints.setText(currentUser.getPoints() + " XP");
            lblCreatedAt.setText(currentUser.getCreated_at() != null ? currentUser.getCreated_at() : "-");

            // Load avatar
            String avatarUrl = currentUser.getImage();
            if (avatarUrl == null || avatarUrl.isEmpty()) {
                avatarUrl = "https://api.dicebear.com/7.x/avataaars/png?seed=" + name;
            }
            if (imgAvatar != null) {
                imgAvatar.setImage(new Image(avatarUrl, true));
            }

            if (lblUserRole != null) {
                String role = currentUser.getRole() != null
                        ? currentUser.getRole().replace("ROLE_", "") : "USER";
                lblUserRole.setText(role);
            }
        }
    }

    @FXML
    public void goToEditProfile() {
        try {
            StackPane contentArea = (StackPane) lblUsername.getScene().lookup("#contentArea");
            if (contentArea != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Users/EditProfile.fxml"));
                javafx.scene.Node root = loader.load();
                contentArea.getChildren().setAll(root);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Could not open Edit Profile: " + e.getMessage());
        }
    }
}
