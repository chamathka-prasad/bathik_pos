package com.chamathka.bathikpos.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entity class representing a line item in a Sale.
 * Stores the critical priceAtSale (snapshot of selling price at time of sale).
 */
@Entity
@Table(name = "SaleItem", indexes = {
    @Index(name = "idx_sale", columnList = "sale_id"),
    @Index(name = "idx_variant", columnList = "variant_id")
})
public class SaleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "saleItemId")
    private Long saleItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false, foreignKey = @ForeignKey(name = "fk_saleitem_sale"))
    private Sale sale;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "variant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_saleitem_variant"))
    private ProductVariant variant;

    @Column(name = "quantitySold", nullable = false)
    private Integer quantitySold;

    @Column(name = "priceAtSale", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtSale; // Critical: snapshot of sellingPrice at time of sale

    // Constructors
    public SaleItem() {
    }

    public SaleItem(Sale sale, ProductVariant variant, Integer quantitySold, BigDecimal priceAtSale) {
        this.sale = sale;
        this.variant = variant;
        this.quantitySold = quantitySold;
        this.priceAtSale = priceAtSale;
    }

    // Getters and Setters
    public Long getSaleItemId() {
        return saleItemId;
    }

    public void setSaleItemId(Long saleItemId) {
        this.saleItemId = saleItemId;
    }

    public Sale getSale() {
        return sale;
    }

    public void setSale(Sale sale) {
        this.sale = sale;
    }

    public ProductVariant getVariant() {
        return variant;
    }

    public void setVariant(ProductVariant variant) {
        this.variant = variant;
    }

    public Integer getQuantitySold() {
        return quantitySold;
    }

    public void setQuantitySold(Integer quantitySold) {
        this.quantitySold = quantitySold;
    }

    public BigDecimal getPriceAtSale() {
        return priceAtSale;
    }

    public void setPriceAtSale(BigDecimal priceAtSale) {
        this.priceAtSale = priceAtSale;
    }

    // Utility method
    public BigDecimal getLineTotal() {
        return priceAtSale.multiply(new BigDecimal(quantitySold));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SaleItem saleItem = (SaleItem) o;
        return Objects.equals(saleItemId, saleItem.saleItemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(saleItemId);
    }

    @Override
    public String toString() {
        return "SaleItem{" +
                "saleItemId=" + saleItemId +
                ", quantitySold=" + quantitySold +
                ", priceAtSale=" + priceAtSale +
                '}';
    }
}
