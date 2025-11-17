package com.chamathka.bathikpos.service;

import com.chamathka.bathikpos.dao.ProductVariantDAO;
import com.chamathka.bathikpos.dao.SaleDAO;
import com.chamathka.bathikpos.entity.ProductVariant;
import com.chamathka.bathikpos.entity.Sale;
import com.chamathka.bathikpos.entity.SaleItem;
import com.chamathka.bathikpos.util.HibernateUtil;
import com.chamathka.bathikpos.util.SessionManager;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Service class for Return operations.
 * This class contains the critical ATOMIC TRANSACTION for processing returns.
 */
public class ReturnService {

    private static final Logger logger = LoggerFactory.getLogger(ReturnService.class);
    private final SaleDAO saleDAO;
    private final ProductVariantDAO variantDAO;
    private final SessionManager sessionManager;

    public ReturnService() {
        this.saleDAO = new SaleDAO();
        this.variantDAO = new ProductVariantDAO();
        this.sessionManager = SessionManager.getInstance();
    }

    /**
     * Process a return for specific items from a sale.
     * This is a CRITICAL ATOMIC TRANSACTION that:
     * 1. Validates the sale exists
     * 2. Adds the returned quantity back to ProductVariant stock
     *
     * As per SRS UC-04: Only Admin can process returns.
     *
     * @param saleId The ID of the original sale
     * @param returnedItems List of SaleItems being returned (with quantities)
     * @throws SecurityException if user is not an Admin
     */
    public void processReturn(Long saleId, List<SaleItem> returnedItems) {
        // Enforce Admin access (as per SRS - only Admin can process returns)
        sessionManager.requireAdmin();

        logger.info("Starting return process for Sale ID: {}", saleId);

        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Step 1: Validate the sale exists
            Sale originalSale = session.get(Sale.class, saleId);
            if (originalSale == null) {
                throw new IllegalArgumentException("Sale not found: " + saleId);
            }

            // Step 2: Process each returned item ATOMICALLY
            for (SaleItem returnedItem : returnedItems) {
                // Get the variant and add stock back
                ProductVariant variant = session.get(ProductVariant.class,
                    returnedItem.getVariant().getVariantId());

                if (variant == null) {
                    throw new IllegalStateException("Product variant not found: " +
                        returnedItem.getVariant().getItemCode());
                }

                // Add the returned quantity back to stock
                variant.addStock(returnedItem.getQuantitySold());
                session.merge(variant);

                logger.debug("Returned {} units of {} (new stock: {})",
                    returnedItem.getQuantitySold(), variant.getItemCode(),
                    variant.getQuantityInStock());
            }

            // Commit the transaction - ALL OR NOTHING!
            transaction.commit();

            logger.info("Return processed successfully for Sale ID: {}", saleId);

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
                logger.error("Return processing FAILED and ROLLED BACK", e);
            }
            throw new RuntimeException("Return processing failed: " + e.getMessage(), e);
        }
    }

    /**
     * Find a sale by ID for return processing.
     * @param saleId The sale ID
     * @return The Sale object with items
     */
    public Sale findSaleForReturn(Long saleId) {
        sessionManager.requireAdmin();
        return saleDAO.findById(saleId)
            .orElseThrow(() -> new IllegalArgumentException("Sale not found: " + saleId));
    }
}
