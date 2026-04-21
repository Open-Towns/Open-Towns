package xaos.tiles.terrain;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import xaos.TownsProperties;

import xaos.actions.ActionManager;
import xaos.actions.ActionManagerItem;
import xaos.data.SoldierData;
import xaos.data.SoldierGroupData;
import xaos.data.SoldierGroups;
import xaos.events.EventManager;
import xaos.events.EventManagerItem;
import xaos.main.Game;
import xaos.main.World;
import xaos.panels.CommandPanel;
import xaos.panels.MiniMapPanel;
import xaos.panels.menus.SmartMenu;
import xaos.stockpiles.Stockpile;
import xaos.tiles.Cell;
import xaos.tiles.entities.buildings.Building;
import xaos.tiles.entities.items.Item;
import xaos.tiles.entities.items.ItemManager;
import xaos.tiles.entities.items.ItemManagerItem;
import xaos.tiles.entities.living.Citizen;
import xaos.tiles.entities.living.LivingEntityManager;
import xaos.tiles.entities.living.LivingEntityManagerItem;
import xaos.utils.Messages;
import xaos.utils.Point3D;
import xaos.utils.Point3DShort;
import xaos.utils.Utils;
import xaos.utils.UtilsAL;
import xaos.zones.Zone;

public class Terrain implements Externalizable {

    private static final long serialVersionUID = -1566260935587865427L;

    public static final byte FLUIDS_NONE = 0;
    public static final byte FLUIDS_WATER = 1;
    public static final byte FLUIDS_LAVA = 2;

    public static final byte FLUIDS_COUNT_MAX = 6;
    public static final byte FLUIDS_COUNT_INFINITE = 7;

    private short mineTurns; // If mineTurns reachs 0, cell it's mined

    private byte fluidType;
    private byte fluidCount; // If fluidCount reachs 0, cell hasn't fluids

    private short terrainID; // Para el tipo de terreno
    private transient short terrainTileID; // Para el tile (esto es por si tiene slopes o lo que sea)

    public Terrain() {
    }

    public Terrain(String iniHeader) {
        setTerrainID(TerrainManager.getItem(iniHeader).getTerrainID());
        setTerrainTileID(getTerrainID() * TerrainManager.SLOPES_INIHEADER.length);
    }

    public void refreshTransients() {
        setTerrainTileID(getTerrainID() * TerrainManager.SLOPES_INIHEADER.length);
    }

    public int getMineTurns() {
        return mineTurns;
    }

    public void setMineTurns(int mineTurns) {
        this.mineTurns = (short) mineTurns;
    }

