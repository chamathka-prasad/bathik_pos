package com.chamathka.bathikpos.controller;

import com.chamathka.bathikpos.BatikPOSApplication;
import com.chamathka.bathikpos.dao.CustomerDAO;
import com.chamathka.bathikpos.dao.ProductVariantDAO;
import com.chamathka.bathikpos.util.SessionManager;
import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Controller for the Main Dashboard.
 * Implements role-based UI - shows different menus for ADMIN vs CASHIER.
 *
 * As per SRS: Admin has full access, Cashier has restricted access.
 */
public class MainDashboardController {

    private static final Logger logger = LoggerFactory.getLogger(MainDashboardController.class);

    // Header
    @FXML private Label userInfoLabel;
    @FXML private Label roleLabel;

    // Welcome Screen
    @FXML private Label welcomeMessage;
    @FXML private VBox welcomeScreen;

    // Admin-only elements
    @FXML private VBox adminSection;
    @FXML private HBox statsContainer;
    @FXML private Label lblTotalProducts;
    @FXML private Label lblLowStock;
    @FXML private Label lblTotalCustomers;

    // Navigation buttons
    @FXML private JFXButton btnPOS;
    @FXML private JFXButton btnCustomers;
    @FXML private JFXButton btnProducts;
    @FXML private JFXButton btnSuppliers;
    @FXML private JFXButton btnGRN;
    @FXML private JFXButton btnReports;
    @FXML private JFXButton btnReturns;

    private final SessionManager sessionManager;
    private final ProductVariantDAO variantDAO;
    private final CustomerDAO customerDAO;

    public MainDashboardController() {
        this.sessionManager = SessionManager.getInstance();
        this.variantDAO = new ProductVariantDAO();
        this.customerDAO = new CustomerDAO();
    }

    @FXML
    private void initialize() {
        logger.info("Main Dashboard initialized");

        // Display user information
        String username = sessionManager.getCurrentUsername();
        String role = sessionManager.getCurrentUserRole();
        userInfoLabel.setText("Welcome, " + username);
        roleLabel.setText(role);
        welcomeMessage.setText("You are logged in as " + role);

        // Show/hide sections based on role
        boolean isAdmin = sessionManager.isAdmin();
        adminSection.setVisible(isAdmin);
        adminSection.setManaged(isAdmin);
        statsContainer.setVisible(isAdmin);
        statsContainer.setManaged(isAdmin);

        logger.info("Dashboard loaded for user: {} ({})", username, role);

        // Load dashboard statistics if admin
        if (isAdmin) {
            loadDashboardStats();
        }

        // Apply button hover effects
        setupButtonHoverEffects();
    }

    /**
     * Load dashboard statistics (Admin only).
     */
    private void loadDashboardStats() {
        Task<Void> statsTask = new Task<>() {
            private long totalProducts;
            private long lowStockCount;
            private long totalCustomers;

            @Override
            protected Void call() {
                totalProducts = variantDAO.count();
                lowStockCount = variantDAO.getLowStockVariants().size();
                totalCustomers = customerDAO.count();
                return null;
            }

            @Override
            protected void succeeded() {
                lblTotalProducts.setText(String.valueOf(totalProducts));
                lblLowStock.setText(String.valueOf(lowStockCount));
                lblTotalCustomers.setText(String.valueOf(totalCustomers));
                logger.debug("Dashboard stats loaded: Products={}, LowStock={}, Customers={}",
                    totalProducts, lowStockCount, totalCustomers);
            }

            @Override
            protected void failed() {
                logger.error("Failed to load dashboard stats", getException());
            }
        };

        new Thread(statsTask).start();
    }

    /**
     * Setup hover effects for navigation buttons.
     */
    private void setupButtonHoverEffects() {
        JFXButton[] buttons = {btnPOS, btnCustomers, btnProducts, btnSuppliers,
                              btnGRN, btnReports, btnReturns};

        for (JFXButton button : buttons) {
            if (button != null) {
                button.setOnMouseEntered(e ->
                    button.setStyle("-fx-background-color: #e0e7ff; -fx-text-fill: #4338ca; -fx-background-radius: 5;"));
                button.setOnMouseExited(e ->
                    button.setStyle("-fx-background-color: transparent; -fx-text-fill: #1f2937;"));
            }
        }
    }

