package xaos.api.buildings;

import java.util.List;

import xaos.api.definitions.DefinitionId;
import xaos.api.entity.EntityId;
import xaos.api.world.WorldPosition;

public record BuildingView(
        EntityId id,
        DefinitionId definitionId,
        WorldPosition origin,
        List<WorldPosition> occupiedPositions,
        boolean operative
) {

    public BuildingView {
        if (id == null) {
            throw new IllegalArgumentException(
                    "Building ID cannot be null"
            );
        }

        if (definitionId == null) {
            throw new IllegalArgumentException(
                    "Building definition cannot be null"
            );
        }

        if (origin == null) {
            throw new IllegalArgumentException(
                    "Building origin cannot be null"
            );
        }

        occupiedPositions = occupiedPositions == null
                ? List.of()
                : List.copyOf(occupiedPositions);
    }

    public boolean occupies(WorldPosition position) {
        return position != null
                && occupiedPositions.contains(position);
    }

    public int occupiedCellCount() {
        return occupiedPositions.size();
    }
}