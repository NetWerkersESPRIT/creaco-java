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
import javafx.stage.FileChooser;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
        gui.FrontMainController.setNavbarText("Edit Profile", "Account / Settings / Edit");
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

            // Handle Google accounts: lock password field
            if ("GOOGLE_AUTH".equals(currentUser.getPassword())) {
                txtPassword.setDisable(true);
                txtPassword.setPromptText("Google Authenticated User");
                txtPassword.setStyle("-fx-opacity: 0.8; -fx-background-color: #edf2f7;");
            } else {
                txtPassword.setDisable(false);
                txtPassword.setPromptText("Enter new password to change");
                txtPassword.setStyle("");
            }
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
            gui.FrontMainController.refreshNavbar();

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
    public void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(txtUsername.getScene().getWindow());
        
        if (selectedFile != null) {
            try {
                // Ensure the uploads directory exists
                File uploadDir = new File("src/main/resources/uploads");
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }

                // Create a unique name for the file to avoid collisions
                String fileName = UUID.randomUUID().toString() + "_" + selectedFile.getName();
                File destination = new File(uploadDir, fileName);

                // Copy the file
                Files.copy(selectedFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Update the avatar URL
                currentAvatarUrl = destination.toURI().toString();
                imgAvatar.setImage(new Image(currentAvatarUrl, true));
                
                showSuccess("✅ Image uploaded! (Save to apply)");
                
            } catch (Exception e) {
                e.printStackTrace();
                showError("❌ Failed to upload image: " + e.getMessage());
            }
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
