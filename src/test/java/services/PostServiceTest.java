package services;

import entities.Post;
import org.junit.jupiter.api.*;
import utils.MyConnection;
import services.forum.PostService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PostServiceTest {

    private static PostService postService;
    private static Connection connection;
    // Utilisé pour garder l'identifiant du post tout au long du cycle CRUD
    private static int createdPostId = -1; 

    @BeforeAll
    public static void setUpAll() {
        postService = new PostService();
        connection = MyConnection.getInstance().getConnection();
    }

    @AfterEach
    public void cleanUp() {
        // Nettoyage après chaque test.
        // Remarque : On ne supprime pas le `createdPostId` ici car il est utilisé 
        // par les tests suivants (respect de l'ordre d'exécution add -> update -> delete).
        // On pourrait ajouter ici des vérifications ou vider d'autres données orphelines si nécessaire.
        System.out.println("Fin d'exécution d'un test (Méthode @AfterEach invoquée).");
    }

    @Test
    @Order(1)
    public void testAdd() throws SQLException {
        // Préparation des données
        Post post = new Post();
        post.setTitle("Test Post");
        post.setContent("This is a test post content");
        post.setStatus("Active");
        // Utiliser un user_id valide ou 1 par défaut
        post.setUserId(1); 
        post.setImageName("test.jpg");
        post.setPdfName("test.pdf");
        post.setLikes(0);
        post.setPinned(false);
        post.setCommentLocked(false);
        post.setProfane(false);
        post.setSpam(false);
        post.setSpamScore(0);
        post.setProfaneWords(0);
        post.setGrammarErrors(0);
        post.setRefusalReason("");

        int initialSize = postService.afficher().size();
        
        // Exécution de la méthode
        postService.ajouter(post);
        
        // Vérification
        List<Post> posts = postService.afficher();
        assertEquals(initialSize + 1, posts.size(), "La liste des posts devrait augmenter de 1 après l'ajout.");
        
        // Récupérer le post créé pour les futurs tests
        Post created = posts.stream()
                .filter(p -> p.getTitle().equals("Test Post"))
                .findFirst()
                .orElse(null);
                
        assertNotNull(created, "Le post n'a pas pu être inséré ou retrouvé.");
        createdPostId = created.getId();
        assertTrue(createdPostId > 0, "L'ID du post créé doit être valide.");
    }

    @Test
    @Order(2)
    public void testGet() throws SQLException {
        // Vérification des prérequis
        assertTrue(createdPostId != -1, "Le post doit avoir été créé dans testAdd (ordre 1).");
        
        // Exécution
        List<Post> posts = postService.afficher();
        
        // Assertions
        assertNotNull(posts, "La méthode afficher ne doit pas retourner null.");
        assertFalse(posts.isEmpty(), "La base de données ne devrait pas être vide.");
        
        boolean found = posts.stream().anyMatch(p -> p.getId() == createdPostId);
        assertTrue(found, "Le post précédemment ajouté doit se trouver dans la liste.");
    }

    @Test
    @Order(3)
    public void testUpdate() throws SQLException {
        assertTrue(createdPostId != -1, "Le post doit exister pour pouvoir le modifier.");

        // Récupérer le post depuis la BDD (ou recréer un objet temporaire)
        Post postToUpdate = postService.afficher().stream()
                .filter(p -> p.getId() == createdPostId)
                .findFirst()
                .orElse(null);
                
        assertNotNull(postToUpdate, "Impossible de trouver le post à modifier.");

        // Modifier les données
        postToUpdate.setTitle("Test Post Updated");
        postToUpdate.setContent("Content has been updated!");
        
        // Exécution
        postService.modifier(createdPostId, postToUpdate);
        
        // Vérification
        Post updatedPost = postService.afficher().stream()
                .filter(p -> p.getId() == createdPostId)
                .findFirst()
                .orElse(null);
                
        assertNotNull(updatedPost, "Le post modifié n'a pas pu être retrouvé.");
        assertEquals("Test Post Updated", updatedPost.getTitle(), "Le titre du post devrait être mis à jour.");
        assertEquals("Content has been updated!", updatedPost.getContent(), "Le contenu du post devrait être mis à jour.");
    }

    @Test
    @Order(4)
    public void testDelete() throws SQLException {
        assertTrue(createdPostId != -1, "Le post doit exister pour pouvoir le supprimer.");
        
        // Récupérer la taille avant
        int initialSize = postService.afficher().size();

        // Exécution de la suppression
        postService.supprimer(createdPostId);
        
        // Vérification
        List<Post> posts = postService.afficher();
        assertEquals(initialSize - 1, posts.size(), "Le nombre total de posts doit diminuer de 1.");
        
        boolean found = posts.stream().anyMatch(p -> p.getId() == createdPostId);
        assertFalse(found, "Le post supprimé ne doit plus exister dans la BDD.");
        
        // On réinitialise l'ID à la fin
        createdPostId = -1;
    }
}
