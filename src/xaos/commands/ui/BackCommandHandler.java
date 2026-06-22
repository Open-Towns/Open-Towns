

package xaos.commands.ui;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.panels.CommandPanel;
import xaos.panels.menus.SmartMenu;

public final class BackCommandHandler implements CommandHandler {

    @Override
    public void execute(CommandContext context) {
        SmartMenu currentMenu = CommandPanel.getCurrentMenu();

        if (currentMenu != null && currentMenu.getParent() != null) {
            CommandPanel.setCurrentMenu(currentMenu.getParent());
            return;
        }

        CommandPanel.executeCommand(
                CommandPanel.COMMAND_EXIT_TO_MAIN_MENU,
                null,
                null,
                null,
                null,
                0
        );
    }
}