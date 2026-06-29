package xaos.panels.menus.rendering;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import xaos.main.Game;
import xaos.panels.UI.UIPanelState;
import xaos.panels.menus.SmartMenu;
import xaos.utils.ColorGL;
import xaos.utils.UtilFont;
import xaos.utils.UtilsGL;

public final class MenuPanelRenderer {

    private MenuPanelRenderer() {
    }

    public static final int PANEL_PADDING_X = 10;
    public static final int PANEL_PADDING_Y = 12;
    public static final int TITLE_HEIGHT = UtilFont.MAX_HEIGHT + 10;

    public static void renderPanel(
            SmartMenu menu,
            int x,
            int y,
            int width,
            int height) {

        if (menu == null) {
            return;
        }

        // Outer shadow
        MenuPrimitiveRenderer.drawColoredRect(
                x + 3,
                y + 3,
                width,
                height,
                0.05f,
                0.035f,
                0.025f);

        // Outer frame
        MenuPrimitiveRenderer.drawColoredRect(
                x,
                y,
                width,
                height,
                0.16f,
                0.10f,
                0.06f);

        // Inner frame
        MenuPrimitiveRenderer.drawColoredRect(
                x + 2,
                y + 2,
                width - 4,
                height - 4,
                0.38f,
                0.27f,
                0.16f);

        // Main panel fill
        MenuPrimitiveRenderer.drawColoredRect(
                x + 4,
                y + 4,
                width - 8,
                height - 8,
                0.22f,
                0.16f,
                0.10f);

        // Subtle textured overlay using the existing tooltip background
        GL11.glColor4f(1f, 1f, 1f, 0.25f);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, UIPanelState.tileTooltipBackground.getTextureID());
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE);

        UtilsGL.glBegin(GL11.GL_QUADS);
        UtilsGL.drawTexture(
                x + 4,
                y + 4,
                x + width - 4,
                y + height - 4,
                UIPanelState.tileTooltipBackground.getTileSetTexX0(),
                UIPanelState.tileTooltipBackground.getTileSetTexY0(),
                UIPanelState.tileTooltipBackground.getTileSetTexX1(),
                UIPanelState.tileTooltipBackground.getTileSetTexY1());
        UtilsGL.glEnd();

        renderTitle(menu, x, y, width);
    }

    private static void renderTitle(SmartMenu menu, int x, int y, int width) {
        String title = menu.getName();

        if (title == null || title.trim().length() == 0) {
            return;
        }

        title = MenuTextSanitiser.sanitise(title);

        if (title.length() == 0) {
            return;
        }

        int textWidth = UtilFont.getWidth(title);
        int titlePaddingX = 14;
        int titleBoxWidth = textWidth + titlePaddingX * 2;
        int titleBoxHeight = UtilFont.MAX_HEIGHT + 8;

        int titleBoxX = x + (width - titleBoxWidth) / 2;
        int titleBoxY = y - titleBoxHeight / 2;

        // Title plate
        MenuPrimitiveRenderer.drawColoredRect(
                titleBoxX,
                titleBoxY,
                titleBoxWidth,
                titleBoxHeight,
                0.16f,
                0.10f,
                0.06f);

        MenuPrimitiveRenderer.drawColoredRect(
                titleBoxX + 2,
                titleBoxY + 2,
                titleBoxWidth - 4,
                titleBoxHeight - 4,
                0.50f,
                0.36f,
                0.20f);

        MenuPrimitiveRenderer.drawColoredRect(
                titleBoxX + 4,
                titleBoxY + 4,
                titleBoxWidth - 8,
                titleBoxHeight - 8,
                0.30f,
                0.21f,
                0.13f);

        int textX = titleBoxX + titlePaddingX;
        int textY = titleBoxY + 4;

        ColorGL textColor = new ColorGL(new Color(245, 232, 190));
        ColorGL borderColor = new ColorGL(new Color(30, 20, 12));

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

        UtilsGL.glBegin(GL11.GL_QUADS);

        UtilsGL.drawString(title, textX - 1, textY, borderColor);
        UtilsGL.drawString(title, textX + 1, textY, borderColor);
        UtilsGL.drawString(title, textX, textY - 1, borderColor);
        UtilsGL.drawString(title, textX, textY + 1, borderColor);
        UtilsGL.drawString(title, textX, textY, textColor);

        UtilsGL.glEnd();
    }

    public static void renderPanelTitleOnly(
            SmartMenu menu,
            int x,
            int y,
            int width) {

        renderTitle(menu, x, y, width);
    }
}