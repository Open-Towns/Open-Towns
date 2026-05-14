package xaos.panels;

import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.util.ArrayList;

import xaos.platform.lwjgl3.opengl.Display;
import org.lwjgl.opengl.GL11;

import xaos.Towns;
import xaos.TownsProperties;
import xaos.actions.ActionManager;
import xaos.actions.ActionManagerItem;
import xaos.campaign.TutorialTrigger;
import xaos.data.CitizenGroupData;
import xaos.data.CitizenGroups;
import xaos.data.Type;
import xaos.data.Types;
import xaos.events.EventManager;
import xaos.events.EventManagerItem;
import xaos.main.Game;
import xaos.main.World;
import xaos.panels.UI.UIPanel;
import xaos.panels.menus.ContextMenu;
import xaos.panels.menus.SmartMenu;
import xaos.stockpiles.Stockpile;
import xaos.tasks.Task;
import xaos.tiles.Cell;
import xaos.tiles.Tile;
import xaos.tiles.entities.items.Container;
import xaos.tiles.entities.items.Item;
import xaos.tiles.entities.items.ItemManager;
import xaos.tiles.entities.items.ItemManagerItem;
import xaos.tiles.entities.living.Citizen;
import xaos.tiles.entities.living.LivingEntity;
import xaos.tiles.entities.living.LivingEntityManager;
import xaos.tiles.entities.living.LivingEntityManagerItem;
import xaos.tiles.entities.living.heroes.Hero;
import xaos.utils.ColorGL;
import xaos.utils.Log;
import xaos.utils.Messages;
import xaos.utils.Point3D;
import xaos.utils.Utils;
import xaos.utils.UtilsAL;
import xaos.utils.UtilsGL;
import xaos.panels.UI.UIPanelState;

public final class CommandPanel {

    private static final long serialVersionUID = 5224811443500566092L;

    // MAIN MENU
    public static String COMMAND_MM_NEWGAME = "NEWGAME"; //$NON-NLS-1$
    public static String COMMAND_MM_NEWGAME_SET_SAVE_NAME = "NEWGAMESETSAVENAME"; //$NON-NLS-1$
    public static String COMMAND_MM_NEWGAME_SET_SAVE_NAME_NO_BURY = "NEWGAMESETSAVENAMENB"; //$NON-NLS-1$
    public static String COMMAND_MM_CONTINUEGAME = "CONTINUEGAME"; //$NON-NLS-1$
    public static String COMMAND_MM_DELETEGAME = "DELETEGAME"; //$NON-NLS-1$
    public static String COMMAND_MM_SWITCH_MUSIC = "SWITCHMUSIC"; //$NON-NLS-1$
    public static String COMMAND_MM_ADD_MUSIC_VOLUME = "ADDMVOL"; //$NON-NLS-1$
    public static String COMMAND_MM_SWITCH_FX = "SWITCHFX"; //$NON-NLS-1$
    public static String COMMAND_MM_ADD_FX_VOLUME = "ADDFVOL"; //$NON-NLS-1$
    public static String COMMAND_MM_TOGGLE_FULL_SCREEN = "TOGGLEFULLSCREEN"; //$NON-NLS-1$
    public static String COMMAND_MM_SWITCH_MOUSE_SCROLL = "SWITCHMOUSESCROLL"; //$NON-NLS-1$
    public static String COMMAND_MM_SWITCH_MOUSE_SCROLL_EARS = "SWITCHMOUSESCROLLEARS"; //$NON-NLS-1$
    public static String COMMAND_MM_SWITCH_MOUSE_2D_CUBES = "SWITCHMOUSE2DCUBES"; //$NON-NLS-1$
    public static String COMMAND_MM_SWITCH_DISABLE_ITEMS = "SWITCHDISABLEITEMS"; //$NON-NLS-1$
    public static String COMMAND_MM_SWITCH_DISABLE_GODS = "SWITCHDISABLEGODS"; //$NON-NLS-1$
    public static String COMMAND_MM_SWITCH_PAUSE = "SWITCHPAUSE"; //$NON-NLS-1$
    public static String COMMAND_MM_SWITCH_AUTOSAVE_DAYS = "SWITCHAUTOSAVE"; //$NON-NLS-1$
    public static String COMMAND_MM_SWITCH_SIEGES = "SWITCHSIEGES"; //$NON-NLS-1$
    public static String COMMAND_MM_SWITCH_SIEGE_PAUSE = "SWITCHSIEGEPAUSE"; //$NON-NLS-1$
    public static String COMMAND_MM_SWITCH_CARAVAN_PAUSE = "SWITCHCARPAUSE"; //$NON-NLS-1$
    public static String COMMAND_MM_SWITCH_BURY = "SWITCHBURY"; //$NON-NLS-1$
    public static String COMMAND_MM_DELETE_ERROR = "MMDELETEERROR"; //$NON-NLS-1$
    public static String COMMAND_CHANGE_HOTKEY = "CHGHOTKEY"; //$NON-NLS-1$
    public static String COMMAND_MM_SWITCH_PATHFINDING_LEVEL = "SWITCHPFL"; //$NON-NLS-1$
    public static String COMMAND_TOGGLE_MOD = "TOGGLEMOD"; //$NON-NLS-1$
    public static String COMMAND_SERVER_ADD = "ADDSERVER"; //$NON-NLS-1$
    public static String COMMAND_SERVER_REMOVE = "REMOVESERVER"; //$NON-NLS-1$
    public static String COMMAND_OPEN_FOLDER = "OPENFOLDER"; //$NON-NLS-1$

    // Order commands
    public static final String COMMAND_MINE = "MINE"; //$NON-NLS-1$
    public static final String COMMAND_MINE_LADDER = "MINELADDER"; //$NON-NLS-1$
    public static final String COMMAND_DIG = "DIG"; //$NON-NLS-1$
    public static final String COMMAND_CANCEL_ORDER = "CANCELORDER"; //$NON-NLS-1$
    public static final String COMMAND_WEAR = "WEAR"; //$NON-NLS-1$
    public static final String COMMAND_WEAR_OFF = "WEAROFF"; //$NON-NLS-1$
    public static final String COMMAND_AUTOEQUIP = "AUTOEQUIP"; //$NON-NLS-1$

    // Terrain commands
    public static final String COMMAND_TERRAIN_CHANGE = "TERRAINCHANGE"; //$NON-NLS-1$
    public static final String COMMAND_TERRAIN_ADD_FLUID = "TERRAINADDFLUID"; //$NON-NLS-1$
    public static final String COMMAND_TERRAIN_REMOVE_FLUID = "TERRAINREMOVEFLUID"; //$NON-NLS-1$

    // Buildings
    public static final String COMMAND_BUILD = "BUILD"; //$NON-NLS-1$
    public static final String COMMAND_TURN_OFF_NONSTOP = "TURNOFFNONSTOP"; //$NON-NLS-1$
    public static final String COMMAND_TURN_ON_NONSTOP = "TURNONNONSTOP"; //$NON-NLS-1$
    public static final String COMMAND_REMOVE_BUILDING_TASK = "REMOVEBULDINGTASK"; //$NON-NLS-1$
    public static final String COMMAND_DESTROY_BUILDING = "DESTROYBUILDING"; //$NON-NLS-1$

    // Items
    public static final String COMMAND_CREATE = "CREATE"; //$NON-NLS-1$
    public static final String COMMAND_CREATE_IN_A_BUILDING = "CREATEINABUILDING"; //$NON-NLS-1$
    public static final String COMMAND_CREATE_AND_PLACE = "CREATEANDPLACE"; //$NON-NLS-1$
    public static final String COMMAND_CREATE_AND_PLACE_ROW = "CREATEANDPLACEROW"; //$NON-NLS-1$
    public static final String COMMAND_LOCK = "LOCK"; //$NON-NLS-1$
    public static final String COMMAND_UNLOCK_OPEN = "UNLOCKOPEN"; //$NON-NLS-1$
    public static final String COMMAND_UNLOCK_CLOSE = "UNLOCKCLOSE"; //$NON-NLS-1$
    public static final String COMMAND_ITEM_TEXT_ADD = "ADDTEXT"; //$NON-NLS-1$
    public static final String COMMAND_ITEM_TEXT_DELETE = "DELETETEXT"; //$NON-NLS-1$
    public static final String COMMAND_ITEM_ROTATE = "ROTATEITEM"; //$NON-NLS-1$
    public static final String COMMAND_UNLOCK = "UNLOCK"; //$NON-NLS-1$

    // Containers
    public static final String COMMAND_CONTAINER_ENABLE_ALL = "CONTAINERENABLEALL"; //$NON-NLS-1$
    public static final String COMMAND_CONTAINER_DISABLE_ALL = "CONTAINERDISABLEALL"; //$NON-NLS-1$
    public static final String COMMAND_CONTAINER_ENABLE_ITEM = "CONTAINERENABLEITEM"; //$NON-NLS-1$
    public static final String COMMAND_CONTAINER_DISABLE_ITEM = "CONTAINERDISABLEITEM"; //$NON-NLS-1$
    public static final String COMMAND_CONTAINER_MANAGE = "CONTAINERMANAGE"; //$NON-NLS-1$
    public static final String COMMAND_CONTAINER_COPY_TO_ALL = "CONTAINERCOPY"; //$NON-NLS-1$

    // Professions
    public static final String COMMAND_PROFESSIONS_ENABLE_ALL = "PROFENABLEALL"; //$NON-NLS-1$
    public static final String COMMAND_PROFESSIONS_DISABLE_ALL = "PROFDISABLEALL"; //$NON-NLS-1$
    public static final String COMMAND_PROFESSIONS_ENABLE_ITEM = "PROFENABLEITEM"; //$NON-NLS-1$
    public static final String COMMAND_PROFESSIONS_DISABLE_ITEM = "PROFDISABLEITEM"; //$NON-NLS-1$

