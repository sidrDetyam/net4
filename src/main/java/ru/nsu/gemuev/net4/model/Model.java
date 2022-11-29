package ru.nsu.gemuev.net4.model;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import ru.nsu.gemuev.net4.controllers.uievents.GameStateChanged;
import ru.nsu.gemuev.net4.controllers.uievents.ListOfAnnGamesChangedEvent;
import ru.nsu.gemuev.net4.controllers.uievents.ShowGameViewEvent;
import ru.nsu.gemuev.net4.mappers.MessageMapper;
import ru.nsu.gemuev.net4.model.communication.*;
import ru.nsu.gemuev.net4.model.game.Direction;
import ru.nsu.gemuev.net4.model.game.GameConfig;
import ru.nsu.gemuev.net4.model.game.GameState;
import ru.nsu.gemuev.net4.model.ports.*;
import ru.nsu.gemuev.net4.net.MulticastReceiver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Log4j2
public class Model {
    private static final long ANNOUNCEMENT_GAME_TTL = 2000;
    private static final InetAddress mAddress;
    private static final int mPort = 9193;

    static {
        try {
            mAddress = InetAddress.getByName("239.192.0.4");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private final Map<AnnouncementGame, Long> gamesRepository = new HashMap<>();
    private final GameMessageSender sender;
    private final SenderReceiverFactoryCreator senderReceiverFactoryCreator;
    private final Thread multicastListenerThread;
    private final Thread unicastListenerThread;
    private final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);
    private ModelState currentState;
    private CommunicationModel communicationModel;
    private final EventBus eventBus;

    @Inject
    public Model(@NonNull MulticastReceiver multicastReceiver,
                 @NonNull SenderReceiverFactoryCreator senderReceiverFactoryCreator,
                 @NonNull EventBus eventBus) {
        this.eventBus = eventBus;
        this.senderReceiverFactoryCreator = senderReceiverFactoryCreator;
        SenderReceiverFactory factory = senderReceiverFactoryCreator.newFactory();
        sender = factory.createSender();
        GameMessageReceiver receiver = factory.createReceiver();

        // TODO утечка this
        multicastListenerThread = new Thread(new MessagesListener(
                multicastReceiver,
                new AnnouncementMessageHandler(this)));
        multicastListenerThread.start();
        unicastListenerThread = new Thread(new MessagesListener(
                receiver,
                new AnnouncementMessageHandler(this)));
        unicastListenerThread.start();

        scheduler.scheduleAtFixedRate(this::deleteExpiredGames,
                0, ANNOUNCEMENT_GAME_TTL / 2, TimeUnit.MILLISECONDS);
    }

    public synchronized void deleteExpiredGames() {
        long instant = Instant.now().toEpochMilli();
        gamesRepository.entrySet().removeIf(entry -> instant - entry.getValue() > ANNOUNCEMENT_GAME_TTL);
        eventBus.post(new ListOfAnnGamesChangedEvent(gamesRepository.keySet()));
    }

    @SneakyThrows
    public synchronized void newGame(@NonNull GameConfig gameConfig,
                                     @NonNull String playerName,
                                     @NonNull String gameName) {
        if (currentState == ModelState.GAME_RUNNING) {
            communicationModel.leave();
        }
        currentState = ModelState.NO_GAME;

        communicationModel = new CommunicationModelBuilder(senderReceiverFactoryCreator.newFactory(),
                playerName, mAddress, mPort, this::gameStateChanged)
                .createGame(gameName, gameConfig);
        currentState = ModelState.GAME_RUNNING;
        eventBus.post(new ShowGameViewEvent());
    }

    public synchronized void gameStateChanged(@NonNull GameState newState) {
        eventBus.post(new GameStateChanged(newState));
    }

    public synchronized void steer(@NonNull Direction direction) {
        if (currentState == ModelState.GAME_RUNNING) {
            communicationModel.steer(direction);
        }
    }

    public synchronized void stopGame() {
        if (currentState == ModelState.GAME_RUNNING) {
            communicationModel.leave();
        }
        currentState = ModelState.NO_GAME;
    }

    public synchronized void exit() {
        stopGame();
        multicastListenerThread.interrupt();
        unicastListenerThread.interrupt();
        scheduler.shutdown();
    }

    public void joinGame(@NonNull AnnouncementGame game, @NonNull String playerName, boolean isOnlyView) {
        scheduler.schedule(() -> {
            synchronized (this) {
                try {
                    communicationModel = new CommunicationModelBuilder(senderReceiverFactoryCreator.newFactory(),
                            playerName, mAddress, mPort, this::gameStateChanged)
                            .joinToGame(isOnlyView? NodeRole.VIEWER : NodeRole.NORMAL, game);
                    currentState = ModelState.GAME_RUNNING;
                    eventBus.post(new ShowGameViewEvent());
                } catch (JoinGameException e) {
                    log.error(e);
                }
            }
        }, 0, TimeUnit.MILLISECONDS);
    }

    public void discoverGames(){
        scheduler.schedule(() -> {
            synchronized (this){
                try {
                    sender.sendGameMessage(new Message(MessageMapper.discoverOf(0), mAddress, mPort));
                } catch (IOException e) {
                    log.error("Can`t send discover " + e);
                }
            }
        }, 0, TimeUnit.MILLISECONDS);
    }

    public synchronized void announcementGameMessage(@NonNull AnnouncementGame game) {
        gamesRepository.put(game, Instant.now().toEpochMilli());
        eventBus.post(new ListOfAnnGamesChangedEvent(gamesRepository.keySet()));
    }
}
