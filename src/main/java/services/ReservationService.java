package services;



import entities.Event;
import entities.Users;
import entities.Reservation;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationService implements ReservationInterface<Reservation> {
    Connection con;

    public ReservationService() {
        con = MyConnection.getInstance().getConnection();
    }

    @Override
    public void ajouter(Reservation reservation) throws SQLException {
        String sql = "INSERT INTO `reservation`(`reserved_at`, `status`, `event_id`, `user_id`) VALUES (?,?,?,?)";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, reservation.getReservedAt());
        ps.setString(2, reservation.getStatus());
        ps.setInt(3, reservation.getEvent().getId());
        ps.setInt(4, reservation.getUser().getId());
        ps.executeUpdate();
        System.out.println("Réservation ajoutée avec succès!");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM `reservation` WHERE `id`=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("Réservation supprimée avec succès!");
    }

    @Override
    public List<Reservation> afficher() throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT r.*, e.name as event_name, u.username as user_username FROM reservation r " +
                "JOIN event e ON r.event_id = e.id " +
                "JOIN users u ON r.user_id = u.id";
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next()) {
            Reservation reservation = new Reservation();
            reservation.setId(rs.getInt("id"));
            reservation.setReservedAt(rs.getString("reserved_at"));
            reservation.setStatus(rs.getString("status"));

            Event event = new Event();
            event.setId(rs.getInt("event_id"));
            event.setName(rs.getString("event_name"));
            reservation.setEvent(event);

            Users user = new Users();
            user.setId(rs.getInt("user_id"));
            user.setUsername(rs.getString("user_username"));
            reservation.setUser(user);

            reservations.add(reservation);
        }
        return reservations;
    }

    @Override
    public void modifier(Reservation reservation) throws SQLException {
        String sql = "UPDATE `reservation` SET `reserved_at`=?, `status`=?, `event_id`=?, `user_id`=? WHERE `id`=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, reservation.getReservedAt());
        ps.setString(2, reservation.getStatus());
        ps.setInt(3, reservation.getEvent().getId());
        ps.setInt(4, reservation.getUser().getId());
        ps.setInt(5, reservation.getId());
        ps.executeUpdate();
        System.out.println("Réservation modifiée avec succès!");
    }
}