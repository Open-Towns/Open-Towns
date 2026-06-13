package xaos.panels.menus.mainmenu;

import java.io.File;

import xaos.panels.menus.MenuDefinition;
import xaos.panels.menus.MenuManager;
import xaos.panels.menus.SmartMenu;

public class MainMenuOptions {

        public static SmartMenu createMenu(SmartMenu mainMenu) {

                MenuManager menuManager = new MenuManager();
                menuManager.loadMenus(new File("data/menus/main"));

                MenuDefinition optionsMenu = menuManager.getMenu("main.options.root");

                return menuManager.buildSmartMenu(optionsMenu, mainMenu);

        }
}
