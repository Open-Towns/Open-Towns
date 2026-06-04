package xaos.panels.UI;

import xaos.campaign.TutorialFlow;
import xaos.panels.menus.SmartMenu;
import xaos.tiles.Tile;
import xaos.utils.UtilsGL;

import java.awt.Point;

public class BottomPanel {

	private static int ui(int value) {
		return UIScaler.ui(value);
	}

	private static void drawScaledButton(Tile tile, Point point, int width, int height) {
		UtilsGL.drawTexture(
				point.x,
				point.y,
				point.x + width,
				point.y + height,
				tile.getTileSetTexX0(),
				tile.getTileSetTexY0(),
				tile.getTileSetTexX1(),
				tile.getTileSetTexY1());
	}

	private static void drawScaledIcon(Tile tile, Point buttonPoint, int buttonWidth, int buttonHeight,
			boolean highlighted) {
		int iconInset = ui(6);

		int iconX = buttonPoint.x + iconInset;
		int iconY = buttonPoint.y + iconInset;
		int iconWidth = buttonWidth - (iconInset * 2);
		int iconHeight = buttonHeight - (iconInset * 2);

		if (iconWidth < 1) {
			iconWidth = 1;
		}

		if (iconHeight < 1) {
			iconHeight = 1;
		}

		UtilsGL.drawTexture(
				iconX,
				iconY,
				iconX + iconWidth,
				iconY + iconHeight,
				tile.getTileSetTexX0(),
				tile.getTileSetTexY0(),
				tile.getTileSetTexX1(),
				tile.getTileSetTexY1());
	}

	private static Point getScaledBottomItemPoint(int itemIndex, int bottomPanelX, int bottomPanelY) {
		Point originalPoint = UIPanelState.bottomPanelItemsPosition.get(itemIndex);

		int originalOffsetX = originalPoint.x - UIPanelState.bottomPanelX;
		int originalOffsetY = originalPoint.y - UIPanelState.bottomPanelY;

		return new Point(
				bottomPanelX + ui(originalOffsetX),
				bottomPanelY + ui(originalOffsetY));
	}

	private static Point getScaledBottomSubPanelPoint(int bottomPanelX, int bottomPanelY) {
		/*
		 * Keep the subpanel anchored relative to the bottom panel instead of scaling
		 * its
		 * absolute screen position.
		 */
		int originalOffsetX = UIPanelState.bottomSubPanelPoint.x - UIPanelState.bottomPanelX;
		int originalOffsetY = UIPanelState.bottomSubPanelPoint.y - UIPanelState.bottomPanelY;

		return new Point(
				bottomPanelX + ui(originalOffsetX),
				bottomPanelY + ui(originalOffsetY));
	}

	private static Point getScaledBottomSubPanelItemPoint(int itemIndex, Point bottomSubPanelPoint) {
		int originalOffsetX = UIPanelState.bottomSubPanelItemsPosition.get(itemIndex).x
				- UIPanelState.bottomSubPanelPoint.x;

		int originalOffsetY = UIPanelState.bottomSubPanelItemsPosition.get(itemIndex).y
				- UIPanelState.bottomSubPanelPoint.y;

		return new Point(
				bottomSubPanelPoint.x + ui(originalOffsetX),
				bottomSubPanelPoint.y + ui(originalOffsetY));
	}

