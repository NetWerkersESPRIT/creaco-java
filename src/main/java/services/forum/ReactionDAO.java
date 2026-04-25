package services.forum;

import entities.PostReaction;
import entities.ReactionType;
import utils.MyConnection;

import java.sql.*;
import java.util.EnumMap;
import java.util.Map;

/**
 * DAO for post_reaction table.
 * Provides: find, add, update, remove, and count-per-type operations.
 */
public class ReactionDAO {

    private final Connection con;

    public ReactionDAO() {
        this.con = MyConnection.getInstance().getConnection();
    }

    // -----------------------------------------------------------------------
    // Find existing reaction for (user, post) pair
    // -----------------------------------------------------------------------
    public PostReaction findByUserAndPost(int userId, int postId) throws SQLException {
        String sql = "SELECT id, type, created_at, user_id, post_id " +
                     "FROM post_reaction WHERE user_id = ? AND post_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, postId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PostReaction r = new PostReaction();
                    r.setId(rs.getInt("id"));
                    r.setType(ReactionType.fromString(rs.getString("type")));
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) r.setCreatedAt(ts.toLocalDateTime());
                    r.setUserId(rs.getInt("user_id"));
                    r.setPostId(rs.getInt("post_id"));
                    return r;
                }
            }
        }
        return null;
    }

    // -----------------------------------------------------------------------
    // Insert new reaction
    // -----------------------------------------------------------------------
    public void addReaction(int userId, int postId, ReactionType type) throws SQLException {
        String sql = "INSERT INTO post_reaction (type, created_at, user_id, post_id) VALUES (?, NOW(), ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, type.name());
            ps.setInt(2, userId);
            ps.setInt(3, postId);
            ps.executeUpdate();
        }
        System.out.println("[ReactionDAO] addReaction userId=" + userId + " postId=" + postId + " type=" + type);
    }


    // -----------------------------------------------------------------------
    // Update type of existing reaction
    // -----------------------------------------------------------------------
    public void updateReaction(int userId, int postId, ReactionType type) throws SQLException {
        String sql = "UPDATE post_reaction SET type = ? WHERE user_id = ? AND post_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, type.name());
            ps.setInt(2, userId);
            ps.setInt(3, postId);
            ps.executeUpdate();
        }
        System.out.println("[ReactionDAO] updateReaction userId=" + userId + " postId=" + postId + " type=" + type);
    }

    // -----------------------------------------------------------------------
    // Remove reaction (toggle off)
    // -----------------------------------------------------------------------
    public void removeReaction(int userId, int postId) throws SQLException {
        String sql = "DELETE FROM post_reaction WHERE user_id = ? AND post_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, postId);
            ps.executeUpdate();
        }
        System.out.println("[ReactionDAO] removeReaction userId=" + userId + " postId=" + postId);
    }

    // -----------------------------------------------------------------------
    // Count reactions per type for a given post
    // -----------------------------------------------------------------------
    public Map<ReactionType, Integer> getReactionCounts(int postId) throws SQLException {
        Map<ReactionType, Integer> counts = new EnumMap<>(ReactionType.class);
        // initialise all to zero
        for (ReactionType t : ReactionType.values()) counts.put(t, 0);

        String sql = "SELECT type, COUNT(*) AS cnt FROM post_reaction WHERE post_id = ? GROUP BY type";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReactionType t = ReactionType.fromString(rs.getString("type"));
                    if (t != null) counts.put(t, rs.getInt("cnt"));
                }
            }
        }
        return counts;
    }
}
