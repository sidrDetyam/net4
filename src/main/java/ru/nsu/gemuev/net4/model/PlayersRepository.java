package ru.nsu.gemuev.net4.model;

import lombok.NonNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PlayersRepository {

    private final List<Player> players = new ArrayList<>();

    public PlayersRepository(){

    }

    public synchronized List<Player> getPlayers(){
        return List.copyOf(players);
    }

    public synchronized void updateTll(int playerId){
        players.stream()
                .filter(player -> player.getId() == playerId)
                .findFirst()
                .ifPresent(player -> player.setLastComm(Instant.now().toEpochMilli()));
    }

    public synchronized void updatePlayers(@NonNull Collection<Player> players){
        this.players.clear();
        this.players.addAll(players);
    }

    public synchronized void newPlayer(@NonNull Player player){
        players.add(player);
    }
}
