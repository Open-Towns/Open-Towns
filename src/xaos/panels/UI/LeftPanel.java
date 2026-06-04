package xaos.panels.UI;

import static xaos.panels.UI.UIPanel.*;
import static xaos.panels.UI.UIPanelInputHandler.*;
import static xaos.panels.UI.UIPanelScaler.*;
import static xaos.panels.UI.UIPanelState.*;

import java.awt.Point;
import java.util.HashMap;

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

    private static Point getScaledProductionPanelPoint() {
        /*
         * Left-side panel:
         * keep the original top-left anchor and grow right/down.
         */
        return productionPanelPoint;
    }

    private static Point getScaledProductionPoint(Point originalPoint, Point scaledProductionPanelPoint) {
        return scalePointFromAnchor(
                originalPoint,
                productionPanelPoint,
                scaledProductionPanelPoint);
    }

    private static Point getScaledProductionItemPoint(int itemIndex, Point scaledProductionPanelPoint) {
        return getScaledProductionPoint(
                productionPanelItemsPosition.get(itemIndex),
                scaledProductionPanelPoint);
    }

    private static Point getScaledProductionPlusRegularPoint(int itemIndex, Point scaledProductionPanelPoint) {
        return getScaledProductionPoint(
                productionPanelItemsPlusRegularPosition.get(itemIndex),
                scaledProductionPanelPoint);
    }

    private static Point getScaledProductionPlusAutomatedPoint(int itemIndex, Point scaledProductionPanelPoint) {
        return getScaledProductionPoint(
                productionPanelItemsPlusAutomatedPosition.get(itemIndex),
                scaledProductionPanelPoint);
    }

    private static Point getScaledProductionMinusRegularPoint(int itemIndex, Point scaledProductionPanelPoint) {
        return getScaledProductionPoint(
                productionPanelItemsMinusRegularPosition.get(itemIndex),
                scaledProductionPanelPoint);
    }

    private static Point getScaledProductionMinusAutomatedPoint(int itemIndex, Point scaledProductionPanelPoint) {
        return getScaledProductionPoint(
                productionPanelItemsMinusAutomatedPosition.get(itemIndex),
                scaledProductionPanelPoint);
    }

    private static Point getScaledOpenCloseProductionPoint(Point scaledProductionPanelPoint) {
        return getScaledProductionPoint(
                tileOpenCloseProductionPanelPoint,
                scaledProductionPanelPoint);
    }

    public static void renderProductionPanel(int mouseX, int mouseY, int mousePanel) {
        checkBlinkProduction = (blinkTurns >= MAX_BLINK_TURNS / 2) && TutorialFlow.isBlinkProduction();

        int scaledPanelWidth = ui(PRODUCTION_PANEL_WIDTH);
        int scaledPanelHeight = ui(PRODUCTION_PANEL_HEIGHT);

        int scaledItemWidth = ui(BOTTOM_ITEM_WIDTH);
        int scaledItemHeight = ui(BOTTOM_ITEM_HEIGHT);

        int scaledIconWidth = ui(ICON_WIDTH);
        int scaledIconHeight = ui(ICON_HEIGHT);

        int scaledProductionItemWidth = ui(PRODUCTION_PANEL_ITEM_WIDTH);
        int scaledProductionItemHeight = ui(PRODUCTION_PANEL_ITEM_HEIGHT);

        Point scaledProductionPanelPoint = getScaledProductionPanelPoint();

        if (isProductionPanelActive()) {
            int iCurrentTexture = tileProductionPanel[0].getTextureID();

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, iCurrentTexture);
            GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
            UtilsGL.glBegin(GL11.GL_QUADS);

            renderBackground(
                    tileProductionPanel,
                    scaledProductionPanelPoint,
                    scaledPanelWidth,
                    scaledPanelHeight);

            int iMenu;
            Point point;
            SmartMenu smItem;
            Point pItem;

            if (mousePanel == MOUSE_PRODUCTION_PANEL_ITEMS
                    || mousePanel == MOUSE_PRODUCTION_PANEL_ITEMS_MINUS_AUTOMATED
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

                        point = getScaledProductionItemPoint(iMenu, scaledProductionPanelPoint);

                        boolean bBlinkItem = checkBlinkProduction
                                && TutorialFlow.currentBlinkProduction(
                                        productionPanelMenu.getItems().get(iMenu).getID());

                        TutorialFlow tutFlow = null;

                        if (bBlinkItem
                                && Game.getCurrentMissionData() != null
                                && ImagesPanel.getCurrentFlowIndex() >= 0
                                && ImagesPanel.getCurrentFlowIndex() < Game.getCurrentMissionData()
                                        .getTutorialFlows().size()) {
                            tutFlow = Game.getCurrentMissionData().getTutorialFlows()
                                    .get(ImagesPanel.getCurrentFlowIndex());
                        }

                        /*
                         * Round button
                         */
                        if (productionPanelMenu.getItems().get(iMenu).getType() == SmartMenu.TYPE_MENU) {
                            iCurrentTexture = UtilsGL.setTexture(tileBottomItemSM, iCurrentTexture);

                            if (bBlinkItem) {
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

                            if (bBlinkItem) {
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

                        /*
                         * UI icon
                         */
                        tile = productionPanelMenu.getItems().get(iMenu).getIcon();

                        if (tile != null
                                && productionPanelMenu.getItems().get(iMenu).getIconType() == SmartMenu.ICON_TYPE_UI) {
                            iCurrentTexture = UtilsGL.setTexture(tile, iCurrentTexture);

                            drawScaledIcon(
                                    tile,
                                    point,
                                    scaledItemWidth,
                                    scaledItemHeight);
                        }

                        /*
                         * Plus/minus icons
                         */
                        Point plusRegularPoint = productionPanelItemsPlusRegularPosition.get(iMenu);

                        if (plusRegularPoint.x != -1) {
                            // Regular plus
                            iCurrentTexture = UtilsGL.setTexture(tileProductionPanelPlusIcon, iCurrentTexture);

                            if (tutFlow != null && tutFlow.isBlinkProductionRegularPlus()) {
                                UtilsGL.setColorRed();
                            }

                            drawScaledTile(
                                    tileProductionPanelPlusIcon,
                                    getScaledProductionPlusRegularPoint(iMenu, scaledProductionPanelPoint),
                                    scaledIconWidth,
                                    scaledIconHeight);

                            if (tutFlow != null && tutFlow.isBlinkProductionRegularPlus()) {
                                UtilsGL.unsetColor();
                            }

                            // Automated plus
                            if (tutFlow != null && tutFlow.isBlinkProductionAutomatedPlus()) {
                                UtilsGL.setColorRed();
                            }

                            drawScaledTile(
                                    tileProductionPanelPlusIcon,
                                    getScaledProductionPlusAutomatedPoint(iMenu, scaledProductionPanelPoint),
                                    scaledIconWidth,
                                    scaledIconHeight);

                            if (tutFlow != null && tutFlow.isBlinkProductionAutomatedPlus()) {
                                UtilsGL.unsetColor();
                            }

                            iCurrentTexture = UtilsGL.setTexture(tileProductionPanelMinusIcon, iCurrentTexture);

                            // Regular minus
                            if (tutFlow != null && tutFlow.isBlinkProductionRegularMinus()) {
                                UtilsGL.setColorRed();
                            }

                            drawScaledTile(
                                    tileProductionPanelMinusIcon,
                                    getScaledProductionMinusRegularPoint(iMenu, scaledProductionPanelPoint),
                                    scaledIconWidth,
                                    scaledIconHeight);

                            if (tutFlow != null && tutFlow.isBlinkProductionRegularMinus()) {
                                UtilsGL.unsetColor();
                            }

                            // Automated minus
                            if (tutFlow != null && tutFlow.isBlinkProductionAutomatedMinus()) {
                                UtilsGL.setColorRed();
                            }

                            drawScaledTile(
                                    tileProductionPanelMinusIcon,
                                    getScaledProductionMinusAutomatedPoint(iMenu, scaledProductionPanelPoint),
                                    scaledIconWidth,
                                    scaledIconHeight);

                            if (tutFlow != null && tutFlow.isBlinkProductionAutomatedMinus()) {
                                UtilsGL.unsetColor();
                            }
                        }
                    }
                }
            }

            UtilsGL.glEnd();

            /*
             * ITEM TEXTURES
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

                        point = getScaledProductionItemPoint(iMenu, scaledProductionPanelPoint);

                        Tile tile = productionPanelMenu.getItems().get(iMenu).getIcon();

                        if (tile != null
                                && productionPanelMenu.getItems().get(iMenu)
                                        .getIconType() == SmartMenu.ICON_TYPE_ITEM) {
                            iCurrentTexture = UtilsGL.setTexture(tile, iCurrentTexture);

                            drawScaledIcon(
                                    tile,
                                    point,
                                    scaledItemWidth,
                                    scaledItemHeight);
                        }
                    }
                }

                UtilsGL.glEnd();
            }

            /*
             * NUMBERS
             *
             * The font itself is not scaled here. This only scales the position.
             * If you later add real font scaling, this section can be updated again.
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
                                point = getScaledProductionItemPoint(iMenu, scaledProductionPanelPoint);

                                iItemQueue = hmItemsOnQueue.get(smItem.getParameter());

                                if (iItemQueue == null) {
                                    strValue = "0"; //$NON-NLS-1$
                                } else {
                                    strValue = Integer.toString(iItemQueue);
                                }

                                // Regular queue number
                                UtilsGL.drawStringWithBorder(
                                        strValue,
                                        point.x - (scaledIconWidth / 2) - (UtilFont.getWidth(strValue) / 2),
                                        point.y + (scaledProductionItemHeight / 2) - (UtilFont.MAX_HEIGHT / 2),
                                        ColorGL.WHITE,
                                        ColorGL.BLACK);

                                // Automated queue number
                                strValue = Integer.toString(Game.getWorld().getTaskManager()
                                        .getNumItemsOnAutomatedQueue(smItem.getParameter()));

                                UtilsGL.drawStringWithBorder(
                                        strValue,
                                        point.x + scaledProductionItemWidth + (scaledIconWidth / 2)
                                                - (UtilFont.getWidth(strValue) / 2),
                                        point.y + (scaledProductionItemHeight / 2) - (UtilFont.MAX_HEIGHT / 2),
                                        ColorGL.WHITE,
                                        ColorGL.BLACK);

                                // Items in world
                                ActionManagerItem ami = ActionManager.getItem(smItem.getParameter());

                                if (ami != null && ami.getGeneratedItem() != null) {
                                    int iNum = Item.getNumItems(
                                            UtilsIniHeaders.getIntIniHeader(ami.getGeneratedItem()),
                                            false,
                                            World.MAP_DEPTH);

                                    if (iNum > 0) {
                                        strValue = Integer.toString(iNum);

                                        UtilsGL.drawStringWithBorder(
                                                strValue,
                                                point.x + (scaledProductionItemWidth / 2)
                                                        - (UtilFont.getWidth(strValue) / 2),
                                                point.y + (scaledProductionItemHeight / 4)
                                                        - (UtilFont.MAX_HEIGHT / 2),
                                                ColorGL.WHITE,
                                                ColorGL.BLACK);
                                    }
                                }
                            }
                        }
                    }
                }

                UtilsGL.glEnd();
            }
        }

        /*
         * Open/close button
         */
        Point openClosePoint = getScaledOpenCloseProductionPoint(scaledProductionPanelPoint);

        if (isProductionPanelLocked()) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, tileOpenProductionPanelON.getTextureID());
            GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
            UtilsGL.glBegin(GL11.GL_QUADS);

            drawScaledTile(
                    tileOpenProductionPanelON,
                    openClosePoint,
                    ui(tileOpenProductionPanelON.getTileWidth()),
                    ui(tileOpenProductionPanelON.getTileHeight()));

            UtilsGL.glEnd();
        } else {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, tileOpenProductionPanel.getTextureID());
            GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
            UtilsGL.glBegin(GL11.GL_QUADS);

            if (checkBlinkProduction) {
                UtilsGL.setColorRed();
            }

            drawScaledTile(
                    tileOpenProductionPanel,
                    openClosePoint,
                    ui(tileOpenProductionPanel.getTileWidth()),
                    ui(tileOpenProductionPanel.getTileHeight()));

            if (checkBlinkProduction) {
                UtilsGL.unsetColor();
            }

            UtilsGL.glEnd();
        }
    }
}