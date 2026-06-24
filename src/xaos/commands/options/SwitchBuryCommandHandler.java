package xaos.commands.options;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.main.Game;
import xaos.utils.Utils;

public final class SwitchBuryCommandHandler implements CommandHandler {

    @Override
    public void execute(CommandContext context) {
        Game.setAllowBury(!Game.isAllowBury());
        Utils.saveOptions();

        Game.exitToMainMenu();
        Game.getPanelMainMenu().createMenu();
    }
}