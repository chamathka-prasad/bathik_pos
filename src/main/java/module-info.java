module com.chamathka.bathikpos {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens com.chamathka.bathikpos to javafx.fxml;
    exports com.chamathka.bathikpos;
}