package ru.nsu.gemuev.net4.model.communication;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.Range;
import ru.nsu.gemuev.net4.SnakesProto;
import ru.nsu.gemuev.net4.model.ports.GameMessageSender;
import ru.nsu.gemuev.net4.model.ports.Message;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Log4j2
public class PseudoReliableSender {

    private final List<Message> unconfirmedMessages = new ArrayList<>();
    private final GameMessageSender messageSender;

    public PseudoReliableSender(@NonNull GameMessageSender messageSender) {
        this.messageSender = messageSender;
    }

    public void sendAsync(@NonNull InetAddress address,
                          @Range(from = 0, to = 65536) int port,
                          @NonNull SnakesProto.GameMessage gameMessage,
                          boolean needConfirm) {

        CompletableFuture.runAsync(() -> {
            Message message = new Message(gameMessage, address, port);
            message.setSentAt(Instant.now().toEpochMilli());
            if (needConfirm) {
                synchronized (unconfirmedMessages) {
                    unconfirmedMessages.add(message);
                }
            }
            try {
                messageSender.sendGameMessage(message);
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
                    sendAsync(message.getAddress(), message.getPort(), message.getMessage(), false);
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
                            message.getAddress().equals(address) && message.getPort() == port);
        }
    }

    public void cancelConfirmationForHost(@NonNull InetAddress address,
                                          @Range(from = 0, to = 65536) int port) {
        synchronized (unconfirmedMessages) {
            unconfirmedMessages.removeIf(message ->
                    message.getAddress().equals(address) && message.getPort() == port);
        }
    }

    public void replaceDestination(@NonNull InetAddress oldAddress,
                                   @Range(from = 0, to = 65536) int oldPort,
                                   @NonNull InetAddress newAddress,
                                   @Range(from = 0, to = 65536) int newPort) {
        synchronized (unconfirmedMessages) {
            unconfirmedMessages.replaceAll(message ->
                    message.getAddress().equals(oldAddress) && message.getPort()==oldPort?
                    new Message(message.getMessage(), newAddress, newPort, message.getSentAt()) : message);
        }
    }

    public void clear(){
        synchronized (unconfirmedMessages){
            unconfirmedMessages.clear();
        }
    }
}
