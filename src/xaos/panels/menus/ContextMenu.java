package xaos.panels.menus;

import xaos.panels.MainPanel;
import xaos.panels.menus.rendering.MenuPrimitiveRenderer;
import xaos.utils.UtilFont;
import xaos.utils.UtilsGL;

public class ContextMenu {

    private int x;
    private int y;
    private int width;
    private int height;
    private int scrollY;
    private int maxVisibleHeight;
    private boolean scrollable;

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
        } else {
            getSmartMenu().render(getX(), getY(), getWidth(), getHeight(), true);
        }
    }

    private void renderScrollIndicator() {
        if (!scrollable || getSmartMenu() == null) {
            return;
        }

        int contentHeight = getSmartMenu().getContentHeight() + 2;

        if (contentHeight <= getHeight()) {
            return;
        }

        int barWidth = 6;
        int barX = getX() + getWidth() - barWidth - 2;
        int trackY = getY() + 2;
        int trackHeight = getHeight() - 4;

        float visibleRatio = (float) getHeight() / (float) contentHeight;
        int thumbHeight = (int) (trackHeight * visibleRatio);

        if (thumbHeight < 12) {
            thumbHeight = 12;
        }

        int maxScrollY = contentHeight - getHeight();
        int maxThumbTravel = trackHeight - thumbHeight;

        int thumbY = trackY;

        if (maxScrollY > 0) {
            thumbY = trackY + (int) (((float) scrollY / (float) maxScrollY) * maxThumbTravel);
        }

        MenuPrimitiveRenderer.drawColoredRect(barX, trackY, barWidth, trackHeight, 0.12f, 0.08f, 0.05f);
        MenuPrimitiveRenderer.drawColoredRect(barX + 1, thumbY, barWidth - 2, thumbHeight, 0.72f, 0.66f, 0.50f);
    }

    public void mouseWheelMoved(int amount) {
        System.out.println("Context menu wheel: " + amount + " scrollable=" + scrollable + " scrollY=" + scrollY);

        if (!scrollable || getSmartMenu() == null) {
            return;
        }

        int scrollStep = UtilFont.MAX_HEIGHT * 3;

        if (amount > 0) {
            scrollY -= scrollStep;
        } else if (amount < 0) {
            scrollY += scrollStep;
        }

        int maxScrollY = getSmartMenu().getContentHeight() + 2 - getHeight();

        if (scrollY < 0) {
            scrollY = 0;
        }

        if (scrollY > maxScrollY) {
            scrollY = maxScrollY;
        }

        System.out.println("New scrollY=" + scrollY + " maxScrollY=" + maxScrollY);
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

        if (scrollable) {
            setSmartMenu(getSmartMenu().mousePressed(x, y + scrollY));
        } else {
            setSmartMenu(getSmartMenu().mousePressed(x, y));
        }

        resize();
    }

    public void resize() {
        if (getSmartMenu() == null) {
            return;
        }

        int maxHeight = MainPanel.renderHeight - 4 * UtilFont.MAX_HEIGHT;
        int contentHeight = smartMenu.getContentHeight() + 2;

        scrollable = contentHeight > maxHeight;

        if (scrollable) {
            setHeight(maxHeight);
        } else {
            setHeight(contentHeight);
            scrollY = 0;
        }

        int maxScrollY = contentHeight - getHeight();

        if (scrollY < 0) {
            scrollY = 0;
        }

        if (scrollY > maxScrollY) {
            scrollY = maxScrollY;
        }

        int iMaxWidth = 1;
        int iWidth;

        for (int i = 0; i < smartMenu.getItems().size(); i++) {
            if (smartMenu.getItems().get(i).getName() != null) {
                iWidth = UtilFont.getWidth(smartMenu.getItems().get(i).getName());

                if (iMaxWidth < iWidth) {
                    iMaxWidth = iWidth;
                }
            }
        }

        setWidth(iMaxWidth + 32);

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

    // public void resize() {

    // if (getSmartMenu() == null)
    // return;
    // // int iHeight = smartMenu.getContentHeight() + 2;
    // int maxHeight = MainPanel.renderHeight - 4 * UtilFont.MAX_HEIGHT;
    // int contentHeight = smartMenu.getContentHeight() + 2;

    // scrollable = contentHeight > maxHeight;
    // maxVisibleHeight = scrollable ? maxHeight : contentHeight;

    // if (!scrollable) {
    // scrollY = 0;
    // } else {
    // int maxScrollY = contentHeight - maxVisibleHeight;

    // if (scrollY > maxScrollY) {
    // scrollY = maxScrollY;
    // }

    // if (scrollY < 0) {
    // scrollY = 0;
    // }
    // }

    // int iHeight = maxVisibleHeight;
    // // if (iHeight >= (MainPanel.renderHeight - 4 * UtilFont.MAX_HEIGHT)) {
    // // // Demasiado grande, lo dividimos
    // // int iParts = 1 + (iHeight / (MainPanel.renderHeight - 4 *
    // // UtilFont.MAX_HEIGHT));
    // // // Hay que añadir 2 items por cada menu (blanco y forward), así que lo
    // // tenemos
    // // // en cuenta
    // // if (iParts > 1) {
    // // int newItemsSize = (iParts - 1) * (3 * UtilFont.MAX_HEIGHT);
    // // iParts = 1 + ((iHeight + newItemsSize) / (MainPanel.renderHeight - 4 *
    // // UtilFont.MAX_HEIGHT));
    // // }
    // // this.smartMenu = SmartMenu.split(this.smartMenu, iParts);
    // // iHeight = this.smartMenu.getContentHeight() + 2;
    // // }

    // // Modificamos alto y ancho
    // int iMaxWidth = 1, iWidth;
    // for (int i = 0; i < smartMenu.getItems().size(); i++) {
    // if (smartMenu.getItems().get(i).getName() != null) {
    // iWidth = UtilFont.getWidth(smartMenu.getItems().get(i).getName());
    // if (iMaxWidth < iWidth) {
    // iMaxWidth = iWidth;
    // }
    // }
    // }

    // setWidth(iMaxWidth + 32);
    // setHeight(iHeight);

    // // Modificamos X e Y si es que no cabe
    // if (getX() + getWidth() > UtilsGL.getWidth()) {
    // setX(UtilsGL.getWidth() - getWidth());
    // } else if (getX() < 0) {
    // setX(0);
    // }
    // if (getY() + getHeight() > UtilsGL.getHeight()) {
    // setY(UtilsGL.getHeight() - getHeight());
    // } else if (getY() < 0) {
    // setY(0);
    // }

    // }
}
