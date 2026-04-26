package services;

import entities.Notification;
import entities.Users;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class NotificationService {
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final UsersService usersService = new UsersService();

    public void createNotification(int userId, String message, String type, Integer relatedId, String targetUrl) {
        try {
            Notification n = new Notification();
            n.setUserId(userId);
            n.setMessage(message);
            n.setType(type);
            n.setRelatedId(relatedId);
            n.setTargetUrl(targetUrl);
            n.setRead(false);
            n.setCreatedAt(LocalDateTime.now());
            n.setStatus("ACTIVE");
            notificationDAO.insertNotification(n);
        } catch (SQLException e) {
            System.err.println("Error creating notification: " + e.getMessage());
        }
    }

    public List<Notification> getNotificationsForUser(int userId) {
        try {
            return notificationDAO.getUserNotifications(userId);
        } catch (SQLException e) {
            System.err.println("Error fetching notifications: " + e.getMessage());
            return java.util.Collections.emptyList();
        }
    }

    public void markAsRead(int notificationId) {
        try {
            notificationDAO.markAsRead(notificationId);
        } catch (SQLException e) {
            System.err.println("Error marking notification as read: " + e.getMessage());
        }
    }

    // Trigger Helpers
    public void notifyWelcome(Users user) {
        createNotification(user.getId(), "Welcome to CreaCo, " + user.getUsername() + "! 🎉 Your account has been created successfully.", "WELCOME", null, "/Users/Profile.fxml");
    }

    public void notifyPostPending(int postId) {
        try {
            List<Users> admins = getAllAdmins();
            for (Users admin : admins) {
                createNotification(admin.getId(), "New post pending approval", "POST_PENDING", postId, "post/" + postId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void notifyPostApproved(int userId, int postId) {
        createNotification(userId, "Your post has been approved ✅", "POST_APPROVED", postId, "post/" + postId);
    }

    public void notifyPostRefused(int userId, int postId) {
        createNotification(userId, "Your post has been refused ❌", "POST_REFUSED", postId, "post/" + postId);
    }

    public void notifyComment(int postOwnerId, String commenterName, int postId) {
        createNotification(postOwnerId, commenterName + " commented on your post", "COMMENT", postId, "post/" + postId);
    }

    public void notifyReply(int commentOwnerId, String replierName, int commentId, int postId) {
        createNotification(commentOwnerId, replierName + " replied to your comment", "REPLY", commentId, "post/" + postId + "#comment_" + commentId);
    }

    public void notifyPostLike(int postOwnerId, String likerName, int postId) {
        createNotification(postOwnerId, likerName + " liked your post", "LIKE_POST", postId, "post/" + postId);
    }

    public void notifyCommentLike(int commentOwnerId, String likerName, int commentId, int postId) {
        createNotification(commentOwnerId, likerName + " liked your comment", "LIKE_COMMENT", commentId, "post/" + postId + "#comment_" + commentId);
    }

    private List<Users> getAllAdmins() throws SQLException {
        // We'll need to implement this in UsersService or here directly
        // For now, let's query directly to avoid modifying UsersService if possible, 
        // but it's cleaner in UsersService. Let's try to query here.
        java.sql.Connection con = utils.MyConnection.getInstance().getConnection();
        List<Users> admins = new java.util.ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'ROLE_ADMIN'";
        java.sql.Statement st = con.createStatement();
        java.sql.ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            Users u = new Users();
            u.setId(rs.getInt("id"));
            u.setUsername(rs.getString("username"));
            admins.add(u);
        }
        return admins;
    }
}
