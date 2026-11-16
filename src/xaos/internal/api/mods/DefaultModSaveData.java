package xaos.internal.api.mods;

import java.util.Set;

import xaos.api.mods.ModSaveData;

public final class DefaultModSaveData
        implements ModSaveData {

    private final ModSaveDataStore store;

    DefaultModSaveData(
            ModSaveDataStore store
    ) {
        if (store == null) {
            throw new IllegalArgumentException(
                    "Mod save data store cannot be null"
            );
        }

        this.store = store;
    }

    @Override
    public boolean contains(
            String key
    ) {
        validateKey(key);

        return store.contains(
                key
        );
    }

    @Override
    public String getString(
            String key,
            String defaultValue
    ) {
        validateKey(key);

        String value =
                store.get(
                        key
                );

        return value == null
                ? defaultValue
                : value;
    }

    @Override
    public void putString(
            String key,
            String value
    ) {
        validateKey(key);

        if (value == null) {
            store.remove(
                    key
            );

            return;
        }

        store.put(
                key,
                value
        );
    }

    @Override
    public int getInt(
            String key,
            int defaultValue
    ) {
        String value =
                getString(
                        key,
                        null
                );

        if (value == null) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(
                    value
            );
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    @Override
    public void putInt(
            String key,
            int value
    ) {
        putString(
                key,
                Integer.toString(value)
        );
    }

    @Override
    public long getLong(
            String key,
            long defaultValue
    ) {
        String value =
                getString(
                        key,
                        null
                );

        if (value == null) {
            return defaultValue;
        }

        try {
            return Long.parseLong(
                    value
            );
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    @Override
    public void putLong(
            String key,
            long value
    ) {
        putString(
                key,
                Long.toString(value)
        );
    }

    @Override
    public double getDouble(
            String key,
            double defaultValue
    ) {
        String value =
                getString(
                        key,
                        null
                );

        if (value == null) {
            return defaultValue;
        }

        try {
            return Double.parseDouble(
                    value
            );
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    @Override
    public void putDouble(
            String key,
            double value
    ) {
        putString(
                key,
                Double.toString(value)
        );
    }

    @Override
    public boolean getBoolean(
            String key,
            boolean defaultValue
    ) {
        String value =
                getString(
                        key,
                        null
                );

        if (value == null) {
            return defaultValue;
        }

        if ("true".equalsIgnoreCase(value)) {
            return true;
        }

        if ("false".equalsIgnoreCase(value)) {
            return false;
        }

        return defaultValue;
    }

    @Override
    public void putBoolean(
            String key,
            boolean value
    ) {
        putString(
                key,
                Boolean.toString(value)
        );
    }

    @Override
    public void remove(
            String key
    ) {
        validateKey(key);

        store.remove(
                key
        );
    }

    @Override
    public Set<String> keys() {
        return store.keys();
    }

    @Override
    public void clear() {
        store.clear();
    }

    private static void validateKey(
            String key
    ) {
        if (key == null
                || key.isBlank()) {

            throw new IllegalArgumentException(
                    "Mod save-data key cannot be blank"
            );
        }
    }
}