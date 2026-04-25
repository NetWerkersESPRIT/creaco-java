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
        ps.setInt(6, 1);        // issued_by_id is always 1
        ps.setInt(7, 1);        // assumed_by_id is always 1
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
        List<Tasks> tasksList = new ArrayList<>();
        String sql = "SELECT * FROM task";
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery(sql);
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
