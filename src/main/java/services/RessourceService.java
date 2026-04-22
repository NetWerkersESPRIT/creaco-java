package services;

import entities.Ressource;
import utils.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class RessourceService {

    private final Connection con;

    public RessourceService() {
        con = MyConnection.getInstance().getConnection();
    }

    public List<Ressource> afficherParCours(int courseId) throws SQLException {
        List<Ressource> ressources = new ArrayList<>();
        String sql = "SELECT * FROM ressource WHERE cours_id = ? ORDER BY date_de_creation DESC, id DESC";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ressources.add(mapRessource(rs));
                }
            }
        }

        return ressources;
    }

    public void ajouter(Ressource ressource) throws SQLException {
        String sql = "INSERT INTO ressource (nom, url, type, contenu, date_de_creation, date_de_modification, cours_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, ressource.getNom());
            ps.setString(2, emptyToNull(ressource.getUrl()));
            ps.setString(3, emptyToNull(ressource.getType()));
            ps.setString(4, emptyToNull(ressource.getContenu()));
            ps.setTimestamp(5, toTimestamp(ressource.getDateDeCreation()));
            ps.setTimestamp(6, toTimestamp(ressource.getDateDeModification()));
            ps.setInt(7, ressource.getCourseId());
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    ressource.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public void modifier(Ressource ressource) throws SQLException {
        String sql = "UPDATE ressource SET nom = ?, url = ?, type = ?, contenu = ?, date_de_modification = ? "
                + "WHERE id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ressource.getNom());
            ps.setString(2, emptyToNull(ressource.getUrl()));
            ps.setString(3, emptyToNull(ressource.getType()));
            ps.setString(4, emptyToNull(ressource.getContenu()));
            ps.setTimestamp(5, toTimestamp(ressource.getDateDeModification()));
            ps.setInt(6, ressource.getId());
            ps.executeUpdate();
        }
    }

    public void supprimer(int resourceId) throws SQLException {
        String sql = "DELETE FROM ressource WHERE id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, resourceId);
            ps.executeUpdate();
        }
    }

    private Ressource mapRessource(ResultSet rs) throws SQLException {
        Ressource ressource = new Ressource();
        ressource.setId(rs.getInt("id"));
        ressource.setNom(rs.getString("nom"));
        ressource.setUrl(rs.getString("url"));
        ressource.setType(rs.getString("type"));
        ressource.setContenu(rs.getString("contenu"));
        ressource.setDateDeCreation(toString(rs.getTimestamp("date_de_creation")));
        ressource.setDateDeModification(toString(rs.getTimestamp("date_de_modification")));
        ressource.setCourseId(rs.getInt("cours_id"));
        return ressource;
    }

    private Timestamp toTimestamp(String value) {
        if (value == null || value.isBlank()) {
            return Timestamp.valueOf(LocalDateTime.now());
        }

        try {
            return Timestamp.valueOf(LocalDateTime.parse(value));
        } catch (DateTimeParseException ignored) {
            return Timestamp.valueOf(LocalDateTime.now());
        }
    }

    private String toString(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime().toString();
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
