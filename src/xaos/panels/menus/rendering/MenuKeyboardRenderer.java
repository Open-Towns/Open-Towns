package xaos.panels.menus.rendering;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import xaos.main.Game;
import xaos.panels.menus.SmartMenu;
import xaos.utils.ColorGL;
import xaos.utils.UtilFont;
import xaos.utils.UtilsGL;

public final class MenuKeyboardRenderer {

    private static final int KEYCAP_RIGHT_PADDING = 14;
    private static final int KEYCAP_MIN_WIDTH = 52;
    private static final int KEYCAP_HEIGHT = 24;
    private static final int KEYCAP_GAP = 14;

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

        int rowInsetX = 8;

        int rowX = x + rowInsetX;
        int rowY = y;
        int rowWidth = width - rowInsetX * 2;
        int rowHeight = height - 2;

        if (hovered) {
            rowX += 1;
            rowY += 1;
        }

        String labelText = getLabelText(text);
        String keyText = getKeyText(text);

        int keycapWidth = Math.max(KEYCAP_MIN_WIDTH, UtilFont.getWidth(keyText) + 20);
        int keycapX = rowX + rowWidth - keycapWidth - KEYCAP_RIGHT_PADDING;
        int keycapY = rowY + (rowHeight - KEYCAP_HEIGHT) / 2;

        int textX = rowX + 12;
        int textMaxWidth = keycapX - textX - KEYCAP_GAP;

        renderRow(rowX, rowY, rowWidth, rowHeight, hovered);
        renderText(labelText, textX, rowY, textMaxWidth, rowHeight, hovered);
        renderKeycap(keyText, keycapX, keycapY, keycapWidth, KEYCAP_HEIGHT, hovered);
    }

    private static String getLabelText(String text) {
        text = MenuTextSanitiser.sanitise(text);

        int openBracket = text.lastIndexOf("(");
        int closeBracket = text.lastIndexOf(")");

        if (openBracket >= 0 && closeBracket > openBracket) {
            return text.substring(0, openBracket).trim();
        }

        return text.trim();
    }

    private static String getKeyText(String text) {
        text = MenuTextSanitiser.sanitise(text);

        int openBracket = text.lastIndexOf("(");
        int closeBracket = text.lastIndexOf(")");

        if (openBracket >= 0 && closeBracket > openBracket) {
            String keyText = text.substring(openBracket + 1, closeBracket).trim();

            if (keyText.length() > 0) {
                return keyText;
            }
        }

        return "-";
    }

    private static void renderRow(
            int x,
            int y,
            int width,
            int height,
            boolean hovered) {

        MenuPrimitiveRenderer.drawColoredRect(
                x,
                y,
                width,
                height,
                0.18f,
                0.12f,
                0.08f);

        MenuPrimitiveRenderer.drawColoredRect(
                x + 1,
                y + 1,
                width - 2,
                height - 2,
                0.35f,
                0.26f,
                0.18f);

        MenuPrimitiveRenderer.drawColoredRect(
                x + 2,
                y + 2,
                width - 4,
                height - 4,
                hovered ? 0.54f : 0.42f,
                hovered ? 0.45f : 0.33f,
                hovered ? 0.30f : 0.22f);

        MenuPrimitiveRenderer.drawColoredRect(
                x + 2,
                y + 2,
                width - 4,
                2,
                hovered ? 0.82f : 0.70f,
                hovered ? 0.70f : 0.60f,
                hovered ? 0.46f : 0.40f);

        MenuPrimitiveRenderer.drawColoredRect(
                x + 2,
                y + height - 4,
                width - 4,
                2,
                0.22f,
                0.15f,
                0.09f);
    }

    private static void renderText(
            String text,
            int x,
            int y,
            int maxTextWidth,
            int height,
            boolean hovered) {

        text = MenuTextSanitiser.sanitise(text);

        if (text.length() == 0) {
            return;
        }

        text = fitTextToWidth(text, maxTextWidth);

        int textY = y + (height - UtilFont.MAX_HEIGHT) / 2;

        ColorGL textColor = hovered
                ? new ColorGL(new Color(255, 240, 200))
                : new ColorGL(new Color(235, 225, 190));

        ColorGL borderColor = new ColorGL(new Color(35, 24, 16));

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

        UtilsGL.glBegin(GL11.GL_QUADS);
        drawTextWithBorder(text, x, textY, textColor, borderColor);
        UtilsGL.glEnd();
    }

    private static void renderKeycap(
            String text,
            int x,
            int y,
            int width,
            int height,
            boolean hovered) {

        if (text == null || text.length() == 0) {
            text = "-";
        }

        // Keycap shadow/frame
        MenuPrimitiveRenderer.drawColoredRect(
                x,
                y,
                width,
                height,
                0.10f,
                0.07f,
                0.04f);

        // Keycap bevel
        MenuPrimitiveRenderer.drawColoredRect(
                x + 2,
                y + 2,
                width - 4,
                height - 4,
                0.32f,
                0.24f,
                0.16f);

        // Keycap face
        MenuPrimitiveRenderer.drawColoredRect(
                x + 4,
                y + 4,
                width - 8,
                height - 8,
                hovered ? 0.76f : 0.64f,
                hovered ? 0.66f : 0.55f,
                hovered ? 0.44f : 0.36f);

        // Top highlight
        MenuPrimitiveRenderer.drawColoredRect(
                x + 5,
                y + 5,
                width - 10,
                2,
                0.90f,
                0.78f,
                0.52f);

        // Bottom shadow
        MenuPrimitiveRenderer.drawColoredRect(
                x + 5,
                y + height - 7,
                width - 10,
                2,
                0.30f,
                0.22f,
                0.14f);

        int textX = x + (width - UtilFont.getWidth(text)) / 2;
        int textY = y + (height - UtilFont.MAX_HEIGHT) / 2;

        ColorGL textColor = new ColorGL(new Color(35, 24, 16));
        ColorGL borderColor = new ColorGL(new Color(235, 220, 175));

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

        UtilsGL.glBegin(GL11.GL_QUADS);
        drawTextWithBorder(text, textX, textY, textColor, borderColor);
        UtilsGL.glEnd();
    }

    private static void drawTextWithBorder(
            String text,
            int x,
            int y,
            ColorGL textColor,
            ColorGL borderColor) {

        UtilsGL.drawString(text, x - 1, y, borderColor);
        UtilsGL.drawString(text, x + 1, y, borderColor);
        UtilsGL.drawString(text, x, y - 1, borderColor);
        UtilsGL.drawString(text, x, y + 1, borderColor);
        UtilsGL.drawString(text, x, y, textColor);
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