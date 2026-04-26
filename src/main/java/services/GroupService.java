package services;

import entities.Group;
import entities.Users;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GroupService {
    private final Connection con;

    public GroupService() {
        con = MyConnection.getInstance().getConnection();
    }

    // --- Group Management ---

    public void createGroup(Group group) throws SQLException {
        String sql = "INSERT INTO `group` (name, owner_id) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, group.getName());
            ps.setInt(2, group.getCreatorId()); // Entity still uses creatorId internally, mapping to owner_id
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    group.setId(rs.getInt(1));
                }
            }
        }
    }

    public Group getGroupById(int id) throws SQLException {
        String sql = "SELECT * FROM `group` WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Group(rs.getInt("id"), rs.getString("name"), rs.getInt("owner_id"));
                }
            }
        }
        return null;
    }

    public Group getGroupByCreatorId(int ownerId) throws SQLException {
        String sql = "SELECT * FROM `group` WHERE owner_id = ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Group(rs.getInt("id"), rs.getString("name"), rs.getInt("owner_id"));
                }
            }
        }
        return null;
    }

    public List<Group> getGroupsForMember(int userId) throws SQLException {
        List<Group> joinedGroups = new ArrayList<>();
        String sql = "SELECT g.* FROM `group` g " +
                     "JOIN group_user gu ON g.id = gu.group_id " +
                     "WHERE gu.users_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    joinedGroups.add(new Group(rs.getInt("id"), rs.getString("name"), rs.getInt("owner_id")));
                }
            }
        }
        return joinedGroups;
    }

    public Users getGroupOwner(int ownerId) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Users u = new Users();
                    u.setId(rs.getInt("id"));
                    u.setUsername(rs.getString("username"));
                    u.setEmail(rs.getString("email"));
                    u.setRole(rs.getString("role"));
                    u.setNumtel(rs.getString("numtel"));
                    u.setImage(rs.getString("image"));
                    return u;
                }
            }
        }
        return null;
    }

    // --- Member Management ---

    public void addMemberToGroup(int usersId, int groupId) throws SQLException {
        String sql = "INSERT INTO group_user (group_id, users_id) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setInt(2, usersId);
            ps.executeUpdate();
        }
    }

    public void removeMemberFromGroup(int usersId, int groupId) throws SQLException {
        String sql = "DELETE FROM group_user WHERE users_id = ? AND group_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usersId);
            ps.setInt(2, groupId);
            ps.executeUpdate();
        }
    }

    public List<Users> getGroupMembers(int groupId) throws SQLException {
        List<Users> members = new ArrayList<>();
        // Assuming role is stored in the users table as per user's schema description
        String sql = "SELECT u.* FROM users u " +
                     "JOIN group_user gu ON u.id = gu.users_id " +
                     "WHERE gu.group_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Users u = new Users();
                    u.setId(rs.getInt("id"));
                    u.setUsername(rs.getString("username"));
                    u.setEmail(rs.getString("email"));
                    u.setRole(rs.getString("role"));
                    u.setNumtel(rs.getString("numtel"));
                    u.setImage(rs.getString("image"));
                    u.setPoints(rs.getInt("points"));
                    members.add(u);
                }
            }
        }
        return members;
    }

    // Since role is in the users table, we update the user entity directly via UsersService or here
    public void updateMemberRole(int usersId, String newRole) throws SQLException {
        String sql = "UPDATE users SET role = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newRole);
            ps.setInt(2, usersId);
            ps.executeUpdate();
        }
    }
}
