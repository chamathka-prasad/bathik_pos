package com.chamathka.bathikpos.controller;

import com.chamathka.bathikpos.BatikPOSApplication;
import com.chamathka.bathikpos.entity.Product;
import com.chamathka.bathikpos.entity.ProductVariant;
import com.chamathka.bathikpos.entity.Supplier;
import com.chamathka.bathikpos.service.ProductService;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Controller for Product Management.
 * Handles master products and their variants (Admin only).
 */
public class ProductManagementController {

    private static final Logger logger = LoggerFactory.getLogger(ProductManagementController.class);

    @FXML private JFXTextField searchField;
    @FXML private TableView<Product> productTable;
    @FXML private Label statusLabel;

    private final ProductService productService;
    private final SupplierService supplierService;
    private final ObservableList<Product> productList;

    public ProductManagementController() {
        this.productService = new ProductService();
        this.supplierService = new SupplierService();
        this.productList = FXCollections.observableArrayList();
    }

    @FXML
    private void initialize() {
        logger.info("Product Management initialized");

        // Set up supplier column
        TableColumn<Product, Void> supplierColumn = (TableColumn<Product, Void>) productTable.getColumns().get(3);
        supplierColumn.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    Product product = getTableRow().getItem();
                    setText(product.getSupplier() != null ? product.getSupplier().getSupplierName() : "N/A");
                }
            }
        });

        // Set up variant count column
        TableColumn<Product, Void> variantCountColumn = (TableColumn<Product, Void>) productTable.getColumns().get(4);
        variantCountColumn.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    Product product = getTableRow().getItem();
                    int variantCount = productService.getVariantsByProductId(product.getProductId()).size();
                    setText(String.valueOf(variantCount));
                    setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");
                }
            }
        });

        // Set up action column
        TableColumn<Product, Void> actionColumn = (TableColumn<Product, Void>) productTable.getColumns().get(5);
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final JFXButton manageVariantsBtn = new JFXButton("Manage Variants");
            private final JFXButton editBtn = new JFXButton("Edit");
            private final JFXButton deleteBtn = new JFXButton("Delete");
            private final HBox container = new HBox(5, manageVariantsBtn, editBtn, deleteBtn);

            {
                manageVariantsBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 3; -fx-font-size: 11px;");
                editBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 3; -fx-font-size: 11px;");
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 3; -fx-font-size: 11px;");
                container.setAlignment(Pos.CENTER);

                manageVariantsBtn.setOnAction(e -> handleManageVariants(getTableRow().getItem()));
                editBtn.setOnAction(e -> handleEditProduct(getTableRow().getItem()));
                deleteBtn.setOnAction(e -> handleDeleteProduct(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });

        // Bind table to observable list
        productTable.setItems(productList);

        // Load products
        loadProducts();
    }

    private void loadProducts() {
        Task<List<Product>> loadTask = new Task<>() {
            @Override
            protected List<Product> call() {
                return productService.getAllProducts();
            }
        };

        loadTask.setOnSucceeded(e -> {
            productList.clear();
            productList.addAll(loadTask.getValue());
            statusLabel.setText(String.format("Showing %d products", productList.size()));
            logger.info("Loaded {} products", productList.size());
        });

        loadTask.setOnFailed(e -> {
            logger.error("Failed to load products", loadTask.getException());
            showError("Failed to load products", loadTask.getException().getMessage());
        });

        new Thread(loadTask).start();
    }

    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().trim();

        if (searchTerm.isEmpty()) {
            loadProducts();
            return;
        }

        Task<List<Product>> searchTask = new Task<>() {
            @Override
            protected List<Product> call() {
                return productService.searchProducts(searchTerm);
            }
        };

        searchTask.setOnSucceeded(e -> {
            productList.clear();
            productList.addAll(searchTask.getValue());
            statusLabel.setText(String.format("Found %d products matching '%s'", productList.size(), searchTerm));
        });

        new Thread(searchTask).start();
    }

    @FXML
    private void handleAddProduct() {
        Dialog<Product> dialog = createProductDialog(null);
        Optional<Product> result = dialog.showAndWait();

        result.ifPresent(product -> {
            Task<Product> saveTask = new Task<>() {
                @Override
                protected Product call() {
                    return productService.createProduct(product);
                }
            };

            saveTask.setOnSucceeded(e -> {
                loadProducts();
                showInfo("Success", "Product added successfully");
                logger.info("Product created: {}", product.getName());
            });

            saveTask.setOnFailed(e -> {
                logger.error("Failed to create product", saveTask.getException());
                showError("Failed to add product", saveTask.getException().getMessage());
            });

            new Thread(saveTask).start();
        });
    }

    private void handleEditProduct(Product product) {
        if (product == null) return;

        Dialog<Product> dialog = createProductDialog(product);
        Optional<Product> result = dialog.showAndWait();

        result.ifPresent(updatedProduct -> {
            Task<Product> updateTask = new Task<>() {
                @Override
                protected Product call() {
                    return productService.updateProduct(updatedProduct);
                }
            };

            updateTask.setOnSucceeded(e -> {
                loadProducts();
                showInfo("Success", "Product updated successfully");
            });

            updateTask.setOnFailed(e -> {
                logger.error("Failed to update product", updateTask.getException());
                showError("Failed to update product", updateTask.getException().getMessage());
            });

            new Thread(updateTask).start();
        });
    }

    private void handleDeleteProduct(Product product) {
        if (product == null) return;

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Product");
        confirmation.setHeaderText("Are you sure you want to delete this product?");
        confirmation.setContentText(product.getName() + "\n\nThis will also delete all variants!");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Task<Void> deleteTask = new Task<>() {
                @Override
                protected Void call() {
                    productService.deleteProduct(product);
                    return null;
                }
            };

            deleteTask.setOnSucceeded(e -> {
                loadProducts();
                showInfo("Success", "Product deleted successfully");
            });

            deleteTask.setOnFailed(e -> {
                logger.error("Failed to delete product", deleteTask.getException());
                showError("Failed to delete product", deleteTask.getException().getMessage());
            });

            new Thread(deleteTask).start();
        }
    }

    private void handleManageVariants(Product product) {
        if (product == null) return;

        logger.info("Opening variant management for: {}", product.getName());

        // Create variant management dialog
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Manage Variants - " + product.getName());
        dialog.setHeaderText("Product Variants");

        // Create table for variants
        TableView<ProductVariant> variantTable = new TableView<>();
        ObservableList<ProductVariant> variants = FXCollections.observableArrayList();

        // Set up columns
        TableColumn<ProductVariant, String> codeCol = new TableColumn<>("SKU");
        codeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getItemCode()));
        codeCol.setPrefWidth(150);

        TableColumn<ProductVariant, String> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getAttributeSize()));
        sizeCol.setPrefWidth(100);

        TableColumn<ProductVariant, String> colorCol = new TableColumn<>("Color");
        colorCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getAttributeColor()));
        colorCol.setPrefWidth(100);

        TableColumn<ProductVariant, String> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.format("Rs. %.2f", data.getValue().getSellingPrice())));
        priceCol.setPrefWidth(100);

        TableColumn<ProductVariant, String> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getQuantityInStock())));
        stockCol.setPrefWidth(80);

        TableColumn<ProductVariant, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(150);
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final JFXButton deleteBtn = new JFXButton("Delete");

            {
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");
                deleteBtn.setOnAction(e -> {
                    ProductVariant variant = getTableRow().getItem();
                    if (variant != null) {
                        Task<Void> deleteTask = new Task<>() {
                            @Override
                            protected Void call() {
                                productService.deleteVariant(variant);
                                return null;
                            }
                        };
                        deleteTask.setOnSucceeded(ev -> {
                            variants.remove(variant);
                            showInfo("Success", "Variant deleted");
                        });
                        new Thread(deleteTask).start();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });

        variantTable.getColumns().addAll(codeCol, sizeCol, colorCol, priceCol, stockCol, actionsCol);
        variantTable.setItems(variants);

        // Load variants
        Task<List<ProductVariant>> loadVariantsTask = new Task<>() {
            @Override
            protected List<ProductVariant> call() {
                return productService.getVariantsByProductId(product.getProductId());
            }
        };
        loadVariantsTask.setOnSucceeded(e -> variants.addAll(loadVariantsTask.getValue()));
        new Thread(loadVariantsTask).start();

        // Add variant button
        JFXButton addVariantBtn = new JFXButton("+ Add Variant");
        addVariantBtn.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white;");
        addVariantBtn.setOnAction(e -> {
            Dialog<ProductVariant> variantDialog = createVariantDialog(product, null);
            Optional<ProductVariant> result = variantDialog.showAndWait();
            result.ifPresent(variant -> {
                Task<ProductVariant> saveTask = new Task<>() {
                    @Override
                    protected ProductVariant call() {
                        return productService.createVariant(variant);
                    }
                };
                saveTask.setOnSucceeded(ev -> {
                    variants.add(saveTask.getValue());
                    showInfo("Success", "Variant added successfully");
                });
                new Thread(saveTask).start();
            });
        });

        VBox content = new VBox(10, variantTable, addVariantBtn);
        content.setPrefSize(700, 400);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();

        loadProducts(); // Refresh product list
    }

    private Dialog<Product> createProductDialog(Product product) {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle(product == null ? "Add Product" : "Edit Product");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        JFXTextField nameField = new JFXTextField();
        nameField.setPromptText("Product Name");
        if (product != null) nameField.setText(product.getName());

        JFXTextField categoryField = new JFXTextField();
        categoryField.setPromptText("Category");
        if (product != null) categoryField.setText(product.getCategory());

        ComboBox<Supplier> supplierCombo = new ComboBox<>();
        Task<List<Supplier>> loadSuppliersTask = new Task<>() {
            @Override
            protected List<Supplier> call() {
                return supplierService.getAllSuppliers();
            }
        };
        loadSuppliersTask.setOnSucceeded(e -> {
            supplierCombo.getItems().addAll(loadSuppliersTask.getValue());
            if (product != null && product.getSupplier() != null) {
                supplierCombo.setValue(product.getSupplier());
            }
        });
        new Thread(loadSuppliersTask).start();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Product Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Category:"), 0, 1);
        grid.add(categoryField, 1, 1);
        grid.add(new Label("Supplier:"), 0, 2);
        grid.add(supplierCombo, 1, 2);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(nameField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Product result = product != null ? product : new Product();
                result.setName(nameField.getText());
                result.setCategory(categoryField.getText());
                result.setSupplier(supplierCombo.getValue());
                return result;
            }
            return null;
        });

        return dialog;
    }

    private Dialog<ProductVariant> createVariantDialog(Product product, ProductVariant variant) {
        Dialog<ProductVariant> dialog = new Dialog<>();
        dialog.setTitle("Add Product Variant");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        JFXTextField skuField = new JFXTextField();
        skuField.setPromptText("SKU (e.g., SHIRT-RED-M)");

        JFXTextField sizeField = new JFXTextField();
        sizeField.setPromptText("Size (e.g., S, M, L, XL)");

        JFXTextField colorField = new JFXTextField();
        colorField.setPromptText("Color");

        JFXTextField priceField = new JFXTextField();
        priceField.setPromptText("Selling Price");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("SKU:"), 0, 0);
        grid.add(skuField, 1, 0);
        grid.add(new Label("Size:"), 0, 1);
        grid.add(sizeField, 1, 1);
        grid.add(new Label("Color:"), 0, 2);
        grid.add(colorField, 1, 2);
        grid.add(new Label("Price:"), 0, 3);
        grid.add(priceField, 1, 3);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(skuField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                ProductVariant result = new ProductVariant();
                result.setProduct(product);
                result.setItemCode(skuField.getText());
                result.setAttributeSize(sizeField.getText());
                result.setAttributeColor(colorField.getText());
                result.setSellingPrice(new BigDecimal(priceField.getText()));
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
