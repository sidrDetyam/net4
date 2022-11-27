package ru.nsu.gemuev.net4.model;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import ru.nsu.gemuev.net4.SnakesProto;
import ru.nsu.gemuev.net4.mappers.AnnouncementMapper;
import ru.nsu.gemuev.net4.model.communication.MessageHandler;
import ru.nsu.gemuev.net4.model.ports.Message;

import java.net.InetAddress;

@Log4j2
@RequiredArgsConstructor
public class AnnouncementMessageHandler implements MessageHandler {
    private final Model model;

    public void handle(@NonNull Message message){
        InetAddress address = message.getAddress();
        int port = message.getPort();
        SnakesProto.GameMessage msg = message.getMessage();

        if(msg.hasAnnouncement()){
            msg.getAnnouncement().getGamesList().forEach(game ->
                    model.announcementGameMessage(AnnouncementMapper.of(game, address, port)));
            return;
        }

        log.error("This message is not allowed here " + message.getMessage());
    }
}
