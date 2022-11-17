package ru.nsu.gemuev.net4.model.communication;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import ru.nsu.gemuev.net4.SnakesProto;
import ru.nsu.gemuev.net4.model.GameEventHandler;

import java.util.Arrays;

@Log4j2
public class MessagesListener implements Runnable{

    private static final int MAX_MESSAGE_SIZE = 10000;

    private final GameMessageReceiver gameMessageReceiver;
    private final GameEventHandler gameEventHandler;

    public MessagesListener(@NonNull GameMessageReceiver gameMessageReceiver,
                            @NonNull GameEventHandler gameEventHandler){
        this.gameMessageReceiver = gameMessageReceiver;
        this.gameEventHandler = gameEventHandler;
    }

    @Override
    public void run() {
        var buff = new byte[MAX_MESSAGE_SIZE];
        while(!Thread.currentThread().isInterrupted()) {
            try {
                var datagramPacket = gameMessageReceiver.receiveGameMessage(buff);
                var data = Arrays.copyOf(datagramPacket.getData(), datagramPacket.getLength());
                var message = SnakesProto.GameMessage.parseFrom(data);
                gameEventHandler.handle(message, datagramPacket.getAddress(), datagramPacket.getPort());
            }
            catch (Throwable e){
                log.error(e);
            }
        }
    }
}
