package ru.nsu.gemuev.net4.controllers.uievents;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.nsu.gemuev.net4.model.game.GameState;

@RequiredArgsConstructor
@Getter
public class GameStateChanged {
    private final GameState gameState;
}
