package xaos.commands.ui;

import java.io.File;

import xaos.campaign.TutorialTrigger;
import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.main.Game;
import xaos.panels.menus.ContextMenu;
import xaos.panels.menus.MenuDefinition;
import xaos.panels.menus.MenuManager;
import xaos.panels.menus.SmartMenu;
import xaos.utils.UtilsGL;

public final class ExitToMainMenuCommandHandler implements CommandHandler {
     @Override
    public void execute(CommandContext context) {
        if (Game.getCurrentState() == Game.STATE_CREATING_TASK) {
                    Game.deleteCurrentTask();
                }
                ContextMenu menuExit = createEscapeMenu();
                menuExit.setX(UtilsGL.getWidth() / 2 - menuExit.getWidth() / 2);
                menuExit.setY(UtilsGL.getHeight() / 2 - menuExit.getHeight() / 2);
                Game.setContextMenu(menuExit);

                // Tutorial flow
                Game.updateTutorialFlow(TutorialTrigger.TYPE_INT_ICONHIT, TutorialTrigger.ICON_INT_SETTINGS, null);
    }

     private static ContextMenu createEscapeMenu() {

        ContextMenu menuExit = new ContextMenu();

        MenuManager menuManager = new MenuManager();
        menuManager.loadMenus(new File("data/menus/game"));

        MenuDefinition optionsMenu = menuManager.getMenu("game.root");
        SmartMenu menu = menuManager.buildSmartMenu(optionsMenu, null);
        menu.setTrasparency(false);
        menuExit.setSmartMenu(menu);
        return menuExit;

    }

}
