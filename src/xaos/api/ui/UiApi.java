package xaos.api.ui;

import java.util.List;

/**
 * Public API for interacting with the Open Towns user interface.
 *
 * Mods should use this API rather than accessing internal panel
 * and menu classes directly.
 */
public interface UiApi {

        /**
         * Displays a message in the game's message system.
         */
        void showMessage(
                        UiMessageType type,
                        String message);

        /**
         * Convenience method for displaying a system message.
         */
        default void showMessage(String message) {
                showMessage(
                                UiMessageType.SYSTEM,
                                message);
        }

        /**
         * Displays a centered information dialog.
         *
         * The dialog contains a title, zero or more lines of text,
         * and a close button.
         */
        void showInfoDialog(
                        String title,
                        List<String> lines);

        /**
         * Registers an action that can be exposed through the game UI.
         *
         * The returned registration should normally be tracked using
         * ModContext.worldResource() or ModContext.resource().
         */
        UiActionRegistration registerGameAction(
                        String id,
                        String label,
                        Runnable action);

        void unregisterGameAction(
                        String id);
}