package ru.nsu.gemuev.net4.model.communication;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.nsu.gemuev.net4.SnakesProto;

import java.net.InetAddress;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public class Message {
    private final SnakesProto.GameMessage message;
    private final InetAddress receiverAddress;
    private final int receiverPort;
    @Setter
    private long sentAt = 0;
}
