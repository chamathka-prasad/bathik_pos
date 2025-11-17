package com.chamathka.bathikpos.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entity class representing a Product Variant (the actual, sellable item in inventory).
 * This is what has stock and a specific SKU (itemCode).
 */
@Entity
@Table(name = "ProductVariant", indexes = {
    @Index(name = "idx_item_code", columnList = "itemCode"),
    @Index(name = "idx_product", columnList = "product_id"),
    @Index(name = "idx_stock", columnList = "quantityInStock")
})
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "variantId")
    private Long variantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_variant_product"))
    private Product product;

    @Column(name = "itemCode", nullable = false, unique = true, length = 50)
    private String itemCode; // SKU (e.g., "SHIRT-RED-M")

    @Column(name = "attribute_Size", length = 50)
    private String attributeSize;

    @Column(name = "attribute_Color", length = 50)
    private String attributeColor;

    @Column(name = "sellingPrice", nullable = false, precision = 10, scale = 2)
    private BigDecimal sellingPrice;

    @Column(name = "quantityInStock", nullable = false)
    private Integer quantityInStock = 0;

    @Column(name = "lowStockThreshold", nullable = false)
    private Integer lowStockThreshold = 5;

    // Constructors
    public ProductVariant() {
    }

    public ProductVariant(Product product, String itemCode, String attributeSize,
                         String attributeColor, BigDecimal sellingPrice) {
        this.product = product;
        this.itemCode = itemCode;
        this.attributeSize = attributeSize;
        this.attributeColor = attributeColor;
        this.sellingPrice = sellingPrice;
        this.quantityInStock = 0;
        this.lowStockThreshold = 5;
    }

    // Getters and Setters
    public Long getVariantId() {
        return variantId;
    }

    public void setVariantId(Long variantId) {
        this.variantId = variantId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getAttributeSize() {
        return attributeSize;
    }

    public void setAttributeSize(String attributeSize) {
        this.attributeSize = attributeSize;
    }

    public String getAttributeColor() {
        return attributeColor;
    }

    public void setAttributeColor(String attributeColor) {
        this.attributeColor = attributeColor;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public Integer getQuantityInStock() {
        return quantityInStock;
    }

    public void setQuantityInStock(Integer quantityInStock) {
        this.quantityInStock = quantityInStock;
    }

    public Integer getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(Integer lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    // Utility methods
    public boolean isLowStock() {
        return quantityInStock <= lowStockThreshold;
    }

    public boolean isOutOfStock() {
        return quantityInStock == 0;
    }

    public void addStock(int quantity) {
        this.quantityInStock += quantity;
    }

    public void deductStock(int quantity) {
        if (quantity > this.quantityInStock) {
            throw new IllegalStateException("Cannot deduct " + quantity + " items. Only " +
                                          this.quantityInStock + " in stock.");
        }
        this.quantityInStock -= quantity;
    }

    public String getFullDescription() {
        StringBuilder sb = new StringBuilder();
        if (product != null) {
            sb.append(product.getName()).append(" - ");
        }
        if (attributeColor != null && !attributeColor.isEmpty()) {
            sb.append(attributeColor).append(" ");
        }
        if (attributeSize != null && !attributeSize.isEmpty()) {
            sb.append("(").append(attributeSize).append(")");
        }
        return sb.toString().trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductVariant that = (ProductVariant) o;
        return Objects.equals(variantId, that.variantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variantId);
    }

    @Override
    public String toString() {
        return "ProductVariant{" +
                "variantId=" + variantId +
                ", itemCode='" + itemCode + '\'' +
                ", size='" + attributeSize + '\'' +
                ", color='" + attributeColor + '\'' +
                ", sellingPrice=" + sellingPrice +
                ", quantityInStock=" + quantityInStock +
                '}';
    }
}
