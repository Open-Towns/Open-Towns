package xaos.panels.menus;

import java.awt.Color;
import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import xaos.platform.lwjgl3.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import xaos.TownsProperties;

import xaos.actions.ActionManager;
import xaos.actions.ActionManagerItem;
import xaos.actions.QueueItem;
import xaos.main.Game;
import xaos.panels.CommandPanel;
import xaos.panels.MainPanel;
import xaos.panels.UI.UIPanel;
import xaos.panels.UI.UIPanelState;
import xaos.tiles.Tile;
import xaos.tiles.entities.buildings.BuildingManager;
import xaos.tiles.entities.buildings.BuildingManagerItem;
import xaos.tiles.entities.items.ItemManager;
import xaos.tiles.entities.items.ItemManagerItem;
import xaos.tiles.entities.living.LivingEntityManager;
import xaos.tiles.entities.living.LivingEntityManagerItem;
import xaos.tiles.terrain.TerrainManager;
import xaos.utils.ColorGL;
import xaos.utils.Log;
import xaos.utils.Messages;
import xaos.utils.Point3D;
import xaos.utils.UtilFont;
import xaos.utils.Utils;
import xaos.utils.UtilsAL;
import xaos.utils.UtilsGL;
import xaos.utils.UtilsIniHeaders;
import xaos.zones.ZoneManager;
import xaos.zones.ZoneManagerItem;

public class SmartMenu implements Externalizable {

    private static final long serialVersionUID = -2274612491197616852L;

    private static Tile RED_TILE = new Tile("ui_red"); //$NON-NLS-1$

    public final static int TYPE_NO_TYPE = -1;
    public final static int TYPE_TEXT = 0;
    public final static int TYPE_MENU = 1;
    public final static int TYPE_ITEM = 2;

    public final static int ICON_TYPE_UI = 0;
    public final static int ICON_TYPE_ITEM = 1;

    public final static Color COLOR_SUBMENU = Color.ORANGE.brighter();
    public final static ColorGL COLORGL_SUBMENU = new ColorGL(COLOR_SUBMENU);

    private int type;
    private String id; // Se usa en los menuXXX.xml , así los mods pueden referirse a un item para borrarlo
    private String name;
    private SmartMenu parent;
    private ArrayList<SmartMenu> items;
    private String command; // Acción que lanza este item
    private String parameter; // Parámetro del comando
    private String parameter2; // Parámetro 2 del comando
    private Point3D directCoordinates; // Se usa en los menus contextuales, ya que lanzan un comando en casillas concretas
    private ColorGL color;
    private boolean trasparency; // Si es transparente no se dibuja el rectángulo negro abajo
    private boolean dynamic; // Para sustituir cadenas de texto de los menues
    private boolean maintainOpen; // Para saber si hay que cerrar el menú al clicar en una opción
    private ColorGL borderColor; // Si es distinto de null pinta un borde a los textos del color indicado
    private Tile icon; // Icono a usar en los menus
    private int iconType; // Tipo de icono (ui, items, ...)

    private ArrayList<String> prerequisites;
    private ArrayList<ColorGL> prerequisitesColor;

    public SmartMenu() {
        this(TYPE_NO_TYPE, null, null, null, null);
    }

    public SmartMenu(int type, String name, SmartMenu parent, String command, String parameter) {
        this(type, name, parent, command, parameter, null);
    }

    public SmartMenu(int type, String name, SmartMenu parent, String command, String parameter, String parameter2) {
        this(type, name, parent, command, parameter, parameter2, null);
    }

    public SmartMenu(int type, String name, SmartMenu parent, String command, String parameter, String parameter2, Point3D directCoordinates) {
        this(type, name, parent, command, parameter, parameter2, directCoordinates, null);
    }

    public SmartMenu(int type, String name, SmartMenu parent, String command, String parameter, String parameter2, Point3D directCoordinates, Color color) {
        this.type = type;
        this.name = name;
        this.parent = parent;
        this.command = command;
        this.parameter = parameter;
        this.parameter2 = parameter2;
        this.directCoordinates = directCoordinates;
        this.color = new ColorGL(color);
    }

    public void refreshTransients() {
        if (parent != null) {
            parent.refreshTransients();
        }

        if (getItems() != null) {
            for (int i = 0; i < getItems().size(); i++) {
                getItems().get(i).refreshTransients();
            }
        }

        if (getIcon() != null) {
            getIcon().refreshTransients();
        }
    }

    public String getID() {
        return id;
    }

