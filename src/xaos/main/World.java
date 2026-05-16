package xaos.main;

import java.awt.Point;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import xaos.platform.lwjgl3.input.Keyboard;
import xaos.platform.lwjgl3.opengl.Display;
import org.lwjgl.opengl.GL11;

import xaos.TownsProperties;
import xaos.campaign.TutorialTrigger;
import xaos.caravans.CaravanManager;
import xaos.caravans.CaravanManagerItem;
import xaos.data.BuryData;
import xaos.data.CaravanData;
import xaos.data.CitizenGroups;
import xaos.data.EventData;
import xaos.data.GlobalEventData;
import xaos.data.HeroPrerequisite;
import xaos.data.SiegeData;
import xaos.data.SoldierData;
import xaos.data.SoldierGroupData;
import xaos.data.SoldierGroups;
import xaos.dungeons.DungeonData;
import xaos.dungeons.DungeonGenerator;
import xaos.dungeons.DungeonManager;
import xaos.dungeons.MonsterData;
import xaos.events.EventManager;
import xaos.events.EventManagerItem;
import xaos.generator.ItemGenerator;
import xaos.generator.LivingEntityGenerator;
import xaos.generator.MapGenerator;
import xaos.panels.CommandPanel;
import xaos.panels.MainPanel;
import xaos.panels.MessagesPanel;
import xaos.panels.MiniMapPanel;
import xaos.panels.UI.UIPanel;
import xaos.panels.UI.UIPanelState;
import xaos.panels.menus.SmartMenu;
import xaos.stockpiles.Stockpile;
import xaos.tasks.Task;
import xaos.tasks.TaskManager;
import xaos.tiles.Cell;
import xaos.tiles.Tile;
import xaos.tiles.entities.buildings.Building;
import xaos.tiles.entities.items.Container;
import xaos.tiles.entities.items.Item;
import xaos.tiles.entities.items.ItemManager;
import xaos.tiles.entities.items.ItemManagerItem;
import xaos.tiles.entities.living.Citizen;
import xaos.tiles.entities.living.LivingEntity;
import xaos.tiles.entities.living.LivingEntityManager;
import xaos.tiles.entities.living.LivingEntityManagerItem;
import xaos.tiles.entities.living.allies.Ally;
import xaos.tiles.entities.living.enemies.Enemy;
import xaos.tiles.entities.living.friendly.Friendly;
import xaos.tiles.entities.living.heroes.Hero;
import xaos.tiles.entities.living.heroes.HeroManager;
import xaos.tiles.entities.living.heroes.HeroTask;
import xaos.tiles.entities.living.projectiles.Projectile;
import xaos.tiles.entities.special.CitEating;
import xaos.tiles.entities.special.CitExclamation;
import xaos.tiles.entities.special.CitSleeping;
import xaos.tiles.entities.special.RedCross;
import xaos.tiles.terrain.Terrain;
import xaos.tiles.terrain.special.Lava;
import xaos.tiles.terrain.special.MouseCursor;
import xaos.tiles.terrain.special.MouseCursorBAD;
import xaos.tiles.terrain.special.Orders;
import xaos.tiles.terrain.special.StockpileTile;
import xaos.tiles.terrain.special.Water;
import xaos.utils.AStarQueue;
import xaos.utils.ColorGL;
import xaos.utils.Date;
import xaos.utils.Log;
import xaos.utils.Messages;
import xaos.utils.Names;
import xaos.utils.Point3D;
import xaos.utils.Point3DShort;
import xaos.utils.UtilFont;
import xaos.utils.Utils;
import xaos.utils.UtilsAL;
import xaos.utils.UtilsGL;
import xaos.utils.UtilsIniHeaders;
import xaos.utils.UtilsKeyboard;
import xaos.zones.Zone;
import xaos.zones.ZoneHeroRoom;
import xaos.zones.ZoneManager;
import xaos.zones.ZoneManagerItem;
import xaos.zones.ZonePersonal;
import xaos.panels.UI.UIPanelInputHandler;
import static xaos.panels.UI.UIPanelState.*;
import static xaos.panels.UI.UIPanelInputHandler.*;
import static xaos.panels.UI.UIPanel.*;


public final class World implements Externalizable {

	private static final long serialVersionUID = -6414576448033818136L;

	public final static int FLUIDS_MOVED_PER_INVOCATION = 64; // Max fluidos a procesar por turno (también se usa para la evaporación)
	public final static int FLUIDS_NOT_MOVED_PER_INVOCATION = 64;
	public final static int FLUIDS_MAX_EVAPORATION = 48;

	public static int SPEED = 3;
	public static int SPEED_MAX = 5;
	public static int FRAMES_PER_TURN;

	public static final short MAX_DEPTH = 64;
	public static final short MAP_WIDTH = 200;
	public static final short MAP_HEIGHT = 200;
	public static short MAP_DEPTH;
	public static short MAP_NUM_LEVELS_OUTSIDE;
	public static short MAP_NUM_LEVELS_UNDERGROUND;

	public static final int TIME_MODIFIER_HALF_MINUTE = 1;
	public static final int TIME_MODIFIER_MINUTE = 2 * TIME_MODIFIER_HALF_MINUTE;
	public static final int TIME_MODIFIER_HOUR = 60 * TIME_MODIFIER_MINUTE;
	public static final int TIME_MODIFIER_DAY = 24 * TIME_MODIFIER_HOUR;
	public static final int TIME_MODIFIER_MONTH = 30 * TIME_MODIFIER_DAY;
	public static final int TIME_MODIFIER_YEAR = 12 * TIME_MODIFIER_MONTH;

	public static Cell[][][] cells;
	private static ArrayList<Integer> citizenIDs = new ArrayList<Integer> ();
	private CitizenGroups citizenGroups = new CitizenGroups ();
	private static ArrayList<Integer> soldierIDs = new ArrayList<Integer> ();
	private SoldierGroups soldierGroups = new SoldierGroups ();
	private static ArrayList<Integer> heroIDs = new ArrayList<Integer> (2);
	private ArrayList<Point3DShort> exploringHotPoints = new ArrayList<Point3DShort> ();
	private int maxHeroXP = 0;
	private ArrayList<LivingEntity> oldHeroes = new ArrayList<LivingEntity> (2);
	private ArrayList<String> oldHeroesDied = new ArrayList<String> (2);
	private CaravanData currentCaravanData;
	private HashMap<String, Integer> enemiesKilled;
	private ArrayList<Projectile> projectiles;
	private static ArrayList<Building> buildings;
	private static HashMap<Integer, Item> items;
	private static HashMap<Integer, ArrayList<String>> itemsText;
	private ArrayList<Integer> itemsToBeHauled = new ArrayList<Integer> ();
	private static ArrayList<Integer> fallItemList = new ArrayList<Integer> ();
	private static HashMap<Integer, LivingEntity> livingsDiscovered = new HashMap<Integer, LivingEntity> ();
	private static HashMap<Integer, LivingEntity> livingsUndiscovered = new HashMap<Integer, LivingEntity> ();
	private ArrayList<Container> containers;
	private ArrayList<Stockpile> stockpiles;
	private ArrayList<BuryData> buryData;
	private ArrayList<Zone> zones;
	private GlobalEventData globalEvents;
	private ArrayList<EventData> events;
	// private ArrayList<GodData> gods;
	private int coins;
	private static String sCoins;
	private static int townValue;
	private static int happinessAverage;
	private static String sHappinessAverage;
	private int restrictHaulEquippingLevel;
	private int restrictExploringLevel;

	private TaskManager taskManager;

	// ID Máximo (current) de los entities (livingentities, items, edificios, special)
	private static int maxEntityID;

	private int turn;
	private int currentAutosaveDays;
	private Date date;

	// Special tiles
	private static MouseCursor tileMouseCursor;
	private static MouseCursorBAD tileMouseCursorBAD;
	private static Tile tileUnknown;
	private static Tile tileUnknownMini;
	private static Water tileWater;
	private static Lava tileLava;
	private static Orders tileOrders;
	private static RedCross tileRedCross;
	private static StockpileTile tileStockpile;
	private static Tile tilePatrolMark;

	// Special tiles (citizens)
	private static CitEating tileCitizenEating;
	private static CitSleeping tileCitizenSleeping;
	private static CitExclamation tileCitizenExclamation;

	private Point3D view = new Point3D (MAP_WIDTH / 2, MAP_HEIGHT / 2, 0);

	private static boolean recheckASZID; // Sirve para saber cuando recheckear los zone ID
	private static int recheckASZIDCounter; // Sirve para saber cuando recheckear los zone ID

	private transient int indexViewCitizen; // Indica el número de aldeano en la lista global de aldeanos que se está "viendo"
	private transient int indexViewSoldier; // Indica el número de soldado en la lista global de aldeanos que se está "viendo"
	private transient int indexViewHero; // Indica el número de héroe en la lista global de héroes que se está "viendo"

	private int numFloorsDiscovered = 1;

	private boolean readyForNextTurn = false;
	private int readyForNextTurnFrameCounter = 0;
	private boolean readyForNextTurnTasks = false;

	private ArrayList<Point3DShort> fluidCellsToProcess; // Fluidos a chequear
	private transient boolean fluidsMoved;
	private transient int fluidMovedCounter;
	public static ArrayList<Point3DShort> fluidEvaporation = new ArrayList<Point3DShort> (); // Fluidos con fuerza 1 que van a evaporarse cada X tiempo

	// IDs de campaña y misión
	private String campaignID;
	private String missionID;

	private transient static int maxDemoDays;


	public World () {
	}


	public World (String sCampaignID, String sMissionID) {
		setCampaignID (sCampaignID);
		setMissionID (sMissionID);

		// Loading tilesets
		Game.getPanelMainMenu ().setLoadingText (Messages.getString ("World.13")); //$NON-NLS-1$

		// Generate random map
		cells = MapGenerator.generateMap (sCampaignID, sMissionID);
	}


	public void generateAll (String sCampaignID, String sMissionID) {
		String sLog = null;
		long lTime;

		if (TownsProperties.DEBUG_MODE) {
			sLog = Messages.getString ("World.14") + " ("; //$NON-NLS-1$ //$NON-NLS-2$
			lTime = System.currentTimeMillis ();
		}
		Game.getPanelMainMenu ().setLoadingText (Messages.getString ("World.14")); //$NON-NLS-1$

		DungeonGenerator.generateDungeons (sCampaignID, sMissionID);

		if (TownsProperties.DEBUG_MODE) {
			sLog += (System.currentTimeMillis () - lTime) + "ms)"; //$NON-NLS-1$
			Log.log (Log.LEVEL_DEBUG, sLog, getClass ().toString ());
			sLog = Messages.getString ("World.21") + " ("; //$NON-NLS-1$ //$NON-NLS-2$
			lTime = System.currentTimeMillis ();
		}
		Game.getPanelMainMenu ().setLoadingText (Messages.getString ("World.21")); //$NON-NLS-1$

		ItemGenerator.generateItems (cells, sCampaignID, sMissionID);

		// Generate citizens, friendlies, enemies & projectiles
		if (TownsProperties.DEBUG_MODE) {
			sLog += (System.currentTimeMillis () - lTime) + "ms)"; //$NON-NLS-1$
			Log.log (Log.LEVEL_DEBUG, sLog, getClass ().toString ());
			sLog = Messages.getString ("World.8") + " ("; //$NON-NLS-1$ //$NON-NLS-2$
			lTime = System.currentTimeMillis ();
		}
		Game.getPanelMainMenu ().setLoadingText (Messages.getString ("World.8")); //$NON-NLS-1$

		generateCitizens ();
		if (TownsProperties.DEBUG_MODE) {
			sLog += (System.currentTimeMillis () - lTime) + "ms)"; //$NON-NLS-1$
			Log.log (Log.LEVEL_DEBUG, sLog, getClass ().toString ());
			sLog = Messages.getString ("World.9") + " ("; //$NON-NLS-1$ //$NON-NLS-2$
			lTime = System.currentTimeMillis ();
		}
		// Por cada citizen aumentamos las world coins en 1d100
		setCoins (Utils.launchDice (MapGenerator.NUM_CITIZENS, 10));
		setTownValue (0);

		Game.getPanelMainMenu ().setLoadingText (Messages.getString ("World.9")); //$NON-NLS-1$

		generateHeroes ();
		if (TownsProperties.DEBUG_MODE) {
			sLog += (System.currentTimeMillis () - lTime) + "ms)"; //$NON-NLS-1$
			Log.log (Log.LEVEL_DEBUG, sLog, getClass ().toString ());
			sLog = Messages.getString ("World.10") + " ("; //$NON-NLS-1$ //$NON-NLS-2$
			lTime = System.currentTimeMillis ();
		}
		Game.getPanelMainMenu ().setLoadingText (Messages.getString ("World.10")); //$NON-NLS-1$

		generateDungeons (sCampaignID, sMissionID);
		if (TownsProperties.DEBUG_MODE) {
			sLog += (System.currentTimeMillis () - lTime) + "ms)"; //$NON-NLS-1$
			Log.log (Log.LEVEL_DEBUG, sLog, getClass ().toString ());
			sLog = Messages.getString ("World.11") + " ("; //$NON-NLS-1$ //$NON-NLS-2$
			lTime = System.currentTimeMillis ();
		}

		Game.getPanelMainMenu ().setLoadingText (Messages.getString ("World.11")); //$NON-NLS-1$
		LivingEntityGenerator.generateLivingEntities (cells, sCampaignID, sMissionID);
		if (TownsProperties.DEBUG_MODE) {
			sLog += (System.currentTimeMillis () - lTime) + "ms)"; //$NON-NLS-1$
			Log.log (Log.LEVEL_DEBUG, sLog, getClass ().toString ());
		}

		projectiles = new ArrayList<Projectile> ();
		generateLocations ();

		// Building list
		buildings = new ArrayList<Building> ();

		// Item list
		items = new HashMap<Integer, Item> ();
		itemsText = new HashMap<Integer, ArrayList<String>> ();
		itemsToBeHauled = new ArrayList<Integer> ();
		fallItemList.clear ();

		// Containers
		containers = new ArrayList<Container> ();

		// Stockpiles
		stockpiles = new ArrayList<Stockpile> ();

		// Bury Data
		buryData = new ArrayList<BuryData> ();

		// Zones
		zones = new ArrayList<Zone> ();

		// Events
		globalEvents = new GlobalEventData ();
		events = new ArrayList<EventData> ();

		// Gods
		// gods = new ArrayList<GodData> ();
		// if (Game.GODS_ACTIVATED) {
		// for (int i = 0; i < MapGenerator.NUM_GODS; i++) {
		// addGod ();
		// }
		// }
		setNumFloorsDiscovered (World.MAP_NUM_LEVELS_OUTSIDE + 1);

		// Date
		date = new Date ();

		// Fluid check list
		if (TownsProperties.DEBUG_MODE) {
			sLog = Messages.getString ("World.12") + " ("; //$NON-NLS-1$ //$NON-NLS-2$
			lTime = System.currentTimeMillis ();
		}
		fluidCellsToProcess = getInitialFluidCheckPoints ();
		if (TownsProperties.DEBUG_MODE) {
			sLog += (System.currentTimeMillis () - lTime) + "ms)"; //$NON-NLS-1$
			Log.log (Log.LEVEL_DEBUG, sLog, getClass ().toString ());
		}
		fluidMovedCounter = 0;
		fluidsMoved = false;

		// Create task manager
		taskManager = new TaskManager ();

		loadSpecialTiles ();

		// Exploring hotpoints
		exploringHotPoints = new ArrayList<Point3DShort> ();
		maxHeroXP = 0;

		// Restrict
		setRestrictHaulEquippingLevel (MAP_DEPTH - 1);
		setRestrictExploringLevel (MAP_DEPTH - 1);

		// Mensajes
		MessagesPanel.initialize ();

		// Evaporation
		Point3DShort p3ds;
		while (World.fluidEvaporation.size () > 0) {
			p3ds = World.fluidEvaporation.remove (World.fluidEvaporation.size () - 1);
			Point3DShort.returnToPool (p3ds);
		}

		setTurnsPerSecond ();
	}


	public static void setTurnsPerSecond () {
		if (SPEED == 1) {
			FRAMES_PER_TURN = 7;
		} else if (SPEED == 2) {
			FRAMES_PER_TURN = 5;
		} else if (SPEED == 3) {
			FRAMES_PER_TURN = 3;
		} else if (SPEED == 4) {
			FRAMES_PER_TURN = 2;
		} else {
			FRAMES_PER_TURN = 1;
		}
	}


	public static void addTurnsPerSecond () {
		SPEED++;
		if (SPEED > SPEED_MAX) {
			SPEED = SPEED_MAX;
		}
		setTurnsPerSecond ();
	}


	public static void removeTurnsPerSecond () {
		SPEED--;
		if (SPEED < 1) {
			SPEED = 1;
		}
		setTurnsPerSecond ();
	}


	public void moveFluidsPreNewGame () {
		fluidMovedCounter = 0;
		fluidsMoved = false;
		int iCounter = fluidCellsToProcess.size ();
		while (iCounter > 0 && fluidCellsToProcess.size () > 0) {
			iCounter -= FLUIDS_NOT_MOVED_PER_INVOCATION;
			moveFluids (true);
		}
		Cell.setAllZoneIDs ();
	}


	public void refreshTransients () {
		// Refresh transients de todas las celdas (con su contenido)
		for (short z = 0; z < MAP_DEPTH; z++) { // Importante poner la Z aquí para que el iMaxDiscovered no se lie
			for (short x = 0; x < MAP_WIDTH; x++) {
				for (short y = 0; y < MAP_HEIGHT; y++) {
					cells[x][y][z].refreshTransients (x, y, z);
					checkNewEvaporation (cells[x][y][z]);
				}
			}
		}

		// Refresh transients de los proyectiles
		for (int i = 0; i < projectiles.size (); i++) {
			projectiles.get (i).refreshTransients ();
		}

		// Citizens, enemies, friendlies & projectiles
		generateLocations ();

		loadSpecialTiles ();

		// Building coordinates
		for (int i = 0; i < getBuildings ().size (); i++) {
			getBuildings ().get (i).setAllBuildingCoordinates ();
		}

		// Stockpiles
		Stockpile.updateIndexID ();

		// Zones
		Zone.updateIndexID ();

		// Tareas
		taskManager.updateTaskIndexID ();

		// Caravan
		if (getCurrentCaravanData () != null) {
			getCurrentCaravanData ().refreshTransients ();
		}

		setTurnsPerSecond ();
	}


	private ArrayList<Point3DShort> getInitialFluidCheckPoints () {
		ArrayList<Point3DShort> alPoints = new ArrayList<Point3DShort> ();

		for (short z = 0; z < MAP_DEPTH; z++) {
			for (short x = 0; x < MAP_WIDTH; x++) {
				for (short y = 0; y < MAP_HEIGHT; y++) {
					if (cells[x][y][z].getTerrain ().hasFluids ()) {
						alPoints.add (Point3DShort.getPoolInstance (x, y, z));
						cells[x][y][z].setFluidCheckList (true);
					}
				}
			}
		}

		return alPoints;
	}


	public int getTurn () {
		return turn;
	}


	public static Cell[][][] getCells () {
		return cells;
	}


	public static Cell getCell (Point3DShort p3d) {
		return getCell (p3d.x, p3d.y, p3d.z);
	}


	public static Cell getCell (Point3D p3d) {
		return getCell (p3d.x, p3d.y, p3d.z);
	}


	public static Cell getCell (int x, int y, int z) {
		return cells[x][y][z];
	}


	public static int getCurrentEntityID () {
		return maxEntityID;
	}


	public static void setCurrentEntityID (int iID) {
		maxEntityID = iID;
	}


	public static int getNextEntityID () {
		maxEntityID++;
		return maxEntityID;
	}


	public Point3D getView () {
		return view;
	}


	public void setView (Point3DShort point) {
		setView (point.x, point.y, point.z);
	}


	public void setView (Point point) {
		setView (point.x, point.y);
	}


	public void setView (int x, int y) {
		setView (x, y, view.z);
	}


	public void setView (int x, int y, int z) {
		if (z != view.z && Game.getCurrentState () == Game.STATE_CREATING_TASK) {
			if (Game.getCurrentTask ().getState () == Task.STATE_CREATING_ENDZONE) {
				Game.getCurrentTask ().setPointIni ((Point3D) null);
				Game.getCurrentTask ().setState (Task.STATE_CREATING_INIZONE);
			}
			// Game.deleteCurrentTask ();
		}

		view.setPoint (x, y, z);
	}


	// public static void setRecheckASZID (Point3D p3d) {
	// Integer iASZID = Integer.valueOf (Game.getWorld ().getCell (p3d).getAstarZoneID ());
	// if (listASZIDToCheck.contains (iASZID)) {
	// listASZIDToCheck.add (iASZID);
	// World.recheckASZID = true;
	// }
	public static void setRecheckASZID (boolean bRecheckASZID) {
		World.recheckASZID = bRecheckASZID;
		if (bRecheckASZID && World.recheckASZIDCounter <= 0) {
			World.recheckASZIDCounter = 16; // Espera unos cuantos turnos por si acaso y luego hace el setallzone...blahblah
		}
	}


	public static boolean isRecheckASZID () {
		return recheckASZID;
	}


