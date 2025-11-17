package com.chamathka.bathikpos.controller;

import com.chamathka.bathikpos.BatikPOSApplication;
import com.chamathka.bathikpos.entity.Supplier;
import com.chamathka.bathikpos.service.SupplierService;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Controller for Supplier Management.
 * Provides CRUD operations for suppliers (Admin only).
 */
public class SupplierManagementController {

    private static final Logger logger = LoggerFactory.getLogger(SupplierManagementController.class);

    @FXML private JFXTextField searchField;
    @FXML private TableView<Supplier> supplierTable;
    @FXML private Label statusLabel;

    private final SupplierService supplierService;
    private final ObservableList<Supplier> supplierList;

    public SupplierManagementController() {
        this.supplierService = new SupplierService();
        this.supplierList = FXCollections.observableArrayList();
    }

    @FXML
    private void initialize() {
        logger.info("Supplier Management initialized");

        // Set up action column with Edit and Delete buttons
        TableColumn<Supplier, Void> actionColumn = (TableColumn<Supplier, Void>) supplierTable.getColumns().get(5);
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final JFXButton editBtn = new JFXButton("Edit");
            private final JFXButton deleteBtn = new JFXButton("Delete");
            private final HBox container = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 3;");
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 3;");
                container.setAlignment(Pos.CENTER);

                editBtn.setOnAction(e -> handleEditSupplier(getTableRow().getItem()));
                deleteBtn.setOnAction(e -> handleDeleteSupplier(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });

        // Bind table to observable list
        supplierTable.setItems(supplierList);

        // Load suppliers
        loadSuppliers();
    }

    /**
     * Load all suppliers from database.
     */
    private void loadSuppliers() {
        Task<List<Supplier>> loadTask = new Task<>() {
            @Override
            protected List<Supplier> call() {
                return supplierService.getAllSuppliers();
            }
        };

        loadTask.setOnSucceeded(e -> {
            supplierList.clear();
            supplierList.addAll(loadTask.getValue());
            statusLabel.setText(String.format("Showing %d suppliers", supplierList.size()));
            logger.info("Loaded {} suppliers", supplierList.size());
        });

        loadTask.setOnFailed(e -> {
            logger.error("Failed to load suppliers", loadTask.getException());
            showError("Failed to load suppliers", loadTask.getException().getMessage());
        });

        new Thread(loadTask).start();
    }

    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().trim();

        if (searchTerm.isEmpty()) {
            loadSuppliers();
            return;
        }

        Task<List<Supplier>> searchTask = new Task<>() {
            @Override
            protected List<Supplier> call() {
                return supplierService.searchSuppliers(searchTerm);
            }
        };

        searchTask.setOnSucceeded(e -> {
            supplierList.clear();
            supplierList.addAll(searchTask.getValue());
            statusLabel.setText(String.format("Found %d suppliers matching '%s'", supplierList.size(), searchTerm));
        });

        searchTask.setOnFailed(e -> {
            logger.error("Search failed", searchTask.getException());
            showError("Search failed", searchTask.getException().getMessage());
        });

        new Thread(searchTask).start();
    }

    @FXML
    private void handleAddSupplier() {
        logger.info("Opening Add Supplier dialog");

        Dialog<Supplier> dialog = createSupplierDialog(null);
        Optional<Supplier> result = dialog.showAndWait();

        result.ifPresent(supplier -> {
            Task<Supplier> saveTask = new Task<>() {
                @Override
                protected Supplier call() {
                    return supplierService.createSupplier(supplier);
                }
            };

            saveTask.setOnSucceeded(e -> {
                loadSuppliers();
                showInfo("Success", "Supplier added successfully");
                logger.info("Supplier created: {}", supplier.getSupplierName());
            });

            saveTask.setOnFailed(e -> {
                logger.error("Failed to create supplier", saveTask.getException());
                showError("Failed to add supplier", saveTask.getException().getMessage());
            });

            new Thread(saveTask).start();
        });
    }

    private void handleEditSupplier(Supplier supplier) {
        if (supplier == null) return;

        logger.info("Opening Edit Supplier dialog for: {}", supplier.getSupplierName());

        Dialog<Supplier> dialog = createSupplierDialog(supplier);
        Optional<Supplier> result = dialog.showAndWait();

        result.ifPresent(updatedSupplier -> {
            Task<Supplier> updateTask = new Task<>() {
                @Override
                protected Supplier call() {
                    return supplierService.updateSupplier(updatedSupplier);
                }
            };

            updateTask.setOnSucceeded(e -> {
                loadSuppliers();
                showInfo("Success", "Supplier updated successfully");
                logger.info("Supplier updated: {}", updatedSupplier.getSupplierName());
            });

            updateTask.setOnFailed(e -> {
                logger.error("Failed to update supplier", updateTask.getException());
                showError("Failed to update supplier", updateTask.getException().getMessage());
            });

            new Thread(updateTask).start();
        });
    }

    private void handleDeleteSupplier(Supplier supplier) {
        if (supplier == null) return;

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Supplier");
        confirmation.setHeaderText("Are you sure you want to delete this supplier?");
        confirmation.setContentText(supplier.getSupplierName() + "\n\nThis action cannot be undone.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Task<Void> deleteTask = new Task<>() {
                @Override
                protected Void call() {
                    supplierService.deleteSupplier(supplier);
                    return null;
                }
            };

            deleteTask.setOnSucceeded(e -> {
                loadSuppliers();
                showInfo("Success", "Supplier deleted successfully");
                logger.info("Supplier deleted: {}", supplier.getSupplierName());
            });

            deleteTask.setOnFailed(e -> {
                logger.error("Failed to delete supplier", deleteTask.getException());
                showError("Failed to delete supplier", deleteTask.getException().getMessage());
            });

            new Thread(deleteTask).start();
        }
    }

    /**
     * Create a dialog for adding or editing a supplier.
     */
    private Dialog<Supplier> createSupplierDialog(Supplier supplier) {
        Dialog<Supplier> dialog = new Dialog<>();
        dialog.setTitle(supplier == null ? "Add Supplier" : "Edit Supplier");
        dialog.setHeaderText(supplier == null ? "Enter supplier details" : "Update supplier details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create form fields
        JFXTextField nameField = new JFXTextField();
        nameField.setPromptText("Supplier Name");
        if (supplier != null) nameField.setText(supplier.getSupplierName());

        JFXTextField contactPersonField = new JFXTextField();
        contactPersonField.setPromptText("Contact Person");
        if (supplier != null) contactPersonField.setText(supplier.getContactPerson());

        JFXTextField phoneField = new JFXTextField();
        phoneField.setPromptText("Phone Number");
        if (supplier != null) phoneField.setText(supplier.getPhone());

        TextArea addressField = new TextArea();
        addressField.setPromptText("Address");
        addressField.setPrefRowCount(3);
        if (supplier != null) addressField.setText(supplier.getAddress());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Supplier Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Contact Person:"), 0, 1);
        grid.add(contactPersonField, 1, 1);
        grid.add(new Label("Phone:"), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(new Label("Address:"), 0, 3);
        grid.add(addressField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Request focus on name field
        Platform.runLater(nameField::requestFocus);

        // Convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Supplier result = supplier != null ? supplier : new Supplier();
                result.setSupplierName(nameField.getText());
                result.setContactPerson(contactPersonField.getText());
                result.setPhone(phoneField.getText());
                result.setAddress(addressField.getText());
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
            showError("Navigation Error", "Failed to return to dashboard");
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
