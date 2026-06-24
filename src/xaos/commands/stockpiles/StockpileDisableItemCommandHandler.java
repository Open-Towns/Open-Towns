package xaos.commands.stockpiles;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.main.Game;
import xaos.stockpiles.Stockpile;
import xaos.main.World;

public final class StockpileDisableItemCommandHandler implements CommandHandler {

    @Override
    public void execute(CommandContext context) {
        if (context.getDirectPoint() == null) {
            return;
        }

        Stockpile stockpile = Stockpile.getStockpile(
                context.getDirectPoint().toPoint3DShort()
        );

        if (stockpile == null) {
            return;
        }

        stockpile.getType().removeElement(context.getParameter());
        markStockpileItemsForHauling(stockpile);
    }

    private void markStockpileItemsForHauling(Stockpile stockpile) {
        for (int i = 0; i < stockpile.getPoints().size(); i++) {
            Game.getWorld().addItemToBeHauled(
                    World.getCell(stockpile.getPoints().get(i)).getItem()
            );
        }
    }
}