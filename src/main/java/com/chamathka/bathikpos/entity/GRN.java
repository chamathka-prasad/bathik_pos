package com.chamathka.bathikpos.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entity class representing a Goods Received Note (GRN).
 * Header table for a stock-in transaction from a supplier.
 */
@Entity
@Table(name = "GRN", indexes = {
    @Index(name = "idx_supplier", columnList = "supplier_id"),
    @Index(name = "idx_timestamp", columnList = "grnTimestamp"),
    @Index(name = "idx_status", columnList = "status")
})
public class GRN {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grnId")
    private Long grnId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false, foreignKey = @ForeignKey(name = "fk_grn_supplier"))
    private Supplier supplier;

    @Column(name = "grnTimestamp", nullable = false)
    private LocalDateTime grnTimestamp;

    @Column(name = "supplierInvoiceNo", length = 50)
    private String supplierInvoiceNo;

    @Column(name = "totalCost", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCost = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_grn_user"))
    private User user;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING"; // "PENDING" or "CONFIRMED"

    @OneToMany(mappedBy = "grn", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<GRNItem> items = new ArrayList<>();

    // Constructors
    public GRN() {
        this.grnTimestamp = LocalDateTime.now();
        this.status = "PENDING";
    }

    public GRN(Supplier supplier, User user, String supplierInvoiceNo) {
        this.supplier = supplier;
        this.user = user;
        this.supplierInvoiceNo = supplierInvoiceNo;
        this.grnTimestamp = LocalDateTime.now();
        this.status = "PENDING";
        this.totalCost = BigDecimal.ZERO;
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

    public List<GRNItem> getItems() {
        return items;
    }

    public void setItems(List<GRNItem> items) {
        this.items = items;
    }

    // Helper methods
    public void addItem(GRNItem item) {
        items.add(item);
        item.setGrn(this);
        recalculateTotalCost();
    }

    public void removeItem(GRNItem item) {
        items.remove(item);
        item.setGrn(null);
        recalculateTotalCost();
    }

    public void recalculateTotalCost() {
        this.totalCost = items.stream()
                .map(item -> item.getCostPrice().multiply(new BigDecimal(item.getQuantityReceived())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(status);
    }

    public boolean isConfirmed() {
        return "CONFIRMED".equalsIgnoreCase(status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GRN grn = (GRN) o;
        return Objects.equals(grnId, grn.grnId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(grnId);
    }

    @Override
    public String toString() {
        return "GRN{" +
                "grnId=" + grnId +
                ", supplierInvoiceNo='" + supplierInvoiceNo + '\'' +
                ", totalCost=" + totalCost +
                ", status='" + status + '\'' +
                ", grnTimestamp=" + grnTimestamp +
                '}';
    }
}
