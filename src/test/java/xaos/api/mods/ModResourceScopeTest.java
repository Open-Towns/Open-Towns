package xaos.api.mods;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModResourceScopeTest {

    @Test
    void trackReturnsSameResource() {

        ModResourceScope scope =
                new ModResourceScope();

        TestResource resource =
                new TestResource(
                        "test",
                        new ArrayList<>(),
                        false
                );

        assertSame(
                resource,
                scope.track(resource)
        );

        assertEquals(
                1,
                scope.size()
        );
    }

    @Test
    void trackRejectsNullResource() {

        ModResourceScope scope =
                new ModResourceScope();

        assertThrows(
                IllegalArgumentException.class,
                () -> scope.track(null)
        );
    }

    @Test
    void clearClosesResourcesInReverseOrder() {

        ModResourceScope scope =
                new ModResourceScope();

        List<String> closed =
                new ArrayList<>();

        scope.track(
                new TestResource(
                        "first",
                        closed,
                        false
                )
        );

        scope.track(
                new TestResource(
                        "second",
                        closed,
                        false
                )
        );

        scope.track(
                new TestResource(
                        "third",
                        closed,
                        false
                )
        );

        scope.clear();

        assertEquals(
                List.of(
                        "third",
                        "second",
                        "first"
                ),
                closed
        );

        assertEquals(
                0,
                scope.size()
        );
    }

    @Test
    void brokenResourceDoesNotPreventOtherCleanup() {

        ModResourceScope scope =
                new ModResourceScope();

        List<String> closed =
                new ArrayList<>();

        scope.track(
                new TestResource(
                        "first",
                        closed,
                        false
                )
        );

        scope.track(
                new TestResource(
                        "broken",
                        closed,
                        true
                )
        );

        scope.track(
                new TestResource(
                        "last",
                        closed,
                        false
                )
        );

        scope.clear();

        /*
         * The broken resource is still attempted, and cleanup
         * continues afterwards.
         */
        assertEquals(
                List.of(
                        "last",
                        "broken",
                        "first"
                ),
                closed
        );

        assertEquals(
                0,
                scope.size()
        );
    }

    @Test
    void clearingTwiceDoesNotCloseResourcesTwice() {

        ModResourceScope scope =
                new ModResourceScope();

        List<String> closed =
                new ArrayList<>();

        scope.track(
                new TestResource(
                        "test",
                        closed,
                        false
                )
        );

        scope.clear();
        scope.clear();

        assertEquals(
                List.of("test"),
                closed
        );
    }

    private static final class TestResource
            implements ModResource {

        private final String name;
        private final List<String> closed;
        private final boolean throwOnClose;

        private TestResource(
                String name,
                List<String> closed,
                boolean throwOnClose
        ) {
            this.name = name;
            this.closed = closed;
            this.throwOnClose = throwOnClose;
        }

        @Override
        public void close() {

            closed.add(name);

            if (throwOnClose) {
                throw new RuntimeException(
                        "Test cleanup failure"
                );
            }
        }
    }
}