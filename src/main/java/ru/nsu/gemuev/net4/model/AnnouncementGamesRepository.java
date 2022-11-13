package ru.nsu.gemuev.net4.model;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import lombok.Getter;
import lombok.NonNull;
import ru.nsu.gemuev.net4.SnakesProto;
import ru.nsu.gemuev.net4.controllers.uievents.ListOfAnnGamesChangedEvent;

import java.net.InetAddress;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AnnouncementGamesRepository {

    private final Map<HostAddress, AnnGame> repository = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final long TTL = 3000;

    private final EventBus eventBus;

    @Getter
    public static class AnnGame{
        private final SnakesProto.GameAnnouncement game;
        private final long receiveTime;

        public AnnGame(@NonNull SnakesProto.GameAnnouncement game){
            this.game = game;
            receiveTime = Instant.now().toEpochMilli();
        }

        public boolean isExpired(){
            return Instant.now().toEpochMilli() - receiveTime > TTL;
        }
    }

    private synchronized List<SnakesProto.GameAnnouncement> getListOfGames(){
        return repository
                .values()
                .stream()
                .map(AnnGame::getGame)
                .toList();
    }

    public synchronized void deleteExpiredGames(){
        if(repository.entrySet().removeIf(entry -> entry.getValue().isExpired())){
            eventBus.post(new ListOfAnnGamesChangedEvent(getListOfGames()));
        }
    }

    public synchronized void addGame(@NonNull InetAddress address,
                                     int port,
                                     @NonNull SnakesProto.GameAnnouncement game){
        HostAddress hostAddress = new HostAddress(address, port, game.getGameName());
        if(repository.put(hostAddress, new AnnGame(game)) == null){
            eventBus.post(new ListOfAnnGamesChangedEvent(getListOfGames()));
        }
    }

    @Inject
    public AnnouncementGamesRepository(EventBus eventBus){
        this.eventBus = eventBus;
        scheduler.scheduleAtFixedRate(this::deleteExpiredGames, TTL, TTL, TimeUnit.MILLISECONDS);
    }
}
