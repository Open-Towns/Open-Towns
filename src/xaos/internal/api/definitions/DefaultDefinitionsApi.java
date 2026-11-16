package xaos.internal.api.definitions;

import xaos.actions.ActionManager;
import xaos.actions.ActionManagerItem;
import xaos.api.definitions.ActionDefinition;
import xaos.api.definitions.BuildingDefinition;
import xaos.api.definitions.DefinitionId;
import xaos.api.definitions.DefinitionRegistry;
import xaos.api.definitions.DefinitionsApi;
import xaos.api.definitions.EventDefinition;
import xaos.api.definitions.ItemDefinition;
import xaos.api.definitions.LivingDefinition;
import xaos.api.definitions.TerrainDefinition;
import xaos.events.EventManager;
import xaos.events.EventManagerItem;
import xaos.tiles.entities.buildings.BuildingManager;
import xaos.tiles.entities.buildings.BuildingManagerItem;
import xaos.tiles.entities.items.ItemManager;
import xaos.tiles.entities.items.ItemManagerItem;
import xaos.tiles.entities.living.LivingEntityManager;
import xaos.tiles.entities.living.LivingEntityManagerItem;

import xaos.tiles.terrain.TerrainManager;
import xaos.tiles.terrain.TerrainManagerItem;
import xaos.api.definitions.DefinitionSnapshotFactory;

/** Default bridge from Towns' existing XML managers to the public API. */
public final class DefaultDefinitionsApi implements DefinitionsApi {

        private final DefinitionRegistry<ItemDefinition> items = new MappedDefinitionRegistry<ItemManagerItem, ItemDefinition>(
                        DefinitionKind.ITEM,
                        ItemManager::getAllItems,
                        DefaultDefinitionsApi::mapItem);
        private final DefinitionRegistry<LivingDefinition> living = new MappedDefinitionRegistry<LivingEntityManagerItem, LivingDefinition>(
                        DefinitionKind.LIVING,
                        LivingEntityManager::getAllItems,
                        DefaultDefinitionsApi::mapLiving);

        private final DefinitionRegistry<BuildingDefinition> buildings = new MappedDefinitionRegistry<BuildingManagerItem, BuildingDefinition>(
                        DefinitionKind.BUILDING,
                        BuildingManager::getAllItems,
                        DefaultDefinitionsApi::mapBuilding);

        private final DefinitionRegistry<ActionDefinition> actions = new MappedDefinitionRegistry<ActionManagerItem, ActionDefinition>(
                        DefinitionKind.ACTION,
                        ActionManager::getAllItems,
                        DefaultDefinitionsApi::mapAction);

        private final DefinitionRegistry<EventDefinition> events = new MappedDefinitionRegistry<EventManagerItem, EventDefinition>(
                        DefinitionKind.EVENT,
                        EventManager::getAllItems,
                        DefaultDefinitionsApi::mapEvent);

        private final DefinitionRegistry<TerrainDefinition> terrain = new MappedDefinitionRegistry<TerrainManagerItem, TerrainDefinition>(
                        DefinitionKind.TERRAIN,
                        TerrainManager::getAllItems,
                        DefaultDefinitionsApi::mapTerrain);

        @Override
        public DefinitionRegistry<ItemDefinition> items() {
                return items;
        }

        @Override
        public DefinitionRegistry<LivingDefinition> living() {
                return living;
        }

        @Override
        public DefinitionRegistry<BuildingDefinition> buildings() {
                return buildings;
        }

        @Override
        public DefinitionRegistry<ActionDefinition> actions() {
                return actions;
        }

        @Override
        public DefinitionRegistry<EventDefinition> events() {
                return events;
        }

        @Override
        public DefinitionRegistry<TerrainDefinition> terrain() {
                return terrain;
        }

        private static ItemDefinition mapItem(
                        DefinitionId id,
                        ItemManagerItem item) {
                return DefinitionSnapshotFactory.item(
                                id,
                                item.getName(),
                                item.getType(),
                                item.getLevel(),
                                item.getBuilding(),
                                item.getBuildingTime(),
                                item.getValue(),
                                item.getTags(),
                                item.getActions(),
                                item.isWall(),
                                item.isDoor(),
                                item.isContainer(),
                                item.isStackable(),
                                item.isMilitaryItem());
        }

        private static LivingDefinition mapLiving(
                        DefinitionId id,
                        LivingEntityManagerItem living) {
                return DefinitionSnapshotFactory.living(
                                id,
                                living.getName(),
                                living.getType(),
                                living.getLevel(),
                                living.getBuilding(),
                                living.getBuildingTime(),
                                living.isFemale(),
                                living.getActions(),
                                living.getEffects());
        }

        private static BuildingDefinition mapBuilding(DefinitionId id, BuildingManagerItem building) {
                return DefinitionSnapshotFactory.building(
                                id,
                                building.getName(),
                                building.getType(),
                                building.getWidth(),
                                building.getHeight(),
                                building.isAutomatic(),
                                building.getDescriptions());
        }

        private static ActionDefinition mapAction(
                        DefinitionId id,
                        ActionManagerItem action) {
                return DefinitionSnapshotFactory.action(
                                id,
                                action.getName(),
                                action.getPriorityID(),
                                action.getTurns(),
                                action.getEffect(),
                                action.getGeneratedItem(),
                                action.isKillsSource(),
                                action.isInverted());
        }

        private static EventDefinition mapEvent(
                        DefinitionId id,
                        EventManagerItem event) {
                return DefinitionSnapshotFactory.event(
                                id,
                                event.getName(),
                                event.getOrder(),
                                event.getMinPopulation(),
                                event.isSpawnAtRandom(),
                                event.getTurns(),
                                event.getEventCooldown(),
                                event.isSiege(),
                                event.getPrerequisites(),
                                event.getAfterEvents(),
                                event.getEffects(),
                                event.getItems());
        }

        private static TerrainDefinition mapTerrain(
                        DefinitionId id,
                        TerrainManagerItem terrain) {
                return DefinitionSnapshotFactory.terrain(
                                id,
                                terrain.getName(),
                                terrain.getTerrainID(),
                                terrain.getMineTurns(),
                                terrain.getDrop(),
                                terrain.getDropPCT(),
                                terrain.getLadderItem(),
                                terrain.isCanBeFilled(),
                                terrain.getGroup(),
                                terrain.isBlocky(),
                                terrain.getActions());
        }
}
