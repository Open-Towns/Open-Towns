package xaos.panels;

import java.awt.Point;

import xaos.platform.lwjgl3.input.Keyboard;
import org.lwjgl.opengl.GL11;

import xaos.main.Game;
import xaos.panels.UI.UIPanel;
import xaos.panels.UI.UIPanelInputHandler;
import xaos.tiles.Tile;
import xaos.utils.ColorGL;
import xaos.utils.Messages;
import xaos.utils.UtilFont;
import xaos.utils.Utils;
import xaos.utils.UtilsGL;
import static xaos.panels.UI.UIPanelState.*;
import static xaos.panels.UI.UIPanelInputHandler.*;
import static xaos.panels.UI.UIPanel.*;

public class TypingPanel {

    public static final int MAX_CHARS = 48;

    public static final int TYPE_NONE = 0;
    public static final int TYPE_RENAME_GROUP = 1;
    public static final int TYPE_REDEFINE_KEYS = 2;
    public static final int TYPE_SAVEGAME_NAME = 3;
    public static final int TYPE_RENAME_JOB_GROUP = 4;
    public static final int TYPE_ADD_SERVER = 5;
    public static final int TYPE_ADD_TEXT_TO_ITEM = 6;

    public static int TYPING_TYPE;
    public static int TYPING_PARAMETER;

    public static int WIDTH = 1024;
    public static int HEIGHT = 600;
    public static int WIDTH_SUBPANEL = 512;
    public static int HEIGHT_SUBPANEL = 300;

    // Text and data
    private static String title;
    private static String oldText;
    private static String newText;

    private static String subTitle;

    // Points
    private static Point panelPoint = new Point(0, 0);
    private static Point panelNewTextPoint = new Point(0, 0);
    private static Point closeButtonPoint = new Point(0, 0);
    private static Point titlePoint = new Point(0, 0);
    private static Point oldTextPoint = new Point(0, 0);
    private static Point newTextPoint = new Point(0, 0);
    private static Point subTitlePoint = new Point(0, 0);
    private static Point confirmPoint = new Point(0, 0);

    // Tiles
    private static Tile[] tilePanel;
    private static Tile[] tilePanelNewText;
    private static Tile tileConfirm;
    private static Tile tileConfirmON;
    private static boolean[][] tileConfirmAlpha;

    public TypingPanel(int renderWidth, int renderHeight, String title, String oldText, int type, int parameter) {
        setTitle(title);
        setOldText(oldText);
        setNewText(new String());
        setSubTitle(null);

        TYPING_TYPE = type;
        TYPING_PARAMETER = parameter;

        createPanel(renderWidth, renderHeight);
    }

