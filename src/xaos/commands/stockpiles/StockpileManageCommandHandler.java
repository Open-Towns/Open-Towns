package xaos.commands.stockpiles;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.panels.UI.UIPanelState;

public final class StockpileManageCommandHandler implements CommandHandler {

    @Override
    public void execute(CommandContext context) {
        int pileId = Integer.parseInt(context.getParameter());

        UIPanelState.setPilePanelActive(pileId, false);
    }
}