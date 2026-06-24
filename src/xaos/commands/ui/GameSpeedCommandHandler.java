package xaos.commands.ui;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.main.Game;

import xaos.panels.MessagesPanel;
import xaos.campaign.TutorialTrigger;
import xaos.utils.ColorGL;
import xaos.utils.Messages;
import xaos.main.World;

public final class GameSpeedCommandHandler implements CommandHandler {

    public static final int INCREASE = 0;
    public static final int DECREASE = 1;

    private final int actionType;

    public GameSpeedCommandHandler(int actionType) {
        this.actionType = actionType;
    }

    @Override
    public void execute(CommandContext context) {
        if (actionType == INCREASE) {
            World.addTurnsPerSecond();
            addSpeedMessage();

            Game.updateTutorialFlow(
                    TutorialTrigger.TYPE_INT_ICONHIT,
                    TutorialTrigger.ICON_INT_SPEEDUP,
                    null
            );
            return;
        }

        World.removeTurnsPerSecond();
        addSpeedMessage();

        Game.updateTutorialFlow(
                TutorialTrigger.TYPE_INT_ICONHIT,
                TutorialTrigger.ICON_INT_SPEEDDOWN,
                null
        );
    }

    private void addSpeedMessage() {
        MessagesPanel.addMessage(
                MessagesPanel.TYPE_SYSTEM,
                Messages.getString("Game.6") + World.SPEED,
                ColorGL.YELLOW
        );
    }
}