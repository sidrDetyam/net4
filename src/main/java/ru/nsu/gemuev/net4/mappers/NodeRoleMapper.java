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
}
