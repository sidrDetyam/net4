module ru.nsu.gemuev.net4 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.google.protobuf;
    requires lombok;
    requires com.google.guice;
    requires org.apache.logging.log4j;
    requires com.google.common;

    opens ru.nsu.gemuev.net4.util to com.google.guice;

    opens ru.nsu.gemuev.net4 to javafx.fxml;
    exports ru.nsu.gemuev.net4;
    exports ru.nsu.gemuev.net4.controllers;
    exports ru.nsu.gemuev.net4.controllers.events;
    opens ru.nsu.gemuev.net4.controllers to javafx.fxml;
}