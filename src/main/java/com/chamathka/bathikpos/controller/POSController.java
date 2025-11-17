package com.chamathka.bathikpos.controller;

import com.chamathka.bathikpos.BatikPOSApplication;
import com.chamathka.bathikpos.entity.Customer;
import com.chamathka.bathikpos.entity.ProductVariant;
import com.chamathka.bathikpos.entity.Sale;
import com.chamathka.bathikpos.entity.SaleItem;
import com.chamathka.bathikpos.service.CustomerService;
import com.chamathka.bathikpos.service.ProductService;
import com.chamathka.bathikpos.service.SaleService;
import com.chamathka.bathikpos.util.SessionManager;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Controller for Point of Sale (POS).
 * Handles the complete sales process with ATOMIC checkout transaction.
 */
public class POSController {

    private static final Logger logger = LoggerFactory.getLogger(POSController.class);

    // Product Panel
    @FXML private JFXTextField productSearchField;
    @FXML private TableView<ProductVariant> productTable;

    // Cart Panel
    @FXML private TableView<SaleItem> cartTable;
    @FXML private Label subtotalLabel;
    @FXML private JFXTextField discountField;
    @FXML private Label discountLabel;
    @FXML private Label totalLabel;

    // Checkout Panel
    @FXML private Label cashierLabel;
    @FXML private JFXComboBox<Customer> customerCombo;
    @FXML private JFXComboBox<String> paymentTypeCombo;
    @FXML private VBox splitPaymentPanel;
    @FXML private JFXTextField cashAmountField;
    @FXML private JFXTextField cardAmountField;
    @FXML private Label checkoutMessageLabel;

    private final SaleService saleService;
    private final ProductService productService;
    private final CustomerService customerService;
    private final SessionManager sessionManager;

    private final ObservableList<ProductVariant> productList;
    private final ObservableList<SaleItem> cartItems;

    public POSController() {
        this.saleService = new SaleService();
        this.productService = new ProductService();
        this.customerService = new CustomerService();
        this.sessionManager = SessionManager.getInstance();
        this.productList = FXCollections.observableArrayList();
        this.cartItems = FXCollections.observableArrayList();
    }

