package xaos.panels.UI;

import xaos.actions.ActionPriorityManager;
import xaos.campaign.TutorialTrigger;
import xaos.main.Game;
import xaos.main.World;
import xaos.panels.ImagesPanel;
import xaos.panels.TradePanel;
import xaos.panels.TypingPanel;
import xaos.panels.menus.SmartMenu;
import xaos.stockpiles.Stockpile;
import xaos.tiles.Tile;
import xaos.tiles.entities.items.Container;
import xaos.utils.UtilsGL;

import java.awt.Point;
import java.util.ArrayList;

public class UIPanelState {
    private UIPanelState() {}
    public static int PIXELS_TO_BORDER = 16;
    public static final int CLOSE_PIXELS = 20;
    public static final int MAX_BLINK_TURNS = Game.FPS_INGAME;

    public static Tile BACK_TILE = new Tile("ui_back"); //$NON-NLS-1$
    public static Tile BLACK_TILE = new Tile("ui_black"); //$NON-NLS-1$
    public static Tile BIG_RED_CROSS_TILE = new Tile("iconredcross"); //$NON-NLS-1$
    public static Tile ENABLE_ALL_TILE = new Tile("iconenableall"); //$NON-NLS-1$
    public static Tile DISABLE_ALL_TILE = new Tile("icondisableall"); //$NON-NLS-1$

    public final static int MOUSE_NONE = 0;
    public final static int MOUSE_BOTTOM_PANEL = 1;
    public final static int MOUSE_BOTTOM_LEFT_SCROLL = 2;
    public final static int MOUSE_BOTTOM_RIGHT_SCROLL = 3;
    public final static int MOUSE_BOTTOM_ITEMS = 4;
    public final static int MOUSE_BOTTOM_SUBPANEL = 5;
    public final static int MOUSE_BOTTOM_SUBITEMS = 6;
    public final static int MOUSE_BOTTOM_OPENCLOSE = 7;
    public final static int MOUSE_MINIMAP = 10;
    public final static int MOUSE_MESSAGES_PANEL = 20;
    public final static int MOUSE_MESSAGES_ICON_COMBAT = 21;
    public final static int MOUSE_MESSAGES_ICON_SYSTEM = 22;
    public final static int MOUSE_MESSAGES_ICON_ANNOUNCEMENT = 23;
    public final static int MOUSE_MESSAGES_ICON_HEROES = 24;
    public final static int MOUSE_MESSAGES_PANEL_BUTTONS_CLOSE = 25;
    public final static int MOUSE_MESSAGES_PANEL_BUTTONS_ANNOUNCEMENT = 26;
    public final static int MOUSE_MESSAGES_PANEL_BUTTONS_COMBAT = 27;
    public final static int MOUSE_MESSAGES_PANEL_BUTTONS_HEROES = 28;
    public final static int MOUSE_MESSAGES_PANEL_BUTTONS_SYSTEM = 29;
    public final static int MOUSE_MESSAGES_PANEL_BUTTONS_SCROLL_UP = 30;
    public final static int MOUSE_MESSAGES_PANEL_BUTTONS_SCROLL_DOWN = 31;
    public final static int MOUSE_MENU_PANEL = 35;
    public final static int MOUSE_MENU_PANEL_ITEMS = 36;
    public final static int MOUSE_MENU_OPENCLOSE = 37;
    public final static int MOUSE_ICON_LEVEL_UP = 40;
    public final static int MOUSE_ICON_LEVEL_DOWN = 41;
    public final static int MOUSE_ICON_CITIZEN_NEXT = 42;
    public final static int MOUSE_ICON_CITIZEN_PREVIOUS = 43;
    public final static int MOUSE_ICON_SOLDIER_NEXT = 44;
    public final static int MOUSE_ICON_SOLDIER_PREVIOUS = 45;
    public final static int MOUSE_ICON_LEVEL = 46;
    public final static int MOUSE_ICON_HERO_NEXT = 47;
    public final static int MOUSE_ICON_HERO_PREVIOUS = 48;
    // public final static int MOUSE_INFO_CURRENT_LEVEL = 50;
    public final static int MOUSE_INFO_NUM_CITIZENS = 51;
    public final static int MOUSE_INFO_NUM_SOLDIERS = 52;
    public final static int MOUSE_INFO_NUM_HEROES = 53;
    public final static int MOUSE_INFO_CARAVAN = 54;
    public final static int MOUSE_INFOPANEL = 60;
    public final static int MOUSE_DATEPANEL = 61;
    public final static int MOUSE_PRODUCTION_PANEL = 65;
    public final static int MOUSE_PRODUCTION_PANEL_ITEMS = 66;
    // public final static int MOUSE_PRODUCTION_PANEL_ICON = 67;
    public final static int MOUSE_PRODUCTION_PANEL_ITEMS_PLUS_REGULAR = 68;
    public final static int MOUSE_PRODUCTION_PANEL_ITEMS_MINUS_REGULAR = 69;
    public final static int MOUSE_PRODUCTION_PANEL_ITEMS_PLUS_AUTOMATED = 70;
    public final static int MOUSE_PRODUCTION_PANEL_ITEMS_MINUS_AUTOMATED = 71;
    public final static int MOUSE_PRODUCTION_OPENCLOSE = 72;
    public final static int MOUSE_ICON_PRIORITIES = 77;
    public final static int MOUSE_ICON_PAUSE_RESUME = 78;
    public final static int MOUSE_ICON_SETTINGS = 79;
    public final static int MOUSE_ICON_GRID = 80;
    public final static int MOUSE_PRIORITIES_PANEL = 81;
    public final static int MOUSE_PRIORITIES_PANEL_ITEMS = 82;
    public final static int MOUSE_PRIORITIES_PANEL_ITEMS_UP = 83;
    public final static int MOUSE_PRIORITIES_PANEL_ITEMS_DOWN = 84;
    public final static int MOUSE_ICON_INCREASE_SPEED = 85;
    public final static int MOUSE_ICON_LOWER_SPEED = 86;
    public final static int MOUSE_ICON_MINIBLOCKS = 87;
    public final static int MOUSE_ICON_FLATMOUSE = 88;
    public final static int MOUSE_ICON_3DMOUSE = 89;
    public final static int MOUSE_TRADE_PANEL = 90;
    public final static int MOUSE_TRADE_PANEL_BUTTONS_CARAVAN = 91;
    public final static int MOUSE_TRADE_PANEL_BUTTONS_DOWN_CARAVAN = 92;
    public final static int MOUSE_TRADE_PANEL_BUTTONS_UP_CARAVAN = 93;
    public final static int MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_CARAVAN = 94;
    public final static int MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_DOWN_CARAVAN = 95;
    public final static int MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_UP_CARAVAN = 96;
    public final static int MOUSE_TRADE_PANEL_BUTTONS_CONFIRM = 97;
    public final static int MOUSE_TRADE_PANEL_BUTTONS_TOWN = 98;
    public final static int MOUSE_TRADE_PANEL_BUTTONS_DOWN_TOWN = 99;
    public final static int MOUSE_TRADE_PANEL_BUTTONS_UP_TOWN = 100;
    public final static int MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_TOWN = 101;
    public final static int MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_DOWN_TOWN = 102;
    public final static int MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_UP_TOWN = 103;
    public final static int MOUSE_TRADE_PANEL_BUTTONS_CLOSE = 104;
    public final static int MOUSE_TRADE_PANEL_ICON_BUY = 105;
    public final static int MOUSE_TRADE_PANEL_ICON_SELL = 106;

    public final static int MOUSE_EVENTS_ICON = 107;
    // public final static int MOUSE_GODS_ICON = 108;
    public final static int MOUSE_TUTORIAL_ICON = 109;

