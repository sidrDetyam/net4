package ru.nsu.gemuev.net4.model.communication;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import ru.nsu.gemuev.net4.model.ports.GameMessageReceiver;

import java.net.SocketTimeoutException;

@Log4j2
public class MessagesListener implements Runnable{

    private static final int MAX_MESSAGE_SIZE = 10000;
    private static final int SO_TIMEOUT = 300;

    private final GameMessageReceiver gameMessageReceiver;
    private final MessageHandler gameMessageHandler;

    public MessagesListener(@NonNull GameMessageReceiver gameMessageReceiver,
                            @NonNull MessageHandler gameMessageHandler){
        this.gameMessageReceiver = gameMessageReceiver;
        this.gameMessageHandler = gameMessageHandler;
    }

    @Override
    public void run() {
        var buff = new byte[MAX_MESSAGE_SIZE];
        while(!Thread.currentThread().isInterrupted()) {
            try {
                gameMessageReceiver.setSoTimeout(SO_TIMEOUT);
                var message = gameMessageReceiver.receiveGameMessage(buff);
                gameMessageHandler.handle(message);
            }
            catch (SocketTimeoutException ignore){}
            catch (Throwable e){
                log.error(e);
            }
        }
    }
}
