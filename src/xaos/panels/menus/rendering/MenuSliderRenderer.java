package xaos.panels.menus.rendering;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import xaos.main.Game;
import xaos.panels.CommandPanel;
import xaos.panels.menus.SmartMenu;
import xaos.utils.ColorGL;
import xaos.utils.UtilFont;
import xaos.utils.UtilsGL;


public final class MenuSliderRenderer {

    public static final int SLIDER_WIDTH = 150;
    public static final int SLIDER_HEIGHT = 24;
    public static final int SLIDER_RIGHT_PADDING = 14;
    public static final int SLIDER_GAP = 14;
    public static final int VALUE_TEXT_WIDTH = 34;
    public static final int VALUE_TEXT_GAP = 8;

    private MenuSliderRenderer() {
    }

    public static void render(
            SmartMenu item,
            String text,
            int x,
            int y,
            int width,
            int height,
            boolean hovered) {

        int value = getSliderValue(item);
        value = clamp(value, 0, 100);

        int rowX = x + 2;
        int rowY = y;
        int rowWidth = width - 4;
        int rowHeight = height - 2;

        if (hovered) {
            rowX += 1;
            rowY += 1;
        }

        int sliderX = rowX + rowWidth - SLIDER_WIDTH - SLIDER_RIGHT_PADDING;
        int sliderY = rowY + (rowHeight - SLIDER_HEIGHT) / 2;

        int textX = rowX + 12;
        int textMaxWidth = sliderX - textX - SLIDER_GAP;

        renderSliderRow(rowX, rowY, rowWidth, rowHeight, hovered);
        renderSliderText(text, textX, rowY, textMaxWidth, rowHeight, hovered);
        renderSliderControl(sliderX, sliderY, SLIDER_WIDTH, SLIDER_HEIGHT, value, hovered);
    }

private static int getSliderValue(SmartMenu item) {
    if (item == null || item.getCommand() == null) {
        return 0;
    }

    if (item.getCommand().equals(CommandPanel.COMMAND_MM_ADD_MUSIC_VOLUME)) {
        return clamp(Game.getVolumeMusic() * 10, 0, 100);
    }

    if (item.getCommand().equals(CommandPanel.COMMAND_MM_ADD_FX_VOLUME)) {
        return clamp(Game.getVolumeFX() * 10, 0, 100);
    }

    return 0;
}

    private static void renderSliderRow(
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

        // Fill
        if (hovered) {
            MenuPrimitiveRenderer.drawColoredRect(
                    x + 2,
                    y + 2,
                    width - 4,
                    height - 4,
                    0.54f,
                    0.45f,
                    0.30f);
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
                0.70f,
                0.60f,
                0.40f);

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

    private static void renderSliderText(
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

        ColorGL textColor = new ColorGL(new Color(235, 225, 190));
        ColorGL borderColor = new ColorGL(new Color(35, 24, 16));

        if (hovered) {
            textColor = new ColorGL(new Color(255, 240, 200));
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

        UtilsGL.glBegin(GL11.GL_QUADS);

        UtilsGL.drawString(text, x - 1, textY, borderColor);
        UtilsGL.drawString(text, x + 1, textY, borderColor);
        UtilsGL.drawString(text, x, textY - 1, borderColor);
        UtilsGL.drawString(text, x, textY + 1, borderColor);

        UtilsGL.drawString(text, x, textY, textColor);

        UtilsGL.glEnd();
    }

    private static void renderSliderControl(
            int x,
            int y,
            int width,
            int height,
            int value,
            boolean hovered) {

        int valueTextWidth = 34;
        int trackX = x;
        int trackY = y + 7;
       int trackWidth = width - VALUE_TEXT_WIDTH - VALUE_TEXT_GAP;
        int trackHeight = 10;

        int valueX = x + width - valueTextWidth;
        int valueY = y + (height - UtilFont.MAX_HEIGHT) / 2;

        // Track outer
        MenuPrimitiveRenderer.drawColoredRect(
                trackX,
                trackY,
                trackWidth,
                trackHeight,
                0.12f,
                0.08f,
                0.05f);

        // Track inset
        MenuPrimitiveRenderer.drawColoredRect(
                trackX + 2,
                trackY + 2,
                trackWidth - 4,
                trackHeight - 4,
                0.26f,
                0.19f,
                0.13f);

        int fillWidth = (int) ((trackWidth - 4) * (value / 100f));

        if (fillWidth > 0) {
            MenuPrimitiveRenderer.drawColoredRect(
                    trackX + 2,
                    trackY + 2,
                    fillWidth,
                    trackHeight - 4,
                    0.58f,
                    0.48f,
                    0.28f);
        }

        int usableTrackWidth = trackWidth - 8;
        int knobSize = 18;
        int knobX = trackX + 4 + (int) (usableTrackWidth * (value / 100f)) - knobSize / 2;
        int knobY = y + (height - knobSize) / 2;

        if (knobX < trackX) {
            knobX = trackX;
        }

        if (knobX + knobSize > trackX + trackWidth) {
            knobX = trackX + trackWidth - knobSize;
        }

        // Knob shadow/frame
        MenuPrimitiveRenderer.drawColoredRect(
                knobX,
                knobY,
                knobSize,
                knobSize,
                0.13f,
                0.08f,
                0.05f);

        // Knob fill
        MenuPrimitiveRenderer.drawColoredRect(
                knobX + 2,
                knobY + 2,
                knobSize - 4,
                knobSize - 4,
                hovered ? 0.88f : 0.76f,
                hovered ? 0.76f : 0.64f,
                hovered ? 0.50f : 0.42f);

        // Knob highlight
        MenuPrimitiveRenderer.drawColoredRect(
                knobX + 3,
                knobY + 3,
                knobSize - 6,
                2,
                0.95f,
                0.84f,
                0.58f);

        renderValueText(value, valueX, valueY);
    }

    private static void renderValueText(int value, int x, int y) {
        String text = Integer.toString(value) + "%";

        ColorGL textColor = new ColorGL(new Color(230, 215, 175));
        ColorGL borderColor = new ColorGL(new Color(30, 20, 12));

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

    private static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }

        if (value > max) {
            return max;
        }

        return value;
    }
}