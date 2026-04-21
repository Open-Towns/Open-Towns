package xaos.tasks;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import xaos.actions.Action;
import xaos.actions.ActionManager;
import xaos.actions.ActionManagerItem;
import xaos.actions.ActionPriorityManager;
import xaos.actions.QueueData;
import xaos.actions.QueueItem;
import xaos.campaign.TutorialTrigger;
import xaos.data.CitizenGroupData;
import xaos.data.FocusData;
import xaos.data.SoldierData;
import xaos.data.SoldierGroupData;
import xaos.data.SoldierGroups;
import xaos.main.Game;
import xaos.main.World;
import xaos.panels.MessagesPanel;
import xaos.stockpiles.Stockpile;
import xaos.tiles.Cell;
import xaos.tiles.entities.buildings.Building;
import xaos.tiles.entities.buildings.BuildingManager;
import xaos.tiles.entities.buildings.BuildingManagerItem;
import xaos.tiles.entities.items.Container;
import xaos.tiles.entities.items.Item;
import xaos.tiles.entities.items.ItemManager;
import xaos.tiles.entities.items.ItemManagerItem;
import xaos.tiles.entities.living.Citizen;
import xaos.tiles.entities.living.LivingEntity;
import xaos.tiles.entities.living.LivingEntityManager;
import xaos.tiles.entities.living.LivingEntityManagerItem;
import xaos.tiles.terrain.Terrain;
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
import xaos.zones.ZonePersonal;

/**
 * Se usa para relacionar aldeanos ocn tareas
 */
public final class TaskManager implements Externalizable {

    private static final long serialVersionUID = -1530236959590878316L;

    public static int MAX_CONTAINER_HAUL_PER_TURN = 64; // Número de containers a chequear, para ver si hay items maols dentro
    public static int MAX_HAUL_PER_TURN = 64; // Número de items a chequear cada turno

    private static int MAX_CUSTOM_ACTIONS_PER_TURN_AND_PRIORITY_LEVEL = 8;

    private static int TASK_TYPE_OTHERS = -1;
    private static int TASK_TYPE_MINEDIG = 0;
    private static int TASK_TYPE_MOVE_TO_CARAVAN = 1;
    private static int TASK_TYPE_FEED_ANIMALS = 3;
    private static int TASK_TYPE_BUILD_BUILDING = 4;

    private ArrayList<TaskManagerItem> taskItems;
    private ArrayList<TaskManagerItem> taskItemsTemp; // Se usa este array para meter las tareas y que no se coloquen asíncronamente en el "bueno"
    private ArrayList<Action> customActions;
    private ArrayList<Action> customActionsTemp; // Se usa este array para meter las actions y que no se coloquen asíncronamente en el "bueno"
    private ArrayList<Action> customActionsWait; // Se usa este array cuando se saca una action que no se puede hacer pero no hay que eliminarla (performance)
    private int haulIndex;
    private int containerIndex;
    private boolean reCheckMinePlaces;

    private HashMap<String, Integer> hmItemsOnRegularQueue; // Se usa para que el production panel sepa cuantos items hay en cola
    private HashMap<String, Integer> hmItemsOnAutomatedQueue; // Se usa para que el production panel sepa cuantos items hay automated mode
    private int automatedQueueTurns;
    private ArrayList<String> alProductionToRemove;

    private HashMap<String, ArrayList<Integer>> hmNonAvailableActions = new HashMap<String, ArrayList<Integer>>();

    public TaskManager() {
        taskItems = new ArrayList<TaskManagerItem>();
        taskItemsTemp = new ArrayList<TaskManagerItem>();
        customActions = new ArrayList<Action>();
        customActionsTemp = new ArrayList<Action>();
        customActionsWait = new ArrayList<Action>();
        hmItemsOnRegularQueue = new HashMap<String, Integer>();
        hmItemsOnAutomatedQueue = new HashMap<String, Integer>();
        alProductionToRemove = new ArrayList<String>();
        automatedQueueTurns = 0;
        haulIndex = 0;
        containerIndex = 0;
        setReCheckMinePlaces(false);
    }

    public ArrayList<TaskManagerItem> getTaskItems() {
        return taskItems;
    }

    public ArrayList<TaskManagerItem> getTaskItemsTemp() {
        return taskItemsTemp;
    }

    public void addTask(Task task) {
        synchronized (taskItemsTemp) {
            taskItemsTemp.add(new TaskManagerItem(task));
        }
    }

    public void setTaskItems(ArrayList<TaskManagerItem> taskItems) {
        this.taskItems = taskItems;
    }

    public void setCustomActions(ArrayList<Action> customActions) {
        this.customActions = customActions;
    }

    public ArrayList<Action> getCustomActions() {
        return customActions;
    }

    public ArrayList<Action> getCustomActionsTemp() {
        return customActionsTemp;
    }

    public ArrayList<Action> getCustomActionsWait() {
        return customActionsWait;
    }

    /**
     * Añade una custom action, controla que no esté metida, para no repetirla
     *
     * @param action
     * @param checkDuplicate
     */
    public void addCustomAction(Action newAction, boolean checkDuplicate) {
        addCustomAction(newAction, checkDuplicate, true);
    }

    public void addCustomAction(Action newAction, boolean checkDuplicate, boolean putOnProdPanel) {
        if (newAction == null) {
            return;
        }

        boolean added = false;
        synchronized (customActionsTemp) {
            if (checkDuplicate) {
                if (!isDuplicateAction(newAction)) {
                    customActionsTemp.add(newAction);
                    added = true;
                }
            } else {
                customActionsTemp.add(newAction);
                added = true;
            }
        }

        if (added) {
            // Flag orders en la celda
            ActionManagerItem ami = ActionManager.getItem(newAction.getId());
            if (newAction.getDestinationPoint() != null) {
                World.getCell(newAction.getDestinationPoint()).setFlagOrders(true);
            } else {
                Item item = Item.getItemByID(newAction.getEntityID());
                if (item != null) {
                    World.getCell(item.getCoordinates()).setFlagOrders(true);
                } else {
                    // Terrain?
                    if (newAction.getTerrainPoint() != null) {
                        World.getCell(newAction.getTerrainPoint()).setFlagOrders(true);
                    }
                }
            }

            if (putOnProdPanel) {
                // PRODUCTION PANEL INFO
                if (hmItemsOnRegularQueue.containsKey(ami.getId())) {
                    Integer iValue = hmItemsOnRegularQueue.get(ami.getId());
                    if (iValue != null) {
                        hmItemsOnRegularQueue.put(ami.getId(), Integer.valueOf(iValue.intValue() + 1));
                    } else {
                        hmItemsOnRegularQueue.put(ami.getId(), Integer.valueOf(1));
                    }
                } else {
                    hmItemsOnRegularQueue.put(ami.getId(), Integer.valueOf(1));
                }
            }
        }
    }

