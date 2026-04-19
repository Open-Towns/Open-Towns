package xaos.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

import xaos.caravans.CaravanItemDataInstance;
import xaos.caravans.CaravanManager;
import xaos.caravans.CaravanManagerItem;
import xaos.caravans.PricesManager;
import xaos.main.Game;
import xaos.main.World;
import xaos.panels.MessagesPanel;
import xaos.panels.UIPanel;
import xaos.panels.menus.SmartMenu;
import xaos.tasks.HotPoint;
import xaos.tasks.Task;
import xaos.tiles.entities.items.Item;
import xaos.tiles.entities.items.ItemManager;
import xaos.tiles.entities.items.ItemManagerItem;
import xaos.tiles.entities.items.military.MilitaryItem;
import xaos.tiles.entities.living.LivingEntity;
import xaos.tiles.entities.living.LivingEntityManager;
import xaos.tiles.entities.living.LivingEntityManagerItem;
import xaos.utils.ColorGL;
import xaos.utils.Messages;
import xaos.utils.Point3D;
import xaos.utils.Point3DShort;
import xaos.utils.Utils;
import xaos.zones.Zone;

public class CaravanData implements Externalizable {

    private static final long serialVersionUID = 4026449570879006794L;

    public static final int STATUS_NONE = 0;
    public static final int STATUS_COMING = 1;
    public static final int STATUS_IN_PLACE = 2;
    public static final int STATUS_TRADING = 3;
    public static final int STATUS_LEAVING = 4;

    private int livingId;
    private ArrayList<CaravanItemDataInstance> alItems;
    private int status;
    private int pricePCT;
    private int coins;
    private Point3DShort startingPoint;
    private int turnsToLeave;
    private SmartMenu menuCaravanToBuy; // Lista de items que el usuario va marcando para comprar
    private SmartMenu menuTownToSell; // Lista de items que el usuario va marcando para vender

    public CaravanData() {
    }

    public void refreshTransients() {
        if (menuCaravanToBuy != null) {
            menuCaravanToBuy.refreshTransients();
        }
        if (menuTownToSell != null) {
            menuTownToSell.refreshTransients();
        }
        if (alItems != null) {
            for (int i = 0; i < alItems.size(); i++) {
                alItems.get(i).getItem().refreshTransients();
            }
        }
    }

    public int getLivingId() {
        return livingId;
    }

    public void setLivingId(int livingId) {
        this.livingId = livingId;
    }

    public void setAlItems(ArrayList<CaravanItemDataInstance> alItems) {
        this.alItems = alItems;
    }

    public ArrayList<CaravanItemDataInstance> getAlItems() {
        return alItems;
    }

    public void setStatus(int status) {
        this.status = status;

        if (status == STATUS_IN_PLACE) {
            LivingEntity le = World.getLivingEntityByID(getLivingId());
            if (le != null) {
                String sCaravanMessage = Messages.getString("CaravanData.0"); //$NON-NLS-1$
                if (!Game.isPaused() && Game.isCaravanPause()) {
                    sCaravanMessage = sCaravanMessage + " " + Messages.getString("World.22"); //$NON-NLS-1$ //$NON-NLS-2$
                }
                MessagesPanel.addMessage(MessagesPanel.TYPE_ANNOUNCEMENT, sCaravanMessage, ColorGL.ORANGE, le.getCoordinates(), le.getID());

                if (!Game.isPaused() && Game.isCaravanPause()) {
                    Game.pause(false);
                }
            }
        } else if (status == STATUS_LEAVING) {
            LivingEntity le = World.getLivingEntityByID(getLivingId());
            if (le != null) {
                MessagesPanel.addMessage(MessagesPanel.TYPE_ANNOUNCEMENT, Messages.getString("CaravanData.1"), ColorGL.ORANGE, le.getCoordinates(), le.getID()); //$NON-NLS-1$
            }
        }
    }

    public int getStatus() {
        return status;
    }

