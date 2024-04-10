module org.zenith.app {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens org.zenith.app to javafx.fxml;
    exports org.zenith.app;

    requires org.zenith.lib;
}