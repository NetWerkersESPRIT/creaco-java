package services;

import entities.Notification;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {
    private Connection con;

    public NotificationDAO() {
        con = MyConnection.getInstance().getConnection();
        if (con != null) {
            createTableIfNotExists();
        }
    }

    private void createTableIfNotExists() {
        String createSql = "CREATE TABLE IF NOT EXISTS notification (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "message TEXT NOT NULL, " +
                "is_read BOOLEAN DEFAULT FALSE, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "user_id_id INT NOT NULL, " +
                "related_id INT, " +
                "target_url VARCHAR(255), " +
                "type VARCHAR(50), " +
                "status VARCHAR(20)" +
                ") ENGINE=InnoDB";
        
        boolean needsRecreate = false;
        try (Statement st = con.createStatement()) {
            st.executeQuery("SELECT 1 FROM notification LIMIT 1").close();
        } catch (SQLException e) {
            if (e.getMessage().contains("doesn't exist in engine") || e.getErrorCode() == 1146) {
                needsRecreate = true;
            }
        }

        try (Statement st = con.createStatement()) {
            if (needsRecreate) {
                st.executeUpdate("DROP TABLE IF EXISTS notification");
            }
            st.executeUpdate(createSql);
        } catch (SQLException e) {
            System.err.println("❌ Failed to create/verify notification table: " + e.getMessage());
        }
    }

    public void insertNotification(Notification n) throws SQLException {
        String sql = "INSERT INTO notification (message, is_read, created_at, user_id_id, related_id, target_url, type, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, n.getMessage());
        ps.setBoolean(2, n.isRead());
        ps.setTimestamp(3, Timestamp.valueOf(n.getCreatedAt()));
        ps.setInt(4, n.getUserId());
        if (n.getRelatedId() != null) ps.setInt(5, n.getRelatedId());
        else ps.setNull(5, Types.INTEGER);
        ps.setString(6, n.getTargetUrl());
        ps.setString(7, n.getType());
        ps.setString(8, n.getStatus());
        ps.executeUpdate();
    }

    public List<Notification> getUserNotifications(int userId) throws SQLException {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notification WHERE user_id_id = ? ORDER BY created_at DESC";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Notification n = new Notification();
            n.setId(rs.getInt("id"));
            n.setMessage(rs.getString("message"));
            n.setRead(rs.getBoolean("is_read"));
            n.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            n.setUserId(rs.getInt("user_id_id"));
            n.setRelatedId(rs.getInt("related_id"));
            n.setTargetUrl(rs.getString("target_url"));
            n.setType(rs.getString("type"));
            n.setStatus(rs.getString("status"));
            list.add(n);
        }
        return list;
    }

    public void markAsRead(int notificationId) throws SQLException {
        String sql = "UPDATE notification SET is_read = 1 WHERE id = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, notificationId);
        ps.executeUpdate();
    }

    public void updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE notification SET status = ? WHERE id = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, status);
        ps.setInt(2, id);
        ps.executeUpdate();
    }
}
