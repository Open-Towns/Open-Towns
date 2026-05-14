package xaos.panels.info;

import org.lwjgl.opengl.GL11;

import xaos.main.Game;
import xaos.panels.MainPanel;
import xaos.panels.UI.UIPanel;
import xaos.utils.UtilFont;
import xaos.utils.UtilsGL;

/**
 * Clase base para los paneles de información
 */
public class InfoPanel {
	public InfoPanel() {
		
	}
/*
    private int x;
    private int y;
    private int width;
    private int height;

    public InfoPanel() {
        this(
                UtilFont.MAX_HEIGHT * 3,
                UtilFont.MAX_HEIGHT * 3,
                MainPanel.renderWidth - UtilFont.MAX_HEIGHT * 6,
                MainPanel.renderHeight - UtilFont.MAX_HEIGHT * 6);
    }

    public InfoPanel(int x, int y, int width, int height) {
        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);
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

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void render() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, UIPanel.BLACK_TILE.getTextureID());
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
        UtilsGL.glBegin(GL11.GL_QUADS);

        // Rectángulo negro
        UtilsGL.drawTexture(getX(), getY(), getX() + getWidth(), getY() + getHeight(), UIPanel.BLACK_TILE.getTileSetTexX0(), UIPanel.BLACK_TILE.getTileSetTexY0(), UIPanel.BLACK_TILE.getTileSetTexX1(), UIPanel.BLACK_TILE.getTileSetTexY1());
        UtilsGL.glEnd();
    }

    public void mousePressed(int x, int y) {
        //Game.closeInfoPanel();
    }
*/
}