    public final static int MOUSE_ICON_MATS = 120;
    public final static int MOUSE_MATS_PANEL = 121;
    public final static int MOUSE_MATS_PANEL_BUTTONS_CLOSE = 122;
    public final static int MOUSE_MATS_PANEL_BUTTONS_GROUPS = 123;
    public final static int MOUSE_MATS_PANEL_BUTTONS_ITEMS = 124;
    public final static int MOUSE_MATS_PANEL_BUTTONS_SCROLL_UP = 125;
    public final static int MOUSE_MATS_PANEL_BUTTONS_SCROLL_DOWN = 126;

    public final static int MOUSE_LIVINGS_PANEL = 140;
    public final static int MOUSE_LIVINGS_PANEL_BUTTONS_CLOSE = 141;
    public final static int MOUSE_LIVINGS_PANEL_BUTTONS_ROWS = 142;
    public final static int MOUSE_LIVINGS_PANEL_BUTTONS_SCROLL_UP = 143;
    public final static int MOUSE_LIVINGS_PANEL_BUTTONS_SCROLL_DOWN = 144;
    public final static int MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_HEAD = 145;
    public final static int MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_BODY = 146;
    public final static int MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_LEGS = 147;
    public final static int MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_FEET = 148;
    public final static int MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_WEAPON = 149;
    public final static int MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_AUTOEQUIP = 150;
    public final static int MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER = 151;
    public final static int MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_CIVILIAN = 152;
    public final static int MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_GUARD = 153;
    public final static int MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_PATROL = 154;
    public final static int MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_BOSS = 155;
    public final static int MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_SGROUP_ADD = 156;
    public final static int MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_SGROUP_REMOVE = 157;
    public final static int MOUSE_LIVINGS_PANEL_SGROUP_NOGROUP = 158;
    public final static int MOUSE_LIVINGS_PANEL_SGROUP_GROUP = 159;
    public final static int MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_RENAME = 160;
    public final static int MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_GUARD = 161;
    public final static int MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_PATROL = 162;
    public final static int MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_BOSS = 163;
    public final static int MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_AUTOEQUIP = 164;
    public final static int MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_DISBAND = 165;
    public final static int MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_PROFESSIONS = 166;
    public final static int MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_JOBS_GROUPS_ADDREMOVE = 167;
    public final static int MOUSE_LIVINGS_PANEL_CGROUP_NOGROUP = 168;
    public final static int MOUSE_LIVINGS_PANEL_CGROUP_GROUP = 169;
    public final static int MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_RENAME = 170;
    public final static int MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_AUTOEQUIP = 171;
    public final static int MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_DISBAND = 172;
    public final static int MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_CHANGE_JOBS = 173;
    public final static int MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_UP = 174;
    public final static int MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_DOWN = 175;

    public final static int MOUSE_TYPING_PANEL = 180;
    public final static int MOUSE_TYPING_PANEL_CLOSE = 181;
    public final static int MOUSE_TYPING_PANEL_CONFIRM = 182;

    public final static int MOUSE_PILE_PANEL = 185;
    public final static int MOUSE_PILE_PANEL_BUTTONS_CLOSE = 186;
    public final static int MOUSE_PILE_PANEL_BUTTONS_ITEMS = 187;
    public final static int MOUSE_PILE_PANEL_BUTTONS_SCROLL_UP = 188;
    public final static int MOUSE_PILE_PANEL_BUTTONS_SCROLL_DOWN = 189;
    public final static int MOUSE_PILE_PANEL_BUTTONS_CONFIG_COPY = 190;
    public final static int MOUSE_PILE_PANEL_BUTTONS_CONFIG_LOCK = 191;
    public final static int MOUSE_PILE_PANEL_BUTTONS_CONFIG_LOCK_ALL = 192;
    public final static int MOUSE_PILE_PANEL_BUTTONS_CONFIG_UNLOCK_ALL = 193;

    public final static int MOUSE_PROFESSIONS_PANEL = 195;
    public final static int MOUSE_PROFESSIONS_PANEL_BUTTONS_CLOSE = 196;
    public final static int MOUSE_PROFESSIONS_PANEL_BUTTONS_ITEMS = 197;
    public final static int MOUSE_PROFESSIONS_PANEL_BUTTONS_SCROLL_UP = 198;
    public final static int MOUSE_PROFESSIONS_PANEL_BUTTONS_SCROLL_DOWN = 199;

    public final static int MOUSE_IMAGES_PANEL = 200;
    public final static int MOUSE_IMAGES_PANEL_CLOSE = 201;
    public final static int MOUSE_IMAGES_PANEL_PREVIOUS = 202;
    public final static int MOUSE_IMAGES_PANEL_NEXT = 203;
    public final static int MOUSE_IMAGES_PANEL_NEXT_MISSION = 204;

    public static Point MOUSE_PRODUCTION_PANEL_ITEMS_POINT = new Point(MOUSE_PRODUCTION_PANEL_ITEMS, -1);
    public static Point MOUSE_PRODUCTION_PANEL_ITEMS_PLUS_REGULAR_POINT = new Point(
            MOUSE_PRODUCTION_PANEL_ITEMS_PLUS_REGULAR, -1);
    public static Point MOUSE_PRODUCTION_PANEL_ITEMS_MINUS_REGULAR_POINT = new Point(
            MOUSE_PRODUCTION_PANEL_ITEMS_MINUS_REGULAR, -1);
    public static Point MOUSE_PRODUCTION_PANEL_ITEMS_PLUS_AUTOMATED_POINT = new Point(
            MOUSE_PRODUCTION_PANEL_ITEMS_PLUS_AUTOMATED, -1);
    public static Point MOUSE_PRODUCTION_PANEL_ITEMS_MINUS_AUTOMATED_POINT = new Point(
            MOUSE_PRODUCTION_PANEL_ITEMS_MINUS_AUTOMATED, -1);

    public static Point MOUSE_PRIORITIES_PANEL_ITEMS_POINT = new Point(MOUSE_PRIORITIES_PANEL_ITEMS, -1);
    public static Point MOUSE_PRIORITIES_PANEL_ITEMS_UP_POINT = new Point(MOUSE_PRIORITIES_PANEL_ITEMS_UP, -1);
    public static Point MOUSE_PRIORITIES_PANEL_ITEMS_DOWN_POINT = new Point(MOUSE_PRIORITIES_PANEL_ITEMS_DOWN, -1);

    public static Point MOUSE_TRADE_PANEL_BUTTONS_CARAVAN_POINT = new Point(MOUSE_TRADE_PANEL_BUTTONS_CARAVAN, -1);
    public static Point MOUSE_TRADE_PANEL_BUTTONS_CARAVAN_UP_POINT = new Point(MOUSE_TRADE_PANEL_BUTTONS_UP_CARAVAN,
            -1);
    public static Point MOUSE_TRADE_PANEL_BUTTONS_CARAVAN_DOWN_POINT = new Point(MOUSE_TRADE_PANEL_BUTTONS_DOWN_CARAVAN,
            -1);
    public static Point MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_CARAVAN_POINT = new Point(
            MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_CARAVAN, -1);
    public static Point MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_CARAVAN_UP_POINT = new Point(
            MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_UP_CARAVAN, -1);
    public static Point MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_CARAVAN_DOWN_POINT = new Point(
            MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_DOWN_CARAVAN, -1);
    public static Point MOUSE_TRADE_PANEL_BUTTONS_CONFIRM_POINT = new Point(MOUSE_TRADE_PANEL_BUTTONS_CONFIRM, -1);
    public static Point MOUSE_TRADE_PANEL_BUTTONS_TOWN_POINT = new Point(MOUSE_TRADE_PANEL_BUTTONS_TOWN, -1);
    public static Point MOUSE_TRADE_PANEL_BUTTONS_TOWN_UP_POINT = new Point(MOUSE_TRADE_PANEL_BUTTONS_UP_TOWN, -1);
    public static Point MOUSE_TRADE_PANEL_BUTTONS_TOWN_DOWN_POINT = new Point(MOUSE_TRADE_PANEL_BUTTONS_DOWN_TOWN, -1);
    public static Point MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_TOWN_POINT = new Point(MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_TOWN,
            -1);
    public static Point MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_TOWN_UP_POINT = new Point(
            MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_UP_TOWN, -1);
    public static Point MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_TOWN_DOWN_POINT = new Point(
            MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_DOWN_TOWN, -1);
    public static Point MOUSE_TRADE_PANEL_BUTTONS_CLOSE_POINT = new Point(MOUSE_TRADE_PANEL_BUTTONS_CLOSE, -1);
    public static Point MOUSE_TRADE_PANEL_ICON_BUY_POINT = new Point(MOUSE_TRADE_PANEL_ICON_BUY, -1);
    public static Point MOUSE_TRADE_PANEL_ICON_SELL_POINT = new Point(MOUSE_TRADE_PANEL_ICON_SELL, -1);

