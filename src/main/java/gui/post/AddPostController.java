package gui.post;

import entities.Post;
import javafx.event.ActionEvent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import services.forum.PostService;
import utils.SessionManager;
import entities.Users;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.Scanner;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import services.forum.UserPostValidator;
import entities.forum.SentimentResult;
import gui.forum.CatMatchGame;

public class AddPostController {

    @FXML
    private TextField titleField;
    @FXML
    private TextArea contentArea;

    @FXML
    private ToggleButton pinToggle;
    @FXML
    private Label imageLabel;
    @FXML
    private Label pdfLabel;
    @FXML
    private VBox sentimentWarningContainer;
    @FXML
    private Label titleErrorLabel;
    @FXML
    private Label contentErrorLabel;
    
    @FXML
    private Label lblUsername;
    @FXML
    private Label lblUserRole;

    private File imageFile;
    private File pdfFile;

    private final PostService postService = new PostService();
    private boolean isAdminMode = false;
    private final UserPostValidator postValidator = new UserPostValidator();
    private final PauseTransition debounce = new PauseTransition(javafx.util.Duration.millis(800));
    private boolean isPostCalm = false;

    public void setAdminMode(boolean isAdminMode) {
        // Admin mode is now session-based
        // this.isAdminMode = isAdminMode;
    }

    @FXML
    private void initialize() {
        gui.FrontMainController.setNavbarText("Create New Discussion", "Pages / Forum / Post");
        this.isAdminMode = utils.SessionManager.getInstance().isAdmin();
        Users user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            String displayName = user.getUsername() != null ? user.getUsername() : "User";
            if (lblUsername != null) lblUsername.setText(displayName);
            
            String role = user.getRole() != null ? user.getRole().replace("ROLE_", "") : "USER";
            if (lblUserRole != null) lblUserRole.setText(role);
        }

