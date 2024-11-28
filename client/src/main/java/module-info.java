module org.client.demo {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens org.client to javafx.fxml;
    exports org.client;
}