package xaos.internal.api.definitions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xaos.api.definitions.DefinitionId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefinitionOwnershipRegistryTest {

    @BeforeEach
    void setUp() {
        DefinitionOwnershipRegistry.clear();
    }

    @AfterEach
    void tearDown() {
        DefinitionOwnershipRegistry.clear();
    }

    @Test
    void unknownDefinitionDefaultsToTownsNamespace() {

        assertEquals(
                DefinitionId.DEFAULT_NAMESPACE,
                DefinitionOwnershipRegistry.namespaceFor(
                        DefinitionKind.ITEM,
                        "wood"
                )
        );

        assertEquals(
                DefinitionId.towns("wood"),
                DefinitionOwnershipRegistry.idFor(
                        DefinitionKind.ITEM,
                        "wood"
                )
        );
    }

    @Test
    void registeredDefinitionUsesOwningNamespace() {

        DefinitionOwnershipRegistry.register(
                DefinitionKind.ITEM,
                "conveyor",
                "lava-logistics"
        );

        assertEquals(
                "lava-logistics",
                DefinitionOwnershipRegistry.namespaceFor(
                        DefinitionKind.ITEM,
                        "conveyor"
                )
        );

        assertEquals(
                DefinitionId.of(
                        "lava-logistics",
                        "conveyor"
                ),
                DefinitionOwnershipRegistry.idFor(
                        DefinitionKind.ITEM,
                        "conveyor"
                )
        );
    }

    @Test
    void sameOwnerDoesNotConflict() {

        DefinitionOwnershipRegistry.register(
                DefinitionKind.ITEM,
                "conveyor",
                "lava-logistics"
        );

        assertFalse(
                DefinitionOwnershipRegistry.conflictsWithOwner(
                        DefinitionKind.ITEM,
                        "conveyor",
                        "lava-logistics"
                )
        );
    }

    @Test
    void differentModOwnerConflicts() {

        DefinitionOwnershipRegistry.register(
                DefinitionKind.ITEM,
                "conveyor",
                "lava-logistics"
        );

        assertTrue(
                DefinitionOwnershipRegistry.conflictsWithOwner(
                        DefinitionKind.ITEM,
                        "conveyor",
                        "strip-mine"
                )
        );
    }

    @Test
    void baseGameDefinitionMayBeOverridden() {

        assertFalse(
                DefinitionOwnershipRegistry.conflictsWithOwner(
                        DefinitionKind.ITEM,
                        "wood",
                        "example-mod"
                )
        );
    }

    @Test
    void unregisterRestoresDefaultNamespace() {

        DefinitionOwnershipRegistry.register(
                DefinitionKind.ITEM,
                "conveyor",
                "lava-logistics"
        );

        DefinitionOwnershipRegistry.unregister(
                DefinitionKind.ITEM,
                "conveyor"
        );

        assertEquals(
                DefinitionId.towns("conveyor"),
                DefinitionOwnershipRegistry.idFor(
                        DefinitionKind.ITEM,
                        "conveyor"
                )
        );
    }

    @Test
    void ownershipIsSeparateForEachDefinitionKind() {

        DefinitionOwnershipRegistry.register(
                DefinitionKind.ITEM,
                "example",
                "item-mod"
        );

        DefinitionOwnershipRegistry.register(
                DefinitionKind.ACTION,
                "example",
                "action-mod"
        );

        assertEquals(
                DefinitionId.of(
                        "item-mod",
                        "example"
                ),
                DefinitionOwnershipRegistry.idFor(
                        DefinitionKind.ITEM,
                        "example"
                )
        );

        assertEquals(
                DefinitionId.of(
                        "action-mod",
                        "example"
                ),
                DefinitionOwnershipRegistry.idFor(
                        DefinitionKind.ACTION,
                        "example"
                )
        );
    }

    @Test
    void resolverAcceptsCorrectOwner() {

        DefinitionOwnershipRegistry.register(
                DefinitionKind.ITEM,
                "conveyor",
                "lava-logistics"
        );

        assertEquals(
                "conveyor",
                DefinitionResolver.resolveItem(
                        DefinitionId.of(
                                "lava-logistics",
                                "conveyor"
                        )
                )
        );
    }

    @Test
    void resolverRejectsWrongNamespace() {

        DefinitionOwnershipRegistry.register(
                DefinitionKind.ITEM,
                "conveyor",
                "lava-logistics"
        );

        assertNull(
                DefinitionResolver.resolveItem(
                        DefinitionId.of(
                                "strip-mine",
                                "conveyor"
                        )
                )
        );

        assertNull(
                DefinitionResolver.resolveItem(
                        DefinitionId.towns(
                                "conveyor"
                        )
                )
        );
    }
}