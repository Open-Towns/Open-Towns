package xaos.panels;

import java.awt.Point;

import org.lwjgl.opengl.GL11;

import xaos.main.Game;
import xaos.utils.ColorGL;
import xaos.utils.UIScale;
import xaos.utils.UtilsGL;
import xaos.utils.CharDef;
import xaos.utils.UtilFont;

public final class TooltipRenderer {

    private TooltipRenderer() {
    }

    public static Point centeredAbove(int centerX, int bottomY, String text) {
        return new Point(
                centerX - UIScale.textWidth(text) / 2,
                bottomY - UIScale.fontHeight() * 2);
    }

    public static Point centeredBelow(int centerX, int topY, String text) {
        return new Point(
                centerX - UIScale.textWidth(text) / 2,
                topY + UIScale.fontHeight() * 2);
    }

    public static Point rightOf(int x, int y) {
        return new Point(x + UIScale.px(32), y);
    }

    public static Point leftOf(int x, int y, String text) {
        return new Point(x - UIScale.textWidth(text), y);
    }

    public static void draw(String tooltip, int tooltipX, int tooltipY, int renderWidth, int renderHeight) {
        if (tooltip == null) {
            return;
        }

        int paddingX = UIScale.px(6);
        int paddingY = UIScale.px(3);
        int safetyPadding = UIScale.px(4);

        int tooltipWidth = UIScale.textWidth(tooltip) + paddingX * 2 + safetyPadding;
        int tooltipHeight = UIScale.fontHeight() + paddingY * 2;

        if (tooltipX < 0) {
            tooltipX = 0;
        } else if (tooltipX + tooltipWidth + UIScale.px(1) > renderWidth) {
            tooltipX -= (tooltipX + tooltipWidth + UIScale.px(1)) - renderWidth;
        }

        if (tooltipY < 0) {
            tooltipY = 0;
        } else if (tooltipY + tooltipHeight + UIScale.px(1) > renderHeight) {
            tooltipY -= (tooltipY + tooltipHeight + UIScale.px(1)) - renderHeight;
        }

        GL11.glColor4f(1, 1, 1, 1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, UIPanel.tileTooltipBackground.getTextureID());

        UtilsGL.glBegin(GL11.GL_QUADS);
        UtilsGL.drawTexture(
                tooltipX,
                tooltipY,
                tooltipX + tooltipWidth,
                tooltipY + tooltipHeight,
                UIPanel.tileTooltipBackground.getTileSetTexX0(),
                UIPanel.tileTooltipBackground.getTileSetTexY0(),
                UIPanel.tileTooltipBackground.getTileSetTexX1(),
                UIPanel.tileTooltipBackground.getTileSetTexY1());
        UtilsGL.glEnd();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);

        UtilsGL.glBegin(GL11.GL_QUADS);
        drawScaledString(tooltip, tooltipX + paddingX, tooltipY + paddingY);
        UtilsGL.glEnd();

        GL11.glColor3f(1, 1, 1);
    }

    private static void drawScaledString(String text, int x, int y) {
        if (text == null) {
            return;
        }

        int xOffset = x;

        for (int i = 0; i < text.length(); i++) {
            CharDef charDef = UtilFont.getCharDef(text.charAt(i));

            if (charDef == null) {
                continue;
            }

            UtilsGL.drawTexture(
                    xOffset,
                    y + UIScale.px(charDef.yoffset),
                    xOffset + UIScale.px(charDef.width),
                    y + UIScale.px(charDef.yoffset) + UIScale.px(charDef.height),
                    charDef.xTex,
                    charDef.yTex,
                    charDef.xTex + charDef.widthTex,
                    charDef.yTex + charDef.heightTex);

            xOffset += UIScale.px(charDef.xadvance);
        }
    }
}