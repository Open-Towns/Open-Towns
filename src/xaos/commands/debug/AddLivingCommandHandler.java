package xaos.commands.debug;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.tiles.entities.living.LivingEntityManager;
import xaos.tiles.entities.living.LivingEntityManagerItem;
import xaos.TownsProperties;
import xaos.main.World;
import xaos.tiles.Cell;


public final class AddLivingCommandHandler implements CommandHandler {

    @Override
    public void execute(CommandContext context) {
        if (!TownsProperties.TEST_COMMANDS) {
            return;
        }

        if (context.getDirectPoint() == null) {
            return;
        }

        Cell cell = World.getCell(context.getDirectPoint());

        if (cell == null) {
            return;
        }

        LivingEntityManagerItem livingDefinition = LivingEntityManager.getItem(
                context.getParameter()
        );

        if (livingDefinition == null) {
            return;
        }

        World.addNewLiving(
                context.getParameter(),
                livingDefinition.getType(),
                cell.isDiscovered(),
                context.getDirectPoint().x,
                context.getDirectPoint().y,
                context.getDirectPoint().z,
                true
        );
    }
}