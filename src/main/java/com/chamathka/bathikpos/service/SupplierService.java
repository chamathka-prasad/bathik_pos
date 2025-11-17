package com.chamathka.bathikpos.service;

import com.chamathka.bathikpos.dao.SupplierDAO;
import com.chamathka.bathikpos.entity.Supplier;
import com.chamathka.bathikpos.util.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Service class for Supplier operations.
 * All supplier business logic is centralized here.
 */
public class SupplierService {

    private static final Logger logger = LoggerFactory.getLogger(SupplierService.class);
    private final SupplierDAO supplierDAO;
    private final SessionManager sessionManager;

    public SupplierService() {
        this.supplierDAO = new SupplierDAO();
        this.sessionManager = SessionManager.getInstance();
    }

    /**
     * Create a new supplier.
     * @param supplier The supplier to create
     * @return The saved supplier
     */
    public Supplier createSupplier(Supplier supplier) {
        sessionManager.requireAdmin();
        logger.info("Creating new supplier: {}", supplier.getSupplierName());
        return supplierDAO.save(supplier);
    }

    /**
     * Update an existing supplier.
     * @param supplier The supplier to update
     * @return The updated supplier
     */
    public Supplier updateSupplier(Supplier supplier) {
        sessionManager.requireAdmin();
        logger.info("Updating supplier: {}", supplier.getSupplierName());
        return supplierDAO.update(supplier);
    }

    /**
     * Delete a supplier.
     * @param supplier The supplier to delete
     */
    public void deleteSupplier(Supplier supplier) {
        sessionManager.requireAdmin();
        logger.info("Deleting supplier: {}", supplier.getSupplierName());
        supplierDAO.delete(supplier);
    }

    /**
     * Find a supplier by ID.
     * @param id The supplier ID
     * @return The supplier, or null if not found
     */
    public Supplier findSupplierById(Long id) {
        sessionManager.requireAdmin();
        return supplierDAO.findById(id).orElse(null);
    }

    /**
     * Get all suppliers.
     * @return List of all suppliers
     */
    public List<Supplier> getAllSuppliers() {
        sessionManager.requireAdmin();
        return supplierDAO.findAll();
    }

    /**
     * Search suppliers by name.
     * @param searchTerm The search term
     * @return List of matching suppliers
     */
    public List<Supplier> searchSuppliers(String searchTerm) {
        sessionManager.requireAdmin();
        return supplierDAO.searchByName(searchTerm);
    }
}
