package xaos;

/**
 * Game properties.
 *
 * These values used to be filtered by Maven.
 * Runtime overrides can now be passed from Gradle using JVM system properties.
 *
 * Example:
 * gradle run "-Popentowns.debug=true" "-Popentowns.testCommands=true"
 *
 * Production defaults should remain safe:
 * - DEBUG_MODE = false
 * - TEST_COMMANDS = false
 */
public class TownsProperties {

    public static final boolean GODS_ACTIVATED = false;

    public static boolean DEBUG_MODE = getBooleanProperty("opentowns.debug", false);

    public static final boolean DEMO_VERSION = false;

    public static boolean TEST_COMMANDS = getBooleanProperty("opentowns.testCommands", false);

    public static final String GAME_NAME = "Towns"; //$NON-NLS-1$
    public static final String GAME_VERSION = "v14f";
    public static final String GAME_VERSION_FULL = GAME_VERSION;
    public static final String GAME_VERSION_SHORT = GAME_VERSION;

    private static boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = System.getProperty(key);

        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }

        return value.equalsIgnoreCase("true")
                || value.equalsIgnoreCase("yes")
                || value.equalsIgnoreCase("1")
                || value.equalsIgnoreCase("on");
    }

}