package ru.nsu.gemuev.net4.model;

import lombok.*;

import java.net.InetAddress;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@ToString
public class Player {
    private final String name;
    private final int id;
    private InetAddress address;
    private int port;
    private NodeRole playerRole;
    private int score;
    private long lastComm;
}
