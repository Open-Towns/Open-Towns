package xaos.panels.menus;

import java.awt.Color;

import xaos.panels.CommandPanel;
import xaos.panels.menus.SmartMenu.MenuItemsDefinition;
import xaos.utils.Messages;

public class IngameOptionsMenu {

        private static final MenuItemsDefinition[] GRAPHICS_MENU_ITEMS = {
                        // Options - Graphics - Fullscreen
                        new MenuItemsDefinition(
                                        SmartMenu.TYPE_ITEM,
                                        "MainMenuPanel.17",
                                        CommandPanel.COMMAND_MM_TOGGLE_FULL_SCREEN,
                                        true,
                                        null,
                                        null,
                                        null,
                                        true, null, false),

                        // Options - Graphics - UI Scale
                        new MenuItemsDefinition(
                                        SmartMenu.TYPE_ITEM,
                                        "MainMenuPanel.83",
                                        CommandPanel.COMMAND_MM_UI_SCALE,
                                        false,
                                        null,
                                        null,
                                        null,
                                        true, null, false),

                        // Options - Graphics - Tooltip Scale
                        new MenuItemsDefinition(
                                        SmartMenu.TYPE_ITEM,
                                        "MainMenuPanel.84",
                                        CommandPanel.COMMAND_MM_TOOLTIP_SCALE,
                                        false,
                                        null,
                                        null,
                                        null,
                                        true, null, false),

                        // Options - Graphics - World Zoom
                        new MenuItemsDefinition(
                                        SmartMenu.TYPE_ITEM,
                                        "MainMenuPanel.85",
                                        CommandPanel.COMMAND_MM_WORLD_ZOOM,
                                        false,
                                        null,
                                        null,
                                        null,
                                        true, null, false)
        };

      

        private static SmartMenu createGraphicsMenu(SmartMenu menuOptions) {
                SmartMenu menuOptionsGraphics = new SmartMenu(
                                SmartMenu.TYPE_MENU,
                                Messages.getString("MainMenuPanel.16"), //$NON-NLS-1$
                                menuOptions,
                                null,
                                null,
                                null,
                                null);

                SmartMenu.addMenuItems(menuOptionsGraphics, GRAPHICS_MENU_ITEMS);
                menuOptionsGraphics.addItem(SmartMenu.createBackButton());
                return menuOptionsGraphics;
        }

        private static final MenuItemsDefinition[] AUDIO_MENU_ITEMS = {
                        // Options - Audio - Music on/off
                        new MenuItemsDefinition(
                                        SmartMenu.TYPE_ITEM,
                                        "MainMenuPanel.5",
                                        CommandPanel.COMMAND_MM_SWITCH_MUSIC,
                                        true,
                                        null,
                                        null,
                                        null,
                                        true, null, false),

                        // Options - Audio - Music volume
                        new MenuItemsDefinition(
                                        SmartMenu.TYPE_ITEM,
                                        "MainMenuPanel.56",
                                        CommandPanel.COMMAND_MM_ADD_MUSIC_VOLUME,
                                        true,
                                        null,
                                        null,
                                        null,
                                        true, null, false),

                        // Spacer
                        menuSpacerDefinition(),

                        // Options - Audio - FX on/off
                        new MenuItemsDefinition(
                                        SmartMenu.TYPE_ITEM,
                                        "MainMenuPanel.6",
                                        CommandPanel.COMMAND_MM_SWITCH_FX,
                                        true,
                                        null,
                                        null,
                                        null,

                                        true, null, false),

                        // Options - Audio - FX volume
                        new MenuItemsDefinition(
                                        SmartMenu.TYPE_ITEM,
                                        "MainMenuPanel.57",
                                        CommandPanel.COMMAND_MM_ADD_FX_VOLUME,
                                        true,
                                        null,
                                        null,
                                        null,
                                        true, null, false),

                        // Spacer
                        menuSpacerDefinition(),

        };

        private static SmartMenu createAudioMenu(SmartMenu menuOptions) {
                SmartMenu menuOptionsAudio = new SmartMenu(SmartMenu.TYPE_MENU, Messages.getString("MainMenuPanel.13"), //$NON-NLS-1$
                                menuOptions, null, null, null, null);

                SmartMenu.addMenuItems(menuOptionsAudio, AUDIO_MENU_ITEMS);
                menuOptionsAudio.addItem(SmartMenu.createBackButton());

                return menuOptionsAudio;
        }

