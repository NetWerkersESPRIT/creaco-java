package services;

import entities.Course;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CourseServiceTest {

    private static CourseService courseService;
    private static int createdCourseId = -1;

    @BeforeAll
    public static void setUp() {
        courseService = new CourseService();
    }

    @Test
    @Order(1)
    @DisplayName("Create course test")
    public void testAjouter() {
        Course c = new Course();
        c.setTitre("junit test course");
        c.setDescription("description for junit test");
        c.setSlug("junit-test-course");
        c.setStatut("draft");
        c.setNiveau("Beginner");
        c.setCategorieId(1);
        c.setDateDeCreation(LocalDateTime.now().toString());
        c.setDateDeModification(LocalDateTime.now().toString());

        assertDoesNotThrow(() -> {
            courseService.ajouter(c);
        }, "ajouter shouldn't throw an exception");
        
        // We need to fetch the ID of the created course.
        try {
            List<Course> courses = courseService.afficher();
            Course inserted = courses.stream()
                .filter(course -> "junit test course".equals(course.getTitre()))
                .findFirst()
                .orElse(null);
            
            assertNotNull(inserted, "The course should be found in the database after insertion");
            createdCourseId = inserted.getId();
        } catch (SQLException e) {
            fail("Failed to retrieve course for further testing");
        }
    }

    @Test
    @Order(2)
    @DisplayName("read course test")
    public void testTrouverParId() {
        assertTrue(createdCourseId > 0, "A valid course ID should exist before reading");
        
        assertDoesNotThrow(() -> {
            Course c = courseService.trouverParId(createdCourseId);
            assertNotNull(c, "trouverParId should return a course");
            assertEquals("JUnit Test Course", c.getTitre(), "The title should match");
        }, "trouverParId should not throw an exception");
    }

    @Test
    @Order(3)
    @DisplayName("Test de modification d'un cours (Update)")
    public void testModifier() {
        assertTrue(createdCourseId > 0, "A valid course ID should exist before tracking update");
        
        assertDoesNotThrow(() -> {
            Course c = courseService.trouverParId(createdCourseId);
            c.setTitre("JUnit Test Course Updated");
            courseService.modifier(c);
            
            Course updated = courseService.trouverParId(createdCourseId);
            assertEquals("JUnit Test Course Updated", updated.getTitre(), "The title should be updated");
        }, "modifier should not throw an exception");
    }

    @Test
    @Order(4)
    @DisplayName("Test de suppression d'un cours (Delete)")
    public void testSupprimer() {
        assertTrue(createdCourseId > 0, "A valid course ID should exist before deleting");
        
        assertDoesNotThrow(() -> {
            courseService.supprimer(createdCourseId);
            
            Course deleted = courseService.trouverParId(createdCourseId);
            assertNull(deleted, "The course should no longer exist after deletion");
        }, "supprimer should not throw an exception");
    }
}
