package ru.nsu.gemuev.net4.model.gameevents;

import com.google.inject.Inject;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.controlsfx.control.PropertySheet;
import ru.nsu.gemuev.net4.SnakesProto;
import ru.nsu.gemuev.net4.mappers.DirectionMapper;
import ru.nsu.gemuev.net4.model.Model;
import ru.nsu.gemuev.net4.net.MulticastReceiver;

import java.net.InetAddress;

@Log4j2
public class GameEventHandler{

    private final Model model;

    public GameEventHandler(Model model){
        this.model = model;
    }

    public void handle(@NonNull SnakesProto.GameMessage gameMessage, @NonNull InetAddress inetAddress, int port){

        var playersRep = model.getPlayersRepository();
        final int senderId = gameMessage.getSenderId();
        playersRep.updateTll(senderId);

        if(gameMessage.hasSteer()){
            var steer = gameMessage.getSteer();
            model.getGameState().getSnakes()
                    .stream()
                    .filter(snake -> snake.getPlayerId() == senderId)
                    .findFirst()
                    .ifPresent(snake -> snake.setHeadDirection(DirectionMapper.dto2Model(steer.getDirection())));
            return;
        }

        if(gameMessage.hasState()){
            var state = gameMessage.getState();

            return;
        }

        log.info("handler for this message isn`t impl " + gameMessage);
    }


}
