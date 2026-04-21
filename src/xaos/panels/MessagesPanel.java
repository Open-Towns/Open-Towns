package xaos.panels;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

import org.lwjgl.opengl.GL11;
import xaos.property.PropertyFile;

import xaos.Towns;
import xaos.TownsProperties;
import xaos.main.Game;
import xaos.main.World;
import xaos.tiles.entities.living.LivingEntity;
import xaos.utils.ColorGL;
import xaos.utils.Log;
import xaos.utils.Messages;
import xaos.utils.Point3DShort;
import xaos.utils.TextureData;
import xaos.utils.UtilFont;
import xaos.utils.UtilsGL;

public final class MessagesPanel {

    public static final int MAX_MESSAGES = 512;

    // Message types
    public static final int TYPE_ANNOUNCEMENT = 0;
    public static final int TYPE_COMBAT = 1;
    public static final int TYPE_HEROES = 2;
    public static final int TYPE_SYSTEM = 3;

    public static final int MAX_TYPES = 4;

    private static ArrayList<MessagesPanelData[]> messagesDataFull;
    private static boolean[] blink;
    private static int[] pages;
    private static int[] pagesCurrent;
    private static int[] numMessages;
    private static int maxRenderLines = 10;

    public MessagesPanel(int renderWidth, int renderHeight) {
        final TextureData fontTexture = UtilsGL.loadTexture(Towns.getPropertiesString(PropertyFile.PROPERTY_FILE_GRAPHICS, "TILESET_FONT_FILE"), GL11.GL_MODULATE);
        if (fontTexture == null) {
            Log.log(Log.LEVEL_ERROR, Messages.getString("MessagesPanel.2"), getClass().toString()); //$NON-NLS-1$
            Game.exit();
        }
        Game.TEXTURE_FONT_ID = fontTexture.getTextureID(); //$NON-NLS-1$ //$NON-NLS-2$

        clear();

        resize(renderWidth, renderHeight);
    }

    public static void clear() {
        messagesDataFull = new ArrayList<>(MAX_TYPES * 2);
        for (int i = 0; i < (MAX_TYPES * 2); i++) {
            messagesDataFull.add(new MessagesPanelData[MAX_MESSAGES]);
        }

        blink = new boolean[MAX_TYPES];
        pages = new int[MAX_TYPES];
        pagesCurrent = new int[MAX_TYPES];
        numMessages = new int[MAX_TYPES];
        maxRenderLines = 10;

        for (int i = 0; i < MAX_TYPES; i++) {
            blink[i] = false;
            pages[i] = 1;
            pagesCurrent[i] = 1;
            numMessages[i] = 0;
        }
    }

    public static void resize(int width, int height) {
//		// Messages
//		for (int m = 0; m < messagesDataFull.size () / 2; m++) {
//			if (messagesDataFull.get (m + 1) != null) {
//				// Hay que hacer un resize de todo
//				MessagesPanelData[] messagesData = messagesDataFull.get (m);
//				if (messagesData != null) {
//					messagesDataFull.set (m + 1, new MessagesPanelData [MAX_MESSAGES]);
//					MessagesPanelData mpd;
//					for (int i = 0; i < messagesData.length; i++) {
//						mpd = messagesData[i];
//						if (mpd != null) {
//							addMessageRender (m / 2, mpd.getMessage (), mpd.getColor (), mpd.getView (), mpd.getEntityID());
//						}
//					}
//				}
//			}
//		}

        if (pages == null) {
            clear();
        }

        maxRenderLines = (UIPanel.MESSAGES_PANEL_SUBPANEL_HEIGHT - 2 * UIPanel.tileMessagesPanel[1].getTileHeight()) / (UtilFont.MAX_HEIGHT);
        if (maxRenderLines < 1) {
            maxRenderLines = 1;
        }
        changePages();
    }

    private static void changePages() {
        for (int i = 0; i < pages.length; i++) {
            pages[i] = ((numMessages[i] - 1) / maxRenderLines) + 1;
            pagesCurrent[i] = pages[i];
        }
    }

    /**
     * Añade un mensaje al final de la cola de mensajes, lo parte si no cabe
     *
     * @param iMessageType
     * @param sMessage Mensaje
     */
    public static void addMessage(int iMessageType, String sMessage) {
        addMessage(iMessageType, sMessage, null, null, -1);
    }

    /**
     * Añade un mensaje al final de la cola de mensajes, lo parte si no cabe
     *
     * @param iMessageType
     * @param sMessage Mensaje
     * @param color Color del fondo del mensaje
     */
    public static void addMessage(int iMessageType, String sMessage, ColorGL color) {
        addMessage(iMessageType, sMessage, color, null, -1);
    }

    /**
     * Añade un mensaje al final de la cola de mensajes Lo parte para meter en
     * la cola a renderizar
     *
     * @param iMessageType
     * @param sMessage Mensaje
     * @param color ColorGL del fondo del mensaje
     */
    public static void addMessage(int iMessageType, String sMessage, ColorGL color, Point3DShort view) {
        addMessage(iMessageType, sMessage, color, view, -1);
    }