        setupSentimentAnalysis();
    }

    private void setupSentimentAnalysis() {
        contentArea.textProperty().addListener((obs, oldVal, newVal) -> {
            debounce.setOnFinished(e -> analyzeSentiment(newVal));
            debounce.playFromStart();
        });
    }

    private void analyzeSentiment(String text) {
        if (text == null || text.trim().length() < 10) {
            hideSentimentWarning();
            return;
        }

        Task<List<SentimentResult>> task = new Task<>() {
            @Override
            protected List<SentimentResult> call() {
                return postValidator.validate(text);
            }
        };

        task.setOnSucceeded(e -> {
            List<SentimentResult> results = task.getValue();
            if (!results.isEmpty()) {
                SentimentResult res = results.get(0); 
                if ("NEGATIVE".equalsIgnoreCase(res.getLabel()) && res.getScore() >= 0.5) {
                    if (isPostCalm) {
                        showCalmState(res);
                    } else {
                        showSentimentWarning(res);
                    }
                } else {
                    hideSentimentWarning();
                    isPostCalm = false; 
                }
            }
        });

        new Thread(task).start();
    }

    private void showSentimentWarning(SentimentResult res) {
        sentimentWarningContainer.getChildren().clear();
        sentimentWarningContainer.setVisible(true);
        sentimentWarningContainer.setManaged(true);
        sentimentWarningContainer.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 25; -fx-background-radius: 25; -fx-padding: 30; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");

        VBox content = new VBox(20);
        
        HBox header = new HBox(15);
        header.setAlignment(Pos.TOP_LEFT);
        
        // Wind/Waves icon using symbol
        Label icon = new Label("≋"); 
        icon.setStyle("-fx-font-size: 28px; -fx-text-fill: #475569; -fx-font-weight: bold;");
        
        VBox titles = new VBox(8);
        Label title = new Label("It seems you're feeling a bit frustrated...");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #334155;");
        Label sub = new Label("Your words carry some heat. Take a moment to breathe! This advice is not a substitute for professional help.");
        sub.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px;");
        sub.setWrapText(true);
        titles.getChildren().addAll(title, sub);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label closeBtn = new Label("✕");
        closeBtn.setStyle("-fx-cursor: hand; -fx-text-fill: #94a3b8; -fx-font-size: 18px;");
        closeBtn.setOnMouseClicked(e -> hideSentimentWarning());
        
        header.getChildren().addAll(icon, titles, spacer, closeBtn);

        Label sentimentLabel = new Label("Sentiment: NEGATIVE (" + (int)(res.getScore() * 100) + "%)");
        sentimentLabel.setStyle("-fx-text-fill: #f87171; -fx-font-weight: bold; -fx-font-size: 16px;");

        VBox suggestions = new VBox(12);
        String[] list = {"Try 4 slow deep breaths", "Step away for 1 minute", "Rephrase calmly"};
        for (String s : list) {
            HBox item = new HBox(12);
            item.setAlignment(Pos.CENTER_LEFT);
            Label check = new Label("✓");
            check.setStyle("-fx-text-fill: #64748b; -fx-font-weight: bold; -fx-font-size: 16px;");
            Label text = new Label(s);
            text.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 16px;");
            item.getChildren().addAll(check, text);
            suggestions.getChildren().add(item);
        }

        Button gameBtn = new Button("😻 Click Me for a Cat-tastic Mini-Exercise! 😻");
        gameBtn.setStyle("-fx-background-color: linear-gradient(to right, #818cf8, #c084fc); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15; -fx-padding: 12 25; -fx-cursor: hand; -fx-font-size: 14px;");
        gameBtn.setOnAction(e -> CatMatchGame.launch(sentimentWarningContainer.getScene().getWindow(), () -> {
            isPostCalm = true;
            analyzeSentiment(contentArea.getText());
        }));

        content.getChildren().addAll(header, sentimentLabel, suggestions, gameBtn);
        sentimentWarningContainer.getChildren().add(content);
        
        contentArea.setStyle("-fx-border-color: #f472b6; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 10; -fx-border-width: 2;");
    }

    private void showCalmState(SentimentResult res) {
        sentimentWarningContainer.getChildren().clear();
        sentimentWarningContainer.setVisible(true);
        sentimentWarningContainer.setManaged(true);
        sentimentWarningContainer.setStyle("-fx-background-color: #ecfdf5; -fx-border-color: #d1fae5; -fx-border-radius: 25; -fx-background-radius: 25; -fx-padding: 30;");

        VBox content = new VBox(20);
        
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        
        // Green check icon
        Label icon = new Label("✔"); 
        icon.setStyle("-fx-font-size: 22px; -fx-text-fill: #059669; -fx-font-weight: bold; -fx-background-color: white; -fx-background-radius: 50; -fx-padding: 5;");
        
        VBox titles = new VBox(8);
        Label title = new Label("Ready to rephrase calmly? ✨");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #065f46;");
        Label sub = new Label("You look much calmer now! Why not try to rephrase your thoughts into a more positive post?");
        sub.setStyle("-fx-text-fill: #065f46; -fx-font-size: 14px;");
        sub.setWrapText(true);
        titles.getChildren().addAll(title, sub);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label closeBtn = new Label("✕");
        closeBtn.setStyle("-fx-cursor: hand; -fx-text-fill: #065f46; -fx-font-size: 18px;");
        closeBtn.setOnMouseClicked(e -> hideSentimentWarning());
        
        header.getChildren().addAll(icon, titles, spacer, closeBtn);

        Label sentimentLabel = new Label("Sentiment: NEGATIVE (" + (int)(res.getScore() * 100) + "%)");
        sentimentLabel.setStyle("-fx-text-fill: #f87171; -fx-font-weight: bold; -fx-font-size: 16px;");

        VBox suggestions = new VBox(12);
        String[] list = {"Try 4 slow deep breaths", "Step away for 1 minute", "Rephrase calmly"};
        for (String s : list) {
            HBox item = new HBox(12);
            item.setAlignment(Pos.CENTER_LEFT);
            Label check = new Label("✓");
            check.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 16px;");
            Label text = new Label(s);
            text.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 16px;");
            item.getChildren().addAll(check, text);
            suggestions.getChildren().add(item);
        }

        content.getChildren().addAll(header, sentimentLabel, suggestions);
        sentimentWarningContainer.getChildren().add(content);
        
        contentArea.setStyle("-fx-border-color: #86efac; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 10; -fx-border-width: 2;");
    }

    private void hideSentimentWarning() {
        sentimentWarningContainer.setVisible(false);
        sentimentWarningContainer.setManaged(false);
        contentArea.setStyle("-fx-background-color: white; -fx-border-color: #dbe4f0; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 10;");
    }

    @FXML
    private void savePost(ActionEvent event) {
        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();

        if (!validateInputs(title, content)) {
            return;
        }

        // Automatic Text Correction
        title = utils.TextCorrectionService.correctText(title);
        content = utils.TextCorrectionService.correctText(content);

        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        
        if (isAdminMode) {
            post.setStatus("ACCEPTED");
        } else {
            post.setStatus("PENDING");
        }
        
        boolean requestPin = pinToggle.isSelected();
        post.setPinned(false);
        post.setCreatedAt(java.time.LocalDateTime.now());
        
        Users currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            post.setUserId(currentUser.getId());
        } else {
            post.setUserId(5); // Fallback to Admin ID if session is null for some reason
        }

        // Pin eligibility is not checked during creation anymore as per user request

        // Handle Image
        if (imageFile != null) {
            String newImageName = saveFile(imageFile, "images");
            post.setImageName(newImageName);
        }

        // Handle PDF
        if (pdfFile != null) {
            String newPdfName = saveFile(pdfFile, "pdfs");
            post.setPdfName(newPdfName);
        }
