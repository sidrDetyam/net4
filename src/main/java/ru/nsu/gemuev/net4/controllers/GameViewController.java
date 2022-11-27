package ru.nsu.gemuev.net4.controllers;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import ru.nsu.gemuev.net4.controllers.uievents.GameStateChanged;
import ru.nsu.gemuev.net4.controllers.uievents.ShowConfigViewEvent;
import ru.nsu.gemuev.net4.controllers.uievents.ShowGameViewEvent;
import ru.nsu.gemuev.net4.controllers.uievents.ShowMainViewEvent;
import ru.nsu.gemuev.net4.model.Model;
import ru.nsu.gemuev.net4.model.game.Direction;
import ru.nsu.gemuev.net4.model.game.GameState;

import java.net.URL;
import java.util.ResourceBundle;

@Log4j2
public class GameViewController implements Initializable {

    private final Model model;
    private final EventBus eventBus;
    @FXML
    private Canvas canvas;
    private static int WIDTH = 600;

    @Inject
    public GameViewController(EventBus eventBus, Model model){
        this.eventBus = eventBus;
        this.model = model;
    }

    @FXML
    protected void onBackClicked(ActionEvent actionEvent) {
        model.stopGame();
        eventBus.post(new ShowMainViewEvent());
    }

    @FXML
    protected void onKeyPressed(KeyEvent keyEvent) {
        Direction d = null;
        switch (keyEvent.getCode()){
            case W -> d = Direction.UP;
            case A -> d = Direction.LEFT;
            case S -> d = Direction.DOWN;
            case D -> d = Direction.RIGHT;
        }

        if(d != null){
            model.steer(d);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //drawBackground();
    }

    @Subscribe
    public void onGameEvent(ShowGameViewEvent __){

    }

    @Subscribe
    public void onNextState(@NonNull GameStateChanged newState){
        Platform.runLater(() -> drawBackground(newState.getGameState()));
    }

    private void drawBackground(@NonNull GameState gameState) {
        var gc = canvas.getGraphicsContext2D();
        final int SQUARE_SIZE = WIDTH / gameState.getGameConfig().width();
        final int sizeX = gameState.getGameConfig().width();
        final int sizeY = gameState.getGameConfig().height();
        var field = gameState.fieldPresentation();

        for (int i = 0; i < sizeX; i++) {
            for (int j = 0; j < sizeY; j++) {
                switch (field[i][j]) {
                    case FREE -> gc.setFill((i+j) % 2 == 0? Color.web("AAD751") : Color.web("A2D149"));
                    case FOOD -> gc.setFill(Color.RED);
                    case SNAKE_BODY -> gc.setFill(Color.BLUE);
                }
                gc.fillRect(i * SQUARE_SIZE, j * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
            }
        }
    }
}
