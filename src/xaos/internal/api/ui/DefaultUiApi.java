package xaos.internal.api.ui;

import java.util.List;

import xaos.api.ui.UiActionRegistration;
import xaos.api.ui.UiApi;
import xaos.api.ui.UiMessageType;
import xaos.main.Game;
import xaos.panels.CommandPanel;
import xaos.panels.MessagesPanel;
import xaos.panels.UI.UiDispatcher;
import xaos.panels.menus.ContextMenu;
import xaos.panels.menus.SmartMenu;
import xaos.utils.UtilsGL;

/**
 * Default implementation of the public UI API using Towns'
 * existing user-interface systems.
 */
public final class DefaultUiApi implements UiApi {

    @Override
    public void showMessage(
            UiMessageType type,
            String message) {

        if (message == null || message.isBlank()) {
            return;
        }

        MessagesPanel.addMessage(
                mapMessageType(type),
                message);
    }

    private static int mapMessageType(
            UiMessageType type) {

        if (type == null) {
            return MessagesPanel.TYPE_SYSTEM;
        }

        return switch (type) {
            case SYSTEM ->
                MessagesPanel.TYPE_SYSTEM;

            case ANNOUNCEMENT ->
                MessagesPanel.TYPE_ANNOUNCEMENT;

            case COMBAT ->
                MessagesPanel.TYPE_COMBAT;

            case HERO ->
                MessagesPanel.TYPE_HEROES;
        };
    }

    @Override
    public void showInfoDialog(
            String title,
            List<String> lines) {

        if ((title == null || title.isBlank())
                && (lines == null || lines.isEmpty())) {
            return;
        }

        /*
         * Copy the list now so the caller cannot modify it
         * before the queued UI task runs.
         */
        List<String> safeLines = lines == null
                ? List.of()
                : List.copyOf(lines);

        UiDispatcher.invokeLater(
                () -> showInfoDialogNow(
                        title,
                        safeLines));
    }

    @Override
    public UiActionRegistration registerGameAction(
            String id,
            String label,
            Runnable action) {

        return UiActionRegistry.register(
                id,
                label,
                action);
    }

    

    private static void showInfoDialogNow(
            String title,
            List<String> lines) {

        SmartMenu contents = new SmartMenu();

        if (title != null && !title.isBlank()) {
            contents.addItem(
                    new SmartMenu(
                            SmartMenu.TYPE_HEADING,
                            title,
                            null,
                            null,
                            null));
        }

        for (String line : lines) {
            contents.addItem(
                    new SmartMenu(
                            SmartMenu.TYPE_TEXT,
                            line == null ? "" : line,
                            null,
                            null,
                            null));
        }

        contents.addItem(
                new SmartMenu(
                        SmartMenu.TYPE_SPACER,
                        null,
                        null,
                        null,
                        null));

        contents.addItem(
                new SmartMenu(
                        SmartMenu.TYPE_ITEM,
                        "Close",
                        null,
                        CommandPanel.COMMAND_CLOSE_CONTEXT,
                        null));

        ContextMenu dialog = new ContextMenu();

        /*
         * setSmartMenu() calculates the final width and height.
         */
        dialog.setSmartMenu(contents);

        /*
         * Centre after sizing.
         */
        dialog.setX(
                UtilsGL.getWidth() / 2
                        - dialog.getWidth() / 2);

        dialog.setY(
                UtilsGL.getHeight() / 2
                        - dialog.getHeight() / 2);

        Game.setContextMenu(dialog);
    }
}