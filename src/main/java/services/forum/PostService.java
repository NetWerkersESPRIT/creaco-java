package services.forum;

import entities.Post;
import utils.MyConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PostService implements ForumInterface<Post> {
    Connection con;

    public PostService() {
        con = MyConnection.getInstance().getConnection();
    }

    @Override
    public void ajouter(Post post) throws SQLException {
        String sql = "INSERT INTO `post`(`title`, `content`, `status`, `user_id`, `image_name`, `pdf_name`, `likes`, `pinned`, `is_comment_locked`, `is_profane`, `is_spam`, `spam_score`, `profane_words`, `grammar_errors`, `refusal_reason`, `created_at`) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(sql);
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
}