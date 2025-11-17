package com.chamathka.bathikpos.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entity class representing a Customer in the CRM system.
 * Tracks customer information and purchase history.
 */
@Entity
@Table(name = "Customer", indexes = {
    @Index(name = "idx_phone", columnList = "phoneNumber")
})
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customerId")
    private Long customerId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "phoneNumber", nullable = false, unique = true, length = 20)
    private String phoneNumber;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "totalPurchases", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPurchases = BigDecimal.ZERO;

    @Column(name = "visitCount", nullable = false)
    private Integer visitCount = 0;

    // Constructors
    public Customer() {
    }

    public Customer(String name, String phoneNumber, String email) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.totalPurchases = BigDecimal.ZERO;
        this.visitCount = 0;
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

    // Utility methods
    public void incrementVisitCount() {
        this.visitCount++;
    }

    public void addPurchaseAmount(BigDecimal amount) {
        this.totalPurchases = this.totalPurchases.add(amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(customerId, customer.customerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId);
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
