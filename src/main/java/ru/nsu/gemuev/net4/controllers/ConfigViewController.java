package ru.nsu.gemuev.net4.controllers;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

public class ConfigViewController {

    private final EventBus eventBus;

    @Inject
    public ConfigViewController(EventBus eventBus){
        this.eventBus = eventBus;
    }
}
