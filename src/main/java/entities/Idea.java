package entities;

public class Idea {
    private int id;
    private String title;
    private String description;
    private String category;
    private String created_at;
    private String last_used;
    private int creator_id;

    public Idea() {}

    public Idea(int id, String title, String description, String category, String created_at, String last_used, int creator_id) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.created_at = created_at;
        this.last_used = last_used;
        this.creator_id = creator_id;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }

    public String getLast_used() { return last_used; }
    public void setLast_used(String last_used) { this.last_used = last_used; }

    public int getCreator_id() { return creator_id; }
    public void setCreator_id(int creator_id) { this.creator_id = creator_id; }

    @Override
    public String toString() {
        return "Idea{" + "id=" + id + ", title='" + title + '\'' + ", category='" + category + '\'' + '}';
    }
}
