package xaos.api.mods;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import xaos.api.GameApiBootstrap;
import xaos.api.SdkVersion;
import xaos.internal.api.mods.ModSaveDataRegistry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ModContextResourceLifecycleTest {

    @AfterEach
    void tearDown() {
        ModSaveDataRegistry.clear();
    }

    @Test
    void worldResourceReturnsSameResource() {

        ModContext context =
                createContext();

        TestResource resource =
                new TestResource(
                        "world",
                        new ArrayList<>()
                );

        assertSame(
                resource,
                context.worldResource(
                        resource
                )
        );
    }

    @Test
    void modResourceReturnsSameResource() {

        ModContext context =
                createContext();

        TestResource resource =
                new TestResource(
                        "mod",
                        new ArrayList<>()
                );

        assertSame(
                resource,
                context.resource(
                        resource
                )
        );
    }

    @Test
    void worldUnloadOnlyClearsWorldResources() {

        ModContext context =
                createContext();

        List<String> closed =
                new ArrayList<>();

        context.worldResource(
                new TestResource(
                        "world",
                        closed
                )
        );

        context.resource(
                new TestResource(
                        "mod",
                        closed
                )
        );

        context.clearWorldResources();

        assertEquals(
                List.of("world"),
                closed
        );
    }

    @Test
    void fullUnloadClearsBothResourceScopes() {

        ModContext context =
                createContext();

        List<String> closed =
                new ArrayList<>();

        context.worldResource(
                new TestResource(
                        "world",
                        closed
                )
        );

        context.resource(
                new TestResource(
                        "mod",
                        closed
                )
        );

        context.clearAllResources();

        assertEquals(
                List.of(
                        "world",
                        "mod"
                ),
                closed
        );
    }

    @Test
    void worldResourcesCanBeRegisteredAgainAfterUnload() {

        ModContext context =
                createContext();

        List<String> closed =
                new ArrayList<>();

        context.worldResource(
                new TestResource(
                        "first-world",
                        closed
                )
        );

        context.clearWorldResources();

        context.worldResource(
                new TestResource(
                        "second-world",
                        closed
                )
        );

        context.clearWorldResources();

        assertEquals(
                List.of(
                        "first-world",
                        "second-world"
                ),
                closed
        );
    }

    @Test
    void fullUnloadDoesNotCloseAlreadyClearedWorldResourceTwice() {

        ModContext context =
                createContext();

        List<String> closed =
                new ArrayList<>();

        context.worldResource(
                new TestResource(
                        "world",
                        closed
                )
        );

        context.clearWorldResources();
        context.clearAllResources();

        assertEquals(
                List.of("world"),
                closed
        );
    }

    private static ModContext createContext() {

        ModMetadata metadata =
                new ModMetadata(
                        "test-mod",
                        "Test Mod",
                        "1.0.0",
                        "Test",
                        SdkVersion.API_VERSION,
                        List.of()
                );

        return new ModContext(
                metadata,
                GameApiBootstrap.get(),
                new TestLogger(),
                ModSaveDataRegistry.forMod(
                        "test-mod"
                )
        );
    }

    private static final class TestResource
            implements ModResource {

        private final String name;
        private final List<String> closed;

        private TestResource(
                String name,
                List<String> closed
        ) {
            this.name = name;
            this.closed = closed;
        }

        @Override
        public void close() {
            closed.add(name);
        }
    }

    private static final class TestLogger
            implements ModLogger {

        @Override
        public void info(
                String message
        ) {
        }

        @Override
        public void error(
                String message
        ) {
        }

        @Override
        public void error(
                String message,
                Throwable throwable
        ) {
        }
    }
}