    @FXML
    private void initialize() {
        logger.info("POS initialized");

        // Set cashier name
        cashierLabel.setText("Cashier: " + sessionManager.getCurrentUsername());

        // Set up product table cell factories
        setupProductTableCells();

        // Set up cart table cell factories
        setupCartTableCells();

        // Bind tables
        productTable.setItems(productList);
        cartTable.setItems(cartItems);

        // Set default payment type
        paymentTypeCombo.setValue("Cash");

        // Listen for payment type changes
        paymentTypeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isSplit = "Split".equals(newVal);
            splitPaymentPanel.setVisible(isSplit);
            splitPaymentPanel.setManaged(isSplit);
        });

        // Load initial data
        loadProducts();
        loadCustomers();

        // Set discount field to 0
        discountField.setText("0");
    }

    private void setupProductTableCells() {
        TableColumn<ProductVariant, Void> nameCol = (TableColumn<ProductVariant, Void>) productTable.getColumns().get(0);
        nameCol.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    ProductVariant variant = getTableRow().getItem();
                    setText(variant.getFullDescription());
                }
            }
        });

        TableColumn<ProductVariant, Void> priceCol = (TableColumn<ProductVariant, Void>) productTable.getColumns().get(2);
        priceCol.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    ProductVariant variant = getTableRow().getItem();
                    setText(String.format("Rs. %.2f", variant.getSellingPrice()));
                }
            }
        });
    }

    private void setupCartTableCells() {
        TableColumn<SaleItem, Void> itemCol = (TableColumn<SaleItem, Void>) cartTable.getColumns().get(0);
        itemCol.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    SaleItem saleItem = getTableRow().getItem();
                    setText(saleItem.getVariant().getFullDescription());
                }
            }
        });

        TableColumn<SaleItem, Void> priceCol = (TableColumn<SaleItem, Void>) cartTable.getColumns().get(1);
        priceCol.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    SaleItem saleItem = getTableRow().getItem();
                    setText(String.format("Rs. %.2f", saleItem.getPriceAtSale()));
                }
            }
        });

        TableColumn<SaleItem, Void> totalCol = (TableColumn<SaleItem, Void>) cartTable.getColumns().get(3);
        totalCol.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    SaleItem saleItem = getTableRow().getItem();
                    setText(String.format("Rs. %.2f", saleItem.getLineTotal()));
                }
            }
        });

        TableColumn<SaleItem, Void> removeCol = (TableColumn<SaleItem, Void>) cartTable.getColumns().get(4);
        removeCol.setCellFactory(param -> new TableCell<>() {
            private final JFXButton removeBtn = new JFXButton("✕");

            {
                removeBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 3;");
                removeBtn.setOnAction(e -> {
                    SaleItem item = getTableRow().getItem();
                    if (item != null) {
                        cartItems.remove(item);
                        updateTotals();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeBtn);
                setAlignment(Pos.CENTER);
            }
        });
    }

    private void loadProducts() {
        Task<List<ProductVariant>> task = new Task<>() {
            @Override
            protected List<ProductVariant> call() {
                return productService.getInStockVariants();
            }
        };

        task.setOnSucceeded(e -> {
            productList.clear();
            productList.addAll(task.getValue());
            logger.info("Loaded {} products with stock", productList.size());
        });

        task.setOnFailed(e -> {
            logger.error("Failed to load products", task.getException());
            showError("Failed to load products", task.getException().getMessage());
        });

        new Thread(task).start();
    }

    private void loadCustomers() {
        Task<List<Customer>> task = new Task<>() {
            @Override
            protected List<Customer> call() {
                return customerService.getAllCustomers();
            }
        };

        task.setOnSucceeded(e -> {
            customerCombo.getItems().clear();
            customerCombo.getItems().addAll(task.getValue());
        });

        new Thread(task).start();
    }

    @FXML
    private void handleProductSearch() {
        String searchTerm = productSearchField.getText().trim();

        if (searchTerm.isEmpty()) {
            loadProducts();
            return;
        }

        Task<List<ProductVariant>> task = new Task<>() {
            @Override
            protected List<ProductVariant> call() {
                return productService.searchVariants(searchTerm);
            }
        };

        task.setOnSucceeded(e -> {
            productList.clear();
            List<ProductVariant> results = task.getValue();
            // Filter to only show in-stock items
            results.removeIf(v -> v.getQuantityInStock() <= 0);
            productList.addAll(results);
        });

        new Thread(task).start();
    }

    @FXML
    private void handleAddToCart() {
        ProductVariant selected = productTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showWarning("No Product Selected", "Please select a product to add to cart");
            return;
        }

        // CRITICAL: Check stock availability (as per SRS UC-01)
        if (selected.getQuantityInStock() <= 0) {
            showWarning("Out of Stock", "This product is currently out of stock");
            return;
        }

        // Ask for quantity
        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Add to Cart");
        dialog.setHeaderText("Add " + selected.getFullDescription());
        dialog.setContentText("Enter quantity:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(quantityStr -> {
            try {
                int quantity = Integer.parseInt(quantityStr);

                if (quantity <= 0) {
                    showWarning("Invalid Quantity", "Quantity must be greater than 0");
                    return;
                }

                // CRITICAL: Validate stock before adding (as per SRS)
                if (quantity > selected.getQuantityInStock()) {
                    showWarning("Insufficient Stock",
                        String.format("Only %d units available", selected.getQuantityInStock()));
                    return;
                }

                // Check if item already in cart
                Optional<SaleItem> existing = cartItems.stream()
                    .filter(item -> item.getVariant().getVariantId().equals(selected.getVariantId()))
                    .findFirst();

                if (existing.isPresent()) {
                    // Update quantity
                    SaleItem item = existing.get();
                    int newQty = item.getQuantitySold() + quantity;

                    if (newQty > selected.getQuantityInStock()) {
                        showWarning("Insufficient Stock",
                            String.format("Only %d units available", selected.getQuantityInStock()));
                        return;
                    }

                    item.setQuantitySold(newQty);
                } else {
                    // Add new item
                    SaleItem newItem = new SaleItem();
                    newItem.setVariant(selected);
                    newItem.setQuantitySold(quantity);
                    newItem.setPriceAtSale(selected.getSellingPrice()); // Snapshot price
                    cartItems.add(newItem);
                }

                updateTotals();
                logger.info("Added to cart: {} x {}", selected.getItemCode(), quantity);

            } catch (NumberFormatException e) {
                showWarning("Invalid Input", "Please enter a valid number");
            }
        });
    }

    @FXML
    private void handleClearCart() {
        if (cartItems.isEmpty()) return;

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Clear Cart");
        confirmation.setHeaderText("Are you sure you want to clear the cart?");
        confirmation.setContentText(String.format("%d items will be removed", cartItems.size()));

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            cartItems.clear();
            updateTotals();
        }
    }

    @FXML
    private void handleDiscountChange() {
        updateTotals();
    }

    @FXML
    private void handleQuickAddCustomer() {
        Dialog<Customer> dialog = new Dialog<>();
        dialog.setTitle("Quick Add Customer");
        dialog.setHeaderText("Add new customer");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        JFXTextField nameField = new JFXTextField();
        nameField.setPromptText("Customer Name");

        JFXTextField phoneField = new JFXTextField();
        phoneField.setPromptText("Phone Number");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Phone:"), 0, 1);
        grid.add(phoneField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(nameField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                Customer customer = new Customer();
                customer.setName(nameField.getText());
                customer.setPhoneNumber(phoneField.getText());
                return customer;
            }
            return null;
        });

        Optional<Customer> result = dialog.showAndWait();
        result.ifPresent(customer -> {
            Task<Customer> saveTask = new Task<>() {
                @Override
                protected Customer call() {
                    return customerService.createCustomer(customer);
                }
            };

            saveTask.setOnSucceeded(e -> {
                Customer saved = saveTask.getValue();
                customerCombo.getItems().add(saved);
                customerCombo.setValue(saved);
                showInfo("Success", "Customer added successfully");
            });

            saveTask.setOnFailed(e -> {
                logger.error("Failed to create customer", saveTask.getException());
                showError("Failed to add customer", saveTask.getException().getMessage());
            });

            new Thread(saveTask).start();
        });
    }

    @FXML
    private void handleCheckout() {
        // Validate cart
        if (cartItems.isEmpty()) {
            showWarning("Empty Cart", "Please add items to cart before checkout");
            return;
        }

        // Validate payment type
        if (paymentTypeCombo.getValue() == null) {
            showWarning("No Payment Type", "Please select a payment type");
            return;
        }

        // Show confirmation
        BigDecimal total = calculateTotal();
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Sale");
        confirmation.setHeaderText("Complete this sale?");
        confirmation.setContentText(String.format(
            "Items: %d\nTotal Amount: Rs. %.2f\nPayment: %s\n\n" +
            "This will deduct stock from inventory and cannot be undone.",
            cartItems.size(), total, paymentTypeCombo.getValue()));

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            processCheckout();
        }
    }

    private void processCheckout() {
        // Create sale
        Sale sale = new Sale();
        sale.setUser(sessionManager.getCurrentUser());
        sale.setCustomer(customerCombo.getValue()); // Can be null
        sale.setPaymentType(paymentTypeCombo.getValue());
        sale.setDiscountAmount(getDiscountAmount());

        // Add items to sale
        for (SaleItem item : cartItems) {
            sale.addItem(item);
        }

        // Calculate total
        sale.recalculateTotalAmount();

        // Call ATOMIC transaction service
        Task<Sale> checkoutTask = new Task<>() {
            @Override
            protected Sale call() {
                return saleService.processCheckout(sale);
            }
        };

        checkoutTask.setOnSucceeded(e -> {
            Sale completedSale = checkoutTask.getValue();
            logger.info("Checkout successful! Sale ID: {}", completedSale.getSaleId());

            // Show success message
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Sale Complete");
            success.setHeaderText("Sale completed successfully!");
            success.setContentText(String.format(
                "Receipt ID: %d\nTotal: Rs. %.2f\n\nThank you!",
                completedSale.getSaleId(), completedSale.getTotalAmount()));
            success.showAndWait();

            // Clear cart and reset form
            resetPOS();
        });

        checkoutTask.setOnFailed(e -> {
            logger.error("Checkout failed", checkoutTask.getException());
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Checkout Failed");
            error.setHeaderText("Failed to complete sale");
            error.setContentText(
                "Error: " + checkoutTask.getException().getMessage() +
                "\n\nThe transaction has been rolled back.\nNo changes were made to inventory.");
            error.showAndWait();
        });

        new Thread(checkoutTask).start();
    }

    private void resetPOS() {
        cartItems.clear();
        customerCombo.setValue(null);
        paymentTypeCombo.setValue("Cash");
        discountField.setText("0");
        cashAmountField.clear();
        cardAmountField.clear();
        updateTotals();
        loadProducts(); // Reload to get updated stock
    }

    private void updateTotals() {
        BigDecimal subtotal = calculateSubtotal();
        BigDecimal discount = getDiscountAmount();
        BigDecimal total = subtotal.subtract(discount);

        subtotalLabel.setText(String.format("Rs. %.2f", subtotal));
        discountLabel.setText(String.format("Rs. %.2f", discount));
        totalLabel.setText(String.format("Rs. %.2f", total));
    }

    private BigDecimal calculateSubtotal() {
        return cartItems.stream()
            .map(SaleItem::getLineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getDiscountAmount() {
        try {
            String discountText = discountField.getText().trim();
            if (discountText.isEmpty()) return BigDecimal.ZERO;
            return new BigDecimal(discountText);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal calculateTotal() {
        return calculateSubtotal().subtract(getDiscountAmount());
    }

    @FXML
    private void handleBackToDashboard() {
        if (!cartItems.isEmpty()) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Unsaved Changes");
            confirmation.setHeaderText("Cart has items");
            confirmation.setContentText("Are you sure you want to leave? Cart will be cleared.");

            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }
        }

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

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("An error occurred");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
