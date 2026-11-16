package xaos.api.ui;

import xaos.api.mods.ModResource;

/**
 * Represents a registered mod UI action.
 *
 * The registration can be tracked through ModContext.resource()
 * or ModContext.worldResource() so it is automatically removed
 * during lifecycle cleanup.
 */
public interface UiActionRegistration
        extends ModResource {

    /**
     * Removes the UI action.
     *
     * Calling this more than once has no effect.
     */
    void unregister();

    /**
     * Returns whether this registration is still active.
     */
    boolean isActive();

    /**
     * Returns the namespaced action ID.
     */
    String id();

    /**
     * Returns the action's display label.
     */
    String label();

    @Override
    default void close() {
        unregister();
    }
}