package xaos.tiles.entities.buildings;

import java.awt.Color;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

import xaos.main.Game;
import xaos.main.World;
import xaos.panels.CommandPanel;
import xaos.panels.menus.SmartMenu;
import xaos.tiles.Cell;
import xaos.tiles.entities.Entity;
import xaos.tiles.entities.items.Item;
import xaos.tiles.entities.items.ItemManager;
import xaos.tiles.entities.items.ItemManagerItem;
import xaos.tiles.entities.living.LivingEntity;
import xaos.tiles.entities.living.LivingEntityManager;
import xaos.tiles.entities.living.LivingEntityManagerItem;
import xaos.utils.Messages;
import xaos.utils.Point3D;
import xaos.utils.Point3DShort;
import xaos.utils.UtilsIniHeaders;

public class Building extends Entity implements Externalizable {

    private static final long serialVersionUID = 582404994776340708L;

    public static String TYPE_SPAWN = "SPAWN"; //$NON-NLS-1$

    public static char GROUND_ENTRANCE = 'E'; //$NON-NLS-1$
    public static char GROUND_TRANSITABLE = '1'; //$NON-NLS-1$
    public static char GROUND_NON_TRANSITABLE = '0'; //$NON-NLS-1$
    public static char GROUND_NON_BUILDING = 'X'; //$NON-NLS-1$

    /**
     * Indica si ya está operativo
     */
    private boolean operative;

    /**
     * Son los prerequisitos para construirse, a medida que los aldeanos los
     * traigan irá desapareciendo Cuando lleguen a 0 el edificio pasa a estar
     * operativo
     */
    private ArrayList<int[]> prerequisites;
    private ArrayList<int[]> prerequisitesLiving;

    private boolean nonStop; // Indica si siempre está produciendo items o se hace manual
    private ArrayList<Item> itemQueue; // Es la lista de items en cola para producirse
    private String lastItem; // Indica el último item producido, es útil cuando esté en modo non-stop
    private int counter; // Se usa para contar el tiempo de los items automáticos

    public Building() {
        super();
    }

    public Building(String iniHeader) {
        super(iniHeader);
    }

    public boolean isOperative() {
        return operative;
    }

    public void setOperative(boolean operative, boolean setZoneIDS) {
        this.operative = operative;

        // Regeneramos las zonas A*
        if (setZoneIDS) {
            World.setRecheckASZID(true);
        }
    }

    public ArrayList<int[]> getPrerequisites() {
        return prerequisites;
    }

    public void setPrerequisites(ArrayList<int[]> prerequisites) {
        this.prerequisites = prerequisites;
    }

    public ArrayList<int[]> getPrerequisitesLiving() {
        return prerequisitesLiving;
    }

    public void setPrerequisitesLiving(ArrayList<int[]> prerequisitesLiving) {
        this.prerequisitesLiving = prerequisitesLiving;
    }

    /**
     * Indica si en las coordenadas pasadas se puede construir
     *
     * @param x
     * @param y
     * @param z
     *
     * @return true si en las coordenadas pasadas se puede construir
     */
    public static boolean isCellAvailableForBuilding(BuildingManagerItem bmi, Point3D point) {
        return isCellAvailableForBuilding(bmi, point.x, point.y, point.z);
    }

    /**
     * Indica si en las coordenadas pasadas se puede construir
     *
     * @param x
     * @param y
     * @param z
     *
     * @return true si en las coordenadas pasadas se puede construir
     */
    public static boolean isCellAvailableForBuilding(BuildingManagerItem bmi, int x, int y, int z) {
        // Fuera del mapa
        if (x < 0 || x >= World.MAP_WIDTH || y < 0 || y >= World.MAP_HEIGHT || z < 0 || z >= World.MAP_DEPTH) {
            return false;
        }

        // Casilla NO descubierta
        Cell cell = World.getCells()[x][y][z];
        if (!cell.isDiscovered()) {
            return false;
        }

        // Casilla NO minada o con fluidos
        if (!cell.isMined() || cell.getTerrain().hasFluids()) {
            return false;
        }

        // No vacía o con zona
        if (!cell.isEmpty() || cell.hasZone()) {
            return false;
        }

        // Underground?
        if (!bmi.canBeBuiltUnderground()) {
            if (z >= World.MAP_NUM_LEVELS_OUTSIDE) {
                return false;
            }
        }

        // Casilla de abajo minada y los mustBeBuiltOver
        if (z < (World.MAP_DEPTH - 1)) {
            Cell cellUnder = World.getCell(x, y, z + 1);
            if (cellUnder.isMined()) {
                return false;
            } else {
                // Over terrain!
                if (bmi.getMustBeBuiltOver() != null && bmi.getMustBeBuiltOver().size() > 0) {
                    if (!bmi.getMustBeBuiltOver().contains(cellUnder.getTerrain().getTerrainID())) {
                        return false;
                    }
                }
            }
        } else {
            return false;
        }

        return true;
    }

