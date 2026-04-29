package xaos.panels;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.ImageIcon;

import xaos.platform.lwjgl3.opengl.Display;
import org.lwjgl.opengl.GL11;
import xaos.property.PropertyFile;

import xaos.Towns;
import xaos.TownsProperties;
import xaos.main.Game;
import xaos.main.World;
import xaos.utils.Utils;
import xaos.utils.UtilsGL;

public final class MainFrame extends Frame implements WindowListener, ComponentListener {

    private static final long serialVersionUID = -2545208247526410538L;

    public final static int MIN_WIDTH = 1024;
    public static int MIN_HEIGHT = 600;

    private Canvas canvas;
    private boolean wantsToClose;
    private boolean wantsToResize;

    public MainFrame() {
        setWantsToClose(false);

        // Ancho y alto
        int iDesktopWidth = Display.getDesktopDisplayMode().getWidth();
        int iDesktopHeight = Display.getDesktopDisplayMode().getHeight();
        int iWidth = Towns.getPropertiesInt("WINDOW_WIDTH", (iDesktopWidth * 2) / 3); //$NON-NLS-1$
        int iHeight = Towns.getPropertiesInt("WINDOW_HEIGHT", (iDesktopHeight * 2) / 3); //$NON-NLS-1$

        if (iWidth > iDesktopWidth) {
            iWidth = iDesktopWidth;
        } else if (iWidth < MIN_WIDTH) {
            iWidth = MIN_WIDTH;
        }
        if (iHeight > iDesktopHeight) {
            iHeight = iDesktopHeight;
        } else if (iHeight < MIN_HEIGHT) {
            iHeight = MIN_HEIGHT;
        }

        // Centramos la ventana
        int iX = iDesktopWidth / 2 - iWidth / 2;
        int iY = iDesktopHeight / 2 - iHeight / 2;

        setTitle(TownsProperties.GAME_NAME);
        Image iIcon = new ImageIcon(Towns.getPropertiesString("GRAPHICS_FOLDER") + Towns.getPropertiesString(PropertyFile.PROPERTY_FILE_GRAPHICS, "ICON_FILE")).getImage(); //$NON-NLS-1$ //$NON-NLS-2$
        setIconImage(iIcon);
        setVisible(true); // Se pone visible antes de pillar los insets ya que sino devuelve 0

        // Tamaño de los bordes
        Insets insets = getInsets();
        int iBorderWidth = insets.left + insets.right;
        int iBorderHeight = insets.top + insets.bottom;

        setMinimumSize(new Dimension(MIN_WIDTH + iBorderWidth, MIN_HEIGHT + iBorderHeight));
        setMaximumSize(new Dimension(iDesktopWidth + iBorderWidth, iDesktopHeight + iBorderHeight));
        setSize(iWidth + iBorderWidth, iHeight + iBorderHeight);
        setBounds(iX - iBorderWidth / 2, iY - iBorderHeight / 2, iWidth + iBorderWidth, iHeight + iBorderHeight);

        // Canvas
        canvas = new Canvas();
        canvas.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        canvas.setMaximumSize(new Dimension(iDesktopWidth, iDesktopHeight));
        canvas.setBounds(iX, iY, iWidth, iHeight);
        canvas.setIgnoreRepaint(true);
        add(canvas);

        // Listeners
        addWindowListener(this);
        addComponentListener(this);
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    public void setWantsToClose(boolean wantsToClose) {
        this.wantsToClose = wantsToClose;
    }

    public boolean isWantsToClose() {
        return wantsToClose;
    }

    public void setWantsToResize(boolean wantsToResize) {
        this.wantsToResize = wantsToResize;
    }

    public boolean isWantsToResize() {
        return wantsToResize;
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
        setWantsToClose(true);
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
    }

    public void componentHidden(ComponentEvent e) {
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentResized(ComponentEvent e) {
        setWantsToResize(true);
    }

    public void resize() {
        int iWidth, iHeight;
        if (Display.isFullscreen()) {
            iWidth = UtilsGL.getWidth();
            iHeight = UtilsGL.getHeight();
        } else {
            iWidth = getCanvas().getWidth();
            iHeight = getCanvas().getHeight();
        }

        MainPanel.resize(iWidth, iHeight);
        if (Game.getWorld() == null) {
            Game.getPanelUI().resize(iWidth, iHeight, null, null, false);
        } else {
            Game.getPanelUI().resize(iWidth, iHeight, Game.getWorld().getCampaignID(), Game.getWorld().getMissionID(), true);
        }

        Game.getPanelCommand().resize(iWidth - World.MAP_WIDTH, World.MAP_HEIGHT, World.MAP_WIDTH, iHeight - World.MAP_HEIGHT);
        Game.getPanelMainMenu().resize(0, 0, iWidth, iHeight);

        UtilsGL.initGLModes();
        GL11.glViewport(0, 0, UtilsGL.getWidth(), UtilsGL.getHeight());
        Utils.saveOptions();

    }

    public void componentShown(ComponentEvent e) {
    }
}
