package xaos.internal.api.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xaos.api.events.EventListener;
import xaos.api.events.EventSubscription;
import xaos.api.events.GameEvent;
import xaos.utils.Log;

public final class EventBus {

    private static final Map<
            Class<?>,
            List<Subscription<?>>
    > listeners = new HashMap<>();

    private EventBus() {
    }

    public static synchronized <T extends GameEvent>
            EventSubscription subscribe(
                    Class<T> eventType,
                    EventListener<T> listener
            ) {

        if (eventType == null) {
            throw new IllegalArgumentException(
                    "Event type cannot be null"
            );
        }

        if (listener == null) {
            throw new IllegalArgumentException(
                    "Event listener cannot be null"
            );
        }

        Subscription<T> subscription =
                new Subscription<>(
                        eventType,
                        listener
                );

        listeners
                .computeIfAbsent(
                        eventType,
                        key -> new ArrayList<>()
                )
                .add(subscription);

        return subscription;
    }

    public static void publish(GameEvent event) {
        if (event == null) {
            return;
        }

        List<Subscription<?>> snapshot;

        synchronized (EventBus.class) {
            List<Subscription<?>> registered =
                    listeners.get(
                            event.getClass()
                    );

            if (registered == null
                    || registered.isEmpty()) {
                return;
            }

            snapshot =
                    new ArrayList<>(registered);
        }

        /*
         * Execute outside the synchronized block so a
         * callback can safely unsubscribe itself or
         * register another listener.
         */
        for (Subscription<?> subscription : snapshot) {
            subscription.dispatch(event);
        }
    }

    private static synchronized void remove(
            Subscription<?> subscription
    ) {
        List<Subscription<?>> registered =
                listeners.get(
                        subscription.eventType
                );

        if (registered == null) {
            return;
        }

        registered.remove(subscription);

        if (registered.isEmpty()) {
            listeners.remove(
                    subscription.eventType
            );
        }
    }

    private static final class Subscription<
            T extends GameEvent
    > implements EventSubscription {

        private final Class<T> eventType;
        private final EventListener<T> listener;

        private boolean active = true;

        private Subscription(
                Class<T> eventType,
                EventListener<T> listener
        ) {
            this.eventType = eventType;
            this.listener = listener;
        }

        @Override
        public void unsubscribe() {
            if (!active) {
                return;
            }

            active = false;

            EventBus.remove(this);
        }

        @Override
        public boolean isActive() {
            return active;
        }

        private void dispatch(GameEvent event) {
            if (!active
                    || !eventType.isInstance(event)) {
                return;
            }

            try {
                listener.onEvent(
                        eventType.cast(event)
                );

            } catch (RuntimeException exception) {

                Log.log(
                        Log.LEVEL_ERROR,
                        "Error in mod event listener for "
                                + eventType.getSimpleName()
                                + ": "
                                + exception,
                        "EventBus"
                );
            }
        }
    }
}