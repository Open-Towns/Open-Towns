package xaos.tiles.entities.living.enemies;

import java.awt.Color;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import xaos.TownsProperties;

import xaos.actions.ActionManager;
import xaos.actions.ActionManagerItem;
import xaos.data.CarryingData;
import xaos.data.SiegeData;
import xaos.main.Game;
import xaos.main.World;
import xaos.panels.CommandPanel;
import xaos.panels.menus.SmartMenu;
import xaos.tiles.Cell;
import xaos.tiles.entities.items.Container;
import xaos.tiles.entities.items.Item;
import xaos.tiles.entities.items.ItemManager;
import xaos.tiles.entities.items.ItemManagerItem;
import xaos.tiles.entities.living.Citizen;
import xaos.tiles.entities.living.LivingEntity;
import xaos.tiles.entities.living.LivingEntityManager;
import xaos.tiles.entities.living.LivingEntityManagerItem;
import xaos.utils.Point3D;
import xaos.utils.Point3DShort;
import xaos.utils.Utils;
import xaos.utils.UtilsIniHeaders;

public class Enemy extends LivingEntity implements Externalizable {

    private static final long serialVersionUID = -115858431325278204L;

    // Siege
    private SiegeData siegeData;

    // Carrying
    private CarryingData carryingData;

    public Enemy() {
        super();
    }

    // Constructor principal, actualiza el ID de enemigo
    public Enemy(String iniHeader) {
        super(iniHeader);
        setSiegeData(null);
    }

    public void refreshTransients() {
        super.refreshTransients();

        if (getCarryingData() != null) {
            if (getCarryingData().getCarrying() != null) {
                getCarryingData().getCarrying().refreshTransients();
            }

            if (getCarryingData().getCarryingLiving() != null) {
                getCarryingData().getCarryingLiving().refreshTransients();
            }
        }
    }

    public void setSiegeData(SiegeData siegeData) {
        this.siegeData = siegeData;
    }

    public SiegeData getSiegeData() {
        return siegeData;
    }

    public void setCarryingData(CarryingData carryingData) {
        this.carryingData = carryingData;
    }

    public CarryingData getCarryingData() {
        return carryingData;
    }

    public boolean doSiegeStuff(LivingEntityManagerItem lemi) {
        if (getSiegeData() == null) {
            moveAtRandom(getLivingEntityData().getMovePCTCurrent(), TYPE_ENEMY);
            return false;
        }

        // Primero de todo miramos si está pirando o si tiene que pirar
        if (getSiegeData().getStatus() == SiegeData.STATUS_LEAVING) {
            // Miramos que no esté en el starting point
            if (getCoordinates().equals(getSiegeData().getStartingPoint())) {
                // Bye bye
                return true;
            } else {
                // Miramos si podemos ir a ese punto para pirarnos
                int iASZID = World.getCell(getCoordinates()).getAstarZoneID();
                if (iASZID == World.getCell(getSiegeData().getStartingPoint()).getAstarZoneID()) {
                    // Puede ir
                    setDestination(getSiegeData().getStartingPoint());
                } else {
                    // No puede ir, buscamos un punto a random
                    Point3DShort p3d = World.getRandomBorderPoint(iASZID);
                    if (p3d != null) {
                        // Bingo, vamos para ahí
                        getSiegeData().setStartingPoint(p3d);
                        setDestination(p3d);
                    } else {
                        // No puede pirarse, quitamos el flag de siege
                        setSiegeData(null);
                    }
                }
                return false;
            }
        }

        // Si llega aquí es que no está pirando
        if (getSiegeData().getCount() > 0) {
            // Estamos en siege, decrementamos el siegecount, cuando valga 0 hará lo que toque
            getSiegeData().setCount(getSiegeData().getCount() - 1);
            moveAtRandom(getLivingEntityData().getMovePCTCurrent(), TYPE_ENEMY);
        } else {
            byte bType = getSiegeData().getType();
            if (bType == SiegeData.SIEGE_STANDARD) {
                doSiegeStandard();
            } else if (bType == SiegeData.SIEGE_ROBBERY) {
                doSiegeRobbery(lemi);
            } else {
                setSiegeData(null);
            }
        }

        return false;
    }

