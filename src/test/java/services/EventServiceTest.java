package services;


import entities.Event;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EventServiceTest {

    static EventService service;
    static int idEventTest;

    @BeforeAll
    static void setup() {
        service = new EventService();
    }

    @Test
    @Order(1)
    void testAjouterEvent() throws SQLException {
        Event e = new Event();
        e.setCreatedAt("2025-01-01");
        e.setName("TestEvent");
        e.setDescription("Description de test");
        e.setType("Online");
        e.setCategory("Education");
        e.setDate("2025-06-01");
        e.setTime("10:00");
        e.setOrganizer("OrgTest");
        e.setForAllUsers(true);
        e.setMeetingLink("https://www.meet.com");
        e.setPlatform("Zoom");
        e.setAddress("");
        e.setGoogleMapsLink("");
        e.setCapacity(100);
        e.setContact("test@test.com");
        e.setImagePath("");

        service.ajouter(e);

        List<Event> events = service.afficher();

        assertFalse(events.isEmpty());
        assertTrue(
                events.stream().anyMatch(ev -> ev.getName().equals("TestEvent"))
        );

        idEventTest = events.stream()
                .filter(ev -> ev.getName().equals("TestEvent"))
                .findFirst()
                .get()
                .getId();
    }

    @Test
    @Order(2)
    void testAfficherEvents() throws SQLException {
        List<Event> events = service.afficher();

        assertNotNull(events);
        assertFalse(events.isEmpty());

        assertTrue(
                events.stream().allMatch(ev -> ev.getName() != null && !ev.getName().isEmpty())
        );
    }

    @Test
    @Order(3)
    void testModifierEvent() throws SQLException {
        Event e = new Event();
        e.setId(idEventTest);
        e.setName("TestEventModifie");
        e.setDescription("Description modifiée");
        e.setType("In-Person");
        e.setCategory("Sport");
        e.setDate("2025-07-01");
        e.setTime("14:00");
        e.setOrganizer("OrgModifie");
        e.setForAllUsers(false);
        e.setMeetingLink("");
        e.setPlatform("");
        e.setAddress("123 Rue Modifiée");
        e.setGoogleMapsLink("https://maps.test.com");
        e.setCapacity(50);
        e.setContact("test@gmail.com");
        e.setImagePath("");

        service.modifier(e);

        List<Event> events = service.afficher();
        boolean trouve = events.stream()
                .anyMatch(ev -> ev.getName().equals("TestEventModifie"));

        assertTrue(trouve);
    }

    @Test
    @Order(4)
    void testSupprimerEvent() throws SQLException {
        service.supprimer(idEventTest);

        List<Event> events = service.afficher();
        boolean existe = events.stream()
                .anyMatch(ev -> ev.getId() == idEventTest);

        assertFalse(existe);
    }


    @AfterAll
    static void cleanUp() throws SQLException {
        List<Event> events = service.afficher();
        if (!events.isEmpty()) {

            events.stream()
                    .filter(ev -> ev.getName().equals("TestEvent")
                            || ev.getName().equals("TestEventModifie"))
                    .forEach(ev -> {
                        try {
                            service.supprimer(ev.getId());
                        } catch (SQLException ex) {
                            System.err.println("Cleanup error: " + ex.getMessage());
                        }
                    });
        }
    }
}