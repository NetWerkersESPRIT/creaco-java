package services;

import entities.Mission;
import utils.MyConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MissionService {
    private Connection con;

    public MissionService() {
        con = MyConnection.getInstance().getConnection();
    }

    public void ajouter(Mission mission) throws SQLException {
        String sql = "INSERT INTO mission (title, description, state, created_at, last_update, mission_date, implement_idea_id, assigned_by_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(sql);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String now = LocalDateTime.now().format(formatter);

        ps.setString(1, mission.getTitle());
        ps.setString(2, mission.getDescription());
        ps.setString(3, "new"); // Always "new" upon creation
        ps.setString(4, now);   // created_at
        ps.setString(5, now);   // last_update initially same as created_at
        ps.setString(6, mission.getMission_datetime());
        ps.setInt(7, mission.getImplement_idea_id());
        ps.setInt(8, mission.getAssigned_by_id());
        
        ps.executeUpdate();
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM mission WHERE id = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    public List<Mission> afficher() throws SQLException {
        return afficher(0, "ROLE_ADMIN"); // Default to admin behavior (all missions)
    }

    public List<Mission> afficher(int userId, String role) throws SQLException {
        List<Mission> list = new ArrayList<>();
        String sql;
        
        if ("ROLE_ADMIN".equals(role)) {
            sql = "SELECT m.*, m.mission_date as mission_datetime, u_creator.username as creator_name FROM mission m " +
                  "LEFT JOIN users u_creator ON m.assigned_by_id = u_creator.id";
        } else {
            // Non-admin: Creator is current user OR creator shares a group with current user OR creator is in a group owned by current user
            sql = "SELECT DISTINCT m.*, m.mission_date as mission_datetime, u_creator.username as creator_name FROM mission m " +
                  "JOIN users u_creator ON m.assigned_by_id = u_creator.id " +
                  "LEFT JOIN group_user gu_creator ON u_creator.id = gu_creator.users_id " +
                  "LEFT JOIN `group` g_own_creator ON u_creator.id = g_own_creator.owner_id " +
                  "WHERE m.assigned_by_id = ? " +
                  "OR gu_creator.group_id IN (SELECT group_id FROM group_user WHERE users_id = ?) " +
                  "OR gu_creator.group_id IN (SELECT id FROM `group` WHERE owner_id = ?) " +
                  "OR g_own_creator.id IN (SELECT group_id FROM group_user WHERE users_id = ?) " +
                  "OR g_own_creator.id IN (SELECT id FROM `group` WHERE owner_id = ?)";
        }

        PreparedStatement ps = con.prepareStatement(sql);
        if (!"ROLE_ADMIN".equals(role)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);
            ps.setInt(4, userId);
            ps.setInt(5, userId);
        }

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Mission m = new Mission();
            m.setId(rs.getInt("id"));
            m.setTitle(rs.getString("title"));
            m.setDescription(rs.getString("description"));
            m.setState(rs.getString("state"));
            m.setCreated_at(rs.getString("created_at"));
            m.setLast_update(rs.getString("last_update"));
            m.setMission_datetime(rs.getString("mission_datetime"));
            m.setCompleted_at(rs.getString("completed_at"));
            m.setImplement_idea_id(rs.getInt("implement_idea_id"));
            m.setAssigned_by_id(rs.getInt("assigned_by_id"));
            m.setCreatorName(rs.getString("creator_name"));
            list.add(m);
        }
        return list;
    }
    public void modifier(Mission m) throws SQLException {
        String sql = "UPDATE mission SET title=?, description=?, state=?, last_update=?, mission_date=? WHERE id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String now = LocalDateTime.now().format(formatter);

        ps.setString(1, m.getTitle());
        ps.setString(2, m.getDescription());
        ps.setString(3, m.getState());
        ps.setString(4, now);
        ps.setString(5, m.getMission_datetime());
        ps.setInt(6, m.getId());
        
        ps.executeUpdate();
    }

    public Mission getMissionById(int id) {
        try {
            String sql = "SELECT m.*, m.mission_date as mission_datetime, u.username as creator_name FROM mission m LEFT JOIN users u ON m.assigned_by_id = u.id WHERE m.id = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Mission m = new Mission();
                m.setId(rs.getInt("id"));
                m.setTitle(rs.getString("title"));
                m.setDescription(rs.getString("description"));
                m.setState(rs.getString("state"));
                m.setCreated_at(rs.getString("created_at"));
                m.setLast_update(rs.getString("last_update"));
                m.setMission_datetime(rs.getString("mission_datetime"));
                m.setCompleted_at(rs.getString("completed_at"));
                m.setImplement_idea_id(rs.getInt("implement_idea_id"));
                m.setAssigned_by_id(rs.getInt("assigned_by_id"));
                m.setCreatorName(rs.getString("creator_name"));
                return m;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}
