package xaos.main;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import xaos.Towns;
import xaos.TownsProperties;
import xaos.actions.ActionManager;
import xaos.actions.ActionPriorityManager;
import xaos.campaign.CampaignManager;
import xaos.campaign.MissionData;
import xaos.campaign.TutorialFlow;
import xaos.campaign.TutorialTrigger;
import xaos.caravans.CaravanManager;
import xaos.caravans.PricesManager;
import xaos.data.BuryData;
import xaos.data.CitizenGroups;
import xaos.data.Type;
import xaos.data.Types;
import xaos.dungeons.DungeonGenerator;
import xaos.dungeons.DungeonManager;
import xaos.effects.EffectManager;
import xaos.events.EventManager;
import xaos.events.EventManagerItem;
import xaos.generator.MapGenerator;
import xaos.panels.CommandPanel;
import xaos.panels.ImagesPanel;
import xaos.panels.MainMenuPanel;
import xaos.panels.MainPanel;
import xaos.panels.MatsPanelData;
import xaos.panels.MessagesPanel;
import xaos.panels.MiniMapPanel;
import xaos.panels.TypingPanel;
import xaos.panels.UIPanel;
import xaos.panels.menus.ContextMenu;
import xaos.property.MainProperties;
import xaos.skills.SkillManager;
import xaos.tasks.Task;
import xaos.tiles.Cell;
import xaos.tiles.entities.buildings.Building;
import xaos.tiles.entities.buildings.BuildingManager;
import xaos.tiles.entities.items.Item;
import xaos.tiles.entities.items.ItemManager;
import xaos.tiles.entities.items.military.PrefixSuffixManager;
import xaos.tiles.entities.living.Citizen;
import xaos.tiles.entities.living.LivingEntity;
import xaos.tiles.entities.living.LivingEntityManager;
import xaos.tiles.entities.living.LivingEntityManagerItem;
import xaos.tiles.entities.living.heroes.HeroManager;
import xaos.tiles.terrain.Terrain;
import xaos.tiles.terrain.TerrainManager;
import xaos.utils.AStarQueue;
import xaos.utils.ColorGL;
import xaos.utils.Log;
import xaos.utils.Messages;
import xaos.utils.Names;
import xaos.utils.Point3D;
import xaos.utils.Point3DShort;
import xaos.utils.Utils;
import xaos.utils.UtilsAL;
import xaos.utils.UtilsGL;
import xaos.utils.UtilsIniHeaders;
import xaos.utils.UtilsKeyboard;
import xaos.utils.UtilsServer;
import xaos.zones.ZoneManager;


public final class Game {

	private static final long serialVersionUID = 1449863524190928424L;

	public final static int SAVEGAME_V9_V10a = 1;
	public final static int SAVEGAME_V10b = 2;
	public final static int SAVEGAME_V11 = 3;
	public final static int SAVEGAME_V12 = 4;
	public final static int SAVEGAME_V13 = 5;
	public final static int SAVEGAME_V14 = 6;
	public final static int SAVEGAME_V14d = 7;
	public final static int SAVEGAME_V14e = 8;
	public final static int SAVEGAME_VERSION = SAVEGAME_V14e;
	public static int SAVEGAME_LOADING_VERSION = 0;
	public static boolean SAVE_MISSION = true;

	public final static String SAVE_FOLDER1 = "save"; //$NON-NLS-1$
	public final static String MODS_FOLDER1 = "mods"; //$NON-NLS-1$
	public final static String BURY_FOLDER1 = "bury"; //$NON-NLS-1$
	public final static String SCREENSHOTS_FOLDER1 = "screenshots"; //$NON-NLS-1$

	public static int FPS_MAINMENU = 30;
	public static int FPS_INGAME = 30;

	public static final int MIN_DISPLAY_WIDTH = 1024;
	public static final int MIN_DISPLAY_HEIGHT = 600;

	// public static boolean MULTITEXTURING_AVAILABLE = false;
	public static boolean OPENGL_13_AVAILABLE = false;

	public static boolean texturesLoaded = false;

	public static int TEXTURE_FONT_ID;

	public static HashMap<String, Integer> hmExtraTextures;

	public static boolean takeScreenshot = false;

	public final static int SIEGE_DIFFICULTY_OFF = 0;
	public final static int SIEGE_DIFFICULTY_EASY = 1;
	public final static int SIEGE_DIFFICULTY_NORMAL = 2;
	public final static int SIEGE_DIFFICULTY_HARD = 3;
	public final static int SIEGE_DIFFICULTY_HARDER = 4;
	public final static int SIEGE_DIFFICULTY_INSANE = 5;

	public final static int STATE_NO_STATE = 0;
	public final static int STATE_CREATING_TASK = 1;
	public final static int STATE_SHOWING_CONTEXT_MENU = 2;
	public final static int STATE_SHOWING_INFO_PANEL = 3;

	private static int currentState = STATE_NO_STATE;
	private static Task currentTask;
	private static ContextMenu currentContextMenu;
	// private static InfoPanel currentInfoPanel;
	private static MissionData missionData;

	private static String userFolder;
	private static String fileSeparator;

	private static boolean bPaused;

	public static int iError;

	private static MainPanel panelMain;
	private static MainMenuPanel panelMainMenu;
	private static CommandPanel panelCommand;
	private static UIPanel panelUI;
	private static MessagesPanel panelMessages;

	private static World world;
	private static boolean musicON;
	private static boolean FXON;
	private static boolean mouseScrollON;
	private static boolean mouseScrollEarsON;
	private static boolean mouse2DCubesON;
	private static boolean disabledItemsON;
	private static boolean disabledGodsON;
	private static boolean pauseStartON;
	private static int autosaveDays = 0;
	private static int siegeDifficulty;
	private static boolean siegePause;
	private static boolean caravanPause;
	private static boolean allowBury;
	private static int volumeMusic = 10;
	private static int volumeFX = 10;
	private static int pathfindingCPULevel;
	private static ArrayList<String> alModsLoaded = new ArrayList<String> ();
	private static ArrayList<String> alServers = new ArrayList<String> ();
	private static ArrayList<String> alServerNames = new ArrayList<String> ();
	private static int serverToUse;

	private static String savegameName;

	private boolean displayFullscreen = false;


