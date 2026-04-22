package gui.UsersControllers;

import entities.Users;
import services.UsersService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RegistrationController {

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String now = LocalDateTime.now().format(formatter);

    @FXML private TextField txtUsername, txtEmail, txtNumtel;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblMessage;

    private final UsersService usersService = new UsersService();

    private String validate() {
        String username  = txtUsername.getText().trim();
        String email     = txtEmail.getText().trim();
        String password  = txtPassword.getText();
        String numtel    = txtNumtel.getText().trim();

        if (username.length() < 4) return "❌ Username must be at least 4 characters.";
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) return "❌ Invalid email address.";
        if (password.length() < 8) return "❌ Password must be at least 8 characters.";
        if (!password.matches(".*[A-Z].*")) return "❌ Password must contain at least 1 uppercase letter.";
        if (!password.matches(".*[0-9].*")) return "❌ Password must contain at least 1 number.";
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) return "❌ Password must contain at least 1 special character.";
        if (!numtel.isEmpty() && !numtel.matches("^\\d{8}$")) return "❌ Phone number must be exactly 8 digits.";

        return null;
    }

    @FXML
    public void handleRegister() {
        String error = validate();
        if (error != null) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText(error);
            return;
        }

        try {
            Users u = new Users();
            u.setUsername(txtUsername.getText().trim());
            u.setEmail(txtEmail.getText().trim());
            u.setPassword(txtPassword.getText());
            u.setRole("ROLE_CONTENT_CREATOR");
            u.setNumtel(txtNumtel.getText().trim());
            u.setPoints(0);
            u.setCreated_at(now);

            usersService.ajouter(u);
            lblMessage.setStyle("-fx-text-fill: green;");
            lblMessage.setText("✅ Account created! Redirecting...");

            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
            pause.setOnFinished(e -> {
                try {
                    goBack();
                } catch (Exception ex) { ex.printStackTrace(); }
            });
            pause.play();

        } catch (Exception e) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("❌ Error: " + e.getMessage());
        }
    }

    @FXML
    public void goBack() throws Exception {
        Stage stage = (Stage) txtUsername.getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/Users/SignIn.fxml"))));
        stage.setTitle("CreaCo - Sign In");
    }
}
