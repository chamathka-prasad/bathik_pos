package com.chamathka.bathikpos.dao;

import com.chamathka.bathikpos.entity.GRN;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DAO class for GRN (Goods Received Note) entity operations.
 */
public class GRNDAO extends BaseDAO<GRN, Long> {

    public GRNDAO() {
        super(GRN.class);
    }

    /**
     * Find GRNs by status.
     * @param status The status ("PENDING" or "CONFIRMED")
     * @return List of GRNs with the specified status
     */
    public List<GRN> findByStatus(String status) {
        try (Session session = getSession()) {
            Query<GRN> query = session.createQuery(
                "FROM GRN WHERE status = :status ORDER BY grnTimestamp DESC", GRN.class);
            query.setParameter("status", status);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding GRNs by status: " + e.getMessage(), e);
        }
    }

    /**
     * Find GRNs by supplier.
     * @param supplierId The supplier ID
     * @return List of GRNs from the supplier
     */
    public List<GRN> findBySupplierId(Long supplierId) {
        try (Session session = getSession()) {
            Query<GRN> query = session.createQuery(
                "FROM GRN g WHERE g.supplier.supplierId = :supplierId ORDER BY g.grnTimestamp DESC",
                GRN.class);
            query.setParameter("supplierId", supplierId);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding GRNs by supplier: " + e.getMessage(), e);
        }
    }

    /**
     * Find GRNs within a date range.
     * @param startDate Start date
     * @param endDate End date
     * @return List of GRNs in the date range
     */
    public List<GRN> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        try (Session session = getSession()) {
            Query<GRN> query = session.createQuery(
                "FROM GRN WHERE grnTimestamp BETWEEN :start AND :end ORDER BY grnTimestamp DESC",
                GRN.class);
            query.setParameter("start", startDate);
            query.setParameter("end", endDate);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding GRNs by date range: " + e.getMessage(), e);
        }
    }
}
