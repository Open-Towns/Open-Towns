package xaos.commands.ui;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.main.Game;
import xaos.campaign.TutorialTrigger;

public final class PauseCommandHandler implements CommandHandler {

    @Override
    public void execute(CommandContext context) {
        Game.togglePause(true);

        Game.updateTutorialFlow(
                TutorialTrigger.TYPE_INT_ICONHIT,
                TutorialTrigger.ICON_INT_PAUSE,
                null
        );
    }
}