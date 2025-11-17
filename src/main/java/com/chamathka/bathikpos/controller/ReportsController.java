package com.chamathka.bathikpos.controller;

import com.chamathka.bathikpos.BatikPOSApplication;
import com.chamathka.bathikpos.service.ReportService;
import com.chamathka.bathikpos.service.ReportService.*;
import com.chamathka.bathikpos.util.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Controller for Reports & Analytics.
 * Provides Low Stock, Sales, Profit, and Customer reports.
 */
public class ReportsController {

    private static final Logger logger = LoggerFactory.getLogger(ReportsController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML private TabPane reportsTabPane;
    @FXML private Tab profitReportTab;

    // Low Stock Report
    @FXML private TableView<LowStockReportItem> lowStockTable;
    @FXML private Label lowStockCountLabel;

    // Sales Report
    @FXML private DatePicker salesStartDate;
    @FXML private DatePicker salesEndDate;
    @FXML private Label salesReportStatus;
    @FXML private Label totalSalesLabel;
    @FXML private Label totalTransactionsLabel;
    @FXML private Label totalDiscountLabel;
    @FXML private Label avgTransactionLabel;
    @FXML private TableView<SalesUserSummary> salesByUserTable;
    @FXML private TableView<SalesCustomerSummary> salesByCustomerTable;
    @FXML private TableView<PaymentTypeSummary> salesByPaymentTable;

    // Profit Report
    @FXML private DatePicker profitStartDate;
    @FXML private DatePicker profitEndDate;
    @FXML private Label profitReportStatus;
    @FXML private Label totalRevenueLabel;
    @FXML private Label totalCostLabel;
    @FXML private Label totalProfitLabel;
    @FXML private Label profitMarginLabel;
    @FXML private TableView<ProfitReportItem> profitDetailsTable;

    // Top Customers
    @FXML private TableView<TopCustomerWithRank> topCustomersTable;

    private final ReportService reportService;
    private final SessionManager sessionManager;

    public ReportsController() {
        this.reportService = new ReportService();
        this.sessionManager = SessionManager.getInstance();
    }

    @FXML
    private void initialize() {
        logger.info("Reports view initialized");

        // Hide profit report tab for non-admin users
        if (!sessionManager.isAdmin()) {
            reportsTabPane.getTabs().remove(profitReportTab);
            logger.info("Profit report tab hidden for non-admin user");
        }

        // Initialize date pickers with default values (last 30 days)
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);

        salesStartDate.setValue(thirtyDaysAgo);
        salesEndDate.setValue(today);
        profitStartDate.setValue(thirtyDaysAgo);
        profitEndDate.setValue(today);

        // Load initial data
        loadLowStockReport();
        loadTopCustomersReport();
    }

    // ==================== LOW STOCK REPORT ====================

    @FXML
    private void handleRefreshLowStock() {
        loadLowStockReport();
    }

    private void loadLowStockReport() {
        Task<List<LowStockReportItem>> loadTask = new Task<>() {
            @Override
            protected List<LowStockReportItem> call() {
                return reportService.getLowStockReport();
            }
        };

        loadTask.setOnSucceeded(e -> {
            List<LowStockReportItem> items = loadTask.getValue();
            lowStockTable.setItems(FXCollections.observableArrayList(items));
            lowStockCountLabel.setText(String.valueOf(items.size()));
            logger.info("Loaded {} low stock items", items.size());
        });

        loadTask.setOnFailed(e -> {
            logger.error("Failed to load low stock report", loadTask.getException());
            showError("Report Error", "Failed to load low stock report");
        });

        new Thread(loadTask).start();
    }

    // ==================== SALES REPORT ====================

