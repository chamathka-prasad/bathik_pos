package com.chamathka.bathikpos.dao;

import com.chamathka.bathikpos.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Base DAO class providing common CRUD operations.
 * All entity-specific DAOs should extend this class.
 * @param <T> The entity type
 * @param <ID> The ID type (typically Long)
 */
public abstract class BaseDAO<T, ID> {

    private static final Logger logger = LoggerFactory.getLogger(BaseDAO.class);
    private final Class<T> entityClass;

    protected BaseDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * Save a new entity to the database.
     * @param entity The entity to save
     * @return The saved entity with generated ID
     */
    public T save(T entity) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(entity);
            transaction.commit();
            logger.debug("Saved entity: {}", entity);
            return entity;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error saving entity", e);
            throw new RuntimeException("Error saving entity: " + e.getMessage(), e);
        }
    }

    /**
     * Update an existing entity in the database.
     * @param entity The entity to update
     * @return The updated entity
     */
    public T update(T entity) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            T updated = session.merge(entity);
            transaction.commit();
            logger.debug("Updated entity: {}", entity);
            return updated;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error updating entity", e);
            throw new RuntimeException("Error updating entity: " + e.getMessage(), e);
        }
    }

    /**
     * Delete an entity from the database.
     * @param entity The entity to delete
     */
    public void delete(T entity) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(entity);
            transaction.commit();
            logger.debug("Deleted entity: {}", entity);
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error deleting entity", e);
            throw new RuntimeException("Error deleting entity: " + e.getMessage(), e);
        }
    }

    /**
     * Find an entity by its ID.
     * @param id The entity ID
     * @return Optional containing the entity if found, empty otherwise
     */
    public Optional<T> findById(ID id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            T entity = session.find(entityClass, id);
            return Optional.ofNullable(entity);
        } catch (Exception e) {
            logger.error("Error finding entity by ID: {}", id, e);
            throw new RuntimeException("Error finding entity: " + e.getMessage(), e);
        }
    }

    /**
     * Find all entities of this type.
     * @return List of all entities
     */
    public List<T> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<T> query = session.createQuery("FROM " + entityClass.getSimpleName(), entityClass);
            return query.list();
        } catch (Exception e) {
            logger.error("Error finding all entities", e);
            throw new RuntimeException("Error finding entities: " + e.getMessage(), e);
        }
    }

    /**
     * Count all entities of this type.
     * @return The total count
     */
    public long count() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e", Long.class);
            return query.uniqueResult();
        } catch (Exception e) {
            logger.error("Error counting entities", e);
            throw new RuntimeException("Error counting entities: " + e.getMessage(), e);
        }
    }

    /**
     * Get the current Hibernate session.
     * Use this for custom queries in subclasses.
     * IMPORTANT: Remember to close the session after use.
     * @return A new Hibernate session
     */
    protected Session getSession() {
        return HibernateUtil.getSessionFactory().openSession();
    }
}
