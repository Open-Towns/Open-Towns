package xaos.api.entity;

import xaos.api.definitions.DefinitionId;
import xaos.api.world.WorldPosition;

/**
 * Read-only snapshot of a runtime entity.
 *
 * This is the common representation shared by items, buildings,
 * living entities and other runtime entities.
 */
public record EntityView(
        EntityId id,
        EntityType type,
        DefinitionId definitionId,
        WorldPosition position
) {

    /**
     * Returns true when this entity has an associated game definition.
     *
     * Most normal entities such as items, buildings and living entities
     * do. Some internal/special entities may not.
     */
    public boolean hasDefinition() {
        return definitionId != null;
    }
}