package gui.UsersControllers;

import entities.Users;
import services.UsersService;
import utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SignInController {

    @FXML private TextField     txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Label         lblMessage;

    private final UsersService usersService = new UsersService();

    @FXML
    public void handleLogin() {
        String email    = txtEmail.getText().trim();
        String password = txtPassword.getText();

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
