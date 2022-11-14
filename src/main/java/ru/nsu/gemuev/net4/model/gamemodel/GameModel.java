package ru.nsu.gemuev.net4.model.gamemodel;

import lombok.Getter;
import lombok.NonNull;
import ru.nsu.gemuev.net4.SnakesProto;

import java.util.Optional;

public class GameModel {

    @Getter
    private SnakesProto.GameState gameState;
    @Getter
    private final SnakesProto.GameConfig gameConfig;

    public GameModel(@NonNull SnakesProto.GameConfig gameConfig) {
        this.gameConfig = gameConfig;
//        gameState = SnakesProto.GameState.newBuilder()
//                .setStateOrder(0)
//                .set
    }

    private int id = 0;

    private int nextId() {
        return id++;
    }

    void nextState() {

    }

    public enum Cell {
        SNAKE_BODY, FOOD, FREE
    }

    public Cell[][] fieldPresentation() {
        var field = new Cell[gameConfig.getWidth()][gameConfig.getHeight()];
        for (int i = 0; i < gameConfig.getWidth(); ++i) {
            for (int j = 0; j < gameConfig.getHeight(); ++i) {
                field[i][j] = Cell.FREE;
            }
        }

        for (var snake : gameState.getSnakesList()) {
            int x = snake.getPoints(0).getX();
            int y = snake.getPoints(0).getY();
            field[x][y] = Cell.SNAKE_BODY;
            for (int i = 1; i < snake.getPointsCount(); ++i) {
                x += snake.getPoints(i).getX();
                y += snake.getPoints(i).getY();
                field[x][y] = Cell.SNAKE_BODY;
            }
        }

        for (var food : gameState.getFoodsList()) {
            field[food.getX()][food.getY()] = Cell.FOOD;
        }

        return field;
    }

    private boolean checkAreaWithoutSnakes(Cell[][] field, int leftX, int topY) {
        for (int x = leftX; x < leftX + 5; ++x) {
            for (int y = topY; y < topY + 5; ++y) {
                if (field[x][y] == Cell.SNAKE_BODY) {
                    return false;
                }
            }
        }
        return true;
    }

    private Optional<SnakesProto.GameState.Snake> placeNewSnake(int playerId) {
        var field = fieldPresentation();
        for (int i = 0; i < gameConfig.getWidth() - 4; ++i) {
            for (int j = 0; j < gameConfig.getHeight() - 4; ++j) {
                if (checkAreaWithoutSnakes(field, i, j) && field[i + 2][j + 2] == Cell.FREE
                        && field[i + 2][j + 3] == Cell.FREE) {
                    var snakeBuilder = SnakesProto.GameState.Snake.newBuilder()
                            .setPlayerId(playerId)
                            .setHeadDirection(SnakesProto.Direction.UP)
                            .setState(SnakesProto.GameState.Snake.SnakeState.ALIVE);
                    var coord = SnakesProto.GameState.Coord.newBuilder().setX(i + 2).setY(j + 2).build();
                    var coord1 = SnakesProto.GameState.Coord.newBuilder().setX(0).setY(1).build();
                    return Optional.of(snakeBuilder.addPoints(coord).addPoints(coord1).build());
                }
            }
        }
        return Optional.empty();
    }

    boolean addPlayer(@NonNull SnakesProto.GamePlayer player) {
        var snake = placeNewSnake(player.getId());
        return true;
    }

}
