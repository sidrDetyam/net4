package ru.nsu.gemuev.net4.model.game;

import lombok.Getter;
import lombok.NonNull;

import java.util.*;

public class GameState {

    private final List<Snake> snakes = new ArrayList<>();
    private final List<Coordinate> foods = new ArrayList<>();
    private final GameConfig gameConfig;
    @Getter
    private int stateOrder = 0;

    public List<Snake> getSnakes(){
        return List.copyOf(snakes);
    }

    public List<Coordinate> getFoods(){
        return List.copyOf(foods);
    }

    public GameState(@NonNull GameConfig gameConfig,
                     @NonNull Collection<Snake> snakes,
                     @NonNull Collection<Coordinate> foods,
                     int stateOrder){
        this.snakes.addAll(snakes);
        this.foods.addAll(foods);
        this.gameConfig = gameConfig;
        this.stateOrder = stateOrder;
    }

    public GameState(@NonNull GameConfig gameConfig){
        this.gameConfig = gameConfig;
        generateFood(gameConfig.foodStatic());
    }

    private void generateFood(int count){
        Random random = new Random();
        var field = fieldPresentation();
        for(int i=0; i<count;){
            int x = random.nextInt(gameConfig.width());
            int y = random.nextInt(gameConfig.height());
            if(field[x][y] == Cell.FREE){
                foods.add(new Coordinate(x, y));
                ++i;
            }
        }
    }

    public Cell[][] fieldPresentation() {
        var field = new Cell[gameConfig.width()][gameConfig.height()];
        for (int i = 0; i < gameConfig.width(); ++i) {
            for (int j = 0; j < gameConfig.height(); ++j) {
                field[i][j] = Cell.FREE;
            }
        }

        for (var snake : snakes) {
            for(var segment : snake.getBody()){
                field[segment.getCoordinate().x()][segment.getCoordinate().y()] = Cell.SNAKE_BODY;
            }
        }

        for (var food : foods) {
            field[food.x()][food.y()] = Cell.FOOD;
        }

        return field;
    }

    private boolean isAreaWithoutSnakes(Cell[][] field, int leftX, int topY) {
        for (int x = leftX; x < leftX + 5; ++x) {
            for (int y = topY; y < topY + 5; ++y) {
                if (field[x][y] == Cell.SNAKE_BODY) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean addPlayer(int playerId){
        var field = fieldPresentation();
        for (int i = 0; i < gameConfig.width() - 4; ++i) {
            for (int j = 0; j < gameConfig.height() - 4; ++j) {
                if (isAreaWithoutSnakes(field, i, j) && field[i + 2][j + 2] == Cell.FREE
                        && field[i + 2][j + 3] == Cell.FREE) {

                    Snake snake = new Snake(gameConfig.width(), gameConfig.height(),
                            Direction.UP, new Coordinate(i+2, j+2), playerId);
                    snake.grow();
                    snakes.add(snake);
                    return true;
                }
            }
        }
        return false;
    }

    public void nextState(){
        var field = fieldPresentation();
        Set<Coordinate> ateFoods = new HashSet<>();
        for(Snake snake : snakes){
            snake.forward();
            Coordinate headCoord = snake.getHeadCoordinate();
            if(field[headCoord.x()][headCoord.y()] == Cell.FOOD){
                snake.grow();
                ateFoods.add(headCoord);
            }
        }
        for(Coordinate food : ateFoods){
            foods.remove(food);
        }
        generateFood(ateFoods.size());
        ++stateOrder;
    }
}
