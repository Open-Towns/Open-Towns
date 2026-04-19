package xaos.campaign;

import java.util.ArrayList;

import xaos.main.Game;
import xaos.main.World;
import xaos.tiles.entities.buildings.Building;
import xaos.tiles.entities.buildings.BuildingManager;
import xaos.tiles.entities.buildings.BuildingManagerItem;
import xaos.tiles.entities.items.Item;
import xaos.tiles.entities.items.ItemManager;
import xaos.tiles.entities.items.ItemManagerItem;
import xaos.tiles.entities.living.LivingEntityManager;
import xaos.tiles.entities.living.LivingEntityManagerItem;
import xaos.utils.Messages;
import xaos.utils.Utils;

public class ObjectiveData {

    public static final String TYPE_COLLECT = "COLLECT"; //$NON-NLS-1$
    public static final String TYPE_BUILD = "BUILD"; //$NON-NLS-1$
    public static final String TYPE_ZONE = "ZONE"; //$NON-NLS-1$
    public static final String TYPE_KILL = "KILL"; //$NON-NLS-1$
    public static final String TYPE_PILE = "PILE"; //$NON-NLS-1$
//	public static final String TYPE_ROOF = "ROOF"; //$NON-NLS-1$
    public static final String TYPE_SOLDIER = "SOLDIER"; //$NON-NLS-1$

    private String type;
    private String param1;
    private int param2;

    public String getType() {
        return type;
    }

