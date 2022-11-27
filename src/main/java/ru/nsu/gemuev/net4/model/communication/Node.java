package ru.nsu.gemuev.net4.model.communication;

import lombok.*;
import ru.nsu.gemuev.net4.model.NodeRole;
import ru.nsu.gemuev.net4.model.game.Player;

import java.net.InetAddress;
import java.time.Instant;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode(of = {"player"})
public class Node {
    private final Player player;
    private final InetAddress address;
    private final int port;
    @Setter
    private NodeRole role;
    private long lastCommunication;

    public int getPlayerId(){
        return player.getId();
    }

    public void updateLastCommunication(){
        lastCommunication = Instant.now().toEpochMilli();
    }
}