    // Entities
    public static final String COMMAND_DESTROY_ENTITY = "DESTROYENTITY"; //$NON-NLS-1$

    // Stockpiles
    public static final String COMMAND_STOCKPILE = "STOCKPILE"; //$NON-NLS-1$
    public static final String COMMAND_MANAGE_STOCKPILE = "MANAGESTOCKPILE"; //$NON-NLS-1$
    public static final String COMMAND_DELETE_STOCKPILE = "DELETESTOCKPILE"; //$NON-NLS-1$
    public static final String COMMAND_STOCKPILE_ENABLE_ITEM = "STOCKPILEENABLEITEM"; //$NON-NLS-1$
    public static final String COMMAND_STOCKPILE_DISABLE_ITEM = "STOCKPILEDISABLEITEM"; //$NON-NLS-1$
    public static final String COMMAND_STOCKPILE_ENABLE_ALL = "STOCKPILEENABLEALL"; //$NON-NLS-1$
    public static final String COMMAND_STOCKPILE_DISABLE_ALL = "STOCKPILEDISABLEALL"; //$NON-NLS-1$
    public static final String COMMAND_STOCKPILE_MANAGE = "STOCKPILEMANAGE"; //$NON-NLS-1$
    public static final String COMMAND_STOCKPILE_COPY_TO_ALL = "STOCKPILECOPY"; //$NON-NLS-1$

    // Zones
    public static final String COMMAND_CREATE_ZONE = "CREATEZONE"; //$NON-NLS-1$
    public static final String COMMAND_DELETE_ZONE = "DELETEZONE"; //$NON-NLS-1$
    public static final String COMMAND_EXPAND_ZONE = "EXPANDZONE"; //$NON-NLS-1$
    public static final String COMMAND_CHANGE_OWNER = "CHANGEOWNER"; //$NON-NLS-1$
    public static final String COMMAND_CHANGE_OWNER_GROUP = "CHANGEOWNERGROUP"; //$NON-NLS-1$

    // Citizens
    public static final String COMMAND_CONVERT_TO_CIVILIAN = "CONVERTTOCIVILIAN"; //$NON-NLS-1$
    public static final String COMMAND_CONVERT_TO_SOLDIER = "CONVERTTOSOLDIER"; //$NON-NLS-1$
    public static final String COMMAND_SOLDIER_SET_STATE = "SOLDIERSETSTATE"; //$NON-NLS-1$
    public static final String COMMAND_ADD_PATROL_POINT = "ADDPATROLPOINT"; //$NON-NLS-1$
    public static final String COMMAND_REMOVE_PATROL_POINT = "REMOVEPATROLPOINT"; //$NON-NLS-1$

    // Groups
    public static final String COMMAND_ADD_PATROL_POINT_GROUP = "ADDPATROLPOINTGROUP"; //$NON-NLS-1$
    public static final String COMMAND_REMOVE_PATROL_POINT_GROUP = "REMOVEPATROLPOINTGROUP"; //$NON-NLS-1$

    // Job groups
    public static final String COMMAND_CITIZEN_SET_JOB_GROUP = "CITSETJOBGROUP"; //$NON-NLS-1$
    public static final String COMMAND_JOB_GROUP_ENABLE_ALL = "JGENABLEALL"; //$NON-NLS-1$
    public static final String COMMAND_JOB_GROUP_DISABLE_ALL = "JGDISABLEALL"; //$NON-NLS-1$
    public static final String COMMAND_JOB_GROUP_ENABLE_ITEM = "JGFENABLEITEM"; //$NON-NLS-1$
    public static final String COMMAND_JOB_GROUP_DISABLE_ITEM = "JGFDISABLEITEM"; //$NON-NLS-1$

    // Caravans
    public static final String COMMAND_TRADE = "TRADE"; //$NON-NLS-1$

    // Custom actions
    public static final String COMMAND_CUSTOM_ACTION = "CUSTOMACTION"; //$NON-NLS-1$
    public static final String COMMAND_CUSTOM_ACTION_DIRECT_LIVING = "CADIRECT_L"; //$NON-NLS-1$
    public static final String COMMAND_CUSTOM_ACTION_DIRECT_ITEM = "CADIRECT_I"; //$NON-NLS-1$
    public static final String COMMAND_QUEUE = "QUEUE"; //$NON-NLS-1$
    public static final String COMMAND_QUEUE_AND_PLACE = "QUEUEANDPLACE"; //$NON-NLS-1$
    public static final String COMMAND_QUEUE_AND_PLACE_ROW = "QUEUEANDPLACEROW"; //$NON-NLS-1$
    public static final String COMMAND_QUEUE_AND_PLACE_AREA = "QUEUEANDPLACEAREA"; //$NON-NLS-1$

    // View commands
    public static final String COMMAND_LEVEL_DOWN = "LEVEL_DOWN"; //$NON-NLS-1$
    public static final String COMMAND_LEVEL_UP = "LEVEL_UP"; //$NON-NLS-1$
    public static final String COMMAND_NEXT_CITIZEN = "NEXT_CITIZEN"; //$NON-NLS-1$
    public static final String COMMAND_PREVIOUS_CITIZEN = "PREVIOUS_CITIZEN"; //$NON-NLS-1$
    public static final String COMMAND_NEXT_SOLDIER = "NEXT_SOLDIER"; //$NON-NLS-1$
    public static final String COMMAND_PREVIOUS_SOLDIER = "PREVIOUS_SOLDIER"; //$NON-NLS-1$
    public static final String COMMAND_NEXT_HERO = "NEXT_HERO"; //$NON-NLS-1$
    public static final String COMMAND_PREVIOUS_HERO = "PREVIOUS_HERO"; //$NON-NLS-1$
    public static final String COMMAND_MINIBLOCKS = "MINIBLOCKS"; //$NON-NLS-1$

    // System commands
    public static final String COMMAND_EXIT_GAME = "EXITGAME"; //$NON-NLS-1$
    public static final String COMMAND_EXIT_TO_MAIN_MENU = "EXITTOMM"; //$NON-NLS-1$
    public static final String COMMAND_EXIT_TO_MAIN_MENU_SAVE = "EXITTOMMSAVE"; //$NON-NLS-1$
    public static final String COMMAND_EXIT_TO_MAIN_MENU_NOSAVE = "EXITTOMMNOSAVE"; //$NON-NLS-1$
    public static final String COMMAND_BURY = "BURY"; //$NON-NLS-1$
    public static final String COMMAND_CLOSE_CONTEXT = "CLOSECONTEXT"; //$NON-NLS-1$
    public static final String COMMAND_SAVE = "SAVE"; //$NON-NLS-1$
    public static final String COMMAND_SAVE_NO_MISSIONDATA = "SAVENOMD"; //$NON-NLS-1$
//	public static final String COMMAND_SAVE_OPTIONS = "SAVE_OPTIONS"; //$NON-NLS-1$
    public static final String COMMAND_PAUSE = "PAUSE"; //$NON-NLS-1$
    public static final String COMMAND_INCREASE_SPEED = "INC_SPEED"; //$NON-NLS-1$
    public static final String COMMAND_LOWER_SPEED = "LOW_SPEED"; //$NON-NLS-1$
    public static final String COMMAND_BACK = "BACK"; //$NON-NLS-1$
    public static final String COMMAND_CHANGE_LANGUAGE = "CHANGELANGUAGE"; //$NON-NLS-1$

    // Test commands
    public static final String COMMAND_TEST = "TEST"; //$NON-NLS-1$
    public static final String COMMAND_TEST2 = "TEST2"; //$NON-NLS-1$
    public static final String COMMAND_TEST3 = "TEST3"; //$NON-NLS-1$
    public static final String COMMAND_TEST4 = "TEST4"; //$NON-NLS-1$
    public static final String COMMAND_TEST5 = "TEST5"; //$NON-NLS-1$
    public static final String COMMAND_TEST6 = "TEST6"; //$NON-NLS-1$
    public static final String COMMAND_TEST7 = "TEST7"; //$NON-NLS-1$

    // Admin commands
    public static final String COMMAND_ADD_ITEM = "ADD_ITEM"; //$NON-NLS-1$
    public static final String COMMAND_ADD_LIVING = "ADD_LIVING"; //$NON-NLS-1$
    public static final String COMMAND_ADD_EVENT = "ADD_EVENT"; //$NON-NLS-1$
    public static final String COMMAND_GOD_STATUS_LOWER_5 = "GSLOW5"; //$NON-NLS-1$
    public static final String COMMAND_GOD_STATUS_RAISE_5 = "GSRAI5"; //$NON-NLS-1$

    private static SmartMenu currentMenu;

    public int renderX;
    public int renderY;
    public int renderWidth;
    public int renderHeight;

    public CommandPanel(int renderX, int renderY, int renderWidth, int renderHeight, String sCampaignID, String sMissionID) {
        resize(renderX, renderY, renderWidth, renderHeight);
        initialize(sCampaignID, sMissionID);
    }

    public static void initialize(String sCampaignID, String sMissionID) {
        loadMenu(sCampaignID, sMissionID);
    }

    private static void loadMenu(String sCampaignID, String sMissionID) {
        currentMenu = new SmartMenu();
        SmartMenu.readXMLMenu(currentMenu, "menu.xml", sCampaignID, sMissionID); //$NON-NLS-1$
    }