    /**
     * Chequea si la caravana está lista para trade Cambia el flag "ready" de la
     * misma (tanto true como false)
     *
     * @return true si hay que eliminar la caravana (pq se fué)
     */
    public boolean updateCaravanStatus() {
        if (getStatus() == STATUS_NONE) {
            setStatus(STATUS_COMING);
        }

        // Turns to leave
        if (getStatus() != STATUS_LEAVING) {
            // Miramos si tiene que pirarse
            if (getTurnsToLeave() > 0) {
                setTurnsToLeave(getTurnsToLeave() - 1);
                if (getTurnsToLeave() <= 0) {
                    // Se acabó la espera, nos piramos
                    setStatus(STATUS_LEAVING);
                }
            }
        }

        LivingEntity le;
        switch (getStatus()) {
            case STATUS_COMING:
                le = World.getLivingEntityByID(getLivingId());
                if (le != null) {
                    LivingEntityManagerItem lemi = LivingEntityManager.getItem(le.getIniHeader());
                    if (lemi != null && lemi.getCaravan() != null) {
                        if (le.getPath().size() == 0) {
                            int iZoneID = World.getCell(le.getCoordinates()).getZoneID();
                            if (iZoneID != 0) {
                                Zone zone = Zone.getZone(iZoneID);
                                CaravanManagerItem cmi = CaravanManager.getItem(lemi.getCaravan());
                                if (zone != null && cmi != null && cmi.getZone() != null && zone.getIniHeader().equals(cmi.getZone())) {
                                    setStatus(STATUS_IN_PLACE);
                                    setTurnsToLeave(World.TIME_MODIFIER_DAY * 2);
                                    UIPanel.createTradePanelContent(this);
                                }
                            } else {
                                // No está en una zona y no tiene path. A buscar zona o a pirarse
                                Zone zone;
                                ArrayList<Point3DShort> alDestinations = null;
                                CaravanManagerItem cmi = CaravanManager.getItem(lemi.getCaravan());
                                if (cmi != null) {
                                    for (int i = 0; i < Game.getWorld().getZones().size(); i++) {
                                        zone = Game.getWorld().getZones().get(i);
                                        if (zone.getIniHeader().equals(cmi.getZone())) {
                                            // La tenemos
                                            if (alDestinations == null) {
                                                alDestinations = new ArrayList<Point3DShort>();
                                            }
                                            alDestinations.add(zone.getPoints().get(Utils.getRandomBetween(0, zone.getPoints().size() - 1)));
                                        }
                                    }

                                    if (alDestinations == null || alDestinations.size() == 0) {
                                        // Ni una zona, pacasa
                                        setStatus(STATUS_LEAVING);
                                    } else {
										// Tenemos puntos de zona, a ver si podemos ir
                                        // Vamos recorriendo la lista de zones a random
                                        Point3DShort p3dDestination = null;
                                        int iCurrentASZID = World.getCell(le.getCoordinates()).getAstarZoneID();
                                        while (alDestinations.size() > 0) {
                                            p3dDestination = alDestinations.remove(Utils.getRandomBetween(0, alDestinations.size() - 1));
                                            if (World.getCell(p3dDestination).getAstarZoneID() == iCurrentASZID) {
                                                // Bingo
                                                le.setDestination(p3dDestination);
                                                return false;
                                            }
                                        }

                                        // Si llega aquí es que nanay de la china
                                        setStatus(STATUS_LEAVING);
                                    }
                                } else {
                                    // Raro, por si acaso nos piramos
                                    setStatus(STATUS_LEAVING);
                                }
                            }
                        } else {
                            // Tiene camino, a saber pq se ha llamado a esta función
                        }
                    } else {
                        // Raro, por si acaso nos piramos
                        setStatus(STATUS_LEAVING);
                    }
                } else {
                    // Rarísimo, no existe en el mundo
                    setStatus(STATUS_LEAVING);
                }
                break;

            case STATUS_IN_PLACE:
                break;
            case STATUS_TRADING:
                break;
            case STATUS_LEAVING:
                le = World.getLivingEntityByID(getLivingId());
                if (le != null) {
                    if (le.getCoordinates().equals(getStartingPoint())) {
                        return true;
                    }

                    if ((le.getFocusData() != null || le.getFocusData().getEntityID() == -1) && le.getPath().size() == 0) {
                        // Lista para irse
                        if (World.getCell(le.getCoordinates()).getAstarZoneID() == World.getCell(getStartingPoint()).getAstarZoneID()) {
                            // Via libre
                            le.setDestination(getStartingPoint());
                        } else {
                            // Buscamos una salida
                            Point3DShort p3dLeave = World.getRandomBorderPoint(World.getCell(le.getCoordinates()).getAstarZoneID());
                            if (p3dLeave != null) {
                                // Encontrado, nos vamos!!
                                setStartingPoint(p3dLeave);
                                le.setDestination(getStartingPoint());
                            }
                        }
                    }
                    break;
                }
                break;
        }

        return false;
    }

