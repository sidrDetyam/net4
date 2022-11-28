package ru.nsu.gemuev.net4.model.game.presentation;

import lombok.NonNull;
import ru.nsu.gemuev.net4.model.game.GameState;

public class FieldPresenter {
    private FieldPresenter() {
    }

    public static Cell[][] fieldPresentation(@NonNull GameState state) {
        final int width = state.getGameConfig().width();
        final int height = state.getGameConfig().height();

        var field = new Cell[width][height];
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                field[i][j] = new Cell(CellType.FREE, -1);
            }
        }

        state.getSnakes().forEach(snake ->
                snake.getBody().forEach(segment ->
                        field[segment.x()][segment.y()] = new Cell(CellType.SNAKE_BODY, snake.getPlayerId())));

        state.getFoods().forEach(food ->
                field[food.x()][food.y()] = new Cell(CellType.FOOD, -1));

        return field;
    }
}
