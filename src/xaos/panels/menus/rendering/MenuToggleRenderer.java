package xaos.panels.menus.rendering;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import xaos.main.Game;
import xaos.panels.menus.MenuStateResolver;
import xaos.panels.menus.SmartMenu;
import xaos.utils.ColorGL;
import xaos.utils.UtilFont;
import xaos.utils.UtilsGL;

public final class MenuToggleRenderer {

    private MenuToggleRenderer() {
    }

    public static void render(
            SmartMenu item,
            String text,
            int x,
            int y,
            int width,
            int height,
            boolean hovered) {

        boolean enabled = MenuStateResolver.getBoolean(item.getEffectiveStateKey());

        int rowX = x + 2;
        int rowY = y;
        int rowWidth = width - 4;
        int rowHeight = height - 2;

        if (hovered) {
            rowX += 1;
            rowY += 1;
        }

        renderToggleRow(rowX, rowY, rowWidth, rowHeight, hovered);
        renderToggleText(text, rowX + 12, rowY, rowWidth, rowHeight, hovered);
        renderToggleSwitch(rowX, rowY, rowWidth, rowHeight, enabled, hovered);
    }

    private static void renderToggleRow(
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

        MenuPrimitiveRenderer.drawColoredRect(
                x + 2,
                y + 2,
                width - 4,
                2,
                0.70f,
                0.60f,
                0.40f);

        MenuPrimitiveRenderer.drawColoredRect(
                x + 2,
                y + height - 4,
                width - 4,
                2,
                0.22f,
                0.15f,
                0.09f);
    }

    private static void renderToggleText(
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

    private static void renderToggleSwitch(
            int rowX,
            int rowY,
            int rowWidth,
            int rowHeight,
            boolean enabled,
            boolean hovered) {

        int switchWidth = 58;
        int switchHeight = 24;

        int switchX = rowX + rowWidth - switchWidth - 12;
        int switchY = rowY + (rowHeight - switchHeight) / 2;

        // Outer switch frame
        MenuPrimitiveRenderer.drawColoredRect(
                switchX,
                switchY,
                switchWidth,
                switchHeight,
                0.12f,
                0.08f,
                0.05f);

        // Inner frame
        MenuPrimitiveRenderer.drawColoredRect(
                switchX + 2,
                switchY + 2,
                switchWidth - 4,
                switchHeight - 4,
                0.34f,
                0.24f,
                0.15f);

        // Inset fill
        if (enabled) {
            MenuPrimitiveRenderer.drawColoredRect(
                    switchX + 4,
                    switchY + 4,
                    switchWidth - 8,
                    switchHeight - 8,
                    0.30f,
                    0.48f,
                    0.24f);
        } else {
            MenuPrimitiveRenderer.drawColoredRect(
                    switchX + 4,
                    switchY + 4,
                    switchWidth - 8,
                    switchHeight - 8,
                    0.30f,
                    0.24f,
                    0.20f);
        }

        // Small label
        String label = enabled ? "ON" : "OFF";
        ColorGL labelColor = enabled
                ? new ColorGL(new Color(220, 245, 190))
                : new ColorGL(new Color(205, 185, 160));

        int labelWidth = UtilFont.getWidth(label);
        int labelX = switchX + (switchWidth - labelWidth) / 2;
        int labelY = switchY + (switchHeight - UtilFont.MAX_HEIGHT) / 2;

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

        UtilsGL.glBegin(GL11.GL_QUADS);
        UtilsGL.drawString(label, labelX, labelY, labelColor);
        UtilsGL.glEnd();

        // Knob
        int knobSize = switchHeight - 8;
        int knobX = enabled
                ? switchX + switchWidth - knobSize - 5
                : switchX + 5;
        int knobY = switchY + 4;

        MenuPrimitiveRenderer.drawColoredRect(
                knobX,
                knobY,
                knobSize,
                knobSize,
                0.14f,
                0.09f,
                0.05f);

        MenuPrimitiveRenderer.drawColoredRect(
                knobX + 2,
                knobY + 2,
                knobSize - 4,
                knobSize - 4,
                0.78f,
                0.68f,
                0.46f);

        MenuPrimitiveRenderer.drawColoredRect(
                knobX + 3,
                knobY + 3,
                knobSize - 6,
                2,
                0.92f,
                0.80f,
                0.55f);
    }
}