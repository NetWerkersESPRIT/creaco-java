package entities;

import java.time.LocalDateTime;

public class Comment {
    private int id;
    private String body;
    private String status;
    private int likes;
    private boolean isProfane;
    private int profaneWords;
    private int grammarErrors;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int postId;
    private int userId;
    private Integer parentCommentId;

    public Comment() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = "visible";
        this.likes = 0;
        this.isProfane = false;
        this.profaneWords = 0;
        this.grammarErrors = 0;
    }

    public Comment(int id, String body, String status, int likes,
                   boolean isProfane, int profaneWords, int grammarErrors,
                   LocalDateTime createdAt, LocalDateTime updatedAt,
                   int postId, int userId, Integer parentCommentId) {
        this.id = id;
        this.body = body;
        this.status = status;
        this.likes = likes;
        this.isProfane = isProfane;
        this.profaneWords = profaneWords;
        this.grammarErrors = grammarErrors;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.postId = postId;
        this.userId = userId;
        this.parentCommentId = parentCommentId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }

    public boolean isProfane() { return isProfane; }
    public void setProfane(boolean profane) { isProfane = profane; }

    public int getProfaneWords() { return profaneWords; }
    public void setProfaneWords(int profaneWords) { this.profaneWords = profaneWords; }

    public int getGrammarErrors() { return grammarErrors; }
    public void setGrammarErrors(int grammarErrors) { this.grammarErrors = grammarErrors; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public int getPostId() { return postId; }
    public void setPostId(int postId) { this.postId = postId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Integer getParentCommentId() { return parentCommentId; }
    public void setParentCommentId(Integer parentCommentId) { this.parentCommentId = parentCommentId; }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", body='" + body + '\'' +
                ", status='" + status + '\'' +
                ", likes=" + likes +
                ", postId=" + postId +
                ", userId=" + userId +
                '}';
    }
}

