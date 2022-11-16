package ru.nsu.gemuev.net4.mappers;

import lombok.NonNull;
import ru.nsu.gemuev.net4.SnakesProto;
import ru.nsu.gemuev.net4.model.Player;
import ru.nsu.gemuev.net4.model.game.Coordinate;
import ru.nsu.gemuev.net4.model.game.GameConfig;
import ru.nsu.gemuev.net4.model.game.GameState;
import ru.nsu.gemuev.net4.model.game.Snake;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StateMapper {

    private StateMapper(){}

    public static GameState dto2Model(@NonNull SnakesProto.GameState dtoState,
                                      @NonNull GameConfig gameConfig){

        List<Snake> snakes = new ArrayList<>();
        for(var dtoSnake : dtoState.getSnakesList()){
            snakes.add(SnakeMapper.dto2Model(dtoSnake, gameConfig.width(), gameConfig.height()));
        }
        List<Coordinate> foods = new ArrayList<>();
        for(var dtoFood : dtoState.getFoodsList()){
            foods.add(CoordinateMapper.dto2Model(dtoFood));
        }

        return new GameState(gameConfig, snakes, foods, dtoState.getStateOrder());
    }

    public static SnakesProto.GameState model2Dto(@NonNull GameState modelState,
                                                  @NonNull Collection<Player> modelPlayers){
        var dtoPlayers = SnakesProto.GamePlayers.newBuilder()
                .addAllPlayers(modelPlayers.stream().map(PlayerMapper::model2Dto).toList())
                .build();

        return SnakesProto.GameState.newBuilder()
                .setStateOrder(modelState.getStateOrder())
                .setPlayers(dtoPlayers)
                .addAllFoods(modelState.getFoods().stream().map(CoordinateMapper::model2Dto).toList())
                .addAllSnakes(modelState.getSnakes().stream().map(SnakeMapper::model2Dto).toList())
                .build();
    }
}
