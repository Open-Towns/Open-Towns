package xaos.panels.menus.rendering;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import xaos.main.Game;
import xaos.panels.menus.SmartMenu;
import xaos.utils.ColorGL;
import xaos.utils.UtilFont;
import xaos.utils.UtilsGL;

public final class MenuTextRenderer {

    private MenuTextRenderer() {
    }

    public static void render(
            SmartMenu item,
            String text,
            int x,
            int y,
            int width,
            int height) {

        text = MenuTextSanitiser.sanitise(text);

        if (text.length() == 0) {
            renderSeparator(x, y, width, height);
            return;
        }

        if (item.getType() == SmartMenu.TYPE_HEADING) {
            renderHeading(text, x, y, width, height);
        } else {
            renderTextRow(text, x, y, width, height);
        }
    }

   private static void renderTextRow(
        String text,
        int x,
        int y,
        int width,
        int height) {

    int textInsetX = 18;

    int textX = x + textInsetX;
    int textY = y + (height - UtilFont.MAX_HEIGHT) / 2;

    int maxTextWidth = width - textInsetX * 2;
    text = fitTextToWidth(text, maxTextWidth);

    ColorGL textColor = new ColorGL(new Color(215, 200, 165));
    ColorGL borderColor = new ColorGL(new Color(30, 20, 12));

    renderTextWithBorder(text, textX, textY, textColor, borderColor);
}

    private static void renderHeading(
            String text,
            int x,
            int y,
            int width,
            int height) {

        int rowInsetX = 8;

        int rowX = x + rowInsetX;
        int rowY = y + 2;
        int rowWidth = width - rowInsetX * 2;
        int rowHeight = height - 4;

        if (rowWidth <= 0 || rowHeight <= 0) {
            return;
        }

        // Heading plate
        MenuPrimitiveRenderer.drawColoredRect(
                rowX,
                rowY,
                rowWidth,
                rowHeight,
                0.16f,
                0.10f,
                0.06f);

        MenuPrimitiveRenderer.drawColoredRect(
                rowX + 2,
                rowY + 2,
                rowWidth - 4,
                rowHeight - 4,
                0.48f,
                0.34f,
                0.18f);

        MenuPrimitiveRenderer.drawColoredRect(
                rowX + 4,
                rowY + 4,
                rowWidth - 8,
                rowHeight - 8,
                0.30f,
                0.21f,
                0.13f);

        // Decorative horizontal lines
        int centerY = rowY + rowHeight / 2;
        MenuPrimitiveRenderer.drawColoredRect(
                rowX + 8,
                centerY,
                32,
                1,
                0.75f,
                0.62f,
                0.36f);

        MenuPrimitiveRenderer.drawColoredRect(
                rowX + rowWidth - 40,
                centerY,
                32,
                1,
                0.75f,
                0.62f,
                0.36f);

        text = fitTextToWidth(text, rowWidth - 90);

        int textWidth = UtilFont.getWidth(text);
        int textX = rowX + (rowWidth - textWidth) / 2;
        int textY = rowY + (rowHeight - UtilFont.MAX_HEIGHT) / 2;

        ColorGL textColor = new ColorGL(new Color(245, 225, 175));
        ColorGL borderColor = new ColorGL(new Color(30, 20, 12));

        renderTextWithBorder(text, textX, textY, textColor, borderColor);
    }

    private static void renderSeparator(
            int x,
            int y,
            int width,
            int height) {

        int lineInsetX = 18;
        int lineY = y + height / 2;

        MenuPrimitiveRenderer.drawColoredRect(
                x + lineInsetX,
                lineY,
                width - lineInsetX * 2,
                1,
                0.12f,
                0.08f,
                0.05f);

        MenuPrimitiveRenderer.drawColoredRect(
                x + lineInsetX,
                lineY + 1,
                width - lineInsetX * 2,
                1,
                0.58f,
                0.46f,
                0.28f);
    }

    private static void renderTextWithBorder(
            String text,
            int x,
            int y,
            ColorGL textColor,
            ColorGL borderColor) {

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

        UtilsGL.glBegin(GL11.GL_QUADS);

        UtilsGL.drawString(text, x - 1, y, borderColor);
        UtilsGL.drawString(text, x + 1, y, borderColor);
        UtilsGL.drawString(text, x, y - 1, borderColor);
        UtilsGL.drawString(text, x, y + 1, borderColor);

        UtilsGL.drawString(text, x, y, textColor);

        UtilsGL.glEnd();
    }

    private static String fitTextToWidth(String text, int maxWidth) {
        if (maxWidth <= 0) {
            return "";
        }

        if (UtilFont.getWidth(text) <= maxWidth) {
            return text;
        }

        String suffix = "...";
        int suffixWidth = UtilFont.getWidth(suffix);

        while (text.length() > 0 && UtilFont.getWidth(text) + suffixWidth > maxWidth) {
            text = text.substring(0, text.length() - 1);
        }

        return text + suffix;
    }
}