package gui.UsersControllers;

import entities.Users;
import utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class ProfileController {

    @FXML private Label lblUsername, lblEmail, lblNumtel, lblPoints, lblCreatedAt;
    @FXML private Label lblInitial, lblUserRole;

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

            if (lblInitial != null) lblInitial.setText(String.valueOf(name.charAt(0)).toUpperCase());
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