        private static final MenuItemsDefinition[] GAME_MENU_ITEMS = {
                        // Options - Game - Mouse scroll
                        new MenuItemsDefinition(
                                        SmartMenu.TYPE_ITEM,
                                        "MainMenuPanel.20",
                                        CommandPanel.COMMAND_MM_SWITCH_MOUSE_SCROLL,
                                        true,
                                        null,
                                        null,
                                        null,
                                        true, null, false),

                        // Options - Game - Mouse scroll ears
                        new MenuItemsDefinition(
                                        SmartMenu.TYPE_ITEM,
                                        "MainMenuPanel.33",
                                        CommandPanel.COMMAND_MM_SWITCH_MOUSE_SCROLL_EARS,
                                        true,
                                        null,
                                        null,
                                        null,
                                        true, null, false),

                        // Options - Game - 2D mouse cubes
                        new MenuItemsDefinition(
                                        SmartMenu.TYPE_ITEM,
                                        "MainMenuPanel.72",
                                        CommandPanel.COMMAND_MM_SWITCH_MOUSE_2D_CUBES,
                                        true,
                                        null,
                                        null,
                                        null,
                                        true, null, false),

                        // Options - Game - Disable items
                        new MenuItemsDefinition(
                                        SmartMenu.TYPE_ITEM,
                                        "MainMenuPanel.27",
                                        CommandPanel.COMMAND_MM_SWITCH_DISABLE_ITEMS,
                                        true,
                                        null,
                                        null,
                                        null,
                                        true, null, false),

                        // Options - Game - Disable gods
                        new MenuItemsDefinition(
                                        SmartMenu.TYPE_ITEM,
                                        "MainMenuPanel.77",
                                        CommandPanel.COMMAND_MM_SWITCH_DISABLE_GODS,
                                        true,
                                        null,
                                        null,
                                        null,
                                        true, null, false),

                        // Options - Game - Pause when inactive
                        new MenuItemsDefinition(
                                        SmartMenu.TYPE_ITEM,
                                        "MainMenuPanel.28",
                                        CommandPanel.COMMAND_MM_SWITCH_PAUSE,
                                        true,
                                        null,
                                        null,
                                        null,
                                        true, null, false),

                        // Options - Game - Autosave days
                        new MenuItemsDefinition(
                                        SmartMenu.TYPE_ITEM,
                                        "MainMenuPanel.31",
                                        CommandPanel.COMMAND_MM_SWITCH_AUTOSAVE_DAYS,
                                        true,
                                        null,
                                        null,
                                        null,
                                        true, null, false),

                        // Options - Game - Sieges
                        new MenuItemsDefinition(
                                        SmartMenu.TYPE_ITEM,
                                        "MainMenuPanel.32",
                                        CommandPanel.COMMAND_MM_SWITCH_SIEGES,
                                        true,
                                        null,
                                        null,
                                        null,
                                        true, null, false),

                        // Options - Game - Pause on siege
                        new MenuItemsDefinition(
                                        SmartMenu.TYPE_ITEM,
                                        "MainMenuPanel.36",
                                        CommandPanel.COMMAND_MM_SWITCH_SIEGE_PAUSE,
                                        true,
                                        null,
                                        null,
                                        null,
                                        true, null, false),

                        // Options - Game - Pause on caravan
                        new MenuItemsDefinition(
                                        SmartMenu.TYPE_ITEM,
                                        "MainMenuPanel.66",
                                        CommandPanel.COMMAND_MM_SWITCH_CARAVAN_PAUSE,
                                        true,
                                        null,
                                        null,
                                        null,
                                        true, null, false),

                        // Spacer
                        menuSpacerDefinition(),
        };

        private final static MenuItemsDefinition menuSpacerDefinition() {
                return new MenuItemsDefinition(
                                SmartMenu.TYPE_TEXT,
                                null,
                                null,
                                false,
                                null,
                                null,
                                null,
                                false, null, false);
        }

        private static final MenuItemsDefinition[] PERFORMANCE_MENU_ITEMS = {
                        // Options - Performance - Info text
                        new MenuItemsDefinition(
                                        SmartMenu.TYPE_TEXT,
                                        "MainMenuPanel.59",
                                        null,
                                        false,
                                        null,
                                        null,
                                        null,
                                        false, Color.LIGHT_GRAY, false),

                        // Options - Performance - Info text
                        new MenuItemsDefinition(
                                        SmartMenu.TYPE_TEXT,
                                        "MainMenuPanel.64",
                                        null,
                                        false,
                                        null,
                                        null,
                                        null,
                                        false, Color.LIGHT_GRAY, false),

                        // Options - Performance - Info text
                        new MenuItemsDefinition(
                                        SmartMenu.TYPE_TEXT,
                                        "MainMenuPanel.65",
                                        null,
                                        false,
                                        null,
                                        null,
                                        null,
                                        false, Color.LIGHT_GRAY, false),

                        // Options - Performance - Pathfinding level
                        new MenuItemsDefinition(
                                        SmartMenu.TYPE_ITEM,
                                        "MainMenuPanel.63",
                                        CommandPanel.COMMAND_MM_SWITCH_PATHFINDING_LEVEL,
                                        true,
                                        null,
                                        null,
                                        null,
                                        true, null, false),

                        // Spacer
                        menuSpacerDefinition()
        };

        private static SmartMenu createGameMenu(SmartMenu menuOptions) {
                SmartMenu menuOptionsGame = new SmartMenu(SmartMenu.TYPE_MENU, Messages.getString("MainMenuPanel.19"), //$NON-NLS-1$
                                menuOptions, null, null, null, null);

                SmartMenu.addMenuItems(menuOptionsGame, GAME_MENU_ITEMS);
                menuOptionsGame.addItem(SmartMenu.createBackButton());

                return menuOptionsGame;
        }

        private static SmartMenu createPerformanceMenu(SmartMenu menuOptions) {
                SmartMenu menuOptionsPerformance = new SmartMenu(SmartMenu.TYPE_MENU,
                                Messages.getString("MainMenuPanel.8"), //$NON-NLS-1$
                                menuOptions, null, null, null, null);

                SmartMenu.addMenuItems(menuOptionsPerformance, PERFORMANCE_MENU_ITEMS);
                menuOptionsPerformance.addItem(SmartMenu.createBackButton());

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
                menuOptions.addItem(SmartMenu.createSpacer());

                menuOptions.addItem(createAudioMenu(menuOptions));
                menuOptions.addItem(SmartMenu.createSpacer());

                menuOptions.addItem(createGameMenu(menuOptions));
                menuOptions.addItem(SmartMenu.createSpacer());

                menuOptions.addItem(createPerformanceMenu(menuOptions));
                menuOptions.addItem(SmartMenu.createSpacer());

                menuOptions.addItem(SmartMenu.createBackButton());

                return menuOptions;
        }
}
