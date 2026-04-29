package xaos;

import xaos.property.PropertyFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Properties;

import xaos.main.Game;
import xaos.utils.JNASteamAPI;
import xaos.utils.Log;
import xaos.utils.Messages;

import com.sun.jna.Native;
import xaos.property.Property;

public final class Towns {

    // Properties in ini files
    public static Properties propertiesMain;
    public static Properties propertiesGraphics;

    public static boolean loadSteamAPI(String sLibName) {
        try {
            JNASteamAPI steamAPI = Native.load(sLibName, JNASteamAPI.class);
            steamAPI.SteamAPI_Init();
            return true;
        } catch (Throwable t) {
        }

        return false;
    }

    public static void main(String[] args) {
//		if (true) System.exit (0);
        // Steam
        if (!loadSteamAPI("steam_api")) {
            loadSteamAPI("steam_api64");
        }

        // Lanzamos la ventana principal
        try {
            new Game();
        } catch (Throwable t) {
            try {
                Writer writer = new StringWriter();
                PrintWriter pw = new PrintWriter(writer);
                t.printStackTrace(pw);
                pw.close();
                writer.close();

                Log.log(Log.LEVEL_ERROR, "Error Code [" + Game.iError + "]", "Towns"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                Log.log(Log.LEVEL_ERROR, writer.toString(), "Towns"); //$NON-NLS-1$
            } catch (Exception e) {
            }

            Game.exit();
        }
    }

    public native boolean SteamAPI_Init();

    /**
     * Loads town.ini
     */
    private static void loadPropertiesMain() {
        // Cargamos el .ini
        propertiesMain = new Properties();

        String sFile = "towns.ini"; //$NON-NLS-1$
        try {
            propertiesMain.load(new FileInputStream(sFile));
            try {
                propertiesMain.load(new FileInputStream(Game.getUserFolder() + Game.getFileSeparator() + sFile));
            } catch (Exception e) {
            }
        } catch (FileNotFoundException e) {
            Log.log(Log.LEVEL_ERROR, Messages.getString("Towns.2") + sFile, "Towns"); //$NON-NLS-1$ //$NON-NLS-2$
            Game.exit();
        } catch (IOException e) {
            Log.log(Log.LEVEL_ERROR, Messages.getString("Towns.7"), "Towns"); //$NON-NLS-1$ //$NON-NLS-2$
            Log.log(Log.LEVEL_ERROR, e.toString(), "Towns"); //$NON-NLS-1$
            Game.exit();
        }
    }

    /**
     * Loads graphics.ini
     */
    private static void loadPropertiesGraphics() {
        // Cargamos el .ini
        propertiesGraphics = new Properties();

        String sFile = "graphics.ini"; //$NON-NLS-1$
        try {
            propertiesGraphics.load(new FileInputStream(sFile));

            // Mods
            File fUserFolder = new File(Game.getUserFolder());
            if (!fUserFolder.exists() || !fUserFolder.isDirectory()) {
                return;
            }

            ArrayList<String> alMods = Game.getModsLoaded();
            if (alMods != null && alMods.size() > 0) {
                for (int i = 0; i < alMods.size(); i++) {
                    String sModGraphicsIniPath = fUserFolder.getAbsolutePath() + System.getProperty("file.separator") + Game.MODS_FOLDER1 + System.getProperty("file.separator") + alMods.get(i) + System.getProperty("file.separator") + "graphics.ini";
                    File fIni = new File(sModGraphicsIniPath);
                    if (fIni.exists()) {
                        propertiesGraphics.load(new FileInputStream(fIni));
                    }
                }
            }
        } catch (FileNotFoundException e) {
            Log.log(Log.LEVEL_ERROR, Messages.getString("Towns.2") + sFile, "Towns"); //$NON-NLS-1$ //$NON-NLS-2$
            Game.exit();
        } catch (IOException e) {
            Log.log(Log.LEVEL_ERROR, Messages.getString("Towns.7"), "Towns"); //$NON-NLS-1$ //$NON-NLS-2$
            Log.log(Log.LEVEL_ERROR, e.toString(), "Towns"); //$NON-NLS-1$
            Game.exit();
        }
    }

    public static <T> T getProperty(Property<T> property, T defaultValue) {
        final String rawValue = getPropertiesString(property.getPropertyFile(), property.getKey());
        if (rawValue != null) {
            final T value = property.getPropertyWrapper().wrap(rawValue);
            if (value != null) {
                return value;
            }
        }
        return defaultValue;
    }

    /**
     * Returns a property from main .ini converted to int with default value 0
     *
     * @param sProperty Property name
     * @return a property converted to int with default value 0
     */
    public static int getPropertiesInt(String sProperty) {
        return getPropertiesInt(PropertyFile.PROPERTY_FILE_MAIN, sProperty, 0);
    }

    /**
     * Returns a main property converted to int with default value if error
     *
     * @param sProperty Propery name
     * @param iDefaultValue Default value in case of error
     * @return a property converted to int with default value if error
     */
    public static int getPropertiesInt(String sProperty, int iDefaultValue) {
        return getPropertiesInt(PropertyFile.PROPERTY_FILE_MAIN, sProperty, iDefaultValue);
    }

    /**
     * Returns a property converted to int with default value 0
     *
     * @param propertyFile
     * @param sProperty Property name
     * @return a property converted to int with default value 0
     */
    public static int getPropertiesInt(PropertyFile propertyFile, String sProperty) {
        return getPropertiesInt(propertyFile, sProperty, 0);
    }

    /**
     * Returns a property converted to int with default value if error
     *
     * @param propertyFile
     * @param sProperty Propery name
     * @param iDefaultValue Default value in case of error
     * @return a property converted to int with default value if error
     */
    public static int getPropertiesInt(PropertyFile propertyFile, String sProperty, int iDefaultValue) {
        String sValue = getPropertiesString(propertyFile, sProperty);

        try {
            if (sValue != null) {
                return Integer.parseInt(sValue);
            }
        } catch (NumberFormatException nfe) {
            Log.log(Log.LEVEL_ERROR, Messages.getString("Towns.10") + sProperty + Messages.getString("Towns.11") + sValue + Messages.getString("Towns.12") + iDefaultValue + "]", "Towns"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        }

        return iDefaultValue;
    }

    /**
     * Returns a property from main .ini
     *
     * @param sProperty Property name
     * @return a property from .ini, null if something fails
     */
    public static String getPropertiesString(String sProperty) {
        return getPropertiesString(PropertyFile.PROPERTY_FILE_MAIN, sProperty);
    }

    /**
     *
     * @param propertyFile
     * @param sProperty Property name
     * @return a property from a .ini, null if something fails
     */
    public static String getPropertiesString(PropertyFile propertyFile, String sProperty) {
        if (propertyFile == PropertyFile.PROPERTY_FILE_MAIN && propertiesMain == null) {
            loadPropertiesMain();
        } else if (propertyFile == PropertyFile.PROPERTY_FILE_GRAPHICS && propertiesGraphics == null) {
            loadPropertiesGraphics();
        }

        if (sProperty == null || sProperty.length() == 0) {
            Log.log(Log.LEVEL_ERROR, Messages.getString("Towns.15"), "Towns"); //$NON-NLS-1$ //$NON-NLS-2$
            Game.exit();
        }

        switch (propertyFile) {
            case PROPERTY_FILE_MAIN:
                return propertiesMain.getProperty(sProperty);
            case PROPERTY_FILE_GRAPHICS:
                return propertiesGraphics.getProperty(sProperty);
            default:
                return null;
        }
    }

    public static Properties getPropertiesGraphics() {
        if (propertiesGraphics == null) {
            loadPropertiesGraphics();
        }

        return propertiesGraphics;
    }

    public static void clearPropertiesGraphics() {
        if (propertiesGraphics != null) {
            propertiesGraphics.clear();
        }
        propertiesGraphics = null;
    }
}
