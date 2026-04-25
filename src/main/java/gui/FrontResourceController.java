package gui;

import entities.Course;
import entities.Ressource;
import entities.Users;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.RessourceService;
import utils.SessionManager;

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

    // Profile Navbar labels
    @FXML private Label lblNavUsername;
    @FXML private Label lblNavUserRole;

    @FXML
    public void initialize() {
        // Initialize User Profile in Navbar
        Users user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            String displayName = user.getUsername() != null ? user.getUsername() : "User";
            if (lblNavUsername != null) lblNavUsername.setText(displayName);
            
            String role = user.getRole() != null ? user.getRole().replace("ROLE_", "") : "USER";
            if (lblNavUserRole != null) {
                lblNavUserRole.setText(role);
                if ("ADMIN".equals(role)) {
                    lblNavUserRole.setStyle("-fx-background-color: #434a75;");
                }
            }
        }
    }

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
        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        card.setPrefWidth(300);
        card.setMinWidth(300);

        Label name = new Label(ressource.getNom());
        name.getStyleClass().add("card-title");

        Label type = new Label("Type: " + (ressource.getType() == null ? "-" : ressource.getType()));
        type.getStyleClass().add("badge-pink"); // Using badge style for the type
        type.setMaxWidth(Region.USE_PREF_SIZE);

        Label desc = new Label(ressource.getContenu() == null ? "-" : ressource.getContenu());
        desc.setWrapText(true);
        desc.setPrefHeight(60);
        desc.getStyleClass().add("card-subtitle");

        Button downloadBtn = new Button("View / Download");
        downloadBtn.getStyleClass().add("btn-primary");
        downloadBtn.setPrefWidth(160);
        downloadBtn.setPrefHeight(40);
        
        downloadBtn.setOnAction(e -> {
            System.out.println("Viewing resource URL: " + ressource.getUrl());
        });

        card.getChildren().addAll(name, type, desc, downloadBtn);
        return card;
    }

    @FXML private javafx.scene.layout.HBox previewBanner;
    private boolean isPreview = false;

    public void setPreviewMode(boolean isPreview) {
        this.isPreview = isPreview;
        if (previewBanner != null) {
            previewBanner.setVisible(isPreview);
            previewBanner.setManaged(isPreview);
        }
    }

    @FXML
    private void exitPreview(javafx.event.ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/gui/main-view.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goBack(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/front-main-view.fxml"));
            Parent root = loader.load();
            
            FrontMainController controller = loader.getController();
            controller.setPreviewMode(this.isPreview);
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @javafx.fxml.FXML
    public void logout(javafx.event.ActionEvent event) {
        gui.SessionHelper.logout(event);
    }
}
