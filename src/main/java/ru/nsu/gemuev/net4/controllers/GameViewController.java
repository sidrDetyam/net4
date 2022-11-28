package ru.nsu.gemuev.net4.controllers;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import ru.nsu.gemuev.net4.controllers.uievents.GameStateChanged;
import ru.nsu.gemuev.net4.controllers.uievents.ShowGameViewEvent;
import ru.nsu.gemuev.net4.controllers.uievents.ShowMainViewEvent;
import ru.nsu.gemuev.net4.model.Model;
import ru.nsu.gemuev.net4.model.game.Direction;
import ru.nsu.gemuev.net4.model.game.GameState;
import ru.nsu.gemuev.net4.model.game.Player;
import ru.nsu.gemuev.net4.model.game.presentation.FieldPresenter;

import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;

@Log4j2
@SuppressWarnings("unused")
public class GameViewController implements Initializable {

    private static final int CANVAS_SIZE = 500;

    private final Model model;
    private final EventBus eventBus;

    @FXML
    public ListView<Player> topList;
    @FXML
    private Canvas canvas;

    @Inject
    public GameViewController(@NonNull EventBus eventBus, @NonNull Model model){
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

    private static final Color[] colors = {Color.BLUE, Color.PURPLE, Color.BLACK, Color.TEAL, Color.TAN, Color.SIENNA};

    private static Color getPlayerColor(int id){
        return colors[Math.abs(id) % colors.length];
    }

    static class ColorRectCell extends ListCell<Player> {
        @Override
        public void updateItem(Player item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null && !empty) {
                Rectangle rect = new Rectangle(10, 10);
                rect.setFill(getPlayerColor(item.getId()));
                setGraphic(rect);
                setText(item.toString());
            }
            else{
                setGraphic(null);
                setText(null);
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        topList.setCellFactory(list -> new ColorRectCell());
        topList.setMouseTransparent(true);
        topList.setFocusTraversable(false);
    }

    @Subscribe
    public void onGameEvent(ShowGameViewEvent __){

    }

    @Subscribe
    public void onNextState(@NonNull GameStateChanged newState){
        Platform.runLater(() -> {
            topList.getItems().clear();
            topList.getItems().addAll(newState.getGameState().getPlayers().stream()
                    .sorted(Comparator.comparingInt(Player::getScore).reversed())
                    .toList());
            drawBackground(newState.getGameState());
        });
    }


    private void drawBackground(@NonNull GameState gameState) {
        var gc = canvas.getGraphicsContext2D();
        final int sizeX = gameState.getGameConfig().width();
        final int sizeY = gameState.getGameConfig().height();

        final double CELL_SIZE = Math.min((double) CANVAS_SIZE / sizeX, (double) CANVAS_SIZE / sizeY);
        var field = FieldPresenter.fieldPresentation(gameState);

        for (int i = 0; i < sizeX; i++) {
            for (int j = 0; j < sizeY; j++) {
                switch (field[i][j].getType()) {
                    case FREE -> gc.setFill((i+j) % 2 == 0? Color.web("AAD751") : Color.web("A2D149"));
                    case FOOD -> gc.setFill(Color.RED);
                    case SNAKE_BODY -> gc.setFill(getPlayerColor(field[i][j].getSnakeId()));
                }
                gc.fillRect(i * CELL_SIZE, j * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
    }
}
