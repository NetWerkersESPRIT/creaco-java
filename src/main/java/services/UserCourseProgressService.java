package services;

import utils.MyConnection;
import java.sql.*;

public class UserCourseProgressService {
    private final Connection con;

    public UserCourseProgressService() {
        con = MyConnection.getInstance().getConnection();
        createTablesIfNotExist();
    }

    private void createTablesIfNotExist() {
        try (Statement st = con.createStatement()) {
            // 1. Create tables if they don't exist
            st.execute("CREATE TABLE IF NOT EXISTS user_cours_progress (user_id INT, cours_id INT, PRIMARY KEY (user_id, cours_id))");
            st.execute("CREATE TABLE IF NOT EXISTS user_resource_completion (user_id INT, ressource_id INT, PRIMARY KEY (user_id, ressource_id))");

            // 2. Ensure columns exist in user_cours_progress
            ensureColumnExists("user_cours_progress", "progress", "DOUBLE DEFAULT 0.0");
            ensureColumnExists("user_cours_progress", "progress_percentage", "DOUBLE DEFAULT 0.0");
            ensureColumnExists("user_cours_progress", "created_at", "TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
            ensureColumnExists("user_cours_progress", "updated_at", "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");

            // 3. Ensure columns exist in user_resource_completion
            ensureColumnExists("user_resource_completion", "cours_id", "INT");
            ensureColumnExists("user_resource_completion", "created_at", "TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
            
            System.out.println("[DEBUG] Database schema verification complete.");
        } catch (SQLException e) {
            System.err.println("[ERROR] Schema verification failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void ensureColumnExists(String tableName, String columnName, String definition) {
        try (ResultSet rs = con.getMetaData().getColumns(null, null, tableName, columnName)) {
            if (!rs.next()) {
                System.out.println("[DEBUG] Adding missing column '" + columnName + "' to table '" + tableName + "'");
                try (Statement st = con.createStatement()) {
                    st.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + definition);
                }
            } else {
                System.out.println("[DEBUG] Column '" + columnName + "' already exists in '" + tableName + "'");
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Could not verify/add column " + columnName + ": " + e.getMessage());
        }
    }

    public void markResourceCompleted(int userId, int resourceId, int courseId) throws SQLException {
        System.out.println("[DEBUG] Marking resource " + resourceId + " as completed for user " + userId + " in course " + courseId);
        
        // 1. Check if already completed
        String checkSql = "SELECT COUNT(*) FROM user_resource_completion WHERE user_id = ? AND ressource_id = ?";
        boolean alreadyDone = false;
        try (PreparedStatement ps = con.prepareStatement(checkSql)) {
            ps.setInt(1, userId);
            ps.setInt(2, resourceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) alreadyDone = true;
            }
        }

        if (!alreadyDone) {
            String insertCompletion = "INSERT INTO user_resource_completion (user_id, ressource_id, cours_id) VALUES (?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(insertCompletion)) {
                ps.setInt(1, userId);
                ps.setInt(2, resourceId);
                ps.setInt(3, courseId);
                ps.executeUpdate();
                System.out.println("[DEBUG] Inserted new completion record.");
            }
        } else {
            System.out.println("[DEBUG] Resource already marked as completed previously.");
        }

        // 2. Count total resources in this course
        int totalResources = 0;
        String countTotalSql = "SELECT COUNT(*) FROM ressource WHERE cours_id = ?";
        try (PreparedStatement ps = con.prepareStatement(countTotalSql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) totalResources = rs.getInt(1);
            }
        }
        System.out.println("[DEBUG] Total resources in course: " + totalResources);

        if (totalResources == 0) return;

        // 3. Count completed resources by this user for this course
        int completedResources = 0;
        String countCompletedSql = "SELECT COUNT(*) FROM user_resource_completion WHERE user_id = ? AND cours_id = ?";
        try (PreparedStatement ps = con.prepareStatement(countCompletedSql)) {
            ps.setInt(1, userId);
            ps.setInt(2, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) completedResources = rs.getInt(1);
            }
        }
        System.out.println("[DEBUG] Completed resources by user: " + completedResources);

        // 4. Update the overall course progress
        double progress = (double) completedResources / totalResources;
        System.out.println("[DEBUG] New calculated progress: " + (progress * 100) + "%");
        setProgress(userId, courseId, progress);
    }

    public double getProgress(int userId, int courseId) throws SQLException {
        String sql = "SELECT progress FROM user_cours_progress WHERE user_id = ? AND cours_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double val = rs.getDouble("progress");
                    System.out.println("[DEBUG] getProgress for user=" + userId + ", course=" + courseId + " -> " + val);
                    return val;
                }
            }
        }
        System.out.println("[DEBUG] getProgress for user=" + userId + ", course=" + courseId + " -> NOT FOUND (0.0)");
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
            String updateSql = "UPDATE user_cours_progress SET progress = ?, progress_percentage = ?, updated_at = NOW() WHERE user_id = ? AND cours_id = ?";
            try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                ps.setDouble(1, progress);
                ps.setDouble(2, progress * 100);
                ps.setInt(3, userId);
                ps.setInt(4, courseId);
                ps.executeUpdate();
            }
        } else {
            String insertSql = "INSERT INTO user_cours_progress (user_id, cours_id, progress, progress_percentage, created_at, updated_at) VALUES (?, ?, ?, ?, NOW(), NOW())";
            try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                ps.setInt(1, userId);
                ps.setInt(2, courseId);
                ps.setDouble(3, progress);
                ps.setDouble(4, progress * 100);
                ps.executeUpdate();
            }
        }
    }
}
