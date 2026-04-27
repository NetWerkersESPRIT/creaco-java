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
import javafx.scene.layout.BorderPane;
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

import io.github.cdimascio.dotenv.Dotenv;
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

public class FrontResourceController {

    private final RessourceService ressourceService = new RessourceService();
    private final services.UserCourseProgressService progressService = new services.UserCourseProgressService();
    private Course currentCourse;
    private List<Ressource> ressources = Collections.emptyList();

    @FXML private Label courseTitleLabel;
    @FXML private TilePane resourcesContainer;

    // Profile Navbar labels
    @FXML private Label lblNavUsername;
    @FXML private Label lblNavUserRole;
    @FXML private javafx.scene.layout.HBox profileBox;
    @FXML private Button logoutBtn;

    // Smart Tutor UI
    @FXML private StackPane tutorModal;
    @FXML private VBox chatBox;
    @FXML private TextField chatInput;
    @FXML private ScrollPane chatScroll;
    @FXML private Button askTutorBtn;
    @FXML private VBox mainContent;

    // Resource Modal UI
    @FXML private StackPane resourceModal;
    @FXML private Label modalResourceTitle;
    @FXML private Label modalResourceDesc;

    @FXML
    public void initialize() {
        // Entrance Animation
        if (mainContent != null) {
            new FadeIn(mainContent).setSpeed(0.8).play();
        }

        // Button Hover Animation
        if (askTutorBtn != null) {
            askTutorBtn.setOnMouseEntered(e -> new Pulse(askTutorBtn).setSpeed(2.0).play());
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
        card.setPrefWidth(300);
        card.setMinWidth(300);

        Label name = new Label(ressource.getNom());
        name.getStyleClass().add("card-title");
        
        // Check completion status
        Users user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            try {
                if (progressService.isResourceCompleted(user.getId(), ressource.getId())) {
                    name.setText("✓ " + ressource.getNom());
                    name.setStyle("-fx-text-fill: #10b981;"); // Green for completed
                }
            } catch (SQLException e) { e.printStackTrace(); }
        }

        Label type = new Label("Type: " + (ressource.getType() == null ? "-" : ressource.getType()));
        type.getStyleClass().add("badge-pink"); 
        type.setMaxWidth(Region.USE_PREF_SIZE);

        Label desc = new Label(ressource.getContenu() == null ? "-" : ressource.getContenu());
        desc.setWrapText(true);
        desc.setPrefHeight(60);
        desc.getStyleClass().add("card-subtitle");

        HBox actions = new HBox(10);
        actions.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Button openBtn = new Button("Open");
        openBtn.getStyleClass().add("btn-primary");
        openBtn.setPrefWidth(120);
        openBtn.setPrefHeight(40);
        openBtn.setOnAction(e -> onOpenResource(ressource));

        Button downloadIconBtn = new Button("📥");
        downloadIconBtn.getStyleClass().add("btn-action-light");
        downloadIconBtn.setStyle("-fx-font-size: 18px; -fx-padding: 5 12; -fx-background-radius: 10;");
        downloadIconBtn.setPrefHeight(40);
        downloadIconBtn.setOnAction(e -> onDownloadResource(ressource));
        
        actions.getChildren().addAll(openBtn, downloadIconBtn);

        card.getChildren().addAll(name, type, desc, actions);
        return card;
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
                // Refresh the list to show the checkmark immediately
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
        if (fileUrl == null || fileUrl.isEmpty()) {
            System.out.println("No URL for resource: " + res.getNom());
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Resource");
        
        // Suggest a filename based on URL or name
        String ext = fileUrl.contains(".") ? fileUrl.substring(fileUrl.lastIndexOf(".")) : ".bin";
        if (ext.length() > 5) ext = ".bin"; // Sanitize
        fileChooser.setInitialFileName(res.getNom().replaceAll("[^a-zA-Z0-9.-]", "_") + ext);
        
        File file = fileChooser.showSaveDialog(resourcesContainer.getScene().getWindow());
        
        if (file != null) {
            updateProgress(res);
            new Thread(() -> {
                try (InputStream in = new URL(fileUrl).openStream()) {
                    Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    Platform.runLater(() -> {
                        // Optional: Show success alert
                        System.out.println("Downloaded: " + file.getAbsolutePath());
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    @FXML private javafx.scene.layout.HBox previewBanner;
    private boolean isPreview = false;

    public void setPreviewMode(boolean isPreview) {
        this.isPreview = isPreview;
        
        // Hide the internal banner if we are already in the main dashboard shell
        // which has its own banner.
        if (previewBanner != null) {
            previewBanner.setVisible(isPreview && !FrontMainController.isPreviewModeActive());
            previewBanner.setManaged(isPreview && !FrontMainController.isPreviewModeActive());
        }
        
        if (profileBox != null) {
            profileBox.setVisible(!isPreview);
            profileBox.setManaged(!isPreview);
        }
        
        if (logoutBtn != null) {
            logoutBtn.setVisible(!isPreview);
            logoutBtn.setManaged(!isPreview);
        }
    }

    @FXML
    private void exitPreview(javafx.event.ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/gui/admin-courses-view.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goBack(javafx.event.ActionEvent event) {
        if (FrontMainController.isPreviewModeActive()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/front-courses-grid-view.fxml"));
                Parent root = loader.load();
                if (root instanceof BorderPane) {
                    FrontMainController.loadContent(((BorderPane) root).getCenter());
                } else {
                    FrontMainController.loadContent(root);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
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
    }

    @javafx.fxml.FXML
    public void logout(javafx.event.ActionEvent event) {
        gui.SessionHelper.logout(event);
    }

    @FXML
    private void openTutorModal() {
        tutorModal.setVisible(true);
        new ZoomIn(tutorModal).setSpeed(1.5).play();
        if (chatBox.getChildren().isEmpty()) {
            addChatBubble("Tutor", "Hi! I'm your AI tutor for " + (currentCourse != null ? currentCourse.getTitre() : "this course") + ". What would you like to know?", false);
        }
    }

    @FXML
    private void closeTutorModal() {
        ZoomOut zoomOut = new ZoomOut(tutorModal);
        zoomOut.setSpeed(1.5);
        zoomOut.setOnFinished(e -> tutorModal.setVisible(false));
        zoomOut.play();
    }

    @FXML
    private void sendChatMessage() {
        String msg = chatInput.getText().trim();
        if (msg.isEmpty()) return;

        addChatBubble("You", msg, true);
        chatInput.clear();

        // Call Gemini API asynchronously
        new Thread(() -> {
            String reply = callGeminiAPI(msg);
            Platform.runLater(() -> addChatBubble("Tutor", reply, false));
        }).start();
    }

    private void addChatBubble(String sender, String text, boolean isUser) {
        VBox bubble = new VBox(5);
        bubble.setMaxWidth(300);
        
        Label senderLabel = new Label(sender);
        senderLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8;");
        
        Label msgLabel = new Label(text);
        msgLabel.setWrapText(true);
        msgLabel.setStyle("-fx-font-size: 13px; -fx-padding: 10; -fx-background-radius: 12; " + 
                         (isUser ? "-fx-background-color: #ec4899; -fx-text-fill: white;" 
                                 : "-fx-background-color: #f1f5f9; -fx-text-fill: #334155;"));
        
        bubble.getChildren().addAll(senderLabel, msgLabel);
        
        javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(bubble);
        row.setAlignment(isUser ? javafx.geometry.Pos.CENTER_RIGHT : javafx.geometry.Pos.CENTER_LEFT);
        
        chatBox.getChildren().add(row);
        
        // Auto scroll to bottom
        Platform.runLater(() -> chatScroll.setVvalue(1.0));
    }

    private String callGeminiAPI(String prompt) {
        try {
            Dotenv dotenv = Dotenv.load();
            String apiKey = dotenv.get("SMART_TUTOR");
            if (apiKey == null || apiKey.isEmpty()) return "Error: API key missing.";
            apiKey = apiKey.trim();
            
            String systemPrompt = "You are a helpful course tutor for " + (currentCourse != null ? currentCourse.getTitre() : "this course") + ". Keep answers concise and professional.";
            
            // Proper JSON escaping for the prompt
            String escapedPrompt = prompt.replace("\\", "\\\\")
                                           .replace("\"", "\\\"")
                                           .replace("\n", "\\n")
                                           .replace("\r", "\\r")
                                           .replace("\t", "\\t");
            
            String jsonPayload = "{" +
                "\"model\": \"llama-3.1-8b-instant\"," +
                "\"messages\": [" +
                    "{\"role\": \"system\", \"content\": \"" + systemPrompt.replace("\"", "\\\"") + "\"}," +
                    "{\"role\": \"user\", \"content\": \"" + escapedPrompt + "\"}" +
                "]" +
            "}";
            
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();
                    
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            String body = response.body();
            if (response.statusCode() != 200) {
                return "Groq Error (" + response.statusCode() + "): " + body;
            }

            // Parse response for OpenAI/Groq format: choices[0].message.content
            // Using a more flexible search for "content" field
            int contentKeyIndex = body.indexOf("\"content\"");
            if (contentKeyIndex != -1) {
                int colonIndex = body.indexOf(":", contentKeyIndex);
                if (colonIndex != -1) {
                    int contentStart = body.indexOf("\"", colonIndex);
                    if (contentStart != -1) {
                        contentStart++; // Move past the opening quote
                        int contentEnd = -1;
                        
                        // Find the end quote, skipping escaped ones (\")
                        for (int i = contentStart; i < body.length(); i++) {
                            if (body.charAt(i) == '\"') {
                                // Check if this quote is escaped
                                int backslashCount = 0;
                                for (int j = i - 1; j >= contentStart && body.charAt(j) == '\\'; j--) {
                                    backslashCount++;
                                }
                                if (backslashCount % 2 == 0) {
                                    contentEnd = i;
                                    break;
                                }
                            }
                        }
                        
                        if (contentEnd != -1) {
                            String result = body.substring(contentStart, contentEnd);
                            // Unescape the result
                            result = result.replace("\\n", "\n")
                                         .replace("\\\"", "\"")
                                         .replace("\\\\", "\\")
                                         .replace("\\t", "\t")
                                         .replace("\\r", "\r");
                            return result;
                        }
                    }
                }
            }
            return "Sorry, I couldn't parse the Groq response. Body: " + body;
            
        } catch (Exception e) {
            e.printStackTrace();
            return "Error calling AI: " + e.getMessage();
        }
    }
}
