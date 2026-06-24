package xaos.commands;

import java.util.HashMap;
import java.util.Map;

import xaos.data.Messages;
import xaos.main.Game;
import xaos.panels.CommandPanel;
import xaos.panels.MainPanel;
import xaos.panels.UI.UIScaler;
import xaos.tasks.Task;
import xaos.utils.Log;
import xaos.utils.TooltipScale;
import xaos.utils.UtilsGL;
import xaos.commands.containers.ContainerCopyToAllCommandHandler;
import xaos.commands.containers.ContainerFilterCommandHandler;
import xaos.commands.containers.ContainerManageCommandHandler;
import xaos.commands.debug.AddEventCommandHandler;
import xaos.commands.debug.AddItemCommandHandler;
import xaos.commands.debug.AddLivingCommandHandler;
import xaos.commands.debug.TestCommandHandler;
import xaos.commands.items.ItemRotateCommandHandler;
import xaos.commands.items.ItemTextAddCommandHandler;
import xaos.commands.items.ItemTextDeleteCommandHandler;
import xaos.commands.items.UnlockItemCommandHandler;
import xaos.commands.mainmenu.ContinueGameCommandHandler;
import xaos.commands.mainmenu.DeleteGameCommandHandler;
import xaos.commands.mainmenu.NewGameCommandHandler;
import xaos.commands.mainmenu.NewGameSetSaveNameCommandHandler;
import xaos.commands.options.*;
import xaos.commands.professions.CitizenSetJobGroupCommandHandler;
import xaos.commands.professions.JobGroupCommandHandler;
import xaos.commands.professions.ProfessionCommandHandler;
import xaos.commands.stockpiles.StockpileCopyToAllCommandHandler;
import xaos.commands.stockpiles.StockpileDisableAllCommandHandler;
import xaos.commands.stockpiles.StockpileDisableItemCommandHandler;
import xaos.commands.stockpiles.StockpileEnableAllCommandHandler;
import xaos.commands.stockpiles.StockpileEnableItemCommandHandler;
import xaos.commands.stockpiles.StockpileManageCommandHandler;
import xaos.commands.tasks.CreateSelectableTaskCommandHandler;
import xaos.commands.tasks.CreateTaskWithParameterCommandHandler;
import xaos.commands.tasks.CustomActionCommandHandler;
import xaos.commands.tasks.CustomActionDirectItemCommandHandler;
import xaos.commands.tasks.CustomActionDirectLivingCommandHandler;
import xaos.commands.tasks.ImmediateTaskCommandHandler;
import xaos.commands.tasks.QueueAndPlaceCommandHandler;
import xaos.commands.ui.BackCommandHandler;
import xaos.commands.ui.BuryCommandHandler;
import xaos.commands.ui.CloseContextCommandHandler;
import xaos.commands.ui.ExitGameCommandHandler;
import xaos.commands.ui.ExitToMainMenuCommandHandler;
import xaos.commands.ui.ExitToMainMenuNoSaveCommandHandler;
import xaos.commands.ui.ExitToMainMenuSaveCommandHandler;
import xaos.commands.ui.GameSpeedCommandHandler;
import xaos.commands.ui.LevelDownCommandHandler;
import xaos.commands.ui.LevelUpCommandHandler;
import xaos.commands.ui.MiniBlocksCommandHandler;
import xaos.commands.ui.PauseCommandHandler;
import xaos.commands.ui.SaveCommandHandler;
import xaos.commands.ui.TradeCommandHandler;
import xaos.commands.ui.ViewEntityCommandHandler;

public final class CommandDispatcher {

        private final Map<String, CommandHandler> handlers = new HashMap<>();

        public CommandDispatcher() {
                registerHandlers();
        }

        public void execute(CommandContext context) {
                CommandHandler handler = handlers.get(context.getCommand());

                if (handler == null) {
                        handleUnknownCommand(context);
                       
                }

                handler.execute(context);
                
        }

