package xaos.tiles.entities.living;

import java.awt.Color;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import xaos.TownsProperties;

import xaos.actions.Action;
import xaos.actions.ActionManager;
import xaos.actions.ActionManagerItem;
import xaos.actions.QueueData;
import xaos.actions.QueueItem;
import xaos.campaign.TutorialTrigger;
import xaos.data.CaravanData;
import xaos.data.CitizenData;
import xaos.data.EventData;
import xaos.data.SoldierData;
import xaos.data.SoldierGroupData;
import xaos.data.SoldierGroups;
import xaos.effects.EffectManager;
import xaos.events.EventManager;
import xaos.events.EventManagerItem;
import xaos.main.Game;
import xaos.main.World;
import xaos.panels.CommandPanel;
import xaos.panels.MessagesPanel;
import xaos.panels.menus.SmartMenu;
import xaos.stockpiles.Stockpile;
import xaos.tasks.HotPoint;
import xaos.tasks.Task;
import xaos.tiles.Cell;
import xaos.tiles.entities.Entity;
import xaos.tiles.entities.buildings.Building;
import xaos.tiles.entities.buildings.BuildingManager;
import xaos.tiles.entities.buildings.BuildingManagerItem;
import xaos.tiles.entities.items.Container;
import xaos.tiles.entities.items.Item;
import xaos.tiles.entities.items.ItemManager;
import xaos.tiles.entities.items.ItemManagerItem;
import xaos.tiles.entities.items.military.MilitaryItem;
import xaos.tiles.entities.items.military.PrefixSuffixManager;
import xaos.tiles.entities.living.friendly.Friendly;
import xaos.tiles.entities.living.heroes.Hero;
import xaos.tiles.terrain.TerrainManager;
import xaos.tiles.terrain.TerrainManagerItem;
import xaos.utils.ColorGL;
import xaos.utils.Log;
import xaos.utils.Messages;
import xaos.utils.Point3D;
import xaos.utils.Point3DShort;
import xaos.utils.Utils;
import xaos.utils.UtilsAL;
import xaos.utils.UtilsIniHeaders;
import xaos.zones.Zone;
import xaos.zones.ZoneManager;
import xaos.zones.ZoneManagerItem;

public class Citizen extends LivingEntity implements Externalizable {

    private static final long serialVersionUID = -3243228563558407857L;

    // Datos del citizen (nombre, sexo, ...)
    CitizenData citizenData;

    // Datos si es soldado (estado (patrol, guard, ...), contadores, ...)
    SoldierData soldierData;

    // Tareas
    private Task currentTask;
    private int hotPointIndex;
    private int placesIndex;

    // Custom action
    private Action currentCustomAction; // Acción que el aldeano está haciendo

    // Exclamation
    private int showExclamationTurns;

    public Citizen() {
        super();
    }

    // Constructor principal, actualiza el ID del ciudadano
    public Citizen(String iniHeader) {
        super(iniHeader);

        setWaitingForPath(false);

        // Data (nombre, vida, ...)
        citizenData = new CitizenData(LivingEntityManager.getItem(getIniHeader()));
        Game.getWorld().getCitizenGroups().addCitizenToGroup(getID(), -1);

        getCitizenData().setHungry(getCitizenData().getMaxHungry() * 6); // Al crearse los aldeanos llegan con fuerzas, por eso el multiplicador

        soldierData = new SoldierData(getID());
        getLivingEntityData().setName(citizenData.getFullName());

        resetTaskIndexes();
    }

    /**
     * Retorna una instancia de aldean@ (50% male 50% female)
     *
     * @return una instancia de aldean@ (50% male 50% female)
     */
    public static Citizen getInstance() {
        return new Citizen(LivingEntityManager.getRandomItemByType(TYPE_CITIZEN).getIniHeader());
    }

    public Task getCurrentTask() {
        return currentTask;
    }

    public void setCurrentTask(Task currentTask) {
        this.currentTask = currentTask;
    }

    public int getHotPointIndex() {
        return hotPointIndex;
    }

    public void setHotPointIndex(int hotPointIndex) {
        this.hotPointIndex = hotPointIndex;
        setPlacesIndex(-1);
    }

    public int getPlacesIndex() {
        return placesIndex;
    }

    public void setPlacesIndex(int placesIndex) {
        this.placesIndex = placesIndex;
    }

    public void setCurrentCustomAction(Action currentCustomAction) {
        this.currentCustomAction = currentCustomAction;

        // Cuando le meten una custom action, en caso de QUEUE bloquearemos el primer <move> de la lista
        if (this.currentCustomAction != null && getCurrentTask() != null && getCurrentTask().getParameter() != null) {
            ActionManagerItem ami = ActionManager.getItem(getCurrentTask().getParameter());
            if (ami != null) {
                // Cola
                if (getCurrentCustomAction().getQueue().size() == 0) {
                    // Finish
                    getCurrentTask().setFinished(true);
                    Game.getWorld().getTaskManager().removeCitizen(this);
                    return;
                }

				// Primero de todo vamos a bloquear el primer <move> para que no vayan 5 aldeanos a hacer una tarea si sólo hay 2 benches (por ejemplo)
                // Si no hay <move> pues perfecto, no hacemos nada
                if (getCurrentCustomAction().getQueueData().getItemIDCurrentPlace() == -1) {
                    String firstMove = null;
                    int iFirstSource = -1;
                    for (int i = 0; i < getCurrentCustomAction().getQueue().size(); i++) {
                        if (getCurrentCustomAction().getQueue().get(i).getType() == QueueItem.TYPE_MOVE) {
                            firstMove = getCurrentCustomAction().getQueue().get(i).getValue();
                            if (getCurrentCustomAction().getQueue().get(i).isUseSource()) {
                                iFirstSource = getCurrentCustomAction().getEntityID();
                            }
                            break;
                        }
                    }

                    // Si tenemos move lo bloqueamos
                    if (firstMove != null) {
                        if (iFirstSource != -1) {
                            Item item = Item.getItemByID(iFirstSource);
                            if (item != null) {
                                getCurrentCustomAction().getQueueData().setItemIDCurrentPlace(iFirstSource);
                            }
                        }
                    } else {
                        ArrayList<Integer> alItemsInUse = Item.searchItemInUse(getID());
                        int[] aiMoves = new int[1];
                        aiMoves[0] = UtilsIniHeaders.getIntIniHeader(firstMove);
                        Point3DShort p3dMoveCoordenadas = Item.searchItem(false, getCoordinates(), aiMoves, true, Item.SEARCH_DOESNTMATTER, Item.SEARCH_TRUE, alItemsInUse, true, false, Game.getWorld ().getRestrictHaulEquippingLevel ());
                        if (p3dMoveCoordenadas != null) {
                            Item itemMove = World.getCell(p3dMoveCoordenadas).getItem();
                            if (itemMove != null) {
                                getCurrentCustomAction().getQueueData().setItemIDCurrentPlace(itemMove.getID());
                            }
                        }
                    }

                }
            }
        }
    }

    public void setCurrentCustomAction(Action currentCustomAction, String sOldActionID) {
        setCurrentCustomAction(currentCustomAction);

        if (sOldActionID != null) {
            // Quitamos la tarea del production panel
            Game.getWorld().getTaskManager().removeFromProductionPanelRegular(sOldActionID);
        }
    }

    public Action getCurrentCustomAction() {
        return currentCustomAction;
    }

    protected void doTasksStuff() {
        if (getCurrentTask() != null) {
			// Tiene tarea

            // Miramos si es tarea de construcción
            if (getCurrentTask().getTask() == Task.TASK_CUSTOM_ACTION) {
                doCustomActionTask();
            } else if (getCurrentTask().getTask() == Task.TASK_BUILD) {
                doBuildingTask();
            } else if (getCurrentTask().getTask() == Task.TASK_HAUL) {
                doHaulingTask(false, false, false);
            } else if (getCurrentTask().getTask() == Task.TASK_MOVE_AND_LOCK) {
                doHaulingTask(true, false, true);
            } else if (getCurrentTask().getTask() == Task.TASK_PUT_IN_CONTAINER) {
                doHaulingTask(false, true, false);
            } else if (getCurrentTask().getTask() == Task.TASK_REMOVE_FROM_CONTAINER) {
                doRemoveFromContainerTask();
            } else if (getCurrentTask().getTask() == Task.TASK_DROP) {
                doDropTask();
            } else if (getCurrentTask().getTask() == Task.TASK_MOVE_TO_CARAVAN) {
                doMoveToCaravan();
            } else if (getCurrentTask().getTask() == Task.TASK_FOOD_NEEDED) {
                doFoodNeeded();
            } else if (getCurrentTask().getTask() == Task.TASK_CREATE_AND_PLACE) {
                doCreateAndPlaceTask();
            } else if (getCurrentTask().getTask() == Task.TASK_EAT) {
                doEatTask();
            } else if (getCurrentTask().getTask() == Task.TASK_HEAL) {
                doHealTask();
            } else if (getCurrentTask().getTask() == Task.TASK_SLEEP) {
                doSleepTask();
				// } else if (getCurrentTask ().getTask () == Task.TASK_FIGHT) {
                // doFightTask ();
            } else if (getCurrentTask().getTask() == Task.TASK_WEAR) {
                doWearTask();
            } else if (getCurrentTask().getTask() == Task.TASK_WEAR_OFF) {
                doWearOffTask();
            } else if (getCurrentTask().getTask() == Task.TASK_AUTOEQUIP) {
                doAutoEquipTask();
            } else {
                doGenericTask();
            }

        } else {
            // Quitamos los índices de tarea, no fuera que la tarea la haya terminado otro aldeano y éste aún esté intentando hacerla
            resetTaskIndexes();

            // Ocioso, lo movemos a random
            executeDropLivingTask();
            if (getCarrying() != null) {
                dropCarryingItem(true);
            } else {
                // Antes de moverlo miramos que no sea soldado, en ese caso ejecutamos sus tareas
                if (getSoldierData().isSoldier()) {
                    doSoldierTasks();
                } else {
                    // Aldeano idle, miramos si hay alguna social zone para moverlo por ahí
                    Zone zone;
                    boolean bAldeanoEnZonaSocial = World.getCell(getCoordinates()).hasZone();
                    if (bAldeanoEnZonaSocial) {
                        // Está en una zona, miramos si es social
                        zone = Zone.getZone(World.getCell(getCoordinates()).getZoneID());
                        if (zone != null && ZoneManager.getItem(zone.getIniHeader()).getType() == ZoneManagerItem.TYPE_SOCIAL) {
                            // Ya está en una social, no hacemos nada
                        } else {
                            bAldeanoEnZonaSocial = false;
                        }
                    }

                    if (!bAldeanoEnZonaSocial) {
                        if (Utils.getRandomBetween(1, 200) == 1) { // 0.5% de ir a una social zone (si existen)
                            // No está en una zona social, buscamos una y mandamos al aldeano allí
                            ArrayList<Zone> alZones = Game.getWorld().getZones();
                            int iCurrentASZID = World.getCell(getCoordinates()).getAstarZoneID();
                            Point3DShort p3dClosest = null, p3dTemp;
                            int iMaxDistance = Utils.MAX_DISTANCE;
                            for (int i = 0; i < alZones.size(); i++) {
                                zone = alZones.get(i);
                                if (ZoneManager.getItem(zone.getIniHeader()).getType() == ZoneManagerItem.TYPE_SOCIAL) {
                                    // Zona social encontrada, miramos la distancia
                                    p3dTemp = Zone.getFreeSitItemAtRandom(zone, iCurrentASZID);
                                    if (p3dTemp == null) {
                                        p3dTemp = Zone.getFreeCellAtRandom(zone, iCurrentASZID);
                                    }
                                    if (p3dTemp != null) {
                                        // Tenemos un punto, calculamos distancia
                                        if (p3dClosest == null) {
                                            p3dClosest = Point3DShort.getPoolInstance(p3dTemp);
                                            iMaxDistance = Utils.getDistance(getCoordinates(), p3dTemp);
                                        } else {
                                            int iTmp = Utils.getDistance(getCoordinates(), p3dTemp);
                                            if (iTmp < iMaxDistance) {
                                                p3dClosest.setPoint(p3dTemp);
                                                iMaxDistance = iTmp;
                                            }
                                        }
                                    }
                                }
                            }

                            // Zonas miradas, miramos si tenemos punto para ir
                            if (p3dClosest != null) {
                                setDestination(p3dClosest);
                            } else {
                                moveAtRandom(getLivingEntityData().getMovePCTCurrent(), TYPE_CITIZEN);
                            }
                        } else {
                            moveAtRandom(getLivingEntityData().getMovePCTCurrent(), TYPE_CITIZEN);
                        }
                    } else {
                        // Aldeano SI está en una zona social, lo movemos más bien poco
                        if (Utils.getRandomBetween(1, 10) == 1) {
                            moveAtRandom(getLivingEntityData().getMovePCTCurrent(), TYPE_CITIZEN);
                        }
                    }
                }
            }
        }
    }

    public void refreshCounters() {
        // Contador para las animaciones de comer/dormir
        getCitizenData().setBlinkAnimationTurns(getCitizenData().getBlinkAnimationTurns() + 1);
        if (getCitizenData().getBlinkAnimationTurns() > CitizenData.MAX_BLINK_ANIMATION_TURNS) {
            getCitizenData().setBlinkAnimationTurns(0);
        }

        // Contador de boost
        if (getCitizenData().getBoostCounter() > 0) {
            getCitizenData().setBoostCounter(getCitizenData().getBoostCounter() - 1);
        }
        // Contador de exclamation mark
        if (getShowExclamationTurns() > 0) {
            setShowExclamationTurns(getShowExclamationTurns() - 1);
        }
    }

    /**
     * Ejecuta la acción de su hotpoint (minar, chopear, diggear, custom, ...)
     *
     * @return true if it's job it's finished
     */
    private boolean executeTask() {
        boolean bReturn = false;

        Task task = getCurrentTask();

        // Obtenemos el punto donde actuar (el hotpoint del hotpoint)
        Point3DShort hotPoint3D = task.getHotPoint(getHotPointIndex()).getHotPoint();
        Cell cell = World.getCell(hotPoint3D);
        if (task.getTask() == Task.TASK_MINE || task.getTask() == Task.TASK_MINE_LADDER) {
            // Minar
            updatePathConstantOffsets();
            updateFacingDirection(getX(), getY(), getZ(), hotPoint3D.x, hotPoint3D.y, hotPoint3D.z);

            // Minamos
            if (!cell.isMined()) {
                cell.getTerrain().mine(hotPoint3D.x, hotPoint3D.y, hotPoint3D.z, task.getTask() == Task.TASK_MINE_LADDER);
            }

            bReturn = cell.isMined();
        }

        if (bReturn) {
            // Seteamos la celda indicando que ya no tiene órdenes (se usa para el dibujado)
            cell.setFlagOrders(false);
        }

        return bReturn;
    }

    public int compareTo(Entity e) {
        return getY() - e.getY();
    }

    /**
     * Borra la tarea actual del ciudadano y los índices
     */
    public void resetTaskIndexes() {
        if (getCurrentTask() != null && getCurrentTask().getTask() == Task.TASK_CUSTOM_ACTION) {
            boolean bFinished = true;
            // Custom action, si no está terminada la ponemos en la lista de tareas para que lo haga otro aldeano
            if (!getCurrentTask().isFinished()) {
                if (getCurrentCustomAction() != null) {
                    QueueItem qi = null;
                    if (getCurrentCustomAction().getQueueData() != null) {
                        qi = getCurrentCustomAction().getQueueData().getLastQueueItem();
                        if (qi != null) {
                            getCurrentCustomAction().getQueue().add(0, qi);
                        }
                    }
                    getCurrentCustomAction().setQueueData(new QueueData());
                }
                bFinished = false;
                Game.getWorld().getTaskManager().addCustomAction(getCurrentCustomAction(), false, false);
            }

            if (bFinished) {
                Action action = getCurrentCustomAction();
                if (action != null) {
                    setCurrentCustomAction(null, action.getId());
                }
            } else {
                setCurrentCustomAction(null);
            }
        }

        setCurrentTask(null);
        setHotPointIndex(-1); // Esto ya borra placesIndex
    }

    /**
     * Hace las cosas necesarias para ejecutar una tarea de construcción (buscar
     * materiales, llevarlos, ...)
     *
     * @param task
     */
    private void doBuildingTask() {
        int x = getX();
        int y = getY();
        int z = getZ();
        Task task = getCurrentTask();

        HotPoint hp = task.getHotPoint(getHotPointIndex());

        // Miramos prerequisitos
        Building building = Building.getBuilding(hp.getHotPoint());

        if (building == null) {
            // Esto no debería pasar nunca, tiene que haber un edificio en esa casilla
            Log.log(Log.LEVEL_ERROR, Messages.getString("Citizen.4") + hp.getHotPoint().x + "][" + hp.getHotPoint().y + "][" + hp.getHotPoint().z + "]", getClass().toString()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            getCurrentTask().setFinished(true);
            Game.getWorld().getTaskManager().removeCitizen(this);
            return;
        }

        ArrayList<int[]> alPrerequisites = building.getPrerequisites();
        if (alPrerequisites != null && alPrerequisites.size() > 0) {
            // Primero de todo miramos si llevamos un material bueno encima y estamos en la casilla del edificio
            if (getCitizenData().getCarryingData().getCarrying() != null) {
				// Llevamos algo encima, miramos si va bien para la construcción

                int iIndex = UtilsIniHeaders.contains(alPrerequisites, getCitizenData().getCarryingData().getCarrying().getNumericIniHeader());
                if (iIndex != -1) {
                    // Tenemos un material bueno, miramos si estamos en la casilla del edificio
                    if (x == hp.getHotPoint().x && y == hp.getHotPoint().y && z == hp.getHotPoint().z) {
                        // Guais, eleminamos el prerequisito del edificio
                        alPrerequisites.remove(iIndex);
                        setCarrying(null);

                        UtilsAL.play(UtilsAL.SOURCE_FX_BUILDING, z);
                        return;
                    } else {
                        // No estamos ahí, nos movemos
                        setDestination(hp.getHotPoint());
                        return;
                    }
                } else {
                    // Soltamos el material
                    dropCarryingItem();
                    return;
                }

            }

			// No lleva item encima, buscamos el item más cercano que nos vaya bien
            // Para evitar lag miramos que haya items en la hash de num items
            boolean bHayItems = false;
            forcountitems:
            for (int i = 0; i < alPrerequisites.size(); i++) {
                for (int j = 0; j < alPrerequisites.get(i).length; j++) {
                    if (Item.getNumItems(alPrerequisites.get(i)[j], false, Game.getWorld ().getRestrictHaulEquippingLevel ()) > 0) {
                        bHayItems = true;
                        break forcountitems;
                    }
                }
            }
            if (bHayItems) {
                Point3DShort p3dItem = null;
                for (int i = 0; i < alPrerequisites.size(); i++) {
                    p3dItem = Item.searchItem(false, getCoordinates(), alPrerequisites.get(i), true, Item.SEARCH_FALSE, Item.SEARCH_DOESNTMATTER, null, Game.getWorld ().getRestrictHaulEquippingLevel ());
                    if (p3dItem != null) {
                        break;
                    }
                }

                if (p3dItem == null) {
                    Game.getWorld().getTaskManager().removeCitizen(this);
                    return;
                } else {
                    if (p3dItem.equals(getCoordinates())) {
                        // Item en la casilla actual
                        Cell cell = World.getCell(getCoordinates());
                        if (cell.getEntity() != null && cell.getEntity() instanceof Item) {
                            if (ItemManager.getItem(cell.getEntity().getIniHeader()).isContainer()) {
                                Container container = Game.getWorld().getContainer(cell.getEntity().getID());
                                if (container != null) {
                                    Item item = container.removeItemWithPrerequisitesInts(alPrerequisites);
                                    if (item != null) {
                                        setCarrying(item); // Lo pillamos
                                        // Tenemos el material, lo llevamos al edificio para construir el item
                                        setDestination(hp.getHotPoint());
                                    }
                                }
                            } else {
                                setCarrying((Item) cell.getEntity()); // Lo pillamos
                                cell.getEntity().delete(); // Lo borramos
                                // Tenemos el material, lo llevamos al edificio para construir el item
                                setDestination(hp.getHotPoint());
                            }
                        }
                    } else {
                        // Item por ahí
                        setDestination(p3dItem);
                    }
                }
            } else {
                Game.getWorld().getTaskManager().removeCitizen(this);
                return;
            }
        } else {
            // No tiene prerequisitos, miramos los prerequisitos living

            ArrayList<int[]> alPrerequisitesLiving = building.getPrerequisitesLiving();
            if (alPrerequisitesLiving != null && alPrerequisitesLiving.size() > 0) {
                // Primero de todo miramos si llevamos un living bueno encima y estamos en la casilla del edificio
                if (getCitizenData().getCarryingData().getCarryingLiving() != null) {
                    // Llevamos algo encima, miramos si va bien para la construcción
                    int iIndex = UtilsIniHeaders.contains(alPrerequisitesLiving, getCitizenData().getCarryingData().getCarryingLiving().getNumericIniHeader());
                    if (iIndex != -1) {
                        // Tenemos un living bueno, miramos si estamos en la casilla del edificio
                        if (x == hp.getHotPoint().x && y == hp.getHotPoint().y && z == hp.getHotPoint().z) {
                            // Guais, eleminamos el prerequisito del edificio
                            alPrerequisitesLiving.remove(iIndex);

                            // FX
                            LivingEntityManagerItem lemi = LivingEntityManager.getItem(getCitizenData().getCarryingData().getCarryingLiving().getIniHeader());
                            if (lemi.getFxDead() != null && lemi.getFxDead().length() > 0) {
                                UtilsAL.play(lemi.getFxDead(), z);
                            } else {
                                UtilsAL.play(UtilsAL.SOURCE_FX_DEAD, z);
                            }

                            setCarryingLiving(null);

                            return;
                        } else {
                            // No estamos ahí, nos movemos
                            setDestination(hp.getHotPoint());
                            return;
                        }
                    } else {
                        // Soltamos el living
                        dropCarryingItem();
                        return;
                    }

                }

                // Buscamos el friendly más cercano que nos vaya bien
                LivingEntity le = null;
                for (int i = 0; i < alPrerequisitesLiving.size(); i++) {
                    le = searchLiving(getCoordinates(), alPrerequisitesLiving.get(i), true, null);
                    if (le != null) {
                        break;
                    }
                }

                if (le == null) {
                    Game.getWorld().getTaskManager().removeCitizen(this);
                    return;
                } else {
                    if (le.getCoordinates().equals(getCoordinates())) {
                        // Friendly en la casilla actual
                        setCarryingLiving(le);
                        le.delete(false);
                        // Tenemos el living, lo llevamos al edificio para construir el item
                        setDestination(hp.getHotPoint());
                    } else {
                        // Living por ahí
                        setDestination(le.getCoordinates());
                    }
                }
            } else {
				// Sin más prerequisitos, tarea terminada (edificio construido)
                // Por si acaso nos movemos al edificio (importante)
                if (x != hp.getHotPoint().x || y != hp.getHotPoint().y || z != hp.getHotPoint().z) {
                    // No estamos ahí, nos movemos
                    setDestination(hp.getHotPoint());
                    return;
                } else {
                    // Nos esperamos unos turnos en el caso de que haya algun citizen encima para que no muera aplastado
                    building.setOperative(true, false); // temporalmente operativo para llamar al Citizen.iscellallowed

                    boolean bAldeanoEncima = false;
                    int buildingX = building.getX();
                    int buildingY = building.getY();
                    BuildingManagerItem bmi = BuildingManager.getItem(building.getIniHeader());
                    Cell cell;
                    bucle:
                    for (int xb = buildingX; xb < (buildingX + bmi.getWidth()); xb++) {
                        for (int yb = buildingY; yb < (buildingY + bmi.getHeight()); yb++) {
                            if (!isCellAllowed(xb, yb, building.getZ())) {
                                cell = World.getCell(xb, yb, building.getZ());
                                if (cell.containsSpecificLiving(TYPE_CITIZEN) != null) {
                                    // Hay aldeano
                                    bAldeanoEncima = true;
                                    break bucle;
                                }
                            }
                        }
                    }

                    if (bAldeanoEncima) {
                        // Esperamos a ver si se va
                        building.setOperative(false, false);
                    } else {
                        building.setOperative(true, true);
                        Game.getWorld().getTaskManager().setHotPointFinished(task, getHotPointIndex());
                        Game.getWorld().getTaskManager().removeCitizen(this);

                        // Update tutorial flow
                        Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_BUILD, building.getNumericIniHeader (), null);
                    }
                }
            }
        }
    }

