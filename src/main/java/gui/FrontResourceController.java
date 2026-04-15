package gui;

import entities.Course;
import entities.Ressource;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.RessourceService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class FrontResourceController {

    private final RessourceService ressourceService = new RessourceService();
    private Course currentCourse;
    private List<Ressource> ressources = Collections.emptyList();

    @FXML private Label courseTitleLabel;
    @FXML private TilePane resourcesContainer;

    public void setCourse(Course course) {
        this.currentCourse = course;
        if (courseTitleLabel != null) {
            courseTitleLabel.setText("Resources for: " + course.getTitre());
        }
        loadResources();
    }

    private void loadResources() {
        if (currentCourse == null) return;
        try {
            ressources = ressourceService.afficherParCours(currentCourse.getId());
            renderResources();
        } catch (SQLException e) {
            ressources = Collections.emptyList();
            resourcesContainer.getChildren().clear();
            Label error = new Label("Error loading resources: " + e.getMessage());
            error.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
            resourcesContainer.getChildren().add(error);
        }
    }

    private void renderResources() {
        resourcesContainer.getChildren().clear();
        if (ressources.isEmpty()) {
            Label empty = new Label("No resources available for this course.");
            empty.setStyle("-fx-text-fill: #64748b; -fx-font-size: 16px;");
            resourcesContainer.getChildren().add(empty);
            return;
        }

        for (Ressource ressource : ressources) {
            Node card = buildResourceCard(ressource);
            resourcesContainer.getChildren().add(card);
        }
    }

    private Node buildResourceCard(Ressource ressource) {
        VBox card = new VBox(10);
        card.setPrefWidth(280);
        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 16;" +
                "-fx-padding: 16;" +
                "-fx-border-color: #dbe4f0;" +
                "-fx-border-radius: 16;"
        );

        Label name = new Label(ressource.getNom());
        name.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #243b63;");

        Label type = new Label("Type: " + (ressource.getType() == null ? "-" : ressource.getType()));
        type.setStyle("-fx-text-fill: #10b981; -fx-font-size: 13px; -fx-font-weight: bold;");

        Label desc = new Label(ressource.getContenu() == null ? "-" : ressource.getContenu());
        desc.setWrapText(true);
        desc.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px;");

        Button downloadBtn = new Button("View / Download");
        downloadBtn.setStyle(
                "-fx-background-color: #f59e0b;" +
                "-fx-text-fill: #1f365c;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 10;" +
                "-fx-padding: 6 12;"
        );
        
        // Optional action for downloading/viewing the resource url
        downloadBtn.setOnAction(e -> {
            System.out.println("Viewing resource URL: " + ressource.getUrl());
        });

        card.getChildren().addAll(name, type, desc, downloadBtn);
        return card;
    }

    @FXML
    private void goBack(javafx.event.ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/gui/front-main-view.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
