package xaos.zones;

import java.awt.Color;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

import xaos.data.SoldierGroupData;
import xaos.data.SoldierGroups;
import xaos.main.Game;
import xaos.main.World;
import xaos.panels.CommandPanel;
import xaos.panels.menus.SmartMenu;
import xaos.tiles.Cell;
import xaos.tiles.entities.Entity;
import xaos.tiles.entities.items.Item;
import xaos.tiles.entities.items.ItemManager;
import xaos.tiles.entities.items.ItemManagerItem;
import xaos.tiles.entities.living.Citizen;
import xaos.tiles.entities.living.LivingEntity;
import xaos.tiles.entities.living.heroes.Hero;
import xaos.utils.Messages;
import xaos.utils.Point3DShort;
import xaos.utils.Utils;

public class Zone implements Externalizable {

    private static final long serialVersionUID = 3172280383849844317L;

    public static int ID_INDEX = 0;

    private String iniHeader; // Identificador de la zona (obtenido del zones.xml)
    private int ID; // Id numérico
    private ArrayList<Point3DShort> points;
    private boolean operative;

    public Zone() {
    }

    public Zone(String sIniHeader) {
        ID_INDEX++;
        setID(ID_INDEX);

        setIniHeader(sIniHeader);
        setPoints(new ArrayList<Point3DShort>());
        setOperative(true);
    }

    public void setID(int iD) {
        ID = iD;
    }

    public int getID() {
        return ID;
    }

    public void setIniHeader(String iniHeader) {
        this.iniHeader = iniHeader;
    }

    public String getIniHeader() {
        return iniHeader;
    }

    public ArrayList<Point3DShort> getPoints() {
        return points;
    }

    public void setPoints(ArrayList<Point3DShort> points) {
        this.points = points;
    }

    public void addPoint(Point3DShort p3d) {
        getPoints().add(p3d);
    }

    public void setOperative(boolean operative) {
        this.operative = operative;
    }

    public boolean isOperative() {
        return operative;
    }

    /**
     * Indica si en las coordenadas pasadas puede ir una zona
     *
     * @param x
     * @param y
     * @param z
     * @return true si en las coordenadas pasadas puede ir una zona
     */
    public static boolean isCellAvailableForZone(int x, int y, int z, Zone expandZone) {
        Cell cell = World.getCell(x, y, z);

        if (!cell.isDiscovered()) {
            return false;
        }

		// No miramos el digada ya que ya se mirará en el living-iscellAllowed, de paso así mirará si hay base=true debajo
//		if (cell.getTerrain ().isDigged ()) {
//			return false;
//		}
        // Miramos si es accesible por aldeanos
        if (!LivingEntity.isCellAllowed(cell)) {
            return false;
        }

        if (expandZone == null) {
            // Zona nueva, no puede haber otra zona ahí (a no ser que tenga neighbors)
            if (cell.hasZone()) {
                return false;
            }
        } else {
            // Expandiendo zona, la casilla debe ser igual a una de la zona anterior
            if (!expandZone.getPoints().contains(cell.getCoordinates())) {
                return false;
            }
        }

        return true;
    }

