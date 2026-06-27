package xaos.panels.menus.rendering;

import org.lwjgl.opengl.GL11;

import xaos.main.Game;
import xaos.panels.MainPanel;
import xaos.panels.UI.UIPanelState;
import xaos.panels.menus.SmartMenu;
import xaos.platform.lwjgl3.input.Mouse;
import xaos.tiles.Tile;
import xaos.utils.Utils;
import xaos.utils.UtilsGL;

public final class SmartMenuRenderer {

    private SmartMenuRenderer() {
    }

    public static void render(SmartMenu menu, int x, int y, int width, int height, boolean isContext) {
        if (menu == null) {
            return;
        }

        height = SmartMenuLayout.getContentHeight(menu);

        if (!menu.isTrasparency()) {
            GL11.glColor4f(1, 1, 1, 1);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, UIPanelState.tileTooltipBackground.getTextureID());

            UtilsGL.glBegin(GL11.GL_QUADS);
            UtilsGL.drawTexture(
                    x,
                    y,
                    x + width,
                    y + height,
                    UIPanelState.tileTooltipBackground.getTileSetTexX0(),
                    UIPanelState.tileTooltipBackground.getTileSetTexY0(),
                    UIPanelState.tileTooltipBackground.getTileSetTexX1(),
                    UIPanelState.tileTooltipBackground.getTileSetTexY1());
            UtilsGL.glEnd();
        }

        int mouseX = Mouse.getX();
        int mouseY = UtilsGL.getHeight() - Mouse.getY() - 1;
        int itemIndex = -1;

        if (isContext) {
            itemIndex = SmartMenuLayout.getHoveredItemIndex(menu, x, y, width, mouseX, mouseY);
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

        int currentY = y;

        for (int i = 0; i < menu.getItems().size(); i++) {
            SmartMenu item = menu.getItems().get(i);

            int itemHeight = SmartMenuLayout.getItemHeight(item);
            int itemY = currentY + 1;

            String text;

            if (item.isDynamic()) {
                text = Utils.getDynamicString(item.getName());
            } else {
                text = item.getName();
            }

            renderMenuItemByType(
                    item,
                    text,
                    x,
                    itemY,
                    width,
                    itemHeight,
                    itemIndex == i);

            currentY += itemHeight;
        }

        if (itemIndex != -1) {
            SmartMenu menuItem = menu.getItems().get(itemIndex);

            if (menuItem.getPrerequisites() != null && menuItem.getPrerequisites().size() > 0) {
                MainPanel.renderMessages(
                        mouseX,
                        mouseY + Tile.TERRAIN_ICON_HEIGHT / 2,
                        UtilsGL.getWidth(),
                        UtilsGL.getHeight(),
                        Tile.TERRAIN_ICON_WIDTH / 2,
                        menuItem.getPrerequisites(),
                        menuItem.getPrerequisitesColor());
            }
        }
    }

    private static void renderMenuItemByType(
            SmartMenu item,
            String text,
            int x,
            int y,
            int width,
            int height,
            boolean hovered) {

        switch (item.getType()) {
            case SmartMenu.TYPE_BUTTON:
                MenuButtonRenderer.render(item, text, x, y, width, height, hovered);
                break;

            case SmartMenu.TYPE_TOGGLE:
                MenuToggleRenderer.render(item, text, x, y, width, height, hovered);
                break;

            case SmartMenu.TYPE_SLIDER:
                MenuSliderRenderer.render(item, text, x, y, width, height, hovered);
                break;

            case SmartMenu.TYPE_KEYBOARD:
                MenuKeyboardRenderer.render(item, text, x, y, width, height, hovered);
                break;

            case SmartMenu.TYPE_HEADING:
                MenuTextRenderer.renderText(item, text, x, y);
                break;

            case SmartMenu.TYPE_TEXT:
                MenuTextRenderer.renderText(item, text, x, y);
                break;

            case SmartMenu.TYPE_MENU:
                MenuButtonRenderer.render(item, text, x, y, width, height, hovered);
                break;

            case SmartMenu.TYPE_ITEM:
            default:
                MenuTextRenderer.renderText(item, text, x, y);
                break;
        }
    }
}