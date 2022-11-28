package ru.nsu.gemuev.net4.model;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import ru.nsu.gemuev.net4.controllers.uievents.ShowGameViewEvent;
import ru.nsu.gemuev.net4.model.communication.*;
import ru.nsu.gemuev.net4.model.game.Direction;
import ru.nsu.gemuev.net4.model.game.GameConfig;
import ru.nsu.gemuev.net4.model.ports.GameMessageReceiver;
import ru.nsu.gemuev.net4.model.ports.GameMessageSender;
import ru.nsu.gemuev.net4.net.MulticastReceiver;
import ru.nsu.gemuev.net4.net.UdpSenderReceiver;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Log4j2
public class Model {

    private static final long ANNOUNCEMENT_GAME_TTL = 2000;

    private final AnnouncementGamesRepository gamesRepository;
    private final GameMessageSender sender;
    private final GameMessageReceiver receiver;
    private final PseudoReliableSender pseudoReliableSender;
    private final Thread multicastListenerThread;
    private final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);
    private ModelState currentState;
    private CommunicationModel communicationModel;
    private final EventBus eventBus;

    @Inject
    public Model(@NonNull MulticastReceiver multicastReceiver,
                 @NonNull UdpSenderReceiver messageSender,
                 @NonNull UdpSenderReceiver messageReceiver,
                 @NonNull EventBus eventBus) {
        gamesRepository = new AnnouncementGamesRepository(eventBus);
        this.eventBus = eventBus;
        this.receiver = messageReceiver;
        this.sender = messageSender;
        pseudoReliableSender = new PseudoReliableSender(messageSender);
        // TODO утечка this
        MessageHandler handler = new AnnouncementMessageHandler(this);
        multicastListenerThread = new Thread(new MessagesListener(multicastReceiver, handler));
        multicastListenerThread.start();
        scheduler.scheduleAtFixedRate(() -> gamesRepository.deleteExpiredGames(ANNOUNCEMENT_GAME_TTL),
                0, ANNOUNCEMENT_GAME_TTL / 2, TimeUnit.MILLISECONDS);
    }

    @SneakyThrows
    public synchronized void newGame(@NonNull GameConfig gameConfig,
                                     @NonNull String playerName,
                                     @NonNull String gameName) {
        if(currentState == ModelState.GAME_RUNNING){
            communicationModel.leave();
        }
        currentState = ModelState.NO_GAME;

        communicationModel = new CommunicationModelBuilder(receiver, sender, playerName)
                .createGame(gameName, gameConfig);
        currentState = ModelState.GAME_RUNNING;
        eventBus.post(new ShowGameViewEvent());
    }

    public synchronized void steer(@NonNull Direction direction) {
        if(currentState == ModelState.GAME_RUNNING) {
            communicationModel.steer(direction);
        }
    }

    public synchronized void stopGame() {
        if(currentState == ModelState.GAME_RUNNING){
            communicationModel.leave();
        }
        currentState = ModelState.NO_GAME;
    }

    public synchronized void exit(){
        stopGame();
        multicastListenerThread.interrupt();
        scheduler.shutdown();
    }

    public synchronized void joinGame(@NonNull String playerName) {
        var opt = gamesRepository.getRepository().keySet().stream().findFirst();
        if(opt.isEmpty()){
            // TODO
            return;
        }
        opt.ifPresent(entry -> {
            pseudoReliableSender.clear();
            try {
                communicationModel = new CommunicationModelBuilder(receiver, sender, playerName)
                        .joinToGame(NodeRole.NORMAL, entry);
                currentState = ModelState.GAME_RUNNING;
                eventBus.post(new ShowGameViewEvent());
            } catch (JoinGameException e) {
                log.error(e);
            }
        });
    }

    public synchronized void announcementGameMessage(@NonNull AnnouncementGame game) {
        gamesRepository.addGame(game);
    }
}
