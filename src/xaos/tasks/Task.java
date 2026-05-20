package xaos.tasks;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import xaos.actions.ActionManager;
import xaos.actions.ActionManagerItem;
import xaos.actions.QueueItem;
import xaos.main.Game;
import xaos.main.World;
import xaos.panels.MainPanel;
import xaos.platform.lwjgl3.input.Keyboard;
import xaos.tiles.Cell;
import xaos.tiles.Tile;
import xaos.tiles.entities.items.Item;
import xaos.utils.Log;
import xaos.utils.Messages;
import xaos.utils.Point3D;
import xaos.utils.Point3DShort;

public final class Task implements Externalizable {

    private static final long serialVersionUID = -1621427522490649314L;

    public static int ID_INDEX = 0;

    // Compatibility constants.
    // These allow existing code using Task.TASK_... to keep compiling while the real
    // values live in TaskTypes.
    public static final int TASK_NO_TASK = TaskTypes.TASK_NO_TASK;
    public static final int TASK_DIG = TaskTypes.TASK_DIG;
    public static final int TASK_MINE = TaskTypes.TASK_MINE;
    public static final int TASK_CANCEL_ORDER = TaskTypes.TASK_CANCEL_ORDER;
    public static final int TASK_MINE_LADDER = TaskTypes.TASK_MINE_LADDER;

    public static final int TASK_WEAR = TaskTypes.TASK_WEAR;
    public static final int TASK_WEAR_OFF = TaskTypes.TASK_WEAR_OFF;
    public static final int TASK_CONVERT_TO_CIVILIAN = TaskTypes.TASK_CONVERT_TO_CIVILIAN;
    public static final int TASK_CONVERT_TO_SOLDIER = TaskTypes.TASK_CONVERT_TO_SOLDIER;
    public static final int TASK_FIGHT = TaskTypes.TASK_FIGHT;
    public static final int TASK_HEAL = TaskTypes.TASK_HEAL;
    public static final int TASK_AUTOEQUIP = TaskTypes.TASK_AUTOEQUIP;
    public static final int TASK_SOLDIER_SET_STATE = TaskTypes.TASK_SOLDIER_SET_STATE;
    public static final int TASK_SOLDIER_ADD_PATROL_POINT = TaskTypes.TASK_SOLDIER_ADD_PATROL_POINT;
    public static final int TASK_SOLDIER_REMOVE_PATROL_POINT = TaskTypes.TASK_SOLDIER_REMOVE_PATROL_POINT;

    public static final int TASK_SOLDIER_ADD_PATROL_POINT_GROUP = TaskTypes.TASK_SOLDIER_ADD_PATROL_POINT_GROUP;
    public static final int TASK_SOLDIER_REMOVE_PATROL_POINT_GROUP = TaskTypes.TASK_SOLDIER_REMOVE_PATROL_POINT_GROUP;

    public static final int TASK_BUILD = TaskTypes.TASK_BUILD;
    public static final int TASK_DESTROY_BUILDING = TaskTypes.TASK_DESTROY_BUILDING;
    public static final int TASK_TURN_OFF_NON_STOP = TaskTypes.TASK_TURN_OFF_NON_STOP;
    public static final int TASK_TURN_ON_NON_STOP = TaskTypes.TASK_TURN_ON_NON_STOP;

    public static final int TASK_TERRAIN_RAISE = TaskTypes.TASK_TERRAIN_RAISE;
    public static final int TASK_TERRAIN_LOWER = TaskTypes.TASK_TERRAIN_LOWER;
    public static final int TASK_TERRAIN_CHANGE = TaskTypes.TASK_TERRAIN_CHANGE;
    public static final int TASK_TERRAIN_ADD_FLUID = TaskTypes.TASK_TERRAIN_ADD_FLUID;
    public static final int TASK_TERRAIN_REMOVE_FLUID = TaskTypes.TASK_TERRAIN_REMOVE_FLUID;

