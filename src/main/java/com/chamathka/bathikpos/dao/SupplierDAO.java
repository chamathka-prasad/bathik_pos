package com.chamathka.bathikpos.dao;

import com.chamathka.bathikpos.entity.Supplier;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

/**
 * DAO class for Supplier entity operations.
 */
public class SupplierDAO extends BaseDAO<Supplier, Long> {

    public SupplierDAO() {
        super(Supplier.class);
    }

    /**
     * Search suppliers by name (partial match).
     * @param searchTerm The search term
     * @return List of matching suppliers
     */
    public List<Supplier> searchByName(String searchTerm) {
        try (Session session = getSession()) {
            Query<Supplier> query = session.createQuery(
                "FROM Supplier WHERE LOWER(supplierName) LIKE LOWER(:term) ORDER BY supplierName",
                Supplier.class);
            query.setParameter("term", "%" + searchTerm + "%");
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error searching suppliers: " + e.getMessage(), e);
        }
    }
}
