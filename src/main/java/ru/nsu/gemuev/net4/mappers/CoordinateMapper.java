package ru.nsu.gemuev.net4.mappers;

import lombok.NonNull;
import ru.nsu.gemuev.net4.SnakesProto;
import ru.nsu.gemuev.net4.model.game.Coordinate;

public class CoordinateMapper {

    private CoordinateMapper(){}

    public static Coordinate dto2Model(@NonNull SnakesProto.GameState.Coord dtoCoord){
        return new Coordinate(dtoCoord.getX(), dtoCoord.getY());
    }
}
