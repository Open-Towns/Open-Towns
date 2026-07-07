package xaos.panels.menus;

import xaos.panels.MainPanel;
import xaos.panels.menus.rendering.MenuPanelRenderer;
import xaos.panels.menus.rendering.MenuPrimitiveRenderer;
import xaos.utils.UtilFont;
import xaos.utils.UtilsGL;

public class ContextMenu {

    private int x;
    private int y;
    private int width;
    private int height;
    private int scrollY;
    private boolean scrollable;
    private static final int MAX_KEYBOARD_VISIBLE_ROWS = 10;
private boolean centered;
    private SmartMenu smartMenu;

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public SmartMenu getSmartMenu() {
        return smartMenu;
    }

    public void setSmartMenu(SmartMenu smartMenu) {
        this.smartMenu = smartMenu;

        resize();
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void render() {
        if (getSmartMenu() == null) {
            return;
        }

        if (scrollable) {
            getSmartMenu().renderScrollable(
                    getX(),
                    getY(),
                    getWidth(),
                    getHeight(),
                    scrollY,
                    true);

            renderScrollBar();
        } else {
            getSmartMenu().render(getX(), getY(), getWidth(), getHeight(), true);
        }
    }
public void setCentered(boolean centered) {
    this.centered = centered;
}

public boolean isCentered() {
    return centered;
}private void centerOnScreen() {
    setX(UtilsGL.getWidth() / 2 - getWidth() / 2);
    setY(UtilsGL.getHeight() / 2 - getHeight() / 2);
}
    private boolean isKeyboardMenu() {
        if (smartMenu == null || smartMenu.getItems() == null) {
            return false;
        }

        for (int i = 0; i < smartMenu.getItems().size(); i++) {
            if (smartMenu.getItems().get(i).getType() == SmartMenu.TYPE_KEYBOARD) {
                return true;
            }
        }

        return false;
    }

    private void renderScrollBar() {
        if (!scrollable || getSmartMenu() == null) {
            return;
        }

        int contentHeight = getSmartMenu().getContentHeight() + 2;
        if (!smartMenu.isTrasparency()) {
            contentHeight += MenuPanelRenderer.PANEL_PADDING_Y * 2;
        }
        if (contentHeight <= getHeight()) {
            return;
        }

        int barWidth = 8;
        int padding = 3;

        int trackX = getX() + getWidth() - barWidth - padding;
        int trackY = getY() + padding;
        int trackHeight = getHeight() - padding * 2;

        if (trackHeight <= 0) {
            return;
        }

        float visibleRatio = (float) getHeight() / (float) contentHeight;

        int thumbHeight = (int) (trackHeight * visibleRatio);

        if (thumbHeight < 14) {
            thumbHeight = 14;
        }

        if (thumbHeight > trackHeight) {
            thumbHeight = trackHeight;
        }

        int maxScrollY = contentHeight - getHeight();
        int maxThumbTravel = trackHeight - thumbHeight;

        int thumbY = trackY;

        if (maxScrollY > 0 && maxThumbTravel > 0) {
            thumbY = trackY + (int) (((float) scrollY / (float) maxScrollY) * maxThumbTravel);
        }

        // Track - dark recessed line
        MenuPrimitiveRenderer.drawColoredRect(
                trackX,
                trackY,
                barWidth,
                trackHeight,
                0.12f,
                0.08f,
                0.05f);

        // Inner track
        MenuPrimitiveRenderer.drawColoredRect(
                trackX + 1,
                trackY + 1,
                barWidth - 2,
                trackHeight - 2,
                0.22f,
                0.16f,
                0.10f);

        // Thumb - Towns-style parchment/brown
        MenuPrimitiveRenderer.drawColoredRect(
                trackX + 1,
                thumbY,
                barWidth - 2,
                thumbHeight,
                0.72f,
                0.66f,
                0.50f);

        // Thumb highlight
        MenuPrimitiveRenderer.drawColoredRect(
                trackX + 2,
                thumbY + 1,
                barWidth - 4,
                2,
                0.86f,
                0.78f,
                0.58f);

        // Thumb shadow
        MenuPrimitiveRenderer.drawColoredRect(
                trackX + 2,
                thumbY + thumbHeight - 3,
                barWidth - 4,
                2,
                0.32f,
                0.24f,
                0.16f);
    }
private int getMenuContentHeight() {
    if (smartMenu == null) {
        return 0;
    }

    return smartMenu.getContentHeight() + 2;
}

private int getVisibleContentHeight() {
    int visibleHeight = getHeight();

    if (smartMenu != null && !smartMenu.isTrasparency()) {
        visibleHeight -= MenuPanelRenderer.PANEL_PADDING_Y * 2;
    }

    if (visibleHeight < 0) {
        visibleHeight = 0;
    }

    return visibleHeight;
}

private int getMaxScrollY() {
    int maxScrollY = getMenuContentHeight() - getVisibleContentHeight();

    if (maxScrollY < 0) {
        return 0;
    }

    return maxScrollY;
}

private void clampScrollY() {
    int maxScrollY = getMaxScrollY();

    if (scrollY < 0) {
        scrollY = 0;
    }

    if (scrollY > maxScrollY) {
        scrollY = maxScrollY;
    }
}
    public void mouseWheelMoved(int amount) {
    if (!scrollable || getSmartMenu() == null) {
        return;
    }

    int scrollStep = UtilFont.MAX_HEIGHT * 3;

    if (amount > 0) {
        scrollY -= scrollStep;
    } else if (amount < 0) {
        scrollY += scrollStep;
    }

    clampScrollY();
}

    public void mousePressed(int x, int y) {
        if (getSmartMenu() == null) {
            return;
        }

        if (x < 0 || x >= getWidth()) {
            return;
        }

        if (y < 0 || y >= getHeight()) {
            return;
        }

        int menuX = x;
        int menuY = y;

        if (!getSmartMenu().isTrasparency()) {
            menuX -= MenuPanelRenderer.PANEL_PADDING_X;
            menuY -= MenuPanelRenderer.PANEL_PADDING_Y;
        }

        if (menuX < 0 || menuY < 0) {
            return;
        }

        int finalMenuY = scrollable ? menuY + scrollY : menuY;

        setSmartMenu(getSmartMenu().mousePressed(menuX, finalMenuY, getContentClickWidth()));
        resize();
    }

    private int getContentClickWidth() {
        int width = getWidth();

        if (getSmartMenu() != null && !getSmartMenu().isTrasparency()) {
            width -= MenuPanelRenderer.PANEL_PADDING_X * 2;
        }

        return width;
    }

    public void resize() {
    if (getSmartMenu() == null) {
        return;
    }

    int menuContentHeight = smartMenu.getContentHeight() + 8;

   
    int fullPanelHeight = menuContentHeight;

    if (!smartMenu.isTrasparency()) {
        fullPanelHeight += MenuPanelRenderer.PANEL_PADDING_Y * 2;
    }

    int maxHeight = MainPanel.renderHeight - 4 * UtilFont.MAX_HEIGHT;

    if (isKeyboardMenu()) {
        int keyboardRowHeight = UtilFont.MAX_HEIGHT + 24;
        int keyboardMaxHeight = keyboardRowHeight * MAX_KEYBOARD_VISIBLE_ROWS;

        if (!smartMenu.isTrasparency()) {
            keyboardMaxHeight += MenuPanelRenderer.PANEL_PADDING_Y * 2;
        }

        if (keyboardMaxHeight < maxHeight) {
            maxHeight = keyboardMaxHeight;
        }
    }

    scrollable = fullPanelHeight > maxHeight;

    if (scrollable) {
        setHeight(maxHeight);
    } else {
        setHeight(fullPanelHeight);
        scrollY = 0;
    }

    int visibleContentHeight = getHeight();

    if (!smartMenu.isTrasparency()) {
        visibleContentHeight -= MenuPanelRenderer.PANEL_PADDING_Y * 2;
    }

    if (visibleContentHeight < 0) {
        visibleContentHeight = 0;
    }

    int maxScrollY = menuContentHeight - visibleContentHeight;

    if (maxScrollY < 0) {
        maxScrollY = 0;
    }

    if (scrollY < 0) {
        scrollY = 0;
    }

    if (scrollY > maxScrollY) {
        scrollY = maxScrollY;
    }

    int recommendedWidth = smartMenu.getRecommendedWidth();

    if (scrollable) {
        recommendedWidth += 12;
    }

    if (centered) {
        centerOnScreen();
    }
    setWidth(recommendedWidth);
    if (getX() + getWidth() > UtilsGL.getWidth()) {
        setX(UtilsGL.getWidth() - getWidth());
    } else if (getX() < 0) {
        setX(0);
    }

    if (getY() + getHeight() > UtilsGL.getHeight()) {
        setY(UtilsGL.getHeight() - getHeight());
    } else if (getY() < 0) {
        setY(0);
    }
}

}
