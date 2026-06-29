package xaos.panels.menus.rendering;

import org.lwjgl.opengl.GL11;

import xaos.main.Game;
import xaos.panels.MainPanel;
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

        int panelX = x;
        int panelY = y;
        int panelWidth = width;
        int panelHeight = height;

        int contentX = x;
        int contentY = y;
        int contentWidth = width;

        if (!menu.isTrasparency()) {
            panelHeight = height + MenuPanelRenderer.PANEL_PADDING_Y * 2;

            MenuPanelRenderer.renderPanel(
                    menu,
                    panelX,
                    panelY,
                    panelWidth,
                    panelHeight);

            contentX = x + MenuPanelRenderer.PANEL_PADDING_X;
            contentY = y + MenuPanelRenderer.PANEL_PADDING_Y;
            contentWidth = width - MenuPanelRenderer.PANEL_PADDING_X * 2;
        }

        int mouseX = Mouse.getX();
        int mouseY = UtilsGL.getHeight() - Mouse.getY() - 1;
        int itemIndex = -1;

        if (isContext) {
            itemIndex = SmartMenuLayout.getHoveredItemIndex(
                    menu,
                    contentX,
                    contentY,
                    contentWidth,
                    mouseX,
                    mouseY);
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

        // int currentY = y;

        int currentY = contentY;

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
                    contentX,
                    itemY,
                    contentWidth,
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

    public static void renderScrollable(
            SmartMenu menu,
            int x,
            int y,
            int width,
            int height,
            int scrollY,
            boolean isContext) {

        if (menu == null) {
            return;
        }
        int panelX = x;
        int panelY = y;
        int panelWidth = width;
        int panelHeight = height;

        int contentX = x;
        int contentY = y;
        int contentWidth = width;
        int contentHeight = height;

        if (!menu.isTrasparency()) {
            MenuPanelRenderer.renderPanel(
                    menu,
                    panelX,
                    panelY,
                    panelWidth,
                    panelHeight);

            contentX = x + MenuPanelRenderer.PANEL_PADDING_X;
            contentY = y + MenuPanelRenderer.PANEL_PADDING_Y;
            contentWidth = width - MenuPanelRenderer.PANEL_PADDING_X * 2;
            contentHeight = height - MenuPanelRenderer.PANEL_PADDING_Y * 2;
        }

        int mouseX = Mouse.getX();
        int mouseY = UtilsGL.getHeight() - Mouse.getY() - 1;

        int itemIndex = -1;

        if (isContext) {
            int relativeMouseY = mouseY - contentY + scrollY;

            if (mouseX >= contentX && mouseX < contentX + contentWidth
                    && mouseY >= contentY && mouseY < contentY + contentHeight) {

                itemIndex = SmartMenuLayout.getItemIndexAtY(menu, relativeMouseY);

                if (itemIndex >= 0 && itemIndex < menu.getItems().size()) {
                    SmartMenu hoveredItem = menu.getItems().get(itemIndex);

                    if (hoveredItem.getType() == SmartMenu.TYPE_TEXT
                            || hoveredItem.getType() == SmartMenu.TYPE_HEADING) {
                        itemIndex = -1;
                    }
                }
            }
        }
        int currentY = 0;

        for (int i = 0; i < menu.getItems().size(); i++) {
            SmartMenu item = menu.getItems().get(i);

            int itemHeight = SmartMenuLayout.getItemHeight(item);
            int itemScreenY = contentY + currentY - scrollY + 1;

            boolean above = itemScreenY + itemHeight < contentY;
            boolean below = itemScreenY > contentY + contentHeight;

            if (!above && !below) {
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
                        itemScreenY,
                        width,
                        itemHeight,
                        itemIndex == i);
            }

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