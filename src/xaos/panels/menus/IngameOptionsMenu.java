package xaos.panels.menus;

import xaos.utils.Point3D;
import java.awt.Color;

import xaos.panels.CommandPanel;
import xaos.utils.Messages;

public class IngameOptionsMenu {
    private static final class MenuItemDefinition {
        final int type;
        final String messageKey;
        final String command;
        final boolean dynamic;
        final String parameter;
        final String parameter2;
        final Point3D directCoordinates;
        final boolean maintainOpen;
        final Color color;

        MenuItemDefinition(
                int type,
                String messageKey,
                String command,
                boolean dynamic,
                String parameter,
                String parameter2,
                Point3D directCoordinates,
                boolean maintainOpen,
                Color color) {
            this.type = type;
            this.messageKey = messageKey;
            this.command = command;
            this.dynamic = dynamic;
            this.parameter = parameter;
            this.parameter2 = parameter2;
            this.directCoordinates = directCoordinates;
            this.maintainOpen = maintainOpen;
            this.color = color;
        }
    }

    private static final MenuItemDefinition[] GRAPHICS_MENU_ITEMS = {
            // Options - Graphics - Fullscreen
            new MenuItemDefinition(
                    SmartMenu.TYPE_ITEM,
                    "MainMenuPanel.17",
                    CommandPanel.COMMAND_MM_TOGGLE_FULL_SCREEN,
                    true,
                    null,
                    null,
                    null,
                    true, null),

            // Options - Graphics - UI Scale
            new MenuItemDefinition(
                    SmartMenu.TYPE_ITEM,
                    "MainMenuPanel.83",
                    CommandPanel.COMMAND_MM_UI_SCALE,
                    false,
                    null,
                    null,
                    null,
                    true, null),

            // Options - Graphics - Tooltip Scale
            new MenuItemDefinition(
                    SmartMenu.TYPE_ITEM,
                    "MainMenuPanel.84",
                    CommandPanel.COMMAND_MM_TOOLTIP_SCALE,
                    false,
                    null,
                    null,
                    null,
                    true, null),

            // Options - Graphics - World Zoom
            new MenuItemDefinition(
                    SmartMenu.TYPE_ITEM,
                    "MainMenuPanel.85",
                    CommandPanel.COMMAND_MM_WORLD_ZOOM,
                    false,
                    null,
                    null,
                    null,
                    true, null)
    };