    /**
     * Indica si la acción ya existe en la lista de acciones
     *
     * @param newAction
     * @return
     */
    public boolean isDuplicateAction(Action newAction) {
        Action action;
        // Acciones en cola
        for (int i = 0; i < customActions.size(); i++) {
            action = customActions.get(i);
            if (isDuplicateAction(newAction, action)) {
                return true;
            }
        }
        for (int i = 0; i < customActionsTemp.size(); i++) {
            action = customActionsTemp.get(i);
            if (isDuplicateAction(newAction, action)) {
                return true;
            }
        }
        for (int i = 0; i < customActionsWait.size(); i++) {
            action = customActionsWait.get(i);
            if (isDuplicateAction(newAction, action)) {
                return true;
            }
        }

        // Acciones de aldeanos
        ArrayList<Integer> citizens = World.getCitizenIDs();
        Citizen citizen;
        for (int i = 0; i < citizens.size(); i++) {
            citizen = (Citizen) World.getLivingEntityByID(citizens.get(i));
            action = citizen.getCurrentCustomAction();
            if (action != null && isDuplicateAction(newAction, action)) {
                return true;
            }
        }
        // Y de soldiers (hace falta?)
        citizens = World.getSoldierIDs();
        for (int i = 0; i < citizens.size(); i++) {
            citizen = (Citizen) World.getLivingEntityByID(citizens.get(i));
            action = citizen.getCurrentCustomAction();
            if (action != null && isDuplicateAction(newAction, action)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Indica cuantas actions "iguales" hay en curso No se miran duplicados
     * reales, sólo que exista una action con mismo ID
     *
     * @param newAction
     * @return cuantas actions "iguales" hay en curso
     */
    private int getNumDuplicateActionAutomated(Action newAction) {
        int iNum = 0;

        Action action;
        // Acciones en cola
        for (int i = 0; i < customActions.size(); i++) {
            action = customActions.get(i);
            if (isDuplicateActionAutomated(newAction, action)) {
                iNum++;
            }
        }
        for (int i = 0; i < customActionsTemp.size(); i++) {
            action = customActionsTemp.get(i);
            if (isDuplicateActionAutomated(newAction, action)) {
                iNum++;
            }
        }
        for (int i = 0; i < customActionsWait.size(); i++) {
            action = customActionsWait.get(i);
            if (isDuplicateActionAutomated(newAction, action)) {
                iNum++;
            }
        }
        // Acciones de aldeanos
        ArrayList<Integer> citizens = World.getCitizenIDs();
        Citizen citizen;
        for (int i = 0; i < citizens.size(); i++) {
            citizen = (Citizen) World.getLivingEntityByID(citizens.get(i));
            action = citizen.getCurrentCustomAction();
            if (action != null && isDuplicateActionAutomated(newAction, action)) {
                iNum++;
            }
        }
        // Y de soldiers (hace falta?)
        citizens = World.getSoldierIDs();
        for (int i = 0; i < citizens.size(); i++) {
            citizen = (Citizen) World.getLivingEntityByID(citizens.get(i));
            action = citizen.getCurrentCustomAction();
            if (action != null && isDuplicateActionAutomated(newAction, action)) {
                iNum++;
            }
        }

        return iNum;
    }

    private boolean isDuplicateAction(Action newAction, Action action) {
        if (newAction.getEntityID() != -1 && newAction.getEntityID() == action.getEntityID()) {
            return true;
        }

        // Miramos el destination point
        if (newAction.getDestinationPoint() != null && action.getDestinationPoint() != null && newAction.getDestinationPoint().equals(action.getDestinationPoint())) {
            return true;
        }

        return false;
    }

    private boolean isDuplicateActionAutomated(Action newAction, Action action) {
        ActionManagerItem ami = ActionManager.getItem(newAction.getId());
        ActionManagerItem amiTemp = ActionManager.getItem(action.getId());
        if (ami.getId().equals(amiTemp.getId())) {
            return true;
        }

        return false;
    }

    public void setReCheckMinePlaces(boolean reCheckMinePlaces) {
        this.reCheckMinePlaces = reCheckMinePlaces;
    }

    public boolean isReCheckMinePlaces() {
        return reCheckMinePlaces;
    }

    /**
     * Devuelve la tarea asignada al aldeano. Nulo si no tiene tarea
     *
     * @param citizenID
     * @return la tarea aseignada al aldeano. Nulo si no tiene tarea
     */
    public Task getTask(int citizenID) {
        TaskManagerItem item;
        for (int i = 0; i < taskItems.size(); i++) {
            item = taskItems.get(i);
            if (item.containsCitizen(citizenID)) {
                return item.getTask();
            }
        }

        return null;
    }

    /**
     * Elimina a un aldeano de su tarea
     *
     * @param citizen Aldeano
     */
    public void removeCitizen(Citizen citizen) {
        citizen.resetTaskIndexes(); // Importante

        TaskManagerItem item;
        for (int i = 0; i < taskItems.size(); i++) {
            item = taskItems.get(i);
            if (item.containsCitizen(citizen.getID())) {
                item.removeCitizen(citizen.getID());
                return;
            }
        }

    }

    /**
     * Elimina a un aldeano de su tarea
     *
     * @param citizenID ID de aldeano
     */
    public void removeCitizen(int citizenID) {
        ArrayList<Integer> citizens = World.getCitizenIDs();
        Citizen citizen;
        for (int j = 0; j < citizens.size(); j++) {
            citizen = (Citizen) World.getLivingEntityByID(citizens.get(j));
            if (citizen.getID() == citizenID) {
                removeCitizen(citizen);
                return;
            }
        }

        // soldiers?
        citizens = World.getSoldierIDs();
        for (int j = 0; j < citizens.size(); j++) {
            citizen = (Citizen) World.getLivingEntityByID(citizens.get(j));
            if (citizen.getID() == citizenID) {
                removeCitizen(citizen);
                return;
            }
        }
    }

    /**
     * Comprueba cuantas acciones de tipo queue se podrían meter en base a las
     * que hay en el APS
     *
     * @param ami
     * @param iNumOnAPS
     * @return
     */
    private int checkQueueAction(ActionManagerItem ami, int iNumOnAPS) {
        if (ami.getGeneratedItem() != null) {
            int iWorld = Item.getNumItems(UtilsIniHeaders.getIntIniHeader(ami.getGeneratedItem()), false, Game.getWorld ().getRestrictHaulEquippingLevel ());
            Item carryingItem;
            // Sumamos los carrying de ciudadanos
            for (int c = 0; c < World.getCitizenIDs().size(); c++) {
                carryingItem = ((Citizen) World.getLivingEntityByID(World.getCitizenIDs().get(c))).getCarrying();
                if (carryingItem != null && carryingItem.getIniHeader().equals(ami.getGeneratedItem())) {
                    iWorld++;
                }
            }
            for (int c = 0; c < World.getSoldierIDs().size(); c++) {
                carryingItem = ((Citizen) World.getLivingEntityByID(World.getSoldierIDs().get(c))).getCarrying();
                if (carryingItem != null && carryingItem.getIniHeader().equals(ami.getGeneratedItem())) {
                    iWorld++;
                }
            }

            if (ami.isInverted()) {
                if (iWorld > iNumOnAPS) {
                    int iMaxAccionesAMeter = iWorld - iNumOnAPS;

                    // Miramos prerequisitos básicos
                    QueueItem qi;
                    boolean bPrerequisitesOK = true;
                    // Actión de tipo QUEUE
                    ArrayList<QueueItem> actionQueue = ami.getQueueNoCopy();
                    ArrayList<String> alList;
                    for (int i = 0; i < actionQueue.size(); i++) {
                        qi = actionQueue.get(i);
                        if (qi.getType() == QueueItem.TYPE_MOVE) {
                            alList = Utils.getArray(qi.getValue());
                            if (alList != null && alList.size() > 0) {
                                boolean bAnyItem = false;
                                int iTmp = 0;
                                for (int t = 0; t < alList.size(); t++) {
                                    iTmp += Item.getNumItems(UtilsIniHeaders.getIntIniHeader(alList.get(t)), true, Game.getWorld ().getRestrictHaulEquippingLevel ());
                                }
                                if (iTmp > 0) {
                                    bAnyItem = true;
                                    if (iMaxAccionesAMeter > iTmp) {
                                        iMaxAccionesAMeter = iTmp;
                                    }
                                }
                                if (!bAnyItem) {
                                    bPrerequisitesOK = false;
                                }
                                if (!bPrerequisitesOK) {
                                    break;
                                }
                            }
                        } else if (qi.getType() == QueueItem.TYPE_PICK) {
                            alList = Utils.getArray(qi.getValue());
                            if (alList != null && alList.size() > 0) {
                                boolean bAnyItem = false;
                                int iTmp = 0;
                                for (int t = 0; t < alList.size(); t++) {
                                    iTmp += Item.getNumItems(UtilsIniHeaders.getIntIniHeader(alList.get(t)), false, Game.getWorld ().getRestrictHaulEquippingLevel ());
                                }
                                if (iTmp > 0) {
                                    bAnyItem = true;
                                    if (iMaxAccionesAMeter > iTmp) {
                                        iMaxAccionesAMeter = iTmp;
                                    }
                                }
                                if (!bAnyItem) {
                                    bPrerequisitesOK = false;
                                }
                                if (!bPrerequisitesOK) {
                                    break;
                                }
                            }
                        } else if (qi.getType() == QueueItem.TYPE_PICK_FRIENDLY) {
                            alList = Utils.getArray(qi.getValue());
                            if (alList != null && alList.size() > 0) {
                                boolean bAnyLiving = false;
                                int iTmp = 0;
                                for (int t = 0; t < alList.size(); t++) {
                                    iTmp += LivingEntity.getNumLivings(alList.get(t), true);
                                }
                                if (iTmp > 0) {
                                    bAnyLiving = true;
                                    if (iMaxAccionesAMeter > iTmp) {
                                        iMaxAccionesAMeter = iTmp;
                                    }
                                }
                                if (!bAnyLiving) {
                                    bPrerequisitesOK = false;
                                }
                                if (!bPrerequisitesOK) {
                                    break;
                                }
                            }
                        }
                    }

                    return iMaxAccionesAMeter;
                }
            } else {
                if (iWorld < iNumOnAPS) {
                    int iMaxAccionesAMeter = iNumOnAPS - iWorld;

                    // Miramos prerequisitos básicos
                    QueueItem qi;
                    boolean bPrerequisitesOK = true;
                    // Actión de tipo QUEUE
                    ArrayList<QueueItem> actionQueue = ami.getQueueNoCopy();
                    ArrayList<String> alList;
                    for (int i = 0; i < actionQueue.size(); i++) {
                        qi = actionQueue.get(i);
                        if (qi.getType() == QueueItem.TYPE_MOVE) {
                            alList = Utils.getArray(qi.getValue());
                            if (alList != null && alList.size() > 0) {
                                boolean bAnyItem = false;
                                int iTmp = 0;
                                for (int t = 0; t < alList.size(); t++) {
                                    iTmp += Item.getNumItems(UtilsIniHeaders.getIntIniHeader(alList.get(t)), true, Game.getWorld ().getRestrictHaulEquippingLevel ());
                                }
                                if (iTmp > 0) {
                                    bAnyItem = true;
                                    if (iMaxAccionesAMeter > iTmp) {
                                        iMaxAccionesAMeter = iTmp;
                                    }
                                }
                                if (!bAnyItem) {
                                    bPrerequisitesOK = false;
                                }
                                if (!bPrerequisitesOK) {
                                    iMaxAccionesAMeter = 0;
                                    break;
                                }
                            }
                        } else if (qi.getType() == QueueItem.TYPE_PICK) {
                            alList = Utils.getArray(qi.getValue());
                            if (alList != null && alList.size() > 0) {
                                boolean bAnyItem = false;
                                int iTmp = 0;
                                for (int t = 0; t < alList.size(); t++) {
                                    iTmp += Item.getNumItems(UtilsIniHeaders.getIntIniHeader(alList.get(t)), false, Game.getWorld ().getRestrictHaulEquippingLevel ());
                                }
                                if (iTmp > 0) {
                                    bAnyItem = true;
                                    if (iMaxAccionesAMeter > iTmp) {
                                        iMaxAccionesAMeter = iTmp;
                                    }
                                }
                                if (!bAnyItem) {
                                    bPrerequisitesOK = false;
                                }
                                if (!bPrerequisitesOK) {
                                    iMaxAccionesAMeter = 0;
                                    break;
                                }
                            }
                        } else if (qi.getType() == QueueItem.TYPE_PICK_FRIENDLY) {
                            alList = Utils.getArray(qi.getValue());
                            if (alList != null && alList.size() > 0) {
                                boolean bAnyLiving = false;
                                int iTmp = 0;
                                for (int t = 0; t < alList.size(); t++) {
                                    iTmp += LivingEntity.getNumLivings(alList.get(t), true);
                                }
                                if (iTmp > 0) {
                                    bAnyLiving = true;
                                    if (iMaxAccionesAMeter > iTmp) {
                                        iMaxAccionesAMeter = iTmp;
                                    }
                                }
                                if (!bAnyLiving) {
                                    bPrerequisitesOK = false;
                                }
                                if (!bPrerequisitesOK) {
                                    iMaxAccionesAMeter = 0;
                                    break;
                                }
                            }
                        }
                    }

                    return iMaxAccionesAMeter;
                }
            }
        }

        return 0;
    }

    /**
     * Ejecuta todas las tareas necesarias del manager (sincronizar, tareas
     * directas, eliminar tareas vencidas, asignar nuevas tareas...
     */
    public void executeAll(boolean bOnlyDirect) {
		// System.out.println (customActions.size () + ", " + customActionsTemp.size () + ", " + customActionsWait.size ());
        // Miramos si tenemos tareas automáticas del panel de producción, sólo lo hacemos cada FPS_INGAME turnos
        if (automatedQueueTurns < 32) {
            automatedQueueTurns++;
        } else {
            automatedQueueTurns = 0;

            synchronized (hmItemsOnAutomatedQueue) {
                String sQueue;
                Integer iValue;
                ActionManagerItem ami;
                int iMaxAccionesAMeter;
                Iterator<String> it = hmItemsOnAutomatedQueue.keySet().iterator();
                while (it.hasNext()) {
                    sQueue = it.next();
                    iValue = hmItemsOnAutomatedQueue.get(sQueue);
                    if (iValue.intValue() > 0) {
                        ami = ActionManager.getItem(sQueue);
                        iMaxAccionesAMeter = checkQueueAction(ami, iValue);

                        // Prerequisitos OK, metemos las qué podemos hacer (en base a moves y picks ahora mismo)
                        if (iMaxAccionesAMeter > 0) {

                            Action action = new Action(sQueue);
                            action.setQueue(ami.getQueue());
                            action.setQueueData(new QueueData());
                            action.setSilent(true);

                            int iActionsIgualesEnCurso = getNumDuplicateActionAutomated(action);

                            // Metemos todas las acciones que haga falta
                            while (iActionsIgualesEnCurso < iMaxAccionesAMeter) {
                                addCustomAction(action, false);

                                iActionsIgualesEnCurso++;

                                if (iActionsIgualesEnCurso < iMaxAccionesAMeter) {
                                    action = new Action(sQueue);
                                    action.setQueue(ami.getQueue());
                                    action.setQueueData(new QueueData());
                                    action.setSilent(true);
                                }
                            }
                        }
                    }
                }
            }
        }

		// Metemos las tareas síncronamente en la lista de tareas
        // Tiene que hacerse lo primero de todo, ya que si no podrían pasar tareas sin hotpoint (tareas directas) al método de asignar tareas normales
        synchronized (taskItemsTemp) {
            for (int i = 0; i < taskItemsTemp.size(); i++) {
                taskItems.add(taskItemsTemp.remove(0));
            }
        }

        // Ahora metemos las custom actions, las metemos ordenadas
        Action action;
        synchronized (alProductionToRemove) {
            synchronized (customActionsTemp) {

                // Primero metemos 4 de las wait (esto es para performance, si hay 2000 acciones no las chequeará todas cada vez)
                for (int i = 0; i < 4; i++) {
                    if (customActionsWait.size() > 0) {
                        customActionsTemp.add(customActionsWait.remove(0));
                    }
                }

                for (int i = 0; i < customActionsTemp.size(); i++) {
                    action = customActionsTemp.remove(0);
                    if (alProductionToRemove.contains(action.getId())) {
                        alProductionToRemove.remove(action.getId());

                        removeFromProductionPanelRegular(action.getId());
                    } else {
                        customActions.add(action);
                    }
                }
            }

            // Miramos citizens
            Citizen citizen;
            Task task;
            for (int i = 0; i < World.getCitizenIDs().size(); i++) {
                citizen = (Citizen) World.getLivingEntityByID(World.getCitizenIDs().get(i));
                action = citizen.getCurrentCustomAction();
                task = citizen.getCurrentTask();
                if (action != null && task != null && alProductionToRemove.contains(action.getId())) {
                    alProductionToRemove.remove(action.getId());

                    task.setFinished(true);
                    removeCitizen(citizen);
                }
            }
            for (int i = 0; i < World.getSoldierIDs().size(); i++) {
                citizen = (Citizen) World.getLivingEntityByID(World.getSoldierIDs().get(i));
                action = citizen.getCurrentCustomAction();
                task = citizen.getCurrentTask();
                if (action != null && task != null && alProductionToRemove.contains(action.getId())) {
                    alProductionToRemove.remove(action.getId());

                    task.setFinished(true);
                    removeCitizen(citizen);
                }
            }

            // Miramos tareas en la lista normal de tareas
            synchronized (customActions) {
                for (int i = customActions.size() - 1; i >= 0; i--) {
                    action = customActions.get(i);
                    if (alProductionToRemove.contains(action.getId())) {
                        alProductionToRemove.remove(action.getId());
                        removeAction(i, false, true, null);
                    }
                }
            }

            alProductionToRemove.clear();
        }

        synchronized (taskItems) {
            executeDirectTasks();

            // Eliminamos tareas vencidas
            removeFinishedTasks();

            if (!bOnlyDirect) { // Esto es por si el juego está en pausa, que ejecute las tareas directas
                if (isReCheckMinePlaces()) {
                    recheckMinePlaces();
                    setReCheckMinePlaces(false);
                }
                // Asignamos nuevas tareas
                assignTasks();
            }
        }

    }

    /**
     * Comprueba las places de las órdenes de mine/dig Se llama cuando se acaba
     * de digar/minar en un sitio, por si ha cambiado algo También cancelaremos
     * tareas de dig mine si ya están hechas
     */
    private void recheckMinePlaces() {
        TaskManagerItem item;
        Point3DShort hPoint3D;
        Cell cell;
        for (int i = 0; i < taskItems.size(); i++) {
            item = taskItems.get(i);
            int iTaskType = item.getTask().getTask();
            if (!item.getTask().isFinished() && (iTaskType == Task.TASK_MINE || iTaskType == Task.TASK_MINE_LADDER)) {
                ArrayList<HotPoint> alHotpoints = item.getTask().getHotPoints();
                if (alHotpoints != null && alHotpoints.size() > 0) {
                    for (int h = 0; h < alHotpoints.size(); h++) {
                        hPoint3D = alHotpoints.get(h).getHotPoint();
                        cell = World.getCell(hPoint3D);

                        if (iTaskType == Task.TASK_MINE || iTaskType == Task.TASK_MINE_LADDER) {
                            if (!(!cell.getTerrain().hasFluids() && (!cell.isMined()) || !cell.isDiscovered())) {
                                // if (terrain.isMined ()) {
                                cell.setFlagOrders(false);
                                alHotpoints.get(h).setFinished(true);
                            }
                        }
                        if (!alHotpoints.get(h).isFinished()) {
                            alHotpoints.get(h).setPlaces(Task.getAccesingPoints(hPoint3D.x, hPoint3D.y, hPoint3D.z, iTaskType), true);
                        }
                    }
                }
            }
        }
    }

    public HashMap<String, Integer> getItemsOnRegularQueue() {
        return hmItemsOnRegularQueue;
    }

    public HashMap<String, Integer> getItemsOnAutomatedQueue() {
        return hmItemsOnAutomatedQueue;
    }

    public int getNumItemsOnAutomatedQueue(String sID) {
        synchronized (hmItemsOnAutomatedQueue) {
            if (hmItemsOnAutomatedQueue.containsKey(sID)) {
                return hmItemsOnAutomatedQueue.get(sID).intValue();
            } else {
                return 0;
            }
        }
    }

    public void addItemOnAutomatedQueue(String sID) {
        synchronized (hmItemsOnAutomatedQueue) {
            if (hmItemsOnAutomatedQueue.containsKey(sID)) {
                Integer iValue = hmItemsOnAutomatedQueue.get(sID);
                if (iValue != null) {
                    hmItemsOnAutomatedQueue.put(sID, Integer.valueOf(iValue.intValue() + 1));
                } else {
                    hmItemsOnAutomatedQueue.put(sID, Integer.valueOf(1));
                }
            } else {
                hmItemsOnAutomatedQueue.put(sID, Integer.valueOf(1));
            }
        }
    }

    public void removeItemOnAutomatedQueue(String sID) {
        synchronized (hmItemsOnAutomatedQueue) {
            if (hmItemsOnAutomatedQueue.containsKey(sID)) {
                Integer iValue = hmItemsOnAutomatedQueue.get(sID);
                if (iValue != null) {
                    int intValue = iValue.intValue();
                    if (intValue > 1) {
                        hmItemsOnAutomatedQueue.put(sID, Integer.valueOf(intValue - 1));
                    } else {
                        hmItemsOnAutomatedQueue.put(sID, Integer.valueOf(0));
                    }
                } else {
                    hmItemsOnAutomatedQueue.put(sID, Integer.valueOf(0));
                }
            }
        }
    }

    /**
     * Ejecuta las tareas directas.
     *
     */
    private void executeDirectTasks() {
        TaskManagerItem item;
        for (int i = (taskItems.size() - 1); i >= 0; i--) {
            item = taskItems.get(i);
            if (item.getTask().getTask() == Task.TASK_DESTROY_BUILDING) {
                // DESTROY BUILDING

                Building building = Building.getBuilding(item.getTask().getPointIni());
                if (building != null) {
                    Building.delete(item.getTask().getPointIni().toPoint3DShort());

                    // Marcamos como finished si hay tarea de build de este edificio
                    TaskManagerItem itemAux;
                    for (int j = 0; j < taskItems.size(); j++) {
                        itemAux = taskItems.get(j);
                        if (itemAux.getTask().getTask() == Task.TASK_BUILD) {
                            if (itemAux.getTask().getHotPoint(0).getHotPoint().equals(item.getTask().getPointIni())) {
                                itemAux.getTask().setFinished(true);
                                break;
                            }
                        }
                    }
                }
                item.getTask().setFinished(true);
            } else if (item.getTask().getTask() == Task.TASK_TURN_OFF_NON_STOP || item.getTask().getTask() == Task.TASK_TURN_ON_NON_STOP) {
                // TURN ON/OFF NON_STOP

                Building building = Building.getBuilding(item.getTask().getPointIni());
                if (building != null) {
                    building.setNonStop(item.getTask().getTask() == Task.TASK_TURN_ON_NON_STOP);
                }
                item.getTask().setFinished(true);
				// POPO } else if (item.getTask ().getTask () == Task.TASK_TERRAIN_RAISE) {
                // TERRAIN RAISE
                // POPO Terrain.raise (World.getCells (), item.getTask ().getPointIni (), true);
                // POPO item.getTask ().setFinished (true);
                // POPO } else if (item.getTask ().getTask () == Task.TASK_TERRAIN_LOWER) {
                // POPO // TERRAIN LOWER
                // POPO Terrain.lower (World.getCells (), item.getTask ().getPointIni (), true);
                // POPO item.getTask ().setFinished (true);
            } else if (item.getTask().getTask() == Task.TASK_TERRAIN_CHANGE) {
                // TERRAIN CHANGE
                Cell cell = World.getCell(item.getTask().getPointIni());
				// int id = Terrain.getTileHeightID (item.getTask ().getPointIni ().x, item.getTask ().getPointIni ().y, item.getTask ().getPointIni ().z);
                // cell.getTerrain ().setIniHeader (item.getTask ().getParameter ());
                // cell.getTerrain ().setTileSetCoordinates (item.getTask ().getParameter () + MapGenerator.SLOPES_INIHEADER[id], item.getTask ().getParameter ());
                // cell.getTerrain ().setTileSize (item.getTask ().getParameter () + MapGenerator.SLOPES_INIHEADER[id], item.getTask ().getParameter ());
                // cell.getTerrain ().setTileSetTexCoordinates ();
                TerrainManagerItem tmi = TerrainManager.getItem(item.getTask().getParameter());
                int iAdd = cell.getTerrain().getTerrainTileID() - (cell.getTerrain().getTerrainID() * TerrainManager.SLOPES_INIHEADER.length);
                cell.getTerrain().setTerrainID(tmi.getTerrainID());
                cell.getTerrain().setTerrainTileID((tmi.getTerrainID() * TerrainManager.SLOPES_INIHEADER.length) + iAdd);
                item.getTask().setFinished(true);
            } else if (item.getTask().getTask() == Task.TASK_TERRAIN_ADD_FLUID) {
                // TERRAIN ADD FLUID
                Cell cell = World.getCell(item.getTask().getPointIni());
                int iFluidType = Integer.parseInt(item.getTask().getParameter());
                int iFluidCount = Integer.parseInt(item.getTask().getParameter2());

                cell.getTerrain().setFluidType(iFluidType);
                cell.getTerrain().setFluidCount(iFluidCount);

                World.checkNewEvaporation(cell);

                Game.getWorld().addFluidCellToProcess(item.getTask().getPointIni().x, item.getTask().getPointIni().y, item.getTask().getPointIni().z, false);
                World.deleteCellAll(cell, true);

                // Paint under
                Cell.setShouldPaintUnders(World.getCells(), item.getTask().getPointIni().toPoint3DShort());

                cell.setAstarZoneID(-1);

                World.setRecheckASZID(true);

                item.getTask().setFinished(true);
            } else if (item.getTask().getTask() == Task.TASK_TERRAIN_REMOVE_FLUID) {
                // TERRAIN REMOVE FLUID
                Point3D p3d = item.getTask().getPointIni();
                Cell cell = World.getCell(p3d);

                cell.getTerrain().setFluidType(Terrain.FLUIDS_NONE);
                cell.getTerrain().setFluidCount(0);
                World.checkNewEvaporation(cell);
                Game.getWorld().addFluidCellToProcess(p3d.x, p3d.y, p3d.z, true);

                Cell.setShouldPaintUnders(World.getCells(), item.getTask().getPointIni().toPoint3DShort());

                Cell.mergeZoneID(p3d.x, p3d.y, p3d.z, false);
                item.getTask().setFinished(true);
            } else if (item.getTask().getTask() == Task.TASK_DESTROY_ENTITY) {
                // DESTROY ENTITY

                Cell cell = World.getCell(item.getTask().getPointIni());
                if (cell.hasEntity()) {
                    cell.getEntity().delete();
                }
                item.getTask().setFinished(true);
            } else if (item.getTask().getTask() == Task.TASK_CREATE) {
				// CREAR UN ITEM SIN ESPECIFICAR EDIFICIO
                // Primero miramos en qué edificio se puede construir el item
                String sItemIniHeader = item.getTask().getParameter();
                ItemManagerItem imi = ItemManager.getItem(sItemIniHeader);
                if (imi != null) {
                    String sBuildingIniHeader = imi.getBuilding();
                    if (sBuildingIniHeader != null) {
                        // Buscamos edificio donde se pueda construir, si tiene la cola llena seguimos a ver si encontramos uno sin cola
                        Building building;
                        int iBuildingIndex = -1;
                        for (int j = 0; j < World.getBuildings().size(); j++) {
                            building = World.getBuildings().get(j);
                            if (building.getIniHeader().equals(sBuildingIniHeader)) {
                                // Edificio que puede construirlo
                                if (iBuildingIndex == -1) {
                                    iBuildingIndex = j;
                                    if (!building.hasItemsInQueue()) {
                                        // Si no tiene elementos en cola ya lo tenemos, salimos
                                        break;
                                    }
                                } else {
                                    // Miramos si no tiene cola
                                    if (!building.hasItemsInQueue()) {
                                        iBuildingIndex = j;
                                        break;
                                    }
                                }
                            }
                        }

                        if (iBuildingIndex == -1) {
                            // No existe edificio para construirlo
                            BuildingManagerItem bmi = BuildingManager.getItem(sBuildingIniHeader);
                            if (bmi == null) {
                                Log.log(Log.LEVEL_ERROR, Messages.getString("TaskManager.1") + sBuildingIniHeader + "]", getClass().toString()); //$NON-NLS-1$ //$NON-NLS-2$
                            } else {
                                MessagesPanel.addMessage(MessagesPanel.TYPE_ANNOUNCEMENT, Messages.getString("TaskManager.3") + imi.getName() + Messages.getString("TaskManager.4") + bmi.getName() + Messages.getString("TaskManager.2"), ColorGL.ORANGE); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            }
                        } else {
                            // Tenemos el edificio de guais, metemos el item en cola y pacasa
                            building = World.getBuildings().get(iBuildingIndex);
                            BuildingManagerItem bmi = BuildingManager.getItem(building.getIniHeader());
                            Item it = Item.createItem(imi);
                            it.setCoordinates(bmi.getEntranceBaseCoordinates().merge(building.getCoordinates()));
                            it.setPrerequisites(imi.getPrerequisites());
                            it.setOperative(true);
                            building.addItem(it);
                        }
                    } else {
                        Log.log(Log.LEVEL_ERROR, Messages.getString("TaskManager.6") + sItemIniHeader + Messages.getString("TaskManager.7"), getClass().toString()); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                } else {
                    Log.log(Log.LEVEL_ERROR, Messages.getString("TaskManager.8") + sItemIniHeader + "]", getClass().toString()); //$NON-NLS-1$ //$NON-NLS-2$
                }

                item.getTask().setFinished(true);
            } else if (item.getTask().getTask() == Task.TASK_QUEUE) {
                // CREAR UNA COLA DE TAREAS

                ActionManagerItem ami = ActionManager.getItem(item.getTask().getParameter());
                if (ami != null) {
                    // Cola
                    Action action = new Action(ami.getId());
                    action.setQueue(ami.getQueue());
                    action.setQueueData(new QueueData());
                    addCustomAction(action, false);
                }

                item.getTask().setFinished(true);
            } else if (item.getTask().getTask() == Task.TASK_CREATE_IN_A_BUILDING) {
				// PONER UN ITEM EN LA COLA DE UN EDIFICIO
                // Obtenemos el edificio
                Building building = Building.getBuilding(item.getTask().getPointIni());
                if (building != null) {
                    // Añadimos un elemento a la cola
                    BuildingManagerItem bmi = BuildingManager.getItem(building.getIniHeader());
                    ItemManagerItem imi = ItemManager.getItem(item.getTask().getParameter());
                    Item it = Item.createItem(imi);
                    it.setCoordinates(bmi.getEntranceBaseCoordinates().merge(building.getCoordinates()));
                    it.setPrerequisites(imi.getPrerequisites());
                    it.setOperative(true);
                    building.addItem(it);
                }
                item.getTask().setFinished(true);
            } else if (item.getTask().getTask() == Task.TASK_REMOVE_BUILDING_TASK) {
				// QUITAR UNA TAREA DE UN EDIFICIO
                // Obtenemos el edificio
                Building building = Building.getBuilding(item.getTask().getPointIni());
                int iItemIndex = Integer.parseInt(item.getTask().getParameter2());
                if (iItemIndex >= building.getItemQueue().size()) {
                    iItemIndex = building.getItemQueue().size() - 1;
                }
                if (building != null) {
                    for (int b = iItemIndex; b >= 0; b--) {
                        if (building.getItemQueue().get(b).getIniHeader().equals(item.getTask().getParameter())) {
                            // Perfecto, borramos la tarea
                            building.getItemQueue().remove(b);
                            break;
                        }
                    }
                }
                item.getTask().setFinished(true);
            } else if (item.getTask().getTask() == Task.TASK_DELETE_STOCKPILE) {
                // DELETE STOCKPILE

                Stockpile.deleteStockpile(Integer.parseInt(item.getTask().getParameter()));
                item.getTask().setFinished(true);
//			} else if (item.getTask ().getTask () == Task.TASK_STOCKPILE_ENABLE_ALL) {
//				// STOCKPILE ENABLE ALL
//
//				//Stockpile.enableAll (Integer.parseInt (item.getTask ().getParameter ()), item.getTask ().getParameter2 ());
//				item.getTask ().setFinished (true);
//			} else if (item.getTask ().getTask () == Task.TASK_STOCKPILE_DISABLE_ALL) {
//				// STOCKPILE DISABLE ALL
//
//				//Stockpile.disableAll (Integer.parseInt (item.getTask ().getParameter ()), item.getTask ().getParameter2 ());
//				item.getTask ().setFinished (true);
            } else if (item.getTask().getTask() == Task.TASK_LOCK) {
                // LOCK ITEM
                Integer itemID = Integer.valueOf(item.getTask().getParameter());
                Item itemToLock = World.getItems().get(itemID);
                if (itemToLock != null) {
                    itemToLock.setWallConnectorStatus(Item.FLAG_WALL_CONNECTOR_STATUS_LOCKED_AND_CLOSED, true);
                }
                item.getTask().setFinished(true);
            } else if (item.getTask().getTask() == Task.TASK_UNLOCK_OPEN) {
                // UNLOCK ITEM, OPEN
                Integer itemID = Integer.valueOf(item.getTask().getParameter());
                Item itemToUnlock = World.getItems().get(itemID);
                if (itemToUnlock != null) {
                    itemToUnlock.setWallConnectorStatus(Item.FLAG_WALL_CONNECTOR_STATUS_UNLOCKED_AND_OPENED, true);
                }
                item.getTask().setFinished(true);
            } else if (item.getTask().getTask() == Task.TASK_UNLOCK_CLOSE) {
                // UNLOCK ITEM, CLOSE
                Integer itemID = Integer.valueOf(item.getTask().getParameter());
                Item itemToUnlock = World.getItems().get(itemID);
                if (itemToUnlock != null) {
                    itemToUnlock.setWallConnectorStatus(Item.FLAG_WALL_CONNECTOR_STATUS_UNLOCKED_AND_CLOSED, true);
                }
                item.getTask().setFinished(true);
            } else if (item.getTask().getTask() == Task.TASK_DELETE_ZONE) {
                // DELETE ZONE

                Zone.deleteZone(Integer.parseInt(item.getTask().getParameter()));
                item.getTask().setFinished(true);
//			} else if (item.getTask ().getTask () == Task.TASK_STOCKPILE_ENABLE_ITEM) {
                // Enable entity in stockpile

//				Stockpile stockpile = Stockpile.getStockpile (item.getTask ().getPointIni ().toPoint3DShort ());
//				if (stockpile != null && !stockpile.getType ().contains (item.getTask ().getParameter ())) {
//					Type type = Types.getType (stockpile.getType ().getID ());
//					String sName = type.getElementName (item.getTask ().getParameter ());
//					if (sName != null) {
//						stockpile.getType ().addElement (item.getTask ().getParameter (), sName);
//					}
//				}
//				item.getTask ().setFinished (true);
//			} else if (item.getTask ().getTask () == Task.TASK_STOCKPILE_DISABLE_ITEM) {
                // DISABLE ITEM IN STOCKPILE
//				Stockpile stockpile = Stockpile.getStockpile (item.getTask ().getPointIni ().toPoint3DShort ());
//				if (stockpile != null) {
//					stockpile.getType ().removeElement (item.getTask ().getParameter ());
//					// Marcamos items para hauling
//					for (int h = 0; h < stockpile.getPoints ().size (); h++) {
//						Game.getWorld ().addItemToBeHauled (World.getCell (stockpile.getPoints ().get (h)).getItem ());
//					}
//				}
//				item.getTask ().setFinished (true);
//			} else if (item.getTask ().getTask () == Task.TASK_CONTAINER_ENABLE_ALL) {
                // CONTAINER ENABLE ALL
//				Container container = Game.getWorld ().getContainer (Integer.parseInt (item.getTask ().getParameter ()));
//				if (container != null) {
//					container.enableAll (item.getTask ().getParameter2 ());
//				}
//				item.getTask ().setFinished (true);
//			} else if (item.getTask ().getTask () == Task.TASK_CONTAINER_DISABLE_ALL) {
                // CONTAINER DISABLE ALL
//				Container container = Game.getWorld ().getContainer (Integer.parseInt (item.getTask ().getParameter ()));
//				if (container != null) {
//					container.disableAll (item.getTask ().getParameter2 ());
//				}
//				item.getTask ().setFinished (true);
//			} else if (item.getTask ().getTask () == Task.TASK_CONTAINER_DISABLE_ITEM) {
                // DISABLE ITEM IN CONTAINER
//				Container container = Game.getWorld ().getContainer (Integer.parseInt (item.getTask ().getParameter ()));
//				if (container != null) {
//					container.disableItem (item.getTask ().getParameter2 ());
//				}
//				item.getTask ().setFinished (true);
//			} else if (item.getTask ().getTask () == Task.TASK_CONTAINER_ENABLE_ITEM) {
                // ENABLE ITEM IN CONTAINER
//				Container container = Game.getWorld ().getContainer (Integer.parseInt (item.getTask ().getParameter ()));
//				if (container != null) {
//					container.enableItem (item.getTask ().getParameter2 ());
//				}
//				item.getTask ().setFinished (true);
            } else if (item.getTask().getTask() == Task.TASK_CONVERT_TO_CIVILIAN || item.getTask().getTask() == Task.TASK_CONVERT_TO_SOLDIER) {
                // CONVERTIR UN SOLDADO EN CIVIL O VICEVERSA
                int citID = Integer.parseInt(item.getTask().getParameter());

                // Puede convertir, lo hacemos
                Citizen citizen = (Citizen) World.getLivingEntityByID(citID);
                if (citizen != null && ((citizen.getSoldierData().isSoldier() && item.getTask().getTask() == Task.TASK_CONVERT_TO_CIVILIAN) || (!citizen.getSoldierData().isSoldier() && item.getTask().getTask() == Task.TASK_CONVERT_TO_SOLDIER))) {
                    if (item.getTask().getTask() == Task.TASK_CONVERT_TO_SOLDIER) {
                        // Convertir a soldado, seteamos su soldier data
                        citizen.getSoldierData().setState(SoldierData.STATE_GUARD, -1, citizen.getID());

                        // Lo borramos del job group
                        int iOldGroup = citizen.getCitizenData().getGroupID();
                        citizen.getCitizenData().setGroupID(-1);
                        if (iOldGroup != -1) {
                            CitizenGroupData cgd = Game.getWorld().getCitizenGroups().getGroup(iOldGroup);
                            if (cgd != null) {
                                cgd.getLivingIDs().remove(Integer.valueOf(citID));
                            }
                        } else {
                            Game.getWorld().getCitizenGroups().getCitizensWithoutGroup().remove(Integer.valueOf(citID));
                        }

                        // Tutorial flow
                        Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_SOLDIER, 0, null);
                    } else {
                        // Convertir a civil, seteamos su soldier data
                        citizen.getSoldierData().setState(SoldierData.STATE_NOT_A_SOLDIER, -1, citizen.getID());

                        // Lo añadimos al job group
                        Game.getWorld().getCitizenGroups().getCitizensWithoutGroup().add(Integer.valueOf(citizen.getID()));
                        // Borramos sus jobs
                        citizen.getCitizenData().removeAllDeniedJobs();
                    }
                }

                item.getTask().setFinished(true);
            } else if (item.getTask().getTask() == Task.TASK_SOLDIER_SET_STATE) {
                // CAMBIAR EL ESTADO DE UN SOLDIER
                int citID = Integer.parseInt(item.getTask().getParameter());

                // Puede convertir, lo hacemos
                Citizen citizen = (Citizen) World.getLivingEntityByID(citID);
                if (citizen != null) {
                    int iState = Integer.parseInt(item.getTask().getParameter2());
                    if (citizen.getSoldierData().getState() != iState || (iState == SoldierData.STATE_IN_A_GROUP && citizen.getSoldierData().getGroup() != item.getTask().getPointIni().x)) {
                        citizen.getSoldierData().setState(iState, item.getTask().getPointIni().x, citizen.getID());
                        if (iState == SoldierData.STATE_BOSS_AROUND) {
                            citizen.getSoldierData().setCounter(Utils.getRandomBetween(0, SoldierData.COUNTER_MAX_BOSS_AROUND - 1));
                        }

						// Tutorial flow
                        if (iState == SoldierData.STATE_GUARD) {
    						Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_LIVINGS_GUARD, null);
                        } else if (iState == SoldierData.STATE_PATROL) {
    						Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_LIVINGS_PATROL, null);
                        } else if (iState == SoldierData.STATE_BOSS_AROUND) {
    						Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_LIVINGS_BOSS, null);
                        }
                    }
                }

                item.getTask().setFinished(true);
            } else if (item.getTask().getTask() == Task.TASK_SOLDIER_ADD_PATROL_POINT) {
                // AÑADIR PUNTO DE PATROL A UN SOLDADO PATROL
                int citID = Integer.parseInt(item.getTask().getParameter());
                Citizen citizen = (Citizen) World.getLivingEntityByID(citID);
                if (citizen != null) {
                    Point3D p3dPatrol = item.getTask().getPointIni();
                    if (p3dPatrol != null && World.getCell(p3dPatrol).getAstarZoneID() != -1) {
                        if (citizen.getSoldierData().getState() == SoldierData.STATE_PATROL) {
                            citizen.getSoldierData().addPatrolPoint(p3dPatrol);
                        }
                    }
                }

                item.getTask().setFinished(true);
            } else if (item.getTask().getTask() == Task.TASK_SOLDIER_ADD_PATROL_POINT_GROUP) {
                // AÑADIR PUNTO DE PATROL A UN GRUPO PATROL
                Point3DShort p3dPatrol = item.getTask().getPointIni().toPoint3DShort();
                if (p3dPatrol != null && World.getCell(p3dPatrol).getAstarZoneID() != -1) {
                    int groupID = Integer.parseInt(item.getTask().getParameter());
                    SoldierGroupData sgd = Game.getWorld().getSoldierGroups().getGroup(groupID);

                    if (sgd.getState() == SoldierGroupData.STATE_PATROL) {
                        sgd.addPatrolPoint(p3dPatrol);
                    }
                }

                item.getTask().setFinished(true);
            } else if (item.getTask().getTask() == Task.TASK_SOLDIER_REMOVE_PATROL_POINT) {
                // ELIMINAR PUNTO DE PATROL A UN SOLDADO PATROL
                int citID = Integer.parseInt(item.getTask().getParameter());
                Citizen citizen = (Citizen) World.getLivingEntityByID(citID);
                if (citizen != null) {
                    Point3DShort p3dPatrol = item.getTask().getPointIni().toPoint3DShort();
                    if (p3dPatrol != null) {
                        citizen.getSoldierData().removePatrolPoint(p3dPatrol);
                    }

                }

                item.getTask().setFinished(true);
            } else if (item.getTask().getTask() == Task.TASK_SOLDIER_REMOVE_PATROL_POINT_GROUP) {
                // ELIMINAR PUNTO DE PATROL A UN GRUPO PATROL
                Point3DShort p3dPatrol = item.getTask().getPointIni().toPoint3DShort();
                int groupID = Integer.parseInt(item.getTask().getParameter());

                SoldierGroupData sgd = Game.getWorld().getSoldierGroups().getGroup(groupID);
                sgd.removePatrolPoint(p3dPatrol);

                item.getTask().setFinished(true);
            } else if (item.getTask().getTask() == Task.TASK_CHANGE_OWNER) {
                // Cambiar el propietario de la zona personal
                int zoneID = Integer.parseInt(item.getTask().getParameter());
                int citizenID = Integer.parseInt(item.getTask().getParameter2());

                Zone zone = Zone.getZone(zoneID);
                if (zone != null && zone instanceof ZonePersonal) {
                    ZonePersonal zonePersonal = (ZonePersonal) zone;
                    Citizen citizenCurrent = null;
                    if (zonePersonal.getOwnerID() != -1) {
                        LivingEntity le = World.getLivingEntityByID(zonePersonal.getOwnerID());
                        if (le != null) {
                            LivingEntityManagerItem lemi = LivingEntityManager.getItem(le.getIniHeader());
                            if (lemi.getType() == LivingEntity.TYPE_CITIZEN) {
                                citizenCurrent = (Citizen) le;
                            }
                        }
                    }

					// Tenemos al propietario actual
                    // Buscamos a la living destino
                    Citizen citizenNuevo = null;
                    LivingEntity leDestino = World.getLivingEntityByID(citizenID);
                    if (leDestino != null) {
                        LivingEntityManagerItem lemi = LivingEntityManager.getItem(leDestino.getIniHeader());
                        if (lemi.getType() == LivingEntity.TYPE_CITIZEN) {
                            citizenNuevo = (Citizen) leDestino;
                        }
                    }

					// Ya tenemos todo, go, go, go
                    // Sólo hacemos las cosas si existe un destino, claro
                    if (citizenNuevo != null) {
                        if (citizenNuevo.getCitizenData().hasZone()) {
                            int iZoneNuevo = citizenNuevo.getCitizenData().getZoneID();
                            Zone zoneNuevo = Zone.getZone(iZoneNuevo);
                            if (zoneNuevo != null && zoneNuevo instanceof ZonePersonal) {
                                // Borramos el propietario de la zone y le metemos el nuevo
                                zonePersonal.setOwnerID(citizenNuevo.getID());
                                citizenNuevo.getCitizenData().setZoneID(zoneID);

                                // Eliminamos/cambiamos la zona del origen
                                if (citizenCurrent != null) {
                                    // Cambiamos
                                    citizenCurrent.getCitizenData().setZoneID(iZoneNuevo);
                                    ((ZonePersonal) zoneNuevo).setOwnerID(citizenCurrent.getID());
                                } else {
                                    // La zona destino no tenía dueño, así que borramos el dueño de la zona origen
                                    ((ZonePersonal) zoneNuevo).setOwnerID(-1);
                                }
                            }
                        } else {
                            // Borramos el propietario de la zone y le metemos el nuevo
                            zonePersonal.setOwnerID(citizenNuevo.getID());
                            citizenNuevo.getCitizenData().setZoneID(zoneID);

                            if (citizenCurrent != null) {
                                citizenCurrent.getCitizenData().setZoneID(0);
                            }
                        }
                    }
                }

                item.getTask().setFinished(true);
            } else if (item.getTask().getTask() == Task.TASK_CHANGE_OWNER_GROUP) {
                // Cambiar el GRUPO propietario de la zona
                int zoneID = Integer.parseInt(item.getTask().getParameter());
                int groupID = Integer.parseInt(item.getTask().getParameter2());

                if (groupID >= 0 && groupID < SoldierGroups.MAX_GROUPS) {
                    Zone zone = Zone.getZone(zoneID);
                    if (zone != null && zone instanceof ZoneBarracks) {
                        ZoneBarracks zoneBarracks = (ZoneBarracks) zone;
                        SoldierGroupData groupCurrent = null;
                        if (zoneBarracks.getGroupID() >= 0 && zoneBarracks.getGroupID() < SoldierGroups.MAX_GROUPS) {
                            groupCurrent = Game.getWorld().getSoldierGroups().getGroup(zoneBarracks.getGroupID());
                        }

						// Tenemos al propietario actual
                        // Buscamos al grupo destino
                        SoldierGroupData groupNuevo = Game.getWorld().getSoldierGroups().getGroup(groupID);

						// Ya tenemos todo, go, go, go
                        // Sólo hacemos las cosas si existe un destino, claro
                        if (groupNuevo.hasZone()) {
                            int iZoneNuevo = groupNuevo.getZoneID();
                            Zone zoneNuevo = Zone.getZone(iZoneNuevo);
                            if (zoneNuevo != null && zoneNuevo instanceof ZoneBarracks) {
                                // Borramos el propietario de la zone y le metemos el nuevo
                                zoneBarracks.setGroupID(groupNuevo.getId());
                                groupNuevo.setZoneID(zoneID);

                                // Eliminamos/cambiamos la zona del origen
                                if (groupCurrent != null) {
                                    groupCurrent.setZoneID(iZoneNuevo);
                                    ((ZoneBarracks) zoneNuevo).setGroupID(groupCurrent.getId());
                                }
                            }
                        } else {
                            // Borramos el propietario de la zone y le metemos el nuevo
                            zoneBarracks.setGroupID(groupNuevo.getId());
                            groupNuevo.setZoneID(zoneID);

                            if (groupCurrent != null) {
                                groupCurrent.setZoneID(0);
                            }
                        }
                    }

                }
                item.getTask().setFinished(true);
            }
        }
    }