    private static boolean checkZoneNeighbors(ZoneManagerItem zmi, int xIni, int yIni, int xEnd, int yEnd, int z) {
        if (zmi.getNeighbors().size() > 0) {
            for (int x = xIni; x <= xEnd; x++) {
                for (int y = yIni; y <= yEnd; y++) {
                    // Tiene que tocar alguno de los vecinos obligatoriamente
                    if (y > 0) {
                        if (x > 0) {
                            if (containsNeighbor(x - 1, y - 1, z, zmi.getNeighbors())) {
                                return true;
                            }
                        }
                        if (containsNeighbor(x, y - 1, z, zmi.getNeighbors())) {
                            return true;
                        }
                        if (x < (World.MAP_WIDTH - 1)) {
                            if (containsNeighbor(x + 1, y - 1, z, zmi.getNeighbors())) {
                                return true;
                            }
                        }
                    }
                    if (x > 0) {
                        if (containsNeighbor(x - 1, y, z, zmi.getNeighbors())) {
                            return true;
                        }
                    }
                    if (x < (World.MAP_WIDTH - 1)) {
                        if (containsNeighbor(x + 1, y, z, zmi.getNeighbors())) {
                            return true;
                        }
                    }
                    if (y < (World.MAP_HEIGHT - 1)) {
                        if (x > 0) {
                            if (containsNeighbor(x - 1, y + 1, z, zmi.getNeighbors())) {
                                return true;
                            }
                        }
                        if (containsNeighbor(x, y + 1, z, zmi.getNeighbors())) {
                            return true;
                        }
                        if (x < (World.MAP_WIDTH - 1)) {
                            if (containsNeighbor(x + 1, y + 1, z, zmi.getNeighbors())) {
                                return true;
                            }
                        }
                    }
                }
            }

            return false;
        }

        return true;
    }

    /**
     * Indica si la casilla pasada tiene una zona contenida en la lista de
     * vecinos
     *
     * @param x
     * @param y
     * @param z
     * @param alNeighbors
     * @return
     */
    private static boolean containsNeighbor(int x, int y, int z, ArrayList<String> alNeighbors) {
        Cell cell = World.getCell(x, y, z);
        if (!cell.hasZone()) {
            return false;
        }

        Zone zone = Zone.getZone(cell.getZoneID());
        return alNeighbors.contains(zone.getIniHeader());
    }

