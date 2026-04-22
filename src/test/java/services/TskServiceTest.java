package services;

import entities.Tasks;
import services.TskService;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TskServiceTest {
    static TskService tskService;

    @BeforeAll
    static void setup() {
        tskService = new TskService();
    }

    @Test
    @Order(1)
    void testAjouterTask() throws SQLException {
        Tasks task = new Tasks("Test Task", "Description of test task", "TODO", 1);
        tskService.ajouter(task);
        List<Tasks> tasksList = tskService.afficher();
        assertFalse(tasksList.isEmpty());
        assertTrue(tasksList.stream().anyMatch(t -> t.getTitle().equals("Test Task")));
    }

    @Test
    @Order(2)
    void testAfficherTasks() throws SQLException {
        List<Tasks> tasksList = tskService.afficher();
        assertNotNull(tasksList);
    }
}
