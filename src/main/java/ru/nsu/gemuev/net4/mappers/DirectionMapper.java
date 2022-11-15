package ru.nsu.gemuev.net4.mappers;

import ru.nsu.gemuev.net4.SnakesProto;
import ru.nsu.gemuev.net4.model.game.Direction;

public class DirectionMapper {

    private DirectionMapper(){}

    public static Direction dto2Model(SnakesProto.Direction direction){
        return switch (direction) {
            case UP -> Direction.UP;
            case DOWN -> Direction.DOWN;
            case LEFT -> Direction.LEFT;
            case RIGHT -> Direction.RIGHT;
        };
    }
}
