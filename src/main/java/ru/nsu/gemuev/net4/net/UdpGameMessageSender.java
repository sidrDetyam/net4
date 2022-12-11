package ru.nsu.gemuev.net4.net;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ru.nsu.gemuev.net4.model.ports.GameMessageSender;
import ru.nsu.gemuev.net4.model.ports.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

@RequiredArgsConstructor
public class UdpGameMessageSender implements GameMessageSender {
    private final DatagramSocket datagramSocket;

    @Override
    public void sendGameMessage(@NonNull Message message) throws IOException {
        byte[] buff = message.getMessage().toByteArray();
        DatagramPacket msgPacket = new DatagramPacket(buff, buff.length, message.getAddress(), message.getPort());
        datagramSocket.send(msgPacket);
    }

    @Override
    public void close(){
        datagramSocket.close();
    }
}