    public static void createPanel(int renderWidth, int renderHeight) {
        if (tilePanel == null) {
            tilePanel = new Tile[9]; // Background/N/S/E/W/NE,NW,SE,SW
            tilePanel[0] = new Tile("typing_panel"); //$NON-NLS-1$
            tilePanel[1] = new Tile("typing_panel_N"); //$NON-NLS-1$
            tilePanel[2] = new Tile("typing_panel_S"); //$NON-NLS-1$
            tilePanel[3] = new Tile("typing_panel_E"); //$NON-NLS-1$
            tilePanel[4] = new Tile("typing_panel_W"); //$NON-NLS-1$
            tilePanel[5] = new Tile("typing_panel_NE"); //$NON-NLS-1$
            tilePanel[6] = new Tile("typing_panel_NW"); //$NON-NLS-1$
            tilePanel[7] = new Tile("typing_panel_SE"); //$NON-NLS-1$
            tilePanel[8] = new Tile("typing_panel_SW"); //$NON-NLS-1$
            tilePanelNewText = new Tile[9]; // Background/N/S/E/W/NE,NW,SE,SW
            tilePanelNewText[0] = new Tile("typing_subpanel"); //$NON-NLS-1$
            tilePanelNewText[1] = new Tile("typing_subpanel_N"); //$NON-NLS-1$
            tilePanelNewText[2] = new Tile("typing_subpanel_S"); //$NON-NLS-1$
            tilePanelNewText[3] = new Tile("typing_subpanel_E"); //$NON-NLS-1$
            tilePanelNewText[4] = new Tile("typing_subpanel_W"); //$NON-NLS-1$
            tilePanelNewText[5] = new Tile("typing_subpanel_NE"); //$NON-NLS-1$
            tilePanelNewText[6] = new Tile("typing_subpanel_NW"); //$NON-NLS-1$
            tilePanelNewText[7] = new Tile("typing_subpanel_SE"); //$NON-NLS-1$
            tilePanelNewText[8] = new Tile("typing_subpanel_SW"); //$NON-NLS-1$

            tileConfirm = new Tile("typing_confirm"); //$NON-NLS-1$
            tileConfirmON = new Tile("typing_confirmON"); //$NON-NLS-1$
            tileConfirmAlpha = UtilsGL.generateAlpha(tileConfirm);
        }

        // Width / height
        WIDTH = UtilFont.MAX_WIDTH * MAX_CHARS + (UtilFont.MAX_WIDTH * MAX_CHARS) / 8 + 2 * tilePanel[3].getTileWidth();
        HEIGHT = 2 * tilePanel[1].getTileWidth() + UtilFont.MAX_HEIGHT * 7 + tileConfirm.getTileHeight();
        WIDTH_SUBPANEL = (UtilFont.MAX_WIDTH * MAX_CHARS) + 2 * tilePanelNewText[3].getTileWidth();
        HEIGHT_SUBPANEL = UtilFont.MAX_HEIGHT * 2 + 2 * tilePanel[1].getTileHeight();

        panelPoint.setLocation(renderWidth / 2 - WIDTH / 2, renderHeight / 2 - HEIGHT / 2);
        closeButtonPoint.setLocation(panelPoint.x + WIDTH - tileButtonClose.getTileWidth(), panelPoint.y);
        titlePoint.setLocation(panelPoint.x + WIDTH / 2 - UtilFont.getWidth(getTitle()) / 2, panelPoint.y + tilePanel[1].getTileHeight());
        oldTextPoint.setLocation(panelPoint.x + WIDTH / 2 - UtilFont.getWidth(getOldText()) / 2, titlePoint.y + 2 * UtilFont.MAX_HEIGHT);

        panelNewTextPoint.setLocation(panelPoint.x + WIDTH / 2 - WIDTH_SUBPANEL / 2, oldTextPoint.y + 2 * UtilFont.MAX_HEIGHT);
        updateNewTextPosition();

        confirmPoint.setLocation(panelPoint.x + WIDTH / 2 - tileConfirm.getTileWidth() / 2, panelNewTextPoint.y + HEIGHT_SUBPANEL + UtilFont.MAX_HEIGHT);
    }

    private static void updateNewTextPosition() {
        newTextPoint.setLocation(panelNewTextPoint.x + WIDTH_SUBPANEL / 2 - UtilFont.getWidth(getNewText()) / 2, panelNewTextPoint.y + HEIGHT_SUBPANEL / 2 - UtilFont.MAX_HEIGHT / 2);
    }

    private static void updateSubTitlePosition() {
        if (getSubTitle() != null) {
            subTitlePoint.setLocation(panelPoint.x + WIDTH / 2 - UtilFont.getWidth(getSubTitle()) / 2, titlePoint.y + 2 * UtilFont.MAX_HEIGHT);
        }
    }

    public static int whereIsMouse(int mouseX, int mouseY) {
        if (UIPanelInputHandler.isMouseOnAnIcon(mouseX, mouseY, closeButtonPoint, tileButtonClose, tileButtonCloseAlpha)) {
            return MOUSE_TYPING_PANEL_CLOSE;
        } else if (isMouseOnAnIcon(mouseX, mouseY, confirmPoint, tileConfirm, tileConfirmAlpha)) {
            return MOUSE_TYPING_PANEL_CONFIRM;
        }

        return MOUSE_NONE;
    }

    /**
     * Determina donde está el mouse y llama al render (mousePanel) Se usa, de
     * momento, desde el main menu, para teclear el nombre de la partida grabada
     */
    public static void render(int mouseX, int mouseY) {
        render(whereIsMouse(mouseX, mouseY));
    }

    public static void render(int mousePanel) {
        // Fondo
        GL11.glColor4f(1, 1, 1, 1);
        int iCurrentTexture = tilePanel[0].getTextureID();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, iCurrentTexture);
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
        UtilsGL.glBegin(GL11.GL_QUADS);
        drawTile(tilePanel[0], panelPoint);

