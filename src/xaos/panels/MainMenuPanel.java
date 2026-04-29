package xaos.panels;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import xaos.platform.lwjgl3.input.Keyboard;
import xaos.platform.lwjgl3.input.Mouse;
import xaos.platform.lwjgl3.opengl.Display;
import org.lwjgl.opengl.GL11;
import xaos.property.PropertyFile;

import xaos.Towns;
import xaos.TownsProperties;
import xaos.campaign.CampaignData;
import xaos.campaign.CampaignManager;
import xaos.main.Game;
import xaos.panels.menus.ContextMenu;
import xaos.panels.menus.SmartMenu;
import xaos.utils.ColorGL;
import xaos.utils.LanguageData;
import xaos.utils.Log;
import xaos.utils.Messages;
import xaos.utils.Point3D;
import xaos.utils.TextureData;
import xaos.utils.UtilFont;
import xaos.utils.Utils;
import xaos.utils.UtilsGL;
import xaos.utils.UtilsKeyboard;

public final class MainMenuPanel implements Runnable {

    public static int TEXTURE_MAIN_MENU_ID;
    public static int TEXTURE_SMP_LOGO_ID;
    public static int TEXTURE_LOADING_ID;
    public static int TEXTURE_TOWNS_LOGO_ID;

    public boolean active;

    private int renderX;
    private int renderY;
    private int renderWidth;
    private int renderHeight;
    private int xMenu;
    private int yMenu;

    private int imageLoadingWidth;
    private int imageLoadingHeight;
    private int imageTownsLogoWidth;
    private int imageTownsLogoHeight;

    private ContextMenu menu;

    public final ColorGL COLORGL_BLACK = new ColorGL(Color.BLACK);
    public final ColorGL COLORGL_WHITE = new ColorGL(Color.WHITE);
    public final ColorGL COLORGL_RED = new ColorGL(Color.RED);

    public static float startingGame = 80f;
    public static boolean deleteLogoTexture = false;
    public static boolean loadingGame = false;
    public static boolean useBuryTemporary = true;
    public static ColorGL startingGameColor;
    private String loadingText = new String();

    private boolean settingSavegameName = false;
    private int settingHotkey = 0; // Entero, 0 = cerrado, 1 = abierto y seteando la primera hotkey, 2 = abierto y seteando la segunda hotkey
    private boolean settingNewServer = false;
    private String saveGameCampaignID;
    private String saveGameMissionID;

    private String errorToShow = null;

    public MainMenuPanel(int renderX, int renderY, int renderWidth, int renderHeight) {
        resize(renderX, renderY, renderWidth, renderHeight);

        loadMenuTexture(false);

        TextureData textureSMPLogo = UtilsGL.loadTexture(Towns.getPropertiesString(PropertyFile.PROPERTY_FILE_GRAPHICS, "SMP_LOGO_FILE"), GL11.GL_MODULATE); //$NON-NLS-1$
        if (textureSMPLogo == null) {
            Log.log(Log.LEVEL_ERROR, Messages.getString("MainMenuPanel.21"), getClass().getName()); //$NON-NLS-1$
            Game.exit();
        }
        TEXTURE_SMP_LOGO_ID = textureSMPLogo.getTextureID();

        TextureData textureLoading = UtilsGL.loadTexture(Towns.getPropertiesString(PropertyFile.PROPERTY_FILE_GRAPHICS, "LOADING_FILE"), GL11.GL_REPLACE); //$NON-NLS-1$
        if (textureLoading == null) {
            Log.log(Log.LEVEL_ERROR, Messages.getString("MainMenuPanel.24"), getClass().getName()); //$NON-NLS-1$
            Game.exit();
        }
        TEXTURE_LOADING_ID = textureLoading.getTextureID();

        imageLoadingWidth = textureLoading.getWidth();
        imageLoadingHeight = textureLoading.getHeight();

        textureLoading = UtilsGL.loadTexture(Towns.getPropertiesString(PropertyFile.PROPERTY_FILE_GRAPHICS, "TOWNS_LOGO_FILE"), GL11.GL_REPLACE); //$NON-NLS-1$ //$NON-NLS-2$
        if (textureLoading == null) {
            Log.log(Log.LEVEL_ERROR, Messages.getString("MainMenuPanel.25"), getClass().getName()); //$NON-NLS-1$
            Game.exit();
        }

        TEXTURE_TOWNS_LOGO_ID = textureLoading.getTextureID();
        imageTownsLogoWidth = textureLoading.getWidth();
        imageTownsLogoHeight = textureLoading.getHeight();

        setErrorToShow(null);
        createMenu();

        startingGame = 80;
        startingGameColor = new ColorGL(new Color(255, 255, 255));
        loadingGame = false;
        deleteLogoTexture = false;
        settingSavegameName = false;
        settingHotkey = 0;
        settingNewServer = false;
        new Thread(this).start();

        if (TownsProperties.DEBUG_MODE) {
            startingGame = 0;
        }
    }

