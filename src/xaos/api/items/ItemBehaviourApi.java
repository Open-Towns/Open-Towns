package xaos.api.items;

import xaos.api.definitions.DefinitionId;

/**
 * API for attaching periodic behaviour to placed item definitions.
 */
public interface ItemBehaviourApi {

    /**
     * Registers behaviour that runs every simulation tick.
     */
    default ItemBehaviourRegistration register(
            DefinitionId definitionId,
            ItemBehaviour behaviour
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
    ItemBehaviourRegistration register(
            DefinitionId definitionId,
            int intervalTicks,
            ItemBehaviour behaviour
    );
}
