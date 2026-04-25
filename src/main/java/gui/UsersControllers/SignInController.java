package gui.UsersControllers;

import entities.Users;
import services.UsersService;
import utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SignInController {

    @FXML private TextField     txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private TextField     txtPasswordVisible;
    @FXML private Button        btnTogglePassword;
    @FXML private Label         lblMessage;

    private final UsersService usersService = new UsersService();

    @FXML
    public void handleLogin() {
        String email    = txtEmail.getText().trim();
        String password = txtPassword.isVisible() ? txtPassword.getText() : txtPasswordVisible.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("❌ Please fill in all fields.");
            return;
        }

        // Email validation restriction same as AddUser
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            showError("❌ Invalid email address format.");
            return;
        }

        try {
            Users user = usersService.findByEmail(email);

            if (user == null) {
                showError("❌ No account found with this email.");
                return;
            }

            // Check if user is banned
            if (user.isBanned()) {
                String banMessage = "Your account has been suspended by an administrator.\n\n" +
                                   "If you believe this is a mistake, please contact support.";
                
                // Show ONLY the popup alert as requested
                gui.util.AlertHelper.showCustomAlert(
                    "Account Suspended", 
                    banMessage, 
                    gui.util.AlertHelper.AlertType.WARNING
                );
                return;
            }

            // Verify bcrypt password
            boolean match = UsersService.verifyBcrypt(password, user.getPassword());
            if (!match) {
                showError("❌ Incorrect password.");
                return;
            }

            // Store in session
            SessionManager.getInstance().setCurrentUser(user);

            // Route to unified Dashboard A
            Stage stage = (Stage) txtEmail.getScene().getWindow();
            stage.getScene().setRoot(FXMLLoader.load(getClass().getResource("/gui/front-main-view.fxml")));
            stage.setMaximized(true);
            stage.setTitle("CreaCo Dashboard");

        } catch (Exception e) {
            showError("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void togglePassword() {
        if (txtPassword.isVisible()) {
            txtPasswordVisible.setText(txtPassword.getText());
            txtPasswordVisible.setVisible(true);
            txtPassword.setVisible(false);
            btnTogglePassword.setText("🙈");
        } else {
            txtPassword.setText(txtPasswordVisible.getText());
            txtPassword.setVisible(true);
            txtPasswordVisible.setVisible(false);
            btnTogglePassword.setText("👁");
        }
    }

    @FXML
    public void goToRegister() throws Exception {
        Stage stage = (Stage) txtEmail.getScene().getWindow();
        stage.getScene().setRoot(FXMLLoader.load(getClass().getResource("/Users/CreateAccount.fxml")));
        stage.setTitle("Create Account");
    }

    private void showError(String msg) {
        lblMessage.setStyle("-fx-text-fill: #EA0606;");
        lblMessage.setText(msg);
    }
}
