package ru.nsu.gemuev.net4.model;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import ru.nsu.gemuev.net4.SnakesProto;
import ru.nsu.gemuev.net4.model.gameevents.GameEventHandler;

import java.util.Arrays;

@Log4j2
public class MessagesListener implements Runnable{

    private final GameMessageReceiver gameMessageReceiver;
    private final GameEventHandler gameEventHandler;

    public MessagesListener(@NonNull GameMessageReceiver gameMessageReceiver,
                            @NonNull GameEventHandler gameEventHandler){
        this.gameMessageReceiver = gameMessageReceiver;
        this.gameEventHandler = gameEventHandler;
    }

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()) {
            var buff = new byte[10000];
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
