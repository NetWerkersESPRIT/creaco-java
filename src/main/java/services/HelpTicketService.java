package services;

import entities.HelpTicket;
import utils.MyConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HelpTicketService {
    private Connection con;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public HelpTicketService() {
        con = MyConnection.getInstance().getConnection();
    }

    public void add(HelpTicket t) throws SQLException {
        String sql = "INSERT INTO help_ticket (creator_id, course_id, subject, message, status, priority, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, t.getCreatorId());
        if (t.getCourseId() != null) ps.setInt(2, t.getCourseId()); else ps.setNull(2, Types.INTEGER);
        ps.setString(3, t.getSubject());
        ps.setString(4, t.getMessage());
        ps.setString(5, t.getStatus());
        ps.setString(6, t.getPriority());
        String now = LocalDateTime.now().format(formatter);
        ps.setString(7, now);
        ps.setString(8, now);
        ps.executeUpdate();
        
        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            t.setId(rs.getInt(1));
        }
    }

    public List<HelpTicket> getAll() throws SQLException {
        List<HelpTicket> tickets = new ArrayList<>();
        String sql = "SELECT * FROM help_ticket ORDER BY created_at DESC";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            tickets.add(mapResultSetToTicket(rs));
        }
        return tickets;
    }

    public List<HelpTicket> getByCreator(int creatorId) throws SQLException {
        List<HelpTicket> tickets = new ArrayList<>();
        String sql = "SELECT * FROM help_ticket WHERE creator_id = ? ORDER BY created_at DESC";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, creatorId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            tickets.add(mapResultSetToTicket(rs));
        }
        return tickets;
    }

    public void update(HelpTicket t) throws SQLException {
        String sql = "UPDATE help_ticket SET course_id = ?, subject = ?, message = ?, status = ?, priority = ?, admin_response = ?, updated_at = ? WHERE id = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        if (t.getCourseId() != null) ps.setInt(1, t.getCourseId()); else ps.setNull(1, Types.INTEGER);
        ps.setString(2, t.getSubject());
        ps.setString(3, t.getMessage());
        ps.setString(4, t.getStatus());
        ps.setString(5, t.getPriority());
        ps.setString(6, t.getAdminResponse());
        ps.setString(7, LocalDateTime.now().format(formatter));
        ps.setInt(8, t.getId());
        ps.executeUpdate();
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM help_ticket WHERE id = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    private HelpTicket mapResultSetToTicket(ResultSet rs) throws SQLException {
        HelpTicket t = new HelpTicket();
        t.setId(rs.getInt("id"));
        t.setCreatorId(rs.getInt("creator_id"));
        int courseId = rs.getInt("course_id");
        if (!rs.wasNull()) t.setCourseId(courseId);
        t.setSubject(rs.getString("subject"));
        t.setMessage(rs.getString("message"));
        t.setStatus(rs.getString("status"));
        t.setPriority(rs.getString("priority"));
        t.setAdminResponse(rs.getString("admin_response"));
        t.setCreatedAt(rs.getString("created_at"));
        t.setUpdatedAt(rs.getString("updated_at"));
        return t;
    }
}