    private void doSiegeStandard() {
        if ((World.getNumCitizens() + World.getNumSoldiers()) > 0) {
            // En caso de siege y con aldeanos en el mundo le metemos un focus a alguien en el ASZI (el citizen más cercano)
            int iASZI = World.getCell(getCoordinates()).getAstarZoneID();
            Citizen citizen;
            ArrayList<Integer> alCitsInArea = new ArrayList<Integer>();
            for (int i = 0; i < World.getCitizenIDs().size(); i++) {
                citizen = (Citizen) World.getLivingEntityByID(World.getCitizenIDs().get(i));
                if (World.getCell(citizen.getCoordinates()).getAstarZoneID() == iASZI) {
                    alCitsInArea.add(citizen.getID());
                }
            }
            for (int i = 0; i < World.getSoldierIDs().size(); i++) {
                citizen = (Citizen) World.getLivingEntityByID(World.getSoldierIDs().get(i));
                if (World.getCell(citizen.getCoordinates()).getAstarZoneID() == iASZI) {
                    alCitsInArea.add(citizen.getID());
                }
            }

            if (alCitsInArea.size() > 0) {
                getFocusData().setEntityID(alCitsInArea.get(Utils.getRandomBetween(0, (alCitsInArea.size() - 1))));
                getFocusData().setEntityType(TYPE_CITIZEN);
                setFighting (true);
            } else {
                getSiegeData().setCount(World.TIME_MODIFIER_HOUR * LivingEntityManager.getItem(getIniHeader()).getLevel());
                if (getSiegeData().getCount() <= 0) {
                    getSiegeData().setCount(World.TIME_MODIFIER_HOUR);
                }
            }
        } else {
            // No hay aldeanos, quitamos el siege
            setSiegeData(null);
        }
    }

    private void doSiegeRobbery(LivingEntityManagerItem lemi) {
        if (getCarryingData() != null && (getCarryingData().getCarrying() != null || getCarryingData().getCarryingLiving() != null)) {
            // Ya lleva algo, pacasa
            getSiegeData().setStatus(SiegeData.STATUS_LEAVING);
            return;
        }

        // Primero de todo miramos que no haya una living, container o steal item en la celda actual
        Cell cell = World.getCell(getCoordinates());
        if (lemi.getStealLivings() != null) {
			// Livings
            // Miramos si alguna nos va bien
            if (cell.getLivings() != null) {
                for (int i = 0; i < cell.getLivings().size(); i++) {
                    if (UtilsIniHeaders.contains(lemi.getStealLivings(), cell.getLivings().get(i).getNumericIniHeader())) {
                        // Bingo, la pillamos y pacasa
                        if (getCarryingData() == null) {
                            setCarryingData(new CarryingData());
                        }
                        LivingEntity le = cell.getLivings().remove(i);
                        le.delete(false);
                        getCarryingData().setCarryingLiving(le); // Lo pillamos
                        getSiegeData().setStatus(SiegeData.STATUS_LEAVING);
                        return;
                    }
                }
            }
        }

        // Miramos si hay items
        if (lemi.getSteal() != null) {
            Item itemCell = cell.getItem();
            if (itemCell != null) {
                ItemManagerItem imi = ItemManager.getItem(itemCell.getIniHeader());
                if (imi.isContainer() || UtilsIniHeaders.contains(lemi.getSteal(), imi.getNumericalIniHeader())) {
                    // BINGO! Lo pillamos
                    if (getCarryingData() == null) {
                        setCarryingData(new CarryingData());
                    }
                    getCarryingData().setCarrying(cell.getItem()); // Lo pillamos
                    cell.getEntity().delete(); // Lo borramos
                    getSiegeData().setStatus(SiegeData.STATUS_LEAVING);
                    return;
                }
            }
        }

        // Buscamos livings
        if (lemi.getStealLivings() != null) {
            LivingEntity le = LivingEntity.searchLiving(getCoordinates(), lemi.getStealLivings(), true, null);
            if (le != null) {
                setDestination(le.getCoordinates());
                return;
            }
        }
        // Buscamos item to steal
        Point3DShort p3dItem = null;
        if (lemi.getSteal() != null) {
            p3dItem = Item.searchItem(false, getCoordinates(), lemi.getSteal(), true, Item.SEARCH_DOESNTMATTER, Item.SEARCH_DOESNTMATTER, null, World.MAP_DEPTH - 1);
            if (p3dItem != null) {
                setDestination(p3dItem);
                return;
            }
        }

        // Si llega aquí es que no hay items to steal, buscamos containers
        if (Game.getWorld().getContainers().size() > 0) {
            int iASZI = World.getCell(getCoordinates()).getAstarZoneID();
            ArrayList<Container> alContainers = Game.getWorld().getContainers();
            ArrayList<Integer> alContainersInArea = new ArrayList<Integer>();
            Container container;
            Item itemContainer;
            for (int i = 0; i < alContainers.size(); i++) {
                container = alContainers.get(i);
                if (container != null) {
                    itemContainer = Item.getItemByID(container.getItemID());
                    if (itemContainer != null && World.getCell(itemContainer.getCoordinates()).getAstarZoneID() == iASZI) {
                        alContainersInArea.add(container.getItemID());
                    }
                }
            }

            if (alContainersInArea.size() > 0) {
                // Hay containers en el área, pillamos uno a random y vamos a por él
                int iItemID = alContainersInArea.get(Utils.getRandomBetween(0, alContainersInArea.size() - 1));
                itemContainer = Item.getItemByID(iItemID); // No puede dar null, lo tenemos controlado arriba
                setDestination(itemContainer.getCoordinates());
            } else {
                // Hay containers pero no en el área, seteamos el contador
                getSiegeData().setCount(World.TIME_MODIFIER_HOUR * LivingEntityManager.getItem(getIniHeader()).getLevel());
                if (getSiegeData().getCount() <= 0) {
                    getSiegeData().setCount(World.TIME_MODIFIER_HOUR);
                }
            }
            return;
        }

        // No hay nada, nos piramos
        getSiegeData().setStatus(SiegeData.STATUS_LEAVING);
    }

