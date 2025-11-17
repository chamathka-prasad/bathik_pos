package com.chamathka.bathikpos.util;

import com.chamathka.bathikpos.entity.User;

/**
 * Singleton class to manage the current user session.
 * Keeps track of the logged-in user throughout the application lifecycle.
 * Provides security methods to enforce role-based access control at the service layer.
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

    /**
     * Require admin access. Throws exception if current user is not an admin.
     * This method enforces authorization at the service layer as per SRS requirements.
     * @throws SecurityException if current user is not an admin
     */
    public void requireAdmin() {
        if (!isAdmin()) {
            throw new SecurityException("Admin access required. Current user role: " + getCurrentUserRole());
        }
    }

    /**
     * Require authentication. Throws exception if no user is logged in.
     * @throws SecurityException if no user is logged in
     */
    public void requireAuthentication() {
        if (!isLoggedIn()) {
            throw new SecurityException("Authentication required. No user is logged in.");
        }
    }
}