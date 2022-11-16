package ru.nsu.gemuev.net4.model.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class Snake {
    private final List<SnakeSegment> body = new ArrayList<>();
    //TODO А должна ли змейка знать размер поля?
    private final int sizeX;
    private final int sizeY;
    @Getter
    private final int playerId;
    @Getter @Setter
    private SnakeState snakeState;

    public List<SnakeSegment> getBody(){
        return List.copyOf(body);
    }

    public SnakeSegment getHead(){
        return body.get(0);
    }

    @Getter
    @AllArgsConstructor
    public class SnakeSegment{
        private Direction direction;
        private Coordinate coordinate;

        private void forward(){
            coordinate = direction.shift(coordinate, sizeX, sizeY);
        }
    }

    public Snake(int sizeX, int sizeY,
                 Direction headDirection, @NonNull Coordinate headCoordinate,
                 int playerId){
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.playerId = playerId;
        body.add(new SnakeSegment(headDirection, headCoordinate));
        snakeState = SnakeState.ALIVE;
    }

    public void addSegment(Direction direction){
        var lastSegment = body.get(body.size()-1);
        var coord = direction.shift(lastSegment.coordinate, sizeX, sizeY);
        body.add(new SnakeSegment(direction.opposite(), coord));
    }

    public void forward(){
        for(var segment : body){
            segment.forward();
        }
        for(int i=body.size()-1; i>0; --i){
            body.get(i).direction = (body.get(i-1).getDirection());
        }
    }

    public void grow(){
        addSegment(body.get(body.size()-1).getDirection().opposite());
    }

    public void setHeadDirection(Direction newDirection){
        var head = body.get(0);
        if(body.size() > 1){
            var neck = body.get(1);
            if(neck.getDirection() == newDirection.opposite() || snakeState == SnakeState.ZOMBIE){
                return;
            }
        }
        head.direction = newDirection;
    }

    public Coordinate getHeadCoordinate(){
        return body.get(0).getCoordinate();
    }
}