    public void loadMenuTexture(boolean bUnload) {
        final TextureData textureMainMenu = UtilsGL.loadTexture(Towns.getPropertiesString(PropertyFile.PROPERTY_FILE_GRAPHICS, "MAINMENU_BG_FILE"), GL11.GL_REPLACE, true); //$NON-NLS-1$
        if (textureMainMenu == null) {
            Log.log(Log.LEVEL_ERROR, Messages.getString("MainMenuPanel.0"), getClass().getName()); //$NON-NLS-1$
            Game.exit();
        }
        TEXTURE_MAIN_MENU_ID = textureMainMenu.getTextureID(); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void createMenu() {
        setLoadingText(new String());
        loadingGame = false;

        SmartMenu menuAux;
        menu = new ContextMenu();
        menu.setHeight(MainFrame.MIN_HEIGHT - UtilFont.MAX_HEIGHT * 8);
        Color textColor = Color.WHITE;
        Color creditsColor = Color.YELLOW;
        ColorGL borderColor = new ColorGL(Color.BLACK);
        SmartMenu mainMenu = new SmartMenu();
        mainMenu.setTrasparency(true);

        // Error??
        if (getErrorToShow() != null) {
            mainMenu.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, getErrorToShow(), null, CommandPanel.COMMAND_MM_DELETE_ERROR, null, null, null, Color.RED));
            mainMenu.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null, null));
        }

        // Campaign / new game
        ArrayList<CampaignData> alCampaigns = CampaignManager.getCampaigns();
        if (alCampaigns != null && alCampaigns.size() > 0) {
        	// Tutorial
            SmartMenu menuTutorial = new SmartMenu(SmartMenu.TYPE_MENU, Messages.getString("MainMenuPanel.82"), mainMenu, null, null, null, null, textColor); //$NON-NLS-1$
            menuTutorial.setTrasparency (mainMenu.isTrasparency());
            menuTutorial.setBorderColor (borderColor);
            menuTutorial.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("MainMenuPanel.82"), null, null, null, null)); //$NON-NLS-1$
            menuTutorial.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null, null));

            // New game
            SmartMenu menuCampaign = new SmartMenu(SmartMenu.TYPE_MENU, Messages.getString("MainMenuPanel.53"), mainMenu, null, null, null, null, textColor); //$NON-NLS-1$
            menuCampaign.setTrasparency(mainMenu.isTrasparency());
            menuCampaign.setBorderColor(borderColor);
            menuCampaign.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("MainMenuPanel.53"), null, null, null, null)); //$NON-NLS-1$
            menuCampaign.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null, null));

            // Fill the menus (tutorial OR new game)
            for (int i = 0; i < alCampaigns.size(); i++) {
                String sCampaignID = alCampaigns.get(i).getId();
                boolean bTutorial = alCampaigns.get(i).isTutorial ();
                SmartMenu campaign;
                if (bTutorial) {
                	campaign = menuTutorial;
                } else {
                	campaign = menuCampaign;
                }

//                SmartMenu campaign = new SmartMenu(SmartMenu.TYPE_MENU, alCampaigns.get(i).getName(), menuCampaign, null, null, null, null, textColor);
//                campaign.setTrasparency(true);
//                campaign.setBorderColor(borderColor);

//                campaign.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, alCampaigns.get(i).getName(), null, null, null, null));
//                campaign.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null, null));

                for (int j = 0; j < alCampaigns.get(i).getMissions().size(); j++) {
                    String missionName = alCampaigns.get(i).getMissions().get(j).getName();
                    String sMissionID = alCampaigns.get(i).getMissions().get(j).getId();

                    // Añadimos la opción de bajar burieds
                    if (Game.isAllowBury() && alCampaigns.get(i).getMissions().get(j).isAllowBury()) {
                        SmartMenu loadBurieds = new SmartMenu(SmartMenu.TYPE_MENU, missionName, campaign, null, null, null, null, textColor);
                        loadBurieds.setTrasparency(true);
                        loadBurieds.setBorderColor(borderColor);

                        loadBurieds.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, missionName, null, null, null, null));
                        loadBurieds.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null, null));

                        // No bury
                        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.69"), null, CommandPanel.COMMAND_MM_NEWGAME_SET_SAVE_NAME_NO_BURY, sCampaignID, sMissionID, null, textColor); //$NON-NLS-1$
                        menuAux.setBorderColor(borderColor);
                        loadBurieds.addItem(menuAux);
                        loadBurieds.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));

                        // Local bury
                        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.67"), null, CommandPanel.COMMAND_MM_NEWGAME_SET_SAVE_NAME, sCampaignID, sMissionID, null, textColor); //$NON-NLS-1$
                        menuAux.setBorderColor(borderColor);
                        loadBurieds.addItem(menuAux);
                        loadBurieds.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));

                        // Server bury
                        if (Game.getServerNames().size() > 0) {
                            for (int s = 0; s < Game.getServerNames().size(); s++) {
                                menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.68") + " [" + Game.getServerNames().get(s) + "]", null, CommandPanel.COMMAND_MM_NEWGAME_SET_SAVE_NAME, sCampaignID, sMissionID, new Point3D(s, s, s), textColor); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                menuAux.setBorderColor(borderColor);
                                loadBurieds.addItem(menuAux);
                                loadBurieds.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
                            }
                        }
                        loadBurieds.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.7"), null, CommandPanel.COMMAND_BACK, null, null, null, textColor)); //$NON-NLS-1$
                        campaign.addItem(loadBurieds);
                    } else {
                        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, missionName, null, CommandPanel.COMMAND_MM_NEWGAME_SET_SAVE_NAME, sCampaignID, sMissionID, null, textColor);
                        menuAux.setBorderColor(borderColor);
                        campaign.addItem(menuAux);
                    }
                }
                campaign.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
                menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.7"), null, CommandPanel.COMMAND_BACK, null, null, null, textColor); //$NON-NLS-1$
                menuAux.setBorderColor(borderColor);
                campaign.addItem(menuAux);

                // Add it
//                if (bTutorial) {
//                    menuTutorial.addItem(campaign);
//                    menuTutorial.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
//                } else {
//                    menuCampaign.addItem(campaign);
//                    menuCampaign.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
//                }
            }

            menuCampaign.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
            menuTutorial.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));

