package xaos.tasks;

import java.util.ArrayList;

import xaos.actions.Action;
import xaos.main.Game;
import xaos.main.World;
import xaos.tiles.entities.items.Item;
import xaos.tiles.entities.living.Citizen;
import xaos.tiles.entities.living.LivingEntity;
import xaos.utils.Point3DShort;

final class TaskCancelHelper {

    private final Task owner;

    TaskCancelHelper(Task owner) {
        this.owner = owner;
    }

    void cancelExistingOrders() {
        checkCancelTask(Game.getWorld().getTaskManager().getTaskItems());
        checkCancelTask(Game.getWorld().getTaskManager().getTaskItemsTemp());

        checkCancelActions(Game.getWorld().getTaskManager().getCustomActions(), true);
        checkCancelActions(Game.getWorld().getTaskManager().getCustomActionsTemp(), false);
        checkCancelActions(Game.getWorld().getTaskManager().getCustomActionsWait(), false);

        owner.setMaxCitizens(0);
        owner.setFinished(true);
    }

    private void checkCancelTask(ArrayList<TaskManagerItem> taskManagerItems) {
        for (int taskIndex = 0; taskIndex < taskManagerItems.size(); taskIndex++) {
            Task task = taskManagerItems.get(taskIndex).getTask();

            if (!isMineTask(task)) {
                continue;
            }

            cancelMatchingMineHotPoints(task);
        }
    }

    private boolean isMineTask(Task task) {
        return task != null
                && (task.getTask() == TaskTypes.TASK_MINE
                        || task.getTask() == TaskTypes.TASK_MINE_LADDER);
    }

    private void cancelMatchingMineHotPoints(Task task) {
        ArrayList<HotPoint> hotPoints = task.getHotPoints();

        for (int hotPointIndex = 0; hotPointIndex < hotPoints.size(); hotPointIndex++) {
            HotPoint hotPoint = hotPoints.get(hotPointIndex);

            if (hotPoint == null || hotPoint.isFinished()) {
                continue;
            }

            Point3DShort taskPoint = hotPoint.getHotPoint();

            if (isCancelPoint(taskPoint)) {
                finishMineHotPoint(task, hotPointIndex, taskPoint);
            }
        }
    }

    private boolean isCancelPoint(Point3DShort taskPoint) {
        if (taskPoint == null) {
            return false;
        }

        for (int cancelPointIndex = 0; cancelPointIndex < owner.getHotPoints().size(); cancelPointIndex++) {
            HotPoint hotPoint = owner.getHotPoints().get(cancelPointIndex);

            if (hotPoint == null || hotPoint.getHotPoint() == null) {
                continue;
            }

            Point3DShort cancelPoint = hotPoint.getHotPoint();

            if (cancelPoint.equals(taskPoint)) {
                return true;
            }
        }

        return false;
    }

    private void finishMineHotPoint(Task task, int hotPointIndex, Point3DShort taskPoint) {
        Game.getWorld().getTaskManager().setHotPointFinished(task, hotPointIndex);

        World.getCell(taskPoint).setFlagOrders(false);
    }

    private void checkCancelActions(ArrayList<Action> actions, boolean checkCitizens) {
        for (int cancelPointIndex = 0; cancelPointIndex < owner.getHotPoints().size(); cancelPointIndex++) {
            HotPoint hotPoint = owner.getHotPoints().get(cancelPointIndex);

            if (hotPoint == null || hotPoint.getHotPoint() == null) {
                continue;
            }

            Point3DShort cancelPoint = hotPoint.getHotPoint();

            cancelQueuedActionsAtPoint(actions, cancelPoint);

            if (checkCitizens) {
                cancelCitizenActionsAtPoint(World.getCitizenIDs(), cancelPoint);
                cancelCitizenActionsAtPoint(World.getSoldierIDs(), cancelPoint);
            }
        }
    }

    private void cancelQueuedActionsAtPoint(ArrayList<Action> actions, Point3DShort cancelPoint) {
        for (int actionIndex = actions.size() - 1; actionIndex >= 0; actionIndex--) {
            Action action = actions.get(actionIndex);

            if (shouldCancelActionAtPoint(action, cancelPoint)) {
                removeQueuedAction(actions, actionIndex, cancelPoint);
            }
        }
    }

    private boolean shouldCancelActionAtPoint(Action action, Point3DShort cancelPoint) {
        return isActionDestinationAtPoint(action, cancelPoint)
                || isActionTerrainAtPoint(action, cancelPoint)
                || isActionEntityAtPoint(action, cancelPoint);
    }

    private boolean isActionDestinationAtPoint(Action action, Point3DShort cancelPoint) {
        return action.getDestinationPoint() != null
                && action.getDestinationPoint().equals(cancelPoint);
    }

    private boolean isActionTerrainAtPoint(Action action, Point3DShort cancelPoint) {
        return action.getTerrainPoint() != null
                && action.getTerrainPoint().equals(cancelPoint);
    }

    private boolean isActionEntityAtPoint(Action action, Point3DShort cancelPoint) {
        if (action.getEntityID() == -1) {
            return false;
        }

        Item item = Item.getItemByID(action.getEntityID());
        if (item != null && item.getCoordinates().equals(cancelPoint)) {
            return true;
        }

        LivingEntity livingEntity = World.getLivingEntityByID(action.getEntityID());
        return livingEntity != null && livingEntity.getCoordinates().equals(cancelPoint);
    }

    private void removeQueuedAction(ArrayList<Action> actions, int actionIndex, Point3DShort cancelPoint) {
        Action removedAction = actions.remove(actionIndex);

        Game.getWorld().getTaskManager().removeFromProductionPanelRegular(removedAction.getId());
        World.getCell(cancelPoint).setFlagOrders(false);
    }

    private void cancelCitizenActionsAtPoint(ArrayList<Integer> livingEntityIds, Point3DShort cancelPoint) {
        for (int entityIndex = 0; entityIndex < livingEntityIds.size(); entityIndex++) {
            Citizen citizen = (Citizen) World.getLivingEntityByID(livingEntityIds.get(entityIndex));

            if (citizen == null) {
                continue;
            }

            cancelCurrentCitizenActionAtPoint(citizen, cancelPoint);
        }
    }

    private void cancelCurrentCitizenActionAtPoint(Citizen citizen, Point3DShort cancelPoint) {
        Action action = citizen.getCurrentCustomAction();

        if (action == null || citizen.getCurrentTask() == null) {
            return;
        }

        if (!isCitizenActionAtPoint(action, cancelPoint)) {
            return;
        }

        citizen.getCurrentTask().setFinished(true);
        Game.getWorld().getTaskManager().removeCitizen(citizen);
        World.getCell(cancelPoint).setFlagOrders(false);
    }

    private boolean isCitizenActionAtPoint(Action action, Point3DShort cancelPoint) {
        return isActionDestinationAtPoint(action, cancelPoint)
                || isActionTerrainAtPoint(action, cancelPoint);
    }
}