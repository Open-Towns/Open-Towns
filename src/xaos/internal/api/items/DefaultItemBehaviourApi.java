package xaos.internal.api.items;

import xaos.api.definitions.DefinitionId;
import xaos.api.items.ItemBehaviour;
import xaos.api.items.ItemBehaviourApi;
import xaos.api.items.ItemBehaviourRegistration;

public final class DefaultItemBehaviourApi
        implements ItemBehaviourApi {

    public DefaultItemBehaviourApi() {
        ItemBehaviourRegistry.initialize();
    }

    @Override
    public ItemBehaviourRegistration register(
            DefinitionId definitionId,
            int intervalTicks,
            ItemBehaviour behaviour
    ) {
        return ItemBehaviourRegistry.register(
                definitionId,
                intervalTicks,
                behaviour
        );
    }
}
