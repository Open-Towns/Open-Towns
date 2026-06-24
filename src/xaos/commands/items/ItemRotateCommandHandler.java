package xaos.commands.items;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.tiles.entities.items.Item;

public final class ItemRotateCommandHandler implements CommandHandler {

    @Override
    public void execute(CommandContext context) {
        Item item = Item.getItemByID(
                Integer.parseInt(context.getParameter())
        );

        if (item == null) {
            return;
        }

        int facing = Integer.parseInt(context.getParameter2());
        item.setFacing(facing);
    }
}