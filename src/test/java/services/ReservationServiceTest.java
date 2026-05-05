package services;

import entities.Event;
import entities.Personne;
import entities.Reservation;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReservationServiceTest {

    static ReservationService service;
    static int idReservationTest;

    static final int EXISTING_EVENT_ID = 15;
    static final int EXISTING_USER_ID  = 3;

    @BeforeAll
    static void setup() {
        service = new ReservationService();
    }

    @Test
    @Order(1)
    void testAjouterReservation() throws SQLException {
        Reservation r = new Reservation();
        r.setReservedAt("2025-06-01");
        r.setStatus("pending");

        Event event = new Event();
        event.setId(EXISTING_EVENT_ID);
        r.setEvent(event);

        Personne user = new Personne();
        user.setId(EXISTING_USER_ID);
        r.setUser(user);

        service.ajouter(r);

        List<Reservation> reservations = service.afficher();

        assertFalse(reservations.isEmpty());
        assertTrue(
                reservations.stream().anyMatch(res ->
                        res.getEvent().getId() == EXISTING_EVENT_ID &&
                                res.getUser().getId()  == EXISTING_USER_ID  &&
                                res.getStatus().equals("pending")
                )
        );

        idReservationTest = reservations.stream()
                .filter(res ->
                        res.getEvent().getId() == EXISTING_EVENT_ID &&
                                res.getUser().getId()  == EXISTING_USER_ID  &&
                                res.getStatus().equals("pending")
                )
                .findFirst()
                .get()
                .getId();
    }

    @Test
    @Order(2)
    void testAfficherReservations() throws SQLException {
        List<Reservation> reservations = service.afficher();

        assertNotNull(reservations);
        assertFalse(reservations.isEmpty());

        assertTrue(
                reservations.stream().allMatch(res ->
                        res.getEvent() != null &&
                                res.getUser()  != null &&
                                res.getStatus() != null && !res.getStatus().isEmpty()
                )
        );
    }

    @Test
    @Order(3)
    void testModifierReservation() throws SQLException {
        Reservation r = new Reservation();
        r.setId(idReservationTest);
        r.setReservedAt("2025-07-01");
        r.setStatus("confirmed");

        Event event = new Event();
        event.setId(EXISTING_EVENT_ID);
        r.setEvent(event);

        Personne user = new Personne();
        user.setId(EXISTING_USER_ID);
        r.setUser(user);

        service.modifier(r);

        List<Reservation> reservations = service.afficher();
        boolean trouve = reservations.stream()
                .anyMatch(res ->
                        res.getId() == idReservationTest &&
                                res.getStatus().equals("confirmed")
                );

        assertTrue(trouve);
    }

    @Test
    @Order(4)
    void testSupprimerReservation() throws SQLException {
        service.supprimer(idReservationTest);

        List<Reservation> reservations = service.afficher();
        boolean existe = reservations.stream()
                .anyMatch(res -> res.getId() == idReservationTest);

        assertFalse(existe);
    }

    @AfterAll
    static void cleanUp() throws SQLException {
        List<Reservation> reservations = service.afficher();
        if (!reservations.isEmpty()) {
            reservations.stream()
                    .filter(res ->
                            res.getEvent().getId() == EXISTING_EVENT_ID &&
                                    res.getUser().getId()  == EXISTING_USER_ID  &&
                                    (res.getStatus().equals("pending") ||
                                            res.getStatus().equals("confirmed"))
                    )
                    .forEach(res -> {
                        try {
                            service.supprimer(res.getId());
                        } catch (SQLException ex) {
                            System.err.println("Cleanup error: " + ex.getMessage());
                        }
                    });
        }
    }
}