    public static void addMessage(int iMessageType, String sMessage, ColorGL color, Point3DShort view, int entityID) {
        MessagesPanelData[] messagesData = messagesDataFull.get(iMessageType * 2);
        for (int i = 0; i < (messagesData.length - 1); i++) {
            messagesData[i] = messagesData[i + 1];
        }
        if (color == null) {
            color = new ColorGL(null);
        }

        messagesData[messagesData.length - 1] = new MessagesPanelData(sMessage, color, view, entityID);
        messagesDataFull.set(iMessageType * 2, messagesData);

        addMessageRender(iMessageType, sMessage, color, view, entityID);

        blink[iMessageType] = true;
    }

    /**
     * Añade el mensaje a la cola de mensajes a renderizar
     *
     * @param sMessage
     * @param color
     * @param view
     */
    private static void addMessageRender(int iMessageType, String sMessage, ColorGL color, Point3DShort view, int iEntityID) {
        if (numMessages[iMessageType] < MAX_MESSAGES) {
            numMessages[iMessageType]++;
        }
        changePages();
        MessagesPanelData[] messagesDataRender = messagesDataFull.get(iMessageType * 2 + 1);
        for (int i = 0; i < (messagesDataRender.length - 1); i++) {
            messagesDataRender[i] = messagesDataRender[i + 1];
        }
        if (color == null) {
            color = new ColorGL(null);
        }

        int iMaxChars = UtilFont.getMaxCharsByWidth(sMessage, UIPanel.MESSAGES_PANEL_SUBPANEL_WIDTH - 2 * UIPanel.tileMessagesPanel[3].getTileWidth());
        if (sMessage.length() > iMaxChars) {
            // No cabe, lo partimos
            messagesDataRender[messagesDataRender.length - 1] = new MessagesPanelData(sMessage.substring(0, iMaxChars).trim(), color, view, iEntityID);
            messagesDataFull.set(iMessageType * 2 + 1, messagesDataRender);
            addMessageRender(iMessageType, sMessage.substring(iMaxChars).trim(), color, view, iEntityID);
        } else {
            messagesDataRender[messagesDataRender.length - 1] = new MessagesPanelData(sMessage, color, view, iEntityID);
            messagesDataFull.set(iMessageType * 2 + 1, messagesDataRender);
        }
    }

    public static void render(int mouseX, int mouseY, int iMessagesType, int x, int y) {
        blink[iMessagesType] = false;

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
        UtilsGL.glBegin(GL11.GL_QUADS);
        // Pintamos los mensajes y también miramos si el ratón está allí para highlightearlos
        MessagesPanelData[] messagesDataRender = messagesDataFull.get(iMessagesType * 2 + 1);
        int xM = x;
        int iIndex = (pages[iMessagesType] + 1 - pagesCurrent[iMessagesType]) * maxRenderLines;
        iIndex = messagesDataRender.length - iIndex;
        for (int i = iIndex; i < iIndex + maxRenderLines; i++) {
            if (i >= 0 && messagesDataRender.length > i && messagesDataRender[i] != null) {
                int yM = y + (i - iIndex) * UtilFont.MAX_HEIGHT + UtilFont.MAX_HEIGHT / 2;
                if (messagesDataRender[i].getView() != null || messagesDataRender[i].getEntityID() != -1) {
                    if (mouseX >= xM && mouseX < (xM + messagesDataRender[i].getWidth()) && mouseY >= yM && mouseY < (yM + UtilFont.MAX_HEIGHT)) {
                        UtilsGL.drawStringWithBorder(messagesDataRender[i].getMessage(), xM, yM, messagesDataRender[i].getColor(), ColorGL.WHITE);
                    } else {
                        UtilsGL.drawString(messagesDataRender[i].getMessage(), xM, yM, messagesDataRender[i].getColor());
                    }
                } else {
                    UtilsGL.drawString(messagesDataRender[i].getMessage(), xM, yM, messagesDataRender[i].getColor());
                }
            }
        }
        UtilsGL.glEnd();
    }

    public static MessagesPanelData[] getMessagesData(int messageType) {
        return messagesDataFull.get(messageType * 2);
    }

    public static String getLastestMessage(int messageType, int offset) {
        blink[messageType] = false;
        MessagesPanelData[] mpd = messagesDataFull.get(messageType * 2);
        int iIndex = MAX_MESSAGES - 1 - offset;
        if (mpd != null && mpd.length > iIndex && mpd[iIndex] != null) {
            return mpd[iIndex].getMessage();
        } else {
            return null;
        }
    }

    public static ColorGL getLastestMessageColor(int messageType, int offset) {
        blink[messageType] = false;
        MessagesPanelData[] mpd = messagesDataFull.get(messageType * 2);
        int iIndex = MAX_MESSAGES - 1 - offset;
        if (mpd != null && mpd.length > iIndex && mpd[iIndex] != null) {
            return mpd[iIndex].getColor();
        } else {
            return null;
        }
    }

