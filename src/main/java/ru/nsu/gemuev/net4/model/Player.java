package ru.nsu.gemuev.net4.model;

import lombok.Data;
import lombok.NonNull;

import java.net.InetAddress;

@Data
public class Player {
    private final String name;
    private final int id;
    private InetAddress address;
    private int port;
    private NodeRole playerRole;
    private int score;

    private long lastComm;

    public Player(@NonNull String name, int id){
        this.name = name;
        this.id = id;
    }
}
