package xaos.tasks;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.lwjgl.input.Keyboard;

import xaos.actions.Action;
import xaos.actions.ActionManager;
import xaos.actions.ActionManagerItem;
import xaos.actions.QueueData;
import xaos.actions.QueueItem;
import xaos.campaign.TutorialTrigger;
import xaos.data.HeroPrerequisite;
import xaos.data.SoldierGroupData;
import xaos.data.SoldierGroups;
import xaos.main.Game;
import xaos.main.World;
import xaos.panels.MainPanel;
import xaos.panels.MessagesPanel;
import xaos.stockpiles.Stockpile;
import xaos.tiles.Cell;
import xaos.tiles.Tile;
import xaos.tiles.entities.Entity;
import xaos.tiles.entities.buildings.Building;
import xaos.tiles.entities.buildings.BuildingManager;
import xaos.tiles.entities.buildings.BuildingManagerItem;
import xaos.tiles.entities.items.Item;
import xaos.tiles.entities.items.ItemManager;
import xaos.tiles.entities.items.ItemManagerItem;
import xaos.tiles.entities.living.Citizen;
import xaos.tiles.entities.living.LivingEntity;
import xaos.tiles.entities.living.LivingEntityManager;
import xaos.tiles.entities.living.LivingEntityManagerItem;
import xaos.tiles.entities.living.heroes.Hero;
import xaos.tiles.entities.living.heroes.HeroManager;
import xaos.tiles.terrain.TerrainManager;
import xaos.tiles.terrain.TerrainManagerItem;
import xaos.utils.ColorGL;
import xaos.utils.Log;
import xaos.utils.Messages;
import xaos.utils.Point3D;
import xaos.utils.Point3DShort;
import xaos.utils.Utils;
import xaos.utils.UtilsIniHeaders;
import xaos.zones.Zone;
import xaos.zones.ZoneBarracks;
import xaos.zones.ZoneHeroRoom;
import xaos.zones.ZoneManager;
import xaos.zones.ZoneManagerItem;
import xaos.zones.ZonePersonal;

public final class Task implements Externalizable {

    private static final long serialVersionUID = -1621427522490649314L;

    public static int ID_INDEX = 0;

    // Tipos de tarea
    public final static int TASK_NO_TASK = 0; // Sin tarea
    public final static int TASK_DIG = 3; // Tarea de digar (mine abajo)
    public final static int TASK_MINE = 4; // Tarea de minar
    public final static int TASK_CANCEL_ORDER = 5; // Cancelar órden (mine/dig/chop)
    public final static int TASK_MINE_LADDER = 7; // Tarea de minar y poner escalera

    // Citizens
    public final static int TASK_WEAR = 10; // Tarea de equipar aldeano
    public final static int TASK_WEAR_OFF = 11; // Tarea de 'des'equipar aldeano
    public final static int TASK_CONVERT_TO_CIVILIAN = 12; // Tarea de convertir un soldado en civil
    public final static int TASK_CONVERT_TO_SOLDIER = 13; // Tarea de convertir un civil en soldado
    public final static int TASK_FIGHT = 14; // Tarea de luchar, sólo se asigna a soldados
    public final static int TASK_HEAL = 15; // Tarea de curarse
    public final static int TASK_AUTOEQUIP = 16; // Tarea de autoequiparse
    public final static int TASK_SOLDIER_SET_STATE = 17; // Tarea de cambiar el estado de un soldado (guard, boss around, patrol)
    public final static int TASK_SOLDIER_ADD_PATROL_POINT = 18; // Tarea de añadir un punto de patrol a un soldado
    public final static int TASK_SOLDIER_REMOVE_PATROL_POINT = 19; // Tarea de eliminar un punto de patrol a un soldado

    // Groups
    public final static int TASK_SOLDIER_ADD_PATROL_POINT_GROUP = 20; // Tarea de añadir un punto de patrol a un grupo
    public final static int TASK_SOLDIER_REMOVE_PATROL_POINT_GROUP = 21; // Tarea de eliminar un punto de patrol a un grupo

    // Buildings
    public final static int TASK_BUILD = 25; // Tarea de construir (edificios)
    public final static int TASK_DESTROY_BUILDING = 26;
    public final static int TASK_TURN_OFF_NON_STOP = 27;
    public final static int TASK_TURN_ON_NON_STOP = 28;

    // Terrain
    public final static int TASK_TERRAIN_RAISE = 30;
    public final static int TASK_TERRAIN_LOWER = 31;
    public final static int TASK_TERRAIN_CHANGE = 32;
    public final static int TASK_TERRAIN_ADD_FLUID = 33;
    public final static int TASK_TERRAIN_REMOVE_FLUID = 34;

    // Items
    public final static int TASK_CREATE_AND_PLACE = 40; // Tarea de construir (items) y ponerlos en algún sitio
    public final static int TASK_REMOVE_BUILDING_TASK = 41; // Tarea de eliminar el item que se está construyendo de un edificio
    public final static int TASK_CREATE_IN_A_BUILDING = 42; // Tarea de construir (items) en un edificio dado
    public final static int TASK_CREATE = 43; // Tarea de construir (items) sin especificar edificio ni place
    public final static int TASK_DESTROY_ENTITY = 44;
    public final static int TASK_CREATE_AND_PLACE_ROW = 45; // Tarea de construir (items) y ponerlos en algún sitio. Se crea una fila de ellos
    public final static int TASK_LOCK = 46;
    public final static int TASK_UNLOCK_OPEN = 47;
    public final static int TASK_UNLOCK_CLOSE = 48;

    // Stockpiles
    public final static int TASK_STOCKPILE = 50; // Tarea de crear stockpile
    public final static int TASK_DELETE_STOCKPILE = 51;

    // Zones
    public final static int TASK_CREATE_ZONE = 56; // Tarea de crear zonas (hospital, comedor, carpenters, ...)
    public final static int TASK_DELETE_ZONE = 57; // Tarea de eliminar zona
    public final static int TASK_EXPAND_ZONE = 58; // Tarea de expandir zona
    public final static int TASK_CHANGE_OWNER = 59; // Tarea de cambiar el propietario de la zona
    public final static int TASK_CHANGE_OWNER_GROUP = 60; // Tarea de cambiar el grupo propietario de la zona

    // Haul / Move / put in containers
    public final static int TASK_HAUL = 65; // Tarea de haul (son especiales, se crean "on the fly" y no se guardan en la lista de tareas, desaparecen cuando el aldeano las suelta
    public final static int TASK_MOVE_AND_LOCK = 66; // Como el haul pero persistente (SI se guarda en la lista de tareas). Se usan solamente en las tareas de create, en el caso de que ya haya un item en el mundo y no haya que construir nada
    public final static int TASK_DROP = 67; // Igual que la tarea de haul a excepción que el aldeano no tiene ir a recoger nada ("on the fly" también)
    public final static int TASK_PUT_IN_CONTAINER = 68; // Igual que la tarea de haul a excepción que el aldeano no tiene ir a recoger nada ("on the fly" también)
    public final static int TASK_REMOVE_FROM_CONTAINER = 69; // Igual que la tarea de haul a excepción que el aldeano no tiene ir a recoger nada ("on the fly" también)

    // Sleep / eat
    public final static int TASK_SLEEP = 70;
    public final static int TASK_EAT = 71;

    // Custom action
    public final static int TASK_CUSTOM_ACTION = 80;
    public final static int TASK_QUEUE = 81;
    public final static int TASK_QUEUE_AND_PLACE = 82;
    public final static int TASK_QUEUE_AND_PLACE_ROW = 83;
    public final static int TASK_QUEUE_AND_PLACE_AREA = 84;

	// Containers
//	public final static int TASK_CONTAINER_ENABLE_ALL = 90;
//	public final static int TASK_CONTAINER_DISABLE_ALL = 91;
//	public final static int TASK_CONTAINER_ENABLE_ITEM = 92;
//	public final static int TASK_CONTAINER_DISABLE_ITEM = 93;
    // Caravan
    public final static int TASK_MOVE_TO_CARAVAN = 100;

    // Food
    public final static int TASK_FOOD_NEEDED = 110;

    // Estados de tarea
    public final static int STATE_CREATING_INIZONE = 1; // Para marcar el inicio de un area
    public final static int STATE_CREATING_ENDZONE = 2; // Para marcar el final de un area
    public final static int STATE_CREATING_SINGLEPOINT = 3; // Para marcar un punto en el mapa
    public final static int STATE_CREATED = 10; // Para indicar que la tarea ya está creada

    private int id; // ID
    private int task; // Tipo de tarea (minar, construir, ...)
    private int state; // Estado actual en la creación de la misma (ej: marcando punto inicial de la zona, ...)
    private Point3D pointIni; // Punto inicial de la zona (también se usa en el casi de tareas de un solo punto (ej: construir))
    private Point3D pointEnd; // Punto final de la zona
    private ArrayList<HotPoint> hotPoints;
    private int maxCitizens; // Máximo de aldeanos que pueden realizar la tarea
    private String parameter; // Parámetro usado en ciertas tareas
    private String parameter2; // Parámetro usado en ciertas tareas
    private int face = Item.FACE_WEST; // Parámetro usado para rotar los items construidos

