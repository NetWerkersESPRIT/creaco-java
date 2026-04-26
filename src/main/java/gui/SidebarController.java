package gui;

import entities.Users;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utils.SessionManager;

import java.io.IOException;

public class SidebarController {

    @FXML private Label lblAdminHeader;
    @FXML private VBox boxAdmin;
    @FXML private Label lblUXHeader;
    @FXML private Button btnPreview;

    @FXML
    public void initialize() {
        Users user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            boolean isAdmin = "ROLE_ADMIN".equals(user.getRole());
            if (lblAdminHeader != null) {
                lblAdminHeader.setVisible(isAdmin);
                lblAdminHeader.setManaged(isAdmin);
            }
            if (boxAdmin != null) {
                boxAdmin.setVisible(isAdmin);
                boxAdmin.setManaged(isAdmin);
            }

            // Hide Preview Mode for Content Creators
            boolean isContentCreator = "ROLE_CONTENT_CREATOR".equals(user.getRole());
            if (isContentCreator) {
                if (lblUXHeader != null) {
                    lblUXHeader.setVisible(false);
                    lblUXHeader.setManaged(false);
                }
                if (btnPreview != null) {
                    btnPreview.setVisible(false);
                    btnPreview.setManaged(false);
                }
            }
        }
    }

    @FXML
    private void onGoToDashboard(ActionEvent event) {
        navigateTo(event, "/gui/front-main-view.fxml");
    }

    @FXML
    private void onShowConnectedUsers(ActionEvent event) {
        navigateTo(event, "/Users/Admin.fxml");
    }

    @FXML
    private void onShowPostModeration(ActionEvent event) {
        navigateTo(event, "/post/postModeration.fxml");
    }

    @FXML
    private void onShowForumStats(ActionEvent event) {
        navigateTo(event, "/post/forumStats.fxml");
    }

    @FXML
    private void onShowIdeas(ActionEvent event) {
        navigateTo(event, "/TSK/Idea.fxml");
    }

    @FXML
    private void onShowMissions(ActionEvent event) {
        navigateTo(event, "/TSK/Mission.fxml");
    }

    @FXML
    private void onShowTasks(ActionEvent event) {
        navigateTo(event, "/TSK/Tasks.fxml");
    }

    @FXML
    private void onShowEvents(ActionEvent event) {
        System.out.println("Events - Coming soon");
    }

    @FXML
    private void onShowForum(ActionEvent event) {
        navigateTo(event, "/post/displayPost.fxml");
    }

    @FXML
    private void onShowCollaborations(ActionEvent event) {
        navigateTo(event, "/collaborator/ListCollaborator.fxml");
    }

    @FXML
    private void onShowCourses(ActionEvent event) {
        Users user = SessionManager.getInstance().getCurrentUser();
        if (user != null && "ROLE_ADMIN".equals(user.getRole())) {
            navigateTo(event, "/gui/admin-courses-view.fxml");
        } else {
            navigateTo(event, "/gui/front-courses-grid-view.fxml");
        }
    }

    @FXML
    private void onGoToPreview(ActionEvent event) {
        gui.PreviewHelper.goToPreview(event);
    }

    @FXML
    private void onLogout(ActionEvent event) {
        gui.SessionHelper.logout(event);
    }

    private void navigateTo(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
