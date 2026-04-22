package entities;

import java.time.LocalDateTime;

public class HelpTicket {
    private int id;
    private int creatorId;
    private String creatorName;
    private Integer courseId; // Optional: specific course related
    private String subject;
    private String message;
    private String status; // Pending, In_Progress, Resolved, Closed
    private String priority; // Low, Medium, High
    private String adminResponse;
    private String createdAt;
    private String updatedAt;

    // Constructors
    public HelpTicket() {}

    public HelpTicket(int creatorId, Integer courseId, String subject, String message, String priority) {
        this.creatorId = creatorId;
        this.courseId = courseId;
        this.subject = subject;
        this.message = message;
        this.priority = priority;
        this.status = "Pending";
        this.createdAt = LocalDateTime.now().toString();
        this.updatedAt = this.createdAt;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCreatorId() { return creatorId; }
    public void setCreatorId(int creatorId) { this.creatorId = creatorId; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getAdminResponse() { return adminResponse; }
    public void setAdminResponse(String adminResponse) { this.adminResponse = adminResponse; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
