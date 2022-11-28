package ru.nsu.gemuev.net4.controllers;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import ru.nsu.gemuev.net4.controllers.uievents.ShowMainViewEvent;
import ru.nsu.gemuev.net4.model.Model;
import ru.nsu.gemuev.net4.model.game.GameConfig;

public class ConfigViewController {

    private final EventBus eventBus;
    private final Model model;

    @FXML
    private TextField playerName;
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
    public ConfigViewController(EventBus eventBus, Model model){
        this.eventBus = eventBus;
        this.model = model;
    }

    @FXML
    protected void onStartGameClick(){
        GameConfig gameConfig = new GameConfig(
                Integer.parseInt(width.getText()),
                Integer.parseInt(height.getText()),
                Integer.parseInt(foodStatic.getText()),
                Integer.parseInt(delay.getText()));

        model.newGame(gameConfig, playerName.getText(), gameTitle.getText());
    }

    @FXML
    protected void onBackClick() {
        eventBus.post(new ShowMainViewEvent());
    }
}