    /**
     * Hace las cosas necesarias para mover un objeto del mundo a la caravana
     */
    private void doMoveToCaravan() {
        Task task = getCurrentTask();

        int iCaravanID = task.getPointIni().x;

        // Miramos que la caravana exista
        LivingEntity leCaravan = World.getLivingEntityByID(iCaravanID);
        if (leCaravan == null) {
            getCurrentTask().setFinished(true);
            Game.getWorld().getTaskManager().removeCitizen(this);
            return;
        }
        int iCitizenASZID = World.getCell(getCoordinates()).getAstarZoneID();
        if (iCitizenASZID != World.getCell(leCaravan.getCoordinates()).getAstarZoneID()) {
            Game.getWorld().getTaskManager().removeCitizen(this);
            return;
        }

        // Miramos el item a llevar
        int iItemID = task.getPointIni().y;
        String sItemIniHeader = task.getParameter();
        Item itemCarrying = getCarrying();
        if (itemCarrying == null) {
            // No lleva item, lo buscamos y vamos a por el
            Point3DShort p3dItemDestination = null;
            if (iItemID != -1) {
                // Item militar
                Point3DShort p3dItemMilitar = Item.searchItemByID(iItemID);
                // Debe existir (y en la zona) pq el taskmanager ya ha hecho esta búsqueda
                if (p3dItemMilitar == null) {
                    Game.getWorld().getTaskManager().removeCitizen(this);
                    return;
                }
                if (World.getCell(p3dItemMilitar).getAstarZoneID() != iCitizenASZID) {
                    Game.getWorld().getTaskManager().removeCitizen(this);
                    return;
                }

                p3dItemDestination = p3dItemMilitar;
            } else {
                // Item genérico
                int[] aiItemHeaders = new int[1];
                aiItemHeaders[0] = UtilsIniHeaders.getIntIniHeader(sItemIniHeader);
                Point3DShort p3dItem = Item.searchItem(false, getCoordinates(), aiItemHeaders, true, Item.SEARCH_FALSE, Item.SEARCH_DOESNTMATTER, null, World.MAP_DEPTH - 1);
                // Debe existir pq el taskmanager ya ha hecho esta búsqueda
                if (p3dItem == null) {
                    Game.getWorld().getTaskManager().removeCitizen(this);
                    return;
                }

                p3dItemDestination = p3dItem;
            }

            // Si llega aquí es que hemos encontrado el item, vamos a ver si ya estamos donde toca para pillarlo
            if (p3dItemDestination.equals(getCoordinates())) {
                // Item en la casilla actual
                Cell cell = World.getCell(getCoordinates());
                if (cell.getEntity() != null && cell.getEntity() instanceof Item) {
                    if (!sItemIniHeader.equals(cell.getEntity().getIniHeader()) && ItemManager.getItem(cell.getEntity().getIniHeader()).isContainer()) {
                        Container container = Game.getWorld().getContainer(cell.getEntity().getID());
                        if (container != null) {
                            Item item;
                            if (iItemID == -1) {
                                ArrayList<String> alItemHeaders = new ArrayList<String>(1);
                                alItemHeaders.add(sItemIniHeader);
                                item = container.removeItemWithPrerequisites(alItemHeaders);
                            } else {
                                item = container.removeItem(iItemID);
                            }
                            if (item != null) {
                                setCarrying(item); // Lo pillamos
                                // Tenemos el material, lo llevamos a la caravana
                                setDestination(leCaravan.getCoordinates());
                                return;
                            } else {
                                // ???
                                Game.getWorld().getTaskManager().removeCitizen(this);
                                return;
                            }
                        } else {
                            // ???
                            Game.getWorld().getTaskManager().removeCitizen(this);
                            return;
                        }
                    } else {
                        setCarrying((Item) cell.getEntity()); // Lo pillamos
                        cell.getEntity().delete(); // Lo borramos
                        // Tenemos el material, lo llevamos a la caravana
                        setDestination(leCaravan.getCoordinates());
                    }
                } else {
                    // ???
                    Game.getWorld().getTaskManager().removeCitizen(this);
                    return;
                }
            } else {
                // Item por ahí
                setDestination(p3dItemDestination);
                return;
            }

        } else {
            // Lleva item, si es el que toca lo llevamos a la caravana sino lo tiramos
            if (iItemID != -1) {
                // Item militar
                if (itemCarrying.getID() != iItemID) {
                    dropCarryingItem();
                    return;
//				} else {
                    // Item bueno
                }
            } else {
                if (!sItemIniHeader.equals(itemCarrying.getIniHeader())) {
                    dropCarryingItem();
                    return;
//				} else {
                    // Item bueno
                }
            }

            // Si llega aquí es que lleva el item bueno, hay que llevarlo a sitio
            if (leCaravan.getCoordinates().equals(getCoordinates())) {
                CaravanData caravanData = Game.getWorld().getCurrentCaravanData();
                if (caravanData != null) {
                    // Debería existir
                    if (caravanData.itemCarried(getCarrying())) {
                        // Está en el sitio, hacemos la transacción y pacasa
                        setCarrying(null);
                        getCurrentTask().setFinished(true);
                    }
                }

                Game.getWorld().getTaskManager().removeCitizen(this);
                return;
            } else {
                setDestination(leCaravan.getCoordinates());
                return;
            }
        }
    }

    /**
     * Hace las cosas necesarias para alimentar a un living
     */
    private void doFoodNeeded() {
        Task task = getCurrentTask();

        int iLivingID = Integer.parseInt(task.getParameter());

        // Miramos que el living exista
        LivingEntity leToFeed = World.getLivingEntityByID(iLivingID);
        if (leToFeed == null) {
            // Si la living la tiene pillada alguien de momento no hacemos nada, en otro caso ya estamos
            Citizen cit;
            for (int i = 0; i < World.getCitizenIDs().size(); i++) {
                cit = (Citizen) World.getLivingEntityByID(World.getCitizenIDs().get(i));
                if (cit != null && cit.getCarryingLiving() != null && cit.getCarryingLiving().getID() == iLivingID) {
                    // Cit con la living deseada, nos esperamos
                    Game.getWorld().getTaskManager().removeCitizen(this);
                    return;
                }
            }

            // Si llega aquí es que nadie tiene la living, debe haber muerto
            getCurrentTask().setFinished(true);
            Game.getWorld().getTaskManager().removeCitizen(this);
            return;
        }

        // Si llega aquí es que la living existe
        LivingEntityManagerItem lemi = LivingEntityManager.getItem(leToFeed.getIniHeader());

        // Miramos si estamos en la misma zona
        int iCitizenASZID = World.getCell(getCoordinates()).getAstarZoneID();
        if (iCitizenASZID != World.getCell(leToFeed.getCoordinates()).getAstarZoneID()) {
            Game.getWorld().getTaskManager().removeCitizen(this);
            return;
        }

        // Miramos el item a llevar
        Item itemCarrying = getCarrying();
        if (itemCarrying == null) {
            Point3DShort p3dItem = Item.searchItem(false, getCoordinates(), lemi.getFoodNeeded(), true, Item.SEARCH_FALSE, Item.SEARCH_DOESNTMATTER, null, Game.getWorld ().getRestrictHaulEquippingLevel ());
            if (p3dItem == null) {
                Game.getWorld().getTaskManager().removeCitizen(this);
                return;
            }

            // Si llega aquí es que hemos encontrado el item, vamos a ver si ya estamos donde toca para pillarlo
            if (p3dItem.equals(getCoordinates())) {
                // Item en la casilla actual
                Cell cell = World.getCell(getCoordinates());
                if (cell.getEntity() != null && cell.getEntity() instanceof Item) {
                    if (ItemManager.getItem(cell.getEntity().getIniHeader()).isContainer()) {
                        Container container = Game.getWorld().getContainer(cell.getEntity().getID());
                        if (container != null) {
                            Item item;
                            item = container.removeItemWithPrerequisites(lemi.getFoodNeeded());

                            if (item != null) {
                                setCarrying(item); // Lo pillamos
                                // Tenemos el material, lo llevamos a la living
                                setDestination(leToFeed.getCoordinates());
                                return;
                            } else {
                                // ???
                                Game.getWorld().getTaskManager().removeCitizen(this);
                                return;
                            }
                        } else {
                            // ???
                            Game.getWorld().getTaskManager().removeCitizen(this);
                            return;
                        }
                    } else {
                        setCarrying((Item) cell.getEntity()); // Lo pillamos
                        cell.getEntity().delete(); // Lo borramos
                        // Tenemos el material, lo llevamos a la living
                        setDestination(leToFeed.getCoordinates());
                    }
                } else {
                    // ???
                    Game.getWorld().getTaskManager().removeCitizen(this);
                    return;
                }
            } else {
                // Item por ahí
                setDestination(p3dItem);
                return;
            }
        } else {
            // Lleva item, si es el que toca lo llevamos a la living sino lo tiramos
            if (!UtilsIniHeaders.contains(lemi.getFoodNeeded(), itemCarrying.getNumericIniHeader())) {
                dropCarryingItem();
                return;
//			} else {
                // Item bueno
            }

            // Si llega aquí es que lleva el item bueno, hay que llevarlo a sitio
            if (leToFeed.getCoordinates().equals(getCoordinates())) {
                // Está en el sitio, hacemos la transacción y pacasa
                setCarrying(null);
                leToFeed.setFoodNeededTurns(lemi.getFoodNeededTurns());
                getCurrentTask().setFinished(true);
                Game.getWorld().getTaskManager().removeCitizen(this);
                return;
            } else {
                setDestination(leToFeed.getCoordinates());
                return;
            }
        }
    }

    /**
     * Hace lo necesario para sacar un item malo de un container
     */
    private void doRemoveFromContainerTask() {
        int iContainerID = Integer.parseInt(getCurrentTask().getParameter());
        Container container = Game.getWorld().getContainer(iContainerID);
        Item itemContainer = World.getItems().get(iContainerID);
        if (itemContainer == null || container == null) {
            getCurrentTask().setFinished(true);
            Game.getWorld().getTaskManager().removeCitizen(this);
            return;
        }

        // Tenemos el container, miramos si aún hay algo malo dentro, se supone que si
        if (!container.isWrongItemsInside()) {
            getCurrentTask().setFinished(true);
            Game.getWorld().getTaskManager().removeCitizen(this);
            return;
        }

        // Miramos si estamos en el sitio
        if (getCoordinates().equals(itemContainer.getCoordinates())) {
            // Estamos donde el container, sacamos el item y se acabó
            setCarrying(container.removeBadItem());
            getCurrentTask().setFinished(true);
            Game.getWorld().getTaskManager().removeCitizen(this);
            return;
        } else {
			// NO estamos donde el container, movemos al citizen
            // Chequeo de zona, por si acaso
            if (World.getCell(getCoordinates()).getAstarZoneID() == World.getCell(itemContainer.getCoordinates()).getAstarZoneID()) {
                // Zona buena, nos movemos
                setDestination(itemContainer.getCoordinates());
                return;
            } else {
                // Zona mala, salimos
                getCurrentTask().setFinished(true);
                Game.getWorld().getTaskManager().removeCitizen(this);
                return;
            }
        }
    }

    /**
     * Hace lo necesario para que el aldeano lleve un item de un punto a otro
     *
     * @param lockItem Indica si hay que poner operativo y bloquear el item al
     * llevarlo a destino
     */
    private void doHaulingTask(boolean operativeAndLock, boolean bPutInContainer, boolean bCheckSourceNoLocked) {
        int x = getX();
        int y = getY();
        int z = getZ();
        Task task = getCurrentTask();

        // Si no tenemos el item vamos a buscarlo
        if (getCarrying() == null) {
            Point3D iniPoint = task.getPointIni();
            // Primero lo llevamos al punto de inicio (donde reside el item)
            if (x != iniPoint.x || y != iniPoint.y || z != iniPoint.z) {
                setDestination(iniPoint);
            } else {
                // Estamos en el punto de inicio, pillamos el item
                Cell cell = World.getCell(x, y, z);
                if (!cell.hasItem()) {
                    // No hay item aquí, lo habrá pillado alguien, fin de tarea
                    getCurrentTask().setFinished(true);
                    Game.getWorld().getTaskManager().removeCitizen(this);
                } else {
                    // Miramos si está el item
                    if (cell.getEntity().getIniHeader().equals(task.getParameter())) {
                        // Tenemos el item que toca, lo pillamos
                        Item item = cell.getItem();
                        if (bCheckSourceNoLocked && item.isLocked()) {
                            // Item bloqueado, pasando
                            getCurrentTask().setFinished(true);
                            Game.getWorld().getTaskManager().removeCitizen(this);
                        } else {
                            setCarrying((Item) cell.getEntity());
                            cell.getEntity().delete();
                        }
                    } else {
                        // Miramos que no esté en container
                        Item item = cell.getItem();
                        if (item != null) {
                            ItemManagerItem imi = ItemManager.getItem(item.getIniHeader());
                            if (imi != null) {
                                if (imi.isContainer()) {
                                    // Hay un container, vamos a ver si el item buscado está dentro
                                    ArrayList<Container> containers = Game.getWorld().getContainers();
                                    Container container = null;
                                    for (int i = 0; i < containers.size(); i++) {
                                        container = containers.get(i);
                                        if (container.getItemID() == item.getID()) {
                                            // Tenemos el container
                                            break;
                                        }
                                    }
                                    if (container != null && container.getItemID() == item.getID()) {
                                        // A ver si está dentro
                                        Item itemRemoved = container.removeItem(task.getParameter());
                                        if (itemRemoved != null) {
                                            // Lo tenemos !!!!
                                            setCarrying(itemRemoved);
                                        } else {
                                            // Nanai, no debería pasar
                                            getCurrentTask().setFinished(true);
                                            Game.getWorld().getTaskManager().removeCitizen(this);
                                        }
                                    } else {
                                        // Nunca debería pasar
                                        getCurrentTask().setFinished(true);
                                        Game.getWorld().getTaskManager().removeCitizen(this);
                                    }
                                } else {
                                    // Casilla vacía (lo habrá pillado otro aldeano). Fin de tarea
                                    getCurrentTask().setFinished(true);
                                    Game.getWorld().getTaskManager().removeCitizen(this);
                                }
                            } else {
                                // Nunca debería pasar
                                getCurrentTask().setFinished(true);
                                Game.getWorld().getTaskManager().removeCitizen(this);
                            }
                        } else {
                            // Nunca debería pasar
                            getCurrentTask().setFinished(true);
                            Game.getWorld().getTaskManager().removeCitizen(this);
                        }
                    }
                }
            }

        } else {
            // Tenemos un item, si es el que toca lo llevamos a destino
            if (getCitizenData().getCarryingData().getCarrying().getIniHeader().equals(task.getParameter())) {
                Point3D endPoint = task.getPointEnd();
                // Miramos si ya estamos en destino
                boolean bEnDestino = false;
                // Caso especial, bridges (o items que se ponen desde una casilla anterior al destino)
                if (getCitizenData().getCarryingData().getCarrying() instanceof Item && ItemManager.getItem(((Item) getCitizenData().getCarryingData().getCarrying()).getIniHeader()).canBeBuiltOnHoles()) {
                    ArrayList<Point3DShort> alPoints = Task.getAccesingPointsMatchingASZI(endPoint, World.getCell(x, y, z).getAstarZoneID(), task.getTask());
                    if (alPoints.size() > 0) {
                        // Miramos si estamos en alguna casilla
                        for (int i = 0; i < alPoints.size(); i++) {
                            if (getCoordinates().equals(alPoints.get(i))) {
                                bEnDestino = true;
                                break;
                            }
                        }

                        if (!bEnDestino) {
                            setDestination(alPoints.get(0));
                            return;
                        }
                    } else {
                        // No hay casilla posible, soltamos item
                        getCurrentTask().setFinished(true);
                        Game.getWorld().getTaskManager().removeCitizen(this);
                        dropCarryingItem();
                        return;
                    }
                } else {
                    bEnDestino = (x == endPoint.x && y == endPoint.y && z == endPoint.z);
                }

                if (!bEnDestino) {
                    // No estamos en destino, nos movemos
                    setDestination(endPoint);
                } else {
                    // Estamos en destino, soltamos el material y fin de tarea
                    Cell cell = World.getCell(endPoint);
                    if (cell.isEmpty()) {
                        ItemManagerItem imi = ItemManager.getItem(getCarrying().getIniHeader());
                        if (operativeAndLock && Item.isCellAvailableForItem(imi, endPoint.x, endPoint.y, endPoint.z, true, false)) {
                            getCarrying().setOperative(true);
                            getCarrying().setLocked(true);
                        }
                        getCarrying().init(endPoint.x, endPoint.y, endPoint.z);
                        if (imi.isCanBeRotated()) {
                            getCarrying().setFacing(task.getFace());
                        }
                        cell.setEntity(getCarrying());
                        setCarrying(null);
                        getCurrentTask().setFinished(true);
                        Game.getWorld().getTaskManager().removeCitizen(this);
                    } else {
                        // Casilla que ya tiene algo, raro (no tan raro ahora con los containers)
                        if (bPutInContainer) {
                            Entity cellItem = cell.getEntity();
                            if (cellItem != null && cellItem instanceof Item) {
                                ItemManagerItem imi = ItemManager.getItem(cellItem.getIniHeader());
                                if (imi.isContainer()) {
                                    Container container = Game.getWorld().getContainer(cellItem.getID());
                                    if (container != null && container.itemAllowed(getCarrying())) {
                                        container.addItem(getCarrying());
                                        setCarrying(null);
                                        getCurrentTask().setFinished(true);
                                        Game.getWorld().getTaskManager().removeCitizen(this);
                                        return;
                                    }
                                }
                            }
                        }

                        // Si llega aquí es que no ha podido meterlo o no es tarea de container (raro), soltamos el item y fin de tarea
                        cell.setFlagOrders(false);
                        getCurrentTask().setFinished(true);
                        dropCarryingItem();
                        return;
                    }
                }
            } else {
                // Material que no toca, lo soltamos (OJO, NO fin de tarea)
                dropCarryingItem();
                return;
            }
        }
    }

    /**
     * Lleva le material que está cargando a alguna celda libre y suelta una
     * living si la lleva
     */
    private void doDropTask() {
        executeDropLivingTask();
        if (executeDropItemTask()) {
            resetTaskIndexes();
        }
    }

