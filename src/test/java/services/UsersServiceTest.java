package services;

import entities.Users;
import services.UsersService;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder (MethodOrderer.OrderAnnotation.class)
public class UsersServiceTest {
    static UsersService usersService;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String now = LocalDateTime.now().format(formatter);

    @BeforeAll
    static void setup(){
        usersService = new UsersService();
    }

    @Test
    @Order(1)
    void testAjouterUser() throws SQLException {
        Users user = new Users("Sa3id","Sa3idos22.like@establich.com","Jzjdkl84a5z","ROLE_CONTENT_CREATOR","12858456",now,0);
        usersService.ajouter(user);
        List<Users> usersList = usersService.afficher();
        assertFalse(usersList.isEmpty());
        assertTrue(
                usersList.stream().anyMatch(usr ->
                        usr.getUsername().equals("Sa3id")
                )
        );
    }

    @Test
    @Order(2)
    void testModifierUser() throws SQLException {
        Users user = new Users("Sa3doun","Sa3idos22.like@establich.com","Jzjdkl84a5z","ROLE_CONTENT_CREATOR","12858456",now,27,0);
        usersService.modifier(user);
        List<Users> usersList = usersService.afficher();
        assertFalse(usersList.isEmpty());
        assertTrue(
                usersList.stream().anyMatch(usr ->
                        usr.getUsername().equals("Sa3doun")
        ));
    }

    @Test
    @Order(3)
    void testAffichertest()  throws SQLException {
        usersService.afficher();
        List<Users> usersList = usersService.afficher();
        assertFalse(usersList.isEmpty());

    }

    @Test
    @Order(4)
    void tessupprimerUser() throws SQLException {

        usersService.supprimer(26);
        List<Users> usersList = usersService.afficher();
        assertFalse(usersList.isEmpty());
        assertFalse(
                usersList.stream().anyMatch(usr ->
                        usr.getId() == 26)
        );

    }



}
