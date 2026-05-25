package xaos.utils;

public final class TooltipScale {
    private static final float[] TOOLTIP_SCALE_VALUES = {
			0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f
	};

	private static int tooltipScaleIndex = 1;
	private static float scale = TOOLTIP_SCALE_VALUES[tooltipScaleIndex];
  
    private TooltipScale() {}

    public static float get() {
        return scale;
    }

    public static void set(float newScale) {
        if (newScale < 1.0f) {
            scale = 1.0f;
        } else if (newScale > 3.0f) {
            scale = 3.0f;
        } else {
            scale = newScale;
        }
    }

    public static int px(int value) {
        return Math.round(value * scale);
    }

    public static int fontWidth() {
        return px(UtilFont.MAX_WIDTH);
    }

    public static int fontHeight() {
        return px(UtilFont.MAX_HEIGHT);
    }

    public static int textWidth(String text) {
        return px(UtilFont.getWidth(text));
    }
    public static void cycleTooltipScale() {
		tooltipScaleIndex++;

		if (tooltipScaleIndex >= TOOLTIP_SCALE_VALUES.length) {
			tooltipScaleIndex = 0;
		}

		scale = TOOLTIP_SCALE_VALUES[tooltipScaleIndex];
	}
    public static String getDisplayText() {
		return Math.round(scale * 100f) + "%";
	}

	private static int getClosestIndex(float value) {
		int closestIndex = 0;
		float closestDistance = Math.abs(TOOLTIP_SCALE_VALUES[0] - value);

		for (int i = 1; i < TOOLTIP_SCALE_VALUES.length; i++) {
			float distance = Math.abs(TOOLTIP_SCALE_VALUES[i] - value);

			if (distance < closestDistance) {
				closestDistance = distance;
				closestIndex = i;
			}
		}

		return closestIndex;
	}
}
