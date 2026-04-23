package entities;

public class Tasks {
    private int id;
    private String title;
    private String description;
    private String state;
    private String created_at;
    private String time_limit;
    private String completed_at;
    private int issued_by_id;
    private int assumed_by_id;
    private int belong_to_id;

    public Tasks() {}

    public Tasks(int id, String title, String description, String state, String created_at, String time_limit, String completed_at, int issued_by_id, int assumed_by_id, int belong_to_id) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.state = state;
        this.created_at = created_at;
        this.time_limit = time_limit;
        this.completed_at = completed_at;
        this.issued_by_id = issued_by_id;
        this.assumed_by_id = assumed_by_id;
        this.belong_to_id = belong_to_id;
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

    public String getTime_limit() { return time_limit; }
    public void setTime_limit(String time_limit) { this.time_limit = time_limit; }

    public String getCompleted_at() { return completed_at; }
    public void setCompleted_at(String completed_at) { this.completed_at = completed_at; }

    public int getIssued_by_id() { return issued_by_id; }
    public void setIssued_by_id(int issued_by_id) { this.issued_by_id = issued_by_id; }

    public int getAssumed_by_id() { return assumed_by_id; }
    public void setAssumed_by_id(int assumed_by_id) { this.assumed_by_id = assumed_by_id; }

    public int getBelong_to_id() { return belong_to_id; }
    public void setBelong_to_id(int belong_to_id) { this.belong_to_id = belong_to_id; }

    @Override
    public String toString() {
        return "Tasks{" + "id=" + id + ", title='" + title + '\'' + ", state='" + state + '\'' + '}';
    }
}
