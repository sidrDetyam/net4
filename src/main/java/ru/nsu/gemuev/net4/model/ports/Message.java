package ru.nsu.gemuev.net4.model.ports;

import lombok.*;
import ru.nsu.gemuev.net4.SnakesProto;

import java.net.InetAddress;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
public class Message {
    private final SnakesProto.GameMessage message;
    private final InetAddress address;
    private final int port;
    @Setter
    private long sentAt;
}