//            // Back New game
//            menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.7"), null, CommandPanel.COMMAND_BACK, null, null, null, textColor); //$NON-NLS-1$
//            menuAux.setBorderColor(borderColor);
//            menuCampaign.addItem(menuAux);
//
//            // Back Tutorial
//            menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.7"), null, CommandPanel.COMMAND_BACK, null, null, null, textColor); //$NON-NLS-1$
//            menuAux.setBorderColor(borderColor);
//            menuTutorial.addItem(menuAux);

            mainMenu.addItem(menuTutorial);
            mainMenu.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
            mainMenu.addItem(menuCampaign);
        }

        // 2 Blanks
        mainMenu.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));

        // Load game
        // Si no hay saves no creamos este menu
        ArrayList<File> alSavegames = Utils.getSaveFiles();
        if (alSavegames != null && alSavegames.size() > 0) {
            SmartMenu menuLoad = new SmartMenu(SmartMenu.TYPE_MENU, Messages.getString("MainMenuPanel.38"), mainMenu, null, null, null, null, textColor); //$NON-NLS-1$
            menuLoad.setTrasparency(mainMenu.isTrasparency());
            menuLoad.setBorderColor(borderColor);

            menuLoad.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("MainMenuPanel.38"), null, null, null)); //$NON-NLS-1$
            menuLoad.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));

            File fAux;
            Calendar cal;
            Calendar calToday = Calendar.getInstance();
            String sDate;
            for (int i = 0; i < alSavegames.size(); i++) {
                fAux = alSavegames.get(i);
                cal = Calendar.getInstance();
                cal.setTimeInMillis(fAux.lastModified());
                if (cal.get(Calendar.DAY_OF_MONTH) == calToday.get(Calendar.DAY_OF_MONTH) && cal.get(Calendar.MONTH) == calToday.get(Calendar.MONTH) && cal.get(Calendar.YEAR) == calToday.get(Calendar.YEAR)) {
                    sDate = " (" + Messages.getString("MainMenuPanel.42"); //$NON-NLS-1$ //$NON-NLS-2$
                } else if ((cal.get(Calendar.DAY_OF_MONTH) + 1) == calToday.get(Calendar.DAY_OF_MONTH) && cal.get(Calendar.MONTH) == calToday.get(Calendar.MONTH) && cal.get(Calendar.YEAR) == calToday.get(Calendar.YEAR)) {
                    sDate = " (" + Messages.getString("MainMenuPanel.51"); //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    sDate = " (" + cal.get(Calendar.DAY_OF_MONTH) + "/" + (((cal.get(Calendar.MONTH) + 1) < 10) ? "0" : "") + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                }
                sDate += " " + ((cal.get(Calendar.HOUR_OF_DAY) < 10) ? "0" : "") + cal.get(Calendar.HOUR_OF_DAY) + ":" + (cal.get(Calendar.MINUTE) < 10 ? "0" : "") + cal.get(Calendar.MINUTE) + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$

                // Load game
                menuLoad.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.40") + Utils.removeExtension(fAux.getName()) + sDate, null, CommandPanel.COMMAND_MM_CONTINUEGAME, fAux.getName(), null, null, textColor)); //$NON-NLS-1$
                // Delete game (con su submenú de confirmación)
                SmartMenu menuDelete = new SmartMenu(SmartMenu.TYPE_MENU, Messages.getString("MainMenuPanel.49") + Utils.removeExtension(fAux.getName()) + sDate, menuLoad, null, null, null, null, Color.RED); //$NON-NLS-1$
                menuDelete.setTrasparency(mainMenu.isTrasparency());
                menuDelete.setBorderColor(borderColor);

                menuDelete.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("MainMenuPanel.49") + Utils.removeExtension(fAux.getName()) + sDate, null, null, null)); //$NON-NLS-1$
                menuDelete.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
                menuDelete.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("MainMenuPanel.54"), null, null, null, null, null, Color.RED)); //$NON-NLS-1$
                menuDelete.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.55") + Utils.removeExtension(fAux.getName()) + sDate, null, CommandPanel.COMMAND_MM_DELETEGAME, fAux.getName(), null, null, textColor)); //$NON-NLS-1$
                menuDelete.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
                menuDelete.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.7"), null, CommandPanel.COMMAND_BACK, null, null, null, textColor)); //$NON-NLS-1$
                menuLoad.addItem(menuDelete);

                // Blank line
                menuLoad.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
            }
            menuLoad.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
            menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.7"), null, CommandPanel.COMMAND_BACK, null, null, null, textColor); //$NON-NLS-1$
            menuAux.setBorderColor(borderColor);
            menuLoad.addItem(menuAux);
            mainMenu.addItem(menuLoad);

            // 2 Blanks
            mainMenu.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
            mainMenu.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));

        }

        // Mods
        SmartMenu smMods = new SmartMenu(SmartMenu.TYPE_MENU, Messages.getString("MainMenuPanel.58"), mainMenu, null, null, null, null, textColor); //$NON-NLS-1$
        smMods.setTrasparency(mainMenu.isTrasparency());
        smMods.setBorderColor(borderColor);
        smMods.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("MainMenuPanel.58"), null, null, null, null)); //$NON-NLS-1$
        smMods.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null, null));
        String sModsFolder = Game.getUserFolder() + Game.getFileSeparator() + Game.MODS_FOLDER1 + Game.getFileSeparator();
        smMods.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.70") + " [" + sModsFolder + "]", null, CommandPanel.COMMAND_OPEN_FOLDER, sModsFolder, null)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        smMods.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null, null));

        ArrayList<File> alMods = Utils.getModsFolders();

        if (alMods != null && alMods.size() > 0) {
            for (int i = 0; i < alMods.size(); i++) {
                menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, alMods.get(i).getName() + " __MOD__" + alMods.get(i).getName() + "__/MOD__", null, CommandPanel.COMMAND_TOGGLE_MOD, alMods.get(i).getName(), null); //$NON-NLS-1$ //$NON-NLS-2$
                menuAux.setDynamic(true);
                menuAux.setBorderColor(borderColor);
                smMods.addItem(menuAux);
            }
        } else {
            // No mods
            smMods.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("MainMenuPanel.60"), null, null, null, null)); //$NON-NLS-1$
        }

        smMods.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.7"), null, CommandPanel.COMMAND_BACK, null, null, null, textColor); //$NON-NLS-1$
        menuAux.setBorderColor(borderColor);
        smMods.addItem(menuAux);

        mainMenu.addItem(smMods);

        // Blank
        mainMenu.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));

        // Servers
		SmartMenu smServers = new SmartMenu(SmartMenu.TYPE_MENU, Messages.getString("MainMenuPanel.71"), mainMenu, null, null, null, null, textColor); //$NON-NLS-1$
        smServers.setTrasparency(mainMenu.isTrasparency());
        smServers.setBorderColor(borderColor);
        smServers.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("MainMenuPanel.71"), null, null, null, null)); //$NON-NLS-1$
        smServers.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null, null));

        ArrayList<String> alServerNames = Game.getServerNames();

        if (alServerNames.size() > 0) {
            String sServer;
            for (int i = 0; i < alServerNames.size(); i++) {
                sServer = alServerNames.get(i);

                menuAux = new SmartMenu(SmartMenu.TYPE_TEXT, sServer, null, null, null, null, null, Color.YELLOW);
                menuAux.setDynamic(true);
                menuAux.setBorderColor(borderColor);
                smServers.addItem(menuAux);
                smServers.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.73") + sServer, null, CommandPanel.COMMAND_SERVER_REMOVE, Game.getServers().get(i))); //$NON-NLS-1$
                smServers.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
            }
        } else {
            // No servers
            smServers.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("MainMenuPanel.74"), null, null, null, null)); //$NON-NLS-1$
            smServers.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
        }

        smServers.addItem(new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.75"), null, CommandPanel.COMMAND_SERVER_ADD, null)); //$NON-NLS-1$
        smServers.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.7"), null, CommandPanel.COMMAND_BACK, null, null, null, textColor); //$NON-NLS-1$
        menuAux.setBorderColor(borderColor);
        smServers.addItem(menuAux);

        mainMenu.addItem(smServers);

        // Blank
        mainMenu.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));

        // Options
        SmartMenu menuOptions = new SmartMenu(SmartMenu.TYPE_MENU, Messages.getString("MainMenuPanel.4"), mainMenu, null, null, null, null, textColor); //$NON-NLS-1$
        menuOptions.setTrasparency(mainMenu.isTrasparency());
        menuOptions.setBorderColor(borderColor);

        menuOptions.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("MainMenuPanel.4"), null, null, null, null)); //$NON-NLS-1$
        menuOptions.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null, null));

        // Options - Graphics
        SmartMenu menuOptionsGraphics = new SmartMenu(SmartMenu.TYPE_MENU, Messages.getString("MainMenuPanel.16"), menuOptions, null, null, null, null, textColor); //$NON-NLS-1$
        menuOptionsGraphics.setTrasparency(mainMenu.isTrasparency());
        menuOptionsGraphics.setBorderColor(borderColor);
        menuOptionsGraphics.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("MainMenuPanel.16"), null, null, null, null)); //$NON-NLS-1$
        menuOptionsGraphics.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null, null));

        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.17"), null, CommandPanel.COMMAND_MM_TOGGLE_FULL_SCREEN, null, null, null, textColor); //$NON-NLS-1$
        menuAux.setDynamic(true);
        menuAux.setBorderColor(borderColor);
        menuOptionsGraphics.addItem(menuAux);
        menuOptionsGraphics.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.7"), null, CommandPanel.COMMAND_BACK, null, null, null, textColor); //$NON-NLS-1$
        menuAux.setBorderColor(borderColor);
        menuOptionsGraphics.addItem(menuAux);

        // Options - Audio
        SmartMenu menuOptionsAudio = new SmartMenu(SmartMenu.TYPE_MENU, Messages.getString("MainMenuPanel.13"), menuOptions, null, null, null, null, textColor); //$NON-NLS-1$
        menuOptionsAudio.setTrasparency(mainMenu.isTrasparency());
        menuOptionsAudio.setBorderColor(borderColor);
        menuOptionsAudio.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("MainMenuPanel.13"), null, null, null, null)); //$NON-NLS-1$
        menuOptionsAudio.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null, null));

        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.5"), null, CommandPanel.COMMAND_MM_SWITCH_MUSIC, null, null, null, textColor); //$NON-NLS-1$
        menuAux.setDynamic(true);
        menuAux.setBorderColor(borderColor);
        menuOptionsAudio.addItem(menuAux);
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.56"), null, CommandPanel.COMMAND_MM_ADD_MUSIC_VOLUME, null, null, null, textColor); //$NON-NLS-1$
        menuAux.setDynamic(true);
        menuAux.setBorderColor(borderColor);
        menuOptionsAudio.addItem(menuAux);
        menuOptionsAudio.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.6"), null, CommandPanel.COMMAND_MM_SWITCH_FX, null, null, null, textColor); //$NON-NLS-1$
        menuAux.setDynamic(true);
        menuAux.setBorderColor(borderColor);
        menuOptionsAudio.addItem(menuAux);
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.57"), null, CommandPanel.COMMAND_MM_ADD_FX_VOLUME, null, null, null, textColor); //$NON-NLS-1$
        menuAux.setDynamic(true);
        menuAux.setBorderColor(borderColor);
        menuOptionsAudio.addItem(menuAux);
        menuOptionsAudio.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.7"), null, CommandPanel.COMMAND_BACK, null, null, null, textColor); //$NON-NLS-1$
        menuAux.setBorderColor(borderColor);
        menuOptionsAudio.addItem(menuAux);

        // Options - Game
        SmartMenu menuOptionsGame = new SmartMenu(SmartMenu.TYPE_MENU, Messages.getString("MainMenuPanel.19"), menuOptions, null, null, null, null, textColor); //$NON-NLS-1$
        menuOptionsGame.setTrasparency(mainMenu.isTrasparency());
        menuOptionsGame.setBorderColor(borderColor);
        menuOptionsGame.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("MainMenuPanel.19"), null, null, null, null)); //$NON-NLS-1$
        menuOptionsGame.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null, null));

        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.20"), null, CommandPanel.COMMAND_MM_SWITCH_MOUSE_SCROLL, null, null, null, textColor); //$NON-NLS-1$
        menuAux.setDynamic(true);
        menuAux.setBorderColor(borderColor);
        menuOptionsGame.addItem(menuAux);
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.33"), null, CommandPanel.COMMAND_MM_SWITCH_MOUSE_SCROLL_EARS, null, null, null, textColor); //$NON-NLS-1$
        menuAux.setDynamic(true);
        menuAux.setBorderColor(borderColor);
        menuOptionsGame.addItem(menuAux);
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.72"), null, CommandPanel.COMMAND_MM_SWITCH_MOUSE_2D_CUBES, null, null, null, textColor); //$NON-NLS-1$
        menuAux.setDynamic(true);
        menuAux.setBorderColor(borderColor);
        menuOptionsGame.addItem(menuAux);
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.27"), null, CommandPanel.COMMAND_MM_SWITCH_DISABLE_ITEMS, null, null, null, textColor); //$NON-NLS-1$
        menuAux.setDynamic(true);
        menuAux.setBorderColor(borderColor);
        menuOptionsGame.addItem(menuAux);

        if (TownsProperties.GODS_ACTIVATED) {
            menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.77"), null, CommandPanel.COMMAND_MM_SWITCH_DISABLE_GODS, null, null, null, textColor); //$NON-NLS-1$
            menuAux.setDynamic(true);
            menuAux.setBorderColor(borderColor);
            menuOptionsGame.addItem(menuAux);
        }

        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.28"), null, CommandPanel.COMMAND_MM_SWITCH_PAUSE, null, null, null, textColor); //$NON-NLS-1$
        menuAux.setDynamic(true);
        menuAux.setBorderColor(borderColor);
        menuOptionsGame.addItem(menuAux);
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.31"), null, CommandPanel.COMMAND_MM_SWITCH_AUTOSAVE_DAYS, null, null, null, textColor); //$NON-NLS-1$
        menuAux.setDynamic(true);
        menuAux.setBorderColor(borderColor);
        menuOptionsGame.addItem(menuAux);
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.32"), null, CommandPanel.COMMAND_MM_SWITCH_SIEGES, null, null, null, textColor); //$NON-NLS-1$
        menuAux.setDynamic(true);
        menuAux.setBorderColor(borderColor);
        menuOptionsGame.addItem(menuAux);
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.36"), null, CommandPanel.COMMAND_MM_SWITCH_SIEGE_PAUSE, null, null, null, textColor); //$NON-NLS-1$
        menuAux.setDynamic(true);
        menuAux.setBorderColor(borderColor);
        menuOptionsGame.addItem(menuAux);
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.66"), null, CommandPanel.COMMAND_MM_SWITCH_CARAVAN_PAUSE, null, null, null, textColor); //$NON-NLS-1$
        menuAux.setDynamic(true);
        menuAux.setBorderColor(borderColor);
        menuOptionsGame.addItem(menuAux);
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.62"), null, CommandPanel.COMMAND_MM_SWITCH_BURY, null, null, null, textColor); //$NON-NLS-1$
        menuAux.setDynamic(true);
        menuAux.setBorderColor(borderColor);
        menuOptionsGame.addItem(menuAux);
        menuOptionsGame.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.7"), null, CommandPanel.COMMAND_BACK, null, null, null, textColor); //$NON-NLS-1$
        menuAux.setBorderColor(borderColor);
        menuOptionsGame.addItem(menuAux);

        // Options - Controls
        SmartMenu menuOptionsControls = new SmartMenu(SmartMenu.TYPE_MENU, Messages.getString("MainMenuPanel.41"), menuOptions, null, null, null, null, textColor); //$NON-NLS-1$
        menuOptionsControls.setTrasparency(mainMenu.isTrasparency());
        menuOptionsControls.setBorderColor(borderColor);
        menuOptionsControls.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("MainMenuPanel.41"), null, null, null, null)); //$NON-NLS-1$
        menuOptionsControls.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null, null));

        menuOptionsControls.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("MainMenuPanel.43"), null, null, null, null, null, creditsColor)); //$NON-NLS-1$
        menuOptionsControls.addItem(createKeyboardMenu(UtilsKeyboard.FN_UP, textColor, borderColor));
        menuOptionsControls.addItem(createKeyboardMenu(UtilsKeyboard.FN_DOWN, textColor, borderColor));
        menuOptionsControls.addItem(createKeyboardMenu(UtilsKeyboard.FN_LEFT, textColor, borderColor));
        menuOptionsControls.addItem(createKeyboardMenu(UtilsKeyboard.FN_RIGHT, textColor, borderColor));
        menuOptionsControls.addItem(createKeyboardMenu(UtilsKeyboard.FN_LEVEL_UP, textColor, borderColor));
        menuOptionsControls.addItem(createKeyboardMenu(UtilsKeyboard.FN_LEVEL_DOWN, textColor, borderColor));
        menuOptionsControls.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null, null));
        menuOptionsControls.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("MainMenuPanel.44"), null, null, null, null, null, creditsColor)); //$NON-NLS-1$
        menuOptionsControls.addItem(createKeyboardMenu(UtilsKeyboard.FN_SHOW_MISSION, textColor, borderColor));
        menuOptionsControls.addItem(createKeyboardMenu(UtilsKeyboard.FN_SHOW_STOCK, textColor, borderColor));
        menuOptionsControls.addItem(createKeyboardMenu(UtilsKeyboard.FN_SHOW_PRIORITIES, textColor, borderColor));
        menuOptionsControls.addItem(createKeyboardMenu(UtilsKeyboard.FN_SHOW_TRADE, textColor, borderColor));
        menuOptionsControls.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null, null));
        menuOptionsControls.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("MainMenuPanel.45"), null, null, null, null, null, creditsColor)); //$NON-NLS-1$
        menuOptionsControls.addItem(createKeyboardMenu(UtilsKeyboard.FN_NEXT_CITIZEN, textColor, borderColor));
        menuOptionsControls.addItem(createKeyboardMenu(UtilsKeyboard.FN_PREVIOUS_CITIZEN, textColor, borderColor));
        menuOptionsControls.addItem(createKeyboardMenu(UtilsKeyboard.FN_NEXT_SOLDIER, textColor, borderColor));
        menuOptionsControls.addItem(createKeyboardMenu(UtilsKeyboard.FN_PREVIOUS_SOLDIER, textColor, borderColor));
        menuOptionsControls.addItem(createKeyboardMenu(UtilsKeyboard.FN_NEXT_HERO, textColor, borderColor));
        menuOptionsControls.addItem(createKeyboardMenu(UtilsKeyboard.FN_PREVIOUS_HERO, textColor, borderColor));
        menuOptionsControls.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null, null));
        menuOptionsControls.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("MainMenuPanel.46"), null, null, null, null, null, creditsColor)); //$NON-NLS-1$
        menuOptionsControls.addItem(createKeyboardMenu(UtilsKeyboard.FN_TOGGLE_FULLSCREEN, textColor, borderColor));
        menuOptionsControls.addItem(createKeyboardMenu(UtilsKeyboard.FN_TOGGLE_FLAT_MOUSE, textColor, borderColor));
        menuOptionsControls.addItem(createKeyboardMenu(UtilsKeyboard.FN_TOGGLE_3D_MOUSE, textColor, borderColor));
        menuOptionsControls.addItem(createKeyboardMenu(UtilsKeyboard.FN_TOGGLE_GRID, textColor, borderColor));
        menuOptionsControls.addItem(createKeyboardMenu(UtilsKeyboard.FN_TOGGLE_MINIBLOCKS, textColor, borderColor));
        menuOptionsControls.addItem(createKeyboardMenu(UtilsKeyboard.FN_TOGGLE_HIDE_UI, textColor, borderColor));
        menuOptionsControls.addItem(createKeyboardMenu(UtilsKeyboard.FN_TOGGLE_ITEM_BUILD_FACE, textColor, borderColor));
        menuOptionsControls.addItem(createKeyboardMenu(UtilsKeyboard.FN_PAUSE, textColor, borderColor));
        menuOptionsControls.addItem(createKeyboardMenu(UtilsKeyboard.FN_SPEED_UP, textColor, borderColor));
        menuOptionsControls.addItem(createKeyboardMenu(UtilsKeyboard.FN_SPEED_DOWN, textColor, borderColor));
        menuOptionsControls.addItem(createKeyboardMenu(UtilsKeyboard.FN_SCREENSHOT, textColor, borderColor));
        menuOptionsControls.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null, null));
        menuOptionsControls.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("MainMenuPanel.61"), null, null, null, null, null, creditsColor)); //$NON-NLS-1$
        menuOptionsControls.addItem(createKeyboardMenu(UtilsKeyboard.FN_BOT_1, textColor, borderColor));
        menuOptionsControls.addItem(createKeyboardMenu(UtilsKeyboard.FN_BOT_2, textColor, borderColor));
        menuOptionsControls.addItem(createKeyboardMenu(UtilsKeyboard.FN_BOT_3, textColor, borderColor));
        menuOptionsControls.addItem(createKeyboardMenu(UtilsKeyboard.FN_BOT_4, textColor, borderColor));
        menuOptionsControls.addItem(createKeyboardMenu(UtilsKeyboard.FN_BOT_5, textColor, borderColor));
        menuOptionsControls.addItem(createKeyboardMenu(UtilsKeyboard.FN_BOT_6, textColor, borderColor));
        menuOptionsControls.addItem(createKeyboardMenu(UtilsKeyboard.FN_BOT_7, textColor, borderColor));
        menuOptionsControls.addItem(createKeyboardMenu(UtilsKeyboard.FN_BOT_8, textColor, borderColor));
        menuOptionsControls.addItem(createKeyboardMenu(UtilsKeyboard.FN_BOT_9, textColor, borderColor));
        menuOptionsControls.addItem(createKeyboardMenu(UtilsKeyboard.FN_BOT_10, textColor, borderColor));

        menuOptionsControls.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.7"), null, CommandPanel.COMMAND_BACK, null, null, null, textColor); //$NON-NLS-1$
        menuAux.setBorderColor(borderColor);
        menuOptionsControls.addItem(menuAux);

        // Options - Performance
        SmartMenu menuOptionsPerformance = new SmartMenu(SmartMenu.TYPE_MENU, Messages.getString("MainMenuPanel.8"), menuOptions, null, null, null, null, textColor); //$NON-NLS-1$
        menuOptionsPerformance.setTrasparency(mainMenu.isTrasparency());
        menuOptionsPerformance.setBorderColor(borderColor);
        menuOptionsPerformance.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("MainMenuPanel.8"), null, null, null, null)); //$NON-NLS-1$
        menuOptionsPerformance.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null, null));

        menuOptionsPerformance.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("MainMenuPanel.59"), null, null, null, null, null, Color.LIGHT_GRAY)); //$NON-NLS-1$
        menuOptionsPerformance.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("MainMenuPanel.64"), null, null, null, null, null, Color.LIGHT_GRAY)); //$NON-NLS-1$
        menuOptionsPerformance.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("MainMenuPanel.65"), null, null, null, null, null, Color.LIGHT_GRAY)); //$NON-NLS-1$
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.63"), null, CommandPanel.COMMAND_MM_SWITCH_PATHFINDING_LEVEL, null, null, null, textColor); //$NON-NLS-1$
        menuAux.setDynamic(true);
        menuAux.setBorderColor(borderColor);
        menuOptionsPerformance.addItem(menuAux);
        menuOptionsPerformance.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.7"), null, CommandPanel.COMMAND_BACK, null, null, null, textColor); //$NON-NLS-1$
        menuAux.setBorderColor(borderColor);
        menuOptionsPerformance.addItem(menuAux);

        // Options - Language
        ArrayList<LanguageData> alLanguages = Utils.getLanguages();
        SmartMenu menuOptionsLanguage = null;
        if (alLanguages != null && alLanguages.size() > 1) {
            menuOptionsLanguage = new SmartMenu(SmartMenu.TYPE_MENU, Messages.getString("MainMenuPanel.14"), menuOptions, null, null, null, null, textColor); //$NON-NLS-1$
            menuOptionsLanguage.setTrasparency(mainMenu.isTrasparency());
            menuOptionsLanguage.setBorderColor(borderColor);
            menuOptionsLanguage.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("MainMenuPanel.14"), null, null, null, null)); //$NON-NLS-1$
            menuOptionsLanguage.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null, null));

            // Languages
            for (int l = 0; l < alLanguages.size(); l++) {
                LanguageData ld = alLanguages.get(l);
                if (ld.mod == null || Game.getModsLoaded() == null) {
                    menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, ld.name, null, CommandPanel.COMMAND_CHANGE_LANGUAGE, ld.language, ld.country, null, textColor);
                } else {
                    // Buscamos el índice del mod
                    int iModIndex = -1;
                    for (int m = 0; m < Game.getModsLoaded().size(); m++) {
                        if (Game.getModsLoaded().get(m).equals(ld.mod)) {
                            iModIndex = m;
                            break;
                        }
                    }
                    if (iModIndex != -1) {
                        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, ld.name, null, CommandPanel.COMMAND_CHANGE_LANGUAGE, ld.language, ld.country, new Point3D(iModIndex, 0, 0), textColor);
                    } else {
                        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, ld.name, null, CommandPanel.COMMAND_CHANGE_LANGUAGE, ld.language, ld.country, null, textColor);
                    }
                }
                menuAux.setDynamic(true);
                menuAux.setBorderColor(borderColor);
                menuOptionsLanguage.addItem(menuAux);
            }

            // Back
            menuOptionsLanguage.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
            menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.7"), null, CommandPanel.COMMAND_BACK, null, null, null, textColor); //$NON-NLS-1$
            menuAux.setBorderColor(borderColor);
            menuOptionsLanguage.addItem(menuAux);
        }

        menuOptions.addItem(menuOptionsGraphics);
        menuOptions.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
        menuOptions.addItem(menuOptionsAudio);
        menuOptions.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
        menuOptions.addItem(menuOptionsGame);
        menuOptions.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
        menuOptions.addItem(menuOptionsPerformance);
        menuOptions.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
        menuOptions.addItem(menuOptionsControls);
        if (menuOptionsLanguage != null) {
            menuOptions.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
            menuOptions.addItem(menuOptionsLanguage);
        }
        menuOptions.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
        menuOptions.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
