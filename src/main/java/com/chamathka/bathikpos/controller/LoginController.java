package com.chamathka.bathikpos.controller;

import com.chamathka.bathikpos.model.User;
import com.chamathka.bathikpos.service.UserService;
import com.chamathka.bathikpos.util.SessionManager;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

/**
 * Controller for the Login screen
 * Handles user authentication and navigation to main dashboard
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

    private final UserService userService;

    public LoginController() {
        this.userService = new UserService();
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
     * Handle login button click
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
        new Thread(() -> {
            try {
                Optional<User> userOptional = userService.authenticate(username, password);

                Platform.runLater(() -> {
                    if (userOptional.isPresent()) {
                        User user = userOptional.get();
                        logger.info("Login successful for user: {} (Role: {})",
                                user.getUsername(), user.getRole());

                        // Set the current user in session
                        SessionManager.getInstance().setCurrentUser(user);

                        // Navigate to main dashboard
                        navigateToDashboard();
                    } else {
                        logger.warn("Login failed for username: {}", username);
                        showError("Invalid username or password");
                        passwordField.clear();
                        passwordField.requestFocus();
                    }

                    // Re-enable login button
                    loginButton.setDisable(false);
                    loginButton.setText("LOGIN");
                });

            } catch (Exception e) {
                logger.error("Error during login process", e);
                Platform.runLater(() -> {
                    showError("An error occurred during login. Please try again.");
                    loginButton.setDisable(false);
                    loginButton.setText("LOGIN");
                });
            }
        }).start();
    }

    /**
     * Show error message to user
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    /**
     * Navigate to the main dashboard after successful login
     */
    private void navigateToDashboard() {
        try {
            // Load the main dashboard view
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/chamathka/bathikpos/MainDashboardView.fxml")
            );
            Parent root = loader.load();

            // Get the current stage
            Stage stage = (Stage) loginButton.getScene().getWindow();

            // Create and set the new scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Batik POS - Dashboard");
            stage.setMaximized(true);

            logger.info("Navigated to main dashboard");

        } catch (IOException e) {
            logger.error("Error loading main dashboard view", e);
            showError("Error loading dashboard. Please contact support.");
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