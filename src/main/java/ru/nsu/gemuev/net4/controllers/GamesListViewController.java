package ru.nsu.gemuev.net4.controllers;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import lombok.NonNull;
import ru.nsu.gemuev.net4.controllers.uievents.ListOfAnnGamesChangedEvent;

public class GamesListViewController {

    private final EventBus eventBus;

    @Inject
    public GamesListViewController(@NonNull EventBus eventBus){
        this.eventBus = eventBus;
    }


    @Subscribe
    public void listOfGamesChanged(ListOfAnnGamesChangedEvent e){
        for(var i : e.getGames()){
            System.out.println(i);
        }
    }
}
