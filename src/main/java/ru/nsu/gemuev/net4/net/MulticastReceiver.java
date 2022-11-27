package ru.nsu.gemuev.net4.net;

import lombok.NonNull;
import ru.nsu.gemuev.net4.model.ports.GameMessageReceiver;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;

public class MulticastReceiver implements GameMessageReceiver, Closeable {

    private final MulticastSocket multicastSocket;

    public MulticastReceiver(@NonNull SocketAddress socketAddress,
                             @NonNull NetworkInterface netInterface,
                             int port) throws IOException{
        if(port<0 || 65535<port){
            throw new IllegalArgumentException("Incorrect port");
        }
        multicastSocket = new MulticastSocket(port);
        multicastSocket.joinGroup(socketAddress, netInterface);
    }

    @Override
    public DatagramPacket receiveGameMessage(byte[] buffer) throws IOException{
        DatagramPacket datagram = new DatagramPacket(buffer, buffer.length);
        multicastSocket.receive(datagram);
        return datagram;
    }

    @Override
    public void close(){
        multicastSocket.close();
    }
}