	public boolean setPreviousIndexViewCitizen () {
		int iTemp = this.indexViewCitizen - 1;

		if (iTemp < 0) {
			iTemp = getCitizenIDs ().size () - 1;
		}
		if (iTemp >= 0 && iTemp < getCitizenIDs ().size ()) {
			if (getLivingEntityByID (getCitizenIDs ().get (iTemp)) != null) {
				this.indexViewCitizen = iTemp;
				return true;
			}
		}

		return false;
	}


	public boolean setNextIndexViewCitizen () {
		int iTemp = this.indexViewCitizen + 1;

		if (iTemp >= getCitizenIDs ().size ()) {
			iTemp = 0;
		}
		if (iTemp < getCitizenIDs ().size ()) {
			if (getLivingEntityByID (getCitizenIDs ().get (iTemp)) != null) {
				this.indexViewCitizen = iTemp;
				return true;
			}
		}

		return false;
	}


	public boolean setPreviousIndexViewSoldier () {
		int iTemp = this.indexViewSoldier - 1;

		if (iTemp < 0) {
			iTemp = getSoldierIDs ().size () - 1;
		}
		if (iTemp >= 0 && iTemp < getSoldierIDs ().size ()) {
			if (getLivingEntityByID (getSoldierIDs ().get (iTemp)) != null) {
				this.indexViewSoldier = iTemp;
				return true;
			}
		}

		return false;
	}


	public boolean setNextIndexViewSoldier () {
		int iTemp = this.indexViewSoldier + 1;

		if (iTemp >= getSoldierIDs ().size ()) {
			iTemp = 0;
		}
		if (iTemp < getSoldierIDs ().size ()) {
			if (getLivingEntityByID (getSoldierIDs ().get (iTemp)) != null) {
				this.indexViewSoldier = iTemp;
				return true;
			}
		}

		return false;
	}


	public boolean setPreviousIndexViewHero () {
		int iTemp = this.indexViewHero - 1;

		if (iTemp < 0) {
			iTemp = getHeroIDs ().size () - 1;
		}
		if (iTemp >= 0 && iTemp < getHeroIDs ().size ()) {
			if (getLivingEntityByID (getHeroIDs ().get (iTemp)) != null) {
				this.indexViewHero = iTemp;
				return true;
			}
		}

		return false;
	}


	public boolean setNextIndexViewHero () {
		int iTemp = this.indexViewHero + 1;

		if (iTemp >= getHeroIDs ().size ()) {
			iTemp = 0;
		}
		if (iTemp < getHeroIDs ().size ()) {
			if (getLivingEntityByID (getHeroIDs ().get (iTemp)) != null) {
				this.indexViewHero = iTemp;
				return true;
			}
		}

		return false;
	}


	public void setViewOnCitizen () {
		Citizen citizen;
		if (indexViewCitizen >= 0 && indexViewCitizen < getCitizenIDs ().size ()) {
			citizen = (Citizen) getLivingEntityByID (getCitizenIDs ().get (indexViewCitizen));
			setView (citizen.getCoordinates ());
		}
	}


	public void setViewOnSoldier () {
		Citizen soldier;
		if (indexViewSoldier >= 0 && indexViewSoldier < getSoldierIDs ().size ()) {
			soldier = (Citizen) getLivingEntityByID (getSoldierIDs ().get (indexViewSoldier));
			setView (soldier.getCoordinates ());
		}
	}


	public void setViewOnHero () {
		Hero hero;
		if (indexViewHero >= 0 && indexViewHero < getHeroIDs ().size ()) {
			hero = (Hero) getLivingEntityByID (getHeroIDs ().get (indexViewHero));
			setView (hero.getCoordinates ());
		}
	}


	public static ArrayList<Integer> getCitizenIDs () {
		return citizenIDs;
	}


	public static ArrayList<Integer> getSoldierIDs () {
		return soldierIDs;
	}


	public CitizenGroups getCitizenGroups () {
		return citizenGroups;
	}


	public SoldierGroups getSoldierGroups () {
		return soldierGroups;
	}


	public static ArrayList<Integer> getHeroIDs () {
		return heroIDs;
	}


	public ArrayList<LivingEntity> getOldHeroes () {
		return oldHeroes;
	}


	public ArrayList<String> getOldHeroesDied () {
		return oldHeroesDied;
	}


	public void resetOldHeroesDied () {
		if (oldHeroesDied != null) {
			oldHeroesDied.clear ();
		}
	}


	public CaravanData getCurrentCaravanData () {
		return currentCaravanData;
	}


	public void setCurrentCaravanData (CaravanData caravanData) {
		currentCaravanData = caravanData;
	}


	public static int getNumCitizens () {
		return citizenIDs.size ();
	}


	public static int getNumSoldiers () {
		return soldierIDs.size ();
	}


	public static int getNumHeroes () {
		return heroIDs.size ();
	}


	public ArrayList<Projectile> getProjectiles () {
		return projectiles;
	}


	public static ArrayList<Building> getBuildings () {
		if (buildings == null) {
			buildings = new ArrayList<Building> ();
		}

		return buildings;
	}


	public static HashMap<Integer, Item> getItems () {
		if (items == null) {
			items = new HashMap<Integer, Item> ();
		}
		return items;
	}


	public static HashMap<Integer, ArrayList<String>> getItemsText () {
		if (itemsText == null) {
			itemsText = new HashMap<Integer, ArrayList<String>> ();
		}
		return itemsText;
	}


	public static HashMap<Integer, LivingEntity> getLivings (boolean bDiscovered) {
		return (bDiscovered) ? livingsDiscovered : livingsUndiscovered;
	}


	public static LivingEntity getLivingEntityByID (int ID) {
		LivingEntity le = livingsDiscovered.get (ID);
		if (le == null) {
			return livingsUndiscovered.get (ID);
		}

		return le;
	}


	public static LivingEntity getLivingEntityByID (int ID, boolean bDiscovered) {
		if (bDiscovered) {
			return livingsDiscovered.get (ID);
		} else {
			return livingsUndiscovered.get (ID);
		}
	}


	public Container getContainer (int iItemID) {
		for (int i = 0; i < getContainers ().size (); i++) {
			if (getContainers ().get (i).getItemID () == iItemID) {
				return getContainers ().get (i);
			}
		}

		return null;
	}


	public ArrayList<Container> getContainers () {
		return containers;
	}


	public ArrayList<Stockpile> getStockpiles () {
		return stockpiles;
	}


	public ArrayList<BuryData> getBuryData () {
		return buryData;
	}


	public void addBuryData (BuryData bd) {
		if (this.buryData == null) {
			this.buryData = new ArrayList<BuryData> ();
		}

		this.buryData.add (bd);
	}


	public ArrayList<Zone> getZones () {
		return zones;
	}


	public GlobalEventData getGlobalEvents () {
		return globalEvents;
	}


	/**
	 * Mira todos los eventos y setea las opciones globales de los mismos
	 */
	public void checkGlobalEvents () {
		globalEvents.reset ();

		int iIndex = events.size () - 1;
		while (iIndex >= 0) {
			EventData eventData = events.get (iIndex);

			// Primero de todo comprobamos que aún tenga los prerequisitos
			if (containsEventPrerequisites (eventData.getEventID ())) {
				if (eventData.getEventCooldown () <= 0) { // Evento activo
					EventManagerItem emi = EventManager.getItem (eventData.getEventID ());

					if (emi != null) {
						// Shadows
						if (emi.isShadows ()) {
							globalEvents.setShadows (true);
						}

						// Half shadows
						if (emi.isHalfShadows ()) {
							globalEvents.setHalfShadows (true);
						}

						// RGB
						globalEvents.setRed (globalEvents.getRed () + emi.getRed ());
						globalEvents.setGreen (globalEvents.getGreen () + emi.getGreen ());
						globalEvents.setBlue (globalEvents.getBlue () + emi.getBlue ());

						// waitPCTs
						if (eventData.getWaitPCT () != 100) {
							globalEvents.setWaitPCT ((globalEvents.getWaitPCT () * eventData.getWaitPCT ()) / 100);
						}

						// walkSpeedPCTs
						if (eventData.getWalkSpeedPCT () != 100) {
							globalEvents.setWalkSpeedPCT ((globalEvents.getWalkSpeedPCT () * eventData.getWalkSpeedPCT ()) / 100);
						}
					}
				}
			} else {
				// No tiene los prerequisitos, borramos el evento
				deleteEvent (eventData.getEventID ());
			}

			iIndex--;
		}
	}


	public ArrayList<EventData> getEvents () {
		return events;
	}


	// public ArrayList<GodData> getGods () {
	// return gods;
	// }
	// private void addGod () {
	// // Buscamos un god a random para meter
	// // Si al cabo de X intentos no se consigue uno que no esté en la lista, se recorre a manija la lista, uno a uno
	//
	// String sGodID;
	// GodManagerItem gmi;
	// int iTries = 100; // 100 randoms
	// while (iTries > 0) {
	// gmi = GodManager.getItemAtRandom ();
	//
	// if (gmi != null) {
	// iTries--;
	// sGodID = gmi.getId ();
	//
	// // Miramos que no exista
	// boolean bExists = false;
	// for (int g = 0; g < gods.size (); g++) {
	// if (gods.get (g).getGodID ().equals (sGodID)) {
	// // Ya existía
	// bExists = true;
	// break;
	// }
	// }
	//
	// if (bExists) {
	// continue;
	// }
	//
	// // Si llega aquí ya lo tenemos
	// gods.add (gmi.getGodDataInstance ());
	// return;
	// }
	// }
	//
	// // Si llega aquí nos recorreremos la lista de gods a manija
	// HashMap<String, GodManagerItem> hmGods = GodManager.getAllItems ();
	// Iterator<String> itGods = hmGods.keySet ().iterator ();
	// while (itGods.hasNext ()) {
	// gmi = hmGods.get (itGods.next ());
	// sGodID = gmi.getId ();
	// // Miramos que no exista
	//
	// boolean bExists = false;
	// for (int g = 0; g < gods.size (); g++) {
	// if (gods.get (g).getGodID ().equals (sGodID)) {
	// // Ya existía
	// bExists = true;
	// break;
	// }
	// }
	//
	// if (bExists) {
	// continue;
	// }
	//
	// // Si llega aquí ya lo tenemos
	// gods.add (gmi.getGodDataInstance ());
	// return;
	// }
	// }
	/**
	 * Añade un evento a la lista, comprueba los prerequisitos y elimina los eventos a los que es immune
	 * 
	 * @param emi
	 */
	public boolean addEvent (EventManagerItem emi) {
		// Miramos que no exista
		for (int i = 0; i < events.size (); i++) {
			if (events.get (i).getEventID ().equals (emi.getId ())) {
				return false;
			}
		}

		// Si llega aquí es que no existe en la lista actualmente
		// Comprobamos los prerequisitos, todos deben existir
		if (!containsEventPrerequisites (emi.getId ())) {
			return false;
		}

		// Comprobamos que algún efecto no lo tenga como immune
		if (containsEventImmunization (emi.getId ())) {
			return false;
		}

		// Comprobamos los effectsImmune del nuevo efecto, para borrar los otros
		if (emi.getEventsImmune () != null) {
			for (int i = 0; i < emi.getEventsImmune ().size (); i++) {
				deleteEvent (emi.getEventsImmune ().get (i));
			}
			checkGlobalEvents ();
		}

		// Si llega aquí es que los prerequisitos están ok, creamos una instancia y la metemos
		// Miramos la posición donde debe ir
		int iIndex = -1;
		for (int i = 0; i < events.size (); i++) {
			if (events.get (i).getOrder () >= emi.getOrder ()) {
				iIndex = i;
				break;
			}
		}

		if (iIndex == -1) {
			events.add (emi.getEventDataInstance ());
		} else {
			events.add (iIndex, emi.getEventDataInstance ());
		}

		// Miramos si hay que hacer sonar un fichero de audio
		if (emi.getFxBeforeCooldown () != null) {
			UtilsAL.play (emi.getFxBeforeCooldown ());
		}

		return true;
	}


	/**
	 * Borra eventos con ese ID
	 * 
	 * @param sEvent
	 * @return
	 */
	private boolean deleteEvent (String sEvent) {
		for (int i = 0; i < events.size (); i++) {
			if (events.get (i).getEventID ().equals (sEvent)) {
				EventData ed = events.remove (i);

				// Si era un evento con items hay que restaurarlos
				EventManagerItem emi = EventManager.getItem (ed.getEventID ());
				if (emi != null) {
					if (ed.getEventCooldown () <= 0) {
						if (emi.getItems () != null && emi.getItems ().size () > 0) {
							ed.checkAllItemsMaxAgePCTs (emi, true);
						}
					}

					// Miramos si hay que hacer sonar un fichero de audio
					if (emi.getFxFinish () != null) {
						UtilsAL.play (emi.getFxFinish ());
					}
				}

				return true;
			}
		}

		return false;
	}


	/**
	 * Comprueba si un evento está en la lista de eventos
	 * 
	 * @param sEvent
	 * @return
	 */
	private boolean containsEvent (String sEvent) {
		for (int i = 0; i < events.size (); i++) {
			if (events.get (i).getEventID ().equals (sEvent)) {
				return true;
			}
		}

		return false;
	}


	/**
	 * Comprueba si un evento tiene los prerequisitos en la lista. Tambien mira el minPopulation
	 * 
	 * @param sEvent
	 * @return
	 */
	private boolean containsEventPrerequisites (String sEvent) {
		EventManagerItem emi = EventManager.getItem (sEvent);

		if (emi == null) {
			return false;
		}

		if (emi.getPrerequisites () != null) {
			for (int i = 0; i < emi.getPrerequisites ().size (); i++) {
				if (!containsEvent (emi.getPrerequisites ().get (i))) {
					return false;
				}
			}
		}

		if (emi.getMinPopulation () != 0) {
			if ((World.getNumCitizens () + World.getNumSoldiers ()) < emi.getMinPopulation ()) {
				return false;
			}
		}

		return true;
	}


	/**
	 * Comprueba si un evento puede añadire por los immunes actuales
	 * 
	 * @param sEvent
	 * @return
	 */
	private boolean containsEventImmunization (String sEvent) {
		EventManagerItem emi;
		for (int i = 0; i < events.size (); i++) {
			emi = EventManager.getItem (events.get (i).getEventID ());

			if (emi != null && emi.getEventsImmune () != null && emi.getEventsImmune ().contains (sEvent)) {
				return true;
			}
		}

		return false;
	}


	public void setCoins (int coins) {
		this.coins = coins;
		sCoins = null;
	}


	public int getCoins () {
		return coins;
	}


	public String getCoinsString () {
		if (sCoins == null) {
			sCoins = Integer.toString (coins);
		}

		return sCoins;
	}


	public void calculateHappinessAverage () {
		int iHappiness = 0;
		if (getCitizenIDs ().size () > 0) {
			for (int i = 0; i < getCitizenIDs ().size (); i++) {
				iHappiness += ((Citizen) getLivingEntityByID (getCitizenIDs ().get (i))).getCitizenData ().getHappiness ();
			}
			iHappiness /= getCitizenIDs ().size ();
		}

		setHappinessAverage (iHappiness);
	}


	public static void setHappinessAverage (int happinessAverage) {
		World.happinessAverage = happinessAverage;
		setHappinessAverageString (Integer.toString (happinessAverage));
	}


	public static int getHappinessAverage () {
		return happinessAverage;
	}


	private static void setHappinessAverageString (String sAverage) {
		sHappinessAverage = sAverage;
	}


	public static String getHappinessAverageString () {
		if (sHappinessAverage == null) {
			setHappinessAverage (getHappinessAverage ());
		}

		return sHappinessAverage;
	}


	public static void setTownValue (int townValue) {
		World.townValue = townValue;
	}


	public static int getTownValue () {
		return townValue;
	}


	public void setRestrictHaulEquippingLevel (int restrictHaulEquippingLevel) {
		this.restrictHaulEquippingLevel = restrictHaulEquippingLevel;
	}


	public int getRestrictHaulEquippingLevel () {
		return restrictHaulEquippingLevel;
	}


	public void addRestrictHaulEquippingLevel () {
		if (restrictHaulEquippingLevel < (MAP_DEPTH - 1)) {
			restrictHaulEquippingLevel++;
		}
	}


	public void substractRestrictHaulEquippingLevel () {
		if (restrictHaulEquippingLevel > 0) {
			restrictHaulEquippingLevel--;
		}
	}


	public void setRestrictExploringLevel (int restrictExploringLevel) {
		this.restrictExploringLevel = restrictExploringLevel;
	}


	public int getRestrictExploringLevel () {
		return restrictExploringLevel;
	}


	public void addRestrictExploringLevel () {
		if (restrictExploringLevel < (MAP_DEPTH - 1)) {
			restrictExploringLevel++;
		}
	}


	public void substractRestrictExploringLevel () {
		if (restrictExploringLevel > 0) {
			restrictExploringLevel--;
		}
	}


	/**
	 * Añade una stockpile a la lista y setea el ID de stockpile en todas las celdas involucradas
	 * 
	 * @param pile
	 */
	public void addStockPile (Stockpile pile) {
		stockpiles.add (pile);
		ArrayList<Point3DShort> alp3d = pile.getPoints ();
		Point3DShort p3d;
		for (int i = 0; i < alp3d.size (); i++) {
			p3d = alp3d.get (i);
			cells[p3d.x][p3d.y][p3d.z].setStockPileID (pile.getID ());
		}
	}


	/**
	 * Añade un container si no existe con anterioridad
	 * 
	 * @param iItemID
	 */
	public void addContainer (int iItemID) {
		// Miramos que no exista ya (por si lo están moviendo de sitio)
		Container container;
		for (int i = 0; i < getContainers ().size (); i++) {
			container = getContainers ().get (i);
			if (container.getItemID () == iItemID) {
				// Ya existe, quizá lo ha movido de sitio, por lo que setearemos las coordinates de los items de dentro
				Item itemContainer = getItems ().get (Integer.valueOf (iItemID));
				if (itemContainer != null) {
					for (int j = 0; j < container.getItemsInside ().size (); j++) {
						container.getItemsInside ().get (j).setCoordinates (itemContainer.getCoordinates ());
					}
				}

				return;
			}
		}

		// Nuevo container
		getContainers ().add (new Container (iItemID));
	}


	/**
	 * Borra un container y su contenido
	 * 
	 * @param iItemID
	 */
	public void deleteContainer (int iItemID) {
		Container container;
		for (int i = 0; i < getContainers ().size (); i++) {
			container = getContainers ().get (i);
			if (container.getItemID () == iItemID) {
				// Encontrado, lo borramos
				getContainers ().remove (i);

				// Borramos el contenido
				for (int n = 0; n < container.getItemsInside ().size (); n++) {
					container.getItemsInside ().get (n).delete ();
				}
				return;
			}
		}
	}


	/**
	 * Añade una zona a la lista y setea el ID de zona en todas las celdas involucradas
	 * 
	 * @param zone
	 */
	public void addZone (Zone zone, boolean expand) {
		if (!expand) {
			zones.add (zone);
		}
		ArrayList<Point3DShort> alp3d = zone.getPoints ();
		Point3DShort p3d;
		for (int i = 0; i < alp3d.size (); i++) {
			p3d = alp3d.get (i);
			cells[p3d.x][p3d.y][p3d.z].setZoneID (zone.getID ());

			if (cells[p3d.x][p3d.y][p3d.z].hasStockPile ()) {
				Stockpile.deleteStockpilePoint (p3d);
			}
		}
	}


	public TaskManager getTaskManager () {
		return taskManager;
	}


	public int getNumFloorsDiscovered () {
		return numFloorsDiscovered;
	}


	public void setNumFloorsDiscovered (int numFloorsDiscovered) {
		this.numFloorsDiscovered = numFloorsDiscovered;
	}


	public void discoverFloor (int z) {
		if (getNumFloorsDiscovered () < (z + 2)) {
			Game.getWorld ().setNumFloorsDiscovered (z + 2);

			// Hemos descubierto un nuevo nivel, ponemos como discovered las casillas con fluidos
			for (int i = 0; i < World.MAP_WIDTH; i++) {
				for (int j = 0; j < World.MAP_HEIGHT; j++) {
					if (cells[i][j][z].getTerrain ().hasFluids ()) {
						cells[i][j][z].setDiscovered (true);
					}
				}
			}
		}
	}


	/**
	 * Setea el array [MAP_WIDTH][MAP_HEIGHT][MAP_DEPTH] de ints indicando por cada celda el número de proyectiles
	 */
	public void generateLocations () {
		// Citizens
		byte[][][] locations = new byte [World.MAP_WIDTH] [World.MAP_HEIGHT] [World.MAP_DEPTH];
		Projectile projectile;
		for (int i = 0; i < projectiles.size (); i++) {
			projectile = projectiles.get (i);
			locations[projectile.getX ()][projectile.getY ()][projectile.getZ ()]++;
		}

		Projectile.setLocations (locations);
	}


	private void generateHeroes () {
		heroIDs.clear ();
		oldHeroes.clear ();
		oldHeroesDied.clear ();
		maxHeroXP = 0;
	}


