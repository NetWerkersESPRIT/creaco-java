package gui.UsersControllers;

import entities.Users;
import services.UsersService;
import utils.SessionManager;
import utils.GoogleAuthService;
import com.google.api.services.oauth2.model.Userinfo;
// import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
// import javafx.scene.web.WebEngine;
// import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.Properties;

public class SignInController {

    @FXML
    private TextField txtEmail;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private TextField txtPasswordVisible;
    @FXML
    private Button btnTogglePassword;
//    @FXML
//    private WebView webViewRecaptcha;
    @FXML
    private Label lblMessage;

//    private String recaptchaToken = null;
    private final UsersService usersService = new UsersService();

    @FXML
    public void initialize() {
        javafx.application.Platform.runLater(() -> {
            if (txtEmail.getScene() != null) {
                Stage stage = (Stage) txtEmail.getScene().getWindow();
                if (stage != null) stage.setMaximized(true);
            }
        });
//        setupRecaptcha();
    }

//    private void setupRecaptcha() {
//        WebEngine engine = webViewRecaptcha.getEngine();
//
//        // 1. Load Site Key from config.properties
//        Properties props = new Properties();
//        String siteKey = "";
//        File configFile = new File("config.properties");
//
//        System.out.println("[reCAPTCHA] Looking for config at: " + configFile.getAbsolutePath());
//
//        if (configFile.exists()) {
//            try (FileInputStream fis = new FileInputStream(configFile)) {
//                props.load(fis);
//                siteKey = props.getProperty("RECAPTCHA_SITE_KEY", "").trim();
//                System.out.println("[reCAPTCHA] Key loaded (Length: " + siteKey.length() + ")");
//            } catch (IOException e) {
//                System.err.println("[reCAPTCHA] Error loading config.properties: " + e.getMessage());
//            }
//        } else {
//            System.err.println("[reCAPTCHA] config.properties NOT FOUND at root!");
//        }
//
//        final String finalSiteKey = siteKey;
//
//        // 2. Load the static HTML via XAMPP localhost (avoids file:// domain restriction for reCAPTCHA)
//        String recaptchaUrl = "http://localhost/recaptcha.html";
//        engine.load(recaptchaUrl);
//
//        // 3. Once the page loads: register Java bridge + inject site key
//        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
//            if (newState == Worker.State.SUCCEEDED) {
//                System.out.println("[reCAPTCHA] Page loaded, registering Java bridge...");
//                try {
//                    // Expose this controller to JavaScript as window.javaApp
//                    netscape.javascript.JSObject window =
//                            (netscape.javascript.JSObject) engine.executeScript("window");
//                    window.setMember("javaApp", this);
//
//                    // Inject site key into the widget
//                    engine.executeScript("initRecaptcha('" + finalSiteKey + "')");
//                    System.out.println("[reCAPTCHA] Bridge registered and key injected.");
//                } catch (Exception e) {
//                    System.err.println("[reCAPTCHA] Bridge setup failed: " + e.getMessage());
//                }
//            }
//        });
//    }
//
//    /**
//     * Called by JavaScript (window.javaApp.onToken) when reCAPTCHA is solved.
//     * Must be public so the JS bridge can invoke it via reflection.
//     */
//    public void onToken(String token) {
//        System.out.println("[reCAPTCHA] Token received via bridge, length=" + token.length());
//        recaptchaToken = token;
//        javafx.application.Platform.runLater(() -> showSuccess("✅ Verification successful!"));
//    }
//
//    /**
//     * Called by JavaScript (window.javaApp.onExpired) when reCAPTCHA expires.
//     */
//    public void onExpired() {
//        System.out.println("[reCAPTCHA] Token expired.");
//        recaptchaToken = null;
//        javafx.application.Platform.runLater(() -> showError("❌ Verification expired. Please try again."));
//    }

    @FXML
    public void handleLogin() {
        String email = txtEmail.getText().trim();
        String password = txtPassword.isVisible() ? txtPassword.getText() : txtPasswordVisible.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("❌ Please fill in all fields.");
            return;
        }
 
        
//        if (recaptchaToken == null || recaptchaToken.isEmpty()) {
//            showError("❌ Please complete the security verification.");
//            return;
//        }

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
                gui.util.AlertHelper.showCustomAlert("Account Suspended", banMessage,
                        gui.util.AlertHelper.AlertType.WARNING);
                return;
            }

            // 1. Check if this is a Google account first
            if ("GOOGLE_AUTH".equals(user.getPassword())) {
                showError("❌ This is a Google account. Please use 'Sign in with Google'.");
                return;
            }

            // 2. Only if it's a normal account, verify the Bcrypt password
            boolean match = UsersService.verifyBcrypt(password, user.getPassword());
            if (!match) {
                showError("❌ Incorrect password.");
                return;
            }

            SessionManager.getInstance().setCurrentUser(user);

            Stage stage = (Stage) txtEmail.getScene().getWindow();
            String fxmlFile = "/gui/front-first-view.fxml";
            stage.getScene().setRoot(FXMLLoader.load(getClass().getResource(fxmlFile)));
            stage.setMaximized(true);
            stage.setTitle("CreaCo Dashboard");

        } catch (Exception e) {
            showError("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleGoogleLogin() {
        try {
            Userinfo googleUser = GoogleAuthService.authenticate();
            String email = googleUser.getEmail();

            if (email == null || email.isEmpty()) {
                showError("❌ Failed to retrieve email from Google.");
                return;
            }

            Users user = usersService.findByEmail(email);

            if (user == null) {
                showError("❌ User not found. Please create an account first.");
                return;
            }

            // Verify it's a Google account
            if (!"GOOGLE_AUTH".equals(user.getPassword())) {
                showError("❌ This email is registered with a password. Please sign in normally.");
                return;
            }

            if (user.isBanned()) {
                showError("❌ This account is suspended.");
                return;
            }

            // Authenticate and navigate
            SessionManager.getInstance().setCurrentUser(user);
            showSuccess("✅ Signed in with Google!");
            
            Stage stage = (Stage) txtEmail.getScene().getWindow();
            String fxmlFile = "/gui/front-first-view.fxml";
            stage.getScene().setRoot(FXMLLoader.load(getClass().getResource(fxmlFile)));
            stage.setMaximized(true);

        } catch (Exception e) {
            e.printStackTrace();
            showError("❌ Google Sign-In failed: " + e.getMessage());
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
        stage.setMaximized(true);
        stage.getScene().setRoot(FXMLLoader.load(getClass().getResource("/Users/CreateAccount.fxml")));
        stage.setTitle("Create Account");
    }

    @FXML
    public void goToWelcome() throws Exception {
        Stage stage = (Stage) txtEmail.getScene().getWindow();
        stage.setMaximized(true);
        stage.getScene().setRoot(FXMLLoader.load(getClass().getResource("/Users/Welcome.fxml")));
        stage.setTitle("CreaCo - Welcome");
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
