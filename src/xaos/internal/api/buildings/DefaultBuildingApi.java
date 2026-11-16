package xaos.internal.api.buildings;

import java.util.ArrayList;
import java.util.List;

import xaos.api.buildings.BuildingApi;
import xaos.api.buildings.BuildingView;
import xaos.api.definitions.DefinitionId;
import xaos.api.entity.EntityId;
import xaos.api.world.WorldPosition;

import xaos.internal.api.definitions.DefinitionKind;
import xaos.internal.api.definitions.DefinitionOwnershipRegistry;
import xaos.internal.api.definitions.DefinitionResolver;

import xaos.main.World;
import xaos.tiles.entities.buildings.Building;
import xaos.tiles.entities.buildings.BuildingManager;
import xaos.tiles.entities.buildings.BuildingManagerItem;

public final class DefaultBuildingApi
        implements BuildingApi {

    @Override
    public BuildingView get(
            EntityId id
    ) {
        if (id == null) {
            return null;
        }

        for (Building building
                : World.getBuildings()) {

            if (building.getID()
                    == id.value()) {

                return map(building);
            }
        }

        return null;
    }

    @Override
    public BuildingView at(
            WorldPosition position
    ) {
        if (position == null) {
            return null;
        }

        if (position.x() < 0
                || position.x() >= World.MAP_WIDTH
                || position.y() < 0
                || position.y() >= World.MAP_HEIGHT
                || position.z() < 0
                || position.z() >= World.MAP_DEPTH) {

            return null;
        }

        return map(
                Building.getBuilding(
                        position.x(),
                        position.y(),
                        position.z()
                )
        );
    }

    @Override
    public List<BuildingView> all() {
        List<BuildingView> result =
                new ArrayList<>();

        for (Building building
                : World.getBuildings()) {

            BuildingView view =
                    map(building);

            if (view != null) {
                result.add(view);
            }
        }

        return List.copyOf(result);
    }

    @Override
    public List<BuildingView> findByDefinition(
            DefinitionId definitionId
    ) {
        if (definitionId == null) {
            return List.of();
        }

        /*
         * Resolve the public namespaced ID back to the
         * legacy ID used by BuildingManager/Building.
         */
        String legacyId =
                DefinitionResolver.resolveBuilding(
                        definitionId
                );

        if (legacyId == null) {
            return List.of();
        }

        List<BuildingView> result =
                new ArrayList<>();

        for (Building building
                : World.getBuildings()) {

            if (!legacyId.equals(
                    building.getIniHeader()
            )) {
                continue;
            }

            BuildingView view =
                    map(building);

            if (view != null) {
                result.add(view);
            }
        }

        return List.copyOf(result);
    }

    static BuildingView map(
            Building building
    ) {
        if (building == null) {
            return null;
        }

        BuildingManagerItem definition =
                BuildingManager.getItem(
                        building.getIniHeader()
                );

        if (definition == null) {
            return null;
        }

        /*
         * Reconstruct the correct public namespaced
         * definition ID from ownership metadata.
         */
        DefinitionId definitionId =
                DefinitionOwnershipRegistry.idFor(
                        DefinitionKind.BUILDING,
                        building.getIniHeader()
                );

        WorldPosition origin =
                new WorldPosition(
                        building.getX(),
                        building.getY(),
                        building.getZ()
                );

        List<WorldPosition> occupied =
                new ArrayList<>();

        for (int x = building.getX();
                x < building.getX()
                        + definition.getWidth();
                x++) {

            for (int y = building.getY();
                    y < building.getY()
                            + definition.getHeight();
                    y++) {

                int localX =
                        x - building.getX();

                int localY =
                        y - building.getY();

                int groundIndex =
                        localY
                                * definition.getWidth()
                                + localX;

                char ground =
                        definition
                                .getGroundData()
                                .charAt(
                                        groundIndex
                                );

                /*
                 * X means this cell lies inside the
                 * rectangular definition but is not
                 * actually occupied by the building.
                 */
                if (ground
                        == Building.GROUND_NON_BUILDING) {

                    continue;
                }

                occupied.add(
                        new WorldPosition(
                                x,
                                y,
                                building.getZ()
                        )
                );
            }
        }

        return new BuildingView(
                new EntityId(
                        building.getID()
                ),
                definitionId,
                origin,
                occupied,
                building.isOperative()
        );
    }
}