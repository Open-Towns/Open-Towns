package xaos.panels.UI;

import java.awt.Point;

import xaos.tiles.Tile;
import xaos.utils.UtilsGL;

public final class UIPanelScaler {

	private UIPanelScaler() {
	}

	public static int ui(int value) {
		return UIScaler.ui(value);
	}

	public static Point scalePoint(Point point) {
		return new Point(ui(point.x), ui(point.y));
	}

	public static Point scalePointFromAnchor(Point originalPoint, Point originalAnchor, Point scaledAnchor) {
		int offsetX = originalPoint.x - originalAnchor.x;
		int offsetY = originalPoint.y - originalAnchor.y;

		return new Point(
				scaledAnchor.x + ui(offsetX),
				scaledAnchor.y + ui(offsetY));
	}

	public static Point scalePanelFromCentreX(Point originalPoint, int originalWidth, int scaledWidth) {
		int originalCenterX = originalPoint.x + (originalWidth / 2);

		return new Point(
				originalCenterX - (scaledWidth / 2),
				originalPoint.y);
	}

	public static Point scalePanelFromRightEdge(Point originalPoint, int originalWidth, int scaledWidth) {
		int originalRightEdge = originalPoint.x + originalWidth;

		return new Point(
				originalRightEdge - scaledWidth,
				originalPoint.y);
	}

	public static Point scalePanelFromBottomEdge(Point originalPoint, int originalHeight, int scaledHeight) {
		int originalBottomEdge = originalPoint.y + originalHeight;

		return new Point(
				originalPoint.x,
				originalBottomEdge - scaledHeight);
	}

	public static Point scalePanelFromCentreXAndBottomEdge(Point originalPoint, int originalWidth, int originalHeight,
			int scaledWidth, int scaledHeight) {
		int originalCenterX = originalPoint.x + (originalWidth / 2);
		int originalBottomEdge = originalPoint.y + originalHeight;

		return new Point(
				originalCenterX - (scaledWidth / 2),
				originalBottomEdge - scaledHeight);
	}

	public static void drawScaledTile(Tile tile, Point point, int width, int height) {
		UtilsGL.drawTexture(
				point.x,
				point.y,
				point.x + width,
				point.y + height,
				tile.getTileSetTexX0(),
				tile.getTileSetTexY0(),
				tile.getTileSetTexX1(),
				tile.getTileSetTexY1());
	}

	public static void drawScaledTile(Tile tile, Point point) {
		drawScaledTile(
				tile,
				point,
				ui(tile.getTileWidth()),
				ui(tile.getTileHeight()));
	}

	public static void drawScaledIcon(Tile tile, Point buttonPoint, int buttonWidth, int buttonHeight) {
		drawScaledIcon(tile, buttonPoint, buttonWidth, buttonHeight, 6);
	}

	public static void drawScaledIcon(Tile tile, Point buttonPoint, int buttonWidth, int buttonHeight, int inset) {
		int iconInset = ui(inset);

		int iconX = buttonPoint.x + iconInset;
		int iconY = buttonPoint.y + iconInset;
		int iconWidth = buttonWidth - (iconInset * 2);
		int iconHeight = buttonHeight - (iconInset * 2);

		if (iconWidth < 1) {
			iconWidth = 1;
		}

		if (iconHeight < 1) {
			iconHeight = 1;
		}

		UtilsGL.drawTexture(
				iconX,
				iconY,
				iconX + iconWidth,
				iconY + iconHeight,
				tile.getTileSetTexX0(),
				tile.getTileSetTexY0(),
				tile.getTileSetTexX1(),
				tile.getTileSetTexY1());
	}

	public static boolean isMouseInside(int mouseX, int mouseY, Point point, int width, int height) {
		return mouseX >= point.x
				&& mouseX < point.x + width
				&& mouseY >= point.y
				&& mouseY < point.y + height;
	}
    public static Point anchorFromCentreXAndBottom(Point originalPoint, int originalWidth, int originalHeight,
		int scaledWidth, int scaledHeight) {
	int originalCentreX = originalPoint.x + (originalWidth / 2);
	int originalBottomY = originalPoint.y + originalHeight;

	return new Point(
			originalCentreX - (scaledWidth / 2),
			originalBottomY - scaledHeight);
}
public static Point anchorFromRight(Point originalPoint, int originalWidth, int scaledWidth) {
	int originalRightX = originalPoint.x + originalWidth;

	return new Point(
			originalRightX - scaledWidth,
			originalPoint.y);
}
}
