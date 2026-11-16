package xaos.internal.api.items;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import xaos.api.definitions.DefinitionId;
import xaos.api.events.SimulationTickEvent;
import xaos.api.items.ItemBehaviour;
import xaos.api.items.ItemBehaviourContext;
import xaos.api.items.ItemBehaviourRegistration;
import xaos.api.items.ItemFacing;
import xaos.api.items.ItemView;
import xaos.api.world.WorldPosition;

import xaos.internal.api.definitions.DefinitionKind;
import xaos.internal.api.definitions.DefinitionOwnershipRegistry;
import xaos.internal.api.definitions.DefinitionResolver;
import xaos.internal.api.events.EventBus;

import xaos.main.World;
import xaos.tiles.entities.items.Item;
import xaos.tiles.entities.items.ItemManager;
import xaos.utils.Log;

/**
 * Central dispatcher for periodic behaviour attached to
 * item definitions.
 *
 * Only items currently placed in the world are considered.
 * Items held by citizens or stored inside containers are not
 * dispatched.
 */
final class ItemBehaviourRegistry {

    private static final Map<
            DefinitionId,
            List<Registration>> registrations =
                    new LinkedHashMap<>();

    private static boolean initialized;
    private static long simulationTick;

    private ItemBehaviourRegistry() {
    }

    static synchronized void initialize() {

        if (initialized) {
            return;
        }

        initialized = true;

        EventBus.subscribe(
                SimulationTickEvent.class,
                event -> dispatchTick()
        );
    }

    static synchronized ItemBehaviourRegistration register(
            DefinitionId definitionId,
            int intervalTicks,
            ItemBehaviour behaviour
    ) {
        if (definitionId == null) {
            throw new IllegalArgumentException(
                    "Item definition ID cannot be null"
            );
        }

        if (intervalTicks <= 0) {
            throw new IllegalArgumentException(
                    "Item behaviour interval must be greater than zero"
            );
        }

        if (behaviour == null) {
            throw new IllegalArgumentException(
                    "Item behaviour cannot be null"
            );
        }

        String legacyId =
                DefinitionResolver.resolveItem(
                        definitionId
                );

        if (legacyId == null
                || ItemManager.getItem(
                        legacyId
                ) == null) {

            throw new IllegalArgumentException(
                    "Unknown item definition: "
                            + definitionId
            );
        }

        initialize();

        Registration registration =
                new Registration(
                        definitionId,
                        intervalTicks,
                        behaviour
                );

        registrations
                .computeIfAbsent(
                        definitionId,
                        ignored ->
                                new ArrayList<>()
                )
                .add(
                        registration
                );

        return registration;
    }

    private static void dispatchTick() {

        Map<
                DefinitionId,
                List<Registration>> due;

        synchronized (ItemBehaviourRegistry.class) {

            simulationTick++;

            due =
                    new LinkedHashMap<>();

            for (Map.Entry<
                    DefinitionId,
                    List<Registration>> entry
                    : registrations.entrySet()) {

                List<Registration> dueForDefinition =
                        new ArrayList<>();

                for (Registration registration
                        : entry.getValue()) {

                    if (!registration.active) {
                        continue;
                    }

                    if (simulationTick
                            % registration.intervalTicks
                            != 0) {

                        continue;
                    }

                    dueForDefinition.add(
                            registration
                    );
                }

                if (!dueForDefinition.isEmpty()) {

                    due.put(
                            entry.getKey(),
                            dueForDefinition
                    );
                }
            }
        }

        if (due.isEmpty()) {
            return;
        }

        /*
         * Behaviours may eventually move/delete/create items.
         * Iterate over a stable snapshot so those mutations cannot
         * invalidate the collection we're currently traversing.
         */
        List<Item> worldItems =
                new ArrayList<>(
                        World.getItems()
                                .values()
                );

        for (Item item
                : worldItems) {

            if (item == null
                    || !item.isOperative()
                    || !item.isLocked()) {

                continue;
            }

            DefinitionId definitionId =
                    DefinitionOwnershipRegistry.idFor(
                            DefinitionKind.ITEM,
                            item.getIniHeader()
                    );

            List<Registration> handlers =
                    due.get(
                            definitionId
                    );

            if (handlers == null
                    || handlers.isEmpty()) {

                continue;
            }

            ItemView view =
                    map(
                            item,
                            definitionId
                    );

            for (Registration registration
                    : handlers) {

                registration.dispatch(
        new ItemBehaviourContext(
                view,
                simulationTick
        )
);
            }
        }
    }

    private static ItemView map(
            Item item,
            DefinitionId definitionId
    ) {
        return new ItemView(
                new xaos.api.entity.EntityId(
                        item.getID()
                ),
                definitionId,
                new WorldPosition(
                        item.getX(),
                        item.getY(),
                        item.getZ()
                ),
                mapFacing(
                        item.getFacing()
                ),
                item.isOperative(),
                item.isLocked()
        );
    }

    private static ItemFacing mapFacing(
            int facing
    ) {
        return switch (facing) {

            case Item.FACE_WEST ->
                    ItemFacing.WEST;

            case Item.FACE_NORTH ->
                    ItemFacing.NORTH;

            case Item.FACE_EAST ->
                    ItemFacing.EAST;

            case Item.FACE_SOUTH ->
                    ItemFacing.SOUTH;

            default ->
                    throw new IllegalStateException(
                            "Unknown item facing: "
                                    + facing
                    );
        };
    }

    private static synchronized void remove(
            Registration registration
    ) {
        List<Registration> definitions =
                registrations.get(
                        registration.definitionId
                );

        if (definitions == null) {
            return;
        }

        definitions.remove(
                registration
        );

        if (definitions.isEmpty()) {

            registrations.remove(
                    registration.definitionId
            );
        }
    }

    private static final class Registration
            implements ItemBehaviourRegistration {

        private final DefinitionId definitionId;
        private final int intervalTicks;
        private final ItemBehaviour behaviour;

        private boolean active = true;

        private Registration(
                DefinitionId definitionId,
                int intervalTicks,
                ItemBehaviour behaviour
        ) {
            this.definitionId =
                    definitionId;

            this.intervalTicks =
                    intervalTicks;

            this.behaviour =
                    behaviour;
        }

        @Override
        public void unregister() {

            if (!active) {
                return;
            }

            active = false;

            ItemBehaviourRegistry.remove(
                    this
            );
        }

        @Override
        public boolean isActive() {
            return active;
        }

        @Override
        public DefinitionId definitionId() {
            return definitionId;
        }

        @Override
        public int intervalTicks() {
            return intervalTicks;
        }

        private void dispatch(
                ItemBehaviourContext context
        ) {
            if (!active) {
                return;
            }

            try {

                behaviour.onTick(
                        context
                );

            } catch (RuntimeException exception) {

                /*
                 * A broken mod behaviour must not stop
                 * world simulation.
                 */
                Log.log(
                        Log.LEVEL_ERROR,
                        "Error in item behaviour for "
                                + definitionId
                                + ": "
                                + exception,
                        "ItemBehaviourRegistry"
                );
            }
        }
    }
}
