package services;

import utils.MyConnection;
import java.sql.*;

public class UserCourseProgressService {
    private final Connection con;

    public UserCourseProgressService() {
        con = MyConnection.getInstance().getConnection();
    }

    public double getProgress(int userId, int courseId) throws SQLException {
        String sql = "SELECT progress FROM user_cours_progress WHERE user_id = ? AND cours_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("progress");
                }
            }
        }
        return 0.0;
    }

    public void setProgress(int userId, int courseId, double progress) throws SQLException {
        if (progress > 1.0) progress = 1.0;
        
        String checkSql = "SELECT COUNT(*) FROM user_cours_progress WHERE user_id = ? AND cours_id = ?";
        boolean exists = false;
        try (PreparedStatement ps = con.prepareStatement(checkSql)) {
            ps.setInt(1, userId);
            ps.setInt(2, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    exists = true;
                }
            }
        }

        if (exists) {
            String updateSql = "UPDATE user_cours_progress SET progress = ? WHERE user_id = ? AND cours_id = ?";
            try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                ps.setDouble(1, progress);
                ps.setInt(2, userId);
                ps.setInt(3, courseId);
                ps.executeUpdate();
            }
        } else {
            String insertSql = "INSERT INTO user_cours_progress (user_id, cours_id, progress) VALUES (?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                ps.setInt(1, userId);
                ps.setInt(2, courseId);
                ps.setDouble(3, progress);
                ps.executeUpdate();
            }
        }
    }
}
