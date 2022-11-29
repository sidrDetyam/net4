package ru.nsu.gemuev.net4.model.ports;

import lombok.NonNull;

import java.io.Closeable;
import java.io.IOException;

public interface GameMessageSender extends Closeable {
    void sendGameMessage(@NonNull Message message) throws IOException;
}
