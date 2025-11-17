package com.chamathka.bathikpos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a customer sales transaction
 * Header table for a sale (receipt)
 */
@Entity
@Table(name = "sale")
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sale_id")
    private Long saleId;

    @NotNull(message = "Sale timestamp is required")
    @Column(name = "sale_timestamp", nullable = false)
    private LocalDateTime saleTimestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @NotNull(message = "Total amount is required")
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @NotNull(message = "Payment type is required")
    @Size(max = 50)
    @Column(name = "payment_type", nullable = false, length = 50)
    private String paymentType; // "Cash", "Card", "Split"

    // Additional fields for split payments
    @Column(name = "cash_amount", precision = 10, scale = 2)
    private BigDecimal cashAmount = BigDecimal.ZERO;

    @Column(name = "card_amount", precision = 10, scale = 2)
    private BigDecimal cardAmount = BigDecimal.ZERO;

    // Relationships
    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<SaleItem> saleItems = new ArrayList<>();

    // Constructors
    public Sale() {
        this.saleTimestamp = LocalDateTime.now();
    }

    public Sale(User user) {
        this();
        this.user = user;
    }

    public Sale(User user, Customer customer) {
        this(user);
        this.customer = customer;
    }

    // Getters and Setters
    public Long getSaleId() {
        return saleId;
    }

    public void setSaleId(Long saleId) {
        this.saleId = saleId;
    }

    public LocalDateTime getSaleTimestamp() {
        return saleTimestamp;
    }

    public void setSaleTimestamp(LocalDateTime saleTimestamp) {
        this.saleTimestamp = saleTimestamp;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public BigDecimal getCashAmount() {
        return cashAmount;
    }

    public void setCashAmount(BigDecimal cashAmount) {
        this.cashAmount = cashAmount;
    }

    public BigDecimal getCardAmount() {
        return cardAmount;
    }

    public void setCardAmount(BigDecimal cardAmount) {
        this.cardAmount = cardAmount;
    }

    public List<SaleItem> getSaleItems() {
        return saleItems;
    }

    public void setSaleItems(List<SaleItem> saleItems) {
        this.saleItems = saleItems;
    }

    // Helper methods
    public void addSaleItem(SaleItem item) {
        saleItems.add(item);
        item.setSale(this);
        recalculateTotal();
    }

    public void removeSaleItem(SaleItem item) {
        saleItems.remove(item);
        item.setSale(null);
        recalculateTotal();
    }

    public void recalculateTotal() {
        BigDecimal subtotal = saleItems.stream()
                .map(item -> item.getPriceAtSale().multiply(BigDecimal.valueOf(item.getQuantitySold())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        this.totalAmount = subtotal.subtract(discountAmount);
    }

    public BigDecimal getSubtotal() {
        return saleItems.stream()
                .map(item -> item.getPriceAtSale().multiply(BigDecimal.valueOf(item.getQuantitySold())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTotalItemCount() {
        return saleItems.stream()
                .mapToInt(SaleItem::getQuantitySold)
                .sum();
    }

    public boolean isSplitPayment() {
        return "Split".equalsIgnoreCase(paymentType);
    }

    @Override
    public String toString() {
        return "Sale{" +
                "saleId=" + saleId +
                ", saleTimestamp=" + saleTimestamp +
                ", totalAmount=" + totalAmount +
                ", discountAmount=" + discountAmount +
                ", paymentType='" + paymentType + '\'' +
                '}';
    }
}
