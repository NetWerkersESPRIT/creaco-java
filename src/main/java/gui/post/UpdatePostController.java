package gui.post;

import entities.Post;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import services.forum.PostService;
import utils.SessionManager;
import entities.Users;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import services.forum.UserPostValidator;
import entities.forum.SentimentResult;
import gui.forum.CatMatchGame;
import utils.SpamDetectionService;
import utils.TextCorrectionService;
import utils.DetectBadWordService;

public class UpdatePostController {

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
    private Label lblUsername;
    @FXML
    private Label lblUserRole;

    private File imageFile;
    private File pdfFile;

    private final PostService postService = new PostService();
    private Post postToUpdate;
    private boolean isAdminMode = false;
    private final UserPostValidator postValidator = new UserPostValidator();
    private final SpamDetectionService spamService = new SpamDetectionService();
    private final PauseTransition debounce = new PauseTransition(javafx.util.Duration.millis(800));
    private boolean isPostCalm = false;

    public void setAdminMode(boolean isAdminMode) {
        // Admin mode is now session-based
        // this.isAdminMode = isAdminMode;
    }

    @FXML
    private void initialize() {
        gui.FrontMainController.setNavbarText("Edit Discussion", "Pages / Forum / Edit");
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

        Task<java.util.List<SentimentResult>> task = new Task<>() {
            @Override
            protected java.util.List<SentimentResult> call() {
                return postValidator.validate(text);
            }
        };

        task.setOnSucceeded(e -> {
            java.util.List<SentimentResult> results = task.getValue();
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
        
        Label icon = new Label("≋");
        icon.setStyle("-fx-font-size: 28px; -fx-text-fill: #475569; -fx-font-weight: bold;");
        
        VBox titles = new VBox(8);
        Label title = new Label("Whoa! It seems you're feeling quite angry...");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #b91c1c;"); // Redder text for angry
        Label sub = new Label("Your words carry a lot of heat! Why not cool down with a quick game before updating? It might help you feel better!");
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

    public void setPost(Post post) {
        this.postToUpdate = post;
        titleField.setText(post.getTitle());
        contentArea.setText(post.getContent());
        pinToggle.setSelected(post.isPinned());
        
        if (post.getImageName() != null && !post.getImageName().isEmpty()) {
            imageLabel.setText(post.getImageName());
        }
        if (post.getPdfName() != null && !post.getPdfName().isEmpty()) {
            pdfLabel.setText(post.getPdfName());
        }
    }

    @FXML
    private void updatePost(ActionEvent event) {
        if (postToUpdate == null) return;

        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();

        if (!validateInputs(title, content)) {
            return;
        }

        // Automatic Text Correction
        String titleCorrected = TextCorrectionService.correctText(title);
        String contentCorrected = TextCorrectionService.correctText(content);

        // Content Moderation
        DetectBadWordService.ModerationResult titleMod = DetectBadWordService.moderate(titleCorrected).join();
        DetectBadWordService.ModerationResult contentMod = DetectBadWordService.moderate(contentCorrected).join();

        // Spam Detection
        int spamScore = spamService.calculateSpamScore(titleMod.moderatedText + " " + contentMod.moderatedText);
        if (spamScore >= 80) {
            showAlert(Alert.AlertType.ERROR, "Spam Detected", "Your changes have been blocked because the content was detected as spam (Score: " + spamScore + "/100).");
            return;
        }

        postToUpdate.setTitle(titleMod.moderatedText);
        postToUpdate.setContent(contentMod.moderatedText);
        postToUpdate.setSpamScore(spamScore);
        postToUpdate.setSpam(spamScore >= 40);
        postToUpdate.setProfane(titleMod.isProfane || contentMod.isProfane);
        postToUpdate.setProfaneWords(titleMod.profaneWordsCount + contentMod.profaneWordsCount);
        postToUpdate.setGrammarErrors(titleMod.grammarErrorsCount + contentMod.grammarErrorsCount);

        boolean requestPin = pinToggle.isSelected();
        
        if (!isAdminMode) {
            if (postToUpdate.isProfane() || postToUpdate.isSpam()) {
                postToUpdate.setStatus("FLAGGED");
            } else {
                postToUpdate.setStatus("PENDING");
            }
            postToUpdate.setPinned(false);
        } else {
            postToUpdate.setStatus("APPROVED");
            postToUpdate.setPinned(requestPin);
        }

        if (imageFile != null) {
            String newImageName = saveFile(imageFile, "images");
            postToUpdate.setImageName(newImageName);
        }

        if (pdfFile != null) {
            String newPdfName = saveFile(pdfFile, "pdfs");
            postToUpdate.setPdfName(newPdfName);
        }

        try {
            postService.modifier(postToUpdate.getId(), postToUpdate);
            
            if (requestPin) {
                if (isAdminMode) {
                    postService.acceptPinRequest(postToUpdate.getId());
                } else {
                    postService.requestPin(postToUpdate.getUserId(), postToUpdate.getId());
                }
            }
            
            if (!isAdminMode) {
                if (requestPin) {
                    showAlert(Alert.AlertType.INFORMATION, "Submitted for Review", "Your edits and pin request have been submitted and are awaiting admin approval.");
                } else {
                    showAlert(Alert.AlertType.INFORMATION, "Submitted for Review", "Your edits have been submitted and are awaiting admin approval.");
                }
            }
            
            goBack(event);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not update the post.");
        }
    }

    @FXML
    private void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
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
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
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
            if (!directory.exists()) directory.mkdirs();

            String fileName = System.currentTimeMillis() + "_" + file.getName();
            Files.copy(file.toPath(), Paths.get(uploadsDir + fileName), StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException e) { return null; }
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
        } catch (IOException e) { e.printStackTrace(); }
    }

    private boolean validateInputs(String title, String content) {
        if (title.isEmpty()) { showAlert(Alert.AlertType.ERROR, "Error", "Title is required."); return false; }
        if (content.isEmpty()) { showAlert(Alert.AlertType.ERROR, "Error", "Content is required."); return false; }
        if (!title.matches(".*\\p{L}.*")) { showAlert(Alert.AlertType.ERROR, "Error", "Title cannot be numbers only."); return false; }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        gui.util.AlertHelper.AlertType ct = (type == Alert.AlertType.ERROR) ? gui.util.AlertHelper.AlertType.ERROR : gui.util.AlertHelper.AlertType.INFORMATION;
        gui.util.AlertHelper.showCustomAlert(title, content, ct);
    }

    @FXML
    private void onDictateTitle() { startDictation(titleField); }
    @FXML
    private void onDictateContent() { startDictation(contentArea); }

    private void startDictation(javafx.scene.control.TextInputControl target) {
        new Thread(() -> {
            try {
                String script = "$code = '[DllImport(\"user32.dll\")] public static extern void keybd_event(byte bVk, byte bScan, uint dwFlags, uint dwExtraInfo);'; "
                        + "$type = Add-Type -MemberDefinition $code -Name 'Win32' -Namespace 'External' -PassThru; "
                        + "$type::keybd_event(0x5B, 0, 0, 0); $type::keybd_event(0x48, 0, 0, 0); "
                        + "$type::keybd_event(0x48, 0, 2, 0); $type::keybd_event(0x5B, 0, 2, 0);";
                new ProcessBuilder("powershell.exe", "-Command", script).start();
            } catch (Exception e) { e.printStackTrace(); }
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
    public void logout(ActionEvent event) { gui.SessionHelper.logout(event); }
}
