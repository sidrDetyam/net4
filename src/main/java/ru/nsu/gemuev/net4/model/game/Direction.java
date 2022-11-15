package ru.nsu.gemuev.net4.model.game;

import lombok.NonNull;

public enum Direction {
    UP(0), RIGHT(1), DOWN(2), LEFT(3);

    private static final int[] shiftX = {0, 1, 0, -1};
    private static final int[] shiftY = {-1, 0, 1, 0};

    private final int index;

    Direction(int index) {
        this.index = index;
    }

    public Coordinate shift(@NonNull Coordinate coordinate, int sizeX, int sizeY) {
        return new Coordinate(
                (coordinate.x() + shiftX[index] + sizeX) % sizeX,
                (coordinate.y() + shiftY[index] + sizeY) % sizeY);
    }

    public Direction opposite() {
        return switch (this) {
            case DOWN -> UP;
            case UP -> DOWN;
            case RIGHT -> LEFT;
            case LEFT -> RIGHT;
        };
    }
}
