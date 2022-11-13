package ru.nsu.gemuev.net4.model;

import com.google.inject.Inject;
import ru.nsu.gemuev.net4.SnakesProto;
import ru.nsu.gemuev.net4.model.gameevents.GameEventHandler;
import ru.nsu.gemuev.net4.net.MulticastReceiver;
import ru.nsu.gemuev.net4.net.UdpSenderReceiver;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Model {

    private final MulticastReceiver multicastReceiver;
    private final UdpSenderReceiver udpSenderReceiver;
    private final MessagesListener multicastListener;
    private final MessagesListener unicastListener;
    private final GameEventHandler eventHandler;

    private Thread mThread;
    private Thread uThread;

    @Inject
    public Model(MulticastReceiver multicastReceiver, UdpSenderReceiver udpSenderReceiver) throws UnknownHostException {
        this.multicastReceiver = multicastReceiver;
        this.udpSenderReceiver = udpSenderReceiver;
        eventHandler = new GameEventHandler();
        multicastListener = new MessagesListener(multicastReceiver, eventHandler);
        unicastListener = new MessagesListener(udpSenderReceiver, eventHandler);

        mThread = new Thread(multicastListener);
        uThread = new Thread(unicastListener);
        mThread.start();
        uThread.start();

//        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
//        final int port = 9193;
//        InetAddress inetAddress = Inet4Address.getByName("239.192.0.4");
//        scheduler.scheduleAtFixedRate(() -> {
//            var disk = SnakesProto.GameMessage.DiscoverMsg.newBuilder().build();
//
//            SnakesProto.GameMessage mess = SnakesProto.GameMessage.newBuilder()
//                    .setDiscover(disk)
//                    .setMsgSeq(1)
//                    .build();
//
//            try {
//                udpSenderReceiver.sendGameMessage(inetAddress, port, mess);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }, 1000, 5000, TimeUnit.MILLISECONDS);
    }
}