    public boolean confirmTrade() {
        if (getStatus() != STATUS_IN_PLACE) {
            return false;
        }

        CaravanData caravanData = Game.getWorld().getCurrentCaravanData();
        if (caravanData == null) {
            return false;
        }
        LivingEntity leCaravan = World.getLivingEntityByID(getLivingId());
        if (leCaravan == null) {
            // Uh? La caravana no existe?
            return false;
        }

        setStatus(STATUS_TRADING);
        setTurnsToLeave(World.TIME_MODIFIER_DAY * 2);

        // Creamos las tareas de los citizens para llevar items a la caravana
        SmartMenu smItem;
        for (int i = 0; i < getMenuTownToSell().getItems().size(); i++) {
            smItem = getMenuTownToSell().getItems().get(i);
            for (int q = 0; q < smItem.getDirectCoordinates().y; q++) {
                // Nueva tarea
                Task task = new Task(Task.TASK_MOVE_TO_CARAVAN);
                task.setParameter(smItem.getParameter()); // Item iniheader
                task.setPointIni(new Point3D(getLivingId(), smItem.getDirectCoordinates().z, -1)); // Caravan ID y itemID (caso militares)
                task.setPointEnd(leCaravan.getCoordinates());
                ArrayList<HotPoint> alHotPoint = new ArrayList<HotPoint>(1);
                alHotPoint.add(new HotPoint(leCaravan.getCoordinates(), leCaravan.getCoordinates()));
                task.setHotPoints(alHotPoint);
                task.setMaxCitizens(1);

                Game.getWorld().getTaskManager().addTask(task);
            }
        }

        // Miramos si ya se puede soltar algún item
        while (checkItemToDrop()) {
        }

        // Miramos si ya se ha acabado el trade
        if (getMenuCaravanToBuy().getItems().size() == 0 && getMenuTownToSell().getItems().size() == 0) {
            setStatus(STATUS_LEAVING);
        }

        return true;
    }

