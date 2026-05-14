package xaos.panels.UI;

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
import xaos.panels.CommandPanel;
import xaos.panels.ImagesPanel;
import xaos.panels.MainPanel;
import xaos.panels.MatsPanelData;
import xaos.panels.MessagesPanel;
import xaos.panels.MiniMapPanel;
import xaos.panels.TradePanel;
import xaos.panels.TypingPanel;
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
import xaos.utils.UIScale;
import xaos.utils.UtilFont;
import xaos.utils.UtilsAL;
import xaos.utils.UtilsGL;
import xaos.utils.UtilsIniHeaders;
import xaos.utils.UtilsKeyboard;
import static xaos.panels.UI.UIPanelState.*;
import static xaos.panels.UI.UIPanelInputHandler.*;

public final class UIPanel {

	public UIPanel() {
		if (Game.getWorld() != null) {
			resize(UtilsGL.getWidth(), UtilsGL.getHeight(), Game.getWorld().getCampaignID(),
					Game.getWorld().getMissionID(), false);
		} else {
			resize(UtilsGL.getWidth(), UtilsGL.getHeight(), null, null, false);
		}
	}

	public void loadMenus(String sCampaignID, String sMissionID) {
		setCurrentMenu(new SmartMenu());
		SmartMenu.readXMLMenu(getCurrentMenu(), "menu.xml", sCampaignID, sMissionID); //$NON-NLS-1$

		menuPanelMenu = new SmartMenu();
		SmartMenu.readXMLMenu(menuPanelMenu, "menu_right.xml", sCampaignID, sMissionID); //$NON-NLS-1$

		productionPanelMenu = new SmartMenu();
		SmartMenu.readXMLMenu(productionPanelMenu, "menu_production.xml", sCampaignID, sMissionID); //$NON-NLS-1$

		// Vamos a setear los tamaños de los iconos de los menús para que sea
		// proporcional al botón de menú
		resizeIcons(currentMenu, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT);
		resizeIcons(menuPanelMenu, MENU_ITEM_WIDTH, MENU_ITEM_HEIGHT);
		resizeIcons(productionPanelMenu, PRODUCTION_PANEL_ITEM_WIDTH,
				PRODUCTION_PANEL_ITEM_HEIGHT);
	}

	public static void resizeIcons(SmartMenu menu, int width, int height) {
		if (menu.getItems() != null) {
			for (int i = 0; i < menu.getItems().size(); i++) {
				resizeIcons(menu.getItems().get(i), width, height);
			}

			if (menu.getIcon() != null) {
				Tile tile = menu.getIcon();
				if (tile.getTileWidth() > width || tile.getTileHeight() > height) {
					float relation = (float) tile.getTileWidth() / (float) tile.getTileHeight();

					if (tile.getTileWidth() > tile.getTileHeight()) {
						tile.setTileWidth(width);
						tile.setTileHeight((int) (width / relation));
					} else {
						tile.setTileHeight(height);
						tile.setTileWidth((int) (height * relation));
					}
				}
			}
		}
	}

	public void resize(int renderW, int renderH, String sCampaignID, String sMissionID, boolean bLoadMenus) {
		renderWidth = renderW;
		renderHeight = renderH;

		initialize(sCampaignID, sMissionID, bLoadMenus);
	}

	public SmartMenu getCurrentMenu() {
		return currentMenu;
	}

	public void setCurrentMenu(SmartMenu menu) {
		currentMenu = menu;
	}

	public void initialize(String sCampaignID, String sMissionID, boolean bLoadMenus) {
		if (currentMenu == null && bLoadMenus) {
			loadMenus(sCampaignID, sMissionID);
		}

		if (tileBottomItem == null) {
			// generateTiles();
			GenerateTiles generateTiles = new GenerateTiles();
			generateTiles.generateTiles();
		}

		PIXELS_TO_BORDER = renderWidth / 80;

		// MINIMAP
		MINIMAP_PANEL_WIDTH = tileMinimapPanel.getTileWidth();
		MINIMAP_PANEL_HEIGHT = tileMinimapPanel.getTileHeight();

		minimapPanelX = renderWidth - MINIMAP_PANEL_WIDTH
				- PIXELS_TO_BORDER;
		minimapPanelY = PIXELS_TO_BORDER;
		MiniMapPanel.initialize(minimapPanelX, minimapPanelY,
				MINIMAP_PANEL_WIDTH, MINIMAP_PANEL_HEIGHT);

		/*
		 * BOTTOM panel
		 */
		if (bottomPanelItemsPosition == null) {
			bottomPanelItemsPosition = new ArrayList<Point>(BOTTOM_PANEL_NUM_ITEMS);
		}

		// Centramos el panel
		bottomPanelX = renderWidth / 2 - BOTTOM_PANEL_WIDTH / 2;
		bottomPanelY = renderHeight - BOTTOM_PANEL_HEIGHT
				- tileOpenBottomMenu.getTileHeight();
		// Calculamos la posición de los minipaneles de scroll
		bottomPanelLeftScrollX = bottomPanelX - BOTTOM_PANEL_SCROLL_WIDTH;
		bottomPanelRightScrollX = bottomPanelX + BOTTOM_PANEL_WIDTH;

		bottomPanelItemIndex = 0;

		// Subpanel
		bottomSubPanelMenu = null;

		// Cargamos las posiciones
		bottomPanelItemsPosition.clear();
		int spaceBetweenItems = (BOTTOM_PANEL_WIDTH
				- (BOTTOM_ITEM_WIDTH * BOTTOM_PANEL_NUM_ITEMS))
				/ (BOTTOM_PANEL_NUM_ITEMS + 1);
		for (int i = 0; i < BOTTOM_PANEL_NUM_ITEMS; i++) {
			bottomPanelItemsPosition
					.add(new Point(
							bottomPanelX + spaceBetweenItems
									+ (i * (BOTTOM_ITEM_WIDTH + spaceBetweenItems)),
							bottomPanelY + (BOTTOM_PANEL_HEIGHT / 2)
									- (BOTTOM_ITEM_HEIGHT / 2)));
		}

		// Minibotón para abrir/cerrar el panel de abajo
		tileOpenCloseBottomMenuPoint.setLocation(
				renderWidth / 2 - tileOpenBottomMenu.getTileWidth() / 2,
				renderHeight - tileOpenBottomMenu.getTileHeight());

		/*
		 * Date panel
		 */
		datePanelPoint.setLocation(
				renderWidth / 2 - tileDatePanel.getTileWidth() / 2,
				tileIconCoins.getTileHeight());

		/*
		 * Coins icon point
		 */
		tileIconCoinsPoint.setLocation(renderWidth / 2,
				0 + tileIconCoins.getTileHeightOffset());

		/*
		 * Info panel
		 */
		infoPanelPoint.setLocation(
				renderWidth / 2 - tileInfoPanel.getTileWidth() / 2
						+ tileInfoPanel.getTileWidthOffset(),
				0);

		int iSeparation = datePanelPoint.x - infoPanelPoint.x;
		iSeparation = iSeparation - 2 * tileBottomItem.getTileWidth();
		iSeparation /= 3;
		// Citizens
		iconNumCitizensBackgroundPoint.setLocation(infoPanelPoint.x + iSeparation,
				infoPanelPoint.y + tileIconNumCitizens.getTileHeightOffset());
		iconNumCitizensPoint.setLocation(
				iconNumCitizensBackgroundPoint.x + tileIconNumCitizens.getTileWidthOffset(),
				iconNumCitizensBackgroundPoint.y + tileIconNumCitizens.getTileHeightOffset());
		iconCitizenPreviousPoint.setLocation(
				iconNumCitizensBackgroundPoint.x
						+ tileIconCitizenPrevious.getTileWidthOffset(),
				iconNumCitizensBackgroundPoint.y
						+ tileIconCitizenPrevious.getTileHeightOffset());
		iconCitizenNextPoint.setLocation(
				iconNumCitizensBackgroundPoint.x + tileIconCitizenNext.getTileWidthOffset(),
				iconNumCitizensBackgroundPoint.y + tileIconCitizenNext.getTileHeightOffset());

		// Soldiers
		iconNumSoldiersBackgroundPoint.setLocation(
				infoPanelPoint.x + 2 * iSeparation + tileBottomItem.getTileWidth(),
				infoPanelPoint.y + tileIconNumSoldiers.getTileHeightOffset());
		iconNumSoldiersPoint.setLocation(
				iconNumSoldiersBackgroundPoint.x + tileIconNumSoldiers.getTileWidthOffset(),
				iconNumSoldiersBackgroundPoint.y + tileIconNumSoldiers.getTileHeightOffset());
		iconSoldierPreviousPoint.setLocation(
				iconNumSoldiersBackgroundPoint.x
						+ tileIconSoldierPrevious.getTileWidthOffset(),
				iconNumSoldiersBackgroundPoint.y
						+ tileIconSoldierPrevious.getTileHeightOffset());
		iconSoldierNextPoint.setLocation(
				iconNumSoldiersBackgroundPoint.x + tileIconSoldierNext.getTileWidthOffset(),
				iconNumSoldiersBackgroundPoint.y + tileIconSoldierNext.getTileHeightOffset());

		iSeparation = infoPanelPoint.x + tileInfoPanel.getTileWidth()
				- (datePanelPoint.x + tileDatePanel.getTileWidth());
		iSeparation = iSeparation - 2 * tileBottomItem.getTileWidth();
		iSeparation /= 3;

		// Heroes
		iconNumHeroesBackgroundPoint.setLocation(
				datePanelPoint.x + tileDatePanel.getTileWidth() + iSeparation,
				infoPanelPoint.y + tileIconNumHeroes.getTileHeightOffset());
		iconNumHeroesPoint.setLocation(
				iconNumHeroesBackgroundPoint.x + tileIconNumHeroes.getTileWidthOffset(),
				iconNumHeroesBackgroundPoint.y + tileIconNumHeroes.getTileHeightOffset());
		iconHeroPreviousPoint.setLocation(
				iconNumHeroesBackgroundPoint.x + tileIconHeroPrevious.getTileWidthOffset(),
				iconNumHeroesBackgroundPoint.y + tileIconHeroPrevious.getTileHeightOffset());
		iconHeroNextPoint.setLocation(
				iconNumHeroesBackgroundPoint.x + tileIconHeroNext.getTileWidthOffset(),
				iconNumHeroesBackgroundPoint.y + tileIconHeroNext.getTileHeightOffset());

		// Caravan
		iconCaravanBackgroundPoint.setLocation(
				datePanelPoint.x + tileDatePanel.getTileWidth() + 2 * iSeparation
						+ tileBottomItem.getTileWidth(),
				infoPanelPoint.y + tileIconCaravan.getTileHeightOffset());
		iconCaravanPoint.setLocation(
				iconCaravanBackgroundPoint.x + tileIconCaravan.getTileWidthOffset(),
				iconCaravanBackgroundPoint.y + tileBottomItem.getTileHeight() / 2
						- tileIconCaravan.getTileHeight() / 2);

		if (bLoadMenus) {
			/*
			 * Menu panel (menú de la derecha)
			 */
			createMenuPanel(menuPanelMenu);

			/*
			 * Production panel
			 */
			createProductionPanel(productionPanelMenu);

			/*
			 * Trade panel
			 */
			createTradePanel();

			/*
			 * Mats panel
			 */
			createMatsPanel();

			/*
			 * Stockpile panel (piles + containers)
			 */
			createPilePanel();

			/*
			 * Professions panel
			 */
			createProfessionsPanel();

			/*
			 * Livings panel
			 */
			createLivingsPanel(LIVINGS_PANEL_TYPE_NONE, -1, -1);

			/*
			 * Priorities panel
			 */
			createPrioritiesPanel();

			/*
			 * Images panel
			 */
			ImagesPanel.resize(renderWidth, renderHeight);
		}

		/*
		 * Messages panel
		 */
		createMessagesPanel();

		// Images button location
		iconTutorialPoint.setLocation(messageIconPoints[0].x,
				messageIconPoints[0].y + messageTiles[0].getTileHeight()
						+ PIXELS_TO_BORDER + BOTTOM_ITEM_HEIGHT
						+ BOTTOM_ITEM_HEIGHT / 4);

		// Events + gods icons
		int iStartingX = messageIconPoints[messageIconPoints.length - 1].x
				+ messageTiles[messageTiles.length - 1].getTileWidth();
		int iAvailableWidth = infoPanelPoint.x - iStartingX;

		iconEventsPoint.setLocation(iStartingX + iAvailableWidth / 4 - ICON_WIDTH / 2 + 3,
				messageIconPoints[messageIconPoints.length - 1].y
						+ messageTiles[messageTiles.length - 1].getTileHeight() / 2
						- GlobalEventData.getIcon().getTileHeight() / 2);

		// Gods icon
		// iconGodsPoint.setLocation (iStartingX + (iAvailableWidth / 4) * 3 -
		// ICON_WIDTH / 2 + 3, iconEventsPoint.y + GlobalEventData.getIcon
		// ().getTileHeight () / 2 - tileIconGods.getTileHeight () / 2);

		/*
		 * Mini icons
		 */
		iconLevelUpPoint.setLocation(
				minimapPanelX + tileIconLevelUp.getTileWidthOffset(),
				minimapPanelY + tileIconLevelUp.getTileHeightOffset());
		iconLevelPoint.setLocation(
				minimapPanelX + tileIconLevel.getTileWidthOffset(),
				minimapPanelY + tileIconLevel.getTileHeightOffset());
		iconLevelDownPoint.setLocation(
				minimapPanelX + tileIconLevelDown.getTileWidthOffset(),
				minimapPanelY + tileIconLevelDown.getTileHeightOffset());

		// Debajo del date metemos 2 iconos de panel (de momento priorities y mats)
		int iPanels = (tileDatePanel.getTileWidth() - tileIconPriorities.getTileWidth()
				- tileIconMats.getTileWidth())
				/ 3;
		iconMatsPoint.setLocation(datePanelPoint.x + iPanels,
				datePanelPoint.y + tileDatePanel.getTileHeight()
						+ tileIconMats.getTileHeightOffset());
		iconPrioritiesPoint.setLocation(
				datePanelPoint.x + iPanels + tileIconMats.getTileWidth() + iPanels,
				datePanelPoint.y + tileDatePanel.getTileHeight()
						+ tileIconPriorities.getTileHeightOffset());

		// Miniblocks, grid, settings, flat mouse, 3D mouse
		iconMiniblocksPoint.setLocation(
				minimapPanelX + tileIconMiniblocks.getTileWidthOffset(),
				minimapPanelY + tileIconMiniblocks.getTileHeightOffset());
		iconGridPoint.setLocation(
				minimapPanelX + tileIconGrid.getTileWidthOffset(),
				minimapPanelY + tileIconGrid.getTileHeightOffset());
		iconSettingsPoint.setLocation(
				minimapPanelX + tileIconSettings.getTileWidthOffset(),
				minimapPanelY + tileIconSettings.getTileHeightOffset());
		iconFlatMousePoint.setLocation(
				minimapPanelX + tileIconFlatMouse.getTileWidthOffset(),
				minimapPanelY + tileIconFlatMouse.getTileHeightOffset());
		icon3DMousePoint.setLocation(
				minimapPanelX + tileIcon3DMouse.getTileWidthOffset(),
				minimapPanelY + tileIcon3DMouse.getTileHeightOffset());

		// Lower speed, pause/resume, increase speed
		iconLowerSpeedPoint.setLocation(
				minimapPanelX + tileIconLowerSpeedON.getTileWidthOffset(),
				minimapPanelY + tileIconLowerSpeedON.getTileHeightOffset());
		iconPauseResumePoint.setLocation(
				minimapPanelX + tileIconPause.getTileWidthOffset(),
				minimapPanelY + tileIconPause.getTileHeightOffset());
		iconIncreaseSpeedPoint.setLocation(
				minimapPanelX + tileIconIncreaseSpeedON.getTileWidthOffset(),
				minimapPanelY + tileIconIncreaseSpeedON.getTileHeightOffset());

		// Edge menus
		if (isProductionPanelLocked()) {
			setProductionPanelActive(true);
		}

		if (isMenuPanelLocked()) {
			UIPanel.setMenuPanelActive(true);
		}

		if (UIPanel.isBottomMenuPanelLocked()) {
			UIPanel.setBottomMenuPanelActive(true, true);
		}
	}

	public void render() {
		if (MainPanel.bHideUION) {
			return;
		}

		int mouseX = Mouse.getX();
		int mouseY = renderHeight - Mouse.getY() - 1;
		delayTime++;
		blinkTurns++;
		if (blinkTurns >= MAX_BLINK_TURNS) {
			blinkTurns = 0;
		}

		int mousePanel = isMouseOnAPanel(mouseX, mouseY, true);

		/*
		 * BOTTOM menu panel
		 */
		int iCurrentTexture = tileBottomScrollLeft.getTextureID(); // Esta textura es la primera quese usa
																				// en el bottom
		// menu
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, iCurrentTexture);
		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		GL11.glColor4f(1, 1, 1, 1);
		UtilsGL.glBegin(GL11.GL_QUADS);

		checkBlinkBottom = (blinkTurns >= MAX_BLINK_TURNS / 2)
				&& TutorialFlow.isBlinkBottom();
		if (UIPanel.isBottomMenuPanelActive()) {
			iCurrentTexture = BottomMenuPanel.renderPanel(mouseX, mouseY, mousePanel, iCurrentTexture);
		}

		// Rendereamos el botoncito para hacer visible/invisible el bottom panel
		if (UIPanel.isBottomMenuPanelLocked()) {
			iCurrentTexture = UtilsGL.setTexture(tileOpenBottomMenuON, iCurrentTexture);
			drawTile(tileOpenBottomMenuON, tileOpenCloseBottomMenuPoint, tileOpenBottomMenuON.getTileWidth(),
					tileOpenBottomMenuON.getTileHeight(), mousePanel == MOUSE_BOTTOM_OPENCLOSE);
		} else {
			iCurrentTexture = UtilsGL.setTexture(tileOpenBottomMenu, iCurrentTexture);
			if (checkBlinkBottom) {
				UtilsGL.setColorRed();
			}
			drawTile(tileOpenBottomMenu, tileOpenCloseBottomMenuPoint, tileOpenBottomMenu.getTileWidth(),
					tileOpenBottomMenu.getTileHeight(), mousePanel == MOUSE_BOTTOM_OPENCLOSE);
			if (checkBlinkBottom) {
				UtilsGL.unsetColor();
			}
		}

		/*
		 * MINIMAP (textures)
		 */
		// Minimap background
		iCurrentTexture = UtilsGL.setTexture(tileMinimapPanel, iCurrentTexture);
		UtilsGL.drawTexture(minimapPanelX, minimapPanelY, minimapPanelX + MINIMAP_PANEL_WIDTH,
				minimapPanelY + MINIMAP_PANEL_HEIGHT, tileMinimapPanel.getTileSetTexX0(),
				tileMinimapPanel.getTileSetTexY0(), tileMinimapPanel.getTileSetTexX1(),
				tileMinimapPanel.getTileSetTexY1());

		UtilsGL.glEnd();

		// Minimap content
		MiniMapPanel.render();

		/*
		 * MENU (right)
		 */
		renderMenuPanel(mouseX, mouseY, mousePanel);

		// Possible mini icon blinks?
		// Blink
		TutorialFlow tutorialFlow = null;
		if ((blinkTurns >= MAX_BLINK_TURNS / 2)) {
			if (Game.getCurrentMissionData() != null && ImagesPanel.getCurrentFlowIndex() >= 0
					&& ImagesPanel.getCurrentFlowIndex() < Game.getCurrentMissionData().getTutorialFlows().size()) {
				tutorialFlow = Game.getCurrentMissionData().getTutorialFlows().get(ImagesPanel.getCurrentFlowIndex());
			}
		}

