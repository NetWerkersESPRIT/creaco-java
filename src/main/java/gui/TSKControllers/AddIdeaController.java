package gui.TSKControllers;

import entities.Idea;
import services.IdeaService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.sql.SQLException;

public class AddIdeaController {
    @FXML private TextField txtTitle, txtCategory;
    @FXML private TextArea txtDescription;
    @FXML private Label lblMessage;

    private final IdeaService ideaService = new IdeaService();
    
    @FXML
    public void initialize() {
        gui.FrontMainController.setNavbarText("Share Your Idea", "Innovation / New");
    }

    @FXML public void goToIdea()    throws Exception { switchScene("/TSK/Idea.fxml"); }
    @FXML public void goToMission() throws Exception { switchScene("/TSK/Mission.fxml"); }
    @FXML public void goToTasks()   throws Exception { switchScene("/TSK/Tasks.fxml"); }
    @FXML public void goToAdmin()   throws Exception { switchScene("/Users/Admin.fxml"); }

    private void switchScene(String fxml) throws Exception {
        StackPane contentArea = (StackPane) txtTitle.getScene().lookup("#contentArea");
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        javafx.scene.Parent root = loader.load();
        javafx.scene.Node view = root;

        if (root instanceof BorderPane) {
            view = ((BorderPane) root).getCenter();
        }

        if (contentArea != null) {
            contentArea.getChildren().setAll(view);
        } else {
            Stage stage = (Stage) txtTitle.getScene().getWindow();
            stage.getScene().setRoot(root);
        }
    }

    @FXML
    public void saveIdea() {
        if (txtTitle.getText().isEmpty() || txtDescription.getText().isEmpty()) {
            lblMessage.setText("❌ Title and Description are required.");
            lblMessage.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            Idea idea = new Idea();
            idea.setTitle(txtTitle.getText().trim());
            idea.setDescription(txtDescription.getText().trim());
            idea.setCategory(txtCategory.getText().trim());
            idea.setCreator_id(1); // creator_id is always 1

            ideaService.ajouter(idea);
            lblMessage.setText("✅ Idea added successfully!");
            lblMessage.setStyle("-fx-text-fill: green;");
            clearFields();
        } catch (SQLException e) {
            lblMessage.setText("❌ Error: " + e.getMessage());
            lblMessage.setStyle("-fx-text-fill: red;");
        }
    }

    private void clearFields() {
        txtTitle.clear();
        txtDescription.clear();
        txtCategory.clear();
    }

    @FXML
    public void goBack() throws Exception {
        goToIdea();
    }
}
