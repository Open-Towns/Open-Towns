package xaos.panels.UI;

import static xaos.panels.UI.UIPanel.*;
import static xaos.panels.UI.UIPanelInputHandler.*;
import static xaos.panels.UI.UIPanelScaler.*;
import static xaos.panels.UI.UIPanelState.*;

import java.util.HashMap;
import java.awt.Point;

import org.lwjgl.opengl.GL11;

import xaos.actions.ActionManager;
import xaos.actions.ActionManagerItem;
import xaos.campaign.TutorialFlow;
import xaos.main.Game;
import xaos.main.World;
import xaos.panels.CommandPanel;
import xaos.panels.ImagesPanel;
import xaos.panels.menus.SmartMenu;
import xaos.tiles.Tile;
import xaos.tiles.entities.items.Item;
import xaos.utils.ColorGL;
import xaos.utils.UtilFont;
import xaos.utils.UtilsGL;
import xaos.utils.UtilsIniHeaders;

public class LeftPanel {
    public static void renderProductionPanel(int mouseX, int mouseY, int mousePanel) {
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
}