    private static SmartMenu createOptionsMenu(SmartMenu mainMenu) {
        // Options
        SmartMenu menuOptions = new SmartMenu(SmartMenu.TYPE_MENU, Messages.getString("MainMenuPanel.4"), mainMenu, null, null, null, null); //$NON-NLS-1$

        // Options - Graphics
        SmartMenu menuOptionsGraphics = new SmartMenu(SmartMenu.TYPE_MENU, Messages.getString("MainMenuPanel.16"), menuOptions, null, null, null, null); //$NON-NLS-1$
        SmartMenu menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.17"), null, CommandPanel.COMMAND_MM_TOGGLE_FULL_SCREEN, null, null, null); //$NON-NLS-1$
        menuAux.setDynamic(true);
        menuOptionsGraphics.addItem(menuAux);
        menuOptionsGraphics.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.7"), null, CommandPanel.COMMAND_BACK, null, null, null); //$NON-NLS-1$
        menuOptionsGraphics.addItem(menuAux);

        // Options - Audio
        SmartMenu menuOptionsAudio = new SmartMenu(SmartMenu.TYPE_MENU, Messages.getString("MainMenuPanel.13"), menuOptions, null, null, null, null); //$NON-NLS-1$
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.5"), null, CommandPanel.COMMAND_MM_SWITCH_MUSIC, null, null, null); //$NON-NLS-1$
        menuAux.setDynamic(true);
        menuAux.setMaintainOpen(true);
        menuOptionsAudio.addItem(menuAux);
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.56"), null, CommandPanel.COMMAND_MM_ADD_MUSIC_VOLUME, null, null, null); //$NON-NLS-1$
        menuAux.setDynamic(true);
        menuAux.setMaintainOpen(true);
        menuOptionsAudio.addItem(menuAux);
        menuOptionsAudio.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.6"), null, CommandPanel.COMMAND_MM_SWITCH_FX, null, null, null); //$NON-NLS-1$
        menuAux.setDynamic(true);
        menuAux.setMaintainOpen(true);
        menuOptionsAudio.addItem(menuAux);
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.57"), null, CommandPanel.COMMAND_MM_ADD_FX_VOLUME, null, null, null); //$NON-NLS-1$
        menuAux.setDynamic(true);
        menuAux.setMaintainOpen(true);
        menuOptionsAudio.addItem(menuAux);
        menuOptionsAudio.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.7"), null, CommandPanel.COMMAND_BACK, null, null, null); //$NON-NLS-1$
        menuOptionsAudio.addItem(menuAux);

        // Options - Game
        SmartMenu menuOptionsGame = new SmartMenu(SmartMenu.TYPE_MENU, Messages.getString("MainMenuPanel.19"), menuOptions, null, null, null, null); //$NON-NLS-1$
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.20"), null, CommandPanel.COMMAND_MM_SWITCH_MOUSE_SCROLL, null, null, null); //$NON-NLS-1$
        menuAux.setDynamic(true);
        menuAux.setMaintainOpen(true);
        menuOptionsGame.addItem(menuAux);
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.33"), null, CommandPanel.COMMAND_MM_SWITCH_MOUSE_SCROLL_EARS, null, null, null); //$NON-NLS-1$
        menuAux.setDynamic(true);
        menuAux.setMaintainOpen(true);
        menuOptionsGame.addItem(menuAux);
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.72"), null, CommandPanel.COMMAND_MM_SWITCH_MOUSE_2D_CUBES, null, null, null); //$NON-NLS-1$
        menuAux.setDynamic(true);
        menuAux.setMaintainOpen(true);
        menuOptionsGame.addItem(menuAux);
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.27"), null, CommandPanel.COMMAND_MM_SWITCH_DISABLE_ITEMS, null, null, null); //$NON-NLS-1$
        menuAux.setDynamic(true);
        menuAux.setMaintainOpen(true);
        menuOptionsGame.addItem(menuAux);
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.77"), null, CommandPanel.COMMAND_MM_SWITCH_DISABLE_GODS, null, null, null); //$NON-NLS-1$
        menuAux.setDynamic(true);
        menuAux.setMaintainOpen(true);
        menuOptionsGame.addItem(menuAux);
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.28"), null, CommandPanel.COMMAND_MM_SWITCH_PAUSE, null, null, null); //$NON-NLS-1$
        menuAux.setDynamic(true);
        menuAux.setMaintainOpen(true);
        menuOptionsGame.addItem(menuAux);
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.31"), null, CommandPanel.COMMAND_MM_SWITCH_AUTOSAVE_DAYS, null, null, null); //$NON-NLS-1$
        menuAux.setDynamic(true);
        menuAux.setMaintainOpen(true);
        menuOptionsGame.addItem(menuAux);
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.32"), null, CommandPanel.COMMAND_MM_SWITCH_SIEGES, null, null, null); //$NON-NLS-1$
        menuAux.setDynamic(true);
        menuAux.setMaintainOpen(true);
        menuOptionsGame.addItem(menuAux);
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.36"), null, CommandPanel.COMMAND_MM_SWITCH_SIEGE_PAUSE, null, null, null); //$NON-NLS-1$
        menuAux.setDynamic(true);
        menuAux.setMaintainOpen(true);
        menuOptionsGame.addItem(menuAux);
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.66"), null, CommandPanel.COMMAND_MM_SWITCH_CARAVAN_PAUSE, null, null, null); //$NON-NLS-1$
        menuAux.setDynamic(true);
        menuAux.setMaintainOpen(true);
        menuOptionsGame.addItem(menuAux);
        menuOptionsGame.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.7"), null, CommandPanel.COMMAND_BACK, null, null, null); //$NON-NLS-1$
        menuOptionsGame.addItem(menuAux);

        // Options - Performance
        SmartMenu menuOptionsPerformance = new SmartMenu(SmartMenu.TYPE_MENU, Messages.getString("MainMenuPanel.8"), menuOptions, null, null, null, null); //$NON-NLS-1$
        menuOptionsPerformance.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("MainMenuPanel.59"), null, null, null, null, null, Color.LIGHT_GRAY)); //$NON-NLS-1$
        menuOptionsPerformance.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("MainMenuPanel.64"), null, null, null, null, null, Color.LIGHT_GRAY)); //$NON-NLS-1$
        menuOptionsPerformance.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("MainMenuPanel.65"), null, null, null, null, null, Color.LIGHT_GRAY)); //$NON-NLS-1$
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.63"), null, CommandPanel.COMMAND_MM_SWITCH_PATHFINDING_LEVEL, null, null, null); //$NON-NLS-1$
        menuAux.setDynamic(true);
        menuAux.setMaintainOpen(true);
        menuOptionsPerformance.addItem(menuAux);
        menuOptionsPerformance.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.7"), null, CommandPanel.COMMAND_BACK, null, null, null); //$NON-NLS-1$
        menuOptionsPerformance.addItem(menuAux);

        menuOptions.addItem(menuOptionsGraphics);
        menuOptions.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
        menuOptions.addItem(menuOptionsAudio);
        menuOptions.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
        menuOptions.addItem(menuOptionsGame);
        menuOptions.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
        menuOptions.addItem(menuOptionsPerformance);
        menuOptions.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.7"), null, CommandPanel.COMMAND_BACK, null, null, null); //$NON-NLS-1$
        menuOptions.addItem(menuAux);

        return menuOptions;
    }

    /**
     * Executes a command
     *
     * @param sCommand command code
     */
    public static void executeCommand(String sCommand, String sParameter, String sParameter2, Point3D p3dDirect, Tile tile, int iconType) {
        if (sCommand != null) {
            if (sCommand.equals(COMMAND_BACK)) {
                if (currentMenu.getParent() != null) {
                    currentMenu = currentMenu.getParent();
                } else {
                    executeCommand(COMMAND_EXIT_TO_MAIN_MENU, null, null, null, null, 0);
                }
            } else if (sCommand.equals(COMMAND_MINE)) {
                Game.createTask(Task.TASK_MINE);
                Game.getCurrentTask().setTile(tile, iconType);
                if (p3dDirect != null) {
                    Game.getCurrentTask().setPoint(p3dDirect);
                    Game.getCurrentTask().setPoint(p3dDirect);
                }
            } else if (sCommand.equals(COMMAND_MINE_LADDER)) {
                Game.createTask(Task.TASK_MINE_LADDER);
                Game.getCurrentTask().setTile(tile, iconType);
                if (p3dDirect != null) {
                    Game.getCurrentTask().setPoint(p3dDirect);
                    Game.getCurrentTask().setPoint(p3dDirect);
                }
            } else if (sCommand.equals(COMMAND_DIG)) {
                Game.createTask(Task.TASK_DIG);
                Game.getCurrentTask().setTile(tile, iconType);
                if (p3dDirect != null) {
                    Game.getCurrentTask().setPoint(p3dDirect);
                    Game.getCurrentTask().setPoint(p3dDirect);
                }
            } else if (sCommand.equals(COMMAND_CANCEL_ORDER)) {
                Game.createTask(Task.TASK_CANCEL_ORDER);
                Game.getCurrentTask().setTile(tile, iconType);
                if (p3dDirect != null) {
                    Game.getCurrentTask().setPoint(p3dDirect);
                    Game.getCurrentTask().setPoint(p3dDirect);
                }
            } else if (sCommand.equals(COMMAND_WEAR)) {
                Task task = new Task(Task.TASK_WEAR);
                task.setTile(tile, iconType);
                task.setPointIni(p3dDirect);
                task.setParameter(sParameter);
                task.setParameter2(sParameter2);
                Game.getWorld().getTaskManager().addTask(task);
            } else if (sCommand.equals(COMMAND_AUTOEQUIP)) {
                Task task = new Task(Task.TASK_AUTOEQUIP);
                task.setTile(tile, iconType);
                task.setParameter(sParameter);
                Game.getWorld().getTaskManager().addTask(task);
            } else if (sCommand.equals(COMMAND_WEAR_OFF)) {
                Task task = new Task(Task.TASK_WEAR_OFF);
                task.setTile(tile, iconType);
                task.setPointIni(p3dDirect);
                Game.getWorld().getTaskManager().addTask(task);
            } else if (sCommand.equals(COMMAND_BUILD)) {
                Game.createTask(Task.TASK_BUILD, sParameter);
                Game.getCurrentTask().setTile(tile, iconType);
            } else if (sCommand.equals(COMMAND_TURN_OFF_NONSTOP)) {
                Task task = new Task(Task.TASK_TURN_OFF_NON_STOP);
                task.setTile(tile, iconType);
                task.setPointIni(p3dDirect);
                Game.getWorld().getTaskManager().addTask(task);
            } else if (sCommand.equals(COMMAND_CUSTOM_ACTION)) {
                Game.createTask(Task.TASK_CUSTOM_ACTION);
                Game.getCurrentTask().setTile(tile, iconType);
                Game.getCurrentTask().setParameter(sParameter);
                Game.getCurrentTask().setParameter2(sParameter2);
                if (p3dDirect != null) {
                    Game.getCurrentTask().setPoint(p3dDirect);
                    Game.getCurrentTask().setPoint(p3dDirect);
                }
            } else if (sCommand.equals(COMMAND_TURN_ON_NONSTOP)) {
                Task task = new Task(Task.TASK_TURN_ON_NON_STOP);
                task.setTile(tile, iconType);
                task.setPointIni(p3dDirect);
                Game.getWorld().getTaskManager().addTask(task);
            } else if (sCommand.equals(COMMAND_REMOVE_BUILDING_TASK)) {
                Task task = new Task(Task.TASK_REMOVE_BUILDING_TASK);
                task.setTile(tile, iconType);
                task.setPointIni(p3dDirect);
                task.setParameter(sParameter);
                task.setParameter2(sParameter2);
                Game.getWorld().getTaskManager().addTask(task);
            } else if (sCommand.equals(COMMAND_DESTROY_BUILDING)) {
                Task task = new Task(Task.TASK_DESTROY_BUILDING);
                task.setTile(tile, iconType);
                task.setPointIni(p3dDirect);
                Game.getWorld().getTaskManager().addTask(task);
            } else if (sCommand.equals(COMMAND_QUEUE)) {
                Task task = new Task(Task.TASK_QUEUE);
                task.setTile(tile, iconType);
                task.setParameter(sParameter);
                task.setParameter2(sParameter2);
                Game.getWorld().getTaskManager().addTask(task);
            } else if (sCommand.equals(COMMAND_QUEUE_AND_PLACE)) {
                Game.createTask(Task.TASK_QUEUE_AND_PLACE, sParameter);
                if (tile != null) {
                    Game.getCurrentTask().setTile(tile, iconType);
                } else {
                    ActionManagerItem ami = ActionManager.getItem(sParameter);
                    if (ami != null && ami.getGeneratedItem() != null) {
                        Game.getCurrentTask().setTile(new Tile(ami.getGeneratedItem()), SmartMenu.ICON_TYPE_ITEM);
                    }
                }
            } else if (sCommand.equals(COMMAND_QUEUE_AND_PLACE_ROW)) {
                Game.createTask(Task.TASK_QUEUE_AND_PLACE_ROW, sParameter);
                Game.getCurrentTask().setTile(tile, iconType);
            } else if (sCommand.equals(COMMAND_QUEUE_AND_PLACE_AREA)) {
                Game.createTask(Task.TASK_QUEUE_AND_PLACE_AREA, sParameter);
                Game.getCurrentTask().setTile(tile, iconType);
            } else if (sCommand.equals(COMMAND_CREATE)) {
                Task task = new Task(Task.TASK_CREATE);
                task.setTile(tile, iconType);
                task.setParameter(sParameter);
                Game.getWorld().getTaskManager().addTask(task);
            } else if (sCommand.equals(COMMAND_CREATE_IN_A_BUILDING)) {
                Task task = new Task(Task.TASK_CREATE_IN_A_BUILDING);
                task.setTile(tile, iconType);
                task.setParameter(sParameter);
                task.setPointIni(p3dDirect);
                Game.getWorld().getTaskManager().addTask(task);
            } else if (sCommand.equals(COMMAND_CREATE_AND_PLACE)) {
                Game.createTask(Task.TASK_CREATE_AND_PLACE, sParameter);
                Game.getCurrentTask().setTile(tile, iconType);
            } else if (sCommand.equals(COMMAND_CREATE_AND_PLACE_ROW)) {
                Game.createTask(Task.TASK_CREATE_AND_PLACE_ROW, sParameter);
                Game.getCurrentTask().setTile(tile, iconType);
            } else if (sCommand.equals(COMMAND_DESTROY_ENTITY)) {
                Task task = new Task(Task.TASK_DESTROY_ENTITY);
                task.setTile(tile, iconType);
                task.setParameter(sParameter);
                task.setPointIni(p3dDirect);
                Game.getWorld().getTaskManager().addTask(task);
            } else if (sCommand.equals(COMMAND_LOCK)) {
                Task task = new Task(Task.TASK_LOCK);
                task.setTile(tile, iconType);
                task.setParameter(sParameter);
                Game.getWorld().getTaskManager().addTask(task);
            } else if (sCommand.equals(COMMAND_UNLOCK_OPEN)) {
                Task task = new Task(Task.TASK_UNLOCK_OPEN);
                task.setTile(tile, iconType);
                task.setParameter(sParameter);
                Game.getWorld().getTaskManager().addTask(task);
            } else if (sCommand.equals(COMMAND_UNLOCK_CLOSE)) {
                Task task = new Task(Task.TASK_UNLOCK_CLOSE);
                task.setTile(tile, iconType);
                task.setParameter(sParameter);
                Game.getWorld().getTaskManager().addTask(task);
            } else if (sCommand.equals(COMMAND_STOCKPILE)) {
                Game.createTask(Task.TASK_STOCKPILE, sParameter);
                Game.getCurrentTask().setTile(tile, iconType);
            } else if (sCommand.equals(COMMAND_DELETE_STOCKPILE)) {
                Task task = new Task(Task.TASK_DELETE_STOCKPILE);
                task.setTile(tile, iconType);
                task.setParameter(sParameter);
                Game.getWorld().getTaskManager().addTask(task);
            } else if (sCommand.equals(COMMAND_STOCKPILE_ENABLE_ALL)) {
                Stockpile.enableAll(Integer.parseInt(sParameter), sParameter2);
            } else if (sCommand.equals(COMMAND_STOCKPILE_DISABLE_ALL)) {
                Stockpile.disableAll(Integer.parseInt(sParameter), sParameter2);
            } else if (sCommand.equals(COMMAND_STOCKPILE_MANAGE)) {
                int iPileID = Integer.parseInt(sParameter);
                UIPanelState.setPilePanelActive(iPileID, false);
            } else if (sCommand.equals(COMMAND_CONTAINER_MANAGE)) {
                int iContainerID = Integer.parseInt(sParameter);
                UIPanelState.setPilePanelActive(iContainerID, true);
            } else if (sCommand.equals(COMMAND_STOCKPILE_ENABLE_ITEM)) {
                Stockpile stockpile = Stockpile.getStockpile(p3dDirect.toPoint3DShort());
                if (stockpile != null && !stockpile.getType().contains(sParameter)) {
                    Type type = Types.getType(stockpile.getType().getID());
                    String sName = type.getElementName(sParameter);
                    if (sName != null) {
                        stockpile.getType().addElement(sParameter, sName);
                    }
                }

            } else if (sCommand.equals(COMMAND_STOCKPILE_DISABLE_ITEM)) {
                Stockpile stockpile = Stockpile.getStockpile(p3dDirect.toPoint3DShort());
                if (stockpile != null) {
                    stockpile.getType().removeElement(sParameter);
                    // Marcamos items para hauling
                    for (int h = 0; h < stockpile.getPoints().size(); h++) {
                        Game.getWorld().addItemToBeHauled(World.getCell(stockpile.getPoints().get(h)).getItem());
                    }
                }
            } else if (sCommand.equals(COMMAND_STOCKPILE_COPY_TO_ALL)) {
                Stockpile pileSource = Stockpile.getStockpile(sParameter);
                if (pileSource != null) {
                	Type typeSource = pileSource.getType ();

                	// Check all piles until we find the ones with the same type
                    ArrayList<Stockpile> alStockpiles = Game.getWorld().getStockpiles();
                    Stockpile pileDest;
                    boolean bSomethingRemoved = false;
                    for (int i = 0; i < alStockpiles.size (); i++) {
                    	pileDest = alStockpiles.get (i);
                    	if (pileDest.getID () != pileSource.getID ()) {
                    		if (pileDest.getType ().getID ().equals (typeSource.getID ()) && !pileDest.isLockedToCopy ()) {
                    			// Bingo!
                    			// Let's copy the type

                    			// First lets remove the elements FROM the destination
                    			Type typeDestination = pileDest.getType ();
                    			for (int j = (typeDestination.getElements ().size () - 1); j >= 0 ; j--) {
                    				if (!typeSource.contains (typeDestination.getElements ().get (j))) {
                    					typeDestination.removeElement (typeDestination.getElements ().get (j));
                    					bSomethingRemoved = true;
                    				}
                    			}

                    			// Now lets add the elements TO the destination
                    			for (int j = (typeSource.getElements ().size () - 1); j >= 0 ; j--) {
                    				if (!typeDestination.contains (typeSource.getElements ().get (j))) {
                    					typeDestination.addElement (typeSource.getElements ().get (j), typeSource.getElementNames ().get (j));
                    				}
                    			}
                    		}
                    	}

    					// Set items to be hauled (if applies)
                    	if (bSomethingRemoved) {
                            for (int h = 0; h < pileDest.getPoints().size(); h++) {
                                Game.getWorld().addItemToBeHauled(World.getCell(pileDest.getPoints().get(h)).getItem());
                            }
                    	}
                    }
                }
            } else if (sCommand.equals(COMMAND_CONTAINER_ENABLE_ALL)) {
                Container container = Game.getWorld().getContainer(Integer.parseInt(sParameter));
                if (container != null) {
                    container.enableAll(sParameter2);
                }
            } else if (sCommand.equals(COMMAND_CONTAINER_DISABLE_ALL)) {
                Container container = Game.getWorld().getContainer(Integer.parseInt(sParameter));
                if (container != null) {
                    container.disableAll(sParameter2);
                }
            } else if (sCommand.equals(COMMAND_CONTAINER_ENABLE_ITEM)) {
                Container container = Game.getWorld().getContainer(Integer.parseInt(sParameter));
                if (container != null) {
                    container.enableItem(sParameter2);
                }
            } else if (sCommand.equals(COMMAND_CONTAINER_DISABLE_ITEM)) {
                Container container = Game.getWorld().getContainer(Integer.parseInt(sParameter));
                if (container != null) {
                    container.disableItem(sParameter2);
                }
            } else if (sCommand.equals(COMMAND_CONTAINER_COPY_TO_ALL)) {
                Container containerSource = Game.getWorld().getContainer (Integer.parseInt(sParameter));
                if (containerSource != null) {
                	Type typeSource = containerSource.getType ();

                	// Check all piles until we find the ones with the same type
                    ArrayList<Container> alContainers = Game.getWorld().getContainers ();
                    Container containerDest;
                    
                    boolean bSomethingRemoved = false;
                    for (int i = 0; i < alContainers.size (); i++) {
                    	containerDest = alContainers.get (i);
                    	if (containerDest.getItemID () != containerSource.getItemID ()) {
                    		if (containerDest.getType ().getID ().equals (typeSource.getID ()) && !containerDest.isLockedToCopy ()) {
                    			// Bingo!
                    			// Let's copy the type

                    			// First lets remove the elements FROM the destination
                    			Type typeDestination = containerDest.getType ();
                    			for (int j = (typeDestination.getElements ().size () - 1); j >= 0 ; j--) {
                    				if (!typeSource.contains (typeDestination.getElements ().get (j))) {
                    					typeDestination.removeElement (typeDestination.getElements ().get (j));
                    					bSomethingRemoved = true;
                    				}
                    			}

                    			// Now lets add the elements TO the destination
                    			for (int j = (typeSource.getElements ().size () - 1); j >= 0 ; j--) {
                    				if (!typeDestination.contains (typeSource.getElements ().get (j))) {
                    					typeDestination.addElement (typeSource.getElements ().get (j), typeSource.getElementNames ().get (j));
                    				}
                    			}
                    		}
                    	}

    					// Set items to be removed from the container (if applies)
                    	if (bSomethingRemoved) {
                    		containerDest.setWrongItemsInside (true);
                    	}
                    }
                }
            } else if (sCommand.equals(COMMAND_PROFESSIONS_ENABLE_ALL)) {
                LivingEntity le = World.getLivingEntityByID(Integer.parseInt(sParameter));
                if (le != null) {
                    LivingEntityManagerItem lemi = LivingEntityManager.getItem(le.getIniHeader());
                    if (lemi != null && lemi.getType() == LivingEntity.TYPE_CITIZEN) {
                        Citizen cit = (Citizen) le;
                        cit.getCitizenData().removeAllDeniedJobs();
                    }
                }
            } else if (sCommand.equals(COMMAND_PROFESSIONS_DISABLE_ALL)) {
                LivingEntity le = World.getLivingEntityByID(Integer.parseInt(sParameter));
                if (le != null) {
                    LivingEntityManagerItem lemi = LivingEntityManager.getItem(le.getIniHeader());
                    if (lemi != null && lemi.getType() == LivingEntity.TYPE_CITIZEN) {
                        Citizen cit = (Citizen) le;
                        cit.getCitizenData().addAllDeniedJobs();
                    }
                }
            } else if (sCommand.equals(COMMAND_PROFESSIONS_ENABLE_ITEM)) {
                LivingEntity le = World.getLivingEntityByID(Integer.parseInt(sParameter));
                if (le != null) {
                    LivingEntityManagerItem lemi = LivingEntityManager.getItem(le.getIniHeader());
                    if (lemi != null && lemi.getType() == LivingEntity.TYPE_CITIZEN) {
                        Citizen cit = (Citizen) le;
                        cit.getCitizenData().removeDeniedJob(sParameter2);
                    }
                }
            } else if (sCommand.equals(COMMAND_PROFESSIONS_DISABLE_ITEM)) {
                LivingEntity le = World.getLivingEntityByID(Integer.parseInt(sParameter));
                if (le != null) {
                    LivingEntityManagerItem lemi = LivingEntityManager.getItem(le.getIniHeader());
                    if (lemi != null && lemi.getType() == LivingEntity.TYPE_CITIZEN) {
                        Citizen cit = (Citizen) le;
                        cit.getCitizenData().addDeniedJob(sParameter2);
                    }
                }
            } else if (sCommand.equals(COMMAND_JOB_GROUP_ENABLE_ALL)) {
                int iGroupID = Integer.parseInt(sParameter);
                if (iGroupID >= 0 && iGroupID < CitizenGroups.MAX_GROUPS) {
                    CitizenGroupData cgd = Game.getWorld().getCitizenGroups().getGroup(iGroupID);
                    if (cgd != null) {
                        cgd.removeAllDeniedJobs();
                        cgd.setJobsToCitizens();
                    }
                }
            } else if (sCommand.equals(COMMAND_JOB_GROUP_DISABLE_ALL)) {
                int iGroupID = Integer.parseInt(sParameter);
                if (iGroupID >= 0 && iGroupID < CitizenGroups.MAX_GROUPS) {
                    CitizenGroupData cgd = Game.getWorld().getCitizenGroups().getGroup(iGroupID);
                    if (cgd != null) {
                        cgd.addAllDeniedJobs();
                        cgd.setJobsToCitizens();
                    }
                }
            } else if (sCommand.equals(COMMAND_JOB_GROUP_ENABLE_ITEM)) {
                int iGroupID = Integer.parseInt(sParameter);
                if (iGroupID >= 0 && iGroupID < CitizenGroups.MAX_GROUPS) {
                    CitizenGroupData cgd = Game.getWorld().getCitizenGroups().getGroup(iGroupID);
                    if (cgd != null) {
                        cgd.removeDeniedJob(sParameter2);
                        cgd.setJobsToCitizens();
                    }
                }
            } else if (sCommand.equals(COMMAND_JOB_GROUP_DISABLE_ITEM)) {
                int iGroupID = Integer.parseInt(sParameter);
                if (iGroupID >= 0 && iGroupID < CitizenGroups.MAX_GROUPS) {
                    CitizenGroupData cgd = Game.getWorld().getCitizenGroups().getGroup(iGroupID);
                    if (cgd != null) {
                        cgd.addDeniedJob(sParameter2);
                        cgd.setJobsToCitizens();
                    }
                }
            } else if (sCommand.equals(COMMAND_CONVERT_TO_CIVILIAN)) {
                Task task = new Task(Task.TASK_CONVERT_TO_CIVILIAN);
                task.setTile(tile, iconType);
                task.setParameter(sParameter);
                Game.getWorld().getTaskManager().addTask(task);
            } else if (sCommand.equals(COMMAND_CONVERT_TO_SOLDIER)) {
                Task task = new Task(Task.TASK_CONVERT_TO_SOLDIER);
                task.setTile(tile, iconType);
                task.setParameter(sParameter);
                Game.getWorld().getTaskManager().addTask(task);
            } else if (sCommand.equals(COMMAND_SOLDIER_SET_STATE)) {
                Task task = new Task(Task.TASK_SOLDIER_SET_STATE);
                task.setTile(tile, iconType);
                task.setParameter(sParameter);
                task.setParameter2(sParameter2);
                task.setPointIni(p3dDirect);
                Game.getWorld().getTaskManager().addTask(task);
            } else if (sCommand.equals(COMMAND_ADD_PATROL_POINT)) {
                Task task = new Task(Task.TASK_SOLDIER_ADD_PATROL_POINT);
                task.setTile(tile, iconType);
                task.setParameter(sParameter);
                task.setParameter2(sParameter2);
                task.setPointIni(p3dDirect);
                Game.getWorld().getTaskManager().addTask(task);
            } else if (sCommand.equals(COMMAND_ADD_PATROL_POINT_GROUP)) {
                Task task = new Task(Task.TASK_SOLDIER_ADD_PATROL_POINT_GROUP);
                task.setTile(tile, iconType);
                task.setParameter(sParameter);
                task.setParameter2(sParameter2);
                task.setPointIni(p3dDirect);
                Game.getWorld().getTaskManager().addTask(task);
            } else if (sCommand.equals(COMMAND_REMOVE_PATROL_POINT)) {
                Task task = new Task(Task.TASK_SOLDIER_REMOVE_PATROL_POINT);
                task.setTile(tile, iconType);
                task.setParameter(sParameter);
                task.setParameter2(sParameter2);
                task.setPointIni(p3dDirect);
                Game.getWorld().getTaskManager().addTask(task);
            } else if (sCommand.equals(COMMAND_REMOVE_PATROL_POINT_GROUP)) {
                Task task = new Task(Task.TASK_SOLDIER_REMOVE_PATROL_POINT_GROUP);
                task.setTile(tile, iconType);
                task.setParameter(sParameter);
                task.setParameter2(sParameter2);
                task.setPointIni(p3dDirect);
                Game.getWorld().getTaskManager().addTask(task);
            } else if (sCommand.equals(COMMAND_CITIZEN_SET_JOB_GROUP)) {
                int iCitID = Integer.parseInt(sParameter);
                LivingEntity le = World.getLivingEntityByID(iCitID);
                if (le != null) {
                    LivingEntityManagerItem lemi = LivingEntityManager.getItem(le.getIniHeader());
                    if (lemi != null && lemi.getType() == LivingEntity.TYPE_CITIZEN) {
                        Citizen cit = (Citizen) le;
                        // Lo borramos del grupo actual
                        int iCurrentJobGroup = cit.getCitizenData().getGroupID();
                        Game.getWorld().getCitizenGroups().removeCitizenFromGroup(iCitID, iCurrentJobGroup);

                        // Lo metemos en el nuevo
                        int iNewGRoupID = Integer.parseInt(sParameter2);
                        Game.getWorld().getCitizenGroups().addCitizenToGroup(iCitID, iNewGRoupID);

                        cit.getCitizenData().setGroupID(Integer.parseInt(sParameter2));

                        cit.getCitizenData().removeAllDeniedJobs();
                        // Le copiamos los jobs denied del nuevo grupo
                        if (iNewGRoupID != -1) {
                            CitizenGroupData cgd = Game.getWorld().getCitizenGroups().getGroup(iNewGRoupID);
                            if (cgd != null) {
                                cgd.setJobsToCitizens();
                            }
                        }

                        // Tutorial flow
                        Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_CIV2GROUP, (iNewGRoupID + 1), null);
                    }
                }
            } else if (sCommand.equals(COMMAND_CREATE_ZONE)) {
                Game.createTask(Task.TASK_CREATE_ZONE, sParameter);
                Game.getCurrentTask().setTile(tile, iconType);
            } else if (sCommand.equals(COMMAND_DELETE_ZONE)) {
                Task task = new Task(Task.TASK_DELETE_ZONE);
                task.setTile(tile, iconType);
                task.setParameter(sParameter);
                Game.getWorld().getTaskManager().addTask(task);
            } else if (sCommand.equals(COMMAND_EXPAND_ZONE)) {
                Game.createTask(Task.TASK_EXPAND_ZONE, sParameter);
                Game.getCurrentTask().setTile(tile, iconType);
            } else if (sCommand.equals(COMMAND_CHANGE_OWNER)) {
                Task task = new Task(Task.TASK_CHANGE_OWNER);
                task.setTile(tile, iconType);
                task.setParameter(sParameter);
                task.setParameter2(sParameter2);
                task.setPointIni(p3dDirect);
                Game.getWorld().getTaskManager().addTask(task);
            } else if (sCommand.equals(COMMAND_CHANGE_OWNER_GROUP)) {
                Task task = new Task(Task.TASK_CHANGE_OWNER_GROUP);
                task.setTile(tile, iconType);
                task.setParameter(sParameter);
                task.setParameter2(sParameter2);
                task.setPointIni(p3dDirect);
                Game.getWorld().getTaskManager().addTask(task);
            } else if (sCommand.equals(COMMAND_CUSTOM_ACTION_DIRECT_LIVING)) {
				// Como esto es en diferido, quizá el usuario hace botón derecho, deja pasar el tiempo y luego clica
                // Así que buscamos las coordenadas actuales de la living
                LivingEntity le = World.getLivingEntityByID(Integer.parseInt(sParameter2));
                if (le != null) {
                    Game.createTask(Task.TASK_CUSTOM_ACTION);
                    Game.getCurrentTask().setTile(tile, iconType);
                    Game.getCurrentTask().setParameter(sParameter);
                    Game.getCurrentTask().setParameter2(sParameter2);
                    Game.getCurrentTask().setPoint(le.getCoordinates().toPoint3D());
                    Game.getCurrentTask().setPoint(le.getCoordinates().toPoint3D());
                }
            } else if (sCommand.equals(COMMAND_CUSTOM_ACTION_DIRECT_ITEM)) {
				// Como esto es en diferido, quizá el usuario hace botón derecho, deja pasar el tiempo y luego clica
                // Así que buscamos las coordenadas actuales de la living
                Item it = Item.getItemByID(Integer.parseInt(sParameter2));
                if (it != null) {
                    Game.createTask(Task.TASK_CUSTOM_ACTION);
                    Game.getCurrentTask().setTile(tile, iconType);
                    Game.getCurrentTask().setParameter(sParameter);
                    Game.getCurrentTask().setParameter2(sParameter2);
                    Game.getCurrentTask().setPoint(it.getCoordinates().toPoint3D());
                    Game.getCurrentTask().setPoint(it.getCoordinates().toPoint3D());
                }
            } else if (sCommand.equals(COMMAND_LEVEL_DOWN)) {
                Point3D view = Game.getWorld().getView();
                if (TownsProperties.DEBUG_MODE) {
                    if (view.z < (World.MAP_DEPTH - 1)) {
                        Game.getWorld().setView(view.x, view.y, view.z + 1);

                        // Tutorial flow
                        Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_LAYERUPDOWN, TutorialTrigger.LAYER_DOWN, null);
            			Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_LEVELDOWN, null);
                    }
                } else {
                    if (view.z < (Game.getWorld().getNumFloorsDiscovered() - 1) && view.z < (World.MAP_DEPTH - 1)) {
                        Game.getWorld().setView(view.x, view.y, view.z + 1);

                        // Tutorial flow
                        Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_LAYERUPDOWN, TutorialTrigger.LAYER_DOWN, null);
            			Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_LEVELDOWN, null);
                    }
                }
            } else if (sCommand.equals(COMMAND_LEVEL_UP)) {
                Point3D view = Game.getWorld().getView();
                if (view.z > 0) {
                    Game.getWorld().setView(view.x, view.y, view.z - 1);

                    // Tutorial flow
                    Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_LAYERUPDOWN, TutorialTrigger.LAYER_UP, null);
        			Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_LEVELUP, null);
                }
            } else if (sCommand.equals(COMMAND_TERRAIN_CHANGE)) {
                Task task = new Task(Task.TASK_TERRAIN_CHANGE);
                task.setTile(tile, iconType);
                task.setParameter(sParameter);
                task.setPointIni(p3dDirect);
                Game.getWorld().getTaskManager().addTask(task);
            } else if (sCommand.equals(COMMAND_TERRAIN_ADD_FLUID)) {
                Task task = new Task(Task.TASK_TERRAIN_ADD_FLUID);
                task.setTile(tile, iconType);
                task.setParameter(sParameter);
                task.setParameter2(sParameter2);
                task.setPointIni(p3dDirect);
                Game.getWorld().getTaskManager().addTask(task);
            } else if (sCommand.equals(COMMAND_TERRAIN_REMOVE_FLUID)) {
                Task task = new Task(Task.TASK_TERRAIN_REMOVE_FLUID);
                task.setTile(tile, iconType);
                task.setPointIni(p3dDirect);
                Game.getWorld().getTaskManager().addTask(task);
            } else if (sCommand.equals(COMMAND_PAUSE)) {
                Game.togglePause(true);
    			Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_PAUSE, null);
            } else if (sCommand.equals(COMMAND_INCREASE_SPEED)) {
                World.addTurnsPerSecond();
                MessagesPanel.addMessage(MessagesPanel.TYPE_SYSTEM, Messages.getString("Game.6") + World.SPEED, ColorGL.YELLOW); //$NON-NLS-1$

                // Tutorial flow
				Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_SPEEDUP, null);
            } else if (sCommand.equals(COMMAND_LOWER_SPEED)) {
                World.removeTurnsPerSecond();
                MessagesPanel.addMessage(MessagesPanel.TYPE_SYSTEM, Messages.getString("Game.6") + World.SPEED, ColorGL.YELLOW); //$NON-NLS-1$

                // Tutorial flow
				Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_SPEEDDOWN, null);
            } else if (sCommand.equals(COMMAND_NEXT_CITIZEN)) {
                if (Game.getWorld().setNextIndexViewCitizen()) {
                    Game.getWorld().setViewOnCitizen();
                }
            } else if (sCommand.equals(COMMAND_PREVIOUS_CITIZEN)) {
                if (Game.getWorld().setPreviousIndexViewCitizen()) {
                    Game.getWorld().setViewOnCitizen();
                }
            } else if (sCommand.equals(COMMAND_NEXT_SOLDIER)) {
                if (Game.getWorld().setNextIndexViewSoldier()) {
                    Game.getWorld().setViewOnSoldier();
                }
            } else if (sCommand.equals(COMMAND_PREVIOUS_SOLDIER)) {
                if (Game.getWorld().setPreviousIndexViewSoldier()) {
                    Game.getWorld().setViewOnSoldier();
                }
            } else if (sCommand.equals(COMMAND_NEXT_HERO)) {
                if (Game.getWorld().setNextIndexViewHero()) {
                    Game.getWorld().setViewOnHero();
                }
            } else if (sCommand.equals(COMMAND_PREVIOUS_HERO)) {
                if (Game.getWorld().setPreviousIndexViewHero()) {
                    Game.getWorld().setViewOnHero();
                }
            } else if (sCommand.equals(COMMAND_MINIBLOCKS)) {
                MainPanel.toggleMiniBlocks();
            } else if (sCommand.equals(COMMAND_TRADE)) {
                UIPanelState.setTradePanelActive(true);
            } else if (sCommand.equals(COMMAND_SAVE)) {
                try {
                    Utils.save (true);
                } catch (Exception ex) {
                    Log.log(Log.LEVEL_ERROR, Messages.getString("CommandPanel.38") + ex.toString() + "]", "CommandPanel"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    MessagesPanel.addMessage(MessagesPanel.TYPE_SYSTEM, Messages.getString("CommandPanel.38") + ex.toString() + "]", ColorGL.RED); //$NON-NLS-1$ //$NON-NLS-2$
                }
            } else if (sCommand.equals(COMMAND_SAVE_NO_MISSIONDATA)) {
                try {
                    Utils.save (false);
                } catch (Exception ex) {
                    Log.log(Log.LEVEL_ERROR, Messages.getString("CommandPanel.38") + ex.toString() + "]", "CommandPanel"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    MessagesPanel.addMessage(MessagesPanel.TYPE_SYSTEM, Messages.getString("CommandPanel.38") + ex.toString() + "]", ColorGL.RED); //$NON-NLS-1$ //$NON-NLS-2$
                }
            } else if (sCommand.equals(COMMAND_ITEM_TEXT_ADD)) {
                if (UIPanelState.typingPanel == null) {
                    UIPanelState.typingPanel = new TypingPanel(UIPanelState.renderWidth, UIPanelState.renderHeight, Messages.getString("CommandPanel.14"), "", TypingPanel.TYPE_ADD_TEXT_TO_ITEM, Integer.valueOf(sParameter)); //$NON-NLS-1$ //$NON-NLS-2$
                }
            } else if (sCommand.equals(COMMAND_ITEM_TEXT_DELETE)) {
                World.getItemsText().remove(Integer.valueOf(sParameter));
            } else if (sCommand.equals(COMMAND_ITEM_ROTATE)) {
                Item item = Item.getItemByID(Integer.parseInt(sParameter));
                if (item != null) {
                    int iFace = Integer.parseInt(sParameter2);
                    item.setFacing(iFace);
                }
            } else if (sCommand.equals(COMMAND_UNLOCK)) {
                Item item = Item.getItemByID(Integer.parseInt(sParameter));
                if (item != null && item.isLocked() && item.getCoordinates() != null && item.getCoordinates().x != -1) {
                    Cell cell = World.getCell(item.getCoordinates());
                    ItemManagerItem imi = ItemManager.getItem(item.getIniHeader());
                    cell.setEntity(null);
                    item.setOperative(imi.isAlwaysOperative());
                    item.setLocked(false);
                    cell.setEntity(item);

                    if (imi.isWall() || imi.isZoneMergerUp()) {
                        Cell.setAllZoneIDs();
                    }
                }
            } else if (sCommand.equals(COMMAND_BURY)) {
                // Enterramos
                try {
                    Utils.saveBury();
                    MessagesPanel.addMessage(MessagesPanel.TYPE_SYSTEM, Messages.getString("CommandPanel.9")); //$NON-NLS-1$
                } catch (Exception ex) {
                    Log.log(Log.LEVEL_ERROR, Messages.getString("CommandPanel.8") + " [" + ex.toString() + "]", "CommandPanel"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    MessagesPanel.addMessage(MessagesPanel.TYPE_SYSTEM, Messages.getString("CommandPanel.8") + " [" + ex.toString() + "]", ColorGL.RED); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
            } else if (sCommand.equals(COMMAND_EXIT_GAME)) {
                Game.exit();
            } else if (sCommand.equals(COMMAND_EXIT_TO_MAIN_MENU)) {
                if (Game.getCurrentState() == Game.STATE_CREATING_TASK) {
                    Game.deleteCurrentTask();
                }
                ContextMenu menuExit = new ContextMenu();
                SmartMenu smExit = new SmartMenu();
                if (TownsProperties.DEBUG_MODE) {
                    smExit.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, "Admin save, no mission", null, COMMAND_SAVE_NO_MISSIONDATA, null)); //$NON-NLS-1$
                    smExit.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
                }
                smExit.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("CommandPanel.4"), null, COMMAND_SAVE, null)); //$NON-NLS-1$
                smExit.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
                smExit.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("CommandPanel.0"), null, COMMAND_EXIT_TO_MAIN_MENU_SAVE, null)); //$NON-NLS-1$
                smExit.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
                SmartMenu smSure = new SmartMenu(SmartMenu.TYPE_MENU, Messages.getString("CommandPanel.1"), smExit, null, null); //$NON-NLS-1$
                smSure.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("CommandPanel.3"), null, COMMAND_EXIT_TO_MAIN_MENU_NOSAVE, null)); //$NON-NLS-1$
                smSure.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
                smExit.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("CommandPanel.7"), null, COMMAND_BURY, null)); //$NON-NLS-1$
                smExit.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
                smSure.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("CommandPanel.5"), null, COMMAND_BACK, null)); //$NON-NLS-1$

                smExit.addItem(smSure);

                smExit.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
                smExit.addItem(createOptionsMenu(smExit));
                smExit.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
                smExit.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("CommandPanel.2"), null, COMMAND_CLOSE_CONTEXT, null)); //$NON-NLS-1$
                menuExit.setSmartMenu(smExit);
                menuExit.setX(UtilsGL.getWidth() / 2 - menuExit.getWidth() / 2);
                menuExit.setY(UtilsGL.getHeight() / 2 - menuExit.getHeight() / 2);
                Game.setContextMenu(menuExit);

                // Tutorial flow
                Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_SETTINGS, null);
            } else if (sCommand.equals(COMMAND_EXIT_TO_MAIN_MENU_SAVE)) {
                executeCommand(COMMAND_SAVE, null, null, null, null, 0);
                executeCommand(COMMAND_EXIT_TO_MAIN_MENU_NOSAVE, null, null, null, null, 0);
            } else if (sCommand.equals(COMMAND_EXIT_TO_MAIN_MENU_NOSAVE)) {
                UtilsAL.stopMusic();
                UtilsAL.stopFX();
                UtilsAL.play(UtilsAL.SOURCE_MUSIC_MAINMENU);
                Game.exitToMainMenu();
//			} else if (sCommand.equals (COMMAND_SAVE_OPTIONS)) {
//				Utils.saveOptions ();
            } else if (sCommand.equals(COMMAND_MM_NEWGAME_SET_SAVE_NAME)) {
                MainMenuPanel.useBuryTemporary = true;
                // Si tiene el parámetro del point, ahí indica el número de servidor a usar
                if (p3dDirect != null) {
                    Game.setServerToUse(p3dDirect.x);
                } else {
                    Game.setServerToUse(-1);
                }
                Game.getPanelMainMenu().setSettingSavegameName(true, sParameter, sParameter2);
            } else if (sCommand.equals(COMMAND_MM_NEWGAME_SET_SAVE_NAME_NO_BURY)) {
                executeCommand(COMMAND_MM_NEWGAME_SET_SAVE_NAME, sParameter, sParameter2, p3dDirect, tile, iconType);
                MainMenuPanel.useBuryTemporary = false;
            } else if (sCommand.equals(COMMAND_MM_NEWGAME)) {
            	// If the campaign/mission folder contains a "save.zip", then we will just load that one and set the missionData on campaigns.xml
            	ArrayList<String> alPaths = Utils.getPathToFile ("save.zip", sParameter, sParameter2); //$NON-NLS-1$
            	if (alPaths.size () > 0) {
            		executeCommand (COMMAND_MM_CONTINUEGAME, "save.zip", sParameter + "," + sParameter2 + "," + alPaths.get (0), null, null, 0); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            	} else {
                    MainMenuPanel.loadingGame = true;
                    Game.getPanelMainMenu().render();
                    Display.update();
                    Display.sync(Game.FPS_MAINMENU); // Para "capear" a 30 fps
                    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_ACCUM_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
                    Game.startGame(sParameter, sParameter2);
            	}
            } else if (sCommand.equals(COMMAND_MM_CONTINUEGAME)) {
                MainMenuPanel.loadingGame = true;
                Game.getPanelMainMenu().render();
                Display.update();
                if (sParameter2 != null) {
                    Game.continueGame(sParameter, sParameter2);
                } else {
                    Game.continueGame(sParameter, null);
                }
            } else if (sCommand.equals(COMMAND_MM_DELETEGAME)) {
                Utils.deleteSavegame(sParameter);
                Game.getPanelMainMenu().createMenu();
            } else if (sCommand.equals(COMMAND_MM_SWITCH_MUSIC)) {
                Game.setMusicON(!Game.isMusicON());
                Utils.saveOptions();
                if (!Game.isMusicON()) {
                    UtilsAL.stopMusic();
                } else {
                    UtilsAL.initAL(Game.getVolumeMusic(), Game.getVolumeFX());
                    if (Game.getPanelMainMenu().isActive()) {
                        UtilsAL.play(UtilsAL.SOURCE_MUSIC_MAINMENU);
                    } else {
                        UtilsAL.play(UtilsAL.SOURCE_MUSIC_INGAME);
                    }
                }
            } else if (sCommand.equals(COMMAND_MM_ADD_MUSIC_VOLUME)) {
                Game.addMusicVolume();
                Utils.saveOptions();
            } else if (sCommand.equals(COMMAND_MM_SWITCH_FX)) {
                Game.setFXON(!Game.isFXON());
                Utils.saveOptions();
                if (!Game.isFXON()) {
                    UtilsAL.stopFX();
                } else {
                    UtilsAL.initAL(Game.getVolumeMusic(), Game.getVolumeFX());
                    UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
                }
            } else if (sCommand.equals(COMMAND_MM_ADD_FX_VOLUME)) {
                Game.addFXVolume();
                Utils.saveOptions();
            } else if (sCommand.equals(COMMAND_CHANGE_LANGUAGE)) {
                if (p3dDirect == null) {
                    Messages.changeLanguage(sParameter, sParameter2, null);
                } else {
                    if (Game.getModsLoaded() != null && Game.getModsLoaded().size() > p3dDirect.x) {
                        Messages.changeLanguage(sParameter, sParameter2, Game.getModsLoaded().get(p3dDirect.x));
                    } else {
                        Messages.changeLanguage(sParameter, sParameter2, null);
                    }
                }
                Utils.saveOptions();
                Game.getPanelMainMenu().createMenu();
            } else if (sCommand.equals(COMMAND_MM_TOGGLE_FULL_SCREEN)) {
                UtilsGL.toggleFullScreen();
                Utils.saveOptions();
            } else if (sCommand.equals(COMMAND_MM_SWITCH_MOUSE_SCROLL)) {
                Game.setMouseScrollON(!Game.isMouseScrollON());
                Utils.saveOptions();
            } else if (sCommand.equals(COMMAND_MM_SWITCH_MOUSE_SCROLL_EARS)) {
                Game.setMouseScrollEarsON(!Game.isMouseScrollEarsON());
                Utils.saveOptions();
            } else if (sCommand.equals(COMMAND_MM_SWITCH_MOUSE_2D_CUBES)) {
                Game.setMouse2DCubesON(!Game.isMouse2DCubesON());
                Utils.saveOptions();
            } else if (sCommand.equals(COMMAND_MM_SWITCH_DISABLE_ITEMS)) {
                Game.setDisabledItemsON(!Game.isDisabledItemsON());
                Utils.saveOptions();
            } else if (sCommand.equals(COMMAND_MM_SWITCH_DISABLE_GODS)) {
                Game.setDisabledGodsON(!Game.isDisabledGodsON());
                Utils.saveOptions();
            } else if (sCommand.equals(COMMAND_MM_SWITCH_PAUSE)) {
                Game.setPauseStartON(!Game.isPauseStartON());
                Utils.saveOptions();
            } else if (sCommand.equals(COMMAND_MM_SWITCH_AUTOSAVE_DAYS)) {
                Game.setAutosaveDays(Game.getAutosaveDays() + 1);
                Utils.saveOptions();
            } else if (sCommand.equals(COMMAND_MM_SWITCH_SIEGES)) {
                Game.setSiegeDifficulty(Game.getSiegeDifficulty() + 1);
                if (Game.getSiegeDifficulty() > Game.SIEGE_DIFFICULTY_INSANE) {
                    Game.setSiegeDifficulty(Game.SIEGE_DIFFICULTY_OFF);
                }
                Utils.saveOptions();
            } else if (sCommand.equals(COMMAND_MM_SWITCH_SIEGE_PAUSE)) {
                Game.setSiegePause(!Game.isSiegePause());
                Utils.saveOptions();
            } else if (sCommand.equals(COMMAND_MM_SWITCH_CARAVAN_PAUSE)) {
                Game.setCaravanPause(!Game.isCaravanPause());
                Utils.saveOptions();
            } else if (sCommand.equals(COMMAND_MM_SWITCH_BURY)) {
                Game.setAllowBury(!Game.isAllowBury());
                Utils.saveOptions();
                Game.exitToMainMenu();
                Game.getPanelMainMenu().createMenu();
            } else if (sCommand.equals(COMMAND_MM_SWITCH_PATHFINDING_LEVEL)) {
                Game.setPathfindingCPULevel(Game.getPathfindingCPULevel() + 1);
                Utils.saveOptions();
            } else if (sCommand.equals(COMMAND_MM_DELETE_ERROR)) {
                Game.getPanelMainMenu().setErrorToShow(null);
                Game.getPanelMainMenu().createMenu();
            } else if (sCommand.equals(COMMAND_CHANGE_HOTKEY)) {
                Game.getPanelMainMenu().setSettingHotkey(true, Integer.parseInt(sParameter));
            } else if (sCommand.equals(COMMAND_TOGGLE_MOD)) {
                Game.toggleMod(sParameter);
                // Graphics
                Towns.clearPropertiesGraphics();
                Game.loadAllIniTextures();
                // Audio
                UtilsAL.clearPropertiesAudio();
                UtilsAL.initAL(Game.getVolumeMusic(), Game.getVolumeFX());
                if (Game.getPanelMainMenu().isActive()) {
                    UtilsAL.play(UtilsAL.SOURCE_MUSIC_MAINMENU);
                } else {
                    UtilsAL.play(UtilsAL.SOURCE_MUSIC_INGAME);
                }

                Utils.saveOptions();
                Game.exitToMainMenu();
                Game.getPanelMainMenu().loadMenuTexture(true);
                Game.getPanelMainMenu().createMenu();
            } else if (sCommand.equals(COMMAND_SERVER_ADD)) {
                Game.getPanelMainMenu().setSettingNewServer(true);
            } else if (sCommand.equals(COMMAND_SERVER_REMOVE)) {
                Game.removeServer(sParameter);
                Utils.saveOptions();
                Game.exitToMainMenu();
                Game.getPanelMainMenu().createMenu();
            } else if (sCommand.equals(COMMAND_OPEN_FOLDER)) {
                try {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(new File(sParameter));
                    }
                } catch (Exception e) {
                }
            } else {
                if (TownsProperties.TEST_COMMANDS) {
                    if (sCommand.equals(COMMAND_TEST)) {
                        // New citizen
                        World.addNewLiving(null, LivingEntity.TYPE_CITIZEN, true, 0, 0, 0, true);
//						World.addNewLiving ("sips", LivingEntity.TYPE_HERO, true, 0, 0, 0, true);
                    } else if (sCommand.equals(COMMAND_TEST2)) {
                        // Fulfill them
                        for (int i = 0; i < World.getCitizenIDs().size(); i++) {
                            ((Citizen) World.getLivingEntityByID(World.getCitizenIDs().get(i))).getCitizenData().setHungry(5000);
                        }
                        for (int i = 0; i < World.getSoldierIDs().size(); i++) {
                            ((Citizen) World.getLivingEntityByID(World.getSoldierIDs().get(i))).getCitizenData().setHungry(5000);
                        }
                        for (int i = 0; i < World.getHeroIDs().size(); i++) {
                            ((Hero) World.getLivingEntityByID(World.getHeroIDs().get(i))).getCitizenData().setHungry(5000);
                        }

                        // Delete messages
                        MessagesPanel.clear ();
                        MessagesPanel.addMessage (MessagesPanel.TYPE_SYSTEM, TownsProperties.GAME_NAME + " " + TownsProperties.GAME_VERSION_FULL); //$NON-NLS-1$
                    } else if (sCommand.equals(COMMAND_TEST3)) {
                        // Siege!!
                        Game.getWorld().spawnSiege();
                    } else if (sCommand.equals(COMMAND_TEST4)) {
                        // Sleep time
                        for (int i = 0; i < World.getCitizenIDs().size(); i++) {
                            ((Citizen) World.getLivingEntityByID(World.getCitizenIDs().get(i))).getCitizenData().setSleep(0);
                        }
                        for (int i = 0; i < World.getSoldierIDs().size(); i++) {
                            ((Citizen) World.getLivingEntityByID(World.getSoldierIDs().get(i))).getCitizenData().setSleep(0);
                        }
                        for (int i = 0; i < World.getHeroIDs().size(); i++) {
                            ((Hero) World.getLivingEntityByID(World.getHeroIDs().get(i))).getCitizenData().setSleep(0);
                        }
                    } else if (sCommand.equals(COMMAND_TEST5)) {
                        // Caravan
                        Game.getWorld().checkCaravansCome();
                    } else if (sCommand.equals(COMMAND_TEST6)) {
                        // Reveal all map
                        for (int x = 0; x < World.MAP_WIDTH; x++) {
                            for (int y = 0; y < World.MAP_HEIGHT; y++) {
                                for (int z = 0; z < World.MAP_DEPTH; z++) {
                                    World.getCell(x, y, z).setDiscovered(true);
                                }
                            }
                        }
                        Game.getWorld().setNumFloorsDiscovered(World.MAP_DEPTH);
                    } else if (sCommand.equals(COMMAND_TEST7)) {
                        Game.getWorld().checkHeroesCome();
                    } else if (sCommand.equals(COMMAND_ADD_ITEM)) {
                        if (!World.getCell(p3dDirect).hasItem()) {
                            ItemManagerItem imi = ItemManager.getItem(sParameter);
                            Item item = Item.createItem(imi);
                            item.init(p3dDirect.x, p3dDirect.y, p3dDirect.z);
                            item.setOperative(true);
                            item.setLocked(imi.isLocked());
                            World.getCell(p3dDirect).setEntity(item);
                        }
                    } else if (sCommand.equals(COMMAND_ADD_LIVING)) {
                        Cell cell = World.getCell(p3dDirect);
                        LivingEntityManagerItem lemi = LivingEntityManager.getItem(sParameter);
                        World.addNewLiving(sParameter, lemi.getType(), cell.isDiscovered(), p3dDirect.x, p3dDirect.y, p3dDirect.z, true);
                    } else if (sCommand.equals(COMMAND_ADD_EVENT)) {
                        EventManagerItem emi = EventManager.getItem(sParameter);
                        Game.getWorld().addEvent(emi);
                    } else if (sCommand.equals(COMMAND_GOD_STATUS_LOWER_5)) {
//						for (int i = 0; i < Game.getWorld ().getGods ().size (); i++) {
//							if (Game.getWorld ().getGods ().get (i).getGodID ().equals (sParameter)) {
//								Game.getWorld ().getGods ().get (i).setStatus (Game.getWorld ().getGods ().get (i).getStatus () - 5);
//								break;
//							}
//						}
                    } else if (sCommand.equals(COMMAND_GOD_STATUS_RAISE_5)) {
//						for (int i = 0; i < Game.getWorld ().getGods ().size (); i++) {
//							if (Game.getWorld ().getGods ().get (i).getGodID ().equals (sParameter)) {
//								Game.getWorld ().getGods ().get (i).setStatus (Game.getWorld ().getGods ().get (i).getStatus () + 5);
//								break;
//							}
//						}
                    } else {
                        Log.log(Log.LEVEL_ERROR, Messages.getString("CommandPanel.6") + sCommand + "] [" + sParameter + "]", "CommandPannel"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    }
                } else {
                    Log.log(Log.LEVEL_ERROR, Messages.getString("CommandPanel.6") + sCommand + "] [" + sParameter + "]", "CommandPannel"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                }
            }
        }
    }

    public void resize(int renderX, int renderY, int renderWidth, int renderHeight) {
        this.renderX = renderX;
        this.renderY = renderY;
        this.renderWidth = renderWidth;
        this.renderHeight = renderHeight;
    }

    /**
     * Limpia todos los datos (se usa cuando se sale de la partida y se va al
     * menú principal)
     */
    public void clear() {
        currentMenu = null;
    }
}
