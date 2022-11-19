package ru.nsu.gemuev.net4.model;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import lombok.Getter;
import lombok.NonNull;
import ru.nsu.gemuev.net4.controllers.uievents.ListOfAnnGamesChangedEvent;

import java.time.Instant;
import java.util.*;

public class AnnouncementGamesRepository {

    @Getter
    private final Map<AnnouncementGame, Long> repository = new HashMap<>();
    private final EventBus eventBus;

    public synchronized List<AnnouncementGame> getListOfGames(){
        return List.copyOf(repository.keySet());
    }

    public synchronized void deleteExpiredGames(long ttl){
        long instant = Instant.now().toEpochMilli();
        repository.entrySet().removeIf(entry -> instant - entry.getValue() > ttl);
        eventBus.post(new ListOfAnnGamesChangedEvent(getListOfGames()));
    }

    public synchronized void addGame(@NonNull AnnouncementGame game){
        repository.put(game, Instant.now().toEpochMilli());
        eventBus.post(new ListOfAnnGamesChangedEvent(getListOfGames()));
    }

    @Inject
    public AnnouncementGamesRepository(@NonNull EventBus eventBus){
        this.eventBus = eventBus;
    }
}
