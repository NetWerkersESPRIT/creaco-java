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
        String sql = "SELECT * FROM post WHERE status = 'ACCEPTED' ORDER BY likes DESC LIMIT ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, limit);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            posts.add(mapResultSetToPost(rs));
        }
        return posts;
    }

    public List<PostWithCommentCount> getTopCommentedPosts(int limit) throws SQLException {
        List<PostWithCommentCount> results = new ArrayList<>();
        String sql = "SELECT p.*, COUNT(c.id) as comment_count " +
                     "FROM post p " +
                     "LEFT JOIN comment c ON p.id = c.post_id " +
                     "WHERE p.status = 'ACCEPTED' " +
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
        String sql = "SELECT u.username, u.image, " +
                     "COALESCE(pa.cnt, 0) as post_count, " +
                     "COALESCE(ca.cnt, 0) as comment_count, " +
                     "(COALESCE(pa.cnt, 0) + COALESCE(ca.cnt, 0)) as total_activity " +
                     "FROM users u " +
                     "LEFT JOIN (SELECT user_id, COUNT(*) as cnt FROM post GROUP BY user_id) pa ON u.id = pa.user_id " +
                     "LEFT JOIN (SELECT user_id, COUNT(*) as cnt FROM comment GROUP BY user_id) ca ON u.id = ca.user_id " +
                     "WHERE u.role != 'ROLE_ADMIN' " +
                     "ORDER BY total_activity DESC " +
                     "LIMIT ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, limit);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            results.add(new UserActivity(
                    rs.getString("username"),
                    rs.getString("image"),
                    rs.getInt("post_count"),
                    rs.getInt("comment_count"),
                    rs.getInt("total_activity")
            ));
        }
        return results;
    }

    public int getTotalPosts() throws SQLException {
        String sql = "SELECT COUNT(*) FROM post WHERE status = 'ACCEPTED'";
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
        public String image;
        public int postCount;
        public int commentCount;
        public int totalActivity;
        public UserActivity(String username, String image, int postCount, int commentCount, int totalActivity) {
            this.username = username;
            this.image = image;
            this.postCount = postCount;
            this.commentCount = commentCount;
            this.totalActivity = totalActivity;
        }
    }
}
