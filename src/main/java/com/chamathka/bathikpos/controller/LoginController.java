package com.chamathka.bathikpos.controller;

import com.chamathka.bathikpos.BatikPOSApplication;
import com.chamathka.bathikpos.entity.User;
import com.chamathka.bathikpos.service.AuthenticationService;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for the Login screen.
 * Handles user authentication and navigation to main dashboard.
 *
 * This controller contains NO business logic - it only calls AuthenticationService.
 */
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @FXML
    private JFXTextField usernameField;

    @FXML
    private JFXPasswordField passwordField;

    @FXML
    private JFXButton loginButton;

    @FXML
    private Label errorLabel;

    private final AuthenticationService authService;

    public LoginController() {
        this.authService = new AuthenticationService();
    }

    @FXML
    public void initialize() {
        logger.info("Login screen initialized");

        // Set focus to username field
        Platform.runLater(() -> usernameField.requestFocus());

        // Enable Enter key to trigger login from password field
        passwordField.setOnKeyPressed(this::handleKeyPressed);
        usernameField.setOnKeyPressed(this::handleKeyPressed);

        // Add hover effect to login button
        loginButton.setOnMouseEntered(e ->
                loginButton.setStyle("-fx-background-color: #4f46e5; -fx-background-radius: 8;"));
        loginButton.setOnMouseExited(e ->
                loginButton.setStyle("-fx-background-color: #6366f1; -fx-background-radius: 8;"));
    }

    /**
     * Handle key press events (Enter key to login)
     */
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleLogin();
        }
    }

    /**
     * Handle login button click.
     * Performs authentication in a background thread to keep UI responsive.
     */
    @FXML
    public void handleLogin() {
        // Clear previous error message
        errorLabel.setVisible(false);

        // Get input values
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Validate input
        if (username == null || username.trim().isEmpty()) {
            showError("Please enter your username");
            usernameField.requestFocus();
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            showError("Please enter your password");
            passwordField.requestFocus();
            return;
        }

        // Disable login button to prevent multiple clicks
        loginButton.setDisable(true);
        loginButton.setText("Logging in...");

        // Perform authentication in background thread
        Task<User> loginTask = new Task<>() {
            @Override
            protected User call() {
                return authService.login(username, password);
            }
        };

        loginTask.setOnSucceeded(event -> {
            User user = loginTask.getValue();
            loginButton.setDisable(false);
            loginButton.setText("LOGIN");

            if (user != null) {
                logger.info("Login successful for user: {} ({})", user.getUsername(), user.getRole());
                navigateToDashboard();
            } else {
                logger.warn("Login failed for username: {}", username);
                showError("Invalid username or password");
                passwordField.clear();
                passwordField.requestFocus();
            }
        });

        loginTask.setOnFailed(event -> {
            loginButton.setDisable(false);
            loginButton.setText("LOGIN");
            Throwable error = loginTask.getException();
            logger.error("Login error", error);
            showError("Login failed: " + error.getMessage());
        });

        // Start the background task
        new Thread(loginTask).start();
    }

    /**
     * Show error message to user
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    /**
     * Navigate to the main dashboard after successful login.
     */
    private void navigateToDashboard() {
        try {
            BatikPOSApplication.navigateTo("/fxml/MainDashboard.fxml", "Batik POS - Dashboard");
            logger.info("Navigated to main dashboard");
        } catch (Exception e) {
            logger.error("Failed to navigate to dashboard", e);
            showError("Failed to load dashboard: " + e.getMessage());
            loginButton.setDisable(false);
        }
    }

    /**
     * Handle exit/close application
     */
    @FXML
    public void handleExit() {
        logger.info("Application exit requested from login screen");
        Platform.exit();
    }
}