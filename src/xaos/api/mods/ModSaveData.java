package xaos.api.mods;

import java.util.Set;

/**
 * Persistent key/value storage scoped to a single mod.
 *
 * Values are stored by Open Towns as strings, with convenience
 * methods for common primitive types.
 *
 * A mod can only access the store supplied through its own
 * ModContext.
 */
public interface ModSaveData {

    boolean contains(String key);

    String getString(
            String key,
            String defaultValue
    );

    void putString(
            String key,
            String value
    );

    int getInt(
            String key,
            int defaultValue
    );

    void putInt(
            String key,
            int value
    );

    long getLong(
            String key,
            long defaultValue
    );

    void putLong(
            String key,
            long value
    );

    double getDouble(
            String key,
            double defaultValue
    );

    void putDouble(
            String key,
            double value
    );

    boolean getBoolean(
            String key,
            boolean defaultValue
    );

    void putBoolean(
            String key,
            boolean value
    );

    void remove(String key);

    Set<String> keys();

    void clear();
}