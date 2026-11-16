package xaos.api.buildings;

import xaos.api.definitions.DefinitionId;
import xaos.api.mods.ModResource;

public interface BuildingBehaviourRegistration
        extends ModResource {

    void unregister();

    boolean isActive();

    DefinitionId definitionId();

    int intervalTicks();

    @Override
    default void close() {
        unregister();
    }
}
