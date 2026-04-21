package xaos.utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import xaos.Towns;
import xaos.main.Game;

public final class Messages {

    public static final String BUNDLE_NAME = "data.languages.messages"; //$NON-NLS-1$

    private static ResourceBundle RESOURCE_BUNDLE = Utils.getResourceBundle(BUNDLE_NAME);

    public static void changeLanguage(String sLanguage, String sCountry, String sModName) {
        Locale.setDefault(Locale.of(sLanguage, sCountry));

        if (sModName == null) {
            RESOURCE_BUNDLE = Utils.getResourceBundle(BUNDLE_NAME);
        } else {
            File fUserFolder = new File(Game.getUserFolder());
            if (!fUserFolder.exists() || !fUserFolder.isDirectory()) {
                RESOURCE_BUNDLE = Utils.getResourceBundle(BUNDLE_NAME);
            } else {
                String sModLanguagePath;
                if (sLanguage.equals("en") && sCountry.equals("US")) {
                    sModLanguagePath = fUserFolder.getAbsolutePath() + System.getProperty("file.separator") + Game.MODS_FOLDER1 + System.getProperty("file.separator") + sModName + System.getProperty("file.separator") + Towns.getPropertiesString("DATA_FOLDER") + "/languages/messages.properties"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                } else {
                    sModLanguagePath = fUserFolder.getAbsolutePath() + System.getProperty("file.separator") + Game.MODS_FOLDER1 + System.getProperty("file.separator") + sModName + System.getProperty("file.separator") + Towns.getPropertiesString("DATA_FOLDER") + "/languages/messages_" + sLanguage + "_" + sCountry + ".properties"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
                }
                File f = new File(sModLanguagePath);
                if (f.exists()) {
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(f);
                        RESOURCE_BUNDLE = new PropertyResourceBundle(fis);
                    } catch (Exception e) {
                        RESOURCE_BUNDLE = Utils.getResourceBundle(BUNDLE_NAME);
                    } finally {
                        try {
                            if (fis != null) {
                                fis.close();
                            }
                        } catch (Exception closeException) {
                        }
                    }
                } else {
                    RESOURCE_BUNDLE = Utils.getResourceBundle(BUNDLE_NAME);
                }

            }
        }

        Game.exitToMainMenu();
    }

    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}