    /**
     * Mina la celda actual y si ha acabado sacamos el material
     *
     * @param world
     * @param x
     * @param y
     * @param z
     */
    public void mine(short x, short y, short z, boolean bMineLadder) {
        if (getMineTurns() > 0) {
            setMineTurns(getMineTurns() - 1);

            if (getMineTurns() % 5 == 0) {
                UtilsAL.play(UtilsAL.SOURCE_FX_MINE, z);
            }

            if (getMineTurns() <= 0) {
                // Mined !!
                Cell currentCell = World.getCell(x, y, z);
                Cell cellOver = null;
                currentCell.setMined(true);
                currentCell.setBlocky(false);

                // Vamos a diggear también la casilla de justo arriba
                if (z > 0) {
                    cellOver = World.getCell(x, y, z - 1);
                    cellOver.setDiscovered(true);
                    cellOver.setDigged(true);

                    // Creamos raw material en la casilla
                    if (!bMineLadder) {
                        Item rm = createDrop(currentCell.getTerrain(), x, y, z);
                        if (rm != null) {
                            rm.init(x, y, z);
                            rm.setOperative(true);
                            currentCell.setEntity(rm);
                        }
                    }
                }

                // Slopes de ella, la de arriba y las adyacentes
                for (short i = -1; i <= 1; i++) {
                    for (short j = -1; j <= 1; j++) {
                        for (short k = 0; k <= 1; k++) {
                            Terrain.checkSlope(World.getCells(), (short) (x + i), (short) (y + j), (short) (z + k));
                            Game.getWorld().addFluidCellToProcess(x + i, y + j, z + k, false);
                        }
                    }
                }
                Game.getWorld().addFluidCellToProcess(x, y, z - 1, false);
                Game.getWorld().addFluidCellToProcess(x, y, z + 1, false);

                // Discovered a las adyacentes
                discoverNeighbours(x, y, z);
                if (z > 0) {
                    discoverNeighbours(x, y, z - 1);
                }

                // Discovered abajo si la celda está minada
                if (z < (World.MAP_DEPTH - 1)) {
                    Cell cell = World.getCell(x, y, z + 1);
                    cell.setDiscovered(true);
                    discoverNeighbours(x, y, z + 1);
                }

                if (z > 0) {
                    // Si la casilla de arriba está digged las cosas deben caer
                    cellOver.fallThings();
                }

                // Añadimos el item de la celda de arriba (si tiene) a la lista de items posibles a caer, tambien los de los lados (por si tienen glue)
                Item item;
                if (z > 0) {
                    item = cellOver.getItem();
                    if (item != null) {
                        World.addFallItem(item.getID());
                    }
                }
                if (x > 0) {
                    item = World.getCell(x - 1, y, z).getItem();
                    if (item != null) {
                        World.addFallItem(item.getID());
                    }
                }
                if (x < (World.MAP_WIDTH - 1)) {
                    item = World.getCell(x + 1, y, z).getItem();
                    if (item != null) {
                        World.addFallItem(item.getID());
                    }
                }
                if (y > 0) {
                    item = World.getCell(x, y - 1, z).getItem();
                    if (item != null) {
                        World.addFallItem(item.getID());
                    }
                }
                if (y < (World.MAP_HEIGHT - 1)) {
                    item = World.getCell(x, y + 1, z).getItem();
                    if (item != null) {
                        World.addFallItem(item.getID());
                    }
                }

                // Si no mina en la planta (floor) más baja descubrimos una nueva planta (floor)
                Game.getWorld().discoverFloor(z);

                if (z > 0) {
                    // Si ha diggeado en una stockpile le quitamos el flag
                    Stockpile.deleteStockpilePoint(x, y, (short) (z - 1));

                    // Si ha diggeado en una zone le quitamos el flag
                    Zone.deleteZonePoint(x, y, z - 1);
                }

                // Minimap reload
                MiniMapPanel.setMinimapReload(z);
                MiniMapPanel.setMinimapReload(z - 1);

                if (bMineLadder) {
                    // Antes de minar miro si es un mine a ladder y pillo el ItemManagerItem, ya que luego quizá la celda se ha convertido en AIR debido a otras tareas o lo que sea
                    ItemManagerItem imiLadder = null;
                    // Bingo, creamos el item si se puede
                    String sLadderItem = TerrainManager.getItemByID(currentCell.getTerrain().getTerrainID()).getLadderItem();
                    if (sLadderItem != null) {
                        imiLadder = ItemManager.getItem(sLadderItem);

                        if (Item.isCellAvailableForItem(imiLadder, x, y, z, true, true)) {
                            Item itemLadder = Item.createItem(imiLadder);
                            itemLadder.setOperative(true);
                            itemLadder.setLocked(true);
                            itemLadder.init(x, y, z);
                            currentCell.setEntity(itemLadder);
                        }
                    }
                }

                // Cambiamos el tipo a AIR (al final por si el mine suelta un drop o por si metemos un ladder)
                setTerrainID(TerrainManagerItem.TERRAIN_AIR_ID);
                setTerrainTileID(TerrainManagerItem.TERRAIN_AIR_ID * TerrainManager.SLOPES_INIHEADER.length);

                // Accesing points para las tareas de MINE pueden haber cambiado
                Game.getWorld().getTaskManager().setReCheckMinePlaces(true);

                // ShouldPaintUnders
                Cell.setShouldPaintUnders(World.getCells(), x, y, z);

                // Shadows
                Cell.generateFullShadows(x, y, z);

                // Light
                Cell.generateLightsItemRemovedCellMined(x, y, z, ItemManagerItem.MAX_LIGHT_RADIUS);

                // Open cell
                Cell.generateOpen(World.getCells(), x, y);

                // ShouldPaintUnders 2 (potser pot anar a dalt, per si acàs no li poso :D )
                if (z < (World.MAP_DEPTH - 1)) {
                    Cell.setShouldPaintUnders(World.getCells(), x, y, (short) (z + 1));
                }

                // ASZID
                if (z > 0) {
                    cellOver.setAstarZoneID(-1);
                    //if (!Cell.splitTrickZoneID (x, y, z - 1)) {
                    World.setRecheckASZID(true);
                    //}
                }
                Cell.mergeZoneID(x, y, z, false);
            }
        }
    }

    public static void discoverNeighbours(int x, int y, int z) {
        if (x > 0) {
            World.getCell(x - 1, y, z).setDiscovered(true);
            if (y > 0) {
                World.getCell(x - 1, y - 1, z).setDiscovered(true);
            }
            if (y < (World.MAP_HEIGHT - 1)) {
                World.getCell(x - 1, y + 1, z).setDiscovered(true);
            }
        }

        if (y > 0) {
            World.getCell(x, y - 1, z).setDiscovered(true);
        }

        if (y < (World.MAP_HEIGHT - 1)) {
            World.getCell(x, y + 1, z).setDiscovered(true);
        }

        if (x < (World.MAP_WIDTH - 1)) {
            World.getCell(x + 1, y, z).setDiscovered(true);
            if (y > 0) {
                World.getCell(x + 1, y - 1, z).setDiscovered(true);
            }
            if (y < (World.MAP_HEIGHT - 1)) {
                World.getCell(x + 1, y + 1, z).setDiscovered(true);
            }
        }
    }

