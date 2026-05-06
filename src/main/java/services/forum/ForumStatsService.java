package services.forum;

import entities.Post;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class ForumStatsService {
    private Connection con;

    public ForumStatsService() {
        con = MyConnection.getInstance().getConnection();
    }

    public List<Post> getTopLikedPosts(int limit) throws SQLException {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT p.*, (SELECT COUNT(*) FROM post_reaction WHERE post_id = p.id) as real_likes " +
                     "FROM post p WHERE p.status IN ('ACCEPTED', 'APPROVED') " +
                     "ORDER BY real_likes DESC LIMIT ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, limit);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Post post = mapResultSetToPost(rs);
            post.setLikes(rs.getInt("real_likes")); // Override with count from reaction table
            posts.add(post);
        }
        return posts;
    }

    public List<PostWithCommentCount> getTopCommentedPosts(int limit) throws SQLException {
        List<PostWithCommentCount> results = new ArrayList<>();
        String sql = "SELECT p.*, COUNT(c.id) as comment_count " +
                     "FROM post p " +
                     "LEFT JOIN comment c ON p.id = c.post_id " +
                     "WHERE p.status IN ('ACCEPTED', 'APPROVED') " +
                     "GROUP BY p.id " +
                     "ORDER BY comment_count DESC " +
                     "LIMIT ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, limit);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Post post = mapResultSetToPost(rs);
            int count = rs.getInt("comment_count");
            results.add(new PostWithCommentCount(post, count));
        }
        return results;
    }

    public List<UserActivity> getMostActiveUsers(int limit) throws SQLException {
        List<UserActivity> results = new ArrayList<>();
        // Simple subquery approach to avoid join issues
        String sql = "SELECT u.username, " +
                     "(SELECT COUNT(*) FROM post WHERE user_id = u.id AND status IN ('ACCEPTED', 'APPROVED')) as post_count, " +
                     "(SELECT COUNT(*) FROM comment WHERE user_id = u.id AND post_id IN (SELECT id FROM post WHERE status IN ('ACCEPTED', 'APPROVED'))) as comment_count, " +
                     "(SELECT COUNT(*) FROM post_reaction WHERE user_id = u.id) as reaction_count " +
                     "FROM users u " +
                     "WHERE u.id IN (SELECT user_id FROM post UNION SELECT user_id FROM comment UNION SELECT user_id FROM post_reaction) " +
                     "ORDER BY (post_count + comment_count + reaction_count) DESC " +
                     "LIMIT ?";
                     
        System.out.println("[ForumStatsService] Executing query: " + sql);
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, limit);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int pc = rs.getInt("post_count");
            int cc = rs.getInt("comment_count");
            int rc = 0; // Simplified for now
            results.add(new UserActivity(
                    rs.getString("username"),
                    pc,
                    cc,
                    rc,
                    pc + cc + rc
            ));
        }
        System.out.println("[ForumStatsService] Found " + results.size() + " active users.");
        return results;
    }

    public int getTotalPosts() throws SQLException {
        String sql = "SELECT COUNT(*) FROM post WHERE status IN ('ACCEPTED', 'APPROVED')";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);
        return rs.next() ? rs.getInt(1) : 0;
    }

    public int getTotalComments() throws SQLException {
        String sql = "SELECT COUNT(*) FROM comment";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);
        return rs.next() ? rs.getInt(1) : 0;
    }

    private Post mapResultSetToPost(ResultSet rs) throws SQLException {
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
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) post.setCreatedAt(ts.toLocalDateTime());
        return post;
    }

    // Helper classes for stats data
    public static class PostWithCommentCount {
        public Post post;
        public int commentCount;
        public PostWithCommentCount(Post post, int commentCount) {
            this.post = post;
            this.commentCount = commentCount;
        }
    }

    public static class UserActivity {
        public String username;
        public int postCount;
        public int commentCount;
        public int reactionCount;
        public int totalActivity;
        public UserActivity(String username, int postCount, int commentCount, int reactionCount, int totalActivity) {
            this.username = username;
            this.postCount = postCount;
            this.commentCount = commentCount;
            this.reactionCount = reactionCount;
            this.totalActivity = totalActivity;
        }
    }
}
