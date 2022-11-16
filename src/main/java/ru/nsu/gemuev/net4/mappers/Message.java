package ru.nsu.gemuev.net4.mappers;

import lombok.NonNull;
import ru.nsu.gemuev.net4.SnakesProto;
import ru.nsu.gemuev.net4.model.Player;
import ru.nsu.gemuev.net4.model.game.Direction;
import ru.nsu.gemuev.net4.model.game.GameState;

import java.util.Collection;

public class Message {

    private Message(){}

    public static SnakesProto.GameMessage of(@NonNull GameState gameState,
                                             @NonNull Collection<Player> players,
                                             long msgSeq){

        var dtoState = StateMapper.model2Dto(gameState, players);
        return SnakesProto.GameMessage.newBuilder()
                .setState(SnakesProto.GameMessage.StateMsg.newBuilder().setState(dtoState).build())
                .setMsgSeq(msgSeq)
                .build();
    }

    public static SnakesProto.GameMessage of(@NonNull Direction steer,
                                             int senderId,
                                             long msgSeq){
        return SnakesProto.GameMessage.newBuilder()
                .setSteer(SnakesProto.GameMessage.SteerMsg.newBuilder()
                        .setDirection(DirectionMapper.model2Dto(steer))
                        .build())
                .setMsgSeq(msgSeq)
                .setSenderId(senderId)
                .build();
    }
}
