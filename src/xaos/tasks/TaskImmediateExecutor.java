package xaos.tasks;

import java.util.ArrayList;

import xaos.main.Game;
import xaos.main.World;
import xaos.tiles.Cell;
import xaos.tiles.entities.buildings.Building;
import xaos.tiles.entities.items.Item;
import xaos.tiles.entities.items.ItemManager;
import xaos.tiles.entities.items.ItemManagerItem;
import xaos.utils.Point3D;
import xaos.utils.Point3DShort;
import xaos.utils.Utils;

final class TaskImmediateExecutor {

    private final Task task;

    TaskImmediateExecutor(Task task) {
        this.task = task;
    }

    boolean canExecuteImmediately() {
        switch (task.getTask()) {
            case TaskTypes.TASK_MINE:
            case TaskTypes.TASK_MINE_LADDER:
            case TaskTypes.TASK_DIG:
            case TaskTypes.TASK_BUILD:
            case TaskTypes.TASK_TERRAIN_CHANGE:
            case TaskTypes.TASK_TERRAIN_ADD_FLUID:
            case TaskTypes.TASK_TERRAIN_REMOVE_FLUID:
            case TaskTypes.TASK_DESTROY_BUILDING:
            case TaskTypes.TASK_DESTROY_ENTITY:
            case TaskTypes.TASK_DELETE_STOCKPILE:
            case TaskTypes.TASK_DELETE_ZONE:
            case TaskTypes.TASK_LOCK:
            case TaskTypes.TASK_UNLOCK_OPEN:
            case TaskTypes.TASK_UNLOCK_CLOSE:
            case TaskTypes.TASK_TURN_OFF_NON_STOP:
            case TaskTypes.TASK_TURN_ON_NON_STOP:
            case TaskTypes.TASK_CREATE_AND_PLACE:
            case TaskTypes.TASK_QUEUE_AND_PLACE:
                return true;

            default:
                return false;
        }
    }

    boolean executeImmediately() {
        switch (task.getTask()) {
            case TaskTypes.TASK_MINE:
            case TaskTypes.TASK_MINE_LADDER:
            case TaskTypes.TASK_DIG:
                return executeMineHotPointsImmediately();

            case TaskTypes.TASK_BUILD:
                return executeBuildImmediately();

            case TaskTypes.TASK_CREATE_AND_PLACE:
            case TaskTypes.TASK_QUEUE_AND_PLACE:
                return executeCreateAndPlaceImmediately();

            case TaskTypes.TASK_TERRAIN_CHANGE:
            case TaskTypes.TASK_TERRAIN_ADD_FLUID:
            case TaskTypes.TASK_TERRAIN_REMOVE_FLUID:
            case TaskTypes.TASK_DESTROY_BUILDING:
            case TaskTypes.TASK_DESTROY_ENTITY:
            case TaskTypes.TASK_DELETE_STOCKPILE:
            case TaskTypes.TASK_DELETE_ZONE:
            case TaskTypes.TASK_LOCK:
            case TaskTypes.TASK_UNLOCK_OPEN:
            case TaskTypes.TASK_UNLOCK_CLOSE:
            case TaskTypes.TASK_TURN_OFF_NON_STOP:
            case TaskTypes.TASK_TURN_ON_NON_STOP:
                Game.getWorld().getTaskManager().executeTaskImmediately(task);
                return true;

            default:
                return false;
        }
    }

    boolean executeCreateAndPlaceImmediately() {
        if (!isImmediateCreateAndPlaceTask()) {
            return false;
        }

        Point3D placementPoint = task.getPointIni();

        if (placementPoint == null) {
            return false;
        }

        String itemHeader = getItemHeaderForImmediatePlacement();

        if (itemHeader == null) {
            return false;
        }

        short x = (short) placementPoint.x;
        short y = (short) placementPoint.y;
        short z = (short) placementPoint.z;

        ItemManagerItem itemDefinition = ItemManager.getItem(itemHeader);

        if (!canPlaceItemImmediately(itemDefinition, x, y, z)) {
            return false;
        }

        Item item = createAndPlaceItem(itemDefinition, x, y, z);

        if (item == null) {
            return false;
        }

        updateWorldAfterImmediatePlacement(item, itemDefinition, x, y, z);

        task.setFinished(true);
        return true;
    }

