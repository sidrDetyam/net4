package ru.nsu.gemuev.net4.model.game;

import lombok.*;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode(of = "id")
@Getter
public class Player {
    private final String name;
    private final int id;
    @Setter
    private int killed;
}
