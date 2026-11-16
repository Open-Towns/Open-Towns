package xaos.internal.api.definitions;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import xaos.api.definitions.Definition;
import xaos.api.definitions.DefinitionId;
import xaos.api.definitions.DefinitionRegistry;
import xaos.internal.api.definitions.MappedDefinitionRegistry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MappedDefinitionRegistryTest {

    @Test
    void resolvesLegacyAndQualifiedTownsIds() {
        Map<String, String> source = new LinkedHashMap<>();
        source.put("wood", "Wood");

        DefinitionRegistry<TestDefinition> registry = registry(source);

        assertEquals("wood", registry.get("wood").legacyId());
        assertEquals("wood", registry.get("towns:wood").legacyId());
    }

    @Test
    void rejectsUnknownNamespaceUntilSourceAwareLoadingExists() {
        Map<String, String> source = new LinkedHashMap<>();
        source.put("wood", "Wood");

        DefinitionRegistry<TestDefinition> registry = registry(source);

        assertNull(registry.get("examplemod:wood"));
        assertFalse(registry.contains("examplemod:wood"));
    }

    @Test
    void allAndIdsExposeTheCurrentSourceMap() {
        Map<String, String> source = new LinkedHashMap<>();
        source.put("wood", "Wood");
        source.put("stone", "Stone");

        DefinitionRegistry<TestDefinition> registry = registry(source);

        assertEquals(2, registry.size());
        assertTrue(registry.ids().contains(DefinitionId.towns("wood")));
        assertTrue(registry.ids().contains(DefinitionId.towns("stone")));
    }

    @Test
    void invalidLookupReturnsNullInsteadOfEscapingParserError() {
        DefinitionRegistry<TestDefinition> registry = registry(Map.of());

        assertNull(registry.get("bad:id:format"));
    }

    private static DefinitionRegistry<TestDefinition> registry(
            Map<String, String> source) {
        return new MappedDefinitionRegistry<>(
                DefinitionKind.ITEM,
                () -> source,
                (id, value) -> new TestDefinition(
                        id,
                        value));
    }

    private static final class TestDefinition implements Definition {
        private final DefinitionId id;
        private final String legacyId;
        private final String name;

        private TestDefinition(
                DefinitionId id,
                String name) {
            this.id = id;
            this.legacyId = id.path();
            this.name = name;
        }

        @Override
        public DefinitionId id() {
            return id;
        }

        @Override
        public String legacyId() {
            return legacyId;
        }

        @Override
        public String name() {
            return name;
        }
    }
}
