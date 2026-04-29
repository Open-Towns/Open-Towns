package xaos.panels;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;

import xaos.platform.lwjgl3.input.Keyboard;
import xaos.platform.lwjgl3.input.Mouse;
import org.lwjgl.opengl.GL11;

import xaos.TownsProperties;
import xaos.actions.ActionManager;
import xaos.actions.ActionManagerItem;
import xaos.actions.ActionPriorityManager;
import xaos.campaign.TutorialFlow;
import xaos.campaign.TutorialTrigger;
import xaos.data.CaravanData;
import xaos.data.CitizenGroupData;
import xaos.data.CitizenGroups;
import xaos.data.EffectData;
import xaos.data.EquippedData;
import xaos.data.EventData;
import xaos.data.GlobalEventData;
import xaos.data.HeroData;
import xaos.data.SoldierData;
import xaos.data.SoldierGroupData;
import xaos.data.SoldierGroups;
import xaos.effects.EffectManager;
import xaos.events.EventManager;
import xaos.events.EventManagerItem;
import xaos.main.Game;
import xaos.main.World;
import xaos.panels.menus.ContextMenu;
import xaos.panels.menus.SmartMenu;
import xaos.stockpiles.Stockpile;
import xaos.tasks.Task;
import xaos.tiles.Tile;
import xaos.tiles.entities.items.Container;
import xaos.tiles.entities.items.Item;
import xaos.tiles.entities.items.ItemManager;
import xaos.tiles.entities.items.ItemManagerItem;
import xaos.tiles.entities.items.military.MilitaryItem;
import xaos.tiles.entities.living.Citizen;
import xaos.tiles.entities.living.LivingEntity;
import xaos.tiles.entities.living.heroes.Hero;
import xaos.utils.ColorGL;
import xaos.utils.Log;
import xaos.utils.Messages;
import xaos.utils.Point3D;
import xaos.utils.UtilFont;
import xaos.utils.UtilsAL;
import xaos.utils.UtilsGL;
import xaos.utils.UtilsIniHeaders;
import xaos.utils.UtilsKeyboard;


public final class UIPanel {

	public static int PIXELS_TO_BORDER = 16;
	private static final int CLOSE_PIXELS = 20;
	public static final int MAX_BLINK_TURNS = Game.FPS_INGAME;

	private static Tile BACK_TILE = new Tile ("ui_back"); //$NON-NLS-1$
	public static Tile BLACK_TILE = new Tile ("ui_black"); //$NON-NLS-1$
	public static Tile BIG_RED_CROSS_TILE = new Tile ("iconredcross"); //$NON-NLS-1$
	public static Tile ENABLE_ALL_TILE = new Tile ("iconenableall"); //$NON-NLS-1$
	public static Tile DISABLE_ALL_TILE = new Tile ("icondisableall"); //$NON-NLS-1$

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

	public static Point MOUSE_PRODUCTION_PANEL_ITEMS_POINT = new Point (MOUSE_PRODUCTION_PANEL_ITEMS, -1);
	public static Point MOUSE_PRODUCTION_PANEL_ITEMS_PLUS_REGULAR_POINT = new Point (MOUSE_PRODUCTION_PANEL_ITEMS_PLUS_REGULAR, -1);
	public static Point MOUSE_PRODUCTION_PANEL_ITEMS_MINUS_REGULAR_POINT = new Point (MOUSE_PRODUCTION_PANEL_ITEMS_MINUS_REGULAR, -1);
	public static Point MOUSE_PRODUCTION_PANEL_ITEMS_PLUS_AUTOMATED_POINT = new Point (MOUSE_PRODUCTION_PANEL_ITEMS_PLUS_AUTOMATED, -1);
	public static Point MOUSE_PRODUCTION_PANEL_ITEMS_MINUS_AUTOMATED_POINT = new Point (MOUSE_PRODUCTION_PANEL_ITEMS_MINUS_AUTOMATED, -1);

	public static Point MOUSE_PRIORITIES_PANEL_ITEMS_POINT = new Point (MOUSE_PRIORITIES_PANEL_ITEMS, -1);
	public static Point MOUSE_PRIORITIES_PANEL_ITEMS_UP_POINT = new Point (MOUSE_PRIORITIES_PANEL_ITEMS_UP, -1);
	public static Point MOUSE_PRIORITIES_PANEL_ITEMS_DOWN_POINT = new Point (MOUSE_PRIORITIES_PANEL_ITEMS_DOWN, -1);

	public static Point MOUSE_TRADE_PANEL_BUTTONS_CARAVAN_POINT = new Point (MOUSE_TRADE_PANEL_BUTTONS_CARAVAN, -1);
	public static Point MOUSE_TRADE_PANEL_BUTTONS_CARAVAN_UP_POINT = new Point (MOUSE_TRADE_PANEL_BUTTONS_UP_CARAVAN, -1);
	public static Point MOUSE_TRADE_PANEL_BUTTONS_CARAVAN_DOWN_POINT = new Point (MOUSE_TRADE_PANEL_BUTTONS_DOWN_CARAVAN, -1);
	public static Point MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_CARAVAN_POINT = new Point (MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_CARAVAN, -1);
	public static Point MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_CARAVAN_UP_POINT = new Point (MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_UP_CARAVAN, -1);
	public static Point MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_CARAVAN_DOWN_POINT = new Point (MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_DOWN_CARAVAN, -1);
	public static Point MOUSE_TRADE_PANEL_BUTTONS_CONFIRM_POINT = new Point (MOUSE_TRADE_PANEL_BUTTONS_CONFIRM, -1);
	public static Point MOUSE_TRADE_PANEL_BUTTONS_TOWN_POINT = new Point (MOUSE_TRADE_PANEL_BUTTONS_TOWN, -1);
	public static Point MOUSE_TRADE_PANEL_BUTTONS_TOWN_UP_POINT = new Point (MOUSE_TRADE_PANEL_BUTTONS_UP_TOWN, -1);
	public static Point MOUSE_TRADE_PANEL_BUTTONS_TOWN_DOWN_POINT = new Point (MOUSE_TRADE_PANEL_BUTTONS_DOWN_TOWN, -1);
	public static Point MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_TOWN_POINT = new Point (MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_TOWN, -1);
	public static Point MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_TOWN_UP_POINT = new Point (MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_UP_TOWN, -1);
	public static Point MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_TOWN_DOWN_POINT = new Point (MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_DOWN_TOWN, -1);
	public static Point MOUSE_TRADE_PANEL_BUTTONS_CLOSE_POINT = new Point (MOUSE_TRADE_PANEL_BUTTONS_CLOSE, -1);
	public static Point MOUSE_TRADE_PANEL_ICON_BUY_POINT = new Point (MOUSE_TRADE_PANEL_ICON_BUY, -1);
	public static Point MOUSE_TRADE_PANEL_ICON_SELL_POINT = new Point (MOUSE_TRADE_PANEL_ICON_SELL, -1);

	public static Point MOUSE_MESSAGES_PANEL_BUTTONS_CLOSE_POINT = new Point (MOUSE_MESSAGES_PANEL_BUTTONS_CLOSE, -1);
	public static Point MOUSE_MESSAGES_PANEL_BUTTONS_ANNOUNCEMENT_POINT = new Point (MOUSE_MESSAGES_PANEL_BUTTONS_ANNOUNCEMENT, -1);
	public static Point MOUSE_MESSAGES_PANEL_BUTTONS_COMBAT_POINT = new Point (MOUSE_MESSAGES_PANEL_BUTTONS_COMBAT, -1);
	public static Point MOUSE_MESSAGES_PANEL_BUTTONS_HEROES_POINT = new Point (MOUSE_MESSAGES_PANEL_BUTTONS_HEROES, -1);
	public static Point MOUSE_MESSAGES_PANEL_BUTTONS_SYSTEM_POINT = new Point (MOUSE_MESSAGES_PANEL_BUTTONS_SYSTEM, -1);
	public static Point MOUSE_MESSAGES_PANEL_BUTTONS_SCROLL_UP_POINT = new Point (MOUSE_MESSAGES_PANEL_BUTTONS_SCROLL_UP, -1);
	public static Point MOUSE_MESSAGES_PANEL_BUTTONS_SCROLL_DOWN_POINT = new Point (MOUSE_MESSAGES_PANEL_BUTTONS_SCROLL_DOWN, -1);

	public static Point MOUSE_MATS_PANEL_BUTTONS_CLOSE_POINT = new Point (MOUSE_MATS_PANEL_BUTTONS_CLOSE, -1);
	public static Point MOUSE_MATS_PANEL_BUTTONS_GROUPS_POINT = new Point (MOUSE_MATS_PANEL_BUTTONS_GROUPS, -1);
	public static Point MOUSE_MATS_PANEL_BUTTONS_ITEMS_POINT = new Point (MOUSE_MATS_PANEL_BUTTONS_ITEMS, -1);
	public static Point MOUSE_MATS_PANEL_BUTTONS_SCROLL_UP_POINT = new Point (MOUSE_MATS_PANEL_BUTTONS_SCROLL_UP, -1);
	public static Point MOUSE_MATS_PANEL_BUTTONS_SCROLL_DOWN_POINT = new Point (MOUSE_MATS_PANEL_BUTTONS_SCROLL_DOWN, -1);

	public static Point MOUSE_PILE_PANEL_BUTTONS_CLOSE_POINT = new Point (MOUSE_PILE_PANEL_BUTTONS_CLOSE, -1);
	public static Point MOUSE_PILE_PANEL_BUTTONS_ITEMS_POINT = new Point (MOUSE_PILE_PANEL_BUTTONS_ITEMS, -1);
	public static Point MOUSE_PILE_PANEL_BUTTONS_SCROLL_UP_POINT = new Point (MOUSE_PILE_PANEL_BUTTONS_SCROLL_UP, -1);
	public static Point MOUSE_PILE_PANEL_BUTTONS_SCROLL_DOWN_POINT = new Point (MOUSE_PILE_PANEL_BUTTONS_SCROLL_DOWN, -1);
	public static Point MOUSE_PILE_PANEL_BUTTONS_CONFIG_COPY_POINT = new Point (MOUSE_PILE_PANEL_BUTTONS_CONFIG_COPY, -1);
	public static Point MOUSE_PILE_PANEL_BUTTONS_CONFIG_LOCK_POINT = new Point (MOUSE_PILE_PANEL_BUTTONS_CONFIG_LOCK, -1);
	public static Point MOUSE_PILE_PANEL_BUTTONS_CONFIG_LOCK_ALL_POINT = new Point (MOUSE_PILE_PANEL_BUTTONS_CONFIG_LOCK_ALL, -1);
	public static Point MOUSE_PILE_PANEL_BUTTONS_CONFIG_UNLOCK_ALL_POINT = new Point (MOUSE_PILE_PANEL_BUTTONS_CONFIG_UNLOCK_ALL, -1);

	public static Point MOUSE_PROFESSIONS_PANEL_BUTTONS_CLOSE_POINT = new Point (MOUSE_PROFESSIONS_PANEL_BUTTONS_CLOSE, -1);
	public static Point MOUSE_PROFESSIONS_PANEL_BUTTONS_ITEMS_POINT = new Point (MOUSE_PROFESSIONS_PANEL_BUTTONS_ITEMS, -1);
	public static Point MOUSE_PROFESSIONS_PANEL_BUTTONS_SCROLL_UP_POINT = new Point (MOUSE_PROFESSIONS_PANEL_BUTTONS_SCROLL_UP, -1);
	public static Point MOUSE_PROFESSIONS_PANEL_BUTTONS_SCROLL_DOWN_POINT = new Point (MOUSE_PROFESSIONS_PANEL_BUTTONS_SCROLL_DOWN, -1);

	public static Point MOUSE_LIVINGS_PANEL_BUTTONS_CLOSE_POINT = new Point (MOUSE_LIVINGS_PANEL_BUTTONS_CLOSE, -1);
	public static Point MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_POINT = new Point (MOUSE_LIVINGS_PANEL_BUTTONS_ROWS, -1);
	public static Point MOUSE_LIVINGS_PANEL_BUTTONS_SCROLL_UP_POINT = new Point (MOUSE_LIVINGS_PANEL_BUTTONS_SCROLL_UP, -1);
	public static Point MOUSE_LIVINGS_PANEL_BUTTONS_SCROLL_DOWN_POINT = new Point (MOUSE_LIVINGS_PANEL_BUTTONS_SCROLL_DOWN, -1);
	public static Point MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_HEAD_POINT = new Point (MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_HEAD, -1);
	public static Point MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_BODY_POINT = new Point (MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_BODY, -1);
	public static Point MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_LEGS_POINT = new Point (MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_LEGS, -1);
	public static Point MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_FEET_POINT = new Point (MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_FEET, -1);
	public static Point MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_WEAPON_POINT = new Point (MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_WEAPON, -1);
	public static Point MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_AUTOEQUIP_POINT = new Point (MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_AUTOEQUIP, -1);
	public static Point MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_PROFESSIONS_POINT = new Point (MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_PROFESSIONS, -1);
	public static Point MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_JOBS_GROUPS_ADDREMOVE_POINT = new Point (MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_JOBS_GROUPS_ADDREMOVE, -1);
	public static Point MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_POINT = new Point (MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER, -1);
	public static Point MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_CIVILIAN_POINT = new Point (MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_CIVILIAN, -1);
	public static Point MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_GUARD_POINT = new Point (MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_GUARD, -1);
	public static Point MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_PATROL_POINT = new Point (MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_PATROL, -1);
	public static Point MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_BOSS_POINT = new Point (MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_BOSS, -1);
	public static Point MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_SGROUP_ADD_POINT = new Point (MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_SGROUP_ADD, -1);
	public static Point MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_SGROUP_REMOVE_POINT = new Point (MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_SGROUP_REMOVE, -1);
	public static Point MOUSE_LIVINGS_PANEL_SGROUP_NOGROUP_POINT = new Point (MOUSE_LIVINGS_PANEL_SGROUP_NOGROUP, -1);
	public static Point MOUSE_LIVINGS_PANEL_SGROUP_GROUP_POINT = new Point (MOUSE_LIVINGS_PANEL_SGROUP_GROUP, -1);
	public static Point MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_RENAME_POINT = new Point (MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_RENAME, -1);
	public static Point MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_GUARD_POINT = new Point (MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_GUARD, -1);
	public static Point MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_PATROL_POINT = new Point (MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_PATROL, -1);
	public static Point MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_BOSS_POINT = new Point (MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_BOSS, -1);
	public static Point MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_AUTOEQUIP_POINT = new Point (MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_AUTOEQUIP, -1);
	public static Point MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_DISBAND_POINT = new Point (MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_DISBAND, -1);
	public static Point MOUSE_LIVINGS_PANEL_CGROUP_NOGROUP_POINT = new Point (MOUSE_LIVINGS_PANEL_CGROUP_NOGROUP, -1);
	public static Point MOUSE_LIVINGS_PANEL_CGROUP_GROUP_POINT = new Point (MOUSE_LIVINGS_PANEL_CGROUP_GROUP, -1);
	public static Point MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_RENAME_POINT = new Point (MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_RENAME, -1);
	public static Point MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_AUTOEQUIP_POINT = new Point (MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_AUTOEQUIP, -1);
	public static Point MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_DISBAND_POINT = new Point (MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_DISBAND, -1);
	public static Point MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_CHANGE_JOBS_POINT = new Point (MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_CHANGE_JOBS, -1);
	public static Point MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_UP_POINT = new Point (MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_UP, -1);
	public static Point MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_DOWN_POINT = new Point (MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_DOWN, -1);

	/*
	 * BOTTOM PANEL
	 */
	private final static int BOTTOM_PANEL_SCROLL_WIDTH = 32;
	private final static int BOTTOM_PANEL_WIDTH = 1024 - (BOTTOM_PANEL_SCROLL_WIDTH * 2);
	private final static int BOTTOM_PANEL_HEIGHT = 64;
	private final static int BOTTOM_PANEL_NUM_ITEMS = 10;
	private final static int BOTTOM_ITEM_WIDTH = 64;
	private final static int BOTTOM_ITEM_HEIGHT = 64;

	private static int BOTTOM_SUBPANEL_WIDTH;
	private static int BOTTOM_SUBPANEL_HEIGHT;
	private static int BOTTOM_SUBPANEL_NUM_ITEMS_X;
	private static int BOTTOM_SUBPANEL_NUM_ITEMS_Y;
	private static int BOTTOM_SUBITEM_WIDTH = 64;
	private static int BOTTOM_SUBITEM_HEIGHT = 64;

	/*
	 * PRODUCTION PANEL
	 */
	private final static int PRODUCTION_PANEL_ITEM_WIDTH = 64;
	private final static int PRODUCTION_PANEL_ITEM_HEIGHT = 64;
	private static int PRODUCTION_PANEL_NUM_ITEMS_X;
	private static int PRODUCTION_PANEL_NUM_ITEMS_Y;
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
	private final static int PRIORITIES_PANEL_ITEM_SIZE = 64;
	private static int PRIORITIES_PANEL_NUM_ITEMS;
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
	private static int MINIMAP_PANEL_WIDTH = World.MAP_WIDTH;
	private static int MINIMAP_PANEL_HEIGHT = World.MAP_HEIGHT;

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
	private final static int MENU_ITEM_WIDTH = 64;
	private final static int MENU_ITEM_HEIGHT = 64;
	private static int MENU_PANEL_NUM_ITEMS_X;
	private static int MENU_PANEL_NUM_ITEMS_Y;
	private static int MENU_PANEL_WIDTH;
	private static int MENU_PANEL_HEIGHT;

	/*
	 * MINI-ICONS
	 */
	private final static int ICON_WIDTH = 32;
	private final static int ICON_HEIGHT = 32;

	private static int delayTime;
	public static int blinkTurns;
	private static SmartMenu currentMenu;
	private static boolean bottomMenuPanelActive = true;
	private static boolean bottomMenuPanelLocked = true;

	public static int renderWidth;
	public static int renderHeight;

	// BOTTOM panel
	private static ArrayList<Point> bottomPanelItemsPosition; // Array de sólo BOTTOM_PANEL_NUM_ITEMS posiciones (9) con las coordenadas de los items que caben
	private static int bottomPanelItemIndex;
	private static int bottomPanelX;
	private static int bottomPanelY;
	private static int bottomPanelLeftScrollX;
	private static int bottomPanelRightScrollX;

	private static Tile tileBottomItem;
	private static Tile tileBottomItemSM;
	private static Tile tileBottomScrollLeft;
	private static Tile tileBottomScrollLeftON;
	private static Tile tileBottomScrollRight;
	private static Tile tileBottomScrollRightON;
	private static Tile tileBottomPanel;
	private static boolean tileBottomItemAlpha[][];
	private static boolean tileBottomScrollLeftAlpha[][];
	private static boolean tileBottomScrollRightAlpha[][];
	private static boolean tileBottomPanelAlpha[][];

	// Open/close bottom
	private static Tile tileOpenBottomMenu;
	private static boolean tileOpenBottomMenuAlpha[][];
	private static Tile tileOpenBottomMenuON;
	private static boolean tileOpenBottomMenuONAlpha[][];
	private static Point tileOpenCloseBottomMenuPoint = new Point (0, 0);

	// BOTTOM subpanel
	private static ArrayList<Point> bottomSubPanelItemsPosition; // Array de BOTTOM_SUBPANEL_NUM_ITEMS_X x BOTTOM_SUBPANEL_NUM_ITEMS_Y posiciones con las coordenadas de los subitems
	private static Point bottomSubPanelPoint = new Point (0, 0);
	private static Tile tileBottomSubItem;
	private static Tile[] tileBottomSubPanel;
	private static SmartMenu bottomSubPanelMenu;

	private static boolean tileBottomSubItemAlpha[][];

	// MINIMAP panel
	private int minimapPanelX;
	private int minimapPanelY;

	private static Tile tileMinimapPanel;
	private static boolean tileMinimapPanelAlpha[][];

	// PRODUCTION panel
	private static Tile[] tileProductionPanel;
	private static Point productionPanelPoint = new Point (0, 0);
	private static boolean productionPanelActive = false;
	private static boolean productionPanelLocked = false;

	private static SmartMenu productionPanelMenu;
	private static ArrayList<Point> productionPanelItemsPosition = new ArrayList<Point> ();
	private static Tile tileProductionPanelPlusIcon;
	private static boolean tileProductionPanelPlusIconAlpha[][];
	private static ArrayList<Point> productionPanelItemsPlusRegularPosition = new ArrayList<Point> ();
	private static ArrayList<Point> productionPanelItemsPlusAutomatedPosition = new ArrayList<Point> ();
	private static Tile tileProductionPanelMinusIcon;
	private static boolean tileProductionPanelMinusIconAlpha[][];
	private static ArrayList<Point> productionPanelItemsMinusRegularPosition = new ArrayList<Point> ();
	private static ArrayList<Point> productionPanelItemsMinusAutomatedPosition = new ArrayList<Point> ();

	// Open/close production
	private static Tile tileOpenProductionPanel;
	private static boolean tileOpenProductionPanelAlpha[][];
	private static Tile tileOpenProductionPanelON;
	private static boolean tileOpenProductionPanelONAlpha[][];
	private static Point tileOpenCloseProductionPanelPoint = new Point (0, 0);

	// TRADE panel
	private static Tile[] tileTradePanel;
	public static Point tradePanelPoint = new Point (0, 0);
	public static Point tradePanelClosePoint = new Point (0, 0);
	private static TradePanel tradePanel;
	private static boolean tradePanelActive = false;
	private static boolean tradePanelActivePausedBefore = false;
	public static Tile tileScrollUp = new Tile ("scrollup"); //$NON-NLS-1$
	public static Tile tileScrollUpDisabled = new Tile ("scrollup_disabled"); //$NON-NLS-1$
	public static final boolean tileScrollUpButtonAlpha[][] = UtilsGL.generateAlpha (tileScrollUp);
	public static Tile tileScrollDown = new Tile ("scrolldown"); //$NON-NLS-1$
	public static Tile tileScrollDownDisabled = new Tile ("scrolldown_disabled"); //$NON-NLS-1$
	public static final boolean tileScrollDownButtonAlpha[][] = UtilsGL.generateAlpha (tileScrollDown);

	// PRIORITIES panel
	private static Tile[] tilePrioritiesPanel;
	private static Point prioritiesPanelPoint = new Point (0, 0);
	private static boolean prioritiesPanelActive = false;

	private static ArrayList<Point> prioritiesPanelItemsPosition;
	private static Tile tilePrioritiesPanelUpIcon;
	private static boolean tilePrioritiesPanelUpIconAlpha[][];
	private static ArrayList<Point> prioritiesPanelItemsUpPosition;
	private static Tile tilePrioritiesPanelDownIcon;
	private static boolean tilePrioritiesPanelDownIconAlpha[][];
	private static ArrayList<Point> prioritiesPanelItemsDownPosition;

	// MATS panel
	private static Tile[] tileMatsPanel;
	private static Tile[] tileMatsPanelSubPanel;
	private static Point matsPanelPoint = new Point (0, 0);
	private static int matsPanelActive = -1;
	public static Point matsPanelClosePoint = new Point (0, 0);
	private static Point matsPanelIconScrollUpPoint = new Point (0, 0);
	private static Point matsPanelIconScrollDownPoint = new Point (0, 0);
	private static Tile[] matsPanelTiles;
	private static Tile[] matsPanelTilesON;
	public static Point matsPanelSubPanelPoint = new Point (0, 0);
	private static Point[] matsPanelIconPoints;
	private static Point[] matsPanelItemPoints;
	private static Point matsPanelPagesPositionPoint = new Point (0, 0);
	private static int[] matsNumPages;
	private static int[] matsIndexPages;
	private static int matsLastGroup = -1;

	// PILE panel
	private static Point pilePanelPoint = new Point (0, 0);
	private static int pilePanelPileContainerIDActive = -1;
	private static boolean pilePanelIsContainer = false;
	private static boolean pilePanelIsLocked = false;
	public static Point pilePanelClosePoint = new Point (0, 0);
	private static Point pilePanelIconScrollUpPoint = new Point (0, 0);
	private static Point pilePanelIconScrollDownPoint = new Point (0, 0);
	private static Point pilePanelIconConfigCopyPoint = new Point (0, 0);
	private static Point pilePanelIconConfigLockPoint = new Point (0, 0);
	private static Point pilePanelIconConfigUnlockAllPoint = new Point (0, 0);
	private static Point pilePanelIconConfigLockAllPoint = new Point (0, 0);
	private static Point[] pilePanelItemPoints;
	private static Point pilePanelPagesPositionPoint = new Point (0, 0);
	private static SmartMenu menuPile = null;
	private static int pilePanelPageIndex = -1;
	private static int pilePanelMaxPages = -1;
	public static Tile tileConfigCopy = new Tile ("configcopy"); //$NON-NLS-1$
	public static final boolean tileConfigCopyButtonAlpha[][] = UtilsGL.generateAlpha (tileConfigCopy);
	public static Tile tileConfigLock = new Tile ("configlock"); //$NON-NLS-1$
	public static final boolean tileConfigLockButtonAlpha[][] = UtilsGL.generateAlpha (tileConfigLock);
	public static Tile tileConfigLockLocked = new Tile ("configlocklocked"); //$NON-NLS-1$
	public static final boolean tileConfigLockLockedButtonAlpha[][] = UtilsGL.generateAlpha (tileConfigLockLocked);
	public static Tile tileConfigLockAll = new Tile ("configlockall"); //$NON-NLS-1$
	public static final boolean tileConfigLockAllButtonAlpha[][] = UtilsGL.generateAlpha (tileConfigLockAll);
	public static Tile tileConfigUnlockAll = new Tile ("configunlockall"); //$NON-NLS-1$
	public static final boolean tileConfigUnlockAllButtonAlpha[][] = UtilsGL.generateAlpha (tileConfigUnlockAll);

	// PROFESSIONS panel
	private static Point professionsPanelPoint = new Point (0, 0);
	private static int professionsPanelCitizenOrGroupIDActive = -1;
	private static boolean professionsPanelIsCitizen = true;
	public static Point professionsPanelClosePoint = new Point (0, 0);
	private static Point professionsPanelIconScrollUpPoint = new Point (0, 0);
	private static Point professionsPanelIconScrollDownPoint = new Point (0, 0);
	private static Point[] professionsPanelItemPoints;
	private static Point professionsPanelPagesPositionPoint = new Point (0, 0);
	private static SmartMenu menuProfessions = null;
	private static int professionsPanelPageIndex = -1;
	private static int professionsPanelMaxPages = -1;

	// LIVINGS PANEL
	private static Tile[] tileLivingsPanel;
	private static Point livingsPanelPoint = new Point (0, 0);
	private static int livingsPanelActive = LIVINGS_PANEL_TYPE_NONE;
	private static int livingsPanelCitizensGroupActive = -1;
	private static int livingsPanelSoldiersGroupActive = -1;
	public static Point livingsPanelClosePoint = new Point (0, 0);
	private static Point livingsPanelIconScrollUpPoint = new Point (0, 0);
	private static Point livingsPanelIconScrollDownPoint = new Point (0, 0);
	private static Point livingsPanelPagesPoint = new Point (0, 0);
	private static Point[] livingsPanelRowPoints;
	private static Point[] livingsPanelRowHeadPoints;
	private static Point[] livingsPanelRowBodyPoints;
	private static Point[] livingsPanelRowLegsPoints;
	private static Point[] livingsPanelRowFeetPoints;
	private static Point[] livingsPanelRowWeaponPoints;
	private static Tile tileLivingsPanelRowNoHead;
	private static Tile tileLivingsPanelRowNoBody;
	private static Tile tileLivingsPanelRowNoLegs;
	private static Tile tileLivingsPanelRowNoFeet;
	private static Tile tileLivingsPanelRowNoWeapon;
	private static Point[] livingsPanelRowAutoequipPoints;
	private static Tile tileLivingsRowAutoequip;
	private static Tile tileLivingsRowAutoequipON;
	private static boolean tileLivingsRowAutoequipAlpha[][];
	private static Point[] livingsPanelRowProfessionPoints;
	private static Point[] livingsPanelRowJobsGroupsPoints;
	private static Point[] livingsPanelRowConvertCivilianSoldierPoints;
	private static Point[] livingsPanelRowConvertSoldierGuardPoints;
	private static Point[] livingsPanelRowConvertSoldierPatrolPoints;
	private static Point[] livingsPanelRowConvertSoldierBossPoints;
	private static Tile tileLivingsRowProfession;
	private static Tile tileLivingsRowJobsGroups;
	private static Tile tileLivingsRowProfessionON;
	private static Tile tileLivingsRowJobsGroupsON;
	private static Tile tileLivingsRowConvertSoldier;
	private static Tile tileLivingsRowConvertSoldierON;
	private static Tile tileLivingsRowConvertCivilian;
	private static Tile tileLivingsRowConvertCivilianON;
	private static Tile tileLivingsRowConvertSoldierGuard;
	private static Tile tileLivingsRowConvertSoldierGuardON;
	private static Tile tileLivingsRowConvertSoldierPatrol;
	private static Tile tileLivingsRowConvertSoldierPatrolON;
	private static Tile tileLivingsRowConvertSoldierBoss;
	private static Tile tileLivingsRowConvertSoldierBossON;
	private static boolean tileLivingsRowProfessionAlpha[][];
	private static boolean tileLivingsRowJobsGroupsAlpha[][];
	private static boolean tileLivingsRowConvertSoldierAlpha[][];
	private static boolean tileLivingsRowConvertCivilianAlpha[][];
	private static boolean tileLivingsRowConvertSoldierGuardAlpha[][];
	private static boolean tileLivingsRowConvertSoldierPatrolAlpha[][];
	private static boolean tileLivingsRowConvertSoldierBossAlpha[][];

	private static Point[] livingsPanelRowGroupPoints;
	private static Tile tileLivingsRowGroupAdd;
	private static Tile tileLivingsRowGroupAddON;
	private static boolean tileLivingsRowGroupAddAlpha[][];
	private static Tile tileLivingsRowGroupRemove;
	private static Tile tileLivingsRowGroupRemoveON;
	private static boolean tileLivingsRowGroupRemoveAlpha[][];

	private static Point livingsPanelIconRestrictUpPoint = new Point (0, 0);
	private static Point livingsPanelIconRestrictDownPoint = new Point (0, 0);

	// LIVINGS GROUP PANEL data
	private static Tile[] tileLivingsGroupPanel;
	private static Point livingsGroupPanelPoint = new Point (0, 0);
	private static Point livingsSingleGroupPanelPoint = new Point (0, 0);
	private static Point livingsGroupPanelFirstIconPoint = new Point (0, 0);
	private static int livingsGroupPanelIconsSeparation = Tile.TERRAIN_ICON_WIDTH; // Esto se cambiará seguro, no tiene nada que ver, es por si acaso
	private static Tile tileLivingsGroup;
	private static Tile tileLivingsGroupON;
	private static Tile tileLivingsGroupGreen;
	private static boolean tileLivingsGroupAlpha[][];
	private static Tile tileLivingsNoGroup;
	private static Tile tileLivingsNoGroupON;
	private static Tile tileLivingsNoGroupGreen;
	private static boolean tileLivingsNoGroupAlpha[][];
	private static Tile tileLivingsNoJobGroup;
	private static Tile tileLivingsNoJobGroupON;
	private static Tile tileLivingsNoJobGroupGreen;
	private static boolean tileLivingsNoJobGroupAlpha[][];
	private static Tile tileLivingsJobGroup;
	private static Tile tileLivingsJobGroupON;
	private static Tile tileLivingsJobGroupGreen;
	private static boolean tileLivingsJobGroupAlpha[][];

	private static Point livingsSingleGroupRenamePoint = new Point (0, 0);
	private static Tile tileLivingsSingleGroupRename;
	private static Tile tileLivingsSingleGroupRenameON;
	private static boolean tileLivingsSingleGroupRenameAlpha[][];
	private static Tile tileLivingsSingleJobGroupRename;
	private static Tile tileLivingsSingleJobGroupRenameON;
	private static boolean tileLivingsSingleJobGroupRenameAlpha[][];
	private static Point livingsSingleGroupGuardPoint = new Point (0, 0);
	private static Tile tileLivingsSingleGroupGuard;
	private static Tile tileLivingsSingleGroupGuardON;
	private static boolean tileLivingsSingleGroupGuardAlpha[][];
	private static Point livingsSingleGroupPatrolPoint = new Point (0, 0);
	private static Tile tileLivingsSingleGroupPatrol;
	private static Tile tileLivingsSingleGroupPatrolON;
	private static boolean tileLivingsSingleGroupPatrolAlpha[][];
	private static Point livingsSingleGroupBossPoint = new Point (0, 0);
	private static Tile tileLivingsSingleGroupBoss;
	private static Tile tileLivingsSingleGroupBossON;
	private static boolean tileLivingsSingleGroupBossAlpha[][];
	private static Point livingsSingleGroupDisbandPoint = new Point (0, 0);
	private static Tile tileLivingsSingleGroupDisband;
	private static Tile tileLivingsSingleGroupDisbandON;
	private static boolean tileLivingsSingleGroupDisbandAlpha[][];
	private static Tile tileLivingsSingleJobGroupDisband;
	private static Tile tileLivingsSingleJobGroupDisbandON;
	private static boolean tileLivingsSingleJobGroupDisbandAlpha[][];
	private static Point livingsSingleGroupAutoequipPoint = new Point (0, 0);
	private static Point livingsSingleGroupChangeJobsPoint = new Point (0, 0);
	private static Tile tileLivingsSingleGroupChangeJobs;
	private static Tile tileLivingsSingleGroupChangeJobsON;
	private static boolean tileLivingsSingleGroupChangeJobsAlpha[][];

	// LIVINGS PANEL data
	private static int[] livingsDataIndexPages;
	private static int[] livingsDataIndexPagesCitizenGroups;
	private static int[] livingsDataIndexPagesSoldierGroups;

	// MESSAGES panel
	private static int messagesPanelActive = -1;

	private static Point[] messageIconPoints;
	private static Point[] messagePanelIconPoints;
	private static Tile[] messageTiles;
	private static Tile[] messageTilesON;
	private static ArrayList<boolean[][]> messageTilesAlpha;
	private static Tile[] messagePanelTiles;
	private static Tile[] messagePanelTilesON;
	private static ArrayList<boolean[][]> messagePanelTilesAlpha;
	public static Point messagesPanelPoint = new Point (0, 0);
	public static Point messagesPanelSubPanelPoint = new Point (0, 0);
	public static Tile[] tileMessagesPanel;
	private static Tile[] tileMessagesPanelSubPanel;
	public static Point messagesPanelClosePoint = new Point (0, 0);
	private static Point messagePanelIconScrollUpPoint = new Point (0, 0);
	private static Point messagePanelIconScrollDownPoint = new Point (0, 0);
	private static Point messagePanelPagesPositionPoint = new Point (0, 0);

	// MENU panel (right)
	private static Point menuPanelPoint = new Point (0, 0);
	private static ArrayList<Point> menuPanelItemsPosition;

	private static Tile[] tileMenuPanel;

	private static SmartMenu menuPanelMenu = null;
	private static boolean menuPanelActive = false;
	private static boolean menuPanelLocked = false;

	// Open/close right menu
	private static Tile tileOpenRightMenu;
	private static boolean tileOpenRightMenuAlpha[][];
	private static Tile tileOpenRightMenuON;
	private static boolean tileOpenRightMenuONAlpha[][];
	private static Point tileOpenCloseRightMenuPoint = new Point (0, 0);

	// Close panels
	public static Tile tileButtonClose = new Tile ("panel_close"); //$NON-NLS-1$
	public static Tile tileButtonCloseDisabled = new Tile ("panel_close_disabled"); //$NON-NLS-1$
	public static final boolean tileButtonCloseAlpha[][] = UtilsGL.generateAlpha (tileButtonClose);

	// MINI-ICONS
	private static boolean tileIconNextMiniAlpha[][];
	private static boolean tileIconPreviousMiniAlpha[][];
	private static Point iconLevelUpPoint = new Point (0, 0);
	private static Tile tileIconLevelUp;
	private static boolean tileIconLevelUpAlpha[][];
	private static Point iconLevelDownPoint = new Point (0, 0);
	private static Tile tileIconLevelDown;
	private static boolean tileIconLevelDownAlpha[][];
	private static Point iconLevelPoint = new Point (0, 0);
	private static Tile tileIconLevel;
	private static boolean tileIconLevelAlpha[][];

	// ICONS
	private static Tile tileIconPriorities;
	private static Tile tileIconPrioritiesON;
	private static boolean tileIconPrioritiesAlpha[][];
	private static Point iconPrioritiesPoint = new Point (0, 0);
	private static Tile tileIconMats;
	private static Tile tileIconMatsON;
	private static boolean tileIconMatsAlpha[][];
	private static Point iconMatsPoint = new Point (0, 0);
	private static Tile tileIconPause;
	private static boolean tileIconPauseResumeAlpha[][];
	private static Tile tileIconResume;
	private static Point iconPauseResumePoint = new Point (0, 0);
	private static Tile tileIconIncreaseSpeed;
	private static Tile tileIconIncreaseSpeedON;
	private static boolean tileIconIncreaseSpeedAlpha[][];
	private static Point iconIncreaseSpeedPoint = new Point (0, 0);
	private static Tile tileIconLowerSpeed;
	private static Tile tileIconLowerSpeedON;
	private static boolean tileIconLowerSpeedAlpha[][];
	private static Point iconLowerSpeedPoint = new Point (0, 0);
	private static Tile tileIconSettings;
	private static boolean tileIconSettingsAlpha[][];
	private static Point iconSettingsPoint = new Point (0, 0);
	private static Tile tileIconGrid;
	private static Tile tileIconGridON;
	private static boolean tileIconGridAlpha[][];
	private static Point iconGridPoint = new Point (0, 0);
	private static Tile tileIconMiniblocks;
	private static Tile tileIconMiniblocksON;
	private static boolean tileIconMiniblocksAlpha[][];
	private static Point iconMiniblocksPoint = new Point (0, 0);
	private static Tile tileIconFlatMouse;
	private static Tile tileIconFlatMouseON;
	private static boolean tileIconFlatMouseAlpha[][];
	private static Point iconFlatMousePoint = new Point (0, 0);
	private static Tile tileIcon3DMouse;
	private static Tile tileIcon3DMouseON;
	private static boolean tileIcon3DMouseAlpha[][];
	private static Point icon3DMousePoint = new Point (0, 0);

	private static Point iconEventsPoint = new Point (0, 0);
	// private static Point iconGodsPoint = new Point (0, 0);
	// private static Tile tileIconGods;
	private static Point iconTutorialPoint = new Point (0, 0);
	public static Tile tileIconTutorial;

	// INFO
	private static Tile tileIconNumCitizens;
	private static Point iconNumCitizensBackgroundPoint = new Point (0, 0);
	private static Point iconNumCitizensPoint = new Point (0, 0);
	private static Point iconCitizenNextPoint = new Point (0, 0);
	private static Tile tileIconCitizenNext;
	private static Tile tileIconCitizenNextON;
	private static Point iconCitizenPreviousPoint = new Point (0, 0);
	private static Tile tileIconCitizenPrevious;
	private static Tile tileIconCitizenPreviousON;

	private static Tile tileIconNumSoldiers;
	private static Point iconNumSoldiersBackgroundPoint = new Point (0, 0);
	private static Point iconNumSoldiersPoint = new Point (0, 0);
	private static Point iconSoldierNextPoint = new Point (0, 0);
	private static Tile tileIconSoldierNext;
	private static Tile tileIconSoldierNextON;
	private static Point iconSoldierPreviousPoint = new Point (0, 0);
	private static Tile tileIconSoldierPrevious;
	private static Tile tileIconSoldierPreviousON;

	private static Tile tileIconNumHeroes;
	private static Point iconNumHeroesBackgroundPoint = new Point (0, 0);
	private static Point iconNumHeroesPoint = new Point (0, 0);
	private static Point iconHeroNextPoint = new Point (0, 0);
	private static Tile tileIconHeroNext;
	private static Tile tileIconHeroNextON;
	private static Point iconHeroPreviousPoint = new Point (0, 0);
	private static Tile tileIconHeroPrevious;
	private static Tile tileIconHeroPreviousON;

	private static Tile tileIconCaravan;
	private static Tile tileIconCaravanON;
	private static Point iconCaravanBackgroundPoint = new Point (0, 0);
	private static Point iconCaravanPoint = new Point (0, 0);

	private static Tile tileInfoPanel;
	private static boolean tileInfoPanelAlpha[][];
	private static Point infoPanelPoint = new Point (0, 0);

	private static Tile tileDatePanel;
	private static boolean tileDatePanelAlpha[][];
	private static Point datePanelPoint = new Point (0, 0);

	private static Tile tileIconCoins;
	private static Point tileIconCoinsPoint = new Point (0, 0);

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
	private static boolean checkBlinkBottom = false;
	private static boolean checkBlinkRight = false;
	private static boolean checkBlinkProduction = false;


	public UIPanel () {
		if (Game.getWorld () != null) {
			resize (UtilsGL.getWidth (), UtilsGL.getHeight (), Game.getWorld ().getCampaignID (), Game.getWorld ().getMissionID (), false);
		} else {
			resize (UtilsGL.getWidth (), UtilsGL.getHeight (), null, null, false);
		}
	}


	private void loadMenus (String sCampaignID, String sMissionID) {
		currentMenu = new SmartMenu ();
		SmartMenu.readXMLMenu (currentMenu, "menu.xml", sCampaignID, sMissionID); //$NON-NLS-1$

		menuPanelMenu = new SmartMenu ();
		SmartMenu.readXMLMenu (menuPanelMenu, "menu_right.xml", sCampaignID, sMissionID); //$NON-NLS-1$

		productionPanelMenu = new SmartMenu ();
		SmartMenu.readXMLMenu (productionPanelMenu, "menu_production.xml", sCampaignID, sMissionID); //$NON-NLS-1$

		// Vamos a setear los tamaños de los iconos de los menús para que sea proporcional al botón de menú
		resizeIcons (currentMenu, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT);
		resizeIcons (menuPanelMenu, MENU_ITEM_WIDTH, MENU_ITEM_HEIGHT);
		resizeIcons (productionPanelMenu, PRODUCTION_PANEL_ITEM_WIDTH, PRODUCTION_PANEL_ITEM_HEIGHT);
	}


	public static void resizeIcons (SmartMenu menu, int width, int height) {
		if (menu.getItems () != null) {
			for (int i = 0; i < menu.getItems ().size (); i++) {
				resizeIcons (menu.getItems ().get (i), width, height);
			}

			if (menu.getIcon () != null) {
				Tile tile = menu.getIcon ();
				if (tile.getTileWidth () > width || tile.getTileHeight () > height) {
					float relation = (float) tile.getTileWidth () / (float) tile.getTileHeight ();

					if (tile.getTileWidth () > tile.getTileHeight ()) {
						tile.setTileWidth (width);
						tile.setTileHeight ((int) (width / relation));
					} else {
						tile.setTileHeight (height);
						tile.setTileWidth ((int) (height * relation));
					}
				}
			}
		}
	}


	public void resize (int renderW, int renderH, String sCampaignID, String sMissionID, boolean bLoadMenus) {
		renderWidth = renderW;
		renderHeight = renderH;

		initialize (sCampaignID, sMissionID, bLoadMenus);
	}


	public SmartMenu getCurrentMenu () {
		return currentMenu;
	}


	/**
	 * Genera los tiles de la UI con su alpha y tal
	 */
	private static void generateTiles () {
		/*
		 * BOTTOM
		 */
		tileBottomItem = new Tile ("bottom_item"); //$NON-NLS-1$
		tileBottomItemSM = new Tile ("bottom_item_sm"); //$NON-NLS-1$
		tileBottomScrollLeft = new Tile ("bottom_scr_left"); //$NON-NLS-1$
		tileBottomScrollLeftON = new Tile ("bottom_scr_leftON"); //$NON-NLS-1$
		tileBottomScrollRight = new Tile ("bottom_scr_right"); //$NON-NLS-1$
		tileBottomScrollRightON = new Tile ("bottom_scr_rightON"); //$NON-NLS-1$
		tileBottomPanel = new Tile ("bottom_panel"); //$NON-NLS-1$

		tileBottomItemAlpha = UtilsGL.generateAlpha (tileBottomItem);
		tileBottomScrollLeftAlpha = UtilsGL.generateAlpha (tileBottomScrollLeft);
		tileBottomScrollRightAlpha = UtilsGL.generateAlpha (tileBottomScrollRight);
		tileBottomPanelAlpha = UtilsGL.generateAlpha (tileBottomPanel, BOTTOM_PANEL_WIDTH, BOTTOM_PANEL_HEIGHT);

		tileBottomSubItem = new Tile ("bottom_subitem"); //$NON-NLS-1$
		tileBottomSubPanel = new Tile [9]; // Background/N/S/E/W/NE,NW,SE,SW
		tileBottomSubPanel[0] = new Tile ("bottom_subpanel"); //$NON-NLS-1$
		tileBottomSubPanel[1] = new Tile ("bottom_subpanel_N"); //$NON-NLS-1$
		tileBottomSubPanel[2] = new Tile ("bottom_subpanel_S"); //$NON-NLS-1$
		tileBottomSubPanel[3] = new Tile ("bottom_subpanel_E"); //$NON-NLS-1$
		tileBottomSubPanel[4] = new Tile ("bottom_subpanel_W"); //$NON-NLS-1$
		tileBottomSubPanel[5] = new Tile ("bottom_subpanel_NE"); //$NON-NLS-1$
		tileBottomSubPanel[6] = new Tile ("bottom_subpanel_NW"); //$NON-NLS-1$
		tileBottomSubPanel[7] = new Tile ("bottom_subpanel_SE"); //$NON-NLS-1$
		tileBottomSubPanel[8] = new Tile ("bottom_subpanel_SW"); //$NON-NLS-1$

		tileBottomSubItemAlpha = UtilsGL.generateAlpha (tileBottomSubItem);

		tileOpenBottomMenu = new Tile ("icon_openBottom"); //$NON-NLS-1$
		tileOpenBottomMenuAlpha = UtilsGL.generateAlpha (tileOpenBottomMenu);
		tileOpenBottomMenuON = new Tile ("icon_openBottomON"); //$NON-NLS-1$
		tileOpenBottomMenuONAlpha = UtilsGL.generateAlpha (tileOpenBottomMenuON);

		/*
		 * MINIMAP
		 */
		tileMinimapPanel = new Tile ("minimap_panel"); //$NON-NLS-1$
		tileMinimapPanelAlpha = UtilsGL.generateAlpha (tileMinimapPanel);

		/*
		 * MESSAGES
		 */
		tileMessagesPanel = new Tile [9]; // Background/N/S/E/W/NE,NW,SE,SW
		tileMessagesPanel[0] = new Tile ("messages_panel"); //$NON-NLS-1$
		tileMessagesPanel[1] = new Tile ("messages_panel_N"); //$NON-NLS-1$
		tileMessagesPanel[2] = new Tile ("messages_panel_S"); //$NON-NLS-1$
		tileMessagesPanel[3] = new Tile ("messages_panel_E"); //$NON-NLS-1$
		tileMessagesPanel[4] = new Tile ("messages_panel_W"); //$NON-NLS-1$
		tileMessagesPanel[5] = new Tile ("messages_panel_NE"); //$NON-NLS-1$
		tileMessagesPanel[6] = new Tile ("messages_panel_NW"); //$NON-NLS-1$
		tileMessagesPanel[7] = new Tile ("messages_panel_SE"); //$NON-NLS-1$
		tileMessagesPanel[8] = new Tile ("messages_panel_SW"); //$NON-NLS-1$
		tileMessagesPanelSubPanel = new Tile [9]; // Background/N/S/E/W/NE,NW,SE,SW
		tileMessagesPanelSubPanel[0] = new Tile ("messages_subpanel"); //$NON-NLS-1$
		tileMessagesPanelSubPanel[1] = new Tile ("messages_subpanel_N"); //$NON-NLS-1$
		tileMessagesPanelSubPanel[2] = new Tile ("messages_subpanel_S"); //$NON-NLS-1$
		tileMessagesPanelSubPanel[3] = new Tile ("messages_subpanel_E"); //$NON-NLS-1$
		tileMessagesPanelSubPanel[4] = new Tile ("messages_subpanel_W"); //$NON-NLS-1$
		tileMessagesPanelSubPanel[5] = new Tile ("messages_subpanel_NE"); //$NON-NLS-1$
		tileMessagesPanelSubPanel[6] = new Tile ("messages_subpanel_NW"); //$NON-NLS-1$
		tileMessagesPanelSubPanel[7] = new Tile ("messages_subpanel_SE"); //$NON-NLS-1$
		tileMessagesPanelSubPanel[8] = new Tile ("messages_subpanel_SW"); //$NON-NLS-1$

		/*
		 * PRODUCTION PANEL
		 */
		tileProductionPanel = new Tile [9]; // Background/N/S/E/W/NE,NW,SE,SW
		tileProductionPanel[0] = new Tile ("production_panel"); //$NON-NLS-1$
		tileProductionPanel[1] = new Tile ("production_panel_N"); //$NON-NLS-1$
		tileProductionPanel[2] = new Tile ("production_panel_S"); //$NON-NLS-1$
		tileProductionPanel[3] = new Tile ("production_panel_E"); //$NON-NLS-1$
		tileProductionPanel[4] = new Tile ("production_panel_W"); //$NON-NLS-1$
		tileProductionPanel[5] = new Tile ("production_panel_NE"); //$NON-NLS-1$
		tileProductionPanel[6] = new Tile ("production_panel_NW"); //$NON-NLS-1$
		tileProductionPanel[7] = new Tile ("production_panel_SE"); //$NON-NLS-1$
		tileProductionPanel[8] = new Tile ("production_panel_SW"); //$NON-NLS-1$
		tileProductionPanelPlusIcon = new Tile ("production_panel_plus_icon"); //$NON-NLS-1$
		tileProductionPanelPlusIconAlpha = UtilsGL.generateAlpha (tileProductionPanelPlusIcon);
		tileProductionPanelMinusIcon = new Tile ("production_panel_minus_icon"); //$NON-NLS-1$
		tileProductionPanelMinusIconAlpha = UtilsGL.generateAlpha (tileProductionPanelMinusIcon);

		tileOpenProductionPanel = new Tile ("icon_openLeft"); //$NON-NLS-1$
		tileOpenProductionPanelAlpha = UtilsGL.generateAlpha (tileOpenProductionPanel);
		tileOpenProductionPanelON = new Tile ("icon_openLeftON"); //$NON-NLS-1$
		tileOpenProductionPanelONAlpha = UtilsGL.generateAlpha (tileOpenProductionPanelON);

		/*
		 * TRADE PANEL
		 */
		tileTradePanel = new Tile [9]; // Background/N/S/E/W/NE,NW,SE,SW
		tileTradePanel[0] = new Tile ("trade_panel"); //$NON-NLS-1$
		tileTradePanel[1] = new Tile ("trade_panel_N"); //$NON-NLS-1$
		tileTradePanel[2] = new Tile ("trade_panel_S"); //$NON-NLS-1$
		tileTradePanel[3] = new Tile ("trade_panel_E"); //$NON-NLS-1$
		tileTradePanel[4] = new Tile ("trade_panel_W"); //$NON-NLS-1$
		tileTradePanel[5] = new Tile ("trade_panel_NE"); //$NON-NLS-1$
		tileTradePanel[6] = new Tile ("trade_panel_NW"); //$NON-NLS-1$
		tileTradePanel[7] = new Tile ("trade_panel_SE"); //$NON-NLS-1$
		tileTradePanel[8] = new Tile ("trade_panel_SW"); //$NON-NLS-1$

		/*
		 * PRIORITIES PANEL
		 */
		tilePrioritiesPanel = new Tile [9]; // Background/N/S/E/W/NE,NW,SE,SW
		tilePrioritiesPanel[0] = new Tile ("priorities_panel"); //$NON-NLS-1$
		tilePrioritiesPanel[1] = new Tile ("priorities_panel_N"); //$NON-NLS-1$
		tilePrioritiesPanel[2] = new Tile ("priorities_panel_S"); //$NON-NLS-1$
		tilePrioritiesPanel[3] = new Tile ("priorities_panel_E"); //$NON-NLS-1$
		tilePrioritiesPanel[4] = new Tile ("priorities_panel_W"); //$NON-NLS-1$
		tilePrioritiesPanel[5] = new Tile ("priorities_panel_NE"); //$NON-NLS-1$
		tilePrioritiesPanel[6] = new Tile ("priorities_panel_NW"); //$NON-NLS-1$
		tilePrioritiesPanel[7] = new Tile ("priorities_panel_SE"); //$NON-NLS-1$
		tilePrioritiesPanel[8] = new Tile ("priorities_panel_SW"); //$NON-NLS-1$
		tilePrioritiesPanelUpIcon = new Tile ("priorities_up"); //$NON-NLS-1$
		tilePrioritiesPanelUpIconAlpha = UtilsGL.generateAlpha (tilePrioritiesPanelUpIcon);
		tilePrioritiesPanelDownIcon = new Tile ("priorities_down"); //$NON-NLS-1$
		tilePrioritiesPanelDownIconAlpha = UtilsGL.generateAlpha (tilePrioritiesPanelDownIcon);

		/*
		 * MATS PANEL
		 */
		tileMatsPanel = new Tile [9]; // Background/N/S/E/W/NE,NW,SE,SW
		tileMatsPanel[0] = new Tile ("mats_panel"); //$NON-NLS-1$
		tileMatsPanel[1] = new Tile ("mats_panel_N"); //$NON-NLS-1$
		tileMatsPanel[2] = new Tile ("mats_panel_S"); //$NON-NLS-1$
		tileMatsPanel[3] = new Tile ("mats_panel_E"); //$NON-NLS-1$
		tileMatsPanel[4] = new Tile ("mats_panel_W"); //$NON-NLS-1$
		tileMatsPanel[5] = new Tile ("mats_panel_NE"); //$NON-NLS-1$
		tileMatsPanel[6] = new Tile ("mats_panel_NW"); //$NON-NLS-1$
		tileMatsPanel[7] = new Tile ("mats_panel_SE"); //$NON-NLS-1$
		tileMatsPanel[8] = new Tile ("mats_panel_SW"); //$NON-NLS-1$
		tileMatsPanelSubPanel = new Tile [9]; // Background/N/S/E/W/NE,NW,SE,SW
		tileMatsPanelSubPanel[0] = new Tile ("mats_subpanel"); //$NON-NLS-1$
		tileMatsPanelSubPanel[1] = new Tile ("mats_subpanel_N"); //$NON-NLS-1$
		tileMatsPanelSubPanel[2] = new Tile ("mats_subpanel_S"); //$NON-NLS-1$
		tileMatsPanelSubPanel[3] = new Tile ("mats_subpanel_E"); //$NON-NLS-1$
		tileMatsPanelSubPanel[4] = new Tile ("mats_subpanel_W"); //$NON-NLS-1$
		tileMatsPanelSubPanel[5] = new Tile ("mats_subpanel_NE"); //$NON-NLS-1$
		tileMatsPanelSubPanel[6] = new Tile ("mats_subpanel_NW"); //$NON-NLS-1$
		tileMatsPanelSubPanel[7] = new Tile ("mats_subpanel_SE"); //$NON-NLS-1$
		tileMatsPanelSubPanel[8] = new Tile ("mats_subpanel_SW"); //$NON-NLS-1$

		/*
		 * LIVINGS PANEL
		 */
		tileLivingsPanel = new Tile [9]; // Background/N/S/E/W/NE,NW,SE,SW
		tileLivingsPanel[0] = new Tile ("livings_panel"); //$NON-NLS-1$
		tileLivingsPanel[1] = new Tile ("livings_panel_N"); //$NON-NLS-1$
		tileLivingsPanel[2] = new Tile ("livings_panel_S"); //$NON-NLS-1$
		tileLivingsPanel[3] = new Tile ("livings_panel_E"); //$NON-NLS-1$
		tileLivingsPanel[4] = new Tile ("livings_panel_W"); //$NON-NLS-1$
		tileLivingsPanel[5] = new Tile ("livings_panel_NE"); //$NON-NLS-1$
		tileLivingsPanel[6] = new Tile ("livings_panel_NW"); //$NON-NLS-1$
		tileLivingsPanel[7] = new Tile ("livings_panel_SE"); //$NON-NLS-1$
		tileLivingsPanel[8] = new Tile ("livings_panel_SW"); //$NON-NLS-1$
		tileLivingsGroupPanel = new Tile [9]; // Background/N/S/E/W/NE,NW,SE,SW
		tileLivingsGroupPanel[0] = new Tile ("livings_group_panel"); //$NON-NLS-1$
		tileLivingsGroupPanel[1] = new Tile ("livings_group_panel_N"); //$NON-NLS-1$
		tileLivingsGroupPanel[2] = new Tile ("livings_group_panel_S"); //$NON-NLS-1$
		tileLivingsGroupPanel[3] = new Tile ("livings_group_panel_E"); //$NON-NLS-1$
		tileLivingsGroupPanel[4] = new Tile ("livings_group_panel_W"); //$NON-NLS-1$
		tileLivingsGroupPanel[5] = new Tile ("livings_group_panel_NE"); //$NON-NLS-1$
		tileLivingsGroupPanel[6] = new Tile ("livings_group_panel_NW"); //$NON-NLS-1$
		tileLivingsGroupPanel[7] = new Tile ("livings_group_panel_SE"); //$NON-NLS-1$
		tileLivingsGroupPanel[8] = new Tile ("livings_group_panel_SW"); //$NON-NLS-1$

		tileLivingsPanelRowNoHead = new Tile ("livings_nohead"); //$NON-NLS-1$
		tileLivingsPanelRowNoBody = new Tile ("livings_nobody"); //$NON-NLS-1$
		tileLivingsPanelRowNoLegs = new Tile ("livings_nolegs"); //$NON-NLS-1$
		tileLivingsPanelRowNoFeet = new Tile ("livings_nofeet"); //$NON-NLS-1$
		tileLivingsPanelRowNoWeapon = new Tile ("livings_noweapon"); //$NON-NLS-1$

		tileLivingsRowAutoequip = new Tile ("livings_autoequip"); //$NON-NLS-1$
		tileLivingsRowAutoequipON = new Tile ("livings_autoequipON"); //$NON-NLS-1$
		tileLivingsRowAutoequipAlpha = UtilsGL.generateAlpha (tileLivingsRowAutoequip);

		tileLivingsRowProfession = new Tile ("livings_professions"); //$NON-NLS-1$
		tileLivingsRowProfessionON = new Tile ("livings_professionsON"); //$NON-NLS-1$
		tileLivingsRowProfessionAlpha = UtilsGL.generateAlpha (tileLivingsRowProfession);
		tileLivingsRowJobsGroups = new Tile ("livings_jobgroup_change"); //$NON-NLS-1$
		tileLivingsRowJobsGroupsON = new Tile ("livings_jobgroup_changeON"); //$NON-NLS-1$
		tileLivingsRowJobsGroupsAlpha = UtilsGL.generateAlpha (tileLivingsRowJobsGroups);
		tileLivingsRowConvertSoldier = new Tile ("livings_convert_soldier"); //$NON-NLS-1$
		tileLivingsRowConvertSoldierON = new Tile ("livings_convert_soldierON"); //$NON-NLS-1$
		tileLivingsRowConvertSoldierAlpha = UtilsGL.generateAlpha (tileLivingsRowConvertSoldier);
		tileLivingsRowConvertCivilian = new Tile ("livings_convert_civilian"); //$NON-NLS-1$
		tileLivingsRowConvertCivilianON = new Tile ("livings_convert_civilianON"); //$NON-NLS-1$
		tileLivingsRowConvertCivilianAlpha = UtilsGL.generateAlpha (tileLivingsRowConvertCivilian);
		tileLivingsRowConvertSoldierGuard = new Tile ("livings_convert_soldier_guard"); //$NON-NLS-1$
		tileLivingsRowConvertSoldierGuardON = new Tile ("livings_convert_soldier_guardON"); //$NON-NLS-1$
		tileLivingsRowConvertSoldierGuardAlpha = UtilsGL.generateAlpha (tileLivingsRowConvertSoldierGuard);
		tileLivingsRowConvertSoldierPatrol = new Tile ("livings_convert_soldier_patrol"); //$NON-NLS-1$
		tileLivingsRowConvertSoldierPatrolON = new Tile ("livings_convert_soldier_patrolON"); //$NON-NLS-1$
		tileLivingsRowConvertSoldierPatrolAlpha = UtilsGL.generateAlpha (tileLivingsRowConvertSoldierPatrol);
		tileLivingsRowConvertSoldierBoss = new Tile ("livings_convert_soldier_boss"); //$NON-NLS-1$
		tileLivingsRowConvertSoldierBossON = new Tile ("livings_convert_soldier_bossON"); //$NON-NLS-1$
		tileLivingsRowConvertSoldierBossAlpha = UtilsGL.generateAlpha (tileLivingsRowConvertSoldierBoss);

		tileLivingsRowGroupAdd = new Tile ("livings_group_add"); //$NON-NLS-1$
		tileLivingsRowGroupAddON = new Tile ("livings_group_addON"); //$NON-NLS-1$
		tileLivingsRowGroupAddAlpha = UtilsGL.generateAlpha (tileLivingsRowGroupAdd);
		tileLivingsRowGroupRemove = new Tile ("livings_group_remove"); //$NON-NLS-1$
		tileLivingsRowGroupRemoveON = new Tile ("livings_group_removeON"); //$NON-NLS-1$
		tileLivingsRowGroupRemoveAlpha = UtilsGL.generateAlpha (tileLivingsRowGroupRemove);

		tileLivingsGroup = new Tile ("livings_group"); //$NON-NLS-1$
		tileLivingsGroupON = new Tile ("livings_groupON"); //$NON-NLS-1$
		tileLivingsGroupGreen = new Tile ("livings_group_green"); //$NON-NLS-1$
		tileLivingsGroupAlpha = UtilsGL.generateAlpha (tileLivingsGroup);
		tileLivingsNoGroup = new Tile ("livings_nogroup"); //$NON-NLS-1$
		tileLivingsNoGroupON = new Tile ("livings_nogroupON"); //$NON-NLS-1$
		tileLivingsNoGroupGreen = new Tile ("livings_nogroup_green"); //$NON-NLS-1$
		tileLivingsNoGroupAlpha = UtilsGL.generateAlpha (tileLivingsNoGroup);

		tileLivingsJobGroup = new Tile ("livings_jobgroup"); //$NON-NLS-1$
		tileLivingsJobGroupON = new Tile ("livings_jobgroupON"); //$NON-NLS-1$
		tileLivingsJobGroupGreen = new Tile ("livings_jobgroup_green"); //$NON-NLS-1$
		tileLivingsJobGroupAlpha = UtilsGL.generateAlpha (tileLivingsJobGroup);
		tileLivingsNoJobGroup = new Tile ("livings_nojobgroup"); //$NON-NLS-1$
		tileLivingsNoJobGroupON = new Tile ("livings_nojobgroupON"); //$NON-NLS-1$
		tileLivingsNoJobGroupGreen = new Tile ("livings_nojobgroup_green"); //$NON-NLS-1$
		tileLivingsNoJobGroupAlpha = UtilsGL.generateAlpha (tileLivingsNoJobGroup);

		tileLivingsSingleGroupRename = new Tile ("livings_group_rename"); //$NON-NLS-1$
		tileLivingsSingleGroupRenameON = new Tile ("livings_group_renameON"); //$NON-NLS-1$
		tileLivingsSingleGroupRenameAlpha = UtilsGL.generateAlpha (tileLivingsSingleGroupRename);
		tileLivingsSingleJobGroupRename = new Tile ("livings_jobgroup_rename"); //$NON-NLS-1$
		tileLivingsSingleJobGroupRenameON = new Tile ("livings_jobgroup_renameON"); //$NON-NLS-1$
		tileLivingsSingleJobGroupRenameAlpha = UtilsGL.generateAlpha (tileLivingsSingleJobGroupRename);
		tileLivingsSingleGroupGuard = new Tile ("livings_group_guard"); //$NON-NLS-1$
		tileLivingsSingleGroupGuardON = new Tile ("livings_group_guardON"); //$NON-NLS-1$
		tileLivingsSingleGroupGuardAlpha = UtilsGL.generateAlpha (tileLivingsSingleGroupGuard);
		tileLivingsSingleGroupPatrol = new Tile ("livings_group_patrol"); //$NON-NLS-1$
		tileLivingsSingleGroupPatrolON = new Tile ("livings_group_patrolON"); //$NON-NLS-1$
		tileLivingsSingleGroupPatrolAlpha = UtilsGL.generateAlpha (tileLivingsSingleGroupPatrol);
		tileLivingsSingleGroupBoss = new Tile ("livings_group_boss"); //$NON-NLS-1$
		tileLivingsSingleGroupBossON = new Tile ("livings_group_bossON"); //$NON-NLS-1$
		tileLivingsSingleGroupBossAlpha = UtilsGL.generateAlpha (tileLivingsSingleGroupBoss);
		tileLivingsSingleGroupDisband = new Tile ("livings_group_disband"); //$NON-NLS-1$
		tileLivingsSingleGroupDisbandON = new Tile ("livings_group_disbandON"); //$NON-NLS-1$
		tileLivingsSingleGroupDisbandAlpha = UtilsGL.generateAlpha (tileLivingsSingleGroupDisband);
		tileLivingsSingleJobGroupDisband = new Tile ("livings_jobgroup_disband"); //$NON-NLS-1$
		tileLivingsSingleJobGroupDisbandON = new Tile ("livings_jobgroup_disbandON"); //$NON-NLS-1$
		tileLivingsSingleJobGroupDisbandAlpha = UtilsGL.generateAlpha (tileLivingsSingleJobGroupDisband);

		tileLivingsSingleGroupChangeJobs = new Tile ("livings_jobgroup_changejob"); //$NON-NLS-1$
		tileLivingsSingleGroupChangeJobsON = new Tile ("livings_jobgroup_changejobON"); //$NON-NLS-1$
		tileLivingsSingleGroupChangeJobsAlpha = UtilsGL.generateAlpha (tileLivingsSingleGroupChangeJobs);

		/*
		 * MENU (right)
		 */
		tileMenuPanel = new Tile [9]; // Background/N/S/E/W/NE,NW,SE,SW
		tileMenuPanel[0] = new Tile ("menu_panel"); //$NON-NLS-1$
		tileMenuPanel[1] = new Tile ("menu_panel_N"); //$NON-NLS-1$
		tileMenuPanel[2] = new Tile ("menu_panel_S"); //$NON-NLS-1$
		tileMenuPanel[3] = new Tile ("menu_panel_E"); //$NON-NLS-1$
		tileMenuPanel[4] = new Tile ("menu_panel_W"); //$NON-NLS-1$
		tileMenuPanel[5] = new Tile ("menu_panel_NE"); //$NON-NLS-1$
		tileMenuPanel[6] = new Tile ("menu_panel_NW"); //$NON-NLS-1$
		tileMenuPanel[7] = new Tile ("menu_panel_SE"); //$NON-NLS-1$
		tileMenuPanel[8] = new Tile ("menu_panel_SW"); //$NON-NLS-1$
		tileOpenRightMenu = new Tile ("icon_openRight"); //$NON-NLS-1$
		tileOpenRightMenuAlpha = UtilsGL.generateAlpha (tileOpenRightMenu);
		tileOpenRightMenuON = new Tile ("icon_openRightON"); //$NON-NLS-1$
		tileOpenRightMenuONAlpha = UtilsGL.generateAlpha (tileOpenRightMenuON);

		/*
		 * Mini icons
		 */
		tileIconCitizenNext = new Tile ("icon_citizennext"); //$NON-NLS-1$
		tileIconCitizenNextON = new Tile ("icon_citizennextON"); //$NON-NLS-1$
		tileIconCitizenPrevious = new Tile ("icon_citizenprevious"); //$NON-NLS-1$
		tileIconCitizenPreviousON = new Tile ("icon_citizenpreviousON"); //$NON-NLS-1$
		tileIconSoldierNext = new Tile ("icon_soldiernext"); //$NON-NLS-1$
		tileIconSoldierNextON = new Tile ("icon_soldiernextON"); //$NON-NLS-1$
		tileIconSoldierPrevious = new Tile ("icon_soldierprevious"); //$NON-NLS-1$
		tileIconSoldierPreviousON = new Tile ("icon_soldierpreviousON"); //$NON-NLS-1$
		tileIconHeroNext = new Tile ("icon_heronext"); //$NON-NLS-1$
		tileIconHeroNextON = new Tile ("icon_heronextON"); //$NON-NLS-1$
		tileIconHeroPrevious = new Tile ("icon_heroprevious"); //$NON-NLS-1$
		tileIconHeroPreviousON = new Tile ("icon_heropreviousON"); //$NON-NLS-1$

		tileIconNextMiniAlpha = UtilsGL.generateAlpha (tileIconCitizenNext);
		tileIconPreviousMiniAlpha = UtilsGL.generateAlpha (tileIconCitizenPrevious);

		tileIconLevelDown = new Tile ("icon_leveldown"); //$NON-NLS-1$
		tileIconLevelDownAlpha = UtilsGL.generateAlpha (tileIconLevelDown);
		tileIconLevel = new Tile ("icon_level"); //$NON-NLS-1$
		tileIconLevelAlpha = UtilsGL.generateAlpha (tileIconLevel);
		tileIconLevelUp = new Tile ("icon_levelup"); //$NON-NLS-1$
		tileIconLevelUpAlpha = UtilsGL.generateAlpha (tileIconLevelUp);

		/*
		 * Icons
		 */
		tileIconPriorities = new Tile ("icon_priorities"); //$NON-NLS-1$
		tileIconPrioritiesON = new Tile ("icon_prioritiesON"); //$NON-NLS-1$
		tileIconPrioritiesAlpha = UtilsGL.generateAlpha (tileIconPriorities);
		tileIconMats = new Tile ("icon_mats"); //$NON-NLS-1$
		tileIconMatsON = new Tile ("icon_matsON"); //$NON-NLS-1$
		tileIconMatsAlpha = UtilsGL.generateAlpha (tileIconMats);
		tileIconSettings = new Tile ("icon_settings"); //$NON-NLS-1$
		tileIconSettingsAlpha = UtilsGL.generateAlpha (tileIconSettings);
		tileIconGrid = new Tile ("icon_grid"); //$NON-NLS-1$
		tileIconGridON = new Tile ("icon_gridON"); //$NON-NLS-1$
		tileIconGridAlpha = UtilsGL.generateAlpha (tileIconGrid);
		tileIconMiniblocks = new Tile ("icon_miniblock"); //$NON-NLS-1$
		tileIconMiniblocksON = new Tile ("icon_miniblockON"); //$NON-NLS-1$
		tileIconMiniblocksAlpha = UtilsGL.generateAlpha (tileIconMiniblocks);
		tileIconFlatMouse = new Tile ("icon_flatmouse"); //$NON-NLS-1$
		tileIconFlatMouseON = new Tile ("icon_flatmouseON"); //$NON-NLS-1$
		tileIconFlatMouseAlpha = UtilsGL.generateAlpha (tileIconFlatMouse);
		tileIcon3DMouse = new Tile ("icon_3d_mouse"); //$NON-NLS-1$
		tileIcon3DMouseON = new Tile ("icon_3d_mouseON"); //$NON-NLS-1$
		tileIcon3DMouseAlpha = UtilsGL.generateAlpha (tileIcon3DMouse);
		tileIconPause = new Tile ("icon_pause"); //$NON-NLS-1$
		tileIconResume = new Tile ("icon_resume"); //$NON-NLS-1$
		tileIconPauseResumeAlpha = UtilsGL.generateAlpha (tileIconPause);
		tileIconIncreaseSpeed = new Tile ("icon_increase_speed"); //$NON-NLS-1$
		tileIconIncreaseSpeedON = new Tile ("icon_increase_speedON"); //$NON-NLS-1$
		tileIconIncreaseSpeedAlpha = UtilsGL.generateAlpha (tileIconIncreaseSpeed);
		tileIconLowerSpeed = new Tile ("icon_lower_speed"); //$NON-NLS-1$
		tileIconLowerSpeedON = new Tile ("icon_lower_speedON"); //$NON-NLS-1$
		tileIconLowerSpeedAlpha = UtilsGL.generateAlpha (tileIconLowerSpeed);

		/*
		 * Info
		 */
		tileIconNumCitizens = new Tile ("icon_numcitizens"); //$NON-NLS-1$
		tileIconNumSoldiers = new Tile ("icon_numsoldiers"); //$NON-NLS-1$
		tileIconNumHeroes = new Tile ("icon_numheroes"); //$NON-NLS-1$
		tileIconCaravan = new Tile ("icon_caravan"); //$NON-NLS-1$
		tileIconCaravanON = new Tile ("icon_caravanON"); //$NON-NLS-1$

		tileInfoPanel = new Tile ("info_panel"); //$NON-NLS-1$
		tileInfoPanelAlpha = UtilsGL.generateAlpha (tileInfoPanel);
		tileDatePanel = new Tile ("date_panel"); //$NON-NLS-1$
		tileDatePanelAlpha = UtilsGL.generateAlpha (tileDatePanel);

		tileIconCoins = new Tile ("icon_coins"); //$NON-NLS-1$

		tileIconTutorial = new Tile ("icon_tutorial"); //$NON-NLS-1$

		// tileIconGods = new Tile ("icon_gods");

		/*
		 * Tooltip
		 */
		tileTooltipBackground = new Tile ("tooltip_background"); //$NON-NLS-1$
	}


	public void initialize (String sCampaignID, String sMissionID, boolean bLoadMenus) {
		if (currentMenu == null && bLoadMenus) {
			loadMenus (sCampaignID, sMissionID);
		}

		if (tileBottomItem == null) {
			generateTiles ();
		}

		PIXELS_TO_BORDER = renderWidth / 80;

		// MINIMAP
		MINIMAP_PANEL_WIDTH = tileMinimapPanel.getTileWidth ();
		MINIMAP_PANEL_HEIGHT = tileMinimapPanel.getTileHeight ();

		minimapPanelX = renderWidth - MINIMAP_PANEL_WIDTH - PIXELS_TO_BORDER;
		minimapPanelY = PIXELS_TO_BORDER;
		MiniMapPanel.initialize (minimapPanelX, minimapPanelY, MINIMAP_PANEL_WIDTH, MINIMAP_PANEL_HEIGHT);

		/*
		 * BOTTOM panel
		 */
		if (bottomPanelItemsPosition == null) {
			bottomPanelItemsPosition = new ArrayList<Point> (BOTTOM_PANEL_NUM_ITEMS);
		}

		// Centramos el panel
		bottomPanelX = renderWidth / 2 - BOTTOM_PANEL_WIDTH / 2;
		bottomPanelY = renderHeight - BOTTOM_PANEL_HEIGHT - tileOpenBottomMenu.getTileHeight ();
		// Calculamos la posición de los minipaneles de scroll
		bottomPanelLeftScrollX = bottomPanelX - BOTTOM_PANEL_SCROLL_WIDTH;
		bottomPanelRightScrollX = bottomPanelX + BOTTOM_PANEL_WIDTH;

		bottomPanelItemIndex = 0;

		// Subpanel
		bottomSubPanelMenu = null;

		// Cargamos las posiciones
		bottomPanelItemsPosition.clear ();
		int spaceBetweenItems = (BOTTOM_PANEL_WIDTH - (BOTTOM_ITEM_WIDTH * BOTTOM_PANEL_NUM_ITEMS)) / (BOTTOM_PANEL_NUM_ITEMS + 1);
		for (int i = 0; i < BOTTOM_PANEL_NUM_ITEMS; i++) {
			bottomPanelItemsPosition.add (new Point (bottomPanelX + spaceBetweenItems + (i * (BOTTOM_ITEM_WIDTH + spaceBetweenItems)), bottomPanelY + (BOTTOM_PANEL_HEIGHT / 2) - (BOTTOM_ITEM_HEIGHT / 2)));
		}

		// Minibotón para abrir/cerrar el panel de abajo
		tileOpenCloseBottomMenuPoint.setLocation (renderWidth / 2 - tileOpenBottomMenu.getTileWidth () / 2, renderHeight - tileOpenBottomMenu.getTileHeight ());

		/*
		 * Date panel
		 */
		datePanelPoint.setLocation (renderWidth / 2 - tileDatePanel.getTileWidth () / 2, tileIconCoins.getTileHeight ());

		/*
		 * Coins icon point
		 */
		tileIconCoinsPoint.setLocation (renderWidth / 2, 0 + tileIconCoins.getTileHeightOffset ());

		/*
		 * Info panel
		 */
		infoPanelPoint.setLocation (renderWidth / 2 - tileInfoPanel.getTileWidth () / 2 + tileInfoPanel.getTileWidthOffset (), 0);

		int iSeparation = datePanelPoint.x - infoPanelPoint.x;
		iSeparation = iSeparation - 2 * tileBottomItem.getTileWidth ();
		iSeparation /= 3;
		// Citizens
		iconNumCitizensBackgroundPoint.setLocation (infoPanelPoint.x + iSeparation, infoPanelPoint.y + tileIconNumCitizens.getTileHeightOffset ());
		iconNumCitizensPoint.setLocation (iconNumCitizensBackgroundPoint.x + tileIconNumCitizens.getTileWidthOffset (), iconNumCitizensBackgroundPoint.y + tileIconNumCitizens.getTileHeightOffset ());
		iconCitizenPreviousPoint.setLocation (iconNumCitizensBackgroundPoint.x + tileIconCitizenPrevious.getTileWidthOffset (), iconNumCitizensBackgroundPoint.y + tileIconCitizenPrevious.getTileHeightOffset ());
		iconCitizenNextPoint.setLocation (iconNumCitizensBackgroundPoint.x + tileIconCitizenNext.getTileWidthOffset (), iconNumCitizensBackgroundPoint.y + tileIconCitizenNext.getTileHeightOffset ());

		// Soldiers
		iconNumSoldiersBackgroundPoint.setLocation (infoPanelPoint.x + 2 * iSeparation + tileBottomItem.getTileWidth (), infoPanelPoint.y + tileIconNumSoldiers.getTileHeightOffset ());
		iconNumSoldiersPoint.setLocation (iconNumSoldiersBackgroundPoint.x + tileIconNumSoldiers.getTileWidthOffset (), iconNumSoldiersBackgroundPoint.y + tileIconNumSoldiers.getTileHeightOffset ());
		iconSoldierPreviousPoint.setLocation (iconNumSoldiersBackgroundPoint.x + tileIconSoldierPrevious.getTileWidthOffset (), iconNumSoldiersBackgroundPoint.y + tileIconSoldierPrevious.getTileHeightOffset ());
		iconSoldierNextPoint.setLocation (iconNumSoldiersBackgroundPoint.x + tileIconSoldierNext.getTileWidthOffset (), iconNumSoldiersBackgroundPoint.y + tileIconSoldierNext.getTileHeightOffset ());

		iSeparation = infoPanelPoint.x + tileInfoPanel.getTileWidth () - (datePanelPoint.x + tileDatePanel.getTileWidth ());
		iSeparation = iSeparation - 2 * tileBottomItem.getTileWidth ();
		iSeparation /= 3;

		// Heroes
		iconNumHeroesBackgroundPoint.setLocation (datePanelPoint.x + tileDatePanel.getTileWidth () + iSeparation, infoPanelPoint.y + tileIconNumHeroes.getTileHeightOffset ());
		iconNumHeroesPoint.setLocation (iconNumHeroesBackgroundPoint.x + tileIconNumHeroes.getTileWidthOffset (), iconNumHeroesBackgroundPoint.y + tileIconNumHeroes.getTileHeightOffset ());
		iconHeroPreviousPoint.setLocation (iconNumHeroesBackgroundPoint.x + tileIconHeroPrevious.getTileWidthOffset (), iconNumHeroesBackgroundPoint.y + tileIconHeroPrevious.getTileHeightOffset ());
		iconHeroNextPoint.setLocation (iconNumHeroesBackgroundPoint.x + tileIconHeroNext.getTileWidthOffset (), iconNumHeroesBackgroundPoint.y + tileIconHeroNext.getTileHeightOffset ());

		// Caravan
		iconCaravanBackgroundPoint.setLocation (datePanelPoint.x + tileDatePanel.getTileWidth () + 2 * iSeparation + tileBottomItem.getTileWidth (), infoPanelPoint.y + tileIconCaravan.getTileHeightOffset ());
		iconCaravanPoint.setLocation (iconCaravanBackgroundPoint.x + tileIconCaravan.getTileWidthOffset (), iconCaravanBackgroundPoint.y + tileBottomItem.getTileHeight () / 2 - tileIconCaravan.getTileHeight () / 2);

		if (bLoadMenus) {
			/*
			 * Menu panel (menú de la derecha)
			 */
			createMenuPanel (menuPanelMenu);

			/*
			 * Production panel
			 */
			createProductionPanel (productionPanelMenu);

			/*
			 * Trade panel
			 */
			createTradePanel ();

			/*
			 * Mats panel
			 */
			createMatsPanel ();

			/*
			 * Stockpile panel (piles + containers)
			 */
			createPilePanel ();

			/*
			 * Professions panel
			 */
			createProfessionsPanel ();

			/*
			 * Livings panel
			 */
			createLivingsPanel (LIVINGS_PANEL_TYPE_NONE, -1, -1);

			/*
			 * Priorities panel
			 */
			createPrioritiesPanel ();

			/*
			 * Images panel
			 */
			ImagesPanel.resize (renderWidth, renderHeight);
		}

		/*
		 * Messages panel
		 */
		createMessagesPanel ();

		// Images button location
		iconTutorialPoint.setLocation (messageIconPoints[0].x, messageIconPoints[0].y + messageTiles[0].getTileHeight () + PIXELS_TO_BORDER + BOTTOM_ITEM_HEIGHT + BOTTOM_ITEM_HEIGHT / 4);

		// Events + gods icons
		int iStartingX = messageIconPoints[messageIconPoints.length - 1].x + messageTiles[messageTiles.length - 1].getTileWidth ();
		int iAvailableWidth = infoPanelPoint.x - iStartingX;

		iconEventsPoint.setLocation (iStartingX + iAvailableWidth / 4 - ICON_WIDTH / 2 + 3, messageIconPoints[messageIconPoints.length - 1].y + messageTiles[messageTiles.length - 1].getTileHeight () / 2 - GlobalEventData.getIcon ().getTileHeight () / 2);

		// Gods icon
		// iconGodsPoint.setLocation (iStartingX + (iAvailableWidth / 4) * 3 - ICON_WIDTH / 2 + 3, iconEventsPoint.y + GlobalEventData.getIcon ().getTileHeight () / 2 - tileIconGods.getTileHeight () / 2);

		/*
		 * Mini icons
		 */
		iconLevelUpPoint.setLocation (minimapPanelX + tileIconLevelUp.getTileWidthOffset (), minimapPanelY + tileIconLevelUp.getTileHeightOffset ());
		iconLevelPoint.setLocation (minimapPanelX + tileIconLevel.getTileWidthOffset (), minimapPanelY + tileIconLevel.getTileHeightOffset ());
		iconLevelDownPoint.setLocation (minimapPanelX + tileIconLevelDown.getTileWidthOffset (), minimapPanelY + tileIconLevelDown.getTileHeightOffset ());

		// Debajo del date metemos 2 iconos de panel (de momento priorities y mats)
		int iPanels = (tileDatePanel.getTileWidth () - tileIconPriorities.getTileWidth () - tileIconMats.getTileWidth ()) / 3;
		iconMatsPoint.setLocation (datePanelPoint.x + iPanels, datePanelPoint.y + tileDatePanel.getTileHeight () + tileIconMats.getTileHeightOffset ());
		iconPrioritiesPoint.setLocation (datePanelPoint.x + iPanels + tileIconMats.getTileWidth () + iPanels, datePanelPoint.y + tileDatePanel.getTileHeight () + tileIconPriorities.getTileHeightOffset ());

		// Miniblocks, grid, settings, flat mouse, 3D mouse
		iconMiniblocksPoint.setLocation (minimapPanelX + tileIconMiniblocks.getTileWidthOffset (), minimapPanelY + tileIconMiniblocks.getTileHeightOffset ());
		iconGridPoint.setLocation (minimapPanelX + tileIconGrid.getTileWidthOffset (), minimapPanelY + tileIconGrid.getTileHeightOffset ());
		iconSettingsPoint.setLocation (minimapPanelX + tileIconSettings.getTileWidthOffset (), minimapPanelY + tileIconSettings.getTileHeightOffset ());
		iconFlatMousePoint.setLocation (minimapPanelX + tileIconFlatMouse.getTileWidthOffset (), minimapPanelY + tileIconFlatMouse.getTileHeightOffset ());
		icon3DMousePoint.setLocation (minimapPanelX + tileIcon3DMouse.getTileWidthOffset (), minimapPanelY + tileIcon3DMouse.getTileHeightOffset ());

		// Lower speed, pause/resume, increase speed
		iconLowerSpeedPoint.setLocation (minimapPanelX + tileIconLowerSpeedON.getTileWidthOffset (), minimapPanelY + tileIconLowerSpeedON.getTileHeightOffset ());
		iconPauseResumePoint.setLocation (minimapPanelX + tileIconPause.getTileWidthOffset (), minimapPanelY + tileIconPause.getTileHeightOffset ());
		iconIncreaseSpeedPoint.setLocation (minimapPanelX + tileIconIncreaseSpeedON.getTileWidthOffset (), minimapPanelY + tileIconIncreaseSpeedON.getTileHeightOffset ());

		// Edge menus
		if (isProductionPanelLocked ()) {
			setProductionPanelActive (true);
		}

		if (isMenuPanelLocked ()) {
			setMenuPanelActive (true);
		}

		if (isBottomMenuPanelLocked ()) {
			setBottomMenuPanelActive (true, true);
		}
	}


	public void render () {
		if (MainPanel.bHideUION) {
			return;
		}

		int mouseX = Mouse.getX ();
		int mouseY = renderHeight - Mouse.getY () - 1;
		delayTime++;
		blinkTurns++;
		if (blinkTurns >= MAX_BLINK_TURNS) {
			blinkTurns = 0;
		}

		int mousePanel = isMouseOnAPanel (mouseX, mouseY, true);

		/*
		 * BOTTOM menu panel
		 */
		int iCurrentTexture = tileBottomScrollLeft.getTextureID (); // Esta textura es la primera quese usa en el bottom menu
		GL11.glBindTexture (GL11.GL_TEXTURE_2D, iCurrentTexture);
		GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		GL11.glColor4f (1, 1, 1, 1);
		UtilsGL.glBegin (GL11.GL_QUADS);

		checkBlinkBottom = (blinkTurns >= MAX_BLINK_TURNS / 2) && TutorialFlow.isBlinkBottom ();
		if (isBottomMenuPanelActive ()) {
			iCurrentTexture = renderBottomMenuPanel (mouseX, mouseY, mousePanel, iCurrentTexture);
		}

		// Rendereamos el botoncito para hacer visible/invisible el bottom panel
		if (isBottomMenuPanelLocked ()) {
			iCurrentTexture = UtilsGL.setTexture (tileOpenBottomMenuON, iCurrentTexture);
			drawTile (tileOpenBottomMenuON, tileOpenCloseBottomMenuPoint, tileOpenBottomMenuON.getTileWidth (), tileOpenBottomMenuON.getTileHeight (), mousePanel == MOUSE_BOTTOM_OPENCLOSE);
		} else {
			iCurrentTexture = UtilsGL.setTexture (tileOpenBottomMenu, iCurrentTexture);
			if (checkBlinkBottom) {
				UtilsGL.setColorRed ();
			}
			drawTile (tileOpenBottomMenu, tileOpenCloseBottomMenuPoint, tileOpenBottomMenu.getTileWidth (), tileOpenBottomMenu.getTileHeight (), mousePanel == MOUSE_BOTTOM_OPENCLOSE);
			if (checkBlinkBottom) {
				UtilsGL.unsetColor ();
			}
		}

		/*
		 * MINIMAP (textures)
		 */
		// Minimap background
		iCurrentTexture = UtilsGL.setTexture (tileMinimapPanel, iCurrentTexture);
		UtilsGL.drawTexture (minimapPanelX, minimapPanelY, minimapPanelX + MINIMAP_PANEL_WIDTH, minimapPanelY + MINIMAP_PANEL_HEIGHT, tileMinimapPanel.getTileSetTexX0 (), tileMinimapPanel.getTileSetTexY0 (), tileMinimapPanel.getTileSetTexX1 (), tileMinimapPanel.getTileSetTexY1 ());

		UtilsGL.glEnd ();

		// Minimap content
		MiniMapPanel.render ();

		/*
		 * MENU (right)
		 */
		renderMenuPanel (mouseX, mouseY, mousePanel);

		// Possible mini icon blinks?
		// Blink
		TutorialFlow tutorialFlow = null;
		if ((blinkTurns >= MAX_BLINK_TURNS / 2)) {
			if (Game.getCurrentMissionData () != null && ImagesPanel.getCurrentFlowIndex () >= 0 && ImagesPanel.getCurrentFlowIndex () < Game.getCurrentMissionData ().getTutorialFlows ().size ()) {
				tutorialFlow = Game.getCurrentMissionData ().getTutorialFlows ().get (ImagesPanel.getCurrentFlowIndex ());
			}
		}

		/*
		 * Info
		 */
		iCurrentTexture = tileInfoPanel.getTextureID ();
		GL11.glBindTexture (GL11.GL_TEXTURE_2D, iCurrentTexture);
		GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		UtilsGL.glBegin (GL11.GL_QUADS);
		drawTile (tileInfoPanel, infoPanelPoint);
		iCurrentTexture = UtilsGL.setTexture (tileDatePanel, iCurrentTexture);
		drawTile (tileDatePanel, datePanelPoint);

		// Level up/down
		iCurrentTexture = UtilsGL.setTexture (tileIconLevelUp, iCurrentTexture);
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniLevelUp ()) {
			UtilsGL.setColorRed ();
		}
		drawTile (tileIconLevelUp, iconLevelUpPoint, ICON_WIDTH, ICON_HEIGHT, mousePanel == MOUSE_ICON_LEVEL_UP);
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniLevelUp ()) {
			UtilsGL.unsetColor ();
		}
		iCurrentTexture = UtilsGL.setTexture (tileIconLevelDown, iCurrentTexture);
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniLevelDown ()) {
			UtilsGL.setColorRed ();
		}
		drawTile (tileIconLevelDown, iconLevelDownPoint, ICON_WIDTH, ICON_HEIGHT, mousePanel == MOUSE_ICON_LEVEL_DOWN);
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniLevelDown ()) {
			UtilsGL.unsetColor ();
		}
		iCurrentTexture = UtilsGL.setTexture (tileIconLevel, iCurrentTexture);
		drawTile (tileIconLevel, iconLevelPoint);

		// Num citizens / soldiers / heroes / caravan
		iCurrentTexture = UtilsGL.setTexture (tileBottomItem, iCurrentTexture);
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniCitizens ()) {
			UtilsGL.setColorRed ();
		}
		drawTile (tileBottomItem, iconNumCitizensBackgroundPoint);
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniCitizens ()) {
			UtilsGL.unsetColor ();
		}
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniSoldiers ()) {
			UtilsGL.setColorRed ();
		}
		drawTile (tileBottomItem, iconNumSoldiersBackgroundPoint);
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniSoldiers ()) {
			UtilsGL.unsetColor ();
		}
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniHeroes ()) {
			UtilsGL.setColorRed ();
		}
		drawTile (tileBottomItem, iconNumHeroesBackgroundPoint);
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniHeroes ()) {
			UtilsGL.unsetColor ();
		}
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniTrade ()) {
			UtilsGL.setColorRed ();
		}
		drawTile (tileBottomItem, iconCaravanBackgroundPoint);
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniTrade ()) {
			UtilsGL.unsetColor ();
		}
		iCurrentTexture = UtilsGL.setTexture (tileIconNumCitizens, iCurrentTexture);
		drawTile (tileIconNumCitizens, iconNumCitizensPoint, (mousePanel == MOUSE_INFO_NUM_CITIZENS));
		iCurrentTexture = UtilsGL.setTexture (tileIconNumSoldiers, iCurrentTexture);
		drawTile (tileIconNumSoldiers, iconNumSoldiersPoint, (mousePanel == MOUSE_INFO_NUM_SOLDIERS));
		iCurrentTexture = UtilsGL.setTexture (tileIconNumHeroes, iCurrentTexture);
		drawTile (tileIconNumHeroes, iconNumHeroesPoint, (mousePanel == MOUSE_INFO_NUM_HEROES));
		if (Game.getWorld ().getCurrentCaravanData () != null) {
			iCurrentTexture = UtilsGL.setTexture (tileIconCaravanON, iCurrentTexture);
			drawTile (tileIconCaravanON, iconCaravanPoint, (mousePanel == MOUSE_INFO_CARAVAN));
		} else {
			iCurrentTexture = UtilsGL.setTexture (tileIconCaravan, iCurrentTexture);
			drawTile (tileIconCaravan, iconCaravanPoint, (mousePanel == MOUSE_INFO_CARAVAN));
		}

		// Previous/next citizen/soldiers/heroes
		iCurrentTexture = UtilsGL.setTexture (tileIconCitizenPrevious, iCurrentTexture);
		if (mousePanel == MOUSE_ICON_CITIZEN_PREVIOUS) {
			drawTile (tileIconCitizenPreviousON, iconCitizenPreviousPoint);
		} else {
			drawTile (tileIconCitizenPrevious, iconCitizenPreviousPoint);
		}
		iCurrentTexture = UtilsGL.setTexture (tileIconCitizenNext, iCurrentTexture);
		if (mousePanel == MOUSE_ICON_CITIZEN_NEXT) {
			drawTile (tileIconCitizenNextON, iconCitizenNextPoint);
		} else {
			drawTile (tileIconCitizenNext, iconCitizenNextPoint);
		}
		iCurrentTexture = UtilsGL.setTexture (tileIconSoldierPrevious, iCurrentTexture);
		if (mousePanel == MOUSE_ICON_SOLDIER_PREVIOUS) {
			drawTile (tileIconSoldierPreviousON, iconSoldierPreviousPoint);
		} else {
			drawTile (tileIconSoldierPrevious, iconSoldierPreviousPoint);
		}
		iCurrentTexture = UtilsGL.setTexture (tileIconSoldierNext, iCurrentTexture);
		if (mousePanel == MOUSE_ICON_SOLDIER_NEXT) {
			drawTile (tileIconSoldierNextON, iconSoldierNextPoint);
		} else {
			drawTile (tileIconSoldierNext, iconSoldierNextPoint);
		}
		iCurrentTexture = UtilsGL.setTexture (tileIconHeroPrevious, iCurrentTexture);
		if (mousePanel == MOUSE_ICON_HERO_PREVIOUS) {
			drawTile (tileIconHeroPreviousON, iconHeroPreviousPoint);
		} else {
			drawTile (tileIconHeroPrevious, iconHeroPreviousPoint);
		}
		iCurrentTexture = UtilsGL.setTexture (tileIconHeroNext, iCurrentTexture);
		if (mousePanel == MOUSE_ICON_HERO_NEXT) {
			drawTile (tileIconHeroNextON, iconHeroNextPoint);
		} else {
			drawTile (tileIconHeroNext, iconHeroNextPoint);
		}

		// Panel icons (priorities, mats)
		iCurrentTexture = UtilsGL.setTexture (isMatsPanelActive () ? tileIconMatsON : tileIconMats, iCurrentTexture);
		drawTile (isMatsPanelActive () ? tileIconMatsON : tileIconMats, iconMatsPoint, (mousePanel == MOUSE_ICON_MATS));
		iCurrentTexture = UtilsGL.setTexture (isPrioritiesPanelActive () ? tileIconPrioritiesON : tileIconPriorities, iCurrentTexture);
		drawTile (isPrioritiesPanelActive () ? tileIconPrioritiesON : tileIconPriorities, iconPrioritiesPoint, (mousePanel == MOUSE_ICON_PRIORITIES));

		// Coins
		String sTownCoins = Game.getWorld ().getCoinsString ();
		int iTownsCoinsWidth = UtilFont.getWidth (sTownCoins);
		iCurrentTexture = UtilsGL.setTexture (tileIconCoins, iCurrentTexture);
		drawTile (tileIconCoins, tileIconCoinsPoint.x - iTownsCoinsWidth / 2 - tileIconCoins.getTileWidth () / 2, tileIconCoinsPoint.y, false);

		// Icons (miniblock + grid + settings + flat mouse + 3d mouse)
		iCurrentTexture = UtilsGL.setTexture (MainPanel.bMiniBlocksON ? tileIconMiniblocksON : tileIconMiniblocks, iCurrentTexture);
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniFlat ()) {
			UtilsGL.setColorRed ();
		}
		drawTile (MainPanel.bMiniBlocksON ? tileIconMiniblocksON : tileIconMiniblocks, iconMiniblocksPoint, (mousePanel == MOUSE_ICON_MINIBLOCKS));
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniFlat ()) {
			UtilsGL.unsetColor ();
		}
		iCurrentTexture = UtilsGL.setTexture (MainPanel.gridON ? tileIconGridON : tileIconGrid, iCurrentTexture);
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniGrid ()) {
			UtilsGL.setColorRed ();
		}
		drawTile (MainPanel.gridON ? tileIconGridON : tileIconGrid, iconGridPoint, (mousePanel == MOUSE_ICON_GRID));
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniGrid ()) {
			UtilsGL.unsetColor ();
		}
		iCurrentTexture = UtilsGL.setTexture (tileIconSettings, iCurrentTexture);
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniSettings ()) {
			UtilsGL.setColorRed ();
		}
		drawTile (tileIconSettings, iconSettingsPoint, (mousePanel == MOUSE_ICON_SETTINGS));
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniSettings ()) {
			UtilsGL.unsetColor ();
		}
		iCurrentTexture = UtilsGL.setTexture (tileIconFlatMouse, iCurrentTexture);
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniFlatCursor ()) {
			UtilsGL.setColorRed ();
		}
		drawTile (MainPanel.flatMouseON ? tileIconFlatMouseON : tileIconFlatMouse, iconFlatMousePoint, (mousePanel == MOUSE_ICON_FLATMOUSE));
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniFlatCursor ()) {
			UtilsGL.unsetColor ();
		}
		iCurrentTexture = UtilsGL.setTexture (tileIcon3DMouse, iCurrentTexture);
		if (tutorialFlow != null && tutorialFlow.isBlinkMini3DMouse ()) {
			UtilsGL.setColorRed ();
		}
		drawTile (MainPanel.tDMouseON ? tileIcon3DMouseON : tileIcon3DMouse, icon3DMousePoint, (mousePanel == MOUSE_ICON_3DMOUSE));
		if (tutorialFlow != null && tutorialFlow.isBlinkMini3DMouse ()) {
			UtilsGL.unsetColor ();
		}

		// Message icons
		if (getMessagesPanelActive () == MessagesPanel.TYPE_ANNOUNCEMENT || (MessagesPanel.getBlink ()[MessagesPanel.TYPE_ANNOUNCEMENT] && blinkTurns >= MAX_BLINK_TURNS / 2)) {
			iCurrentTexture = UtilsGL.setTexture (messageTilesON[0], iCurrentTexture);
			drawTile (messageTilesON[0], messageIconPoints[0], (mousePanel == MOUSE_MESSAGES_ICON_ANNOUNCEMENT));
		} else {
			iCurrentTexture = UtilsGL.setTexture (messageTiles[0], iCurrentTexture);
			drawTile (messageTiles[0], messageIconPoints[0], (mousePanel == MOUSE_MESSAGES_ICON_ANNOUNCEMENT));
		}
		if (getMessagesPanelActive () == MessagesPanel.TYPE_COMBAT || (MessagesPanel.getBlink ()[MessagesPanel.TYPE_COMBAT] && blinkTurns >= MAX_BLINK_TURNS / 2)) {
			iCurrentTexture = UtilsGL.setTexture (messageTilesON[1], iCurrentTexture);
			drawTile (messageTilesON[1], messageIconPoints[1], (mousePanel == MOUSE_MESSAGES_ICON_COMBAT));
		} else {
			iCurrentTexture = UtilsGL.setTexture (messageTiles[1], iCurrentTexture);
			drawTile (messageTiles[1], messageIconPoints[1], (mousePanel == MOUSE_MESSAGES_ICON_COMBAT));
		}
		if (getMessagesPanelActive () == MessagesPanel.TYPE_HEROES || (MessagesPanel.getBlink ()[MessagesPanel.TYPE_HEROES] && blinkTurns >= MAX_BLINK_TURNS / 2)) {
			iCurrentTexture = UtilsGL.setTexture (messageTilesON[2], iCurrentTexture);
			drawTile (messageTilesON[2], messageIconPoints[2], (mousePanel == MOUSE_MESSAGES_ICON_HEROES));
		} else {
			iCurrentTexture = UtilsGL.setTexture (messageTiles[2], iCurrentTexture);
			drawTile (messageTiles[2], messageIconPoints[2], (mousePanel == MOUSE_MESSAGES_ICON_HEROES));
		}
		if (getMessagesPanelActive () == MessagesPanel.TYPE_SYSTEM || (MessagesPanel.getBlink ()[MessagesPanel.TYPE_SYSTEM] && blinkTurns >= MAX_BLINK_TURNS / 2)) {
			iCurrentTexture = UtilsGL.setTexture (messageTilesON[3], iCurrentTexture);
			drawTile (messageTilesON[3], messageIconPoints[3], (mousePanel == MOUSE_MESSAGES_ICON_SYSTEM));
		} else {
			iCurrentTexture = UtilsGL.setTexture (messageTiles[3], iCurrentTexture);
			drawTile (messageTiles[3], messageIconPoints[3], (mousePanel == MOUSE_MESSAGES_ICON_SYSTEM));
		}

		// Events icon
		iCurrentTexture = UtilsGL.setTexture (GlobalEventData.getIcon (), iCurrentTexture);
		drawTile (GlobalEventData.getIcon (), iconEventsPoint, false);

		// Gods icon
		// if (TownsProperties.GODS_ACTIVATED) {
		// iCurrentTexture = UtilsGL.setTexture (tileIconGods, iCurrentTexture);
		// drawTile (tileIconGods, iconGodsPoint, false);
		// }

		// (speed down, pause/play, speed up)
		if (World.SPEED > 1) {
			iCurrentTexture = UtilsGL.setTexture (tileIconLowerSpeedON, iCurrentTexture);
			if (tutorialFlow != null && tutorialFlow.isBlinkMiniSpeedDown ()) {
				UtilsGL.setColorRed ();
			}
			drawTile (tileIconLowerSpeedON, iconLowerSpeedPoint, (mousePanel == MOUSE_ICON_LOWER_SPEED));
		} else {
			iCurrentTexture = UtilsGL.setTexture (tileIconLowerSpeed, iCurrentTexture);
			if (tutorialFlow != null && tutorialFlow.isBlinkMiniSpeedDown ()) {
				UtilsGL.setColorRed ();
			}
			drawTile (tileIconLowerSpeed, iconLowerSpeedPoint, false);
		}
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniSpeedDown ()) {
			UtilsGL.unsetColor ();
		}
		if (Game.isPaused ()) {
			iCurrentTexture = UtilsGL.setTexture (tileIconResume, iCurrentTexture);
			if (tutorialFlow != null && tutorialFlow.isBlinkMiniPause ()) {
				UtilsGL.setColorRed ();
			}
			drawTile (tileIconResume, iconPauseResumePoint, (mousePanel == MOUSE_ICON_PAUSE_RESUME));
		} else {
			iCurrentTexture = UtilsGL.setTexture (tileIconPause, iCurrentTexture);
			if (tutorialFlow != null && tutorialFlow.isBlinkMiniPause ()) {
				UtilsGL.setColorRed ();
			}
			drawTile (tileIconPause, iconPauseResumePoint, (mousePanel == MOUSE_ICON_PAUSE_RESUME));
		}
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniPause ()) {
			UtilsGL.unsetColor ();
		}
		if (World.SPEED < World.SPEED_MAX) {
			iCurrentTexture = UtilsGL.setTexture (tileIconIncreaseSpeedON, iCurrentTexture);
			if (tutorialFlow != null && tutorialFlow.isBlinkMiniSpeedUp ()) {
				UtilsGL.setColorRed ();
			}
			drawTile (tileIconIncreaseSpeedON, iconIncreaseSpeedPoint, (mousePanel == MOUSE_ICON_INCREASE_SPEED));
		} else {
			iCurrentTexture = UtilsGL.setTexture (tileIconIncreaseSpeed, iCurrentTexture);
			if (tutorialFlow != null && tutorialFlow.isBlinkMiniSpeedUp ()) {
				UtilsGL.setColorRed ();
			}
			drawTile (tileIconIncreaseSpeed, iconIncreaseSpeedPoint, false);
		}
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniSpeedUp ()) {
			UtilsGL.unsetColor ();
		}

		UtilsGL.glEnd ();

		// Date
		String sDate = Game.getWorld ().getDate ().toString ();
		int dateW = UtilFont.getWidth (sDate);
		int iLevel = World.MAP_NUM_LEVELS_OUTSIDE - Game.getWorld ().getView ().z;
		String sLevel = Integer.toString (iLevel);
		int sLevelW = UtilFont.getWidth (sLevel);
		String sNumCitizens = Integer.toString (World.getNumCitizens ());
		int numCitizensW = UtilFont.getWidth (sNumCitizens);
		String sNumSoldiers = Integer.toString (World.getNumSoldiers ());
		int numSoldiersW = UtilFont.getWidth (sNumSoldiers);
		String sNumHeroes = Integer.toString (World.getNumHeroes ());
		int numHeroesW = UtilFont.getWidth (sNumHeroes);

		GL11.glBindTexture (GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
		GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		UtilsGL.glBegin (GL11.GL_QUADS);
		UtilsGL.drawString (sLevel, iconLevelPoint.x + tileIconLevel.getTileWidth () / 2 - sLevelW / 2, iconLevelPoint.y + tileIconLevel.getTileHeight () / 2 - UtilFont.MAX_HEIGHT / 2, ColorGL.BLACK);
		UtilsGL.drawString (sNumCitizens, iconNumCitizensPoint.x + tileIconNumCitizens.getTileWidth () / 2 - numCitizensW / 2, iconNumCitizensPoint.y + tileIconNumCitizens.getTileHeight (), ColorGL.BLACK);
		UtilsGL.drawString (sNumSoldiers, iconNumSoldiersPoint.x + tileIconNumSoldiers.getTileWidth () / 2 - numSoldiersW / 2, iconNumSoldiersPoint.y + tileIconNumSoldiers.getTileHeight (), ColorGL.BLACK);
		UtilsGL.drawString (sNumHeroes, iconNumHeroesPoint.x + tileIconNumHeroes.getTileWidth () / 2 - numHeroesW / 2, iconNumHeroesPoint.y + tileIconNumHeroes.getTileHeight (), ColorGL.BLACK);
		UtilsGL.drawString (sDate, datePanelPoint.x + tileDatePanel.getTileWidth () / 2 - dateW / 2, datePanelPoint.y + tileDatePanel.getTileHeight () / 2 - UtilFont.MAX_HEIGHT / 2, ColorGL.BLACK);
		UtilsGL.drawString (sTownCoins, tileIconCoinsPoint.x - iTownsCoinsWidth / 2 + tileIconCoins.getTileWidth () / 2, tileIconCoinsPoint.y + tileIconCoins.getTileHeight () / 2 - UtilFont.MAX_HEIGHT / 2, ColorGL.WHITE);

		if (TownsProperties.DEBUG_MODE) {
			// Global events
			GlobalEventData ged = Game.getWorld ().getGlobalEvents ();
			UtilsGL.drawStringWithBorder ("Shadows " + ged.isShadows (), 2, 3 * UtilFont.MAX_HEIGHT, ColorGL.WHITE, ColorGL.BLACK); //$NON-NLS-1$
			UtilsGL.drawStringWithBorder ("Half shadows " + ged.isHalfShadows (), 2, 4 * UtilFont.MAX_HEIGHT, ColorGL.WHITE, ColorGL.BLACK); //$NON-NLS-1$
			UtilsGL.drawStringWithBorder ("RGB " + ged.getRed () + "," + ged.getGreen () + "," + ged.getBlue (), 2, 5 * UtilFont.MAX_HEIGHT, ColorGL.WHITE, ColorGL.BLACK); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			UtilsGL.drawStringWithBorder ("waitPCT " + ged.getWaitPCT (), 2, 6 * UtilFont.MAX_HEIGHT, ColorGL.WHITE, ColorGL.BLACK); //$NON-NLS-1$
			UtilsGL.drawStringWithBorder ("walkSpeedPCT " + ged.getWalkSpeedPCT (), 2, 7 * UtilFont.MAX_HEIGHT, ColorGL.WHITE, ColorGL.BLACK); //$NON-NLS-1$

			// Events
			StringBuffer sbEvents = new StringBuffer ("Events: "); //$NON-NLS-1$
			for (int e = 0; e < Game.getWorld ().getEvents ().size (); e++) {
				sbEvents.append (Game.getWorld ().getEvents ().get (e).getEventID ());
				sbEvents.append (" ("); //$NON-NLS-1$
				sbEvents.append (Game.getWorld ().getEvents ().get (e).getTurns ());
				sbEvents.append (")"); //$NON-NLS-1$
				sbEvents.append (", "); //$NON-NLS-1$
			}

			UtilsGL.drawStringWithBorder (sbEvents.toString (), 2, 2 + 9 * UtilFont.MAX_HEIGHT, ColorGL.WHITE, ColorGL.BLACK);

			// Gods
			//			sbEvents = new StringBuffer ("Gods: "); //$NON-NLS-1$
			// for (int e = 0; e < Game.getWorld ().getGods ().size (); e++) {
			// sbEvents.append (Game.getWorld ().getGods ().get (e).getGodID ());
			//				sbEvents.append (" ("); //$NON-NLS-1$
			// sbEvents.append (Game.getWorld ().getGods ().get (e).getStatus ());
			//				sbEvents.append (")"); //$NON-NLS-1$
			//				sbEvents.append (", "); //$NON-NLS-1$
			// }
			//
			// UtilsGL.drawStringWithBorder (sbEvents.toString (), 2, 2 + 10 * UtilFont.MAX_HEIGHT, ColorGL.WHITE, ColorGL.BLACK);
		}

		UtilsGL.glEnd ();

		// Task
		renderTask ();

		// Tutorial button
		renderTutorialButton (mousePanel, blinkTurns >= MAX_BLINK_TURNS / 2);

		/*
		 * PANELS, PRODUCTION PANEL, PRIORITIES PANEL, TRADE_PANEL (este va encima de todo siempre)
		 */
		renderProductionPanel (mouseX, mouseY, mousePanel);

		if (isPilePanelActive ()) {
			renderPilePanel (mouseX, mouseY, mousePanel);
		}
		if (isLivingsPanelActive ()) {
			renderLivingsPanel (mouseX, mouseY, mousePanel);
		}
		if (isProfessionsPanelActive ()) {
			renderProfessionsPanel (mouseX, mouseY, mousePanel);
		}
		if (isMatsPanelActive ()) {
			renderMatsPanel (mouseX, mouseY, mousePanel);
		}
		if (isPrioritiesPanelActive ()) {
			renderPrioritiesPanel (mouseX, mouseY, mousePanel);
		}
		if (isTradePanelActive ()) {
			renderTradePanel (mouseX, mouseY, mousePanel);
		}
		if (isMessagesPanelActive ()) {
			renderMessagesPanel (mouseX, mouseY, mousePanel);
		}

		// Al final de todo el Typing panel si hace falta
		if (typingPanel != null) {
			TypingPanel.render (mousePanel);
		}

		// Al final de todo el Images panel si hace falta
		if (imagesPanel != null && ImagesPanel.isVisible ()) {
			ImagesPanel.render (mousePanel);
		}

		// Tooltip
		renderTooltip (mouseX, mouseY, mousePanel);
	}


	private int renderBottomMenuPanel (int mouseX, int mouseY, int mousePanel, int iCurrentTexture) {
		/*
		 * BOTTOM PANEL
		 */
		// Left scroll
		if (mousePanel == MOUSE_BOTTOM_LEFT_SCROLL && bottomPanelItemIndex > 0) {
			UtilsGL.drawTexture (bottomPanelLeftScrollX, bottomPanelY, bottomPanelLeftScrollX + BOTTOM_PANEL_SCROLL_WIDTH, bottomPanelY + BOTTOM_PANEL_HEIGHT, tileBottomScrollLeftON.getTileSetTexX0 (), tileBottomScrollLeftON.getTileSetTexY0 (), tileBottomScrollLeftON.getTileSetTexX1 (), tileBottomScrollLeftON.getTileSetTexY1 ());
		} else {
			UtilsGL.drawTexture (bottomPanelLeftScrollX, bottomPanelY, bottomPanelLeftScrollX + BOTTOM_PANEL_SCROLL_WIDTH, bottomPanelY + BOTTOM_PANEL_HEIGHT, tileBottomScrollLeft.getTileSetTexX0 (), tileBottomScrollLeft.getTileSetTexY0 (), tileBottomScrollLeft.getTileSetTexX1 (), tileBottomScrollLeft.getTileSetTexY1 ());
		}

		// Right scroll
		iCurrentTexture = UtilsGL.setTexture (tileBottomScrollRight, iCurrentTexture);
		if (mousePanel == MOUSE_BOTTOM_RIGHT_SCROLL && (bottomPanelItemIndex + BOTTOM_PANEL_NUM_ITEMS) < currentMenu.getItems ().size ()) {
			UtilsGL.drawTexture (bottomPanelRightScrollX, bottomPanelY, bottomPanelRightScrollX + BOTTOM_PANEL_SCROLL_WIDTH, bottomPanelY + BOTTOM_PANEL_HEIGHT, tileBottomScrollRightON.getTileSetTexX0 (), tileBottomScrollRightON.getTileSetTexY0 (), tileBottomScrollRightON.getTileSetTexX1 (), tileBottomScrollRightON.getTileSetTexY1 ());
		} else {
			UtilsGL.drawTexture (bottomPanelRightScrollX, bottomPanelY, bottomPanelRightScrollX + BOTTOM_PANEL_SCROLL_WIDTH, bottomPanelY + BOTTOM_PANEL_HEIGHT, tileBottomScrollRight.getTileSetTexX0 (), tileBottomScrollRight.getTileSetTexY0 (), tileBottomScrollRight.getTileSetTexX1 (), tileBottomScrollRight.getTileSetTexY1 ());
		}

		// Panel itself
		iCurrentTexture = UtilsGL.setTexture (tileBottomPanel, iCurrentTexture);
		UtilsGL.drawTexture (bottomPanelX, bottomPanelY, bottomPanelX + BOTTOM_PANEL_WIDTH, bottomPanelY + BOTTOM_PANEL_HEIGHT, tileBottomPanel.getTileSetTexX0 (), tileBottomPanel.getTileSetTexY0 (), tileBottomPanel.getTileSetTexX1 (), tileBottomPanel.getTileSetTexY1 ());

		// BOTTOM PANEL Items
		int iItemBottomPanel;
		if (mousePanel == MOUSE_BOTTOM_ITEMS) {
			iItemBottomPanel = isMouseOnBottomItems (mouseX, mouseY);
		} else {
			iItemBottomPanel = -1;
		}

		// UI TEXTURE bottom panel
		Point point;
		for (int i = bottomPanelItemIndex; i < bottomPanelItemIndex + BOTTOM_PANEL_NUM_ITEMS; i++) {
			if (i > currentMenu.getItems ().size ()) {
				break;
			}

			point = bottomPanelItemsPosition.get (i - bottomPanelItemIndex);

			// Round button
			if (currentMenu.getItems ().get (i).getType () == SmartMenu.TYPE_MENU) {
				iCurrentTexture = UtilsGL.setTexture (tileBottomItemSM, iCurrentTexture);
				if (checkBlinkBottom && TutorialFlow.currentBlinkBottom (currentMenu.getItems ().get (i).getID ())) {
					UtilsGL.setColorRed ();
					drawTile (tileBottomItemSM, point, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT, (iItemBottomPanel == (i - bottomPanelItemIndex)));
					UtilsGL.unsetColor ();
				} else {
					drawTile (tileBottomItemSM, point, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT, (iItemBottomPanel == (i - bottomPanelItemIndex)));
				}
			} else {
				iCurrentTexture = UtilsGL.setTexture (tileBottomItem, iCurrentTexture);
				if (checkBlinkBottom && TutorialFlow.currentBlinkBottom (currentMenu.getItems ().get (i).getID ())) {
					UtilsGL.setColorRed ();
					drawTile (tileBottomItem, point, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT, (iItemBottomPanel == (i - bottomPanelItemIndex)));
					UtilsGL.unsetColor ();
				} else {
					drawTile (tileBottomItem, point, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT, (iItemBottomPanel == (i - bottomPanelItemIndex)));
				}
			}

			// Icono
			Tile tile = currentMenu.getItems ().get (i).getIcon ();
			if (tile != null && currentMenu.getItems ().get (i).getIconType () == SmartMenu.ICON_TYPE_UI) {
				iCurrentTexture = UtilsGL.setTexture (tile, iCurrentTexture);
				drawTile (tile, point, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT, (iItemBottomPanel == (i - bottomPanelItemIndex)));
			}
		}

		/*
		 * BOTTOM SUBPANEL
		 */
		int iItemBottomSubPanel;
		if (mousePanel == MOUSE_BOTTOM_SUBITEMS) {
			iItemBottomSubPanel = isMouseOnBottomSubItems (mouseX, mouseY);
		} else {
			iItemBottomSubPanel = -1;
		}
		if (bottomSubPanelMenu != null) {
			// Pintamos el panel
			iCurrentTexture = UtilsGL.setTexture (tileBottomSubPanel[0], iCurrentTexture);
			renderBackground (tileBottomSubPanel, bottomSubPanelPoint, BOTTOM_SUBPANEL_WIDTH, BOTTOM_SUBPANEL_HEIGHT);
			// UtilsGL.drawTexture (bottomSubPanelX, bottomSubPanelY, bottomSubPanelX + BOTTOM_SUBPANEL_WIDTH, bottomSubPanelY + BOTTOM_SUBPANEL_HEIGHT, tileBottomSubPanel.getTileSetTexX0 (), tileBottomSubPanel.getTileSetTexY0 (), tileBottomSubPanel.getTileSetTexX1 (), tileBottomSubPanel.getTileSetTexY1 ());

			// Pintamos los items
			int iMenu;
			bucle1: for (int y = 0; y < BOTTOM_SUBPANEL_NUM_ITEMS_Y; y++) {
				for (int x = 0; x < BOTTOM_SUBPANEL_NUM_ITEMS_X; x++) {
					iMenu = (y * BOTTOM_SUBPANEL_NUM_ITEMS_X) + x;
					if (iMenu >= bottomSubPanelMenu.getItems ().size ()) {
						break bucle1;
					}

					point = bottomSubPanelItemsPosition.get (iMenu);
					// Round button
					if (bottomSubPanelMenu.getItems ().get (iMenu).getType () == SmartMenu.TYPE_MENU) {
						iCurrentTexture = UtilsGL.setTexture (tileBottomItemSM, iCurrentTexture);
						if (checkBlinkBottom && TutorialFlow.currentBlinkBottom (bottomSubPanelMenu.getItems ().get (iMenu).getID ())) {
							UtilsGL.setColorRed ();
							drawTile (tileBottomItemSM, point, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT, (iItemBottomSubPanel == iMenu));
							UtilsGL.unsetColor ();
						} else {
							drawTile (tileBottomItemSM, point, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT, (iItemBottomSubPanel == iMenu));
						}
					} else {
						iCurrentTexture = UtilsGL.setTexture (tileBottomItem, iCurrentTexture);
						if (checkBlinkBottom && TutorialFlow.currentBlinkBottom (bottomSubPanelMenu.getItems ().get (iMenu).getID ())) {
							UtilsGL.setColorRed ();
							drawTile (tileBottomItem, point, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT, (iItemBottomSubPanel == iMenu));
							UtilsGL.unsetColor ();
						} else {
							drawTile (tileBottomItem, point, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT, (iItemBottomSubPanel == iMenu));
						}
					}

					// Icono
					Tile tile = bottomSubPanelMenu.getItems ().get (iMenu).getIcon ();
					if (tile != null && bottomSubPanelMenu.getItems ().get (iMenu).getIconType () == SmartMenu.ICON_TYPE_UI) {
						iCurrentTexture = UtilsGL.setTexture (tile, iCurrentTexture);
						drawTile (tile, point, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT, (iItemBottomSubPanel == iMenu));
					}
				}
			}
		}

		/*
		 * ITEMS
		 */
		// BOTTOM PANEL
		for (int i = bottomPanelItemIndex; i < bottomPanelItemIndex + BOTTOM_PANEL_NUM_ITEMS; i++) {
			if (i > currentMenu.getItems ().size ()) {
				break;
			}

			point = bottomPanelItemsPosition.get (i - bottomPanelItemIndex);
			// Icono
			Tile tile = currentMenu.getItems ().get (i).getIcon ();
			if (tile != null && currentMenu.getItems ().get (i).getIconType () == SmartMenu.ICON_TYPE_ITEM) {
				iCurrentTexture = UtilsGL.setTexture (tile, iCurrentTexture);
				drawTile (tile, point, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT, (iItemBottomPanel == (i - bottomPanelItemIndex)));
			}
		}

		// BOTTOM SUBPANEL
		if (bottomSubPanelMenu != null) {
			// Ahora los items
			int iMenu;
			Tile tile;
			bucle1: for (int y = 0; y < BOTTOM_SUBPANEL_NUM_ITEMS_Y; y++) {
				for (int x = 0; x < BOTTOM_SUBPANEL_NUM_ITEMS_X; x++) {
					iMenu = (y * BOTTOM_SUBPANEL_NUM_ITEMS_X) + x;
					if (iMenu >= bottomSubPanelMenu.getItems ().size ()) {
						break bucle1;
					}

					point = bottomSubPanelItemsPosition.get (iMenu);
					// Icono
					tile = bottomSubPanelMenu.getItems ().get (iMenu).getIcon ();
					if (tile != null && bottomSubPanelMenu.getItems ().get (iMenu).getIconType () == SmartMenu.ICON_TYPE_ITEM) {
						iCurrentTexture = UtilsGL.setTexture (tile, iCurrentTexture);
						drawTile (tile, point, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT, (iItemBottomSubPanel == iMenu));
					}
				}
			}
		}

		return iCurrentTexture;
	}


	private void renderMenuPanel (int mouseX, int mouseY, int mousePanel) {
		checkBlinkRight = (blinkTurns >= MAX_BLINK_TURNS / 2) && TutorialFlow.isBlinkRight ();

		if (isMenuPanelActive ()) {
			// XAVI GL11.glColor4f (1, 1, 1, 1);
			int iCurrentTexture = tileMenuPanel[0].getTextureID ();
			GL11.glBindTexture (GL11.GL_TEXTURE_2D, iCurrentTexture);
			GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
			UtilsGL.glBegin (GL11.GL_QUADS);
			renderBackground (tileMenuPanel, menuPanelPoint, MENU_PANEL_WIDTH, MENU_PANEL_HEIGHT);

			int iItemMenu;
			if (mousePanel == MOUSE_MENU_PANEL_ITEMS) {
				iItemMenu = isMouseOnMenuItems (mouseX, mouseY);
			} else {
				iItemMenu = -1;
			}

			// Items
			if (menuPanelMenu != null) {
				int iMenu;
				Point point;
				bucle1: for (int y = 0; y < MENU_PANEL_NUM_ITEMS_Y; y++) {
					for (int x = 0; x < MENU_PANEL_NUM_ITEMS_X; x++) {
						iMenu = (y * MENU_PANEL_NUM_ITEMS_X) + x;
						if (iMenu >= menuPanelMenu.getItems ().size ()) {
							break bucle1;
						}
						point = menuPanelItemsPosition.get (iMenu);

						// Round button
						if (menuPanelMenu.getItems ().get (iMenu).getType () == SmartMenu.TYPE_MENU) {
							iCurrentTexture = UtilsGL.setTexture (tileBottomItemSM, iCurrentTexture);
							if (checkBlinkRight && TutorialFlow.currentBlinkRight (menuPanelMenu.getItems ().get (iMenu).getID ())) {
								UtilsGL.setColorRed ();
								drawTile (tileBottomItemSM, point, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT, (iItemMenu == iMenu));
								UtilsGL.unsetColor ();
							} else {
								drawTile (tileBottomItemSM, point, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT, (iItemMenu == iMenu));
							}
						} else {
							iCurrentTexture = UtilsGL.setTexture (tileBottomItem, iCurrentTexture);

							if (checkBlinkRight && TutorialFlow.currentBlinkRight (menuPanelMenu.getItems ().get (iMenu).getID ())) {
								UtilsGL.setColorRed ();
								drawTile (tileBottomItem, point, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT, (iItemMenu == iMenu));
								UtilsGL.unsetColor ();
							} else {
								drawTile (tileBottomItem, point, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT, (iItemMenu == iMenu));
							}
						}

						// Icono
						Tile tile = menuPanelMenu.getItems ().get (iMenu).getIcon ();
						if (tile != null && menuPanelMenu.getItems ().get (iMenu).getIconType () == SmartMenu.ICON_TYPE_UI) { // MENU
							iCurrentTexture = UtilsGL.setTexture (tile, iCurrentTexture);
							drawTile (tile, point, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT, (iItemMenu == iMenu));
						}
					}
				}
			}

			// MENU
			if (menuPanelMenu != null) {
				int iMenu;
				Tile tile;
				Point point;
				bucle1: for (int y = 0; y < MENU_PANEL_NUM_ITEMS_Y; y++) {
					for (int x = 0; x < MENU_PANEL_NUM_ITEMS_X; x++) {
						iMenu = (y * MENU_PANEL_NUM_ITEMS_X) + x;
						if (iMenu >= menuPanelMenu.getItems ().size ()) {
							break bucle1;
						}
						point = menuPanelItemsPosition.get (iMenu);
						// Icono
						tile = menuPanelMenu.getItems ().get (iMenu).getIcon ();
						if (tile != null && menuPanelMenu.getItems ().get (iMenu).getIconType () == SmartMenu.ICON_TYPE_ITEM) { // ICONO
							iCurrentTexture = UtilsGL.setTexture (tile, iCurrentTexture);
							drawTile (tile, point, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT, (iItemMenu == iMenu));
						}
					}
				}
			}

			UtilsGL.glEnd ();
		}

		// Botoncito open/close
		if (isMenuPanelLocked ()) {
			// Close menu icon
			// XAVI GL11.glColor4f (1, 1, 1, 1);
			GL11.glBindTexture (GL11.GL_TEXTURE_2D, tileOpenRightMenuON.getTextureID ());
			GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
			UtilsGL.glBegin (GL11.GL_QUADS);
			drawTile (tileOpenRightMenuON, tileOpenCloseRightMenuPoint, tileOpenRightMenuON.getTileWidth (), tileOpenRightMenuON.getTileHeight (), mousePanel == MOUSE_MENU_OPENCLOSE);
			UtilsGL.glEnd ();
		} else {
			// XAVI GL11.glColor4f (1, 1, 1, 1);
			GL11.glBindTexture (GL11.GL_TEXTURE_2D, tileOpenRightMenu.getTextureID ());
			GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
			UtilsGL.glBegin (GL11.GL_QUADS);
			if (checkBlinkRight) {
				UtilsGL.setColorRed ();
			}
			drawTile (tileOpenRightMenu, tileOpenCloseRightMenuPoint, tileOpenRightMenu.getTileWidth (), tileOpenRightMenu.getTileHeight (), mousePanel == MOUSE_MENU_OPENCLOSE);
			if (checkBlinkRight) {
				UtilsGL.unsetColor ();
			}
			UtilsGL.glEnd ();
		}
	}


	private void renderProductionPanel (int mouseX, int mouseY, int mousePanel) {
		checkBlinkProduction = (blinkTurns >= MAX_BLINK_TURNS / 2) && TutorialFlow.isBlinkProduction ();

		if (isProductionPanelActive ()) {
			int iCurrentTexture = tileProductionPanel[0].getTextureID ();
			GL11.glBindTexture (GL11.GL_TEXTURE_2D, iCurrentTexture);
			GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
			UtilsGL.glBegin (GL11.GL_QUADS);

			renderBackground (tileProductionPanel, productionPanelPoint, PRODUCTION_PANEL_WIDTH, PRODUCTION_PANEL_HEIGHT);

			// Items
			int iMenu;
			Point point;
			SmartMenu smItem;
			Point pItem;
			if (mousePanel == MOUSE_PRODUCTION_PANEL_ITEMS || mousePanel == MOUSE_PRODUCTION_PANEL_ITEMS_MINUS_AUTOMATED || mousePanel == MOUSE_PRODUCTION_PANEL_ITEMS_MINUS_REGULAR || mousePanel == MOUSE_PRODUCTION_PANEL_ITEMS_PLUS_AUTOMATED || mousePanel == MOUSE_PRODUCTION_PANEL_ITEMS_PLUS_REGULAR) {
				pItem = isMouseOnProductionItems (mouseX, mouseY);
			} else {
				pItem = null;
			}

			if (productionPanelMenu != null) {
				Tile tile;
				bucle1: for (int y = 0; y < PRODUCTION_PANEL_NUM_ITEMS_Y; y++) {
					for (int x = 0; x < PRODUCTION_PANEL_NUM_ITEMS_X; x++) {
						iMenu = (y * PRODUCTION_PANEL_NUM_ITEMS_X) + x;
						if (iMenu >= productionPanelMenu.getItems ().size ()) {
							break bucle1;
						}
						smItem = productionPanelMenu.getItems ().get (iMenu);

						point = productionPanelItemsPosition.get (iMenu);
						boolean bBlinkItem = checkBlinkProduction && TutorialFlow.currentBlinkProduction (productionPanelMenu.getItems ().get (iMenu).getID ());
						TutorialFlow tutFlow = null;
						if (bBlinkItem && Game.getCurrentMissionData () != null && ImagesPanel.getCurrentFlowIndex () >= 0 && ImagesPanel.getCurrentFlowIndex () < Game.getCurrentMissionData ().getTutorialFlows ().size ()) {
							tutFlow = Game.getCurrentMissionData ().getTutorialFlows ().get (ImagesPanel.getCurrentFlowIndex ());
						}

						// Round button
						if (productionPanelMenu.getItems ().get (iMenu).getType () == SmartMenu.TYPE_MENU) {
							iCurrentTexture = UtilsGL.setTexture (tileBottomItemSM, iCurrentTexture);
							if (bBlinkItem) {
								UtilsGL.setColorRed ();
								drawTile (tileBottomItemSM, point, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT, (pItem != null && pItem.x == MOUSE_PRODUCTION_PANEL_ITEMS && pItem.y == iMenu));
								UtilsGL.unsetColor ();
							} else {
								drawTile (tileBottomItemSM, point, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT, (pItem != null && pItem.x == MOUSE_PRODUCTION_PANEL_ITEMS && pItem.y == iMenu));
							}
						} else {
							iCurrentTexture = UtilsGL.setTexture (tileBottomItem, iCurrentTexture);
							if (bBlinkItem) {
								UtilsGL.setColorRed ();
								drawTile (tileBottomItem, point, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT, (pItem != null && pItem.x == MOUSE_PRODUCTION_PANEL_ITEMS && pItem.y == iMenu));
								UtilsGL.unsetColor ();
							} else {
								drawTile (tileBottomItem, point, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT, (pItem != null && pItem.x == MOUSE_PRODUCTION_PANEL_ITEMS && pItem.y == iMenu));
							}
						}

						// Icono
						tile = productionPanelMenu.getItems ().get (iMenu).getIcon ();
						if (tile != null && productionPanelMenu.getItems ().get (iMenu).getIconType () == SmartMenu.ICON_TYPE_UI) {
							iCurrentTexture = UtilsGL.setTexture (tile, iCurrentTexture);
							drawTile (tile, point, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT, (pItem != null && pItem.x == MOUSE_PRODUCTION_PANEL_ITEMS && pItem.y == iMenu));
						}

						point = productionPanelItemsPlusRegularPosition.get (iMenu);
						if (point.x != -1) {
							// Regular
							iCurrentTexture = UtilsGL.setTexture (tileProductionPanelPlusIcon, iCurrentTexture);
							if (tutFlow != null && tutFlow.isBlinkProductionRegularPlus ()) {
								UtilsGL.setColorRed ();
							}
							drawTile (tileProductionPanelPlusIcon, point, ICON_WIDTH, ICON_HEIGHT, (pItem != null && pItem.x == MOUSE_PRODUCTION_PANEL_ITEMS_PLUS_REGULAR && pItem.y == iMenu));
							if (tutFlow != null && tutFlow.isBlinkProductionRegularPlus ()) {
								UtilsGL.unsetColor ();
							}

							// Automated
							if (tutFlow != null && tutFlow.isBlinkProductionAutomatedPlus ()) {
								UtilsGL.setColorRed ();
							}
							drawTile (tileProductionPanelPlusIcon, productionPanelItemsPlusAutomatedPosition.get (iMenu), ICON_WIDTH, ICON_HEIGHT, (pItem != null && pItem.x == MOUSE_PRODUCTION_PANEL_ITEMS_PLUS_AUTOMATED && pItem.y == iMenu));
							if (tutFlow != null && tutFlow.isBlinkProductionAutomatedPlus ()) {
								UtilsGL.unsetColor ();
							}

							iCurrentTexture = UtilsGL.setTexture (tileProductionPanelMinusIcon, iCurrentTexture);

							// Regular
							if (tutFlow != null && tutFlow.isBlinkProductionRegularMinus ()) {
								UtilsGL.setColorRed ();
							}
							drawTile (tileProductionPanelMinusIcon, productionPanelItemsMinusRegularPosition.get (iMenu), ICON_WIDTH, ICON_HEIGHT, (pItem != null && pItem.x == MOUSE_PRODUCTION_PANEL_ITEMS_MINUS_REGULAR && pItem.y == iMenu));
							if (tutFlow != null && tutFlow.isBlinkProductionRegularMinus ()) {
								UtilsGL.unsetColor ();
							}

							// Automated
							if (tutFlow != null && tutFlow.isBlinkProductionAutomatedMinus ()) {
								UtilsGL.setColorRed ();
							}
							drawTile (tileProductionPanelMinusIcon, productionPanelItemsMinusAutomatedPosition.get (iMenu), ICON_WIDTH, ICON_HEIGHT, (pItem != null && pItem.x == MOUSE_PRODUCTION_PANEL_ITEMS_MINUS_AUTOMATED && pItem.y == iMenu));
							if (tutFlow != null && tutFlow.isBlinkProductionAutomatedMinus ()) {
								UtilsGL.unsetColor ();
							}
						}
					}
				}
			}
			UtilsGL.glEnd ();

			/*
			 * ITEMS TEXTURES
			 */
			if (productionPanelMenu != null) {
				iCurrentTexture = Game.TEXTURE_FONT_ID;
				GL11.glBindTexture (GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
				GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
				UtilsGL.glBegin (GL11.GL_QUADS);

				bucle1: for (int y = 0; y < PRODUCTION_PANEL_NUM_ITEMS_Y; y++) {
					for (int x = 0; x < PRODUCTION_PANEL_NUM_ITEMS_X; x++) {
						iMenu = (y * PRODUCTION_PANEL_NUM_ITEMS_X) + x;
						if (iMenu >= productionPanelMenu.getItems ().size ()) {
							break bucle1;
						}
						point = productionPanelItemsPosition.get (iMenu);
						// Icono
						Tile tile = productionPanelMenu.getItems ().get (iMenu).getIcon ();
						if (tile != null && productionPanelMenu.getItems ().get (iMenu).getIconType () == SmartMenu.ICON_TYPE_ITEM) {
							iCurrentTexture = UtilsGL.setTexture (tile, iCurrentTexture);
							drawTile (tile, point, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT, (pItem != null && pItem.x == MOUSE_PRODUCTION_PANEL_ITEMS && pItem.y == iMenu));
						}
					}
				}
				UtilsGL.glEnd ();
			}

			/*
			 * NUMBERS
			 */
			if (productionPanelMenu != null) {
				GL11.glBindTexture (GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
				GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
				UtilsGL.glBegin (GL11.GL_QUADS);

				String strValue;
				HashMap<String, Integer> hmItemsOnQueue = Game.getWorld ().getTaskManager ().getItemsOnRegularQueue ();
				Integer iItemQueue;
				bucle1: for (int y = 0; y < PRODUCTION_PANEL_NUM_ITEMS_Y; y++) {
					for (int x = 0; x < PRODUCTION_PANEL_NUM_ITEMS_X; x++) {
						iMenu = (y * PRODUCTION_PANEL_NUM_ITEMS_X) + x;
						if (iMenu >= productionPanelMenu.getItems ().size ()) {
							break bucle1;
						}
						smItem = productionPanelMenu.getItems ().get (iMenu);
						if (smItem.getType () == SmartMenu.TYPE_ITEM) {
							if (!smItem.getCommand ().equalsIgnoreCase (CommandPanel.COMMAND_BACK)) {
								point = productionPanelItemsPosition.get (iMenu);
								iItemQueue = hmItemsOnQueue.get (smItem.getParameter ());
								if (iItemQueue == null) {
									strValue = "0"; //$NON-NLS-1$
								} else {
									strValue = Integer.toString (iItemQueue);
								}
								// Regular
								UtilsGL.drawStringWithBorder (strValue, point.x - ICON_WIDTH / 2 - (UtilFont.getWidth (strValue)) / 2, point.y + PRODUCTION_PANEL_ITEM_HEIGHT / 2 - UtilFont.MAX_HEIGHT / 2, ColorGL.WHITE, ColorGL.BLACK);

								// Automated
								strValue = Integer.toString (Game.getWorld ().getTaskManager ().getNumItemsOnAutomatedQueue (smItem.getParameter ()));
								UtilsGL.drawStringWithBorder (strValue, point.x + PRODUCTION_PANEL_ITEM_WIDTH + ICON_WIDTH / 2 - (UtilFont.getWidth (strValue)) / 2, point.y + PRODUCTION_PANEL_ITEM_HEIGHT / 2 - UtilFont.MAX_HEIGHT / 2, ColorGL.WHITE, ColorGL.BLACK);

								// Items in world
								ActionManagerItem ami = ActionManager.getItem (smItem.getParameter ());
								if (ami != null && ami.getGeneratedItem () != null) {
									int iNum = Item.getNumItems (UtilsIniHeaders.getIntIniHeader (ami.getGeneratedItem ()), false, World.MAP_DEPTH);
									if (iNum > 0) {
										strValue = Integer.toString (iNum);
										UtilsGL.drawStringWithBorder (strValue, point.x + PRODUCTION_PANEL_ITEM_WIDTH / 2 - (UtilFont.getWidth (strValue)) / 2, point.y + PRODUCTION_PANEL_ITEM_HEIGHT / 4 - UtilFont.MAX_HEIGHT / 2, ColorGL.WHITE, ColorGL.BLACK);
									}
								}
							}
						}
					}
				}

				UtilsGL.glEnd ();
			}
		}

		if (isProductionPanelLocked ()) {
			// Close icon
			// XAVI GL11.glColor4f (1, 1, 1, 1);
			GL11.glBindTexture (GL11.GL_TEXTURE_2D, tileOpenProductionPanelON.getTextureID ());
			GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
			UtilsGL.glBegin (GL11.GL_QUADS);
			drawTile (tileOpenProductionPanelON, tileOpenCloseProductionPanelPoint, tileOpenProductionPanelON.getTileWidth (), tileOpenProductionPanelON.getTileHeight (), mousePanel == MOUSE_PRODUCTION_OPENCLOSE);
			UtilsGL.glEnd ();
		} else {
			// Open icon
			// XAVI GL11.glColor4f (1, 1, 1, 1);
			GL11.glBindTexture (GL11.GL_TEXTURE_2D, tileOpenProductionPanel.getTextureID ());
			GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
			UtilsGL.glBegin (GL11.GL_QUADS);
			if (checkBlinkProduction) {
				UtilsGL.setColorRed ();
			}
			drawTile (tileOpenProductionPanel, tileOpenCloseProductionPanelPoint, tileOpenProductionPanel.getTileWidth (), tileOpenProductionPanel.getTileHeight (), mousePanel == MOUSE_PRODUCTION_OPENCLOSE);
			if (checkBlinkProduction) {
				UtilsGL.unsetColor ();
			}
			UtilsGL.glEnd ();
		}
	}


	/**
	 * Renderiza el background con los 8 tiles de los lados y esquinas 0: background 1: N 2: S 3: E 4: W 5: NE 6: NW 7: SE 8: SW
	 * 
	 * @param tiles
	 */
	public static void renderBackground (Tile[] tiles, Point point, int width, int height) {
		int iEdgeWidth = tiles[6].getTileWidth ();
		int iEdgeHeight = tiles[6].getTileHeight ();

		// Background
		Tile tile = tiles[0];
		UtilsGL.drawTexture (point.x + iEdgeWidth, point.y + iEdgeHeight, point.x + width - iEdgeWidth, point.y + height - iEdgeHeight, tile.getTileSetTexX0 (), tile.getTileSetTexY0 (), tile.getTileSetTexX1 (), tile.getTileSetTexY1 ());

		// N
		tile = tiles[1];
		UtilsGL.drawTexture (point.x + iEdgeWidth, point.y, point.x + width - iEdgeWidth, point.y + iEdgeHeight, tile.getTileSetTexX0 (), tile.getTileSetTexY0 (), tile.getTileSetTexX1 (), tile.getTileSetTexY1 ());

		// S
		tile = tiles[2];
		UtilsGL.drawTexture (point.x + iEdgeWidth, point.y + height - iEdgeHeight, point.x + width - iEdgeWidth, point.y + height, tile.getTileSetTexX0 (), tile.getTileSetTexY0 (), tile.getTileSetTexX1 (), tile.getTileSetTexY1 ());

		// E
		tile = tiles[3];
		UtilsGL.drawTexture (point.x + width - iEdgeWidth, point.y + iEdgeHeight, point.x + width, point.y + height - iEdgeHeight, tile.getTileSetTexX0 (), tile.getTileSetTexY0 (), tile.getTileSetTexX1 (), tile.getTileSetTexY1 ());

		// W
		tile = tiles[4];
		UtilsGL.drawTexture (point.x, point.y + iEdgeHeight, point.x + iEdgeWidth, point.y + height - iEdgeHeight, tile.getTileSetTexX0 (), tile.getTileSetTexY0 (), tile.getTileSetTexX1 (), tile.getTileSetTexY1 ());

		// NE
		tile = tiles[5];
		UtilsGL.drawTexture (point.x + width - iEdgeWidth, point.y, point.x + width, point.y + iEdgeHeight, tile.getTileSetTexX0 (), tile.getTileSetTexY0 (), tile.getTileSetTexX1 (), tile.getTileSetTexY1 ());

		// NW
		tile = tiles[6];
		UtilsGL.drawTexture (point.x, point.y, point.x + iEdgeWidth, point.y + iEdgeHeight, tile.getTileSetTexX0 (), tile.getTileSetTexY0 (), tile.getTileSetTexX1 (), tile.getTileSetTexY1 ());

		// SE
		tile = tiles[7];
		UtilsGL.drawTexture (point.x + width - iEdgeWidth, point.y + height - iEdgeHeight, point.x + width, point.y + height, tile.getTileSetTexX0 (), tile.getTileSetTexY0 (), tile.getTileSetTexX1 (), tile.getTileSetTexY1 ());

		// SW
		tile = tiles[8];
		UtilsGL.drawTexture (point.x, point.y + height - iEdgeHeight, point.x + iEdgeWidth, point.y + height, tile.getTileSetTexX0 (), tile.getTileSetTexY0 (), tile.getTileSetTexX1 (), tile.getTileSetTexY1 ());
	}


	private void renderTradePanel (int mouseX, int mouseY, int mousePanel) {
		Point pItem = isMouseOnTradeButtons (mouseX, mouseY);

		// Fondo
		// XAVI GL11.glColor4f (1, 1, 1, 1);
		int iCurrentTexture = tileTradePanel[0].getTextureID ();
		GL11.glBindTexture (GL11.GL_TEXTURE_2D, iCurrentTexture);
		GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		UtilsGL.glBegin (GL11.GL_QUADS);
		renderBackground (tileTradePanel, tradePanelPoint, TRADE_PANEL_WIDTH, TRADE_PANEL_HEIGHT);

		// Close button
		iCurrentTexture = UtilsGL.setTexture (tileButtonClose, iCurrentTexture);
		if (pItem != null && pItem.x == MOUSE_TRADE_PANEL_BUTTONS_CLOSE) {
			drawTile (tileButtonClose, tradePanelClosePoint);
		} else {
			drawTile (tileButtonCloseDisabled, tradePanelClosePoint);
		}

		UtilsGL.glEnd ();

		// Miramos si hay caravana activa
		CaravanData caravanData = Game.getWorld ().getCurrentCaravanData ();
		String sText = null;
		boolean bTrading = false;
		if (caravanData == null || caravanData.getStatus () == CaravanData.STATUS_NONE) {
			sText = Messages.getString ("UIPanel.17"); //$NON-NLS-1$
		} else if (caravanData.getStatus () == CaravanData.STATUS_COMING) {
			sText = Messages.getString ("UIPanel.18"); //$NON-NLS-1$
		} else if (caravanData.getStatus () == CaravanData.STATUS_IN_PLACE) {
			sText = null;
		} else if (caravanData.getStatus () == CaravanData.STATUS_TRADING) {
			sText = Messages.getString ("UIPanel.20"); //$NON-NLS-1$
			bTrading = true;
		} else if (caravanData.getStatus () == CaravanData.STATUS_LEAVING) {
			sText = Messages.getString ("UIPanel.21"); //$NON-NLS-1$
		} else {
			// Nunca debería llegar aquí
			Log.log (Log.LEVEL_ERROR, "Caravan status [" + caravanData.getStatus () + "]", "UIPanel"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			sText = null;
		}

		if (sText != null && !bTrading) {
			int iTextWidth = UtilFont.getWidth (sText);
			GL11.glBindTexture (GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
			GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
			UtilsGL.glBegin (GL11.GL_QUADS);
			UtilsGL.drawStringWithBorder (sText, tradePanelPoint.x + TRADE_PANEL_WIDTH / 2 - iTextWidth / 2, tradePanelPoint.y + TRADE_PANEL_HEIGHT / 2 - UtilFont.MAX_HEIGHT / 2, ColorGL.ORANGE, ColorGL.BLACK);
			UtilsGL.glEnd ();
			return;
		}

		// Si llega aquí es que la caravana está lista para tradear (o está tradeando)
		// if (!bTrading && tradePanel == null) {
		if (tradePanel == null) {
			// Acaba de entrar por primera vez, generamos el panel
			tradePanel = new TradePanel (caravanData, tradePanelPoint, TRADE_PANEL_WIDTH, TRADE_PANEL_HEIGHT);
		}

		// Renderizamos
		UtilsGL.glBegin (GL11.GL_QUADS);

		// Confirm button
		if (!bTrading) {
			iCurrentTexture = UtilsGL.setTexture (TradePanel.tileTradeConfirm, iCurrentTexture);
			if (tradePanel.isTransactionReady ()) {
				drawTile (TradePanel.tileTradeConfirm, tradePanel.getConfirmPoint (), TradePanel.tileTradeConfirm.getTileWidth (), TradePanel.tileTradeConfirm.getTileHeight (), (pItem != null && pItem.x == MOUSE_TRADE_PANEL_BUTTONS_CONFIRM));
			} else {
				drawTile (TradePanel.tileTradeConfirmDisabled, tradePanel.getConfirmPoint ());
			}
		}

		Point point;
		if (!bTrading) {
			// Caravan buttons scroll up
			iCurrentTexture = UtilsGL.setTexture (tileScrollUp, iCurrentTexture);
			point = tradePanel.getScrollUpCaravanPoint ();
			if (tradePanel.getIndexButtonsCaravan () > 0) {
				drawTile (tileScrollUp, point, tileScrollUp.getTileWidth (), tileScrollUp.getTileHeight (), (pItem != null && pItem.x == MOUSE_TRADE_PANEL_BUTTONS_UP_CARAVAN));
			} else {
				drawTile (tileScrollUpDisabled, point);
			}
		}
		// Caravan buttons scroll up to-buy
		point = tradePanel.getScrollUpCaravanToBuyPoint ();
		if (tradePanel.getIndexButtonsToBuyCaravan () > 0) {
			drawTile (tileScrollUp, point, tileScrollUp.getTileWidth (), tileScrollUp.getTileHeight (), (pItem != null && pItem.x == MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_UP_CARAVAN));
		} else {
			drawTile (tileScrollUpDisabled, point);
		}
		// Town buttons scroll up to-sell
		point = tradePanel.getScrollUpTownToSellPoint ();
		if (tradePanel.getIndexButtonsToSellTown () > 0) {
			drawTile (tileScrollUp, point, tileScrollUp.getTileWidth (), tileScrollUp.getTileHeight (), (pItem != null && pItem.x == MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_UP_TOWN));
		} else {
			drawTile (tileScrollUpDisabled, point);
		}

		if (!bTrading) {
			// Town buttons scroll up
			point = tradePanel.getScrollUpTownPoint ();
			if (tradePanel.getIndexButtonsTown () > 0) {
				drawTile (tileScrollUp, point, tileScrollUp.getTileWidth (), tileScrollUp.getTileHeight (), (pItem != null && pItem.x == MOUSE_TRADE_PANEL_BUTTONS_UP_TOWN));
			} else {
				drawTile (tileScrollUpDisabled, point);
			}
		}

		// Caravan buttons
		if (!bTrading) {
			iCurrentTexture = UtilsGL.setTexture (TradePanel.tileTradeButton, iCurrentTexture);
			for (int i = 0; i < tradePanel.getAlButtonPointsCaravan ().size (); i++) {
				point = tradePanel.getAlButtonPointsCaravan ().get (i);
				drawTile (TradePanel.tileTradeButton, point, TradePanel.tileTradeButton.getTileWidth (), TradePanel.tileTradeButton.getTileHeight (), (pItem != null && pItem.x == MOUSE_TRADE_PANEL_BUTTONS_CARAVAN && pItem.y == i));
			}
		}
		// Caravan buttons to-buy
		for (int i = 0; i < tradePanel.getAlButtonPointsCaravanToBuy ().size (); i++) {
			point = tradePanel.getAlButtonPointsCaravanToBuy ().get (i);
			drawTile (TradePanel.tileTradeButton, point, TradePanel.tileTradeButton.getTileWidth (), TradePanel.tileTradeButton.getTileHeight (), (pItem != null && pItem.x == MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_CARAVAN && pItem.y == i));
		}
		// Town buttons to-sell
		for (int i = 0; i < tradePanel.getAlButtonPointsTownToSell ().size (); i++) {
			point = tradePanel.getAlButtonPointsTownToSell ().get (i);
			drawTile (TradePanel.tileTradeButton, point, TradePanel.tileTradeButton.getTileWidth (), TradePanel.tileTradeButton.getTileHeight (), (pItem != null && pItem.x == MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_TOWN && pItem.y == i));
		}
		if (!bTrading) {
			// Town buttons
			for (int i = 0; i < tradePanel.getAlButtonPointsTown ().size (); i++) {
				point = tradePanel.getAlButtonPointsTown ().get (i);
				drawTile (TradePanel.tileTradeButton, point, TradePanel.tileTradeButton.getTileWidth (), TradePanel.tileTradeButton.getTileHeight (), (pItem != null && pItem.x == MOUSE_TRADE_PANEL_BUTTONS_TOWN && pItem.y == i));
			}
		}

		if (!bTrading) {
			// Caravan buttons scroll down
			iCurrentTexture = UtilsGL.setTexture (tileScrollDown, iCurrentTexture);
			point = tradePanel.getScrollDownCaravanPoint ();
			if (tradePanel.getAlButtonPointsCaravan ().size () + tradePanel.getIndexButtonsCaravan () < tradePanel.getMenuCaravan ().getItems ().size ()) {
				drawTile (tileScrollDown, point, tileScrollDown.getTileWidth (), tileScrollDown.getTileHeight (), (pItem != null && pItem.x == MOUSE_TRADE_PANEL_BUTTONS_DOWN_CARAVAN));
			} else {
				drawTile (tileScrollDownDisabled, point);
			}
		}
		// Caravan buttons scroll down to-buy
		point = tradePanel.getScrollDownCaravanToBuyPoint ();
		if (tradePanel.getAlButtonPointsCaravanToBuy ().size () + tradePanel.getIndexButtonsToBuyCaravan () < caravanData.getMenuCaravanToBuy ().getItems ().size ()) {
			drawTile (tileScrollDown, point, tileScrollDown.getTileWidth (), tileScrollDown.getTileHeight (), (pItem != null && pItem.x == MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_DOWN_CARAVAN));
		} else {
			drawTile (tileScrollDownDisabled, point);
		}
		// Town buttons scroll down to-sell
		point = tradePanel.getScrollDownTownToSellPoint ();
		if (tradePanel.getAlButtonPointsTownToSell ().size () + tradePanel.getIndexButtonsToSellTown () < caravanData.getMenuTownToSell ().getItems ().size ()) {
			drawTile (tileScrollDown, point, tileScrollDown.getTileWidth (), tileScrollDown.getTileHeight (), (pItem != null && pItem.x == MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_DOWN_TOWN));
		} else {
			drawTile (tileScrollDownDisabled, point);
		}
		if (!bTrading) {
			// Town buttons scroll down
			point = tradePanel.getScrollDownTownPoint ();
			if (tradePanel.getAlButtonPointsTown ().size () + tradePanel.getIndexButtonsTown () < tradePanel.getMenuTown ().getItems ().size ()) {
				drawTile (tileScrollDown, point, tileScrollDown.getTileWidth (), tileScrollDown.getTileHeight (), (pItem != null && pItem.x == MOUSE_TRADE_PANEL_BUTTONS_DOWN_TOWN));
			} else {
				drawTile (tileScrollDownDisabled, point);
			}
		}

		// Icons
		iCurrentTexture = UtilsGL.setTexture (TradePanel.tileTradeCaravanCoins, iCurrentTexture);
		drawTile (TradePanel.tileTradeCaravanCoins, tradePanel.getCaravanCoinsIconPoint ());
		iCurrentTexture = UtilsGL.setTexture (TradePanel.tileTradeTownCoins, iCurrentTexture);
		drawTile (TradePanel.tileTradeTownCoins, tradePanel.getTownCoinsIconPoint ());
		iCurrentTexture = UtilsGL.setTexture (TradePanel.tileTradeBuy, iCurrentTexture);
		drawTile (TradePanel.tileTradeBuy, tradePanel.getBuyIconPoint ());
		iCurrentTexture = UtilsGL.setTexture (TradePanel.tileTradeSell, iCurrentTexture);
		drawTile (TradePanel.tileTradeSell, tradePanel.getSellIconPoint ());
		iCurrentTexture = UtilsGL.setTexture (TradePanel.tileTradeCost, iCurrentTexture);
		drawTile (TradePanel.tileTradeCost, tradePanel.getCostPoint ());

		SmartMenu menu;
		int iIndex;

		if (!bTrading) {
			// Caravan Items
			menu = tradePanel.getMenuCaravan ();
			for (int i = 0; i < tradePanel.getAlButtonPointsCaravan ().size (); i++) {
				iIndex = i + tradePanel.getIndexButtonsCaravan ();
				if (menu.getItems ().size () <= iIndex) {
					break;
				}
				if (menu.getItems ().get (iIndex).getIcon () != null && menu.getItems ().get (iIndex).getIconType () == SmartMenu.ICON_TYPE_ITEM) {
					point = tradePanel.getAlButtonPointsCaravan ().get (i);
					iCurrentTexture = UtilsGL.setTexture (menu.getItems ().get (iIndex).getIcon (), iCurrentTexture);
					drawTile (menu.getItems ().get (iIndex).getIcon (), point, TRADE_PANEL_BUTTON_WIDTH, TRADE_PANEL_BUTTON_HEIGHT, (pItem != null && pItem.x == MOUSE_TRADE_PANEL_BUTTONS_CARAVAN && pItem.y == i));
				}
			}
		}
		// Caravan Items to-buy
		menu = caravanData.getMenuCaravanToBuy ();
		for (int i = 0; i < tradePanel.getAlButtonPointsCaravanToBuy ().size (); i++) {
			iIndex = i + tradePanel.getIndexButtonsToBuyCaravan ();
			if (menu.getItems ().size () <= iIndex) {
				break;
			}
			if (menu.getItems ().get (iIndex).getIcon () != null && menu.getItems ().get (iIndex).getIconType () == SmartMenu.ICON_TYPE_ITEM) {
				point = tradePanel.getAlButtonPointsCaravanToBuy ().get (i);
				iCurrentTexture = UtilsGL.setTexture (menu.getItems ().get (iIndex).getIcon (), iCurrentTexture);
				drawTile (menu.getItems ().get (iIndex).getIcon (), point, TRADE_PANEL_BUTTON_WIDTH, TRADE_PANEL_BUTTON_HEIGHT, (pItem != null && pItem.x == MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_CARAVAN && pItem.y == i));
			}
		}
		// Town Items to-sell
		menu = caravanData.getMenuTownToSell ();
		for (int i = 0; i < tradePanel.getAlButtonPointsTownToSell ().size (); i++) {
			iIndex = i + tradePanel.getIndexButtonsToSellTown ();
			if (menu.getItems ().size () <= iIndex) {
				break;
			}
			if (menu.getItems ().get (iIndex).getIcon () != null && menu.getItems ().get (iIndex).getIconType () == SmartMenu.ICON_TYPE_ITEM) {
				point = tradePanel.getAlButtonPointsTownToSell ().get (i);
				iCurrentTexture = UtilsGL.setTexture (menu.getItems ().get (iIndex).getIcon (), iCurrentTexture);
				drawTile (menu.getItems ().get (iIndex).getIcon (), point, TRADE_PANEL_BUTTON_WIDTH, TRADE_PANEL_BUTTON_HEIGHT, (pItem != null && pItem.x == MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_TOWN && pItem.y == i));
			}
		}
		if (!bTrading) {
			// Town Items
			menu = tradePanel.getMenuTown ();
			for (int i = 0; i < tradePanel.getAlButtonPointsTown ().size (); i++) {
				iIndex = i + tradePanel.getIndexButtonsTown ();
				if (menu.getItems ().size () <= iIndex) {
					break;
				}
				if (menu.getItems ().get (iIndex).getIcon () != null && menu.getItems ().get (iIndex).getIconType () == SmartMenu.ICON_TYPE_ITEM) {
					point = tradePanel.getAlButtonPointsTown ().get (i);
					iCurrentTexture = UtilsGL.setTexture (menu.getItems ().get (iIndex).getIcon (), iCurrentTexture);
					drawTile (menu.getItems ().get (iIndex).getIcon (), point, TRADE_PANEL_BUTTON_WIDTH, TRADE_PANEL_BUTTON_HEIGHT, (pItem != null && pItem.x == MOUSE_TRADE_PANEL_BUTTONS_TOWN && pItem.y == i));
				}
			}
		}
		UtilsGL.glEnd ();

		// Números
		int iTextWidth;
		GL11.glBindTexture (GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
		GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		UtilsGL.glBegin (GL11.GL_QUADS);
		if (!bTrading) {
			// Caravan
			menu = tradePanel.getMenuCaravan ();
			for (int i = 0; i < tradePanel.getAlButtonPointsCaravan ().size (); i++) {
				iIndex = i + tradePanel.getIndexButtonsCaravan ();
				if (menu.getItems ().size () <= iIndex) {
					break;
				}
				point = tradePanel.getAlButtonPointsCaravan ().get (i);
				sText = menu.getItems ().get (iIndex).getParameter2 ();
				iTextWidth = UtilFont.getWidth (sText); // Qtty
				UtilsGL.drawStringWithBorder (sText, point.x + TRADE_PANEL_BUTTON_WIDTH / 2 - iTextWidth / 2, point.y + TRADE_PANEL_BUTTON_HEIGHT - UtilFont.MAX_HEIGHT, ColorGL.WHITE, ColorGL.BLACK);
				sText = Integer.toString (menu.getItems ().get (iIndex).getDirectCoordinates ().x);
				iTextWidth = UtilFont.getWidth (sText); // Price
				UtilsGL.drawStringWithBorder (sText, point.x + TRADE_PANEL_BUTTON_WIDTH / 2 - iTextWidth / 2, point.y, ColorGL.WHITE, ColorGL.BLACK);
			}
		}
		// Caravan to-buy
		menu = caravanData.getMenuCaravanToBuy ();
		for (int i = 0; i < tradePanel.getAlButtonPointsCaravanToBuy ().size (); i++) {
			iIndex = i + tradePanel.getIndexButtonsToBuyCaravan ();
			if (menu.getItems ().size () <= iIndex) {
				break;
			}
			point = tradePanel.getAlButtonPointsCaravanToBuy ().get (i);
			sText = menu.getItems ().get (iIndex).getParameter2 ();
			iTextWidth = UtilFont.getWidth (sText); // Qtty
			UtilsGL.drawStringWithBorder (sText, point.x + TRADE_PANEL_BUTTON_WIDTH / 2 - iTextWidth / 2, point.y + TRADE_PANEL_BUTTON_HEIGHT - UtilFont.MAX_HEIGHT, ColorGL.WHITE, ColorGL.BLACK);
			sText = Integer.toString (menu.getItems ().get (iIndex).getDirectCoordinates ().x);
			iTextWidth = UtilFont.getWidth (sText); // Price
			UtilsGL.drawStringWithBorder (sText, point.x + TRADE_PANEL_BUTTON_WIDTH / 2 - iTextWidth / 2, point.y, ColorGL.WHITE, ColorGL.BLACK);
		}
		// Town to-sell
		menu = caravanData.getMenuTownToSell ();
		for (int i = 0; i < tradePanel.getAlButtonPointsTownToSell ().size (); i++) {
			iIndex = i + tradePanel.getIndexButtonsToSellTown ();
			if (menu.getItems ().size () <= iIndex) {
				break;
			}
			point = tradePanel.getAlButtonPointsTownToSell ().get (i);
			sText = menu.getItems ().get (iIndex).getParameter2 ();
			iTextWidth = UtilFont.getWidth (sText); // Qtty
			UtilsGL.drawStringWithBorder (sText, point.x + TRADE_PANEL_BUTTON_WIDTH / 2 - iTextWidth / 2, point.y + TRADE_PANEL_BUTTON_HEIGHT - UtilFont.MAX_HEIGHT, ColorGL.WHITE, ColorGL.BLACK);
			sText = Integer.toString (menu.getItems ().get (iIndex).getDirectCoordinates ().x);
			iTextWidth = UtilFont.getWidth (sText); // Price
			UtilsGL.drawStringWithBorder (sText, point.x + TRADE_PANEL_BUTTON_WIDTH / 2 - iTextWidth / 2, point.y, ColorGL.WHITE, ColorGL.BLACK);
		}
		if (!bTrading) {
			// Town
			menu = tradePanel.getMenuTown ();
			for (int i = 0; i < tradePanel.getAlButtonPointsTown ().size (); i++) {
				iIndex = i + tradePanel.getIndexButtonsTown ();
				if (menu.getItems ().size () <= iIndex) {
					break;
				}
				point = tradePanel.getAlButtonPointsTown ().get (i);
				sText = menu.getItems ().get (iIndex).getParameter2 ();
				iTextWidth = UtilFont.getWidth (sText); // Qtty
				UtilsGL.drawStringWithBorder (sText, point.x + TRADE_PANEL_BUTTON_WIDTH / 2 - iTextWidth / 2, point.y + TRADE_PANEL_BUTTON_HEIGHT - UtilFont.MAX_HEIGHT, ColorGL.WHITE, ColorGL.BLACK);
				sText = Integer.toString (menu.getItems ().get (iIndex).getDirectCoordinates ().x);
				iTextWidth = UtilFont.getWidth (sText); // Price
				UtilsGL.drawStringWithBorder (sText, point.x + TRADE_PANEL_BUTTON_WIDTH / 2 - iTextWidth / 2, point.y, ColorGL.WHITE, ColorGL.BLACK);
			}
		}

		// Caravan coins
		sText = Integer.toString (caravanData.getCoins ());
		UtilsGL.drawString (sText, tradePanel.getCaravanCoinsIconPoint ().x + TradePanel.tileTradeCaravanCoins.getTileWidth () / 2 - UtilFont.getWidth (sText) / 2, tradePanel.getCaravanCoinsIconPoint ().y + TradePanel.tileTradeCaravanCoins.getTileHeight (), ColorGL.BLACK);
		// Cost
		sText = Integer.toString (tradePanel.getCost ());
		UtilsGL.drawString (sText, tradePanel.getCostPoint ().x + TradePanel.tileTradeCost.getTileWidth () / 2 - UtilFont.getWidth (sText) / 2, tradePanel.getCostPoint ().y + TradePanel.tileTradeCost.getTileHeight (), tradePanel.getCost () >= 0 ? ColorGL.BLACK : ColorGL.RED);
		// Towns coins
		sText = Integer.toString (Game.getWorld ().getCoins ());
		UtilsGL.drawString (sText, tradePanel.getTownCoinsIconPoint ().x + TradePanel.tileTradeTownCoins.getTileWidth () / 2 - UtilFont.getWidth (sText) / 2, tradePanel.getTownCoinsIconPoint ().y + TradePanel.tileTradeTownCoins.getTileHeight (), ColorGL.BLACK);

		UtilsGL.glEnd ();
	}


	private void renderMessagesPanel (int mouseX, int mouseY, int mousePanel) {
		Point pItem = isMouseOnMessagesButtons (mouseX, mouseY);

		// Fondo
		// XAVI GL11.glColor4f (1, 1, 1, 1);
		int iCurrentTexture = tileMessagesPanel[0].getTextureID ();
		GL11.glBindTexture (GL11.GL_TEXTURE_2D, iCurrentTexture);
		GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		UtilsGL.glBegin (GL11.GL_QUADS);
		renderBackground (tileMessagesPanel, messagesPanelPoint, MESSAGES_PANEL_WIDTH, MESSAGES_PANEL_HEIGHT);

		// Close button
		iCurrentTexture = UtilsGL.setTexture (tileButtonClose, iCurrentTexture);
		if (pItem != null && pItem.x == MOUSE_MESSAGES_PANEL_BUTTONS_CLOSE) {
			drawTile (tileButtonClose, messagesPanelClosePoint);
		} else {
			drawTile (tileButtonCloseDisabled, messagesPanelClosePoint);
		}

		// "Tabs"
		int iMessagesType = getMessagesPanelActive ();
		if (MessagesPanel.TYPE_ANNOUNCEMENT == iMessagesType) {
			iCurrentTexture = UtilsGL.setTexture (messagePanelTilesON[MessagesPanel.TYPE_ANNOUNCEMENT], iCurrentTexture);
			drawTile (messagePanelTilesON[MessagesPanel.TYPE_ANNOUNCEMENT], messagePanelIconPoints[MessagesPanel.TYPE_ANNOUNCEMENT], (pItem != null && pItem.x == MOUSE_MESSAGES_PANEL_BUTTONS_ANNOUNCEMENT));
		} else {
			iCurrentTexture = UtilsGL.setTexture (messagePanelTiles[MessagesPanel.TYPE_ANNOUNCEMENT], iCurrentTexture);
			drawTile (messagePanelTiles[MessagesPanel.TYPE_ANNOUNCEMENT], messagePanelIconPoints[MessagesPanel.TYPE_ANNOUNCEMENT], (pItem != null && pItem.x == MOUSE_MESSAGES_PANEL_BUTTONS_ANNOUNCEMENT));
		}
		if (MessagesPanel.TYPE_COMBAT == iMessagesType) {
			iCurrentTexture = UtilsGL.setTexture (messagePanelTilesON[MessagesPanel.TYPE_COMBAT], iCurrentTexture);
			drawTile (messagePanelTilesON[MessagesPanel.TYPE_COMBAT], messagePanelIconPoints[MessagesPanel.TYPE_COMBAT], (pItem != null && pItem.x == MOUSE_MESSAGES_PANEL_BUTTONS_COMBAT));
		} else {
			iCurrentTexture = UtilsGL.setTexture (messagePanelTiles[MessagesPanel.TYPE_COMBAT], iCurrentTexture);
			drawTile (messagePanelTiles[MessagesPanel.TYPE_COMBAT], messagePanelIconPoints[MessagesPanel.TYPE_COMBAT], (pItem != null && pItem.x == MOUSE_MESSAGES_PANEL_BUTTONS_COMBAT));
		}
		if (MessagesPanel.TYPE_HEROES == iMessagesType) {
			iCurrentTexture = UtilsGL.setTexture (messagePanelTilesON[MessagesPanel.TYPE_HEROES], iCurrentTexture);
			drawTile (messagePanelTilesON[MessagesPanel.TYPE_HEROES], messagePanelIconPoints[MessagesPanel.TYPE_HEROES], (pItem != null && pItem.x == MOUSE_MESSAGES_PANEL_BUTTONS_HEROES));
		} else {
			iCurrentTexture = UtilsGL.setTexture (messagePanelTiles[MessagesPanel.TYPE_HEROES], iCurrentTexture);
			drawTile (messagePanelTiles[MessagesPanel.TYPE_HEROES], messagePanelIconPoints[MessagesPanel.TYPE_HEROES], (pItem != null && pItem.x == MOUSE_MESSAGES_PANEL_BUTTONS_HEROES));
		}
		if (MessagesPanel.TYPE_SYSTEM == iMessagesType) {
			iCurrentTexture = UtilsGL.setTexture (messagePanelTilesON[MessagesPanel.TYPE_SYSTEM], iCurrentTexture);
			drawTile (messagePanelTilesON[MessagesPanel.TYPE_SYSTEM], messagePanelIconPoints[MessagesPanel.TYPE_SYSTEM], (pItem != null && pItem.x == MOUSE_MESSAGES_PANEL_BUTTONS_SYSTEM));
		} else {
			iCurrentTexture = UtilsGL.setTexture (messagePanelTiles[MessagesPanel.TYPE_SYSTEM], iCurrentTexture);
			drawTile (messagePanelTiles[MessagesPanel.TYPE_SYSTEM], messagePanelIconPoints[MessagesPanel.TYPE_SYSTEM], (pItem != null && pItem.x == MOUSE_MESSAGES_PANEL_BUTTONS_SYSTEM));
		}

		// Scrolls
		if (MessagesPanel.getPages (iMessagesType) > 1 && MessagesPanel.getPagesCurrent (iMessagesType) > 1) {
			iCurrentTexture = UtilsGL.setTexture (tileScrollUp, iCurrentTexture);
			drawTile (tileScrollUp, messagePanelIconScrollUpPoint, (pItem != null && pItem.x == MOUSE_MESSAGES_PANEL_BUTTONS_SCROLL_UP));
		} else {
			iCurrentTexture = UtilsGL.setTexture (tileScrollUpDisabled, iCurrentTexture);
			drawTile (tileScrollUpDisabled, messagePanelIconScrollUpPoint);
		}
		if (MessagesPanel.getPages (iMessagesType) > 1 && MessagesPanel.getPagesCurrent (iMessagesType) < MessagesPanel.getPages (iMessagesType)) {
			iCurrentTexture = UtilsGL.setTexture (tileScrollDown, iCurrentTexture);
			drawTile (tileScrollDown, messagePanelIconScrollDownPoint, (pItem != null && pItem.x == MOUSE_MESSAGES_PANEL_BUTTONS_SCROLL_DOWN));
		} else {
			iCurrentTexture = UtilsGL.setTexture (tileScrollDownDisabled, iCurrentTexture);
			drawTile (tileScrollDownDisabled, messagePanelIconScrollDownPoint);
		}

		// Subpanel donde irá el texto
		iCurrentTexture = UtilsGL.setTexture (tileMessagesPanelSubPanel[0], iCurrentTexture);
		renderBackground (tileMessagesPanelSubPanel, messagesPanelSubPanelPoint, MESSAGES_PANEL_SUBPANEL_WIDTH, MESSAGES_PANEL_SUBPANEL_HEIGHT);

		UtilsGL.glEnd ();

		// Pages
		String sText = MessagesPanel.getPagesCurrent (iMessagesType) + " / " + MessagesPanel.getPages (iMessagesType); //$NON-NLS-1$
		// XAVI GL11.glColor4f (1, 1, 1, 1);
		GL11.glBindTexture (GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
		GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		UtilsGL.glBegin (GL11.GL_QUADS);
		UtilsGL.drawString (sText, messagePanelPagesPositionPoint.x - UtilFont.getWidth (sText) / 2, messagePanelPagesPositionPoint.y, ColorGL.BLACK);
		UtilsGL.glEnd ();

		// Mensajes
		MessagesPanel.render (mouseX, mouseY, getMessagesPanelActive (), messagesPanelSubPanelPoint.x + tileMessagesPanel[3].getTileWidth (), messagesPanelSubPanelPoint.y + tileMessagesPanel[1].getTileHeight ());
	}


	private void renderPilePanel (int mouseX, int mouseY, int mousePanel) {
		Point pItem = isMouseOnPileButtons (mouseX, mouseY);

		// XAVI GL11.glColor4f (1, 1, 1, 1);
		int iCurrentTexture = tileMatsPanel[0].getTextureID ();
		GL11.glBindTexture (GL11.GL_TEXTURE_2D, iCurrentTexture);
		GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		UtilsGL.glBegin (GL11.GL_QUADS);
		renderBackground (tileMatsPanel, pilePanelPoint, PILE_PANEL_WIDTH, PILE_PANEL_HEIGHT);

		// Close button
		iCurrentTexture = UtilsGL.setTexture (tileButtonClose, iCurrentTexture);
		if (pItem != null && pItem.x == MOUSE_PILE_PANEL_BUTTONS_CLOSE) {
			drawTile (tileButtonClose, pilePanelClosePoint);
		} else {
			drawTile (tileButtonCloseDisabled, pilePanelClosePoint);
		}

		// Scroll up
		if (pilePanelPageIndex > 0) {
			// Enabled
			iCurrentTexture = UtilsGL.setTexture (tileScrollUp, iCurrentTexture);
			drawTile (tileScrollUp, pilePanelIconScrollUpPoint, (pItem != null && pItem.x == MOUSE_PILE_PANEL_BUTTONS_SCROLL_UP));
		} else {
			// Disabled
			iCurrentTexture = UtilsGL.setTexture (tileScrollUpDisabled, iCurrentTexture);
			drawTile (tileScrollUpDisabled, pilePanelIconScrollUpPoint);
		}
		// Scroll down
		if ((pilePanelPageIndex + 1) < pilePanelMaxPages) {
			// Enabled
			iCurrentTexture = UtilsGL.setTexture (tileScrollDown, iCurrentTexture);
			drawTile (tileScrollDown, pilePanelIconScrollDownPoint, (pItem != null && pItem.x == MOUSE_PILE_PANEL_BUTTONS_SCROLL_DOWN));
		} else {
			// Disabled
			iCurrentTexture = UtilsGL.setTexture (tileScrollDownDisabled, iCurrentTexture);
			drawTile (tileScrollDownDisabled, pilePanelIconScrollDownPoint);
		}

		// Items
		if (menuPile != null) {
			int iFirstIndex = pilePanelPageIndex * PILE_PANEL_MAX_ITEMS_PER_PAGE;
			int iMaxItems = Math.min (menuPile.getItems ().size () - iFirstIndex, PILE_PANEL_MAX_ITEMS_PER_PAGE);
			Tile tile;
			SmartMenu smAux;
			for (int i = 0; i < iMaxItems; i++) {
				smAux = menuPile.getItems ().get (i + iFirstIndex);
				if (smAux.getType () == SmartMenu.TYPE_MENU) {
					// Submenu
					iCurrentTexture = UtilsGL.setTexture (tileBottomItemSM, iCurrentTexture);
					drawTile (tileBottomItemSM, pilePanelItemPoints[i].x + tileBottomItemSM.getTileWidth () / 2 - tileBottomItemSM.getTileWidth () / 2, pilePanelItemPoints[i].y + tileBottomItemSM.getTileHeight () / 2 - tileBottomItemSM.getTileHeight () / 2, (pItem != null && pItem.y == i));
				} else {
					// Item
					iCurrentTexture = UtilsGL.setTexture (tileBottomItem, iCurrentTexture);
					drawTile (tileBottomItem, pilePanelItemPoints[i].x + tileBottomItem.getTileWidth () / 2 - tileBottomItem.getTileWidth () / 2, pilePanelItemPoints[i].y + tileBottomItem.getTileHeight () / 2 - tileBottomItem.getTileHeight () / 2, (pItem != null && pItem.y == i));
				}

				// Icono
				tile = smAux.getIcon ();
				if (tile != null) {
					iCurrentTexture = UtilsGL.setTexture (tile, iCurrentTexture);
					drawTile (tile, pilePanelItemPoints[i].x, pilePanelItemPoints[i].y, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT, (pItem != null && pItem.y == i));
				}

				if (smAux.getCommand () != null && (smAux.getCommand ().equals (CommandPanel.COMMAND_STOCKPILE_ENABLE_ITEM) || smAux.getCommand ().equals (CommandPanel.COMMAND_CONTAINER_ENABLE_ITEM))) {
					// Cruz roja
					tile = BIG_RED_CROSS_TILE; // World.getTileRedCross ();
					iCurrentTexture = UtilsGL.setTexture (tile, iCurrentTexture);
					drawTile (tile, pilePanelItemPoints[i].x + tileBottomItem.getTileWidth () / 2 - tile.getTileWidth () / 2, pilePanelItemPoints[i].y + tileBottomItem.getTileHeight () / 2 - tile.getTileHeight () / 2, (pItem != null && pItem.y == i));
				}
			}
		}

		// Configuration buttons
		iCurrentTexture = UtilsGL.setTexture (tileConfigCopy, iCurrentTexture);
		drawTile (tileConfigCopy, pilePanelIconConfigCopyPoint.x, pilePanelIconConfigCopyPoint.y, (pItem != null && pItem.x == MOUSE_PILE_PANEL_BUTTONS_CONFIG_COPY));

		if (pilePanelIsLocked) {
			iCurrentTexture = UtilsGL.setTexture (tileConfigLockLocked, iCurrentTexture);
			drawTile (tileConfigLockLocked, pilePanelIconConfigLockPoint.x, pilePanelIconConfigLockPoint.y, (pItem != null && pItem.x == MOUSE_PILE_PANEL_BUTTONS_CONFIG_LOCK));
		} else {
			iCurrentTexture = UtilsGL.setTexture (tileConfigLock, iCurrentTexture);
			drawTile (tileConfigLock, pilePanelIconConfigLockPoint.x, pilePanelIconConfigLockPoint.y, (pItem != null && pItem.x == MOUSE_PILE_PANEL_BUTTONS_CONFIG_LOCK));
		}

		iCurrentTexture = UtilsGL.setTexture (tileConfigLockAll, iCurrentTexture);
		drawTile (tileConfigLockAll, pilePanelIconConfigLockAllPoint.x, pilePanelIconConfigLockAllPoint.y, (pItem != null && pItem.x == MOUSE_PILE_PANEL_BUTTONS_CONFIG_LOCK_ALL));

		iCurrentTexture = UtilsGL.setTexture (tileConfigUnlockAll, iCurrentTexture);
		drawTile (tileConfigUnlockAll, pilePanelIconConfigUnlockAllPoint.x, pilePanelIconConfigUnlockAllPoint.y, (pItem != null && pItem.x == MOUSE_PILE_PANEL_BUTTONS_CONFIG_UNLOCK_ALL));
		UtilsGL.glEnd ();

		// Pages
		GL11.glBindTexture (GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
		GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		UtilsGL.glBegin (GL11.GL_QUADS);

		String sText = (pilePanelPageIndex + 1) + " / " + pilePanelMaxPages; //$NON-NLS-1$
		UtilsGL.drawString (sText, pilePanelPagesPositionPoint.x - UtilFont.getWidth (sText) / 2, pilePanelPagesPositionPoint.y, ColorGL.BLACK);

		if (menuPile != null) {
			int iFirstIndex = pilePanelPageIndex * PILE_PANEL_MAX_ITEMS_PER_PAGE;
			int iMaxItems = Math.min (menuPile.getItems ().size () - iFirstIndex, PILE_PANEL_MAX_ITEMS_PER_PAGE);
			SmartMenu smAux;

			// Stock
			for (int i = 0; i < iMaxItems; i++) {
				smAux = menuPile.getItems ().get (i + iFirstIndex);
				if (smAux.getType () == SmartMenu.TYPE_ITEM) {
					if (smAux.getCommand ().equals (CommandPanel.COMMAND_STOCKPILE_ENABLE_ITEM) || smAux.getCommand ().equals (CommandPanel.COMMAND_STOCKPILE_DISABLE_ITEM)) {
						int iNumItems = Item.getNumItems (UtilsIniHeaders.getIntIniHeader (smAux.getParameter ()), false, World.MAP_DEPTH);
						sText = Integer.toString (iNumItems);
						UtilsGL.drawStringWithBorder (sText, pilePanelItemPoints[i].x + tileBottomItem.getTileWidth () / 2 - UtilFont.getWidth (sText) / 2, pilePanelItemPoints[i].y + tileBottomItem.getTileHeight () - UtilFont.MAX_HEIGHT, ColorGL.WHITE, ColorGL.BLACK);
					} else if (smAux.getCommand ().equals (CommandPanel.COMMAND_CONTAINER_ENABLE_ITEM) || smAux.getCommand ().equals (CommandPanel.COMMAND_CONTAINER_DISABLE_ITEM)) {
						int iNumItems = Item.getNumItems (UtilsIniHeaders.getIntIniHeader (smAux.getParameter2 ()), false, World.MAP_DEPTH);
						sText = Integer.toString (iNumItems);
						UtilsGL.drawStringWithBorder (sText, pilePanelItemPoints[i].x + tileBottomItem.getTileWidth () / 2 - UtilFont.getWidth (sText) / 2, pilePanelItemPoints[i].y + tileBottomItem.getTileHeight () - UtilFont.MAX_HEIGHT, ColorGL.WHITE, ColorGL.BLACK);
					}
				}
			}
		}

		// Title
		String sTitle;
		if (isPilePanelIsContainer ()) {
			sTitle = Messages.getString ("UIPanel.62"); //$NON-NLS-1$
		} else {
			sTitle = Messages.getString ("UIPanel.64"); //$NON-NLS-1$
		}
		UtilsGL.drawStringWithBorder (sTitle, pilePanelPoint.x + tileMatsPanel[3].getTileWidth (), pilePanelPoint.y + UtilFont.MAX_HEIGHT, ColorGL.ORANGE, ColorGL.BLACK);

		UtilsGL.glEnd ();
	}


	private void renderProfessionsPanel (int mouseX, int mouseY, int mousePanel) {
		Point pItem = isMouseOnProfessionsButtons (mouseX, mouseY);

		// XAVI GL11.glColor4f (1, 1, 1, 1);
		int iCurrentTexture = tileMatsPanel[0].getTextureID ();
		GL11.glBindTexture (GL11.GL_TEXTURE_2D, iCurrentTexture);
		GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		UtilsGL.glBegin (GL11.GL_QUADS);
		renderBackground (tileMatsPanel, professionsPanelPoint, PROFESSIONS_PANEL_WIDTH, PROFESSIONS_PANEL_HEIGHT);

		// Close button
		iCurrentTexture = UtilsGL.setTexture (tileButtonClose, iCurrentTexture);
		if (pItem != null && pItem.x == MOUSE_PROFESSIONS_PANEL_BUTTONS_CLOSE) {
			drawTile (tileButtonClose, professionsPanelClosePoint);
		} else {
			drawTile (tileButtonCloseDisabled, professionsPanelClosePoint);
		}

		// Scroll up
		if (professionsPanelPageIndex > 0) {
			// Enabled
			iCurrentTexture = UtilsGL.setTexture (tileScrollUp, iCurrentTexture);
			drawTile (tileScrollUp, professionsPanelIconScrollUpPoint, (pItem != null && pItem.x == MOUSE_PROFESSIONS_PANEL_BUTTONS_SCROLL_UP));
		} else {
			// Disabled
			iCurrentTexture = UtilsGL.setTexture (tileScrollUpDisabled, iCurrentTexture);
			drawTile (tileScrollUpDisabled, professionsPanelIconScrollUpPoint);
		}
		// Scroll down
		if ((professionsPanelPageIndex + 1) < professionsPanelMaxPages) {
			// Enabled
			iCurrentTexture = UtilsGL.setTexture (tileScrollDown, iCurrentTexture);
			drawTile (tileScrollDown, professionsPanelIconScrollDownPoint, (pItem != null && pItem.x == MOUSE_PROFESSIONS_PANEL_BUTTONS_SCROLL_DOWN));
		} else {
			// Disabled
			iCurrentTexture = UtilsGL.setTexture (tileScrollDownDisabled, iCurrentTexture);
			drawTile (tileScrollDownDisabled, professionsPanelIconScrollDownPoint);
		}

		// Items
		if (menuProfessions != null) {
			int iFirstIndex = professionsPanelPageIndex * PROFESSIONS_PANEL_MAX_ITEMS_PER_PAGE;
			int iMaxItems = Math.min (menuProfessions.getItems ().size () - iFirstIndex, PROFESSIONS_PANEL_MAX_ITEMS_PER_PAGE);
			Tile tile;
			SmartMenu smAux;
			for (int i = 0; i < iMaxItems; i++) {
				smAux = menuProfessions.getItems ().get (i + iFirstIndex);
				if (smAux.getType () == SmartMenu.TYPE_MENU) {
					// Submenu
					iCurrentTexture = UtilsGL.setTexture (tileBottomItemSM, iCurrentTexture);
					drawTile (tileBottomItemSM, professionsPanelItemPoints[i].x + tileBottomItemSM.getTileWidth () / 2 - tileBottomItemSM.getTileWidth () / 2, professionsPanelItemPoints[i].y + tileBottomItemSM.getTileHeight () / 2 - tileBottomItemSM.getTileHeight () / 2, (pItem != null && pItem.y == i));
				} else {
					// Item
					iCurrentTexture = UtilsGL.setTexture (tileBottomItem, iCurrentTexture);
					drawTile (tileBottomItem, professionsPanelItemPoints[i].x + tileBottomItem.getTileWidth () / 2 - tileBottomItem.getTileWidth () / 2, professionsPanelItemPoints[i].y + tileBottomItem.getTileHeight () / 2 - tileBottomItem.getTileHeight () / 2, (pItem != null && pItem.y == i));
				}

				// Icono
				tile = smAux.getIcon ();
				if (tile != null) {
					iCurrentTexture = UtilsGL.setTexture (tile, iCurrentTexture);
					drawTile (tile, professionsPanelItemPoints[i].x, professionsPanelItemPoints[i].y, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT, (pItem != null && pItem.y == i));
				}

				if (smAux.getCommand () != null && (smAux.getCommand ().equals (CommandPanel.COMMAND_PROFESSIONS_ENABLE_ITEM) || smAux.getCommand ().equals (CommandPanel.COMMAND_JOB_GROUP_ENABLE_ITEM))) {
					// Cruz roja
					tile = BIG_RED_CROSS_TILE;
					iCurrentTexture = UtilsGL.setTexture (tile, iCurrentTexture);
					drawTile (tile, professionsPanelItemPoints[i].x + tileBottomItem.getTileWidth () / 2 - tile.getTileWidth () / 2, professionsPanelItemPoints[i].y + tileBottomItem.getTileHeight () / 2 - tile.getTileHeight () / 2, (pItem != null && pItem.y == i));
				}
			}
		}

		UtilsGL.glEnd ();

		// Pages
		GL11.glBindTexture (GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
		GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		UtilsGL.glBegin (GL11.GL_QUADS);

		String sText = (professionsPanelPageIndex + 1) + " / " + professionsPanelMaxPages; //$NON-NLS-1$
		UtilsGL.drawString (sText, professionsPanelPagesPositionPoint.x - UtilFont.getWidth (sText) / 2, professionsPanelPagesPositionPoint.y, ColorGL.BLACK);

		// Title
		if (professionsPanelIsCitizen) {
			UtilsGL.drawStringWithBorder (Messages.getString ("UIPanel.63"), professionsPanelPoint.x + tileMatsPanel[3].getTileWidth (), professionsPanelPoint.y + UtilFont.MAX_HEIGHT, ColorGL.ORANGE, ColorGL.BLACK); //$NON-NLS-1$
		} else {
			UtilsGL.drawStringWithBorder (Messages.getString ("UIPanel.67"), professionsPanelPoint.x + tileMatsPanel[3].getTileWidth (), professionsPanelPoint.y + UtilFont.MAX_HEIGHT, ColorGL.ORANGE, ColorGL.BLACK); //$NON-NLS-1$
		}

		UtilsGL.glEnd ();
	}


	private void renderMatsPanel (int mouseX, int mouseY, int mousePanel) {
		Point pItem = isMouseOnMatsButtons (mouseX, mouseY);

		// XAVI GL11.glColor4f (1, 1, 1, 1);
		int iCurrentTexture = tileMatsPanel[0].getTextureID ();
		GL11.glBindTexture (GL11.GL_TEXTURE_2D, iCurrentTexture);
		GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		UtilsGL.glBegin (GL11.GL_QUADS);
		renderBackground (tileMatsPanel, matsPanelPoint, MATS_PANEL_WIDTH, MATS_PANEL_HEIGHT);

		// Close button
		iCurrentTexture = UtilsGL.setTexture (tileButtonClose, iCurrentTexture);
		if (pItem != null && pItem.x == MOUSE_MATS_PANEL_BUTTONS_CLOSE) {
			drawTile (tileButtonClose, matsPanelClosePoint);
		} else {
			drawTile (tileButtonCloseDisabled, matsPanelClosePoint);
		}

		// Subpanel donde irán los items
		iCurrentTexture = UtilsGL.setTexture (tileMatsPanelSubPanel[0], iCurrentTexture);
		renderBackground (tileMatsPanelSubPanel, matsPanelSubPanelPoint, MATS_PANEL_SUBPANEL_WIDTH, MATS_PANEL_SUBPANEL_HEIGHT);

		// "Tabs"
		iCurrentTexture = UtilsGL.setTexture (tileBottomItem, iCurrentTexture);
		for (int i = 0; i < MatsPanelData.numGroups; i++) {
			drawTile (tileBottomItem, matsPanelIconPoints[i], (pItem != null && pItem.x == MOUSE_MATS_PANEL_BUTTONS_GROUPS && pItem.y == i));
		}
		for (int i = 0; i < MatsPanelData.numGroups; i++) {
			if (i == getMatsPanelActive ()) {
				iCurrentTexture = UtilsGL.setTexture (matsPanelTilesON[i], iCurrentTexture);
				drawTile (matsPanelTilesON[i], matsPanelIconPoints[i].x + tileBottomItem.getTileWidth () / 2 - matsPanelTilesON[i].getTileWidth () / 2, matsPanelIconPoints[i].y + tileBottomItem.getTileHeight () / 2 - matsPanelTilesON[i].getTileHeight () / 2, (pItem != null && pItem.x == MOUSE_MATS_PANEL_BUTTONS_GROUPS && pItem.y == i));
			} else {
				iCurrentTexture = UtilsGL.setTexture (matsPanelTiles[i], iCurrentTexture);
				drawTile (matsPanelTiles[i], matsPanelIconPoints[i].x + tileBottomItem.getTileWidth () / 2 - matsPanelTiles[i].getTileWidth () / 2, matsPanelIconPoints[i].y + tileBottomItem.getTileHeight () / 2 - matsPanelTiles[i].getTileHeight () / 2, (pItem != null && pItem.x == MOUSE_MATS_PANEL_BUTTONS_GROUPS && pItem.y == i));
			}
		}

		// Scrolls
		if (matsIndexPages[getMatsPanelActive ()] > 0) {
			iCurrentTexture = UtilsGL.setTexture (tileScrollUp, iCurrentTexture);
			drawTile (tileScrollUp, matsPanelIconScrollUpPoint, (pItem != null && pItem.x == MOUSE_MATS_PANEL_BUTTONS_SCROLL_UP));
		} else {
			iCurrentTexture = UtilsGL.setTexture (tileScrollUpDisabled, iCurrentTexture);
			drawTile (tileScrollUpDisabled, matsPanelIconScrollUpPoint);
		}
		if (matsIndexPages[getMatsPanelActive ()] < (matsNumPages[getMatsPanelActive ()] - 1)) {
			iCurrentTexture = UtilsGL.setTexture (tileScrollDown, iCurrentTexture);
			drawTile (tileScrollDown, matsPanelIconScrollDownPoint, (pItem != null && pItem.x == MOUSE_MATS_PANEL_BUTTONS_SCROLL_DOWN));
		} else {
			iCurrentTexture = UtilsGL.setTexture (tileScrollDownDisabled, iCurrentTexture);
			drawTile (tileScrollDownDisabled, matsPanelIconScrollDownPoint);
		}

		// Icons
		int iIndex = matsIndexPages[getMatsPanelActive ()] * MATS_PANEL_MAX_ITEMS_PER_PAGE;
		int iMax = Math.min (MATS_PANEL_MAX_ITEMS_PER_PAGE, (MatsPanelData.tileGroups.get (getMatsPanelActive ()).size () - iIndex));
		iCurrentTexture = UtilsGL.setTexture (tileBottomItem, iCurrentTexture);
		for (int i = 0; i < iMax; i++) {
			drawTile (tileBottomItem, matsPanelItemPoints[i]);
		}
		Tile tile;
		for (int i = 0; i < iMax; i++) {
			if (MatsPanelData.tileGroups.get (getMatsPanelActive ()).size () > (i + iIndex)) {
				tile = MatsPanelData.tileGroups.get (getMatsPanelActive ()).get (i + iIndex);
				iCurrentTexture = UtilsGL.setTexture (tile, iCurrentTexture);
				// drawTile (tile, matsPanelItemPoints [i].x + tileBottomItem.getTileWidth () / 2 - tile.getTileWidth () / 2, matsPanelItemPoints [i].y + tileBottomItem.getTileHeight () / 2 - tile.getTileHeight () / 2, false); //BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT, false);
				drawTile (tile, matsPanelItemPoints[i].x + tileBottomItem.getTileWidth () / 2 - tile.getTileWidth () / 2, matsPanelItemPoints[i].y + tileBottomItem.getTileHeight () / 2 - BOTTOM_ITEM_HEIGHT / 2, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT, false); // BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT, false);
			}
		}
		UtilsGL.glEnd ();

		// Numbers
		GL11.glBindTexture (GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
		GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		UtilsGL.glBegin (GL11.GL_QUADS);
		String sText;
		for (int i = 0; i < iMax; i++) {
			if (MatsPanelData.tileGroups.get (getMatsPanelActive ()).size () > (i + iIndex)) {
				tile = MatsPanelData.tileGroups.get (getMatsPanelActive ()).get (i + iIndex);
				sText = Integer.toString (Item.getNumItems (UtilsIniHeaders.getIntIniHeader (tile.getIniHeader ()), false, World.MAP_DEPTH));
				UtilsGL.drawStringWithBorder (sText, matsPanelItemPoints[i].x + tileBottomItem.getTileWidth () / 2 - UtilFont.getWidth (sText) / 2, matsPanelItemPoints[i].y + tileBottomItem.getTileHeight () - UtilFont.MAX_HEIGHT, ColorGL.WHITE, ColorGL.BLACK);
			}
		}

		// Pages
		sText = (matsIndexPages[getMatsPanelActive ()] + 1) + " / " + matsNumPages[getMatsPanelActive ()]; //$NON-NLS-1$
		UtilsGL.drawString (sText, matsPanelPagesPositionPoint.x - UtilFont.getWidth (sText) / 2, matsPanelPagesPositionPoint.y, ColorGL.BLACK);
		UtilsGL.glEnd ();
	}


	private static boolean checkGroupsPanelEnabled (int iLivingsPanelActive) {
		return (iLivingsPanelActive == LIVINGS_PANEL_TYPE_CITIZENS && livingsPanelCitizensGroupActive != -1) || (iLivingsPanelActive == LIVINGS_PANEL_TYPE_SOLDIERS && livingsPanelSoldiersGroupActive != -1);
	}


	private void renderLivingsPanel (int mouseX, int mouseY, int mousePanel) {
		// Possible mini icon blinks?
		// Blink
		TutorialFlow tutorialFlow = null;
		if ((blinkTurns >= MAX_BLINK_TURNS / 2)) {
			if (Game.getCurrentMissionData () != null && ImagesPanel.getCurrentFlowIndex () >= 0 && ImagesPanel.getCurrentFlowIndex () < Game.getCurrentMissionData ().getTutorialFlows ().size ()) {
				tutorialFlow = Game.getCurrentMissionData ().getTutorialFlows ().get (ImagesPanel.getCurrentFlowIndex ());
			}
		}

		Point pItem = isMouseOnLivingsButtons (mouseX, mouseY);

		// XAVI GL11.glColor4f (1, 1, 1, 1);
		int iCurrentTexture = tileLivingsPanel[0].getTextureID ();
		GL11.glBindTexture (GL11.GL_TEXTURE_2D, iCurrentTexture);
		GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		UtilsGL.glBegin (GL11.GL_QUADS);
		renderBackground (tileLivingsPanel, livingsPanelPoint, LIVINGS_PANEL_WIDTH, LIVINGS_PANEL_HEIGHT);

		// Close button
		iCurrentTexture = UtilsGL.setTexture (tileButtonClose, iCurrentTexture);
		if (pItem != null && pItem.x == MOUSE_LIVINGS_PANEL_BUTTONS_CLOSE) {
			drawTile (tileButtonClose, livingsPanelClosePoint);
		} else {
			drawTile (tileButtonCloseDisabled, livingsPanelClosePoint);
		}

		// Groups panel
		if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_SOLDIERS) {
			// Subpanel
			iCurrentTexture = UtilsGL.setTexture (tileLivingsGroupPanel[0], iCurrentTexture);
			renderBackground (tileLivingsGroupPanel, livingsGroupPanelPoint, LIVINGS_PANEL_GROUPS_WIDTH, LIVINGS_PANEL_GROUPS_HEIGHT);

			// No-group icon
			if (livingsPanelSoldiersGroupActive == -1 || mousePanel == MOUSE_LIVINGS_PANEL_SGROUP_NOGROUP) {
				iCurrentTexture = UtilsGL.setTexture (tileLivingsNoGroupON, iCurrentTexture);
				drawTile (tileLivingsNoGroupON, livingsGroupPanelFirstIconPoint);
			} else {
				// Miramos si el grupo tiene miembros
				if (Game.getWorld ().getSoldierGroups ().getSoldiersWithoutGroup ().size () > 0) {
					iCurrentTexture = UtilsGL.setTexture (tileLivingsNoGroupGreen, iCurrentTexture);
					drawTile (tileLivingsNoGroupGreen, livingsGroupPanelFirstIconPoint);
				} else {
					iCurrentTexture = UtilsGL.setTexture (tileLivingsNoGroup, iCurrentTexture);
					drawTile (tileLivingsNoGroup, livingsGroupPanelFirstIconPoint);
				}
			}

			// Group icons
			for (int i = 0; i < SoldierGroups.MAX_GROUPS; i++) {
				if (livingsPanelSoldiersGroupActive == i || (pItem != null && pItem.x == MOUSE_LIVINGS_PANEL_SGROUP_GROUP && pItem.y == i)) {
					iCurrentTexture = UtilsGL.setTexture (tileLivingsGroupON, iCurrentTexture);
					drawTile (tileLivingsGroupON, livingsGroupPanelFirstIconPoint.x, livingsGroupPanelFirstIconPoint.y + (i + 1) * livingsGroupPanelIconsSeparation, false);
				} else {
					// Miramos si el grupo tiene miembros
					if (Game.getWorld ().getSoldierGroups ().getGroup (i).getLivingIDs ().size () > 0) {
						iCurrentTexture = UtilsGL.setTexture (tileLivingsGroupGreen, iCurrentTexture);
						drawTile (tileLivingsGroupGreen, livingsGroupPanelFirstIconPoint.x, livingsGroupPanelFirstIconPoint.y + (i + 1) * livingsGroupPanelIconsSeparation, false);
					} else {
						iCurrentTexture = UtilsGL.setTexture (tileLivingsGroup, iCurrentTexture);
						drawTile (tileLivingsGroup, livingsGroupPanelFirstIconPoint.x, livingsGroupPanelFirstIconPoint.y + (i + 1) * livingsGroupPanelIconsSeparation, false);
					}
				}
			}

			// Single group subpanel
			if (livingsPanelSoldiersGroupActive != -1) {
				iCurrentTexture = UtilsGL.setTexture (tileLivingsGroupPanel[0], iCurrentTexture);
				renderBackground (tileLivingsGroupPanel, livingsSingleGroupPanelPoint, LIVINGS_PANEL_SINGLE_GROUP_WIDTH, LIVINGS_PANEL_SINGLE_GROUP_HEIGHT);

				int iGroupState = Game.getWorld ().getSoldierGroups ().getGroup (livingsPanelSoldiersGroupActive).getState ();
				// Botones
				if (mousePanel == MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_RENAME) {
					iCurrentTexture = UtilsGL.setTexture (tileLivingsSingleGroupRenameON, iCurrentTexture);
					drawTile (tileLivingsSingleGroupRenameON, livingsSingleGroupRenamePoint);
				} else {
					iCurrentTexture = UtilsGL.setTexture (tileLivingsSingleGroupRename, iCurrentTexture);
					drawTile (tileLivingsSingleGroupRename, livingsSingleGroupRenamePoint);
				}
				if (iGroupState == SoldierGroupData.STATE_GUARD || mousePanel == MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_GUARD) {
					iCurrentTexture = UtilsGL.setTexture (tileLivingsSingleGroupGuardON, iCurrentTexture);
					drawTile (tileLivingsSingleGroupGuardON, livingsSingleGroupGuardPoint);
				} else {
					iCurrentTexture = UtilsGL.setTexture (tileLivingsSingleGroupGuard, iCurrentTexture);
					drawTile (tileLivingsSingleGroupGuard, livingsSingleGroupGuardPoint);
				}
				if (iGroupState == SoldierGroupData.STATE_PATROL || mousePanel == MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_PATROL) {
					iCurrentTexture = UtilsGL.setTexture (tileLivingsSingleGroupPatrolON, iCurrentTexture);
					drawTile (tileLivingsSingleGroupPatrolON, livingsSingleGroupPatrolPoint);
				} else {
					iCurrentTexture = UtilsGL.setTexture (tileLivingsSingleGroupPatrol, iCurrentTexture);
					drawTile (tileLivingsSingleGroupPatrol, livingsSingleGroupPatrolPoint);
				}
				if (iGroupState == SoldierGroupData.STATE_BOSS || mousePanel == MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_BOSS) {
					iCurrentTexture = UtilsGL.setTexture (tileLivingsSingleGroupBossON, iCurrentTexture);
					drawTile (tileLivingsSingleGroupBossON, livingsSingleGroupBossPoint);
				} else {
					iCurrentTexture = UtilsGL.setTexture (tileLivingsSingleGroupBoss, iCurrentTexture);
					drawTile (tileLivingsSingleGroupBoss, livingsSingleGroupBossPoint);
				}
				if (mousePanel == MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_DISBAND) {
					iCurrentTexture = UtilsGL.setTexture (tileLivingsSingleGroupDisbandON, iCurrentTexture);
					drawTile (tileLivingsSingleGroupDisbandON, livingsSingleGroupDisbandPoint);
				} else {
					iCurrentTexture = UtilsGL.setTexture (tileLivingsSingleGroupDisband, iCurrentTexture);
					drawTile (tileLivingsSingleGroupDisband, livingsSingleGroupDisbandPoint);
				}
				if (mousePanel == MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_AUTOEQUIP) {
					iCurrentTexture = UtilsGL.setTexture (tileLivingsRowAutoequipON, iCurrentTexture);
					drawTile (tileLivingsRowAutoequipON, livingsSingleGroupAutoequipPoint);
				} else {
					iCurrentTexture = UtilsGL.setTexture (tileLivingsRowAutoequip, iCurrentTexture);
					drawTile (tileLivingsRowAutoequip, livingsSingleGroupAutoequipPoint);
				}

				// Text
				UtilsGL.glEnd ();
				iCurrentTexture = Game.TEXTURE_FONT_ID;
				GL11.glBindTexture (GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
				GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
				UtilsGL.glBegin (GL11.GL_QUADS);

				// Group name
				String sText = Game.getWorld ().getSoldierGroups ().getGroup (livingsPanelSoldiersGroupActive).getName ();
				UtilsGL.drawStringWithBorder (sText, livingsSingleGroupPanelPoint.x + LIVINGS_PANEL_SINGLE_GROUP_WIDTH / 2 - UtilFont.getWidth (sText) / 2, livingsSingleGroupPanelPoint.y, ColorGL.WHITE, ColorGL.BLACK);
			}

			// Text
			if (iCurrentTexture != Game.TEXTURE_FONT_ID) {
				UtilsGL.glEnd ();
				iCurrentTexture = Game.TEXTURE_FONT_ID;
				GL11.glBindTexture (GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
				GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
				UtilsGL.glBegin (GL11.GL_QUADS);
			}

			// Group icons (1, 2, 3, 4, 5, 6, 7, 8)
			String sNumber;
			for (int i = 0; i < SoldierGroups.MAX_GROUPS; i++) {
				sNumber = Integer.toString (i + 1);
				UtilsGL.drawStringWithBorder (sNumber, livingsGroupPanelFirstIconPoint.x + tileLivingsGroup.getTileWidth () / 2 - UtilFont.getWidth (sNumber) / 2, livingsGroupPanelFirstIconPoint.y + (i + 1) * livingsGroupPanelIconsSeparation + tileLivingsGroup.getTileHeight () / 2 - UtilFont.MAX_HEIGHT / 2, ColorGL.WHITE, ColorGL.BLACK);
			}
		} else if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_CITIZENS) {
			// Subpanel
			iCurrentTexture = UtilsGL.setTexture (tileLivingsGroupPanel[0], iCurrentTexture);
			renderBackground (tileLivingsGroupPanel, livingsGroupPanelPoint, LIVINGS_PANEL_GROUPS_WIDTH, LIVINGS_PANEL_GROUPS_HEIGHT);

			// No-group icon
			if (livingsPanelCitizensGroupActive == -1 || mousePanel == MOUSE_LIVINGS_PANEL_CGROUP_NOGROUP) {
				iCurrentTexture = UtilsGL.setTexture (tileLivingsNoJobGroupON, iCurrentTexture);
				drawTile (tileLivingsNoJobGroupON, livingsGroupPanelFirstIconPoint);
			} else {
				// Miramos si el grupo tiene miembros
				if (Game.getWorld ().getCitizenGroups ().getCitizensWithoutGroup ().size () > 0) {
					iCurrentTexture = UtilsGL.setTexture (tileLivingsNoJobGroupGreen, iCurrentTexture);
					drawTile (tileLivingsNoJobGroupGreen, livingsGroupPanelFirstIconPoint);
				} else {
					iCurrentTexture = UtilsGL.setTexture (tileLivingsNoJobGroup, iCurrentTexture);
					drawTile (tileLivingsNoJobGroup, livingsGroupPanelFirstIconPoint);
				}
			}

			// Group icons
			for (int i = 0; i < CitizenGroups.MAX_GROUPS; i++) {
				if (livingsPanelCitizensGroupActive == i || (pItem != null && pItem.x == MOUSE_LIVINGS_PANEL_CGROUP_GROUP && pItem.y == i)) {
					iCurrentTexture = UtilsGL.setTexture (tileLivingsJobGroupON, iCurrentTexture);
					drawTile (tileLivingsJobGroupON, livingsGroupPanelFirstIconPoint.x, livingsGroupPanelFirstIconPoint.y + (i + 1) * livingsGroupPanelIconsSeparation, false);
				} else {
					// Miramos si el grupo tiene miembros
					if (Game.getWorld ().getCitizenGroups ().getGroup (i).getLivingIDs ().size () > 0) {
						iCurrentTexture = UtilsGL.setTexture (tileLivingsJobGroupGreen, iCurrentTexture);
						drawTile (tileLivingsJobGroupGreen, livingsGroupPanelFirstIconPoint.x, livingsGroupPanelFirstIconPoint.y + (i + 1) * livingsGroupPanelIconsSeparation, false);
					} else {
						iCurrentTexture = UtilsGL.setTexture (tileLivingsJobGroup, iCurrentTexture);
						drawTile (tileLivingsJobGroup, livingsGroupPanelFirstIconPoint.x, livingsGroupPanelFirstIconPoint.y + (i + 1) * livingsGroupPanelIconsSeparation, false);
					}
				}
			}

			// Single group subpanel
			if (livingsPanelCitizensGroupActive != -1) {
				iCurrentTexture = UtilsGL.setTexture (tileLivingsGroupPanel[0], iCurrentTexture);
				renderBackground (tileLivingsGroupPanel, livingsSingleGroupPanelPoint, LIVINGS_PANEL_SINGLE_GROUP_WIDTH, LIVINGS_PANEL_SINGLE_GROUP_HEIGHT);

				// Botones
				if (mousePanel == MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_RENAME) {
					iCurrentTexture = UtilsGL.setTexture (tileLivingsSingleJobGroupRenameON, iCurrentTexture);
					drawTile (tileLivingsSingleJobGroupRenameON, livingsSingleGroupRenamePoint);
				} else {
					iCurrentTexture = UtilsGL.setTexture (tileLivingsSingleJobGroupRename, iCurrentTexture);
					drawTile (tileLivingsSingleJobGroupRename, livingsSingleGroupRenamePoint);
				}
				if (mousePanel == MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_CHANGE_JOBS) {
					iCurrentTexture = UtilsGL.setTexture (tileLivingsSingleGroupChangeJobsON, iCurrentTexture);
					drawTile (tileLivingsSingleGroupChangeJobsON, livingsSingleGroupChangeJobsPoint);
				} else {
					iCurrentTexture = UtilsGL.setTexture (tileLivingsSingleGroupChangeJobs, iCurrentTexture);
					drawTile (tileLivingsSingleGroupChangeJobs, livingsSingleGroupChangeJobsPoint);
				}
				if (mousePanel == MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_DISBAND) {
					iCurrentTexture = UtilsGL.setTexture (tileLivingsSingleJobGroupDisbandON, iCurrentTexture);
					drawTile (tileLivingsSingleJobGroupDisbandON, livingsSingleGroupDisbandPoint);
				} else {
					iCurrentTexture = UtilsGL.setTexture (tileLivingsSingleJobGroupDisband, iCurrentTexture);
					drawTile (tileLivingsSingleJobGroupDisband, livingsSingleGroupDisbandPoint);
				}
				if (mousePanel == MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_AUTOEQUIP) {
					iCurrentTexture = UtilsGL.setTexture (tileLivingsRowAutoequipON, iCurrentTexture);
					drawTile (tileLivingsRowAutoequipON, livingsSingleGroupAutoequipPoint);
				} else {
					iCurrentTexture = UtilsGL.setTexture (tileLivingsRowAutoequip, iCurrentTexture);
					drawTile (tileLivingsRowAutoequip, livingsSingleGroupAutoequipPoint);
				}

				// Text
				UtilsGL.glEnd ();
				iCurrentTexture = Game.TEXTURE_FONT_ID;
				GL11.glBindTexture (GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
				GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
				UtilsGL.glBegin (GL11.GL_QUADS);

				// Group name
				String sText = Game.getWorld ().getCitizenGroups ().getGroup (livingsPanelCitizensGroupActive).getName ();
				UtilsGL.drawStringWithBorder (sText, livingsSingleGroupPanelPoint.x + LIVINGS_PANEL_SINGLE_GROUP_WIDTH / 2 - UtilFont.getWidth (sText) / 2, livingsSingleGroupPanelPoint.y, ColorGL.WHITE, ColorGL.BLACK);
			}

			// Text
			if (iCurrentTexture != Game.TEXTURE_FONT_ID) {
				UtilsGL.glEnd ();
				iCurrentTexture = Game.TEXTURE_FONT_ID;
				GL11.glBindTexture (GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
				GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
				UtilsGL.glBegin (GL11.GL_QUADS);
			}

			// Group icons (1, 2, 3, 4, 5, 6, 7, 8)
			String sNumber;
			for (int i = 0; i < CitizenGroups.MAX_GROUPS; i++) {
				sNumber = Integer.toString (i + 1);
				UtilsGL.drawStringWithBorder (sNumber, livingsGroupPanelFirstIconPoint.x + tileLivingsGroup.getTileWidth () / 2 - UtilFont.getWidth (sNumber) / 2, livingsGroupPanelFirstIconPoint.y + (i + 1) * livingsGroupPanelIconsSeparation + tileLivingsGroup.getTileHeight () / 2 - UtilFont.MAX_HEIGHT / 2, ColorGL.WHITE, ColorGL.BLACK);
			}
		}

		// Restrict
		if ((getLivingsPanelActive () == LIVINGS_PANEL_TYPE_CITIZENS && livingsPanelCitizensGroupActive == -1) || (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_HEROES)) {
			if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsRestriction ()) {
				UtilsGL.setColorRed ();
			}
			iCurrentTexture = UtilsGL.setTexture (tileIconLevelUp, iCurrentTexture);
			drawTile (tileIconLevelUp, livingsPanelIconRestrictUpPoint, ICON_WIDTH, ICON_HEIGHT, mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_UP);
			iCurrentTexture = UtilsGL.setTexture (tileIconLevelDown, iCurrentTexture);
			drawTile (tileIconLevelDown, livingsPanelIconRestrictDownPoint, ICON_WIDTH, ICON_HEIGHT, mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_DOWN);
			if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsRestriction ()) {
				UtilsGL.unsetColor ();
			}
		}

		ArrayList<Integer> alLivingIDs = getLivings ();
		int iNumLivings;
		if (alLivingIDs != null) {
			iNumLivings = alLivingIDs.size ();
		} else {
			iNumLivings = 0;
		}
		if (iNumLivings == 0) {
			UtilsGL.glEnd ();

			String sText;
			if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_CITIZENS) {
				sText = Messages.getString ("UIPanel.34"); //$NON-NLS-1$
			} else if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_SOLDIERS) {
				sText = Messages.getString ("UIPanel.37"); //$NON-NLS-1$
			} else if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_HEROES) {
				sText = Messages.getString ("UIPanel.38"); //$NON-NLS-1$
			} else {
				sText = Messages.getString ("UIPanel.39"); //$NON-NLS-1$
			}

			int iTextWidth = UtilFont.getWidth (sText);
			GL11.glBindTexture (GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
			GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
			UtilsGL.glBegin (GL11.GL_QUADS);
			UtilsGL.drawStringWithBorder (sText, livingsPanelPoint.x + LIVINGS_PANEL_WIDTH / 2 - iTextWidth / 2, livingsPanelPoint.y + LIVINGS_PANEL_HEIGHT / 2 - UtilFont.MAX_HEIGHT / 2, ColorGL.ORANGE, ColorGL.BLACK);

			// Restrict
			if ((getLivingsPanelActive () == LIVINGS_PANEL_TYPE_CITIZENS && livingsPanelCitizensGroupActive == -1) || (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_HEROES)) {
				int iLevel;
				if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_CITIZENS) {
					iLevel = Game.getWorld ().getRestrictHaulEquippingLevel ();
				} else {
					iLevel = Game.getWorld ().getRestrictExploringLevel ();
				}
				iLevel = World.MAP_NUM_LEVELS_OUTSIDE - iLevel;
				sText = Integer.toString (iLevel);
				UtilsGL.drawString (sText, (livingsPanelIconRestrictUpPoint.x + tileIconLevelUp.getTileWidth ()) + ((livingsPanelIconRestrictDownPoint.x) - (livingsPanelIconRestrictUpPoint.x + tileIconLevelUp.getTileWidth ())) / 2 - UtilFont.getWidth (sText) / 2, livingsPanelIconRestrictUpPoint.y + tileIconLevelUp.getTileHeight () / 2 - UtilFont.MAX_HEIGHT / 2, ColorGL.BLACK);
			}

			UtilsGL.glEnd ();

			return;
		}

		// Num livigs > 0, comprobamos índices
		int iNumPages = (iNumLivings % LIVINGS_PANEL_MAX_ROWS == 0) ? iNumLivings / LIVINGS_PANEL_MAX_ROWS : (iNumLivings / LIVINGS_PANEL_MAX_ROWS) + 1;
		int iIndexPage;
		boolean bNoGroupsPanel = !checkGroupsPanelEnabled (getLivingsPanelActive ());
		if (bNoGroupsPanel) {
			iIndexPage = livingsDataIndexPages[getLivingsPanelActive ()];
		} else {
			if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_CITIZENS) {
				iIndexPage = livingsDataIndexPagesCitizenGroups[livingsPanelCitizensGroupActive];
			} else {
				iIndexPage = livingsDataIndexPagesSoldierGroups[livingsPanelSoldiersGroupActive];
			}
		}
		if (iIndexPage > iNumPages) {
			if (bNoGroupsPanel) {
				livingsDataIndexPages[getLivingsPanelActive ()] = iNumPages;
			} else {
				if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_CITIZENS) {
					livingsDataIndexPagesCitizenGroups[livingsPanelCitizensGroupActive] = iNumPages;
				} else {
					livingsDataIndexPagesSoldierGroups[livingsPanelSoldiersGroupActive] = iNumPages;
				}
			}
			iIndexPage = iNumPages;
		} else if (iIndexPage < 1) {
			if (bNoGroupsPanel) {
				livingsDataIndexPages[getLivingsPanelActive ()] = 1;
			} else {
				if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_CITIZENS) {
					livingsDataIndexPagesCitizenGroups[livingsPanelCitizensGroupActive] = 1;
				} else {
					livingsDataIndexPagesSoldierGroups[livingsPanelSoldiersGroupActive] = 1;
				}
			}
			iIndexPage = 1;
		}

		// Scrolls
		if (iIndexPage > 1) {
			iCurrentTexture = UtilsGL.setTexture (tileScrollUp, iCurrentTexture);
			drawTile (tileScrollUp, livingsPanelIconScrollUpPoint, (pItem != null && pItem.x == MOUSE_LIVINGS_PANEL_BUTTONS_SCROLL_UP));
		} else {
			iCurrentTexture = UtilsGL.setTexture (tileScrollUpDisabled, iCurrentTexture);
			drawTile (tileScrollUpDisabled, livingsPanelIconScrollUpPoint);
		}
		if (iIndexPage < iNumPages) {
			iCurrentTexture = UtilsGL.setTexture (tileScrollDown, iCurrentTexture);
			drawTile (tileScrollDown, livingsPanelIconScrollDownPoint, (pItem != null && pItem.x == MOUSE_LIVINGS_PANEL_BUTTONS_SCROLL_DOWN));
		} else {
			iCurrentTexture = UtilsGL.setTexture (tileScrollDownDisabled, iCurrentTexture);
			drawTile (tileScrollDownDisabled, livingsPanelIconScrollDownPoint);
		}

		// Rows + equipment
		// Livings pictures + military stuff + civ/soldier stuff
		int iMaxRows = Math.min (iNumLivings - ((iIndexPage - 1) * LIVINGS_PANEL_MAX_ROWS), livingsPanelRowPoints.length);
		iMaxRows = Math.min (iMaxRows, LIVINGS_PANEL_MAX_ROWS);

		iCurrentTexture = UtilsGL.setTexture (tileBottomItem, iCurrentTexture);
		for (int i = 0; i < iMaxRows; i++) {
			if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsLivings ()) {
				UtilsGL.setColorRed ();
			}
			drawTile (tileBottomItem, livingsPanelRowPoints[i]);
			if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsLivings ()) {
				UtilsGL.unsetColor ();
			}
			if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsBody ()) {
				UtilsGL.setColorRed ();
			}
			drawTile (tileBottomItem, livingsPanelRowHeadPoints[i]);
			drawTile (tileBottomItem, livingsPanelRowBodyPoints[i]);
			drawTile (tileBottomItem, livingsPanelRowLegsPoints[i]);
			drawTile (tileBottomItem, livingsPanelRowFeetPoints[i]);
			drawTile (tileBottomItem, livingsPanelRowWeaponPoints[i]);
			if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsBody ()) {
				UtilsGL.unsetColor ();
			}
		}
		LivingEntity le;
		int iIndex;
		for (int i = 0; i < iMaxRows; i++) {
			iIndex = ((iIndexPage - 1) * LIVINGS_PANEL_MAX_ROWS) + i;
			if (iIndex >= 0 && iIndex < alLivingIDs.size ()) {
				// Living
				le = World.getLivingEntityByID (alLivingIDs.get (iIndex));

				iCurrentTexture = renderLiving (le, livingsPanelRowPoints[i].x + tileBottomItem.getTileWidth () / 2 - le.getTileWidth () / 2, livingsPanelRowPoints[i].y + tileBottomItem.getTileHeight () / 2 - le.getTileHeight () / 2, getLivingsPanelActive (), iCurrentTexture);

				EquippedData equippedData = le.getEquippedData ();
				// Head
				if (equippedData.isWearing (MilitaryItem.LOCATION_HEAD)) {
					MilitaryItem mi = equippedData.getHead ();
					iCurrentTexture = UtilsGL.setTexture (mi, iCurrentTexture);
					drawTile (mi, livingsPanelRowHeadPoints[i].x + tileBottomItem.getTileWidth () / 2 - mi.getTileWidth () / 2, livingsPanelRowHeadPoints[i].y + tileBottomItem.getTileHeight () / 2 - mi.getTileHeight () / 2, false);
				} else {
					iCurrentTexture = UtilsGL.setTexture (tileLivingsPanelRowNoHead, iCurrentTexture);
					drawTile (tileLivingsPanelRowNoHead, livingsPanelRowHeadPoints[i].x + tileBottomItem.getTileWidth () / 2 - tileLivingsPanelRowNoHead.getTileWidth () / 2, livingsPanelRowHeadPoints[i].y + tileBottomItem.getTileHeight () / 2 - tileLivingsPanelRowNoHead.getTileHeight () / 2, false);
				}
				// Body
				if (equippedData.isWearing (MilitaryItem.LOCATION_BODY)) {
					MilitaryItem mi = equippedData.getBody ();
					iCurrentTexture = UtilsGL.setTexture (mi, iCurrentTexture);
					drawTile (mi, livingsPanelRowBodyPoints[i].x + tileBottomItem.getTileWidth () / 2 - mi.getTileWidth () / 2, livingsPanelRowBodyPoints[i].y + tileBottomItem.getTileHeight () / 2 - mi.getTileHeight () / 2, false);
				} else {
					iCurrentTexture = UtilsGL.setTexture (tileLivingsPanelRowNoBody, iCurrentTexture);
					drawTile (tileLivingsPanelRowNoBody, livingsPanelRowBodyPoints[i].x + tileBottomItem.getTileWidth () / 2 - tileLivingsPanelRowNoBody.getTileWidth () / 2, livingsPanelRowBodyPoints[i].y + tileBottomItem.getTileHeight () / 2 - tileLivingsPanelRowNoBody.getTileHeight () / 2, false);
				}
				// Legs
				if (equippedData.isWearing (MilitaryItem.LOCATION_LEGS)) {
					MilitaryItem mi = equippedData.getLegs ();
					iCurrentTexture = UtilsGL.setTexture (mi, iCurrentTexture);
					drawTile (mi, livingsPanelRowLegsPoints[i].x + tileBottomItem.getTileWidth () / 2 - mi.getTileWidth () / 2, livingsPanelRowLegsPoints[i].y + tileBottomItem.getTileHeight () / 2 - mi.getTileHeight () / 2, false);
				} else {
					iCurrentTexture = UtilsGL.setTexture (tileLivingsPanelRowNoLegs, iCurrentTexture);
					drawTile (tileLivingsPanelRowNoLegs, livingsPanelRowLegsPoints[i].x + tileBottomItem.getTileWidth () / 2 - tileLivingsPanelRowNoLegs.getTileWidth () / 2, livingsPanelRowLegsPoints[i].y + tileBottomItem.getTileHeight () / 2 - tileLivingsPanelRowNoLegs.getTileHeight () / 2, false);
				}
				// Feet
				if (equippedData.isWearing (MilitaryItem.LOCATION_FEET)) {
					MilitaryItem mi = equippedData.getFeet ();
					iCurrentTexture = UtilsGL.setTexture (mi, iCurrentTexture);
					drawTile (mi, livingsPanelRowFeetPoints[i].x + tileBottomItem.getTileWidth () / 2 - mi.getTileWidth () / 2, livingsPanelRowFeetPoints[i].y + tileBottomItem.getTileHeight () / 2 - mi.getTileHeight () / 2, false);
				} else {
					iCurrentTexture = UtilsGL.setTexture (tileLivingsPanelRowNoFeet, iCurrentTexture);
					drawTile (tileLivingsPanelRowNoFeet, livingsPanelRowFeetPoints[i].x + tileBottomItem.getTileWidth () / 2 - tileLivingsPanelRowNoFeet.getTileWidth () / 2, livingsPanelRowFeetPoints[i].y + tileBottomItem.getTileHeight () / 2 - tileLivingsPanelRowNoFeet.getTileHeight () / 2, false);
				}
				// Weapon
				if (equippedData.isWearing (MilitaryItem.LOCATION_WEAPON)) {
					MilitaryItem mi = equippedData.getWeapon ();
					iCurrentTexture = UtilsGL.setTexture (mi, iCurrentTexture);
					drawTile (mi, livingsPanelRowWeaponPoints[i].x + tileBottomItem.getTileWidth () / 2 - mi.getTileWidth () / 2, livingsPanelRowWeaponPoints[i].y + tileBottomItem.getTileHeight () / 2 - mi.getTileHeight () / 2, false);
				} else {
					iCurrentTexture = UtilsGL.setTexture (tileLivingsPanelRowNoWeapon, iCurrentTexture);
					drawTile (tileLivingsPanelRowNoWeapon, livingsPanelRowWeaponPoints[i].x + tileBottomItem.getTileWidth () / 2 - tileLivingsPanelRowNoWeapon.getTileWidth () / 2, livingsPanelRowWeaponPoints[i].y + tileBottomItem.getTileHeight () / 2 - tileLivingsPanelRowNoWeapon.getTileHeight () / 2, false);
				}

				// Autoequip
				if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_CITIZENS || getLivingsPanelActive () == LIVINGS_PANEL_TYPE_SOLDIERS) {
					if (pItem != null && pItem.x == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_AUTOEQUIP && pItem.y == i) {
						iCurrentTexture = UtilsGL.setTexture (tileLivingsRowAutoequipON, iCurrentTexture);
						if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsAutoequip ()) {
							UtilsGL.setColorRed ();
						}
						drawTile (tileLivingsRowAutoequipON, livingsPanelRowAutoequipPoints[i]);
						if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsAutoequip ()) {
							UtilsGL.unsetColor ();
						}
					} else {
						iCurrentTexture = UtilsGL.setTexture (tileLivingsRowAutoequip, iCurrentTexture);
						if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsAutoequip ()) {
							UtilsGL.setColorRed ();
						}
						drawTile (tileLivingsRowAutoequip, livingsPanelRowAutoequipPoints[i]);
						if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsAutoequip ()) {
							UtilsGL.unsetColor ();
						}
					}
				}

				// Civ/soldier stuff
				if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_CITIZENS) {
					if (pItem != null && pItem.x == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER && pItem.y == i) {
						iCurrentTexture = UtilsGL.setTexture (tileLivingsRowConvertSoldierON, iCurrentTexture);
						if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsConvertSoldier ()) {
							UtilsGL.setColorRed ();
						}
						drawTile (tileLivingsRowConvertSoldierON, livingsPanelRowConvertCivilianSoldierPoints[i]);
						if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsConvertSoldier ()) {
							UtilsGL.unsetColor ();
						}
					} else {
						iCurrentTexture = UtilsGL.setTexture (tileLivingsRowConvertSoldier, iCurrentTexture);
						if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsConvertSoldier ()) {
							UtilsGL.setColorRed ();
						}
						drawTile (tileLivingsRowConvertSoldier, livingsPanelRowConvertCivilianSoldierPoints[i]);
						if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsConvertSoldier ()) {
							UtilsGL.unsetColor ();
						}
					}
					if (livingsPanelCitizensGroupActive == -1) {
						if (pItem != null && pItem.x == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_PROFESSIONS && pItem.y == i) {
							iCurrentTexture = UtilsGL.setTexture (tileLivingsRowProfessionON, iCurrentTexture);
							if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsJobs ()) {
								UtilsGL.setColorRed ();
							}
							drawTile (tileLivingsRowProfessionON, livingsPanelRowProfessionPoints[i]);
							if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsJobs ()) {
								UtilsGL.unsetColor ();
							}
						} else {
							iCurrentTexture = UtilsGL.setTexture (tileLivingsRowProfession, iCurrentTexture);
							if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsJobs ()) {
								UtilsGL.setColorRed ();
							}
							drawTile (tileLivingsRowProfession, livingsPanelRowProfessionPoints[i]);
							if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsJobs ()) {
								UtilsGL.unsetColor ();
							}
						}
					}
					if (pItem != null && pItem.x == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_JOBS_GROUPS_ADDREMOVE && pItem.y == i) {
						iCurrentTexture = UtilsGL.setTexture (tileLivingsRowJobsGroupsON, iCurrentTexture);
						if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsGroup ()) {
							UtilsGL.setColorRed ();
						}
						drawTile (tileLivingsRowJobsGroupsON, livingsPanelRowJobsGroupsPoints[i]);
						if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsGroup ()) {
							UtilsGL.unsetColor ();
						}
					} else {
						iCurrentTexture = UtilsGL.setTexture (tileLivingsRowJobsGroups, iCurrentTexture);
						if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsGroup ()) {
							UtilsGL.setColorRed ();
						}
						drawTile (tileLivingsRowJobsGroups, livingsPanelRowJobsGroupsPoints[i]);
						if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsGroup ()) {
							UtilsGL.unsetColor ();
						}
					}
				} else if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_SOLDIERS) {
					if (pItem != null && pItem.x == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_CIVILIAN && pItem.y == i) {
						iCurrentTexture = UtilsGL.setTexture (tileLivingsRowConvertCivilianON, iCurrentTexture);
						if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsConvertCivilian ()) {
							UtilsGL.setColorRed ();
						}
						drawTile (tileLivingsRowConvertCivilianON, livingsPanelRowConvertCivilianSoldierPoints[i]);
						if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsConvertCivilian ()) {
							UtilsGL.unsetColor ();
						}
					} else {
						iCurrentTexture = UtilsGL.setTexture (tileLivingsRowConvertCivilian, iCurrentTexture);
						if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsConvertCivilian ()) {
							UtilsGL.setColorRed ();
						}
						drawTile (tileLivingsRowConvertCivilian, livingsPanelRowConvertCivilianSoldierPoints[i]);
						if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsConvertCivilian ()) {
							UtilsGL.unsetColor ();
						}
					}

					// Soldier type
					Citizen soldier = ((Citizen) le);
					int soldierState = soldier.getSoldierData ().getState ();
					if (livingsPanelSoldiersGroupActive == -1) {
						if (soldierState == SoldierData.STATE_GUARD || (pItem != null && pItem.x == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_GUARD && pItem.y == i)) {
							iCurrentTexture = UtilsGL.setTexture (tileLivingsRowConvertSoldierGuardON, iCurrentTexture);
							if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsGuard ()) {
								UtilsGL.setColorRed ();
							}
							drawTile (tileLivingsRowConvertSoldierGuardON, livingsPanelRowConvertSoldierGuardPoints[i]);
							if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsGuard ()) {
								UtilsGL.unsetColor ();
							}
						} else {
							iCurrentTexture = UtilsGL.setTexture (tileLivingsRowConvertSoldierGuard, iCurrentTexture);
							if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsGuard ()) {
								UtilsGL.setColorRed ();
							}
							drawTile (tileLivingsRowConvertSoldierGuard, livingsPanelRowConvertSoldierGuardPoints[i]);
							if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsGuard ()) {
								UtilsGL.unsetColor ();
							}
						}
						if (soldierState == SoldierData.STATE_PATROL || (pItem != null && pItem.x == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_PATROL && pItem.y == i)) {
							iCurrentTexture = UtilsGL.setTexture (tileLivingsRowConvertSoldierPatrolON, iCurrentTexture);
							if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsPatrol ()) {
								UtilsGL.setColorRed ();
							}
							drawTile (tileLivingsRowConvertSoldierPatrolON, livingsPanelRowConvertSoldierPatrolPoints[i]);
							if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsPatrol ()) {
								UtilsGL.unsetColor ();
							}
						} else {
							iCurrentTexture = UtilsGL.setTexture (tileLivingsRowConvertSoldierPatrol, iCurrentTexture);
							if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsPatrol ()) {
								UtilsGL.setColorRed ();
							}
							drawTile (tileLivingsRowConvertSoldierPatrol, livingsPanelRowConvertSoldierPatrolPoints[i]);
							if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsPatrol ()) {
								UtilsGL.unsetColor ();
							}
						}
						if (soldierState == SoldierData.STATE_BOSS_AROUND || (pItem != null && pItem.x == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_BOSS && pItem.y == i)) {
							iCurrentTexture = UtilsGL.setTexture (tileLivingsRowConvertSoldierBossON, iCurrentTexture);
							if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsBoss ()) {
								UtilsGL.setColorRed ();
							}
							drawTile (tileLivingsRowConvertSoldierBossON, livingsPanelRowConvertSoldierBossPoints[i]);
							if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsBoss ()) {
								UtilsGL.unsetColor ();
							}
						} else {
							iCurrentTexture = UtilsGL.setTexture (tileLivingsRowConvertSoldierBoss, iCurrentTexture);
							if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsBoss ()) {
								UtilsGL.setColorRed ();
							}
							drawTile (tileLivingsRowConvertSoldierBoss, livingsPanelRowConvertSoldierBossPoints[i]);
							if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsBoss ()) {
								UtilsGL.unsetColor ();
							}
						}

						if (soldierState == SoldierData.STATE_IN_A_GROUP || (pItem != null && pItem.x == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_SGROUP_ADD && pItem.y == i)) {
							iCurrentTexture = UtilsGL.setTexture (tileLivingsRowGroupAddON, iCurrentTexture);
							drawTile (tileLivingsRowGroupAddON, livingsPanelRowGroupPoints[i]);
						} else {
							iCurrentTexture = UtilsGL.setTexture (tileLivingsRowGroupAdd, iCurrentTexture);
							drawTile (tileLivingsRowGroupAdd, livingsPanelRowGroupPoints[i]);
						}
					} else {
						if (pItem != null && pItem.x == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_SGROUP_REMOVE && pItem.y == i) {
							iCurrentTexture = UtilsGL.setTexture (tileLivingsRowGroupRemoveON, iCurrentTexture);
							drawTile (tileLivingsRowGroupRemoveON, livingsPanelRowGroupPoints[i]);
						} else {
							iCurrentTexture = UtilsGL.setTexture (tileLivingsRowGroupRemove, iCurrentTexture);
							drawTile (tileLivingsRowGroupRemove, livingsPanelRowGroupPoints[i]);
						}
					}
				}
			}
		}

		UtilsGL.glEnd ();

		// Text
		// Pages
		String sText = iIndexPage + " / " + iNumPages; //$NON-NLS-1$
		GL11.glBindTexture (GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
		GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		UtilsGL.glBegin (GL11.GL_QUADS);
		UtilsGL.drawString (sText, livingsPanelPagesPoint.x - UtilFont.getWidth (sText) / 2, livingsPanelPagesPoint.y, ColorGL.BLACK);

		// Restrict
		if ((getLivingsPanelActive () == LIVINGS_PANEL_TYPE_CITIZENS && livingsPanelCitizensGroupActive == -1) || (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_HEROES)) {
			int iLevel;
			if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_CITIZENS) {
				iLevel = Game.getWorld ().getRestrictHaulEquippingLevel ();
			} else {
				iLevel = Game.getWorld ().getRestrictExploringLevel ();
			}
			iLevel = World.MAP_NUM_LEVELS_OUTSIDE - iLevel;
			sText = Integer.toString (iLevel);
			UtilsGL.drawString (sText, (livingsPanelIconRestrictUpPoint.x + tileIconLevelUp.getTileWidth ()) + ((livingsPanelIconRestrictDownPoint.x) - (livingsPanelIconRestrictUpPoint.x + tileIconLevelUp.getTileWidth ())) / 2 - UtilFont.getWidth (sText) / 2, livingsPanelIconRestrictUpPoint.y + tileIconLevelUp.getTileHeight () / 2 - UtilFont.MAX_HEIGHT / 2, ColorGL.BLACK);
		}

		UtilsGL.glEnd ();
	}


	private int renderLiving (LivingEntity le, int x, int y, int iType, int iCurrentTexture) {
		// Render
		iCurrentTexture = UtilsGL.setTexture (le, iCurrentTexture);
		UtilsGL.drawTexture (x, y, x + le.getTileWidth (), y + le.getTileHeight (), le.getBaseTileSetTexX0 (), le.getBaseTileSetTexY0 (), le.getBaseTileSetTexX1 (), le.getBaseTileSetTexY1 ());

		// Comprobamos que no tenga un effect de graphicchange
		boolean bGraphiChanged = false;
		for (int e = 0; e < le.getLivingEntityData ().getEffects ().size (); e++) {
			if (le.getLivingEntityData ().getEffects ().get (e).isGraphicChange ()) {
				bGraphiChanged = true;
				break;
			}
		}

		if (!bGraphiChanged) {
			// Miramos si lleva algo equipado para dibujarlo
			if (iType == LIVINGS_PANEL_TYPE_CITIZENS || iType == LIVINGS_PANEL_TYPE_SOLDIERS) {
				EquippedData equippedData = le.getEquippedData ();
				if (equippedData.isWearing (MilitaryItem.LOCATION_BODY)) {
					MilitaryItem mi = equippedData.getBody ();
					iCurrentTexture = UtilsGL.setTexture (mi, iCurrentTexture);
					UtilsGL.drawTexture (x + le.getOffset_body_x (), y + le.getOffset_body_y (), x + le.getOffset_body_x () + mi.getTileWidth (), y + le.getOffset_body_y () + mi.getTileHeight (), mi.getBaseTileSetTexX0 (), mi.getBaseTileSetTexY0 (), mi.getBaseTileSetTexX1 (), mi.getBaseTileSetTexY1 ());
				}
				if (equippedData.isWearing (MilitaryItem.LOCATION_HEAD)) {
					MilitaryItem mi = equippedData.getHead ();
					iCurrentTexture = UtilsGL.setTexture (mi, iCurrentTexture);
					UtilsGL.drawTexture (x + le.getOffset_head_x (), y + le.getOffset_head_y (), x + le.getOffset_head_x () + mi.getTileWidth (), y + le.getOffset_head_y () + mi.getTileHeight (), mi.getBaseTileSetTexX0 (), mi.getBaseTileSetTexY0 (), mi.getBaseTileSetTexX1 (), mi.getBaseTileSetTexY1 ());
				}
				if (equippedData.isWearing (MilitaryItem.LOCATION_FEET)) {
					MilitaryItem mi = equippedData.getFeet ();
					iCurrentTexture = UtilsGL.setTexture (mi, iCurrentTexture);
					UtilsGL.drawTexture (x + le.getOffset_feet_x (), y + le.getOffset_feet_y (), x + le.getOffset_feet_x () + mi.getTileWidth (), y + le.getOffset_feet_y () + mi.getTileHeight (), mi.getBaseTileSetTexX0 (), mi.getBaseTileSetTexY0 (), mi.getBaseTileSetTexX1 (), mi.getBaseTileSetTexY1 ());
				}
				if (equippedData.isWearing (MilitaryItem.LOCATION_LEGS)) {
					MilitaryItem mi = equippedData.getLegs ();
					iCurrentTexture = UtilsGL.setTexture (mi, iCurrentTexture);
					UtilsGL.drawTexture (x + le.getOffset_legs_x (), y + le.getOffset_legs_y (), x + le.getOffset_legs_x () + mi.getTileWidth (), y + le.getOffset_legs_y () + mi.getTileHeight (), mi.getBaseTileSetTexX0 (), mi.getBaseTileSetTexY0 (), mi.getBaseTileSetTexX1 (), mi.getBaseTileSetTexY1 ());
				}
				if (equippedData.isWearing (MilitaryItem.LOCATION_WEAPON)) {
					MilitaryItem mi = equippedData.getWeapon ();
					iCurrentTexture = UtilsGL.setTexture (mi, iCurrentTexture);
					UtilsGL.drawTexture (x + le.getOffset_weapon_x (), y + le.getOffset_weapon_y (), x + le.getOffset_weapon_x () + mi.getTileWidth (), y + le.getOffset_weapon_y () + mi.getTileHeight (), mi.getBaseTileSetTexX0 (), mi.getBaseTileSetTexY0 (), mi.getBaseTileSetTexX1 (), mi.getBaseTileSetTexY1 ());
				}
			}
		}

		return iCurrentTexture;
	}


	private void renderPrioritiesPanel (int mouseX, int mouseY, int mousePanel) {
		// XAVI GL11.glColor4f (1, 1, 1, 1);
		int iCurrentTexture = tilePrioritiesPanel[0].getTextureID ();
		GL11.glBindTexture (GL11.GL_TEXTURE_2D, iCurrentTexture);
		GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		UtilsGL.glBegin (GL11.GL_QUADS);
		renderBackground (tilePrioritiesPanel, prioritiesPanelPoint, PRIORITIES_PANEL_WIDTH, PRIORITIES_PANEL_HEIGHT);

		// Items
		Point point;
		Point pItem;
		if (mousePanel == MOUSE_PRIORITIES_PANEL_ITEMS || mousePanel == MOUSE_PRIORITIES_PANEL_ITEMS_UP || mousePanel == MOUSE_PRIORITIES_PANEL_ITEMS_DOWN) {
			pItem = isMouseOnPrioritiesItems (mouseX, mouseY);
		} else {
			pItem = null;
		}

		for (int i = 0; i < PRIORITIES_PANEL_NUM_ITEMS; i++) {
			point = prioritiesPanelItemsPosition.get (i);
			// Round button
			iCurrentTexture = UtilsGL.setTexture (tileBottomItem, iCurrentTexture);
			drawTile (tileBottomItem, point, PRIORITIES_PANEL_ITEM_SIZE, PRIORITIES_PANEL_ITEM_SIZE, (pItem != null && pItem.x == MOUSE_PRIORITIES_PANEL_ITEMS && pItem.y == i));

			// Icono
			if (i < (PRIORITIES_PANEL_NUM_ITEMS - 1)) {
				// Icono de UI que toque
				Tile tile = ActionPriorityManager.getItem (ActionPriorityManager.getPrioritiesList ().get (i)).getIcon ();
				iCurrentTexture = UtilsGL.setTexture (tile, iCurrentTexture);
				drawTile (tile, point, PRIORITIES_PANEL_ITEM_SIZE, PRIORITIES_PANEL_ITEM_SIZE, (pItem != null && pItem.x == MOUSE_PRIORITIES_PANEL_ITEMS && pItem.y == i));
			} else {
				// Back
				iCurrentTexture = UtilsGL.setTexture (BACK_TILE, iCurrentTexture);
				drawTile (BACK_TILE, point, PRIORITIES_PANEL_ITEM_SIZE, PRIORITIES_PANEL_ITEM_SIZE, (pItem != null && pItem.x == MOUSE_PRODUCTION_PANEL_ITEMS && pItem.y == i));
			}

			point = prioritiesPanelItemsUpPosition.get (i);
			if (point.x != -1) {
				// Up
				iCurrentTexture = UtilsGL.setTexture (tilePrioritiesPanelUpIcon, iCurrentTexture);
				drawTile (tilePrioritiesPanelUpIcon, point, ICON_WIDTH, ICON_HEIGHT, (pItem != null && pItem.x == MOUSE_PRIORITIES_PANEL_ITEMS_UP && pItem.y == i));
			}
			point = prioritiesPanelItemsDownPosition.get (i);
			if (point.x != -1) {
				// Down
				iCurrentTexture = UtilsGL.setTexture (tilePrioritiesPanelDownIcon, iCurrentTexture);
				drawTile (tilePrioritiesPanelDownIcon, point, ICON_WIDTH, ICON_HEIGHT, (pItem != null && pItem.x == MOUSE_PRIORITIES_PANEL_ITEMS_DOWN && pItem.y == i));
			}
		}
		UtilsGL.glEnd ();

		// Render priority numbers
		GL11.glBindTexture (GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
		GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		UtilsGL.glBegin (GL11.GL_QUADS);
		for (int i = 0; i < PRIORITIES_PANEL_NUM_ITEMS - 1; i++) {
			point = prioritiesPanelItemsPosition.get (i);
			UtilsGL.drawStringWithBorder (Integer.toString ((i + 1)), point.x, point.y, ColorGL.WHITE, ColorGL.BLACK);
		}
		UtilsGL.glEnd ();
	}


	/**
	 * Draws the current task
	 * 
	 * @param x
	 * @param y
	 * @param mousePanel
	 */
	private void renderTask () {
		if (Game.getCurrentState () != Game.STATE_CREATING_TASK) {
			return;
		}

		Task task = Game.getCurrentTask ();
		if (task == null) {
			return;
		}

		String taskString = task.toString ();
		int taskX = messageIconPoints[0].x;
		int taskY = messageIconPoints[0].y + messageTiles[0].getTileHeight () + PIXELS_TO_BORDER;
		int taskWidth = UtilFont.getWidth (taskString);
		int taskHeight = UtilFont.MAX_HEIGHT;

		// Render del icono de la tarea
		if (Game.getCurrentState () == Game.STATE_CREATING_TASK) {
			Tile tile = Game.getCurrentTask ().getTile ();
			if (tile != null) {
				// XAVI GL11.glColor4f (1, 1, 1, 1);

				// Round button
				GL11.glBindTexture (GL11.GL_TEXTURE_2D, tileBottomItem.getTextureID ());
				GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
				UtilsGL.glBegin (GL11.GL_QUADS);
				drawTile (tileBottomItem, taskX, taskY, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT, false);
				UtilsGL.glEnd ();

				// Icon
				GL11.glBindTexture (GL11.GL_TEXTURE_2D, tile.getTextureID ());
				GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
				UtilsGL.glBegin (GL11.GL_QUADS);
				drawTile (tile, taskX, taskY, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT, false);
				UtilsGL.glEnd ();

				taskX += (BOTTOM_ITEM_WIDTH + PIXELS_TO_BORDER);
				taskY += (BOTTOM_ITEM_HEIGHT / 4);
			}
		}

		GL11.glBindTexture (GL11.GL_TEXTURE_2D, UIPanel.BLACK_TILE.getTextureID ());
		GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		UtilsGL.glBegin (GL11.GL_QUADS);
		UtilsGL.drawTexture (taskX, taskY, taskX + taskWidth + 2, taskY + taskHeight + 2, UIPanel.BLACK_TILE.getTileSetTexX0 (), UIPanel.BLACK_TILE.getTileSetTexY0 (), UIPanel.BLACK_TILE.getTileSetTexX1 (), UIPanel.BLACK_TILE.getTileSetTexY1 ());
		UtilsGL.glEnd ();

		// Texto
		GL11.glBindTexture (GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
		GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		UtilsGL.glBegin (GL11.GL_QUADS);
		UtilsGL.drawString (taskString, taskX + 1, taskY + 1);
		UtilsGL.glEnd ();
	}


	/**
	 * Draws the tutorial button if it is enabled
	 * 
	 * @param mousePanel
	 */
	private void renderTutorialButton (int mousePanel, boolean bCheckBlink) {
		if (Game.getCurrentMissionData () == null || Game.getCurrentMissionData ().getTutorialFlows ().size () == 0) {
			return;
		}

		// Render del icono del tutorial
		// Round button
		int iCurrentTexture = tileBottomItem.getTextureID ();
		GL11.glBindTexture (GL11.GL_TEXTURE_2D, iCurrentTexture);
		GL11.glTexEnvf (GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		UtilsGL.glBegin (GL11.GL_QUADS);
		if (bCheckBlink) {
			if (imagesPanel == null || (ImagesPanel.getCurrentFlowIndex () == 0 && !ImagesPanel.isVisible ())) {
				UtilsGL.setColorRed ();
			}
		}
		drawTile (tileBottomItem, iconTutorialPoint.x, iconTutorialPoint.y, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT, (mousePanel == MOUSE_TUTORIAL_ICON));
		if (bCheckBlink) {
			if (imagesPanel == null || (ImagesPanel.getCurrentFlowIndex () == 0 && !ImagesPanel.isVisible ())) {
				UtilsGL.unsetColor ();
			}
		}

		// Icon
		iCurrentTexture = UtilsGL.setTexture (iCurrentTexture, tileIconTutorial.getTextureID ());
		drawTile (tileIconTutorial, iconTutorialPoint.x, iconTutorialPoint.y, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT, (mousePanel == MOUSE_TUTORIAL_ICON));

		UtilsGL.glEnd ();
	}


	/**
	 * Draws the tooltip
	 * 
	 * @param x
	 * @param y
	 * @param mousePanel
	 */
	private void renderTooltip (int x, int y, int mousePanel) {
		if (mousePanel == MOUSE_NONE || mousePanel == MOUSE_PRODUCTION_PANEL || mousePanel == MOUSE_PRIORITIES_PANEL) {
			return;
		}

		String tooltip = null;
		int tooltipX = 0, tooltipY = 0;

		if (typingPanel != null) {
			// TYPING PANEL

		} else {
			// TYPING PANEL NO ACTIVO

			// Bottom
			if (isBottomMenuPanelActive ()) {
				if (mousePanel == MOUSE_BOTTOM_ITEMS) {
					int iItem = isMouseOnBottomItems (x, y);
					if (iItem != -1) {
						SmartMenu item = currentMenu.getItems ().get (iItem + bottomPanelItemIndex);
						tooltip = item.getName ();
						if (item.getType () == SmartMenu.TYPE_ITEM && (iItem + bottomPanelItemIndex) >= 0 && (iItem + bottomPanelItemIndex) <= 9) {
							switch (iItem + bottomPanelItemIndex) {
								case 0:
									tooltip += UtilsKeyboard.getTooltip (UtilsKeyboard.FN_BOT_1);
									break;
								case 1:
									tooltip += UtilsKeyboard.getTooltip (UtilsKeyboard.FN_BOT_2);
									break;
								case 2:
									tooltip += UtilsKeyboard.getTooltip (UtilsKeyboard.FN_BOT_3);
									break;
								case 3:
									tooltip += UtilsKeyboard.getTooltip (UtilsKeyboard.FN_BOT_4);
									break;
								case 4:
									tooltip += UtilsKeyboard.getTooltip (UtilsKeyboard.FN_BOT_5);
									break;
								case 5:
									tooltip += UtilsKeyboard.getTooltip (UtilsKeyboard.FN_BOT_6);
									break;
								case 6:
									tooltip += UtilsKeyboard.getTooltip (UtilsKeyboard.FN_BOT_7);
									break;
								case 7:
									tooltip += UtilsKeyboard.getTooltip (UtilsKeyboard.FN_BOT_8);
									break;
								case 8:
									tooltip += UtilsKeyboard.getTooltip (UtilsKeyboard.FN_BOT_9);
									break;
								case 9:
									tooltip += UtilsKeyboard.getTooltip (UtilsKeyboard.FN_BOT_10);
									break;
							}
						}
						if (tooltip != null) {
							tooltipX = x - (UtilFont.getWidth (tooltip)) / 2;
							tooltipY = bottomPanelY - (2 * UtilFont.MAX_HEIGHT);
						}
					}
				} else if (mousePanel == MOUSE_BOTTOM_SUBITEMS) {
					int iItem = isMouseOnBottomSubItems (x, y);
					if (iItem != -1) {
						SmartMenu item = bottomSubPanelMenu.getItems ().get (iItem);
						if (item.getPrerequisites () != null && item.getPrerequisites ().size () > 0) {
							MainPanel.renderMessages (x, bottomSubPanelPoint.y - (item.getPrerequisites ().size () * (UtilFont.MAX_HEIGHT + 5)), renderWidth, renderHeight, Tile.TERRAIN_ICON_WIDTH / 2, item.getPrerequisites (), item.getPrerequisitesColor ());
						} else {
							tooltip = item.getName ();
							if (tooltip != null) {
								tooltipX = x - (UtilFont.getWidth (tooltip)) / 2;
								tooltipY = bottomSubPanelPoint.y - (2 * UtilFont.MAX_HEIGHT);
							}
						}
					}
				}
			}

			// Right menu
			if (tooltip == null && isMenuPanelActive ()) {
				if (mousePanel == MOUSE_MENU_PANEL_ITEMS) {
					int iItem = isMouseOnMenuItems (x, y);
					if (iItem != -1) {
						SmartMenu item = menuPanelMenu.getItems ().get (iItem);
						if (item.getPrerequisites () != null && item.getPrerequisites ().size () > 0) {
							MainPanel.renderMessages (menuPanelPoint.x, y, renderWidth, renderHeight, Tile.TERRAIN_ICON_WIDTH / 2, item.getPrerequisites (), item.getPrerequisitesColor ());
						} else {
							tooltip = item.getName ();
							if (tooltip != null) {
								tooltipX = menuPanelPoint.x - (UtilFont.getWidth (tooltip));
								tooltipY = y;
							}
						}
					}
				}
			}

			// Production
			if (tooltip == null && isProductionPanelActive ()) {
				if (mousePanel == MOUSE_PRODUCTION_PANEL_ITEMS || mousePanel == MOUSE_PRODUCTION_PANEL_ITEMS_MINUS_AUTOMATED || mousePanel == MOUSE_PRODUCTION_PANEL_ITEMS_PLUS_AUTOMATED) {
					Point p = isMouseOnProductionItems (x, y);
					if (p != null) {
						if (p.x == MOUSE_PRODUCTION_PANEL_ITEMS) {
							SmartMenu item = productionPanelMenu.getItems ().get (p.y);
							if (item.getPrerequisites () != null && item.getPrerequisites ().size () > 0) {
								if ((productionPanelPoint.x + PRODUCTION_PANEL_WIDTH) > renderWidth) {
									MainPanel.renderMessages (renderWidth, y, renderWidth, renderHeight, 0, item.getPrerequisites (), item.getPrerequisitesColor ());
								} else {
									MainPanel.renderMessages (productionPanelPoint.x + PRODUCTION_PANEL_WIDTH, y, renderWidth, renderHeight, 0, item.getPrerequisites (), item.getPrerequisitesColor ());
								}
							} else {
								tooltip = item.getName ();
								if (tooltip != null) {
									tooltipX = productionPanelPoint.x + PRODUCTION_PANEL_WIDTH;
									tooltipY = y;
								}
							}
						} else if (p.x == MOUSE_PRODUCTION_PANEL_ITEMS_MINUS_AUTOMATED || p.x == MOUSE_PRODUCTION_PANEL_ITEMS_PLUS_AUTOMATED) {
							SmartMenu item = productionPanelMenu.getItems ().get (p.y);
							if (item.getCommand ().equals (CommandPanel.COMMAND_QUEUE)) {
								String sParam = item.getParameter ();
								ActionManagerItem ami = ActionManager.getItem (sParam);
								if (ami != null) {
									if (ami.isInverted ()) {
										tooltip = Messages.getString ("UIPanel.68"); //$NON-NLS-1$
									} else {
										tooltip = Messages.getString ("UIPanel.72"); //$NON-NLS-1$
									}
									tooltipX = x + 32;
									tooltipY = y;
								}
							}
						}
					}
				}
			}

			// Priorities
			if (tooltip == null && isPrioritiesPanelActive ()) {
				if (mousePanel == MOUSE_PRIORITIES_PANEL_ITEMS) {
					Point p = isMouseOnPrioritiesItems (x, y);
					if (p != null && p.x == MOUSE_PRIORITIES_PANEL_ITEMS) {
						if (p.y == (PRIORITIES_PANEL_NUM_ITEMS - 1)) {
							// Back
							tooltip = Messages.getString ("UIPanel.13"); //$NON-NLS-1$
						} else {
							tooltip = ActionPriorityManager.getItem (ActionPriorityManager.getPrioritiesList ().get (p.y)).getName ();
						}
						tooltipX = prioritiesPanelPoint.x + PRIORITIES_PANEL_WIDTH;
						tooltipY = y;
					}
				}
			}

			// Trade
			if (tooltip == null && isTradePanelActive ()) {
				CaravanData caravanData = Game.getWorld ().getCurrentCaravanData ();
				boolean bTrading = (caravanData != null && caravanData.getStatus () == CaravanData.STATUS_TRADING);

				if (!bTrading && mousePanel == MOUSE_TRADE_PANEL_BUTTONS_CARAVAN) {
					Point p = isMouseOnTradeButtons (x, y);
					if (p != null && p.x == MOUSE_TRADE_PANEL_BUTTONS_CARAVAN && (p.y + tradePanel.getIndexButtonsCaravan ()) < tradePanel.getMenuCaravan ().getItems ().size ()) {
						tooltip = tradePanel.getMenuCaravan ().getItems ().get (p.y + tradePanel.getIndexButtonsCaravan ()).getName ();
						tooltipX = x + 32;
						tooltipY = y;
					}
				} else if (mousePanel == MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_CARAVAN) {
					if (caravanData != null) {
						Point p = isMouseOnTradeButtons (x, y);
						if (p != null && p.x == MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_CARAVAN && (p.y + tradePanel.getIndexButtonsToBuyCaravan ()) < caravanData.getMenuCaravanToBuy ().getItems ().size ()) {
							tooltip = caravanData.getMenuCaravanToBuy ().getItems ().get (p.y + tradePanel.getIndexButtonsToBuyCaravan ()).getName ();
							tooltipX = x + 32;
							tooltipY = y;
						}
					}
				} else if (!bTrading && mousePanel == MOUSE_TRADE_PANEL_BUTTONS_TOWN) {
					Point p = isMouseOnTradeButtons (x, y);
					if (p != null && p.x == MOUSE_TRADE_PANEL_BUTTONS_TOWN && (p.y + tradePanel.getIndexButtonsTown ()) < tradePanel.getMenuTown ().getItems ().size ()) {
						tooltip = tradePanel.getMenuTown ().getItems ().get (p.y + tradePanel.getIndexButtonsTown ()).getName ();
						tooltipX = x + 32;
						tooltipY = y;
					}
				} else if (mousePanel == MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_TOWN) {
					if (caravanData != null) {
						Point p = isMouseOnTradeButtons (x, y);
						if (p != null && p.x == MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_TOWN && (p.y + tradePanel.getIndexButtonsToSellTown ()) < caravanData.getMenuTownToSell ().getItems ().size ()) {
							tooltip = caravanData.getMenuTownToSell ().getItems ().get (p.y + tradePanel.getIndexButtonsToSellTown ()).getName ();
							tooltipX = x + 32;
							tooltipY = y;
						}
					}
				} else if (mousePanel == MOUSE_TRADE_PANEL_BUTTONS_CLOSE) {
					tooltip = Messages.getString ("UIPanel.19"); //$NON-NLS-1$
					tooltipX = x + 32;
					tooltipY = y;
				} else if (mousePanel == MOUSE_TRADE_PANEL_ICON_BUY) {
					tooltip = Messages.getString ("UIPanel.33"); //$NON-NLS-1$
					tooltipX = x + 32;
					tooltipY = y;
				} else if (mousePanel == MOUSE_TRADE_PANEL_ICON_SELL) {
					tooltip = Messages.getString ("UIPanel.35"); //$NON-NLS-1$
					tooltipX = x + 32;
					tooltipY = y;
				} else if (!bTrading && mousePanel == MOUSE_TRADE_PANEL_BUTTONS_CONFIRM) {
					tooltip = Messages.getString ("UIPanel.36"); //$NON-NLS-1$
					tooltipX = x + 32;
					tooltipY = y;
				}
			}

			// Professions
			if (tooltip == null && isProfessionsPanelActive ()) {
				if (mousePanel == MOUSE_PROFESSIONS_PANEL_BUTTONS_ITEMS) {
					Point p = isMouseOnProfessionsButtons (x, y);
					if (p != null && p.y > -1 && p.y < menuProfessions.getItems ().size ()) {
						String sName = menuProfessions.getItems ().get (p.y).getName ();
						if (sName != null) {
							tooltip = sName;
							tooltipX = x;
							tooltipY = y + UtilFont.MAX_HEIGHT * 2;
						}
					}
				} else if (mousePanel == MOUSE_PROFESSIONS_PANEL_BUTTONS_CLOSE) {
					tooltip = Messages.getString ("UIPanel.19"); //$NON-NLS-1$
					tooltipX = x + 32;
					tooltipY = y;
				}
			}

			// Pile
			if (tooltip == null && isPilePanelActive ()) {
				if (mousePanel == MOUSE_PILE_PANEL_BUTTONS_ITEMS) {
					Point p = isMouseOnPileButtons (x, y);
					if (p != null && p.y > -1 && p.y < menuPile.getItems ().size ()) {
						String sName = menuPile.getItems ().get (p.y).getName ();
						if (sName != null) {
							tooltip = sName;
							tooltipX = x;
							tooltipY = y + UtilFont.MAX_HEIGHT * 2;
						}
					}
				} else if (mousePanel == MOUSE_PILE_PANEL_BUTTONS_CLOSE) {
					tooltip = Messages.getString ("UIPanel.19"); //$NON-NLS-1$
					tooltipX = x + 32;
					tooltipY = y;
				} else if (mousePanel == MOUSE_PILE_PANEL_BUTTONS_CONFIG_COPY) {
					if (pilePanelIsContainer) {
						tooltip = Messages.getString("UIPanel.80"); //$NON-NLS-1$
					} else {
						tooltip = Messages.getString("UIPanel.82"); //$NON-NLS-1$
					}
					tooltipX = x + 32;
					tooltipY = y;
				} else if (mousePanel == MOUSE_PILE_PANEL_BUTTONS_CONFIG_LOCK) {
					if (pilePanelIsLocked) {
						tooltip = Messages.getString("UIPanel.86"); //$NON-NLS-1$
					} else {
						tooltip = Messages.getString("UIPanel.85"); //$NON-NLS-1$
					}
					tooltipX = x + 32;
					tooltipY = y;
				} else if (mousePanel == MOUSE_PILE_PANEL_BUTTONS_CONFIG_LOCK_ALL) {
					tooltip = Messages.getString("UIPanel.87"); //$NON-NLS-1$
					tooltipX = x + 32;
					tooltipY = y;
				} else if (mousePanel == MOUSE_PILE_PANEL_BUTTONS_CONFIG_UNLOCK_ALL) {
					tooltip = Messages.getString("UIPanel.88"); //$NON-NLS-1$
					tooltipX = x + 32;
					tooltipY = y;
				}
			}

			// Mats
			if (tooltip == null && isMatsPanelActive ()) {
				if (mousePanel == MOUSE_MATS_PANEL_BUTTONS_GROUPS) {
					Point p = isMouseOnMatsButtons (x, y);
					if (p != null && p.y > -1 && p.y < MatsPanelData.nameGroups.size ()) {
						tooltip = MatsPanelData.nameGroups.get (p.y);
						tooltipX = x + 32;
						tooltipY = y;
					}
				} else if (mousePanel == MOUSE_MATS_PANEL_BUTTONS_ITEMS) {
					Point p = isMouseOnMatsButtons (x, y);
					if (p != null && p.y > -1 && p.y < MatsPanelData.tileGroups.get (getMatsPanelActive ()).size ()) {
						String sIniHeader = MatsPanelData.tileGroups.get (getMatsPanelActive ()).get (p.y).getIniHeader ();
						if (sIniHeader != null) {
							ItemManagerItem imi = ItemManager.getItem (sIniHeader);
							if (imi != null && imi.getName () != null) {
								tooltip = imi.getName ();
								tooltipX = x + 32;
								tooltipY = y;
							}
						}
					}
				} else if (mousePanel == MOUSE_MATS_PANEL_BUTTONS_CLOSE) {
					tooltip = Messages.getString ("UIPanel.19"); //$NON-NLS-1$
					tooltipX = x + 32;
					tooltipY = y;
				}
			}

			// Livings
			if (tooltip == null && isLivingsPanelActive ()) {
				if (mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_CLOSE) {
					tooltip = Messages.getString ("UIPanel.19"); //$NON-NLS-1$
					tooltipX = x + 32;
					tooltipY = y;
				} else if ((mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_UP || mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_DOWN) && livingsPanelCitizensGroupActive == -1 && getLivingsPanelActive () == LIVINGS_PANEL_TYPE_CITIZENS) {
					tooltip = Messages.getString ("UIPanel.73"); //$NON-NLS-1$
					tooltipX = x - (UtilFont.getWidth (tooltip) / 2);
					tooltipY = livingsPanelIconRestrictUpPoint.y + tileIconLevelUp.getTileHeight ();
				} else if ((mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_UP || mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_DOWN) && getLivingsPanelActive () == LIVINGS_PANEL_TYPE_HEROES) {
					tooltip = Messages.getString ("UIPanel.74"); //$NON-NLS-1$
					tooltipX = x - (UtilFont.getWidth (tooltip) / 2);
					tooltipY = livingsPanelIconRestrictUpPoint.y + tileIconLevelUp.getTileHeight ();
				} else if (mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_PROFESSIONS && livingsPanelCitizensGroupActive == -1) {
					tooltip = Messages.getString ("UIPanel.63"); //$NON-NLS-1$
					tooltipX = x + 32;
					tooltipY = y;
				} else if (mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_JOBS_GROUPS_ADDREMOVE) {
					tooltip = Messages.getString ("UIPanel.65"); //$NON-NLS-1$
					tooltipX = x + 32;
					tooltipY = y;
				} else if (mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER) {
					tooltip = Messages.getString ("Citizen.27"); //$NON-NLS-1$
					tooltipX = x + 32;
					tooltipY = y;
				} else if (mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_CIVILIAN) {
					tooltip = Messages.getString ("Citizen.26"); //$NON-NLS-1$
					tooltipX = x + 32;
					tooltipY = y;
				} else if (mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_GUARD) {
					tooltip = Messages.getString ("Citizen.32"); //$NON-NLS-1$
					tooltipX = x + 32;
					tooltipY = y;
				} else if (mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_PATROL) {
					tooltip = Messages.getString ("Citizen.34"); //$NON-NLS-1$
					tooltipX = x + 32;
					tooltipY = y;
				} else if (mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_BOSS) {
					tooltip = Messages.getString ("Citizen.35"); //$NON-NLS-1$
					tooltipX = x + 32;
					tooltipY = y;
				} else if (mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_AUTOEQUIP) {
					tooltip = Messages.getString ("UIPanel.43"); //$NON-NLS-1$
					tooltipX = x + 32;
					tooltipY = y;
				} else if (mousePanel == MOUSE_LIVINGS_PANEL_CGROUP_NOGROUP) {
					tooltip = Messages.getString ("UIPanel.66") + " (" + Game.getWorld ().getCitizenGroups ().getCitizensWithoutGroup ().size () + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					tooltipX = x - UtilFont.getWidth (tooltip) / 2;
					tooltipY = y - UtilFont.MAX_HEIGHT - 2;
				} else if (mousePanel == MOUSE_LIVINGS_PANEL_CGROUP_GROUP) {
					Point p = isMouseOnLivingsButtons (x, y);
					if (p != null && p.y >= 0 && p.y < CitizenGroups.MAX_GROUPS) {
						CitizenGroupData cgd = Game.getWorld ().getCitizenGroups ().getGroup (p.y);
						if (cgd != null) {
							tooltip = cgd.getName () + " (" + cgd.getLivingIDs ().size () + ")"; //$NON-NLS-1$ //$NON-NLS-2$
							tooltipX = x - UtilFont.getWidth (tooltip) / 2;
							tooltipY = y - UtilFont.MAX_HEIGHT - 2;
						}
					}
				} else if (mousePanel == MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_RENAME) {
					tooltip = Messages.getString ("UIPanel.54"); //$NON-NLS-1$
					tooltipX = x + 32;
					tooltipY = y;
				} else if (mousePanel == MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_AUTOEQUIP) {
					tooltip = Messages.getString ("UIPanel.59"); //$NON-NLS-1$
					tooltipX = x + 32;
					tooltipY = y;
				} else if (mousePanel == MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_DISBAND) {
					tooltip = Messages.getString ("UIPanel.60"); //$NON-NLS-1$
					tooltipX = x + 32;
					tooltipY = y;
				} else if (mousePanel == MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_CHANGE_JOBS) {
					tooltip = Messages.getString ("UIPanel.69"); //$NON-NLS-1$
					tooltipX = x + 32;
					tooltipY = y;
				} else if (mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_SGROUP_ADD) {
					tooltip = Messages.getString ("UIPanel.47"); //$NON-NLS-1$
					tooltipX = x + 32;
					tooltipY = y;
				} else if (mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_SGROUP_REMOVE) {
					tooltip = Messages.getString ("UIPanel.51"); //$NON-NLS-1$
					tooltipX = x + 32;
					tooltipY = y;
				} else if (mousePanel == MOUSE_LIVINGS_PANEL_SGROUP_NOGROUP) {
					tooltip = Messages.getString ("UIPanel.53") + " (" + Game.getWorld ().getSoldierGroups ().getSoldiersWithoutGroup ().size () + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					tooltipX = x - UtilFont.getWidth (tooltip) / 2;
					tooltipY = y - UtilFont.MAX_HEIGHT - 2;
				} else if (mousePanel == MOUSE_LIVINGS_PANEL_SGROUP_GROUP) {
					Point p = isMouseOnLivingsButtons (x, y);
					if (p != null && p.y >= 0 && p.y < SoldierGroups.MAX_GROUPS) {
						SoldierGroupData sgd = Game.getWorld ().getSoldierGroups ().getGroup (p.y);
						if (sgd != null) {
							tooltip = sgd.getName () + " (" + sgd.getLivingIDs ().size () + ")"; //$NON-NLS-1$ //$NON-NLS-2$
							tooltipX = x - UtilFont.getWidth (tooltip) / 2;
							tooltipY = y - UtilFont.MAX_HEIGHT - 2;
						}
					}
				} else if (mousePanel == MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_RENAME) {
					tooltip = Messages.getString ("UIPanel.54"); //$NON-NLS-1$
					tooltipX = x + 32;
					tooltipY = y;
				} else if (mousePanel == MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_GUARD) {
					tooltip = Messages.getString ("UIPanel.55"); //$NON-NLS-1$
					tooltipX = x + 32;
					tooltipY = y;
				} else if (mousePanel == MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_PATROL) {
					tooltip = Messages.getString ("UIPanel.57"); //$NON-NLS-1$
					tooltipX = x + 32;
					tooltipY = y;
				} else if (mousePanel == MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_BOSS) {
					tooltip = Messages.getString ("UIPanel.58"); //$NON-NLS-1$
					tooltipX = x + 32;
					tooltipY = y;
				} else if (mousePanel == MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_AUTOEQUIP) {
					tooltip = Messages.getString ("UIPanel.59"); //$NON-NLS-1$
					tooltipX = x + 32;
					tooltipY = y;
				} else if (mousePanel == MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_DISBAND) {
					tooltip = Messages.getString ("UIPanel.60"); //$NON-NLS-1$
					tooltipX = x + 32;
					tooltipY = y;
				} else if (mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS || mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_HEAD || mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_BODY || mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_LEGS || mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_FEET || mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_WEAPON) {
					Point p = isMouseOnLivingsButtons (x, y);
					int iIndex = getLivingsIndex ();
					ArrayList<Integer> alLivings = getLivings ();
					if (alLivings != null && p != null && (p.y + iIndex) >= 0 && (p.y + iIndex) < alLivings.size ()) {
						LivingEntity le = World.getLivingEntityByID (alLivings.get ((p.y + iIndex)));
						if (le != null) {
							if (mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS) {
								if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_CITIZENS || getLivingsPanelActive () == LIVINGS_PANEL_TYPE_SOLDIERS) {
									Citizen citizen = (Citizen) le;
									int iNumEffects = le.getLivingEntityData ().getEffects ().size ();
									ArrayList<String> alMessages = new ArrayList<String> (6 + iNumEffects);
									ArrayList<ColorGL> alColors = new ArrayList<ColorGL> (6 + iNumEffects);

									alMessages.add (citizen.getCitizenData ().getFullName ());
									alColors.add (ColorGL.YELLOW);
									alMessages.add (citizen.getLivingEntityData ().toString ());
									alColors.add (ColorGL.WHITE);
									if (citizen.getCurrentTask () != null) {
										alMessages.add (Messages.getString ("Citizen.7") + citizen.getCurrentTask ()); //$NON-NLS-1$
										alColors.add (ColorGL.WHITE);
									}

									// Level / Xp
									if (citizen.getSoldierData ().isSoldier ()) {
										alMessages.add (Messages.getString ("Hero.4") + citizen.getSoldierData ().getLevel () + " (" + citizen.getSoldierData ().getXp () + Messages.getString ("Hero.5") + citizen.getSoldierData ().getXpPCT () + "%)"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
										alColors.add (ColorGL.WHITE);
									}

									alMessages.add (Messages.getString ("UIPanel.40") + citizen.getCitizenData ().getHappiness () + " / 100"); //$NON-NLS-1$ //$NON-NLS-2$
									alColors.add (ColorGL.WHITE);
									alMessages.add (Messages.getString ("UIPanel.49") + citizen.getCitizenData ().getHungry () + " / " + citizen.getCitizenData ().getMaxHungry ()); //$NON-NLS-1$ //$NON-NLS-2$
									alColors.add (ColorGL.WHITE);
									alMessages.add (Messages.getString ("UIPanel.52") + citizen.getCitizenData ().getSleep () + " / " + citizen.getCitizenData ().getMaxSleep ()); //$NON-NLS-1$ //$NON-NLS-2$
									alColors.add (ColorGL.WHITE);

									// Effects
									EffectData eData;
									for (int e = 0; e < iNumEffects; e++) {
										eData = le.getLivingEntityData ().getEffects ().get (e);
										alMessages.add (EffectManager.getItem (eData.getEffectID ()).getName ());
										alColors.add (ColorGL.ORANGE);
									}

									MainPanel.renderMessages (x + 32, y, MainPanel.renderWidth, MainPanel.renderHeight, 2, alMessages, alColors);
									return;
								} else if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_HEROES) {
									Hero hero = (Hero) le;
									int iNumEffects = le.getLivingEntityData ().getEffects ().size ();
									ArrayList<String> alMessages = new ArrayList<String> (3 + iNumEffects);
									ArrayList<ColorGL> alColors = new ArrayList<ColorGL> (3 + iNumEffects);

									alMessages.add (hero.getCitizenData ().getFullName ());
									alMessages.add (hero.getLivingEntityData ().toString ());
									alMessages.add (Messages.getString ("Hero.4") + hero.getHeroData ().getLevel () + " (" + hero.getHeroData ().getXp () + Messages.getString ("Hero.5") + hero.getHeroData ().getXpPCT () + "%)"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
									alColors.add (ColorGL.YELLOW);
									alColors.add (ColorGL.WHITE);
									alColors.add (ColorGL.ORANGE);

									// Friendship
									String sHeroFriends = HeroData.getFriendshipString (hero);
									if (sHeroFriends != null) {
										alMessages.add (sHeroFriends);
										alColors.add (ColorGL.WHITE);
									}

									// Effects
									EffectData eData;
									for (int e = 0; e < iNumEffects; e++) {
										eData = le.getLivingEntityData ().getEffects ().get (e);
										alMessages.add (EffectManager.getItem (eData.getEffectID ()).getName ());
										alColors.add (ColorGL.ORANGE);
									}

									MainPanel.renderMessages (x + 32, y, MainPanel.renderWidth, MainPanel.renderHeight, 2, alMessages, alColors);
									return;
								}
							} else if (mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_HEAD) {
								// Head
								EquippedData equippedData = le.getEquippedData ();
								if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_CITIZENS || getLivingsPanelActive () == LIVINGS_PANEL_TYPE_SOLDIERS) {
									if (equippedData.isWearing (MilitaryItem.LOCATION_HEAD)) {
										tooltip = Messages.getString ("UIPanel.41") + equippedData.getHead ().getExtendedTilename (); //$NON-NLS-1$
									} else {
										tooltip = Messages.getString ("UIPanel.42"); //$NON-NLS-1$
									}
								} else {
									if (equippedData.isWearing (MilitaryItem.LOCATION_HEAD)) {
										tooltip = Messages.getString ("Citizen.21") + equippedData.getHead ().getExtendedTilename (); //$NON-NLS-1$
									} else {
										tooltip = Messages.getString ("Citizen.14"); //$NON-NLS-1$
									}
								}
								tooltipX = x + 32;
								tooltipY = y;
							} else if (mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_BODY) {
								// Body
								EquippedData equippedData = le.getEquippedData ();
								if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_CITIZENS || getLivingsPanelActive () == LIVINGS_PANEL_TYPE_SOLDIERS) {
									if (equippedData.isWearing (MilitaryItem.LOCATION_BODY)) {
										tooltip = Messages.getString ("UIPanel.41") + equippedData.getBody ().getExtendedTilename (); //$NON-NLS-1$
									} else {
										tooltip = Messages.getString ("UIPanel.44"); //$NON-NLS-1$
									}
								} else {
									if (equippedData.isWearing (MilitaryItem.LOCATION_BODY)) {
										tooltip = Messages.getString ("Citizen.22") + equippedData.getBody ().getExtendedTilename (); //$NON-NLS-1$
									} else {
										tooltip = Messages.getString ("Citizen.15"); //$NON-NLS-1$
									}
								}
								tooltipX = x + 32;
								tooltipY = y;
							} else if (mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_LEGS) {
								// Legs
								EquippedData equippedData = le.getEquippedData ();
								if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_CITIZENS || getLivingsPanelActive () == LIVINGS_PANEL_TYPE_SOLDIERS) {
									if (equippedData.isWearing (MilitaryItem.LOCATION_LEGS)) {
										tooltip = Messages.getString ("UIPanel.41") + equippedData.getLegs ().getExtendedTilename (); //$NON-NLS-1$
									} else {
										tooltip = Messages.getString ("UIPanel.46"); //$NON-NLS-1$
									}
								} else {
									if (equippedData.isWearing (MilitaryItem.LOCATION_LEGS)) {
										tooltip = Messages.getString ("Citizen.23") + equippedData.getLegs ().getExtendedTilename (); //$NON-NLS-1$
									} else {
										tooltip = Messages.getString ("Citizen.16"); //$NON-NLS-1$
									}
								}
								tooltipX = x + 32;
								tooltipY = y;
							} else if (mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_FEET) {
								// Feet
								EquippedData equippedData = le.getEquippedData ();
								if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_CITIZENS || getLivingsPanelActive () == LIVINGS_PANEL_TYPE_SOLDIERS) {
									if (equippedData.isWearing (MilitaryItem.LOCATION_FEET)) {
										tooltip = Messages.getString ("UIPanel.41") + equippedData.getFeet ().getExtendedTilename (); //$NON-NLS-1$
									} else {
										tooltip = Messages.getString ("UIPanel.48"); //$NON-NLS-1$
									}
								} else {
									if (equippedData.isWearing (MilitaryItem.LOCATION_FEET)) {
										tooltip = Messages.getString ("Citizen.24") + equippedData.getFeet ().getExtendedTilename (); //$NON-NLS-1$
									} else {
										tooltip = Messages.getString ("Citizen.17"); //$NON-NLS-1$
									}
								}
								tooltipX = x + 32;
								tooltipY = y;
							} else if (mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_WEAPON) {
								// Weapon
								EquippedData equippedData = le.getEquippedData ();
								if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_CITIZENS || getLivingsPanelActive () == LIVINGS_PANEL_TYPE_SOLDIERS) {
									if (equippedData.isWearing (MilitaryItem.LOCATION_WEAPON)) {
										tooltip = Messages.getString ("UIPanel.41") + equippedData.getWeapon ().getExtendedTilename (); //$NON-NLS-1$
									} else {
										tooltip = Messages.getString ("UIPanel.50"); //$NON-NLS-1$
									}
								} else {
									if (equippedData.isWearing (MilitaryItem.LOCATION_WEAPON)) {
										tooltip = Messages.getString ("Citizen.25") + equippedData.getWeapon ().getExtendedTilename (); //$NON-NLS-1$
									} else {
										tooltip = Messages.getString ("Citizen.18"); //$NON-NLS-1$
									}
								}
								tooltipX = x + 32;
								tooltipY = y;
							}
						}
					}
				}
			}

			if (tooltip == null) {
				if (mousePanel == MOUSE_DATEPANEL) {
					tooltip = Messages.getString ("UIPanel.29"); //$NON-NLS-1$
					tooltipX = datePanelPoint.x + tileDatePanel.getTileWidth () / 2 - (UtilFont.getWidth (tooltip) / 2);
					tooltipY = datePanelPoint.y + tileDatePanel.getTileHeight ();
				} else if (mousePanel == MOUSE_ICON_LEVEL_UP) {
					tooltip = Messages.getString ("UIPanel.0") + UtilsKeyboard.getTooltip (UtilsKeyboard.FN_LEVEL_UP); //$NON-NLS-1$
					tooltipX = iconLevelUpPoint.x + tileIconLevelUp.getTileWidth () / 2 - (UtilFont.getWidth (tooltip) / 2);
					tooltipY = iconLevelUpPoint.y - UtilFont.MAX_HEIGHT;
				} else if (mousePanel == MOUSE_ICON_LEVEL_DOWN) {
					tooltip = Messages.getString ("UIPanel.2") + UtilsKeyboard.getTooltip (UtilsKeyboard.FN_LEVEL_DOWN); //$NON-NLS-1$
					tooltipX = iconLevelDownPoint.x + tileIconLevelDown.getTileWidth () / 2 - (UtilFont.getWidth (tooltip) / 2);
					tooltipY = iconLevelDownPoint.y + UtilFont.MAX_HEIGHT * 2;
				} else if (mousePanel == MOUSE_ICON_LEVEL) {
					tooltip = Messages.getString ("UIPanel.30"); //$NON-NLS-1$
					tooltipX = iconLevelPoint.x + tileIconLevel.getTileWidth () / 2 - (UtilFont.getWidth (tooltip) / 2);
					tooltipY = iconLevelPoint.y + UtilFont.MAX_HEIGHT * 2;
				} else if (mousePanel == MOUSE_ICON_CITIZEN_PREVIOUS) {
					tooltip = Messages.getString ("UIPanel.3") + UtilsKeyboard.getTooltip (UtilsKeyboard.FN_PREVIOUS_CITIZEN); //$NON-NLS-1$
					tooltipX = iconCitizenPreviousPoint.x + tileIconCitizenPrevious.getTileWidth () / 2 - (UtilFont.getWidth (tooltip) / 2);
					tooltipY = iconCitizenPreviousPoint.y + tileBottomItem.getTileHeight ();
				} else if (mousePanel == MOUSE_ICON_CITIZEN_NEXT) {
					tooltip = Messages.getString ("UIPanel.4") + UtilsKeyboard.getTooltip (UtilsKeyboard.FN_NEXT_CITIZEN); //$NON-NLS-1$
					tooltipX = iconCitizenNextPoint.x + tileIconCitizenNext.getTileWidth () / 2 - (UtilFont.getWidth (tooltip) / 2);
					tooltipY = iconCitizenNextPoint.y + tileBottomItem.getTileHeight ();
				} else if (mousePanel == MOUSE_ICON_SOLDIER_PREVIOUS) {
					tooltip = Messages.getString ("UIPanel.5") + UtilsKeyboard.getTooltip (UtilsKeyboard.FN_PREVIOUS_SOLDIER); //$NON-NLS-1$
					tooltipX = iconSoldierPreviousPoint.x + tileIconSoldierPrevious.getTileWidth () / 2 - (UtilFont.getWidth (tooltip) / 2);
					tooltipY = iconSoldierPreviousPoint.y + tileBottomItem.getTileHeight ();
				} else if (mousePanel == MOUSE_ICON_SOLDIER_NEXT) {
					tooltip = Messages.getString ("UIPanel.6") + UtilsKeyboard.getTooltip (UtilsKeyboard.FN_NEXT_SOLDIER); //$NON-NLS-1$
					tooltipX = iconSoldierNextPoint.x + tileIconSoldierNext.getTileWidth () / 2 - (UtilFont.getWidth (tooltip) / 2);
					tooltipY = iconSoldierNextPoint.y + tileBottomItem.getTileHeight ();
				} else if (mousePanel == MOUSE_ICON_HERO_PREVIOUS) {
					tooltip = Messages.getString ("UIPanel.22") + UtilsKeyboard.getTooltip (UtilsKeyboard.FN_PREVIOUS_HERO); //$NON-NLS-1$
					tooltipX = iconHeroPreviousPoint.x + tileIconHeroPrevious.getTileWidth () / 2 - (UtilFont.getWidth (tooltip) / 2);
					tooltipY = iconHeroPreviousPoint.y + tileBottomItem.getTileHeight ();
				} else if (mousePanel == MOUSE_ICON_HERO_NEXT) {
					tooltip = Messages.getString ("UIPanel.23") + UtilsKeyboard.getTooltip (UtilsKeyboard.FN_NEXT_HERO); //$NON-NLS-1$
					tooltipX = iconHeroNextPoint.x + tileIconHeroNext.getTileWidth () / 2 - (UtilFont.getWidth (tooltip) / 2);
					tooltipY = iconHeroNextPoint.y + tileBottomItem.getTileHeight ();
				} else if (mousePanel == MOUSE_INFO_NUM_CITIZENS) {
					int happinessMin = (World.getCitizenIDs ().size () + World.getSoldierIDs ().size ()) * 2;
					if (happinessMin < 20) {
						happinessMin = 20;
					} else if (happinessMin > 80) {
						happinessMin = 80;
					}
					tooltip = Messages.getString ("UIPanel.8") + " (" + Messages.getString ("UIPanel.76") + ": " + World.getHappinessAverage () + " " + Messages.getString ("UIPanel.81") + ": " + happinessMin + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
					tooltipX = iconNumCitizensBackgroundPoint.x + tileBottomItem.getTileWidth () / 2 - (UtilFont.getWidth (tooltip) / 2);
					tooltipY = iconNumCitizensBackgroundPoint.y + tileBottomItem.getTileHeight ();
				} else if (mousePanel == MOUSE_INFO_NUM_SOLDIERS) {
					tooltip = Messages.getString ("UIPanel.9"); //$NON-NLS-1$
					tooltipX = iconNumSoldiersBackgroundPoint.x + tileBottomItem.getTileWidth () / 2 - (UtilFont.getWidth (tooltip) / 2);
					tooltipY = iconNumSoldiersBackgroundPoint.y + tileBottomItem.getTileHeight ();
				} else if (mousePanel == MOUSE_INFO_NUM_HEROES) {
					tooltip = Messages.getString ("UIPanel.24"); //$NON-NLS-1$
					tooltipX = iconNumHeroesBackgroundPoint.x + tileBottomItem.getTileWidth () / 2 - (UtilFont.getWidth (tooltip) / 2);
					tooltipY = iconNumHeroesBackgroundPoint.y + tileBottomItem.getTileHeight ();
				} else if (mousePanel == MOUSE_INFO_CARAVAN) {
					tooltip = Messages.getString ("UIPanel.25") + UtilsKeyboard.getTooltip (UtilsKeyboard.FN_SHOW_TRADE); //$NON-NLS-1$
					tooltipX = iconCaravanBackgroundPoint.x + tileBottomItem.getTileWidth () / 2 - (UtilFont.getWidth (tooltip) / 2);
					tooltipY = iconCaravanBackgroundPoint.y + tileBottomItem.getTileHeight ();
				} else if (mousePanel == MOUSE_ICON_PRIORITIES) {
					tooltip = Messages.getString ("UIPanel.14") + UtilsKeyboard.getTooltip (UtilsKeyboard.FN_SHOW_PRIORITIES); //$NON-NLS-1$
					tooltipX = iconPrioritiesPoint.x + tileIconPriorities.getTileWidth () / 2 - (UtilFont.getWidth (tooltip) / 2);
					tooltipY = iconPrioritiesPoint.y + UtilFont.MAX_HEIGHT * 2;
				} else if (mousePanel == MOUSE_ICON_MATS) {
					tooltip = Messages.getString ("UIPanel.32") + UtilsKeyboard.getTooltip (UtilsKeyboard.FN_SHOW_STOCK); //$NON-NLS-1$
					tooltipX = iconMatsPoint.x + tileIconMats.getTileWidth () / 2 - (UtilFont.getWidth (tooltip) / 2);
					tooltipY = iconMatsPoint.y + UtilFont.MAX_HEIGHT * 2;
				} else if (mousePanel == MOUSE_ICON_GRID) {
					tooltip = Messages.getString ("UIPanel.12") + UtilsKeyboard.getTooltip (UtilsKeyboard.FN_TOGGLE_GRID); //$NON-NLS-1$
					tooltipX = iconGridPoint.x + tileIconGrid.getTileWidth () / 2 - (UtilFont.getWidth (tooltip) / 2);
					tooltipY = iconGridPoint.y + UtilFont.MAX_HEIGHT * 2;
				} else if (mousePanel == MOUSE_ICON_MINIBLOCKS) {
					tooltip = Messages.getString ("UIPanel.16") + UtilsKeyboard.getTooltip (UtilsKeyboard.FN_TOGGLE_MINIBLOCKS); //$NON-NLS-1$
					tooltipX = iconMiniblocksPoint.x + tileIconMiniblocks.getTileWidth () / 2 - (UtilFont.getWidth (tooltip) / 2);
					tooltipY = iconMiniblocksPoint.y + UtilFont.MAX_HEIGHT * 2;
				} else if (mousePanel == MOUSE_ICON_FLATMOUSE) {
					tooltip = Messages.getString ("UIPanel.45") + UtilsKeyboard.getTooltip (UtilsKeyboard.FN_TOGGLE_FLAT_MOUSE); //$NON-NLS-1$
					tooltipX = iconFlatMousePoint.x + tileIconFlatMouse.getTileWidth () / 2 - (UtilFont.getWidth (tooltip) / 2);
					tooltipY = iconFlatMousePoint.y + UtilFont.MAX_HEIGHT * 2;
				} else if (mousePanel == MOUSE_ICON_3DMOUSE) {
					tooltip = Messages.getString ("UtilsKeyboard.16") + UtilsKeyboard.getTooltip (UtilsKeyboard.FN_TOGGLE_3D_MOUSE); //$NON-NLS-1$
					tooltipX = icon3DMousePoint.x + tileIcon3DMouse.getTileWidth () / 2 - (UtilFont.getWidth (tooltip) / 2);
					tooltipY = icon3DMousePoint.y + UtilFont.MAX_HEIGHT * 2;
				} else if (mousePanel == MOUSE_ICON_PAUSE_RESUME) {
					tooltip = Messages.getString ("UIPanel.10") + UtilsKeyboard.getTooltip (UtilsKeyboard.FN_PAUSE); //$NON-NLS-1$
					tooltipX = iconPauseResumePoint.x + tileIconPause.getTileWidth () / 2 - (UtilFont.getWidth (tooltip) / 2);
					tooltipY = iconPauseResumePoint.y + UtilFont.MAX_HEIGHT * 2;
				} else if (mousePanel == MOUSE_ICON_SETTINGS) {
					tooltip = Messages.getString ("UIPanel.11"); //$NON-NLS-1$
					tooltipX = iconSettingsPoint.x + tileIconSettings.getTileWidth () / 2 - (UtilFont.getWidth (tooltip) / 2);
					tooltipY = iconSettingsPoint.y + UtilFont.MAX_HEIGHT * 2;
				} else if (mousePanel == MOUSE_ICON_LOWER_SPEED) {
					tooltip = Messages.getString ("UIPanel.1") + UtilsKeyboard.getTooltip (UtilsKeyboard.FN_SPEED_DOWN); //$NON-NLS-1$
					tooltipX = iconLowerSpeedPoint.x + tileIconLowerSpeed.getTileWidth () / 2 - (UtilFont.getWidth (tooltip) / 2);
					tooltipY = iconLowerSpeedPoint.y + UtilFont.MAX_HEIGHT * 2;
				} else if (mousePanel == MOUSE_ICON_INCREASE_SPEED) {
					tooltip = Messages.getString ("UIPanel.15") + UtilsKeyboard.getTooltip (UtilsKeyboard.FN_SPEED_UP); //$NON-NLS-1$
					tooltipX = iconIncreaseSpeedPoint.x + tileIconIncreaseSpeed.getTileWidth () / 2 - (UtilFont.getWidth (tooltip) / 2);
					tooltipY = iconIncreaseSpeedPoint.y + UtilFont.MAX_HEIGHT * 2;
				} else if (mousePanel == MOUSE_TUTORIAL_ICON) {
					tooltip = Messages.getString ("UIPanel.75") + UtilsKeyboard.getTooltip (UtilsKeyboard.FN_SHOW_MISSION); //$NON-NLS-1$
					tooltipX = x + 32;
					tooltipY = y;
				} else if (mousePanel == MOUSE_MESSAGES_ICON_ANNOUNCEMENT) {
					ArrayList<String> alMessages = new ArrayList<String> (4);
					ArrayList<ColorGL> alColors = new ArrayList<ColorGL> (4);
					alMessages.add (Messages.getString ("UIPanel.26")); //$NON-NLS-1$
					alColors.add (ColorGL.WHITE);

					tooltip = MessagesPanel.getLastestMessage (MessagesPanel.TYPE_ANNOUNCEMENT, 2);
					if (tooltip != null) {
						alMessages.add (tooltip);
						alColors.add (MessagesPanel.getLastestMessageColor (MessagesPanel.TYPE_ANNOUNCEMENT, 2));
					}
					tooltip = MessagesPanel.getLastestMessage (MessagesPanel.TYPE_ANNOUNCEMENT, 1);
					if (tooltip != null) {
						alMessages.add (tooltip);
						alColors.add (MessagesPanel.getLastestMessageColor (MessagesPanel.TYPE_ANNOUNCEMENT, 1));
					}
					tooltip = MessagesPanel.getLastestMessage (MessagesPanel.TYPE_ANNOUNCEMENT, 0);
					if (tooltip != null) {
						alMessages.add (tooltip);
						alColors.add (MessagesPanel.getLastestMessageColor (MessagesPanel.TYPE_ANNOUNCEMENT, 0));
					}

					tooltipX = messageIconPoints[0].x;
					tooltipY = messageIconPoints[0].y + UtilFont.MAX_HEIGHT * 2;
					MainPanel.renderMessages (tooltipX, tooltipY, MainPanel.renderWidth, MainPanel.renderHeight, 2, alMessages, alColors);
					return;
				} else if (mousePanel == MOUSE_MESSAGES_ICON_COMBAT) {
					ArrayList<String> alMessages = new ArrayList<String> (4);
					ArrayList<ColorGL> alColors = new ArrayList<ColorGL> (4);
					alMessages.add (Messages.getString ("UIPanel.27")); //$NON-NLS-1$
					alColors.add (ColorGL.WHITE);

					tooltip = MessagesPanel.getLastestMessage (MessagesPanel.TYPE_COMBAT, 2);
					if (tooltip != null) {
						alMessages.add (tooltip);
						alColors.add (MessagesPanel.getLastestMessageColor (MessagesPanel.TYPE_COMBAT, 2));
					}
					tooltip = MessagesPanel.getLastestMessage (MessagesPanel.TYPE_COMBAT, 1);
					if (tooltip != null) {
						alMessages.add (tooltip);
						alColors.add (MessagesPanel.getLastestMessageColor (MessagesPanel.TYPE_COMBAT, 1));
					}
					tooltip = MessagesPanel.getLastestMessage (MessagesPanel.TYPE_COMBAT, 0);
					if (tooltip != null) {
						alMessages.add (tooltip);
						alColors.add (MessagesPanel.getLastestMessageColor (MessagesPanel.TYPE_COMBAT, 0));
					}

					tooltipX = messageIconPoints[1].x;
					tooltipY = messageIconPoints[1].y + UtilFont.MAX_HEIGHT * 2;
					MainPanel.renderMessages (tooltipX, tooltipY, MainPanel.renderWidth, MainPanel.renderHeight, 2, alMessages, alColors);
					return;
				} else if (mousePanel == MOUSE_MESSAGES_ICON_HEROES) {
					ArrayList<String> alMessages = new ArrayList<String> (4);
					ArrayList<ColorGL> alColors = new ArrayList<ColorGL> (4);
					alMessages.add (Messages.getString ("UIPanel.28")); //$NON-NLS-1$
					alColors.add (ColorGL.WHITE);

					tooltip = MessagesPanel.getLastestMessage (MessagesPanel.TYPE_HEROES, 2);
					if (tooltip != null) {
						alMessages.add (tooltip);
						alColors.add (MessagesPanel.getLastestMessageColor (MessagesPanel.TYPE_HEROES, 2));
					}
					tooltip = MessagesPanel.getLastestMessage (MessagesPanel.TYPE_HEROES, 1);
					if (tooltip != null) {
						alMessages.add (tooltip);
						alColors.add (MessagesPanel.getLastestMessageColor (MessagesPanel.TYPE_HEROES, 1));
					}
					tooltip = MessagesPanel.getLastestMessage (MessagesPanel.TYPE_HEROES, 0);
					if (tooltip != null) {
						alMessages.add (tooltip);
						alColors.add (MessagesPanel.getLastestMessageColor (MessagesPanel.TYPE_HEROES, 0));
					}

					tooltipX = messageIconPoints[2].x;
					tooltipY = messageIconPoints[2].y + UtilFont.MAX_HEIGHT * 2;
					MainPanel.renderMessages (tooltipX, tooltipY, MainPanel.renderWidth, MainPanel.renderHeight, 2, alMessages, alColors);
					return;
				} else if (mousePanel == MOUSE_MESSAGES_ICON_SYSTEM) {
					ArrayList<String> alMessages = new ArrayList<String> (4);
					ArrayList<ColorGL> alColors = new ArrayList<ColorGL> (4);
					alMessages.add (Messages.getString ("UIPanel.31")); //$NON-NLS-1$
					alColors.add (ColorGL.WHITE);

					tooltip = MessagesPanel.getLastestMessage (MessagesPanel.TYPE_SYSTEM, 2);
					if (tooltip != null) {
						alMessages.add (tooltip);
						alColors.add (MessagesPanel.getLastestMessageColor (MessagesPanel.TYPE_SYSTEM, 2));
					}
					tooltip = MessagesPanel.getLastestMessage (MessagesPanel.TYPE_SYSTEM, 1);
					if (tooltip != null) {
						alMessages.add (tooltip);
						alColors.add (MessagesPanel.getLastestMessageColor (MessagesPanel.TYPE_SYSTEM, 1));
					}
					tooltip = MessagesPanel.getLastestMessage (MessagesPanel.TYPE_SYSTEM, 0);
					if (tooltip != null) {
						alMessages.add (tooltip);
						alColors.add (MessagesPanel.getLastestMessageColor (MessagesPanel.TYPE_SYSTEM, 0));
					}

					tooltipX = messageIconPoints[3].x;
					tooltipY = messageIconPoints[3].y + UtilFont.MAX_HEIGHT * 2;
					MainPanel.renderMessages (tooltipX, tooltipY, MainPanel.renderWidth, MainPanel.renderHeight, 2, alMessages, alColors);
					return;
				}
			}
		}

		if (tooltip != null) {
			UtilsGL.drawTooltip (tooltip, tooltipX, tooltipY, renderWidth, renderHeight);
		}

		if (typingPanel == null) {
			// Multi-lineas tooltip
			if (mousePanel == MOUSE_MESSAGES_ICON_ANNOUNCEMENT) {
				tooltip = MessagesPanel.getLastestMessage (MessagesPanel.TYPE_ANNOUNCEMENT, 2);
				if (tooltip != null) {
					tooltipY += UtilFont.MAX_HEIGHT;
					UtilsGL.drawTooltip (tooltip, tooltipX, tooltipY, renderWidth, renderHeight);
				}

				tooltip = MessagesPanel.getLastestMessage (MessagesPanel.TYPE_ANNOUNCEMENT, 1);
				if (tooltip != null) {
					tooltipY += UtilFont.MAX_HEIGHT;
					UtilsGL.drawTooltip (tooltip, tooltipX, tooltipY, renderWidth, renderHeight);
				}

				tooltip = MessagesPanel.getLastestMessage (MessagesPanel.TYPE_ANNOUNCEMENT, 0);
				if (tooltip != null) {
					tooltipY += UtilFont.MAX_HEIGHT;
					UtilsGL.drawTooltip (tooltip, tooltipX, tooltipY, renderWidth, renderHeight);
				}
			} else if (mousePanel == MOUSE_MESSAGES_ICON_COMBAT) {
				tooltip = MessagesPanel.getLastestMessage (MessagesPanel.TYPE_COMBAT, 2);
				if (tooltip != null) {
					tooltipY += UtilFont.MAX_HEIGHT;
					UtilsGL.drawTooltip (tooltip, tooltipX, tooltipY, renderWidth, renderHeight);
				}

				tooltip = MessagesPanel.getLastestMessage (MessagesPanel.TYPE_COMBAT, 1);
				if (tooltip != null) {
					tooltipY += UtilFont.MAX_HEIGHT;
					UtilsGL.drawTooltip (tooltip, tooltipX, tooltipY, renderWidth, renderHeight);
				}

				tooltip = MessagesPanel.getLastestMessage (MessagesPanel.TYPE_COMBAT, 0);
				if (tooltip != null) {
					tooltipY += UtilFont.MAX_HEIGHT;
					UtilsGL.drawTooltip (tooltip, tooltipX, tooltipY, renderWidth, renderHeight);
				}
			} else if (mousePanel == MOUSE_MESSAGES_ICON_HEROES) {
				tooltip = MessagesPanel.getLastestMessage (MessagesPanel.TYPE_HEROES, 2);
				if (tooltip != null) {
					tooltipY += UtilFont.MAX_HEIGHT;
					UtilsGL.drawTooltip (tooltip, tooltipX, tooltipY, renderWidth, renderHeight);
				}

				tooltip = MessagesPanel.getLastestMessage (MessagesPanel.TYPE_HEROES, 1);
				if (tooltip != null) {
					tooltipY += UtilFont.MAX_HEIGHT;
					UtilsGL.drawTooltip (tooltip, tooltipX, tooltipY, renderWidth, renderHeight);
				}

				tooltip = MessagesPanel.getLastestMessage (MessagesPanel.TYPE_HEROES, 0);
				if (tooltip != null) {
					tooltipY += UtilFont.MAX_HEIGHT;
					UtilsGL.drawTooltip (tooltip, tooltipX, tooltipY, renderWidth, renderHeight);
				}
			} else if (mousePanel == MOUSE_MESSAGES_ICON_SYSTEM) {
				tooltip = MessagesPanel.getLastestMessage (MessagesPanel.TYPE_SYSTEM, 2);
				if (tooltip != null) {
					tooltipY += UtilFont.MAX_HEIGHT;
					UtilsGL.drawTooltip (tooltip, tooltipX, tooltipY, renderWidth, renderHeight);
				}

				tooltip = MessagesPanel.getLastestMessage (MessagesPanel.TYPE_SYSTEM, 1);
				if (tooltip != null) {
					tooltipY += UtilFont.MAX_HEIGHT;
					UtilsGL.drawTooltip (tooltip, tooltipX, tooltipY, renderWidth, renderHeight);
				}

				tooltip = MessagesPanel.getLastestMessage (MessagesPanel.TYPE_SYSTEM, 0);
				if (tooltip != null) {
					tooltipY += UtilFont.MAX_HEIGHT;
					UtilsGL.drawTooltip (tooltip, tooltipX, tooltipY, renderWidth, renderHeight);
				}
			} else if (mousePanel == MOUSE_EVENTS_ICON) {
				ArrayList<EventData> alEvents = Game.getWorld ().getEvents ();
				if (alEvents.size () == 0) {
					tooltip = Messages.getString ("UIPanel.83"); //$NON-NLS-1$
					UtilsGL.drawTooltip (tooltip, iconEventsPoint.x + GlobalEventData.getIcon ().getTileWidth () / 2 - UtilFont.getWidth (tooltip) / 2, iconEventsPoint.y + GlobalEventData.getIcon ().getTileHeight (), renderWidth, renderHeight);
				} else {
					// Obtenemos el tamaño del tooltip
					tooltip = Messages.getString ("UIPanel.84"); //$NON-NLS-1$
					int tooltipWidth = UtilFont.getWidth (tooltip);
					int tooltipHeight = UtilFont.MAX_HEIGHT; // Título

					EventData ed;
					EventManagerItem emi;
					int iAux;
					for (int i = 0; i < alEvents.size (); i++) {
						ed = alEvents.get (i);
						emi = EventManager.getItem (ed.getEventID ());
						if (emi != null) {
							// Alto
							if (emi.getIcon () != null) {
								tooltipHeight += emi.getIcon ().getTileHeight () + 2;

								// Ancho
								iAux = UtilFont.getWidth (emi.getName ()) + emi.getIcon ().getTileWidth ();
								if (iAux > tooltipWidth) {
									tooltipWidth = iAux;
								}
							} else {
								tooltipHeight += UtilFont.MAX_HEIGHT + 2;

								// Ancho
								iAux = UtilFont.getWidth (emi.getName ());
								if (iAux > tooltipWidth) {
									tooltipWidth = iAux;
								}
							}
						}
					}
					tooltipX = iconEventsPoint.x + GlobalEventData.getIcon ().getTileWidth () / 2 - tooltipWidth / 2;
					tooltipY = iconEventsPoint.y + GlobalEventData.getIcon ().getTileHeight ();

					// Renderizamos
					// Fondo
					int iCurrentTexture = UIPanel.tileTooltipBackground.getTextureID ();
					GL11.glColor4f (1, 1, 1, 1);
					GL11.glBindTexture (GL11.GL_TEXTURE_2D, UIPanel.tileTooltipBackground.getTextureID ());
					UtilsGL.glBegin (GL11.GL_QUADS);
					UtilsGL.drawTexture (tooltipX, tooltipY - 4, tooltipX + tooltipWidth + 8, tooltipY + tooltipHeight + 4, UIPanel.tileTooltipBackground.getTileSetTexX0 (), UIPanel.tileTooltipBackground.getTileSetTexY0 (), UIPanel.tileTooltipBackground.getTileSetTexX1 (), UIPanel.tileTooltipBackground.getTileSetTexY1 ());

					// Iconos
					int iCurrentHeight = tooltipY + UtilFont.MAX_HEIGHT + 2;
					for (int i = 0; i < alEvents.size (); i++) {
						ed = alEvents.get (i);
						emi = EventManager.getItem (ed.getEventID ());
						if (emi != null) {
							// Alto
							if (emi.getIcon () != null) {
								iCurrentTexture = UtilsGL.setTexture (emi.getIcon (), iCurrentTexture);
								drawTile (emi.getIcon (), tooltipX, iCurrentHeight, false);
								iCurrentHeight += emi.getIcon ().getTileHeight () + 2;
							} else {
								iCurrentHeight += UtilFont.MAX_HEIGHT + 2;
							}
						}
					}
					UtilsGL.glEnd ();

					// Textos
					GL11.glBindTexture (GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
					UtilsGL.glBegin (GL11.GL_QUADS);
					iCurrentHeight = tooltipY;
					UtilsGL.drawString (tooltip, tooltipX, iCurrentHeight);
					iCurrentHeight += UtilFont.MAX_HEIGHT + 2;

					for (int i = 0; i < alEvents.size (); i++) {
						ed = alEvents.get (i);
						emi = EventManager.getItem (ed.getEventID ());
						if (emi != null) {
							// Alto
							if (emi.getIcon () != null) {
								UtilsGL.drawString (emi.getName (), tooltipX + emi.getIcon ().getTileWidth () + 4, iCurrentHeight + emi.getIcon ().getTileHeight () / 2 - UtilFont.MAX_HEIGHT / 2);
								iCurrentHeight += emi.getIcon ().getTileHeight () + 2;
							} else {
								UtilsGL.drawString (emi.getName (), tooltipX, iCurrentHeight);
								iCurrentHeight += UtilFont.MAX_HEIGHT + 2;
							}
						}
					}

					UtilsGL.glEnd ();
				}
				// } else if (mousePanel == MOUSE_GODS_ICON) {
				// ArrayList<GodData> alGods = Game.getWorld ().getGods ();
				// int iNonHidden = 0;
				// for (int i = 0; i < alGods.size (); i++) {
				// if (!alGods.get (i).isHidden ()) {
				// iNonHidden++;
				// }
				// }
				// if (iNonHidden == 0) {
				//					tooltip = Messages.getString("UIPanel.77"); //$NON-NLS-1$
				// UtilsGL.drawTooltip (tooltip, iconGodsPoint.x + tileIconGods.getTileWidth () / 2 - UtilFont.getWidth (tooltip) / 2, iconGodsPoint.y + tileIconGods.getTileHeight (), renderWidth, renderHeight);
				// } else {
				// // Obtenemos el tamaño del tooltip
				//					tooltip = Messages.getString("UIPanel.78"); //$NON-NLS-1$
				// int tooltipWidth = UtilFont.getWidth (tooltip);
				// int tooltipHeight = UtilFont.MAX_HEIGHT; // Título
				//
				// GodData gd;
				// int iAux;
				// // Alto
				// tooltipHeight += iNonHidden * UtilFont.MAX_HEIGHT + 2;
				// for (int i = 0; i < alGods.size (); i++) {
				// gd = alGods.get (i);
				// if (!gd.isHidden ()) {
				// // Ancho
				// if (Game.DEBUG_MODE) {
				//								iAux = UtilFont.getWidth (gd.getFullName () + " (" + gd.getStatus () + ")"); //$NON-NLS-1$ //$NON-NLS-2$
				// } else {
				// iAux = UtilFont.getWidth (gd.getFullName ());
				// }
				//
				// if (iAux > tooltipWidth) {
				// tooltipWidth = iAux;
				// }
				// }
				// }
				//
				// tooltipX = iconGodsPoint.x + tileIconGods.getTileWidth () / 2 - tooltipWidth / 2;
				// tooltipY = iconGodsPoint.y + tileIconGods.getTileHeight ();
				//
				// // Renderizamos
				// // Fondo
				// GL11.glColor4f (1, 1, 1, 1);
				// GL11.glBindTexture (GL11.GL_TEXTURE_2D, UIPanel.tileTooltipBackground.getTextureID ());
				// UtilsGL.glBegin (GL11.GL_QUADS);
				// UtilsGL.drawTexture (tooltipX, tooltipY - 4, tooltipX + tooltipWidth + 8, tooltipY + tooltipHeight + 4, UIPanel.tileTooltipBackground.getTileSetTexX0 (), UIPanel.tileTooltipBackground.getTileSetTexY0 (), UIPanel.tileTooltipBackground.getTileSetTexX1 (), UIPanel.tileTooltipBackground.getTileSetTexY1 ());
				// UtilsGL.glEnd ();
				//
				// // Textos
				// GL11.glBindTexture (GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
				// UtilsGL.glBegin (GL11.GL_QUADS);
				// int iCurrentHeight = tooltipY;
				// UtilsGL.drawString (tooltip, tooltipX, iCurrentHeight);
				// iCurrentHeight += UtilFont.MAX_HEIGHT + 2;
				//
				// for (int i = 0; i < alGods.size (); i++) {
				// gd = alGods.get (i);
				//
				// if (Game.DEBUG_MODE) {
				//							UtilsGL.drawString (gd.getFullName () + " (" + gd.getStatus () + ")", tooltipX, iCurrentHeight); //$NON-NLS-1$ //$NON-NLS-2$
				// } else {
				// UtilsGL.drawString (gd.getFullName (), tooltipX, iCurrentHeight);
				// }
				// iCurrentHeight += UtilFont.MAX_HEIGHT + 2;
				// }
				//
				// UtilsGL.glEnd ();
				// }
			}
		}
	}


	public static void drawTile (Tile tile, Point point, boolean bigger) {
		drawTile (tile, point, tile.getTileWidth (), tile.getTileHeight (), bigger);
	}


	/**
	 * Draws a tile
	 * 
	 * @param tile Tile
	 * @param point Coordinates
	 * @param width Base width
	 * @param height Base height
	 * @param bigger Make it bigger?
	 */
	private static void drawTile (Tile tile, Point point, int width, int height, boolean bigger) {
		int iTemp = (width - tile.getTileWidth ()) / 2;
		int iTemp2 = (height - tile.getTileHeight ()) / 2;

		if (bigger) {
			UtilsGL.drawTexture (point.x - (tile.getTileWidth () / 4) + iTemp, point.y - (tile.getTileHeight () / 4) + iTemp2, point.x + tile.getTileWidth () + (tile.getTileWidth () / 4) + iTemp, point.y + tile.getTileHeight () + (tile.getTileHeight () / 4) + iTemp2, tile.getTileSetTexX0 (), tile.getTileSetTexY0 (), tile.getTileSetTexX1 (), tile.getTileSetTexY1 ());
		} else {
			UtilsGL.drawTexture (point.x + iTemp, point.y + iTemp2, point.x + tile.getTileWidth () + iTemp, point.y + tile.getTileHeight () + iTemp2, tile.getTileSetTexX0 (), tile.getTileSetTexY0 (), tile.getTileSetTexX1 (), tile.getTileSetTexY1 ());
		}
	}


	private static void drawTile (Tile tile, int pointX, int pointY, int width, int height, boolean bigger) {
		int iTemp = (width - tile.getTileWidth ()) / 2;
		int iTemp2 = (height - tile.getTileHeight ()) / 2;

		if (bigger) {
			UtilsGL.drawTexture (pointX - (tile.getTileWidth () / 4) + iTemp, pointY - (tile.getTileHeight () / 4) + iTemp2, pointX + tile.getTileWidth () + (tile.getTileWidth () / 4) + iTemp, pointY + tile.getTileHeight () + (tile.getTileHeight () / 4) + iTemp2, tile.getTileSetTexX0 (), tile.getTileSetTexY0 (), tile.getTileSetTexX1 (), tile.getTileSetTexY1 ());
		} else {
			UtilsGL.drawTexture (pointX + iTemp, pointY + iTemp2, pointX + tile.getTileWidth () + iTemp, pointY + tile.getTileHeight () + iTemp2, tile.getTileSetTexX0 (), tile.getTileSetTexY0 (), tile.getTileSetTexX1 (), tile.getTileSetTexY1 ());
		}
	}


	public static void drawTile (Tile tile, Point point) {
		UtilsGL.drawTexture (point.x, point.y, point.x + tile.getTileWidth (), point.y + tile.getTileHeight (), tile.getTileSetTexX0 (), tile.getTileSetTexY0 (), tile.getTileSetTexX1 (), tile.getTileSetTexY1 ());
	}


	private static void drawTile (Tile tile, int x, int y, boolean bigger) {
		drawTile (tile, x, y, tile.getTileWidth (), tile.getTileHeight (), bigger);
	}


	/**
	 * Cierra los menús que no están locked indicados
	 * 
	 * @param bottom
	 * @param right
	 * @param production
	 */
	private void closeNonLockedMenus (boolean bottom, boolean right, boolean production) {
		if (bottom && !isBottomMenuPanelLocked ()) {
			setBottomMenuPanelActive (false);
		}
		if (right && !isMenuPanelLocked ()) {
			setMenuPanelActive (false);
		}
		if (production && !isProductionPanelLocked ()) {
			setProductionPanelActive (false);
		}
	}


	public int isMouseOnAPanel (int x, int y) {
		return isMouseOnAPanel (x, y, false);
	}


	/**
	 * Indica si el ratón está en algún panel. Retorna un código según el panel
	 * 
	 * @param x
	 * @param y
	 * @param doEdgeMenusStuff. Setea el delay a 0 si el mouse está en uno de los paneles laterales, también abre/cierra menus y tal
	 * @return
	 */
	public int isMouseOnAPanel (int x, int y, boolean doEdgeMenusStuff) {
		/*
		 * TYPING PANEL (Si está activo ya no miraremos nada más)
		 */
		if (typingPanel != null) {
			if (isMouseOnTypingPanel (x, y)) {
				if (isMouseOnAnIcon (x, y, TypingPanel.getCloseButtonPoint (), tileButtonClose, tileButtonCloseAlpha)) {
					return MOUSE_TYPING_PANEL_CLOSE;
				} else if (isMouseOnAnIcon (x, y, TypingPanel.getConfirmPoint (), TypingPanel.getTileConfirm (), TypingPanel.getTileConfirmAlpha ())) {
					return MOUSE_TYPING_PANEL_CONFIRM;
				}

				return MOUSE_TYPING_PANEL;
			}

			return MOUSE_NONE;
		}

		/*
		 * IMAGES PANEL
		 */
		if (imagesPanel != null && ImagesPanel.isVisible ()) {
			if (isMouseOnImagesPanel (x, y)) {
				if (isMouseOnAnIcon (x, y, ImagesPanel.getCloseButtonPoint (), tileButtonClose, tileButtonCloseAlpha)) {
					return MOUSE_IMAGES_PANEL_CLOSE;
				} else if (isMouseOnAnIcon (x, y, ImagesPanel.getPreviousImagePoint (), ImagesPanel.getTilePrevious ())) {
					return MOUSE_IMAGES_PANEL_PREVIOUS;
				} else if (isMouseOnAnIcon (x, y, ImagesPanel.getNextImagePoint (), ImagesPanel.getTileNext ())) {
					return MOUSE_IMAGES_PANEL_NEXT;
				} else if (isMouseOnAnIcon (x, y, ImagesPanel.getNextMissionPoint (), ImagesPanel.getTileNextMission ())) {
					return MOUSE_IMAGES_PANEL_NEXT_MISSION;
				}

				return MOUSE_IMAGES_PANEL;
			}

			// Miramos también el botón (para hacer toggle)
//			if (isMouseOnAnIcon (x, y, iconTutorialPoint, tileBottomItem, tileBottomItemAlpha)) {
//				return MOUSE_TUTORIAL_ICON;
//			}
//
//			return MOUSE_NONE;
		}

		/*
		 * PROFESSIONS PANEL
		 */
		if (isProfessionsPanelActive ()) {
			if (isMouseOnProfessionsPanel (x, y)) {
				if (doEdgeMenusStuff) {
					// Cerramos los menús no locked
					closeNonLockedMenus (true, true, true);
				}

				Point p = isMouseOnProfessionsButtons (x, y);
				if (p != null) {
					return p.x;
				}

				return MOUSE_PROFESSIONS_PANEL;
			}
		}

		/*
		 * PILE PANEL
		 */
		if (isPilePanelActive ()) {
			if (isMouseOnPilePanel (x, y)) {
				if (doEdgeMenusStuff) {
					// Cerramos los menús no locked
					closeNonLockedMenus (true, true, true);
				}

				Point p = isMouseOnPileButtons (x, y);
				if (p != null) {
					return p.x;
				}

				return MOUSE_PILE_PANEL;
			}
		}

		/*
		 * MESSAGES PANEL
		 */
		if (isMessagesPanelActive ()) {
			if (isMouseOnMessagesPanel (x, y)) {
				if (doEdgeMenusStuff) {
					// Cerramos los menús no locked
					closeNonLockedMenus (true, true, true);
				}

				Point p = isMouseOnMessagesButtons (x, y);
				if (p != null) {
					return p.x;
				}

				return MOUSE_MESSAGES_PANEL;
			}
		}

		/*
		 * MATS PANEL
		 */
		if (isMatsPanelActive ()) {
			if (isMouseOnMatsPanel (x, y)) {
				if (doEdgeMenusStuff) {
					// Cerramos los menús no locked
					closeNonLockedMenus (true, true, true);
				}

				Point p = isMouseOnMatsButtons (x, y);
				if (p != null) {
					return p.x;
				}

				return MOUSE_MATS_PANEL;
			}
		}

		/*
		 * LIVINGS PANEL
		 */
		if (isLivingsPanelActive ()) {
			if (isMouseOnLivingsPanel (x, y)) {
				if (doEdgeMenusStuff) {
					// Cerramos los menús no locked
					closeNonLockedMenus (true, true, true);
				}

				Point p = isMouseOnLivingsButtons (x, y);
				if (p != null) {
					return p.x;
				}

				return MOUSE_LIVINGS_PANEL;
			}
		}

		/*
		 * TRADE PANEL
		 */
		if (isTradePanelActive ()) {
			if (isMouseOnTradePanel (x, y)) {
				if (doEdgeMenusStuff) {
					// Cerramos los menús no locked
					closeNonLockedMenus (true, true, true);
				}

				Point p = isMouseOnTradeButtons (x, y);
				if (p != null) {
					return p.x;
				}

				return MOUSE_TRADE_PANEL;
			}
		}

		/*
		 * PRIORITIES PANEL
		 */
		if (isPrioritiesPanelActive ()) {
			if (isMouseOnPrioritiesPanel (x, y)) {
				if (doEdgeMenusStuff) {
					// Cerramos los menús no locked
					closeNonLockedMenus (true, true, true);
				}

				Point p = isMouseOnPrioritiesItems (x, y);
				if (p != null) {
					return p.x;
				}
				return MOUSE_PRIORITIES_PANEL;
			}
		}

		/*
		 * PRODUCTION PANEL
		 */
		if (isProductionPanelActive ()) {
			if (isMouseOnAnIcon (x, y, tileOpenCloseProductionPanelPoint, tileOpenProductionPanelON, tileOpenProductionPanelONAlpha)) {
				if (doEdgeMenusStuff) {
					// Cerramos los menús no locked
					closeNonLockedMenus (true, true, false);
					delayTime = 0;
				}
				return MOUSE_PRODUCTION_OPENCLOSE;
			}
			if (isMouseOnProductionPanel (x, y)) {
				if (doEdgeMenusStuff) {
					// Cerramos los menús no locked
					closeNonLockedMenus (true, true, false);
					delayTime = 0;
				}

				Point p = isMouseOnProductionItems (x, y);
				if (p != null) {
					return p.x;
				}

				return MOUSE_PRODUCTION_PANEL;
			}
			if (doEdgeMenusStuff) {
				if (delayTime > (Game.FPS_INGAME / 8) * 6) {
					if (!isProductionPanelLocked () && !isMouseOnAnIcon (x, y, tileOpenCloseProductionPanelPoint, tileOpenProductionPanel, tileOpenProductionPanelAlpha)) {
						delayTime = 0;
						setProductionPanelActive (false);
					}
				}
			}
		} else {
			if (doEdgeMenusStuff) {
				if (isMouseOnAnIcon (x, y, tileOpenCloseProductionPanelPoint, tileOpenProductionPanel, tileOpenProductionPanelAlpha)) {
					setProductionPanelActive (true);

					// Cerramos los menús no locked
					closeNonLockedMenus (true, true, false);
					delayTime = 0;
					return MOUSE_PRODUCTION_OPENCLOSE;
				}
			}
		}

		// BOTTOM
		if (isBottomMenuPanelActive ()) {
			if (isMouseOnBottomLeftScroll (x, y)) {
				if (doEdgeMenusStuff) {
					// Cerramos los menús no locked
					closeNonLockedMenus (false, true, true);
					delayTime = 0;
				}
				return MOUSE_BOTTOM_LEFT_SCROLL;
			}
			if (isMouseOnBottomRightScroll (x, y)) {
				if (doEdgeMenusStuff) {
					// Cerramos los menús no locked
					closeNonLockedMenus (false, true, true);
					delayTime = 0;
				}
				return MOUSE_BOTTOM_RIGHT_SCROLL;
			}
			if (isMouseOnBottomItems (x, y) != -1) {
				if (doEdgeMenusStuff) {
					// Cerramos los menús no locked
					closeNonLockedMenus (false, true, true);
					delayTime = 0;
				}
				return MOUSE_BOTTOM_ITEMS;
			}
			if (isMouseOnBottomPanel (x, y)) { // Este check tiene que ir detrás de los items, ya que los items están encima
				if (doEdgeMenusStuff) {
					// Cerramos los menús no locked
					closeNonLockedMenus (false, true, true);
					delayTime = 0;
				}
				return MOUSE_BOTTOM_PANEL;
			}
			if (isMouseOnAnIcon (x, y, tileOpenCloseBottomMenuPoint, tileOpenBottomMenuON, tileOpenBottomMenuONAlpha)) {
				if (doEdgeMenusStuff) {
					// Cerramos los menús no locked
					closeNonLockedMenus (false, true, true);
					delayTime = 0;
				}
				return MOUSE_BOTTOM_OPENCLOSE;
			}

			if (bottomSubPanelMenu != null) {
				// BOTTOM SUBPANEL
				if (isMouseOnBottomSubItems (x, y) != -1) {
					if (doEdgeMenusStuff) {
						// Cerramos los menús no locked
						closeNonLockedMenus (false, true, true);
						delayTime = 0;
					}
					return MOUSE_BOTTOM_SUBITEMS;
				}
				if (isMouseOnBottomSubPanel (x, y)) {
					if (doEdgeMenusStuff) {
						// Cerramos los menús no locked
						closeNonLockedMenus (false, true, true);
						delayTime = 0;
					}
					return MOUSE_BOTTOM_SUBPANEL;
				}
			}

			if (doEdgeMenusStuff) {
				if (delayTime > (Game.FPS_INGAME / 8) * 6) {
					if (!isBottomMenuPanelLocked () && !isMouseOnAnIcon (x, y, tileOpenCloseBottomMenuPoint, tileOpenBottomMenu, tileOpenBottomMenuAlpha)) {
						delayTime = 0;
						setBottomMenuPanelActive (false);
					}
				}
			}
		} else {
			if (doEdgeMenusStuff) {
				if (isMouseOnAnIcon (x, y, tileOpenCloseBottomMenuPoint, tileOpenBottomMenu, tileOpenBottomMenuAlpha)) {
					setBottomMenuPanelActive (true);

					// Cerramos los menús no locked
					closeNonLockedMenus (false, true, true);
					delayTime = 0;
					return MOUSE_BOTTOM_OPENCLOSE;
				}
			}
		}

		// MENU (right)
		if (isMenuPanelActive ()) {
			if (isMouseOnMenuItems (x, y) != -1) {
				if (doEdgeMenusStuff) {
					// Cerramos los menús no locked
					closeNonLockedMenus (true, false, true);
					delayTime = 0;
				}
				return MOUSE_MENU_PANEL_ITEMS;
			}
			if (isMouseOnMenuPanel (x, y)) {
				if (doEdgeMenusStuff) {
					// Cerramos los menús no locked
					closeNonLockedMenus (true, false, true);
					delayTime = 0;
				}
				return MOUSE_MENU_PANEL;
			}
			if (isMouseOnAnIcon (x, y, tileOpenCloseRightMenuPoint, tileOpenRightMenuON, tileOpenRightMenuONAlpha)) {
				if (doEdgeMenusStuff) {
					// Cerramos los menús no locked
					closeNonLockedMenus (true, false, true);
					delayTime = 0;
				}
				return MOUSE_MENU_OPENCLOSE;
			}

			if (doEdgeMenusStuff) {
				if (delayTime > (Game.FPS_INGAME / 8) * 6) {
					if (!isMenuPanelLocked () && !isMouseOnAnIcon (x, y, tileOpenCloseRightMenuPoint, tileOpenRightMenu, tileOpenRightMenuAlpha)) {
						delayTime = 0;
						setMenuPanelActive (false);
					}
				}
			}
		} else {
			if (doEdgeMenusStuff) {
				if (isMouseOnAnIcon (x, y, tileOpenCloseRightMenuPoint, tileOpenRightMenu, tileOpenRightMenuAlpha)) {
					setMenuPanelActive (true);

					// Cerramos los menús no locked
					closeNonLockedMenus (true, false, true);
					delayTime = 0;
					return MOUSE_MENU_OPENCLOSE;
				}
			}
		}

		// MINI ICONS
		if (isMouseOnAnIcon (x, y, iconLevelUpPoint, tileIconLevelUp, tileIconLevelUpAlpha)) {
			return MOUSE_ICON_LEVEL_UP;
		}
		if (isMouseOnAnIcon (x, y, iconLevelPoint, tileIconLevel, tileIconLevelAlpha)) {
			return MOUSE_ICON_LEVEL;
		}
		if (isMouseOnAnIcon (x, y, iconLevelDownPoint, tileIconLevelDown, tileIconLevelDownAlpha)) {
			return MOUSE_ICON_LEVEL_DOWN;
		}
		if (isMouseOnAnIcon (x, y, iconCitizenPreviousPoint, tileIconCitizenPrevious, tileIconPreviousMiniAlpha)) {
			return MOUSE_ICON_CITIZEN_PREVIOUS;
		}
		if (isMouseOnAnIcon (x, y, iconCitizenNextPoint, tileIconCitizenNext, tileIconNextMiniAlpha)) {
			return MOUSE_ICON_CITIZEN_NEXT;
		}
		if (isMouseOnAnIcon (x, y, iconSoldierPreviousPoint, tileIconSoldierPrevious, tileIconPreviousMiniAlpha)) {
			return MOUSE_ICON_SOLDIER_PREVIOUS;
		}
		if (isMouseOnAnIcon (x, y, iconSoldierNextPoint, tileIconSoldierNext, tileIconNextMiniAlpha)) {
			return MOUSE_ICON_SOLDIER_NEXT;
		}
		if (isMouseOnAnIcon (x, y, iconHeroPreviousPoint, tileIconHeroPrevious, tileIconPreviousMiniAlpha)) {
			return MOUSE_ICON_HERO_PREVIOUS;
		}
		if (isMouseOnAnIcon (x, y, iconHeroNextPoint, tileIconHeroNext, tileIconNextMiniAlpha)) {
			return MOUSE_ICON_HERO_NEXT;
		}

		// Messages
		if (isMouseOnAnIcon (x, y, messageIconPoints[0], messageTiles[0], messageTilesAlpha.get (0))) {
			return MOUSE_MESSAGES_ICON_ANNOUNCEMENT;
		}
		if (isMouseOnAnIcon (x, y, messageIconPoints[1], messageTiles[1], messageTilesAlpha.get (1))) {
			return MOUSE_MESSAGES_ICON_COMBAT;
		}
		if (isMouseOnAnIcon (x, y, messageIconPoints[2], messageTiles[2], messageTilesAlpha.get (2))) {
			return MOUSE_MESSAGES_ICON_HEROES;
		}
		if (isMouseOnAnIcon (x, y, messageIconPoints[3], messageTiles[3], messageTilesAlpha.get (3))) {
			return MOUSE_MESSAGES_ICON_SYSTEM;
		}

		// Events
		if (isMouseOnAnIcon (x, y, iconEventsPoint, GlobalEventData.getIcon ())) {
			return MOUSE_EVENTS_ICON;
		}

		// Tutorial
		if (isMouseOnAnIcon (x, y, iconTutorialPoint, tileIconTutorial)) {
			return MOUSE_TUTORIAL_ICON;
		}

		// Gods
		// if (TownsProperties.GODS_ACTIVATED) {
		// if (isMouseOnAnIcon (x, y, iconGodsPoint, tileIconGods)) {
		// return MOUSE_GODS_ICON;
		// }
		// }

		// Backgrounds
		if (isMouseOnAnIcon (x, y, iconNumCitizensBackgroundPoint, tileBottomItem, tileBottomItemAlpha)) {
			return MOUSE_INFO_NUM_CITIZENS;
		}
		if (isMouseOnAnIcon (x, y, iconNumSoldiersBackgroundPoint, tileBottomItem, tileBottomItemAlpha)) {
			return MOUSE_INFO_NUM_SOLDIERS;
		}
		if (isMouseOnAnIcon (x, y, iconNumHeroesBackgroundPoint, tileBottomItem, tileBottomItemAlpha)) {
			return MOUSE_INFO_NUM_HEROES;
		}
		if (isMouseOnAnIcon (x, y, iconCaravanBackgroundPoint, tileBottomItem, tileBottomItemAlpha)) {
			return MOUSE_INFO_CARAVAN;
		}

		// ICONS
		if (isMouseOnAnIcon (x, y, iconPrioritiesPoint, tileIconPriorities, tileIconPrioritiesAlpha)) {
			return MOUSE_ICON_PRIORITIES;
		}
		if (isMouseOnAnIcon (x, y, iconMatsPoint, tileIconMats, tileIconMatsAlpha)) {
			return MOUSE_ICON_MATS;
		}
		if (isMouseOnAnIcon (x, y, iconMiniblocksPoint, tileIconMiniblocks, tileIconMiniblocksAlpha)) {
			return MOUSE_ICON_MINIBLOCKS;
		}
		if (isMouseOnAnIcon (x, y, iconFlatMousePoint, tileIconFlatMouse, tileIconFlatMouseAlpha)) {
			return MOUSE_ICON_FLATMOUSE;
		}
		if (isMouseOnAnIcon (x, y, icon3DMousePoint, tileIcon3DMouse, tileIcon3DMouseAlpha)) {
			return MOUSE_ICON_3DMOUSE;
		}
		if (isMouseOnAnIcon (x, y, iconGridPoint, tileIconGrid, tileIconGridAlpha)) {
			return MOUSE_ICON_GRID;
		}
		if (isMouseOnAnIcon (x, y, iconSettingsPoint, tileIconSettings, tileIconSettingsAlpha)) {
			return MOUSE_ICON_SETTINGS;
		}
		if (isMouseOnAnIcon (x, y, iconPauseResumePoint, tileIconPause, tileIconPauseResumeAlpha)) {
			return MOUSE_ICON_PAUSE_RESUME;
		}
		if (isMouseOnAnIcon (x, y, iconLowerSpeedPoint, tileIconLowerSpeed, tileIconLowerSpeedAlpha)) {
			return MOUSE_ICON_LOWER_SPEED;
		}
		if (isMouseOnAnIcon (x, y, iconIncreaseSpeedPoint, tileIconIncreaseSpeed, tileIconIncreaseSpeedAlpha)) {
			return MOUSE_ICON_INCREASE_SPEED;
		}
		if (Game.getCurrentMissionData () != null && Game.getCurrentMissionData ().getTutorialFlows ().size () > 0) {
			if (isMouseOnAnIcon (x, y, iconTutorialPoint, tileBottomItem, tileBottomItemAlpha)) {
				return MOUSE_TUTORIAL_ICON;
			}
		}

		// DATE
		if (isMouseOnDatePanel (x, y)) {
			return MOUSE_DATEPANEL;
		}

		// INFO
		if (isMouseOnInfoPanel (x, y)) {
			return MOUSE_INFOPANEL;
		}

		// MINIMAP
		if (isMouseOnMinimap (x, y)) {
			return MOUSE_MINIMAP;
		}

		return MOUSE_NONE;
	}


	public static boolean isMouseOnAnIcon (int x, int y, Point point, Tile tile) {
		if ((y >= point.y && y < (point.y + tile.getTileHeight ())) && (x >= point.x && x < (point.x + tile.getTileWidth ()))) {
			return true;
		}

		return false;
	}


	public static boolean isMouseOnAnIcon (int x, int y, Point point, Tile tile, boolean[][] alpha) {
		if ((y >= point.y && y < (point.y + tile.getTileHeight ())) && (x >= point.x && x < (point.x + tile.getTileWidth ()))) {
			return !alpha[x - point.x][y - point.y];
		}

		return false;
	}


	public static boolean isMouseCloseToOpenCloseBottomIcon (int x, int y) {
		return isMouseCloseToIcon (x, y, tileOpenCloseBottomMenuPoint, tileOpenBottomMenu, CLOSE_PIXELS);
	}


	public static boolean isMouseCloseToOpenCloseMenuIcon (int x, int y) {
		return isMouseCloseToIcon (x, y, tileOpenCloseRightMenuPoint, tileOpenRightMenu, CLOSE_PIXELS);
	}


	public static boolean isMouseCloseToOpenCloseProductionIcon (int x, int y) {
		return isMouseCloseToIcon (x, y, tileOpenCloseProductionPanelPoint, tileOpenProductionPanel, CLOSE_PIXELS);
	}


	private static boolean isMouseCloseToIcon (int x, int y, Point point, Tile tile, int closeFactor) {
		if ((y >= (point.y - closeFactor) && y < (point.y + tile.getTileHeight () + closeFactor)) && (x >= (point.x - closeFactor) && x < (point.x + tile.getTileWidth () + closeFactor))) {
			return true;
		}

		return false;
	}


	private static boolean isMouseOnBottomPanel (int x, int y) {
		if (y >= bottomPanelY && y < (bottomPanelY + BOTTOM_PANEL_HEIGHT)) {
			// Dentro del panel "virtual", miramos los paneles internos con sus transparencias

			if (x >= bottomPanelX && x < (bottomPanelX + BOTTOM_PANEL_WIDTH)) {
				return (!tileBottomPanelAlpha[x - bottomPanelX][y - bottomPanelY]);
			}
		}

		return false;
	}


	private boolean isMouseOnBottomLeftScroll (int x, int y) {
		if ((y >= bottomPanelY && y < (bottomPanelY + BOTTOM_PANEL_HEIGHT)) && (x >= bottomPanelLeftScrollX && x < (bottomPanelLeftScrollX + BOTTOM_PANEL_SCROLL_WIDTH))) {
			return !tileBottomScrollLeftAlpha[x - bottomPanelLeftScrollX][y - bottomPanelY];
		}

		return false;
	}


	private boolean isMouseOnBottomRightScroll (int x, int y) {
		if ((y >= bottomPanelY && y < (bottomPanelY + BOTTOM_PANEL_HEIGHT)) && (x >= bottomPanelRightScrollX && x < (bottomPanelRightScrollX + BOTTOM_PANEL_SCROLL_WIDTH))) {
			return !tileBottomScrollRightAlpha[x - bottomPanelRightScrollX][y - bottomPanelY];
		}

		return false;
	}


	/**
	 * Indica si el mouse está en un item, devuelve el número del mismo o -1 en caso de no estar
	 * 
	 * @param x
	 * @param y
	 * @return devuelve el número del item o -1 en caso de no estar
	 */
	private int isMouseOnBottomItems (int x, int y) {
		if (y >= bottomPanelY && y < (bottomPanelY + BOTTOM_PANEL_HEIGHT)) {
			Point point;
			for (int i = 0; i < BOTTOM_PANEL_NUM_ITEMS; i++) {
				point = bottomPanelItemsPosition.get (i);
				if (x >= point.x && x < (point.x + BOTTOM_ITEM_WIDTH)) {
					if (!tileBottomItemAlpha[x - point.x][y - point.y]) {
						return i;
					}
				}
			}
		}

		return -1;
	}


	private boolean isMouseOnBottomSubPanel (int x, int y) {
		if (x >= bottomSubPanelPoint.x && x < (bottomSubPanelPoint.x + BOTTOM_SUBPANEL_WIDTH) && y >= bottomSubPanelPoint.y && y < (bottomSubPanelPoint.y + BOTTOM_SUBPANEL_HEIGHT)) {
			return true;
		}

		return false;
	}


	/**
	 * Indica si el mouse está en un item del submenu de abajo, devuelve el número del mismo o -1 en caso de no estar
	 * 
	 * @param x
	 * @param y
	 * @return devuelve el número del item o -1 en caso de no estar
	 */
	private int isMouseOnBottomSubItems (int x, int y) {
		if (bottomSubPanelMenu != null && y >= bottomSubPanelPoint.y && y < (bottomSubPanelPoint.y + BOTTOM_SUBPANEL_HEIGHT) && x >= bottomSubPanelPoint.x && x < (bottomSubPanelPoint.x + BOTTOM_SUBPANEL_WIDTH)) {
			Point point;
			bucle1: for (int y1 = 0; y1 < BOTTOM_SUBPANEL_NUM_ITEMS_Y; y1++) {
				for (int x1 = 0; x1 < BOTTOM_SUBPANEL_NUM_ITEMS_X; x1++) {
					int i = (y1 * BOTTOM_SUBPANEL_NUM_ITEMS_X) + x1;
					if (i >= bottomSubPanelMenu.getItems ().size ()) {
						break bucle1;
					}
					point = bottomSubPanelItemsPosition.get (i);
					if (x >= point.x && x < (point.x + BOTTOM_SUBITEM_WIDTH) && y >= point.y && y < (point.y + BOTTOM_SUBITEM_HEIGHT)) {
						if (!tileBottomSubItemAlpha[x - point.x][y - point.y]) {
							return i;
						}
					}
				}
			}
		}

		return -1;
	}


	private boolean isMouseOnDatePanel (int x, int y) {
		if ((y >= datePanelPoint.y && y < (datePanelPoint.y + tileDatePanel.getTileHeight ())) && (x >= datePanelPoint.x && x < (datePanelPoint.x + tileDatePanel.getTileWidth ()))) {
			return !tileDatePanelAlpha[x - datePanelPoint.x][y - datePanelPoint.y];
		}

		return false;
	}


	private boolean isMouseOnInfoPanel (int x, int y) {
		if ((y >= infoPanelPoint.y && y < (infoPanelPoint.y + tileInfoPanel.getTileHeight ())) && (x >= infoPanelPoint.x && x < (infoPanelPoint.x + tileInfoPanel.getTileWidth ()))) {
			return !tileInfoPanelAlpha[x - infoPanelPoint.x][y - infoPanelPoint.y];
		}

		return false;
	}


	private boolean isMouseOnProductionPanel (int x, int y) {
		return ((y >= productionPanelPoint.y && y < (productionPanelPoint.y + PRODUCTION_PANEL_HEIGHT)) && (x >= productionPanelPoint.x && x < (productionPanelPoint.x + PRODUCTION_PANEL_WIDTH)));
	}


	private boolean isMouseOnImagesPanel (int x, int y) {
		return ((y >= ImagesPanel.getPanelPoint ().y && y < (ImagesPanel.getPanelPoint ().y + ImagesPanel.HEIGHT)) && (x >= ImagesPanel.getPanelPoint ().x && x < (ImagesPanel.getPanelPoint ().x + ImagesPanel.WIDTH)));
	}


	private boolean isMouseOnTypingPanel (int x, int y) {
		return ((y >= TypingPanel.getPanelPoint ().y && y < (TypingPanel.getPanelPoint ().y + TypingPanel.HEIGHT)) && (x >= TypingPanel.getPanelPoint ().x && x < (TypingPanel.getPanelPoint ().x + TypingPanel.WIDTH)));
	}


	private boolean isMouseOnMessagesPanel (int x, int y) {
		return ((y >= messagesPanelPoint.y && y < (messagesPanelPoint.y + MESSAGES_PANEL_HEIGHT)) && (x >= messagesPanelPoint.x && x < (messagesPanelPoint.x + MESSAGES_PANEL_WIDTH)));
	}


	private boolean isMouseOnMatsPanel (int x, int y) {
		return ((y >= matsPanelPoint.y && y < (matsPanelPoint.y + MATS_PANEL_HEIGHT)) && (x >= matsPanelPoint.x && x < (matsPanelPoint.x + MATS_PANEL_WIDTH)));
	}


	private boolean isMouseOnProfessionsPanel (int x, int y) {
		return ((y >= professionsPanelPoint.y && y < (professionsPanelPoint.y + PROFESSIONS_PANEL_HEIGHT)) && (x >= professionsPanelPoint.x && x < (professionsPanelPoint.x + PROFESSIONS_PANEL_WIDTH)));
	}


	private boolean isMouseOnPilePanel (int x, int y) {
		return ((y >= pilePanelPoint.y && y < (pilePanelPoint.y + PILE_PANEL_HEIGHT)) && (x >= pilePanelPoint.x && x < (pilePanelPoint.x + PILE_PANEL_WIDTH)));
	}


	private boolean isMouseOnLivingsPanel (int x, int y) {
		return ((y >= livingsPanelPoint.y && y < (livingsPanelPoint.y + LIVINGS_PANEL_HEIGHT)) && (x >= livingsPanelPoint.x && x < (livingsPanelPoint.x + LIVINGS_PANEL_WIDTH)));
	}


	private boolean isMouseOnTradePanel (int x, int y) {
		return ((y >= tradePanelPoint.y && y < (tradePanelPoint.y + TRADE_PANEL_HEIGHT)) && (x >= tradePanelPoint.x && x < (tradePanelPoint.x + TRADE_PANEL_WIDTH)));
	}


	private boolean isMouseOnPrioritiesPanel (int x, int y) {
		return ((y >= prioritiesPanelPoint.y && y < (prioritiesPanelPoint.y + PRIORITIES_PANEL_HEIGHT)) && (x >= prioritiesPanelPoint.x && x < (prioritiesPanelPoint.x + PRIORITIES_PANEL_WIDTH)));
	}


	private boolean isMouseOnMinimap (int x, int y) {
		if (y >= minimapPanelY && y < (minimapPanelY + MINIMAP_PANEL_HEIGHT) && x >= minimapPanelX && x < (minimapPanelX + MINIMAP_PANEL_WIDTH)) {
			if (MiniMapPanel.isMouseOver (x - minimapPanelX, y - minimapPanelY)) {
				return true;
			}

			return !tileMinimapPanelAlpha[x - minimapPanelX][y - minimapPanelY];
		}

		return false;
	}


	private boolean isMouseOnMenuPanel (int x, int y) {
		if (x >= menuPanelPoint.x && x < (menuPanelPoint.x + MENU_PANEL_WIDTH) && y >= menuPanelPoint.y && y < (menuPanelPoint.y + MENU_PANEL_HEIGHT)) {
			return true;
		}

		return false;
	}


	private int isMouseOnMenuItems (int x, int y) {
		if (y >= menuPanelPoint.y && y < (menuPanelPoint.y + MENU_PANEL_HEIGHT) && x >= menuPanelPoint.x && x < (menuPanelPoint.x + MENU_PANEL_WIDTH)) {
			Point point;
			bucle1: for (int y1 = 0; y1 < MENU_PANEL_NUM_ITEMS_Y; y1++) {
				for (int x1 = 0; x1 < MENU_PANEL_NUM_ITEMS_X; x1++) {
					int i = (y1 * MENU_PANEL_NUM_ITEMS_X) + x1;
					if (i >= menuPanelMenu.getItems ().size ()) {
						break bucle1;
					}
					point = menuPanelItemsPosition.get (i);
					if (x >= point.x && x < (point.x + MENU_ITEM_WIDTH) && y >= point.y && y < (point.y + MENU_ITEM_HEIGHT)) {
						if (!tileBottomItemAlpha[x - point.x][y - point.y]) {
							return i;
						}
					}
				}
			}
		}

		return -1;
	}


	/**
	 * Indica si el mouse está en un item (o en los +/-) del panel de producción
	 * 
	 * @param x
	 * @param y
	 * @return Un punto, X es el MOUSE_ID y Y indica la posición del item en el array correspondiente
	 */
	private Point isMouseOnProductionItems (int x, int y) {
		if (y >= productionPanelPoint.y && y < (productionPanelPoint.y + PRODUCTION_PANEL_HEIGHT) && x >= productionPanelPoint.x && x < (productionPanelPoint.x + PRODUCTION_PANEL_WIDTH)) {
			Point point;
			bucle1: for (int y1 = 0; y1 < PRODUCTION_PANEL_NUM_ITEMS_Y; y1++) {
				for (int x1 = 0; x1 < PRODUCTION_PANEL_NUM_ITEMS_X; x1++) {
					int i = (y1 * PRODUCTION_PANEL_NUM_ITEMS_X) + x1;
					if (i >= productionPanelMenu.getItems ().size ()) {
						break bucle1;
					}
					point = productionPanelItemsPosition.get (i);
					if (x >= point.x && x < (point.x + PRODUCTION_PANEL_ITEM_WIDTH) && y >= point.y && y < (point.y + PRODUCTION_PANEL_ITEM_HEIGHT)) {
						if (!tileBottomItemAlpha[x - point.x][y - point.y]) {
							MOUSE_PRODUCTION_PANEL_ITEMS_POINT.y = i;
							return MOUSE_PRODUCTION_PANEL_ITEMS_POINT;
						}
					}
					point = productionPanelItemsPlusRegularPosition.get (i);
					if (point.x != -1) {
						if (x >= point.x && x < (point.x + ICON_WIDTH) && y >= point.y && y < (point.y + ICON_HEIGHT)) {
							if (!tileProductionPanelPlusIconAlpha[x - point.x][y - point.y]) {
								MOUSE_PRODUCTION_PANEL_ITEMS_PLUS_REGULAR_POINT.y = i;
								return MOUSE_PRODUCTION_PANEL_ITEMS_PLUS_REGULAR_POINT;
							}
						}
						point = productionPanelItemsMinusRegularPosition.get (i);
						if (x >= point.x && x < (point.x + ICON_WIDTH) && y >= point.y && y < (point.y + ICON_HEIGHT)) {
							if (!tileProductionPanelMinusIconAlpha[x - point.x][y - point.y]) {
								MOUSE_PRODUCTION_PANEL_ITEMS_MINUS_REGULAR_POINT.y = i;
								return MOUSE_PRODUCTION_PANEL_ITEMS_MINUS_REGULAR_POINT;
							}
						}
						point = productionPanelItemsPlusAutomatedPosition.get (i);
						if (x >= point.x && x < (point.x + ICON_WIDTH) && y >= point.y && y < (point.y + ICON_HEIGHT)) {
							if (!tileProductionPanelPlusIconAlpha[x - point.x][y - point.y]) {
								MOUSE_PRODUCTION_PANEL_ITEMS_PLUS_AUTOMATED_POINT.y = i;
								return MOUSE_PRODUCTION_PANEL_ITEMS_PLUS_AUTOMATED_POINT;
							}
						}
						point = productionPanelItemsMinusAutomatedPosition.get (i);
						if (x >= point.x && x < (point.x + ICON_WIDTH) && y >= point.y && y < (point.y + ICON_HEIGHT)) {
							if (!tileProductionPanelMinusIconAlpha[x - point.x][y - point.y]) {
								MOUSE_PRODUCTION_PANEL_ITEMS_MINUS_AUTOMATED_POINT.y = i;
								return MOUSE_PRODUCTION_PANEL_ITEMS_MINUS_AUTOMATED_POINT;
							}
						}
					}
				}
			}
		}

		return null;
	}


	/**
	 * Indica si el mouse está en un item (o en los up/down) del panel de prioridades
	 * 
	 * @param x
	 * @param y
	 * @return Un punto, X es el MOUSE_ID y Y indica la posición del item en el array correspondiente
	 */
	private Point isMouseOnPrioritiesItems (int x, int y) {
		if (typingPanel != null) {
			return null;
		}

		if (y >= prioritiesPanelPoint.y && y < (prioritiesPanelPoint.y + PRIORITIES_PANEL_HEIGHT) && x >= prioritiesPanelPoint.x && x < (prioritiesPanelPoint.x + PRIORITIES_PANEL_WIDTH)) {
			Point point;
			for (int i = 0; i < PRIORITIES_PANEL_NUM_ITEMS; i++) {
				point = prioritiesPanelItemsPosition.get (i);
				if (x >= point.x && x < (point.x + PRIORITIES_PANEL_ITEM_SIZE) && y >= point.y && y < (point.y + PRIORITIES_PANEL_ITEM_SIZE)) {
					if (!tileBottomItemAlpha[x - point.x][y - point.y]) {
						MOUSE_PRIORITIES_PANEL_ITEMS_POINT.y = i;
						return MOUSE_PRIORITIES_PANEL_ITEMS_POINT;
					}
				}
				point = prioritiesPanelItemsUpPosition.get (i);
				if (x >= point.x && x < (point.x + ICON_WIDTH) && y >= point.y && y < (point.y + ICON_HEIGHT)) {
					if (!tilePrioritiesPanelUpIconAlpha[x - point.x][y - point.y]) {
						MOUSE_PRIORITIES_PANEL_ITEMS_UP_POINT.y = i;
						return MOUSE_PRIORITIES_PANEL_ITEMS_UP_POINT;
					}
				}
				point = prioritiesPanelItemsDownPosition.get (i);
				if (x >= point.x && x < (point.x + ICON_WIDTH) && y >= point.y && y < (point.y + ICON_HEIGHT)) {
					if (!tilePrioritiesPanelDownIconAlpha[x - point.x][y - point.y]) {
						MOUSE_PRIORITIES_PANEL_ITEMS_DOWN_POINT.y = i;
						return MOUSE_PRIORITIES_PANEL_ITEMS_DOWN_POINT;
					}
				}
			}
		}

		return null;
	}


	private Point isMouseOnMessagesButtons (int x, int y) {
		if (typingPanel != null) {
			return null;
		}

		Point point;
		// Close button
		point = messagesPanelClosePoint;
		if (x >= point.x && x < (point.x + tileButtonClose.getTileWidth ()) && y >= point.y && y < (point.y + tileButtonClose.getTileHeight ())) {
			if (!tileButtonCloseAlpha[x - point.x][y - point.y]) {
				return MOUSE_MESSAGES_PANEL_BUTTONS_CLOSE_POINT;
			}
		}

		// Types
		if (isMouseOnAnIcon (x, y, messagePanelIconPoints[MessagesPanel.TYPE_ANNOUNCEMENT], messagePanelTiles[MessagesPanel.TYPE_ANNOUNCEMENT], messagePanelTilesAlpha.get (MessagesPanel.TYPE_ANNOUNCEMENT))) {
			return MOUSE_MESSAGES_PANEL_BUTTONS_ANNOUNCEMENT_POINT;
		}
		if (isMouseOnAnIcon (x, y, messagePanelIconPoints[MessagesPanel.TYPE_COMBAT], messagePanelTiles[MessagesPanel.TYPE_COMBAT], messagePanelTilesAlpha.get (MessagesPanel.TYPE_COMBAT))) {
			return MOUSE_MESSAGES_PANEL_BUTTONS_COMBAT_POINT;
		}
		if (isMouseOnAnIcon (x, y, messagePanelIconPoints[MessagesPanel.TYPE_HEROES], messagePanelTiles[MessagesPanel.TYPE_HEROES], messagePanelTilesAlpha.get (MessagesPanel.TYPE_HEROES))) {
			return MOUSE_MESSAGES_PANEL_BUTTONS_HEROES_POINT;
		}
		if (isMouseOnAnIcon (x, y, messagePanelIconPoints[MessagesPanel.TYPE_SYSTEM], messagePanelTiles[MessagesPanel.TYPE_SYSTEM], messagePanelTilesAlpha.get (MessagesPanel.TYPE_SYSTEM))) {
			return MOUSE_MESSAGES_PANEL_BUTTONS_SYSTEM_POINT;
		}

		// Scrolls
		if (isMouseOnAnIcon (x, y, messagePanelIconScrollUpPoint, tileScrollUp, tileScrollUpButtonAlpha)) {
			return MOUSE_MESSAGES_PANEL_BUTTONS_SCROLL_UP_POINT;
		}
		if (isMouseOnAnIcon (x, y, messagePanelIconScrollDownPoint, tileScrollDown, tileScrollDownButtonAlpha)) {
			return MOUSE_MESSAGES_PANEL_BUTTONS_SCROLL_DOWN_POINT;
		}

		return null;
	}


	/**
	 * Indica si el mouse está en un item (o en los up/down) del panel de trade
	 * 
	 * @param x
	 * @param y
	 * @return Un punto, X es el MOUSE_ID y Y indica la posición del item en el array correspondiente
	 */
	private Point isMouseOnTradeButtons (int x, int y) {
		if (typingPanel != null) {
			return null;
		}

		Point point;
		// Close button
		point = tradePanelClosePoint;
		if (x >= point.x && x < (point.x + tileButtonClose.getTileWidth ()) && y >= point.y && y < (point.y + tileButtonClose.getTileHeight ())) {
			if (!tileButtonCloseAlpha[x - point.x][y - point.y]) {
				return MOUSE_TRADE_PANEL_BUTTONS_CLOSE_POINT;
			}
		}

		if (tradePanel != null && y >= tradePanelPoint.y && y < (tradePanelPoint.y + TRADE_PANEL_HEIGHT) && x >= tradePanelPoint.x && x < (tradePanelPoint.x + TRADE_PANEL_WIDTH)) {
			// Scroll up caravan
			point = tradePanel.getScrollUpCaravanPoint ();
			if (x >= point.x && x < (point.x + tileScrollUp.getTileWidth ()) && y >= point.y && y < (point.y + tileScrollUp.getTileHeight ())) {
				if (!tileScrollUpButtonAlpha[x - point.x][y - point.y]) {
					return MOUSE_TRADE_PANEL_BUTTONS_CARAVAN_UP_POINT;
				}
			}
			// Scroll down caravan
			point = tradePanel.getScrollDownCaravanPoint ();
			if (x >= point.x && x < (point.x + tileScrollDown.getTileWidth ()) && y >= point.y && y < (point.y + tileScrollDown.getTileHeight ())) {
				if (!tileScrollDownButtonAlpha[x - point.x][y - point.y]) {
					return MOUSE_TRADE_PANEL_BUTTONS_CARAVAN_DOWN_POINT;
				}
			}

			// Scroll up caravan to-buy
			point = tradePanel.getScrollUpCaravanToBuyPoint ();
			if (x >= point.x && x < (point.x + tileScrollUp.getTileWidth ()) && y >= point.y && y < (point.y + tileScrollUp.getTileHeight ())) {
				if (!tileScrollUpButtonAlpha[x - point.x][y - point.y]) {
					return MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_CARAVAN_UP_POINT;
				}
			}
			// Scroll down caravan to-buy
			point = tradePanel.getScrollDownCaravanToBuyPoint ();
			if (x >= point.x && x < (point.x + tileScrollDown.getTileWidth ()) && y >= point.y && y < (point.y + tileScrollDown.getTileHeight ())) {
				if (!tileScrollDownButtonAlpha[x - point.x][y - point.y]) {
					return MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_CARAVAN_DOWN_POINT;
				}
			}

			// Scroll up town to-sell
			point = tradePanel.getScrollUpTownToSellPoint ();
			if (x >= point.x && x < (point.x + tileScrollUp.getTileWidth ()) && y >= point.y && y < (point.y + tileScrollUp.getTileHeight ())) {
				if (!tileScrollUpButtonAlpha[x - point.x][y - point.y]) {
					return MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_TOWN_UP_POINT;
				}
			}
			// Scroll down town to-sell
			point = tradePanel.getScrollDownTownToSellPoint ();
			if (x >= point.x && x < (point.x + tileScrollDown.getTileWidth ()) && y >= point.y && y < (point.y + tileScrollDown.getTileHeight ())) {
				if (!tileScrollDownButtonAlpha[x - point.x][y - point.y]) {
					return MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_TOWN_DOWN_POINT;
				}
			}

			// Scroll up town
			point = tradePanel.getScrollUpTownPoint ();
			if (x >= point.x && x < (point.x + tileScrollUp.getTileWidth ()) && y >= point.y && y < (point.y + tileScrollUp.getTileHeight ())) {
				if (!tileScrollUpButtonAlpha[x - point.x][y - point.y]) {
					return MOUSE_TRADE_PANEL_BUTTONS_TOWN_UP_POINT;
				}
			}
			// Scroll down town
			point = tradePanel.getScrollDownTownPoint ();
			if (x >= point.x && x < (point.x + tileScrollDown.getTileWidth ()) && y >= point.y && y < (point.y + tileScrollDown.getTileHeight ())) {
				if (!tileScrollDownButtonAlpha[x - point.x][y - point.y]) {
					return MOUSE_TRADE_PANEL_BUTTONS_TOWN_DOWN_POINT;
				}
			}

			// Confirm
			point = tradePanel.getConfirmPoint ();
			if (x >= point.x && x < (point.x + TradePanel.tileTradeConfirm.getTileWidth ()) && y >= point.y && y < (point.y + TradePanel.tileTradeConfirm.getTileHeight ())) {
				if (!TradePanel.tileTradeConfirmAlpha[x - point.x][y - point.y]) {
					return MOUSE_TRADE_PANEL_BUTTONS_CONFIRM_POINT;
				}
			}

			// Caravan buttons
			for (int i = 0; i < tradePanel.getAlButtonPointsCaravan ().size (); i++) {
				point = tradePanel.getAlButtonPointsCaravan ().get (i);
				if (x >= point.x && x < (point.x + TRADE_PANEL_BUTTON_WIDTH) && y >= point.y && y < (point.y + TRADE_PANEL_BUTTON_HEIGHT)) {
					if (!TradePanel.tileTradeButtonAlpha[x - point.x][y - point.y]) {
						MOUSE_TRADE_PANEL_BUTTONS_CARAVAN_POINT.y = i;
						return MOUSE_TRADE_PANEL_BUTTONS_CARAVAN_POINT;
					}
				}
			}

			// Caravan buttons to-buy
			for (int i = 0; i < tradePanel.getAlButtonPointsCaravanToBuy ().size (); i++) {
				point = tradePanel.getAlButtonPointsCaravanToBuy ().get (i);
				if (x >= point.x && x < (point.x + TRADE_PANEL_BUTTON_WIDTH) && y >= point.y && y < (point.y + TRADE_PANEL_BUTTON_HEIGHT)) {
					if (!TradePanel.tileTradeButtonAlpha[x - point.x][y - point.y]) {
						MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_CARAVAN_POINT.y = i;
						return MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_CARAVAN_POINT;
					}
				}
			}

			// Town buttons to-sell
			for (int i = 0; i < tradePanel.getAlButtonPointsTownToSell ().size (); i++) {
				point = tradePanel.getAlButtonPointsTownToSell ().get (i);
				if (x >= point.x && x < (point.x + TRADE_PANEL_BUTTON_WIDTH) && y >= point.y && y < (point.y + TRADE_PANEL_BUTTON_HEIGHT)) {
					if (!TradePanel.tileTradeButtonAlpha[x - point.x][y - point.y]) {
						MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_TOWN_POINT.y = i;
						return MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_TOWN_POINT;
					}
				}
			}

			// Town buttons
			for (int i = 0; i < tradePanel.getAlButtonPointsTown ().size (); i++) {
				point = tradePanel.getAlButtonPointsTown ().get (i);
				if (x >= point.x && x < (point.x + TRADE_PANEL_BUTTON_WIDTH) && y >= point.y && y < (point.y + TRADE_PANEL_BUTTON_HEIGHT)) {
					if (!TradePanel.tileTradeButtonAlpha[x - point.x][y - point.y]) {
						MOUSE_TRADE_PANEL_BUTTONS_TOWN_POINT.y = i;
						return MOUSE_TRADE_PANEL_BUTTONS_TOWN_POINT;
					}
				}
			}

			// Buy icon
			point = tradePanel.getBuyIconPoint ();
			if (x >= point.x && x < (point.x + TradePanel.tileTradeBuy.getTileWidth ()) && y >= point.y && y < (point.y + TradePanel.tileTradeBuy.getTileHeight ())) {
				return MOUSE_TRADE_PANEL_ICON_BUY_POINT;
			}

			// Sell icon
			point = tradePanel.getSellIconPoint ();
			if (x >= point.x && x < (point.x + TradePanel.tileTradeSell.getTileWidth ()) && y >= point.y && y < (point.y + TradePanel.tileTradeSell.getTileHeight ())) {
				return MOUSE_TRADE_PANEL_ICON_SELL_POINT;
			}
		}

		return null;
	}


	/**
	 * Indica si el mouse está en un item (o en los up/down) del panel de pila
	 * 
	 * @param x
	 * @param y
	 * @return Un punto, X es el MOUSE_ID y Y indica la posición del item en el array correspondiente
	 */
	private Point isMouseOnPileButtons (int x, int y) {
		if (typingPanel != null) {
			return null;
		}

		Point point;
		// Close button
		point = pilePanelClosePoint;
		if (x >= point.x && x < (point.x + tileButtonClose.getTileWidth ()) && y >= point.y && y < (point.y + tileButtonClose.getTileHeight ())) {
			if (!tileButtonCloseAlpha[x - point.x][y - point.y]) {
				return MOUSE_PILE_PANEL_BUTTONS_CLOSE_POINT;
			}
		}

		// Scrolls
		point = pilePanelIconScrollUpPoint;
		if (x >= point.x && x < (point.x + tileScrollUp.getTileWidth ()) && y >= point.y && y < (point.y + tileScrollUp.getTileHeight ())) {
			if (!tileScrollUpButtonAlpha[x - point.x][y - point.y]) {
				return MOUSE_PILE_PANEL_BUTTONS_SCROLL_UP_POINT;
			}
		}
		point = pilePanelIconScrollDownPoint;
		if (x >= point.x && x < (point.x + tileScrollDown.getTileWidth ()) && y >= point.y && y < (point.y + tileScrollDown.getTileHeight ())) {
			if (!tileScrollDownButtonAlpha[x - point.x][y - point.y]) {
				return MOUSE_PILE_PANEL_BUTTONS_SCROLL_DOWN_POINT;
			}
		}

		// Configuration buttons
		point = pilePanelIconConfigCopyPoint;
		if (x >= point.x && x < (point.x + tileConfigCopy.getTileWidth ()) && y >= point.y && y < (point.y + tileConfigCopy.getTileHeight ())) {
			if (!tileConfigCopyButtonAlpha[x - point.x][y - point.y]) {
				return MOUSE_PILE_PANEL_BUTTONS_CONFIG_COPY_POINT;
			}
		}
		point = pilePanelIconConfigLockPoint;
		if (x >= point.x && x < (point.x + tileConfigLock.getTileWidth ()) && y >= point.y && y < (point.y + tileConfigLock.getTileHeight ())) {
			if (!tileConfigLockButtonAlpha[x - point.x][y - point.y]) {
				return MOUSE_PILE_PANEL_BUTTONS_CONFIG_LOCK_POINT;
			}
		}
		point = pilePanelIconConfigLockAllPoint;
		if (x >= point.x && x < (point.x + tileConfigLockAll.getTileWidth ()) && y >= point.y && y < (point.y + tileConfigLockAll.getTileHeight ())) {
			if (!tileConfigLockAllButtonAlpha[x - point.x][y - point.y]) {
				return MOUSE_PILE_PANEL_BUTTONS_CONFIG_LOCK_ALL_POINT;
			}
		}
		point = pilePanelIconConfigUnlockAllPoint;
		if (x >= point.x && x < (point.x + tileConfigUnlockAll.getTileWidth ()) && y >= point.y && y < (point.y + tileConfigUnlockAll.getTileHeight ())) {
			if (!tileConfigUnlockAllButtonAlpha[x - point.x][y - point.y]) {
				return MOUSE_PILE_PANEL_BUTTONS_CONFIG_UNLOCK_ALL_POINT;
			}
		}
		
		// Items
		if (menuPile != null) {
			int iFirstIndex = pilePanelPageIndex * PILE_PANEL_MAX_ITEMS_PER_PAGE;
			int iMin = Math.min (menuPile.getItems ().size () - iFirstIndex, PILE_PANEL_MAX_ITEMS_PER_PAGE);
			for (int i = 0; i < iMin; i++) {
				point = pilePanelItemPoints[i];
				if (x >= point.x && x < (point.x + tileBottomItem.getTileWidth ()) && y >= point.y && y < (point.y + tileBottomItem.getTileHeight ())) {
					if (!tileBottomItemAlpha[x - point.x][y - point.y]) {
						MOUSE_PILE_PANEL_BUTTONS_ITEMS_POINT.y = i + iFirstIndex;
						return MOUSE_PILE_PANEL_BUTTONS_ITEMS_POINT;
					}
				}
			}
		}

		return null;
	}


	/**
	 * Indica si el mouse está en un item (o en los up/down) del panel de profesiones
	 * 
	 * @param x
	 * @param y
	 * @return Un punto, X es el MOUSE_ID y Y indica la posición del item en el array correspondiente
	 */
	private Point isMouseOnProfessionsButtons (int x, int y) {
		if (typingPanel != null) {
			return null;
		}

		Point point;
		// Close button
		point = professionsPanelClosePoint;
		if (x >= point.x && x < (point.x + tileButtonClose.getTileWidth ()) && y >= point.y && y < (point.y + tileButtonClose.getTileHeight ())) {
			if (!tileButtonCloseAlpha[x - point.x][y - point.y]) {
				return MOUSE_PROFESSIONS_PANEL_BUTTONS_CLOSE_POINT;
			}
		}

		// Scrolls
		point = professionsPanelIconScrollUpPoint;
		if (x >= point.x && x < (point.x + tileScrollUp.getTileWidth ()) && y >= point.y && y < (point.y + tileScrollUp.getTileHeight ())) {
			if (!tileScrollUpButtonAlpha[x - point.x][y - point.y]) {
				return MOUSE_PROFESSIONS_PANEL_BUTTONS_SCROLL_UP_POINT;
			}
		}
		point = professionsPanelIconScrollDownPoint;
		if (x >= point.x && x < (point.x + tileScrollDown.getTileWidth ()) && y >= point.y && y < (point.y + tileScrollDown.getTileHeight ())) {
			if (!tileScrollDownButtonAlpha[x - point.x][y - point.y]) {
				return MOUSE_PROFESSIONS_PANEL_BUTTONS_SCROLL_DOWN_POINT;
			}
		}

		// Items
		if (menuProfessions != null) {
			int iFirstIndex = professionsPanelPageIndex * PROFESSIONS_PANEL_MAX_ITEMS_PER_PAGE;
			int iMin = Math.min (menuProfessions.getItems ().size () - iFirstIndex, PROFESSIONS_PANEL_MAX_ITEMS_PER_PAGE);
			for (int i = 0; i < iMin; i++) {
				point = professionsPanelItemPoints[i];
				if (x >= point.x && x < (point.x + tileBottomItem.getTileWidth ()) && y >= point.y && y < (point.y + tileBottomItem.getTileHeight ())) {
					if (!tileBottomItemAlpha[x - point.x][y - point.y]) {
						MOUSE_PROFESSIONS_PANEL_BUTTONS_ITEMS_POINT.y = i + iFirstIndex;
						return MOUSE_PROFESSIONS_PANEL_BUTTONS_ITEMS_POINT;
					}
				}
			}
		}

		return null;
	}


	/**
	 * Indica si el mouse está en un item (o en los up/down) del panel de trade
	 * 
	 * @param x
	 * @param y
	 * @return Un punto, X es el MOUSE_ID y Y indica la posición del item en el array correspondiente
	 */
	private Point isMouseOnMatsButtons (int x, int y) {
		if (typingPanel != null) {
			return null;
		}

		Point point;
		// Close button
		point = matsPanelClosePoint;
		if (x >= point.x && x < (point.x + tileButtonClose.getTileWidth ()) && y >= point.y && y < (point.y + tileButtonClose.getTileHeight ())) {
			if (!tileButtonCloseAlpha[x - point.x][y - point.y]) {
				return MOUSE_MATS_PANEL_BUTTONS_CLOSE_POINT;
			}
		}

		// Scrolls
		point = matsPanelIconScrollUpPoint;
		if (x >= point.x && x < (point.x + tileScrollUp.getTileWidth ()) && y >= point.y && y < (point.y + tileScrollUp.getTileHeight ())) {
			if (!tileScrollUpButtonAlpha[x - point.x][y - point.y]) {
				return MOUSE_MATS_PANEL_BUTTONS_SCROLL_UP_POINT;
			}
		}
		point = matsPanelIconScrollDownPoint;
		if (x >= point.x && x < (point.x + tileScrollDown.getTileWidth ()) && y >= point.y && y < (point.y + tileScrollDown.getTileHeight ())) {
			if (!tileScrollDownButtonAlpha[x - point.x][y - point.y]) {
				return MOUSE_MATS_PANEL_BUTTONS_SCROLL_DOWN_POINT;
			}
		}

		// Groups
		for (int i = 0; i < MatsPanelData.numGroups; i++) {
			point = matsPanelIconPoints[i];
			if (x >= point.x && x < (point.x + tileBottomItem.getTileWidth ()) && y >= point.y && y < (point.y + tileBottomItem.getTileHeight ())) {
				if (!tileBottomItemAlpha[x - point.x][y - point.y]) {
					MOUSE_MATS_PANEL_BUTTONS_GROUPS_POINT.y = i;
					return MOUSE_MATS_PANEL_BUTTONS_GROUPS_POINT;
				}
			}
		}

		// Items
		if (getMatsPanelActive () != -1) {
			int iFirstIndex = matsIndexPages[getMatsPanelActive ()] * MATS_PANEL_MAX_ITEMS_PER_PAGE;
			int iMin = Math.min (MatsPanelData.tileGroups.get (getMatsPanelActive ()).size () - iFirstIndex, MATS_PANEL_MAX_ITEMS_PER_PAGE);
			for (int i = 0; i < iMin; i++) {
				point = matsPanelItemPoints[i];
				if (x >= point.x && x < (point.x + tileBottomItem.getTileWidth ()) && y >= point.y && y < (point.y + tileBottomItem.getTileHeight ())) {
					if (!tileBottomItemAlpha[x - point.x][y - point.y]) {
						MOUSE_MATS_PANEL_BUTTONS_ITEMS_POINT.y = i + iFirstIndex;
						return MOUSE_MATS_PANEL_BUTTONS_ITEMS_POINT;
					}
				}
			}
		}

		return null;
	}


	private Point isMouseOnLivingsButtons (int x, int y) {
		if (typingPanel != null) {
			return null;
		}

		Point point;
		// Close button
		point = livingsPanelClosePoint;
		if (x >= point.x && x < (point.x + tileButtonClose.getTileWidth ()) && y >= point.y && y < (point.y + tileButtonClose.getTileHeight ())) {
			if (!tileButtonCloseAlpha[x - point.x][y - point.y]) {
				return MOUSE_LIVINGS_PANEL_BUTTONS_CLOSE_POINT;
			}
		}

		// Scrolls
		point = livingsPanelIconScrollUpPoint;
		if (x >= point.x && x < (point.x + tileScrollUp.getTileWidth ()) && y >= point.y && y < (point.y + tileScrollUp.getTileHeight ())) {
			if (!tileScrollUpButtonAlpha[x - point.x][y - point.y]) {
				return MOUSE_LIVINGS_PANEL_BUTTONS_SCROLL_UP_POINT;
			}
		}
		point = livingsPanelIconScrollDownPoint;
		if (x >= point.x && x < (point.x + tileScrollDown.getTileWidth ()) && y >= point.y && y < (point.y + tileScrollDown.getTileHeight ())) {
			if (!tileScrollDownButtonAlpha[x - point.x][y - point.y]) {
				return MOUSE_LIVINGS_PANEL_BUTTONS_SCROLL_DOWN_POINT;
			}
		}

		if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_SOLDIERS) {
			// Groups subpanel
			point = livingsGroupPanelFirstIconPoint;
			if (x >= point.x && x < (point.x + tileLivingsNoGroup.getTileWidth ()) && y >= point.y && y < (point.y + tileLivingsNoGroup.getTileHeight ())) {
				if (!tileLivingsNoGroupAlpha[x - point.x][y - point.y]) {
					return MOUSE_LIVINGS_PANEL_SGROUP_NOGROUP_POINT;
				}
			}

			// Los 10 grupos
			for (int i = 0; i < SoldierGroups.MAX_GROUPS; i++) {
				point = new Point (livingsGroupPanelFirstIconPoint.x, livingsGroupPanelFirstIconPoint.y + (i + 1) * livingsGroupPanelIconsSeparation);
				if (x >= point.x && x < (point.x + tileLivingsGroup.getTileWidth ()) && y >= point.y && y < (point.y + tileLivingsGroup.getTileHeight ())) {
					if (!tileLivingsGroupAlpha[x - point.x][y - point.y]) {
						MOUSE_LIVINGS_PANEL_SGROUP_GROUP_POINT.y = i;
						return MOUSE_LIVINGS_PANEL_SGROUP_GROUP_POINT;
					}
				}
			}
		} else if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_CITIZENS) {
			// Groups subpanel
			point = livingsGroupPanelFirstIconPoint;
			if (x >= point.x && x < (point.x + tileLivingsNoJobGroup.getTileWidth ()) && y >= point.y && y < (point.y + tileLivingsNoJobGroup.getTileHeight ())) {
				if (!tileLivingsNoJobGroupAlpha[x - point.x][y - point.y]) {
					return MOUSE_LIVINGS_PANEL_CGROUP_NOGROUP_POINT;
				}
			}

			// Los 10 grupos
			for (int i = 0; i < SoldierGroups.MAX_GROUPS; i++) {
				point = new Point (livingsGroupPanelFirstIconPoint.x, livingsGroupPanelFirstIconPoint.y + (i + 1) * livingsGroupPanelIconsSeparation);
				if (x >= point.x && x < (point.x + tileLivingsJobGroup.getTileWidth ()) && y >= point.y && y < (point.y + tileLivingsJobGroup.getTileHeight ())) {
					if (!tileLivingsJobGroupAlpha[x - point.x][y - point.y]) {
						MOUSE_LIVINGS_PANEL_CGROUP_GROUP_POINT.y = i;
						return MOUSE_LIVINGS_PANEL_CGROUP_GROUP_POINT;
					}
				}
			}
		}

		if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_CITIZENS) {
			// Single group
			if (livingsPanelCitizensGroupActive != -1) {
				point = livingsSingleGroupRenamePoint;
				if (x >= point.x && x < (point.x + tileLivingsSingleJobGroupRename.getTileWidth ()) && y >= point.y && y < (point.y + tileLivingsSingleJobGroupRename.getTileHeight ())) {
					if (!tileLivingsSingleJobGroupRenameAlpha[x - point.x][y - point.y]) {
						return MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_RENAME_POINT;
					}
				}
				point = livingsSingleGroupAutoequipPoint;
				if (x >= point.x && x < (point.x + tileLivingsRowAutoequip.getTileWidth ()) && y >= point.y && y < (point.y + tileLivingsRowAutoequip.getTileHeight ())) {
					if (!tileLivingsRowAutoequipAlpha[x - point.x][y - point.y]) {
						return MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_AUTOEQUIP_POINT;
					}
				}
				point = livingsSingleGroupChangeJobsPoint;
				if (x >= point.x && x < (point.x + tileLivingsSingleGroupChangeJobs.getTileWidth ()) && y >= point.y && y < (point.y + tileLivingsSingleGroupChangeJobs.getTileHeight ())) {
					if (!tileLivingsSingleGroupChangeJobsAlpha[x - point.x][y - point.y]) {
						return MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_CHANGE_JOBS_POINT;
					}
				}
				point = livingsSingleGroupDisbandPoint;
				if (x >= point.x && x < (point.x + tileLivingsSingleJobGroupDisband.getTileWidth ()) && y >= point.y && y < (point.y + tileLivingsSingleJobGroupDisband.getTileHeight ())) {
					if (!tileLivingsSingleJobGroupDisbandAlpha[x - point.x][y - point.y]) {
						return MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_DISBAND_POINT;
					}
				}
			} else {
				// Restrict
				point = livingsPanelIconRestrictUpPoint;
				if (x >= point.x && x < (point.x + tileIconLevelUp.getTileWidth ()) && y >= point.y && y < (point.y + tileIconLevelUp.getTileHeight ())) {
					if (!tileIconLevelUpAlpha[x - point.x][y - point.y]) {
						return MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_UP_POINT;
					}
				}
				point = livingsPanelIconRestrictDownPoint;
				if (x >= point.x && x < (point.x + tileIconLevelDown.getTileWidth ()) && y >= point.y && y < (point.y + tileIconLevelDown.getTileHeight ())) {
					if (!tileIconLevelDownAlpha[x - point.x][y - point.y]) {
						return MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_DOWN_POINT;
					}
				}
			}
		} else if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_SOLDIERS) {
			// Single group
			if (livingsPanelSoldiersGroupActive != -1) {
				point = livingsSingleGroupRenamePoint;
				if (x >= point.x && x < (point.x + tileLivingsSingleGroupRename.getTileWidth ()) && y >= point.y && y < (point.y + tileLivingsSingleGroupRename.getTileHeight ())) {
					if (!tileLivingsSingleGroupRenameAlpha[x - point.x][y - point.y]) {
						return MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_RENAME_POINT;
					}
				}
				point = livingsSingleGroupGuardPoint;
				if (x >= point.x && x < (point.x + tileLivingsSingleGroupGuard.getTileWidth ()) && y >= point.y && y < (point.y + tileLivingsSingleGroupGuard.getTileHeight ())) {
					if (!tileLivingsSingleGroupGuardAlpha[x - point.x][y - point.y]) {
						return MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_GUARD_POINT;
					}
				}
				point = livingsSingleGroupPatrolPoint;
				if (x >= point.x && x < (point.x + tileLivingsSingleGroupPatrol.getTileWidth ()) && y >= point.y && y < (point.y + tileLivingsSingleGroupPatrol.getTileHeight ())) {
					if (!tileLivingsSingleGroupPatrolAlpha[x - point.x][y - point.y]) {
						return MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_PATROL_POINT;
					}
				}
				point = livingsSingleGroupBossPoint;
				if (x >= point.x && x < (point.x + tileLivingsSingleGroupBoss.getTileWidth ()) && y >= point.y && y < (point.y + tileLivingsSingleGroupBoss.getTileHeight ())) {
					if (!tileLivingsSingleGroupBossAlpha[x - point.x][y - point.y]) {
						return MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_BOSS_POINT;
					}
				}
				point = livingsSingleGroupAutoequipPoint;
				if (x >= point.x && x < (point.x + tileLivingsRowAutoequip.getTileWidth ()) && y >= point.y && y < (point.y + tileLivingsRowAutoequip.getTileHeight ())) {
					if (!tileLivingsRowAutoequipAlpha[x - point.x][y - point.y]) {
						return MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_AUTOEQUIP_POINT;
					}
				}
				point = livingsSingleGroupDisbandPoint;
				if (x >= point.x && x < (point.x + tileLivingsSingleGroupDisband.getTileWidth ()) && y >= point.y && y < (point.y + tileLivingsSingleGroupDisband.getTileHeight ())) {
					if (!tileLivingsSingleGroupDisbandAlpha[x - point.x][y - point.y]) {
						return MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_DISBAND_POINT;
					}
				}
			}
		} else {
			// Heroes
			// Restrict
			point = livingsPanelIconRestrictUpPoint;
			if (x >= point.x && x < (point.x + tileIconLevelUp.getTileWidth ()) && y >= point.y && y < (point.y + tileIconLevelUp.getTileHeight ())) {
				if (!tileIconLevelUpAlpha[x - point.x][y - point.y]) {
					return MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_UP_POINT;
				}
			}
			point = livingsPanelIconRestrictDownPoint;
			if (x >= point.x && x < (point.x + tileIconLevelDown.getTileWidth ()) && y >= point.y && y < (point.y + tileIconLevelDown.getTileHeight ())) {
				if (!tileIconLevelDownAlpha[x - point.x][y - point.y]) {
					return MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_DOWN_POINT;
				}
			}
		}

		// Living, equipment, civ/soldier converts, ...
		ArrayList<Integer> alLivings = getLivings ();
		int iNumLivings;
		if (alLivings != null) {
			iNumLivings = alLivings.size ();
		} else {
			iNumLivings = 0;
		}

		if (iNumLivings > 0) {
			int iNumPages = (iNumLivings % LIVINGS_PANEL_MAX_ROWS == 0) ? iNumLivings / LIVINGS_PANEL_MAX_ROWS : (iNumLivings / LIVINGS_PANEL_MAX_ROWS) + 1;
			int iIndexPage;
			boolean bNoGroupsPanel = !checkGroupsPanelEnabled (getLivingsPanelActive ());
			if (bNoGroupsPanel) {
				iIndexPage = livingsDataIndexPages[getLivingsPanelActive ()];
			} else {
				if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_CITIZENS) {
					iIndexPage = livingsDataIndexPagesCitizenGroups[livingsPanelCitizensGroupActive];
				} else {
					iIndexPage = livingsDataIndexPagesSoldierGroups[livingsPanelSoldiersGroupActive];
				}
			}
			if (iIndexPage > iNumPages) {
				if (bNoGroupsPanel) {
					livingsDataIndexPages[getLivingsPanelActive ()] = iNumPages;
				} else {
					if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_CITIZENS) {
						livingsDataIndexPagesCitizenGroups[livingsPanelCitizensGroupActive] = iNumPages;
					} else {
						livingsDataIndexPagesSoldierGroups[livingsPanelSoldiersGroupActive] = iNumPages;
					}
				}
				iIndexPage = iNumPages;
			} else if (iIndexPage < 1) {
				if (bNoGroupsPanel) {
					livingsDataIndexPages[getLivingsPanelActive ()] = 1;
				} else {
					if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_CITIZENS) {
						livingsDataIndexPagesCitizenGroups[livingsPanelCitizensGroupActive] = 1;
					} else {
						livingsDataIndexPagesSoldierGroups[livingsPanelSoldiersGroupActive] = 1;
					}
				}
				iIndexPage = 1;
			}

			int iMaxRows = Math.min (iNumLivings - ((iIndexPage - 1) * LIVINGS_PANEL_MAX_ROWS), livingsPanelRowPoints.length);
			iMaxRows = Math.min (iMaxRows, LIVINGS_PANEL_MAX_ROWS);

			for (int i = 0; i < iMaxRows; i++) {
				// Living
				point = livingsPanelRowPoints[i];
				if (x >= point.x && x < (point.x + tileBottomItem.getTileWidth ()) && y >= point.y && y < (point.y + tileBottomItem.getTileHeight ())) {
					if (!tileBottomItemAlpha[x - point.x][y - point.y]) {
						MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_POINT.y = i;
						return MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_POINT;
					}
				}

				// Equipment
				point = livingsPanelRowHeadPoints[i];
				if (x >= point.x && x < (point.x + tileBottomItem.getTileWidth ()) && y >= point.y && y < (point.y + tileBottomItem.getTileHeight ())) {
					if (!tileBottomItemAlpha[x - point.x][y - point.y]) {
						MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_HEAD_POINT.y = i;
						return MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_HEAD_POINT;
					}
				}
				point = livingsPanelRowBodyPoints[i];
				if (x >= point.x && x < (point.x + tileBottomItem.getTileWidth ()) && y >= point.y && y < (point.y + tileBottomItem.getTileHeight ())) {
					if (!tileBottomItemAlpha[x - point.x][y - point.y]) {
						MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_BODY_POINT.y = i;
						return MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_BODY_POINT;
					}
				}
				point = livingsPanelRowLegsPoints[i];
				if (x >= point.x && x < (point.x + tileBottomItem.getTileWidth ()) && y >= point.y && y < (point.y + tileBottomItem.getTileHeight ())) {
					if (!tileBottomItemAlpha[x - point.x][y - point.y]) {
						MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_LEGS_POINT.y = i;
						return MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_LEGS_POINT;
					}
				}
				point = livingsPanelRowFeetPoints[i];
				if (x >= point.x && x < (point.x + tileBottomItem.getTileWidth ()) && y >= point.y && y < (point.y + tileBottomItem.getTileHeight ())) {
					if (!tileBottomItemAlpha[x - point.x][y - point.y]) {
						MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_FEET_POINT.y = i;
						return MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_FEET_POINT;
					}
				}
				point = livingsPanelRowWeaponPoints[i];
				if (x >= point.x && x < (point.x + tileBottomItem.getTileWidth ()) && y >= point.y && y < (point.y + tileBottomItem.getTileHeight ())) {
					if (!tileBottomItemAlpha[x - point.x][y - point.y]) {
						MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_WEAPON_POINT.y = i;
						return MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_WEAPON_POINT;
					}
				}

				// Autoequip
				if (getLivingsPanelActive () != LIVINGS_PANEL_TYPE_HEROES) {
					point = livingsPanelRowAutoequipPoints[i];
					if (x >= point.x && x < (point.x + tileLivingsRowAutoequip.getTileWidth ()) && y >= point.y && y < (point.y + tileLivingsRowAutoequip.getTileHeight ())) {
						if (!tileLivingsRowAutoequipAlpha[x - point.x][y - point.y]) {
							MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_AUTOEQUIP_POINT.y = i;
							return MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_AUTOEQUIP_POINT;
						}
					}
				}

				// Civ/soldier converts
				if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_CITIZENS) {
					point = livingsPanelRowConvertCivilianSoldierPoints[i];
					if (x >= point.x && x < (point.x + tileLivingsRowConvertSoldier.getTileWidth ()) && y >= point.y && y < (point.y + tileLivingsRowConvertSoldier.getTileHeight ())) {
						if (!tileLivingsRowConvertSoldierAlpha[x - point.x][y - point.y]) {
							MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_POINT.y = i;
							return MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_POINT;
						}
					}
					if (livingsPanelCitizensGroupActive == -1) {
						point = livingsPanelRowProfessionPoints[i];
						if (x >= point.x && x < (point.x + tileLivingsRowProfession.getTileWidth ()) && y >= point.y && y < (point.y + tileLivingsRowProfession.getTileHeight ())) {
							if (!tileLivingsRowProfessionAlpha[x - point.x][y - point.y]) {
								MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_PROFESSIONS_POINT.y = i;
								return MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_PROFESSIONS_POINT;
							}
						}
					}
					point = livingsPanelRowJobsGroupsPoints[i];
					if (x >= point.x && x < (point.x + tileLivingsRowJobsGroups.getTileWidth ()) && y >= point.y && y < (point.y + tileLivingsRowJobsGroups.getTileHeight ())) {
						if (!tileLivingsRowJobsGroupsAlpha[x - point.x][y - point.y]) {
							MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_JOBS_GROUPS_ADDREMOVE_POINT.y = i;
							return MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_JOBS_GROUPS_ADDREMOVE_POINT;
						}
					}
				} else if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_SOLDIERS) {
					point = livingsPanelRowConvertCivilianSoldierPoints[i];
					if (x >= point.x && x < (point.x + tileLivingsRowConvertCivilian.getTileWidth ()) && y >= point.y && y < (point.y + tileLivingsRowConvertCivilian.getTileHeight ())) {
						if (!tileLivingsRowConvertCivilianAlpha[x - point.x][y - point.y]) {
							MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_CIVILIAN_POINT.y = i;
							return MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_CIVILIAN_POINT;
						}
					}

					if (livingsPanelSoldiersGroupActive == -1) {
						point = livingsPanelRowConvertSoldierGuardPoints[i];
						if (x >= point.x && x < (point.x + tileLivingsRowConvertSoldierGuard.getTileWidth ()) && y >= point.y && y < (point.y + tileLivingsRowConvertSoldierGuard.getTileHeight ())) {
							if (!tileLivingsRowConvertSoldierGuardAlpha[x - point.x][y - point.y]) {
								MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_GUARD_POINT.y = i;
								return MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_GUARD_POINT;
							}
						}
						point = livingsPanelRowConvertSoldierPatrolPoints[i];
						if (x >= point.x && x < (point.x + tileLivingsRowConvertSoldierPatrol.getTileWidth ()) && y >= point.y && y < (point.y + tileLivingsRowConvertSoldierPatrol.getTileHeight ())) {
							if (!tileLivingsRowConvertSoldierPatrolAlpha[x - point.x][y - point.y]) {
								MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_PATROL_POINT.y = i;
								return MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_PATROL_POINT;
							}
						}
						point = livingsPanelRowConvertSoldierBossPoints[i];
						if (x >= point.x && x < (point.x + tileLivingsRowConvertSoldierBoss.getTileWidth ()) && y >= point.y && y < (point.y + tileLivingsRowConvertSoldierBoss.getTileHeight ())) {
							if (!tileLivingsRowConvertSoldierBossAlpha[x - point.x][y - point.y]) {
								MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_BOSS_POINT.y = i;
								return MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_BOSS_POINT;
							}
						}
						point = livingsPanelRowGroupPoints[i];
						if (x >= point.x && x < (point.x + tileLivingsRowGroupAdd.getTileWidth ()) && y >= point.y && y < (point.y + tileLivingsRowGroupAdd.getTileHeight ())) {
							if (!tileLivingsRowGroupAddAlpha[x - point.x][y - point.y]) {
								MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_SGROUP_ADD_POINT.y = i;
								return MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_SGROUP_ADD_POINT;
							}
						}
					} else {
						point = livingsPanelRowGroupPoints[i];
						if (x >= point.x && x < (point.x + tileLivingsRowGroupRemove.getTileWidth ()) && y >= point.y && y < (point.y + tileLivingsRowGroupRemove.getTileHeight ())) {
							if (!tileLivingsRowGroupRemoveAlpha[x - point.x][y - point.y]) {
								MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_SGROUP_REMOVE_POINT.y = i;
								return MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_SGROUP_REMOVE_POINT;
							}
						}
					}
				}
			}
		}

		return null;
	}


	/**
	 * Retorna una lista de IDs de livings a partir de lo que esté mostrando el living panel
	 * 
	 * @return
	 */
	private ArrayList<Integer> getLivings () {
		if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_CITIZENS) {
			if (livingsPanelCitizensGroupActive == -1) {
				// Citizens sin grupo
				return Game.getWorld ().getCitizenGroups ().getCitizensWithoutGroup ();
			} else {
				// Está mostrando un grupo, miramos los miembros que tiene
				if (livingsPanelCitizensGroupActive >= 0 && livingsPanelCitizensGroupActive < CitizenGroups.MAX_GROUPS) {
					return Game.getWorld ().getCitizenGroups ().getGroup (livingsPanelCitizensGroupActive).getLivingIDs ();
				}
			}

			return World.getCitizenIDs ();
		} else if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_SOLDIERS) {
			if (livingsPanelSoldiersGroupActive == -1) {
				// Soldiers sin grupo
				return Game.getWorld ().getSoldierGroups ().getSoldiersWithoutGroup ();
			} else {
				// Está mostrando un grupo, miramos los miembros que tiene
				if (livingsPanelSoldiersGroupActive >= 0 && livingsPanelSoldiersGroupActive < SoldierGroups.MAX_GROUPS) {
					return Game.getWorld ().getSoldierGroups ().getGroup (livingsPanelSoldiersGroupActive).getLivingIDs ();
				}
			}
		} else if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_HEROES) {
			return World.getHeroIDs ();
		}

		return null;
	}


	/**
	 * Retorna la primera posición de la página que esté mostrando el living panel
	 * 
	 * @return
	 */
	private int getLivingsIndex () {
		if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_CITIZENS) {
			if (livingsPanelCitizensGroupActive == -1) {
				// Todos los citizens
				return (livingsDataIndexPages[LIVINGS_PANEL_TYPE_CITIZENS] - 1) * LIVINGS_PANEL_MAX_ROWS;
			} else {
				// Está mostrando un grupo, miramos los miembros que tiene
				return (livingsDataIndexPagesCitizenGroups[livingsPanelCitizensGroupActive] - 1) * LIVINGS_PANEL_MAX_ROWS;
			}
		} else if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_SOLDIERS) {
			if (livingsPanelSoldiersGroupActive == -1) {
				// Todos los soldiers
				return (livingsDataIndexPages[LIVINGS_PANEL_TYPE_SOLDIERS] - 1) * LIVINGS_PANEL_MAX_ROWS;
			} else {
				// Está mostrando un grupo, miramos los miembros que tiene
				return (livingsDataIndexPagesSoldierGroups[livingsPanelSoldiersGroupActive] - 1) * LIVINGS_PANEL_MAX_ROWS;
			}
		} else if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_HEROES) {
			return (livingsDataIndexPages[LIVINGS_PANEL_TYPE_HEROES] - 1) * LIVINGS_PANEL_MAX_ROWS;
		}

		return -1;
	}


	/**
	 * Mouse pressed
	 * 
	 * @param x
	 * @param y
	 * @param mouseButton
	 */
	public void mousePressed (int x, int y, int mouseButton) {
		int iPanel = isMouseOnAPanel (x, y);

		if (iPanel == MOUSE_NONE) {
			return;
		}

		/*
		 * TYPING PANEL (Si está activo ya no miraremos nada más)
		 */
		if (typingPanel != null) {
			if (iPanel == MOUSE_TYPING_PANEL_CLOSE || (mouseButton == 1 && iPanel == MOUSE_TYPING_PANEL)) {
				typingPanel = null;
				UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_TYPING_PANEL_CONFIRM) {
				closeTypingPanel ();
				UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
				return;
			}
			return;
		}

		/*
		 * IMAGES PANEL
		 */
		if (imagesPanel != null && ImagesPanel.isVisible ()) {
			if (iPanel == MOUSE_IMAGES_PANEL_CLOSE || (mouseButton == 1 && iPanel == MOUSE_IMAGES_PANEL)) {
				ImagesPanel.setVisible (false);
				UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_IMAGES_PANEL_NEXT) {
				if (ImagesPanel.nextFlow ()) {
					UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
					return;
				}
			} else if (iPanel == MOUSE_IMAGES_PANEL_PREVIOUS) {
				if (ImagesPanel.previousFlow ()) {
					UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
					return;
				}
			} else if (iPanel == MOUSE_IMAGES_PANEL_NEXT_MISSION) {
				if (ImagesPanel.nextMission ()) {
					UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
					return;
				}
			} else if (iPanel == MOUSE_TUTORIAL_ICON) {
				// Miramos también el botón (para hacer toggle)
				toggleTutorialPanel (false);
				UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_IMAGES_PANEL) {
				return;
			}
		}

		/*
		 * PROFESSIONS PANEL
		 */
		if (isProfessionsPanelActive ()) {
			if (iPanel == MOUSE_PROFESSIONS_PANEL_BUTTONS_CLOSE) {
				setProfessionsPanelActive (-1, false);
				UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (professionsPanelMaxPages > 1 && iPanel == MOUSE_PROFESSIONS_PANEL_BUTTONS_SCROLL_UP) {
				if (professionsPanelPageIndex > 0) {
					professionsPanelPageIndex--;
				}
				UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (professionsPanelMaxPages > 1 && iPanel == MOUSE_PROFESSIONS_PANEL_BUTTONS_SCROLL_DOWN) {
				if ((professionsPanelPageIndex + 1) < professionsPanelMaxPages) {
					professionsPanelPageIndex++;
				}
				UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_PROFESSIONS_PANEL_BUTTONS_ITEMS) {
				// Ha clicado en un item, vamos a ver qué pasa
				if (menuProfessions == null) {
					return;
				}

				if (mouseButton == 1) { // Botón derecho (back al menú)
					if (menuProfessions.getParent () != null) {
						menuProfessions = menuProfessions.getParent ();
						resizeProfessionsPanel (menuProfessions);
						recheckProfessionsPanelPages ();
						UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
					} else {
						setProfessionsPanelActive (-1, false);
					}
					return;
				}

				// Botón izquierdo
				Point p = isMouseOnProfessionsButtons (x, y);
				if (p != null && p.y < menuProfessions.getItems ().size ()) {
					SmartMenu menuAux = menuProfessions.getItems ().get (p.y);
					if (menuAux.getType () == SmartMenu.TYPE_MENU) {
						menuProfessions = menuAux;
						resizeProfessionsPanel (menuProfessions);
						recheckProfessionsPanelPages ();
						UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
						return;
					}
					if (menuAux.getCommand ().equals (CommandPanel.COMMAND_BACK)) {
						// Back
						if (menuProfessions.getParent () != null) {
							menuProfessions = menuProfessions.getParent ();
							resizeProfessionsPanel (menuProfessions);
							recheckProfessionsPanelPages ();
							UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
						} else {
							setProfessionsPanelActive (-1, false);
						}
					} else {
						CommandPanel.executeCommand (menuAux.getCommand (), menuAux.getParameter (), menuAux.getParameter2 (), menuAux.getDirectCoordinates (), null, SmartMenu.ICON_TYPE_ITEM);
						// Regeneramos el menu
						if (professionsPanelIsCitizen) {
							if (!ActionPriorityManager.regenerateProfessionsPanelMenu (menuProfessions, professionsPanelCitizenOrGroupIDActive)) {
								setProfessionsPanelActive (-1, false);
							}
						} else {
							if (!ActionPriorityManager.regenerateJobGroupPanelMenu (menuProfessions, professionsPanelCitizenOrGroupIDActive)) {
								setProfessionsPanelActive (-1, false);
							}
						}

						UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
					}

					return;
				}
			}

			if (iPanel == MOUSE_PROFESSIONS_PANEL) {
				if (mouseButton == 1) { // Botón derecho (cerramos o tiramos 1 atrás el menú)
					if (menuProfessions != null) {
						if (menuProfessions.getParent () != null) {
							menuProfessions = menuProfessions.getParent ();
							resizePilePanel (menuProfessions);
							recheckProfessionsPanelPages ();
						} else {
							setProfessionsPanelActive (-1, false);
						}
					} else {
						setProfessionsPanelActive (-1, false);
					}
				}
				return;
			}
		}

		/*
		 * PILE PANEL
		 */
		if (isPilePanelActive ()) {
			if (iPanel == MOUSE_PILE_PANEL_BUTTONS_CLOSE) {
				setPilePanelActive (-1, false);
				UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (pilePanelMaxPages > 1 && iPanel == MOUSE_PILE_PANEL_BUTTONS_SCROLL_UP) {
				if (pilePanelPageIndex > 0) {
					pilePanelPageIndex--;
				}
				UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (pilePanelMaxPages > 1 && iPanel == MOUSE_PILE_PANEL_BUTTONS_SCROLL_DOWN) {
				if ((pilePanelPageIndex + 1) < pilePanelMaxPages) {
					pilePanelPageIndex++;
				}
				UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_PILE_PANEL_BUTTONS_CONFIG_COPY) {
				if (menuPile == null) {
					return;
				}

				if (pilePanelIsContainer) {
					CommandPanel.executeCommand (CommandPanel.COMMAND_CONTAINER_COPY_TO_ALL, menuPile.getParameter (), null, null, null, -1);
				} else {
					CommandPanel.executeCommand (CommandPanel.COMMAND_STOCKPILE_COPY_TO_ALL, menuPile.getParameter (), null, null, null, -1);
				}
				UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_PILE_PANEL_BUTTONS_CONFIG_LOCK) {
				if (menuPile == null) {
					return;
				}

				if (pilePanelIsContainer) {
					Container container = Game.getWorld ().getContainer (Integer.parseInt (menuPile.getParameter ()));
					if (container != null) {
						container.setLockedToCopy (!container.isLockedToCopy ());
						pilePanelIsLocked = container.isLockedToCopy ();
					}
				} else {
					Stockpile pile = Stockpile.getStockpile (menuPile.getParameter ());
					if (pile != null) {
						pile.setLockedToCopy (!pile.isLockedToCopy ());
						pilePanelIsLocked = pile.isLockedToCopy ();
					}
				}

				UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_PILE_PANEL_BUTTONS_CONFIG_LOCK_ALL) {
				if (menuPile == null) {
					return;
				}

				if (pilePanelIsContainer) {
					Container.lockUnlockAllConfigurations (true);

					Container container = Game.getWorld ().getContainer (Integer.parseInt (menuPile.getParameter ()));
					if (container != null) {
						pilePanelIsLocked = container.isLockedToCopy ();
					}
				} else {
					Stockpile.lockUnlockAllConfigurations (true);

					Stockpile pile = Stockpile.getStockpile (menuPile.getParameter ());
					if (pile != null) {
						pilePanelIsLocked = pile.isLockedToCopy ();
					}
				}

				UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_PILE_PANEL_BUTTONS_CONFIG_UNLOCK_ALL) {
				if (menuPile == null) {
					return;
				}

				if (pilePanelIsContainer) {
					Container.lockUnlockAllConfigurations (false);

					Container container = Game.getWorld ().getContainer (Integer.parseInt (menuPile.getParameter ()));
					if (container != null) {
						pilePanelIsLocked = container.isLockedToCopy ();
					}
				} else {
					Stockpile.lockUnlockAllConfigurations (false);

					Stockpile pile = Stockpile.getStockpile (menuPile.getParameter ());
					if (pile != null) {
						pilePanelIsLocked = pile.isLockedToCopy ();
					}
				}

				UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_PILE_PANEL_BUTTONS_ITEMS) {
				// Ha clicado en un item, vamos a ver qué pasa
				if (menuPile == null) {
					return;
				}

				if (mouseButton == 1) { // Botón derecho (back al menú)
					if (menuPile.getParent () != null) {
						menuPile = menuPile.getParent ();
						resizePilePanel (menuPile);
						recheckPilePanelPages ();
						UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
					} else {
						setPilePanelActive (-1, false);
					}
					return;
				}

				// Botón izquierdo
				Point p = isMouseOnPileButtons (x, y);
				if (p != null && p.y < menuPile.getItems ().size ()) {
					SmartMenu menuAux = menuPile.getItems ().get (p.y);
					if (menuAux.getType () == SmartMenu.TYPE_MENU) {
						menuPile = menuAux;
						resizePilePanel (menuPile);
						recheckPilePanelPages ();
						UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
						return;
					}
					if (menuAux.getCommand ().equals (CommandPanel.COMMAND_BACK)) {
						// Back
						if (menuPile.getParent () != null) {
							menuPile = menuPile.getParent ();
							resizePilePanel (menuPile);
							recheckPilePanelPages ();
							UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
						} else {
							setPilePanelActive (-1, false);
						}
					} else {
						CommandPanel.executeCommand (menuAux.getCommand (), menuAux.getParameter (), menuAux.getParameter2 (), menuAux.getDirectCoordinates (), null, SmartMenu.ICON_TYPE_ITEM);
						// Regeneramos el menu
						if (isPilePanelIsContainer ()) {
							// Container
							if (!Container.regenerateContainerPanelMenu (pilePanelPileContainerIDActive, menuPile)) {
								setPilePanelActive (-1, false);
							}
						} else {
							// Pila
							if (!Stockpile.regeneratePilePanelMenu (pilePanelPileContainerIDActive, menuPile)) {
								setPilePanelActive (-1, false);
							}
						}

						UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
					}

					return;
				}
			}

			if (iPanel == MOUSE_PILE_PANEL) {
				if (mouseButton == 1) { // Botón derecho (cerramos o tiramos 1 atrás el menú)
					if (menuPile != null) {
						if (menuPile.getParent () != null) {
							menuPile = menuPile.getParent ();
							resizePilePanel (menuPile);
							recheckPilePanelPages ();
						} else {
							setPilePanelActive (-1, false);
						}
					} else {
						setPilePanelActive (-1, false);
					}
				}
				return;
			}
		}

		/*
		 * MESSAGES PANEL
		 */
		if (isMessagesPanelActive ()) {
			int iMessagesType = getMessagesPanelActive ();
			if (iPanel == MOUSE_MESSAGES_PANEL_BUTTONS_CLOSE) {
				setMessagesPanelActive (-1);
				UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_MESSAGES_PANEL_BUTTONS_ANNOUNCEMENT) {
				setMessagesPanelActive (MessagesPanel.TYPE_ANNOUNCEMENT);
				UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_MESSAGES_PANEL_BUTTONS_COMBAT) {
				setMessagesPanelActive (MessagesPanel.TYPE_COMBAT);
				UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_MESSAGES_PANEL_BUTTONS_HEROES) {
				setMessagesPanelActive (MessagesPanel.TYPE_HEROES);
				UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_MESSAGES_PANEL_BUTTONS_SYSTEM) {
				setMessagesPanelActive (MessagesPanel.TYPE_SYSTEM);
				UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_MESSAGES_PANEL_BUTTONS_SCROLL_UP) {
				if (MessagesPanel.getPages (iMessagesType) > 1 && MessagesPanel.getPagesCurrent (iMessagesType) > 1) {
					MessagesPanel.pageUp (iMessagesType);
					UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
					return;
				}
			} else if (iPanel == MOUSE_MESSAGES_PANEL_BUTTONS_SCROLL_DOWN) {
				if (MessagesPanel.getPages (iMessagesType) > 1 && MessagesPanel.getPagesCurrent (iMessagesType) < MessagesPanel.getPages (iMessagesType)) {
					MessagesPanel.pageDown (iMessagesType);
					UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
					return;
				}
			} else if (iPanel == MOUSE_MESSAGES_PANEL) {
				if (mouseButton == 1) { // Botón derecho (cerramos)
					setMessagesPanelActive (-1);
				} else {
					if (MessagesPanel.mousePressed (x, y, getMessagesPanelActive (), messagesPanelSubPanelPoint.x + tileMessagesPanel[3].getTileWidth (), messagesPanelSubPanelPoint.y + tileMessagesPanel[1].getTileHeight ())) {
						setMessagesPanelActive (-1);
					}
				}
				return;
			}
		}

		/*
		 * MATS PANEL
		 */
		if (isMatsPanelActive ()) {
			if (iPanel == MOUSE_MATS_PANEL_BUTTONS_CLOSE) {
				setMatsPanelActive (false);
				UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_MATS_PANEL_BUTTONS_GROUPS) {
				Point p = isMouseOnMatsButtons (x, y);
				if (p != null) {
					setMatsPanelActive (p.y);
					UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
					return;
				}
			} else if (iPanel == MOUSE_MATS_PANEL_BUTTONS_SCROLL_UP) {
				if (matsIndexPages[getMatsPanelActive ()] > 0) {
					matsIndexPages[getMatsPanelActive ()]--;
					UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
					return;
				}
			} else if (iPanel == MOUSE_MATS_PANEL_BUTTONS_SCROLL_DOWN) {
				if (matsIndexPages[getMatsPanelActive ()] < (matsNumPages[getMatsPanelActive ()] - 1)) {
					matsIndexPages[getMatsPanelActive ()]++;
					UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
					return;
				}
			}

			if (iPanel == MOUSE_MATS_PANEL) {
				if (mouseButton == 1) { // Botón derecho (cerramos)
					setMatsPanelActive (false);
				}
				return;
			}
		}

		/*
		 * LIVINGS PANEL
		 */
		if (isLivingsPanelActive ()) {
			if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_CLOSE) {
				setLivingsPanelActive (LIVINGS_PANEL_TYPE_NONE, livingsPanelSoldiersGroupActive, livingsPanelCitizensGroupActive);
				UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_SCROLL_UP) {
				if (!checkGroupsPanelEnabled (getLivingsPanelActive ())) {
					if (livingsDataIndexPages[getLivingsPanelActive ()] > 1) {
						livingsDataIndexPages[getLivingsPanelActive ()]--;
						UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
						return;
					}
				} else {
					if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_CITIZENS) {
						if (livingsDataIndexPagesCitizenGroups[livingsPanelCitizensGroupActive] > 1) {
							livingsDataIndexPagesCitizenGroups[livingsPanelCitizensGroupActive]--;
							UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
							return;
						}
					} else {
						if (livingsDataIndexPagesSoldierGroups[livingsPanelSoldiersGroupActive] > 1) {
							livingsDataIndexPagesSoldierGroups[livingsPanelSoldiersGroupActive]--;
							UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
							return;
						}
					}
				}
			} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_SCROLL_DOWN) {
				int iNumLivings;
				ArrayList<Integer> alLivings = getLivings ();
				if (alLivings != null) {
					iNumLivings = alLivings.size ();
				} else {
					iNumLivings = 0;
				}
				if (iNumLivings > 0) {
					int iNumPages = (iNumLivings % LIVINGS_PANEL_MAX_ROWS == 0) ? iNumLivings / LIVINGS_PANEL_MAX_ROWS : (iNumLivings / LIVINGS_PANEL_MAX_ROWS) + 1;

					// Scrolls
					if (!checkGroupsPanelEnabled (getLivingsPanelActive ())) {
						if (livingsDataIndexPages[getLivingsPanelActive ()] < iNumPages) {
							livingsDataIndexPages[getLivingsPanelActive ()]++;
							UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
							return;
						}
					} else {
						if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_CITIZENS) {
							if (livingsDataIndexPagesCitizenGroups[livingsPanelCitizensGroupActive] < iNumPages) {
								livingsDataIndexPagesCitizenGroups[livingsPanelCitizensGroupActive]++;
								UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
								return;
							}
						} else {
							if (livingsDataIndexPagesSoldierGroups[livingsPanelSoldiersGroupActive] < iNumPages) {
								livingsDataIndexPagesSoldierGroups[livingsPanelSoldiersGroupActive]++;
								UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
								return;
							}
						}
					}
				}
			} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_UP && getLivingsPanelActive () == LIVINGS_PANEL_TYPE_CITIZENS && livingsPanelCitizensGroupActive == -1) {
				Game.getWorld ().substractRestrictHaulEquippingLevel ();
				UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
				// Tutorial flow
				Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_LIVINGS_RESTRICTION, null);
				return;
			} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_DOWN && getLivingsPanelActive () == LIVINGS_PANEL_TYPE_CITIZENS && livingsPanelCitizensGroupActive == -1) {
				Game.getWorld ().addRestrictHaulEquippingLevel ();
				UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
				// Tutorial flow
				Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_LIVINGS_RESTRICTION, null);
				return;
			} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_UP && getLivingsPanelActive () == LIVINGS_PANEL_TYPE_HEROES) {
				Game.getWorld ().substractRestrictExploringLevel ();
				UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
				// Tutorial flow
				Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_LIVINGS_RESTRICTION, null);
				return;
			} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_DOWN && getLivingsPanelActive () == LIVINGS_PANEL_TYPE_HEROES) {
				Game.getWorld ().addRestrictExploringLevel ();
				UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
				// Tutorial flow
				Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_LIVINGS_RESTRICTION, null);
				return;
			} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_SGROUP_REMOVE) {
				if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_SOLDIERS && livingsPanelSoldiersGroupActive != -1) {
					Point p = isMouseOnLivingsButtons (x, y);
					if (p != null) {
						ArrayList<Integer> alLivings = getLivings ();
						int iIndex = getLivingsIndex ();
						if (alLivings != null && p != null && (p.y + iIndex) >= 0 && (p.y + iIndex) < alLivings.size ()) {
							LivingEntity le = World.getLivingEntityByID (alLivings.get ((p.y + iIndex)));
							if (le != null) {
								Citizen soldier = (Citizen) le;
								soldier.getSoldierData ().setState (SoldierData.STATE_GUARD, -1, le.getID ());
								UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
								return;
							}
						}
					}
				}
			} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_SGROUP_ADD) {
				if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_SOLDIERS && livingsPanelSoldiersGroupActive == -1) {
					Point p = isMouseOnLivingsButtons (x, y);
					if (p != null) {
						ArrayList<Integer> alLivings = getLivings ();
						int iIndex = getLivingsIndex ();
						if (alLivings != null && p != null && (p.y + iIndex) >= 0 && (p.y + iIndex) < alLivings.size ()) {
							LivingEntity le = World.getLivingEntityByID (alLivings.get ((p.y + iIndex)));
							if (le != null) {
								Citizen soldier = (Citizen) le;
								if (Game.getCurrentState () == Game.STATE_CREATING_TASK) {
									Game.deleteCurrentTask ();
								}
								ContextMenu menu = new ContextMenu ();
								SmartMenu sm = new SmartMenu ();

								SoldierGroupData sgd;
								for (int g = 0; g < SoldierGroups.MAX_GROUPS; g++) {
									// Añadir a grupos existentes
									sgd = Game.getWorld ().getSoldierGroups ().getGroup (g);
									if (soldier.getSoldierData ().getState () != SoldierData.STATE_IN_A_GROUP || soldier.getSoldierData ().getGroup () != sgd.getId ()) {
										sm.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, sgd.getName (), null, CommandPanel.COMMAND_SOLDIER_SET_STATE, Integer.toString (soldier.getID ()), Integer.toString (SoldierData.STATE_IN_A_GROUP), new Point3D (sgd.getId (), -1, -1)));
									} else {
										sm.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, sgd.getName (), null, null, null, null, null, Color.GRAY));
									}
								}

								menu.setSmartMenu (sm);
								menu.setX (x + 16 + -menu.getWidth () / 2);
								menu.setY (y + 32);
								menu.resize ();
								Game.setContextMenu (menu);
								UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
								// Tutorial flow
								Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_LIVINGS_GROUPS, null);
								return;
							}
						}
					}
				}
			} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_JOBS_GROUPS_ADDREMOVE) {
				if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_CITIZENS) {
					Point p = isMouseOnLivingsButtons (x, y);
					if (p != null) {
						ArrayList<Integer> alLivings = getLivings ();
						int iIndex = getLivingsIndex ();
						if (alLivings != null && p != null && (p.y + iIndex) >= 0 && (p.y + iIndex) < alLivings.size ()) {
							LivingEntity le = World.getLivingEntityByID (alLivings.get ((p.y + iIndex)));
							if (le != null) {
								Citizen citizen = (Citizen) le;
								if (Game.getCurrentState () == Game.STATE_CREATING_TASK) {
									Game.deleteCurrentTask ();
								}
								ContextMenu menu = new ContextMenu ();
								SmartMenu sm = new SmartMenu ();

								CitizenGroupData cgd;
								for (int g = 0; g < CitizenGroups.MAX_GROUPS; g++) {
									// Añadir a grupos existentes
									cgd = Game.getWorld ().getCitizenGroups ().getGroup (g);
									if (citizen.getCitizenData ().getGroupID () != g) {
										sm.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("UIPanel.70") + cgd.getName (), null, CommandPanel.COMMAND_CITIZEN_SET_JOB_GROUP, Integer.toString (citizen.getID ()), Integer.toString (g), null, Color.GREEN)); //$NON-NLS-1$
									} else {
										sm.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("UIPanel.71") + cgd.getName (), null, CommandPanel.COMMAND_CITIZEN_SET_JOB_GROUP, Integer.toString (citizen.getID ()), Integer.toString (-1), null, Color.ORANGE)); //$NON-NLS-1$
									}
								}

								menu.setSmartMenu (sm);
								menu.setX (x + 16 + -menu.getWidth () / 2);
								menu.setY (y + 32);
								menu.resize ();
								Game.setContextMenu (menu);
								UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
								// Tutorial flow
								Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_LIVINGS_GROUPS, null);
								return;
							}
						}
					}
				}
			} else if (iPanel == MOUSE_LIVINGS_PANEL_SGROUP_NOGROUP) {
				livingsPanelSoldiersGroupActive = -1;
				createLivingsPanel (LIVINGS_PANEL_TYPE_SOLDIERS, -1, livingsPanelCitizensGroupActive);
				UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_LIVINGS_PANEL_SGROUP_GROUP) {
				Point p = isMouseOnLivingsButtons (x, y);
				if (p != null && p.y >= 0 && p.y < SoldierGroups.MAX_GROUPS) {
					livingsPanelSoldiersGroupActive = p.y;
					createLivingsPanel (LIVINGS_PANEL_TYPE_SOLDIERS, p.y, livingsPanelCitizensGroupActive);
					UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
					return;
				}
			} else if (iPanel == MOUSE_LIVINGS_PANEL_CGROUP_NOGROUP) {
				livingsPanelCitizensGroupActive = -1;
				createLivingsPanel (LIVINGS_PANEL_TYPE_CITIZENS, livingsPanelSoldiersGroupActive, -1);
				UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_LIVINGS_PANEL_CGROUP_GROUP) {
				Point p = isMouseOnLivingsButtons (x, y);
				if (p != null && p.y >= 0 && p.y < CitizenGroups.MAX_GROUPS) {
					livingsPanelCitizensGroupActive = p.y;
					createLivingsPanel (LIVINGS_PANEL_TYPE_CITIZENS, livingsPanelSoldiersGroupActive, p.y);
					UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
					return;
				}
			} else if (iPanel == MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_RENAME) {
				if (livingsPanelSoldiersGroupActive != -1) {
					typingPanel = new TypingPanel (renderWidth, renderHeight, Messages.getString ("UIPanel.61"), Game.getWorld ().getSoldierGroups ().getGroup (livingsPanelSoldiersGroupActive).getName (), TypingPanel.TYPE_RENAME_GROUP, livingsPanelSoldiersGroupActive); //$NON-NLS-1$
					UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
					return;
				}
			} else if (iPanel == MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_RENAME) {
				if (livingsPanelCitizensGroupActive != -1) {
					typingPanel = new TypingPanel (renderWidth, renderHeight, Messages.getString ("UIPanel.61"), Game.getWorld ().getCitizenGroups ().getGroup (livingsPanelCitizensGroupActive).getName (), TypingPanel.TYPE_RENAME_JOB_GROUP, livingsPanelCitizensGroupActive); //$NON-NLS-1$
					UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
					return;
				}
			} else if (iPanel == MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_GUARD) {
				if (livingsPanelSoldiersGroupActive != -1) {
					Game.getWorld ().getSoldierGroups ().getGroup (livingsPanelSoldiersGroupActive).setState (SoldierGroupData.STATE_GUARD);
					UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
					return;
				}
			} else if (iPanel == MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_PATROL) {
				if (livingsPanelSoldiersGroupActive != -1) {
					Game.getWorld ().getSoldierGroups ().getGroup (livingsPanelSoldiersGroupActive).setState (SoldierGroupData.STATE_PATROL);
					UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
					return;
				}
			} else if (iPanel == MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_BOSS) {
				if (livingsPanelSoldiersGroupActive != -1) {
					Game.getWorld ().getSoldierGroups ().getGroup (livingsPanelSoldiersGroupActive).setState (SoldierGroupData.STATE_BOSS);
					UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
					return;
				}
			} else if (iPanel == MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_AUTOEQUIP) {
				if (livingsPanelSoldiersGroupActive != -1) {
					ArrayList<Integer> alSoldiers = Game.getWorld ().getSoldierGroups ().getGroup (livingsPanelSoldiersGroupActive).getLivingIDs ();
					LivingEntity le;
					for (int s = 0; s < alSoldiers.size (); s++) {
						le = World.getLivingEntityByID (alSoldiers.get (s));
						if (le != null) {
							CommandPanel.executeCommand (CommandPanel.COMMAND_AUTOEQUIP, Integer.toString (le.getID ()), null, null, null, SmartMenu.ICON_TYPE_ITEM);
						}
					}
					UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
					return;
				}
			} else if (iPanel == MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_AUTOEQUIP) {
				ArrayList<Integer> alCitizens;
				if (livingsPanelCitizensGroupActive != -1) {
					alCitizens = Game.getWorld ().getCitizenGroups ().getGroup (livingsPanelCitizensGroupActive).getLivingIDs ();
				} else {
					alCitizens = Game.getWorld ().getCitizenGroups ().getCitizensWithoutGroup ();
				}
				LivingEntity le;
				for (int s = 0; s < alCitizens.size (); s++) {
					le = World.getLivingEntityByID (alCitizens.get (s));
					if (le != null) {
						CommandPanel.executeCommand (CommandPanel.COMMAND_AUTOEQUIP, Integer.toString (le.getID ()), null, null, null, SmartMenu.ICON_TYPE_ITEM);
					}
				}
				UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_DISBAND) {
				if (livingsPanelSoldiersGroupActive != -1) {
					ArrayList<Integer> alSoldiers = Game.getWorld ().getSoldierGroups ().getGroup (livingsPanelSoldiersGroupActive).getLivingIDs ();
					LivingEntity le;
					int iSize = alSoldiers.size ();
					for (int s = 0; s < iSize; s++) {
						le = World.getLivingEntityByID (alSoldiers.get (0));
						if (le != null) {
							((Citizen) le).getSoldierData ().setState (SoldierData.STATE_GUARD, -1, le.getID ());
						}
					}
					UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
					return;
				}
			} else if (iPanel == MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_DISBAND) {
				ArrayList<Integer> alCitizens;
				if (livingsPanelCitizensGroupActive != -1) {
					alCitizens = Game.getWorld ().getCitizenGroups ().getGroup (livingsPanelCitizensGroupActive).getLivingIDs ();
				} else {
					alCitizens = Game.getWorld ().getCitizenGroups ().getCitizensWithoutGroup ();
				}

				LivingEntity le;
				for (int s = (alCitizens.size () - 1); s >= 0; s--) {
					le = World.getLivingEntityByID (alCitizens.get (s));
					if (le != null) {
						CommandPanel.executeCommand (CommandPanel.COMMAND_CITIZEN_SET_JOB_GROUP, Integer.toString (le.getID ()), Integer.toString (-1), null, null, SmartMenu.ICON_TYPE_ITEM);
					}
				}
				UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_CHANGE_JOBS) {
				setProfessionsPanelActive (livingsPanelCitizensGroupActive, false);
				UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
				return;
			} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS || iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_HEAD || iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_BODY || iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_LEGS || iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_FEET || iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_WEAPON || iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_PROFESSIONS || iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER || iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_CIVILIAN
					|| iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_GUARD || iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_PATROL || iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_BOSS || iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_AUTOEQUIP) {
				Point p = isMouseOnLivingsButtons (x, y);
				if (p != null) {
					ArrayList<Integer> alLivings = getLivings ();
					int iIndex = getLivingsIndex ();
					if (alLivings != null && p != null && (p.y + iIndex) >= 0 && (p.y + iIndex) < alLivings.size ()) {
						LivingEntity le = World.getLivingEntityByID (alLivings.get ((p.y + iIndex)));
						if (le != null) {
							if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS) {
								Game.getWorld ().setView (le.getCoordinates ());
								UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
								setLivingsPanelActive (LIVINGS_PANEL_TYPE_NONE, livingsPanelSoldiersGroupActive, livingsPanelCitizensGroupActive);

								// Tutorial flow
								Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_LIVINGS_LIVINGS, null);
								return;
							} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_PROFESSIONS) {
								setProfessionsPanelActive (le.getID (), true);
								UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
								// Tutorial flow
								Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_LIVINGS_JOBS, null);
								return;
							} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER) {
								CommandPanel.executeCommand (CommandPanel.COMMAND_CONVERT_TO_SOLDIER, Integer.toString (le.getID ()), null, null, null, SmartMenu.ICON_TYPE_ITEM);
								UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
								// Tutorial flow
								Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_LIVINGS_CONVERTSOLDIER, null);
								return;
							} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_CIVILIAN) {
								CommandPanel.executeCommand (CommandPanel.COMMAND_CONVERT_TO_CIVILIAN, Integer.toString (le.getID ()), null, null, null, SmartMenu.ICON_TYPE_ITEM);
								UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
								// Tutorial flow
								Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_LIVINGS_CONVERTCIVILIAN, null);
								return;
							} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_GUARD) {
								CommandPanel.executeCommand (CommandPanel.COMMAND_SOLDIER_SET_STATE, Integer.toString (le.getID ()), Integer.toString (SoldierData.STATE_GUARD), new Point3D (-1, -1, -1), null, SmartMenu.ICON_TYPE_ITEM);
								UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
								return;
							} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_PATROL) {
								CommandPanel.executeCommand (CommandPanel.COMMAND_SOLDIER_SET_STATE, Integer.toString (le.getID ()), Integer.toString (SoldierData.STATE_PATROL), new Point3D (-1, -1, -1), null, SmartMenu.ICON_TYPE_ITEM);
								UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
								return;
							} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_BOSS) {
								CommandPanel.executeCommand (CommandPanel.COMMAND_SOLDIER_SET_STATE, Integer.toString (le.getID ()), Integer.toString (SoldierData.STATE_BOSS_AROUND), new Point3D (-1, -1, -1), null, SmartMenu.ICON_TYPE_ITEM);
								UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
								return;
							} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_HEAD) {
								if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_CITIZENS || getLivingsPanelActive () == LIVINGS_PANEL_TYPE_SOLDIERS) {
									MilitaryItem mi = le.getEquippedData ().getHead ();
									SmartMenu smUnequip = null;
									if (mi != null) {
										smUnequip = new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Citizen.20") + le.getEquippedData ().getHead ().getExtendedTilename (), null, CommandPanel.COMMAND_WEAR_OFF, null, null, new Point3D (le.getID (), MilitaryItem.LOCATION_HEAD, -1), le.getEquippedData ().getHead ().getItemTextColor ()); //$NON-NLS-1$
									}
									createMilitaryContextMenu (smUnequip, MilitaryItem.LOCATION_HEAD, le, x, y);
									UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
									// Tutorial flow
									Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_LIVINGS_BODY, null);
									return;
								}
							} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_BODY) {
								if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_CITIZENS || getLivingsPanelActive () == LIVINGS_PANEL_TYPE_SOLDIERS) {
									MilitaryItem mi = le.getEquippedData ().getBody ();
									SmartMenu smUnequip = null;
									if (mi != null) {
										smUnequip = new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Citizen.20") + le.getEquippedData ().getBody ().getExtendedTilename (), null, CommandPanel.COMMAND_WEAR_OFF, null, null, new Point3D (le.getID (), MilitaryItem.LOCATION_BODY, -1), le.getEquippedData ().getBody ().getItemTextColor ()); //$NON-NLS-1$
									}
									createMilitaryContextMenu (smUnequip, MilitaryItem.LOCATION_BODY, le, x, y);
									UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
									// Tutorial flow
									Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_LIVINGS_BODY, null);
									return;
								}
							} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_LEGS) {
								if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_CITIZENS || getLivingsPanelActive () == LIVINGS_PANEL_TYPE_SOLDIERS) {
									MilitaryItem mi = le.getEquippedData ().getLegs ();
									SmartMenu smUnequip = null;
									if (mi != null) {
										smUnequip = new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Citizen.20") + le.getEquippedData ().getLegs ().getExtendedTilename (), null, CommandPanel.COMMAND_WEAR_OFF, null, null, new Point3D (le.getID (), MilitaryItem.LOCATION_LEGS, -1), le.getEquippedData ().getLegs ().getItemTextColor ()); //$NON-NLS-1$
									}
									createMilitaryContextMenu (smUnequip, MilitaryItem.LOCATION_LEGS, le, x, y);
									UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
									// Tutorial flow
									Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_LIVINGS_BODY, null);
									return;
								}
							} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_FEET) {
								if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_CITIZENS || getLivingsPanelActive () == LIVINGS_PANEL_TYPE_SOLDIERS) {
									MilitaryItem mi = le.getEquippedData ().getFeet ();
									SmartMenu smUnequip = null;
									if (mi != null) {
										smUnequip = new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Citizen.20") + le.getEquippedData ().getFeet ().getExtendedTilename (), null, CommandPanel.COMMAND_WEAR_OFF, null, null, new Point3D (le.getID (), MilitaryItem.LOCATION_FEET, -1), le.getEquippedData ().getFeet ().getItemTextColor ()); //$NON-NLS-1$
									}
									createMilitaryContextMenu (smUnequip, MilitaryItem.LOCATION_FEET, le, x, y);
									UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
									// Tutorial flow
									Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_LIVINGS_BODY, null);
									return;
								}
							} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_WEAPON) {
								if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_CITIZENS || getLivingsPanelActive () == LIVINGS_PANEL_TYPE_SOLDIERS) {
									MilitaryItem mi = le.getEquippedData ().getWeapon ();
									SmartMenu smUnequip = null;
									if (mi != null) {
										smUnequip = new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("Citizen.20") + le.getEquippedData ().getWeapon ().getExtendedTilename (), null, CommandPanel.COMMAND_WEAR_OFF, null, null, new Point3D (le.getID (), MilitaryItem.LOCATION_WEAPON, -1), le.getEquippedData ().getWeapon ().getItemTextColor ()); //$NON-NLS-1$
									}
									createMilitaryContextMenu (smUnequip, MilitaryItem.LOCATION_WEAPON, le, x, y);
									UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
									// Tutorial flow
									Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_LIVINGS_BODY, null);
									return;
								}
							} else if (iPanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_AUTOEQUIP) {
								if (getLivingsPanelActive () == LIVINGS_PANEL_TYPE_CITIZENS || getLivingsPanelActive () == LIVINGS_PANEL_TYPE_SOLDIERS) {
									CommandPanel.executeCommand (CommandPanel.COMMAND_AUTOEQUIP, Integer.toString (le.getID ()), null, null, null, SmartMenu.ICON_TYPE_ITEM);
									UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
									// Tutorial flow
									Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_LIVINGS_AUTOEQUIP, null);
									return;
								}
							}
						}
					}
				}
			}

			if (iPanel == MOUSE_LIVINGS_PANEL) {
				if (mouseButton == 1) { // Botón derecho (cerramos)
					setLivingsPanelActive (LIVINGS_PANEL_TYPE_NONE, livingsPanelSoldiersGroupActive, livingsPanelCitizensGroupActive);
				}
				return;
			}
		}

		/*
		 * TRADE PANEL
		 */
		if (isTradePanelActive ()) {
			if (iPanel == MOUSE_TRADE_PANEL_BUTTONS_CLOSE) {
				setTradePanelActive (false);
				UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
				return;
			}

			if (tradePanel != null) {
				CaravanData caravanData = Game.getWorld ().getCurrentCaravanData ();
				boolean bTrading = (caravanData != null && caravanData.getStatus () == CaravanData.STATUS_TRADING);

				if (!bTrading && iPanel == MOUSE_TRADE_PANEL_BUTTONS_DOWN_CARAVAN) {
					tradePanel.scrollDownCaravan ();
					UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
					return;
				} else if (!bTrading && iPanel == MOUSE_TRADE_PANEL_BUTTONS_UP_CARAVAN) {
					tradePanel.scrollUpCaravan ();
					UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
					return;
				} else if (iPanel == MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_DOWN_CARAVAN) {
					tradePanel.scrollDownToBuyCaravan ();
					UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
					return;
				} else if (iPanel == MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_UP_CARAVAN) {
					tradePanel.scrollUpToBuyCaravan ();
					UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
					return;
				} else if (iPanel == MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_DOWN_TOWN) {
					tradePanel.scrollDownToSellTown ();
					UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
					return;
				} else if (iPanel == MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_UP_TOWN) {
					tradePanel.scrollUpToSellTown ();
					UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
					return;
				} else if (!bTrading && iPanel == MOUSE_TRADE_PANEL_BUTTONS_DOWN_TOWN) {
					tradePanel.scrollDownTown ();
					UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
					return;
				} else if (!bTrading && iPanel == MOUSE_TRADE_PANEL_BUTTONS_UP_TOWN) {
					tradePanel.scrollUpTown ();
					UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
					return;
				} else if (!bTrading && iPanel == MOUSE_TRADE_PANEL_BUTTONS_CARAVAN) {
					Point p = isMouseOnTradeButtons (x, y);
					if (p != null) {
						tradePanel.selectItemToBuy (p.y + tradePanel.getIndexButtonsCaravan ());
						UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
						return;
					}
				} else if (!bTrading && iPanel == MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_CARAVAN) {
					Point p = isMouseOnTradeButtons (x, y);
					if (p != null) {
						tradePanel.selectItemToNonBuy (p.y + tradePanel.getIndexButtonsToBuyCaravan ());
						UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
						return;
					}
				} else if (!bTrading && iPanel == MOUSE_TRADE_PANEL_BUTTONS_TOWN) {
					Point p = isMouseOnTradeButtons (x, y);
					if (p != null) {
						tradePanel.selectItemToSell (p.y + tradePanel.getIndexButtonsTown ());
						UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
						return;
					}
				} else if (!bTrading && iPanel == MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_TOWN) {
					Point p = isMouseOnTradeButtons (x, y);
					if (p != null) {
						tradePanel.selectItemToNonSell (p.y + tradePanel.getIndexButtonsToSellTown ());
						UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
						return;
					}
				} else if (!bTrading && iPanel == MOUSE_TRADE_PANEL_BUTTONS_CONFIRM) {
					if (tradePanel.isTransactionReady ()) {
						if (Game.getWorld ().getCurrentCaravanData () != null) {
							Game.getWorld ().getCurrentCaravanData ().confirmTrade ();
							UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
							return;
						}
					}
				}
			}
			if (iPanel == MOUSE_TRADE_PANEL) {
				if (mouseButton == 1) { // Botón derecho (cerramos)
					setTradePanelActive (false);
				}
				return;
			}
		}

		/*
		 * PRIORITIES PANEL
		 */
		if (isPrioritiesPanelActive ()) {
			if (iPanel == MOUSE_PRIORITIES_PANEL_ITEMS_DOWN) {
				Point p = isMouseOnPrioritiesItems (x, y);
				if (p != null) {
					if (p.x == MOUSE_PRIORITIES_PANEL_ITEMS_DOWN) {
						ActionPriorityManager.swapPriorities (p.y, p.y + 1);
						UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
						return;
					}
				}
			} else if (iPanel == MOUSE_PRIORITIES_PANEL_ITEMS_UP) {
				Point p = isMouseOnPrioritiesItems (x, y);
				if (p != null) {
					if (p.x == MOUSE_PRIORITIES_PANEL_ITEMS_UP) {
						ActionPriorityManager.swapPriorities (p.y, p.y - 1);
						UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
						return;
					}
				}
			} else if (iPanel == MOUSE_PRIORITIES_PANEL_ITEMS) {
				Point p = isMouseOnPrioritiesItems (x, y);
				if (p != null && p.y == (PRIORITIES_PANEL_NUM_ITEMS - 1)) {
					setPrioritiesPanelActive (false);
					UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
					return;
				}
			} else if (iPanel == MOUSE_PRIORITIES_PANEL) {
				if (mouseButton == 1) { // Botón derecho (cerramos)
					setPrioritiesPanelActive (false);
				}
				return;
			}
		}

		/*
		 * PRODUCTION PANEL
		 */
		if (iPanel == MOUSE_PRODUCTION_OPENCLOSE) {
			setProductionPanelLocked (!isProductionPanelLocked ());
			// setProductionPanelActive (!isProductionPanelActive ());
			UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (isProductionPanelActive ()) {
			if (iPanel == MOUSE_PRODUCTION_PANEL_ITEMS) {
				if (mouseButton == 1) { // Botón derecho (back al menú)
					if (productionPanelMenu.getParent () != null) {
						productionPanelMenu = productionPanelMenu.getParent ();
						createProductionPanel (productionPanelMenu);
						UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
					} else {
						setProductionPanelLocked (false);
					}
					return;
				}
				Point p = isMouseOnProductionItems (x, y);
				if (p != null) {
					if (p.x == MOUSE_PRODUCTION_PANEL_ITEMS) {
						SmartMenu smItem = productionPanelMenu.getItems ().get (p.y);
						if (smItem.getType () == SmartMenu.TYPE_MENU) {
							productionPanelMenu = smItem;
							createProductionPanel (smItem);
						} else if (smItem.getType () == SmartMenu.TYPE_ITEM) {
							if (!smItem.getCommand ().equalsIgnoreCase (CommandPanel.COMMAND_BACK)) {
								CommandPanel.executeCommand (smItem.getCommand (), smItem.getParameter (), smItem.getParameter2 (), smItem.getDirectCoordinates (), smItem.getIcon (), smItem.getIconType ());

								if (Keyboard.isKeyDown (Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown (Keyboard.KEY_RCONTROL)) {
									for (int rep = 0; rep < 99; rep++) {
										CommandPanel.executeCommand (smItem.getCommand (), smItem.getParameter (), smItem.getParameter2 (), smItem.getDirectCoordinates (), smItem.getIcon (), smItem.getIconType ());
									}
								} else if (Keyboard.isKeyDown (Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown (Keyboard.KEY_RSHIFT)) {
									CommandPanel.executeCommand (smItem.getCommand (), smItem.getParameter (), smItem.getParameter2 (), smItem.getDirectCoordinates (), smItem.getIcon (), smItem.getIconType ());
									CommandPanel.executeCommand (smItem.getCommand (), smItem.getParameter (), smItem.getParameter2 (), smItem.getDirectCoordinates (), smItem.getIcon (), smItem.getIconType ());
									CommandPanel.executeCommand (smItem.getCommand (), smItem.getParameter (), smItem.getParameter2 (), smItem.getDirectCoordinates (), smItem.getIcon (), smItem.getIconType ());
									CommandPanel.executeCommand (smItem.getCommand (), smItem.getParameter (), smItem.getParameter2 (), smItem.getDirectCoordinates (), smItem.getIcon (), smItem.getIconType ());
								}
							} else {
								if (productionPanelMenu.getParent () != null) {
									productionPanelMenu = productionPanelMenu.getParent ();
									createProductionPanel (productionPanelMenu);
								} else {
									setProductionPanelActive (false);
								}
							}
						}
						UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
					}
				}
				return;
			} else if (iPanel == MOUSE_PRODUCTION_PANEL_ITEMS_PLUS_REGULAR) {
				Point p = isMouseOnProductionItems (x, y);
				if (p != null) {
					if (p.x == MOUSE_PRODUCTION_PANEL_ITEMS_PLUS_REGULAR) {
						SmartMenu smItem = productionPanelMenu.getItems ().get (p.y);
						if (smItem.getType () == SmartMenu.TYPE_ITEM) {
							if (!smItem.getCommand ().equalsIgnoreCase (CommandPanel.COMMAND_BACK)) {
								CommandPanel.executeCommand (smItem.getCommand (), smItem.getParameter (), smItem.getParameter2 (), smItem.getDirectCoordinates (), smItem.getIcon (), smItem.getIconType ());
								// Tutorial flow
								Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_REGULAR_PLUS, null, smItem.getParameter ());

								if (Keyboard.isKeyDown (Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown (Keyboard.KEY_RCONTROL)) {
									for (int rep = 0; rep < 99; rep++) {
										CommandPanel.executeCommand (smItem.getCommand (), smItem.getParameter (), smItem.getParameter2 (), smItem.getDirectCoordinates (), smItem.getIcon (), smItem.getIconType ());
									}
								} else if (Keyboard.isKeyDown (Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown (Keyboard.KEY_RSHIFT)) {
									CommandPanel.executeCommand (smItem.getCommand (), smItem.getParameter (), smItem.getParameter2 (), smItem.getDirectCoordinates (), smItem.getIcon (), smItem.getIconType ());
									CommandPanel.executeCommand (smItem.getCommand (), smItem.getParameter (), smItem.getParameter2 (), smItem.getDirectCoordinates (), smItem.getIcon (), smItem.getIconType ());
									CommandPanel.executeCommand (smItem.getCommand (), smItem.getParameter (), smItem.getParameter2 (), smItem.getDirectCoordinates (), smItem.getIcon (), smItem.getIconType ());
									CommandPanel.executeCommand (smItem.getCommand (), smItem.getParameter (), smItem.getParameter2 (), smItem.getDirectCoordinates (), smItem.getIcon (), smItem.getIconType ());
								}
							}
						}
						UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
					}
				}
			} else if (iPanel == MOUSE_PRODUCTION_PANEL_ITEMS_MINUS_REGULAR) {
				Point p = isMouseOnProductionItems (x, y);
				if (p != null) {
					if (p.x == MOUSE_PRODUCTION_PANEL_ITEMS_MINUS_REGULAR) {
						SmartMenu smItem = productionPanelMenu.getItems ().get (p.y);
						if (smItem.getType () == SmartMenu.TYPE_ITEM) {
							if (!smItem.getCommand ().equalsIgnoreCase (CommandPanel.COMMAND_BACK)) {
								Game.getWorld ().getTaskManager ().removeFromQueue (smItem.getParameter ());
								// Tutorial flow
								Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_REGULAR_MINUS, null, smItem.getParameter ());

								if (Keyboard.isKeyDown (Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown (Keyboard.KEY_RCONTROL)) {
									for (int rep = 0; rep < 99; rep++) {
										Game.getWorld ().getTaskManager ().removeFromQueue (smItem.getParameter ());
									}
								} else if (Keyboard.isKeyDown (Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown (Keyboard.KEY_RSHIFT)) {
									Game.getWorld ().getTaskManager ().removeFromQueue (smItem.getParameter ());
									Game.getWorld ().getTaskManager ().removeFromQueue (smItem.getParameter ());
									Game.getWorld ().getTaskManager ().removeFromQueue (smItem.getParameter ());
									Game.getWorld ().getTaskManager ().removeFromQueue (smItem.getParameter ());
								}
							}
						}
						UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
					}
				}
			} else if (iPanel == MOUSE_PRODUCTION_PANEL_ITEMS_PLUS_AUTOMATED) {
				Point p = isMouseOnProductionItems (x, y);
				if (p != null) {
					if (p.x == MOUSE_PRODUCTION_PANEL_ITEMS_PLUS_AUTOMATED) {
						SmartMenu smItem = productionPanelMenu.getItems ().get (p.y);
						if (smItem.getType () == SmartMenu.TYPE_ITEM) {
							if (!smItem.getCommand ().equalsIgnoreCase (CommandPanel.COMMAND_BACK)) {
								Game.getWorld ().getTaskManager ().addItemOnAutomatedQueue (smItem.getParameter ());
								// Tutorial flow
								Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_AUTOMATED_PLUS, null, smItem.getParameter ());

								if (Keyboard.isKeyDown (Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown (Keyboard.KEY_RCONTROL)) {
									for (int rep = 0; rep < 99; rep++) {
										Game.getWorld ().getTaskManager ().addItemOnAutomatedQueue (smItem.getParameter ());
									}
								} else if (Keyboard.isKeyDown (Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown (Keyboard.KEY_RSHIFT)) {
									Game.getWorld ().getTaskManager ().addItemOnAutomatedQueue (smItem.getParameter ());
									Game.getWorld ().getTaskManager ().addItemOnAutomatedQueue (smItem.getParameter ());
									Game.getWorld ().getTaskManager ().addItemOnAutomatedQueue (smItem.getParameter ());
									Game.getWorld ().getTaskManager ().addItemOnAutomatedQueue (smItem.getParameter ());
								}
								UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
							}
						}
					}
				}
			} else if (iPanel == MOUSE_PRODUCTION_PANEL_ITEMS_MINUS_AUTOMATED) {
				Point p = isMouseOnProductionItems (x, y);
				if (p != null) {
					if (p.x == MOUSE_PRODUCTION_PANEL_ITEMS_MINUS_AUTOMATED) {
						SmartMenu smItem = productionPanelMenu.getItems ().get (p.y);
						if (smItem.getType () == SmartMenu.TYPE_ITEM) {
							if (!smItem.getCommand ().equalsIgnoreCase (CommandPanel.COMMAND_BACK)) {
								Game.getWorld ().getTaskManager ().removeItemOnAutomatedQueue (smItem.getParameter ());
								// Tutorial flow
								Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_AUTOMATED_MINUS, null, smItem.getParameter ());

								if (Keyboard.isKeyDown (Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown (Keyboard.KEY_RCONTROL)) {
									for (int rep = 0; rep < 99; rep++) {
										Game.getWorld ().getTaskManager ().removeItemOnAutomatedQueue (smItem.getParameter ());
									}
								} else if (Keyboard.isKeyDown (Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown (Keyboard.KEY_RSHIFT)) {
									Game.getWorld ().getTaskManager ().removeItemOnAutomatedQueue (smItem.getParameter ());
									Game.getWorld ().getTaskManager ().removeItemOnAutomatedQueue (smItem.getParameter ());
									Game.getWorld ().getTaskManager ().removeItemOnAutomatedQueue (smItem.getParameter ());
									Game.getWorld ().getTaskManager ().removeItemOnAutomatedQueue (smItem.getParameter ());
								}
								UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
							}
						}
					}
				}
			}
			if (iPanel == MOUSE_PRODUCTION_PANEL) {
				if (mouseButton == 1) { // Botón derecho (back al menú)
					if (productionPanelMenu.getParent () != null) {
						productionPanelMenu = productionPanelMenu.getParent ();
						createProductionPanel (productionPanelMenu);
						UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
					} else {
						setProductionPanelLocked (false);
					}
				}
				return;
			}
		}

		// BOTTOM
		if (iPanel == MOUSE_BOTTOM_OPENCLOSE) {
			setBottomMenuPanelLocked (!isBottomMenuPanelLocked ());
			// setProductionPanelActive (!isProductionPanelActive ());
			UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (isBottomMenuPanelActive ()) {
			if (iPanel == MOUSE_BOTTOM_LEFT_SCROLL) {
				if (bottomPanelItemIndex > 0) {
					bottomPanelItemIndex--;
					UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
				}
				return;
			}
			if (iPanel == MOUSE_BOTTOM_RIGHT_SCROLL) {
				if ((bottomPanelItemIndex + BOTTOM_PANEL_NUM_ITEMS) < currentMenu.getItems ().size ()) {
					bottomPanelItemIndex++;
					UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
				}
				return;
			}
			if (iPanel == MOUSE_BOTTOM_ITEMS) {
				int iItem = isMouseOnBottomItems (x, y);
				if (iItem != -1) {
					iItem = iItem + bottomPanelItemIndex;
					SmartMenu smItem = currentMenu.getItems ().get (iItem);
					if (smItem.getType () == SmartMenu.TYPE_MENU && smItem.getItems () != null && smItem.getItems ().size () > 0) {
						// Activamos el subpanel de abajo
						bottomSubPanelMenu = smItem;
						createBottomSubPanel (smItem);
					} else if (smItem.getType () == SmartMenu.TYPE_ITEM) {
						CommandPanel.executeCommand (smItem.getCommand (), smItem.getParameter (), smItem.getParameter2 (), smItem.getDirectCoordinates (), smItem.getIcon (), smItem.getIconType ());
					}
					UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
				}
				return;
			}
		}

		if (iPanel == MOUSE_BOTTOM_OPENCLOSE) {
			setBottomMenuPanelActive (!isBottomMenuPanelActive ());
			UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
		}

		// MENU
		if (iPanel == MOUSE_MENU_OPENCLOSE) {
			setMenuPanelLocked (!isMenuPanelLocked ());
			UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (isMenuPanelActive ()) {
			if (iPanel == MOUSE_MENU_PANEL_ITEMS) {
				if (mouseButton == 1) { // Botón derecho (back al menú)
					if (menuPanelMenu.getParent () != null) {
						menuPanelMenu = menuPanelMenu.getParent ();
						createMenuPanel (menuPanelMenu);
						createProductionPanel (productionPanelMenu);
						UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
					} else {
						setMenuPanelLocked (false);
					}
					return;
				}
				int iItem = isMouseOnMenuItems (x, y);
				if (iItem != -1) {
					SmartMenu smItem = menuPanelMenu.getItems ().get (iItem);
					if (smItem.getType () == SmartMenu.TYPE_MENU) {
						createMenuPanel (smItem);
						createProductionPanel (productionPanelMenu);
						menuPanelMenu = smItem;
					} else if (smItem.getType () == SmartMenu.TYPE_ITEM) {
						if (!smItem.getCommand ().equalsIgnoreCase (CommandPanel.COMMAND_BACK)) {
							CommandPanel.executeCommand (smItem.getCommand (), smItem.getParameter (), smItem.getParameter2 (), smItem.getDirectCoordinates (), smItem.getIcon (), smItem.getIconType ());
						} else {
							if (menuPanelMenu.getParent () != null) {
								menuPanelMenu = menuPanelMenu.getParent ();
								createMenuPanel (menuPanelMenu);
								createProductionPanel (productionPanelMenu);
							}
						}
					}
					UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
				}
				return;
			}
			if (iPanel == MOUSE_MENU_PANEL) {
				if (mouseButton == 1) { // Botón derecho (back al menú)
					if (menuPanelMenu.getParent () != null) {
						menuPanelMenu = menuPanelMenu.getParent ();
						createMenuPanel (menuPanelMenu);
						createProductionPanel (productionPanelMenu);
						UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
					} else {
						setMenuPanelLocked (false);
					}
				}
				return;
			}
		}

		if (iPanel == MOUSE_MENU_OPENCLOSE) {
			setMenuPanelActive (!isMenuPanelActive ());
			UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
		}

		// BOTTOM submenu
		if (bottomSubPanelMenu != null && iPanel == MOUSE_BOTTOM_SUBITEMS) {
			// BOTTOM SUBPANEL
			if (mouseButton == 1) { // Botón derecho (back al menú)
				bottomSubPanelMenu = bottomSubPanelMenu.getParent ();
				if (bottomSubPanelMenu != null) {
					if (bottomSubPanelMenu.getParent () == null) {
						bottomSubPanelMenu = null;
						createProductionPanel (productionPanelMenu);
					} else {
						createBottomSubPanel (bottomSubPanelMenu);
					}
					UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
				}
				return;
			}

			int iItem = isMouseOnBottomSubItems (x, y);
			if (iItem != -1) {
				SmartMenu smItem = bottomSubPanelMenu.getItems ().get (iItem);
				if (smItem.getType () == SmartMenu.TYPE_MENU && smItem.getItems () != null && smItem.getItems ().size () > 0) {
					// Activamos el subpanel de abajo
					bottomSubPanelMenu = smItem;
					createBottomSubPanel (smItem);
				} else if (smItem.getType () == SmartMenu.TYPE_ITEM) {
					if (!smItem.getCommand ().equalsIgnoreCase (CommandPanel.COMMAND_BACK)) {
						CommandPanel.executeCommand (smItem.getCommand (), smItem.getParameter (), smItem.getParameter2 (), smItem.getDirectCoordinates (), smItem.getIcon (), smItem.getIconType ());
						bottomSubPanelMenu = null;
						createProductionPanel (productionPanelMenu);
					} else {
						bottomSubPanelMenu = bottomSubPanelMenu.getParent ();
						if (bottomSubPanelMenu != null) {
							if (bottomSubPanelMenu.getParent () == null) {
								bottomSubPanelMenu = null;
								createProductionPanel (productionPanelMenu);
							} else {
								createBottomSubPanel (bottomSubPanelMenu);
							}
						}
					}
				}
				UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
			}
			return;
		}

		if (bottomSubPanelMenu != null && iPanel == MOUSE_BOTTOM_SUBPANEL) {
			// BOTTOM SUBPANEL
			if (mouseButton == 1) { // Botón derecho (back al menú)
				bottomSubPanelMenu = bottomSubPanelMenu.getParent ();
				if (bottomSubPanelMenu != null) {
					if (bottomSubPanelMenu.getParent () == null) {
						bottomSubPanelMenu = null;
						createProductionPanel (productionPanelMenu);
					} else {
						createBottomSubPanel (bottomSubPanelMenu);
					}
				}
				UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
			}
			return;
		}

		// ICONS
		if (iPanel == MOUSE_ICON_LEVEL_UP) {
			CommandPanel.executeCommand (CommandPanel.COMMAND_LEVEL_UP, null, null, null, null, 0);
			if (Keyboard.isKeyDown (Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown (Keyboard.KEY_RCONTROL)) {
				for (int rep = 0; rep < 99; rep++) {
					CommandPanel.executeCommand (CommandPanel.COMMAND_LEVEL_UP, null, null, null, null, 0);
				}
			} else if (Keyboard.isKeyDown (Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown (Keyboard.KEY_RSHIFT)) {
				CommandPanel.executeCommand (CommandPanel.COMMAND_LEVEL_UP, null, null, null, null, 0);
				CommandPanel.executeCommand (CommandPanel.COMMAND_LEVEL_UP, null, null, null, null, 0);
				CommandPanel.executeCommand (CommandPanel.COMMAND_LEVEL_UP, null, null, null, null, 0);
				CommandPanel.executeCommand (CommandPanel.COMMAND_LEVEL_UP, null, null, null, null, 0);
			}
			UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (iPanel == MOUSE_ICON_LEVEL_DOWN) {
			CommandPanel.executeCommand (CommandPanel.COMMAND_LEVEL_DOWN, null, null, null, null, 0);
			if (Keyboard.isKeyDown (Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown (Keyboard.KEY_RCONTROL)) {
				for (int rep = 0; rep < 99; rep++) {
					CommandPanel.executeCommand (CommandPanel.COMMAND_LEVEL_DOWN, null, null, null, null, 0);
				}
			} else if (Keyboard.isKeyDown (Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown (Keyboard.KEY_RSHIFT)) {
				CommandPanel.executeCommand (CommandPanel.COMMAND_LEVEL_DOWN, null, null, null, null, 0);
				CommandPanel.executeCommand (CommandPanel.COMMAND_LEVEL_DOWN, null, null, null, null, 0);
				CommandPanel.executeCommand (CommandPanel.COMMAND_LEVEL_DOWN, null, null, null, null, 0);
				CommandPanel.executeCommand (CommandPanel.COMMAND_LEVEL_DOWN, null, null, null, null, 0);
			}
			UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (iPanel == MOUSE_ICON_CITIZEN_PREVIOUS) {
			CommandPanel.executeCommand (CommandPanel.COMMAND_PREVIOUS_CITIZEN, null, null, null, null, 0);
			UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (iPanel == MOUSE_ICON_CITIZEN_NEXT) {
			CommandPanel.executeCommand (CommandPanel.COMMAND_NEXT_CITIZEN, null, null, null, null, 0);
			UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (iPanel == MOUSE_INFO_NUM_CITIZENS) {
			if (getLivingsPanelActive () != LIVINGS_PANEL_TYPE_CITIZENS) {
				setLivingsPanelActive (LIVINGS_PANEL_TYPE_CITIZENS, livingsPanelSoldiersGroupActive, livingsPanelCitizensGroupActive);

				Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_CITIZENS, null);
			} else {
				setLivingsPanelActive (LIVINGS_PANEL_TYPE_NONE, livingsPanelSoldiersGroupActive, livingsPanelCitizensGroupActive);
			}
			UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (iPanel == MOUSE_ICON_SOLDIER_PREVIOUS) {
			CommandPanel.executeCommand (CommandPanel.COMMAND_PREVIOUS_SOLDIER, null, null, null, null, 0);
			UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (iPanel == MOUSE_ICON_SOLDIER_NEXT) {
			CommandPanel.executeCommand (CommandPanel.COMMAND_NEXT_SOLDIER, null, null, null, null, 0);
			UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (iPanel == MOUSE_INFO_NUM_SOLDIERS) {
			if (getLivingsPanelActive () != LIVINGS_PANEL_TYPE_SOLDIERS) {
				setLivingsPanelActive (LIVINGS_PANEL_TYPE_SOLDIERS, livingsPanelSoldiersGroupActive, livingsPanelCitizensGroupActive);

				Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_SOLDIERS, null);
			} else {
				setLivingsPanelActive (LIVINGS_PANEL_TYPE_NONE, livingsPanelSoldiersGroupActive, livingsPanelCitizensGroupActive);
			}
			UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (iPanel == MOUSE_ICON_HERO_PREVIOUS) {
			CommandPanel.executeCommand (CommandPanel.COMMAND_PREVIOUS_HERO, null, null, null, null, 0);
			UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (iPanel == MOUSE_ICON_HERO_NEXT) {
			CommandPanel.executeCommand (CommandPanel.COMMAND_NEXT_HERO, null, null, null, null, 0);
			UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (iPanel == MOUSE_INFO_NUM_HEROES) {
			if (getLivingsPanelActive () != LIVINGS_PANEL_TYPE_HEROES) {
				setLivingsPanelActive (LIVINGS_PANEL_TYPE_HEROES, livingsPanelSoldiersGroupActive, livingsPanelCitizensGroupActive);

				Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_HEROES, null);
			} else {
				setLivingsPanelActive (LIVINGS_PANEL_TYPE_NONE, livingsPanelSoldiersGroupActive, livingsPanelCitizensGroupActive);
			}
			UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (iPanel == MOUSE_INFO_CARAVAN) {
			setTradePanelActive (!UIPanel.isTradePanelActive ());
			UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (iPanel == MOUSE_ICON_PRIORITIES) {
			setPrioritiesPanelActive (!isPrioritiesPanelActive ());
			UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (iPanel == MOUSE_ICON_MATS) {
			setMatsPanelActive (!isMatsPanelActive ());
			UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (iPanel == MOUSE_ICON_MINIBLOCKS) {
			MainPanel.toggleMiniBlocks ();
			UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (iPanel == MOUSE_ICON_FLATMOUSE) {
			MainPanel.toggleFlatMouse ();
			UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (iPanel == MOUSE_ICON_3DMOUSE) {
			MainPanel.toggle3DMouse ();
			UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (iPanel == MOUSE_ICON_GRID) {
			MainPanel.toggleGrid ();
			UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (iPanel == MOUSE_ICON_SETTINGS) {
			CommandPanel.executeCommand (CommandPanel.COMMAND_EXIT_TO_MAIN_MENU, null, null, null, null, 0);
			UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (iPanel == MOUSE_ICON_PAUSE_RESUME) {
			CommandPanel.executeCommand (CommandPanel.COMMAND_PAUSE, null, null, null, null, 0);
			UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
			return;
		}
		if (iPanel == MOUSE_ICON_LOWER_SPEED) {
			if (World.SPEED > 1) {
				CommandPanel.executeCommand (CommandPanel.COMMAND_LOWER_SPEED, null, null, null, null, 0);
				UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
			}
			return;
		}
		if (iPanel == MOUSE_ICON_INCREASE_SPEED) {
			if (World.SPEED < World.SPEED_MAX) {
				CommandPanel.executeCommand (CommandPanel.COMMAND_INCREASE_SPEED, null, null, null, null, 0);
				UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
			}
			return;
		}
		if (iPanel == MOUSE_TUTORIAL_ICON) {
			toggleTutorialPanel (false);
			UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
			return;
		}

		if (iPanel == MOUSE_MESSAGES_ICON_ANNOUNCEMENT) {
			if (getMessagesPanelActive () != MessagesPanel.TYPE_ANNOUNCEMENT) {
				setMessagesPanelActive (MessagesPanel.TYPE_ANNOUNCEMENT);
			} else {
				setMessagesPanelActive (-1);
			}
			UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
			return;
		}

		if (iPanel == MOUSE_MESSAGES_ICON_COMBAT) {
			if (getMessagesPanelActive () != MessagesPanel.TYPE_COMBAT) {
				setMessagesPanelActive (MessagesPanel.TYPE_COMBAT);
			} else {
				setMessagesPanelActive (-1);
			}
			UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
			return;
		}

		if (iPanel == MOUSE_MESSAGES_ICON_HEROES) {
			if (getMessagesPanelActive () != MessagesPanel.TYPE_HEROES) {
				setMessagesPanelActive (MessagesPanel.TYPE_HEROES);
			} else {
				setMessagesPanelActive (-1);
			}
			UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
			return;
		}

		if (iPanel == MOUSE_MESSAGES_ICON_SYSTEM) {
			if (getMessagesPanelActive () != MessagesPanel.TYPE_SYSTEM) {
				setMessagesPanelActive (MessagesPanel.TYPE_SYSTEM);
			} else {
				setMessagesPanelActive (-1);
			}
			UtilsAL.play (UtilsAL.SOURCE_FX_CLICK);
			return;
		}

		// MINIMAP
		if (iPanel == MOUSE_MINIMAP) {
			MiniMapPanel.mousePressed (x - minimapPanelX, y - minimapPanelY, mouseButton);
			return;
		}
	}


	/**
	 * Key pressed. Mostly because the ESC key to close the panels
	 * 
	 * @param tecla
	 * @return true if something is done
	 */
	public static boolean keyPressed (int tecla) {
		if (tecla == Keyboard.KEY_ESCAPE) {
			if (imagesPanel != null && ImagesPanel.isVisible ()) {
				ImagesPanel.setVisible (false);
				return true;
			} else if (isPrioritiesPanelActive ()) {
				setPrioritiesPanelActive (false);
				return true;
			} else if (isTradePanelActive ()) {
				setTradePanelActive (false);
				return true;
			} else if (isProfessionsPanelActive ()) {
				setProfessionsPanelActive (-1, false);
				return true;
			} else if (isLivingsPanelActive ()) {
				setLivingsPanelActive (LIVINGS_PANEL_TYPE_NONE, livingsPanelSoldiersGroupActive, livingsPanelCitizensGroupActive);
				return true;
			} else if (isMatsPanelActive ()) {
				setMatsPanelActive (false);
				return true;
			} else if (isMessagesPanelActive ()) {
				setMessagesPanelActive (-1);
				return true;
			} else if (isPilePanelActive ()) {
				setPilePanelActive (-1, false);
				return true;
			}
		}

		return false;
	}


	public static int getImagesPanelOffset () {
		if (isProductionPanelActive ()) {
			return productionPanelPoint.x + PRODUCTION_PANEL_WIDTH;
		}

		return 0;
	}


	public static void toggleTutorialPanel (boolean bNewGame) {
		if (Game.getCurrentMissionData () != null && Game.getCurrentMissionData ().getTutorialFlows ().size () > 0) {
			if (imagesPanel == null) {
				imagesPanel = new ImagesPanel (MainPanel.renderWidth, MainPanel.renderHeight, Game.getCurrentMissionData ());
				if (bNewGame) {
					ImagesPanel.setCurrentFlowIndex (0);
				} else {
					ImagesPanel.setCurrentFlowIndex (Game.getCurrentMissionData ().getTutorialFlowIndex ());
				}
				ImagesPanel.setMaxFlowIndex (Game.getCurrentMissionData ().getTutorialFlowIndex ());
				ImagesPanel.setVisible (true);
			} else {
				ImagesPanel.setVisible (!ImagesPanel.isVisible ());
			}
		}
	}


	private void createMilitaryContextMenu (SmartMenu smToAdd, int iLocation, LivingEntity le, int mouseX, int mouseY) {
		// Equipar, miramos si hay objetos militares en el mundo, de paso ya hacemos una lista para poner en el menú
		Integer[] aItems = World.getItems ().keySet ().toArray (new Integer [0]);
		ArrayList<MilitaryItem> alMilitaryItems = new ArrayList<MilitaryItem> ();

		int iASZID = World.getCell (le.getCoordinates ()).getAstarZoneID ();

		ItemManagerItem imi;
		Item mi;
		for (int i = 0; i < aItems.length; i++) {
			mi = World.getItems ().get (aItems[i]);
			if (mi != null && mi instanceof MilitaryItem) {
				if (World.getCell (mi.getCoordinates ()).getAstarZoneID () == iASZID) {
					imi = ItemManager.getItem (mi.getIniHeader ());
					if (imi.getLocation () == iLocation) {
						// Lo metemos en la posición correcta, ordenado por item level
						int iItemLevel = imi.getLevel ();
						int iIndexLevel = -1;
						for (int iL = 0; iL < alMilitaryItems.size (); iL++) {
							imi = ItemManager.getItem (alMilitaryItems.get (iL).getIniHeader ());
							if (imi.getLevel () <= iItemLevel) {
								// Bingo
								iIndexLevel = iL;
								break;
							}
						}

						if (iIndexLevel == -1) {
							alMilitaryItems.add ((MilitaryItem) mi);
						} else {
							alMilitaryItems.add (iIndexLevel, (MilitaryItem) mi);
						}
					}
				}
			}
		}

		// Containers
		ArrayList<Container> alContainers = Game.getWorld ().getContainers ();
		ArrayList<Item> alContainerItems;
		nextContainer: for (int i = 0; i < alContainers.size (); i++) {
			alContainerItems = alContainers.get (i).getItemsInside ();
			for (int j = 0; j < alContainerItems.size (); j++) {
				mi = alContainerItems.get (j);
				if (World.getCell (mi.getCoordinates ()).getAstarZoneID () != iASZID) {
					continue nextContainer;
				}

				if (mi != null && mi instanceof MilitaryItem) {
					imi = ItemManager.getItem (mi.getIniHeader ());
					if (imi.getLocation () == iLocation) {
						// Lo metemos en la posición correcta, ordenado por item level
						int iItemLevel = imi.getLevel ();
						int iIndexLevel = -1;
						for (int iL = 0; iL < alMilitaryItems.size (); iL++) {
							imi = ItemManager.getItem (alMilitaryItems.get (iL).getIniHeader ());
							if (imi.getLevel () <= iItemLevel) {
								// Bingo
								iIndexLevel = iL;
								break;
							}
						}

						if (iIndexLevel == -1) {
							alMilitaryItems.add ((MilitaryItem) mi);
						} else {
							alMilitaryItems.add (iIndexLevel, (MilitaryItem) mi);
						}
					}
				}
			}
		}

		if (Game.getCurrentState () == Game.STATE_CREATING_TASK) {
			Game.deleteCurrentTask ();
		}
		ContextMenu menuMilitary = new ContextMenu ();
		SmartMenu smMilitary = new SmartMenu ();

		if (alMilitaryItems.size () > 0 || smToAdd != null) {
			// Tenemos la lista con items que el aldeano puede equipar, creamos el menú
			if (smToAdd != null) {
				smMilitary.addItem (smToAdd);
				if (alMilitaryItems.size () > 0) {
					smMilitary.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, null, null, null, null));
				}
			}

			// Ordenamos el menú por item level
			MilitaryItem militaryItem;
			for (int i = 0; i < alMilitaryItems.size (); i++) {
				militaryItem = alMilitaryItems.get (i);
				if (militaryItem.getZ () > Game.getWorld ().getRestrictHaulEquippingLevel ()) {
					smMilitary.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString ("UIPanel.79") + militaryItem.getExtendedTilename (), null, CommandPanel.COMMAND_WEAR, Integer.toString (le.getID ()), Integer.toString (militaryItem.getID ()), militaryItem.getCoordinates ().toPoint3D (), militaryItem.getItemTextColor ())); //$NON-NLS-1$
				} else {
					smMilitary.addItem (new SmartMenu (SmartMenu.TYPE_ITEM, militaryItem.getExtendedTilename (), null, CommandPanel.COMMAND_WEAR, Integer.toString (le.getID ()), Integer.toString (militaryItem.getID ()), militaryItem.getCoordinates ().toPoint3D (), militaryItem.getItemTextColor ()));
				}
			}
		} else {
			smMilitary.addItem (new SmartMenu (SmartMenu.TYPE_TEXT, Messages.getString ("UIPanel.56"), null, null, null)); //$NON-NLS-1$
		}
		menuMilitary.setSmartMenu (smMilitary);
		menuMilitary.setX (mouseX + 16 + -menuMilitary.getWidth () / 2);
		menuMilitary.setY (mouseY + 32);
		menuMilitary.resize ();
		Game.setContextMenu (menuMilitary);
	}


	public static boolean isMessagesPanelActive () {
		return messagesPanelActive != -1;
	}


	public static int getMessagesPanelActive () {
		return messagesPanelActive;
	}


	public static void setMessagesPanelActive (int iMessageType) {
		messagesPanelActive = iMessageType;
		if (iMessageType != -1) {
			closePanels (true, true, false, true, true, true, true);
		}
	}


	public static void setProductionPanelActive (boolean productionPanelActive) {
		UIPanel.productionPanelActive = productionPanelActive;

		ImagesPanel.resize (renderWidth, renderHeight);
	}


	public static boolean isProductionPanelActive () {
		return productionPanelActive;
	}


	public static void setProductionPanelLocked (boolean productionPanelLocked) {
		UIPanel.productionPanelLocked = productionPanelLocked;
	}


	public static boolean isProductionPanelLocked () {
		return productionPanelLocked;
	}


	public static void setTradePanelActive (boolean tradePanelActive) {
		if (!isTradePanelActive ()) {
			if (Game.getWorld ().getCurrentCaravanData () != null) {
				Game.getWorld ().getCurrentCaravanData ().updateCaravanStatus ();
			}
		}

		if (UIPanel.tradePanelActive != tradePanelActive) {
			if (tradePanelActive) {
				// Se activa el panel
				tradePanelActivePausedBefore = Game.isPaused ();
				Game.pause (false);

				// Chequeamos los items en el mundo
				if (tradePanel != null) {
					tradePanel.createTownMenu ();
				}
				UIPanel.tradePanelActive = tradePanelActive;


				Game.updateTutorialFlow (TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_TRADE, null);
			} else {
				UIPanel.tradePanelActive = tradePanelActive; // Se pone primero pq sino el Game.resume no funcionará

				// Se desactiva el panel, quitamos la pausa si al activar el panel el juego no estaba pausado
				if (!tradePanelActivePausedBefore) {
					// Si antes no había pausa la quitamos
					Game.resume (false);
				}
			}

			if (tradePanelActive) {
				closePanels (true, false, true, true, true, true, true);
			}
		}
	}


	public static boolean isTradePanelActive () {
		return tradePanelActive;
	}


	public static void setPrioritiesPanelActive (boolean prioritiesPanelActive) {
		UIPanel.prioritiesPanelActive = prioritiesPanelActive;
		if (prioritiesPanelActive) {
			closePanels (false, true, true, true, true, true, true);
		}
	}


	public static boolean isPrioritiesPanelActive () {
		return prioritiesPanelActive;
	}


	public static void setMatsPanelActive (boolean bActive) {
		if (bActive) {
			setMatsPanelActive (matsLastGroup);
		} else {
			setMatsPanelActive (-1);
		}
	}


	public static void setMatsPanelActive (int iGroup) {
		UIPanel.matsPanelActive = iGroup;
		if (iGroup != -1) {
			closePanels (true, true, true, false, true, true, true);
			matsLastGroup = iGroup;
		}
	}


	public static boolean isPilePanelActive () {
		return pilePanelPileContainerIDActive != -1;
	}


	public static boolean isProfessionsPanelActive () {
		return professionsPanelCitizenOrGroupIDActive != -1;
	}


	public static int getMatsPanelActive () {
		return matsPanelActive;
	}


	public static void setPilePanelActive (int iPileContainerID, boolean isContainer) {
		pilePanelPileContainerIDActive = iPileContainerID;
		pilePanelIsContainer = isContainer;

		if (iPileContainerID != -1) {
			closePanels (true, true, true, true, true, false, true);

			// Creamos el menú
			if (isContainer) {
				menuPile = Container.createContainerMenu (iPileContainerID);

				if (menuPile != null) {
					Container container = Game.getWorld ().getContainer (iPileContainerID);
					pilePanelIsLocked = container.isLockedToCopy ();
				}
			} else {
				menuPile = Stockpile.createPilePanelMenu (iPileContainerID);

				if (menuPile != null) {
					Stockpile pile = Stockpile.getStockpile (iPileContainerID);
					pilePanelIsLocked = pile.isLockedToCopy ();
				}
			}

			if (menuPile != null) {
				// Cambiamos el tamaño de los iconos
				resizeIcons (menuPile, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT);

				resizePilePanel (menuPile);

				// Miramos las pages
				recheckPilePanelPages ();
			}
		} else {
			menuPile = null;
		}
	}


	public static void setPilePanelIsContainer (boolean pilePanelIsContainer) {
		UIPanel.pilePanelIsContainer = pilePanelIsContainer;
	}


	public static boolean isPilePanelIsContainer () {
		return pilePanelIsContainer;
	}


	public static void setProfessionsPanelActive (int iProfessionsCitizenOrGroupID, boolean isCitizen) {
		professionsPanelCitizenOrGroupIDActive = iProfessionsCitizenOrGroupID;
		professionsPanelIsCitizen = isCitizen;

		if (iProfessionsCitizenOrGroupID != -1) {
			closePanels (true, true, true, true, false, true, false);

			// Creamos el menú
			if (isCitizen) {
				menuProfessions = ActionPriorityManager.createProfessionsMenu (iProfessionsCitizenOrGroupID);
			} else {
				menuProfessions = ActionPriorityManager.createJobGroupPanelMenu (iProfessionsCitizenOrGroupID);
			}

			if (menuProfessions != null) {
				// Cambiamos el tamaño de los iconos
				resizeIcons (menuProfessions, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT);

				resizeProfessionsPanel (menuProfessions);

				// Miramos las pages
				recheckProfessionsPanelPages ();
			}
		} else {
			menuProfessions = null;
		}
	}


	public static boolean isMatsPanelActive () {
		return matsPanelActive != -1;
	}


	public static void setLivingsPanelActive (int iType, int iSoldiersGroup, int iCitizenGroup) {
		if (iType != LIVINGS_PANEL_TYPE_NONE) {
			createLivingsPanel (iType, iSoldiersGroup, iCitizenGroup);
		}
		UIPanel.livingsPanelActive = iType;
		UIPanel.livingsPanelCitizensGroupActive = iCitizenGroup;
		UIPanel.livingsPanelSoldiersGroupActive = iSoldiersGroup;

		if (iType != -1) {
			closePanels (true, true, true, true, false, true, true);
		}
	}


	public static boolean isLivingsPanelActive () {
		return livingsPanelActive != -1;
	}


	public static int getLivingsPanelActive () {
		return livingsPanelActive;
	}


	private static void closePanels (boolean bPriorities, boolean bTrade, boolean bMessages, boolean bMats, boolean bLivings, boolean bPile, boolean bProfessions) {
		if (bPriorities) {
			setPrioritiesPanelActive (false);
		}
		if (bTrade) {
			setTradePanelActive (false);
		}
		if (bMessages) {
			setMessagesPanelActive (-1);
		}
		if (bMats) {
			setMatsPanelActive (false);
		}
		if (bLivings) {
			setLivingsPanelActive (LIVINGS_PANEL_TYPE_NONE, livingsPanelSoldiersGroupActive, livingsPanelCitizensGroupActive);
		}
		if (bPile) {
			setPilePanelActive (-1, false);
		}
		if (bProfessions) {
			setProfessionsPanelActive (-1, false);
		}
	}


	private void createBottomSubPanel (SmartMenu smItem) {
		int iMaxItems = smItem.getItems ().size ();
		BOTTOM_SUBPANEL_WIDTH = (menuPanelPoint.x - bottomPanelX) - 2 * PIXELS_TO_BORDER;

		BOTTOM_SUBPANEL_NUM_ITEMS_X = (BOTTOM_SUBPANEL_WIDTH - PIXELS_TO_BORDER) / (BOTTOM_SUBITEM_WIDTH + PIXELS_TO_BORDER);
		if (BOTTOM_SUBPANEL_NUM_ITEMS_X < 1) {
			BOTTOM_SUBPANEL_NUM_ITEMS_X = 1;
		} else if (BOTTOM_SUBPANEL_NUM_ITEMS_X > iMaxItems) {
			BOTTOM_SUBPANEL_NUM_ITEMS_X = iMaxItems;
		}
		BOTTOM_SUBPANEL_WIDTH = BOTTOM_SUBPANEL_NUM_ITEMS_X * (BOTTOM_SUBITEM_WIDTH + PIXELS_TO_BORDER) + PIXELS_TO_BORDER;

		BOTTOM_SUBPANEL_NUM_ITEMS_Y = iMaxItems / BOTTOM_SUBPANEL_NUM_ITEMS_X;
		if (iMaxItems % BOTTOM_SUBPANEL_NUM_ITEMS_X != 0) {
			BOTTOM_SUBPANEL_NUM_ITEMS_Y++;
		}
		BOTTOM_SUBPANEL_HEIGHT = BOTTOM_SUBPANEL_NUM_ITEMS_Y * (BOTTOM_SUBITEM_HEIGHT + PIXELS_TO_BORDER) + PIXELS_TO_BORDER;

		bottomSubPanelPoint.setLocation (bottomPanelX, bottomPanelY - PIXELS_TO_BORDER - BOTTOM_SUBPANEL_HEIGHT);
		bottomSubPanelItemsPosition = new ArrayList<Point> ();
		bucle1: for (int y1 = 0; y1 < BOTTOM_SUBPANEL_NUM_ITEMS_Y; y1++) {
			for (int x1 = 0; x1 < BOTTOM_SUBPANEL_NUM_ITEMS_X; x1++) {
				if ((y1 * BOTTOM_SUBPANEL_NUM_ITEMS_X + x1) < smItem.getItems ().size ()) {
					bottomSubPanelItemsPosition.add (new Point (bottomSubPanelPoint.x + PIXELS_TO_BORDER + (x1 * (BOTTOM_SUBITEM_WIDTH + PIXELS_TO_BORDER)), bottomSubPanelPoint.y + PIXELS_TO_BORDER + (y1 * (BOTTOM_SUBITEM_HEIGHT + PIXELS_TO_BORDER))));
				} else {
					break bucle1;
				}
			}
		}

		createProductionPanel (productionPanelMenu);
	}


	private void createMenuPanel (SmartMenu menu) {
		MENU_PANEL_HEIGHT = renderHeight - (minimapPanelY + MINIMAP_PANEL_HEIGHT + 2 * PIXELS_TO_BORDER) - BOTTOM_PANEL_HEIGHT - 2 * PIXELS_TO_BORDER;
		MENU_PANEL_NUM_ITEMS_Y = (MENU_PANEL_HEIGHT - PIXELS_TO_BORDER) / (MENU_ITEM_HEIGHT + PIXELS_TO_BORDER);
		if (MENU_PANEL_NUM_ITEMS_Y < 1) {
			MENU_PANEL_NUM_ITEMS_Y = 1;
		}
		MENU_PANEL_HEIGHT = MENU_PANEL_NUM_ITEMS_Y * (MENU_ITEM_HEIGHT + PIXELS_TO_BORDER) + PIXELS_TO_BORDER;

		int iMaxItems = menu.getItems ().size ();
		MENU_PANEL_NUM_ITEMS_X = (iMaxItems / MENU_PANEL_NUM_ITEMS_Y);
		if ((iMaxItems % MENU_PANEL_NUM_ITEMS_Y) != 0) {
			MENU_PANEL_NUM_ITEMS_X++;
		}
		MENU_PANEL_WIDTH = MENU_PANEL_NUM_ITEMS_X * (MENU_ITEM_WIDTH + PIXELS_TO_BORDER) + PIXELS_TO_BORDER;

		while (((MENU_PANEL_NUM_ITEMS_Y - 1) * MENU_PANEL_NUM_ITEMS_X) >= iMaxItems) {
			MENU_PANEL_HEIGHT -= (MENU_ITEM_HEIGHT + PIXELS_TO_BORDER);
			MENU_PANEL_NUM_ITEMS_Y--;
		}

		menuPanelPoint.setLocation (renderWidth - MENU_PANEL_WIDTH - tileOpenRightMenu.getTileWidth (), minimapPanelY + MINIMAP_PANEL_HEIGHT + 2 * PIXELS_TO_BORDER);

		// Positions
		menuPanelItemsPosition = new ArrayList<Point> ();
		for (int y = 0; y < MENU_PANEL_NUM_ITEMS_Y; y++) {
			for (int x = 0; x < MENU_PANEL_NUM_ITEMS_X; x++) {
				menuPanelItemsPosition.add (new Point (menuPanelPoint.x + PIXELS_TO_BORDER + (x * (MENU_ITEM_WIDTH + PIXELS_TO_BORDER)), menuPanelPoint.y + PIXELS_TO_BORDER + (y * (MENU_ITEM_HEIGHT + PIXELS_TO_BORDER))));
			}
		}

		// Minibotón para abrir/cerrar el menú
		tileOpenCloseRightMenuPoint.setLocation (renderWidth - tileOpenRightMenu.getTileWidth (), renderHeight / 2 - tileOpenRightMenu.getTileHeight () / 2);
	}


	public static boolean isBottomMenuPanelActive () {
		return UIPanel.bottomMenuPanelActive;
	}


	public static void setBottomMenuPanelActive (boolean bottomMenuPanelActive) {
		setBottomMenuPanelActive (bottomMenuPanelActive, false);
	}


	public static void setBottomMenuPanelActive (boolean bottomMenuPanelActive, boolean bInitializing) {
		UIPanel.bottomMenuPanelActive = bottomMenuPanelActive;
		if (!bInitializing) {
			createProductionPanel (productionPanelMenu);
		}
	}


	public static void setBottomMenuPanelLocked (boolean bottomMenuPanelLocked) {
		UIPanel.bottomMenuPanelLocked = bottomMenuPanelLocked;
	}


	public static boolean isBottomMenuPanelLocked () {
		return bottomMenuPanelLocked;
	}


	public static boolean isMenuPanelActive () {
		return UIPanel.menuPanelActive;
	}


	public static void setMenuPanelActive (boolean menuPanelActive) {
		UIPanel.menuPanelActive = menuPanelActive;
	}


	public static void setMenuPanelLocked (boolean menuPanelLocked) {
		UIPanel.menuPanelLocked = menuPanelLocked;
	}


	public static boolean isMenuPanelLocked () {
		return menuPanelLocked;
	}


	private static void createProductionPanel (SmartMenu menu) {
		int iFirstY = iconNumCitizensBackgroundPoint.y + tileBottomItem.getTileHeight () + tileBottomItem.getTileHeight () / 4;
		int iLastY;
		if (bottomSubPanelMenu != null && isBottomMenuPanelActive ()) {
			iLastY = bottomSubPanelPoint.y;
		} else {
			iLastY = bottomPanelY;
		}
		PRODUCTION_PANEL_HEIGHT = iLastY - iFirstY - PIXELS_TO_BORDER * 2;

		PRODUCTION_PANEL_WIDTH = menuPanelPoint.x - PIXELS_TO_BORDER * 4;

		PRODUCTION_PANEL_NUM_ITEMS_Y = (PRODUCTION_PANEL_HEIGHT - PIXELS_TO_BORDER) / (PRODUCTION_PANEL_ITEM_HEIGHT + PIXELS_TO_BORDER);
		if (PRODUCTION_PANEL_NUM_ITEMS_Y < 1) {
			PRODUCTION_PANEL_NUM_ITEMS_Y = 1;
		}
		PRODUCTION_PANEL_HEIGHT = PRODUCTION_PANEL_NUM_ITEMS_Y * (PRODUCTION_PANEL_ITEM_HEIGHT + PIXELS_TO_BORDER) + PIXELS_TO_BORDER;

		int iMaxItems = menu.getItems ().size ();
		PRODUCTION_PANEL_NUM_ITEMS_X = (iMaxItems / PRODUCTION_PANEL_NUM_ITEMS_Y);
		if ((iMaxItems % PRODUCTION_PANEL_NUM_ITEMS_Y) != 0) {
			PRODUCTION_PANEL_NUM_ITEMS_X++;
		}
		PRODUCTION_PANEL_WIDTH = PRODUCTION_PANEL_NUM_ITEMS_X * (PRODUCTION_PANEL_ITEM_WIDTH + PIXELS_TO_BORDER + 2 * ICON_WIDTH) + PIXELS_TO_BORDER;

		while (((PRODUCTION_PANEL_NUM_ITEMS_Y - 1) * PRODUCTION_PANEL_NUM_ITEMS_X) >= iMaxItems) {
			PRODUCTION_PANEL_HEIGHT -= (PRODUCTION_PANEL_ITEM_HEIGHT + PIXELS_TO_BORDER);
			PRODUCTION_PANEL_NUM_ITEMS_Y--;
		}

		productionPanelPoint.setLocation (tileOpenProductionPanel.getTileWidth (), iFirstY + ((iLastY - iFirstY) / 2) - PRODUCTION_PANEL_HEIGHT / 2);

		// Positions
		productionPanelItemsPosition.clear ();
		productionPanelItemsPlusRegularPosition.clear ();
		productionPanelItemsMinusRegularPosition.clear ();
		productionPanelItemsPlusAutomatedPosition.clear ();
		productionPanelItemsMinusAutomatedPosition.clear ();
		Point p;
		SmartMenu smItem;
		int iMenu;
		bucle1: for (int y = 0; y < PRODUCTION_PANEL_NUM_ITEMS_Y; y++) {
			for (int x = 0; x < PRODUCTION_PANEL_NUM_ITEMS_X; x++) {
				iMenu = (y * PRODUCTION_PANEL_NUM_ITEMS_X) + x;
				if (iMenu >= productionPanelMenu.getItems ().size ()) {
					break bucle1;
				}
				smItem = productionPanelMenu.getItems ().get (iMenu);

				p = new Point (productionPanelPoint.x + PIXELS_TO_BORDER + ICON_WIDTH + (x * (PRODUCTION_PANEL_ITEM_WIDTH + PIXELS_TO_BORDER + 2 * ICON_WIDTH)), productionPanelPoint.y + PIXELS_TO_BORDER + (y * (PRODUCTION_PANEL_ITEM_HEIGHT + PIXELS_TO_BORDER)));
				productionPanelItemsPosition.add (p);
				if (smItem.getType () == SmartMenu.TYPE_ITEM && !smItem.getCommand ().equalsIgnoreCase (CommandPanel.COMMAND_BACK)) {
					productionPanelItemsPlusRegularPosition.add (new Point (p.x - ICON_WIDTH, p.y));
					productionPanelItemsMinusRegularPosition.add (new Point (p.x - ICON_WIDTH, p.y + PRODUCTION_PANEL_ITEM_HEIGHT - ICON_HEIGHT));
					productionPanelItemsPlusAutomatedPosition.add (new Point (p.x + PRODUCTION_PANEL_ITEM_WIDTH, p.y));
					productionPanelItemsMinusAutomatedPosition.add (new Point (p.x + PRODUCTION_PANEL_ITEM_WIDTH, p.y + PRODUCTION_PANEL_ITEM_HEIGHT - ICON_HEIGHT));
				} else {
					productionPanelItemsPlusRegularPosition.add (new Point (-1, -1));
					productionPanelItemsMinusRegularPosition.add (new Point (-1, -1));
					productionPanelItemsPlusAutomatedPosition.add (new Point (-1, -1));
					productionPanelItemsMinusAutomatedPosition.add (new Point (-1, -1));
				}
			}
		}

		// Minibotón para abrir/cerrar el menú de producción
		tileOpenCloseProductionPanelPoint.setLocation (0, renderHeight / 2 - tileOpenProductionPanel.getTileHeight () / 2);


		// Tutorial?
		ImagesPanel.resize (renderWidth, renderHeight);
	}


	private void createTradePanel () {
		TRADE_PANEL_WIDTH = (renderWidth / 8) * 7;
		TRADE_PANEL_HEIGHT = renderHeight - (iconNumCitizensBackgroundPoint.y + tileBottomItem.getTileHeight ()) - tileBottomItem.getTileHeight () / 2;
		tradePanelPoint.setLocation (renderWidth / 8 - ((renderWidth / 8) / 2), iconNumCitizensBackgroundPoint.y + tileBottomItem.getTileHeight () + tileBottomItem.getTileHeight () / 4);
		tradePanelClosePoint.setLocation (tradePanelPoint.x + TRADE_PANEL_WIDTH - tileButtonClose.getTileWidth (), tradePanelPoint.y);

		TradePanel.loadStatics ();
		if (tradePanel != null && Game.getWorld () != null && Game.getWorld ().getCurrentCaravanData () != null) {
			tradePanel.resize (Game.getWorld ().getCurrentCaravanData ());
		}
	}


	public static void createTradePanelContent (CaravanData caravanData) {
		if (caravanData != null) {
			if (tradePanel == null) {
				tradePanel = new TradePanel (caravanData, tradePanelPoint, TRADE_PANEL_WIDTH, TRADE_PANEL_HEIGHT);
			} else {
				tradePanel.resize (caravanData);
			}
		}
	}


	private void createMessagesPanel () {
		messagesPanelActive = -1;

		// Tamaño y close button
		MESSAGES_PANEL_WIDTH = (renderWidth / 8) * 7;
		MESSAGES_PANEL_HEIGHT = renderHeight - (iconNumCitizensBackgroundPoint.y + tileBottomItem.getTileHeight ()) - tileBottomItem.getTileHeight () / 2;
		messagesPanelPoint.setLocation (renderWidth / 8 - ((renderWidth / 8) / 2), iconNumCitizensBackgroundPoint.y + tileBottomItem.getTileHeight () + tileBottomItem.getTileHeight () / 4);
		messagesPanelClosePoint.setLocation (messagesPanelPoint.x + MESSAGES_PANEL_WIDTH - tileButtonClose.getTileWidth (), messagesPanelPoint.y);

		// Mini iconos y puntos (los que salen arriba a la izquierda de la pantalla, no los de dentro del panel)
		if (messageTiles == null) {
			messageTiles = new Tile [MessagesPanel.MAX_TYPES];
			messageTilesON = new Tile [MessagesPanel.MAX_TYPES];
			messageTilesAlpha = new ArrayList<boolean[][]> (MessagesPanel.MAX_TYPES);

			for (int i = 0; i < MessagesPanel.MAX_TYPES; i++) {
				messageTiles[i] = new Tile ("icon_messages" + i); //$NON-NLS-1$
				messageTilesON[i] = new Tile ("icon_messages" + i + "ON"); //$NON-NLS-1$ //$NON-NLS-2$
				messageTilesAlpha.add (UtilsGL.generateAlpha (messageTiles[i]));
			}
		}

		messageIconPoints = new Point [MessagesPanel.MAX_TYPES];
		for (int i = 0; i < MessagesPanel.MAX_TYPES; i++) {
			messageIconPoints[i] = new Point (PIXELS_TO_BORDER + i * (messageTiles[i].getTileWidth () + PIXELS_TO_BORDER), PIXELS_TO_BORDER);
		}

		// Iconos dentro del panel
		// Los "tabs" (iconos gordos arriba)
		if (messagePanelTiles == null) {
			messagePanelTiles = new Tile [MessagesPanel.MAX_TYPES];
			messagePanelTilesON = new Tile [MessagesPanel.MAX_TYPES];
			messagePanelTilesAlpha = new ArrayList<boolean[][]> (MessagesPanel.MAX_TYPES);

			for (int i = 0; i < MessagesPanel.MAX_TYPES; i++) {
				messagePanelTiles[i] = new Tile ("icon_big_messages" + i); //$NON-NLS-1$
				messagePanelTilesON[i] = new Tile ("icon_big_messages" + i + "ON"); //$NON-NLS-1$ //$NON-NLS-2$
				messagePanelTilesAlpha.add (UtilsGL.generateAlpha (messagePanelTiles[i]));
			}
		}

		// Scroll up/down
		messagePanelIconScrollUpPoint.setLocation (messagesPanelPoint.x + MESSAGES_PANEL_WIDTH - tileMessagesPanel[3].getTileWidth () - tileScrollUp.getTileWidth (), messagesPanelPoint.y + tileMessagesPanel[1].getTileHeight () + messagePanelTiles[0].getTileHeight ());
		messagePanelIconScrollDownPoint.setLocation (messagesPanelPoint.x + MESSAGES_PANEL_WIDTH - tileMessagesPanel[3].getTileWidth () - tileScrollUp.getTileWidth (), messagesPanelPoint.y + MESSAGES_PANEL_HEIGHT - tileScrollDown.getTileHeight () - tileMessagesPanel[1].getTileHeight ());

		// Pages
		messagePanelPagesPositionPoint.setLocation (messagePanelIconScrollUpPoint.x + tileScrollUp.getTileWidth () / 2, messagePanelIconScrollUpPoint.y + tileScrollUp.getTileHeight () + (messagePanelIconScrollDownPoint.y - (messagePanelIconScrollUpPoint.y + tileScrollUp.getTileHeight ())) / 2 - UtilFont.MAX_HEIGHT / 2);

		// Subpanel
		MESSAGES_PANEL_SUBPANEL_WIDTH = MESSAGES_PANEL_WIDTH - (3 * tileMessagesPanel[3].getTileWidth () + tileScrollUp.getTileWidth ());
		MESSAGES_PANEL_SUBPANEL_HEIGHT = (messagePanelIconScrollDownPoint.y + tileScrollDown.getTileHeight ()) - messagePanelIconScrollUpPoint.y;
		messagesPanelSubPanelPoint.setLocation (messagesPanelPoint.x + tileMessagesPanel[3].getTileWidth (), messagePanelIconScrollUpPoint.y);

		// Posición de iconos (los 4 de arriba) dentro del panel (va aquí pq tienen que centrarse con el subpanel)
		messagePanelIconPoints = new Point [MessagesPanel.MAX_TYPES];
		int iSeparation = (MESSAGES_PANEL_SUBPANEL_WIDTH - (MessagesPanel.MAX_TYPES * messagePanelTiles[0].getTileWidth ())) / (MessagesPanel.MAX_TYPES + 1);
		for (int i = 0; i < MessagesPanel.MAX_TYPES; i++) {
			messagePanelIconPoints[i] = new Point (messagesPanelSubPanelPoint.x + iSeparation + (i * (messagePanelTiles[0].getTileWidth () + iSeparation)), messagesPanelPoint.y + tileMessagesPanel[1].getTileHeight ());
		}

		// Ésto es para que parta los messages render
		MessagesPanel.resize (MESSAGES_PANEL_WIDTH, MESSAGES_PANEL_HEIGHT);
	}


	private void createMatsPanel () {
		// Groups, si peta sale del juego
		MatsPanelData.loadGroups ();

		MATS_PANEL_WIDTH = (renderWidth / 8) * 7;
		MATS_PANEL_HEIGHT = renderHeight - (iconMatsPoint.y + tileIconMats.getTileHeight ()) - tileBottomItem.getTileHeight () - tileBottomItem.getTileHeight () / 2;

		matsPanelPoint.setLocation (renderWidth / 8 - ((renderWidth / 8) / 2), iconMatsPoint.y + tileIconMats.getTileHeight () + tileBottomItem.getTileHeight () / 4);
		matsPanelClosePoint.setLocation (matsPanelPoint.x + MATS_PANEL_WIDTH - tileButtonClose.getTileWidth (), matsPanelPoint.y);

		// Iconos dentro del panel
		// Los "tabs" (iconos gordos arriba)
		if (matsPanelTiles == null) {
			matsPanelTiles = new Tile [MatsPanelData.numGroups];
			matsPanelTilesON = new Tile [MatsPanelData.numGroups];
			for (int i = 0; i < MatsPanelData.numGroups; i++) {
				matsPanelTiles[i] = new Tile (MatsPanelData.iconGroups.get (i));
				matsPanelTilesON[i] = new Tile (MatsPanelData.iconGroups.get (i) + "ON"); //$NON-NLS-1$
			}
		}

		matsLastGroup = 0;

		// Scroll up/down
		matsPanelIconScrollUpPoint.setLocation (matsPanelPoint.x + MATS_PANEL_WIDTH - tileMatsPanel[3].getTileWidth () - tileScrollUp.getTileWidth (), matsPanelPoint.y + tileMatsPanel[1].getTileHeight () + tileBottomItem.getTileHeight ());
		matsPanelIconScrollDownPoint.setLocation (matsPanelPoint.x + MATS_PANEL_WIDTH - tileMatsPanel[3].getTileWidth () - tileScrollUp.getTileWidth (), matsPanelPoint.y + MATS_PANEL_HEIGHT - tileScrollDown.getTileHeight () - tileMatsPanel[1].getTileHeight ());

		// Pages
		matsPanelPagesPositionPoint.setLocation (matsPanelIconScrollUpPoint.x + tileScrollUp.getTileWidth () / 2, matsPanelIconScrollUpPoint.y + tileScrollUp.getTileHeight () + (matsPanelIconScrollDownPoint.y - (matsPanelIconScrollUpPoint.y + tileScrollUp.getTileHeight ())) / 2 - UtilFont.MAX_HEIGHT / 2);

		// Subpanel
		MATS_PANEL_SUBPANEL_WIDTH = MATS_PANEL_WIDTH - (3 * tileMatsPanelSubPanel[3].getTileWidth () + tileScrollUp.getTileWidth ());
		MATS_PANEL_SUBPANEL_HEIGHT = (matsPanelIconScrollDownPoint.y + tileScrollDown.getTileHeight ()) - matsPanelIconScrollUpPoint.y;
		matsPanelSubPanelPoint.setLocation (matsPanelPoint.x + tileMatsPanelSubPanel[3].getTileWidth (), matsPanelIconScrollUpPoint.y);

		// Posición de iconos (los X de arriba) dentro del panel (va aquí pq tienen que centrarse con el subpanel)
		matsPanelIconPoints = new Point [MatsPanelData.numGroups];
		int iSeparation = (MATS_PANEL_SUBPANEL_WIDTH - (MatsPanelData.numGroups * tileBottomItem.getTileWidth ())) / (MatsPanelData.numGroups + 1);
		for (int i = 0; i < MatsPanelData.numGroups; i++) {
			matsPanelIconPoints[i] = new Point (matsPanelSubPanelPoint.x + iSeparation + (i * (tileBottomItem.getTileWidth () + iSeparation)), matsPanelPoint.y + tileMatsPanel[1].getTileHeight ());
		}

		// Ahora miramos de cuantas filas y columnas disponemos y seteamos el array de posiciones
		int iMaxItemsWidth = (MATS_PANEL_SUBPANEL_WIDTH - 2 * tileMatsPanelSubPanel[3].getTileWidth ()) / (tileBottomItem.getTileWidth () + 8);
		int iMaxItemsHeight = (MATS_PANEL_SUBPANEL_HEIGHT - 2 * tileMatsPanelSubPanel[1].getTileHeight ()) / (tileBottomItem.getTileHeight () + 8);

		MATS_PANEL_MAX_ITEMS_PER_PAGE = iMaxItemsWidth * iMaxItemsHeight;
		matsPanelItemPoints = new Point [MATS_PANEL_MAX_ITEMS_PER_PAGE];
		int iSeparationW = (MATS_PANEL_SUBPANEL_WIDTH - 2 * tileMatsPanelSubPanel[3].getTileWidth () - iMaxItemsWidth * tileBottomItem.getTileWidth ()) / (iMaxItemsWidth + 1);
		int iSeparationH = (MATS_PANEL_SUBPANEL_HEIGHT - 2 * tileMatsPanelSubPanel[1].getTileHeight () - iMaxItemsHeight * tileBottomItem.getTileHeight ()) / (iMaxItemsHeight + 1);
		int iFirstWidth = matsPanelSubPanelPoint.x + tileMatsPanelSubPanel[3].getTileWidth () + iSeparationW;
		int iFirstHeight = matsPanelSubPanelPoint.y + tileMatsPanelSubPanel[1].getTileHeight () + iSeparationH;
		int x = iFirstWidth;
		int y = iFirstHeight;
		for (int i = 0; i < MATS_PANEL_MAX_ITEMS_PER_PAGE; i++) {
			matsPanelItemPoints[i] = new Point (x, y);
			x += (tileBottomItem.getTileWidth () + iSeparationW);
			if (x > (matsPanelSubPanelPoint.x + MATS_PANEL_SUBPANEL_WIDTH - tileMatsPanelSubPanel[3].getTileWidth () - tileBottomItem.getTileWidth () - 1)) {
				y += (tileBottomItem.getTileHeight () + iSeparationH);
				x = iFirstWidth;
			}
		}

		// Pages
		matsNumPages = new int [MatsPanelData.numGroups];
		matsIndexPages = new int [MatsPanelData.numGroups];
		for (int i = 0; i < MatsPanelData.numGroups; i++) {
			if (MatsPanelData.tileGroups.get (i).size () % MATS_PANEL_MAX_ITEMS_PER_PAGE == 0) {
				matsNumPages[i] = MatsPanelData.tileGroups.get (i).size () / MATS_PANEL_MAX_ITEMS_PER_PAGE;
			} else {
				matsNumPages[i] = (MatsPanelData.tileGroups.get (i).size () / MATS_PANEL_MAX_ITEMS_PER_PAGE) + 1;
			}

			matsIndexPages[i] = 0;
		}
	}


	private static void recheckPilePanelPages () {
		if (menuPile != null) {
			pilePanelPageIndex = 0;
			pilePanelMaxPages = (menuPile.getItems ().size () / PILE_PANEL_MAX_ITEMS_PER_PAGE) + 1;
			if ((menuPile.getItems ().size () % PILE_PANEL_MAX_ITEMS_PER_PAGE) == 0) {
				pilePanelMaxPages--;
			}
		}
	}


	private static void createPilePanel () {
		PILE_PANEL_WIDTH = (renderWidth / 8) * 7;
		PILE_PANEL_HEIGHT = renderHeight - (iconMatsPoint.y + tileIconMats.getTileHeight ()) - tileBottomItem.getTileHeight () - tileBottomItem.getTileHeight () / 2;

		pilePanelPoint.setLocation (renderWidth / 8 - ((renderWidth / 8) / 2), iconMatsPoint.y + tileIconMats.getTileHeight () + tileBottomItem.getTileHeight () / 4);
		pilePanelClosePoint.setLocation (pilePanelPoint.x + PILE_PANEL_WIDTH - tileButtonClose.getTileWidth (), pilePanelPoint.y);

		pilePanelPileContainerIDActive = -1;
		pilePanelIsContainer = false;
		pilePanelIsLocked = false;

		// Scroll up/down
		pilePanelIconScrollUpPoint.setLocation (pilePanelPoint.x + PILE_PANEL_WIDTH - tileMatsPanel[3].getTileWidth () - tileScrollUp.getTileWidth (), pilePanelPoint.y + tileMatsPanel[1].getTileHeight ());
		pilePanelIconScrollDownPoint.setLocation (pilePanelPoint.x + PILE_PANEL_WIDTH - tileMatsPanel[3].getTileWidth () - tileScrollUp.getTileWidth (), pilePanelPoint.y + PILE_PANEL_HEIGHT - tileScrollDown.getTileHeight () - tileMatsPanel[1].getTileHeight ());

		// Configuration buttons
		pilePanelIconConfigCopyPoint.setLocation (pilePanelPoint.x + PILE_PANEL_WIDTH / 2 - 3 * tileConfigCopy.getTileWidth (), pilePanelPoint.y + PILE_PANEL_HEIGHT - tileConfigCopy.getTileHeight () - UtilFont.MAX_HEIGHT / 2);
		pilePanelIconConfigLockPoint.setLocation (pilePanelPoint.x + PILE_PANEL_WIDTH / 2 - tileConfigCopy.getTileWidth () - tileConfigCopy.getTileWidth () / 2, pilePanelPoint.y + PILE_PANEL_HEIGHT - tileConfigLock.getTileHeight () - UtilFont.MAX_HEIGHT / 2);
		pilePanelIconConfigUnlockAllPoint.setLocation (pilePanelPoint.x + PILE_PANEL_WIDTH / 2 + tileConfigCopy.getTileWidth () / 2, pilePanelPoint.y + PILE_PANEL_HEIGHT - tileConfigLock.getTileHeight () - UtilFont.MAX_HEIGHT / 2);
		pilePanelIconConfigLockAllPoint.setLocation (pilePanelPoint.x + PILE_PANEL_WIDTH / 2 + 2 * tileConfigCopy.getTileWidth (), pilePanelPoint.y + PILE_PANEL_HEIGHT - tileConfigLock.getTileHeight () - UtilFont.MAX_HEIGHT / 2);
		
		// Pages
		pilePanelPagesPositionPoint.setLocation (pilePanelIconScrollUpPoint.x + tileScrollUp.getTileWidth () / 2, pilePanelIconScrollUpPoint.y + tileScrollUp.getTileHeight () + (pilePanelIconScrollDownPoint.y - (pilePanelIconScrollUpPoint.y + tileScrollUp.getTileHeight ())) / 2 - UtilFont.MAX_HEIGHT / 2);

		// Miramos de cuantas filas y columnas disponemos y seteamos el array de posiciones
		int iMaxItemsWidth = (PILE_PANEL_WIDTH - tileScrollUp.getTileWidth () - 4 * tileMatsPanelSubPanel[3].getTileWidth ()) / (tileBottomItem.getTileWidth () + 8);
		int iMaxItemsHeight = (PILE_PANEL_HEIGHT - 2 * tileMatsPanelSubPanel[1].getTileHeight ()) / (tileBottomItem.getTileHeight () + 8);

		PILE_PANEL_MAX_ITEMS_PER_PAGE = iMaxItemsWidth * iMaxItemsHeight;
		pilePanelItemPoints = new Point [PILE_PANEL_MAX_ITEMS_PER_PAGE];
		int iSeparationW = (PILE_PANEL_WIDTH - tileScrollUp.getTileWidth () - 4 * tileMatsPanelSubPanel[3].getTileWidth () - iMaxItemsWidth * tileBottomItem.getTileWidth ()) / (iMaxItemsWidth + 1);
		int iSeparationH = (PILE_PANEL_HEIGHT - 2 * tileMatsPanelSubPanel[1].getTileHeight () - iMaxItemsHeight * tileBottomItem.getTileHeight ()) / (iMaxItemsHeight + 1);
		int iFirstWidth = pilePanelPoint.x + tileMatsPanelSubPanel[3].getTileWidth () + iSeparationW;
		int iFirstHeight = pilePanelPoint.y + tileMatsPanelSubPanel[1].getTileHeight () + iSeparationH;
		// int x = iFirstWidth;
		// int y = iFirstHeight;
		int i = 0;
		for (int y = 0; y < iMaxItemsHeight; y++) {
			for (int x = 0; x < iMaxItemsWidth; x++) {
				pilePanelItemPoints[i] = new Point (iFirstWidth + (x * (tileBottomItem.getTileWidth () + iSeparationW)), iFirstHeight + (y * (tileBottomItem.getTileHeight () + iSeparationH)));
				i++;
			}
		}
	}


	private static void resizePilePanel (SmartMenu menuPile) {
		if (menuPile == null) {
			return;
		}

		PILE_PANEL_WIDTH = (renderWidth / 8) * 7; // Copied from the createPilePanel method
		int iMaxItemsWidth = (PILE_PANEL_WIDTH - tileScrollUp.getTileWidth () - 4 * tileMatsPanelSubPanel[3].getTileWidth ()) / (tileBottomItem.getTileWidth () + 8);
		int iRows = (menuPile.getItems ().size () / iMaxItemsWidth) + 1;
		if (menuPile.getItems ().size () % iMaxItemsWidth == 0) {
			iRows--;
		}

		PILE_PANEL_HEIGHT = renderHeight - (iconMatsPoint.y + tileIconMats.getTileHeight ()) - tileBottomItem.getTileHeight () - tileBottomItem.getTileHeight () / 2;
		int iMaxItemsHeight = (PILE_PANEL_HEIGHT - 2 * tileMatsPanelSubPanel[1].getTileHeight ()) / (tileBottomItem.getTileHeight () + 8);
		int iSeparationH = (PILE_PANEL_HEIGHT - 2 * tileMatsPanelSubPanel[1].getTileHeight () - iMaxItemsHeight * tileBottomItem.getTileHeight ()) / (iMaxItemsHeight + 1);

		int iRowsToDelete = 0;
		if (iMaxItemsHeight <= iRows) {
			iRows = iMaxItemsHeight;
		} else {
			iRowsToDelete = (iMaxItemsHeight - iRows);
		}

		PILE_PANEL_HEIGHT -= (iRowsToDelete * (tileBottomItem.getTileHeight () + iSeparationH));
		// PILE_PANEL_HEIGHT = renderHeight - (iconMatsPoint.y + tileIconMats.getTileHeight ()) - tileBottomItem.getTileHeight () - tileBottomItem.getTileHeight () / 2;

		PILE_PANEL_MAX_ITEMS_PER_PAGE = iMaxItemsWidth * iRows;

		if (iRows <= 1) {
			// Lo hacemos pequeño por la derecha
			int iSeparationW = (PILE_PANEL_WIDTH - tileScrollUp.getTileWidth () - 4 * tileMatsPanelSubPanel[3].getTileWidth () - iMaxItemsWidth * tileBottomItem.getTileWidth ()) / (iMaxItemsWidth + 1);
			int iFirstWidth = pilePanelPoint.x + tileMatsPanelSubPanel[3].getTileWidth () + iSeparationW;
			PILE_PANEL_WIDTH = iFirstWidth + ((menuPile.getItems ().size () + 1) * (tileBottomItem.getTileWidth () + iSeparationW));
		} else {
			PILE_PANEL_WIDTH = (renderWidth / 8) * 7;
		}

		pilePanelClosePoint.setLocation (pilePanelPoint.x + PILE_PANEL_WIDTH - tileButtonClose.getTileWidth (), pilePanelPoint.y);

		// Scroll up/down
		pilePanelIconScrollUpPoint.setLocation (pilePanelPoint.x + PILE_PANEL_WIDTH - tileMatsPanel[3].getTileWidth () - tileScrollUp.getTileWidth (), pilePanelPoint.y + tileMatsPanel[1].getTileHeight ());
		pilePanelIconScrollDownPoint.setLocation (pilePanelPoint.x + PILE_PANEL_WIDTH - tileMatsPanel[3].getTileWidth () - tileScrollUp.getTileWidth (), pilePanelPoint.y + PILE_PANEL_HEIGHT - tileScrollDown.getTileHeight () - tileMatsPanel[1].getTileHeight ());

		// Configuration buttons
		pilePanelIconConfigCopyPoint.setLocation (pilePanelPoint.x + PILE_PANEL_WIDTH / 2 - 3 * tileConfigCopy.getTileWidth (), pilePanelPoint.y + PILE_PANEL_HEIGHT - tileConfigCopy.getTileHeight () - UtilFont.MAX_HEIGHT / 2);
		pilePanelIconConfigLockPoint.setLocation (pilePanelPoint.x + PILE_PANEL_WIDTH / 2 - tileConfigCopy.getTileWidth () - tileConfigCopy.getTileWidth () / 2, pilePanelPoint.y + PILE_PANEL_HEIGHT - tileConfigLock.getTileHeight () - UtilFont.MAX_HEIGHT / 2);
		pilePanelIconConfigUnlockAllPoint.setLocation (pilePanelPoint.x + PILE_PANEL_WIDTH / 2 + tileConfigCopy.getTileWidth () / 2, pilePanelPoint.y + PILE_PANEL_HEIGHT - tileConfigLock.getTileHeight () - UtilFont.MAX_HEIGHT / 2);
		pilePanelIconConfigLockAllPoint.setLocation (pilePanelPoint.x + PILE_PANEL_WIDTH / 2 + 2 * tileConfigCopy.getTileWidth (), pilePanelPoint.y + PILE_PANEL_HEIGHT - tileConfigLock.getTileHeight () - UtilFont.MAX_HEIGHT / 2);

		// Pages
		pilePanelPagesPositionPoint.setLocation (pilePanelIconScrollUpPoint.x + tileScrollUp.getTileWidth () / 2, pilePanelIconScrollUpPoint.y + tileScrollUp.getTileHeight () + (pilePanelIconScrollDownPoint.y - (pilePanelIconScrollUpPoint.y + tileScrollUp.getTileHeight ())) / 2 - UtilFont.MAX_HEIGHT / 2);
	}


	private static void recheckProfessionsPanelPages () {
		if (menuProfessions != null) {
			professionsPanelPageIndex = 0;
			professionsPanelMaxPages = (menuProfessions.getItems ().size () / PROFESSIONS_PANEL_MAX_ITEMS_PER_PAGE) + 1;
			if ((menuProfessions.getItems ().size () % PROFESSIONS_PANEL_MAX_ITEMS_PER_PAGE) == 0) {
				professionsPanelMaxPages--;
			}
		}
	}


	private static void createProfessionsPanel () {
		PROFESSIONS_PANEL_WIDTH = (renderWidth / 8) * 7;
		PROFESSIONS_PANEL_HEIGHT = renderHeight - (iconMatsPoint.y + tileIconMats.getTileHeight ()) - tileBottomItem.getTileHeight () - tileBottomItem.getTileHeight () / 2;

		professionsPanelPoint.setLocation (renderWidth / 8 - ((renderWidth / 8) / 2), iconMatsPoint.y + tileIconMats.getTileHeight () + tileBottomItem.getTileHeight () / 4);
		professionsPanelClosePoint.setLocation (professionsPanelPoint.x + PROFESSIONS_PANEL_WIDTH - tileButtonClose.getTileWidth (), professionsPanelPoint.y);

		professionsPanelCitizenOrGroupIDActive = -1;
		professionsPanelIsCitizen = false;

		// Scroll up/down
		professionsPanelIconScrollUpPoint.setLocation (professionsPanelPoint.x + PROFESSIONS_PANEL_WIDTH - tileMatsPanel[3].getTileWidth () - tileScrollUp.getTileWidth (), professionsPanelPoint.y + tileMatsPanel[1].getTileHeight ());
		professionsPanelIconScrollDownPoint.setLocation (professionsPanelPoint.x + PROFESSIONS_PANEL_WIDTH - tileMatsPanel[3].getTileWidth () - tileScrollUp.getTileWidth (), professionsPanelPoint.y + PROFESSIONS_PANEL_HEIGHT - tileScrollDown.getTileHeight () - tileMatsPanel[1].getTileHeight ());

		// Pages
		professionsPanelPagesPositionPoint.setLocation (professionsPanelIconScrollUpPoint.x + tileScrollUp.getTileWidth () / 2, professionsPanelIconScrollUpPoint.y + tileScrollUp.getTileHeight () + (professionsPanelIconScrollDownPoint.y - (professionsPanelIconScrollUpPoint.y + tileScrollUp.getTileHeight ())) / 2 - UtilFont.MAX_HEIGHT / 2);

		// Miramos de cuantas filas y columnas disponemos y seteamos el array de posiciones
		int iMaxItemsWidth = (PROFESSIONS_PANEL_WIDTH - tileScrollUp.getTileWidth () - 4 * tileMatsPanelSubPanel[3].getTileWidth ()) / (tileBottomItem.getTileWidth () + 8);
		int iMaxItemsHeight = (PROFESSIONS_PANEL_HEIGHT - 2 * tileMatsPanelSubPanel[1].getTileHeight ()) / (tileBottomItem.getTileHeight () + 8);

		PROFESSIONS_PANEL_MAX_ITEMS_PER_PAGE = iMaxItemsWidth * iMaxItemsHeight;
		professionsPanelItemPoints = new Point [PROFESSIONS_PANEL_MAX_ITEMS_PER_PAGE];
		int iSeparationW = (PROFESSIONS_PANEL_WIDTH - tileScrollUp.getTileWidth () - 4 * tileMatsPanelSubPanel[3].getTileWidth () - iMaxItemsWidth * tileBottomItem.getTileWidth ()) / (iMaxItemsWidth + 1);
		int iSeparationH = (PROFESSIONS_PANEL_HEIGHT - 2 * tileMatsPanelSubPanel[1].getTileHeight () - iMaxItemsHeight * tileBottomItem.getTileHeight ()) / (iMaxItemsHeight + 1);
		int iFirstWidth = professionsPanelPoint.x + tileMatsPanelSubPanel[3].getTileWidth () + iSeparationW;
		int iFirstHeight = professionsPanelPoint.y + tileMatsPanelSubPanel[1].getTileHeight () + iSeparationH;
		// int x = iFirstWidth;
		// int y = iFirstHeight;
		int i = 0;
		for (int y = 0; y < iMaxItemsHeight; y++) {
			for (int x = 0; x < iMaxItemsWidth; x++) {
				professionsPanelItemPoints[i] = new Point (iFirstWidth + (x * (tileBottomItem.getTileWidth () + iSeparationW)), iFirstHeight + (y * (tileBottomItem.getTileHeight () + iSeparationH)));
				i++;
			}
		}
	}


	private static void resizeProfessionsPanel (SmartMenu menuProfessions) {
		if (menuProfessions == null) {
			return;
		}

		int iMaxItemsWidth = (PROFESSIONS_PANEL_WIDTH - tileScrollUp.getTileWidth () - 4 * tileMatsPanelSubPanel[3].getTileWidth ()) / (tileBottomItem.getTileWidth () + 8);
		int iRows = (menuProfessions.getItems ().size () / iMaxItemsWidth) + 1;
		if (menuProfessions.getItems ().size () % iMaxItemsWidth == 0) {
			iRows--;
		}

		PROFESSIONS_PANEL_HEIGHT = renderHeight - (iconMatsPoint.y + tileIconMats.getTileHeight ()) - tileBottomItem.getTileHeight () - tileBottomItem.getTileHeight () / 2;
		int iMaxItemsHeight = (PROFESSIONS_PANEL_HEIGHT - 2 * tileMatsPanelSubPanel[1].getTileHeight ()) / (tileBottomItem.getTileHeight () + 8);
		int iSeparationH = (PROFESSIONS_PANEL_HEIGHT - 2 * tileMatsPanelSubPanel[1].getTileHeight () - iMaxItemsHeight * tileBottomItem.getTileHeight ()) / (iMaxItemsHeight + 1);

		int iRowsToDelete = 0;
		if (iMaxItemsHeight <= iRows) {
			iRows = iMaxItemsHeight;
		} else {
			iRowsToDelete = (iMaxItemsHeight - iRows);
		}

		PROFESSIONS_PANEL_HEIGHT -= (iRowsToDelete * (tileBottomItem.getTileHeight () + iSeparationH));
		// PILE_PANEL_HEIGHT = renderHeight - (iconMatsPoint.y + tileIconMats.getTileHeight ()) - tileBottomItem.getTileHeight () - tileBottomItem.getTileHeight () / 2;

		PROFESSIONS_PANEL_MAX_ITEMS_PER_PAGE = iMaxItemsWidth * iRows;

		if (iRows <= 1) {
			// Lo hacemos pequeño por la derecha
			int iSeparationW = (PROFESSIONS_PANEL_WIDTH - tileScrollUp.getTileWidth () - 4 * tileMatsPanelSubPanel[3].getTileWidth () - iMaxItemsWidth * tileBottomItem.getTileWidth ()) / (iMaxItemsWidth + 1);
			int iFirstWidth = professionsPanelPoint.x + tileMatsPanelSubPanel[3].getTileWidth () + iSeparationW;
			PROFESSIONS_PANEL_WIDTH = iFirstWidth + ((menuProfessions.getItems ().size () + 1) * (tileBottomItem.getTileWidth () + iSeparationW));
		} else {
			PROFESSIONS_PANEL_WIDTH = (renderWidth / 8) * 7;
		}

		professionsPanelClosePoint.setLocation (professionsPanelPoint.x + PROFESSIONS_PANEL_WIDTH - tileButtonClose.getTileWidth (), professionsPanelPoint.y);

		// Scroll up/down
		professionsPanelIconScrollUpPoint.setLocation (professionsPanelPoint.x + PROFESSIONS_PANEL_WIDTH - tileMatsPanel[3].getTileWidth () - tileScrollUp.getTileWidth (), professionsPanelPoint.y + tileMatsPanel[1].getTileHeight ());
		professionsPanelIconScrollDownPoint.setLocation (professionsPanelPoint.x + PROFESSIONS_PANEL_WIDTH - tileMatsPanel[3].getTileWidth () - tileScrollUp.getTileWidth (), professionsPanelPoint.y + PROFESSIONS_PANEL_HEIGHT - tileScrollDown.getTileHeight () - tileMatsPanel[1].getTileHeight ());

		// Pages
		professionsPanelPagesPositionPoint.setLocation (professionsPanelIconScrollUpPoint.x + tileScrollUp.getTileWidth () / 2, professionsPanelIconScrollUpPoint.y + tileScrollUp.getTileHeight () + (professionsPanelIconScrollDownPoint.y - (professionsPanelIconScrollUpPoint.y + tileScrollUp.getTileHeight ())) / 2 - UtilFont.MAX_HEIGHT / 2);
	}


	public static void createLivingsPanel (int iPanelTypeActive, int iSoldiersGroupActive, int iCitizensGroupActive) {
		livingsPanelActive = iPanelTypeActive;
		livingsPanelCitizensGroupActive = iCitizensGroupActive;
		livingsPanelSoldiersGroupActive = iSoldiersGroupActive;

		LIVINGS_PANEL_GROUPS_WIDTH = 2 * tileLivingsGroupPanel[3].getTileWidth () + 32;
		LIVINGS_PANEL_WIDTH = 2 * tileLivingsPanel[3].getTileWidth () + 7 * tileBottomItem.getTileWidth () + tileScrollUp.getTileWidth ();
		if (iPanelTypeActive == LIVINGS_PANEL_TYPE_CITIZENS) {
			LIVINGS_PANEL_WIDTH += (2 * tileBottomItem.getTileWidth () + tileLivingsRowAutoequip.getTileWidth () + tileLivingsRowConvertSoldier.getTileWidth () + tileLivingsRowProfession.getTileWidth () + tileLivingsRowJobsGroups.getTileWidth () + LIVINGS_PANEL_GROUPS_WIDTH);
		} else if (iPanelTypeActive == LIVINGS_PANEL_TYPE_SOLDIERS) {
			LIVINGS_PANEL_WIDTH += (2 * tileBottomItem.getTileWidth () + tileLivingsRowAutoequip.getTileWidth () + tileLivingsRowConvertCivilian.getTileWidth () + tileLivingsRowConvertSoldierGuard.getTileWidth () + tileLivingsRowGroupAdd.getTileWidth ()) + LIVINGS_PANEL_GROUPS_WIDTH;
		}

		LIVINGS_PANEL_HEIGHT = renderHeight - (iconNumCitizensBackgroundPoint.y + tileBottomItem.getTileHeight ()) - tileBottomItem.getTileHeight () / 2;
		LIVINGS_PANEL_GROUPS_HEIGHT = LIVINGS_PANEL_HEIGHT - 2 * tileLivingsPanel[1].getTileHeight ();

		livingsPanelPoint.setLocation (renderWidth / 2 - LIVINGS_PANEL_WIDTH / 2, iconNumCitizensBackgroundPoint.y + tileBottomItem.getTileHeight () + tileBottomItem.getTileHeight () / 4);
		livingsPanelClosePoint.setLocation (livingsPanelPoint.x + LIVINGS_PANEL_WIDTH - tileButtonClose.getTileWidth (), livingsPanelPoint.y);

		if (iPanelTypeActive == LIVINGS_PANEL_TYPE_SOLDIERS || iPanelTypeActive == LIVINGS_PANEL_TYPE_CITIZENS) {
			// Sub-groups panel
			livingsGroupPanelPoint.setLocation (livingsPanelPoint.x + LIVINGS_PANEL_WIDTH - tileLivingsPanel[3].getTileWidth () - LIVINGS_PANEL_GROUPS_WIDTH, livingsPanelPoint.y + (((livingsPanelPoint.y + LIVINGS_PANEL_HEIGHT) - livingsPanelPoint.y) / 2) - LIVINGS_PANEL_GROUPS_HEIGHT / 2);

			// Primer icono del subpanel y la separación
			int iSeparation = (LIVINGS_PANEL_GROUPS_HEIGHT - 2 * tileLivingsGroupPanel[3].getTileHeight () - tileLivingsNoGroup.getTileHeight () - (SoldierGroups.MAX_GROUPS * tileLivingsGroup.getTileHeight ())) / (SoldierGroups.MAX_GROUPS + 2);
			livingsGroupPanelFirstIconPoint.setLocation (livingsGroupPanelPoint.x + LIVINGS_PANEL_GROUPS_WIDTH / 2 - tileLivingsNoGroup.getTileWidth () / 2, livingsGroupPanelPoint.y + tileLivingsGroupPanel[3].getTileHeight () + iSeparation);
			livingsGroupPanelIconsSeparation = iSeparation + tileLivingsNoGroup.getTileHeight ();
		}

		// Miramos cuantas livings caben
		int iMaxHeight;
		if (checkGroupsPanelEnabled (iPanelTypeActive)) {
			iMaxHeight = LIVINGS_PANEL_HEIGHT - 2 * tileLivingsPanel[1].getTileHeight () - tileBottomItem.getTileHeight () - tileBottomItem.getTileHeight () / 2;
		} else {
			iMaxHeight = LIVINGS_PANEL_HEIGHT - 2 * tileLivingsPanel[1].getTileHeight ();
		}
		LIVINGS_PANEL_MAX_ROWS = iMaxHeight / tileBottomItem.getTileHeight ();
		if (LIVINGS_PANEL_MAX_ROWS < 1) {
			LIVINGS_PANEL_MAX_ROWS = 1;
		}
		// Rows
		int iSeparation;
		if (LIVINGS_PANEL_MAX_ROWS > 1) {
			iSeparation = (iMaxHeight - LIVINGS_PANEL_MAX_ROWS * tileBottomItem.getTileHeight ()) / (LIVINGS_PANEL_MAX_ROWS - 1);
		} else {
			iSeparation = 0;
		}

		int iIniY = livingsPanelPoint.y + tileLivingsPanel[1].getTileHeight ();

		if (livingsPanelRowPoints == null || livingsPanelRowPoints.length < LIVINGS_PANEL_MAX_ROWS) {
			livingsPanelRowPoints = new Point [LIVINGS_PANEL_MAX_ROWS];
			livingsPanelRowHeadPoints = new Point [LIVINGS_PANEL_MAX_ROWS];
			livingsPanelRowBodyPoints = new Point [LIVINGS_PANEL_MAX_ROWS];
			livingsPanelRowLegsPoints = new Point [LIVINGS_PANEL_MAX_ROWS];
			livingsPanelRowFeetPoints = new Point [LIVINGS_PANEL_MAX_ROWS];
			livingsPanelRowWeaponPoints = new Point [LIVINGS_PANEL_MAX_ROWS];
			livingsPanelRowAutoequipPoints = new Point [LIVINGS_PANEL_MAX_ROWS];
			livingsPanelRowProfessionPoints = new Point [LIVINGS_PANEL_MAX_ROWS];
			livingsPanelRowJobsGroupsPoints = new Point [LIVINGS_PANEL_MAX_ROWS];
			livingsPanelRowConvertCivilianSoldierPoints = new Point [LIVINGS_PANEL_MAX_ROWS];
			livingsPanelRowConvertSoldierGuardPoints = new Point [LIVINGS_PANEL_MAX_ROWS];
			livingsPanelRowConvertSoldierPatrolPoints = new Point [LIVINGS_PANEL_MAX_ROWS];
			livingsPanelRowConvertSoldierBossPoints = new Point [LIVINGS_PANEL_MAX_ROWS];
			livingsPanelRowGroupPoints = new Point [LIVINGS_PANEL_MAX_ROWS];

			for (int i = 0; i < LIVINGS_PANEL_MAX_ROWS; i++) {
				livingsPanelRowPoints[i] = new Point (0, 0);
				livingsPanelRowHeadPoints[i] = new Point (0, 0);
				livingsPanelRowBodyPoints[i] = new Point (0, 0);
				livingsPanelRowLegsPoints[i] = new Point (0, 0);
				livingsPanelRowFeetPoints[i] = new Point (0, 0);
				livingsPanelRowWeaponPoints[i] = new Point (0, 0);

				livingsPanelRowAutoequipPoints[i] = new Point (0, 0);
				livingsPanelRowProfessionPoints[i] = new Point (0, 0);
				livingsPanelRowJobsGroupsPoints[i] = new Point (0, 0);
				livingsPanelRowConvertCivilianSoldierPoints[i] = new Point (0, 0);
			}

			if (livingsPanelRowConvertSoldierGuardPoints == null || iPanelTypeActive == LIVINGS_PANEL_TYPE_SOLDIERS) {
				for (int i = 0; i < LIVINGS_PANEL_MAX_ROWS; i++) {
					livingsPanelRowConvertSoldierGuardPoints[i] = new Point (0, 0);
					livingsPanelRowConvertSoldierPatrolPoints[i] = new Point (0, 0);
					livingsPanelRowConvertSoldierBossPoints[i] = new Point (0, 0);
					livingsPanelRowGroupPoints[i] = new Point (0, 0);
				}
			}
		}

		int iRowsOffsetY = 0;
		if (checkGroupsPanelEnabled (iPanelTypeActive)) {
			iRowsOffsetY = tileBottomItem.getTileHeight () + tileBottomItem.getTileHeight () / 2;
		}

		for (int i = 0; i < LIVINGS_PANEL_MAX_ROWS; i++) {
			// Living
			livingsPanelRowPoints[i] = new Point (livingsPanelPoint.x + tileLivingsPanel[3].getTileWidth (), iIniY + (i * (tileBottomItem.getTileHeight () + iSeparation)) + iRowsOffsetY);

			// Equipment
			livingsPanelRowHeadPoints[i] = new Point (livingsPanelRowPoints[i].x + tileBottomItem.getTileWidth () + tileBottomItem.getTileWidth () / 2, livingsPanelRowPoints[i].y);
			livingsPanelRowBodyPoints[i] = new Point (livingsPanelRowHeadPoints[i].x + tileBottomItem.getTileWidth (), livingsPanelRowPoints[i].y);
			livingsPanelRowLegsPoints[i] = new Point (livingsPanelRowBodyPoints[i].x + tileBottomItem.getTileWidth (), livingsPanelRowPoints[i].y);
			livingsPanelRowFeetPoints[i] = new Point (livingsPanelRowLegsPoints[i].x + tileBottomItem.getTileWidth (), livingsPanelRowPoints[i].y);
			livingsPanelRowWeaponPoints[i] = new Point (livingsPanelRowFeetPoints[i].x + tileBottomItem.getTileWidth (), livingsPanelRowPoints[i].y);

			// Autoequip
			livingsPanelRowAutoequipPoints[i] = new Point (livingsPanelRowWeaponPoints[i].x + tileBottomItem.getTileWidth () + tileBottomItem.getTileWidth () / 2, livingsPanelRowWeaponPoints[i].y + tileBottomItem.getTileHeight () / 2 - tileLivingsRowAutoequip.getTileHeight () / 2);

			// Convert to soldier / civilian
			livingsPanelRowConvertCivilianSoldierPoints[i] = new Point (livingsPanelRowAutoequipPoints[i].x + tileLivingsRowAutoequip.getTileWidth () + tileBottomItem.getTileWidth () / 2, livingsPanelRowWeaponPoints[i].y + tileBottomItem.getTileHeight () / 2 - tileLivingsRowConvertSoldier.getTileHeight () / 2);

			// Profession
			livingsPanelRowProfessionPoints[i] = new Point (livingsPanelRowConvertCivilianSoldierPoints[i].x + tileLivingsRowConvertSoldier.getTileWidth (), livingsPanelRowConvertCivilianSoldierPoints[i].y + tileLivingsRowConvertSoldier.getTileHeight () / 2 - tileLivingsRowProfession.getTileHeight () / 2);

			// Jobs groups
			livingsPanelRowJobsGroupsPoints[i] = new Point (livingsPanelRowProfessionPoints[i].x + tileLivingsRowProfession.getTileWidth (), livingsPanelRowProfessionPoints[i].y + tileLivingsRowProfession.getTileHeight () / 2 - tileLivingsRowJobsGroups.getTileHeight () / 2);
		}
		if (iPanelTypeActive == LIVINGS_PANEL_TYPE_SOLDIERS) {
			if (livingsPanelSoldiersGroupActive == -1) {
				for (int i = 0; i < LIVINGS_PANEL_MAX_ROWS; i++) {
					// Soldier states
					livingsPanelRowConvertSoldierGuardPoints[i] = new Point (livingsPanelRowConvertCivilianSoldierPoints[i].x + tileLivingsRowConvertCivilian.getTileWidth () + tileBottomItem.getTileWidth () / 2, livingsPanelRowWeaponPoints[i].y + tileBottomItem.getTileHeight () / 2 - tileLivingsRowConvertSoldierGuard.getTileHeight ());
					livingsPanelRowConvertSoldierPatrolPoints[i] = new Point (livingsPanelRowConvertSoldierGuardPoints[i].x + tileLivingsRowConvertSoldierGuard.getTileWidth (), livingsPanelRowConvertSoldierGuardPoints[i].y);
					livingsPanelRowConvertSoldierBossPoints[i] = new Point (livingsPanelRowConvertSoldierGuardPoints[i].x, livingsPanelRowConvertSoldierGuardPoints[i].y + tileLivingsRowConvertSoldierGuard.getTileHeight ());

					// Soldier add group
					livingsPanelRowGroupPoints[i] = new Point (livingsPanelRowConvertSoldierPatrolPoints[i].x, livingsPanelRowConvertSoldierBossPoints[i].y);
				}
			} else {
				for (int i = 0; i < LIVINGS_PANEL_MAX_ROWS; i++) {
					// Soldier remove group
					livingsPanelRowGroupPoints[i] = new Point (livingsPanelRowConvertCivilianSoldierPoints[i].x + tileLivingsRowConvertCivilian.getTileWidth () + tileBottomItem.getTileWidth () / 2, livingsPanelRowConvertCivilianSoldierPoints[i].y);
				}

				// Single group panel
				if (iPanelTypeActive == LIVINGS_PANEL_TYPE_SOLDIERS && livingsPanelSoldiersGroupActive != -1) {
					LIVINGS_PANEL_SINGLE_GROUP_WIDTH = livingsPanelRowGroupPoints[0].x + tileLivingsRowGroupRemove.getTileWidth () - livingsPanelRowPoints[0].x;
					LIVINGS_PANEL_SINGLE_GROUP_HEIGHT = tileBottomItem.getTileHeight () + tileBottomItem.getTileHeight () / 2;
					livingsSingleGroupPanelPoint.setLocation (livingsPanelRowPoints[0].x, iIniY - tileLivingsGroupPanel[1].getTileHeight () / 2);

					int iSeparationSingleGroup = (LIVINGS_PANEL_SINGLE_GROUP_WIDTH - 2 * tileLivingsGroupPanel[3].getTileWidth () - tileLivingsSingleGroupRename.getTileWidth () - tileLivingsSingleGroupGuard.getTileWidth () - tileLivingsSingleGroupPatrol.getTileWidth () - tileLivingsSingleGroupBoss.getTileWidth () - tileLivingsRowAutoequip.getTileWidth () - tileLivingsSingleGroupDisband.getTileWidth ()) / 5;
					// Botones del single group panel
					int iFirstButton = livingsSingleGroupPanelPoint.x + tileLivingsGroupPanel[3].getTileWidth ();
					// Rename
					livingsSingleGroupRenamePoint.setLocation (iFirstButton, livingsSingleGroupPanelPoint.y + LIVINGS_PANEL_SINGLE_GROUP_HEIGHT / 2 - tileLivingsSingleGroupRename.getTileHeight () / 2);
					iFirstButton += tileLivingsSingleGroupRename.getTileWidth () + iSeparationSingleGroup;
					// Guard
					livingsSingleGroupGuardPoint.setLocation (iFirstButton, livingsSingleGroupPanelPoint.y + LIVINGS_PANEL_SINGLE_GROUP_HEIGHT / 2 - tileLivingsSingleGroupGuard.getTileHeight () / 2);
					iFirstButton += tileLivingsSingleGroupGuard.getTileWidth () + iSeparationSingleGroup;
					// Patrol
					livingsSingleGroupPatrolPoint.setLocation (iFirstButton, livingsSingleGroupPanelPoint.y + LIVINGS_PANEL_SINGLE_GROUP_HEIGHT / 2 - tileLivingsSingleGroupPatrol.getTileHeight () / 2);
					iFirstButton += tileLivingsSingleGroupPatrol.getTileWidth () + iSeparationSingleGroup;
					// Boss
					livingsSingleGroupBossPoint.setLocation (iFirstButton, livingsSingleGroupPanelPoint.y + LIVINGS_PANEL_SINGLE_GROUP_HEIGHT / 2 - tileLivingsSingleGroupBoss.getTileHeight () / 2);
					iFirstButton += tileLivingsSingleGroupBoss.getTileWidth () + iSeparationSingleGroup;
					// Autoequip
					livingsSingleGroupAutoequipPoint.setLocation (iFirstButton, livingsSingleGroupPanelPoint.y + LIVINGS_PANEL_SINGLE_GROUP_HEIGHT / 2 - tileLivingsRowAutoequip.getTileHeight () / 2);
					iFirstButton += tileLivingsRowAutoequip.getTileWidth () + iSeparationSingleGroup;
					// Disband
					livingsSingleGroupDisbandPoint.setLocation (iFirstButton, livingsSingleGroupPanelPoint.y + LIVINGS_PANEL_SINGLE_GROUP_HEIGHT / 2 - tileLivingsSingleGroupDisband.getTileHeight () / 2);
				}
			}
		} else if (iPanelTypeActive == LIVINGS_PANEL_TYPE_CITIZENS) {
			if (livingsPanelCitizensGroupActive == -1) {
				for (int i = 0; i < LIVINGS_PANEL_MAX_ROWS; i++) {
					// Soldier states
					livingsPanelRowConvertSoldierGuardPoints[i] = new Point (livingsPanelRowConvertCivilianSoldierPoints[i].x + tileLivingsRowConvertCivilian.getTileWidth () + tileBottomItem.getTileWidth () / 2, livingsPanelRowWeaponPoints[i].y + tileBottomItem.getTileHeight () / 2 - tileLivingsRowConvertSoldierGuard.getTileHeight ());
					livingsPanelRowConvertSoldierPatrolPoints[i] = new Point (livingsPanelRowConvertSoldierGuardPoints[i].x + tileLivingsRowConvertSoldierGuard.getTileWidth (), livingsPanelRowConvertSoldierGuardPoints[i].y);
					livingsPanelRowConvertSoldierBossPoints[i] = new Point (livingsPanelRowConvertSoldierGuardPoints[i].x, livingsPanelRowConvertSoldierGuardPoints[i].y + tileLivingsRowConvertSoldierGuard.getTileHeight ());

					// Soldier add group
					livingsPanelRowGroupPoints[i] = new Point (livingsPanelRowConvertSoldierPatrolPoints[i].x, livingsPanelRowConvertSoldierBossPoints[i].y);
				}
			} else {
				for (int i = 0; i < LIVINGS_PANEL_MAX_ROWS; i++) {
					// Civilian??? remove group
					livingsPanelRowGroupPoints[i] = new Point (livingsPanelRowConvertCivilianSoldierPoints[i].x + tileLivingsRowConvertCivilian.getTileWidth () + tileBottomItem.getTileWidth () / 2, livingsPanelRowConvertCivilianSoldierPoints[i].y);
				}

				// Single group panel
				if (iPanelTypeActive == LIVINGS_PANEL_TYPE_CITIZENS && livingsPanelCitizensGroupActive != -1) {
					LIVINGS_PANEL_SINGLE_GROUP_WIDTH = livingsPanelRowGroupPoints[0].x + tileLivingsRowGroupRemove.getTileWidth () - livingsPanelRowPoints[0].x;
					LIVINGS_PANEL_SINGLE_GROUP_HEIGHT = tileBottomItem.getTileHeight () + tileBottomItem.getTileHeight () / 2;
					livingsSingleGroupPanelPoint.setLocation (livingsPanelRowPoints[0].x, iIniY - tileLivingsGroupPanel[1].getTileHeight () / 2);

					int iSeparationSingleGroup = (LIVINGS_PANEL_SINGLE_GROUP_WIDTH - 2 * tileLivingsGroupPanel[3].getTileWidth () - tileLivingsSingleGroupRename.getTileWidth () - tileLivingsRowAutoequip.getTileWidth () - tileLivingsSingleGroupDisband.getTileWidth () - tileLivingsSingleGroupChangeJobs.getTileWidth ()) / 3;
					// Botones del single group panel
					int iFirstButton = livingsSingleGroupPanelPoint.x + tileLivingsGroupPanel[3].getTileWidth ();
					// Rename
					livingsSingleGroupRenamePoint.setLocation (iFirstButton, livingsSingleGroupPanelPoint.y + LIVINGS_PANEL_SINGLE_GROUP_HEIGHT / 2 - tileLivingsSingleGroupRename.getTileHeight () / 2);
					iFirstButton += tileLivingsSingleGroupRename.getTileWidth () + iSeparationSingleGroup;
					// Autoequip
					livingsSingleGroupAutoequipPoint.setLocation (iFirstButton, livingsSingleGroupPanelPoint.y + LIVINGS_PANEL_SINGLE_GROUP_HEIGHT / 2 - tileLivingsRowAutoequip.getTileHeight () / 2);
					iFirstButton += tileLivingsRowAutoequip.getTileWidth () + iSeparationSingleGroup;
					// Change jobs
					livingsSingleGroupChangeJobsPoint.setLocation (iFirstButton, livingsSingleGroupPanelPoint.y + LIVINGS_PANEL_SINGLE_GROUP_HEIGHT / 2 - tileLivingsSingleGroupChangeJobs.getTileHeight () / 2);
					iFirstButton += tileLivingsSingleGroupChangeJobs.getTileWidth () + iSeparationSingleGroup;
					// Disband
					livingsSingleGroupDisbandPoint.setLocation (iFirstButton, livingsSingleGroupPanelPoint.y + LIVINGS_PANEL_SINGLE_GROUP_HEIGHT / 2 - tileLivingsSingleGroupDisband.getTileHeight () / 2);
				}
			}
		}

		// Scrolls
		if (iPanelTypeActive == LIVINGS_PANEL_TYPE_SOLDIERS) {
			if (livingsPanelSoldiersGroupActive != -1) {
				// Scroll up/down
				livingsPanelIconScrollUpPoint.setLocation (livingsGroupPanelPoint.x - tileBottomItem.getTileWidth () / 2 - tileScrollUp.getTileWidth (), livingsPanelPoint.y + tileLivingsPanel[1].getTileHeight ());
				livingsPanelIconScrollDownPoint.setLocation (livingsGroupPanelPoint.x - tileBottomItem.getTileWidth () / 2 - tileScrollDown.getTileWidth (), livingsPanelRowPoints[LIVINGS_PANEL_MAX_ROWS - 1].y + tileBottomItem.getTileWidth () - tileScrollDown.getTileHeight ());
			} else {
				// Scroll up/down
				livingsPanelIconScrollUpPoint.setLocation (livingsGroupPanelPoint.x - tileBottomItem.getTileWidth () / 2 - tileScrollUp.getTileWidth (), livingsPanelPoint.y + tileLivingsPanel[1].getTileHeight ());
				livingsPanelIconScrollDownPoint.setLocation (livingsGroupPanelPoint.x - tileBottomItem.getTileWidth () / 2 - tileScrollDown.getTileWidth (), livingsPanelPoint.y + LIVINGS_PANEL_HEIGHT - tileLivingsPanel[1].getTileHeight () - tileScrollDown.getTileHeight ());
			}
		} else if (iPanelTypeActive == LIVINGS_PANEL_TYPE_CITIZENS) {
			if (livingsPanelCitizensGroupActive != -1) {
				// Scroll up/down
				livingsPanelIconScrollUpPoint.setLocation (livingsGroupPanelPoint.x - tileBottomItem.getTileWidth () / 2 - tileScrollUp.getTileWidth (), livingsPanelPoint.y + tileLivingsPanel[1].getTileHeight ());
				livingsPanelIconScrollDownPoint.setLocation (livingsGroupPanelPoint.x - tileBottomItem.getTileWidth () / 2 - tileScrollDown.getTileWidth (), livingsPanelRowPoints[LIVINGS_PANEL_MAX_ROWS - 1].y + tileBottomItem.getTileWidth () - tileScrollDown.getTileHeight ());
			} else {
				// Scroll up/down
				livingsPanelIconScrollUpPoint.setLocation (livingsGroupPanelPoint.x - tileBottomItem.getTileWidth () / 2 - tileScrollUp.getTileWidth (), livingsPanelPoint.y + tileLivingsPanel[1].getTileHeight () + 2 * tileIconLevelDown.getTileHeight ());
				livingsPanelIconScrollDownPoint.setLocation (livingsGroupPanelPoint.x - tileBottomItem.getTileWidth () / 2 - tileScrollDown.getTileWidth (), livingsPanelPoint.y + LIVINGS_PANEL_HEIGHT - tileLivingsPanel[1].getTileHeight () - tileScrollDown.getTileHeight ());

				// Restrict points
				livingsPanelIconRestrictUpPoint.setLocation (livingsPanelIconScrollUpPoint.x - (tileIconLevelUp.getTileWidth () / 4) * 3, livingsPanelPoint.y + tileLivingsPanel[1].getTileHeight ());
				livingsPanelIconRestrictDownPoint.setLocation (livingsPanelIconScrollUpPoint.x + tileScrollUp.getTileWidth () - (tileIconLevelDown.getTileWidth () / 4), livingsPanelPoint.y + tileLivingsPanel[1].getTileHeight ());
			}
		} else { // Heroes
			// Scroll up/down
			livingsPanelIconScrollUpPoint.setLocation (livingsPanelPoint.x + LIVINGS_PANEL_WIDTH - tileLivingsPanel[3].getTileWidth () - tileScrollUp.getTileWidth (), livingsPanelPoint.y + tileLivingsPanel[1].getTileHeight () + 2 * tileIconLevelDown.getTileHeight ());
			livingsPanelIconScrollDownPoint.setLocation (livingsPanelPoint.x + LIVINGS_PANEL_WIDTH - tileLivingsPanel[3].getTileWidth () - tileScrollUp.getTileWidth (), livingsPanelPoint.y + LIVINGS_PANEL_HEIGHT - tileLivingsPanel[1].getTileHeight () - tileScrollDown.getTileHeight ());

			// Restrict points
			livingsPanelIconRestrictUpPoint.setLocation (livingsPanelIconScrollUpPoint.x - (tileIconLevelUp.getTileWidth () / 4) * 3, livingsPanelPoint.y + tileLivingsPanel[1].getTileHeight ());
			livingsPanelIconRestrictDownPoint.setLocation (livingsPanelIconScrollUpPoint.x + tileScrollUp.getTileWidth () - (tileIconLevelDown.getTileWidth () / 4), livingsPanelPoint.y + tileLivingsPanel[1].getTileHeight ());
		}

		// Pages
		int iSeparationScroll = livingsPanelIconScrollDownPoint.y - (livingsPanelIconScrollUpPoint.y + tileScrollUp.getTileHeight ());
		livingsPanelPagesPoint.setLocation (livingsPanelIconScrollUpPoint.x + tileScrollUp.getTileWidth () / 2, livingsPanelIconScrollUpPoint.y + tileScrollUp.getTileHeight () + iSeparationScroll / 2 - UtilFont.MAX_HEIGHT / 2);
		// Pages data
		if (livingsDataIndexPages == null) {
			livingsDataIndexPages = new int [3];
			livingsDataIndexPages[LIVINGS_PANEL_TYPE_CITIZENS] = 1;
			livingsDataIndexPages[LIVINGS_PANEL_TYPE_SOLDIERS] = 1;
			livingsDataIndexPages[LIVINGS_PANEL_TYPE_HEROES] = 1;

			livingsDataIndexPagesCitizenGroups = new int [CitizenGroups.MAX_GROUPS];
			for (int i = 0; i < CitizenGroups.MAX_GROUPS; i++) {
				livingsDataIndexPagesCitizenGroups[i] = 1;
			}

			livingsDataIndexPagesSoldierGroups = new int [SoldierGroups.MAX_GROUPS];
			for (int i = 0; i < SoldierGroups.MAX_GROUPS; i++) {
				livingsDataIndexPagesSoldierGroups[i] = 1;
			}
		}
	}


	private void createPrioritiesPanel () {
		PRIORITIES_PANEL_NUM_ITEMS = ActionPriorityManager.getPrioritiesListSize () + 1; // +1 para el back

		// Miramos la separación entre items
		int iPixelsBetweenItems;
		if (PRIORITIES_PANEL_NUM_ITEMS > 1) {
			iPixelsBetweenItems = PIXELS_TO_BORDER;
		} else {
			iPixelsBetweenItems = 0;
		}

		// Tenemos el tamaño de los items
		PRIORITIES_PANEL_WIDTH = PRIORITIES_PANEL_ITEM_SIZE + 2 * tilePrioritiesPanelUpIcon.getTileWidth ();
		PRIORITIES_PANEL_HEIGHT = 2 * PIXELS_TO_BORDER + (PRIORITIES_PANEL_NUM_ITEMS * PRIORITIES_PANEL_ITEM_SIZE) + ((PRIORITIES_PANEL_NUM_ITEMS - 1) * iPixelsBetweenItems);

		// Número de columnas para que quepa
		int MAX_ITEMS_PER_COLUMN = PRIORITIES_PANEL_NUM_ITEMS;
		int iNumColumns;
		int iMaxHeight = (bottomPanelY - PIXELS_TO_BORDER) - (20 + 2 * PIXELS_TO_BORDER);

		if (PRIORITIES_PANEL_NUM_ITEMS > 1 && PRIORITIES_PANEL_HEIGHT > iMaxHeight) {
			if (iMaxHeight - 2 * PIXELS_TO_BORDER != 0) { // Check división por 0
				iNumColumns = PRIORITIES_PANEL_HEIGHT / (iMaxHeight - 2 * PIXELS_TO_BORDER); // Realmente no entiendo el 2*PIXELS en esta operación
				if (PRIORITIES_PANEL_HEIGHT % (iMaxHeight - 2 * PIXELS_TO_BORDER) != 0) {
					iNumColumns++;
				}
				if (iNumColumns < 1) {
					iNumColumns = 1;
				}
			} else {
				iNumColumns = PRIORITIES_PANEL_NUM_ITEMS;
			}

			MAX_ITEMS_PER_COLUMN = (PRIORITIES_PANEL_NUM_ITEMS / iNumColumns);
			if (PRIORITIES_PANEL_NUM_ITEMS % iNumColumns != 0) {
				MAX_ITEMS_PER_COLUMN++;
			}
			if (MAX_ITEMS_PER_COLUMN < 1) {
				MAX_ITEMS_PER_COLUMN = 1;
			}

			PRIORITIES_PANEL_WIDTH = iNumColumns * (PRIORITIES_PANEL_ITEM_SIZE + 2 * tilePrioritiesPanelUpIcon.getTileWidth () + PIXELS_TO_BORDER);
			PRIORITIES_PANEL_HEIGHT = 2 * PIXELS_TO_BORDER + (MAX_ITEMS_PER_COLUMN * PRIORITIES_PANEL_ITEM_SIZE) + ((MAX_ITEMS_PER_COLUMN - 1) * iPixelsBetweenItems);
		} else {
			iNumColumns = 1;
		}

		prioritiesPanelPoint.setLocation (renderWidth / 2 - PRIORITIES_PANEL_WIDTH / 2, renderHeight / 2 - PRIORITIES_PANEL_HEIGHT / 2);

		// Positions
		prioritiesPanelItemsPosition = new ArrayList<Point> ();
		prioritiesPanelItemsUpPosition = new ArrayList<Point> ();
		prioritiesPanelItemsDownPosition = new ArrayList<Point> ();
		Point p;
		int iColumnCounter = 0, iColumnIndex = 0;
		for (int i = 0; i < PRIORITIES_PANEL_NUM_ITEMS; i++) {
			p = new Point (prioritiesPanelPoint.x + tilePrioritiesPanelUpIcon.getTileWidth () + (iColumnIndex * (PRIORITIES_PANEL_ITEM_SIZE + 2 * ICON_WIDTH + iPixelsBetweenItems)), prioritiesPanelPoint.y + PIXELS_TO_BORDER + ((i - (iColumnIndex * MAX_ITEMS_PER_COLUMN)) * (PRIORITIES_PANEL_ITEM_SIZE + iPixelsBetweenItems)));
			prioritiesPanelItemsPosition.add (p);
			if (i != (PRIORITIES_PANEL_NUM_ITEMS - 1)) {
				if (i > 0) {
					prioritiesPanelItemsUpPosition.add (new Point (p.x - ICON_WIDTH, p.y + PRIORITIES_PANEL_ITEM_SIZE / 2 - ICON_HEIGHT / 2));
				} else {
					prioritiesPanelItemsUpPosition.add (new Point (-1, -1));
				}
				if (i < (PRIORITIES_PANEL_NUM_ITEMS - 2)) {
					prioritiesPanelItemsDownPosition.add (new Point (p.x + PRIORITIES_PANEL_ITEM_SIZE, p.y + PRIORITIES_PANEL_ITEM_SIZE / 2 - ICON_HEIGHT / 2));
				} else {
					prioritiesPanelItemsDownPosition.add (new Point (-1, -1));
				}
			} else {
				prioritiesPanelItemsUpPosition.add (new Point (-1, -1));
				prioritiesPanelItemsDownPosition.add (new Point (-1, -1));
			}

			iColumnCounter++;
			if (iColumnCounter == MAX_ITEMS_PER_COLUMN) {
				iColumnCounter = 0;
				iColumnIndex++;
			}
		}
	}


	public static void deleteTradePanel () {
		tradePanelActive = false;
		tradePanel = null;
	}


	public static void closeTypingPanel () {
		if (typingPanel != null) {
			if (TypingPanel.TYPING_TYPE == TypingPanel.TYPE_RENAME_GROUP) {
				int iGroup = TypingPanel.TYPING_PARAMETER;
				if (iGroup >= 0 && iGroup < SoldierGroups.MAX_GROUPS) {
					Game.getWorld ().getSoldierGroups ().getGroup (iGroup).setName (TypingPanel.getNewText ());
				}
			} else if (TypingPanel.TYPING_TYPE == TypingPanel.TYPE_RENAME_JOB_GROUP) {
				int iGroup = TypingPanel.TYPING_PARAMETER;
				if (iGroup >= 0 && iGroup < CitizenGroups.MAX_GROUPS) {
					Game.getWorld ().getCitizenGroups ().getGroup (iGroup).setName (TypingPanel.getNewText ());
				}
			} else if (TypingPanel.TYPING_TYPE == TypingPanel.TYPE_ADD_TEXT_TO_ITEM) {
				// Miramos si el item existe
				Integer iItemID = Integer.valueOf (TypingPanel.TYPING_PARAMETER);
				if (World.getItems ().containsKey (iItemID)) {
					// Existe
					ArrayList<String> alTexts = World.getItemsText ().get (iItemID);
					if (alTexts == null) {
						alTexts = new ArrayList<String> ();
					}
					alTexts.add (TypingPanel.getNewText ());
					World.getItemsText ().put (iItemID, alTexts);
				}
			}

			typingPanel = null;
		}
	}


	/**
	 * Limpia todos los datos (se usa cuando se sale de la partida y se va al menú principal)
	 */
	public static void clear () {
		currentMenu = null;
		bottomSubPanelMenu = null;
		menuPanelMenu = null;

		productionPanelActive = false;
		prioritiesPanelActive = false;
		tradePanelActive = false;
		tradePanel = null;

		ImagesPanel.clear ();
		imagesPanel = null;
	}
}