	/**
	 * Genera aldeanos en un punto de partida común
	 */
	private void generateCitizens () {
		citizenIDs.clear ();
		soldierIDs.clear ();
		citizenGroups.clear ();
		soldierGroups.clear ();

		// Buscamos un punto de llegada válido (10.000 intentos)
		// int outsideIndex = 1;
		int trys = 0;
		Point3DShort p3d = null;
		// boolean bFinished = false;
		// while (!bFinished) {
		// p3d = new Point3D (Utils.getRandomBetween (0, World.MAP_WIDTH - 0), Utils.getRandomBetween (0, World.MAP_HEIGHT - 1), World.MAP_NUM_LEVELS_OUTSIDE - outsideIndex);
		if (MapGenerator.STARTING_X != -1 && MapGenerator.STARTING_Y != -1) {
			p3d = Point3DShort.getPoolInstance (MapGenerator.STARTING_X, MapGenerator.STARTING_Y, MapGenerator.STARTING_LEVEL);
		} else {
			p3d = Point3DShort.getPoolInstance ((short) Utils.getRandomBetween (0, World.MAP_WIDTH - 0), (short) Utils.getRandomBetween (0, World.MAP_HEIGHT - 1), MapGenerator.STARTING_LEVEL);
		}
		trys = 10000;
		while (!validStartingPoint (p3d) && trys > 0) {
			// p3d = new Point3D (Utils.getRandomBetween (0, World.MAP_WIDTH - 0), Utils.getRandomBetween (0, World.MAP_HEIGHT - 1), World.MAP_NUM_LEVELS_OUTSIDE - outsideIndex);
			p3d = Point3DShort.getPoolInstance ((short) Utils.getRandomBetween (0, World.MAP_WIDTH - 0), (short) Utils.getRandomBetween (0, World.MAP_HEIGHT - 1), MapGenerator.STARTING_LEVEL);
			trys--;
		}

		// if (trys == 0) {
		// outsideIndex++;
		// if ((World.MAP_NUM_LEVELS_OUTSIDE - outsideIndex) < 0) {
		// bFinished = true;
		// }
		// } else {
		// bFinished = true;
		// }
		// }
		if (trys == 0 || p3d == null) {
			// No hemos encontrado punto de inicio, no metemos aldeanos
			Log.log (Log.LEVEL_ERROR, Messages.getString ("World.4"), getClass ().toString ()); //$NON-NLS-1$
			return;
		}

		// Tenemos el punto de inicio, creamos aldeanos
		for (int i = 0; i < MapGenerator.NUM_CITIZENS; i++) {
			addNewLiving (null, LivingEntity.TYPE_CITIZEN, true, p3d.x, p3d.y, p3d.z);
		}
	}


	/**
	 * Indica si el punto pasado es adecuado para empezar la partida
	 * 
	 * @param p3d
	 * @return true si el punto pasado es adecuado para empezar la partida
	 */
	private boolean validStartingPoint (Point3DShort p3d) {
		// Miraremos que tenca una cuadrícula de 17x17 sin agua
		final int NUM_CELLS = 8;
		if (p3d.x < NUM_CELLS || p3d.y < NUM_CELLS || p3d.x > MAP_WIDTH - 1 - NUM_CELLS || p3d.y > MAP_HEIGHT - 1 - NUM_CELLS) {
			return false;
		}

		for (int x = p3d.x - NUM_CELLS; x <= p3d.x + NUM_CELLS; x++) {
			for (int y = p3d.y - NUM_CELLS; y <= p3d.y + NUM_CELLS; y++) {
				if (getCells ()[x][y][p3d.z].getTerrain ().hasFluids ()) {
					return false;
				}
			}
		}

		// También miramos si la celda es ok para ellos
		return LivingEntity.isCellAllowed (p3d);
	}


	private void generateDungeons (String scampaignID, String sMissionID) {
		// enemiesDiscovered = new HashMap<String, ArrayList<Enemy>> ();
		// enemiesUndiscovered = new HashMap<String, ArrayList<Enemy>> ();

		ArrayList<DungeonData> alDungeons = DungeonManager.getDungeons (scampaignID, sMissionID);
		for (int i = 0; i < alDungeons.size (); i++) {
//			long l = System.currentTimeMillis ();
//			System.out.print ("Dungeon: " + (i + 1));
			generateDungeons (alDungeons.get (i));
//			System.out.println ((l - System.currentTimeMillis ()) + "ms");
		}
	}


	private void generateDungeons (DungeonData dungeonData) {
		if (dungeonData.getLevel () >= MAP_DEPTH) {
			return;
		}

		short dungeonLevel = dungeonData.getLevel ();

		ArrayList<MonsterData> alMonsters = dungeonData.getMonsters ();
		MonsterData monsterData;
		LivingEntityManagerItem lemi;
		// Recorremos los monsters
		for (int i = 0; i < alMonsters.size (); i++) {
			monsterData = alMonsters.get (i);
			int iNumber = monsterData.getNumber ();

			ArrayList<LivingEntityManagerItem> alMonsterList = null;
			// Metemos a random el monster indicado tantas veces como esté indicado
	        if (monsterData.getId().equalsIgnoreCase(MonsterData.ID_RANDOM)) {
	            // Enemigo a random, obtenemos la lista por rango de nivel
	        	alMonsterList = LivingEntityManager.getItemByLevelList (monsterData.getLevelMin(), monsterData.getLevelMax(), LivingEntity.TYPE_ENEMY, false);
	        }

			for (int n = 0; n < iNumber; n++) {
				if (alMonsterList == null || alMonsterList.size () == 0) {
		            // Enemigo fijo
					lemi = LivingEntityManager.getItem (monsterData.getId ());
				} else {
		            // Enemigo a random
					lemi = alMonsterList.get (Utils.getRandomBetween(0, (alMonsterList.size() - 1)));
				}

				if (lemi == null) {
					Log.log (Log.LEVEL_ERROR, Messages.getString ("World.5") + monsterData.getId () + "]", getClass ().toString ()); //$NON-NLS-1$ //$NON-NLS-2$
					Game.exit ();
				}

				// Intentamos meterlo 100 veces a random, si no cabe recorremos el mundo casilla a casilla
				int iRandom = 100;
				short x, y;
				boolean enemigoOK = false;
				while (!enemigoOK && iRandom > 0) {
					x = (short) Utils.getRandomBetween (0, MAP_WIDTH - 1);
					y = (short) Utils.getRandomBetween (0, MAP_HEIGHT - 1);
					if (cells[x][y][dungeonLevel].isCave () && !cells[x][y][dungeonLevel].getTerrain ().hasFluids ()) {
						// El enemigo cabe, perfecto
						if (addNewLiving (lemi.getIniHeader (), LivingEntity.TYPE_ENEMY, false, x, y, dungeonLevel) != null) {
							enemigoOK = true;
						}
					}
					iRandom--;
				}

				if (!enemigoOK) {
					// A random no ha cabido, bucle para meterlo "a mano"
					bmetido: for (x = 0; x < MAP_WIDTH; x++) {
						for (y = 0; y < MAP_HEIGHT; y++) {
							if (cells[x][y][dungeonLevel].isCave () && !cells[x][y][dungeonLevel].getTerrain ().hasFluids ()) {
								// El enemigo cabe, perfecto
								if (addNewLiving (lemi.getIniHeader (), LivingEntity.TYPE_ENEMY, false, x, y, dungeonLevel) != null) {
									break bmetido;
								}
							}
						}
					}
				}
			}
		}
	}


	private void loadSpecialTiles () {
		tileMouseCursor = new MouseCursor ();
		tileMouseCursorBAD = new MouseCursorBAD ();
		tileUnknown = new Tile ("unknown"); //$NON-NLS-1$
		tileUnknownMini = new Tile ("unknown"); //$NON-NLS-1$
		tileUnknownMini.changeGraphic ("unknown_block"); //$NON-NLS-1$
		tileWater = new Water ();

		// Demo
		maxDemoDays = 0;
		for (int i = 1; i < 4; i++) {
			maxDemoDays += i;
		}

		tileLava = new Lava ();
		tileOrders = new Orders ();
		tileRedCross = new RedCross ();
		tileStockpile = new StockpileTile ();
		tilePatrolMark = new Tile ("patrolmark"); //$NON-NLS-1$

		for (int i = -1; i < 6; i++) {
			maxDemoDays += i;
		}

		tileCitizenEating = new CitEating ();
		tileCitizenSleeping = new CitSleeping ();
		tileCitizenExclamation = new CitExclamation ();
	}


	/**
	 * Next turn
	 */
	public void nextTurn () {
		// Cursores (si no está sacando el panel de typing)
		if (UIPanelState.typingPanel == null) {
			if (UtilsKeyboard.isFNKeyDown (UtilsKeyboard.FN_UP)) {
				keyPressed (Keyboard.KEY_NONE, UtilsKeyboard.FN_UP);
			} else if (UtilsKeyboard.isFNKeyDown (UtilsKeyboard.FN_DOWN)) {
				keyPressed (Keyboard.KEY_NONE, UtilsKeyboard.FN_DOWN);
			}

			if (UtilsKeyboard.isFNKeyDown (UtilsKeyboard.FN_LEFT)) {
				keyPressed (Keyboard.KEY_NONE, UtilsKeyboard.FN_LEFT);
			} else if (UtilsKeyboard.isFNKeyDown (UtilsKeyboard.FN_RIGHT)) {
				keyPressed (Keyboard.KEY_NONE, UtilsKeyboard.FN_RIGHT);
			}
		}

		updateNextFrameTurn ();
		// Preparado para el siguiente turno?
		if (!isReadyForNextTurn ()) {
			if (isReadyForNextTurnTasks ()) {
				// Hacemos todas las tareas
				getTaskManager ().executeAll (true);
				setReadyForNextTurnTasks (false);
			}
			return;
		}
		setReadyForNextTurn (false);
		setReadyForNextTurnTasks (false);

		// if (World.getCitizenIDs ().size () > 0) {
		// setReadyForNextTurn (true);
		// setReadyForNextTurnTasks (true);
		// }
		// Demo version, codificado de forma rara para evitar que se toque con un editor hexadecimal o así
		if (TownsProperties.DEMO_VERSION && (date.getDay () > maxDemoDays || date.getMonth () > 1 || date.getYear () > 1)) {
			if (Game.getCurrentState () == Game.STATE_CREATING_TASK) {
				Game.deleteCurrentTask ();
			}
			UtilsAL.stopMusic ();
			UtilsAL.stopFX ();
			UtilsAL.play (UtilsAL.SOURCE_MUSIC_MAINMENU);
			Game.exitToMainMenu ();
			return;
		}

		// Fecha
		turn++;
		if (turn >= TIME_MODIFIER_DAY) {
			turn = 0;
			date.addDay ();

			// Autosave?
			int iSave = Game.getAutosaveDays ();
			if (iSave > 0) {
				currentAutosaveDays++;
				if ((currentAutosaveDays % iSave) == 0) {
					String sText = Messages.getString ("World.18"); //$NON-NLS-1$
					MessagesPanel.addMessage (MessagesPanel.TYPE_SYSTEM, sText, ColorGL.YELLOW);

					// Text on top
					// Para que no parezca que el juego se lagea
					Game.render ();
					GL11.glBindTexture (GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
					GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
					UtilsGL.glBegin (GL11.GL_QUADS);
					UtilsGL.drawStringWithBorder (sText, MainPanel.renderWidth / 2 - UtilFont.getWidth (sText) / 2, MainPanel.renderHeight / 2 - UtilFont.MAX_HEIGHT / 2, ColorGL.YELLOW, ColorGL.BLACK);
					UtilsGL.glEnd ();
					Display.update ();
					Display.sync (Game.FPS_MAINMENU); // Para "capear" a 30 fps

					CommandPanel.executeCommand (CommandPanel.COMMAND_SAVE, null, null, null, null, 0);
					currentAutosaveDays = 0;
				}
			}
		}

		// Eventos (se refiere a cosa, no a los events)
		if (turn % TIME_MODIFIER_HOUR == 0) {
			// Happiness
			modifyHappiness ();

			// Immigrants
			if (turn % (TIME_MODIFIER_HOUR * 3) == 0) {
				checkImmigrants ();
			}

			// Heroes
			if (date.getDay () > 1 || date.getMonth () > 1 || date.getYear () > 1) {
				if (turn % (TIME_MODIFIER_HOUR * 2) == 0) {
					checkHeroesLeave ();
					checkHeroesCome ();
				}
				if (turn % (TIME_MODIFIER_DAY) == 0) {
					// Cada dia miramos si pilla nuevos amigos
					checkHeroesFriendships ();
				}
			}

			// Caravans
			if (date.getDay () > 7 || date.getMonth () > 1 || date.getYear () > 1) {
				if (turn % (TIME_MODIFIER_HOUR * 7) == 0) {
					checkCaravansCome ();
				}
			}

			// Siege?
			checkSiege (null);

			// Events
			checkEvents ();

			// Gods
			// if (Game.GODS_ACTIVATED) {
			// checkGodsStatus ();
			// }
		}

		// Recorremos los items lanzando el nextTurn en cada uno
		Integer[] aItems = items.keySet ().toArray (new Integer [0]);
		Item oItem;
		for (int i = (aItems.length - 1); i >= 0; i--) {
			oItem = getItems ().get (aItems[i]);
			if (oItem != null) {
				if (oItem.nextTurn ()) {
					// Borrar item
					oItem.delete ();
				}
			}
		}

		// Recorremos los edificios lanzando el nextTurn en cada uno
		int iIndex = getBuildings ().size () - 1;
		while (iIndex >= 0) {
			if (getBuildings ().get (iIndex).nextTurn ()) {
				getBuildings ().get (iIndex).delete ();
			}
			iIndex--;
		}

		// Livings
		aItems = livingsDiscovered.keySet ().toArray (new Integer [0]);
		LivingEntity oLiving;
		for (int i = (aItems.length - 1); i >= 0; i--) {
			oLiving = livingsDiscovered.get (aItems[i]);
			if (oLiving != null) {
				if (oLiving.nextTurn ()) {
					// Borrar living
					oLiving.delete ();

					// Tutorial flow
					Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_KILL, oLiving.getNumericIniHeader (), null);
				}
			}
		}

		// Llamamos al nextTurn de los proyectiles
		// Primero borramos los que toque
		iIndex = projectiles.size () - 1;
		while (iIndex >= 0) {
			if (projectiles.get (iIndex).isDelete ()) {
				projectiles.remove (iIndex).delete ();
			}
			iIndex--;
		}

		iIndex = projectiles.size () - 1;
		while (iIndex >= 0) {
			projectiles.get (iIndex).setDelete (projectiles.get (iIndex).nextTurn ());
			iIndex--;
		}

		// Falling items
		while (fallItemList.size () > 0) {
			Item item = Item.getItemByID (fallItemList.remove (0));
			if (item != null) {
				// Miramos si cae
				item.checkFall ();
			}
		}

		// Events
		iIndex = events.size () - 1;
		while (iIndex >= 0) {
			if (iIndex < events.size () && events.get (iIndex).nextTurn ()) { // Cuidado, que al hacer deletes pueden pasar cosas raras (no debería)
				EventData ed = events.get (iIndex);
				ed.addAfterEvents (ed.getEventID ());
				deleteEvent (ed.getEventID ());
				checkGlobalEvents ();
			}
			iIndex--;
		}

		// Hacemos todas las tareas
		getTaskManager ().executeAll (false);

		// Asignamos caminos a los aldeanos
		AStarQueue.setFinishedPaths ();

		// Si algún camino no se ha encontrado hacemos un full check
		if (isRecheckASZID ()) {
			if (recheckASZIDCounter > 0) {
				recheckASZIDCounter--;
			} else {
				Cell.setAllZoneIDs ();
			}
		}

		// Movimiento de los fluidos (agua, lava, ...)
		moveFluids (false);
		if (turn % 128 == 0) {
			evaporateFluids ();
		}
	}


	public static void addFallItem (int iID) {
		fallItemList.add (Integer.valueOf (iID));
	}


	/**
	 * Modifica la happiness de todos a partir de un porcentaje
	 * 
	 * @param PCT
	 */
	public void updateHappiness (int PCT) {
		Citizen cit;
		for (int i = 0; i < citizenIDs.size (); i++) {
			cit = (Citizen) getLivingEntityByID (citizenIDs.get (i));
			if (cit != null) {
				cit.getCitizenData ().setHappiness ((cit.getCitizenData ().getHappiness () * PCT) / 100);
			}
		}
		for (int i = 0; i < soldierIDs.size (); i++) {
			cit = (Citizen) getLivingEntityByID (soldierIDs.get (i));
			if (cit != null) {
				cit.getCitizenData ().setHappiness ((cit.getCitizenData ().getHappiness () * PCT) / 100);
			}
		}

		calculateHappinessAverage ();
	}


	/**
	 * Recorre todos los aldeanos y les modifica la happiness según lo que estén haciendo / viendo
	 */
	private void modifyHappiness () {
		Citizen citizen;
		// Cada hora modificamos la happiness según lo que estén haciendo/viendo en ese momento
		for (int i = 0; i < getCitizenIDs ().size (); i++) {
			citizen = (Citizen) getLivingEntityByID (getCitizenIDs ().get (i));
			if (citizen != null) {
				modifyHappiness (citizen);
			}
		}
		// for (int i = 0; i < getSoldierIDs ().size (); i++) {
		// citizen = (Citizen) getLivingEntityByID (getSoldierIDs ().get (i));
		// if (citizen != null) {
		// modifyHappiness (citizen);
		// }
		// }

		calculateHappinessAverage ();

	}


	private void modifyHappiness (Citizen citizen) {
		// Soldiers doesn't receive happiness
		// if (citizen.getSoldierData ().isSoldier ()) {
		// return;
		// }

		Cell cell;
		// Modificador por tarea
		// POPO citizen.getCitizenData ().setHappiness (citizen.getCitizenData ().getHappiness () + Task.getHappiness (citizen.getCurrentTask ()));

		// Modificador por LOS (sólo si el idle y work counters no son 0)
		if (citizen.getCitizenData ().getHappinessWorkCounter () != 0 && citizen.getCitizenData ().getHappinessIdleCounter () != 0) {
			ArrayList<Integer> alItemsHappy = new ArrayList<Integer> ();
			// Llenamos la lista con los valores de happiness que encontramos en LOS, quitando los que tienen happiness = 0
			for (short x = (short) (citizen.getX () - citizen.getLivingEntityData ().getLOSCurrent ()); x <= (citizen.getX () + citizen.getLivingEntityData ().getLOSCurrent ()); x++) {
				for (short y = (short) (citizen.getY () - citizen.getLivingEntityData ().getLOSCurrent ()); y <= (citizen.getY () + citizen.getLivingEntityData ().getLOSCurrent ()); y++) {
					if (Utils.isInsideMap (x, y, citizen.getZ ())) {
						cell = getCell (x, y, citizen.getZ ());
						if (cell.hasEntity ()) {
							ItemManagerItem imi = ItemManager.getItem (cell.getEntity ().getIniHeader ());
							if (imi != null && imi.getHappiness () != 0) {
								// Evitamos la infravisión (teniendo en cuenta que si que hay camino hasta la misma casilla donde está)
								// if ((x == citizen.getX () && y == citizen.getY ()) || Utils.bresenhamLineExists (citizen.getX (), citizen.getY (), x, y, citizen.getZ (), LivingEntity.TYPE_CITIZEN) || Utils.bresenhamLineExists (x, y, citizen.getX (), citizen.getY (), citizen.getZ (), LivingEntity.TYPE_CITIZEN)) {
								if ((x == citizen.getX () && y == citizen.getY ()) || Utils.bresenhamLineExists (citizen.getX (), citizen.getY (), x, y, citizen.getZ ()) || Utils.bresenhamLineExists (x, y, citizen.getX (), citizen.getY (), citizen.getZ ())) {
									alItemsHappy.add (Integer.valueOf(imi.getHappiness ()));
								}
							}
						}
					}
				}
			}

			// Si la lista tiene items pillamos uno a random
			if (alItemsHappy.size () > 0) {
				citizen.getCitizenData ().setHappiness (citizen.getCitizenData ().getHappiness () + alItemsHappy.get (Utils.getRandomBetween (0, alItemsHappy.size () - 1)).intValue ());
			}
		}
	}


	/**
	 * Mira si se lanza algún efecto random
	 */
	private void checkEvents () {
		if (Utils.getRandomBetween (1, (24 * 2)) > 1) { // Cada 2 dias de media
			return;
		}

		// BAM, lanzamos evento
		EventManagerItem emi = EventManager.getRandomItem ();
		if (emi != null) {
			addEvent (emi);
		}
	}


