package xaos.panels.menus.rendering;

import org.lwjgl.opengl.GL11;

import xaos.panels.UI.UIPanelState;
import xaos.panels.menus.MenuStateResolver;
import xaos.panels.menus.SmartMenu;
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

        MenuTextRenderer.renderText(item, text, x, y);

        int toggleWidth = 34;
        int toggleHeight = 14;
        int toggleX = x + width - toggleWidth - 8;
        int toggleY = y + 2;

        boolean enabled = MenuStateResolver.getBoolean(item.getEffectiveStateKey());

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, UIPanelState.tileTooltipBackground.getTextureID());
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE);

        UtilsGL.glBegin(GL11.GL_QUADS);

        if (enabled) {
            GL11.glColor4f(0.5f, 1f, 0.5f, 1f);
        } else {
            GL11.glColor4f(0.5f, 0.5f, 0.5f, 1f);
        }

        UtilsGL.drawTexture(
                toggleX,
                toggleY,
                toggleX + toggleWidth,
                toggleY + toggleHeight,
                UIPanelState.tileTooltipBackground.getTileSetTexX0(),
                UIPanelState.tileTooltipBackground.getTileSetTexY0(),
                UIPanelState.tileTooltipBackground.getTileSetTexX1(),
                UIPanelState.tileTooltipBackground.getTileSetTexY1());

        int knobSize = toggleHeight - 4;
        int knobX = enabled
                ? toggleX + toggleWidth - knobSize - 2
                : toggleX + 2;

        GL11.glColor4f(1f, 1f, 1f, 1f);

        UtilsGL.drawTexture(
                knobX,
                toggleY + 2,
                knobX + knobSize,
                toggleY + 2 + knobSize,
                UIPanelState.tileTooltipBackground.getTileSetTexX0(),
                UIPanelState.tileTooltipBackground.getTileSetTexY0(),
                UIPanelState.tileTooltipBackground.getTileSetTexX1(),
                UIPanelState.tileTooltipBackground.getTileSetTexY1());

        UtilsGL.glEnd();
    }
}