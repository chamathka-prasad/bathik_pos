package com.chamathka.bathikpos.service;

import com.chamathka.bathikpos.dao.GRNDAO;
import com.chamathka.bathikpos.dao.ProductVariantDAO;
import com.chamathka.bathikpos.entity.GRN;
import com.chamathka.bathikpos.entity.GRNItem;
import com.chamathka.bathikpos.entity.ProductVariant;
import com.chamathka.bathikpos.util.HibernateUtil;
import com.chamathka.bathikpos.util.SessionManager;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for GRN (Goods Received Note) operations.
 * This class contains the critical ATOMIC TRANSACTION for confirming GRNs.
 * All business logic for stock intake is centralized here.
 */
public class GRNService {

    private static final Logger logger = LoggerFactory.getLogger(GRNService.class);
    private final GRNDAO grnDAO;
    private final ProductVariantDAO variantDAO;
    private final SessionManager sessionManager;

    public GRNService() {
        this.grnDAO = new GRNDAO();
        this.variantDAO = new ProductVariantDAO();
        this.sessionManager = SessionManager.getInstance();
    }

    /**
     * Confirm a GRN and add stock to inventory.
     * This is a CRITICAL ATOMIC TRANSACTION that:
     * 1. Saves the GRN header
     * 2. Saves all GRNItems
     * 3. Adds quantities to ProductVariant stock
     * 4. Updates GRN status to "CONFIRMED"
     *
     * As per SRS: If ANY part fails, the ENTIRE operation is rolled back.
     * This is the ONLY way costPrice is recorded in the system.
     *
     * @param grn The GRN object with items
     * @return The saved GRN with generated ID
     * @throws SecurityException if user is not an Admin
     */
    public GRN confirmGRN(GRN grn) {
        // Enforce Admin access (as per SRS - only Admin can process GRNs)
        sessionManager.requireAdmin();

        logger.info("Starting GRN confirmation for supplier: {}", grn.getSupplier().getSupplierName());

        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Step 1: Set GRN metadata
            grn.setGrnTimestamp(LocalDateTime.now());
            grn.setStatus("CONFIRMED");
            grn.setUser(sessionManager.getCurrentUser());
            grn.recalculateTotalCost();

            // Step 2: Save the GRN header
            session.persist(grn);

            // Step 3: Save all GRNItems and add stock ATOMICALLY
            for (GRNItem item : grn.getItems()) {
                // Get fresh variant from session
                ProductVariant variant = session.get(ProductVariant.class, item.getVariant().getVariantId());
                if (variant == null) {
                    throw new IllegalStateException("Product variant not found: " + item.getVariant().getItemCode());
                }

                // Add stock to variant
                variant.addStock(item.getQuantityReceived());
                session.merge(variant);

                // Save GRN item (this records the critical costPrice)
                item.setGrn(grn);
                session.persist(item);

                logger.debug("Added {} units of {} at cost {} (new stock: {})",
                    item.getQuantityReceived(), variant.getItemCode(),
                    item.getCostPrice(), variant.getQuantityInStock());
            }

            // Commit the transaction - ALL OR NOTHING!
            transaction.commit();

            logger.info("GRN confirmed successfully. GRN ID: {}, Total Cost: {}",
                grn.getGrnId(), grn.getTotalCost());

            return grn;

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
                logger.error("GRN confirmation FAILED and ROLLED BACK", e);
            }
            throw new RuntimeException("GRN confirmation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Save a pending GRN (not yet confirmed).
     * This allows Admin to create a GRN draft and confirm it later.
     *
     * @param grn The GRN object
     * @return The saved GRN
     */
    public GRN savePendingGRN(GRN grn) {
        sessionManager.requireAdmin();
        grn.setStatus("PENDING");
        grn.setUser(sessionManager.getCurrentUser());
        grn.recalculateTotalCost();
        return grnDAO.save(grn);
    }

    /**
     * Get all pending GRNs.
     * @return List of pending GRNs
     */
    public List<GRN> getPendingGRNs() {
        sessionManager.requireAdmin();
        return grnDAO.findByStatus("PENDING");
    }

    /**
     * Get all confirmed GRNs.
     * @return List of confirmed GRNs
     */
    public List<GRN> getConfirmedGRNs() {
        sessionManager.requireAdmin();
        return grnDAO.findByStatus("CONFIRMED");
    }

    /**
     * Get GRNs within a date range.
     * @param startDate Start date
     * @param endDate End date
     * @return List of GRNs
     */
    public List<GRN> getGRNsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        sessionManager.requireAdmin();
        return grnDAO.findByDateRange(startDate, endDate);
    }
}
