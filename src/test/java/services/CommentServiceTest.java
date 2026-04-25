package services;

import entities.Comment;
import entities.Post;
import org.junit.jupiter.api.*;

import services.forum.PostService;
import services.forum.CommentService;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CommentServiceTest {

    private static CommentService commentService;
    private static PostService postService;
    
    // Identifiants pour le suivi du cycle CRUD
    private static int createdPostId = -1;
    private static int createdCommentId = -1;

    @BeforeAll
    public static void setUpAll() throws SQLException {
        commentService = new CommentService();
        postService = new PostService();
        
        // Créer un Post pour respecter la liaison post_id
        Post p = new Post();
        p.setTitle("Post for Comment Test");
        p.setContent("Content");
        p.setStatus("Active");
        p.setUserId(1); // User ID par défaut
        
        postService.ajouter(p);
        
        // Récupérer l'ID de ce post
        Post created = postService.afficher().stream()
                .filter(post -> "Post for Comment Test".equals(post.getTitle()))
                .findFirst()
                .orElse(null);
                
        if (created != null) {
            createdPostId = created.getId();
        }
    }
    
    @AfterAll
    public static void tearDownAll() throws SQLException {
        // Optionnel : on supprime le post créé spécialement pour lier le commentaire
        // pour laisser la base de données propre.
        if (createdPostId != -1) {
            postService.supprimer(createdPostId);
            System.out.println("The generated post for comment tests has been deleted.");
        }
    }

    @AfterEach
    public void cleanUp() {
        System.out.println("End of execution of a test services.CommentServiceTest.");
    }

    @Test
    @Order(1)
    public void testAdd() throws SQLException {
        assertTrue(createdPostId != -1, "Cannot insert a comment without a Parent Post.");

        // Préparation du commentaire
        Comment comment = new Comment();
        comment.setBody("Test Comment");
        comment.setStatus("Active");
        comment.setLikes(0);
        comment.setPostId(createdPostId);  // Relation avec le Post !
        comment.setUserId(1);
        comment.setParentCommentId(null);
        comment.setProfane(false);
        comment.setProfaneWords(0);
        comment.setGrammarErrors(0);

        int initialSize = commentService.afficher().size();

        // Exécution
        commentService.ajouter(comment);

        // Vérification
        List<Comment> comments = commentService.afficher();
        assertEquals(initialSize + 1, comments.size(), "There should be 1 more comment.");

        // Récupérer le commentaire pour la suite des tests
        Comment created = comments.stream()
                .filter(c -> "Test Comment".equals(c.getBody()) && c.getPostId() == createdPostId)
                .findFirst()
                .orElse(null);

        assertNotNull(created, "The created comment could not be found.");
        createdCommentId = created.getId();
        assertTrue(createdCommentId > 0, "The ID of the created comment must be valid.");
    }

    @Test
    @Order(2)
    public void testGet() throws SQLException {
        assertTrue(createdCommentId != -1, "Le commentaire doit avoir été ajouté dans testAdd.");

        List<Comment> comments = commentService.afficher();
        
        assertNotNull(comments, "La méthode afficher ne doit pas retourner null.");
        assertFalse(comments.isEmpty(), "La base de données devrait contenir au moins 1 commentaire.");

        boolean found = comments.stream().anyMatch(c -> c.getId() == createdCommentId);
        assertTrue(found, "Le commentaire qu'on vient d'ajouter doit exister dans la liste des résultats.");
    }

    @Test
    @Order(3)
    public void testUpdate() throws SQLException {
        assertTrue(createdCommentId != -1, "Le commentaire doit exister pour pouvoir le modifier.");

        Comment commentToUpdate = commentService.afficher().stream()
                .filter(c -> c.getId() == createdCommentId)
                .findFirst()
                .orElse(null);

        assertNotNull(commentToUpdate, "Impossible de retrouver le commentaire à modifier.");

        // Modification
        commentToUpdate.setBody("Test Comment Updated");
        commentToUpdate.setStatus("Resolved");

        // Exécution
        commentService.modifier(createdCommentId, commentToUpdate);

        // Vérification
        Comment updatedComment = commentService.afficher().stream()
                .filter(c -> c.getId() == createdCommentId)
                .findFirst()
                .orElse(null);

        assertNotNull(updatedComment, "Le commentaire mis à jour doit exister.");
        assertEquals("Test Comment Updated", updatedComment.getBody(), "Le contenu du commentaire doit être mis à jour.");
        assertEquals("Resolved", updatedComment.getStatus(), "Le statut du commentaire doit être mis à jour.");
    }

    @Test
    @Order(4)
    public void testDelete() throws SQLException {
        assertTrue(createdCommentId != -1, "Le commentaire doit exister pour pouvoir le supprimer.");

        int initialSize = commentService.afficher().size();

        // Exécution
        commentService.supprimer(createdCommentId);

        // Vérification
        List<Comment> comments = commentService.afficher();
        assertEquals(initialSize - 1, comments.size(), "Le nombre de commentaires doit reculer de 1.");

        boolean found = comments.stream().anyMatch(c -> c.getId() == createdCommentId);
        assertFalse(found, "Le commentaire supprimé ne doit plus se trouver en base.");
        
        createdCommentId = -1; // RAZ pour sécurité
    }
}
