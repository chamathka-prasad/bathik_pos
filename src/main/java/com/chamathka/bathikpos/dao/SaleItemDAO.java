package com.chamathka.bathikpos.dao;

import com.chamathka.bathikpos.entity.SaleItem;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

/**
 * DAO class for SaleItem entity operations.
 */
public class SaleItemDAO extends BaseDAO<SaleItem, Long> {

    public SaleItemDAO() {
        super(SaleItem.class);
    }

    /**
     * Find all items for a specific sale.
     * @param saleId The sale ID
     * @return List of sale items
     */
    public List<SaleItem> findBySaleId(Long saleId) {
        try (Session session = getSession()) {
            Query<SaleItem> query = session.createQuery(
                "FROM SaleItem si WHERE si.sale.saleId = :saleId", SaleItem.class);
            query.setParameter("saleId", saleId);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding sale items: " + e.getMessage(), e);
        }
    }

    /**
     * Find all sales containing a specific product variant.
     * @param variantId The product variant ID
     * @return List of sale items
     */
    public List<SaleItem> findByVariantId(Long variantId) {
        try (Session session = getSession()) {
            Query<SaleItem> query = session.createQuery(
                "FROM SaleItem si WHERE si.variant.variantId = :variantId " +
                "ORDER BY si.sale.saleTimestamp DESC", SaleItem.class);
            query.setParameter("variantId", variantId);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding sale items by variant: " + e.getMessage(), e);
        }
    }
}
