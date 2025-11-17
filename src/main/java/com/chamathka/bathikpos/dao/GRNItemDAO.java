package com.chamathka.bathikpos.dao;

import com.chamathka.bathikpos.entity.GRNItem;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

/**
 * DAO class for GRNItem entity operations.
 */
public class GRNItemDAO extends BaseDAO<GRNItem, Long> {

    public GRNItemDAO() {
        super(GRNItem.class);
    }

    /**
     * Find all items for a specific GRN.
     * @param grnId The GRN ID
     * @return List of GRN items
     */
    public List<GRNItem> findByGrnId(Long grnId) {
        try (Session session = getSession()) {
            Query<GRNItem> query = session.createQuery(
                "FROM GRNItem gi WHERE gi.grn.grnId = :grnId", GRNItem.class);
            query.setParameter("grnId", grnId);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding GRN items: " + e.getMessage(), e);
        }
    }
}