    public static Point MOUSE_MESSAGES_PANEL_BUTTONS_CLOSE_POINT = new Point(MOUSE_MESSAGES_PANEL_BUTTONS_CLOSE, -1);
    public static Point MOUSE_MESSAGES_PANEL_BUTTONS_ANNOUNCEMENT_POINT = new Point(
            MOUSE_MESSAGES_PANEL_BUTTONS_ANNOUNCEMENT, -1);
    public static Point MOUSE_MESSAGES_PANEL_BUTTONS_COMBAT_POINT = new Point(MOUSE_MESSAGES_PANEL_BUTTONS_COMBAT, -1);
    public static Point MOUSE_MESSAGES_PANEL_BUTTONS_HEROES_POINT = new Point(MOUSE_MESSAGES_PANEL_BUTTONS_HEROES, -1);
    public static Point MOUSE_MESSAGES_PANEL_BUTTONS_SYSTEM_POINT = new Point(MOUSE_MESSAGES_PANEL_BUTTONS_SYSTEM, -1);
    public static Point MOUSE_MESSAGES_PANEL_BUTTONS_SCROLL_UP_POINT = new Point(MOUSE_MESSAGES_PANEL_BUTTONS_SCROLL_UP,
            -1);
    public static Point MOUSE_MESSAGES_PANEL_BUTTONS_SCROLL_DOWN_POINT = new Point(
            MOUSE_MESSAGES_PANEL_BUTTONS_SCROLL_DOWN, -1);

    public static Point MOUSE_MATS_PANEL_BUTTONS_CLOSE_POINT = new Point(MOUSE_MATS_PANEL_BUTTONS_CLOSE, -1);
    public static Point MOUSE_MATS_PANEL_BUTTONS_GROUPS_POINT = new Point(MOUSE_MATS_PANEL_BUTTONS_GROUPS, -1);
    public static Point MOUSE_MATS_PANEL_BUTTONS_ITEMS_POINT = new Point(MOUSE_MATS_PANEL_BUTTONS_ITEMS, -1);
    public static Point MOUSE_MATS_PANEL_BUTTONS_SCROLL_UP_POINT = new Point(MOUSE_MATS_PANEL_BUTTONS_SCROLL_UP, -1);
    public static Point MOUSE_MATS_PANEL_BUTTONS_SCROLL_DOWN_POINT = new Point(MOUSE_MATS_PANEL_BUTTONS_SCROLL_DOWN,
            -1);

    public static Point MOUSE_PILE_PANEL_BUTTONS_CLOSE_POINT = new Point(MOUSE_PILE_PANEL_BUTTONS_CLOSE, -1);
    public static Point MOUSE_PILE_PANEL_BUTTONS_ITEMS_POINT = new Point(MOUSE_PILE_PANEL_BUTTONS_ITEMS, -1);
    public static Point MOUSE_PILE_PANEL_BUTTONS_SCROLL_UP_POINT = new Point(MOUSE_PILE_PANEL_BUTTONS_SCROLL_UP, -1);
    public static Point MOUSE_PILE_PANEL_BUTTONS_SCROLL_DOWN_POINT = new Point(MOUSE_PILE_PANEL_BUTTONS_SCROLL_DOWN,
            -1);
    public static Point MOUSE_PILE_PANEL_BUTTONS_CONFIG_COPY_POINT = new Point(MOUSE_PILE_PANEL_BUTTONS_CONFIG_COPY,
            -1);
    public static Point MOUSE_PILE_PANEL_BUTTONS_CONFIG_LOCK_POINT = new Point(MOUSE_PILE_PANEL_BUTTONS_CONFIG_LOCK,
            -1);
    public static Point MOUSE_PILE_PANEL_BUTTONS_CONFIG_LOCK_ALL_POINT = new Point(
            MOUSE_PILE_PANEL_BUTTONS_CONFIG_LOCK_ALL, -1);
    public static Point MOUSE_PILE_PANEL_BUTTONS_CONFIG_UNLOCK_ALL_POINT = new Point(
            MOUSE_PILE_PANEL_BUTTONS_CONFIG_UNLOCK_ALL, -1);

    public static Point MOUSE_PROFESSIONS_PANEL_BUTTONS_CLOSE_POINT = new Point(MOUSE_PROFESSIONS_PANEL_BUTTONS_CLOSE,
            -1);
    public static Point MOUSE_PROFESSIONS_PANEL_BUTTONS_ITEMS_POINT = new Point(MOUSE_PROFESSIONS_PANEL_BUTTONS_ITEMS,
            -1);
    public static Point MOUSE_PROFESSIONS_PANEL_BUTTONS_SCROLL_UP_POINT = new Point(
            MOUSE_PROFESSIONS_PANEL_BUTTONS_SCROLL_UP, -1);
    public static Point MOUSE_PROFESSIONS_PANEL_BUTTONS_SCROLL_DOWN_POINT = new Point(
            MOUSE_PROFESSIONS_PANEL_BUTTONS_SCROLL_DOWN, -1);

