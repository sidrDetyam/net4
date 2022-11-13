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
import lombok.extern.log4j.Log4j2;
import ru.nsu.gemuev.net4.controllers.uievents.ShowConfigViewEvent;
import ru.nsu.gemuev.net4.controllers.uievents.ShowGameViewEvent;
import ru.nsu.gemuev.net4.model.game.Game;
import ru.nsu.gemuev.net4.model.game.Snake;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Log4j2
public class GameViewController implements Initializable {

    private final EventBus eventBus;
    @FXML
    private Canvas canvas;

    private static int WIDTH = 600;
    private static int HEIGHT = 600;

    private static int ROWS = 20;
    private static int COLS = 20;
    private static int SQUARE_SIZE = WIDTH / ROWS;

    private Game game;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Inject
    public GameViewController(EventBus eventBus){
        this.eventBus = eventBus;
        game = new Game(ROWS, COLS, 1);
        scheduler.scheduleAtFixedRate(this::nextState, 0, 200, TimeUnit.MILLISECONDS);
    }

    @FXML
    protected void onBackClicked(ActionEvent actionEvent) {
        eventBus.post(new ShowConfigViewEvent());
    }

    @FXML
    protected void onKeyPressed(KeyEvent keyEvent) {
        Snake.Direction d = Snake.Direction.UP;
        switch (keyEvent.getCode()){
            case W -> d = Snake.Direction.UP;
            case A -> d = Snake.Direction.LEFT;
            case S -> d = Snake.Direction.DOWN;
            case D -> d = Snake.Direction.RIGHT;
        }
        game.getSnake().getBody().get(0).setDirection(d);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        drawBackground();
    }

    @Subscribe
    public void onGameEvent(ShowGameViewEvent __){
        //scheduler.shutdown();
        Platform.runLater(()-> {
            game = new Game(ROWS, COLS, 1);
        });
    }

    public void nextState(){
        Platform.runLater(() -> {
            game.nextState();
            drawBackground();
        });
    }

    private void drawBackground() {
        var gc = canvas.getGraphicsContext2D();

        for (int i = 0; i < game.getSizeY(); i++) {
            for (int j = 0; j < game.getSizeX(); j++) {
                switch (game.getCells()[i][j]) {
                    case FREE -> gc.setFill((i+j) % 2 == 0? Color.web("AAD751") : Color.web("A2D149"));
                    case FOOD -> gc.setFill(Color.RED);
                    case SNAKE_BODY -> gc.setFill(Color.BLUE);
                }
                gc.fillRect(i * SQUARE_SIZE, j * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
            }
        }
    }
}
