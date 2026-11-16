package xaos.api.mods;

/**
 * Entry point implemented by Java mods.
 *
 * The mod loader creates the implementation and invokes lifecycle
 * methods as the game changes state.
 */
public interface TownsMod {

    /**
     * Called once when the mod has been discovered and loaded.
     *
     * At this point the game world may not yet exist.
     */
    default void onLoad(ModContext context) {
    }

    /**
     * Called when a playable world has been created or loaded.
     *
     * Mods may safely query the World API from this point.
     */
    default void onWorldLoaded(ModContext context) {
    }

    /**
     * Called immediately before the current playable world is cleared.
     *
     * The World API is still available during this callback.
     *
     * After this method returns, all resources registered through
     * ModContext.worldResource(...) are automatically released.
     */
    default void onWorldUnloaded(ModContext context) {
    }

    /**
     * Called once when the Java mod loader is shutting down.
     *
     * Mod-scoped resources are automatically released after this callback.
     */
    default void onUnload(ModContext context) {
    }
}
