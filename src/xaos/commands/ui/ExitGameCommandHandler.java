package xaos.commands.ui;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.main.Game;

public final class ExitGameCommandHandler implements CommandHandler{
    @Override
    public void execute(CommandContext context) {
      Game.exit();
    }
}
