package ru.nsu.gemuev.net4.model;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.Range;
import ru.nsu.gemuev.net4.controllers.uievents.GameStateChanged;
import ru.nsu.gemuev.net4.mappers.MessageMapper;
import ru.nsu.gemuev.net4.model.communication.GameMessageConfirmingSender;
import ru.nsu.gemuev.net4.model.communication.MessagesListener;
import ru.nsu.gemuev.net4.model.game.Direction;
import ru.nsu.gemuev.net4.model.game.GameConfig;
import ru.nsu.gemuev.net4.model.game.GameState;
import ru.nsu.gemuev.net4.net.MulticastReceiver;
import ru.nsu.gemuev.net4.net.UdpSenderReceiver;
import ru.nsu.gemuev.net4.util.DIContainer;

import java.net.InetAddress;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Data
@Log4j2
public class Model {

    private final MulticastReceiver multicastReceiver;
    private final UdpSenderReceiver udpSenderReceiver;
    private final MessagesListener multicastListener;
    private final MessagesListener unicastListener;

    private final GameEventHandler eventHandler;
    //private final PlayersRepository playersRepository;
    private final AnnouncementGamesRepository gamesRepository;

    public synchronized AnnouncementGamesRepository getGamesRepository(){
        return gamesRepository;
    }

    private final GameMessageConfirmingSender gameMessageConfirmingSender;

    private Thread mThread;
    private Thread uThread;

    private volatile GameState gameState;
    private volatile GameConfig gameConfig;

    private volatile boolean isGameStart;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    private final EventBus eventBus;

    private final List<Player> players = new ArrayList<>();

    public synchronized Optional<Player> findPlayerByAddress(@NonNull InetAddress address,
                                                             @Range(from = 0, to = 65536) int port){
        return players.stream()
                .filter(player -> address.equals(player.getAddress()) && player.getPort() == port)
                .findAny();
    }

    public synchronized Optional<Player> findPlayerByRole(@NonNull NodeRole role){
        return players.stream()
                .filter(player -> role.equals(player.getPlayerRole()))
                .findAny();
    }

    public synchronized void updateLastComm(@NonNull InetAddress address,
                                            @Range(from = 0, to = 65536) int port){
        findPlayerByAddress(address, port)
                .ifPresent(player -> player.setLastComm(Instant.now().toEpochMilli()));
    }

    public synchronized void handleExpired(){
        if(isGameStart) {
            long instant = Instant.now().toEpochMilli();
            long delay = gameConfig.delay();
            var expired = players.stream()
                    .filter(p -> p.getId() != myId)
                    .filter(p -> instant - p.getLastComm() > delay / 10)
                    .peek(p -> gameMessageConfirmingSender.sendAsync(p.getAddress(), p.getPort(),
                            MessageMapper.pingOf(p.getId(), nextMsgSeq()), false))
                    .filter(player -> instant - player.getLastComm() > delay)
                    .peek(this::playerExpire)
                    .toList();
            players.removeAll(expired);
        }
    }

    private void playerExpire(@NonNull Player player){
        gameMessageConfirmingSender.cancelConfirmationForHost(player.getAddress(), player.getPort());
        System.out.println("expire: " + player);
    }


    @Inject
    public Model(MulticastReceiver multicastReceiver, UdpSenderReceiver udpSenderReceiver) {
        this.multicastReceiver = multicastReceiver;
        this.udpSenderReceiver = udpSenderReceiver;

        eventBus = DIContainer.getInjector().getInstance(EventBus.class);

        eventHandler = new GameEventHandler(this);
        gamesRepository = DIContainer.getInjector().getInstance(AnnouncementGamesRepository.class);

        multicastListener = new MessagesListener(multicastReceiver, eventHandler);
        unicastListener = new MessagesListener(udpSenderReceiver, eventHandler);

        gameMessageConfirmingSender = new GameMessageConfirmingSender(udpSenderReceiver);

        mThread = new Thread(multicastListener);
        uThread = new Thread(unicastListener);
        mThread.start();
        uThread.start();

        scheduler.scheduleAtFixedRate(this::announceGame, 0, 1000, TimeUnit.MILLISECONDS);
        scheduler.scheduleAtFixedRate(() -> gamesRepository.deleteExpiredGames(2000), 0, 1000, TimeUnit.MILLISECONDS);
    }

    ScheduledExecutorService gameSchedule = new ScheduledThreadPoolExecutor(2);

    private void startDaemons(){
        scheduler.shutdown();
        scheduler = Executors.newScheduledThreadPool(5);
        long delay = gameConfig.delay();
        scheduler.scheduleAtFixedRate(this::nextState, delay, delay, TimeUnit.MILLISECONDS);
        scheduler.scheduleAtFixedRate(this::resend, 0, delay / 10, TimeUnit.MILLISECONDS);
        scheduler.scheduleAtFixedRate(this::handleExpired, delay, delay/10, TimeUnit.MILLISECONDS);
    }

