package xaos.api.definitions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefinitionIdTest {

    @Test
    void unqualifiedIdUsesTownsNamespace() {
        DefinitionId id = DefinitionId.parse("woodensword");

        assertEquals("towns", id.namespace());
        assertEquals("woodensword", id.path());
        assertEquals("towns:woodensword", id.toString());
    }

    @Test
    void qualifiedIdKeepsNamespaceAndPath() {
        DefinitionId id = DefinitionId.parse("examplemod:crystal");

        assertEquals("examplemod", id.namespace());
        assertEquals("crystal", id.path());
    }

    @Test
    void namespaceIsNormalizedToLowerCase() {
        DefinitionId id = DefinitionId.of("ExampleMod", "Crystal_Item");

        assertEquals("examplemod", id.namespace());
        assertEquals("Crystal_Item", id.path());
    }

    @Test
    void malformedIdsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> DefinitionId.parse(""));
        assertThrows(IllegalArgumentException.class, () -> DefinitionId.parse(":wood"));
        assertThrows(IllegalArgumentException.class, () -> DefinitionId.parse("towns:"));
        assertThrows(IllegalArgumentException.class, () -> DefinitionId.parse("a:b:c"));
    }
}
