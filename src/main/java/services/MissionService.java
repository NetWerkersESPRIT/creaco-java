package services;

import entities.Mission;
import database.MyConnection;
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
        ps.setString(6, mission.getMission_date());
        ps.setInt(7, mission.getImplement_idea_id());
        ps.setInt(8, 1);        // assigned_by_id is always 1
        
        ps.executeUpdate();
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM mission WHERE id = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    public List<Mission> afficher() throws SQLException {
        List<Mission> list = new ArrayList<>();
        String sql = "SELECT * FROM mission";
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next()) {
            Mission m = new Mission();
            m.setId(rs.getInt("id"));
            m.setTitle(rs.getString("title"));
            m.setDescription(rs.getString("description"));
            m.setState(rs.getString("state"));
            m.setCreated_at(rs.getString("created_at"));
            m.setLast_update(rs.getString("last_update"));
            m.setMission_date(rs.getString("mission_date"));
            m.setCompleted_at(rs.getString("completed_at"));
            m.setImplement_idea_id(rs.getInt("implement_idea_id"));
            m.setAssigned_by_id(rs.getInt("assigned_by_id"));
            list.add(m);
        }
        return list;
    }
    public void modifier(Mission m) throws SQLException {
        String sql = "UPDATE mission SET title=?, description=?, state=?, last_update=? WHERE id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String now = LocalDateTime.now().format(formatter);

        ps.setString(1, m.getTitle());
        ps.setString(2, m.getDescription());
        ps.setString(3, m.getState());
        ps.setString(4, now);
        ps.setInt(5, m.getId());
        
        ps.executeUpdate();
    }
}
