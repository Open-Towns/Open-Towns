package xaos.panels.menus.rendering;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import xaos.main.Game;
import xaos.panels.UI.UIPanelState;
import xaos.panels.menus.SmartMenu;
import xaos.utils.ColorGL;
import xaos.utils.UtilFont;
import xaos.utils.UtilsGL;

public final class MenuButtonRenderer {

    private MenuButtonRenderer() {
    }

    public static void render(
            SmartMenu item,
            String text,
            int x,
            int y,
            int width,
            int height,
            boolean hovered) {

        if (text == null) {
            text = "";
        }

        int buttonX = x + 2;
        int buttonY = y;
        int buttonWidth = width - 4;
        int buttonHeight = height - 2;

        if (hovered) {
            buttonX += 1;
            buttonY += 1;
        }

        MenuPrimitiveRenderer.drawColoredRect(buttonX, buttonY, buttonWidth, buttonHeight, 0.18f, 0.12f, 0.08f);
        MenuPrimitiveRenderer.drawColoredRect(buttonX + 1, buttonY + 1, buttonWidth - 2, buttonHeight - 2, 0.35f, 0.26f, 0.18f);

        if (hovered) {
            MenuPrimitiveRenderer.drawColoredRect(buttonX + 2, buttonY + 2, buttonWidth - 4, buttonHeight - 4, 0.72f, 0.66f, 0.50f);
        } else {
            MenuPrimitiveRenderer.drawColoredRect(buttonX + 2, buttonY + 2, buttonWidth - 4, buttonHeight - 4, 0.62f, 0.56f, 0.42f);
        }

        MenuPrimitiveRenderer.drawColoredRect(buttonX + 2, buttonY + 2, buttonWidth - 4, 2, 0.82f, 0.76f, 0.58f);
        MenuPrimitiveRenderer.drawColoredRect(buttonX + 2, buttonY + buttonHeight - 4, buttonWidth - 4, 2, 0.28f, 0.22f, 0.16f);

        GL11.glColor4f(1f, 1f, 1f, 0.35f);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, UIPanelState.tileTooltipBackground.getTextureID());
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE);

        UtilsGL.glBegin(GL11.GL_QUADS);
        UtilsGL.drawTexture(
                buttonX + 2,
                buttonY + 2,
                buttonX + buttonWidth - 2,
                buttonY + buttonHeight - 2,
                UIPanelState.tileTooltipBackground.getTileSetTexX0(),
                UIPanelState.tileTooltipBackground.getTileSetTexY0(),
                UIPanelState.tileTooltipBackground.getTileSetTexX1(),
                UIPanelState.tileTooltipBackground.getTileSetTexY1());
        UtilsGL.glEnd();

        GL11.glColor4f(1f, 1f, 1f, 1f);

        renderButtonText(text, buttonX, buttonY, buttonWidth, buttonHeight, hovered);
    }

    private static void renderButtonText(String text, int x, int y, int width, int height, boolean hovered) {
        text = MenuTextSanitiser.sanitise(text);

        if (text.length() == 0) {
            return;
        }

        int textWidth = UtilFont.getWidth(text);
        int textX = x + (width - textWidth) / 2;
        int textY = y + (height - UtilFont.MAX_HEIGHT) / 2;

        ColorGL textColor = new ColorGL(new Color(235, 225, 190));
        ColorGL borderColor = new ColorGL(new Color(45, 30, 20));

        if (hovered) {
            textColor = new ColorGL(new Color(255, 240, 200));
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

        UtilsGL.glBegin(GL11.GL_QUADS);

        UtilsGL.drawString(text, textX - 1, textY, borderColor);
        UtilsGL.drawString(text, textX + 1, textY, borderColor);
        UtilsGL.drawString(text, textX, textY - 1, borderColor);
        UtilsGL.drawString(text, textX, textY + 1, borderColor);

        UtilsGL.drawString(text, textX, textY, textColor);

        UtilsGL.glEnd();
    }
}