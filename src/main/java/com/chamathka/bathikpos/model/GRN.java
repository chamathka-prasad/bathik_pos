package com.chamathka.bathikpos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a Goods Received Note (GRN)
 * Header table for a stock-in transaction from a supplier
 */
@Entity
@Table(name = "grn")
public class GRN {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grn_id")
    private Long grnId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @NotNull(message = "GRN timestamp is required")
    @Column(name = "grn_timestamp", nullable = false)
    private LocalDateTime grnTimestamp;

    @Size(max = 50, message = "Supplier invoice number must not exceed 50 characters")
    @Column(name = "supplier_invoice_no", length = 50)
    private String supplierInvoiceNo;

    @NotNull(message = "Total cost is required")
    @Column(name = "total_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCost = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "Status is required")
    @Size(max = 20)
    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING"; // "PENDING" or "CONFIRMED"

    // Relationships
    @OneToMany(mappedBy = "grn", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<GRNItem> grnItems = new ArrayList<>();

    // Constructors
    public GRN() {
        this.grnTimestamp = LocalDateTime.now();
    }

    public GRN(Supplier supplier, User user) {
        this();
        this.supplier = supplier;
        this.user = user;
    }

    public GRN(Supplier supplier, User user, String supplierInvoiceNo) {
        this(supplier, user);
        this.supplierInvoiceNo = supplierInvoiceNo;
    }

    // Getters and Setters
    public Long getGrnId() {
        return grnId;
    }

    public void setGrnId(Long grnId) {
        this.grnId = grnId;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public LocalDateTime getGrnTimestamp() {
        return grnTimestamp;
    }

    public void setGrnTimestamp(LocalDateTime grnTimestamp) {
        this.grnTimestamp = grnTimestamp;
    }

    public String getSupplierInvoiceNo() {
        return supplierInvoiceNo;
    }

    public void setSupplierInvoiceNo(String supplierInvoiceNo) {
        this.supplierInvoiceNo = supplierInvoiceNo;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<GRNItem> getGrnItems() {
        return grnItems;
    }

    public void setGrnItems(List<GRNItem> grnItems) {
        this.grnItems = grnItems;
    }

    // Helper methods
    public void addGrnItem(GRNItem item) {
        grnItems.add(item);
        item.setGrn(this);
        recalculateTotalCost();
    }

    public void removeGrnItem(GRNItem item) {
        grnItems.remove(item);
        item.setGrn(null);
        recalculateTotalCost();
    }

    public void recalculateTotalCost() {
        this.totalCost = grnItems.stream()
                .map(item -> item.getCostPrice().multiply(BigDecimal.valueOf(item.getQuantityReceived())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isPending() {
        return "PENDING".equals(status);
    }

    public boolean isConfirmed() {
        return "CONFIRMED".equals(status);
    }

    public void confirm() {
        this.status = "CONFIRMED";
        this.grnTimestamp = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "GRN{" +
                "grnId=" + grnId +
                ", supplierInvoiceNo='" + supplierInvoiceNo + '\'' +
                ", grnTimestamp=" + grnTimestamp +
                ", totalCost=" + totalCost +
                ", status='" + status + '\'' +
                '}';
    }
}
