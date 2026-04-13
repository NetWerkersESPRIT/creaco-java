package services;

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
}