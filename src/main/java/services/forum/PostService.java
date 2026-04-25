package services.forum;

import entities.Post;
import entities.PostReaction;
import entities.ReactionType;
import utils.MyConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;

public class PostService implements ForumInterface<Post> {
    Connection con;

    // Simulated in-memory list for pending pin requests: requesterUserId -> postId
    private static final Map<Integer, Integer> pendingPinRequests = new HashMap<>();

    private final ReactionDAO reactionDAO = new ReactionDAO();

    public PostService() {
        con = MyConnection.getInstance().getConnection();
    }

    // -----------------------------------------------------------------------
    // Reaction System
    // -----------------------------------------------------------------------

    /**
     * Toggle / switch reaction for a user on a post.
     * <ul>
     *   <li>No existing reaction  → INSERT new reaction</li>
     *   <li>Same type clicked     → DELETE (toggle off)</li>
     *   <li>Different type        → UPDATE existing row</li>
     * </ul>
     *
     * @return the active ReactionType after the operation, or {@code null} if removed.
     */
    public ReactionType handleReaction(int userId, int postId, ReactionType chosen) throws SQLException {
        PostReaction existing = reactionDAO.findByUserAndPost(userId, postId);
        if (existing == null) {
            // No reaction yet — insert
            reactionDAO.addReaction(userId, postId, chosen);
            return chosen;
        } else if (existing.getType() == chosen) {
            // Same emoji clicked again — remove (toggle off)
            reactionDAO.removeReaction(userId, postId);
            return null;
        } else {
            // Different emoji — update
            reactionDAO.updateReaction(userId, postId, chosen);
            return chosen;
        }
    }

    /** Returns the current reaction for this user+post, or null if none. */
    public ReactionType getUserReaction(int userId, int postId) throws SQLException {
        PostReaction r = reactionDAO.findByUserAndPost(userId, postId);
        return (r != null) ? r.getType() : null;
    }

    /** Returns a map of reaction type → count for the given post. */
    public Map<ReactionType, Integer> getReactionCounts(int postId) throws SQLException {
        return reactionDAO.getReactionCounts(postId);
    }

    public void checkUserCanPin(int userId) throws Exception {
        if (pendingPinRequests.containsKey(userId)) {
            throw new Exception("You already have a pinned or pending post. Only one pin is allowed.");
        }
        List<Post> posts = afficher();
        boolean hasPinned = posts.stream().anyMatch(p -> p.getUserId() == userId && p.isPinned());
        if (hasPinned) {
            throw new Exception("You already have a pinned or pending post. Only one pin is allowed.");
        }
    }

    public void requestPin(int userId, int postId) {
        pendingPinRequests.put(userId, postId);
    }

    public boolean isPinRequested(int postId) {
        return pendingPinRequests.containsValue(postId);
    }

    public void rejectPinRequest(int postId) {
        pendingPinRequests.values().removeIf(id -> id == postId);
    }

    public String handleTogglePin(Post post, boolean isAdmin, int userId) throws Exception {
        if (isAdmin) {
            if (post.isPinned()) {
                post.setPinned(false);
                modifier(post.getId(), post);
                return null;
            } else {
                acceptPinRequest(post.getId());
                return null;
            }
        } else {
            if (post.isPinned()) {
                throw new Exception("Only admins can unpin posts directly.");
            } else {
                checkUserCanPin(userId);
                requestPin(userId, post.getId());
                return "Pin request sent to admin.";
            }
        }
    }

    public void acceptPinRequest(int postId) throws SQLException {
        // Ensure absolutely NO other posts are pinned
        List<Post> allPosts = afficher();
        for (Post p : allPosts) {
            if (p.isPinned() && p.getId() != postId) {
                p.setPinned(false);
                modifier(p.getId(), p);
            }
        }
        Post newPinned = getPostById(postId);
        if (newPinned != null) {
            newPinned.setPinned(true);
            modifier(postId, newPinned);
            pendingPinRequests.values().removeIf(id -> id == postId);
        }
    }

