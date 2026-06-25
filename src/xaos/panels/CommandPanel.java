package xaos.panels;

import xaos.commands.CommandContext;
import xaos.commands.CommandDispatcher;

import xaos.panels.menus.ContextMenu;

import xaos.panels.menus.MenuDefinition;
import xaos.panels.menus.MenuManager;
import xaos.panels.menus.SmartMenu;

import xaos.tiles.Tile;

import xaos.utils.Point3D;

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
    public static String COMMAND_MM_WORLD_ZOOM = "WORLDZOOM"; //$NON-NLS-1$
    public static String COMMAND_MM_TOOLTIP_SCALE = "TOOLTIPSCALE"; //$NON-NLS-1$
    public static String COMMAND_MM_UI_SCALE = "UISCALE"; //$NON-NLS-1$
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
    // public static final String COMMAND_SAVE_OPTIONS = "SAVE_OPTIONS";
    // //$NON-NLS-1$
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
    public static ContextMenu escapeMenu;

    public static SmartMenu getCurrentMenu() {
        return currentMenu;
    }

    public static void setCurrentMenu(SmartMenu menu) {
        currentMenu = menu;
    }

    public CommandPanel(int renderX, int renderY, int renderWidth, int renderHeight, String sCampaignID,
            String sMissionID) {
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

    private static final CommandDispatcher commandDispatcher = new CommandDispatcher();

    public static void executeCommand(
            String sCommand,
            String sParameter,
            String sParameter2,
            Point3D p3dDirect,
            Tile tile,
            int iconType) {
        if (sCommand == null) {
            return;
        }

        CommandContext context = new CommandContext(
                sCommand,
                sParameter,
                sParameter2,
                p3dDirect,
                tile,
                iconType);

        commandDispatcher.execute(context);

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
