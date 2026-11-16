package xaos.api.definitions;

import java.util.List;

/**
 * Runtime bridge used by the Open Towns engine to construct
 * public read-only definition snapshots.
 *
 * Not part of the public modding SDK.
 */
public final class DefinitionSnapshotFactory {

    private DefinitionSnapshotFactory() {
    }

    public static ItemDefinition item(
            DefinitionId id,
            String name,
            String type,
            int level,
            String building,
            int buildingTime,
            int value,
            List<String> tags,
            List<String> actions,
            boolean wall,
            boolean door,
            boolean container,
            boolean stackable,
            boolean militaryItem
    ) {
        return new ItemDefinition(
                id,
                name,
                type,
                level,
                building,
                buildingTime,
                value,
                tags,
                actions,
                wall,
                door,
                container,
                stackable,
                militaryItem
        );
    }

    public static LivingDefinition living(
            DefinitionId id,
            String name,
            int type,
            int level,
            String building,
            int buildingTime,
            boolean female,
            List<String> actions,
            List<String> effects
    ) {
        return new LivingDefinition(
                id,
                name,
                type,
                level,
                building,
                buildingTime,
                female,
                actions,
                effects
        );
    }

    public static BuildingDefinition building(
            DefinitionId id,
            String name,
            String type,
            short width,
            short height,
            boolean automatic,
            List<String> descriptions
    ) {
        return new BuildingDefinition(
                id,
                name,
                type,
                width,
                height,
                automatic,
                descriptions
        );
    }

    public static ActionDefinition action(
            DefinitionId id,
            String name,
            String priorityId,
            int turns,
            String effect,
            String generatedItem,
            boolean killsSource,
            boolean inverted
    ) {
        return new ActionDefinition(
                id,
                name,
                priorityId,
                turns,
                effect,
                generatedItem,
                killsSource,
                inverted
        );
    }

    public static EventDefinition event(
            DefinitionId id,
            String name,
            int order,
            int minPopulation,
            boolean spawnAtRandom,
            String turns,
            String eventCooldown,
            boolean siege,
            List<String> prerequisites,
            List<String> afterEvents,
            List<String> effects,
            List<String> items
    ) {
        return new EventDefinition(
                id,
                name,
                order,
                minPopulation,
                spawnAtRandom,
                turns,
                eventCooldown,
                siege,
                prerequisites,
                afterEvents,
                effects,
                items
        );
    }

    public static TerrainDefinition terrain(
            DefinitionId id,
            String name,
            int terrainId,
            int mineTurns,
            String drop,
            int dropPercent,
            String ladderItem,
            boolean canBeFilled,
            String group,
            boolean blocky,
            List<String> actions
    ) {
        return new TerrainDefinition(
                id,
                name,
                terrainId,
                mineTurns,
                drop,
                dropPercent,
                ladderItem,
                canBeFilled,
                group,
                blocky,
                actions
        );
    }
}