    private boolean finished = false; // Indica si la tarea está finalizada para que el gestor de taeras la elimine cuando le pete

    private transient Tile tile; // Icono para mostrar al setear esa acción
    private transient int iconType; // Tipo de icono para saber que textura usar

    public Task() {
    }

    public Task(int iTaskID) {
        setID(ID_INDEX);
        ID_INDEX++;
        setTask(iTaskID);
        setMaxCitizens(1); // Por defecto 1
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

    public void setTask(int taskID) {
        task = taskID;

        if (task == Task.TASK_MINE || task == Task.TASK_MINE_LADDER || task == Task.TASK_DIG || task == Task.TASK_CANCEL_ORDER || task == Task.TASK_STOCKPILE || task == Task.TASK_CREATE_ZONE || task == Task.TASK_EXPAND_ZONE || task == Task.TASK_CREATE_AND_PLACE_ROW || task == Task.TASK_QUEUE_AND_PLACE_ROW || task == Task.TASK_QUEUE_AND_PLACE_AREA || task == Task.TASK_CUSTOM_ACTION) {
            setState(STATE_CREATING_INIZONE);
        } else if (task == Task.TASK_BUILD || task == Task.TASK_CREATE_AND_PLACE || task == Task.TASK_QUEUE_AND_PLACE) {
            setState(STATE_CREATING_SINGLEPOINT);
        } else {
            setState(STATE_CREATED);
        }
    }

    /**
     * Devuelve el valor de happines segun la tarea pasada
     *
     * @param oTask
     * @return
     */
    public static int getHappiness(Task oTask) {
        if (oTask == null) {
            return 1;
        }

        int task = oTask.getTask();
        switch (task) {
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
            //case TASK_DIG: // No hace falta, un aldeano nunca tendrá esta tarea
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

    /**
     * Indica si la tarea se considera trabajo (se usa en el modificador de
     * happiness)
     *
     * @param oTask
     * @return
     */
    public static boolean isWorkingTask(Task oTask) {
        if (oTask == null) {
            return false;
        }

        int task = oTask.getTask();
        switch (task) {
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

    public String toString() {
        switch (task) {
            case TASK_NO_TASK:
                return Messages.getString("Task.0"); //$NON-NLS-1$
            case TASK_DIG:
            case TASK_MINE:
                return Messages.getString("Task.3"); //$NON-NLS-1$
            case TASK_MINE_LADDER:
                return Messages.getString("Task.41"); //$NON-NLS-1$
            case TASK_CANCEL_ORDER:
                return Messages.getString("Task.29"); //$NON-NLS-1$
            case TASK_WEAR:
                return Messages.getString("Task.18"); //$NON-NLS-1$
            case TASK_AUTOEQUIP:
                return Messages.getString("Task.38"); //$NON-NLS-1$
            case TASK_WEAR_OFF:
                return Messages.getString("Task.24"); //$NON-NLS-1$
            case TASK_FIGHT:
                return Messages.getString("Task.25"); //$NON-NLS-1$
            case TASK_HEAL:
                return Messages.getString("Task.28"); //$NON-NLS-1$
            case TASK_BUILD:
                return Messages.getString("Task.4"); //$NON-NLS-1$
            case TASK_CREATE_AND_PLACE:
            case TASK_QUEUE_AND_PLACE:
            case TASK_CREATE_AND_PLACE_ROW:
            case TASK_QUEUE_AND_PLACE_ROW:
            case TASK_QUEUE_AND_PLACE_AREA:
                return Messages.getString("Task.5"); //$NON-NLS-1$
            case TASK_REMOVE_BUILDING_TASK:
                return Messages.getString("Task.20"); //$NON-NLS-1$
            case TASK_QUEUE:
                return Messages.getString("Task.1"); //$NON-NLS-1$
            case TASK_CREATE:
                return Messages.getString("Task.23"); //$NON-NLS-1$
            case TASK_CREATE_IN_A_BUILDING:
                return Messages.getString("Task.23"); //$NON-NLS-1$
            case TASK_STOCKPILE:
                return Messages.getString("Task.6"); //$NON-NLS-1$
            case TASK_CREATE_ZONE:
                return Messages.getString("Task.26"); //$NON-NLS-1$
            case TASK_DELETE_ZONE:
                return Messages.getString("Task.27"); //$NON-NLS-1$
            case TASK_EXPAND_ZONE:
                return Messages.getString("Task.35"); //$NON-NLS-1$
            case TASK_HAUL:
            case TASK_PUT_IN_CONTAINER:
                return Messages.getString("Task.7"); //$NON-NLS-1$
            case TASK_MOVE_AND_LOCK:
                return Messages.getString("Task.8"); //$NON-NLS-1$
            case TASK_DROP:
                return Messages.getString("Task.9"); //$NON-NLS-1$
            case TASK_SLEEP:
                return Messages.getString("Task.10"); //$NON-NLS-1$
            case TASK_EAT:
                return Messages.getString("Task.11"); //$NON-NLS-1$
            case TASK_MOVE_TO_CARAVAN:
                return Messages.getString("Task.39"); //$NON-NLS-1$
            case TASK_FOOD_NEEDED:
                return Messages.getString("Task.42"); //$NON-NLS-1$

            case TASK_REMOVE_FROM_CONTAINER:
                return Messages.getString("Task.43"); //$NON-NLS-1$
            case TASK_CUSTOM_ACTION:
                ActionManagerItem ami = ActionManager.getItem(getParameter());
                if (ami != null && ami.getName() != null) {
                    return ami.getName();
                } else {
                    if (getParameter() != null && getParameter().contains(",")) { //$NON-NLS-1$
                        StringTokenizer tokenizer = new StringTokenizer(getParameter(), ","); //$NON-NLS-1$
                        String sToken;
                        ArrayList<String> alStrings = new ArrayList<String>();
                        while (tokenizer.hasMoreTokens()) {
                            sToken = tokenizer.nextToken().trim();
                            ami = ActionManager.getItem(sToken);
                            if (ami != null && !alStrings.contains(ami.getName())) {
                                alStrings.add(ami.getName());
                            }
                        }
                        StringBuffer sBuffer = new StringBuffer();
                        if (alStrings.size() > 0) {
                            for (int i = 0; i < alStrings.size(); i++) {
                                if (i > 0) {
                                    sBuffer.append(", "); //$NON-NLS-1$
                                }
                                sBuffer.append(alStrings.get(i));
                            }
                        }
                        if (sBuffer.length() > 0) {
                            return sBuffer.toString();
                        } else {
                            return Messages.getString("Task.12"); //$NON-NLS-1$
                        }
                    } else {
                        return Messages.getString("Task.12"); //$NON-NLS-1$
                    }
                }
            default:
                return Messages.getString("Task.12"); //$NON-NLS-1$
        }
    }

    public String toStringState() {
        switch (state) {
            case STATE_CREATING_INIZONE:
                return Messages.getString("Task.13"); //$NON-NLS-1$
            case STATE_CREATING_ENDZONE:
                return Messages.getString("Task.14"); //$NON-NLS-1$
            case STATE_CREATING_SINGLEPOINT:
                return Messages.getString("Task.15"); //$NON-NLS-1$
            default:
                return Messages.getString("Task.16"); //$NON-NLS-1$
        }
    }

    public Point3D getPointIni() {
        return pointIni;
    }

    public void setPointIni(Point3D pointIni) {
        this.pointIni = pointIni;
    }

    public void setPointIni(Point3DShort pointIni) {
        if (pointIni == null) {
            this.pointIni = null;
        } else {
            this.pointIni = pointIni.toPoint3D();
        }
    }

    public Point3D getPointEnd() {
        return pointEnd;
    }

    public void setPointEnd(Point3D pointEnd) {
        this.pointEnd = pointEnd;
    }

    public void setPointEnd(Point3DShort pointEnd) {
        this.pointEnd = pointEnd.toPoint3D();
    }

    public void setPoint(Point3D point) {
        boolean bCheckShift = false;
        int iOldTask = getTask(); // Esto es para que el dig/mine con el shift no se jorobe
        if (state == STATE_CREATING_INIZONE) {
            setPointIni(point);
            setState(STATE_CREATING_ENDZONE);
        } else if (state == STATE_CREATING_ENDZONE) {
            if (MainPanel.tDMouseON) {
                setPointEnd(new Point3D(point.x, point.y, getPointIni().z));
            } else {
                setPointIni(new Point3D(getPointIni().x, getPointIni().y, point.z));
                setPointEnd(point);
            }

            // Buscamos los puntos donde ir a hacer la tarea
            setZoneHotPoints();
            setState(STATE_CREATED);

			// Controlamos que no haya acabado
            // Podría ser en el caso de marcar una zona no accesible para una tarea
            if (isFinished()) {
                Game.deleteCurrentTask();
            } else {
                Game.taskCreated();
            }
            bCheckShift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        } else if (state == STATE_CREATING_SINGLEPOINT) {
//			if (MainPanel.tDMouseON) {
            //int iZ3D = MainPanel.getMaxZ3DMouse (point.x, point.y, point.z);
            //setPointIni (MainPanel.get3DMouse (point.x, point.y, point.z));
//			} else {
//				setPointIni (point);
//			}
            setPointIni(point);

            // Buscamos los puntos donde ir a hacer la tarea
            setZoneHotPoints();
            setState(STATE_CREATED);

			// Controlamos que no haya acabado
            // Podría ser en el caso de marcar una zona no accesible para una tarea
            if (isFinished()) {
                Game.deleteCurrentTask();
            } else {
                Game.taskCreated();
            }
            bCheckShift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        }

        // Miramos si hay que crear otra tarea igual
        if (bCheckShift) {
            Game.createTask(iOldTask);
            Game.getCurrentTask().setParameter(getParameter());
            Game.getCurrentTask().setParameter2(getParameter2());
            Game.getCurrentTask().setTile(getTile(), getIconType());
        }
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;

        if (task == Task.TASK_QUEUE_AND_PLACE || task == Task.TASK_QUEUE_AND_PLACE_ROW || task == Task.TASK_QUEUE_AND_PLACE_AREA) {
            // Tarea queue and place, metemos en el parameter el item a crear (para el dibujado mientras lo coloca) y la queueID en el parameter2
            ActionManagerItem ami = ActionManager.getItem(getParameter());
            if (ami == null) {
                Log.log(Log.LEVEL_ERROR, Messages.getString("Task.34") + getParameter() + "]", getClass().toString()); //$NON-NLS-1$ //$NON-NLS-2$
                Game.deleteCurrentTask();
                return;
            }
            ArrayList<QueueItem> alQueue = ami.getQueue();
            String sItem = null;
            for (int i = alQueue.size() - 1; i >= 0; i--) {
                if (alQueue.get(i).getType() == QueueItem.TYPE_CREATE_ITEM || alQueue.get(i).getType() == QueueItem.TYPE_CREATE_ITEM_BY_TYPE) {
                    sItem = alQueue.get(i).getValue();
                    break;
                }
            }

            if (sItem != null) {
                setParameter2(sItem);
            } else {
                Log.log(Log.LEVEL_ERROR, Messages.getString("Task.36") + getParameter() + "]", getClass().toString()); //$NON-NLS-1$ //$NON-NLS-2$
                Game.deleteCurrentTask();
                return;
            }
        }
    }

    public String getParameter2() {
        return parameter2;
    }

    public void setParameter2(String parameter2) {
        this.parameter2 = parameter2;
    }

    public void setFace(int face) {
        this.face = face;
    }

    public int getFace() {
        return face;
    }

    private void checkCancelTask(ArrayList<TaskManagerItem> alTasks) {
        ArrayList<HotPoint> alPoints;
        Point3DShort p3dCancel, p3dTask;
        Task task;
        for (int x = 0; x < alTasks.size(); x++) {
            task = alTasks.get(x).getTask();
            if (task.getTask() == Task.TASK_MINE || task.getTask() == Task.TASK_MINE_LADDER) {
				// Tarea mine/dig
                // Miramos los puntos
                alPoints = task.getHotPoints();
                for (int p = 0; p < alPoints.size(); p++) {
                    if (!alPoints.get(p).isFinished()) {
                        p3dTask = alPoints.get(p).getHotPoint();

                        for (int t = 0; t < getHotPoints().size(); t++) {
                            p3dCancel = getHotPoints().get(t).getHotPoint();

                            if (p3dCancel.equals(p3dTask)) {
                                // Punto encontrado, lo marcamos como finished
                                Game.getWorld().getTaskManager().setHotPointFinished(task, p);
                                // Quitamos el flag de tarea de la celda
                                World.getCell(p3dCancel).setFlagOrders(false);
                                // El aldeano ya mirará si el hp está acabado y se quitará de la tarea
                            }
                        }
                    }
                }
            }
        }
    }

    private void checkCancelActions(ArrayList<Action> alActions, boolean checkCitizens) {
        Action action;
        ArrayList<Integer> alCitizens = World.getCitizenIDs();
        ArrayList<Integer> alSoldiers = World.getSoldierIDs();
        Citizen citizen;
        Point3DShort p3dCancel;
        for (int t = 0; t < getHotPoints().size(); t++) {
            p3dCancel = getHotPoints().get(t).getHotPoint();

            // Acciones en cola
            for (int x = (alActions.size() - 1); x >= 0; x--) {
                action = alActions.get(x);

                boolean bRemover = false;
                // Colas
                if (action.getDestinationPoint() != null && action.getDestinationPoint().equals(p3dCancel)) {
                    bRemover = true;
                } else {
                    if (action.getTerrainPoint() != null && action.getTerrainPoint().equals(p3dCancel)) {
                        bRemover = true;
                    } else {
                        // Create item desde item
                        if (action.getEntityID() != -1) {
                            Item item = Item.getItemByID(action.getEntityID());
                            if (item != null && item.getCoordinates().equals(p3dCancel)) {
                                bRemover = true;
                            } else {
                                // Livings
                                LivingEntity le = World.getLivingEntityByID(action.getEntityID());
                                if (le != null && le.getCoordinates().equals(p3dCancel)) {
                                    bRemover = true;
                                }
                            }
                        }
                    }
                }

                if (bRemover) {
                    Action actionRemoved = alActions.remove(x);
                    Game.getWorld().getTaskManager().removeFromProductionPanelRegular(actionRemoved.getId());
                    World.getCell(p3dCancel).setFlagOrders(false);
                }
            }

            if (checkCitizens) {
                // Acciones de aldeanos
                for (int x = 0; x < alCitizens.size(); x++) {
                    citizen = (Citizen) World.getLivingEntityByID(alCitizens.get(x));
                    if (citizen == null) {
                        continue;
                    }
                    action = citizen.getCurrentCustomAction();
                    if (action != null && citizen.getCurrentTask() != null) {
                        // Colas
                        if ((action.getDestinationPoint() != null && action.getDestinationPoint().equals(p3dCancel)) || (action.getTerrainPoint() != null && action.getTerrainPoint().equals(p3dCancel))) {
                            citizen.getCurrentTask().setFinished(true);
                            Game.getWorld().getTaskManager().removeCitizen(citizen);
                            World.getCell(p3dCancel).setFlagOrders(false);
                        }
                    }
                }
                // Acciones de soldiers... necesario?
                for (int x = 0; x < alSoldiers.size(); x++) {
                    citizen = (Citizen) World.getLivingEntityByID(alSoldiers.get(x));
                    if (citizen == null) {
                        continue;
                    }
                    action = citizen.getCurrentCustomAction();
                    if (action != null && citizen.getCurrentTask() != null) {
                        // Colas
                        if ((action.getDestinationPoint() != null && action.getDestinationPoint().equals(p3dCancel)) || (action.getTerrainPoint() != null && action.getTerrainPoint().equals(p3dCancel))) {
                            citizen.getCurrentTask().setFinished(true);
                            Game.getWorld().getTaskManager().removeCitizen(citizen);
                            World.getCell(p3dCancel).setFlagOrders(false);
                        }
                    }
                }
            }
        }
    }

    /**
     * Crea los hotpoints, también setea el maxCitizens para la tarea segun
     * estos
     */
    public void setZoneHotPoints() {
        // Buscamos en el mapa
        Cell[][][] cells = World.getCells();

        short x0 = (short) getPointIni().x;
        short y0 = (short) getPointIni().y;
        short x1, y1;
        boolean bXSwapped = false;
        boolean bYSwapped = false;
        if (getPointEnd() != null) {
            x1 = (short) getPointEnd().x;
            y1 = (short) getPointEnd().y;

            if (x0 > x1) {
                short aux = x0;
                x0 = x1;
                x1 = aux;
                bXSwapped = true;
            }
            if (y0 > y1) {
                short aux = y0;
                y0 = y1;
                y1 = aux;
                bYSwapped = true;
            }
        } else {
            // No es una zona, punto inicial y final los dejamos igual
            x1 = x0;
            y1 = y0;
        }
        short z = (short) getPointIni().z;

        Cell cell;
        if (getTask() == TASK_MINE || getTask() == TASK_DIG || getTask() == TASK_MINE_LADDER || getTask() == TASK_CUSTOM_ACTION || getTask() == TASK_CANCEL_ORDER) {
            ArrayList<ActionManagerItem> alAmis = new ArrayList<ActionManagerItem>(); // Con arrays por si se usa lo de 2 (o más) acciones en 1 mismo botón
            ArrayList<String> alParameters = new ArrayList<String>(); // Con arrays por si se usa lo de 2 (o más) acciones en 1 mismo botón
            if (getTask() == TASK_CUSTOM_ACTION) {
                String sParameter = getParameter();
                if (sParameter.contains(",")) { //$NON-NLS-1$
                    StringTokenizer tokenizer = new StringTokenizer(sParameter, ","); //$NON-NLS-1$
                    String sToken;
                    while (tokenizer.hasMoreTokens()) {
                        sToken = tokenizer.nextToken().trim();
                        alAmis.add(ActionManager.getItem(sToken));
                        alParameters.add(sToken);
                    }
                } else {
                    alAmis.add(ActionManager.getItem(sParameter));
                    alParameters.add(sParameter);
                }
            }

            // Recorremos todas las celdas de la órden (o órdenes)
            for (short x = x0; x <= x1; x++) {
                for (short y = y0; y <= y1; y++) {
                    cell = cells[x][y][getPointIni().z];

                    if (getTask() == Task.TASK_MINE || getTask() == Task.TASK_MINE_LADDER || getTask() == Task.TASK_DIG) {
						// MINE: Por cada celda minable añadimos su coordenada a los hotpoints, y alloweds adyacentes a las places
                        // DIG: Se crea tarea de mine de la celda de abajo
                        if (getTask() == Task.TASK_DIG) {
                            if (getPointIni().z < (World.MAP_DEPTH - 2)) {
                                cell = cells[x][y][getPointIni().z + 1];
                            } else {
                                continue;
                            }
                        } else if (getTask() == Task.TASK_MINE || getTask() == Task.TASK_MINE_LADDER) {
                            if (getPointIni().z >= (World.MAP_DEPTH - 1)) {
                                continue;
                            }
                        }

                        if (!cell.getTerrain().hasFluids() && (!cell.isMined()) || !cell.isDiscovered()) {
                            Point3DShort p3d = Point3DShort.getPoolInstance(x, y, cell.getCoordinates().z);
                            if (!existTaskPoint(getTask(), p3d)) {
                                // Miramos desde donde se puede acceder
                                ArrayList<Point3DShort> places = getAccesingPoints(x, y, cell.getCoordinates().z, getTask());

                                addHotPoint(new HotPoint(p3d, places));
                            }
                        }
                    } else if (getTask() == Task.TASK_CUSTOM_ACTION) {
                        // CUSTOM: Depende del tipo
                        ActionManagerItem ami;
                        String sParameter;
                        for (int a = 0; a < alAmis.size(); a++) {
                            ami = alAmis.get(a);
                            sParameter = alParameters.get(a);
                            Entity entity = cell.getEntity();
                            if (entity != null && entity instanceof Item && ItemManager.getItem(entity.getIniHeader()).getActions().contains(sParameter)) {
                                Action action = new Action(ami.getId());
                                action.setEntityID(entity.getID());
                                action.setQueue(ami.getQueue());
                                action.setQueueData(new QueueData());
                                action.setFace(MainPanel.itemBuildFace);
                                Game.getWorld().getTaskManager().addCustomAction(action, true);
                            } else {
                                // Living?
                                boolean bLiving = false;
                                ArrayList<LivingEntity> alLivings = cell.getLivings();
                                if (alLivings != null) {
                                    for (int i = 0; i < alLivings.size(); i++) {
                                        LivingEntity le = alLivings.get(i);
                                        if (LivingEntityManager.getItem(le.getIniHeader()).getActions().contains(sParameter)) {
                                            Action action = new Action(ami.getId());
                                            action.setEntityID(le.getID());
                                            action.setQueue(ami.getQueue());
                                            action.setQueueData(new QueueData());
                                            Game.getWorld().getTaskManager().addCustomAction(action, true);
                                            bLiving = true;
                                        }
                                    }
                                }

                                if (!bLiving) {
                                    // Acción de terrain?
                                    if (cell.isMined() && cell.getCoordinates().z < (World.MAP_DEPTH - 1)) {

                                        Cell cellUnder = World.getCell(x, y, getPointIni().z + 1);
                                        TerrainManagerItem tmi = TerrainManager.getItemByID(cellUnder.getTerrain().getTerrainID());
                                        if (tmi.getActions().contains(sParameter)) {
                                            // Celda posible para acción de terrain
                                            boolean bCasillaOcupada = cell.getTerrain().hasFluids() || cell.isFlagOrders();
                                            if (!bCasillaOcupada) {
                                                if (cell.hasItem()) {
                                                    Item item = (Item) cell.getEntity();
                                                    if (item != null && item.isOperative()) {
                                                        ItemManagerItem imi = ItemManager.getItem(item.getIniHeader());
                                                        if (imi.isWall()) {
                                                            bCasillaOcupada = true;
                                                        }
                                                    }
                                                }
                                                if (!bCasillaOcupada) {
                                                    if (cell.hasBuilding()) {
                                                        Building building = Building.getBuilding(cell.getCoordinates());
                                                        if (building != null) {
                                                            bCasillaOcupada = true;
                                                        }
                                                    }

                                                    if (!bCasillaOcupada) {
                                                        Point3DShort p3d = Point3DShort.getPoolInstance(x, y, z);
                                                        Action action = new Action(ami.getId());
                                                        action.setTerrainPoint(p3d);
                                                        action.setQueue(ami.getQueue());
                                                        action.setQueueData(new QueueData());
                                                        Game.getWorld().getTaskManager().addCustomAction(action, true);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    } else if (getTask() == Task.TASK_CANCEL_ORDER) {
                        if (cell.isFlagOrders()) {
                            Point3DShort p3d = Point3DShort.getPoolInstance(x, y, z);
                            addHotPoint(new HotPoint(p3d, p3d));
                        }
                    }
                }
            }

            if (getTask() == Task.TASK_CANCEL_ORDER) {
				// Miramos si existen tareas dig/mine/custom en el manager
                // En ese caso miramos si alguno de sus puntos se corresponde con alguno de la lista de puntos a cancelar
                checkCancelTask(Game.getWorld().getTaskManager().getTaskItems());
                checkCancelTask(Game.getWorld().getTaskManager().getTaskItemsTemp());

                // Ahora las custom actions
                checkCancelActions(Game.getWorld().getTaskManager().getCustomActions(), true);
                checkCancelActions(Game.getWorld().getTaskManager().getCustomActionsTemp(), false);
                checkCancelActions(Game.getWorld().getTaskManager().getCustomActionsWait(), false);

                setMaxCitizens(0);
                setFinished(true);
            } else {
                // Seteamos el flag de casilla con "Órdenes" en cada celda (se usa en el pintado)
                for (int x = 0; x < getHotPoints().size(); x++) {
                    World.getCell(getHotPoints().get(x).getHotPoint()).setFlagOrders(true);
                }

                setMaxCitizens(getHotPoints().size());
            }

        } else if (getTask() == TASK_MOVE_TO_CARAVAN) {
            addHotPoint(new HotPoint(getPointIni().toPoint3DShort(), getPointEnd().toPoint3DShort()));
        } else if (getTask() == TASK_FOOD_NEEDED) {
            addHotPoint(new HotPoint(getPointIni().toPoint3DShort(), getPointIni().toPoint3DShort()));
        } else if (getTask() == TASK_BUILD) {
            // BUILD
            cell = cells[x0][y0][z];

			// BUILD: Miramos si la celda es accesible en todas las casillas
            // Excepto las que no forman parte del edificio ya que ahora no tienen porque ser rectangulares
            BuildingManagerItem item = BuildingManager.getItem(getParameter());
            Building building = Building.createBuilding(item);
            if (building == null) {
                Log.log(Log.LEVEL_ERROR, Messages.getString("Task.17") + getParameter() + "]", getClass().getName()); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                boolean bAvailableForBuilding = true;
                break1:
                for (short x = x0; x < (x0 + item.getWidth()); x++) {
                    for (short y = y0; y < (y0 + item.getHeight()); y++) {
                        char groundDataChar = item.getGroundData().charAt((y - y0) * item.getWidth() + (x - x0));
                        if (groundDataChar == Building.GROUND_NON_BUILDING) {
                            continue;
                        }
                        if (!Building.isCellAvailableForBuilding(item, x, y, z)) {
                            bAvailableForBuilding = false;
                            break break1;
                        }
                    }
                }

                if (bAvailableForBuilding) {
                    // Metemos las celdas del edificio transitables y la entrance como places para construirse
                    Point3DShort p3d = null;
                    break2:
                    for (short x = x0; x < (x0 + item.getWidth()); x++) {
                        for (short y = y0; y < (y0 + item.getHeight()); y++) {
                            char groundDataChar = item.getGroundData().charAt((y - y0) * item.getWidth() + (x - x0));
                            if (groundDataChar == Building.GROUND_ENTRANCE) {
                                p3d = Point3DShort.getPoolInstance(x, y, z);
                                break break2;
                            }
                        }
                    }

                    if (p3d == null) {
                        // No debería pasar nunca
                        Log.log(Log.LEVEL_ERROR, Messages.getString("Task.31") + building.getIniHeader() + "]", getClass().toString()); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    addHotPoint(new HotPoint(p3d, p3d));

                    setMaxCitizens(1);

                    // Tarea de construcción, metemos el building donde toca y los aldeanos ya lo construirán y pondrán operativo
                    building.setCoordinates(x0, y0, z);

                    // Cargamos los prerequisitos
                    BuildingManagerItem bmi = BuildingManager.getItem(getParameter());
                    building.setPrerequisites(bmi.getPrerequisites());
                    building.setPrerequisitesLiving(bmi.getPrerequisitesFriendly());

                    // Si es un edificio automático le metemos en cola el primer item/living que pueda construir y le ponemos non-stop
                    if (bmi.isAutomatic()) {
                        boolean bSpawn = (bmi.getType() != null && bmi.getType().equalsIgnoreCase(Building.TYPE_SPAWN));

                        String itemName = null;
                        if (bSpawn) {
                            // SPAWNER
                            ArrayList<LivingEntityManagerItem> alItems = LivingEntityManager.getItemsByBuilding(bmi.getIniHeader());
                            if (alItems.size() > 0) {
                                itemName = alItems.get(0).getIniHeader();
                            }
                        } else {
                            // ITEMS
                            if (bmi.isMineTerrain()) {
                                // Pillamos un terreno al azar de los que tiene debajo, que tenga drop y pillamos su drop como primer item
                                ArrayList<String> alDrops = new ArrayList<String>();
                                if (z < (World.MAP_DEPTH - 1)) {
                                    TerrainManagerItem tmi;
                                    for (int x = x0; x < (x0 + item.getWidth()); x++) {
                                        for (int y = y0; y < (y0 + item.getHeight()); y++) {
                                            tmi = TerrainManager.getItemByID(World.getCell(x, y, z + 1).getTerrain().getTerrainID());
                                            if (tmi.getDrop() != null) {
                                                alDrops.add(tmi.getDrop());
                                            }
                                        }
                                    }
                                }

                                if (alDrops.size() == 0) {
                                    // No debería pasar
                                    Log.log(Log.LEVEL_ERROR, Messages.getString("Task.33"), getClass().toString()); //$NON-NLS-1$
                                } else {
                                    itemName = alDrops.get(Utils.getRandomBetween(0, alDrops.size() - 1));
                                }
                            } else {
                                ArrayList<ItemManagerItem> alItems = ItemManager.getItemsByBuilding(bmi.getIniHeader());
                                if (alItems.size() > 0) {
                                    itemName = alItems.get(0).getIniHeader();
                                }
                            }
                        }

                        // Tenemos item/living
                        if (itemName != null) {
                            building.setLastItem(itemName);
                            building.setNonStop(true);
                        }
                    }

                    // Lo añadimos
                    World.getCells()[x0][y0][z].setEntity(building);
                    World.getBuildings().add(building);

					// Activamos el flag de building a todas las casillas que formen parte del edificio
                    // Quitamos también el flag de stockpile y/o zona (si lo hubiera)
                    for (short x = x0; x < (x0 + item.getWidth()); x++) {
                        for (short y = y0; y < (y0 + item.getHeight()); y++) {
                            char groundDataChar = item.getGroundData().charAt((y - y0) * item.getWidth() + (x - x0));
                            if (groundDataChar == Building.GROUND_NON_BUILDING) {
                                continue;
                            }
                            World.getCells()[x][y][z].setBuildingCoordinates(Point3DShort.getPoolInstance(x0, y0, z));
                            Stockpile.deleteStockpilePoint(x, y, z);
                            Zone.deleteZonePoint(x, y, z);
                        }
                    }
                }
            }
        } else if (getTask() == TASK_STOCKPILE) {
            // STOCKPILE: Marcamos las celdas que toca como stockpile
            boolean bStockpileOK = false;
            Stockpile stockpile = new Stockpile(getParameter());
            for (short x = x0; x <= x1; x++) {
                for (short y = y0; y <= y1; y++) {
                    if (Stockpile.isCellAvailableForStockpile(x, y, (short) getPointIni().z)) {
                        stockpile.addPoint(Point3DShort.getPoolInstance(x, y, getPointIni().z));
                        bStockpileOK = true;
                    }
                }
            }

            if (bStockpileOK) {
                if (stockpile.getPoints() != null && stockpile.getPoints().size() > 0) {
                    // Deshabilitamos items si hace falta
                    if (Game.isDisabledItemsON()) {
                        stockpile.disableAll();
                    }

                    // Añadimos la stockpile al mundo
                    Game.getWorld().addStockPile(stockpile);

                    // Tutorial flow
                    Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_PILE, UtilsIniHeaders.getIntIniHeader (getParameter()), null);
                }
            }

            // No hay que hacer nada más, el Manager de tareas ya se encargará de crear/asignar tareas de haul
        } else if (getTask() == TASK_CREATE_ZONE) {
            // ZONE: Marcamos las celdas que toca como zone del tipo pasado
            Zone zone;
            ZoneManagerItem zmi = ZoneManager.getItem(getParameter());
            if (zmi != null) {

                if (zmi.getType() == ZoneManagerItem.TYPE_PERSONAL) {
                    zone = new ZonePersonal(getParameter());
                } else if (zmi.getType() == ZoneManagerItem.TYPE_HERO_ROOM) {
                    zone = new ZoneHeroRoom(getParameter());
                } else if (zmi.getType() == ZoneManagerItem.TYPE_BARRACKS) {
                    zone = new ZoneBarracks(getParameter());
                } else {
                    zone = new Zone(getParameter());
                }

                boolean bZoneOK = Zone.areCellsAvailableForZone(zmi, x0, y0, x1, y1, getPointIni().z, null);

                if (bZoneOK) {
                    // Metemos los puntos
                    for (short x = x0; x <= x1; x++) {
                        for (short y = y0; y <= y1; y++) {
                            if (!World.getCell(x, y, getPointIni().z).hasZone()) {
                                zone.getPoints().add(Point3DShort.getPoolInstance(x, y, getPointIni().z));
                            }
                        }
                    }

                    if (zone.getPoints() != null && zone.getPoints().size() > 0) {
                        // Zona personal, se la asignamos a alguien
                        if (zmi.getType() == ZoneManagerItem.TYPE_PERSONAL) {
                            Citizen cit;
                            boolean bAsignada = false;
                            for (int i = 0; i < World.getCitizenIDs().size(); i++) {
                                cit = (Citizen) World.getLivingEntityByID(World.getCitizenIDs().get(i));
                                if (!cit.getCitizenData().hasZone()) {
                                    // Aldeano sin zona personal, se la metemos
                                    cit.getCitizenData().setZoneID(zone.getID());
                                    ((ZonePersonal) zone).setOwnerID(cit.getID());
                                    bAsignada = true;
                                    break;
                                }
                            }
                            if (!bAsignada) {
                                // Soldiers
                                for (int i = 0; i < World.getSoldierIDs().size(); i++) {
                                    cit = (Citizen) World.getLivingEntityByID(World.getSoldierIDs().get(i));
                                    if (!cit.getCitizenData().hasZone()) {
                                        // Aldeano sin zona personal, se la metemos
                                        cit.getCitizenData().setZoneID(zone.getID());
                                        ((ZonePersonal) zone).setOwnerID(cit.getID());
                                        break;
                                    }
                                }
                            }
                        } else if (zmi.getType() == ZoneManagerItem.TYPE_HERO_ROOM) {
                            Hero hero;
                            for (int i = 0; i < World.getHeroIDs().size(); i++) {
                                hero = (Hero) World.getLivingEntityByID(World.getHeroIDs().get(i));
                                if (!hero.getCitizenData().hasZone()) {
                                    // Héroe sin zona personal, se la metemos (si es que le molan las free rooms)
                                    HeroPrerequisite hPrerequisite = HeroPrerequisite.getHeroPrerequisite(HeroManager.getStayPrerequisites(LivingEntityManager.getItem(hero.getIniHeader()).getHeroStayPrerequisite()), HeroPrerequisite.ID_FREE_ROOM);
                                    if (hPrerequisite != null && hPrerequisite.isValueBoolean()) {
                                        // Héroe necesita free room para stay
                                        hero.getCitizenData().setZoneID(zone.getID());
                                        ((ZoneHeroRoom) zone).setOwnerID(hero.getID());
                                        break;
                                    }
                                }
                            }
                        } else if (zmi.getType() == ZoneManagerItem.TYPE_BARRACKS) {
                            SoldierGroupData sgd;
                            for (int i = 0; i < SoldierGroups.MAX_GROUPS; i++) {
                                sgd = Game.getWorld().getSoldierGroups().getGroup(i);
                                if (!sgd.hasZone()) {
                                    // Grupo sin zona, se la metemos
                                    sgd.setZoneID(zone.getID());
                                    ((ZoneBarracks) zone).setGroupID(i);
                                    break;
                                }
                            }
                        }

                        // Añadimos la zone al mundo
                        Game.getWorld().addZone(zone, false);

                        // Tutorial flow
                        Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_ZONE, UtilsIniHeaders.getIntIniHeader (zone.getIniHeader ()), null);
                    }
                }
            }

            // No hay que hacer nada más
        } else if (getTask() == TASK_EXPAND_ZONE) {
            // EXPAND ZONE
            Zone zone = Zone.getZone(Integer.parseInt(getParameter()));
            if (zone != null) {
                ZoneManagerItem zmi = ZoneManager.getItem(zone.getIniHeader());
                boolean bZoneOK = Zone.areCellsAvailableForZone(zmi, x0, y0, x1, y1, getPointIni().z, zone);

                if (bZoneOK) {
                    // Metemos los puntos
                    Point3DShort p3dZonePoint;
                    Cell cellZone;
                    for (short x = x0; x <= x1; x++) {
                        for (short y = y0; y <= y1; y++) {
                            p3dZonePoint = Point3DShort.getPoolInstance(x, y, getPointIni().z);
                            cellZone = World.getCell(p3dZonePoint);
                            if (cellZone.getAstarZoneID() != -1 && !zone.getPoints().contains(p3dZonePoint)) {
                                zone.getPoints().add(p3dZonePoint);
                            }
                        }
                    }

                    if (zone.getPoints() != null && zone.getPoints().size() > 0) {
                        Game.getWorld().addZone(zone, true);
                    }
                }
            } else {
                Log.log(Log.LEVEL_ERROR, Messages.getString("Task.37"), getClass().toString()); //$NON-NLS-1$
            }

            // No hay que hacer nada más
        } else if (getTask() == TASK_CREATE_AND_PLACE || getTask() == TASK_QUEUE_AND_PLACE) {
            // CREATE: Miramos si en la casilla indicada se puede meter el item y, si no existe item, que tengamos un edificio
            cell = cells[x0][y0][z];

            ItemManagerItem imi;
            if (getTask() == TASK_QUEUE_AND_PLACE) {
                imi = ItemManager.getItem(getParameter2());
            } else {
                imi = ItemManager.getItem(getParameter());
            }
            if (imi == null) {
                Log.log(Log.LEVEL_ERROR, Messages.getString("Task.30") + getParameter() + "]", getClass().toString()); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                // Miramos si se puede poner el item en todas las casillas (sólo 1, he borrado el width/height)
                boolean bAvailableForBuilding = true;
                if (!Item.isCellAvailableForItem(imi, x0, y0, z, true, true)) {
                    bAvailableForBuilding = false;
                }
                if (bAvailableForBuilding) {
                    if (imi.canBeBuiltOnHoles()) {
                        // Miramos si hay algún ASZID distinto de -1
                        boolean bAllUnavailable = true;
                        foriteming:
                        for (short itemX = (short) (x0 - 1); itemX <= (x0 + 1); itemX++) {
                            for (short itemY = (short) (y0 - 1); itemY <= (y0 + 1); itemY++) {
                                for (short itemZ = (short) (z - 1); itemZ <= (z + 1); itemZ++) {
                                    if (Utils.isInsideMap(itemX, itemY, itemZ)) {
                                        if (World.getCell(itemX, itemY, itemZ).getAstarZoneID() != -1) {
                                            bAllUnavailable = false;
                                            break foriteming;
                                        }
                                    }
                                }
                            }
                        }
                        if (bAllUnavailable) {
                            // Arriba
                            if (z > 0) {
                                if (World.getCell(x0, y0, z - 1).getAstarZoneID() != -1) {
                                    bAllUnavailable = false;
                                }
                            }

                            if (bAllUnavailable) {
                                // Abajo
                                if (z < (World.MAP_DEPTH - 1)) {
                                    if (World.getCell(x0, y0, z + 1).getAstarZoneID() != -1) {
                                        bAllUnavailable = false;
                                    }
                                }
                            }
                        }
                        if (bAllUnavailable) {
                            bAvailableForBuilding = false;
                        }
                    }
                }

                boolean bItemEnElMundo = false;
                if (bAvailableForBuilding) {
                    // Miramos si en el mundo hay algun item NO-LOCKED de estos
                    int numItems = Item.getNumItems(UtilsIniHeaders.getIntIniHeader(imi.getIniHeader()), false, Game.getWorld ().getRestrictHaulEquippingLevel ());
                    if (numItems > 0) {
						// Hay items, miramos si alguno no está locked
                        //Integer[] aItems = World.getItems ().keySet ().toArray (new Integer [0]);
                        ArrayList<Integer> aItems = Item.getMapItems().get(imi.getNumericalIniHeader());
                        if (aItems != null) {
                            Item itemAux;
                            for (int i = 0; i < aItems.size(); i++) {
                                itemAux = Item.getItemByID(aItems.get(i), true);
                                if (itemAux != null && itemAux.getNumericIniHeader() == imi.getNumericalIniHeader()) {
                                    // Item del tipo deseado, miramos si no está locked y en la misma zona A* que el destino
                                    if (!itemAux.isLocked()) {
                                        // Item NO locked, miramos que no haya otro aldeano que lo vaya a usar
                                        if (!itemInUse(itemAux)) {
                                            int iItemASZID = World.getCell(itemAux.getCoordinates()).getAstarZoneID();
                                            int iItemASZIDDestination = World.getCell(x0, y0, z).getAstarZoneID();
                                            if (iItemASZID != -1 && (iItemASZID == iItemASZIDDestination || iItemASZIDDestination == -1)) {
												// Caso especial, ladders, estos van en una casilla digada, entonces el A*ZI será distinto
                                                // Hay que mirar si las casillas adyacentes (desde donde se colocará) son accesibles
                                                if (ItemManager.getItem(itemAux.getIniHeader()).canBeBuiltOnHoles()) {
                                                    ArrayList<Point3DShort> alPoints = getAccesingPointsMatchingASZI(x0, y0, z, iItemASZID, getTask());
                                                    if (alPoints.size() > 0) {
														// De guais, tenemos casillas accesibles en la misma zona que el item
                                                        // Creamos una tarea de MOVE (como las haul pero persistentes)
                                                        bItemEnElMundo = true;
                                                        Task task = new Task(TASK_MOVE_AND_LOCK);
                                                        task.setPointIni(itemAux.getCoordinates().toPoint3D());

                                                        task.setPointEnd(new Point3D(x0, y0, z));

                                                        // Seteamos el flag de casilla con "Órdenes" en cada celda (se usa en el pintado)
                                                        World.getCell(x0, y0, z).setFlagOrders(true);

                                                        task.setParameter(itemAux.getIniHeader());
                                                        task.setFace(MainPanel.itemBuildFace);
                                                        HotPoint hp = new HotPoint(itemAux.getCoordinates(), alPoints);
                                                        task.addHotPoint(hp);
                                                        Game.getWorld().getTaskManager().addTask(task);
                                                        break;
                                                    }
                                                } else {
                                                    // Item encontrado !!! Creamos una tarea de MOVE (como las haul pero persistentes)
                                                    bItemEnElMundo = true;
                                                    Task task = new Task(TASK_MOVE_AND_LOCK);
                                                    task.setPointIni(itemAux.getCoordinates().toPoint3D());
                                                    task.setPointEnd(new Point3D(x0, y0, z));

                                                    // Seteamos el flag de casilla con "Órdenes" en cada celda (se usa en el pintado)
                                                    World.getCell(x0, y0, z).setFlagOrders(true);

                                                    task.setParameter(itemAux.getIniHeader());
                                                    task.setFace(MainPanel.itemBuildFace);
                                                    HotPoint hp = new HotPoint(itemAux.getCoordinates(), itemAux.getCoordinates());
                                                    task.addHotPoint(hp);
                                                    Game.getWorld().getTaskManager().addTask(task);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Si llega aquí es que no hemos encontrado item en el mundo, tendremos que construirlo
                if (!bItemEnElMundo) {
                    if (bAvailableForBuilding) {
                        // Aquí distinguimos entre tarea QUEUE o tarea normal de toda la vida
                        if (getTask() == TASK_QUEUE_AND_PLACE) {
                            // QUEUE
                            Action action = new Action(getParameter());
                            action.setDestinationPoint(Point3DShort.getPoolInstance(x0, y0, z));
                            action.setQueue(ActionManager.getItem(getParameter()).getQueue());
                            action.setQueueData(new QueueData());
                            action.setFace(MainPanel.itemBuildFace);
                            Game.getWorld().getTaskManager().addCustomAction(action, true, false);
                        } else {
                            // Miramos si tenemos el edificio, el más cercano (y con la cola de items más pequeña) a donde se deja el item
                            ArrayList<Building> buildings = World.getBuildings();
                            Building building;
                            int iBuildingCercano = -1;
                            int iBuildingCercanoSinCola = -1;
                            Point3DShort p3dBuildingAux = null;
                            int iDistance = Utils.MAX_DISTANCE;
                            int iDistanceSinCola = Utils.MAX_DISTANCE;

                            // Buscamos la cola de items más pequeña
                            int iMinCola = 1000;
                            for (int i = 0; i < buildings.size(); i++) {
                                building = buildings.get(i);
                                if (building.isOperative() && building.getIniHeader().equals(imi.getBuilding())) {
                                    if (building.getItemQueue().size() < iMinCola) {
                                        iMinCola = building.getItemQueue().size();
                                    }
                                }
                            }

                            // Buscamos el edicifio más cercano que tenga la cola mínima obtenida arriba
                            for (int i = 0; i < buildings.size(); i++) {
                                building = buildings.get(i);
                                if (building.isOperative() && building.getIniHeader().equals(imi.getBuilding())) {
                                    if (building.getItemQueue().size() != iMinCola) {
                                        continue;
                                    }

                                    // Buscamos la entrada del edificio
                                    BuildingManagerItem bmi = BuildingManager.getItem(building.getIniHeader());
                                    p3dBuildingAux = bmi.getEntranceBaseCoordinates().merge(building.getCoordinates());

									// Caso especial, puentes y ladders (o items que se construyen desde una casilla anterior a destino)
                                    // Miramos accesingpoints ya que se construye desde casillas adyacentes
                                    if (ItemManager.getItem(imi.getIniHeader()).canBeBuiltOnHoles()) {
                                        int iBuildingASZID = World.getCell(p3dBuildingAux).getAstarZoneID();
                                        ArrayList<Point3DShort> alPoints = getAccesingPointsMatchingASZI(x0, y0, z, iBuildingASZID, getTask());
                                        if (alPoints.size() > 0) {
                                            // Edificio de guais y en la zona del item, miramos la distancia
                                            int iDistanceAux = Utils.getDistance(x0, y0, z, p3dBuildingAux);
                                            if (iDistanceAux < iDistance) {
                                                iDistance = iDistanceAux;
                                                iBuildingCercano = i;
                                            }
                                            if (!building.hasItemsInQueue()) {
                                                if (iDistanceAux < iDistanceSinCola) {
                                                    iDistanceSinCola = iDistanceAux;
                                                    iBuildingCercanoSinCola = i;
                                                }
                                            }
                                        }
                                    } else {
                                        if (World.getCell(p3dBuildingAux).getAstarZoneID() == World.getCell(x0, y0, z).getAstarZoneID()) {
                                            // Edificio de guais y en la zona, miramos la distancia
                                            int iDistanceAux = Utils.getDistance(x0, y0, z, p3dBuildingAux);
                                            if (iDistanceAux < iDistance) {
                                                iDistance = iDistanceAux;
                                                iBuildingCercano = i;
                                            }
                                            if (!building.hasItemsInQueue()) {
                                                if (iDistanceAux < iDistanceSinCola) {
                                                    iDistanceSinCola = iDistanceAux;
                                                    iBuildingCercanoSinCola = i;
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (iBuildingCercanoSinCola != -1) {
                                iBuildingCercano = iBuildingCercanoSinCola;
                            }

                            if (iBuildingCercano != -1) {
                                // Tenemos el edificio más cercano, lo pillamos y le añadimos el item en su cola
                                Item item = Item.createItem(imi);
                                item.setCoordinates(x0, y0, z); // Destino del item cuando se construya

                                // Seteamos el flag de casilla con "Órdenes" en cada celda (se usa en el pintado)
                                World.getCell(x0, y0, z).setFlagOrders(true);

                                // Cargamos los prerequisitos
                                item.setPrerequisites(ItemManager.getItem(getParameter()).getPrerequisites());

                                // Lo añadimos al edificio
                                buildings.get(iBuildingCercano).addItem(item);
                            } else {
                                if (imi.getBuilding() != null) {
                                    MessagesPanel.addMessage(MessagesPanel.TYPE_ANNOUNCEMENT, Messages.getString("Task.21") + imi.getName() + Messages.getString("Task.22") + BuildingManager.getItem(imi.getBuilding()).getName() + "]", ColorGL.ORANGE); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                } else {
                                    MessagesPanel.addMessage(MessagesPanel.TYPE_ANNOUNCEMENT, Messages.getString("Task.19") + imi.getName() + Messages.getString("Task.32"), ColorGL.ORANGE); //$NON-NLS-1$ //$NON-NLS-2$
                                }
                            }
                        }
                    }
                }

            }
        } else if (getTask() == TASK_CREATE_AND_PLACE_ROW) {
			// Por cada punto de la fila (row) crearemos una tarea de create_and_place
            // Miramos si la fila es horizontal o vertical
            boolean bHorizontal = (x1 - x0) >= (y1 - y0);

            boolean bToggle3D = false;
            if (MainPanel.tDMouseON) {
                MainPanel.toggle3DMouse();
                bToggle3D = true;
            }

            if (bHorizontal) {
                for (int i = x0; i <= x1; i++) {
                    // Creamos tarea de create_and_place
                    Task taskTmp = new Task(Task.TASK_CREATE_AND_PLACE);
                    taskTmp.setParameter(getParameter());
                    taskTmp.setPoint(new Point3D(i, (bYSwapped) ? y1 : y0, z));
                }
            } else {
                for (int i = y0; i <= y1; i++) {
                    // Creamos tarea de create_and_place
                    Task taskTmp = new Task(Task.TASK_CREATE_AND_PLACE);
                    taskTmp.setParameter(getParameter());
                    taskTmp.setPoint(new Point3D((bXSwapped) ? x1 : x0, i, z));
                }
            }

            if (bToggle3D) {
                MainPanel.toggle3DMouse();
            }
        } else if (getTask() == TASK_QUEUE_AND_PLACE_ROW) {
			// Por cada punto de la fila (row) crearemos una tarea de queue_and_place
            // Miramos si la fila es horizontal o vertical
            boolean bHorizontal = (x1 - x0) >= (y1 - y0);

            boolean bToggle3D = false;
            if (MainPanel.tDMouseON) {
                MainPanel.toggle3DMouse();
                bToggle3D = true;
            }

            if (bHorizontal) {
                for (int i = x0; i <= x1; i++) {
                    // Creamos tarea de queue_and_place
                    Task taskTmp = new Task(Task.TASK_QUEUE_AND_PLACE);
                    taskTmp.setParameter(getParameter());
                    taskTmp.setPoint(new Point3D(i, (bYSwapped) ? y1 : y0, z));
                }
            } else {
                for (int i = y0; i <= y1; i++) {
                    // Creamos tarea de create_and_place
                    Task taskTmp = new Task(Task.TASK_QUEUE_AND_PLACE);
                    taskTmp.setParameter(getParameter());
                    taskTmp.setPoint(new Point3D((bXSwapped) ? x1 : x0, i, z));
                }
            }

            if (bToggle3D) {
                MainPanel.toggle3DMouse();
            }
        } else if (getTask() == TASK_QUEUE_AND_PLACE_AREA) {
			// Por cada punto de la fila (row) y columna (col) crearemos una tarea de queue_and_place

            boolean bToggle3D = false;
            if (MainPanel.tDMouseON) {
                MainPanel.toggle3DMouse();
                bToggle3D = true;
            }

            for (int i = x0; i <= x1; i++) {
                for (int j = y0; j <= y1; j++) {
                    // Creamos tarea de queue_and_place
                    Task taskTmp = new Task(Task.TASK_QUEUE_AND_PLACE);
                    taskTmp.setParameter(getParameter());
                    taskTmp.setPoint(new Point3D(i, j, z));
                }
            }

            if (bToggle3D) {
                MainPanel.toggle3DMouse();
            }
        }

        // Si no hay hotpoints es que la tarea ya está terminada
        if (getHotPoints().size() == 0) {
            // Tarea finalizada
            setFinished(true);
        }
    }

    private void addHotPoint(HotPoint hotPoint) {
        if (hotPoints == null) {
            hotPoints = new ArrayList<HotPoint>();
        }
        hotPoints.add(hotPoint);
    }

    public int getMaxCitizens() {
        return maxCitizens;
    }

    /**
     * Setea el máximo de aldeanos para la tarea. Excepciones: Si es tarea de
     * construcción sólo permite 1 aldeano
     *
     * @param maxCitizens Máximo de aldeanos
     */
    public void setMaxCitizens(int maxCitizens) {
        if (maxCitizens > 1 && getTask() == TASK_BUILD) {
            this.maxCitizens = 1;
        } else {
            this.maxCitizens = maxCitizens;
        }
    }

    /**
     * Indica si el item pasado está en uso. Vamos, que otro aldeano va a por él
     *
     * @param item
     * @return true si el item pasado está en uso
     */
    private boolean itemInUse(Item item) {
        Task task;
        for (int i = 0; i < World.getCitizenIDs().size(); i++) {
            task = ((Citizen) World.getLivingEntityByID(World.getCitizenIDs().get(i))).getCurrentTask();
            if (task != null) {
                // Aldeano con tarea, veamos si implica el uso del item pasado
                if (task.getTask() == Task.TASK_HAUL || task.getTask() == Task.TASK_MOVE_AND_LOCK || task.getTask() == Task.TASK_PUT_IN_CONTAINER) {
                    if (task.getPointIni().equals(item.getCoordinates())) {
                        return true;
                    }
                }
            }
        }
        for (int i = 0; i < World.getSoldierIDs().size(); i++) {
            task = ((Citizen) World.getLivingEntityByID(World.getSoldierIDs().get(i))).getCurrentTask();
            if (task != null) {
                // Aldeano con tarea, veamos si implica el uso del item pasado
                if (task.getTask() == Task.TASK_HAUL || task.getTask() == Task.TASK_MOVE_AND_LOCK || task.getTask() == Task.TASK_PUT_IN_CONTAINER) {
                    if (task.getPointIni().equals(item.getCoordinates())) {
                        return true;
                    }
                }
            }
        }

        // Si llega aquí es que ningún aldeano tiene tarea con ese item, vamos a mirar que no esté pendiente en el taskManager
        ArrayList<TaskManagerItem> alTasks = Game.getWorld().getTaskManager().getTaskItems();
        for (int i = 0; i < alTasks.size(); i++) {
            task = alTasks.get(i).getTask();
            if (task.getTask() == Task.TASK_HAUL || task.getTask() == Task.TASK_MOVE_AND_LOCK || task.getTask() == Task.TASK_PUT_IN_CONTAINER) {
                if (task.getPointIni().equals(item.getCoordinates())) {
                    return true;
                }
            }
        }

        alTasks = Game.getWorld().getTaskManager().getTaskItemsTemp();
        for (int i = 0; i < alTasks.size(); i++) {
            task = alTasks.get(i).getTask();
            if (task.getTask() == Task.TASK_HAUL || task.getTask() == Task.TASK_MOVE_AND_LOCK || task.getTask() == Task.TASK_PUT_IN_CONTAINER) {
                if (task.getPointIni().equals(item.getCoordinates())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Indica si, para un punto de una tarea ya existe otra tarea con el mismo
     * punto y el mismo NO está finished Se usa para DIG/CHOP/MINE/CUSTOM
     * solamente
     *
     * @param taskID
     * @param p3d
     * @return true si para un punto de una tarea ya existe otra tarea con el
     * mismo punto
     */
    private boolean existTaskPoint(int taskID, Point3DShort p3d) {
        ArrayList<TaskManagerItem> alTasks = Game.getWorld().getTaskManager().getTaskItems();
        ArrayList<HotPoint> alHPs;
        for (int i = 0; i < alTasks.size(); i++) {
            if (alTasks.get(i).getTask().getTask() == taskID) {
                alHPs = alTasks.get(i).getTask().getHotPoints();
                for (int j = 0; j < alHPs.size(); j++) {
                    if (alHPs.get(j).getHotPoint().equals(p3d) && !alHPs.get(j).isFinished()) {
                        return true;
                    }
                }
            }
        }

        return false;
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

    /**
     * Devuelve el punto indicado ir para realizar la tarea
     *
     * @return el punto indicado ir para realizar la tarea
     */
    public HotPoint getHotPoint(int hotPointIndex) {
        return getHotPoints().get(hotPointIndex);
    }

    /**
     * Devuelve una lista de puntos adyacentes accesibles
     *
     * @param x
     * @param y
     * @param z
     * @param task
     * @return una lista de puntos adyacentes accesibles
     */
    public static ArrayList<Point3DShort> getAccesingPoints(int x, int y, int z, int task) {
        ArrayList<Point3DShort> places = new ArrayList<Point3DShort>();
        if (x > 0) {
            if (y > 0) {
                places.add(Point3DShort.getPoolInstance(x - 1, y - 1, z));
            }
            places.add(Point3DShort.getPoolInstance(x - 1, y, z));
            if (y < (World.MAP_HEIGHT - 1)) {
                places.add(Point3DShort.getPoolInstance(x - 1, y + 1, z));
            }
        }
        if (x < (World.MAP_WIDTH - 1)) {
            if (y > 0) {
                places.add(Point3DShort.getPoolInstance(x + 1, y - 1, z));
            }
            places.add(Point3DShort.getPoolInstance(x + 1, y, z));
            if (y < (World.MAP_HEIGHT - 1)) {
                places.add(Point3DShort.getPoolInstance(x + 1, y + 1, z));
            }
        }
        if (y > 0) {
            places.add(Point3DShort.getPoolInstance(x, y - 1, z));
        }
        if (y < (World.MAP_HEIGHT - 1)) {
            places.add(Point3DShort.getPoolInstance(x, y + 1, z));
        }

        // Abajo
        if (z < (World.MAP_DEPTH - 1)) {
            // Minar desde abajo
            places.add(Point3DShort.getPoolInstance(x, y, z + 1));
        }

        // Celdas vecinas de arriba (sólo si la central está minada)
        if (z > 0 && World.getCell(x, y, z - 1).isMined()) {
            // Oeste
            if (x > 0) {
                if (y > 0) {
                    places.add(Point3DShort.getPoolInstance(x - 1, y - 1, z - 1));
                }
                places.add(Point3DShort.getPoolInstance(x - 1, y, z - 1));
                if (y < (World.MAP_HEIGHT - 1)) {
                    places.add(Point3DShort.getPoolInstance(x - 1, y + 1, z - 1));
                }
            }
            // Este
            if (x < (World.MAP_WIDTH - 1)) {
                if (y > 0) {
                    places.add(Point3DShort.getPoolInstance(x + 1, y - 1, z - 1));
                }
                places.add(Point3DShort.getPoolInstance(x + 1, y, z - 1));
                if (y < (World.MAP_HEIGHT - 1)) {
                    places.add(Point3DShort.getPoolInstance(x + 1, y + 1, z - 1));
                }
            }
            // Norte
            if (y > 0) {
                places.add(Point3DShort.getPoolInstance(x, y - 1, z - 1));
            }
            // Sur
            if (y < (World.MAP_HEIGHT - 1)) {
                places.add(Point3DShort.getPoolInstance(x, y + 1, z - 1));
            }
        }

        // Celdas vecinas de abajo (sólo si la central está minada)
        if (z < (World.MAP_DEPTH - 1) && World.getCell(x, y, z + 1).isMined()) {
            // Oeste
            if (x > 0) {
                if (y > 0) {
                    places.add(Point3DShort.getPoolInstance(x - 1, y - 1, z + 1));
                }
                places.add(Point3DShort.getPoolInstance(x - 1, y, z + 1));
                if (y < (World.MAP_HEIGHT - 1)) {
                    places.add(Point3DShort.getPoolInstance(x - 1, y + 1, z + 1));
                }
            }
            // Este
            if (x < (World.MAP_WIDTH - 1)) {
                if (y > 0) {
                    places.add(Point3DShort.getPoolInstance(x + 1, y - 1, z + 1));
                }
                places.add(Point3DShort.getPoolInstance(x + 1, y, z + 1));
                if (y < (World.MAP_HEIGHT - 1)) {
                    places.add(Point3DShort.getPoolInstance(x + 1, y + 1, z + 1));
                }
            }
            // Norte
            if (y > 0) {
                places.add(Point3DShort.getPoolInstance(x, y - 1, z + 1));
            }
            // Sur
            if (y < (World.MAP_HEIGHT - 1)) {
                places.add(Point3DShort.getPoolInstance(x, y + 1, z + 1));
            }
        }

		// Justo arriba (la pongo al final para que sea la última opción de los aldeanos en caso de mine)
        //if (task != TASK_MINE && task != TASK_MINE_LADDER && task != TASK_DIG) {
        if (z > 0) {
            places.add(Point3DShort.getPoolInstance(x, y, z - 1));
        }
        //}

        return places;
    }

    /**
     * Devuelve una lista de puntos adyacentes accesibles que estén en la zona
     * pasada
     *
     * @param p3d
     * @para aszi
     * @para task
     * @return una lista de puntos adyacentes accesibles que estén en la zona
     * pasada
     */
    public static ArrayList<Point3DShort> getAccesingPointsMatchingASZI(Point3DShort p3d, int aszi, int task) {
        return getAccesingPointsMatchingASZI(p3d.x, p3d.y, p3d.z, aszi, task);
    }

    public static ArrayList<Point3DShort> getAccesingPointsMatchingASZI(Point3D p3d, int aszi, int task) {
        return getAccesingPointsMatchingASZI(p3d.x, p3d.y, p3d.z, aszi, task);
    }

    /**
     * Devuelve una lista de puntos adyacentes accesibles que estén en la zona
     * pasada
     *
     * @param x
     * @param y
     * @param z
     * @para aszi
     * @return una lista de puntos adyacentes accesibles que estén en la zona
     * pasada
     */
    public static ArrayList<Point3DShort> getAccesingPointsMatchingASZI(int x, int y, int z, int aszi, int task) {
        ArrayList<Point3DShort> places = getAccesingPoints(x, y, z, task);
        ArrayList<Point3DShort> placesOK = new ArrayList<Point3DShort>();

        for (int i = 0; i < places.size(); i++) {
            if (World.getCell(places.get(i)).getAstarZoneID() == aszi) {
                placesOK.add(places.get(i));
            }
        }

        return placesOK;
    }

    public ArrayList<HotPoint> getHotPoints() {
        if (hotPoints == null) {
            hotPoints = new ArrayList<HotPoint>();
        }
        return hotPoints;
    }

    public void setHotPoints(ArrayList<HotPoint> hotPoints) {
        this.hotPoints = hotPoints;
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
