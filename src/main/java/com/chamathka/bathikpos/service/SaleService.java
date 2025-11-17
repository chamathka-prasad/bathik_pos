package com.chamathka.bathikpos.service;

import com.chamathka.bathikpos.dao.CustomerDAO;
import com.chamathka.bathikpos.dao.ProductVariantDAO;
import com.chamathka.bathikpos.dao.SaleDAO;
import com.chamathka.bathikpos.entity.Customer;
import com.chamathka.bathikpos.entity.ProductVariant;
import com.chamathka.bathikpos.entity.Sale;
import com.chamathka.bathikpos.entity.SaleItem;
import com.chamathka.bathikpos.util.HibernateUtil;
import com.chamathka.bathikpos.util.SessionManager;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for Sale operations.
 * This class contains the critical ATOMIC TRANSACTION for checkout.
 * All business logic for POS is centralized here.
 */
public class SaleService {

    private static final Logger logger = LoggerFactory.getLogger(SaleService.class);
    private final SaleDAO saleDAO;
    private final ProductVariantDAO variantDAO;
    private final CustomerDAO customerDAO;
    private final SessionManager sessionManager;

    public SaleService() {
        this.saleDAO = new SaleDAO();
        this.variantDAO = new ProductVariantDAO();
        this.customerDAO = new CustomerDAO();
        this.sessionManager = SessionManager.getInstance();
    }

    /**
     * Process a complete checkout operation.
     * This is a CRITICAL ATOMIC TRANSACTION that:
     * 1. Saves the Sale
     * 2. Saves all SaleItems
     * 3. Deducts stock from ProductVariants
     * 4. Updates Customer statistics (if customer is attached)
     *
     * As per SRS: If ANY part fails, the ENTIRE operation is rolled back.
     *
     * @param sale The Sale object with items
     * @return The saved Sale with generated ID
     * @throws IllegalStateException if stock validation fails
     * @throws SecurityException if user is not authenticated
     */
    public Sale processCheckout(Sale sale) {
        // Enforce authentication
        sessionManager.requireAuthentication();

        logger.info("Starting checkout process for {} items", sale.getItems().size());

        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Step 1: Validate stock availability for ALL items
            for (SaleItem item : sale.getItems()) {
                ProductVariant variant = session.get(ProductVariant.class, item.getVariant().getVariantId());
                if (variant == null) {
                    throw new IllegalStateException("Product variant not found: " + item.getVariant().getItemCode());
                }
                if (variant.getQuantityInStock() < item.getQuantitySold()) {
                    throw new IllegalStateException(
                        String.format("Insufficient stock for %s. Available: %d, Requested: %d",
                            variant.getItemCode(), variant.getQuantityInStock(), item.getQuantitySold()));
                }
            }

            // Step 2: Save the Sale
            session.persist(sale);

            // Step 3: Save all SaleItems and deduct stock ATOMICALLY
            for (SaleItem item : sale.getItems()) {
                // Get fresh variant from session
                ProductVariant variant = session.get(ProductVariant.class, item.getVariant().getVariantId());

                // Deduct stock
                variant.deductStock(item.getQuantitySold());
                session.merge(variant);

                // Save sale item
                item.setSale(sale);
                session.persist(item);

                logger.debug("Deducted {} units of {} (new stock: {})",
                    item.getQuantitySold(), variant.getItemCode(), variant.getQuantityInStock());
            }

            // Step 4: Update customer statistics if customer is attached
            if (sale.getCustomer() != null) {
                Customer customer = session.get(Customer.class, sale.getCustomer().getCustomerId());
                if (customer != null) {
                    customer.incrementVisitCount();
                    customer.addPurchaseAmount(sale.getTotalAmount());
                    session.merge(customer);
                    logger.debug("Updated customer stats: {} (visits: {}, total: {})",
                        customer.getName(), customer.getVisitCount(), customer.getTotalPurchases());
                }
            }

            // Commit the transaction - ALL OR NOTHING!
            transaction.commit();

            logger.info("Checkout completed successfully. Sale ID: {}, Total: {}",
                sale.getSaleId(), sale.getTotalAmount());

            return sale;

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
                logger.error("Checkout FAILED and ROLLED BACK", e);
            }
            throw new RuntimeException("Checkout failed: " + e.getMessage(), e);
        }
    }

    /**
     * Validate if a product variant has sufficient stock.
     * This should be called BEFORE adding an item to cart (as per SRS UC-01).
     *
     * @param variantId The variant ID
     * @param requestedQuantity The requested quantity
     * @return true if stock is sufficient, false otherwise
     */
    public boolean checkStockAvailability(Long variantId, int requestedQuantity) {
        ProductVariant variant = variantDAO.findById(variantId)
            .orElseThrow(() -> new IllegalArgumentException("Variant not found: " + variantId));

        return variant.getQuantityInStock() >= requestedQuantity;
    }

    /**
     * Get sales within a date range.
     * @param startDate Start date
     * @param endDate End date
     * @return List of sales
     */
    public List<Sale> getSalesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return saleDAO.findByDateRange(startDate, endDate);
    }

    /**
     * Get today's sales.
     * @return List of today's sales
     */
    public List<Sale> getTodaysSales() {
        return saleDAO.getTodaysSales();
    }

    /**
     * Get total sales amount for a date range.
     * @param startDate Start date
     * @param endDate End date
     * @return Total sales amount
     */
    public BigDecimal getTotalSalesAmount(LocalDateTime startDate, LocalDateTime endDate) {
        return saleDAO.getTotalSalesAmount(startDate, endDate);
    }

    /**
     * Find a sale by ID (for returns processing).
     * @param saleId The sale ID
     * @return The Sale object
     */
    public Sale findSaleById(Long saleId) {
        return saleDAO.findById(saleId)
            .orElseThrow(() -> new IllegalArgumentException("Sale not found: " + saleId));
    }
}
