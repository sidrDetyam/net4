package ru.nsu.gemuev.net4.controllers;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import lombok.NonNull;
import lombok.SneakyThrows;
import ru.nsu.gemuev.net4.controllers.uievents.ShowMainViewEvent;
import ru.nsu.gemuev.net4.model.Model;
import ru.nsu.gemuev.net4.util.PropertyGetter;

import java.net.InetAddress;
import java.net.URL;
import java.util.ResourceBundle;

public class SettingsViewController implements Initializable {
    private final Model model;
    private final EventBus uiEventBus;

    @FXML
    private TextField mAddressField;
    @FXML
    private TextField mAddressPort;

    @Inject
    public SettingsViewController(@NonNull Model model, @NonNull EventBus uieventBus){
        this.model = model;
        this.uiEventBus = uieventBus;
    }

    @FXML
    public void onBackClicked() {
        uiEventBus.post(new ShowMainViewEvent());
    }

    @FXML
    @SneakyThrows
    public void onAcceptClicked() {
        InetAddress mAddress = InetAddress.getByName(PropertyGetter.getPropertyOrThrow("multicast_address"));
        int mPort = Integer.parseInt(PropertyGetter.getPropertyOrThrow("multicast_port"));
        model.setMulticastAddressAndPort(mAddress, mPort);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mAddressField.setText(PropertyGetter.getPropertyOrThrow("multicast_address"));
        mAddressPort.setText(PropertyGetter.getPropertyOrThrow("multicast_port"));
    }
}