    public int getFluidType() {
        return fluidType;
    }

    public void setFluidType(int fluidType) {
        this.fluidType = (byte) fluidType;
    }

    public int getFluidCount() {
        return fluidCount;
    }

    /**
     * @param fluidCount
     */
    public void setFluidCount(int fluidCount) {
        this.fluidCount = (byte) fluidCount;
    }

    public boolean hasFluids() {
        return getFluidCount() > 0;
    }

    public void setTerrainID(int terrainID) {
        this.terrainID = (short) terrainID;
    }

    public int getTerrainID() {
        return terrainID;
    }

    public void setTerrainTileID(int terrainTileID) {
        this.terrainTileID = (short) terrainTileID;
    }

    public int getTerrainTileID() {
        return terrainTileID;
    }

    public static SmartMenu createMenuItems(Cell cell, SmartMenu smParent) {
        SmartMenu menuAddItem = new SmartMenu(SmartMenu.TYPE_MENU, "Add item", smParent, null, null); //$NON-NLS-1$
        HashMap<String, ItemManagerItem> allItems = ItemManager.getAllItems();
        Iterator<String> itAllItems = allItems.keySet().iterator();
        String sIniHeader;
        ItemManagerItem imi;
        ArrayList<String> alNames = new ArrayList<String>(allItems.size());
        while (itAllItems.hasNext()) {
            sIniHeader = itAllItems.next();
            imi = allItems.get(sIniHeader);

            menuAddItem.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, imi.getName() + " (" + sIniHeader + ")", null, CommandPanel.COMMAND_ADD_ITEM, sIniHeader, null, cell.getCoordinates().toPoint3D())); //$NON-NLS-1$ //$NON-NLS-2$

            if (imi.getName() == null) {
                alNames.add(Character.MIN_VALUE + "X"); //$NON-NLS-1$
            } else {
                alNames.add(imi.getName());
            }
        }

        // Sort
        ArrayList<SmartMenu> alItems = menuAddItem.getItems();
        String sAux;
        SmartMenu smAux;
        for (int s1 = 0; s1 < (alNames.size() - 1); s1++) {
            for (int s2 = (s1 + 1); s2 < alNames.size(); s2++) {
                if (alNames.get(s1).compareTo(alNames.get(s2)) > 0) {
                    sAux = alNames.get(s1);
                    alNames.set(s1, alNames.get(s2));
                    alNames.set(s2, sAux);

                    smAux = alItems.get(s1);
                    alItems.set(s1, alItems.get(s2));
                    alItems.set(s2, smAux);
                }
            }
        }

        menuAddItem.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
        menuAddItem.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Terrain.8"), smParent, CommandPanel.COMMAND_BACK, null)); //$NON-NLS-1$

        return menuAddItem;
    }

    public static SmartMenu createMenuLivings(Cell cell, SmartMenu smParent) {
        SmartMenu menuAddLiving = new SmartMenu(SmartMenu.TYPE_MENU, "Add living", smParent, null, null); //$NON-NLS-1$
        HashMap<String, LivingEntityManagerItem> allLivings = LivingEntityManager.getAllItems();
        Iterator<String> itAllLivings = allLivings.keySet().iterator();
        String sIniHeader;
        LivingEntityManagerItem lemi;
        ArrayList<String> alNames = new ArrayList<String>(allLivings.size());
        while (itAllLivings.hasNext()) {
            sIniHeader = itAllLivings.next();
            lemi = allLivings.get(sIniHeader);

            menuAddLiving.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, lemi.getName() + " (" + sIniHeader + ")", null, CommandPanel.COMMAND_ADD_LIVING, sIniHeader, null, cell.getCoordinates().toPoint3D())); //$NON-NLS-1$ //$NON-NLS-2$

            if (lemi.getName() == null) {
                alNames.add(Character.MIN_VALUE + "X"); //$NON-NLS-1$
            } else {
                alNames.add(lemi.getName());
            }
        }

        // Sort
        ArrayList<SmartMenu> alItems = menuAddLiving.getItems();
        String sAux;
        SmartMenu smAux;
        for (int s1 = 0; s1 < (alNames.size() - 1); s1++) {
            for (int s2 = (s1 + 1); s2 < alNames.size(); s2++) {
                if (alNames.get(s1).compareTo(alNames.get(s2)) > 0) {
                    sAux = alNames.get(s1);
                    alNames.set(s1, alNames.get(s2));
                    alNames.set(s2, sAux);

                    smAux = alItems.get(s1);
                    alItems.set(s1, alItems.get(s2));
                    alItems.set(s2, smAux);
                }
            }
        }

        menuAddLiving.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
        menuAddLiving.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Terrain.8"), smParent, CommandPanel.COMMAND_BACK, null)); //$NON-NLS-1$

        return menuAddLiving;
    }

    public static SmartMenu createMenuEvents(SmartMenu smParent) {
        SmartMenu menuAddEvent = new SmartMenu(SmartMenu.TYPE_MENU, "Add event", smParent, null, null); //$NON-NLS-1$
        HashMap<String, EventManagerItem> allEvents = EventManager.getAllItems();
        Iterator<String> itAllEvents = allEvents.keySet().iterator();
        String sIniHeader;
        EventManagerItem emi;
        ArrayList<String> alNames = new ArrayList<String>(allEvents.size());
        while (itAllEvents.hasNext()) {
            sIniHeader = itAllEvents.next();
            emi = allEvents.get(sIniHeader);

            menuAddEvent.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, emi.getName() + " (" + sIniHeader + ")", null, CommandPanel.COMMAND_ADD_EVENT, sIniHeader, null, null)); //$NON-NLS-1$ //$NON-NLS-2$

            if (emi.getName() == null) {
                alNames.add(Character.MIN_VALUE + "X"); //$NON-NLS-1$
            } else {
                alNames.add(emi.getName());
            }
        }

        // Sort
        ArrayList<SmartMenu> alItems = menuAddEvent.getItems();
        String sAux;
        SmartMenu smAux;
        for (int s1 = 0; s1 < (alNames.size() - 1); s1++) {
            for (int s2 = (s1 + 1); s2 < alNames.size(); s2++) {
                if (alNames.get(s1).compareTo(alNames.get(s2)) > 0) {
                    sAux = alNames.get(s1);
                    alNames.set(s1, alNames.get(s2));
                    alNames.set(s2, sAux);

                    smAux = alItems.get(s1);
                    alItems.set(s1, alItems.get(s2));
                    alItems.set(s2, smAux);
                }
            }
        }

        menuAddEvent.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
        menuAddEvent.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Terrain.8"), smParent, CommandPanel.COMMAND_BACK, null)); //$NON-NLS-1$

        return menuAddEvent;
    }

