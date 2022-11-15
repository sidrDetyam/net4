package ru.nsu.gemuev.net4.model;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import ru.nsu.gemuev.net4.model.game.GameState;
import ru.nsu.gemuev.net4.model.gameevents.GameEventHandler;
import ru.nsu.gemuev.net4.net.MulticastReceiver;
import ru.nsu.gemuev.net4.net.UdpSenderReceiver;

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

    private GameState gameState;

    //private boolean isGameStart;
    //private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    @Inject
    public Model(MulticastReceiver multicastReceiver, UdpSenderReceiver udpSenderReceiver){
        this.multicastReceiver = multicastReceiver;
        this.udpSenderReceiver = udpSenderReceiver;

        eventHandler = new GameEventHandler(this);
        playersRepository = new PlayersRepository();
        gamesRepository = new AnnouncementGamesRepository(new EventBus()); //бред

        multicastListener = new MessagesListener(multicastReceiver, eventHandler);
        unicastListener = new MessagesListener(udpSenderReceiver, eventHandler);

        mThread = new Thread(multicastListener);
        uThread = new Thread(unicastListener);
        mThread.start();
        uThread.start();
    }
}
