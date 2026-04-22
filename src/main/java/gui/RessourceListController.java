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
        row.getStyleClass().add("list-row");

        Label nameLabel = new Label(safeText(ressource.getNom()));
        nameLabel.setMinWidth(310);
        nameLabel.setWrapText(true);
        nameLabel.getStyleClass().add("card-title");

        Label typeLabel = new Label(formatType(ressource.getType()));
        typeLabel.getStyleClass().add("badge-pink");

        Button openLinkButton = new Button("Open");
        openLinkButton.getStyleClass().add("btn-primary");
        openLinkButton.setStyle("-fx-font-size: 13px; -fx-background-radius: 10;"); // Override for row buttons
        openLinkButton.setPrefWidth(90);
        openLinkButton.setPrefHeight(35);
        openLinkButton.setOnAction(event -> openRessource(ressource));

        VBox createdAtBox = new VBox(6);
        createdAtBox.setMinWidth(170);
        Label createdAtLabel = new Label(formatDate(ressource.getDateDeCreation()));
        createdAtLabel.getStyleClass().add("card-title");
        createdAtLabel.setStyle("-fx-font-size: 14px;");
        Label updatedAtLabel = new Label("Updated " + formatDate(ressource.getDateDeModification()));
        updatedAtLabel.getStyleClass().add("card-subtitle");
        createdAtBox.getChildren().addAll(createdAtLabel, updatedAtLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actionsBox = new HBox(10);
        Button editButton = createActionButton("Edit", "btn-action-dark");
        Button deleteButton = createActionButton("Delete", "btn-action-light");

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

    private Button createActionButton(String text, String colorClass) {
        Button button = new Button(text);
        button.getStyleClass().addAll("btn-action", colorClass);
        button.setMinWidth(90);
        return button;
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