    public static final int TASK_CREATE_AND_PLACE = TaskTypes.TASK_CREATE_AND_PLACE;
    public static final int TASK_REMOVE_BUILDING_TASK = TaskTypes.TASK_REMOVE_BUILDING_TASK;
    public static final int TASK_CREATE_IN_A_BUILDING = TaskTypes.TASK_CREATE_IN_A_BUILDING;
    public static final int TASK_CREATE = TaskTypes.TASK_CREATE;
    public static final int TASK_DESTROY_ENTITY = TaskTypes.TASK_DESTROY_ENTITY;
    public static final int TASK_CREATE_AND_PLACE_ROW = TaskTypes.TASK_CREATE_AND_PLACE_ROW;
    public static final int TASK_LOCK = TaskTypes.TASK_LOCK;
    public static final int TASK_UNLOCK_OPEN = TaskTypes.TASK_UNLOCK_OPEN;
    public static final int TASK_UNLOCK_CLOSE = TaskTypes.TASK_UNLOCK_CLOSE;

    public static final int TASK_STOCKPILE = TaskTypes.TASK_STOCKPILE;
    public static final int TASK_DELETE_STOCKPILE = TaskTypes.TASK_DELETE_STOCKPILE;

    public static final int TASK_CREATE_ZONE = TaskTypes.TASK_CREATE_ZONE;
    public static final int TASK_DELETE_ZONE = TaskTypes.TASK_DELETE_ZONE;
    public static final int TASK_EXPAND_ZONE = TaskTypes.TASK_EXPAND_ZONE;
    public static final int TASK_CHANGE_OWNER = TaskTypes.TASK_CHANGE_OWNER;
    public static final int TASK_CHANGE_OWNER_GROUP = TaskTypes.TASK_CHANGE_OWNER_GROUP;

    public static final int TASK_HAUL = TaskTypes.TASK_HAUL;
    public static final int TASK_MOVE_AND_LOCK = TaskTypes.TASK_MOVE_AND_LOCK;
    public static final int TASK_DROP = TaskTypes.TASK_DROP;
    public static final int TASK_PUT_IN_CONTAINER = TaskTypes.TASK_PUT_IN_CONTAINER;
    public static final int TASK_REMOVE_FROM_CONTAINER = TaskTypes.TASK_REMOVE_FROM_CONTAINER;

    public static final int TASK_SLEEP = TaskTypes.TASK_SLEEP;
    public static final int TASK_EAT = TaskTypes.TASK_EAT;

    public static final int TASK_CUSTOM_ACTION = TaskTypes.TASK_CUSTOM_ACTION;
    public static final int TASK_QUEUE = TaskTypes.TASK_QUEUE;
    public static final int TASK_QUEUE_AND_PLACE = TaskTypes.TASK_QUEUE_AND_PLACE;
    public static final int TASK_QUEUE_AND_PLACE_ROW = TaskTypes.TASK_QUEUE_AND_PLACE_ROW;
    public static final int TASK_QUEUE_AND_PLACE_AREA = TaskTypes.TASK_QUEUE_AND_PLACE_AREA;

    public static final int TASK_MOVE_TO_CARAVAN = TaskTypes.TASK_MOVE_TO_CARAVAN;
    public static final int TASK_FOOD_NEEDED = TaskTypes.TASK_FOOD_NEEDED;

    public static final int STATE_CREATING_INIZONE = TaskTypes.STATE_CREATING_INIZONE;
    public static final int STATE_CREATING_ENDZONE = TaskTypes.STATE_CREATING_ENDZONE;
    public static final int STATE_CREATING_SINGLEPOINT = TaskTypes.STATE_CREATING_SINGLEPOINT;
    public static final int STATE_CREATED = TaskTypes.STATE_CREATED;

    private int id;
    private int task;
    private int state;
    private Point3D pointIni;
    private Point3D pointEnd;
    private ArrayList<HotPoint> hotPoints;
    private int maxCitizens;
    private String parameter;
    private String parameter2;
    private int face = Item.FACE_WEST;

    private boolean finished = false;

    private transient Tile tile;
    private transient int iconType;

    public Task() {
    }