    public static Point MOUSE_LIVINGS_PANEL_BUTTONS_CLOSE_POINT = new Point(MOUSE_LIVINGS_PANEL_BUTTONS_CLOSE, -1);
    public static Point MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_POINT = new Point(MOUSE_LIVINGS_PANEL_BUTTONS_ROWS, -1);
    public static Point MOUSE_LIVINGS_PANEL_BUTTONS_SCROLL_UP_POINT = new Point(MOUSE_LIVINGS_PANEL_BUTTONS_SCROLL_UP,
            -1);
    public static Point MOUSE_LIVINGS_PANEL_BUTTONS_SCROLL_DOWN_POINT = new Point(
            MOUSE_LIVINGS_PANEL_BUTTONS_SCROLL_DOWN, -1);
    public static Point MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_HEAD_POINT = new Point(MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_HEAD,
            -1);
    public static Point MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_BODY_POINT = new Point(MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_BODY,
            -1);
    public static Point MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_LEGS_POINT = new Point(MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_LEGS,
            -1);
    public static Point MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_FEET_POINT = new Point(MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_FEET,
            -1);
    public static Point MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_WEAPON_POINT = new Point(
            MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_WEAPON, -1);
    public static Point MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_AUTOEQUIP_POINT = new Point(
            MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_AUTOEQUIP, -1);
    public static Point MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_PROFESSIONS_POINT = new Point(
            MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_PROFESSIONS, -1);
    public static Point MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_JOBS_GROUPS_ADDREMOVE_POINT = new Point(
            MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_JOBS_GROUPS_ADDREMOVE, -1);
    public static Point MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_POINT = new Point(
            MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER, -1);
    public static Point MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_CIVILIAN_POINT = new Point(
            MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_CIVILIAN, -1);
    public static Point MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_GUARD_POINT = new Point(
            MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_GUARD, -1);
    public static Point MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_PATROL_POINT = new Point(
            MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_PATROL, -1);
    public static Point MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_BOSS_POINT = new Point(
            MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_BOSS, -1);
    public static Point MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_SGROUP_ADD_POINT = new Point(
            MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_SGROUP_ADD, -1);
    public static Point MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_SGROUP_REMOVE_POINT = new Point(
            MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_SGROUP_REMOVE, -1);
    public static Point MOUSE_LIVINGS_PANEL_SGROUP_NOGROUP_POINT = new Point(MOUSE_LIVINGS_PANEL_SGROUP_NOGROUP, -1);
    public static Point MOUSE_LIVINGS_PANEL_SGROUP_GROUP_POINT = new Point(MOUSE_LIVINGS_PANEL_SGROUP_GROUP, -1);
    public static Point MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_RENAME_POINT = new Point(
            MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_RENAME, -1);
    public static Point MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_GUARD_POINT = new Point(
            MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_GUARD, -1);
    public static Point MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_PATROL_POINT = new Point(
            MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_PATROL, -1);
    public static Point MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_BOSS_POINT = new Point(MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_BOSS,
            -1);
    public static Point MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_AUTOEQUIP_POINT = new Point(
            MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_AUTOEQUIP, -1);
    public static Point MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_DISBAND_POINT = new Point(
            MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_DISBAND, -1);
    public static Point MOUSE_LIVINGS_PANEL_CGROUP_NOGROUP_POINT = new Point(MOUSE_LIVINGS_PANEL_CGROUP_NOGROUP, -1);
    public static Point MOUSE_LIVINGS_PANEL_CGROUP_GROUP_POINT = new Point(MOUSE_LIVINGS_PANEL_CGROUP_GROUP, -1);
    public static Point MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_RENAME_POINT = new Point(
            MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_RENAME, -1);
    public static Point MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_AUTOEQUIP_POINT = new Point(
            MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_AUTOEQUIP, -1);
    public static Point MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_DISBAND_POINT = new Point(
            MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_DISBAND, -1);
    public static Point MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_CHANGE_JOBS_POINT = new Point(
            MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_CHANGE_JOBS, -1);
    public static Point MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_UP_POINT = new Point(
            MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_UP, -1);
    public static Point MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_DOWN_POINT = new Point(
            MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_DOWN, -1);

            	/*
	 * BOTTOM PANEL
	 */
	public final static int BOTTOM_PANEL_SCROLL_WIDTH = 32;
	public final static int BOTTOM_PANEL_WIDTH = 1024 - (BOTTOM_PANEL_SCROLL_WIDTH * 2);
	public final static int BOTTOM_PANEL_HEIGHT = 64;
	public final static int BOTTOM_PANEL_NUM_ITEMS = 10;
	public final static int BOTTOM_ITEM_WIDTH = 64;
	public final static int BOTTOM_ITEM_HEIGHT = 64;

	public static int BOTTOM_SUBPANEL_WIDTH;
	public static int BOTTOM_SUBPANEL_HEIGHT;
	public static int BOTTOM_SUBPANEL_NUM_ITEMS_X;
	public static int BOTTOM_SUBPANEL_NUM_ITEMS_Y;
	public static int BOTTOM_SUBITEM_WIDTH = 64;
	public static int BOTTOM_SUBITEM_HEIGHT = 64;

	/*
	 * PRODUCTION PANEL
	 */
	public final static int PRODUCTION_PANEL_ITEM_WIDTH = 64;
	public final static int PRODUCTION_PANEL_ITEM_HEIGHT = 64;
	public static int PRODUCTION_PANEL_NUM_ITEMS_X;
	public static int PRODUCTION_PANEL_NUM_ITEMS_Y;
	public static int PRODUCTION_PANEL_WIDTH;
	public static int PRODUCTION_PANEL_HEIGHT;

	/*
	 * TRADE PANEL
	 */
	public static int TRADE_PANEL_WIDTH;
	public static int TRADE_PANEL_HEIGHT;
	public final static int TRADE_PANEL_BUTTON_WIDTH = 64;
	public final static int TRADE_PANEL_BUTTON_HEIGHT = 64;

	/*
	 * PRIORITIES PANEL
	 */
	public final static int PRIORITIES_PANEL_ITEM_SIZE = 64;
	public static int PRIORITIES_PANEL_NUM_ITEMS;
	public static int PRIORITIES_PANEL_WIDTH;
	public static int PRIORITIES_PANEL_HEIGHT;
	public static int PRIORITIES_PANEL_ICON_WIDTH;
	public static int PRIORITIES_PANEL_ICON_HEIGHT;

	/*
	 * MATS PANEL
	 */
	public static int MATS_PANEL_WIDTH;
	public static int MATS_PANEL_HEIGHT;
	public static int MATS_PANEL_SUBPANEL_WIDTH;
	public static int MATS_PANEL_SUBPANEL_HEIGHT;
	public static int MATS_PANEL_MAX_ITEMS_PER_PAGE;

	/*
	 * PILE PANEL
	 */
	public static int PILE_PANEL_WIDTH;
	public static int PILE_PANEL_HEIGHT;
	public static int PILE_PANEL_MAX_ITEMS_PER_PAGE;

	/*
	 * PROFESSIONS PANEL
	 */
	public static int PROFESSIONS_PANEL_WIDTH;
	public static int PROFESSIONS_PANEL_HEIGHT;
	public static int PROFESSIONS_PANEL_MAX_ITEMS_PER_PAGE;

	/*
	 * LIVINGS PANEL
	 */
	public static int LIVINGS_PANEL_WIDTH;
	public static int LIVINGS_PANEL_HEIGHT;
	public static int LIVINGS_PANEL_MAX_ROWS = 1;
	public static int LIVINGS_PANEL_GROUPS_WIDTH;
	public static int LIVINGS_PANEL_GROUPS_HEIGHT;
	public static int LIVINGS_PANEL_SINGLE_GROUP_WIDTH;
	public static int LIVINGS_PANEL_SINGLE_GROUP_HEIGHT;

	public static final int LIVINGS_PANEL_TYPE_NONE = -1;
	public static final int LIVINGS_PANEL_TYPE_CITIZENS = 0;
	public static final int LIVINGS_PANEL_TYPE_SOLDIERS = 1;
	public static final int LIVINGS_PANEL_TYPE_HEROES = 2;

	/*
	 * MINIMAP
	 */
	public static int MINIMAP_PANEL_WIDTH = World.MAP_WIDTH;
	public static int MINIMAP_PANEL_HEIGHT = World.MAP_HEIGHT;

	/*
	 * MESSAGES PANEL
	 */
	public static int MESSAGES_PANEL_WIDTH;
	public static int MESSAGES_PANEL_HEIGHT;
	public static int MESSAGES_PANEL_SUBPANEL_WIDTH;
	public static int MESSAGES_PANEL_SUBPANEL_HEIGHT;

	/*
	 * MENU PANEL
	 */
	public final static int MENU_ITEM_WIDTH = 64;
	public final static int MENU_ITEM_HEIGHT = 64;
	public static int MENU_PANEL_NUM_ITEMS_X;
	public static int MENU_PANEL_NUM_ITEMS_Y;
	public static int MENU_PANEL_WIDTH;
	public static int MENU_PANEL_HEIGHT;

	/*
	 * MINI-ICONS
	 */
	public final static int ICON_WIDTH = 32;
	public final static int ICON_HEIGHT = 32;

	public static int delayTime;
	public static int blinkTurns;
	public static SmartMenu currentMenu;
	public static boolean bottomMenuPanelActive = true;
	public static boolean bottomMenuPanelLocked = true;

