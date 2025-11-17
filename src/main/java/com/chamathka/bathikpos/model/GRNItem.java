package com.chamathka.bathikpos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Entity representing a line item for a specific GRN
 * Records the quantity and cost price of each item received from supplier
 */
@Entity
@Table(name = "grn_item")
public class GRNItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grn_item_id")
    private Long grnItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grn_id", nullable = false)
    private GRN grn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @NotNull(message = "Quantity received is required")
    @Column(name = "quantity_received", nullable = false)
    private Integer quantityReceived;

    @NotNull(message = "Cost price is required")
    @Column(name = "cost_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal costPrice;

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

    // Helper methods
    public BigDecimal getTotalCost() {
        return costPrice.multiply(BigDecimal.valueOf(quantityReceived));
    }

    @Override
    public String toString() {
        return "GRNItem{" +
                "grnItemId=" + grnItemId +
                ", quantityReceived=" + quantityReceived +
                ", costPrice=" + costPrice +
                ", totalCost=" + getTotalCost() +
                '}';
    }
}
