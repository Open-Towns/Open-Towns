package xaos.panels.menus.rendering;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import xaos.main.Game;
import xaos.panels.CommandPanel;
import xaos.panels.menus.SmartMenu;
import xaos.utils.ColorGL;
import xaos.utils.UtilFont;
import xaos.utils.UtilsGL;

public final class MenuItemRenderer {

    private MenuItemRenderer() {
    }

    public static void render(
            SmartMenu item,
            String text,
            int x,
            int y,
            int width,
            int height,
            boolean hovered) {

        int rowX = x + 2;
        int rowY = y;
        int rowWidth = width - 4;
        int rowHeight = height - 2;

        if (hovered) {
            rowX += 1;
            rowY += 1;
        }

        renderItemBackground(rowX, rowY, rowWidth, rowHeight, hovered);
        renderItemText(item, text, rowX, rowY, rowWidth, rowHeight, hovered);
    }

    private static void renderItemBackground(
            int x,
            int y,
            int width,
            int height,
            boolean hovered) {

        // Outer frame
        MenuPrimitiveRenderer.drawColoredRect(
                x,
                y,
                width,
                height,
                0.18f,
                0.12f,
                0.08f);

        // Inner frame
        MenuPrimitiveRenderer.drawColoredRect(
                x + 1,
                y + 1,
                width - 2,
                height - 2,
                0.35f,
                0.26f,
                0.18f);

        // Main fill
        if (hovered) {
            MenuPrimitiveRenderer.drawColoredRect(
                    x + 2,
                    y + 2,
                    width - 4,
                    height - 4,
                    0.58f,
                    0.49f,
                    0.32f);
        } else {
            MenuPrimitiveRenderer.drawColoredRect(
                    x + 2,
                    y + 2,
                    width - 4,
                    height - 4,
                    0.42f,
                    0.33f,
                    0.22f);
        }

        // Top highlight
        MenuPrimitiveRenderer.drawColoredRect(
                x + 2,
                y + 2,
                width - 4,
                2,
                hovered ? 0.82f : 0.70f,
                hovered ? 0.70f : 0.60f,
                hovered ? 0.46f : 0.40f);

        // Bottom shadow
        MenuPrimitiveRenderer.drawColoredRect(
                x + 2,
                y + height - 4,
                width - 4,
                2,
                0.22f,
                0.15f,
                0.09f);
    }

    private static void renderItemText(
            SmartMenu item,
            String text,
            int x,
            int y,
            int width,
            int height,
            boolean hovered) {

        text = MenuTextSanitiser.sanitise(text);

        if (text.length() == 0) {
            return;
        }

        int textX = x + 12;
        int textY = y + (height - UtilFont.MAX_HEIGHT) / 2;

        boolean opensSubMenu = item.getItems() != null && item.getItems().size() > 0;
        boolean isBack = item.getCommand() != null
                && item.getCommand().equals(CommandPanel.COMMAND_BACK);

        int maxTextWidth = width - 24;

        if (opensSubMenu || isBack) {
            maxTextWidth -= 20;
        }

        text = fitTextToWidth(text, maxTextWidth);

        ColorGL textColor = new ColorGL(new Color(235, 225, 190));
        ColorGL borderColor = new ColorGL(new Color(35, 24, 16));

        if (hovered) {
            textColor = new ColorGL(new Color(255, 240, 200));
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

        UtilsGL.glBegin(GL11.GL_QUADS);

        drawTextWithBorder(text, textX, textY, textColor, borderColor);

        if (opensSubMenu) {
            drawTextWithBorder(">", x + width - 18, textY, textColor, borderColor);
        } else if (isBack) {
            drawTextWithBorder("<", x + 6, textY, textColor, borderColor);
        }

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
