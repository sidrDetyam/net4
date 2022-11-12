package ru.nsu.gemuev.net4.controllers;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

public class GameViewController {

    private final EventBus eventBus;

    @Inject
    public GameViewController(EventBus eventBus){
        this.eventBus = eventBus;
    }

}
