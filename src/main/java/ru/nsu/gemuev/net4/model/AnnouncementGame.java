package ru.nsu.gemuev.net4.model;

import lombok.NonNull;
import ru.nsu.gemuev.net4.model.game.GameConfig;

import java.net.InetAddress;
import java.util.Objects;

public record AnnouncementGame(@NonNull InetAddress senderAddress,
                               int senderPort,
                               @NonNull String gameName,
                               int countOfPlayers,
                               @NonNull GameConfig gameConfig,
                               boolean canJoin) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnnouncementGame that = (AnnouncementGame) o;
        return senderPort == that.senderPort && senderAddress.equals(that.senderAddress) && gameName.equals(that.gameName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(senderAddress, senderPort, gameName);
    }

    @Override
    public String toString(){
        return String.format("%s [Players: %d, Width: %d, Height: %d, Food: %d, Delay: %d]",
                gameName,
                countOfPlayers,
                gameConfig().width(),
                gameConfig().height(),
                gameConfig().foodStatic(),
                gameConfig.delay());
    }
}
