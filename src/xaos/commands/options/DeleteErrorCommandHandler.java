package xaos.commands.options;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.main.Game;

public final class DeleteErrorCommandHandler implements CommandHandler {

    @Override
    public void execute(CommandContext context) {
        Game.getPanelMainMenu().setErrorToShow(null);
        Game.getPanelMainMenu().createMenu();
    }
}
