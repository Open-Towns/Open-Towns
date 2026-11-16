package xaos.api;

import xaos.api.world.WorldApi;

import xaos.api.entity.EntityApi;
import xaos.api.ui.UiApi;
import xaos.api.tasks.TaskApi;

import xaos.api.selection.SelectionApi;

import xaos.api.events.EventApi;

import xaos.api.inventory.InventoryApi;

import xaos.api.stockpiles.StockpileApi;

import xaos.api.items.ItemBehaviourApi;
import xaos.api.items.ItemQueryApi;

import xaos.api.buildings.BuildingApi;

import xaos.api.fluids.FluidApi;

import xaos.api.production.ProductionApi;

import xaos.api.actions.ActionApi;

import xaos.api.buildings.BuildingBehaviourApi;
import xaos.api.definitions.DefinitionsApi;

/**
 * Main entry point for the public Open Towns API.
 *
 * Mods and external tools should access game systems through this
 * class rather than depending directly on internal engine classes.
 */
public final class GameApi {

   

    private final DefinitionsApi definitions;
    private final WorldApi world;
    private final EntityApi entities;
    private final UiApi ui;
    private final TaskApi tasks;
    private final SelectionApi selection;
    private final EventApi events;
    private final InventoryApi inventory;
    private final StockpileApi stockpiles;
    private final ItemQueryApi items;
    private final ItemBehaviourApi itemBehaviours;
    private final BuildingApi buildings;
    private final BuildingBehaviourApi buildingBehaviours;
    private final FluidApi fluids;
    private final ProductionApi production;
    private final ActionApi actions;

    GameApi(
            DefinitionsApi definitions,
            WorldApi world,
            EntityApi entities,
            UiApi ui,
            TaskApi tasks,
            SelectionApi selection,
            EventApi events,
            InventoryApi inventory,
            StockpileApi stockpiles,
            ItemQueryApi items,
            ItemBehaviourApi itemBehaviours,
            BuildingApi buildings,
            BuildingBehaviourApi buildingBehaviours,
            FluidApi fluids,
            ProductionApi production,
            ActionApi actions) {
        this.definitions = definitions;
        this.world = world;
        this.entities = entities;
        this.ui = ui;
        this.tasks = tasks;
        this.selection = selection;
        this.events = events;
        this.inventory = inventory;
        this.stockpiles = stockpiles;
        this.items = items;
        this.itemBehaviours = itemBehaviours;
        this.buildings = buildings;
        this.buildingBehaviours = buildingBehaviours;
        this.fluids = fluids;
        this.production = production;
        this.actions = actions;
    }


    /**
     * Access to game content definitions such as items,
     * buildings, living entities, actions and events.
     */
    public DefinitionsApi definitions() {
        return definitions;
    }

    /**
     * Access to the current game world.
     */
    public WorldApi world() {
        return world;
    }

    /**
     * Access to runtime entities in the current world.
     */
    public EntityApi entities() {
        return entities;
    }

    /**
     * Access to public user-interface functionality.
     */
    public UiApi ui() {
        return ui;
    }

    public TaskApi tasks() {
        return tasks;
    }

    public SelectionApi selection() {
        return selection;
    }

    public EventApi events() {
        return events;
    }

    public InventoryApi inventory() {
        return inventory;
    }

    public StockpileApi stockpiles() {
        return stockpiles;
    }

    public ItemQueryApi items() {
        return items;
    }

    public ItemBehaviourApi itemBehaviours() {
        return itemBehaviours;
    }

    public BuildingApi buildings() {
        return buildings;
    }

    /**
     * Registers periodic behaviour for runtime building instances.
     */
    public BuildingBehaviourApi buildingBehaviours() {
        return buildingBehaviours;
    }

    public FluidApi fluids() {
        return fluids;
    }

    public ProductionApi production() {
        return production;
    }

    public ActionApi actions() {
        return actions;
    }
}