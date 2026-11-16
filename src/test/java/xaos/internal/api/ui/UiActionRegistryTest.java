package xaos.internal.api.ui;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import xaos.api.ui.UiActionRegistration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UiActionRegistryTest {

    @AfterEach
    void tearDown() {

        /*
         * Remove any actions created by a test.
         */
        for (var action : UiActionRegistry.getActions()) {
            UiActionRegistry.unregister(
                    action.id()
            );
        }
    }

    @Test
    void registrationBecomesActive() {

        UiActionRegistration registration =
                UiActionRegistry.register(
                        "test:action",
                        "Test Action",
                        () -> {
                        }
                );

        assertTrue(
                registration.isActive()
        );

        assertTrue(
                UiActionRegistry.hasActions()
        );

        assertEquals(
                1,
                UiActionRegistry.getActions()
                        .size()
        );
    }

    @Test
    void unregisterRemovesAction() {

        UiActionRegistration registration =
                UiActionRegistry.register(
                        "test:action",
                        "Test Action",
                        () -> {
                        }
                );

        registration.unregister();

        assertFalse(
                registration.isActive()
        );

        assertFalse(
                UiActionRegistry.hasActions()
        );
    }

    @Test
    void closeUnregistersAction() {

        UiActionRegistration registration =
                UiActionRegistry.register(
                        "test:action",
                        "Test Action",
                        () -> {
                        }
                );

        registration.close();

        assertFalse(
                registration.isActive()
        );

        assertFalse(
                UiActionRegistry.hasActions()
        );
    }

    @Test
    void actionCanBeExecuted() {

        AtomicInteger executions =
                new AtomicInteger();

        UiActionRegistry.register(
                "test:action",
                "Test Action",
                executions::incrementAndGet
        );

        assertTrue(
                UiActionRegistry.execute(
                        "test:action"
                )
        );

        assertEquals(
                1,
                executions.get()
        );
    }

    @Test
    void unknownActionCannotBeExecuted() {

        assertFalse(
                UiActionRegistry.execute(
                        "test:missing"
                )
        );
    }

    @Test
    void registeringSameIdReplacesPreviousAction() {

        AtomicInteger firstExecutions =
                new AtomicInteger();

        AtomicInteger secondExecutions =
                new AtomicInteger();

        UiActionRegistration first =
                UiActionRegistry.register(
                        "test:action",
                        "First",
                        firstExecutions::incrementAndGet
                );

        UiActionRegistration second =
                UiActionRegistry.register(
                        "test:action",
                        "Second",
                        secondExecutions::incrementAndGet
                );

        assertFalse(
                first.isActive()
        );

        assertTrue(
                second.isActive()
        );

        assertEquals(
                1,
                UiActionRegistry.getActions()
                        .size()
        );

        UiActionRegistry.execute(
                "test:action"
        );

        assertEquals(
                0,
                firstExecutions.get()
        );

        assertEquals(
                1,
                secondExecutions.get()
        );
    }

    @Test
    void oldRegistrationCannotRemoveReplacement() {

        UiActionRegistration first =
                UiActionRegistry.register(
                        "test:action",
                        "First",
                        () -> {
                        }
                );

        UiActionRegistration second =
                UiActionRegistry.register(
                        "test:action",
                        "Second",
                        () -> {
                        }
                );

        /*
        
         * Cleanup of the old registration must not delete
         * the newer action using the same ID.
         */
        first.unregister();

        assertTrue(
                second.isActive()
        );

        assertTrue(
                UiActionRegistry.execute(
                        "test:action"
                )
        );

        assertEquals(
                "Second",
                UiActionRegistry.getActions()
                        .get(0)
                        .label()
        );
    }

    @Test
    void unregisterByIdDeactivatesRegistration() {

        UiActionRegistration registration =
                UiActionRegistry.register(
                        "test:action",
                        "Test Action",
                        () -> {
                        }
                );

        UiActionRegistry.unregister(
                "test:action"
        );

        assertFalse(
                registration.isActive()
        );

        assertFalse(
                UiActionRegistry.hasActions()
        );
    }
}