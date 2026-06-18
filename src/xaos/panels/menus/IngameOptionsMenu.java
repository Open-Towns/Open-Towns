package xaos.panels.menus;

import java.io.File;

public class IngameOptionsMenu {

        public static SmartMenu createOptionsMenu(SmartMenu mainMenu) {

                MenuManager menuManager = new MenuManager();
                menuManager.loadMenus(new File("data/menus/game"));

                MenuDefinition optionsMenu = menuManager.getMenu("game.root");

                return menuManager.buildSmartMenu(optionsMenu, mainMenu);

        }
}
