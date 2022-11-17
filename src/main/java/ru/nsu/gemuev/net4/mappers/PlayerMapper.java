package ru.nsu.gemuev.net4.mappers;

import lombok.NonNull;
import lombok.SneakyThrows;
import ru.nsu.gemuev.net4.SnakesProto;
import ru.nsu.gemuev.net4.model.Player;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

public class PlayerMapper {

    private PlayerMapper(){}

    @SneakyThrows
    public static Player dto2Model(@NonNull SnakesProto.GamePlayer dtoPlayer){
        return new Player(
                dtoPlayer.getName(),
                dtoPlayer.getId(),
                InetAddress.getByName(dtoPlayer.getIpAddress()),
                dtoPlayer.getPort(),
                NodeRoleMapper.dto2Model(dtoPlayer.getRole()),
                dtoPlayer.getScore(),
                Instant.now().toEpochMilli());
    }

    public static SnakesProto.GamePlayer model2Dto(@NonNull Player modelPlayer){
        var builder = SnakesProto.GamePlayer
                .newBuilder()
                .setName(modelPlayer.getName())
                .setId(modelPlayer.getId())
                .setPort(modelPlayer.getPort())
                .setRole(NodeRoleMapper.model2Dto(modelPlayer.getPlayerRole()))
                .setScore(modelPlayer.getScore());
        if(modelPlayer.getAddress() != null){
            builder.setIpAddress(modelPlayer.getAddress().getHostAddress());
        }
        return builder.build();
    }

    public static SnakesProto.GamePlayers dtoPlayers(@NonNull Collection<? extends Player> players){
        return SnakesProto.GamePlayers.newBuilder()
                .addAllPlayers(players.stream().map(PlayerMapper::model2Dto).toList())
                .build();
    }

    public static List<Player> modelPlayers(@NonNull SnakesProto.GamePlayers dtoPlayers){
        return dtoPlayers.getPlayersList().stream()
                .map(PlayerMapper::dto2Model).toList();
    }
}
