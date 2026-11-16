package xaos.api.items;

import xaos.api.definitions.DefinitionId;
import xaos.api.entity.EntityId;
import xaos.api.world.WorldPosition;

/**
 * Read-only snapshot of an item currently placed in the world.
 */
public record ItemView(
        EntityId id,
        DefinitionId definitionId,
        WorldPosition position,
        ItemFacing facing,
        boolean operative,
        boolean locked
) {

    public ItemView {
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

        if (position == null) {
            throw new IllegalArgumentException(
                    "Item position cannot be null"
            );
        }

        if (facing == null) {
            throw new IllegalArgumentException(
                    "Item facing cannot be null"
            );
        }
    }
}
