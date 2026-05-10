package services;

import entities.CourseCategory;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseCategoryService {

    private final Connection con;

    public CourseCategoryService() {
        con = MyConnection.getInstance().getConnection();
        if (con != null) {
            createTableIfNotExists();
        }
    }

    private void createTableIfNotExists() {
        String createSql = "CREATE TABLE IF NOT EXISTS categorie_cours (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "nom VARCHAR(255) NOT NULL, " +
                "description TEXT, " +
                "date_de_creation DATE, " +
                "date_de_modification DATE, " +
                "slug VARCHAR(255), " +
                "deleted_at DATE" +
                ") ENGINE=InnoDB";
        
        boolean needsRecreate = false;
        try (Statement st = con.createStatement()) {
            st.executeQuery("SELECT 1 FROM categorie_cours LIMIT 1").close();
        } catch (SQLException e) {
            if (e.getMessage().contains("doesn't exist in engine") || e.getErrorCode() == 1146) {
                needsRecreate = true;
            }
        }

        try (Statement st = con.createStatement()) {
            if (needsRecreate) {
                st.executeUpdate("DROP TABLE IF EXISTS categorie_cours");
            }
            st.executeUpdate(createSql);
        } catch (SQLException e) {
            System.err.println("❌ Failed to create/verify categorie_cours table: " + e.getMessage());
        }
    }

    // =========================
    // CREATE
    // =========================
    public void ajouter(CourseCategory category) throws SQLException {

        String sql = "INSERT INTO categorie_cours " +
                "(nom, description, date_de_creation, date_de_modification, slug, deleted_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = con.prepareStatement(sql);

        ps.setString(1, category.getNom());
        ps.setString(2, category.getDescription());
        ps.setDate(3, category.getDateDeCreation());
        ps.setDate(4, category.getDateDeModification());
        ps.setString(5, category.getSlug());
        ps.setDate(6, category.getDeletedAt());

        ps.executeUpdate();

        System.out.println("CourseCategory added successfully!");
    }

    // =========================
    // READ ALL
    // =========================
    public List<CourseCategory> afficher() throws SQLException {

        List<CourseCategory> categories = new ArrayList<>();

        String sql = "SELECT * FROM categorie_cours";

        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        while (rs.next()) {

            CourseCategory category = new CourseCategory();

            category.setId(rs.getInt("id"));
            category.setNom(rs.getString("nom"));
            category.setDescription(rs.getString("description"));
            category.setDateDeCreation(rs.getDate("date_de_creation"));
            category.setDateDeModification(rs.getDate("date_de_modification"));
            category.setSlug(rs.getString("slug"));
            category.setDeletedAt(rs.getDate("deleted_at"));

            categories.add(category);
        }

        return categories;
    }

    // =========================
    // UPDATE
    // =========================
    public void modifier(int id, CourseCategory category) throws SQLException {

        String sql = "UPDATE categorie_cours SET " +
                "nom = ?, description = ?, date_de_creation = ?, " +
                "date_de_modification = ?, slug = ?, deleted_at = ? " +
                "WHERE id = ?";

        PreparedStatement ps = con.prepareStatement(sql);

        ps.setString(1, category.getNom());
        ps.setString(2, category.getDescription());
        ps.setDate(3, category.getDateDeCreation());
        ps.setDate(4, category.getDateDeModification());
        ps.setString(5, category.getSlug());
        ps.setDate(6, category.getDeletedAt());
        ps.setInt(7, id);

        ps.executeUpdate();

        System.out.println("CourseCategory updated successfully!");
    }

    // =========================
    // DELETE
    // =========================
    public void supprimer(int id) throws SQLException {
        boolean previousAutoCommit = con.getAutoCommit();

        try {
            con.setAutoCommit(false);

            for (Integer courseId : findCourseIdsByCategory(id)) {
                deleteCourseDependents(courseId);
                deleteCourse(courseId);
            }

            String sql = "DELETE FROM categorie_cours WHERE id = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }

            con.commit();
            System.out.println("CourseCategory deleted successfully!");
        } catch (SQLException exception) {
            con.rollback();
            throw exception;
        } finally {
            con.setAutoCommit(previousAutoCommit);
        }
    }





    public boolean existsByNom(String nom) throws SQLException {
        String sql = "SELECT COUNT(*) FROM categorie_cours WHERE nom = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, nom);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
        return false;
    }

    public boolean hasCourses(int categoryId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM cours WHERE categorie_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }



    // =========================
    // FIND BY ID
    // =========================
    public CourseCategory trouverParId(int id) throws SQLException {

        String sql = "SELECT * FROM categorie_cours WHERE id = ?";

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {

            CourseCategory category = new CourseCategory();

            category.setId(rs.getInt("id"));
            category.setNom(rs.getString("nom"));
            category.setDescription(rs.getString("description"));
            category.setDateDeCreation(rs.getDate("date_de_creation"));
            category.setDateDeModification(rs.getDate("date_de_modification"));
            category.setSlug(rs.getString("slug"));
            category.setDeletedAt(rs.getDate("deleted_at"));

            return category;
        }

        return null;
    }

    private List<Integer> findCourseIdsByCategory(int categoryId) throws SQLException {
        List<Integer> courseIds = new ArrayList<>();
        String sql = "SELECT id FROM cours WHERE categorie_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    courseIds.add(rs.getInt("id"));
                }
            }
        }

        return courseIds;
    }

    private void deleteCourseDependents(int courseId) throws SQLException {
        deleteByCourseId("DELETE FROM ressource WHERE cours_id = ?", courseId);
        deleteByCourseId("DELETE FROM cours_rating WHERE cours_id = ?", courseId);
        deleteByCourseId("DELETE FROM user_cours_progress WHERE cours_id = ?", courseId);
    }

    private void deleteCourse(int courseId) throws SQLException {
        deleteByCourseId("DELETE FROM cours WHERE id = ?", courseId);
    }

    private void deleteByCourseId(String sql, int courseId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ps.executeUpdate();
        }
    }
}
