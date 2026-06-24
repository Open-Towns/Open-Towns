package xaos.commands.ui;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.main.Game;
import xaos.campaign.TutorialTrigger;
import xaos.utils.Point3D;

public final class LevelUpCommandHandler implements CommandHandler {

    @Override
    public void execute(CommandContext context) {
        Point3D view = Game.getWorld().getView();

        if (view.z <= 0) {
            return;
        }

        Game.getWorld().setView(view.x, view.y, view.z - 1);

        Game.updateTutorialFlow(
                TutorialTrigger.TYPE_INT_LAYERUPDOWN,
                TutorialTrigger.LAYER_UP,
                null
        );

        Game.updateTutorialFlow(
                TutorialTrigger.TYPE_INT_ICONHIT,
                TutorialTrigger.ICON_INT_LEVELUP,
                null
        );
    }
}