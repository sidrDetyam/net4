package ru.nsu.gemuev.net4.net;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import ru.nsu.gemuev.net4.SnakesProto;
import ru.nsu.gemuev.net4.model.ports.GameMessageReceiver;
import ru.nsu.gemuev.net4.model.ports.GameMessageSender;
import ru.nsu.gemuev.net4.model.ports.Message;

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

    public InetAddress getLocalAddress(){
        return datagramSocket.getLocalAddress();
    }

    public int getLocalPort(){
        return datagramSocket.getLocalPort();
    }

    @Override
    public void sendGameMessage(@NonNull Message message) throws IOException {
        byte[] buff = message.getMessage().toByteArray();
        DatagramPacket msgPacket = new DatagramPacket(buff, buff.length, message.getAddress(), message.getPort());
        datagramSocket.send(msgPacket);
    }

    @Override
    public Message receiveGameMessage(byte[] buffer) throws IOException{
        DatagramPacket datagram = new DatagramPacket(buffer, buffer.length);
        datagramSocket.receive(datagram);
        var data = Arrays.copyOf(datagram.getData(), datagram.getLength());
        SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.parseFrom(data);
        return new Message(gameMessage, datagram.getAddress(), datagram.getPort());
    }

    @Override
    public void close() {
        datagramSocket.close();
    }
}
