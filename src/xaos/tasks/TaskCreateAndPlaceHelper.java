package xaos.tasks;

import java.util.ArrayList;

import xaos.actions.Action;
import xaos.actions.ActionManager;
import xaos.actions.QueueData;
import xaos.main.Game;
import xaos.main.World;
import xaos.panels.MainPanel;
import xaos.panels.MessagesPanel;
import xaos.tiles.entities.buildings.Building;
import xaos.tiles.entities.buildings.BuildingManager;
import xaos.tiles.entities.buildings.BuildingManagerItem;
import xaos.tiles.entities.items.Item;
import xaos.tiles.entities.items.ItemManager;
import xaos.tiles.entities.items.ItemManagerItem;
import xaos.tiles.entities.living.Citizen;
import xaos.utils.ColorGL;
import xaos.utils.Log;
import xaos.utils.Messages;
import xaos.utils.Point3D;
import xaos.utils.Point3DShort;
import xaos.utils.Utils;
import xaos.utils.UtilsIniHeaders;

final class TaskCreateAndPlaceHelper {

    private final Task owner;

    TaskCreateAndPlaceHelper(Task owner) {
        this.owner = owner;
    }

    void createAndPlaceItem(TaskArea area) {
        ItemManagerItem itemDefinition = getItemDefinitionForCreateAndPlace();

        if (itemDefinition == null) {
            Log.log(
                    Log.LEVEL_ERROR,
                    Messages.getString("Task.30") + owner.getParameter() + "]", //$NON-NLS-1$ //$NON-NLS-2$
                    owner.getClass().toString());
            return;
        }

        if (!isCellAvailableForCreateAndPlace(itemDefinition, area)) {
            return;
        }

        boolean foundExistingItem = tryCreateMoveTaskForExistingItem(itemDefinition, area);

        if (foundExistingItem) {
            return;
        }

        if (owner.getTask() == TaskTypes.TASK_QUEUE_AND_PLACE) {
            createQueueAndPlaceAction(area);
            return;
        }

        addItemToNearestBuildingQueue(itemDefinition, area);
    }

    private ItemManagerItem getItemDefinitionForCreateAndPlace() {
        if (owner.getTask() == TaskTypes.TASK_QUEUE_AND_PLACE) {
            return ItemManager.getItem(owner.getParameter2());
        }

        return ItemManager.getItem(owner.getParameter());
    }

