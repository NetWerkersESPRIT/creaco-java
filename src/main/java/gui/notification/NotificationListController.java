package gui.notification;

import entities.Notification;
import entities.Users;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import services.NotificationService;
import utils.SessionManager;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class NotificationListController {

    @FXML private VBox notificationContainer;

    private final NotificationService notificationService = new NotificationService();
    private Runnable onCloseCallback;

    @FXML
    public void initialize() {
        loadNotifications();
    }

    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }

    private void loadNotifications() {
        Users currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        notificationContainer.getChildren().clear();
        List<Notification> notifications = notificationService.getNotificationsForUser(currentUser.getId());

        if (notifications.isEmpty()) {
            Label emptyLabel = new Label("No notifications yet");
            emptyLabel.setStyle("-fx-text-fill: #94a3b8; -fx-padding: 40;");
            notificationContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Notification n : notifications) {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/notification/notification_item.fxml"));
                javafx.scene.Node node = loader.load();
                NotificationItemController controller = loader.getController();
                controller.setData(n);
                
                node.setOnMouseClicked(e -> {
                    notificationService.markAsRead(n.getId());
                    handleNavigation(n);
                    if (onCloseCallback != null) onCloseCallback.run();
                });
                
                notificationContainer.getChildren().add(node);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleNavigation(Notification n) {
        String url = n.getTargetUrl();
        if (url == null || url.isEmpty()) return;
        
        try {
            javafx.stage.Window popupWindow = notificationContainer.getScene().getWindow();
            javafx.scene.Scene mainScene = null;
            if (popupWindow instanceof javafx.stage.Popup) {
                mainScene = ((javafx.stage.Popup) popupWindow).getOwnerWindow().getScene();
            } else {
                mainScene = notificationContainer.getScene();
            }
            if (mainScene == null) return;
            javafx.scene.layout.StackPane contentArea = (javafx.scene.layout.StackPane) mainScene.lookup("#contentArea");
            if (contentArea == null) return;

            String type = n.getType() != null ? n.getType() : "";
            
            if ("POST_APPROVED".equals(type)) {
                // Navigate to FORUM list and highlight post
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/post/displayPost.fxml"));
                javafx.scene.Parent root = loader.load();
                gui.post.DisplayPostController controller = loader.getController();
                controller.scrollToPost(n.getRelatedId());
                contentArea.getChildren().setAll(root);
                
            } else if ("POST_PENDING".equals(type)) {
                // Navigate to Moderation Panel
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/post/postModeration.fxml"));
                javafx.scene.Parent root = loader.load();
                contentArea.getChildren().setAll(root);
                
            } else if (url.startsWith("post/")) {
                // Comment/Reply: Navigate to Post Detail View
                String postPart = url.split("#")[0];
                int postId = Integer.parseInt(postPart.replace("post/", ""));
                String commentIdPart = url.contains("#comment_") ? url.split("#comment_")[1] : null;
                Integer commentId = (commentIdPart != null) ? Integer.parseInt(commentIdPart) : null;

                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/comment/displayComment.fxml"));
                javafx.scene.Parent root = loader.load();
                gui.comment.DisplayCommentController controller = loader.getController();
                
                services.forum.PostService ps = new services.forum.PostService();
                entities.Post post = ps.getPostById(postId);
                if (post != null) {
                    controller.setPost(post);
                    if (commentId != null) {
                        controller.scrollToComment(commentId);
                    }
                }
                contentArea.getChildren().setAll(root);
            } else {
                // Generic fallback
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(url));
                javafx.scene.Parent root = loader.load();
                contentArea.getChildren().setAll(root);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onMarkAllRead() {
        Users currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;
        
        List<Notification> notifications = notificationService.getNotificationsForUser(currentUser.getId());
        for (Notification n : notifications) {
            if (!n.isRead()) notificationService.markAsRead(n.getId());
        }
        loadNotifications();
    }

    @FXML
    private void onClose() {
        if (onCloseCallback != null) onCloseCallback.run();
    }
}
