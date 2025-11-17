module com.chamathka.bathikpos {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires jakarta.persistence;
    requires jakarta.validation;
    requires org.slf4j;

    opens com.chamathka.bathikpos to javafx.fxml;
    exports com.chamathka.bathikpos;
}