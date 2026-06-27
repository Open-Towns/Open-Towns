package xaos.panels.menus.rendering;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import xaos.main.Game;
import xaos.panels.menus.SmartMenu;
import xaos.utils.ColorGL;
import xaos.utils.UtilsGL;

public final class MenuTextRenderer {

    private MenuTextRenderer() {
    }

    public static void renderText(SmartMenu item, String text, int x, int y) {
        if (text == null) {
            return;
        }

        text = MenuTextSanitiser.sanitise(text);

        if (text.length() == 0) {
            return;
        }

        ColorGL color = item.getColor();
        if (color == null) {
            color = new ColorGL(Color.WHITE);
        }

        ColorGL borderColor = item.getBorderColor();
        if (borderColor == null) {
            borderColor = new ColorGL(Color.BLACK);
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

        UtilsGL.glBegin(GL11.GL_QUADS);

        if (item.getBorderColor() != null) {
            UtilsGL.drawString(text, x, y - 1, borderColor);
            UtilsGL.drawString(text, x + 1, y - 1, borderColor);
            UtilsGL.drawString(text, x + 2, y - 1, borderColor);
            UtilsGL.drawString(text, x, y, borderColor);
            UtilsGL.drawString(text, x + 2, y, borderColor);
            UtilsGL.drawString(text, x, y + 1, borderColor);
            UtilsGL.drawString(text, x + 1, y + 1, borderColor);
            UtilsGL.drawString(text, x + 2, y + 1, borderColor);

            UtilsGL.drawString(text, x + 1, y, color);
        } else {
            UtilsGL.drawString(text, x, y, color);
        }

        UtilsGL.glEnd();
    }
}
