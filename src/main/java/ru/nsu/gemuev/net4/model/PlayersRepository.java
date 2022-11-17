package ru.nsu.gemuev.net4.model;

import lombok.NonNull;
import org.jetbrains.annotations.Range;

import java.net.InetAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class PlayersRepository {

    private final List<Player> players = new ArrayList<>();

    public synchronized List<Player> getPlayers(){
        return Collections.unmodifiableList(players);
    }

    public synchronized void updateLastComm(@NonNull InetAddress address,
                                            @Range(from = 0, to = 65536) int port){
        players.stream()
                .filter(player -> player.getAddress().equals(address) && player.getPort() == port)
                .findAny()
                .ifPresent(player -> player.setLastComm(Instant.now().toEpochMilli()));
    }

    public synchronized void setPlayers(@NonNull Collection<? extends Player> players){
        this.players.clear();
        this.players.addAll(players);
    }

    public synchronized void addPlayer(@NonNull Player player){
        players.add(player);
    }

    public synchronized void handleExpired(@Range(from=0, to=Integer.MAX_VALUE) long ttl,
                                           @NonNull Consumer<? super Player> onExpired){
        long instant = Instant.now().toEpochMilli();
        players.stream()
                .filter(player -> instant - player.getLastComm() > ttl)
                .forEach(onExpired);
    }
}
