package xaos.internal.api.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import xaos.api.ui.UiActionRegistration;

public final class UiActionRegistry {

    private static final Map<String, Registration> actions =
            new LinkedHashMap<>();

    private UiActionRegistry() {
    }

    public static synchronized UiActionRegistration register(
            String id,
            String label,
            Runnable action
    ) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Action ID cannot be empty"
            );
        }

        if (!id.contains(":")) {
            throw new IllegalArgumentException(
                    "Action ID must be namespaced, e.g. strip-mine:start"
            );
        }

        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException(
                    "Action label cannot be empty"
            );
        }

        if (action == null) {
            throw new IllegalArgumentException(
                    "Action callback cannot be null"
            );
        }

        Registration registration =
                new Registration(
                        id,
                        label,
                        action
                );

        /*
         * Preserve the existing behaviour where registering the
         * same ID replaces the previous action.
         *
         * Mark the old registration inactive so its cleanup
         * handle cannot later remove this newer registration.
         */
        Registration previous =
                actions.put(
                        id,
                        registration
                );

        if (previous != null) {
            previous.deactivate();
        }

        return registration;
    }

    public static synchronized void unregister(
            String id
    ) {
        if (id == null) {
            return;
        }

        Registration registration =
                actions.remove(id);

        if (registration != null) {
            registration.deactivate();
        }
    }

    public static synchronized List<RegisteredAction> getActions() {

        return actions.values()
                .stream()
                .map(registration ->
                        new RegisteredAction(
                                registration.id,
                                registration.label,
                                registration.callback
                        ))
                .toList();
    }

    public static synchronized boolean hasActions() {
        return !actions.isEmpty();
    }

    public static boolean execute(
            String id
    ) {
        Runnable callback;

        synchronized (UiActionRegistry.class) {

            Registration registration =
                    actions.get(id);

            if (registration == null
                    || !registration.active) {
                return false;
            }

            callback =
                    registration.callback;
        }

        /*
         * Run mod code outside the registry lock.
         */
        callback.run();

        return true;
    }

    private static synchronized void remove(
            Registration registration
    ) {
        /*
         * Only remove the action if this registration is still
         * the current owner of the ID.
         *
         * This prevents an old cleanup handle from removing a
         * newer registration using the same ID.
         */
        if (actions.get(registration.id)
                == registration) {

            actions.remove(
                    registration.id
            );
        }
    }

    private static final class Registration
            implements UiActionRegistration {

        private final String id;
        private final String label;
        private final Runnable callback;

        private boolean active = true;

        private Registration(
                String id,
                String label,
                Runnable callback
        ) {
            this.id = id;
            this.label = label;
            this.callback = callback;
        }

        @Override
        public void unregister() {

            synchronized (UiActionRegistry.class) {

                if (!active) {
                    return;
                }

                active = false;

                UiActionRegistry.remove(
                        this
                );
            }
        }

        private void deactivate() {
            active = false;
        }

        @Override
        public boolean isActive() {
            return active;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public String label() {
            return label;
        }
    }

    public record RegisteredAction(
            String id,
            String label,
            Runnable callback
    ) {
    }
}