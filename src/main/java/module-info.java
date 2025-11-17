module com.chamathka.bathikpos {
    // JavaFX modules
    requires javafx.controls;
    requires javafx.fxml;

    // JFoenix Material Design
    requires com.jfoenix;

    // Persistence
    requires jakarta.persistence;
    requires jakarta.validation;
    requires org.hibernate.orm.core;
    requires com.zaxxer.hikari;
    requires java.sql;
    requires java.naming;

    // Security
    requires spring.security.crypto;

    // Logging
    requires org.slf4j;

    // Bootstrap FX
    requires org.kordamp.bootstrapfx.core;

    // Open packages for JavaFX FXML reflection
    opens com.chamathka.bathikpos to javafx.fxml;
    opens com.chamathka.bathikpos.controller to javafx.fxml;

    // Open entity packages for Hibernate reflection
    opens com.chamathka.bathikpos.entity to org.hibernate.orm.core;

    // Export main application package
    exports com.chamathka.bathikpos;
    exports com.chamathka.bathikpos.controller;
}