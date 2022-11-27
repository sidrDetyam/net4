package ru.nsu.gemuev.net4.model.ports;

import lombok.NonNull;

import java.io.IOException;

public interface GameMessageSender {
    void sendGameMessage(@NonNull Message message) throws IOException;
}
