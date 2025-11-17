package com.chamathka.bathikpos.dao;

import com.chamathka.bathikpos.entity.Product;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

/**
 * DAO class for Product (Master Product) entity operations.
 */
public class ProductDAO extends BaseDAO<Product, Long> {

    public ProductDAO() {
        super(Product.class);
    }

    /**
     * Find products by category.
     * @param category The category to filter by
     * @return List of products in the category
     */
    public List<Product> findByCategory(String category) {
        try (Session session = getSession()) {
            Query<Product> query = session.createQuery(
                "FROM Product WHERE category = :category ORDER BY name", Product.class);
            query.setParameter("category", category);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding products by category: " + e.getMessage(), e);
        }
    }

    /**
     * Get all distinct categories.
     * @return List of category names
     */
    public List<String> getAllCategories() {
        try (Session session = getSession()) {
            Query<String> query = session.createQuery(
                "SELECT DISTINCT p.category FROM Product p ORDER BY p.category", String.class);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error getting categories: " + e.getMessage(), e);
        }
    }

    /**
     * Search products by name (partial match).
     * @param searchTerm The search term
     * @return List of matching products
     */
    public List<Product> searchByName(String searchTerm) {
        try (Session session = getSession()) {
            Query<Product> query = session.createQuery(
                "FROM Product WHERE LOWER(name) LIKE LOWER(:term) ORDER BY name", Product.class);
            query.setParameter("term", "%" + searchTerm + "%");
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error searching products: " + e.getMessage(), e);
        }
    }
}
