package ru.nsu.gemuev.net4.net;

import ru.nsu.gemuev.net4.model.ports.GameMessageReceiver;
import ru.nsu.gemuev.net4.model.ports.GameMessageSender;
import ru.nsu.gemuev.net4.model.ports.SenderReceiverFactory;

import java.net.DatagramSocket;
import java.net.SocketException;

public class UdpSenderReceiverFactory implements SenderReceiverFactory {

    private final DatagramSocket datagramSocket;

    public UdpSenderReceiverFactory() throws SocketException {
        datagramSocket = new DatagramSocket();
    }

    @Override
    public GameMessageReceiver createReceiver() {
        return new UdpGameMessageReceiver(datagramSocket);
    }

    @Override
    public GameMessageSender createSender() {
        return new UdpGameMessageSender(datagramSocket);
    }
}
