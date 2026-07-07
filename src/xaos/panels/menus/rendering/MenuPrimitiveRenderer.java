package xaos.panels.menus.rendering;

import org.lwjgl.opengl.GL11;

public final class MenuPrimitiveRenderer {

    private MenuPrimitiveRenderer() {
    }

    public static void drawColoredRect(int x, int y, int width, int height, float r, float g, float b) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(r, g, b, 1f);

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2i(x, y);
        GL11.glVertex2i(x + width, y);
        GL11.glVertex2i(x + width, y + height);
        GL11.glVertex2i(x, y + height);
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
}
