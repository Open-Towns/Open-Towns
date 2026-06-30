package xaos.panels.menus.rendering;

import xaos.panels.menus.SmartMenu;
import xaos.utils.UtilFont;
import xaos.utils.Utils;

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
            case SmartMenu.TYPE_TOGGLE:
            case SmartMenu.TYPE_SLIDER:
            case SmartMenu.TYPE_ITEM:
            case SmartMenu.TYPE_KEYBOARD:
            case SmartMenu.TYPE_CYCLE:

                return UtilFont.MAX_HEIGHT + 30;

            case SmartMenu.TYPE_HEADING:
                return UtilFont.MAX_HEIGHT + 10;
            case SmartMenu.TYPE_TEXT:

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
        int maxWidth = 1;

        for (int i = 0; i < menu.getItems().size(); i++) {
            SmartMenu item = menu.getItems().get(i);

            if (item.getName() == null) {
                continue;
            }

            String text = item.getName();

            if (item.isDynamic()) {
                text = Utils.getDynamicString(item.getName());
            }

            text = MenuTextSanitiser.sanitise(text);

            int itemWidth = UtilFont.getWidth(text);

            if (item.getType() == SmartMenu.TYPE_ITEM || item.getType() == SmartMenu.TYPE_MENU) {
                itemWidth += 60;
            }
            if (item.getType() == SmartMenu.TYPE_TEXT) {
                itemWidth += 50;
            }

            if (item.getType() == SmartMenu.TYPE_HEADING) {
                itemWidth += 90;
            }

            if (item.getType() == SmartMenu.TYPE_TOGGLE) {
                itemWidth += 130;
            }

            if (item.getType() == SmartMenu.TYPE_SLIDER) {
                itemWidth += 210;
            }
            if (item.getType() == SmartMenu.TYPE_CYCLE) {
                itemWidth += 210;
            }

            if (itemWidth > maxWidth) {
                maxWidth = itemWidth;
            }
        }

        return maxWidth + 32;
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
