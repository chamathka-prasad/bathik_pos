package com.chamathka.bathikpos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a product variant (the actual sellable item in inventory)
 * Example: "SHIRT-RED-M" is a variant of the master product "Batik Shirt"
 * This is what has stock and is sold to customers
 */
@Entity
@Table(name = "product_variant")
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "variant_id")
    private Long variantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull(message = "Item code (SKU) is required")
    @Size(max = 50, message = "Item code must not exceed 50 characters")
    @Column(name = "item_code", nullable = false, unique = true, length = 50)
    private String itemCode; // SKU

    @Size(max = 50)
    @Column(name = "attribute_size", length = 50)
    private String attributeSize;

    @Size(max = 50)
    @Column(name = "attribute_color", length = 50)
    private String attributeColor;

    @NotNull(message = "Selling price is required")
    @Column(name = "selling_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal sellingPrice;

    @NotNull(message = "Quantity in stock is required")
    @Column(name = "quantity_in_stock", nullable = false)
    private Integer quantityInStock = 0;

    @NotNull(message = "Low stock threshold is required")
    @Column(name = "low_stock_threshold", nullable = false)
    private Integer lowStockThreshold = 5;

    // Relationships
    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GRNItem> grnItems = new ArrayList<>();

    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SaleItem> saleItems = new ArrayList<>();

    // Constructors
    public ProductVariant() {
    }

    public ProductVariant(Product product, String itemCode, BigDecimal sellingPrice) {
        this.product = product;
        this.itemCode = itemCode;
        this.sellingPrice = sellingPrice;
    }

    public ProductVariant(Product product, String itemCode, String attributeSize, 
                         String attributeColor, BigDecimal sellingPrice) {
        this.product = product;
        this.itemCode = itemCode;
        this.attributeSize = attributeSize;
        this.attributeColor = attributeColor;
        this.sellingPrice = sellingPrice;
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

    public List<GRNItem> getGrnItems() {
        return grnItems;
    }

    public void setGrnItems(List<GRNItem> grnItems) {
        this.grnItems = grnItems;
    }

    public List<SaleItem> getSaleItems() {
        return saleItems;
    }

    public void setSaleItems(List<SaleItem> saleItems) {
        this.saleItems = saleItems;
    }

    // Helper methods
    public boolean isLowStock() {
        return quantityInStock <= lowStockThreshold;
    }

    public void addStock(int quantity) {
        this.quantityInStock += quantity;
    }

    public void deductStock(int quantity) {
        this.quantityInStock -= quantity;
    }

    public boolean hasStock() {
        return quantityInStock > 0;
    }

    public String getFullName() {
        StringBuilder fullName = new StringBuilder(product != null ? product.getName() : "");
        if (attributeColor != null && !attributeColor.isEmpty()) {
            fullName.append(" - ").append(attributeColor);
        }
        if (attributeSize != null && !attributeSize.isEmpty()) {
            fullName.append(" - ").append(attributeSize);
        }
        return fullName.toString();
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