    public static void setMessagesDataFull(ArrayList<MessagesPanelData[]> messagesDataFull) {
        MessagesPanel.messagesDataFull = messagesDataFull;
    }

    public static ArrayList<MessagesPanelData[]> getMessagesDataFull() {
        return messagesDataFull;
    }

    public static void setBlink(boolean[] blink) {
        MessagesPanel.blink = blink;
    }

    public static boolean[] getBlink() {
        return blink;
    }

    public static void setPages(int[] pages) {
        MessagesPanel.pages = pages;
    }

    public static int[] getPages() {
        return pages;
    }

    public static int getPages(int iType) {
        return pages[iType];
    }

    public static void setPagesCurrent(int[] pagesCurrent) {
        MessagesPanel.pagesCurrent = pagesCurrent;
    }

    public static int[] getPagesCurrent() {
        return pagesCurrent;
    }

    public static int getPagesCurrent(int iType) {
        return pagesCurrent[iType];
    }

    public static int[] getNumMessages() {
        return numMessages;
    }

    public static void setNumMessages(int[] numMessages) {
        MessagesPanel.numMessages = numMessages;
    }

    public static void pageUp(int iType) {
        if (pagesCurrent.length > iType) {
            pagesCurrent[iType]--;
        }
    }

    public static void pageDown(int iType) {
        if (pagesCurrent.length > iType && pages[iType] > pagesCurrent[iType]) {
            pagesCurrent[iType]++;
        }
    }

    public static void setMessagesData(int messageType, MessagesPanelData[] msgs) {
        messagesDataFull.set(messageType * 2, msgs);
    }

    /**
     * Limpia todos los datos (se usa cuando se sale de la partida y se va al
     * menú principal)
     */
    public static void initialize() {
        messagesDataFull = new ArrayList<>(MAX_TYPES * 2);
        for (int i = 0; i < (MAX_TYPES * 2); i++) {
            messagesDataFull.add(new MessagesPanelData[MAX_MESSAGES]);
        }
        blink = new boolean[MAX_TYPES];
        pages = new int[MAX_TYPES];
        pagesCurrent = new int[MAX_TYPES];
        numMessages = new int[MAX_TYPES];
        for (int i = 0; i < MAX_TYPES; i++) {
            pages[i] = 1;
            pagesCurrent[i] = 1;
        }

        String sVersion = TownsProperties.GAME_NAME + " " + TownsProperties.GAME_VERSION_FULL; //$NON-NLS-1$
        if (TownsProperties.DEMO_VERSION) {
            sVersion += " (demo)"; //$NON-NLS-1$
        }

        addMessage(TYPE_SYSTEM, sVersion);
        blink[TYPE_SYSTEM] = false; //no blinking because of the version info
    }

    public static boolean mousePressed(int mouseX, int mouseY, int iMessagesType, int x, int y) {
        MessagesPanelData[] messagesDataRender = messagesDataFull.get(iMessagesType * 2 + 1);
        int xM = x;
        int iIndex = (pages[iMessagesType] + 1 - pagesCurrent[iMessagesType]) * maxRenderLines;
        iIndex = messagesDataRender.length - iIndex;
        for (int i = iIndex; i < iIndex + maxRenderLines; i++) {
            if (i >= 0 && messagesDataRender.length > i && messagesDataRender[i] != null) {
                int yM = y + (i - iIndex) * UtilFont.MAX_HEIGHT + UtilFont.MAX_HEIGHT / 2;
                if (messagesDataRender[i].getView() != null || messagesDataRender[i].getEntityID() != -1) {
                    if (mouseX >= xM && mouseX < (xM + messagesDataRender[i].getWidth()) && mouseY >= yM && mouseY < (yM + UtilFont.MAX_HEIGHT)) {
                        // POM POM
                        if (messagesDataRender[i].getEntityID() != -1) {
                            // Parece que hay entity ID, miramos si existe
                            LivingEntity le = World.getLivingEntityByID(messagesDataRender[i].getEntityID());
                            if (le != null && le.getCoordinates() != null && le.getCoordinates().x != -1) {
                                Game.getWorld().setView(le.getCoordinates());
                                return true;
                            } else if (messagesDataRender[i].getView() != null) {
                                Game.getWorld().setView(messagesDataRender[i].getView());
                                return true;
                            }
                        } else if (messagesDataRender[i].getView() != null) { // Si llega aquí debería ser != null siempre
                            Game.getWorld().setView(messagesDataRender[i].getView());
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    public static void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        messagesDataFull = (ArrayList<MessagesPanelData[]>) in.readObject();
        blink = (boolean[]) in.readObject();
        pages = (int[]) in.readObject();
        pagesCurrent = (int[]) in.readObject();
        numMessages = (int[]) in.readObject();
    }

    public static void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(messagesDataFull);
        out.writeObject(blink);
        out.writeObject(pages);
        out.writeObject(pagesCurrent);
        out.writeObject(numMessages);
    }
}
