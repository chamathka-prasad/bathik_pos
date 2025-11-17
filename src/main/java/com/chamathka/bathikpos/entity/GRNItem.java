package com.chamathka.bathikpos.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entity class representing a line item in a GRN.
 * Stores the quantity received and the critical costPrice from the supplier.
 */
@Entity
@Table(name = "GRNItem", indexes = {
    @Index(name = "idx_grn", columnList = "grn_id"),
    @Index(name = "idx_variant", columnList = "variant_id")
})
public class GRNItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grnItemId")
    private Long grnItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grn_id", nullable = false, foreignKey = @ForeignKey(name = "fk_grnitem_grn"))
    private GRN grn;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "variant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_grnitem_variant"))
    private ProductVariant variant;

    @Column(name = "quantityReceived", nullable = false)
    private Integer quantityReceived;

    @Column(name = "costPrice", nullable = false, precision = 10, scale = 2)
    private BigDecimal costPrice; // Critical: cost per unit from supplier

    // Constructors
    public GRNItem() {
    }

    public GRNItem(GRN grn, ProductVariant variant, Integer quantityReceived, BigDecimal costPrice) {
        this.grn = grn;
        this.variant = variant;
        this.quantityReceived = quantityReceived;
        this.costPrice = costPrice;
    }

    // Getters and Setters
    public Long getGrnItemId() {
        return grnItemId;
    }

    public void setGrnItemId(Long grnItemId) {
        this.grnItemId = grnItemId;
    }

    public GRN getGrn() {
        return grn;
    }

    public void setGrn(GRN grn) {
        this.grn = grn;
    }

    public ProductVariant getVariant() {
        return variant;
    }

    public void setVariant(ProductVariant variant) {
        this.variant = variant;
    }

    public Integer getQuantityReceived() {
        return quantityReceived;
    }

    public void setQuantityReceived(Integer quantityReceived) {
        this.quantityReceived = quantityReceived;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    // Utility method
    public BigDecimal getTotalCost() {
        return costPrice.multiply(new BigDecimal(quantityReceived));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GRNItem grnItem = (GRNItem) o;
        return Objects.equals(grnItemId, grnItem.grnItemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(grnItemId);
    }

    @Override
    public String toString() {
        return "GRNItem{" +
                "grnItemId=" + grnItemId +
                ", quantityReceived=" + quantityReceived +
                ", costPrice=" + costPrice +
                '}';
    }
}
