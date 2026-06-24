package xaos.commands.options;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.main.Game;
import xaos.utils.Utils;

public final class ServerRemoveCommandHandler implements CommandHandler {

    @Override
    public void execute(CommandContext context) {
        Game.removeServer(context.getParameter());
        Utils.saveOptions();

        Game.exitToMainMenu();
        Game.getPanelMainMenu().createMenu();
    }
}