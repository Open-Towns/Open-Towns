package xaos.commands.options;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.main.Game;
import xaos.utils.Utils;

public final class SiegeDifficultyCommandHandler implements CommandHandler {

    @Override
    public void execute(CommandContext context) {
        Game.setSiegeDifficulty(Game.getSiegeDifficulty() + 1);

        if (Game.getSiegeDifficulty() > Game.SIEGE_DIFFICULTY_INSANE) {
            Game.setSiegeDifficulty(Game.SIEGE_DIFFICULTY_OFF);
        }

        Utils.saveOptions();
    }
}