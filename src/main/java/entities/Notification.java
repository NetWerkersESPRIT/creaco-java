package entities;

import java.time.LocalDateTime;

public class Notification {
    private int id;
    private String message;
    private boolean read; // maps to is_read
    private LocalDateTime createdAt;
    private int userId; // maps to user_id_id
    private Integer relatedId;
    private String targetUrl;
    private String type;
    private String status;

    public Notification() {}

    public Notification(int id, String message, boolean read, LocalDateTime createdAt, int userId, Integer relatedId, String targetUrl, String type, String status) {
        this.id = id;
        this.message = message;
        this.read = read;
        this.createdAt = createdAt;
        this.userId = userId;
        this.relatedId = relatedId;
        this.targetUrl = targetUrl;
        this.type = type;
        this.status = status;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Integer getRelatedId() { return relatedId; }
    public void setRelatedId(Integer relatedId) { this.relatedId = relatedId; }

    public String getTargetUrl() { return targetUrl; }
    public void setTargetUrl(String targetUrl) { this.targetUrl = targetUrl; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
