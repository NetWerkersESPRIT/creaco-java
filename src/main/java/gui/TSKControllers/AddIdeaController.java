package gui.TSKControllers;

import entities.Idea;
import services.IdeaService;
import gui.util.AlertHelper;
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
        String title = txtTitle.getText().trim();
        String description = txtDescription.getText().trim();
        String proposedCategory = txtCategory.getText().trim();

        if (title.isEmpty() || description.isEmpty()) {
            lblMessage.setText("❌ Title and Description are required.");
            lblMessage.setStyle("-fx-text-fill: red;");
            return;
        }

        lblMessage.setText("🤖 AI is normalizing category...");
        lblMessage.setStyle("-fx-text-fill: #3b82f6;");

        new Thread(() -> {
            try {
                // Fetch existing categories for the AI to consider
                java.util.List<String> existingCategories = ideaService.getDistinctCategories();
                
                // Classify the category using AI
                String finalCategory = services.IdeaCategoryClassifier.classify(
                    title, description, proposedCategory, existingCategories
                );

                javafx.application.Platform.runLater(() -> {
                    try {
                        Idea idea = new Idea();
                        idea.setTitle(title);
                        idea.setDescription(description);
                        idea.setCategory(finalCategory);
                        
                        entities.Users currentUser = utils.SessionManager.getInstance().getCurrentUser();
                        if (currentUser != null) {
                            idea.setCreator_id(currentUser.getId());
                        } else {
                            idea.setCreator_id(1); // Fallback
                        }

                        ideaService.ajouter(idea);
                        
                        txtCategory.setText(finalCategory); // Show the autocorrected category
                        boolean confirmed = AlertHelper.showCustomAlert(
                            "Success", 
                            "✅ Idea added successfully (Category: " + finalCategory + ")", 
                            AlertHelper.AlertType.INFORMATION
                        );
                        
                        if (confirmed) {
                            try {
                                goToIdea();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (SQLException e) {
                        lblMessage.setText("❌ Error: " + e.getMessage());
                        lblMessage.setStyle("-fx-text-fill: red;");
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    lblMessage.setText("❌ Error during classification: " + e.getMessage());
                    lblMessage.setStyle("-fx-text-fill: red;");
                });
            }
        }).start();
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
