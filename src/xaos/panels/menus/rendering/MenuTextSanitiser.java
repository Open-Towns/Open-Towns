package xaos.panels.menus.rendering;

public final class MenuTextSanitiser {

    private MenuTextSanitiser() {
    }

    public static String sanitise(String text) {
        if (text == null) {
            return "";
        }

        StringBuilder safe = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if ((c >= 'A' && c <= 'Z')
                    || (c >= 'a' && c <= 'z')
                    || (c >= '0' && c <= '9')
                    || c == ' '
                    || c == '-'
                    || c == '_'
                    || c == '/'
                    || c == ':'
                    || c == '.'
                    || c == ','
                    || c == '('
                    || c == ')') {
                safe.append(c);
            }
        }

        return safe.toString();
    }
}