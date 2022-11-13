package ru.nsu.gemuev.net4.controllers;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import ru.nsu.gemuev.net4.controllers.uievents.ShowGameViewEvent;
import ru.nsu.gemuev.net4.controllers.uievents.ShowMainViewEvent;

public class ConfigViewController {

    private final EventBus eventBus;

    @FXML
    private TextField gameTitle;
    @FXML
    private TextField width;
    @FXML
    private TextField height;
    @FXML
    private TextField foodStatic;
    @FXML
    private TextField delay;

    @Inject
    public ConfigViewController(EventBus eventBus){
        this.eventBus = eventBus;
    }

    @FXML
    protected void onStartGameClick(){
        eventBus.post(new ShowGameViewEvent());
    }

    @FXML
    protected void onBackClick() {
        eventBus.post(new ShowMainViewEvent());
    }
}
