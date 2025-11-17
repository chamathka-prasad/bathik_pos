package com.chamathka.bathikpos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Entity representing a line item for a specific Sale
 * Records the quantity and price of each item sold in a transaction
 */
@Entity
@Table(name = "sale_item")
public class SaleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sale_item_id")
    private Long saleItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @NotNull(message = "Quantity sold is required")
    @Column(name = "quantity_sold", nullable = false)
    private Integer quantitySold;

    @NotNull(message = "Price at sale is required")
    @Column(name = "price_at_sale", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtSale;

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

    // Helper methods
    public BigDecimal getTotalPrice() {
        return priceAtSale.multiply(BigDecimal.valueOf(quantitySold));
    }

    @Override
    public String toString() {
        return "SaleItem{" +
                "saleItemId=" + saleItemId +
                ", quantitySold=" + quantitySold +
                ", priceAtSale=" + priceAtSale +
                ", totalPrice=" + getTotalPrice() +
                '}';
    }
}
