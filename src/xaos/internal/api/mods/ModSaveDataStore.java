package xaos.internal.api.mods;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

final class ModSaveDataStore {

    private final Map<String, String> values =
            new LinkedHashMap<>();

    synchronized boolean contains(
            String key
    ) {
        return values.containsKey(key);
    }

    synchronized String get(
            String key
    ) {
        return values.get(key);
    }

    synchronized void put(
            String key,
            String value
    ) {
        values.put(
                key,
                value
        );
    }

    synchronized void remove(
            String key
    ) {
        values.remove(key);
    }

    synchronized Set<String> keys() {
        return Set.copyOf(
                values.keySet()
        );
    }

    synchronized void clear() {
        values.clear();
    }

    synchronized Map<String, String> snapshot() {
        return new LinkedHashMap<>(
                values
        );
    }

    synchronized void replace(
            Map<String, String> newValues
    ) {
        values.clear();

        if (newValues != null) {
            values.putAll(
                    newValues
            );
        }
    }
}