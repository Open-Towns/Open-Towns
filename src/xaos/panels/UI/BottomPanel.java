package xaos.panels.UI;

import static xaos.panels.UI.UIPanelScaler.*;

import xaos.campaign.TutorialFlow;
import xaos.panels.menus.SmartMenu;
import xaos.tiles.Tile;
import xaos.utils.UtilsGL;

import java.awt.Point;

public class BottomPanel {

	private static Point getOriginalBottomPanelPoint() {
		return new Point(UIPanelState.bottomPanelX, UIPanelState.bottomPanelY);
	}

	private static Point getScaledBottomItemPoint(int itemIndex, Point bottomPanelPoint) {
		return scalePointFromAnchor(
				UIPanelState.bottomPanelItemsPosition.get(itemIndex),
				getOriginalBottomPanelPoint(),
				bottomPanelPoint);
	}

	private static Point getScaledBottomSubPanelPoint(Point bottomPanelPoint) {
		return scalePointFromAnchor(
				UIPanelState.bottomSubPanelPoint,
				getOriginalBottomPanelPoint(),
				bottomPanelPoint);
	}

	private static Point getScaledBottomSubPanelItemPoint(int itemIndex, Point bottomSubPanelPoint) {
		return scalePointFromAnchor(
				UIPanelState.bottomSubPanelItemsPosition.get(itemIndex),
				UIPanelState.bottomSubPanelPoint,
				bottomSubPanelPoint);
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

		Point bottomPanelPoint = anchorFromCentreXAndBottom(
				getOriginalBottomPanelPoint(),
				UIPanelState.BOTTOM_PANEL_WIDTH,
				UIPanelState.BOTTOM_PANEL_HEIGHT,
				bottomPanelWidth,
				bottomPanelHeight);

		int bottomPanelX = bottomPanelPoint.x;
		int bottomPanelY = bottomPanelPoint.y;

		int bottomPanelLeftScrollX = bottomPanelX;
		int bottomPanelRightScrollX = bottomPanelX + bottomPanelWidth - bottomScrollWidth;

		// Panel itself
		iCurrentTexture = UtilsGL.setTexture(UIPanelState.tileBottomPanel, iCurrentTexture);

		drawScaledTile(
				UIPanelState.tileBottomPanel,
				bottomPanelPoint,
				bottomPanelWidth,
				bottomPanelHeight);

		// Left scroll
		if (mousePanel == UIPanelState.MOUSE_BOTTOM_LEFT_SCROLL && UIPanelState.bottomPanelItemIndex > 0) {
			iCurrentTexture = UtilsGL.setTexture(UIPanelState.tileBottomScrollLeftON, iCurrentTexture);

			drawScaledTile(
					UIPanelState.tileBottomScrollLeftON,
					new Point(bottomPanelLeftScrollX, bottomPanelY),
					bottomScrollWidth,
					bottomPanelHeight);
		} else {
			iCurrentTexture = UtilsGL.setTexture(UIPanelState.tileBottomScrollLeft, iCurrentTexture);

			drawScaledTile(
					UIPanelState.tileBottomScrollLeft,
					new Point(bottomPanelLeftScrollX, bottomPanelY),
					bottomScrollWidth,
					bottomPanelHeight);
		}

		// Right scroll
		if (mousePanel == UIPanelState.MOUSE_BOTTOM_RIGHT_SCROLL
				&& (UIPanelState.bottomPanelItemIndex + UIPanelState.BOTTOM_PANEL_NUM_ITEMS) < UIPanelState.currentMenu
						.getItems().size()) {
			iCurrentTexture = UtilsGL.setTexture(UIPanelState.tileBottomScrollRightON, iCurrentTexture);

			drawScaledTile(
					UIPanelState.tileBottomScrollRightON,
					new Point(bottomPanelRightScrollX, bottomPanelY),
					bottomScrollWidth,
					bottomPanelHeight);
		} else {
			iCurrentTexture = UtilsGL.setTexture(UIPanelState.tileBottomScrollRight, iCurrentTexture);

			drawScaledTile(
					UIPanelState.tileBottomScrollRight,
					new Point(bottomPanelRightScrollX, bottomPanelY),
					bottomScrollWidth,
					bottomPanelHeight);
		}

		// BOTTOM PANEL items
		int iItemBottomPanel;

		if (mousePanel == UIPanelState.MOUSE_BOTTOM_ITEMS) {
			iItemBottomPanel = UIPanelInputHandler.isMouseOnBottomItems(mouseX, mouseY);
		} else {
			iItemBottomPanel = -1;
		}

		Point point;

		for (int i = UIPanelState.bottomPanelItemIndex;
				i < UIPanelState.bottomPanelItemIndex + UIPanelState.BOTTOM_PANEL_NUM_ITEMS;
				i++) {

			if (i >= UIPanelState.currentMenu.getItems().size()) {
				break;
			}

			int visibleIndex = i - UIPanelState.bottomPanelItemIndex;
			point = getScaledBottomItemPoint(visibleIndex, bottomPanelPoint);

			// Round button
			if (UIPanelState.currentMenu.getItems().get(i).getType() == SmartMenu.TYPE_MENU) {
				iCurrentTexture = UtilsGL.setTexture(UIPanelState.tileBottomItemSM, iCurrentTexture);

				if (UIPanelState.checkBlinkBottom
						&& TutorialFlow.currentBlinkBottom(UIPanelState.currentMenu.getItems().get(i).getID())) {
					UtilsGL.setColorRed();

					drawScaledTile(
							UIPanelState.tileBottomItemSM,
							point,
							bottomItemWidth,
							bottomItemHeight);

					UtilsGL.unsetColor();
				} else {
					drawScaledTile(
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

					drawScaledTile(
							UIPanelState.tileBottomItem,
							point,
							bottomItemWidth,
							bottomItemHeight);

					UtilsGL.unsetColor();
				} else {
					drawScaledTile(
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
						bottomItemHeight);
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
			Point bottomSubPanelPoint = getScaledBottomSubPanelPoint(bottomPanelPoint);

			// Paint subpanel background
			iCurrentTexture = UtilsGL.setTexture(UIPanelState.tileBottomSubPanel[0], iCurrentTexture);

			UIPanel.renderBackground(
					UIPanelState.tileBottomSubPanel,
					bottomSubPanelPoint,
					bottomSubPanelWidth,
					bottomSubPanelHeight);

			// Paint subpanel item buttons
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

							drawScaledTile(
									UIPanelState.tileBottomItemSM,
									point,
									bottomItemWidth,
									bottomItemHeight);

							UtilsGL.unsetColor();
						} else {
							drawScaledTile(
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

							drawScaledTile(
									UIPanelState.tileBottomItem,
									point,
									bottomItemWidth,
									bottomItemHeight);

							UtilsGL.unsetColor();
						} else {
							drawScaledTile(
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
								bottomItemHeight);
					}
				}
			}
		}

		/*
		 * ITEMS
		 */

		// BOTTOM PANEL item icons
		for (int i = UIPanelState.bottomPanelItemIndex;
				i < UIPanelState.bottomPanelItemIndex + UIPanelState.BOTTOM_PANEL_NUM_ITEMS;
				i++) {

			if (i >= UIPanelState.currentMenu.getItems().size()) {
				break;
			}

			int visibleIndex = i - UIPanelState.bottomPanelItemIndex;
			point = getScaledBottomItemPoint(visibleIndex, bottomPanelPoint);

			Tile tile = UIPanelState.currentMenu.getItems().get(i).getIcon();

			if (tile != null && UIPanelState.currentMenu.getItems().get(i).getIconType() == SmartMenu.ICON_TYPE_ITEM) {
				iCurrentTexture = UtilsGL.setTexture(tile, iCurrentTexture);

				drawScaledIcon(
						tile,
						point,
						bottomItemWidth,
						bottomItemHeight);
			}
		}

		// BOTTOM SUBPANEL item icons
		if (UIPanelState.bottomSubPanelMenu != null) {
			Point bottomSubPanelPoint = getScaledBottomSubPanelPoint(bottomPanelPoint);

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
								bottomItemHeight);
					}
				}
			}
		}

		return iCurrentTexture;
	}
}