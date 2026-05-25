package xaos.panels.UI;

public final class UIScaleSettings {
	private static final float WORLD_ZOOM_MIN = 0.5f;
	private static final float WORLD_ZOOM_MAX = 2.0f;

	private static final float UI_SCALE_MIN = 0.75f;
	private static final float UI_SCALE_MAX = 2.0f;

	private static final float TOOLTIP_SCALE_MIN = 0.75f;
	private static final float TOOLTIP_SCALE_MAX = 2.0f;

	private UIScaleSettings() {
	}

	public static float clampWorldZoom(float value) {
		return clamp(value, WORLD_ZOOM_MIN, WORLD_ZOOM_MAX);
	}

	public static float clampUIScale(float value) {
		return clamp(value, UI_SCALE_MIN, UI_SCALE_MAX);
	}

	public static float clampTooltipScale(float value) {
		return clamp(value, TOOLTIP_SCALE_MIN, TOOLTIP_SCALE_MAX);
	}

	public static float sliderValueFromMouse(int mouseX, int sliderX, int sliderWidth, float min, float max) {
		float scaleValue = (mouseX - sliderX) / (float) sliderWidth;

		if (scaleValue < 0f) {
			scaleValue = 0f;
		} else if (scaleValue > 1f) {
			scaleValue = 1f;
		}

		return min + ((max - min) * scaleValue);
	}

	public static int sliderHandleX(float value, int sliderX, int sliderWidth, float min, float max) {
		float scaleValue = (value - min) / (max - min);

		if (scaleValue < 0f) {
			scaleValue = 0f;
		} else if (scaleValue > 1f) {
			scaleValue = 1f;
		}

		return sliderX + Math.round(sliderWidth * scaleValue);
	}

	private static float clamp(float value, float min, float max) {
		if (value < min) {
			return min;
		}

		if (value > max) {
			return max;
		}

		return value;
	}
}