        private void registerHandlers() {

                // Two commands COMMAND_GOD_STATUS_LOWER_5 & COMMAND_GOD_STATUS_RAISE_5 are in
                // the original code but commented out. When the gods system is looked at these
                // commands may need to be implemented.

                // ============================================================
                // UI / NAVIGATION COMMANDS
                // ============================================================

                handlers.put(CommandPanel.COMMAND_BACK, new BackCommandHandler());
                handlers.put(CommandPanel.COMMAND_EXIT_GAME, new ExitGameCommandHandler());
                handlers.put(CommandPanel.COMMAND_EXIT_TO_MAIN_MENU, new ExitToMainMenuCommandHandler());
                handlers.put(CommandPanel.COMMAND_EXIT_TO_MAIN_MENU_NOSAVE, new ExitToMainMenuNoSaveCommandHandler());
                handlers.put(CommandPanel.COMMAND_EXIT_TO_MAIN_MENU_SAVE, new ExitToMainMenuSaveCommandHandler());

                handlers.put(CommandPanel.COMMAND_LEVEL_DOWN, new LevelDownCommandHandler());
                handlers.put(CommandPanel.COMMAND_LEVEL_UP, new LevelUpCommandHandler());

                handlers.put(CommandPanel.COMMAND_MINIBLOCKS, new MiniBlocksCommandHandler());
                handlers.put(CommandPanel.COMMAND_TRADE, new TradeCommandHandler());

                // ============================================================
                // GAME CONTROL COMMANDS
                // ============================================================

                handlers.put(CommandPanel.COMMAND_PAUSE, new PauseCommandHandler());

                handlers.put(
                                CommandPanel.COMMAND_INCREASE_SPEED,
                                new GameSpeedCommandHandler(GameSpeedCommandHandler.INCREASE));

                handlers.put(
                                CommandPanel.COMMAND_LOWER_SPEED,
                                new GameSpeedCommandHandler(GameSpeedCommandHandler.DECREASE));

                // ============================================================
                // CAMERA / ENTITY VIEW COMMANDS
                // ============================================================

                handlers.put(
                                CommandPanel.COMMAND_NEXT_CITIZEN,
                                new ViewEntityCommandHandler(ViewEntityCommandHandler.NEXT_CITIZEN));

                handlers.put(
                                CommandPanel.COMMAND_PREVIOUS_CITIZEN,
                                new ViewEntityCommandHandler(ViewEntityCommandHandler.PREVIOUS_CITIZEN));

                handlers.put(
                                CommandPanel.COMMAND_NEXT_SOLDIER,
                                new ViewEntityCommandHandler(ViewEntityCommandHandler.NEXT_SOLDIER));

                handlers.put(
                                CommandPanel.COMMAND_PREVIOUS_SOLDIER,
                                new ViewEntityCommandHandler(ViewEntityCommandHandler.PREVIOUS_SOLDIER));

                handlers.put(
                                CommandPanel.COMMAND_NEXT_HERO,
                                new ViewEntityCommandHandler(ViewEntityCommandHandler.NEXT_HERO));

                handlers.put(
                                CommandPanel.COMMAND_PREVIOUS_HERO,
                                new ViewEntityCommandHandler(ViewEntityCommandHandler.PREVIOUS_HERO));

                // ============================================================
                // SAVE / BURY COMMANDS
                // ============================================================

                handlers.put(CommandPanel.COMMAND_SAVE, new SaveCommandHandler(true));
                handlers.put(CommandPanel.COMMAND_SAVE_NO_MISSIONDATA, new SaveCommandHandler(false));
                handlers.put(CommandPanel.COMMAND_BURY, new BuryCommandHandler());

                // ============================================================
                // MAIN MENU / GAME START COMMANDS
                // ============================================================

                handlers.put(
                                CommandPanel.COMMAND_MM_NEWGAME_SET_SAVE_NAME,
                                new NewGameSetSaveNameCommandHandler(true));

                handlers.put(
                                CommandPanel.COMMAND_MM_NEWGAME_SET_SAVE_NAME_NO_BURY,
                                new NewGameSetSaveNameCommandHandler(false));

                handlers.put(CommandPanel.COMMAND_MM_NEWGAME, new NewGameCommandHandler());
                handlers.put(CommandPanel.COMMAND_MM_CONTINUEGAME, new ContinueGameCommandHandler());
                handlers.put(CommandPanel.COMMAND_MM_DELETEGAME, new DeleteGameCommandHandler());

                // ============================================================
                // SELECTABLE TASK COMMANDS
                // These create a current task using Game.createTask(...)
                // ============================================================

                handlers.put(
                                CommandPanel.COMMAND_MINE,
                                new CreateSelectableTaskCommandHandler(Task.TASK_MINE));

                handlers.put(
                                CommandPanel.COMMAND_MINE_LADDER,
                                new CreateSelectableTaskCommandHandler(Task.TASK_MINE_LADDER));

                handlers.put(
                                CommandPanel.COMMAND_DIG,
                                new CreateSelectableTaskCommandHandler(Task.TASK_DIG));

                handlers.put(
                                CommandPanel.COMMAND_CANCEL_ORDER,
                                new CreateSelectableTaskCommandHandler(Task.TASK_CANCEL_ORDER));

                handlers.put(
                                CommandPanel.COMMAND_BUILD,
                                new CreateTaskWithParameterCommandHandler(Task.TASK_BUILD));

                handlers.put(
                                CommandPanel.COMMAND_STOCKPILE,
                                new CreateTaskWithParameterCommandHandler(Task.TASK_STOCKPILE));

                handlers.put(
                                CommandPanel.COMMAND_CREATE_ZONE,
                                new CreateTaskWithParameterCommandHandler(Task.TASK_CREATE_ZONE));

                handlers.put(
                                CommandPanel.COMMAND_EXPAND_ZONE,
                                new CreateTaskWithParameterCommandHandler(Task.TASK_EXPAND_ZONE));

                handlers.put(
                                CommandPanel.COMMAND_CREATE_AND_PLACE,
                                new CreateTaskWithParameterCommandHandler(Task.TASK_CREATE_AND_PLACE));

                handlers.put(
                                CommandPanel.COMMAND_CREATE_AND_PLACE_ROW,
                                new CreateTaskWithParameterCommandHandler(Task.TASK_CREATE_AND_PLACE_ROW));

                // ============================================================
                // QUEUE / PLACE TASK COMMANDS
                // ============================================================

                handlers.put(
                                CommandPanel.COMMAND_QUEUE_AND_PLACE,
                                new QueueAndPlaceCommandHandler(Task.TASK_QUEUE_AND_PLACE, true));

                handlers.put(
                                CommandPanel.COMMAND_QUEUE_AND_PLACE_ROW,
                                new QueueAndPlaceCommandHandler(Task.TASK_QUEUE_AND_PLACE_ROW, false));

                handlers.put(
                                CommandPanel.COMMAND_QUEUE_AND_PLACE_AREA,
                                new QueueAndPlaceCommandHandler(Task.TASK_QUEUE_AND_PLACE_AREA, false));

                // ============================================================
                // CUSTOM ACTION COMMANDS
                // ============================================================

                handlers.put(CommandPanel.COMMAND_CUSTOM_ACTION, new CustomActionCommandHandler());

                handlers.put(
                                CommandPanel.COMMAND_CUSTOM_ACTION_DIRECT_LIVING,
                                new CustomActionDirectLivingCommandHandler());

                handlers.put(
                                CommandPanel.COMMAND_CUSTOM_ACTION_DIRECT_ITEM,
                                new CustomActionDirectItemCommandHandler());

                // ============================================================
                // IMMEDIATE TASK COMMANDS
                // These create a new Task(...) and add it directly to TaskManager
                // ============================================================

                handlers.put(CommandPanel.COMMAND_WEAR, new ImmediateTaskCommandHandler(Task.TASK_WEAR));
                handlers.put(CommandPanel.COMMAND_AUTOEQUIP, new ImmediateTaskCommandHandler(Task.TASK_AUTOEQUIP));
                handlers.put(CommandPanel.COMMAND_WEAR_OFF, new ImmediateTaskCommandHandler(Task.TASK_WEAR_OFF));

                handlers.put(
                                CommandPanel.COMMAND_TURN_OFF_NONSTOP,
                                new ImmediateTaskCommandHandler(Task.TASK_TURN_OFF_NON_STOP));

                handlers.put(
                                CommandPanel.COMMAND_TURN_ON_NONSTOP,
                                new ImmediateTaskCommandHandler(Task.TASK_TURN_ON_NON_STOP));

                handlers.put(
                                CommandPanel.COMMAND_REMOVE_BUILDING_TASK,
                                new ImmediateTaskCommandHandler(Task.TASK_REMOVE_BUILDING_TASK));

                handlers.put(
                                CommandPanel.COMMAND_DESTROY_BUILDING,
                                new ImmediateTaskCommandHandler(Task.TASK_DESTROY_BUILDING));

                handlers.put(CommandPanel.COMMAND_QUEUE, new ImmediateTaskCommandHandler(Task.TASK_QUEUE));
                handlers.put(CommandPanel.COMMAND_CREATE, new ImmediateTaskCommandHandler(Task.TASK_CREATE));

                handlers.put(
                                CommandPanel.COMMAND_CREATE_IN_A_BUILDING,
                                new ImmediateTaskCommandHandler(Task.TASK_CREATE_IN_A_BUILDING));

                handlers.put(
                                CommandPanel.COMMAND_DESTROY_ENTITY,
                                new ImmediateTaskCommandHandler(Task.TASK_DESTROY_ENTITY));

                handlers.put(CommandPanel.COMMAND_LOCK, new ImmediateTaskCommandHandler(Task.TASK_LOCK));
                handlers.put(CommandPanel.COMMAND_UNLOCK_OPEN, new ImmediateTaskCommandHandler(Task.TASK_UNLOCK_OPEN));
                handlers.put(CommandPanel.COMMAND_UNLOCK_CLOSE,
                                new ImmediateTaskCommandHandler(Task.TASK_UNLOCK_CLOSE));

                handlers.put(
                                CommandPanel.COMMAND_DELETE_STOCKPILE,
                                new ImmediateTaskCommandHandler(Task.TASK_DELETE_STOCKPILE));

                handlers.put(
                                CommandPanel.COMMAND_DELETE_ZONE,
                                new ImmediateTaskCommandHandler(Task.TASK_DELETE_ZONE));

                handlers.put(
                                CommandPanel.COMMAND_CHANGE_OWNER,
                                new ImmediateTaskCommandHandler(Task.TASK_CHANGE_OWNER));

                handlers.put(
                                CommandPanel.COMMAND_CHANGE_OWNER_GROUP,
                                new ImmediateTaskCommandHandler(Task.TASK_CHANGE_OWNER_GROUP));

                // ============================================================
                // TERRAIN TASK COMMANDS
                // ============================================================

                handlers.put(
                                CommandPanel.COMMAND_TERRAIN_CHANGE,
                                new ImmediateTaskCommandHandler(Task.TASK_TERRAIN_CHANGE));

                handlers.put(
                                CommandPanel.COMMAND_TERRAIN_ADD_FLUID,
                                new ImmediateTaskCommandHandler(Task.TASK_TERRAIN_ADD_FLUID));

                handlers.put(
                                CommandPanel.COMMAND_TERRAIN_REMOVE_FLUID,
                                new ImmediateTaskCommandHandler(Task.TASK_TERRAIN_REMOVE_FLUID));

                // ============================================================
                // SOLDIER / PATROL TASK COMMANDS
                // ============================================================

                handlers.put(
                                CommandPanel.COMMAND_CONVERT_TO_CIVILIAN,
                                new ImmediateTaskCommandHandler(Task.TASK_CONVERT_TO_CIVILIAN));

                handlers.put(
                                CommandPanel.COMMAND_CONVERT_TO_SOLDIER,
                                new ImmediateTaskCommandHandler(Task.TASK_CONVERT_TO_SOLDIER));

                handlers.put(
                                CommandPanel.COMMAND_SOLDIER_SET_STATE,
                                new ImmediateTaskCommandHandler(Task.TASK_SOLDIER_SET_STATE));

                handlers.put(
                                CommandPanel.COMMAND_ADD_PATROL_POINT,
                                new ImmediateTaskCommandHandler(Task.TASK_SOLDIER_ADD_PATROL_POINT));

                handlers.put(
                                CommandPanel.COMMAND_ADD_PATROL_POINT_GROUP,
                                new ImmediateTaskCommandHandler(Task.TASK_SOLDIER_ADD_PATROL_POINT_GROUP));

                handlers.put(
                                CommandPanel.COMMAND_REMOVE_PATROL_POINT,
                                new ImmediateTaskCommandHandler(Task.TASK_SOLDIER_REMOVE_PATROL_POINT));

                handlers.put(
                                CommandPanel.COMMAND_REMOVE_PATROL_POINT_GROUP,
                                new ImmediateTaskCommandHandler(Task.TASK_SOLDIER_REMOVE_PATROL_POINT_GROUP));

                // ============================================================
                // ITEM COMMANDS
                // ============================================================

                handlers.put(CommandPanel.COMMAND_ITEM_TEXT_ADD, new ItemTextAddCommandHandler());
                handlers.put(CommandPanel.COMMAND_ITEM_TEXT_DELETE, new ItemTextDeleteCommandHandler());
                handlers.put(CommandPanel.COMMAND_ITEM_ROTATE, new ItemRotateCommandHandler());
                handlers.put(CommandPanel.COMMAND_UNLOCK, new UnlockItemCommandHandler());

                // ============================================================
                // STOCKPILE COMMANDS
                // ============================================================

                handlers.put(
                                CommandPanel.COMMAND_STOCKPILE_ENABLE_ALL,
                                new StockpileEnableAllCommandHandler());

                handlers.put(
                                CommandPanel.COMMAND_STOCKPILE_DISABLE_ALL,
                                new StockpileDisableAllCommandHandler());

                handlers.put(
                                CommandPanel.COMMAND_STOCKPILE_MANAGE,
                                new StockpileManageCommandHandler());

                handlers.put(
                                CommandPanel.COMMAND_STOCKPILE_ENABLE_ITEM,
                                new StockpileEnableItemCommandHandler());

                handlers.put(
                                CommandPanel.COMMAND_STOCKPILE_DISABLE_ITEM,
                                new StockpileDisableItemCommandHandler());

                handlers.put(
                                CommandPanel.COMMAND_STOCKPILE_COPY_TO_ALL,
                                new StockpileCopyToAllCommandHandler());

                // ============================================================
                // CONTAINER COMMANDS
                // ============================================================

                handlers.put(
                                CommandPanel.COMMAND_CONTAINER_MANAGE,
                                new ContainerManageCommandHandler());

                handlers.put(
                                CommandPanel.COMMAND_CONTAINER_ENABLE_ALL,
                                new ContainerFilterCommandHandler(ContainerFilterCommandHandler.ENABLE_ALL));

                handlers.put(
                                CommandPanel.COMMAND_CONTAINER_DISABLE_ALL,
                                new ContainerFilterCommandHandler(ContainerFilterCommandHandler.DISABLE_ALL));

                handlers.put(
                                CommandPanel.COMMAND_CONTAINER_ENABLE_ITEM,
                                new ContainerFilterCommandHandler(ContainerFilterCommandHandler.ENABLE_ITEM));

                handlers.put(
                                CommandPanel.COMMAND_CONTAINER_DISABLE_ITEM,
                                new ContainerFilterCommandHandler(ContainerFilterCommandHandler.DISABLE_ITEM));

                handlers.put(
                                CommandPanel.COMMAND_CONTAINER_COPY_TO_ALL,
                                new ContainerCopyToAllCommandHandler());

                // ============================================================
                // PROFESSION / CITIZEN JOB COMMANDS
                // ============================================================

                handlers.put(
                                CommandPanel.COMMAND_PROFESSIONS_ENABLE_ALL,
                                new ProfessionCommandHandler(ProfessionCommandHandler.ENABLE_ALL));

                handlers.put(
                                CommandPanel.COMMAND_PROFESSIONS_DISABLE_ALL,
                                new ProfessionCommandHandler(ProfessionCommandHandler.DISABLE_ALL));

                handlers.put(
                                CommandPanel.COMMAND_PROFESSIONS_ENABLE_ITEM,
                                new ProfessionCommandHandler(ProfessionCommandHandler.ENABLE_ITEM));

                handlers.put(
                                CommandPanel.COMMAND_PROFESSIONS_DISABLE_ITEM,
                                new ProfessionCommandHandler(ProfessionCommandHandler.DISABLE_ITEM));

                handlers.put(
                                CommandPanel.COMMAND_JOB_GROUP_ENABLE_ALL,
                                new JobGroupCommandHandler(JobGroupCommandHandler.ENABLE_ALL));

                handlers.put(
                                CommandPanel.COMMAND_JOB_GROUP_DISABLE_ALL,
                                new JobGroupCommandHandler(JobGroupCommandHandler.DISABLE_ALL));

                handlers.put(
                                CommandPanel.COMMAND_JOB_GROUP_ENABLE_ITEM,
                                new JobGroupCommandHandler(JobGroupCommandHandler.ENABLE_ITEM));

                handlers.put(
                                CommandPanel.COMMAND_JOB_GROUP_DISABLE_ITEM,
                                new JobGroupCommandHandler(JobGroupCommandHandler.DISABLE_ITEM));

                handlers.put(
                                CommandPanel.COMMAND_CITIZEN_SET_JOB_GROUP,
                                new CitizenSetJobGroupCommandHandler());

                // ============================================================
                // OPTIONS - AUDIO
                // ============================================================

                handlers.put(CommandPanel.COMMAND_MM_SWITCH_MUSIC, new ToggleMusicCommandHandler());

                handlers.put(
                                CommandPanel.COMMAND_MM_ADD_MUSIC_VOLUME,
                                new SimpleOptionCommandHandler(() -> Game.addMusicVolume()));

                handlers.put(CommandPanel.COMMAND_MM_SWITCH_FX, new ToggleFxCommandHandler());

                handlers.put(
                                CommandPanel.COMMAND_MM_ADD_FX_VOLUME,
                                new SimpleOptionCommandHandler(() -> Game.addFXVolume()));

                // ============================================================
                // OPTIONS - DISPLAY / SCALE
                // ============================================================

                handlers.put(
                                CommandPanel.COMMAND_MM_TOGGLE_FULL_SCREEN,
                                new SimpleOptionCommandHandler(() -> UtilsGL.toggleFullScreen()));

                handlers.put(
                                CommandPanel.COMMAND_MM_UI_SCALE,
                                new SimpleOptionCommandHandler(() -> UIScaler.cycleUIScale()));

                handlers.put(
                                CommandPanel.COMMAND_MM_WORLD_ZOOM,
                                new SimpleOptionCommandHandler(() -> MainPanel.cycleWorldZoom()));

                handlers.put(
                                CommandPanel.COMMAND_MM_TOOLTIP_SCALE,
                                new SimpleOptionCommandHandler(() -> TooltipScale.cycleTooltipScale()));

                // ============================================================
                // OPTIONS - MOUSE / GAMEPLAY
                // ============================================================

                handlers.put(
                                CommandPanel.COMMAND_MM_SWITCH_MOUSE_SCROLL,
                                new SimpleOptionCommandHandler(() -> Game.setMouseScrollON(!Game.isMouseScrollON())));

                handlers.put(
                                CommandPanel.COMMAND_MM_SWITCH_MOUSE_SCROLL_EARS,
                                new SimpleOptionCommandHandler(
                                                () -> Game.setMouseScrollEarsON(!Game.isMouseScrollEarsON())));

                handlers.put(
                                CommandPanel.COMMAND_MM_SWITCH_MOUSE_2D_CUBES,
                                new SimpleOptionCommandHandler(() -> Game.setMouse2DCubesON(!Game.isMouse2DCubesON())));

                handlers.put(
                                CommandPanel.COMMAND_MM_SWITCH_DISABLE_ITEMS,
                                new SimpleOptionCommandHandler(
                                                () -> Game.setDisabledItemsON(!Game.isDisabledItemsON())));

                handlers.put(
                                CommandPanel.COMMAND_MM_SWITCH_DISABLE_GODS,
                                new SimpleOptionCommandHandler(() -> Game.setDisabledGodsON(!Game.isDisabledGodsON())));

                handlers.put(
                                CommandPanel.COMMAND_MM_SWITCH_PAUSE,
                                new SimpleOptionCommandHandler(() -> Game.setPauseStartON(!Game.isPauseStartON())));

                handlers.put(
                                CommandPanel.COMMAND_MM_SWITCH_AUTOSAVE_DAYS,
                                new SimpleOptionCommandHandler(() -> Game.setAutosaveDays(Game.getAutosaveDays() + 1)));

                handlers.put(
                                CommandPanel.COMMAND_MM_SWITCH_SIEGES,
                                new SiegeDifficultyCommandHandler());

                handlers.put(
                                CommandPanel.COMMAND_MM_SWITCH_SIEGE_PAUSE,
                                new SimpleOptionCommandHandler(() -> Game.setSiegePause(!Game.isSiegePause())));

                handlers.put(
                                CommandPanel.COMMAND_MM_SWITCH_CARAVAN_PAUSE,
                                new SimpleOptionCommandHandler(() -> Game.setCaravanPause(!Game.isCaravanPause())));

                handlers.put(
                                CommandPanel.COMMAND_MM_SWITCH_BURY,
                                new SwitchBuryCommandHandler());

                handlers.put(
                                CommandPanel.COMMAND_MM_SWITCH_PATHFINDING_LEVEL,
                                new SimpleOptionCommandHandler(
                                                () -> Game.setPathfindingCPULevel(Game.getPathfindingCPULevel() + 1)));

                // ============================================================
                // OPTIONS - LANGUAGE / HOTKEYS / ERRORS
                // ============================================================

                handlers.put(CommandPanel.COMMAND_CHANGE_LANGUAGE, new ChangeLanguageCommandHandler());
                handlers.put(CommandPanel.COMMAND_MM_DELETE_ERROR, new DeleteErrorCommandHandler());
                handlers.put(CommandPanel.COMMAND_CHANGE_HOTKEY, new ChangeHotkeyCommandHandler());

                // ============================================================
                // MOD / SERVER / SYSTEM COMMANDS
                // ============================================================

                handlers.put(CommandPanel.COMMAND_TOGGLE_MOD, new ToggleModCommandHandler());
                handlers.put(CommandPanel.COMMAND_SERVER_ADD, new ServerAddCommandHandler());
                handlers.put(CommandPanel.COMMAND_SERVER_REMOVE, new ServerRemoveCommandHandler());
                handlers.put(CommandPanel.COMMAND_OPEN_FOLDER, new OpenFolderCommandHandler());
                handlers.put(CommandPanel.COMMAND_CLOSE_CONTEXT, new CloseContextCommandHandler());

                // ============================================================
                // DEBUG / TEST COMMANDS
                // ============================================================

                handlers.put(
                                CommandPanel.COMMAND_TEST,
                                new TestCommandHandler(TestCommandHandler.NEW_CITIZEN));

                handlers.put(
                                CommandPanel.COMMAND_TEST2,
                                new TestCommandHandler(TestCommandHandler.FULFILL_NEEDS));

                handlers.put(
                                CommandPanel.COMMAND_TEST3,
                                new TestCommandHandler(TestCommandHandler.SPAWN_SIEGE));

                handlers.put(
                                CommandPanel.COMMAND_TEST4,
                                new TestCommandHandler(TestCommandHandler.FORCE_SLEEP));

                handlers.put(
                                CommandPanel.COMMAND_TEST5,
                                new TestCommandHandler(TestCommandHandler.CHECK_CARAVANS));

                handlers.put(
                                CommandPanel.COMMAND_TEST6,
                                new TestCommandHandler(TestCommandHandler.REVEAL_MAP));

                handlers.put(
                                CommandPanel.COMMAND_TEST7,
                                new TestCommandHandler(TestCommandHandler.CHECK_HEROES));

                handlers.put(CommandPanel.COMMAND_ADD_ITEM, new AddItemCommandHandler());
                handlers.put(CommandPanel.COMMAND_ADD_LIVING, new AddLivingCommandHandler());
                handlers.put(CommandPanel.COMMAND_ADD_EVENT, new AddEventCommandHandler());
        }

        private void handleUnknownCommand(CommandContext context) {
                Log.log(
                                Log.LEVEL_ERROR,
                                Messages.getString("CommandPanel.6")
                                                + context.getCommand()
                                                + "] ["
                                                + context.getParameter()
                                                + "]",
                                "CommandPanel");
        }
}