    public synchronized void resend(){
        if(isGameStart) {
            gameMessageConfirmingSender.resendUnconfirmed(gameConfig.delay() / 10);
        }
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


    public synchronized void newGame(@NonNull GameConfig gameConfig) {
        gameState = new GameState(gameConfig);
        myRole = NodeRole.MASTER;
        this.gameConfig = gameConfig;
        Player player = new Player("name", nextId(), null, 0, NodeRole.MASTER, 0, 0);
        players.clear();
        players.add(player);
        gameState.addPlayer(player.getId());
        myId = player.getId();
        startDaemons();
        isGameStart = true;
    }

    public synchronized void steer(@NonNull Direction direction) {
        if (myRole == NodeRole.MASTER) {
            gameState.getSnakes().stream()
                    .filter(snake -> snake.getPlayerId() == myId)
                    .findFirst()
                    .ifPresent(snake -> snake.setHeadDirection(direction));
        } else {
            gameMessageConfirmingSender.sendAsync(masterAddress, masterPort,
                    MessageMapper.steerOf(direction, myId, nextMsgSeq()), true);
        }
    }

    public synchronized void stopGame() {
        scheduler.shutdown();
        scheduler = new ScheduledThreadPoolExecutor(5);
        isGameStart = false;
    }

    public synchronized List<Player> getPlayers(){
        return Collections.unmodifiableList(players);
    }

    public synchronized void nextState() {
        if (isGameStart && myRole == NodeRole.MASTER) {
            gameState.nextState();
            eventBus.post(new GameStateChanged(gameState));
            players.forEach(player -> {
                if (player.getPlayerRole() != NodeRole.MASTER) {
                    gameMessageConfirmingSender.sendAsync(player.getAddress(), player.getPort(),
                            MessageMapper.stateOf(gameState, getPlayers(), nextMsgSeq()), true);
                }
            });
        }
    }

    @SneakyThrows
    public synchronized void announceGame() {
        if (isGameStart && myRole == NodeRole.MASTER) {
            System.out.println("ann");
            gameMessageConfirmingSender.sendAsync(InetAddress.getByName("239.192.0.4"), 9193,
                    MessageMapper.announcementOf(gameConfig, getPlayers(),
                    "game name",
                    true,
                    nextMsgSeq()),
                    false);
        }
    }

    public synchronized void joinGame_(@NonNull String playerName) {
        gamesRepository.getRepository().stream().findFirst().ifPresent(entry -> {
            var msg = MessageMapper.joinOf(entry.gameName(), playerName, NodeRole.NORMAL, nextMsgSeq());
            gameMessageConfirmingSender.sendAsync(entry.senderAddress(), entry.senderPort(), msg, false);
            gameConfig = entry.gameConfig();
            masterAddress = entry.senderAddress();
            masterPort = entry.senderPort();
        });
    }

    //events

    public synchronized void steerMessage(@NonNull Direction newDirection,
                                          long messageSeq,
                                          @NonNull InetAddress senderAddress,
                                          @Range(from = 0, to = 65536) int senderPort){

        var playerOpt = findPlayerByAddress(senderAddress, senderPort);
        if(!isGameStart || playerOpt.isEmpty() || myRole != NodeRole.MASTER){
            return;
        }
        gameState.getSnakes().stream()
                .filter(snake -> snake.getPlayerId() == playerOpt.get().getId())
                .findAny()
                .ifPresent(snake -> snake.setHeadDirection(newDirection));
        gameMessageConfirmingSender
                .sendAsync(senderAddress, senderPort, MessageMapper.ackOf(0, messageSeq), false);
    }

    public synchronized void stateMessage(@NonNull GameState newState,
                                          @NonNull Collection<? extends Player> players,
                                          long messageSeq,
                                          @NonNull InetAddress senderAddress,
                                          @Range(from = 0, to = 65536) int senderPort){

        if(isGameStart && senderAddress.equals(masterAddress) && senderPort == masterPort) {
            gameState = newState;
            this.players.clear();
            this.players.addAll(players);
            findPlayerByRole(NodeRole.MASTER).ifPresent(master -> {
                master.setPort(senderPort);
                master.setAddress(senderAddress);
            });
            gameMessageConfirmingSender
                    .sendAsync(senderAddress, senderPort, MessageMapper.ackOf(0, messageSeq), false);
            eventBus.post(new GameStateChanged(newState));
        }
    }

    public synchronized void joinMessage(@NonNull String name,
                                         @NonNull NodeRole role,
                                         long messageSeq,
                                         @NonNull InetAddress address,
                                         @Range(from = 0, to = 65536) int port) {

        var playerOpt = findPlayerByAddress(address, port);
        if(isGameStart && myRole==NodeRole.MASTER && playerOpt.isEmpty()) {
            Player player = new Player(name, nextId(), address, port, role, 0, Instant.now().toEpochMilli());
            players.add(player);
            if(role != NodeRole.VIEWER){
                gameState.addPlayer(player.getId());
            }
            gameMessageConfirmingSender.sendAsync(address, port,
                    MessageMapper.ackOf(player.getId(), messageSeq), false);
        }
    }

    public synchronized void ackMessage(int receiverId,
                                        long messageSeq,
                                        @NonNull InetAddress address,
                                        @Range(from = 0, to = 65536) int port){
        gameMessageConfirmingSender.confirm(address, port, messageSeq);
        if(!isGameStart){
            isGameStart = true;
            myRole = NodeRole.NORMAL;
            myId = receiverId;
            startDaemons();
        }
    }

    public synchronized void pingMessage(long messageSeq,
                                         @NonNull InetAddress address,
                                         @Range(from = 0, to = 65536) int port){
        if(isGameStart) {
            gameMessageConfirmingSender
                    .sendAsync(address, port, MessageMapper.ackOf(0, messageSeq), false);
        }
    }

    public synchronized void addGame(@NonNull AnnouncementGame game){
        gamesRepository.addGame(game);
    }
}
