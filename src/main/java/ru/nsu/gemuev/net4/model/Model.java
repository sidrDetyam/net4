package ru.nsu.gemuev.net4.model;

import com.google.inject.Inject;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import ru.nsu.gemuev.net4.SnakesProto;
import ru.nsu.gemuev.net4.model.game.Game;
import ru.nsu.gemuev.net4.model.gameevents.GameEventHandler;
import ru.nsu.gemuev.net4.net.MulticastReceiver;
import ru.nsu.gemuev.net4.net.UdpSenderReceiver;

import java.net.*;
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

    private Thread mThread;
    private Thread uThread;

    private Game game;
    private boolean isGameStart;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    @Inject
    public Model(MulticastReceiver multicastReceiver, UdpSenderReceiver udpSenderReceiver){
        this.multicastReceiver = multicastReceiver;
        this.udpSenderReceiver = udpSenderReceiver;
        eventHandler = new GameEventHandler();
        multicastListener = new MessagesListener(multicastReceiver, eventHandler);
        unicastListener = new MessagesListener(udpSenderReceiver, eventHandler);

        mThread = new Thread(multicastListener);
        uThread = new Thread(unicastListener);
        mThread.start();
        uThread.start();
    }

    SnakesProto.GameState gameState = null;
    SnakesProto.GameConfig gameConfig = null;

    private int id = 0;
    public int nextId() {
        return id++;
    }
    private int msgSeq = 0;
    public int nextMsgSeq(){
        return msgSeq++;
    }

    public void newGame(@NonNull SnakesProto.GameConfig gameConfig, @NonNull String playerName) {
        this.gameConfig = gameConfig;
        var gamePlayer = SnakesProto.GamePlayer
                .newBuilder()
                .setId(nextId())
                .setName(playerName)
                .setRole(SnakesProto.NodeRole.MASTER)
                .setType(SnakesProto.PlayerType.HUMAN)
                .setScore(0)
                .build();

        gamePlayers = SnakesProto.GamePlayers
                .newBuilder()
                .addPlayers(gamePlayer)
                .build();

        try {
            scheduler.shutdown();
        } catch (Throwable ignore) {}
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::notifyAboutGame, 0, 1000, TimeUnit.MILLISECONDS);
    }

    private SnakesProto.GamePlayers gamePlayers;
    //private SnakesProto.GameConfig gameConfig;

    @SneakyThrows
    void notifyAboutGame() {
        var ann = SnakesProto.GameAnnouncement
                .newBuilder()
                .setGameName("game 1")
                .setConfig(gameConfig)
                .setPlayers(gamePlayers)
                .setCanJoin(true)
                .build();

        var ann2 = SnakesProto.GameMessage.AnnouncementMsg
                .newBuilder().addGames(ann).build();

        var msh = SnakesProto.GameMessage.newBuilder()
                .setAnnouncement(ann2)
                .setMsgSeq(0)
                .build();

        final int port = 9193;
        InetAddress address = Inet4Address.getByName("239.192.0.4");
        udpSenderReceiver.sendGameMessage(address, port, msh);
    }
}
