package services.forum;

import entities.Comment;
import utils.MyConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CommentService implements ForumInterface<Comment> {
    Connection con;

    public CommentService() {
        con = MyConnection.getInstance().getConnection();
    }

    @Override
    public void ajouter(Comment comment) throws SQLException {
        String sql = "INSERT INTO `comment`(`body`, `status`, `likes`, `post_id`, `user_id`, `parent_comment_id`, `is_profane`, `profane_words`, `grammar_errors`, `created_at`, `updated_at`) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, comment.getBody());
        ps.setString(2, comment.getStatus());
        ps.setInt(3, comment.getLikes());
        ps.setInt(4, comment.getPostId());
        ps.setInt(5, comment.getUserId());
        if (comment.getParentCommentId() != null) {
            ps.setInt(6, comment.getParentCommentId());
        } else {
            ps.setNull(6, Types.INTEGER);
        }
        ps.setBoolean(7, comment.isProfane());
        ps.setInt(8, comment.getProfaneWords());
        ps.setInt(9, comment.getGrammarErrors());
        ps.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
        ps.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now()));
        ps.executeUpdate();
        System.out.println("Commentaire ajouté avec succès!");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM `comment` WHERE `id` = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("Commentaire supprimé avec succès!");
    }

    @Override
    public List<Comment> afficher() throws SQLException {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT * FROM comment";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            Comment comment = new Comment();
            comment.setId(rs.getInt("id"));
            comment.setBody(rs.getString("body"));
            comment.setStatus(rs.getString("status"));
            comment.setLikes(rs.getInt("likes"));
            comment.setPostId(rs.getInt("post_id"));
            comment.setUserId(rs.getInt("user_id"));
            int parentId = rs.getInt("parent_comment_id");
            comment.setParentCommentId(rs.wasNull() ? null : parentId);
            comment.setProfane(rs.getBoolean("is_profane"));
            comment.setProfaneWords(rs.getInt("profane_words"));
            comment.setGrammarErrors(rs.getInt("grammar_errors"));
            Timestamp createdAt = rs.getTimestamp("created_at");
            if (createdAt != null) comment.setCreatedAt(createdAt.toLocalDateTime());
            Timestamp updatedAt = rs.getTimestamp("updated_at");
            if (updatedAt != null) comment.setUpdatedAt(updatedAt.toLocalDateTime());
            comments.add(comment);
        }
        return comments;
    }

    @Override
    public void modifier(int id, Comment comment) throws SQLException {
        String sql = "UPDATE `comment` SET `body`=?, `status`=?, `post_id`=?, `user_id`=?, `updated_at`=? WHERE `id`=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, comment.getBody());
        ps.setString(2, comment.getStatus());
        ps.setInt(3, comment.getPostId());
        ps.setInt(4, comment.getUserId());
        ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
        ps.setInt(6, id);
        ps.executeUpdate();
        System.out.println("Commentaire modifié avec succès!");
    }

    public List<Comment> getCommentsByPost(int postId) throws SQLException {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT * FROM comment WHERE post_id = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, postId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Comment comment = new Comment();
            comment.setId(rs.getInt("id"));
            comment.setBody(rs.getString("body"));
            comment.setStatus(rs.getString("status"));
            comment.setLikes(rs.getInt("likes"));
            comment.setPostId(rs.getInt("post_id"));
            comment.setUserId(rs.getInt("user_id"));
            int parentId = rs.getInt("parent_comment_id");
            comment.setParentCommentId(rs.wasNull() ? null : parentId);
            comment.setProfane(rs.getBoolean("is_profane"));
            comment.setProfaneWords(rs.getInt("profane_words"));
            comment.setGrammarErrors(rs.getInt("grammar_errors"));
            Timestamp createdAt = rs.getTimestamp("created_at");
            if (createdAt != null) comment.setCreatedAt(createdAt.toLocalDateTime());
            Timestamp updatedAt = rs.getTimestamp("updated_at");
            if (updatedAt != null) comment.setUpdatedAt(updatedAt.toLocalDateTime());
            comments.add(comment);
        }
        return comments;
    }

    public int getCommentCountByPost(int postId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM comment WHERE post_id = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, postId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getInt(1);
        return 0;
    }

    /** Returns current likes count for a comment. */
    public int getLikes(int commentId) throws SQLException {
        String sql = "SELECT likes FROM comment WHERE id = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, commentId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getInt(1);
        return 0;
    }

    /** Ensures the comment_like table exists (idempotent). */
    private void ensureLikeTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS `comment_like` (" +
                     "`comment_id` INT NOT NULL, " +
                     "`user_id` INT NOT NULL, " +
                     "PRIMARY KEY (`comment_id`, `user_id`))";
        con.createStatement().executeUpdate(sql);
    }

    /** Returns true if the given user has already liked this comment. */
    public boolean hasUserLiked(int commentId, int userId) throws SQLException {
        ensureLikeTable();
        String sql = "SELECT 1 FROM comment_like WHERE comment_id = ? AND user_id = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, commentId);
        ps.setInt(2, userId);
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }

    /**
     * Per-user like toggle:
     *  - If user hasn't liked → inserts row + increments likes counter → returns new count
     *  - If user already liked → removes row + decrements likes counter → returns new count
     */
    public int toggleCommentLike(int commentId, int userId) throws SQLException {
        ensureLikeTable();
        if (hasUserLiked(commentId, userId)) {
            // Unlike
            String del = "DELETE FROM comment_like WHERE comment_id = ? AND user_id = ?";
            PreparedStatement ps = con.prepareStatement(del);
            ps.setInt(1, commentId); ps.setInt(2, userId);
            ps.executeUpdate();
            String upd = "UPDATE comment SET likes = GREATEST(likes - 1, 0) WHERE id = ?";
            PreparedStatement ps2 = con.prepareStatement(upd);
            ps2.setInt(1, commentId); ps2.executeUpdate();
        } else {
            // Like
            String ins = "INSERT INTO comment_like (comment_id, user_id) VALUES (?, ?)";
            PreparedStatement ps = con.prepareStatement(ins);
            ps.setInt(1, commentId); ps.setInt(2, userId);
            ps.executeUpdate();
            String upd = "UPDATE comment SET likes = likes + 1 WHERE id = ?";
            PreparedStatement ps2 = con.prepareStatement(upd);
            ps2.setInt(1, commentId); ps2.executeUpdate();
        }
        return getLikes(commentId);
    }
}