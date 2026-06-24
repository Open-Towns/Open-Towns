package xaos.commands.options;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.main.Game;

public final class ChangeHotkeyCommandHandler implements CommandHandler {

    @Override
    public void execute(CommandContext context) {
        Game.getPanelMainMenu().setSettingHotkey(
                true,
                Integer.parseInt(context.getParameter())
        );
    }
}
