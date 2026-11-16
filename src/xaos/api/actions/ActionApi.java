package xaos.api.actions;

import xaos.api.definitions.DefinitionId;
import xaos.api.world.WorldPosition;

public interface ActionApi {

    ActionResult queueAt(
            DefinitionId actionId,
            WorldPosition position
    );
}