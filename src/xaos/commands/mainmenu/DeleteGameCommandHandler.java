package xaos.commands.mainmenu;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.main.Game;
import xaos.utils.Utils;

public final class DeleteGameCommandHandler implements CommandHandler {

    @Override
    public void execute(CommandContext context) {
        Utils.deleteSavegame(context.getParameter());
        Game.getPanelMainMenu().createMenu();
    }
}