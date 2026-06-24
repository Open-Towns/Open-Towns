package xaos.commands.ui;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.main.Game;
import xaos.TownsProperties;
import xaos.campaign.TutorialTrigger;
import xaos.utils.Point3D;
import xaos.main.World;

public final class LevelDownCommandHandler implements CommandHandler {

    @Override
    public void execute(CommandContext context) {
        Point3D view = Game.getWorld().getView();

        if (!canMoveDown(view)) {
            return;
        }

        Game.getWorld().setView(view.x, view.y, view.z + 1);

        Game.updateTutorialFlow(
                TutorialTrigger.TYPE_INT_LAYERUPDOWN,
                TutorialTrigger.LAYER_DOWN,
                null
        );

        Game.updateTutorialFlow(
                TutorialTrigger.TYPE_INT_ICONHIT,
                TutorialTrigger.ICON_INT_LEVELDOWN,
                null
        );
    }

    private boolean canMoveDown(Point3D view) {
        if (TownsProperties.DEBUG_MODE) {
            return view.z < World.MAP_DEPTH - 1;
        }

        return view.z < Game.getWorld().getNumFloorsDiscovered() - 1
                && view.z < World.MAP_DEPTH - 1;
    }
}