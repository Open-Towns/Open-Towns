package xaos.api.events;

import xaos.api.mods.ModResource;

public interface EventSubscription extends ModResource {

    void unsubscribe();

    boolean isActive();

    @Override
    default void close() {
        unsubscribe();
    }
}
