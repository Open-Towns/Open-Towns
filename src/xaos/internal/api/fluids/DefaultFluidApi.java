package xaos.internal.api.fluids;


import xaos.api.GameApiBootstrap;
import xaos.api.definitions.DefinitionId;
import xaos.api.fluids.FluidApi;
import xaos.api.fluids.FluidMutationResult;
import xaos.api.fluids.FluidView;
import xaos.api.world.WorldPosition;

import xaos.main.Game;
import xaos.main.World;
import xaos.tasks.Task;
import xaos.tasks.TaskTypes;
import xaos.tiles.Cell;
import xaos.tiles.terrain.Terrain;
import xaos.utils.Point3D;

public final class DefaultFluidApi
        implements FluidApi {

    @Override
    public FluidView at(
            WorldPosition position) {

        if (!isValid(position)) {
            return null;
        }

        Cell cell = World.getCell(
                position.x(),
                position.y(),
                position.z());

        int internalType = cell.getTerrain()
                .getFluidType();

        int count = cell.getTerrain()
                .getFluidCount();

        return new FluidView(
                position,
                mapType(internalType),
                count,
                count == Terrain.FLUIDS_COUNT_MAX,
                count == Terrain.FLUIDS_COUNT_INFINITE);
    }

    @Override
    public FluidMutationResult fill(
            WorldPosition position,
            DefinitionId fluidId) {

        if (!isValid(position)) {
            return FluidMutationResult.rejected(
                    "Position is outside the world");
        }

        if (fluidId == null) {
            return FluidMutationResult.rejected(
                    "A fluid definition is required");
        }

        int requestedType =
                toInternalType(fluidId);

        if (requestedType < 0) {
            return FluidMutationResult.rejected(
                    "Unknown or unsupported fluid: "
                            + fluidId);
        }

        Cell cell = World.getCell(
                position.x(),
                position.y(),
                position.z());

        /*
         * Fluids can only occupy open/mined cells
         * in the normal simulation.
         */
        if (!cell.isMined()) {
            return FluidMutationResult.rejected(
                    "Fluid can only be placed in a mined cell");
        }

        int existingCount = cell.getTerrain()
                .getFluidCount();

        int existingType = cell.getTerrain()
                .getFluidType();

        if (existingCount
                == Terrain.FLUIDS_COUNT_INFINITE) {

            return FluidMutationResult.rejected(
                    "Cell already contains infinite fluid");
        }

        if (existingCount
                == Terrain.FLUIDS_COUNT_MAX
                && existingType == requestedType) {

            return FluidMutationResult.rejected(
                    "Cell is already full");
        }

        /*
         * Don't implicitly mix different fluids through
         * the public API.
         */
        if (existingCount > 0
                && existingType != requestedType) {

            return FluidMutationResult.rejected(
                    "Cell already contains a different fluid");
        }

        Task task = new Task(
                TaskTypes.TASK_TERRAIN_ADD_FLUID);

        task.setPointIni(
                new Point3D(
                        position.x(),
                        position.y(),
                        position.z()));

        task.setParameter(
                Integer.toString(
                        requestedType));

        task.setParameter2(
                Integer.toString(
                        Terrain.FLUIDS_COUNT_MAX));

        /*
         * Use Towns' existing fluid task implementation.
         *
         * This performs the required fluid processing,
         * redraw and pathfinding updates.
         */
        Game.getWorld()
                .getTaskManager()
                .executeTaskImmediately(task);

        return FluidMutationResult.success();
    }

    @Override
    public FluidMutationResult remove(
            WorldPosition position) {

        if (!isValid(position)) {
            return FluidMutationResult.rejected(
                    "Position is outside the world");
        }

        Cell cell = World.getCell(
                position.x(),
                position.y(),
                position.z());

        if (cell.getTerrain()
                .getFluidCount() <= 0) {

            return FluidMutationResult.rejected(
                    "Cell does not contain fluid");
        }

        Task task = new Task(
                TaskTypes.TASK_TERRAIN_REMOVE_FLUID);

        task.setPointIni(
                new Point3D(
                        position.x(),
                        position.y(),
                        position.z()));

        Game.getWorld()
                .getTaskManager()
                .executeTaskImmediately(task);

        return FluidMutationResult.success();
    }
    @Override
public boolean supports(
        DefinitionId fluidId) {

    return toInternalType(fluidId) >= 0;
}

    private static boolean isValid(
            WorldPosition position) {

        return position != null
        && GameApiBootstrap.get()
                .world()
                .isValidPosition(position);
    }

    /*
     * For now the engine only knows about Towns'
     * built-in water and lava fluid types.
     *
     * Later this can be replaced by a proper fluid
     * definition registry for namespaced mod fluids.
     */
    private static int toInternalType(
            DefinitionId fluidId) {

        if (fluidId == null
                || !fluidId.isDefaultNamespace()) {

            return -1;
        }

        return switch (fluidId.path()) {
            case "water" ->
                Terrain.FLUIDS_WATER;

            case "lava" ->
                Terrain.FLUIDS_LAVA;

            default ->
                -1;
        };
    }

    private static DefinitionId mapType(
            int type) {

        if (type == Terrain.FLUIDS_WATER) {
            return FluidApi.WATER;
        }

        if (type == Terrain.FLUIDS_LAVA) {
            return FluidApi.LAVA;
        }

        return null;
    }
}