    public Task(int taskId) {
        setID(ID_INDEX);
        ID_INDEX++;
        setTask(taskId);
        setMaxCitizens(1);
    }

    public void setID(int id) {
        this.id = id;
    }

    public int getID() {
        return id;
    }

    public int getTask() {
        return task;
    }

    public void setTask(int taskId) {
        this.task = taskId;
        setState(getInitialStateForTask(taskId));
    }

    private int getInitialStateForTask(int taskId) {
        if (requiresAreaSelection(taskId)) {
            return STATE_CREATING_INIZONE;
        }

        if (requiresSinglePointSelection(taskId)) {
            return STATE_CREATING_SINGLEPOINT;
        }

        return STATE_CREATED;
    }

    private boolean requiresAreaSelection(int taskId) {
        return taskId == TASK_MINE
                || taskId == TASK_MINE_LADDER
                || taskId == TASK_DIG
                || taskId == TASK_CANCEL_ORDER
                || taskId == TASK_STOCKPILE
                || taskId == TASK_CREATE_ZONE
                || taskId == TASK_EXPAND_ZONE
                || taskId == TASK_CREATE_AND_PLACE_ROW
                || taskId == TASK_QUEUE_AND_PLACE_ROW
                || taskId == TASK_QUEUE_AND_PLACE_AREA
                || taskId == TASK_CUSTOM_ACTION;
    }

    private boolean requiresSinglePointSelection(int taskId) {
        return taskId == TASK_BUILD
                || taskId == TASK_CREATE_AND_PLACE
                || taskId == TASK_QUEUE_AND_PLACE;
    }

    public static int getHappiness(Task task) {
        if (task == null) {
            return 1;
        }

        return getHappinessForTaskType(task.getTask());
    }

    private static int getHappinessForTaskType(int taskType) {
        switch (taskType) {
            case TASK_NO_TASK:
            case TASK_SLEEP:
            case TASK_EAT:
                return 1;

            case TASK_HAUL:
            case TASK_PUT_IN_CONTAINER:
            case TASK_MOVE_TO_CARAVAN:
            case TASK_FOOD_NEEDED:
            case TASK_CUSTOM_ACTION:
            case TASK_MINE:
            case TASK_MINE_LADDER:
            case TASK_FIGHT:
            case TASK_BUILD:
            case TASK_CREATE:
            case TASK_CREATE_AND_PLACE:
            case TASK_QUEUE:
            case TASK_QUEUE_AND_PLACE:
            case TASK_MOVE_AND_LOCK:
                return -2;

            default:
                return 0;
        }
    }

    public static boolean isWorkingTask(Task task) {
        if (task == null) {
            return false;
        }

        return isWorkingTaskType(task.getTask());
    }

