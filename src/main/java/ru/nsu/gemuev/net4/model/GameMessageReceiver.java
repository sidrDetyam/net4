package ru.nsu.gemuev.net4.model;

import ru.nsu.gemuev.net4.SnakesProto;

import java.io.IOException;

public interface GameMessageReceiver {
    SnakesProto.GameMessage receiveGameMessage(byte[] buff) throws IOException;
}
