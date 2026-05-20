package xaos.panels.UI;
import xaos.tiles.Tile;
import xaos.utils.UtilsGL;

public class GenerateTiles {
    public void generateTiles() {
    /*
		 * BOTTOM
		 */
		UIPanelState.tileBottomItem = new Tile("bottom_item"); //$NON-NLS-1$
		UIPanelState.tileBottomItemSM = new Tile("bottom_item_sm"); //$NON-NLS-1$
		UIPanelState.tileBottomScrollLeft = new Tile("bottom_scr_left"); //$NON-NLS-1$
		UIPanelState.tileBottomScrollLeftON = new Tile("bottom_scr_leftON"); //$NON-NLS-1$
		UIPanelState.tileBottomScrollRight = new Tile("bottom_scr_right"); //$NON-NLS-1$
		UIPanelState.tileBottomScrollRightON = new Tile("bottom_scr_rightON"); //$NON-NLS-1$
		UIPanelState.tileBottomPanel = new Tile("bottom_panel"); //$NON-NLS-1$

		UIPanelState.tileBottomItemAlpha = UtilsGL.generateAlpha(UIPanelState.tileBottomItem);
		UIPanelState.tileBottomScrollLeftAlpha = UtilsGL.generateAlpha(UIPanelState.tileBottomScrollLeft);
		UIPanelState.tileBottomScrollRightAlpha = UtilsGL.generateAlpha(UIPanelState.tileBottomScrollRight);
		UIPanelState.tileBottomPanelAlpha = UtilsGL.generateAlpha(UIPanelState.tileBottomPanel, UIPanelState.BOTTOM_PANEL_WIDTH, UIPanelState.BOTTOM_PANEL_HEIGHT);

		UIPanelState.tileBottomSubItem = new Tile("bottom_subitem"); //$NON-NLS-1$
		UIPanelState.tileBottomSubPanel = new Tile[9]; // Background/N/S/E/W/NE,NW,SE,SW
		UIPanelState.tileBottomSubPanel[0] = new Tile("bottom_subpanel"); //$NON-NLS-1$
		UIPanelState.tileBottomSubPanel[1] = new Tile("bottom_subpanel_N"); //$NON-NLS-1$
		UIPanelState.tileBottomSubPanel[2] = new Tile("bottom_subpanel_S"); //$NON-NLS-1$
		UIPanelState.tileBottomSubPanel[3] = new Tile("bottom_subpanel_E"); //$NON-NLS-1$
		UIPanelState.tileBottomSubPanel[4] = new Tile("bottom_subpanel_W"); //$NON-NLS-1$
		UIPanelState.tileBottomSubPanel[5] = new Tile("bottom_subpanel_NE"); //$NON-NLS-1$
		UIPanelState.tileBottomSubPanel[6] = new Tile("bottom_subpanel_NW"); //$NON-NLS-1$
		UIPanelState.tileBottomSubPanel[7] = new Tile("bottom_subpanel_SE"); //$NON-NLS-1$
		UIPanelState.tileBottomSubPanel[8] = new Tile("bottom_subpanel_SW"); //$NON-NLS-1$

		UIPanelState.tileBottomSubItemAlpha = UtilsGL.generateAlpha(UIPanelState.tileBottomSubItem);

		UIPanelState.tileOpenBottomMenu = new Tile("icon_openBottom"); //$NON-NLS-1$
		UIPanelState.tileOpenBottomMenuAlpha = UtilsGL.generateAlpha(UIPanelState.tileOpenBottomMenu);
		UIPanelState.tileOpenBottomMenuON = new Tile("icon_openBottomON"); //$NON-NLS-1$
		UIPanelState.tileOpenBottomMenuONAlpha = UtilsGL.generateAlpha(UIPanelState.tileOpenBottomMenuON);

		/*
		 * MINIMAP
		 */
		UIPanelState.tileMinimapPanel = new Tile("minimap_panel"); //$NON-NLS-1$
		UIPanelState.tileMinimapPanelAlpha = UtilsGL.generateAlpha(UIPanelState.tileMinimapPanel);

		/*
		 * MESSAGES
		 */
		UIPanelState.tileMessagesPanel = new Tile[9]; // Background/N/S/E/W/NE,NW,SE,SW
		UIPanelState.tileMessagesPanel[0] = new Tile("messages_panel"); //$NON-NLS-1$
		UIPanelState.tileMessagesPanel[1] = new Tile("messages_panel_N"); //$NON-NLS-1$
		UIPanelState.tileMessagesPanel[2] = new Tile("messages_panel_S"); //$NON-NLS-1$
		UIPanelState.tileMessagesPanel[3] = new Tile("messages_panel_E"); //$NON-NLS-1$
		UIPanelState.tileMessagesPanel[4] = new Tile("messages_panel_W"); //$NON-NLS-1$
		UIPanelState.tileMessagesPanel[5] = new Tile("messages_panel_NE"); //$NON-NLS-1$
		UIPanelState.tileMessagesPanel[6] = new Tile("messages_panel_NW"); //$NON-NLS-1$
		UIPanelState.tileMessagesPanel[7] = new Tile("messages_panel_SE"); //$NON-NLS-1$
		UIPanelState.tileMessagesPanel[8] = new Tile("messages_panel_SW"); //$NON-NLS-1$
		UIPanelState.tileMessagesPanelSubPanel = new Tile[9]; // Background/N/S/E/W/NE,NW,SE,SW
		UIPanelState.tileMessagesPanelSubPanel[0] = new Tile("messages_subpanel"); //$NON-NLS-1$
		UIPanelState.tileMessagesPanelSubPanel[1] = new Tile("messages_subpanel_N"); //$NON-NLS-1$
		UIPanelState.tileMessagesPanelSubPanel[2] = new Tile("messages_subpanel_S"); //$NON-NLS-1$
		UIPanelState.tileMessagesPanelSubPanel[3] = new Tile("messages_subpanel_E"); //$NON-NLS-1$
		UIPanelState.tileMessagesPanelSubPanel[4] = new Tile("messages_subpanel_W"); //$NON-NLS-1$
		UIPanelState.tileMessagesPanelSubPanel[5] = new Tile("messages_subpanel_NE"); //$NON-NLS-1$
		UIPanelState.tileMessagesPanelSubPanel[6] = new Tile("messages_subpanel_NW"); //$NON-NLS-1$
		UIPanelState.tileMessagesPanelSubPanel[7] = new Tile("messages_subpanel_SE"); //$NON-NLS-1$
		UIPanelState.tileMessagesPanelSubPanel[8] = new Tile("messages_subpanel_SW"); //$NON-NLS-1$

		/*
		 * PRODUCTION PANEL
		 */
		UIPanelState.tileProductionPanel = new Tile[9]; // Background/N/S/E/W/NE,NW,SE,SW
		UIPanelState.tileProductionPanel[0] = new Tile("production_panel"); //$NON-NLS-1$
		UIPanelState.tileProductionPanel[1] = new Tile("production_panel_N"); //$NON-NLS-1$
		UIPanelState.tileProductionPanel[2] = new Tile("production_panel_S"); //$NON-NLS-1$
		UIPanelState.tileProductionPanel[3] = new Tile("production_panel_E"); //$NON-NLS-1$
		UIPanelState.tileProductionPanel[4] = new Tile("production_panel_W"); //$NON-NLS-1$
		UIPanelState.tileProductionPanel[5] = new Tile("production_panel_NE"); //$NON-NLS-1$
		UIPanelState.tileProductionPanel[6] = new Tile("production_panel_NW"); //$NON-NLS-1$
		UIPanelState.tileProductionPanel[7] = new Tile("production_panel_SE"); //$NON-NLS-1$
		UIPanelState.tileProductionPanel[8] = new Tile("production_panel_SW"); //$NON-NLS-1$
		UIPanelState.tileProductionPanelPlusIcon = new Tile("production_panel_plus_icon"); //$NON-NLS-1$
		UIPanelState.tileProductionPanelPlusIconAlpha = UtilsGL.generateAlpha(UIPanelState.tileProductionPanelPlusIcon);
		UIPanelState.tileProductionPanelMinusIcon = new Tile("production_panel_minus_icon"); //$NON-NLS-1$
		UIPanelState.tileProductionPanelMinusIconAlpha = UtilsGL.generateAlpha(UIPanelState.tileProductionPanelMinusIcon);

		UIPanelState.tileOpenProductionPanel = new Tile("icon_openLeft"); //$NON-NLS-1$
		UIPanelState.tileOpenProductionPanelAlpha = UtilsGL.generateAlpha(UIPanelState.tileOpenProductionPanel);
		UIPanelState.tileOpenProductionPanelON = new Tile("icon_openLeftON"); //$NON-NLS-1$
		UIPanelState.tileOpenProductionPanelONAlpha = UtilsGL.generateAlpha(UIPanelState.tileOpenProductionPanelON);

		/*
		 * TRADE PANEL
		 */
		UIPanelState.tileTradePanel = new Tile[9]; // Background/N/S/E/W/NE,NW,SE,SW
		UIPanelState.tileTradePanel[0] = new Tile("trade_panel"); //$NON-NLS-1$
		UIPanelState.tileTradePanel[1] = new Tile("trade_panel_N"); //$NON-NLS-1$
		UIPanelState.tileTradePanel[2] = new Tile("trade_panel_S"); //$NON-NLS-1$
		UIPanelState.tileTradePanel[3] = new Tile("trade_panel_E"); //$NON-NLS-1$
		UIPanelState.tileTradePanel[4] = new Tile("trade_panel_W"); //$NON-NLS-1$
		UIPanelState.tileTradePanel[5] = new Tile("trade_panel_NE"); //$NON-NLS-1$
		UIPanelState.tileTradePanel[6] = new Tile("trade_panel_NW"); //$NON-NLS-1$
		UIPanelState.tileTradePanel[7] = new Tile("trade_panel_SE"); //$NON-NLS-1$
		UIPanelState.tileTradePanel[8] = new Tile("trade_panel_SW"); //$NON-NLS-1$

		/*
		 * PRIORITIES PANEL
		 */
		UIPanelState.tilePrioritiesPanel = new Tile[9]; // Background/N/S/E/W/NE,NW,SE,SW
		UIPanelState.tilePrioritiesPanel[0] = new Tile("priorities_panel"); //$NON-NLS-1$
		UIPanelState.tilePrioritiesPanel[1] = new Tile("priorities_panel_N"); //$NON-NLS-1$
		UIPanelState.tilePrioritiesPanel[2] = new Tile("priorities_panel_S"); //$NON-NLS-1$
		UIPanelState.tilePrioritiesPanel[3] = new Tile("priorities_panel_E"); //$NON-NLS-1$
		UIPanelState.tilePrioritiesPanel[4] = new Tile("priorities_panel_W"); //$NON-NLS-1$
		UIPanelState.tilePrioritiesPanel[5] = new Tile("priorities_panel_NE"); //$NON-NLS-1$
		UIPanelState.tilePrioritiesPanel[6] = new Tile("priorities_panel_NW"); //$NON-NLS-1$
		UIPanelState.tilePrioritiesPanel[7] = new Tile("priorities_panel_SE"); //$NON-NLS-1$
		UIPanelState.tilePrioritiesPanel[8] = new Tile("priorities_panel_SW"); //$NON-NLS-1$
		UIPanelState.tilePrioritiesPanelUpIcon = new Tile("priorities_up"); //$NON-NLS-1$
		UIPanelState.tilePrioritiesPanelUpIconAlpha = UtilsGL.generateAlpha(UIPanelState.tilePrioritiesPanelUpIcon);
		UIPanelState.tilePrioritiesPanelDownIcon = new Tile("priorities_down"); //$NON-NLS-1$
		UIPanelState.tilePrioritiesPanelDownIconAlpha = UtilsGL.generateAlpha(UIPanelState.tilePrioritiesPanelDownIcon);

		/*
		 * MATS PANEL
		 */
		UIPanelState.tileMatsPanel = new Tile[9]; // Background/N/S/E/W/NE,NW,SE,SW
		UIPanelState.tileMatsPanel[0] = new Tile("mats_panel"); //$NON-NLS-1$
		UIPanelState.tileMatsPanel[1] = new Tile("mats_panel_N"); //$NON-NLS-1$
		UIPanelState.tileMatsPanel[2] = new Tile("mats_panel_S"); //$NON-NLS-1$
		UIPanelState.tileMatsPanel[3] = new Tile("mats_panel_E"); //$NON-NLS-1$
		UIPanelState.tileMatsPanel[4] = new Tile("mats_panel_W"); //$NON-NLS-1$
		UIPanelState.tileMatsPanel[5] = new Tile("mats_panel_NE"); //$NON-NLS-1$
		UIPanelState.tileMatsPanel[6] = new Tile("mats_panel_NW"); //$NON-NLS-1$
		UIPanelState.tileMatsPanel[7] = new Tile("mats_panel_SE"); //$NON-NLS-1$
		UIPanelState.tileMatsPanel[8] = new Tile("mats_panel_SW"); //$NON-NLS-1$
		UIPanelState.tileMatsPanelSubPanel = new Tile[9]; // Background/N/S/E/W/NE,NW,SE,SW
		UIPanelState.tileMatsPanelSubPanel[0] = new Tile("mats_subpanel"); //$NON-NLS-1$
		UIPanelState.tileMatsPanelSubPanel[1] = new Tile("mats_subpanel_N"); //$NON-NLS-1$
		UIPanelState.tileMatsPanelSubPanel[2] = new Tile("mats_subpanel_S"); //$NON-NLS-1$
		UIPanelState.tileMatsPanelSubPanel[3] = new Tile("mats_subpanel_E"); //$NON-NLS-1$
		UIPanelState.tileMatsPanelSubPanel[4] = new Tile("mats_subpanel_W"); //$NON-NLS-1$
		UIPanelState.tileMatsPanelSubPanel[5] = new Tile("mats_subpanel_NE"); //$NON-NLS-1$
		UIPanelState.tileMatsPanelSubPanel[6] = new Tile("mats_subpanel_NW"); //$NON-NLS-1$
		UIPanelState.tileMatsPanelSubPanel[7] = new Tile("mats_subpanel_SE"); //$NON-NLS-1$
		UIPanelState.tileMatsPanelSubPanel[8] = new Tile("mats_subpanel_SW"); //$NON-NLS-1$

		/*
		 * LIVINGS PANEL
		 */
		UIPanelState.tileLivingsPanel = new Tile[9]; // Background/N/S/E/W/NE,NW,SE,SW
		UIPanelState.tileLivingsPanel[0] = new Tile("livings_panel"); //$NON-NLS-1$
		UIPanelState.tileLivingsPanel[1] = new Tile("livings_panel_N"); //$NON-NLS-1$
		UIPanelState.tileLivingsPanel[2] = new Tile("livings_panel_S"); //$NON-NLS-1$
		UIPanelState.tileLivingsPanel[3] = new Tile("livings_panel_E"); //$NON-NLS-1$
		UIPanelState.tileLivingsPanel[4] = new Tile("livings_panel_W"); //$NON-NLS-1$
		UIPanelState.tileLivingsPanel[5] = new Tile("livings_panel_NE"); //$NON-NLS-1$
		UIPanelState.tileLivingsPanel[6] = new Tile("livings_panel_NW"); //$NON-NLS-1$
		UIPanelState.tileLivingsPanel[7] = new Tile("livings_panel_SE"); //$NON-NLS-1$
		UIPanelState.tileLivingsPanel[8] = new Tile("livings_panel_SW"); //$NON-NLS-1$
		UIPanelState.tileLivingsGroupPanel = new Tile[9]; // Background/N/S/E/W/NE,NW,SE,SW
		UIPanelState.tileLivingsGroupPanel[0] = new Tile("livings_group_panel"); //$NON-NLS-1$
		UIPanelState.tileLivingsGroupPanel[1] = new Tile("livings_group_panel_N"); //$NON-NLS-1$
		UIPanelState.tileLivingsGroupPanel[2] = new Tile("livings_group_panel_S"); //$NON-NLS-1$
		UIPanelState.tileLivingsGroupPanel[3] = new Tile("livings_group_panel_E"); //$NON-NLS-1$
		UIPanelState.tileLivingsGroupPanel[4] = new Tile("livings_group_panel_W"); //$NON-NLS-1$
		UIPanelState.tileLivingsGroupPanel[5] = new Tile("livings_group_panel_NE"); //$NON-NLS-1$
		UIPanelState.tileLivingsGroupPanel[6] = new Tile("livings_group_panel_NW"); //$NON-NLS-1$
		UIPanelState.tileLivingsGroupPanel[7] = new Tile("livings_group_panel_SE"); //$NON-NLS-1$
		UIPanelState.tileLivingsGroupPanel[8] = new Tile("livings_group_panel_SW"); //$NON-NLS-1$

		UIPanelState.tileLivingsPanelRowNoHead = new Tile("livings_nohead"); //$NON-NLS-1$
		UIPanelState.tileLivingsPanelRowNoBody = new Tile("livings_nobody"); //$NON-NLS-1$
		UIPanelState.tileLivingsPanelRowNoLegs = new Tile("livings_nolegs"); //$NON-NLS-1$
		UIPanelState.tileLivingsPanelRowNoFeet = new Tile("livings_nofeet"); //$NON-NLS-1$
		UIPanelState.tileLivingsPanelRowNoWeapon = new Tile("livings_noweapon"); //$NON-NLS-1$

		UIPanelState.tileLivingsRowAutoequip = new Tile("livings_autoequip"); //$NON-NLS-1$
		UIPanelState.tileLivingsRowAutoequipON = new Tile("livings_autoequipON"); //$NON-NLS-1$
		UIPanelState.tileLivingsRowAutoequipAlpha = UtilsGL.generateAlpha(UIPanelState.tileLivingsRowAutoequip);

		UIPanelState.tileLivingsRowProfession = new Tile("livings_professions"); //$NON-NLS-1$
		UIPanelState.tileLivingsRowProfessionON = new Tile("livings_professionsON"); //$NON-NLS-1$
		UIPanelState.tileLivingsRowProfessionAlpha = UtilsGL.generateAlpha(UIPanelState.tileLivingsRowProfession);
		UIPanelState.tileLivingsRowJobsGroups = new Tile("livings_jobgroup_change"); //$NON-NLS-1$
		UIPanelState.tileLivingsRowJobsGroupsON = new Tile("livings_jobgroup_changeON"); //$NON-NLS-1$
		UIPanelState.tileLivingsRowJobsGroupsAlpha = UtilsGL.generateAlpha(UIPanelState.tileLivingsRowJobsGroups);
		UIPanelState.tileLivingsRowConvertSoldier = new Tile("livings_convert_soldier"); //$NON-NLS-1$
		UIPanelState.tileLivingsRowConvertSoldierON = new Tile("livings_convert_soldierON"); //$NON-NLS-1$
		UIPanelState.tileLivingsRowConvertSoldierAlpha = UtilsGL.generateAlpha(UIPanelState.tileLivingsRowConvertSoldier);
		UIPanelState.tileLivingsRowConvertCivilian = new Tile("livings_convert_civilian"); //$NON-NLS-1$
		UIPanelState.tileLivingsRowConvertCivilianON = new Tile("livings_convert_civilianON"); //$NON-NLS-1$
		UIPanelState.tileLivingsRowConvertCivilianAlpha = UtilsGL.generateAlpha(UIPanelState.tileLivingsRowConvertCivilian);
		UIPanelState.tileLivingsRowConvertSoldierGuard = new Tile("livings_convert_soldier_guard"); //$NON-NLS-1$
		UIPanelState.tileLivingsRowConvertSoldierGuardON = new Tile("livings_convert_soldier_guardON"); //$NON-NLS-1$
		UIPanelState.tileLivingsRowConvertSoldierGuardAlpha = UtilsGL.generateAlpha(UIPanelState.tileLivingsRowConvertSoldierGuard);
		UIPanelState.tileLivingsRowConvertSoldierPatrol = new Tile("livings_convert_soldier_patrol"); //$NON-NLS-1$
		UIPanelState.tileLivingsRowConvertSoldierPatrolON = new Tile("livings_convert_soldier_patrolON"); //$NON-NLS-1$
		UIPanelState.tileLivingsRowConvertSoldierPatrolAlpha = UtilsGL.generateAlpha(UIPanelState.tileLivingsRowConvertSoldierPatrol);
		UIPanelState.tileLivingsRowConvertSoldierBoss = new Tile("livings_convert_soldier_boss"); //$NON-NLS-1$
		UIPanelState.tileLivingsRowConvertSoldierBossON = new Tile("livings_convert_soldier_bossON"); //$NON-NLS-1$
		UIPanelState.tileLivingsRowConvertSoldierBossAlpha = UtilsGL.generateAlpha(UIPanelState.tileLivingsRowConvertSoldierBoss);

		UIPanelState.tileLivingsRowGroupAdd = new Tile("livings_group_add"); //$NON-NLS-1$
		UIPanelState.tileLivingsRowGroupAddON = new Tile("livings_group_addON"); //$NON-NLS-1$
		UIPanelState.tileLivingsRowGroupAddAlpha = UtilsGL.generateAlpha(UIPanelState.tileLivingsRowGroupAdd);
		UIPanelState.tileLivingsRowGroupRemove = new Tile("livings_group_remove"); //$NON-NLS-1$
		UIPanelState.tileLivingsRowGroupRemoveON = new Tile("livings_group_removeON"); //$NON-NLS-1$
		UIPanelState.tileLivingsRowGroupRemoveAlpha = UtilsGL.generateAlpha(UIPanelState.tileLivingsRowGroupRemove);

		UIPanelState.tileLivingsGroup = new Tile("livings_group"); //$NON-NLS-1$
		UIPanelState.tileLivingsGroupON = new Tile("livings_groupON"); //$NON-NLS-1$
		UIPanelState.tileLivingsGroupGreen = new Tile("livings_group_green"); //$NON-NLS-1$
		UIPanelState.tileLivingsGroupAlpha = UtilsGL.generateAlpha(UIPanelState.tileLivingsGroup);
		UIPanelState.tileLivingsNoGroup = new Tile("livings_nogroup"); //$NON-NLS-1$
		UIPanelState.tileLivingsNoGroupON = new Tile("livings_nogroupON"); //$NON-NLS-1$
		UIPanelState.tileLivingsNoGroupGreen = new Tile("livings_nogroup_green"); //$NON-NLS-1$
		UIPanelState.tileLivingsNoGroupAlpha = UtilsGL.generateAlpha(UIPanelState.  tileLivingsNoGroup);

		UIPanelState.tileLivingsJobGroup = new Tile("livings_jobgroup"); //$NON-NLS-1$
		UIPanelState.tileLivingsJobGroupON = new Tile("livings_jobgroupON"); //$NON-NLS-1$
		UIPanelState.tileLivingsJobGroupGreen = new Tile("livings_jobgroup_green"); //$NON-NLS-1$
		UIPanelState.tileLivingsJobGroupAlpha = UtilsGL.generateAlpha(UIPanelState.tileLivingsJobGroup);
		UIPanelState.tileLivingsNoJobGroup = new Tile("livings_nojobgroup"); //$NON-NLS-1$
		UIPanelState.tileLivingsNoJobGroupON = new Tile("livings_nojobgroupON"); //$NON-NLS-1$
		UIPanelState.tileLivingsNoJobGroupGreen = new Tile("livings_nojobgroup_green"); //$NON-NLS-1$
		UIPanelState.tileLivingsNoJobGroupAlpha = UtilsGL.generateAlpha(UIPanelState.tileLivingsNoJobGroup);

		UIPanelState.tileLivingsSingleGroupRename = new Tile("livings_group_rename"); //$NON-NLS-1$
		UIPanelState.tileLivingsSingleGroupRenameON = new Tile("livings_group_renameON"); //$NON-NLS-1$
		UIPanelState.tileLivingsSingleGroupRenameAlpha = UtilsGL.generateAlpha(UIPanelState.tileLivingsSingleGroupRename);
		UIPanelState.tileLivingsSingleJobGroupRename = new Tile("livings_jobgroup_rename"); //$NON-NLS-1$
		UIPanelState.tileLivingsSingleJobGroupRenameON = new Tile("livings_jobgroup_renameON"); //$NON-NLS-1$
		UIPanelState.tileLivingsSingleJobGroupRenameAlpha = UtilsGL.generateAlpha(UIPanelState.tileLivingsSingleJobGroupRename);
		UIPanelState.tileLivingsSingleGroupGuard = new Tile("livings_group_guard"); //$NON-NLS-1$
		UIPanelState.tileLivingsSingleGroupGuardON = new Tile("livings_group_guardON"); //$NON-NLS-1$
		UIPanelState.tileLivingsSingleGroupGuardAlpha = UtilsGL.generateAlpha(UIPanelState.tileLivingsSingleGroupGuard);
		UIPanelState.tileLivingsSingleGroupPatrol = new Tile("livings_group_patrol"); //$NON-NLS-1$
		UIPanelState.tileLivingsSingleGroupPatrolON = new Tile("livings_group_patrolON"); //$NON-NLS-1$
		UIPanelState.tileLivingsSingleGroupPatrolAlpha = UtilsGL.generateAlpha(UIPanelState.tileLivingsSingleGroupPatrol);
		UIPanelState.tileLivingsSingleGroupBoss = new Tile("livings_group_boss"); //$NON-NLS-1$
		UIPanelState.tileLivingsSingleGroupBossON = new Tile("livings_group_bossON"); //$NON-NLS-1$
		UIPanelState.tileLivingsSingleGroupBossAlpha = UtilsGL.generateAlpha(UIPanelState.tileLivingsSingleGroupBoss);
		UIPanelState.tileLivingsSingleGroupDisband = new Tile("livings_group_disband"); //$NON-NLS-1$
		UIPanelState.tileLivingsSingleGroupDisbandON = new Tile("livings_group_disbandON"); //$NON-NLS-1$
		UIPanelState.tileLivingsSingleGroupDisbandAlpha = UtilsGL.generateAlpha(UIPanelState.tileLivingsSingleGroupDisband);
		UIPanelState.tileLivingsSingleJobGroupDisband = new Tile("livings_jobgroup_disband"); //$NON-NLS-1$
		UIPanelState.tileLivingsSingleJobGroupDisbandON = new Tile("livings_jobgroup_disbandON"); //$NON-NLS-1$
		UIPanelState.tileLivingsSingleJobGroupDisbandAlpha = UtilsGL.generateAlpha(UIPanelState.tileLivingsSingleJobGroupDisband);

		UIPanelState.tileLivingsSingleGroupChangeJobs = new Tile("livings_jobgroup_changejob"); //$NON-NLS-1$
		UIPanelState.tileLivingsSingleGroupChangeJobsON = new Tile("livings_jobgroup_changejobON"); //$NON-NLS-1$
		UIPanelState.tileLivingsSingleGroupChangeJobsAlpha = UtilsGL.generateAlpha(UIPanelState.tileLivingsSingleGroupChangeJobs);

		/*
		 * MENU (right)
		 */
		UIPanelState.tileMenuPanel = new Tile[9]; // Background/N/S/E/W/NE,NW,SE,SW
		UIPanelState.tileMenuPanel[0] = new Tile("menu_panel"); //$NON-NLS-1$
		UIPanelState.tileMenuPanel[1] = new Tile("menu_panel_N"); //$NON-NLS-1$
		UIPanelState.tileMenuPanel[2] = new Tile("menu_panel_S"); //$NON-NLS-1$
		UIPanelState.tileMenuPanel[3] = new Tile("menu_panel_E"); //$NON-NLS-1$
		UIPanelState.tileMenuPanel[4] = new Tile("menu_panel_W"); //$NON-NLS-1$
		UIPanelState.tileMenuPanel[5] = new Tile("menu_panel_NE"); //$NON-NLS-1$
		UIPanelState.tileMenuPanel[6] = new Tile("menu_panel_NW"); //$NON-NLS-1$
		UIPanelState.tileMenuPanel[7] = new Tile("menu_panel_SE"); //$NON-NLS-1$
		UIPanelState.tileMenuPanel[8] = new Tile("menu_panel_SW"); //$NON-NLS-1$
		UIPanelState.tileOpenRightMenu = new Tile("icon_openRight"); //$NON-NLS-1$
		UIPanelState.tileOpenRightMenuAlpha = UtilsGL.generateAlpha(UIPanelState.tileOpenRightMenu);
		UIPanelState.tileOpenRightMenuON = new Tile("icon_openRightON"); //$NON-NLS-1$
		UIPanelState.tileOpenRightMenuONAlpha = UtilsGL.generateAlpha(UIPanelState.tileOpenRightMenuON);

		/*
		 * Mini icons
		 */
		UIPanelState.tileIconCitizenNext = new Tile("icon_citizennext"); //$NON-NLS-1$
		UIPanelState.tileIconCitizenNextON = new Tile("icon_citizennextON"); //$NON-NLS-1$
		UIPanelState.tileIconCitizenPrevious = new Tile("icon_citizenprevious"); //$NON-NLS-1$
		UIPanelState.tileIconCitizenPreviousON = new Tile("icon_citizenpreviousON"); //$NON-NLS-1$
		UIPanelState.tileIconSoldierNext = new Tile("icon_soldiernext"); //$NON-NLS-1$
		UIPanelState.tileIconSoldierNextON = new Tile("icon_soldiernextON"); //$NON-NLS-1$
		UIPanelState.tileIconSoldierPrevious = new Tile("icon_soldierprevious"); //$NON-NLS-1$
		UIPanelState.tileIconSoldierPreviousON = new Tile("icon_soldierpreviousON"); //$NON-NLS-1$
		UIPanelState.tileIconHeroNext = new Tile("icon_heronext"); //$NON-NLS-1$
		UIPanelState.tileIconHeroNextON = new Tile("icon_heronextON"); //$NON-NLS-1$
		UIPanelState.tileIconHeroPrevious = new Tile("icon_heroprevious"); //$NON-NLS-1$
		UIPanelState.tileIconHeroPreviousON = new Tile("icon_heropreviousON"); //$NON-NLS-1$

		UIPanelState.tileIconNextMiniAlpha = UtilsGL.generateAlpha(UIPanelState.tileIconCitizenNext);
		UIPanelState.tileIconPreviousMiniAlpha = UtilsGL.generateAlpha(UIPanelState.tileIconCitizenPrevious);

		UIPanelState.tileIconLevelDown = new Tile("icon_leveldown"); //$NON-NLS-1$
		UIPanelState.tileIconLevelDownAlpha = UtilsGL.generateAlpha(UIPanelState.tileIconLevelDown);
		UIPanelState.tileIconLevel = new Tile("icon_level"); //$NON-NLS-1$
		UIPanelState.tileIconLevelAlpha = UtilsGL.generateAlpha(UIPanelState.tileIconLevel);
		UIPanelState.tileIconLevelUp = new Tile("icon_levelup"); //$NON-NLS-1$
		UIPanelState.tileIconLevelUpAlpha = UtilsGL.generateAlpha(UIPanelState.tileIconLevelUp);

		/*
		 * Icons
		 */
		UIPanelState.tileIconPriorities = new Tile("icon_priorities"); //$NON-NLS-1$
		UIPanelState.tileIconPrioritiesON = new Tile("icon_prioritiesON"); //$NON-NLS-1$
		UIPanelState.tileIconPrioritiesAlpha = UtilsGL.generateAlpha(UIPanelState.tileIconPriorities);
		UIPanelState.tileIconMats = new Tile("icon_mats"); //$NON-NLS-1$
		UIPanelState.tileIconMatsON = new Tile("icon_matsON"); //$NON-NLS-1$
		UIPanelState.tileIconMatsAlpha = UtilsGL.generateAlpha(UIPanelState.tileIconMats);
		UIPanelState.tileIconSettings = new Tile("icon_settings"); //$NON-NLS-1$
		UIPanelState.tileIconSettingsAlpha = UtilsGL.generateAlpha(UIPanelState.tileIconSettings);
		UIPanelState.tileIconGrid = new Tile("icon_grid"); //$NON-NLS-1$
		UIPanelState.tileIconGridON = new Tile("icon_gridON"); //$NON-NLS-1$
		UIPanelState.tileIconGridAlpha = UtilsGL.generateAlpha(UIPanelState.tileIconGrid);
		UIPanelState.tileIconMiniblocks = new Tile("icon_miniblock"); //$NON-NLS-1$
		UIPanelState.tileIconMiniblocksON = new Tile("icon_miniblockON"); //$NON-NLS-1$
		UIPanelState.tileIconMiniblocksAlpha = UtilsGL.generateAlpha(UIPanelState.tileIconMiniblocks);
		UIPanelState.tileIconFlatMouse = new Tile("icon_flatmouse"); //$NON-NLS-1$
		UIPanelState.tileIconFlatMouseON = new Tile("icon_flatmouseON"); //$NON-NLS-1$
		UIPanelState.tileIconFlatMouseAlpha = UtilsGL.generateAlpha(UIPanelState.tileIconFlatMouse);
		UIPanelState.tileIcon3DMouse = new Tile("icon_3d_mouse"); //$NON-NLS-1$
		UIPanelState.tileIcon3DMouseON = new Tile("icon_3d_mouseON"); //$NON-NLS-1$
		UIPanelState.tileIcon3DMouseAlpha = UtilsGL.generateAlpha(UIPanelState.tileIcon3DMouse);
		UIPanelState.tileIconPause = new Tile("icon_pause"); //$NON-NLS-1$
		UIPanelState.tileIconResume = new Tile("icon_resume"); //$NON-NLS-1$
		UIPanelState.tileIconPauseResumeAlpha = UtilsGL.generateAlpha(UIPanelState.tileIconPause);
		UIPanelState.tileIconIncreaseSpeed = new Tile("icon_increase_speed"); //$NON-NLS-1$
		UIPanelState.tileIconIncreaseSpeedON = new Tile("icon_increase_speedON"); //$NON-NLS-1$
		UIPanelState.tileIconIncreaseSpeedAlpha = UtilsGL.generateAlpha(UIPanelState.tileIconIncreaseSpeed);
		UIPanelState.tileIconLowerSpeed = new Tile("icon_lower_speed"); //$NON-NLS-1$
		UIPanelState.tileIconLowerSpeedON = new Tile("icon_lower_speedON"); //$NON-NLS-1$
		UIPanelState.tileIconLowerSpeedAlpha = UtilsGL.generateAlpha(UIPanelState.tileIconLowerSpeed);

		/*
		 * Info
		 */
		UIPanelState.tileIconNumCitizens = new Tile("icon_numcitizens"); //$NON-NLS-1$
		UIPanelState.tileIconNumSoldiers = new Tile("icon_numsoldiers"); //$NON-NLS-1$
		UIPanelState.tileIconNumHeroes = new Tile("icon_numheroes"); //$NON-NLS-1$
		UIPanelState.tileIconCaravan = new Tile("icon_caravan"); //$NON-NLS-1$
		UIPanelState.tileIconCaravanON = new Tile("icon_caravanON"); //$NON-NLS-1$

		UIPanelState.tileInfoPanel = new Tile("info_panel"); //$NON-NLS-1$
		UIPanelState.tileInfoPanelAlpha = UtilsGL.generateAlpha(UIPanelState.tileInfoPanel);
		UIPanelState.tileDatePanel = new Tile("date_panel"); //$NON-NLS-1$
		UIPanelState.tileDatePanelAlpha = UtilsGL.generateAlpha(UIPanelState.tileDatePanel);

		UIPanelState.tileIconCoins = new Tile("icon_coins"); //$NON-NLS-1$

		UIPanelState.tileIconTutorial = new Tile("icon_tutorial"); //$NON-NLS-1$

		// tileIconGods = new Tile ("icon_gods");

		/*
		 * Tooltip
		 */
		UIPanelState.tileTooltipBackground = new Tile("tooltip_background"); //$NON-NLS-1$
    }
}