    @FXML
    private void handleGenerateSalesReport() {
        LocalDate startDate = salesStartDate.getValue();
        LocalDate endDate = salesEndDate.getValue();

        if (startDate == null || endDate == null) {
            showWarning("Invalid Date", "Please select both start and end dates");
            return;
        }

        if (startDate.isAfter(endDate)) {
            showWarning("Invalid Date Range", "Start date must be before end date");
            return;
        }

        salesReportStatus.setText("Generating report...");

        Task<SalesReportSummary> reportTask = new Task<>() {
            @Override
            protected SalesReportSummary call() {
                return reportService.getSalesReport(startDate, endDate);
            }
        };

        reportTask.setOnSucceeded(e -> {
            SalesReportSummary summary = reportTask.getValue();
            displaySalesReport(summary);
            salesReportStatus.setText(String.format("Report generated: %s to %s",
                    startDate, endDate));
            logger.info("Generated sales report: {} transactions, total: {}",
                    summary.getTotalTransactions(), summary.getTotalSales());
        });

        reportTask.setOnFailed(e -> {
            logger.error("Failed to generate sales report", reportTask.getException());
            salesReportStatus.setText("Report generation failed");
            showError("Report Error", "Failed to generate sales report");
        });

        new Thread(reportTask).start();
    }

