package xaos.panels.UI;

import static xaos.panels.UI.UIPanel.*;
import static xaos.panels.UI.UIPanelInputHandler.*;
import static xaos.panels.UI.UIPanelScaler.*;
import static xaos.panels.UI.UIPanelState.*;

import java.awt.Point;

import org.lwjgl.opengl.GL11;

import xaos.campaign.TutorialFlow;
import xaos.panels.menus.SmartMenu;
import xaos.tiles.Tile;
import xaos.utils.UtilsGL;

public class RightPanel {

	private static Point getScaledMenuPanelPoint() {
		return anchorFromRight(
				menuPanelPoint,
				MENU_PANEL_WIDTH,
				ui(MENU_PANEL_WIDTH));
	}

	private static Point getScaledMenuItemPoint(int itemIndex, Point scaledMenuPanelPoint) {
		return scalePointFromAnchor(
				menuPanelItemsPosition.get(itemIndex),
				menuPanelPoint,
				scaledMenuPanelPoint);
	}

	private static Point getScaledOpenCloseRightMenuPoint(Point scaledMenuPanelPoint) {
		return scalePointFromAnchor(
				tileOpenCloseRightMenuPoint,
				menuPanelPoint,
				scaledMenuPanelPoint);
	}

	public static void renderMenuPanel(int mouseX, int mouseY, int mousePanel) {
		checkBlinkRight = (blinkTurns >= MAX_BLINK_TURNS / 2) && TutorialFlow.isBlinkRight();

		int scaledMenuPanelWidth = ui(MENU_PANEL_WIDTH);
		int scaledMenuPanelHeight = ui(MENU_PANEL_HEIGHT);

		int scaledItemWidth = ui(BOTTOM_ITEM_WIDTH);
		int scaledItemHeight = ui(BOTTOM_ITEM_HEIGHT);

		Point scaledMenuPanelPoint = getScaledMenuPanelPoint();

		if (isMenuPanelActive()) {
			int iCurrentTexture = tileMenuPanel[0].getTextureID();

			GL11.glBindTexture(GL11.GL_TEXTURE_2D, iCurrentTexture);
			GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

			UtilsGL.glBegin(GL11.GL_QUADS);

			renderBackground(
					tileMenuPanel,
					scaledMenuPanelPoint,
					scaledMenuPanelWidth,
					scaledMenuPanelHeight);

			int iItemMenu;

			if (mousePanel == MOUSE_MENU_PANEL_ITEMS) {
				iItemMenu = isMouseOnMenuItems(mouseX, mouseY);
			} else {
				iItemMenu = -1;
			}

			/*
			 * Button backgrounds and UI icons
			 */
			if (menuPanelMenu != null) {
				int iMenu;
				Point point;

				bucle1: for (int y = 0; y < MENU_PANEL_NUM_ITEMS_Y; y++) {
					for (int x = 0; x < MENU_PANEL_NUM_ITEMS_X; x++) {
						iMenu = (y * MENU_PANEL_NUM_ITEMS_X) + x;

						if (iMenu >= menuPanelMenu.getItems().size()) {
							break bucle1;
						}

						point = getScaledMenuItemPoint(iMenu, scaledMenuPanelPoint);

						if (menuPanelMenu.getItems().get(iMenu).getType() == SmartMenu.TYPE_MENU) {
							iCurrentTexture = UtilsGL.setTexture(tileBottomItemSM, iCurrentTexture);

							if (checkBlinkRight
									&& TutorialFlow.currentBlinkRight(menuPanelMenu.getItems().get(iMenu).getID())) {
								UtilsGL.setColorRed();

								drawScaledTile(
										tileBottomItemSM,
										point,
										scaledItemWidth,
										scaledItemHeight);

								UtilsGL.unsetColor();
							} else {
								drawScaledTile(
										tileBottomItemSM,
										point,
										scaledItemWidth,
										scaledItemHeight);
							}
						} else {
							iCurrentTexture = UtilsGL.setTexture(tileBottomItem, iCurrentTexture);

							if (checkBlinkRight
									&& TutorialFlow.currentBlinkRight(menuPanelMenu.getItems().get(iMenu).getID())) {
								UtilsGL.setColorRed();

								drawScaledTile(
										tileBottomItem,
										point,
										scaledItemWidth,
										scaledItemHeight);

								UtilsGL.unsetColor();
							} else {
								drawScaledTile(
										tileBottomItem,
										point,
										scaledItemWidth,
										scaledItemHeight);
							}
						}

						Tile tile = menuPanelMenu.getItems().get(iMenu).getIcon();

						if (tile != null
								&& menuPanelMenu.getItems().get(iMenu).getIconType() == SmartMenu.ICON_TYPE_UI) {
							iCurrentTexture = UtilsGL.setTexture(tile, iCurrentTexture);

							drawScaledIcon(
									tile,
									point,
									scaledItemWidth,
									scaledItemHeight);
						}
					}
				}
			}

			/*
			 * Item icons
			 */
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

						point = getScaledMenuItemPoint(iMenu, scaledMenuPanelPoint);

						tile = menuPanelMenu.getItems().get(iMenu).getIcon();

						if (tile != null
								&& menuPanelMenu.getItems().get(iMenu).getIconType() == SmartMenu.ICON_TYPE_ITEM) {
							iCurrentTexture = UtilsGL.setTexture(tile, iCurrentTexture);

							drawScaledIcon(
									tile,
									point,
									scaledItemWidth,
									scaledItemHeight);
						}
					}
				}
			}

			UtilsGL.glEnd();
		}

		/*
		 * Open/close button
		 */
		Point openClosePoint = getScaledOpenCloseRightMenuPoint(scaledMenuPanelPoint);

		if (isMenuPanelLocked()) {
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, tileOpenRightMenuON.getTextureID());
			GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

			UtilsGL.glBegin(GL11.GL_QUADS);

			drawScaledTile(
					tileOpenRightMenuON,
					openClosePoint,
					ui(tileOpenRightMenuON.getTileWidth()),
					ui(tileOpenRightMenuON.getTileHeight()));

			UtilsGL.glEnd();
		} else {
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, tileOpenRightMenu.getTextureID());
			GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

			UtilsGL.glBegin(GL11.GL_QUADS);

			if (checkBlinkRight) {
				UtilsGL.setColorRed();
			}

			drawScaledTile(
					tileOpenRightMenu,
					openClosePoint,
					ui(tileOpenRightMenu.getTileWidth()),
					ui(tileOpenRightMenu.getTileHeight()));

			if (checkBlinkRight) {
				UtilsGL.unsetColor();
			}

			UtilsGL.glEnd();
		}
	}
}