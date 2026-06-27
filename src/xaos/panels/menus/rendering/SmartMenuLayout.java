package xaos.panels.menus.rendering;

import xaos.panels.menus.SmartMenu;
import xaos.utils.UtilFont;

public final class SmartMenuLayout {

    private SmartMenuLayout() {
    }

    public static int getItemHeight(SmartMenu item) {
        if (item == null) {
            return UtilFont.MAX_HEIGHT;
        }

        switch (item.getType()) {
            case SmartMenu.TYPE_MENU:
            case SmartMenu.TYPE_BUTTON:
                return UtilFont.MAX_HEIGHT + 30;

            case SmartMenu.TYPE_TOGGLE:
                return UtilFont.MAX_HEIGHT + 6;

            case SmartMenu.TYPE_SLIDER:
                return UtilFont.MAX_HEIGHT + 8;

            case SmartMenu.TYPE_KEYBOARD:
                return UtilFont.MAX_HEIGHT + 8;

            case SmartMenu.TYPE_HEADING:
                return UtilFont.MAX_HEIGHT + 10;

            case SmartMenu.TYPE_TEXT:
            case SmartMenu.TYPE_ITEM:
            default:
                return UtilFont.MAX_HEIGHT;
        }
    }

    public static int getContentHeight(SmartMenu menu) {
        if (menu == null) {
            return 0;
        }

        int contentHeight = 0;

        for (int i = 0; i < menu.getItems().size(); i++) {
            contentHeight += getItemHeight(menu.getItems().get(i));
        }

        return contentHeight;
    }

    public static int getRecommendedWidth(SmartMenu menu) {
        if (menu == null) {
            return 260;
        }

        int recommendedWidth = 260;

        for (int i = 0; i < menu.getItems().size(); i++) {
            SmartMenu item = menu.getItems().get(i);

            if (item.getName() == null) {
                continue;
            }

            int textWidth = UtilFont.getWidth(MenuTextSanitiser.sanitise(item.getName())) + 32;

            if (item.getType() == SmartMenu.TYPE_TOGGLE) {
                textWidth += 50;
            }

            if (item.getType() == SmartMenu.TYPE_SLIDER) {
                textWidth += 160;
            }

            if (textWidth > recommendedWidth) {
                recommendedWidth = textWidth;
            }
        }

        return recommendedWidth;
    }

    public static int getItemIndexAtY(SmartMenu menu, int mouseY) {
        if (menu == null) {
            return -1;
        }

        int currentY = 0;

        for (int i = 0; i < menu.getItems().size(); i++) {
            SmartMenu item = menu.getItems().get(i);
            int itemHeight = getItemHeight(item);

            if (mouseY >= currentY && mouseY < currentY + itemHeight) {
                return i;
            }

            currentY += itemHeight;
        }

        return -1;
    }

    public static int getHoveredItemIndex(SmartMenu menu, int x, int y, int width, int mouseX, int mouseY) {
        if (menu == null || mouseX < x || mouseX >= x + width || mouseY < y) {
            return -1;
        }

        int relativeY = mouseY - y;
        int index = getItemIndexAtY(menu, relativeY);

        if (index < 0 || index >= menu.getItems().size()) {
            return -1;
        }

        SmartMenu item = menu.getItems().get(index);

        if (item.getType() == SmartMenu.TYPE_TEXT || item.getType() == SmartMenu.TYPE_HEADING) {
            return -1;
        }

        return index;
    }
}
