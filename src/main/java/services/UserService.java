package services;

import entities.Users;
import utils.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserService {

    private final Connection con;

    public UserService() {
        con = MyConnection.getInstance().getConnection();
    }

    /**
     * Helper to check if a column exists in the ResultSet.
     */
    private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        java.sql.ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            if (columnName.equalsIgnoreCase(metaData.getColumnLabel(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Fetch a user by their ID from the `users` table.
     * Returns a placeholder Users object if the user is not found or an error occurs.
     */
    public Users getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Users user = new Users();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                
                if (hasColumn(rs, "email")) {
                    user.setEmail(rs.getString("email"));
                }
                
                if (hasColumn(rs, "image")) {
                    user.setImage(rs.getString("image"));
                }
                
                // Try to fetch 'role' or 'roles' depending on schema
                if (hasColumn(rs, "roles")) {
                    user.setRole(rs.getString("roles"));
                } else if (hasColumn(rs, "role")) {
                    user.setRole(rs.getString("role"));
                }
                
                return user;
            }
        } catch (SQLException e) {
            System.err.println("UserService.getUserById Error: " + e.getMessage());
        }
        
        // Return a safe placeholder if user not found or query failed
        System.err.println("[UserService] Returning placeholder for ID: " + id);
        Users placeholder = new Users();
        placeholder.setId(id);
        placeholder.setUsername("User #" + id);
        return placeholder;
    }
}
