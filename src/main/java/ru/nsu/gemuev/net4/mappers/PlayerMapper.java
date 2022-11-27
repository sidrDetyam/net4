package ru.nsu.gemuev.net4.mappers;

import lombok.NonNull;
import lombok.SneakyThrows;
import ru.nsu.gemuev.net4.SnakesProto;
import ru.nsu.gemuev.net4.model.communication.Node;
import ru.nsu.gemuev.net4.model.game.Player;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

public class PlayerMapper {

    private PlayerMapper(){}

    @SneakyThrows
    public static Node dto2Model(@NonNull SnakesProto.GamePlayer dtoPlayer){
        return new Node(
                new Player(dtoPlayer.getName(), dtoPlayer.getId()),
                InetAddress.getByName(dtoPlayer.getIpAddress()),
                dtoPlayer.getPort(),
                NodeRoleMapper.dto2Model(dtoPlayer.getRole()),
                Instant.now().toEpochMilli());
    }

    public static SnakesProto.GamePlayer model2Dto(@NonNull Node node){
        var builder = SnakesProto.GamePlayer
                .newBuilder()
                .setName(node.getPlayer().getName())
                .setId(node.getPlayerId())
                .setPort(node.getPort())
                .setRole(NodeRoleMapper.model2Dto(node.getRole()))
                .setScore(0);
        if(node.getAddress() != null){
            builder.setIpAddress(node.getAddress().getHostAddress());
        }
        else{
            builder.setIpAddress("");
        }
        return builder.build();
    }

    public static SnakesProto.GamePlayers dtoPlayers(@NonNull Collection<? extends Node> nodes){
        return SnakesProto.GamePlayers.newBuilder()
                .addAllPlayers(nodes.stream().map(PlayerMapper::model2Dto).toList())
                .build();
    }

    public static List<Node> nodes(@NonNull SnakesProto.GamePlayers dtoPlayers){
        return dtoPlayers.getPlayersList().stream()
                .map(PlayerMapper::dto2Model).toList();
    }
}
