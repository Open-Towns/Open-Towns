package xaos.panels.UI;



import java.awt.Point;
import java.util.ArrayList;

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
import static xaos.panels.UI.UIPanelState.*;

public final class TooltipRenderer {
	// private static SmartMenu currentMenu;

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

		if (tooltipX < 0) {
			tooltipX = 0;
		} else if (tooltipX + tooltipWidth + UIScale.px(1) > renderWidth) {
			tooltipX -= (tooltipX + tooltipWidth + UIScale.px(1)) - renderWidth;
		}

		if (tooltipY < 0) {
			tooltipY = 0;
		} else if (tooltipY + tooltipHeight + UIScale.px(1) > renderHeight) {
			tooltipY -= (tooltipY + tooltipHeight + UIScale.px(1)) - renderHeight;
		}

		GL11.glColor4f(1, 1, 1, 1);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, UIPanelState.tileTooltipBackground.getTextureID());

		UtilsGL.glBegin(GL11.GL_QUADS);
		UtilsGL.drawTexture(
				tooltipX,
				tooltipY,
				tooltipX + tooltipWidth,
				tooltipY + tooltipHeight,
				UIPanelState.tileTooltipBackground.getTileSetTexX0(),
				UIPanelState.tileTooltipBackground.getTileSetTexY0(),
				UIPanelState.tileTooltipBackground.getTileSetTexX1(),
				UIPanelState.tileTooltipBackground.getTileSetTexY1());
		UtilsGL.glEnd();

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);

		UtilsGL.glBegin(GL11.GL_QUADS);
		drawScaledString(tooltip, tooltipX + paddingX, tooltipY + paddingY);
		UtilsGL.glEnd();

		GL11.glColor3f(1, 1, 1);
	}

	private static void drawScaledString(String text, int x, int y) {
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

	public static void renderTooltips(int x, int y, int mousePanel) {
		if (mousePanel == UIPanelState.MOUSE_NONE || mousePanel == UIPanelState.MOUSE_PRODUCTION_PANEL
				|| mousePanel == UIPanelState.MOUSE_PRIORITIES_PANEL) {
			return;
		}

		String tooltip = null;
		int tooltipX = 0, tooltipY = 0;

		if (UIPanelState.typingPanel != null) {
			// TYPING PANEL

		} else {
			// TYPING PANEL NO ACTIVO

			// Bottom
			if (UIPanel.isBottomMenuPanelActive()) {
				if (mousePanel == UIPanelState.MOUSE_BOTTOM_ITEMS) {
					int iItem = UIPanelInputHandler.isMouseOnBottomItems(x, y);
					if (iItem != -1) {
						SmartMenu item = currentMenu.getItems().get(iItem + UIPanelState.bottomPanelItemIndex);
						tooltip = item.getName();
						if (item.getType() == SmartMenu.TYPE_ITEM && (iItem + UIPanelState.bottomPanelItemIndex) >= 0
								&& (iItem + UIPanelState.bottomPanelItemIndex) <= 9) {
							switch (iItem + UIPanelState.bottomPanelItemIndex) {
								case 0:
									tooltip += UtilsKeyboard.getTooltip(UtilsKeyboard.FN_BOT_1);
									break;
								case 1:
									tooltip += UtilsKeyboard.getTooltip(UtilsKeyboard.FN_BOT_2);
									break;
								case 2:
									tooltip += UtilsKeyboard.getTooltip(UtilsKeyboard.FN_BOT_3);
									break;
								case 3:
									tooltip += UtilsKeyboard.getTooltip(UtilsKeyboard.FN_BOT_4);
									break;
								case 4:
									tooltip += UtilsKeyboard.getTooltip(UtilsKeyboard.FN_BOT_5);
									break;
								case 5:
									tooltip += UtilsKeyboard.getTooltip(UtilsKeyboard.FN_BOT_6);
									break;
								case 6:
									tooltip += UtilsKeyboard.getTooltip(UtilsKeyboard.FN_BOT_7);
									break;
								case 7:
									tooltip += UtilsKeyboard.getTooltip(UtilsKeyboard.FN_BOT_8);
									break;
								case 8:
									tooltip += UtilsKeyboard.getTooltip(UtilsKeyboard.FN_BOT_9);
									break;
								case 9:
									tooltip += UtilsKeyboard.getTooltip(UtilsKeyboard.FN_BOT_10);
									break;
							}
						}
						if (tooltip != null) {
							Point tooltipPoint = TooltipRenderer.centeredAbove(x, UIPanelState.bottomPanelY, tooltip);
							tooltipX = tooltipPoint.x;
							tooltipY = tooltipPoint.y;
						}
					}
				} else if (mousePanel == UIPanelState.MOUSE_BOTTOM_SUBITEMS) {
					int iItem = UIPanelInputHandler.isMouseOnBottomSubItems(x, y);
					if (iItem != -1) {
						SmartMenu item = UIPanelState.bottomSubPanelMenu.getItems().get(iItem);
						if (item.getPrerequisites() != null && item.getPrerequisites().size() > 0) {
							MainPanel.renderMessages(x,
									UIPanelState.bottomSubPanelPoint.y
											- (item.getPrerequisites().size() * (UIScale.fontHeight() + UIScale.px(5))),
									UIPanelState.renderWidth, UIPanelState.renderHeight,
									UIScale.px(Tile.TERRAIN_ICON_WIDTH / 2),
									item.getPrerequisites(),
									item.getPrerequisitesColor());
						} else {
							tooltip = item.getName();
							if (tooltip != null) {
								Point tooltipPoint = TooltipRenderer.centeredAbove(x,
										UIPanelState.bottomSubPanelPoint.y, tooltip);
								tooltipX = tooltipPoint.x;
								tooltipY = tooltipPoint.y;
							}
						}
					}
				}
			}

			// Right menu
			if (tooltip == null && UIPanel.isMenuPanelActive()) {
				if (mousePanel == UIPanelState.MOUSE_MENU_PANEL_ITEMS) {
					int iItem = UIPanelInputHandler.isMouseOnMenuItems(x, y);
					if (iItem != -1) {
						SmartMenu item = UIPanelState.menuPanelMenu.getItems().get(iItem);
						if (item.getPrerequisites() != null && item.getPrerequisites().size() > 0) {
							MainPanel.renderMessages(UIPanelState.menuPanelPoint.x, y, UIPanelState.renderWidth,
									UIPanelState.renderHeight,
									Tile.TERRAIN_ICON_WIDTH / 2, item.getPrerequisites(), item.getPrerequisitesColor());
						} else {
							tooltip = item.getName();
							if (tooltip != null) {
								Point tooltipPoint = TooltipRenderer.leftOf(UIPanelState.menuPanelPoint.x, y, tooltip);
								tooltipX = tooltipPoint.x;
								tooltipY = tooltipPoint.y;
							}
						}
					}
				}
			}

			// Production
			if (tooltip == null && isProductionPanelActive()) {
				if (mousePanel == UIPanelState.MOUSE_PRODUCTION_PANEL_ITEMS
						|| mousePanel == UIPanelState.MOUSE_PRODUCTION_PANEL_ITEMS_MINUS_AUTOMATED
						|| mousePanel == UIPanelState.MOUSE_PRODUCTION_PANEL_ITEMS_PLUS_AUTOMATED) {
					Point p = UIPanelInputHandler.isMouseOnProductionItems(x, y);
					if (p != null) {
						if (p.x == UIPanelState.MOUSE_PRODUCTION_PANEL_ITEMS) {
							SmartMenu item = UIPanelState.productionPanelMenu.getItems().get(p.y);
							if (item.getPrerequisites() != null && item.getPrerequisites().size() > 0) {
								if ((UIPanelState.productionPanelPoint.x
										+ UIPanelState.PRODUCTION_PANEL_WIDTH) > UIPanelState.renderWidth) {
									MainPanel.renderMessages(UIPanelState.renderWidth, y, UIPanelState.renderWidth,
											UIPanelState.renderHeight, 0,
											item.getPrerequisites(), item.getPrerequisitesColor());
								} else {
									MainPanel.renderMessages(
											UIPanelState.productionPanelPoint.x + UIPanelState.PRODUCTION_PANEL_WIDTH,
											y,
											UIPanelState.renderWidth, UIPanelState.renderHeight, 0,
											item.getPrerequisites(),
											item.getPrerequisitesColor());
								}
							} else {
								tooltip = item.getName();
								if (tooltip != null) {
									Point tooltipPoint = TooltipRenderer.rightOf(x, y);
									tooltipX = tooltipPoint.x;
									tooltipY = tooltipPoint.y;
								}
							}
						} else if (p.x == UIPanelState.MOUSE_PRODUCTION_PANEL_ITEMS_MINUS_AUTOMATED
								|| p.x == UIPanelState.MOUSE_PRODUCTION_PANEL_ITEMS_PLUS_AUTOMATED) {
							SmartMenu item = UIPanelState.productionPanelMenu.getItems().get(p.y);
							if (item.getCommand().equals(CommandPanel.COMMAND_QUEUE)) {
								String sParam = item.getParameter();
								ActionManagerItem ami = ActionManager.getItem(sParam);
								if (ami != null) {
									if (ami.isInverted()) {
										tooltip = Messages.getString("UIPanel.68"); //$NON-NLS-1$
									} else {
										tooltip = Messages.getString("UIPanel.72"); //$NON-NLS-1$
									}
									Point tooltipPoint = TooltipRenderer.rightOf(x, y);
									tooltipX = tooltipPoint.x;
									tooltipY = tooltipPoint.y;
								}
							}
						}
					}
				}
			}

			// Priorities
			if (tooltip == null && UIPanelState.isPrioritiesPanelActive()) {
				if (mousePanel == UIPanelState.MOUSE_PRIORITIES_PANEL_ITEMS) {
					Point p = UIPanelInputHandler.isMouseOnPrioritiesItems(x, y);
					if (p != null && p.x == UIPanelState.MOUSE_PRIORITIES_PANEL_ITEMS) {
						if (p.y == (UIPanelState.PRIORITIES_PANEL_NUM_ITEMS - 1)) {
							// Back
							tooltip = Messages.getString("UIPanel.13"); //$NON-NLS-1$
						} else {
							tooltip = ActionPriorityManager.getItem(ActionPriorityManager.getPrioritiesList().get(p.y))
									.getName();
						}
						Point tooltipPoint = TooltipRenderer.centeredBelow(
								UIPanelState.prioritiesPanelPoint.x + UIScale.px(UIPanelState.PRIORITIES_PANEL_WIDTH),
								y, tooltip);

						tooltipX = tooltipPoint.x;
						tooltipY = tooltipPoint.y;
					}
				}
			}

			// Trade
			if (tooltip == null && UIPanelState.isTradePanelActive()) {
				CaravanData caravanData = Game.getWorld().getCurrentCaravanData();
				boolean bTrading = (caravanData != null && caravanData.getStatus() == CaravanData.STATUS_TRADING);

				if (!bTrading && mousePanel == UIPanelState.MOUSE_TRADE_PANEL_BUTTONS_CARAVAN) {
					Point p = UIPanelInputHandler.isMouseOnTradeButtons(x, y);
					if (p != null && p.x == UIPanelState.MOUSE_TRADE_PANEL_BUTTONS_CARAVAN
							&& (p.y + UIPanelState.tradePanel.getIndexButtonsCaravan()) < UIPanelState.tradePanel
									.getMenuCaravan().getItems()
									.size()) {
						tooltip = UIPanelState.tradePanel.getMenuCaravan().getItems()
								.get(p.y + UIPanelState.tradePanel.getIndexButtonsCaravan())
								.getName();
						Point tooltipPoint = TooltipRenderer.rightOf(x, y);
						tooltipX = tooltipPoint.x;
						tooltipY = tooltipPoint.y;
					}
				} else if (mousePanel == UIPanelState.MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_CARAVAN) {
					if (caravanData != null) {
						Point p = UIPanelInputHandler.isMouseOnTradeButtons(x, y);
						if (p != null && p.x == UIPanelState.MOUSE_TRADE_PANEL_BUTTONS_TO_BUY_CARAVAN
								&& (p.y + UIPanelState.tradePanel.getIndexButtonsToBuyCaravan()) < caravanData
										.getMenuCaravanToBuy()
										.getItems().size()) {
							tooltip = caravanData.getMenuCaravanToBuy().getItems()
									.get(p.y + UIPanelState.tradePanel.getIndexButtonsToBuyCaravan()).getName();
							Point tooltipPoint = TooltipRenderer.rightOf(x, y);
							tooltipX = tooltipPoint.x;
							tooltipY = tooltipPoint.y;
						}
					}
				} else if (!bTrading && mousePanel == UIPanelState.MOUSE_TRADE_PANEL_BUTTONS_TOWN) {
					Point p = UIPanelInputHandler.isMouseOnTradeButtons(x, y);
					if (p != null && p.x == UIPanelState.MOUSE_TRADE_PANEL_BUTTONS_TOWN
							&& (p.y + UIPanelState.tradePanel.getIndexButtonsTown()) < UIPanelState.tradePanel
									.getMenuTown().getItems().size()) {
						tooltip = UIPanelState.tradePanel.getMenuTown().getItems()
								.get(p.y + UIPanelState.tradePanel.getIndexButtonsTown())
								.getName();
						Point tooltipPoint = TooltipRenderer.rightOf(x, y);
						tooltipX = tooltipPoint.x;
						tooltipY = tooltipPoint.y;
					}
				} else if (mousePanel == UIPanelState.MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_TOWN) {
					if (caravanData != null) {
						Point p = UIPanelInputHandler.isMouseOnTradeButtons(x, y);
						if (p != null && p.x == UIPanelState.MOUSE_TRADE_PANEL_BUTTONS_TO_SELL_TOWN
								&& (p.y + UIPanelState.tradePanel.getIndexButtonsToSellTown()) < caravanData
										.getMenuTownToSell()
										.getItems().size()) {
							tooltip = caravanData.getMenuTownToSell().getItems()
									.get(p.y + UIPanelState.tradePanel.getIndexButtonsToSellTown()).getName();
							Point tooltipPoint = TooltipRenderer.rightOf(x, y);
							tooltipX = tooltipPoint.x;
							tooltipY = tooltipPoint.y;
						}
					}
				} else if (mousePanel == UIPanelState.MOUSE_TRADE_PANEL_BUTTONS_CLOSE) {
					tooltip = Messages.getString("UIPanel.19"); //$NON-NLS-1$
					Point tooltipPoint = TooltipRenderer.rightOf(x, y);
					tooltipX = tooltipPoint.x;
					tooltipY = tooltipPoint.y;
				} else if (mousePanel == UIPanelState.MOUSE_TRADE_PANEL_ICON_BUY) {
					tooltip = Messages.getString("UIPanel.33"); //$NON-NLS-1$
					Point tooltipPoint = TooltipRenderer.rightOf(x, y);
					tooltipX = tooltipPoint.x;
					tooltipY = tooltipPoint.y;
				} else if (mousePanel == UIPanelState.MOUSE_TRADE_PANEL_ICON_SELL) {
					tooltip = Messages.getString("UIPanel.35"); //$NON-NLS-1$
					Point tooltipPoint = TooltipRenderer.rightOf(x, y);
					tooltipX = tooltipPoint.x;
					tooltipY = tooltipPoint.y;
				} else if (!bTrading && mousePanel == UIPanelState.MOUSE_TRADE_PANEL_BUTTONS_CONFIRM) {
					tooltip = Messages.getString("UIPanel.36"); //$NON-NLS-1$
					Point tooltipPoint = TooltipRenderer.rightOf(x, y);
					tooltipX = tooltipPoint.x;
					tooltipY = tooltipPoint.y;
				}
			}

			// Professions
			if (tooltip == null && UIPanelState.isProfessionsPanelActive()) {
				if (mousePanel == UIPanelState.MOUSE_PROFESSIONS_PANEL_BUTTONS_ITEMS) {
					Point p = UIPanelInputHandler.isMouseOnProfessionsButtons(x, y);
					if (p != null && p.y > -1 && p.y < UIPanelState.menuProfessions.getItems().size()) {
						String sName = UIPanelState.menuProfessions.getItems().get(p.y).getName();
						if (sName != null) {
							tooltip = sName;
							Point tooltipPoint = TooltipRenderer.rightOf(x, y);
							tooltipX = tooltipPoint.x;
							tooltipY = tooltipPoint.y;
							tooltipX = x;
							tooltipY = y + UtilFont.MAX_HEIGHT * 2;
						}
					}
				} else if (mousePanel == UIPanelState.MOUSE_PROFESSIONS_PANEL_BUTTONS_CLOSE) {
					tooltip = Messages.getString("UIPanel.19"); //$NON-NLS-1$
					Point tooltipPoint = TooltipRenderer.rightOf(x, y);
					tooltipX = tooltipPoint.x;
					tooltipY = tooltipPoint.y;
				}
			}

			// Pile
			if (tooltip == null && UIPanelState.isPilePanelActive()) {
				if (mousePanel == UIPanelState.MOUSE_PILE_PANEL_BUTTONS_ITEMS) {
					Point p = UIPanelInputHandler.isMouseOnPileButtons(x, y);
					if (p != null && p.y > -1 && p.y < UIPanelState.menuPile.getItems().size()) {
						String sName = UIPanelState.menuPile.getItems().get(p.y).getName();
						if (sName != null) {
							tooltip = sName;
							Point tooltipPoint = TooltipRenderer.rightOf(x, y);
							tooltipX = tooltipPoint.x;
							tooltipY = tooltipPoint.y;
						}
					}
				} else if (mousePanel == UIPanelState.MOUSE_PILE_PANEL_BUTTONS_CLOSE) {
					tooltip = Messages.getString("UIPanel.19"); //$NON-NLS-1$
					Point tooltipPoint = TooltipRenderer.rightOf(x, y);
					tooltipX = tooltipPoint.x;
					tooltipY = tooltipPoint.y;
					tooltipY = y;
				} else if (mousePanel == UIPanelState.MOUSE_PILE_PANEL_BUTTONS_CONFIG_COPY) {
					if (UIPanelState.pilePanelIsContainer) {
						tooltip = Messages.getString("UIPanel.80"); //$NON-NLS-1$
					} else {
						tooltip = Messages.getString("UIPanel.82"); //$NON-NLS-1$
					}
					Point tooltipPoint = TooltipRenderer.rightOf(x, y);
					tooltipX = tooltipPoint.x;
					tooltipY = tooltipPoint.y;
				} else if (mousePanel == UIPanelState.MOUSE_PILE_PANEL_BUTTONS_CONFIG_LOCK) {
					if (UIPanelState.pilePanelIsLocked) {
						tooltip = Messages.getString("UIPanel.86"); //$NON-NLS-1$
					} else {
						tooltip = Messages.getString("UIPanel.85"); //$NON-NLS-1$
					}
					Point tooltipPoint = TooltipRenderer.rightOf(x, y);
					tooltipX = tooltipPoint.x;
					tooltipY = tooltipPoint.y;
				} else if (mousePanel == UIPanelState.MOUSE_PILE_PANEL_BUTTONS_CONFIG_LOCK_ALL) {
					tooltip = Messages.getString("UIPanel.87"); //$NON-NLS-1$
					Point tooltipPoint = TooltipRenderer.rightOf(x, y);
					tooltipX = tooltipPoint.x;
					tooltipY = tooltipPoint.y;
				} else if (mousePanel == UIPanelState.MOUSE_PILE_PANEL_BUTTONS_CONFIG_UNLOCK_ALL) {
					tooltip = Messages.getString("UIPanel.88"); //$NON-NLS-1$
					Point tooltipPoint = TooltipRenderer.rightOf(x, y);
					tooltipX = tooltipPoint.x;
					tooltipY = tooltipPoint.y;
				}
			}

			// Mats
			if (tooltip == null && UIPanelState.isMatsPanelActive()) {
				if (mousePanel == UIPanelState.MOUSE_MATS_PANEL_BUTTONS_GROUPS) {
					Point p = UIPanelInputHandler.isMouseOnMatsButtons(x, y);
					if (p != null && p.y > -1 && p.y < MatsPanelData.nameGroups.size()) {
						tooltip = MatsPanelData.nameGroups.get(p.y);
						Point tooltipPoint = TooltipRenderer.rightOf(x, y);
						tooltipX = tooltipPoint.x;
						tooltipY = tooltipPoint.y;
					}
				} else if (mousePanel == UIPanelState.MOUSE_MATS_PANEL_BUTTONS_ITEMS) {
					Point p = UIPanelInputHandler.isMouseOnMatsButtons(x, y);
					if (p != null && p.y > -1 && p.y < MatsPanelData.tileGroups.get(UIPanelState.getMatsPanelActive()).size()) {
						String sIniHeader = MatsPanelData.tileGroups.get(UIPanelState.getMatsPanelActive()).get(p.y).getIniHeader();
						if (sIniHeader != null) {
							ItemManagerItem imi = ItemManager.getItem(sIniHeader);
							if (imi != null && imi.getName() != null) {
								tooltip = imi.getName();
								Point tooltipPoint = TooltipRenderer.rightOf(x, y);
								tooltipX = tooltipPoint.x;
								tooltipY = tooltipPoint.y;
							}
						}
					}
				} else if (mousePanel == UIPanelState.MOUSE_MATS_PANEL_BUTTONS_CLOSE) {
					tooltip = Messages.getString("UIPanel.19"); //$NON-NLS-1$
					Point tooltipPoint = TooltipRenderer.rightOf(x, y);
					tooltipX = tooltipPoint.x;
					tooltipY = tooltipPoint.y;
				}
			}

			// Livings
			if (tooltip == null && UIPanelState.isLivingsPanelActive()) {
				if (mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_BUTTONS_CLOSE) {
					tooltip = Messages.getString("UIPanel.19"); //$NON-NLS-1$
					Point tooltipPoint = TooltipRenderer.rightOf(x, y);
					tooltipX = tooltipPoint.x;
					tooltipY = tooltipPoint.y;
				} else if ((mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_UP
						|| mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_DOWN)
						&& UIPanelState.livingsPanelCitizensGroupActive == -1
						&& UIPanelState.getLivingsPanelActive() == UIPanelState.LIVINGS_PANEL_TYPE_CITIZENS) {
					tooltip = Messages.getString("UIPanel.73"); //$NON-NLS-1$
					tooltipX = x - (UtilFont.getWidth(tooltip) / 2);
					tooltipY = UIPanelState.livingsPanelIconRestrictUpPoint.y + UIPanelState.tileIconLevelUp.getTileHeight();
				} else if ((mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_UP
						|| mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_BUTTONS_RESTRICT_DOWN)
						&& UIPanelState.getLivingsPanelActive() == UIPanelState.LIVINGS_PANEL_TYPE_HEROES) {
					tooltip = Messages.getString("UIPanel.74"); //$NON-NLS-1$
					tooltipX = x - (UtilFont.getWidth(tooltip) / 2);
					tooltipY = UIPanelState.livingsPanelIconRestrictUpPoint.y + UIPanelState.tileIconLevelUp.getTileHeight();
				} else if (mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_PROFESSIONS
						&& UIPanelState.livingsPanelCitizensGroupActive == -1) {
					tooltip = Messages.getString("UIPanel.63"); //$NON-NLS-1$
					Point tooltipPoint = TooltipRenderer.rightOf(x, y);
					tooltipX = tooltipPoint.x;
					tooltipY = tooltipPoint.y;
				} else if (mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_JOBS_GROUPS_ADDREMOVE) {
					tooltip = Messages.getString("UIPanel.65"); //$NON-NLS-1$
					Point tooltipPoint = TooltipRenderer.rightOf(x, y);
					tooltipX = tooltipPoint.x;
					tooltipY = tooltipPoint.y;
				} else if (mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER) {
					tooltip = Messages.getString("Citizen.27"); //$NON-NLS-1$
					Point tooltipPoint = TooltipRenderer.rightOf(x, y);
					tooltipX = tooltipPoint.x;
					tooltipY = tooltipPoint.y;
				} else if (mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_CIVILIAN) {
					tooltip = Messages.getString("Citizen.26"); //$NON-NLS-1$
					Point tooltipPoint = TooltipRenderer.rightOf(x, y);
					tooltipX = tooltipPoint.x;
					tooltipY = tooltipPoint.y;
				} else if (mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_GUARD) {
					tooltip = Messages.getString("Citizen.32"); //$NON-NLS-1$
					Point tooltipPoint = TooltipRenderer.rightOf(x, y);
					tooltipX = tooltipPoint.x;
					tooltipY = tooltipPoint.y;
				} else if (mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_PATROL) {
					tooltip = Messages.getString("Citizen.34"); //$NON-NLS-1$
					Point tooltipPoint = TooltipRenderer.rightOf(x, y);
					tooltipX = tooltipPoint.x;
					tooltipY = tooltipPoint.y;
				} else if (mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_CONVERT_SOLDIER_BOSS) {
					tooltip = Messages.getString("Citizen.35"); //$NON-NLS-1$
					Point tooltipPoint = TooltipRenderer.rightOf(x, y);
					tooltipX = tooltipPoint.x;
					tooltipY = tooltipPoint.y;
				} else if (mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_AUTOEQUIP) {
					tooltip = Messages.getString("UIPanel.43"); //$NON-NLS-1$
					Point tooltipPoint = TooltipRenderer.rightOf(x, y);
					tooltipX = tooltipPoint.x;
					tooltipY = tooltipPoint.y;
				} else if (mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_CGROUP_NOGROUP) {
					tooltip = Messages.getString("UIPanel.66") + " (" //$NON-NLS-1$ //$NON-NLS-2$
							+ Game.getWorld().getCitizenGroups().getCitizensWithoutGroup().size() + ")"; //$NON-NLS-1$
					tooltipX = x - UtilFont.getWidth(tooltip) / 2;
					tooltipY = y - UtilFont.MAX_HEIGHT - 2;
				} else if (mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_CGROUP_GROUP) {
					Point p = UIPanelInputHandler.isMouseOnLivingsButtons(x, y);
					if (p != null && p.y >= 0 && p.y < CitizenGroups.MAX_GROUPS) {
						CitizenGroupData cgd = Game.getWorld().getCitizenGroups().getGroup(p.y);
						if (cgd != null) {
							tooltip = cgd.getName() + " (" + cgd.getLivingIDs().size() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
							Point tooltipPoint = TooltipRenderer.rightOf(x, y);
							tooltipX = tooltipPoint.x;
							tooltipY = tooltipPoint.y;
						}
					}
				} else if (mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_RENAME) {
					tooltip = Messages.getString("UIPanel.54"); //$NON-NLS-1$
					Point tooltipPoint = TooltipRenderer.rightOf(x, y);
					tooltipX = tooltipPoint.x;
					tooltipY = tooltipPoint.y;
				} else if (mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_AUTOEQUIP) {
					tooltip = Messages.getString("UIPanel.59"); //$NON-NLS-1$
					Point tooltipPoint = TooltipRenderer.rightOf(x, y);
					tooltipX = tooltipPoint.x;
					tooltipY = tooltipPoint.y;
				} else if (mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_DISBAND) {
					tooltip = Messages.getString("UIPanel.60"); //$NON-NLS-1$
					Point tooltipPoint = TooltipRenderer.rightOf(x, y);
					tooltipX = tooltipPoint.x;
					tooltipY = tooltipPoint.y;
				} else if (mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_SINGLE_CGROUP_CHANGE_JOBS) {
					tooltip = Messages.getString("UIPanel.69"); //$NON-NLS-1$
					Point tooltipPoint = TooltipRenderer.rightOf(x, y);
					tooltipX = tooltipPoint.x;
					tooltipY = tooltipPoint.y;
				} else if (mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_SGROUP_ADD) {
					tooltip = Messages.getString("UIPanel.47"); //$NON-NLS-1$
					Point tooltipPoint = TooltipRenderer.rightOf(x, y);
					tooltipX = tooltipPoint.x;
					tooltipY = tooltipPoint.y;
				} else if (mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_SGROUP_REMOVE) {
					tooltip = Messages.getString("UIPanel.51"); //$NON-NLS-1$
					Point tooltipPoint = TooltipRenderer.rightOf(x, y);
					tooltipX = tooltipPoint.x;
					tooltipY = tooltipPoint.y;
				} else if (mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_SGROUP_NOGROUP) {
					tooltip = Messages.getString("UIPanel.53") + " (" //$NON-NLS-1$ //$NON-NLS-2$
							+ Game.getWorld().getSoldierGroups().getSoldiersWithoutGroup().size() + ")"; //$NON-NLS-1$
					tooltipX = x - UtilFont.getWidth(tooltip) / 2;
					tooltipY = y - UtilFont.MAX_HEIGHT - 2;
				} else if (mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_SGROUP_GROUP) {
					Point p = UIPanelInputHandler.isMouseOnLivingsButtons(x, y);
					if (p != null && p.y >= 0 && p.y < SoldierGroups.MAX_GROUPS) {
						SoldierGroupData sgd = Game.getWorld().getSoldierGroups().getGroup(p.y);
						if (sgd != null) {
							tooltip = sgd.getName() + " (" + sgd.getLivingIDs().size() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
							tooltipX = x - UtilFont.getWidth(tooltip) / 2;
							tooltipY = y - UtilFont.MAX_HEIGHT - 2;
						}
					}
				} else if (mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_RENAME) {
					tooltip = Messages.getString("UIPanel.54"); //$NON-NLS-1$
					Point tooltipPoint = TooltipRenderer.rightOf(x, y);
					tooltipX = tooltipPoint.x;
					tooltipY = tooltipPoint.y;
				} else if (mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_GUARD) {
					tooltip = Messages.getString("UIPanel.55"); //$NON-NLS-1$
					Point tooltipPoint = TooltipRenderer.rightOf(x, y);
					tooltipX = tooltipPoint.x;
					tooltipY = tooltipPoint.y;
				} else if (mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_PATROL) {
					tooltip = Messages.getString("UIPanel.57"); //$NON-NLS-1$
					Point tooltipPoint = TooltipRenderer.rightOf(x, y);
					tooltipX = tooltipPoint.x;
					tooltipY = tooltipPoint.y;
				} else if (mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_BOSS) {
					tooltip = Messages.getString("UIPanel.58"); //$NON-NLS-1$
					Point tooltipPoint = TooltipRenderer.rightOf(x, y);
					tooltipX = tooltipPoint.x;
					tooltipY = tooltipPoint.y;
				} else if (mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_AUTOEQUIP) {
					tooltip = Messages.getString("UIPanel.59"); //$NON-NLS-1$
					Point tooltipPoint = TooltipRenderer.rightOf(x, y);
					tooltipX = tooltipPoint.x;
					tooltipY = tooltipPoint.y;
				} else if (mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_SINGLE_SGROUP_DISBAND) {
					tooltip = Messages.getString("UIPanel.60"); //$NON-NLS-1$
					Point tooltipPoint = TooltipRenderer.rightOf(x, y);
					tooltipX = tooltipPoint.x;
					tooltipY = tooltipPoint.y;
				} else if (mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_BUTTONS_ROWS
						|| mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_HEAD
						|| mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_BODY
						|| mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_LEGS
						|| mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_FEET
						|| mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_WEAPON) {
					Point p = UIPanelInputHandler.isMouseOnLivingsButtons(x, y);
					int iIndex = UIPanel.getLivingsIndex();
					ArrayList<Integer> alLivings = UIPanel.getLivings();
					if (alLivings != null && p != null && (p.y + iIndex) >= 0 && (p.y + iIndex) < alLivings.size()) {
						LivingEntity le = World.getLivingEntityByID(alLivings.get((p.y + iIndex)));
						if (le != null) {
							if (mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_BUTTONS_ROWS) {
								if (UIPanelState.getLivingsPanelActive() == UIPanelState.LIVINGS_PANEL_TYPE_CITIZENS
										|| UIPanelState.getLivingsPanelActive() == UIPanelState.LIVINGS_PANEL_TYPE_SOLDIERS) {
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
								} else if (UIPanelState.getLivingsPanelActive() == UIPanelState.LIVINGS_PANEL_TYPE_HEROES) {
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
							} else if (mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_HEAD) {
								// Head
								EquippedData equippedData = le.getEquippedData();
								if (UIPanelState.getLivingsPanelActive() == UIPanelState.LIVINGS_PANEL_TYPE_CITIZENS
										|| UIPanelState.getLivingsPanelActive() == UIPanelState.LIVINGS_PANEL_TYPE_SOLDIERS) {
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
								Point tooltipPoint = TooltipRenderer.rightOf(x, y);
								tooltipX = tooltipPoint.x;
								tooltipY = tooltipPoint.y;
							} else if (mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_BODY) {
								// Body
								EquippedData equippedData = le.getEquippedData();
								if (UIPanelState.getLivingsPanelActive() == UIPanelState.LIVINGS_PANEL_TYPE_CITIZENS
										|| UIPanelState.getLivingsPanelActive() == UIPanelState.LIVINGS_PANEL_TYPE_SOLDIERS) {
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
								Point tooltipPoint = TooltipRenderer.rightOf(x, y);
								tooltipX = tooltipPoint.x;
								tooltipY = tooltipPoint.y;
							} else if (mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_LEGS) {
								// Legs
								EquippedData equippedData = le.getEquippedData();
								if (UIPanelState.getLivingsPanelActive() == UIPanelState.LIVINGS_PANEL_TYPE_CITIZENS
										|| UIPanelState.getLivingsPanelActive() == UIPanelState.LIVINGS_PANEL_TYPE_SOLDIERS) {
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
								Point tooltipPoint = TooltipRenderer.rightOf(x, y);
								tooltipX = tooltipPoint.x;
								tooltipY = tooltipPoint.y;
							} else if (mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_FEET) {
								// Feet
								EquippedData equippedData = le.getEquippedData();
								if (UIPanelState.getLivingsPanelActive() == UIPanelState.LIVINGS_PANEL_TYPE_CITIZENS
										|| UIPanelState.getLivingsPanelActive() == UIPanelState.LIVINGS_PANEL_TYPE_SOLDIERS) {
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
								Point tooltipPoint = TooltipRenderer.rightOf(x, y);
								tooltipX = tooltipPoint.x;
								tooltipY = tooltipPoint.y;
							} else if (mousePanel == UIPanelState.MOUSE_LIVINGS_PANEL_BUTTONS_ROWS_WEAPON) {
								// Weapon
								EquippedData equippedData = le.getEquippedData();
								if (UIPanelState.getLivingsPanelActive() == UIPanelState.LIVINGS_PANEL_TYPE_CITIZENS
										|| UIPanelState.getLivingsPanelActive() == UIPanelState.LIVINGS_PANEL_TYPE_SOLDIERS) {
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
								Point tooltipPoint = TooltipRenderer.rightOf(x, y);
								tooltipX = tooltipPoint.x;
								tooltipY = tooltipPoint.y;
							}
						}
					}
				}
			}

			if (tooltip == null) {
				if (mousePanel == UIPanelState.MOUSE_DATEPANEL) {
					tooltip = Messages.getString("UIPanel.29"); //$NON-NLS-1$
					tooltipX = UIPanelState.datePanelPoint.x + UIPanelState.tileDatePanel.getTileWidth() / 2 - (UtilFont.getWidth(tooltip) / 2);
					tooltipY = UIPanelState.datePanelPoint.y + UIPanelState.tileDatePanel.getTileHeight();
				} else if (mousePanel == UIPanelState.MOUSE_ICON_LEVEL_UP) {
					tooltip = Messages.getString("UIPanel.0") + UtilsKeyboard.getTooltip(UtilsKeyboard.FN_LEVEL_UP); //$NON-NLS-1$
					tooltipX = UIPanelState.iconLevelUpPoint.x + UIPanelState.tileIconLevelUp.getTileWidth() / 2
							- (UtilFont.getWidth(tooltip) / 2);
					tooltipY = UIPanelState.iconLevelUpPoint.y - UtilFont.MAX_HEIGHT;
				} else if (mousePanel == UIPanelState.MOUSE_ICON_LEVEL_DOWN) {
					tooltip = Messages.getString("UIPanel.2") + UtilsKeyboard.getTooltip(UtilsKeyboard.FN_LEVEL_DOWN); //$NON-NLS-1$
					tooltipX = UIPanelState.iconLevelDownPoint.x + UIPanelState.tileIconLevelDown.getTileWidth() / 2
							- (UtilFont.getWidth(tooltip) / 2);
					tooltipY = UIPanelState.iconLevelDownPoint.y + UtilFont.MAX_HEIGHT * 2;
				} else if (mousePanel == UIPanelState.MOUSE_ICON_LEVEL) {
					tooltip = Messages.getString("UIPanel.30"); //$NON-NLS-1$
					tooltipX = UIPanelState.iconLevelPoint.x + UIPanelState.tileIconLevel.getTileWidth() / 2 - (UtilFont.getWidth(tooltip) / 2);
					tooltipY = UIPanelState.iconLevelPoint.y + UtilFont.MAX_HEIGHT * 2;
				} else if (mousePanel == UIPanelState.MOUSE_ICON_CITIZEN_PREVIOUS) {
					tooltip = Messages.getString("UIPanel.3") //$NON-NLS-1$
							+ UtilsKeyboard.getTooltip(UtilsKeyboard.FN_PREVIOUS_CITIZEN);
					tooltipX = UIPanelState.iconCitizenPreviousPoint.x + UIPanelState.tileIconCitizenPrevious.getTileWidth() / 2
							- (UtilFont.getWidth(tooltip) / 2);
					tooltipY = UIPanelState.iconCitizenPreviousPoint.y + UIPanelState.tileBottomItem.getTileHeight();
				} else if (mousePanel == UIPanelState.MOUSE_ICON_CITIZEN_NEXT) {
					tooltip = Messages.getString("UIPanel.4") + UtilsKeyboard.getTooltip(UtilsKeyboard.FN_NEXT_CITIZEN); //$NON-NLS-1$
					tooltipX = UIPanelState.iconCitizenNextPoint.x + UIPanelState.tileIconCitizenNext.getTileWidth() / 2
							- (UtilFont.getWidth(tooltip) / 2);
					tooltipY = UIPanelState.iconCitizenNextPoint.y + UIPanelState.tileBottomItem.getTileHeight();
				} else if (mousePanel == UIPanelState.MOUSE_ICON_SOLDIER_PREVIOUS) {
					tooltip = Messages.getString("UIPanel.5") //$NON-NLS-1$
							+ UtilsKeyboard.getTooltip(UtilsKeyboard.FN_PREVIOUS_SOLDIER);
					tooltipX = UIPanelState.iconSoldierPreviousPoint.x + UIPanelState.tileIconSoldierPrevious.getTileWidth() / 2
							- (UtilFont.getWidth(tooltip) / 2);
					tooltipY = UIPanelState.iconSoldierPreviousPoint.y + UIPanelState.tileBottomItem.getTileHeight();
				} else if (mousePanel == UIPanelState.MOUSE_ICON_SOLDIER_NEXT) {
					tooltip = Messages.getString("UIPanel.6") + UtilsKeyboard.getTooltip(UtilsKeyboard.FN_NEXT_SOLDIER); //$NON-NLS-1$
					tooltipX = UIPanelState.iconSoldierNextPoint.x + UIPanelState.tileIconSoldierNext.getTileWidth() / 2
							- (UtilFont.getWidth(tooltip) / 2);
					tooltipY = UIPanelState.iconSoldierNextPoint.y + UIPanelState.tileBottomItem.getTileHeight();
				} else if (mousePanel == UIPanelState.MOUSE_ICON_HERO_PREVIOUS) {
					tooltip = Messages.getString("UIPanel.22") //$NON-NLS-1$
							+ UtilsKeyboard.getTooltip(UtilsKeyboard.FN_PREVIOUS_HERO);
					tooltipX = UIPanelState.iconHeroPreviousPoint.x + UIPanelState.tileIconHeroPrevious.getTileWidth() / 2
							- (UtilFont.getWidth(tooltip) / 2);
					tooltipY = UIPanelState.iconHeroPreviousPoint.y + UIPanelState.tileBottomItem.getTileHeight();
				} else if (mousePanel == UIPanelState.MOUSE_ICON_HERO_NEXT) {
					tooltip = Messages.getString("UIPanel.23") + UtilsKeyboard.getTooltip(UtilsKeyboard.FN_NEXT_HERO); //$NON-NLS-1$
					tooltipX = UIPanelState.iconHeroNextPoint.x + UIPanelState.tileIconHeroNext.getTileWidth() / 2
							- (UtilFont.getWidth(tooltip) / 2);
					tooltipY = UIPanelState.iconHeroNextPoint.y + UIPanelState.tileBottomItem.getTileHeight();
				} else if (mousePanel == UIPanelState.MOUSE_INFO_NUM_CITIZENS) {
					int happinessMin = (World.getCitizenIDs().size() + World.getSoldierIDs().size()) * 2;
					if (happinessMin < 20) {
						happinessMin = 20;
					} else if (happinessMin > 80) {
						happinessMin = 80;
					}
					tooltip = Messages.getString("UIPanel.8") + " (" + Messages.getString("UIPanel.76") + ": " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
							+ World.getHappinessAverage() + " " + Messages.getString("UIPanel.81") + ": " + happinessMin //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							+ ")"; //$NON-NLS-1$
					tooltipX = UIPanelState.iconNumCitizensBackgroundPoint.x + UIPanelState.tileBottomItem.getTileWidth() / 2
							- (UtilFont.getWidth(tooltip) / 2);
					tooltipY = UIPanelState.iconNumCitizensBackgroundPoint.y + UIPanelState.tileBottomItem.getTileHeight();
				} else if (mousePanel == UIPanelState.MOUSE_INFO_NUM_SOLDIERS) {
					tooltip = Messages.getString("UIPanel.9"); //$NON-NLS-1$
					tooltipX = UIPanelState.iconNumSoldiersBackgroundPoint.x + UIPanelState.tileBottomItem.getTileWidth() / 2
							- (UtilFont.getWidth(tooltip) / 2);
					tooltipY = UIPanelState.iconNumSoldiersBackgroundPoint.y + UIPanelState.tileBottomItem.getTileHeight();
				} else if (mousePanel == UIPanelState.MOUSE_INFO_NUM_HEROES) {
					tooltip = Messages.getString("UIPanel.24"); //$NON-NLS-1$
					tooltipX = UIPanelState.iconNumHeroesBackgroundPoint.x + UIPanelState.tileBottomItem.getTileWidth() / 2
							- (UtilFont.getWidth(tooltip) / 2);
					tooltipY = UIPanelState.iconNumHeroesBackgroundPoint.y + UIPanelState.tileBottomItem.getTileHeight();
				} else if (mousePanel == UIPanelState.MOUSE_INFO_CARAVAN) {
					tooltip = Messages.getString("UIPanel.25") + UtilsKeyboard.getTooltip(UtilsKeyboard.FN_SHOW_TRADE); //$NON-NLS-1$
					tooltipX = UIPanelState.iconCaravanBackgroundPoint.x + UIPanelState.tileBottomItem.getTileWidth() / 2
							- (UtilFont.getWidth(tooltip) / 2);
					tooltipY = UIPanelState.iconCaravanBackgroundPoint.y + UIPanelState.tileBottomItem.getTileHeight();
				} else if (mousePanel == UIPanelState.MOUSE_ICON_PRIORITIES) {
					tooltip = Messages.getString("UIPanel.14") //$NON-NLS-1$
							+ UtilsKeyboard.getTooltip(UtilsKeyboard.FN_SHOW_PRIORITIES);
					tooltipX = UIPanelState.iconPrioritiesPoint.x + UIPanelState.tileIconPriorities.getTileWidth() / 2
							- (UtilFont.getWidth(tooltip) / 2);
					tooltipY = UIPanelState.iconPrioritiesPoint.y + UtilFont.MAX_HEIGHT * 2;
				} else if (mousePanel == UIPanelState.MOUSE_ICON_MATS) {
					tooltip = Messages.getString("UIPanel.32") + UtilsKeyboard.getTooltip(UtilsKeyboard.FN_SHOW_STOCK); //$NON-NLS-1$
					tooltipX = UIPanelState.iconMatsPoint.x + UIPanelState.tileIconMats.getTileWidth() / 2 - (UtilFont.getWidth(tooltip) / 2);
					tooltipY = UIPanelState.iconMatsPoint.y + UtilFont.MAX_HEIGHT * 2;
				} else if (mousePanel == UIPanelState.MOUSE_ICON_GRID) {
					tooltip = Messages.getString("UIPanel.12") + UtilsKeyboard.getTooltip(UtilsKeyboard.FN_TOGGLE_GRID); //$NON-NLS-1$
					tooltipX = UIPanelState.iconGridPoint.x + UIPanelState.tileIconGrid.getTileWidth() / 2 - (UtilFont.getWidth(tooltip) / 2);
					tooltipY = UIPanelState.iconGridPoint.y + UtilFont.MAX_HEIGHT * 2;
				} else if (mousePanel == UIPanelState.MOUSE_ICON_MINIBLOCKS) {
					tooltip = Messages.getString("UIPanel.16") //$NON-NLS-1$
							+ UtilsKeyboard.getTooltip(UtilsKeyboard.FN_TOGGLE_MINIBLOCKS);
					tooltipX = UIPanelState.iconMiniblocksPoint.x + UIPanelState.tileIconMiniblocks.getTileWidth() / 2
							- (UtilFont.getWidth(tooltip) / 2);
					tooltipY = UIPanelState.iconMiniblocksPoint.y + UtilFont.MAX_HEIGHT * 2;
				} else if (mousePanel == UIPanelState.MOUSE_ICON_FLATMOUSE) {
					tooltip = Messages.getString("UIPanel.45") //$NON-NLS-1$
							+ UtilsKeyboard.getTooltip(UtilsKeyboard.FN_TOGGLE_FLAT_MOUSE);
					tooltipX = UIPanelState.iconFlatMousePoint.x + UIPanelState.tileIconFlatMouse.getTileWidth() / 2
							- (UtilFont.getWidth(tooltip) / 2);
					tooltipY = UIPanelState.iconFlatMousePoint.y + UtilFont.MAX_HEIGHT * 2;
				} else if (mousePanel == UIPanelState.	MOUSE_ICON_3DMOUSE) {
					tooltip = Messages.getString("UtilsKeyboard.16") //$NON-NLS-1$
							+ UtilsKeyboard.getTooltip(UtilsKeyboard.FN_TOGGLE_3D_MOUSE);
					tooltipX = UIPanelState.icon3DMousePoint.x + UIPanelState.tileIcon3DMouse.getTileWidth() / 2
							- (UtilFont.getWidth(tooltip) / 2);
					tooltipY = UIPanelState.icon3DMousePoint.y + UtilFont.MAX_HEIGHT * 2;
				} else if (mousePanel == UIPanelState.MOUSE_ICON_PAUSE_RESUME) {
					tooltip = Messages.getString("UIPanel.10") + UtilsKeyboard.getTooltip(UtilsKeyboard.FN_PAUSE); //$NON-NLS-1$
					tooltipX = UIPanelState.iconPauseResumePoint.x + UIPanelState.tileIconPause.getTileWidth() / 2
							- (UtilFont.getWidth(tooltip) / 2);
					tooltipY = UIPanelState.iconPauseResumePoint.y + UtilFont.MAX_HEIGHT * 2;
				} else if (mousePanel == UIPanelState.MOUSE_ICON_SETTINGS) {
					tooltip = Messages.getString("UIPanel.11"); //$NON-NLS-1$
					tooltipX = UIPanelState.iconSettingsPoint.x + UIPanelState.tileIconSettings.getTileWidth() / 2
							- (UtilFont.getWidth(tooltip) / 2);
					tooltipY = UIPanelState.iconSettingsPoint.y + UtilFont.MAX_HEIGHT * 2;
				} else if (mousePanel == UIPanelState.MOUSE_ICON_LOWER_SPEED) {
					tooltip = Messages.getString("UIPanel.1") + UtilsKeyboard.getTooltip(UtilsKeyboard.FN_SPEED_DOWN); //$NON-NLS-1$
					tooltipX = UIPanelState.iconLowerSpeedPoint.x + UIPanelState.tileIconLowerSpeed.getTileWidth() / 2
							- (UtilFont.getWidth(tooltip) / 2);
					tooltipY = UIPanelState.iconLowerSpeedPoint.y + UtilFont.MAX_HEIGHT * 2;
				} else if (mousePanel == UIPanelState.MOUSE_ICON_INCREASE_SPEED) {
					tooltip = Messages.getString("UIPanel.15") + UtilsKeyboard.getTooltip(UtilsKeyboard.FN_SPEED_UP); //$NON-NLS-1$
					tooltipX = UIPanelState.iconIncreaseSpeedPoint.x + UIPanelState.tileIconIncreaseSpeed.getTileWidth() / 2
							- (UtilFont.getWidth(tooltip) / 2);
					tooltipY = UIPanelState.iconIncreaseSpeedPoint.y + UtilFont.MAX_HEIGHT * 2;
				} else if (mousePanel == UIPanelState.MOUSE_TUTORIAL_ICON) {
					tooltip = Messages.getString("UIPanel.75") //$NON-NLS-1$
							+ UtilsKeyboard.getTooltip(UtilsKeyboard.FN_SHOW_MISSION);
					tooltipX = x + 32;
					tooltipY = y;
				} else if (mousePanel == UIPanelState.MOUSE_MESSAGES_ICON_ANNOUNCEMENT) {
					ArrayList<String> alMessages = new ArrayList<String>(4);
					ArrayList<ColorGL> alColors = new ArrayList<ColorGL>(4);
					alMessages.add(Messages.getString("UIPanel.26")); //$NON-NLS-1$
					alColors.add(ColorGL.WHITE);

					tooltip = MessagesPanel.getLastestMessage(MessagesPanel.TYPE_ANNOUNCEMENT, 2);
					if (tooltip != null) {
						alMessages.add(tooltip);
						alColors.add(MessagesPanel.getLastestMessageColor(MessagesPanel.TYPE_ANNOUNCEMENT, 2));
					}
					tooltip = MessagesPanel.getLastestMessage(MessagesPanel.TYPE_ANNOUNCEMENT, 1);
					if (tooltip != null) {
						alMessages.add(tooltip);
						alColors.add(MessagesPanel.getLastestMessageColor(MessagesPanel.TYPE_ANNOUNCEMENT, 1));
					}
					tooltip = MessagesPanel.getLastestMessage(MessagesPanel.TYPE_ANNOUNCEMENT, 0);
					if (tooltip != null) {
						alMessages.add(tooltip);
						alColors.add(MessagesPanel.getLastestMessageColor(MessagesPanel.TYPE_ANNOUNCEMENT, 0));
					}

					tooltipX = UIPanelState.messageIconPoints[0].x;
					tooltipY = UIPanelState.messageIconPoints[0].y + UtilFont.MAX_HEIGHT * 2;
					MainPanel.renderMessages(tooltipX, tooltipY, MainPanel.renderWidth, MainPanel.renderHeight, 2,
							alMessages, alColors);
					return;
				} else if (mousePanel == UIPanelState.MOUSE_MESSAGES_ICON_COMBAT) {
					ArrayList<String> alMessages = new ArrayList<String>(4);
					ArrayList<ColorGL> alColors = new ArrayList<ColorGL>(4);
					alMessages.add(Messages.getString("UIPanel.27")); //$NON-NLS-1$
					alColors.add(ColorGL.WHITE);

					tooltip = MessagesPanel.getLastestMessage(MessagesPanel.TYPE_COMBAT, 2);
					if (tooltip != null) {
						alMessages.add(tooltip);
						alColors.add(MessagesPanel.getLastestMessageColor(MessagesPanel.TYPE_COMBAT, 2));
					}
					tooltip = MessagesPanel.getLastestMessage(MessagesPanel.TYPE_COMBAT, 1);
					if (tooltip != null) {
						alMessages.add(tooltip);
						alColors.add(MessagesPanel.getLastestMessageColor(MessagesPanel.TYPE_COMBAT, 1));
					}
					tooltip = MessagesPanel.getLastestMessage(MessagesPanel.TYPE_COMBAT, 0);
					if (tooltip != null) {
						alMessages.add(tooltip);
						alColors.add(MessagesPanel.getLastestMessageColor(MessagesPanel.TYPE_COMBAT, 0));
					}

					tooltipX = UIPanelState.messageIconPoints[1].x;
					tooltipY = UIPanelState.messageIconPoints[1].y + UtilFont.MAX_HEIGHT * 2;
					MainPanel.renderMessages(tooltipX, tooltipY, MainPanel.renderWidth, MainPanel.renderHeight, 2,
							alMessages, alColors);
					return;
				} else if (mousePanel == UIPanelState.MOUSE_MESSAGES_ICON_HEROES) {
					ArrayList<String> alMessages = new ArrayList<String>(4);
					ArrayList<ColorGL> alColors = new ArrayList<ColorGL>(4);
					alMessages.add(Messages.getString("UIPanel.28")); //$NON-NLS-1$
					alColors.add(ColorGL.WHITE);

					tooltip = MessagesPanel.getLastestMessage(MessagesPanel.TYPE_HEROES, 2);
					if (tooltip != null) {
						alMessages.add(tooltip);
						alColors.add(MessagesPanel.getLastestMessageColor(MessagesPanel.TYPE_HEROES, 2));
					}
					tooltip = MessagesPanel.getLastestMessage(MessagesPanel.TYPE_HEROES, 1);
					if (tooltip != null) {
						alMessages.add(tooltip);
						alColors.add(MessagesPanel.getLastestMessageColor(MessagesPanel.TYPE_HEROES, 1));
					}
					tooltip = MessagesPanel.getLastestMessage(MessagesPanel.TYPE_HEROES, 0);
					if (tooltip != null) {
						alMessages.add(tooltip);
						alColors.add(MessagesPanel.getLastestMessageColor(MessagesPanel.TYPE_HEROES, 0));
					}

					tooltipX = UIPanelState.messageIconPoints[2].x;
					tooltipY = UIPanelState.messageIconPoints[2].y + UtilFont.MAX_HEIGHT * 2;
					MainPanel.renderMessages(tooltipX, tooltipY, MainPanel.renderWidth, MainPanel.renderHeight, 2,
							alMessages, alColors);
					return;
				} else if (mousePanel == UIPanelState.MOUSE_MESSAGES_ICON_SYSTEM) {
					ArrayList<String> alMessages = new ArrayList<String>(4);
					ArrayList<ColorGL> alColors = new ArrayList<ColorGL>(4);
					alMessages.add(Messages.getString("UIPanel.31")); //$NON-NLS-1$
					alColors.add(ColorGL.WHITE);

					tooltip = MessagesPanel.getLastestMessage(MessagesPanel.TYPE_SYSTEM, 2);
					if (tooltip != null) {
						alMessages.add(tooltip);
						alColors.add(MessagesPanel.getLastestMessageColor(MessagesPanel.TYPE_SYSTEM, 2));
					}
					tooltip = MessagesPanel.getLastestMessage(MessagesPanel.TYPE_SYSTEM, 1);
					if (tooltip != null) {
						alMessages.add(tooltip);
						alColors.add(MessagesPanel.getLastestMessageColor(MessagesPanel.TYPE_SYSTEM, 1));
					}
					tooltip = MessagesPanel.getLastestMessage(MessagesPanel.TYPE_SYSTEM, 0);
					if (tooltip != null) {
						alMessages.add(tooltip);
						alColors.add(MessagesPanel.getLastestMessageColor(MessagesPanel.TYPE_SYSTEM, 0));
					}

					tooltipX = UIPanelState.messageIconPoints[3].x;
					tooltipY = UIPanelState.messageIconPoints[3].y + UtilFont.MAX_HEIGHT * 2;
					MainPanel.renderMessages(tooltipX, tooltipY, MainPanel.renderWidth, MainPanel.renderHeight, 2,
							alMessages, alColors);
					return;
				}
			}
		}

		if (tooltip != null) {
			TooltipRenderer.draw(tooltip, tooltipX, tooltipY, UIPanelState.renderWidth, UIPanelState.renderHeight);
		}

		if (UIPanelState.typingPanel == null) {
			// Multi-lineas tooltip
			if (mousePanel == UIPanelState.MOUSE_MESSAGES_ICON_ANNOUNCEMENT) {
				tooltip = MessagesPanel.getLastestMessage(MessagesPanel.TYPE_ANNOUNCEMENT, 2);
				if (tooltip != null) {
					tooltipY += UtilFont.MAX_HEIGHT;
					UtilsGL.drawTooltip(tooltip, tooltipX, tooltipY, UIPanelState.renderWidth, UIPanelState.renderHeight);
				}

				tooltip = MessagesPanel.getLastestMessage(MessagesPanel.TYPE_ANNOUNCEMENT, 1);
				if (tooltip != null) {
					tooltipY += UtilFont.MAX_HEIGHT;
					UtilsGL.drawTooltip(tooltip, tooltipX, tooltipY, UIPanelState.renderWidth, UIPanelState.renderHeight);
				}

				tooltip = MessagesPanel.getLastestMessage(MessagesPanel.TYPE_ANNOUNCEMENT, 0);
				if (tooltip != null) {
					tooltipY += UtilFont.MAX_HEIGHT;
					UtilsGL.drawTooltip(tooltip, tooltipX, tooltipY, UIPanelState.renderWidth, UIPanelState.renderHeight);
				}
			} else if (mousePanel == UIPanelState.MOUSE_MESSAGES_ICON_COMBAT) {
				tooltip = MessagesPanel.getLastestMessage(MessagesPanel.TYPE_COMBAT, 2);
				if (tooltip != null) {
					tooltipY += UtilFont.MAX_HEIGHT;
					UtilsGL.drawTooltip(tooltip, tooltipX, tooltipY, UIPanelState.renderWidth, UIPanelState.renderHeight);
				}

				tooltip = MessagesPanel.getLastestMessage(MessagesPanel.TYPE_COMBAT, 1);
				if (tooltip != null) {
					tooltipY += UtilFont.MAX_HEIGHT;
					UtilsGL.drawTooltip(tooltip, tooltipX, tooltipY, UIPanelState.renderWidth, UIPanelState.renderHeight);
				}

				tooltip = MessagesPanel.getLastestMessage(MessagesPanel.TYPE_COMBAT, 0);
				if (tooltip != null) {
					tooltipY += UtilFont.MAX_HEIGHT;
					UtilsGL.drawTooltip(tooltip, tooltipX, tooltipY, UIPanelState.renderWidth, UIPanelState.renderHeight);
				}
			} else if (mousePanel == UIPanelState.MOUSE_MESSAGES_ICON_HEROES) {
				tooltip = MessagesPanel.getLastestMessage(MessagesPanel.TYPE_HEROES, 2);
				if (tooltip != null) {
					tooltipY += UtilFont.MAX_HEIGHT;
					UtilsGL.drawTooltip(tooltip, tooltipX, tooltipY, UIPanelState.renderWidth, UIPanelState.renderHeight);
				}

				tooltip = MessagesPanel.getLastestMessage(MessagesPanel.TYPE_HEROES, 1);
				if (tooltip != null) {
					tooltipY += UtilFont.MAX_HEIGHT;
					UtilsGL.drawTooltip(tooltip, tooltipX, tooltipY, UIPanelState.renderWidth, UIPanelState.renderHeight);
				}

				tooltip = MessagesPanel.getLastestMessage(MessagesPanel.TYPE_HEROES, 0);
				if (tooltip != null) {
					tooltipY += UtilFont.MAX_HEIGHT;
					UtilsGL.drawTooltip(tooltip, tooltipX, tooltipY, UIPanelState.renderWidth, UIPanelState.renderHeight);
				}
			} else if (mousePanel == UIPanelState.MOUSE_MESSAGES_ICON_SYSTEM) {
				tooltip = MessagesPanel.getLastestMessage(MessagesPanel.TYPE_SYSTEM, 2);
				if (tooltip != null) {
					tooltipY += UtilFont.MAX_HEIGHT;
					UtilsGL.drawTooltip(tooltip, tooltipX, tooltipY, UIPanelState.renderWidth, UIPanelState.renderHeight);
				}

				tooltip = MessagesPanel.getLastestMessage(MessagesPanel.TYPE_SYSTEM, 1);
				if (tooltip != null) {
					tooltipY += UtilFont.MAX_HEIGHT;
					UtilsGL.drawTooltip(tooltip, tooltipX, tooltipY, UIPanelState.renderWidth, UIPanelState.renderHeight);
				}

				tooltip = MessagesPanel.getLastestMessage(MessagesPanel.TYPE_SYSTEM, 0);
				if (tooltip != null) {
					tooltipY += UtilFont.MAX_HEIGHT;
					UtilsGL.drawTooltip(tooltip, tooltipX, tooltipY, UIPanelState.renderWidth, UIPanelState.renderHeight);
				}
			} else if (mousePanel == UIPanelState.MOUSE_EVENTS_ICON) {
				ArrayList<EventData> alEvents = Game.getWorld().getEvents();
				if (alEvents.size() == 0) {
					tooltip = Messages.getString("UIPanel.83"); //$NON-NLS-1$
					UtilsGL.drawTooltip(tooltip,
							UIPanelState.iconEventsPoint.x + GlobalEventData.getIcon().getTileWidth() / 2
									- UtilFont.getWidth(tooltip) / 2,
							UIPanelState.iconEventsPoint.y + GlobalEventData.getIcon().getTileHeight(), UIPanelState.renderWidth, UIPanelState.renderHeight);
				} else {
					// Obtenemos el tamaño del tooltip
					tooltip = Messages.getString("UIPanel.84"); //$NON-NLS-1$
					int tooltipWidth = UtilFont.getWidth(tooltip);
					int tooltipHeight = UtilFont.MAX_HEIGHT; // Título

					EventData ed;
					EventManagerItem emi;
					int iAux;
					for (int i = 0; i < alEvents.size(); i++) {
						ed = alEvents.get(i);
						emi = EventManager.getItem(ed.getEventID());
						if (emi != null) {
							// Alto
							if (emi.getIcon() != null) {
								tooltipHeight += emi.getIcon().getTileHeight() + 2;

								// Ancho
								iAux = UtilFont.getWidth(emi.getName()) + emi.getIcon().getTileWidth();
								if (iAux > tooltipWidth) {
									tooltipWidth = iAux;
								}
							} else {
								tooltipHeight += UtilFont.MAX_HEIGHT + 2;

								// Ancho
								iAux = UtilFont.getWidth(emi.getName());
								if (iAux > tooltipWidth) {
									tooltipWidth = iAux;
								}
							}
						}
					}
					tooltipX = UIPanelState.iconEventsPoint.x + GlobalEventData.getIcon().getTileWidth() / 2 - tooltipWidth / 2;
					tooltipY = UIPanelState.iconEventsPoint.y + GlobalEventData.getIcon().getTileHeight();

					// Renderizamos
					// Fondo
					int iCurrentTexture = UIPanelState.tileTooltipBackground.getTextureID();
					GL11.glColor4f(1, 1, 1, 1);
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, UIPanelState.tileTooltipBackground.getTextureID());
					UtilsGL.glBegin(GL11.GL_QUADS);
					UtilsGL.drawTexture(tooltipX, tooltipY - 4, tooltipX + tooltipWidth + 8,
							tooltipY + tooltipHeight + 4, UIPanelState.tileTooltipBackground.getTileSetTexX0(),
							UIPanelState.tileTooltipBackground.getTileSetTexY0(),
							UIPanelState.tileTooltipBackground.getTileSetTexX1(),
							UIPanelState.tileTooltipBackground.getTileSetTexY1());

					// Iconos
					int iCurrentHeight = tooltipY + UtilFont.MAX_HEIGHT + 2;
					for (int i = 0; i < alEvents.size(); i++) {
						ed = alEvents.get(i);
						emi = EventManager.getItem(ed.getEventID());
						if (emi != null) {
							// Alto
							if (emi.getIcon() != null) {
								iCurrentTexture = UtilsGL.setTexture(emi.getIcon(), iCurrentTexture);
								UIPanel.drawTile(emi.getIcon(), tooltipX, iCurrentHeight, false);
								iCurrentHeight += emi.getIcon().getTileHeight() + 2;
							} else {
								iCurrentHeight += UtilFont.MAX_HEIGHT + 2;
							}
						}
					}
					UtilsGL.glEnd();

					// Textos
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
					UtilsGL.glBegin(GL11.GL_QUADS);
					iCurrentHeight = tooltipY;
					UtilsGL.drawString(tooltip, tooltipX, iCurrentHeight);
					iCurrentHeight += UtilFont.MAX_HEIGHT + 2;

					for (int i = 0; i < alEvents.size(); i++) {
						ed = alEvents.get(i);
						emi = EventManager.getItem(ed.getEventID());
						if (emi != null) {
							// Alto
							if (emi.getIcon() != null) {
								UtilsGL.drawString(emi.getName(), tooltipX + emi.getIcon().getTileWidth() + 4,
										iCurrentHeight + emi.getIcon().getTileHeight() / 2 - UtilFont.MAX_HEIGHT / 2);
								iCurrentHeight += emi.getIcon().getTileHeight() + 2;
							} else {
								UtilsGL.drawString(emi.getName(), tooltipX, iCurrentHeight);
								iCurrentHeight += UtilFont.MAX_HEIGHT + 2;
							}
						}
					}

					UtilsGL.glEnd();
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
				// tooltip = Messages.getString("UIPanel.77"); //$NON-NLS-1$
				// UtilsGL.drawTooltip (tooltip, iconGodsPoint.x + tileIconGods.getTileWidth ()
				// / 2 - UtilFont.getWidth (tooltip) / 2, iconGodsPoint.y +
				// tileIconGods.getTileHeight (), renderWidth, renderHeight);
				// } else {
				// // Obtenemos el tamaño del tooltip
				// tooltip = Messages.getString("UIPanel.78"); //$NON-NLS-1$
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
				// iAux = UtilFont.getWidth (gd.getFullName () + " (" + gd.getStatus () + ")");
				// //$NON-NLS-1$ //$NON-NLS-2$
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
				// tooltipX = iconGodsPoint.x + tileIconGods.getTileWidth () / 2 - tooltipWidth
				// / 2;
				// tooltipY = iconGodsPoint.y + tileIconGods.getTileHeight ();
				//
				// // Renderizamos
				// // Fondo
				// GL11.glColor4f (1, 1, 1, 1);
				// GL11.glBindTexture (GL11.GL_TEXTURE_2D,
				// UIPanel.tileTooltipBackground.getTextureID ());
				// UtilsGL.glBegin (GL11.GL_QUADS);
				// UtilsGL.drawTexture (tooltipX, tooltipY - 4, tooltipX + tooltipWidth + 8,
				// tooltipY + tooltipHeight + 4, UIPanel.tileTooltipBackground.getTileSetTexX0
				// (), UIPanel.tileTooltipBackground.getTileSetTexY0 (),
				// UIPanel.tileTooltipBackground.getTileSetTexX1 (),
				// UIPanel.tileTooltipBackground.getTileSetTexY1 ());
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
				// UtilsGL.drawString (gd.getFullName () + " (" + gd.getStatus () + ")",
				// tooltipX, iCurrentHeight); //$NON-NLS-1$ //$NON-NLS-2$
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

}