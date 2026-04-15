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
        post.setStatus("PENDING"); // Enforce PENDING status
        post.setUserId(1); 
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

        try {
            postService.ajouter(post);
            
            // --- INSTANT SYNC: Notify Backoffice Window ---
            BackofficeController bc = FxApplication.getBackofficeController();
            if (bc != null) {
                bc.loadPendingPosts();
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
            Parent root = FXMLLoader.load(getClass().getResource("/post/displayPost.fxml"));
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
        if (!title.matches("^[a-zA-Z0-9 ]+$")) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Title can only contain letters and numbers.");
            return false;
        }
        if (title.matches("^[0-9]+$")) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Title cannot contain only numbers.");
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
