package ru.nsu.gemuev.net4.util;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import ru.nsu.gemuev.net4.controllers.ConfigViewController;
import ru.nsu.gemuev.net4.controllers.GameViewController;
import ru.nsu.gemuev.net4.controllers.MainViewController;
import ru.nsu.gemuev.net4.controllers.SceneManager;

public class DIModule extends AbstractModule {

    @Provides
    @Singleton
    EventBus getEventBus() {
        return new EventBus();
    }

    @Provides
    @Singleton
    SceneManager getSceneManager(EventBus eventBus){
        var manager = new SceneManager();
        eventBus.register(manager);
        return manager;
    }

    @Provides
    @Singleton
    MainViewController getMainViewController(EventBus eventBus) {
        var controller = new MainViewController(eventBus);
        eventBus.register(controller);
        return controller;
    }

    @Provides
    @Singleton
    GameViewController getGameViewController(EventBus eventBus) {
        var controller = new GameViewController(eventBus);
        eventBus.register(controller);
        return controller;
    }

    @Provides
    @Singleton
    ConfigViewController getConfigViewController(EventBus eventBus) {
        var controller = new ConfigViewController(eventBus);
        eventBus.register(controller);
        return controller;
    }
}
