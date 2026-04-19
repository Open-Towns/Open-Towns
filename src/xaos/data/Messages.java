package xaos.data;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import xaos.utils.Utils;

public class Messages {

    private static final String BUNDLE_NAME = "xaos.data.messages"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = Utils.getResourceBundle(BUNDLE_NAME);

    private Messages() {
    }

    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}
