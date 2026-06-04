
# UIPanelScaler

`UIPanelScaler` is a shared helper class for scaling UI panel positions, sizes, tiles, icons, and mouse hitboxes.

It is used alongside `UIScaler`, which stores the current UI scale value.

## Purpose

The UI scaling system separates two responsibilities:

- `UIScaler` stores and clamps the current UI scale value.
- `UIPanelScaler` applies that scale to panel rendering and input calculations.

This keeps panel classes cleaner and avoids duplicating scaling logic across `BottomPanel`, `RightPanel`, `LeftPanel`, and `UIPanelInputHandler`.

## Package

```java
package xaos.panels.UI;
````

## Main usage pattern

Most panels should follow this pattern:

```java
Point scaledPanelPoint = anchorFromRight(
		originalPanelPoint,
		originalPanelWidth,
		ui(originalPanelWidth));

Point scaledItemPoint = scalePointFromAnchor(
		originalItemPoint,
		originalPanelPoint,
		scaledPanelPoint);

drawScaledTile(tile, scaledItemPoint, ui(width), ui(height));
```

The important rule is:

```text
Render position and mouse input position must use the same scaled maths.
```

If a panel is scaled visually but the input handler still uses the original positions, hover and click detection will not line up.

---

# Methods

## `ui(int value)`

```java
public static int ui(int value)
```

Scales an integer value using the current UI scale.

Example:

```java
int scaledWidth = UIPanelScaler.ui(BOTTOM_ITEM_WIDTH);
```

This is a shortcut for:

```java
UIScaler.ui(value);
```

---

## `scalePointFromAnchor(...)`

```java
public static Point scalePointFromAnchor(Point originalPoint, Point originalAnchor, Point scaledAnchor)
```

Scales a point based on its offset from an original anchor point.

This is the most important method in the scaler.

Example:

```java
Point scaledItemPoint = scalePointFromAnchor(
		menuPanelItemsPosition.get(i),
		menuPanelPoint,
		scaledMenuPanelPoint);
```

This means:

```text
Take the item's original offset from the original panel.
Scale that offset.
Apply it to the scaled panel point.
```

Use this for:

* menu buttons
* icon positions
* scroll buttons
* close buttons inside panels
* panel contents that should stay aligned relative to their panel

Do not use this for standalone edge tabs unless they should move with the panel.

---

## `anchorFromCentreXAndBottom(...)`

```java
public static Point anchorFromCentreXAndBottom(Point originalPoint, int originalWidth, int originalHeight,
		int scaledWidth, int scaledHeight)
```

Returns a scaled panel point while keeping:

* the original horizontal centre fixed
* the original bottom edge fixed

This is useful for the bottom panel.

Example:

```java
Point bottomPanelPoint = anchorFromCentreXAndBottom(
		new Point(bottomPanelX, bottomPanelY),
		BOTTOM_PANEL_WIDTH,
		BOTTOM_PANEL_HEIGHT,
		ui(BOTTOM_PANEL_WIDTH),
		ui(BOTTOM_PANEL_HEIGHT));
```

This lets the bottom panel grow:

```text
left and right from the centre
upwards from the bottom edge
```

---

## `anchorFromRight(...)`

```java
public static Point anchorFromRight(Point originalPoint, int originalWidth, int scaledWidth)
```

Returns a scaled panel point while keeping the original right edge fixed.

This is useful for the right panel.

Example:

```java
Point scaledMenuPanelPoint = anchorFromRight(
		menuPanelPoint,
		MENU_PANEL_WIDTH,
		ui(MENU_PANEL_WIDTH));
```

This lets the right panel grow leftwards as it scales.

---

## `anchorFromBottom(...)`

```java
public static Point anchorFromBottom(Point originalPoint, int originalHeight, int scaledHeight)
```

Returns a scaled point while keeping the original bottom edge fixed.

This is useful for elements that should grow upwards while staying attached to the bottom of the screen.

---

## `drawScaledTile(...)`

```java
public static void drawScaledTile(Tile tile, Point point, int width, int height)
```

Draws a tile at a specific scaled position and size.

Example:

```java
drawScaledTile(
		tileBottomItem,
		point,
		ui(BOTTOM_ITEM_WIDTH),
		ui(BOTTOM_ITEM_HEIGHT));
```

This bypasses `UIPanel.drawTile(...)`, which may centre or clamp tiles based on their original size.

Use this when you need the tile to actually render larger or smaller.

---

## `drawScaledTile(Tile tile, Point point)`

```java
public static void drawScaledTile(Tile tile, Point point)
```

Draws a tile using its own scaled tile width and height.

Example:

```java
drawScaledTile(tileOpenProductionPanel, openClosePoint);
```

This is equivalent to:

```java
drawScaledTile(
		tileOpenProductionPanel,
		openClosePoint,
		ui(tileOpenProductionPanel.getTileWidth()),
		ui(tileOpenProductionPanel.getTileHeight()));
