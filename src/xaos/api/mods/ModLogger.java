package xaos.api.mods;

/**
 * Logger exposed to Java mods.
 *
 * Mods should use this rather than depending directly on the
 * game's internal logging implementation.
 */
public interface ModLogger {

    void info(String message);

    void error(String message);

    void error(String message, Throwable throwable);
}
