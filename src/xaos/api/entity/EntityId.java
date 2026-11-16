package xaos.api.entity;

/**
 * Stable runtime identifier for an entity in the current world.
 *
 * Entity IDs are assigned by the existing Towns entity system and
 * are unique across items, buildings and living entities.
 *
 * An EntityId identifies a runtime entity instance, not a definition.
 *
 * For example:
 *
 *     EntityId      -> this particular wooden sword
 *     DefinitionId  -> the wooden sword item type
 */
public record EntityId(int value) {

    public EntityId {
        if (value < 0) {
            throw new IllegalArgumentException(
                    "Entity ID cannot be negative"
            );
        }
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}