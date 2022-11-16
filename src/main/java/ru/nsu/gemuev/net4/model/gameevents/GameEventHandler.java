package ru.nsu.gemuev.net4.model.gameevents;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import ru.nsu.gemuev.net4.SnakesProto;
import ru.nsu.gemuev.net4.mappers.DirectionMapper;
import ru.nsu.gemuev.net4.mappers.PlayerMapper;
import ru.nsu.gemuev.net4.mappers.StateMapper;
import ru.nsu.gemuev.net4.model.Model;
import ru.nsu.gemuev.net4.model.NodeRole;
import ru.nsu.gemuev.net4.model.Player;
import ru.nsu.gemuev.net4.model.game.GameState;

import java.net.InetAddress;
import java.util.ArrayList;

@Log4j2
public class GameEventHandler{

    private final Model model;

    public GameEventHandler(Model model){
        this.model = model;
    }

    public void handle(@NonNull SnakesProto.GameMessage gameMessage, @NonNull InetAddress inetAddress, int port){

        log.info(gameMessage);

        var playersRep = model.getPlayersRepository();
        final int senderId = gameMessage.getSenderId();
        playersRep.updateTll(senderId);

        if(gameMessage.hasSteer()){
            if(model.getMyRole() != NodeRole.MASTER){
                return;
            }
            var steer = gameMessage.getSteer();
            model.getGameState().getSnakes()
                    .stream()
                    .filter(snake -> snake.getPlayerId() == senderId)
                    .findFirst()
                    .ifPresent(snake -> snake.setHeadDirection(DirectionMapper.dto2Model(steer.getDirection())));
            return;
        }

        if(gameMessage.hasState()){
            if(model.getMyRole() == NodeRole.MASTER){
                return;
            }
            var dtoState = gameMessage.getState().getState();
            GameState state = StateMapper.dto2Model(dtoState, model.getGameConfig());
            model.stateChanged(state);

            var players = new ArrayList<Player>();
            for(var dtoPlayer : dtoState.getPlayers().getPlayersList()){
                players.add(PlayerMapper.dto2Model(dtoPlayer));
            }
            playersRep.updatePlayers(players);

            return;
        }

        if(gameMessage.hasAnnouncement()){
            var announcements = gameMessage.getAnnouncement().getGamesList();
            for(var ann : announcements){
                model.getGamesRepository().addGame(inetAddress, port, ann);
            }
        }

        if(gameMessage.hasJoin()){
            model.playerJoin(inetAddress, port);
        }

        if(gameMessage.hasAck()){
            model.joinGame(gameMessage.getReceiverId());
        }

        log.info("handler for this message isn`t impl " + gameMessage);
    }


}
