package xaos.api.buildings;

import xaos.api.definitions.DefinitionId;

/**
 * API for attaching periodic behaviour to building definitions.
 */
public interface BuildingBehaviourApi {

    /**
     * Registers behaviour that runs every simulation tick.
     */
    default BuildingBehaviourRegistration register(
            DefinitionId definitionId,
            BuildingBehaviour behaviour
    ) {
        return register(
                definitionId,
                1,
                behaviour
        );
    }

    /**
     * Registers behaviour that runs every intervalTicks
     * completed simulation turns.
     */
    BuildingBehaviourRegistration register(
            DefinitionId definitionId,
            int intervalTicks,
            BuildingBehaviour behaviour
    );
}