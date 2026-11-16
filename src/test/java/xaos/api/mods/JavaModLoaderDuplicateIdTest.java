package xaos.api.mods;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaModLoaderDuplicateIdTest {

    @Test
    void firstModIdIsRegistered() {

        Map<String, String> discovered =
                new HashMap<>();

        Set<String> duplicates =
                new HashSet<>();

        var result =
                JavaModLoader.registerDiscovered(
                        discovered,
                        duplicates,
                        "example-mod",
                        "first"
                );

        assertEquals(
                JavaModLoader.DiscoveryRegistrationResult.ADDED,
                result
        );

        assertEquals(
                "first",
                discovered.get(
                        "example-mod"
                )
        );

        assertTrue(
                duplicates.isEmpty()
        );
    }

    @Test
    void secondModWithSameIdRemovesFirst() {

        Map<String, String> discovered =
                new HashMap<>();

        Set<String> duplicates =
                new HashSet<>();

        JavaModLoader.registerDiscovered(
                discovered,
                duplicates,
                "example-mod",
                "first"
        );

        var result =
                JavaModLoader.registerDiscovered(
                        discovered,
                        duplicates,
                        "example-mod",
                        "second"
                );

        assertEquals(
                JavaModLoader.DiscoveryRegistrationResult.DUPLICATE_DETECTED,
                result
        );

        assertFalse(
                discovered.containsKey(
                        "example-mod"
                )
        );

        assertTrue(
                duplicates.contains(
                        "example-mod"
                )
        );
    }

    @Test
    void thirdModWithDuplicateIdIsIgnored() {

        Map<String, String> discovered =
                new HashMap<>();

        Set<String> duplicates =
                new HashSet<>();

        JavaModLoader.registerDiscovered(
                discovered,
                duplicates,
                "example-mod",
                "first"
        );

        JavaModLoader.registerDiscovered(
                discovered,
                duplicates,
                "example-mod",
                "second"
        );

        var result =
                JavaModLoader.registerDiscovered(
                        discovered,
                        duplicates,
                        "example-mod",
                        "third"
                );

        assertEquals(
                JavaModLoader.DiscoveryRegistrationResult.ALREADY_DUPLICATE,
                result
        );

        assertFalse(
                discovered.containsKey(
                        "example-mod"
                )
        );
    }

    @Test
    void duplicateIdDoesNotAffectOtherMods() {

        Map<String, String> discovered =
                new HashMap<>();

        Set<String> duplicates =
                new HashSet<>();

        JavaModLoader.registerDiscovered(
                discovered,
                duplicates,
                "duplicate-mod",
                "first"
        );

        JavaModLoader.registerDiscovered(
                discovered,
                duplicates,
                "working-mod",
                "working"
        );

        JavaModLoader.registerDiscovered(
                discovered,
                duplicates,
                "duplicate-mod",
                "second"
        );

        assertFalse(
                discovered.containsKey(
                        "duplicate-mod"
                )
        );

        assertEquals(
                "working",
                discovered.get(
                        "working-mod"
                )
        );

        assertTrue(
                duplicates.contains(
                        "duplicate-mod"
                )
        );

        assertFalse(
                duplicates.contains(
                        "working-mod"
                )
        );
    }
}