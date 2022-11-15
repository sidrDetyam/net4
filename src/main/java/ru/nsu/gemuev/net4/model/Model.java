package ru.nsu.gemuev.net4.model;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import ru.nsu.gemuev.net4.controllers.uievents.GameStateChanged;
import ru.nsu.gemuev.net4.model.game.GameConfig;
import ru.nsu.gemuev.net4.model.game.GameState;
import ru.nsu.gemuev.net4.model.gameevents.GameEventHandler;
import ru.nsu.gemuev.net4.net.MulticastReceiver;
import ru.nsu.gemuev.net4.net.UdpSenderReceiver;
import ru.nsu.gemuev.net4.util.DIContainer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Data
@Log4j2
public class Model {

    private final MulticastReceiver multicastReceiver;
    private final UdpSenderReceiver udpSenderReceiver;
    private final MessagesListener multicastListener;
    private final MessagesListener unicastListener;

    private final GameEventHandler eventHandler;
    private final PlayersRepository playersRepository;
    private final AnnouncementGamesRepository gamesRepository;


    private Thread mThread;
    private Thread uThread;

    private volatile GameState gameState;
    private volatile GameConfig gameConfig;

    private volatile boolean isGameStart;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final EventBus eventBus;

    @Inject
    public Model(MulticastReceiver multicastReceiver, UdpSenderReceiver udpSenderReceiver){
        this.multicastReceiver = multicastReceiver;
        this.udpSenderReceiver = udpSenderReceiver;

        eventBus = DIContainer.getInjector().getInstance(EventBus.class);

        eventHandler = new GameEventHandler(this);
        playersRepository = new PlayersRepository();
        gamesRepository = DIContainer.getInjector().getInstance(AnnouncementGamesRepository.class);

        multicastListener = new MessagesListener(multicastReceiver, eventHandler);
        unicastListener = new MessagesListener(udpSenderReceiver, eventHandler);

        mThread = new Thread(multicastListener);
        uThread = new Thread(unicastListener);
        mThread.start();
        uThread.start();

        scheduler.scheduleAtFixedRate(this::nextState, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    public void newGame(@NonNull GameConfig gameConfig){
        gameState = new GameState(gameConfig);
        this.gameConfig = gameConfig;
        playersRepository.setNodeRole(NodeRole.MASTER);
        isGameStart = true;
    }

    public void stopGame(){
        isGameStart = false;
    }

    public void nextState(){
        if(isGameStart && playersRepository.getNodeRole() == NodeRole.MASTER){
            gameState.nextState();
            eventBus.post(new GameStateChanged(gameState));
        }
    }
}
