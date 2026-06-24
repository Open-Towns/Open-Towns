package xaos.commands.items;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.tiles.entities.items.*;

import xaos.tiles.Cell;
import xaos.main.World;

public final class UnlockItemCommandHandler implements CommandHandler {

    @Override
    public void execute(CommandContext context) {
        Item item = Item.getItemByID(
                Integer.parseInt(context.getParameter())
        );

        if (item == null) {
            return;
        }

        if (!item.isLocked()) {
            return;
        }

        if (item.getCoordinates() == null || item.getCoordinates().x == -1) {
            return;
        }

        Cell cell = World.getCell(item.getCoordinates());
        ItemManagerItem itemDefinition = ItemManager.getItem(item.getIniHeader());

        cell.setEntity(null);
        item.setOperative(itemDefinition.isAlwaysOperative());
        item.setLocked(false);
        cell.setEntity(item);

        if (itemDefinition.isWall() || itemDefinition.isZoneMergerUp()) {
            Cell.setAllZoneIDs();
        }
    }
}