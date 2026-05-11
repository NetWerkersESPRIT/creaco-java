package gui;

import entities.Course;
import entities.Ressource;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import services.RessourceService;
import java.time.LocalDateTime;

public class CourseResourceFormController {

    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private TextField nameField;
    @FXML private Label nameErrorLabel;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField urlField;
    @FXML private TextArea contentArea;

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
