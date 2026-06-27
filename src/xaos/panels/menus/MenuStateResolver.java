package xaos.panels.menus;

import xaos.main.Game;
import xaos.panels.CommandPanel;
import xaos.utils.UtilsGL;

public final class MenuStateResolver {

    private MenuStateResolver() {
    }

    public static boolean getBoolean(String stateKey) {
        if (stateKey == null) {
            return false;
        }

        if (stateKey.equals(CommandPanel.COMMAND_MM_TOGGLE_FULL_SCREEN)) {
            return UtilsGL.isFullScreen();
        }

        if (stateKey.equals(CommandPanel.COMMAND_MM_SWITCH_MUSIC)) {
            return Game.isMusicON();
        }

        if (stateKey.equals(CommandPanel.COMMAND_MM_SWITCH_FX)) {
            return Game.isFXON();
        }

        if (stateKey.equals(CommandPanel.COMMAND_MM_SWITCH_MOUSE_SCROLL)) {
            return Game.isMouseScrollON();
        }

        if (stateKey.equals(CommandPanel.COMMAND_MM_SWITCH_MOUSE_SCROLL_EARS)) {
            return Game.isMouseScrollEarsON();
        }

        if (stateKey.equals(CommandPanel.COMMAND_MM_SWITCH_MOUSE_2D_CUBES)) {
            return Game.isMouse2DCubesON();
        }

        if (stateKey.equals(CommandPanel.COMMAND_MM_SWITCH_DISABLE_ITEMS)) {
            return Game.isDisabledItemsON();
        }

        if (stateKey.equals(CommandPanel.COMMAND_MM_SWITCH_DISABLE_GODS)) {
            return Game.isDisabledGodsON();
        }

        if (stateKey.equals(CommandPanel.COMMAND_MM_SWITCH_PAUSE)) {
            return Game.isPauseStartON();
        }

        if (stateKey.equals(CommandPanel.COMMAND_MM_SWITCH_SIEGE_PAUSE)) {
            return Game.isSiegePause();
        }

        if (stateKey.equals(CommandPanel.COMMAND_MM_SWITCH_CARAVAN_PAUSE)) {
            return Game.isCaravanPause();
        }

        if (stateKey.equals(CommandPanel.COMMAND_MM_SWITCH_BURY)) {
            return Game.isAllowBury();
        }

        return false;
    }

    public static float getFloat(String stateKey) {
        if (stateKey == null) {
            return 0f;
        }

        if (stateKey.equals(CommandPanel.COMMAND_MM_ADD_MUSIC_VOLUME)) {
            return Game.getVolumeMusic();
        }

        if (stateKey.equals(CommandPanel.COMMAND_MM_ADD_FX_VOLUME)) {
            return Game.getVolumeFX();
        }

        if (stateKey.equals(CommandPanel.COMMAND_MM_SWITCH_AUTOSAVE_DAYS)) {
            return Game.getAutosaveDays();
        }

        if (stateKey.equals(CommandPanel.COMMAND_MM_SWITCH_SIEGES)) {
            return Game.getSiegeDifficulty();
        }

        if (stateKey.equals(CommandPanel.COMMAND_MM_SWITCH_PATHFINDING_LEVEL)) {
            return Game.getPathfindingCPULevel();
        }

        return 0f;
    }
}