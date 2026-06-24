package xaos.commands.debug;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.tiles.entities.items.*;
import xaos.TownsProperties;
import xaos.main.World;

public final class AddItemCommandHandler implements CommandHandler {

    @Override
    public void execute(CommandContext context) {
        if (!TownsProperties.TEST_COMMANDS) {
            return;
        }

        if (context.getDirectPoint() == null) {
            return;
        }

        if (World.getCell(context.getDirectPoint()).hasItem()) {
            return;
        }

        ItemManagerItem itemDefinition = ItemManager.getItem(context.getParameter());

        if (itemDefinition == null) {
            return;
        }

        Item item = Item.createItem(itemDefinition);

        if (item == null) {
            return;
        }

        item.init(
                context.getDirectPoint().x,
                context.getDirectPoint().y,
                context.getDirectPoint().z);

        item.setOperative(true);
        item.setLocked(itemDefinition.isLocked());

        World.getCell(context.getDirectPoint()).setEntity(item);
    }
}