package ru.nsu.gemuev.net4.controllers.uievents;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.nsu.gemuev.net4.SnakesProto.GameAnnouncement;

import java.util.List;

@AllArgsConstructor
@Getter
public class ListOfAnnGamesChangedEvent {
    private final List<GameAnnouncement> games;
}
