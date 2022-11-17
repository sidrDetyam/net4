package ru.nsu.gemuev.net4.model.communication;

import com.google.inject.Inject;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.Range;
import ru.nsu.gemuev.net4.SnakesProto;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

// TODO бля тупое название
@Log4j2
public class GameMessageConfirmingSender {

    private final List<Message> unconfirmedMessages = new ArrayList<>();
    private final GameMessageSender messageSender;

    @Inject
    public GameMessageConfirmingSender(@NonNull GameMessageSender messageSender) {
        this.messageSender = messageSender;
    }

    public void sendAsync(@NonNull InetAddress address,
                          @Range(from = 0, to = 65536) int port,
                          @NonNull SnakesProto.GameMessage message,
                          boolean needConfirm) {

        CompletableFuture.runAsync(() -> {
            if (needConfirm) {
                synchronized (unconfirmedMessages) {
                    unconfirmedMessages.add(new Message(message, address, port));
                }
            }
            try {
                messageSender.sendGameMessage(address, port, message);
            } catch (IOException e) {
                log.error(e);
            }
        });
    }

    public void resendUnconfirmed(@Range(from = 0, to = Long.MAX_VALUE) long ttl) {
        synchronized (unconfirmedMessages) {
            unconfirmedMessages.forEach(message -> {
                long instant = Instant.now().toEpochMilli();
                if (instant - message.getSentAt() > ttl) {
                    sendAsync(message.getReceiverAddress(), message.getReceiverPort(), message.getMessage(), false);
                    message.setSentAt(instant);
                }
            });
        }
    }

    public void confirm(@NonNull InetAddress address,
                        @Range(from = 0, to = 65536) int port,
                        long messageSeq) {
        synchronized (unconfirmedMessages) {
            unconfirmedMessages.removeIf(message ->
                    message.getMessage().getMsgSeq() == messageSeq &&
                            message.getReceiverAddress().equals(address) && message.getReceiverPort() == port);
        }
    }

    public void cancelConfirmationForHost(@NonNull InetAddress address,
                                          @Range(from = 0, to = 65536) int port) {
        synchronized (unconfirmedMessages) {
            unconfirmedMessages.removeIf(message ->
                    message.getReceiverAddress().equals(address) && message.getReceiverPort() == port);
        }
    }
}
