package entities;

import java.util.ArrayList;
import java.util.List;

public class Event {
    private int id;
    private String createdAt;
    private String name;
    private String description;
    private String type;
    private String category;
    private String date;
    private String time;
    private String organizer;
    private boolean isForAllUsers;
    private String meetingLink;
    private String platform;
    private String address;
    private String googleMapsLink;
    private int capacity;
    private String contact;
    private String imagePath;
    private List<Reservation> reservations;
    private List<Users> targetUsers;

    public Event() {
        this.reservations = new ArrayList<>();
        this.targetUsers = new ArrayList<>();
    }

    public Event(String createdAt, String name, String description, String type, String category,
                 String date, String time, String organizer, boolean isForAllUsers,
                 String meetingLink, String platform, String address, String googleMapsLink,
                 int capacity, String contact, String imagePath) {
        this.createdAt = createdAt;
        this.name = name;
        this.description = description;
        this.type = type;
        this.category = category;
        this.date = date;
        this.time = time;
        this.organizer = organizer;
        this.isForAllUsers = isForAllUsers;
        this.meetingLink = meetingLink;
        this.platform = platform;
        this.address = address;
        this.googleMapsLink = googleMapsLink;
        this.capacity = capacity;
        this.contact = contact;
        this.imagePath = imagePath;
        this.reservations = new ArrayList<>();
        this.targetUsers = new ArrayList<>();
    }

    public Event(int id, String createdAt, String name, String description, String type, String category,
                 String date, String time, String organizer, boolean isForAllUsers,
                 String meetingLink, String platform, String address, String googleMapsLink,
                 int capacity, String contact, String imagePath) {
        this.id = id;
        this.createdAt = createdAt;
        this.name = name;
        this.description = description;
        this.type = type;
        this.category = category;
        this.date = date;
        this.time = time;
        this.organizer = organizer;
        this.isForAllUsers = isForAllUsers;
        this.meetingLink = meetingLink;
        this.platform = platform;
        this.address = address;
        this.googleMapsLink = googleMapsLink;
        this.capacity = capacity;
        this.contact = contact;
        this.imagePath = imagePath;
        this.reservations = new ArrayList<>();
        this.targetUsers = new ArrayList<>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getOrganizer() { return organizer; }
    public void setOrganizer(String organizer) { this.organizer = organizer; }

    public boolean isForAllUsers() { return isForAllUsers; }
    public void setForAllUsers(boolean forAllUsers) { isForAllUsers = forAllUsers; }

    public String getMeetingLink() { return meetingLink; }
    public void setMeetingLink(String meetingLink) { this.meetingLink = meetingLink; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getGoogleMapsLink() { return googleMapsLink; }
    public void setGoogleMapsLink(String googleMapsLink) { this.googleMapsLink = googleMapsLink; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public List<Reservation> getReservations() { return reservations; }
    public void setReservations(List<Reservation> reservations) { this.reservations = reservations; }

    public void addReservation(Reservation reservation) {
        if (!this.reservations.contains(reservation)) {
            this.reservations.add(reservation);
            reservation.setEvent(this);
        }
    }

    public void removeReservation(Reservation reservation) {
        this.reservations.remove(reservation);
    }

    public List<Users> getTargetUsers() { return targetUsers; }
    public void setTargetUsers(List<Users> targetUsers) { this.targetUsers = targetUsers; }

    public void addTargetUser(Users user) {
        if (!this.targetUsers.contains(user)) {
            this.targetUsers.add(user);
        }
    }

    public void removeTargetUser(Users user) {
        this.targetUsers.remove(user);
    }

    public int getValidatedReservationsCount() {
        int count = 0;
        for (Reservation reservation : reservations) {
            if ("validated".equals(reservation.getStatus())) {
                count++;
            }
        }
        return count;
    }

    public int getPendingReservationsCount() {
        int count = 0;
        for (Reservation reservation : reservations) {
            if ("pending".equals(reservation.getStatus())) {
                count++;
            }
        }
        return count;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", category='" + category + '\'' +
                ", date='" + date + '\'' +
                ", organizer='" + organizer + '\'' +
                '}';
    }
}