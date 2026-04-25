package gui.UsersControllers;

import entities.Users;
import services.UsersService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class EditUserController {

    @FXML private TextField txtUsername, txtEmail, txtNumtel, txtPoints;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblMessage;
    @FXML private Label lblTitle;
    @FXML private Label lblNavUsername;
    @FXML private Label lblNavUserRole;

    private final UsersService usersService = new UsersService();
    private Users currentUser;

    @FXML
    public void initialize() {
        gui.FrontMainController.setNavbarText("Edit User", "Pages / Administration / Users");
        // Populate Navbar Profile
        entities.Users current = utils.SessionManager.getInstance().getCurrentUser();
        if (current != null && lblNavUsername != null) {
            String displayName = current.getUsername() != null ? current.getUsername() : "User";
            lblNavUsername.setText(displayName);
            String role = current.getRole() != null ? current.getRole().replace("ROLE_", "") : "USER";
            lblNavUserRole.setText(role);
            if ("ADMIN".equals(role)) lblNavUserRole.setStyle("-fx-background-color: #434a75;");
        }
    }

    // Called by AdminController to pre-fill the form
    public void setUser(Users u) {
        this.currentUser = u;
        txtUsername.setText(u.getUsername());
        txtEmail.setText(u.getEmail());
        txtNumtel.setText(u.getNumtel());
        if (txtPoints != null) txtPoints.setText(String.valueOf(u.getPoints()));
        if (lblTitle != null) lblTitle.setText("Account Details: " + u.getUsername());
    }

    @FXML
    public void updateUser() {
        try {
            currentUser.setUsername(txtUsername.getText());
            currentUser.setEmail(txtEmail.getText());
            currentUser.setNumtel(txtNumtel.getText());
            if (txtPoints != null) {
                currentUser.setPoints(Integer.parseInt(txtPoints.getText()));
            }

            // Only update password if a new one was typed
            if (txtPassword.getText() != null && !txtPassword.getText().isBlank()) {
                currentUser.setPassword(txtPassword.getText());
            }

            usersService.modifier(currentUser);
            lblMessage.setText("✅ User updated successfully!");

            // Redirect back to admin panel
            goBack();

        } catch (Exception e) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("❌ Error: " + e.getMessage());

            // Show algebraic SQL error in Alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("SQL Operation Failed");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void goBack() throws Exception {
        javafx.scene.layout.StackPane contentArea = (javafx.scene.layout.StackPane) txtUsername.getScene().lookup("#contentArea");
        if (contentArea != null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Users/Admin.fxml"));
            contentArea.getChildren().setAll((javafx.scene.Node) loader.load());
        } else {
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.getScene().setRoot(FXMLLoader.load(getClass().getResource("/Users/Admin.fxml")));
        }
    }

    @FXML
    public void logout(javafx.event.ActionEvent event) {
        gui.SessionHelper.logout(event);
    }
}
