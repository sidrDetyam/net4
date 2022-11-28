package ru.nsu.gemuev.net4.model.communication;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import ru.nsu.gemuev.net4.SnakesProto;
import ru.nsu.gemuev.net4.mappers.MessageMapper;
import ru.nsu.gemuev.net4.model.AnnouncementGame;
import ru.nsu.gemuev.net4.model.game.GameConfig;
import ru.nsu.gemuev.net4.model.game.GameState;
import ru.nsu.gemuev.net4.model.game.Player;
import ru.nsu.gemuev.net4.model.ports.GameMessageReceiver;
import ru.nsu.gemuev.net4.model.ports.GameMessageSender;
import ru.nsu.gemuev.net4.model.ports.Message;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Instant;
import java.util.function.Consumer;

@Log4j2
@RequiredArgsConstructor
public class CommunicationModelBuilder {
    private static final int COUNT_OF_ATTEMPTS = 100500;
    private static final int TIMEOUT = 500;
    private static final int BUFFER_SIZE = 10000;

    private final GameMessageReceiver receiver;
    private final GameMessageSender sender;
    private final String playerName;
    private final InetAddress mAddress;
    private final int mPort;
    private final Consumer<? super GameState> onStateChanged;

    public CommunicationModel joinToGame(NodeRole role,
                                         @NonNull AnnouncementGame announcementGame) throws JoinGameException {
        try {
            SnakesProto.GameMessage joinMsg = MessageMapper
                    .joinOf(announcementGame.gameName(), playerName, role, 0);
            Message message = new Message(joinMsg, announcementGame.senderAddress(), announcementGame.senderPort());
            byte[] buffer = new byte[BUFFER_SIZE];
            for (int i = 0; i < COUNT_OF_ATTEMPTS; ++i) {
                try {
                    receiver.setSoTimeout(TIMEOUT);
                    sender.sendGameMessage(message);
                    Message response = receiver.receiveGameMessage(buffer);

                    if(response.getMessage().hasAck()){
                        long instant = Instant.now().toEpochMilli();
                        Node master = new Node(new Player("", response.getMessage().getSenderId()),
                                response.getAddress(), response.getPort(), NodeRole.MASTER, instant);
                        Node me = new Node(new Player("", response.getMessage().getReceiverId()),
                                InetAddress.getLocalHost(), 0, role, instant);
                        return new CommunicationModel(mAddress, mPort, onStateChanged, receiver, sender,
                                master, me, announcementGame.gameConfig(), announcementGame.gameName());
                    }

                    if(response.getMessage().hasError()){
                        throw new JoinGameException(response.getMessage().getError().toString());
                    }
                }
                catch (IOException exception){
                    log.error(exception);
                }
            }
            throw new JoinGameException("Unable to join");
        }
        finally {
            try {
                receiver.setSoTimeout(0);
            }
            catch (IOException e){
                log.error(e);
            }
        }
    }

    @SneakyThrows
    public CommunicationModel createGame(@NonNull String gameName,
                                         @NonNull GameConfig gameConfig){
        Node master = new Node(new Player(playerName, 0),
                InetAddress.getLocalHost(), 0, NodeRole.MASTER, 0);
        return new CommunicationModel(mAddress, mPort, onStateChanged, receiver, sender,
                master, master, gameConfig, gameName);
    }
}
