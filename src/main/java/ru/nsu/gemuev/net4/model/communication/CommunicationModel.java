package ru.nsu.gemuev.net4.model.communication;

import com.google.common.eventbus.EventBus;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.Range;
import ru.nsu.gemuev.net4.controllers.uievents.GameStateChanged;
import ru.nsu.gemuev.net4.mappers.MessageMapper;
import ru.nsu.gemuev.net4.model.game.Direction;
import ru.nsu.gemuev.net4.model.game.GameConfig;
import ru.nsu.gemuev.net4.model.game.GameState;
import ru.nsu.gemuev.net4.model.game.Player;
import ru.nsu.gemuev.net4.model.ports.GameMessageReceiver;
import ru.nsu.gemuev.net4.model.ports.GameMessageSender;
import ru.nsu.gemuev.net4.util.DIContainer;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Log4j2
public class CommunicationModel {
    private final EventBus eventBus;
    private final Thread listenerThread;
    private final PseudoReliableSender sender;
    private final NodesRepository nodes = new NodesRepository();
    private final int myId;
    private int msgSeq;
    private final IdGenerator idGenerator = new IdGenerator();
    private final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(5);
    private final String gameName;
    private GameState currentGameState;

    public synchronized GameConfig getGameConfig() {
        return currentGameState.getGameConfig();
    }

