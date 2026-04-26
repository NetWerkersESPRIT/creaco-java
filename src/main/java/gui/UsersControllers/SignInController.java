package gui.UsersControllers;

import entities.Users;
import services.UsersService;
import utils.SessionManager;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class SignInController {

    @FXML private TextField     txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private TextField     txtPasswordVisible;
    @FXML private Button        btnTogglePassword;
    @FXML private WebView       webViewRecaptcha;
    @FXML private Label         lblMessage;

    private String recaptchaToken = null;
    private final UsersService usersService = new UsersService();

    @FXML
    public void initialize() {
        setupRecaptcha();
    }

    private void setupRecaptcha() {
        WebEngine engine = webViewRecaptcha.getEngine();
        
        // 1. Load Site Key from config.properties
        Properties props = new Properties();
        String siteKey = "";
        File configFile = new File("config.properties");
        
        System.out.println("[reCAPTCHA] Looking for config at: " + configFile.getAbsolutePath());
        
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                props.load(fis);
                siteKey = props.getProperty("RECAPTCHA_SITE_KEY", "").trim();
                System.out.println("[reCAPTCHA] Key loaded (Length: " + siteKey.length() + ")");
            } catch (IOException e) {
                System.err.println("[reCAPTCHA] Error loading config.properties: " + e.getMessage());
            }
        } else {
            System.err.println("[reCAPTCHA] config.properties NOT FOUND at root!");
        }

        final String finalSiteKey = siteKey;

        // 2. Load the static HTML
        URL url = getClass().getResource("/Users/recaptcha.html");
        if (url != null) {
            engine.load(url.toExternalForm());
            
            // 3. Inject the key once the page is loaded
            engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    System.out.println("[reCAPTCHA] Page loaded, injecting key...");
                    try {
                        engine.executeScript("initRecaptcha('" + finalSiteKey + "')");
                    } catch (Exception e) {
                        System.err.println("[reCAPTCHA] JS Injection failed: " + e.getMessage());
                    }
                }
            });

            // 4. Listen for verification token
            engine.locationProperty().addListener((obs, oldLocation, newLocation) -> {
                if (newLocation != null) {
                    if (newLocation.contains("recaptcha-verify:")) {
                        recaptchaToken = newLocation.substring(newLocation.indexOf(":") + 1);
                        showSuccess("✅ Verification successful!");
                    } else if (newLocation.contains("recaptcha-expired:")) {
                        recaptchaToken = null;
                        showError("❌ Verification expired. Please try again.");
                    }
                }
            });
        }
    }

    @FXML
    public void handleLogin() {
        String email    = txtEmail.getText().trim();
        String password = txtPassword.isVisible() ? txtPassword.getText() : txtPasswordVisible.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("❌ Please fill in all fields.");
            return;
        }

        /*
        if (recaptchaToken == null || recaptchaToken.isEmpty()) {
            showError("❌ Please complete the security verification.");
            return;
        }
        */

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

            if (user.isBanned()) {
                String banMessage = "Your account has been suspended by an administrator.";
                gui.util.AlertHelper.showCustomAlert("Account Suspended", banMessage, gui.util.AlertHelper.AlertType.WARNING);
                return;
            }

            boolean match = UsersService.verifyBcrypt(password, user.getPassword());
            if (!match) {
                showError("❌ Incorrect password.");
                return;
            }

            SessionManager.getInstance().setCurrentUser(user);

            Stage stage = (Stage) txtEmail.getScene().getWindow();
            String fxmlPath = "/gui/front-main-view.fxml";
            stage.getScene().setRoot(FXMLLoader.load(getClass().getResource(fxmlPath)));
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

    private void showSuccess(String msg) {
        lblMessage.setStyle("-fx-text-fill: #38a169;");
        lblMessage.setText(msg);
    }
}
