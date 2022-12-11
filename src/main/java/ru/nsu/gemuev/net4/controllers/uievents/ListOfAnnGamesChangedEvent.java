package ru.nsu.gemuev.net4.controllers.uievents;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.nsu.gemuev.net4.model.AnnouncementGame;

import java.util.Collection;

@AllArgsConstructor
@Getter
public class ListOfAnnGamesChangedEvent {
    private final Collection<? extends AnnouncementGame> games;
}
