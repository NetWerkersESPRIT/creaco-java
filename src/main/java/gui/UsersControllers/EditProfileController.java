package gui.UsersControllers;

import entities.Users;
import services.UsersService;
import utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

public class EditProfileController {

    @FXML private TextField txtUsername;
    @FXML private TextField txtEmail;
    @FXML private TextField txtNumtel;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblMessage;

    private final UsersService usersService = new UsersService();
    private Users currentUser;

    @FXML
    public void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            txtUsername.setText(currentUser.getUsername() != null ? currentUser.getUsername() : "");
            txtEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
            txtNumtel.setText(currentUser.getNumtel() != null ? currentUser.getNumtel() : "");
            // Password intentionally left blank for security
        }
    }

    @FXML
    public void saveProfile() {
        if (currentUser == null) {
            showError("No user session found. Please log in again.");
            return;
        }

        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();
        String numtel = txtNumtel.getText().trim();
        String password = txtPassword.getText();

        // Validation
        if (username.length() < 4) {
            showError("❌ Username must be at least 4 characters.");
            return;
        }
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            showError("❌ Invalid email address format.");
            return;
        }
        if (!numtel.isEmpty() && !numtel.matches("^\\d{8}$")) {
            showError("❌ Phone number must be exactly 8 digits.");
            return;
        }
        if (!password.isEmpty()) {
            if (password.length() < 8) {
                showError("❌ Password must be at least 8 characters.");
                return;
            }
            if (!password.matches(".*[A-Z].*")) {
                showError("❌ Password must contain at least 1 uppercase letter.");
                return;
            }
            if (!password.matches(".*[0-9].*")) {
                showError("❌ Password must contain at least 1 digit.");
                return;
            }
        }

        try {
            currentUser.setUsername(username);
            currentUser.setEmail(email);
            currentUser.setNumtel(numtel);
            if (!password.isEmpty()) {
                currentUser.setPassword(password);
            }

            usersService.modifier(currentUser);

            // Update session with new info
            SessionManager.getInstance().setCurrentUser(currentUser);

            showSuccess("✅ Profile updated successfully!");

            // Go back to profile view after short delay
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.2));
            pause.setOnFinished(e -> goBack());
            pause.play();

        } catch (Exception e) {
            e.printStackTrace();
            showError("❌ Error: " + e.getMessage());
        }
    }

    @FXML
    public void goBack() {
        try {
            StackPane contentArea = (StackPane) txtUsername.getScene().lookup("#contentArea");
            if (contentArea != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Users/Profile.fxml"));
                javafx.scene.Node root = loader.load();
                contentArea.getChildren().setAll(root);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String msg) {
        lblMessage.setStyle("-fx-text-fill: #e53e3e; -fx-font-size: 13px; -fx-font-weight: bold;");
        lblMessage.setText(msg);
    }

    private void showSuccess(String msg) {
        lblMessage.setStyle("-fx-text-fill: #38a169; -fx-font-size: 13px; -fx-font-weight: bold;");
        lblMessage.setText(msg);
    }
}
