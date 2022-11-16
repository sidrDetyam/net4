package ru.nsu.gemuev.net4.mappers;

import lombok.NonNull;
import ru.nsu.gemuev.net4.SnakesProto;
import ru.nsu.gemuev.net4.model.NodeRole;

public class NodeRoleMapper {

    private NodeRoleMapper(){}

    public static NodeRole dto2Model(@NonNull SnakesProto.NodeRole dtoRole){
        return switch (dtoRole) {
            case MASTER -> NodeRole.MASTER;
            case NORMAL -> NodeRole.NORMAL;
            case VIEWER -> NodeRole.VIEWER;
            case DEPUTY -> NodeRole.DEPUTY;
        };
    }

    public static SnakesProto.NodeRole model2Dto(@NonNull NodeRole modelRole){
        return switch (modelRole) {
            case MASTER -> SnakesProto.NodeRole.MASTER;
            case NORMAL -> SnakesProto.NodeRole.NORMAL;
            case VIEWER -> SnakesProto.NodeRole.VIEWER;
            case DEPUTY -> SnakesProto.NodeRole.DEPUTY;
        };
    }
}
