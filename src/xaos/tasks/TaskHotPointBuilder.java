package xaos.tasks;

import java.util.ArrayList;

import xaos.actions.ActionManagerItem;
import xaos.main.Game;
import xaos.main.World;
import xaos.panels.MainPanel;
import xaos.tiles.Cell;
import xaos.utils.Point3D;
import xaos.utils.Point3DShort;

final class TaskHotPointBuilder {

    private final Task owner;

    TaskHotPointBuilder(Task owner) {
        this.owner = owner;
    }

    void setZoneHotPoints() {
        TaskArea area = getTaskArea();

        switch (owner.getTask()) {
            case TaskTypes.TASK_MINE:
            case TaskTypes.TASK_DIG:
            case TaskTypes.TASK_MINE_LADDER:
            case TaskTypes.TASK_CUSTOM_ACTION:
            case TaskTypes.TASK_CANCEL_ORDER:
                setActionHotPoints(area);
                break;

            case TaskTypes.TASK_MOVE_TO_CARAVAN:
                setMoveToCaravanHotPoint();
                break;

            case TaskTypes  .TASK_FOOD_NEEDED:
                setFoodNeededHotPoint();
                break;

            case TaskTypes.TASK_BUILD:
                new TaskBuildHelper(owner).setBuildHotPoints(area);
                break;

            case TaskTypes.TASK_STOCKPILE:
                new TaskStockpileHelper(owner).createStockpile(area);
                break;

            case TaskTypes.TASK_CREATE_ZONE:
                new TaskZoneHelper(owner).createZone(area);
                break;

            case TaskTypes.TASK_EXPAND_ZONE:
                new TaskZoneHelper(owner).expandZone(area);
                break;

            case TaskTypes.TASK_CREATE_AND_PLACE:
            case TaskTypes.TASK_QUEUE_AND_PLACE:
                new TaskCreateAndPlaceHelper(owner).createAndPlaceItem(area);
                break;

            case TaskTypes.TASK_CREATE_AND_PLACE_ROW:
                createAndPlaceRow(area, TaskTypes.TASK_CREATE_AND_PLACE);
                break;

            case TaskTypes.TASK_QUEUE_AND_PLACE_ROW:
                createAndPlaceRow(area, TaskTypes.TASK_QUEUE_AND_PLACE);
                break;

            case TaskTypes.TASK_QUEUE_AND_PLACE_AREA:
                createAndPlaceArea(area);
                break;

            default:
                break;
        }

        markFinishedIfNoHotPoints();
    }

    private TaskArea getTaskArea() {
        TaskArea area = new TaskArea();

        area.xStart = (short) owner.getPointIni().x;
        area.yStart = (short) owner.getPointIni().y;
        area.z = (short) owner.getPointIni().z;

        if (owner.getPointEnd() == null) {
            area.xEnd = area.xStart;
            area.yEnd = area.yStart;
            return area;
        }

        area.xEnd = (short) owner.getPointEnd().x;
        area.yEnd = (short) owner.getPointEnd().y;

        if (area.xStart > area.xEnd) {
            short temp = area.xStart;
            area.xStart = area.xEnd;
            area.xEnd = temp;
            area.xSwapped = true;
        }

        if (area.yStart > area.yEnd) {
            short temp = area.yStart;
            area.yStart = area.yEnd;
            area.yEnd = temp;
            area.ySwapped = true;
        }

        return area;
    }

    private void setMoveToCaravanHotPoint() {
        owner.addHotPoint(new HotPoint(
                owner.getPointIni().toPoint3DShort(),
                owner.getPointEnd().toPoint3DShort()));
    }

    private void setFoodNeededHotPoint() {
        Point3DShort point = owner.getPointIni().toPoint3DShort();
        owner.addHotPoint(new HotPoint(point, point));
    }

    private void markFinishedIfNoHotPoints() {
        if (owner.getHotPoints().size() == 0) {
            owner.setFinished(true);
        }
    }

    private void setActionHotPoints(TaskArea area) {
        Cell[][][] cells = World.getCells();
        TaskCustomActionBuilder customActionBuilder = new TaskCustomActionBuilder(owner);

        ArrayList<ActionManagerItem> actionItems = customActionBuilder.getCustomActionItems();
        ArrayList<String> actionParameters = customActionBuilder.getCustomActionParameters();

        for (short x = area.xStart; x <= area.xEnd; x++) {
            for (short y = area.yStart; y <= area.yEnd; y++) {
                Cell cell = cells[x][y][area.z];

                if (isMiningTask()) {
                    addMineHotPoint(cells, x, y);
                } else if (owner.getTask() == Task.TASK_CUSTOM_ACTION) {
                    customActionBuilder.addCustomActionsForCell(cell, x, y, area.z, actionItems, actionParameters);
                } else if (owner.getTask() == Task.TASK_CANCEL_ORDER) {
                    addCancelHotPointIfNeeded(cell, x, y, area.z);
                }
            }
        }

        if (owner.getTask() == Task.TASK_CANCEL_ORDER) {
            new TaskCancelHelper(owner).cancelExistingOrders();
        } else {
            flagHotPointCellsAsOrdered();
            owner.setMaxCitizens(owner.getHotPoints().size());
        }
    }