    private static boolean isWorkingTaskType(int taskType) {
        switch (taskType) {
            case TASK_NO_TASK:
            case TASK_SLEEP:
            case TASK_EAT:
                return false;

            default:
                return true;
        }
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    private static final Map<Integer, String> TASK_MESSAGE_KEYS = new HashMap<Integer, String>();

    static {
        TASK_MESSAGE_KEYS.put(TASK_NO_TASK, "Task.0");

        TASK_MESSAGE_KEYS.put(TASK_DIG, "Task.3");
        TASK_MESSAGE_KEYS.put(TASK_MINE, "Task.3");

        TASK_MESSAGE_KEYS.put(TASK_MINE_LADDER, "Task.41");
        TASK_MESSAGE_KEYS.put(TASK_CANCEL_ORDER, "Task.29");

        TASK_MESSAGE_KEYS.put(TASK_WEAR, "Task.18");
        TASK_MESSAGE_KEYS.put(TASK_AUTOEQUIP, "Task.38");
        TASK_MESSAGE_KEYS.put(TASK_WEAR_OFF, "Task.24");
        TASK_MESSAGE_KEYS.put(TASK_FIGHT, "Task.25");
        TASK_MESSAGE_KEYS.put(TASK_HEAL, "Task.28");

        TASK_MESSAGE_KEYS.put(TASK_BUILD, "Task.4");

        TASK_MESSAGE_KEYS.put(TASK_CREATE_AND_PLACE, "Task.5");
        TASK_MESSAGE_KEYS.put(TASK_QUEUE_AND_PLACE, "Task.5");
        TASK_MESSAGE_KEYS.put(TASK_CREATE_AND_PLACE_ROW, "Task.5");
        TASK_MESSAGE_KEYS.put(TASK_QUEUE_AND_PLACE_ROW, "Task.5");
        TASK_MESSAGE_KEYS.put(TASK_QUEUE_AND_PLACE_AREA, "Task.5");

        TASK_MESSAGE_KEYS.put(TASK_REMOVE_BUILDING_TASK, "Task.20");

        TASK_MESSAGE_KEYS.put(TASK_QUEUE, "Task.1");

        TASK_MESSAGE_KEYS.put(TASK_CREATE, "Task.23");
        TASK_MESSAGE_KEYS.put(TASK_CREATE_IN_A_BUILDING, "Task.23");

        TASK_MESSAGE_KEYS.put(TASK_STOCKPILE, "Task.6");

        TASK_MESSAGE_KEYS.put(TASK_CREATE_ZONE, "Task.26");
        TASK_MESSAGE_KEYS.put(TASK_DELETE_ZONE, "Task.27");
        TASK_MESSAGE_KEYS.put(TASK_EXPAND_ZONE, "Task.35");

        TASK_MESSAGE_KEYS.put(TASK_HAUL, "Task.7");
        TASK_MESSAGE_KEYS.put(TASK_PUT_IN_CONTAINER, "Task.7");

        TASK_MESSAGE_KEYS.put(TASK_MOVE_AND_LOCK, "Task.8");
        TASK_MESSAGE_KEYS.put(TASK_DROP, "Task.9");

        TASK_MESSAGE_KEYS.put(TASK_SLEEP, "Task.10");
        TASK_MESSAGE_KEYS.put(TASK_EAT, "Task.11");

        TASK_MESSAGE_KEYS.put(TASK_MOVE_TO_CARAVAN, "Task.39");
        TASK_MESSAGE_KEYS.put(TASK_FOOD_NEEDED, "Task.42");
        TASK_MESSAGE_KEYS.put(TASK_REMOVE_FROM_CONTAINER, "Task.43");
    }

    @Override
    public String toString() {
        if (task == TASK_CUSTOM_ACTION) {
            return getCustomActionName();
        }

        String messageKey = TASK_MESSAGE_KEYS.get(task);

        if (messageKey != null) {
            return Messages.getString(messageKey); //$NON-NLS-1$
        }

        return Messages.getString("Task.12"); //$NON-NLS-1$
    }

    private String getCustomActionName() {
        String actionParameter = getParameter();

        ActionManagerItem actionItem = ActionManager.getItem(actionParameter);
        if (actionItem != null && actionItem.getName() != null) {
            return actionItem.getName();
        }

        if (actionParameter != null && actionParameter.contains(",")) { //$NON-NLS-1$
            String customActionNames = getCustomActionNames(actionParameter);

            if (customActionNames.length() > 0) {
                return customActionNames;
            }
        }

        return Messages.getString("Task.12"); //$NON-NLS-1$
    }

    private String getCustomActionNames(String actionParameter) {
        StringTokenizer tokenizer = new StringTokenizer(actionParameter, ","); //$NON-NLS-1$
        ArrayList<String> actionNames = new ArrayList<String>();

        while (tokenizer.hasMoreTokens()) {
            String actionId = tokenizer.nextToken().trim();
            ActionManagerItem actionItem = ActionManager.getItem(actionId);

            if (actionItem != null && actionItem.getName() != null && !actionNames.contains(actionItem.getName())) {
                actionNames.add(actionItem.getName());
            }
        }

        return joinActionNames(actionNames);
    }

    private String joinActionNames(ArrayList<String> actionNames) {
        StringBuilder actionNamesText = new StringBuilder();

        for (int i = 0; i < actionNames.size(); i++) {
            if (i > 0) {
                actionNamesText.append(", "); //$NON-NLS-1$
            }

            actionNamesText.append(actionNames.get(i));
        }

        return actionNamesText.toString();
    }

    private static final Map<Integer, String> TASK_STATE_MESSAGE_KEYS = new HashMap<Integer, String>();

    static {
        TASK_STATE_MESSAGE_KEYS.put(STATE_CREATING_INIZONE, "Task.13");
        TASK_STATE_MESSAGE_KEYS.put(STATE_CREATING_ENDZONE, "Task.14");
        TASK_STATE_MESSAGE_KEYS.put(STATE_CREATING_SINGLEPOINT, "Task.15");
    }

    public String toStringState() {
        String messageKey = TASK_STATE_MESSAGE_KEYS.get(state);

        if (messageKey != null) {
            return Messages.getString(messageKey); //$NON-NLS-1$
        }

        return Messages.getString("Task.16"); //$NON-NLS-1$
    }

    public Point3D getPointIni() {
        return pointIni;
    }

    public void setPointIni(Point3D initialPoint) {
        this.pointIni = initialPoint;
    }

    public void setPointIni(Point3DShort initialPoint) {
        this.pointIni = initialPoint == null ? null : initialPoint.toPoint3D();
    }

    public Point3D getPointEnd() {
        return pointEnd;
    }

    public void setPointEnd(Point3D pointEnd) {
        this.pointEnd = pointEnd;
    }

    public void setPointEnd(Point3DShort pointEnd) {
        this.pointEnd = pointEnd == null ? null : pointEnd.toPoint3D();
    }

    public void setPoint(Point3D selectedPoint) {
        int previousTaskType = getTask();
        boolean shouldRepeatTask = false;

        if (state == STATE_CREATING_INIZONE) {
            setInitialAreaPoint(selectedPoint);
        } else if (state == STATE_CREATING_ENDZONE) {
            setFinalAreaPoint(selectedPoint);
            shouldRepeatTask = isShiftPressed();
        } else if (state == STATE_CREATING_SINGLEPOINT) {
            setSingleTaskPoint(selectedPoint);
            shouldRepeatTask = isShiftPressed();
        }

        if (shouldRepeatTask) {
            createRepeatedTask(previousTaskType);
        }
    }

    private void setInitialAreaPoint(Point3D selectedPoint) {
        setPointIni(selectedPoint);
        setState(STATE_CREATING_ENDZONE);
    }

    private void setFinalAreaPoint(Point3D selectedPoint) {
        if (MainPanel.tDMouseON) {
            setPointEnd(new Point3D(selectedPoint.x, selectedPoint.y, getPointIni().z));
        } else {
            setPointIni(new Point3D(getPointIni().x, getPointIni().y, selectedPoint.z));
            setPointEnd(selectedPoint);
        }

        finishCreation();
    }

    private void setSingleTaskPoint(Point3D selectedPoint) {
        setPointIni(selectedPoint);
        finishCreation();
    }

    private boolean isShiftPressed() {
        return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)
                || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
    }