    public void setType(String type) throws Exception {
        if (type == null || (!type.equalsIgnoreCase(TYPE_COLLECT) && !type.equalsIgnoreCase(TYPE_BUILD) && !type.equalsIgnoreCase(TYPE_ZONE) && !type.equalsIgnoreCase(TYPE_KILL) && !type.equalsIgnoreCase(TYPE_PILE) /*&& !type.equalsIgnoreCase (TYPE_ROOF)*/ && !type.equalsIgnoreCase(TYPE_SOLDIER))) {
            throw new Exception(Messages.getString("ObjectiveData.2") + " [" + type + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        this.type = type;
    }

    public String getParam1() {
        return param1;
    }

    public void setParam1(String param1) {
        this.param1 = param1;
    }

    public int getParam2() {
        return param2;
    }

    public void setParam2(int param2) {
        this.param2 = param2;
    }

    public void setParam2(String sParam2) {
        setParam2(Utils.launchDice(sParam2));
    }

    public String toString() {
        if (getType().equalsIgnoreCase(TYPE_COLLECT)) {

            ItemManagerItem imi = ItemManager.getItem(getParam1());
            int number = Item.getNumItemsTotal(getParam1(), World.MAP_DEPTH - 1);
            if (number >= getParam2()) {
                return Messages.getString("ObjectiveData.0") + getParam2() + " " + imi.getName() + Messages.getString("ObjectiveData.7"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            } else {
                return Messages.getString("ObjectiveData.0") + getParam2() + " " + imi.getName() + " (" + number + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            }
        } else if (getType().equalsIgnoreCase(TYPE_BUILD)) {
            BuildingManagerItem bmi = BuildingManager.getItem(getParam1());
            ArrayList<Building> alBuildings = World.getBuildings();
            Building building;
            int number = 0;
            for (int i = 0; i < alBuildings.size(); i++) {
                building = alBuildings.get(i);
                if (building.isOperative() && building.getIniHeader().equals(getParam1())) {
                    number++;
                }
            }
            if (number >= getParam2()) {
                return Messages.getString("ObjectiveData.3") + getParam2() + " " + bmi.getName() + Messages.getString("ObjectiveData.7"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            } else {
                return Messages.getString("ObjectiveData.3") + getParam2() + " " + bmi.getName() + " (" + number + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            }
        } else if (getType().equalsIgnoreCase(TYPE_ZONE)) {
            int number = Game.getWorld().getZones().size();
            if (number >= getParam2()) {
                return Messages.getString("ObjectiveData.1") + getParam2() + Messages.getString("ObjectiveData.4") + Messages.getString("ObjectiveData.7"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            } else {
                return Messages.getString("ObjectiveData.1") + getParam2() + Messages.getString("ObjectiveData.4") + " (" + number + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            }
        } else if (getType().equalsIgnoreCase(TYPE_KILL)) {
            LivingEntityManagerItem lemi = LivingEntityManager.getItem(getParam1());
            String sName;
            if (lemi == null) {
                sName = Messages.getString("ObjectiveData.5"); //$NON-NLS-1$
            } else {
                sName = lemi.getName();
            }
            int killed = Game.getWorld().getNumKilledEnemies(getParam1());
            if (killed >= getParam2()) {
                return Messages.getString("ObjectiveData.6") + getParam2() + " " + sName + Messages.getString("ObjectiveData.7"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            } else {
                return Messages.getString("ObjectiveData.6") + getParam2() + " " + sName + " (" + killed + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            }
        } else if (getType().equalsIgnoreCase(TYPE_PILE)) {
            int number = Game.getWorld().getStockpiles().size();
            if (number >= getParam2()) {
                return Messages.getString("ObjectiveData.1") + getParam2() + Messages.getString("ObjectiveData.8") + Messages.getString("ObjectiveData.7"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            } else {
                return Messages.getString("ObjectiveData.1") + getParam2() + Messages.getString("ObjectiveData.8") + " (" + number + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            }
//		} else if (getType ().equalsIgnoreCase (TYPE_ROOF)) {
//			int number = Roof.getNumRoofs ();
//			if (number >= getParam2 ()) {
//				return Messages.getString("ObjectiveData.1") + getParam2 () + Messages.getString("ObjectiveData.9") + Messages.getString("ObjectiveData.7"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//			} else {
//				return Messages.getString("ObjectiveData.1") + getParam2 () + Messages.getString("ObjectiveData.9") + " (" + number + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
//			}
        } else if (getType().equalsIgnoreCase(TYPE_SOLDIER)) {
            if (World.getNumSoldiers() >= getParam2()) {
                return Messages.getString("ObjectiveData.1") + getParam2() + Messages.getString("ObjectiveData.10") + Messages.getString("ObjectiveData.7"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            } else {
                return Messages.getString("ObjectiveData.1") + getParam2() + Messages.getString("ObjectiveData.10") + " (" + World.getNumSoldiers() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            }
        } else {
            return Messages.getString("ObjectiveData.2"); //$NON-NLS-1$
        }
    }

    /**
     * Indica si el objetivo pasado está conseguido
     *
     * @param alData
     * @return true si el objetivo pasado está conseguido
     */
    public static boolean checkCompleted(ObjectiveData data) {
        if (data == null) {
            return false;
        }

        if (data.getType().equals(TYPE_COLLECT)) {
            // Collect, contamos el número de items
            return Item.getNumItemsTotal(data.getParam1(), World.MAP_DEPTH - 1) >= data.getParam2();
        } else if (data.getType().equals(TYPE_BUILD)) {
			// Construir edificio
            // Hay que contarlos y tiene que estar operativo
            ArrayList<Building> alBuildings = World.getBuildings();
            if (alBuildings.size() == 0) {
                return false;
            }

            int contador = data.getParam2();
            for (int i = 0; i < alBuildings.size(); i++) {
                if (alBuildings.get(i).getIniHeader().equalsIgnoreCase(data.getParam1())) {
                    if (alBuildings.get(i).isOperative()) {
                        contador--;
                        if (contador == 0) {
                            return true;
                        }
                    }
                }
            }
        } else if (data.getType().equals(TYPE_ZONE)) {
            // Crear zonas
            return Game.getWorld().getZones() != null && Game.getWorld().getZones().size() >= data.getParam2();
        } else if (data.getType().equals(TYPE_KILL)) {
            // Matar
            return Game.getWorld().getNumKilledEnemies(data.getParam1()) >= data.getParam2();
        } else if (data.getType().equals(TYPE_PILE)) {
            // Crear pilas
            return Game.getWorld().getStockpiles() != null && Game.getWorld().getStockpiles().size() >= data.getParam2();
//		} else if (data.getType ().equals (TYPE_ROOF)) {
//			// Roofs
//			return Roof.getNumRoofs () >= data.getParam2 ();
        } else if (data.getType().equals(TYPE_SOLDIER)) {
            // Soldiers
            return World.getNumSoldiers() >= data.getParam2();
        }

        return false;
    }

    /**
     * Indica si la lista de objetivos pasada está conseguida
     *
     * @param alData
     * @return true si la lista de objetivos pasada está conseguida
     */
    public static boolean checkCompleted(ArrayList<ObjectiveData> alData) {
        if (alData == null || alData.size() == 0) {
            return false;
        }

        for (int i = 0; i < alData.size(); i++) {
            if (!checkCompleted(alData.get(i))) {
                return false;
            }
        }

        return true;
    }
}
