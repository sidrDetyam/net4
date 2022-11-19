package ru.nsu.gemuev.net4.model;

import lombok.NonNull;
import org.jetbrains.annotations.Range;

import java.net.InetAddress;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;


// TODO и вообще этот класс не нужен?
public class PlayersRepository {

    private final List<Player> players = new ArrayList<>();

    //TODO опасное
    public synchronized List<Player> getPlayers(){
        return Collections.unmodifiableList(players);
    }

    public synchronized void forEach(@NonNull Consumer<? super Player> action){
        players.forEach(action);
    }

    public synchronized Optional<Player> findPlayerByAddress(@NonNull InetAddress address,
                                                             @Range(from = 0, to = 65536) int port){
        return players.stream()
                .filter(player -> address.equals(player.getAddress()) && player.getPort() == port)
                .findAny();
    }

    public synchronized void updateLastComm(@NonNull InetAddress address,
                                            @Range(from = 0, to = 65536) int port){
        findPlayerByAddress(address, port)
                .ifPresent(player -> player.setLastComm(Instant.now().toEpochMilli()));
    }

    public synchronized void setPlayers(@NonNull Collection<? extends Player> players){
        this.players.clear();
        this.players.addAll(players);
    }

    public synchronized void addPlayer(@NonNull Player player){
        players.add(player);
    }

    //TODO калбэк в синхроназе = изи дедлок
    public synchronized void handleExpired(@Range(from=0, to=Integer.MAX_VALUE) long ttl,
                                           @NonNull Consumer<? super Player> onExpired){
        long instant = Instant.now().toEpochMilli();
        setPlayers(players.stream()
                .filter(player -> instant - player.getLastComm() > ttl)
                .peek(onExpired)
                .toList());
    }
}
