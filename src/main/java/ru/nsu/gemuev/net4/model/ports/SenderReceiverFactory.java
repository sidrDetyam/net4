package ru.nsu.gemuev.net4.model.ports;

public interface SenderReceiverFactory extends ReceiverFactory {
    GameMessageSender createSender();
}
