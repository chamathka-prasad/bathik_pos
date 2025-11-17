# Batik POS System v1.0

A comprehensive, standalone desktop Point of Sale system for Batik clothing stores, built with JavaFX and Hibernate.

## Table of Contents
- [Overview](#overview)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Setup Instructions](#setup-instructions)
- [Project Structure](#project-structure)
- [Database Schema](#database-schema)
- [Key Features](#key-features)
- [Default Credentials](#default-credentials)
- [Development Status](#development-status)
- [Next Steps](#next-steps)

## Overview

The Batik POS System is a feature-rich desktop application designed for single-location Batik clothing retail stores. It implements a strict 3-tier architecture with comprehensive business logic, atomic transactions for critical operations, and role-based access control.

### Key Capabilities
- **Role-Based Access**: ADMIN and CASHIER roles with different permissions
- **Inventory Management**: Master Products with multiple variants (size/color)
- **Goods Received Note (GRN)**: Track stock intake from suppliers with cost prices
- **Point of Sale**: Full POS functionality with stock validation
- **Customer Management**: CRM with purchase history tracking
- **Returns Processing**: Handle customer returns with stock restoration
- **Reporting**: Sales, inventory, and profit reports

## Architecture

The system follows a strict **3-Tier Architecture**:

### 1. Presentation Layer (View)
- **Technology**: JavaFX with FXML
- **Location**: `src/main/resources/fxml/`
- **Controllers**: `src/main/java/com/chamathka/bathikpos/controller/`
- **Purpose**: Display UI and capture user input only (NO business logic)

### 2. Business Logic Layer (Service)
- **Location**: `src/main/java/com/chamathka/bathikpos/service/`
- **Purpose**:
  - Orchestrate all business operations
  - Enforce role-based security
  - Implement atomic transactions for critical operations
  - Coordinate between multiple DAOs

**Key Services**:
- `AuthenticationService` - Login/logout and session management
- `SaleService` - POS operations with atomic checkout transaction
- `GRNService` - Stock intake with atomic confirmation transaction
- `ReturnService` - Returns processing with atomic stock restoration

### 3. Data Access Layer (DAO)
- **Location**: `src/main/java/com/chamathka/bathikpos/dao/`
- **Purpose**: All database CRUD operations using Hibernate
- **Pattern**: BaseDAO with entity-specific extensions

## Prerequisites

1. **Java Development Kit (JDK) 17** or higher
2. **Maven 3.6+** for build management
3. **MySQL 8.0+** for the database
4. **IDE**: IntelliJ IDEA, Eclipse, or VS Code with Java extensions

## Setup Instructions

### 1. Clone the Repository
```bash
git clone <repository-url>
cd bathik_pos
```

### 2. Configure Database

#### Option A: Update Hibernate Configuration
Edit `src/main/resources/hibernate.cfg.xml` and update these properties:
```xml
<property name="hibernate.connection.url">jdbc:mysql://localhost:3306/bathik_pos?createDatabaseIfNotExist=true</property>
<property name="hibernate.connection.username">YOUR_MYSQL_USERNAME</property>
<property name="hibernate.connection.password">YOUR_MYSQL_PASSWORD</property>
```

#### Option B: Use the SQL Schema (Optional)
If you prefer to create the database manually:
```bash
mysql -u root -p < database/schema.sql
```

### 3. Build the Project
```bash
mvn clean install
```

### 4. Initialize Sample Data
Run the data initializer to populate the database with sample data:
```bash
mvn exec:java -Dexec.mainClass="com.chamathka.bathikpos.util.DataInitializer"
```

This will create:
- 2 users (admin and cashier)
- 2 suppliers
- 3 customers
- 3 master products with 9 variants (all with 0 stock initially)

### 5. Run the Application
```bash
mvn javafx:run
```

## Project Structure

```
bathik_pos/
├── src/main/java/com/chamathka/bathikpos/
│   ├── entity/              # Hibernate entity classes (9 entities)
│   │   ├── User.java
│   │   ├── Customer.java
│   │   ├── Supplier.java
│   │   ├── Product.java
│   │   ├── ProductVariant.java
│   │   ├── GRN.java
│   │   ├── GRNItem.java
│   │   ├── Sale.java
│   │   └── SaleItem.java
│   ├── dao/                 # Data Access Layer
│   │   ├── BaseDAO.java
│   │   ├── UserDAO.java
│   │   ├── CustomerDAO.java
│   │   ├── SupplierDAO.java
│   │   ├── ProductDAO.java
│   │   ├── ProductVariantDAO.java
│   │   ├── GRNDAO.java
│   │   ├── GRNItemDAO.java
│   │   ├── SaleDAO.java
│   │   └── SaleItemDAO.java
│   ├── service/             # Business Logic Layer
│   │   ├── AuthenticationService.java
│   │   ├── SaleService.java
│   │   ├── GRNService.java
│   │   └── ReturnService.java
│   ├── controller/          # JavaFX Controllers (TO BE CREATED)
│   ├── util/                # Utility classes
│   │   ├── HibernateUtil.java
│   │   ├── SessionManager.java
│   │   ├── PasswordUtil.java
│   │   └── DataInitializer.java
│   └── BatikPOSApplication.java
├── src/main/resources/
│   ├── fxml/                # FXML view files (TO BE CREATED)
│   ├── css/                 # Stylesheets (TO BE CREATED)
│   └── hibernate.cfg.xml
├── database/
│   └── schema.sql           # Database schema
└── pom.xml
```

## Database Schema

The system uses 9 interconnected tables:

1. **User** - System users (Admin/Cashier)
2. **Customer** - Customer information and purchase history
3. **Supplier** - Supplier details
4. **Product** - Master products (containers for variants)
5. **ProductVariant** - Actual sellable items with stock
6. **GRN** - Goods Received Note header
7. **GRNItem** - GRN line items (records cost prices)
8. **Sale** - Sales transaction header
9. **SaleItem** - Sales line items (records selling prices)

### Critical Data Flow

**Stock Intake (GRN):**
```
GRN (PENDING) → Confirm GRN → GRNItems saved with costPrice
                             → Stock added to ProductVariant.quantityInStock
```

**Sales (POS):**
```
Add to Cart (stock check) → Checkout → Sale + SaleItems saved with priceAtSale
                                     → Stock deducted from ProductVariant.quantityInStock
                                     → Customer stats updated
```

**Returns:**
```
Find Sale by ID → Select Items → Process Return → Stock added back to ProductVariant.quantityInStock
```

## Key Features

### ✅ Implemented (Core Foundation)

1. **Strict 3-Tier Architecture**
   - Complete separation of concerns
   - Entity → DAO → Service → Controller flow

2. **All 9 Hibernate Entities**
   - Proper JPA annotations
   - Bidirectional relationships
   - Utility methods for business logic

3. **Complete DAO Layer**
   - BaseDAO with common CRUD operations
   - Entity-specific query methods
   - Optimized queries with proper indexing

4. **Critical Service Layer**
   - **AuthenticationService**: BCrypt password hashing, session management
   - **SaleService**: ATOMIC checkout transaction (UC-01)
   - **GRNService**: ATOMIC GRN confirmation transaction (UC-02)
   - **ReturnService**: ATOMIC return processing transaction (UC-04)

5. **Security & Session Management**
   - Role-based access control enforced at service layer
   - SessionManager with security methods (`requireAdmin()`, `requireAuthentication()`)
   - BCrypt password hashing

6. **Atomic Transactions**
   - Checkout: Save sale + deduct stock + update customer (all or nothing)
   - GRN Confirm: Save GRN + add stock + record cost prices (all or nothing)
   - Returns: Restore stock atomically

### 🚧 To Be Implemented (UI Layer)

The backend is complete. What remains is the presentation layer:

1. **Login View** (Phase 2)
   - FXML: `LoginView.fxml`
   - Controller: `LoginController.java`
   - Call: `AuthenticationService.login()`

2. **Main Dashboard** (Phase 2)
   - Role-based menu (Admin vs Cashier)
   - Navigation to all modules

3. **Supplier Management** (Phase 3)
   - CRUD operations
   - Service: `SupplierService` (needs to be created)

4. **Product/Variant Management** (Phase 3)
   - Two-step process: Master Product → Manage Variants
   - Service: `ProductService` (needs to be created)

5. **GRN Module** (Phase 3)
   - Create GRN with items
   - Confirm button calls: `GRNService.confirmGRN()`

6. **POS Module** (Phase 4)
   - 3-panel layout: Product Grid | Current Bill | Checkout
   - Add to cart calls: `SaleService.checkStockAvailability()`
   - Checkout calls: `SaleService.processCheckout()`

7. **Customer Management** (Phase 5)
   - CRUD operations
   - View purchase history
   - Service: `CustomerService` (needs to be created)

8. **Reports** (Phase 5)
   - Low Stock Report
   - Sales Report
   - Profit Report (joins SaleItem and GRNItem)
   - Service: `ReportService` (needs to be created)

9. **Returns Processing** (Phase 5)
   - Find sale by receipt ID
   - Select items to return
   - Process calls: `ReturnService.processReturn()`

## Default Credentials

After running `DataInitializer`:

| Username | Password     | Role    |
|----------|--------------|---------|
| admin    | admin123     | ADMIN   |
| cashier  | cashier123   | CASHIER |

**IMPORTANT**: Change these passwords in production!

## Development Status

### ✅ Phase 1: Foundation (COMPLETE)
- [x] Maven project with all dependencies
- [x] Database schema
- [x] Hibernate configuration
- [x] All 9 entity classes
- [x] Complete DAO layer
- [x] Core service layer with atomic transactions
- [x] Utility classes (Hibernate, Session, Password)
- [x] Data initializer

### ✅ Phase 2: Authentication & Dashboard (COMPLETE)
- [x] Login view and controller with async authentication
- [x] Main dashboard with role-based menu
- [x] Navigation framework
- [x] Additional services (Supplier, Product, Customer)
- [x] Dashboard statistics for Admin users
- [x] Access control enforcement

### 🚧 Phase 3: Inventory & Stock-In (PENDING)
- [ ] Supplier management UI
- [ ] Product management UI
- [ ] Variant management UI
- [ ] GRN module UI
- [ ] ProductService, SupplierService

### 🚧 Phase 4: Point of Sale (PENDING)
- [ ] POS UI (3-panel layout)
- [ ] Add to cart with stock validation
- [ ] Checkout UI
- [ ] Receipt printing

### 🚧 Phase 5: Reporting & Returns (PENDING)
- [ ] Customer management UI
- [ ] Reports UI
- [ ] Return processing UI
- [ ] ReportService, CustomerService

## Next Steps

### For Immediate Testing (Backend Only)

You can test the backend without UI using unit tests or a simple main method:

```java
public class BackendTest {
    public static void main(String[] args) {
        // Test authentication
        AuthenticationService authService = new AuthenticationService();
        User user = authService.login("admin", "admin123");
        System.out.println("Logged in: " + user.getUsername());

        // Test creating a GRN (you'll need to create entities manually)
        // Test creating a sale
        // etc.
    }
}
```

### For Full Application Development

1. **Create FXML Views**: Start with `LoginView.fxml`
2. **Create Controllers**: Start with `LoginController.java`
3. **Create Additional Services**: `ProductService`, `CustomerService`, `SupplierService`, `ReportService`
4. **Integrate UI with Services**: Controllers call service methods
5. **Add Error Handling**: Show alerts for exceptions
6. **Test Critical Paths**: Especially atomic transactions

## Architecture Compliance Checklist

- ✅ **No business logic in controllers**: Controllers only call service methods
- ✅ **Security at service layer**: All services check roles before operations
- ✅ **Atomic transactions**: Critical operations (checkout, GRN, returns) are atomic
- ✅ **Cost price tracking**: GRNItem records costPrice for profit calculation
- ✅ **Price snapshot**: SaleItem records priceAtSale for historical accuracy
- ✅ **Stock validation**: POS checks stock before adding to cart
- ✅ **Password hashing**: BCrypt with cost factor 12

## Technologies Used

- **Java 17**: Programming language
- **JavaFX 17**: UI framework
- **Hibernate 6.4**: ORM framework
- **MySQL 8**: Database
- **Maven**: Build tool
- **BCrypt**: Password hashing
- **SLF4J + Logback**: Logging
- **JasperReports**: Receipt/report printing (configured)
- **HikariCP**: Connection pooling

## License

Copyright © 2025 Batik POS System. All rights reserved.

## Support

For issues or questions, please create an issue in the repository.

---

**Built with ❤️ following the comprehensive SRS specification**
