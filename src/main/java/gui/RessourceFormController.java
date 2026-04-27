package gui;

import entities.Course;
import entities.Ressource;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.InlineCssTextArea;
import javafx.stage.Stage;
import services.RessourceService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import io.github.cdimascio.dotenv.Dotenv;
import java.io.File;
import java.util.Map;
import javafx.stage.FileChooser;

public class RessourceFormController {

    private final RessourceService ressourceService = new RessourceService();

    private Course course;
    private Ressource ressource;

    @FXML
    private Label titleLabel;

    @FXML
    private Label subtitleLabel;

    @FXML
    private TextField nameField;

    @FXML
    private Label nameErrorLabel;

    @FXML
    private TextField urlField;

    @FXML
    private Label urlErrorLabel;

    @FXML
    private InlineCssTextArea contentArea;

    @FXML
    private StackPane contentContainer;

    @FXML
    public void initialize() {
        if (contentContainer != null) {
            contentArea = new InlineCssTextArea();
            contentArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 13px;");
            VirtualizedScrollPane<InlineCssTextArea> vsPane = new VirtualizedScrollPane<>(contentArea);
            contentContainer.getChildren().add(vsPane);
            VBox.setVgrow(contentContainer, Priority.ALWAYS);
        }
    }

    @FXML
    private javafx.scene.control.Button btnParaphrase;

    private File selectedFile;

    @FXML
    private void onBrowseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Resource File");
        File file = fileChooser.showOpenDialog(nameField.getScene().getWindow());
        if (file != null) {
            selectedFile = file;
            urlField.setText(file.getAbsolutePath());
        }
    }

    public void setContext(Course course, Ressource ressource) {
        this.course = course;
        this.ressource = ressource;
        boolean editing = ressource != null;

        titleLabel.setText(editing ? "Edit Ressource" : "Add Ressource");
        subtitleLabel.setText((editing ? "Update" : "Create") + " a resource for " + course.getTitre());

        if (editing) {
            populateForm();
        }
    }

    @FXML
    private void onCancel() {
        openRessourceList();
    }

    @FXML
    private void onParaphrase() {
        String content = contentArea.getText();
        if (content == null || content.isBlank()) {
            AlertHelper.showError("Input Required", "Please enter some content to paraphrase first.");
            return;
        }

        // Disable button to show work in progress
        contentArea.setDisable(true);
        String originalText = btnParaphrase.getText();
        btnParaphrase.setText("✨ Processing...");
        btnParaphrase.setDisable(true);

        // Run in a separate thread to keep UI responsive
        new Thread(() -> {
            String response = utils.GeminiService.getParaphrase(content);
            
            javafx.application.Platform.runLater(() -> {
                if (response != null && !response.startsWith("API Error") && !response.startsWith("Failed to connect") && !response.startsWith("Error:")) {
                    contentArea.replaceText(response.trim());
                } else {
                    AlertHelper.showError("AI Error", "Failed to paraphrase content: " + response);
                }
                contentArea.setDisable(false);
                btnParaphrase.setText(originalText);
                btnParaphrase.setDisable(false);
            });
        }).start();
    }

    @FXML
    private void onSave() {
        if (course == null) {
            return;
        }

        if (!validateForm()) {
            return;
        }

        boolean editing = ressource != null;
        Ressource target = editing ? ressource : new Ressource();

        if (selectedFile != null) {
            try {
                Dotenv dotenv = Dotenv.load();
                Cloudinary cloudinary = new Cloudinary(dotenv.get("CLOUDINARY_URL"));
                Map uploadResult = cloudinary.uploader().upload(selectedFile, ObjectUtils.emptyMap());
                String secureUrl = (String) uploadResult.get("secure_url");
                target.setUrl(secureUrl);
            } catch (Exception e) {
                AlertHelper.showError("Upload Error", "Failed to upload file to Cloudinary: " + e.getMessage());
                return;
            }
        } else {
            target.setUrl(urlField.getText().trim());
        }

        target.setNom(nameField.getText().trim());
        target.setType("File");
        target.setContenu(contentArea.getText());
        target.setCourseId(course.getId());

        if (!editing) {
            target.setDateDeCreation(LocalDateTime.now().toString());
        }
        target.setDateDeModification(LocalDateTime.now().toString());

        try {
            if (editing) {
                ressourceService.modifier(target);
            } else {
                ressourceService.ajouter(target);
            }
            openRessourceList();
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to save resource.", exception);
        }
    }

    private void populateForm() {
        nameField.setText(ressource.getNom() != null ? ressource.getNom() : "");
        urlField.setText(ressource.getUrl() != null ? ressource.getUrl() : "");
        contentArea.replaceText(ressource.getContenu() != null ? ressource.getContenu() : "");
    }

    private boolean validateForm() {
        clearValidationErrors();

        boolean valid = true;

        String name = nameField.getText();
        String url = urlField.getText();

        if (name == null || name.isBlank()) {
            nameErrorLabel.setText("Resource name is required.");
            nameErrorLabel.setVisible(true);
            nameErrorLabel.setManaged(true);
            valid = false;
        }

        if (url == null || url.isBlank()) {
            urlErrorLabel.setText("Resource file or URL is required.");
            urlErrorLabel.setVisible(true);
            urlErrorLabel.setManaged(true);
            valid = false;
        }

        if (!valid) {
            AlertHelper.showError("Validation error", "Please correct the highlighted fields before saving the resource.");
        }
        return valid;
    }

    private void clearValidationErrors() {
        nameErrorLabel.setText("");
        nameErrorLabel.setVisible(false);
        nameErrorLabel.setManaged(false);

        urlErrorLabel.setText("");
        urlErrorLabel.setVisible(false);
        urlErrorLabel.setManaged(false);
    }

    private void openRessourceList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/resource-list-view.fxml"));
            Parent root = loader.load();
            RessourceListController controller = loader.getController();
            controller.setCourse(course);
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to return to the resources page.", exception);
        }
    }
    @javafx.fxml.FXML
    public void goToPreview(javafx.event.ActionEvent event) {
        gui.PreviewHelper.goToPreview(event);
    }

    @javafx.fxml.FXML
    public void logout(javafx.event.ActionEvent event) {
        gui.SessionHelper.logout(event);
    }
}
