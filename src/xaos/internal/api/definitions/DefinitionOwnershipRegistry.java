package xaos.internal.api.definitions;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import xaos.api.definitions.DefinitionId;

public final class DefinitionOwnershipRegistry {

        private static final Map<DefinitionKind, Map<String, String>> owners = new EnumMap<>(DefinitionKind.class);

        private DefinitionOwnershipRegistry() {
        }

        public static synchronized void register(
                        DefinitionKind kind,
                        String legacyId,
                        String namespace) {
                if (kind == null
                                || legacyId == null
                                || legacyId.isBlank()
                                || namespace == null
                                || namespace.isBlank()) {

                        return;
                }

                owners.computeIfAbsent(
                                kind,
                                ignored -> new HashMap<>()).put(
                                                legacyId,
                                                namespace);
        }

        public static synchronized void unregister(
                        DefinitionKind kind,
                        String legacyId) {
                Map<String, String> definitions = owners.get(kind);

                if (definitions == null) {
                        return;
                }

                definitions.remove(legacyId);
        }

        public static synchronized String namespaceFor(
                        DefinitionKind kind,
                        String legacyId) {
                Map<String, String> definitions = owners.get(kind);

                if (definitions == null) {
                        return DefinitionId.DEFAULT_NAMESPACE;
                }

                return definitions.getOrDefault(
                                legacyId,
                                DefinitionId.DEFAULT_NAMESPACE);
        }

        public static DefinitionId idFor(
                        DefinitionKind kind,
                        String legacyId) {
                return DefinitionId.of(
                                namespaceFor(
                                                kind,
                                                legacyId),
                                legacyId);
        }

        public static boolean conflictsWithOwner(
                        DefinitionKind kind,
                        String legacyId,
                        String namespace) {
                String existingNamespace = namespaceFor(
                                kind,
                                legacyId);

                if (existingNamespace == null) {
                        return false;
                }

                /*
                 * Mods may intentionally override base-game definitions.
                 */
                if (existingNamespace.equals(
                                DefinitionId.DEFAULT_NAMESPACE)) {

                        return false;
                }

                /*
                 * The same namespace may encounter the definition again.
                 */
                return !existingNamespace.equals(
                                namespace);
        }

        public static synchronized void clear() {
                owners.clear();
        }
}