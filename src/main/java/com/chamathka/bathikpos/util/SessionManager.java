package com.chamathka.bathikpos.util;

import com.chamathka.bathikpos.model.User;

/**
 * Singleton class to manage the current user session
 * Keeps track of the logged-in user throughout the application lifecycle
 */
public class SessionManager {

    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {
        // Private constructor for singleton
    }

    /**
     * Get the SessionManager instance
     * @return SessionManager instance
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Set the current logged-in user
     * @param user The authenticated user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Get the current logged-in user
     * @return The current user, or null if no user is logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Check if a user is currently logged in
     * @return true if a user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Check if the current user is an Admin
     * @return true if current user is Admin, false otherwise
     */
    public boolean isAdmin() {
        return currentUser != null && "ADMIN".equals(currentUser.getRole());
    }

    /**
     * Check if the current user is a Cashier
     * @return true if current user is Cashier, false otherwise
     */
    public boolean isCashier() {
        return currentUser != null && "CASHIER".equals(currentUser.getRole());
    }

    /**
     * Get the username of the current user
     * @return Username or "Guest" if no user is logged in
     */
    public String getCurrentUsername() {
        return currentUser != null ? currentUser.getUsername() : "Guest";
    }

    /**
     * Get the role of the current user
     * @return User role or "NONE" if no user is logged in
     */
    public String getCurrentUserRole() {
        return currentUser != null ? currentUser.getRole() : "NONE";
    }

    /**
     * Clear the current user session (logout)
     */
    public void logout() {
        this.currentUser = null;
    }
}