    public void setID(String sID) {
        this.id = sID;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SmartMenu getParent() {
        return parent;
    }

    public void setParent(SmartMenu parent) {
        this.parent = parent;
    }

    public ArrayList<SmartMenu> getItems() {
        if (items == null) {
            items = new ArrayList<>();
        }
        return items;
    }

    public void setItems(ArrayList<SmartMenu> items) {
        this.items = items;
    }

    public void addItem(SmartMenu item) {
        if (items == null) {
            items = new ArrayList<>();
        }

        items.add(item);
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public String getParameter2() {
        return parameter2;
    }

    public void setParameter2(String parameter2) {
        this.parameter2 = parameter2;
    }

    public Point3D getDirectCoordinates() {
        return directCoordinates;
    }

    public void setDirectCoordinates(Point3D directCoordinates) {
        this.directCoordinates = directCoordinates;
    }

    public void setColor(ColorGL color) {
        this.color = color;
    }

    public ColorGL getColor() {
        return color;
    }

    public void setTrasparency(boolean trasparency) {
        this.trasparency = trasparency;
    }

    public boolean isTrasparency() {
        return trasparency;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void setMaintainOpen(boolean mOpen) {
        this.maintainOpen = mOpen;
    }

    public boolean isMaintainOpen() {
        return maintainOpen;
    }

    public void setBorderColor(Color borderColor) {
        setBorderColor(new ColorGL(borderColor));
    }

    public void setBorderColor(ColorGL borderColor) {
        this.borderColor = borderColor;
    }

    public ColorGL getBorderColor() {
        return borderColor;
    }

    public void setIcon(String icon) {
        if (icon != null) {
            this.icon = new Tile(icon);
        }
    }

    public Tile getIcon() {
        return icon;
    }

    public void setIconType(int iconType) {
        this.iconType = iconType;
    }

    public int getIconType() {
        return iconType;
    }

    public void setPrerequisites(ArrayList<String> prerequisites) {
        this.prerequisites = prerequisites;
    }

    public ArrayList<String> getPrerequisites() {
        return prerequisites;
    }

    public void setPrerequisitesColor(ArrayList<ColorGL> prerequisitesColor) {
        this.prerequisitesColor = prerequisitesColor;
    }

    public ArrayList<ColorGL> getPrerequisitesColor() {
        return prerequisitesColor;
    }

    public void render(int x, int y, int width, int height, boolean isContext) {
        if (!isTrasparency()) {
            GL11.glColor4f(1, 1, 1, 1);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, UIPanelState.tileTooltipBackground.getTextureID());
            UtilsGL.glBegin(GL11.GL_QUADS);
            UtilsGL.drawTexture(x, y, x + width, y + height, UIPanelState.tileTooltipBackground.getTileSetTexX0(), UIPanelState.tileTooltipBackground.getTileSetTexY0(), UIPanelState.tileTooltipBackground.getTileSetTexX1(), UIPanelState.tileTooltipBackground.getTileSetTexY1());
            UtilsGL.glEnd();
        }

        // Rectángulito rojo en el item marcado (excepto TYPE_TEXT)
        int iY;
        int mouseX = Mouse.getX();
        int mouseY = UtilsGL.getHeight() - Mouse.getY() - 1;
        int itemIndex = -1;
        if (isContext) {
            if (mouseX >= x && mouseX < (x + width) && mouseY >= y && mouseY < (y + getItems().size() * UtilFont.MAX_HEIGHT)) {
                itemIndex = (mouseY - y) / UtilFont.MAX_HEIGHT;
                if (getItems().get(itemIndex).getType() != TYPE_TEXT) {
                    iY = y + itemIndex * UtilFont.MAX_HEIGHT + 1;
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, RED_TILE.getTextureID());
                    GL11.glColor3f(1, 0, 0);
                    UtilsGL.glBegin(GL11.GL_QUADS);
                    UtilsGL.drawTexture(x, iY, x + width, iY + UtilFont.MAX_HEIGHT, RED_TILE.getTileSetTexX0(), RED_TILE.getTileSetTexY0(), RED_TILE.getTileSetTexX1(), RED_TILE.getTileSetTexY1());
                    UtilsGL.glEnd();
                }
            }
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, Game.TEXTURE_FONT_ID);
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

        // Menú
        UtilsGL.glBegin(GL11.GL_QUADS);
        String sTexto;
        SmartMenu item;
        for (int i = 0; i < getItems().size(); i++) {
            item = getItems().get(i);
            iY = y + (i * UtilFont.MAX_HEIGHT) + 1;
            if (item.isDynamic()) {
                sTexto = Utils.getDynamicString(item.getName());
            } else {
                sTexto = item.getName();
            }

            if (item.getBorderColor() != null) {
                UtilsGL.drawString(sTexto, x, iY - 1, item.getBorderColor());
                UtilsGL.drawString(sTexto, x + 1, iY - 1, item.getBorderColor());
                UtilsGL.drawString(sTexto, x + 2, iY - 1, item.getBorderColor());
                UtilsGL.drawString(sTexto, x, iY, item.getBorderColor());
                UtilsGL.drawString(sTexto, x + 2, iY, item.getBorderColor());
                UtilsGL.drawString(sTexto, x, iY + 1, item.getBorderColor());
                UtilsGL.drawString(sTexto, x + 1, iY + 1, item.getBorderColor());
                UtilsGL.drawString(sTexto, x + 2, iY + 1, item.getBorderColor());

                if (item.getParent() != null) {
                    UtilsGL.drawString(sTexto, x + 1, iY, COLORGL_SUBMENU);
                } else {
                    UtilsGL.drawString(sTexto, x + 1, iY, item.getColor());
                }
            } else {
                if (item.getParent() != null) {
                    UtilsGL.drawString(sTexto, x, iY, COLORGL_SUBMENU);
                } else {
                    UtilsGL.drawString(sTexto, x, iY, item.getColor());
                }
            }

        }
        UtilsGL.glEnd();

        // Tooltip
        if (itemIndex != -1) {
            SmartMenu menuItem = getItems().get(itemIndex);
            if (menuItem.getPrerequisites() != null && menuItem.getPrerequisites().size() > 0) {
                MainPanel.renderMessages(mouseX, mouseY + Tile.TERRAIN_ICON_HEIGHT / 2, UtilsGL.getWidth(), UtilsGL.getHeight(), Tile.TERRAIN_ICON_WIDTH / 2, menuItem.getPrerequisites(), menuItem.getPrerequisitesColor());
            }
        }
    }

    /**
     * Carga los menús del .xml y lo mapea todo a clases SmartMenu
     *
     * @return el padre de todos los menús
     */
    public static void readXMLMenu(SmartMenu menuInicial, String sFilename, String sCampaignID, String sMissionID) {
        //SmartMenu menuInicial = new SmartMenu ();

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            ArrayList<String> alPaths = Utils.getPathToFile(sFilename, sCampaignID, sMissionID);
            for (int i = 0; i < alPaths.size(); i++) {
                File f = new File(alPaths.get(i));
                Document doc = db.parse(f);
                readXMLItem(doc, doc.getDocumentElement().getChildNodes(), menuInicial, i == 0);
            }
        } catch (Exception e) {
            Log.log(Log.LEVEL_ERROR, Messages.getString("SmartMenu.1") + sFilename + Messages.getString("SmartMenu.2") + e.toString() + "]", "SmartMenu"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            Game.exit();
        }

        //return menuInicial;
    }

    private static void readXMLItem(Document doc, NodeList list, SmartMenu smartMenu, boolean bLoadingMain) {
        Node node;
        for (int i = 0; i < list.getLength(); i++) {
            node = list.item(i);

            String sLocale = Locale.getDefault().getLanguage() + Locale.getDefault().getCountry();
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                // Si el elemento se llama "item" es que es un item, en otro caso es un submenú

                NamedNodeMap map = node.getAttributes();
                if (node.getNodeName().equalsIgnoreCase("ITEM")) { //$NON-NLS-1$
                    // Item
                    // Miramos que no sea un delete
                    if (map.getNamedItem("delete") != null && map.getNamedItem("delete").getNodeValue() != null && map.getNamedItem("delete").getNodeValue().equalsIgnoreCase("TRUE")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                        // Es un delete, miramos el ID a borrar
                        if (map.getNamedItem("id") != null) { //$NON-NLS-1$
                            String sID = map.getNamedItem("id").getNodeValue(); //$NON-NLS-1$
                            if (sID != null && sID.length() > 0) {
                                // Buscamos el ID en el current menu, si lo encontramos lo petamos
                                ArrayList<SmartMenu> alItems = smartMenu.getItems();
                                if (alItems != null) {
                                    for (int m = 0; m < alItems.size(); m++) {
                                        SmartMenu sm = alItems.get(m);
                                        if (sm.getID() != null && sm.getID().equalsIgnoreCase(sID)) {
                                            // Bingo
                                            alItems.remove(m);
                                        }
                                    }
                                }
                            }
                        }
                        continue;
                    }

                    // Code
                    Node code = map.getNamedItem("code"); //$NON-NLS-1$
                    if (code == null) {
                        Log.log(Log.LEVEL_ERROR, Messages.getString("SmartMenu.7"), "SmartMenu"); //$NON-NLS-1$ //$NON-NLS-2$
                        Game.exit();
                    } else if (code.getNodeValue() != null && code.getNodeValue().equalsIgnoreCase("BLANKLINE")) { //$NON-NLS-1$
                        // Linea en blanco
                        smartMenu.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, null, null, null, null));
                        continue;
                    } else {
                        Node parameter = map.getNamedItem("parameter"); //$NON-NLS-1$

                        SmartMenu item;

                        // Name
                        String sName = null;
                        if (map.getNamedItem(sLocale) != null) {
                            sName = map.getNamedItem(sLocale).getNodeValue();
                        }
                        if (sName == null || sName.length() == 0) {
                            if (map.getNamedItem("name") != null) { //$NON-NLS-1$
                                sName = map.getNamedItem("name").getNodeValue(); //$NON-NLS-1$
                            }
                        }
                        if (sName == null || sName.length() == 0) {
                            // No encuentra name, miramos si es una tarea de CREATE, CREATEANDPLACE, CREATEANDPLACEROW o BUILD para obtener la cadena de la definición del item/edificio
                            if (parameter != null
                                    && parameter.getNodeValue() != null
                                    && parameter.getNodeValue().length() > 0
                                    && code.getNodeValue() != null
                                    && (code.getNodeValue().equalsIgnoreCase(CommandPanel.COMMAND_CREATE) || code.getNodeValue().equalsIgnoreCase(CommandPanel.COMMAND_CREATE_AND_PLACE) || code.getNodeValue().equalsIgnoreCase(CommandPanel.COMMAND_CREATE_AND_PLACE_ROW) || code.getNodeValue().equalsIgnoreCase(CommandPanel.COMMAND_BUILD) || code.getNodeValue().equalsIgnoreCase(CommandPanel.COMMAND_CUSTOM_ACTION) || code.getNodeValue().equalsIgnoreCase(CommandPanel.COMMAND_QUEUE)
                                    || code.getNodeValue().equalsIgnoreCase(CommandPanel.COMMAND_QUEUE_AND_PLACE) || code.getNodeValue().equalsIgnoreCase(CommandPanel.COMMAND_QUEUE_AND_PLACE_ROW) || code.getNodeValue().equalsIgnoreCase(CommandPanel.COMMAND_QUEUE_AND_PLACE_AREA)
                                    || code.getNodeValue().equalsIgnoreCase(CommandPanel.COMMAND_CREATE_ZONE))) {
                                if (code.getNodeValue().equalsIgnoreCase(CommandPanel.COMMAND_CUSTOM_ACTION) || code.getNodeValue().equalsIgnoreCase(CommandPanel.COMMAND_QUEUE) || code.getNodeValue().equalsIgnoreCase(CommandPanel.COMMAND_QUEUE_AND_PLACE) || code.getNodeValue().equalsIgnoreCase(CommandPanel.COMMAND_QUEUE_AND_PLACE_ROW) || code.getNodeValue().equalsIgnoreCase(CommandPanel.COMMAND_QUEUE_AND_PLACE_AREA)) {
                                    // Custom action & queues
                                    ActionManagerItem ami = ActionManager.getItem(parameter.getNodeValue());
                                    if (ami != null) {
                                        sName = ami.getName();
                                        if (TownsProperties.DEBUG_MODE) {
                                            sName += " (" + ami.getGeneratedItem() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                                        }
                                    }
                                } else if (code.getNodeValue().equalsIgnoreCase(CommandPanel.COMMAND_BUILD)) {
                                    // Edificio
                                    BuildingManagerItem bmi = BuildingManager.getItem(parameter.getNodeValue());
                                    if (bmi != null) {
                                        sName = bmi.getName();
                                        if (TownsProperties.DEBUG_MODE) {
                                            sName += " (" + bmi.getIniHeader() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                                        }
                                    }
                                } else if (code.getNodeValue().equalsIgnoreCase(CommandPanel.COMMAND_CREATE_ZONE)) {
                                    ZoneManagerItem zmi = ZoneManager.getItem(parameter.getNodeValue());
                                    if (zmi != null) {
                                        sName = zmi.getName();
                                        if (TownsProperties.DEBUG_MODE) {
                                            sName += " (" + zmi.getIniHeader() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                                        }
                                    }
                                } else {
                                    // Item
                                    ItemManagerItem imi = ItemManager.getItem(parameter.getNodeValue());
                                    if (imi != null) {
                                        sName = imi.getName();
                                        if (TownsProperties.DEBUG_MODE) {
                                            sName += " (" + imi.getIniHeader() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                                        }
                                    }
                                }
                                if (sName == null || sName.trim().length() == 0) {
                                    Log.log(Log.LEVEL_ERROR, Messages.getString("SmartMenu.0") + parameter.getNodeValue() + "]", Messages.getString("SmartMenu.5")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    Game.exit();
                                }
                            } else {
                                Log.log(Log.LEVEL_ERROR, Messages.getString("SmartMenu.4"), "SmartMenu"); //$NON-NLS-1$ //$NON-NLS-2$
                                Game.exit();
                            }
                        }

                        if (parameter != null) {
                            item = new SmartMenu(SmartMenu.TYPE_ITEM, sName, null, code.getNodeValue(), parameter.getNodeValue()); //$NON-NLS-1$
                        } else {
                            item = new SmartMenu(SmartMenu.TYPE_ITEM, sName, null, code.getNodeValue(), null); //$NON-NLS-1$
                        }

                        // ID
                        if (map.getNamedItem("id") != null) { //$NON-NLS-1$
                            item.setID(map.getNamedItem("id").getNodeValue()); //$NON-NLS-1$
                        }

                        // Icono
                        if (map.getNamedItem("icon") != null) { //$NON-NLS-1$
                            item.setIcon(map.getNamedItem("icon").getNodeValue()); //$NON-NLS-1$
                            item.setIconType(ICON_TYPE_UI);
                        } else {
                            if (code != null && code.getNodeValue() != null && parameter != null && parameter.getNodeValue() != null) {
                                String sCode = code.getNodeValue();
                                String sParameter = parameter.getNodeValue();
                                // Miramos si es un código de crear objeto, en ese caso el icono se pilla según el mismo
                                if (sCode.equals(CommandPanel.COMMAND_QUEUE) || sCode.equals(CommandPanel.COMMAND_QUEUE_AND_PLACE) || sCode.equals(CommandPanel.COMMAND_QUEUE_AND_PLACE_ROW) || sCode.equals(CommandPanel.COMMAND_QUEUE_AND_PLACE_AREA)) {
                                    ActionManagerItem ami = ActionManager.getItem(sParameter);

                                    if (ami != null && ami.getGeneratedItem() != null) {
                                        item.setIcon(ami.getGeneratedItem());
                                        item.setIconType(ICON_TYPE_ITEM);
                                    }
                                } else if (sCode.equals(CommandPanel.COMMAND_CREATE) || sCode.equals(CommandPanel.COMMAND_CREATE_AND_PLACE) || sCode.equals(CommandPanel.COMMAND_CREATE_AND_PLACE_ROW) || sCode.equals(CommandPanel.COMMAND_CREATE_IN_A_BUILDING)) {
                                    item.setIcon(sParameter);
                                    item.setIconType(ICON_TYPE_ITEM);
                                }
                            }
                        }

                        // Prerequisitos
                        setPrerequisites(item, code, parameter);

                        // Si es un back lo añadimos tal cual, en otro caso miramos que no haya un back, para añadirlo justo antes
                        if (item.getCommand() != null && item.getCommand().equalsIgnoreCase(CommandPanel.COMMAND_BACK)) {
                            smartMenu.addItem(item);
                        } else {
                            // Miramos que el último no sea un back
                            if (smartMenu.getItems().size() > 0) {
                                SmartMenu smLast = smartMenu.getItems().get(smartMenu.getItems().size() - 1);
                                if (smLast.getCommand() != null && smLast.getCommand().equals(CommandPanel.COMMAND_BACK)) {
                                    // Hay un back, añadimos el item justo antes
                                    smLast = smartMenu.getItems().remove(smartMenu.getItems().size() - 1);
                                    smartMenu.addItem(item);
                                    smartMenu.addItem(smLast);
                                } else {
                                    smartMenu.addItem(item);
                                }
                            } else {
                                smartMenu.addItem(item);
                            }
                        }
                    }
                } else {
                    // Submenu
                    String sMenuID = node.getNodeName();

                    int iIndex = -1;
                    for (int s = 0; s < smartMenu.getItems().size(); s++) {
                        SmartMenu smAux = smartMenu.getItems().get(s);
                        if (smAux.getType() == SmartMenu.TYPE_MENU) {
                            if (smAux.getParameter() != null && smAux.getParameter().equalsIgnoreCase(sMenuID)) {
                                // Bingo
                                iIndex = s;
                                break;
                            }
                        }
                    }

                    // Mod cambiando valores de un item que ya existe?
                    boolean bModChangingValues = (iIndex != -1 && !bLoadingMain);
                    if (map.getNamedItem("delete") != null && map.getNamedItem("delete").getNodeValue() != null && map.getNamedItem("delete").getNodeValue().equalsIgnoreCase("TRUE")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                        // Borramos el submenu
                        if (bModChangingValues) {
                            smartMenu.getItems().remove(iIndex);
                        }
                        continue;
                    } else if (map.getNamedItem("deleteContent") != null && map.getNamedItem("deleteContent").getNodeValue() != null && map.getNamedItem("deleteContent").getNodeValue().equalsIgnoreCase("TRUE")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                        if (bModChangingValues) {
                            SmartMenu sm = smartMenu.getItems().get(iIndex);
                            if (sm.getItems() != null) {
                                sm.getItems().clear();
                            }
                        }
                        continue;
                    }

                    String sName = null;
                    if (map.getNamedItem(sLocale) != null) {
                        sName = map.getNamedItem(sLocale).getNodeValue();
                    }
                    if (sName == null || sName.length() == 0) {
                        if (map.getNamedItem("name") != null && map.getNamedItem("name").getNodeValue() != null && map.getNamedItem("name").getNodeValue().length() > 0) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            sName = map.getNamedItem("name").getNodeValue(); //$NON-NLS-1$
                        }
                    }

                    SmartMenu subMenu;
                    if (bModChangingValues) {
                        subMenu = smartMenu.getItems().get(iIndex);
                    } else {
                        subMenu = new SmartMenu(SmartMenu.TYPE_MENU, sName, smartMenu, null, sMenuID);
                        subMenu.setID (sMenuID);
                    }

                    if (sName != null) {
                        subMenu.setName(sName);
                    }

                    readXMLItem(doc, node.getChildNodes(), subMenu, bLoadingMain);

                    if (map.getNamedItem("icon") != null) { //$NON-NLS-1$
                        subMenu.setIcon(map.getNamedItem("icon").getNodeValue()); //$NON-NLS-1$
                        subMenu.setIconType(ICON_TYPE_UI);
                    }

                    if (!bModChangingValues) {
                        smartMenu.addItem(subMenu);
                    }
                }
            }
        }
    }

    private static void setPrerequisites(SmartMenu item, Node code, Node parameter) {
        final ColorGL COLOR_BUILDING = new ColorGL(Color.GREEN);
        final ColorGL COLOR_ZONE = new ColorGL(Color.YELLOW);
        final ColorGL COLOR_HABITAT = new ColorGL(Color.YELLOW.darker());
        final ColorGL COLOR_PREREQUISITES = new ColorGL(Color.GREEN.darker());
        if (code != null && code.getNodeValue() != null && parameter != null && parameter.getNodeValue() != null) {
            String sCode = code.getNodeValue();
            String sParameter = parameter.getNodeValue();
            // Miramos si es un código de crear objeto, en ese caso el icono se pilla según el mismo
            ItemManagerItem imi = null;
            LivingEntityManagerItem lemi = null;
            ArrayList<String> alMessages = new ArrayList<>();
            ArrayList<ColorGL> alColor = new ArrayList<>();
            if (sCode.equals(CommandPanel.COMMAND_QUEUE) || sCode.equals(CommandPanel.COMMAND_QUEUE_AND_PLACE) || sCode.equals(CommandPanel.COMMAND_QUEUE_AND_PLACE_ROW) || sCode.equals(CommandPanel.COMMAND_QUEUE_AND_PLACE_AREA)) {
                ArrayList<String> alMessagesBuilding = new ArrayList<>();
                ArrayList<String> alMessagesPrerequisites = new ArrayList<>();
                ActionManagerItem ami = ActionManager.getItem(sParameter);
                if (ami != null && ami.getQueue() != null) {
                    ArrayList<QueueItem> alQueue = ami.getQueue();
                    String sName;
                    for (int i = 0; i < alQueue.size(); i++) {
                        if (alQueue.get(i).getType() == QueueItem.TYPE_MOVE || alQueue.get(i).getType() == QueueItem.TYPE_PICK) {
                            ArrayList<String> alList = Utils.getArray(alQueue.get(i).getValue());
                            ArrayList<String> alListNames = new ArrayList<>();
                            sName = null;
                            if (alList != null) {
                                for (int ite = 0; ite < alList.size(); ite++) {
                                    imi = ItemManager.getItem(alList.get(ite));
                                    if (imi != null && imi.getName() != null) {
                                        sName = imi.getName();

                                        if (!alListNames.contains(sName)) {
                                            alListNames.add(sName);
                                        }
                                    }
                                }

                                sName = null;
                                // Creamos el name gordo
                                for (int ite = 0; ite < alListNames.size(); ite++) {
                                    if (ite == 0) {
                                        sName = alListNames.get(ite);
                                    } else {
                                        sName += Messages.getString("SmartMenu.3") + alListNames.get(ite); //$NON-NLS-1$
                                    }
                                }
                                if (sName != null) {
                                    if (alQueue.get(i).getType() == QueueItem.TYPE_MOVE) {
                                        if (!alMessagesBuilding.contains(sName)) {
                                            alMessagesBuilding.add(sName);
                                        }
                                    } else {
                                        alMessagesPrerequisites.add(sName);
                                    }
                                }
                            }
                        } else if (alQueue.get(i).getType() == QueueItem.TYPE_PICK_FRIENDLY) {
                            ArrayList<String> alList = Utils.getArray(alQueue.get(i).getValue());
                            ArrayList<String> alListNames = new ArrayList<>();
                            sName = null;
                            if (alList != null) {
                                for (int ite = 0; ite < alList.size(); ite++) {
                                    lemi = LivingEntityManager.getItem(alList.get(ite));
                                    if (lemi != null && lemi.getName() != null) {
                                        sName = lemi.getName();

                                        if (!alListNames.contains(sName)) {
                                            alListNames.add(sName);
                                        }
                                    }
                                }

                                sName = null;
                                // Creamos el name gordo
                                for (int ite = 0; ite < alListNames.size(); ite++) {
                                    if (ite == 0) {
                                        sName = alListNames.get(ite);
                                    } else {
                                        sName += Messages.getString("SmartMenu.3") + alListNames.get(ite); //$NON-NLS-1$
                                    }
                                }
                                if (sName != null) {
                                    alMessagesPrerequisites.add(sName);
                                }
                            }
                        } else if (alQueue.get(i).getType() == QueueItem.TYPE_CREATE_ITEM) {
                            imi = ItemManager.getItem(alQueue.get(i).getValue());
                            if (imi != null) {
                                if (imi.isMilitaryItem()) {
                                    // Item militar, ponemos los min/max de los atributos
                                    String sMilitaryAttributes = imi.getMilitaryString();
                                    if (sMilitaryAttributes != null && sMilitaryAttributes.length() > 0) {
                                        alMessages.add(sMilitaryAttributes);
                                        alColor.add(new ColorGL(null));
                                    }
                                }
                                if (imi.canBeEaten()) {
                                    // Item de comida, ponemos los porcentajes
                                    String sEatAttributes = imi.getEatString();
                                    if (sEatAttributes != null && sEatAttributes.length() > 0) {
                                        alMessages.add(sEatAttributes);
                                        alColor.add(new ColorGL(null));
                                    }
                                }
                                if (imi.isBlockFluids()) {
                                    alMessages.add("(" + Messages.getString("SmartMenu.8") + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    alColor.add(COLOR_HABITAT);
                                }
                                ArrayList<String> alDescs = imi.getDescriptions();
                                if (alDescs != null) {
                                    for (int d = 0; d < alDescs.size(); d++) {
                                        alMessages.add(alDescs.get(d));
                                        alColor.add(new ColorGL(null));
                                    }
                                }
                            }
                        }
                    }

                    if (alMessages.size() > 0) { // Tiene descripciones
                        if (alMessagesBuilding.size() > 0 || alMessagesPrerequisites.size() > 0) {
                            // Linea en blanco
                            alMessages.add(new String());
                            alColor.add(new ColorGL(null));
                        }
                    }

                    // "Building" (o objetos "move")
                    for (int i = 0; i < alMessagesBuilding.size(); i++) {
                        alMessages.add(alMessagesBuilding.get(i));
                        alColor.add(COLOR_BUILDING);
                    }

                    if (alMessagesBuilding.size() > 0) {
                        if (alMessagesPrerequisites.size() > 0) {
                            // Item en blanco
                            alMessages.add(new String());
                            alColor.add(new ColorGL(null));
                        }
                    }

                    // Prerequisites (o objetos "pick")
                    for (int i = 0; i < alMessagesPrerequisites.size(); i++) {
                        alMessages.add(alMessagesPrerequisites.get(i));
                        alColor.add(COLOR_PREREQUISITES);
                    }
                }
            } else if (sCode.equals(CommandPanel.COMMAND_CREATE) || sCode.equals(CommandPanel.COMMAND_CREATE_AND_PLACE) || sCode.equals(CommandPanel.COMMAND_CREATE_AND_PLACE_ROW) || sCode.equals(CommandPanel.COMMAND_CREATE_IN_A_BUILDING)) {
                imi = ItemManager.getItem(sParameter);
                if (imi != null) {
                    if (imi.getDescriptions() != null) {
                        ArrayList<String> alDescs = imi.getDescriptions();
                        if (alDescs != null && alDescs.size() > 0) {
                            for (int d = 0; d < alDescs.size(); d++) {
                                alMessages.add(alDescs.get(d));
                                alColor.add(new ColorGL(null));
                            }
                        }
                    }

                    if (imi.getBuilding() != null) {
                        BuildingManagerItem bmi = BuildingManager.getItem(imi.getBuilding());
                        if (bmi != null) {
                            if (imi.getDescriptions() != null && imi.getDescriptions().size() > 0) {
                                // Linea en blanco
                                alMessages.add(new String());
                                alColor.add(new ColorGL(null));
                            }

                            alMessages.add(bmi.getName());
                            alColor.add(COLOR_BUILDING);

                            ArrayList<String> alPrerequisites = imi.getPrerequisites();
                            for (int i = 0; i < alPrerequisites.size(); i++) {
                                imi = ItemManager.getItem(alPrerequisites.get(i));
                                if (imi != null) {
                                    if (alMessages.size() == 1) {
                                        // Linea en blanco
                                        alMessages.add(new String());
                                        alColor.add(new ColorGL(null));
                                    }
                                    alMessages.add(imi.getName());
                                    alColor.add(COLOR_PREREQUISITES);
                                }
                            }
                        }
                    }
                }
            } else if (sCode.equals(CommandPanel.COMMAND_BUILD)) {
                BuildingManagerItem bmi = BuildingManager.getItem(sParameter);
                if (bmi != null) {
                    // Description
                    ArrayList<String> alDescs = bmi.getDescriptions();
                    if (alDescs != null) {
                        for (int d = 0; d < alDescs.size(); d++) {
                            alMessages.add(alDescs.get(d));
                            alColor.add(new ColorGL(null));
                        }
                    }

                    // Linea en blanco
                    alMessages.add(new String());
                    alColor.add(new ColorGL(null));

                    ArrayList<int[]> alPrerequisites = bmi.getPrerequisites();
                    if (alPrerequisites != null) {
                        for (int i = 0; i < alPrerequisites.size(); i++) {
                            int[] aItems = alPrerequisites.get(i);
                            String sName = null;
                            for (int ite = 0; ite < aItems.length; ite++) {
                                if (ite == 0) {
                                    sName = ItemManager.getItem(UtilsIniHeaders.getStringIniHeader(aItems[ite])).getName();
                                } else {
                                    sName += Messages.getString("SmartMenu.3") + ItemManager.getItem(UtilsIniHeaders.getStringIniHeader(aItems[ite])).getName(); //$NON-NLS-1$
                                }
                            }
                            if (sName != null) {
                                alMessages.add(sName);
                                alColor.add(COLOR_PREREQUISITES);
                            }
                        }
                    }
                    alPrerequisites = bmi.getPrerequisitesFriendly();
                    if (alPrerequisites != null) {
                        for (int i = 0; i < alPrerequisites.size(); i++) {
                            int[] aLivings = alPrerequisites.get(i);
                            String sName = null;
                            for (int liv = 0; liv < aLivings.length; liv++) {
                                if (liv == 0) {
                                    sName = LivingEntityManager.getItem(UtilsIniHeaders.getStringIniHeader(aLivings[liv])).getName();
                                } else {
                                    sName += Messages.getString("SmartMenu.3") + LivingEntityManager.getItem(UtilsIniHeaders.getStringIniHeader(aLivings[liv])).getName(); //$NON-NLS-1$
                                }
                            }
                            if (sName != null) {
                                alMessages.add(sName);
                                alColor.add(COLOR_PREREQUISITES);
                            }
                        }
                    }
                }
            }

            // Seteamos los prerequisitos
            if (alMessages.size() > 0) {
                alMessages.add(0, item.getName());
                alColor.add(0, new ColorGL(null));

                // Añadimos zonas
                boolean bBlankLineAdded = false;
                if (imi != null && imi.getZones() != null && imi.getZones().size() > 0) {
                    // Linea en blanco
                    bBlankLineAdded = true;
                    alMessages.add(new String());
                    alColor.add(new ColorGL(null));
                    for (int i = 0; i < imi.getZones().size(); i++) {
                        alMessages.add(ZoneManager.getItem(imi.getZones().get(i)).getName());
                        alColor.add(COLOR_ZONE);
                    }
                }

                if (imi != null && imi.getHabitat() != null && imi.getHabitat().size() > 0) {
                    // Linea en blanco
                    if (!bBlankLineAdded) {
                        alMessages.add(new String());
                        alColor.add(new ColorGL(null));
                    }
                    String sHabitat;
                    for (int i = 0; i < imi.getHabitat().size(); i++) {
                        sHabitat = TerrainManager.getItemByID(imi.getHabitat().get(i)).getName();
                        if (!alMessages.contains(sHabitat)) {
                            alMessages.add(sHabitat);
                            alColor.add(COLOR_HABITAT);
                        }
                    }
                }

                item.setPrerequisites(alMessages);
                item.setPrerequisitesColor(alColor);
            }
        }
    }

    /**
     * Comprueba si se ha clicado en un submenu o en un item En el primer caso
     * devuelve dicho submenú En el segundo caso ejecuta la acción
     * correspondiente y se devuelve él mismo
     *
     * @param x X Relativa al menú
     * @param y Y relativa al menú
     * @return
     */
    public SmartMenu mousePressed(int x, int y) {
        // Miramos donde ha clicado
        int iMenuIndex = y / UtilFont.MAX_HEIGHT; // Posición donde ha clicado

        if (iMenuIndex >= getItems().size() || y < 0) {
            return this;
        }

        UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);

        SmartMenu menu = getItems().get(iMenuIndex);
        if (menu.getType() == SmartMenu.TYPE_MENU) {
            return menu;
        } else if (menu.getType() == SmartMenu.TYPE_ITEM) {
            if (menu.getCommand().equals(CommandPanel.COMMAND_BACK)) {
                return getParent();
            } else if (menu.getCommand().equals(CommandPanel.COMMAND_CLOSE_CONTEXT)) {
                Game.deleteCurrentContextMenu();
                return null;
            } else {
                CommandPanel.executeCommand(menu.getCommand(), menu.getParameter(), menu.getParameter2(), menu.getDirectCoordinates(), menu.getIcon(), menu.getIconType());
                if (Game.getCurrentState() == Game.STATE_SHOWING_CONTEXT_MENU && !menu.getCommand().equals(CommandPanel.COMMAND_EXIT_TO_MAIN_MENU)) {
                    if (menu.isMaintainOpen()) {
                        return this;
                    }
                    Game.deleteCurrentContextMenu();
                    return null;
//				} else if (menu.getCommand ().equals (CommandPanel.COMMAND_SAVE_OPTIONS)) {
//					return getParent ();
                }
            }
        }

        return this;
    }

    /**
     * Divide el menú en varias pantallas en el caso de que sea demasiado ganso
     *
     * @param menu
     */
    public static SmartMenu split(SmartMenu menu, int parts) {
        if (parts <= 1) {
            return menu;
        }

        int iPartSize = (menu.getItems().size() / parts) + 1;
        ArrayList<SmartMenu> alParts = new ArrayList<>();

        // Creamos los menus
        for (int i = 0; i < parts; i++) {
            SmartMenu sm;
            if (i > 0) {
                sm = new SmartMenu(TYPE_MENU, "(" + i + " / " + parts + ") ---->", menu.getParent(), null, null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            } else {
                sm = new SmartMenu(menu.getType(), menu.getName(), menu.getParent(), menu.getCommand(), menu.getParameter(), menu.getParameter2(), menu.getDirectCoordinates(), menu.getColor().toColor());
            }
            sm.setTrasparency(menu.isTrasparency());
            sm.setBorderColor(menu.getBorderColor());
            for (int j = 0; j < iPartSize; j++) {
                if ((i * iPartSize + j) >= menu.getItems().size()) {
                    break;
                }
                sm.addItem(menu.getItems().get(i * iPartSize + j));
            }

            alParts.add(sm);
        }

        // Añadimos los forwards
        for (int i = 0; i < alParts.size(); i++) {
            if (i < (alParts.size() - 1)) {
                alParts.get(i).addItem(new SmartMenu(TYPE_TEXT, null, null, null, null));

                // Forward
                alParts.get(i).addItem(alParts.get(i + 1));
            }
        }

        return alParts.get(0);
    }

    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        type = in.readInt();
        name = (String) in.readObject();
        parent = (SmartMenu) in.readObject();
        items = (ArrayList<SmartMenu>) in.readObject();
        command = (String) in.readObject();
        parameter = (String) in.readObject();
        parameter2 = (String) in.readObject();
        directCoordinates = (Point3D) in.readObject();
        color = (ColorGL) in.readObject();
        trasparency = in.readBoolean();
        dynamic = in.readBoolean();
        maintainOpen = in.readBoolean();
        borderColor = (ColorGL) in.readObject();
        icon = (Tile) in.readObject();
        iconType = in.readInt();
        prerequisites = (ArrayList<String>) in.readObject();
        prerequisitesColor = (ArrayList<ColorGL>) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(type);
        out.writeObject(name);
        out.writeObject(parent);
        out.writeObject(items);
        out.writeObject(command);
        out.writeObject(parameter);
        out.writeObject(parameter2);
        out.writeObject(directCoordinates);
        out.writeObject(color);
        out.writeBoolean(trasparency);
        out.writeBoolean(dynamic);
        out.writeBoolean(maintainOpen);
        out.writeObject(borderColor);
        out.writeObject(icon);
        out.writeInt(iconType);
        out.writeObject(prerequisites);
        out.writeObject(prerequisitesColor);
    }
}
