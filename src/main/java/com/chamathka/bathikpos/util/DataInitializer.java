package com.chamathka.bathikpos.util;

import com.chamathka.bathikpos.dao.*;
import com.chamathka.bathikpos.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * Utility class to initialize sample data for the Batik POS System.
 * This should be run once on first startup to populate the database with initial data.
 */
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    public static void initializeData() {
        logger.info("Starting data initialization...");

        try {
            // Create DAOs
            UserDAO userDAO = new UserDAO();
            SupplierDAO supplierDAO = new SupplierDAO();
            CustomerDAO customerDAO = new CustomerDAO();
            ProductDAO productDAO = new ProductDAO();
            ProductVariantDAO variantDAO = new ProductVariantDAO();

            // Check if data already exists
            if (userDAO.count() > 0) {
                logger.info("Data already exists. Skipping initialization.");
                return;
            }

            logger.info("Creating users...");
            // Create default users
            User admin = new User("admin", PasswordUtil.hashPassword("admin123"), "ADMIN");
            User cashier = new User("cashier", PasswordUtil.hashPassword("cashier123"), "CASHIER");
            userDAO.save(admin);
            userDAO.save(cashier);
            logger.info("Created users: admin and cashier");

            logger.info("Creating suppliers...");
            // Create sample suppliers
            Supplier supplier1 = new Supplier("Lanka Batik Suppliers", "Nimal Perera", "0771234567",
                "123 Galle Road, Colombo");
            Supplier supplier2 = new Supplier("Ceylon Textiles Ltd", "Kamala Silva", "0777654321",
                "456 Kandy Road, Kandy");
            supplierDAO.save(supplier1);
            supplierDAO.save(supplier2);
            logger.info("Created {} suppliers", 2);

            logger.info("Creating customers...");
            // Create sample customers
            Customer customer1 = new Customer("Saman Kumara", "0771111111", "saman@email.com");
            Customer customer2 = new Customer("Nisha Fernando", "0772222222", "nisha@email.com");
            Customer customer3 = new Customer("Rajesh Patel", "0773333333", null);
            customerDAO.save(customer1);
            customerDAO.save(customer2);
            customerDAO.save(customer3);
            logger.info("Created {} customers", 3);

            logger.info("Creating products and variants...");
            // Create sample products (Master Products)
            Product shirtProduct = new Product("Batik Shirt", "Shirts", supplier1);
            Product sareeProduct = new Product("Batik Saree", "Sarees", supplier1);
            Product scarfProduct = new Product("Batik Scarf", "Accessories", supplier2);
            productDAO.save(shirtProduct);
            productDAO.save(sareeProduct);
            productDAO.save(scarfProduct);

            // Create product variants (actual sellable items with 0 initial stock)
            // Batik Shirts
            ProductVariant shirtRedM = new ProductVariant(shirtProduct, "SHIRT-RED-M",
                "M", "Red", new BigDecimal("2500.00"));
            ProductVariant shirtRedL = new ProductVariant(shirtProduct, "SHIRT-RED-L",
                "L", "Red", new BigDecimal("2500.00"));
            ProductVariant shirtBlueM = new ProductVariant(shirtProduct, "SHIRT-BLUE-M",
                "M", "Blue", new BigDecimal("2500.00"));
            ProductVariant shirtBlueL = new ProductVariant(shirtProduct, "SHIRT-BLUE-L",
                "L", "Blue", new BigDecimal("2500.00"));

            // Batik Sarees
            ProductVariant sareeRed = new ProductVariant(sareeProduct, "SAREE-RED-STD",
                "Standard", "Red", new BigDecimal("5500.00"));
            ProductVariant sareeBlue = new ProductVariant(sareeProduct, "SAREE-BLUE-STD",
                "Standard", "Blue", new BigDecimal("5500.00"));
            ProductVariant sareeGreen = new ProductVariant(sareeProduct, "SAREE-GREEN-STD",
                "Standard", "Green", new BigDecimal("6000.00"));

            // Batik Scarfs
            ProductVariant scarfMulti = new ProductVariant(scarfProduct, "SCARF-MULTI-STD",
                "Standard", "Multicolor", new BigDecimal("800.00"));
            ProductVariant scarfBlack = new ProductVariant(scarfProduct, "SCARF-BLACK-STD",
                "Standard", "Black", new BigDecimal("750.00"));

            // Save all variants
            variantDAO.save(shirtRedM);
            variantDAO.save(shirtRedL);
            variantDAO.save(shirtBlueM);
            variantDAO.save(shirtBlueL);
            variantDAO.save(sareeRed);
            variantDAO.save(sareeBlue);
            variantDAO.save(sareeGreen);
            variantDAO.save(scarfMulti);
            variantDAO.save(scarfBlack);

            logger.info("Created {} products with {} variants", 3, 9);

            logger.info("Data initialization completed successfully!");
            logger.info("Default credentials:");
            logger.info("  Admin    - username: admin,    password: admin123");
            logger.info("  Cashier  - username: cashier,  password: cashier123");
            logger.info("Note: All product variants have 0 stock. Use GRN module to add stock.");

        } catch (Exception e) {
            logger.error("Data initialization failed", e);
            throw new RuntimeException("Data initialization failed: " + e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        // Run this main method once to initialize data
        logger.info("=== Batik POS Data Initializer ===");
        initializeData();
        HibernateUtil.shutdown();
    }
}
