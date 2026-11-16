package xaos.api.buildings;

import java.util.List;
import java.util.Optional;

import xaos.api.definitions.DefinitionId;
import xaos.api.entity.EntityId;
import xaos.api.world.WorldPosition;

public interface BuildingApi {

    BuildingView get(
            EntityId id
    );

    default Optional<BuildingView> find(
            EntityId id
    ) {
        return Optional.ofNullable(
                get(id)
        );
    }

    BuildingView at(
            WorldPosition position
    );

    List<BuildingView> all();

    List<BuildingView> findByDefinition(
            DefinitionId definitionId
    );
}