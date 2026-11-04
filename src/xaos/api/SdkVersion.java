package xaos.api;

/**
 * Version information for the public Open Towns SDK.
 */
public final class SdkVersion {

    /**
     * Human-readable SDK release version.
     */
    public static final String SDK_VERSION =
            "0.1.0-dev";

    /**
     * Current public API version.
     *
     * Mods declare the API version they were built against
     * in mod.properties.
     */
    public static final int API_VERSION = 1;

    /**
     * Oldest API version this build still supports.
     */
    public static final int MIN_SUPPORTED_API_VERSION = 1;

    private SdkVersion() {
    }
}