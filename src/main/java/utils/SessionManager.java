package utils;

import entities.Users;

/**
 * Singleton session holder – stores the authenticated user for the lifetime
 * of the JavaFX application so every controller can read role / username.
 */
public class SessionManager {

    private static SessionManager instance;
    private Users currentUser;
    private boolean isVisitor = false;
    private final java.util.Map<Integer, entities.ReactionType> visitorReactions = new java.util.HashMap<>();

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void setCurrentUser(Users user) { 
        this.currentUser = user; 
        this.isVisitor = false;
    }
    public Users getCurrentUser()          { return currentUser; }

    public void setVisitor(boolean isVisitor) {
        this.isVisitor = isVisitor;
        if (isVisitor) {
            this.currentUser = null;
        }
    }
    public boolean isVisitor() { return isVisitor; }

    public void setVisitorReaction(int postId, entities.ReactionType type) {
        if (type == null) {
            visitorReactions.remove(postId);
        } else {
            visitorReactions.put(postId, type);
        }
    }
    public entities.ReactionType getVisitorReaction(int postId) {
        return visitorReactions.get(postId);
    }
    
    // Legacy support for toggleVisitorLike
    public void toggleVisitorLike(int postId) {
        if (visitorReactions.get(postId) == entities.ReactionType.LIKE) {
            visitorReactions.remove(postId);
        } else {
            visitorReactions.put(postId, entities.ReactionType.LIKE);
        }
    }
    public boolean hasVisitorLiked(int postId) {
        return visitorReactions.get(postId) == entities.ReactionType.LIKE;
    }

    public boolean isAdmin() {
        return currentUser != null && "ROLE_ADMIN".equals(currentUser.getRole());
    }

    public boolean isContentCreator() {
        return currentUser != null && "ROLE_CONTENT_CREATOR".equals(currentUser.getRole());
    }

    public void logout() { 
        currentUser = null; 
        isVisitor = false;
        visitorReactions.clear();
    }
}
