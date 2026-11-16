package xaos.internal.api.events;

import xaos.api.events.EventApi;
import xaos.api.events.EventListener;
import xaos.api.events.EventSubscription;
import xaos.api.events.GameEvent;

public final class DefaultEventApi
        implements EventApi {

    @Override
    public <T extends GameEvent>
            EventSubscription subscribe(
                    Class<T> eventType,
                    EventListener<T> listener
            ) {

        return EventBus.subscribe(
                eventType,
                listener
        );
    }
}