    /**
     * Crea un objeto edificio
     *
     * @param item Objeto con las características del edificio
     *
     * @return un objeto edificio
     */
    public static Building createBuilding(BuildingManagerItem item) {
        if (item == null) {
            return null;
        }

        return new Building(item.getIniHeader());
    }

    /**
     * Devuelve el edificio de una casilla dada
     *
     * @param x X
     * @param y Y
     * @param z Z
     * @return el edificio de la casilla dada
     */
    public static Building getBuilding(int x, int y, int z) {
        Point3DShort p3d = World.getCells()[x][y][z].getBuildingCoordinates();
        if (p3d == null) {
            return null;
        }

        Entity entity = World.getCell(p3d).getEntity();
        if (entity instanceof Building) {
            return (Building) entity;
        }

        return null;
    }

    /**
     * Devuelve el edificio de una casilla dada
     *
     * @param p3d Punto
     * @return el edificio de la casilla dada
     */
    public static Building getBuilding(Point3DShort p3d) {
        return getBuilding(p3d.x, p3d.y, p3d.z);
    }

    public static Building getBuilding(Point3D p3d) {
        return getBuilding(p3d.x, p3d.y, p3d.z);
    }

    public void setItemQueue(ArrayList<Item> itemQueue) {
        this.itemQueue = itemQueue;
    }

    public ArrayList<Item> getItemQueue() {
        if (itemQueue == null) {
            itemQueue = new ArrayList<Item>();
        }
        return itemQueue;
    }

    public void addItem(Item item) {
        getItemQueue().add(item);
        setLastItem(item.getIniHeader());
    }

    public boolean hasItemsInQueue() {
        return getItemQueue().size() > 0;
    }

    public void setLastItem(String lastItem) {
        this.lastItem = lastItem;
    }

