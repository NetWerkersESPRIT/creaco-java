package services;

import entities.Users;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.crypto.generators.OpenBSDBCrypt;
import java.security.SecureRandom;

public class UsersService implements services.UsersInterface<Users> {
    Connection con;

    public UsersService() {
        con = MyConnection.getInstance().getConnection();
    }

    public static String bcryptHash(String password) {
        // Generate a random 16-byte salt
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);

        // Generate BCrypt hash with $2y$ prefix and cost factor 13
        return OpenBSDBCrypt.generate("2y", password.toCharArray(), salt, 13);
    }

    public static boolean verifyBcrypt(String password, String storedHash) {
        // Constant-time verification
        return OpenBSDBCrypt.checkPassword(storedHash, password.toCharArray());
    }

    @Override
    public void ajouter(Users users) throws SQLException {
        String sql = "INSERT INTO users (username, email, password, role, numtel, points, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(sql);

        ps.setString(1, users.getUsername());
        ps.setString(2, users.getEmail());

        String hashedPassword = bcryptHash(users.getPassword());
        ps.setString(3, hashedPassword);

        ps.setString(4, users.getRole());
        ps.setString(5, users.getNumtel());
        ps.setInt(6, users.getPoints());
        ps.setString(7, users.getCreated_at());

        ps.executeUpdate();

    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM `users` WHERE `id`=?";
        PreparedStatement preparedStatement = con.prepareStatement(sql);
        preparedStatement.setInt(1, id);
        preparedStatement.executeUpdate();
        System.out.println("User Deleted");


    }

    @Override
    public List<Users> afficher() throws SQLException {
        List<Users> usersList = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role != 'ROLE_ADMIN'";
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next()) {
            Users users = new Users();
            users.setId(rs.getInt("id"));
            users.setPoints(rs.getInt("points"));
            users.setUsername(rs.getString("username"));
            users.setEmail(rs.getString("email"));
            users.setPassword(rs.getString("password"));
            users.setRole(rs.getString("role"));
            users.setNumtel(rs.getString("numtel"));
            users.setCreated_at(rs.getString("created_at"));
            usersList.add(users);
        }
        return usersList;


    }

    @Override
    public void modifier(Users users) throws SQLException {
        String sql = "UPDATE users SET username=?, email=?, password=?, role=?, numtel=?, points=? WHERE id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, users.getUsername());
        ps.setString(2, users.getEmail());

        String pwd = users.getPassword();
        if (pwd != null && !pwd.isBlank() && !pwd.startsWith("$2")) {
            // It's a plain text password, hash it
            ps.setString(3, bcryptHash(pwd));
        } else {
            // Either null/blank or already hashed
            ps.setString(3, pwd);
        }

        ps.setString(4, users.getRole());
        ps.setString(5, users.getNumtel());
        ps.setInt(6, users.getPoints());
        ps.setInt(7, users.getId());
        ps.executeUpdate();
    }


    /** Returns the Users entity matching the given email, or null if not found. */
    public Users findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ? LIMIT 1";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            Users u = new Users();
            u.setId(rs.getInt("id"));
            u.setUsername(rs.getString("username"));
            u.setEmail(rs.getString("email"));
            u.setPassword(rs.getString("password"));
            u.setRole(rs.getString("role"));
            u.setNumtel(rs.getString("numtel"));
            u.setPoints(rs.getInt("points"));
            u.setCreated_at(rs.getString("created_at"));
            return u;
        }
        return null;
    }

    public List<Users> getManagers() throws SQLException {
        List<Users> usersList = new ArrayList<>();
        String sql = "SELECT * FROM users";
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next()) {
            Users users = new Users();
            users.setId(rs.getInt("id"));
            users.setPoints(rs.getInt("points"));
            users.setUsername(rs.getString("username"));
            users.setEmail(rs.getString("email"));
            users.setPassword(rs.getString("password"));
            users.setRole(rs.getString("role"));
            users.setNumtel(rs.getString("numtel"));
            users.setCreated_at(rs.getString("created_at"));
            usersList.add(users);
        }
        return usersList;
    }
}
