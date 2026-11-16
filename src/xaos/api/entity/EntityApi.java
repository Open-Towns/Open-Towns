package xaos.api.entity;

import java.util.Optional;

/**
 * Public API for querying runtime entities in the current world.
 */
public interface EntityApi {

    /**
     * Returns a read-only snapshot of the entity with the supplied ID.
     *
     * Returns null if the entity cannot be found.
     */
    EntityView get(EntityId id);

    /**
     * Null-safe alternative to get().
     */
    default Optional<EntityView> find(EntityId id) {
        return Optional.ofNullable(get(id));
    }

    /**
     * Convenience overload using the raw runtime entity ID.
     */
    default EntityView get(int id) {
        if (id < 0) {
            return null;
        }

        return get(new EntityId(id));
    }
}