    /**
     * Elimina las tareas finalizadas
     */
    private void removeFinishedTasks() {
        TaskManagerItem item;
        for (int i = (taskItems.size() - 1); i >= 0; i--) {
            item = taskItems.get(i);
            if (item.getTask().isFinished()) {
                for (int j = 0; j < item.getListCitizens().size(); j++) {
                    removeCitizen(item.getListCitizens().get(j));
                }

                // Borramos la tarea
//				TaskManagerItem tmi = taskItems.remove (i);
                taskItems.remove(i);

                // Si era tarea de haul reseteamos el contador de turnos, para que vuelva a chequear el haul sin esperar MAX_HAUL_TURNS turnos
//				if (tmi != null && tmi.getTask () != null && (tmi.getTask ().getTask () == Task.TASK_HAUL || tmi.getTask ().getTask () == Task.TASK_PUT_IN_CONTAINER)) {
//					haulTurns = MAX_HAUL_TURNS;
//				}
            }
        }
    }

    /**
     * Asigna tareas pendientes a aldeanos inactivos.
     */
    private void assignTasks() {
		// Fight tasks lo primero
        // assignFightTasks ();
        // Obtenemos aldeanos inactivos (y que no estén cargando nada) y los metemos en una hash, la key será el número de zona A*
        HashMap<Integer, ArrayList<Citizen>> hmCitizensSinTarea = new HashMap<Integer, ArrayList<Citizen>>();
        ArrayList<Integer> citizens = World.getCitizenIDs();
        Citizen citizen;

        for (int i = 0; i < citizens.size(); i++) {
            citizen = (Citizen) World.getLivingEntityByID(citizens.get(i));
            if (!citizen.isWaitingForPath() && citizen.isIdle() && citizen.getCarrying() == null && citizen.getCarryingLiving() == null && citizen.getCitizenData().getSleep() > 0) {
                // Tenemos aldeano sin tarea, miramos la zona A* y lo metemos en la hash
                Integer iZID = Integer.valueOf(World.getCells()[citizen.getX()][citizen.getY()][citizen.getZ()].getAstarZoneID());
                ArrayList<Citizen> alCitizensSinTarea;
                if (hmCitizensSinTarea.containsKey(iZID)) {
                    // Obtenemos el Arraylist de la hash
                    alCitizensSinTarea = hmCitizensSinTarea.get(iZID);
                } else {
                    // Creamos un array vacio
                    alCitizensSinTarea = new ArrayList<Citizen>();
                }
                // Metemos al citizen en el array
                alCitizensSinTarea.add(citizen);
                // Updateamos la hash
                hmCitizensSinTarea.put(iZID, alCitizensSinTarea);
            }
        }

        // Primero borramos tareas que puedan ser null ¿?¿?¿?, esto evita un pete
        for (int i = (taskItems.size() - 1); i >= 0; i--) {
            if (taskItems.get(i) == null) {
                taskItems.remove(i);
            }
        }

        // Tareas de equipar
        Game.iError = 666;
        assignWearWearOffTasks(hmCitizensSinTarea);

        // Si no hay aldeanos sin tarea salimos
        if (hmCitizensSinTarea.size() == 0) {
            return;
        }

        // Tenemos aldeanos sin tarea, buscamos tareas pendientes
        int iLibres = getNumCitizens(hmCitizensSinTarea);
        if (iLibres == 0) {
            return;
        }

        // Tareas de crear items (en edificios) las primeras
        iLibres = assignCreateTasks(hmCitizensSinTarea, iLibres);
        if (iLibres == 0) {
            return;
        }

        Game.iError = 667;
        iLibres = getNumCitizens(hmCitizensSinTarea);
        if (iLibres == 0) {
            return;
        }

        // Ahora van las tareas "normales"
        assignNormalTasks(hmCitizensSinTarea, citizens, TASK_TYPE_OTHERS, null);

        Game.iError = 670;
        // Nos hemos "pulido" todas las tareas, miramos si quedan aldeanos libres para las tareas mine/dig, move_caravan, build_building, feed_animals, custom y de haul
        iLibres = getNumCitizens(hmCitizensSinTarea);
        if (iLibres == 0) {
            return;
        }

        // Obtenemos las prioridades de mine/dig y haul
        int iMineDigPriority = ActionPriorityManager.getPriorityValueByPriorityID(ActionPriorityManager.PRIORITY_MINE_DIG);
        int iHaulPriority = ActionPriorityManager.getPriorityValueByPriorityID(ActionPriorityManager.PRIORITY_HAUL);
        int iTradingPriority = ActionPriorityManager.getPriorityValueByPriorityID(ActionPriorityManager.PRIORITY_TRADING);
        int iBuildingsPriority = ActionPriorityManager.getPriorityValueByPriorityID(ActionPriorityManager.PRIORITY_BUILD_BUILDINGS);
        int iFeedAnimalsPriority = ActionPriorityManager.getPriorityValueByPriorityID(ActionPriorityManager.PRIORITY_FEED_ANIMALS);

        int iPriorityIndex = 0;
        boolean bHaulers = true;
        while (iPriorityIndex < ActionPriorityManager.getPrioritiesListSize()) {
            if (iPriorityIndex == iMineDigPriority) {
                assignNormalTasks(hmCitizensSinTarea, citizens, TASK_TYPE_MINEDIG, ActionPriorityManager.PRIORITY_MINE_DIG);
                iMineDigPriority = -2; // Esto servirá para saber que ya la hemos tratado
                iLibres = getNumCitizens(hmCitizensSinTarea);
            } else if (iPriorityIndex == iHaulPriority) {
                int iHaulers = getNumCitizensHaul(hmCitizensSinTarea);
                if (iHaulers > 0) {
                    assignHaulTasks(hmCitizensSinTarea, iLibres, iHaulers);
                    iLibres = getNumCitizens(hmCitizensSinTarea);
                } else {
                    bHaulers = false;
                }
                iHaulPriority = -2; // Esto servirá para saber que ya la hemos tratado
            } else if (iPriorityIndex == iFeedAnimalsPriority) {
                assignNormalTasks(hmCitizensSinTarea, citizens, TASK_TYPE_FEED_ANIMALS, ActionPriorityManager.PRIORITY_FEED_ANIMALS);
                iFeedAnimalsPriority = -2; // Esto servirá para saber que ya la hemos tratado
                iLibres = getNumCitizens(hmCitizensSinTarea);
            } else if (iPriorityIndex == iTradingPriority) {
                assignNormalTasks(hmCitizensSinTarea, citizens, TASK_TYPE_MOVE_TO_CARAVAN, ActionPriorityManager.PRIORITY_TRADING);
                iTradingPriority = -2; // Esto servirá para saber que ya la hemos tratado
                iLibres = getNumCitizens(hmCitizensSinTarea);
            } else if (iPriorityIndex == iBuildingsPriority) {
                assignNormalTasks(hmCitizensSinTarea, citizens, TASK_TYPE_BUILD_BUILDING, ActionPriorityManager.PRIORITY_BUILD_BUILDINGS);
                iBuildingsPriority = -2; // Esto servirá para saber que ya la hemos tratado
                iLibres = getNumCitizens(hmCitizensSinTarea);
            } else {
                // Custom actions (Tiene un límite de 32 tareas, si llega a el, no miramos más)
                iLibres = assignCustomActions(hmCitizensSinTarea, iLibres, iPriorityIndex);
                if (iLibres == 0) {
                    return;
                }
            }

            if (iLibres == 0) {
                return;
            }

            iPriorityIndex++;
        }

		// Si llega aquí es que tenemos más aldeanos
        // Vamos a tratar las cosas por si queda algo
        if (iMineDigPriority != -2) {
            assignNormalTasks(hmCitizensSinTarea, citizens, TASK_TYPE_MINEDIG, ActionPriorityManager.PRIORITY_MINE_DIG);
            iLibres = getNumCitizens(hmCitizensSinTarea);
            if (iLibres == 0) {
                return;
            }
        }

        if (iFeedAnimalsPriority != -2) {
            assignNormalTasks(hmCitizensSinTarea, citizens, TASK_TYPE_FEED_ANIMALS, ActionPriorityManager.PRIORITY_FEED_ANIMALS);
            iLibres = getNumCitizens(hmCitizensSinTarea);
            if (iLibres == 0) {
                return;
            }
        }

        if (iTradingPriority != -2) {
            assignNormalTasks(hmCitizensSinTarea, citizens, TASK_TYPE_MOVE_TO_CARAVAN, ActionPriorityManager.PRIORITY_TRADING);
            iLibres = getNumCitizens(hmCitizensSinTarea);
            if (iLibres == 0) {
                return;
            }
        }

        if (iBuildingsPriority != -2) {
            assignNormalTasks(hmCitizensSinTarea, citizens, TASK_TYPE_BUILD_BUILDING, ActionPriorityManager.PRIORITY_BUILD_BUILDINGS);
            iLibres = getNumCitizens(hmCitizensSinTarea);
            if (iLibres == 0) {
                return;
            }
        }

        // Customs
        iLibres = assignCustomActions(hmCitizensSinTarea, iLibres, -1);
        if (iLibres == 0) {
            return;
        }

        // Haul
        if (bHaulers && iHaulPriority != -2) {
            int iHaulers = getNumCitizensHaul(hmCitizensSinTarea);
            if (iHaulers > 0) {
                assignHaulTasks(hmCitizensSinTarea, iLibres, iHaulers);
                iLibres = getNumCitizens(hmCitizensSinTarea);
                if (iLibres == 0) {
                    return;
                }
            }
        }
    }

