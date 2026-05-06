package services;

import entities.Contract;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContractService {
    private Connection con;

    public ContractService() {
        con = MyConnection.getInstance().getConnection();
    }
    public void ajouter(Contract c) throws SQLException {
        String sql = "INSERT INTO `contract` (`contract_number`, `title`, `start_date`, `end_date`, `amount`, `pdf_path`, `status`, `signed_by_creator`, `signed_by_collaborator`, `creator_signature_date`, `collaborator_signature_date`, `terms`, `payment_schedule`, `confidentiality_clause`, `cancellation_terms`, `signature_token`, `created_at`, `sent_at`, `collab_request_id`, `creator_id`, `collaborator_id`) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, c.getContractNumber());
        ps.setString(2, c.getTitle());
        ps.setDate(3, c.getStartDate() != null ? new java.sql.Date(c.getStartDate().getTime()) : null);
        ps.setDate(4, c.getEndDate() != null ? new java.sql.Date(c.getEndDate().getTime()) : null);
        ps.setBigDecimal(5, c.getAmount());
        ps.setString(6, c.getPdfPath());
        ps.setString(7, c.getStatus());
        ps.setBoolean(8, c.isSignedByCreator());
        ps.setBoolean(9, c.isSignedByCollaborator());
        ps.setTimestamp(10, c.getCreatorSignatureDate() != null ? new Timestamp(c.getCreatorSignatureDate().getTime()) : null);
        ps.setTimestamp(11, c.getCollaboratorSignatureDate() != null ? new Timestamp(c.getCollaboratorSignatureDate().getTime()) : null);
        ps.setString(12, c.getTerms());
        ps.setString(13, c.getPaymentSchedule());
        ps.setString(14, c.getConfidentialityClause());
        ps.setString(15, c.getCancellationTerms());
        ps.setString(16, c.getSignatureToken());
        ps.setTimestamp(17, new Timestamp(System.currentTimeMillis()));
        ps.setTimestamp(18, null); // sent_at
        ps.setInt(19, c.getCollabRequestId());
        if (c.getCreatorId() != null) {
            ps.setInt(20, c.getCreatorId());
        } else {
            ps.setNull(20, java.sql.Types.INTEGER);
        }
        ps.setInt(21, c.getCollaboratorId());
        ps.executeUpdate();
    }

    public void modifier(int id, Contract c) throws SQLException {
        String sql = "UPDATE `contract` SET `contract_number`=?, `title`=?, `start_date`=?, `end_date`=?, `amount`=?, `pdf_path`=?, `status`=?, `signed_by_creator`=?, `signed_by_collaborator`=?, `creator_signature_date`=?, `collaborator_signature_date`=?, `terms`=?, `payment_schedule`=?, `confidentiality_clause`=?, `cancellation_terms`=?, `signature_token`=?, `collab_request_id`=?, `creator_id`=?, `collaborator_id`=? WHERE `id`=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, c.getContractNumber());
        ps.setString(2, c.getTitle());
        ps.setDate(3, c.getStartDate() != null ? new java.sql.Date(c.getStartDate().getTime()) : null);
        ps.setDate(4, c.getEndDate() != null ? new java.sql.Date(c.getEndDate().getTime()) : null);
        ps.setBigDecimal(5, c.getAmount());
        ps.setString(6, c.getPdfPath());
        ps.setString(7, c.getStatus());
        ps.setBoolean(8, c.isSignedByCreator());
        ps.setBoolean(9, c.isSignedByCollaborator());
        ps.setTimestamp(10, c.getCreatorSignatureDate() != null ? new Timestamp(c.getCreatorSignatureDate().getTime()) : null);
        ps.setTimestamp(11, c.getCollaboratorSignatureDate() != null ? new Timestamp(c.getCollaboratorSignatureDate().getTime()) : null);
        ps.setString(12, c.getTerms());
        ps.setString(13, c.getPaymentSchedule());
        ps.setString(14, c.getConfidentialityClause());
        ps.setString(15, c.getCancellationTerms());
        ps.setString(16, c.getSignatureToken());
        ps.setInt(17, c.getCollabRequestId());
        if (c.getCreatorId() != null) {
            ps.setInt(18, c.getCreatorId());
        } else {
            ps.setNull(18, java.sql.Types.INTEGER);
        }
        ps.setInt(19, c.getCollaboratorId());
        ps.setInt(20, id);
        ps.executeUpdate();
    }

    public List<Contract> afficher() throws SQLException {
        List<Contract> list = new ArrayList<>();
        String sql = "SELECT * FROM contract";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            list.add(new Contract(
                    rs.getInt("id"),
                    rs.getString("contract_number"),
                    rs.getString("title"),
                    rs.getDate("start_date"),
                    rs.getDate("end_date"),
                    rs.getBigDecimal("amount"),
                    rs.getString("pdf_path"),
                    rs.getString("status"),
                    rs.getBoolean("signed_by_creator"),
                    rs.getBoolean("signed_by_collaborator"),
                    rs.getTimestamp("creator_signature_date"),
                    rs.getTimestamp("collaborator_signature_date"),
                    rs.getString("terms"),
                    rs.getString("payment_schedule"),
                    rs.getString("confidentiality_clause"),
                    rs.getString("cancellation_terms"),
                    rs.getString("signature_token"),
                    rs.getTimestamp("created_at"),
                    rs.getTimestamp("sent_at"),
                    rs.getInt("collab_request_id"),
                    rs.getObject("creator_id") != null ? rs.getInt("creator_id") : null,
                    rs.getInt("collaborator_id")
            ));
        }
        return list;
    }

    public List<Contract> afficherByManager(int managerId) throws SQLException {
        List<Contract> list = new ArrayList<>();
        String sql = "SELECT c.* FROM contract c " +
                "JOIN collab_request r ON c.collab_request_id = r.id " +
                "WHERE r.revisor_id = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, managerId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(new Contract(
                    rs.getInt("id"),
                    rs.getString("contract_number"),
                    rs.getString("title"),
                    rs.getDate("start_date"),
                    rs.getDate("end_date"),
                    rs.getBigDecimal("amount"),
                    rs.getString("pdf_path"),
                    rs.getString("status"),
                    rs.getBoolean("signed_by_creator"),
                    rs.getBoolean("signed_by_collaborator"),
                    rs.getTimestamp("creator_signature_date"),
                    rs.getTimestamp("collaborator_signature_date"),
                    rs.getString("terms"),
                    rs.getString("payment_schedule"),
                    rs.getString("confidentiality_clause"),
                    rs.getString("cancellation_terms"),
                    rs.getString("signature_token"),
                    rs.getTimestamp("created_at"),
                    rs.getTimestamp("sent_at"),
                    rs.getInt("collab_request_id"),
                    rs.getObject("creator_id") != null ? rs.getInt("creator_id") : null,
                    rs.getInt("collaborator_id")
            ));
        }
        return list;
    }

    /**
     * Called by the DocuSign poller when the envelope status reaches "completed".
     * Updates the contract's status to SIGNED and records the collaborator signature date.
     *
     * @param contractId the local DB contract ID
     */
    public void markAsSignedByCollaborator(int contractId) throws SQLException {
        String sql = "UPDATE `contract` SET `status`='SIGNED', `signed_by_collaborator`=1, " +
                     "`collaborator_signature_date`=NOW() WHERE `id`=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, contractId);
        ps.executeUpdate();
    }

    public Contract getByRequestId(int requestId) throws SQLException {
        String sql = "SELECT * FROM contract WHERE collab_request_id = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, requestId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return new Contract(
                    rs.getInt("id"),
                    rs.getString("contract_number"),
                    rs.getString("title"),
                    rs.getDate("start_date"),
                    rs.getDate("end_date"),
                    rs.getBigDecimal("amount"),
                    rs.getString("pdf_path"),
                    rs.getString("status"),
                    rs.getBoolean("signed_by_creator"),
                    rs.getBoolean("signed_by_collaborator"),
                    rs.getTimestamp("creator_signature_date"),
                    rs.getTimestamp("collaborator_signature_date"),
                    rs.getString("terms"),
                    rs.getString("payment_schedule"),
                    rs.getString("confidentiality_clause"),
                    rs.getString("cancellation_terms"),
                    rs.getString("signature_token"),
                    rs.getTimestamp("created_at"),
                    rs.getTimestamp("sent_at"),
                    rs.getInt("collab_request_id"),
                    rs.getObject("creator_id") != null ? rs.getInt("creator_id") : null,
                    rs.getInt("collaborator_id")
            );
        }
        return null;
    }
}
