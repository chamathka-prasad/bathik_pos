package com.chamathka.bathikpos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a customer in the CRM system
 * Stores customer information and tracks purchase history
 */
@Entity
@Table(name = "customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long customerId;

    @NotNull(message = "Customer name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotNull(message = "Phone number is required")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Column(name = "phone_number", nullable = false, unique = true, length = 20)
    private String phoneNumber;

    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "total_purchases", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPurchases = BigDecimal.ZERO;

    @Column(name = "visit_count", nullable = false)
    private Integer visitCount = 0;

    // Relationships
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Sale> sales = new ArrayList<>();

    // Constructors
    public Customer() {
    }

    public Customer(String name, String phoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public Customer(String name, String phoneNumber, String email) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    // Getters and Setters
    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BigDecimal getTotalPurchases() {
        return totalPurchases;
    }

    public void setTotalPurchases(BigDecimal totalPurchases) {
        this.totalPurchases = totalPurchases;
    }

    public Integer getVisitCount() {
        return visitCount;
    }

    public void setVisitCount(Integer visitCount) {
        this.visitCount = visitCount;
    }

    public List<Sale> getSales() {
        return sales;
    }

    public void setSales(List<Sale> sales) {
        this.sales = sales;
    }

    // Helper methods
    public void incrementVisitCount() {
        this.visitCount++;
    }

    public void addToPurchases(BigDecimal amount) {
        this.totalPurchases = this.totalPurchases.add(amount);
    }

    @Override
    public String toString() {
        return "Customer{" +
                "customerId=" + customerId +
                ", name='" + name + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", totalPurchases=" + totalPurchases +
                ", visitCount=" + visitCount +
                '}';
    }
}
