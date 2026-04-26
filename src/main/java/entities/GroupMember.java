package entities;

public class GroupMember {
    private int userId;
    private int groupId;
    private String role; // MANAGER or EDITOR

    public GroupMember() {}

    public GroupMember(int userId, int groupId, String role) {
        this.userId = userId;
        this.groupId = groupId;
        this.role = role;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getGroupId() { return groupId; }
    public void setGroupId(int groupId) { this.groupId = groupId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    @Override
    public String toString() {
        return "GroupMember{" +
                "userId=" + userId +
                ", groupId=" + groupId +
                ", role='" + role + '\'' +
                '}';
    }
}
