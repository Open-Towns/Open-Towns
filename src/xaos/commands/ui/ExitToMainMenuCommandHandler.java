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
import xaos.internal.api.ui.UiActionRegistry;
import xaos.internal.api.ui.UiActionRegistry.RegisteredAction;
import xaos.panels.CommandPanel;

public final class ExitToMainMenuCommandHandler implements CommandHandler {

    @Override
    public void execute(CommandContext context) {
        if (Game.getCurrentState() == Game.STATE_CREATING_TASK) {
            Game.deleteCurrentTask();
        }

        ContextMenu menuExit = createEscapeMenu();
        menuExit.setCentered(true);
        menuExit.resize();

        menuExit.setX(UtilsGL.getWidth() / 2 - menuExit.getWidth() / 2);
        menuExit.setY(UtilsGL.getHeight() / 2 - menuExit.getHeight() / 2);

        menuExit.resize();

        Game.setContextMenu(menuExit);

        Game.updateTutorialFlow(
                TutorialTrigger.TYPE_INT_ICONHIT,
                TutorialTrigger.ICON_INT_SETTINGS,
                null);
    }

    private static ContextMenu createEscapeMenu() {
        ContextMenu menuExit = new ContextMenu();

        MenuManager menuManager = new MenuManager();
        menuManager.loadMenus(new File("data/menus/game"));

        MenuDefinition optionsMenu = menuManager.getMenu("game.root");
        SmartMenu menu = menuManager.buildSmartMenu(optionsMenu, null);
        menu.setTrasparency(false);
        appendModActions(menu);
        menuExit.setSmartMenu(menu);
        return menuExit;
    }

    private static void appendModActions(
            SmartMenu rootMenu) {
        if (!UiActionRegistry.hasActions()) {
            return;
        }

        SmartMenu modsMenu = new SmartMenu(
                SmartMenu.TYPE_MENU,
                "Mods",
                rootMenu,
                null,
                null);

        for (RegisteredAction action : UiActionRegistry.getActions()) {

            modsMenu.addItem(
                    new SmartMenu(
                            SmartMenu.TYPE_ITEM,
                            action.label(),
                            modsMenu,
                            CommandPanel.COMMAND_MOD_ACTION,
                            action.id()));
        }

        rootMenu.addItem(
                new SmartMenu(
                        SmartMenu.TYPE_SPACER,
                        null,
                        rootMenu,
                        null,
                        null));

        rootMenu.addItem(modsMenu);
    }
}
