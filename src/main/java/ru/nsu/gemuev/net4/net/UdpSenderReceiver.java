package ru.nsu.gemuev.net4.net;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import ru.nsu.gemuev.net4.SnakesProto;
import ru.nsu.gemuev.net4.model.GameMessageReceiver;
import ru.nsu.gemuev.net4.model.GameMessageSender;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

@Log4j2
public class UdpSenderReceiver implements GameMessageSender, GameMessageReceiver, Closeable {
    private final DatagramSocket datagramSocket;

    public UdpSenderReceiver() throws SocketException {
        datagramSocket = new DatagramSocket();
    }

    @Override
    public void sendGameMessage(@NonNull InetAddress address, int port, @NonNull SnakesProto.GameMessage gameMessage)
            throws IOException {
        byte[] buff = gameMessage.toByteArray();
        DatagramPacket msgPacket = new DatagramPacket(buff, buff.length, address, port);
        datagramSocket.send(msgPacket);
    }

    @Override
    public SnakesProto.GameMessage receiveGameMessage(byte[] buffer) throws IOException {
        DatagramPacket datagram = new DatagramPacket(buffer, buffer.length);
        datagramSocket.receive(datagram);
        var data = Arrays.copyOf(datagram.getData(), datagram.getLength());
        return SnakesProto.GameMessage.parseFrom(data);
    }

    @Override
    public void close() {
        datagramSocket.close();
    }
}
