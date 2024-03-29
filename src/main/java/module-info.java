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
    requires org.jetbrains.annotations;

    opens ru.nsu.gemuev.net4.util to com.google.guice;

    opens ru.nsu.gemuev.net4 to javafx.fxml;
    exports ru.nsu.gemuev.net4;
    exports ru.nsu.gemuev.net4.controllers;
    exports ru.nsu.gemuev.net4.controllers.uievents;
    opens ru.nsu.gemuev.net4.controllers to javafx.fxml;
    opens ru.nsu.gemuev.net4.model;
    exports ru.nsu.gemuev.net4.model;
    exports ru.nsu.gemuev.net4.model.game;
    exports ru.nsu.gemuev.net4.net;
    opens ru.nsu.gemuev.net4.model.game;
    exports ru.nsu.gemuev.net4.model.communication;
    opens ru.nsu.gemuev.net4.model.communication;
    exports ru.nsu.gemuev.net4.model.ports;
    opens ru.nsu.gemuev.net4.model.ports;
    exports ru.nsu.gemuev.net4.model.game.presentation;
    opens ru.nsu.gemuev.net4.model.game.presentation;
}