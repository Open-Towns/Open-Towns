package xaos.api.mods;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks resources that share the same mod lifecycle.
 *
 * Mods normally obtain a scope through ModContext rather than constructing
 * one directly.
 */
 final class ModResourceScope {

    private final List<ModResource> resources =
            new ArrayList<>();

    /**
     * Adds a resource to this scope and returns the same resource so calls
     * can remain fluent.
     */
    public synchronized <T extends ModResource> T track(
            T resource
    ) {
        if (resource == null) {
            throw new IllegalArgumentException(
                    "Mod resource cannot be null"
            );
        }

        resources.add(resource);

        return resource;
    }

    /**
     * Releases every resource currently owned by this scope.
     *
     * Package-private: lifecycle ownership belongs to the mod loader.
     */
    synchronized void clear() {

        List<ModResource> snapshot =
                new ArrayList<>(resources);

        resources.clear();

        /*
         * Reverse order mirrors common resource-stack semantics:
         * the most recently registered resource is released first.
         */
        for (int i = snapshot.size() - 1; i >= 0; i--) {
            try {
                snapshot.get(i).close();
            } catch (RuntimeException ignored) {
                /*
                 * Cleanup is best-effort. One broken resource must not
                 * prevent the rest of the mod's resources from closing.
                 */
            }
        }
    }

    synchronized int size() {
        return resources.size();
    }
}
