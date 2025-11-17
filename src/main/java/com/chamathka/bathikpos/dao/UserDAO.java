package com.chamathka.bathikpos.dao;

import com.chamathka.bathikpos.entity.User;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.Optional;

/**
 * DAO class for User entity operations.
 */
public class UserDAO extends BaseDAO<User, Long> {

    public UserDAO() {
        super(User.class);
    }

    /**
     * Find a user by username.
     * @param username The username to search for
     * @return Optional containing the user if found, empty otherwise
     */
    public Optional<User> findByUsername(String username) {
        try (Session session = getSession()) {
            Query<User> query = session.createQuery(
                "FROM User WHERE username = :username", User.class);
            query.setParameter("username", username);
            return query.uniqueResultOptional();
        } catch (Exception e) {
            throw new RuntimeException("Error finding user by username: " + e.getMessage(), e);
        }
    }
}