    private boolean isCellAvailableForCreateAndPlace(ItemManagerItem itemDefinition, TaskArea area) {
        if (!Item.isCellAvailableForItem(itemDefinition, area.xStart, area.yStart, area.z, true, true)) {
            return false;
        }

        if (itemDefinition.canBeBuiltOnHoles()) {
            return hasNearbyAvailableAstarZone(area.xStart, area.yStart, area.z);
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

    private boolean tryCreateMoveTaskForExistingItem(ItemManagerItem itemDefinition, TaskArea area) {
        int numberOfItems = Item.getNumItems(
                UtilsIniHeaders.getIntIniHeader(itemDefinition.getIniHeader()),
                false,
                Game.getWorld().getRestrictHaulEquippingLevel());

        if (numberOfItems <= 0) {
            return false;
        }

        ArrayList<Integer> itemIds = Item.getMapItems().get(itemDefinition.getNumericalIniHeader());

        if (itemIds == null) {
            return false;
        }

        for (int itemIndex = 0; itemIndex < itemIds.size(); itemIndex++) {
            Item item = Item.getItemByID(itemIds.get(itemIndex), true);

            if (!canUseExistingItem(item, itemDefinition, area)) {
                continue;
            }

            if (createMoveAndLockTaskForExistingItem(item, itemDefinition, area)) {
                return true;
            }
        }

        return false;
    }

    private boolean canUseExistingItem(Item item, ItemManagerItem itemDefinition, TaskArea area) {
        if (item == null || item.getNumericIniHeader() != itemDefinition.getNumericalIniHeader()) {
            return false;
        }

        if (item.isLocked() || isItemInUseByTask(item)) {
            return false;
        }

        int itemAstarZoneId = World.getCell(item.getCoordinates()).getAstarZoneID();
        int destinationAstarZoneId = World.getCell(area.xStart, area.yStart, area.z).getAstarZoneID();

        return itemAstarZoneId != -1
                && (itemAstarZoneId == destinationAstarZoneId || destinationAstarZoneId == -1);
    }

    private boolean createMoveAndLockTaskForExistingItem(Item item, ItemManagerItem itemDefinition, TaskArea area) {
        if (itemDefinition.canBeBuiltOnHoles()) {
            int itemAstarZoneId = World.getCell(item.getCoordinates()).getAstarZoneID();

            ArrayList<Point3DShort> accessPoints = Task.getAccessingPointsMatchingAstarZone(
                    area.xStart,
                    area.yStart,
                    area.z,
                    itemAstarZoneId,
                    owner.getTask());

            if (accessPoints.size() == 0) {
                return false;
            }

            createMoveAndLockTask(item, accessPoints, area);
            return true;
        }

        createMoveAndLockTask(item, item.getCoordinates(), area);
        return true;
    }

    private void createMoveAndLockTask(Item item, Point3DShort accessPoint, TaskArea area) {
        Task moveTask = new Task(TaskTypes.TASK_MOVE_AND_LOCK);
        moveTask.setPointIni(item.getCoordinates().toPoint3D());
        moveTask.setPointEnd(new Point3D(area.xStart, area.yStart, area.z));

        World.getCell(area.xStart, area.yStart, area.z).setFlagOrders(true);

        moveTask.setParameter(item.getIniHeader());
        moveTask.setFace(MainPanel.itemBuildFace);
        moveTask.addHotPoint(new HotPoint(item.getCoordinates(), accessPoint));

        Game.getWorld().getTaskManager().addTask(moveTask);
    }

    private void createMoveAndLockTask(Item item, ArrayList<Point3DShort> accessPoints, TaskArea area) {
        Task moveTask = new Task(TaskTypes.TASK_MOVE_AND_LOCK);
        moveTask.setPointIni(item.getCoordinates().toPoint3D());
        moveTask.setPointEnd(new Point3D(area.xStart, area.yStart, area.z));

        World.getCell(area.xStart, area.yStart, area.z).setFlagOrders(true);

        moveTask.setParameter(item.getIniHeader());
        moveTask.setFace(MainPanel.itemBuildFace);
        moveTask.addHotPoint(new HotPoint(item.getCoordinates(), accessPoints));

        Game.getWorld().getTaskManager().addTask(moveTask);
    }

    private void createQueueAndPlaceAction(TaskArea area) {
        Action action = new Action(owner.getParameter());
        action.setDestinationPoint(Point3DShort.getPoolInstance(area.xStart, area.yStart, area.z));
        action.setQueue(ActionManager.getItem(owner.getParameter()).getQueue());
        action.setQueueData(new QueueData());
        action.setFace(MainPanel.itemBuildFace);

        Game.getWorld().getTaskManager().addCustomAction(action, true, false);
    }

    private void addItemToNearestBuildingQueue(ItemManagerItem itemDefinition, TaskArea area) {
        ArrayList<Building> buildings = World.getBuildings();

        int minimumQueueSize = getMinimumQueueSizeForBuilding(itemDefinition, buildings);
        int nearestBuildingIndex = findNearestBuildingIndex(itemDefinition, buildings, minimumQueueSize, area);

        if (nearestBuildingIndex != -1) {
            addItemToBuildingQueue(itemDefinition, buildings.get(nearestBuildingIndex), area);
            return;
        }

        showMissingBuildingMessage(itemDefinition);
    }

    private int getMinimumQueueSizeForBuilding(ItemManagerItem itemDefinition, ArrayList<Building> buildings) {
        int minimumQueueSize = 1000;

        for (int i = 0; i < buildings.size(); i++) {
            Building building = buildings.get(i);

            if (building.isOperative() && building.getIniHeader().equals(itemDefinition.getBuilding())) {
                if (building.getItemQueue().size() < minimumQueueSize) {
                    minimumQueueSize = building.getItemQueue().size();
                }
            }
        }

        return minimumQueueSize;
    }

    private int findNearestBuildingIndex(
            ItemManagerItem itemDefinition,
            ArrayList<Building> buildings,
            int minimumQueueSize,
            TaskArea area) {

        int nearestBuildingIndex = -1;
        int nearestEmptyQueueBuildingIndex = -1;
        int nearestDistance = Utils.MAX_DISTANCE;
        int nearestEmptyQueueDistance = Utils.MAX_DISTANCE;

        for (int i = 0; i < buildings.size(); i++) {
            Building building = buildings.get(i);

            if (!isMatchingBuildingForItem(building, itemDefinition, minimumQueueSize)) {
                continue;
            }

            BuildingManagerItem buildingDefinition = BuildingManager.getItem(building.getIniHeader());
            Point3DShort buildingEntrance = buildingDefinition.getEntranceBaseCoordinates()
                    .merge(building.getCoordinates());

            if (!canBuildingReachDestination(itemDefinition, buildingEntrance, area)) {
                continue;
            }

            int distance = Utils.getDistance(area.xStart, area.yStart, area.z, buildingEntrance);

            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestBuildingIndex = i;
            }

            if (!building.hasItemsInQueue() && distance < nearestEmptyQueueDistance) {
                nearestEmptyQueueDistance = distance;
                nearestEmptyQueueBuildingIndex = i;
            }
        }

        if (nearestEmptyQueueBuildingIndex != -1) {
            return nearestEmptyQueueBuildingIndex;
        }

        return nearestBuildingIndex;
    }

    private boolean isMatchingBuildingForItem(Building building, ItemManagerItem itemDefinition, int minimumQueueSize) {
        return building.isOperative()
                && building.getIniHeader().equals(itemDefinition.getBuilding())
                && building.getItemQueue().size() == minimumQueueSize;
    }

    private boolean canBuildingReachDestination(
            ItemManagerItem itemDefinition,
            Point3DShort buildingEntrance,
            TaskArea area) {

        if (itemDefinition.canBeBuiltOnHoles()) {
            int buildingAstarZoneId = World.getCell(buildingEntrance).getAstarZoneID();

            ArrayList<Point3DShort> accessPoints = Task.getAccessingPointsMatchingAstarZone(
                    area.xStart,
                    area.yStart,
                    area.z,
                    buildingAstarZoneId,
                    owner.getTask());

            return accessPoints.size() > 0;
        }

        return World.getCell(buildingEntrance).getAstarZoneID()
                == World.getCell(area.xStart, area.yStart, area.z).getAstarZoneID();
    }

    private void addItemToBuildingQueue(ItemManagerItem itemDefinition, Building building, TaskArea area) {
        Item item = Item.createItem(itemDefinition);
        item.setCoordinates(area.xStart, area.yStart, area.z);

        World.getCell(area.xStart, area.yStart, area.z).setFlagOrders(true);

        item.setPrerequisites(ItemManager.getItem(owner.getParameter()).getPrerequisites());

        building.addItem(item);
    }

    private void showMissingBuildingMessage(ItemManagerItem itemDefinition) {
        if (itemDefinition.getBuilding() != null) {
            MessagesPanel.addMessage(
                    MessagesPanel.TYPE_ANNOUNCEMENT,
                    Messages.getString("Task.21") + itemDefinition.getName() //$NON-NLS-1$
                            + Messages.getString("Task.22") //$NON-NLS-1$
                            + BuildingManager.getItem(itemDefinition.getBuilding()).getName() + "]", //$NON-NLS-1$
                    ColorGL.ORANGE);
            return;
        }

        MessagesPanel.addMessage(
                MessagesPanel.TYPE_ANNOUNCEMENT,
                Messages.getString("Task.19") + itemDefinition.getName() + Messages.getString("Task.32"), //$NON-NLS-1$ //$NON-NLS-2$
                ColorGL.ORANGE);
    }

    /**
     * Indicates whether the given item is already reserved by another task.
     *
     * @param item item to check
     * @return true if another citizen or task is already going to pick up this item
     */
    private boolean isItemInUseByTask(Item item) {
        if (isItemInUseByLivingList(item, World.getCitizenIDs())) {
            return true;
        }

        if (isItemInUseByLivingList(item, World.getSoldierIDs())) {
            return true;
        }

        if (isItemInUseByTaskList(item, Game.getWorld().getTaskManager().getTaskItems())) {
            return true;
        }

        return isItemInUseByTaskList(item, Game.getWorld().getTaskManager().getTaskItemsTemp());
    }

    private boolean isItemInUseByLivingList(Item item, ArrayList<Integer> livingIds) {
        for (int i = 0; i < livingIds.size(); i++) {
            Citizen citizen = (Citizen) World.getLivingEntityByID(livingIds.get(i));

            if (citizen == null || citizen.getCurrentTask() == null) {
                continue;
            }

            if (isTaskUsingItem(citizen.getCurrentTask(), item)) {
                return true;
            }
        }

        return false;
    }

    private boolean isItemInUseByTaskList(Item item, ArrayList<TaskManagerItem> taskItems) {
        for (int i = 0; i < taskItems.size(); i++) {
            Task task = taskItems.get(i).getTask();

            if (isTaskUsingItem(task, item)) {
                return true;
            }
        }

        return false;
    }

    private boolean isTaskUsingItem(Task task, Item item) {
        if (task == null || task.getPointIni() == null) {
            return false;
        }

        if (task.getTask() != TaskTypes.TASK_HAUL
                && task.getTask() != TaskTypes.TASK_MOVE_AND_LOCK
                && task.getTask() != TaskTypes.TASK_PUT_IN_CONTAINER) {
            return false;
        }

        return task.getPointIni().equals(item.getCoordinates());
    }
}