    private void createRepeatedTask(int taskType) {
        Game.createTask(taskType);

        Task repeatedTask = Game.getCurrentTask();
        repeatedTask.setParameter(getParameter());
        repeatedTask.setParameter2(getParameter2());
        repeatedTask.setTile(getTile(), getIconType());
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;

        if (isQueueAndPlaceTask()) {
            setQueueAndPlaceItemParameter();
        }
    }

    public String getParameter2() {
        return parameter2;
    }

    public void setParameter2(String parameter2) {
        this.parameter2 = parameter2;
    }

    private boolean isQueueAndPlaceTask() {
        return task == TASK_QUEUE_AND_PLACE
                || task == TASK_QUEUE_AND_PLACE_ROW
                || task == TASK_QUEUE_AND_PLACE_AREA;
    }

    private void setQueueAndPlaceItemParameter() {
        ActionManagerItem actionItem = ActionManager.getItem(getParameter());

        if (actionItem == null) {
            logInvalidQueueAction();
            Game.deleteCurrentTask();
            return;
        }

        String itemToCreate = findLastCreatedItemInQueue(actionItem.getQueue());

        if (itemToCreate == null) {
            logMissingCreatedItemInQueue();
            Game.deleteCurrentTask();
            return;
        }

        setParameter2(itemToCreate);
    }