    private boolean isMiningTask() {
        return owner.getTask() == Task.TASK_MINE
                || owner.getTask() == Task.TASK_DIG
                || owner.getTask() == Task.TASK_MINE_LADDER;
    }

    private void addMineHotPoint(Cell[][][] cells, short x, short y) {
        Cell targetCell = owner.getMineTargetCell(cells, x, y);

        if (!owner.canMineTargetCell(targetCell)) {
            return;
        }

        Point3DShort targetPoint = Point3DShort.getPoolInstance(
                x,
                y,
                targetCell.getCoordinates().z);

        if (hasExistingUnfinishedTaskPoint(owner.getTask(), targetPoint)) {
            return;
        }

        ArrayList<Point3DShort> accessPoints = Task.getAccessingPoints(
                x,
                y,
                targetCell.getCoordinates().z,
                owner.getTask());

        owner.addHotPoint(new HotPoint(targetPoint, accessPoints));
    }

    private void addCancelHotPointIfNeeded(Cell cell, short x, short y, short z) {
        if (!cell.isFlagOrders()) {
            return;
        }

        Point3DShort cancelPoint = Point3DShort.getPoolInstance(x, y, z);
        owner.addHotPoint(new HotPoint(cancelPoint, cancelPoint));
    }

    private void flagHotPointCellsAsOrdered() {
        for (int hotPointIndex = 0; hotPointIndex < owner.getHotPoints().size(); hotPointIndex++) {
            World.getCell(owner.getHotPoints().get(hotPointIndex).getHotPoint()).setFlagOrders(true);
        }
    }

    private void createAndPlaceRow(TaskArea area, int taskType) {
        boolean isHorizontal = (area.xEnd - area.xStart) >= (area.yEnd - area.yStart);
        boolean toggled3DMouse = disable3DMouseIfNeeded();

        if (isHorizontal) {
            for (int x = area.xStart; x <= area.xEnd; x++) {
                createPlacementTask(taskType, x, area.ySwapped ? area.yEnd : area.yStart, area.z);
            }
        } else {
            for (int y = area.yStart; y <= area.yEnd; y++) {
                createPlacementTask(taskType, area.xSwapped ? area.xEnd : area.xStart, y, area.z);
            }
        }

        restore3DMouseIfNeeded(toggled3DMouse);
    }

    private void createAndPlaceArea(TaskArea area) {
        boolean toggled3DMouse = disable3DMouseIfNeeded();

        for (int x = area.xStart; x <= area.xEnd; x++) {
            for (int y = area.yStart; y <= area.yEnd; y++) {
                createPlacementTask(TaskTypes.TASK_QUEUE_AND_PLACE, x, y, area.z);
            }
        }

        restore3DMouseIfNeeded(toggled3DMouse);
    }

    private void createPlacementTask(int taskType, int x, int y, short z) {
        Task task = new Task(taskType);
        task.setParameter(owner.getParameter());
        task.setPoint(new Point3D(x, y, z));
    }

    private boolean disable3DMouseIfNeeded() {
        if (!MainPanel.tDMouseON) {
            return false;
        }

        MainPanel.toggle3DMouse();
        return true;
    }

    private void restore3DMouseIfNeeded(boolean toggled3DMouse) {
        if (toggled3DMouse) {
            MainPanel.toggle3DMouse();
        }
    }

    private boolean hasExistingUnfinishedTaskPoint(int taskType, Point3DShort point) {
        ArrayList<TaskManagerItem> taskItems = Game.getWorld().getTaskManager().getTaskItems();

        for (int taskIndex = 0; taskIndex < taskItems.size(); taskIndex++) {
            Task existingTask = taskItems.get(taskIndex).getTask();

            if (existingTask.getTask() != taskType) {
                continue;
            }

            if (hasUnfinishedHotPoint(existingTask, point)) {
                return true;
            }
        }

        return false;
    }

    private boolean hasUnfinishedHotPoint(Task task, Point3DShort point) {
        if (task == null || point == null || task.getHotPoints() == null) {
            return false;
        }

        for (HotPoint hotPoint : task.getHotPoints()) {
            if (isMatchingUnfinishedHotPoint(hotPoint, point)) {
                return true;
            }
        }

        return false;
    }

    private boolean isMatchingUnfinishedHotPoint(HotPoint hotPoint, Point3DShort point) {
        return hotPoint != null
                && hotPoint.getHotPoint() != null
                && hotPoint.getHotPoint().equals(point)
                && !hotPoint.isFinished();
    }
}