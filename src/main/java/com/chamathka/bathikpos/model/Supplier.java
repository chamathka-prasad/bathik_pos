package com.chamathka.bathikpos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a supplier who provides goods to the store
 */
@Entity
@Table(name = "supplier")
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "supplier_id")
    private Long supplierId;

    @NotNull(message = "Supplier name is required")
    @Size(max = 100, message = "Supplier name must not exceed 100 characters")
    @Column(name = "supplier_name", nullable = false, length = 100)
    private String supplierName;

    @Size(max = 100, message = "Contact person name must not exceed 100 characters")
    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    @Column(name = "phone", length = 20)
    private String phone;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    @Column(name = "address", length = 255)
    private String address;

    // Relationships
    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();

    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GRN> grns = new ArrayList<>();

    // Constructors
    public Supplier() {
    }

    public Supplier(String supplierName) {
        this.supplierName = supplierName;
    }

    public Supplier(String supplierName, String contactPerson, String phone, String address) {
        this.supplierName = supplierName;
        this.contactPerson = contactPerson;
        this.phone = phone;
        this.address = address;
    }

    // Getters and Setters
    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public List<GRN> getGrns() {
        return grns;
    }

    public void setGrns(List<GRN> grns) {
        this.grns = grns;
    }

    @Override
    public String toString() {
        return "Supplier{" +
                "supplierId=" + supplierId +
                ", supplierName='" + supplierName + '\'' +
                ", contactPerson='" + contactPerson + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