        // Close button
        if (mousePanel == MOUSE_TYPING_PANEL_CLOSE) {
            iCurrentTexture = UtilsGL.setTexture(tileButtonClose, iCurrentTexture);
            drawTile(tileButtonClose, closeButtonPoint);
        } else {
            iCurrentTexture = UtilsGL.setTexture(tileButtonCloseDisabled, iCurrentTexture);
            drawTile(tileButtonCloseDisabled, closeButtonPoint);
        }

        // Subpanel
        iCurrentTexture = UtilsGL.setTexture(tilePanelNewText[0], iCurrentTexture);
        UIPanel.renderBackground(tilePanelNewText, panelNewTextPoint, WIDTH_SUBPANEL, HEIGHT_SUBPANEL);

        // Confirm
        if (TYPING_TYPE != TYPE_REDEFINE_KEYS) {
            if (mousePanel == MOUSE_TYPING_PANEL_CONFIRM) {
                iCurrentTexture = UtilsGL.setTexture(tileConfirmON, iCurrentTexture);
                drawTile(tileConfirmON, confirmPoint);
            } else {
                iCurrentTexture = UtilsGL.setTexture(tileConfirm, iCurrentTexture);
                drawTile(tileConfirm, confirmPoint);
            }
        }

        UtilsGL.glEnd();

        // Text
        iCurrentTexture = Game.TEXTURE_FONT_ID;
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
        UtilsGL.glBegin(GL11.GL_QUADS);

        // Title
        UtilsGL.drawStringWithBorder(getTitle(), titlePoint.x, titlePoint.y, ColorGL.WHITE, ColorGL.BLACK);

        // SubTitle
        if (getSubTitle() != null) {
            UtilsGL.drawStringWithBorder(getSubTitle(), subTitlePoint.x, subTitlePoint.y, ColorGL.RED, ColorGL.BLACK);
        }

        // OldText
        UtilsGL.drawStringWithBorder(getOldText(), oldTextPoint.x, oldTextPoint.y, ColorGL.YELLOW, ColorGL.BLACK);

        // New text
        UtilsGL.drawStringWithBorder(getNewText(), newTextPoint.x, newTextPoint.y, ColorGL.WHITE, ColorGL.BLACK);

        // Cursor
        UtilsGL.drawStringWithBorder("_", newTextPoint.x + UtilFont.getWidth(getNewText()), newTextPoint.y, ColorGL.WHITE, ColorGL.BLACK); //$NON-NLS-1$

