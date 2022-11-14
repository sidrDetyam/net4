package ru.nsu.gemuev.net4.model.gamemodel;

import lombok.AllArgsConstructor;
import lombok.Getter;
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

    @Getter
    @AllArgsConstructor
    public class SnakeSegment{
        private Direction direction;
        private Coordinate coordinate;

        private void forward(){
            coordinate = direction.shift(coordinate, sizeX, sizeY);
        }
    }

    public Snake(int sizeX, int sizeY, SnakeSegment head, int playerId){
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.playerId = playerId;
        body.add(head);
        snakeState = SnakeState.ALIVE;
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
        var lastSegment = body.get(body.size()-1);
        var coord = lastSegment
                .getDirection()
                .opposite()
                .shift(lastSegment.coordinate, sizeX, sizeY);
        body.add(new SnakeSegment(lastSegment.direction, coord));
    }

    public void setHeadDirection(Direction newDirection){
        var head = body.get(0);
        if(head.direction != newDirection.opposite() && snakeState != SnakeState.ZOMBIE){
            head.direction = newDirection;
        }
    }
}
