package xaos.panels.UI;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.lwjgl.opengl.GL11;

import xaos.actions.ActionManager;
import xaos.actions.ActionManagerItem;
import xaos.actions.ActionPriorityManager;
import xaos.data.CaravanData;
import xaos.data.CitizenGroupData;
import xaos.data.CitizenGroups;
import xaos.data.EffectData;
import xaos.data.EquippedData;
import xaos.data.EventData;
import xaos.data.GlobalEventData;
import xaos.data.HeroData;
import xaos.data.SoldierGroupData;
import xaos.data.SoldierGroups;
import xaos.effects.EffectManager;
import xaos.events.EventManager;
import xaos.events.EventManagerItem;
import xaos.main.Game;
import xaos.main.World;
import xaos.panels.MainPanel;
import xaos.panels.MatsPanelData;
import xaos.panels.MessagesPanel;
import xaos.panels.menus.SmartMenu;
import xaos.panels.CommandPanel;
import xaos.tiles.Tile;
import xaos.tiles.entities.items.ItemManager;
import xaos.tiles.entities.items.ItemManagerItem;
import xaos.tiles.entities.items.military.MilitaryItem;
import xaos.tiles.entities.living.Citizen;
import xaos.tiles.entities.living.LivingEntity;
import xaos.tiles.entities.living.heroes.Hero;
import xaos.utils.ColorGL;
import xaos.utils.UIScale;
import xaos.utils.UtilsGL;
import xaos.utils.UtilsKeyboard;
import xaos.utils.CharDef;
import xaos.utils.UtilFont;
import xaos.utils.Messages;

import static xaos.panels.UI.UIPanel.*;
import static xaos.panels.UI.UIPanelState.*;
import static xaos.panels.UI.UIPanelInputHandler.*;

public final class TooltipRenderer {
	// private static SmartMenu currentMenu;
	private static String tooltip = null;
	// private static int tooltipX = 0, tooltipY = 0;
	private static Point tooltipPoint = null;

	private static final int[] BOTTOM_PANEL_KEYBINDS = {
			UtilsKeyboard.FN_BOT_1,
			UtilsKeyboard.FN_BOT_2,
			UtilsKeyboard.FN_BOT_3,
			UtilsKeyboard.FN_BOT_4,
			UtilsKeyboard.FN_BOT_5,
			UtilsKeyboard.FN_BOT_6,
			UtilsKeyboard.FN_BOT_7,
			UtilsKeyboard.FN_BOT_8,
			UtilsKeyboard.FN_BOT_9,
			UtilsKeyboard.FN_BOT_10
	};

	private TooltipRenderer() {
	}

	public static Point centeredAbove(int centerX, int bottomY, String text) {
		return new Point(
				centerX - UIScale.textWidth(text) / 2,
				bottomY - UIScale.fontHeight() * 2);
	}

	public static Point centeredBelow(int centerX, int topY, String text) {
		return new Point(
				centerX - UIScale.textWidth(text) / 2,
				topY + UIScale.fontHeight() * 2);
	}

	public static Point rightOf(int x, int y) {
		return new Point(x + UIScale.px(32), y);
	}

	public static Point leftOf(int x, int y, String text) {
		return new Point(x - UIScale.textWidth(text), y);
	}

	public static void draw(String tooltip, int tooltipX, int tooltipY, int renderWidth, int renderHeight) {
		if (tooltip == null) {
			return;
		}

		int paddingX = UIScale.px(6);
		int paddingY = UIScale.px(3);
		int safetyPadding = UIScale.px(4);

		int tooltipWidth = UIScale.textWidth(tooltip) + paddingX * 2 + safetyPadding;
		int tooltipHeight = UIScale.fontHeight() + paddingY * 2;

		Point tooltipPosition = clampTooltipPosition(
				tooltipX,
				tooltipY,
				tooltipWidth + paddingX * 2,
				tooltipHeight + paddingY * 2,
				renderWidth,
				renderHeight);

		tooltipX = tooltipPosition.x;
		tooltipY = tooltipPosition.y;
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, tileTooltipBackground.getTextureID());

