package utils;

import entities.Users;

/**
 * Singleton session holder – stores the authenticated user for the lifetime
 * of the JavaFX application so every controller can read role / username.
 */
public class SessionManager {

    private static SessionManager instance;
    private Users currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void setCurrentUser(Users user) { this.currentUser = user; }
    public Users getCurrentUser()          { return currentUser; }

    public boolean isAdmin() {
        if (currentUser == null || currentUser.getRole() == null) return false;
        String role = currentUser.getRole().toUpperCase();
        return role.contains("ADMIN");
    }

    public boolean isContentCreator() {
        if (currentUser == null || currentUser.getRole() == null) return false;
        String role = currentUser.getRole().toUpperCase();
        return role.contains("CONTENT_CREATOR") || role.contains("CREATOR");
    }

    public boolean isManager() {
        if (currentUser == null || currentUser.getRole() == null) return false;
        String role = currentUser.getRole().toUpperCase();
        return role.contains("MANAGER");
    }

    public void logout() { currentUser = null; }
}