    private boolean checkItemToDrop() {
        CaravanData caravanData = Game.getWorld().getCurrentCaravanData();
        if (caravanData == null) {
            return false;
        }
        LivingEntity leCaravan = World.getLivingEntityByID(getLivingId());
        if (leCaravan == null) {
            // Uh? La caravana no existe?
            return false;
        }

        // Según la pasta del pueblo miramos si puede soltar objetos
        int iWorldCoins = Game.getWorld().getCoins();
        if (iWorldCoins > 0) {
            // Hay pasta, miramos si hay algún item que pueda soltar
            int iIndexItem = -1;
            SmartMenu smItem = null;
            for (int i = 0; i < getMenuCaravanToBuy().getItems().size(); i++) {
                smItem = getMenuCaravanToBuy().getItems().get(i);
                if (smItem.getDirectCoordinates().x <= iWorldCoins) {
                    iIndexItem = i;
                    break;
                }
            }

            if (iIndexItem != -1) {
				// Encontrado! Lo soltamos
                // Buscamos el item en la lista de items de caravana (tiene que existir por huevos)
                int iIndexRealItem = -1;
                ArrayList<CaravanItemDataInstance> alItems = caravanData.getAlItems();
                CaravanItemDataInstance cidi = null;
                Item itemAux;
                for (int i = 0; i < alItems.size(); i++) {
                    cidi = alItems.get(i);
                    itemAux = cidi.getItem();
                    if (itemAux != null && itemAux instanceof MilitaryItem) {
                        // Militar
                        if (itemAux.getID() == smItem.getDirectCoordinates().z) {
                            // Encontrado!
                            iIndexRealItem = i;
                            break;
                        }
                    } else {
                        // NO militar
                        if (((cidi.getPrice() * getPricePCT()) / 100) == smItem.getDirectCoordinates().x && cidi.getItem().getIniHeader().equals(smItem.getParameter())) {
                            // Encontrado!
                            iIndexRealItem = i;
                            break;
                        }
                    }
                }

                if (iIndexRealItem != -1) {
                    // Al lio
                    ItemManagerItem imi = ItemManager.getItem(cidi.getItem().getIniHeader());
                    Point3DShort p3dDrop = getDropPoint(leCaravan.getCoordinates(), imi);
                    if (p3dDrop != null) {
                        // Se puede dejar en la location actual, go go go
                        Item item = cidi.getItem();
                        if (!(item instanceof MilitaryItem)) {
                            // Item no militar, creamos una instancia
                            item = Item.createItem(imi);
                        }

                        // Lo inicializamos y metemos en la celda
                        item.init(p3dDrop.x, p3dDrop.y, p3dDrop.z);
                        item.setOperative(imi.isAlwaysOperative());
                        item.setLocked(false);
                        World.getCell(p3dDrop).setEntity(item);

                        if (item instanceof MilitaryItem) {
                            // Item militar, lo borramos de la lista de caravana
                            caravanData.getAlItems().remove(iIndexRealItem);
                        }

                        // Restamos 1 al qtty, si llega a 0 se borra de la lista de caravana
                        Point3D p3dInfo = smItem.getDirectCoordinates();
                        p3dInfo.y = p3dInfo.y - 1;
                        if (p3dInfo.y <= 0) {
                            // Se borra de la lista
                            getMenuCaravanToBuy().getItems().remove(iIndexItem);
                            UIPanel.createTradePanelContent(this);
                        } else {
                            smItem.setDirectCoordinates(p3dInfo);
                            smItem.setParameter2("x" + p3dInfo.y); //$NON-NLS-1$
                        }

                        // Restamos el precio de las coins de town y lo sumamos a la caravana
                        iWorldCoins -= p3dInfo.x;
                        setCoins(getCoins() + p3dInfo.x);
                        Game.getWorld().setCoins(iWorldCoins);

                        Point3DShort.returnToPool(p3dDrop);
                        return true;
                    }
                } else {
                    // Item no encontrado ¿?¿?¿? rarísimo, lo eliminamos de la lista de to-buy
                    getMenuCaravanToBuy().getItems().remove(iIndexItem);
                }
            }
        }

        return false;
    }