    public Post getPinnedPost() throws SQLException {
        return afficher().stream().filter(Post::isPinned).findFirst().orElse(null);
    }

    public Post getPostById(int id) throws SQLException {
        return afficher().stream().filter(p -> p.getId() == id).findFirst().orElse(null);
    }

    @Override
    public void ajouter(Post post) throws SQLException {
        String sql = "INSERT INTO `post`(`title`, `content`, `status`, `user_id`, `image_name`, `pdf_name`, `likes`, `pinned`, `is_comment_locked`, `is_profane`, `is_spam`, `spam_score`, `profane_words`, `grammar_errors`, `refusal_reason`, `created_at`) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, post.getTitle());
        ps.setString(2, post.getContent());
        ps.setString(3, post.getStatus());
        ps.setInt(4, post.getUserId());
        ps.setString(5, post.getImageName());
        ps.setString(6, post.getPdfName());
        ps.setInt(7, post.getLikes());
        ps.setBoolean(8, post.isPinned());
        ps.setBoolean(9, post.isCommentLocked());
        ps.setBoolean(10, post.isProfane());
        ps.setBoolean(11, post.isSpam());
        ps.setInt(12, post.getSpamScore());
        ps.setInt(13, post.getProfaneWords());
        ps.setInt(14, post.getGrammarErrors());
        ps.setString(15, post.getRefusalReason());
        ps.setTimestamp(16, Timestamp.valueOf(LocalDateTime.now()));
        ps.executeUpdate();
        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            post.setId(rs.getInt(1));
        }
        System.out.println("Post ajouté avec succès!");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        // First delete all comments associated with this post
        String deleteCommentsSQL = "DELETE FROM `comment` WHERE `post_id` = ?";
        PreparedStatement psComments = con.prepareStatement(deleteCommentsSQL);
        psComments.setInt(1, id);
        psComments.executeUpdate();
        psComments.close();

        // Then delete the post
        String sql = "DELETE FROM `post` WHERE `id` = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();