    private String findLastCreatedItemInQueue(ArrayList<QueueItem> queueItems) {
        for (int i = queueItems.size() - 1; i >= 0; i--) {
            QueueItem queueItem = queueItems.get(i);

            if (isCreateItemQueueEntry(queueItem)) {
                return queueItem.getValue();
            }
        }

        return null;
    }

    private boolean isCreateItemQueueEntry(QueueItem queueItem) {
        return queueItem.getType() == QueueItem.TYPE_CREATE_ITEM
                || queueItem.getType() == QueueItem.TYPE_CREATE_ITEM_BY_TYPE;
    }

    private void logInvalidQueueAction() {
        Log.log(
                Log.LEVEL_ERROR,
                Messages.getString("Task.34") + getParameter() + "]", //$NON-NLS-1$ //$NON-NLS-2$
                getClass().toString());
    }

    private void logMissingCreatedItemInQueue() {
        Log.log(
                Log.LEVEL_ERROR,
                Messages.getString("Task.36") + getParameter() + "]", //$NON-NLS-1$ //$NON-NLS-2$
                getClass().toString());
    }

    public void setFace(int face) {
        this.face = face;
    }

    public int getFace() {
        return face;
    }

    public void finishCreation() {
        if (tryExecuteCreateAndPlaceImmediatelyInGodMode()) {
            return;
        }

        prepareForQueue();

        if (tryExecuteImmediatelyInGodMode()) {
            return;
        }

        Game.taskCreated(this);
    }

    private boolean tryExecuteCreateAndPlaceImmediatelyInGodMode() {
        if (!Game.isGodMode()) {
            return false;
        }

        TaskImmediateExecutor executor = new TaskImmediateExecutor(this);

        if (!executor.isImmediateCreateAndPlaceTask()) {
            return false;
        }

        if (!executor.executeCreateAndPlaceImmediately()) {
            return false;
        }

        deleteCurrentTaskIfThisTaskIsActive();
        return true;
    }

    private boolean tryExecuteImmediatelyInGodMode() {
        if (!Game.isGodMode()) {
            return false;
        }

        if (!canExecuteImmediately()) {
            return false;
        }

        if (!executeImmediately()) {
            return false;
        }

        deleteCurrentTaskIfThisTaskIsActive();
        return true;
    }

    private void deleteCurrentTaskIfThisTaskIsActive() {
        if (this == Game.getCurrentTask()) {
            Game.deleteCurrentTask();
        }
    }

    public boolean canExecuteImmediately() {
        return new TaskImmediateExecutor(this).canExecuteImmediately();
    }

    public boolean executeImmediately() {
        return new TaskImmediateExecutor(this).executeImmediately();
    }

    public boolean executeMineAtImmediately(Point3DShort hotPoint3D) {
        return new TaskImmediateExecutor(this).executeMineAtImmediately(hotPoint3D);
    }

    public boolean executeMineAt(Point3DShort hotPoint3D) {
        return new TaskImmediateExecutor(this).executeMineAt(hotPoint3D);
    }

    public void prepareForQueue() {
        setZoneHotPoints();
        setState(STATE_CREATED);
    }

    public void setZoneHotPoints() {
        new TaskHotPointBuilder(this).setZoneHotPoints();
    }

    Cell getMineTargetCell(Cell[][][] cells, short x, short y) {
        if (getTask() == TASK_DIG) {
            if (getPointIni().z >= World.MAP_DEPTH - 2) {
                return null;
            }

            return cells[x][y][getPointIni().z + 1];
        }

        if (getTask() == TASK_MINE || getTask() == TASK_MINE_LADDER) {
            if (getPointIni().z >= World.MAP_DEPTH - 1) {
                return null;
            }

            return cells[x][y][getPointIni().z];
        }

        return null;
    }

