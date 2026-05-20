
## TooltipRenderer

`TooltipRenderer` is now the preferred place for tooltip rendering logic.

Single-line tooltips should use:

```java
TooltipRenderer.draw(...)
```

Multiline tooltip-style boxes should use:

```java
MainPanel.renderMessages(...)
```

The old tooltip renderer:

```java
UtilsGL.drawTooltip(...)
```

has been deprecated and should not be used for new tooltip work.

---
## UIScale

A new helper class is used to centralise UI scale calculations:

```java
UIScale.px(value)
UIScale.fontWidth()
UIScale.fontHeight()
UIScale.textWidth(text)
UIScale.get()
UIScale.set(scale)
````

The default scale is currently:

```java
private static float scale = 1.0f;
```

This means the game starts at original/default size unless changed later.

Scale values are clamped between:

```java
1.0f
```

and:

```java
3.0f
```

This prevents the UI becoming too small or excessively large.




---

## Scaled text rendering

`TooltipRenderer.drawScaledString(...)` was introduced so text can be rendered using scaled glyph dimensions.

This avoids relying on OpenGL matrix scaling and makes tooltip text sizing match tooltip box sizing.

The method scales each character using:

```java
UIScale.px(...)
```

This keeps text width, height, and positioning consistent with:

```java
UIScale.textWidth(...)
UIScale.fontHeight()
```

---

## Tooltip positioning

Tooltip positioning has been updated to use scaled measurements.

For example, old code like this:

```java
UtilFont.getWidth(tooltip)
UtilFont.MAX_HEIGHT
```

has been replaced in tooltip positioning code with:

```java
UIScale.textWidth(tooltip)
UIScale.fontHeight()
```

Tile dimensions such as:

```java
tileIcon.getTileWidth()
tileIcon.getTileHeight()
```

are left unscaled because those values already represent the current rendered tile dimensions.

---

## Shared clamp logic

Tooltip boundary clamping has been moved into a helper:

```java
clampTooltipPosition(...)
```

This ensures tooltips do not render off-screen.

This is used by:

* single-line tooltips
* the events tooltip

This reduces duplicate clamping logic and keeps edge behaviour consistent.

---

## Message icon tooltips

The message icon tooltip logic has been simplified.

Previously, announcement, combat, heroes, and system message tooltips duplicated a lot of code and some older logic caused tooltip boxes to drift down the screen.

The old issue was caused by repeatedly mutating shared tooltip position state:

```java
tooltipY += UtilFont.MAX_HEIGHT;
```

This happened every frame while hovering, causing tooltips to move down the screen and eventually stack at the bottom.

The new logic builds a multiline message list and renders it once through:

```java
MainPanel.renderMessages(...)
```

The repeated message icon logic is now handled through:

```java
renderMessageIconTooltipIfNeeded(...)
renderMessageIconTooltip(...)
```

---

## Events tooltip

The events icon tooltip has been cleaned up and scaled.

The old version contained unreachable message-icon branches inside the events tooltip method. These were removed because `renderEventsTooltip(...)` is only called when hovering the events icon.

The events tooltip now handles only two states:

```text
No active events
One or more active events
```

When there are no events, it immediately draws a scaled single-line tooltip.

When events exist, it calculates scaled width, height, padding, row spacing, and text rendering manually so icons and text align correctly.

---

## MainPanel.renderMessages

`MainPanel.renderMessages(...)` is used for multiline tooltip boxes such as:

* build requirements
* item prerequisites
* message icon history
* world hover details
* living entity details

This method now supports scaled width, height, padding, line spacing, and text rendering.

Important rule:

```text
Pass raw separator values into renderMessages.
Do not pass UIScale.px(...) as the separator.
```

Correct:

```java
MainPanel.renderMessages(
		x,
		y,
		renderWidth,
		renderHeight,
		Tile.TERRAIN_ICON_WIDTH / 2,
		messages,
		colors);
```

Incorrect:

```java
MainPanel.renderMessages(
		x,
		y,
		renderWidth,
		renderHeight,
		UIScale.px(Tile.TERRAIN_ICON_WIDTH / 2),
		messages,
		colors);
```

`renderMessages(...)` scales the separator internally.

---

## Null colour handling

Some existing tooltip message lists intentionally pass `null` colours to mean “default white”.

The previous `UtilsGL.drawString(...)` method supported this behaviour internally.

Because scaled text rendering now sets colour directly before drawing, this behaviour had to be preserved manually.

Null colours now fall back to:

```java
ColorGL.WHITE
```

This prevents crashes such as:

```text
NullPointerException: Cannot read field "r" because "color" is null
```

---

## Current intended usage

Use this for normal single-line tooltips:

```java
tooltip = Messages.getString("Some.Key");
tooltipPoint = TooltipRenderer.rightOf(x, y);
```

The main renderer will later draw it through:

```java
TooltipRenderer.draw(...)
```

Use this for immediate custom single-line rendering:

```java
TooltipRenderer.draw(
		tooltip,
		x,
		y,
		renderWidth,
		renderHeight);
```

Use this for multiline tooltip boxes:

```java
MainPanel.renderMessages(
		x,
		y,
		renderWidth,
		renderHeight,
		2,
		messages,
		colors);
```