//	public static SmartMenu createMenuGods (SmartMenu smParent) {
//		SmartMenu menuChangeGodStatus = new SmartMenu (SmartMenu.TYPE_MENU, "Change god status", smParent, null, null); //$NON-NLS-1$
//		ArrayList<GodData> alGods = Game.getWorld ().getGods ();
//
//		if (alGods != null) {
//			for (int i = 0; i < alGods.size (); i++) {
//				SmartMenu smGod = new SmartMenu (SmartMenu.TYPE_MENU, alGods.get (i).getFullName (), menuChangeGodStatus, null, null);
//				smGod.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, "-5 status", null, CommandPanel.COMMAND_GOD_STATUS_LOWER_5, alGods.get (i).getGodID (), null, null)); //$NON-NLS-1$ //$NON-NLS-2$
//				smGod.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, "+5 status", null, CommandPanel.COMMAND_GOD_STATUS_RAISE_5, alGods.get (i).getGodID (), null, null)); //$NON-NLS-1$ //$NON-NLS-2$
//				smGod.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, null, null, null, null));
//				smGod.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Terrain.8"), null, CommandPanel.COMMAND_BACK, null));
//				menuChangeGodStatus.addItem (smGod);
//			}
//		}
//
//		menuChangeGodStatus.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, null, null, null, null));
//		menuChangeGodStatus.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Terrain.8"), smParent, CommandPanel.COMMAND_BACK, null)); //$NON-NLS-1$
//
//		return menuChangeGodStatus;
//	}
    /**
     * Fills a contextual menú refering citizens of a cell
     *
     * @param cell
     * @param sm
     */
    public static void fillMenu(Cell cell, SmartMenu sm) {
        // Menu add fluids
        SmartMenu menuAddFluids = new SmartMenu(SmartMenu.TYPE_MENU, Messages.getString("Terrain.4"), sm, null, null); //$NON-NLS-1$
        if (cell.getTerrain().getFluidCount() != 0) {
            menuAddFluids.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Terrain.5"), null, CommandPanel.COMMAND_TERRAIN_REMOVE_FLUID, null, null, cell.getCoordinates().toPoint3D())); //$NON-NLS-1$
            menuAddFluids.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
        }

        menuAddFluids.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Terrain.6"), null, CommandPanel.COMMAND_TERRAIN_ADD_FLUID, Integer.toString(FLUIDS_WATER), Integer.toString(FLUIDS_COUNT_MAX), cell.getCoordinates().toPoint3D())); //$NON-NLS-1$
        if (TownsProperties.DEBUG_MODE) {
            menuAddFluids.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, "Water Inf", null, CommandPanel.COMMAND_TERRAIN_ADD_FLUID, Integer.toString(FLUIDS_WATER), Integer.toString(FLUIDS_COUNT_INFINITE), cell.getCoordinates().toPoint3D())); //$NON-NLS-1$
        }
        menuAddFluids.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Terrain.7"), null, CommandPanel.COMMAND_TERRAIN_ADD_FLUID, Integer.toString(FLUIDS_LAVA), Integer.toString(FLUIDS_COUNT_MAX), cell.getCoordinates().toPoint3D())); //$NON-NLS-1$
        if (TownsProperties.DEBUG_MODE) {
            menuAddFluids.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, "Lava Inf", null, CommandPanel.COMMAND_TERRAIN_ADD_FLUID, Integer.toString(FLUIDS_LAVA), Integer.toString(FLUIDS_COUNT_INFINITE), cell.getCoordinates().toPoint3D())); //$NON-NLS-1$
        }
        menuAddFluids.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
        menuAddFluids.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Terrain.8"), sm, CommandPanel.COMMAND_BACK, null)); //$NON-NLS-1$

        sm.addItem(menuAddFluids);
        sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));

        if (TownsProperties.DEBUG_MODE) {
            // Add item / livings
            sm.addItem(createMenuItems(cell, sm));
            sm.addItem(createMenuLivings(cell, sm));
            sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));

            // Add/remove events
            sm.addItem(createMenuEvents(sm));
            sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));

