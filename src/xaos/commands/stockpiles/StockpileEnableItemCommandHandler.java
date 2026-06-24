package xaos.commands.stockpiles;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.stockpiles.Stockpile;
import xaos.data.Type;
import xaos.data.Types;

public final class StockpileEnableItemCommandHandler implements CommandHandler {

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

        if (stockpile.getType().contains(context.getParameter())) {
            return;
        }

        Type type = Types.getType(stockpile.getType().getID());

        if (type == null) {
            return;
        }

        String name = type.getElementName(context.getParameter());

        if (name != null) {
            stockpile.getType().addElement(
                    context.getParameter(),
                    name
            );
        }
    }
}