package ru.nsu.gemuev.net4.model.ports;

import java.io.IOException;

public interface GameMessageReceiver {
    Message receiveGameMessage(byte[] buff) throws IOException;
}
