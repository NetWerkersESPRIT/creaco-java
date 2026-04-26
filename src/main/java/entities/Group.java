package entities;

public class Group {
    private int id;
    private String name;
    private int creatorId; // The ID of the Content Creator who owns this group

    public Group() {}

    public Group(int id, String name, int creatorId) {
        this.id = id;
        this.name = name;
        this.creatorId = creatorId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCreatorId() { return creatorId; }
    public void setCreatorId(int creatorId) { this.creatorId = creatorId; }

    @Override
    public String toString() {
        return "Group{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", creatorId=" + creatorId +
                '}';
    }
}