//			sm.addItem (createMenuGods (sm));
//			sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, null, null, null, null));
            sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, "Digged " + cell.isDigged(), null, null, null)); //$NON-NLS-1$
            sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, "Mined " + cell.isMined(), null, null, null)); //$NON-NLS-1$

            // Menu change terrain
            SmartMenu menuChangeTerrain = new SmartMenu(SmartMenu.TYPE_MENU, "Change terrain [" + TerrainManager.getItemByID(cell.getTerrain().getTerrainID()).getIniHeader() + "]", sm, null, null); //$NON-NLS-1$ //$NON-NLS-2$

            HashMap<String, TerrainManagerItem> hmTerrains = TerrainManager.getTerrainList();
            Object[] asTerrains = hmTerrains.keySet().toArray();
            TerrainManagerItem tmi;
            for (int i = 0; i < asTerrains.length; i++) {
                tmi = hmTerrains.get(asTerrains[i]);
                if (tmi != null && tmi.getTerrainID() != cell.getTerrain().getTerrainID()) {
                    menuChangeTerrain.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, tmi.getName() + " (" + tmi.getIniHeader() + ")", null, CommandPanel.COMMAND_TERRAIN_CHANGE, tmi.getIniHeader(), null, cell.getCoordinates().toPoint3D())); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }

            menuChangeTerrain.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
            menuChangeTerrain.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, "Back", sm, CommandPanel.COMMAND_BACK, null)); //$NON-NLS-1$
            sm.addItem(menuChangeTerrain);

            if (cell.isDigged()) {
                sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, "Digged", null, null, null)); //$NON-NLS-1$
            }

            // Info current fluids
            if (cell.getTerrain().getFluidCount() != 0) {
                String sFluidType;
                if (cell.getTerrain().getFluidType() == FLUIDS_WATER) {
                    sFluidType = "Water"; //$NON-NLS-1$
                } else if (cell.getTerrain().getFluidType() == FLUIDS_LAVA) {
                    sFluidType = "Lava"; //$NON-NLS-1$
                } else {
                    sFluidType = "Unknown"; //$NON-NLS-1$
                }
                String sStrenght;
                if (cell.getTerrain().getFluidCount() > 0) {
                    sStrenght = Integer.toString(cell.getTerrain().getFluidCount());
                } else {
                    sStrenght = "Infinite"; //$NON-NLS-1$
                }
                sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, "Fluid [" + sFluidType + "] Strenght [" + sStrenght + "]", null, null, null)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }

            sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
        }
        if (!cell.getTerrain().hasFluids()) {

            // Soldados con patrol
            ArrayList<Citizen> alPatrolSoldiers = new ArrayList<Citizen>();
            ArrayList<Integer> alPatrolGroups = new ArrayList<Integer>();
            Citizen cit;
            for (int i = 0; i < World.getSoldierIDs().size(); i++) {
                cit = (Citizen) World.getLivingEntityByID(World.getSoldierIDs().get(i));
                if (cit != null) {
                    if (cit.getSoldierData().getState() == SoldierData.STATE_PATROL) {
                        alPatrolSoldiers.add(cit);
                    }
                }
            }

            for (int i = 0; i < SoldierGroups.MAX_GROUPS; i++) {
                if (Game.getWorld().getSoldierGroups().getGroup(i).getState() == SoldierGroupData.STATE_PATROL) {
                    alPatrolGroups.add(i);
                }
            }

            if (alPatrolSoldiers.size() > 0 || alPatrolGroups.size() > 0) {
                if (cell.getAstarZoneID() != -1) {
                    // Hay soldados con patrol, creamos el menú de poner punto de patrol
                    SmartMenu menuPatrols = new SmartMenu(SmartMenu.TYPE_MENU, Messages.getString("Terrain.9"), sm, null, null); //$NON-NLS-1$

                    for (int i = 0; i < alPatrolSoldiers.size(); i++) {
                        menuPatrols.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, alPatrolSoldiers.get(i).getCitizenData().getFullName(), null, CommandPanel.COMMAND_ADD_PATROL_POINT, Integer.toString(alPatrolSoldiers.get(i).getID()), null, cell.getCoordinates().toPoint3D()));
                    }
                    for (int i = 0; i < alPatrolGroups.size(); i++) {
                        menuPatrols.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Game.getWorld().getSoldierGroups().getGroup(alPatrolGroups.get(i)).getName(), null, CommandPanel.COMMAND_ADD_PATROL_POINT_GROUP, Integer.toString(alPatrolGroups.get(i)), null, cell.getCoordinates().toPoint3D()));
                    }

                    menuPatrols.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
                    menuPatrols.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Terrain.8"), sm, CommandPanel.COMMAND_BACK, null)); //$NON-NLS-1$
                    sm.addItem(menuPatrols);
                }
            }

            // Ahora los remove patrol points
            if (cell.isFlagPatrol()) {
                // Miramos si hay más de 1 aldeano con ese punto, para crear un menú lista (también miramos los grupos)
                SmartMenu menuPatrols = new SmartMenu(SmartMenu.TYPE_MENU, Messages.getString("Terrain.10"), sm, null, null); //$NON-NLS-1$

                for (int i = 0; i < alPatrolSoldiers.size(); i++) {
                    if (alPatrolSoldiers.get(i).getSoldierData().getPatrolPoints().contains(cell.getCoordinates())) {
                        menuPatrols.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, alPatrolSoldiers.get(i).getCitizenData().getFullName(), null, CommandPanel.COMMAND_REMOVE_PATROL_POINT, Integer.toString(alPatrolSoldiers.get(i).getID()), null, cell.getCoordinates().toPoint3D()));
                    }
                }
                SoldierGroupData sgd;
                for (int i = 0; i < alPatrolGroups.size(); i++) {
                    sgd = Game.getWorld().getSoldierGroups().getGroup(alPatrolGroups.get(i));
                    if (sgd.getPatrolPoints().contains(cell.getCoordinates())) {
                        menuPatrols.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, sgd.getName(), null, CommandPanel.COMMAND_REMOVE_PATROL_POINT_GROUP, Integer.toString(alPatrolGroups.get(i)), null, cell.getCoordinates().toPoint3D()));
                    }
                }

                menuPatrols.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
                menuPatrols.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Terrain.8"), sm, CommandPanel.COMMAND_BACK, null)); //$NON-NLS-1$
                sm.addItem(menuPatrols);
            }

            if (cell.hasItem()) {
                Item item = (Item) cell.getEntity();
                if (item != null && item.isOperative()) {
                    ItemManagerItem imi = ItemManager.getItem(item.getIniHeader());
                    if (imi.isWall()) {
                        return;
                    }
                }
            }
            if (cell.hasBuilding()) {
                Building building = Building.getBuilding(cell.getCoordinates());
                if (building != null) {
                    return;
                }
            }
            if (cell.hasZone()) {
                return;
            }

            // Mine
            TerrainManagerItem tmi = TerrainManager.getItemByID(cell.getTerrain().getTerrainID());
            if (!cell.isMined()) {
                sm.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Task.3"), null, CommandPanel.COMMAND_MINE, Boolean.FALSE.toString(), null, cell.getCoordinates().toPoint3D())); //$NON-NLS-1$
                if (tmi != null && tmi.getLadderItem() != null) {
                    sm.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Task.41"), null, CommandPanel.COMMAND_MINE_LADDER, Boolean.FALSE.toString(), null, cell.getCoordinates().toPoint3D())); //$NON-NLS-1$
                }
                sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
            }

            // Dig
            if (Cell.isDiggable(cell)) {
                sm.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Task.2"), null, CommandPanel.COMMAND_MINE, null, null, new Point3D(cell.getCoordinates().x, cell.getCoordinates().y, cell.getCoordinates().z + 1))); //$NON-NLS-1$
                TerrainManagerItem tmiUnder = TerrainManager.getItemByID(World.getCell(cell.getCoordinates().x, cell.getCoordinates().y, cell.getCoordinates().z + 1).getTerrain().getTerrainID());
                if (tmiUnder != null && tmiUnder.getLadderItem() != null) {
                    sm.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Task.40"), null, CommandPanel.COMMAND_MINE_LADDER, null, null, new Point3D(cell.getCoordinates().x, cell.getCoordinates().y, cell.getCoordinates().z + 1))); //$NON-NLS-1$
                }
                sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
            }

            // Actions
