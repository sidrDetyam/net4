package ru.nsu.gemuev.net4.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Snake {

    public enum Direction{
        UP, DOWN, LEFT, RIGHT
    }

    @Data
    @AllArgsConstructor
    public static class Coordinate{
        private int x;
        private int y;
    }

    @Data
    @AllArgsConstructor
    public class SnakeSegment{
        private Direction direction;
        private Coordinate coordinate;

        public void forward(){
            switch (direction) {
                case UP -> coordinate.setY((coordinate.getY() + sizeY - 1) % sizeY);
                case DOWN -> coordinate.setY((coordinate.getY() + 1) % sizeY);
                case LEFT -> coordinate.setX((coordinate.getX() + sizeX - 1) % sizeX);
                case RIGHT -> coordinate.setX((coordinate.getX() + 1) % sizeX);
            }
        }
    }

    private final List<SnakeSegment> body;
    private final int sizeX;
    private final int sizeY;

    public Snake(int sizeX, int sizeY){
        this.sizeX = sizeX;
        this.sizeY = sizeY;

        body = new ArrayList<>();
        for(int i=0; i<3; ++i) {
            body.add(new SnakeSegment(Direction.RIGHT, new Coordinate(sizeX/2-i, sizeY/2)));
        }
    }

    public void forward(){
        for(var seg : body){
            seg.forward();
        }
        for(int i=body.size()-1; i>0; --i){
            body.get(i).setDirection(body.get(i-1).getDirection());
        }
    }
}