	public Game () {

		// User folder
		String sUserFolder = Towns.getPropertiesString ("USER_FOLDER"); //$NON-NLS-1$
		Towns.propertiesMain = null;
		File fUserFolder;
		if (sUserFolder != null && sUserFolder.length () > 0) {
			fUserFolder = Utils.createUserFolder (sUserFolder);
		} else {
			fUserFolder = Utils.createUserFolder (System.getProperty ("user.home")); //$NON-NLS-1$
		}
		if (fUserFolder == null) {
			Log.log (Log.LEVEL_ERROR, Messages.getString ("Game.7"), getClass ().toString ()); //$NON-NLS-1$
			exit ();
		}
		userFolder = fUserFolder.getAbsolutePath ();

		// Música?
		musicON = Boolean.parseBoolean (Towns.getPropertiesString ("MUSIC")); //$NON-NLS-1$
		FXON = Boolean.parseBoolean (Towns.getPropertiesString ("FX")); //$NON-NLS-1$

		// Game options
		mouseScrollON = Boolean.parseBoolean (Towns.getPropertiesString ("MOUSE_SCROLL")); //$NON-NLS-1$
		mouseScrollEarsON = Boolean.parseBoolean (Towns.getPropertiesString ("MOUSE_SCROLL_EARS")); //$NON-NLS-1$
		mouse2DCubesON = Boolean.parseBoolean (Towns.getPropertiesString ("MOUSE_2D_CUBES")); //$NON-NLS-1$
		disabledItemsON = Boolean.parseBoolean (Towns.getPropertiesString ("DISABLED_ITEMS")); //$NON-NLS-1$
		disabledGodsON = Boolean.parseBoolean (Towns.getPropertiesString ("DISABLED_GODS")); //$NON-NLS-1$
		pauseStartON = Boolean.parseBoolean (Towns.getPropertiesString ("PAUSE_START")); //$NON-NLS-1$
		setAutosaveDays (Towns.getPropertiesInt ("AUTOSAVE_DAYS", 0)); //$NON-NLS-1$
		setSiegeDifficulty (Towns.getPropertiesInt ("SIEGES", SIEGE_DIFFICULTY_NORMAL)); //$NON-NLS-1$
		siegePause = Boolean.parseBoolean (Towns.getPropertiesString ("SIEGE_PAUSE")); //$NON-NLS-1$
		caravanPause = Boolean.parseBoolean (Towns.getPropertiesString ("CARAVAN_PAUSE")); //$NON-NLS-1$
		allowBury = Boolean.parseBoolean (Towns.getPropertiesString ("ALLOW_BURY")); //$NON-NLS-1$
		setVolumeMusic (Towns.getPropertiesInt ("VOLUME_MUSIC", 10)); //$NON-NLS-1$
		setVolumeFX (Towns.getPropertiesInt ("VOLUME_FX", 10)); //$NON-NLS-1$

		setPathfindingCPULevel (Towns.getPropertiesInt ("PATHFINDING_LEVEL", 2)); //$NON-NLS-1$
		// Mods loaded
		setModsLoaded (Towns.getPropertiesString ("MODS")); //$NON-NLS-1$

		// Servers
		setServers (Towns.getPropertiesString ("SERVERS")); //$NON-NLS-1$

		// Shortcuts
		UtilsKeyboard.loadShortcuts ();

		// FPS
		FPS_MAINMENU = Towns.getPropertiesInt ("FPS_MAINMENU", FPS_MAINMENU); //$NON-NLS-1$
		FPS_INGAME = Towns.getPropertiesInt ("FPS_INGAME", FPS_INGAME); //$NON-NLS-1$

		// window size
		int desktopWidth = Display.getDesktopDisplayMode ().getWidth ();
		int desktopHeight = Display.getDesktopDisplayMode ().getHeight ();

		final int configuredWidth = Towns.getProperty (MainProperties.WINDOW_WIDTH, (desktopWidth * 2) / 3);
		final int configuredHeight = Towns.getProperty (MainProperties.WINDOW_HEIGHT, (desktopHeight * 2) / 3);
		final boolean fullscreen = Towns.getProperty (MainProperties.FULLSCREEN, false);

		int width = Math.max (MIN_DISPLAY_WIDTH, Math.min (desktopWidth, configuredWidth));
		int height = Math.max (MIN_DISPLAY_HEIGHT, Math.min (desktopWidth, configuredHeight));

		// Inicializamos OpenGL
		UtilsGL.initGL (width, height, fullscreen);
		UtilsAL.initAL (Game.getVolumeMusic (), Game.getVolumeFX ());

		// OpenGL 1.3 or better
		String sVersion = GL11.glGetString (GL11.GL_VERSION);
		if (sVersion == null || sVersion.length () == 0) {
			Log.log (Log.LEVEL_ERROR, "OpenGL version not available", "Game"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			StringTokenizer tokenizer = new StringTokenizer (sVersion, "."); //$NON-NLS-1$
			int iMajor = -1, iMinor = -1;
			if (tokenizer.hasMoreTokens ()) {
				try {
					iMajor = Integer.parseInt (tokenizer.nextToken ());
					if (tokenizer.hasMoreTokens ()) {
						try {
							iMinor = Integer.parseInt (tokenizer.nextToken ());
						}
						catch (Exception e) {
						}
					}
				}
				catch (Exception e) {
				}
			}

			if (iMajor != -1 && iMinor != -1) {
				if (iMajor > 1) {
					OPENGL_13_AVAILABLE = true;
				} else if (iMajor == 1) {
					OPENGL_13_AVAILABLE = (iMinor >= 3);
				}
			}
		}

		// Depths
		Cell.generateDepths ();

		// Cargamos los paneles
		panelMain = new MainPanel ();
		panelUI = new UIPanel ();
		if (getWorld () == null) {
			panelCommand = new CommandPanel (UtilsGL.getWidth () - World.MAP_WIDTH, World.MAP_HEIGHT, World.MAP_WIDTH, UtilsGL.getHeight () - World.MAP_HEIGHT, null, null);
		} else {
			panelCommand = new CommandPanel (UtilsGL.getWidth () - World.MAP_WIDTH, World.MAP_HEIGHT, World.MAP_WIDTH, UtilsGL.getHeight () - World.MAP_HEIGHT, Game.getWorld ().getCampaignID (), Game.getWorld ().getMissionID ());
		}
		panelMessages = new MessagesPanel (UIPanel.MESSAGES_PANEL_WIDTH, UIPanel.MESSAGES_PANEL_HEIGHT);

		// Cargamos las texturas
		loadAllIniTextures ();

		// Esto aquí, después de cargar las texturas, que si no empieza el contador del SMP logo a bajar antes de tiempo
		panelMainMenu = new MainMenuPanel (0, 0, UtilsGL.getWidth (), UtilsGL.getHeight ());
		panelMainMenu.setActive (true);

		// Main loop
		run ();

	}


	public static void loadAllIniTextures () {
		Properties properties = Towns.getPropertiesGraphics ();
		if (properties != null) {
			Enumeration<?> enumNames = properties.propertyNames ();
			String sName;
			ArrayList<String> alNames = new ArrayList<String> ();
			while (enumNames.hasMoreElements ()) {
				sName = (String) enumNames.nextElement ();
				if (sName.indexOf ("TEXTURE_FILE") != -1) { //$NON-NLS-1$
					sName = properties.getProperty (sName);
					if (!alNames.contains (sName)) {
						alNames.add (sName);
					}
				}
			}

			// UtilsGL.clearAllCached ();
			// Tenemos la lista de texturas, las cargamos
			for (int i = 0; i < alNames.size (); i++) {
				UtilsGL.loadTexture (alNames.get (i), GL11.GL_MODULATE);
			}
		}
	}


	public static String getUserFolder () {
		return userFolder;
	}


	public static String getFileSeparator () {
		if (fileSeparator == null || fileSeparator.trim ().length () == 0) {
			fileSeparator = System.getProperty ("file.separator"); //$NON-NLS-1$
			if (fileSeparator == null || fileSeparator.trim ().length () == 0) {
				fileSeparator = "/"; //$NON-NLS-1$
			}
		}

		return fileSeparator;
	}


	/**
	 * Continua una partida grabada
	 */
	public static void continueGame (String savegameName, String sCampaignMissionAndForcedZipPath) {
		File fMapa;
		boolean bForzedZip = false;
		String sForcedCampaign = null;
		String sForcedMission = null;
		String sForcedZipPath = null;
		int iIndex = -1;
		if (sCampaignMissionAndForcedZipPath != null) {
			StringTokenizer tokenizer = new StringTokenizer (sCampaignMissionAndForcedZipPath, ",");
			if (tokenizer.hasMoreTokens ()) {
				sForcedCampaign = tokenizer.nextToken ();

				if (tokenizer.hasMoreTokens ()) {
					sForcedMission = tokenizer.nextToken ();

					if (tokenizer.hasMoreTokens ()) {
						sForcedZipPath = tokenizer.nextToken ();

						iIndex = sForcedZipPath.indexOf ("save.zip"); //$NON-NLS-1$
						if (iIndex != -1) {
							bForzedZip = true;
						}
					}
				}
			}
		}

		if (bForzedZip) {
			fMapa = new File (sForcedZipPath);
		} else {
			fMapa = new File (Game.getUserFolder () + Game.getFileSeparator () + Game.SAVE_FOLDER1 + Game.getFileSeparator () + savegameName); //$NON-NLS-1$
		}
		
		boolean bNewGame = !fMapa.exists ();
		if (!bNewGame) {
			getPanelMainMenu ().setLoadingText (Messages.getString ("Game.13")); //$NON-NLS-1$
			try {
				if (bForzedZip) {
					Utils.load (sForcedZipPath.substring (0, iIndex), "save.zip"); //$NON-NLS-1$
					getWorld ().setCampaignID (sForcedCampaign);
					getWorld ().setMissionID (sForcedMission);
				} else {
					Utils.load (Game.getUserFolder () + Game.getFileSeparator () + Game.SAVE_FOLDER1 + Game.getFileSeparator (), fMapa.getName ()); //$NON-NLS-1$
				}

				setupGame (bNewGame, getWorld ().getCampaignID (), getWorld ().getMissionID (), bForzedZip);
			}
			catch (Exception e) {
				Game.exitToMainMenu ();
				getPanelMainMenu ().setErrorToShow (e.getMessage ());
				getPanelMainMenu ().createMenu ();
				return;
			}
		} else {
			Log.log (Log.LEVEL_ERROR, Messages.getString ("Game.1") + " [" + savegameName + ".zip]", "Game"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			Game.exitToMainMenu ();
			getPanelMainMenu ().setErrorToShow (Messages.getString ("Game.1") + " [" + savegameName + ".zip]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			getPanelMainMenu ().createMenu ();
			return;
		}
	}


	/**
	 * Empieza una partida desde 0
	 * 
	 * @param campaignID ID de la campaña, null en caso de partida normal
	 * @param missionID ID de la misión, null en caso de partida normal
	 */
	public static void startGame (String campaignID, String missionID) {
		File fMapa;
		if (campaignID == null || missionID == null) {
			fMapa = new File (Game.getUserFolder () + Game.getFileSeparator () + Game.SAVE_FOLDER1 + Game.getFileSeparator () + "freemode.zip"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			fMapa = new File (Game.getUserFolder () + Game.getFileSeparator () + Game.SAVE_FOLDER1 + Game.getFileSeparator () + campaignID + missionID + ".zip"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (fMapa.exists ()) {
			fMapa.delete ();
		}

		world = new World (campaignID, missionID);
		world.generateAll (campaignID, missionID);

		setupGame (true, campaignID, missionID, false);
	}


	private static void setupGame (boolean bNewGame, String sCampaignID, String sMissionID, boolean bForzedZip) {
		if (bNewGame) {
			Game.SAVEGAME_LOADING_VERSION = Game.SAVEGAME_VERSION;
		}
		Cell[][][] cells = World.getCells ();

		// Terrain slopes
		Terrain.changeSlopes (cells);

		long lTime;
		String sLog = null;

		// Aquí mismo borro los alphas
		UtilsGL.clearCachedAlphas ();

		// Menus
		if (TownsProperties.DEBUG_MODE) {
			sLog = Messages.getString ("Game.8") + " ("; //$NON-NLS-1$ //$NON-NLS-2$
			lTime = System.currentTimeMillis ();
		}
		getPanelMainMenu ().setLoadingText (Messages.getString ("Game.8")); //$NON-NLS-1$

		getPanelUI ().initialize (sCampaignID, sMissionID, true);
		CommandPanel.initialize (sCampaignID, sMissionID);
		if (TownsProperties.DEBUG_MODE) {
			sLog += (System.currentTimeMillis () - lTime) + "ms)"; //$NON-NLS-1$
			Log.log (Log.LEVEL_DEBUG, sLog, "Game"); //$NON-NLS-1$
		}

		// Events
		EventManager.loadItems ();
		getWorld ().checkGlobalEvents ();

		// Load buildings, items and types info
		BuildingManager.loadItems ();
		ItemManager.loadItems ();

		// Items
		if (TownsProperties.DEBUG_MODE) {
			sLog = Messages.getString ("Game.9") + " ("; //$NON-NLS-1$ //$NON-NLS-2$
			lTime = System.currentTimeMillis ();
		}
		getPanelMainMenu ().setLoadingText (Messages.getString ("Game.9")); //$NON-NLS-1$
		Item.loadItems (getWorld ());
		Building.loadBuildings ();
		if (TownsProperties.DEBUG_MODE) {
			sLog += (System.currentTimeMillis () - lTime) + "ms)"; //$NON-NLS-1$
			Log.log (Log.LEVEL_DEBUG, sLog, "Game"); //$NON-NLS-1$
			sLog = Messages.getString ("Game.12") + " ("; //$NON-NLS-1$ //$NON-NLS-2$
			lTime = System.currentTimeMillis ();
		}

		getPanelMainMenu ().setLoadingText (Messages.getString ("Game.12")); //$NON-NLS-1$
		// Living entities
		LivingEntity.loadLivings ();
		if (TownsProperties.DEBUG_MODE) {
			sLog += (System.currentTimeMillis () - lTime) + "ms)"; //$NON-NLS-1$
			Log.log (Log.LEVEL_DEBUG, sLog, "Game"); //$NON-NLS-1$
			sLog = Messages.getString ("Game.10") + " ("; //$NON-NLS-1$ //$NON-NLS-2$
			lTime = System.currentTimeMillis ();
		}
		getPanelMainMenu ().setLoadingText (Messages.getString ("Game.10")); //$NON-NLS-1$

		// Contenido de containers
		for (int i = (getWorld ().getContainers ().size () - 1); i >= 0; i--) {
			getWorld ().getContainers ().get (i).refreshTransients ();
		}

		if (TownsProperties.DEBUG_MODE) {
			sLog += (System.currentTimeMillis () - lTime) + "ms)"; //$NON-NLS-1$
			Log.log (Log.LEVEL_DEBUG, sLog, "Game"); //$NON-NLS-1$
		}

		// Set ALL A* zone IDs
		Cell.setAllZoneIDs ();

		// Paint under
		Cell.generatePaintUnder (cells);

		// Fluids pre-newgame
		if (bNewGame) {
			if (TownsProperties.DEBUG_MODE) {
				sLog = Messages.getString ("Game.11") + " ("; //$NON-NLS-1$ //$NON-NLS-2$
				lTime = System.currentTimeMillis ();
			}
			getPanelMainMenu ().setLoadingText (Messages.getString ("Game.11")); //$NON-NLS-1$
			getWorld ().moveFluidsPreNewGame ();
			if (TownsProperties.DEBUG_MODE) {
				sLog += (System.currentTimeMillis () - lTime) + "ms)"; //$NON-NLS-1$
				Log.log (Log.LEVEL_DEBUG, sLog, "Game"); //$NON-NLS-1$
			}
		}

		// Evaporation
		Point3DShort p3ds;
		while (World.fluidEvaporation.size () > 0) {
			p3ds = World.fluidEvaporation.remove (World.fluidEvaporation.size () - 1);
			Point3DShort.returnToPool (p3ds);
		}
		for (int x = 0; x < cells.length; x++) {
			for (int y = 0; y < cells[0].length; y++) {
				for (int z = 0; z < cells[0][0].length; z++) {
					World.checkNewEvaporation (cells[x][y][z]);
				}
			}
		}

		// Falling items
		Integer[] aItems = World.getItems ().keySet ().toArray (new Integer [0]);
		for (int i = 0; i < aItems.length; i++) {
			World.addFallItem (aItems[i]);
		}

		// Shadows
		Cell.generateShadows ();

		// Lights
		Cell.generateAllLights ();

		// Open cells
		Cell.generateAllOpen ();

		// Citizen groups (para los saves antiguos)
		CitizenGroups citizenGroups = getWorld ().getCitizenGroups ();
		ArrayList<Integer> alCitsSinGrupo = citizenGroups.getCitizensWithoutGroup ();
		for (int i = 0, n = World.getCitizenIDs ().size (); i < n; i++) {
			LivingEntity le = World.getLivingEntityByID (World.getCitizenIDs ().get (i));
			if (le != null) {
				LivingEntityManagerItem lemi = LivingEntityManager.getItem (le.getIniHeader ());
				if (lemi != null && lemi.getType () == LivingEntity.TYPE_CITIZEN) {
					Citizen cit = (Citizen) le;
					if (cit.getCitizenData () != null && cit.getCitizenData ().getGroupID () == -1) {
						// Ciudadano sin grupo (pq no tiene grupo, pq es un save de la v10 o pq es una nueva partida)
						if (!alCitsSinGrupo.contains (le.getID ())) {
							alCitsSinGrupo.add (Integer.valueOf(le.getID ()));
						}
					}
				}
			}
		}
		citizenGroups.setCitizensWithoutGroup (alCitsSinGrupo);

		// Esto es un parche para solventar un bug. Antes no se borraban los ciudadanos de los job grupos al morir, y podría estar corrupto
		citizenGroups.purgeNonExistentCitizens ();

		// Bury
		if (bNewGame && isAllowBury () && MapGenerator.ALLOW_BURY && MainMenuPanel.useBuryTemporary) {
			lTime = System.currentTimeMillis ();
			getPanelMainMenu ().setLoadingText (Messages.getString ("Game.15")); //$NON-NLS-1$

			generateBury (cells);

			if (TownsProperties.DEBUG_MODE) {
				sLog += (System.currentTimeMillis () - lTime) + "ms)"; //$NON-NLS-1$
				Log.log (Log.LEVEL_DEBUG, sLog, "Game"); //$NON-NLS-1$
			}
		}

		// Events
		if (bNewGame && MapGenerator.STARTING_EVENTS != null) {
			EventManagerItem emi;
			for (int i = 0; i < MapGenerator.STARTING_EVENTS.size (); i++) {
				emi = EventManager.getItem (MapGenerator.STARTING_EVENTS.get (i));
				if (emi != null) {
					getWorld ().addEvent (emi);
				}
			}
		}

		// A* thread
		Thread t = new Thread (new AStarQueue ());
		t.start ();

		// Paramos la música del main menú y arrancamos la música in-game
		UtilsAL.stop (UtilsAL.SOURCE_MUSIC_MAINMENU);
		UtilsAL.play (UtilsAL.SOURCE_MUSIC_INGAME);

		Game.getPanelMainMenu ().setActive (false);

		if (bForzedZip) {
			// Forced zip, we load the campaign data in the folder
			missionData = CampaignManager.getMission (sCampaignID, sMissionID);
		}

		if (missionData == null) {
			if (sCampaignID != null && sMissionID != null) {
				missionData = CampaignManager.getMission (sCampaignID, sMissionID);

				if (missionData == null) {
					if (bNewGame) {
						Log.log (Log.LEVEL_ERROR, Messages.getString ("Game.14") + " [" + sCampaignID + "][" + sMissionID + "]", "Game"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
						Game.exit ();
						// } else {
						// // Cargando una partida y no encuentra la campaña, ponemos todo vacío y pacasa
						//                    MissionData md = new MissionData(""); //$NON-NLS-1$
						// missionPanel.setMissionData(md);
					}
//				} else {
//					MessagesPanel.addMessage (MessagesPanel.TYPE_SYSTEM, Messages.getString ("Game.5"), ColorGL.GREEN); //$NON-NLS-1$
				}
				// if (missionData != null && missionPanel.getMissionData().getText() != null && missionPanel.getMissionData().getObjectives() != null && missionPanel.getMissionData().getObjectives().size() > 0) {
				//                MessagesPanel.addMessage(MessagesPanel.TYPE_SYSTEM, Messages.getString("Game.0") + CampaignManager.getMission(sCampaignID, sMissionID).getName() + "]", ColorGL.GREEN); //$NON-NLS-1$ //$NON-NLS-2$
				//                MessagesPanel.addMessage(MessagesPanel.TYPE_SYSTEM, Messages.getString("Game.5"), ColorGL.GREEN); //$NON-NLS-1$
				// }
			} else {
				missionData = null;
			}
		}

		// Update flow? In case the current flow doesn't have a trigger
		updateTutorialFlow (TutorialTrigger.TYPE_INT_NONE, 0, null);

		// Initializing view
		if (bNewGame) {
			if (World.getCitizenIDs ().size () > 0) {
				LivingEntity le = World.getLivingEntityByID (World.getCitizenIDs ().get (0));
				if (le != null) {
					Game.getWorld ().setView (Point3DShort.getPoolInstance (le.getCoordinates ()));
				} else {
					Game.getWorld ().setView (Point3DShort.getPoolInstance (World.MAP_WIDTH / 2, World.MAP_HEIGHT / 2, World.MAP_NUM_LEVELS_OUTSIDE - 1));
				}
			} else if (World.getSoldierIDs ().size () > 0) {
				LivingEntity le = World.getLivingEntityByID (World.getSoldierIDs ().get (0));
				if (le != null) {
					Game.getWorld ().setView (Point3DShort.getPoolInstance (le.getCoordinates ()));
				} else {
					Game.getWorld ().setView (Point3DShort.getPoolInstance (World.MAP_WIDTH / 2, World.MAP_HEIGHT / 2, World.MAP_NUM_LEVELS_OUTSIDE - 1));
				}
			} else {
				Game.getWorld ().setView (Point3DShort.getPoolInstance (World.MAP_WIDTH / 2, World.MAP_HEIGHT / 2, World.MAP_NUM_LEVELS_OUTSIDE - 1));
			}
		}

		// Happiness average
		world.calculateHappinessAverage ();

		// Pause at start?
		if (Game.isPauseStartON () && !Game.isPaused ()) {
			Game.pause (true);
		}

		UtilsGL.initGLModes ();

		// Tutorial?
		if (missionData != null && missionData.getTutorialFlows ().size () > 0) {
			UIPanel.toggleTutorialPanel (bNewGame || bForzedZip);
		}
	}


	public static void generateBury (Cell[][][] cells) {
		String sServerName = null;
		if (getServerToUse () >= 0 && getServerToUse () < getServers ().size ()) {
			sServerName = getServers ().get (getServerToUse ());
		}

		Point3DShort p3ds;
		int iBuryStartingZ = World.MAP_NUM_LEVELS_OUTSIDE + 2;
		boolean bContinue = iBuryStartingZ < (World.MAP_DEPTH - 1);
		while (bContinue) { // Nada de burying en el último nivel
			// Obtenemos un bury al azar y cargamos los datos
			BuryData bd = Utils.getRandomBuryData (sServerName);

			// Flags
			if (bd.getHash () != null) {
				int iHeightMin = bd.getHeightMin ();
				int iMinX = -1;
				int iMaxX = -1;
				int iMinY = -1;
				int iMaxY = -1;
				int iMinZ = -1;
				int iMaxZ = -1;
				BuryData newBuryData = new BuryData ();
				ArrayList<Point3DShort> alPoints = new ArrayList<Point3DShort> ();
				ArrayList<Integer> alHeaderIDs = new ArrayList<Integer> ();
				ArrayList<ArrayList<String>> alHeaderTexts = new ArrayList<ArrayList<String>> ();

				Iterator<Point3DShort> it = bd.getHash ().keySet ().iterator ();
				while (it.hasNext ()) {
					p3ds = it.next ();
					int iZ = p3ds.z - iHeightMin + iBuryStartingZ;
					if (iZ < (World.MAP_DEPTH - 1)) {
						alPoints.add (Point3DShort.getPoolInstance (p3ds.x, p3ds.y, iZ));
						alHeaderIDs.add (bd.getHash ().get (p3ds));
						if (bd.getHashTexts ().get (p3ds) != null) {
							alHeaderTexts.add (bd.getHashTexts ().get (p3ds));
						} else {
							alHeaderTexts.add (new ArrayList<String> ());
						}

						if (iMaxZ == -1) {
							iMinX = p3ds.x;
							iMaxX = p3ds.x;
							iMinY = p3ds.y;
							iMaxY = p3ds.y;
							iMinZ = iZ;
							iMaxZ = iZ;
						} else {
							if (iMinX > p3ds.x) {
								iMinX = p3ds.x;
							}
							if (iMaxX < p3ds.x) {
								iMaxX = p3ds.x;
							}
							if (iMinY > p3ds.y) {
								iMinY = p3ds.y;
							}
							if (iMaxY < p3ds.y) {
								iMaxY = p3ds.y;
							}
							if (iMinZ > iZ) {
								iMinZ = iZ;
							}
							if (iMaxZ < iZ) {
								iMaxZ = iZ;
							}
						}
					}
				}

				if (iMaxZ == -1) {
					bContinue = false;
				} else {
					// Vamos a mover el pueblo enterrado a random
					int iXTotal = (iMaxX - iMinX) + 1;
					int iYTotal = (iMaxY - iMinY) + 1;
					int iXRandom = Utils.getRandomBetween (0, (World.MAP_WIDTH - iXTotal - 1));
					int iYRandom = Utils.getRandomBetween (0, (World.MAP_HEIGHT - iYTotal - 1));
					int iXMove = 0;
					int iYMove = 0;
					if (iXRandom < iMinX) {
						iXMove = (iMinX - iXRandom);
					} else if (iXRandom > iMinX) {
						iXMove = (iXRandom - iMinX);
					}
					if (iYRandom < iMinY) {
						iYMove = (iMinY - iYRandom);
					} else if (iYRandom > iMinY) {
						iYMove = (iYRandom - iMinY);
					}
					// Comprobación pq no estoy seguro que se pueda salir de rango
					if ((iMinX + iXMove) < 0 || (iMaxX + iXMove) >= World.MAP_WIDTH) {
						iXMove = 0;
					}
					if ((iMinY + iYMove) < 0 || (iMaxY + iYMove) >= World.MAP_HEIGHT) {
						iYMove = 0;
					}

					HashMap<Point3DShort, Integer> hashPoints = new HashMap<Point3DShort, Integer> ();
					HashMap<Point3DShort, ArrayList<String>> hashPointsText = new HashMap<Point3DShort, ArrayList<String>> ();

					// Movemos el pueblo
					Point3DShort p3dsAux;
					Integer iHeader;
					ArrayList<String> alTexts;
					while (alPoints.size () > 0) {
						p3dsAux = alPoints.remove (0);
						iHeader = alHeaderIDs.remove (0);
						alTexts = alHeaderTexts.remove (0);
						p3dsAux.x += (short) iXMove;
						p3dsAux.y += (short) iYMove;

						if (!DungeonGenerator.hasFluidsAround (cells, p3dsAux.x, p3dsAux.y, p3dsAux.z)) {
							if (!cells[p3dsAux.x][p3dsAux.y][p3dsAux.z].isMined ()) {
								// Si no está minada la minamos
								DungeonGenerator.removeBlock (cells, p3dsAux.x, p3dsAux.y, p3dsAux.z);
							}
							cells[p3dsAux.x][p3dsAux.y][p3dsAux.z].setBury (true);
							hashPoints.put (p3dsAux, iHeader);
							if (alTexts != null && alTexts.size () > 0) {
								hashPointsText.put (p3dsAux, alTexts);
							}
						}
					}

					// Añadimos los datos
					int iHeight = (iMaxZ - iMinZ) + 1;
					newBuryData.setHeight (iHeight);
					newBuryData.setHash (hashPoints);
					newBuryData.setHashTexts (hashPointsText);

					Game.getWorld ().addBuryData (newBuryData);

					if (iHeight > 0) {
						iBuryStartingZ += iHeight;
					} else {
						iBuryStartingZ++;
					}
					bContinue = iBuryStartingZ < (World.MAP_DEPTH - 1);
				}
			} else {
				bContinue = false;
			}
		}
	}


	/**
	 * Returns Main Menu panel
	 * 
	 * @return Main Menu panel
	 */
	public static MainMenuPanel getPanelMainMenu () {
		return panelMainMenu;
	}


	/**
	 * Returns Main panel
	 * 
	 * @return Main panel
	 */
	public static MainPanel getPanelMain () {
		return panelMain;
	}


	/**
	 * Returns Command panel
	 * 
	 * @return Command panel
	 */
	public static CommandPanel getPanelCommand () {
		return panelCommand;
	}


	/**
	 * Returns UI panel
	 * 
	 * @return UI panel
	 */
	public static UIPanel getPanelUI () {
		return panelUI;
	}


	/**
	 * Returns Messages panel
	 * 
	 * @return Messages panel
	 */
	public static MessagesPanel getPanelMessages () {
		return panelMessages;
	}


	public final static World getWorld () {
		return world;
	}


	public static void setWorld (World wrld) {
		world = wrld;
	}


	public static int getCurrentState () {
		return currentState;
	}


	public static void setCurrentState (int currentState) {
		Game.currentState = currentState;
	}


	public static Task getCurrentTask () {
		return currentTask;
	}


	public static ContextMenu getCurrentContextMenu () {
		return currentContextMenu;
	}


	public static void setContextMenu (ContextMenu menu) {
		if (getCurrentState () == STATE_SHOWING_CONTEXT_MENU) {
			deleteCurrentContextMenu ();
		}

		currentContextMenu = menu;

		if (getCurrentContextMenu () != null) {
			setCurrentState (STATE_SHOWING_CONTEXT_MENU);
		}
	}


	// public static void setCurrentInfoPanel(InfoPanel currentInfoPanel) {
	// Game.currentInfoPanel = currentInfoPanel;
	// }
	//
	// public static InfoPanel getCurrentInfoPanel() {
	// return currentInfoPanel;
	// }

	public static MissionData getCurrentMissionData () {
		return missionData;
	}


	public static void setCurrentMissionData (MissionData data) {
		missionData = data;
	}


	public static void createTask (int taskID) {
		setCurrentState (STATE_CREATING_TASK);
		currentTask = new Task (taskID);
	}


	public static void createTask (int taskID, String sParameter) {
		createTask (taskID);
		currentTask.setParameter (sParameter);
	}


	public static void taskCreated () {
		if (getCurrentTask () != null && getCurrentTask ().getTask () == Task.TASK_DIG) {
			getCurrentTask ().setTask (Task.TASK_MINE);
		}
		world.getTaskManager ().addTask (getCurrentTask ());
		setCurrentState (Game.STATE_NO_STATE);
	}


	public static void deleteCurrentTask () {
		setCurrentState (STATE_NO_STATE);
		currentTask = null;
	}


	public static void deleteCurrentContextMenu () {
		setCurrentState (STATE_NO_STATE);
		currentContextMenu = null;
	}


	// public static void showInfoPanel(InfoPanel panel) {
	// if (getCurrentState() == STATE_SHOWING_CONTEXT_MENU) {
	// deleteCurrentContextMenu();
	// } else if (getCurrentState() == STATE_CREATING_TASK) {
	// deleteCurrentTask();
	// }
	// closeInfoPanel();
	//
	// setCurrentInfoPanel(panel);
	// setCurrentState(STATE_SHOWING_INFO_PANEL);
	// }
	//
	// public static void closeInfoPanel() {
	// setCurrentState(STATE_NO_STATE);
	// currentInfoPanel = null;
	// }

	public static void togglePause (boolean showInfo) {
		if (UIPanel.isTradePanelActive ()) {
			return;
		}

		bPaused = !bPaused;
		if (showInfo) {
			showPauseStatus ();
		}
	}


	public static void pause (boolean showInfo) {
		if (UIPanel.isTradePanelActive ()) {
			return;
		}

		if (!bPaused) {
			bPaused = true;
			if (showInfo) {
				showPauseStatus ();
			}
		}
	}


	public static void resume (boolean showInfo) {
		if (UIPanel.isTradePanelActive ()) {
			return;
		}

		if (bPaused) {
			bPaused = false;
			if (showInfo) {
				showPauseStatus ();
			}
		}
	}


	public static boolean isPaused () {
		return bPaused;
	}


	public static void setPaused (boolean paused) {
		bPaused = paused;
	}


	public static void showPauseStatus () {
		if (isPaused ()) {
			MessagesPanel.addMessage (MessagesPanel.TYPE_SYSTEM, Messages.getString ("Game.3"), ColorGL.YELLOW); //$NON-NLS-1$
		} else {
			MessagesPanel.addMessage (MessagesPanel.TYPE_SYSTEM, Messages.getString ("Game.4"), ColorGL.YELLOW); //$NON-NLS-1$
		}
	}


	/**
	 * Obtiene eventos del mouse y llama al "panel" correspondiente según donde sea el click
	 */
	private void checkMouseEvents () {
		int mouseX = Mouse.getEventX ();
		int mouseY = UtilsGL.getHeight () - Mouse.getEventY () - 1;
		int mouseButton;

		while (Mouse.next ()) {
			mouseButton = Mouse.getEventButton ();

			if (Mouse.getEventButtonState ()) {
				// Main menu
				if (getPanelMainMenu ().isActive ()) {
					getPanelMainMenu ().mousePressed (mouseX, mouseY, mouseButton);
					continue;
				}

				// Info panel
				// if (getCurrentState() == STATE_SHOWING_INFO_PANEL) {
				// // Info panel, miramos donde clica
				// if (mouseX >= getCurrentInfoPanel().getX() && mouseX < (getCurrentInfoPanel().getX() + getCurrentInfoPanel().getWidth()) && mouseY >= getCurrentInfoPanel().getY() && mouseY < (getCurrentInfoPanel().getY() + getCurrentInfoPanel().getHeight())) {
				// getCurrentInfoPanel().mousePressed(mouseX - getCurrentInfoPanel().getX(), mouseY - getCurrentInfoPanel().getY());
				// }
				// continue;
				// }

				if (mouseButton == 0) {
					// Botón izquierdo pulsado, miramos a qué panel pertenece y llamamos a su función mousePressed (x, y) (con x e y relativas al panel)
					// Primero miramos que no haya un contextmenu
					if (getCurrentState () == STATE_SHOWING_CONTEXT_MENU) {
						// Context menú, miramos donde clica
						if (mouseX >= getCurrentContextMenu ().getX () && mouseX < (getCurrentContextMenu ().getX () + getCurrentContextMenu ().getWidth ()) && mouseY >= getCurrentContextMenu ().getY () && mouseY < (getCurrentContextMenu ().getY () + getCurrentContextMenu ().getHeight ())) {
							getCurrentContextMenu ().mousePressed (mouseX - getCurrentContextMenu ().getX (), mouseY - getCurrentContextMenu ().getY ());
						} else {
							// Cierra el menú
							deleteCurrentContextMenu ();
						}
					} else {
						if (getPanelUI ().isMouseOnAPanel (mouseX, mouseY) != UIPanel.MOUSE_NONE) {
							getPanelUI ().mousePressed (mouseX, mouseY, mouseButton);
						} else {
							getPanelMain ().mousePressed (mouseX, mouseY, mouseButton);
						}
					}
				} else if (mouseButton == 1) {
					if (UIPanel.typingPanel != null) {
						if (getPanelUI ().isMouseOnAPanel (mouseX, mouseY) != UIPanel.MOUSE_NONE) {
							getPanelUI ().mousePressed (mouseX, mouseY, mouseButton);
						}
					} else {
						// Botón derecho pulsado, cancelamos tarea (si hay)
						// y miramos que contextMenu cargar
						if (getCurrentState () == STATE_CREATING_TASK) {
							deleteCurrentTask ();
						}

						if (getCurrentState () == STATE_SHOWING_CONTEXT_MENU) {
							deleteCurrentContextMenu ();
						}

						if (mouseX >= 0 && mouseX < MainPanel.renderWidth && mouseY >= 0 && mouseY < MainPanel.renderHeight) {
							// Main panel
							if (getPanelUI ().isMouseOnAPanel (mouseX, mouseY) != UIPanel.MOUSE_NONE) {
								getPanelUI ().mousePressed (mouseX, mouseY, mouseButton);
							} else {
								setContextMenu (getPanelMain ().getContextMenu (mouseX, mouseY));
							}
							// } else if (mouseX >= getPanelMessages ().renderX && mouseX < (getPanelMessages ().renderX + getPanelMessages ().renderWidth) && mouseY >= getPanelMessages ().renderY && mouseY < (getPanelMessages ().renderY + getPanelMessages ().renderHeight)) {
							// Messages panel
							// setContextMenu (getPanelMessages ().getContextMenu (mouseX - getPanelMessages ().renderX, mouseY - getPanelMessages ().renderY));
						}
					}
				}
			}

			// Wheel
			if (Mouse.getEventDWheel () > 0) {
				if (getPanelMainMenu ().isActive ()) {
					continue;
				}
				world.keyPressed (Keyboard.KEY_NONE, UtilsKeyboard.FN_LEVEL_UP);
			} else if (Mouse.getEventDWheel () < 0) {
				if (getPanelMainMenu ().isActive ()) {
					continue;
				}
				world.keyPressed (Keyboard.KEY_NONE, UtilsKeyboard.FN_LEVEL_DOWN);
			}
		}

		// Bordes (scroll de mouse)
		if (isMouseScrollON () && Mouse.isInsideWindow ()) {
			if (!getPanelMainMenu ().isActive ()) {
				final int BORDE = UIPanel.PIXELS_TO_BORDER; // Si se acerca X pixels al borde moveremos la cámara
				if (mouseX < BORDE) {
					if (Game.isMouseScrollEarsON () || !UIPanel.isMouseCloseToOpenCloseProductionIcon (mouseX, mouseY)) {
						if (getCurrentState () == STATE_SHOWING_CONTEXT_MENU) {
							if (!(mouseX >= getCurrentContextMenu ().getX () && mouseX < (getCurrentContextMenu ().getX () + getCurrentContextMenu ().getWidth ()) && mouseY >= getCurrentContextMenu ().getY () && mouseY < (getCurrentContextMenu ().getY () + getCurrentContextMenu ().getHeight ()))) {
								world.keyPressed (Keyboard.KEY_NONE, UtilsKeyboard.FN_LEFT);
							}
						} else {
							world.keyPressed (Keyboard.KEY_NONE, UtilsKeyboard.FN_LEFT);
						}
					}
				} else if (mouseX > (UtilsGL.getWidth () - BORDE - 1)) {
					// Miramos que no esté cerca del botón de abrir/cerrar el menu
					if (Game.isMouseScrollEarsON () || !UIPanel.isMouseCloseToOpenCloseMenuIcon (mouseX, mouseY)) {
						if (getCurrentState () == STATE_SHOWING_CONTEXT_MENU) {
							if (!(mouseX >= getCurrentContextMenu ().getX () && mouseX < (getCurrentContextMenu ().getX () + getCurrentContextMenu ().getWidth ()) && mouseY >= getCurrentContextMenu ().getY () && mouseY < (getCurrentContextMenu ().getY () + getCurrentContextMenu ().getHeight ()))) {
								world.keyPressed (Keyboard.KEY_NONE, UtilsKeyboard.FN_RIGHT);
							}
						} else {
							world.keyPressed (Keyboard.KEY_NONE, UtilsKeyboard.FN_RIGHT);
						}
					}
				}
				if (mouseY < BORDE) {
					if (getCurrentState () == STATE_SHOWING_CONTEXT_MENU) {
						if (!(mouseX >= getCurrentContextMenu ().getX () && mouseX < (getCurrentContextMenu ().getX () + getCurrentContextMenu ().getWidth ()) && mouseY >= getCurrentContextMenu ().getY () && mouseY < (getCurrentContextMenu ().getY () + getCurrentContextMenu ().getHeight ()))) {
							world.keyPressed (Keyboard.KEY_NONE, UtilsKeyboard.FN_UP);
						}
					} else {
						world.keyPressed (Keyboard.KEY_NONE, UtilsKeyboard.FN_UP);
					}
				} else if (mouseY > (UtilsGL.getHeight () - BORDE - 1)) {
					if (Game.isMouseScrollEarsON () || !UIPanel.isMouseCloseToOpenCloseBottomIcon (mouseX, mouseY)) {
						if (getCurrentState () == STATE_SHOWING_CONTEXT_MENU) {
							if (!(mouseX >= getCurrentContextMenu ().getX () && mouseX < (getCurrentContextMenu ().getX () + getCurrentContextMenu ().getWidth ()) && mouseY >= getCurrentContextMenu ().getY () && mouseY < (getCurrentContextMenu ().getY () + getCurrentContextMenu ().getHeight ()))) {
								world.keyPressed (Keyboard.KEY_NONE, UtilsKeyboard.FN_DOWN);
							}
						} else {
							world.keyPressed (Keyboard.KEY_NONE, UtilsKeyboard.FN_DOWN);
						}
					}
				}
			}
		}
	}


	/**
	 * Obtiene los eventos de teclado y llama al World.keyPressed (int tecla)
	 */
	private void checkKeyboardEvents () {
		while (Keyboard.next ()) {
			if (Keyboard.getEventKeyState ()) {
				// Tecla pulsada
				int iKEY = Keyboard.getEventKey ();

				// Main menu
				if (getPanelMainMenu ().isActive ()) {
					// Función
					int iFN = UtilsKeyboard.getFN (iKEY);
					if (iFN == UtilsKeyboard.FN_TOGGLE_FULLSCREEN) {
						UtilsGL.toggleFullScreen ();
					} else {
						// Ha tecleado algo en el main menu, quizá seteando el nombre del savegame
						getPanelMainMenu ().keyPressed (iKEY);
					}
					continue;
				}

				if (UIPanel.typingPanel != null) {
					// Typing
					if (TypingPanel.keyPressed (iKEY)) {
						UIPanel.closeTypingPanel ();
					}
				} else {
					if (iKEY == Keyboard.KEY_ESCAPE && getCurrentState () == STATE_SHOWING_CONTEXT_MENU) {
						// Back al contextMenu
						if (currentContextMenu.getSmartMenu ().getParent () == null) {
							// No hay back posible, lo borramos
							currentContextMenu = null;
							setCurrentState (STATE_NO_STATE);
						} else {
							// Back
							currentContextMenu.setSmartMenu (currentContextMenu.getSmartMenu ().getParent ());
						}
						// } else if (iKEY == Keyboard.KEY_ESCAPE && getCurrentState() == STATE_SHOWING_INFO_PANEL) {
						// closeInfoPanel();
					} else {
						// Función
						int iFN = UtilsKeyboard.getFN (iKEY);

						if (iFN == UtilsKeyboard.FN_SHOW_MISSION) {
							if (getCurrentMissionData () == null) {
								continue;
							}
							UIPanel.toggleTutorialPanel (false);
							// if (getWorld().getCampaignID() == null || getWorld().getMissionID() == null) {
							// continue;
							// }
							// if (getCurrentState() != STATE_SHOWING_INFO_PANEL) {
							// // Panel de misión
							// MissionPanel panel = new MissionPanel(getWorld().getCampaignID(), getWorld().getMissionID());
							// if (getCurrentMissionData () != null && getCurrentMissionData ().getText() != null && getCurrentMissionData ().getObjectives() != null && getCurrentMissionData ().getObjectives().size() > 0) {
							// showInfoPanel(panel);
							// if (UIPanel.imagesPanel == null) {
							// UIPanel.imagesPanel = new ImagesPanel (MainPanel.renderWidth, MainPanel.renderHeight, getCurrentMissionData ().getTutorialImages ());
							// }
							// } else {
							// continue;
							// }
							// } else {
							// closeInfoPanel();
							// }
						} else if (iFN == UtilsKeyboard.FN_SHOW_STOCK) {
							UIPanel.setMatsPanelActive (!UIPanel.isMatsPanelActive ());
						} else if (iFN == UtilsKeyboard.FN_SHOW_PRIORITIES) {
							UIPanel.setPrioritiesPanelActive (!UIPanel.isPrioritiesPanelActive ());
						} else if (iFN == UtilsKeyboard.FN_SHOW_TRADE) {
							UIPanel.setTradePanelActive (!UIPanel.isTradePanelActive ());
							// } else if (iKEY == Keyboard.KEY_F5) {
							// UIPanel.setMenuPanelActive (!UIPanel.isMenuPanelActive ());
							// } else if (iKEY == Keyboard.KEY_F6) {
							// UIPanel.setBottomMenuPanelActive (!UIPanel.isBottomMenuPanelActive ());
						} else if (iFN == UtilsKeyboard.FN_TOGGLE_FULLSCREEN) {
							UtilsGL.toggleFullScreen ();
						} else if (iFN == UtilsKeyboard.FN_SPEED_DOWN) {
							if (World.SPEED > 1) {
								World.removeTurnsPerSecond ();
								MessagesPanel.addMessage (MessagesPanel.TYPE_SYSTEM, Messages.getString ("Game.6") + World.SPEED, ColorGL.YELLOW); //$NON-NLS-1$
							}
						} else if (iFN == UtilsKeyboard.FN_SPEED_UP) {
							if (World.SPEED < World.SPEED_MAX) {
								World.addTurnsPerSecond ();
								MessagesPanel.addMessage (MessagesPanel.TYPE_SYSTEM, Messages.getString ("Game.6") + World.SPEED, ColorGL.YELLOW); //$NON-NLS-1$
							}
						} else if (iFN == UtilsKeyboard.FN_TOGGLE_GRID) {
							MainPanel.toggleGrid ();
						} else if (iFN == UtilsKeyboard.FN_TOGGLE_FLAT_MOUSE) {
							MainPanel.toggleFlatMouse ();
						} else if (iFN == UtilsKeyboard.FN_TOGGLE_3D_MOUSE) {
							MainPanel.toggle3DMouse ();
						} else if (iFN == UtilsKeyboard.FN_TOGGLE_HIDE_UI) {
							MainPanel.toggleHideUI ();
						} else if (iFN == UtilsKeyboard.FN_TOGGLE_ITEM_BUILD_FACE) {
							MainPanel.toggleItemBuildFace ();
						} else if (iFN == UtilsKeyboard.FN_SCREENSHOT) {
							takeScreenshot = true;
						} else {
							// if (iFN == UtilsKeyboard.FN_UP || iFN == UtilsKeyboard.FN_DOWN || iFN == UtilsKeyboard.FN_LEFT || iFN == UtilsKeyboard.FN_RIGHT) {
							// Pasamos de los cursores que ya se encarga World de controlarlos
							world.keyPressed (iKEY, iFN);
							// }
						}
					}
				}
			}
		}
	}


	/**
	 * Se encarga de todo, OJO! No estamos extendiendo de runnable (single threaded)
	 */
	public void run () {
		// long fps = 0;
		// long lastFPS = (Sys.getTime () * 1000) / Sys.getTimerResolution ();

		// Hacemos sonar la música del main menú
		UtilsAL.play (UtilsAL.SOURCE_MUSIC_MAINMENU);
//		int checkTrigger = (4 * FPS_INGAME); // Para que lo mire nada más empezar, en el primer ciclo
//		boolean bVictory = false;

//		int iMissionCompletedWidth = (UtilFont.getWidth (Messages.getString ("Game.2"))) / 2; //$NON-NLS-1$
		while (!Display.isCloseRequested ()) {
			handleResize ();

			// Tratamos los eventos del mouse
			checkMouseEvents ();
			// Tratamos los eventos del teclado
			checkKeyboardEvents ();

			// Game logic
			if (!getPanelMainMenu ().isActive ()) {
				world.nextTurn ();
/*
				// Comprobamos la victoria cada 4xFPS_INGAME turnos (para ganar un poquillo de performance)
				if (bVictory == false && world != null && getWorld ().getCampaignID () != null) {
					checkVictory++;
					if (checkVictory >= (4 * FPS_INGAME)) {
						checkVictory = 0;
						MissionData md = CampaignManager.getMission (getWorld ().getCampaignID (), getWorld ().getMissionID ());
						if (md != null && ObjectiveData.checkCompleted (md.getObjectives ())) {
							// Victoria !!
							bVictory = true;
						}
					}
				}
			} else {
				bVictory = false;
*/

/*
				// Tutorial trigger?
				checkTrigger++;
				if (checkTrigger >= (4 * FPS_INGAME)) {
					checkTrigger = 0;
					if (Game.getCurrentMissionData () != null && Game.getCurrentMissionData ().getTutorialFlowIndex () < Game.getCurrentMissionData ().getTutorialFlows ().size ()) {
						if (Game.getCurrentMissionData ().getTutorialFlows ().get (Game.getCurrentMissionData ().getTutorialFlowIndex ()).getTriggers () != null && Game.getCurrentMissionData ().getTutorialFlows ().get (Game.getCurrentMissionData ().getTutorialFlowIndex ()).getTriggers ().size () > 0) {
							// There are triggers to check
							if (TutorialTrigger.checkCompleted (Game.getCurrentMissionData ().getTutorialFlows ().get (Game.getCurrentMissionData ().getTutorialFlowIndex ()).getTriggers ().get (0))) {
								// Trigger completed!
								Game.getCurrentMissionData ().getTutorialFlows ().get (Game.getCurrentMissionData ().getTutorialFlowIndex ()).getTriggers ().remove (0);
								// If no more triggers we update the index
								if (Game.getCurrentMissionData ().getTutorialFlows ().get (Game.getCurrentMissionData ().getTutorialFlowIndex ()).getTriggers ().size () == 0) {
									Game.getCurrentMissionData ().setTutorialFlowIndex (Game.getCurrentMissionData ().getTutorialFlowIndex () + 1);
								}

								// We trigger a new image on the images panel
								ImagesPanel.triggerNewFlow ();
							}
						} else {
							// No more triggers, we update the index
							Game.getCurrentMissionData ().setTutorialFlowIndex (Game.getCurrentMissionData ().getTutorialFlowIndex () + 1);
							//ImagesPanel.notifyFlowChanged ();
						}
					}
				}
*/
			}

			// FPS
			// if (((Sys.getTime () * 1000) / Sys.getTimerResolution ()) - lastFPS > 1000) {
			//				Display.setTitle ("FPS: " + fps); //$NON-NLS-1$
			// fps = 0;
			// lastFPS += 1000;
			// }
			// fps++;
			// Pongo este método pq dice la doc de LWJGL que actualiza los timers y tiene que llamarse lo más posible
			Sys.getTimerResolution ();

			// Render
			// if (getPanelMainMenu ().isActive () || (world != null && World.getCitizenIDs ().size () == 0)) {
			render ();

			// Misión completada?
/*
			if (bVictory) {
				GL11.glBindTexture (GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
				GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
				UtilsGL.glBegin (GL11.GL_QUADS);
				UtilsGL.drawStringWithBorder (Messages.getString ("Game.2"), (MainPanel.renderWidth / 2) - iMissionCompletedWidth, MainPanel.renderHeight / 2 - UtilFont.MAX_HEIGHT / 2, ColorGL.RED, ColorGL.WHITE); //$NON-NLS-1$
				UtilsGL.glEnd ();

				if (getCurrentState () == STATE_SHOWING_CONTEXT_MENU && getCurrentContextMenu () != null) {
					// ContextMenu
					getCurrentContextMenu ().render ();
				}
			}
*/

			// Updateamos la pantalla / ventana
			Display.update ();

			if (takeScreenshot) {
				takeScreenshot ();
			}

			if (getPanelMainMenu ().isActive ()) {
				Display.sync (FPS_MAINMENU); // Para "capear" a 30 fps
			} else {
				Display.sync (FPS_INGAME); // Para "capear" a 30 fps
			}

			// Borramos buffers
			GL11.glClear (GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_ACCUM_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
			// GL11.glClear (GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

			// if (getMainFrame ().isWantsToResize ()) {
			// getMainFrame ().setWantsToResize (false);
			// getMainFrame ().resize ();
			//
			// deleteCurrentContextMenu ();
			// if (getCurrentInfoPanel () != null) {
			// closeInfoPanel ();
			// }
			// }
			// }
		}

		// Exit
		Game.exit ();
	}


	public static void setBlinkingCells (int iFlowIndex, boolean bValue) {
		if (getCurrentMissionData () != null && iFlowIndex >= 0 && iFlowIndex < Game.getCurrentMissionData ().getTutorialFlows ().size ()) {
			// Check the triggers
			TutorialFlow flow = Game.getCurrentMissionData ().getTutorialFlows ().get (iFlowIndex);
			if (flow.getTriggers () != null) {
				Point3D p3d;
				for (int i = 0; i < flow.getTriggers ().size (); i++) {
					if (flow.getTriggers ().get (i).getType () == TutorialTrigger.TYPE_INT_PLACE) {
						p3d = flow.getTriggers ().get (i).getParamXYZ ();
						if (p3d != null && p3d.x >= 0 && p3d.x < World.MAP_WIDTH && p3d.y >= 0 && p3d.y < World.MAP_HEIGHT && p3d.z >= 0 && p3d.z < World.MAP_DEPTH) {
							World.getCell (p3d).setBlink (bValue);
						}
					}
				}
			}
		}
	}


	public static void updateTutorialFlow (int iTutorialType, int iParam, Point3D p3d) {
		updateTutorialFlow (iTutorialType, iParam, p3d, null);
	}


	public static void updateTutorialFlow (int iTutorialType, int iParam, Point3D p3d, String sParam) {
		updateTutorialFlow (iTutorialType, iParam, p3d, sParam, true);
	}


	public static void updateTutorialFlow (int iTutorialType, int iParam, Point3D p3d, String sParam, boolean bAdvanceIfOk) {
		if (missionData != null && missionData.getTutorialFlowIndex () < missionData.getTutorialFlows ().size () && missionData.getTutorialFlowIndex () == ImagesPanel.getCurrentFlowIndex ()) {
			TutorialFlow flow = missionData.getTutorialFlows ().get (missionData.getTutorialFlowIndex ());
			if (flow.getTriggers () != null && flow.getTriggers ().size () > 0) {
				int iMax = (flow.isOrderedTriggers ()) ? 1 : flow.getTriggers ().size ();
				for (int i = 0; i < iMax; i++) {
					TutorialTrigger trigger = flow.getTriggers ().get (i);

					if (iTutorialType == trigger.getType ()) {
						boolean bTriggerEnd = false;

						switch (iTutorialType) {
							case TutorialTrigger.TYPE_INT_COLLECT:
								// We check if the item created is the same one as in the param1
								if (UtilsIniHeaders.getIntIniHeader (trigger.getParam1 ()) == iParam) {
									trigger.setParam2 (trigger.getParam2 () - 1);
									if (trigger.getParam2 () <= 0) {
										bTriggerEnd = true;
									}
								}
								break;
							case TutorialTrigger.TYPE_INT_BUILD:
								if (UtilsIniHeaders.getIntIniHeader (trigger.getParam1 ()) == iParam) {
									trigger.setParam2 (trigger.getParam2 () - 1);
									if (trigger.getParam2 () <= 0) {
										bTriggerEnd = true;
									}
								}

								break;
							case TutorialTrigger.TYPE_INT_ZONE:
								if (UtilsIniHeaders.getIntIniHeader (trigger.getParam1 ()) == iParam) {
									trigger.setParam2 (trigger.getParam2 () - 1);
									if (trigger.getParam2 () <= 0) {
										bTriggerEnd = true;
									}
								}

								break;
							case TutorialTrigger.TYPE_INT_KILL:
								if (UtilsIniHeaders.getIntIniHeader (trigger.getParam1 ()) == iParam) {
									trigger.setParam2 (trigger.getParam2 () - 1);
									if (trigger.getParam2 () <= 0) {
										bTriggerEnd = true;
									}
								}

								break;
							case TutorialTrigger.TYPE_INT_PILE:
								if (UtilsIniHeaders.getIntIniHeader (trigger.getParam1 ()) == iParam) {
									trigger.setParam2 (trigger.getParam2 () - 1);
									if (trigger.getParam2 () <= 0) {
										bTriggerEnd = true;
									}
								}

								break;
							case TutorialTrigger.TYPE_INT_SOLDIER:
								trigger.setParam2 (trigger.getParam2 () - 1);
								if (trigger.getParam2 () <= 0) {
									bTriggerEnd = true;
								}

								break;
							case TutorialTrigger.TYPE_INT_WASD:
								trigger.setParam2 (trigger.getParam2 () | iParam); // Logical OR
								if (trigger.getParam2 () >= (TutorialTrigger.WASD_UP + TutorialTrigger.WASD_DOWN + TutorialTrigger.WASD_LEFT + TutorialTrigger.WASD_RIGHT)) {
									bTriggerEnd = true;
								}

								break;
							case TutorialTrigger.TYPE_INT_LAYERUPDOWN:
								trigger.setParam2 (trigger.getParam2 () | iParam); // Logical OR
								if (trigger.getParam2 () >= (TutorialTrigger.LAYER_UP + TutorialTrigger.LAYER_DOWN)) {
									bTriggerEnd = true;
								}

								break;
							case TutorialTrigger.TYPE_INT_ICONHIT:
								if (iParam == TutorialTrigger.ICON_INT_FLAT) {
									bTriggerEnd = (trigger.getParam1 () != null && trigger.getParam1 ().equalsIgnoreCase (TutorialTrigger.ICON_FLAT));
								} else if (iParam == TutorialTrigger.ICON_INT_GRID) {
									bTriggerEnd = (trigger.getParam1 () != null && trigger.getParam1 ().equalsIgnoreCase (TutorialTrigger.ICON_GRID));
								} else if (iParam == TutorialTrigger.ICON_INT_FLATCURSOR) {
									bTriggerEnd = (trigger.getParam1 () != null && trigger.getParam1 ().equalsIgnoreCase (TutorialTrigger.ICON_FLATCURSOR));
								} else if (iParam == TutorialTrigger.ICON_INT_3DMOUSE) {
									bTriggerEnd = (trigger.getParam1 () != null && trigger.getParam1 ().equalsIgnoreCase (TutorialTrigger.ICON_3DMOUSE));
								} else if (iParam == TutorialTrigger.ICON_INT_SETTINGS) {
									bTriggerEnd = (trigger.getParam1 () != null && trigger.getParam1 ().equalsIgnoreCase (TutorialTrigger.ICON_SETTINGS));
								} else if (iParam == TutorialTrigger.ICON_INT_LEVELUP) {
									bTriggerEnd = (trigger.getParam1 () != null && trigger.getParam1 ().equalsIgnoreCase (TutorialTrigger.ICON_LEVELUP));
								} else if (iParam == TutorialTrigger.ICON_INT_LEVELDOWN) {
									bTriggerEnd = (trigger.getParam1 () != null && trigger.getParam1 ().equalsIgnoreCase (TutorialTrigger.ICON_LEVELDOWN));
								} else if (iParam == TutorialTrigger.ICON_INT_SPEEDUP) {
									bTriggerEnd = (trigger.getParam1 () != null && trigger.getParam1 ().equalsIgnoreCase (TutorialTrigger.ICON_SPEEDUP));
								} else if (iParam == TutorialTrigger.ICON_INT_SPEEDDOWN) {
									bTriggerEnd = (trigger.getParam1 () != null && trigger.getParam1 ().equalsIgnoreCase (TutorialTrigger.ICON_SPEEDDOWN));
								} else if (iParam == TutorialTrigger.ICON_INT_PAUSE) {
									bTriggerEnd = (trigger.getParam1 () != null && trigger.getParam1 ().equalsIgnoreCase (TutorialTrigger.ICON_PAUSE));
								} else if (iParam == TutorialTrigger.ICON_INT_CITIZENS) {
									bTriggerEnd = (trigger.getParam1 () != null && trigger.getParam1 ().equalsIgnoreCase (TutorialTrigger.ICON_CITIZENS));
								} else if (iParam == TutorialTrigger.ICON_INT_SOLDIERS) {
									bTriggerEnd = (trigger.getParam1 () != null && trigger.getParam1 ().equalsIgnoreCase (TutorialTrigger.ICON_SOLDIERS));
								} else if (iParam == TutorialTrigger.ICON_INT_HEROES) {
									bTriggerEnd = (trigger.getParam1 () != null && trigger.getParam1 ().equalsIgnoreCase (TutorialTrigger.ICON_HEROES));
								} else if (iParam == TutorialTrigger.ICON_INT_TRADE) {
									bTriggerEnd = (trigger.getParam1 () != null && trigger.getParam1 ().equalsIgnoreCase (TutorialTrigger.ICON_TRADE));
								} else if (iParam == TutorialTrigger.ICON_INT_LIVINGS_LIVINGS) {
									bTriggerEnd = (trigger.getParam1 () != null && trigger.getParam1 ().equalsIgnoreCase (TutorialTrigger.ICON_LIVINGS_LIVINGS));
								} else if (iParam == TutorialTrigger.ICON_INT_LIVINGS_BODY) {
									bTriggerEnd = (trigger.getParam1 () != null && trigger.getParam1 ().equalsIgnoreCase (TutorialTrigger.ICON_LIVINGS_BODY));
								} else if (iParam == TutorialTrigger.ICON_INT_LIVINGS_AUTOEQUIP) {
									bTriggerEnd = (trigger.getParam1 () != null && trigger.getParam1 ().equalsIgnoreCase (TutorialTrigger.ICON_LIVINGS_AUTOEQUIP));
								} else if (iParam == TutorialTrigger.ICON_INT_LIVINGS_CONVERTSOLDIER) {
									bTriggerEnd = (trigger.getParam1 () != null && trigger.getParam1 ().equalsIgnoreCase (TutorialTrigger.ICON_LIVINGS_CONVERTSOLDIER));
								} else if (iParam == TutorialTrigger.ICON_INT_LIVINGS_JOBS) {
									bTriggerEnd = (trigger.getParam1 () != null && trigger.getParam1 ().equalsIgnoreCase (TutorialTrigger.ICON_LIVINGS_JOBS));
								} else if (iParam == TutorialTrigger.ICON_INT_LIVINGS_GROUPS) {
									bTriggerEnd = (trigger.getParam1 () != null && trigger.getParam1 ().equalsIgnoreCase (TutorialTrigger.ICON_LIVINGS_GROUPS));
								} else if (iParam == TutorialTrigger.ICON_INT_LIVINGS_CONVERTCIVILIAN) {
									bTriggerEnd = (trigger.getParam1 () != null && trigger.getParam1 ().equalsIgnoreCase (TutorialTrigger.ICON_LIVINGS_CONVERTCIVILIAN));
								} else if (iParam == TutorialTrigger.ICON_INT_LIVINGS_RESTRICTION) {
									bTriggerEnd = (trigger.getParam1 () != null && trigger.getParam1 ().equalsIgnoreCase (TutorialTrigger.ICON_LIVINGS_RESTRICTION));
								} else if (iParam == TutorialTrigger.ICON_INT_LIVINGS_GUARD) {
									bTriggerEnd = (trigger.getParam1 () != null && trigger.getParam1 ().equalsIgnoreCase (TutorialTrigger.ICON_LIVINGS_GUARD));
								} else if (iParam == TutorialTrigger.ICON_INT_LIVINGS_PATROL) {
									bTriggerEnd = (trigger.getParam1 () != null && trigger.getParam1 ().equalsIgnoreCase (TutorialTrigger.ICON_LIVINGS_PATROL));
								} else if (iParam == TutorialTrigger.ICON_INT_LIVINGS_BOSS) {
									bTriggerEnd = (trigger.getParam1 () != null && trigger.getParam1 ().equalsIgnoreCase (TutorialTrigger.ICON_LIVINGS_BOSS));
								} else if (iParam == TutorialTrigger.ICON_INT_REGULAR_PLUS) {
									if (trigger.getParam1 () != null && sParam != null) {
										int iComma = trigger.getParam1 ().indexOf (',');
										if (iComma != -1 && iComma < (trigger.getParam1 ().length () - 1)) {
											String sIcon = trigger.getParam1 ().substring (0, iComma);
											String sButton = trigger.getParam1 ().substring (iComma + 1);
											if (sIcon.equalsIgnoreCase (TutorialTrigger.ICON_REGULAR_PLUS)) {
												if (sParam.equalsIgnoreCase (sButton)) {
													bTriggerEnd = true;
												}
											}
										}
									}
								} else if (iParam == TutorialTrigger.ICON_INT_REGULAR_MINUS) {
									if (trigger.getParam1 () != null && sParam != null) {
										int iComma = trigger.getParam1 ().indexOf (',');
										if (iComma != -1 && iComma < (trigger.getParam1 ().length () - 1)) {
											String sIcon = trigger.getParam1 ().substring (0, iComma);
											String sButton = trigger.getParam1 ().substring (iComma + 1);
											if (sIcon.equalsIgnoreCase (TutorialTrigger.ICON_REGULAR_MINUS)) {
												if (sParam.equalsIgnoreCase (sButton)) {
													bTriggerEnd = true;
												}
											}
										}
									}
								} else if (iParam == TutorialTrigger.ICON_INT_AUTOMATED_PLUS) {
									if (trigger.getParam1 () != null && sParam != null) {
										int iComma = trigger.getParam1 ().indexOf (',');
										if (iComma != -1 && iComma < (trigger.getParam1 ().length () - 1)) {
											String sIcon = trigger.getParam1 ().substring (0, iComma);
											String sButton = trigger.getParam1 ().substring (iComma + 1);
											if (sIcon.equalsIgnoreCase (TutorialTrigger.ICON_AUTOMATED_PLUS)) {
												if (sParam.equalsIgnoreCase (sButton)) {
													bTriggerEnd = true;
												}
											}
										}
									}
								} else if (iParam == TutorialTrigger.ICON_INT_AUTOMATED_MINUS) {
									if (trigger.getParam1 () != null && sParam != null) {
										int iComma = trigger.getParam1 ().indexOf (',');
										if (iComma != -1 && iComma < (trigger.getParam1 ().length () - 1)) {
											String sIcon = trigger.getParam1 ().substring (0, iComma);
											String sButton = trigger.getParam1 ().substring (iComma + 1);
											if (sIcon.equalsIgnoreCase (TutorialTrigger.ICON_AUTOMATED_MINUS)) {
												if (sParam.equalsIgnoreCase (sButton)) {
													bTriggerEnd = true;
												}
											}
										}
									}
								}

								break;
							case TutorialTrigger.TYPE_INT_PLACE:
								if (p3d != null && trigger.getParamXYZ () != null) {
									if (UtilsIniHeaders.getIntIniHeader (trigger.getParam1 ()) == iParam) {
										if (p3d.x == trigger.getParamXYZ ().x && p3d.y == trigger.getParamXYZ ().y && p3d.z == trigger.getParamXYZ ().z) {
											bTriggerEnd = true;
											World.getCell (p3d.x, p3d.y, p3d.z).setBlink (false);
										}
									}
								}
								break;
							case TutorialTrigger.TYPE_INT_POPULATION:
								// iParam will have the current population
								bTriggerEnd = iParam >= trigger.getParam2 ();
								break;
							case TutorialTrigger.TYPE_INT_TILL:
								trigger.setParam2 (trigger.getParam2 () - 1);
								if (trigger.getParam2 () <= 0) {
									bTriggerEnd = true;
								}
								break;
							case TutorialTrigger.TYPE_INT_CIV2GROUP:
								if (trigger.getParam2 () == iParam) {
									bTriggerEnd = true;
								}

								break;
						}

						if (bTriggerEnd) {
							flow.getTriggers ().remove (i);
							updateTutorialFlow (TutorialTrigger.TYPE_INT_NONE, 0, null);
							break;
						}
					} else {
						// Here I'll check for NONE triggers and delete the PLACE ones if the items are already set
						if (iTutorialType == TutorialTrigger.TYPE_INT_NONE && trigger.getType () == TutorialTrigger.TYPE_INT_PLACE) {
							if (trigger.getParamXYZ () != null) {
								// We check the cell
								Cell cell = World.getCell (trigger.getParamXYZ ());
								Item item = cell.getItem ();
								if (item != null && item.getNumericIniHeader () == UtilsIniHeaders.getIntIniHeader (trigger.getParam1 ())) {
									// Bingo !
									// We should delete this trigger because is already been done
									flow.getTriggers ().remove (i);
									updateTutorialFlow (TutorialTrigger.TYPE_INT_NONE, 0, null);
									break;
								}
							}
						}
					}
				}
			} else {
				// Flow advance (not if is the last flow)
				if ((missionData.getTutorialFlowIndex () + 1) < missionData.getTutorialFlows ().size ()) {
					missionData.setTutorialFlowIndex (missionData.getTutorialFlowIndex () + 1);
					ImagesPanel.triggerNewFlow (bAdvanceIfOk);
					updateTutorialFlow (TutorialTrigger.TYPE_INT_NONE, 0, null, null, false);
				}
			}
		}
	}


	/**
	 * handles a possible resize of the display
	 */
	private void handleResize () {
		if (Display.wasResized () || Display.isFullscreen () != displayFullscreen) {
			int iWidth = Display.getWidth ();
			int iHeight = Display.getHeight ();

			boolean resetDisplayMode = false;
			if (iWidth < MIN_DISPLAY_WIDTH) {
				iWidth = MIN_DISPLAY_WIDTH;
				resetDisplayMode = true;
			}
			if (iHeight < MIN_DISPLAY_HEIGHT) {
				iHeight = MIN_DISPLAY_HEIGHT;
				resetDisplayMode = true;
			}

			if (resetDisplayMode) {
				try {
					Display.setDisplayMode (new DisplayMode (iWidth, iHeight));
				}
				catch (LWJGLException ex) {
					Log.log (Log.LEVEL_ERROR, "Problem encountered when setting the display mode: " + ex, "Game");
				}
			}

			MainPanel.resize (iWidth, iHeight);
			if (Game.getWorld () == null) {
				Game.getPanelUI ().resize (iWidth, iHeight, null, null, false);
			} else {
				Game.getPanelUI ().resize (iWidth, iHeight, Game.getWorld ().getCampaignID (), Game.getWorld ().getMissionID (), true);
			}

			Game.getPanelCommand ().resize (iWidth - World.MAP_WIDTH, World.MAP_HEIGHT, World.MAP_WIDTH, iHeight - World.MAP_HEIGHT);
			Game.getPanelMainMenu ().resize (0, 0, iWidth, iHeight);

			UtilsGL.initGLModes ();
			UtilsGL.onResize (iWidth, iHeight, Display.isFullscreen ());
			Utils.saveOptions ();

			displayFullscreen = Display.isFullscreen ();
		}
	}


	public static void render () {
		if (getPanelMainMenu ().isActive ()) {
			// Main menu
			getPanelMainMenu ().render ();
		} else {
			// Main Panel
			MainPanel.render ();

			if (getCurrentState () == STATE_SHOWING_CONTEXT_MENU) {
				// ContextMenu
				getCurrentContextMenu ().render ();
				// } else if (getCurrentState() == STATE_SHOWING_INFO_PANEL) {
				// // Info panel
				// getCurrentInfoPanel().render();
			}
		}
	}


	private static void takeScreenshot () {
		takeScreenshot = false;

		// Buscamos un nombre válido
		File file = null;
		boolean bNameFound = false;
		int iNumber = 1;
		while (!bNameFound) {
			file = new File (Game.getUserFolder () + Game.getFileSeparator () + Game.SCREENSHOTS_FOLDER1 + Game.getFileSeparator () + "Towns_" + iNumber + ".png"); //$NON-NLS-1$ //$NON-NLS-2$
			if (file.exists ()) {
				iNumber++;
			} else {
				bNameFound = true;
			}
		}

		try {
			// Tomamos el pantallazo
			GL11.glReadBuffer (GL11.GL_FRONT);
			int width = UtilsGL.getWidth ();
			int height = UtilsGL.getHeight ();
			int bpp = 4; // Assuming a 32-bit display with a byte each for red, green, blue, and alpha.
			ByteBuffer buffer = BufferUtils.createByteBuffer (width * height * bpp);
			GL11.glReadPixels (0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

			// Save
			String format = "PNG"; //$NON-NLS-1$
			BufferedImage image = new BufferedImage (width, height, BufferedImage.TYPE_INT_RGB);

			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					int i = (x + (width * y)) * bpp;
					int r = buffer.get (i) & 0xFF;
					int g = buffer.get (i + 1) & 0xFF;
					int b = buffer.get (i + 2) & 0xFF;
					image.setRGB (x, height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
				}
			}

			ImageIO.write (image, format, file);
		}
		catch (Exception e) {
			Log.log (Log.LEVEL_ERROR, Messages.getString ("Game.17") + " [" + file.getName () + "]", "Game"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
	}


	public static void setMusicON (boolean bMusic) {
		musicON = bMusic;
	}


	public static boolean isMusicON () {
		return musicON;
	}


	public static void setFXON (boolean bFX) {
		FXON = bFX;
	}


	public static boolean isFXON () {
		return FXON;
	}


	public static void setMouseScrollON (boolean mouseScrollON) {
		Game.mouseScrollON = mouseScrollON;
	}


	public static boolean isMouseScrollON () {
		return mouseScrollON;
	}


	public static void setMouseScrollEarsON (boolean mouseScrollEarsON) {
		Game.mouseScrollEarsON = mouseScrollEarsON;
	}


	public static boolean isMouseScrollEarsON () {
		return mouseScrollEarsON;
	}


	public static void setMouse2DCubesON (boolean mouse2DCubesON) {
		Game.mouse2DCubesON = mouse2DCubesON;
	}


	public static boolean isMouse2DCubesON () {
		return mouse2DCubesON;
	}


	public static void setDisabledItemsON (boolean disabledItemsON) {
		Game.disabledItemsON = disabledItemsON;
	}


	public static boolean isDisabledItemsON () {
		return disabledItemsON;
	}


	public static void setDisabledGodsON (boolean disabledGodsON) {
		Game.disabledGodsON = disabledGodsON;
	}


	public static boolean isDisabledGodsON () {
		return disabledGodsON;
	}


	public static void setPauseStartON (boolean pauseStartON) {
		Game.pauseStartON = pauseStartON;
	}


	public static boolean isPauseStartON () {
		return pauseStartON;
	}


	public static void setAutosaveDays (int autosaveDays) {
		if (autosaveDays > 10) {
			autosaveDays = 0;
		}

		Game.autosaveDays = autosaveDays;
	}


	public static int getAutosaveDays () {
		return autosaveDays;
	}


	public static void setSiegeDifficulty (int siegeDifficulty) {
		Game.siegeDifficulty = siegeDifficulty;
	}


	public static int getSiegeDifficulty () {
		return siegeDifficulty;
	}


	public static void setSiegePause (boolean siegePause) {
		Game.siegePause = siegePause;
	}


	public static boolean isSiegePause () {
		return siegePause;
	}


	public static void setCaravanPause (boolean caravanPause) {
		Game.caravanPause = caravanPause;
	}


	public static boolean isCaravanPause () {
		return caravanPause;
	}


	public static void setAllowBury (boolean allowBury) {
		Game.allowBury = allowBury;
	}


	public static boolean isAllowBury () {
		return allowBury;
	}


	public static void setVolumeMusic (int volume) {
		if (volume < 1 || volume > 10) {
			volume = 10;
		}

		Game.volumeMusic = volume;
	}


	public static int getVolumeMusic () {
		return volumeMusic;
	}


	public static void addMusicVolume () {
		volumeMusic++;
		if (volumeMusic > 10) {
			volumeMusic = 1;
		}

		UtilsAL.setMusicVolume (volumeMusic);
	}


	public static void setVolumeFX (int volume) {
		if (volume < 1 || volume > 10) {
			volume = 10;
		}

		Game.volumeFX = volume;
	}


	public static int getVolumeFX () {
		return volumeFX;
	}


	public static void addFXVolume () {
		volumeFX++;
		if (volumeFX > 10) {
			volumeFX = 1;
		}

		UtilsAL.setFXVolume (volumeFX);
	}


	public static void setPathfindingCPULevel (int pathfindingCPULevel) {
		if (pathfindingCPULevel < 1 || pathfindingCPULevel > 6) {
			Game.pathfindingCPULevel = 1;
		} else {
			Game.pathfindingCPULevel = pathfindingCPULevel;
		}

		if (Game.pathfindingCPULevel == 1) {
			AStarQueue.NUM_ITERATIONS = 2048;
		} else if (Game.pathfindingCPULevel == 2) {
			AStarQueue.NUM_ITERATIONS = 2048 * 2;
		} else if (Game.pathfindingCPULevel == 3) {
			AStarQueue.NUM_ITERATIONS = 2048 * 4;
		} else if (Game.pathfindingCPULevel == 4) {
			AStarQueue.NUM_ITERATIONS = 2048 * 8;
		} else if (Game.pathfindingCPULevel == 5) {
			AStarQueue.NUM_ITERATIONS = 2048 * 16;
		} else {
			AStarQueue.NUM_ITERATIONS = 2048 * 32;
		}
	}


	public static int getPathfindingCPULevel () {
		return pathfindingCPULevel;
	}


	public static void setSavegameName (String savegameName) {
		Game.savegameName = savegameName;
	}


	public static String getSavegameName () {
		return savegameName;
	}


	public static void setModsLoaded (String sMods) {
		alModsLoaded.clear ();
		if (sMods != null && sMods.length () > 0) {
			StringTokenizer tokenizer = new StringTokenizer (sMods, ","); //$NON-NLS-1$
			while (tokenizer.hasMoreTokens ()) {
				String sModName = tokenizer.nextToken ();
				if (sModName.length () > 0) {
					alModsLoaded.add (sModName);
				}
			}
			sortModsLoaded ();
		}
	}


	public static boolean isModLoaded (String sMod) {
		if (sMod != null && sMod.length () > 0) {
			return alModsLoaded.contains (sMod);
		}

		return false;
	}


	public static ArrayList<String> getModsLoaded () {
		return alModsLoaded;
	}


	public static String getModsLoadedString () {
		StringBuffer buffer = new StringBuffer ();

		for (int i = 0; i < alModsLoaded.size (); i++) {
			buffer.append (alModsLoaded.get (i));
			if (i < (alModsLoaded.size () - 1)) {
				buffer.append (","); //$NON-NLS-1$
			}
		}

		return buffer.toString ();
	}


	public static void toggleMod (String sMod) {
		if (sMod == null || sMod.trim ().length () == 0) {
			return;
		}

		if (isModLoaded (sMod)) {
			alModsLoaded.remove (sMod);
		} else {
			alModsLoaded.add (sMod);
			sortModsLoaded ();
		}
	}


	private static void sortModsLoaded () {
		if (alModsLoaded.size () > 1) {
			for (int i = 0; i < (alModsLoaded.size () - 1); i++) {
				for (int j = i + 1; j < alModsLoaded.size (); j++) {
					if (alModsLoaded.get (i).compareTo (alModsLoaded.get (j)) > 0) {
						// Swap
						String sAux = alModsLoaded.get (i);
						alModsLoaded.set (i, alModsLoaded.get (j));
						alModsLoaded.set (j, sAux);
					}
				}
			}
		}
	}


	public static void setServers (String sServers) {
		// Servers and names
		alServers.clear ();
		alServerNames.clear ();

		if (sServers != null && sServers.trim ().length () > 0) {
			StringTokenizer tokenizer = new StringTokenizer (sServers, ","); //$NON-NLS-1$
			while (tokenizer.hasMoreTokens ()) {
				String sServerAddress = tokenizer.nextToken ();
				if (sServerAddress.length () > 0) {
					alServers.add (sServerAddress.trim ());

					// Buscamos el nombre
					alServerNames.add (UtilsServer.getServerName (sServerAddress.trim ()));
				}
			}
			sortServers ();
		}
	}


	public static ArrayList<String> getServers () {
		return alServers;
	}


	public static ArrayList<String> getServerNames () {
		return alServerNames;
	}


	private static void sortServers () {
		if (alServerNames.size () > 1) {
			for (int i = 0; i < (alServerNames.size () - 1); i++) {
				for (int j = i + 1; j < alServerNames.size (); j++) {
					if (alServerNames.get (i).compareTo (alServerNames.get (j)) > 0) {
						// Swap
						String sAux = alServers.get (i);
						alServers.set (i, alServers.get (j));
						alServers.set (j, sAux);

						String sAux2 = alServerNames.get (i);
						alServerNames.set (i, alServerNames.get (j));
						alServerNames.set (j, sAux2);
					}
				}
			}
		}
	}


	public static String getServersString () {
		StringBuffer buffer = new StringBuffer ();

		for (int i = 0; i < alServers.size (); i++) {
			buffer.append (alServers.get (i));
			if (i < (alServers.size () - 1)) {
				buffer.append (","); //$NON-NLS-1$
			}
		}

		return buffer.toString ();
	}


	public static void addServer (String sServer) {
		if (sServer == null || sServer.trim ().length () == 0) {
			return;
		}

		if (!alServers.contains (sServer.trim ())) {
			alServers.add (sServer.trim ());
			alServerNames.add (UtilsServer.getServerName (sServer.trim ()));
		}
	}


	public static void removeServer (String sServer) {
		if (sServer == null || sServer.trim ().length () == 0) {
			return;
		}

		int iIndex = alServers.indexOf (sServer.trim ());
		if (iIndex != -1) {
			alServers.remove (iIndex);
			alServerNames.remove (iIndex);
		}
	}


	/**
	 * Devuelve el texture ID o 0 si lo que se pasa no tiene texture extra
	 * 
	 * @param sFilename
	 * @return el texture ID o 0 si lo que se pasa no tiene texture extra
	 */
	public static int getExtraTextureID (String sFilename) {
		if (sFilename == null || sFilename.trim ().length () == 0) {
			return 0;
		}

		Integer iTextureID = hmExtraTextures.get (sFilename);
		if (iTextureID == null) {
			return 0;
		}

		return iTextureID.intValue ();
	}


	public static void setServerToUse (int serverToUse) {
		Game.serverToUse = serverToUse;
	}


	public static int getServerToUse () {
		return serverToUse;
	}


	public static void exitToMainMenu () {
		AStarQueue.exit ();
		while (!AStarQueue.isExitOK ()) {
			try {
				Thread.sleep (100);
			}
			catch (InterruptedException e) {
			}
		}

		MessagesPanel.clear ();
		MiniMapPanel.clear ();
		panelCommand.clear ();
		panelMain.clear ();
		UIPanel.clear ();
		
		// Tutorial
		
		
		missionData = null;

		if (world != null) {
			world.clear ();
			world = null;
		}

		currentState = STATE_NO_STATE;
		currentTask = null;
		currentContextMenu = null;

		// Managers
		// matspanel.xml, menu_*.xml
		ActionManager.clear ();
		ActionPriorityManager.clear ();
		ItemManager.clear ();
		BuildingManager.clear ();
		CampaignManager.clear ();
		LivingEntityManager.clear ();
		TerrainManager.clear ();
		SkillManager.clear ();
		ZoneManager.clear ();
		Type.clear ();
		Types.clear ();
		EffectManager.clear ();
		CaravanManager.clear ();
		HeroManager.clear ();
		Names.clear ();
		PrefixSuffixManager.clear ();
		PricesManager.clear ();
		MatsPanelData.clear ();

		DungeonManager.clear ();

		Item.clear ();

		setPaused (false);

		getPanelMainMenu ().createMenu ();
		getPanelMainMenu ().setActive (true);

		Towns.clearPropertiesGraphics ();
	}


	public static void exit () {
		UtilsGL.destroy ();
		UtilsAL.destroy ();
		System.exit (0);
	}
}
