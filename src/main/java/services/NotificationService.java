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

    // ── COLLABORATION MODULE NOTIFICATIONS ──────────────────────────

    public void notifyNewCollabRequest(int requestId) {
        try {
            List<Users> admins = getAllAdmins();
            for (Users admin : admins) {
                createNotification(admin.getId(), "New collaboration request submitted for review.", "COLLAB_REQUEST_NEW", requestId, "admin/requests");
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void notifyManagerOfNewRequest(int managerId, int requestId, String creatorName) {
        createNotification(managerId, "📌 " + creatorName + " has assigned you a new collaboration request for review.", "COLLAB_REQUEST_ASSIGNED", requestId, "manager/review");
    }

    public void notifyCollabRequestStatus(int creatorId, String status, String partnerName) {
        String icon = status.equalsIgnoreCase("APPROVED") ? "✅" : "❌";
        createNotification(creatorId, icon + " Your collaboration request with " + partnerName + " has been " + status.toLowerCase() + ".", "COLLAB_REQUEST_UPDATE", null, "my_requests");
    }

    public void notifyCollabRequestModification(int creatorId, String feedback, String partnerName) {
        createNotification(creatorId, "🛠️ Modification requested for " + partnerName + ": \"" + feedback + "\"", "COLLAB_REQUEST_MODIF", null, "my_requests");
    }

    public void notifyContractSent(int creatorId, String contractNum) {
        createNotification(creatorId, "📩 Contract " + contractNum + " has been sent to the partner for signature.", "CONTRACT_SENT", null, "contracts");
    }

    public void notifyContractSignature(int creatorId, String contractNum, String signerType) {
        String msg = signerType.equalsIgnoreCase("PARTNER") 
            ? "📝 Partner has signed the contract " + contractNum + "."
            : "👤 You have signed the contract " + contractNum + ".";
        createNotification(creatorId, msg, "CONTRACT_SIGNED", null, "contracts");
        
        // Also notify admins of progress
        try {
            List<Users> admins = getAllAdmins();
            for (Users admin : admins) {
                createNotification(admin.getId(), "Contract " + contractNum + " signature progress: " + signerType + " signed.", "CONTRACT_PROGRESS", null, "admin/contracts");
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void notifyContractCompleted(int creatorId, String contractNum) {
        createNotification(creatorId, "🎉 Contract " + contractNum + " is now FULLY SIGNED and active!", "CONTRACT_COMPLETED", null, "contracts");
        try {
            List<Users> admins = getAllAdmins();
            for (Users admin : admins) {
                createNotification(admin.getId(), "Contract " + contractNum + " is now completed.", "CONTRACT_COMPLETED", null, "admin/contracts");
            }
        } catch (SQLException e) { e.printStackTrace(); }
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
