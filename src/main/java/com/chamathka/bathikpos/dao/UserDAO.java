package com.chamathka.bathikpos.dao;

import com.chamathka.bathikpos.model.User;
import com.chamathka.bathikpos.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for User entity
 * Handles all database operations for User table
 */
public class UserDAO {

    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    /**
     * Find a user by username
     * @param username The username to search for
     * @return Optional containing the User if found, empty otherwise
     */
    public Optional<User> findByUsername(String username) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Query<User> query = session.createQuery(
                    "FROM User WHERE username = :username", User.class);
            query.setParameter("username", username);

            User user = query.uniqueResult();

            transaction.commit();

            logger.debug("User lookup for username '{}': {}", username, user != null ? "found" : "not found");
            return Optional.ofNullable(user);

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error finding user by username: {}", username, e);
            throw new RuntimeException("Error finding user by username", e);
        }
    }

    /**
     * Find a user by ID
     * @param userId The user ID
     * @return Optional containing the User if found, empty otherwise
     */
    public Optional<User> findById(Long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.get(User.class, userId);
            logger.debug("User lookup for ID '{}': {}", userId, user != null ? "found" : "not found");
            return Optional.ofNullable(user);
        } catch (Exception e) {
            logger.error("Error finding user by ID: {}", userId, e);
            throw new RuntimeException("Error finding user by ID", e);
        }
    }

    /**
     * Save a new user to the database
     * @param user The User object to save
     * @return The saved User with generated ID
     */
    public User save(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            session.persist(user);

            transaction.commit();

            logger.info("User saved successfully: {}", user.getUsername());
            return user;

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error saving user: {}", user.getUsername(), e);
            throw new RuntimeException("Error saving user", e);
        }
    }

    /**
     * Update an existing user
     * @param user The User object to update
     * @return The updated User
     */
    public User update(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            User updatedUser = session.merge(user);

            transaction.commit();

            logger.info("User updated successfully: {}", user.getUsername());
            return updatedUser;

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error updating user: {}", user.getUsername(), e);
            throw new RuntimeException("Error updating user", e);
        }
    }

    /**
     * Get all users from the database
     * @return List of all users
     */
    public List<User> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User", User.class);
            List<User> users = query.list();
            logger.debug("Retrieved {} users from database", users.size());
            return users;
        } catch (Exception e) {
            logger.error("Error retrieving all users", e);
            throw new RuntimeException("Error retrieving all users", e);
        }
    }

    /**
     * Delete a user by ID
     * @param userId The ID of the user to delete
     */
    public void deleteById(Long userId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            User user = session.get(User.class, userId);
            if (user != null) {
                session.remove(user);
                logger.info("User deleted successfully: {}", user.getUsername());
            }

            transaction.commit();

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error deleting user with ID: {}", userId, e);
            throw new RuntimeException("Error deleting user", e);
        }
    }
}