package xaos.api.definitions;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Read-only registry of game content definitions.
 *
 * <p>The first public API intentionally exposes lookup/discovery only. Content
 * registration will be added once the XML loaders and runtime managers share a
 * single registration path.</p>
 */
public interface DefinitionRegistry<T extends Definition> {

    /**
     * Returns a definition or {@code null} when it does not exist.
     */
    T get(String id);

    default T get(DefinitionId id) {
        return id == null ? null : get(id.toString());
    }

    default Optional<T> find(String id) {
        return Optional.ofNullable(get(id));
    }

    default boolean contains(String id) {
        return get(id) != null;
    }

    List<T> all();

    Set<DefinitionId> ids();

    default int size() {
        return all().size();
    }
}
