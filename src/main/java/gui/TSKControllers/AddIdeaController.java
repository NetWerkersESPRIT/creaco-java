package gui.TSKControllers;

import entities.Idea;
import services.IdeaService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.SQLException;

public class AddIdeaController {
    @FXML private TextField txtTitle, txtCategory;
    @FXML private TextArea txtDescription;
    @FXML private Label lblMessage;

    private final IdeaService ideaService = new IdeaService();

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
        Stage stage = (Stage) txtTitle.getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/TSK/Idea.fxml"))));
    }
}
