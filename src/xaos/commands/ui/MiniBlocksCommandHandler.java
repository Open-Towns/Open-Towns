package xaos.commands.ui;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.panels.MainPanel;

public final class MiniBlocksCommandHandler implements CommandHandler {

    @Override
    public void execute(CommandContext context) {
        MainPanel.toggleMiniBlocks();
    }
}