    public String getLastItem() {
        return lastItem;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public int getCounter() {
        return counter;
    }

    public void setNonStop(boolean nonStop) {
        this.nonStop = nonStop;
    }

    public boolean isNonStop() {
        return nonStop;
    }

    /**
     * Elimina el edificio del punto dado.
     *
     * @param p3d Punto
     */
    public static void delete(Point3DShort p3d) {
        Cell cell = World.getCell(p3d);
        if (cell.hasBuilding()) {
            cell = World.getCell(cell.getBuildingCoordinates());
            if (cell.hasEntity()) {
                cell.getEntity().delete();
            }
        }
    }

    public void init(short x, short y, short z) {
        super.init(x, y, z);

        // Lo metemos en la lista de buildings
        World.getBuildings().add(this);

        setAllBuildingCoordinates();
    }

    public void setAllBuildingCoordinates() {
        BuildingManagerItem bmi = BuildingManager.getItem(getIniHeader());
        // Seteamos los flags de las celdas
        for (int i = getX(); i < (getX() + bmi.getWidth()); i++) {
            for (int j = getY(); j < (getY() + bmi.getHeight()); j++) {
                char groundDataChar = bmi.getGroundData().charAt((j - getY()) * bmi.getWidth() + (i - getX()));
                if (groundDataChar == Building.GROUND_NON_BUILDING) {
                    continue;
                }

                World.getCell(i, j, getZ()).setBuildingCoordinates(getCoordinates());
            }
        }
    }

    /**
     * Recorre todas las celdas del mundo y lanza un init por cada building
     * encontrado
     */
    public static void loadBuildings() {
        World.getBuildings(); // Por si no hay items en la partida, que cree la hash

        Cell cell;
        for (short x = 0; x < World.MAP_WIDTH; x++) {
            for (short y = 0; y < World.MAP_HEIGHT; y++) {
                for (short z = 0; z < World.MAP_DEPTH; z++) {
                    cell = World.getCell(x, y, z);
                    if (cell.getEntity() != null && cell.getEntity() instanceof Building) {
                        ((Building) cell.getEntity()).init(x, y, z);
                    }
                }
            }
        }
    }

    /**
     * Elimina el edificio del juego
     */
    public void delete() {
        super.delete(); // Lo elimina del mapa

        // Lo sacamos de la lista de Buildings
        Building building;
        int iIndex = -1;
        for (int i = 0; i < World.getBuildings().size(); i++) {
            building = World.getBuildings().get(i);

            // Miramos las coordenadas "reales" del edificio
            if (building.getCoordinates().equals(World.getCell(getCoordinates()).getBuildingCoordinates())) {
                iIndex = i;
                break;
            }
        }

        if (iIndex != -1) {
            building = World.getBuildings().remove(iIndex);

            BuildingManagerItem bmi = BuildingManager.getItem(getIniHeader());
            // Quitamos también las marcas de la/s celda/s (de edificio y de A*ZoneID)
            for (int x = building.getX(); x < (building.getX() + bmi.getWidth()); x++) {
                for (int y = building.getY(); y < (building.getY() + bmi.getHeight()); y++) {
                    char groundDataChar = bmi.getGroundData().charAt((y - building.getY()) * bmi.getWidth() + (x - building.getX()));
                    if (groundDataChar == Building.GROUND_NON_BUILDING) {
                        continue;
                    }

                    World.getCell(x, y, building.getZ()).setBuildingCoordinates(null);
                    World.getCell(x, y, building.getZ()).setAstarZoneID(-1);
                }
            }

            // Regeneramos las zonas A*
            for (int x = building.getX(); x < (building.getX() + bmi.getWidth()); x++) {
                for (int y = building.getY(); y < (building.getY() + bmi.getHeight()); y++) {
                    char groundDataChar = bmi.getGroundData().charAt((y - building.getY()) * bmi.getWidth() + (x - building.getX()));
                    if (groundDataChar == Building.GROUND_NON_BUILDING) {
                        continue;
                    }

                    Cell.mergeZoneID(x, y, building.getZ(), false);
                }
            }
        }
    }

    public void refreshTransients() {
        super.refreshTransients();

        if (hasItemsInQueue()) {
            for (int i = 0; i < getItemQueue().size(); i++) {
                getItemQueue().get(i).refreshTransients();
            }
        }
    }

    /**
     * Fills a contextual menú refering buildings of a cell
     *
     * @param cell
     * @param sm
     */
    public static void fillMenu(Cell cell, SmartMenu sm) {
        if (cell.hasBuilding()) {
            Building building = Building.getBuilding(cell.getBuildingCoordinates());
            if (building != null) {
                if (!building.isOperative() && (building.getPrerequisites().size() > 0 || building.getPrerequisitesLiving().size() > 0)) {
                    // Mostramos los prerequisitos si aún no se ha construido
                    sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("Building.0"), null, null, null)); //$NON-NLS-1$
                    if (building.getPrerequisites() != null) {
                        for (int i = 0; i < building.getPrerequisites().size(); i++) {
                            int iNumInWorld = 0;
                            String sMats = null;
                            for (int j = 0; j < building.getPrerequisites().get(i).length; j++) {
                                iNumInWorld += Item.getNumItems(building.getPrerequisites().get(i)[j], false, Game.getWorld ().getRestrictHaulEquippingLevel ());

                                if (j == 0) {
                                    sMats = ItemManager.getItem(UtilsIniHeaders.getStringIniHeader(building.getPrerequisites().get(i)[j])).getName();
                                } else {
                                    sMats += Messages.getString("SmartMenu.3") + ItemManager.getItem(UtilsIniHeaders.getStringIniHeader(building.getPrerequisites().get(i)[j])).getName();
                                }
                            }

                            if (sMats != null) {
                                if (iNumInWorld > 0) {
                                    sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("Building.1") + sMats, null, null, null, null, null, Color.GREEN)); //$NON-NLS-1$
                                } else {
                                    sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("Building.1") + sMats, null, null, null, null, null, Color.RED)); //$NON-NLS-1$
                                }
                            }
                        }
                    }

                    if (building.getPrerequisitesLiving() != null) {
                        for (int i = 0; i < building.getPrerequisitesLiving().size(); i++) {
                            int iNumInWorld = 0;
                            String sMats = null;

                            for (int j = 0; j < building.getPrerequisitesLiving().get(i).length; j++) {
                                iNumInWorld += LivingEntity.getNumLivings(UtilsIniHeaders.getStringIniHeader(building.getPrerequisitesLiving().get(i)[j]), true);

                                if (j == 0) {
                                    sMats = LivingEntityManager.getItem(UtilsIniHeaders.getStringIniHeader(building.getPrerequisitesLiving().get(i)[j])).getName();
                                } else {
                                    sMats += Messages.getString("SmartMenu.3") + LivingEntityManager.getItem(UtilsIniHeaders.getStringIniHeader(building.getPrerequisitesLiving().get(i)[j])).getName();
                                }
                            }

                            // Estic escribint els pigs en vermell, tot i que existeixen
                            if (sMats != null) {
                                if (iNumInWorld > 0) {
                                    sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("Building.1") + sMats, null, null, null, null, null, Color.GREEN)); //$NON-NLS-1$
                                } else {
                                    sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("Building.1") + sMats, null, null, null, null, null, Color.RED)); //$NON-NLS-1$
                                }
                            }
                        }
                    }
                }

                if (building.isOperative()) {
                    // Non-stop on/off
                    if (building.isNonStop()) {
                        sm.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Building.9"), null, CommandPanel.COMMAND_TURN_OFF_NONSTOP, null, null, cell.getBuildingCoordinates().toPoint3D())); //$NON-NLS-1$
                    } else {
                        sm.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Building.10"), null, CommandPanel.COMMAND_TURN_ON_NONSTOP, null, null, cell.getBuildingCoordinates().toPoint3D())); //$NON-NLS-1$
                    }
                }

                BuildingManagerItem bmi = BuildingManager.getItem(building.getIniHeader());
                if (building.isOperative() && !bmi.isAutomatic()) {
                    // Obtenemos todo lo que puede crear
                    ArrayList<ItemManagerItem> alIMI = ItemManager.getItemsByBuilding(building.getIniHeader());
                    if (alIMI.size() > 0) {
                        // Añadir objetos para crear
                        SmartMenu smBuildingAdd = new SmartMenu(SmartMenu.TYPE_MENU, Messages.getString("Building.2"), sm, null, null); //$NON-NLS-1$

                        String sName;
                        for (int i = 0; i < alIMI.size(); i++) {
                            // Add
                            sName = alIMI.get(i).getName();
                            if (sName == null || sName.length() == 0) {
                                sName = alIMI.get(i).getName();
                            }
                            smBuildingAdd.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Building.3") + sName, null, CommandPanel.COMMAND_CREATE_IN_A_BUILDING, alIMI.get(i).getIniHeader(), null, cell.getBuildingCoordinates().toPoint3D())); //$NON-NLS-1$
                        }
                        smBuildingAdd.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
                        smBuildingAdd.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Building.4"), sm, CommandPanel.COMMAND_BACK, null)); //$NON-NLS-1$
                        sm.addItem(smBuildingAdd);
                    }

                    // Si tiene cosas en cola añadimos el menú de quitarlas
                    if (building.hasItemsInQueue()) {
                        // Remove tasks
                        SmartMenu smBuildingRemove = new SmartMenu(SmartMenu.TYPE_MENU, Messages.getString("Building.5"), sm, null, null); //$NON-NLS-1$
                        ItemManagerItem imi;
                        for (int i = 0; i < building.getItemQueue().size(); i++) {
                            // Remove
                            imi = ItemManager.getItem(building.getItemQueue().get(i).getIniHeader());
                            smBuildingRemove.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Building.6") + imi.getName() + Messages.getString("Building.7"), null, CommandPanel.COMMAND_REMOVE_BUILDING_TASK, building.getItemQueue().get(i).getIniHeader(), Integer.toString(i), cell.getBuildingCoordinates().toPoint3D())); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                        smBuildingRemove.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
                        smBuildingRemove.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Building.4"), sm, CommandPanel.COMMAND_BACK, null)); //$NON-NLS-1$
                        sm.addItem(smBuildingRemove);
                    }
                    sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
                }

                sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
                // Destruir edificio (busco la entrance que es la coordenada que se usa para la tarea)
                Point3DShort p3dEntrance = bmi.getEntranceBaseCoordinates().merge(cell.getBuildingCoordinates());
                sm.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Building.8") + bmi.getName(), null, CommandPanel.COMMAND_DESTROY_BUILDING, null, null, p3dEntrance.toPoint3D(), Color.ORANGE)); //$NON-NLS-1$
                sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
            }
        }
    }

    public void updateAnimation() {
        if (isOperative()) {
            super.updateAnimation(false);
        }
    }

    public boolean nextTurn() {
        if (!isOperative() || hasItemsInQueue()) {
            return false;
        }

        if (!isNonStop()) {
            return false;
        }

        // Operativo, con non-stop y sin items en cola
        if (getLastItem() == null) { // En caso de edificio automático el primer item se pone al crearlo (Task.java)
            return false;
        }

        // Tenemos last item, y está en non-stop, le metemos otro
        BuildingManagerItem bmi = BuildingManager.getItem(getIniHeader());
        // Miramos primero si el edificio saca items o livingentities
        Point3DShort p3dEntrance = bmi.getEntranceBaseCoordinates().merge(getCoordinates());
        boolean bSpawn = (bmi.getType() != null && bmi.getType().equalsIgnoreCase(Building.TYPE_SPAWN));
        if (bSpawn) {
            // LivingEntity
            LivingEntityManagerItem lemi = LivingEntityManager.getItem(getLastItem());

            // Miramos el contador de tiempo
            if (getCounter() < lemi.getBuildingTime() || lemi.getBuildingTime() == 0) {
                setCounter(getCounter() + 1);
                return false;
            }

            // Ya toca
            setCounter(0);

            // Spawnea !
            World.addNewLiving(lemi.getIniHeader(), lemi.getType(), World.getCell(p3dEntrance).isDiscovered(), p3dEntrance.x, p3dEntrance.y, p3dEntrance.z, true);
        } else {
            // Item
            ItemManagerItem imi = ItemManager.getItem(getLastItem());

            // Miramos el contador de tiempo
            if (getCounter() < imi.getBuildingTime()) {
                setCounter(getCounter() + 1);
                return false;
            }

            // Ya toca
            setCounter(0);

            // Creamos el item a meter
            Item item = Item.createItem(imi);
            item.setCoordinates(p3dEntrance);
            item.setPrerequisites(imi.getPrerequisites());
            item.setOperative(true);
            addItem(item);
        }

        return false;
    }

    public String getTileName() {
        return BuildingManager.getItem(getIniHeader()).getName();
    }

    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        operative = in.readBoolean();
        if (Game.SAVEGAME_LOADING_VERSION <= Game.SAVEGAME_V9_V10a) {
            in.readObject(); // (ArrayList<int[]>) antic prerequisites
            in.readObject(); // (ArrayList<int[]>) antic prerequisitesLiving
        }
        nonStop = in.readBoolean();
        itemQueue = (ArrayList<Item>) in.readObject();
        lastItem = (String) in.readObject();
        counter = in.readInt();

        if (Game.SAVEGAME_LOADING_VERSION >= Game.SAVEGAME_V10b) {
            prerequisites = UtilsIniHeaders.getArrayIntsArray((ArrayList<String[]>) in.readObject());
            prerequisitesLiving = UtilsIniHeaders.getArrayIntsArray((ArrayList<String[]>) in.readObject());
        } else {
            prerequisites = new ArrayList<int[]>();
            prerequisitesLiving = new ArrayList<int[]>();
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeBoolean(operative);
		// antic out.writeObject (prerequisites);
        // antic out.writeObject (prerequisitesLiving);
        out.writeBoolean(nonStop);
        out.writeObject(itemQueue);
        out.writeObject(lastItem);
        out.writeInt(counter);

        // Pasamos a Strings
        out.writeObject(UtilsIniHeaders.getArrayStringsArray(prerequisites));
        out.writeObject(UtilsIniHeaders.getArrayStringsArray(prerequisitesLiving));
    }
}
