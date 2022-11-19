package ru.nsu.gemuev.net4.model.communication;

import java.io.IOException;
import java.net.DatagramPacket;

public interface GameMessageReceiver {
    DatagramPacket receiveGameMessage(byte[] buff) throws IOException;
}