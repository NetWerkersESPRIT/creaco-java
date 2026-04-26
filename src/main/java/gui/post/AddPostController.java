package gui.post;

import entities.Post;
import javafx.event.ActionEvent;
import javafx.application.Platform;
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
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.UUID;
import java.util.Scanner;
import java.nio.file.Path;
import java.nio.file.Paths;

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
    }

    @FXML
    private void savePost(ActionEvent event) {
        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();

        if (!validateInputs(title, content)) {
            return;
        }

        // Automatic Text Correction
        title = services.forum.TextCorrectionService.correctText(title);
        content = services.forum.TextCorrectionService.correctText(content);

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
