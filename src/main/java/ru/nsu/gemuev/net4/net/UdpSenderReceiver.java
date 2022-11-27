package ru.nsu.gemuev.net4.net;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import ru.nsu.gemuev.net4.SnakesProto;
import ru.nsu.gemuev.net4.model.ports.GameMessageReceiver;
import ru.nsu.gemuev.net4.model.ports.GameMessageSender;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

@Log4j2
public class UdpSenderReceiver implements GameMessageSender, GameMessageReceiver, Closeable {
    private final DatagramSocket datagramSocket;

    public UdpSenderReceiver() throws SocketException {
        datagramSocket = new DatagramSocket();
    }

    public InetAddress getLocalAddress(){
        return datagramSocket.getLocalAddress();
    }

    public int getLocalPort(){
        return datagramSocket.getLocalPort();
    }

    @Override
    public void sendGameMessage(@NonNull InetAddress address, int port, @NonNull SnakesProto.GameMessage gameMessage)
            throws IOException {
        byte[] buff = gameMessage.toByteArray();
        DatagramPacket msgPacket = new DatagramPacket(buff, buff.length, address, port);
        datagramSocket.send(msgPacket);
    }

    @Override
    public DatagramPacket receiveGameMessage(byte[] buffer) throws IOException {
        DatagramPacket datagram = new DatagramPacket(buffer, buffer.length);
        datagramSocket.receive(datagram);
        return datagram;
    }

    @Override
    public void close() {
        datagramSocket.close();
    }
}