```

---

## `drawScaledIcon(...)`

```java
public static void drawScaledIcon(Tile tile, Point buttonPoint, int buttonWidth, int buttonHeight)
```

Draws an icon inside a scaled button area with a default inset.

Example:

```java
drawScaledIcon(
		iconTile,
		buttonPoint,
		ui(BOTTOM_ITEM_WIDTH),
		ui(BOTTOM_ITEM_HEIGHT));
```

This keeps icons centred inside their button background.

---

## `drawScaledIcon(..., int inset)`

```java
public static void drawScaledIcon(Tile tile, Point buttonPoint, int buttonWidth, int buttonHeight, int inset)
```

Draws an icon inside a scaled button area using a custom inset.

Example:

```java
drawScaledIcon(
		iconTile,
		buttonPoint,
		ui(BOTTOM_ITEM_WIDTH),
		ui(BOTTOM_ITEM_HEIGHT),
		4);
```

The inset is also scaled with the UI scale.

---

## `isMouseInside(...)`

```java
public static boolean isMouseInside(int mouseX, int mouseY, Point point, int width, int height)
```

Checks whether the mouse is inside a scaled rectangle.

Example:

```java
if (isMouseInside(mouseX, mouseY, point, itemWidth, itemHeight)) {
	return i;
}
```

This is useful in `UIPanelInputHandler`.

---

# Panel anchoring guide

## Bottom panel

Use:

```java
anchorFromCentreXAndBottom(...)
```

The bottom panel should stay centred and grow upwards.

## Right panel

Use:

```java
anchorFromRight(...)
```

The right panel should keep its right edge fixed and grow leftwards.

## Left production panel

Usually keep the top-left point fixed:

```java
Point scaledProductionPanelPoint = productionPanelPoint;
```

Then scale child points relative to it:

```java
scalePointFromAnchor(originalPoint, productionPanelPoint, scaledProductionPanelPoint);
```

## Open/close edge tabs

Do not always scale these relative to the panel.

For example, the left production tab is outside the panel and sits at the left edge of the screen. It should usually keep `x = 0` and scale around its own vertical centre.

Example:

```java
private static Point getScaledProductionOpenClosePoint(Tile tile) {
	int scaledHeight = UIPanelScaler.ui(tile.getTileHeight());

	int originalCenterY = UIPanelState.tileOpenCloseProductionPanelPoint.y
			+ (tile.getTileHeight() / 2);

	return new Point(
			UIPanelState.tileOpenCloseProductionPanelPoint.x,
			originalCenterY - (scaledHeight / 2));
}
```

---

# Common mistakes

## Scaling the absolute point directly

Avoid this:

```java
new Point(ui(originalPoint.x), ui(originalPoint.y));
```

This moves the panel away from its intended screen anchor.

Instead, scale the offset from an anchor:

```java
scalePointFromAnchor(originalPoint, originalPanelPoint, scaledPanelPoint);
```

## Rendering scaled but using old input hitboxes

If the UI looks correct but the mouse does not line up, the input handler is still using old positions.

Every scaled render position needs a matching scaled input position.

## Using `UIPanel.drawTile(...)` for scaled buttons

`UIPanel.drawTile(...)` centres the tile using the tile's original size. For true scaling, use:

```java
drawScaledTile(...)
```

## Scaling text positions but not text size

Most current code scales text positions only. The font itself is not scaled unless `UtilFont` or text rendering is updated separately.

This is expected for now.

---

# Recommended workflow for scaling a panel

1. Scale the panel background.
2. Scale all child points relative to the panel anchor.
3. Replace `drawTile(...)` with `drawScaledTile(...)` for buttons/backgrounds.
4. Use `drawScaledIcon(...)` for icons inside buttons.
5. Scale number/text positions.
6. Update matching mouse hitboxes in `UIPanelInputHandler`.
7. Test at `1.0`, `1.5`, and `2.0`.

---

# Example

```java
Point scaledPanelPoint = anchorFromRight(
		menuPanelPoint,
		MENU_PANEL_WIDTH,
		ui(MENU_PANEL_WIDTH));

Point itemPoint = scalePointFromAnchor(
		menuPanelItemsPosition.get(i),
		menuPanelPoint,
		scaledPanelPoint);

drawScaledTile(
		tileBottomItem,
		itemPoint,
		ui(BOTTOM_ITEM_WIDTH),
		ui(BOTTOM_ITEM_HEIGHT));

drawScaledIcon(
		iconTile,
		itemPoint,
		ui(BOTTOM_ITEM_WIDTH),
		ui(BOTTOM_ITEM_HEIGHT));
```

This keeps rendering and layout predictable across UI scale values.

