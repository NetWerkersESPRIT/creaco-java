package entities;

public class Mission {
    private int id;
    private String title;
    private String description;
    private String state;
    private String created_at;
    private String last_update;
    private String mission_datetime;
    private String completed_at;
    private int implement_idea_id;
    private int assigned_by_id;
    private String creatorName; // New field for UI display

    public Mission() {}

    public Mission(int id, String title, String description, String state, String created_at, String last_update, String mission_datetime, String completed_at, int implement_idea_id, int assigned_by_id) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.state = state;
        this.created_at = created_at;
        this.last_update = last_update;
        this.mission_datetime = mission_datetime;
        this.completed_at = completed_at;
        this.implement_idea_id = implement_idea_id;
        this.assigned_by_id = assigned_by_id;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }

    public String getLast_update() { return last_update; }
    public void setLast_update(String last_update) { this.last_update = last_update; }

    public String getMission_datetime() { return mission_datetime; }
    public void setMission_datetime(String mission_datetime) { this.mission_datetime = mission_datetime; }

    public String getCompleted_at() { return completed_at; }
    public void setCompleted_at(String completed_at) { this.completed_at = completed_at; }

    public int getImplement_idea_id() { return implement_idea_id; }
    public void setImplement_idea_id(int implement_idea_id) { this.implement_idea_id = implement_idea_id; }

    public int getAssigned_by_id() { return assigned_by_id; }
    public void setAssigned_by_id(int assigned_by_id) { this.assigned_by_id = assigned_by_id; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

    @Override
    public String toString() {
        return "Mission{" + "id=" + id + ", title='" + title + '\'' + ", state='" + state + '\'' + '}';
    }
}