    private void displaySalesReport(SalesReportSummary summary) {
        // Update summary cards
        totalSalesLabel.setText(String.format("LKR %.2f", summary.getTotalSales()));
        totalTransactionsLabel.setText(String.valueOf(summary.getTotalTransactions()));
        totalDiscountLabel.setText(String.format("LKR %.2f", summary.getTotalDiscount()));

        BigDecimal avgTransaction = summary.getTotalTransactions() > 0
                ? summary.getTotalSales().divide(
                        BigDecimal.valueOf(summary.getTotalTransactions()),
                        2, BigDecimal.ROUND_HALF_UP)
                : BigDecimal.ZERO;
        avgTransactionLabel.setText(String.format("LKR %.2f", avgTransaction));

        // Update tables
        salesByUserTable.setItems(FXCollections.observableArrayList(summary.getSalesByUser()));
        salesByCustomerTable.setItems(FXCollections.observableArrayList(summary.getSalesByCustomer()));

        // Convert payment type map to list for table
        List<PaymentTypeSummary> paymentSummaries = summary.getSalesByPaymentType().entrySet().stream()
                .map(entry -> new PaymentTypeSummary(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        salesByPaymentTable.setItems(FXCollections.observableArrayList(paymentSummaries));
    }

    // ==================== PROFIT REPORT ====================

    @FXML
    private void handleGenerateProfitReport() {
        if (!sessionManager.isAdmin()) {
            showError("Access Denied", "Only administrators can view profit reports");
            return;
        }

        LocalDate startDate = profitStartDate.getValue();
        LocalDate endDate = profitEndDate.getValue();

        if (startDate == null || endDate == null) {
            showWarning("Invalid Date", "Please select both start and end dates");
            return;
        }

        if (startDate.isAfter(endDate)) {
            showWarning("Invalid Date Range", "Start date must be before end date");
            return;
        }

        profitReportStatus.setText("Generating report...");

        Task<ProfitReportSummary> reportTask = new Task<>() {
            @Override
            protected ProfitReportSummary call() {
                return reportService.getProfitReport(startDate, endDate);
            }
        };

        reportTask.setOnSucceeded(e -> {
            ProfitReportSummary summary = reportTask.getValue();
            displayProfitReport(summary);
            profitReportStatus.setText(String.format("Report generated: %s to %s",
                    startDate, endDate));
            logger.info("Generated profit report: Revenue: {}, Cost: {}, Profit: {}",
                    summary.getTotalRevenue(), summary.getTotalCost(), summary.getTotalProfit());
        });

        reportTask.setOnFailed(e -> {
            logger.error("Failed to generate profit report", reportTask.getException());
            profitReportStatus.setText("Report generation failed");
            showError("Report Error", "Failed to generate profit report");
        });

        new Thread(reportTask).start();
    }

    private void displayProfitReport(ProfitReportSummary summary) {
        // Update summary cards
        totalRevenueLabel.setText(String.format("LKR %.2f", summary.getTotalRevenue()));
        totalCostLabel.setText(String.format("LKR %.2f", summary.getTotalCost()));
        totalProfitLabel.setText(String.format("LKR %.2f", summary.getTotalProfit()));
        profitMarginLabel.setText(String.format("%.2f%%", summary.getProfitMargin()));

        // Update details table
        profitDetailsTable.setItems(FXCollections.observableArrayList(summary.getItems()));
    }

    // ==================== TOP CUSTOMERS REPORT ====================

    @FXML
    private void handleRefreshTopCustomers() {
        loadTopCustomersReport();
    }

    private void loadTopCustomersReport() {
        Task<List<TopCustomerReportItem>> loadTask = new Task<>() {
            @Override
            protected List<TopCustomerReportItem> call() {
                return reportService.getTopCustomersReport(20);
            }
        };

        loadTask.setOnSucceeded(e -> {
            List<TopCustomerReportItem> items = loadTask.getValue();

            // Add rank to items
            AtomicInteger rank = new AtomicInteger(1);
            List<TopCustomerWithRank> rankedItems = items.stream()
                    .map(item -> new TopCustomerWithRank(
                            rank.getAndIncrement(),
                            item.getCustomerName(),
                            item.getPhoneNumber(),
                            item.getVisitCount(),
                            item.getTotalPurchase(),
                            item.getAvgPurchase()
                    ))
                    .collect(Collectors.toList());

            topCustomersTable.setItems(FXCollections.observableArrayList(rankedItems));
            logger.info("Loaded top {} customers", items.size());
        });

        loadTask.setOnFailed(e -> {
            logger.error("Failed to load top customers report", loadTask.getException());
            showError("Report Error", "Failed to load top customers report");
        });

        new Thread(loadTask).start();
    }

    // ==================== NAVIGATION ====================

    @FXML
    private void handleBackToDashboard() {
        try {
            BatikPOSApplication.navigateTo("/fxml/MainDashboard.fxml", "Batik POS - Dashboard");
        } catch (Exception e) {
            logger.error("Failed to navigate to dashboard", e);
            showError("Navigation Error", "Failed to return to dashboard");
        }
    }

    // ==================== UTILITY METHODS ====================

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("An error occurred");
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ==================== WRAPPER CLASSES FOR TABLE DISPLAY ====================

    /**
     * Wrapper class for payment type summary display in table.
     */
    public static class PaymentTypeSummary {
        private final String paymentType;
        private final BigDecimal amount;

        public PaymentTypeSummary(String paymentType, BigDecimal amount) {
            this.paymentType = paymentType;
            this.amount = amount;
        }

        public String getPaymentType() { return paymentType; }
        public BigDecimal getAmount() { return amount; }
    }

    /**
     * Wrapper class for top customer with rank.
     */
    public static class TopCustomerWithRank {
        private final int rank;
        private final String customerName;
        private final String phoneNumber;
        private final int visitCount;
        private final BigDecimal totalPurchase;
        private final BigDecimal avgPurchase;

        public TopCustomerWithRank(int rank, String customerName, String phoneNumber,
                                  int visitCount, BigDecimal totalPurchase, BigDecimal avgPurchase) {
            this.rank = rank;
            this.customerName = customerName;
            this.phoneNumber = phoneNumber;
            this.visitCount = visitCount;
            this.totalPurchase = totalPurchase;
            this.avgPurchase = avgPurchase;
        }

        public int getRank() { return rank; }
        public String getCustomerName() { return customerName; }
        public String getPhoneNumber() { return phoneNumber; }
        public int getVisitCount() { return visitCount; }
        public BigDecimal getTotalPurchase() { return totalPurchase; }
        public BigDecimal getAvgPurchase() { return avgPurchase; }
    }
}