    CommunicationModel(@NonNull GameMessageReceiver receiver,
                       @NonNull GameMessageSender sender,
                       @NonNull Node master,
                       @NonNull Node me,
                       @NonNull GameConfig gameConfig,
                       @NonNull String gameName) {
        this.eventBus = DIContainer.getInjector().getInstance(EventBus.class);
        this.gameName = gameName;
        listenerThread = new Thread(new MessagesListener(receiver, new GameMessageHandler(this)));
        listenerThread.start();
        this.sender = new PseudoReliableSender(sender);
        currentGameState = new GameState(gameConfig);
        nodes.addNode(master);
        currentGameState.addPlayer(master.getPlayer());
        if (!me.equals(master)) {
            nodes.addNode(me);
            currentGameState.addPlayer(me.getPlayer());
        }
        myId = me.getPlayerId();

        long delay = gameConfig.delay();
        scheduler.scheduleAtFixedRate(this::nextState, delay, delay, TimeUnit.MILLISECONDS);
        scheduler.scheduleAtFixedRate(() -> this.sender.resendUnconfirmed(delay / 10),
                0, delay / 10, TimeUnit.MILLISECONDS);
        scheduler.scheduleAtFixedRate(this::handleExpired, delay, delay / 10, TimeUnit.MILLISECONDS);
        scheduler.scheduleAtFixedRate(this::announceGame, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    private int nextMsgSeq() {
        return msgSeq++;
    }

    private boolean isMyRole(@NonNull NodeRole role) {
        var me = nodes.findNodeById(myId).orElseThrow(
                () -> new IllegalStateException("Game without player"));
        return me.getRole() == role;
    }

    private synchronized void handleExpired() {
        long delay = currentGameState.getGameConfig().delay();
        nodes.findExpired(delay / 10, myId).forEach(node ->
                sender.sendAsync(node.getAddress(), node.getPort(),
                        MessageMapper.pingOf(node.getPlayerId(), nextMsgSeq()), false));
        var expired = nodes.findExpired((long) (delay * 0.8), myId);
        nodes.deleteAll(expired);
        expired.forEach(this::nodeExpire);
    }

    private void nodeExpire(@NonNull Node node) {
        currentGameState.playerLeave(node.getPlayerId());

        if (isMyRole(NodeRole.MASTER)) {
            sender.cancelConfirmationForHost(node.getAddress(), node.getPort());
            if (node.getRole() == NodeRole.DEPUTY) {
                nodes.findNodeByRole(NodeRole.NORMAL).ifPresent(p -> {
                    p.setRole(NodeRole.DEPUTY);
//                    sender.sendAsync(p.getAddress(), p.getPort(),
//                            MessageMapper.roleChangedOf(NodeRole.MASTER, NodeRole.DEPUTY, myId, p.getPlayerId(), nextMsgSeq()),
//                            true);
                });
            }
        }

        if (isMyRole(NodeRole.NORMAL) && node.getRole() == NodeRole.MASTER) {
            var deputy = nodes.findNodeByRole(NodeRole.DEPUTY)
                    .orElseThrow(() -> new IllegalStateException("No deputy"));
            sender.replaceDestination(node.getAddress(), node.getPort(), deputy.getAddress(), deputy.getPort());
            deputy.setRole(NodeRole.MASTER);
        }

        if (isMyRole(NodeRole.DEPUTY) && node.getRole() == NodeRole.MASTER) {
            sender.cancelConfirmationForHost(node.getAddress(), node.getPort());
            nodes.findNodeById(myId)
                    .orElseThrow(() -> new IllegalStateException("Cannot find myself"))
                    .setRole(NodeRole.MASTER);
            nodes.findNodeByRole(NodeRole.NORMAL).ifPresent(n -> {
                n.setRole(NodeRole.DEPUTY);
//                sender.sendAsync(n.getAddress(), n.getPort(),
//                        MessageMapper.roleChangedOf(NodeRole.MASTER, NodeRole.DEPUTY, myId, n.getPlayerId(), nextMsgSeq()),
//                        true);
            });
            nodes.getNodes().forEach(n ->
                    sender.sendAsync(n.getAddress(), n.getPort(),
                            MessageMapper.roleChangedOf(NodeRole.MASTER, n.getRole(), myId, n.getPlayerId(), nextMsgSeq()),
                            true));
        }
    }

    public synchronized void steer(@NonNull Direction direction) {
        var master = nodes.findNodeByRole(NodeRole.MASTER)
                .orElseThrow(() -> new IllegalStateException("Game without master"));
        if (master.getPlayerId() == myId) {
            currentGameState.steer(myId, direction);
        } else {
            sender.sendAsync(master.getAddress(), master.getPort(),
                    MessageMapper.steerOf(direction, myId, nextMsgSeq()), true);
        }
    }

    public synchronized void leave() {
        listenerThread.interrupt();
        scheduler.shutdown();
    }

    private synchronized void nextState() {
        if (isMyRole(NodeRole.MASTER)) {
            currentGameState.nextState();
            eventBus.post(new GameStateChanged(currentGameState));
            nodes.getNodes().forEach(node -> {
                if (node.getPlayerId() != myId) {
                    sender.sendAsync(node.getAddress(), node.getPort(),
                            MessageMapper.stateOf(currentGameState, nodes.getNodes(), nextMsgSeq()), true);
                }
            });
        }
    }

    @SneakyThrows
    private synchronized void announceGame() {
        System.out.println(nodes.getNodes());
        if (isMyRole(NodeRole.MASTER)) {
            sender.sendAsync(InetAddress.getByName("239.192.0.4"), 9193,
                    MessageMapper.announcementOf(currentGameState.getGameConfig(), nodes.getNodes(),
                            gameName,
                            true,
                            nextMsgSeq()),
                    false);
        }
    }

    //events

    public synchronized void steerMessage(@NonNull Direction newDirection,
                                          long messageSeq,
                                          @NonNull InetAddress senderAddress,
                                          @Range(from = 0, to = 65536) int senderPort) {
        var node = nodes.findNodeByAddress(senderAddress, senderPort)
                .orElseThrow(() -> new IllegalCallerException("Unknown node"));
        if (isMyRole(NodeRole.MASTER)) {
            node.updateLastCommunication();
            int id = node.getPlayerId();
            currentGameState.steer(id, newDirection);
            sender.sendAsync(senderAddress, senderPort, MessageMapper.ackOf(id, messageSeq), false);
        }
    }

    public synchronized void stateMessage(@NonNull GameState newState,
                                          @NonNull Collection<? extends Node> nodesCol,
                                          long messageSeq,
                                          @NonNull InetAddress senderAddress,
                                          @Range(from = 0, to = 65536) int senderPort) {
        var node = nodes.findNodeByAddress(senderAddress, senderPort)
                .orElseThrow(() -> new IllegalCallerException("Unknown node"));
        if (node.getRole() == NodeRole.MASTER && newState.getStateOrder() > currentGameState.getStateOrder()) {
            node.updateLastCommunication();
            currentGameState = newState;
            nodes.setNodes(nodesCol);
            Node master = nodes.findNodeByRole(NodeRole.MASTER).orElseThrow();
            nodes.delete(master);
            nodes.addNode(new Node(master.getPlayer(), senderAddress, senderPort, NodeRole.MASTER, Instant.now().toEpochMilli()));
            sender.sendAsync(senderAddress, senderPort, MessageMapper.ackOf(node.getPlayerId(), messageSeq), false);
            eventBus.post(new GameStateChanged(currentGameState));
        }
    }

    public synchronized void joinMessage(@NonNull String name,
                                         @NonNull NodeRole role,
                                         long messageSeq,
                                         @NonNull InetAddress address,
                                         @Range(from = 0, to = 65536) int port) {
        if (role != NodeRole.NORMAL && role != NodeRole.VIEWER) {
            log.error("This role not allowed %s [%s %s]".formatted(role, address, port));
            return;
        }
        var nodeOpt = nodes.findNodeByAddress(address, port);
        if (nodeOpt.isPresent()) {
            sender.sendAsync(address, port, MessageMapper.ackOf(nodeOpt.get().getPlayerId(), messageSeq), false);
            return;
        }
        if (isMyRole(NodeRole.MASTER)) {
            Player player = new Player(name, idGenerator.nextId());
            Node node = new Node(player, address, port, role, Instant.now().toEpochMilli());
            if (role != NodeRole.VIEWER) {
                if (nodes.findNodeByRole(NodeRole.DEPUTY).isEmpty()) {
                    node.setRole(NodeRole.DEPUTY);
                }
                currentGameState.addPlayer(player);
            }
            nodes.addNode(node);
            sender.sendAsync(address, port, MessageMapper.ackOf(player.getId(), messageSeq), false);
        }
    }

    public synchronized void ackMessage(long messageSeq,
                                        @NonNull InetAddress address,
                                        @Range(from = 0, to = 65536) int port) {
        sender.confirm(address, port, messageSeq);
        nodes.updateLastCommunication(address, port);
    }

    public synchronized void pingMessage(long messageSeq,
                                         @NonNull InetAddress address,
                                         @Range(from = 0, to = 65536) int port) {
        var node = nodes.findNodeByAddress(address, port).orElseThrow();
        node.updateLastCommunication();
        sender.sendAsync(address, port, MessageMapper.ackOf(node.getPlayerId(), messageSeq), false);
    }

    public synchronized void roleChangedMessage(@NonNull NodeRole receiverRole,
                                                @NonNull NodeRole senderRole,
                                                int senderId,
                                                int receiverId,
                                                long messageSeq,
                                                @NonNull InetAddress address,
                                                @Range(from = 0, to = 65536) int port) {
        nodes.findNodeByAddress(address, port).orElseThrow();
        nodes.findNodeById(senderId).ifPresent(n -> n.setRole(senderRole));
        nodes.findNodeById(receiverId).ifPresent(n -> n.setRole(receiverRole));
        sender.sendAsync(address, port, MessageMapper.ackOf(senderId, messageSeq), false);
    }
}