	public static int renderWidth;
	public static int renderHeight;

	// BOTTOM panel
	public static ArrayList<Point> bottomPanelItemsPosition; // Array de sólo BOTTOM_PANEL_NUM_ITEMS posiciones (9) con
																// las coordenadas de los items que caben
	public static int bottomPanelItemIndex;
	public static int bottomPanelX;
	public static int bottomPanelY;
	public static int bottomPanelLeftScrollX;
	public static int bottomPanelRightScrollX;

	public static Tile tileBottomItem;
	public static Tile tileBottomItemSM;
	public static Tile tileBottomScrollLeft;
	public static Tile tileBottomScrollLeftON;
	public static Tile tileBottomScrollRight;
	public static Tile tileBottomScrollRightON;
	public static Tile tileBottomPanel;
	public static boolean tileBottomItemAlpha[][];
	public static boolean tileBottomScrollLeftAlpha[][];
	public static boolean tileBottomScrollRightAlpha[][];
	public static boolean tileBottomPanelAlpha[][];

	// Open/close bottom
	public static Tile tileOpenBottomMenu;
	public static boolean tileOpenBottomMenuAlpha[][];
	public static Tile tileOpenBottomMenuON;
	public static boolean tileOpenBottomMenuONAlpha[][];
	public static Point tileOpenCloseBottomMenuPoint = new Point(0, 0);

	// BOTTOM subpanel
	public static ArrayList<Point> bottomSubPanelItemsPosition; // Array de BOTTOM_SUBPANEL_NUM_ITEMS_X x
																	// BOTTOM_SUBPANEL_NUM_ITEMS_Y posiciones con las
																	// coordenadas de los subitems
	public static Point bottomSubPanelPoint = new Point(0, 0);
	public static Tile tileBottomSubItem;
	public static Tile[] tileBottomSubPanel;
	public static SmartMenu bottomSubPanelMenu;

	public static boolean tileBottomSubItemAlpha[][];

	// MINIMAP panel
	public static int minimapPanelX;
	public static int minimapPanelY;

	public static Tile tileMinimapPanel;
	public static boolean tileMinimapPanelAlpha[][];

	// PRODUCTION panel
	public static Tile[] tileProductionPanel;
	public static Point productionPanelPoint = new Point(0, 0);
	public static boolean productionPanelActive = false;
	public static boolean productionPanelLocked = false;

	public static SmartMenu productionPanelMenu;
	public static ArrayList<Point> productionPanelItemsPosition = new ArrayList<Point>();
	public static Tile tileProductionPanelPlusIcon;
	public static boolean tileProductionPanelPlusIconAlpha[][];
	public static ArrayList<Point> productionPanelItemsPlusRegularPosition = new ArrayList<Point>();
	public static ArrayList<Point> productionPanelItemsPlusAutomatedPosition = new ArrayList<Point>();
	public static Tile tileProductionPanelMinusIcon;
	public static boolean tileProductionPanelMinusIconAlpha[][];
	public static ArrayList<Point> productionPanelItemsMinusRegularPosition = new ArrayList<Point>();
	public static ArrayList<Point> productionPanelItemsMinusAutomatedPosition = new ArrayList<Point>();

	// Open/close production
	public static Tile tileOpenProductionPanel;
	public static boolean tileOpenProductionPanelAlpha[][];
	public static Tile tileOpenProductionPanelON;
	public static boolean tileOpenProductionPanelONAlpha[][];
	public static Point tileOpenCloseProductionPanelPoint = new Point(0, 0);

	// TRADE panel
	public static Tile[] tileTradePanel;
	public static Point tradePanelPoint = new Point(0, 0);
	public static Point tradePanelClosePoint = new Point(0, 0);
	public static TradePanel tradePanel;
	public static boolean tradePanelActive = false;
	public static boolean tradePanelActivePausedBefore = false;
	public static Tile tileScrollUp = new Tile("scrollup"); //$NON-NLS-1$
	public static Tile tileScrollUpDisabled = new Tile("scrollup_disabled"); //$NON-NLS-1$
	public static final boolean tileScrollUpButtonAlpha[][] = UtilsGL.generateAlpha(tileScrollUp);
	public static Tile tileScrollDown = new Tile("scrolldown"); //$NON-NLS-1$
	public static Tile tileScrollDownDisabled = new Tile("scrolldown_disabled"); //$NON-NLS-1$
	public static final boolean tileScrollDownButtonAlpha[][] = UtilsGL.generateAlpha(tileScrollDown);

	// PRIORITIES panel
	public static Tile[] tilePrioritiesPanel;
	public static Point prioritiesPanelPoint = new Point(0, 0);
	public static boolean prioritiesPanelActive = false;

	public static ArrayList<Point> prioritiesPanelItemsPosition;
	public static Tile tilePrioritiesPanelUpIcon;
	public static boolean tilePrioritiesPanelUpIconAlpha[][];
	public static ArrayList<Point> prioritiesPanelItemsUpPosition;
	public static Tile tilePrioritiesPanelDownIcon;
	public static boolean tilePrioritiesPanelDownIconAlpha[][];
	public static ArrayList<Point> prioritiesPanelItemsDownPosition;

	// MATS panel
	public static Tile[] tileMatsPanel;
	public static Tile[] tileMatsPanelSubPanel;
	public static Point matsPanelPoint = new Point(0, 0);
	public static int matsPanelActive = -1;
	public static Point matsPanelClosePoint = new Point(0, 0);
	public static Point matsPanelIconScrollUpPoint = new Point(0, 0);
	public static Point matsPanelIconScrollDownPoint = new Point(0, 0);
	public static Tile[] matsPanelTiles;
	public static Tile[] matsPanelTilesON;
	public static Point matsPanelSubPanelPoint = new Point(0, 0);
	public static Point[] matsPanelIconPoints;
	public static Point[] matsPanelItemPoints;
	public static Point matsPanelPagesPositionPoint = new Point(0, 0);
	public static int[] matsNumPages;
	public static int[] matsIndexPages;
	public static int matsLastGroup = -1;

	// PILE panel
	public static Point pilePanelPoint = new Point(0, 0);
	public static int pilePanelPileContainerIDActive = -1;
	public static boolean pilePanelIsContainer = false;
	public static boolean pilePanelIsLocked = false;
	public static Point pilePanelClosePoint = new Point(0, 0);
	public static Point pilePanelIconScrollUpPoint = new Point(0, 0);
	public static Point pilePanelIconScrollDownPoint = new Point(0, 0);
	public static Point pilePanelIconConfigCopyPoint = new Point(0, 0);
	public static Point pilePanelIconConfigLockPoint = new Point(0, 0);
	public static Point pilePanelIconConfigUnlockAllPoint = new Point(0, 0);
	public static Point pilePanelIconConfigLockAllPoint = new Point(0, 0);
	public static Point[] pilePanelItemPoints;
	public static Point pilePanelPagesPositionPoint = new Point(0, 0);
	public static SmartMenu menuPile = null;
	public static int pilePanelPageIndex = -1;
	public static int pilePanelMaxPages = -1;
	public static Tile tileConfigCopy = new Tile("configcopy"); //$NON-NLS-1$
	public static final boolean tileConfigCopyButtonAlpha[][] = UtilsGL.generateAlpha(tileConfigCopy);
	public static Tile tileConfigLock = new Tile("configlock"); //$NON-NLS-1$
	public static final boolean tileConfigLockButtonAlpha[][] = UtilsGL.generateAlpha(tileConfigLock);
	public static Tile tileConfigLockLocked = new Tile("configlocklocked"); //$NON-NLS-1$
	public static final boolean tileConfigLockLockedButtonAlpha[][] = UtilsGL.generateAlpha(tileConfigLockLocked);
	public static Tile tileConfigLockAll = new Tile("configlockall"); //$NON-NLS-1$
	public static final boolean tileConfigLockAllButtonAlpha[][] = UtilsGL.generateAlpha(tileConfigLockAll);
	public static Tile tileConfigUnlockAll = new Tile("configunlockall"); //$NON-NLS-1$
	public static final boolean tileConfigUnlockAllButtonAlpha[][] = UtilsGL.generateAlpha(tileConfigUnlockAll);