        System.out.println("Post and its comments supprimé avec succès!");
    }

    @Override
    public List<Post> afficher() throws SQLException {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT * FROM post";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            Post post = new Post();
            post.setId(rs.getInt("id"));
            post.setTitle(rs.getString("title"));
            post.setContent(rs.getString("content"));
            post.setStatus(rs.getString("status"));
            post.setUserId(rs.getInt("user_id"));
            post.setImageName(rs.getString("image_name"));
            post.setPdfName(rs.getString("pdf_name"));
            post.setLikes(rs.getInt("likes"));
            post.setPinned(rs.getBoolean("pinned"));
            post.setCommentLocked(rs.getBoolean("is_comment_locked"));
            post.setProfane(rs.getBoolean("is_profane"));
            post.setSpam(rs.getBoolean("is_spam"));
            post.setSpamScore(rs.getInt("spam_score"));
            post.setProfaneWords(rs.getInt("profane_words"));
            post.setGrammarErrors(rs.getInt("grammar_errors"));
            post.setRefusalReason(rs.getString("refusal_reason"));
            Timestamp ts = rs.getTimestamp("created_at");
            if (ts != null) post.setCreatedAt(ts.toLocalDateTime());
            posts.add(post);
        }
        return posts;
    }

    /**
     * Returns only posts whose status is "PENDING" (awaiting moderation).
     */
    public List<Post> getPendingPosts() throws SQLException {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT * FROM post WHERE status = 'PENDING' ORDER BY created_at ASC";
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Post post = new Post();
            post.setId(rs.getInt("id"));
            post.setTitle(rs.getString("title"));
            post.setContent(rs.getString("content"));
            post.setStatus(rs.getString("status"));
            post.setUserId(rs.getInt("user_id"));
            post.setImageName(rs.getString("image_name"));
            post.setPdfName(rs.getString("pdf_name"));
            post.setLikes(rs.getInt("likes"));
            post.setPinned(rs.getBoolean("pinned"));
            post.setCommentLocked(rs.getBoolean("is_comment_locked"));
            post.setProfane(rs.getBoolean("is_profane"));
            post.setSpam(rs.getBoolean("is_spam"));
            post.setSpamScore(rs.getInt("spam_score"));
            post.setProfaneWords(rs.getInt("profane_words"));
            post.setGrammarErrors(rs.getInt("grammar_errors"));
            post.setRefusalReason(rs.getString("refusal_reason"));
            Timestamp ts = rs.getTimestamp("created_at");
            if (ts != null) post.setCreatedAt(ts.toLocalDateTime());
            posts.add(post);
        }
        return posts;
    }

    /**
     * Returns only posts whose status is "ACCEPTED" (visible on forum).
     */
    public List<Post> getAcceptedPosts() throws SQLException {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT * FROM post WHERE status = 'ACCEPTED' ORDER BY pinned DESC, created_at DESC";
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Post post = new Post();
            post.setId(rs.getInt("id"));
            post.setTitle(rs.getString("title"));
            post.setContent(rs.getString("content"));
            post.setStatus(rs.getString("status"));
            post.setUserId(rs.getInt("user_id"));
            post.setImageName(rs.getString("image_name"));
            post.setPdfName(rs.getString("pdf_name"));
            post.setLikes(rs.getInt("likes"));
            post.setPinned(rs.getBoolean("pinned"));
            post.setCommentLocked(rs.getBoolean("is_comment_locked"));
            post.setProfane(rs.getBoolean("is_profane"));
            post.setSpam(rs.getBoolean("is_spam"));
            post.setSpamScore(rs.getInt("spam_score"));
            post.setProfaneWords(rs.getInt("profane_words"));
            post.setGrammarErrors(rs.getInt("grammar_errors"));
            post.setRefusalReason(rs.getString("refusal_reason"));
            Timestamp ts = rs.getTimestamp("created_at");
            if (ts != null) post.setCreatedAt(ts.toLocalDateTime());
            posts.add(post);
        }
        return posts;
    }

    /**
     * Updates only the status and refusal_reason of a post (used by backoffice moderation).
     */
    public void updatePostStatus(Post post) throws SQLException {
        String sql = "UPDATE `post` SET `status` = ?, `refusal_reason` = ? WHERE `id` = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, post.getStatus());
        if (post.getRefusalReason() == null) {
            ps.setNull(2, java.sql.Types.VARCHAR);
        } else {
            ps.setString(2, post.getRefusalReason());
        }
        ps.setInt(3, post.getId());
        ps.executeUpdate();
        System.out.println("Post status updated: id=" + post.getId() + " -> " + post.getStatus());
    }

    @Override
    public void modifier(int id, Post post) throws SQLException {
        String sql = "UPDATE `post` SET `title`=?, `content`=?, `status`=?, `user_id`=?, `image_name`=?, `pdf_name`=?, `pinned`=?, `is_comment_locked`=?, `updated_at`=? WHERE `id`=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, post.getTitle());
        ps.setString(2, post.getContent());
        ps.setString(3, post.getStatus());
        ps.setInt(4, post.getUserId());
        ps.setString(5, post.getImageName());
        ps.setString(6, post.getPdfName());
        ps.setBoolean(7, post.isPinned());
        ps.setBoolean(8, post.isCommentLocked());
        ps.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
        ps.setInt(10, id);
        ps.executeUpdate();
        System.out.println("Post modifié avec succès!");
    }
    public void likePost(int id, int newLikes) throws SQLException {
        String sql = "UPDATE `post` SET `likes` = ? WHERE `id` = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, newLikes);
        ps.setInt(2, id);
        ps.executeUpdate();
        ps.close();
    }
}