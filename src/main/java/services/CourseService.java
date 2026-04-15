package services;

import entities.Course;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CourseService {

    private final Connection con;

    public CourseService() {

        con = MyConnection.getInstance().getConnection();
    }

    public List<Course> afficher() throws SQLException {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM cours WHERE deleted_at IS NULL ORDER BY date_de_modification DESC, id DESC";

        try (Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                courses.add(mapCourse(rs));
            }
        }

        return courses;
    }

    public List<Course> afficherParCategorie(int categoryId) throws SQLException {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM cours WHERE deleted_at IS NULL AND categorie_id = ? "
                + "ORDER BY date_de_modification DESC, id DESC";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    courses.add(mapCourse(rs));
                }
            }
        }

        return courses;
    }

    public Course trouverParId(int id) throws SQLException {
        String sql = "SELECT * FROM cours WHERE id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapCourse(rs);
                }
            }
        }

        return null;
    }

    public void ajouter(Course course) throws SQLException {
        String sql = "INSERT INTO cours (titre, description, image, date_de_creation, date_de_modification, "
                + "categorie_id, slug, views, statut, niveau, duree_estimee, deleted_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, course.getTitre());
            ps.setString(2, course.getDescription());
            ps.setString(3, emptyToNull(course.getImage()));
            ps.setTimestamp(4, toTimestamp(course.getDateDeCreation()));
            ps.setTimestamp(5, toTimestamp(course.getDateDeModification()));
            ps.setInt(6, course.getCategorieId());
            ps.setString(7, course.getSlug());
            if (course.getViews() == null) {
                ps.setNull(8, java.sql.Types.INTEGER);
            } else {
                ps.setInt(8, course.getViews());
            }
            ps.setString(9, course.getStatut());
            ps.setString(10, emptyToNull(course.getNiveau()));
            if (course.getDureeEstimee() == null) {
                ps.setNull(11, java.sql.Types.INTEGER);
            } else {
                ps.setInt(11, course.getDureeEstimee());
            }
            ps.setNull(12, java.sql.Types.TIMESTAMP);
            ps.executeUpdate();
        }
    }

    public void modifier(Course course) throws SQLException {
        String sql = "UPDATE cours SET titre = ?, description = ?, image = ?, date_de_modification = ?, "
                + "categorie_id = ?, slug = ?, views = ?, statut = ?, niveau = ?, duree_estimee = ? "
                + "WHERE id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, course.getTitre());
            ps.setString(2, course.getDescription());
            ps.setString(3, emptyToNull(course.getImage()));
            ps.setTimestamp(4, toTimestamp(course.getDateDeModification()));
            ps.setInt(5, course.getCategorieId());
            ps.setString(6, course.getSlug());
            if (course.getViews() == null) {
                ps.setNull(7, java.sql.Types.INTEGER);
            } else {
                ps.setInt(7, course.getViews());
            }
            ps.setString(8, course.getStatut());
            ps.setString(9, emptyToNull(course.getNiveau()));
            if (course.getDureeEstimee() == null) {
                ps.setNull(10, java.sql.Types.INTEGER);
            } else {
                ps.setInt(10, course.getDureeEstimee());
            }
            ps.setInt(11, course.getId());
            ps.executeUpdate();
        }
    }

    public void supprimer(int id) throws SQLException {
        boolean previousAutoCommit = con.getAutoCommit();

        try {
            con.setAutoCommit(false);
            deleteDependents("DELETE FROM ressource WHERE cours_id = ?", id);
            deleteDependents("DELETE FROM cours_rating WHERE cours_id = ?", id);
            deleteDependents("DELETE FROM user_cours_progress WHERE cours_id = ?", id);
            deleteDependents("DELETE FROM cours WHERE id = ?", id);
            con.commit();
        } catch (SQLException exception) {
            con.rollback();
            throw exception;
        } finally {
            con.setAutoCommit(previousAutoCommit);
        }
    }

    public Map<Integer, String> getCategoryNames() throws SQLException {
        Map<Integer, String> categories = new LinkedHashMap<>();
        String sql = "SELECT id, nom FROM categorie_cours WHERE deleted_at IS NULL ORDER BY nom";

        try (Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                categories.put(rs.getInt("id"), rs.getString("nom"));
            }
        }

        return categories;
    }

    private Course mapCourse(ResultSet rs) throws SQLException {
        Course course = new Course();
        course.setId(rs.getInt("id"));
        course.setTitre(rs.getString("titre"));
        course.setDescription(rs.getString("description"));
        course.setImage(rs.getString("image"));
        course.setDateDeCreation(toString(rs.getTimestamp("date_de_creation")));
        course.setDateDeModification(toString(rs.getTimestamp("date_de_modification")));
        course.setCategorieId(rs.getInt("categorie_id"));
        course.setSlug(rs.getString("slug"));
        course.setViews((Integer) rs.getObject("views"));
        course.setStatut(rs.getString("statut"));
        course.setNiveau(rs.getString("niveau"));
        course.setDureeEstimee((Integer) rs.getObject("duree_estimee"));
        course.setDeletedAt(toString(rs.getTimestamp("deleted_at")));
        return course;
    }

    private String toString(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime().toString();
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

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private void deleteDependents(String sql, int courseId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ps.executeUpdate();
        }
    }
}