	// PROFESSIONS panel
	public static Point professionsPanelPoint = new Point(0, 0);
	public static int professionsPanelCitizenOrGroupIDActive = -1;
	public static boolean professionsPanelIsCitizen = true;
	public static Point professionsPanelClosePoint = new Point(0, 0);
	public static Point professionsPanelIconScrollUpPoint = new Point(0, 0);
	public static Point professionsPanelIconScrollDownPoint = new Point(0, 0);
	public static Point[] professionsPanelItemPoints;
	public static Point professionsPanelPagesPositionPoint = new Point(0, 0);
	public static SmartMenu menuProfessions = null;
	public static int professionsPanelPageIndex = -1;
	public static int professionsPanelMaxPages = -1;

	// LIVINGS PANEL
	public static Tile[] tileLivingsPanel;
	public static Point livingsPanelPoint = new Point(0, 0);
	public static int livingsPanelActive = LIVINGS_PANEL_TYPE_NONE;
	public static int livingsPanelCitizensGroupActive = -1;
	public static int livingsPanelSoldiersGroupActive = -1;
	public static Point livingsPanelClosePoint = new Point(0, 0);
	public static Point livingsPanelIconScrollUpPoint = new Point(0, 0);
	public static Point livingsPanelIconScrollDownPoint = new Point(0, 0);
	public static Point livingsPanelPagesPoint = new Point(0, 0);
	public static Point[] livingsPanelRowPoints;
	public static Point[] livingsPanelRowHeadPoints;
	public static Point[] livingsPanelRowBodyPoints;
	public static Point[] livingsPanelRowLegsPoints;
	public static Point[] livingsPanelRowFeetPoints;
	public static Point[] livingsPanelRowWeaponPoints;
	public static Tile tileLivingsPanelRowNoHead;
	public static Tile tileLivingsPanelRowNoBody;
	public static Tile tileLivingsPanelRowNoLegs;
	public static Tile tileLivingsPanelRowNoFeet;
	public static Tile tileLivingsPanelRowNoWeapon;
	public static Point[] livingsPanelRowAutoequipPoints;
	public static Tile tileLivingsRowAutoequip;
	public static Tile tileLivingsRowAutoequipON;
	public static boolean tileLivingsRowAutoequipAlpha[][];
	public static Point[] livingsPanelRowProfessionPoints;
	public static Point[] livingsPanelRowJobsGroupsPoints;
	public static Point[] livingsPanelRowConvertCivilianSoldierPoints;
	public static Point[] livingsPanelRowConvertSoldierGuardPoints;
	public static Point[] livingsPanelRowConvertSoldierPatrolPoints;
	public static Point[] livingsPanelRowConvertSoldierBossPoints;
	public static Tile tileLivingsRowProfession;
	public static Tile tileLivingsRowJobsGroups;
	public static Tile tileLivingsRowProfessionON;
	public static Tile tileLivingsRowJobsGroupsON;
	public static Tile tileLivingsRowConvertSoldier;
	public static Tile tileLivingsRowConvertSoldierON;
	public static Tile tileLivingsRowConvertCivilian;
	public static Tile tileLivingsRowConvertCivilianON;
	public static Tile tileLivingsRowConvertSoldierGuard;
	public static Tile tileLivingsRowConvertSoldierGuardON;
	public static Tile tileLivingsRowConvertSoldierPatrol;
	public static Tile tileLivingsRowConvertSoldierPatrolON;
	public static Tile tileLivingsRowConvertSoldierBoss;
	public static Tile tileLivingsRowConvertSoldierBossON;
	public static boolean tileLivingsRowProfessionAlpha[][];
	public static boolean tileLivingsRowJobsGroupsAlpha[][];
	public static boolean tileLivingsRowConvertSoldierAlpha[][];
	public static boolean tileLivingsRowConvertCivilianAlpha[][];
	public static boolean tileLivingsRowConvertSoldierGuardAlpha[][];
	public static boolean tileLivingsRowConvertSoldierPatrolAlpha[][];
	public static boolean tileLivingsRowConvertSoldierBossAlpha[][];

	public static Point[] livingsPanelRowGroupPoints;
	public static Tile tileLivingsRowGroupAdd;
	public static Tile tileLivingsRowGroupAddON;
	public static boolean tileLivingsRowGroupAddAlpha[][];
	public static Tile tileLivingsRowGroupRemove;
	public static Tile tileLivingsRowGroupRemoveON;
	public static boolean tileLivingsRowGroupRemoveAlpha[][];

	public static Point livingsPanelIconRestrictUpPoint = new Point(0, 0);
	public static Point livingsPanelIconRestrictDownPoint = new Point(0, 0);

	// LIVINGS GROUP PANEL data
	public static Tile[] tileLivingsGroupPanel;
	public static Point livingsGroupPanelPoint = new Point(0, 0);
	public static Point livingsSingleGroupPanelPoint = new Point(0, 0);
	public static Point livingsGroupPanelFirstIconPoint = new Point(0, 0);
	public static int livingsGroupPanelIconsSeparation = Tile.TERRAIN_ICON_WIDTH; // Esto se cambiará seguro, no tiene
																					// nada que ver, es por si acaso
	public static Tile tileLivingsGroup;
	public static Tile tileLivingsGroupON;
	public static Tile tileLivingsGroupGreen;
	public static boolean tileLivingsGroupAlpha[][];
	public static Tile tileLivingsNoGroup;
	public static Tile tileLivingsNoGroupON;
	public static Tile tileLivingsNoGroupGreen;
	public static boolean tileLivingsNoGroupAlpha[][];
	public static Tile tileLivingsNoJobGroup;
	public static Tile tileLivingsNoJobGroupON;
	public static Tile tileLivingsNoJobGroupGreen;
	public static boolean tileLivingsNoJobGroupAlpha[][];
	public static Tile tileLivingsJobGroup;
	public static Tile tileLivingsJobGroupON;
	public static Tile tileLivingsJobGroupGreen;
	public static boolean tileLivingsJobGroupAlpha[][];

	public static Point livingsSingleGroupRenamePoint = new Point(0, 0);
	public static Tile tileLivingsSingleGroupRename;
	public static Tile tileLivingsSingleGroupRenameON;
	public static boolean tileLivingsSingleGroupRenameAlpha[][];
	public static Tile tileLivingsSingleJobGroupRename;
	public static Tile tileLivingsSingleJobGroupRenameON;
	public static boolean tileLivingsSingleJobGroupRenameAlpha[][];
	public static Point livingsSingleGroupGuardPoint = new Point(0, 0);
	public static Tile tileLivingsSingleGroupGuard;
	public static Tile tileLivingsSingleGroupGuardON;
	public static boolean tileLivingsSingleGroupGuardAlpha[][];
	public static Point livingsSingleGroupPatrolPoint = new Point(0, 0);
	public static Tile tileLivingsSingleGroupPatrol;
	public static Tile tileLivingsSingleGroupPatrolON;
	public static boolean tileLivingsSingleGroupPatrolAlpha[][];
	public static Point livingsSingleGroupBossPoint = new Point(0, 0);
	public static Tile tileLivingsSingleGroupBoss;
	public static Tile tileLivingsSingleGroupBossON;
	public static boolean tileLivingsSingleGroupBossAlpha[][];
	public static Point livingsSingleGroupDisbandPoint = new Point(0, 0);
	public static Tile tileLivingsSingleGroupDisband;
	public static Tile tileLivingsSingleGroupDisbandON;
	public static boolean tileLivingsSingleGroupDisbandAlpha[][];
	public static Tile tileLivingsSingleJobGroupDisband;
	public static Tile tileLivingsSingleJobGroupDisbandON;
	public static boolean tileLivingsSingleJobGroupDisbandAlpha[][];
	public static Point livingsSingleGroupAutoequipPoint = new Point(0, 0);
	public static Point livingsSingleGroupChangeJobsPoint = new Point(0, 0);
	public static Tile tileLivingsSingleGroupChangeJobs;
	public static Tile tileLivingsSingleGroupChangeJobsON;
	public static boolean tileLivingsSingleGroupChangeJobsAlpha[][];

