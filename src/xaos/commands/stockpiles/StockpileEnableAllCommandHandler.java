package xaos.commands.stockpiles;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.stockpiles.Stockpile;

public final class StockpileEnableAllCommandHandler implements CommandHandler {

    @Override
    public void execute(CommandContext context) {
        int pileId = Integer.parseInt(context.getParameter());

        Stockpile.enableAll(
                pileId,
                context.getParameter2()
        );
    }
}