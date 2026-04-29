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
    private java.util.Set<String> usersTableColumns;

    public UsersService() {
        con = MyConnection.getInstance().getConnection();
        if (con == null) {
            System.err.println("❌ UsersService: Connection is null! Database connection likely failed.");
        }
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

    private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            if (columnName.equalsIgnoreCase(metaData.getColumnLabel(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean readIsBanned(ResultSet rs) throws SQLException {
        return hasColumn(rs, "is_banned") && rs.getBoolean("is_banned");
    }

    private String readOptionalString(ResultSet rs, String columnName) throws SQLException {
        return hasColumn(rs, columnName) ? rs.getString(columnName) : null;
    }

    private int readOptionalInt(ResultSet rs, String columnName) throws SQLException {
        return hasColumn(rs, columnName) ? rs.getInt(columnName) : 0;
    }

    private java.util.Set<String> getUsersTableColumns() throws SQLException {
        if (usersTableColumns != null) {
            return usersTableColumns;
        }

        usersTableColumns = new java.util.HashSet<>();
        DatabaseMetaData metaData = con.getMetaData();
        try (ResultSet rs = metaData.getColumns(con.getCatalog(), null, "users", null)) {
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                if (columnName != null) {
                    usersTableColumns.add(columnName.toLowerCase());
                }
            }
        }
        return usersTableColumns;
    }

    private boolean usersTableHasColumn(String columnName) throws SQLException {
        return getUsersTableColumns().contains(columnName.toLowerCase());
    }

    private Users mapUser(ResultSet rs) throws SQLException {
        Users user = new Users();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPassword(readOptionalString(rs, "password"));
        user.setRole(readOptionalString(rs, "role"));
        user.setNumtel(readOptionalString(rs, "numtel"));
        user.setPoints(readOptionalInt(rs, "points"));
        user.setCreated_at(readOptionalString(rs, "created_at"));
        user.setBanned(readIsBanned(rs));
        user.setImage(readOptionalString(rs, "image"));
        return user;
    }

    @Override
    public void ajouter(Users users) throws SQLException {
        String passwordToStore = users.getPassword();
        // Only hash if it's not a Google Auth placeholder
        if (!"GOOGLE_AUTH".equals(passwordToStore)) {
            passwordToStore = bcryptHash(passwordToStore);
        }
        List<String> columns = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        columns.add("username");
        values.add(users.getUsername());
        columns.add("email");
        values.add(users.getEmail());
        columns.add("password");
        values.add(passwordToStore);

        if (usersTableHasColumn("role")) {
            columns.add("role");
            values.add(users.getRole());
        }
        if (usersTableHasColumn("numtel")) {
            columns.add("numtel");
            values.add(users.getNumtel());
        }
        if (usersTableHasColumn("points")) {
            columns.add("points");
            values.add(users.getPoints());
        }
        if (usersTableHasColumn("created_at")) {
            columns.add("created_at");
            values.add(users.getCreated_at());
        }
        if (usersTableHasColumn("is_banned")) {
            columns.add("is_banned");
            values.add(users.isBanned());
        }
        if (usersTableHasColumn("image")) {
            columns.add("image");
            values.add(users.getImage());
        }

        String placeholders = String.join(", ", java.util.Collections.nCopies(columns.size(), "?"));
        String sql = "INSERT INTO users (" + String.join(", ", columns) + ") VALUES (" + placeholders + ")";
        PreparedStatement ps = con.prepareStatement(sql);
        for (int i = 0; i < values.size(); i++) {
            ps.setObject(i + 1, values.get(i));
        }
        ps.executeUpdate();

    }

    @Override
    public void supprimer(int id) throws SQLException {
        // First, delete notifications associated with this user to avoid FK constraint error
        String deleteNotificationsSql = "DELETE FROM `notification` WHERE `user_id_id`=?";
        try (PreparedStatement psNotify = con.prepareStatement(deleteNotificationsSql)) {
            psNotify.setInt(1, id);
            psNotify.executeUpdate();
        }

        // Now delete the user
        String sql = "DELETE FROM `users` WHERE `id`=?";
        PreparedStatement preparedStatement = con.prepareStatement(sql);
        preparedStatement.setInt(1, id);
        preparedStatement.executeUpdate();
        System.out.println("User and associated notifications deleted");
    }


    @Override
    public List<Users> afficher() throws SQLException {
        List<Users> usersList = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role != 'ROLE_ADMIN'";
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next()) {
            usersList.add(mapUser(rs));
        }
        return usersList;


    }

    @Override
    public void modifier(Users users) throws SQLException {
        String pwd = users.getPassword();
        String storedPassword = pwd;
        // Only hash if it's not empty, not already hashed ($2), and not the Google Auth placeholder
        if (pwd != null && !pwd.isBlank() && !pwd.startsWith("$2") && !"GOOGLE_AUTH".equals(pwd)) {
            storedPassword = bcryptHash(pwd);
        }


        List<String> assignments = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        assignments.add("username=?");
        values.add(users.getUsername());
        assignments.add("email=?");
        values.add(users.getEmail());

        if (usersTableHasColumn("password")) {
            assignments.add("password=?");
            values.add(storedPassword);
        }
        if (usersTableHasColumn("role")) {
            assignments.add("role=?");
            values.add(users.getRole());
        }
        if (usersTableHasColumn("numtel")) {
            assignments.add("numtel=?");
            values.add(users.getNumtel());
        }
        if (usersTableHasColumn("points")) {
            assignments.add("points=?");
            values.add(users.getPoints());
        }
        if (usersTableHasColumn("is_banned")) {
            assignments.add("is_banned=?");
            values.add(users.isBanned());
        }
        if (usersTableHasColumn("image")) {
            assignments.add("image=?");
            values.add(users.getImage());
        }

        String sql = "UPDATE users SET " + String.join(", ", assignments) + " WHERE id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        for (int i = 0; i < values.size(); i++) {
            ps.setObject(i + 1, values.get(i));
        }
        ps.setInt(values.size() + 1, users.getId());
        ps.executeUpdate();
    }

    public void modifierBan(int id, boolean status) throws SQLException {
        if (!usersTableHasColumn("is_banned")) {
            return;
        }
        String sql = "UPDATE users SET is_banned=? WHERE id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setBoolean(1, status);
        ps.setInt(2, id);
        ps.executeUpdate();
    }


    /** Returns the Users entity matching the given email, or null if not found. */
    public Users findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ? LIMIT 1";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return mapUser(rs);
        }
        return null;
    }

    public Users getUserById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ? LIMIT 1";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return mapUser(rs);
        }
        return null;
    }

    public List<Users> findByRoles(List<String> roles) throws SQLException {
        List<Users> usersList = new ArrayList<>();
        if (roles == null || roles.isEmpty()) return usersList;

        String placeholders = String.join(", ", java.util.Collections.nCopies(roles.size(), "?"));
        String sql = "SELECT * FROM users WHERE role IN (" + placeholders + ")";
        PreparedStatement ps = con.prepareStatement(sql);
        for (int i = 0; i < roles.size(); i++) {
            ps.setString(i + 1, roles.get(i));
        }

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            usersList.add(mapUser(rs));
        }
        return usersList;
    }
}
