package ru.nsu.gemuev.net4.model;

import lombok.*;

import java.net.InetAddress;

@EqualsAndHashCode
@AllArgsConstructor
@Getter
@ToString
public class HostAddress{
    private final InetAddress inetAddress;
    private final int port;
    private final String gameName;
}