		UtilsGL.glBegin(GL11.GL_QUADS);
		UtilsGL.drawTexture(
				tooltipX,
				tooltipY,
				tooltipX + tooltipWidth,
				tooltipY + tooltipHeight,
				tileTooltipBackground.getTileSetTexX0(),
				tileTooltipBackground.getTileSetTexY0(),
				tileTooltipBackground.getTileSetTexX1(),
				tileTooltipBackground.getTileSetTexY1());
		UtilsGL.glEnd();

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);

		UtilsGL.glBegin(GL11.GL_QUADS);
		drawScaledString(tooltip, tooltipX + paddingX, tooltipY + paddingY);
		UtilsGL.glEnd();

		GL11.glColor3f(1, 1, 1);
	}

	private static Point clampTooltipPosition(
			int x,
			int y,
			int width,
			int height,
			int renderWidth,
			int renderHeight) {
		int clampedX = x;
		int clampedY = y;

		if (clampedX < 0) {
			clampedX = 0;
		} else if (clampedX + width > renderWidth) {
			clampedX -= (clampedX + width) - renderWidth;
		}

		if (clampedY < 0) {
			clampedY = 0;
		} else if (clampedY + height > renderHeight) {
			clampedY -= (clampedY + height) - renderHeight;
		}

		if (clampedX < 0) {
			clampedX = 0;
		}

		if (clampedY < 0) {
			clampedY = 0;
		}

		return new Point(clampedX, clampedY);
	}

	public static void drawScaledString(String text, int x, int y) {
		if (text == null) {
			return;
		}

		int xOffset = x;

		for (int i = 0; i < text.length(); i++) {
			CharDef charDef = UtilFont.getCharDef(text.charAt(i));

			if (charDef == null) {
				continue;
			}

			UtilsGL.drawTexture(
					xOffset,
					y + UIScale.px(charDef.yoffset),
					xOffset + UIScale.px(charDef.width),
					y + UIScale.px(charDef.yoffset) + UIScale.px(charDef.height),
					charDef.xTex,
					charDef.yTex,
					charDef.xTex + charDef.widthTex,
					charDef.yTex + charDef.heightTex);

			xOffset += UIScale.px(charDef.xadvance);
		}
	}

	private static void setTooltip(int x, int y, String text) {
		tooltip = text;
		tooltipPoint = TooltipRenderer.rightOf(x, y);
	}

	public static void renderTooltips(int x, int y, int mousePanel) {
		tooltip = null;
		tooltipPoint = null;
		if (mousePanel == MOUSE_NONE || mousePanel == MOUSE_PRODUCTION_PANEL
				|| mousePanel == MOUSE_PRIORITIES_PANEL) {
			return;
		}

		if (typingPanel != null) {
			// TYPING PANEL

		} else {
			// TYPING PANEL NO ACTIVO

			// Bottom
			if (isBottomMenuPanelActive()) {
				bottomPanelTooltip(x, y, mousePanel);
			}

			// Right menu
			if (isMenuPanelActive() && mousePanel == MOUSE_MENU_PANEL_ITEMS) {
				rightPanelTooltip(x, y);

			}

			// Production
			if (tooltip == null && isProductionPanelActive()) {
				productionPanelTooltip(x, y);
			}

			// Priorities
			if (tooltip == null && isPrioritiesPanelActive() && mousePanel == MOUSE_PRIORITIES_PANEL_ITEMS) {

				prioritiesPanelTooltip(x, y);

			}

			// Trade
			if (tooltip == null && isTradePanelActive()) {
				tradePanelTooltip(x, y, mousePanel);
			}

			// Professions
			if (tooltip == null && isProfessionsPanelActive()) {
				professionsPanelTooltip(x, y, mousePanel);
			}

			// Pile
			if (tooltip == null && isPilePanelActive()) {
				pilePanelTooltip(x, y, mousePanel);
			}

			// Mats
			if (tooltip == null && isMatsPanelActive()) {
				matsPanelTooltip(x, y, mousePanel);
			}

			// Livings
			if (tooltip == null && isLivingsPanelActive()) {
				livingsPanelTooltip(x, y, mousePanel);
			}

			if (tooltip == null) {
				miscTooltips(x, y, mousePanel);
			}
		}

		if (tooltip != null && tooltipPoint != null) {
			TooltipRenderer.draw(tooltip, tooltipPoint.x, tooltipPoint.y, renderWidth,
					renderHeight);
		}

		if (typingPanel == null) {
			typingPanelTooltips(x, y, mousePanel);
		}
	}

	public static void bottomPanelTooltip(int x, int y, int mousePanel) {
		if (mousePanel == MOUSE_BOTTOM_ITEMS) {
			int itemIndex = isMouseOnBottomItems(x, y);
			SmartMenu item = currentMenu.getItems().get(itemIndex + bottomPanelItemIndex);
			tooltip = item.getName();
			int bottomPanelIndex = itemIndex + bottomPanelItemIndex;
			if (isMouseOnBottomItems(x, y) != -1 && item.getType() == SmartMenu.TYPE_ITEM
					&& bottomPanelIndex >= 0
					&& bottomPanelIndex < BOTTOM_PANEL_KEYBINDS.length) {
				tooltip += UtilsKeyboard.getTooltip(BOTTOM_PANEL_KEYBINDS[bottomPanelIndex]);
			}
		} else if (mousePanel == MOUSE_BOTTOM_SUBITEMS) {
			int itemIndex = isMouseOnBottomSubItems(x, y);
			SmartMenu item = bottomSubPanelMenu.getItems().get(itemIndex);
			if (itemIndex != -1 && item.getPrerequisites() != null && item.getPrerequisites().size() > 0) {

				MainPanel.renderMessages(x,
						bottomSubPanelPoint.y
								- (item.getPrerequisites().size() * (UIScale.fontHeight() + UIScale.px(5))),
						renderWidth, renderHeight,
						Tile.TERRAIN_ICON_WIDTH / 2,
						item.getPrerequisites(),
						item.getPrerequisitesColor());
			} else {
				tooltip = item.getName();
			}
		}

		if (tooltip != null) {
			tooltipPoint = TooltipRenderer.centeredAbove(x,
					mousePanel == MOUSE_BOTTOM_ITEMS ? bottomPanelY : bottomSubPanelPoint.y, tooltip);

		}
	}

	public static void rightPanelTooltip(int x, int y) {
		if (isMouseOnMenuItems(x, y) != -1) {
			SmartMenu item = menuPanelMenu.getItems().get(isMouseOnMenuItems(x, y));
			if (item.getPrerequisites() != null && item.getPrerequisites().size() > 0) {

				MainPanel.renderMessages(menuPanelPoint.x, y, renderWidth,
						renderHeight,
						Tile.TERRAIN_ICON_WIDTH / 2, item.getPrerequisites(), item.getPrerequisitesColor());
			} else {
				tooltip = item.getName();
				if (tooltip != null) {
					tooltipPoint = TooltipRenderer.leftOf(menuPanelPoint.x, y, tooltip);
				}
			}
		}
	}

	public static void productionPanelTooltip(int x, int y) {

		Point mouseOnProductionItems = isMouseOnProductionItems(x, y);
		if (mouseOnProductionItems != null) {
			SmartMenu panelItems = productionPanelMenu.getItems().get(mouseOnProductionItems.y);
			tooltipPoint = TooltipRenderer.rightOf(x, y);
			if (mouseOnProductionItems.x == MOUSE_PRODUCTION_PANEL_ITEMS) {
				if (panelItems.getPrerequisites() != null && panelItems.getPrerequisites().size() > 0) {
					if ((productionPanelPoint.x
							+ PRODUCTION_PANEL_WIDTH) > renderWidth) {
						MainPanel.renderMessages(renderWidth, y, renderWidth,
								renderHeight, 0,
								panelItems.getPrerequisites(), panelItems.getPrerequisitesColor());
					} else {
						MainPanel.renderMessages(
								productionPanelPoint.x + PRODUCTION_PANEL_WIDTH,
								y,
								renderWidth, renderHeight, 0,
								panelItems.getPrerequisites(),
								panelItems.getPrerequisitesColor());
					}
				} else {
					tooltip = panelItems.getName();

				}
			} else if (mouseOnProductionItems.x == MOUSE_PRODUCTION_PANEL_ITEMS_MINUS_AUTOMATED
					|| mouseOnProductionItems.x == MOUSE_PRODUCTION_PANEL_ITEMS_PLUS_AUTOMATED) {

				if (panelItems.getCommand().equals(CommandPanel.COMMAND_QUEUE)) {

					ActionManagerItem actionManagerItem = ActionManager.getItem(panelItems.getParameter());
					if (actionManagerItem != null) {
						if (actionManagerItem.isInverted()) {
							tooltip = Messages.getString("UIPanel.68"); //$NON-NLS-1$
						} else {
							tooltip = Messages.getString("UIPanel.72"); //$NON-NLS-1$
						}

					}
				}
			}

		}

	}

	public static void prioritiesPanelTooltip(int x, int y) {
		Point priotitiesItems = isMouseOnPrioritiesItems(x, y);
		if (priotitiesItems != null && priotitiesItems.x == MOUSE_PRIORITIES_PANEL_ITEMS) {
			if (priotitiesItems.y == (PRIORITIES_PANEL_NUM_ITEMS - 1)) {
				// Back
				tooltip = Messages.getString("UIPanel.13"); //$NON-NLS-1$
			} else {
				tooltip = ActionPriorityManager
						.getItem(ActionPriorityManager.getPrioritiesList().get(priotitiesItems.y))
						.getName();
			}
			tooltipPoint = TooltipRenderer.centeredBelow(
					prioritiesPanelPoint.x + PRIORITIES_PANEL_WIDTH,
					y,
					tooltip);

		}
	}

	public static void tradePanelTooltip(int x, int y, int mousePanel) {
		CaravanData caravanData = Game.getWorld().getCurrentCaravanData();
		boolean isTrading = caravanData != null
				&& caravanData.getStatus() == CaravanData.STATUS_TRADING;

		Point mouseOnTradeButtons = isMouseOnTradeButtons(x, y);

		if (!isTrading && mousePanel == MOUSE_TRADE_PANEL_BUTTONS_CARAVAN) {
			setTooltipFromTradeMenu(
					x,
					y,
					mouseOnTradeButtons,
					MOUSE_TRADE_PANEL_BUTTONS_CARAVAN,
					tradePanel.getMenuCaravan(),
					tradePanel.getIndexButtonsCaravan());
			return;
		}

		if (mousePanel == MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_CARAVAN && caravanData != null) {
			setTooltipFromTradeMenu(
					x,
					y,
					mouseOnTradeButtons,
					MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_CARAVAN,
					caravanData.getMenuCaravanToBuy(),
					tradePanel.getIndexButtonsToBuyCaravan());
			return;
		}

		if (!isTrading && mousePanel == MOUSE_TRADE_PANEL_BUTTONS_TOWN) {
			setTooltipFromTradeMenu(
					x,
					y,
					mouseOnTradeButtons,
					MOUSE_TRADE_PANEL_BUTTONS_TOWN,
					tradePanel.getMenuTown(),
					tradePanel.getIndexButtonsTown());
			return;
		}

		if (mousePanel == MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_TOWN && caravanData != null) {
			setTooltipFromTradeMenu(
					x,
					y,
					mouseOnTradeButtons,
					MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_TOWN,
					caravanData.getMenuTownToSell(),
					tradePanel.getIndexButtonsToSellTown());
			return;
		}

		if (mousePanel == MOUSE_TRADE_PANEL_BUTTONS_CLOSE) {
			setTooltip(x, y, Messages.getString("UIPanel.19")); //$NON-NLS-1$
			return;
		}

		if (mousePanel == MOUSE_TRADE_PANEL_ICON_BUY) {
			setTooltip(x, y, Messages.getString("UIPanel.33")); //$NON-NLS-1$
			return;
		}

		if (mousePanel == MOUSE_TRADE_PANEL_ICON_SELL) {
			setTooltip(x, y, Messages.getString("UIPanel.35")); //$NON-NLS-1$
			return;
		}

		if (!isTrading && mousePanel == MOUSE_TRADE_PANEL_BUTTONS_CONFIRM) {
			setTooltip(x, y, Messages.getString("UIPanel.36")); //$NON-NLS-1$
		}
	}

	private static void setTooltipFromTradeMenu(
			int x,
			int y,
			Point mouseOnTradeButtons,
			int expectedMousePanel,
			SmartMenu menu,
			int scrollIndex) {
		if (mouseOnTradeButtons == null || menu == null) {
			return;
		}

		if (mouseOnTradeButtons.x != expectedMousePanel) {
			return;
		}

		int itemIndex = mouseOnTradeButtons.y + scrollIndex;

		if (itemIndex < 0 || itemIndex >= menu.getItems().size()) {
			return;
		}

		setTooltip(x, y, menu.getItems().get(itemIndex).getName());
	}

	public static void professionsPanelTooltip(int x, int y, int mousePanel) {
		if (mousePanel == MOUSE_PROFESSIONS_PANEL_BUTTONS_ITEMS) {
			Point p = UIPanelInputHandler.isMouseOnProfessionsButtons(x, y);
			if (p != null && p.y > -1 && p.y < menuProfessions.getItems().size()) {
				String sName = menuProfessions.getItems().get(p.y).getName();
				if (sName != null) {
					tooltip = sName;
					tooltipPoint = TooltipRenderer.rightOf(x, y);

				}
			}
		} else if (mousePanel == MOUSE_PROFESSIONS_PANEL_BUTTONS_CLOSE) {
			tooltip = Messages.getString("UIPanel.19"); //$NON-NLS-1$
			tooltipPoint = TooltipRenderer.rightOf(x, y);

		}
	}

	public static void pilePanelTooltip(int x, int y, int mousePanel) {
		if (mousePanel == MOUSE_PILE_PANEL_BUTTONS_ITEMS) {
			Point p = UIPanelInputHandler.isMouseOnPileButtons(x, y);
			if (p != null && p.y > -1 && p.y < menuPile.getItems().size()) {
				String sName = menuPile.getItems().get(p.y).getName();
				if (sName != null) {
					tooltip = sName;
					tooltipPoint = TooltipRenderer.rightOf(x, y);

				}
			}
		} else if (mousePanel == MOUSE_PILE_PANEL_BUTTONS_CLOSE) {
			tooltip = Messages.getString("UIPanel.19"); //$NON-NLS-1$
			tooltipPoint = TooltipRenderer.rightOf(x, y);

		} else if (mousePanel == MOUSE_PILE_PANEL_BUTTONS_CONFIG_COPY) {
			if (UIPanelState.pilePanelIsContainer) {
				tooltip = Messages.getString("UIPanel.80"); //$NON-NLS-1$
			} else {
				tooltip = Messages.getString("UIPanel.82"); //$NON-NLS-1$
			}

		} else if (mousePanel == UIPanelState.MOUSE_PILE_PANEL_BUTTONS_CONFIG_LOCK) {
			if (UIPanelState.pilePanelIsLocked) {
				tooltip = Messages.getString("UIPanel.86"); //$NON-NLS-1$
			} else {
				tooltip = Messages.getString("UIPanel.85"); //$NON-NLS-1$
			}
			tooltipPoint = TooltipRenderer.rightOf(x, y);

		} else if (mousePanel == UIPanelState.MOUSE_PILE_PANEL_BUTTONS_CONFIG_LOCK_ALL) {
			tooltip = Messages.getString("UIPanel.87"); //$NON-NLS-1$
			tooltipPoint = TooltipRenderer.rightOf(x, y);

		} else if (mousePanel == UIPanelState.MOUSE_PILE_PANEL_BUTTONS_CONFIG_UNLOCK_ALL) {
			tooltip = Messages.getString("UIPanel.88"); //$NON-NLS-1$
			tooltipPoint = TooltipRenderer.rightOf(x, y);

		}
	}

	public static void matsPanelTooltip(int x, int y, int mousePanel) {
		if (mousePanel == UIPanelState.MOUSE_MATS_PANEL_BUTTONS_GROUPS) {
			Point p = UIPanelInputHandler.isMouseOnMatsButtons(x, y);
			if (p != null && p.y > -1 && p.y < MatsPanelData.nameGroups.size()) {
				tooltip = MatsPanelData.nameGroups.get(p.y);
				tooltipPoint = TooltipRenderer.rightOf(x, y);

			}
		} else if (mousePanel == UIPanelState.MOUSE_MATS_PANEL_BUTTONS_ITEMS) {
			Point p = UIPanelInputHandler.isMouseOnMatsButtons(x, y);
			if (p != null && p.y > -1
					&& p.y < MatsPanelData.tileGroups.get(UIPanelState.getMatsPanelActive()).size()) {
				String sIniHeader = MatsPanelData.tileGroups.get(UIPanelState.getMatsPanelActive()).get(p.y)
						.getIniHeader();
				if (sIniHeader != null) {
					ItemManagerItem imi = ItemManager.getItem(sIniHeader);
					if (imi != null && imi.getName() != null) {
						tooltip = imi.getName();
						tooltipPoint = TooltipRenderer.rightOf(x, y);

					}
				}
			}
		} else if (mousePanel == UIPanelState.MOUSE_MATS_PANEL_BUTTONS_CLOSE) {
			tooltip = Messages.getString("UIPanel.19"); //$NON-NLS-1$
			tooltipPoint = TooltipRenderer.rightOf(x, y);

		}
	}

	public static String getTooltipKey(int mousePanel) {
		return TOOLTIP_KEYS.get(mousePanel);
	}

	public static String getTooltipText(int mousePanel) {
		String key = getTooltipKey(mousePanel);

		if (key == null) {
			return null;
		}

		return Messages.getString(key);
	}

	private static final Map<Integer, String> TOOLTIP_KEYS = new HashMap<>();

	static {
		TOOLTIP_KEYS.put(MOUSE_LIVINGS_PANEL_BUTTONS_CLOSE, "UIPanel.19");
		TOOLTIP_KEYS.put(MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_PROFESSIONS, "UIPanel.63");
		TOOLTIP_KEYS.put(MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_JOBS_GROUPS_ADDREMOVE, "UIPanel.65");
		TOOLTIP_KEYS.put(MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER, "Citizen.27");
		TOOLTIP_KEYS.put(MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_CIVILIAN, "Citizen.26");
		TOOLTIP_KEYS.put(MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_GUARD, "Citizen.32");
		TOOLTIP_KEYS.put(MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_PATROL, "Citizen.34");
		TOOLTIP_KEYS.put(MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_BOSS, "Citizen.35");
		TOOLTIP_KEYS.put(MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_AUTOEQUIP, "UIPanel.43");
		TOOLTIP_KEYS.put(MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_RENAME, "UIPanel.54");
		TOOLTIP_KEYS.put(MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_AUTOEQUIP, "UIPanel.59");
		TOOLTIP_KEYS.put(MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_DISBAND, "UIPanel.60");
		TOOLTIP_KEYS.put(MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_CHANGE_JOBS, "UIPanel.69");
		TOOLTIP_KEYS.put(MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_SGROUP_ADD, "UIPanel.47");
		TOOLTIP_KEYS.put(MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_SGROUP_REMOVE, "UIPanel.51");
		TOOLTIP_KEYS.put(MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_RENAME, "UIPanel.54");
		TOOLTIP_KEYS.put(MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_GUARD, "UIPanel.55");
		TOOLTIP_KEYS.put(MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_PATROL, "UIPanel.57");
		TOOLTIP_KEYS.put(MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_BOSS, "UIPanel.58");
		TOOLTIP_KEYS.put(MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_AUTOEQUIP, "UIPanel.59");
		TOOLTIP_KEYS.put(MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_DISBAND, "UIPanel.60");

	}

	public static void livingsPanelTooltip(int x, int y, int mousePanel) {
		if (TOOLTIP_KEYS.containsKey(mousePanel)) {
			tooltip = getTooltipText(mousePanel);
			tooltipPoint = TooltipRenderer.rightOf(x, y);

		} else if ((mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_UP
				|| mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_DOWN)
				&& livingsPanelCitizensGroupActive == -1
				&& getLivingsPanelActive() == LIVINGS_PANEL_TYPE_CITIZENS) {

			tooltip = Messages.getString("UIPanel.73"); //$NON-NLS-1$

			tooltipPoint = new Point(
					x - UIScale.textWidth(tooltip) / 2,
					livingsPanelIconRestrictUpPoint.y + tileIconLevelUp.getTileHeight());

		} else if ((mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_UP
				|| mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_DOWN)
				&& getLivingsPanelActive() == LIVINGS_PANEL_TYPE_HEROES) {

			tooltip = Messages.getString("UIPanel.74"); //$NON-NLS-1$

			tooltipPoint = new Point(
					x - UIScale.textWidth(tooltip) / 2,
					livingsPanelIconRestrictUpPoint.y + tileIconLevelUp.getTileHeight());

		} else if (mousePanel == MOUSE_LIVINGS_PANEL_CGROUP_NOGROUP) {

			tooltip = Messages.getString("UIPanel.66") + " (" //$NON-NLS-1$ //$NON-NLS-2$
					+ Game.getWorld().getCitizenGroups().getCitizensWithoutGroup().size() + ")"; //$NON-NLS-1$

			tooltipPoint = new Point(
					x - UIScale.textWidth(tooltip) / 2,
					y - UIScale.fontHeight() - UIScale.px(2));

		} else if (mousePanel == MOUSE_LIVINGS_PANEL_CGROUP_GROUP) {

			Point p = UIPanelInputHandler.isMouseOnLivingsButtons(x, y);

			if (p != null && p.y >= 0 && p.y < CitizenGroups.MAX_GROUPS) {
				CitizenGroupData cgd = Game.getWorld().getCitizenGroups().getGroup(p.y);

				if (cgd != null) {
					tooltip = cgd.getName() + " (" + cgd.getLivingIDs().size() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
					tooltipPoint = TooltipRenderer.rightOf(x, y);
				}
			}

		} else if (mousePanel == MOUSE_LIVINGS_PANEL_SGROUP_NOGROUP) {

			tooltip = Messages.getString("UIPanel.53") + " (" //$NON-NLS-1$ //$NON-NLS-2$
					+ Game.getWorld().getSoldierGroups().getSoldiersWithoutGroup().size() + ")"; //$NON-NLS-1$

			tooltipPoint = new Point(
					x - UIScale.textWidth(tooltip) / 2,
					y - UIScale.fontHeight() - UIScale.px(2));

		} else if (mousePanel == MOUSE_LIVINGS_PANEL_SGROUP_GROUP) {

			Point p = UIPanelInputHandler.isMouseOnLivingsButtons(x, y);

			if (p != null && p.y >= 0 && p.y < SoldierGroups.MAX_GROUPS) {
				SoldierGroupData sgd = Game.getWorld().getSoldierGroups().getGroup(p.y);

				if (sgd != null) {
					tooltip = sgd.getName() + " (" + sgd.getLivingIDs().size() + ")"; //$NON-NLS-1$ //$NON-NLS-2$

					tooltipPoint = new Point(
							x - UIScale.textWidth(tooltip) / 2,
							y - UIScale.fontHeight() - UIScale.px(2));
				}
			}
		} else if (mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS
				|| mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_HEAD
				|| mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_BODY
				|| mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_LEGS
				|| mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_FEET
				|| mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_WEAPON) {
			livingPlayerEntitiesTooltip(x, y, mousePanel);
		}
	}

	private static final Map<Integer, Function<String, Point>> TOOLTIP_POINTS = new HashMap<>();

	static {
		TOOLTIP_POINTS.put(MOUSE_DATEPANEL, tooltip -> new Point(
				datePanelPoint.x + tileDatePanel.getTileWidth() / 2
						- UIScale.textWidth(tooltip) / 2,
				datePanelPoint.y + tileDatePanel.getTileHeight()));

		TOOLTIP_POINTS.put(MOUSE_ICON_LEVEL_UP, tooltip -> new Point(
				iconLevelUpPoint.x + tileIconLevelUp.getTileWidth() / 2
						- UIScale.textWidth(tooltip) / 2,
				iconLevelUpPoint.y - UIScale.fontHeight()));

		TOOLTIP_POINTS.put(MOUSE_ICON_LEVEL_DOWN, tooltip -> new Point(
				iconLevelDownPoint.x + tileIconLevelDown.getTileWidth() / 2
						- UIScale.textWidth(tooltip) / 2,
				iconLevelDownPoint.y + UIScale.fontHeight() * 2));

		TOOLTIP_POINTS.put(MOUSE_ICON_LEVEL, tooltip -> new Point(
				iconLevelPoint.x + tileIconLevel.getTileWidth() / 2
						- UIScale.textWidth(tooltip) / 2,
				iconLevelPoint.y + UIScale.fontHeight() * 2));

		TOOLTIP_POINTS.put(MOUSE_ICON_CITIZEN_PREVIOUS, tooltip -> new Point(
				iconCitizenPreviousPoint.x + tileIconCitizenPrevious.getTileWidth() / 2
						- UIScale.textWidth(tooltip) / 2,
				iconCitizenPreviousPoint.y + tileBottomItem.getTileHeight()));

		TOOLTIP_POINTS.put(MOUSE_ICON_CITIZEN_NEXT, tooltip -> new Point(
				iconCitizenNextPoint.x + tileIconCitizenNext.getTileWidth() / 2
						- UIScale.textWidth(tooltip) / 2,
				iconCitizenNextPoint.y + tileBottomItem.getTileHeight()));

		TOOLTIP_POINTS.put(MOUSE_ICON_SOLDIER_PREVIOUS, tooltip -> new Point(
				iconSoldierPreviousPoint.x + tileIconSoldierPrevious.getTileWidth() / 2
						- UIScale.textWidth(tooltip) / 2,
				iconSoldierPreviousPoint.y + tileBottomItem.getTileHeight()));

		TOOLTIP_POINTS.put(MOUSE_ICON_SOLDIER_NEXT, tooltip -> new Point(
				iconSoldierNextPoint.x + tileIconSoldierNext.getTileWidth() / 2
						- UIScale.textWidth(tooltip) / 2,
				iconSoldierNextPoint.y + tileBottomItem.getTileHeight()));

		TOOLTIP_POINTS.put(MOUSE_ICON_HERO_PREVIOUS, tooltip -> new Point(
				iconHeroPreviousPoint.x + tileIconHeroPrevious.getTileWidth() / 2
						- UIScale.textWidth(tooltip) / 2,
				iconHeroPreviousPoint.y + tileBottomItem.getTileHeight()));

		TOOLTIP_POINTS.put(MOUSE_ICON_HERO_NEXT, tooltip -> new Point(
				iconHeroNextPoint.x + tileIconHeroNext.getTileWidth() / 2
						- UIScale.textWidth(tooltip) / 2,
				iconHeroNextPoint.y + tileBottomItem.getTileHeight()));

		TOOLTIP_POINTS.put(MOUSE_INFO_NUM_SOLDIERS, tooltip -> new Point(
				iconNumSoldiersBackgroundPoint.x
						+ tileBottomItem.getTileWidth() / 2
						- UIScale.textWidth(tooltip) / 2,
				iconNumSoldiersBackgroundPoint.y
						+ tileBottomItem.getTileHeight()));

		TOOLTIP_POINTS.put(MOUSE_INFO_NUM_HEROES, tooltip -> new Point(
				iconNumHeroesBackgroundPoint.x
						+ tileBottomItem.getTileWidth() / 2
						- UIScale.textWidth(tooltip) / 2,
				iconNumHeroesBackgroundPoint.y
						+ tileBottomItem.getTileHeight()));

		TOOLTIP_POINTS.put(MOUSE_INFO_CARAVAN, tooltip -> new Point(
				iconCaravanBackgroundPoint.x
						+ tileBottomItem.getTileWidth() / 2
						- UIScale.textWidth(tooltip) / 2,
				iconCaravanBackgroundPoint.y
						+ tileBottomItem.getTileHeight()));

		TOOLTIP_POINTS.put(MOUSE_ICON_PRIORITIES, tooltip -> new Point(
				iconPrioritiesPoint.x
						+ tileIconPriorities.getTileWidth() / 2
						- UIScale.textWidth(tooltip) / 2,
				iconPrioritiesPoint.y + UIScale.fontHeight() * 2));

		TOOLTIP_POINTS.put(MOUSE_ICON_MATS, tooltip -> new Point(
				iconMatsPoint.x
						+ tileIconMats.getTileWidth() / 2
						- UIScale.textWidth(tooltip) / 2,
				iconMatsPoint.y + UIScale.fontHeight() * 2));

		TOOLTIP_POINTS.put(MOUSE_ICON_GRID, tooltip -> new Point(
				iconGridPoint.x
						+ tileIconGrid.getTileWidth() / 2
						- UIScale.textWidth(tooltip) / 2,
				iconGridPoint.y + UIScale.fontHeight() * 2));

		TOOLTIP_POINTS.put(MOUSE_ICON_MINIBLOCKS, tooltip -> new Point(
				iconMiniblocksPoint.x
						+ tileIconMiniblocks.getTileWidth() / 2
						- UIScale.textWidth(tooltip) / 2,
				iconMiniblocksPoint.y + UIScale.fontHeight() * 2));

		TOOLTIP_POINTS.put(MOUSE_ICON_FLATMOUSE, tooltip -> new Point(
				iconFlatMousePoint.x
						+ tileIconFlatMouse.getTileWidth() / 2
						- UIScale.textWidth(tooltip) / 2,
				iconFlatMousePoint.y + UIScale.fontHeight() * 2));

		TOOLTIP_POINTS.put(MOUSE_ICON_3DMOUSE, tooltip -> new Point(
				icon3DMousePoint.x
						+ tileIcon3DMouse.getTileWidth() / 2
						- UIScale.textWidth(tooltip) / 2,
				icon3DMousePoint.y + UIScale.fontHeight() * 2));

		TOOLTIP_POINTS.put(MOUSE_ICON_PAUSE_RESUME, tooltip -> new Point(
				iconPauseResumePoint.x
						+ tileIconPause.getTileWidth() / 2
						- UIScale.textWidth(tooltip) / 2,
				iconPauseResumePoint.y + UIScale.fontHeight() * 2));

		TOOLTIP_POINTS.put(MOUSE_ICON_SETTINGS, tooltip -> new Point(
				iconSettingsPoint.x
						+ tileIconSettings.getTileWidth() / 2
						- UIScale.textWidth(tooltip) / 2,
				iconSettingsPoint.y + UIScale.fontHeight() * 2));

		TOOLTIP_POINTS.put(MOUSE_ICON_LOWER_SPEED, tooltip -> new Point(
				iconLowerSpeedPoint.x
						+ tileIconLowerSpeed.getTileWidth() / 2
						- UIScale.textWidth(tooltip) / 2,
				iconLowerSpeedPoint.y + UIScale.fontHeight() * 2));

		TOOLTIP_POINTS.put(MOUSE_ICON_INCREASE_SPEED, tooltip -> new Point(
				iconIncreaseSpeedPoint.x
						+ tileIconIncreaseSpeed.getTileWidth() / 2
						- UIScale.textWidth(tooltip) / 2,
				iconIncreaseSpeedPoint.y + UIScale.fontHeight() * 2));
	}
	private static final Map<Integer, Supplier<String>> TOOLTIP_MESSAGES = new HashMap<>();

	static {
		TOOLTIP_MESSAGES.put(MOUSE_DATEPANEL,
				() -> Messages.getString("UIPanel.29")); //$NON-NLS-1$

		TOOLTIP_MESSAGES.put(MOUSE_ICON_LEVEL_UP,
				() -> Messages.getString("UIPanel.0") //$NON-NLS-1$
						+ UtilsKeyboard.getTooltip(UtilsKeyboard.FN_LEVEL_UP));

		TOOLTIP_MESSAGES.put(MOUSE_ICON_LEVEL_DOWN,
				() -> Messages.getString("UIPanel.2") //$NON-NLS-1$
						+ UtilsKeyboard.getTooltip(UtilsKeyboard.FN_LEVEL_DOWN));

		TOOLTIP_MESSAGES.put(MOUSE_ICON_LEVEL,
				() -> Messages.getString("UIPanel.30")); //$NON-NLS-1$

		TOOLTIP_MESSAGES.put(MOUSE_ICON_CITIZEN_PREVIOUS,
				() -> Messages.getString("UIPanel.3") //$NON-NLS-1$
						+ UtilsKeyboard.getTooltip(UtilsKeyboard.FN_PREVIOUS_CITIZEN));

		TOOLTIP_MESSAGES.put(MOUSE_ICON_CITIZEN_NEXT,
				() -> Messages.getString("UIPanel.4") //$NON-NLS-1$
						+ UtilsKeyboard.getTooltip(UtilsKeyboard.FN_NEXT_CITIZEN));

		TOOLTIP_MESSAGES.put(MOUSE_ICON_SOLDIER_PREVIOUS,
				() -> Messages.getString("UIPanel.5") //$NON-NLS-1$
						+ UtilsKeyboard.getTooltip(UtilsKeyboard.FN_PREVIOUS_SOLDIER));

		TOOLTIP_MESSAGES.put(MOUSE_ICON_SOLDIER_NEXT,
				() -> Messages.getString("UIPanel.6") //$NON-NLS-1$
						+ UtilsKeyboard.getTooltip(UtilsKeyboard.FN_NEXT_SOLDIER));

		TOOLTIP_MESSAGES.put(MOUSE_ICON_HERO_PREVIOUS,
				() -> Messages.getString("UIPanel.22") //$NON-NLS-1$
						+ UtilsKeyboard.getTooltip(UtilsKeyboard.FN_PREVIOUS_HERO));

		TOOLTIP_MESSAGES.put(MOUSE_ICON_HERO_NEXT,
				() -> Messages.getString("UIPanel.23") //$NON-NLS-1$
						+ UtilsKeyboard.getTooltip(UtilsKeyboard.FN_NEXT_HERO));

		TOOLTIP_MESSAGES.put(MOUSE_INFO_NUM_SOLDIERS,
				() -> Messages.getString("UIPanel.9")); //$NON-NLS-1$

		TOOLTIP_MESSAGES.put(MOUSE_INFO_NUM_HEROES,
				() -> Messages.getString("UIPanel.24")); //$NON-NLS-1$

		TOOLTIP_MESSAGES.put(MOUSE_INFO_CARAVAN,
				() -> Messages.getString("UIPanel.25") //$NON-NLS-1$
						+ UtilsKeyboard.getTooltip(UtilsKeyboard.FN_SHOW_TRADE));

		TOOLTIP_MESSAGES.put(MOUSE_ICON_PRIORITIES,
				() -> Messages.getString("UIPanel.14") //$NON-NLS-1$
						+ UtilsKeyboard.getTooltip(UtilsKeyboard.FN_SHOW_PRIORITIES));

		TOOLTIP_MESSAGES.put(MOUSE_ICON_MATS,
				() -> Messages.getString("UIPanel.32") //$NON-NLS-1$
						+ UtilsKeyboard.getTooltip(UtilsKeyboard.FN_SHOW_STOCK));

		TOOLTIP_MESSAGES.put(MOUSE_ICON_GRID,
				() -> Messages.getString("UIPanel.12") //$NON-NLS-1$
						+ UtilsKeyboard.getTooltip(UtilsKeyboard.FN_TOGGLE_GRID));

		TOOLTIP_MESSAGES.put(MOUSE_ICON_MINIBLOCKS,
				() -> Messages.getString("UIPanel.16") //$NON-NLS-1$
						+ UtilsKeyboard.getTooltip(UtilsKeyboard.FN_TOGGLE_MINIBLOCKS));

		TOOLTIP_MESSAGES.put(MOUSE_ICON_FLATMOUSE,
				() -> Messages.getString("UIPanel.45") //$NON-NLS-1$
						+ UtilsKeyboard.getTooltip(UtilsKeyboard.FN_TOGGLE_FLAT_MOUSE));

		TOOLTIP_MESSAGES.put(MOUSE_ICON_3DMOUSE,
				() -> Messages.getString("UtilsKeyboard.16") //$NON-NLS-1$
						+ UtilsKeyboard.getTooltip(UtilsKeyboard.FN_TOGGLE_3D_MOUSE));

		TOOLTIP_MESSAGES.put(MOUSE_ICON_PAUSE_RESUME,
				() -> Messages.getString("UIPanel.10") //$NON-NLS-1$
						+ UtilsKeyboard.getTooltip(UtilsKeyboard.FN_PAUSE));

		TOOLTIP_MESSAGES.put(MOUSE_ICON_SETTINGS,
				() -> Messages.getString("UIPanel.11")); //$NON-NLS-1$

		TOOLTIP_MESSAGES.put(MOUSE_ICON_LOWER_SPEED,
				() -> Messages.getString("UIPanel.1") //$NON-NLS-1$
						+ UtilsKeyboard.getTooltip(UtilsKeyboard.FN_SPEED_DOWN));

		TOOLTIP_MESSAGES.put(MOUSE_ICON_INCREASE_SPEED,
				() -> Messages.getString("UIPanel.15") //$NON-NLS-1$
						+ UtilsKeyboard.getTooltip(UtilsKeyboard.FN_SPEED_UP));

		TOOLTIP_MESSAGES.put(MOUSE_TUTORIAL_ICON,
				() -> Messages.getString("UIPanel.75") //$NON-NLS-1$
						+ UtilsKeyboard.getTooltip(UtilsKeyboard.FN_SHOW_MISSION));
	}

	private static boolean renderMessageIconTooltipIfNeeded(int mousePanel) {
		if (mousePanel == MOUSE_MESSAGES_ICON_ANNOUNCEMENT) {
			renderMessageIconTooltip(
					messageIconPoints[0],
					Messages.getString("UIPanel.26"),
					MessagesPanel.TYPE_ANNOUNCEMENT);
			return true;
		}

		if (mousePanel == MOUSE_MESSAGES_ICON_COMBAT) {
			renderMessageIconTooltip(
					messageIconPoints[1],
					Messages.getString("UIPanel.27"),
					MessagesPanel.TYPE_COMBAT);
			return true;
		}

		if (mousePanel == MOUSE_MESSAGES_ICON_HEROES) {
			renderMessageIconTooltip(
					messageIconPoints[2],
					Messages.getString("UIPanel.28"),
					MessagesPanel.TYPE_HEROES);
			return true;
		}

		if (mousePanel == MOUSE_MESSAGES_ICON_SYSTEM) {
			renderMessageIconTooltip(
					messageIconPoints[3],
					Messages.getString("UIPanel.31"),
					MessagesPanel.TYPE_SYSTEM);
			return true;
		}

		return false;
	}

	private static void renderMessageIconTooltip(
			Point iconPoint,
			String title,
			int messageType) {
		ArrayList<String> alMessages = new ArrayList<String>(4);
		ArrayList<ColorGL> alColors = new ArrayList<ColorGL>(4);

		alMessages.add(title);
		alColors.add(ColorGL.WHITE);

		String message = MessagesPanel.getLastestMessage(messageType, 2);
		if (message != null) {
			alMessages.add(message);
			alColors.add(MessagesPanel.getLastestMessageColor(messageType, 2));
		}

		message = MessagesPanel.getLastestMessage(messageType, 1);
		if (message != null) {
			alMessages.add(message);
			alColors.add(MessagesPanel.getLastestMessageColor(messageType, 1));
		}

		message = MessagesPanel.getLastestMessage(messageType, 0);
		if (message != null) {
			alMessages.add(message);
			alColors.add(MessagesPanel.getLastestMessageColor(messageType, 0));
		}

		int tooltipX = iconPoint.x;
		int tooltipY = iconPoint.y + UIScale.fontHeight() * 2;

		MainPanel.renderMessages(
				tooltipX,
				tooltipY,
				MainPanel.renderWidth,
				MainPanel.renderHeight,
				2,
				alMessages,
				alColors);
	}

	public static void miscTooltips(int x, int y, int mousePanel) {
		if (mousePanel == MOUSE_INFO_NUM_CITIZENS) {
			int happinessMin = (World.getCitizenIDs().size() + World.getSoldierIDs().size()) * 2;

			if (happinessMin < 20) {
				happinessMin = 20;
			} else if (happinessMin > 80) {
				happinessMin = 80;
			}

			tooltip = Messages.getString("UIPanel.8")
					+ " ("
					+ Messages.getString("UIPanel.76")
					+ ": "
					+ World.getHappinessAverage()
					+ " "
					+ Messages.getString("UIPanel.81")
					+ ": "
					+ happinessMin
					+ ")";
			tooltipPoint = TooltipRenderer.centeredBelow(
					iconNumCitizensBackgroundPoint.x + tileBottomItem.getTileWidth() / 2,
					iconNumCitizensBackgroundPoint.y + tileBottomItem.getTileHeight(),
					tooltip);

			return;

		}
		if (mousePanel == MOUSE_TUTORIAL_ICON) {
			tooltip = Messages.getString("UIPanel.75")
					+ UtilsKeyboard.getTooltip(UtilsKeyboard.FN_SHOW_MISSION);

			tooltipPoint = TooltipRenderer.rightOf(x, y);

			return;
		}

		if (renderMessageIconTooltipIfNeeded(mousePanel)) {
			return;
		}
		Supplier<String> tooltipSupplier = TOOLTIP_MESSAGES.get(mousePanel);

		if (tooltipSupplier == null) {
			return;
		}

		tooltip = tooltipSupplier.get();

		if (tooltip == null) {
			return;
		}

		Function<String, Point> tooltipPointFunction = TOOLTIP_POINTS.get(mousePanel);

		if (tooltipPointFunction != null) {
			tooltipPoint = tooltipPointFunction.apply(tooltip);
		} else {
			tooltipPoint = TooltipRenderer.rightOf(x, y);
		}

	}

	public static void typingPanelTooltips(int x, int y, int mousePanel) {
		if (mousePanel == UIPanelState.MOUSE_MESSAGES_ICON_ANNOUNCEMENT
				|| mousePanel == MOUSE_MESSAGES_ICON_COMBAT
				|| mousePanel == MOUSE_MESSAGES_ICON_HEROES
				|| mousePanel == MOUSE_MESSAGES_ICON_SYSTEM) {
			return;
		}

		if (mousePanel == MOUSE_EVENTS_ICON) {
			renderEventsTooltip(x, y);
		}
	}

	private static void renderEventsTooltip(int x, int y) {
		ArrayList<EventData> alEvents = Game.getWorld().getEvents();

		if (alEvents.size() == 0) {
			String noEventsTooltip = Messages.getString("UIPanel.83"); //$NON-NLS-1$

			Point point = TooltipRenderer.centeredBelow(
					iconEventsPoint.x + GlobalEventData.getIcon().getTileWidth() / 2,
					iconEventsPoint.y + GlobalEventData.getIcon().getTileHeight(),
					noEventsTooltip);

			TooltipRenderer.draw(
					noEventsTooltip,
					point.x,
					point.y,
					renderWidth,
					renderHeight);

			return;
		}

		String eventsTooltip = Messages.getString("UIPanel.84"); //$NON-NLS-1$

		int paddingX = UIScale.px(4);
		int paddingY = UIScale.px(4);
		int rowGap = UIScale.px(2);
		int iconTextGap = UIScale.px(4);

		int tooltipWidth = UIScale.textWidth(eventsTooltip);
		int tooltipHeight = UIScale.fontHeight();

		EventData eventData;
		EventManagerItem eventItem;
		int rowWidth;

		for (int i = 0; i < alEvents.size(); i++) {
			eventData = alEvents.get(i);
			eventItem = EventManager.getItem(eventData.getEventID());

			if (eventItem == null) {
				continue;
			}

			if (eventItem.getIcon() != null) {
				tooltipHeight += eventItem.getIcon().getTileHeight() + rowGap;

				rowWidth = UIScale.textWidth(eventItem.getName())
						+ eventItem.getIcon().getTileWidth()
						+ iconTextGap;
			} else {
				tooltipHeight += UIScale.fontHeight() + rowGap;

				rowWidth = UIScale.textWidth(eventItem.getName());
			}

			if (rowWidth > tooltipWidth) {
				tooltipWidth = rowWidth;
			}
		}

		int tooltipX = iconEventsPoint.x + GlobalEventData.getIcon().getTileWidth() / 2
				- tooltipWidth / 2;
		int tooltipY = iconEventsPoint.y + GlobalEventData.getIcon().getTileHeight();

		Point tooltipPosition = clampTooltipPosition(
				tooltipX,
				tooltipY,
				tooltipWidth + paddingX * 2,
				tooltipHeight + paddingY * 2,
				renderWidth,
				renderHeight);

		tooltipX = tooltipPosition.x;
		tooltipY = tooltipPosition.y;

		int iCurrentTexture = tileTooltipBackground.getTextureID();

		GL11.glColor4f(1, 1, 1, 1);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, tileTooltipBackground.getTextureID());

		UtilsGL.glBegin(GL11.GL_QUADS);

		UtilsGL.drawTexture(
				tooltipX,
				tooltipY,
				tooltipX + tooltipWidth + paddingX * 2,
				tooltipY + tooltipHeight + paddingY * 2,
				tileTooltipBackground.getTileSetTexX0(),
				tileTooltipBackground.getTileSetTexY0(),
				tileTooltipBackground.getTileSetTexX1(),
				tileTooltipBackground.getTileSetTexY1());

		int currentY = tooltipY + paddingY + UIScale.fontHeight() + rowGap;

		for (int i = 0; i < alEvents.size(); i++) {
			eventData = alEvents.get(i);
			eventItem = EventManager.getItem(eventData.getEventID());

			if (eventItem == null) {
				continue;
			}

			if (eventItem.getIcon() != null) {
				iCurrentTexture = UtilsGL.setTexture(eventItem.getIcon(), iCurrentTexture);

				UIPanel.drawTile(
						eventItem.getIcon(),
						tooltipX + paddingX,
						currentY,
						false);

				currentY += eventItem.getIcon().getTileHeight() + rowGap;
			} else {
				currentY += UIScale.fontHeight() + rowGap;
			}
		}

		UtilsGL.glEnd();

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);

		UtilsGL.glBegin(GL11.GL_QUADS);

		currentY = tooltipY + paddingY;

		TooltipRenderer.drawScaledString(
				eventsTooltip,
				tooltipX + paddingX,
				currentY);

		currentY += UIScale.fontHeight() + rowGap;

		for (int i = 0; i < alEvents.size(); i++) {
			eventData = alEvents.get(i);
			eventItem = EventManager.getItem(eventData.getEventID());

			if (eventItem == null) {
				continue;
			}

			if (eventItem.getIcon() != null) {
				TooltipRenderer.drawScaledString(
						eventItem.getName(),
						tooltipX + paddingX + eventItem.getIcon().getTileWidth() + iconTextGap,
						currentY + eventItem.getIcon().getTileHeight() / 2 - UIScale.fontHeight() / 2);

				currentY += eventItem.getIcon().getTileHeight() + rowGap;
			} else {
				TooltipRenderer.drawScaledString(
						eventItem.getName(),
						tooltipX + paddingX,
						currentY);

				currentY += UIScale.fontHeight() + rowGap;
			}
		}

		UtilsGL.glEnd();

		GL11.glColor3f(1, 1, 1);
	}

	public static void livingPlayerEntitiesTooltip(int x, int y, int mousePanel) {
		Point p = UIPanelInputHandler.isMouseOnLivingsButtons(x, y);
		int iIndex = UIPanel.getLivingsIndex();
		ArrayList<Integer> alLivings = UIPanel.getLivings();
		if (alLivings != null && p != null && (p.y + iIndex) >= 0 && (p.y + iIndex) < alLivings.size()) {
			LivingEntity le = World.getLivingEntityByID(alLivings.get((p.y + iIndex)));
			if (le != null) {
				if (mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS) {
					if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_CITIZENS
							|| getLivingsPanelActive() == LIVINGS_PANEL_TYPE_SOLDIERS) {
						Citizen citizen = (Citizen) le;
						int iNumEffects = le.getLivingEntityData().getEffects().size();
						ArrayList<String> alMessages = new ArrayList<String>(6 + iNumEffects);
						ArrayList<ColorGL> alColors = new ArrayList<ColorGL>(6 + iNumEffects);

						alMessages.add(citizen.getCitizenData().getFullName());
						alColors.add(ColorGL.YELLOW);
						alMessages.add(citizen.getLivingEntityData().toString());
						alColors.add(ColorGL.WHITE);
						if (citizen.getCurrentTask() != null) {
							alMessages.add(Messages.getString("Citizen.7") + citizen.getCurrentTask()); //$NON-NLS-1$
							alColors.add(ColorGL.WHITE);
						}

						// Level / Xp
						if (citizen.getSoldierData().isSoldier()) {
							alMessages.add(Messages.getString("Hero.4") //$NON-NLS-1$
									+ citizen.getSoldierData().getLevel() + " (" //$NON-NLS-1$
									+ citizen.getSoldierData().getXp() + Messages.getString("Hero.5") //$NON-NLS-1$
									+ citizen.getSoldierData().getXpPCT() + "%)"); //$NON-NLS-1$
							alColors.add(ColorGL.WHITE);
						}

						alMessages.add(Messages.getString("UIPanel.40") //$NON-NLS-1$
								+ citizen.getCitizenData().getHappiness() + " / 100"); //$NON-NLS-1$
						alColors.add(ColorGL.WHITE);
						alMessages
								.add(Messages.getString("UIPanel.49") + citizen.getCitizenData().getHungry() //$NON-NLS-1$
										+ " / " + citizen.getCitizenData().getMaxHungry()); //$NON-NLS-1$
						alColors.add(ColorGL.WHITE);
						alMessages
								.add(Messages.getString("UIPanel.52") + citizen.getCitizenData().getSleep() //$NON-NLS-1$
										+ " / " + citizen.getCitizenData().getMaxSleep()); //$NON-NLS-1$
						alColors.add(ColorGL.WHITE);

						// Effects
						EffectData eData;
						for (int e = 0; e < iNumEffects; e++) {
							eData = le.getLivingEntityData().getEffects().get(e);
							alMessages.add(EffectManager.getItem(eData.getEffectID()).getName());
							alColors.add(ColorGL.ORANGE);
						}

						MainPanel.renderMessages(x + 32, y, MainPanel.renderWidth, MainPanel.renderHeight,
								2, alMessages, alColors);
						return;
					} else if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_HEROES) {
						Hero hero = (Hero) le;
						int iNumEffects = le.getLivingEntityData().getEffects().size();
						ArrayList<String> alMessages = new ArrayList<String>(3 + iNumEffects);
						ArrayList<ColorGL> alColors = new ArrayList<ColorGL>(3 + iNumEffects);

						alMessages.add(hero.getCitizenData().getFullName());
						alMessages.add(hero.getLivingEntityData().toString());
						alMessages.add(Messages.getString("Hero.4") + hero.getHeroData().getLevel() + " (" //$NON-NLS-1$ //$NON-NLS-2$
								+ hero.getHeroData().getXp() + Messages.getString("Hero.5") //$NON-NLS-1$
								+ hero.getHeroData().getXpPCT() + "%)"); //$NON-NLS-1$
						alColors.add(ColorGL.YELLOW);
						alColors.add(ColorGL.WHITE);
						alColors.add(ColorGL.ORANGE);

						// Friendship
						String sHeroFriends = HeroData.getFriendshipString(hero);
						if (sHeroFriends != null) {
							alMessages.add(sHeroFriends);
							alColors.add(ColorGL.WHITE);
						}

						// Effects
						EffectData eData;
						for (int e = 0; e < iNumEffects; e++) {
							eData = le.getLivingEntityData().getEffects().get(e);
							alMessages.add(EffectManager.getItem(eData.getEffectID()).getName());
							alColors.add(ColorGL.ORANGE);
						}

						MainPanel.renderMessages(x + 32, y, MainPanel.renderWidth, MainPanel.renderHeight,
								2, alMessages, alColors);
						return;
					}
				} else if (mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_HEAD) {
					// Head
					EquippedData equippedData = le.getEquippedData();
					if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_CITIZENS
							|| getLivingsPanelActive() == LIVINGS_PANEL_TYPE_SOLDIERS) {
						if (equippedData.isWearing(MilitaryItem.LOCATION_HEAD)) {
							tooltip = Messages.getString("UIPanel.41") //$NON-NLS-1$
									+ equippedData.getHead().getExtendedTilename();
						} else {
							tooltip = Messages.getString("UIPanel.42"); //$NON-NLS-1$
						}
					} else {
						if (equippedData.isWearing(MilitaryItem.LOCATION_HEAD)) {
							tooltip = Messages.getString("Citizen.21") //$NON-NLS-1$
									+ equippedData.getHead().getExtendedTilename();
						} else {
							tooltip = Messages.getString("Citizen.14"); //$NON-NLS-1$
						}
					}
					tooltipPoint = TooltipRenderer.rightOf(x, y);

				} else if (mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_BODY) {
					// Body
					EquippedData equippedData = le.getEquippedData();
					if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_CITIZENS
							|| getLivingsPanelActive() == LIVINGS_PANEL_TYPE_SOLDIERS) {
						if (equippedData.isWearing(MilitaryItem.LOCATION_BODY)) {
							tooltip = Messages.getString("UIPanel.41") //$NON-NLS-1$
									+ equippedData.getBody().getExtendedTilename();
						} else {
							tooltip = Messages.getString("UIPanel.44"); //$NON-NLS-1$
						}
					} else {
						if (equippedData.isWearing(MilitaryItem.LOCATION_BODY)) {
							tooltip = Messages.getString("Citizen.22") //$NON-NLS-1$
									+ equippedData.getBody().getExtendedTilename();
						} else {
							tooltip = Messages.getString("Citizen.15"); //$NON-NLS-1$
						}
					}
					tooltipPoint = TooltipRenderer.rightOf(x, y);

				} else if (mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_LEGS) {
					// Legs
					EquippedData equippedData = le.getEquippedData();
					if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_CITIZENS
							|| getLivingsPanelActive() == LIVINGS_PANEL_TYPE_SOLDIERS) {
						if (equippedData.isWearing(MilitaryItem.LOCATION_LEGS)) {
							tooltip = Messages.getString("UIPanel.41") //$NON-NLS-1$
									+ equippedData.getLegs().getExtendedTilename();
						} else {
							tooltip = Messages.getString("UIPanel.46"); //$NON-NLS-1$
						}
					} else {
						if (equippedData.isWearing(MilitaryItem.LOCATION_LEGS)) {
							tooltip = Messages.getString("Citizen.23") //$NON-NLS-1$
									+ equippedData.getLegs().getExtendedTilename();
						} else {
							tooltip = Messages.getString("Citizen.16"); //$NON-NLS-1$
						}
					}
					tooltipPoint = TooltipRenderer.rightOf(x, y);

				} else if (mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_FEET) {
					// Feet
					EquippedData equippedData = le.getEquippedData();
					if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_CITIZENS
							|| getLivingsPanelActive() == LIVINGS_PANEL_TYPE_SOLDIERS) {
						if (equippedData.isWearing(MilitaryItem.LOCATION_FEET)) {
							tooltip = Messages.getString("UIPanel.41") //$NON-NLS-1$
									+ equippedData.getFeet().getExtendedTilename();
						} else {
							tooltip = Messages.getString("UIPanel.48"); //$NON-NLS-1$
						}
					} else {
						if (equippedData.isWearing(MilitaryItem.LOCATION_FEET)) {
							tooltip = Messages.getString("Citizen.24") //$NON-NLS-1$
									+ equippedData.getFeet().getExtendedTilename();
						} else {
							tooltip = Messages.getString("Citizen.17"); //$NON-NLS-1$
						}
					}
					tooltipPoint = TooltipRenderer.rightOf(x, y);

				} else if (mousePanel == MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_WEAPON) {
					// Weapon
					EquippedData equippedData = le.getEquippedData();
					if (getLivingsPanelActive() == LIVINGS_PANEL_TYPE_CITIZENS
							|| getLivingsPanelActive() == LIVINGS_PANEL_TYPE_SOLDIERS) {
						if (equippedData.isWearing(MilitaryItem.LOCATION_WEAPON)) {
							tooltip = Messages.getString("UIPanel.41") //$NON-NLS-1$
									+ equippedData.getWeapon().getExtendedTilename();
						} else {
							tooltip = Messages.getString("UIPanel.50"); //$NON-NLS-1$
						}
					} else {
						if (equippedData.isWearing(MilitaryItem.LOCATION_WEAPON)) {
							tooltip = Messages.getString("Citizen.25") //$NON-NLS-1$
									+ equippedData.getWeapon().getExtendedTilename();
						} else {
							tooltip = Messages.getString("Citizen.18"); //$NON-NLS-1$
						}
					}
					tooltipPoint = TooltipRenderer.rightOf(x, y);

				}
			}
		}
	}
}