    /**
     * Se llama cuando un aldeano ha dejado un item en la caravana
     *
     * @param item
     * @return true si la transacción es correcta (debería)
     */
    public boolean itemCarried(Item item) {
        if (item == null) {
            return false;
        }

        int iPrice = PricesManager.getPrice(item);

        // Lo quitamos de la lista de items to-sell
        SmartMenu smItem = null;
        int iIndexItem = -1;
        boolean bMilitary = item instanceof MilitaryItem;
        for (int i = 0; i < getMenuTownToSell().getItems().size(); i++) {
            smItem = getMenuTownToSell().getItems().get(i);

            if (bMilitary) {
                if (smItem.getDirectCoordinates().z == item.getID()) {
                    // Encontrado
                    iIndexItem = i;
                    break;
                }
            } else {
                if (smItem.getDirectCoordinates().x == iPrice && smItem.getParameter().equals(item.getIniHeader())) {
                    // Encontrado
                    iIndexItem = i;
                    break;
                }
            }
        }

        if (iIndexItem != -1) {
            // Lo normal, item encontrado
            Point3D p3dInfo = smItem.getDirectCoordinates();
            p3dInfo.y = p3dInfo.y - 1;
            if (p3dInfo.y <= 0) {
                getMenuTownToSell().getItems().remove(iIndexItem);
                UIPanel.createTradePanelContent(this);
            } else {
                getMenuTownToSell().getItems().get(iIndexItem).setDirectCoordinates(p3dInfo);
                getMenuTownToSell().getItems().get(iIndexItem).setParameter2("x" + p3dInfo.y); //$NON-NLS-1$
            }

            // Borramos container si es que es un container
            if (ItemManager.getItem(item.getIniHeader()).isContainer()) {
                Game.getWorld().deleteContainer(item.getID());
            }

            // Sumamos / restamos el precio
            Game.getWorld().setCoins(Game.getWorld().getCoins() + iPrice);
            CaravanData caravanData = Game.getWorld().getCurrentCaravanData();
            if (caravanData != null) {
                caravanData.setCoins(caravanData.getCoins() - iPrice);
            }

            // Miramos si la caravana puede soltar más cosas
            checkItemToDrop();

            // Miramos si ya se ha acabado el trade
            if (getMenuCaravanToBuy().getItems().size() == 0 && getMenuTownToSell().getItems().size() == 0) {
                setStatus(STATUS_LEAVING);
            } else {
                // Si no se ha acabado el trade reseteamos el leaving
                setTurnsToLeave(World.TIME_MODIFIER_DAY * 2);
            }

            return true;
        } else {
            // Raro, raro, no hacemos nada
            return false;
        }
    }

