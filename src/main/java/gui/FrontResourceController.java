package gui;

import entities.Course;
import entities.Question;
import entities.Quiz;
import entities.QuizResult;
import entities.Ressource;
import entities.Users;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.QuestionService;
import services.QuizResultService;
import services.QuizService;
import services.RessourceService;
import utils.GroqConfig;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import animatefx.animation.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Priority;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;

public class FrontResourceController {

    private final RessourceService ressourceService = new RessourceService();
    private final QuizService quizService = new QuizService();
    private final QuestionService questionService = new QuestionService();
    private final QuizResultService quizResultService = new QuizResultService();
    private final services.UserCourseProgressService progressService = new services.UserCourseProgressService();
    private Course currentCourse;
    private List<Ressource> ressources = Collections.emptyList();
    private Quiz activeQuiz;
    private Ressource activeResource;

    @FXML private Label courseTitleLabel;
    @FXML private TilePane resourcesContainer;

    // Profile Navbar labels
    @FXML private Label lblNavUsername;
    @FXML private Label lblNavUserRole;
    @FXML private javafx.scene.layout.HBox profileBox;
    @FXML private Button logoutBtn;

    @FXML private VBox mainContent;

    // Resource Modal UI
    @FXML private StackPane resourceModal;
    @FXML private Label modalResourceTitle;
    @FXML private Label modalResourceDesc;


    @FXML
    public void initialize() {
        if (FrontMainController.getInstance() != null) {
            FrontMainController.getInstance().setFloatingButtonVisible(true, this::openTutorModal);
        }

        // Entrance Animation
        if (mainContent != null) {
            new FadeIn(mainContent).setSpeed(0.8).play();
        }

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
        card.setPrefWidth(320);
        card.setMinWidth(320);

        Users currentUser = SessionManager.getInstance().getCurrentUser();
        boolean isPrivileged = SessionManager.getInstance().isAdmin() || SessionManager.getInstance().isContentCreator();

        // Header with Title and Kebab Menu (only for privileged users)
        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label name = new Label(ressource.getNom());
        name.getStyleClass().add("card-title");
        HBox.setHgrow(name, Priority.ALWAYS);
        name.setMaxWidth(Double.MAX_VALUE);

        if (isPrivileged) {
            MenuButton kebab = new MenuButton("⋮");
            kebab.getStyleClass().add("kebab-menu");
            
            MenuItem editItem = new MenuItem("Edit");
            editItem.getStyleClass().add("menu-item");
            editItem.setOnAction(e -> onEditResource(ressource));
            
            MenuItem deleteItem = new MenuItem("Delete");
            deleteItem.getStyleClass().add("menu-item");
            deleteItem.getStyleClass().add("menu-item-delete");
            deleteItem.setOnAction(e -> onDeleteResource(ressource));
            
            kebab.getItems().addAll(editItem, deleteItem);
            header.getChildren().addAll(name, kebab);
        } else {
            header.getChildren().add(name);
        }

        Label type = new Label("Type: " + (ressource.getType() == null ? "-" : ressource.getType()));
        type.getStyleClass().add("badge-pink"); 
        type.setMaxWidth(Region.USE_PREF_SIZE);

        Label desc = new Label(ressource.getContenu() == null ? "-" : ressource.getContenu());
        desc.setWrapText(true);
        desc.setPrefHeight(60);
        desc.getStyleClass().add("card-subtitle");

        // Footer Actions
        VBox footer = new VBox(12);
        footer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        HBox actionRow = new HBox(10);
        actionRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Button openBtn = new Button("Open");
        openBtn.getStyleClass().add("btn-primary");
        openBtn.setPrefWidth(120);
        openBtn.setPrefHeight(36);
        openBtn.setOnAction(e -> onOpenResource(ressource));

        Button downloadIconBtn = new Button("📥");
        downloadIconBtn.getStyleClass().add("btn-action-light");
        downloadIconBtn.setStyle("-fx-font-size: 16px; -fx-padding: 5 10; -fx-background-radius: 8;");
        downloadIconBtn.setPrefHeight(36);
        downloadIconBtn.setOnAction(e -> onDownloadResource(ressource));

        actionRow.getChildren().addAll(openBtn, downloadIconBtn);

        try {
            if (quizService.hasQuizForResource(ressource.getId())) {
                Button assessmentBtn = new Button("Start Assessment");
                assessmentBtn.getStyleClass().add("btn-primary");
                assessmentBtn.setPrefWidth(200);
                assessmentBtn.setPrefHeight(36);
                
                boolean hasCompleted = false;
                if (currentUser != null) {
                    List<Quiz> quizzes = quizService.afficherParRessource(ressource.getId());
                    if (!quizzes.isEmpty()) {
                        hasCompleted = quizResultService.hasUserCompletedQuiz(currentUser.getId(), quizzes.get(0).getId());
                    }
                }

                if (hasCompleted) {
                    assessmentBtn.setText("View Results");
                    assessmentBtn.getStyleClass().setAll("btn-action-light");
                    assessmentBtn.setOnAction(e -> openQuizModal(ressource));
                } else {
                    assessmentBtn.setOnAction(e -> openQuizModal(ressource));
                }
                footer.getChildren().addAll(assessmentBtn, actionRow);
            } else if (isPrivileged) {
                Button setupQuizBtn = new Button("✨ Setup Quiz");
                setupQuizBtn.getStyleClass().add("btn-generate-quiz");
                setupQuizBtn.setPrefWidth(200);
                setupQuizBtn.setPrefHeight(36);
                setupQuizBtn.setOnAction(e -> openAdminQuizManagement(ressource));
                footer.getChildren().addAll(setupQuizBtn, actionRow);
            } else {
                footer.getChildren().add(actionRow);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        card.getChildren().addAll(header, type, desc, footer);
        return card;
    }

    private void openAdminQuizManagement(Ressource res) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/quiz-management-modal.fxml"));
            Parent root = loader.load();
            QuizManagementController controller = loader.getController();
            controller.setResource(res);
            controller.setOnRefreshCard(this::renderResources);
            showAsModal(root, "Quiz Management");
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void openQuizModal(Ressource res) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/quiz-dialog.fxml"));
            Parent root = loader.load();
            QuizDialogController controller = loader.getController();
            
            List<Quiz> quizzes = quizService.afficherParRessource(res.getId());
            if (quizzes.isEmpty()) return;
            Quiz quiz = quizzes.get(0);
            
            Users user = SessionManager.getInstance().getCurrentUser();
            if (user != null && quizResultService.hasUserCompletedQuiz(user.getId(), quiz.getId())) {
                entities.QuizResult result = quizResultService.getUserQuizResult(user.getId(), quiz.getId());
                List<Question> questions = questionService.afficherParQuiz(quiz.getId());
                controller.showAlreadyCompleted(result, questions);
            }
            
            controller.setQuiz(quiz);
            controller.setOnQuizCompleted(this::renderResources);
            showAsModal(root, "Quiz Assessment");
        } catch (IOException | SQLException e) { e.printStackTrace(); }
    }