    /**
     * Fills a contextual menú refering an enemy of a cell
     *
     * @param cell
     * @param sm
     */
    public static void fillMenu(Cell cell, SmartMenu sm) {
        Enemy enemy;
        LivingEntity le;
        LivingEntityManagerItem lemi;

        Point3D p3d = cell.getCoordinates().toPoint3D();
        ArrayList<LivingEntity> alLivings = cell.getLivings();

        if (alLivings != null) {
            for (int liv = 0; liv < alLivings.size(); liv++) {
                le = alLivings.get(liv);
                lemi = LivingEntityManager.getItem(le.getIniHeader());
                if (lemi.getType() == TYPE_ENEMY) {
                    enemy = (Enemy) le;

                    sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, le.getLivingEntityData().getName(), null, null, null, null, null, Color.YELLOW)); //$NON-NLS-1$

                    // Actions
                    if (lemi.hasActions()) {
                        ActionManagerItem ami;
                        for (int j = 0; j < lemi.getActions().size(); j++) {
                            ami = ActionManager.getItem(lemi.getActions().get(j));
                            sm.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, ami.getName() + " " + lemi.getName().toLowerCase(), null, CommandPanel.COMMAND_CUSTOM_ACTION, ami.getId(), Integer.toString(enemy.getID()), p3d)); //$NON-NLS-1$
                        }
                        sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, "", null, null, null)); //$NON-NLS-1$
                    }
                    if (TownsProperties.DEBUG_MODE && enemy.getSiegeData() != null) {
                        sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, "siegeType " + enemy.getSiegeData().getType(), null, null, null, null, p3d)); //$NON-NLS-1$
                        sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, "siegeCount " + enemy.getSiegeData().getCount(), null, null, null, null, p3d)); //$NON-NLS-1$
                    }

                    sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, le.getLivingEntityData().toString(), null, null, null));
                    sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        siegeData = (SiegeData) in.readObject();
        carryingData = (CarryingData) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(siegeData);
        out.writeObject(carryingData);
    }
}
