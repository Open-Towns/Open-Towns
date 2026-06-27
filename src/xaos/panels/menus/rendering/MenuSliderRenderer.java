package xaos.panels.menus.rendering;

import org.lwjgl.opengl.GL11;

import xaos.panels.UI.UIPanelState;
import xaos.panels.menus.MenuStateResolver;
import xaos.panels.menus.SmartMenu;
import xaos.utils.UtilsGL;

public final class MenuSliderRenderer {

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

        MenuTextRenderer.renderText(item, text, x, y);

        int sliderWidth = 140;
        int sliderHeight = 6;
        int sliderX = x + width - sliderWidth - 8;
        int sliderY = y + height / 2;

        float rawValue = MenuStateResolver.getFloat(item.getEffectiveStateKey());
        float value = normaliseSliderValue(rawValue, item.getSliderMin(), item.getSliderMax());

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, UIPanelState.tileTooltipBackground.getTextureID());
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE);

        UtilsGL.glBegin(GL11.GL_QUADS);

        GL11.glColor4f(0.4f, 0.4f, 0.4f, 1f);

        UtilsGL.drawTexture(
                sliderX,
                sliderY,
                sliderX + sliderWidth,
                sliderY + sliderHeight,
                UIPanelState.tileTooltipBackground.getTileSetTexX0(),
                UIPanelState.tileTooltipBackground.getTileSetTexY0(),
                UIPanelState.tileTooltipBackground.getTileSetTexX1(),
                UIPanelState.tileTooltipBackground.getTileSetTexY1());

        int fillWidth = (int) (sliderWidth * value);

        GL11.glColor4f(1f, 1f, 1f, 1f);

        UtilsGL.drawTexture(
                sliderX,
                sliderY,
                sliderX + fillWidth,
                sliderY + sliderHeight,
                UIPanelState.tileTooltipBackground.getTileSetTexX0(),
                UIPanelState.tileTooltipBackground.getTileSetTexY0(),
                UIPanelState.tileTooltipBackground.getTileSetTexX1(),
                UIPanelState.tileTooltipBackground.getTileSetTexY1());

        int knobSize = 12;
        int knobX = sliderX + fillWidth - knobSize / 2;

        UtilsGL.drawTexture(
                knobX,
                sliderY - 3,
                knobX + knobSize,
                sliderY + 9,
                UIPanelState.tileTooltipBackground.getTileSetTexX0(),
                UIPanelState.tileTooltipBackground.getTileSetTexY0(),
                UIPanelState.tileTooltipBackground.getTileSetTexX1(),
                UIPanelState.tileTooltipBackground.getTileSetTexY1());

        UtilsGL.glEnd();
    }

    private static float normaliseSliderValue(float value, float min, float max) {
        if (max <= min) {
            return 0f;
        }

        float normalised = (value - min) / (max - min);

        if (normalised < 0f) {
            return 0f;
        }

        if (normalised > 1f) {
            return 1f;
        }

        return normalised;
    }
}
