package com.chamathka.bathikpos.dao;

import com.chamathka.bathikpos.entity.Customer;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

/**
 * DAO class for Customer entity operations.
 */
public class CustomerDAO extends BaseDAO<Customer, Long> {

    public CustomerDAO() {
        super(Customer.class);
    }

    /**
     * Find a customer by phone number.
     * @param phoneNumber The phone number to search for
     * @return Optional containing the customer if found, empty otherwise
     */
    public Optional<Customer> findByPhoneNumber(String phoneNumber) {
        try (Session session = getSession()) {
            Query<Customer> query = session.createQuery(
                "FROM Customer WHERE phoneNumber = :phone", Customer.class);
            query.setParameter("phone", phoneNumber);
            return query.uniqueResultOptional();
        } catch (Exception e) {
            throw new RuntimeException("Error finding customer by phone: " + e.getMessage(), e);
        }
    }

    /**
     * Search customers by name (partial match).
     * @param searchTerm The search term
     * @return List of matching customers
     */
    public List<Customer> searchByName(String searchTerm) {
        try (Session session = getSession()) {
            Query<Customer> query = session.createQuery(
                "FROM Customer WHERE LOWER(name) LIKE LOWER(:term) ORDER BY name", Customer.class);
            query.setParameter("term", "%" + searchTerm + "%");
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error searching customers: " + e.getMessage(), e);
        }
    }

    /**
     * Get top customers by total purchases.
     * @param limit Maximum number of customers to return
     * @return List of top customers
     */
    public List<Customer> getTopCustomers(int limit) {
        try (Session session = getSession()) {
            Query<Customer> query = session.createQuery(
                "FROM Customer ORDER BY totalPurchases DESC", Customer.class);
            query.setMaxResults(limit);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error getting top customers: " + e.getMessage(), e);
        }
    }
}
