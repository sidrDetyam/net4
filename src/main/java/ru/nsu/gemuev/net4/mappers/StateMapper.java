package ru.nsu.gemuev.net4.mappers;

import lombok.NonNull;
import ru.nsu.gemuev.net4.SnakesProto;
import ru.nsu.gemuev.net4.model.communication.Node;
import ru.nsu.gemuev.net4.model.game.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StateMapper {

    private StateMapper(){}

    public static GameState dto2Model(@NonNull SnakesProto.GameState dtoState,
                                      @NonNull GameConfig gameConfig){

        List<Snake> snakes = dtoState.getSnakesList().stream()
                .map(dtoSnake -> SnakeMapper.dto2Model(dtoSnake, gameConfig.width(), gameConfig.height()))
                .collect(Collectors.toList());
        Set<Integer> snakesId = snakes.stream().map(Snake::getPlayerId).collect(Collectors.toSet());

        List<Coordinate> foods = dtoState.getFoodsList().stream()
                .map(CoordinateMapper::dto2Model)
                .toList();

        List<Player> players = PlayerMapper.nodes(dtoState.getPlayers()).stream()
                .map(Node::getPlayer)
                .filter(player -> snakesId.contains(player.getId()))
                .toList();

        return new GameState(gameConfig, snakes, players, foods, dtoState.getStateOrder());
    }

    public static SnakesProto.GameState model2Dto(@NonNull GameState modelState,
                                                  @NonNull Collection<? extends Node> nodes){

        return SnakesProto.GameState.newBuilder()
                .setStateOrder(modelState.getStateOrder())
                .setPlayers(PlayerMapper.dtoPlayers(nodes))
                .addAllFoods(modelState.getFoods().stream().map(CoordinateMapper::model2Dto).toList())
                .addAllSnakes(modelState.getSnakes().stream().map(SnakeMapper::model2Dto).toList())
                .build();
    }
}
