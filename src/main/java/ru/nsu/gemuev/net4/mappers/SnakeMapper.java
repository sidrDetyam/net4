package ru.nsu.gemuev.net4.mappers;

import lombok.NonNull;
import ru.nsu.gemuev.net4.SnakesProto;
import ru.nsu.gemuev.net4.model.game.Direction;
import ru.nsu.gemuev.net4.model.game.Snake;

public class SnakeMapper {

    private SnakeMapper(){}

    public static Snake dto2Model(@NonNull SnakesProto.GameState.Snake dtoSnake,
                                  int sizeX, int sizeY){
        Snake modelSnake = new Snake(
                sizeX, sizeY,
                DirectionMapper.dto2Model(dtoSnake.getHeadDirection()),
                CoordinateMapper.dto2Model(dtoSnake.getPoints(0)),
                dtoSnake.getPlayerId());

        for(int i=1; i<dtoSnake.getPointsCount(); ++i){
            modelSnake.addSegment(Direction.fromShift(
                    CoordinateMapper.dto2Model(dtoSnake.getPoints(i))));
        }
        modelSnake.setSnakeState(SnakeStateMapper.dto2Model(dtoSnake.getState()));

        return modelSnake;
    }

    public static SnakesProto.GameState.Snake model2Dto(@NonNull Snake modelSnake){
        var snakeBuilder = SnakesProto.GameState.Snake.newBuilder()
                .setState(SnakeStateMapper.model2Dto(modelSnake.getSnakeState()))
                .setHeadDirection(DirectionMapper.model2Dto(modelSnake.getHead().getDirection()))
                .setPlayerId(modelSnake.getPlayerId());

        snakeBuilder.addPoints(CoordinateMapper.model2Dto(modelSnake.getHead().getCoordinate()));
        var body = modelSnake.getBody();
        for(int i=1; i<body.size(); ++i){
            snakeBuilder.addPoints(CoordinateMapper.model2Dto(
                    body.get(i).getDirection().opposite().shift()));
        }

        return snakeBuilder.build();
    }
}
