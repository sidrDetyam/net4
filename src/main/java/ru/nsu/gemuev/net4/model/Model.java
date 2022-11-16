package ru.nsu.gemuev.net4.model;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import ru.nsu.gemuev.net4.SnakesProto;
import ru.nsu.gemuev.net4.controllers.uievents.GameStateChanged;
import ru.nsu.gemuev.net4.mappers.DirectionMapper;
import ru.nsu.gemuev.net4.mappers.GameConfigMapper;
import ru.nsu.gemuev.net4.mappers.Message;
import ru.nsu.gemuev.net4.mappers.PlayerMapper;
import ru.nsu.gemuev.net4.model.game.Direction;
import ru.nsu.gemuev.net4.model.game.GameConfig;
import ru.nsu.gemuev.net4.model.game.GameState;
import ru.nsu.gemuev.net4.model.gameevents.GameEventHandler;
import ru.nsu.gemuev.net4.net.MulticastReceiver;
import ru.nsu.gemuev.net4.net.UdpSenderReceiver;
import ru.nsu.gemuev.net4.util.DIContainer;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
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
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

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

        scheduler.scheduleAtFixedRate(this::nextState, 500, 100, TimeUnit.MILLISECONDS);
        scheduler.scheduleAtFixedRate(this::announceGame, 100, 1000, TimeUnit.MILLISECONDS);
    }

    NodeRole myRole;
    private int myId;

    private int id = 0;
    private int nextId(){
        return id++;
    }

    private long msgSeq = 0;
    private long nextMsgSeq(){
        return msgSeq++;
    }

    private void send(@NonNull InetAddress address, int port, @NonNull SnakesProto.GameMessage msg){
        
    }

    public synchronized void newGame(@NonNull GameConfig gameConfig){
        gameState = new GameState(gameConfig);
        myRole = NodeRole.MASTER;
        this.gameConfig = gameConfig;
        Player player = new Player("name", nextId(), null, 0, NodeRole.MASTER, 0, 0);
        playersRepository.updatePlayers(List.of(player));
        myId = player.getId();
        isGameStart = true;
    }

    public synchronized void steer(@NonNull Direction direction){
        if(myRole == NodeRole.MASTER){
            gameState.getSnakes().stream()
                    .filter(snake -> snake.getPlayerId() == myId)
                    .findFirst()
                    .ifPresent(snake -> snake.setHeadDirection(direction));
        }
        else{
            playersRepository.getPlayers().stream()
                    .filter(player -> player.getPlayerRole() == NodeRole.MASTER)
                    .findFirst()
                    .ifPresent(player -> {
                        try {
                            udpSenderReceiver.sendGameMessage(player.getAddress(), player.getPort(),
                                    Message.of(direction, myId, nextMsgSeq()));
                        } catch (IOException e) {
                            log.error(e);
                        }
                    });
        }
    }

    public synchronized void stopGame(){
        isGameStart = false;
    }

    public synchronized void nextState(){
        if(isGameStart && myRole == NodeRole.MASTER){
            gameState.nextState();
            eventBus.post(new GameStateChanged(gameState));
            playersRepository.getPlayers().forEach(player -> {
                try{
                    udpSenderReceiver.sendGameMessage(player.getAddress(), player.getPort(), );
                }
                catch (IOException e){
                    log.error(e);
                }
            });
        }
    }

    public void announceGame() {
        if (isGameStart && playersRepository.getNodeRole() == NodeRole.MASTER) {
            var players = SnakesProto.GamePlayers.newBuilder();
            for (var player : playersRepository.getPlayers()) {
                players.addPlayers(PlayerMapper.model2Dto(player));
            }
            var game = SnakesProto.GameAnnouncement
                    .newBuilder()
                    .setGameName("test game")
                    .setCanJoin(true)
                    .setConfig(GameConfigMapper.model2Dto(gameConfig))
                    .setPlayers(players.build())
                    .build();
            var message = SnakesProto.GameMessage.AnnouncementMsg
                    .newBuilder()
                    .addGames(game)
                    .build();
            var bruh = SnakesProto.GameMessage.newBuilder()
                    .setAnnouncement(message)
                    .setMsgSeq(0)
                    .build();

            try {
                udpSenderReceiver.sendGameMessage(InetAddress.getByName("239.192.0.4"), 9193, bruh);
            }
            catch(Throwable e){
                log.error(e);
            }
        }
    }
}
