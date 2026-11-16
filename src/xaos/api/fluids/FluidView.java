package xaos.api.fluids;

import xaos.api.definitions.DefinitionId;
import xaos.api.world.WorldPosition;

public record FluidView(
        WorldPosition position,
        DefinitionId fluidId,
        int level,
        boolean full,
        boolean infinite
) {

    public boolean empty() {
        return fluidId == null || level <= 0;
    }
}