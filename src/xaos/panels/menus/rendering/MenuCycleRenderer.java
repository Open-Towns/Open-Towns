package xaos.panels.menus.rendering;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import xaos.main.Game;
import xaos.panels.CommandPanel;
import xaos.panels.menus.SmartMenu;
import xaos.utils.ColorGL;
import xaos.utils.UtilFont;
import xaos.utils.Utils;
import xaos.utils.UtilsGL;

public final class MenuCycleRenderer {

    public static final int CYCLE_WIDTH = 150;
    public static final int CYCLE_HEIGHT = 24;
    public static final int CYCLE_RIGHT_PADDING = 14;
    public static final int CYCLE_GAP = 14;

    private MenuCycleRenderer() {
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

        String valueText = getValueText(item);

        int cycleX = rowX + rowWidth - CYCLE_WIDTH - CYCLE_RIGHT_PADDING;
        int cycleY = rowY + (rowHeight - CYCLE_HEIGHT) / 2;

        int textX = rowX + 12;
        int textMaxWidth = cycleX - textX - CYCLE_GAP;

        renderRow(rowX, rowY, rowWidth, rowHeight, hovered);
        renderLabel(text, textX, rowY, textMaxWidth, rowHeight, hovered);
        renderCycleBox(valueText, cycleX, cycleY, CYCLE_WIDTH, CYCLE_HEIGHT, hovered);
    }

    private static void renderRow(
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
        MenuPrimitiveRenderer.drawColoredRect(
                x + 2,
                y + 2,
                width - 4,
                height - 4,
                hovered ? 0.54f : 0.42f,
                hovered ? 0.45f : 0.33f,
                hovered ? 0.30f : 0.22f);

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

    private static void renderLabel(
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

    private static void renderCycleBox(
            String valueText,
            int x,
            int y,
            int width,
            int height,
            boolean hovered) {

        if (valueText == null || valueText.trim().length() == 0) {
            valueText = "-";
        }

        valueText = MenuTextSanitiser.sanitise(valueText);

        // Outer frame
        MenuPrimitiveRenderer.drawColoredRect(
                x,
                y,
                width,
                height,
                0.12f,
                0.08f,
                0.05f);

        // Inner frame
        MenuPrimitiveRenderer.drawColoredRect(
                x + 2,
                y + 2,
                width - 4,
                height - 4,
                0.34f,
                0.24f,
                0.15f);

        // Face
        MenuPrimitiveRenderer.drawColoredRect(
                x + 4,
                y + 4,
                width - 8,
                height - 8,
                hovered ? 0.72f : 0.60f,
                hovered ? 0.62f : 0.50f,
                hovered ? 0.42f : 0.32f);

        // Top highlight
        MenuPrimitiveRenderer.drawColoredRect(
                x + 5,
                y + 5,
                width - 10,
                2,
                0.88f,
                0.76f,
                0.50f);

        // Bottom shadow
        MenuPrimitiveRenderer.drawColoredRect(
                x + 5,
                y + height - 7,
                width - 10,
                2,
                0.30f,
                0.22f,
                0.14f);

        ColorGL textColor = new ColorGL(new Color(35, 24, 16));
        ColorGL borderColor = new ColorGL(new Color(235, 220, 175));

        String leftArrow = "<";
        String rightArrow = ">";

        int textY = y + (height - UtilFont.MAX_HEIGHT) / 2;

        String fittedValue = fitTextToWidth(valueText, width - 50);
        int valueX = x + (width - UtilFont.getWidth(fittedValue)) / 2;

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

        UtilsGL.glBegin(GL11.GL_QUADS);

        drawTextWithBorder(leftArrow, x + 10, textY, textColor, borderColor);
        drawTextWithBorder(fittedValue, valueX, textY, textColor, borderColor);
        drawTextWithBorder(rightArrow, x + width - 18, textY, textColor, borderColor);

        UtilsGL.glEnd();
    }

    private static String getValueText(SmartMenu item) {
        if (item == null || item.getCommand() == null) {
            return "-";
        }

        String command = item.getCommand();

        if (command.equals(CommandPanel.COMMAND_MM_SWITCH_SIEGES)) {
            return Utils.getDynamicString("__SIEGES__");
        }

        if (command.equals(CommandPanel.COMMAND_MM_SWITCH_AUTOSAVE_DAYS)) {

            int daysCount = Game.getAutosaveDays();

            return daysCount == 0 ? "Disabled" : daysCount + " Days";
        }

        return "-";
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
        if (text == null) {
            return "";
        }

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