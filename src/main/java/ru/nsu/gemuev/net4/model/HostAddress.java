package ru.nsu.gemuev.net4.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.net.InetAddress;

@EqualsAndHashCode
@AllArgsConstructor
public class HostAddress{
    private final InetAddress inetAddress;
    private final int port;
    private final String gameName;
}
