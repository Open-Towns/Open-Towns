package xaos.commands.mainmenu;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.main.Game;
import xaos.panels.MainMenuPanel;

public final class NewGameSetSaveNameCommandHandler implements CommandHandler {

    private final boolean useBuryTemporary;

    public NewGameSetSaveNameCommandHandler(boolean useBuryTemporary) {
        this.useBuryTemporary = useBuryTemporary;
    }

    @Override
    public void execute(CommandContext context) {
        MainMenuPanel.useBuryTemporary = useBuryTemporary;

        if (context.getDirectPoint() != null) {
            Game.setServerToUse(context.getDirectPoint().x);
        } else {
            Game.setServerToUse(-1);
        }

        Game.getPanelMainMenu().setSettingSavegameName(
                true,
                context.getParameter(),
                context.getParameter2()
        );
    }
}