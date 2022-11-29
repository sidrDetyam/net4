package ru.nsu.gemuev.net4.model.ports;

public interface SenderReceiverFactory {
    GameMessageReceiver createReceiver();

    GameMessageSender createSender();
}
