package services;

import entities.Collaborator;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CollaboratorService {
    private Connection con;

    public CollaboratorService() {
        con = MyConnection.getInstance().getConnection();
    }

    public void ajouter(Collaborator collaborator) throws SQLException {
        String sql = "INSERT INTO `collaborator`(`name`, `company_name`, `email`, `phone`, `address`, `website`, `domain`, `description`, `logo`, `is_public`, `status`, `created_at`, `added_by_user_id`) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, collaborator.getName());
        ps.setString(2, collaborator.getCompanyName());
        ps.setString(3, collaborator.getEmail());
        ps.setString(4, collaborator.getPhone());
        ps.setString(5, collaborator.getAddress());
        ps.setString(6, collaborator.getWebsite());
        ps.setString(7, collaborator.getDomain());
        ps.setString(8, collaborator.getDescription());
        ps.setString(9, collaborator.getLogo());
        ps.setBoolean(10, collaborator.isPublic());
        ps.setString(11, collaborator.getStatus());
        ps.setTimestamp(12, collaborator.getCreatedAt() != null ? new Timestamp(collaborator.getCreatedAt().getTime()) : new Timestamp(System.currentTimeMillis()));
        ps.setInt(13, collaborator.getAddedByUserId());
        ps.executeUpdate();
    }

    public void modifier(int id, Collaborator collaborator) throws SQLException {
        String sql = "UPDATE `collaborator` SET `name`=?, `company_name`=?, `email`=?, `phone`=?, `address`=?, `website`=?, `domain`=?, `description`=?, `logo`=?, `is_public`=?, `status`=? WHERE `id`=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, collaborator.getName());
        ps.setString(2, collaborator.getCompanyName());
        ps.setString(3, collaborator.getEmail());
        ps.setString(4, collaborator.getPhone());
        ps.setString(5, collaborator.getAddress());
        ps.setString(6, collaborator.getWebsite());
        ps.setString(7, collaborator.getDomain());
        ps.setString(8, collaborator.getDescription());
        ps.setString(9, collaborator.getLogo());
        ps.setBoolean(10, collaborator.isPublic());
        ps.setString(11, collaborator.getStatus());
        ps.setInt(12, id);
        ps.executeUpdate();
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM `collaborator` WHERE `id` = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    public List<Collaborator> afficher() throws SQLException {
        List<Collaborator> collaborators = new ArrayList<>();
        String sql = "SELECT * FROM collaborator";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            Collaborator collab = new Collaborator(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("company_name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("address"),
                    rs.getString("website"),
                    rs.getString("domain"),
                    rs.getString("description"),
                    rs.getString("logo"),
                    rs.getBoolean("is_public"),
                    rs.getString("status"),
                    rs.getTimestamp("created_at"),
                    rs.getInt("added_by_user_id")
            );
            collaborators.add(collab);
        }
        return collaborators;
    }

    public Collaborator getById(int id) throws SQLException {
        String sql = "SELECT * FROM collaborator WHERE id = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return new Collaborator(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("company_name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("address"),
                    rs.getString("website"),
                    rs.getString("domain"),
                    rs.getString("description"),
                    rs.getString("logo"),
                    rs.getBoolean("is_public"),
                    rs.getString("status"),
                    rs.getTimestamp("created_at"),
                    rs.getInt("added_by_user_id")
            );
        }
        return null;
    }
}
