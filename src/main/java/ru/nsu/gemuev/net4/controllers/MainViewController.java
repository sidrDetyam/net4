package ru.nsu.gemuev.net4.controllers;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import ru.nsu.gemuev.net4.controllers.events.ShowConfigViewEvent;
import ru.nsu.gemuev.net4.controllers.events.ShowMainViewEvent;

public class MainViewController {

    private final EventBus eventBus;

    @Inject
    public MainViewController(EventBus eventBus){
        this.eventBus = eventBus;
    }

    @FXML
    protected void onNewGameClick() {
        eventBus.post(new ShowConfigViewEvent());
    }
}