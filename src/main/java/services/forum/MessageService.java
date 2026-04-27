package services.forum;

import entities.forum.Conversation;
import entities.forum.Message;
import utils.MyConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageService {
    private Connection con;

    public MessageService() {
        con = MyConnection.getInstance().getConnection();
    }

    public Conversation getOrCreateConversation(int postId, int ownerUserId, int adminUserId) throws SQLException {
        // Check if exists
        String checkSql = "SELECT * FROM conversation WHERE post_id = ? AND owner_user_id = ? AND admin_user_id = ?";
        PreparedStatement psCheck = con.prepareStatement(checkSql);
        psCheck.setInt(1, postId);
        psCheck.setInt(2, ownerUserId);
        psCheck.setInt(3, adminUserId);
        ResultSet rs = psCheck.executeQuery();
        
        if (rs.next()) {
            Conversation c = new Conversation();
            c.setId(rs.getInt("id"));
            c.setPostId(rs.getInt("post_id"));
            c.setOwnerUserId(rs.getInt("owner_user_id"));
            c.setAdminUserId(rs.getInt("admin_user_id"));
            Timestamp ts = rs.getTimestamp("created_at");
            if (ts != null) c.setCreatedAt(ts.toLocalDateTime());
            return c;
        }

        // Create new
        String insertSql = "INSERT INTO conversation (post_id, owner_user_id, admin_user_id, created_at) VALUES (?, ?, ?, ?)";
        PreparedStatement psInsert = con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
        psInsert.setInt(1, postId);
        psInsert.setInt(2, ownerUserId);
        psInsert.setInt(3, adminUserId);
        psInsert.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
        psInsert.executeUpdate();
        
        ResultSet rsKeys = psInsert.getGeneratedKeys();
        if (rsKeys.next()) {
            Conversation c = new Conversation(postId, ownerUserId, adminUserId);
            c.setId(rsKeys.getInt(1));
            c.setCreatedAt(LocalDateTime.now());
            return c;
        }
        
        return null;
    }

    public Conversation getConversationById(int id) throws SQLException {
        String sql = "SELECT * FROM conversation WHERE id = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            Conversation c = new Conversation();
            c.setId(rs.getInt("id"));
            c.setPostId(rs.getInt("post_id"));
            c.setOwnerUserId(rs.getInt("owner_user_id"));
            c.setAdminUserId(rs.getInt("admin_user_id"));
            Timestamp ts = rs.getTimestamp("created_at");
            if (ts != null) c.setCreatedAt(ts.toLocalDateTime());
            return c;
        }
        return null;
    }

    public void addMessage(int conversationId, int senderUserId, String content) throws SQLException {
        String sql = "INSERT INTO message (content, created_at, is_read, conversation_id, sender_user_id) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, content);
        ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
        ps.setBoolean(3, false);
        ps.setInt(4, conversationId);
        ps.setInt(5, senderUserId);
        ps.executeUpdate();
    }

    public List<Message> getMessagesByConversation(int conversationId) throws SQLException {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM message WHERE conversation_id = ? ORDER BY created_at ASC";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, conversationId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Message m = new Message();
            m.setId(rs.getInt("id"));
            m.setContent(rs.getString("content"));
            m.setConversationId(rs.getInt("conversation_id"));
            m.setSenderUserId(rs.getInt("sender_user_id"));
            m.setRead(rs.getBoolean("is_read"));
            Timestamp ts = rs.getTimestamp("created_at");
            if (ts != null) m.setCreatedAt(ts.toLocalDateTime());
            messages.add(m);
        }
        return messages;
    }
    
    public void markMessagesAsRead(int conversationId, int currentUserId) throws SQLException {
        String sql = "UPDATE message SET is_read = true WHERE conversation_id = ? AND sender_user_id != ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, conversationId);
        ps.setInt(2, currentUserId);
        ps.executeUpdate();
    }
}
