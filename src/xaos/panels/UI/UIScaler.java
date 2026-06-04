package xaos.panels.UI;

public class UIScaler {
	private static final float[] UI_SCALE_VALUES = {
			0.75f, 1.0f, 1.25f, 1.5f
	};

	private static int uiScaleIndex = 1;
	private static float uiScale = UI_SCALE_VALUES[uiScaleIndex];

	public static float getUIScale() {
		return uiScale;
	}

	public static void setUIScale(float scale) {
		if (scale < 0.75f) {
			uiScale = 0.75f;
		} else if (scale > 2.0f) {
			uiScale = 2.0f;
		} else {
			uiScale = scale;
		}

		uiScaleIndex = getClosestIndex(uiScale);
	}

	public static void cycleUIScale() {
		uiScaleIndex++;

		if (uiScaleIndex >= UI_SCALE_VALUES.length) {
			uiScaleIndex = 0;
		}

		uiScale = UI_SCALE_VALUES[uiScaleIndex];
	}

	public static int ui(int value) {
		return Math.round(value * uiScale);
	}

	public static String getDisplayText() {
		return Math.round(uiScale * 100f) + "%";
	}

	private static int getClosestIndex(float value) {
		int closestIndex = 0;
		float closestDistance = Math.abs(UI_SCALE_VALUES[0] - value);

		for (int i = 1; i < UI_SCALE_VALUES.length; i++) {
			float distance = Math.abs(UI_SCALE_VALUES[i] - value);

			if (distance < closestDistance) {
				closestDistance = distance;
				closestIndex = i;
			}
		}

		return closestIndex;
	}
}