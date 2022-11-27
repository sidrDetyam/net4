package ru.nsu.gemuev.net4.model;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.Range;
import ru.nsu.gemuev.net4.controllers.uievents.GameStateChanged;
import ru.nsu.gemuev.net4.controllers.uievents.ShowGameViewEvent;
import ru.nsu.gemuev.net4.mappers.MessageMapper;
import ru.nsu.gemuev.net4.model.communication.GameEventHandler;
import ru.nsu.gemuev.net4.model.communication.PseudoReliableSender;
import ru.nsu.gemuev.net4.model.communication.MessagesListener;
import ru.nsu.gemuev.net4.model.game.*;
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

    private final PseudoReliableSender pseudoReliableSender;

    private Thread mThread;
    private Thread uThread;

    private volatile GameState gameState;
    private volatile GameConfig gameConfig;

    private volatile boolean isGameStart;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    private final EventBus eventBus;

    private final List<Player> players = new ArrayList<>();

    public synchronized Optional<Player> findPlayerByAddress(@NonNull InetAddress address,
                                                             @Range(from = 0, to = 65536) int port) {
        return players.stream()
                .filter(player -> address.equals(player.getAddress()) && player.getPort() == port).findAny();
    }

    public synchronized Optional<Player> findPlayerByRole(@NonNull NodeRole role) {
        return players.stream().filter(player -> role.equals(player.getPlayerRole())).findAny();
    }

    public synchronized Optional<Player> findPlayerById(int id) {
        return players.stream().filter(player -> player.getId() == id).findAny();
    }

    public synchronized boolean isMyRole(@NonNull NodeRole role){
        var me = players.stream().filter(player -> player.getId() == myId).findAny();
        return me.isPresent() && me.get().getPlayerRole() == role;
    }

    public synchronized boolean updateLastComm(@NonNull InetAddress address,
                                            @Range(from = 0, to = 65536) int port) {
        if(isGameStart) {
            var opt =findPlayerByAddress(address, port);
            if(opt.isEmpty()){
                return false;
            }
            opt.ifPresent(player -> player.setLastComm(Instant.now().toEpochMilli()));
            return true;
        }
        return false;
    }

    public synchronized void handleExpired() {
        if (isGameStart) {
            long instant = Instant.now().toEpochMilli();
            long delay = gameConfig.delay();
            var expired = players.stream()
                    .filter(p -> p.getId() != myId)
                    .filter(p -> instant - p.getLastComm() > delay / 10)
                    .peek(p -> pseudoReliableSender.sendAsync(p.getAddress(), p.getPort(),
                            MessageMapper.pingOf(p.getId(), nextMsgSeq()), false))
                    .filter(player -> instant - player.getLastComm() > 0.8 * delay)
                    .toList();
            players.removeAll(expired);
            expired.forEach(this::playerExpire);
        }
    }

    private void playerExpire(@NonNull Player player) {
        System.out.println("expired: " + player);

        gameState.getSnakes().stream()
                .filter(s -> s.getPlayerId() == player.getId()).findAny()
                .ifPresent(s -> s.setSnakeState(SnakeState.ZOMBIE));

        if(isMyRole(NodeRole.MASTER)){
            pseudoReliableSender.cancelConfirmationForHost(player.getAddress(), player.getPort());
            if(player.getPlayerRole() == NodeRole.DEPUTY){
                findPlayerByRole(NodeRole.NORMAL).ifPresent(p -> {
                    p.setPlayerRole(NodeRole.DEPUTY);
                    pseudoReliableSender.sendAsync(p.getAddress(), p.getPort(),
                            MessageMapper.roleChangedOf(NodeRole.MASTER, NodeRole.DEPUTY, myId, p.getId(), nextMsgSeq()),
                            true);
                });
            }
        }

        if(isMyRole(NodeRole.NORMAL) && player.getPlayerRole() == NodeRole.MASTER){
            var deputy = findPlayerByRole(NodeRole.DEPUTY)
                    .orElseThrow(() -> new IllegalStateException("No deputy"));
            pseudoReliableSender
                    .replaceDestination(player.getAddress(), player.getPort(), deputy.getAddress(), deputy.getPort());
            deputy.setPlayerRole(NodeRole.MASTER);
        }

        if(isMyRole(NodeRole.DEPUTY) && player.getPlayerRole()==NodeRole.MASTER){
            pseudoReliableSender.cancelConfirmationForHost(player.getAddress(), player.getPort());
            var me = findPlayerById(myId)
                    .orElseThrow(() -> new IllegalStateException("меня нет"));
            me.setPlayerRole(NodeRole.MASTER);
            players.forEach(p -> pseudoReliableSender.sendAsync(
                    p.getAddress(), p.getPort(),
                    MessageMapper.roleChangedOf(NodeRole.MASTER, p.getPlayerRole(), myId, p.getId(), nextMsgSeq()),
                    true));
        }
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

        pseudoReliableSender = new PseudoReliableSender(udpSenderReceiver);

        mThread = new Thread(multicastListener);
        uThread = new Thread(unicastListener);
        mThread.start();
        uThread.start();

        gameSchedule.scheduleAtFixedRate(this::announceGame, 0, 1000, TimeUnit.MILLISECONDS);
        gameSchedule.scheduleAtFixedRate(() -> gamesRepository.deleteExpiredGames(2000), 0, 1000, TimeUnit.MILLISECONDS);

        var random = new Random();
        id = -1;
        while(id < 0) {
            id = random.nextInt();
        }
    }

    ScheduledExecutorService gameSchedule = new ScheduledThreadPoolExecutor(2);

    private void startDaemons() {
        scheduler.shutdown();
        scheduler = Executors.newScheduledThreadPool(5);
        long delay = gameConfig.delay();
        scheduler.scheduleAtFixedRate(this::nextState, delay, delay, TimeUnit.MILLISECONDS);
        scheduler.scheduleAtFixedRate(this::resend, 0, delay / 10, TimeUnit.MILLISECONDS);
        scheduler.scheduleAtFixedRate(this::handleExpired, delay, delay / 10, TimeUnit.MILLISECONDS);
    }

    public synchronized void resend() {
        if (isGameStart) {
            pseudoReliableSender.resendUnconfirmed(gameConfig.delay() / 10);
        }
    }

    //NodeRole myRole;
    private int myId;

    private int id = 0;

    private int nextId() {
        return id++;
    }

    private long msgSeq = 0;

    private long nextMsgSeq() {
        return msgSeq++;
    }


    public synchronized void newGame(@NonNull GameConfig gameConfig, @NonNull String playerName) {
        players.clear();
        pseudoReliableSender.clear();
        gameState = new GameState(gameConfig);
        this.gameConfig = gameConfig;
        Player player = new Player(playerName, nextId(),
                udpSenderReceiver.getLocalAddress(), udpSenderReceiver.getLocalPort(), NodeRole.MASTER, 0, 0);
        players.add(player);
        gameState.addPlayer(player.getId());
        myId = player.getId();
        isGameStart = true;
        startDaemons();
        eventBus.post(new ShowGameViewEvent());
    }

    public synchronized void steer(@NonNull Direction direction) {
        if(isGameStart) {
            var master = findPlayerByRole(NodeRole.MASTER)
                    .orElseThrow(() -> new IllegalStateException("Game without master"));
            if (master.getId() == myId) {
                gameState.steer(myId, direction);
            } else {
                pseudoReliableSender.sendAsync(master.getAddress(), master.getPort(),
                        MessageMapper.steerOf(direction, myId, nextMsgSeq()), true);
            }
        }
    }

    public synchronized boolean getIsGameStart(){
        return isGameStart;
    }

    public synchronized void stopGame() {
        isGameStart = false;
        scheduler.shutdown();
        pseudoReliableSender.clear();
        scheduler = new ScheduledThreadPoolExecutor(5);
    }

    public synchronized List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public synchronized void nextState() {
        if (isGameStart && isMyRole(NodeRole.MASTER)) {
            var murders = gameState.nextState();
            for(var i : murders){
                findPlayerById(i.victimId()).ifPresent(victim -> {
                    if(victim.getPlayerRole() == NodeRole.NORMAL){
                        victim.setPlayerRole(NodeRole.VIEWER);
                    }
                });
                findPlayerById(i.killerId()).ifPresent(p -> p.setKilled(p.getKilled() + 1));
            }
            eventBus.post(new GameStateChanged(gameState));
            players.forEach(player -> {
                if (player.getPlayerRole() != NodeRole.MASTER) {
                    pseudoReliableSender.sendAsync(player.getAddress(), player.getPort(),
                            MessageMapper.stateOf(gameState, getPlayers(), nextMsgSeq()), true);
                }
            });
        }
    }

    @SneakyThrows
    public synchronized void announceGame() {
        System.out.println(players + " " + myId);
        if (isGameStart && isMyRole(NodeRole.MASTER)) {
            pseudoReliableSender.sendAsync(InetAddress.getByName("239.192.0.4"), 9193,
                    MessageMapper.announcementOf(gameConfig, getPlayers(),
                            "game name",
                            true,
                            nextMsgSeq()),
                    false);
        }
    }

    private boolean bred = false;

    public synchronized void joinGame_(@NonNull String playerName) {
        newGame(new GameConfig(40, 30, 3, 100), "хуй");
        stopGame();
        var opt = gamesRepository.getRepository().keySet().stream().findFirst();
        if(opt.isEmpty()){
            System.out.println("Игры нет");
            return;
        }

        opt.ifPresent(entry -> {
            players.clear();
            pseudoReliableSender.clear();
            var msg = MessageMapper.joinOf(entry.gameName(), playerName, NodeRole.NORMAL, nextMsgSeq());
            pseudoReliableSender.sendAsync(entry.senderAddress(), entry.senderPort(), msg, true);
            gameConfig = entry.gameConfig();
            bred = true;
        });
    }

    //events

    public synchronized void steerMessage(@NonNull Direction newDirection,
                                          long messageSeq,
                                          @NonNull InetAddress senderAddress,
                                          @Range(from = 0, to = 65536) int senderPort) {

        var playerOpt = findPlayerByAddress(senderAddress, senderPort);
        if (isGameStart && playerOpt.isPresent() && isMyRole(NodeRole.MASTER)) {
            updateLastComm(senderAddress, senderPort);
            int id = playerOpt.get().getId();
            gameState.steer(id, newDirection);
            pseudoReliableSender
                    .sendAsync(senderAddress, senderPort, MessageMapper.ackOf(id, messageSeq), false);
        }
    }

    public synchronized void stateMessage(@NonNull GameState newState,
                                          @NonNull Collection<? extends Player> players,
                                          long messageSeq,
                                          @NonNull InetAddress senderAddress,
                                          @Range(from = 0, to = 65536) int senderPort) {

        var playerOpt = findPlayerByAddress(senderAddress, senderPort);
        //pseudoReliableSender.sendAsync(senderAddress, senderPort, MessageMapper.ackOf(0, messageSeq), false);
        if (isGameStart && playerOpt.isPresent() && playerOpt.get().getPlayerRole() == NodeRole.MASTER &&
                (gameState==null || newState.getStateOrder() > gameState.getStateOrder())) {
            updateLastComm(senderAddress, senderPort);

            gameState = newState;
            this.players.clear();
            this.players.addAll(players);
            findPlayerByRole(NodeRole.MASTER).ifPresent(master -> {
                master.setPort(senderPort);
                master.setAddress(senderAddress);
            });
            int id = playerOpt.get().getId();
            pseudoReliableSender
                    .sendAsync(senderAddress, senderPort, MessageMapper.ackOf(id, messageSeq), false);
            eventBus.post(new GameStateChanged(newState));
        }
    }

    public synchronized void joinMessage(@NonNull String name,
                                         @NonNull NodeRole role,
                                         long messageSeq,
                                         @NonNull InetAddress address,
                                         @Range(from = 0, to = 65536) int port) {
        var playerOpt = findPlayerByAddress(address, port);
        if(playerOpt.isPresent()){
            pseudoReliableSender.sendAsync(address, port,
                    MessageMapper.ackOf(playerOpt.get().getId(), messageSeq), false);
            return;
        }
        if (isGameStart && isMyRole(NodeRole.MASTER)) {
            Player player = new Player(name, nextId(), address, port, role, 0, Instant.now().toEpochMilli());
            if (role != NodeRole.VIEWER) {
                if(findPlayerByRole(NodeRole.DEPUTY).isEmpty()){
                    player.setPlayerRole(NodeRole.DEPUTY);
                }
                gameState.addPlayer(player.getId());
            }
            players.add(player);
            pseudoReliableSender.sendAsync(address, port,
                    MessageMapper.ackOf(player.getId(), messageSeq), false);
        }
    }

    public synchronized void ackMessage(int receiverId,
                                        long messageSeq,
                                        @NonNull InetAddress address,
                                        @Range(from = 0, to = 65536) int port) {
        pseudoReliableSender.confirm(address, port, messageSeq);

        var opt = findPlayerByAddress(address, port);
        updateLastComm(address, port);

        if (!isGameStart && bred) {
            players.clear();
            players.add(new Player("", 0, address, port, NodeRole.MASTER, 0, Instant.now().toEpochMilli()));
            myId = receiverId;
            startDaemons();
            isGameStart = true;
            bred = false;
            eventBus.post(new ShowGameViewEvent());
        }
    }

    public synchronized void pingMessage(long messageSeq,
                                         @NonNull InetAddress address,
                                         @Range(from = 0, to = 65536) int port) {
        var playerOpt = findPlayerByAddress(address, port);
        if (isGameStart && playerOpt.isPresent()) {
            updateLastComm(address, port);
            int id = playerOpt.get().getId();
            pseudoReliableSender
                    .sendAsync(address, port, MessageMapper.ackOf(id, messageSeq), false);
        }
    }

    public synchronized void addGame(@NonNull AnnouncementGame game) {
        gamesRepository.addGame(game);
    }

    public synchronized void roleChangedMessage(@NonNull NodeRole receiverRole,
                                         @NonNull NodeRole senderRole,
                                         int senderId,
                                         int receiverId,
                                         long messageSeq,
                                         @NonNull InetAddress address,
                                         @Range(from = 0, to = 65536) int port) {
        if (isGameStart) {
            findPlayerById(senderId).ifPresent(p -> p.setPlayerRole(senderRole));
            findPlayerById(receiverId).ifPresent(p -> p.setPlayerRole(receiverRole));
            pseudoReliableSender
                    .sendAsync(address, port, MessageMapper.ackOf(senderId, messageSeq), false);
        }
    }
}
