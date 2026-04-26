package gui.post;

import entities.Post;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
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
    private Label lblUsername;
    @FXML
    private Label lblUserRole;

    private File imageFile;
    private File pdfFile;

    private final PostService postService = new PostService();
    private Post postToUpdate;
    private boolean isAdminMode = false;

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
        title = services.forum.TextCorrectionService.correctText(title);
        content = services.forum.TextCorrectionService.correctText(content);

        postToUpdate.setTitle(title);
        postToUpdate.setContent(content);
        boolean requestPin = pinToggle.isSelected();
        
        if (!isAdminMode) {
            postToUpdate.setStatus("PENDING");
            postToUpdate.setPinned(false);
        } else {
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
