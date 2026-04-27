package entities.forum;

import java.time.LocalDateTime;

public class Conversation {
    private int id;
    private LocalDateTime createdAt;
    private int postId;
    private int ownerUserId;
    private int adminUserId;

    public Conversation() {}

    public Conversation(int postId, int ownerUserId, int adminUserId) {
        this.postId = postId;
        this.ownerUserId = ownerUserId;
        this.adminUserId = adminUserId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public int getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(int ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public int getAdminUserId() {
        return adminUserId;
    }

    public void setAdminUserId(int adminUserId) {
        this.adminUserId = adminUserId;
    }
}
