package ru.nsu.gemuev.net4.model;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.Range;
import ru.nsu.gemuev.net4.controllers.uievents.ShowGameViewEvent;
import ru.nsu.gemuev.net4.mappers.MessageMapper;
import ru.nsu.gemuev.net4.model.communication.*;
import ru.nsu.gemuev.net4.model.game.Direction;
import ru.nsu.gemuev.net4.model.game.GameConfig;
import ru.nsu.gemuev.net4.model.game.GameState;
import ru.nsu.gemuev.net4.model.game.Player;
import ru.nsu.gemuev.net4.model.ports.GameMessageReceiver;
import ru.nsu.gemuev.net4.model.ports.GameMessageSender;
import ru.nsu.gemuev.net4.net.MulticastReceiver;
import ru.nsu.gemuev.net4.net.UdpSenderReceiver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
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
    //private Thread joinGameAckListenerThread;
    private final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);
    private ModelState currentState;
    private CommunicationModel communicationModel;
    private GameConfig gameConfig;
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
    public synchronized void newGame(@NonNull GameConfig gameConfig, @NonNull String playerName) {
        if(currentState == ModelState.GAME_RUNNING){
            communicationModel.leave();
        }
        currentState = ModelState.NO_GAME;

        Player player = new Player(playerName, 0);
        Node node = new Node(player, InetAddress.getLocalHost(), 0, NodeRole.MASTER, 0);
        GameState initGameState = new GameState(gameConfig);
        if(!initGameState.addPlayer(player)){
            // TODO что-то показать
            return;
        }
        communicationModel = new CommunicationModel(receiver, sender, eventBus, node, node, initGameState);
        currentState = ModelState.GAME_RUNNING;
        eventBus.post(new ShowGameViewEvent());
        System.out.println("here");
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

    public synchronized void joinGame(@NonNull String playerName) {
        var opt = gamesRepository.getRepository().keySet().stream().findFirst();
        if(opt.isEmpty()){
            // TODO
            return;
        }
        opt.ifPresent(entry -> {
            var joinMsg = MessageMapper.joinOf(entry.gameName(), playerName, NodeRole.NORMAL, 0);
            pseudoReliableSender.clear();
            //joinGameAckListenerThread = new Thread(new MessagesListener(receiver, new AnnouncementMessageHandler(this)));
            //joinGameAckListenerThread.start();
            gameConfig = entry.gameConfig();

            var buff = new byte[100000];
            while(true){
                try {
                    pseudoReliableSender.sendAsync(entry.senderAddress(), entry.senderPort(), joinMsg, false);
                    var message = receiver.receiveGameMessage(buff);
                    var msg = message.getMessage();
                    if(message.getMessage().hasAck()){
                        currentState = ModelState.GAME_RUNNING;

                        var myNode = new Node(new Player("", msg.getReceiverId()),
                                InetAddress.getLocalHost(), 0, NodeRole.NORMAL, 0);
                        Node master = new Node(new Player("", msg.getSenderId()),
                                entry.senderAddress(), entry.senderPort(), NodeRole.MASTER, Instant.now().toEpochMilli());
                        currentState = ModelState.GAME_RUNNING;
                        GameState init = new GameState(gameConfig);
                        communicationModel = new CommunicationModel(receiver, sender, eventBus, master, myNode, init);
                        eventBus.post(new ShowGameViewEvent());
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

//            try {
//                new AnnouncementMessageHandler(this).handle(receiver.receiveGameMessage(new byte[100000]));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        });
    }

    @SneakyThrows
    public synchronized void ackMessage(int senderId,
                                        int receiverId,
                                        long messageSeq,
                                        @NonNull InetAddress address,
                                        @Range(from = 0, to = 65536) int port) {
        pseudoReliableSender.confirm(address, port, messageSeq);
        if (currentState == ModelState.WAIT_ACK) {
            Player me = new Player("", receiverId);
            Node myNode = new Node(me, InetAddress.getLocalHost(), 0, NodeRole.NORMAL, 0);
            Node master = new Node(new Player("", senderId),
                    address, port, NodeRole.MASTER, Instant.now().toEpochMilli());

            currentState = ModelState.GAME_RUNNING;
            //joinGameAckListenerThread.interrupt();

            GameState init = new GameState(gameConfig);
            communicationModel = new CommunicationModel(receiver, sender, eventBus, master, myNode, init);
            eventBus.post(new ShowGameViewEvent());
        }
    }

    public synchronized void announcementGameMessage(@NonNull AnnouncementGame game) {
        gamesRepository.addGame(game);
    }
}
