package ru.nsu.gemuev.net4.model.communication;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import ru.nsu.gemuev.net4.mappers.DirectionMapper;
import ru.nsu.gemuev.net4.mappers.NodeRoleMapper;
import ru.nsu.gemuev.net4.mappers.PlayerMapper;
import ru.nsu.gemuev.net4.mappers.StateMapper;
import ru.nsu.gemuev.net4.model.game.GameState;
import ru.nsu.gemuev.net4.model.ports.Message;

import java.net.InetAddress;
import java.util.List;

@Log4j2
@RequiredArgsConstructor
public class GameMessageHandler implements MessageHandler{

    private final CommunicationModel model;

    public void handle(@NonNull Message message){
        var gameMessage = message.getMessage();
        InetAddress address = message.getAddress();
        int port = message.getPort();

        if(gameMessage.hasSteer()){
            var steer = gameMessage.getSteer();
            model.steerMessage(DirectionMapper.dto2Model(steer.getDirection()), gameMessage.getMsgSeq(), address, port);
        }

        if(gameMessage.hasState()){
            var dtoState = gameMessage.getState().getState();
            GameState state = StateMapper.dto2Model(dtoState, model.getGameConfig());
            List<Node> players = PlayerMapper.nodes(dtoState.getPlayers());
            model.stateMessage(state, players, gameMessage.getMsgSeq(), address, port);
        }

        if(gameMessage.hasJoin()){
            var join = gameMessage.getJoin();
            model.joinMessage(join.getPlayerName(), NodeRoleMapper.dto2Model(join.getRequestedRole()),
                    gameMessage.getMsgSeq(), address, port);
        }

        if(gameMessage.hasAck()){
            model.ackMessage(gameMessage.getMsgSeq(), address, port);
        }

        if(gameMessage.hasPing()){
            model.pingMessage(gameMessage.getMsgSeq(), address, port);
        }

        if(gameMessage.hasRoleChange()){
            var roleChange = gameMessage.getRoleChange();
            model.roleChangedMessage(
                    NodeRoleMapper.dto2Model(roleChange.getReceiverRole()),
                    NodeRoleMapper.dto2Model(roleChange.getSenderRole()),
                    gameMessage.getSenderId(), gameMessage.getReceiverId(), gameMessage.getMsgSeq(),
                    address, port);
        }
    }

}
