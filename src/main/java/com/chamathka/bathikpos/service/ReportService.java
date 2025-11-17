package com.chamathka.bathikpos.service;

import com.chamathka.bathikpos.dao.*;
import com.chamathka.bathikpos.entity.*;
import com.chamathka.bathikpos.util.SessionManager;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating various reports.
 * Provides Low Stock, Sales, and Profit reports.
 */
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    private final ProductVariantDAO productVariantDAO;
    private final SaleDAO saleDAO;
    private final CustomerDAO customerDAO;
    private final SessionManager sessionManager;

    public ReportService() {
        this.productVariantDAO = new ProductVariantDAO();
        this.saleDAO = new SaleDAO();
        this.customerDAO = new CustomerDAO();
        this.sessionManager = SessionManager.getInstance();
    }

    /**
     * Get low stock report.
     * Returns all variants where stock is below or equal to the threshold.
     */
    public List<LowStockReportItem> getLowStockReport() {
        sessionManager.requireAuthentication();
        logger.info("Generating low stock report for user: {}", sessionManager.getCurrentUser().getUsername());

        List<ProductVariant> lowStockVariants = productVariantDAO.getLowStockVariants();

        return lowStockVariants.stream()
                .map(variant -> new LowStockReportItem(
                        variant.getProduct().getName(),
                        variant.getItemCode(),
                        variant.getAttributeSize(),
                        variant.getAttributeColor(),
                        variant.getQuantityInStock(),
                        variant.getLowStockThreshold(),
                        variant.getSellingPrice()
                ))
                .sorted(Comparator.comparingInt(LowStockReportItem::getCurrentStock))
                .collect(Collectors.toList());
    }

    /**
     * Get sales report for a date range.
     */
    public SalesReportSummary getSalesReport(LocalDate startDate, LocalDate endDate) {
        sessionManager.requireAuthentication();
        logger.info("Generating sales report from {} to {}", startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Sale> sales = saleDAO.findByDateRange(startDateTime, endDateTime);

        // Calculate totals
        BigDecimal totalSales = sales.stream()
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDiscount = sales.stream()
                .map(Sale::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalTransactions = sales.size();

        // Group by user (cashier)
        Map<String, SalesUserSummary> salesByUser = sales.stream()
                .collect(Collectors.groupingBy(
                        sale -> sale.getUser().getUsername(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                userSales -> {
                                    BigDecimal userTotal = userSales.stream()
                                            .map(Sale::getTotalAmount)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                                    return new SalesUserSummary(
                                            userSales.get(0).getUser().getUsername(),
                                            userSales.size(),
                                            userTotal
                                    );
                                }
                        )
                ));

        // Group by customer
        Map<String, SalesCustomerSummary> salesByCustomer = sales.stream()
                .filter(sale -> sale.getCustomer() != null)
                .collect(Collectors.groupingBy(
                        sale -> sale.getCustomer().getName(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                customerSales -> {
                                    BigDecimal customerTotal = customerSales.stream()
                                            .map(Sale::getTotalAmount)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                                    return new SalesCustomerSummary(
                                            customerSales.get(0).getCustomer().getName(),
                                            customerSales.get(0).getCustomer().getPhoneNumber(),
                                            customerSales.size(),
                                            customerTotal
                                    );
                                }
                        )
                ));

        // Group by payment type
        Map<String, BigDecimal> salesByPaymentType = sales.stream()
                .collect(Collectors.groupingBy(
                        Sale::getPaymentType,
                        Collectors.mapping(
                                Sale::getTotalAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                ));

        return new SalesReportSummary(
                startDate,
                endDate,
                totalSales,
                totalDiscount,
                totalTransactions,
                new ArrayList<>(salesByUser.values()),
                new ArrayList<>(salesByCustomer.values()),
                salesByPaymentType,
                sales
        );
    }

    /**
     * Get profit report by calculating the difference between selling price and cost price.
     * This requires joining SaleItem (priceAtSale) with GRNItem (costPrice).
     */
    public ProfitReportSummary getProfitReport(LocalDate startDate, LocalDate endDate) {
        sessionManager.requireAdmin(); // Profit report is admin-only
        logger.info("Generating profit report from {} to {}", startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Sale> sales = saleDAO.findByDateRange(startDateTime, endDateTime);

        List<ProfitReportItem> profitItems = new ArrayList<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal totalProfit = BigDecimal.ZERO;

        for (Sale sale : sales) {
            for (SaleItem saleItem : sale.getItems()) {
                ProductVariant variant = saleItem.getVariant();

                // Get average cost price from GRN items for this variant
                BigDecimal avgCostPrice = getAverageCostPrice(variant);

                // Calculate profit for this item
                int quantity = saleItem.getQuantitySold();
                BigDecimal sellingPrice = saleItem.getPriceAtSale();
                BigDecimal revenue = sellingPrice.multiply(BigDecimal.valueOf(quantity));
                BigDecimal cost = avgCostPrice.multiply(BigDecimal.valueOf(quantity));
                BigDecimal profit = revenue.subtract(cost);

                totalRevenue = totalRevenue.add(revenue);
                totalCost = totalCost.add(cost);
                totalProfit = totalProfit.add(profit);

                profitItems.add(new ProfitReportItem(
                        sale.getSaleTimestamp(),
                        variant.getProduct().getName(),
                        variant.getItemCode(),
                        variant.getAttributeSize(),
                        variant.getAttributeColor(),
                        quantity,
                        avgCostPrice,
                        sellingPrice,
                        revenue,
                        cost,
                        profit
                ));
            }
        }

        // Calculate profit margin
        BigDecimal profitMargin = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                ? totalProfit.divide(totalRevenue, 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        return new ProfitReportSummary(
                startDate,
                endDate,
                totalRevenue,
                totalCost,
                totalProfit,
                profitMargin,
                profitItems
        );
    }

    /**
     * Calculate average cost price for a variant from GRN items.
     * Uses weighted average based on quantities received.
     */
    private BigDecimal getAverageCostPrice(ProductVariant variant) {
        try (Session session = com.chamathka.bathikpos.util.HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT gi FROM GRNItem gi " +
                        "WHERE gi.variant.variantId = :variantId " +
                        "AND gi.grn.status = 'CONFIRMED'";

            Query<GRNItem> query = session.createQuery(hql, GRNItem.class);
            query.setParameter("variantId", variant.getVariantId());
            List<GRNItem> grnItems = query.getResultList();

            if (grnItems.isEmpty()) {
                logger.warn("No GRN items found for variant: {}. Using selling price as cost.",
                           variant.getItemCode());
                return variant.getSellingPrice(); // Fallback to selling price
            }

            // Calculate weighted average
            BigDecimal totalCost = BigDecimal.ZERO;
            int totalQuantity = 0;

            for (GRNItem grnItem : grnItems) {
                BigDecimal itemCost = grnItem.getCostPrice()
                        .multiply(BigDecimal.valueOf(grnItem.getQuantityReceived()));
                totalCost = totalCost.add(itemCost);
                totalQuantity += grnItem.getQuantityReceived();
            }

            return totalQuantity > 0
                    ? totalCost.divide(BigDecimal.valueOf(totalQuantity), 2, BigDecimal.ROUND_HALF_UP)
                    : BigDecimal.ZERO;
        }
    }

    /**
     * Get top customers by purchase amount.
     */
    public List<TopCustomerReportItem> getTopCustomersReport(int limit) {
        sessionManager.requireAuthentication();
        logger.info("Generating top {} customers report", limit);

        List<Customer> allCustomers = customerDAO.findAll();

        return allCustomers.stream()
                .filter(customer -> customer.getTotalPurchases().compareTo(BigDecimal.ZERO) > 0)
                .sorted((c1, c2) -> c2.getTotalPurchases().compareTo(c1.getTotalPurchases()))
                .limit(limit)
                .map(customer -> new TopCustomerReportItem(
                        customer.getName(),
                        customer.getPhoneNumber(),
                        customer.getVisitCount(),
                        customer.getTotalPurchases(),
                        customer.getVisitCount() > 0
                                ? customer.getTotalPurchases()
                                    .divide(BigDecimal.valueOf(customer.getVisitCount()), 2, BigDecimal.ROUND_HALF_UP)
                                : BigDecimal.ZERO
                ))
                .collect(Collectors.toList());
    }

    // ==================== REPORT DATA CLASSES ====================

    /**
     * Low Stock Report Item
     */
    public static class LowStockReportItem {
        private final String productName;
        private final String sku;
        private final String size;
        private final String color;
        private final int currentStock;
        private final int reorderLevel;
        private final BigDecimal sellingPrice;

        public LowStockReportItem(String productName, String sku, String size, String color,
                                 int currentStock, int reorderLevel, BigDecimal sellingPrice) {
            this.productName = productName;
            this.sku = sku;
            this.size = size;
            this.color = color;
            this.currentStock = currentStock;
            this.reorderLevel = reorderLevel;
            this.sellingPrice = sellingPrice;
        }

        // Getters
        public String getProductName() { return productName; }
        public String getSku() { return sku; }
        public String getSize() { return size; }
        public String getColor() { return color; }
        public int getCurrentStock() { return currentStock; }
        public int getReorderLevel() { return reorderLevel; }
        public BigDecimal getSellingPrice() { return sellingPrice; }
    }

    /**
     * Sales Report Summary
     */
    public static class SalesReportSummary {
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final BigDecimal totalSales;
        private final BigDecimal totalDiscount;
        private final int totalTransactions;
        private final List<SalesUserSummary> salesByUser;
        private final List<SalesCustomerSummary> salesByCustomer;
        private final Map<String, BigDecimal> salesByPaymentType;
        private final List<Sale> allSales;

        public SalesReportSummary(LocalDate startDate, LocalDate endDate, BigDecimal totalSales,
                                BigDecimal totalDiscount, int totalTransactions,
                                List<SalesUserSummary> salesByUser,
                                List<SalesCustomerSummary> salesByCustomer,
                                Map<String, BigDecimal> salesByPaymentType,
                                List<Sale> allSales) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.totalSales = totalSales;
            this.totalDiscount = totalDiscount;
            this.totalTransactions = totalTransactions;
            this.salesByUser = salesByUser;
            this.salesByCustomer = salesByCustomer;
            this.salesByPaymentType = salesByPaymentType;
            this.allSales = allSales;
        }

        // Getters
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public BigDecimal getTotalSales() { return totalSales; }
        public BigDecimal getTotalDiscount() { return totalDiscount; }
        public int getTotalTransactions() { return totalTransactions; }
        public List<SalesUserSummary> getSalesByUser() { return salesByUser; }
        public List<SalesCustomerSummary> getSalesByCustomer() { return salesByCustomer; }
        public Map<String, BigDecimal> getSalesByPaymentType() { return salesByPaymentType; }
        public List<Sale> getAllSales() { return allSales; }
    }

    /**
     * Sales by User Summary
     */
    public static class SalesUserSummary {
        private final String username;
        private final int transactionCount;
        private final BigDecimal totalAmount;

        public SalesUserSummary(String username, int transactionCount, BigDecimal totalAmount) {
            this.username = username;
            this.transactionCount = transactionCount;
            this.totalAmount = totalAmount;
        }

        public String getUsername() { return username; }
        public int getTransactionCount() { return transactionCount; }
        public BigDecimal getTotalAmount() { return totalAmount; }
    }

    /**
     * Sales by Customer Summary
     */
    public static class SalesCustomerSummary {
        private final String customerName;
        private final String phoneNumber;
        private final int transactionCount;
        private final BigDecimal totalAmount;

        public SalesCustomerSummary(String customerName, String phoneNumber,
                                   int transactionCount, BigDecimal totalAmount) {
            this.customerName = customerName;
            this.phoneNumber = phoneNumber;
            this.transactionCount = transactionCount;
            this.totalAmount = totalAmount;
        }

        public String getCustomerName() { return customerName; }
        public String getPhoneNumber() { return phoneNumber; }
        public int getTransactionCount() { return transactionCount; }
        public BigDecimal getTotalAmount() { return totalAmount; }
    }

    /**
     * Profit Report Summary
     */
    public static class ProfitReportSummary {
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final BigDecimal totalRevenue;
        private final BigDecimal totalCost;
        private final BigDecimal totalProfit;
        private final BigDecimal profitMargin;
        private final List<ProfitReportItem> items;

        public ProfitReportSummary(LocalDate startDate, LocalDate endDate,
                                 BigDecimal totalRevenue, BigDecimal totalCost,
                                 BigDecimal totalProfit, BigDecimal profitMargin,
                                 List<ProfitReportItem> items) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.totalRevenue = totalRevenue;
            this.totalCost = totalCost;
            this.totalProfit = totalProfit;
            this.profitMargin = profitMargin;
            this.items = items;
        }

        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public BigDecimal getTotalCost() { return totalCost; }
        public BigDecimal getTotalProfit() { return totalProfit; }
        public BigDecimal getProfitMargin() { return profitMargin; }
        public List<ProfitReportItem> getItems() { return items; }
    }

    /**
     * Profit Report Item
     */
    public static class ProfitReportItem {
        private final LocalDateTime saleDate;
        private final String productName;
        private final String sku;
        private final String size;
        private final String color;
        private final int quantity;
        private final BigDecimal costPrice;
        private final BigDecimal sellingPrice;
        private final BigDecimal revenue;
        private final BigDecimal cost;
        private final BigDecimal profit;

        public ProfitReportItem(LocalDateTime saleDate, String productName, String sku,
                              String size, String color, int quantity,
                              BigDecimal costPrice, BigDecimal sellingPrice,
                              BigDecimal revenue, BigDecimal cost, BigDecimal profit) {
            this.saleDate = saleDate;
            this.productName = productName;
            this.sku = sku;
            this.size = size;
            this.color = color;
            this.quantity = quantity;
            this.costPrice = costPrice;
            this.sellingPrice = sellingPrice;
            this.revenue = revenue;
            this.cost = cost;
            this.profit = profit;
        }

        public LocalDateTime getSaleDate() { return saleDate; }
        public String getProductName() { return productName; }
        public String getSku() { return sku; }
        public String getSize() { return size; }
        public String getColor() { return color; }
        public int getQuantity() { return quantity; }
        public BigDecimal getCostPrice() { return costPrice; }
        public BigDecimal getSellingPrice() { return sellingPrice; }
        public BigDecimal getRevenue() { return revenue; }
        public BigDecimal getCost() { return cost; }
        public BigDecimal getProfit() { return profit; }
    }

    /**
     * Top Customer Report Item
     */
    public static class TopCustomerReportItem {
        private final String customerName;
        private final String phoneNumber;
        private final int visitCount;
        private final BigDecimal totalPurchase;
        private final BigDecimal avgPurchase;

        public TopCustomerReportItem(String customerName, String phoneNumber,
                                    int visitCount, BigDecimal totalPurchase,
                                    BigDecimal avgPurchase) {
            this.customerName = customerName;
            this.phoneNumber = phoneNumber;
            this.visitCount = visitCount;
            this.totalPurchase = totalPurchase;
            this.avgPurchase = avgPurchase;
        }

        public String getCustomerName() { return customerName; }
        public String getPhoneNumber() { return phoneNumber; }
        public int getVisitCount() { return visitCount; }
        public BigDecimal getTotalPurchase() { return totalPurchase; }
        public BigDecimal getAvgPurchase() { return avgPurchase; }
    }
}
