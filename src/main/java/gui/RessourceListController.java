package gui;

import entities.Course;
import entities.Ressource;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.RessourceService;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class RessourceListController {

    private static final DateTimeFormatter DISPLAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final RessourceService ressourceService = new RessourceService();
    private Course course;
    private List<Ressource> ressources = Collections.emptyList();

    @FXML
    private Label titleLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private VBox resourcesContainer;

    public void setCourse(Course course) {
        this.course = course;
        titleLabel.setText("Ressources for " + course.getTitre());
        loadRessources();
    }

    @FXML
    private void onBackToCourses() {
        openScene("/gui/main-view.fxml", null);
    }

    @FXML
    private void onAddRessource() {
        openScene("/gui/resource-form-view.fxml", controller -> {
            RessourceFormController formController = (RessourceFormController) controller;
            formController.setContext(course, null);
        });
    }

    private void loadRessources() {
        if (course == null) {
            return;
        }

        try {
            ressources = ressourceService.afficherParCours(course.getId());
            statusLabel.setText(ressources.size() + " resource(s) linked to this course.");
            renderRessources();
        } catch (SQLException exception) {
            ressources = Collections.emptyList();
            statusLabel.setText("Database error: " + exception.getMessage());
            renderRessources();
        }
    }

    private void renderRessources() {
        resourcesContainer.getChildren().clear();

        if (ressources.isEmpty()) {
            Label emptyState = new Label("No resources found for this course.");
            emptyState.setStyle("-fx-font-size: 15px; -fx-text-fill: #6b7280;");
            resourcesContainer.getChildren().add(emptyState);
            return;
        }

        for (Ressource ressource : ressources) {
            resourcesContainer.getChildren().add(buildRessourceRow(ressource));
        }
    }

    private Node buildRessourceRow(Ressource ressource) {
        HBox row = new HBox(18);
        row.setPadding(new Insets(18, 20, 18, 20));
        row.setStyle("-fx-background-color: white; -fx-background-radius: 18; "
                + "-fx-border-color: #e2e8f0; -fx-border-radius: 18;");

        Label nameLabel = new Label(safeText(ressource.getNom()));
        nameLabel.setMinWidth(310);
        nameLabel.setWrapText(true);
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #243b63;");

        Label typeLabel = new Label(formatType(ressource.getType()));
        typeLabel.setMinWidth(100);
        typeLabel.setStyle("-fx-background-color: #6c83a7; -fx-text-fill: white; "
                + "-fx-padding: 6 12 6 12; -fx-background-radius: 12; -fx-font-weight: bold;");

        Button openLinkButton = new Button("Open");
        openLinkButton.setStyle(commonButtonStyle());
        openLinkButton.setOnAction(event -> openRessource(ressource));

        VBox createdAtBox = new VBox(6);
        createdAtBox.setMinWidth(170);
        Label createdAtLabel = new Label(formatDate(ressource.getDateDeCreation()));
        createdAtLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-font-weight: bold;");
        Label updatedAtLabel = new Label("Updated " + formatDate(ressource.getDateDeModification()));
        updatedAtLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        createdAtBox.getChildren().addAll(createdAtLabel, updatedAtLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actionsBox = new HBox(10);
        Button editButton = createActionButton("Edit", "#1d4ed8", "#dbeafe");
        Button deleteButton = createActionButton("Delete", "#b91c1c", "#fee2e2");

        editButton.setOnAction(event -> openScene("/gui/resource-form-view.fxml", controller -> {
            RessourceFormController formController = (RessourceFormController) controller;
            formController.setContext(course, ressource);
        }));
        deleteButton.setOnAction(event -> {
            if (!AlertHelper.confirmDelete("resource")) {
                return;
            }
            try {
                ressourceService.supprimer(ressource.getId());
                loadRessources();
                statusLabel.setText("Resource deleted successfully.");
                AlertHelper.showInfo("Deleted", "Resource deleted successfully.");
            } catch (SQLException exception) {
                statusLabel.setText("Delete failed: " + exception.getMessage());
                AlertHelper.showError("Delete failed", exception.getMessage());
            }
        });

        actionsBox.getChildren().addAll(editButton, deleteButton);
        row.getChildren().addAll(nameLabel, typeLabel, openLinkButton, createdAtBox, spacer, actionsBox);
        return row;
    }

    private Button createActionButton(String text, String textColor, String backgroundColor) {
        Button button = new Button(text);
        button.setStyle(commonButtonStyle(textColor, backgroundColor));
        button.setMinWidth(90);
        return button;
    }

    private String commonButtonStyle() {
        return "-fx-background-color: #3f5f98; -fx-text-fill: white;"
                + " -fx-background-radius: 22; -fx-padding: 10 20 10 20; -fx-font-size: 14px;"
                + " -fx-font-weight: bold; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 8, 0, 0, 2);";
    }

    private String commonButtonStyle(String textColor, String backgroundColor) {
        return "-fx-background-color: " + backgroundColor + "; -fx-text-fill: " + textColor + ";"
                + " -fx-background-radius: 22; -fx-padding: 10 20 10 20; -fx-font-size: 14px;"
                + " -fx-font-weight: bold; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 8, 0, 0, 2);";
    }

    private void openRessource(Ressource ressource) {
        try {
            if (ressource.getUrl() != null && !ressource.getUrl().isBlank() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI.create(ressource.getUrl()));
                statusLabel.setText("Opened resource: " + ressource.getNom());
                return;
            }
            statusLabel.setText("This resource has no valid URL to open.");
        } catch (Exception exception) {
            statusLabel.setText("Open failed: " + exception.getMessage());
        }
    }

    private void openScene(String fxmlPath, ControllerInitializer initializer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            if (initializer != null) {
                initializer.initialize(loader.getController());
            }
            Stage stage = (Stage) titleLabel.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to open view: " + fxmlPath, exception);
        }
    }

    private String formatType(String value) {
        if (value == null || value.isBlank()) {
            return "N/A";
        }
        return value.toUpperCase();
    }

    private String formatDate(String date) {
        if (date == null || date.isBlank()) {
            return "-";
        }
        try {
            return LocalDateTime.parse(date).format(DISPLAY_DATE_FORMAT);
        } catch (Exception ignored) {
            return date;
        }
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    @FunctionalInterface
    private interface ControllerInitializer {
        void initialize(Object controller);
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
