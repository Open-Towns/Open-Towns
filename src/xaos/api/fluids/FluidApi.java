package xaos.api.fluids;

import xaos.api.definitions.DefinitionId;
import xaos.api.world.WorldPosition;

/**
 * Public API for querying and modifying fluids in the world.
 *
 * <p>SDK 0.1 supports the two fluid types currently provided
 * by the Towns engine: water and lava.</p>
 *
 * <p>The API uses DefinitionId so future SDK versions can
 * support namespaced mod-defined fluids without changing the
 * method signatures.</p>
 */
public interface FluidApi {

    DefinitionId WATER =
            DefinitionId.towns("water");

    DefinitionId LAVA =
            DefinitionId.towns("lava");

    /**
     * Returns the fluid state at a world position.
     *
     * @return the fluid view, or null if the position is invalid
     */
    FluidView at(
            WorldPosition position);

    /**
     * Returns whether this SDK/runtime supports the supplied
     * fluid definition.
     *
     * SDK 0.1 currently supports:
     *
     * towns:water
     * towns:lava
     */
    boolean supports(
            DefinitionId fluidId);

    /**
     * Fills a cell with the requested fluid.
     *
     * The cell must be a valid mined/open cell.
     * Unsupported fluid IDs are rejected.
     */
    FluidMutationResult fill(
            WorldPosition position,
            DefinitionId fluidId);

    /**
     * Removes fluid from a cell.
     */
    FluidMutationResult remove(
            WorldPosition position);
}