//			boolean bActions = false;
//			if (tmi.hasActions ()) {
//				ActionManagerItem ami;
//				for (int i = 0; i < tmi.getActions ().size (); i++) {
//					ami = ActionManager.getItem (tmi.getActions ().get (i));
//					sm.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, ami.getName () + " " + tmi.getName ().toLowerCase (), null, CommandPanel.COMMAND_CUSTOM_ACTION, ami.getId (), null, cell.getCoordinates ())); //$NON-NLS-1$
//				}
//			}
            if (cell.isMined() && cell.getCoordinates().z < (World.MAP_DEPTH - 1)) {
                Point3DShort p3d = cell.getCoordinates();
                // Si está minado buscamos acciones de la celda de abajo
                Cell cellUnder = World.getCell(p3d.x, p3d.y, p3d.z + 1);
                TerrainManagerItem tmiUnder = TerrainManager.getItemByID(cellUnder.getTerrain().getTerrainID());
                if (tmiUnder.hasActions()) {
                    ActionManagerItem ami;
                    for (int i = 0; i < tmiUnder.getActions().size(); i++) {
                        ami = ActionManager.getItem(tmiUnder.getActions().get(i));
                        sm.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, ami.getName() + " " + tmiUnder.getName().toLowerCase(), null, CommandPanel.COMMAND_CUSTOM_ACTION, ami.getId(), null, cell.getCoordinates().toPoint3D())); //$NON-NLS-1$
                    }
                    sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
                }
            }
