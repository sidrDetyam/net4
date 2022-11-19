package ru.nsu.gemuev.net4.model;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.Range;
import ru.nsu.gemuev.net4.SnakesProto;
import ru.nsu.gemuev.net4.mappers.*;
import ru.nsu.gemuev.net4.model.game.GameState;

import java.net.InetAddress;
import java.util.List;

@Log4j2
public class GameEventHandler{

    private final Model model;

    public GameEventHandler(@NonNull Model model){
        this.model = model;
    }

    public void handle(@NonNull SnakesProto.GameMessage gameMessage,
                       @NonNull InetAddress address,
                       @Range(from = 0, to = 65536) int port){
        System.out.println(gameMessage.getMsgSeq() + " " + port + " " + gameMessage.hasPing() + " " + gameMessage.hasAck());
        model.updateLastComm(address, port);
        var sender = model.getGameMessageConfirmingSender();

        if(gameMessage.hasSteer()){
            var steer = gameMessage.getSteer();
            model.steerMessage(DirectionMapper.dto2Model(steer.getDirection()), gameMessage.getMsgSeq(), address, port);
            return;
        }

        if(gameMessage.hasState()){
            var dtoState = gameMessage.getState().getState();
            GameState state = StateMapper.dto2Model(dtoState, model.getGameConfig());
            List<Player> players = PlayerMapper.modelPlayers(dtoState.getPlayers());
            model.stateMessage(state, players, gameMessage.getMsgSeq(), address, port);
            return;
        }

        if(gameMessage.hasAnnouncement()){
            var announcements = gameMessage.getAnnouncement().getGamesList();
            for(var ann : announcements){
                System.out.println("count: " + ann.getPlayers().getPlayersCount());
                model.addGame(AnnouncementMapper.of(ann, address, port));
            }
            return;
        }

        if(gameMessage.hasJoin()){
            var join = gameMessage.getJoin();
            model.joinMessage(join.getPlayerName(), NodeRoleMapper.dto2Model(join.getRequestedRole()),
                    gameMessage.getMsgSeq(), address, port);
            return;
        }

        if(gameMessage.hasAck()){
            model.ackMessage(gameMessage.getReceiverId(), gameMessage.getMsgSeq(),
                    address, port);
            return;
        }

        if(gameMessage.hasPing()){
            model.pingMessage(gameMessage.getMsgSeq(), address, port);
            return;
        }

        log.error("handler for this message isn`t impl " + gameMessage);
    }

}