	// LIVINGS PANEL data
	public static int[] livingsDataIndexPages;
	public static int[] livingsDataIndexPagesCitizenGroups;
	public static int[] livingsDataIndexPagesSoldierGroups;

	// MESSAGES panel
	public static int messagesPanelActive = -1;

	public static Point[] messageIconPoints;
	public static Point[] messagePanelIconPoints;
	public static Tile[] messageTiles;
	public static Tile[] messageTilesON;
	public static ArrayList<boolean[][]> messageTilesAlpha;
	public static Tile[] messagePanelTiles;
	public static Tile[] messagePanelTilesON;
	public static ArrayList<boolean[][]> messagePanelTilesAlpha;
	public static Point messagesPanelPoint = new Point(0, 0);
	public static Point messagesPanelSubPanelPoint = new Point(0, 0);
	public static Tile[] tileMessagesPanel;
	public static Tile[] tileMessagesPanelSubPanel;
	public static Point messagesPanelClosePoint = new Point(0, 0);
	public static Point messagePanelIconScrollUpPoint = new Point(0, 0);
	public static Point messagePanelIconScrollDownPoint = new Point(0, 0);
	public static Point messagePanelPagesPositionPoint = new Point(0, 0);

	// MENU panel (right)
	public static Point menuPanelPoint = new Point(0, 0);
	public static ArrayList<Point> menuPanelItemsPosition;

	public static Tile[] tileMenuPanel;

	public static SmartMenu menuPanelMenu = null;
	public static boolean menuPanelActive = false;
	public static boolean menuPanelLocked = false;

	// Open/close right menu
	public static Tile tileOpenRightMenu;
	public static boolean tileOpenRightMenuAlpha[][];
	public static Tile tileOpenRightMenuON;
	public static boolean tileOpenRightMenuONAlpha[][];
	public static Point tileOpenCloseRightMenuPoint = new Point(0, 0);

	// Close panels
	public static Tile tileButtonClose = new Tile("panel_close"); //$NON-NLS-1$
	public static Tile tileButtonCloseDisabled = new Tile("panel_close_disabled"); //$NON-NLS-1$
	public static final boolean tileButtonCloseAlpha[][] = UtilsGL.generateAlpha(tileButtonClose);

	// MINI-ICONS
	public static boolean tileIconNextMiniAlpha[][];
	public static boolean tileIconPreviousMiniAlpha[][];
	public static Point iconLevelUpPoint = new Point(0, 0);
	public static Tile tileIconLevelUp;
	public static boolean tileIconLevelUpAlpha[][];
	public static Point iconLevelDownPoint = new Point(0, 0);
	public static Tile tileIconLevelDown;
	public static boolean tileIconLevelDownAlpha[][];
	public static Point iconLevelPoint = new Point(0, 0);
	public static Tile tileIconLevel;
	public static boolean tileIconLevelAlpha[][];

	// ICONS
	public static Tile tileIconPriorities;
	public static Tile tileIconPrioritiesON;
	public static boolean tileIconPrioritiesAlpha[][];
	public static Point iconPrioritiesPoint = new Point(0, 0);
	public static Tile tileIconMats;
	public static Tile tileIconMatsON;
	public static boolean tileIconMatsAlpha[][];
	public static Point iconMatsPoint = new Point(0, 0);
	public static Tile tileIconPause;
	public static boolean tileIconPauseResumeAlpha[][];
	public static Tile tileIconResume;
	public static Point iconPauseResumePoint = new Point(0, 0);
	public static Tile tileIconIncreaseSpeed;
	public static Tile tileIconIncreaseSpeedON;
	public static boolean tileIconIncreaseSpeedAlpha[][];
	public static Point iconIncreaseSpeedPoint = new Point(0, 0);
	public static Tile tileIconLowerSpeed;
	public static Tile tileIconLowerSpeedON;
	public static boolean tileIconLowerSpeedAlpha[][];
	public static Point iconLowerSpeedPoint = new Point(0, 0);
	public static Tile tileIconSettings;
	public static boolean tileIconSettingsAlpha[][];
	public static Point iconSettingsPoint = new Point(0, 0);
	public static Tile tileIconGrid;
	public static Tile tileIconGridON;
	public static boolean tileIconGridAlpha[][];
	public static Point iconGridPoint = new Point(0, 0);
	public static Tile tileIconMiniblocks;
	public static Tile tileIconMiniblocksON;
	public static boolean tileIconMiniblocksAlpha[][];
	public static Point iconMiniblocksPoint = new Point(0, 0);
	public static Tile tileIconFlatMouse;
	public static Tile tileIconFlatMouseON;
	public static boolean tileIconFlatMouseAlpha[][];
	public static Point iconFlatMousePoint = new Point(0, 0);
	public static Tile tileIcon3DMouse;
	public static Tile tileIcon3DMouseON;
	public static boolean tileIcon3DMouseAlpha[][];
	public static Point icon3DMousePoint = new Point(0, 0);

	public static Point iconEventsPoint = new Point(0, 0);
	// public static Point iconGodsPoint = new Point (0, 0);
	// public static Tile tileIconGods;
	public static Point iconTutorialPoint = new Point(0, 0);
	public static Tile tileIconTutorial;

	// INFO
	public static Tile tileIconNumCitizens;
	public static Point iconNumCitizensBackgroundPoint = new Point(0, 0);
	public static Point iconNumCitizensPoint = new Point(0, 0);
	public static Point iconCitizenNextPoint = new Point(0, 0);
	public static Tile tileIconCitizenNext;
	public static Tile tileIconCitizenNextON;
	public static Point iconCitizenPreviousPoint = new Point(0, 0);
	public static Tile tileIconCitizenPrevious;
	public static Tile tileIconCitizenPreviousON;

	public static Tile tileIconNumSoldiers;
	public static Point iconNumSoldiersBackgroundPoint = new Point(0, 0);
	public static Point iconNumSoldiersPoint = new Point(0, 0);
	public static Point iconSoldierNextPoint = new Point(0, 0);
	public static Tile tileIconSoldierNext;
	public static Tile tileIconSoldierNextON;
	public static Point iconSoldierPreviousPoint = new Point(0, 0);
	public static Tile tileIconSoldierPrevious;
	public static Tile tileIconSoldierPreviousON;

	public static Tile tileIconNumHeroes;
	public static Point iconNumHeroesBackgroundPoint = new Point(0, 0);
	public static Point iconNumHeroesPoint = new Point(0, 0);
	public static Point iconHeroNextPoint = new Point(0, 0);
	public static Tile tileIconHeroNext;
	public static Tile tileIconHeroNextON;
	public static Point iconHeroPreviousPoint = new Point(0, 0);
	public static Tile tileIconHeroPrevious;
	public static Tile tileIconHeroPreviousON;

