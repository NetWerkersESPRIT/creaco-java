package gui.UsersControllers;

import entities.Users;
import services.UsersService;
import utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import java.util.UUID;

public class EditProfileController {

    @FXML private TextField txtUsername;
    @FXML private TextField txtEmail;
    @FXML private TextField txtNumtel;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtAiPrompt;
    @FXML private ImageView imgAvatar;
    @FXML private Label lblMessage;

    private String currentAvatarUrl;

    private final UsersService usersService = new UsersService();
    private Users currentUser;

    @FXML
    public void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            txtUsername.setText(currentUser.getUsername() != null ? currentUser.getUsername() : "");
            txtEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
            txtNumtel.setText(currentUser.getNumtel() != null ? currentUser.getNumtel() : "");
            
            // Load existing avatar or default
            currentAvatarUrl = currentUser.getImage();
            if (currentAvatarUrl == null || currentAvatarUrl.isEmpty()) {
                currentAvatarUrl = "https://api.dicebear.com/7.x/avataaars/png?seed=" + currentUser.getUsername();
            }
            imgAvatar.setImage(new Image(currentAvatarUrl, true)); // true = load in background
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
            currentUser.setImage(currentAvatarUrl);

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
    public void generateAvatar() {
        String prompt = txtAiPrompt.getText().trim();
        
        if (prompt.isEmpty()) {
            // Fallback to random Dicebear if no prompt
            String randomSeed = UUID.randomUUID().toString();
            currentAvatarUrl = "https://api.dicebear.com/7.x/avataaars/png?seed=" + randomSeed;
            showSuccess("✨ Random avatar generated! (Save to keep it)");
        } else {
            // Use Pollinations AI for custom prompts
            try {
                String encodedPrompt = java.net.URLEncoder.encode(prompt, "UTF-8");
                currentAvatarUrl = "https://image.pollinations.ai/prompt/" + encodedPrompt + "?width=512&height=512&nologo=true";
                showSuccess("🎨 AI is generating: '" + prompt + "'...");
            } catch (Exception e) {
                currentAvatarUrl = "https://api.dicebear.com/7.x/avataaars/png?seed=" + prompt;
                showError("❌ Error encoding prompt, using fallback.");
            }
        }
        
        imgAvatar.setImage(new Image(currentAvatarUrl, true));
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