    /**
     * Suelta la living allí donde está
     */
    private void executeDropLivingTask() {
        if (getCarryingLiving() != null) {
            LivingEntityManagerItem lemi = LivingEntityManager.getItem(getCarryingLiving().getIniHeader());

            if (lemi != null) {
                World.addNewLiving(lemi.getIniHeader(), lemi.getType(), true, getX(), getY(), getZ(), true);
            }

            setCarryingLiving(null);
        }
    }

    public boolean checkCellToDrop(Cell cell) {
        if (getCarrying() == null) {
            return false;
        }

        if (cell.isEmpty() && !cell.isFlagOrders()) {
            if (!cell.hasStockPile()) {
                return true;
            } else {
                // Hay stockpile, miramos
                Stockpile stockpile = Stockpile.getStockpile(cell.getCoordinates());
                if (stockpile != null && stockpile.itemAllowed(getCarrying().getIniHeader())) {
                    return true;
                }
            }
        } else {
            // Miramos que no haya un container
            Item item = cell.getItem();
            if (item != null) {
                ItemManagerItem imi = ItemManager.getItem(item.getIniHeader());
                if (imi != null && imi.isContainer()) {
                    Container container = Game.getWorld().getContainer(item.getID());
                    if (container != null && container.itemAllowed(getCarrying())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean doDrop(Cell cell) {
        // Casilla buena para soltar el item
        if (getCarrying() != null) {
            // Container?
            Item cellItem = cell.getItem();
            ItemManagerItem cellImi;
            if (cellItem == null) {
                cellImi = null;
            } else {
                cellImi = ItemManager.getItem(cellItem.getIniHeader());
            }

            if (cellImi != null && cellImi.isContainer()) {
                // Drop en container
                Container container = Game.getWorld().getContainer(cellItem.getID());
                if (container != null && container.itemAllowed(getCarrying())) {
                    container.addItem(getCarrying());
                    setCarrying(null);
                } else {
                    // Drop normal (copia de lo de abajo)
                    getCarrying().init(getX(), getY(), getZ());
                    ItemManagerItem imi = ItemManager.getItem(getCarrying().getIniHeader());
                    getCarrying().setOperative(imi.isAlwaysOperative());
                    getCarrying().setLocked(imi.isLocked());
                    cell.setEntity(getCarrying());
                    setCarrying(null);
                }
                return true;
            } else {
                // Drop normal
                getCarrying().init(getX(), getY(), getZ());
                ItemManagerItem imi = ItemManager.getItem(getCarrying().getIniHeader());
                getCarrying().setOperative(imi.isAlwaysOperative());
                getCarrying().setLocked(imi.isLocked());
                cell.setEntity(getCarrying());
                setCarrying(null);
                return true;
            }
        }

        return false;
    }

    /**
     * Lleva le material que está cargando a alguna celda libre, devuelve true
     * si lo consigue. No saca al aldeano de ninguna tarea.
     *
     * @return true si finaliza la tarea
     */
    private boolean executeDropItemTask() {
        if (getCarrying() == null) {
            // Fin de tarea
            return true;
        }

        // Miramos si lo puede dejar en el suelo
        Cell cell = World.getCell(getCoordinates());
        if (checkCellToDrop(cell)) {
            doDrop(cell);

            // Fin de tarea
            return true;
        }

        // En la casilla actual no se puede soltar, vamos a buscar un punto y llevamos el aldeano allí
        int distancia = 0; // Índice para ir mirando casillas adyacentes
        final int distanciaMAX = World.MAP_WIDTH;

        while (distancia < distanciaMAX) {
            distancia++;
            for (int x = (getX() - distancia); x <= (getX() + distancia); x++) {
                for (int y = (getY() - distancia); y <= (getY() + distancia); y++) {
                    if (Math.abs((getX() - x)) == distancia || Math.abs((getY() - y)) == distancia) { // Para que sólo mire puntos exteriores del radio
                        if (Utils.isInsideMap(x, y, getZ())) {
                            cell = World.getCell(x, y, getZ());
                            if (cell.isEmpty() && !cell.isFlagOrders() && cell.getAstarZoneID() == World.getCell(getCoordinates()).getAstarZoneID() && isCellAllowed(cell)) {
                                if (!cell.hasStockPile() || (cell.hasStockPile() && Stockpile.getStockpile(cell.getCoordinates()).itemAllowed(getCarrying().getIniHeader()))) {
                                    // Punto libre encontrado
                                    setDestination(x, y, getZ());
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }

		// En todo el nivel no hay ni un hueco, haremos el truqui de buscar en un radio de 8x8 (por ejemplo) a partir
        // de la posición de otros aldeanos
        int iNivelMalo = getZ();
        int iASZID = World.getCell(getCoordinates()).getAstarZoneID();

        // Recorremos los aldeanos
        ArrayList<Integer> citIDs = World.getCitizenIDs();
        LivingEntity le;
        Cell cellCit;
        for (int c = 0; c < citIDs.size(); c++) {
            le = World.getLivingEntityByID(citIDs.get(c));
            if (le.getCoordinates().z != iNivelMalo) {
                cellCit = World.getCell(le.getCoordinates());
                if (cellCit.getAstarZoneID() == iASZID) {
                    // Vamos bien, buscamos en un radio de 8x8
                    final int distanciaMAXCITS = 8;
                    distancia = 0;
                    while (distancia < distanciaMAXCITS) {
                        distancia++;
                        for (int x = (le.getX() - distancia); x <= (le.getX() + distancia); x++) {
                            for (int y = (le.getY() - distancia); y <= (le.getY() + distancia); y++) {
                                if (Math.abs((le.getX() - x)) == distancia || Math.abs((le.getY() - y)) == distancia) { // Para que sólo mire puntos exteriores del radio
                                    if (Utils.isInsideMap(x, y, le.getZ())) {
                                        cell = World.getCell(x, y, le.getZ());
                                        if (cell.isEmpty() && !cell.isFlagOrders() && cell.getAstarZoneID() == iASZID && LivingEntity.isCellAllowed(cell)) {
                                            if (!cell.hasStockPile() || (Stockpile.getStockpile(cell.getCoordinates()).itemAllowed(getCarrying().getIniHeader()))) {
                                                // Punto libre encontrado !!
                                                setDestination(cell.getCoordinates());
                                                return false;
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

        // No hay punto libre, a tomar por culo el item y fin de tarea
        setCarrying(null);
        return true;
    }

    /**
     * Hace lo necesario para que el aldeano llegue hasta comida y se alimente
     */
    private void doEatTask() {
//		if (getID () == 273087 || getID () == 272825) {
//			System.out.println (".");
//		}
        executeDropLivingTask();

        if (getCarrying() == null) {
            Point3DShort p3dItem = Item.searchFood(getCoordinates(), getID());
            if (p3dItem == null) {
                // No hay comida en el mundo, raro, salimos de la tarea
                resetTaskIndexes();
            } else {
                // Comida en el mundo
                if (p3dItem.equals(getCoordinates())) {
                    // En la casilla actual, la pillamos
                    Cell cell = World.getCell(getCoordinates());
                    if (cell.getEntity() != null && cell.getEntity() instanceof Item) {
                        if (ItemManager.getItem(cell.getEntity().getIniHeader()).isContainer()) {
                            Container container = Game.getWorld().getContainer(cell.getEntity().getID());
                            if (container != null) {
                                Item item = container.removeFoodItem();
                                if (item != null) {
                                    setCarrying(item);
                                    getCitizenData().setHungryEating(0);
                                } else {
                                    // Ya no había comida en el container, salimos de la tarea
                                    resetTaskIndexes();
                                }
                            } else {
                                // Algo ha pasado, en la casilla no hay nada que nos sirva (no debería pasar, quizá están moviendo un container)
                                resetTaskIndexes();
                            }
                        } else {
                            setCarrying((Item) cell.getEntity());
                            cell.getEntity().delete();
                            getCitizenData().setHungryEating(0);
                        }
                    } else {
                        // Algo ha pasado, en la casilla no hay nada que nos sirva (no debería pasar, quizá están moviendo un container)
                        resetTaskIndexes();
                    }
                } else {
                    // Por ahí
                    getCurrentTask().setPointIni(p3dItem); // Ponemos esto para que otro aldeano pueda detectar que vamos a por la comida
                    setDestination(p3dItem);
                }
            }
        } else {
            ItemManagerItem imiCarrying = ItemManager.getItem(getCarrying().getIniHeader());
            // Tenemos item, miramos si es comida
            if (imiCarrying.canBeEaten()) {
                // Obtenemos la zona en la que estamos (si es que estamos en alguna)
                ArrayList<Zone> zones = Game.getWorld().getZones();
                Cell cell = World.getCell(getCoordinates());
                boolean bEstamosEnComedor = false;
                String sZoneToEat = LivingEntityManager.getItem(getIniHeader()).getEatZone();
                if (sZoneToEat != null && sZoneToEat.length() > 0) {
                    if (cell.hasZone()) {
                        Zone zone;
                        for (int i = 0; i < zones.size(); i++) {
                            zone = zones.get(i);
                            if (zone.getID() == cell.getZoneID()) {
                                // Tenemos la zona actual
                                if (zone.isOperative() && zone.getIniHeader().equalsIgnoreCase(sZoneToEat)) {
                                    // Estamos en el comedor, comemos
                                    bEstamosEnComedor = true;
                                    break;
                                }
                            }
                        }
                    }

                    if (!bEstamosEnComedor) {
                        // No estamos en comedor, buscamos uno y vamos hacia allí, si lo encontramos
                        Point3DShort p3d = null;
                        Point3DShort p3dMinDistance = null;
                        int iMinDistance = Utils.MAX_DISTANCE;
                        Zone zone;
                        for (int i = 0; i < zones.size(); i++) {
                            zone = zones.get(i);
                            if (zone.isOperative() && zone.getIniHeader().equalsIgnoreCase(sZoneToEat)) {
                                // Miramos si alguna celda del comedor es accesible
                                p3d = Zone.getFreeSitItemAtRandom(zone, cell.getAstarZoneID());
                                if (p3d == null) {
                                    continue;
                                } else {
                                    // Tenemos una celda, miramos la distancia
                                    if (p3dMinDistance == null) {
                                        p3dMinDistance = Point3DShort.getPoolInstance(p3d);
                                        iMinDistance = Utils.getDistance(getCoordinates(), p3d);
                                    } else {
                                        int iAuxDistance = Utils.getDistance(getCoordinates(), p3d);
                                        if (iAuxDistance < iMinDistance) {
                                            p3dMinDistance.setPoint(p3d);
                                            iMinDistance = iAuxDistance;
                                        }
                                    }
                                }
                            }
                        }

                        if (p3dMinDistance == null) {
                            // Si no hay sillas miraremos cualquier casilla del comedor
                            for (int i = 0; i < zones.size(); i++) {
                                zone = zones.get(i);
                                if (zone.isOperative() && zone.getIniHeader().equalsIgnoreCase(sZoneToEat)) {
                                    // Miramos si alguna celda del comedor es accesible
                                    p3d = Zone.getFreeCellAtRandom(zone, cell.getAstarZoneID());
                                    if (p3d == null) {
                                        continue;
                                    } else {
                                        // Tenemos una celda, miramos la distancia
                                        if (p3dMinDistance == null) {
                                            p3dMinDistance = Point3DShort.getPoolInstance(p3d);
                                            iMinDistance = Utils.getDistance(getCoordinates(), p3d);
                                        } else {
                                            int iAuxDistance = Utils.getDistance(getCoordinates(), p3d);
                                            if (iAuxDistance < iMinDistance) {
                                                p3dMinDistance.setPoint(p3d);
                                                iMinDistance = iAuxDistance;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (p3dMinDistance != null) {
                            setDestination(p3dMinDistance);
                            return;
                        }
                    }
                }

                getCitizenData().setHungryEating(getCitizenData().getHungryEating() + 1);
                if ((getCitizenData().getHungryEating() % (World.TIME_MODIFIER_HOUR / 5)) == 0) {
                    UtilsAL.play(UtilsAL.SOURCE_FX_EAT, getZ());
                }
                ItemManagerItem imi = ItemManager.getItem(getCarrying().getIniHeader());
                int iEatTime = imi.getFoodEatTime();
                if (iEatTime <= 0) {
                    iEatTime = World.TIME_MODIFIER_HOUR;
                }
                if (getCitizenData().getHungryEating() >= iEatTime) {
                    getCitizenData().setHungryEating(0);
                    resetTaskIndexes();
                    int iFillPCT = imi.getFoodFillPCT();
                    if (iFillPCT <= 0) {
                        iFillPCT = 100;
                    }
                    getCitizenData().setHungry((getCitizenData().getMaxHungry() * iFillPCT) / 100);
                    setCarrying(null);
                    // Happiness
                    if (bEstamosEnComedor) {
                        if (!getSoldierData().isSoldier()) {
                            getCitizenData().setHappiness(getCitizenData().getHappiness() + 3);
                        }
                    }

                    // Effects después de comer
                    if (imi.getFoodEffects() != null) {
                        for (int i = 0; i < imi.getFoodEffects().size(); i++) {
                            addEffect(EffectManager.getItem(imi.getFoodEffects().get(i)), true);
                        }
                    }

                    cell = World.getCell(getCoordinates());
                    if (cell.hasItem()) {
                        imi = ItemManager.getItem(cell.getEntity().getIniHeader());
                        if (imi != null && imi.canBeUsedToSit()) {
                            if (!getSoldierData().isSoldier()) {
                                getCitizenData().setHappiness(getCitizenData().getHappiness() + imi.getHappiness());
                            }
                        }
                    }

                    // Events después de comer
                    EventData ed;
                    for (int i = 0; i < Game.getWorld().getEvents().size(); i++) {
                        ed = Game.getWorld().getEvents().get(i);
                        EventManagerItem emi = EventManager.getItem(ed.getEventID());
                        if (emi != null) {
                            ed.launchEffects(emi, emi.getEffectsAfterEat(), this);
                        }
                    }
                }
            } else {
                // No es comida, la soltamos
                executeDropItemTask();
            }
        }
    }

    /**
     * Hace lo necesario para que el aldeano vaya a un hospital y se cure
     */
    private void doHealTask() {
        int x = getX();
        int y = getY();
        int z = getZ();
        Cell cell = World.getCell(x, y, z);

        ArrayList<Zone> alZones = Game.getWorld().getZones();
        ZoneManagerItem zmi;
        Zone zone = null;

        // Miramos si está en un hospital (o en una litera encima de la zone)
        if (cell.hasZone()) {
            zone = Zone.getZone(cell.getZoneID());
        } else {
            // Miramos si está en una cama encima de la zone
            Item itemTmp = cell.getItem();
            if (itemTmp != null) {
                if (itemTmp.isLocked() && itemTmp.isOperative() && ItemManager.getItem(itemTmp.getIniHeader()).canBeUsedToSleep()) {
                    // Está en una cama, miramos a ver si abajo está el hospital
                    boolean bEnd = false;
                    int iIndexZ = z;
                    Cell cellAux;
                    while (!bEnd) {
                        iIndexZ++;
                        if (iIndexZ < World.MAP_DEPTH) {
                            cellAux = World.getCell(x, y, iIndexZ);
                            if (cellAux.getAstarZoneID() == cell.getAstarZoneID()) {
                                if (cellAux.hasZone()) {
                                    zone = Zone.getZone(cellAux.getZoneID());
                                    bEnd = true;
                                } else {
                                    // Seguimos mirando hacia abajo si está en un item cama
                                    itemTmp = cell.getItem();
                                    if (itemTmp == null || !ItemManager.getItem(itemTmp.getIniHeader()).canBeUsedToSleep()) {
                                        bEnd = true; // Dejamos de mirar
                                    }
                                }
                            } else {
                                bEnd = true; // Dejamos de mirar
                            }
                        } else {
                            bEnd = true; // Dejamos de mirar
                        }
                    }
                }
            }
        }
        if (zone != null) {
            // Tenemos la zona actual, miramos si es un hospital operativo
            zmi = ZoneManager.getItem(zone.getIniHeader());
            if (zmi.getType() == ZoneManagerItem.TYPE_HOSPITAL && zone.isOperative()) {
                // Guais, a curarse toca
                getCitizenData().setHealHealing(getCitizenData().getHealHealing() + 1);

                // Si está en cama se cura el doble
                Item itemTmp = cell.getItem();
                if (itemTmp != null) {
                    if (itemTmp.isLocked() && itemTmp.isOperative() && ItemManager.getItem(itemTmp.getIniHeader()).canBeUsedToSleep()) {
                        getCitizenData().setHealHealing(getCitizenData().getHealHealing() + 1);
                    }
                }

                if (getCitizenData().getHealHealing() >= World.TIME_MODIFIER_DAY) {
                    // Curado!
                    getLivingEntityData().setHealthPoints(getLivingEntityData().getHealthPointsMAXCurrent());

                    getCitizenData().setHealHealing(0);
                    resetTaskIndexes();
                }
                return;
            }
        }

        // No está en un hospital, buscamos uno
        Point3DShort p3d = null;
        Point3DShort p3dNearest = null;
        int iDistanceNearest = Utils.MAX_DISTANCE;

        for (int i = 0; i < alZones.size(); i++) {
            zone = alZones.get(i);
            zmi = ZoneManager.getItem(zone.getIniHeader());
            if (zmi.getType() == ZoneManagerItem.TYPE_HOSPITAL && zone.isOperative()) {
                // Tenemos un hospital, miramos si tiena alguna cama accesible
                p3d = Zone.getFreeSleepItemAtRandom(zone, cell.getAstarZoneID());

                if (p3d == null) {
                    // Miramos si tiena alguna celda accesible
                    p3d = Zone.getFreeCellAtRandom(zone, cell.getAstarZoneID());

                    if (p3d == null) {
                        continue;
                    }
                }

                // Tenemos una casilla, miramos distancia
                int iDistanceAux = Utils.getDistance(getCoordinates(), p3d);
                if (p3dNearest == null) {
                    p3dNearest = Point3DShort.getPoolInstance(p3d.x, p3d.y, p3d.z);
                    iDistanceNearest = iDistanceAux;
                } else {
                    if (iDistanceAux < iDistanceNearest) {
                        p3dNearest.setPoint(p3d.x, p3d.y, p3d.z);
                        iDistanceNearest = iDistanceAux;
                    }
                }
            }
        }

        if (p3dNearest != null) {
            // Coordenada de una celda del hospital, go, go, go
            setDestination(p3dNearest);
            Point3DShort.returnToPool(p3dNearest);
        } else {
            // No hay hospital, fin de tarea
            resetTaskIndexes();
        }
    }

    /**
     * Hace lo necesario para que el aldeano llegue hasta una cama (o suelo) y
     * duerma
     */
    private void doSleepTask() {
        executeDropLivingTask();

		// Si tiene zona personal vamos a ella
        // Antes de mirar nada miraremos si está en una cama, en ese caso no lo movemos pq ya está durmiendo
        boolean bEstaEnCama = false;
        Cell cell = World.getCell(getCoordinates());
        if (cell.hasItem()) {
            ItemManagerItem imi = ItemManager.getItem(cell.getEntity().getIniHeader());
            if (imi != null && imi.canBeUsedToSleep()) {
                bEstaEnCama = true;
            }
        }

        if (!bEstaEnCama) {
            // Buscamos su zona personal (o barracks)
            int iPersonalZoneID = getCitizenData().getZoneID();
            // Comprobamos barracks
            if (getSoldierData().isSoldier() && getSoldierData().getState() == SoldierData.STATE_IN_A_GROUP) {
                // Soldado con grupo, miramos si el grupo tiene zona
                SoldierGroupData sgd = Game.getWorld().getSoldierGroups().getGroup(getSoldierData().getGroup());
                if (sgd.hasZone()) {
                    iPersonalZoneID = sgd.getZoneID();
                }
            }
            Zone zone = Zone.getZone(iPersonalZoneID);

            if (zone != null) {
                // Tenemos su zona, miramos si estamos en ella
                if (cell.hasZone() && zone.isOperative() && cell.getZoneID() == zone.getID()) {
                    // Está en ella, no hacemos nada
                } else {
                    // No estamos en su zona personal, vamos a ella
                    Point3DShort p3d = Zone.getFreeSleepItemAtRandom(zone, cell.getAstarZoneID());
                    if (p3d == null) {
                        // No hay cama, buscamos cualquier casilla de la zona personal
                        p3d = Zone.getFreeCellAtRandom(zone, cell.getAstarZoneID());
                    }
                    if (p3d != null) {
                        setDestination(p3d);
                        return;
                    } else {
                        // Zona personal no accesible, no hacemos nada y que duerma allí donde esté
                    }
                }
            }
        }

		// Si llega aquí es que ya tenemos que dormir
        // Miramos si hay techo donde está
        // Miramos si hay un wall (iOver = 1) o un terrain encima (iOver = 2), lo primero que encontremos
        int iTechoCave = 0; // 1 - techo, 2 - cave
        int iZOver = getZ();
        Cell cellOVer;
        Item itemOver;
        while (iTechoCave == 0 && iZOver > 0) {
            iZOver--;
            cellOVer = World.getCell(getX(), getY(), iZOver);
            if (!cellOVer.isMined()) {
                // Si no está minada se acabó
                iTechoCave = 2; // Cave
            } else {
                itemOver = cellOVer.getItem();
                if (itemOver != null) {
                    // Item
                    if (ItemManager.getItem(itemOver.getIniHeader()).isWall()) {
                        iTechoCave = 1; // Techo
                    }
                }
            }
        }

        getCitizenData().setSleepSleeping(getCitizenData().getSleepSleeping() + 1);
        // Si estaba durmiendo bajo techo se despierta antes (5 horas en vez de 6)
        if (getCitizenData().getSleepSleeping() >= (((iTechoCave == 1) ? 5 : 6) * World.TIME_MODIFIER_HOUR)) { // Con techo duerme 5 horas, sin techo duerme 6
            getCitizenData().setSleepSleeping(0);
            resetTaskIndexes();
            if (iTechoCave == 1) { // techo
                // Con techo aguantará un 25% más de tiempo sin dormir
                getCitizenData().setSleep(getCitizenData().getMaxSleep() + (getCitizenData().getMaxSleep() / 4));
            } else if (iTechoCave == 2) { // cave
                // Con cave aguantará un 12.5% más de tiempo sin dormir
                getCitizenData().setSleep(getCitizenData().getMaxSleep() + (getCitizenData().getMaxSleep() / 8));
            } else {
                getCitizenData().setSleep(getCitizenData().getMaxSleep());
            }

            // Happiness counters
            getCitizenData().setHappinessWorkCounter(getCitizenData().calculateWorkCounter(getCitizenData().getSleep()));
            getCitizenData().setHappinessIdleCounter(getCitizenData().calculateIdleCounter(getCitizenData().getSleep()));
            getCitizenData().setHappinessWorkCounterMax(getCitizenData().getHappinessWorkCounter());
            getCitizenData().setHappinessIdleCounterMax(getCitizenData().getHappinessIdleCounter());

            // Happiness
            if (!getSoldierData().isSoldier()) {
                if (cell.hasItem()) {
                    ItemManagerItem imi = ItemManager.getItem(cell.getEntity().getIniHeader());
                    if (imi != null && imi.canBeUsedToSleep()) {
                        getCitizenData().setHappiness(getCitizenData().getHappiness() + 5 + imi.getHappiness());
                    }
                }
            }

            // Se cura MAX/20 PV (o sea, un 5%)
            if (getLivingEntityData().getHealthPoints() < getLivingEntityData().getHealthPointsMAXCurrent()) {
                int iHealth = getLivingEntityData().getHealthPointsMAXBase() / 20;
                if (iHealth < 1) {
                    iHealth = 1;
                }

                int iHPHealed = getLivingEntityData().getHealthPoints() + iHealth;
                if (iHPHealed > getLivingEntityData().getHealthPointsMAXCurrent()) {
                    iHPHealed = getLivingEntityData().getHealthPointsMAXCurrent();
                }
                getLivingEntityData().setHealthPoints(iHPHealed);
            }

            // Events después de dormir
            for (int i = 0; i < Game.getWorld().getEvents().size(); i++) {
                EventData ed = Game.getWorld().getEvents().get(i);
                EventManagerItem emi = EventManager.getItem(ed.getEventID());
                if (emi != null) {
                    ed.launchEffects(emi, emi.getEffectsAfterSleep(), this);
                }
            }
        }
    }

    /**
     * Hace lo necesario para autoequiparse a saco
     */
    private void doAutoEquipTask() {
        if (getCurrentTask().getParameter2() != null) {
            // El parameter2 indica que estamos buscando un item concreto
            int iItemID = Integer.parseInt(getCurrentTask().getParameter2());
            Item item = Item.getItemByID(iItemID);
            if (item == null) {
                // Quizá está en container, lo buscamos
                ArrayList<Container> alContainers = Game.getWorld().getContainers();
                ArrayList<Item> alContainerItems;
                breakcontainers:
                for (int i = 0; i < alContainers.size(); i++) {
                    alContainerItems = alContainers.get(i).getItemsInside();
                    for (int j = 0; j < alContainerItems.size(); j++) {
                        if (alContainerItems.get(j).getID() == iItemID) {
                            item = alContainerItems.get(j);
                            break breakcontainers;
                        }
                    }
                }
            }

            if (item != null) {
                if (item.getCoordinates().equals(getCoordinates())) {
                    // Estamos en el sitio, equipamos el item
                    Cell cell = World.getCell(item.getCoordinates());
                    Entity entity = cell.getEntity();
                    if (entity != null && entity instanceof Item) {
                        ItemManagerItem imi = ItemManager.getItem(entity.getIniHeader());
                        if (imi.isContainer()) {
                            Container container = Game.getWorld().getContainer(entity.getID());
                            if (container != null) {
                                MilitaryItem itemMilitar = (MilitaryItem) container.removeItem(iItemID);
                                if (itemMilitar != null) {
                                    // Si llevamos algo nos lo quitamos
                                    ItemManagerItem newItemIMI = ItemManager.getItem(itemMilitar.getIniHeader());
                                    MilitaryItem mi = unequip(newItemIMI.getLocation(), LivingEntity.TYPE_CITIZEN);

                                    // Equipamos
                                    equip(itemMilitar);

                                    // Dejamos el antiguo en el container, si no cabe lo dejamos en el carrying
                                    if (mi != null) {
                                        if (container.itemAllowed(mi)) {
                                            container.addItem(mi);
                                        } else {
                                            if (getCarrying() == null) {
                                                setCarrying(mi);
                                            } else {
                                                // Lo dejamos en un area de 3x3
                                                dropItem(mi, 1);
                                            }
                                        }
                                    }
                                }

                                getCurrentTask().setParameter2(null);
                                getCurrentTask().setPointIni((Point3D) null);
                            } else {
                                getCurrentTask().setParameter2(null);
                                getCurrentTask().setPointIni((Point3D) null);
                            }
                        } else {
                            if (imi.getLocation() == getCurrentTask().getPointIni().x) {
                                entity.delete(); // Lo borramos

                                // Si llevamos algo nos lo quitamos
                                ItemManagerItem newItemIMI = ItemManager.getItem(entity.getIniHeader());
                                MilitaryItem mi = unequip(newItemIMI.getLocation(), LivingEntity.TYPE_CITIZEN);

                                // Equipamos
                                equip((MilitaryItem) entity);

                                // Dejamos el antiguo en el suelo (area de 3x3)
                                if (mi != null) {
                                    dropItem(mi, 1);
                                }
                            }
                            getCurrentTask().setParameter2(null);
                            getCurrentTask().setPointIni((Point3D) null);
                        }
                    } else {
                        getCurrentTask().setParameter2(null);
                        getCurrentTask().setPointIni((Point3D) null);
                    }
                } else {
                    // Vamos a por el item
                    if (World.getCell(getCoordinates()).getAstarZoneID() == World.getCell(item.getCoordinates()).getAstarZoneID()) {
                        setDestination(item.getCoordinates());
                    } else {
                        getCurrentTask().setParameter2(null);
                        getCurrentTask().setPointIni((Point3D) null);
                    }
                }
            } else {
                getCurrentTask().setParameter2(null);
                getCurrentTask().setPointIni((Point3D) null);
            }
            return;
        }

		// Si llega aquí es que no hemos seteado nada para equiparse (o ya ha equipado algo)
        // Miramos si hay objetos militares en el mundo
        // Pillaremos los de mayor nivel para cada zona (cabeza, cuerpo, ...) (si el aldeano puede llegar al area, claro)
        Integer[] aItems = World.getItems().keySet().toArray(new Integer[0]);
        ArrayList<MilitaryItem> alHead = new ArrayList<MilitaryItem>(); // Head
        ArrayList<MilitaryItem> alBody = new ArrayList<MilitaryItem>(); // Body
        ArrayList<MilitaryItem> alLegs = new ArrayList<MilitaryItem>(); // Legs
        ArrayList<MilitaryItem> alFeet = new ArrayList<MilitaryItem>(); // Feet
        ArrayList<MilitaryItem> alWeapon = new ArrayList<MilitaryItem>(); // Weapon
        int iHeadLevel = -1;
        int iBodyLevel = -1;
        int iLegsLevel = -1;
        int iFeetLevel = -1;
        int iWeaponLevel = -1;

        int iASZID = World.getCell(getCoordinates()).getAstarZoneID();

        Item mi;
        ItemManagerItem imi;
        for (int i = 0; i < aItems.length; i++) {
            mi = World.getItems().get(aItems[i]);
            if (mi != null && mi instanceof MilitaryItem) {
                if (World.getCell(mi.getCoordinates()).getAstarZoneID() == iASZID && mi.getCoordinates().z <= Game.getWorld().getRestrictHaulEquippingLevel()) {
                    imi = ItemManager.getItem(mi.getIniHeader());
                    int location = imi.getLocation();
                    if (location == MilitaryItem.LOCATION_HEAD) {
                        if (imi.getLevel() > iHeadLevel) {
                            alHead.clear();
                            iHeadLevel = imi.getLevel();
                        }
                        if (imi.getLevel() == iHeadLevel) {
                            alHead.add((MilitaryItem) mi);
                        }
                    } else if (location == MilitaryItem.LOCATION_BODY) {
                        if (imi.getLevel() > iBodyLevel) {
                            alBody.clear();
                            iBodyLevel = imi.getLevel();
                        }
                        if (imi.getLevel() == iBodyLevel) {
                            alBody.add((MilitaryItem) mi);
                        }
                    } else if (location == MilitaryItem.LOCATION_LEGS) {
                        if (imi.getLevel() > iLegsLevel) {
                            alLegs.clear();
                            iLegsLevel = imi.getLevel();
                        }
                        if (imi.getLevel() == iLegsLevel) {
                            alLegs.add((MilitaryItem) mi);
                        }
                    } else if (location == MilitaryItem.LOCATION_FEET) {
                        if (imi.getLevel() > iFeetLevel) {
                            alFeet.clear();
                            iFeetLevel = imi.getLevel();
                        }
                        if (imi.getLevel() == iFeetLevel) {
                            alFeet.add((MilitaryItem) mi);
                        }
                    } else if (location == MilitaryItem.LOCATION_WEAPON) {
                        if (imi.getLevel() > iWeaponLevel) {
                            alWeapon.clear();
                            iWeaponLevel = imi.getLevel();
                        }
                        if (imi.getLevel() == iWeaponLevel) {
                            alWeapon.add((MilitaryItem) mi);
                        }
                    }
                }
            }
        }

        // Containers
        ArrayList<Container> alContainers = Game.getWorld().getContainers();
        ArrayList<Item> alContainerItems;
        nextContainer:
        for (int i = 0; i < alContainers.size(); i++) {
            Item itemContainer = Item.getItemByID(alContainers.get(i).getItemID());
            if (itemContainer != null && itemContainer.getCoordinates().z > Game.getWorld().getRestrictHaulEquippingLevel()) {
                continue;
            }
            alContainerItems = alContainers.get(i).getItemsInside();
            for (int j = 0; j < alContainerItems.size(); j++) {
                mi = alContainerItems.get(j);
                if (World.getCell(mi.getCoordinates()).getAstarZoneID() != iASZID) {
                    continue nextContainer;
                }
                if (mi != null && mi instanceof MilitaryItem) {
                    imi = ItemManager.getItem(mi.getIniHeader());
                    int location = imi.getLocation();
                    if (location == MilitaryItem.LOCATION_HEAD) {
                        if (imi.getLevel() > iHeadLevel) {
                            alHead.clear();
                            iHeadLevel = imi.getLevel();
                        }
                        if (imi.getLevel() == iHeadLevel) {
                            alHead.add((MilitaryItem) mi);
                        }
                    } else if (location == MilitaryItem.LOCATION_BODY) {
                        if (imi.getLevel() > iBodyLevel) {
                            alBody.clear();
                            iBodyLevel = imi.getLevel();
                        }
                        if (imi.getLevel() == iBodyLevel) {
                            alBody.add((MilitaryItem) mi);
                        }
                    } else if (location == MilitaryItem.LOCATION_LEGS) {
                        if (imi.getLevel() > iLegsLevel) {
                            alLegs.clear();
                            iLegsLevel = imi.getLevel();
                        }
                        if (imi.getLevel() == iLegsLevel) {
                            alLegs.add((MilitaryItem) mi);
                        }
                    } else if (location == MilitaryItem.LOCATION_FEET) {
                        if (imi.getLevel() > iFeetLevel) {
                            alFeet.clear();
                            iFeetLevel = imi.getLevel();
                        }
                        if (imi.getLevel() == iFeetLevel) {
                            alFeet.add((MilitaryItem) mi);
                        }
                    } else if (location == MilitaryItem.LOCATION_WEAPON) {
                        if (imi.getLevel() > iWeaponLevel) {
                            alWeapon.clear();
                            iWeaponLevel = imi.getLevel();
                        }
                        if (imi.getLevel() == iWeaponLevel) {
                            alWeapon.add((MilitaryItem) mi);
                        }
                    }
                }
            }
        }

        // Tenemos todos los items, vamos a ver qué le equipamos
        if (doAutoEquipInternal(alHead, MilitaryItem.LOCATION_HEAD) || doAutoEquipInternal(alBody, MilitaryItem.LOCATION_BODY) || doAutoEquipInternal(alLegs, MilitaryItem.LOCATION_LEGS) || doAutoEquipInternal(alFeet, MilitaryItem.LOCATION_FEET) || doAutoEquipInternal(alWeapon, MilitaryItem.LOCATION_WEAPON)) {
            return;
        }

        // Si llega aquí es que ya estamos
        getCurrentTask().setFinished(true);
        Game.getWorld().getTaskManager().removeCitizen(this);
    }

    private void dropItem(Item item, int radius) {
        int x = getX();
        int y = getY();
        int z = getZ();

        // Celda actual primero
        ItemManagerItem imi = ItemManager.getItem(item.getIniHeader());
        if (Item.isCellAvailableForItem(imi, x, y, z, false, true)) {
            item.init(x, y, z);
            item.setOperative(imi.isAlwaysOperative());
            item.setLocked(imi.isLocked());
            World.getCell(x, y, z).setEntity(item);
            return;
        }

        // Alrrededor
        for (x = (getX() - radius); x <= (getX() + radius); x++) {
            for (y = (getY() - radius); y <= (getY() + radius); y++) {
                if (Utils.isInsideMap(x, y, z)) {
                    if (Item.isCellAvailableForItem(imi, x, y, z, false, true)) {
                        item.init(x, y, z);
                        item.setOperative(imi.isAlwaysOperative());
                        item.setLocked(imi.isLocked());
                        World.getCell(x, y, z).setEntity(item);
                        return;
                    }
                }
            }
        }
    }

    private boolean doAutoEquipInternal(ArrayList<MilitaryItem> alList, int location) {
        // Si ya lleva algo sólo se cambiaría si lo que hay en la lista es mejor
        MilitaryItem mi = getEquippedData().getLocation(location);
        if (mi != null && alList.size() > 0) {
            int iCurrentLevel = ItemManager.getItem(mi.getIniHeader()).getLevel();
            int iListLevel = ItemManager.getItem(alList.get(0).getIniHeader()).getLevel();

            if (iCurrentLevel >= iListLevel) {
                return false;
            }
        }

        Cell citizenCell = World.getCell(getCoordinates());
        Cell cell;
        ArrayList<Integer> alIndexes = new ArrayList<Integer>();
        for (int i = 0; i < alList.size(); i++) {
            // Miramos por cada item si está en la zona del aldeano
            cell = World.getCell(alList.get(i).getCoordinates());
            if (cell.getAstarZoneID() == citizenCell.getAstarZoneID()) {
                // Item bueno, lo añadimos a la lista para luego hacer un random
                alIndexes.add(i);
            }
        }

        if (alIndexes.size() > 0) {
            int i = Utils.getRandomBetween(0, (alIndexes.size() - 1));
            // Lo tenemos, salimos y a por el
            getCurrentTask().setParameter2(Integer.toString(alList.get(alIndexes.get(i).intValue()).getID()));
            getCurrentTask().setPointIni(new Point3D(location, -1, -1));
            return true;
        }

        return false;
    }

    /**
     * Hace lo necesario para equipar un item
     */
    private void doWearTask() {
        if (getCarrying() != null) {
            dropCarryingItem();
            return;
        }

        Point3D p3dItem = getCurrentTask().getPointIni();
        Cell cellItem = World.getCell(p3dItem);

        // Miramos si estamos en la casilla del item
        if (getCoordinates().equals(p3dItem)) {
            // Estamos en destino, miramos si existe el item en la casilla
            if (cellItem.hasItem()) {
                if (cellItem.getEntity().getID() == Integer.parseInt(getCurrentTask().getParameter2())) {
					// ITEM DIRECTO
                    // Pillamos el item de la casilla
                    MilitaryItem item = (MilitaryItem) cellItem.getEntity();
                    cellItem.getEntity().delete(); // Lo borramos

                    // Si lleva algo se lo ponemos en el carrying
                    int location = ItemManager.getItem(item.getIniHeader()).getLocation();
                    if (getEquippedData().isWearing(location)) {
                        MilitaryItem itemDesequipado = unequip(location, LivingEntity.TYPE_CITIZEN);
                        setCarrying(itemDesequipado);
                    }

                    // Equipamos
                    equip((MilitaryItem) item);

                    // Tarea finalizada ok
                    getCurrentTask().setFinished(true);
                    Game.getWorld().getTaskManager().removeCitizen(this);
                } else {
                    if (ItemManager.getItem(cellItem.getEntity().getIniHeader()).isContainer()) {
                        // Container
                        Container container = Game.getWorld().getContainer(cellItem.getEntity().getID());
                        if (container != null) {
                            MilitaryItem item = (MilitaryItem) container.removeItem(Integer.parseInt(getCurrentTask().getParameter2()));
                            if (item != null) {
                                // Si lleva algo se lo ponemos en el carrying
                                int location = ItemManager.getItem(item.getIniHeader()).getLocation();
                                if (getEquippedData().isWearing(location)) {
                                    MilitaryItem itemDesequipado = unequip(location, LivingEntity.TYPE_CITIZEN);
                                    setCarrying(itemDesequipado);
                                }

                                // Equipamos
                                equip((MilitaryItem) item);

                                // Tarea finalizada ok
                                getCurrentTask().setFinished(true);
                                Game.getWorld().getTaskManager().removeCitizen(this);
                            } else {
                                // No puede equiparse
                                MessagesPanel.addMessage(MessagesPanel.TYPE_ANNOUNCEMENT, getCitizenData().getFullName() + Messages.getString("Citizen.1"), ColorGL.ORANGE, getCoordinates(), getID()); //$NON-NLS-1$
                                getCurrentTask().setFinished(true);
                                Game.getWorld().getTaskManager().removeCitizen(this);
                            }
                        } else {
                            // No puede equiparse
                            MessagesPanel.addMessage(MessagesPanel.TYPE_ANNOUNCEMENT, getCitizenData().getFullName() + Messages.getString("Citizen.1"), ColorGL.ORANGE, getCoordinates(), getID()); //$NON-NLS-1$
                            getCurrentTask().setFinished(true);
                            Game.getWorld().getTaskManager().removeCitizen(this);
                        }
                    } else {
                        // No es el item que toca
                        MessagesPanel.addMessage(MessagesPanel.TYPE_ANNOUNCEMENT, getCitizenData().getFullName() + Messages.getString("Citizen.0"), ColorGL.ORANGE, getCoordinates(), getID()); //$NON-NLS-1$
                        getCurrentTask().setFinished(true);
                        Game.getWorld().getTaskManager().removeCitizen(this);
                    }
                }
            } else {
                MessagesPanel.addMessage(MessagesPanel.TYPE_ANNOUNCEMENT, getCitizenData().getFullName() + Messages.getString("Citizen.1"), ColorGL.ORANGE, getCoordinates(), getID()); //$NON-NLS-1$
                getCurrentTask().setFinished(true);
                Game.getWorld().getTaskManager().removeCitizen(this);
            }
        } else {
            // No estamos en la casilla, nos movemos hacia ahí
            Cell cell = World.getCell(getCoordinates());

            if (cell.getAstarZoneID() == cellItem.getAstarZoneID()) {
                setDestination(p3dItem);
            } else {
                // No se puede llegar al item
                MessagesPanel.addMessage(MessagesPanel.TYPE_ANNOUNCEMENT, getCitizenData().getFullName() + Messages.getString("Citizen.5")); //$NON-NLS-1$
                getCurrentTask().setFinished(true);
                Game.getWorld().getTaskManager().removeCitizen(this);
            }

        }
    }

    /**
     * Hace lo necesario para desequipar un item
     */
    private void doWearOffTask() {
		// Se quita el item y lo ponemos en el carrying
        // Después lo dejamos en el suelo

        if (getCarrying() != null) {
            dropCarryingItem();
            return;
        }

        // Truquito, la X del p3d es el citID, la Y es el location del item
        int location = getCurrentTask().getPointIni().y;

        if (getEquippedData().isWearing(location)) {
            MilitaryItem itemDesequipado = unequip(location, LivingEntity.TYPE_CITIZEN);
            setCarrying(itemDesequipado);
        } else {
            getCurrentTask().setFinished(true);
            Game.getWorld().getTaskManager().removeCitizen(this);
        }
    }

    /**
     * Va hacia un enemigo para meterle la del pulpo
     */
	// private void doFightTask () {
    // if (getCarryingLiving () != null) {
    // executeDropLivingTask ();
    // }
    //
    // if (getCoordinates ().equals (getCurrentTask ().getPointIni ())) {
    // // Estamos en destino, fin de tarea
    // getCurrentTask ().setFinished (true);
    // resetTaskIndexes ();
    // } else {
    // // No estamos en destino, nos movemos
    // Cell cellDestino = World.getCell (getCurrentTask ().getPointIni ());
    // Cell cellOrigen = World.getCell (getCoordinates ());
    //
    // if (cellDestino.getAstarZoneID () == cellOrigen.getAstarZoneID ()) {
    // // Celda destino accesible, vamos !!!
    // setDestination (getCurrentTask ().getPointIni ());
    // } else {
    // // No hay camino, fin de tarea
    // getCurrentTask ().setFinished (true);
    // resetTaskIndexes ();
    // }
    // }
    // }
    /**
     * Hace lo necesario para que el aldeano construya un item y lo lleve a su
     * sitio
     */
    private void doCreateAndPlaceTask() {
        int x = getX();
        int y = getY();
        int z = getZ();
        Task task = getCurrentTask();

        // Si estamos cargando el item miramos si estamos en destino para soltarlo
        if (getCitizenData().getCarryingData().getCarrying() != null) {
            String sItemAux = ItemManager.getItem(task.getParameter()).getIniHeader();
            if (sItemAux.equals(getCitizenData().getCarryingData().getCarrying().getIniHeader())) { // Es el item adecuado, procedemos
                boolean bEnDestino = false;
                // Caso especial, bridges (o items que se ponen desde una casilla anterior a destino)
                if (getCitizenData().getCarryingData().getCarrying() instanceof Item && ItemManager.getItem(((Item) getCitizenData().getCarryingData().getCarrying()).getIniHeader()).canBeBuiltOnHoles()) {
                    ArrayList<Point3DShort> alPoints = Task.getAccesingPointsMatchingASZI(getCitizenData().getCarryingData().getCarrying().getCoordinates(), World.getCell(x, y, z).getAstarZoneID(), task.getTask());
                    // Miramos si estamos en alguno de los puntos
                    for (int i = 0; i < alPoints.size(); i++) {
                        if (getCoordinates().equals(alPoints.get(i))) {
                            bEnDestino = true;
                            break;
                        }
                    }
                } else {
                    bEnDestino = (getCitizenData().getCarryingData().getCarrying().getX() == x && getCitizenData().getCarryingData().getCarrying().getY() == y && getCitizenData().getCarryingData().getCarrying().getZ() == z);
                }
                if (bEnDestino) {
                    // Estamos en destino, soltamos el item lo ponemos operativo y se acabó la tarea
                    Cell cell = World.getCell(getCitizenData().getCarryingData().getCarrying().getCoordinates());
                    ItemManagerItem imi = ItemManager.getItem(getCarrying().getIniHeader());
                    if (cell.isEmpty() && Item.isCellAvailableForItem(imi, x, y, z, true, false)) {
                        getCarrying().setOperative(true);
                        getCarrying().setLocked(true);
                        getCarrying().init(getCitizenData().getCarryingData().getCarrying().getX(), getCitizenData().getCarryingData().getCarrying().getY(), getCitizenData().getCarryingData().getCarrying().getZ());
                        Stockpile.deleteStockpilePoint(getCitizenData().getCarryingData().getCarrying().getCoordinates());
                        cell.setEntity(getCitizenData().getCarryingData().getCarrying());
                        setCarrying(null);
                        task.setFinished(true);
                        Game.getWorld().getTaskManager().removeCitizen(this);
                    } else {
                        // Casilla ya contiene un entity, soltamos lo que tengamos y fin de tarea
                        task.setFinished(true);
                        dropCarryingItem(); // Esto ya saca al aldeano de la tarea
                        return;
                    }
                } else {
					// Tenemos el item pero no estamos en destino, nos movemos
                    // Caso especial, puentes (o items que se ponen desde una casilla anterior a destino)
                    // Nos moveremos al primer place adyacente disponible
                    if (getCitizenData().getCarryingData().getCarrying() instanceof Item && ItemManager.getItem(((Item) getCitizenData().getCarryingData().getCarrying()).getIniHeader()).canBeBuiltOnHoles()) {
                        ArrayList<Point3DShort> alPoints = Task.getAccesingPointsMatchingASZI(getCitizenData().getCarryingData().getCarrying().getCoordinates(), World.getCell(x, y, z).getAstarZoneID(), task.getTask());
                        if (alPoints.size() > 0) {
                            setDestination(alPoints.get(0));
                        } else {
                            // No se puede acceder, soltamos item y se acabó
                            dropCarryingItem();
                            return;
                        }
                    } else {
                        setDestination(getCitizenData().getCarryingData().getCarrying().getCoordinates());
                    }
                }
                return;
            } else {
                // Lleva un item que no es el que toca, puede ser un material para construirlo o cualquier otra cosa, no hacemos nada de momento
            }
        }

		// Si llegamos aquí es que no tenemos item, estará en el edificio, aún por construirse
        // Obtenemos el edificio y el item
        Item item = null;
        Building building = Building.getBuilding(task.getPointIni());
        if (building != null && building.getItemQueue().size() > 0) {
            item = building.getItemQueue().get(0);
        }

        if (building == null || item == null) {
            // Ocurre si el edificio es destruido o el item cancelado, marcamos la tarea como finalizada
            task.setFinished(true);
            Game.getWorld().getTaskManager().removeCitizen(this);
            return;
        }

        // Buscamos la entrada del edificio
        BuildingManagerItem bmi = BuildingManager.getItem(building.getIniHeader());
        Point3DShort p3dEntrance = bmi.getEntranceBaseCoordinates().merge(building.getCoordinates());

        // Ok, tenemos building y item, obtenemos los prerequisitos del item
        ArrayList<String> alPrerequisites = item.getPrerequisites();
        if (alPrerequisites == null || alPrerequisites.size() == 0) {
            // Perfecto, item construido
            if (getCarrying() != null) {
                dropCarryingItem();
                return;
            }

			// Si estamos en el edificio quitamos el item del edificio y lo llevamos a destino
            // En caso contrario nos movemos hasta el edificio
            // Obtenemos las coordenadas de la puerta
            if (p3dEntrance.equals(getCoordinates())) {
                setCarrying(building.getItemQueue().remove(0));
                // Caso especial, puentes (o items que se ponen en desde una casilla anterior a destino)
                if (getCitizenData().getCarryingData().getCarrying() instanceof Item && ItemManager.getItem(((Item) getCitizenData().getCarryingData().getCarrying()).getIniHeader()).canBeBuiltOnHoles()) {
                    // Iremos al primer punto accesible
                    ArrayList<Point3DShort> alPoints = Task.getAccesingPointsMatchingASZI(getCitizenData().getCarryingData().getCarrying().getCoordinates(), World.getCell(x, y, z).getAstarZoneID(), task.getTask());
                    if (alPoints.size() > 0) {
                        setDestination(alPoints.get(0));
                    } else {
                        // No se puede poner, soltamos el item
                        dropCarryingItem();
                        return;
                    }
                } else {
                    setDestination(item.getCoordinates());
                }
            } else {
                setDestination(p3dEntrance);
            }
            return;
        }

		// Item no construido, procedemos a llevar los materiales al edificio
        // Primero de todo miramos si llevamos un material bueno encima y estamos en la casilla del edificio
        if (getCitizenData().getCarryingData().getCarrying() != null) {
            // Llevamos algo encima, miramos si va bien para la construcción
            if (alPrerequisites.contains(getCitizenData().getCarryingData().getCarrying().getIniHeader())) {
                // Tenemos un material bueno, miramos si estamos en la casilla del edificio
                if (x == p3dEntrance.x && y == p3dEntrance.y && z == p3dEntrance.z) {
                    // Guais, eliminamos el prerequisito del item
                    alPrerequisites.remove(alPrerequisites.indexOf(getCitizenData().getCarryingData().getCarrying().getIniHeader()));
                    setCarrying(null);
                } else {
                    // No estamos ahí, nos movemos
                    setDestination(p3dEntrance);
                }
            } else {
                // Soltamos el material
                dropCarryingItem();
            }
            return;
        }

        // Buscamos el item más cercano que nos vaya bien
        Point3DShort p3dItem = Item.searchItem(false, getCoordinates(), UtilsIniHeaders.getIntsArray(alPrerequisites), true, Item.SEARCH_FALSE, Item.SEARCH_DOESNTMATTER, null, Game.getWorld ().getRestrictHaulEquippingLevel ());

        if (p3dItem == null) {
            Game.getWorld().getTaskManager().removeCitizen(this);
        } else {
            if (p3dItem.equals(getCoordinates())) {
                // Item en la casilla actual
                Cell cell = World.getCell(getCoordinates());

                if (ItemManager.getItem(cell.getEntity().getIniHeader()).isContainer()) {
                    // Container
                    Container container = Game.getWorld().getContainer(cell.getEntity().getID());
                    if (container != null) {
                        Item it = container.removeItemWithPrerequisites(alPrerequisites);
                        if (it != null) {
                            setCarrying(it); // Lo pillamos
                            // Tenemos el material, lo llevamos al edificio para construir el item
                            setDestination(p3dEntrance);
                        }
                    }
                } else {
                    // Item directo
                    setCarrying((Item) cell.getEntity()); // Lo pillamos
                    cell.getEntity().delete(); // Lo borramos

                    // Tenemos el material, lo llevamos al edificio para construir el item
                    setDestination(p3dEntrance);
                }

            } else {
                // Item por ahí
                setDestination(p3dItem);
            }
        }
    }

    /**
     * Saca al aldeano de la tarea actual y le crea una tarea de "DROP_ITEM"
     */
    private void dropCarryingItem() {
        dropCarryingItem(false);
    }

    /**
     * Saca al aldeano de la tarea actual y le crea una tarea de "DROP_ITEM"
     *
     * @param bSearch Indica si tiene que buscar un sitio adecuado para el item
     */
    private void dropCarryingItem(boolean bSearch) {
        if (getCarrying() == null && getCarryingLiving() == null) {
            return;
        }

        // Creamos tarea de "drop_item" para que suelte el material en alguna parte
        Game.getWorld().getTaskManager().removeCitizen(this);

        // Buscamos un punto bueno en LOS
        boolean bFound = false;
        if (bSearch) {
            int iLOS = (getLivingEntityData().getLOSCurrent() * 2);
            if (iLOS < 1) {
                iLOS = 1;
            }

            // Buscamos un barril o una stockpile que admita al objeto
            Item item = getCarrying();
            ItemManagerItem imi = ItemManager.getItem(item.getIniHeader());
            if (imi != null) {
                if (imi.getType() != null) {
					// Tiene que tener type, sino no va a haber ni pila ni container adecuados

                    // Primero miramos que la casilla actual no sea buena
                    if (!isGoodStockorContainerCell(item, getCoordinates())) {
						// Si no es buena miramos el LOS
                        // Miramos en LOS, haciendo "círculos" desde donde está
                        int iX, iY;
                        int x0 = getCoordinates().x;
                        int y0 = getCoordinates().y;
                        int z0 = getCoordinates().z;

                        losfor:
                        for (int i = 1; i <= iLOS; i++) {
                            // Arriba y abajo
                            for (int x = -i; x <= i; x++) {
                                if ((x0 + x) >= 0 && (x0 + x) < World.MAP_WIDTH) {
                                    // Arriba
                                    iY = y0 - i;
                                    if (iY >= 0) {
                                        Point3DShort p3ds = Point3DShort.getPoolInstance(x0 + x, iY, z0);
                                        if (isGoodStockorContainerCell(item, p3ds)) {
                                            bFound = true;
                                            setDestination(p3ds);
                                            break losfor;
                                        }
                                    }

                                    // Abajo
                                    iY = y0 + i;
                                    if (iY < World.MAP_HEIGHT) {
                                        Point3DShort p3ds = Point3DShort.getPoolInstance(x0 + x, iY, z0);
                                        if (isGoodStockorContainerCell(item, p3ds)) {
                                            bFound = true;
                                            setDestination(p3ds);
                                            break losfor;
                                        }
                                    }
                                }
                            }

                            // Izquierda y derecha
                            for (int y = -i; y <= i; y++) {
                                if ((y0 + y) >= 0 && (y0 + y) < World.MAP_HEIGHT) {
                                    // Izquierda
                                    iX = x0 - i;
                                    if (iX >= 0) {
                                        Point3DShort p3ds = Point3DShort.getPoolInstance(iX, y0 + y, z0);
                                        if (isGoodStockorContainerCell(item, p3ds)) {
                                            bFound = true;
                                            setDestination(p3ds);
                                            break losfor;
                                        }
                                    }

                                    // Derecha
                                    iX = x0 + i;
                                    if (iX < World.MAP_WIDTH) {
                                        Point3DShort p3ds = Point3DShort.getPoolInstance(iX, y0 + y, z0);
                                        if (isGoodStockorContainerCell(item, p3ds)) {
                                            bFound = true;
                                            setDestination(p3ds);
                                            break losfor;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Creamos la tarea de DROP (se ejecutará cuando llegue al destino)
        Task task = new Task(Task.TASK_DROP);
        if (bFound) {
            // Ponemos una marca para que no dejen el item a la primera casilla vacía
            task.setParameter("P");
        }
        setCurrentTask(task);
    }

    /**
     * Indica si la celda es buena para un item en concreto. Por buena se
     * entiende que haya una pila adecuada o un container.
     *
     * @param imi ItemManagerItem del objeto
     * @param p3ds Coordenadas de la celda
     * @return true si la celda es buena para un item en concreto
     */
    private boolean isGoodStockorContainerCell(Item item, Point3DShort p3ds) {
        Cell cell = World.getCell(p3ds);
        Item cellItem = cell.getItem();
        ItemManagerItem imi = ItemManager.getItem(item.getIniHeader());
        ItemManagerItem cellImi;
        if (cellItem != null) {
            cellImi = ItemManager.getItem(cellItem.getIniHeader());
        } else {
            cellImi = null;
        }

        if (cell.hasStockPile() || (cellImi != null && cellImi.isContainer())) {
			// Pila o container encontrado

            // Pila, para que sea válido no tiene que haber item en la celda
            if (cell.hasStockPile() && cellImi == null) {
                // Miramos que la pila sea buena
                Stockpile pile = Stockpile.getStockpile(p3ds);

                if (pile != null && pile.itemAllowed(imi.getIniHeader())) {
                    return true;
                }
            }

            // Container
            if (cellImi != null && cellImi.isContainer()) {
                Container container = Game.getWorld().getContainer(cellItem.getID());

                if (container != null && container.itemAllowed(item)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Hace lo necesario para ejecutar una custom action
     */
    private void doCustomActionTask() {
        // Cola
        if (getCurrentCustomAction().getQueue().size() == 0) {
            // Finish
            getCurrentTask().setFinished(true);
            Game.getWorld().getTaskManager().removeCitizen(this);
            return;
        }

        // Obtenemos el primer item de la cola
        QueueItem qi = getCurrentCustomAction().getQueue().get(0);

        // Miramos que tipo de item es
        if (qi.getType() == QueueItem.TYPE_CREATE_ITEM) {
            ItemManagerItem imi = ItemManager.getItem(qi.getValue());
            getCurrentCustomAction().getQueueData().setLastQueueItem(null);
            // Creamos un item, se lo ponemos en el carrying, si llevaba algo lo pierde
            Item item = Item.createItem(imi);
            item.setOperative(imi.isAlwaysOperative());

            if (item instanceof MilitaryItem) {
                // Prefijo
                if (Utils.getRandomBetween(1, 100) <= 2) { // 2% de tener prefijo
                    ((MilitaryItem) item).setPrefix(PrefixSuffixManager.getRandomPrefix().getRandom());
                }
                // Sufijo
                if (Utils.getRandomBetween(1, 100) <= 2) { // 2% de tener sufijo
                    ((MilitaryItem) item).setSuffix(PrefixSuffixManager.getRandomSuffix().getRandom());
                }
            }
            if (imi.isCanBeRotated()) {
                item.setFacing(getCurrentCustomAction().getFace());
            }

            setCarrying(item);
            getCurrentCustomAction().getQueue().remove(0);
            QueueItem qiPlace = new QueueItem();
            qiPlace.setType(QueueItem.TYPE_PLACE_ITEM);
            qiPlace.setValueNoException(imi.getIniHeader());
            getCurrentCustomAction().getQueue().add(0, qiPlace);
        } else if (qi.getType() == QueueItem.TYPE_CREATE_ITEM_BY_TYPE) {
            ArrayList<String> alItems = ItemManager.getItemsByType(qi.getValue());
            if (alItems != null && alItems.size() > 0) {
                ItemManagerItem imi = ItemManager.getItem(alItems.get(Utils.getRandomBetween(0, (alItems.size() - 1))));
                getCurrentCustomAction().getQueueData().setLastQueueItem(null);
                // Creamos un item, se lo ponemos en el carrying, si llevaba algo lo pierde
                Item item = Item.createItem(imi);
                item.setOperative(imi.isAlwaysOperative());

                if (item instanceof MilitaryItem) {
                    // Prefijo
                    if (Utils.getRandomBetween(1, 100) <= 2) { // 2% de tener prefijo
                        ((MilitaryItem) item).setPrefix(PrefixSuffixManager.getRandomPrefix().getRandom());
                    }
                    // Sufijo
                    if (Utils.getRandomBetween(1, 100) <= 2) { // 2% de tener sufijo
                        ((MilitaryItem) item).setSuffix(PrefixSuffixManager.getRandomSuffix().getRandom());
                    }
                }

                setCarrying(item);
                getCurrentCustomAction().getQueue().remove(0);
                QueueItem qiPlace = new QueueItem();
                qiPlace.setType(QueueItem.TYPE_PLACE_ITEM);
                qiPlace.setValueNoException(imi.getIniHeader());
                getCurrentCustomAction().getQueue().add(0, qiPlace);
            } else {
                getCurrentCustomAction().getQueue().remove(0);
                Log.log(Log.LEVEL_ERROR, Messages.getString("TaskManager.11") + " [" + qi.getValue() + "]", getClass().toString()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
        } else if (qi.getType() == QueueItem.TYPE_PLACE_ITEM) {
            getCurrentCustomAction().getQueueData().setLastQueueItem(null);
            Point3DShort destPoint = getCurrentCustomAction().getDestinationPoint();
            if (destPoint != null) {
                // Llevamos algo, miramos si es lo que toca
                if (getCarrying() != null && getCarrying().getIniHeader().equals(qi.getValue())) {
					// Es el item bueno
                    // Miramos si estamos en destino
                    boolean bEnDestino = false;
                    // Caso especial, bridges (o items que se ponen desde una casilla anterior a destino)
                    if (getCitizenData().getCarryingData().getCarrying() instanceof Item && ItemManager.getItem(((Item) getCitizenData().getCarryingData().getCarrying()).getIniHeader()).canBeBuiltOnHoles()) {
                        ArrayList<Point3DShort> alPoints = Task.getAccesingPointsMatchingASZI(destPoint, World.getCell(getCoordinates()).getAstarZoneID(), getCurrentTask().getTask());
                        // Miramos si estamos en alguno de los puntos
                        for (int i = 0; i < alPoints.size(); i++) {
                            if (getCoordinates().equals(alPoints.get(i))) {
                                bEnDestino = true;
                                break;
                            }
                        }
                    } else {
                        bEnDestino = destPoint.equals(getCoordinates());
                    }

                    if (bEnDestino) {
                        // Estamos en destino, soltamos el item y pacasa
                        Cell cell = World.getCell(destPoint);

                        ItemManagerItem imi = ItemManager.getItem(qi.getValue());
                        if (cell.isEmpty() && Item.isCellAvailableForItem(imi, destPoint, true, true)) {
                            getCarrying().setOperative(true);
                            getCarrying().setLocked(true);
                            getCarrying().init(destPoint.x, destPoint.y, destPoint.z);
                            if (imi.isCanBeRotated()) {
                                getCarrying().setFacing(getCurrentCustomAction().getFace());
                            }
                            Stockpile.deleteStockpilePoint(destPoint);
                            cell.setEntity(getCitizenData().getCarryingData().getCarrying());
                            setCarrying(null);
                        }
                        getCurrentCustomAction().getQueue().remove(0);
                    } else {
						// Tenemos el item pero no estamos en destino, nos movemos
                        // Caso especial, puentes (o items que se ponen desde una casilla anterior a destino)
                        // Nos moveremos al primer place adyacente disponible
                        if (getCitizenData().getCarryingData().getCarrying() instanceof Item && ItemManager.getItem(((Item) getCitizenData().getCarryingData().getCarrying()).getIniHeader()).canBeBuiltOnHoles()) {
                            ArrayList<Point3DShort> alPoints = Task.getAccesingPointsMatchingASZI(destPoint, World.getCell(getCoordinates()).getAstarZoneID(), getCurrentTask().getTask());
                            if (alPoints.size() > 0) {
                                setDestination(alPoints.get(0));
                            } else {
                                // No se puede acceder
                                World.getCell(destPoint).setFlagOrders(false);
                                getCurrentCustomAction().getQueue().remove(0);
                            }
                        } else {
                            setDestination(destPoint);
                        }
                    }
                } else {
                    // No lelvamos item o no es el que toca ¿?¿? ya estamos
                    World.getCell(destPoint).setFlagOrders(false);
                    getCurrentCustomAction().getQueue().remove(0);
                }
            } else {
                // No hay que moverlo, ya estamos
                getCurrentCustomAction().getQueue().remove(0);
            }
        } else if (qi.getType() == QueueItem.TYPE_DESTROY_ITEM) {
            setCarrying(null);
            getCurrentCustomAction().getQueue().remove(0);
            getCurrentCustomAction().getQueueData().setLastQueueItem(null);
        } else if (qi.getType() == QueueItem.TYPE_DESTROY_CELL_ITEM) {
            Item.delete(getCoordinates());
            getCurrentCustomAction().getQueue().remove(0);
            getCurrentCustomAction().getQueueData().setLastQueueItem(null);
        } else if (qi.getType() == QueueItem.TYPE_REPLACE_CELL_ITEM) {
            Entity currentEntity = World.getCell(getCoordinates()).getEntity();
            boolean bCurrent = (currentEntity != null && currentEntity instanceof Item);
            boolean bCurrentLocked = bCurrent && ((Item) currentEntity).isLocked();
            int iCurrentFace = Item.FACE_WEST;
            if (bCurrent) {
                iCurrentFace = ((Item) currentEntity).getFacing();
            }
            Item.delete(getCoordinates());
            getCurrentCustomAction().getQueue().remove(0);
            getCurrentCustomAction().getQueueData().setLastQueueItem(null);
            ItemManagerItem imi = ItemManager.getItem(qi.getValue());
            if (Item.isCellAvailableForItem(imi, getCoordinates().x, getCoordinates().y, getCoordinates().z, true, true, true, true)) {
                Item newItem = Item.createItem(imi);
                newItem.setOperative(true);
                Cell cell = World.getCell(getCoordinates());
//				if (cell.hasStockPile () || cell.hasZone ()) { // fix per el bluesteel
                if (cell.hasStockPile()) {
                    newItem.setLocked(false);
                } else {
                    if (bCurrent) {
                        newItem.setLocked(bCurrentLocked);
                    } else {
                        newItem.setLocked(imi.isLocked());
                    }
                }
                newItem.init(getX(), getY(), getZ());
                if (imi.isCanBeRotated()) {
                    newItem.setFacing(iCurrentFace);
                }
                cell.setEntity(newItem);
            }
        } else if (qi.getType() == QueueItem.TYPE_MOVE_TERRAIN) {
            // Move terrain
            if (getCurrentCustomAction().getTerrainPoint() != null) {
                // Miramos si estamos en destino
                if (getCoordinates().equals(getCurrentCustomAction().getTerrainPoint())) {
                    // Ya estamos
                    getCurrentCustomAction().getQueueData().setLastQueueItem(getCurrentCustomAction().getQueue().remove(0));
                } else {
                    setDestination(getCurrentCustomAction().getTerrainPoint());
                }
            } else {
                getCurrentCustomAction().getQueue().remove(0);
                getCurrentCustomAction().getQueueData().setLastQueueItem(null);
            }
        } else if (qi.getType() == QueueItem.TYPE_CHANGE_TERRAIN) {
            // Se aplica a la cell de abajo (obviamente... ejem)
            if (getCoordinates().z < (World.MAP_DEPTH - 1)) {
                Cell cell = World.getCell(getCoordinates().x, getCoordinates().y, getCoordinates().z + 1);
                TerrainManagerItem tmi = TerrainManager.getItem(qi.getValue());
                int iAdd = cell.getTerrain().getTerrainTileID() - (cell.getTerrain().getTerrainID() * TerrainManager.SLOPES_INIHEADER.length);
                cell.getTerrain().setTerrainID(tmi.getTerrainID());
                cell.getTerrain().setTerrainTileID((tmi.getTerrainID() * TerrainManager.SLOPES_INIHEADER.length) + iAdd);

                // Se quita el flag de la deabajo (obvio.... cough, cough)
                cell = World.getCell(getCoordinates());
                cell.setFlagOrders(false);
            }

            getCurrentCustomAction().getQueue().remove(0);
            getCurrentCustomAction().getQueueData().setLastQueueItem(null);

            // Update tutorial flow
            Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_TILL, 0, null);
        } else if (qi.getType() == QueueItem.TYPE_DESTROY_FRIENDLY) {
            setCarryingLiving(null);
            getCurrentCustomAction().getQueue().remove(0);
            getCurrentCustomAction().getQueueData().setLastQueueItem(null);
        } else if (qi.getType() == QueueItem.TYPE_CREATE_FRIENDLY) {
            setCarryingLiving(null);
            Friendly friendly = new Friendly(qi.getValue());
            setCarryingLiving(friendly);
            getCurrentCustomAction().getQueue().remove(0);
            getCurrentCustomAction().getQueueData().setLastQueueItem(null);
        } else if (qi.getType() == QueueItem.TYPE_WAIT) {
            if (getCurrentCustomAction().getQueueData().getWaitCounter() < 0) {
                // Justo empieza a esperar
                try {
                    // Miramos si estamos encima de un item que da speedUp
                    Cell currentCell = World.getCell(getCoordinates());
                    int speedUpPCT = 100;
                    if (currentCell.getEntity() != null && currentCell.getEntity() instanceof Item) {
                        ItemManagerItem imi = ItemManager.getItem(currentCell.getEntity().getIniHeader());
                        speedUpPCT = imi.getSpeedUpPCT();
                    }
                    int turns = Integer.parseInt(qi.getValue());
                    turns = (turns * 100) / speedUpPCT;

					// 105% (o sea, más lento) si el aldeano no está contento
//					if (getCitizenData ().getHappiness () < 20) {
//						turns = (turns * 105) / 100;
//					}
                    // Nueva fórmula de happiness (de 0 a 50 la velocidad se ve afectada, de 50% a 100% más lento)
                    int iPCTHappiness = 100 - getCitizenData().getHappiness();
                    if (iPCTHappiness >= 50) {
                        turns = (turns * (100 + iPCTHappiness)) / 100;
                    }

                    // Boost debido a los soldados
                    if (getCitizenData().getBoostCounter() > 0) {
                        turns = (turns * SoldierData.BOOST_PCT_BOSS_AROUND_WORK) / 100;
                    }

					// 75% turns if roof / 85% turns underground
                    // Miramos si hay un wall (iOver = 1) o un terrain encima (iOver = 2), lo primero que encontremos
                    int iOver = 0;
                    int iZOver = getZ();
                    Cell cellOVer;
                    Item itemOver;
                    while (iZOver > 0) {
                        iZOver--;
                        cellOVer = World.getCell(getX(), getY(), iZOver);
                        itemOver = cellOVer.getItem();
                        if (itemOver != null) {
                            // Item
                            if (ItemManager.getItem(itemOver.getIniHeader()).isWall()) {
                                iOver = 1;
                                iZOver = -1;
                            }
                        } else {
                            // Comprobamos terrain
                            if (cellOVer.getTerrain().getTerrainID() != TerrainManagerItem.TERRAIN_AIR_ID) {
                                // Terreno!
                                iOver = 2;
                                iZOver = -1;
                            }
                        }
                    }

                    // Ya hemos comprobado, aplicamos el bonus si hace falta
                    if (iOver == 1) {
                        // 75% "roof" (wall)
                        turns = (turns * 75) / 100;
                    } else if (iOver == 2) {
                        // 85% underground
                        turns = (turns * 85) / 100;
                    }

                    // Events
                    turns = (turns * Game.getWorld().getGlobalEvents().getWaitPCT()) / 100;

                    if (turns < 1) {
                        turns = 1;
                    }

                    getCurrentCustomAction().getQueueData().setWaitCounter(turns);

                    // Audio (no loop)
                    if (qi.getFx() != null && qi.getFxTurns() == 0) {
                        UtilsAL.play(qi.getFx(), getCoordinates().z);
                    }
                } catch (NumberFormatException nfe) {
                    Log.log(Log.LEVEL_ERROR, Messages.getString("Citizen.33") + qi.getValue() + "]", getClass().toString()); //$NON-NLS-1$ //$NON-NLS-2$
                    getCurrentTask().setFinished(true);
                    Game.getWorld().getTaskManager().removeCitizen(this);
                }
            } else {
                // Audio
                if (qi.getFx() != null && qi.getFxTurns() > 0) {
                    if (getCurrentCustomAction().getQueueData().getWaitCounter() % qi.getFxTurns() == 0) {
                        UtilsAL.play(qi.getFx(), getCoordinates().z);
                    }
                }

                // Está esperando
                if (getCurrentCustomAction().getQueueData().getWaitCounter() == 0) {
                    // Fin de la espera
                    getCurrentCustomAction().getQueueData().setWaitCounter(-1);
                    getCurrentCustomAction().getQueueData().setLastQueueItem(getCurrentCustomAction().getQueue().remove(0));
                } else {
                    getCurrentCustomAction().getQueueData().setWaitCounter(getCurrentCustomAction().getQueueData().getWaitCounter() - 1);
                }
            }
        } else if (qi.getType() == QueueItem.TYPE_UNLOCK) {
            getCurrentCustomAction().getQueueData().setItemIDCurrentPlace(-1);
            getCurrentCustomAction().getQueue().remove(0);
            getCurrentCustomAction().getQueueData().setLastQueueItem(null);
        } else if (qi.getType() == QueueItem.TYPE_PICK) {
            // Pick item
            getCurrentCustomAction().getQueueData().setItemIDPick(-1);

            // Miramos si en la celda actual tenemos el item
            Cell cell = World.getCell(getCoordinates());
            Entity entity = cell.getEntity();
            ArrayList<String> alPicks = Utils.getArray(qi.getValue());
            if (entity != null && entity instanceof Item && alPicks.contains(entity.getIniHeader())) {
                boolean todoOK = false;
                // Pick
                if (!((Item) entity).isLocked()) {
					// Si es tarea de PICK lo pillamos y ya estamos, en otro caso ya estamos
                    // Lo pillamos, si tenia algo en el carrying adiós muy buenas
                    setCarrying((Item) cell.getEntity()); // Lo pillamos
                    cell.getEntity().delete(); // Lo borramos
                    todoOK = true;
                }
                if (todoOK) {
                    getCurrentCustomAction().getQueueData().setLastQueueItem(getCurrentCustomAction().getQueue().remove(0));
                    return;
                }
            } else {
                // Containers
                if (entity != null && entity instanceof Item && ItemManager.getItem(entity.getIniHeader()).isContainer()) {
                    // Container
                    Container container = Game.getWorld().getContainer(entity.getID());
                    if (container != null) {
                        // Lo pillamos, si tenia algo en el carrying adiós muy buenas
                        Item it = container.removeItemWithPrerequisites(alPicks);
                        if (it != null) {
                            setCarrying(it); // Lo pillamos
                            getCurrentCustomAction().getQueueData().setLastQueueItem(getCurrentCustomAction().getQueue().remove(0));
                            return;
                        }
                    }
                }
            }

            // No hay item donde estamos, lo buscamos
            Point3DShort p3dCoordenadas = null;

            // Obtenemos una lista de items "en uso" para que al buscarlos no nos de esos
            ArrayList<Integer> alItemsInUse = Item.searchItemInUse(getID());
            p3dCoordenadas = Item.searchItem(false, getCoordinates(), UtilsIniHeaders.getIntsArray(alPicks), true, Item.SEARCH_FALSE, Item.SEARCH_DOESNTMATTER, alItemsInUse, Game.getWorld ().getRestrictHaulEquippingLevel ());
            if (p3dCoordenadas == null) {
                // No existe item, salimos de la tarea
                getCurrentTask().setFinished(false);
                Game.getWorld().getTaskManager().removeCitizen(this);
                return;
            }

            entity = World.getCell(p3dCoordenadas).getEntity();
            if (entity != null && entity instanceof Item) {
                // Pick
                if (ItemManager.getItem(entity.getIniHeader()).isContainer()) {
                    // Container
                    Container container = Game.getWorld().getContainer(entity.getID());
                    if (container != null) {
                        for (int c = 0; c < container.getItemsInside().size(); c++) {
                            if (alPicks.contains(container.getItemsInside().get(c).getIniHeader())) {
                                getCurrentCustomAction().getQueueData().setItemIDPick(container.getItemsInside().get(c).getID());
                                break;
                            }
                        }
                    }
                } else {
                    getCurrentCustomAction().getQueueData().setItemIDPick(entity.getID());
                }
            }

            // Tenemos coordenadas para movernos
            setDestination(p3dCoordenadas);
        } else if (qi.getType() == QueueItem.TYPE_MOVE) {
            // Move
            Cell cell = World.getCell(getCoordinates());
            Entity entity = cell.getEntity();
            ArrayList<String> alMoves = Utils.getArray(qi.getValue());
            if (qi.isUseSource() && getCurrentCustomAction().getEntityID() != -1) {
                // En caso de tarea sobre un entity sustituimos los <move>loquesea por <move>alEntity
                if (entity != null && entity instanceof Item && entity.getID() == getCurrentCustomAction().getEntityID()) {
                    getCurrentCustomAction().getQueueData().setLastQueueItem(getCurrentCustomAction().getQueue().remove(0));
                    return;
                }
            } else {
                // Miramos si en la celda actual tenemos el item
                if (entity != null && entity instanceof Item && alMoves.contains(entity.getIniHeader())) {
                    // if (((Item) entity).isLocked () && ((Item) entity).isOperative ()) {
                    if (((Item) entity).isOperative()) {
                        getCurrentCustomAction().getQueueData().setLastQueueItem(getCurrentCustomAction().getQueue().remove(0));
                        return;
                    }
                }
            }

            // No hay item donde estamos, lo buscamos
            Point3DShort p3dCoordenadas = null;
			// Antes de buscar miramos si tenemos algo bloqueado, en ese caso iremos ahí
            // (para dar coherencia a la construcción, que si no pueden construir un item en distintas mesas de carpintero por ejemplo)
            if (getCurrentCustomAction().getQueueData().getItemIDCurrentPlace() != -1) {
                Item it = Item.getItemByID(getCurrentCustomAction().getQueueData().getItemIDCurrentPlace());
                if (it != null && it.isOperative() && alMoves.contains(it.getIniHeader())) {
                    // Lo tenemos!!!
                    p3dCoordenadas = it.getCoordinates();
                } else {
                    // El item "bloqueado" no existe o no nos vale. Quitamos el bloqueo
                    getCurrentCustomAction().getQueueData().setItemIDCurrentPlace(-1);
                }
            }
            if (p3dCoordenadas == null) {
                // No teniamos item bloqueado, lo buscamos normalmente

                if (qi.isUseSource() && getCurrentCustomAction().getEntityID() != -1) {
                    // Move fijo
                    Item item = Item.getItemByID(getCurrentCustomAction().getEntityID());
                    if (item != null) {
                        p3dCoordenadas = Point3DShort.getPoolInstance(item.getX(), item.getY(), item.getZ());
                    } else {
                        // No existe item, salimos de la tarea y fin de tarea
                        getCurrentTask().setFinished(true);
                        Game.getWorld().getTaskManager().removeCitizen(this);
                        return;
                    }
                } else {
                    // Obtenemos una lista de items "en uso" para que al buscarlos no nos de esos
                    ArrayList<Integer> alItemsInUse = Item.searchItemInUse(getID());
                    // p3dCoordenadas = Item.searchItem (getCoordinates (), alPicks, true, Item.SEARCH_TRUE, Item.SEARCH_TRUE, alItemsInUse, true);
                    p3dCoordenadas = Item.searchItem(false, getCoordinates(), UtilsIniHeaders.getIntsArray(alMoves), true, Item.SEARCH_DOESNTMATTER, Item.SEARCH_TRUE, alItemsInUse, false, false, Game.getWorld ().getRestrictHaulEquippingLevel ());
                }

            }
            if (p3dCoordenadas == null) {
                // No existe item, salimos de la tarea
                getCurrentTask().setFinished(false);
                Game.getWorld().getTaskManager().removeCitizen(this);
                return;
            }

            entity = World.getCell(p3dCoordenadas).getEntity();
            if (entity != null && entity instanceof Item) {
                getCurrentCustomAction().getQueueData().setItemIDCurrentPlace(entity.getID());
            }

            // Tenemos coordenadas para movernos
            setDestination(p3dCoordenadas);
        } else if (qi.getType() == QueueItem.TYPE_LOCK) {
            // Lock
            ArrayList<String> alMoves = Utils.getArray(qi.getValue());

            // Antes de buscar miramos si tenemos algo bloqueado, en ese caso ya estamos
            if (getCurrentCustomAction().getQueueData().getItemIDCurrentPlace() != -1) {
                Item it = Item.getItemByID(getCurrentCustomAction().getQueueData().getItemIDCurrentPlace());
                if (it != null && it.isOperative() && alMoves.contains(it.getIniHeader())) {
                    // Lo tenemos!!!
                    getCurrentCustomAction().getQueue().remove(0);
                    return;
                } else {
                    // El item "bloqueado" no existe o no nos vale. Quitamos el bloqueo
                    getCurrentCustomAction().getQueueData().setItemIDCurrentPlace(-1);
                }
            }

			// Si llega aquí es que tenemos que bloquear un item
            // No teniamos item bloqueado, lo buscamos normalmente
            Point3DShort p3dCoordenadas;
            if (qi.isUseSource() && getCurrentCustomAction().getEntityID() != -1) {
                // Lock fijo
                Item item = Item.getItemByID(getCurrentCustomAction().getEntityID());
                if (item != null) {
                    p3dCoordenadas = Point3DShort.getPoolInstance(item.getX(), item.getY(), item.getZ());
                } else {
                    // No existe item, salimos de la tarea y fin de tarea
                    getCurrentTask().setFinished(true);
                    Game.getWorld().getTaskManager().removeCitizen(this);
                    return;
                }
            } else {
                // Obtenemos una lista de items "en uso" para que al buscarlos no nos de esos
                ArrayList<Integer> alItemsInUse = Item.searchItemInUse(getID());
                // p3dCoordenadas = Item.searchItem (getCoordinates (), alPicks, true, Item.SEARCH_TRUE, Item.SEARCH_TRUE, alItemsInUse, true);
                p3dCoordenadas = Item.searchItem(false, getCoordinates(), UtilsIniHeaders.getIntsArray(alMoves), true, Item.SEARCH_DOESNTMATTER, Item.SEARCH_TRUE, alItemsInUse, false, false, Game.getWorld ().getRestrictHaulEquippingLevel ());
            }

            if (p3dCoordenadas == null) {
                // No existe item, salimos de la tarea
                getCurrentTask().setFinished(false);
                Game.getWorld().getTaskManager().removeCitizen(this);
                return;
            }

            // Lockeamos
            Entity entity = World.getCell(p3dCoordenadas).getEntity();
            if (entity != null && entity instanceof Item) {
                getCurrentCustomAction().getQueueData().setItemIDCurrentPlace(entity.getID());
            }

            // Ya estamos
            getCurrentCustomAction().getQueue().remove(0);

        } else if (qi.getType() == QueueItem.TYPE_PICK_FRIENDLY) {
            getCurrentCustomAction().getQueueData().setLivingIDPick(-1);

            // Miramos si está en la celda actual
            LivingEntity le = null;
            ArrayList<String> alPicks = Utils.getArray(qi.getValue());
            if (qi.isUseSource() && getCurrentCustomAction().getEntityID() != -1) {
                le = World.getLivingEntityByID(getCurrentCustomAction().getEntityID());
                if (le == null || !le.getCoordinates().equals(getCoordinates())) {
                    le = null;
                }
            } else {
                for (int p = 0; p < alPicks.size(); p++) {
                    le = getLivingByCoordinatesAndIniHeader(getCoordinates(), alPicks.get(p));
                    if (le != null) {
                        break;
                    }
                }
            }

            if (le != null) {
				// Lo pillamos y ya estamos, en otro caso ya estamos
                // Si tenia algo en el carrying adiós muy buenas
                setCarryingLiving(le); // Lo pillamos
                le.delete(false); // Lo "borramos"
                getCurrentCustomAction().getQueueData().setLastQueueItem(getCurrentCustomAction().getQueue().remove(0));
                return;
            }

            // Lo buscamos
            if (qi.isUseSource() && getCurrentCustomAction().getEntityID() != -1) {
                le = World.getLivingEntityByID(getCurrentCustomAction().getEntityID());
            } else {
                // Obtenemos una lista de livings "en uso" para que al buscarlos no nos de esos
                ArrayList<Integer> alLivingsInUse = searchLivingsInUse(getID());
                le = searchLiving(getCoordinates(), UtilsIniHeaders.getIntsArray(alPicks), true, alLivingsInUse);
            }

            if (le == null) {
                // No existe la living, salimos de la tarea
                getCurrentTask().setFinished(false);
                Game.getWorld().getTaskManager().removeCitizen(this);
                return;
            }

            // Tenemos el punto de la living
            getCurrentCustomAction().getQueueData().setLivingIDPick(le.getID());

            // Tenemos coordenadas para movernos
            setDestination(le.getCoordinates());
        } else if (qi.getType() == QueueItem.TYPE_REVIVE_HEROES) {
            getCurrentCustomAction().getQueue().remove(0);
            getCurrentCustomAction().getQueueData().setLastQueueItem(null);
            Game.getWorld().resetOldHeroesDied();
        } else if (qi.getType() == QueueItem.TYPE_DELETE_COINS) {
            int iAmmount = Utils.launchDice(qi.getValue());
            getCurrentCustomAction().getQueue().remove(0);
            getCurrentCustomAction().getQueueData().setLastQueueItem(null);
            Game.getWorld().setCoins(Game.getWorld().getCoins() - iAmmount);
            if (Game.getWorld().getCoins() < 0) {
                Game.getWorld().setCoins(0);
            }
        } else if (qi.getType() == QueueItem.TYPE_DELETE_COINS_PCT) {
            int iPCT = 100 - Utils.launchDice(qi.getValue());
            if (iPCT < 0) {
                iPCT = 0;
            }
            int iAmmount = (Game.getWorld().getCoins() * iPCT) / 100;
            getCurrentCustomAction().getQueue().remove(0);
            getCurrentCustomAction().getQueueData().setLastQueueItem(null);
            Game.getWorld().setCoins(iAmmount);
            if (Game.getWorld().getCoins() < 0) { // No debería pasar
                Game.getWorld().setCoins(0);
            }
        } else if (qi.getType() == QueueItem.TYPE_ADD_GOD_STATUS) {
//			String sGodID = qi.getValue ();
//			if (sGodID != null) {
//				for (int g = 0; g < Game.getWorld ().getGods ().size (); g++) {
//					if (Game.getWorld ().getGods ().get (g).getGodID ().equals (sGodID)) {
//						// BINGO
//						Game.getWorld ().getGods ().get (g).setStatus (Game.getWorld ().getGods ().get (g).getStatus () + qi.getGodStatus ());
//						break;
//					}
//				}
//			}
            getCurrentCustomAction().getQueue().remove(0);
            getCurrentCustomAction().getQueueData().setLastQueueItem(null);
        }
    }

    /**
     * Hace las cosas de soldado. Al llamarse no tiene nada en carrying ni
     * carrying living
     */
    private void doSoldierTasks() {
        int iSoldierState = getSoldierData().getState();
        if (iSoldierState == SoldierData.STATE_IN_A_GROUP) {
            // Miramos el grupo en el que está
            int iGroupState = Game.getWorld().getSoldierGroups().getGroup(getSoldierData().getGroup()).getState();

            if (iGroupState == SoldierGroupData.STATE_GUARD) {
                iSoldierState = SoldierData.STATE_GUARD;
            } else if (iGroupState == SoldierGroupData.STATE_PATROL) {
                iSoldierState = SoldierData.STATE_PATROL;
            } else if (iGroupState == SoldierGroupData.STATE_BOSS) {
                iSoldierState = SoldierData.STATE_BOSS_AROUND;
            }
        }

        if (iSoldierState == SoldierData.STATE_PATROL) {
            ArrayList<Point3DShort> alPatrolPoints = getSoldierData().getPatrolPoints();
            if (alPatrolPoints.size() == 0) {
                // Miramos si tiene grupo
                if (getSoldierData().getState() == SoldierData.STATE_IN_A_GROUP) {
                    alPatrolPoints = Game.getWorld().getSoldierGroups().getGroup(getSoldierData().getGroup()).getPatrolPoints();
                }
            }
            if (alPatrolPoints.size() == 0) {
                moveAtRandom(getLivingEntityData().getMovePCTCurrent(), TYPE_CITIZEN);
            } else {
                int iIndexPatrol = getSoldierData().getPatrolPointTarget();
                if (iIndexPatrol >= alPatrolPoints.size()) {
                    iIndexPatrol = 0;
                }

                // Tenemos puntos de patrol, los seguimos
                Point3DShort p3dPatrol = alPatrolPoints.get(iIndexPatrol);
                if (p3dPatrol.equals(getCoordinates())) {
                    // Decrementamos el patrol wait time
                    getSoldierData().setPatrolWaitTime(getSoldierData().getPatrolWaitTime() - 1);
                    if (getSoldierData().getPatrolWaitTime() < 0) {
                        getSoldierData().setPatrolWaitTime(0);
                    }

                    // Ya estamos, esperamos un ratito (media hora) y después vamos al siguiente patrol
                    if (getSoldierData().getPatrolWaitTime() == 0) {
                        // Patrol wait time a 0, vamos al siguiente
                        getSoldierData().setPatrolPointTarget(iIndexPatrol + 1);
                        getSoldierData().setPatrolWaitTime(SoldierData.WAIT_TURNS_BETWEEN_PATROLS);
                    }
                } else {
                    // No estamos, vamos a ver si podemos llegar
                    if (World.getCell(p3dPatrol).getAstarZoneID() == World.getCell(getCoordinates()).getAstarZoneID()) {
                        setDestination(p3dPatrol);
                    } else {
                        // No se puede llegar a ese punto, lo saltamos
                        getSoldierData().setPatrolPointTarget(iIndexPatrol + 1);
                    }
                }
            }
        } else if (iSoldierState == SoldierData.STATE_GUARD) {
            moveAtRandom(getLivingEntityData().getMovePCTCurrent(), TYPE_CITIZEN);

            // De vez en cuando se va para las barracks
            if (Utils.getRandomBetween(1, World.TIME_MODIFIER_HOUR) == 1) {
                if (getFocusData().getEntityID() == -1 && getSoldierData().getState() == SoldierData.STATE_IN_A_GROUP) {
                    SoldierGroupData sgd = Game.getWorld().getSoldierGroups().getGroup(getSoldierData().getGroup());
                    if (sgd.hasZone()) {
                        Zone zone = Zone.getZone(sgd.getZoneID());
                        if (zone != null) {
                            Point3DShort p3d = Zone.getFreeCellAtRandom(zone, World.getCell(getCoordinates()).getAstarZoneID());
                            if (p3d != null) {
                                setDestination(p3d);
                            }
                        }
                    }
                }
            }
        } else if (iSoldierState == SoldierData.STATE_BOSS_AROUND) {
            if (getSoldierData().getTargetID() != -1) {
                Citizen citTarget = (Citizen) World.getLivingEntityByID(getSoldierData().getTargetID());
                if (citTarget != null) {
					// Tenemos target, a por él
                    // Miramos si lo tenemos al lado
                    boolean bTargetClose = Math.abs(citTarget.getZ() - getZ()) <= 1 && Math.abs(citTarget.getY() - getY()) <= 1 && Math.abs(citTarget.getX() - getX()) <= 1;

                    if (bTargetClose) {
                        // Lo tenemos
                        getSoldierData().setTargetID(-1);
                        setShowExclamationTurns(SoldierData.BOOST_TURNS_BOSS_AROUND / 4);

                        if (!citTarget.getSoldierData().isSoldier()) {
                            citTarget.getCitizenData().setHappiness(citTarget.getCitizenData().getHappiness() - 1);
                            citTarget.getCitizenData().setBoostCounter(SoldierData.BOOST_TURNS_BOSS_AROUND);
                        }
                    } else {
                        // No lo tenemos
                        if (World.getCell(citTarget.getCoordinates()).getAstarZoneID() != World.getCell(getCoordinates()).getAstarZoneID()) {
                            // No está en la zona, pacasa
                            getSoldierData().setTargetID(-1);
                        } else {
                            // A por el
                            setDestination(citTarget.getCoordinates(), 1);
                        }
                    }
                } else {
                    getSoldierData().setTargetID(-1);
                }
            } else {
                // No tenemos target
                if (getSoldierData().getCounter() < SoldierData.COUNTER_MAX_BOSS_AROUND) {
                    // Aún no toca buscar target
                    getSoldierData().setCounter(getSoldierData().getCounter() + 1);
                    getSoldierData().setTargetID(-1);
                    moveAtRandom(getLivingEntityData().getMovePCTCurrent(), TYPE_CITIZEN);
                } else {
                    // Toca buscar target a random
                    getSoldierData().setCounter(0);

                    int iNumAldeanos = World.getNumCitizens();
                    if (iNumAldeanos > 0) {
                        int iCurrentZoneID = World.getCell(getCoordinates()).getAstarZoneID();
                        int iID = getID();
                        int iTryes = 64;
                        Citizen cit = null;
                        while (iTryes > 0 && iID == getID()) {
                            iID = World.getCitizenIDs().get(Utils.getRandomBetween(0, iNumAldeanos - 1));
                            iTryes--;
                            cit = (Citizen) World.getLivingEntityByID(iID);
                            if (World.getCell(cit.getCoordinates()).getAstarZoneID() != iCurrentZoneID) {
                                // Aldeano en distinta zona
                                cit = null;
                            }
                        }

                        if (cit != null) {
                            // Lo tenemos
                            getSoldierData().setTargetID(cit.getID());
                        }
                    }
                }
            }
        } else if (getSoldierData().getState() == SoldierData.STATE_IN_A_GROUP) {
        } else {
            // Soldado sin tareas de soldado??? Weird!!
            Log.log(Log.LEVEL_ERROR, "Ding! Level up!", getClass().toString()); //$NON-NLS-1$
        }
    }

    /**
     * Hace las cosas necesarias para ejecutar una tarea genérica (chop/cut/dig)
     *
     * @param task
     */
    private void doGenericTask() {
        if (getCarrying() != null) {
            dropCarryingItem();
            return;
        }

        int x = getX();
        int y = getY();
        int z = getZ();
        Task task = getCurrentTask();

        // Obtenemos su hotpoint
        HotPoint hp = task.getHotPoint(getHotPointIndex());
        if (hp.isFinished()) {
            Game.getWorld().getTaskManager().removeCitizen(this);
            return;
        }

        // Miramos si ha llegado a destino
        boolean bFirstTry = getPlacesIndex() == -1; // Significa que aún no ha intentado ir a ningún sitio del hotpoint
        if (bFirstTry) {
            // Pondremos la place más cercana en el primer punto (esto evitará movimientos raros como ir a minar un sitio dando toda la vuelta por un pasillo)
            ArrayList<Point3DShort> alPlaces = hp.getPlaces();
            if (alPlaces.size() > 1) {
                int iIndexMenor = 0;
                Point3DShort p3d = alPlaces.get(0);
                int iDistanciaMenor = Utils.getDistance(x, y, z, p3d);
                int iDistanciaAux;
                for (int i = 1; i < alPlaces.size(); i++) {
                    p3d = alPlaces.get(i);
                    iDistanciaAux = Utils.getDistance(x, y, z, p3d);
                    if (iDistanciaAux < iDistanciaMenor) {
                        iIndexMenor = i;
                        iDistanciaMenor = iDistanciaAux;
                    }
                }

                if (iIndexMenor != 0) {
                    p3d = alPlaces.remove(iIndexMenor);
                    alPlaces.add(0, p3d);
                }
            }

            // Como es el primer intento empezamos con la 0 (el de menor recorrido)
            setPlacesIndex(0);
        }

        if (getPlacesIndex() < hp.getPlaces().size()) {
            Point3DShort p3d = hp.getPlaces().get(getPlacesIndex());

            if (x != p3d.x || y != p3d.y || z != p3d.z) {
                // No está en destino, lo movemos

                if (!bFirstTry) { // Ya ha buscado camino y NO lo ha encontrado
                    setPlacesIndex(getPlacesIndex() + 1);

                    // Si ya no hay más places es que este aldeano no puede llegar a destino, lo desligamos de la tarea y terminamos el turno
                    if (getPlacesIndex() >= hp.getPlaces().size()) {
                        Game.getWorld().getTaskManager().removeCitizen(this);
                        return;
                    }

                    p3d = hp.getPlaces().get(getPlacesIndex());
                }

                setDestination(p3d, false, 0);
            } else {
                // Está en destino, actuamos
                if (executeTask()) {
                    // Tarea terminada. Indicamos al manager que ya estamos y sacamos al aldeano de la lista
                    Game.getWorld().getTaskManager().setHotPointFinished(task, getHotPointIndex());
                    Game.getWorld().getTaskManager().removeCitizen(this);
                }
            }
        } else {
            // Algo ha pasado con los places, sacamos al aldeano de la tarea y ya veremos
            Game.getWorld().getTaskManager().removeCitizen(this);
        }
    }

    public Item getCarrying() {
        return getCitizenData().getCarryingData().getCarrying();
    }

    public LivingEntity getCarryingLiving() {
        return getCitizenData().getCarryingData().getCarryingLiving();
    }

    public void setCarrying(Item carrying) {
        getCitizenData().getCarryingData().setCarrying(carrying);

        getCitizenData().setBlinkAnimationTurns(0);
    }

    public void setCarryingLiving(LivingEntity carryingLiving) {
        getCitizenData().getCarryingData().setCarryingLiving(carryingLiving);
        getCitizenData().setBlinkAnimationTurns(0);
    }

    public boolean equals(Object p) {
        return ((Citizen) p).getID() == getID();
    }

    public void delete() {
        delete(true);
    }

    public boolean isIdle() {
        return getCurrentTask() == null && !getSoldierData().isSoldier();
    }

    public boolean isSleeping() {
        return getCitizenData().getSleep() == 0;
    }

    public void setShowExclamationTurns(int iTurns) {
        this.showExclamationTurns = iTurns;
    }

    public int getShowExclamationTurns() {
        return showExclamationTurns;
    }

    public CitizenData getCitizenData() {
        return citizenData;
    }

    public SoldierData getSoldierData() {
        return soldierData;
    }

    /**
     * Resta los contadores de comida y sueño, si muere de hambre retorna true,
     * también resta happiness (cada hora) en caso de hambre
     */
    protected boolean updateEatSleep() {
        getCitizenData().setSleep(getCitizenData().getSleep() - 1);
        if (getCitizenData().getSleep() < 0) {
            getCitizenData().setSleep(0);
        }

        getCitizenData().setHungry(getCitizenData().getHungry() - 1);
        if (getCitizenData().getHungry() < 0) {
            getCitizenData().setHungry(0);

            if (!getSoldierData().isSoldier()) {
                if (getCitizenData().getHungryEating() < -World.TIME_MODIFIER_HOUR && getCitizenData().getHungryEating() % World.TIME_MODIFIER_HOUR == 0) {
                    // Cada hora que pasa hambre le bajamos la felicidad
                    getCitizenData().setHappiness(getCitizenData().getHappiness() - 5);
                }
            }

            if (getCurrentTask() == null || (getCurrentTask().getTask() != Task.TASK_EAT)) {
                getCitizenData().setHungryEating(getCitizenData().getHungryEating() - 1);

                if (getCitizenData().getHungryEating() < -(2 * World.TIME_MODIFIER_DAY)) { // 2 dias sin comer, muere
                    MessagesPanel.addMessage(MessagesPanel.TYPE_ANNOUNCEMENT, getCitizenData().getFullName() + Messages.getString("Citizen.8"), ColorGL.RED, getCoordinates(), getID()); //$NON-NLS-1$
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Pone la tarea de eat o de sleep si hace falta
     */
    protected void checkEatSleep() {
        // Si no tiene tarea o tiene una tarea distinta a comer/dormir
        if (getCurrentTask() == null || getCurrentTask().getTask() == Task.TASK_BUILD || getCurrentTask().getTask() == Task.TASK_HEAL || getCurrentTask().getTask() == Task.TASK_HAUL || getCurrentTask().getTask() == Task.TASK_MOVE_TO_CARAVAN || getCurrentTask().getTask() == Task.TASK_AUTOEQUIP) {
            if (getCitizenData().getHungry() == 0) {
                // Hambriento, si hay comida en el mundo (y no usada por nadie) le metemos la tarea de comer
                if (Item.searchFood(getCoordinates(), getID()) != null) {
                    // Lo sacamos de la tarea actual (si tiene)
                    if (getCurrentTask() != null) {
                        Game.getWorld().getTaskManager().removeCitizen(this);
                    }

                    // Le asignamos la nueva tarea (fantasma)
                    setPath(null);
                    if (getCarrying() != null) {
                        dropCarryingItem();
                    } else {
                        setCurrentTask(new Task(Task.TASK_EAT));
                        getCitizenData().setHungryEating(0);
                    }

                    return;
                }
            }

            if (getCitizenData().getSleep() == 0) {
                // Tiene sueño, que deje lo que tiene encima y lo sacamos de la tarea actual
                if (getCarrying() != null) {
                    dropCarryingItem();
                    return;
                }

                // Lo sacamos de la tarea actual (si tiene)
                if (getCurrentTask() != null) {
                    Game.getWorld().getTaskManager().removeCitizen(this);
                }

                // Le asignamos la nueva tarea
                setCurrentTask(new Task(Task.TASK_SLEEP));
                setPath(null);
                getCitizenData().setSleepSleeping(0);
                setShowExclamationTurns(0);
                return;
            }
        }
    }

    /**
     * Mira si el aldeano está bajo de vida para ir a un hospital
     */
    protected void checkHealthPoints() {
        if (getLivingEntityData().getHealthPoints() <= getLivingEntityData().getHealthPointsMAXCurrent() / 3) { // <= 33% HP, vamos al hospital
            // Sólo si no está comiendo / durmiendo, curándose o luchando
            // if (getCurrentTask () == null || (getCurrentTask ().getTask () != Task.TASK_EAT && getCurrentTask ().getTask () != Task.TASK_SLEEP && getCurrentTask ().getTask () != Task.TASK_HEAL && getCurrentTask ().getTask () != Task.TASK_FIGHT)) {
            if (getCurrentTask() == null) {
                // Antes de sacarlo de la tarea y meterla la nueva miramos si existe un hospital con casillas libres
                ArrayList<Zone> alZones = Game.getWorld().getZones();
                boolean bHayHospital = false;
                ZoneManagerItem zmi;
                for (int i = 0; i < alZones.size(); i++) {
                    zmi = ZoneManager.getItem(alZones.get(i).getIniHeader());
                    if (zmi != null && zmi.getType() == ZoneManagerItem.TYPE_HOSPITAL && alZones.get(i).isOperative()) {
                        // Hospital encontrado, miramos si hay celda posible
                        if (Zone.getFreeCellAtRandom(alZones.get(i), World.getCell(getCoordinates()).getAstarZoneID()) != null) {
                            bHayHospital = true;
                            break;
                        }
                    }
                }

                if (bHayHospital) {
					// Lo sacamos de la tarea actual
                    // if (getCurrentTask () != null) {
                    // Game.getWorld ().getTaskManager ().removeCitizen (this);
                    // }

                    // Le metemos la tarea de heal
                    setCurrentTask(new Task(Task.TASK_HEAL));
                    setPath(null);
                    getFocusData().setEntityID(-1);
                    getFocusData().setEntityType(TYPE_UNKNOWN);
                    setFighting (false);
                }
            }
        }
    }

    public void refreshTransients() {
        super.refreshTransients();

        if (getLivingEntityData() != null && getCitizenData() != null) {
            getLivingEntityData().setName(getCitizenData().getFullName());
        }

        if (getCitizenData() != null) {
            if (getCarrying() != null) {
                getCarrying().refreshTransients();
            }

            if (getCarryingLiving() != null) {
                getCarryingLiving().refreshTransients();
            }
        }

        if (getEquippedData() != null) {
            getEquippedData().refreshTransients();
        }
    }

    /**
     * Comprueba que una comida no esté "pillada" por algún aldeano (pillada
     * quiero decir que no haya un aldeano con tarea de EAT caminando hacia
     * ella)
     *
     * @param p3dItem Coordenada de la comida
     * @param citID Aldeano que hace la comprobación
     * @return
     */
    public static boolean isCitizenWalkingToFood(Point3DShort p3dItem, int citID) {
        Citizen citizen;
        for (int i = 0; i < World.getCitizenIDs().size(); i++) {
            citizen = (Citizen) World.getLivingEntityByID(World.getCitizenIDs().get(i));
            if (citizen.getID() != citID) {
                if (isCitizenWalkingToFood(p3dItem, citID, citizen)) {
                    return true;
                }
            }
        }
        for (int i = 0; i < World.getSoldierIDs().size(); i++) {
            citizen = (Citizen) World.getLivingEntityByID(World.getSoldierIDs().get(i));
            if (citizen.getID() != citID) {
                if (isCitizenWalkingToFood(p3dItem, citID, citizen)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean isCitizenWalkingToFood(Point3DShort p3dItem, int citID, Citizen citizen) {
        Point3DShort p3d;
        if (citizen.getCurrentTask() != null && citizen.getCurrentTask().getTask() == Task.TASK_EAT) {
            // Aldeano con tarea de comer
            Point3D p3dFull = citizen.getCurrentTask().getPointIni();
            if (p3dFull != null) {
                p3d = p3dFull.toPoint3DShort();
                if (p3d.equals(p3dItem)) {
                    // Misma coordenada, quizá es un container y tiene más comida, así que miraremos que este aldeano no tenga ya comida en el carrying
                    Item itemCarrying = citizen.getCarrying();
                    if (itemCarrying != null) {
                        // Tiene item, miramos si es "comible"
                        if (!ItemManager.getItem(itemCarrying.getIniHeader()).canBeEaten()) {
                            // El aldeano NO tiene comida, esperamos
                            return true;
                        }
                    } else {
                        // Vaya, aldeano que va a por nuestra comida, esperamos
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Fills a contextual menú refering citizens of a cell
     *
     * @param cell
     * @param sm
     */
    public static void fillMenu(Cell cell, SmartMenu sm) {
        Point3DShort p3d = cell.getCoordinates();
        if (cell.containsSpecificLiving(TYPE_CITIZEN) != null) {
            // Equipar, miramos si hay objetos militares en el mundo, de paso ya hacemos una lista para poner en el menú
            Integer[] aItems = World.getItems().keySet().toArray(new Integer[0]);
            ArrayList<MilitaryItem> alHead = new ArrayList<MilitaryItem>(); // Head
            ArrayList<MilitaryItem> alBody = new ArrayList<MilitaryItem>(); // Body
            ArrayList<MilitaryItem> alLegs = new ArrayList<MilitaryItem>(); // Legs
            ArrayList<MilitaryItem> alFeet = new ArrayList<MilitaryItem>(); // Feet
            ArrayList<MilitaryItem> alWeapon = new ArrayList<MilitaryItem>(); // Weapon

            int iASZID = cell.getAstarZoneID();

            Item mi;
            boolean bMilitaryItems = false;
            for (int i = 0; i < aItems.length; i++) {
                mi = World.getItems().get(aItems[i]);
                if (mi != null && mi instanceof MilitaryItem) {
                    if (World.getCell(mi.getCoordinates()).getAstarZoneID() == iASZID) {
                        int location = ItemManager.getItem(mi.getIniHeader()).getLocation();
                        if (location == MilitaryItem.LOCATION_HEAD) {
                            alHead.add((MilitaryItem) mi);
                        } else if (location == MilitaryItem.LOCATION_BODY) {
                            alBody.add((MilitaryItem) mi);
                        } else if (location == MilitaryItem.LOCATION_LEGS) {
                            alLegs.add((MilitaryItem) mi);
                        } else if (location == MilitaryItem.LOCATION_FEET) {
                            alFeet.add((MilitaryItem) mi);
                        } else if (location == MilitaryItem.LOCATION_WEAPON) {
                            alWeapon.add((MilitaryItem) mi);
                        }
                        bMilitaryItems = true;
                    }
                }
            }

            // Containers
            ArrayList<Container> alContainers = Game.getWorld().getContainers();
            ArrayList<Item> alContainerItems;
            nextContainer:
            for (int i = 0; i < alContainers.size(); i++) {
                alContainerItems = alContainers.get(i).getItemsInside();
                for (int j = 0; j < alContainerItems.size(); j++) {
                    mi = alContainerItems.get(j);
                    if (World.getCell(mi.getCoordinates()).getAstarZoneID() != iASZID) {
                        continue nextContainer;
                    }

                    if (mi != null && mi instanceof MilitaryItem) {
                        int location = ItemManager.getItem(mi.getIniHeader()).getLocation();
                        if (location == MilitaryItem.LOCATION_HEAD) {
                            alHead.add((MilitaryItem) mi);
                        } else if (location == MilitaryItem.LOCATION_BODY) {
                            alBody.add((MilitaryItem) mi);
                        } else if (location == MilitaryItem.LOCATION_LEGS) {
                            alLegs.add((MilitaryItem) mi);
                        } else if (location == MilitaryItem.LOCATION_FEET) {
                            alFeet.add((MilitaryItem) mi);
                        } else if (location == MilitaryItem.LOCATION_WEAPON) {
                            alWeapon.add((MilitaryItem) mi);
                        }
                        bMilitaryItems = true;
                    }
                }
            }

            Citizen citizen;
            LivingEntity le;
            LivingEntityManagerItem lemi;
            ArrayList<LivingEntity> alLivings = cell.getLivings();
            if (alLivings != null) {
                for (int i = 0; i < alLivings.size(); i++) {
                    le = alLivings.get(i);
                    lemi = LivingEntityManager.getItem(le.getIniHeader());
                    if (lemi.getType() == TYPE_CITIZEN) {
                        citizen = (Citizen) le;
                        // Debug
                        if (TownsProperties.DEBUG_MODE) {
                            sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, "Hungry " + citizen.getCitizenData().getHungry() + ", HE " + citizen.getCitizenData().getHungryEating(), null, null, null)); //$NON-NLS-1$ //$NON-NLS-2$
                            sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, "Sleep " + citizen.getCitizenData().getSleep(), null, null, null)); //$NON-NLS-1$
                            sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, "Happy " + citizen.getCitizenData().getHappiness(), null, null, null)); //$NON-NLS-1$
                            sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, "Happy work counter " + citizen.getCitizenData().getHappinessWorkCounter(), null, null, null)); //$NON-NLS-1$
                            sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, "Happy idle counter " + citizen.getCitizenData().getHappinessIdleCounter(), null, null, null)); //$NON-NLS-1$
                            sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, "LOS " + citizen.getLivingEntityData().getLOSCurrent() + " / " + citizen.getLivingEntityData().getLOSBase(), null, null, null)); //$NON-NLS-1$ //$NON-NLS-2$
                            if (citizen.getCurrentTask() != null) {
                                sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, "Task ID " + citizen.getCurrentTask().getTask(), null, null, null)); //$NON-NLS-1$
//								if (citizen.getCurrentTask ().getTask () == Task.TASK_MOVE_AND_LOCK) {
//									System.out.println (citizen.getCurrentTask ().getPointIni ());
//									System.out.println (citizen.getCurrentTask ().getPointEnd ());
//								}
                            }
                            sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, "Waiting " + citizen.isWaitingForPath(), null, null, null)); //$NON-NLS-1$
                        }

                        // Información del aldeano
                        if (citizen.getSoldierData().isSoldier()) {
                            if (citizen.getSoldierData().isSoldier() && citizen.getSoldierData().getState() == SoldierData.STATE_IN_A_GROUP && citizen.getSoldierData().getGroup() >= 0 && citizen.getSoldierData().getGroup() < SoldierGroups.MAX_GROUPS) {
                                sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, citizen.getCitizenData().getFullName() + Messages.getString("Citizen.6") + " (" + Game.getWorld().getSoldierGroups().getGroup(citizen.getSoldierData().getGroup()).getName() + ")", null, null, null, null, p3d.toPoint3D(), Color.YELLOW)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            } else {
                                sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, citizen.getCitizenData().getFullName() + Messages.getString("Citizen.6"), null, null, null, null, p3d.toPoint3D(), Color.YELLOW)); //$NON-NLS-1$
                            }
                        } else {
                            if (citizen.getCitizenData().getGroupID() != -1 && Game.getWorld().getCitizenGroups().getGroup(citizen.getCitizenData().getGroupID()) != null) {
                                sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, citizen.getCitizenData().getFullName() + " (" + Game.getWorld().getCitizenGroups().getGroup(citizen.getCitizenData().getGroupID()).getName() + ")", null, null, null, null, p3d.toPoint3D(), Color.YELLOW)); //$NON-NLS-1$ //$NON-NLS-2$
                            } else {
                                sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, citizen.getCitizenData().getFullName(), null, null, null, null, p3d.toPoint3D(), Color.YELLOW));
                            }
                        }
                        if (citizen.getCurrentTask() != null) {
                            sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("Citizen.7") + citizen.getCurrentTask(), null, null, null)); //$NON-NLS-1$
                        }
                        // Level / Xp
                        if (citizen.getSoldierData().isSoldier()) {
                            sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("Hero.4") + citizen.getSoldierData().getLevel() + " (" + citizen.getSoldierData().getXp() + Messages.getString("Hero.5") + citizen.getSoldierData().getXpPCT() + "%)", null, null, null)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                        }
                        sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("UIPanel.40") + citizen.getCitizenData().getHappiness() + " / 100", null, null, null)); //$NON-NLS-1$ //$NON-NLS-2$
                        sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, citizen.getLivingEntityData().toString(), null, null, null));

                        // Equipar
                        boolean itemMetido = false;
                        if (bMilitaryItems) {
                            if (sm.getItems().size() > 0) {
                                sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
                            }
                            // Autoequip
                            sm.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Citizen.31") + citizen.getCitizenData().getFullName(), sm, CommandPanel.COMMAND_AUTOEQUIP, Integer.toString(citizen.getID()), null, null, Color.YELLOW)); //$NON-NLS-1$

                            // Equip
                            SmartMenu smEquip = new SmartMenu(SmartMenu.TYPE_MENU, Messages.getString("Citizen.13") + citizen.getCitizenData().getFullName(), sm, null, null); //$NON-NLS-1$
                            SmartMenu smAux = createEquipMenu(MilitaryItem.LOCATION_HEAD, Messages.getString("Citizen.14"), alHead, smEquip, citizen); //$NON-NLS-1$
                            if (smAux != null) {
                                smEquip.addItem(smAux);
                                itemMetido = true;
                            }
                            smAux = createEquipMenu(MilitaryItem.LOCATION_BODY, Messages.getString("Citizen.15"), alBody, smEquip, citizen); //$NON-NLS-1$
                            if (smAux != null) {
                                smEquip.addItem(smAux);
                                itemMetido = true;
                            }

                            smAux = createEquipMenu(MilitaryItem.LOCATION_LEGS, Messages.getString("Citizen.16"), alLegs, smEquip, citizen); //$NON-NLS-1$
                            if (smAux != null) {
                                smEquip.addItem(smAux);
                                itemMetido = true;
                            }
                            smAux = createEquipMenu(MilitaryItem.LOCATION_FEET, Messages.getString("Citizen.17"), alFeet, smEquip, citizen); //$NON-NLS-1$
                            if (smAux != null) {
                                smEquip.addItem(smAux);
                                itemMetido = true;
                            }
                            smAux = createEquipMenu(MilitaryItem.LOCATION_WEAPON, Messages.getString("Citizen.18"), alWeapon, smEquip, citizen); //$NON-NLS-1$
                            if (smAux != null) {
                                smEquip.addItem(smAux);
                                itemMetido = true;
                            }

                            if (itemMetido) {
                                smEquip.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
                                smEquip.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Citizen.19"), sm, CommandPanel.COMMAND_BACK, null)); //$NON-NLS-1$
                                sm.addItem(smEquip);
                            }
                        }
                        // Desequipar
                        itemMetido = false;
                        SmartMenu smUnEquip = new SmartMenu(SmartMenu.TYPE_MENU, Messages.getString("Citizen.20") + citizen.getCitizenData().getFullName(), sm, null, null); //$NON-NLS-1$
                        if (citizen.getEquippedData().isWearing(MilitaryItem.LOCATION_HEAD)) {
                            smUnEquip.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Citizen.21") + citizen.getEquippedData().getHead().getExtendedTilename(), smUnEquip, CommandPanel.COMMAND_WEAR_OFF, null, null, new Point3D(citizen.getID(), MilitaryItem.LOCATION_HEAD, -1), citizen.getEquippedData().getHead().getItemTextColor())); //$NON-NLS-1$
                            itemMetido = true;
                        }
                        if (citizen.getEquippedData().isWearing(MilitaryItem.LOCATION_BODY)) {
                            smUnEquip.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Citizen.22") + citizen.getEquippedData().getBody().getExtendedTilename(), smUnEquip, CommandPanel.COMMAND_WEAR_OFF, null, null, new Point3D(citizen.getID(), MilitaryItem.LOCATION_BODY, -1), citizen.getEquippedData().getBody().getItemTextColor())); //$NON-NLS-1$
                            itemMetido = true;
                        }
                        if (citizen.getEquippedData().isWearing(MilitaryItem.LOCATION_LEGS)) {
                            smUnEquip.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Citizen.23") + citizen.getEquippedData().getLegs().getExtendedTilename(), smUnEquip, CommandPanel.COMMAND_WEAR_OFF, null, null, new Point3D(citizen.getID(), MilitaryItem.LOCATION_LEGS, -1), citizen.getEquippedData().getLegs().getItemTextColor())); //$NON-NLS-1$
                            itemMetido = true;
                        }
                        if (citizen.getEquippedData().isWearing(MilitaryItem.LOCATION_FEET)) {
                            smUnEquip.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Citizen.24") + citizen.getEquippedData().getFeet().getExtendedTilename(), smUnEquip, CommandPanel.COMMAND_WEAR_OFF, null, null, new Point3D(citizen.getID(), MilitaryItem.LOCATION_FEET, -1), citizen.getEquippedData().getFeet().getItemTextColor())); //$NON-NLS-1$
                            itemMetido = true;
                        }
                        if (citizen.getEquippedData().isWearing(MilitaryItem.LOCATION_WEAPON)) {
                            smUnEquip.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Citizen.25") + citizen.getEquippedData().getWeapon().getExtendedTilename(), smUnEquip, CommandPanel.COMMAND_WEAR_OFF, null, null, new Point3D(citizen.getID(), MilitaryItem.LOCATION_WEAPON, -1), citizen.getEquippedData().getWeapon().getItemTextColor())); //$NON-NLS-1$
                            itemMetido = true;
                        }

                        if (itemMetido) {
                            smUnEquip.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
                            smUnEquip.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Citizen.19"), sm, CommandPanel.COMMAND_BACK, null)); //$NON-NLS-1$
                            sm.addItem(smUnEquip);
                        }

                        // Soldier / no soldier
                        if (citizen.getSoldierData().isSoldier()) {
                            sm.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Citizen.26"), null, CommandPanel.COMMAND_CONVERT_TO_CIVILIAN, Integer.toString(citizen.getID()), null, null, Color.GREEN)); //$NON-NLS-1$

                            // Change state
                            SmartMenu smChangeState = new SmartMenu(SmartMenu.TYPE_MENU, Messages.getString("Citizen.12"), sm, null, null); //$NON-NLS-1$

                            int iSoldierState = citizen.getSoldierData().getState();
                            if (iSoldierState != SoldierData.STATE_GUARD) {
                                smChangeState.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Citizen.32"), null, CommandPanel.COMMAND_SOLDIER_SET_STATE, Integer.toString(citizen.getID()), Integer.toString(SoldierData.STATE_GUARD), new Point3D(-1, -1, -1))); //$NON-NLS-1$
                            } else {
                                smChangeState.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("Citizen.32"), null, null, null, null, null, Color.GRAY)); //$NON-NLS-1$
                            }
                            if (iSoldierState != SoldierData.STATE_PATROL) {
                                smChangeState.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Citizen.34"), null, CommandPanel.COMMAND_SOLDIER_SET_STATE, Integer.toString(citizen.getID()), Integer.toString(SoldierData.STATE_PATROL), new Point3D(-1, -1, -1))); //$NON-NLS-1$
                            } else {
                                smChangeState.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("Citizen.34"), null, null, null, null, null, Color.GRAY)); //$NON-NLS-1$
                            }
                            if (iSoldierState != SoldierData.STATE_BOSS_AROUND) {
                                smChangeState.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Citizen.35"), null, CommandPanel.COMMAND_SOLDIER_SET_STATE, Integer.toString(citizen.getID()), Integer.toString(SoldierData.STATE_BOSS_AROUND), new Point3D(-1, -1, -1))); //$NON-NLS-1$
                            } else {
                                smChangeState.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("Citizen.35"), null, null, null, null, null, Color.GRAY)); //$NON-NLS-1$
                            }

                            smChangeState.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));

                            // Groups
                            SoldierGroupData sgd;
                            for (int g = 0; g < SoldierGroups.MAX_GROUPS; g++) {
                                // Añadir a grupos existentes
                                sgd = Game.getWorld().getSoldierGroups().getGroup(g);
                                if (iSoldierState != SoldierData.STATE_IN_A_GROUP || citizen.getSoldierData().getGroup() != sgd.getId()) {
                                    smChangeState.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, sgd.getName(), null, CommandPanel.COMMAND_SOLDIER_SET_STATE, Integer.toString(citizen.getID()), Integer.toString(SoldierData.STATE_IN_A_GROUP), new Point3D(sgd.getId(), -1, -1)));
                                } else {
                                    smChangeState.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, sgd.getName(), null, null, null, null, null, Color.GRAY));
                                }
                            }

                            smChangeState.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
                            smChangeState.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Citizen.19"), sm, CommandPanel.COMMAND_BACK, null)); //$NON-NLS-1$
                            sm.addItem(smChangeState);
                        } else {
                            sm.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Citizen.27"), null, CommandPanel.COMMAND_CONVERT_TO_SOLDIER, Integer.toString(citizen.getID()), null, null, Color.ORANGE)); //$NON-NLS-1$
                        }

                        sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));

                        // Actions
                        if (lemi.hasActions()) {
                            ActionManagerItem ami;
                            for (int j = 0; j < lemi.getActions().size(); j++) {
                                ami = ActionManager.getItem(lemi.getActions().get(j));
                                sm.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, ami.getName() + " " + lemi.getName().toLowerCase(), null, CommandPanel.COMMAND_CUSTOM_ACTION_DIRECT_LIVING, ami.getId(), Integer.toString(citizen.getID()))); //$NON-NLS-1$
                            }
                            sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
                        }

                    }
                }
            }
        }
    }

    private static SmartMenu createEquipMenu(int location, String sLocation, ArrayList<MilitaryItem> alMI, SmartMenu parent, Citizen citizen) {
        ItemManagerItem imi;
        ArrayList<MilitaryItem> alMISorted = new ArrayList<MilitaryItem>(alMI.size());

        // Primero creamos un array ordenado
        for (int it = 0; it < alMI.size(); it++) {
            if (World.getCell(alMI.get(it).getCoordinates()).getAstarZoneID() == World.getCell(citizen.getCoordinates()).getAstarZoneID()) {
                imi = ItemManager.getItem(alMI.get(it).getIniHeader());
                if (imi.getLocation() == location) {
                    // Toca meterlo
                    int iItemLevel = imi.getLevel();
                    int iItemIndex = -1;
                    for (int i = 0; i < alMISorted.size(); i++) {
                        imi = ItemManager.getItem(alMISorted.get(i).getIniHeader());
                        if (imi.getLevel() <= iItemLevel) {
                            iItemIndex = i;
                            break;
                        }
                    }

                    if (iItemIndex == -1) {
                        alMISorted.add(alMI.get(it));
                    } else {
                        alMISorted.add(iItemIndex, alMI.get(it));
                    }
                }
            }
        }

        if (alMISorted.size() > 0) {
            SmartMenu smEM = new SmartMenu(SmartMenu.TYPE_MENU, sLocation, parent, null, null);
            for (int it = 0; it < alMISorted.size(); it++) {
                smEM.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, alMISorted.get(it).getExtendedTilename(), parent, CommandPanel.COMMAND_WEAR, Integer.toString(citizen.getID()), Integer.toString(alMISorted.get(it).getID()), alMISorted.get(it).getCoordinates().toPoint3D(), alMISorted.get(it).getItemTextColor()));
            }

            smEM.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
            smEM.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Citizen.19"), parent, CommandPanel.COMMAND_BACK, null)); //$NON-NLS-1$
            return smEM;
        }

        return null;
    }

    /**
     * Retorna el % de velocidad a aplicar a la velocidad actual basado en el
     * hambre y la felicidad
     *
     * @return el % de velocidad a aplicar a la velocidad actual basado en el
     * hambre y la felicidad
     */
    public int getMalusSpeedPCT() {
        // De momento sólo miramos lo hambriento que esté
        if (getCitizenData().getHungry() == 0 && getCitizenData().getHungryEating() < 0) {
            if (getCitizenData().getHungryEating() < -(World.TIME_MODIFIER_DAY)) {
                // A las 24 horas ya camina el máximo de lento (20%)
                return 20;
            } else {
                int iHE = World.TIME_MODIFIER_DAY + getCitizenData().getHungryEating(); // Es una suma pero el hungryEating es negativo
                iHE = ((iHE * 100) / (World.TIME_MODIFIER_DAY));
                if (iHE < 20) {
                    return 20;
                } else if (iHE > 100) {
                    return 100;
                } else {
                    return iHE;
                }
            }
        }

        return 100;
    }

    /**
     * Este método es para notificar a una living entity que ha sido golpeado,
     * se le pasa el atacante
     *
     * @param le El atacante
     * @param bHitted Indica si le ha pegado o sólo lo ha intentado
     */
    public void hitted(LivingEntity le, boolean bHitted, int iDamage) {
        super.hitted(le, bHitted, iDamage);

        // Buscaremos soldados con estado RESPONSE_TEAM sin focus para que vayan a ayudarlo
        Citizen cit;
        for (int i = 0; i < World.getSoldierIDs().size(); i++) {
            cit = (Citizen) World.getLivingEntityByID(World.getSoldierIDs().get(i));

            if (cit.getSoldierData().getState() == SoldierData.STATE_GUARD) {
                if (cit.getFocusData().getEntityID() == -1) {
                    // Cambiamos el focus
                    cit.getFocusData().setEntityID(le.getID());
                    cit.getFocusData().setEntityType(LivingEntityManager.getItem(le.getIniHeader()).getType());
                    setFighting (true);
                }
            } else if (cit.getSoldierData().getState() == SoldierData.STATE_IN_A_GROUP) {
                if (Game.getWorld().getSoldierGroups().getGroup(cit.getSoldierData().getGroup()).getState() == SoldierGroupData.STATE_GUARD) {
                    // Cambiamos el focus
                    cit.getFocusData().setEntityID(le.getID());
                    cit.getFocusData().setEntityType(LivingEntityManager.getItem(le.getIniHeader()).getType());
                    setFighting (true);
                }
            }
        }

        // También buscamos héroes con moral alta
        Hero hero;
        for (int i = 0; i < World.getHeroIDs().size(); i++) {
            hero = (Hero) World.getLivingEntityByID(World.getHeroIDs().get(i));

            if (hero != null && hero.getFocusData().getEntityID() == -1) {
                if (hero.getLivingEntityData().getMoral() >= 80) {
                    hero.getFocusData().setEntityID(le.getID());
                    hero.getFocusData().setEntityType(LivingEntityManager.getItem(le.getIniHeader()).getType());
                    setFighting (true);
                }
            }
        }

    }

    // Eliminamos cosas de soldados (como los patrol points por ejemplo, soldier groups, ...)
    public void deleteSoldierStuff() {
        int iMaxRemove = getSoldierData().getPatrolPoints().size();
        while (iMaxRemove > 0) {
            getSoldierData().removePatrolPoint(getSoldierData().getPatrolPoints().get(0));
            iMaxRemove--;
        }

        // Lo borramos de los soldiergroups
        Game.getWorld().getSoldierGroups().removeSoldierFromGroup(getID(), getSoldierData().getGroup());
    }

    // Eliminamos cosas de job groups
    public void deleteJobGroupsStuff() {
        // Lo borramos de los job groups
        Game.getWorld().getCitizenGroups().removeCitizenFromGroup(getID(), getCitizenData().getGroupID());
    }

    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        citizenData = (CitizenData) in.readObject();
        soldierData = (SoldierData) in.readObject();
        currentTask = (Task) in.readObject();
        hotPointIndex = in.readInt();
        placesIndex = in.readInt();
        currentCustomAction = (Action) in.readObject();
        showExclamationTurns = in.readInt();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(citizenData);
        out.writeObject(soldierData);
        out.writeObject(currentTask);
        out.writeInt(hotPointIndex);
        out.writeInt(placesIndex);
        out.writeObject(currentCustomAction);
        out.writeInt(showExclamationTurns);
    }
}