	public static Tile tileIconCaravan;
	public static Tile tileIconCaravanON;
	public static Point iconCaravanBackgroundPoint = new Point(0, 0);
	public static Point iconCaravanPoint = new Point(0, 0);

	public static Tile tileInfoPanel;
	public static boolean tileInfoPanelAlpha[][];
	public static Point infoPanelPoint = new Point(0, 0);

	public static Tile tileDatePanel;
	public static boolean tileDatePanelAlpha[][];
	public static Point datePanelPoint = new Point(0, 0);

	public static Tile tileIconCoins;
	public static Point tileIconCoinsPoint = new Point(0, 0);

	/*
	 * TOOLTIP
	 */
	public static Tile tileTooltipBackground;

	/*
	 * Typing panel
	 */
	public static TypingPanel typingPanel = null;

	/*
	 * Images panel
	 */
	public static ImagesPanel imagesPanel = null;

	// Menu Blinks
	public static boolean checkBlinkBottom = false;
	public static boolean checkBlinkRight = false;
	public static boolean checkBlinkProduction = false;

		public static boolean isMessagesPanelActive() {
		return messagesPanelActive != -1;
	}

	public static int getMessagesPanelActive() {
		return messagesPanelActive;
	}

	public static void setMessagesPanelActive(int iMessageType) {
		messagesPanelActive = iMessageType;
		if (iMessageType != -1) {
			UIPanel.closePanels(true, true, false, true, true, true, true);
		}
	}

	public static void setProductionPanelActive(boolean panelActive) {
		System.out.println("UIPanelState.setProductionPanelActive()"+panelActive);
		Exception e = new Exception();
e.printStackTrace(System.out);
		productionPanelActive = panelActive;

		ImagesPanel.resize(renderWidth, renderHeight);
	}

	public static boolean isProductionPanelActive() {
		return productionPanelActive;
	}

	public static void setProductionPanelLocked(boolean productionPanelLocked) {
		UIPanelState.productionPanelLocked = productionPanelLocked;
	}

	public static boolean isProductionPanelLocked() {
		return productionPanelLocked;
	}

	public static void setTradePanelActive(boolean tradePanelActive) {
		if (!isTradePanelActive()) {
			if (Game.getWorld().getCurrentCaravanData() != null) {
				Game.getWorld().getCurrentCaravanData().updateCaravanStatus();
			}
		}

		if (UIPanelState.tradePanelActive != tradePanelActive) {
			if (tradePanelActive) {
				// Se activa el panel
				tradePanelActivePausedBefore = Game.isPaused();
				Game.pause(false);

				// Chequeamos los items en el mundo
				if (tradePanel != null) {
					tradePanel.createTownMenu();
				}
				UIPanelState.tradePanelActive = tradePanelActive;

				Game.updateTutorialFlow(TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_TRADE, null);
			} else {
				UIPanelState.tradePanelActive = tradePanelActive; // Se pone primero pq sino el Game.resume no funcionará

				// Se desactiva el panel, quitamos la pausa si al activar el panel el juego no
				// estaba pausado
				if (!tradePanelActivePausedBefore) {
					// Si antes no había pausa la quitamos
					Game.resume(false);
				}
			}

			if (tradePanelActive) {
				UIPanel.closePanels(true, false, true, true, true, true, true);
			}
		}
	}

	public static boolean isTradePanelActive() {
		return tradePanelActive;
	}

	public static void setPrioritiesPanelActive(boolean prioritiesPanelActive) {
		UIPanelState.prioritiesPanelActive = prioritiesPanelActive;
		if (prioritiesPanelActive) {
			UIPanel.closePanels(false, true, true, true, true, true, true);
		}
	}

	public static boolean isPrioritiesPanelActive() {
		return prioritiesPanelActive;
	}

	public static void setMatsPanelActive(boolean bActive) {
		if (bActive) {
			setMatsPanelActive(matsLastGroup);
		} else {
			setMatsPanelActive(-1);
		}
	}

	public static void setMatsPanelActive(int iGroup) {
		UIPanelState.matsPanelActive = iGroup;
		if (iGroup != -1) {
			UIPanel.closePanels(true, true, true, false, true, true, true);
			matsLastGroup = iGroup;
		}
	}

	public static boolean isPilePanelActive() {
		return pilePanelPileContainerIDActive != -1;
	}

	public static boolean isProfessionsPanelActive() {
		return professionsPanelCitizenOrGroupIDActive != -1;
	}

	public static int getMatsPanelActive() {
		return matsPanelActive;
	}

	public static void setPilePanelActive(int iPileContainerID, boolean isContainer) {
		pilePanelPileContainerIDActive = iPileContainerID;
		pilePanelIsContainer = isContainer;

		if (iPileContainerID != -1) {
			UIPanel.closePanels(true, true, true, true, true, false, true);

			// Creamos el menú
			if (isContainer) {
				menuPile = Container.createContainerMenu(iPileContainerID);

				if (menuPile != null) {
					Container container = Game.getWorld().getContainer(iPileContainerID);
					pilePanelIsLocked = container.isLockedToCopy();
				}
			} else {
				menuPile = Stockpile.createPilePanelMenu(iPileContainerID);

				if (menuPile != null) {
					Stockpile pile = Stockpile.getStockpile(iPileContainerID);
					pilePanelIsLocked = pile.isLockedToCopy();
				}
			}

			if (menuPile != null) {
				// Cambiamos el tamaño de los iconos
				UIPanel.resizeIcons(menuPile, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT);

				UIPanel.resizePilePanel(menuPile);

				// Miramos las pages
				UIPanel.recheckPilePanelPages();
			}
		} else {
			menuPile = null;
		}
	}

	public static void setPilePanelIsContainer(boolean pilePanelIsContainer) {
		UIPanelState.pilePanelIsContainer = pilePanelIsContainer;
	}

	public static boolean isPilePanelIsContainer() {
		return pilePanelIsContainer;
	}

	public static void setProfessionsPanelActive(int iProfessionsCitizenOrGroupID, boolean isCitizen) {
		professionsPanelCitizenOrGroupIDActive = iProfessionsCitizenOrGroupID;
		professionsPanelIsCitizen = isCitizen;

		if (iProfessionsCitizenOrGroupID != -1) {
			UIPanel.closePanels(true, true, true, true, false, true, false);

			// Creamos el menú
			if (isCitizen) {
				menuProfessions = ActionPriorityManager.createProfessionsMenu(iProfessionsCitizenOrGroupID);
			} else {
				menuProfessions = ActionPriorityManager.createJobGroupPanelMenu(iProfessionsCitizenOrGroupID);
			}

			if (menuProfessions != null) {
				// Cambiamos el tamaño de los iconos
				UIPanel.resizeIcons(menuProfessions, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT);

				UIPanel.resizeProfessionsPanel(menuProfessions);

				// Miramos las pages
				UIPanel.recheckProfessionsPanelPages();
			}
		} else {
			menuProfessions = null;
		}
	}

	public static boolean isMatsPanelActive() {
		return matsPanelActive != -1;
	}

	public static void setLivingsPanelActive(int iType, int iSoldiersGroup, int iCitizenGroup) {
		if (iType != LIVINGS_PANEL_TYPE_NONE) {
			UIPanel.createLivingsPanel(iType, iSoldiersGroup, iCitizenGroup);
		}
		UIPanelState.livingsPanelActive = iType;
		UIPanelState.livingsPanelCitizensGroupActive = iCitizenGroup;
		UIPanelState.livingsPanelSoldiersGroupActive = iSoldiersGroup;

		if (iType != -1) {
			UIPanel.closePanels(true, true, true, true, false, true, true);
		}
	}

	public static boolean isLivingsPanelActive() {
		return livingsPanelActive != -1;
	}

	public static int getLivingsPanelActive() {
		return livingsPanelActive;
	}

}
