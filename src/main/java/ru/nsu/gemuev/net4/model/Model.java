package ru.nsu.gemuev.net4.model;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import ru.nsu.gemuev.net4.SnakesProto;
import ru.nsu.gemuev.net4.controllers.uievents.GameStateChanged;
import ru.nsu.gemuev.net4.mappers.GameConfigMapper;
import ru.nsu.gemuev.net4.mappers.Message;
import ru.nsu.gemuev.net4.model.game.Direction;
import ru.nsu.gemuev.net4.model.game.GameConfig;
import ru.nsu.gemuev.net4.model.game.GameState;
import ru.nsu.gemuev.net4.model.gameevents.GameEventHandler;
import ru.nsu.gemuev.net4.net.MulticastReceiver;
import ru.nsu.gemuev.net4.net.UdpSenderReceiver;
import ru.nsu.gemuev.net4.util.DIContainer;

import java.io.IOException;
import java.net.InetAddress;
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
    public Model(MulticastReceiver multicastReceiver, UdpSenderReceiver udpSenderReceiver) {
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

    private InetAddress masterAddress;
    private int masterPort;

    private int id = 0;

    private int nextId() {
        return id++;
    }

    private long msgSeq = 0;

    private long nextMsgSeq() {
        return msgSeq++;
    }

    private void send(@NonNull InetAddress address, int port, @NonNull SnakesProto.GameMessage msg){
        try {
            udpSenderReceiver.sendGameMessage(address, port, msg);
        } catch (IOException e) {
            log.error(e);
        }
    }

    private void send(@NonNull Player toPlayer, @NonNull SnakesProto.GameMessage msg) {
        send(toPlayer.getAddress(), toPlayer.getPort(), msg);
    }

    public synchronized void newGame(@NonNull GameConfig gameConfig) {
        gameState = new GameState(gameConfig);
        myRole = NodeRole.MASTER;
        this.gameConfig = gameConfig;
        Player player = new Player("name", nextId(), null, 0, NodeRole.MASTER, 0, 0);
        playersRepository.updatePlayers(List.of(player));
        gameState.addPlayer(player.getId());
        myId = player.getId();
        isGameStart = true;
    }

    public synchronized void steer(@NonNull Direction direction) {
        if (myRole == NodeRole.MASTER) {
            gameState.getSnakes().stream()
                    .filter(snake -> snake.getPlayerId() == myId)
                    .findFirst()
                    .ifPresent(snake -> snake.setHeadDirection(direction));
        } else {
            send(masterAddress, masterPort, Message.of(direction, myId, nextMsgSeq()));
        }
    }

    public synchronized void stopGame() {
        isGameStart = false;
    }

    public synchronized void nextState() {
        if (isGameStart && myRole == NodeRole.MASTER) {
            gameState.nextState();
            eventBus.post(new GameStateChanged(gameState));
            playersRepository.getPlayers().forEach(player -> {
                if (player.getPlayerRole() != NodeRole.MASTER) {
                    send(player, Message.of(gameState, playersRepository.getPlayers(), nextMsgSeq()));
                }
            });
        }
    }

    @SneakyThrows
    public synchronized void announceGame() {
        if (isGameStart && myRole == NodeRole.MASTER) {
            send(InetAddress.getByName("239.192.0.4"), 9193, Message.of(
                    gameConfig,
                    playersRepository.getPlayers(),
                    "game name",
                    true,
                    nextMsgSeq()));
        }
    }

    public synchronized void joinGame_(){
        gamesRepository.getRepository().entrySet().stream().findFirst().ifPresent(entry -> {
            var host = entry.getKey();
            var msg = Message.of(host.getGameName(), "xyu", NodeRole.NORMAL, nextMsgSeq());
            try {
                send(host.getInetAddress(), host.getPort(), msg);
            }
            catch (Throwable e){
                e.printStackTrace();
            }
            gameConfig = GameConfigMapper.dto2Model(entry.getValue().getGame().getConfig());
            masterAddress = host.getInetAddress();
            masterPort = host.getPort();
        });
    }

    public synchronized void joinGame(int id){
        isGameStart = true;
        myRole = NodeRole.NORMAL;
        myId = id;
    }

    public synchronized void playerJoin(@NonNull InetAddress address, int port){
        Player player = new Player("", nextId(), address, port, NodeRole.NORMAL, 0, 0);
        playersRepository.newPlayer(player);
        gameState.addPlayer(player.getId());
        send(player, Message.of(player.getId(), nextMsgSeq()));
    }

    public synchronized void stateChanged(@NonNull GameState newState){
        gameState = newState;
        eventBus.post(new GameStateChanged(gameState));
    }
}