    /**
     * Retorna un punto donde dejar el item, o null si no lo encuentra
     *
     * @param p3dIniPoint
     * @param imi
     * @return
     */
    private Point3DShort getDropPoint(Point3DShort p3dIniPoint, ItemManagerItem imi) {
        Point3DShort p3dDrop = null;

		// Si está en una zona (debería) miraremos los puntos de la misma
        // En otro caso (o si en la zona no hay puntos libres) miraremos en un radio de 8 casillas
        int iZoneID = World.getCell(p3dIniPoint).getZoneID();
        boolean bCellFound = false;
        if (iZoneID > 0) {
            Zone zone = Zone.getZone(iZoneID);
            if (zone != null) {
                for (int i = 0; i < zone.getPoints().size(); i++) {
                    if (Item.isCellAvailableForItem(imi, zone.getPoints().get(i), false, true, false)) {
                        bCellFound = true;
                        p3dDrop = Point3DShort.getPoolInstance(zone.getPoints().get(i));
                        break;
                    }
                }
            }
        }

        // No está en zona o no hay places disponibles, miramos un radio de 8
        if (!bCellFound) {
            Point3DShort p3dTemp;
            radiusloop:
            for (int radius = 1; radius <= 8; radius++) {
                // Arriba y abajo
                for (int i = -radius; i <= radius; i++) {
                    p3dTemp = Point3DShort.getPoolInstance(p3dIniPoint.x + i, p3dIniPoint.y - radius, p3dIniPoint.z);
                    if (p3dTemp.x >= 0 && p3dTemp.x < World.MAP_WIDTH && p3dTemp.y >= 0 && p3dTemp.y < World.MAP_HEIGHT) {
                        if (Item.isCellAvailableForItem(imi, p3dTemp, false, true, false)) {
                            p3dDrop = Point3DShort.getPoolInstance(p3dTemp);
                            break radiusloop;
                        }
                    }
                    p3dTemp = Point3DShort.getPoolInstance(p3dIniPoint.x + i, p3dIniPoint.y + radius, p3dIniPoint.z);
                    if (p3dTemp.x >= 0 && p3dTemp.x < World.MAP_WIDTH && p3dTemp.y >= 0 && p3dTemp.y < World.MAP_HEIGHT) {
                        if (Item.isCellAvailableForItem(imi, p3dTemp, false, true, false)) {
                            p3dDrop = Point3DShort.getPoolInstance(p3dTemp);
                            break radiusloop;
                        }
                    }
                }
                // Izquierda y derecha
                for (int i = -radius; i <= radius; i++) {
                    p3dTemp = Point3DShort.getPoolInstance(p3dIniPoint.x - radius, p3dIniPoint.y + i, p3dIniPoint.z);
                    if (p3dTemp.x >= 0 && p3dTemp.x < World.MAP_WIDTH && p3dTemp.y >= 0 && p3dTemp.y < World.MAP_HEIGHT) {
                        if (Item.isCellAvailableForItem(imi, p3dTemp, false, true, false)) {
                            p3dDrop = Point3DShort.getPoolInstance(p3dTemp);
                            break radiusloop;
                        }
                    }
                    p3dTemp = Point3DShort.getPoolInstance(p3dIniPoint.x + radius, p3dIniPoint.y + i, p3dIniPoint.z);
                    if (p3dTemp.x >= 0 && p3dTemp.x < World.MAP_WIDTH && p3dTemp.y >= 0 && p3dTemp.y < World.MAP_HEIGHT) {
                        if (Item.isCellAvailableForItem(imi, p3dTemp, false, true, false)) {
                            p3dDrop = Point3DShort.getPoolInstance(p3dTemp);
                            break radiusloop;
                        }
                    }
                }
            }
        }

        return p3dDrop;
    }

    public void setPricePCT(int pricePCT) {
        if (pricePCT < 110) {
            pricePCT = 110;
        }
        this.pricePCT = pricePCT;
    }

    public int getPricePCT() {
        return pricePCT;
    }

    public void setCoins(int coins) {
        if (coins < 0) {
            coins = 0;
        }
        this.coins = coins;
    }

    public int getCoins() {
        return coins;
    }

    public void setStartingPoint(Point3DShort startingPoint) {
        this.startingPoint = startingPoint;
    }

    public Point3DShort getStartingPoint() {
        return startingPoint;
    }

    public int getTurnsToLeave() {
        return turnsToLeave;
    }

    public void setTurnsToLeave(int turnsToLeave) {
        this.turnsToLeave = turnsToLeave;
    }

    public void setMenuCaravanToBuy(SmartMenu menuCaravanToBuy) {
        this.menuCaravanToBuy = menuCaravanToBuy;
    }

    public SmartMenu getMenuCaravanToBuy() {
        return menuCaravanToBuy;
    }

    public void setMenuTownToSell(SmartMenu menuTownToSell) {
        this.menuTownToSell = menuTownToSell;
    }

    public SmartMenu getMenuTownToSell() {
        return menuTownToSell;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        livingId = in.readInt();
        alItems = (ArrayList<CaravanItemDataInstance>) in.readObject();
        status = in.readInt();
        pricePCT = in.readInt();
        coins = in.readInt();
        startingPoint = (Point3DShort) in.readObject();
        turnsToLeave = in.readInt();
        menuCaravanToBuy = (SmartMenu) in.readObject();
        menuTownToSell = (SmartMenu) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(livingId);
        out.writeObject(alItems);
        out.writeInt(status);
        out.writeInt(pricePCT);
        out.writeInt(coins);
        out.writeObject(startingPoint);
        out.writeInt(turnsToLeave);
        out.writeObject(menuCaravanToBuy);
        out.writeObject(menuTownToSell);
    }
}
