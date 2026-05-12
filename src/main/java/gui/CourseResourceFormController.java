package gui;

import entities.Course;
import entities.Ressource;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.application.Platform;
import services.RessourceService;
import utils.GroqService;
import utils.CloudinaryService;
import java.io.IOException;
import java.io.File;
import java.time.LocalDateTime;

public class CourseResourceFormController {

    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private TextField nameField;
    @FXML private Label nameErrorLabel;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField urlField;
    @FXML private TextArea contentArea;
    @FXML private Button btnParaphrase;
    @FXML private Label uploadStatusLabel;
    @FXML private Label fileUrlLabel;

    private Course currentCourse;
    private Ressource resourceToEdit;
    private boolean isAdminMode = false;
    private final RessourceService ressourceService = new RessourceService();

    @FXML
    public void initialize() {
        typeComboBox.getSelectionModel().selectFirst();
    }

    public void setCourse(Course course) {
        this.currentCourse = course;
        if (subtitleLabel != null && course != null && resourceToEdit == null) {
            subtitleLabel.setText("Adding resource for: " + course.getTitre());
        }
    }

    public void setResourceToEdit(Ressource res) {
        this.resourceToEdit = res;
        if (res != null) {
            if (titleLabel != null) titleLabel.setText("Edit Resource");
            if (subtitleLabel != null) subtitleLabel.setText("Modify the resource details below");
            
            nameField.setText(res.getNom());
            if (res.getType() != null) typeComboBox.setValue(res.getType());
            urlField.setText(res.getUrl() != null ? res.getUrl() : "");
            if (res.getUrl() != null && !res.getUrl().isEmpty()) {
                if (fileUrlLabel != null) fileUrlLabel.setText(res.getUrl());
                if (uploadStatusLabel != null) uploadStatusLabel.setText("File uploaded");
            }
            contentArea.setText(res.getContenu() != null ? res.getContenu() : "");
        }
    }

    public void setAdminMode(boolean adminMode) {
        this.isAdminMode = adminMode;
    }

    @FXML
    private void onSave(ActionEvent event) {
        String name = nameField.getText().trim();
        String type = typeComboBox.getValue();
        String url = urlField.getText().trim();
        String content = contentArea.getText().trim();

        if (name.isEmpty()) {
            nameErrorLabel.setText("Name is required");
            nameErrorLabel.setVisible(true);
            nameErrorLabel.setManaged(true);
            return;
        }

        if (currentCourse == null) {
            System.err.println("Cannot save resource: no course selected.");
            return;
        }

        try {
            if (resourceToEdit == null) {
                Ressource res = new Ressource(
                        name,
                        url,
                        type,
                        content,
                        LocalDateTime.now().toString(),
                        currentCourse.getId()
                );
                ressourceService.ajouter(res);
            } else {
                resourceToEdit.setNom(name);
                resourceToEdit.setType(type);
                resourceToEdit.setUrl(url);
                resourceToEdit.setContenu(content);
                resourceToEdit.setDateDeModification(LocalDateTime.now().toString());
                ressourceService.modifier(resourceToEdit);
            }
            goBack();
        } catch (Exception e) {
            e.printStackTrace();
            nameErrorLabel.setText("Database error: " + e.getMessage());
            nameErrorLabel.setVisible(true);
            nameErrorLabel.setManaged(true);
        }
    }

    @FXML
    private void onCancel(ActionEvent event) {
        goBack();
    }

    @FXML
    private void onParaphrase(ActionEvent event) {
        String text = contentArea.getText().trim();
        if (text.isEmpty()) {
            return;
        }

        btnParaphrase.setDisable(true);
        btnParaphrase.setText("✨ Paraphrasing...");

        new Thread(() -> {
            try {
                String result = GroqService.getParaphrase(text);
                Platform.runLater(() -> {
                    contentArea.setText(result);
                    btnParaphrase.setDisable(false);
                    btnParaphrase.setText("✨ Paraphrase");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    btnParaphrase.setDisable(false);
                    btnParaphrase.setText("✨ Paraphrase");
                    System.err.println("Paraphrase failed: " + e.getMessage());
                });
            }
        }).start();
    }
    @FXML
    private void onUploadFile(javafx.scene.input.MouseEvent event) {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Upload Resource File");
        
        File selectedFile = fileChooser.showOpenDialog(urlField.getScene().getWindow());
        if (selectedFile != null) {
            try {
                if (uploadStatusLabel != null) uploadStatusLabel.setText("Uploading...");
                String uploadedUrl = CloudinaryService.uploadFile(selectedFile);
                urlField.setText(uploadedUrl);
                if (fileUrlLabel != null) fileUrlLabel.setText(uploadedUrl);
                if (uploadStatusLabel != null) uploadStatusLabel.setText("Upload successful!");
                gui.util.AlertHelper.showInfo("Upload Success", "File has been uploaded to Cloudinary.");
            } catch (IOException e) {
                e.printStackTrace();
                if (uploadStatusLabel != null) uploadStatusLabel.setText("Upload failed");
                gui.util.AlertHelper.showError("Upload Error", "Could not upload file to Cloudinary: " + e.getMessage());
            }
        }
    }

    private void goBack() {
        if (FrontMainController.getInstance() != null && currentCourse != null) {
            if (isAdminMode) {
                FrontMainController.getInstance().openAdminCourse(currentCourse);
            } else {
                FrontMainController.getInstance().openCourse(currentCourse);
            }
        }
    }
}
