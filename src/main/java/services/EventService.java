package services;



import entities.Event;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventService implements EventInterface<Event> {
    Connection con;

    public EventService() {
        con = MyConnection.getInstance().getConnection();
    }

    @Override
    public void ajouter(Event event) throws SQLException {
        String sql = "INSERT INTO `event`(`created_at`, `name`, `description`, `type`, `category`, `date`, `time`, `organizer`, `is_for_all_users`, `meeting_link`, `platform`, `address`, `google_maps_link`, `capacity`, `contact`, `image_path`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, event.getCreatedAt());
        ps.setString(2, event.getName());
        ps.setString(3, event.getDescription());
        ps.setString(4, event.getType());
        ps.setString(5, event.getCategory());
        ps.setString(6, event.getDate());
        ps.setString(7, event.getTime());
        ps.setString(8, event.getOrganizer());
        ps.setBoolean(9, event.isForAllUsers());
        ps.setString(10, event.getMeetingLink());
        ps.setString(11, event.getPlatform());
        ps.setString(12, event.getAddress());
        ps.setString(13, event.getGoogleMapsLink());
        ps.setInt(14, event.getCapacity());
        ps.setString(15, event.getContact());
        ps.setString(16, event.getImagePath());
        ps.executeUpdate();
        System.out.println("Event ajouté avec succès!");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM `event` WHERE `id`=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("Event supprimé avec succès!");
    }

    @Override
    public List<Event> afficher() throws SQLException {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM event";
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next()) {
            Event event = new Event();
            event.setId(rs.getInt("id"));
            event.setCreatedAt(rs.getString("created_at"));
            event.setName(rs.getString("name"));
            event.setDescription(rs.getString("description"));
            event.setType(rs.getString("type"));
            event.setCategory(rs.getString("category"));
            event.setDate(rs.getString("date"));
            event.setTime(rs.getString("time"));
            event.setOrganizer(rs.getString("organizer"));
            event.setForAllUsers(rs.getBoolean("is_for_all_users"));
            event.setMeetingLink(rs.getString("meeting_link"));
            event.setPlatform(rs.getString("platform"));
            event.setAddress(rs.getString("address"));
            event.setGoogleMapsLink(rs.getString("google_maps_link"));
            event.setCapacity(rs.getInt("capacity"));
            event.setContact(rs.getString("contact"));
            event.setImagePath(rs.getString("image_path"));
            events.add(event);
        }
        return events;
    }

    @Override
    public void modifier(Event event) throws SQLException {
        String sql = "UPDATE `event` SET `name`=?, `description`=?, `type`=?, `category`=?, `date`=?, `time`=?, `organizer`=?, `is_for_all_users`=?, `meeting_link`=?, `platform`=?, `address`=?, `google_maps_link`=?, `capacity`=?, `contact`=?, `image_path`=? WHERE `id`=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, event.getName());
        ps.setString(2, event.getDescription());
        ps.setString(3, event.getType());
        ps.setString(4, event.getCategory());
        ps.setString(5, event.getDate());
        ps.setString(6, event.getTime());
        ps.setString(7, event.getOrganizer());
        ps.setBoolean(8, event.isForAllUsers());
        ps.setString(9, event.getMeetingLink());
        ps.setString(10, event.getPlatform());
        ps.setString(11, event.getAddress());
        ps.setString(12, event.getGoogleMapsLink());
        ps.setInt(13, event.getCapacity());
        ps.setString(14, event.getContact());
        ps.setString(15, event.getImagePath());
        ps.setInt(16, event.getId());
        ps.executeUpdate();
        System.out.println("Event modifié avec succès!");
    }
}