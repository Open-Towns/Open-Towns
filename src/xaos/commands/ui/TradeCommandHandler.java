package xaos.commands.ui;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.panels.UI.UIPanelState;

public final class TradeCommandHandler implements CommandHandler {

    @Override
    public void execute(CommandContext context) {
        UIPanelState.setTradePanelActive(true);
    }
}