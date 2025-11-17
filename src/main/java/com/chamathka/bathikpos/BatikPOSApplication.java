package com.chamathka.bathikpos;

import com.chamathka.bathikpos.util.HibernateUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main Application class for Batik POS System.
 * This is the entry point for the JavaFX application.
 */
public class BatikPOSApplication extends Application {

    private static final Logger logger = LoggerFactory.getLogger(BatikPOSApplication.class);
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        try {
            logger.info("Starting Batik POS System...");
            primaryStage = stage;

            // Load the login view
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/LoginView.fxml"));

            Scene scene = new Scene(root);
            stage.setTitle("Batik POS System - Login");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();

            logger.info("Batik POS System started successfully");

        } catch (Exception e) {
            logger.error("Failed to start application", e);
            throw new RuntimeException("Failed to start application", e);
        }
    }

    @Override
    public void stop() {
        logger.info("Shutting down Batik POS System...");
        HibernateUtil.shutdown();
        logger.info("Batik POS System shut down successfully");
    }

    /**
     * Get the primary stage of the application.
     * @return The primary stage
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Navigate to a different view.
     * @param fxmlPath Path to the FXML file (relative to resources)
     * @param title Window title
     */
    public static void navigateTo(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(
                BatikPOSApplication.class.getResource(fxmlPath));
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
        } catch (Exception e) {
            logger.error("Failed to navigate to: {}", fxmlPath, e);
            throw new RuntimeException("Navigation failed: " + e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
