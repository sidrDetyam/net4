package ru.nsu.gemuev.net4.mappers;

import lombok.NonNull;
import ru.nsu.gemuev.net4.SnakesProto;
import ru.nsu.gemuev.net4.model.communication.NodeRole;
import ru.nsu.gemuev.net4.model.communication.Node;
import ru.nsu.gemuev.net4.model.game.Direction;
import ru.nsu.gemuev.net4.model.game.GameConfig;
import ru.nsu.gemuev.net4.model.game.GameState;

import java.util.Collection;

public class MessageMapper {

    private MessageMapper(){}

    public static SnakesProto.GameMessage stateOf(@NonNull GameState gameState,
                                                  @NonNull Collection<? extends Node> nodes,
                                                  long msgSeq){

        var dtoState = StateMapper.model2Dto(gameState, nodes);
        return SnakesProto.GameMessage.newBuilder()
                .setState(SnakesProto.GameMessage.StateMsg.newBuilder().setState(dtoState).build())
                .setMsgSeq(msgSeq)
                .build();
    }

    public static SnakesProto.GameMessage steerOf(@NonNull Direction steer,
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

    public static SnakesProto.GameMessage announcementOf(@NonNull GameConfig gameConfig,
                                                         @NonNull Collection<? extends Node> players,
                                                         @NonNull String name,
                                                         boolean canJoin,
                                                         long msgSeq){
        var game = SnakesProto.GameAnnouncement
                .newBuilder()
                .setGameName(name)
                .setCanJoin(canJoin)
                .setConfig(GameConfigMapper.model2Dto(gameConfig))
                .setPlayers(PlayerMapper.dtoPlayers(players))
                .build();

        var annMsg = SnakesProto.GameMessage.AnnouncementMsg
                .newBuilder()
                .addGames(game)
                .build();

        return SnakesProto.GameMessage.newBuilder()
                .setAnnouncement(annMsg)
                .setMsgSeq(msgSeq)
                .build();
    }

    public static SnakesProto.GameMessage joinOf(@NonNull String gameName,
                                                 @NonNull String playerName,
                                                 @NonNull NodeRole role,
                                                 long msgSeq){
        return SnakesProto.GameMessage.newBuilder()
                .setJoin(SnakesProto.GameMessage.JoinMsg.newBuilder()
                        .setGameName(gameName)
                        .setPlayerName(playerName)
                        .setPlayerType(SnakesProto.PlayerType.HUMAN)
                        .setRequestedRole(NodeRoleMapper.model2Dto(role))
                        .build())
                .setMsgSeq(msgSeq)
                .build();
    }

    public static SnakesProto.GameMessage ackOf(int receiverId,
                                                long msgSeq){
        return SnakesProto.GameMessage.newBuilder()
                .setAck(SnakesProto.GameMessage.AckMsg.newBuilder().build())
                .setMsgSeq(msgSeq)
                .setReceiverId(receiverId)
                .build();
    }

    public static SnakesProto.GameMessage pingOf(int receiverId,
                                                long msgSeq){
        return SnakesProto.GameMessage.newBuilder()
                .setPing(SnakesProto.GameMessage.PingMsg.newBuilder().build())
                .setMsgSeq(msgSeq)
                .setReceiverId(receiverId)
                .build();
    }

    public static SnakesProto.GameMessage roleChangedOf(@NonNull NodeRole senderRole,
                                                        @NonNull NodeRole receiverRole,
                                                        int senderId,
                                                        int receiverId,
                                                        long msgSeq){
        var msg = SnakesProto.GameMessage.RoleChangeMsg
                .newBuilder()
                .setReceiverRole(NodeRoleMapper.model2Dto(receiverRole))
                .setSenderRole(NodeRoleMapper.model2Dto(senderRole))
                .build();
        return SnakesProto.GameMessage.newBuilder()
                .setRoleChange(msg)
                .setMsgSeq(msgSeq)
                .setReceiverId(receiverId)
                .setSenderId(senderId)
                .build();
    }

    public static SnakesProto.GameMessage errorOf(@NonNull String cause,
                                                  long msgSeq){
        var msg = SnakesProto.GameMessage.ErrorMsg
                .newBuilder()
                .setErrorMessage(cause)
                .build();
        return SnakesProto.GameMessage.newBuilder()
                .setError(msg)
                .setMsgSeq(msgSeq)
                .build();
    }
}
