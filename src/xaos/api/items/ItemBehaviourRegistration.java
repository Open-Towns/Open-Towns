package xaos.api.items;

import xaos.api.definitions.DefinitionId;
import xaos.api.mods.ModResource;

public interface ItemBehaviourRegistration
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