    boolean canMineTargetCell(Cell cell) {
        if (cell == null) {
            return false;
        }

        if (cell.getTerrain().hasFluids()) {
            return false;
        }

        return !cell.isMined() || !cell.isDiscovered();
    }

    void addHotPoint(HotPoint hotPoint) {
        if (hotPoints == null) {
            hotPoints = new ArrayList<HotPoint>();
        }

        hotPoints.add(hotPoint);
    }

    public int getMaxCitizens() {
        return maxCitizens;
    }

    public void setMaxCitizens(int maxCitizens) {
        if (isBuildTask() && maxCitizens > 1) {
            this.maxCitizens = 1;
            return;
        }

        this.maxCitizens = maxCitizens;
    }

    private boolean isBuildTask() {
        return getTask() == TASK_BUILD;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public void setTile(Tile tile, int iconType) {
        this.tile = tile;
        this.iconType = iconType;
    }

    public Tile getTile() {
        return tile;
    }

    public int getIconType() {
        return iconType;
    }

    public ArrayList<HotPoint> getHotPoints() {
        if (hotPoints == null) {
            hotPoints = new ArrayList<HotPoint>();
        }

        return hotPoints;
    }

    public HotPoint getHotPoint(int hotPointIndex) {
        return getHotPoints().get(hotPointIndex);
    }

    public void setHotPoints(ArrayList<HotPoint> hotPoints) {
        this.hotPoints = hotPoints;
    }

    public static ArrayList<Point3DShort> getAccessingPoints(int x, int y, int z, int task) {
        return TaskAccessPoints.getAccessingPoints(x, y, z, task);
    }

    /**
     * @deprecated Use {@link #getAccessingPoints(int, int, int, int)} instead.
     */
    @Deprecated
    public static ArrayList<Point3DShort> getAccesingPoints(int x, int y, int z, int task) {
        return getAccessingPoints(x, y, z, task);
    }

    public static ArrayList<Point3DShort> getAccessingPointsMatchingAstarZone(
            int x,
            int y,
            int z,
            int astarZoneId,
            int task) {

        return TaskAccessPoints.getAccessingPointsMatchingAstarZone(x, y, z, astarZoneId, task);
    }

    /**
     * @deprecated Use
     *             {@link #getAccessingPointsMatchingAstarZone(int, int, int, int, int)}
     *             instead.
     */
    @Deprecated
    public static ArrayList<Point3DShort> getAccesingPointsMatchingASZI(
            int x,
            int y,
            int z,
            int aszi,
            int task) {

        return getAccessingPointsMatchingAstarZone(x, y, z, aszi, task);
    }

    public static ArrayList<Point3DShort> getAccesingPointsMatchingASZI(Point3DShort point, int aszi, int task) {
        return getAccesingPointsMatchingASZI(point.x, point.y, point.z, aszi, task);
    }

    public static ArrayList<Point3DShort> getAccesingPointsMatchingASZI(Point3D point, int aszi, int task) {
        return getAccesingPointsMatchingASZI(point.x, point.y, point.z, aszi, task);
    }

    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        id = in.readInt();
        task = in.readInt();
        state = in.readInt();
        pointIni = (Point3D) in.readObject();
        pointEnd = (Point3D) in.readObject();
        hotPoints = (ArrayList<HotPoint>) in.readObject();
        maxCitizens = in.readInt();
        parameter = (String) in.readObject();
        parameter2 = (String) in.readObject();
        finished = in.readBoolean();

        if (Game.SAVEGAME_LOADING_VERSION >= Game.SAVEGAME_V14) {
            face = in.readInt();
        } else {
            face = Item.FACE_WEST;
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(id);
        out.writeInt(task);
        out.writeInt(state);
        out.writeObject(pointIni);
        out.writeObject(pointEnd);
        out.writeObject(hotPoints);
        out.writeInt(maxCitizens);
        out.writeObject(parameter);
        out.writeObject(parameter2);
        out.writeBoolean(finished);
        out.writeInt(face);
    }
}