package ru.nsu.gemuev.net4.controllers;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import lombok.NonNull;
import ru.nsu.gemuev.net4.controllers.uievents.ShowConfigViewEvent;
import ru.nsu.gemuev.net4.controllers.uievents.ShowGamesList;
import ru.nsu.gemuev.net4.model.Model;

public class MainViewController {

    private final EventBus eventBus;
    private final Model model;

    @Inject
    public MainViewController(@NonNull Model model, @NonNull EventBus eventBus){
        this.eventBus = eventBus;
        this.model = model;
    }

    @FXML
    protected void onNewGameClick() {
        eventBus.post(new ShowConfigViewEvent());
    }

    @FXML
    protected void onJoinClick(){
        eventBus.post(new ShowGamesList());
    }

    @FXML
    public void exit() {
        model.exit();
        Platform.exit();
    }
}