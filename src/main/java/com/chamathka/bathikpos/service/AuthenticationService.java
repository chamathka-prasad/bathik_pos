package com.chamathka.bathikpos.service;

import com.chamathka.bathikpos.dao.UserDAO;
import com.chamathka.bathikpos.entity.User;
import com.chamathka.bathikpos.util.PasswordUtil;
import com.chamathka.bathikpos.util.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Service class for authentication operations.
 * Handles login, logout, and user session management.
 */
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    private final UserDAO userDAO;
    private final SessionManager sessionManager;

    public AuthenticationService() {
        this.userDAO = new UserDAO();
        this.sessionManager = SessionManager.getInstance();
    }

    /**
     * Authenticate a user with username and password.
     * @param username The username
     * @param password The plain text password
     * @return The authenticated User object, or null if authentication fails
     */
    public User login(String username, String password) {
        logger.info("Login attempt for username: {}", username);

        // Find user by username
        Optional<User> userOpt = userDAO.findByUsername(username);
        if (userOpt.isEmpty()) {
            logger.warn("Login failed: User not found - {}", username);
            return null;
        }

        User user = userOpt.get();

        // Verify password
        if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            logger.warn("Login failed: Invalid password for user - {}", username);
            return null;
        }

        // Set current user in session
        sessionManager.setCurrentUser(user);
        logger.info("Login successful: {} ({})", username, user.getRole());

        return user;
    }

    /**
     * Logout the current user.
     */
    public void logout() {
        String username = sessionManager.getCurrentUsername();
        sessionManager.logout();
        logger.info("User logged out: {}", username);
    }

    /**
     * Get the currently logged-in user.
     * @return Current User, or null if not logged in
     */
    public User getCurrentUser() {
        return sessionManager.getCurrentUser();
    }

    /**
     * Check if a user is currently logged in.
     * @return true if logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return sessionManager.isLoggedIn();
    }

    /**
     * Check if the current user is an Admin.
     * @return true if Admin, false otherwise
     */
    public boolean isAdmin() {
        return sessionManager.isAdmin();
    }

    /**
     * Check if the current user is a Cashier.
     * @return true if Cashier, false otherwise
     */
    public boolean isCashier() {
        return sessionManager.isCashier();
    }
}