    /**
     * Indica si se puede crear una Zone. Se le pasan los 2 puntos de las
     * esquinas
     *
     * @param p3dIni
     * @param p3dEnd
     * @return
     */
    public static boolean areCellsAvailableForZone(ZoneManagerItem zmi, int x0, int y0, int x1, int y1, int z, Zone zoneExpand) {
        int xIni, xEnd, yIni, yEnd;
        if (x0 < x1) {
            xIni = x0;
            xEnd = x1;
        } else {
            xIni = x1;
            xEnd = x0;
        }
        if (y0 < y1) {
            yIni = y0;
            yEnd = y1;
        } else {
            yIni = y1;
            yEnd = y0;
        }

        // Miramos que no esté en el borde (con +1 de border)
        if (xIni < 0 || yIni < 0 || xEnd >= World.MAP_WIDTH || yEnd >= World.MAP_HEIGHT) {
            return false;
        }

        // Todas las casillas deben ser discovered
        for (int x = xIni; x <= xEnd; x++) {
            for (int y = yIni; y <= yEnd; y++) {
                if (!World.getCell(x, y, z).isDiscovered()) {
                    return false;
                }
            }
        }

        if (zoneExpand == null) {
			// CREATE ZONE
            // 3 de ancho y alto como mínimo
            if (((xEnd - xIni + 1) < zmi.getMinWidth()) || ((yEnd - yIni + 1) < zmi.getMinHeight())) {
                return false;
            }

            if (zmi.getNeighbors().size() == 0) {
                // Si la zone no tiene vecinos asignados, todas las casillas deben ser buenas
                for (int x = xIni; x <= xEnd; x++) {
                    for (int y = yIni; y <= yEnd; y++) {
                        if (!isCellAvailableForZone(x, y, z, null)) {
                            return false;
                        }
                    }
                }

            } else {
                // Tiene vecinos, la zona es buena si alguno de los puntos toca a una zona "neighbor"
                if (!checkZoneNeighbors(zmi, xIni, yIni, xEnd, yEnd, z)) {
                    return false;
                }
            }
        } else {
			// EXPAND ZONE
            // Si hay una casilla ok ya nos vale
            boolean bExpandOK = false;
            breakexpand:
            for (int x = xIni; x <= xEnd; x++) {
                for (int y = yIni; y <= yEnd; y++) {
                    if (isCellAvailableForZone(x, y, z, zoneExpand)) {
                        bExpandOK = true;
                        break breakexpand;
                    }
                }
            }

            if (!bExpandOK) {
                return false;
            } else {
				// Si llega aquí es que una de las casillas toca la zona anterior, ahora tenemos que mirar que no toque otras zonas

                Cell cellZones;
                for (int x = xIni; x <= xEnd; x++) {
                    for (int y = yIni; y <= yEnd; y++) {
                        cellZones = World.getCell(x, y, z);
                        if (cellZones.hasZone() && cellZones.getZoneID() != zoneExpand.getID()) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    public static void updateIndexID() {
        /**
         * Obtiene el ID más alto de Zone
         */
        ArrayList<Zone> zones = Game.getWorld().getZones();
        Zone zone;
        int iMaxID = -1;
        for (int i = 0; i < zones.size(); i++) {
            zone = zones.get(i);
            if (zone.getID() > iMaxID) {
                iMaxID = zone.getID();
            }
        }

        ID_INDEX = (iMaxID + 1);
    }

    public static Zone getZone(int iID) {
        ArrayList<Zone> zones = Game.getWorld().getZones();
        Zone zone;

        for (int i = 0; i < zones.size(); i++) {
            zone = zones.get(i);
            if (zone.getID() == iID) {
                return zone;
            }
        }

        return null;
    }

    /**
     * Borra la zone con el ID indicado
     *
     * @param ID
     */
    public static void deleteZone(int iID) {
        ArrayList<Zone> zones = Game.getWorld().getZones();
        Zone zone;
        Cell cell;
        Entity entity;
        ItemManagerItem imi;

        for (int i = 0; i < zones.size(); i++) {
            zone = zones.get(i);
            if (zone.getID() == iID) {
                // Eliminamos el flag a todos los puntos
                boolean bFlagWallsMergers = false;
                for (int j = 0; j < zone.getPoints().size(); j++) {
                    cell = World.getCell(zone.getPoints().get(j));
                    cell.setZoneID(0);

                    // Si hay un item miramos si puede ir ahí, en caso contrario lo ponemos no operativo
                    entity = cell.getEntity();
                    if (entity != null && entity instanceof Item && ((Item) entity).isLocked()) {
                        imi = ItemManager.getItem(entity.getIniHeader());
                        cell.setEntity(null);
                        if (!Item.isCellAvailableForItem(imi, zone.getPoints().get(j), true, false)) {
                            ((Item) entity).setOperative(imi.isAlwaysOperative());
                            ((Item) entity).setLocked(imi.isLocked());

                            if (imi.isWall() || imi.isZoneMergerUp()) {
                                bFlagWallsMergers = true;
                            }
                        }
                        cell.setEntity(entity);
                    }
                }

                if (bFlagWallsMergers) {
                    Cell.setAllZoneIDs();
                }

                // Eliminamos la zone
                zones.remove(i);

                ZoneManagerItem zmi = ZoneManager.getItem(zone.getIniHeader());
                // Si es personal, se la sacamos al aldeano
                if (zmi.getType() == ZoneManagerItem.TYPE_PERSONAL) {
                    Citizen citizen = null;
                    boolean bCitizenOK = false;
                    for (int j = 0; j < World.getCitizenIDs().size(); j++) {
                        citizen = (Citizen) World.getLivingEntityByID(World.getCitizenIDs().get(j));
                        if (citizen.getCitizenData().getZoneID() == iID) {
                            citizen.getCitizenData().setZoneID(0);

                            // Miramos si queda alguna zone personal libre para el aldeano
                            ZonePersonal.assignZone(citizen);
                            bCitizenOK = true;
                            break;
                        }
                    }
                    if (!bCitizenOK) {
                        // Soldier
                        for (int j = 0; j < World.getSoldierIDs().size(); j++) {
                            citizen = (Citizen) World.getLivingEntityByID(World.getSoldierIDs().get(j));
                            if (citizen.getCitizenData().getZoneID() == iID) {
                                citizen.getCitizenData().setZoneID(0);

                                // Miramos si queda alguna zone personal libre para el aldeano
                                ZonePersonal.assignZone(citizen);
                                bCitizenOK = true;
                                break;
                            }
                        }
                    }
                } else if (zmi.getType() == ZoneManagerItem.TYPE_HERO_ROOM) {
                    Hero hero = null;
                    for (int j = 0; j < World.getHeroIDs().size(); j++) {
                        hero = (Hero) World.getLivingEntityByID(World.getHeroIDs().get(j));
                        if (hero.getCitizenData().getZoneID() == iID) {
                            hero.getCitizenData().setZoneID(0);

                            // Miramos si queda alguna zone personal libre para el héroe
                            ZoneHeroRoom.assignZone(hero);
                            break;
                        }
                    }
                } else if (zmi.getType() == ZoneManagerItem.TYPE_BARRACKS) {
                    SoldierGroupData sgd;;
                    for (int j = 0; j < SoldierGroups.MAX_GROUPS; j++) {
                        sgd = Game.getWorld().getSoldierGroups().getGroup(j);
                        if (sgd.getZoneID() == iID) {
                            sgd.setZoneID(0);

                            // Miramos si queda alguna zone barracks libre para el cuartel
                            ZoneBarracks.assignZone(sgd);
                            break;
                        }
                    }
                }

                break;
            }
        }
    }

    /**
     * Elimina un punto de zone, comprueba que la zone no se haya quedado sin
     * puntos
     *
     * @param p3d
     * @return true en caso de haber borrado o haber hecho inoperativo un item
     * merger/splitter
     */
    public static boolean deleteZonePoint(Point3DShort p3d) {
        boolean bFlagWallsMergers = false;
        Cell cell = World.getCell(p3d);

        if (cell.hasZone()) {
            int iZoneID = cell.getZoneID();

            // Buscamos la zone
            ArrayList<Zone> zones = Game.getWorld().getZones();
            Zone zone;
            Entity entity;
            ItemManagerItem imi;

            for (int i = 0; i < zones.size(); i++) {
                zone = zones.get(i);
                if (zone.getID() == iZoneID) {
                    // Tenemos la zone, quitamos el punto de la zone y el flag de la celda
                    zone.getPoints().remove(p3d);
                    cell.setZoneID(0);

                    // Si hay un item miramos si puede ir ahí, en caso contrario lo ponemos no operativo
                    entity = cell.getEntity();
                    if (entity != null && entity instanceof Item && ((Item) entity).isLocked()) {
                        imi = ItemManager.getItem(entity.getIniHeader());
                        // Antes de chequear hay que sacar temporalmente el item
                        cell.setEntity(null);
                        if (!Item.isCellAvailableForItem(imi, p3d, true, false)) {
                            ((Item) entity).setOperative(imi.isAlwaysOperative());
                            ((Item) entity).setLocked(imi.isLocked());

                            if (imi.isWall() || imi.isZoneMergerUp()) {
                                bFlagWallsMergers = true;
                            }
                        }
                        cell.setEntity(entity);
                    }

                    if (zone.getPoints().size() == 0) {
                        // Zone sin puntos, la borramos
                        deleteZone(iZoneID);
                    }
                    break;
                }
            }
        }

        return bFlagWallsMergers;
    }

    /**
     * Elimina un punto de zone, comprueba que la zone no se haya quedado sin
     * puntos
     *
     * @param x
     * @param y
     * @param z
     */
    public static boolean deleteZonePoint(int x, int y, int z) {
        return deleteZonePoint(Point3DShort.getPoolInstance(x, y, z));
    }

    /**
     * Devuelve una casilla libre de la zone con una cama, si no la encuentra
     * devuelve null
     *
     * @param zone
     * @param iASZI
     * @return
     */
    public static Point3DShort getFreeSleepItemAtRandom(Zone zone, int iASZI) {
        if (zone.getPoints().size() == 0) {
            return null;
        }

        // Buscamos objetos donde se pueda dormir en la zona (camas, ...)
        ArrayList<Point3DShort> alSleepItems = new ArrayList<Point3DShort>();
        Cell cell;
        Point3DShort p3d;
        ItemManagerItem imi;
        Entity entity;
        for (int i = 0; i < zone.getPoints().size(); i++) {
            p3d = zone.getPoints().get(i);
            cell = World.getCell(p3d);
            if (iASZI == cell.getAstarZoneID()) {
                entity = cell.getEntity();
                if (entity != null && entity instanceof Item && ((Item) entity).isOperative() && ((Item) entity).isLocked()) {
                    imi = ItemManager.getItem(entity.getIniHeader());
                    if (imi.canBeUsedToSleep()) {
                        if (cell.containsSpecificLiving(LivingEntity.TYPE_CITIZEN) == null && cell.containsSpecificLiving(LivingEntity.TYPE_HERO) == null) {
                            alSleepItems.add(p3d);
                        } else {
                            // Si hay alguien durmiendo ahí, miraremos arriba (caso literas)
                            boolean bEnd = false;
                            int iIndexZ = p3d.z;
                            while (!bEnd) {
                                iIndexZ--;
                                if (iIndexZ < 0) {
                                    bEnd = true;
                                } else {
                                    cell = World.getCell(p3d.x, p3d.y, iIndexZ);
                                    if (cell.getAstarZoneID() == iASZI) {
                                        entity = cell.getEntity();
                                        if (entity != null && entity instanceof Item && ((Item) entity).isOperative() && ((Item) entity).isLocked()) {
                                            imi = ItemManager.getItem(entity.getIniHeader());
                                            if (imi.canBeUsedToSleep()) {
                                                if (cell.containsSpecificLiving(LivingEntity.TYPE_CITIZEN) == null && cell.containsSpecificLiving(LivingEntity.TYPE_HERO) == null) {
                                                    // Encontrada!
                                                    alSleepItems.add(cell.getCoordinates());
                                                    bEnd = true;
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

        if (alSleepItems.size() > 0) {
            return alSleepItems.get(Utils.getRandomBetween(0, alSleepItems.size() - 1));
        } else {
            return null;
        }
    }

    /**
     * Devuelve una casilla libre de la zone con una silla (o algo para
     * sentarse), si no la encuentra devuelve null
     *
     * @param zone
     * @param iASZI
     * @return
     */
    public static Point3DShort getFreeSitItemAtRandom(Zone zone, int iASZI) {
        if (zone.getPoints().size() == 0) {
            return null;
        }

        // Buscamos objetos donde se pueda sentarse en la zona (sillas, ...)
        ArrayList<Point3DShort> alSitItems = new ArrayList<Point3DShort>();
        Cell cell;
        Point3DShort p3d;
        ItemManagerItem imi;
        Entity entity;
        for (int i = 0; i < zone.getPoints().size(); i++) {
            p3d = zone.getPoints().get(i);
            cell = World.getCell(p3d);
            if (iASZI == cell.getAstarZoneID()) {
                entity = cell.getEntity();
                if (entity != null && entity instanceof Item && ((Item) entity).isOperative() && ((Item) entity).isLocked()) {
                    imi = ItemManager.getItem(entity.getIniHeader());
                    if (imi.canBeUsedToSit()) {
                        if (cell.containsSpecificLiving(LivingEntity.TYPE_CITIZEN) == null && cell.containsSpecificLiving(LivingEntity.TYPE_HERO) == null) {
                            alSitItems.add(p3d);
                        }
                    }
                }
            }
        }

        if (alSitItems.size() > 0) {
            return alSitItems.get(Utils.getRandomBetween(0, alSitItems.size() - 1));
        } else {
            return null;
        }
    }

    /**
     * Retorna una casilla libre de la zone o null si no la encuentra. Busca 25
     * veces a random, si así no encuentra una casilla libre se recorre todos
     * los points 1 por 1
     *
     * @param zone
     * @param iASZI
     * @return
     */
    public static Point3DShort getFreeCellAtRandom(Zone zone, int iASZI) {
        if (zone.getPoints().size() == 0) {
            return null;
        }

        int iTrys = 0;
        int iIndexPoint = Utils.getRandomBetween(0, zone.getPoints().size() - 1);
        Point3DShort p3d = zone.getPoints().get(iIndexPoint);
        while (iTrys < 25 && (iASZI != World.getCell(zone.getPoints().get(iIndexPoint)).getAstarZoneID() || World.getCell(p3d).containsSpecificLiving(LivingEntity.TYPE_CITIZEN) != null || World.getCell(p3d).containsSpecificLiving(LivingEntity.TYPE_HERO) != null)) {
            iIndexPoint = Utils.getRandomBetween(0, zone.getPoints().size() - 1);
            p3d = zone.getPoints().get(iIndexPoint);
            iTrys++;
        }

        if (iTrys == 25) {
            // Recorremos todos los puntos
            for (int i = 0; i < zone.getPoints().size(); i++) {
                p3d = zone.getPoints().get(i);
                if (iASZI == World.getCell(p3d).getAstarZoneID() && World.getCell(p3d).containsSpecificLiving(LivingEntity.TYPE_CITIZEN) == null && World.getCell(p3d).containsSpecificLiving(LivingEntity.TYPE_HERO) == null) {
                    return p3d;
                }
            }
        } else {
            // Encontrado
            return p3d;
        }

        return null;
    }

    public String toString() {
        ZoneManagerItem zmi = ZoneManager.getItem(getIniHeader());

        if (zmi == null) {
            return Messages.getString("Zone.3"); //$NON-NLS-1$
        }
        return zmi.getName();
    }

    /**
     * Fills a contextual menú refering citizens of a cell
     *
     * @param cell
     * @param sm
     */
    public static void fillMenu(Cell cell, SmartMenu sm) {
        if (cell.hasZone()) {
            Zone zone = Zone.getZone(cell.getZoneID());
            if (zone != null) {
                ZoneManagerItem zmi = ZoneManager.getItem(zone.getIniHeader());
                if (zmi.getType() == ZoneManagerItem.TYPE_PERSONAL) {
                    // Miramos si tiene propietario
                    Citizen citizen;
                    // Añadimos la opción de cambiar/añadir propietario
                    SmartMenu smOwner = new SmartMenu(SmartMenu.TYPE_MENU, Messages.getString("Zone.1"), sm, null, null); //$NON-NLS-1$
                    for (int i = 0; i < World.getCitizenIDs().size(); i++) {
                        citizen = (Citizen) World.getLivingEntityByID(World.getCitizenIDs().get(i));
                        if (citizen.getCitizenData().hasZone()) {
                            if (citizen.getCitizenData().getZoneID() != zone.getID()) {
                                smOwner.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, citizen.getCitizenData().getFullName(), null, CommandPanel.COMMAND_CHANGE_OWNER, Integer.toString(zone.getID()), Integer.toString(citizen.getID()), null, Color.ORANGE));
                            } else {
                                // Propietario, añadimos linea de texto
                                sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("Zone.6") + citizen.getCitizenData().getFullName() + Messages.getString("Zone.7"), null, null, null, null, null, Color.GREEN)); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                        } else {
                            smOwner.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, citizen.getCitizenData().getFullName(), null, CommandPanel.COMMAND_CHANGE_OWNER, Integer.toString(zone.getID()), Integer.toString(citizen.getID())));
                        }
                    }
                    for (int i = 0; i < World.getSoldierIDs().size(); i++) {
                        citizen = (Citizen) World.getLivingEntityByID(World.getSoldierIDs().get(i));
                        if (citizen.getCitizenData().hasZone()) {
                            if (citizen.getCitizenData().getZoneID() != zone.getID()) {
                                smOwner.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, citizen.getCitizenData().getFullName(), null, CommandPanel.COMMAND_CHANGE_OWNER, Integer.toString(zone.getID()), Integer.toString(citizen.getID()), null, Color.ORANGE));
                            } else {
                                // Propietario, añadimos linea de texto
                                sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("Zone.6") + citizen.getCitizenData().getFullName() + Messages.getString("Zone.7"), null, null, null, null, null, Color.GREEN)); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                        } else {
                            smOwner.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, citizen.getCitizenData().getFullName(), null, CommandPanel.COMMAND_CHANGE_OWNER, Integer.toString(zone.getID()), Integer.toString(citizen.getID())));
                        }
                    }
                    smOwner.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
                    smOwner.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Citizen.19"), sm, CommandPanel.COMMAND_BACK, null)); //$NON-NLS-1$
                    sm.addItem(smOwner);
                } else if (zmi.getType() == ZoneManagerItem.TYPE_HERO_ROOM) {
                    // Miramos si tiene propietario
                    Hero hero;
                    for (int i = 0; i < World.getHeroIDs().size(); i++) {
                        hero = (Hero) World.getLivingEntityByID(World.getHeroIDs().get(i));
                        if (hero.getCitizenData().getZoneID() == zone.getID()) {
                            // Tiene!
                            sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("Zone.6") + hero.getCitizenData().getFullName() + Messages.getString("Zone.7"), null, null, null, null, null, Color.GREEN)); //$NON-NLS-1$ //$NON-NLS-2$
                            break;
                        }
                    }
                } else if (zmi.getType() == ZoneManagerItem.TYPE_BARRACKS) {
                    // Añadimos la opción de cambiar/añadir propietario
                    SmartMenu smOwner = new SmartMenu(SmartMenu.TYPE_MENU, Messages.getString("Zone.1"), sm, null, null); //$NON-NLS-1$
                    SoldierGroupData sgd;
                    for (int i = 0; i < SoldierGroups.MAX_GROUPS; i++) {
                        sgd = Game.getWorld().getSoldierGroups().getGroup(i);
                        if (sgd.getZoneID() != zone.getID()) {
                            if (sgd.hasZone()) {
                                smOwner.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, sgd.getName(), null, CommandPanel.COMMAND_CHANGE_OWNER_GROUP, Integer.toString(zone.getID()), Integer.toString(i), null, Color.ORANGE));
                            } else {
                                smOwner.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, sgd.getName(), null, CommandPanel.COMMAND_CHANGE_OWNER_GROUP, Integer.toString(zone.getID()), Integer.toString(i)));
                            }
                        } else {
                            // Éste es el grupo propietario, añadimos una linea de texto
                            sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("Zone.8") + sgd.getName() + Messages.getString("Zone.9"), null, null, null, null, null, Color.GREEN)); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    }
                    smOwner.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
                    smOwner.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Citizen.19"), sm, CommandPanel.COMMAND_BACK, null)); //$NON-NLS-1$
                    sm.addItem(smOwner);
                }
            }
            sm.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Zone.0") + zone, null, CommandPanel.COMMAND_EXPAND_ZONE, Integer.toString(cell.getZoneID()), null, null)); //$NON-NLS-1$
            sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
            sm.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Zone.4") + zone, null, CommandPanel.COMMAND_DELETE_ZONE, Integer.toString(cell.getZoneID()), null, null)); //$NON-NLS-1$
            sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
        }
    }

    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        iniHeader = (String) in.readObject();
        ID = in.readInt();
        points = (ArrayList<Point3DShort>) in.readObject();
        operative = in.readBoolean();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(iniHeader);
        out.writeInt(ID);
        out.writeObject(points);
        out.writeBoolean(operative);
    }
}
