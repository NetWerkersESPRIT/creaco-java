package database;

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
        return currentUser != null && "ROLE_ADMIN".equals(currentUser.getRole());
    }

    public boolean isContentCreator() {
        return currentUser != null && "ROLE_CONTENT_CREATOR".equals(currentUser.getRole());
    }

    public void logout() { currentUser = null; }
}