	/**
	 * Mira si llegan inmigrantes. En ese caso los mete
	 */
	private void checkImmigrants () {
		// Inmigrantes?
		if (getCitizenIDs ().size () > 0) {
			int iHappiness = getHappinessAverage ();

			// Miramos que haya happiness suficiente, basada en el número de aldeanos
			int happinessMin = (getCitizenIDs ().size () + getSoldierIDs ().size ()) * 2;
			if (happinessMin < 20) {
				happinessMin = 20;
			} else if (happinessMin > 80) {
				happinessMin = 80;
			}
			if (iHappiness > happinessMin) {
				// Happiness de guais, que vengan inmigrantes! 1d3 + 1
				int iQtty = Utils.launchDice (1, 3, 1);
				// Miramos si hay Zones personales libres, en otro caso no vendrán
				int iASZID;
				ArrayList<Integer> alASZID = new ArrayList<Integer> ();
				ArrayList<Integer> alZonesID = new ArrayList<Integer> ();
				ArrayList<Point3DShort> alZonesPoint = new ArrayList<Point3DShort> ();
				for (int i = 0; i < getZones ().size (); i++) {
					if (ZoneManager.getItem (getZones ().get (i).getIniHeader ()).getType () == ZoneManagerItem.TYPE_PERSONAL && ((ZonePersonal) getZones ().get (i)).getOwnerID () == -1) {
						iASZID = -1;
						ZonePersonal zonePersonal = (ZonePersonal) getZones ().get (i);
						// Buscamos un punto ASZID válido en la zona
						for (int p = 0; p < zonePersonal.getPoints ().size (); p++) {
							iASZID = getCell (getZones ().get (i).getPoints ().get (p)).getAstarZoneID ();
							if (iASZID != -1) {
								// Zona libre
								alASZID.add (Integer.valueOf(iASZID));
								alZonesID.add (zonePersonal.getID ());
								alZonesPoint.add (Point3DShort.getPoolInstance (getZones ().get (i).getPoints ().get (p)));
								iQtty--;
								break;
							}
						}

						if (iQtty == 0) {
							break;
						}
					}
				}

				if (alASZID.size () > 0) {
					int iImmigrantsOK = 0;
					int iImmigrantsNotOK = 0;

					// Upgradeamos ASZID para evitar problemas con el admin water o lo que sea
					if (isRecheckASZID ()) {
						Cell.setAllZoneIDs ();
					}

					Point3DShort p3dArrival = null;
					for (int i = 0; i < alASZID.size (); i++) {
						// Buscamos el sitio donde saldrán, nos basamos en el A*ZI de las Zones libres
						if (p3dArrival == null) {
							p3dArrival = getRandomBorderPoint (alASZID.get (i));
						} else {
							// Ya tenemos punto de antes, miramos si es la misma zona
							// Ésto se hace para que los aldeanos vengan del mismo sitio y no dispersos
							if (getCell (p3dArrival).getAstarZoneID () != alASZID.get (i).intValue ()) {
								// Distinto Zone ID, calculamos nuevo punto
								p3dArrival = getRandomBorderPoint (alASZID.get (i));
							} // Else, mismo zone ID, así que mantenemos el punto de arrival
						}

						if (p3dArrival != null) {
							Citizen citizen = (Citizen) addNewLiving (null, LivingEntity.TYPE_CITIZEN, true, p3dArrival.x, p3dArrival.y, p3dArrival.z, true);
							if (citizen != null) {
								iImmigrantsOK++;
								ZonePersonal.assignZone (citizen, alZonesID.get (i).intValue ());
								// Le añadimos metemos la zona
								updateHappiness (80);

								citizen.setDestination (alZonesPoint.get (i));
							} else {
								// Raro, no ha podido crear el cit
								iImmigrantsNotOK++;
							}
						} else {
							iImmigrantsNotOK++;
						}
					}

					if (iImmigrantsOK > 0) {
						MessagesPanel.addMessage (MessagesPanel.TYPE_ANNOUNCEMENT, Messages.getString ("World.1") + " (" + iImmigrantsOK + ")", ColorGL.GREEN); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

						// Audio
						UtilsAL.play ("fxnewimmigrants"); //$NON-NLS-1$

						// Tutorial flow?
						Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_POPULATION, (getNumCitizens () + getNumSoldiers ()), null);
					}
					if (iImmigrantsNotOK > 0) {
						MessagesPanel.addMessage (MessagesPanel.TYPE_ANNOUNCEMENT, Messages.getString ("World.2") + " (" + iImmigrantsNotOK + ")", ColorGL.RED); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					}
				}
			}
		}
	}


	/**
	 * Mira si los héroes tienen que pirarse. En ese caso les setea el flag de leave
	 */
	private void checkHeroesLeave () {
		// Para cada héroe miramos si se tiene que pirar o no
		int iID;
		ArrayList<Integer> alLeavingHeroes = null;
		Hero hero;
		for (int i = 0; i < World.getHeroIDs ().size (); i++) {
			iID = World.getHeroIDs ().get (i).intValue ();
			hero = (Hero) World.getLivingEntityByID (iID);
			if (hero.getHeroData ().getHeroTask ().getTaskID () == HeroTask.TASK_LEAVING) {
				// Si ya se está pirando no hacemos nada
				continue;
			} else if (hero.getHeroData ().getMinTurnsToStay () > 0) {
				// Turnos mínimos a pasar en la ciudad antes de pirarse
				continue;
			}
			// Tenemos un hero, vamos a ver si se cumplen sus requisitos para quedarse
			LivingEntityManagerItem lemi = LivingEntityManager.getItem (hero.getIniHeader ());

			if (lemi != null) {
				boolean bPrerequisitesOK = true;
				// Miramos los prerequisitos
				ArrayList<HeroPrerequisite> alPrerequisites = HeroManager.getStayPrerequisites (lemi.getHeroStayPrerequisite ());
				HeroPrerequisite prerequisite;
				for (int p = 0; p < alPrerequisites.size (); p++) {
					// Comprobamos cada prerequisito
					prerequisite = alPrerequisites.get (p);

					if (prerequisite.getId () == HeroPrerequisite.ID_MIN_CITIZENS) {
						// Num citizens
						if ((getNumCitizens () + getNumSoldiers ()) < prerequisite.getValueInt ()) {
							bPrerequisitesOK = false;
							break;
						}
					} else if (prerequisite.getId () == HeroPrerequisite.ID_MAX_CITIZENS) {
						// Num citizens
						if ((getNumCitizens () + getNumSoldiers ()) > prerequisite.getValueInt ()) {
							bPrerequisitesOK = false;
							break;
						}
					} else if (prerequisite.getId () == HeroPrerequisite.ID_FREE_ROOM && prerequisite.isValueBoolean ()) {
						// Free room
						if (hero.getCitizenData ().getZoneID () == -1) {
							bPrerequisitesOK = false;
							break;
						}
					} else if (prerequisite.getId () == HeroPrerequisite.ID_FREE_ROOM_UNDERGROUND && prerequisite.isValueBoolean ()) {
						// Free room
						if (hero.getCitizenData ().getZoneID () == -1) {
							bPrerequisitesOK = false;
							break;
						} else {
							// Tiene room, miramos si underground
							Zone zone = Zone.getZone (hero.getCitizenData ().getZoneID ());
							if (zone == null) {
								bPrerequisitesOK = false;
								break;
							} else {
								if (zone.getPoints ().size () > 0 && zone.getPoints ().get (0).z < World.MAP_NUM_LEVELS_OUTSIDE) {
									bPrerequisitesOK = false;
									break;
								}
							}
						}
					} else if (prerequisite.getId () == HeroPrerequisite.ID_FREE_ROOM_HIGH && prerequisite.isValueBoolean ()) {
						// Free room
						if (hero.getCitizenData ().getZoneID () == -1) {
							bPrerequisitesOK = false;
							break;
						} else {
							// Tiene room, miramos si elevada
							Zone zone = Zone.getZone (hero.getCitizenData ().getZoneID ());
							if (zone == null) {
								bPrerequisitesOK = false;
								break;
							} else {
								if (zone.getPoints ().size () > 0 && zone.getPoints ().get (0).z > (MAP_NUM_LEVELS_OUTSIDE - prerequisite.getValueInt ())) {
									bPrerequisitesOK = false;
									break;
								}
							}
						}
					} else if (prerequisite.getId () == HeroPrerequisite.ID_LEVEL_DISCOVERED) {
						if (prerequisite.getValueInt () > (getNumFloorsDiscovered () - World.MAP_NUM_LEVELS_OUTSIDE)) {
							bPrerequisitesOK = false;
							break;
						}
					} else if (prerequisite.getId () == HeroPrerequisite.ID_ZONE) {
						Zone zone;
						boolean bZoneFound = false;
						for (int z = 0; z < getZones ().size (); z++) {
							zone = getZones ().get (z);
							if (zone.getIniHeader ().equals (prerequisite.getValueString ())) {
								bZoneFound = true;
								break;
							}
						}

						if (!bZoneFound) {
							bPrerequisitesOK = false;
							break;
						}
					}
				}

				if (!bPrerequisitesOK) {
					if (alLeavingHeroes == null) {
						alLeavingHeroes = new ArrayList<Integer> ();
						alLeavingHeroes.add (Integer.valueOf(hero.getID ()));
					}
				}
			}
		}

		if (alLeavingHeroes != null) {
			// Upgradeamos ASZID para evitar problemas con el admin water o lo que sea
			if (isRecheckASZID ()) {
				Cell.setAllZoneIDs ();
			}

			for (int i = 0; i < alLeavingHeroes.size (); i++) {
				iID = alLeavingHeroes.get (i).intValue ();
				hero = (Hero) World.getLivingEntityByID (iID);
				if (hero != null) {
					// Un héroe que se pira
					MessagesPanel.addMessage (MessagesPanel.TYPE_HEROES, hero.getCitizenData ().getFullName () + Messages.getString ("World.17"), ColorGL.ORANGE, hero.getCoordinates (), hero.getID ()); //$NON-NLS-1$

					hero.startLeaving ();
				}
			}
		}
	}


	/**
	 * Mira si llegan héroes. En ese caso los mete
	 */
	public void checkHeroesCome () {
		if (Utils.getRandomBetween (1, 12) != 1) {
			return;
		}

		// Antes de nada miramos si tenemos el máximo de héroes, nos basamos en el número de aldeanos
		// De momento 1 hero por cada 2 aldeanos
		int iMaxHeros = Utils.sqrt (getNumCitizens () + getNumSoldiers ());

		if (World.getHeroIDs ().size () >= iMaxHeros) {
			return;
		}

		LivingEntityManagerItem lemi;
		boolean bOldHeroToCome = getOldHeroes ().size () > 0 && Utils.getRandomBetween (1, 20) == 1;
		if (bOldHeroToCome) {
			// Miramos si puede llegar un héroe de los que ya se han ido
			int iIndexHero = Utils.getRandomBetween (0, getOldHeroes ().size () - 1);
			lemi = LivingEntityManager.getItem (getOldHeroes ().get (iIndexHero).getIniHeader ());
			getOldHeroes ().remove (iIndexHero);
		} else {
			// En otro caso obtenemos un heroe a random y vemos si se cumplen sus prerequisitos
			lemi = LivingEntityManager.getRandomItemByType (LivingEntity.TYPE_HERO);
			if (getOldHeroesDied ().contains (lemi.getIniHeader ())) {
				return;
			}
		}

		if (lemi != null) {
			// Antes de mirar los prerequisitos miramos que no sea un héroe único (con nombre fijo), en ese caso miramos que no lo tengamos ya en el mundo o en la lista de héroes que se han ido
			if (lemi.getName () != null && (lemi.getNamePoolTag () == null || lemi.getNamePoolTag ().length () == 0)) {
				// Héroe con nombre único, miramos que no tengamos uno de estos ya en el mundo (o en los old heros)
				Hero hero;
				LivingEntityManagerItem lemiWorld;
				for (int i = 0; i < World.getHeroIDs ().size (); i++) {
					hero = (Hero) World.getLivingEntityByID (World.getHeroIDs ().get (i).intValue ());

					if (hero != null) {
						lemiWorld = LivingEntityManager.getItem (hero.getIniHeader ());
						if (lemiWorld != null && lemiWorld.getName () != null && lemiWorld.getName ().equalsIgnoreCase (lemi.getName ())) {
							// Mismo héroe, pos ala, pacasa, no viene
							return;
						}
					}
				}

				// Si llega aquí es que el nuevo héroe no está en el mundo, miramos si está en los old heros
				if (!bOldHeroToCome) {
					for (int i = 0; i < getOldHeroes ().size (); i++) {
						lemiWorld = LivingEntityManager.getItem (getOldHeroes ().get (i).getIniHeader ());
						if (lemiWorld != null && lemiWorld.getName () != null && lemiWorld.getName ().equalsIgnoreCase (lemi.getName ())) {
							// Mismo héroe, pos ala, pacasa, no viene
							return;
						}
					}
				}
			}

			// Miramos los prerequisitos
			ArrayList<HeroPrerequisite> alPrerequisites = HeroManager.getComePrerequisites (lemi.getHeroComePrerequisite ());

			HeroPrerequisite prerequisite;
			boolean bPrerequisitesOK = true;
			Point3DShort p3dDestinationPoint = null;
			int iZoneID = -1;
			for (int i = 0; i < alPrerequisites.size (); i++) {
				// Comprobamos cada prerequisito
				prerequisite = alPrerequisites.get (i);

				if (prerequisite.getId () == HeroPrerequisite.ID_MIN_CITIZENS) {
					// Num citizens
					if ((getNumCitizens () + getNumSoldiers ()) < prerequisite.getValueInt ()) {
						bPrerequisitesOK = false;
						break;
					}
				} else if (prerequisite.getId () == HeroPrerequisite.ID_MAX_CITIZENS) {
					// Num citizens
					if ((getNumCitizens () + getNumSoldiers ()) > prerequisite.getValueInt ()) {
						bPrerequisitesOK = false;
						break;
					}
				} else if ((prerequisite.getId () == HeroPrerequisite.ID_FREE_ROOM || prerequisite.getId () == HeroPrerequisite.ID_FREE_ROOM_UNDERGROUND) && prerequisite.isValueBoolean ()) {
					// Free room
					int iASZID = -1;
					for (int z = 0; z < getZones ().size (); z++) {
						if (ZoneManager.getItem (getZones ().get (z).getIniHeader ()).getType () == ZoneManagerItem.TYPE_HERO_ROOM && ((ZoneHeroRoom) getZones ().get (z)).getOwnerID () == -1) {
							ZoneHeroRoom zoneHero = (ZoneHeroRoom) getZones ().get (z);
							if (zoneHero.getPoints ().size () > 0) {
								// Miramos si tiene que ser underground
								if (prerequisite.getId () == HeroPrerequisite.ID_FREE_ROOM_UNDERGROUND) {
									if (zoneHero.getPoints ().get (0).z < World.MAP_NUM_LEVELS_OUTSIDE) {
										continue;
									}
								}

								if (!isFreeRoomItemsOK (alPrerequisites, zoneHero)) {
									continue;
								}

								// Buscamos un punto que tenga ASZID válido
								for (int p = 0; p < zoneHero.getPoints ().size (); p++) {
									iASZID = getCell (zoneHero.getPoints ().get (p)).getAstarZoneID ();
									if (iASZID != -1) {
										p3dDestinationPoint = zoneHero.getPoints ().get (p);
										iZoneID = zoneHero.getID ();
										break;
									}
								}
								if (iASZID != -1) {
									break;
								}
							} else {
								// Zone sin points, muy extraño
								Log.log (Log.LEVEL_ERROR, Messages.getString ("World.0"), getClass ().toString ()); //$NON-NLS-1$
							}
						}
					}

					if (iASZID == -1) {
						bPrerequisitesOK = false;
						break;
					}
				} else if (prerequisite.getId () == HeroPrerequisite.ID_FREE_ROOM_HIGH) {
					// Free room HIGH
					int iASZID = -1;
					for (int z = 0; z < getZones ().size (); z++) {
						if (ZoneManager.getItem (getZones ().get (z).getIniHeader ()).getType () == ZoneManagerItem.TYPE_HERO_ROOM && ((ZoneHeroRoom) getZones ().get (z)).getOwnerID () == -1) {
							ZoneHeroRoom zoneHero = (ZoneHeroRoom) getZones ().get (z);
							if (zoneHero.getPoints ().size () > 0) {
								// Comprobamos la altura mínima
								if (zoneHero.getPoints ().size () > 0 && zoneHero.getPoints ().get (0).z > (MAP_NUM_LEVELS_OUTSIDE - prerequisite.getValueInt ())) {
									continue;
								}

								if (!isFreeRoomItemsOK (alPrerequisites, zoneHero)) {
									continue;
								}

								// Buscamos un punto que tenga ASZID válido
								for (int p = 0; p < zoneHero.getPoints ().size (); p++) {
									iASZID = getCell (zoneHero.getPoints ().get (p)).getAstarZoneID ();
									if (iASZID != -1) {
										p3dDestinationPoint = zoneHero.getPoints ().get (p);
										iZoneID = zoneHero.getID ();
										break;
									}
								}
								if (iASZID != -1) {
									break;
								}
							} else {
								// Zone sin points, muy extraño
								Log.log (Log.LEVEL_ERROR, Messages.getString ("World.0"), getClass ().toString ()); //$NON-NLS-1$
							}
						}
					}

					if (iASZID == -1) {
						bPrerequisitesOK = false;
						break;
					}
				} else if (prerequisite.getId () == HeroPrerequisite.ID_LEVEL_DISCOVERED) {
					if (prerequisite.getValueInt () > (getNumFloorsDiscovered () - World.MAP_NUM_LEVELS_OUTSIDE)) {
						bPrerequisitesOK = false;
						break;
					}
				} else if (prerequisite.getId () == HeroPrerequisite.ID_ZONE) {
					Zone zone;
					boolean bZoneFound = false;
					for (int z = 0; z < getZones ().size (); z++) {
						zone = getZones ().get (z);
						if (zone.getIniHeader ().equals (prerequisite.getValueString ())) {
							bZoneFound = true;
							break;
						}
					}

					if (!bZoneFound) {
						bPrerequisitesOK = false;
						break;
					}
				}
			}

			if (bPrerequisitesOK) {
				// Todo OK, héroe tiene que aparecer

				// Upgradeamos ASZID para evitar problemas con el admin water o lo que sea
				if (isRecheckASZID ()) {
					Cell.setAllZoneIDs ();
				}

				if (p3dDestinationPoint == null) {
					// No hay punto de room, metemos al héroe en el mismo ASZID que el primer aldeano
					if (getCitizenIDs ().size () > 0) {
						Citizen cit = (Citizen) World.getLivingEntityByID (getCitizenIDs ().get (0));
						if (cit != null) {
							p3dDestinationPoint = cit.getCoordinates ();
						}
					} else if (World.getSoldierIDs ().size () > 0) {
						Citizen cit = (Citizen) World.getLivingEntityByID (getSoldierIDs ().get (0));
						if (cit != null) {
							p3dDestinationPoint = cit.getCoordinates ();
						}
					}
				}

				if (p3dDestinationPoint != null) {
					// Tenemos punto de destino, metemos al héroe y lo movemos a ese punto
					int iASZID = getCell (p3dDestinationPoint).getAstarZoneID ();
					Point3DShort p3dArrival = getRandomBorderPoint (iASZID);
					if (p3dArrival != null) {
						Hero hero = (Hero) addNewLiving (lemi.getIniHeader (), LivingEntity.TYPE_HERO, true, p3dArrival.x, p3dArrival.y, p3dArrival.z, true);
						if (hero != null) {
							hero.getHeroData ().setStartingPoint (Point3DShort.getPoolInstance (p3dArrival));

							MessagesPanel.addMessage (MessagesPanel.TYPE_HEROES, hero.getCitizenData ().getFullName () + Messages.getString ("World.15"), ColorGL.GREEN, p3dArrival, hero.getID ()); //$NON-NLS-1$
							if (iZoneID != -1) {
								ZoneHeroRoom.assignZone (hero, iZoneID);
							}

							hero.setDestination (p3dDestinationPoint);

							// Audio
							UtilsAL.play ("fxnewhero"); //$NON-NLS-1$
						} else {
							MessagesPanel.addMessage (MessagesPanel.TYPE_HEROES, Messages.getString ("World.16"), ColorGL.RED); //$NON-NLS-1$
						}
					} else {
						MessagesPanel.addMessage (MessagesPanel.TYPE_HEROES, Messages.getString ("World.16"), ColorGL.RED); //$NON-NLS-1$
					}
				}
			}
		}
	}


	private boolean isFreeRoomItemsOK (ArrayList<HeroPrerequisite> alPrerequisites, ZoneHeroRoom zoneHeroRoom) {
		// Miramos que tenga un freeRoomItems
		String sItems = null;
		for (int po = 0; po < alPrerequisites.size (); po++) {
			HeroPrerequisite hPre = alPrerequisites.get (po);
			if (hPre.getId () == HeroPrerequisite.ID_FREE_ROOM_ITEMS) {
				sItems = hPre.getValueString ();
				break;
			}
		}

		if (sItems == null || sItems.length () == 0) {
			return true;
		}

		// Hay items que comprobar, allá vamos
		// Todos deben existir y estar en la zona
		StringTokenizer tokenizer = new StringTokenizer (sItems, ","); //$NON-NLS-1$
		ItemManagerItem imi;
		String sItem;
		whileitems: while (tokenizer.hasMoreTokens ()) {
			sItem = tokenizer.nextToken ();
			imi = ItemManager.getItem (sItem);
			if (imi != null) {
				// Item válido, a ver si hay alguno en la zona
				ArrayList<Point3DShort> p3ds = zoneHeroRoom.getPoints ();
				Cell cell;
				Item item;
				for (int i = 0; i < p3ds.size (); i++) {
					cell = getCell (p3ds.get (i));
					item = cell.getItem ();
					if (item != null && item.getIniHeader ().equals (sItem)) {
						// BINGO, siguiente item
						continue whileitems;
					}
				}

				// Si llega aquí es que el item no existe
				return false;
			} else {
				Log.log (Log.LEVEL_ERROR, Messages.getString ("World.24") + " [" + sItem + "]", getClass ().toString ()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				return false;
			}
		}

		return true;
	}


	private void checkHeroesFriendships () {
		// Para cada héroe miramos si obtien algún nuevo amigo
		int iID, iPossibleFriendID;
		Hero hero, possibleHeroFriend;
		int iNumHeroes = World.getHeroIDs ().size ();
		for (int i = 0; i < (iNumHeroes - 1); i++) {
			for (int h = (i + 1); h < iNumHeroes; h++) {
				iID = World.getHeroIDs ().get (i).intValue ();
				hero = (Hero) World.getLivingEntityByID (iID);

				if (hero.getHeroData ().getMinTurnsToStay () <= 0) {
					iPossibleFriendID = World.getHeroIDs ().get (h).intValue ();
					if (!hero.getHeroData ().getFriendships ().contains (Integer.valueOf (iPossibleFriendID))) {
						// Si no tiene amigos o si no tiene al posible amigo, comprararemos moral y quizá se harán amigos
						possibleHeroFriend = (Hero) World.getLivingEntityByID (iPossibleFriendID);

						if (possibleHeroFriend.getHeroData ().getMinTurnsToStay () <= 0) {
							int iMoraleDifference = Math.abs (hero.getLivingEntityData ().getMoral () - possibleHeroFriend.getLivingEntityData ().getMoral ());
							if (iMoraleDifference <= 5) {
								// Bingo, amigos para siempre !
								hero.getHeroData ().getFriendships ().add (Integer.valueOf (iPossibleFriendID));

								// Y viceversa
								if (!possibleHeroFriend.getHeroData ().getFriendships ().contains (Integer.valueOf (iID))) { // Ésto debería cumplirse siempre
									possibleHeroFriend.getHeroData ().getFriendships ().add (Integer.valueOf (iID));
								}
							}
						}
					}
				}
			}
		}
	}


	/**
	 * Mira si llega una caravana
	 */
	public void checkCaravansCome () {
		if (currentCaravanData != null) {
			return;
		}

		if (Utils.getRandomBetween (1, 24) > 1) {
			return;
		}

		// Llega una caravana! (si puede)
		LivingEntityManagerItem lemi = LivingEntityManager.getCaravanAtRandom ();
		if (lemi == null) {
			return;
		}
		CaravanManagerItem cmi = CaravanManager.getItem (lemi.getCaravan ());

		// Miramos si la zona requerida existe
		// Obtenemos una lista por si hay más de una
		Zone zone;
		ArrayList<Point3DShort> alDestinations = null;
		for (int i = 0; i < getZones ().size (); i++) {
			zone = getZones ().get (i);
			if (zone.getIniHeader ().equals (cmi.getZone ())) {
				// La tenemos
				if (alDestinations == null) {
					alDestinations = new ArrayList<Point3DShort> ();
				}

				// Buscamos un punto libre de la zona
				int iMaxTrys = 10;
				boolean bEncontrado = false;
				Point3DShort p3dAux;
				while (iMaxTrys > 0) {
					iMaxTrys--;
					p3dAux = zone.getPoints ().get (Utils.getRandomBetween (0, zone.getPoints ().size () - 1));
					if (World.getCell (p3dAux).getAstarZoneID () != -1) {
						// Encontrado
						alDestinations.add (p3dAux);
						bEncontrado = true;
					}
				}

				if (!bEncontrado) {
					// Punto no encontrado, miramos en todos los puntos de la zona
					for (int j = 0; j < zone.getPoints ().size (); j++) {
						p3dAux = zone.getPoints ().get (j);
						if (World.getCell (p3dAux).getAstarZoneID () != -1) {
							// Encontrado
							alDestinations.add (p3dAux);
							break;
						}
					}
				}
			}
		}

		if (alDestinations == null || alDestinations.size () == 0) {
			return;
		}

		// Tenemos puntos de destino, buscamos por donde sale
		// Vamos recorriendo la lista de zones a random
		Point3DShort p3dDestination = null;
		Point3DShort p3dCome = null;
		while (alDestinations.size () > 0) {
			p3dDestination = alDestinations.remove (Utils.getRandomBetween (0, alDestinations.size () - 1));
			p3dCome = getRandomBorderPoint (getCell (p3dDestination).getAstarZoneID ());
			if (p3dCome != null) {
				// Ya lo tenemos, salimos
				alDestinations.clear ();
			}
		}

		if (p3dCome == null) {
			return;
		}

		// Tenemos los 2 puntos, metemos la living y que vaya para allá
		LivingEntity leCaravan = World.addNewLiving (lemi.getIniHeader (), lemi.getType (), true, p3dCome.x, p3dCome.y, p3dCome.z, true);
		leCaravan.setDestination (p3dDestination);
		String sCaravanMessage = Messages.getString ("World.19") + " (" + leCaravan.getLivingEntityData ().getName () + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (Game.isCaravanPause ()) {
			sCaravanMessage = sCaravanMessage + " " + Messages.getString ("World.22"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		MessagesPanel.addMessage (MessagesPanel.TYPE_ANNOUNCEMENT, sCaravanMessage, ColorGL.ORANGE, p3dCome, leCaravan.getID ());

		if (Game.isCaravanPause ()) {
			Game.pause (false);
		}

		// Audio
		UtilsAL.play ("fxcaravan"); //$NON-NLS-1$
	}


	/**
	 * Mira si debe meter una siege, y lo hace si puede
	 * 
	 * @param emi Evento, si se pasa null funciona como siempre, en caso contrario se usan los datos del emi (y la siege aparece si o si, si cabe, claro)
	 */
	public void checkSiege (EventManagerItem emi) {
		if (Game.getSiegeDifficulty () == Game.SIEGE_DIFFICULTY_OFF) {
			return;
		}

		// Hasta el día 10 no aparecen
		if (emi == null) {
			Date date = getDate ();
			if (date.getYear () == 1 && date.getMonth () == 1 && date.getDay () < 10) {
				return;
			}
		}

		int iNumTownies = getNumCitizens () + getNumSoldiers ();
		if (iNumTownies == 0) {
			return;
		}

		// 3 sieges cada 360 horas (esto sin contar la protección de soldados)
		int iSiegeRandom;
		if (emi == null) {
			iSiegeRandom = Utils.getRandomBetween (1, 360);
			if (iSiegeRandom > 3) {
				return;
			}
		} else {
			iSiegeRandom = Utils.getRandomBetween (1, 3);
		}

		if (emi == null) {
			// Protección de soldados
			int iSiegeProtection;
			if (getNumSoldiers () == 0) {
				iSiegeProtection = 0;
			} else {
				// Hay soldados
				if (getNumCitizens () == 0) {
					// Todo soldados
					iSiegeProtection = 100;
				} else {
					// Hay soldados Y civiles
					iSiegeProtection = (getNumSoldiers () * 100) / iNumTownies;
				}
			}

			if (iSiegeProtection > 0 && Utils.getRandomBetween (1, 100) <= iSiegeProtection) {
				return;
			}
		}

		ArrayList<Integer> alNumEnemies = new ArrayList<Integer> ();
		if (emi == null || emi.getSiegeSize () == null || emi.getSiegeSize ().size () == 0) {
			// Calculamos los puntos de siege
			int iSiegePoints = calculateSiegePoints ();

			// Miramos el tipo de siege, normal o pequeña
			if (iSiegeRandom > 1) {
				// Pequeña
				iSiegePoints = (int) Math.sqrt (iSiegePoints);
			}

			if (iSiegePoints < 1) {
				return;
			}

			// Siege !!
			// Veamos cuantos bichos vienen de cada nivel
			int iAux = iSiegePoints / 2;
			int iCurrentLevel = 1;
			int iNumEnemies;
			while (iAux > iCurrentLevel) {
				iNumEnemies = iAux / iCurrentLevel;
				if (iNumEnemies > 0) {
					alNumEnemies.add (Integer.valueOf (iNumEnemies));
				}

				iSiegePoints -= iAux;
				iCurrentLevel++;
				iAux = iSiegePoints / 2;
			}

			if (alNumEnemies.size () == 0) {
				return;
			}
		} else {
			// Siege personalizada
			int iNumEnemies = 0;
			for (int i = 0; i < emi.getSiegeSize ().size (); i++) {
				int iAux = Utils.launchDice (emi.getSiegeSize ().get (i));
				alNumEnemies.add (Integer.valueOf (iAux));
				iNumEnemies += iAux;
			}

			// Sumamos para ver que sea > 0
			if (iNumEnemies <= 0) {
				return;
			}
		}

		// A spawnear enemigos toca
		// Upgradeamos ASZID para evitar problemas con el admin water o lo que sea
		if (isRecheckASZID ()) {
			Cell.setAllZoneIDs ();
		}

		int iASZID = -1;
		Point3DShort auxCoordinates = null;
		if (getCitizenIDs ().size () > 0) {
			LivingEntity le = getLivingEntityByID (getCitizenIDs ().get (0));
			if (le != null) {
				auxCoordinates = le.getCoordinates ();
				iASZID = getCell (le.getCoordinates ()).getAstarZoneID ();
			}

		} else if (getSoldierIDs ().size () > 0) {
			LivingEntity le = getLivingEntityByID (getSoldierIDs ().get (0));
			if (le != null) {
				auxCoordinates = le.getCoordinates ();
				iASZID = getCell (le.getCoordinates ()).getAstarZoneID ();
			}
		}

		if (auxCoordinates == null || iASZID == -1) {
			return;
		}

		// Siege type & starting point
		byte siegeType;
		Point3DShort p3dSpawnPoint;
		if (iSiegeRandom == 1 || iSiegeRandom == 2 || (emi != null && emi.isSiegeUnderground ())) {
			siegeType = SiegeData.SIEGE_STANDARD;
			// Pueden aparecer underground (20% y algunos niveles más descubiertos
			int iLevelMinToSpawnUnder = (MAP_NUM_LEVELS_OUTSIDE + 5);
			if (numFloorsDiscovered > (iLevelMinToSpawnUnder - 1) && ((emi != null && emi.isSiegeUnderground ()) || Utils.getRandomBetween (1, 10) <= 2)) {
				p3dSpawnPoint = getRandomUndergroundPoint (iASZID, iLevelMinToSpawnUnder, numFloorsDiscovered, true);
			} else {
				p3dSpawnPoint = getRandomBorderPoint (iASZID);
			}
		} else {
			siegeType = SiegeData.SIEGE_ROBBERY;
			p3dSpawnPoint = getRandomBorderPoint (iASZID);
		}

		if (p3dSpawnPoint == null) {
			return;
		}

		int iWaitTurns = alNumEnemies.size () * TIME_MODIFIER_HOUR;
		boolean bSiegeOK = false;
		for (int i = 0; i < alNumEnemies.size (); i++) {
			if (emi == null || emi.getSiegeLivings () == null || emi.getSiegeLivings ().size () == 0) {
				if (addSiegeEnemies (p3dSpawnPoint, alNumEnemies.get (i).intValue (), (i + 1), siegeType, iWaitTurns)) {
					bSiegeOK = true;
				}
			} else {
				LivingEntityManagerItem lemi = LivingEntityManager.getItem (emi.getSiegeLivings ().get (i));
				if (lemi != null && addSiegeEnemies (p3dSpawnPoint, alNumEnemies.get (i).intValue (), lemi, siegeType, iWaitTurns)) {
					bSiegeOK = true;
				}
			}
		}

		if (bSiegeOK) {
			String sSiegeMessage = Messages.getString ("World.3"); //$NON-NLS-1$
			if (Game.isSiegePause ()) {
				sSiegeMessage = sSiegeMessage + " " + Messages.getString ("World.22"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			MessagesPanel.addMessage (MessagesPanel.TYPE_ANNOUNCEMENT, sSiegeMessage, ColorGL.RED, p3dSpawnPoint);
			if (Game.isSiegePause ()) {
				Game.pause (true);
			}
		}
	}


	/**
	 * Copia de checkSiege, pero sin miramientos.... función admin, mete la siege sin lanzar dados para ver si toca
	 */
	public void spawnSiege () {
		int iNumTownies = getNumCitizens () + getNumSoldiers ();
		if (iNumTownies == 0) {
			return;
		}

		// 3 sieges cada 360 horas (esto sin contar la protección de soldados)
		int iSiegeRandom = Utils.getRandomBetween (1, 3);

		// Calculamos los puntos de siege
		int iSiegePoints = calculateSiegePoints ();

		// Miramos el tipo de siege, normal o pequeña
		if (iSiegeRandom > 1) {
			// Pequeña
			iSiegePoints = (int) Math.sqrt (iSiegePoints);
		}

		if (iSiegePoints < 1) {
			return;
		}

		// Siege !!
		// Veamos cuantos bichos vienen de cada nivel
		ArrayList<Integer> alNumEnemies = new ArrayList<Integer> ();
		int iAux = iSiegePoints / 2;
		int iCurrentLevel = 1;
		int iNumEnemies;
		while (iAux >= iCurrentLevel) {
			iNumEnemies = iAux / iCurrentLevel;
			if (iNumEnemies > 0) {
				alNumEnemies.add (Integer.valueOf (iNumEnemies));
			}

			iSiegePoints -= iAux;
			iCurrentLevel++;
			iAux = iSiegePoints / 2;
		}

		if (alNumEnemies.size () == 0) {
			return;
		}

		// A spawnear enemigos toca
		// Upgradeamos ASZID para evitar problemas con el admin water o lo que sea
		if (isRecheckASZID ()) {
			Cell.setAllZoneIDs ();
		}

		int iASZID = -1;
		Point3DShort auxCoordinates = null;
		if (getCitizenIDs ().size () > 0) {
			LivingEntity le = getLivingEntityByID (getCitizenIDs ().get (0));
			if (le != null) {
				auxCoordinates = le.getCoordinates ();
				iASZID = getCell (le.getCoordinates ()).getAstarZoneID ();
			}

		} else if (getSoldierIDs ().size () > 0) {
			LivingEntity le = getLivingEntityByID (getSoldierIDs ().get (0));
			if (le != null) {
				auxCoordinates = le.getCoordinates ();
				iASZID = getCell (le.getCoordinates ()).getAstarZoneID ();
			}
		}

		if (auxCoordinates == null || iASZID == -1) {
			return;
		}

		// Siege type & starting point
		byte siegeType;
		Point3DShort p3dSpawnPoint;
		if (iSiegeRandom == 1 || iSiegeRandom == 2) {
			siegeType = SiegeData.SIEGE_STANDARD;
			// Pueden aparecer underground (20% y algunos niveles más descubiertos
			int iLevelMinToSpawnUnder = (MAP_NUM_LEVELS_OUTSIDE + 5);
			if (numFloorsDiscovered > (iLevelMinToSpawnUnder - 1) && Utils.getRandomBetween (1, 10) <= 2) {
				p3dSpawnPoint = getRandomUndergroundPoint (iASZID, iLevelMinToSpawnUnder, numFloorsDiscovered, true);
			} else {
				p3dSpawnPoint = getRandomBorderPoint (iASZID);
			}
		} else {
			siegeType = SiegeData.SIEGE_ROBBERY;
			p3dSpawnPoint = getRandomBorderPoint (iASZID);
		}

		if (p3dSpawnPoint == null) {
			return;
		}

		int iWaitTurns = alNumEnemies.size () * TIME_MODIFIER_HOUR;
		iWaitTurns = TIME_MODIFIER_HOUR;
		boolean bSiegeOK = false;
		for (int i = 0; i < alNumEnemies.size (); i++) {
			if (addSiegeEnemies (p3dSpawnPoint, alNumEnemies.get (i).intValue (), (i + 1), siegeType, iWaitTurns)) {
				bSiegeOK = true;
			}
		}

		if (bSiegeOK) {
			String sSiegeMessage = Messages.getString ("World.3"); //$NON-NLS-1$
			if (Game.isSiegePause ()) {
				sSiegeMessage = sSiegeMessage + " " + Messages.getString ("World.22"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			MessagesPanel.addMessage (MessagesPanel.TYPE_ANNOUNCEMENT, sSiegeMessage, ColorGL.RED, p3dSpawnPoint);
			if (Game.isSiegePause ()) {
				Game.pause (true);
			}
		}
	}


	public int calculateSiegePoints () {
		// Número de townies
		int iSiegePoints = getNumCitizens () + getNumSoldiers ();
		// Héroes level
		if (getNumHeroes () > 0) {
			ArrayList<Integer> alHeroes = getHeroIDs ();
			LivingEntity le;
			for (int i = 0; i < alHeroes.size (); i++) {
				le = getLivingEntityByID (alHeroes.get (i));
				if (le != null && le instanceof Hero) {
					iSiegePoints += ((Hero) le).getHeroData ().getLevel ();
				}
			}
		}
		// Días
		iSiegePoints += getDate ().getDay ();
		iSiegePoints += (getDate ().getMonth () - 1) * Date.DAYS_PER_MONTH;
		iSiegePoints += (getDate ().getYear () - 1) * (Date.DAYS_PER_MONTH * Date.MONTHS_PER_YEAR);
		// Town value (de momento doble sqrt)
		iSiegePoints += (int) Math.sqrt ((int) Math.sqrt (getTownValue ()));

		// Modificador según la dificultad the siege
		if (Game.getSiegeDifficulty () == Game.SIEGE_DIFFICULTY_EASY) {
			iSiegePoints /= 20;
		} else if (Game.getSiegeDifficulty () == Game.SIEGE_DIFFICULTY_NORMAL) {
			iSiegePoints /= 10;
		} else if (Game.getSiegeDifficulty () == Game.SIEGE_DIFFICULTY_HARD) {
			iSiegePoints /= 4;
			// } else if (Game.getSiegeDifficulty () == Game.SIEGE_DIFFICULTY_HARDER) {
			// Harder, los dejamos tal cual
		} else if (Game.getSiegeDifficulty () == Game.SIEGE_DIFFICULTY_INSANE) {
			iSiegePoints *= 4;
		}

		return iSiegePoints;
	}


	/**
	 * Busca un punto underground para spawnear enemigos
	 * 
	 * @param iASZI
	 * @param iMinLevelToCheck Nivel mínimo donde puede spawnear
	 * @param bUseBorderPointIfFails Si no encuentra punto buscará un punto normal en los bordes del mapa
	 * @return
	 */
	public static Point3DShort getRandomUndergroundPoint (int iASZI, int iMinLevelToCheck, int maxLevelDiscovered, boolean bUseBorderPointIfFails) {
		if (iASZI == -1) {
			return null;
		}

		int iIndexLevel = Math.min (maxLevelDiscovered, (cells[0][0].length - 1));

		// Buscamos puntos a random desde abajo del todo hasta el tope
		int x, y;
		Cell cell = null;
		while (iIndexLevel > iMinLevelToCheck) {
			// Miramos 200 randoms como máximo (MAP_WIDTH)
			int iTries = MAP_WIDTH;

			while (iTries > 0) {
				iTries--;

				switch (Utils.getRandomBetween (1, 4)) {
					case 1:
						// Norte
						x = Utils.getRandomBetween (0, (MAP_WIDTH - 1));
						y = 0;
						break;
					case 2:
						// Sur
						x = Utils.getRandomBetween (0, (MAP_WIDTH - 1));
						y = MAP_HEIGHT - 1;
						break;
					case 3:
						// Este
						x = MAP_WIDTH - 1;
						y = Utils.getRandomBetween (0, (MAP_HEIGHT - 1));
						break;
					default: // 4
						// Oeste
						x = 0;
						y = Utils.getRandomBetween (0, (MAP_HEIGHT - 1));
						break;
				}

				cell = cells[x][y][iIndexLevel];
				if (cell.isDiscovered () && cell.getAstarZoneID () == iASZI) {
					// BINGO !
					return Point3DShort.getPoolInstance (cell.getCoordinates ());
				}
			}

			iIndexLevel--;
		}

		// Si llega aquí es que no hemos encontrado una casilla a random, recorremos el mundo a manija
		iIndexLevel = Math.min (maxLevelDiscovered, (cells[0][0].length - 1));
		for (int z = iIndexLevel; z > iMinLevelToCheck; z--) {
			for (x = 0; x < MAP_WIDTH; x++) {
				// Norte y sur
				y = 0;
				cell = cells[x][y][z];
				if (cell.isDiscovered () && cell.getAstarZoneID () == iASZI) {
					// BINGO !
					return Point3DShort.getPoolInstance (cell.getCoordinates ());
				}

				y = MAP_HEIGHT - 1;
				cell = cells[x][y][z];
				if (cell.isDiscovered () && cell.getAstarZoneID () == iASZI) {
					// BINGO !
					return Point3DShort.getPoolInstance (cell.getCoordinates ());
				}
			}

			for (y = 0; y < MAP_HEIGHT; y++) {
				// Este y oeste
				x = MAP_WIDTH - 1;
				cell = cells[x][y][z];
				if (cell.isDiscovered () && cell.getAstarZoneID () == iASZI) {
					// BINGO !
					return Point3DShort.getPoolInstance (cell.getCoordinates ());
				}

				x = 0;
				cell = cells[x][y][z];
				if (cell.isDiscovered () && cell.getAstarZoneID () == iASZI) {
					// BINGO !
					return Point3DShort.getPoolInstance (cell.getCoordinates ());
				}
			}
		}

		// Si llega aquí es que nada de nada
		if (bUseBorderPointIfFails) {
			return getRandomBorderPoint (iASZI);
		}

		return null;
	}


	/**
	 * Devuelve un punto a random, que estará en los bordes y en el nivel 0, basándose en el ASZI. Se usa en la llegada de inmigrantes/heroes.
	 * 
	 * @param iASZID
	 * @return
	 */
	public static Point3DShort getRandomBorderPoint (int iASZI) {
		if (iASZI == -1) {
			return null;
		}

		// Miramos (MAP_WIDTH + MAP_HEIGHT) / 2 puntos a random en las 4 coordenadas (norte, sur, ...)
		// Si así no encontramos nos recorreremos todo el mapa
		int iCount = (MAP_WIDTH + MAP_HEIGHT) / 2;
		int iCardinal, iRandom;
		Cell cell;
		while (iCount > 0) {
			iCardinal = Utils.getRandomBetween (1, 4); // 1,2,3,4 .... norte,sur,este,oeste
			int z = World.MAP_NUM_LEVELS_OUTSIDE - 1;
			if (iCardinal == 1) { // Norte
				iRandom = Utils.getRandomBetween (0, MAP_WIDTH - 1);
				cell = getCell (iRandom, 0, z);
				while (cell.getAstarZoneID () != iASZI && z > 0) {
					z--;
					cell = getCell (iRandom, 0, z);
				}
				if (cell.getAstarZoneID () == iASZI) {
					return Point3DShort.getPoolInstance (cell.getCoordinates ());
				}
			} else if (iCardinal == 2) { // Sur
				iRandom = Utils.getRandomBetween (0, MAP_WIDTH - 1);
				cell = getCell (iRandom, MAP_HEIGHT - 1, z);
				while (cell.getAstarZoneID () != iASZI && z > 0) {
					z--;
					cell = getCell (iRandom, MAP_HEIGHT - 1, z);
				}
				if (cell.getAstarZoneID () == iASZI) {
					return Point3DShort.getPoolInstance (cell.getCoordinates ());
				}
			} else if (iCardinal == 3) { // Este
				iRandom = Utils.getRandomBetween (0, MAP_HEIGHT - 1);
				cell = getCell (MAP_WIDTH - 1, iRandom, z);
				while (cell.getAstarZoneID () != iASZI && z > 0) {
					z--;
					cell = getCell (MAP_WIDTH - 1, iRandom, z);
				}
				if (cell.getAstarZoneID () == iASZI) {
					return Point3DShort.getPoolInstance (cell.getCoordinates ());
				}
			} else { // Oeste
				iRandom = Utils.getRandomBetween (0, MAP_HEIGHT - 1);
				cell = getCell (0, iRandom, z);
				while (cell.getAstarZoneID () != iASZI && z > 0) {
					z--;
					cell = getCell (0, iRandom, z);
				}
				if (cell.getAstarZoneID () == iASZI) {
					return Point3DShort.getPoolInstance (cell.getCoordinates ());
				}
			}

			iCount--;
		}

		// Si llega aquí es que los randoms no han funcionado, nos recorremos todo el mundo
		// Norte/sur
		for (int x = 0; x < MAP_WIDTH; x++) {
			// Norte
			int z = World.MAP_NUM_LEVELS_OUTSIDE - 1;
			cell = getCell (x, 0, z);
			while (cell.getAstarZoneID () != iASZI && z > 0) {
				z--;
				cell = getCell (x, 0, z);
			}
			if (cell.getAstarZoneID () == iASZI) {
				return Point3DShort.getPoolInstance (cell.getCoordinates ());
			}

			// Sur
			z = World.MAP_NUM_LEVELS_OUTSIDE - 1;
			cell = getCell (x, MAP_HEIGHT - 1, z);
			while (cell.getAstarZoneID () != iASZI && z > 0) {
				z--;
				cell = getCell (x, MAP_HEIGHT - 1, z);
			}
			if (cell.getAstarZoneID () == iASZI) {
				return Point3DShort.getPoolInstance (cell.getCoordinates ());
			}
		}
		// Este/oeste
		for (int y = 0; y < MAP_HEIGHT; y++) {
			// Este
			int z = World.MAP_NUM_LEVELS_OUTSIDE - 1;
			cell = getCell (MAP_WIDTH - 1, y, z);
			while (cell.getAstarZoneID () != iASZI && z > 0) {
				z--;
				cell = getCell (MAP_WIDTH - 1, y, z);
			}
			if (cell.getAstarZoneID () == iASZI) {
				return Point3DShort.getPoolInstance (cell.getCoordinates ());
			}

			// Oeste
			z = World.MAP_NUM_LEVELS_OUTSIDE - 1;
			cell = getCell (0, y, z);
			while (cell.getAstarZoneID () != iASZI && z > 0) {
				z--;
				cell = getCell (0, y, z);
			}
			if (cell.getAstarZoneID () == iASZI) {
				return Point3DShort.getPoolInstance (cell.getCoordinates ());
			}
		}

		// No existe camino
		return null;
	}


	// private void checkGodsStatus () {
	// if (!Game.isDisabledGodsON () && getGods () != null) {
	// GodData gd;
	// GodManagerItem gmi;
	// int iNumItemsLike, iNumItemsDislike, iNumItems;
	// for (int i = 0; i < getGods ().size (); i++) {
	// gd = getGods ().get (i);
	// gmi = GodManager.getItem (gd.getGodID ());
	//
	// // Miramos si el status sube o baja
	// if (gmi != null) {
	// // Primero de todo actualizamos las horas desde el último evento
	// gd.setHoursLastEvent (gd.getHoursLastEvent () + 1);
	//
	// // Objetos que le gustan
	// iNumItemsLike = 0;
	// if (gmi.getItemsLike () != null) {
	// for (int j = 0; j < gmi.getItemsLike ().size (); j++) {
	// iNumItems = Item.getNumItemsTotal (gmi.getItemsLike ().get (j));
	// iNumItems = (iNumItems * gmi.getItemsLikePCT ().get (j)) / 100;
	// iNumItemsLike += iNumItems;
	// }
	// }
	//
	// // Objetos que NO le gustan
	// iNumItemsDislike = 0;
	// if (gmi.getItemsDislike () != null) {
	// for (int j = 0; j < gmi.getItemsDislike ().size (); j++) {
	// iNumItems = Item.getNumItemsTotal (gmi.getItemsDislike ().get (j));
	// iNumItems = (iNumItems * gmi.getItemsDislikePCT ().get (j)) / 100;
	// iNumItemsDislike += iNumItems;
	// }
	// }
	//
	// // Si hay más like que dislike el status sube, en caso contrario baja
	// // Se tiene en cuanta el status actual para ver si sube o baja
	// boolean bStatusChanged = false;
	// if (iNumItemsLike > iNumItemsDislike) {
	// if (Utils.launchDice (1, 100) <= (((100 - gd.getStatus ()) / 2) + 1)) {
	// // Sube
	// gd.setStatus (gd.getStatus () + 1);
	// bStatusChanged = true;
	// }
	// } else if (iNumItemsLike < iNumItemsDislike) {
	// if (Utils.launchDice (1, 100) <= (gd.getStatus () / 2)) {
	// // Sube
	// gd.setStatus (gd.getStatus () - 1);
	// bStatusChanged = true;
	// }
	// }
	//
	// if (!bStatusChanged) {
	// bStatusChanged = gd.getStatus () <= 2 || gd.getStatus () >= 99;
	// }
	//
	// int iAfterStatus = gd.getStatus ();
	// if (bStatusChanged && gd.getHoursLastEvent () > 24 * 14) { // 2 semanas
	// // Lanzamos eventos si hace falta
	// ArrayList<String> alEvents = null;
	// if (gd.getStatus () <= 20) {
	// if (gd.getStatus () <= 2) {
	// // Really angry
	// alEvents = gmi.getEventsWhenReallyAngry ();
	// if (alEvents == null || alEvents.size () == 0) {
	// alEvents = gmi.getEventsWhenAngry ();
	// }
	// iAfterStatus = 20;
	// } else {
	// // Just angry
	// alEvents = gmi.getEventsWhenAngry ();
	// }
	// } else if (gd.getStatus () >= 80) {
	// if (gd.getStatus () >= 99) {
	// // Really happy
	// alEvents = gmi.getEventsWhenReallyHappy ();
	// if (alEvents == null || alEvents.size () == 0) {
	// alEvents = gmi.getEventsWhenHappy ();
	// }
	// iAfterStatus = 80;
	// } else {
	// // Just happy
	// alEvents = gmi.getEventsWhenHappy ();
	// }
	// }
	//
	// if (alEvents != null && alEvents.size () > 0) {
	// // BAM, lanzamos evento
	// String sEvent = alEvents.get (Utils.getRandomBetween (0, (alEvents.size () - 1)));
	// EventManagerItem emi = EventManager.getItem (sEvent);
	// if (emi != null) {
	// if (Game.getWorld ().addEvent (emi)) {
	// gd.setHoursLastEvent (0);
	// gd.setHidden (false);
	// gd.setStatus (iAfterStatus);
	//									MessagesPanel.addMessage (MessagesPanel.TYPE_ANNOUNCEMENT, gd.getFullName () + " " + Messages.getString("World.23") + " " + emi.getName (), ColorGL.ORANGE); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	// }
	// }
	// }
	// }
	// }
	// }
	// }
	// }
	public void keyPressed (int tecla, int fn) {
		// if (tecla == Keyboard.KEY_W || tecla == Keyboard.KEY_S || tecla == Keyboard.KEY_A || tecla == Keyboard.KEY_D || tecla == Keyboard.KEY_UP || tecla == Keyboard.KEY_DOWN || tecla == Keyboard.KEY_LEFT || tecla == Keyboard.KEY_RIGHT) {
		if (fn == UtilsKeyboard.FN_UP || fn == UtilsKeyboard.FN_DOWN || fn == UtilsKeyboard.FN_LEFT || fn == UtilsKeyboard.FN_RIGHT) {
			// Cursores
			int y = getView ().y;
			int x = getView ().x;
			if (fn == UtilsKeyboard.FN_UP) {
				y--;
				x++;
			} else if (fn == UtilsKeyboard.FN_DOWN) {
				y++;
				x--;
			} else if (fn == UtilsKeyboard.FN_LEFT) {
				y--;
				x--;
			} else {
				// Right
				y++;
				x++;
			}

			if (x >= 0 && y >= 0 && x < MAP_WIDTH && y < MAP_HEIGHT) {
				setView (x, y);

				// Tutorial flow
				if (fn == UtilsKeyboard.FN_UP) {
					Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_WASD, TutorialTrigger.WASD_UP, null);
				} else if (fn == UtilsKeyboard.FN_DOWN) {
					Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_WASD, TutorialTrigger.WASD_DOWN, null);
				} else if (fn == UtilsKeyboard.FN_LEFT) {
					Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_WASD, TutorialTrigger.WASD_LEFT, null);
				} else {
					Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_WASD, TutorialTrigger.WASD_RIGHT, null);
				}
			}
		} else if (fn == UtilsKeyboard.FN_PAUSE) { // Pause
			Game.togglePause (true);

			// Tutorial flow
			Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_PAUSE, null);
		} else if (tecla == Keyboard.KEY_ESCAPE) { // Cancelar tarea
			if (Game.getCurrentState () == Game.STATE_CREATING_TASK) {
				Game.deleteCurrentTask ();
			} else if (Game.getCurrentState () == Game.STATE_SHOWING_CONTEXT_MENU) {
				Game.deleteCurrentContextMenu ();
			} else {
				if (!UIPanelInputHandler.keyPressed (tecla)) {
					if (Game.getCurrentState () == Game.STATE_NO_STATE) {
						// Menu
						CommandPanel.executeCommand (CommandPanel.COMMAND_BACK, null, null, null, null, 0);
					}
				}
			}
		} else if (fn == UtilsKeyboard.FN_LEVEL_UP) { // Sube level
			CommandPanel.executeCommand (CommandPanel.COMMAND_LEVEL_UP, null, null, null, null, 0);
		} else if (fn == UtilsKeyboard.FN_LEVEL_DOWN) { // Baja level
			CommandPanel.executeCommand (CommandPanel.COMMAND_LEVEL_DOWN, null, null, null, null, 0);
		} else if (fn == UtilsKeyboard.FN_NEXT_CITIZEN) { // Siguiente aldeano
			CommandPanel.executeCommand (CommandPanel.COMMAND_NEXT_CITIZEN, null, null, null, null, 0);
		} else if (fn == UtilsKeyboard.FN_PREVIOUS_CITIZEN) { // Anterior aldeano
			CommandPanel.executeCommand (CommandPanel.COMMAND_PREVIOUS_CITIZEN, null, null, null, null, 0);
		} else if (fn == UtilsKeyboard.FN_NEXT_SOLDIER) { // Siguiente soldado
			CommandPanel.executeCommand (CommandPanel.COMMAND_NEXT_SOLDIER, null, null, null, null, 0);
		} else if (fn == UtilsKeyboard.FN_PREVIOUS_SOLDIER) { // Anterior soldado
			CommandPanel.executeCommand (CommandPanel.COMMAND_PREVIOUS_SOLDIER, null, null, null, null, 0);
		} else if (fn == UtilsKeyboard.FN_NEXT_HERO) { // Siguiente héroe
			CommandPanel.executeCommand (CommandPanel.COMMAND_NEXT_HERO, null, null, null, null, 0);
		} else if (fn == UtilsKeyboard.FN_PREVIOUS_HERO) { // Anterior héroe
			CommandPanel.executeCommand (CommandPanel.COMMAND_PREVIOUS_HERO, null, null, null, null, 0);
		} else if (fn == UtilsKeyboard.FN_TOGGLE_MINIBLOCKS) { // Miniblocks
			CommandPanel.executeCommand (CommandPanel.COMMAND_MINIBLOCKS, null, null, null, null, 0);
		} else if (fn == UtilsKeyboard.FN_BOT_1) { // Bottom 1
			executeBottomMenu (1);
		} else if (fn == UtilsKeyboard.FN_BOT_2) { // Bottom 2
			executeBottomMenu (2);
		} else if (fn == UtilsKeyboard.FN_BOT_3) { // Bottom 2
			executeBottomMenu (3);
		} else if (fn == UtilsKeyboard.FN_BOT_4) { // Bottom 2
			executeBottomMenu (4);
		} else if (fn == UtilsKeyboard.FN_BOT_5) { // Bottom 2
			executeBottomMenu (5);
		} else if (fn == UtilsKeyboard.FN_BOT_6) { // Bottom 2
			executeBottomMenu (6);
		} else if (fn == UtilsKeyboard.FN_BOT_7) { // Bottom 2
			executeBottomMenu (7);
		} else if (fn == UtilsKeyboard.FN_BOT_8) { // Bottom 2
			executeBottomMenu (8);
		} else if (fn == UtilsKeyboard.FN_BOT_9) { // Bottom 2
			executeBottomMenu (9);
		} else if (fn == UtilsKeyboard.FN_BOT_10) { // Bottom 2
			executeBottomMenu (10);
		}
	}


	private void executeBottomMenu (int iIndex) {
		SmartMenu smMenu = Game.getPanelUI ().getCurrentMenu ();
		if (smMenu != null) {
			ArrayList<SmartMenu> smItems = smMenu.getItems ();
			if (smItems != null && smItems.size () >= iIndex) {
				SmartMenu smItem = smItems.get (iIndex - 1);
				CommandPanel.executeCommand (smItem.getCommand (), smItem.getParameter (), smItem.getParameter2 (), smItem.getDirectCoordinates (), smItem.getIcon (), smItem.getIconType ());
			}
		}
	}


	public static Tile getTileMouseCursor (boolean bMined) {
		return tileMouseCursor.getMouseCursor (bMined);
	}


	public static Tile getTileMouseCursorBlock () {
		return tileMouseCursor.getMouseCursorBlock ();
	}


	public static Tile getTileMouseCursorMiniBlock () {
		return tileMouseCursor.getMouseCursorMiniBlock ();
	}


	public static Tile getTileMouseCursorAir () {
		return tileMouseCursor.getMouseCursorAir ();
	}


	public static Tile getTileMouseCursorBAD (boolean bMined) {
		return tileMouseCursorBAD.getMouseCursor (bMined);
	}


	public static Tile getTileMouseCursorBADMiniBlock () {
		return tileMouseCursorBAD.getMouseCursorMiniBlock ();
	}


	public static Tile getTileUnknown () {
		return tileUnknown;
	}


	public static Tile getTileUnknownMini () {
		return tileUnknownMini;
	}


	public static Tile getTileWater (int iCount) {
		return tileWater.getWaterCursor (iCount);
	}


	public static Lava getTileLava () {
		return tileLava;
	}


	public static Tile getTileLava (int iCount) {
		return tileLava.getLavaCursor (iCount);
	}


	public static Tile getTileOrders (boolean bMined) {
		return tileOrders.getOrderTile (bMined);
	}


	public static Tile getTileOrdersMiniBlock () {
		return tileOrders.getOrderTileMiniBlock ();
	}


	public static RedCross getTileRedCross () {
		return tileRedCross;
	}


	public static CitSleeping getTileCitizenSleeping () {
		return tileCitizenSleeping;
	}


	public static CitEating getTileCitizenEating () {
		return tileCitizenEating;
	}


	public static CitExclamation getTileCitizenExclamation () {
		return tileCitizenExclamation;
	}


	public static Tile getTileStockpile () {
		return tileStockpile;
	}


	public static Tile getTilePatrolMark () {
		return tilePatrolMark;
	}


	/**
	 * Comprueba una celda y pone/quita el flag de patrol si hace falta
	 * 
	 * @param p3d
	 */
	public static void checkFlagPatrolPoint (Point3DShort p3d) {
		// Soldados
		Citizen cit;
		for (int i = 0; i < getSoldierIDs ().size (); i++) {
			cit = (Citizen) getLivingEntityByID (getSoldierIDs ().get (i));
			if (cit != null) {
				if (cit.getSoldierData ().getState () == SoldierData.STATE_PATROL) {
					if (cit.getSoldierData ().getPatrolPoints ().contains (p3d)) {
						World.getCell (p3d).setFlagPatrol (true);
						return;
					}
				}
			}
		}

		// Grupos
		SoldierGroupData sgd;
		for (int i = 0; i < SoldierGroups.MAX_GROUPS; i++) {
			sgd = Game.getWorld ().getSoldierGroups ().getGroup (i);
			if (sgd.getPatrolPoints () != null && sgd.getPatrolPoints ().contains (p3d)) {
				World.getCell (p3d).setFlagPatrol (true);
				return;
			}
		}

		World.getCell (p3d).setFlagPatrol (false);
	}


	public static void updateSpecialTilesAnimation () {
		tileUnknown.updateAnimation (false);
		tileUnknownMini.updateAnimation (false);
		tileWater.updateAnimation ();
		tileLava.updateAnimation ();
		tileRedCross.updateAnimation (false);
		tileStockpile.updateAnimation (false);
		tilePatrolMark.updateAnimation (false);
		tileCitizenEating.updateAnimation (false);
		tileCitizenSleeping.updateAnimation (false);
		tileCitizenExclamation.updateAnimation (false);
	}


	/**
	 * Genera el movimiento de fluidos
	 * 
	 * 
	 */
	private void moveFluids (boolean bPreloading) {
		// if (fluidCellsToProcess.size () > 0) {
		// System.out.println (fluidCellsToProcess.size ());
		// }

		// Seteamos los zone IDs en caso de que ya no se muevan fluidos y el contador de fluidos movidos sea mayor que 0
		// También si el contador de fluidos llega a 128 (por ejemplo)
		if (!bPreloading) {
			if (fluidsMoved) {
				fluidMovedCounter++;
			}

			if (fluidMovedCounter >= 128) {
				// XCN Cell.setAllZoneIDs ();
				setRecheckASZID (true);
				fluidMovedCounter = 0;
				fluidsMoved = false;
			}
		}

		if (fluidCellsToProcess.size () == 0) {
			return;
		}

		// Por cada celda con fluidos miramos a donde se puede mover
		// A cada casilla movida destruimos lo que contenga
		Point3DShort p3dSource, p3dDestination, p3dTemp;
		Cell cellSource, cellDestination;
		ArrayList<Point3DShort> alNewPoints = new ArrayList<Point3DShort> ();

		int iCounterMoved = 0;
		int iCounterNotMoved = 0;
		while (iCounterMoved < FLUIDS_MOVED_PER_INVOCATION && iCounterNotMoved < FLUIDS_NOT_MOVED_PER_INVOCATION && fluidCellsToProcess.size () > 0) {
			p3dSource = fluidCellsToProcess.remove (Utils.getRandomBetween (0, (fluidCellsToProcess.size () - 1) / 4)); // Random del primer 25% de tiles

			// Miramos si está dentro del mapa
			// if (!Utils.isInsideMap (p3dSource)) {
			// continue;
			// }
			cellSource = cells[p3dSource.x][p3dSource.y][p3dSource.z];
			cellSource.setFluidCheckList (false);

			// Miramos a donde puede moverse (si es que puede)
			p3dDestination = checkFluidMovement (p3dSource);
			if (p3dDestination != null) {

				// Movemos el fluido
				cellDestination = cells[p3dDestination.x][p3dDestination.y][p3dDestination.z];

				if (moveSingleFluid (cellSource, cellDestination, bPreloading, p3dSource, p3dDestination, alNewPoints)) {
					// De fluido a fluido no sumamos el contador
					fluidsMoved = true;
					iCounterMoved++;
				} else {
					// De fluido a fluido, no cuenta
					iCounterNotMoved++;
				}
			} else {
				// Si la source tiene un fluidsElevator la volvemos a meter
				Item item = cellSource.getItem ();
				if (item != null && ItemManager.getItem (item.getIniHeader ()).isFluidsElevator ()) {
					if (!alNewPoints.contains (p3dSource)) {
						alNewPoints.add (p3dSource);
					}
				}
			}
		}

		// Todo procesado, metemos las nuevas casillas a procesar
		for (int i = 0; i < alNewPoints.size (); i++) {
			p3dTemp = alNewPoints.get (i);
			if (!cells[p3dTemp.x][p3dTemp.y][p3dTemp.z].isFluidCheckList () && cells[p3dTemp.x][p3dTemp.y][p3dTemp.z].getTerrain ().hasFluids ()) {
				// if (!fluidCellsToProcess.contains (alNewPoints.get (i))) {
				fluidCellsToProcess.add (alNewPoints.get (i));
				cells[p3dTemp.x][p3dTemp.y][p3dTemp.z].setFluidCheckList (true);
				// }
			}
		}
	}


	/**
	 * Mueve el fluido de una celda origen (se asume que hay fluido ahí) a una destino, y hace las cosas necesarias (borrar items de celdas, minimapa, ...)
	 * 
	 */
	public static boolean moveSingleFluid (Cell cellSource, Cell cellDestination, boolean bPreloading, Point3DShort p3dSource, Point3DShort p3dDestination, ArrayList<Point3DShort> alNewPoints) {
		boolean bRefreshCounter = true;
		// Metemos el nuevo fluido
		boolean bMovedFromFluidToFluid = cellDestination.getTerrain ().hasFluids ();
		if (!bMovedFromFluidToFluid) {
			// Celda destino sin fluidos
			cellDestination.getTerrain ().setFluidType (cellSource.getTerrain ().getFluidType ());
			if (bPreloading) {
				cellDestination.getTerrain ().setFluidCount (cellSource.getTerrain ().getFluidCount ());
			} else {
				cellDestination.getTerrain ().setFluidCount (1);
				cellDestination.setAstarZoneID (-1);
			}
			World.checkNewEvaporation (cellDestination);
		} else {
			// Celda destino ya tenía fluidos
			// fluidMovedCounter--; // No cuenta para los A*zoneID
			bRefreshCounter = false; // No cuenta para los A*zoneID

			if (cellSource.getTerrain ().getFluidType () != cellDestination.getTerrain ().getFluidType ()) {
				// Fluidos distintos
				cellDestination.getTerrain ().setFluidCount (cellDestination.getTerrain ().getFluidCount () - 1);
				if (cellDestination.getTerrain ().getFluidCount () == 0) {
					cellDestination.getTerrain ().setFluidType (cellSource.getTerrain ().getFluidType ());
					cellDestination.getTerrain ().setFluidCount (1);
				}
				World.checkNewEvaporation (cellDestination);
			} else {
				// Fluidos iguales
				if (bPreloading) {
					cellDestination.getTerrain ().setFluidCount (cellSource.getTerrain ().getFluidCount ());
				} else {
					cellDestination.getTerrain ().setFluidCount (cellDestination.getTerrain ().getFluidCount () + 1);
				}
				World.checkNewEvaporation (cellDestination);
			}
		}
		Cell.setShouldPaintUnders (World.getCells (), p3dDestination);

		// Restamos 1 al counter de la celda origen (en caso de que no sea infinito)
		if (!bPreloading) {
			if (cellSource.getTerrain ().getFluidCount () != 0 && cellSource.getTerrain ().getFluidCount () != Terrain.FLUIDS_COUNT_INFINITE) {
				cellSource.getTerrain ().setFluidCount (cellSource.getTerrain ().getFluidCount () - 1);
				World.checkNewEvaporation (cellSource);

				Cell.setShouldPaintUnders (World.getCells (), p3dSource);
				// Si la celda origen se ha quedado sin fluidos hay que tener en cuenta los ASZID
				if (cellSource.getTerrain ().getFluidCount () == 0) {
					// Cell.mergeZoneID (cellSource.getCoordinates ());
					// fluidMovedCounter++;
					bRefreshCounter = true;
				}
			}
		}

		// Minimapa
		MiniMapPanel.setMinimapReload (p3dDestination.z);

		if (!bMovedFromFluidToFluid) {
			// Borramos cosas
			deleteCellAll (cellDestination, true);
		}

		// Metemos la celda origen + vecinas y la destino + vecinas en la lista para re-procesarse
		// Origen
		if (!alNewPoints.contains (p3dSource)) {
			alNewPoints.add (p3dSource);
		}
		Point3DShort p3dTemp;

		// Vecinas origen
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				if (i != 0 || j != 0) {
					p3dTemp = Point3DShort.getPoolInstance (p3dSource.x + i, p3dSource.y + j, p3dSource.z);
					if (Utils.isInsideMap (p3dTemp) && !alNewPoints.contains (p3dTemp)) {
						alNewPoints.add (p3dTemp);
					}
				}
			}
		}
		if (p3dSource.z > 0) {
			p3dTemp = Point3DShort.getPoolInstance (p3dSource.x, p3dSource.y, p3dSource.z - 1);
			if (Utils.isInsideMap (p3dTemp) && !alNewPoints.contains (p3dTemp)) {
				alNewPoints.add (p3dTemp);
			}
		}

		// Destino
		if (!alNewPoints.contains (p3dDestination)) {
			alNewPoints.add (p3dDestination);
		}

		return bRefreshCounter;
	}


	/**
	 * Evapora fluidos con fuerza 1 y sin fluidos con más fuerza a los lados Evapora el 12.5% primero de fluids
	 */
	private void evaporateFluids () {
		Cell cell;
		Point3DShort p3ds;
		if (fluidEvaporation.size () > 0) {
			int iNumFluidsToEvaporate = (fluidEvaporation.size () / 8) + 1; // 12.5%
			if (iNumFluidsToEvaporate < 2) {
				iNumFluidsToEvaporate = 2;
			} else if (iNumFluidsToEvaporate > FLUIDS_MAX_EVAPORATION) {
				iNumFluidsToEvaporate = FLUIDS_MAX_EVAPORATION;
			}
			if (iNumFluidsToEvaporate > fluidEvaporation.size ()) {
				iNumFluidsToEvaporate = fluidEvaporation.size ();
			}

			while (iNumFluidsToEvaporate > 0 && fluidEvaporation.size () > 0) {
				iNumFluidsToEvaporate--;

				p3ds = fluidEvaporation.remove (0);
				cell = getCell (p3ds);

				// Fuerza 1
				if (cell.getTerrain ().getFluidCount () == 1) {
					// Miramos que no haya fluidos con más fuerza a los lados
					boolean bMasFuerza = false;
					forvecinas: for (int x = -1; x <= 1; x++) {
						for (int y = -1; y <= 1; y++) {
							if (x != 0 || y != 0) {
								if (Utils.isInsideMap (cell.getCoordinates ().x + x, cell.getCoordinates ().y + y, cell.getCoordinates ().z)) {
									int iStr = World.getCell (cell.getCoordinates ().x + x, cell.getCoordinates ().y + y, cell.getCoordinates ().z).getTerrain ().getFluidCount ();
									if (iStr == 1) {
										bMasFuerza = false;
										break forvecinas;
									} else if (iStr > 1) {
										bMasFuerza = true;
									}
								}
							}
						}
					}
					// if (!bMasFuerza) {
					// // Miramos arriba también
					// if (cell.getCoordinates ().z > 0) {
					// if (World.getCell (cell.getCoordinates ().x, cell.getCoordinates ().y, cell.getCoordinates ().z - 1).getTerrain ().getFluidCount () > 1) {
					// bMasFuerza = true;
					// }
					// }
					// }

					if (!bMasFuerza) {
						// BAM
						cell.getTerrain ().setFluidType (Terrain.FLUIDS_NONE);
						cell.getTerrain ().setFluidCount (0);
						Cell.mergeZoneID (p3ds, false);

						addFluidCellToProcess (p3ds.x, p3ds.y, p3ds.z, true);

						Point3DShort.returnToPool (p3ds);
					} else {
						fluidEvaporation.add (p3ds);
					}
				} else {
					Point3DShort.returnToPool (p3ds);
				}
			}
		}
	}


	/**
	 * Chequea y mete, si hace falta, las coordenadas de la celda en la lista de celdas a evaporarse
	 * 
	 * @param cell
	 */
	public static void checkNewEvaporation (Cell cell) {
		boolean bEvaporation = cell.getTerrain ().getFluidCount () == 1;

		if (bEvaporation) {
			if (!fluidEvaporation.contains (cell.getCoordinates ())) {
				fluidEvaporation.add (Point3DShort.getPoolInstance (cell.getCoordinates ()));
			}
		}
	}


	/**
	 * Borra el contenido de una celda. Se usa con los fluidos o con las slopes
	 * 
	 * @param cells
	 * @param cell
	 * @param bFluids Indica si hay que borrar por cul`pa de fluidos
	 * @param bRaisedLowered Indica si hay que borrar debido a un raise/lower del terreno
	 */
	public static void deleteCellAll (Cell cell, boolean bFluids) {
		Point3DShort p3d = cell.getCoordinates ();

		// Punto de stockpile borrado (se borra sólo si fluids)
		if (bFluids) {
			Stockpile.deleteStockpilePoint (p3d);
		}

		// Punto de Zone borrado (se borra tanto fluidscomo raise/Lower)
		if (Zone.deleteZonePoint (p3d)) {
			Cell.mergeZoneID (p3d, false);
		}

		// Borramos cosas
		if (!cell.isEmpty ()) {
			if (cell.hasItem ()) {
				// Item (sólo se borra si fluids)
				if (bFluids) {
					Item item = (Item) cell.getEntity ();
					if (item != null) {
						// Miramos que no sea un fluids blocker, fluid elevator o allow fluids
						ItemManagerItem imi = ItemManager.getItem (item.getIniHeader ());
						if (imi != null) {
							if (!imi.isBlockFluids () && !imi.isFluidsElevator () && !imi.isAllowFluids ()) {
								item.delete ();
							}
						} else {
							item.delete ();
						}
					}
				}
			} else if (cell.hasBuilding ()) {
				// Building (se borra tanto fluids como raiseLower)
				Point3DShort p3dBuilding = cell.getBuildingCoordinates ();
				if (p3dBuilding != null) {
					Building.delete (p3dBuilding);
				}
			} else if (cell.hasEntity ()) {
				// Cualquier otra cosa (sólo fluids, a revisar)
				if (bFluids) {
					cell.getEntity ().delete ();
				}
			}
		}
	}


	/**
	 * Devuelve la coordenada de la celda destino donde el fluido debe moverse o null
	 * 
	 * @param p3d
	 * @return
	 */
	private Point3DShort checkFluidMovement (Point3DShort p3d) {
		// Miramos si tiene fluidos
		Cell cell = cells[p3d.x][p3d.y][p3d.z];
		if (!cell.getTerrain ().hasFluids ()) {
			return null;
		}

		// Celda con fluidos, miramos primero si puede mover agua hacia abajo
		if (p3d.z < (World.MAP_DEPTH - 1)) {
			if (cell.isDigged ()) {
				if (checkFluidMovementCells (cell, p3d.x, p3d.y, p3d.z + 1) > 0) {
					// Todo ok, comprobamos los fluid elevator, sólo en este caso (fluidos de arriba a abajo)
					Item item = cells[p3d.x][p3d.y][p3d.z + 1].getItem ();
					if (item != null) {
						ItemManagerItem imi = ItemManager.getItem (item.getIniHeader ());
						if (imi == null || !imi.isFluidsElevator ()) {
							return Point3DShort.getPoolInstance (p3d.x, p3d.y, p3d.z + 1);
						}
					} else {
						return Point3DShort.getPoolInstance (p3d.x, p3d.y, p3d.z + 1);
					}
				}
			}
		}

		// Vamos a ver si puede ir hacia arriba debido a un fluidsElevator
		if (p3d.z > 0) {
			Item item = cell.getItem ();
			if (item != null) {
				ItemManagerItem imi = ItemManager.getItem (item.getIniHeader ());
				if (imi != null && imi.isFluidsElevator ()) {
					// Bingo
					if (checkFluidMovementCells (cell, p3d.x, p3d.y, p3d.z - 1, true) > 0) {
						return Point3DShort.getPoolInstance (p3d.x, p3d.y, p3d.z - 1);
					}
				}
			}
		}

		// Si llega aquí es que el agua no se va para abajo ni arriba
		// Obtenemos los puntos donde puede moverse
		short bPoints[] = new short [8]; // N,S,E,W,NE,NW,SE,SW

		bPoints[0] = checkFluidMovementCells (cell, p3d.x, p3d.y - 1, p3d.z); // N
		bPoints[1] = checkFluidMovementCells (cell, p3d.x, p3d.y + 1, p3d.z); // S
		bPoints[2] = checkFluidMovementCells (cell, p3d.x + 1, p3d.y, p3d.z); // E
		bPoints[3] = checkFluidMovementCells (cell, p3d.x - 1, p3d.y, p3d.z); // W
		bPoints[4] = checkFluidMovementCells (cell, p3d.x + 1, p3d.y - 1, p3d.z); // NE
		bPoints[5] = checkFluidMovementCells (cell, p3d.x - 1, p3d.y - 1, p3d.z); // NW
		bPoints[6] = checkFluidMovementCells (cell, p3d.x + 1, p3d.y + 1, p3d.z); // SE
		bPoints[7] = checkFluidMovementCells (cell, p3d.x - 1, p3d.y + 1, p3d.z); // SW

		boolean bExists2 = (bPoints[0] == 2 || bPoints[1] == 2 || bPoints[2] == 2 || bPoints[3] == 2 || bPoints[4] == 2 || bPoints[5] == 2 || bPoints[6] == 2 || bPoints[7] == 2);

		if (bExists2 || bPoints[0] > 0 || bPoints[1] > 0 || bPoints[2] > 0 || bPoints[3] > 0 || bPoints[4] > 0 || bPoints[5] > 0 || bPoints[6] > 0 || bPoints[7] > 0) {
			int iRandom = Utils.getRandomBetween (0, 7);
			if (bExists2) {
				while (bPoints[iRandom] != 2) {
					iRandom = Utils.getRandomBetween (0, 7);
				}
			} else {
				while (bPoints[iRandom] == 0) {
					iRandom = Utils.getRandomBetween (0, 7);
				}
			}

			switch (iRandom) {
				case 0:
					return Point3DShort.getPoolInstance (p3d.x, p3d.y - 1, p3d.z);
				case 1:
					return Point3DShort.getPoolInstance (p3d.x, p3d.y + 1, p3d.z);
				case 2:
					return Point3DShort.getPoolInstance (p3d.x + 1, p3d.y, p3d.z);
				case 3:
					return Point3DShort.getPoolInstance (p3d.x - 1, p3d.y, p3d.z);
				case 4:
					return Point3DShort.getPoolInstance (p3d.x + 1, p3d.y - 1, p3d.z);
				case 5:
					return Point3DShort.getPoolInstance (p3d.x - 1, p3d.y - 1, p3d.z);
				case 6:
					return Point3DShort.getPoolInstance (p3d.x + 1, p3d.y + 1, p3d.z);
				case 7:
					return Point3DShort.getPoolInstance (p3d.x - 1, p3d.y + 1, p3d.z);
			}
		}

		return null;
	}


	/**
	 * Mira si el fluido puede pasar de una celda a la otra. Se asume que la celda origen tiene fluidos.
	 * 
	 * @param cellSource
	 * @param x
	 * @param y
	 * @param z
	 * @return 0 si no puede, 1 si puede 2 si puede is tiene preferencia (por diferencias de altura)
	 */
	private short checkFluidMovementCells (Cell cellSource, int x, int y, int z) {
		return checkFluidMovementCells (cellSource, x, y, z, false);
	}


	private short checkFluidMovementCells (Cell cellSource, int x, int y, int z, boolean bElevatorOnSource) {
		if (!Utils.isInsideMap (x, y, z)) {
			return 0;
		}

		// if (z < cellSource.getCoordinates ().z) {
		// return 0;
		// }
		// La destino con infinito o max
		Cell cellDestination = cells[x][y][z];
		if (!cellDestination.isMined () || cellDestination.getTerrain ().getFluidCount () == Terrain.FLUIDS_COUNT_INFINITE || cellDestination.getTerrain ().getFluidCount () == Terrain.FLUIDS_COUNT_MAX) {
			return 0;
		}

		// Caso fluid blocker
		Item item = cellDestination.getItem ();
		if (item != null) {
			ItemManagerItem imi = ItemManager.getItem (item.getIniHeader ());
			if (imi != null) {
				if (imi.isBlockFluids ()) {
					// Bloquea fluidos, miramos si es puerta
					if (imi.isDoor ()) {
						// Es puerta, miramos si está cerrada o bloqueada
						if (item.isDoorStatus (Item.FLAG_WALL_CONNECTOR_STATUS_UNLOCKED_AND_CLOSED) || item.isDoorStatus (Item.FLAG_WALL_CONNECTOR_STATUS_LOCKED_AND_CLOSED)) {
							return 0;
						}
					} else {
						return 0;
					}
				}
			}
		}

		// Caso agujero
		if (z > cellSource.getCoordinates ().z) {
			return 1;
		}

		// Caso elevator
		// 114,77,10
		if (bElevatorOnSource) {
			return 1;
		}

		// General
		int iSourceCount = cellSource.getTerrain ().getFluidCount ();
		int iDestinationCount = cellDestination.getTerrain ().getFluidCount ();
		if (iSourceCount > 1 && iSourceCount > (iDestinationCount + 1)) {
			return 1;
		} else {
			// Si llega aquí es que no se puede mover (por lo menos a una casilla con <2 de fuerza)
			// Vamos a mirar si se puede mover a una casilla con <1 fuerza
			// Sólo podrá si tiene una casilla vecina con >1 fuerza

			// En el caso de fuerza 2 sobre fuerza 1 siempre podremos
			if (iSourceCount == 2 && iDestinationCount == 1) {
				return 1;
			}
			if (iSourceCount == 3 && iDestinationCount == 2) {
				return 1;
			}
			if (iSourceCount > 1 && iSourceCount > iDestinationCount) {
				// Miramos si tiene vecinos con + fuerza
				Point3DShort p3d = cellSource.getCoordinates ();
				Cell cell;
				int iNumCellsSameCount = 0;
				int iTmp = 0;
				if (p3d.z > 0) {
					iTmp = getCell (p3d.x, p3d.y, p3d.z - 1).getTerrain ().getFluidCount ();
					if (iTmp >= iSourceCount) {
						if (iTmp > iSourceCount) {
							return 1;
							// } else {
							// iNumCellsSameCount++;
						}
					}
				}
				if (p3d.x > 0) {
					if (p3d.y > 0) {
						cell = getCell (p3d.x - 1, p3d.y - 1, p3d.z);
						iTmp = (cell.isMined ()) ? cell.getTerrain ().getFluidCount () : iSourceCount;
						if (iTmp >= iSourceCount) {
							if (iTmp > iSourceCount) {
								return 1;
							} else {
								iNumCellsSameCount++;
							}
						}
					}
					cell = getCell (p3d.x - 1, p3d.y, p3d.z);
					iTmp = (cell.isMined ()) ? cell.getTerrain ().getFluidCount () : iSourceCount;
					if (iTmp >= iSourceCount) {
						if (iTmp > iSourceCount) {
							return 1;
						} else {
							iNumCellsSameCount++;
						}
					}
					if (p3d.y < (World.MAP_HEIGHT - 1)) {
						cell = getCell (p3d.x - 1, p3d.y + 1, p3d.z);
						iTmp = (cell.isMined ()) ? cell.getTerrain ().getFluidCount () : iSourceCount;
						if (iTmp >= iSourceCount) {
							if (iTmp > iSourceCount) {
								return 1;
							} else {
								iNumCellsSameCount++;
							}
						}
					}
				}
				if (p3d.x < (World.MAP_WIDTH - 1)) {
					if (p3d.y > 0) {
						cell = getCell (p3d.x + 1, p3d.y - 1, p3d.z);
						iTmp = (cell.isMined ()) ? cell.getTerrain ().getFluidCount () : iSourceCount;
						if (iTmp >= iSourceCount) {
							if (iTmp > iSourceCount) {
								return 1;
							} else {
								iNumCellsSameCount++;
							}
						}
					}
					cell = getCell (p3d.x + 1, p3d.y, p3d.z);
					iTmp = (cell.isMined ()) ? cell.getTerrain ().getFluidCount () : iSourceCount;
					if (iTmp >= iSourceCount) {
						if (iTmp > iSourceCount) {
							return 1;
						} else {
							iNumCellsSameCount++;
						}
					}
					if (p3d.y < (World.MAP_HEIGHT - 1)) {
						cell = getCell (p3d.x + 1, p3d.y + 1, p3d.z);
						iTmp = (cell.isMined ()) ? cell.getTerrain ().getFluidCount () : iSourceCount;
						if (iTmp >= iSourceCount) {
							if (iTmp > iSourceCount) {
								return 1;
							} else {
								iNumCellsSameCount++;
							}
						}
					}
				}
				if (p3d.y > 0) {
					cell = getCell (p3d.x, p3d.y - 1, p3d.z);
					iTmp = (cell.isMined ()) ? cell.getTerrain ().getFluidCount () : iSourceCount;
					if (iTmp >= iSourceCount) {
						if (iTmp > iSourceCount) {
							return 1;
						} else {
							iNumCellsSameCount++;
						}
					}
				}
				if (p3d.y < (World.MAP_HEIGHT - 1)) {
					cell = getCell (p3d.x, p3d.y + 1, p3d.z);
					iTmp = (cell.isMined ()) ? cell.getTerrain ().getFluidCount () : iSourceCount;
					if (iTmp >= iSourceCount) {
						if (iTmp > iSourceCount) {
							return 1;
						} else {
							iNumCellsSameCount++;
						}
					}
				}

				if (iSourceCount > 1 && iSourceCount < Terrain.FLUIDS_COUNT_MAX && iNumCellsSameCount > 3) {
					return 1;
				} else {
					return 0;
				}
			} else {
				return 0;
			}
		}
	}


	public void addFluidCellToProcess (int x, int y, int z, boolean bNeighbours) {
		if (bNeighbours) {
			for (int fx = -1; fx <= 1; fx++) {
				for (int fy = -1; fy <= 1; fy++) {
					if (Utils.isInsideMap (x + fx, y + fy, z)) {
						if (!cells[x + fx][y + fy][z].isFluidCheckList () && cells[x + fx][y + fy][z].getTerrain ().hasFluids ()) {
							fluidCellsToProcess.add (Point3DShort.getPoolInstance (x + fx, y + fy, z));
							cells[x + fx][y + fy][z].setFluidCheckList (true);
						}
					}
				}
			}
			// Arriba
			if (z > 0) {
				if (Utils.isInsideMap (x, y, z - 1)) {
					if (!cells[x][y][z - 1].isFluidCheckList () && cells[x][y][z - 1].getTerrain ().hasFluids ()) {
						fluidCellsToProcess.add (Point3DShort.getPoolInstance (x, y, z - 1));
						cells[x][y][z - 1].setFluidCheckList (true);
					}
				}
			}

			// Abajo
			if ((z + 1) < World.MAP_DEPTH) {
				if (Utils.isInsideMap (x, y, z + 1)) {
					if (!cells[x][y][z + 1].isFluidCheckList () && cells[x][y][z + 1].getTerrain ().hasFluids ()) {
						fluidCellsToProcess.add (Point3DShort.getPoolInstance (x, y, z + 1));
						cells[x][y][z + 1].setFluidCheckList (true);
					}
				}
			}
		} else {
			if (Utils.isInsideMap (x, y, z)) {
				if (!cells[x][y][z].isFluidCheckList () && cells[x][y][z].getTerrain ().hasFluids ()) {
					fluidCellsToProcess.add (Point3DShort.getPoolInstance (x, y, z));
					cells[x][y][z].setFluidCheckList (true);
				}
			}
		}
	}


	public void addFluidCellToProcess (Point3DShort p3d, boolean bNeighbours) {
		addFluidCellToProcess (p3d.x, p3d.y, p3d.z, bNeighbours);
	}


	public boolean isReadyForNextTurn () {
		return readyForNextTurn;
	}


	public void setReadyForNextTurn (boolean readyForNextTurn) {
		this.readyForNextTurn = readyForNextTurn;
	}


	/**
	 * @param readyForNextTurnFrameCounter the readyForNextTurnFrameCounter to set
	 */
	public void setReadyForNextTurnFrameCounter (int readyForNextTurnFrameCounter) {
		this.readyForNextTurnFrameCounter = readyForNextTurnFrameCounter;
	}


	/**
	 * Suma 1 al contador de frames, si llega a X seteamos el nextTurn y el nextTurnTasks
	 */
	public void updateNextFrameTurn () {
		setReadyForNextTurnFrameCounter (getReadyForNextTurnFrameCounter () + 1);

		if (getReadyForNextTurnFrameCounter () >= FRAMES_PER_TURN) {
			setReadyForNextTurnFrameCounter (0);
			if (!Game.isPaused ()) {
				setReadyForNextTurn (true);
			}
			setReadyForNextTurnTasks (true);
		}
	}


	/**
	 * @return the readyForNextTurnFrameCounter
	 */
	public int getReadyForNextTurnFrameCounter () {
		return readyForNextTurnFrameCounter;
	}


	public boolean isReadyForNextTurnTasks () {
		return readyForNextTurnTasks;
	}


	public void setReadyForNextTurnTasks (boolean readyForNextTurnTasks) {
		this.readyForNextTurnTasks = readyForNextTurnTasks;
	}


	public Date getDate () {
		return date;
	}


	/**
	 * Añade enemies al mundo
	 * 
	 * @param numEnemies Número de malos
	 * @param level Nivel de los malos
	 * @param ASZI
	 * 
	 * @return true si ha podido meterlos
	 */
	public static boolean addSiegeEnemies (Point3DShort p3dSpawnPoint, int numEnemies, int level, byte bSiegeType, int iWaitTurns) {
		// Añadimos enemigos en el punto indicado
		LivingEntityManagerItem lemi = LivingEntityManager.getItemByLevel (level, LivingEntity.TYPE_ENEMY, bSiegeType == SiegeData.SIEGE_ROBBERY);
		if (lemi == null) {
			// No existen enemigos de ese level ni inferior (no hay que hacer nada)
			return false;
		}

		return addSiegeEnemies (p3dSpawnPoint, numEnemies, lemi, bSiegeType, iWaitTurns);
	}


	public static boolean addSiegeEnemies (Point3DShort p3dSpawnPoint, int numEnemies, LivingEntityManagerItem lemi, byte bSiegeType, int iWaitTurns) {

		// En caso de robbery hay que mirar que existan items <steal>, livings o containers
		if (bSiegeType == SiegeData.SIEGE_ROBBERY) {
			boolean bSiege = false;
			int[] aiSteal = lemi.getSteal ();
			if (aiSteal != null && aiSteal.length > 0) {
				for (int i = 0; i < aiSteal.length; i++) {
					// Miramos que haya items de estos en el mundo
					if (Item.getNumItemsTotal (UtilsIniHeaders.getStringIniHeader (aiSteal[i]), World.MAP_DEPTH - 1) > 0) {
						// Bingo, la siege se puede producir
						bSiege = true;
						break;
					}
				}
			}

			if (!bSiege) {
				// Si aún no hay siege, miramos que existan containers
				if (Game.getWorld ().getContainers () != null && Game.getWorld ().getContainers ().size () > 0) {
					bSiege = true;
				}
			}

			if (!bSiege) {
				// Miramos livings
				aiSteal = lemi.getStealLivings ();
				if (aiSteal != null && aiSteal.length > 0) {
					for (int i = 0; i < aiSteal.length; i++) {
						// Miramos que haya livings de estos en el mundo
						if (LivingEntity.getNumLivings (UtilsIniHeaders.getStringIniHeader (aiSteal[i]), true) > 0) {
							// Bingo, la siege se puede producir
							bSiege = true;
							break;
						}
					}
				}
			}

			if (!bSiege) {
				return false;
			}
		}

		for (int i = 0; i < numEnemies; i++) {
			LivingEntity le = World.addNewLiving (lemi.getIniHeader (), LivingEntity.TYPE_ENEMY, true, p3dSpawnPoint.x, p3dSpawnPoint.y, p3dSpawnPoint.z, true);
			SiegeData siegeData = new SiegeData (bSiegeType, iWaitTurns, Point3DShort.getPoolInstance (p3dSpawnPoint));
			((Enemy) le).setSiegeData (siegeData);
		}

		return true;
	}


	public ArrayList<Integer> getItemsToBeHauled () {
		return itemsToBeHauled;
	}


	public void addItemToBeHauled (Item item) {
		if (item != null && !item.isLocked ()) {
			String sType = ItemManager.getItem (item.getIniHeader ()).getType ();
			if (sType != null && sType.length () > 0) {
				if (!itemsToBeHauled.contains (item.getID ())) {
					itemsToBeHauled.add (item.getID ());
				}
			}
		}
	}


	public void removeItemToBeHauledByItemID (int iItemID) {
		for (int i = 0; i < itemsToBeHauled.size (); i++) {
			if (itemsToBeHauled.get (i).intValue () == iItemID) {
				itemsToBeHauled.remove (i);
				break;
			}
		}
	}


	public void removeItemToBeHauledByPosition (int iPosition) {
		if (itemsToBeHauled.size () > 0 && iPosition < itemsToBeHauled.size ()) {
			itemsToBeHauled.remove (iPosition);
		}
	}


	/**
	 * Limpia todos los datos (se usa cuando se sale de la partida y se va al menú principal)
	 */
	public void clear () {
		cells = null;
		citizenIDs = new ArrayList<Integer> ();
		soldierIDs = new ArrayList<Integer> ();
		citizenGroups = new CitizenGroups ();
		soldierGroups = new SoldierGroups ();

		heroIDs = new ArrayList<Integer> (2);
		exploringHotPoints = new ArrayList<Point3DShort> ();
		maxHeroXP = 0;
		oldHeroes = new ArrayList<LivingEntity> (2);
		oldHeroesDied = new ArrayList<String> (2);
		currentCaravanData = null;
		enemiesKilled = null;
		projectiles = null;

		livingsDiscovered = new HashMap<Integer, LivingEntity> ();
		livingsUndiscovered = new HashMap<Integer, LivingEntity> ();
		LivingEntity.clear ();

		buildings = null;

		// Items
		fallItemList.clear ();
		items = null;
		itemsText = null;
		itemsToBeHauled = null;
		Item.clear ();

		containers = null;
		stockpiles = null;
		buryData = null;
		zones = null;
		events = null;
		// gods = null;
		globalEvents = null;
		coins = 0;
		view = new Point3D (MAP_WIDTH / 2, MAP_HEIGHT / 2, 0);

		setNumFloorsDiscovered (World.MAP_NUM_LEVELS_OUTSIDE + 1);
		date = null;
		taskManager = null;
		campaignID = null;
		missionID = null;
		Names.clear ();

		// Evaporation list
		Point3DShort p3ds;
		while (World.fluidEvaporation.size () > 0) {
			p3ds = World.fluidEvaporation.remove (World.fluidEvaporation.size () - 1);
			Point3DShort.returnToPool (p3ds);
		}
	}


	public void setMissionID (String missionID) {
		this.missionID = missionID;
	}


	public String getMissionID () {
		return missionID;
	}


	public void setCampaignID (String campaignID) {
		this.campaignID = campaignID;
	}


	public String getCampaignID () {
		return campaignID;
	}


	private HashMap<String, Integer> getEnemiesKilled () {
		if (enemiesKilled == null) {
			enemiesKilled = new HashMap<String, Integer> ();
		}

		return enemiesKilled;
	}


	public void addKilledEnemy (String sIniHeader) {
		HashMap<String, Integer> hmKilled = getEnemiesKilled ();
		if (hmKilled.containsKey (sIniHeader)) {
			Integer iCount = hmKilled.remove (sIniHeader);
			hmKilled.put (sIniHeader, Integer.valueOf(iCount.intValue () + 1));
		} else {
			hmKilled.put (sIniHeader, Integer.valueOf(1));
		}
	}


	public int getNumKilledEnemies (String sIniHeader) {
		HashMap<String, Integer> hmKilled = getEnemiesKilled ();
		if (hmKilled.containsKey (sIniHeader)) {
			return hmKilled.get (sIniHeader).intValue ();
		} else {
			return 0;
		}
	}


	public static LivingEntity addNewLiving (String sIniHeader, int type, boolean bDiscovered, int x, int y, int z) {
		return addNewLiving (sIniHeader, type, bDiscovered, x, y, z, false);
	}


	public static LivingEntity addNewLiving (String sIniHeader, int type, boolean bDiscovered, int x, int y, int z, boolean bInit) {
		LivingEntity le;
		LivingEntityManagerItem lemi = null;

		if (type == LivingEntity.TYPE_CITIZEN) {
			le = Citizen.getInstance ();
		} else if (type == LivingEntity.TYPE_FRIENDLY) {
			lemi = LivingEntityManager.getItem (sIniHeader);
			le = new Friendly (lemi.getIniHeader ());
		} else if (type == LivingEntity.TYPE_ENEMY) {
			lemi = LivingEntityManager.getItem (sIniHeader);
			le = new Enemy (lemi.getIniHeader ());
		} else if (type == LivingEntity.TYPE_HERO) {
			le = new Hero (sIniHeader);

			// xp
			Hero hero = (Hero) le;
			int iPCT = Utils.getRandomBetween (20, 80);
			hero.getHeroData ().setXp ((Game.getWorld ().getMaxHeroXP () * iPCT) / 100);
			if (hero.getHeroData ().checkAndUpdateLevelUp (hero, false)) {
				hero.getLivingEntityData ().setModifiers (hero);
			}
		} else if (type == LivingEntity.TYPE_ALLY) {
			lemi = LivingEntityManager.getItem (sIniHeader);
			le = new Ally (lemi.getIniHeader ());
		} else {
			return null;
		}

		if (bInit) {
			le.init (x, y, z, bDiscovered);

			// Caso caravanas, creamos la data
			if (lemi != null && lemi.getCaravan () != null) {
				Game.getWorld ().setCurrentCaravanData (CaravanManager.getItem (lemi.getCaravan ()).createCaravanInstance (le.getID (), x, y, z));
			}
		}
		getCell (x, y, z).addLiving (le);

		return le;
	}


	/**
	 * Devuelve un hotpoint de exploración para un héroe
	 * 
	 * @param iASZID
	 * @return
	 */
	public Point3DShort getRandomExploringHotpoint (int iASZID) {
		// Obtenemos 128 casillas a random, si alguna tiene ASZID -1 la eliminamos de la lista
		// Si una de esas casillas coincide con el ASZID pasado y undiscovered vecinas se devuelve, en otro caso se devuelve null

		int iTryes = 128, iIndex;
		Point3DShort p3d;
		Cell cell;
		while (iTryes > 0) {
			if (exploringHotPoints.size () == 0) {
				return null;
			}

			iTryes--;
			iIndex = Utils.getRandomBetween (0, exploringHotPoints.size () - 1);
			p3d = exploringHotPoints.get (iIndex);
			cell = getCell (p3d);
			if (cell.getAstarZoneID () == -1) {
				// Casilla chunga, fuera
				exploringHotPoints.remove (iIndex);
				cell.setHeroExploringPoint (false);
			} else if (cell.getAstarZoneID () == iASZID) {
				// Casilla buena
				// Miramos que tenga algo undiscovered al lado, sino la quitamos de la lista
				boolean bCasillaBuena = false;
				short cellX = cell.getCoordinates ().x;
				short cellY = cell.getCoordinates ().y;
				short cellZ = cell.getCoordinates ().z;
				bucle: for (short x = -1; x <= 1; x++) {
					for (short y = -1; y <= 1; y++) {
						if (x != 0 || y != 0) {
							if (Utils.isInsideMap ((short) (cellX + x), (short) (cellY + y), cellZ)) {
								if (!getCell (cellX + x, cellY + y, cellZ).isDiscovered ()) {
									bCasillaBuena = true;
									break bucle;
								}
							}
						}
					}
				}

				if (bCasillaBuena) {
					// Restrict
					if (exploringHotPoints.get (iIndex).z <= getRestrictExploringLevel ()) {
						return exploringHotPoints.get (iIndex);
					}
				} else {
					// Casilla sin undiscovereds al lado, mala, malosa, la borramos
					exploringHotPoints.remove (iIndex);
					cell.setHeroExploringPoint (false);
				}
				// } else {
				// Siga jugando, casilla mala para esta zona
			}
		}

		return null;
	}


	public ArrayList<Point3DShort> getExploringHotPoints () {
		return exploringHotPoints;
	}


	/**
	 * @param maxHeroXP the maxHeroXP to set
	 */
	public void setMaxHeroXP (int maxHeroXP) {
		this.maxHeroXP = maxHeroXP;
	}


	/**
	 * @return the maxHeroXP
	 */
	public int getMaxHeroXP () {
		return maxHeroXP;
	}


	@SuppressWarnings("unchecked")
	public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {
		if (Game.SAVEGAME_LOADING_VERSION >= Game.SAVEGAME_V11) {
			citizenGroups = (CitizenGroups) in.readObject ();
		} else {
			citizenGroups = new CitizenGroups ();
		}
		soldierGroups = (SoldierGroups) in.readObject ();
		exploringHotPoints = (ArrayList<Point3DShort>) in.readObject ();
		maxHeroXP = in.readInt ();
		oldHeroes = (ArrayList<LivingEntity>) in.readObject ();
		oldHeroesDied = (ArrayList<String>) in.readObject ();
		currentCaravanData = (CaravanData) in.readObject ();
		enemiesKilled = (HashMap<String, Integer>) in.readObject ();
		projectiles = (ArrayList<Projectile>) in.readObject ();
		containers = (ArrayList<Container>) in.readObject ();
		stockpiles = (ArrayList<Stockpile>) in.readObject ();
		if (Game.SAVEGAME_LOADING_VERSION >= Game.SAVEGAME_V12) {
			buryData = (ArrayList<BuryData>) in.readObject ();
		} else {
			buryData = new ArrayList<BuryData> ();
		}
		zones = (ArrayList<Zone>) in.readObject ();
		coins = in.readInt ();
		sCoins = null;
		sHappinessAverage = null;
		taskManager = (TaskManager) in.readObject ();
		turn = in.readInt ();
		currentAutosaveDays = in.readInt ();
		date = (Date) in.readObject ();
		view = (Point3D) in.readObject ();
		numFloorsDiscovered = in.readInt ();
		readyForNextTurn = in.readBoolean ();
		readyForNextTurnFrameCounter = in.readInt ();
		readyForNextTurnTasks = in.readBoolean ();
		fluidCellsToProcess = (ArrayList<Point3DShort>) in.readObject ();
		itemsToBeHauled = (ArrayList<Integer>) in.readObject ();
		campaignID = (String) in.readObject ();
		missionID = (String) in.readObject ();

		if (Game.SAVEGAME_LOADING_VERSION >= Game.SAVEGAME_V12) {
			restrictHaulEquippingLevel = in.readInt ();
			restrictExploringLevel = in.readInt ();
		} else {
			restrictHaulEquippingLevel = 0;
			restrictExploringLevel = 0;
			// Comentado pq el número de levels aún no está seteado, después de cargar el mundo los seteo
			// restrictHaulEquippingLevel = MAP_NUM_LEVELS_OUTSIDE;
			// restrictExploringLevel = MAP_NUM_LEVELS_OUTSIDE;
		}

		if (Game.SAVEGAME_LOADING_VERSION >= Game.SAVEGAME_V13) {
			itemsText = (HashMap<Integer, ArrayList<String>>) in.readObject ();
		} else {
			itemsText = new HashMap<Integer, ArrayList<String>> ();
		}

		if (Game.SAVEGAME_LOADING_VERSION >= Game.SAVEGAME_V14) {
			events = (ArrayList<EventData>) in.readObject ();
			globalEvents = (GlobalEventData) in.readObject ();
			// gods = (ArrayList<GodData>) in.readObject ();
		} else {
			events = new ArrayList<EventData> ();
			globalEvents = new GlobalEventData ();
			// gods = new ArrayList<GodData> ();
		}
	}


	public void writeExternal (ObjectOutput out) throws IOException {
		out.writeObject (citizenGroups);
		out.writeObject (soldierGroups);
		out.writeObject (exploringHotPoints);
		out.writeInt (maxHeroXP);
		out.writeObject (oldHeroes);
		out.writeObject (oldHeroesDied);
		out.writeObject (currentCaravanData);
		out.writeObject (enemiesKilled);
		out.writeObject (projectiles);
		out.writeObject (containers);
		out.writeObject (stockpiles);
		out.writeObject (buryData);
		out.writeObject (zones);
		out.writeInt (coins);
		out.writeObject (taskManager);
		out.writeInt (turn);
		out.writeInt (currentAutosaveDays);
		out.writeObject (date);
		out.writeObject (view);
		out.writeInt (numFloorsDiscovered);
		out.writeBoolean (readyForNextTurn);
		out.writeInt (readyForNextTurnFrameCounter);
		out.writeBoolean (readyForNextTurnTasks);
		out.writeObject (fluidCellsToProcess);
		out.writeObject (itemsToBeHauled);
		out.writeObject (campaignID);
		out.writeObject (missionID);
		out.writeInt (restrictHaulEquippingLevel);
		out.writeInt (restrictExploringLevel);
		out.writeObject (itemsText);

		// v14
		out.writeObject (events);
		out.writeObject (globalEvents);
		// out.writeObject (gods);
	}
}