    private void showAsModal(Parent root, String title) {
        GaussianBlur blur = new GaussianBlur(10);
        Node rootNode = mainContent.getScene().getRoot();
        rootNode.setEffect(blur);

        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        root.setStyle("-fx-background-color: transparent;");
        stage.getScene().setFill(javafx.scene.paint.Color.TRANSPARENT);
        
        stage.setOnHidden(e -> rootNode.setEffect(null));
        stage.show();
    }

    private void onEditResource(Ressource res) {
        if (FrontMainController.getInstance() != null && currentCourse != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/resource-edit-form-view.fxml"));
                Parent root = loader.load();
                
                CourseResourceFormController controller = loader.getController();
                if (controller != null) {
                    controller.setCourse(currentCourse);
                    controller.setResourceToEdit(res);
                }
                
                FrontMainController.getInstance().setContent(root);
                FrontMainController.getInstance().setNavbarText("Edit Resource", "Pages / Courses / Edit Resource");
            } catch (IOException e) {
                e.printStackTrace();
                gui.util.AlertHelper.showError("Error", "Could not load the edit resource form: " + e.getMessage());
            }
        }
    }

    private void onDeleteResource(Ressource res) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Resource");
        alert.setHeaderText("Delete: " + res.getNom());
        alert.setContentText("Are you sure you want to delete this resource? This action cannot be undone.");

        if (alert.showAndWait().orElse(null) == javafx.scene.control.ButtonType.OK) {
            try {
                ressourceService.supprimer(res.getId());
                renderResources();
            } catch (SQLException e) {
                e.printStackTrace();
                gui.util.AlertHelper.showError("Error", "Could not delete resource: " + e.getMessage());
            }
        }
    }

    private void onOpenResource(Ressource res) {
        modalResourceTitle.setText(res.getNom());
        modalResourceDesc.setText(res.getContenu() != null ? res.getContenu() : "No details available.");
        resourceModal.setVisible(true);
        new ZoomIn(resourceModal).setSpeed(1.5).play();
        updateProgress(res);
    }

    private void updateProgress(Ressource res) {
        Users user = SessionManager.getInstance().getCurrentUser();
        if (user != null && currentCourse != null) {
            try {
                progressService.markResourceCompleted(user.getId(), res.getId(), currentCourse.getId());
                renderResources();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void closeResourceModal() {
        ZoomOut zoomOut = new ZoomOut(resourceModal);
        zoomOut.setSpeed(1.5);
        zoomOut.setOnFinished(e -> resourceModal.setVisible(false));
        zoomOut.play();
    }

    private void onDownloadResource(Ressource res) {
        String fileUrl = res.getUrl();
        if (fileUrl == null || fileUrl.isEmpty()) return;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Resource");
        String ext = fileUrl.contains(".") ? fileUrl.substring(fileUrl.lastIndexOf(".")) : ".bin";
        fileChooser.setInitialFileName(res.getNom().replaceAll("[^a-zA-Z0-9.-]", "_") + ext);
        File file = fileChooser.showSaveDialog(resourcesContainer.getScene().getWindow());
        if (file != null) {
            updateProgress(res);
            new Thread(() -> {
                try (InputStream in = new URL(fileUrl).openStream()) {
                    Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) { e.printStackTrace(); }
            }).start();
        }
    }

    @FXML
    private void goBack(javafx.event.ActionEvent event) {
        if (FrontMainController.getInstance() != null) FrontMainController.getInstance().onShowCourses();
    }

    @javafx.fxml.FXML
    public void logout(javafx.event.ActionEvent event) {
        gui.SessionHelper.logout(event);
    }

    @FXML
    private void openTutorModal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/smart-tutor-chat.fxml"));
            Parent root = loader.load();
            SmartTutorChatController controller = loader.getController();
            controller.setCourse(currentCourse);
            
            GaussianBlur blur = new GaussianBlur(10);
            Node rootNode = mainContent.getScene().getRoot();
            rootNode.setEffect(blur);

            Stage stage = new Stage();
            stage.setTitle("Smart Tutor");
            stage.setScene(new Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setOnHidden(e -> rootNode.setEffect(null));
            stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            root.setStyle("-fx-background-color: transparent;");
            Scene scene = stage.getScene();
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            gui.util.AlertHelper.showError("Error", "Could not load the chat dialog: " + e.getMessage());
        }
    }

    @FXML
    private void closeTutorModal() { }
}