		/*
		 * Info
		 */
		iCurrentTexture = tileInfoPanel.getTextureID();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, iCurrentTexture);
		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		UtilsGL.glBegin(GL11.GL_QUADS);
		drawTile(tileInfoPanel, infoPanelPoint);
		iCurrentTexture = UtilsGL.setTexture(tileDatePanel, iCurrentTexture);
		drawTile(tileDatePanel, datePanelPoint);

		// Level up/down
		iCurrentTexture = UtilsGL.setTexture(tileIconLevelUp, iCurrentTexture);
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniLevelUp()) {
			UtilsGL.setColorRed();
		}
		drawTile(tileIconLevelUp, iconLevelUpPoint, ICON_WIDTH, ICON_HEIGHT, mousePanel == MOUSE_ICON_LEVEL_UP);
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniLevelUp()) {
			UtilsGL.unsetColor();
		}
		iCurrentTexture = UtilsGL.setTexture(tileIconLevelDown, iCurrentTexture);
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniLevelDown()) {
			UtilsGL.setColorRed();
		}
		drawTile(tileIconLevelDown, iconLevelDownPoint, ICON_WIDTH, ICON_HEIGHT, mousePanel == MOUSE_ICON_LEVEL_DOWN);
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniLevelDown()) {
			UtilsGL.unsetColor();
		}
		iCurrentTexture = UtilsGL.setTexture(tileIconLevel, iCurrentTexture);
		drawTile(tileIconLevel, iconLevelPoint);

		// Num citizens / soldiers / heroes / caravan
		iCurrentTexture = UtilsGL.setTexture(tileBottomItem, iCurrentTexture);
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniCitizens()) {
			UtilsGL.setColorRed();
		}
		drawTile(tileBottomItem, iconNumCitizensBackgroundPoint);
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniCitizens()) {
			UtilsGL.unsetColor();
		}
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniSoldiers()) {
			UtilsGL.setColorRed();
		}
		drawTile(tileBottomItem, iconNumSoldiersBackgroundPoint);
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniSoldiers()) {
			UtilsGL.unsetColor();
		}
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniHeroes()) {
			UtilsGL.setColorRed();
		}
		drawTile(tileBottomItem, iconNumHeroesBackgroundPoint);
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniHeroes()) {
			UtilsGL.unsetColor();
		}
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniTrade()) {
			UtilsGL.setColorRed();
		}
		drawTile(tileBottomItem, iconCaravanBackgroundPoint);
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniTrade()) {
			UtilsGL.unsetColor();
		}
		iCurrentTexture = UtilsGL.setTexture(tileIconNumCitizens, iCurrentTexture);
		drawTile(tileIconNumCitizens, iconNumCitizensPoint, (mousePanel == MOUSE_INFO_NUM_CITIZENS));
		iCurrentTexture = UtilsGL.setTexture(tileIconNumSoldiers, iCurrentTexture);
		drawTile(tileIconNumSoldiers, iconNumSoldiersPoint, (mousePanel == MOUSE_INFO_NUM_SOLDIERS));
		iCurrentTexture = UtilsGL.setTexture(tileIconNumHeroes, iCurrentTexture);
		drawTile(tileIconNumHeroes, iconNumHeroesPoint, (mousePanel == MOUSE_INFO_NUM_HEROES));
		if (Game.getWorld().getCurrentCaravanData() != null) {
			iCurrentTexture = UtilsGL.setTexture(tileIconCaravanON, iCurrentTexture);
			drawTile(tileIconCaravanON, iconCaravanPoint, (mousePanel == MOUSE_INFO_CARAVAN));
		} else {
			iCurrentTexture = UtilsGL.setTexture(tileIconCaravan, iCurrentTexture);
			drawTile(tileIconCaravan, iconCaravanPoint, (mousePanel == MOUSE_INFO_CARAVAN));
		}

		// Previous/next citizen/soldiers/heroes
		iCurrentTexture = UtilsGL.setTexture(tileIconCitizenPrevious, iCurrentTexture);
		if (mousePanel == MOUSE_ICON_CITIZEN_PREVIOUS) {
			drawTile(tileIconCitizenPreviousON, iconCitizenPreviousPoint);
		} else {
			drawTile(tileIconCitizenPrevious, iconCitizenPreviousPoint);
		}
		iCurrentTexture = UtilsGL.setTexture(tileIconCitizenNext, iCurrentTexture);
		if (mousePanel == MOUSE_ICON_CITIZEN_NEXT) {
			drawTile(tileIconCitizenNextON, iconCitizenNextPoint);
		} else {
			drawTile(tileIconCitizenNext, iconCitizenNextPoint);
		}
		iCurrentTexture = UtilsGL.setTexture(tileIconSoldierPrevious, iCurrentTexture);
		if (mousePanel == MOUSE_ICON_SOLDIER_PREVIOUS) {
			drawTile(tileIconSoldierPreviousON, iconSoldierPreviousPoint);
		} else {
			drawTile(tileIconSoldierPrevious, iconSoldierPreviousPoint);
		}
		iCurrentTexture = UtilsGL.setTexture(tileIconSoldierNext, iCurrentTexture);
		if (mousePanel == MOUSE_ICON_SOLDIER_NEXT) {
			drawTile(tileIconSoldierNextON, iconSoldierNextPoint);
		} else {
			drawTile(tileIconSoldierNext, iconSoldierNextPoint);
		}
		iCurrentTexture = UtilsGL.setTexture(tileIconHeroPrevious, iCurrentTexture);
		if (mousePanel == MOUSE_ICON_HERO_PREVIOUS) {
			drawTile(tileIconHeroPreviousON, iconHeroPreviousPoint);
		} else {
			drawTile(tileIconHeroPrevious, iconHeroPreviousPoint);
		}
		iCurrentTexture = UtilsGL.setTexture(tileIconHeroNext, iCurrentTexture);
		if (mousePanel == MOUSE_ICON_HERO_NEXT) {
			drawTile(tileIconHeroNextON, iconHeroNextPoint);
		} else {
			drawTile(tileIconHeroNext, iconHeroNextPoint);
		}

		// Panel icons (priorities, mats)
		iCurrentTexture = UtilsGL.setTexture(isMatsPanelActive() ? tileIconMatsON : tileIconMats, iCurrentTexture);
		drawTile(isMatsPanelActive() ? tileIconMatsON : tileIconMats, iconMatsPoint, (mousePanel == MOUSE_ICON_MATS));
		iCurrentTexture = UtilsGL.setTexture(isPrioritiesPanelActive() ? tileIconPrioritiesON : tileIconPriorities,
				iCurrentTexture);
		drawTile(isPrioritiesPanelActive() ? tileIconPrioritiesON : tileIconPriorities, iconPrioritiesPoint,
				(mousePanel == MOUSE_ICON_PRIORITIES));

		// Coins
		String sTownCoins = Game.getWorld().getCoinsString();
		int iTownsCoinsWidth = UtilFont.getWidth(sTownCoins);
		iCurrentTexture = UtilsGL.setTexture(tileIconCoins, iCurrentTexture);
		drawTile(tileIconCoins, tileIconCoinsPoint.x - iTownsCoinsWidth / 2 - tileIconCoins.getTileWidth() / 2,
				tileIconCoinsPoint.y, false);

		// Icons (miniblock + grid + settings + flat mouse + 3d mouse)
		iCurrentTexture = UtilsGL.setTexture(MainPanel.bMiniBlocksON ? tileIconMiniblocksON : tileIconMiniblocks,
				iCurrentTexture);
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniFlat()) {
			UtilsGL.setColorRed();
		}
		drawTile(MainPanel.bMiniBlocksON ? tileIconMiniblocksON : tileIconMiniblocks, iconMiniblocksPoint,
				(mousePanel == MOUSE_ICON_MINIBLOCKS));
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniFlat()) {
			UtilsGL.unsetColor();
		}
		iCurrentTexture = UtilsGL.setTexture(MainPanel.gridON ? tileIconGridON : tileIconGrid, iCurrentTexture);
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniGrid()) {
			UtilsGL.setColorRed();
		}
		drawTile(MainPanel.gridON ? tileIconGridON : tileIconGrid, iconGridPoint, (mousePanel == MOUSE_ICON_GRID));
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniGrid()) {
			UtilsGL.unsetColor();
		}
		iCurrentTexture = UtilsGL.setTexture(tileIconSettings, iCurrentTexture);
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniSettings()) {
			UtilsGL.setColorRed();
		}
		drawTile(tileIconSettings, iconSettingsPoint, (mousePanel == MOUSE_ICON_SETTINGS));
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniSettings()) {
			UtilsGL.unsetColor();
		}
		iCurrentTexture = UtilsGL.setTexture(tileIconFlatMouse, iCurrentTexture);
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniFlatCursor()) {
			UtilsGL.setColorRed();
		}
		drawTile(MainPanel.flatMouseON ? tileIconFlatMouseON : tileIconFlatMouse, iconFlatMousePoint,
				(mousePanel == MOUSE_ICON_FLATMOUSE));
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniFlatCursor()) {
			UtilsGL.unsetColor();
		}
		iCurrentTexture = UtilsGL.setTexture(tileIcon3DMouse, iCurrentTexture);
		if (tutorialFlow != null && tutorialFlow.isBlinkMini3DMouse()) {
			UtilsGL.setColorRed();
		}
		drawTile(MainPanel.tDMouseON ? tileIcon3DMouseON : tileIcon3DMouse, icon3DMousePoint,
				(mousePanel == MOUSE_ICON_3DMOUSE));
		if (tutorialFlow != null && tutorialFlow.isBlinkMini3DMouse()) {
			UtilsGL.unsetColor();
		}

		// Message icons
		if (getMessagesPanelActive() == MessagesPanel.TYPE_ANNOUNCEMENT
				|| (MessagesPanel.getBlink()[MessagesPanel.TYPE_ANNOUNCEMENT] && blinkTurns >= MAX_BLINK_TURNS / 2)) {
			iCurrentTexture = UtilsGL.setTexture(messageTilesON[0], iCurrentTexture);
			drawTile(messageTilesON[0], messageIconPoints[0], (mousePanel == MOUSE_MESSAGES_ICON_ANNOUNCEMENT));
		} else {
			iCurrentTexture = UtilsGL.setTexture(messageTiles[0], iCurrentTexture);
			drawTile(messageTiles[0], messageIconPoints[0], (mousePanel == MOUSE_MESSAGES_ICON_ANNOUNCEMENT));
		}
		if (getMessagesPanelActive() == MessagesPanel.TYPE_COMBAT
				|| (MessagesPanel.getBlink()[MessagesPanel.TYPE_COMBAT] && blinkTurns >= MAX_BLINK_TURNS / 2)) {
			iCurrentTexture = UtilsGL.setTexture(messageTilesON[1], iCurrentTexture);
			drawTile(messageTilesON[1], messageIconPoints[1], (mousePanel == MOUSE_MESSAGES_ICON_COMBAT));
		} else {
			iCurrentTexture = UtilsGL.setTexture(messageTiles[1], iCurrentTexture);
			drawTile(messageTiles[1], messageIconPoints[1], (mousePanel == MOUSE_MESSAGES_ICON_COMBAT));
		}
		if (getMessagesPanelActive() == MessagesPanel.TYPE_HEROES
				|| (MessagesPanel.getBlink()[MessagesPanel.TYPE_HEROES] && blinkTurns >= MAX_BLINK_TURNS / 2)) {
			iCurrentTexture = UtilsGL.setTexture(messageTilesON[2], iCurrentTexture);
			drawTile(messageTilesON[2], messageIconPoints[2], (mousePanel == MOUSE_MESSAGES_ICON_HEROES));
		} else {
			iCurrentTexture = UtilsGL.setTexture(messageTiles[2], iCurrentTexture);
			drawTile(messageTiles[2], messageIconPoints[2], (mousePanel == MOUSE_MESSAGES_ICON_HEROES));
		}
		if (getMessagesPanelActive() == MessagesPanel.TYPE_SYSTEM
				|| (MessagesPanel.getBlink()[MessagesPanel.TYPE_SYSTEM] && blinkTurns >= MAX_BLINK_TURNS / 2)) {
			iCurrentTexture = UtilsGL.setTexture(messageTilesON[3], iCurrentTexture);
			drawTile(messageTilesON[3], messageIconPoints[3], (mousePanel == MOUSE_MESSAGES_ICON_SYSTEM));
		} else {
			iCurrentTexture = UtilsGL.setTexture(messageTiles[3], iCurrentTexture);
			drawTile(messageTiles[3], messageIconPoints[3], (mousePanel == MOUSE_MESSAGES_ICON_SYSTEM));
		}

		// Events icon
		iCurrentTexture = UtilsGL.setTexture(GlobalEventData.getIcon(), iCurrentTexture);
		drawTile(GlobalEventData.getIcon(), iconEventsPoint, false);

		// Gods icon
		// if (TownsProperties.GODS_ACTIVATED) {
		// iCurrentTexture = UtilsGL.setTexture (tileIconGods, iCurrentTexture);
		// drawTile (tileIconGods, iconGodsPoint, false);
		// }

		// (speed down, pause/play, speed up)
		if (World.SPEED > 1) {
			iCurrentTexture = UtilsGL.setTexture(tileIconLowerSpeedON, iCurrentTexture);
			if (tutorialFlow != null && tutorialFlow.isBlinkMiniSpeedDown()) {
				UtilsGL.setColorRed();
			}
			drawTile(tileIconLowerSpeedON, iconLowerSpeedPoint, (mousePanel == MOUSE_ICON_LOWER_SPEED));
		} else {
			iCurrentTexture = UtilsGL.setTexture(tileIconLowerSpeed, iCurrentTexture);
			if (tutorialFlow != null && tutorialFlow.isBlinkMiniSpeedDown()) {
				UtilsGL.setColorRed();
			}
			drawTile(tileIconLowerSpeed, iconLowerSpeedPoint, false);
		}
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniSpeedDown()) {
			UtilsGL.unsetColor();
		}
		if (Game.isPaused()) {
			iCurrentTexture = UtilsGL.setTexture(tileIconResume, iCurrentTexture);
			if (tutorialFlow != null && tutorialFlow.isBlinkMiniPause()) {
				UtilsGL.setColorRed();
			}
			drawTile(tileIconResume, iconPauseResumePoint, (mousePanel == MOUSE_ICON_PAUSE_RESUME));
		} else {
			iCurrentTexture = UtilsGL.setTexture(tileIconPause, iCurrentTexture);
			if (tutorialFlow != null && tutorialFlow.isBlinkMiniPause()) {
				UtilsGL.setColorRed();
			}
			drawTile(tileIconPause, iconPauseResumePoint, (mousePanel == MOUSE_ICON_PAUSE_RESUME));
		}
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniPause()) {
			UtilsGL.unsetColor();
		}
		if (World.SPEED < World.SPEED_MAX) {
			iCurrentTexture = UtilsGL.setTexture(tileIconIncreaseSpeedON, iCurrentTexture);
			if (tutorialFlow != null && tutorialFlow.isBlinkMiniSpeedUp()) {
				UtilsGL.setColorRed();
			}
			drawTile(tileIconIncreaseSpeedON, iconIncreaseSpeedPoint, (mousePanel == MOUSE_ICON_INCREASE_SPEED));
		} else {
			iCurrentTexture = UtilsGL.setTexture(tileIconIncreaseSpeed, iCurrentTexture);
			if (tutorialFlow != null && tutorialFlow.isBlinkMiniSpeedUp()) {
				UtilsGL.setColorRed();
			}
			drawTile(tileIconIncreaseSpeed, iconIncreaseSpeedPoint, false);
		}
		if (tutorialFlow != null && tutorialFlow.isBlinkMiniSpeedUp()) {
			UtilsGL.unsetColor();
		}

		UtilsGL.glEnd();

		// Date
		String sDate = Game.getWorld().getDate().toString();
		int dateW = UtilFont.getWidth(sDate);
		int iLevel = World.MAP_NUM_LEVELS_OUTSIDE - Game.getWorld().getView().z;
		String sLevel = Integer.toString(iLevel);
		int sLevelW = UtilFont.getWidth(sLevel);
		String sNumCitizens = Integer.toString(World.getNumCitizens());
		int numCitizensW = UtilFont.getWidth(sNumCitizens);
		String sNumSoldiers = Integer.toString(World.getNumSoldiers());
		int numSoldiersW = UtilFont.getWidth(sNumSoldiers);
		String sNumHeroes = Integer.toString(World.getNumHeroes());
		int numHeroesW = UtilFont.getWidth(sNumHeroes);

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		UtilsGL.glBegin(GL11.GL_QUADS);
		UtilsGL.drawString(sLevel, iconLevelPoint.x + tileIconLevel.getTileWidth() / 2 - sLevelW / 2,
				iconLevelPoint.y + tileIconLevel.getTileHeight() / 2 - UtilFont.MAX_HEIGHT / 2, ColorGL.BLACK);
		UtilsGL.drawString(sNumCitizens,
				iconNumCitizensPoint.x + tileIconNumCitizens.getTileWidth() / 2 - numCitizensW / 2,
				iconNumCitizensPoint.y + tileIconNumCitizens.getTileHeight(), ColorGL.BLACK);
		UtilsGL.drawString(sNumSoldiers,
				iconNumSoldiersPoint.x + tileIconNumSoldiers.getTileWidth() / 2 - numSoldiersW / 2,
				iconNumSoldiersPoint.y + tileIconNumSoldiers.getTileHeight(), ColorGL.BLACK);
		UtilsGL.drawString(sNumHeroes, iconNumHeroesPoint.x + tileIconNumHeroes.getTileWidth() / 2 - numHeroesW / 2,
				iconNumHeroesPoint.y + tileIconNumHeroes.getTileHeight(), ColorGL.BLACK);
		UtilsGL.drawString(sDate, datePanelPoint.x + tileDatePanel.getTileWidth() / 2 - dateW / 2,
				datePanelPoint.y + tileDatePanel.getTileHeight() / 2 - UtilFont.MAX_HEIGHT / 2, ColorGL.BLACK);
		UtilsGL.drawString(sTownCoins, tileIconCoinsPoint.x - iTownsCoinsWidth / 2 + tileIconCoins.getTileWidth() / 2,
				tileIconCoinsPoint.y + tileIconCoins.getTileHeight() / 2 - UtilFont.MAX_HEIGHT / 2, ColorGL.WHITE);

		if (TownsProperties.DEBUG_MODE) {
			// Global events
			GlobalEventData ged = Game.getWorld().getGlobalEvents();
			UtilsGL.drawStringWithBorder("Shadows " + ged.isShadows(), 2, 3 * UtilFont.MAX_HEIGHT, ColorGL.WHITE, //$NON-NLS-1$
					ColorGL.BLACK);
			UtilsGL.drawStringWithBorder("Half shadows " + ged.isHalfShadows(), 2, 4 * UtilFont.MAX_HEIGHT, //$NON-NLS-1$
					ColorGL.WHITE, ColorGL.BLACK);
			UtilsGL.drawStringWithBorder("RGB " + ged.getRed() + "," + ged.getGreen() + "," + ged.getBlue(), 2, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					5 * UtilFont.MAX_HEIGHT, ColorGL.WHITE, ColorGL.BLACK);
			UtilsGL.drawStringWithBorder("waitPCT " + ged.getWaitPCT(), 2, 6 * UtilFont.MAX_HEIGHT, ColorGL.WHITE, //$NON-NLS-1$
					ColorGL.BLACK);
			UtilsGL.drawStringWithBorder("walkSpeedPCT " + ged.getWalkSpeedPCT(), 2, 7 * UtilFont.MAX_HEIGHT, //$NON-NLS-1$
					ColorGL.WHITE, ColorGL.BLACK);

			// Events
			StringBuffer sbEvents = new StringBuffer("Events: "); //$NON-NLS-1$
			for (int e = 0; e < Game.getWorld().getEvents().size(); e++) {
				sbEvents.append(Game.getWorld().getEvents().get(e).getEventID());
				sbEvents.append(" ("); //$NON-NLS-1$
				sbEvents.append(Game.getWorld().getEvents().get(e).getTurns());
				sbEvents.append(")"); //$NON-NLS-1$
				sbEvents.append(", "); //$NON-NLS-1$
			}

			UtilsGL.drawStringWithBorder(sbEvents.toString(), 2, 2 + 9 * UtilFont.MAX_HEIGHT, ColorGL.WHITE,
					ColorGL.BLACK);

			// Gods
			// sbEvents = new StringBuffer ("Gods: "); //$NON-NLS-1$
			// for (int e = 0; e < Game.getWorld ().getGods ().size (); e++) {
			// sbEvents.append (Game.getWorld ().getGods ().get (e).getGodID ());
			// sbEvents.append (" ("); //$NON-NLS-1$
			// sbEvents.append (Game.getWorld ().getGods ().get (e).getStatus ());
			// sbEvents.append (")"); //$NON-NLS-1$
			// sbEvents.append (", "); //$NON-NLS-1$
			// }
			//
			// UtilsGL.drawStringWithBorder (sbEvents.toString (), 2, 2 + 10 *
			// UtilFont.MAX_HEIGHT, ColorGL.WHITE, ColorGL.BLACK);
		}

		UtilsGL.glEnd();

		// Task
		renderTask();

		// Tutorial button
		renderTutorialButton(mousePanel, blinkTurns >= MAX_BLINK_TURNS / 2);

		/*
		 * PANELS, PRODUCTION PANEL, PRIORITIES PANEL, TRADE_PANEL (este va encima de
		 * todo siempre)
		 */
		renderProductionPanel(mouseX, mouseY, mousePanel);

		if (isPilePanelActive()) {
			renderPilePanel(mouseX, mouseY, mousePanel);
		}
		if (isLivingsPanelActive()) {
			renderLivingsPanel(mouseX, mouseY, mousePanel);
		}
		if (isProfessionsPanelActive()) {
			renderProfessionsPanel(mouseX, mouseY, mousePanel);
		}
		if (isMatsPanelActive()) {
			renderMatsPanel(mouseX, mouseY, mousePanel);
		}
		if (isPrioritiesPanelActive()) {
			renderPrioritiesPanel(mouseX, mouseY, mousePanel);
		}
		if (isTradePanelActive()) {
			renderTradePanel(mouseX, mouseY, mousePanel);
		}
		if (isMessagesPanelActive()) {
			renderMessagesPanel(mouseX, mouseY, mousePanel);
		}

		// Al final de todo el Typing panel si hace falta
		if (typingPanel != null) {
			TypingPanel.render(mousePanel);
		}

		// Al final de todo el Images panel si hace falta
		if (imagesPanel != null && ImagesPanel.isVisible()) {
			ImagesPanel.render(mousePanel);
		}

		// Tooltip
		TooltipRenderer.renderTooltips(mouseX, mouseY, mousePanel);
	}

	public void renderMenuPanel(int mouseX, int mouseY, int mousePanel) {
		checkBlinkRight = (blinkTurns >= MAX_BLINK_TURNS / 2) && TutorialFlow.isBlinkRight();

		if (isMenuPanelActive()) {
			
			// XAVI GL11.glColor4f (1, 1, 1, 1);
			int iCurrentTexture = tileMenuPanel[0].getTextureID();
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, iCurrentTexture);
			GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
			UtilsGL.glBegin(GL11.GL_QUADS);
			renderBackground(tileMenuPanel, menuPanelPoint, MENU_PANEL_WIDTH, MENU_PANEL_HEIGHT);

			int iItemMenu;
			if (mousePanel == MOUSE_MENU_PANEL_ITEMS) {
				iItemMenu = isMouseOnMenuItems(mouseX, mouseY);
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
						if (iMenu >= menuPanelMenu.getItems().size()) {
							break bucle1;
						}
						point = menuPanelItemsPosition.get(iMenu);

						// Round button
						if (menuPanelMenu.getItems().get(iMenu).getType() == SmartMenu.TYPE_MENU) {
							iCurrentTexture = UtilsGL.setTexture(tileBottomItemSM, iCurrentTexture);
							if (checkBlinkRight
									&& TutorialFlow.currentBlinkRight(menuPanelMenu.getItems().get(iMenu).getID())) {
								UtilsGL.setColorRed();
								drawTile(tileBottomItemSM, point, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT,
										(iItemMenu == iMenu));
								UtilsGL.unsetColor();
							} else {
								drawTile(tileBottomItemSM, point, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT,
										(iItemMenu == iMenu));
							}
						} else {
							iCurrentTexture = UtilsGL.setTexture(tileBottomItem, iCurrentTexture);

							if (checkBlinkRight
									&& TutorialFlow.currentBlinkRight(menuPanelMenu.getItems().get(iMenu).getID())) {
								UtilsGL.setColorRed();
								drawTile(tileBottomItem, point, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT,
										(iItemMenu == iMenu));
								UtilsGL.unsetColor();
							} else {
								drawTile(tileBottomItem, point, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT,
										(iItemMenu == iMenu));
							}
						}

						// Icono
						Tile tile = menuPanelMenu.getItems().get(iMenu).getIcon();
						if (tile != null
								&& menuPanelMenu.getItems().get(iMenu).getIconType() == SmartMenu.ICON_TYPE_UI) { // MENU
							iCurrentTexture = UtilsGL.setTexture(tile, iCurrentTexture);
							drawTile(tile, point, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT, (iItemMenu == iMenu));
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
						if (iMenu >= menuPanelMenu.getItems().size()) {
							break bucle1;
						}
						point = menuPanelItemsPosition.get(iMenu);
						// Icono
						tile = menuPanelMenu.getItems().get(iMenu).getIcon();
						if (tile != null
								&& menuPanelMenu.getItems().get(iMenu).getIconType() == SmartMenu.ICON_TYPE_ITEM) { // ICONO
							iCurrentTexture = UtilsGL.setTexture(tile, iCurrentTexture);
							drawTile(tile, point, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT, (iItemMenu == iMenu));
						}
					}
				}
			}

			UtilsGL.glEnd();
		}

		// Botoncito open/close
		if (isMenuPanelLocked()) {
			// Close menu icon
			// XAVI GL11.glColor4f (1, 1, 1, 1);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, tileOpenRightMenuON.getTextureID());
			GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
			UtilsGL.glBegin(GL11.GL_QUADS);
			drawTile(tileOpenRightMenuON, tileOpenCloseRightMenuPoint, tileOpenRightMenuON.getTileWidth(),
					tileOpenRightMenuON.getTileHeight(), mousePanel == MOUSE_MENU_OPENCLOSE);
			UtilsGL.glEnd();
		} else {
			// XAVI GL11.glColor4f (1, 1, 1, 1);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, tileOpenRightMenu.getTextureID());
			GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
			UtilsGL.glBegin(GL11.GL_QUADS);
			if (checkBlinkRight) {
				UtilsGL.setColorRed();
			}
			drawTile(tileOpenRightMenu, tileOpenCloseRightMenuPoint, tileOpenRightMenu.getTileWidth(),
					tileOpenRightMenu.getTileHeight(), mousePanel == MOUSE_MENU_OPENCLOSE);
			if (checkBlinkRight) {
				UtilsGL.unsetColor();
			}
			UtilsGL.glEnd();
		}
	}

	public void renderProductionPanel(int mouseX, int mouseY, int mousePanel) {
		checkBlinkProduction = (blinkTurns >= MAX_BLINK_TURNS / 2) && TutorialFlow.isBlinkProduction();

		if (isProductionPanelActive()) {
			int iCurrentTexture = tileProductionPanel[0].getTextureID();
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, iCurrentTexture);
			GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
			UtilsGL.glBegin(GL11.GL_QUADS);

			renderBackground(tileProductionPanel, productionPanelPoint, PRODUCTION_PANEL_WIDTH,
					PRODUCTION_PANEL_HEIGHT);

			// Items
			int iMenu;
			Point point;
			SmartMenu smItem;
			Point pItem;
			if (mousePanel == MOUSE_PRODUCTION_PANEL_ITEMS || mousePanel == MOUSE_PRODUCTION_PANEL_ITEMS_MINUS_AUTOMATED
					|| mousePanel == MOUSE_PRODUCTION_PANEL_ITEMS_MINUS_REGULAR
					|| mousePanel == MOUSE_PRODUCTION_PANEL_ITEMS_PLUS_AUTOMATED
					|| mousePanel == MOUSE_PRODUCTION_PANEL_ITEMS_PLUS_REGULAR) {
				pItem = isMouseOnProductionItems(mouseX, mouseY);
			} else {
				pItem = null;
			}

			if (productionPanelMenu != null) {
				Tile tile;
				bucle1: for (int y = 0; y < PRODUCTION_PANEL_NUM_ITEMS_Y; y++) {
					for (int x = 0; x < PRODUCTION_PANEL_NUM_ITEMS_X; x++) {
						iMenu = (y * PRODUCTION_PANEL_NUM_ITEMS_X) + x;
						if (iMenu >= productionPanelMenu.getItems().size()) {
							break bucle1;
						}
						smItem = productionPanelMenu.getItems().get(iMenu);

						point = productionPanelItemsPosition.get(iMenu);
						boolean bBlinkItem = checkBlinkProduction && TutorialFlow
								.currentBlinkProduction(productionPanelMenu.getItems().get(iMenu).getID());
						TutorialFlow tutFlow = null;
						if (bBlinkItem && Game.getCurrentMissionData() != null && ImagesPanel.getCurrentFlowIndex() >= 0
								&& ImagesPanel.getCurrentFlowIndex() < Game.getCurrentMissionData().getTutorialFlows()
										.size()) {
							tutFlow = Game.getCurrentMissionData().getTutorialFlows()
									.get(ImagesPanel.getCurrentFlowIndex());
						}

						// Round button
						if (productionPanelMenu.getItems().get(iMenu).getType() == SmartMenu.TYPE_MENU) {
							iCurrentTexture = UtilsGL.setTexture(tileBottomItemSM, iCurrentTexture);
							if (bBlinkItem) {
								UtilsGL.setColorRed();
								drawTile(tileBottomItemSM, point, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT,
										(pItem != null && pItem.x == MOUSE_PRODUCTION_PANEL_ITEMS && pItem.y == iMenu));
								UtilsGL.unsetColor();
							} else {
								drawTile(tileBottomItemSM, point, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT,
										(pItem != null && pItem.x == MOUSE_PRODUCTION_PANEL_ITEMS && pItem.y == iMenu));
							}
						} else {
							iCurrentTexture = UtilsGL.setTexture(tileBottomItem, iCurrentTexture);
							if (bBlinkItem) {
								UtilsGL.setColorRed();
								drawTile(tileBottomItem, point, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT,
										(pItem != null && pItem.x == MOUSE_PRODUCTION_PANEL_ITEMS && pItem.y == iMenu));
								UtilsGL.unsetColor();
							} else {
								drawTile(tileBottomItem, point, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT,
										(pItem != null && pItem.x == MOUSE_PRODUCTION_PANEL_ITEMS && pItem.y == iMenu));
							}
						}

						// Icono
						tile = productionPanelMenu.getItems().get(iMenu).getIcon();
						if (tile != null
								&& productionPanelMenu.getItems().get(iMenu).getIconType() == SmartMenu.ICON_TYPE_UI) {
							iCurrentTexture = UtilsGL.setTexture(tile, iCurrentTexture);
							drawTile(tile, point, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT,
									(pItem != null && pItem.x == MOUSE_PRODUCTION_PANEL_ITEMS && pItem.y == iMenu));
						}

						point = productionPanelItemsPlusRegularPosition.get(iMenu);
						if (point.x != -1) {
							// Regular
							iCurrentTexture = UtilsGL.setTexture(tileProductionPanelPlusIcon, iCurrentTexture);
							if (tutFlow != null && tutFlow.isBlinkProductionRegularPlus()) {
								UtilsGL.setColorRed();
							}
							drawTile(tileProductionPanelPlusIcon, point, ICON_WIDTH, ICON_HEIGHT, (pItem != null
									&& pItem.x == MOUSE_PRODUCTION_PANEL_ITEMS_PLUS_REGULAR && pItem.y == iMenu));
							if (tutFlow != null && tutFlow.isBlinkProductionRegularPlus()) {
								UtilsGL.unsetColor();
							}

							// Automated
							if (tutFlow != null && tutFlow.isBlinkProductionAutomatedPlus()) {
								UtilsGL.setColorRed();
							}
							drawTile(tileProductionPanelPlusIcon, productionPanelItemsPlusAutomatedPosition.get(iMenu),
									ICON_WIDTH, ICON_HEIGHT,
									(pItem != null && pItem.x == MOUSE_PRODUCTION_PANEL_ITEMS_PLUS_AUTOMATED
											&& pItem.y == iMenu));
							if (tutFlow != null && tutFlow.isBlinkProductionAutomatedPlus()) {
								UtilsGL.unsetColor();
							}

							iCurrentTexture = UtilsGL.setTexture(tileProductionPanelMinusIcon, iCurrentTexture);

							// Regular
							if (tutFlow != null && tutFlow.isBlinkProductionRegularMinus()) {
								UtilsGL.setColorRed();
							}
							drawTile(tileProductionPanelMinusIcon, productionPanelItemsMinusRegularPosition.get(iMenu),
									ICON_WIDTH, ICON_HEIGHT,
									(pItem != null && pItem.x == MOUSE_PRODUCTION_PANEL_ITEMS_MINUS_REGULAR
											&& pItem.y == iMenu));
							if (tutFlow != null && tutFlow.isBlinkProductionRegularMinus()) {
								UtilsGL.unsetColor();
							}

							// Automated
							if (tutFlow != null && tutFlow.isBlinkProductionAutomatedMinus()) {
								UtilsGL.setColorRed();
							}
							drawTile(tileProductionPanelMinusIcon,
									productionPanelItemsMinusAutomatedPosition.get(iMenu), ICON_WIDTH, ICON_HEIGHT,
									(pItem != null && pItem.x == MOUSE_PRODUCTION_PANEL_ITEMS_MINUS_AUTOMATED
											&& pItem.y == iMenu));
							if (tutFlow != null && tutFlow.isBlinkProductionAutomatedMinus()) {
								UtilsGL.unsetColor();
							}
						}
					}
				}
			}
			UtilsGL.glEnd();

			/*
			 * ITEMS TEXTURES
			 */
			if (productionPanelMenu != null) {
				iCurrentTexture = Game.TEXTURE_FONT_ID;
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
				GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
				UtilsGL.glBegin(GL11.GL_QUADS);

				bucle1: for (int y = 0; y < PRODUCTION_PANEL_NUM_ITEMS_Y; y++) {
					for (int x = 0; x < PRODUCTION_PANEL_NUM_ITEMS_X; x++) {
						iMenu = (y * PRODUCTION_PANEL_NUM_ITEMS_X) + x;
						if (iMenu >= productionPanelMenu.getItems().size()) {
							break bucle1;
						}
						point = productionPanelItemsPosition.get(iMenu);
						// Icono
						Tile tile = productionPanelMenu.getItems().get(iMenu).getIcon();
						if (tile != null && productionPanelMenu.getItems().get(iMenu)
								.getIconType() == SmartMenu.ICON_TYPE_ITEM) {
							iCurrentTexture = UtilsGL.setTexture(tile, iCurrentTexture);
							drawTile(tile, point, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT,
									(pItem != null && pItem.x == MOUSE_PRODUCTION_PANEL_ITEMS && pItem.y == iMenu));
						}
					}
				}
				UtilsGL.glEnd();
			}

			/*
			 * NUMBERS
			 */
			if (productionPanelMenu != null) {
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
				GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
				UtilsGL.glBegin(GL11.GL_QUADS);

				String strValue;
				HashMap<String, Integer> hmItemsOnQueue = Game.getWorld().getTaskManager().getItemsOnRegularQueue();
				Integer iItemQueue;
				bucle1: for (int y = 0; y < PRODUCTION_PANEL_NUM_ITEMS_Y; y++) {
					for (int x = 0; x < PRODUCTION_PANEL_NUM_ITEMS_X; x++) {
						iMenu = (y * PRODUCTION_PANEL_NUM_ITEMS_X) + x;
						if (iMenu >= productionPanelMenu.getItems().size()) {
							break bucle1;
						}
						smItem = productionPanelMenu.getItems().get(iMenu);
						if (smItem.getType() == SmartMenu.TYPE_ITEM) {
							if (!smItem.getCommand().equalsIgnoreCase(CommandPanel.COMMAND_BACK)) {
								point = productionPanelItemsPosition.get(iMenu);
								iItemQueue = hmItemsOnQueue.get(smItem.getParameter());
								if (iItemQueue == null) {
									strValue = "0"; //$NON-NLS-1$
								} else {
									strValue = Integer.toString(iItemQueue);
								}
								// Regular
								UtilsGL.drawStringWithBorder(strValue,
										point.x - ICON_WIDTH / 2 - (UtilFont.getWidth(strValue)) / 2,
										point.y + PRODUCTION_PANEL_ITEM_HEIGHT / 2 - UtilFont.MAX_HEIGHT / 2,
										ColorGL.WHITE, ColorGL.BLACK);

								// Automated
								strValue = Integer.toString(Game.getWorld().getTaskManager()
										.getNumItemsOnAutomatedQueue(smItem.getParameter()));
								UtilsGL.drawStringWithBorder(strValue,
										point.x + PRODUCTION_PANEL_ITEM_WIDTH + ICON_WIDTH / 2
												- (UtilFont.getWidth(strValue)) / 2,
										point.y + PRODUCTION_PANEL_ITEM_HEIGHT / 2 - UtilFont.MAX_HEIGHT / 2,
										ColorGL.WHITE, ColorGL.BLACK);

								// Items in world
								ActionManagerItem ami = ActionManager.getItem(smItem.getParameter());
								if (ami != null && ami.getGeneratedItem() != null) {
									int iNum = Item.getNumItems(UtilsIniHeaders.getIntIniHeader(ami.getGeneratedItem()),
											false, World.MAP_DEPTH);
									if (iNum > 0) {
										strValue = Integer.toString(iNum);
										UtilsGL.drawStringWithBorder(strValue,
												point.x + PRODUCTION_PANEL_ITEM_WIDTH / 2
														- (UtilFont.getWidth(strValue)) / 2,
												point.y + PRODUCTION_PANEL_ITEM_HEIGHT / 4 - UtilFont.MAX_HEIGHT / 2,
												ColorGL.WHITE, ColorGL.BLACK);
									}
								}
							}
						}
					}
				}

				UtilsGL.glEnd();
			}
		}

		if (isProductionPanelLocked()) {
			// Close icon
			// XAVI GL11.glColor4f (1, 1, 1, 1);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, tileOpenProductionPanelON.getTextureID());
			GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
			UtilsGL.glBegin(GL11.GL_QUADS);
			drawTile(tileOpenProductionPanelON, tileOpenCloseProductionPanelPoint,
					tileOpenProductionPanelON.getTileWidth(), tileOpenProductionPanelON.getTileHeight(),
					mousePanel == MOUSE_PRODUCTION_OPENCLOSE);
			UtilsGL.glEnd();
		} else {
			// Open icon
			// XAVI GL11.glColor4f (1, 1, 1, 1);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, tileOpenProductionPanel.getTextureID());
			GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
			UtilsGL.glBegin(GL11.GL_QUADS);
			if (checkBlinkProduction) {
				UtilsGL.setColorRed();
			}
			drawTile(tileOpenProductionPanel, tileOpenCloseProductionPanelPoint, tileOpenProductionPanel.getTileWidth(),
					tileOpenProductionPanel.getTileHeight(), mousePanel == MOUSE_PRODUCTION_OPENCLOSE);
			if (checkBlinkProduction) {
				UtilsGL.unsetColor();
			}
			UtilsGL.glEnd();
		}
	}

	/**
	 * Renderiza el background con los 8 tiles de los lados y esquinas 0: background
	 * 1: N 2: S 3: E 4: W 5: NE 6: NW 7: SE 8: SW
	 * 
	 * @param tiles
	 */
	public static void renderBackground(Tile[] tiles, Point point, int width, int height) {
		int iEdgeWidth = tiles[6].getTileWidth();
		int iEdgeHeight = tiles[6].getTileHeight();

		// Background
		Tile tile = tiles[0];
		UtilsGL.drawTexture(point.x + iEdgeWidth, point.y + iEdgeHeight, point.x + width - iEdgeWidth,
				point.y + height - iEdgeHeight, tile.getTileSetTexX0(), tile.getTileSetTexY0(), tile.getTileSetTexX1(),
				tile.getTileSetTexY1());

		// N
		tile = tiles[1];
		UtilsGL.drawTexture(point.x + iEdgeWidth, point.y, point.x + width - iEdgeWidth, point.y + iEdgeHeight,
				tile.getTileSetTexX0(), tile.getTileSetTexY0(), tile.getTileSetTexX1(), tile.getTileSetTexY1());

		// S
		tile = tiles[2];
		UtilsGL.drawTexture(point.x + iEdgeWidth, point.y + height - iEdgeHeight, point.x + width - iEdgeWidth,
				point.y + height, tile.getTileSetTexX0(), tile.getTileSetTexY0(), tile.getTileSetTexX1(),
				tile.getTileSetTexY1());

		// E
		tile = tiles[3];
		UtilsGL.drawTexture(point.x + width - iEdgeWidth, point.y + iEdgeHeight, point.x + width,
				point.y + height - iEdgeHeight, tile.getTileSetTexX0(), tile.getTileSetTexY0(), tile.getTileSetTexX1(),
				tile.getTileSetTexY1());

		// W
		tile = tiles[4];
		UtilsGL.drawTexture(point.x, point.y + iEdgeHeight, point.x + iEdgeWidth, point.y + height - iEdgeHeight,
				tile.getTileSetTexX0(), tile.getTileSetTexY0(), tile.getTileSetTexX1(), tile.getTileSetTexY1());

		// NE
		tile = tiles[5];
		UtilsGL.drawTexture(point.x + width - iEdgeWidth, point.y, point.x + width, point.y + iEdgeHeight,
				tile.getTileSetTexX0(), tile.getTileSetTexY0(), tile.getTileSetTexX1(), tile.getTileSetTexY1());

		// NW
		tile = tiles[6];
		UtilsGL.drawTexture(point.x, point.y, point.x + iEdgeWidth, point.y + iEdgeHeight, tile.getTileSetTexX0(),
				tile.getTileSetTexY0(), tile.getTileSetTexX1(), tile.getTileSetTexY1());

		// SE
		tile = tiles[7];
		UtilsGL.drawTexture(point.x + width - iEdgeWidth, point.y + height - iEdgeHeight, point.x + width,
				point.y + height, tile.getTileSetTexX0(), tile.getTileSetTexY0(), tile.getTileSetTexX1(),
				tile.getTileSetTexY1());

		// SW
		tile = tiles[8];
		UtilsGL.drawTexture(point.x, point.y + height - iEdgeHeight, point.x + iEdgeWidth, point.y + height,
				tile.getTileSetTexX0(), tile.getTileSetTexY0(), tile.getTileSetTexX1(), tile.getTileSetTexY1());
	}

	public void renderTradePanel(int mouseX, int mouseY, int mousePanel) {
		Point pItem = isMouseOnTradeButtons(mouseX, mouseY);

		// Fondo
		// XAVI GL11.glColor4f (1, 1, 1, 1);
		int iCurrentTexture = tileTradePanel[0].getTextureID();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, iCurrentTexture);
		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		UtilsGL.glBegin(GL11.GL_QUADS);
		renderBackground(tileTradePanel, tradePanelPoint, TRADE_PANEL_WIDTH, TRADE_PANEL_HEIGHT);

		// Close button
		iCurrentTexture = UtilsGL.setTexture(tileButtonClose, iCurrentTexture);
		if (pItem != null && pItem.x == MOUSE_TRADE_PANEL_BUTTONS_CLOSE) {
			drawTile(tileButtonClose, tradePanelClosePoint);
		} else {
			drawTile(tileButtonCloseDisabled, tradePanelClosePoint);
		}

		UtilsGL.glEnd();

		// Miramos si hay caravana activa
		CaravanData caravanData = Game.getWorld().getCurrentCaravanData();
		String sText = null;
		boolean bTrading = false;
		if (caravanData == null || caravanData.getStatus() == CaravanData.STATUS_NONE) {
			sText = Messages.getString("UIPanel.17"); //$NON-NLS-1$
		} else if (caravanData.getStatus() == CaravanData.STATUS_COMING) {
			sText = Messages.getString("UIPanel.18"); //$NON-NLS-1$
		} else if (caravanData.getStatus() == CaravanData.STATUS_IN_PLACE) {
			sText = null;
		} else if (caravanData.getStatus() == CaravanData.STATUS_TRADING) {
			sText = Messages.getString("UIPanel.20"); //$NON-NLS-1$
			bTrading = true;
		} else if (caravanData.getStatus() == CaravanData.STATUS_LEAVING) {
			sText = Messages.getString("UIPanel.21"); //$NON-NLS-1$
		} else {
			// Nunca debería llegar aquí
			Log.log(Log.LEVEL_ERROR, "Caravan status [" + caravanData.getStatus() + "]", "UIPanel"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			sText = null;
		}

		if (sText != null && !bTrading) {
			int iTextWidth = UtilFont.getWidth(sText);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
			GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
			UtilsGL.glBegin(GL11.GL_QUADS);
			UtilsGL.drawStringWithBorder(sText, tradePanelPoint.x + TRADE_PANEL_WIDTH / 2 - iTextWidth / 2,
					tradePanelPoint.y + TRADE_PANEL_HEIGHT / 2 - UtilFont.MAX_HEIGHT / 2, ColorGL.ORANGE,
					ColorGL.BLACK);
			UtilsGL.glEnd();
			return;
		}

		// Si llega aquí es que la caravana está lista para tradear (o está tradeando)
		// if (!bTrading && tradePanel == null) {
		if (tradePanel == null) {
			// Acaba de entrar por primera vez, generamos el panel
			tradePanel = new TradePanel(caravanData, tradePanelPoint, TRADE_PANEL_WIDTH, TRADE_PANEL_HEIGHT);
		}

		// Renderizamos
		UtilsGL.glBegin(GL11.GL_QUADS);

		// Confirm button
		if (!bTrading) {
			iCurrentTexture = UtilsGL.setTexture(TradePanel.tileTradeConfirm, iCurrentTexture);
			if (tradePanel.isTransactionReady()) {
				drawTile(TradePanel.tileTradeConfirm, tradePanel.getConfirmPoint(),
						TradePanel.tileTradeConfirm.getTileWidth(), TradePanel.tileTradeConfirm.getTileHeight(),
						(pItem != null && pItem.x == MOUSE_TRADE_PANEL_BUTTONS_CONFIRM));
			} else {
				drawTile(TradePanel.tileTradeConfirmDisabled, tradePanel.getConfirmPoint());
			}
		}

		Point point;
		if (!bTrading) {
			// Caravan buttons scroll up
			iCurrentTexture = UtilsGL.setTexture(tileScrollUp, iCurrentTexture);
			point = tradePanel.getScrollUpCaravanPoint();
			if (tradePanel.getIndexButtonsCaravan() > 0) {
				drawTile(tileScrollUp, point, tileScrollUp.getTileWidth(), tileScrollUp.getTileHeight(),
						(pItem != null && pItem.x == MOUSE_TRADE_PANEL_BUTTONS_UP_CARAVAN));
			} else {
				drawTile(tileScrollUpDisabled, point);
			}
		}
		// Caravan buttons scroll up to-buy
		point = tradePanel.getScrollUpCaravanToBuyPoint();
		if (tradePanel.getIndexButtonsToBuyCaravan() > 0) {
			drawTile(tileScrollUp, point, tileScrollUp.getTileWidth(), tileScrollUp.getTileHeight(),
					(pItem != null && pItem.x == MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_UP_CARAVAN));
		} else {
			drawTile(tileScrollUpDisabled, point);
		}
		// Town buttons scroll up to-sell
		point = tradePanel.getScrollUpTownToSellPoint();
		if (tradePanel.getIndexButtonsToSellTown() > 0) {
			drawTile(tileScrollUp, point, tileScrollUp.getTileWidth(), tileScrollUp.getTileHeight(),
					(pItem != null && pItem.x == MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_UP_TOWN));
		} else {
			drawTile(tileScrollUpDisabled, point);
		}

		if (!bTrading) {
			// Town buttons scroll up
			point = tradePanel.getScrollUpTownPoint();
			if (tradePanel.getIndexButtonsTown() > 0) {
				drawTile(tileScrollUp, point, tileScrollUp.getTileWidth(), tileScrollUp.getTileHeight(),
						(pItem != null && pItem.x == MOUSE_TRADE_PANEL_BUTTONS_UP_TOWN));
			} else {
				drawTile(tileScrollUpDisabled, point);
			}
		}

		// Caravan buttons
		if (!bTrading) {
			iCurrentTexture = UtilsGL.setTexture(TradePanel.tileTradeButton, iCurrentTexture);
			for (int i = 0; i < tradePanel.getAlButtonPointsCaravan().size(); i++) {
				point = tradePanel.getAlButtonPointsCaravan().get(i);
				drawTile(TradePanel.tileTradeButton, point, TradePanel.tileTradeButton.getTileWidth(),
						TradePanel.tileTradeButton.getTileHeight(),
						(pItem != null && pItem.x == MOUSE_TRADE_PANEL_BUTTONS_CARAVAN && pItem.y == i));
			}
		}
		// Caravan buttons to-buy
		for (int i = 0; i < tradePanel.getAlButtonPointsCaravanToBuy().size(); i++) {
			point = tradePanel.getAlButtonPointsCaravanToBuy().get(i);
			drawTile(TradePanel.tileTradeButton, point, TradePanel.tileTradeButton.getTileWidth(),
					TradePanel.tileTradeButton.getTileHeight(),
					(pItem != null && pItem.x == MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_CARAVAN && pItem.y == i));
		}
		// Town buttons to-sell
		for (int i = 0; i < tradePanel.getAlButtonPointsTownToSell().size(); i++) {
			point = tradePanel.getAlButtonPointsTownToSell().get(i);
			drawTile(TradePanel.tileTradeButton, point, TradePanel.tileTradeButton.getTileWidth(),
					TradePanel.tileTradeButton.getTileHeight(),
					(pItem != null && pItem.x == MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_TOWN && pItem.y == i));
		}
		if (!bTrading) {
			// Town buttons
			for (int i = 0; i < tradePanel.getAlButtonPointsTown().size(); i++) {
				point = tradePanel.getAlButtonPointsTown().get(i);
				drawTile(TradePanel.tileTradeButton, point, TradePanel.tileTradeButton.getTileWidth(),
						TradePanel.tileTradeButton.getTileHeight(),
						(pItem != null && pItem.x == MOUSE_TRADE_PANEL_BUTTONS_TOWN && pItem.y == i));
			}
		}

		if (!bTrading) {
			// Caravan buttons scroll down
			iCurrentTexture = UtilsGL.setTexture(tileScrollDown, iCurrentTexture);
			point = tradePanel.getScrollDownCaravanPoint();
			if (tradePanel.getAlButtonPointsCaravan().size() + tradePanel.getIndexButtonsCaravan() < tradePanel
					.getMenuCaravan().getItems().size()) {
				drawTile(tileScrollDown, point, tileScrollDown.getTileWidth(), tileScrollDown.getTileHeight(),
						(pItem != null && pItem.x == MOUSE_TRADE_PANEL_BUTTONS_DOWN_CARAVAN));
			} else {
				drawTile(tileScrollDownDisabled, point);
			}
		}
		// Caravan buttons scroll down to-buy
		point = tradePanel.getScrollDownCaravanToBuyPoint();
		if (tradePanel.getAlButtonPointsCaravanToBuy().size() + tradePanel.getIndexButtonsToBuyCaravan() < caravanData
				.getMenuCaravanToBuy().getItems().size()) {
			drawTile(tileScrollDown, point, tileScrollDown.getTileWidth(), tileScrollDown.getTileHeight(),
					(pItem != null && pItem.x == MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_DOWN_CARAVAN));
		} else {
			drawTile(tileScrollDownDisabled, point);
		}
		// Town buttons scroll down to-sell
		point = tradePanel.getScrollDownTownToSellPoint();
		if (tradePanel.getAlButtonPointsTownToSell().size() + tradePanel.getIndexButtonsToSellTown() < caravanData
				.getMenuTownToSell().getItems().size()) {
			drawTile(tileScrollDown, point, tileScrollDown.getTileWidth(), tileScrollDown.getTileHeight(),
					(pItem != null && pItem.x == MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_DOWN_TOWN));
		} else {
			drawTile(tileScrollDownDisabled, point);
		}
		if (!bTrading) {
			// Town buttons scroll down
			point = tradePanel.getScrollDownTownPoint();
			if (tradePanel.getAlButtonPointsTown().size() + tradePanel.getIndexButtonsTown() < tradePanel.getMenuTown()
					.getItems().size()) {
				drawTile(tileScrollDown, point, tileScrollDown.getTileWidth(), tileScrollDown.getTileHeight(),
						(pItem != null && pItem.x == MOUSE_TRADE_PANEL_BUTTONS_DOWN_TOWN));
			} else {
				drawTile(tileScrollDownDisabled, point);
			}
		}

		// Icons
		iCurrentTexture = UtilsGL.setTexture(TradePanel.tileTradeCaravanCoins, iCurrentTexture);
		drawTile(TradePanel.tileTradeCaravanCoins, tradePanel.getCaravanCoinsIconPoint());
		iCurrentTexture = UtilsGL.setTexture(TradePanel.tileTradeTownCoins, iCurrentTexture);
		drawTile(TradePanel.tileTradeTownCoins, tradePanel.getTownCoinsIconPoint());
		iCurrentTexture = UtilsGL.setTexture(TradePanel.tileTradeBuy, iCurrentTexture);
		drawTile(TradePanel.tileTradeBuy, tradePanel.getBuyIconPoint());
		iCurrentTexture = UtilsGL.setTexture(TradePanel.tileTradeSell, iCurrentTexture);
		drawTile(TradePanel.tileTradeSell, tradePanel.getSellIconPoint());
		iCurrentTexture = UtilsGL.setTexture(TradePanel.tileTradeCost, iCurrentTexture);
		drawTile(TradePanel.tileTradeCost, tradePanel.getCostPoint());

		SmartMenu menu;
		int iIndex;

		if (!bTrading) {
			// Caravan Items
			menu = tradePanel.getMenuCaravan();
			for (int i = 0; i < tradePanel.getAlButtonPointsCaravan().size(); i++) {
				iIndex = i + tradePanel.getIndexButtonsCaravan();
				if (menu.getItems().size() <= iIndex) {
					break;
				}
				if (menu.getItems().get(iIndex).getIcon() != null
						&& menu.getItems().get(iIndex).getIconType() == SmartMenu.ICON_TYPE_ITEM) {
					point = tradePanel.getAlButtonPointsCaravan().get(i);
					iCurrentTexture = UtilsGL.setTexture(menu.getItems().get(iIndex).getIcon(), iCurrentTexture);
					drawTile(menu.getItems().get(iIndex).getIcon(), point, TRADE_PANEL_BUTTON_WIDTH,
							TRADE_PANEL_BUTTON_HEIGHT,
							(pItem != null && pItem.x == MOUSE_TRADE_PANEL_BUTTONS_CARAVAN && pItem.y == i));
				}
			}
		}
		// Caravan Items to-buy
		menu = caravanData.getMenuCaravanToBuy();
		for (int i = 0; i < tradePanel.getAlButtonPointsCaravanToBuy().size(); i++) {
			iIndex = i + tradePanel.getIndexButtonsToBuyCaravan();
			if (menu.getItems().size() <= iIndex) {
				break;
			}
			if (menu.getItems().get(iIndex).getIcon() != null
					&& menu.getItems().get(iIndex).getIconType() == SmartMenu.ICON_TYPE_ITEM) {
				point = tradePanel.getAlButtonPointsCaravanToBuy().get(i);
				iCurrentTexture = UtilsGL.setTexture(menu.getItems().get(iIndex).getIcon(), iCurrentTexture);
				drawTile(menu.getItems().get(iIndex).getIcon(), point, TRADE_PANEL_BUTTON_WIDTH,
						TRADE_PANEL_BUTTON_HEIGHT,
						(pItem != null && pItem.x == MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_CARAVAN && pItem.y == i));
			}
		}
		// Town Items to-sell
		menu = caravanData.getMenuTownToSell();
		for (int i = 0; i < tradePanel.getAlButtonPointsTownToSell().size(); i++) {
			iIndex = i + tradePanel.getIndexButtonsToSellTown();
			if (menu.getItems().size() <= iIndex) {
				break;
			}
			if (menu.getItems().get(iIndex).getIcon() != null
					&& menu.getItems().get(iIndex).getIconType() == SmartMenu.ICON_TYPE_ITEM) {
				point = tradePanel.getAlButtonPointsTownToSell().get(i);
				iCurrentTexture = UtilsGL.setTexture(menu.getItems().get(iIndex).getIcon(), iCurrentTexture);
				drawTile(menu.getItems().get(iIndex).getIcon(), point, TRADE_PANEL_BUTTON_WIDTH,
						TRADE_PANEL_BUTTON_HEIGHT,
						(pItem != null && pItem.x == MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_TOWN && pItem.y == i));
			}
		}
		if (!bTrading) {
			// Town Items
			menu = tradePanel.getMenuTown();
			for (int i = 0; i < tradePanel.getAlButtonPointsTown().size(); i++) {
				iIndex = i + tradePanel.getIndexButtonsTown();
				if (menu.getItems().size() <= iIndex) {
					break;
				}
				if (menu.getItems().get(iIndex).getIcon() != null
						&& menu.getItems().get(iIndex).getIconType() == SmartMenu.ICON_TYPE_ITEM) {
					point = tradePanel.getAlButtonPointsTown().get(i);
					iCurrentTexture = UtilsGL.setTexture(menu.getItems().get(iIndex).getIcon(), iCurrentTexture);
					drawTile(menu.getItems().get(iIndex).getIcon(), point, TRADE_PANEL_BUTTON_WIDTH,
							TRADE_PANEL_BUTTON_HEIGHT,
							(pItem != null && pItem.x == MOUSE_TRADE_PANEL_BUTTONS_TOWN && pItem.y == i));
				}
			}
		}
		UtilsGL.glEnd();

		// Números
		int iTextWidth;
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		UtilsGL.glBegin(GL11.GL_QUADS);
		if (!bTrading) {
			// Caravan
			menu = tradePanel.getMenuCaravan();
			for (int i = 0; i < tradePanel.getAlButtonPointsCaravan().size(); i++) {
				iIndex = i + tradePanel.getIndexButtonsCaravan();
				if (menu.getItems().size() <= iIndex) {
					break;
				}
				point = tradePanel.getAlButtonPointsCaravan().get(i);
				sText = menu.getItems().get(iIndex).getParameter2();
				iTextWidth = UtilFont.getWidth(sText); // Qtty
				UtilsGL.drawStringWithBorder(sText, point.x + TRADE_PANEL_BUTTON_WIDTH / 2 - iTextWidth / 2,
						point.y + TRADE_PANEL_BUTTON_HEIGHT - UtilFont.MAX_HEIGHT, ColorGL.WHITE, ColorGL.BLACK);
				sText = Integer.toString(menu.getItems().get(iIndex).getDirectCoordinates().x);
				iTextWidth = UtilFont.getWidth(sText); // Price
				UtilsGL.drawStringWithBorder(sText, point.x + TRADE_PANEL_BUTTON_WIDTH / 2 - iTextWidth / 2, point.y,
						ColorGL.WHITE, ColorGL.BLACK);
			}
		}
		// Caravan to-buy
		menu = caravanData.getMenuCaravanToBuy();
		for (int i = 0; i < tradePanel.getAlButtonPointsCaravanToBuy().size(); i++) {
			iIndex = i + tradePanel.getIndexButtonsToBuyCaravan();
			if (menu.getItems().size() <= iIndex) {
				break;
			}
			point = tradePanel.getAlButtonPointsCaravanToBuy().get(i);
			sText = menu.getItems().get(iIndex).getParameter2();
			iTextWidth = UtilFont.getWidth(sText); // Qtty
			UtilsGL.drawStringWithBorder(sText, point.x + TRADE_PANEL_BUTTON_WIDTH / 2 - iTextWidth / 2,
					point.y + TRADE_PANEL_BUTTON_HEIGHT - UtilFont.MAX_HEIGHT, ColorGL.WHITE, ColorGL.BLACK);
			sText = Integer.toString(menu.getItems().get(iIndex).getDirectCoordinates().x);
			iTextWidth = UtilFont.getWidth(sText); // Price
			UtilsGL.drawStringWithBorder(sText, point.x + TRADE_PANEL_BUTTON_WIDTH / 2 - iTextWidth / 2, point.y,
					ColorGL.WHITE, ColorGL.BLACK);
		}
		// Town to-sell
		menu = caravanData.getMenuTownToSell();
		for (int i = 0; i < tradePanel.getAlButtonPointsTownToSell().size(); i++) {
			iIndex = i + tradePanel.getIndexButtonsToSellTown();
			if (menu.getItems().size() <= iIndex) {
				break;
			}
			point = tradePanel.getAlButtonPointsTownToSell().get(i);
			sText = menu.getItems().get(iIndex).getParameter2();
			iTextWidth = UtilFont.getWidth(sText); // Qtty
			UtilsGL.drawStringWithBorder(sText, point.x + TRADE_PANEL_BUTTON_WIDTH / 2 - iTextWidth / 2,
					point.y + TRADE_PANEL_BUTTON_HEIGHT - UtilFont.MAX_HEIGHT, ColorGL.WHITE, ColorGL.BLACK);
			sText = Integer.toString(menu.getItems().get(iIndex).getDirectCoordinates().x);
			iTextWidth = UtilFont.getWidth(sText); // Price
			UtilsGL.drawStringWithBorder(sText, point.x + TRADE_PANEL_BUTTON_WIDTH / 2 - iTextWidth / 2, point.y,
					ColorGL.WHITE, ColorGL.BLACK);
		}
		if (!bTrading) {
			// Town
			menu = tradePanel.getMenuTown();
			for (int i = 0; i < tradePanel.getAlButtonPointsTown().size(); i++) {
				iIndex = i + tradePanel.getIndexButtonsTown();
				if (menu.getItems().size() <= iIndex) {
					break;
				}
				point = tradePanel.getAlButtonPointsTown().get(i);
				sText = menu.getItems().get(iIndex).getParameter2();
				iTextWidth = UtilFont.getWidth(sText); // Qtty
				UtilsGL.drawStringWithBorder(sText, point.x + TRADE_PANEL_BUTTON_WIDTH / 2 - iTextWidth / 2,
						point.y + TRADE_PANEL_BUTTON_HEIGHT - UtilFont.MAX_HEIGHT, ColorGL.WHITE, ColorGL.BLACK);
				sText = Integer.toString(menu.getItems().get(iIndex).getDirectCoordinates().x);
				iTextWidth = UtilFont.getWidth(sText); // Price
				UtilsGL.drawStringWithBorder(sText, point.x + TRADE_PANEL_BUTTON_WIDTH / 2 - iTextWidth / 2, point.y,
						ColorGL.WHITE, ColorGL.BLACK);
			}
		}

		// Caravan coins
		sText = Integer.toString(caravanData.getCoins());
		UtilsGL.drawString(sText,
				tradePanel.getCaravanCoinsIconPoint().x + TradePanel.tileTradeCaravanCoins.getTileWidth() / 2
						- UtilFont.getWidth(sText) / 2,
				tradePanel.getCaravanCoinsIconPoint().y + TradePanel.tileTradeCaravanCoins.getTileHeight(),
				ColorGL.BLACK);
		// Cost
		sText = Integer.toString(tradePanel.getCost());
		UtilsGL.drawString(sText,
				tradePanel.getCostPoint().x + TradePanel.tileTradeCost.getTileWidth() / 2
						- UtilFont.getWidth(sText) / 2,
				tradePanel.getCostPoint().y + TradePanel.tileTradeCost.getTileHeight(),
				tradePanel.getCost() >= 0 ? ColorGL.BLACK : ColorGL.RED);
		// Towns coins
		sText = Integer.toString(Game.getWorld().getCoins());
		UtilsGL.drawString(sText,
				tradePanel.getTownCoinsIconPoint().x + TradePanel.tileTradeTownCoins.getTileWidth() / 2
						- UtilFont.getWidth(sText) / 2,
				tradePanel.getTownCoinsIconPoint().y + TradePanel.tileTradeTownCoins.getTileHeight(), ColorGL.BLACK);

		UtilsGL.glEnd();
	}

	public void renderMessagesPanel(int mouseX, int mouseY, int mousePanel) {
		Point pItem = isMouseOnMessagesButtons(mouseX, mouseY);

		// Fondo
		// XAVI GL11.glColor4f (1, 1, 1, 1);
		int iCurrentTexture = tileMessagesPanel[0].getTextureID();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, iCurrentTexture);
		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		UtilsGL.glBegin(GL11.GL_QUADS);
		renderBackground(tileMessagesPanel, messagesPanelPoint, MESSAGES_PANEL_WIDTH, MESSAGES_PANEL_HEIGHT);

		// Close button
		iCurrentTexture = UtilsGL.setTexture(tileButtonClose, iCurrentTexture);
		if (pItem != null && pItem.x == MOUSE_MESSAGES_PANEL_BUTTONS_CLOSE) {
			drawTile(tileButtonClose, messagesPanelClosePoint);
		} else {
			drawTile(tileButtonCloseDisabled, messagesPanelClosePoint);
		}

		// "Tabs"
		int iMessagesType = getMessagesPanelActive();
		if (MessagesPanel.TYPE_ANNOUNCEMENT == iMessagesType) {
			iCurrentTexture = UtilsGL.setTexture(messagePanelTilesON[MessagesPanel.TYPE_ANNOUNCEMENT], iCurrentTexture);
			drawTile(messagePanelTilesON[MessagesPanel.TYPE_ANNOUNCEMENT],
					messagePanelIconPoints[MessagesPanel.TYPE_ANNOUNCEMENT],
					(pItem != null && pItem.x == MOUSE_MESSAGES_PANEL_BUTTONS_ANNOUNCEMENT));
		} else {
			iCurrentTexture = UtilsGL.setTexture(messagePanelTiles[MessagesPanel.TYPE_ANNOUNCEMENT], iCurrentTexture);
			drawTile(messagePanelTiles[MessagesPanel.TYPE_ANNOUNCEMENT],
					messagePanelIconPoints[MessagesPanel.TYPE_ANNOUNCEMENT],
					(pItem != null && pItem.x == MOUSE_MESSAGES_PANEL_BUTTONS_ANNOUNCEMENT));
		}
		if (MessagesPanel.TYPE_COMBAT == iMessagesType) {
			iCurrentTexture = UtilsGL.setTexture(messagePanelTilesON[MessagesPanel.TYPE_COMBAT], iCurrentTexture);
			drawTile(messagePanelTilesON[MessagesPanel.TYPE_COMBAT], messagePanelIconPoints[MessagesPanel.TYPE_COMBAT],
					(pItem != null && pItem.x == MOUSE_MESSAGES_PANEL_BUTTONS_COMBAT));
		} else {
			iCurrentTexture = UtilsGL.setTexture(messagePanelTiles[MessagesPanel.TYPE_COMBAT], iCurrentTexture);
			drawTile(messagePanelTiles[MessagesPanel.TYPE_COMBAT], messagePanelIconPoints[MessagesPanel.TYPE_COMBAT],
					(pItem != null && pItem.x == MOUSE_MESSAGES_PANEL_BUTTONS_COMBAT));
		}
		if (MessagesPanel.TYPE_HEROES == iMessagesType) {
			iCurrentTexture = UtilsGL.setTexture(messagePanelTilesON[MessagesPanel.TYPE_HEROES], iCurrentTexture);
			drawTile(messagePanelTilesON[MessagesPanel.TYPE_HEROES], messagePanelIconPoints[MessagesPanel.TYPE_HEROES],
					(pItem != null && pItem.x == MOUSE_MESSAGES_PANEL_BUTTONS_HEROES));
		} else {
			iCurrentTexture = UtilsGL.setTexture(messagePanelTiles[MessagesPanel.TYPE_HEROES], iCurrentTexture);
			drawTile(messagePanelTiles[MessagesPanel.TYPE_HEROES], messagePanelIconPoints[MessagesPanel.TYPE_HEROES],
					(pItem != null && pItem.x == MOUSE_MESSAGES_PANEL_BUTTONS_HEROES));
		}
		if (MessagesPanel.TYPE_SYSTEM == iMessagesType) {
			iCurrentTexture = UtilsGL.setTexture(messagePanelTilesON[MessagesPanel.TYPE_SYSTEM], iCurrentTexture);
			drawTile(messagePanelTilesON[MessagesPanel.TYPE_SYSTEM], messagePanelIconPoints[MessagesPanel.TYPE_SYSTEM],
					(pItem != null && pItem.x == MOUSE_MESSAGES_PANEL_BUTTONS_SYSTEM));
		} else {
			iCurrentTexture = UtilsGL.setTexture(messagePanelTiles[MessagesPanel.TYPE_SYSTEM], iCurrentTexture);
			drawTile(messagePanelTiles[MessagesPanel.TYPE_SYSTEM], messagePanelIconPoints[MessagesPanel.TYPE_SYSTEM],
					(pItem != null && pItem.x == MOUSE_MESSAGES_PANEL_BUTTONS_SYSTEM));
		}

		// Scrolls
		if (MessagesPanel.getPages(iMessagesType) > 1 && MessagesPanel.getPagesCurrent(iMessagesType) > 1) {
			iCurrentTexture = UtilsGL.setTexture(tileScrollUp, iCurrentTexture);
			drawTile(tileScrollUp, messagePanelIconScrollUpPoint,
					(pItem != null && pItem.x == MOUSE_MESSAGES_PANEL_BUTTONS_SCROLL_UP));
		} else {
			iCurrentTexture = UtilsGL.setTexture(tileScrollUpDisabled, iCurrentTexture);
			drawTile(tileScrollUpDisabled, messagePanelIconScrollUpPoint);
		}
		if (MessagesPanel.getPages(iMessagesType) > 1
				&& MessagesPanel.getPagesCurrent(iMessagesType) < MessagesPanel.getPages(iMessagesType)) {
			iCurrentTexture = UtilsGL.setTexture(tileScrollDown, iCurrentTexture);
			drawTile(tileScrollDown, messagePanelIconScrollDownPoint,
					(pItem != null && pItem.x == MOUSE_MESSAGES_PANEL_BUTTONS_SCROLL_DOWN));
		} else {
			iCurrentTexture = UtilsGL.setTexture(tileScrollDownDisabled, iCurrentTexture);
			drawTile(tileScrollDownDisabled, messagePanelIconScrollDownPoint);
		}

		// Subpanel donde irá el texto
		iCurrentTexture = UtilsGL.setTexture(tileMessagesPanelSubPanel[0], iCurrentTexture);
		renderBackground(tileMessagesPanelSubPanel, messagesPanelSubPanelPoint, MESSAGES_PANEL_SUBPANEL_WIDTH,
				MESSAGES_PANEL_SUBPANEL_HEIGHT);

		UtilsGL.glEnd();

		// Pages
		String sText = MessagesPanel.getPagesCurrent(iMessagesType) + " / " + MessagesPanel.getPages(iMessagesType); //$NON-NLS-1$
		// XAVI GL11.glColor4f (1, 1, 1, 1);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		UtilsGL.glBegin(GL11.GL_QUADS);
		UtilsGL.drawString(sText, messagePanelPagesPositionPoint.x - UtilFont.getWidth(sText) / 2,
				messagePanelPagesPositionPoint.y, ColorGL.BLACK);
		UtilsGL.glEnd();

		// Mensajes
		MessagesPanel.render(mouseX, mouseY, getMessagesPanelActive(),
				messagesPanelSubPanelPoint.x + tileMessagesPanel[3].getTileWidth(),
				messagesPanelSubPanelPoint.y + tileMessagesPanel[1].getTileHeight());
	}

	public void renderPilePanel(int mouseX, int mouseY, int mousePanel) {
		Point pItem = isMouseOnPileButtons(mouseX, mouseY);

		// XAVI GL11.glColor4f (1, 1, 1, 1);
		int iCurrentTexture = tileMatsPanel[0].getTextureID();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, iCurrentTexture);
		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		UtilsGL.glBegin(GL11.GL_QUADS);
		renderBackground(tileMatsPanel, pilePanelPoint, PILE_PANEL_WIDTH, PILE_PANEL_HEIGHT);

		// Close button
		iCurrentTexture = UtilsGL.setTexture(tileButtonClose, iCurrentTexture);
		if (pItem != null && pItem.x == MOUSE_PILE_PANEL_BUTTONS_CLOSE) {
			drawTile(tileButtonClose, pilePanelClosePoint);
		} else {
			drawTile(tileButtonCloseDisabled, pilePanelClosePoint);
		}

		// Scroll up
		if (pilePanelPageIndex > 0) {
			// Enabled
			iCurrentTexture = UtilsGL.setTexture(tileScrollUp, iCurrentTexture);
			drawTile(tileScrollUp, pilePanelIconScrollUpPoint,
					(pItem != null && pItem.x == MOUSE_PILE_PANEL_BUTTONS_SCROLL_UP));
		} else {
			// Disabled
			iCurrentTexture = UtilsGL.setTexture(tileScrollUpDisabled, iCurrentTexture);
			drawTile(tileScrollUpDisabled, pilePanelIconScrollUpPoint);
		}
		// Scroll down
		if ((pilePanelPageIndex + 1) < pilePanelMaxPages) {
			// Enabled
			iCurrentTexture = UtilsGL.setTexture(tileScrollDown, iCurrentTexture);
			drawTile(tileScrollDown, pilePanelIconScrollDownPoint,
					(pItem != null && pItem.x == MOUSE_PILE_PANEL_BUTTONS_SCROLL_DOWN));
		} else {
			// Disabled
			iCurrentTexture = UtilsGL.setTexture(tileScrollDownDisabled, iCurrentTexture);
			drawTile(tileScrollDownDisabled, pilePanelIconScrollDownPoint);
		}

		// Items
		if (menuPile != null) {
			int iFirstIndex = pilePanelPageIndex * PILE_PANEL_MAX_ITEMS_PER_PAGE;
			int iMaxItems = Math.min(menuPile.getItems().size() - iFirstIndex, PILE_PANEL_MAX_ITEMS_PER_PAGE);
			Tile tile;
			SmartMenu smAux;
			for (int i = 0; i < iMaxItems; i++) {
				smAux = menuPile.getItems().get(i + iFirstIndex);
				if (smAux.getType() == SmartMenu.TYPE_MENU) {
					// Submenu
					iCurrentTexture = UtilsGL.setTexture(tileBottomItemSM, iCurrentTexture);
					drawTile(tileBottomItemSM,
							pilePanelItemPoints[i].x + tileBottomItemSM.getTileWidth() / 2
									- tileBottomItemSM.getTileWidth() / 2,
							pilePanelItemPoints[i].y + tileBottomItemSM.getTileHeight() / 2
									- tileBottomItemSM.getTileHeight() / 2,
							(pItem != null && pItem.y == i));
				} else {
					// Item
					iCurrentTexture = UtilsGL.setTexture(tileBottomItem, iCurrentTexture);
					drawTile(tileBottomItem,
							pilePanelItemPoints[i].x + tileBottomItem.getTileWidth() / 2
									- tileBottomItem.getTileWidth() / 2,
							pilePanelItemPoints[i].y + tileBottomItem.getTileHeight() / 2
									- tileBottomItem.getTileHeight() / 2,
							(pItem != null && pItem.y == i));
				}

				// Icono
				tile = smAux.getIcon();
				if (tile != null) {
					iCurrentTexture = UtilsGL.setTexture(tile, iCurrentTexture);
					drawTile(tile, pilePanelItemPoints[i].x, pilePanelItemPoints[i].y, BOTTOM_ITEM_WIDTH,
							BOTTOM_ITEM_HEIGHT, (pItem != null && pItem.y == i));
				}

				if (smAux.getCommand() != null && (smAux.getCommand().equals(CommandPanel.COMMAND_STOCKPILE_ENABLE_ITEM)
						|| smAux.getCommand().equals(CommandPanel.COMMAND_CONTAINER_ENABLE_ITEM))) {
					// Cruz roja
					tile = BIG_RED_CROSS_TILE; // World.getTileRedCross ();
					iCurrentTexture = UtilsGL.setTexture(tile, iCurrentTexture);
					drawTile(tile,
							pilePanelItemPoints[i].x + tileBottomItem.getTileWidth() / 2 - tile.getTileWidth() / 2,
							pilePanelItemPoints[i].y + tileBottomItem.getTileHeight() / 2 - tile.getTileHeight() / 2,
							(pItem != null && pItem.y == i));
				}
			}
		}

		// Configuration buttons
		iCurrentTexture = UtilsGL.setTexture(tileConfigCopy, iCurrentTexture);
		drawTile(tileConfigCopy, pilePanelIconConfigCopyPoint.x, pilePanelIconConfigCopyPoint.y,
				(pItem != null && pItem.x == MOUSE_PILE_PANEL_BUTTONS_CONFIG_COPY));

		if (pilePanelIsLocked) {
			iCurrentTexture = UtilsGL.setTexture(tileConfigLockLocked, iCurrentTexture);
			drawTile(tileConfigLockLocked, pilePanelIconConfigLockPoint.x, pilePanelIconConfigLockPoint.y,
					(pItem != null && pItem.x == MOUSE_PILE_PANEL_BUTTONS_CONFIG_LOCK));
		} else {
			iCurrentTexture = UtilsGL.setTexture(tileConfigLock, iCurrentTexture);
			drawTile(tileConfigLock, pilePanelIconConfigLockPoint.x, pilePanelIconConfigLockPoint.y,
					(pItem != null && pItem.x == MOUSE_PILE_PANEL_BUTTONS_CONFIG_LOCK));
		}

		iCurrentTexture = UtilsGL.setTexture(tileConfigLockAll, iCurrentTexture);
		drawTile(tileConfigLockAll, pilePanelIconConfigLockAllPoint.x, pilePanelIconConfigLockAllPoint.y,
				(pItem != null && pItem.x == MOUSE_PILE_PANEL_BUTTONS_CONFIG_LOCK_ALL));

		iCurrentTexture = UtilsGL.setTexture(tileConfigUnlockAll, iCurrentTexture);
		drawTile(tileConfigUnlockAll, pilePanelIconConfigUnlockAllPoint.x, pilePanelIconConfigUnlockAllPoint.y,
				(pItem != null && pItem.x == MOUSE_PILE_PANEL_BUTTONS_CONFIG_UNLOCK_ALL));
		UtilsGL.glEnd();

		// Pages
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		UtilsGL.glBegin(GL11.GL_QUADS);

		String sText = (pilePanelPageIndex + 1) + " / " + pilePanelMaxPages; //$NON-NLS-1$
		UtilsGL.drawString(sText, pilePanelPagesPositionPoint.x - UtilFont.getWidth(sText) / 2,
				pilePanelPagesPositionPoint.y, ColorGL.BLACK);

		if (menuPile != null) {
			int iFirstIndex = pilePanelPageIndex * PILE_PANEL_MAX_ITEMS_PER_PAGE;
			int iMaxItems = Math.min(menuPile.getItems().size() - iFirstIndex, PILE_PANEL_MAX_ITEMS_PER_PAGE);
			SmartMenu smAux;

			// Stock
			for (int i = 0; i < iMaxItems; i++) {
				smAux = menuPile.getItems().get(i + iFirstIndex);
				if (smAux.getType() == SmartMenu.TYPE_ITEM) {
					if (smAux.getCommand().equals(CommandPanel.COMMAND_STOCKPILE_ENABLE_ITEM)
							|| smAux.getCommand().equals(CommandPanel.COMMAND_STOCKPILE_DISABLE_ITEM)) {
						int iNumItems = Item.getNumItems(UtilsIniHeaders.getIntIniHeader(smAux.getParameter()), false,
								World.MAP_DEPTH);
						sText = Integer.toString(iNumItems);
						UtilsGL.drawStringWithBorder(sText,
								pilePanelItemPoints[i].x + tileBottomItem.getTileWidth() / 2
										- UtilFont.getWidth(sText) / 2,
								pilePanelItemPoints[i].y + tileBottomItem.getTileHeight() - UtilFont.MAX_HEIGHT,
								ColorGL.WHITE, ColorGL.BLACK);
					} else if (smAux.getCommand().equals(CommandPanel.COMMAND_CONTAINER_ENABLE_ITEM)
							|| smAux.getCommand().equals(CommandPanel.COMMAND_CONTAINER_DISABLE_ITEM)) {
						int iNumItems = Item.getNumItems(UtilsIniHeaders.getIntIniHeader(smAux.getParameter2()), false,
								World.MAP_DEPTH);
						sText = Integer.toString(iNumItems);
						UtilsGL.drawStringWithBorder(sText,
								pilePanelItemPoints[i].x + tileBottomItem.getTileWidth() / 2
										- UtilFont.getWidth(sText) / 2,
								pilePanelItemPoints[i].y + tileBottomItem.getTileHeight() - UtilFont.MAX_HEIGHT,
								ColorGL.WHITE, ColorGL.BLACK);
					}
				}
			}
		}

		// Title
		String sTitle;
		if (isPilePanelIsContainer()) {
			sTitle = Messages.getString("UIPanel.62"); //$NON-NLS-1$
		} else {
			sTitle = Messages.getString("UIPanel.64"); //$NON-NLS-1$
		}
		UtilsGL.drawStringWithBorder(sTitle, pilePanelPoint.x + tileMatsPanel[3].getTileWidth(),
				pilePanelPoint.y + UtilFont.MAX_HEIGHT, ColorGL.ORANGE, ColorGL.BLACK);

		UtilsGL.glEnd();
	}

	public void renderProfessionsPanel(int mouseX, int mouseY, int mousePanel) {
		Point pItem = isMouseOnProfessionsButtons(mouseX, mouseY);

		// XAVI GL11.glColor4f (1, 1, 1, 1);
		int iCurrentTexture = tileMatsPanel[0].getTextureID();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, iCurrentTexture);
		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		UtilsGL.glBegin(GL11.GL_QUADS);
		renderBackground(tileMatsPanel, professionsPanelPoint, PROFESSIONS_PANEL_WIDTH, PROFESSIONS_PANEL_HEIGHT);

		// Close button
		iCurrentTexture = UtilsGL.setTexture(tileButtonClose, iCurrentTexture);
		if (pItem != null && pItem.x == MOUSE_PROFESSIONS_PANEL_BUTTONS_CLOSE) {
			drawTile(tileButtonClose, professionsPanelClosePoint);
		} else {
			drawTile(tileButtonCloseDisabled, professionsPanelClosePoint);
		}

		// Scroll up
		if (professionsPanelPageIndex > 0) {
			// Enabled
			iCurrentTexture = UtilsGL.setTexture(tileScrollUp, iCurrentTexture);
			drawTile(tileScrollUp, professionsPanelIconScrollUpPoint,
					(pItem != null && pItem.x == MOUSE_PROFESSIONS_PANEL_BUTTONS_SCROLL_UP));
		} else {
			// Disabled
			iCurrentTexture = UtilsGL.setTexture(tileScrollUpDisabled, iCurrentTexture);
			drawTile(tileScrollUpDisabled, professionsPanelIconScrollUpPoint);
		}
		// Scroll down
		if ((professionsPanelPageIndex + 1) < professionsPanelMaxPages) {
			// Enabled
			iCurrentTexture = UtilsGL.setTexture(tileScrollDown, iCurrentTexture);
			drawTile(tileScrollDown, professionsPanelIconScrollDownPoint,
					(pItem != null && pItem.x == MOUSE_PROFESSIONS_PANEL_BUTTONS_SCROLL_DOWN));
		} else {
			// Disabled
			iCurrentTexture = UtilsGL.setTexture(tileScrollDownDisabled, iCurrentTexture);
			drawTile(tileScrollDownDisabled, professionsPanelIconScrollDownPoint);
		}

		// Items
		if (menuProfessions != null) {
			int iFirstIndex = professionsPanelPageIndex * PROFESSIONS_PANEL_MAX_ITEMS_PER_PAGE;
			int iMaxItems = Math.min(menuProfessions.getItems().size() - iFirstIndex,
					PROFESSIONS_PANEL_MAX_ITEMS_PER_PAGE);
			Tile tile;
			SmartMenu smAux;
			for (int i = 0; i < iMaxItems; i++) {
				smAux = menuProfessions.getItems().get(i + iFirstIndex);
				if (smAux.getType() == SmartMenu.TYPE_MENU) {
					// Submenu
					iCurrentTexture = UtilsGL.setTexture(tileBottomItemSM, iCurrentTexture);
					drawTile(tileBottomItemSM,
							professionsPanelItemPoints[i].x + tileBottomItemSM.getTileWidth() / 2
									- tileBottomItemSM.getTileWidth() / 2,
							professionsPanelItemPoints[i].y + tileBottomItemSM.getTileHeight() / 2
									- tileBottomItemSM.getTileHeight() / 2,
							(pItem != null && pItem.y == i));
				} else {
					// Item
					iCurrentTexture = UtilsGL.setTexture(tileBottomItem, iCurrentTexture);
					drawTile(tileBottomItem,
							professionsPanelItemPoints[i].x + tileBottomItem.getTileWidth() / 2
									- tileBottomItem.getTileWidth() / 2,
							professionsPanelItemPoints[i].y + tileBottomItem.getTileHeight() / 2
									- tileBottomItem.getTileHeight() / 2,
							(pItem != null && pItem.y == i));
				}

				// Icono
				tile = smAux.getIcon();
				if (tile != null) {
					iCurrentTexture = UtilsGL.setTexture(tile, iCurrentTexture);
					drawTile(tile, professionsPanelItemPoints[i].x, professionsPanelItemPoints[i].y, BOTTOM_ITEM_WIDTH,
							BOTTOM_ITEM_HEIGHT, (pItem != null && pItem.y == i));
				}

				if (smAux.getCommand() != null
						&& (smAux.getCommand().equals(CommandPanel.COMMAND_PROFESSIONS_ENABLE_ITEM)
								|| smAux.getCommand().equals(CommandPanel.COMMAND_JOB_GROUP_ENABLE_ITEM))) {
					// Cruz roja
					tile = BIG_RED_CROSS_TILE;
					iCurrentTexture = UtilsGL.setTexture(tile, iCurrentTexture);
					drawTile(tile,
							professionsPanelItemPoints[i].x + tileBottomItem.getTileWidth() / 2
									- tile.getTileWidth() / 2,
							professionsPanelItemPoints[i].y + tileBottomItem.getTileHeight() / 2
									- tile.getTileHeight() / 2,
							(pItem != null && pItem.y == i));
				}
			}
		}

		UtilsGL.glEnd();

		// Pages
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		UtilsGL.glBegin(GL11.GL_QUADS);

		String sText = (professionsPanelPageIndex + 1) + " / " + professionsPanelMaxPages; //$NON-NLS-1$
		UtilsGL.drawString(sText, professionsPanelPagesPositionPoint.x - UtilFont.getWidth(sText) / 2,
				professionsPanelPagesPositionPoint.y, ColorGL.BLACK);

		// Title
		if (professionsPanelIsCitizen) {
			UtilsGL.drawStringWithBorder(Messages.getString("UIPanel.63"), //$NON-NLS-1$
					professionsPanelPoint.x + tileMatsPanel[3].getTileWidth(),
					professionsPanelPoint.y + UtilFont.MAX_HEIGHT, ColorGL.ORANGE, ColorGL.BLACK);
		} else {
			UtilsGL.drawStringWithBorder(Messages.getString("UIPanel.67"), //$NON-NLS-1$
					professionsPanelPoint.x + tileMatsPanel[3].getTileWidth(),
					professionsPanelPoint.y + UtilFont.MAX_HEIGHT, ColorGL.ORANGE, ColorGL.BLACK);
		}

		UtilsGL.glEnd();
	}

	public void renderMatsPanel(int mouseX, int mouseY, int mousePanel) {
		Point pItem = isMouseOnMatsButtons(mouseX, mouseY);

		// XAVI GL11.glColor4f (1, 1, 1, 1);
		int iCurrentTexture = tileMatsPanel[0].getTextureID();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, iCurrentTexture);
		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		UtilsGL.glBegin(GL11.GL_QUADS);
		renderBackground(tileMatsPanel, matsPanelPoint, MATS_PANEL_WIDTH, MATS_PANEL_HEIGHT);

		// Close button
		iCurrentTexture = UtilsGL.setTexture(tileButtonClose, iCurrentTexture);
		if (pItem != null && pItem.x == MOUSE_MATS_PANEL_BUTTONS_CLOSE) {
			drawTile(tileButtonClose, matsPanelClosePoint);
		} else {
			drawTile(tileButtonCloseDisabled, matsPanelClosePoint);
		}

		// Subpanel donde irán los items
		iCurrentTexture = UtilsGL.setTexture(tileMatsPanelSubPanel[0], iCurrentTexture);
		renderBackground(tileMatsPanelSubPanel, matsPanelSubPanelPoint, MATS_PANEL_SUBPANEL_WIDTH,
				MATS_PANEL_SUBPANEL_HEIGHT);

		// "Tabs"
		iCurrentTexture = UtilsGL.setTexture(tileBottomItem, iCurrentTexture);
		for (int i = 0; i < MatsPanelData.numGroups; i++) {
			drawTile(tileBottomItem, matsPanelIconPoints[i],
					(pItem != null && pItem.x == MOUSE_MATS_PANEL_BUTTONS_GROUPS && pItem.y == i));
		}
		for (int i = 0; i < MatsPanelData.numGroups; i++) {
			if (i == getMatsPanelActive()) {
				iCurrentTexture = UtilsGL.setTexture(matsPanelTilesON[i], iCurrentTexture);
				drawTile(matsPanelTilesON[i],
						matsPanelIconPoints[i].x + tileBottomItem.getTileWidth() / 2
								- matsPanelTilesON[i].getTileWidth() / 2,
						matsPanelIconPoints[i].y + tileBottomItem.getTileHeight() / 2
								- matsPanelTilesON[i].getTileHeight() / 2,
						(pItem != null && pItem.x == MOUSE_MATS_PANEL_BUTTONS_GROUPS && pItem.y == i));
			} else {
				iCurrentTexture = UtilsGL.setTexture(matsPanelTiles[i], iCurrentTexture);
				drawTile(matsPanelTiles[i],
						matsPanelIconPoints[i].x + tileBottomItem.getTileWidth() / 2
								- matsPanelTiles[i].getTileWidth() / 2,
						matsPanelIconPoints[i].y + tileBottomItem.getTileHeight() / 2
								- matsPanelTiles[i].getTileHeight() / 2,
						(pItem != null && pItem.x == MOUSE_MATS_PANEL_BUTTONS_GROUPS && pItem.y == i));
			}
		}

		// Scrolls
		if (matsIndexPages[getMatsPanelActive()] > 0) {
			iCurrentTexture = UtilsGL.setTexture(tileScrollUp, iCurrentTexture);
			drawTile(tileScrollUp, matsPanelIconScrollUpPoint,
					(pItem != null && pItem.x == MOUSE_MATS_PANEL_BUTTONS_SCROLL_UP));
		} else {
			iCurrentTexture = UtilsGL.setTexture(tileScrollUpDisabled, iCurrentTexture);
			drawTile(tileScrollUpDisabled, matsPanelIconScrollUpPoint);
		}
		if (matsIndexPages[getMatsPanelActive()] < (matsNumPages[getMatsPanelActive()] - 1)) {
			iCurrentTexture = UtilsGL.setTexture(tileScrollDown, iCurrentTexture);
			drawTile(tileScrollDown, matsPanelIconScrollDownPoint,
					(pItem != null && pItem.x == MOUSE_MATS_PANEL_BUTTONS_SCROLL_DOWN));
		} else {
			iCurrentTexture = UtilsGL.setTexture(tileScrollDownDisabled, iCurrentTexture);
			drawTile(tileScrollDownDisabled, matsPanelIconScrollDownPoint);
		}

		// Icons
		int iIndex = matsIndexPages[getMatsPanelActive()] * MATS_PANEL_MAX_ITEMS_PER_PAGE;
		int iMax = Math.min(MATS_PANEL_MAX_ITEMS_PER_PAGE,
				(MatsPanelData.tileGroups.get(getMatsPanelActive()).size() - iIndex));
		iCurrentTexture = UtilsGL.setTexture(tileBottomItem, iCurrentTexture);
		for (int i = 0; i < iMax; i++) {
			drawTile(tileBottomItem, matsPanelItemPoints[i]);
		}
		Tile tile;
		for (int i = 0; i < iMax; i++) {
			if (MatsPanelData.tileGroups.get(getMatsPanelActive()).size() > (i + iIndex)) {
				tile = MatsPanelData.tileGroups.get(getMatsPanelActive()).get(i + iIndex);
				iCurrentTexture = UtilsGL.setTexture(tile, iCurrentTexture);
				// drawTile (tile, matsPanelItemPoints [i].x + tileBottomItem.getTileWidth () /
				// 2 - tile.getTileWidth () / 2, matsPanelItemPoints [i].y +
				// tileBottomItem.getTileHeight () / 2 - tile.getTileHeight () / 2, false);
				// //BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT, false);
				drawTile(tile, matsPanelItemPoints[i].x + tileBottomItem.getTileWidth() / 2 - tile.getTileWidth() / 2,
						matsPanelItemPoints[i].y + tileBottomItem.getTileHeight() / 2 - BOTTOM_ITEM_HEIGHT / 2,
						BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT, false); // BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT, false);
			}
		}
		UtilsGL.glEnd();

		// Numbers
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		UtilsGL.glBegin(GL11.GL_QUADS);
		String sText;
		for (int i = 0; i < iMax; i++) {
			if (MatsPanelData.tileGroups.get(getMatsPanelActive()).size() > (i + iIndex)) {
				tile = MatsPanelData.tileGroups.get(getMatsPanelActive()).get(i + iIndex);
				sText = Integer.toString(
						Item.getNumItems(UtilsIniHeaders.getIntIniHeader(tile.getIniHeader()), false, World.MAP_DEPTH));
				UtilsGL.drawStringWithBorder(sText,
						matsPanelItemPoints[i].x + tileBottomItem.getTileWidth() / 2 - UtilFont.getWidth(sText) / 2,
						matsPanelItemPoints[i].y + tileBottomItem.getTileHeight() - UtilFont.MAX_HEIGHT, ColorGL.WHITE,
						ColorGL.BLACK);
			}
		}

		// Pages
		sText = (matsIndexPages[getMatsPanelActive()] + 1) + " / " + matsNumPages[getMatsPanelActive()]; //$NON-NLS-1$
		UtilsGL.drawString(sText, matsPanelPagesPositionPoint.x - UtilFont.getWidth(sText) / 2,
				matsPanelPagesPositionPoint.y, ColorGL.BLACK);
		UtilsGL.glEnd();
	}

	public static boolean checkGroupsPanelEnabled(int iLivingsPanelActive) {
		return (iLivingsPanelActive == LIVINGS_PANEL_TYPE_CITIZENS && livingsPanelCitizensGroupActive != -1)
				|| (iLivingsPanelActive == LIVINGS_PANEL_TYPE_SOLDIERS && livingsPanelSoldiersGroupActive != -1);
	}

	public void renderLivingsPanel(int mouseX, int mouseY, int mousePanel) {
		// Possible mini icon blinks?
		// Blink
		TutorialFlow tutorialFlow = null;
		if ((blinkTurns >= MAX_BLINK_TURNS / 2)) {
			if (Game.getCurrentMissionData() != null && ImagesPanel.getCurrentFlowIndex() >= 0
					&& ImagesPanel.getCurrentFlowIndex() < Game.getCurrentMissionData().getTutorialFlows().size()) {
				tutorialFlow = Game.getCurrentMissionData().getTutorialFlows().get(ImagesPanel.getCurrentFlowIndex());
			}
		}

		Point pItem = isMouseOnLivingsButtons(mouseX, mouseY);

		// XAVI GL11.glColor4f (1, 1, 1, 1);
		int iCurrentTexture = tileLivingsPanel[0].getTextureID();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, iCurrentTexture);
		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		UtilsGL.glBegin(GL11.GL_QUADS);
		renderBackground(tileLivingsPanel, livingsPanelPoint, LIVINGS_PANEL_WIDTH, LIVINGS_PANEL_HEIGHT);

		// Close button
		iCurrentTexture = UtilsGL.setTexture(tileButtonClose, iCurrentTexture);
		if (pItem != null && pItem.x == MOUSE_LIVINGS_PANEL_BUTTONS_CLOSE) {
			drawTile(tileButtonClose, livingsPanelClosePoint);
		} else {
			drawTile(tileButtonCloseDisabled, livingsPanelClosePoint);
		}

		// Groups panel
		if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_SOLDIERS) {
			// Subpanel
			iCurrentTexture = UtilsGL.setTexture(tileLivingsGroupPanel[0], iCurrentTexture);
			renderBackground(tileLivingsGroupPanel, livingsGroupPanelPoint, LIVINGS_PANEL_GROUPS_WIDTH,
					LIVINGS_PANEL_GROUPS_HEIGHT);

			// No-group icon
			if (livingsPanelSoldiersGroupActive == -1 || mousePanel == MOUSE_LIVINGS_PANEL_SGROUP_NOGROUP) {
				iCurrentTexture = UtilsGL.setTexture(tileLivingsNoGroupON, iCurrentTexture);
				drawTile(tileLivingsNoGroupON, livingsGroupPanelFirstIconPoint);
			} else {
				// Miramos si el grupo tiene miembros
				if (Game.getWorld().getSoldierGroups().getSoldiersWithoutGroup().size() > 0) {
					iCurrentTexture = UtilsGL.setTexture(tileLivingsNoGroupGreen, iCurrentTexture);
					drawTile(tileLivingsNoGroupGreen, livingsGroupPanelFirstIconPoint);
				} else {
					iCurrentTexture = UtilsGL.setTexture(tileLivingsNoGroup, iCurrentTexture);
					drawTile(tileLivingsNoGroup, livingsGroupPanelFirstIconPoint);
				}
			}

			// Group icons
			for (int i = 0; i < SoldierGroups.MAX_GROUPS; i++) {
				if (livingsPanelSoldiersGroupActive == i
						|| (pItem != null && pItem.x == MOUSE_LIVINGS_PANEL_SGROUP_GROUP && pItem.y == i)) {
					iCurrentTexture = UtilsGL.setTexture(tileLivingsGroupON, iCurrentTexture);
					drawTile(tileLivingsGroupON, livingsGroupPanelFirstIconPoint.x,
							livingsGroupPanelFirstIconPoint.y + (i + 1) * livingsGroupPanelIconsSeparation, false);
				} else {
					// Miramos si el grupo tiene miembros
					if (Game.getWorld().getSoldierGroups().getGroup(i).getLivingIDs().size() > 0) {
						iCurrentTexture = UtilsGL.setTexture(tileLivingsGroupGreen, iCurrentTexture);
						drawTile(tileLivingsGroupGreen, livingsGroupPanelFirstIconPoint.x,
								livingsGroupPanelFirstIconPoint.y + (i + 1) * livingsGroupPanelIconsSeparation, false);
					} else {
						iCurrentTexture = UtilsGL.setTexture(tileLivingsGroup, iCurrentTexture);
						drawTile(tileLivingsGroup, livingsGroupPanelFirstIconPoint.x,
								livingsGroupPanelFirstIconPoint.y + (i + 1) * livingsGroupPanelIconsSeparation, false);
					}
				}
			}

			// Single group subpanel
			if (livingsPanelSoldiersGroupActive != -1) {
				iCurrentTexture = UtilsGL.setTexture(tileLivingsGroupPanel[0], iCurrentTexture);
				renderBackground(tileLivingsGroupPanel, livingsSingleGroupPanelPoint, LIVINGS_PANEL_SINGLE_GROUP_WIDTH,
						LIVINGS_PANEL_SINGLE_GROUP_HEIGHT);

				int iGroupState = Game.getWorld().getSoldierGroups().getGroup(livingsPanelSoldiersGroupActive)
						.getState();
				// Botones
				if (mousePanel == MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_RENAME) {
					iCurrentTexture = UtilsGL.setTexture(tileLivingsSingleGroupRenameON, iCurrentTexture);
					drawTile(tileLivingsSingleGroupRenameON, livingsSingleGroupRenamePoint);
				} else {
					iCurrentTexture = UtilsGL.setTexture(tileLivingsSingleGroupRename, iCurrentTexture);
					drawTile(tileLivingsSingleGroupRename, livingsSingleGroupRenamePoint);
				}
				if (iGroupState == SoldierGroupData.STATE_GUARD
						|| mousePanel == MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_GUARD) {
					iCurrentTexture = UtilsGL.setTexture(tileLivingsSingleGroupGuardON, iCurrentTexture);
					drawTile(tileLivingsSingleGroupGuardON, livingsSingleGroupGuardPoint);
				} else {
					iCurrentTexture = UtilsGL.setTexture(tileLivingsSingleGroupGuard, iCurrentTexture);
					drawTile(tileLivingsSingleGroupGuard, livingsSingleGroupGuardPoint);
				}
				if (iGroupState == SoldierGroupData.STATE_PATROL
						|| mousePanel == MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_PATROL) {
					iCurrentTexture = UtilsGL.setTexture(tileLivingsSingleGroupPatrolON, iCurrentTexture);
					drawTile(tileLivingsSingleGroupPatrolON, livingsSingleGroupPatrolPoint);
				} else {
					iCurrentTexture = UtilsGL.setTexture(tileLivingsSingleGroupPatrol, iCurrentTexture);
					drawTile(tileLivingsSingleGroupPatrol, livingsSingleGroupPatrolPoint);
				}
				if (iGroupState == SoldierGroupData.STATE_BOSS
						|| mousePanel == MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_BOSS) {
					iCurrentTexture = UtilsGL.setTexture(tileLivingsSingleGroupBossON, iCurrentTexture);
					drawTile(tileLivingsSingleGroupBossON, livingsSingleGroupBossPoint);
				} else {
					iCurrentTexture = UtilsGL.setTexture(tileLivingsSingleGroupBoss, iCurrentTexture);
					drawTile(tileLivingsSingleGroupBoss, livingsSingleGroupBossPoint);
				}
				if (mousePanel == MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_DISBAND) {
					iCurrentTexture = UtilsGL.setTexture(tileLivingsSingleGroupDisbandON, iCurrentTexture);
					drawTile(tileLivingsSingleGroupDisbandON, livingsSingleGroupDisbandPoint);
				} else {
					iCurrentTexture = UtilsGL.setTexture(tileLivingsSingleGroupDisband, iCurrentTexture);
					drawTile(tileLivingsSingleGroupDisband, livingsSingleGroupDisbandPoint);
				}
				if (mousePanel == MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_AUTOEQUIP) {
					iCurrentTexture = UtilsGL.setTexture(tileLivingsRowAutoequipON, iCurrentTexture);
					drawTile(tileLivingsRowAutoequipON, livingsSingleGroupAutoequipPoint);
				} else {
					iCurrentTexture = UtilsGL.setTexture(tileLivingsRowAutoequip, iCurrentTexture);
					drawTile(tileLivingsRowAutoequip, livingsSingleGroupAutoequipPoint);
				}

				// Text
				UtilsGL.glEnd();
				iCurrentTexture = Game.TEXTURE_FONT_ID;
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
				GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
				UtilsGL.glBegin(GL11.GL_QUADS);

				// Group name
				String sText = Game.getWorld().getSoldierGroups().getGroup(livingsPanelSoldiersGroupActive).getName();
				UtilsGL.drawStringWithBorder(sText,
						livingsSingleGroupPanelPoint.x + LIVINGS_PANEL_SINGLE_GROUP_WIDTH / 2
								- UtilFont.getWidth(sText) / 2,
						livingsSingleGroupPanelPoint.y, ColorGL.WHITE, ColorGL.BLACK);
			}

			// Text
			if (iCurrentTexture != Game.TEXTURE_FONT_ID) {
				UtilsGL.glEnd();
				iCurrentTexture = Game.TEXTURE_FONT_ID;
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
				GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
				UtilsGL.glBegin(GL11.GL_QUADS);
			}

			// Group icons (1, 2, 3, 4, 5, 6, 7, 8)
			String sNumber;
			for (int i = 0; i < SoldierGroups.MAX_GROUPS; i++) {
				sNumber = Integer.toString(i + 1);
				UtilsGL.drawStringWithBorder(sNumber,
						livingsGroupPanelFirstIconPoint.x + tileLivingsGroup.getTileWidth() / 2
								- UtilFont.getWidth(sNumber) / 2,
						livingsGroupPanelFirstIconPoint.y + (i + 1) * livingsGroupPanelIconsSeparation
								+ tileLivingsGroup.getTileHeight() / 2 - UtilFont.MAX_HEIGHT / 2,
						ColorGL.WHITE, ColorGL.BLACK);
			}
		} else if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_CITIZENS) {
			// Subpanel
			iCurrentTexture = UtilsGL.setTexture(tileLivingsGroupPanel[0], iCurrentTexture);
			renderBackground(tileLivingsGroupPanel, livingsGroupPanelPoint, LIVINGS_PANEL_GROUPS_WIDTH,
					LIVINGS_PANEL_GROUPS_HEIGHT);

			// No-group icon
			if (livingsPanelCitizensGroupActive == -1 || mousePanel == MOUSE_LIVINGS_PANEL_CGROUP_NOGROUP) {
				iCurrentTexture = UtilsGL.setTexture(tileLivingsNoJobGroupON, iCurrentTexture);
				drawTile(tileLivingsNoJobGroupON, livingsGroupPanelFirstIconPoint);
			} else {
				// Miramos si el grupo tiene miembros
				if (Game.getWorld().getCitizenGroups().getCitizensWithoutGroup().size() > 0) {
					iCurrentTexture = UtilsGL.setTexture(tileLivingsNoJobGroupGreen, iCurrentTexture);
					drawTile(tileLivingsNoJobGroupGreen, livingsGroupPanelFirstIconPoint);
				} else {
					iCurrentTexture = UtilsGL.setTexture(tileLivingsNoJobGroup, iCurrentTexture);
					drawTile(tileLivingsNoJobGroup, livingsGroupPanelFirstIconPoint);
				}
			}

			// Group icons
			for (int i = 0; i < CitizenGroups.MAX_GROUPS; i++) {
				if (livingsPanelCitizensGroupActive == i
						|| (pItem != null && pItem.x == MOUSE_LIVINGS_PANEL_CGROUP_GROUP && pItem.y == i)) {
					iCurrentTexture = UtilsGL.setTexture(tileLivingsJobGroupON, iCurrentTexture);
					drawTile(tileLivingsJobGroupON, livingsGroupPanelFirstIconPoint.x,
							livingsGroupPanelFirstIconPoint.y + (i + 1) * livingsGroupPanelIconsSeparation, false);
				} else {
					// Miramos si el grupo tiene miembros
					if (Game.getWorld().getCitizenGroups().getGroup(i).getLivingIDs().size() > 0) {
						iCurrentTexture = UtilsGL.setTexture(tileLivingsJobGroupGreen, iCurrentTexture);
						drawTile(tileLivingsJobGroupGreen, livingsGroupPanelFirstIconPoint.x,
								livingsGroupPanelFirstIconPoint.y + (i + 1) * livingsGroupPanelIconsSeparation, false);
					} else {
						iCurrentTexture = UtilsGL.setTexture(tileLivingsJobGroup, iCurrentTexture);
						drawTile(tileLivingsJobGroup, livingsGroupPanelFirstIconPoint.x,
								livingsGroupPanelFirstIconPoint.y + (i + 1) * livingsGroupPanelIconsSeparation, false);
					}
				}
			}

			// Single group subpanel
			if (livingsPanelCitizensGroupActive != -1) {
				iCurrentTexture = UtilsGL.setTexture(tileLivingsGroupPanel[0], iCurrentTexture);
				renderBackground(tileLivingsGroupPanel, livingsSingleGroupPanelPoint, LIVINGS_PANEL_SINGLE_GROUP_WIDTH,
						LIVINGS_PANEL_SINGLE_GROUP_HEIGHT);

				// Botones
				if (mousePanel == MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_RENAME) {
					iCurrentTexture = UtilsGL.setTexture(tileLivingsSingleJobGroupRenameON, iCurrentTexture);
					drawTile(tileLivingsSingleJobGroupRenameON, livingsSingleGroupRenamePoint);
				} else {
					iCurrentTexture = UtilsGL.setTexture(tileLivingsSingleJobGroupRename, iCurrentTexture);
					drawTile(tileLivingsSingleJobGroupRename, livingsSingleGroupRenamePoint);
				}
				if (mousePanel == MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_CHANGE_JOBS) {
					iCurrentTexture = UtilsGL.setTexture(tileLivingsSingleGroupChangeJobsON, iCurrentTexture);
					drawTile(tileLivingsSingleGroupChangeJobsON, livingsSingleGroupChangeJobsPoint);
				} else {
					iCurrentTexture = UtilsGL.setTexture(tileLivingsSingleGroupChangeJobs, iCurrentTexture);
					drawTile(tileLivingsSingleGroupChangeJobs, livingsSingleGroupChangeJobsPoint);
				}
				if (mousePanel == MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_DISBAND) {
					iCurrentTexture = UtilsGL.setTexture(tileLivingsSingleJobGroupDisbandON, iCurrentTexture);
					drawTile(tileLivingsSingleJobGroupDisbandON, livingsSingleGroupDisbandPoint);
				} else {
					iCurrentTexture = UtilsGL.setTexture(tileLivingsSingleJobGroupDisband, iCurrentTexture);
					drawTile(tileLivingsSingleJobGroupDisband, livingsSingleGroupDisbandPoint);
				}
				if (mousePanel == MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_AUTOEQUIP) {
					iCurrentTexture = UtilsGL.setTexture(tileLivingsRowAutoequipON, iCurrentTexture);
					drawTile(tileLivingsRowAutoequipON, livingsSingleGroupAutoequipPoint);
				} else {
					iCurrentTexture = UtilsGL.setTexture(tileLivingsRowAutoequip, iCurrentTexture);
					drawTile(tileLivingsRowAutoequip, livingsSingleGroupAutoequipPoint);
				}

				// Text
				UtilsGL.glEnd();
				iCurrentTexture = Game.TEXTURE_FONT_ID;
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
				GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
				UtilsGL.glBegin(GL11.GL_QUADS);

				// Group name
				String sText = Game.getWorld().getCitizenGroups().getGroup(livingsPanelCitizensGroupActive).getName();
				UtilsGL.drawStringWithBorder(sText,
						livingsSingleGroupPanelPoint.x + LIVINGS_PANEL_SINGLE_GROUP_WIDTH / 2
								- UtilFont.getWidth(sText) / 2,
						livingsSingleGroupPanelPoint.y, ColorGL.WHITE, ColorGL.BLACK);
			}

			// Text
			if (iCurrentTexture != Game.TEXTURE_FONT_ID) {
				UtilsGL.glEnd();
				iCurrentTexture = Game.TEXTURE_FONT_ID;
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
				GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
				UtilsGL.glBegin(GL11.GL_QUADS);
			}

			// Group icons (1, 2, 3, 4, 5, 6, 7, 8)
			String sNumber;
			for (int i = 0; i < CitizenGroups.MAX_GROUPS; i++) {
				sNumber = Integer.toString(i + 1);
				UtilsGL.drawStringWithBorder(sNumber,
						livingsGroupPanelFirstIconPoint.x + tileLivingsGroup.getTileWidth() / 2
								- UtilFont.getWidth(sNumber) / 2,
						livingsGroupPanelFirstIconPoint.y + (i + 1) * livingsGroupPanelIconsSeparation
								+ tileLivingsGroup.getTileHeight() / 2 - UtilFont.MAX_HEIGHT / 2,
						ColorGL.WHITE, ColorGL.BLACK);
			}
		}

		// Restrict
		if ((getLivingsPanelActive() == LIVINGS_PANEL_TYPE_CITIZENS && livingsPanelCitizensGroupActive == -1)
				|| (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_HEROES)) {
			if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsRestriction()) {
				UtilsGL.setColorRed();
			}
			iCurrentTexture = UtilsGL.setTexture(tileIconLevelUp, iCurrentTexture);
			drawTile(tileIconLevelUp, livingsPanelIconRestrictUpPoint, ICON_WIDTH, ICON_HEIGHT,
					mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_UP);
			iCurrentTexture = UtilsGL.setTexture(tileIconLevelDown, iCurrentTexture);
			drawTile(tileIconLevelDown, livingsPanelIconRestrictDownPoint, ICON_WIDTH, ICON_HEIGHT,
					mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_DOWN);
			if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsRestriction()) {
				UtilsGL.unsetColor();
			}
		}

		ArrayList<Integer> alLivingIDs = getLivings();
		int iNumLivings;
		if (alLivingIDs != null) {
			iNumLivings = alLivingIDs.size();
		} else {
			iNumLivings = 0;
		}
		if (iNumLivings == 0) {
			UtilsGL.glEnd();

			String sText;
			if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_CITIZENS) {
				sText = Messages.getString("UIPanel.34"); //$NON-NLS-1$
			} else if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_SOLDIERS) {
				sText = Messages.getString("UIPanel.37"); //$NON-NLS-1$
			} else if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_HEROES) {
				sText = Messages.getString("UIPanel.38"); //$NON-NLS-1$
			} else {
				sText = Messages.getString("UIPanel.39"); //$NON-NLS-1$
			}

			int iTextWidth = UtilFont.getWidth(sText);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
			GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
			UtilsGL.glBegin(GL11.GL_QUADS);
			UtilsGL.drawStringWithBorder(sText, livingsPanelPoint.x + LIVINGS_PANEL_WIDTH / 2 - iTextWidth / 2,
					livingsPanelPoint.y + LIVINGS_PANEL_HEIGHT / 2 - UtilFont.MAX_HEIGHT / 2, ColorGL.ORANGE,
					ColorGL.BLACK);

			// Restrict
			if ((getLivingsPanelActive() == LIVINGS_PANEL_TYPE_CITIZENS && livingsPanelCitizensGroupActive == -1)
					|| (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_HEROES)) {
				int iLevel;
				if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_CITIZENS) {
					iLevel = Game.getWorld().getRestrictHaulEquippingLevel();
				} else {
					iLevel = Game.getWorld().getRestrictExploringLevel();
				}
				iLevel = World.MAP_NUM_LEVELS_OUTSIDE - iLevel;
				sText = Integer.toString(iLevel);
				UtilsGL.drawString(sText,
						(livingsPanelIconRestrictUpPoint.x + tileIconLevelUp.getTileWidth())
								+ ((livingsPanelIconRestrictDownPoint.x)
										- (livingsPanelIconRestrictUpPoint.x + tileIconLevelUp.getTileWidth())) / 2
								- UtilFont.getWidth(sText) / 2,
						livingsPanelIconRestrictUpPoint.y + tileIconLevelUp.getTileHeight() / 2
								- UtilFont.MAX_HEIGHT / 2,
						ColorGL.BLACK);
			}

			UtilsGL.glEnd();

			return;
		}

		// Num livigs > 0, comprobamos índices
		int iNumPages = (iNumLivings % LIVINGS_PANEL_MAX_ROWS == 0) ? iNumLivings / LIVINGS_PANEL_MAX_ROWS
				: (iNumLivings / LIVINGS_PANEL_MAX_ROWS) + 1;
		int iIndexPage;
		boolean bNoGroupsPanel = !checkGroupsPanelEnabled(getLivingsPanelActive());
		if (bNoGroupsPanel) {
			iIndexPage = livingsDataIndexPages[getLivingsPanelActive()];
		} else {
			if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_CITIZENS) {
				iIndexPage = livingsDataIndexPagesCitizenGroups[livingsPanelCitizensGroupActive];
			} else {
				iIndexPage = livingsDataIndexPagesSoldierGroups[livingsPanelSoldiersGroupActive];
			}
		}
		if (iIndexPage > iNumPages) {
			if (bNoGroupsPanel) {
				livingsDataIndexPages[getLivingsPanelActive()] = iNumPages;
			} else {
				if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_CITIZENS) {
					livingsDataIndexPagesCitizenGroups[livingsPanelCitizensGroupActive] = iNumPages;
				} else {
					livingsDataIndexPagesSoldierGroups[livingsPanelSoldiersGroupActive] = iNumPages;
				}
			}
			iIndexPage = iNumPages;
		} else if (iIndexPage < 1) {
			if (bNoGroupsPanel) {
				livingsDataIndexPages[getLivingsPanelActive()] = 1;
			} else {
				if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_CITIZENS) {
					livingsDataIndexPagesCitizenGroups[livingsPanelCitizensGroupActive] = 1;
				} else {
					livingsDataIndexPagesSoldierGroups[livingsPanelSoldiersGroupActive] = 1;
				}
			}
			iIndexPage = 1;
		}

		// Scrolls
		if (iIndexPage > 1) {
			iCurrentTexture = UtilsGL.setTexture(tileScrollUp, iCurrentTexture);
			drawTile(tileScrollUp, livingsPanelIconScrollUpPoint,
					(pItem != null && pItem.x == MOUSE_LIVINGS_PANEL_BUTTONS_SCROLL_UP));
		} else {
			iCurrentTexture = UtilsGL.setTexture(tileScrollUpDisabled, iCurrentTexture);
			drawTile(tileScrollUpDisabled, livingsPanelIconScrollUpPoint);
		}
		if (iIndexPage < iNumPages) {
			iCurrentTexture = UtilsGL.setTexture(tileScrollDown, iCurrentTexture);
			drawTile(tileScrollDown, livingsPanelIconScrollDownPoint,
					(pItem != null && pItem.x == MOUSE_LIVINGS_PANEL_BUTTONS_SCROLL_DOWN));
		} else {
			iCurrentTexture = UtilsGL.setTexture(tileScrollDownDisabled, iCurrentTexture);
			drawTile(tileScrollDownDisabled, livingsPanelIconScrollDownPoint);
		}

		// Rows + equipment
		// Livings pictures + military stuff + civ/soldier stuff
		int iMaxRows = Math.min(iNumLivings - ((iIndexPage - 1) * LIVINGS_PANEL_MAX_ROWS),
				livingsPanelRowPoints.length);
		iMaxRows = Math.min(iMaxRows, LIVINGS_PANEL_MAX_ROWS);

		iCurrentTexture = UtilsGL.setTexture(tileBottomItem, iCurrentTexture);
		for (int i = 0; i < iMaxRows; i++) {
			if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsLivings()) {
				UtilsGL.setColorRed();
			}
			drawTile(tileBottomItem, livingsPanelRowPoints[i]);
			if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsLivings()) {
				UtilsGL.unsetColor();
			}
			if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsBody()) {
				UtilsGL.setColorRed();
			}
			drawTile(tileBottomItem, livingsPanelRowHeadPoints[i]);
			drawTile(tileBottomItem, livingsPanelRowBodyPoints[i]);
			drawTile(tileBottomItem, livingsPanelRowLegsPoints[i]);
			drawTile(tileBottomItem, livingsPanelRowFeetPoints[i]);
			drawTile(tileBottomItem, livingsPanelRowWeaponPoints[i]);
			if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsBody()) {
				UtilsGL.unsetColor();
			}
		}
		LivingEntity le;
		int iIndex;
		for (int i = 0; i < iMaxRows; i++) {
			iIndex = ((iIndexPage - 1) * LIVINGS_PANEL_MAX_ROWS) + i;
			if (iIndex >= 0 && iIndex < alLivingIDs.size()) {
				// Living
				le = World.getLivingEntityByID(alLivingIDs.get(iIndex));

				iCurrentTexture = renderLiving(le,
						livingsPanelRowPoints[i].x + tileBottomItem.getTileWidth() / 2 - le.getTileWidth() / 2,
						livingsPanelRowPoints[i].y + tileBottomItem.getTileHeight() / 2 - le.getTileHeight() / 2,
						getLivingsPanelActive(), iCurrentTexture);

				EquippedData equippedData = le.getEquippedData();
				// Head
				if (equippedData.isWearing(MilitaryItem.LOCATION_HEAD)) {
					MilitaryItem mi = equippedData.getHead();
					iCurrentTexture = UtilsGL.setTexture(mi, iCurrentTexture);
					drawTile(mi,
							livingsPanelRowHeadPoints[i].x + tileBottomItem.getTileWidth() / 2 - mi.getTileWidth() / 2,
							livingsPanelRowHeadPoints[i].y + tileBottomItem.getTileHeight() / 2
									- mi.getTileHeight() / 2,
							false);
				} else {
					iCurrentTexture = UtilsGL.setTexture(tileLivingsPanelRowNoHead, iCurrentTexture);
					drawTile(tileLivingsPanelRowNoHead,
							livingsPanelRowHeadPoints[i].x + tileBottomItem.getTileWidth() / 2
									- tileLivingsPanelRowNoHead.getTileWidth() / 2,
							livingsPanelRowHeadPoints[i].y + tileBottomItem.getTileHeight() / 2
									- tileLivingsPanelRowNoHead.getTileHeight() / 2,
							false);
				}
				// Body
				if (equippedData.isWearing(MilitaryItem.LOCATION_BODY)) {
					MilitaryItem mi = equippedData.getBody();
					iCurrentTexture = UtilsGL.setTexture(mi, iCurrentTexture);
					drawTile(mi,
							livingsPanelRowBodyPoints[i].x + tileBottomItem.getTileWidth() / 2 - mi.getTileWidth() / 2,
							livingsPanelRowBodyPoints[i].y + tileBottomItem.getTileHeight() / 2
									- mi.getTileHeight() / 2,
							false);
				} else {
					iCurrentTexture = UtilsGL.setTexture(tileLivingsPanelRowNoBody, iCurrentTexture);
					drawTile(tileLivingsPanelRowNoBody,
							livingsPanelRowBodyPoints[i].x + tileBottomItem.getTileWidth() / 2
									- tileLivingsPanelRowNoBody.getTileWidth() / 2,
							livingsPanelRowBodyPoints[i].y + tileBottomItem.getTileHeight() / 2
									- tileLivingsPanelRowNoBody.getTileHeight() / 2,
							false);
				}
				// Legs
				if (equippedData.isWearing(MilitaryItem.LOCATION_LEGS)) {
					MilitaryItem mi = equippedData.getLegs();
					iCurrentTexture = UtilsGL.setTexture(mi, iCurrentTexture);
					drawTile(mi,
							livingsPanelRowLegsPoints[i].x + tileBottomItem.getTileWidth() / 2 - mi.getTileWidth() / 2,
							livingsPanelRowLegsPoints[i].y + tileBottomItem.getTileHeight() / 2
									- mi.getTileHeight() / 2,
							false);
				} else {
					iCurrentTexture = UtilsGL.setTexture(tileLivingsPanelRowNoLegs, iCurrentTexture);
					drawTile(tileLivingsPanelRowNoLegs,
							livingsPanelRowLegsPoints[i].x + tileBottomItem.getTileWidth() / 2
									- tileLivingsPanelRowNoLegs.getTileWidth() / 2,
							livingsPanelRowLegsPoints[i].y + tileBottomItem.getTileHeight() / 2
									- tileLivingsPanelRowNoLegs.getTileHeight() / 2,
							false);
				}
				// Feet
				if (equippedData.isWearing(MilitaryItem.LOCATION_FEET)) {
					MilitaryItem mi = equippedData.getFeet();
					iCurrentTexture = UtilsGL.setTexture(mi, iCurrentTexture);
					drawTile(mi,
							livingsPanelRowFeetPoints[i].x + tileBottomItem.getTileWidth() / 2 - mi.getTileWidth() / 2,
							livingsPanelRowFeetPoints[i].y + tileBottomItem.getTileHeight() / 2
									- mi.getTileHeight() / 2,
							false);
				} else {
					iCurrentTexture = UtilsGL.setTexture(tileLivingsPanelRowNoFeet, iCurrentTexture);
					drawTile(tileLivingsPanelRowNoFeet,
							livingsPanelRowFeetPoints[i].x + tileBottomItem.getTileWidth() / 2
									- tileLivingsPanelRowNoFeet.getTileWidth() / 2,
							livingsPanelRowFeetPoints[i].y + tileBottomItem.getTileHeight() / 2
									- tileLivingsPanelRowNoFeet.getTileHeight() / 2,
							false);
				}
				// Weapon
				if (equippedData.isWearing(MilitaryItem.LOCATION_WEAPON)) {
					MilitaryItem mi = equippedData.getWeapon();
					iCurrentTexture = UtilsGL.setTexture(mi, iCurrentTexture);
					drawTile(mi,
							livingsPanelRowWeaponPoints[i].x + tileBottomItem.getTileWidth() / 2
									- mi.getTileWidth() / 2,
							livingsPanelRowWeaponPoints[i].y + tileBottomItem.getTileHeight() / 2
									- mi.getTileHeight() / 2,
							false);
				} else {
					iCurrentTexture = UtilsGL.setTexture(tileLivingsPanelRowNoWeapon, iCurrentTexture);
					drawTile(tileLivingsPanelRowNoWeapon,
							livingsPanelRowWeaponPoints[i].x + tileBottomItem.getTileWidth() / 2
									- tileLivingsPanelRowNoWeapon.getTileWidth() / 2,
							livingsPanelRowWeaponPoints[i].y + tileBottomItem.getTileHeight() / 2
									- tileLivingsPanelRowNoWeapon.getTileHeight() / 2,
							false);
				}

				// Autoequip
				if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_CITIZENS
						|| getLivingsPanelActive() == LIVINGS_PANEL_TYPE_SOLDIERS) {
					if (pItem != null && pItem.x == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_AUTOEQUIP && pItem.y == i) {
						iCurrentTexture = UtilsGL.setTexture(tileLivingsRowAutoequipON, iCurrentTexture);
						if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsAutoequip()) {
							UtilsGL.setColorRed();
						}
						drawTile(tileLivingsRowAutoequipON, livingsPanelRowAutoequipPoints[i]);
						if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsAutoequip()) {
							UtilsGL.unsetColor();
						}
					} else {
						iCurrentTexture = UtilsGL.setTexture(tileLivingsRowAutoequip, iCurrentTexture);
						if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsAutoequip()) {
							UtilsGL.setColorRed();
						}
						drawTile(tileLivingsRowAutoequip, livingsPanelRowAutoequipPoints[i]);
						if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsAutoequip()) {
							UtilsGL.unsetColor();
						}
					}
				}

				// Civ/soldier stuff
				if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_CITIZENS) {
					if (pItem != null && pItem.x == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER && pItem.y == i) {
						iCurrentTexture = UtilsGL.setTexture(tileLivingsRowConvertSoldierON, iCurrentTexture);
						if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsConvertSoldier()) {
							UtilsGL.setColorRed();
						}
						drawTile(tileLivingsRowConvertSoldierON, livingsPanelRowConvertCivilianSoldierPoints[i]);
						if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsConvertSoldier()) {
							UtilsGL.unsetColor();
						}
					} else {
						iCurrentTexture = UtilsGL.setTexture(tileLivingsRowConvertSoldier, iCurrentTexture);
						if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsConvertSoldier()) {
							UtilsGL.setColorRed();
						}
						drawTile(tileLivingsRowConvertSoldier, livingsPanelRowConvertCivilianSoldierPoints[i]);
						if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsConvertSoldier()) {
							UtilsGL.unsetColor();
						}
					}
					if (livingsPanelCitizensGroupActive == -1) {
						if (pItem != null && pItem.x == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_PROFESSIONS && pItem.y == i) {
							iCurrentTexture = UtilsGL.setTexture(tileLivingsRowProfessionON, iCurrentTexture);
							if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsJobs()) {
								UtilsGL.setColorRed();
							}
							drawTile(tileLivingsRowProfessionON, livingsPanelRowProfessionPoints[i]);
							if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsJobs()) {
								UtilsGL.unsetColor();
							}
						} else {
							iCurrentTexture = UtilsGL.setTexture(tileLivingsRowProfession, iCurrentTexture);
							if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsJobs()) {
								UtilsGL.setColorRed();
							}
							drawTile(tileLivingsRowProfession, livingsPanelRowProfessionPoints[i]);
							if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsJobs()) {
								UtilsGL.unsetColor();
							}
						}
					}
					if (pItem != null && pItem.x == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_JOBS_GROUPS_ADDREMOVE
							&& pItem.y == i) {
						iCurrentTexture = UtilsGL.setTexture(tileLivingsRowJobsGroupsON, iCurrentTexture);
						if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsGroup()) {
							UtilsGL.setColorRed();
						}
						drawTile(tileLivingsRowJobsGroupsON, livingsPanelRowJobsGroupsPoints[i]);
						if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsGroup()) {
							UtilsGL.unsetColor();
						}
					} else {
						iCurrentTexture = UtilsGL.setTexture(tileLivingsRowJobsGroups, iCurrentTexture);
						if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsGroup()) {
							UtilsGL.setColorRed();
						}
						drawTile(tileLivingsRowJobsGroups, livingsPanelRowJobsGroupsPoints[i]);
						if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsGroup()) {
							UtilsGL.unsetColor();
						}
					}
				} else if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_SOLDIERS) {
					if (pItem != null && pItem.x == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_CIVILIAN && pItem.y == i) {
						iCurrentTexture = UtilsGL.setTexture(tileLivingsRowConvertCivilianON, iCurrentTexture);
						if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsConvertCivilian()) {
							UtilsGL.setColorRed();
						}
						drawTile(tileLivingsRowConvertCivilianON, livingsPanelRowConvertCivilianSoldierPoints[i]);
						if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsConvertCivilian()) {
							UtilsGL.unsetColor();
						}
					} else {
						iCurrentTexture = UtilsGL.setTexture(tileLivingsRowConvertCivilian, iCurrentTexture);
						if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsConvertCivilian()) {
							UtilsGL.setColorRed();
						}
						drawTile(tileLivingsRowConvertCivilian, livingsPanelRowConvertCivilianSoldierPoints[i]);
						if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsConvertCivilian()) {
							UtilsGL.unsetColor();
						}
					}

					// Soldier type
					Citizen soldier = ((Citizen) le);
					int soldierState = soldier.getSoldierData().getState();
					if (livingsPanelSoldiersGroupActive == -1) {
						if (soldierState == SoldierData.STATE_GUARD || (pItem != null
								&& pItem.x == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_GUARD && pItem.y == i)) {
							iCurrentTexture = UtilsGL.setTexture(tileLivingsRowConvertSoldierGuardON, iCurrentTexture);
							if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsGuard()) {
								UtilsGL.setColorRed();
							}
							drawTile(tileLivingsRowConvertSoldierGuardON, livingsPanelRowConvertSoldierGuardPoints[i]);
							if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsGuard()) {
								UtilsGL.unsetColor();
							}
						} else {
							iCurrentTexture = UtilsGL.setTexture(tileLivingsRowConvertSoldierGuard, iCurrentTexture);
							if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsGuard()) {
								UtilsGL.setColorRed();
							}
							drawTile(tileLivingsRowConvertSoldierGuard, livingsPanelRowConvertSoldierGuardPoints[i]);
							if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsGuard()) {
								UtilsGL.unsetColor();
							}
						}
						if (soldierState == SoldierData.STATE_PATROL
								|| (pItem != null && pItem.x == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_PATROL
										&& pItem.y == i)) {
							iCurrentTexture = UtilsGL.setTexture(tileLivingsRowConvertSoldierPatrolON, iCurrentTexture);
							if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsPatrol()) {
								UtilsGL.setColorRed();
							}
							drawTile(tileLivingsRowConvertSoldierPatrolON,
									livingsPanelRowConvertSoldierPatrolPoints[i]);
							if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsPatrol()) {
								UtilsGL.unsetColor();
							}
						} else {
							iCurrentTexture = UtilsGL.setTexture(tileLivingsRowConvertSoldierPatrol, iCurrentTexture);
							if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsPatrol()) {
								UtilsGL.setColorRed();
							}
							drawTile(tileLivingsRowConvertSoldierPatrol, livingsPanelRowConvertSoldierPatrolPoints[i]);
							if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsPatrol()) {
								UtilsGL.unsetColor();
							}
						}
						if (soldierState == SoldierData.STATE_BOSS_AROUND || (pItem != null
								&& pItem.x == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_BOSS && pItem.y == i)) {
							iCurrentTexture = UtilsGL.setTexture(tileLivingsRowConvertSoldierBossON, iCurrentTexture);
							if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsBoss()) {
								UtilsGL.setColorRed();
							}
							drawTile(tileLivingsRowConvertSoldierBossON, livingsPanelRowConvertSoldierBossPoints[i]);
							if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsBoss()) {
								UtilsGL.unsetColor();
							}
						} else {
							iCurrentTexture = UtilsGL.setTexture(tileLivingsRowConvertSoldierBoss, iCurrentTexture);
							if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsBoss()) {
								UtilsGL.setColorRed();
							}
							drawTile(tileLivingsRowConvertSoldierBoss, livingsPanelRowConvertSoldierBossPoints[i]);
							if (tutorialFlow != null && tutorialFlow.isBlinkMiniLivingsBoss()) {
								UtilsGL.unsetColor();
							}
						}

						if (soldierState == SoldierData.STATE_IN_A_GROUP || (pItem != null
								&& pItem.x == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_SGROUP_ADD && pItem.y == i)) {
							iCurrentTexture = UtilsGL.setTexture(tileLivingsRowGroupAddON, iCurrentTexture);
							drawTile(tileLivingsRowGroupAddON, livingsPanelRowGroupPoints[i]);
						} else {
							iCurrentTexture = UtilsGL.setTexture(tileLivingsRowGroupAdd, iCurrentTexture);
							drawTile(tileLivingsRowGroupAdd, livingsPanelRowGroupPoints[i]);
						}
					} else {
						if (pItem != null && pItem.x == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_SGROUP_REMOVE
								&& pItem.y == i) {
							iCurrentTexture = UtilsGL.setTexture(tileLivingsRowGroupRemoveON, iCurrentTexture);
							drawTile(tileLivingsRowGroupRemoveON, livingsPanelRowGroupPoints[i]);
						} else {
							iCurrentTexture = UtilsGL.setTexture(tileLivingsRowGroupRemove, iCurrentTexture);
							drawTile(tileLivingsRowGroupRemove, livingsPanelRowGroupPoints[i]);
						}
					}
				}
			}
		}

		UtilsGL.glEnd();

		// Text
		// Pages
		String sText = iIndexPage + " / " + iNumPages; //$NON-NLS-1$
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		UtilsGL.glBegin(GL11.GL_QUADS);
		UtilsGL.drawString(sText, livingsPanelPagesPoint.x - UtilFont.getWidth(sText) / 2, livingsPanelPagesPoint.y,
				ColorGL.BLACK);

		// Restrict
		if ((getLivingsPanelActive() == LIVINGS_PANEL_TYPE_CITIZENS && livingsPanelCitizensGroupActive == -1)
				|| (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_HEROES)) {
			int iLevel;
			if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_CITIZENS) {
				iLevel = Game.getWorld().getRestrictHaulEquippingLevel();
			} else {
				iLevel = Game.getWorld().getRestrictExploringLevel();
			}
			iLevel = World.MAP_NUM_LEVELS_OUTSIDE - iLevel;
			sText = Integer.toString(iLevel);
			UtilsGL.drawString(sText,
					(livingsPanelIconRestrictUpPoint.x + tileIconLevelUp.getTileWidth())
							+ ((livingsPanelIconRestrictDownPoint.x)
									- (livingsPanelIconRestrictUpPoint.x + tileIconLevelUp.getTileWidth())) / 2
							- UtilFont.getWidth(sText) / 2,
					livingsPanelIconRestrictUpPoint.y + tileIconLevelUp.getTileHeight() / 2 - UtilFont.MAX_HEIGHT / 2,
					ColorGL.BLACK);
		}

		UtilsGL.glEnd();
	}

	public int renderLiving(LivingEntity le, int x, int y, int iType, int iCurrentTexture) {
		// Render
		iCurrentTexture = UtilsGL.setTexture(le, iCurrentTexture);
		UtilsGL.drawTexture(x, y, x + le.getTileWidth(), y + le.getTileHeight(), le.getBaseTileSetTexX0(),
				le.getBaseTileSetTexY0(), le.getBaseTileSetTexX1(), le.getBaseTileSetTexY1());

		// Comprobamos que no tenga un effect de graphicchange
		boolean bGraphiChanged = false;
		for (int e = 0; e < le.getLivingEntityData().getEffects().size(); e++) {
			if (le.getLivingEntityData().getEffects().get(e).isGraphicChange()) {
				bGraphiChanged = true;
				break;
			}
		}

		if (!bGraphiChanged) {
			// Miramos si lleva algo equipado para dibujarlo
			if (iType == LIVINGS_PANEL_TYPE_CITIZENS || iType == LIVINGS_PANEL_TYPE_SOLDIERS) {
				EquippedData equippedData = le.getEquippedData();
				if (equippedData.isWearing(MilitaryItem.LOCATION_BODY)) {
					MilitaryItem mi = equippedData.getBody();
					iCurrentTexture = UtilsGL.setTexture(mi, iCurrentTexture);
					UtilsGL.drawTexture(x + le.getOffset_body_x(), y + le.getOffset_body_y(),
							x + le.getOffset_body_x() + mi.getTileWidth(),
							y + le.getOffset_body_y() + mi.getTileHeight(), mi.getBaseTileSetTexX0(),
							mi.getBaseTileSetTexY0(), mi.getBaseTileSetTexX1(), mi.getBaseTileSetTexY1());
				}
				if (equippedData.isWearing(MilitaryItem.LOCATION_HEAD)) {
					MilitaryItem mi = equippedData.getHead();
					iCurrentTexture = UtilsGL.setTexture(mi, iCurrentTexture);
					UtilsGL.drawTexture(x + le.getOffset_head_x(), y + le.getOffset_head_y(),
							x + le.getOffset_head_x() + mi.getTileWidth(),
							y + le.getOffset_head_y() + mi.getTileHeight(), mi.getBaseTileSetTexX0(),
							mi.getBaseTileSetTexY0(), mi.getBaseTileSetTexX1(), mi.getBaseTileSetTexY1());
				}
				if (equippedData.isWearing(MilitaryItem.LOCATION_FEET)) {
					MilitaryItem mi = equippedData.getFeet();
					iCurrentTexture = UtilsGL.setTexture(mi, iCurrentTexture);
					UtilsGL.drawTexture(x + le.getOffset_feet_x(), y + le.getOffset_feet_y(),
							x + le.getOffset_feet_x() + mi.getTileWidth(),
							y + le.getOffset_feet_y() + mi.getTileHeight(), mi.getBaseTileSetTexX0(),
							mi.getBaseTileSetTexY0(), mi.getBaseTileSetTexX1(), mi.getBaseTileSetTexY1());
				}
				if (equippedData.isWearing(MilitaryItem.LOCATION_LEGS)) {
					MilitaryItem mi = equippedData.getLegs();
					iCurrentTexture = UtilsGL.setTexture(mi, iCurrentTexture);
					UtilsGL.drawTexture(x + le.getOffset_legs_x(), y + le.getOffset_legs_y(),
							x + le.getOffset_legs_x() + mi.getTileWidth(),
							y + le.getOffset_legs_y() + mi.getTileHeight(), mi.getBaseTileSetTexX0(),
							mi.getBaseTileSetTexY0(), mi.getBaseTileSetTexX1(), mi.getBaseTileSetTexY1());
				}
				if (equippedData.isWearing(MilitaryItem.LOCATION_WEAPON)) {
					MilitaryItem mi = equippedData.getWeapon();
					iCurrentTexture = UtilsGL.setTexture(mi, iCurrentTexture);
					UtilsGL.drawTexture(x + le.getOffset_weapon_x(), y + le.getOffset_weapon_y(),
							x + le.getOffset_weapon_x() + mi.getTileWidth(),
							y + le.getOffset_weapon_y() + mi.getTileHeight(), mi.getBaseTileSetTexX0(),
							mi.getBaseTileSetTexY0(), mi.getBaseTileSetTexX1(), mi.getBaseTileSetTexY1());
				}
			}
		}

		return iCurrentTexture;
	}

	public void renderPrioritiesPanel(int mouseX, int mouseY, int mousePanel) {
		// XAVI GL11.glColor4f (1, 1, 1, 1);
		int iCurrentTexture = tilePrioritiesPanel[0].getTextureID();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, iCurrentTexture);
		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		UtilsGL.glBegin(GL11.GL_QUADS);
		renderBackground(tilePrioritiesPanel, prioritiesPanelPoint, PRIORITIES_PANEL_WIDTH, PRIORITIES_PANEL_HEIGHT);

		// Items
		Point point;
		Point pItem;
		if (mousePanel == MOUSE_PRIORITIES_PANEL_ITEMS || mousePanel == MOUSE_PRIORITIES_PANEL_ITEMS_UP
				|| mousePanel == MOUSE_PRIORITIES_PANEL_ITEMS_DOWN) {
			pItem = isMouseOnPrioritiesItems(mouseX, mouseY);
		} else {
			pItem = null;
		}

		for (int i = 0; i < PRIORITIES_PANEL_NUM_ITEMS; i++) {
			point = prioritiesPanelItemsPosition.get(i);
			// Round button
			iCurrentTexture = UtilsGL.setTexture(tileBottomItem, iCurrentTexture);
			drawTile(tileBottomItem, point, PRIORITIES_PANEL_ITEM_SIZE, PRIORITIES_PANEL_ITEM_SIZE,
					(pItem != null && pItem.x == MOUSE_PRIORITIES_PANEL_ITEMS && pItem.y == i));

			// Icono
			if (i < (PRIORITIES_PANEL_NUM_ITEMS - 1)) {
				// Icono de UI que toque
				Tile tile = ActionPriorityManager.getItem(ActionPriorityManager.getPrioritiesList().get(i)).getIcon();
				iCurrentTexture = UtilsGL.setTexture(tile, iCurrentTexture);
				drawTile(tile, point, PRIORITIES_PANEL_ITEM_SIZE, PRIORITIES_PANEL_ITEM_SIZE,
						(pItem != null && pItem.x == MOUSE_PRIORITIES_PANEL_ITEMS && pItem.y == i));
			} else {
				// Back
				iCurrentTexture = UtilsGL.setTexture(BACK_TILE, iCurrentTexture);
				drawTile(BACK_TILE, point, PRIORITIES_PANEL_ITEM_SIZE, PRIORITIES_PANEL_ITEM_SIZE,
						(pItem != null && pItem.x == MOUSE_PRODUCTION_PANEL_ITEMS && pItem.y == i));
			}

			point = prioritiesPanelItemsUpPosition.get(i);
			if (point.x != -1) {
				// Up
				iCurrentTexture = UtilsGL.setTexture(tilePrioritiesPanelUpIcon, iCurrentTexture);
				drawTile(tilePrioritiesPanelUpIcon, point, ICON_WIDTH, ICON_HEIGHT,
						(pItem != null && pItem.x == MOUSE_PRIORITIES_PANEL_ITEMS_UP && pItem.y == i));
			}
			point = prioritiesPanelItemsDownPosition.get(i);
			if (point.x != -1) {
				// Down
				iCurrentTexture = UtilsGL.setTexture(tilePrioritiesPanelDownIcon, iCurrentTexture);
				drawTile(tilePrioritiesPanelDownIcon, point, ICON_WIDTH, ICON_HEIGHT,
						(pItem != null && pItem.x == MOUSE_PRIORITIES_PANEL_ITEMS_DOWN && pItem.y == i));
			}
		}
		UtilsGL.glEnd();

		// Render priority numbers
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		UtilsGL.glBegin(GL11.GL_QUADS);
		for (int i = 0; i < PRIORITIES_PANEL_NUM_ITEMS - 1; i++) {
			point = prioritiesPanelItemsPosition.get(i);
			UtilsGL.drawStringWithBorder(Integer.toString((i + 1)), point.x, point.y, ColorGL.WHITE, ColorGL.BLACK);
		}
		UtilsGL.glEnd();
	}

	/**
	 * Draws the current task
	 * 
	 * @param x
	 * @param y
	 * @param mousePanel
	 */
	public void renderTask() {
		if (Game.getCurrentState() != Game.STATE_CREATING_TASK) {
			return;
		}

		Task task = Game.getCurrentTask();
		if (task == null) {
			return;
		}

		String taskString = task.toString();
		int taskX = messageIconPoints[0].x;
		int taskY = messageIconPoints[0].y + messageTiles[0].getTileHeight() + PIXELS_TO_BORDER;
		int taskWidth = UtilFont.getWidth(taskString);
		int taskHeight = UtilFont.MAX_HEIGHT;

		// Render del icono de la tarea
		if (Game.getCurrentState() == Game.STATE_CREATING_TASK) {
			Tile tile = Game.getCurrentTask().getTile();
			if (tile != null) {
				// XAVI GL11.glColor4f (1, 1, 1, 1);

				// Round button
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, tileBottomItem.getTextureID());
				GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
				UtilsGL.glBegin(GL11.GL_QUADS);
				drawTile(tileBottomItem, taskX, taskY, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT, false);
				UtilsGL.glEnd();

				// Icon
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, tile.getTextureID());
				GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
				UtilsGL.glBegin(GL11.GL_QUADS);
				drawTile(tile, taskX, taskY, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT, false);
				UtilsGL.glEnd();

				taskX += (BOTTOM_ITEM_WIDTH + PIXELS_TO_BORDER);
				taskY += (BOTTOM_ITEM_HEIGHT / 4);
			}
		}

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, BLACK_TILE.getTextureID());
		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		UtilsGL.glBegin(GL11.GL_QUADS);
		UtilsGL.drawTexture(taskX, taskY, taskX + taskWidth + 2, taskY + taskHeight + 2,
				BLACK_TILE.getTileSetTexX0(), BLACK_TILE.getTileSetTexY0(),
				BLACK_TILE.getTileSetTexX1(), BLACK_TILE.getTileSetTexY1());
		UtilsGL.glEnd();

		// Texto
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		UtilsGL.glBegin(GL11.GL_QUADS);
		UtilsGL.drawString(taskString, taskX + 1, taskY + 1);
		UtilsGL.glEnd();
	}

	/**
	 * Draws the tutorial button if it is enabled
	 * 
	 * @param mousePanel
	 */
	public static void renderTutorialButton(int mousePanel, boolean bCheckBlink) {
		if (Game.getCurrentMissionData() == null || Game.getCurrentMissionData().getTutorialFlows().size() == 0) {
			return;
		}

		// Render del icono del tutorial
		// Round button
		int iCurrentTexture = tileBottomItem.getTextureID();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, iCurrentTexture);
		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		UtilsGL.glBegin(GL11.GL_QUADS);
		if (bCheckBlink) {
			if (imagesPanel == null || (ImagesPanel.getCurrentFlowIndex() == 0 && !ImagesPanel.isVisible())) {
				UtilsGL.setColorRed();
			}
		}
		drawTile(tileBottomItem, iconTutorialPoint.x, iconTutorialPoint.y, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT,
				(mousePanel == MOUSE_TUTORIAL_ICON));
		if (bCheckBlink) {
			if (imagesPanel == null || (ImagesPanel.getCurrentFlowIndex() == 0 && !ImagesPanel.isVisible())) {
				UtilsGL.unsetColor();
			}
		}

		// Icon
		iCurrentTexture = UtilsGL.setTexture(iCurrentTexture, tileIconTutorial.getTextureID());
		drawTile(tileIconTutorial, iconTutorialPoint.x, iconTutorialPoint.y, BOTTOM_ITEM_WIDTH, BOTTOM_ITEM_HEIGHT,
				(mousePanel == MOUSE_TUTORIAL_ICON));

		UtilsGL.glEnd();
	}

	public static void drawTile(Tile tile, Point point, boolean bigger) {
		drawTile(tile, point, tile.getTileWidth(), tile.getTileHeight(), bigger);
	}

	/**
	 * Draws a tile
	 * 
	 * @param tile   Tile
	 * @param point  Coordinates
	 * @param width  Base width
	 * @param height Base height
	 * @param bigger Make it bigger?
	 */
	public static void drawTile(Tile tile, Point point, int width, int height, boolean bigger) {
		int iTemp = (width - tile.getTileWidth()) / 2;
		int iTemp2 = (height - tile.getTileHeight()) / 2;

		if (bigger) {
			UtilsGL.drawTexture(point.x - (tile.getTileWidth() / 4) + iTemp,
					point.y - (tile.getTileHeight() / 4) + iTemp2,
					point.x + tile.getTileWidth() + (tile.getTileWidth() / 4) + iTemp,
					point.y + tile.getTileHeight() + (tile.getTileHeight() / 4) + iTemp2, tile.getTileSetTexX0(),
					tile.getTileSetTexY0(), tile.getTileSetTexX1(), tile.getTileSetTexY1());
		} else {
			UtilsGL.drawTexture(point.x + iTemp, point.y + iTemp2, point.x + tile.getTileWidth() + iTemp,
					point.y + tile.getTileHeight() + iTemp2, tile.getTileSetTexX0(), tile.getTileSetTexY0(),
					tile.getTileSetTexX1(), tile.getTileSetTexY1());
		}
	}

	public static void drawTile(Tile tile, int pointX, int pointY, int width, int height, boolean bigger) {
		int iTemp = (width - tile.getTileWidth()) / 2;
		int iTemp2 = (height - tile.getTileHeight()) / 2;

		if (bigger) {
			UtilsGL.drawTexture(pointX - (tile.getTileWidth() / 4) + iTemp,
					pointY - (tile.getTileHeight() / 4) + iTemp2,
					pointX + tile.getTileWidth() + (tile.getTileWidth() / 4) + iTemp,
					pointY + tile.getTileHeight() + (tile.getTileHeight() / 4) + iTemp2, tile.getTileSetTexX0(),
					tile.getTileSetTexY0(), tile.getTileSetTexX1(), tile.getTileSetTexY1());
		} else {
			UtilsGL.drawTexture(pointX + iTemp, pointY + iTemp2, pointX + tile.getTileWidth() + iTemp,
					pointY + tile.getTileHeight() + iTemp2, tile.getTileSetTexX0(), tile.getTileSetTexY0(),
					tile.getTileSetTexX1(), tile.getTileSetTexY1());
		}
	}

	public static void drawTile(Tile tile, Point point) {
		UtilsGL.drawTexture(point.x, point.y, point.x + tile.getTileWidth(), point.y + tile.getTileHeight(),
				tile.getTileSetTexX0(), tile.getTileSetTexY0(), tile.getTileSetTexX1(), tile.getTileSetTexY1());
	}

	public static void drawTile(Tile tile, int x, int y, boolean bigger) {
		drawTile(tile, x, y, tile.getTileWidth(), tile.getTileHeight(), bigger);
	}

	/**
	 * Cierra los menús que no están locked indicados
	 * 
	 * @param bottom
	 * @param right
	 * @param production
	 */
	public static void closeNonLockedMenus(boolean bottom, boolean right, boolean production) {
		if (bottom && !isBottomMenuPanelLocked()) {
			setBottomMenuPanelActive(false);
		}
		// if (right && !isMenuPanelLocked()) {
		// 	setMenuPanelActive(false);
		// }
		if (production && !isProductionPanelLocked()) {
			setProductionPanelActive(false);
		}
	}

	/**
	 * Retorna una lista de IDs de livings a partir de lo que esté mostrando el
	 * living panel
	 * 
	 * @return
	 */
	public static ArrayList<Integer> getLivings() {
		if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_CITIZENS) {
			if (livingsPanelCitizensGroupActive == -1) {
				// Citizens sin grupo
				return Game.getWorld().getCitizenGroups().getCitizensWithoutGroup();
			} else {
				// Está mostrando un grupo, miramos los miembros que tiene
				if (livingsPanelCitizensGroupActive >= 0
						&& livingsPanelCitizensGroupActive < CitizenGroups.MAX_GROUPS) {
					return Game.getWorld().getCitizenGroups().getGroup(livingsPanelCitizensGroupActive).getLivingIDs();
				}
			}

			return World.getCitizenIDs();
		} else if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_SOLDIERS) {
			if (livingsPanelSoldiersGroupActive == -1) {
				// Soldiers sin grupo
				return Game.getWorld().getSoldierGroups().getSoldiersWithoutGroup();
			} else {
				// Está mostrando un grupo, miramos los miembros que tiene
				if (livingsPanelSoldiersGroupActive >= 0
						&& livingsPanelSoldiersGroupActive < SoldierGroups.MAX_GROUPS) {
					return Game.getWorld().getSoldierGroups().getGroup(livingsPanelSoldiersGroupActive).getLivingIDs();
				}
			}
		} else if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_HEROES) {
			return World.getHeroIDs();
		}

		return null;
	}

	/**
	 * Retorna la primera posición de la página que esté mostrando el living panel
	 * 
	 * @return
	 */
	public static int getLivingsIndex() {
		if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_CITIZENS) {
			if (livingsPanelCitizensGroupActive == -1) {
				// Todos los citizens
				return (livingsDataIndexPages[LIVINGS_PANEL_TYPE_CITIZENS] - 1) * LIVINGS_PANEL_MAX_ROWS;
			} else {
				// Está mostrando un grupo, miramos los miembros que tiene
				return (livingsDataIndexPagesCitizenGroups[livingsPanelCitizensGroupActive] - 1)
						* LIVINGS_PANEL_MAX_ROWS;
			}
		} else if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_SOLDIERS) {
			if (livingsPanelSoldiersGroupActive == -1) {
				// Todos los soldiers
				return (livingsDataIndexPages[LIVINGS_PANEL_TYPE_SOLDIERS] - 1) * LIVINGS_PANEL_MAX_ROWS;
			} else {
				// Está mostrando un grupo, miramos los miembros que tiene
				return (livingsDataIndexPagesSoldierGroups[livingsPanelSoldiersGroupActive] - 1)
						* LIVINGS_PANEL_MAX_ROWS;
			}
		} else if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_HEROES) {
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

	/**
	 * Key pressed. Mostly because the ESC key to close the panels
	 * 
	 * @param tecla
	 * @return true if something is done
	 */

	public static int getImagesPanelOffset() {
		if (isProductionPanelActive()) {
			return productionPanelPoint.x + PRODUCTION_PANEL_WIDTH;
		}

		return 0;
	}

	public static void toggleTutorialPanel(boolean bNewGame) {
		if (Game.getCurrentMissionData() != null && Game.getCurrentMissionData().getTutorialFlows().size() > 0) {
			if (imagesPanel == null) {
				imagesPanel = new ImagesPanel(MainPanel.renderWidth, MainPanel.renderHeight,
						Game.getCurrentMissionData());
				if (bNewGame) {
					ImagesPanel.setCurrentFlowIndex(0);
				} else {
					ImagesPanel.setCurrentFlowIndex(Game.getCurrentMissionData().getTutorialFlowIndex());
				}
				ImagesPanel.setMaxFlowIndex(Game.getCurrentMissionData().getTutorialFlowIndex());
				ImagesPanel.setVisible(true);
			} else {
				ImagesPanel.setVisible(!ImagesPanel.isVisible());
			}
		}
	}

	public static void createMilitaryContextMenu(SmartMenu smToAdd, int iLocation, LivingEntity le, int mouseX, int mouseY) {
		// Equipar, miramos si hay objetos militares en el mundo, de paso ya hacemos una
		// lista para poner en el menú
		Integer[] aItems = World.getItems().keySet().toArray(new Integer[0]);
		ArrayList<MilitaryItem> alMilitaryItems = new ArrayList<MilitaryItem>();

		int iASZID = World.getCell(le.getCoordinates()).getAstarZoneID();

		ItemManagerItem imi;
		Item mi;
		for (int i = 0; i < aItems.length; i++) {
			mi = World.getItems().get(aItems[i]);
			if (mi != null && mi instanceof MilitaryItem) {
				if (World.getCell(mi.getCoordinates()).getAstarZoneID() == iASZID) {
					imi = ItemManager.getItem(mi.getIniHeader());
					if (imi.getLocation() == iLocation) {
						// Lo metemos en la posición correcta, ordenado por item level
						int iItemLevel = imi.getLevel();
						int iIndexLevel = -1;
						for (int iL = 0; iL < alMilitaryItems.size(); iL++) {
							imi = ItemManager.getItem(alMilitaryItems.get(iL).getIniHeader());
							if (imi.getLevel() <= iItemLevel) {
								// Bingo
								iIndexLevel = iL;
								break;
							}
						}

						if (iIndexLevel == -1) {
							alMilitaryItems.add((MilitaryItem) mi);
						} else {
							alMilitaryItems.add(iIndexLevel, (MilitaryItem) mi);
						}
					}
				}
			}
		}

		// Containers
		ArrayList<Container> alContainers = Game.getWorld().getContainers();
		ArrayList<Item> alContainerItems;
		nextContainer: for (int i = 0; i < alContainers.size(); i++) {
			alContainerItems = alContainers.get(i).getItemsInside();
			for (int j = 0; j < alContainerItems.size(); j++) {
				mi = alContainerItems.get(j);
				if (World.getCell(mi.getCoordinates()).getAstarZoneID() != iASZID) {
					continue nextContainer;
				}

				if (mi != null && mi instanceof MilitaryItem) {
					imi = ItemManager.getItem(mi.getIniHeader());
					if (imi.getLocation() == iLocation) {
						// Lo metemos en la posición correcta, ordenado por item level
						int iItemLevel = imi.getLevel();
						int iIndexLevel = -1;
						for (int iL = 0; iL < alMilitaryItems.size(); iL++) {
							imi = ItemManager.getItem(alMilitaryItems.get(iL).getIniHeader());
							if (imi.getLevel() <= iItemLevel) {
								// Bingo
								iIndexLevel = iL;
								break;
							}
						}

						if (iIndexLevel == -1) {
							alMilitaryItems.add((MilitaryItem) mi);
						} else {
							alMilitaryItems.add(iIndexLevel, (MilitaryItem) mi);
						}
					}
				}
			}
		}

		if (Game.getCurrentState() == Game.STATE_CREATING_TASK) {
			Game.deleteCurrentTask();
		}
		ContextMenu menuMilitary = new ContextMenu();
		SmartMenu smMilitary = new SmartMenu();

		if (alMilitaryItems.size() > 0 || smToAdd != null) {
			// Tenemos la lista con items que el aldeano puede equipar, creamos el menú
			if (smToAdd != null) {
				smMilitary.addItem(smToAdd);
				if (alMilitaryItems.size() > 0) {
					smMilitary.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
				}
			}

			// Ordenamos el menú por item level
			MilitaryItem militaryItem;
			for (int i = 0; i < alMilitaryItems.size(); i++) {
				militaryItem = alMilitaryItems.get(i);
				if (militaryItem.getZ() > Game.getWorld().getRestrictHaulEquippingLevel()) {
					smMilitary.addItem(new SmartMenu(SmartMenu.TYPE_ITEM,
							Messages.getString("UIPanel.79") + militaryItem.getExtendedTilename(), null, //$NON-NLS-1$
							CommandPanel.COMMAND_WEAR, Integer.toString(le.getID()),
							Integer.toString(militaryItem.getID()), militaryItem.getCoordinates().toPoint3D(),
							militaryItem.getItemTextColor()));
				} else {
					smMilitary.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, militaryItem.getExtendedTilename(), null,
							CommandPanel.COMMAND_WEAR, Integer.toString(le.getID()),
							Integer.toString(militaryItem.getID()), militaryItem.getCoordinates().toPoint3D(),
							militaryItem.getItemTextColor()));
				}
			}
		} else {
			smMilitary.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("UIPanel.56"), null, null, null)); //$NON-NLS-1$
		}
		menuMilitary.setSmartMenu(smMilitary);
		menuMilitary.setX(mouseX + 16 + -menuMilitary.getWidth() / 2);
		menuMilitary.setY(mouseY + 32);
		menuMilitary.resize();
		Game.setContextMenu(menuMilitary);
	}

	public static void closePanels(boolean bPriorities, boolean bTrade, boolean bMessages, boolean bMats,
			boolean bLivings, boolean bPile, boolean bProfessions) {
		if (bPriorities) {
			setPrioritiesPanelActive(false);
		}
		if (bTrade) {
			setTradePanelActive(false);
		}
		if (bMessages) {
			setMessagesPanelActive(-1);
		}
		if (bMats) {
			setMatsPanelActive(false);
		}
		if (bLivings) {
			setLivingsPanelActive(LIVINGS_PANEL_TYPE_NONE, livingsPanelSoldiersGroupActive,
					livingsPanelCitizensGroupActive);
		}
		if (bPile) {
			setPilePanelActive(-1, false);
		}
		if (bProfessions) {
			setProfessionsPanelActive(-1, false);
		}
	}

	public static void createBottomSubPanel(SmartMenu smItem) {
		int iMaxItems = smItem.getItems().size();
		BOTTOM_SUBPANEL_WIDTH = (menuPanelPoint.x - bottomPanelX) - 2 * PIXELS_TO_BORDER;

		BOTTOM_SUBPANEL_NUM_ITEMS_X = (BOTTOM_SUBPANEL_WIDTH - PIXELS_TO_BORDER)
				/ (BOTTOM_SUBITEM_WIDTH + PIXELS_TO_BORDER);
		if (BOTTOM_SUBPANEL_NUM_ITEMS_X < 1) {
			BOTTOM_SUBPANEL_NUM_ITEMS_X = 1;
		} else if (BOTTOM_SUBPANEL_NUM_ITEMS_X > iMaxItems) {
			BOTTOM_SUBPANEL_NUM_ITEMS_X = iMaxItems;
		}
		BOTTOM_SUBPANEL_WIDTH = BOTTOM_SUBPANEL_NUM_ITEMS_X * (BOTTOM_SUBITEM_WIDTH + PIXELS_TO_BORDER)
				+ PIXELS_TO_BORDER;

		BOTTOM_SUBPANEL_NUM_ITEMS_Y = iMaxItems / BOTTOM_SUBPANEL_NUM_ITEMS_X;
		if (iMaxItems % BOTTOM_SUBPANEL_NUM_ITEMS_X != 0) {
			BOTTOM_SUBPANEL_NUM_ITEMS_Y++;
		}
		BOTTOM_SUBPANEL_HEIGHT = BOTTOM_SUBPANEL_NUM_ITEMS_Y * (BOTTOM_SUBITEM_HEIGHT + PIXELS_TO_BORDER)
				+ PIXELS_TO_BORDER;

		bottomSubPanelPoint.setLocation(bottomPanelX, bottomPanelY - PIXELS_TO_BORDER - BOTTOM_SUBPANEL_HEIGHT);
		bottomSubPanelItemsPosition = new ArrayList<Point>();
		bucle1: for (int y1 = 0; y1 < BOTTOM_SUBPANEL_NUM_ITEMS_Y; y1++) {
			for (int x1 = 0; x1 < BOTTOM_SUBPANEL_NUM_ITEMS_X; x1++) {
				if ((y1 * BOTTOM_SUBPANEL_NUM_ITEMS_X + x1) < smItem.getItems().size()) {
					bottomSubPanelItemsPosition.add(new Point(
							bottomSubPanelPoint.x + PIXELS_TO_BORDER + (x1 * (BOTTOM_SUBITEM_WIDTH + PIXELS_TO_BORDER)),
							bottomSubPanelPoint.y + PIXELS_TO_BORDER
									+ (y1 * (BOTTOM_SUBITEM_HEIGHT + PIXELS_TO_BORDER))));
				} else {
					break bucle1;
				}
			}
		}

		createProductionPanel(productionPanelMenu);
	}

	public static void createMenuPanel(SmartMenu menu) {
		MENU_PANEL_HEIGHT = renderHeight - (minimapPanelY + MINIMAP_PANEL_HEIGHT + 2 * PIXELS_TO_BORDER)
				- BOTTOM_PANEL_HEIGHT - 2 * PIXELS_TO_BORDER;
		MENU_PANEL_NUM_ITEMS_Y = (MENU_PANEL_HEIGHT - PIXELS_TO_BORDER) / (MENU_ITEM_HEIGHT + PIXELS_TO_BORDER);
		if (MENU_PANEL_NUM_ITEMS_Y < 1) {
			MENU_PANEL_NUM_ITEMS_Y = 1;
		}
		MENU_PANEL_HEIGHT = MENU_PANEL_NUM_ITEMS_Y * (MENU_ITEM_HEIGHT + PIXELS_TO_BORDER) + PIXELS_TO_BORDER;

		int iMaxItems = menu.getItems().size();
		MENU_PANEL_NUM_ITEMS_X = (iMaxItems / MENU_PANEL_NUM_ITEMS_Y);
		if ((iMaxItems % MENU_PANEL_NUM_ITEMS_Y) != 0) {
			MENU_PANEL_NUM_ITEMS_X++;
		}
		MENU_PANEL_WIDTH = MENU_PANEL_NUM_ITEMS_X * (MENU_ITEM_WIDTH + PIXELS_TO_BORDER) + PIXELS_TO_BORDER;

		while (((MENU_PANEL_NUM_ITEMS_Y - 1) * MENU_PANEL_NUM_ITEMS_X) >= iMaxItems) {
			MENU_PANEL_HEIGHT -= (MENU_ITEM_HEIGHT + PIXELS_TO_BORDER);
			MENU_PANEL_NUM_ITEMS_Y--;
		}

		menuPanelPoint.setLocation(renderWidth - MENU_PANEL_WIDTH - tileOpenRightMenu.getTileWidth(),
				minimapPanelY + MINIMAP_PANEL_HEIGHT + 2 * PIXELS_TO_BORDER);

		// Positions
		menuPanelItemsPosition = new ArrayList<Point>();
		for (int y = 0; y < MENU_PANEL_NUM_ITEMS_Y; y++) {
			for (int x = 0; x < MENU_PANEL_NUM_ITEMS_X; x++) {
				menuPanelItemsPosition
						.add(new Point(menuPanelPoint.x + PIXELS_TO_BORDER + (x * (MENU_ITEM_WIDTH + PIXELS_TO_BORDER)),
								menuPanelPoint.y + PIXELS_TO_BORDER + (y * (MENU_ITEM_HEIGHT + PIXELS_TO_BORDER))));
			}
		}

		// Minibotón para abrir/cerrar el menú
		tileOpenCloseRightMenuPoint.setLocation(renderWidth - tileOpenRightMenu.getTileWidth(),
				renderHeight / 2 - tileOpenRightMenu.getTileHeight() / 2);
	}

	public static boolean isBottomMenuPanelActive() {
		return bottomMenuPanelActive;
	}

	public static void setBottomMenuPanelActive(boolean bottomMenuPanelActive) {
		setBottomMenuPanelActive(bottomMenuPanelActive, false);
	}

	public static void setBottomMenuPanelActive(boolean MenuPanelActive, boolean bInitializing) {
		bottomMenuPanelActive = MenuPanelActive;
		if (!bInitializing) {
			createProductionPanel(productionPanelMenu);
		}
	}

	public static void setBottomMenuPanelLocked(boolean MenuPanelLocked) {
		bottomMenuPanelLocked = MenuPanelLocked;
	}

	public static boolean isBottomMenuPanelLocked() {
		return bottomMenuPanelLocked;
	}

	public static boolean isMenuPanelActive() {
		return menuPanelActive;
	}

	public static void setMenuPanelActive(boolean PanelActive) {
		menuPanelActive = PanelActive;
	}

	public static void setMenuPanelLocked(boolean PanelLocked) {
		menuPanelLocked = PanelLocked;
	}

	public static boolean isMenuPanelLocked() {
		return menuPanelLocked;
	}

	public static void createProductionPanel(SmartMenu menu) {
		int iFirstY = iconNumCitizensBackgroundPoint.y + tileBottomItem.getTileHeight()
				+ tileBottomItem.getTileHeight() / 4;
		int iLastY;
		if (bottomSubPanelMenu != null && isBottomMenuPanelActive()) {
			iLastY = bottomSubPanelPoint.y;
		} else {
			iLastY = bottomPanelY;
		}
		PRODUCTION_PANEL_HEIGHT = iLastY - iFirstY - PIXELS_TO_BORDER * 2;

		PRODUCTION_PANEL_WIDTH = menuPanelPoint.x - PIXELS_TO_BORDER * 4;

		PRODUCTION_PANEL_NUM_ITEMS_Y = (PRODUCTION_PANEL_HEIGHT - PIXELS_TO_BORDER)
				/ (PRODUCTION_PANEL_ITEM_HEIGHT + PIXELS_TO_BORDER);
		if (PRODUCTION_PANEL_NUM_ITEMS_Y < 1) {
			PRODUCTION_PANEL_NUM_ITEMS_Y = 1;
		}
		PRODUCTION_PANEL_HEIGHT = PRODUCTION_PANEL_NUM_ITEMS_Y * (PRODUCTION_PANEL_ITEM_HEIGHT + PIXELS_TO_BORDER)
				+ PIXELS_TO_BORDER;

		int iMaxItems = menu.getItems().size();
		PRODUCTION_PANEL_NUM_ITEMS_X = (iMaxItems / PRODUCTION_PANEL_NUM_ITEMS_Y);
		if ((iMaxItems % PRODUCTION_PANEL_NUM_ITEMS_Y) != 0) {
			PRODUCTION_PANEL_NUM_ITEMS_X++;
		}
		PRODUCTION_PANEL_WIDTH = PRODUCTION_PANEL_NUM_ITEMS_X
				* (PRODUCTION_PANEL_ITEM_WIDTH + PIXELS_TO_BORDER + 2 * ICON_WIDTH) + PIXELS_TO_BORDER;

		while (((PRODUCTION_PANEL_NUM_ITEMS_Y - 1) * PRODUCTION_PANEL_NUM_ITEMS_X) >= iMaxItems) {
			PRODUCTION_PANEL_HEIGHT -= (PRODUCTION_PANEL_ITEM_HEIGHT + PIXELS_TO_BORDER);
			PRODUCTION_PANEL_NUM_ITEMS_Y--;
		}

		productionPanelPoint.setLocation(tileOpenProductionPanel.getTileWidth(),
				iFirstY + ((iLastY - iFirstY) / 2) - PRODUCTION_PANEL_HEIGHT / 2);

		// Positions
		productionPanelItemsPosition.clear();
		productionPanelItemsPlusRegularPosition.clear();
		productionPanelItemsMinusRegularPosition.clear();
		productionPanelItemsPlusAutomatedPosition.clear();
		productionPanelItemsMinusAutomatedPosition.clear();
		Point p;
		SmartMenu smItem;
		int iMenu;
		bucle1: for (int y = 0; y < PRODUCTION_PANEL_NUM_ITEMS_Y; y++) {
			for (int x = 0; x < PRODUCTION_PANEL_NUM_ITEMS_X; x++) {
				iMenu = (y * PRODUCTION_PANEL_NUM_ITEMS_X) + x;
				if (iMenu >= productionPanelMenu.getItems().size()) {
					break bucle1;
				}
				smItem = productionPanelMenu.getItems().get(iMenu);

				p = new Point(
						productionPanelPoint.x + PIXELS_TO_BORDER + ICON_WIDTH
								+ (x * (PRODUCTION_PANEL_ITEM_WIDTH + PIXELS_TO_BORDER + 2 * ICON_WIDTH)),
						productionPanelPoint.y + PIXELS_TO_BORDER
								+ (y * (PRODUCTION_PANEL_ITEM_HEIGHT + PIXELS_TO_BORDER)));
				productionPanelItemsPosition.add(p);
				if (smItem.getType() == SmartMenu.TYPE_ITEM
						&& !smItem.getCommand().equalsIgnoreCase(CommandPanel.COMMAND_BACK)) {
					productionPanelItemsPlusRegularPosition.add(new Point(p.x - ICON_WIDTH, p.y));
					productionPanelItemsMinusRegularPosition
							.add(new Point(p.x - ICON_WIDTH, p.y + PRODUCTION_PANEL_ITEM_HEIGHT - ICON_HEIGHT));
					productionPanelItemsPlusAutomatedPosition.add(new Point(p.x + PRODUCTION_PANEL_ITEM_WIDTH, p.y));
					productionPanelItemsMinusAutomatedPosition.add(new Point(p.x + PRODUCTION_PANEL_ITEM_WIDTH,
							p.y + PRODUCTION_PANEL_ITEM_HEIGHT - ICON_HEIGHT));
				} else {
					productionPanelItemsPlusRegularPosition.add(new Point(-1, -1));
					productionPanelItemsMinusRegularPosition.add(new Point(-1, -1));
					productionPanelItemsPlusAutomatedPosition.add(new Point(-1, -1));
					productionPanelItemsMinusAutomatedPosition.add(new Point(-1, -1));
				}
			}
		}

		// Minibotón para abrir/cerrar el menú de producción
		tileOpenCloseProductionPanelPoint.setLocation(0,
				renderHeight / 2 - tileOpenProductionPanel.getTileHeight() / 2);

		// Tutorial?
		ImagesPanel.resize(renderWidth, renderHeight);
	}

	public void createTradePanel() {
		TRADE_PANEL_WIDTH = (renderWidth / 8) * 7;
		TRADE_PANEL_HEIGHT = renderHeight - (iconNumCitizensBackgroundPoint.y + tileBottomItem.getTileHeight())
				- tileBottomItem.getTileHeight() / 2;
		tradePanelPoint.setLocation(renderWidth / 8 - ((renderWidth / 8) / 2),
				iconNumCitizensBackgroundPoint.y + tileBottomItem.getTileHeight() + tileBottomItem.getTileHeight() / 4);
		tradePanelClosePoint.setLocation(tradePanelPoint.x + TRADE_PANEL_WIDTH - tileButtonClose.getTileWidth(),
				tradePanelPoint.y);

		TradePanel.loadStatics();
		if (tradePanel != null && Game.getWorld() != null && Game.getWorld().getCurrentCaravanData() != null) {
			tradePanel.resize(Game.getWorld().getCurrentCaravanData());
		}
	}

	public static void createTradePanelContent(CaravanData caravanData) {
		if (caravanData != null) {
			if (tradePanel == null) {
				tradePanel = new TradePanel(caravanData, tradePanelPoint, TRADE_PANEL_WIDTH, TRADE_PANEL_HEIGHT);
			} else {
				tradePanel.resize(caravanData);
			}
		}
	}

	public void createMessagesPanel() {
		messagesPanelActive = -1;

		// Tamaño y close button
		MESSAGES_PANEL_WIDTH = (renderWidth / 8) * 7;
		MESSAGES_PANEL_HEIGHT = renderHeight - (iconNumCitizensBackgroundPoint.y + tileBottomItem.getTileHeight())
				- tileBottomItem.getTileHeight() / 2;
		messagesPanelPoint.setLocation(renderWidth / 8 - ((renderWidth / 8) / 2),
				iconNumCitizensBackgroundPoint.y + tileBottomItem.getTileHeight() + tileBottomItem.getTileHeight() / 4);
		messagesPanelClosePoint.setLocation(
				messagesPanelPoint.x + MESSAGES_PANEL_WIDTH - tileButtonClose.getTileWidth(), messagesPanelPoint.y);

		// Mini iconos y puntos (los que salen arriba a la izquierda de la pantalla, no
		// los de dentro del panel)
		if (messageTiles == null) {
			messageTiles = new Tile[MessagesPanel.MAX_TYPES];
			messageTilesON = new Tile[MessagesPanel.MAX_TYPES];
			messageTilesAlpha = new ArrayList<boolean[][]>(MessagesPanel.MAX_TYPES);

			for (int i = 0; i < MessagesPanel.MAX_TYPES; i++) {
				messageTiles[i] = new Tile("icon_messages" + i); //$NON-NLS-1$
				messageTilesON[i] = new Tile("icon_messages" + i + "ON"); //$NON-NLS-1$ //$NON-NLS-2$
				messageTilesAlpha.add(UtilsGL.generateAlpha(messageTiles[i]));
			}
		}

		messageIconPoints = new Point[MessagesPanel.MAX_TYPES];
		for (int i = 0; i < MessagesPanel.MAX_TYPES; i++) {
			messageIconPoints[i] = new Point(PIXELS_TO_BORDER + i * (messageTiles[i].getTileWidth() + PIXELS_TO_BORDER),
					PIXELS_TO_BORDER);
		}

		// Iconos dentro del panel
		// Los "tabs" (iconos gordos arriba)
		if (messagePanelTiles == null) {
			messagePanelTiles = new Tile[MessagesPanel.MAX_TYPES];
			messagePanelTilesON = new Tile[MessagesPanel.MAX_TYPES];
			messagePanelTilesAlpha = new ArrayList<boolean[][]>(MessagesPanel.MAX_TYPES);

			for (int i = 0; i < MessagesPanel.MAX_TYPES; i++) {
				messagePanelTiles[i] = new Tile("icon_big_messages" + i); //$NON-NLS-1$
				messagePanelTilesON[i] = new Tile("icon_big_messages" + i + "ON"); //$NON-NLS-1$ //$NON-NLS-2$
				messagePanelTilesAlpha.add(UtilsGL.generateAlpha(messagePanelTiles[i]));
			}
		}

		// Scroll up/down
		messagePanelIconScrollUpPoint.setLocation(
				messagesPanelPoint.x + MESSAGES_PANEL_WIDTH - tileMessagesPanel[3].getTileWidth()
						- tileScrollUp.getTileWidth(),
				messagesPanelPoint.y + tileMessagesPanel[1].getTileHeight() + messagePanelTiles[0].getTileHeight());
		messagePanelIconScrollDownPoint.setLocation(
				messagesPanelPoint.x + MESSAGES_PANEL_WIDTH - tileMessagesPanel[3].getTileWidth()
						- tileScrollUp.getTileWidth(),
				messagesPanelPoint.y + MESSAGES_PANEL_HEIGHT - tileScrollDown.getTileHeight()
						- tileMessagesPanel[1].getTileHeight());

		// Pages
		messagePanelPagesPositionPoint.setLocation(messagePanelIconScrollUpPoint.x + tileScrollUp.getTileWidth() / 2,
				messagePanelIconScrollUpPoint.y + tileScrollUp.getTileHeight()
						+ (messagePanelIconScrollDownPoint.y
								- (messagePanelIconScrollUpPoint.y + tileScrollUp.getTileHeight())) / 2
						- UtilFont.MAX_HEIGHT / 2);

		// Subpanel
		MESSAGES_PANEL_SUBPANEL_WIDTH = MESSAGES_PANEL_WIDTH
				- (3 * tileMessagesPanel[3].getTileWidth() + tileScrollUp.getTileWidth());
		MESSAGES_PANEL_SUBPANEL_HEIGHT = (messagePanelIconScrollDownPoint.y + tileScrollDown.getTileHeight())
				- messagePanelIconScrollUpPoint.y;
		messagesPanelSubPanelPoint.setLocation(messagesPanelPoint.x + tileMessagesPanel[3].getTileWidth(),
				messagePanelIconScrollUpPoint.y);

		// Posición de iconos (los 4 de arriba) dentro del panel (va aquí pq tienen que
		// centrarse con el subpanel)
		messagePanelIconPoints = new Point[MessagesPanel.MAX_TYPES];
		int iSeparation = (MESSAGES_PANEL_SUBPANEL_WIDTH
				- (MessagesPanel.MAX_TYPES * messagePanelTiles[0].getTileWidth())) / (MessagesPanel.MAX_TYPES + 1);
		for (int i = 0; i < MessagesPanel.MAX_TYPES; i++) {
			messagePanelIconPoints[i] = new Point(
					messagesPanelSubPanelPoint.x + iSeparation
							+ (i * (messagePanelTiles[0].getTileWidth() + iSeparation)),
					messagesPanelPoint.y + tileMessagesPanel[1].getTileHeight());
		}

		// Ésto es para que parta los messages render
		MessagesPanel.resize(MESSAGES_PANEL_WIDTH, MESSAGES_PANEL_HEIGHT);
	}

	public void createMatsPanel() {
		// Groups, si peta sale del juego
		MatsPanelData.loadGroups();

		MATS_PANEL_WIDTH = (renderWidth / 8) * 7;
		MATS_PANEL_HEIGHT = renderHeight - (iconMatsPoint.y + tileIconMats.getTileHeight())
				- tileBottomItem.getTileHeight() - tileBottomItem.getTileHeight() / 2;

		matsPanelPoint.setLocation(renderWidth / 8 - ((renderWidth / 8) / 2),
				iconMatsPoint.y + tileIconMats.getTileHeight() + tileBottomItem.getTileHeight() / 4);
		matsPanelClosePoint.setLocation(matsPanelPoint.x + MATS_PANEL_WIDTH - tileButtonClose.getTileWidth(),
				matsPanelPoint.y);

		// Iconos dentro del panel
		// Los "tabs" (iconos gordos arriba)
		if (matsPanelTiles == null) {
			matsPanelTiles = new Tile[MatsPanelData.numGroups];
			matsPanelTilesON = new Tile[MatsPanelData.numGroups];
			for (int i = 0; i < MatsPanelData.numGroups; i++) {
				matsPanelTiles[i] = new Tile(MatsPanelData.iconGroups.get(i));
				matsPanelTilesON[i] = new Tile(MatsPanelData.iconGroups.get(i) + "ON"); //$NON-NLS-1$
			}
		}

		matsLastGroup = 0;

		// Scroll up/down
		matsPanelIconScrollUpPoint.setLocation(
				matsPanelPoint.x + MATS_PANEL_WIDTH - tileMatsPanel[3].getTileWidth() - tileScrollUp.getTileWidth(),
				matsPanelPoint.y + tileMatsPanel[1].getTileHeight() + tileBottomItem.getTileHeight());
		matsPanelIconScrollDownPoint.setLocation(
				matsPanelPoint.x + MATS_PANEL_WIDTH - tileMatsPanel[3].getTileWidth() - tileScrollUp.getTileWidth(),
				matsPanelPoint.y + MATS_PANEL_HEIGHT - tileScrollDown.getTileHeight()
						- tileMatsPanel[1].getTileHeight());

		// Pages
		matsPanelPagesPositionPoint.setLocation(matsPanelIconScrollUpPoint.x + tileScrollUp.getTileWidth() / 2,
				matsPanelIconScrollUpPoint.y + tileScrollUp.getTileHeight()
						+ (matsPanelIconScrollDownPoint.y
								- (matsPanelIconScrollUpPoint.y + tileScrollUp.getTileHeight())) / 2
						- UtilFont.MAX_HEIGHT / 2);

		// Subpanel
		MATS_PANEL_SUBPANEL_WIDTH = MATS_PANEL_WIDTH
				- (3 * tileMatsPanelSubPanel[3].getTileWidth() + tileScrollUp.getTileWidth());
		MATS_PANEL_SUBPANEL_HEIGHT = (matsPanelIconScrollDownPoint.y + tileScrollDown.getTileHeight())
				- matsPanelIconScrollUpPoint.y;
		matsPanelSubPanelPoint.setLocation(matsPanelPoint.x + tileMatsPanelSubPanel[3].getTileWidth(),
				matsPanelIconScrollUpPoint.y);

		// Posición de iconos (los X de arriba) dentro del panel (va aquí pq tienen que
		// centrarse con el subpanel)
		matsPanelIconPoints = new Point[MatsPanelData.numGroups];
		int iSeparation = (MATS_PANEL_SUBPANEL_WIDTH - (MatsPanelData.numGroups * tileBottomItem.getTileWidth()))
				/ (MatsPanelData.numGroups + 1);
		for (int i = 0; i < MatsPanelData.numGroups; i++) {
			matsPanelIconPoints[i] = new Point(
					matsPanelSubPanelPoint.x + iSeparation + (i * (tileBottomItem.getTileWidth() + iSeparation)),
					matsPanelPoint.y + tileMatsPanel[1].getTileHeight());
		}

		// Ahora miramos de cuantas filas y columnas disponemos y seteamos el array de
		// posiciones
		int iMaxItemsWidth = (MATS_PANEL_SUBPANEL_WIDTH - 2 * tileMatsPanelSubPanel[3].getTileWidth())
				/ (tileBottomItem.getTileWidth() + 8);
		int iMaxItemsHeight = (MATS_PANEL_SUBPANEL_HEIGHT - 2 * tileMatsPanelSubPanel[1].getTileHeight())
				/ (tileBottomItem.getTileHeight() + 8);

		MATS_PANEL_MAX_ITEMS_PER_PAGE = iMaxItemsWidth * iMaxItemsHeight;
		matsPanelItemPoints = new Point[MATS_PANEL_MAX_ITEMS_PER_PAGE];
		int iSeparationW = (MATS_PANEL_SUBPANEL_WIDTH - 2 * tileMatsPanelSubPanel[3].getTileWidth()
				- iMaxItemsWidth * tileBottomItem.getTileWidth()) / (iMaxItemsWidth + 1);
		int iSeparationH = (MATS_PANEL_SUBPANEL_HEIGHT - 2 * tileMatsPanelSubPanel[1].getTileHeight()
				- iMaxItemsHeight * tileBottomItem.getTileHeight()) / (iMaxItemsHeight + 1);
		int iFirstWidth = matsPanelSubPanelPoint.x + tileMatsPanelSubPanel[3].getTileWidth() + iSeparationW;
		int iFirstHeight = matsPanelSubPanelPoint.y + tileMatsPanelSubPanel[1].getTileHeight() + iSeparationH;
		int x = iFirstWidth;
		int y = iFirstHeight;
		for (int i = 0; i < MATS_PANEL_MAX_ITEMS_PER_PAGE; i++) {
			matsPanelItemPoints[i] = new Point(x, y);
			x += (tileBottomItem.getTileWidth() + iSeparationW);
			if (x > (matsPanelSubPanelPoint.x + MATS_PANEL_SUBPANEL_WIDTH - tileMatsPanelSubPanel[3].getTileWidth()
					- tileBottomItem.getTileWidth() - 1)) {
				y += (tileBottomItem.getTileHeight() + iSeparationH);
				x = iFirstWidth;
			}
		}

		// Pages
		matsNumPages = new int[MatsPanelData.numGroups];
		matsIndexPages = new int[MatsPanelData.numGroups];
		for (int i = 0; i < MatsPanelData.numGroups; i++) {
			if (MatsPanelData.tileGroups.get(i).size() % MATS_PANEL_MAX_ITEMS_PER_PAGE == 0) {
				matsNumPages[i] = MatsPanelData.tileGroups.get(i).size() / MATS_PANEL_MAX_ITEMS_PER_PAGE;
			} else {
				matsNumPages[i] = (MatsPanelData.tileGroups.get(i).size() / MATS_PANEL_MAX_ITEMS_PER_PAGE) + 1;
			}

			matsIndexPages[i] = 0;
		}
	}

	public static void recheckPilePanelPages() {
		if (menuPile != null) {
			pilePanelPageIndex = 0;
			pilePanelMaxPages = (menuPile.getItems().size() / PILE_PANEL_MAX_ITEMS_PER_PAGE) + 1;
			if ((menuPile.getItems().size() % PILE_PANEL_MAX_ITEMS_PER_PAGE) == 0) {
				pilePanelMaxPages--;
			}
		}
	}

	public static void createPilePanel() {
		PILE_PANEL_WIDTH = (renderWidth / 8) * 7;
		PILE_PANEL_HEIGHT = renderHeight - (iconMatsPoint.y + tileIconMats.getTileHeight())
				- tileBottomItem.getTileHeight() - tileBottomItem.getTileHeight() / 2;

		pilePanelPoint.setLocation(renderWidth / 8 - ((renderWidth / 8) / 2),
				iconMatsPoint.y + tileIconMats.getTileHeight() + tileBottomItem.getTileHeight() / 4);
		pilePanelClosePoint.setLocation(pilePanelPoint.x + PILE_PANEL_WIDTH - tileButtonClose.getTileWidth(),
				pilePanelPoint.y);

		pilePanelPileContainerIDActive = -1;
		pilePanelIsContainer = false;
		pilePanelIsLocked = false;

		// Scroll up/down
		pilePanelIconScrollUpPoint.setLocation(
				pilePanelPoint.x + PILE_PANEL_WIDTH - tileMatsPanel[3].getTileWidth() - tileScrollUp.getTileWidth(),
				pilePanelPoint.y + tileMatsPanel[1].getTileHeight());
		pilePanelIconScrollDownPoint.setLocation(
				pilePanelPoint.x + PILE_PANEL_WIDTH - tileMatsPanel[3].getTileWidth() - tileScrollUp.getTileWidth(),
				pilePanelPoint.y + PILE_PANEL_HEIGHT - tileScrollDown.getTileHeight()
						- tileMatsPanel[1].getTileHeight());

		// Configuration buttons
		pilePanelIconConfigCopyPoint.setLocation(
				pilePanelPoint.x + PILE_PANEL_WIDTH / 2 - 3 * tileConfigCopy.getTileWidth(),
				pilePanelPoint.y + PILE_PANEL_HEIGHT - tileConfigCopy.getTileHeight() - UtilFont.MAX_HEIGHT / 2);
		pilePanelIconConfigLockPoint.setLocation(
				pilePanelPoint.x + PILE_PANEL_WIDTH / 2 - tileConfigCopy.getTileWidth()
						- tileConfigCopy.getTileWidth() / 2,
				pilePanelPoint.y + PILE_PANEL_HEIGHT - tileConfigLock.getTileHeight() - UtilFont.MAX_HEIGHT / 2);
		pilePanelIconConfigUnlockAllPoint.setLocation(
				pilePanelPoint.x + PILE_PANEL_WIDTH / 2 + tileConfigCopy.getTileWidth() / 2,
				pilePanelPoint.y + PILE_PANEL_HEIGHT - tileConfigLock.getTileHeight() - UtilFont.MAX_HEIGHT / 2);
		pilePanelIconConfigLockAllPoint.setLocation(
				pilePanelPoint.x + PILE_PANEL_WIDTH / 2 + 2 * tileConfigCopy.getTileWidth(),
				pilePanelPoint.y + PILE_PANEL_HEIGHT - tileConfigLock.getTileHeight() - UtilFont.MAX_HEIGHT / 2);

		// Pages
		pilePanelPagesPositionPoint.setLocation(pilePanelIconScrollUpPoint.x + tileScrollUp.getTileWidth() / 2,
				pilePanelIconScrollUpPoint.y + tileScrollUp.getTileHeight()
						+ (pilePanelIconScrollDownPoint.y
								- (pilePanelIconScrollUpPoint.y + tileScrollUp.getTileHeight())) / 2
						- UtilFont.MAX_HEIGHT / 2);

		// Miramos de cuantas filas y columnas disponemos y seteamos el array de
		// posiciones
		int iMaxItemsWidth = (PILE_PANEL_WIDTH - tileScrollUp.getTileWidth()
				- 4 * tileMatsPanelSubPanel[3].getTileWidth()) / (tileBottomItem.getTileWidth() + 8);
		int iMaxItemsHeight = (PILE_PANEL_HEIGHT - 2 * tileMatsPanelSubPanel[1].getTileHeight())
				/ (tileBottomItem.getTileHeight() + 8);

		PILE_PANEL_MAX_ITEMS_PER_PAGE = iMaxItemsWidth * iMaxItemsHeight;
		pilePanelItemPoints = new Point[PILE_PANEL_MAX_ITEMS_PER_PAGE];
		int iSeparationW = (PILE_PANEL_WIDTH - tileScrollUp.getTileWidth() - 4 * tileMatsPanelSubPanel[3].getTileWidth()
				- iMaxItemsWidth * tileBottomItem.getTileWidth()) / (iMaxItemsWidth + 1);
		int iSeparationH = (PILE_PANEL_HEIGHT - 2 * tileMatsPanelSubPanel[1].getTileHeight()
				- iMaxItemsHeight * tileBottomItem.getTileHeight()) / (iMaxItemsHeight + 1);
		int iFirstWidth = pilePanelPoint.x + tileMatsPanelSubPanel[3].getTileWidth() + iSeparationW;
		int iFirstHeight = pilePanelPoint.y + tileMatsPanelSubPanel[1].getTileHeight() + iSeparationH;
		// int x = iFirstWidth;
		// int y = iFirstHeight;
		int i = 0;
		for (int y = 0; y < iMaxItemsHeight; y++) {
			for (int x = 0; x < iMaxItemsWidth; x++) {
				pilePanelItemPoints[i] = new Point(iFirstWidth + (x * (tileBottomItem.getTileWidth() + iSeparationW)),
						iFirstHeight + (y * (tileBottomItem.getTileHeight() + iSeparationH)));
				i++;
			}
		}
	}

	public static void resizePilePanel(SmartMenu menuPile) {
		if (menuPile == null) {
			return;
		}

		PILE_PANEL_WIDTH = (renderWidth / 8) * 7; // Copied from the createPilePanel method
		int iMaxItemsWidth = (PILE_PANEL_WIDTH - tileScrollUp.getTileWidth()
				- 4 * tileMatsPanelSubPanel[3].getTileWidth()) / (tileBottomItem.getTileWidth() + 8);
		int iRows = (menuPile.getItems().size() / iMaxItemsWidth) + 1;
		if (menuPile.getItems().size() % iMaxItemsWidth == 0) {
			iRows--;
		}

		PILE_PANEL_HEIGHT = renderHeight - (iconMatsPoint.y + tileIconMats.getTileHeight())
				- tileBottomItem.getTileHeight() - tileBottomItem.getTileHeight() / 2;
		int iMaxItemsHeight = (PILE_PANEL_HEIGHT - 2 * tileMatsPanelSubPanel[1].getTileHeight())
				/ (tileBottomItem.getTileHeight() + 8);
		int iSeparationH = (PILE_PANEL_HEIGHT - 2 * tileMatsPanelSubPanel[1].getTileHeight()
				- iMaxItemsHeight * tileBottomItem.getTileHeight()) / (iMaxItemsHeight + 1);

		int iRowsToDelete = 0;
		if (iMaxItemsHeight <= iRows) {
			iRows = iMaxItemsHeight;
		} else {
			iRowsToDelete = (iMaxItemsHeight - iRows);
		}

		PILE_PANEL_HEIGHT -= (iRowsToDelete * (tileBottomItem.getTileHeight() + iSeparationH));
		// PILE_PANEL_HEIGHT = renderHeight - (iconMatsPoint.y +
		// tileIconMats.getTileHeight ()) - tileBottomItem.getTileHeight () -
		// tileBottomItem.getTileHeight () / 2;

		PILE_PANEL_MAX_ITEMS_PER_PAGE = iMaxItemsWidth * iRows;

		if (iRows <= 1) {
			// Lo hacemos pequeño por la derecha
			int iSeparationW = (PILE_PANEL_WIDTH - tileScrollUp.getTileWidth()
					- 4 * tileMatsPanelSubPanel[3].getTileWidth() - iMaxItemsWidth * tileBottomItem.getTileWidth())
					/ (iMaxItemsWidth + 1);
			int iFirstWidth = pilePanelPoint.x + tileMatsPanelSubPanel[3].getTileWidth() + iSeparationW;
			PILE_PANEL_WIDTH = iFirstWidth
					+ ((menuPile.getItems().size() + 1) * (tileBottomItem.getTileWidth() + iSeparationW));
		} else {
			PILE_PANEL_WIDTH = (renderWidth / 8) * 7;
		}

		pilePanelClosePoint.setLocation(pilePanelPoint.x + PILE_PANEL_WIDTH - tileButtonClose.getTileWidth(),
				pilePanelPoint.y);

		// Scroll up/down
		pilePanelIconScrollUpPoint.setLocation(
				pilePanelPoint.x + PILE_PANEL_WIDTH - tileMatsPanel[3].getTileWidth() - tileScrollUp.getTileWidth(),
				pilePanelPoint.y + tileMatsPanel[1].getTileHeight());
		pilePanelIconScrollDownPoint.setLocation(
				pilePanelPoint.x + PILE_PANEL_WIDTH - tileMatsPanel[3].getTileWidth() - tileScrollUp.getTileWidth(),
				pilePanelPoint.y + PILE_PANEL_HEIGHT - tileScrollDown.getTileHeight()
						- tileMatsPanel[1].getTileHeight());

		// Configuration buttons
		pilePanelIconConfigCopyPoint.setLocation(
				pilePanelPoint.x + PILE_PANEL_WIDTH / 2 - 3 * tileConfigCopy.getTileWidth(),
				pilePanelPoint.y + PILE_PANEL_HEIGHT - tileConfigCopy.getTileHeight() - UtilFont.MAX_HEIGHT / 2);
		pilePanelIconConfigLockPoint.setLocation(
				pilePanelPoint.x + PILE_PANEL_WIDTH / 2 - tileConfigCopy.getTileWidth()
						- tileConfigCopy.getTileWidth() / 2,
				pilePanelPoint.y + PILE_PANEL_HEIGHT - tileConfigLock.getTileHeight() - UtilFont.MAX_HEIGHT / 2);
		pilePanelIconConfigUnlockAllPoint.setLocation(
				pilePanelPoint.x + PILE_PANEL_WIDTH / 2 + tileConfigCopy.getTileWidth() / 2,
				pilePanelPoint.y + PILE_PANEL_HEIGHT - tileConfigLock.getTileHeight() - UtilFont.MAX_HEIGHT / 2);
		pilePanelIconConfigLockAllPoint.setLocation(
				pilePanelPoint.x + PILE_PANEL_WIDTH / 2 + 2 * tileConfigCopy.getTileWidth(),
				pilePanelPoint.y + PILE_PANEL_HEIGHT - tileConfigLock.getTileHeight() - UtilFont.MAX_HEIGHT / 2);

		// Pages
		pilePanelPagesPositionPoint.setLocation(pilePanelIconScrollUpPoint.x + tileScrollUp.getTileWidth() / 2,
				pilePanelIconScrollUpPoint.y + tileScrollUp.getTileHeight()
						+ (pilePanelIconScrollDownPoint.y
								- (pilePanelIconScrollUpPoint.y + tileScrollUp.getTileHeight())) / 2
						- UtilFont.MAX_HEIGHT / 2);
	}

	public static void recheckProfessionsPanelPages() {
		if (menuProfessions != null) {
			professionsPanelPageIndex = 0;
			professionsPanelMaxPages = (menuProfessions.getItems().size() / PROFESSIONS_PANEL_MAX_ITEMS_PER_PAGE) + 1;
			if ((menuProfessions.getItems().size() % PROFESSIONS_PANEL_MAX_ITEMS_PER_PAGE) == 0) {
				professionsPanelMaxPages--;
			}
		}
	}

	public static void createProfessionsPanel() {
		PROFESSIONS_PANEL_WIDTH = (renderWidth / 8) * 7;
		PROFESSIONS_PANEL_HEIGHT = renderHeight - (iconMatsPoint.y + tileIconMats.getTileHeight())
				- tileBottomItem.getTileHeight() - tileBottomItem.getTileHeight() / 2;

		professionsPanelPoint.setLocation(renderWidth / 8 - ((renderWidth / 8) / 2),
				iconMatsPoint.y + tileIconMats.getTileHeight() + tileBottomItem.getTileHeight() / 4);
		professionsPanelClosePoint.setLocation(
				professionsPanelPoint.x + PROFESSIONS_PANEL_WIDTH - tileButtonClose.getTileWidth(),
				professionsPanelPoint.y);

		professionsPanelCitizenOrGroupIDActive = -1;
		professionsPanelIsCitizen = false;

		// Scroll up/down
		professionsPanelIconScrollUpPoint
				.setLocation(
						professionsPanelPoint.x + PROFESSIONS_PANEL_WIDTH - tileMatsPanel[3].getTileWidth()
								- tileScrollUp.getTileWidth(),
						professionsPanelPoint.y + tileMatsPanel[1].getTileHeight());
		professionsPanelIconScrollDownPoint.setLocation(
				professionsPanelPoint.x + PROFESSIONS_PANEL_WIDTH - tileMatsPanel[3].getTileWidth()
						- tileScrollUp.getTileWidth(),
				professionsPanelPoint.y + PROFESSIONS_PANEL_HEIGHT - tileScrollDown.getTileHeight()
						- tileMatsPanel[1].getTileHeight());

		// Pages
		professionsPanelPagesPositionPoint.setLocation(
				professionsPanelIconScrollUpPoint.x + tileScrollUp.getTileWidth() / 2,
				professionsPanelIconScrollUpPoint.y + tileScrollUp.getTileHeight()
						+ (professionsPanelIconScrollDownPoint.y
								- (professionsPanelIconScrollUpPoint.y + tileScrollUp.getTileHeight())) / 2
						- UtilFont.MAX_HEIGHT / 2);

		// Miramos de cuantas filas y columnas disponemos y seteamos el array de
		// posiciones
		int iMaxItemsWidth = (PROFESSIONS_PANEL_WIDTH - tileScrollUp.getTileWidth()
				- 4 * tileMatsPanelSubPanel[3].getTileWidth()) / (tileBottomItem.getTileWidth() + 8);
		int iMaxItemsHeight = (PROFESSIONS_PANEL_HEIGHT - 2 * tileMatsPanelSubPanel[1].getTileHeight())
				/ (tileBottomItem.getTileHeight() + 8);

		PROFESSIONS_PANEL_MAX_ITEMS_PER_PAGE = iMaxItemsWidth * iMaxItemsHeight;
		professionsPanelItemPoints = new Point[PROFESSIONS_PANEL_MAX_ITEMS_PER_PAGE];
		int iSeparationW = (PROFESSIONS_PANEL_WIDTH - tileScrollUp.getTileWidth()
				- 4 * tileMatsPanelSubPanel[3].getTileWidth() - iMaxItemsWidth * tileBottomItem.getTileWidth())
				/ (iMaxItemsWidth + 1);
		int iSeparationH = (PROFESSIONS_PANEL_HEIGHT - 2 * tileMatsPanelSubPanel[1].getTileHeight()
				- iMaxItemsHeight * tileBottomItem.getTileHeight()) / (iMaxItemsHeight + 1);
		int iFirstWidth = professionsPanelPoint.x + tileMatsPanelSubPanel[3].getTileWidth() + iSeparationW;
		int iFirstHeight = professionsPanelPoint.y + tileMatsPanelSubPanel[1].getTileHeight() + iSeparationH;
		// int x = iFirstWidth;
		// int y = iFirstHeight;
		int i = 0;
		for (int y = 0; y < iMaxItemsHeight; y++) {
			for (int x = 0; x < iMaxItemsWidth; x++) {
				professionsPanelItemPoints[i] = new Point(
						iFirstWidth + (x * (tileBottomItem.getTileWidth() + iSeparationW)),
						iFirstHeight + (y * (tileBottomItem.getTileHeight() + iSeparationH)));
				i++;
			}
		}
	}

	public static void resizeProfessionsPanel(SmartMenu menuProfessions) {
		if (menuProfessions == null) {
			return;
		}

		int iMaxItemsWidth = (PROFESSIONS_PANEL_WIDTH - tileScrollUp.getTileWidth()
				- 4 * tileMatsPanelSubPanel[3].getTileWidth()) / (tileBottomItem.getTileWidth() + 8);
		int iRows = (menuProfessions.getItems().size() / iMaxItemsWidth) + 1;
		if (menuProfessions.getItems().size() % iMaxItemsWidth == 0) {
			iRows--;
		}

		PROFESSIONS_PANEL_HEIGHT = renderHeight - (iconMatsPoint.y + tileIconMats.getTileHeight())
				- tileBottomItem.getTileHeight() - tileBottomItem.getTileHeight() / 2;
		int iMaxItemsHeight = (PROFESSIONS_PANEL_HEIGHT - 2 * tileMatsPanelSubPanel[1].getTileHeight())
				/ (tileBottomItem.getTileHeight() + 8);
		int iSeparationH = (PROFESSIONS_PANEL_HEIGHT - 2 * tileMatsPanelSubPanel[1].getTileHeight()
				- iMaxItemsHeight * tileBottomItem.getTileHeight()) / (iMaxItemsHeight + 1);

		int iRowsToDelete = 0;
		if (iMaxItemsHeight <= iRows) {
			iRows = iMaxItemsHeight;
		} else {
			iRowsToDelete = (iMaxItemsHeight - iRows);
		}

		PROFESSIONS_PANEL_HEIGHT -= (iRowsToDelete * (tileBottomItem.getTileHeight() + iSeparationH));
		// PILE_PANEL_HEIGHT = renderHeight - (iconMatsPoint.y +
		// tileIconMats.getTileHeight ()) - tileBottomItem.getTileHeight () -
		// tileBottomItem.getTileHeight () / 2;

		PROFESSIONS_PANEL_MAX_ITEMS_PER_PAGE = iMaxItemsWidth * iRows;

		if (iRows <= 1) {
			// Lo hacemos pequeño por la derecha
			int iSeparationW = (PROFESSIONS_PANEL_WIDTH - tileScrollUp.getTileWidth()
					- 4 * tileMatsPanelSubPanel[3].getTileWidth() - iMaxItemsWidth * tileBottomItem.getTileWidth())
					/ (iMaxItemsWidth + 1);
			int iFirstWidth = professionsPanelPoint.x + tileMatsPanelSubPanel[3].getTileWidth() + iSeparationW;
			PROFESSIONS_PANEL_WIDTH = iFirstWidth
					+ ((menuProfessions.getItems().size() + 1) * (tileBottomItem.getTileWidth() + iSeparationW));
		} else {
			PROFESSIONS_PANEL_WIDTH = (renderWidth / 8) * 7;
		}

		professionsPanelClosePoint.setLocation(
				professionsPanelPoint.x + PROFESSIONS_PANEL_WIDTH - tileButtonClose.getTileWidth(),
				professionsPanelPoint.y);

		// Scroll up/down
		professionsPanelIconScrollUpPoint
				.setLocation(
						professionsPanelPoint.x + PROFESSIONS_PANEL_WIDTH - tileMatsPanel[3].getTileWidth()
								- tileScrollUp.getTileWidth(),
						professionsPanelPoint.y + tileMatsPanel[1].getTileHeight());
		professionsPanelIconScrollDownPoint.setLocation(
				professionsPanelPoint.x + PROFESSIONS_PANEL_WIDTH - tileMatsPanel[3].getTileWidth()
						- tileScrollUp.getTileWidth(),
				professionsPanelPoint.y + PROFESSIONS_PANEL_HEIGHT - tileScrollDown.getTileHeight()
						- tileMatsPanel[1].getTileHeight());

		// Pages
		professionsPanelPagesPositionPoint.setLocation(
				professionsPanelIconScrollUpPoint.x + tileScrollUp.getTileWidth() / 2,
				professionsPanelIconScrollUpPoint.y + tileScrollUp.getTileHeight()
						+ (professionsPanelIconScrollDownPoint.y
								- (professionsPanelIconScrollUpPoint.y + tileScrollUp.getTileHeight())) / 2
						- UtilFont.MAX_HEIGHT / 2);
	}

	public static void createLivingsPanel(int iPanelTypeActive, int iSoldiersGroupActive, int iCitizensGroupActive) {
		livingsPanelActive = iPanelTypeActive;
		livingsPanelCitizensGroupActive = iCitizensGroupActive;
		livingsPanelSoldiersGroupActive = iSoldiersGroupActive;

		LIVINGS_PANEL_GROUPS_WIDTH = 2 * tileLivingsGroupPanel[3].getTileWidth() + 32;
		LIVINGS_PANEL_WIDTH = 2 * tileLivingsPanel[3].getTileWidth() + 7 * tileBottomItem.getTileWidth()
				+ tileScrollUp.getTileWidth();
		if (iPanelTypeActive == LIVINGS_PANEL_TYPE_CITIZENS) {
			LIVINGS_PANEL_WIDTH += (2 * tileBottomItem.getTileWidth() + tileLivingsRowAutoequip.getTileWidth()
					+ tileLivingsRowConvertSoldier.getTileWidth() + tileLivingsRowProfession.getTileWidth()
					+ tileLivingsRowJobsGroups.getTileWidth() + LIVINGS_PANEL_GROUPS_WIDTH);
		} else if (iPanelTypeActive == LIVINGS_PANEL_TYPE_SOLDIERS) {
			LIVINGS_PANEL_WIDTH += (2 * tileBottomItem.getTileWidth() + tileLivingsRowAutoequip.getTileWidth()
					+ tileLivingsRowConvertCivilian.getTileWidth() + tileLivingsRowConvertSoldierGuard.getTileWidth()
					+ tileLivingsRowGroupAdd.getTileWidth()) + LIVINGS_PANEL_GROUPS_WIDTH;
		}

		LIVINGS_PANEL_HEIGHT = renderHeight - (iconNumCitizensBackgroundPoint.y + tileBottomItem.getTileHeight())
				- tileBottomItem.getTileHeight() / 2;
		LIVINGS_PANEL_GROUPS_HEIGHT = LIVINGS_PANEL_HEIGHT - 2 * tileLivingsPanel[1].getTileHeight();

		livingsPanelPoint.setLocation(renderWidth / 2 - LIVINGS_PANEL_WIDTH / 2,
				iconNumCitizensBackgroundPoint.y + tileBottomItem.getTileHeight() + tileBottomItem.getTileHeight() / 4);
		livingsPanelClosePoint.setLocation(livingsPanelPoint.x + LIVINGS_PANEL_WIDTH - tileButtonClose.getTileWidth(),
				livingsPanelPoint.y);

		if (iPanelTypeActive == LIVINGS_PANEL_TYPE_SOLDIERS || iPanelTypeActive == LIVINGS_PANEL_TYPE_CITIZENS) {
			// Sub-groups panel
			livingsGroupPanelPoint.setLocation(
					livingsPanelPoint.x + LIVINGS_PANEL_WIDTH - tileLivingsPanel[3].getTileWidth()
							- LIVINGS_PANEL_GROUPS_WIDTH,
					livingsPanelPoint.y + (((livingsPanelPoint.y + LIVINGS_PANEL_HEIGHT) - livingsPanelPoint.y) / 2)
							- LIVINGS_PANEL_GROUPS_HEIGHT / 2);

			// Primer icono del subpanel y la separación
			int iSeparation = (LIVINGS_PANEL_GROUPS_HEIGHT - 2 * tileLivingsGroupPanel[3].getTileHeight()
					- tileLivingsNoGroup.getTileHeight()
					- (SoldierGroups.MAX_GROUPS * tileLivingsGroup.getTileHeight())) / (SoldierGroups.MAX_GROUPS + 2);
			livingsGroupPanelFirstIconPoint.setLocation(
					livingsGroupPanelPoint.x + LIVINGS_PANEL_GROUPS_WIDTH / 2 - tileLivingsNoGroup.getTileWidth() / 2,
					livingsGroupPanelPoint.y + tileLivingsGroupPanel[3].getTileHeight() + iSeparation);
			livingsGroupPanelIconsSeparation = iSeparation + tileLivingsNoGroup.getTileHeight();
		}

		// Miramos cuantas livings caben
		int iMaxHeight;
		if (checkGroupsPanelEnabled(iPanelTypeActive)) {
			iMaxHeight = LIVINGS_PANEL_HEIGHT - 2 * tileLivingsPanel[1].getTileHeight() - tileBottomItem.getTileHeight()
					- tileBottomItem.getTileHeight() / 2;
		} else {
			iMaxHeight = LIVINGS_PANEL_HEIGHT - 2 * tileLivingsPanel[1].getTileHeight();
		}
		LIVINGS_PANEL_MAX_ROWS = iMaxHeight / tileBottomItem.getTileHeight();
		if (LIVINGS_PANEL_MAX_ROWS < 1) {
			LIVINGS_PANEL_MAX_ROWS = 1;
		}
		// Rows
		int iSeparation;
		if (LIVINGS_PANEL_MAX_ROWS > 1) {
			iSeparation = (iMaxHeight - LIVINGS_PANEL_MAX_ROWS * tileBottomItem.getTileHeight())
					/ (LIVINGS_PANEL_MAX_ROWS - 1);
		} else {
			iSeparation = 0;
		}

		int iIniY = livingsPanelPoint.y + tileLivingsPanel[1].getTileHeight();

		if (livingsPanelRowPoints == null || livingsPanelRowPoints.length < LIVINGS_PANEL_MAX_ROWS) {
			livingsPanelRowPoints = new Point[LIVINGS_PANEL_MAX_ROWS];
			livingsPanelRowHeadPoints = new Point[LIVINGS_PANEL_MAX_ROWS];
			livingsPanelRowBodyPoints = new Point[LIVINGS_PANEL_MAX_ROWS];
			livingsPanelRowLegsPoints = new Point[LIVINGS_PANEL_MAX_ROWS];
			livingsPanelRowFeetPoints = new Point[LIVINGS_PANEL_MAX_ROWS];
			livingsPanelRowWeaponPoints = new Point[LIVINGS_PANEL_MAX_ROWS];
			livingsPanelRowAutoequipPoints = new Point[LIVINGS_PANEL_MAX_ROWS];
			livingsPanelRowProfessionPoints = new Point[LIVINGS_PANEL_MAX_ROWS];
			livingsPanelRowJobsGroupsPoints = new Point[LIVINGS_PANEL_MAX_ROWS];
			livingsPanelRowConvertCivilianSoldierPoints = new Point[LIVINGS_PANEL_MAX_ROWS];
			livingsPanelRowConvertSoldierGuardPoints = new Point[LIVINGS_PANEL_MAX_ROWS];
			livingsPanelRowConvertSoldierPatrolPoints = new Point[LIVINGS_PANEL_MAX_ROWS];
			livingsPanelRowConvertSoldierBossPoints = new Point[LIVINGS_PANEL_MAX_ROWS];
			livingsPanelRowGroupPoints = new Point[LIVINGS_PANEL_MAX_ROWS];

			for (int i = 0; i < LIVINGS_PANEL_MAX_ROWS; i++) {
				livingsPanelRowPoints[i] = new Point(0, 0);
				livingsPanelRowHeadPoints[i] = new Point(0, 0);
				livingsPanelRowBodyPoints[i] = new Point(0, 0);
				livingsPanelRowLegsPoints[i] = new Point(0, 0);
				livingsPanelRowFeetPoints[i] = new Point(0, 0);
				livingsPanelRowWeaponPoints[i] = new Point(0, 0);

				livingsPanelRowAutoequipPoints[i] = new Point(0, 0);
				livingsPanelRowProfessionPoints[i] = new Point(0, 0);
				livingsPanelRowJobsGroupsPoints[i] = new Point(0, 0);
				livingsPanelRowConvertCivilianSoldierPoints[i] = new Point(0, 0);
			}

			if (livingsPanelRowConvertSoldierGuardPoints == null || iPanelTypeActive == LIVINGS_PANEL_TYPE_SOLDIERS) {
				for (int i = 0; i < LIVINGS_PANEL_MAX_ROWS; i++) {
					livingsPanelRowConvertSoldierGuardPoints[i] = new Point(0, 0);
					livingsPanelRowConvertSoldierPatrolPoints[i] = new Point(0, 0);
					livingsPanelRowConvertSoldierBossPoints[i] = new Point(0, 0);
					livingsPanelRowGroupPoints[i] = new Point(0, 0);
				}
			}
		}

		int iRowsOffsetY = 0;
		if (checkGroupsPanelEnabled(iPanelTypeActive)) {
			iRowsOffsetY = tileBottomItem.getTileHeight() + tileBottomItem.getTileHeight() / 2;
		}

		for (int i = 0; i < LIVINGS_PANEL_MAX_ROWS; i++) {
			// Living
			livingsPanelRowPoints[i] = new Point(livingsPanelPoint.x + tileLivingsPanel[3].getTileWidth(),
					iIniY + (i * (tileBottomItem.getTileHeight() + iSeparation)) + iRowsOffsetY);

			// Equipment
			livingsPanelRowHeadPoints[i] = new Point(
					livingsPanelRowPoints[i].x + tileBottomItem.getTileWidth() + tileBottomItem.getTileWidth() / 2,
					livingsPanelRowPoints[i].y);
			livingsPanelRowBodyPoints[i] = new Point(livingsPanelRowHeadPoints[i].x + tileBottomItem.getTileWidth(),
					livingsPanelRowPoints[i].y);
			livingsPanelRowLegsPoints[i] = new Point(livingsPanelRowBodyPoints[i].x + tileBottomItem.getTileWidth(),
					livingsPanelRowPoints[i].y);
			livingsPanelRowFeetPoints[i] = new Point(livingsPanelRowLegsPoints[i].x + tileBottomItem.getTileWidth(),
					livingsPanelRowPoints[i].y);
			livingsPanelRowWeaponPoints[i] = new Point(livingsPanelRowFeetPoints[i].x + tileBottomItem.getTileWidth(),
					livingsPanelRowPoints[i].y);

			// Autoequip
			livingsPanelRowAutoequipPoints[i] = new Point(
					livingsPanelRowWeaponPoints[i].x + tileBottomItem.getTileWidth()
							+ tileBottomItem.getTileWidth() / 2,
					livingsPanelRowWeaponPoints[i].y + tileBottomItem.getTileHeight() / 2
							- tileLivingsRowAutoequip.getTileHeight() / 2);

			// Convert to soldier / civilian
			livingsPanelRowConvertCivilianSoldierPoints[i] = new Point(
					livingsPanelRowAutoequipPoints[i].x + tileLivingsRowAutoequip.getTileWidth()
							+ tileBottomItem.getTileWidth() / 2,
					livingsPanelRowWeaponPoints[i].y + tileBottomItem.getTileHeight() / 2
							- tileLivingsRowConvertSoldier.getTileHeight() / 2);

			// Profession
			livingsPanelRowProfessionPoints[i] = new Point(
					livingsPanelRowConvertCivilianSoldierPoints[i].x + tileLivingsRowConvertSoldier.getTileWidth(),
					livingsPanelRowConvertCivilianSoldierPoints[i].y + tileLivingsRowConvertSoldier.getTileHeight() / 2
							- tileLivingsRowProfession.getTileHeight() / 2);

			// Jobs groups
			livingsPanelRowJobsGroupsPoints[i] = new Point(
					livingsPanelRowProfessionPoints[i].x + tileLivingsRowProfession.getTileWidth(),
					livingsPanelRowProfessionPoints[i].y + tileLivingsRowProfession.getTileHeight() / 2
							- tileLivingsRowJobsGroups.getTileHeight() / 2);
		}
		if (iPanelTypeActive == LIVINGS_PANEL_TYPE_SOLDIERS) {
			if (livingsPanelSoldiersGroupActive == -1) {
				for (int i = 0; i < LIVINGS_PANEL_MAX_ROWS; i++) {
					// Soldier states
					livingsPanelRowConvertSoldierGuardPoints[i] = new Point(
							livingsPanelRowConvertCivilianSoldierPoints[i].x
									+ tileLivingsRowConvertCivilian.getTileWidth() + tileBottomItem.getTileWidth() / 2,
							livingsPanelRowWeaponPoints[i].y + tileBottomItem.getTileHeight() / 2
									- tileLivingsRowConvertSoldierGuard.getTileHeight());
					livingsPanelRowConvertSoldierPatrolPoints[i] = new Point(
							livingsPanelRowConvertSoldierGuardPoints[i].x
									+ tileLivingsRowConvertSoldierGuard.getTileWidth(),
							livingsPanelRowConvertSoldierGuardPoints[i].y);
					livingsPanelRowConvertSoldierBossPoints[i] = new Point(
							livingsPanelRowConvertSoldierGuardPoints[i].x, livingsPanelRowConvertSoldierGuardPoints[i].y
									+ tileLivingsRowConvertSoldierGuard.getTileHeight());

					// Soldier add group
					livingsPanelRowGroupPoints[i] = new Point(livingsPanelRowConvertSoldierPatrolPoints[i].x,
							livingsPanelRowConvertSoldierBossPoints[i].y);
				}
			} else {
				for (int i = 0; i < LIVINGS_PANEL_MAX_ROWS; i++) {
					// Soldier remove group
					livingsPanelRowGroupPoints[i] = new Point(
							livingsPanelRowConvertCivilianSoldierPoints[i].x
									+ tileLivingsRowConvertCivilian.getTileWidth() + tileBottomItem.getTileWidth() / 2,
							livingsPanelRowConvertCivilianSoldierPoints[i].y);
				}

				// Single group panel
				if (iPanelTypeActive == LIVINGS_PANEL_TYPE_SOLDIERS && livingsPanelSoldiersGroupActive != -1) {
					LIVINGS_PANEL_SINGLE_GROUP_WIDTH = livingsPanelRowGroupPoints[0].x
							+ tileLivingsRowGroupRemove.getTileWidth() - livingsPanelRowPoints[0].x;
					LIVINGS_PANEL_SINGLE_GROUP_HEIGHT = tileBottomItem.getTileHeight()
							+ tileBottomItem.getTileHeight() / 2;
					livingsSingleGroupPanelPoint.setLocation(livingsPanelRowPoints[0].x,
							iIniY - tileLivingsGroupPanel[1].getTileHeight() / 2);

					int iSeparationSingleGroup = (LIVINGS_PANEL_SINGLE_GROUP_WIDTH
							- 2 * tileLivingsGroupPanel[3].getTileWidth() - tileLivingsSingleGroupRename.getTileWidth()
							- tileLivingsSingleGroupGuard.getTileWidth() - tileLivingsSingleGroupPatrol.getTileWidth()
							- tileLivingsSingleGroupBoss.getTileWidth() - tileLivingsRowAutoequip.getTileWidth()
							- tileLivingsSingleGroupDisband.getTileWidth()) / 5;
					// Botones del single group panel
					int iFirstButton = livingsSingleGroupPanelPoint.x + tileLivingsGroupPanel[3].getTileWidth();
					// Rename
					livingsSingleGroupRenamePoint.setLocation(iFirstButton, livingsSingleGroupPanelPoint.y
							+ LIVINGS_PANEL_SINGLE_GROUP_HEIGHT / 2 - tileLivingsSingleGroupRename.getTileHeight() / 2);
					iFirstButton += tileLivingsSingleGroupRename.getTileWidth() + iSeparationSingleGroup;
					// Guard
					livingsSingleGroupGuardPoint.setLocation(iFirstButton, livingsSingleGroupPanelPoint.y
							+ LIVINGS_PANEL_SINGLE_GROUP_HEIGHT / 2 - tileLivingsSingleGroupGuard.getTileHeight() / 2);
					iFirstButton += tileLivingsSingleGroupGuard.getTileWidth() + iSeparationSingleGroup;
					// Patrol
					livingsSingleGroupPatrolPoint.setLocation(iFirstButton, livingsSingleGroupPanelPoint.y
							+ LIVINGS_PANEL_SINGLE_GROUP_HEIGHT / 2 - tileLivingsSingleGroupPatrol.getTileHeight() / 2);
					iFirstButton += tileLivingsSingleGroupPatrol.getTileWidth() + iSeparationSingleGroup;
					// Boss
					livingsSingleGroupBossPoint.setLocation(iFirstButton, livingsSingleGroupPanelPoint.y
							+ LIVINGS_PANEL_SINGLE_GROUP_HEIGHT / 2 - tileLivingsSingleGroupBoss.getTileHeight() / 2);
					iFirstButton += tileLivingsSingleGroupBoss.getTileWidth() + iSeparationSingleGroup;
					// Autoequip
					livingsSingleGroupAutoequipPoint.setLocation(iFirstButton, livingsSingleGroupPanelPoint.y
							+ LIVINGS_PANEL_SINGLE_GROUP_HEIGHT / 2 - tileLivingsRowAutoequip.getTileHeight() / 2);
					iFirstButton += tileLivingsRowAutoequip.getTileWidth() + iSeparationSingleGroup;
					// Disband
					livingsSingleGroupDisbandPoint.setLocation(iFirstButton,
							livingsSingleGroupPanelPoint.y + LIVINGS_PANEL_SINGLE_GROUP_HEIGHT / 2
									- tileLivingsSingleGroupDisband.getTileHeight() / 2);
				}
			}
		} else if (iPanelTypeActive == LIVINGS_PANEL_TYPE_CITIZENS) {
			if (livingsPanelCitizensGroupActive == -1) {
				for (int i = 0; i < LIVINGS_PANEL_MAX_ROWS; i++) {
					// Soldier states
					livingsPanelRowConvertSoldierGuardPoints[i] = new Point(
							livingsPanelRowConvertCivilianSoldierPoints[i].x
									+ tileLivingsRowConvertCivilian.getTileWidth() + tileBottomItem.getTileWidth() / 2,
							livingsPanelRowWeaponPoints[i].y + tileBottomItem.getTileHeight() / 2
									- tileLivingsRowConvertSoldierGuard.getTileHeight());
					livingsPanelRowConvertSoldierPatrolPoints[i] = new Point(
							livingsPanelRowConvertSoldierGuardPoints[i].x
									+ tileLivingsRowConvertSoldierGuard.getTileWidth(),
							livingsPanelRowConvertSoldierGuardPoints[i].y);
					livingsPanelRowConvertSoldierBossPoints[i] = new Point(
							livingsPanelRowConvertSoldierGuardPoints[i].x, livingsPanelRowConvertSoldierGuardPoints[i].y
									+ tileLivingsRowConvertSoldierGuard.getTileHeight());

					// Soldier add group
					livingsPanelRowGroupPoints[i] = new Point(livingsPanelRowConvertSoldierPatrolPoints[i].x,
							livingsPanelRowConvertSoldierBossPoints[i].y);
				}
			} else {
				for (int i = 0; i < LIVINGS_PANEL_MAX_ROWS; i++) {
					// Civilian??? remove group
					livingsPanelRowGroupPoints[i] = new Point(
							livingsPanelRowConvertCivilianSoldierPoints[i].x
									+ tileLivingsRowConvertCivilian.getTileWidth() + tileBottomItem.getTileWidth() / 2,
							livingsPanelRowConvertCivilianSoldierPoints[i].y);
				}

				// Single group panel
				if (iPanelTypeActive == LIVINGS_PANEL_TYPE_CITIZENS && livingsPanelCitizensGroupActive != -1) {
					LIVINGS_PANEL_SINGLE_GROUP_WIDTH = livingsPanelRowGroupPoints[0].x
							+ tileLivingsRowGroupRemove.getTileWidth() - livingsPanelRowPoints[0].x;
					LIVINGS_PANEL_SINGLE_GROUP_HEIGHT = tileBottomItem.getTileHeight()
							+ tileBottomItem.getTileHeight() / 2;
					livingsSingleGroupPanelPoint.setLocation(livingsPanelRowPoints[0].x,
							iIniY - tileLivingsGroupPanel[1].getTileHeight() / 2);

					int iSeparationSingleGroup = (LIVINGS_PANEL_SINGLE_GROUP_WIDTH
							- 2 * tileLivingsGroupPanel[3].getTileWidth() - tileLivingsSingleGroupRename.getTileWidth()
							- tileLivingsRowAutoequip.getTileWidth() - tileLivingsSingleGroupDisband.getTileWidth()
							- tileLivingsSingleGroupChangeJobs.getTileWidth()) / 3;
					// Botones del single group panel
					int iFirstButton = livingsSingleGroupPanelPoint.x + tileLivingsGroupPanel[3].getTileWidth();
					// Rename
					livingsSingleGroupRenamePoint.setLocation(iFirstButton, livingsSingleGroupPanelPoint.y
							+ LIVINGS_PANEL_SINGLE_GROUP_HEIGHT / 2 - tileLivingsSingleGroupRename.getTileHeight() / 2);
					iFirstButton += tileLivingsSingleGroupRename.getTileWidth() + iSeparationSingleGroup;
					// Autoequip
					livingsSingleGroupAutoequipPoint.setLocation(iFirstButton, livingsSingleGroupPanelPoint.y
							+ LIVINGS_PANEL_SINGLE_GROUP_HEIGHT / 2 - tileLivingsRowAutoequip.getTileHeight() / 2);
					iFirstButton += tileLivingsRowAutoequip.getTileWidth() + iSeparationSingleGroup;
					// Change jobs
					livingsSingleGroupChangeJobsPoint.setLocation(iFirstButton,
							livingsSingleGroupPanelPoint.y + LIVINGS_PANEL_SINGLE_GROUP_HEIGHT / 2
									- tileLivingsSingleGroupChangeJobs.getTileHeight() / 2);
					iFirstButton += tileLivingsSingleGroupChangeJobs.getTileWidth() + iSeparationSingleGroup;
					// Disband
					livingsSingleGroupDisbandPoint.setLocation(iFirstButton,
							livingsSingleGroupPanelPoint.y + LIVINGS_PANEL_SINGLE_GROUP_HEIGHT / 2
									- tileLivingsSingleGroupDisband.getTileHeight() / 2);
				}
			}
		}

		// Scrolls
		if (iPanelTypeActive == LIVINGS_PANEL_TYPE_SOLDIERS) {
			if (livingsPanelSoldiersGroupActive != -1) {
				// Scroll up/down
				livingsPanelIconScrollUpPoint.setLocation(
						livingsGroupPanelPoint.x - tileBottomItem.getTileWidth() / 2 - tileScrollUp.getTileWidth(),
						livingsPanelPoint.y + tileLivingsPanel[1].getTileHeight());
				livingsPanelIconScrollDownPoint.setLocation(
						livingsGroupPanelPoint.x - tileBottomItem.getTileWidth() / 2 - tileScrollDown.getTileWidth(),
						livingsPanelRowPoints[LIVINGS_PANEL_MAX_ROWS - 1].y + tileBottomItem.getTileWidth()
								- tileScrollDown.getTileHeight());
			} else {
				// Scroll up/down
				livingsPanelIconScrollUpPoint.setLocation(
						livingsGroupPanelPoint.x - tileBottomItem.getTileWidth() / 2 - tileScrollUp.getTileWidth(),
						livingsPanelPoint.y + tileLivingsPanel[1].getTileHeight());
				livingsPanelIconScrollDownPoint.setLocation(
						livingsGroupPanelPoint.x - tileBottomItem.getTileWidth() / 2 - tileScrollDown.getTileWidth(),
						livingsPanelPoint.y + LIVINGS_PANEL_HEIGHT - tileLivingsPanel[1].getTileHeight()
								- tileScrollDown.getTileHeight());
			}
		} else if (iPanelTypeActive == LIVINGS_PANEL_TYPE_CITIZENS) {
			if (livingsPanelCitizensGroupActive != -1) {
				// Scroll up/down
				livingsPanelIconScrollUpPoint.setLocation(
						livingsGroupPanelPoint.x - tileBottomItem.getTileWidth() / 2 - tileScrollUp.getTileWidth(),
						livingsPanelPoint.y + tileLivingsPanel[1].getTileHeight());
				livingsPanelIconScrollDownPoint.setLocation(
						livingsGroupPanelPoint.x - tileBottomItem.getTileWidth() / 2 - tileScrollDown.getTileWidth(),
						livingsPanelRowPoints[LIVINGS_PANEL_MAX_ROWS - 1].y + tileBottomItem.getTileWidth()
								- tileScrollDown.getTileHeight());
			} else {
				// Scroll up/down
				livingsPanelIconScrollUpPoint.setLocation(
						livingsGroupPanelPoint.x - tileBottomItem.getTileWidth() / 2 - tileScrollUp.getTileWidth(),
						livingsPanelPoint.y + tileLivingsPanel[1].getTileHeight()
								+ 2 * tileIconLevelDown.getTileHeight());
				livingsPanelIconScrollDownPoint.setLocation(
						livingsGroupPanelPoint.x - tileBottomItem.getTileWidth() / 2 - tileScrollDown.getTileWidth(),
						livingsPanelPoint.y + LIVINGS_PANEL_HEIGHT - tileLivingsPanel[1].getTileHeight()
								- tileScrollDown.getTileHeight());

				// Restrict points
				livingsPanelIconRestrictUpPoint.setLocation(
						livingsPanelIconScrollUpPoint.x - (tileIconLevelUp.getTileWidth() / 4) * 3,
						livingsPanelPoint.y + tileLivingsPanel[1].getTileHeight());
				livingsPanelIconRestrictDownPoint.setLocation(
						livingsPanelIconScrollUpPoint.x + tileScrollUp.getTileWidth()
								- (tileIconLevelDown.getTileWidth() / 4),
						livingsPanelPoint.y + tileLivingsPanel[1].getTileHeight());
			}
		} else { // Heroes
			// Scroll up/down
			livingsPanelIconScrollUpPoint.setLocation(
					livingsPanelPoint.x + LIVINGS_PANEL_WIDTH - tileLivingsPanel[3].getTileWidth()
							- tileScrollUp.getTileWidth(),
					livingsPanelPoint.y + tileLivingsPanel[1].getTileHeight() + 2 * tileIconLevelDown.getTileHeight());
			livingsPanelIconScrollDownPoint.setLocation(
					livingsPanelPoint.x + LIVINGS_PANEL_WIDTH - tileLivingsPanel[3].getTileWidth()
							- tileScrollUp.getTileWidth(),
					livingsPanelPoint.y + LIVINGS_PANEL_HEIGHT - tileLivingsPanel[1].getTileHeight()
							- tileScrollDown.getTileHeight());

			// Restrict points
			livingsPanelIconRestrictUpPoint.setLocation(
					livingsPanelIconScrollUpPoint.x - (tileIconLevelUp.getTileWidth() / 4) * 3,
					livingsPanelPoint.y + tileLivingsPanel[1].getTileHeight());
			livingsPanelIconRestrictDownPoint.setLocation(
					livingsPanelIconScrollUpPoint.x + tileScrollUp.getTileWidth()
							- (tileIconLevelDown.getTileWidth() / 4),
					livingsPanelPoint.y + tileLivingsPanel[1].getTileHeight());
		}

		// Pages
		int iSeparationScroll = livingsPanelIconScrollDownPoint.y
				- (livingsPanelIconScrollUpPoint.y + tileScrollUp.getTileHeight());
		livingsPanelPagesPoint.setLocation(livingsPanelIconScrollUpPoint.x + tileScrollUp.getTileWidth() / 2,
				livingsPanelIconScrollUpPoint.y + tileScrollUp.getTileHeight() + iSeparationScroll / 2
						- UtilFont.MAX_HEIGHT / 2);
		// Pages data
		if (livingsDataIndexPages == null) {
			livingsDataIndexPages = new int[3];
			livingsDataIndexPages[LIVINGS_PANEL_TYPE_CITIZENS] = 1;
			livingsDataIndexPages[LIVINGS_PANEL_TYPE_SOLDIERS] = 1;
			livingsDataIndexPages[LIVINGS_PANEL_TYPE_HEROES] = 1;

			livingsDataIndexPagesCitizenGroups = new int[CitizenGroups.MAX_GROUPS];
			for (int i = 0; i < CitizenGroups.MAX_GROUPS; i++) {
				livingsDataIndexPagesCitizenGroups[i] = 1;
			}

			livingsDataIndexPagesSoldierGroups = new int[SoldierGroups.MAX_GROUPS];
			for (int i = 0; i < SoldierGroups.MAX_GROUPS; i++) {
				livingsDataIndexPagesSoldierGroups[i] = 1;
			}
		}
	}

	public void createPrioritiesPanel() {
		PRIORITIES_PANEL_NUM_ITEMS = ActionPriorityManager.getPrioritiesListSize() + 1; // +1 para el back

		// Miramos la separación entre items
		int iPixelsBetweenItems;
		if (PRIORITIES_PANEL_NUM_ITEMS > 1) {
			iPixelsBetweenItems = PIXELS_TO_BORDER;
		} else {
			iPixelsBetweenItems = 0;
		}

		// Tenemos el tamaño de los items
		PRIORITIES_PANEL_WIDTH = PRIORITIES_PANEL_ITEM_SIZE + 2 * tilePrioritiesPanelUpIcon.getTileWidth();
		PRIORITIES_PANEL_HEIGHT = 2 * PIXELS_TO_BORDER + (PRIORITIES_PANEL_NUM_ITEMS * PRIORITIES_PANEL_ITEM_SIZE)
				+ ((PRIORITIES_PANEL_NUM_ITEMS - 1) * iPixelsBetweenItems);

		// Número de columnas para que quepa
		int MAX_ITEMS_PER_COLUMN = PRIORITIES_PANEL_NUM_ITEMS;
		int iNumColumns;
		int iMaxHeight = (bottomPanelY - PIXELS_TO_BORDER) - (20 + 2 * PIXELS_TO_BORDER);

		if (PRIORITIES_PANEL_NUM_ITEMS > 1 && PRIORITIES_PANEL_HEIGHT > iMaxHeight) {
			if (iMaxHeight - 2 * PIXELS_TO_BORDER != 0) { // Check división por 0
				iNumColumns = PRIORITIES_PANEL_HEIGHT / (iMaxHeight - 2 * PIXELS_TO_BORDER); // Realmente no entiendo el
																								// 2*PIXELS en esta
																								// operación
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

			PRIORITIES_PANEL_WIDTH = iNumColumns
					* (PRIORITIES_PANEL_ITEM_SIZE + 2 * tilePrioritiesPanelUpIcon.getTileWidth() + PIXELS_TO_BORDER);
			PRIORITIES_PANEL_HEIGHT = 2 * PIXELS_TO_BORDER + (MAX_ITEMS_PER_COLUMN * PRIORITIES_PANEL_ITEM_SIZE)
					+ ((MAX_ITEMS_PER_COLUMN - 1) * iPixelsBetweenItems);
		} else {
			iNumColumns = 1;
		}

		prioritiesPanelPoint.setLocation(renderWidth / 2 - PRIORITIES_PANEL_WIDTH / 2,
				renderHeight / 2 - PRIORITIES_PANEL_HEIGHT / 2);

		// Positions
		prioritiesPanelItemsPosition = new ArrayList<Point>();
		prioritiesPanelItemsUpPosition = new ArrayList<Point>();
		prioritiesPanelItemsDownPosition = new ArrayList<Point>();
		Point p;
		int iColumnCounter = 0, iColumnIndex = 0;
		for (int i = 0; i < PRIORITIES_PANEL_NUM_ITEMS; i++) {
			p = new Point(
					prioritiesPanelPoint.x + tilePrioritiesPanelUpIcon.getTileWidth()
							+ (iColumnIndex * (PRIORITIES_PANEL_ITEM_SIZE + 2 * ICON_WIDTH + iPixelsBetweenItems)),
					prioritiesPanelPoint.y + PIXELS_TO_BORDER + ((i - (iColumnIndex * MAX_ITEMS_PER_COLUMN))
							* (PRIORITIES_PANEL_ITEM_SIZE + iPixelsBetweenItems)));
			prioritiesPanelItemsPosition.add(p);
			if (i != (PRIORITIES_PANEL_NUM_ITEMS - 1)) {
				if (i > 0) {
					prioritiesPanelItemsUpPosition
							.add(new Point(p.x - ICON_WIDTH, p.y + PRIORITIES_PANEL_ITEM_SIZE / 2 - ICON_HEIGHT / 2));
				} else {
					prioritiesPanelItemsUpPosition.add(new Point(-1, -1));
				}
				if (i < (PRIORITIES_PANEL_NUM_ITEMS - 2)) {
					prioritiesPanelItemsDownPosition.add(new Point(p.x + PRIORITIES_PANEL_ITEM_SIZE,
							p.y + PRIORITIES_PANEL_ITEM_SIZE / 2 - ICON_HEIGHT / 2));
				} else {
					prioritiesPanelItemsDownPosition.add(new Point(-1, -1));
				}
			} else {
				prioritiesPanelItemsUpPosition.add(new Point(-1, -1));
				prioritiesPanelItemsDownPosition.add(new Point(-1, -1));
			}

			iColumnCounter++;
			if (iColumnCounter == MAX_ITEMS_PER_COLUMN) {
				iColumnCounter = 0;
				iColumnIndex++;
			}
		}
	}

	public static void deleteTradePanel() {
		tradePanelActive = false;
		tradePanel = null;
	}

	public static void closeTypingPanel() {
		if (typingPanel != null) {
			if (TypingPanel.TYPING_TYPE == TypingPanel.TYPE_RENAME_GROUP) {
				int iGroup = TypingPanel.TYPING_PARAMETER;
				if (iGroup >= 0 && iGroup < SoldierGroups.MAX_GROUPS) {
					Game.getWorld().getSoldierGroups().getGroup(iGroup).setName(TypingPanel.getNewText());
				}
			} else if (TypingPanel.TYPING_TYPE == TypingPanel.TYPE_RENAME_JOB_GROUP) {
				int iGroup = TypingPanel.TYPING_PARAMETER;
				if (iGroup >= 0 && iGroup < CitizenGroups.MAX_GROUPS) {
					Game.getWorld().getCitizenGroups().getGroup(iGroup).setName(TypingPanel.getNewText());
				}
			} else if (TypingPanel.TYPING_TYPE == TypingPanel.TYPE_ADD_TEXT_TO_ITEM) {
				// Miramos si el item existe
				Integer iItemID = Integer.valueOf(TypingPanel.TYPING_PARAMETER);
				if (World.getItems().containsKey(iItemID)) {
					// Existe
					ArrayList<String> alTexts = World.getItemsText().get(iItemID);
					if (alTexts == null) {
						alTexts = new ArrayList<String>();
					}
					alTexts.add(TypingPanel.getNewText());
					World.getItemsText().put(iItemID, alTexts);
				}
			}

			typingPanel = null;
		}
	}

	/**
	 * Limpia todos los datos (se usa cuando se sale de la partida y se va al menú
	 * principal)
	 */
	public static void clear() {
		currentMenu = null;
		bottomSubPanelMenu = null;
		menuPanelMenu = null;

		productionPanelActive = false;
		prioritiesPanelActive = false;
		tradePanelActive = false;
		tradePanel = null;

		ImagesPanel.clear();
		imagesPanel = null;
	}
}
