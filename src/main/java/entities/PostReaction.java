package entities;

import java.time.LocalDateTime;

/**
 * Entity mapping the `post_reaction` table.
 * Fields: id, type, created_at, user_id, post_id
 */
public class PostReaction {

    private int id;
    private ReactionType type;
    private LocalDateTime createdAt;
    private int userId;
    private int postId;

    public PostReaction() {
        this.createdAt = LocalDateTime.now();
    }

    public PostReaction(int id, ReactionType type, LocalDateTime createdAt, int userId, int postId) {
        this.id = id;
        this.type = type;
        this.createdAt = createdAt;
        this.userId = userId;
        this.postId = postId;
    }

    // ---------- Getters & Setters ----------

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public ReactionType getType() { return type; }
    public void setType(ReactionType type) { this.type = type; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getPostId() { return postId; }
    public void setPostId(int postId) { this.postId = postId; }

    @Override
    public String toString() {
        return "PostReaction{id=" + id + ", type=" + type + ", userId=" + userId + ", postId=" + postId + "}";
    }
}
