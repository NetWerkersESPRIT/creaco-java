package entities;

public class Users {
    private String username;
    private String email;
    private String password;
    private String role;
    private String numtel;
    private String created_at;
    private int id;
    private int points;

    public Users(){}

    public Users(String username, String email, String password, String role, String numtel, String created_at, int points) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.numtel = numtel;
        this.created_at = created_at;
        this.id = id;
        this.points = points;
    }
    public Users(String username, String email, String password, String role, String numtel, String created_at, int id, int points) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.numtel = numtel;
        this.created_at = created_at;
        this.id = id;
        this.points = points;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getNumtel() {
        return numtel;
    }

    public void setNumtel(String numtel) {
        this.numtel = numtel;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    @Override
    public String toString() {
        return '\n'+
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password=" + password + '\''+
                ", Role=" + role + '\''
                ;
    }
}
