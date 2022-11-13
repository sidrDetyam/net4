package ru.nsu.gemuev.net4.model;

import lombok.NonNull;
import ru.nsu.gemuev.net4.SnakesProto;

import java.io.IOException;
import java.net.InetAddress;

public interface GameMessageSender {
    void sendGameMessage(@NonNull InetAddress address, int port, SnakesProto.GameMessage gameMessage)
            throws IOException;
}
