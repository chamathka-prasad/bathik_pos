package com.chamathka.bathikpos.controller;

import com.chamathka.bathikpos.BatikPOSApplication;
import com.chamathka.bathikpos.dao.SaleDAO;
import com.chamathka.bathikpos.entity.Sale;
import com.chamathka.bathikpos.entity.SaleItem;
import com.chamathka.bathikpos.service.ReturnService;
import com.chamathka.bathikpos.util.SessionManager;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for Return Processing.
 * Allows searching for sales and processing returns with stock restoration.
 */
public class ReturnProcessingController {

    private static final Logger logger = LoggerFactory.getLogger(ReturnProcessingController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML private JFXTextField receiptIdField;
    @FXML private Label searchStatusLabel;

    @FXML private VBox saleDetailsSection;
    @FXML private Label saleReceiptIdLabel;
    @FXML private Label saleDateLabel;
    @FXML private Label customerLabel;
    @FXML private Label totalAmountLabel;

    @FXML private VBox returnItemsSection;
    @FXML private TableView<ReturnItemRow> saleItemsTable;
    @FXML private Label returnSummaryLabel;
    @FXML private Label refundAmountLabel;
    @FXML private JFXButton processReturnButton;

    private final SaleDAO saleDAO;
    private final ReturnService returnService;
    private final SessionManager sessionManager;

    private Sale currentSale;
    private final ObservableList<ReturnItemRow> returnItems;

    public ReturnProcessingController() {
        this.saleDAO = new SaleDAO();
        this.returnService = new ReturnService();
        this.sessionManager = SessionManager.getInstance();
        this.returnItems = FXCollections.observableArrayList();
    }

    @FXML
    private void initialize() {
        logger.info("Return Processing initialized");

        // Set up checkbox column
        TableColumn<ReturnItemRow, Boolean> selectColumn =
                (TableColumn<ReturnItemRow, Boolean>) saleItemsTable.getColumns().get(0);
        selectColumn.setCellFactory(col -> new CheckBoxTableCell<>());

        // Set up return quantity column with spinner
        TableColumn<ReturnItemRow, Integer> returnQtyColumn =
                (TableColumn<ReturnItemRow, Integer>) saleItemsTable.getColumns().get(7);
        returnQtyColumn.setCellFactory(col -> new ReturnQuantityCell());

        // Bind table to observable list
        saleItemsTable.setItems(returnItems);

        // Listen for changes to update refund amount
        returnItems.addListener((javafx.collections.ListChangeListener.Change<? extends ReturnItemRow> c) -> {
            updateRefundAmount();
        });
    }

    @FXML
    private void handleSearchSale() {
        String receiptId = receiptIdField.getText().trim();

        if (receiptId.isEmpty()) {
            showWarning("Invalid Input", "Please enter a sale ID");
            return;
        }

        // Validate that it's a number
        Long saleId;
        try {
            saleId = Long.parseLong(receiptId);
        } catch (NumberFormatException e) {
            showWarning("Invalid Input", "Sale ID must be a number");
            return;
        }

        searchStatusLabel.setText("Searching...");

        Task<Optional<Sale>> searchTask = new Task<>() {
            @Override
            protected Optional<Sale> call() {
                return saleDAO.findById(saleId);
            }
        };

        searchTask.setOnSucceeded(e -> {
            Optional<Sale> saleOpt = searchTask.getValue();

            if (saleOpt.isPresent()) {
                currentSale = saleOpt.get();
                displaySaleDetails(currentSale);
                searchStatusLabel.setText("Sale found");
                logger.info("Found sale: {}", receiptId);
            } else {
                hideSaleDetails();
                searchStatusLabel.setText("Sale not found");
                showWarning("Sale Not Found", "No sale found with receipt ID: " + receiptId);
            }
        });

        searchTask.setOnFailed(e -> {
            logger.error("Failed to search sale", searchTask.getException());
            searchStatusLabel.setText("Search failed");
            showError("Search Error", "Failed to search for sale");
        });

        new Thread(searchTask).start();
    }

    private void displaySaleDetails(Sale sale) {
        // Show sections
        saleDetailsSection.setVisible(true);
        saleDetailsSection.setManaged(true);
        returnItemsSection.setVisible(true);
        returnItemsSection.setManaged(true);

        // Update sale details
        saleReceiptIdLabel.setText(String.valueOf(sale.getSaleId()));
        saleDateLabel.setText(sale.getSaleTimestamp().format(DATE_FORMATTER));
        customerLabel.setText(sale.getCustomer() != null
                ? sale.getCustomer().getName()
                : "Walk-in Customer");
        totalAmountLabel.setText(String.format("LKR %.2f", sale.getTotalAmount()));

        // Populate items table
        returnItems.clear();
        for (SaleItem saleItem : sale.getItems()) {
            returnItems.add(new ReturnItemRow(saleItem));
        }

        // Reset refund amount
        updateRefundAmount();
    }

    private void hideSaleDetails() {
        saleDetailsSection.setVisible(false);
        saleDetailsSection.setManaged(false);
        returnItemsSection.setVisible(false);
        returnItemsSection.setManaged(false);
        returnItems.clear();
        currentSale = null;
    }

    @FXML
    private void handleClearSelection() {
        for (ReturnItemRow row : returnItems) {
            row.setSelected(false);
            row.setReturnQuantity(0);
        }
        updateRefundAmount();
    }

    @FXML
    private void handleProcessReturn() {
        // Validate selection
        List<ReturnItemRow> selectedItems = returnItems.stream()
                .filter(ReturnItemRow::isSelected)
                .filter(row -> row.getReturnQuantity() > 0)
                .collect(Collectors.toList());

        if (selectedItems.isEmpty()) {
            showWarning("No Items Selected", "Please select items to return and specify quantities");
            return;
        }

        // Validate quantities
        for (ReturnItemRow row : selectedItems) {
            if (row.getReturnQuantity() > row.getQuantitySold()) {
                showWarning("Invalid Quantity",
                        String.format("Return quantity for %s cannot exceed sold quantity (%d)",
                                row.getProductName(), row.getQuantitySold()));
                return;
            }
        }

        // Calculate refund amount
        BigDecimal refundAmount = selectedItems.stream()
                .map(row -> row.getPriceAtSale().multiply(BigDecimal.valueOf(row.getReturnQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Confirmation dialog
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Return");
        confirmation.setHeaderText("Process Return?");
        confirmation.setContentText(String.format(
                "Return %d item(s) from sale #%d\n\nRefund Amount: LKR %.2f\n\nThis will restore stock. Continue?",
                selectedItems.size(),
                currentSale.getSaleId(),
                refundAmount
        ));

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            processReturn(selectedItems, refundAmount);
        }
    }

    private void processReturn(List<ReturnItemRow> selectedItems, BigDecimal refundAmount) {
        Task<Void> returnTask = new Task<>() {
            @Override
            protected Void call() {
                // Create list of SaleItems with return quantities
                List<SaleItem> returnedItems = selectedItems.stream()
                        .map(row -> {
                            SaleItem returnItem = new SaleItem();
                            returnItem.setVariant(row.getSaleItem().getVariant());
                            returnItem.setQuantitySold(row.getReturnQuantity()); // Use return quantity
                            return returnItem;
                        })
                        .collect(Collectors.toList());

                // Process return using service (ATOMIC transaction)
                returnService.processReturn(currentSale.getSaleId(), returnedItems);
                return null;
            }
        };

        returnTask.setOnSucceeded(e -> {
            showSuccess("Return Processed",
                    String.format("Return processed successfully!\n\nRefund Amount: LKR %.2f\n\nStock has been restored.",
                            refundAmount));

            logger.info("Return processed for sale: {}, Refund: {}",
                    currentSale.getSaleId(), refundAmount);

            // Clear the form
            receiptIdField.clear();
            hideSaleDetails();
            searchStatusLabel.setText("Return completed - Search for another sale");
        });

        returnTask.setOnFailed(e -> {
            logger.error("Failed to process return", returnTask.getException());
            showError("Return Failed", "Failed to process return: " + returnTask.getException().getMessage());
        });

        new Thread(returnTask).start();
    }

    private void updateRefundAmount() {
        List<ReturnItemRow> selectedItems = returnItems.stream()
                .filter(ReturnItemRow::isSelected)
                .filter(row -> row.getReturnQuantity() > 0)
                .collect(Collectors.toList());

        if (selectedItems.isEmpty()) {
            returnSummaryLabel.setText("No items selected for return");
            refundAmountLabel.setText("LKR 0.00");
            processReturnButton.setDisable(true);
            return;
        }

        BigDecimal refundAmount = selectedItems.stream()
                .map(row -> row.getPriceAtSale().multiply(BigDecimal.valueOf(row.getReturnQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItems = selectedItems.stream()
                .mapToInt(ReturnItemRow::getReturnQuantity)
                .sum();

        returnSummaryLabel.setText(String.format("%d item(s) selected for return", totalItems));
        refundAmountLabel.setText(String.format("LKR %.2f", refundAmount));
        processReturnButton.setDisable(false);
    }

    @FXML
    private void handleBackToDashboard() {
        try {
            BatikPOSApplication.navigateTo("/fxml/MainDashboard.fxml", "Batik POS - Dashboard");
        } catch (Exception e) {
            logger.error("Failed to navigate to dashboard", e);
            showError("Navigation Error", "Failed to return to dashboard");
        }
    }

    // ==================== UTILITY METHODS ====================

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
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

    // ==================== TABLE CELL CLASSES ====================

    /**
     * Custom cell for checkbox selection
     */
    private class CheckBoxTableCell<S, T> extends TableCell<S, T> {
        private final CheckBox checkBox = new CheckBox();

        public CheckBoxTableCell() {
            checkBox.setOnAction(e -> {
                ReturnItemRow row = (ReturnItemRow) getTableRow().getItem();
                if (row != null) {
                    row.setSelected(checkBox.isSelected());
                    updateRefundAmount();
                }
            });
        }

        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                setGraphic(null);
            } else {
                ReturnItemRow row = (ReturnItemRow) getTableRow().getItem();
                checkBox.setSelected(row.isSelected());
                setGraphic(checkBox);
            }
        }
    }

    /**
     * Custom cell for return quantity spinner
     */
    private class ReturnQuantityCell extends TableCell<ReturnItemRow, Integer> {
        private final Spinner<Integer> spinner;

        public ReturnQuantityCell() {
            spinner = new Spinner<>();
            spinner.setEditable(true);
            spinner.setPrefWidth(80);

            spinner.valueProperty().addListener((obs, oldVal, newVal) -> {
                ReturnItemRow row = getTableRow() != null ? getTableRow().getItem() : null;
                if (row != null && newVal != null) {
                    row.setReturnQuantity(newVal);
                    updateRefundAmount();
                }
            });
        }

        @Override
        protected void updateItem(Integer item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                setGraphic(null);
            } else {
                ReturnItemRow row = getTableRow().getItem();
                spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        0, row.getQuantitySold(), row.getReturnQuantity()));
                setGraphic(spinner);
            }
        }
    }

    // ==================== ROW CLASS ====================

    /**
     * Wrapper class for return item row with selection state
     */
    public static class ReturnItemRow {
        private final SaleItem saleItem;
        private final SimpleBooleanProperty selected;
        private final SimpleIntegerProperty returnQuantity;

        public ReturnItemRow(SaleItem saleItem) {
            this.saleItem = saleItem;
            this.selected = new SimpleBooleanProperty(false);
            this.returnQuantity = new SimpleIntegerProperty(0);
        }

        public SaleItem getSaleItem() {
            return saleItem;
        }

        public boolean isSelected() {
            return selected.get();
        }

        public void setSelected(boolean selected) {
            this.selected.set(selected);
        }

        public SimpleBooleanProperty selectedProperty() {
            return selected;
        }

        public int getReturnQuantity() {
            return returnQuantity.get();
        }

        public void setReturnQuantity(int quantity) {
            this.returnQuantity.set(quantity);
        }

        public SimpleIntegerProperty returnQuantityProperty() {
            return returnQuantity;
        }

        // Properties for table display
        public String getProductName() {
            return saleItem.getVariant().getProduct().getName();
        }

        public String getSku() {
            return saleItem.getVariant().getItemCode();
        }

        public String getSize() {
            return saleItem.getVariant().getAttributeSize();
        }

        public String getColor() {
            return saleItem.getVariant().getAttributeColor();
        }

        public int getQuantitySold() {
            return saleItem.getQuantitySold();
        }

        public BigDecimal getPriceAtSale() {
            return saleItem.getPriceAtSale();
        }

        public BigDecimal getSubtotal() {
            return saleItem.getPriceAtSale().multiply(BigDecimal.valueOf(saleItem.getQuantitySold()));
        }
    }
}
