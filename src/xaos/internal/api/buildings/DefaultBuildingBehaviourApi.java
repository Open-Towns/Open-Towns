package xaos.internal.api.buildings;

import xaos.api.buildings.BuildingBehaviour;
import xaos.api.buildings.BuildingBehaviourApi;
import xaos.api.buildings.BuildingBehaviourRegistration;
import xaos.api.definitions.DefinitionId;

public final class DefaultBuildingBehaviourApi
        implements BuildingBehaviourApi {

    public DefaultBuildingBehaviourApi() {
        BuildingBehaviourRegistry.initialize();
    }

    @Override
    public BuildingBehaviourRegistration register(
            DefinitionId definitionId,
            int intervalTicks,
            BuildingBehaviour behaviour
    ) {
        return BuildingBehaviourRegistry.register(
                definitionId,
                intervalTicks,
                behaviour
        );
    }
}