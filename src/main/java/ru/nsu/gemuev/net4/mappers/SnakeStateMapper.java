package ru.nsu.gemuev.net4.mappers;

import lombok.NonNull;
import ru.nsu.gemuev.net4.SnakesProto;
import ru.nsu.gemuev.net4.model.game.SnakeState;

public class SnakeStateMapper {

    private SnakeStateMapper(){}

    public static SnakeState dto2Model(@NonNull SnakesProto.GameState.Snake.SnakeState dtoState){
        return switch (dtoState) {
            case ALIVE -> SnakeState.ALIVE;
            case ZOMBIE -> SnakeState.ZOMBIE;
        };
    }

    public static SnakesProto.GameState.Snake.SnakeState model2Dto(@NonNull SnakeState modelState){
        return switch (modelState) {
            case ALIVE -> SnakesProto.GameState.Snake.SnakeState.ALIVE;
            case ZOMBIE -> SnakesProto.GameState.Snake.SnakeState.ZOMBIE;
        };
    }
}
