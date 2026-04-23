package services;

import entities.Idea;
import utils.MyConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IdeaService {
    private Connection con;

    public IdeaService() {
        con = MyConnection.getInstance().getConnection();
    }

    public void ajouter(Idea idea) throws SQLException {
        String sql = "INSERT INTO idea (title, description, category, creator_id, created_at) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, idea.getTitle());
        ps.setString(2, idea.getDescription());
        ps.setString(3, idea.getCategory());
        ps.setInt(4, 1); // Always 1
        
        // Set created_at to current datetime
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ps.setString(5, java.time.LocalDateTime.now().format(formatter));
        
        ps.executeUpdate();
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM idea WHERE id = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    public List<Idea> afficher() throws SQLException {
        List<Idea> list = new ArrayList<>();
        String sql = "SELECT * FROM idea";
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next()) {
            Idea idea = new Idea();
            idea.setId(rs.getInt("id"));
            idea.setTitle(rs.getString("title"));
            idea.setDescription(rs.getString("description"));
            idea.setCategory(rs.getString("category"));
            idea.setCreated_at(rs.getString("created_at"));
            idea.setLast_used(rs.getString("last_used"));
            idea.setCreator_id(rs.getInt("creator_id"));
            list.add(idea);
        }
        return list;
    }
    public Idea getIdeaById(int id) {
        try {
            String sql = "SELECT * FROM idea WHERE id = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Idea idea = new Idea();
                idea.setId(rs.getInt("id"));
                idea.setTitle(rs.getString("title"));
                idea.setDescription(rs.getString("description"));
                idea.setCategory(rs.getString("category"));
                idea.setCreated_at(rs.getString("created_at"));
                idea.setLast_used(rs.getString("last_used"));
                idea.setCreator_id(rs.getInt("creator_id"));
                return idea;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}