    boolean isImmediateCreateAndPlaceTask() {
        return task.getTask() == TaskTypes.TASK_CREATE_AND_PLACE
                || task.getTask() == TaskTypes.TASK_QUEUE_AND_PLACE;
    }

    private boolean executeBuildImmediately() {
        if (task.getTask() != TaskTypes.TASK_BUILD) {
            return false;
        }

        Building building = findBuildingToComplete();

        if (building == null) {
            return false;
        }

        completeBuildingImmediately(building);
        clearBuildHotPoints();

        task.setFinished(true);
        return true;
    }

    private boolean executeMineHotPointsImmediately() {
        boolean executedAny = false;
        ArrayList<HotPoint> hotPoints = task.getHotPoints();

        for (int i = 0; i < hotPoints.size(); i++) {
            HotPoint hotPoint = hotPoints.get(i);

            if (hotPoint == null) {
                continue;
            }

            Point3DShort point = hotPoint.getHotPoint();

            if (point == null) {
                continue;
            }

            if (executeMineAtImmediately(point)) {
                executedAny = true;
            }
        }

        if (executedAny) {
            task.setFinished(true);
        }

        return executedAny;
    }

    boolean executeMineAtImmediately(Point3DShort hotPoint3D) {
        if (hotPoint3D == null) {
            return false;
        }

        Cell cell = World.getCell(hotPoint3D);

        if (cell == null) {
            return false;
        }

        if (task.getTask() != TaskTypes.TASK_MINE
                && task.getTask() != TaskTypes.TASK_MINE_LADDER
                && task.getTask() != TaskTypes.TASK_DIG) {
            return false;
        }

        boolean mineLadder = task.getTask() == TaskTypes.TASK_MINE_LADDER;

        if (!cell.isMined()) {
            cell.getTerrain().mineImmediately(
                    hotPoint3D.x,
                    hotPoint3D.y,
                    hotPoint3D.z,
                    mineLadder);
        }

        boolean finished = cell.isMined();

        if (finished) {
            cell.setFlagOrders(false);
        }

        return finished;
    }

    boolean executeMineAt(Point3DShort hotPoint3D) {
        if (hotPoint3D == null) {
            return false;
        }

        Cell cell = World.getCell(hotPoint3D);

        if (cell == null) {
            return false;
        }

        if (task.getTask() != TaskTypes.TASK_MINE
                && task.getTask() != TaskTypes.TASK_MINE_LADDER
                && task.getTask() != TaskTypes.TASK_DIG) {
            return false;
        }

        boolean mineLadder = task.getTask() == TaskTypes.TASK_MINE_LADDER;

        if (!cell.isMined()) {
            cell.getTerrain().mine(
                    hotPoint3D.x,
                    hotPoint3D.y,
                    hotPoint3D.z,
                    mineLadder);
        }

        boolean finished = cell.isMined();

        if (finished) {
            cell.setFlagOrders(false);
        }

        return finished;
    }

    private String getItemHeaderForImmediatePlacement() {
        if (task.getTask() == TaskTypes.TASK_QUEUE_AND_PLACE) {
            return task.getParameter2();
        }

        return task.getParameter();
    }

    private boolean canPlaceItemImmediately(ItemManagerItem itemDefinition, short x, short y, short z) {
        if (itemDefinition == null) {
            return false;
        }

        if (!Item.isCellAvailableForItem(itemDefinition, x, y, z, true, true)) {
            return false;
        }

        if (itemDefinition.canBeBuiltOnHoles()) {
            return hasNearbyAvailableAstarZone(x, y, z);
        }

        return true;
    }

    private boolean hasNearbyAvailableAstarZone(short x, short y, short z) {
        for (short checkX = (short) (x - 1); checkX <= x + 1; checkX++) {
            for (short checkY = (short) (y - 1); checkY <= y + 1; checkY++) {
                for (short checkZ = (short) (z - 1); checkZ <= z + 1; checkZ++) {
                    if (isValidAstarZoneCell(checkX, checkY, checkZ)) {
                        return true;
                    }
                }
            }
        }

        return hasAvailableAstarZoneAboveOrBelow(x, y, z);
    }

