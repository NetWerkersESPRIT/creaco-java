package entities.forum;

import java.time.LocalDateTime;

public class Message {
    private int id;
    private String content;
    private LocalDateTime createdAt;
    private boolean isRead;
    private int conversationId;
    private int senderUserId;

    public Message() {}

    public Message(String content, int conversationId, int senderUserId) {
        this.content = content;
        this.conversationId = conversationId;
        this.senderUserId = senderUserId;
        this.isRead = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public int getConversationId() {
        return conversationId;
    }

    public void setConversationId(int conversationId) {
        this.conversationId = conversationId;
    }

    public int getSenderUserId() {
        return senderUserId;
    }

    public void setSenderUserId(int senderUserId) {
        this.senderUserId = senderUserId;
    }
}
