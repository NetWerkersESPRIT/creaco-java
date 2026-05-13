package services;

import entities.Tasks;
import utils.MyConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TskService {
    private Connection con;

    public TskService() {
        con = MyConnection.getInstance().getConnection();
    }

    public void ajouter(Tasks task) throws SQLException {
        String sql = "INSERT INTO task (title, description, state, created_at, time_limit, issued_by_id, assumed_by_id, belong_to_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(sql);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String now = LocalDateTime.now().format(formatter);

        ps.setString(1, task.getTitle());
        ps.setString(2, task.getDescription());
        ps.setString(3, task.getState() != null ? task.getState() : "to do"); // Use task state, fallback to 'to do'
        ps.setString(4, now);   // current timestamp
        ps.setString(5, task.getTime_limit());
        ps.setInt(6, task.getIssued_by_id());
        ps.setInt(7, task.getAssumed_by_id());
        ps.setInt(8, task.getBelong_to_id());
        
        ps.executeUpdate();
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM task WHERE id = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    public List<Tasks> afficher() throws SQLException {
        return afficher(0, "ROLE_ADMIN");
    }

    public List<Tasks> afficher(int userId, String role) throws SQLException {
        List<Tasks> tasksList = new ArrayList<>();
        String sql;

        if ("ROLE_ADMIN".equals(role)) {
            sql = "SELECT * FROM task";
        } else {
            // Non-admin: Filter by mission visibility OR user is issuer/assumed OR issuer/assumed shares group
            sql = "SELECT DISTINCT t.* FROM task t " +
                  "LEFT JOIN mission m ON t.belong_to_id = m.id " +
                  "LEFT JOIN users u_miss ON m.assigned_by_id = u_miss.id " +
                  "LEFT JOIN users u_iss ON t.issued_by_id = u_iss.id " +
                  "LEFT JOIN users u_ass ON t.assumed_by_id = u_ass.id " +
                  "LEFT JOIN group_user gu_miss ON u_miss.id = gu_miss.users_id " +
                  "LEFT JOIN group_user gu_iss ON u_iss.id = gu_iss.users_id " +
                  "LEFT JOIN group_user gu_ass ON u_ass.id = gu_ass.users_id " +
                  "WHERE m.assigned_by_id = ? OR t.issued_by_id = ? OR t.assumed_by_id = ? " +
                  "OR gu_miss.group_id IN (SELECT group_id FROM group_user WHERE users_id = ?) " +
                  "OR gu_iss.group_id IN (SELECT group_id FROM group_user WHERE users_id = ?) " +
                  "OR gu_ass.group_id IN (SELECT group_id FROM group_user WHERE users_id = ?)";
        }

        PreparedStatement ps = con.prepareStatement(sql);
        if (!"ROLE_ADMIN".equals(role)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);
            ps.setInt(4, userId);
            ps.setInt(5, userId);
            ps.setInt(6, userId);
        }

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Tasks t = new Tasks();
            t.setId(rs.getInt("id"));
            t.setTitle(rs.getString("title"));
            t.setDescription(rs.getString("description"));
            t.setState(rs.getString("state"));
            t.setCreated_at(rs.getString("created_at"));
            t.setTime_limit(rs.getString("time_limit"));
            t.setCompleted_at(rs.getString("completed_at"));
            t.setIssued_by_id(rs.getInt("issued_by_id"));
            t.setAssumed_by_id(rs.getInt("assumed_by_id"));
            t.setBelong_to_id(rs.getInt("belong_to_id"));
            tasksList.add(t);
        }
        return tasksList;
    }

    public void modifier(Tasks task) throws SQLException {
        String sql = "UPDATE task SET title=?, description=?, state=?, time_limit=? WHERE id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, task.getTitle());
        ps.setString(2, task.getDescription());
        ps.setString(3, task.getState());
        ps.setString(4, task.getTime_limit());
        ps.setInt(5, task.getId());
        ps.executeUpdate();
    }
}
