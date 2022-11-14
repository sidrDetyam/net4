package ru.nsu.gemuev.net4.model.gamemodel;

import lombok.Getter;
import lombok.NonNull;
import ru.nsu.gemuev.net4.SnakesProto;

import java.util.ArrayList;
import java.util.List;

public class GameState {

    private final List<Snake> snakes = new ArrayList<>();
    private final List<Coordinate> foods = new ArrayList<>();
    private final GameConfig gameConfig;
    @Getter
    private int stateOrder = 0;

    public GameState(@NonNull GameConfig gameConfig){
        this.gameConfig = gameConfig;
    }

    void nextState(){

    }

    void addPlayer(int playerId){
        
    }
}
