package xaos.panels.menus.rendering;

import org.lwjgl.opengl.GL11;

import xaos.main.Game;
import xaos.panels.UI.UIPanelState;
import xaos.panels.menus.SmartMenu;
import xaos.utils.UtilsGL;

public final class MenuKeyboardRenderer {

    private MenuKeyboardRenderer() {
    }

    public static void render(
            SmartMenu item,
            String text,
            int x,
            int y,
            int width,
            int height,
            boolean hovered) {

        int paddingX = 8;

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, UIPanelState.tileTooltipBackground.getTextureID());
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE);

        if (hovered) {
            GL11.glColor4f(1f, 0.8f, 0.8f, 1f);
        } else {
            GL11.glColor4f(1f, 1f, 1f, 1f);
        }

        UtilsGL.glBegin(GL11.GL_QUADS);
        UtilsGL.drawTexture(
                x,
                y,
                x + width,
                y + height,
                UIPanelState.tileTooltipBackground.getTileSetTexX0(),
                UIPanelState.tileTooltipBackground.getTileSetTexY0(),
                UIPanelState.tileTooltipBackground.getTileSetTexX1(),
                UIPanelState.tileTooltipBackground.getTileSetTexY1());
        UtilsGL.glEnd();

        String displayText = MenuTextSanitiser.sanitise(text);

        if (displayText.length() == 0) {
            displayText = "Keyboard";
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

        UtilsGL.glBegin(GL11.GL_QUADS);
        UtilsGL.drawString(displayText, x + paddingX, y + 1, SmartMenu.COLORGL_SUBMENU);
        UtilsGL.glEnd();
    }
}
