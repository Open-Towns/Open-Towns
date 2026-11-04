package xaos.api.mods;

import xaos.api.GameApi;

/**
 * Context supplied to a Java mod by the mod loader.
 *
 * Provides access to the public game API and information about
 * the currently loaded mod.
 */
public final class ModContext {

    private final ModMetadata metadata;
    private final GameApi api;
    private final ModLogger logger;
    private final ModSaveData saveData;

    private final ModResourceScope worldResources = new ModResourceScope();

    private final ModResourceScope modResources = new ModResourceScope();

   ModContext(
            ModMetadata metadata,
            GameApi api,
            ModLogger logger,
            ModSaveData saveData) {
        if (metadata == null) {
            throw new IllegalArgumentException(
                    "Mod metadata cannot be null");
        }

        if (api == null) {
            throw new IllegalArgumentException(
                    "Game API cannot be null");
        }

        if (logger == null) {
            throw new IllegalArgumentException(
                    "Mod logger cannot be null");
        }

        if (saveData == null) {
            throw new IllegalArgumentException(
                    "Mod save data cannot be null");
        }

        this.metadata = metadata;
        this.api = api;
        this.logger = logger;
        this.saveData = saveData;
    }

    /**
     * Metadata describing the currently loaded mod.
     */
    public ModMetadata metadata() {
        return metadata;
    }

    /**
     * Convenience accessor for the mod ID.
     */
    public String modId() {
        return metadata.id();
    }

    /**
     * Entry point to the public Open Towns API.
     */
    public GameApi api() {
        return api;
    }

    /**
     * Logger scoped to this mod.
     */
    public ModLogger logger() {
        return logger;
    }

    /**
     * Persistent save data belonging to this mod.
     */
    public ModSaveData saveData() {
        return saveData;
    }

    /**
     * Tracks a resource that belongs to the currently loaded world.
     *
     * World resources are automatically released after
     * onWorldUnloaded().
     *
     * Example:
     *
     * context.worldResource(
     * context.api().events().subscribe(...)
     * );
     */
    public <T extends ModResource> T worldResource(
            T resource) {
        return worldResources.track(resource);
    }

    /**
     * Tracks a resource that belongs to the lifetime of the mod.
     *
     * Use this only for registrations that intentionally survive
     * world changes. Most gameplay registrations should use
     * worldResource(...).
     */
    public <T extends ModResource> T resource(
            T resource) {
        return modResources.track(resource);
    }

    /**
     * Releases resources belonging to the current world.
     *
     * Called by the mod loader during world teardown.
     */
    void clearWorldResources() {
        worldResources.clear();
    }

    /**
     * Releases all resources belonging to this mod.
     *
     * Called by the mod loader during shutdown.
     */
    void clearAllResources() {
        worldResources.clear();
        modResources.clear();
    }
}