package ru.nsu.gemuev.net4.model.ports;

import org.jetbrains.annotations.Range;

import java.io.IOException;
import java.net.SocketException;

public interface GameMessageReceiver {
    Message receiveGameMessage(byte[] buff) throws IOException;

    void setSoTimeout(@Range(from = 0, to = Integer.MAX_VALUE) int timeout)
            throws SocketException;
}
