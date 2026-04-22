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

    private final UsersService usersService = new UsersService();
    private Users currentUser;

    // Called by AdminController to pre-fill the form
    public void setUser(Users u) {
        this.currentUser = u;
        txtUsername.setText(u.getUsername());
        txtEmail.setText(u.getEmail());
        txtNumtel.setText(u.getNumtel());
        txtPoints.setText(String.valueOf(u.getPoints()));
        // password left blank intentionally
    }

    @FXML
    public void updateUser() {
        try {
            currentUser.setUsername(txtUsername.getText());
            currentUser.setEmail(txtEmail.getText());
            currentUser.setNumtel(txtNumtel.getText());
            currentUser.setPoints(Integer.parseInt(txtPoints.getText()));

            // Only update password if a new one was typed
            if (!txtPassword.getText().isBlank()) {
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
        Stage stage = (Stage) txtUsername.getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/Users/Admin.fxml"))));
    }
}
