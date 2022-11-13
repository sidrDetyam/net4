package ru.nsu.gemuev.net4.model.game;

import lombok.Data;

@Data
public class Game {

    private final Snake snake;
    private final Cell[][] cells;
    private final int sizeX, sizeY;
    private final int countOfFood;

    public Game(int sizeX, int sizeY, int countOfFood) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.countOfFood = countOfFood;
        cells = new Cell[sizeX][sizeY];
        clearField();
        snake = new Snake(sizeX, sizeY);
    }

    public void clearField(){
        for(int i=0; i<sizeX; ++i){
            for(int j=0;  j<sizeY; ++j){
                cells[i][j] = Cell.FREE;
            }
        }
    }

    public void putSnake(){
        for(var seg : snake.getBody()){
            cells[seg.getCoordinate().getX()][seg.getCoordinate().getY()] = Cell.SNAKE_BODY;
        }
    }

    public void nextState(){
        clearField();
        snake.forward();
        putSnake();
    }
}