        UtilsGL.glEnd();
    }

    /**
     * Añade un caracter al texto y retorna true en caso de que ya haya
     * terminado.
     *
     * @param key
     * @return
     */
    public static boolean keyPressed(int key) {
        return keyPressed(key, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT));
    }

    /**
     * Añade un caracter al texto y retorna true en caso de que ya haya
     * terminado.
     *
     * @param key
     * @param shift
     * @return
     */
    public static boolean keyPressed(int key, boolean shift) {
        if (TYPING_TYPE == TYPE_REDEFINE_KEYS) {
            if (key == Keyboard.KEY_ESCAPE) {
                return true;
            }
            if (key == Keyboard.KEY_LSHIFT || key == Keyboard.KEY_RSHIFT) {
                return false;
            }

            setNewText(Integer.toString(key));
            return true;
        }

        if (key == Keyboard.KEY_BACK || key == Keyboard.KEY_DELETE) {
            if (getNewText().length() > 0) {
                setNewText(getNewText().substring(0, getNewText().length() - 1));
            }
        } else if (key == Keyboard.KEY_ESCAPE) {
            setNewText(getOldText());
            return true;
        } else if (key == Keyboard.KEY_RETURN) {
            String sNewName = getNewText().trim();
            setNewText(sNewName);

            if (getNewText().length() == 0) {
                setNewText(getOldText());
            }
            return true;
        } else {
            // Teclas normales
            if (getNewText().length() < MAX_CHARS) {
                String sChar = getKeyString(key, shift);
                if (sChar != null) {
                    if (sChar.equals(" ")) { //$NON-NLS-1$
                        // Si ya tenía un espacio al final pasamos de éste
                        if (getNewText().length() > 0) {
                            if (getNewText().charAt(getNewText().length() - 1) != ' ') {
                                setNewText(getNewText() + sChar);
                            }
                        }
                    } else {
                        setNewText(getNewText() + sChar);
                    }
                }
            }
        }

        return false;
    }

    public static String getKeyString(int key, boolean shift) {
        switch (key) {
            case Keyboard.KEY_SPACE:
                return " "; //$NON-NLS-1$
            case Keyboard.KEY_A:
                return (shift) ? "A" : "a"; //$NON-NLS-1$ //$NON-NLS-2$
            case Keyboard.KEY_B:
                return (shift) ? "B" : "b"; //$NON-NLS-1$ //$NON-NLS-2$
            case Keyboard.KEY_C:
                return (shift) ? "C" : "c"; //$NON-NLS-1$ //$NON-NLS-2$
            case Keyboard.KEY_D:
                return (shift) ? "D" : "d"; //$NON-NLS-1$ //$NON-NLS-2$
            case Keyboard.KEY_E:
                return (shift) ? "E" : "e"; //$NON-NLS-1$ //$NON-NLS-2$
            case Keyboard.KEY_F:
                return (shift) ? "F" : "f"; //$NON-NLS-1$ //$NON-NLS-2$
            case Keyboard.KEY_G:
                return (shift) ? "G" : "g"; //$NON-NLS-1$ //$NON-NLS-2$
            case Keyboard.KEY_H:
                return (shift) ? "H" : "h"; //$NON-NLS-1$ //$NON-NLS-2$
            case Keyboard.KEY_I:
                return (shift) ? "I" : "i"; //$NON-NLS-1$ //$NON-NLS-2$
            case Keyboard.KEY_J:
                return (shift) ? "J" : "j"; //$NON-NLS-1$ //$NON-NLS-2$
            case Keyboard.KEY_K:
                return (shift) ? "K" : "k"; //$NON-NLS-1$ //$NON-NLS-2$
            case Keyboard.KEY_L:
                return (shift) ? "L" : "l"; //$NON-NLS-1$ //$NON-NLS-2$
            case Keyboard.KEY_M:
                return (shift) ? "M" : "m"; //$NON-NLS-1$ //$NON-NLS-2$
            case Keyboard.KEY_N:
                return (shift) ? "N" : "n"; //$NON-NLS-1$ //$NON-NLS-2$
            case Keyboard.KEY_O:
                return (shift) ? "O" : "o"; //$NON-NLS-1$ //$NON-NLS-2$
            case Keyboard.KEY_P:
                return (shift) ? "P" : "p"; //$NON-NLS-1$ //$NON-NLS-2$
            case Keyboard.KEY_Q:
                return (shift) ? "Q" : "q"; //$NON-NLS-1$ //$NON-NLS-2$
            case Keyboard.KEY_R:
                return (shift) ? "R" : "r"; //$NON-NLS-1$ //$NON-NLS-2$
            case Keyboard.KEY_S:
                return (shift) ? "S" : "s"; //$NON-NLS-1$ //$NON-NLS-2$
            case Keyboard.KEY_T:
                return (shift) ? "T" : "t"; //$NON-NLS-1$ //$NON-NLS-2$
            case Keyboard.KEY_U:
                return (shift) ? "U" : "u"; //$NON-NLS-1$ //$NON-NLS-2$
            case Keyboard.KEY_V:
                return (shift) ? "V" : "v"; //$NON-NLS-1$ //$NON-NLS-2$
            case Keyboard.KEY_W:
                return (shift) ? "W" : "w"; //$NON-NLS-1$ //$NON-NLS-2$
            case Keyboard.KEY_X:
                return (shift) ? "X" : "x"; //$NON-NLS-1$ //$NON-NLS-2$
            case Keyboard.KEY_Y:
                return (shift) ? "Y" : "y"; //$NON-NLS-1$ //$NON-NLS-2$
            case Keyboard.KEY_Z:
                return (shift) ? "Z" : "z"; //$NON-NLS-1$ //$NON-NLS-2$

            case Keyboard.KEY_0:
                return (shift) ? "" + Keyboard.getEventCharacter() : "0"; //$NON-NLS-1$ //$NON-NLS-2$
            case Keyboard.KEY_1:
                return (shift) ? "" + Keyboard.getEventCharacter() : "1"; //$NON-NLS-1$ //$NON-NLS-2$
            case Keyboard.KEY_2:
                return (shift) ? "" + Keyboard.getEventCharacter() : "2"; //$NON-NLS-1$ //$NON-NLS-2$
            case Keyboard.KEY_3:
                return (shift) ? "" + Keyboard.getEventCharacter() : "3"; //$NON-NLS-1$ //$NON-NLS-2$
            case Keyboard.KEY_4:
                return (shift) ? "" + Keyboard.getEventCharacter() : "4"; //$NON-NLS-1$ //$NON-NLS-2$
            case Keyboard.KEY_5:
                return (shift) ? "" + Keyboard.getEventCharacter() : "5"; //$NON-NLS-1$ //$NON-NLS-2$
            case Keyboard.KEY_6:
                return (shift) ? "" + Keyboard.getEventCharacter() : "6"; //$NON-NLS-1$ //$NON-NLS-2$
            case Keyboard.KEY_7:
                return (shift) ? "" + Keyboard.getEventCharacter() : "7"; //$NON-NLS-1$ //$NON-NLS-2$
            case Keyboard.KEY_8:
                return (shift) ? "" + Keyboard.getEventCharacter() : "8"; //$NON-NLS-1$ //$NON-NLS-2$
            case Keyboard.KEY_9:
                return (shift) ? "" + Keyboard.getEventCharacter() : "9"; //$NON-NLS-1$ //$NON-NLS-2$

            // Server chars
            case Keyboard.KEY_SLASH:
            case Keyboard.KEY_DIVIDE:
                if (TYPING_TYPE == TYPE_ADD_SERVER) {
                    return "/"; //$NON-NLS-1$
                }
            case Keyboard.KEY_PERIOD:
                if (TYPING_TYPE == TYPE_ADD_SERVER) {
                    return "."; //$NON-NLS-1$
                }
        }

        return null;
    }

    public static String getTitle() {
        return title;
    }

    public static void setTitle(String title) {
        TypingPanel.title = title;
    }

    public static String getOldText() {
        return oldText;
    }

    public static void setOldText(String oldText) {
        TypingPanel.oldText = oldText;
    }

    public static String getNewText() {
        return newText;
    }

    public static void setNewText(String newText) {
        TypingPanel.newText = newText;

        // En el caso de que esté pòniendo el nombre de una partida, miraremos que no exista en disco
        if (TYPING_TYPE == TYPE_SAVEGAME_NAME) {
            if (Utils.existsSavegame(getNewText())) {
                setSubTitle(Messages.getString("TypingPanel.1")); //$NON-NLS-1$
            } else {
                setSubTitle(null);
            }
        }

        updateNewTextPosition();
    }

    public static void setSubTitle(String subTitle) {
        TypingPanel.subTitle = subTitle;
        updateSubTitlePosition();
    }

    public static String getSubTitle() {
        return subTitle;
    }

    public static Point getPanelPoint() {
        return panelPoint;
    }

    public static void setPanelPoint(Point panelPoint) {
        TypingPanel.panelPoint = panelPoint;
    }

    public static Point getPanelNewTextPoint() {
        return panelNewTextPoint;
    }

    public static void setPanelNewTextPoint(Point panelNewTextPoint) {
        TypingPanel.panelNewTextPoint = panelNewTextPoint;
    }

    public static Point getCloseButtonPoint() {
        return closeButtonPoint;
    }

    public static void setCloseButtonPoint(Point closeButtonPoint) {
        TypingPanel.closeButtonPoint = closeButtonPoint;
    }

    public static Point getTitlePoint() {
        return titlePoint;
    }

    public static void setTitlePoint(Point titlePoint) {
        TypingPanel.titlePoint = titlePoint;
    }

    public static Point getOldTextPoint() {
        return oldTextPoint;
    }

    public static void setOldTextPoint(Point oldTextPoint) {
        TypingPanel.oldTextPoint = oldTextPoint;
    }

    public static Point getNewTextPoint() {
        return newTextPoint;
    }

    public static void setNewTextPoint(Point newTextPoint) {
        TypingPanel.newTextPoint = newTextPoint;
    }

    public static Point getConfirmPoint() {
        return confirmPoint;
    }

    public static void setConfirmPoint(Point confirmPoint) {
        TypingPanel.confirmPoint = confirmPoint;
    }

    public static Tile getTileConfirm() {
        return tileConfirm;
    }

    public static void setTileConfirm(Tile tileConfirm) {
        TypingPanel.tileConfirm = tileConfirm;
    }

    public static boolean[][] getTileConfirmAlpha() {
        return tileConfirmAlpha;
    }

    public static void setTileConfirmAlpha(boolean[][] tileConfirmAlpha) {
        TypingPanel.tileConfirmAlpha = tileConfirmAlpha;
    }
}
