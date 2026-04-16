package gui.post;

import entities.Post;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import services.forum.PostService;
import main.FxApplication;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;

public class AddPostController {

    @FXML
    private TextField titleField;
    @FXML
    private TextArea contentArea;

    @FXML
    private CheckBox pinnedCheckBox;
    @FXML
    private Label imageLabel;
    @FXML
    private Label pdfLabel;

    private File imageFile;
    private File pdfFile;

    private final PostService postService = new PostService();
    private boolean isAdminMode = false;

    public void setAdminMode(boolean isAdminMode) {
        this.isAdminMode = isAdminMode;
    }

    @FXML
    private void savePost(ActionEvent event) {
        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();

        if (!validateInputs(title, content)) {
            return;
        }

        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        
        if (isAdminMode) {
            post.setStatus("ACCEPTED");
            post.setUserId(5); // Admin user
        } else {
            post.setStatus("PENDING");
            post.setUserId(1); // Default user
        }
        
        post.setPinned(pinnedCheckBox.isSelected());

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
            BackofficeController bc = FxApplication.getBackofficeController();
            if (bc != null) {
                bc.loadPendingPosts();
            }
            if (isAdminMode) {
                FxApplication.refreshAllForumWindows();
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
        if (title.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Title is required.");
            return false;
        }
        if (content.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Content is required.");
            return false;
        }
        
        // Ensure the title contains at least one letter (prevents numbers-only titles)
        // Also allows letters, numbers, spaces, and common punctuation.
        if (!title.matches(".*\\p{L}.*")) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Title must contain at least one letter. It cannot be numbers only.");
            return false;
        }
        
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
