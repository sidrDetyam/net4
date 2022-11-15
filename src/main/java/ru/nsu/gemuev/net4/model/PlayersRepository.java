package ru.nsu.gemuev.net4.model;

import lombok.Data;
import lombok.NonNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
public class PlayersRepository {

    private final List<Player> players = new ArrayList<>();
    private int id = 0;
    private volatile NodeRole nodeRole = NodeRole.NORMAL;

    public int nextId() {
        return id++;
    }

    public PlayersRepository(){

    }

    public void updateTll(int playerId){
        players.stream()
                .filter(player -> player.getId() == playerId)
                .findFirst()
                .ifPresent(player -> player.setLastComm(Instant.now().toEpochMilli()));
    }

    public void updatePlayers(@NonNull Collection<Player> players){
        this.players.clear();
        this.players.addAll(players);
    }
}
