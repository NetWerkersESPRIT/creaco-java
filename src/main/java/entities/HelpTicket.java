package entities;

public class HelpTicket {
    private int id;
    private int creatorId;
    private Integer courseId; // Nullable
    private String subject;
    private String message;
    private String status;
    private String priority;
    private String adminResponse;
    private String createdAt;
    private String updatedAt;

    public HelpTicket() {
        this.status = "Pending";
        this.priority = "Moyenne";
    }

    public HelpTicket(int id, int creatorId, Integer courseId, String subject, String message, String status, String priority, String adminResponse, String createdAt, String updatedAt) {
        this.id = id;
        this.creatorId = creatorId;
        this.courseId = courseId;
        this.subject = subject;
        this.message = message;
        this.status = status;
        this.priority = priority;
        this.adminResponse = adminResponse;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCreatorId() { return creatorId; }
    public void setCreatorId(int creatorId) { this.creatorId = creatorId; }

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

    @Override
    public String toString() {
        return "HelpTicket{" +
                "id=" + id +
                ", subject='" + subject + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
