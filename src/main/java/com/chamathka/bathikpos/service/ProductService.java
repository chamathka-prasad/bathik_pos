package com.chamathka.bathikpos.service;

import com.chamathka.bathikpos.dao.ProductDAO;
import com.chamathka.bathikpos.dao.ProductVariantDAO;
import com.chamathka.bathikpos.entity.Product;
import com.chamathka.bathikpos.entity.ProductVariant;
import com.chamathka.bathikpos.util.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Service class for Product and ProductVariant operations.
 * Handles both master products and their variants.
 */
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private final ProductDAO productDAO;
    private final ProductVariantDAO variantDAO;
    private final SessionManager sessionManager;

    public ProductService() {
        this.productDAO = new ProductDAO();
        this.variantDAO = new ProductVariantDAO();
        this.sessionManager = SessionManager.getInstance();
    }

    // ========== Product (Master) Operations ==========

    /**
     * Create a new master product.
     * @param product The product to create
     * @return The saved product
     */
    public Product createProduct(Product product) {
        sessionManager.requireAdmin();
        logger.info("Creating new product: {}", product.getName());
        return productDAO.save(product);
    }

    /**
     * Update an existing product.
     * @param product The product to update
     * @return The updated product
     */
    public Product updateProduct(Product product) {
        sessionManager.requireAdmin();
        logger.info("Updating product: {}", product.getName());
        return productDAO.update(product);
    }

    /**
     * Delete a product.
     * @param product The product to delete
     */
    public void deleteProduct(Product product) {
        sessionManager.requireAdmin();
        logger.info("Deleting product: {}", product.getName());
        productDAO.delete(product);
    }

    /**
     * Find a product by ID.
     * @param id The product ID
     * @return The product, or null if not found
     */
    public Product findProductById(Long id) {
        sessionManager.requireAdmin();
        return productDAO.findById(id).orElse(null);
    }

    /**
     * Get all products.
     * @return List of all products
     */
    public List<Product> getAllProducts() {
        sessionManager.requireAdmin();
        return productDAO.findAll();
    }

    /**
     * Get all product categories.
     * @return List of distinct categories
     */
    public List<String> getAllCategories() {
        sessionManager.requireAdmin();
        return productDAO.getAllCategories();
    }

    /**
     * Search products by name.
     * @param searchTerm The search term
     * @return List of matching products
     */
    public List<Product> searchProducts(String searchTerm) {
        sessionManager.requireAdmin();
        return productDAO.searchByName(searchTerm);
    }

    // ========== Product Variant Operations ==========

    /**
     * Create a new product variant.
     * @param variant The variant to create
     * @return The saved variant
     */
    public ProductVariant createVariant(ProductVariant variant) {
        sessionManager.requireAdmin();
        logger.info("Creating new variant: {} for product: {}",
            variant.getItemCode(), variant.getProduct().getName());
        return variantDAO.save(variant);
    }

    /**
     * Update an existing variant.
     * @param variant The variant to update
     * @return The updated variant
     */
    public ProductVariant updateVariant(ProductVariant variant) {
        sessionManager.requireAdmin();
        logger.info("Updating variant: {}", variant.getItemCode());
        return variantDAO.update(variant);
    }

    /**
     * Delete a variant.
     * @param variant The variant to delete
     */
    public void deleteVariant(ProductVariant variant) {
        sessionManager.requireAdmin();
        logger.info("Deleting variant: {}", variant.getItemCode());
        variantDAO.delete(variant);
    }

    /**
     * Find a variant by ID.
     * @param id The variant ID
     * @return The variant, or null if not found
     */
    public ProductVariant findVariantById(Long id) {
        return variantDAO.findById(id).orElse(null);
    }

    /**
     * Find a variant by item code (SKU).
     * @param itemCode The item code
     * @return The variant, or null if not found
     */
    public ProductVariant findVariantByItemCode(String itemCode) {
        return variantDAO.findByItemCode(itemCode).orElse(null);
    }

    /**
     * Get all variants for a product.
     * @param productId The product ID
     * @return List of variants
     */
    public List<ProductVariant> getVariantsByProductId(Long productId) {
        sessionManager.requireAdmin();
        return variantDAO.findByProductId(productId);
    }

    /**
     * Get all variants (all products).
     * @return List of all variants
     */
    public List<ProductVariant> getAllVariants() {
        return variantDAO.findAll();
    }

    /**
     * Get all variants with stock available.
     * @return List of in-stock variants
     */
    public List<ProductVariant> getInStockVariants() {
        return variantDAO.getInStockVariants();
    }

    /**
     * Get all variants with low stock.
     * @return List of low-stock variants
     */
    public List<ProductVariant> getLowStockVariants() {
        sessionManager.requireAdmin();
        return variantDAO.getLowStockVariants();
    }

    /**
     * Get all out-of-stock variants.
     * @return List of out-of-stock variants
     */
    public List<ProductVariant> getOutOfStockVariants() {
        sessionManager.requireAdmin();
        return variantDAO.getOutOfStockVariants();
    }

    /**
     * Search variants by item code or product name.
     * @param searchTerm The search term
     * @return List of matching variants
     */
    public List<ProductVariant> searchVariants(String searchTerm) {
        return variantDAO.searchVariants(searchTerm);
    }
}
