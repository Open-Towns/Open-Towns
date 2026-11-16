package xaos.api.ui;

import java.util.List;

/**
 * Public API for interacting with the Open Towns user interface.
 *
 * Mods should use this API rather than accessing internal panel
 * and menu classes directly.
 */
public interface UiApi {

    void showMessage(
            UiMessageType type,
            String message
    );

    default void showMessage(
            String message
    ) {
        showMessage(
                UiMessageType.SYSTEM,
                message
        );
    }

    void showInfoDialog(
            String title,
            List<String> lines
    );

    UiActionRegistration registerGameAction(
            String id,
            String label,
            Runnable action
    );
}