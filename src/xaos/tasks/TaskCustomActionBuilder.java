package xaos.tasks;

import java.util.ArrayList;
import java.util.StringTokenizer;

import xaos.actions.Action;
import xaos.actions.ActionManager;
import xaos.actions.ActionManagerItem;
import xaos.actions.QueueData;
import xaos.main.Game;
import xaos.main.World;
import xaos.panels.MainPanel;
import xaos.tiles.Cell;
import xaos.tiles.entities.Entity;
import xaos.tiles.entities.buildings.Building;
import xaos.tiles.entities.items.Item;
import xaos.tiles.entities.items.ItemManager;
import xaos.tiles.entities.items.ItemManagerItem;
import xaos.tiles.entities.living.LivingEntity;
import xaos.tiles.entities.living.LivingEntityManager;
import xaos.tiles.entities.living.LivingEntityManagerItem;
import xaos.tiles.terrain.TerrainManager;
import xaos.tiles.terrain.TerrainManagerItem;
import xaos.utils.Point3DShort;

final class TaskCustomActionBuilder {

    private final Task owner;

    TaskCustomActionBuilder(Task owner) {
        this.owner = owner;
    }

    ArrayList<ActionManagerItem> getCustomActionItems() {
        ArrayList<ActionManagerItem> actionItems = new ArrayList<ActionManagerItem>();

        if (owner.getTask() != TaskTypes.TASK_CUSTOM_ACTION) {
            return actionItems;
        }

        ArrayList<String> actionParameters = splitActionParameters(owner.getParameter());

        for (int i = 0; i < actionParameters.size(); i++) {
            actionItems.add(ActionManager.getItem(actionParameters.get(i)));
        }

        return actionItems;
    }

    ArrayList<String> getCustomActionParameters() {
        if (owner.getTask() != TaskTypes.TASK_CUSTOM_ACTION) {
            return new ArrayList<String>();
        }

        return splitActionParameters(owner.getParameter());
    }

    private ArrayList<String> splitActionParameters(String parameter) {
        ArrayList<String> actionParameters = new ArrayList<String>();

        if (parameter == null) {
            return actionParameters;
        }

        StringTokenizer tokenizer = new StringTokenizer(parameter, ","); //$NON-NLS-1$

        while (tokenizer.hasMoreTokens()) {
            actionParameters.add(tokenizer.nextToken().trim());
        }

        return actionParameters;
    }

    void addCustomActionsForCell(
            Cell cell,
            short x,
            short y,
            short z,
            ArrayList<ActionManagerItem> actionItems,
            ArrayList<String> actionParameters) {

        for (int actionIndex = 0; actionIndex < actionItems.size(); actionIndex++) {
            if (actionIndex >= actionParameters.size()) {
                continue;
            }

            ActionManagerItem actionItem = actionItems.get(actionIndex);
            String actionParameter = actionParameters.get(actionIndex);

            if (actionItem == null || actionParameter == null) {
                continue;
            }

            if (addCustomActionForItem(cell, actionItem, actionParameter)) {
                continue;
            }

            if (addCustomActionForLiving(cell, actionItem, actionParameter)) {
                continue;
            }

            addCustomActionForTerrain(cell, x, y, z, actionItem, actionParameter);
        }
    }

    private boolean addCustomActionForItem(
            Cell cell,
            ActionManagerItem actionItem,
            String actionParameter) {

        Entity entity = cell.getEntity();

        if (!(entity instanceof Item)) {
            return false;
        }

        ItemManagerItem itemDefinition = ItemManager.getItem(entity.getIniHeader());

        if (itemDefinition == null || !itemDefinition.getActions().contains(actionParameter)) {
            return false;
        }

        Action action = new Action(actionItem.getId());
        action.setEntityID(entity.getID());
        action.setQueue(actionItem.getQueue());
        action.setQueueData(new QueueData());
        action.setFace(MainPanel.itemBuildFace);

        Game.getWorld().getTaskManager().addCustomAction(action, true);
        return true;
    }

    private boolean addCustomActionForLiving(
            Cell cell,
            ActionManagerItem actionItem,
            String actionParameter) {

        ArrayList<LivingEntity> livingEntities = cell.getLivings();

        if (livingEntities == null) {
            return false;
        }

        boolean addedAction = false;

        for (int livingIndex = 0; livingIndex < livingEntities.size(); livingIndex++) {
            LivingEntity livingEntity = livingEntities.get(livingIndex);
            LivingEntityManagerItem livingDefinition = LivingEntityManager.getItem(livingEntity.getIniHeader());

            if (livingDefinition == null || !livingDefinition.getActions().contains(actionParameter)) {
                continue;
            }

            Action action = new Action(actionItem.getId());
            action.setEntityID(livingEntity.getID());
            action.setQueue(actionItem.getQueue());
            action.setQueueData(new QueueData());

            Game.getWorld().getTaskManager().addCustomAction(action, true);
            addedAction = true;
        }

        return addedAction;
    }

    private void addCustomActionForTerrain(
            Cell cell,
            short x,
            short y,
            short z,
            ActionManagerItem actionItem,
            String actionParameter) {

        if (!cell.isMined() || cell.getCoordinates().z >= World.MAP_DEPTH - 1) {
            return;
        }

        Cell cellUnder = World.getCell(x, y, z + 1);
        TerrainManagerItem terrainDefinition = TerrainManager.getItemByID(
                cellUnder.getTerrain().getTerrainID());

        if (terrainDefinition == null || !terrainDefinition.getActions().contains(actionParameter)) {
            return;
        }

        if (isCellBlockedForTerrainAction(cell)) {
            return;
        }

        Point3DShort terrainPoint = Point3DShort.getPoolInstance(x, y, z);

        Action action = new Action(actionItem.getId());
        action.setTerrainPoint(terrainPoint);
        action.setQueue(actionItem.getQueue());
        action.setQueueData(new QueueData());

        Game.getWorld().getTaskManager().addCustomAction(action, true);
    }

    private boolean isCellBlockedForTerrainAction(Cell cell) {
        if (cell.getTerrain().hasFluids() || cell.isFlagOrders()) {
            return true;
        }

        if (hasOperativeWall(cell)) {
            return true;
        }

        return hasBuilding(cell);
    }

    private boolean hasOperativeWall(Cell cell) {
        if (!cell.hasItem()) {
            return false;
        }

        Item item = (Item) cell.getEntity();

        if (item == null || !item.isOperative()) {
            return false;
        }

        ItemManagerItem itemDefinition = ItemManager.getItem(item.getIniHeader());

        return itemDefinition != null && itemDefinition.isWall();
    }

    private boolean hasBuilding(Cell cell) {
        if (!cell.hasBuilding()) {
            return false;
        }

        return Building.getBuilding(cell.getCoordinates()) != null;
    }
    // private ArrayList<ActionManagerItem> getCustomActionItems() {
    // ArrayList<ActionManagerItem> actionItems = new
    // ArrayList<ActionManagerItem>();

    // if (getTask() != TASK_CUSTOM_ACTION) {
    // return actionItems;
    // }

    // ArrayList<String> actionParameters = splitActionParameters(getParameter());

    // for (int i = 0; i < actionParameters.size(); i++) {
    // actionItems.add(ActionManager.getItem(actionParameters.get(i)));
    // }

    // return actionItems;
    // }

    // private ArrayList<String> getCustomActionParameters() {
    // if (getTask() != TASK_CUSTOM_ACTION) {
    // return new ArrayList<String>();
    // }

    // return splitActionParameters(getParameter());
    // }
}