//hadoum ali save  post kan tzadit fil backoffice w  el front office
        try {
            postService.ajouter(post);
            if (requestPin) {
                if (isAdminMode) {
                    postService.acceptPinRequest(post.getId());
                } else {
                    postService.requestPin(post.getUserId(), post.getId());
                }
            }
            
            if (!isAdminMode) {
                if (requestPin) {
                    showAlert(Alert.AlertType.INFORMATION, "Submitted for Review", "Your post and pin request have been submitted and are awaiting admin approval.");
                } else {
                    showAlert(Alert.AlertType.INFORMATION, "Submitted for Review", "Your post has been submitted and is awaiting admin approval.");
                }
            }
            
            goBack(event);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not save the post.");
        }
    }

    @FXML
    private void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(titleField.getScene().getWindow());
        if (selectedFile != null) {
            this.imageFile = selectedFile;
            imageLabel.setText(selectedFile.getName());
        }
    }

    @FXML
    private void choosePDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose PDF");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        File selectedFile = fileChooser.showOpenDialog(titleField.getScene().getWindow());
        if (selectedFile != null) {
            this.pdfFile = selectedFile;
            pdfLabel.setText(selectedFile.getName());
        }
    }

    private String saveFile(File file, String subDir) {
        try {
            String uploadsDir = "src/main/resources/uploads/" + subDir + "/";
            File directory = new File(uploadsDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String fileName = System.currentTimeMillis() + "_" + file.getName();
            Path targetPath = Paths.get(uploadsDir + fileName);
            Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/post/displayPost.fxml"));
            Parent root = loader.load();
            
            DisplayPostController controller = loader.getController();
            controller.setAdminMode(this.isAdminMode);
            
            StackPane contentArea = (StackPane) ((Node) event.getSource()).getScene().lookup("#contentArea");
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean validateInputs(String title, String content) {
        boolean isValid = true;

        // Reset styles
        titleErrorLabel.setVisible(false);
        titleErrorLabel.setManaged(false);
        contentErrorLabel.setVisible(false);
        contentErrorLabel.setManaged(false);
        titleField.setStyle(""); 
        contentArea.setStyle("-fx-background-color: white; -fx-border-color: #dbe4f0; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 10;");

        // Title Validation
        if (title.isEmpty()) {
            showTitleError("Title is required.");
            isValid = false;
        } else if (title.length() < 5) {
            showTitleError("Title must be more than 4 characters.");
            isValid = false;
        } else if (!title.matches(".*\\p{L}.*")) {
            showTitleError("Title must contain at least one letter (it cannot be numbers only).");
            isValid = false;
        }

        // Content Validation
        if (content.isEmpty()) {
            showContentError("Content is required.");
            isValid = false;
        } else if (content.length() < 11) {
            showContentError("Content must be more than 10 characters.");
            isValid = false;
        }
        
        return isValid;
    }

    private void showTitleError(String message) {
        titleErrorLabel.setText(message);
        titleErrorLabel.setVisible(true);
        titleErrorLabel.setManaged(true);
        titleField.setStyle("-fx-border-color: #e53e3e; -fx-border-radius: 12; -fx-background-radius: 12;");
    }

    private void showContentError(String message) {
        contentErrorLabel.setText(message);
        contentErrorLabel.setVisible(true);
        contentErrorLabel.setManaged(true);
        contentArea.setStyle("-fx-background-color: white; -fx-border-color: #e53e3e; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 10;");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        gui.util.AlertHelper.AlertType customType = gui.util.AlertHelper.AlertType.INFORMATION;
        if (type == Alert.AlertType.ERROR) customType = gui.util.AlertHelper.AlertType.ERROR;
        if (type == Alert.AlertType.WARNING) customType = gui.util.AlertHelper.AlertType.WARNING;
        
        gui.util.AlertHelper.showCustomAlert(title, content, customType);
    }
    @FXML
    private void onDictateTitle() {
        startDictation(titleField);
    }

    @FXML
    private void onDictateContent() {
        startDictation(contentArea);
    }

    private void startDictation(Object target) {
        new Thread(() -> {
            try {
                String command = "Add-Type -AssemblyName System.Speech; " +
                               "$sim = New-Object System.Speech.Recognition.SpeechRecognitionEngine; " +
                               "$sim.SetInputToDefaultAudioDevice(); " +
                               "$sim.LoadGrammar((New-Object System.Speech.Recognition.DictationGrammar)); " +
                               "$result = $sim.Recognize(); " +
                               "if ($result -ne $null) { $result.Text }";
                
                ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-Command", command);
                Process p = pb.start();
                
                try (Scanner s = new Scanner(p.getInputStream()).useDelimiter("\\A")) {
                    String result = s.hasNext() ? s.next().trim() : "";
                    
                    if (!result.isEmpty()) {
                        Platform.runLater(() -> {
                            if (target instanceof TextField) {
                                ((TextField) target).setText(result);
                            } else if (target instanceof TextArea) {
                                ((TextArea) target).setText(result);
                            }
                        });
                    }
                }
            } catch (Exception e) {
                System.err.println("Speech recognition error: " + e.getMessage());
            }
        }).start();
    }

    @FXML
    public void onOpenProfile(javafx.scene.input.MouseEvent event) {
        try {
            StackPane area = findContentArea((Node) event.getSource());
            if (area != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Users/Profile.fxml"));
                area.getChildren().setAll((Node) loader.load());
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private StackPane findContentArea(Node source) {
        StackPane area = (StackPane) source.getScene().lookup("#contentArea");
        if (area != null) return area;
        Node parent = source.getParent();
        while (parent != null) {
            if (parent instanceof StackPane && "contentArea".equals(parent.getId())) return (StackPane) parent;
            parent = parent.getParent();
        }
        return null;
    }

    @FXML
    public void logout(javafx.event.ActionEvent event) {
        gui.SessionHelper.logout(event);
    }
}
