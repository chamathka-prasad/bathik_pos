package com.chamathka.bathikpos.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entity class representing a Sale (customer transaction).
 * Header table for a sales transaction.
 */
@Entity
@Table(name = "Sale", indexes = {
    @Index(name = "idx_timestamp", columnList = "saleTimestamp"),
    @Index(name = "idx_user", columnList = "user_id"),
    @Index(name = "idx_customer", columnList = "customer_id")
})
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "saleId")
    private Long saleId;

    @Column(name = "saleTimestamp", nullable = false)
    private LocalDateTime saleTimestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_sale_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", foreignKey = @ForeignKey(name = "fk_sale_customer"))
    private Customer customer;

    @Column(name = "totalAmount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "discountAmount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "paymentType", nullable = false, length = 50)
    private String paymentType; // "Cash", "Card", "Split"

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SaleItem> items = new ArrayList<>();

    // Constructors
    public Sale() {
        this.saleTimestamp = LocalDateTime.now();
    }

    public Sale(User user, Customer customer, String paymentType) {
        this.user = user;
        this.customer = customer;
        this.paymentType = paymentType;
        this.saleTimestamp = LocalDateTime.now();
        this.totalAmount = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
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

    public List<SaleItem> getItems() {
        return items;
    }

    public void setItems(List<SaleItem> items) {
        this.items = items;
    }

    // Helper methods
    public void addItem(SaleItem item) {
        items.add(item);
        item.setSale(this);
    }

    public void removeItem(SaleItem item) {
        items.remove(item);
        item.setSale(null);
    }

    public BigDecimal getSubtotal() {
        return items.stream()
                .map(item -> item.getPriceAtSale().multiply(new BigDecimal(item.getQuantitySold())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void recalculateTotalAmount() {
        BigDecimal subtotal = getSubtotal();
        this.totalAmount = subtotal.subtract(discountAmount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sale sale = (Sale) o;
        return Objects.equals(saleId, sale.saleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(saleId);
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