    private static SmartMenu createBackButton() {

        return new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.7"), null, //$NON-NLS-1$
                CommandPanel.COMMAND_BACK, null, null, null);
    }

    private static SmartMenu createGraphicsMenu(SmartMenu menuOptions) {
        SmartMenu menuOptionsGraphics = new SmartMenu(
                SmartMenu.TYPE_MENU,
                Messages.getString("MainMenuPanel.16"), //$NON-NLS-1$
                menuOptions,
                null,
                null,
                null,
                null);

        addMenuItems(menuOptionsGraphics, GRAPHICS_MENU_ITEMS);
        menuOptionsGraphics.addItem(createBackButton());
        return menuOptionsGraphics;
    }

    private static void addMenuItems(
            SmartMenu parentMenu,
            MenuItemDefinition[] menuItemDefinitions) {
        for (MenuItemDefinition menuItemDefinition : menuItemDefinitions) {
            SmartMenu menuItem = createMenuItem(parentMenu, menuItemDefinition);
            parentMenu.addItem(menuItem);
        }
    }

    private static SmartMenu createMenuItem(
            SmartMenu parentMenu,
            MenuItemDefinition menuItemDefinition) {

        String message = null;

        if (menuItemDefinition.messageKey != null) {
            message = Messages.getString(menuItemDefinition.messageKey);
        }

        SmartMenu menuItem;

        if (menuItemDefinition.color != null) {
            menuItem = new SmartMenu(
                    menuItemDefinition.type,
                    message,
                    parentMenu,
                    menuItemDefinition.command,
                    menuItemDefinition.parameter,
                    menuItemDefinition.parameter2,
                    menuItemDefinition.directCoordinates,
                    menuItemDefinition.color);
        } else {
            menuItem = new SmartMenu(
                    menuItemDefinition.type,
                    message,
                    parentMenu,
                    menuItemDefinition.command,
                    menuItemDefinition.parameter,
                    menuItemDefinition.parameter2,
                    menuItemDefinition.directCoordinates);
        }

        menuItem.setDynamic(menuItemDefinition.dynamic);
        menuItem.setMaintainOpen(menuItemDefinition.maintainOpen);

        return menuItem;
    }

    private static SmartMenu createSpacer() {
        return new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null);
    }

    private static final MenuItemDefinition[] AUDIO_MENU_ITEMS = {
            // Options - Audio - Music on/off
            new MenuItemDefinition(
                    SmartMenu.TYPE_ITEM,
                    "MainMenuPanel.5",
                    CommandPanel.COMMAND_MM_SWITCH_MUSIC,
                    true,
                    null,
                    null,
                    null,
                    true, null),

            // Options - Audio - Music volume
            new MenuItemDefinition(
                    SmartMenu.TYPE_ITEM,
                    "MainMenuPanel.56",
                    CommandPanel.COMMAND_MM_ADD_MUSIC_VOLUME,
                    true,
                    null,
                    null,
                    null,
                    true, null),

            // Spacer
            menuSpacerDefinition(),

            // Options - Audio - FX on/off
            new MenuItemDefinition(
                    SmartMenu.TYPE_ITEM,
                    "MainMenuPanel.6",
                    CommandPanel.COMMAND_MM_SWITCH_FX,
                    true,
                    null,
                    null,
                    null,
                    true, null),

            // Options - Audio - FX volume
            new MenuItemDefinition(
                    SmartMenu.TYPE_ITEM,
                    "MainMenuPanel.57",
                    CommandPanel.COMMAND_MM_ADD_FX_VOLUME,
                    true,
                    null,
                    null,
                    null,
                    true, null),

            // Spacer
            menuSpacerDefinition(),

    };

    private static SmartMenu createAudioMenu(SmartMenu menuOptions) {
        SmartMenu menuOptionsAudio = new SmartMenu(SmartMenu.TYPE_MENU, Messages.getString("MainMenuPanel.13"), //$NON-NLS-1$
                menuOptions, null, null, null, null);

        addMenuItems(menuOptionsAudio, AUDIO_MENU_ITEMS);
        menuOptionsAudio.addItem(createBackButton());

        return menuOptionsAudio;
    }

    private static final MenuItemDefinition[] GAME_MENU_ITEMS = {
            // Options - Game - Mouse scroll
            new MenuItemDefinition(
                    SmartMenu.TYPE_ITEM,
                    "MainMenuPanel.20",
                    CommandPanel.COMMAND_MM_SWITCH_MOUSE_SCROLL,
                    true,
                    null,
                    null,
                    null,
                    true, null),

            // Options - Game - Mouse scroll ears
            new MenuItemDefinition(
                    SmartMenu.TYPE_ITEM,
                    "MainMenuPanel.33",
                    CommandPanel.COMMAND_MM_SWITCH_MOUSE_SCROLL_EARS,
                    true,
                    null,
                    null,
                    null,
                    true, null),

            // Options - Game - 2D mouse cubes
            new MenuItemDefinition(
                    SmartMenu.TYPE_ITEM,
                    "MainMenuPanel.72",
                    CommandPanel.COMMAND_MM_SWITCH_MOUSE_2D_CUBES,
                    true,
                    null,
                    null,
                    null,
                    true, null),

            // Options - Game - Disable items
            new MenuItemDefinition(
                    SmartMenu.TYPE_ITEM,
                    "MainMenuPanel.27",
                    CommandPanel.COMMAND_MM_SWITCH_DISABLE_ITEMS,
                    true,
                    null,
                    null,
                    null,
                    true, null),

            // Options - Game - Disable gods
            new MenuItemDefinition(
                    SmartMenu.TYPE_ITEM,
                    "MainMenuPanel.77",
                    CommandPanel.COMMAND_MM_SWITCH_DISABLE_GODS,
                    true,
                    null,
                    null,
                    null,
                    true, null),

            // Options - Game - Pause when inactive
            new MenuItemDefinition(
                    SmartMenu.TYPE_ITEM,
                    "MainMenuPanel.28",
                    CommandPanel.COMMAND_MM_SWITCH_PAUSE,
                    true,
                    null,
                    null,
                    null,
                    true, null),

            // Options - Game - Autosave days
            new MenuItemDefinition(
                    SmartMenu.TYPE_ITEM,
                    "MainMenuPanel.31",
                    CommandPanel.COMMAND_MM_SWITCH_AUTOSAVE_DAYS,
                    true,
                    null,
                    null,
                    null,
                    true, null),

            // Options - Game - Sieges
            new MenuItemDefinition(
                    SmartMenu.TYPE_ITEM,
                    "MainMenuPanel.32",
                    CommandPanel.COMMAND_MM_SWITCH_SIEGES,
                    true,
                    null,
                    null,
                    null,
                    true, null),

            // Options - Game - Pause on siege
            new MenuItemDefinition(
                    SmartMenu.TYPE_ITEM,
                    "MainMenuPanel.36",
                    CommandPanel.COMMAND_MM_SWITCH_SIEGE_PAUSE,
                    true,
                    null,
                    null,
                    null,
                    true, null),

            // Options - Game - Pause on caravan
            new MenuItemDefinition(
                    SmartMenu.TYPE_ITEM,
                    "MainMenuPanel.66",
                    CommandPanel.COMMAND_MM_SWITCH_CARAVAN_PAUSE,
                    true,
                    null,
                    null,
                    null,
                    true, null),

            // Spacer
            menuSpacerDefinition(),
    };

    private final static MenuItemDefinition menuSpacerDefinition() {
        return new MenuItemDefinition(
                SmartMenu.TYPE_TEXT,
                null,
                null,
                false,
                null,
                null,
                null,
                false, null);
    }

    private static final MenuItemDefinition[] PERFORMANCE_MENU_ITEMS = {
            // Options - Performance - Info text
            new MenuItemDefinition(
                    SmartMenu.TYPE_TEXT,
                    "MainMenuPanel.59",
                    null,
                    false,
                    null,
                    null,
                    null,
                    false, Color.LIGHT_GRAY),

            // Options - Performance - Info text
            new MenuItemDefinition(
                    SmartMenu.TYPE_TEXT,
                    "MainMenuPanel.64",
                    null,
                    false,
                    null,
                    null,
                    null,
                    false, Color.LIGHT_GRAY),

            // Options - Performance - Info text
            new MenuItemDefinition(
                    SmartMenu.TYPE_TEXT,
                    "MainMenuPanel.65",
                    null,
                    false,
                    null,
                    null,
                    null,
                    false, Color.LIGHT_GRAY),

            // Options - Performance - Pathfinding level
            new MenuItemDefinition(
                    SmartMenu.TYPE_ITEM,
                    "MainMenuPanel.63",
                    CommandPanel.COMMAND_MM_SWITCH_PATHFINDING_LEVEL,
                    true,
                    null,
                    null,
                    null,
                    true, null),

            // Spacer
            menuSpacerDefinition()
    };

    private static SmartMenu createGameMenu(SmartMenu menuOptions) {
        SmartMenu menuOptionsGame = new SmartMenu(SmartMenu.TYPE_MENU, Messages.getString("MainMenuPanel.19"), //$NON-NLS-1$
                menuOptions, null, null, null, null);

        addMenuItems(menuOptionsGame, GAME_MENU_ITEMS);
        menuOptionsGame.addItem(createBackButton());

        return menuOptionsGame;
    }

    private static SmartMenu createPerformanceMenu(SmartMenu menuOptions) {
        SmartMenu menuOptionsPerformance = new SmartMenu(SmartMenu.TYPE_MENU, Messages.getString("MainMenuPanel.8"), //$NON-NLS-1$
                menuOptions, null, null, null, null);

        addMenuItems(menuOptionsPerformance, PERFORMANCE_MENU_ITEMS);
        menuOptionsPerformance.addItem(createBackButton());

        return menuOptionsPerformance;
    }
    // in game menu

    public static SmartMenu createOptionsMenu(SmartMenu mainMenu) {
        SmartMenu menuOptions = new SmartMenu(
                SmartMenu.TYPE_MENU,
                Messages.getString("MainMenuPanel.4"), //$NON-NLS-1$
                mainMenu,
                null,
                null,
                null,
                null);

        menuOptions.addItem(createGraphicsMenu(menuOptions));
        menuOptions.addItem(createSpacer());

        menuOptions.addItem(createAudioMenu(menuOptions));
        menuOptions.addItem(createSpacer());

        menuOptions.addItem(createGameMenu(menuOptions));
        menuOptions.addItem(createSpacer());

        menuOptions.addItem(createPerformanceMenu(menuOptions));
        menuOptions.addItem(createSpacer());

        menuOptions.addItem(createBackButton());

        return menuOptions;
    }
}
