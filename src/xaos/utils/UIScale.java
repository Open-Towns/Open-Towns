package xaos.utils;

public final class UIScale {
    private static float scale = 1.5f;

    private UIScale() {}

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
}
