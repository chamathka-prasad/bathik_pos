package com.chamathka.bathikpos.dao;

import com.chamathka.bathikpos.entity.Sale;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DAO class for Sale entity operations.
 */
public class SaleDAO extends BaseDAO<Sale, Long> {

    public SaleDAO() {
        super(Sale.class);
    }

    /**
     * Find sales within a date range.
     * @param startDate Start date
     * @param endDate End date
     * @return List of sales in the date range
     */
    public List<Sale> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        try (Session session = getSession()) {
            Query<Sale> query = session.createQuery(
                "FROM Sale WHERE saleTimestamp BETWEEN :start AND :end ORDER BY saleTimestamp DESC",
                Sale.class);
            query.setParameter("start", startDate);
            query.setParameter("end", endDate);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding sales by date range: " + e.getMessage(), e);
        }
    }

    /**
     * Find sales by customer.
     * @param customerId The customer ID
     * @return List of sales for the customer
     */
    public List<Sale> findByCustomerId(Long customerId) {
        try (Session session = getSession()) {
            Query<Sale> query = session.createQuery(
                "FROM Sale s WHERE s.customer.customerId = :customerId ORDER BY s.saleTimestamp DESC",
                Sale.class);
            query.setParameter("customerId", customerId);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding sales by customer: " + e.getMessage(), e);
        }
    }

    /**
     * Find sales by user (cashier).
     * @param userId The user ID
     * @return List of sales made by the user
     */
    public List<Sale> findByUserId(Long userId) {
        try (Session session = getSession()) {
            Query<Sale> query = session.createQuery(
                "FROM Sale s WHERE s.user.userId = :userId ORDER BY s.saleTimestamp DESC",
                Sale.class);
            query.setParameter("userId", userId);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding sales by user: " + e.getMessage(), e);
        }
    }

    /**
     * Get total sales amount for a date range.
     * @param startDate Start date
     * @param endDate End date
     * @return Total sales amount
     */
    public BigDecimal getTotalSalesAmount(LocalDateTime startDate, LocalDateTime endDate) {
        try (Session session = getSession()) {
            Query<BigDecimal> query = session.createQuery(
                "SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s " +
                "WHERE s.saleTimestamp BETWEEN :start AND :end", BigDecimal.class);
            query.setParameter("start", startDate);
            query.setParameter("end", endDate);
            return query.uniqueResult();
        } catch (Exception e) {
            throw new RuntimeException("Error getting total sales: " + e.getMessage(), e);
        }
    }

    /**
     * Get today's sales.
     * @return List of today's sales
     */
    public List<Sale> getTodaysSales() {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return findByDateRange(startOfDay, endOfDay);
    }
}
