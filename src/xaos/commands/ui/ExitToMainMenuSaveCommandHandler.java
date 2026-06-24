package xaos.commands.ui;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.panels.CommandPanel;

public final class ExitToMainMenuSaveCommandHandler implements CommandHandler {

    @Override
    public void execute(CommandContext context) {

        CommandPanel.executeCommand(CommandPanel.COMMAND_SAVE, null, null, null, null, 0);
        CommandPanel.executeCommand(CommandPanel.COMMAND_EXIT_TO_MAIN_MENU_NOSAVE, null, null, null, null, 0);
    }
}
