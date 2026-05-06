package services;

import utils.MyConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class AiAssistLogService {
    private Connection con;

    public AiAssistLogService() {
        con = MyConnection.getInstance().getConnection();
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS ai_assist_log (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "context VARCHAR(255), " +
                "original_text TEXT, " +
                "rephrased_text TEXT, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
        try {
            Statement st = con.createStatement();
            st.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("Failed to create ai_assist_log table: " + e.getMessage());
        }
    }

    public void logUsage(String context, String originalText, String rephrasedText) {
        String sql = "INSERT INTO ai_assist_log (context, original_text, rephrased_text) VALUES (?, ?, ?)";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, context);
            ps.setString(2, originalText);
            ps.setString(3, rephrasedText);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to log AI usage: " + e.getMessage());
        }
    }
}
