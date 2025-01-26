module org.zenith.app {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens org.zenith.app to javafx.fxml;
    exports org.zenith.app;
    exports org.zenith.app.controllers;
    opens org.zenith.app.controllers to javafx.fxml;

    requires org.zenith.lib;
    requires org.zenith.models;

    requires java.sql;
}