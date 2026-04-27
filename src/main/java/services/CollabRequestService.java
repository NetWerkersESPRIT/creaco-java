package services;

import entities.CollabRequest;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class CollabRequestService {
    private Connection con;

    public CollabRequestService() {
        con = MyConnection.getInstance().getConnection();
    }

    public void ajouter(CollabRequest req) throws SQLException {
        String sql = "INSERT INTO `collab_request`(`title`, `description`, `budget`, `start_date`, `end_date`, `status`, `rejection_reason`, `deliverables`, `payment_terms`, `created_at`, `creator_id`, `revisor_id`, `collaborator_id`) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, req.getTitle());
        ps.setString(2, req.getDescription());
        ps.setBigDecimal(3, req.getBudget());
        ps.setDate(4, req.getStartDate() != null ? new java.sql.Date(req.getStartDate().getTime()) : null);
        ps.setDate(5, req.getEndDate() != null ? new java.sql.Date(req.getEndDate().getTime()) : null);
        ps.setString(6, req.getStatus());
        ps.setString(7, req.getRejectionReason());
        ps.setString(8, req.getDeliverables());
        ps.setString(9, req.getPaymentTerms());
        ps.setTimestamp(10, new Timestamp(System.currentTimeMillis()));
        ps.setInt(11, req.getCreatorId());
        ps.setInt(12, req.getRevisorId());
        ps.setInt(13, req.getCollaboratorId());
        ps.executeUpdate();
    }

    public void modifier(int id, CollabRequest req) throws SQLException {
        String sql = "UPDATE `collab_request` SET `title`=?, `description`=?, `budget`=?, `start_date`=?, `end_date`=?, `status`=?, `rejection_reason`=?, `deliverables`=?, `payment_terms`=?, `updated_at`=?, `revisor_id`=?, `collaborator_id`=? WHERE `id`=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, req.getTitle());
        ps.setString(2, req.getDescription());
        ps.setBigDecimal(3, req.getBudget());
        ps.setDate(4, req.getStartDate() != null ? new java.sql.Date(req.getStartDate().getTime()) : null);
        ps.setDate(5, req.getEndDate() != null ? new java.sql.Date(req.getEndDate().getTime()) : null);
        ps.setString(6, req.getStatus());
        ps.setString(7, req.getRejectionReason());
        ps.setString(8, req.getDeliverables());
        ps.setString(9, req.getPaymentTerms());
        ps.setTimestamp(10, new Timestamp(System.currentTimeMillis()));
        ps.setInt(11, req.getRevisorId());
        ps.setInt(12, req.getCollaboratorId());
        ps.setInt(13, id);
        ps.executeUpdate();
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM `collab_request` WHERE `id` = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    public List<CollabRequest> afficher() throws SQLException {
        List<CollabRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM collab_request";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            list.add(new CollabRequest(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getBigDecimal("budget"),
                    rs.getDate("start_date"),
                    rs.getDate("end_date"),
                    rs.getString("status"),
                    rs.getString("rejection_reason"),
                    rs.getString("deliverables"),
                    rs.getString("payment_terms"),
                    rs.getTimestamp("created_at"),
                    rs.getTimestamp("updated_at"),
                    rs.getTimestamp("responded_at"),
                    rs.getInt("creator_id"),
                    rs.getInt("revisor_id"),
                    rs.getInt("collaborator_id")
            ));
        }
        return list;
    }

    public List<CollabRequest> afficherByManager(int managerId) throws SQLException {
        List<CollabRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM collab_request WHERE revisor_id = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, managerId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(new CollabRequest(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getBigDecimal("budget"),
                    rs.getDate("start_date"),
                    rs.getDate("end_date"),
                    rs.getString("status"),
                    rs.getString("rejection_reason"),
                    rs.getString("deliverables"),
                    rs.getString("payment_terms"),
                    rs.getTimestamp("created_at"),
                    rs.getTimestamp("updated_at"),
                    rs.getTimestamp("responded_at"),
                    rs.getInt("creator_id"),
                    rs.getInt("revisor_id"),
                    rs.getInt("collaborator_id")
            ));
        }
        return list;
    }
    public List<CollabRequest> afficherByCollaborator(int collabId) throws SQLException {
        List<CollabRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM collab_request WHERE collaborator_id = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, collabId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(new CollabRequest(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getBigDecimal("budget"),
                    rs.getDate("start_date"),
                    rs.getDate("end_date"),
                    rs.getString("status"),
                    rs.getString("rejection_reason"),
                    rs.getString("deliverables"),
                    rs.getString("payment_terms"),
                    rs.getTimestamp("created_at"),
                    rs.getTimestamp("updated_at"),
                    rs.getTimestamp("responded_at"),
                    rs.getInt("creator_id"),
                    rs.getInt("revisor_id"),
                    rs.getInt("collaborator_id")
            ));
        }
        return list;
    }
}