//			if (bActions) {
//				sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, null, null, null, null));
//			}
        }
    }

    public static Item createDrop(Terrain terrain, int x, int y, int z) {
        TerrainManagerItem tmi = TerrainManager.getItemByID(terrain.getTerrainID());
        if (tmi != null && tmi.getDrop() != null) {
            if (Utils.getRandomBetween(1, 100) <= tmi.getDropPCT()) {
                ItemManagerItem imi = ItemManager.getItem(tmi.getDrop());
//				if (Item.isCellAvailableForItem (imi, x, y, z, false, true)) {
                return Item.createItem(imi);
//				}
            }
        }

        return null;
    }

    /**
     * Indica si en la casilla pasada se puede ir hacia arriba
     *
     * @param p3d
     * @return
     */
    public static boolean canGoUp(Point3DShort p3d) {
        return canGoUp(p3d.x, p3d.y, p3d.z);
    }

    /**
     * Indica si en la casilla pasada se puede ir hacia arriba
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static boolean canGoUp(int x, int y, int z) {
        if (z <= 0) {
            return false;
        }

		// Podrá ir hacia arriba si aquí hay una escalera operativa y arriba está digado
        // (ESTO YA NO APLICA) También puede ir hacia arriba si arriba hay una escalera operativa esta digado y aquí está minado
        // Up
        Cell cell = World.getCell(x, y, z - 1);
        if (!cell.isDiscovered() || !cell.isDigged()) {
            return false;
        }
        // Cell
        cell = World.getCell(x, y, z);
        if (!cell.isDiscovered()) { /* || !cell.getTerrain ().isMined ()) {  --- No hace falta mirar el mined pq ya se ha mirado digged arriba*/

            return false;
        }

        // Se cumplen los prerequisitos, sólo nos falta mirar que aquí o arriba haya escalera (SÓLO AQUÍ, ARRIBA YA NO APLICA)
        Item item = cell.getItem();
        if (item != null && item.isOperative() && item.isLocked()) {
            if (ItemManager.getItem(item.getIniHeader()).isZoneMergerUp()) {
                return true;
            }
        }

		// item = cellUp.getItem ();
        // if (item != null && item.isOperative () && item.isLocked ()) {
        // if (ItemManager.getItem (item.getIniHeader ()).isZoneMergerUpDown ()) {
        // return true;
        // }
        // }
        return false;
    }

    /**
     * Indica si en la casilla pasada se puede ir hacia abajo
     *
     * @param p3d
     * @return
     */
    public static boolean canGoDown(Point3DShort p3d) {
        return canGoDown(p3d.x, p3d.y, p3d.z);
    }

    /**
     * Indica si en la casilla pasada se puede ir hacia abajo
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static boolean canGoDown(int x, int y, int z) {
        if (z >= (World.MAP_DEPTH - 1)) {
            return false;
        }

		// Podrá ir hacia abajo si aquí hay una escalera operativa, está digado y abajo está minado (ESTO YA NO APLICA)
        // También puede ir hacia abajo si abajo hay una escalera operativa esta minado (se presupone) y aquí está digado
        // Cell
        Cell cell = World.getCell(x, y, z);
        if (!cell.isDiscovered() || !cell.isDigged()) {
            return false;
        }
        // Down
        cell = World.getCell(x, y, z + 1);
        if (!cell.isDiscovered()) { // || !cell.getTerrain ().isMined ()) { --- no hace falta mirar mined pq se ha mirado "digged" arriba
            return false;
        }

		// Se cumplen los prerequisitos, sólo nos falta mirar que aquí o abajo haya escalera (SÓLO ABAJO)
        // Item item = cell.getItem ();
        // if (item != null && item.isOperative () && item.isLocked ()) {
        // if (ItemManager.getItem (item.getIniHeader ()).isZoneMergerUpDown ()) {
        // return true;
        // }
        // }
        Item item = cell.getItem();
        if (item != null && item.isOperative() && item.isLocked()) {
            if (ItemManager.getItem(cell.getEntity().getIniHeader()).isZoneMergerUp()) {
                return true;
            }
        }

        return false;
    }

    public static void checkSlope(Cell[][][] cells, short x, short y, short z) {
        if (!Utils.isInsideMap(x, y, z)) {
            return;
        }

        StringBuffer buffer;
        Cell cellCurrent = cells[x][y][z];
        if (!cellCurrent.isMined()) {
            Cell cell;
            // Miramos que gráfico usar
            buffer = new StringBuffer("_"); //$NON-NLS-1$
            if (y > 0) {
                cell = cells[x][y - 1][z];
                if (!cell.isDiscovered() || !cell.isMined()) {
                    buffer.append('N');
                }
            } else {
                buffer.append('N');
            }
            if (y < (World.MAP_HEIGHT - 1)) {
                cell = cells[x][y + 1][z];
                if (!cell.isDiscovered() || !cell.isMined()) {
                    buffer.append('S');
                }
            } else {
                buffer.append('S');
            }
            if (x < (World.MAP_WIDTH - 1)) {
                cell = cells[x + 1][y][z];
                if (!cell.isDiscovered() || !cell.isMined()) {
                    buffer.append('E');
                }
            } else {
                buffer.append('E');
            }
            if (x > 0) {
                cell = cells[x - 1][y][z];
                if (!cell.isDiscovered() || !cell.isMined()) {
                    buffer.append('W');
                }
            } else {
                buffer.append('W');
            }

            if (buffer.length() == 5) {
				// Interiors
                // Miramos donde hay "huecos" en las diagonales
                // Noroeste (N)
                if (x > 0 && y > 0) {
                    cell = cells[x - 1][y - 1][z];
                    if (cell.isDiscovered() && cell.isMined()) {
                        buffer.setCharAt(1, 'n');
                    }
                }
                // Sureste (S)
                if (x < (World.MAP_WIDTH - 1) && y < (World.MAP_HEIGHT - 1)) {
                    cell = cells[x + 1][y + 1][z];
                    if (cell.isDiscovered() && cell.isMined()) {
                        buffer.setCharAt(2, 's');
                    }
                }
                // Noreste (E)
                if (x < (World.MAP_WIDTH - 1) && y > 0) {
                    cell = cells[x + 1][y - 1][z];
                    if (cell.isDiscovered() && cell.isMined()) {
                        buffer.setCharAt(3, 'e');
                    }
                }
                // Suroeste (W)
                if (x > 0 && y < (World.MAP_HEIGHT - 1)) {
                    cell = cells[x - 1][y + 1][z];
                    if (cell.isDiscovered() && cell.isMined()) {
                        buffer.setCharAt(4, 'w');
                    }
                }
            }

            if (buffer != null) {
                int iIndex = 0;
                String sIniHeader = buffer.toString();
                for (int i = 1; i < TerrainManager.SLOPES_INIHEADER.length; i++) {
                    if (sIniHeader.equals(TerrainManager.SLOPES_INIHEADER[i])) {
                        iIndex = i;
                        break;
                    }
                }
                if (iIndex != 0) {
                    cellCurrent.getTerrain().setTerrainTileID(cellCurrent.getTerrain().getTerrainID() * TerrainManager.SLOPES_INIHEADER.length + iIndex);
                    //terrain.setTerrainTileID (terrain.getTerrainTileID () + iIndex);
                }
            }
        }
    }

    /**
     * Cambia los gráficos de todas las celdas según las adyacentes (slopes
     * outside)
     *
     * @param cells
     */
    public static void changeSlopes(Cell[][][] cells) {
        for (short x = 0; x < World.MAP_WIDTH; x++) {
            for (short y = 0; y < World.MAP_HEIGHT; y++) {
                for (short z = 1; z < World.MAP_DEPTH; z++) {
                    checkSlope(cells, x, y, z);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        mineTurns = in.readShort();
        fluidType = (byte) in.readShort();
        fluidCount = (byte) in.readShort();
        terrainID = in.readShort();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeShort(mineTurns);
        out.writeShort(fluidType);
        out.writeShort(fluidCount);
        out.writeShort(terrainID);
    }
}
