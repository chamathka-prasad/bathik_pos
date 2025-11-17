package com.chamathka.bathikpos.controller;

import com.chamathka.bathikpos.BatikPOSApplication;
import com.chamathka.bathikpos.entity.*;
import com.chamathka.bathikpos.service.GRNService;
import com.chamathka.bathikpos.service.ProductService;
import com.chamathka.bathikpos.service.SupplierService;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Controller for GRN Management.
 * Handles stock intake with ATOMIC transaction (Admin only).
 */
public class GRNManagementController {

    private static final Logger logger = LoggerFactory.getLogger(GRNManagementController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML private JFXComboBox<String> statusFilter;
    @FXML private TableView<GRN> grnTable;
    @FXML private Label statusLabel;

    // GRN Form Panel
    @FXML private VBox grnFormPanel;
    @FXML private JFXComboBox<Supplier> supplierCombo;
    @FXML private JFXTextField invoiceField;
    @FXML private TableView<GRNItem> grnItemsTable;
    @FXML private Label totalCostLabel;

    private final GRNService grnService;
    private final SupplierService supplierService;
    private final ProductService productService;
    private final SessionManager sessionManager;

    private final ObservableList<GRN> grnList;
    private final ObservableList<GRNItem> currentGRNItems;
    private GRN currentGRN;

    public GRNManagementController() {
        this.grnService = new GRNService();
        this.supplierService = new SupplierService();
        this.productService = new ProductService();
        this.sessionManager = SessionManager.getInstance();
        this.grnList = FXCollections.observableArrayList();
        this.currentGRNItems = FXCollections.observableArrayList();
    }

    @FXML
    private void initialize() {
        logger.info("GRN Management initialized");

        // Set up GRN table cell factories
        setupGRNTableCells();

        // Set up GRN items table
        grnItemsTable.setItems(currentGRNItems);
        setupGRNItemsTableCells();

        // Bind GRN table
        grnTable.setItems(grnList);

        // Set default filter
        statusFilter.setValue("All");

        // Load suppliers into combo box
        loadSuppliers();

        // Load GRN history
        loadGRNHistory();
    }

    private void setupGRNTableCells() {
        TableColumn<GRN, Void> dateCol = (TableColumn<GRN, Void>) grnTable.getColumns().get(1);
        dateCol.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    GRN grn = getTableRow().getItem();
                    setText(grn.getGrnTimestamp().format(DATE_FORMATTER));
                }
            }
        });

        TableColumn<GRN, Void> supplierCol = (TableColumn<GRN, Void>) grnTable.getColumns().get(2);
        supplierCol.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    GRN grn = getTableRow().getItem();
                    setText(grn.getSupplier() != null ? grn.getSupplier().getSupplierName() : "N/A");
                }
            }
        });

        TableColumn<GRN, Void> totalCostCol = (TableColumn<GRN, Void>) grnTable.getColumns().get(4);
        totalCostCol.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    GRN grn = getTableRow().getItem();
                    setText(String.format("Rs. %.2f", grn.getTotalCost()));
                }
            }
        });

        TableColumn<GRN, Void> statusCol = (TableColumn<GRN, Void>) grnTable.getColumns().get(5);
        statusCol.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle("");
                } else {
                    GRN grn = getTableRow().getItem();
                    setText(grn.getStatus());
                    if ("CONFIRMED".equals(grn.getStatus())) {
                        setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    private void setupGRNItemsTableCells() {
        TableColumn<GRNItem, Void> variantCol = (TableColumn<GRNItem, Void>) grnItemsTable.getColumns().get(0);
        variantCol.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    GRNItem grnItem = getTableRow().getItem();
                    setText(grnItem.getVariant().getFullDescription());
                }
            }
        });

        TableColumn<GRNItem, Void> skuCol = (TableColumn<GRNItem, Void>) grnItemsTable.getColumns().get(1);
        skuCol.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    GRNItem grnItem = getTableRow().getItem();
                    setText(grnItem.getVariant().getItemCode());
                }
            }
        });

        TableColumn<GRNItem, Void> costPriceCol = (TableColumn<GRNItem, Void>) grnItemsTable.getColumns().get(3);
        costPriceCol.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    GRNItem grnItem = getTableRow().getItem();
                    setText(String.format("Rs. %.2f", grnItem.getCostPrice()));
                }
            }
        });

        TableColumn<GRNItem, Void> itemTotalCol = (TableColumn<GRNItem, Void>) grnItemsTable.getColumns().get(4);
        itemTotalCol.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    GRNItem grnItem = getTableRow().getItem();
                    setText(String.format("Rs. %.2f", grnItem.getTotalCost()));
                }
            }
        });

        TableColumn<GRNItem, Void> removeCol = (TableColumn<GRNItem, Void>) grnItemsTable.getColumns().get(5);
        removeCol.setCellFactory(param -> new TableCell<>() {
            private final JFXButton removeBtn = new JFXButton("✕");

            {
                removeBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 3;");
                removeBtn.setOnAction(e -> {
                    GRNItem item = getTableRow().getItem();
                    if (item != null) {
                        currentGRNItems.remove(item);
                        updateTotalCost();
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

    private void loadSuppliers() {
        Task<List<Supplier>> task = new Task<>() {
            @Override
            protected List<Supplier> call() {
                return supplierService.getAllSuppliers();
            }
        };

        task.setOnSucceeded(e -> {
            supplierCombo.getItems().clear();
            supplierCombo.getItems().addAll(task.getValue());
        });

        new Thread(task).start();
    }

    private void loadGRNHistory() {
        String filter = statusFilter.getValue();

        Task<List<GRN>> task = new Task<>() {
            @Override
            protected List<GRN> call() {
                if ("All".equals(filter)) {
                    return grnService.getConfirmedGRNs();
                } else {
                    return grnService.findByStatus(filter);
                }
            }
        };

        task.setOnSucceeded(e -> {
            grnList.clear();
            grnList.addAll(task.getValue());
            statusLabel.setText(String.format("Showing %d GRNs", grnList.size()));
        });

        task.setOnFailed(e -> {
            logger.error("Failed to load GRNs", task.getException());
            showError("Failed to load GRNs", task.getException().getMessage());
        });

        new Thread(task).start();
    }

    @FXML
    private void handleFilterChange() {
        loadGRNHistory();
    }

    @FXML
    private void handleNewGRN() {
        logger.info("Creating new GRN");

        // Initialize new GRN
        currentGRN = new GRN();
        currentGRN.setUser(sessionManager.getCurrentUser());
        currentGRNItems.clear();

        // Clear form
        supplierCombo.setValue(null);
        invoiceField.clear();
        totalCostLabel.setText("Rs. 0.00");

        // Show form panel
        grnFormPanel.setVisible(true);
        grnFormPanel.setManaged(true);
    }

    @FXML
    private void handleAddGRNItem() {
        Dialog<GRNItem> dialog = createAddItemDialog();
        Optional<GRNItem> result = dialog.showAndWait();

        result.ifPresent(grnItem -> {
            currentGRNItems.add(grnItem);
            updateTotalCost();
            logger.info("Added item to GRN: {}", grnItem.getVariant().getItemCode());
        });
    }

    private Dialog<GRNItem> createAddItemDialog() {
        Dialog<GRNItem> dialog = new Dialog<>();
        dialog.setTitle("Add Item to GRN");
        dialog.setHeaderText("Select product variant and enter details");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Create form
        ComboBox<ProductVariant> variantCombo = new ComboBox<>();
        variantCombo.setPromptText("Select Product Variant");
        variantCombo.setPrefWidth(300);

        // Load variants
        Task<List<ProductVariant>> loadTask = new Task<>() {
            @Override
            protected List<ProductVariant> call() {
                return productService.getAllVariants();
            }
        };
        loadTask.setOnSucceeded(e -> {
            variantCombo.getItems().addAll(loadTask.getValue());
        });
        new Thread(loadTask).start();

        JFXTextField quantityField = new JFXTextField();
        quantityField.setPromptText("Quantity Received");

        JFXTextField costPriceField = new JFXTextField();
        costPriceField.setPromptText("Cost Price per Unit");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Product Variant:"), 0, 0);
        grid.add(variantCombo, 1, 0);
        grid.add(new Label("Quantity:"), 0, 1);
        grid.add(quantityField, 1, 1);
        grid.add(new Label("Cost Price:"), 0, 2);
        grid.add(costPriceField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(variantCombo::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    ProductVariant variant = variantCombo.getValue();
                    int quantity = Integer.parseInt(quantityField.getText());
                    BigDecimal costPrice = new BigDecimal(costPriceField.getText());

                    if (variant == null) {
                        showError("Validation Error", "Please select a product variant");
                        return null;
                    }

                    GRNItem item = new GRNItem();
                    item.setVariant(variant);
                    item.setQuantityReceived(quantity);
                    item.setCostPrice(costPrice);
                    return item;

                } catch (NumberFormatException e) {
                    showError("Validation Error", "Please enter valid numbers");
                    return null;
                }
            }
            return null;
        });

        return dialog;
    }

    @FXML
    private void handleConfirmGRN() {
        // Validate
        if (supplierCombo.getValue() == null) {
            showError("Validation Error", "Please select a supplier");
            return;
        }

        if (currentGRNItems.isEmpty()) {
            showError("Validation Error", "Please add at least one item to the GRN");
            return;
        }

        // Confirmation dialog
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm GRN");
        confirmation.setHeaderText("Confirm Goods Received Note");
        confirmation.setContentText(
            String.format("Supplier: %s\nItems: %d\nTotal Cost: Rs. %.2f\n\n" +
                         "This will add stock to inventory. Continue?",
                         supplierCombo.getValue().getSupplierName(),
                         currentGRNItems.size(),
                         calculateTotalCost())
        );

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            confirmGRN();
        }
    }

    private void confirmGRN() {
        // Set GRN details
        currentGRN.setSupplier(supplierCombo.getValue());
        currentGRN.setSupplierInvoiceNo(invoiceField.getText());

        // Add items to GRN
        for (GRNItem item : currentGRNItems) {
            currentGRN.addItem(item);
        }

        // Call ATOMIC transaction service
        Task<GRN> confirmTask = new Task<>() {
            @Override
            protected GRN call() {
                return grnService.confirmGRN(currentGRN);
            }
        };

        confirmTask.setOnSucceeded(e -> {
            logger.info("GRN confirmed successfully: {}", confirmTask.getValue().getGrnId());
            showInfo("Success", "GRN confirmed successfully!\nStock has been added to inventory.");

            // Reset form
            grnFormPanel.setVisible(false);
            grnFormPanel.setManaged(false);
            currentGRN = null;
            currentGRNItems.clear();

            // Reload GRN history
            loadGRNHistory();
        });

        confirmTask.setOnFailed(e -> {
            logger.error("Failed to confirm GRN", confirmTask.getException());
            showError("GRN Confirmation Failed",
                     "Failed to confirm GRN: " + confirmTask.getException().getMessage() +
                     "\n\nThe transaction has been rolled back. No changes were made.");
        });

        new Thread(confirmTask).start();
    }

    @FXML
    private void handleCancelGRN() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Cancel GRN");
        confirmation.setHeaderText("Are you sure you want to cancel?");
        confirmation.setContentText("All entered data will be lost.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            grnFormPanel.setVisible(false);
            grnFormPanel.setManaged(false);
            currentGRN = null;
            currentGRNItems.clear();
        }
    }

    private void updateTotalCost() {
        BigDecimal total = calculateTotalCost();
        totalCostLabel.setText(String.format("Rs. %.2f", total));
    }

    private BigDecimal calculateTotalCost() {
        return currentGRNItems.stream()
                .map(GRNItem::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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
