package xaos.internal.api.buildings;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import xaos.api.buildings.BuildingBehaviour;
import xaos.api.buildings.BuildingBehaviourRegistration;
import xaos.api.buildings.BuildingView;
import xaos.api.definitions.DefinitionId;
import xaos.api.events.SimulationTickEvent;

import xaos.internal.api.definitions.DefinitionKind;
import xaos.internal.api.definitions.DefinitionOwnershipRegistry;
import xaos.internal.api.definitions.DefinitionResolver;
import xaos.internal.api.events.EventBus;

import xaos.main.World;
import xaos.tiles.entities.buildings.Building;
import xaos.tiles.entities.buildings.BuildingManager;
import xaos.utils.Log;

/**
 * Central dispatcher for custom mod building behaviour.
 *
 * The game world is scanned once per simulation tick when
 * at least one behaviour is due. Matching building
 * definitions are then dispatched to their registered
 * handlers.
 */
final class BuildingBehaviourRegistry {

    private static final Map<
            DefinitionId,
            List<Registration>> registrations =
                    new LinkedHashMap<>();

    private static boolean initialized;

    private static long simulationTick;

    private BuildingBehaviourRegistry() {
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

    static synchronized BuildingBehaviourRegistration register(
            DefinitionId definitionId,
            int intervalTicks,
            BuildingBehaviour behaviour
    ) {
        if (definitionId == null) {
            throw new IllegalArgumentException(
                    "Building definition ID cannot be null"
            );
        }

        if (intervalTicks <= 0) {
            throw new IllegalArgumentException(
                    "Building behaviour interval must be greater than zero"
            );
        }

        if (behaviour == null) {
            throw new IllegalArgumentException(
                    "Building behaviour cannot be null"
            );
        }

        /*
         * Ensure the building definitions have been loaded
         * before resolving ownership.
         */
        BuildingManager.getAllItems();

        String legacyId =
                DefinitionResolver.resolveBuilding(
                        definitionId
                );

        if (legacyId == null
                || BuildingManager.getItem(
                        legacyId
                ) == null) {

            throw new IllegalArgumentException(
                    "Unknown building definition: "
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

        synchronized (BuildingBehaviourRegistry.class) {

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

        /*
         * No behaviour is due this tick, so don't scan
         * the world at all.
         */
        if (due.isEmpty()) {
            return;
        }

        /*
         * Scan runtime buildings once.
         */
        for (Building building
                : World.getBuildings()) {

            if (building == null
                    || !building.isOperative()) {

                continue;
            }

            DefinitionId definitionId =
                    DefinitionOwnershipRegistry.idFor(
                            DefinitionKind.BUILDING,
                            building.getIniHeader()
                    );

            List<Registration> handlers =
                    due.get(
                            definitionId
                    );

            if (handlers == null
                    || handlers.isEmpty()) {

                continue;
            }

            BuildingView view =
                    DefaultBuildingApi.map(
                            building
                    );

            if (view == null) {
                continue;
            }

            for (Registration registration
                    : handlers) {

                registration.dispatch(
                        view
                );
            }
        }
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
            implements BuildingBehaviourRegistration {

        private final DefinitionId definitionId;
        private final int intervalTicks;
        private final BuildingBehaviour behaviour;

        private boolean active = true;

        private Registration(
                DefinitionId definitionId,
                int intervalTicks,
                BuildingBehaviour behaviour
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

            BuildingBehaviourRegistry.remove(
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
                BuildingView building
        ) {
            if (!active) {
                return;
            }

            try {

                behaviour.onTick(
                        building
                );

            } catch (RuntimeException exception) {

                /*
                 * A broken mod behaviour must not stop
                 * the world simulation.
                 */
                Log.log(
                        Log.LEVEL_ERROR,
                        "Error in building behaviour for "
                                + definitionId
                                + ": "
                                + exception,
                        "BuildingBehaviourRegistry"
                );
            }
        }
    }
}