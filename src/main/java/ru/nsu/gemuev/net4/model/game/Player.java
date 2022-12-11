package ru.nsu.gemuev.net4.model.game;

import lombok.*;

@RequiredArgsConstructor
@EqualsAndHashCode(of = "id")
@Getter
public class Player {
    private final String name;
    private final int id;
    @Setter
    private int score;

    public void increaseScore(){
        score += 1;
    }

    @Override
    public String toString(){
        return "%s %d".formatted(name, score);
    }
}
