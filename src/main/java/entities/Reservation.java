package entities;

public class Reservation {
    private int id;
    private String reservedAt;
    private String status;
    private Event event;
    private Users user;

    public Reservation() {}

    public Reservation(String reservedAt, String status, Event event, Users user) {
        this.reservedAt = reservedAt;
        this.status = status;
        this.event = event;
        this.user = user;
    }

    public Reservation(int id, String reservedAt, String status, Event event, Users user) {
        this.id = id;
        this.reservedAt = reservedAt;
        this.status = status;
        this.event = event;
        this.user = user;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getReservedAt() { return reservedAt; }
    public void setReservedAt(String reservedAt) { this.reservedAt = reservedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }

    public Users getUser() { return user; }
    public void setUser(Users user) { this.user = user; }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", reservedAt='" + reservedAt + '\'' +
                ", status='" + status + '\'' +
                ", event=" + (event != null ? event.getName() : "null") +
                ", user=" + (user != null ? user.getUsername() : "null") +
                '}';
    }
}