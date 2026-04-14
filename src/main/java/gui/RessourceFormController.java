package gui;

import entities.Course;
import entities.Ressource;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import services.RessourceService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;

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
    private TextField typeField;

    @FXML
    private Label typeErrorLabel;

    @FXML
    private TextField urlField;

    @FXML
    private Label urlErrorLabel;

    @FXML
    private TextArea contentArea;

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
    private void onSave() {
        if (course == null) {
            return;
        }

        if (!validateForm()) {
            return;
        }

        boolean editing = ressource != null;
        Ressource target = editing ? ressource : new Ressource();

        target.setNom(nameField.getText().trim());
        target.setType(typeField.getText().trim());
        target.setUrl(urlField.getText().trim());
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
        nameField.setText(ressource.getNom());
        typeField.setText(ressource.getType());
        urlField.setText(ressource.getUrl());
        contentArea.setText(ressource.getContenu());
    }

    private boolean validateForm() {
        clearValidationErrors();

        boolean valid = true;

        String name = nameField.getText();
        String type = typeField.getText();
        String url = urlField.getText();

        if (name == null || name.isBlank()) {
            nameErrorLabel.setText("Resource name is required.");
            nameErrorLabel.setVisible(true);
            nameErrorLabel.setManaged(true);
            valid = false;
        }

        if (type == null || type.isBlank()) {
            typeErrorLabel.setText("Resource type is required.");
            typeErrorLabel.setVisible(true);
            typeErrorLabel.setManaged(true);
            valid = false;
        }

        if (url == null || url.isBlank()) {
            urlErrorLabel.setText("Resource URL is required.");
            urlErrorLabel.setVisible(true);
            urlErrorLabel.setManaged(true);
            valid = false;
        } else if (!url.startsWith("http://") && !url.startsWith("https://")) {
            urlErrorLabel.setText("URL must start with http:// or https://.");
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

        typeErrorLabel.setText("");
        typeErrorLabel.setVisible(false);
        typeErrorLabel.setManaged(false);

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
            stage.setScene(new Scene(root, 1280, 760));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to return to the resources page.", exception);
        }
    }
}
