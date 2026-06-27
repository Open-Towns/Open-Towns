package xaos.panels.menus;

import xaos.panels.MainPanel;
import xaos.utils.UtilFont;
import xaos.utils.UtilsGL;

public class ContextMenu {

    private int x;
    private int y;
    private int width;
    private int height;

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
        getSmartMenu().render(getX(), getY(), getWidth(), getHeight(), true);
    }

    public void mousePressed(int x, int y) {
        if (getSmartMenu() == null)
            return;
        setSmartMenu(getSmartMenu().mousePressed(x, y));
    }

    public void resize() {

        if (getSmartMenu() == null)
            return;
        int iHeight = smartMenu.getContentHeight() + 2;
        if (iHeight >= (MainPanel.renderHeight - 4 * UtilFont.MAX_HEIGHT)) {
            // Demasiado grande, lo dividimos
            int iParts = 1 + (iHeight / (MainPanel.renderHeight - 4 * UtilFont.MAX_HEIGHT));
            // Hay que añadir 2 items por cada menu (blanco y forward), así que lo tenemos
            // en cuenta
            if (iParts > 1) {
                int newItemsSize = (iParts - 1) * (3 * UtilFont.MAX_HEIGHT);
                iParts = 1 + ((iHeight + newItemsSize) / (MainPanel.renderHeight - 4 * UtilFont.MAX_HEIGHT));
            }
            this.smartMenu = SmartMenu.split(this.smartMenu, iParts);
            iHeight = this.smartMenu.getContentHeight() + 2;
        }

        // Modificamos alto y ancho
        int iMaxWidth = 1, iWidth;
        for (int i = 0; i < smartMenu.getItems().size(); i++) {
            if (smartMenu.getItems().get(i).getName() != null) {
                iWidth = UtilFont.getWidth(smartMenu.getItems().get(i).getName());
                if (iMaxWidth < iWidth) {
                    iMaxWidth = iWidth;
                }
            }
        }

        setWidth(iMaxWidth + 32);
        setHeight(iHeight);

        // Modificamos X e Y si es que no cabe
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
