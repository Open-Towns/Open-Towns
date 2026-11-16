package xaos.internal.api.mods;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModDependencyResolverTest {

    @Test
    void unrelatedModsUseAlphabeticalOrder() {

        var result =
                ModDependencyResolver.resolve(
                        Map.of(
                                "zeta", List.of(),
                                "alpha", List.of(),
                                "beta", List.of()
                        )
                );

        assertEquals(
                List.of(
                        "alpha",
                        "beta",
                        "zeta"
                ),
                result.loadOrder()
        );
    }

    @Test
    void dependencyLoadsBeforeDependant() {

        var result =
                ModDependencyResolver.resolve(
                        Map.of(
                                "alpha",
                                List.of("zulu"),
                                "zulu",
                                List.of()
                        )
                );

        assertEquals(
                List.of(
                        "zulu",
                        "alpha"
                ),
                result.loadOrder()
        );
    }

    @Test
    void transitiveDependenciesAreOrderedCorrectly() {

        var result =
                ModDependencyResolver.resolve(
                        Map.of(
                                "addon",
                                List.of("library"),
                                "library",
                                List.of("core"),
                                "core",
                                List.of()
                        )
                );

        assertEquals(
                List.of(
                        "core",
                        "library",
                        "addon"
                ),
                result.loadOrder()
        );
    }

    @Test
    void missingDependencyFailsMod() {

        var result =
                ModDependencyResolver.resolve(
                        Map.of(
                                "addon",
                                List.of("missing")
                        )
                );

        assertTrue(
                result.loadOrder()
                        .isEmpty()
        );

        assertTrue(
                result.failures()
                        .containsKey("addon")
        );
    }

    @Test
    void circularDependencyFailsBothMods() {

        var result =
                ModDependencyResolver.resolve(
                        Map.of(
                                "mod-a",
                                List.of("mod-b"),
                                "mod-b",
                                List.of("mod-a")
                        )
                );

        assertTrue(
                result.loadOrder()
                        .isEmpty()
        );

        assertTrue(
                result.failures()
                        .containsKey("mod-a")
        );

        assertTrue(
                result.failures()
                        .containsKey("mod-b")
        );
    }

    @Test
    void dependantOfFailedModAlsoFails() {

        var result =
                ModDependencyResolver.resolve(
                        Map.of(
                                "addon",
                                List.of("library"),
                                "library",
                                List.of("missing")
                        )
                );

        assertTrue(
                result.failures()
                        .containsKey("library")
        );

        assertTrue(
                result.failures()
                        .containsKey("addon")
        );
    }

    @Test
    void unrelatedModStillLoadsWhenAnotherModFails() {

        var result =
                ModDependencyResolver.resolve(
                        Map.of(
                                "broken",
                                List.of("missing"),
                                "working",
                                List.of()
                        )
                );

        assertEquals(
                List.of("working"),
                result.loadOrder()
        );

        assertTrue(
                result.failures()
                        .containsKey("broken")
        );

        assertFalse(
                result.failures()
                        .containsKey("working")
        );
    }
}