//		menuAux = new SmartMenu (SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.26"), null, CommandPanel.COMMAND_SAVE_OPTIONS, null, null, null, textColor); //$NON-NLS-1$
//		menuAux.setBorderColor (borderColor);
//		menuOptions.addItem (menuAux);
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.7"), null, CommandPanel.COMMAND_BACK, null, null, null, textColor); //$NON-NLS-1$
        menuAux.setBorderColor(borderColor);
        menuOptions.addItem(menuAux);

        mainMenu.addItem(menuOptions);

        // Blank
        mainMenu.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));

        // Credits
        SmartMenu smCredits = new SmartMenu(SmartMenu.TYPE_MENU, Messages.getString("MainMenuPanel.34"), mainMenu, null, null, null, null, textColor); //$NON-NLS-1$
        smCredits.setTrasparency(mainMenu.isTrasparency());
        smCredits.setBorderColor(borderColor);
        smCredits.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("MainMenuPanel.34"), null, null, null, null)); //$NON-NLS-1$
        smCredits.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null, null));

        menuAux = new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("MainMenuPanel.35"), null, null, null, null, null, creditsColor); //$NON-NLS-1$
        menuAux.setDynamic(true);
        menuAux.setBorderColor(borderColor);
        smCredits.addItem(menuAux);
        menuAux = new SmartMenu(SmartMenu.TYPE_TEXT, "Xavi 'supermalparit' Canal", null, null, null, null, null, textColor); //$NON-NLS-1$
        menuAux.setDynamic(true);
        menuAux.setBorderColor(borderColor);
        smCredits.addItem(menuAux);
        smCredits.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));

        menuAux = new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("MainMenuPanel.37"), null, null, null, null, null, creditsColor); //$NON-NLS-1$
        menuAux.setDynamic(true);
        menuAux.setBorderColor(borderColor);
        smCredits.addItem(menuAux);
        menuAux = new SmartMenu(SmartMenu.TYPE_TEXT, "Ben 'burningpet' Palgi", null, null, null, null, null, textColor); //$NON-NLS-1$
        menuAux.setDynamic(true);
        menuAux.setBorderColor(borderColor);
        smCredits.addItem(menuAux);
        smCredits.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));

        menuAux = new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("MainMenuPanel.39"), null, null, null, null, null, creditsColor); //$NON-NLS-1$
        menuAux.setDynamic(true);
        menuAux.setBorderColor(borderColor);
        smCredits.addItem(menuAux);
        menuAux = new SmartMenu(SmartMenu.TYPE_TEXT, "Sam 'Evilpooley' Poole", null, null, null, null, null, textColor); //$NON-NLS-1$
        menuAux.setDynamic(true);
        menuAux.setBorderColor(borderColor);
        smCredits.addItem(menuAux);
        smCredits.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));

        menuAux = new SmartMenu(SmartMenu.TYPE_TEXT, Messages.getString("MainMenuPanel.78"), null, null, null, null, null, creditsColor); //$NON-NLS-1$
        menuAux.setDynamic(true);
        menuAux.setBorderColor(borderColor);
        smCredits.addItem(menuAux);
        menuAux = new SmartMenu(SmartMenu.TYPE_TEXT, "Florian 'Moebius' Frankenberger", null, null, null, null, null, textColor); //$NON-NLS-1$
        menuAux.setDynamic(true);
        menuAux.setBorderColor(borderColor);
        smCredits.addItem(menuAux);
        smCredits.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));


        smCredits.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.7"), null, CommandPanel.COMMAND_BACK, null, null, null, textColor); //$NON-NLS-1$
        menuAux.setBorderColor(borderColor);
        smCredits.addItem(menuAux);

        mainMenu.addItem(smCredits);

        // Blank
        mainMenu.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
        mainMenu.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));

        // Exit
        menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("MainMenuPanel.3"), null, CommandPanel.COMMAND_EXIT_GAME, null, null, null, textColor); //$NON-NLS-1$
        menuAux.setBorderColor(borderColor);
        mainMenu.addItem(menuAux);

        menu.setSmartMenu(mainMenu);

        menu.setX(xMenu);
        menu.setY(yMenu);
    }

    public SmartMenu createKeyboardMenu(int iFN, Color textColor, ColorGL borderColor) {
        SmartMenu menuAux = new SmartMenu(SmartMenu.TYPE_ITEM, UtilsKeyboard.getFNHumanString(iFN) + UtilsKeyboard.getTooltip(iFN), null, CommandPanel.COMMAND_CHANGE_HOTKEY, Integer.toString(iFN), null, null, textColor);
        menuAux.setBorderColor(borderColor);
        return menuAux;
    }

    public boolean isSettingSavegameName() {
        return settingSavegameName;
    }

    public void setSettingSavegameName(boolean settingSavegameName, String sCampaign, String sMission) {
        this.settingSavegameName = settingSavegameName;
        setSaveGameCampaignID(sCampaign);
        setSaveGameMissionID(sMission);
        if (settingSavegameName) {
            new TypingPanel(renderWidth, renderHeight, Messages.getString("MainMenuPanel.47"), new String(), TypingPanel.TYPE_SAVEGAME_NAME, 0); //$NON-NLS-1$
        }
    }

    public boolean isSettingHotkey() {
        return settingHotkey != 0;
    }

    public void setSettingHotkey(boolean settingHotkey, int iFN) {
        if (settingHotkey) {
            this.settingHotkey = 1;
            new TypingPanel(renderWidth, renderHeight, Messages.getString("MainMenuPanel.48") + UtilsKeyboard.getFNHumanString(iFN) + ((UtilsKeyboard.getKey(iFN, 0) == Keyboard.KEY_NONE) ? "" : Messages.getString("MainMenuPanel.50") + Keyboard.getKeyName(UtilsKeyboard.getKey(iFN, 0)) + ")"), new String(), TypingPanel.TYPE_REDEFINE_KEYS, iFN); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        } else {
            this.settingHotkey = 0;
            // El panel se cierra, cambiamos los textos de todos los menus COMMAND_CHANGE_HOTKEY
            checkChangeHotkeyMenusText(menu.getSmartMenu());
        }
    }

    public void setSettingNewServer(boolean bSettingNewServer) {
        this.settingNewServer = bSettingNewServer;

        if (bSettingNewServer) {
            new TypingPanel(renderWidth, renderHeight, Messages.getString("MainMenuPanel.76"), new String(), TypingPanel.TYPE_ADD_SERVER, 0); //$NON-NLS-1$
        }
    }

    public boolean isSettingNewServer() {
        return settingNewServer;
    }

    /**
     * Cambia los textos de todos los menus COMMAND_CHANGE_HOTKEY Se llama
     * después deredifinir alguna tecla.
     *
     */
    private void checkChangeHotkeyMenusText(SmartMenu sm) {
        if (sm.getType() == SmartMenu.TYPE_MENU) {
            for (int i = 0; i < sm.getItems().size(); i++) {
                checkChangeHotkeyMenusText(sm.getItems().get(i));
            }
        } else if (sm.getType() == SmartMenu.TYPE_ITEM && sm.getCommand() != null && sm.getCommand().equals(CommandPanel.COMMAND_CHANGE_HOTKEY)) {
            // Bingo, cambiamos el texto
            int iFN = Integer.parseInt(sm.getParameter());
            sm.setName(UtilsKeyboard.getFNHumanString(iFN) + UtilsKeyboard.getTooltip(iFN));
        }
    }

    public void render() {
        GL11.glColor4f(1, 1, 1, 1);

        int iMaxSize = renderWidth;
        if (renderHeight > iMaxSize) {
            iMaxSize = renderHeight;
        }
        if (iMaxSize % 2 != 0) {
            iMaxSize++;
        }
        iMaxSize /= 2;

        int centerX = (renderWidth - renderX) / 2;
        int centerY = (renderHeight - renderY) / 2;

        if (startingGame > 0) {
            // Pintamos el logo
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, TEXTURE_SMP_LOGO_ID);
            GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
            UtilsGL.glBegin(GL11.GL_QUADS);
            UtilsGL.drawTexture(centerX - iMaxSize, centerY - iMaxSize, centerX + iMaxSize, centerY + iMaxSize, 0, 0, 1, 1, startingGameColor);
            UtilsGL.glEnd();
            return;
        } else {
            if (deleteLogoTexture) {
                deleteLogoTexture = false;

                // Descargamos el logo de la memoria
                GL11.glDeleteTextures(TEXTURE_SMP_LOGO_ID);
            }
        }

        // Pintamos el fondo
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, TEXTURE_MAIN_MENU_ID);
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE);
        UtilsGL.glBegin(GL11.GL_QUADS);
        UtilsGL.drawTexture(centerX - iMaxSize, centerY - iMaxSize, centerX + iMaxSize, centerY + iMaxSize, 0, 0, 1, 1);
        UtilsGL.glEnd();

        // Pintamos el logo Towns
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, TEXTURE_TOWNS_LOGO_ID);
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE);
        UtilsGL.glBegin(GL11.GL_QUADS);
        UtilsGL.drawTexture(centerX - imageTownsLogoWidth / 2, centerY - 10 - imageTownsLogoHeight, centerX + imageTownsLogoWidth / 2, centerY - 10, 0, 0, 1, 1);
        UtilsGL.glEnd();

        boolean bTextureFontLoaded = false;
        if (loadingGame) {
            // Pintamos el loading
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, TEXTURE_LOADING_ID);
            GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE);
            UtilsGL.glBegin(GL11.GL_QUADS);
            UtilsGL.drawTexture(centerX - imageLoadingWidth / 2, centerY + 10, centerX + imageLoadingWidth / 2, centerY + 10 + imageLoadingHeight, 0, 0, 1, 1);
            UtilsGL.glEnd();

            // Si hay texto de loading lo pintamos también
            String sLoadingText = getLoadingText();
            if (sLoadingText.length() > 0) {
                bTextureFontLoaded = true;
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
                GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

                UtilsGL.glBegin(GL11.GL_QUADS);
                UtilsGL.drawStringWithBorder(sLoadingText, centerX - (UtilFont.getWidth(sLoadingText) / 2), centerY + 20 + imageLoadingHeight, COLORGL_WHITE, COLORGL_BLACK);
                UtilsGL.glEnd();
            }
        }

        // Versión del juego abajo a la derecha
        if (!bTextureFontLoaded) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
            GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
        }
        UtilsGL.glBegin(GL11.GL_QUADS);
        String sVersion = TownsProperties.GAME_VERSION;
        if (TownsProperties.DEMO_VERSION) {
            sVersion += " (demo)"; //$NON-NLS-1$
        }
        UtilsGL.drawStringWithBorder(sVersion, renderX + renderWidth - UtilFont.getWidth(sVersion) - 10, renderY + renderHeight - UtilFont.MAX_HEIGHT - 10, COLORGL_WHITE, COLORGL_BLACK);

        // Texto a la izquierda
        if (TownsProperties.DEMO_VERSION) {
            UtilsGL.drawStringWithBorder(Messages.getString("MainMenuPanel.23"), 11, renderY + renderHeight - UtilFont.MAX_HEIGHT - 10, COLORGL_WHITE, COLORGL_BLACK); //$NON-NLS-1$
        }
        UtilsGL.glEnd();

        if (!loadingGame && !isSettingSavegameName() && !isSettingHotkey() && !isSettingNewServer()) {
            // Pintamos el menú
            menu.render();
        }

        if (isSettingSavegameName() || isSettingHotkey() || isSettingNewServer()) {
            int mouseX = Mouse.getEventX();
            int mouseY = UtilsGL.getHeight() - Mouse.getEventY() - 1;
            TypingPanel.render(mouseX, mouseY);
        }
    }

    /**
     * Método que se llama cuando pulse con el ratón.
     *
     * @param x Coordenada X
     * @param y Coordenada Y
     */
    public void mousePressed(int x, int y, int mouseButton) {
        if (startingGame > 0) {
            startingGame = 0;
        } else {
            if (mouseButton == 0) {
                if (isSettingSavegameName()) {
                    // Ha pulsado en algún sitio mientras el panel de savegame name está abierto
                    int iMousePanel = TypingPanel.whereIsMouse(x, y);
                    if (iMousePanel == UIPanel.MOUSE_TYPING_PANEL_CLOSE) {
                        // Cerramos
                        setSettingSavegameName(false, null, null);
                    } else if (iMousePanel == UIPanel.MOUSE_TYPING_PANEL_CONFIRM) {
                        if (TypingPanel.getNewText() != null && TypingPanel.getNewText().length() > 0) {
                            // Confirmamos y empieza la partida
                            if (!Utils.existsSavegame(TypingPanel.getNewText())) { // Sólo si no existe en disco previamente
                                startGame(TypingPanel.getNewText());
                            }
                        }
                    }
                } else if (isSettingHotkey()) {
                    // Ha pulsado en algún sitio mientras el panel de hotkeys está abierto
                    int iMousePanel = TypingPanel.whereIsMouse(x, y);
                    if (iMousePanel == UIPanel.MOUSE_TYPING_PANEL_CLOSE) {
                        // Cerramos
                        setSettingHotkey(false, 0);
                    }
                } else if (isSettingNewServer()) {
                    // Ha pulsado en algún sitio mientras el panel de new server está abierto
                    int iMousePanel = TypingPanel.whereIsMouse(x, y);
                    if (iMousePanel == UIPanel.MOUSE_TYPING_PANEL_CLOSE) {
                        // Cerramos
                        setSettingNewServer(false);
                    } else if (iMousePanel == UIPanel.MOUSE_TYPING_PANEL_CONFIRM) {
                        if (TypingPanel.getNewText() != null && TypingPanel.getNewText().length() > 0) {
                            // Confirmamos
                            Game.addServer(TypingPanel.getNewText());
                            Utils.saveOptions();

                            setSettingNewServer(false);
                            createMenu();
                        }
                    }
                } else if (!loadingGame) {
                    menu.mousePressed(x - xMenu, y - yMenu);
                }
            }
        }
    }

    /**
     * Método llamado al pulsar una tecla cuando estamos en el main menu
     *
     * @param iKey
     */
    public void keyPressed(int iKey) {
        if (isSettingSavegameName()) {
            if (TypingPanel.keyPressed(iKey)) {
                // Ya ha acabado (o ha pulsado ESC)
                if (TypingPanel.getNewText() != null && TypingPanel.getNewText().length() > 0) {
                    // Todo ok, toca empezar la partida (sólo si la partida no existe previamente en disco)
                    if (!Utils.existsSavegame(TypingPanel.getNewText())) {
                        startGame(TypingPanel.getNewText());
                    }
                } else {
                    setSettingSavegameName(false, null, null);
                }
            }
        } else if (isSettingHotkey()) {
            if (TypingPanel.keyPressed(iKey)) {
                // Ya ha acabado (o ha pulsado ESC)
                if (TypingPanel.getNewText() != null && TypingPanel.getNewText().length() > 0) {
                    // Key pulsada, la seteamos y saltamos a la siguiente (si hace falta)
                    if (settingHotkey == 1) {
                        UtilsKeyboard.redefineKey(0, TypingPanel.TYPING_PARAMETER, Integer.parseInt(TypingPanel.getNewText()));
                        settingHotkey = 2;
                        TypingPanel.setTitle(Messages.getString("MainMenuPanel.52") + UtilsKeyboard.getFNHumanString(TypingPanel.TYPING_PARAMETER) + ((UtilsKeyboard.getKey(TypingPanel.TYPING_PARAMETER, 0) == Keyboard.KEY_NONE) ? "" : Messages.getString("MainMenuPanel.50") + Keyboard.getKeyName(UtilsKeyboard.getKey(TypingPanel.TYPING_PARAMETER, 1)) + ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                        TypingPanel.setNewText(new String());
                    } else {
                        // Key 2
                        UtilsKeyboard.redefineKey(1, TypingPanel.TYPING_PARAMETER, Integer.parseInt(TypingPanel.getNewText()));
                        setSettingHotkey(false, 0);
                    }
                } else {
                    // Ha pulsado ESC, borramos hotkeys
                    if (settingHotkey == 1) {
                        UtilsKeyboard.redefineKey(0, TypingPanel.TYPING_PARAMETER, Keyboard.KEY_NONE);
                        UtilsKeyboard.redefineKey(1, TypingPanel.TYPING_PARAMETER, Keyboard.KEY_NONE);
                    } else {
                        // Key 2
                        UtilsKeyboard.redefineKey(1, TypingPanel.TYPING_PARAMETER, Keyboard.KEY_NONE);
                    }

                    setSettingHotkey(false, 0);
                }
            }
        } else if (isSettingNewServer()) {
            if (TypingPanel.keyPressed(iKey)) {
                // Ya ha acabado (o ha pulsado ESC)
                if (TypingPanel.getNewText() != null && TypingPanel.getNewText().length() > 0) {
                    Game.addServer(TypingPanel.getNewText());
                    Utils.saveOptions();

                    createMenu();
                }

                setSettingNewServer(false);
            }
        }
    }

    private void startGame(String savegameName) {
        setSettingSavegameName(false, getSaveGameCampaignID(), getSaveGameMissionID());
        CommandPanel.executeCommand(CommandPanel.COMMAND_MM_NEWGAME, getSaveGameCampaignID(), getSaveGameMissionID(), null, null, 0);
        Game.setSavegameName(savegameName);
        useBuryTemporary = true;
    }

    public void setActive(boolean bActive) {
        if (bActive) {
            createMenu();
        }
        active = bActive;
    }

    public void setLoadingText(String sLoadingText) {
        loadingText = sLoadingText;
        if (sLoadingText.length() > 0) {
            if (isActive()) {
                Game.getPanelMainMenu().render();
                // Updateamos la pantalla / ventana
                Display.update();
                Display.sync(Game.FPS_MAINMENU); // Para "capear" a 30 fps
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_ACCUM_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
            }
        }
    }

    public String getLoadingText() {
        return loadingText;
    }

    public boolean isActive() {
        return active;
    }

    public void resize(int renderX, int renderY, int renderWidth, int renderHeight) {
        this.renderX = renderX;
        this.renderY = renderY;
        this.renderWidth = renderWidth;
        this.renderHeight = renderHeight;
        this.xMenu = 20;
        this.yMenu = 20;

        if (menu != null) {
            menu.setHeight(MainFrame.MIN_HEIGHT - UtilFont.MAX_HEIGHT * 8);
            menu.setX(xMenu);
            menu.setY(yMenu);
            menu.resize();
        }
    }

    public void run() {
        // Starting
        while (startingGame > 0) {
            try {
                startingGame--;
                Thread.sleep(48);
            } catch (Exception e) {
            }

            if (startingGame < 25) {
                startingGameColor.r = ((startingGame - 5) * 5f) / 100f;
                startingGameColor.g = ((startingGame - 5) * 5f) / 100f;
                startingGameColor.b = ((startingGame - 5) * 5f) / 100f;
            }
        }

        startingGame = 0;
        deleteLogoTexture = true;
    }

    public void setSaveGameCampaignID(String saveGameCampaignID) {
        this.saveGameCampaignID = saveGameCampaignID;
    }

    public String getSaveGameCampaignID() {
        return saveGameCampaignID;
    }

    public void setSaveGameMissionID(String saveGameMissionID) {
        this.saveGameMissionID = saveGameMissionID;
    }

    public String getSaveGameMissionID() {
        return saveGameMissionID;
    }

    /**
     * @param errorToShow the errorToShow to set
     */
    public void setErrorToShow(String errorToShow) {
        this.errorToShow = errorToShow;
    }

    /**
     * @return the errorToShow
     */
    public String getErrorToShow() {
        return errorToShow;
    }
}
