package xaos.panels;

import java.awt.Color;
import java.io.File;

import xaos.platform.lwjgl3.input.Keyboard;
import xaos.platform.lwjgl3.input.Mouse;
import xaos.platform.lwjgl3.opengl.Display;
import org.lwjgl.opengl.GL11;
import xaos.property.PropertyFile;
import xaos.Towns;
import xaos.TownsProperties;
import xaos.main.Game;
import xaos.panels.menus.ContextMenu;
import xaos.panels.menus.MenuDefinition;
import xaos.panels.menus.MenuManager;
import xaos.panels.menus.SmartMenu;
import xaos.utils.ColorGL;
import xaos.utils.Log;
import xaos.utils.Messages;
import xaos.utils.TextureData;
import xaos.utils.UtilFont;
import xaos.utils.Utils;
import xaos.utils.UtilsGL;
import xaos.utils.UtilsKeyboard;
import static xaos.panels.UI.UIPanelState.*;

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

    public static Color textColor = Color.WHITE;
    public static Color creditsColor = Color.YELLOW;
    public static ColorGL borderColor = new ColorGL(Color.BLACK);

    public static float startingGame = 80f;
    public static boolean deleteLogoTexture = false;
    public static boolean loadingGame = false;
    public static boolean useBuryTemporary = true;
    public static ColorGL startingGameColor;
    private String loadingText = new String();

    private boolean settingSavegameName = false;
    private int settingHotkey = 0; // Entero, 0 = cerrado, 1 = abierto y seteando la primera hotkey, 2 = abierto y
                                   // seteando la segunda hotkey
    private boolean settingNewServer = false;
    private String saveGameCampaignID;
    private String saveGameMissionID;

    private String errorToShow = null;

    public MainMenuPanel(int renderX, int renderY, int renderWidth, int renderHeight) {
        resize(renderX, renderY, renderWidth, renderHeight);

        loadMenuTexture(false);

        TextureData textureSMPLogo = UtilsGL.loadTexture(
                Towns.getPropertiesString(PropertyFile.PROPERTY_FILE_GRAPHICS, "SMP_LOGO_FILE"), GL11.GL_MODULATE); //$NON-NLS-1$
        if (textureSMPLogo == null) {
            Log.log(Log.LEVEL_ERROR, Messages.getString("MainMenuPanel.21"), getClass().getName()); //$NON-NLS-1$
            Game.exit();
        }
        TEXTURE_SMP_LOGO_ID = textureSMPLogo.getTextureID();

        TextureData textureLoading = UtilsGL.loadTexture(
                Towns.getPropertiesString(PropertyFile.PROPERTY_FILE_GRAPHICS, "LOADING_FILE"), GL11.GL_REPLACE); //$NON-NLS-1$
        if (textureLoading == null) {
            Log.log(Log.LEVEL_ERROR, Messages.getString("MainMenuPanel.24"), getClass().getName()); //$NON-NLS-1$
            Game.exit();
        }
        TEXTURE_LOADING_ID = textureLoading.getTextureID();

        imageLoadingWidth = textureLoading.getWidth();
        imageLoadingHeight = textureLoading.getHeight();

        textureLoading = UtilsGL.loadTexture(
                Towns.getPropertiesString(PropertyFile.PROPERTY_FILE_GRAPHICS, "TOWNS_LOGO_FILE"), GL11.GL_REPLACE); //$NON-NLS-1$ //$NON-NLS-2$
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
        final TextureData textureMainMenu = UtilsGL.loadTexture(
                Towns.getPropertiesString(PropertyFile.PROPERTY_FILE_GRAPHICS, "MAINMENU_BG_FILE"), GL11.GL_REPLACE, //$NON-NLS-1$
                true);
        if (textureMainMenu == null) {
            Log.log(Log.LEVEL_ERROR, Messages.getString("MainMenuPanel.0"), getClass().getName()); //$NON-NLS-1$
            Game.exit();
        }
        TEXTURE_MAIN_MENU_ID = textureMainMenu.getTextureID(); // $NON-NLS-1$ //$NON-NLS-2$
    }

    // MAIN MENU
    public void createMenu() {
        setLoadingText(new String());
        loadingGame = false;
        MenuManager menuManager = new MenuManager();
        menuManager.loadMenus(new File("data/menus/main"));

        MenuDefinition menuRoot = menuManager.getMenu("main.root");

        SmartMenu mainMenu = menuManager.buildSmartMenu(menuRoot, null);

        menu = new ContextMenu();
        menu.setHeight(MainFrame.MIN_HEIGHT - UtilFont.MAX_HEIGHT * 8);

        menu.setSmartMenu(mainMenu);

        menu.setX(xMenu);
        menu.setY(yMenu);
    }

    public static SmartMenu createKeyboardMenu(int iFN, Color textColor, ColorGL borderColor) {
        SmartMenu menuAux = new SmartMenu(SmartMenu.TYPE_ITEM,
                UtilsKeyboard.getFNHumanString(iFN) + UtilsKeyboard.getTooltip(iFN), null,
                CommandPanel.COMMAND_CHANGE_HOTKEY, Integer.toString(iFN), null, null, textColor);
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
            new TypingPanel(renderWidth, renderHeight, Messages.getString("MainMenuPanel.47"), new String(), //$NON-NLS-1$
                    TypingPanel.TYPE_SAVEGAME_NAME, 0);
        }
    }

    public boolean isSettingHotkey() {
        return settingHotkey != 0;
    }

    public void setSettingHotkey(boolean settingHotkey, int iFN) {
        if (settingHotkey) {
            this.settingHotkey = 1;
            new TypingPanel(renderWidth, renderHeight,
                    Messages.getString("MainMenuPanel.48") + UtilsKeyboard.getFNHumanString(iFN) //$NON-NLS-1$
                            + ((UtilsKeyboard.getKey(iFN, 0) == Keyboard.KEY_NONE) ? "" //$NON-NLS-1$
                                    : Messages.getString("MainMenuPanel.50") //$NON-NLS-1$
                                            + Keyboard.getKeyName(UtilsKeyboard.getKey(iFN, 0)) + ")"), //$NON-NLS-1$
                    new String(), TypingPanel.TYPE_REDEFINE_KEYS, iFN);
        } else {
            this.settingHotkey = 0;
            // El panel se cierra, cambiamos los textos de todos los menus
            // COMMAND_CHANGE_HOTKEY
            checkChangeHotkeyMenusText(menu.getSmartMenu());
        }
    }

    public void setSettingNewServer(boolean bSettingNewServer) {
        this.settingNewServer = bSettingNewServer;

        if (bSettingNewServer) {
            new TypingPanel(renderWidth, renderHeight, Messages.getString("MainMenuPanel.76"), new String(), //$NON-NLS-1$
                    TypingPanel.TYPE_ADD_SERVER, 0);
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
        } else if (sm.getType() == SmartMenu.TYPE_ITEM && sm.getCommand() != null
                && sm.getCommand().equals(CommandPanel.COMMAND_CHANGE_HOTKEY)) {
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
            UtilsGL.drawTexture(centerX - iMaxSize, centerY - iMaxSize, centerX + iMaxSize, centerY + iMaxSize, 0, 0, 1,
                    1, startingGameColor);
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
        UtilsGL.drawTexture(centerX - imageTownsLogoWidth / 2, centerY - 10 - imageTownsLogoHeight,
                centerX + imageTownsLogoWidth / 2, centerY - 10, 0, 0, 1, 1);
        UtilsGL.glEnd();

        boolean bTextureFontLoaded = false;
        if (loadingGame) {
            // Pintamos el loading
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, TEXTURE_LOADING_ID);
            GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE);
            UtilsGL.glBegin(GL11.GL_QUADS);
            UtilsGL.drawTexture(centerX - imageLoadingWidth / 2, centerY + 10, centerX + imageLoadingWidth / 2,
                    centerY + 10 + imageLoadingHeight, 0, 0, 1, 1);
            UtilsGL.glEnd();

            // Si hay texto de loading lo pintamos también
            String sLoadingText = getLoadingText();
            if (sLoadingText.length() > 0) {
                bTextureFontLoaded = true;
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
                GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

                UtilsGL.glBegin(GL11.GL_QUADS);
                UtilsGL.drawStringWithBorder(sLoadingText, centerX - (UtilFont.getWidth(sLoadingText) / 2),
                        centerY + 20 + imageLoadingHeight, COLORGL_WHITE, COLORGL_BLACK);
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
        UtilsGL.drawStringWithBorder(sVersion, renderX + renderWidth - UtilFont.getWidth(sVersion) - 10,
                renderY + renderHeight - UtilFont.MAX_HEIGHT - 10, COLORGL_WHITE, COLORGL_BLACK);

        // Texto a la izquierda
        if (TownsProperties.DEMO_VERSION) {
            UtilsGL.drawStringWithBorder(Messages.getString("MainMenuPanel.23"), 11, //$NON-NLS-1$
                    renderY + renderHeight - UtilFont.MAX_HEIGHT - 10, COLORGL_WHITE, COLORGL_BLACK);
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
                    if (iMousePanel == MOUSE_TYPING_PANEL_CLOSE) {
                        // Cerramos
                        setSettingSavegameName(false, null, null);
                    } else if (iMousePanel == MOUSE_TYPING_PANEL_CONFIRM) {
                        if (TypingPanel.getNewText() != null && TypingPanel.getNewText().length() > 0) {
                            // Confirmamos y empieza la partida
                            if (!Utils.existsSavegame(TypingPanel.getNewText())) { // Sólo si no existe en disco
                                                                                   // previamente
                                startGame(TypingPanel.getNewText());
                            }
                        }
                    }
                } else if (isSettingHotkey()) {
                    // Ha pulsado en algún sitio mientras el panel de hotkeys está abierto
                    int iMousePanel = TypingPanel.whereIsMouse(x, y);
                    if (iMousePanel == MOUSE_TYPING_PANEL_CLOSE) {
                        // Cerramos
                        setSettingHotkey(false, 0);
                    }
                } else if (isSettingNewServer()) {
                    // Ha pulsado en algún sitio mientras el panel de new server está abierto
                    int iMousePanel = TypingPanel.whereIsMouse(x, y);
                    if (iMousePanel == MOUSE_TYPING_PANEL_CLOSE) {
                        // Cerramos
                        setSettingNewServer(false);
                    } else if (iMousePanel == MOUSE_TYPING_PANEL_CONFIRM) {
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
                    // Todo ok, toca empezar la partida (sólo si la partida no existe previamente en
                    // disco)
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
                        UtilsKeyboard.redefineKey(0, TypingPanel.TYPING_PARAMETER,
                                Integer.parseInt(TypingPanel.getNewText()));
                        settingHotkey = 2;
                        TypingPanel
                                .setTitle(Messages.getString("MainMenuPanel.52") //$NON-NLS-1$
                                        + UtilsKeyboard.getFNHumanString(TypingPanel.TYPING_PARAMETER)
                                        + ((UtilsKeyboard.getKey(TypingPanel.TYPING_PARAMETER, 0) == Keyboard.KEY_NONE)
                                                ? "" //$NON-NLS-1$
                                                : Messages.getString("MainMenuPanel.50") //$NON-NLS-1$
                                                        + Keyboard.getKeyName(
                                                                UtilsKeyboard.getKey(TypingPanel.TYPING_PARAMETER, 1))
                                                        + ")")); //$NON-NLS-1$
                        TypingPanel.setNewText(new String());
                    } else {
                        // Key 2
                        UtilsKeyboard.redefineKey(1, TypingPanel.TYPING_PARAMETER,
                                Integer.parseInt(TypingPanel.getNewText()));
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
        CommandPanel.executeCommand(CommandPanel.COMMAND_MM_NEWGAME, getSaveGameCampaignID(), getSaveGameMissionID(),
                null, null, 0);
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
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_ACCUM_BUFFER_BIT
                        | GL11.GL_STENCIL_BUFFER_BIT);
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
