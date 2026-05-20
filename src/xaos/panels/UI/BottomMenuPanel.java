package xaos.panels.UI;

import xaos.campaign.TutorialFlow;
import xaos.panels.menus.SmartMenu;
import xaos.tiles.Tile;
import xaos.utils.UtilsGL;
import java.awt.Point;

public class BottomMenuPanel {
    public static int renderPanel(int mouseX, int mouseY, int mousePanel, int iCurrentTexture) {
		/*
		 * BOTTOM PANEL
		 */
		// Left scroll
		if (mousePanel == UIPanelState.MOUSE_BOTTOM_LEFT_SCROLL && UIPanelState.bottomPanelItemIndex > 0) {
			UtilsGL.drawTexture(UIPanelState.bottomPanelLeftScrollX, UIPanelState.bottomPanelY,
					UIPanelState.bottomPanelLeftScrollX + UIPanelState.BOTTOM_PANEL_SCROLL_WIDTH, UIPanelState.bottomPanelY + UIPanelState.BOTTOM_PANEL_HEIGHT,
					UIPanelState.tileBottomScrollLeftON.getTileSetTexX0(), UIPanelState.tileBottomScrollLeftON.getTileSetTexY0(),
					UIPanelState.tileBottomScrollLeftON.getTileSetTexX1(), UIPanelState.tileBottomScrollLeftON.getTileSetTexY1());
		} else {
			UtilsGL.drawTexture(UIPanelState.bottomPanelLeftScrollX, UIPanelState.bottomPanelY,
					UIPanelState.bottomPanelLeftScrollX + UIPanelState.BOTTOM_PANEL_SCROLL_WIDTH, UIPanelState.bottomPanelY + UIPanelState.  BOTTOM_PANEL_HEIGHT,
					UIPanelState.tileBottomScrollLeft.getTileSetTexX0(), UIPanelState.tileBottomScrollLeft.getTileSetTexY0(),
					UIPanelState.tileBottomScrollLeft.getTileSetTexX1(), UIPanelState.tileBottomScrollLeft.getTileSetTexY1());
		}

		// Right scroll
		iCurrentTexture = UtilsGL.setTexture(UIPanelState.tileBottomScrollRight, iCurrentTexture);
		if (mousePanel == UIPanelState.MOUSE_BOTTOM_RIGHT_SCROLL
				&& (UIPanelState.bottomPanelItemIndex + UIPanelState.BOTTOM_PANEL_NUM_ITEMS) < UIPanelState.currentMenu.getItems().size()) {
			UtilsGL.drawTexture(UIPanelState.bottomPanelRightScrollX, UIPanelState.bottomPanelY,
					UIPanelState.bottomPanelRightScrollX + UIPanelState.BOTTOM_PANEL_SCROLL_WIDTH, UIPanelState.bottomPanelY + UIPanelState.         BOTTOM_PANEL_HEIGHT,
					UIPanelState.tileBottomScrollRightON.getTileSetTexX0(), UIPanelState.tileBottomScrollRightON.getTileSetTexY0(),
					UIPanelState.tileBottomScrollRightON.getTileSetTexX1(), UIPanelState.tileBottomScrollRightON.getTileSetTexY1());
		} else {
			UtilsGL.drawTexture(UIPanelState.bottomPanelRightScrollX, UIPanelState.bottomPanelY,
					UIPanelState.bottomPanelRightScrollX + UIPanelState.BOTTOM_PANEL_SCROLL_WIDTH, UIPanelState.bottomPanelY + UIPanelState.         BOTTOM_PANEL_HEIGHT,
					UIPanelState.tileBottomScrollRight.getTileSetTexX0(), UIPanelState.tileBottomScrollRight.getTileSetTexY0(),
					UIPanelState.tileBottomScrollRight.getTileSetTexX1(), UIPanelState.tileBottomScrollRight.getTileSetTexY1());
		}

		// Panel itself
		iCurrentTexture = UtilsGL.setTexture(UIPanelState.tileBottomPanel, iCurrentTexture);
		UtilsGL.drawTexture(UIPanelState.bottomPanelX, UIPanelState.bottomPanelY, UIPanelState.bottomPanelX + UIPanelState.BOTTOM_PANEL_WIDTH,
				UIPanelState.bottomPanelY + UIPanelState.BOTTOM_PANEL_HEIGHT, UIPanelState.tileBottomPanel.getTileSetTexX0(),
				UIPanelState.tileBottomPanel.getTileSetTexY0(), UIPanelState.tileBottomPanel.getTileSetTexX1(),
				UIPanelState.tileBottomPanel.getTileSetTexY1());

		// BOTTOM PANEL Items
		int iItemBottomPanel;
		if (mousePanel == UIPanelState.MOUSE_BOTTOM_ITEMS) {
			iItemBottomPanel = UIPanelInputHandler.isMouseOnBottomItems(mouseX, mouseY);
		} else {
			iItemBottomPanel = -1;
		}

		// UI TEXTURE bottom panel
		Point point;
		for (int i = UIPanelState.bottomPanelItemIndex; i < UIPanelState.bottomPanelItemIndex + UIPanelState.BOTTOM_PANEL_NUM_ITEMS; i++) {
			if (i > UIPanelState.currentMenu.getItems().size()) {
				break;
			}

			point = UIPanelState.bottomPanelItemsPosition.get(i - UIPanelState.bottomPanelItemIndex);

			// Round button
			if (UIPanelState.currentMenu.getItems().get(i).getType() == SmartMenu.TYPE_MENU) {
				iCurrentTexture = UtilsGL.setTexture(UIPanelState.tileBottomItemSM, iCurrentTexture);
				if (UIPanelState.checkBlinkBottom && TutorialFlow.currentBlinkBottom(UIPanelState.currentMenu.getItems().get(i).getID())) {
					UtilsGL.setColorRed();
					UIPanel.drawTile(UIPanelState.tileBottomItemSM, point, UIPanelState.BOTTOM_ITEM_WIDTH, UIPanelState.BOTTOM_ITEM_HEIGHT,
							(iItemBottomPanel == (i - UIPanelState.bottomPanelItemIndex)));
					UtilsGL.unsetColor();
				} else {
					UIPanel.drawTile(UIPanelState.tileBottomItemSM, point, UIPanelState.BOTTOM_ITEM_WIDTH, UIPanelState.BOTTOM_ITEM_HEIGHT,
							(iItemBottomPanel == (i - UIPanelState.bottomPanelItemIndex)));
				}
			} else {
				iCurrentTexture = UtilsGL.setTexture(UIPanelState.tileBottomItem, iCurrentTexture);
				if (UIPanelState.checkBlinkBottom && TutorialFlow.currentBlinkBottom(UIPanelState.currentMenu.getItems().get(i).getID())) {
					UtilsGL.setColorRed();
					UIPanel.drawTile(UIPanelState.tileBottomItem, point, UIPanelState.BOTTOM_ITEM_WIDTH, UIPanelState.BOTTOM_ITEM_HEIGHT,
							(iItemBottomPanel == (i - UIPanelState.bottomPanelItemIndex)));
					UtilsGL.unsetColor();
				} else {
					UIPanel.drawTile(UIPanelState.tileBottomItem, point, UIPanelState.BOTTOM_ITEM_WIDTH, UIPanelState.BOTTOM_ITEM_HEIGHT,
							(iItemBottomPanel == (i - UIPanelState.bottomPanelItemIndex)));
				}
			}

			// Icono
			Tile tile = UIPanelState.currentMenu.getItems().get(i).getIcon();
			if (tile != null && UIPanelState.currentMenu.getItems().get(i).getIconType() == SmartMenu.ICON_TYPE_UI) {
				iCurrentTexture = UtilsGL.setTexture(tile, iCurrentTexture);
				UIPanel.drawTile(tile, point, UIPanelState.BOTTOM_ITEM_WIDTH, UIPanelState.BOTTOM_ITEM_HEIGHT,
						(iItemBottomPanel == (i - UIPanelState.bottomPanelItemIndex)));
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
			// Pintamos el panel
			iCurrentTexture = UtilsGL.setTexture(UIPanelState.tileBottomSubPanel[0], iCurrentTexture);
			UIPanel.renderBackground(UIPanelState.tileBottomSubPanel, UIPanelState.bottomSubPanelPoint, UIPanelState.BOTTOM_SUBPANEL_WIDTH, UIPanelState.BOTTOM_SUBPANEL_HEIGHT);
			// UtilsGL.drawTexture (bottomSubPanelX, bottomSubPanelY, bottomSubPanelX +
			// BOTTOM_SUBPANEL_WIDTH, bottomSubPanelY + BOTTOM_SUBPANEL_HEIGHT,
			// tileBottomSubPanel.getTileSetTexX0 (), tileBottomSubPanel.getTileSetTexY0 (),
			// tileBottomSubPanel.getTileSetTexX1 (), tileBottomSubPanel.getTileSetTexY1
			// ());

			// Pintamos los items
			int iMenu;
			bucle1: for (int y = 0; y < UIPanelState.BOTTOM_SUBPANEL_NUM_ITEMS_Y; y++) {
				for (int x = 0; x < UIPanelState.BOTTOM_SUBPANEL_NUM_ITEMS_X; x++) {
					iMenu = (y * UIPanelState.BOTTOM_SUBPANEL_NUM_ITEMS_X) + x;
					if (iMenu >= UIPanelState.bottomSubPanelMenu.getItems().size()) {
						break bucle1;
					}

					point = UIPanelState.bottomSubPanelItemsPosition.get(iMenu);
					// Round button
					if (UIPanelState.bottomSubPanelMenu.getItems().get(iMenu).getType() == SmartMenu.TYPE_MENU) {
						iCurrentTexture = UtilsGL.setTexture(UIPanelState.tileBottomItemSM, iCurrentTexture);
						if (UIPanelState.checkBlinkBottom
								&& TutorialFlow.currentBlinkBottom(UIPanelState.bottomSubPanelMenu.getItems().get(iMenu).getID())) {
							UtilsGL.setColorRed();
							UIPanel.drawTile(UIPanelState.tileBottomItemSM, point, UIPanelState.BOTTOM_ITEM_WIDTH, UIPanelState.BOTTOM_ITEM_HEIGHT,
									(iItemBottomSubPanel == iMenu));
							UtilsGL.unsetColor();
						} else {
							UIPanel.drawTile(UIPanelState.tileBottomItemSM, point, UIPanelState.BOTTOM_ITEM_WIDTH, UIPanelState.BOTTOM_ITEM_HEIGHT,
									(iItemBottomSubPanel == iMenu));
						}
					} else {
						iCurrentTexture = UtilsGL.setTexture(UIPanelState.tileBottomItem, iCurrentTexture);
						if (UIPanelState.checkBlinkBottom
								&& TutorialFlow.currentBlinkBottom(UIPanelState.bottomSubPanelMenu.getItems().get(iMenu).getID())) {
							UtilsGL.setColorRed();
							UIPanel.drawTile(UIPanelState.tileBottomItem, point, UIPanelState.BOTTOM_ITEM_WIDTH, UIPanelState.BOTTOM_ITEM_HEIGHT,
									(iItemBottomSubPanel == iMenu));
							UtilsGL.unsetColor();
						} else {
							UIPanel.drawTile(UIPanelState.tileBottomItem, point, UIPanelState.BOTTOM_ITEM_WIDTH, UIPanelState.BOTTOM_ITEM_HEIGHT,
									(iItemBottomSubPanel == iMenu));
						}
					}

					// Icono
					Tile tile = UIPanelState.bottomSubPanelMenu.getItems().get(iMenu).getIcon();
					if (tile != null
							&& UIPanelState.bottomSubPanelMenu.getItems().get(iMenu).getIconType() == SmartMenu.ICON_TYPE_UI) {
						iCurrentTexture = UtilsGL.setTexture(tile, iCurrentTexture);
						UIPanel.drawTile(tile, point, UIPanelState.BOTTOM_ITEM_WIDTH, UIPanelState.BOTTOM_ITEM_HEIGHT, (iItemBottomSubPanel == iMenu));
					}
				}
			}
		}

		/*
		 * ITEMS
		 */
		// BOTTOM PANEL
		for (int i = UIPanelState.bottomPanelItemIndex; i < UIPanelState.bottomPanelItemIndex + UIPanelState.BOTTOM_PANEL_NUM_ITEMS; i++) {
			if (i > UIPanelState.currentMenu.getItems().size()) {
				break;
			}

			point = UIPanelState.bottomPanelItemsPosition.get(i - UIPanelState.bottomPanelItemIndex);
			// Icono
			Tile tile = UIPanelState.currentMenu.getItems().get(i).getIcon();
			if (tile != null && UIPanelState.currentMenu.getItems().get(i).getIconType() == SmartMenu.ICON_TYPE_ITEM) {
				iCurrentTexture = UtilsGL.setTexture(tile, iCurrentTexture);
				UIPanel.drawTile(tile, point, UIPanelState.BOTTOM_ITEM_WIDTH, UIPanelState.BOTTOM_ITEM_HEIGHT,
						(iItemBottomPanel == (i - UIPanelState.bottomPanelItemIndex)));
			}
		}

		// BOTTOM SUBPANEL
		if (UIPanelState.bottomSubPanelMenu != null) {
			// Ahora los items
			int iMenu;
			Tile tile;
			bucle1: for (int y = 0; y < UIPanelState.BOTTOM_SUBPANEL_NUM_ITEMS_Y; y++) {
				for (int x = 0; x < UIPanelState.BOTTOM_SUBPANEL_NUM_ITEMS_X; x++) {
					iMenu = (y * UIPanelState.BOTTOM_SUBPANEL_NUM_ITEMS_X) + x;
					if (iMenu >= UIPanelState.bottomSubPanelMenu.getItems().size()) {
						break bucle1;
					}

					point = UIPanelState.bottomSubPanelItemsPosition.get(iMenu);
					// Icono
					tile = UIPanelState.bottomSubPanelMenu.getItems().get(iMenu).getIcon();
					if (tile != null
							&& UIPanelState.bottomSubPanelMenu.getItems().get(iMenu).getIconType() == SmartMenu.ICON_TYPE_ITEM) {
						iCurrentTexture = UtilsGL.setTexture(tile, iCurrentTexture);
						UIPanel.drawTile(tile, point, UIPanelState.BOTTOM_ITEM_WIDTH, UIPanelState.BOTTOM_ITEM_HEIGHT, (iItemBottomSubPanel == iMenu));
					}
				}
			}
		}

		return iCurrentTexture;
	}
}
