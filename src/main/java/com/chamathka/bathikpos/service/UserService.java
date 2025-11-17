package com.chamathka.bathikpos.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.chamathka.bathikpos.dao.UserDAO;
import com.chamathka.bathikpos.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Service class for User-related business logic
 * Handles authentication and user management
 */
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Authenticate a user with username and password
     * @param username The username
     * @param password The plain text password
     * @return Optional containing the authenticated User if successful, empty otherwise
     */
    public Optional<User> authenticate(String username, String password) {
        logger.info("Authentication attempt for user: {}", username);

        // Validate input
        if (username == null || username.trim().isEmpty()) {
            logger.warn("Authentication failed: Username is empty");
            return Optional.empty();
        }

        if (password == null || password.trim().isEmpty()) {
            logger.warn("Authentication failed: Password is empty");
            return Optional.empty();
        }

        try {
            // Find user by username
            Optional<User> userOptional = userDAO.findByUsername(username);

            if (userOptional.isEmpty()) {
                logger.warn("Authentication failed: User '{}' not found", username);
                return Optional.empty();
            }

            User user = userOptional.get();

            // Verify password using BCrypt
            BCrypt.Result result = BCrypt.verifyer()
                    .verify(password.toCharArray(), user.getPasswordHash());

            if (result.verified) {
                logger.info("Authentication successful for user: {}", username);
                return Optional.of(user);
            } else {
                logger.warn("Authentication failed: Invalid password for user '{}'", username);
                return Optional.empty();
            }

        } catch (Exception e) {
            logger.error("Error during authentication for user: {}", username, e);
            return Optional.empty();
        }
    }

    /**
     * Hash a plain text password using BCrypt
     * @param plainPassword The plain text password
     * @return The hashed password
     */
    public String hashPassword(String plainPassword) {
        return BCrypt.withDefaults().hashToString(12, plainPassword.toCharArray());
    }

    /**
     * Create a new user with hashed password
     * @param username The username
     * @param plainPassword The plain text password
     * @param role The user role (ADMIN or CASHIER)
     * @return The created User
     */
    public User createUser(String username, String plainPassword, String role) {
        logger.info("Creating new user: {} with role: {}", username, role);

        // Validate input
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        if (role == null || (!role.equals("ADMIN") && !role.equals("CASHIER"))) {
            throw new IllegalArgumentException("Role must be either ADMIN or CASHIER");
        }

        // Check if username already exists
        Optional<User> existingUser = userDAO.findByUsername(username);
        if (existingUser.isPresent()) {
            logger.warn("User creation failed: Username '{}' already exists", username);
            throw new IllegalArgumentException("Username already exists");
        }

        // Create new user with hashed password
        String hashedPassword = hashPassword(plainPassword);
        User user = new User(username, hashedPassword, role);

        return userDAO.save(user);
    }

    /**
     * Update user password
     * @param userId The user ID
     * @param newPlainPassword The new plain text password
     */
    public void updatePassword(Long userId, String newPlainPassword) {
        logger.info("Updating password for user ID: {}", userId);

        Optional<User> userOptional = userDAO.findById(userId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOptional.get();
        String hashedPassword = hashPassword(newPlainPassword);
        user.setPasswordHash(hashedPassword);

        userDAO.update(user);
        logger.info("Password updated successfully for user: {}", user.getUsername());
    }

    /**
     * Get user by ID
     * @param userId The user ID
     * @return Optional containing the User if found
     */
    public Optional<User> getUserById(Long userId) {
        return userDAO.findById(userId);
    }

    /**
     * Get user by username
     * @param username The username
     * @return Optional containing the User if found
     */
    public Optional<User> getUserByUsername(String username) {
        return userDAO.findByUsername(username);
    }
}