    // ========== Navigation Handlers ==========

    @FXML
    private void handleOpenPOS() {
        logger.info("Opening POS module");
        try {
            BatikPOSApplication.navigateTo("/fxml/POSView.fxml", "Batik POS - Point of Sale");
        } catch (Exception e) {
            logger.error("Failed to open POS", e);
            showError("Failed to open POS", "Could not load POS module: " + e.getMessage());
        }
    }

    @FXML
    private void handleOpenCustomers() {
        logger.info("Opening Customer Management");
        try {
            BatikPOSApplication.navigateTo("/fxml/CustomerManagement.fxml", "Batik POS - Customers");
        } catch (Exception e) {
            logger.error("Failed to open Customer Management", e);
            showError("Failed to open Customers", "Could not load Customer Management: " + e.getMessage());
        }
    }

    @FXML
    private void handleOpenProducts() {
        if (!sessionManager.isAdmin()) {
            showAccessDenied();
            return;
        }
        logger.info("Opening Product Management");
        try {
            BatikPOSApplication.navigateTo("/fxml/ProductManagement.fxml", "Batik POS - Products");
        } catch (Exception e) {
            logger.error("Failed to open Product Management", e);
            showError("Failed to open Products", "Could not load Product Management: " + e.getMessage());
        }
    }

    @FXML
    private void handleOpenSuppliers() {
        if (!sessionManager.isAdmin()) {
            showAccessDenied();
            return;
        }
        logger.info("Opening Supplier Management");
        try {
            BatikPOSApplication.navigateTo("/fxml/SupplierManagement.fxml", "Batik POS - Suppliers");
        } catch (Exception e) {
            logger.error("Failed to open Supplier Management", e);
            showError("Failed to open Suppliers", "Could not load Supplier Management: " + e.getMessage());
        }
    }

    @FXML
    private void handleOpenGRN() {
        if (!sessionManager.isAdmin()) {
            showAccessDenied();
            return;
        }
        logger.info("Opening GRN Module");
        try {
            BatikPOSApplication.navigateTo("/fxml/GRNManagement.fxml", "Batik POS - Goods Received Note");
        } catch (Exception e) {
            logger.error("Failed to open GRN", e);
            showError("Failed to open GRN", "Could not load GRN module: " + e.getMessage());
        }
    }

    @FXML
    private void handleOpenReports() {
        if (!sessionManager.isAdmin()) {
            showAccessDenied();
            return;
        }
        logger.info("Opening Reports");
        try {
            BatikPOSApplication.navigateTo("/fxml/ReportsView.fxml", "Batik POS - Reports");
        } catch (Exception e) {
            logger.error("Failed to open Reports", e);
            showError("Failed to open Reports", "Could not load Reports module: " + e.getMessage());
        }
    }

    @FXML
    private void handleOpenReturns() {
        if (!sessionManager.isAdmin()) {
            showAccessDenied();
            return;
        }
        logger.info("Opening Returns Processing");
        try {
            BatikPOSApplication.navigateTo("/fxml/ReturnProcessing.fxml", "Batik POS - Process Returns");
        } catch (Exception e) {
            logger.error("Failed to open Returns", e);
            showError("Failed to open Returns", "Could not load Returns module: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        logger.info("Logout requested");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.setContentText("You will be returned to the login screen.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String username = sessionManager.getCurrentUsername();
            sessionManager.logout();
            logger.info("User logged out: {}", username);

            // Navigate back to login
            try {
                BatikPOSApplication.navigateTo("/fxml/LoginView.fxml", "Batik POS - Login");
            } catch (Exception e) {
                logger.error("Failed to navigate to login", e);
                showError("Logout Error", "Failed to return to login screen: " + e.getMessage());
            }
        }
    }

    // ========== Utility Methods ==========

    private void showAccessDenied() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Access Denied");
        alert.setHeaderText("Admin Access Required");
        alert.setContentText("You do not have permission to access this module.\n" +
                           "Only administrators can access this feature.");
        alert.showAndWait();
        logger.warn("Access denied for user: {}", sessionManager.getCurrentUsername());
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("An error occurred");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
