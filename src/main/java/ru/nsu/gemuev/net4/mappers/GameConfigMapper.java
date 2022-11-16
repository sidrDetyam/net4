package ru.nsu.gemuev.net4.mappers;

import lombok.NonNull;
import ru.nsu.gemuev.net4.SnakesProto;
import ru.nsu.gemuev.net4.model.game.GameConfig;

public class GameConfigMapper {

    private GameConfigMapper() {
    }

    public static SnakesProto.GameConfig model2Dto(@NonNull GameConfig modelConfig) {
        return SnakesProto.GameConfig.newBuilder()
                .setStateDelayMs(modelConfig.delay())
                .setFoodStatic(modelConfig.foodStatic())
                .setWidth(modelConfig.width())
                .setHeight(modelConfig.height())
                .build();
    }

    public static GameConfig dto2Model(@NonNull SnakesProto.GameConfig dtoConfig) {
        return new GameConfig(dtoConfig.getWidth(), dtoConfig.getHeight(),
                dtoConfig.getFoodStatic(), dtoConfig.getStateDelayMs());
    }
}
