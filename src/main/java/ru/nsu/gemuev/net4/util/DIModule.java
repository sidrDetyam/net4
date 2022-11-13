package ru.nsu.gemuev.net4.util;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.SneakyThrows;
import ru.nsu.gemuev.net4.controllers.ConfigViewController;
import ru.nsu.gemuev.net4.controllers.GameViewController;
import ru.nsu.gemuev.net4.controllers.MainViewController;
import ru.nsu.gemuev.net4.controllers.SceneManager;
import ru.nsu.gemuev.net4.net.MulticastReceiver;
import ru.nsu.gemuev.net4.net.NetInterfaceChecker;
import ru.nsu.gemuev.net4.net.UdpSenderReceiver;

import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;

public class DIModule extends AbstractModule {

    @Provides
    @Singleton
    EventBus getEventBus() {
        return new EventBus();
    }

    @Provides
    @Singleton
    SceneManager getSceneManager(EventBus eventBus) {
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

    @SneakyThrows
    @Provides
    @Singleton
    MulticastReceiver getMulticastGameEventListener() {
        NetworkInterface networkInterface = NetInterfaceChecker.findAnyUpNetworkInterface(false)
                .orElse(NetworkInterface.getByName("loopback"));
        final int port = 9193;
        SocketAddress socketAddress = new InetSocketAddress("239.192.0.4", port);

        return new MulticastReceiver(
                socketAddress,
                networkInterface,
                port);
    }

    @Provides
    @SneakyThrows
    @Singleton
    UdpSenderReceiver getUdpSenderReceiver(){
        return new UdpSenderReceiver();
    }
}