	public static int renderPanel(int mouseX, int mouseY, int mousePanel, int iCurrentTexture) {
		/*
		 * BOTTOM PANEL
		 */

		int bottomPanelWidth = ui(UIPanelState.BOTTOM_PANEL_WIDTH);
		int bottomPanelHeight = ui(UIPanelState.BOTTOM_PANEL_HEIGHT);

		int bottomScrollWidth = ui(UIPanelState.BOTTOM_PANEL_SCROLL_WIDTH);
		int bottomItemWidth = ui(UIPanelState.BOTTOM_ITEM_WIDTH);
		int bottomItemHeight = ui(UIPanelState.BOTTOM_ITEM_HEIGHT);

		// Scale from the original centre, not from the original left edge.
		int originalPanelCenterX = UIPanelState.bottomPanelX + (UIPanelState.BOTTOM_PANEL_WIDTH / 2);

		int bottomPanelX = originalPanelCenterX - (bottomPanelWidth / 2);
		int bottomPanelY = UIPanelState.bottomPanelY - (bottomPanelHeight - UIPanelState.BOTTOM_PANEL_HEIGHT);

		int bottomPanelLeftScrollX = bottomPanelX;
		int bottomPanelRightScrollX = bottomPanelX + bottomPanelWidth - bottomScrollWidth;
		// Panel itself
		iCurrentTexture = UtilsGL.setTexture(UIPanelState.tileBottomPanel, iCurrentTexture);

		UtilsGL.drawTexture(
				bottomPanelX,
				bottomPanelY,
				bottomPanelX + bottomPanelWidth,
				bottomPanelY + bottomPanelHeight,
				UIPanelState.tileBottomPanel.getTileSetTexX0(),
				UIPanelState.tileBottomPanel.getTileSetTexY0(),
				UIPanelState.tileBottomPanel.getTileSetTexX1(),
				UIPanelState.tileBottomPanel.getTileSetTexY1());
		// Left scroll
		if (mousePanel == UIPanelState.MOUSE_BOTTOM_LEFT_SCROLL && UIPanelState.bottomPanelItemIndex > 0) {
			UtilsGL.drawTexture(
					bottomPanelLeftScrollX,
					bottomPanelY,
					bottomPanelLeftScrollX + bottomScrollWidth,
					bottomPanelY + bottomPanelHeight,
					UIPanelState.tileBottomScrollLeftON.getTileSetTexX0(),
					UIPanelState.tileBottomScrollLeftON.getTileSetTexY0(),
					UIPanelState.tileBottomScrollLeftON.getTileSetTexX1(),
					UIPanelState.tileBottomScrollLeftON.getTileSetTexY1());
		} else {
			UtilsGL.drawTexture(
					bottomPanelLeftScrollX,
					bottomPanelY,
					bottomPanelLeftScrollX + bottomScrollWidth,
					bottomPanelY + bottomPanelHeight,
					UIPanelState.tileBottomScrollLeft.getTileSetTexX0(),
					UIPanelState.tileBottomScrollLeft.getTileSetTexY0(),
					UIPanelState.tileBottomScrollLeft.getTileSetTexX1(),
					UIPanelState.tileBottomScrollLeft.getTileSetTexY1());
		}

		// Right scroll
		iCurrentTexture = UtilsGL.setTexture(UIPanelState.tileBottomScrollRight, iCurrentTexture);

		if (mousePanel == UIPanelState.MOUSE_BOTTOM_RIGHT_SCROLL
				&& (UIPanelState.bottomPanelItemIndex + UIPanelState.BOTTOM_PANEL_NUM_ITEMS) < UIPanelState.currentMenu
						.getItems().size()) {
			UtilsGL.drawTexture(
					bottomPanelRightScrollX,
					bottomPanelY,
					bottomPanelRightScrollX + bottomScrollWidth,
					bottomPanelY + bottomPanelHeight,
					UIPanelState.tileBottomScrollRightON.getTileSetTexX0(),
					UIPanelState.tileBottomScrollRightON.getTileSetTexY0(),
					UIPanelState.tileBottomScrollRightON.getTileSetTexX1(),
					UIPanelState.tileBottomScrollRightON.getTileSetTexY1());
		} else {
			UtilsGL.drawTexture(
					bottomPanelRightScrollX,
					bottomPanelY,
					bottomPanelRightScrollX + bottomScrollWidth,
					bottomPanelY + bottomPanelHeight,
					UIPanelState.tileBottomScrollRight.getTileSetTexX0(),
					UIPanelState.tileBottomScrollRight.getTileSetTexY0(),
					UIPanelState.tileBottomScrollRight.getTileSetTexX1(),
					UIPanelState.tileBottomScrollRight.getTileSetTexY1());
		}

		// BOTTOM PANEL Items
		int iItemBottomPanel;

		if (mousePanel == UIPanelState.MOUSE_BOTTOM_ITEMS) {
			iItemBottomPanel = UIPanelInputHandler.isMouseOnBottomItems(mouseX, mouseY);
		} else {
			iItemBottomPanel = -1;
		}

		Point point;

		for (int i = UIPanelState.bottomPanelItemIndex; i < UIPanelState.bottomPanelItemIndex
				+ UIPanelState.BOTTOM_PANEL_NUM_ITEMS; i++) {

			if (i >= UIPanelState.currentMenu.getItems().size()) {
				break;
			}

			int visibleIndex = i - UIPanelState.bottomPanelItemIndex;
			point = getScaledBottomItemPoint(visibleIndex, bottomPanelX, bottomPanelY);

			// Round button
			if (UIPanelState.currentMenu.getItems().get(i).getType() == SmartMenu.TYPE_MENU) {
				iCurrentTexture = UtilsGL.setTexture(UIPanelState.tileBottomItemSM, iCurrentTexture);

				if (UIPanelState.checkBlinkBottom
						&& TutorialFlow.currentBlinkBottom(UIPanelState.currentMenu.getItems().get(i).getID())) {
					UtilsGL.setColorRed();
					drawScaledButton(
							UIPanelState.tileBottomItemSM,
							point,
							bottomItemWidth,
							bottomItemHeight);
					UtilsGL.unsetColor();
				} else {
					drawScaledButton(
							UIPanelState.tileBottomItemSM,
							point,
							bottomItemWidth,
							bottomItemHeight);
				}
			} else {
				iCurrentTexture = UtilsGL.setTexture(UIPanelState.tileBottomItem, iCurrentTexture);

				if (UIPanelState.checkBlinkBottom
						&& TutorialFlow.currentBlinkBottom(UIPanelState.currentMenu.getItems().get(i).getID())) {
					UtilsGL.setColorRed();
					drawScaledButton(
							UIPanelState.tileBottomItem,
							point,
							bottomItemWidth,
							bottomItemHeight);
					UtilsGL.unsetColor();
				} else {
					drawScaledButton(
							UIPanelState.tileBottomItem,
							point,
							bottomItemWidth,
							bottomItemHeight);
				}
			}

			// UI icon
			Tile tile = UIPanelState.currentMenu.getItems().get(i).getIcon();

			if (tile != null && UIPanelState.currentMenu.getItems().get(i).getIconType() == SmartMenu.ICON_TYPE_UI) {
				iCurrentTexture = UtilsGL.setTexture(tile, iCurrentTexture);

				drawScaledIcon(
						tile,
						point,
						bottomItemWidth,
						bottomItemHeight,
						iItemBottomPanel == visibleIndex);
			}
		}

		/*
		 * BOTTOM SUBPANEL
		 */

		int iItemBottomSubPanel;

		if (mousePanel == UIPanelState.MOUSE_BOTTOM_SUBITEMS) {
			iItemBottomSubPanel = UIPanelInputHandler.isMouseOnBottomSubItems(mouseX, mouseY);
		} else {
			iItemBottomSubPanel = -1;
		}

		if (UIPanelState.bottomSubPanelMenu != null) {
			int bottomSubPanelWidth = ui(UIPanelState.BOTTOM_SUBPANEL_WIDTH);
			int bottomSubPanelHeight = ui(UIPanelState.BOTTOM_SUBPANEL_HEIGHT);
			Point bottomSubPanelPoint = getScaledBottomSubPanelPoint(bottomPanelX, bottomPanelY);

			// Pintamos el panel
			iCurrentTexture = UtilsGL.setTexture(UIPanelState.tileBottomSubPanel[0], iCurrentTexture);

			UIPanel.renderBackground(
					UIPanelState.tileBottomSubPanel,
					bottomSubPanelPoint,
					bottomSubPanelWidth,
					bottomSubPanelHeight);

			// Pintamos los items
			int iMenu;

			bucle1: for (int y = 0; y < UIPanelState.BOTTOM_SUBPANEL_NUM_ITEMS_Y; y++) {
				for (int x = 0; x < UIPanelState.BOTTOM_SUBPANEL_NUM_ITEMS_X; x++) {
					iMenu = (y * UIPanelState.BOTTOM_SUBPANEL_NUM_ITEMS_X) + x;

					if (iMenu >= UIPanelState.bottomSubPanelMenu.getItems().size()) {
						break bucle1;
					}

					point = getScaledBottomSubPanelItemPoint(iMenu, bottomSubPanelPoint);

					// Round button
					if (UIPanelState.bottomSubPanelMenu.getItems().get(iMenu).getType() == SmartMenu.TYPE_MENU) {
						iCurrentTexture = UtilsGL.setTexture(UIPanelState.tileBottomItemSM, iCurrentTexture);

						if (UIPanelState.checkBlinkBottom
								&& TutorialFlow.currentBlinkBottom(
										UIPanelState.bottomSubPanelMenu.getItems().get(iMenu).getID())) {
							UtilsGL.setColorRed();
							drawScaledButton(
									UIPanelState.tileBottomItemSM,
									point,
									bottomItemWidth,
									bottomItemHeight);
							UtilsGL.unsetColor();
						} else {
							drawScaledButton(
									UIPanelState.tileBottomItemSM,
									point,
									bottomItemWidth,
									bottomItemHeight);
						}
					} else {
						iCurrentTexture = UtilsGL.setTexture(UIPanelState.tileBottomItem, iCurrentTexture);

						if (UIPanelState.checkBlinkBottom
								&& TutorialFlow.currentBlinkBottom(
										UIPanelState.bottomSubPanelMenu.getItems().get(iMenu).getID())) {
							UtilsGL.setColorRed();
							drawScaledButton(
									UIPanelState.tileBottomItem,
									point,
									bottomItemWidth,
									bottomItemHeight);
							UtilsGL.unsetColor();
						} else {
							drawScaledButton(
									UIPanelState.tileBottomItem,
									point,
									bottomItemWidth,
									bottomItemHeight);
						}
					}

					// UI icon
					Tile tile = UIPanelState.bottomSubPanelMenu.getItems().get(iMenu).getIcon();

					if (tile != null
							&& UIPanelState.bottomSubPanelMenu.getItems().get(iMenu)
									.getIconType() == SmartMenu.ICON_TYPE_UI) {
						iCurrentTexture = UtilsGL.setTexture(tile, iCurrentTexture);

						drawScaledIcon(
								tile,
								point,
								bottomItemWidth,
								bottomItemHeight,
								iItemBottomSubPanel == iMenu);
					}
				}
			}
		}

		/*
		 * ITEMS
		 */

		// BOTTOM PANEL item icons
		for (int i = UIPanelState.bottomPanelItemIndex; i < UIPanelState.bottomPanelItemIndex
				+ UIPanelState.BOTTOM_PANEL_NUM_ITEMS; i++) {

			if (i >= UIPanelState.currentMenu.getItems().size()) {
				break;
			}

			int visibleIndex = i - UIPanelState.bottomPanelItemIndex;
			point = getScaledBottomItemPoint(visibleIndex, bottomPanelX, bottomPanelY);

			Tile tile = UIPanelState.currentMenu.getItems().get(i).getIcon();

			if (tile != null && UIPanelState.currentMenu.getItems().get(i).getIconType() == SmartMenu.ICON_TYPE_ITEM) {
				iCurrentTexture = UtilsGL.setTexture(tile, iCurrentTexture);

				drawScaledIcon(
						tile,
						point,
						bottomItemWidth,
						bottomItemHeight,
						iItemBottomPanel == visibleIndex);
			}
		}

		// BOTTOM SUBPANEL item icons
		if (UIPanelState.bottomSubPanelMenu != null) {
			Point bottomSubPanelPoint = getScaledBottomSubPanelPoint(bottomPanelX, bottomPanelY);

			int iMenu;
			Tile tile;

			bucle1: for (int y = 0; y < UIPanelState.BOTTOM_SUBPANEL_NUM_ITEMS_Y; y++) {
				for (int x = 0; x < UIPanelState.BOTTOM_SUBPANEL_NUM_ITEMS_X; x++) {
					iMenu = (y * UIPanelState.BOTTOM_SUBPANEL_NUM_ITEMS_X) + x;

					if (iMenu >= UIPanelState.bottomSubPanelMenu.getItems().size()) {
						break bucle1;
					}

					point = getScaledBottomSubPanelItemPoint(iMenu, bottomSubPanelPoint);

					tile = UIPanelState.bottomSubPanelMenu.getItems().get(iMenu).getIcon();

					if (tile != null
							&& UIPanelState.bottomSubPanelMenu.getItems().get(iMenu)
									.getIconType() == SmartMenu.ICON_TYPE_ITEM) {
						iCurrentTexture = UtilsGL.setTexture(tile, iCurrentTexture);
						drawScaledIcon(
								tile,
								point,
								bottomItemWidth,
								bottomItemHeight,
								iItemBottomSubPanel == iMenu);
					}
				}

			}

		}
		return iCurrentTexture;
	}
}