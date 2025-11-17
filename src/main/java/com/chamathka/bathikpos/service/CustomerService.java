package com.chamathka.bathikpos.service;

import com.chamathka.bathikpos.dao.CustomerDAO;
import com.chamathka.bathikpos.entity.Customer;
import com.chamathka.bathikpos.util.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Service class for Customer operations.
 * All customer (CRM) business logic is centralized here.
 */
public class CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);
    private final CustomerDAO customerDAO;
    private final SessionManager sessionManager;

    public CustomerService() {
        this.customerDAO = new CustomerDAO();
        this.sessionManager = SessionManager.getInstance();
    }

    /**
     * Create a new customer.
     * @param customer The customer to create
     * @return The saved customer
     */
    public Customer createCustomer(Customer customer) {
        sessionManager.requireAuthentication();
        logger.info("Creating new customer: {}", customer.getName());
        return customerDAO.save(customer);
    }

    /**
     * Update an existing customer.
     * @param customer The customer to update
     * @return The updated customer
     */
    public Customer updateCustomer(Customer customer) {
        sessionManager.requireAuthentication();
        logger.info("Updating customer: {}", customer.getName());
        return customerDAO.update(customer);
    }

    /**
     * Delete a customer.
     * @param customer The customer to delete
     */
    public void deleteCustomer(Customer customer) {
        sessionManager.requireAuthentication();
        logger.info("Deleting customer: {}", customer.getName());
        customerDAO.delete(customer);
    }

    /**
     * Find a customer by ID.
     * @param id The customer ID
     * @return The customer, or null if not found
     */
    public Customer findCustomerById(Long id) {
        sessionManager.requireAuthentication();
        return customerDAO.findById(id).orElse(null);
    }

    /**
     * Find a customer by phone number.
     * @param phoneNumber The phone number
     * @return The customer, or null if not found
     */
    public Customer findCustomerByPhone(String phoneNumber) {
        sessionManager.requireAuthentication();
        return customerDAO.findByPhoneNumber(phoneNumber).orElse(null);
    }

    /**
     * Get all customers.
     * @return List of all customers
     */
    public List<Customer> getAllCustomers() {
        sessionManager.requireAuthentication();
        return customerDAO.findAll();
    }

    /**
     * Search customers by name.
     * @param searchTerm The search term
     * @return List of matching customers
     */
    public List<Customer> searchCustomers(String searchTerm) {
        sessionManager.requireAuthentication();
        return customerDAO.searchByName(searchTerm);
    }

    /**
     * Get top customers by total purchases.
     * @param limit Number of top customers to return
     * @return List of top customers
     */
    public List<Customer> getTopCustomers(int limit) {
        sessionManager.requireAuthentication();
        return customerDAO.getTopCustomers(limit);
    }
}