    private boolean isValidAstarZoneCell(short x, short y, short z) {
        return Utils.isInsideMap(x, y, z)
                && World.getCell(x, y, z).getAstarZoneID() != -1;
    }

    private boolean hasAvailableAstarZoneAboveOrBelow(short x, short y, short z) {
        if (z > 0 && World.getCell(x, y, (short) (z - 1)).getAstarZoneID() != -1) {
            return true;
        }

        return z < World.MAP_DEPTH - 1
                && World.getCell(x, y, (short) (z + 1)).getAstarZoneID() != -1;
    }

    private Item createAndPlaceItem(ItemManagerItem itemDefinition, short x, short y, short z) {
        Item item = Item.createItem(itemDefinition);

        if (item == null) {
            return null;
        }

        item.setCoordinates(x, y, z);
        item.setOperative(true);

        // God Mode placement means this item is already placed in the world,
        // not a loose item waiting to be hauled.
        item.setLocked(true);

        World.getCell(x, y, z).setEntity(item);
        World.getCell(x, y, z).setFlagOrders(false);

        item.init(x, y, z);

        if (itemDefinition.isWall()) {
            item.setWallConnectorStatus(Item.FLAG_WALL_CONNECTOR_STATUS_UNLOCKED_AND_CLOSED, true);
        }

        return item;
    }

    private void updateWorldAfterImmediatePlacement(
            Item item,
            ItemManagerItem itemDefinition,
            short x,
            short y,
            short z) {

        if (itemDefinition.isWall()) {
            updateWallAfterImmediatePlacement(item, x, y, z);
        }

        refreshCellAfterImmediatePlacement(x, y, z);

        World.setRecheckASZID(true);
    }

    private void updateWallAfterImmediatePlacement(Item item, short x, short y, short z) {
        item.setWallConnectorStatus(Item.FLAG_WALL_CONNECTOR_STATUS_UNLOCKED_AND_CLOSED, true);
        refreshNeighbouringOpenCells(x, y, z);
    }

    private void refreshCellAfterImmediatePlacement(short x, short y, short z) {
        Cell.setShouldPaintUnders(World.getCells(), x, y, z);
        Cell.generateFullShadows(x, y, z);
        Cell.generateLightsItemRemovedCellMined(x, y, z, ItemManagerItem.MAX_LIGHT_RADIUS);
        Cell.generateOpen(World.getCells(), x, y);
    }

    private void refreshNeighbouringOpenCells(short x, short y, short z) {
        for (short checkX = (short) (x - 1); checkX <= x + 1; checkX++) {
            for (short checkY = (short) (y - 1); checkY <= y + 1; checkY++) {
                if (!Utils.isInsideMap(checkX, checkY, z)) {
                    continue;
                }

                Cell.generateOpen(World.getCells(), checkX, checkY);
            }
        }
    }

    private Building findBuildingToComplete() {
        Point3D initialPoint = task.getPointIni();

        if (initialPoint == null) {
            return null;
        }

        Building building = Building.getBuilding(initialPoint);

        if (building != null) {
            return building;
        }

        return findBuildingFromFirstHotPoint();
    }

    private Building findBuildingFromFirstHotPoint() {
        if (task.getHotPoints().isEmpty()) {
            return null;
        }

        HotPoint firstHotPoint = task.getHotPoint(0);

        if (firstHotPoint == null || firstHotPoint.getHotPoint() == null) {
            return null;
        }

        return Building.getBuilding(firstHotPoint.getHotPoint());
    }

    private void completeBuildingImmediately(Building building) {
        building.setOperative(true, true);
    }

    private void clearBuildHotPoints() {
        ArrayList<HotPoint> hotPoints = task.getHotPoints();

        for (int hotPointIndex = 0; hotPointIndex < hotPoints.size(); hotPointIndex++) {
            clearBuildHotPoint(hotPoints.get(hotPointIndex));
        }
    }

    private void clearBuildHotPoint(HotPoint hotPoint) {
        if (hotPoint == null || hotPoint.getHotPoint() == null) {
            return;
        }

        World.getCell(hotPoint.getHotPoint()).setFlagOrders(false);
        hotPoint.setFinished(true);
    }
}