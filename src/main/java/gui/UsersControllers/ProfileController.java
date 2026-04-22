package gui.UsersControllers;

import entities.Users;
import utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class ProfileController {

    @FXML private Label lblUsername, lblEmail, lblNumtel, lblPoints, lblCreatedAt;

    @FXML
    public void initialize() {
        Users user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            lblUsername.setText(user.getUsername());
            lblEmail.setText(user.getEmail());
            lblNumtel.setText(user.getNumtel() != null ? user.getNumtel() : "Not set");
            lblPoints.setText(user.getPoints() + " XP");
            lblCreatedAt.setText(user.getCreated_at() != null ? user.getCreated_at() : "-");
        }
    }

    @FXML
    public void goToEditProfile() throws Exception {
        StackPane contentArea = (StackPane) lblUsername.getScene().lookup("#contentArea");
        if (contentArea != null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Users/EditProfile.fxml"));
            contentArea.getChildren().setAll((javafx.scene.Node) loader.load());
        }
    }
}
