package services;

import entities.Users;
import database.MyConnection;

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
     * Fetch a user by their ID from the `users` table.
     * Returns a placeholder Users object if the user is not found.
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
                user.setEmail(rs.getString("email"));
                try {
                    // Try to fetch 'role' or 'roles' depending on schema
                    user.setRole(rs.getString("roles"));
                } catch (Exception e) {
                    try {
                        user.setRole(rs.getString("role"));
                    } catch (Exception ex) {}
                }
                return user;
            }
        } catch (SQLException e) {
            System.err.println("UserService.getUserById: " + e.getMessage());
        }
        // Return a safe placeholder if user not found
        Users placeholder = new Users();
        placeholder.setId(id);
        placeholder.setUsername("User #" + id);
        return placeholder;
    }
}
