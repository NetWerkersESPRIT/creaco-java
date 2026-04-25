package services;

import entities.Post;
import org.junit.jupiter.api.*;
import services.forum.ForumStatsService;
import services.forum.PostService;
import services.forum.CommentService;
import entities.Comment;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ForumStatsServiceTest {

    private ForumStatsService statsService;
    private PostService postService;
    private CommentService commentService;

    @BeforeAll
    public void setUp() {
        statsService = new ForumStatsService();
        postService = new PostService();
        commentService = new CommentService();
    }

    @Test
    @DisplayName("Test fetching top liked posts")
    public void testGetTopLikedPosts() throws SQLException {
        List<Post> topPosts = statsService.getTopLikedPosts(5);
        assertNotNull(topPosts);
        
        // Verify they are sorted by likes descending
        for (int i = 0; i < topPosts.size() - 1; i++) {
            assertTrue(topPosts.get(i).getLikes() >= topPosts.get(i+1).getLikes(), 
                "Posts should be ordered by likes descending");
        }
    }

    @Test
    @DisplayName("Test fetching top commented posts")
    public void testGetTopCommentedPosts() throws SQLException {
        List<ForumStatsService.PostWithCommentCount> topCommented = statsService.getTopCommentedPosts(5);
        assertNotNull(topCommented);
        
        // Verify they are sorted by comment count descending
        for (int i = 0; i < topCommented.size() - 1; i++) {
            assertTrue(topCommented.get(i).commentCount >= topCommented.get(i+1).commentCount,
                "Posts should be ordered by comment count descending");
        }
    }

    @Test
    @DisplayName("Test fetching most active users")
    public void testGetMostActiveUsers() throws SQLException {
        List<ForumStatsService.UserActivity> activeUsers = statsService.getMostActiveUsers(5);
        assertNotNull(activeUsers);
        
        // Verify they are sorted by total activity descending
        for (int i = 0; i < activeUsers.size() - 1; i++) {
            assertTrue(activeUsers.get(i).totalActivity >= activeUsers.get(i+1).totalActivity,
                "Users should be ordered by total activity descending");
        }
    }

    @Test
    @DisplayName("Test total counts")
    public void testTotalCounts() throws SQLException {
        int totalPosts = statsService.getTotalPosts();
        int totalComments = statsService.getTotalComments();
        
        assertTrue(totalPosts >= 0);
        assertTrue(totalComments >= 0);
        
        // Compare with actual list sizes (though status filtering might differ)
        List<Post> allPosts = postService.afficher();
        long acceptedCount = allPosts.stream().filter(p -> "ACCEPTED".equals(p.getStatus())).count();
        assertEquals(acceptedCount, totalPosts, "Total posts count should match number of ACCEPTED posts");
    }
}
