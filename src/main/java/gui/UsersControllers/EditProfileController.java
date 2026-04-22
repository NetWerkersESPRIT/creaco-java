package gui.UsersControllers;

import entities.Users;
import services.UsersService;
import utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

public class EditProfileController {

    @FXML private TextField txtUsername, txtEmail, txtNumtel;
    @FXML private PasswordField txtCurrentPassword, txtNewPassword;
    @FXML private Label lblMessage;

    private final UsersService usersService = new UsersService();

    @FXML
    public void initialize() {
        Users user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            txtUsername.setText(user.getUsername());
            txtEmail.setText(user.getEmail());
            txtNumtel.setText(user.getNumtel() != null ? user.getNumtel() : "");
        }
    }

    @FXML
    public void handleUpdateProfile() {
        Users currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();
        String numtel = txtNumtel.getText().trim();
        String currentPwd = txtCurrentPassword.getText();
        String newPwd = txtNewPassword.getText();

        if (username.isEmpty() || email.isEmpty()) {
            showMessage("❌ Username and Email are required.", true);
            return;
        }

        try {
            // Password change logic
            if (!newPwd.isEmpty()) {
                if (currentPwd.isEmpty()) {
                    showMessage("❌ Current password is required to change password.", true);
                    return;
                }
                
                // Verify current password
                if (!UsersService.verifyBcrypt(currentPwd, currentUser.getPassword())) {
                    showMessage("❌ Incorrect current password.", true);
                    return;
                }

                // Validate new password
                if (newPwd.length() < 8 || !newPwd.matches(".*[A-Z].*") || !newPwd.matches(".*[0-9].*")) {
                    showMessage("❌ New password too weak (min 8 chars, 1 Upper, 1 Digit).", true);
                    return;
                }
                
                currentUser.setPassword(newPwd); // UsersService.modifier will hash it
            }

            // Update allowed fields
            currentUser.setUsername(username);
            currentUser.setEmail(email);
            currentUser.setNumtel(numtel);

            usersService.modifier(currentUser);
            
            // Re-fetch to ensure session is sync'd (optional but good)
            SessionManager.getInstance().setCurrentUser(currentUser);

            showMessage("✅ Profile updated successfully!", false);
            
            // Go back after delay
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
            pause.setOnFinished(e -> {
                try {
                    goBack();
                } catch (Exception ex) { ex.printStackTrace(); }
            });
            pause.play();

        } catch (Exception e) {
            showMessage("❌ Error: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    private void showMessage(String msg, boolean isError) {
        lblMessage.setText(msg);
        lblMessage.setStyle(isError ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
    }

    @FXML
    public void goBack() throws Exception {
        StackPane contentArea = (StackPane) txtUsername.getScene().lookup("#contentArea");
        if (contentArea != null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Users/Profile.fxml"));
            contentArea.getChildren().setAll((javafx.scene.Node) loader.load());
        }
    }
}
