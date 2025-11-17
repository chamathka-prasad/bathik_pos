package com.chamathka.bathikpos.controller;

import com.chamathka.bathikpos.BatikPOSApplication;
import com.chamathka.bathikpos.entity.Customer;
import com.chamathka.bathikpos.service.CustomerService;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Controller for Customer Management.
 * Provides CRUD operations for customers (CRM).
 */
public class CustomerManagementController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerManagementController.class);

    @FXML private JFXTextField searchField;
    @FXML private TableView<Customer> customerTable;
    @FXML private Label statusLabel;

    private final CustomerService customerService;
    private final ObservableList<Customer> customerList;

    public CustomerManagementController() {
        this.customerService = new CustomerService();
        this.customerList = FXCollections.observableArrayList();
    }

    @FXML
    private void initialize() {
        logger.info("Customer Management initialized");

        // Set up purchases column
        TableColumn<Customer, Void> purchasesCol = (TableColumn<Customer, Void>) customerTable.getColumns().get(4);
        purchasesCol.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    Customer customer = getTableRow().getItem();
                    setText(String.format("Rs. %.2f", customer.getTotalPurchases()));
                    setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold; -fx-text-fill: #10b981;");
                }
            }
        });

        // Set up action column
        TableColumn<Customer, Void> actionColumn = (TableColumn<Customer, Void>) customerTable.getColumns().get(6);
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final JFXButton editBtn = new JFXButton("Edit");
            private final JFXButton deleteBtn = new JFXButton("Delete");
            private final HBox container = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 3;");
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 3;");
                container.setAlignment(Pos.CENTER);

                editBtn.setOnAction(e -> handleEditCustomer(getTableRow().getItem()));
                deleteBtn.setOnAction(e -> handleDeleteCustomer(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });

        // Bind table to observable list
        customerTable.setItems(customerList);

        // Load customers
        loadCustomers();
    }

    private void loadCustomers() {
        Task<List<Customer>> loadTask = new Task<>() {
            @Override
            protected List<Customer> call() {
                return customerService.getAllCustomers();
            }
        };

        loadTask.setOnSucceeded(e -> {
            customerList.clear();
            customerList.addAll(loadTask.getValue());
            statusLabel.setText(String.format("Showing %d customers", customerList.size()));
            logger.info("Loaded {} customers", customerList.size());
        });

        loadTask.setOnFailed(e -> {
            logger.error("Failed to load customers", loadTask.getException());
            showError("Failed to load customers", loadTask.getException().getMessage());
        });

        new Thread(loadTask).start();
    }

    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().trim();

        if (searchTerm.isEmpty()) {
            loadCustomers();
            return;
        }

        Task<List<Customer>> searchTask = new Task<>() {
            @Override
            protected List<Customer> call() {
                return customerService.searchCustomers(searchTerm);
            }
        };

        searchTask.setOnSucceeded(e -> {
            customerList.clear();
            customerList.addAll(searchTask.getValue());
            statusLabel.setText(String.format("Found %d customers matching '%s'", customerList.size(), searchTerm));
        });

        new Thread(searchTask).start();
    }

    @FXML
    private void handleAddCustomer() {
        Dialog<Customer> dialog = createCustomerDialog(null);
        Optional<Customer> result = dialog.showAndWait();

        result.ifPresent(customer -> {
            Task<Customer> saveTask = new Task<>() {
                @Override
                protected Customer call() {
                    return customerService.createCustomer(customer);
                }
            };

            saveTask.setOnSucceeded(e -> {
                loadCustomers();
                showInfo("Success", "Customer added successfully");
                logger.info("Customer created: {}", customer.getName());
            });

            saveTask.setOnFailed(e -> {
                logger.error("Failed to create customer", saveTask.getException());
                showError("Failed to add customer", saveTask.getException().getMessage());
            });

            new Thread(saveTask).start();
        });
    }

    private void handleEditCustomer(Customer customer) {
        if (customer == null) return;

        Dialog<Customer> dialog = createCustomerDialog(customer);
        Optional<Customer> result = dialog.showAndWait();

        result.ifPresent(updatedCustomer -> {
            Task<Customer> updateTask = new Task<>() {
                @Override
                protected Customer call() {
                    return customerService.updateCustomer(updatedCustomer);
                }
            };

            updateTask.setOnSucceeded(e -> {
                loadCustomers();
                showInfo("Success", "Customer updated successfully");
            });

            updateTask.setOnFailed(e -> {
                logger.error("Failed to update customer", updateTask.getException());
                showError("Failed to update customer", updateTask.getException().getMessage());
            });

            new Thread(updateTask).start();
        });
    }

    private void handleDeleteCustomer(Customer customer) {
        if (customer == null) return;

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Customer");
        confirmation.setHeaderText("Are you sure you want to delete this customer?");
        confirmation.setContentText(customer.getName() + "\n\nThis action cannot be undone.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Task<Void> deleteTask = new Task<>() {
                @Override
                protected Void call() {
                    customerService.deleteCustomer(customer);
                    return null;
                }
            };

            deleteTask.setOnSucceeded(e -> {
                loadCustomers();
                showInfo("Success", "Customer deleted successfully");
            });

            deleteTask.setOnFailed(e -> {
                logger.error("Failed to delete customer", deleteTask.getException());
                showError("Failed to delete customer", deleteTask.getException().getMessage());
            });

            new Thread(deleteTask).start();
        }
    }

    private Dialog<Customer> createCustomerDialog(Customer customer) {
        Dialog<Customer> dialog = new Dialog<>();
        dialog.setTitle(customer == null ? "Add Customer" : "Edit Customer");
        dialog.setHeaderText(customer == null ? "Enter customer details" : "Update customer details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        JFXTextField nameField = new JFXTextField();
        nameField.setPromptText("Customer Name");
        if (customer != null) nameField.setText(customer.getName());

        JFXTextField phoneField = new JFXTextField();
        phoneField.setPromptText("Phone Number");
        if (customer != null) phoneField.setText(customer.getPhoneNumber());

        JFXTextField emailField = new JFXTextField();
        emailField.setPromptText("Email (Optional)");
        if (customer != null && customer.getEmail() != null) emailField.setText(customer.getEmail());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Phone:"), 0, 1);
        grid.add(phoneField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(nameField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Customer result = customer != null ? customer : new Customer();
                result.setName(nameField.getText());
                result.setPhoneNumber(phoneField.getText());
                result.setEmail(emailField.getText().isEmpty() ? null : emailField.getText());
                return result;
            }
            return null;
        });

        return dialog;
    }

    @FXML
    private void handleBackToDashboard() {
        try {
            BatikPOSApplication.navigateTo("/fxml/MainDashboard.fxml", "Batik POS - Dashboard");
        } catch (Exception e) {
            logger.error("Failed to navigate to dashboard", e);
        }
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
