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
 * Main Application class for Batik POS System
 * Entry point for the JavaFX application
 */
public class HelloApplication extends Application {

    private static final Logger logger = LoggerFactory.getLogger(HelloApplication.class);

    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Starting Batik POS System...");

            // Load the Login view
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/chamathka/bathikpos/LoginView.fxml")
            );
            Parent root = loader.load();

            // Create the scene
            Scene scene = new Scene(root);

            // Configure the primary stage
            primaryStage.setTitle("Batik POS - Login");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);

            // Center the window on screen
            primaryStage.centerOnScreen();

            // Show the stage
            primaryStage.show();

            logger.info("Batik POS System started successfully");

        } catch (Exception e) {
            logger.error("Error starting application", e);
            System.exit(1);
        }
    }

    @Override
    public void stop() {
        logger.info("Shutting down Batik POS System...");

        // Close Hibernate SessionFactory
        HibernateUtil.shutdown();

        logger.info("Batik POS System shutdown complete");
    }

    public static void main(String[] args) {
        launch(args);
    }
}