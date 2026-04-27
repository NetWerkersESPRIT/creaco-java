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
import gui.FrontMainController;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class NotificationListController {

    @FXML private VBox notificationContainer;

    private final NotificationService notificationService = new NotificationService();
    private Runnable onCloseCallback;

    private javafx.scene.Parent loadView(String resourcePath) throws java.io.IOException {
        String normalizedPath = resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath;
        java.net.URL resource = getClass().getResource(normalizedPath);
        if (resource == null && !normalizedPath.endsWith(".fxml")) {
            resource = getClass().getResource(normalizedPath + ".fxml");
        }
        if (resource == null) {
            throw new IllegalArgumentException("FXML resource not found: " + resourcePath);
        }
        return new javafx.fxml.FXMLLoader(resource).load();
    }

    private String normalizeNotificationUrl(String url) {
        if (url == null) {
            return null;
        }

        String normalized = url.trim();
        if (normalized.isEmpty()) {
            return normalized;
        }
        String lower = normalized.toLowerCase();

        if ("/profile".equalsIgnoreCase(normalized) || "profile".equalsIgnoreCase(normalized)) {
            return "/Users/Profile.fxml";
        }

        if (lower.startsWith("/messages/conversation/")) {
            return "chat/" + normalized.substring("/messages/conversation/".length());
        }

        if (lower.startsWith("messages/conversation/")) {
            return "chat/" + normalized.substring("messages/conversation/".length());
        }

        if ("/admin".equals(lower) || "admin".equals(lower)) {
            return "/Users/Admin.fxml";
        }

        if (lower.startsWith("/admin/posts/pending") || lower.startsWith("admin/posts/pending")) {
            return "admin-posts-pending";
        }

        return normalized;
    }

    @FXML
    public void initialize() {
        loadNotifications();
    }

    public void refreshNotifications() {
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
        String url = normalizeNotificationUrl(n.getTargetUrl());
        if (url == null || url.isEmpty()) return;
        String route = url.startsWith("/") ? url.substring(1) : url;
        
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
                
            } else if ("admin-posts-pending".equals(route)) {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/post/postModeration.fxml"));
                javafx.scene.Parent root = loader.load();
                contentArea.getChildren().setAll(root);
            } else if (route.startsWith("post/")) {
                // Comment/Reply: Navigate to Post Detail View
                String postPart = route.split("#")[0];
                int postId = Integer.parseInt(postPart.replace("post/", ""));
                String commentIdPart = route.contains("#comment_") ? route.split("#comment_")[1] : null;
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
            } else if (route.startsWith("chat/")) {
                int conversationId = Integer.parseInt(route.replace("chat/", ""));
                FrontMainController.showFloatingChat(conversationId);
            } else {
                // Generic fallback
                javafx.scene.Parent root = loadView(url);
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
