package ru.nsu.gemuev.net4.net;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Range;
import ru.nsu.gemuev.net4.SnakesProto;
import ru.nsu.gemuev.net4.model.ports.GameMessageReceiver;
import ru.nsu.gemuev.net4.model.ports.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

@RequiredArgsConstructor
public class UdpGameMessageReceiver implements GameMessageReceiver {
    private final DatagramSocket datagramSocket;

    @Override
    public Message receiveGameMessage(byte[] buffer) throws IOException {
        DatagramPacket datagram = new DatagramPacket(buffer, buffer.length);
        datagramSocket.receive(datagram);
        var data = Arrays.copyOf(datagram.getData(), datagram.getLength());
        SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.parseFrom(data);
        return new Message(gameMessage, datagram.getAddress(), datagram.getPort());
    }

    @Override
    public void setSoTimeout(@Range(from = 0, to = Integer.MAX_VALUE) int timeout) throws SocketException {
        datagramSocket.setSoTimeout(timeout);
    }

    @Override
    public void close(){
        datagramSocket.close();
    }
}
