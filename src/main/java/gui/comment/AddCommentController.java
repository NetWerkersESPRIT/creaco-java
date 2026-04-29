package gui.comment;

import entities.Comment;
import entities.Post;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import services.forum.CommentService;
import utils.TextCorrectionService;
import utils.DetectBadWordService;
import javafx.concurrent.Task;
import javafx.application.Platform;

import java.io.IOException;
import java.sql.SQLException;

public class AddCommentController {

    @FXML
    private TextArea bodyArea;
    @FXML
    private Label postTitleLabel;

    private final CommentService commentService = new CommentService();
    private Post currentPost;
    private Integer parentCommentId; // ID of the comment being replied to
    private boolean isAdminMode = false;

    public void setAdminMode(boolean isAdminMode) {
        this.isAdminMode = isAdminMode;
    }

    public void setPost(Post post) {
        setPost(post, null);
    }

    public void setPost(Post post, Integer parentCommentId) {
        this.currentPost = post;
        this.parentCommentId = parentCommentId;
        if (post != null) {
            if (parentCommentId != null) {
                postTitleLabel.setText("Replying to a comment on: " + post.getTitle());
            } else {
                postTitleLabel.setText("Adding a comment to: " + post.getTitle());
            }
        }
    }

    @FXML
    private void saveComment(ActionEvent event) {
        if (currentPost == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No post selected!");
            return;
        }

        String body = bodyArea.getText().trim();

        if (body.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Comment cannot be empty.");
            return;
        }

        if (body.length() < 2) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Comment must be at least 2 characters long.");
            return;
        }

        Task<Comment> task = new Task<>() {
            @Override
            protected Comment call() throws Exception {
                // 1. Correct spelling
                String corrected = TextCorrectionService.correctText(body);
                
                // 2. Moderate
                DetectBadWordService.ModerationResult mod = DetectBadWordService.moderate(corrected).join();
                
                Comment c = new Comment();
                c.setBody(mod.moderatedText);
                c.setProfane(mod.isProfane);
                c.setProfaneWords(mod.profaneWordsCount);
                c.setGrammarErrors(mod.grammarErrorsCount);
                
                if (mod.isProfane) {
                    c.setStatus("FLAGGED");
                } else {
                    c.setStatus("APPROVED");
                }
                
                c.setPostId(currentPost.getId());
                
                entities.Users user = utils.SessionManager.getInstance().getCurrentUser();
                if (user != null) {
                    c.setUserId(user.getId());
                } else {
                    c.setUserId(isAdminMode ? 5 : 1);
                }
                
                c.setParentCommentId(parentCommentId);
                
                commentService.ajouter(c);
                return c;
            }
        };

        task.setOnSucceeded(e -> {
            goBack(event);
        });

        task.setOnFailed(e -> {
            task.getException().printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not save the comment.");
        });

        new Thread(task).start();
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/comment/displayComment.fxml"));
            Parent root = loader.load();

            DisplayCommentController controller = loader.getController();
            controller.setAdminMode(this.isAdminMode);
            controller.setPost(currentPost);

            StackPane contentArea = (StackPane) ((Node) event.getSource()).getScene().lookup("#contentArea");
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
