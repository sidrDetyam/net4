package ru.nsu.gemuev.net4.model;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import lombok.Getter;
import lombok.NonNull;
import ru.nsu.gemuev.net4.controllers.uievents.ListOfAnnGamesChangedEvent;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AnnouncementGamesRepository {

    @Getter
    private final Set<AnnouncementGame> repository = new HashSet<>();
    private final EventBus eventBus;

    public synchronized List<AnnouncementGame> getListOfGames(){
        return List.copyOf(repository);
    }

    public synchronized void deleteExpiredGames(long ttl){
        System.out.println("del " + repository);
        repository.removeIf(entry -> Instant.now().toEpochMilli() - entry.createdAt() > ttl);
        eventBus.post(new ListOfAnnGamesChangedEvent(getListOfGames()));
    }

    public synchronized void addGame(@NonNull AnnouncementGame game){
        System.out.println("add " + repository);
        repository.add(game);
        eventBus.post(new ListOfAnnGamesChangedEvent(getListOfGames()));
    }

    @Inject
    public AnnouncementGamesRepository(@NonNull EventBus eventBus){
        this.eventBus = eventBus;
    }
}
