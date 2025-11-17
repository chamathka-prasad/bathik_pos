package com.chamathka.bathikpos.dao;

import com.chamathka.bathikpos.entity.ProductVariant;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

/**
 * DAO class for ProductVariant entity operations.
 */
public class ProductVariantDAO extends BaseDAO<ProductVariant, Long> {

    public ProductVariantDAO() {
        super(ProductVariant.class);
    }

    /**
     * Find a product variant by item code (SKU).
     * @param itemCode The item code to search for
     * @return Optional containing the variant if found, empty otherwise
     */
    public Optional<ProductVariant> findByItemCode(String itemCode) {
        try (Session session = getSession()) {
            Query<ProductVariant> query = session.createQuery(
                "FROM ProductVariant WHERE itemCode = :code", ProductVariant.class);
            query.setParameter("code", itemCode);
            return query.uniqueResultOptional();
        } catch (Exception e) {
            throw new RuntimeException("Error finding variant by item code: " + e.getMessage(), e);
        }
    }

    /**
     * Find all variants for a specific product.
     * @param productId The product ID
     * @return List of variants
     */
    public List<ProductVariant> findByProductId(Long productId) {
        try (Session session = getSession()) {
            Query<ProductVariant> query = session.createQuery(
                "FROM ProductVariant v WHERE v.product.productId = :productId ORDER BY v.itemCode",
                ProductVariant.class);
            query.setParameter("productId", productId);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding variants by product: " + e.getMessage(), e);
        }
    }

    /**
     * Get all variants with low stock (at or below threshold).
     * @return List of low stock variants
     */
    public List<ProductVariant> getLowStockVariants() {
        try (Session session = getSession()) {
            Query<ProductVariant> query = session.createQuery(
                "FROM ProductVariant v WHERE v.quantityInStock <= v.lowStockThreshold " +
                "ORDER BY v.quantityInStock ASC", ProductVariant.class);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error getting low stock variants: " + e.getMessage(), e);
        }
    }

    /**
     * Get all variants that are out of stock.
     * @return List of out-of-stock variants
     */
    public List<ProductVariant> getOutOfStockVariants() {
        try (Session session = getSession()) {
            Query<ProductVariant> query = session.createQuery(
                "FROM ProductVariant v WHERE v.quantityInStock = 0 ORDER BY v.itemCode",
                ProductVariant.class);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error getting out-of-stock variants: " + e.getMessage(), e);
        }
    }

    /**
     * Search variants by item code or product name (partial match).
     * @param searchTerm The search term
     * @return List of matching variants
     */
    public List<ProductVariant> searchVariants(String searchTerm) {
        try (Session session = getSession()) {
            Query<ProductVariant> query = session.createQuery(
                "FROM ProductVariant v WHERE " +
                "LOWER(v.itemCode) LIKE LOWER(:term) OR " +
                "LOWER(v.product.name) LIKE LOWER(:term) " +
                "ORDER BY v.itemCode", ProductVariant.class);
            query.setParameter("term", "%" + searchTerm + "%");
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error searching variants: " + e.getMessage(), e);
        }
    }

    /**
     * Get all variants with stock available (quantity > 0).
     * @return List of in-stock variants
     */
    public List<ProductVariant> getInStockVariants() {
        try (Session session = getSession()) {
            Query<ProductVariant> query = session.createQuery(
                "FROM ProductVariant v WHERE v.quantityInStock > 0 ORDER BY v.product.name, v.itemCode",
                ProductVariant.class);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error getting in-stock variants: " + e.getMessage(), e);
        }
    }
}