    /**
     *
     * @param item
     * @param hmCitizensSinTarea
     * @param citizens
     * @param iTaskType indica el tipo de tarea a procesar
     * @return
     */
    private void assignNormalTasks(HashMap<Integer, ArrayList<Citizen>> hmCitizensSinTarea, ArrayList<Integer> citizens, int iTaskType, String sPriorityID) {
        TaskManagerItem item;
        int MAX_CARAVAN_TASKS = (World.getNumCitizens() * 30) / 100;
        if (MAX_CARAVAN_TASKS < 1) {
            MAX_CARAVAN_TASKS = 1;
        }
        int iCurrentCaravanTasks = 0;
        hmNonAvailableActions.clear();
        fortasks:
        for (int i = 0; i < taskItems.size(); i++) {
            if (hmCitizensSinTarea.size() == 0) {
                return;
            }
            item = taskItems.get(i);

            // Las tareas de equiparse ya están tratadas
            if (item.getTask().getTask() == Task.TASK_WEAR || item.getTask().getTask() == Task.TASK_WEAR_OFF || item.getTask().getTask() == Task.TASK_AUTOEQUIP) {
                continue;
            }

            if (iTaskType == TASK_TYPE_OTHERS) {
                // Miramos que sea una tarea distinta
                if (item.getTask().getTask() == Task.TASK_MINE || item.getTask().getTask() == Task.TASK_MINE_LADDER || item.getTask().getTask() == Task.TASK_BUILD || item.getTask().getTask() == Task.TASK_FOOD_NEEDED || item.getTask().getTask() == Task.TASK_MOVE_TO_CARAVAN) {
                    continue;
                }
            } else {
                // Miramos sólo las tareas del tipo pasado
                if (iTaskType == TASK_TYPE_MINEDIG) {
                    if (item.getTask().getTask() != Task.TASK_MINE && item.getTask().getTask() != Task.TASK_MINE_LADDER) {
                        continue;
                    }
                } else if (iTaskType == TASK_TYPE_BUILD_BUILDING) {
                    if (item.getTask().getTask() != Task.TASK_BUILD) {
                        continue;
                    }
                } else if (iTaskType == TASK_TYPE_FEED_ANIMALS) {
                    if (item.getTask().getTask() != Task.TASK_FOOD_NEEDED) {
                        continue;
                    }
                } else if (iTaskType == TASK_TYPE_MOVE_TO_CARAVAN) {
                    if (item.getTask().getTask() != Task.TASK_MOVE_TO_CARAVAN) {
                        continue;
                    }
                }
            }

			// Miramos si es tarea de construcción, en ese caso no asignamos aldeano si no hay items (o livings) en el mundo
            //System.out.println (item.getTask ().getParameter () + ", " + item.getTask ().getParameter2 ());
            if (item.getTask().getTask() == Task.TASK_BUILD) {
                Game.iError = 6671;
                // Tarea de construcción, miramos prerequisitos
                HotPoint hp = item.getTask().getHotPoint(0); // Punto 0.... las tareas de construcción sólo tienen 1

                Building building = Building.getBuilding(hp.getHotPoint().x, hp.getHotPoint().y, hp.getHotPoint().z);
                if (building == null) {
                    // Edificio borrado (agua, raise/lower, ...)
                    item.getTask().setFinished(true);
                    continue;
                }
                ArrayList<int[]> alPrerequisites = building.getPrerequisites();

                // Tenemos los prerequisitos
                boolean bItemPrerequisitesDone = false;
                if (alPrerequisites == null || alPrerequisites.size() == 0) {
                    bItemPrerequisitesDone = true;
                } else {
                    int iNumItems = 0;
                    for (int it = 0; it < alPrerequisites.get(0).length; it++) {
                        iNumItems += Item.getNumItems(alPrerequisites.get(0)[it], false, Game.getWorld ().getRestrictHaulEquippingLevel ());
                        if (iNumItems != 0) {
                            break;
                        }
                    }
                    if (iNumItems <= 0) {
                        // Si no hay materiales nos saltamos la tarea
                        continue;
                    } else {
                        // Si hay materiales miramos que nos vayan bien como prerequisito y que estén en el mismo A*ZoneID que el edificio
                        boolean bMatsEnZona = false;
                        for (int it = 0; it < alPrerequisites.size(); it++) {
                            bMatsEnZona = (Item.searchItem(true, building.getCoordinates(), alPrerequisites.get(it), false, Item.SEARCH_FALSE, Item.SEARCH_DOESNTMATTER, null, Game.getWorld ().getRestrictHaulEquippingLevel ()) != null);
                            if (bMatsEnZona) {
                                break;
                            }
                        }
                        if (!bMatsEnZona) {
                            continue;
                        }
                    }
                }

                if (bItemPrerequisitesDone) {
                    // Prerequisitos acabados (o inexistentes), miramos prerequisitos Living
                    alPrerequisites = building.getPrerequisitesLiving();
                    if (alPrerequisites != null && alPrerequisites.size() > 0) {
                        // Hay prerequisitos living, antes de asignar la tarea miramos que exista alguno de esos livings en la zona del edificio
                        boolean bLivingEnZona = false;
                        for (int li = 0; li < alPrerequisites.size(); li++) {
                            bLivingEnZona = (LivingEntity.searchLiving(building.getCoordinates(), alPrerequisites.get(li), false, null) != null);
                            if (bLivingEnZona) {
                                break;
                            }
                        }
                        if (!bLivingEnZona) {
                            // Si no hay living disponible, saltamos la tarea
                            continue;
                        }
                    } else {
						// No tiene prerequisitos, ya estamos (no debería pasar por aquí)
                        // Quizá no ha podido acabar el job pq había alguien sobando en el sitio o algo
                        // No hacemos nada para que asigne la tarea a alguien
                        // continue;
                    }
                }
            } else if (item.getTask().getTask() == Task.TASK_MOVE_TO_CARAVAN) {
                Game.iError = 66711;
                if (iCurrentCaravanTasks >= MAX_CARAVAN_TASKS) {
                    continue;
                }
                int iCaravanID = item.getTask().getPointIni().x;
                int iItemID = item.getTask().getPointIni().y;

                // Miramos que la caravana exista
                LivingEntity leCaravan = World.getLivingEntityByID(iCaravanID);
                if (leCaravan == null) {
                    item.getTask().setFinished(true);
                    continue;
                }

                if (iItemID != -1) {
                    // Item militar, miramos si existe
                    Point3DShort p3dMilitar = Item.searchItemByID(iItemID);
                    if (p3dMilitar == null) {
                        // No está actualmente en el mundo, quizá en el carrying de algún aldeano, de momento pasamos
                        continue;
                    } else {
                        // El item existe, podemos asignar tarea a algún aldeano (si está en la misma zona que la caravana)
                        int iCaravanASZID = World.getCell(leCaravan.getCoordinates()).getAstarZoneID();
                        if (World.getCell(p3dMilitar).getAstarZoneID() != iCaravanASZID) {
                            // Distinto ASZID
                            continue;
                        }

                        // Si llega aquí es que todo es ok
                        iCurrentCaravanTasks++;
                    }
                } else {
                    // Item normal, buscamos uno disponible
                    String sItemIniHeader = item.getTask().getParameter();
                    int[] aiHeaders = new int[1];
                    aiHeaders[0] = UtilsIniHeaders.getIntIniHeader(sItemIniHeader);
                    if (Item.searchItem(true, leCaravan.getCoordinates(), aiHeaders, true, Item.SEARCH_FALSE, Item.SEARCH_DOESNTMATTER, null, Game.getWorld ().getRestrictHaulEquippingLevel ()) == null) {
                        // Item NO encontrado, pasando de momento
                        continue;
                    }

                    // Si llega aquí es que todo es ok
                    iCurrentCaravanTasks++;
                }
            } else if (item.getTask().getTask() == Task.TASK_FOOD_NEEDED) {
                Game.iError = 66712;

                if (item.getTask().getParameter() == null) {
                    item.getTask().setFinished(true);
                    continue;
                }

                int iLivingID = Integer.parseInt(item.getTask().getParameter());

                // Miramos que el living exista
                LivingEntity leToFeed = World.getLivingEntityByID(iLivingID);
                if (leToFeed == null) {
                    // Si la living la tiene pillada alguien de momento no hacemos nada, en otro caso (ha muerto) eliminamos la tarea
                    Citizen cit;
                    for (int c = 0; c < World.getCitizenIDs().size(); c++) {
                        cit = (Citizen) World.getLivingEntityByID(World.getCitizenIDs().get(c));
                        if (cit != null && cit.getCarryingLiving() != null && cit.getCarryingLiving().getID() == iLivingID) {
                            // Cit con la living deseada, nos esperamos
                            continue;
                        }
                    }

                    // Si llega aquí es que nadie tiene la living, debe haber muerto
                    item.getTask().setFinished(true);
                    continue;
                }

                // Si llega aquí es que la living existe
                LivingEntityManagerItem lemi = LivingEntityManager.getItem(leToFeed.getIniHeader());

                // Miramos si el item a llevar existe
                if (lemi.getFoodNeeded() == null) {
                    // Curiosamente no tiene feed, quizá se ha modificado el .xml y el animal es antiguo, pacasa
                    item.getTask().setFinished(true);
                    continue;
                }

                Point3DShort p3dItem = Item.searchItem(true, leToFeed.getCoordinates(), lemi.getFoodNeeded(), true, Item.SEARCH_FALSE, Item.SEARCH_DOESNTMATTER, null, Game.getWorld ().getRestrictHaulEquippingLevel ());
                if (p3dItem == null) {
                    continue;
                }
            }

            Game.iError = 6672;

            int iCitizens = item.getListCitizens().size();
            int iMaxCitizens = item.getTask().getMaxCitizens();
            int iIndex;
            Point3DShort hPoint3D;
            Citizen citizen;
            boolean bTaskEnded = false;
            while (iCitizens < iMaxCitizens && !bTaskEnded) {
                if (hmCitizensSinTarea.size() == 0) {
                    return;
                }
                // Tarea le falta aldeano, buscamos el que esté más cerca
                iIndex = 0; // Marcará el índice de aldeano que está más cerca y que esté en la misma zona que alguno de sus places
                int iHPIndex = 0;
                HotPoint hotPoint;
                boolean bHotPointEncontrado = false;
                while (!bHotPointEncontrado && !bTaskEnded) {
                    Game.iError = 6673;
                    // Obtenemos un hotpoint
                    hotPoint = item.getTask().getHotPoints().get(iHPIndex);
                    hPoint3D = hotPoint.getHotPoint();
                    // Miramos que NO esté terminado y que ningún aldeano ya lo esté haciendo y que las places del hotpoint sean accesible por aldeanos
                    boolean bHotpointLibre = !hotPoint.isFinished();

                    if (bHotpointLibre) {
                        Game.iError = 66731;
                        // Miramos las places del hotpoint a ver si hay alguna accesible y con aldeanos que puedan llegar
                        int iPlacesAccesibles = 0;
                        for (int n = 0; n < hotPoint.getPlaces().size(); n++) {
                            hPoint3D = hotPoint.getPlaces().get(n);
                            int iASZID = World.getCells()[hPoint3D.x][hPoint3D.y][hPoint3D.z].getAstarZoneID();
                            if (iASZID != -1) {
                                ArrayList<Citizen> alCitTmp = hmCitizensSinTarea.get(Integer.valueOf(iASZID));

                                if (alCitTmp != null && alCitTmp.size() > 0 && Citizen.isCellAllowed(hPoint3D.x, hPoint3D.y, hPoint3D.z)) {
                                    iPlacesAccesibles++;
                                }
                            }
                        }
                        bHotpointLibre = iPlacesAccesibles > 0;

                        if (bHotpointLibre) {
                            Game.iError = 66732;
                            // Miramos si algún aldeano lo está haciendo
                            for (int n = 0; n < citizens.size(); n++) {
                                citizen = (Citizen) World.getLivingEntityByID(citizens.get(n));
                                if (citizen.getHotPointIndex() == iHPIndex) {
                                    if (citizen.getCurrentTask() != null && citizen.getCurrentTask().getID() == item.getTask().getID()) {
                                        // Aldeano con la misma tarea
                                        bHotpointLibre = false;
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    if (bHotpointLibre) { // Hotpoint sin terminar y sin nadie haciéndolo, le metemos el aldeano INACTIVO más cercano con A*ZoneID igual a alguna de sus places
                        Game.iError = 6674;

                        bHotPointEncontrado = true;
                        hPoint3D = hotPoint.getHotPoint();

                        // Para evitar stucks raros primero miramos esta casuistica (haul con hotpoint distinto de pointini)
                        if (item.getTask().getTask() == Task.TASK_HAUL || item.getTask().getTask() == Task.TASK_MOVE_AND_LOCK || item.getTask().getTask() == Task.TASK_PUT_IN_CONTAINER) {
                            if (!hPoint3D.equals(item.getTask().getPointIni()) || (World.getCell(hPoint3D).getAstarZoneID() == -1)) {
                                item.getTask().setFinished(true);
                                continue fortasks;
                            }
                        }

						// Obtenemos el arraylist de aldeanos con zoneID igual las places... blablabla
                        // Si hay places con distintos zoneIDs uniremos los 2 (o N) arrays de aldeanos en uno
                        ArrayList<Citizen> alCitizensSinTarea = new ArrayList<Citizen>();
                        Point3DShort p3dPlaces;
                        for (int c = 0; c < hotPoint.getPlaces().size(); c++) {
                            p3dPlaces = hotPoint.getPlaces().get(c);
                            ArrayList<Citizen> alCits = hmCitizensSinTarea.get(Integer.valueOf(World.getCells()[p3dPlaces.x][p3dPlaces.y][p3dPlaces.z].getAstarZoneID()));
                            if (alCits != null) { // Podría ser que no haya aldeanos en esa zona
                                for (int v = 0; v < alCits.size(); v++) {
                                    if (!alCitizensSinTarea.contains(alCits.get(v))) {
                                        alCitizensSinTarea.add(alCits.get(v));
                                    }
                                }
                            }
                        }

                        // Ya tenemos el array con todos los aldeanos que comparten A*ZoneID con las places
                        if (alCitizensSinTarea.size() == 0) {
                            Game.iError = 6675;
                            // Si no hay aldeanos, pacasa
                            bTaskEnded = true;
                        } else {
                            Game.iError = 6676;
                            // Obtenemos al aldeano más cercano
                            iIndex = getClosestCitizen(hPoint3D, alCitizensSinTarea, sPriorityID);

                            if (iIndex != -1) {
                                citizen = alCitizensSinTarea.remove(iIndex);
                                Integer iZoneID = Integer.valueOf(World.getCells()[citizen.getX()][citizen.getY()][citizen.getZ()].getAstarZoneID());
                                ArrayList<Citizen> alCits = hmCitizensSinTarea.get(iZoneID);
                                if (alCits != null) {
                                    alCits.remove(citizen);
                                    hmCitizensSinTarea.put(iZoneID, alCits);
                                }

                                citizen.setCurrentTask(item.getTask());
                                citizen.setHotPointIndex(iHPIndex);
                                item.addCitizen(citizen.getID());
                            } else {
                                // Si no hay aldeanos, pacasa
                                bTaskEnded = true;
                            }
                        }
                    } else {
                        Game.iError = 6677;
                        // Hotpoint terminado (o no accesible), miramos el siguiente
                        iHPIndex++;
                        if (iHPIndex >= item.getTask().getHotPoints().size()) {
                            bTaskEnded = true;
                        }
                    }
                }

                iCitizens = item.getListCitizens().size();
                iMaxCitizens = item.getTask().getMaxCitizens();
            }
        }
    }

    /**
     * Elimina una action de la lista También notifica al production panel
     *
     * @param iIndex
     */
    private Action removeAction(int iIndex, boolean backToTemp, HashMap<String, ArrayList<Integer>> hmNonAvailableActions) {
        return removeAction(iIndex, backToTemp, true, hmNonAvailableActions);
    }

    private Action removeAction(int iIndex, boolean backToTemp, boolean removeFromProdPanel, HashMap<String, ArrayList<Integer>> hmNonAvailableActions) {
        Action action = customActions.remove(iIndex);
        if (action != null) {
            if (backToTemp) {
                // customActionsTemp.add (action);
                customActionsWait.add(action);

                // Hash
                addToNonAvailableHash(action, hmNonAvailableActions);
            } else {
                if (removeFromProdPanel) {
                    removeFromProductionPanelRegular(action.getId());
                }
            }
        }

        return action;
    }

    public void removeFromProductionPanelRegular(String sID) {
        if (hmItemsOnRegularQueue.containsKey(sID)) {
            Integer iItem = hmItemsOnRegularQueue.get(sID);
            if (iItem != null) {
                int intValue = iItem.intValue();
                if (intValue > 1) {
                    hmItemsOnRegularQueue.put(sID, Integer.valueOf(iItem.intValue() - 1));
                } else {
                    hmItemsOnRegularQueue.remove(sID);
                }
            } else {
                hmItemsOnRegularQueue.remove(sID);
            }
        }
    }

    public void removeFromQueue(String sQueueID) {
        synchronized (alProductionToRemove) {
            alProductionToRemove.add(sQueueID);
        }
    }

    private void addToNonAvailableHash(Action action, HashMap<String, ArrayList<Integer>> hmNonAvailableActions) {
        Point3DShort p3dsDestination = action.getDestinationPoint();
        if (p3dsDestination != null) {
            int ASZID = World.getCell(p3dsDestination).getAstarZoneID();

            ArrayList<Integer> alASZID;
            if (hmNonAvailableActions.containsKey(action.getId())) {
                alASZID = hmNonAvailableActions.get(action.getId());
            } else {
                alASZID = new ArrayList<Integer>();
            }
            alASZID.add(Integer.valueOf(ASZID));
            hmNonAvailableActions.put(action.getId(), alASZID);
        }
    }

    private boolean isOnNonAvailableHash(Action action, HashMap<String, ArrayList<Integer>> hmNonAvailableActions) {
        Point3DShort p3dsDestination = action.getDestinationPoint();
        if (p3dsDestination != null) {
            if (hmNonAvailableActions.containsKey(action.getId())) {
                int ASZID = World.getCell(p3dsDestination).getAstarZoneID();

                ArrayList<Integer> alASZID = hmNonAvailableActions.get(action.getId());
                return (alASZID.contains(Integer.valueOf(ASZID)));
            }
        }

        return false;
    }

    /**
     * Asigna las custom actions, se le pasa la hash de aldeanos ociosos
     *
     * @param hmCitizensSinTarea
     */
    private int assignCustomActions(HashMap<Integer, ArrayList<Citizen>> hmCitizensSinTarea, int iLibres, int priorityIndex) {
        ArrayList<Citizen> alCits;
        Action action;
        Citizen citizen;
        Point3DShort p3dDestination = null;
        ActionManagerItem ami;

        int iIndex = 0;
        int iActionsProcesadas;
        if (iLibres > 0 && iLibres < MAX_CUSTOM_ACTIONS_PER_TURN_AND_PRIORITY_LEVEL) {
            iActionsProcesadas = iLibres;
        } else {
            iActionsProcesadas = 0;
        }

        // Hash de action(String) / lista de ASZID donde no se puede hacer (performance trick)
        hmNonAvailableActions.clear();

        // Recorremos las actions
        synchronized (customActions) {
            while (iActionsProcesadas < MAX_CUSTOM_ACTIONS_PER_TURN_AND_PRIORITY_LEVEL && iIndex < customActions.size()) {
                action = customActions.get(iIndex);
                if (priorityIndex != -1 && ActionPriorityManager.getPriorityValueByActionID(action.getId()) != priorityIndex) {
                    iIndex++;
                    continue;
                }

                Game.iError = 6762;
                iActionsProcesadas++;

                // Miramos que no esté en la hash de acciones no posibles
                if (isOnNonAvailableHash(action, hmNonAvailableActions)) {
                    continue;
                }
                ami = ActionManager.getItem(action.getId());
                alCits = null;
                // Queue
                QueueItem queueItem;

                // Antes de mirar nada miramos si es una QUEUE normal o una QUEUE_AND_PLACE
                if (action.getDestinationPoint() != null) {
                    boolean bBadAction = false;
					// Queue and place
                    // Miramos que el item a crear quepa en destino
                    for (int i = 0; i < action.getQueue().size(); i++) {
                        queueItem = action.getQueue().get(i);
                        if (queueItem.getType() == QueueItem.TYPE_CREATE_ITEM) {
                            // Tenemos el item, miramos si cabe
                            ItemManagerItem imi = ItemManager.getItem(queueItem.getValue());
                            if (imi == null) {
                                Log.log(Log.LEVEL_ERROR, Messages.getString("TaskManager.10") + queueItem.getValue() + "]", getClass().toString()); //$NON-NLS-1$ //$NON-NLS-2$
                                removeAction(iIndex, false, null);
                                bBadAction = true;
                                break;
                            }

                            if (!Item.isCellAvailableForItem(imi, action.getDestinationPoint(), true, true)) {
                                removeAction(iIndex, false, null);
                                bBadAction = true;
                                break;
                            }

                            break;
                        } else if (queueItem.getType() == QueueItem.TYPE_CREATE_ITEM_BY_TYPE) {
                            // Tenemos el item, miramos si cabe
                            ArrayList<String> alItems = ItemManager.getItemsByType(queueItem.getValue());
                            if (alItems == null || alItems.size() == 0) {
                                Log.log(Log.LEVEL_ERROR, Messages.getString("TaskManager.11") + " [" + queueItem.getValue() + "]", getClass().toString()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                removeAction(iIndex, false, null);
                                bBadAction = true;
                                break;
                            }

                            ItemManagerItem imi = ItemManager.getItem(alItems.get(0));
                            if (!Item.isCellAvailableForItem(imi, action.getDestinationPoint(), true, true)) {
                                removeAction(iIndex, false, null);
                                bBadAction = true;
                                break;
                            }

                            break;
                        }
                    }

                    if (bBadAction) {
                        World.getCell(action.getDestinationPoint()).setFlagOrders(false);
                        continue;
                    }
                }

                // Me aseguro que el primer "pick/pickLiving" y TODOS los "move" estén disponibles para el aldeano (de momento sólo pillo valores, más adelante lo miro)
                String sPickItemLiving = null;
                ArrayList<String> alMoveITem = new ArrayList<String>();
                int iFirstPickItemOrLiving = 0; // 1 - lo primero es un pick item, 2 - lo primero es un pick_living, 0 - no hay picks
                Point3DShort p3dForcedPick = null;
                boolean bDeleteTaskNoPicks = false;
                for (int i = 0; i < action.getQueue().size(); i++) {
                    queueItem = action.getQueue().get(i);
                    if (iFirstPickItemOrLiving == 0 && queueItem.getType() == QueueItem.TYPE_PICK) {
                        sPickItemLiving = queueItem.getValue();
                        iFirstPickItemOrLiving = 1;

                        // Si tiene usesource y ID obtenemos el punto
                        if (queueItem.isUseSource() && action.getEntityID() != -1) {
                            Item it = Item.getItemByID(action.getEntityID());
                            if (it != null) {
                                p3dForcedPick = it.getCoordinates();
                            } else {
                                bDeleteTaskNoPicks = true;
                                break;
                            }
                        }
                    } else if (iFirstPickItemOrLiving == 0 && queueItem.getType() == QueueItem.TYPE_PICK_FRIENDLY) {
                        sPickItemLiving = queueItem.getValue();
                        iFirstPickItemOrLiving = 2;

                        // Si tiene usesource y ID obtenemos el punto
                        if (queueItem.isUseSource() && action.getEntityID() != -1) {
                            LivingEntity le = World.getLivingEntityByID(action.getEntityID());
                            if (le != null) {
                                p3dForcedPick = le.getCoordinates();
                            } else {
                                bDeleteTaskNoPicks = true;
                                break;
                            }
                        }
                    } else if (queueItem.getType() == QueueItem.TYPE_MOVE) {
                        if (queueItem.isUseSource() && action.getEntityID() != -1) {
                            Item it = Item.getItemByID(action.getEntityID());
                            if (it != null) {
                                if (!alMoveITem.contains(it.getIniHeader())) {
                                    alMoveITem.add(it.getIniHeader());
                                }
                            } else {
                                LivingEntity le = World.getLivingEntityByID(action.getEntityID());
                                if (le != null) {
                                    if (!alMoveITem.contains(le.getIniHeader())) {
                                        alMoveITem.add(le.getIniHeader());
                                    }
                                }
                            }
                        } else if (!alMoveITem.contains(queueItem.getValue())) {
                            alMoveITem.add(queueItem.getValue());
                        }
                    }
                }

                if (bDeleteTaskNoPicks) {
                    removeAction(iIndex, false, null);
                    if (action.getDestinationPoint() != null) {
                        World.getCell(action.getDestinationPoint()).setFlagOrders(false);
                    }
                    continue;
                }

                // Cola chequeada, miramos que los items tengan el mismo A*zoneID y que existan (y operativas las de move)
                Point3DShort p3dMove = null;
                Point3DShort p3dPick = null;
                String sMoveMissing = null;
                // Obtenemos los A*ZID distintos a mirar
                ArrayList<Integer> alASZIDAMirar = new ArrayList<Integer>();
                ArrayList<Point3DShort> alASZIDAMirarCoordinates = new ArrayList<Point3DShort>();
                ArrayList<Integer> citizens = World.getCitizenIDs();
                int iCitASZID;
                for (int i = 0; i < citizens.size(); i++) {
                    iCitASZID = World.getCell(World.getLivingEntityByID(citizens.get(i)).getCoordinates()).getAstarZoneID();
                    if (!alASZIDAMirar.contains(Integer.valueOf(iCitASZID))) {
                        alASZIDAMirar.add(Integer.valueOf(iCitASZID));
                        alASZIDAMirarCoordinates.add(World.getLivingEntityByID(citizens.get(i)).getCoordinates());
                    }
                }

                // Si tiene forcepick y ningún aldeano tiene el mismo ASZID, pos p'acasa
                if (p3dForcedPick != null && !alASZIDAMirar.contains(World.getCell(p3dForcedPick).getAstarZoneID())) {
                    removeAction(iIndex, true, hmNonAvailableActions);
                    continue;
                }

                // Miramos si tiene terrain point y si está en un AZID válido
                Point3DShort p3dMoveTerrain = action.getTerrainPoint();
                if (p3dMoveTerrain != null) {
                    if (!alASZIDAMirar.contains(World.getCell(action.getTerrainPoint()).getAstarZoneID())) {
                        // Terrain point no accesible, saltamos la tarea
                        removeAction(iIndex, true, hmNonAvailableActions);
                        continue;
                    } else {
                        alASZIDAMirarCoordinates.add(p3dMoveTerrain);
                    }
                }

                if (alMoveITem.size() > 0) {
                    // Miraremos según los aldeanos (NO miramos los libres, que quizá no hay ninguno, miramos TODOS)
                    int iMovesEncontrados = 0;
                    Point3DShort p3d;
                    while (iMovesEncontrados < alMoveITem.size() && alASZIDAMirarCoordinates.size() > 0) {
                        iMovesEncontrados = 0;
                        sMoveMissing = null;
                        p3d = alASZIDAMirarCoordinates.remove(0);
                        for (int i = 0; i < alMoveITem.size(); i++) {
                            ArrayList<String> itemToSearch = Utils.getArray(alMoveITem.get(i));

                            // Point3D puntoMoveItem = Item.searchItem (p3d, itemToSearch, false, Item.SEARCH_TRUE, Item.SEARCH_TRUE, null, true); // Buscamos ordenadamente (el último true)
                            Point3DShort puntoMoveItem = Item.searchItem(false, p3d, UtilsIniHeaders.getIntsArray(itemToSearch), false, Item.SEARCH_DOESNTMATTER, Item.SEARCH_TRUE, null, true, false, Game.getWorld ().getRestrictHaulEquippingLevel ()); // Buscamos ordenadamente, evitamos containers
                            if (puntoMoveItem != null) {
                                iMovesEncontrados++;
                                if (p3dMove == null) {
                                    p3dMove = puntoMoveItem;
                                }
                            } else {
                                if (sMoveMissing == null) {
                                    sMoveMissing = alMoveITem.get(i);
                                }
                            }
                        }
                    }
                    if (sMoveMissing != null) {
                        if (!action.isSilent()) {
                            ArrayList<String> moveItems = Utils.getArray(sMoveMissing);
                            if (moveItems.size() > 0) {
                                String sName = ItemManager.getItem(moveItems.get(0)).getName();
                                for (int i = 1; i < moveItems.size(); i++) {
                                    sName += Messages.getString("TaskManager.9"); //$NON-NLS-1$
                                    sName += ItemManager.getItem(moveItems.get(i)).getName();
                                }
                                MessagesPanel.addMessage(MessagesPanel.TYPE_ANNOUNCEMENT, ami.getName() + Messages.getString("TaskManager.5") + sName + Messages.getString("TaskManager.2"), ColorGL.RED); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                        }
                        removeAction(iIndex, false, null);
                        if (action.getDestinationPoint() != null) {
                            World.getCell(action.getDestinationPoint()).setFlagOrders(false);
                        }
                        continue;
                    }
                }

                // Si llega aquí es que los moves están OK, miramos que el primero de la lista no esté en uso, en ese caso saltamos la tarea por el momento
                if (alMoveITem.size() > 0) {
                    // Miraremos según los aldeanos libres
                    ArrayList<String> itemsToSearch = Utils.getArray(alMoveITem.get(0));

                    // Obtenemos una lista de items "en uso" para que al buscarlos no nos de esos
                    ArrayList<Integer> alItemsInUse = Item.searchItemInUse(-1);

                    Iterator<Integer> itAldeanos = hmCitizensSinTarea.keySet().iterator();
                    boolean bPrimerMoveOK = false;
                    while (itAldeanos.hasNext()) {
                        alCits = hmCitizensSinTarea.get(itAldeanos.next());
                        if (alCits != null && alCits.size() > 0) {
                            // if (Item.searchItem (alCits.get (0).getCoordinates (), itemsToSearch, false, Item.SEARCH_TRUE, Item.SEARCH_TRUE, alItemsInUse) != null) {
                            if (Item.searchItem(true, alCits.get(0).getCoordinates(), UtilsIniHeaders.getIntsArray(itemsToSearch), false, Item.SEARCH_DOESNTMATTER, Item.SEARCH_TRUE, alItemsInUse, Game.getWorld ().getRestrictHaulEquippingLevel ()) != null) {
                                bPrimerMoveOK = true;
                                break;
                            }

                        }
                    }

                    if (!bPrimerMoveOK) {
                        // iIndex++;
                        removeAction(iIndex, true, hmNonAvailableActions);
                        continue;
                    }
                }

                // Moves OK, toca el pick
                if (sPickItemLiving != null) {
                    ArrayList<String> alPicks = Utils.getArray(sPickItemLiving);
                    int[] aiPicks = UtilsIniHeaders.getIntsArray(alPicks);

                    // Perfomance improvement
                    if (iFirstPickItemOrLiving == 1) { // Items
                        boolean bExisteAlguno = false;
                        for (int i = 0; i < aiPicks.length; i++) {
                            if (Item.getNumItems(aiPicks[i], false, Game.getWorld ().getRestrictHaulEquippingLevel ()) > 0) {
                                bExisteAlguno = true;
                                break;
                            }
                        }

                        if (!bExisteAlguno) {
                            // No existe el primer pick item
                            removeAction(iIndex, true, hmNonAvailableActions);
                            continue;
                        }
                    } else if (iFirstPickItemOrLiving == 2) { // Livings
                        boolean bExisteAlguno = false;
                        for (int i = 0; i < aiPicks.length; i++) {
                            if (LivingEntity.getNumLivings(alPicks.get(i), true) > 0) {
                                bExisteAlguno = true;
                                break;
                            }
                        }

                        if (!bExisteAlguno) {
                            // No existe el primer pick living
                            removeAction(iIndex, true, hmNonAvailableActions);
                            continue;
                        }
                    }

                    // Si hay moves hay que mirar que estén en la misma A*ZID
                    if (alMoveITem.size() > 0) {
                        // Tenemos move, el pick tiene que estar en la misma A*ZID
                        Point3DShort puntoPickItemLiving = null;
                        if (iFirstPickItemOrLiving == 1) {
                            // Lo primero es un item
                            puntoPickItemLiving = Item.searchItem(false, p3dMove, aiPicks, false, Item.SEARCH_FALSE, Item.SEARCH_DOESNTMATTER, null, Game.getWorld ().getRestrictHaulEquippingLevel ());
                        } else {
                            // Lo primero es una friendly
                            LivingEntity le = LivingEntity.searchLiving(p3dMove, UtilsIniHeaders.getIntsArray(alPicks), false, null);
                            if (le != null) {
                                puntoPickItemLiving = le.getCoordinates();
                            }
                        }
                        if (puntoPickItemLiving == null) {
                            // iIndex++;
                            removeAction(iIndex, true, hmNonAvailableActions);
                            continue;
                        }
                    } else {
                        // No había moves, miramos si hay moveTerrain
                        boolean bPickEncontrado = false;
                        if (p3dMoveTerrain != null) {
                            Cell cellTerrain = World.getCell(p3dMoveTerrain);
                            if (cellTerrain.isMined()) {
                                alCits = hmCitizensSinTarea.get(cellTerrain.getAstarZoneID());
                            } else {
                                if (p3dMoveTerrain.z > 0) {
                                    cellTerrain = World.getCell(p3dMoveTerrain.x, p3dMoveTerrain.y, p3dMoveTerrain.z - 1);
                                    alCits = hmCitizensSinTarea.get(cellTerrain.getAstarZoneID());
									// } else {
                                    // No se puede acceder
                                }
                            }
                            if (alCits != null && alCits.size() > 0) {
                                Point3DShort puntoPickItem = null;
                                if (iFirstPickItemOrLiving == 1) {
                                    puntoPickItem = Item.searchItem(false, alCits.get(0).getCoordinates(), aiPicks, false, Item.SEARCH_FALSE, Item.SEARCH_DOESNTMATTER, null, Game.getWorld ().getRestrictHaulEquippingLevel ());
                                } else {
                                    LivingEntity le = LivingEntity.searchLiving(alCits.get(0).getCoordinates(), UtilsIniHeaders.getIntsArray(alPicks), false, null);
                                    if (le != null) {
                                        puntoPickItem = le.getCoordinates();
                                    }
                                }
                                if (puntoPickItem != null) {
                                    bPickEncontrado = true;
                                    p3dPick = puntoPickItem;
                                    //break;
                                }
                            }
                        } else {
                            // No había moves, nos vale cualquier item, miraremos aldeanos libres
                            Iterator<Integer> itAldeanos = hmCitizensSinTarea.keySet().iterator();
                            while (!bPickEncontrado && itAldeanos.hasNext()) {
                                alCits = hmCitizensSinTarea.get(itAldeanos.next());
                                if (alCits != null && alCits.size() > 0) {
                                    Point3DShort puntoPickItem = null;
                                    if (iFirstPickItemOrLiving == 1) {
                                        puntoPickItem = Item.searchItem(false, alCits.get(0).getCoordinates(), aiPicks, false, Item.SEARCH_FALSE, Item.SEARCH_DOESNTMATTER, null, Game.getWorld ().getRestrictHaulEquippingLevel ());
                                    } else {
                                        LivingEntity le = LivingEntity.searchLiving(alCits.get(0).getCoordinates(), UtilsIniHeaders.getIntsArray(alPicks), false, null);
                                        if (le != null) {
                                            puntoPickItem = le.getCoordinates();
                                        }
                                    }
                                    if (puntoPickItem != null) {
                                        bPickEncontrado = true;
                                        p3dPick = puntoPickItem;
                                        //break;
                                    }
                                }
                            }
                        }

                        if (!bPickEncontrado) {
                            // iIndex++;
                            removeAction(iIndex, true, hmNonAvailableActions);
                            continue;
                        }
                    }
                }

                // Si llega aquí es que todo OK
                p3dDestination = null;
                if (p3dMoveTerrain != null) {
                    p3dDestination = p3dMoveTerrain;
                    alCits = hmCitizensSinTarea.get(Integer.valueOf(World.getCell(p3dDestination).getAstarZoneID()));
                } else if (p3dMove != null) {
                    p3dDestination = p3dMove;
                    alCits = hmCitizensSinTarea.get(Integer.valueOf(World.getCell(p3dDestination).getAstarZoneID()));
                } else if (p3dPick != null) {
                    p3dDestination = p3dPick;
                    alCits = hmCitizensSinTarea.get(Integer.valueOf(World.getCell(p3dDestination).getAstarZoneID()));
                } else {
                    // Ni move ni pick..... cualquier aldeano nos vale (que esté en el mismo A*ZID del destination !)
                    if (action.getDestinationPoint() != null && World.getCell(action.getDestinationPoint()).getAstarZoneID() != -1) {
                        alCits = hmCitizensSinTarea.get(Integer.valueOf(World.getCell(action.getDestinationPoint()).getAstarZoneID()));
                        if (alCits != null && alCits.size() > 0) {
                            p3dDestination = alCits.get(0).getCoordinates();
                        }

                        if (p3dDestination == null) {
                            // No hay aldeanos en la zona, miramos aldeanos en otras zonas adyacentes para ver si pueden construir desde otra celda
                            Point3DShort p3dNeighbour;
                            int iASZIDNeighbour;
                            bucleNeighbours:
                            for (int x = -1; x <= 1; x++) {
                                for (int y = -1; y <= 1; y++) {
                                    for (int z = -1; z <= 1; z++) {
                                        p3dNeighbour = Point3DShort.getPoolInstance(action.getDestinationPoint().x + x, action.getDestinationPoint().y + y, action.getDestinationPoint().z + z);
                                        if (Utils.isInsideMap(p3dNeighbour)) {
                                            iASZIDNeighbour = World.getCell(p3dNeighbour).getAstarZoneID();

                                            if (iASZIDNeighbour != -1) {
                                                alCits = hmCitizensSinTarea.get(iASZIDNeighbour);
                                                if (alCits != null && alCits.size() > 0) {
                                                    p3dDestination = alCits.get(0).getCoordinates();
                                                    break bucleNeighbours;
                                                }
                                            }
                                        }
                                        Point3DShort.returnToPool(p3dNeighbour);
                                    }
                                }
                            }
                        }
                    } else {
                        // Puentes? pillamos cualquiera (puede dar problemas esto)
                        Iterator<Integer> itAldeanos = hmCitizensSinTarea.keySet().iterator();
                        boolean bPickEncontrado = false;
                        while (!bPickEncontrado && itAldeanos.hasNext()) {
                            alCits = hmCitizensSinTarea.get(itAldeanos.next());
                            if (alCits != null && alCits.size() > 0) {
                                p3dDestination = alCits.get(0).getCoordinates();
                                break;
                            }
                        }
                    }
                }

                if (p3dDestination == null) {
                    alCits = null;
                }

                if (alCits != null && alCits.size() > 0) {
                    // Aldeanos encontrados, le metemos la custom action
                    int iIndexCit = getClosestCitizen(p3dDestination, alCits, ami.getPriorityID());
                    if (iIndexCit != -1) {
                        Task task = new Task(Task.TASK_CUSTOM_ACTION);
                        task.setParameter(action.getId());

                        citizen = alCits.remove(iIndexCit);
                        citizen.setCurrentTask(task);
                        citizen.setCurrentCustomAction(removeAction(iIndex, false, false, null));
                        iLibres--;
                        hmCitizensSinTarea.put(Integer.valueOf(World.getCell(citizen.getCoordinates()).getAstarZoneID()), alCits);

                        if (iLibres <= 0) {
                            // No más aldeanos libres
                            return 0;
                        }

                        // Tarea metida, la metemos en las non-available para que espere al menos 1 turno (por los <locks>)
                        addToNonAvailableHash(action, hmNonAvailableActions);
                    } else {
                        // No hay aldeano que nos valga
                        removeAction(iIndex, true, hmNonAvailableActions);
                    }
                } else {
                    // iIndex++;
                    removeAction(iIndex, true, hmNonAvailableActions);
                }
            }
        }

        return iLibres;
    }

    /**
     * Asigna tareas de create (construcción de items), se le pasa la hash de
     * aldeanos ociosos
     *
     * @param hmCitizensSinTarea
     */
    private int assignCreateTasks(HashMap<Integer, ArrayList<Citizen>> hmCitizensSinTarea, int iLibres) {
        ArrayList<Integer> citizens = World.getCitizenIDs();
        Citizen citizen;

        // Recorremos todos los edificios a ver si hay alguno con tarea de construcción y ningún aldeano haciéndola
        Building building;
        ArrayList<Citizen> alCits;
        breakBuildings:
        for (int i = 0; i < World.getBuildings().size(); i++) {
            building = World.getBuildings().get(i);
            if (building.isOperative() && building.hasItemsInQueue()) {
                BuildingManagerItem bmi = BuildingManager.getItem(building.getIniHeader());
                Point3DShort p3dEntrance = bmi.getEntranceBaseCoordinates().merge(building.getCoordinates());
                // Edificio con tareas, primero miramos si hay aldeanos en la zona y después miramos si no se está haciendo por otro aldeano
                int iBuildinigZoneID = World.getCells()[p3dEntrance.x][p3dEntrance.y][p3dEntrance.z].getAstarZoneID();
                alCits = hmCitizensSinTarea.get(Integer.valueOf(iBuildinigZoneID));

                if (alCits == null || alCits.size() == 0) {
                    // No hay aldeanos en la zona, siguiente edificio
                    continue breakBuildings;
                }

                for (int c = 0; c < citizens.size(); c++) {
                    citizen = (Citizen) World.getLivingEntityByID(citizens.get(c));
                    if (citizen.getCurrentTask() != null && (citizen.getCurrentTask().getTask() == Task.TASK_CREATE_AND_PLACE)) {
                        // Citizen con tarea de create, miramos si es en este edificio
                        if (p3dEntrance.equals(citizen.getCurrentTask().getPointIni())) {
                            // Existe un aldeano operando en el edificio, seguimos con el siguiente edificio
                            continue breakBuildings;
                        }
                    }
                }

				// Si llega aquí es que el edificio no tiene aldeano operando
                // Miramos si hay materiales en el mundo
                ArrayList<String> alprerequisites = building.getItemQueue().get(0).getPrerequisites();
                if (alprerequisites != null && alprerequisites.size() > 0) {
                    if (Item.searchItem(true, p3dEntrance, UtilsIniHeaders.getIntsArray(alprerequisites), false, Item.SEARCH_FALSE, Item.SEARCH_DOESNTMATTER, null, Game.getWorld ().getRestrictHaulEquippingLevel ()) == null) {
                        continue breakBuildings;
                    }
                }

                // Buscamos el aldeano más cercano al edificio
                int iIndexCit = getClosestCitizen(p3dEntrance, alCits, null);
                if (iIndexCit != -1) {
					// Todo OK
                    // Creamos la tarea
                    Task task = new Task(Task.TASK_CREATE_AND_PLACE);
                    task.setPointIni(p3dEntrance);
                    task.setPointEnd(p3dEntrance);
                    task.setParameter(building.getItemQueue().get(0).getIniHeader());

                    citizen = alCits.remove(iIndexCit);
                    citizen.setCurrentTask(task);
                    iLibres--;
                    hmCitizensSinTarea.put(Integer.valueOf(iBuildinigZoneID), alCits);

                    if (iLibres <= 0) {
                        // No más aldeanos libres
                        return 0;
                    }
                }
            }
        }

        return iLibres;
    }

    /**
     * Asigna tareas de equiparse/desequiparse, se le pasa la hash de aldeanos
     * ociosos
     *
     * @param hmCitizensSinTarea
     */
    private void assignWearWearOffTasks(HashMap<Integer, ArrayList<Citizen>> hmCitizensSinTarea) {
        // Recorremos todas las tareas buscando tareas de equip

        Task task;
        for (int i = 0; i < taskItems.size(); i++) {
            task = taskItems.get(i).getTask();
            if (task.getTask() == Task.TASK_WEAR) {
                Game.iError = Task.TASK_WEAR;
				// Tarea de equiparse, buscamos al aldeano, nos da igual si está ocupado o no
                // Comprobamos también que esté en la zona del item

                if (task.getParameter() == null || task.getPointIni() == null) {
                    // Raro, pero a la peña le peta en algún sitio (quizá el aldeano está muerto cuando clican en el menú de equipar o algo)
                    task.setFinished(true);
                    continue;
                }
                int iCitID = Integer.parseInt(task.getParameter());
                Point3D p3dItem = task.getPointIni();

                Citizen citizen = (Citizen) World.getLivingEntityByID(iCitID);
                Game.iError = 915;
                if (citizen != null) {
                    Game.iError = 916;
                    if (World.getCell(citizen.getCoordinates()).getAstarZoneID() == World.getCell(p3dItem).getAstarZoneID()) {
                        Game.iError = 917;
                        // Aldeano en la zona
                        if (citizen.isIdle()) {
                            Game.iError = 919;
                            // Ocioso, le metemos la tarea y lo sacamos de la lista de ociosos
                            citizen.setCurrentTask(task);

                            ArrayList<Citizen> alCits = hmCitizensSinTarea.get(World.getCell(citizen.getCoordinates()).getAstarZoneID());
                            if (alCits != null) {
                                // No debería pasar, controlando un nullpointer por eso
                                for (int o = 0; o < alCits.size(); o++) {
                                    if (alCits.get(o).getID() == citizen.getID()) {
                                        // Lo tenemos
                                        alCits.remove(o);
                                        break;
                                    }
                                }
                                hmCitizensSinTarea.put(World.getCell(citizen.getCoordinates()).getAstarZoneID(), alCits);
                            }
                        } else {
                            Game.iError = 920;
                            // No ocioso, si no está comiendo ni durmiendo ni equipándose lo sacamos de su tarea y le metemos la nueva
                            if (citizen.getCurrentTask() == null || (citizen.getCurrentTask().getTask() != Task.TASK_EAT && citizen.getCurrentTask().getTask() != Task.TASK_SLEEP && citizen.getCurrentTask().getTask() != Task.TASK_WEAR && citizen.getCurrentTask().getTask() != Task.TASK_WEAR_OFF && citizen.getCurrentTask().getTask() != Task.TASK_AUTOEQUIP && citizen.getCurrentTask().getTask() != Task.TASK_DROP && citizen.getCurrentTask().getTask() != Task.TASK_HEAL)) {
                                removeCitizen(citizen);
                                citizen.setCurrentTask(task);
                            }
                        }
                    } else {
                        Game.iError = 918;
                        // No está en la zona, fin de tarea
                        task.setFinished(true);
                        MessagesPanel.addMessage(MessagesPanel.TYPE_ANNOUNCEMENT, citizen.getCitizenData().getFullName() + Messages.getString("TaskManager.0"), ColorGL.ORANGE); //$NON-NLS-1$
                    }
                }
            } else if (task.getTask() == Task.TASK_WEAR_OFF) {
                Game.iError = Task.TASK_WEAR_OFF;
				// Tarea de desequiparse, buscamos al aldeano, nos da igual si está ocupado o no
                // Si que miramos que no tenga carrying
                // Truquito el pointini.x marca el ID de aldeano, el pointini.y marca el location del item
                int iCitID = task.getPointIni().x;

                Citizen citizen = (Citizen) World.getLivingEntityByID(iCitID);
                if (citizen != null) {
                    if (citizen.isIdle()) {
                        // Ocioso, le metemos la tarea y lo sacamos de la lista de ociosos
                        citizen.setCurrentTask(task);

                        ArrayList<Citizen> alCits = hmCitizensSinTarea.get(World.getCell(citizen.getCoordinates()).getAstarZoneID());
                        if (alCits != null) {
                            for (int o = 0; o < alCits.size(); o++) {
                                if (alCits.get(o).getID() == citizen.getID()) {
                                    // Lo tenemos
                                    alCits.remove(o);
                                    break;
                                }
                            }
                            hmCitizensSinTarea.put(World.getCell(citizen.getCoordinates()).getAstarZoneID(), alCits);
                        }
                    } else {
                        // No ocioso, si no está comiendo ni bebiendo ni desequipándose lo sacamos de su tarea y le metemos la nueva
                        if (citizen.getCurrentTask() == null || (citizen.getCurrentTask().getTask() != Task.TASK_EAT && citizen.getCurrentTask().getTask() != Task.TASK_SLEEP && citizen.getCurrentTask().getTask() != Task.TASK_WEAR && citizen.getCurrentTask().getTask() != Task.TASK_WEAR_OFF && citizen.getCurrentTask().getTask() != Task.TASK_AUTOEQUIP && citizen.getCurrentTask().getTask() != Task.TASK_DROP && citizen.getCurrentTask().getTask() != Task.TASK_HEAL)) {
                            if (citizen.getCarrying() == null) {
                                removeCitizen(citizen);
                                citizen.setCurrentTask(task);
                            }
                        }
                    }
                }
            } else if (task.getTask() == Task.TASK_AUTOEQUIP) {
                Game.iError = Task.TASK_AUTOEQUIP;
                // Tarea virtual de autoequip, buscamos al aldeano, nos da igual si está ocupado o no

                int iCitID = Integer.parseInt(task.getParameter());

                Citizen citizen = (Citizen) World.getLivingEntityByID(iCitID);
                if (citizen != null) {
                    // Aldeano en la zona
                    if (citizen.isIdle()) {
                        // Ocioso, le metemos la tarea y lo sacamos de la lista de ociosos
                        citizen.setCurrentTask(task);

                        ArrayList<Citizen> alCits = hmCitizensSinTarea.get(World.getCell(citizen.getCoordinates()).getAstarZoneID());
                        if (alCits != null) {
                            for (int o = 0; o < alCits.size(); o++) {
                                if (alCits.get(o).getID() == citizen.getID()) {
                                    // Lo tenemos
                                    alCits.remove(o);
                                    break;
                                }
                            }
                            hmCitizensSinTarea.put(World.getCell(citizen.getCoordinates()).getAstarZoneID(), alCits);
                        }
                    } else {
                        // No ocioso, si no está comiendo ni durmiendo ni curándose ni equipándose lo sacamos de su tarea y le metemos la nueva
                        if (citizen.getCurrentTask() == null || (citizen.getCurrentTask().getTask() != Task.TASK_EAT && citizen.getCurrentTask().getTask() != Task.TASK_SLEEP && citizen.getCurrentTask().getTask() != Task.TASK_WEAR && citizen.getCurrentTask().getTask() != Task.TASK_WEAR_OFF && citizen.getCurrentTask().getTask() != Task.TASK_AUTOEQUIP && citizen.getCurrentTask().getTask() != Task.TASK_DROP && citizen.getCurrentTask().getTask() != Task.TASK_HEAL)) {
                            removeCitizen(citizen);
                            citizen.setCurrentTask(task);
                        }
                    }
                }
            }
        }
    }

    /**
     * Asigna tareas de remover cosas de los containers
     *
     * @param hmCitizensSinTarea
     * @param iLibres
     * @return
     */
    private int assignRemoveFromContainerTasks(HashMap<Integer, ArrayList<Citizen>> hmCitizensSinTarea, int iLibres) {
        ArrayList<Container> containers = Game.getWorld().getContainers();
        Container container;
        ArrayList<Integer> citizens = World.getCitizenIDs();
        Citizen citizen;

        int MAX = containerIndex + MAX_CONTAINER_HAUL_PER_TURN;
        if (MAX > containers.size()) {
            MAX = containers.size();
        }
        for (int i = containerIndex; i < MAX; i++) {
            container = containers.get(i);

            if (container.isWrongItemsInside()) {
                // Container con cosas malas dentro, miramos si hay aldeanos en la zona
                Item itemContainer = World.getItems().get(container.getItemID());
                if (itemContainer != null) {
                    int iZoneContainer = Integer.valueOf(World.getCell(itemContainer.getCoordinates()).getAstarZoneID());
                    ArrayList<Citizen> alCits = hmCitizensSinTarea.get(iZoneContainer);
                    if (alCits != null && alCits.size() > 0) {
                        // Hay aldeanos en la zona, le metemos la tarea si no hay otro con la misma tarea
                        boolean bCitizenConMismaTarea = false;
                        for (int c = 0; c < citizens.size(); c++) {
                            citizen = (Citizen) World.getLivingEntityByID(citizens.get(c));
                            if (citizen.getCurrentTask() != null && citizen.getCurrentTask().getTask() == Task.TASK_REMOVE_FROM_CONTAINER) {
                                // Aldeano con tarea similar, miramos si es la misma
                                if (Integer.parseInt(citizen.getCurrentTask().getParameter()) == container.getItemID()) {
                                    // Misma tarea, saltamos
                                    bCitizenConMismaTarea = true;
                                    break;
                                }
                            }
                        }

                        if (!bCitizenConMismaTarea) {
							// Si nadie tiene la tarea se la metemos al aldeano más cerca del container
                            // Creamos la tarea
                            Task task = new Task(Task.TASK_REMOVE_FROM_CONTAINER);
                            task.setParameter(Integer.toString(container.getItemID()));

                            // Buscamos el aldeano más cercano al container
                            int iIndexCit = getClosestCitizen(itemContainer.getCoordinates(), alCits, ActionPriorityManager.PRIORITY_HAUL);
                            if (iIndexCit != -1) {
                                citizen = alCits.remove(iIndexCit);
                                citizen.setCurrentTask(task);
                                iLibres--;
                                hmCitizensSinTarea.put(Integer.valueOf(iZoneContainer), alCits);
                                if (iLibres <= 0) {
                                    // No más aldeanos libres
                                    return iLibres;
                                }
                            }
                        }
                    }
                }
            }
        }

        // Actualizamos el índice de containers
        containerIndex += MAX_CONTAINER_HAUL_PER_TURN;
        if (containerIndex >= Game.getWorld().getContainers().size()) {
            containerIndex = 0;
        }

        return iLibres;
    }

    /**
     * Asigna tareas de haul, se le pasa la hash de aldeanos ociosos
     *
     * @param hmCitizensSinTarea
     */
    private void assignHaulTasks(HashMap<Integer, ArrayList<Citizen>> hmCitizensSinTarea, int iLibres, int iHaulers) {
		// Las tareas de haul (put_in_container) usan algo de CPU, tampoco mucho (casi nada de hecho), pero hay que recorrerse todos los items del mundo (árboles inclusive)
        // Con lo cual en vez de hacerlo a cada turno esto lo haré cada 9
//		if (haulTurns >= MAX_HAUL_TURNS) {
        // Remove de containers
        Game.iError = 6701;
        iLibres = assignRemoveFromContainerTasks(hmCitizensSinTarea, iLibres);
        if (iLibres <= 0) {
            return;
        }
//			haulTurns = 0;
//		} else {
//			haulTurns++;
//		}

        ArrayList<Stockpile> stockpiles = Game.getWorld().getStockpiles();
        ArrayList<Container> containers = Game.getWorld().getContainers();

        Game.iError = 6702;
        if (stockpiles.size() > 0 || containers.size() > 0) {
            ArrayList<Integer> citizens = World.getCitizenIDs();
            Citizen citizen;
            Stockpile stockpile;
            Container container;
            ArrayList<Point3DShort> points;
            Point3DShort point;
            Cell cell;
            ItemManagerItem imi;

			// Existen stockpiles, procedemos
            // Vamos a recorrernos los items del mundo y ir viendo si está en una stockpile válida, en caso negativo asignaremos a un aldeano
            // POPO Integer[] aItems = World.getItems ().keySet ().toArray (new Integer [0]);
            Item item;
            ArrayList<Citizen> alCits;
            FocusData fd;
            ArrayList<Integer> aItems = Game.getWorld().getItemsToBeHauled();
            ArrayList<Integer> aItemsToDelete = new ArrayList<Integer>();
            int iMax = Math.min(MAX_HAUL_PER_TURN, iHaulers);
            int MAX_INDEX = haulIndex + iMax;
            if (MAX_INDEX > aItems.size()) {
                MAX_INDEX = aItems.size();
            }
            break1:
            for (int i = haulIndex; i < MAX_INDEX; i++) {
                item = World.getItems().get(aItems.get(i));
                if (item == null) {
                    // POPOWorld.getItems ().remove (aItems[i]);
                    aItemsToDelete.add(i);
                    continue;
                }

//				if (item.isLocked ()) {
//					aItemsToDelete.add (i);
//					continue;
//				}
                cell = World.getCell(item.getCoordinates());
                // Miramos si tiene type (para que pase de los bushes por ejemplo)
                imi = ItemManager.getItem(item.getIniHeader());
//				if (imi.getType () == null) {
//					aItemsToDelete.add (i);
//					continue;
//				}

                boolean bItemInTheCorrectPile = cell.hasStockPile() && (Stockpile.getStockpile(cell.getCoordinates()).itemAllowed(item.getIniHeader()));
                if (bItemInTheCorrectPile) {
                    aItemsToDelete.add(i);
                    continue;
                }
//				boolean bItemCanBeStockpiled = !cell.hasStockPile () || (!Stockpile.getStockpile (item.getCoordinates ()).itemAllowed (item.getIniHeader ()));
//				if (!bItemCanBeStockpiled) {
//					aItemsToDelete.add (i);
//					continue;
//				}
//				boolean bItemCanBeStacked = imi.isStackable () && !imi.isContainer ();
//				if (!bItemCanBeStacked) {
//					aItemsToDelete.add (i);
//					continue;
//				}

				// El item no está en una stockpile o está en una stockpile que no toca
                // Primero miramos que no haya una restricción por nivel
                if (item.getCoordinates().z > Game.getWorld().getRestrictHaulEquippingLevel()) {
                    continue;
                }

                // Miramos si hay aldeanos inactivos en el ID del item
                int iMatZoneID = cell.getAstarZoneID();
                alCits = hmCitizensSinTarea.get(Integer.valueOf(iMatZoneID));
                if (alCits != null && alCits.size() > 0) {
					// Hay aldeanos inactivos en esa zona, buscamos una stockpile/barril con puntos libres/espacio en esa zona

					// Primero miramos que no haya enemigo cerca
                    // Si EL ITEM tiene enemigo cerca pasamos
                    fd = LivingEntity.hasEnemyInLOS(alCits.get(0).getIniHeader(), item.getCoordinates(), 6, false);
                    if (fd != null && fd.getEntityID() != -1) { // TODO: 6 de LOS a piñon
                        continue;
                    }

                    // Después miramos que no haya un aldeano con tarea de HAUL de este item
                    boolean bCitizenConMismoHaul = false;
                    for (int c = 0; c < citizens.size(); c++) {
                        citizen = (Citizen) World.getLivingEntityByID(citizens.get(c));
                        if (citizen.getCurrentTask() != null && (citizen.getCurrentTask().getTask() == Task.TASK_HAUL || citizen.getCurrentTask().getTask() == Task.TASK_PUT_IN_CONTAINER)) {
                            // Citizen con tarea de haul/putInContainer, miramos si es la misma
                            if (item.getCoordinates().equals(citizen.getCurrentTask().getPointIni()) /* && item.getIniHeader ().equals (citizen.getCurrentTask ().getParameter ()) */) {
                                // Ya lo está moviendo alguien
                                bCitizenConMismoHaul = true;
                                break;
                            }
                        }
                    }

                    if (bCitizenConMismoHaul) {
                        continue;
                    }

                    // Nadie lo está hauleando, procedemos
                    boolean bContainerEncontrado = false;

                    if (imi.isStackable() && !imi.isContainer()) {
                        // Recorremos los containers
                        for (int j = 0; j < containers.size(); j++) {
                            // breakcontainers: for (int j = 0; j < containers.size (); j++) {
                            container = containers.get(j);
                            // Miramos si el item puede ir en ese container
                            if (container.itemAllowed(item)) {
                                // Container bueno, miramos si está en el mismo zone ID que el item
                                Item itemContainer = Item.getItemByID(container.getItemID());
                                if (itemContainer != null && itemContainer.getCoordinates() != null) {
                                    if (World.getCell(itemContainer.getCoordinates()).getAstarZoneID() == iMatZoneID) {
										// Misma zona, tarea que te vi al aldeano más cercano

                                        // Hay que mirar que no haya un aldeano MOVIENDO algo a ese container
                                        boolean bCitizenHauling = false;
                                        for (int c = 0; c < citizens.size(); c++) {
                                            citizen = (Citizen) World.getLivingEntityByID(citizens.get(c));
                                            if (citizen.getCurrentTask() != null && (citizen.getCurrentTask().getTask() == Task.TASK_HAUL || citizen.getCurrentTask().getTask() == Task.TASK_PUT_IN_CONTAINER)) {
                                                // Citizen con tarea de mover en container, miramos si está llevando algo a esa casilla
                                                if (itemContainer.getCoordinates().equals(citizen.getCurrentTask().getPointEnd())) {
                                                    // Ya lo está moviendo alguien
                                                    bCitizenHauling = true;
                                                    break;
                                                }
                                            }
                                        }
                                        if (!bCitizenHauling) {
                                            int iIndexCit = getClosestCitizen(item.getCoordinates(), alCits, ActionPriorityManager.PRIORITY_HAUL);
                                            if (iIndexCit != -1) {
                                                bContainerEncontrado = true;

                                                // Creamos la tarea
                                                Task task = new Task(Task.TASK_PUT_IN_CONTAINER);
                                                task.setPointIni(item.getCoordinates());
                                                task.setPointEnd(itemContainer.getCoordinates());
                                                task.setParameter(item.getIniHeader());

                                                // Buscamos el aldeano más cercano al item
                                                citizen = alCits.remove(iIndexCit);
                                                citizen.setCurrentTask(task);
                                                iLibres--;
                                                hmCitizensSinTarea.put(Integer.valueOf(iMatZoneID), alCits);
                                                if (iLibres <= 0) {
                                                    // No más aldeanos libres
                                                    break break1; // Fin
                                                }

                                                continue break1;
                                            }
                                            // break breakcontainers;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    boolean bStockEncontrada = false;
//POPO				if (bItemCanBeStockpiled) {
                    if (!bContainerEncontrado) {
							// Recorremos las stockpiles
                        // breakstockpiles: for (int j = 0; j < stockpiles.size (); j++) {
                        for (int j = 0; j < stockpiles.size(); j++) {
                            stockpile = stockpiles.get(j);
                            // Miramos si el item puede ir en ésta stockpile
                            if (!stockpile.isFull() && stockpile.itemAllowed(item.getIniHeader())) {
                                points = stockpile.getPoints();
                                for (int n = 0; n < points.size(); n++) {
                                    point = points.get(n);
                                    cell = World.getCell(point);
                                    if (!cell.hasEntity() && cell.getAstarZoneID() == iMatZoneID) {
											// Tenemos casilla libre en la stockpile en la misma zona que hay aldeanos inactivos, tarea que te vi al aldeano
                                        // que esté más cercano al item

                                        // Hay que mirar que no haya un aldeano MOVIENDO algo a esa casilla del stockpile
                                        boolean bCitizenHauling = false;
                                        for (int c = 0; c < citizens.size(); c++) {
                                            citizen = (Citizen) World.getLivingEntityByID(citizens.get(c));
                                            if (citizen.getCurrentTask() != null && (citizen.getCurrentTask().getTask() == Task.TASK_HAUL || citizen.getCurrentTask().getTask() == Task.TASK_PUT_IN_CONTAINER)) {
                                                // Citizen con tarea de haul, miramos si está llevando algo a esa casilla
                                                if (point.equals(citizen.getCurrentTask().getPointEnd())) {
                                                    // Ya lo está moviendo alguien
                                                    bCitizenHauling = true;
                                                    break;
                                                }
                                            }
                                        }

                                        if (!bCitizenHauling) {
                                            int iIndexCit = getClosestCitizen(item.getCoordinates(), alCits, ActionPriorityManager.PRIORITY_HAUL);
                                            if (iIndexCit != -1) {
                                                bStockEncontrada = true;
                                                // Creamos la tarea
                                                Task task = new Task(Task.TASK_HAUL);
                                                task.setPointIni(item.getCoordinates());
                                                task.setPointEnd(point);
                                                task.setParameter(item.getIniHeader());

                                                // Buscamos el aldeano más cercano al item
                                                citizen = alCits.remove(iIndexCit);
                                                citizen.setCurrentTask(task);
                                                iLibres--;
                                                hmCitizensSinTarea.put(Integer.valueOf(iMatZoneID), alCits);
                                                if (iLibres <= 0) {
                                                    // No más aldeanos libres
                                                    break break1; // Fin
                                                }

                                                // break breakstockpiles; // Seguimos con el siguiente material
                                                continue break1; // Seguimos con el siguiente material
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (!bStockEncontrada && World.getCell(item.getCoordinates()).hasStockPile()) {
                        // Si no ha encontrado stockpile lo movemos donde podamos, por ahí
                        int distancia = 0; // Índice para ir mirando casillas adyacentes
                        final int distanciaMAX = World.MAP_WIDTH;
                        boolean bFound = false;
                        busquedapunto:
                        while (distancia < distanciaMAX) {
                            distancia++;
                            for (int x = (item.getX() - distancia); x <= (item.getX() + distancia); x++) {
                                for (int y = (item.getY() - distancia); y <= (item.getY() + distancia); y++) {
                                    if (Math.abs((item.getX() - x)) == distancia || Math.abs((item.getY() - y)) == distancia) { // Para que sólo mire puntos exteriores del radio
                                        if (Utils.isInsideMap(x, y, item.getZ())) {
                                            cell = World.getCell(x, y, item.getZ());
                                            if (cell.isEmpty() && !cell.isFlagOrders() && cell.getAstarZoneID() == iMatZoneID && LivingEntity.isCellAllowed(cell)) {
                                                if (!cell.hasStockPile() || (Stockpile.getStockpile(cell.getCoordinates()).itemAllowed(item.getIniHeader()))) {
                                                    // Punto libre encontrado
                                                    int iIndexCit = getClosestCitizen(item.getCoordinates(), alCits, ActionPriorityManager.PRIORITY_HAUL);

                                                    if (iIndexCit != -1) {
                                                        Task task = new Task(Task.TASK_HAUL);
                                                        task.setPointIni(item.getCoordinates());
                                                        task.setPointEnd(new Point3D(x, y, item.getZ()));
                                                        task.setParameter(item.getIniHeader());

                                                        // Buscamos el aldeano más cercano al item
                                                        citizen = alCits.remove(iIndexCit);
                                                        citizen.setCurrentTask(task);
                                                        iLibres--;
                                                        hmCitizensSinTarea.put(Integer.valueOf(iMatZoneID), alCits);
                                                        if (iLibres <= 0) {
                                                            // No más aldeanos libres
                                                            break break1; // Fin
                                                        }
                                                        bFound = true;
                                                        break busquedapunto;
                                                    } else {
															// No hay aldeanos, no hace falta mirar más posiciones
                                                        // Saltamos al siguiente item
                                                        continue break1;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

							// En todo el nivel no hay ni un hueco, haremos el truqui de buscar en un radio de 8x8 (por ejemplo) a partir
                        // de la posición de otros aldeanos
                        if (!bFound) {
                            int iNivelMalo = item.getCoordinates().z;

                            // Recorremos los aldeanos
                            ArrayList<Integer> citIDs = World.getCitizenIDs();
                            LivingEntity le;
                            Cell cellCit;
                            forcits:
                            for (int c = 0; c < citIDs.size(); c++) {
                                le = World.getLivingEntityByID(citIDs.get(c));
                                if (le.getCoordinates().z != iNivelMalo) {
                                    cellCit = World.getCell(le.getCoordinates());
                                    if (cellCit.getAstarZoneID() == iMatZoneID) {
                                        // Vamos bien, buscamos en un radio de 8x8
                                        final int distanciaMAXCITS = 8;
                                        distancia = 0;
                                        bFound = false;
                                        while (distancia < distanciaMAXCITS) {
                                            distancia++;
                                            for (int x = (le.getX() - distancia); x <= (le.getX() + distancia); x++) {
                                                for (int y = (le.getY() - distancia); y <= (le.getY() + distancia); y++) {
                                                    if (Math.abs((le.getX() - x)) == distancia || Math.abs((le.getY() - y)) == distancia) { // Para que sólo mire puntos exteriores del radio
                                                        if (Utils.isInsideMap(x, y, le.getZ())) {
                                                            cell = World.getCell(x, y, le.getZ());
                                                            if (cell.isEmpty() && !cell.isFlagOrders() && cell.getAstarZoneID() == iMatZoneID && LivingEntity.isCellAllowed(cell)) {
                                                                if (!cell.hasStockPile() || (Stockpile.getStockpile(cell.getCoordinates()).itemAllowed(item.getIniHeader()))) {
                                                                    // Punto libre encontrado
                                                                    int iIndexCit = getClosestCitizen(item.getCoordinates(), alCits, ActionPriorityManager.PRIORITY_HAUL);

                                                                    if (iIndexCit != -1) {
                                                                        Task task = new Task(Task.TASK_HAUL);
                                                                        task.setPointIni(item.getCoordinates());
                                                                        task.setPointEnd(new Point3D(x, y, le.getZ()));
                                                                        task.setParameter(item.getIniHeader());

                                                                        // Buscamos el aldeano más cercano al item
                                                                        citizen = alCits.remove(iIndexCit);
                                                                        citizen.setCurrentTask(task);
                                                                        iLibres--;
                                                                        hmCitizensSinTarea.put(Integer.valueOf(iMatZoneID), alCits);
                                                                        if (iLibres <= 0) {
                                                                            // No más aldeanos libres
                                                                            break break1; // Fin
                                                                        }
                                                                        break forcits;
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    //}

                }
            }

            // Borramos los items que ya no toca
            for (int i = (aItemsToDelete.size() - 1); i >= 0; i--) {
                Game.getWorld().removeItemToBeHauledByPosition(aItemsToDelete.get(i));
            }

            // Cambiamos el haul index
            haulIndex += iMax;
            if (haulIndex >= Game.getWorld().getItemsToBeHauled().size()) {
                haulIndex = 0;
            }
        }
    }

    /**
     * Cuenta el número de aldeanos ociosos
     *
     * @param hmCitizensSinTarea
     * @return
     */
    private int getNumCitizens(HashMap<Integer, ArrayList<Citizen>> hmCitizensSinTarea) {
        Iterator<Integer> iterator = hmCitizensSinTarea.keySet().iterator();
        Integer iZoneID;
        int iLibres = 0;
        while (iterator.hasNext()) {
            iZoneID = iterator.next();
            if (hmCitizensSinTarea.get(iZoneID) != null) {
                iLibres += hmCitizensSinTarea.get(iZoneID).size();
            }
        }

        return iLibres;
    }

    /**
     * Cuenta el número de aldeanos haulers ociosos
     *
     * @param hmCitizensSinTarea
     * @return
     */
    private int getNumCitizensHaul(HashMap<Integer, ArrayList<Citizen>> hmCitizensSinTarea) {
        Iterator<Integer> iterator = hmCitizensSinTarea.keySet().iterator();
        Integer iZoneID;
        int iLibres = 0;
        ArrayList<Citizen> alCits;
        int iHaulID = UtilsIniHeaders.getIntIniHeader(ActionPriorityManager.PRIORITY_HAUL);
        while (iterator.hasNext()) {
            iZoneID = iterator.next();
            alCits = hmCitizensSinTarea.get(iZoneID);
            if (alCits != null) {
                // Contamos los que puedan hacer haul
                Citizen cit;
                for (int i = 0; i < alCits.size(); i++) {
                    cit = alCits.get(i);
                    if (cit != null && cit.getCitizenData() != null && !cit.getCitizenData().containsDeniedJob(iHaulID)) {
                        iLibres++;
                    }
                }
            }
        }

        return iLibres;
    }

    /**
     * Retorna el índice, dentro de un array, del aldeano más cercano al punto
     * pasado
     *
     * @param p3d Punto
     * @param alCits Lista de Citiens
     * @return el índice, dentro del array, del aldeano más cercano al punto
     * pasado, -1 en caso de no encontrarse
     */
//	private int getClosestCitizen (Point3DShort p3d, ArrayList<Citizen> alCits) {
//		return getClosestCitizen (p3d, alCits, null);
//	}
    /**
     * Retorna el índice, dentro de un array, del aldeano más cercano al punto
     * pasado
     *
     * @param p3d Punto
     * @param alCits Lista de Citiens
     * @param sPriorityID ID de la prioridad, si se le pasa buscará aldeanos que
     * no tengan esa priority (job) denegada
     * @return el índice, dentro del array, del aldeano más cercano al punto
     * pasado, -1 en caso de no encontrarse
     */
    private int getClosestCitizen(Point3DShort p3d, ArrayList<Citizen> alCits, String sPriorityID) {
        if (alCits == null || alCits.size() == 0) {
            return -1;
        }

        if (alCits.size() == 1) {
            if (sPriorityID == null) {
                return 0;
            } else {
                if (alCits.get(0).getCitizenData().containsDeniedJob(sPriorityID)) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }

        if (sPriorityID == null) {
            Game.iError = 866;
            // Pillamos el primer aldeano como base
            int iHeur = Utils.getDistance(alCits.get(0).getCoordinates(), p3d);
            int iHeurTemp;
            int iIndex = 0;
            for (int c = 1; c < alCits.size(); c++) {
                iHeurTemp = Utils.getDistance(alCits.get(c).getCoordinates(), p3d);
                if (iHeurTemp < iHeur) {
                    iIndex = c;
                    iHeur = iHeurTemp;
                }
            }
            Game.iError = 867;

            return iIndex;
        } else {
            Game.iError = 868;
            int iIndexConJob = -1;
            int iIniHeaderJob = UtilsIniHeaders.getIntIniHeader(sPriorityID);
            // Pillamos el primer aldeano como base que no tenga el job denegado
            for (int i = 0; i < alCits.size(); i++) {
                if (!alCits.get(i).getCitizenData().containsDeniedJob(iIniHeaderJob)) {
                    iIndexConJob = i;
                    break;
                }
            }

            if (iIndexConJob == -1) {
                return -1;
            }

            // Tenemos el primer aldeano que puede hacer el job
            int iHeur = Utils.getDistance(alCits.get(iIndexConJob).getCoordinates(), p3d);
            int iHeurTemp;
            int iIndex = iIndexConJob;
            for (int c = (iIndexConJob + 1); c < alCits.size(); c++) {
                if (!alCits.get(c).getCitizenData().containsDeniedJob(iIniHeaderJob)) {
                    iHeurTemp = Utils.getDistance(alCits.get(c).getCoordinates(), p3d);
                    if (iHeurTemp < iHeur) {
                        iIndex = c;
                        iHeur = iHeurTemp;
                    }
                }
            }
            Game.iError = 869;

            return iIndex;
        }
    }

    /**
     * Obtiene el ID más alto de una tarea y pone ese ID+1 como el actual ID Se
     * usa al cargar una partida, para no tener taskID's repetidos
     */
    public void updateTaskIndexID() {
        int iMaxID = 0;

        for (int i = 0; i < taskItems.size(); i++) {
            if (taskItems.get(i).getTask().getID() > iMaxID) {
                iMaxID = taskItems.get(i).getTask().getID();
            }
        }

        Task.ID_INDEX = (iMaxID + 1);
    }

    /**
     * Marca un hotpoint como acabado. Mira si hay más, en caso contrario marca
     * la tarea como acabada. Los aldeanos la llaman cuando acaban su tarea.
     *
     * @param task Tarea
     * @param hotPointIndex Índice de hotpoint
     * @return true si hay más hotpoints por hacer
     */
    public boolean setHotPointFinished(Task task, int hotPointIndex) {
        task.getHotPoints().get(hotPointIndex).setFinished(true);

        task.setMaxCitizens(task.getMaxCitizens() - 1);

        // Miramos si quedan hotpoints
        int hpSize = task.getHotPoints().size();
        for (int i = 0; i < hpSize; i++) {
            if (!task.getHotPoints().get(i).isFinished()) {
                // Encontramos un HP NO acabado, devolvemos true para indicar que aún quedan cosas por hacer
                return true;
            }
        }

        // Si llega aquí es que no hay más hotpoints, tarea finalizada
        task.setFinished(true);
        return false;
    }

    public String toString() {
        TaskManagerItem tmi;
        StringBuffer buffer = new StringBuffer("TaskManager\n"); //$NON-NLS-1$
        buffer.append("Tasks\n"); //$NON-NLS-1$
        for (int i = 0; i < getTaskItems().size(); i++) {
            tmi = getTaskItems().get(i);
            buffer.append(tmi.toString());
        }
        buffer.append("Temp tasks\n"); //$NON-NLS-1$
        for (int i = 0; i < getTaskItemsTemp().size(); i++) {
            tmi = getTaskItemsTemp().get(i);
            buffer.append(tmi.toString());
        }
        buffer.append("Custom actions\n"); //$NON-NLS-1$
        Action action;
        for (int i = 0; i < getCustomActions().size(); i++) {
            action = getCustomActions().get(i);
            buffer.append(action.toString());
        }
        buffer.append("Custom actions temp\n"); //$NON-NLS-1$
        for (int i = 0; i < getCustomActionsTemp().size(); i++) {
            action = getCustomActionsTemp().get(i);
            buffer.append(action.toString());
        }
        buffer.append("Custom actions wait\n"); //$NON-NLS-1$
        for (int i = 0; i < getCustomActionsWait().size(); i++) {
            action = getCustomActionsWait().get(i);
            buffer.append(action.toString());
        }
        return buffer.toString();
    }

    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        taskItems = (ArrayList<TaskManagerItem>) in.readObject();
        taskItemsTemp = (ArrayList<TaskManagerItem>) in.readObject();
        customActions = (ArrayList<Action>) in.readObject();
        customActionsTemp = (ArrayList<Action>) in.readObject();
        customActionsWait = (ArrayList<Action>) in.readObject();
        reCheckMinePlaces = in.readBoolean();
        hmItemsOnRegularQueue = (HashMap<String, Integer>) in.readObject();
        hmItemsOnAutomatedQueue = (HashMap<String, Integer>) in.readObject();
        automatedQueueTurns = in.readInt();
        alProductionToRemove = (ArrayList<String>) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(taskItems);
        out.writeObject(taskItemsTemp);
        out.writeObject(customActions);
        out.writeObject(customActionsTemp);
        out.writeObject(customActionsWait);
        out.writeBoolean(reCheckMinePlaces);
        out.writeObject(hmItemsOnRegularQueue);
        out.writeObject(hmItemsOnAutomatedQueue);
        out.writeInt(automatedQueueTurns);
        out.writeObject(alProductionToRemove);
    }
}
