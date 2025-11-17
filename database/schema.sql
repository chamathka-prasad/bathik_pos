-- ============================================================
-- Batik POS System - Database Schema (v1.0)
-- ============================================================
-- This script creates all 9 tables required by the SRS
-- Database: MySQL 8.0+
-- ============================================================

-- Drop existing tables (in reverse order of dependencies)
DROP TABLE IF EXISTS SaleItem;
DROP TABLE IF EXISTS Sale;
DROP TABLE IF EXISTS GRNItem;
DROP TABLE IF EXISTS GRN;
DROP TABLE IF EXISTS ProductVariant;
DROP TABLE IF EXISTS Product;
DROP TABLE IF EXISTS Supplier;
DROP TABLE IF EXISTS Customer;
DROP TABLE IF EXISTS User;

-- ============================================================
-- Table 1: User
-- ============================================================
CREATE TABLE User (
    userId BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    passwordHash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- Table 2: Customer
-- ============================================================
CREATE TABLE Customer (
    customerId BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phoneNumber VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100),
    totalPurchases DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    visitCount INT NOT NULL DEFAULT 0,
    INDEX idx_phone (phoneNumber)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- Table 3: Supplier
-- ============================================================
CREATE TABLE Supplier (
    supplierId BIGINT AUTO_INCREMENT PRIMARY KEY,
    supplierName VARCHAR(100) NOT NULL,
    contactPerson VARCHAR(100),
    phone VARCHAR(20),
    address VARCHAR(255),
    INDEX idx_supplier_name (supplierName)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- Table 4: Product (Master)
-- ============================================================
CREATE TABLE Product (
    productId BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    supplier_id BIGINT,
    FOREIGN KEY (supplier_id) REFERENCES Supplier(supplierId) ON DELETE SET NULL,
    INDEX idx_category (category),
    INDEX idx_supplier (supplier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- Table 5: ProductVariant (Stocked Item)
-- ============================================================
CREATE TABLE ProductVariant (
    variantId BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    itemCode VARCHAR(50) NOT NULL UNIQUE,
    attribute_Size VARCHAR(50),
    attribute_Color VARCHAR(50),
    sellingPrice DECIMAL(10,2) NOT NULL,
    quantityInStock INT NOT NULL DEFAULT 0,
    lowStockThreshold INT NOT NULL DEFAULT 5,
    FOREIGN KEY (product_id) REFERENCES Product(productId) ON DELETE CASCADE,
    INDEX idx_item_code (itemCode),
    INDEX idx_product (product_id),
    INDEX idx_stock (quantityInStock)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- Table 6: GRN (Goods Received Note)
-- ============================================================
CREATE TABLE GRN (
    grnId BIGINT AUTO_INCREMENT PRIMARY KEY,
    supplier_id BIGINT NOT NULL,
    grnTimestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    supplierInvoiceNo VARCHAR(50),
    totalCost DECIMAL(10,2) NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    FOREIGN KEY (supplier_id) REFERENCES Supplier(supplierId) ON DELETE RESTRICT,
    FOREIGN KEY (user_id) REFERENCES User(userId) ON DELETE RESTRICT,
    INDEX idx_supplier (supplier_id),
    INDEX idx_timestamp (grnTimestamp),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- Table 7: GRNItem
-- ============================================================
CREATE TABLE GRNItem (
    grnItemId BIGINT AUTO_INCREMENT PRIMARY KEY,
    grn_id BIGINT NOT NULL,
    variant_id BIGINT NOT NULL,
    quantityReceived INT NOT NULL,
    costPrice DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (grn_id) REFERENCES GRN(grnId) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES ProductVariant(variantId) ON DELETE RESTRICT,
    INDEX idx_grn (grn_id),
    INDEX idx_variant (variant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- Table 8: Sale (Transaction)
-- ============================================================
CREATE TABLE Sale (
    saleId BIGINT AUTO_INCREMENT PRIMARY KEY,
    saleTimestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT NOT NULL,
    customer_id BIGINT,
    totalAmount DECIMAL(10,2) NOT NULL,
    discountAmount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    paymentType VARCHAR(50) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES User(userId) ON DELETE RESTRICT,
    FOREIGN KEY (customer_id) REFERENCES Customer(customerId) ON DELETE SET NULL,
    INDEX idx_timestamp (saleTimestamp),
    INDEX idx_user (user_id),
    INDEX idx_customer (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- Table 9: SaleItem
-- ============================================================
CREATE TABLE SaleItem (
    saleItemId BIGINT AUTO_INCREMENT PRIMARY KEY,
    sale_id BIGINT NOT NULL,
    variant_id BIGINT NOT NULL,
    quantitySold INT NOT NULL,
    priceAtSale DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (sale_id) REFERENCES Sale(saleId) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES ProductVariant(variantId) ON DELETE RESTRICT,
    INDEX idx_sale (sale_id),
    INDEX idx_variant (variant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- Initial Data: Create Default Admin User
-- ============================================================
-- Password: "admin123" (hashed with BCrypt)
INSERT INTO User (username, passwordHash, role) VALUES
('admin', '$2a$10$8K1p/a0dL3.qY5YY7g4nDuF3E3b0N7x8YtY5h7K9qW5zJ8c3R7W7e', 'ADMIN'),
('cashier1', '$2a$10$8K1p/a0dL3.qY5YY7g4nDuF3E3b0N7x8YtY5h7K9qW5zJ8c3R7W7e', 'CASHIER');

-- Note: Both default users have password "admin123" for initial setup
-- IMPORTANT: Change these passwords immediately in production!
