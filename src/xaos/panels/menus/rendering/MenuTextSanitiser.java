package xaos.panels.menus.rendering;

public final class MenuTextSanitiser {

    private MenuTextSanitiser() {
    }

    public static String sanitise(String text) {
        if (text == null) {
            return "";
        }

        return removeControlCharacters(text);

    }

    private static String removeControlCharacters(String text) {
        StringBuilder builder = new StringBuilder(text.length());

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '\n' || c == '\r' || c == '\t') {
                builder.append(' ');
            } else if (!Character.isISOControl(c)) {
                builder.append(c);
            }
        }

        return builder.toString();
    }
}