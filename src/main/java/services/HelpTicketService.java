package services;

import entities.HelpTicket;
import utils.MyConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HelpTicketService {
    private final Connection con;

    public HelpTicketService() {
        con = MyConnection.getInstance().getConnection();
    }

    public void createTicket(HelpTicket ticket) throws SQLException {
        String sql = "INSERT INTO help_ticket (creator_id, course_id, subject, message, status, priority, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, ticket.getCreatorId());
            if (ticket.getCourseId() != null) ps.setInt(2, ticket.getCourseId());
            else ps.setNull(2, Types.INTEGER);
            ps.setString(3, ticket.getSubject());
            ps.setString(4, ticket.getMessage());
            ps.setString(5, "Pending");
            ps.setString(6, ticket.getPriority());
            ps.setString(7, LocalDateTime.now().toString());
            ps.setString(8, LocalDateTime.now().toString());
            ps.executeUpdate();
        }
    }

    public List<HelpTicket> getAllTickets() throws SQLException {
        List<HelpTicket> tickets = new ArrayList<>();
        String sql = "SELECT t.*, u.username, c.titre as course_title FROM help_ticket t " +
                     "JOIN users u ON t.creator_id = u.id " +
                     "LEFT JOIN cours c ON t.course_id = c.id " +
                     "ORDER BY t.created_at DESC";
        try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                HelpTicket ticket = mapTicket(rs);
                ticket.setCreatorName(rs.getString("username"));
                // You could add a courseTitle field to HelpTicket as well
                if (rs.getString("course_title") != null) {
                    ticket.setSubject("[" + rs.getString("course_title") + "] " + ticket.getSubject());
                }
                tickets.add(ticket);
            }
        }
        return tickets;
    }

    public List<HelpTicket> getTicketsByCreator(int creatorId) throws SQLException {
        List<HelpTicket> tickets = new ArrayList<>();
        String sql = "SELECT * FROM help_ticket WHERE creator_id = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, creatorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tickets.add(mapTicket(rs));
                }
            }
        }
        return tickets;
    }

    public void updateStatus(int ticketId, String status, String response) throws SQLException {
        String sql = "UPDATE help_ticket SET status = ?, admin_response = ?, updated_at = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, response);
            ps.setString(3, LocalDateTime.now().toString());
            ps.setInt(4, ticketId);
            ps.executeUpdate();
        }
    }

    public int getPendingTicketsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM help_ticket WHERE status = 'Pending'";
        try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public void updateTicket(HelpTicket ticket) throws SQLException {
        String sql = "UPDATE help_ticket SET course_id = ?, subject = ?, message = ?, priority = ?, updated_at = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            if (ticket.getCourseId() != null) ps.setInt(1, ticket.getCourseId());
            else ps.setNull(1, Types.INTEGER);
            ps.setString(2, ticket.getSubject());
            ps.setString(3, ticket.getMessage());
            ps.setString(4, ticket.getPriority());
            ps.setString(5, LocalDateTime.now().toString());
            ps.setInt(6, ticket.getId());
            ps.executeUpdate();
        }
    }

    private HelpTicket mapTicket(ResultSet rs) throws SQLException {
        HelpTicket ticket = new HelpTicket();
        ticket.setId(rs.getInt("id"));
        ticket.setCreatorId(rs.getInt("creator_id"));
        ticket.setCourseId((Integer) rs.getObject("course_id"));
        ticket.setSubject(rs.getString("subject"));
        ticket.setMessage(rs.getString("message"));
        ticket.setStatus(rs.getString("status"));
        ticket.setPriority(rs.getString("priority"));
        ticket.setAdminResponse(rs.getString("admin_response"));
        ticket.setCreatedAt(rs.getString("created_at"));
        ticket.setUpdatedAt(rs.getString("updated_at"));
        return ticket;
    }
}
