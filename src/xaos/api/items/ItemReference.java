package xaos.api.items;

import xaos.api.definitions.DefinitionId;
import xaos.api.entity.EntityId;

public record ItemReference(
        EntityId id,
        DefinitionId definitionId,
        ItemLocationType locationType,
        EntityId holderId
) {

    public ItemReference {
        if (id == null) {
            throw new IllegalArgumentException(
                    "Item ID cannot be null"
            );
        }

        if (definitionId == null) {
            throw new IllegalArgumentException(
                    "Item definition cannot be null"
            );
        }

        if (locationType == null) {
            throw new IllegalArgumentException(
                    "Item location cannot be null"
            );
        }
    }

    public boolean isInWorld() {
        return locationType == ItemLocationType.WORLD;
    }

    public boolean isInContainer() {
        return locationType == ItemLocationType.CONTAINER;
    }
}
