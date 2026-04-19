package xaos.caravans;

import java.io.File;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import xaos.Towns;
import xaos.main.Game;
import xaos.tiles.entities.items.Item;
import xaos.tiles.entities.items.ItemManager;
import xaos.tiles.entities.items.military.MilitaryItem;
import xaos.utils.Log;
import xaos.utils.UtilsXML;

/**
 * Indica cada cuanta cantidad de (ataque/defensa/...) hay que sumar 1 al precio
 * de las cosas
 */
public class PricesManager {

    public static int attack = -1;
    public static int defense = -1;
    public static int damage = -1;
    public static int LOS = -1;

    private static void loadPrices() {
        if (attack == -1) {
            loadXMLPrices(Towns.getPropertiesString("DATA_FOLDER") + "prices.xml");

            // Mods
            File fUserFolder = new File(Game.getUserFolder());
            if (!fUserFolder.exists() || !fUserFolder.isDirectory()) {
                return;
            }

            ArrayList<String> alMods = Game.getModsLoaded();
            if (alMods != null && alMods.size() > 0) {
                for (int i = 0; i < alMods.size(); i++) {
                    String sModActionsPath = fUserFolder.getAbsolutePath() + System.getProperty("file.separator") + Game.MODS_FOLDER1 + System.getProperty("file.separator") + alMods.get(i) + System.getProperty("file.separator") + Towns.getPropertiesString("DATA_FOLDER") + "prices.xml"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                    File fIni = new File(sModActionsPath);
                    if (fIni.exists()) {
                        loadXMLPrices(sModActionsPath);
                    }
                }
            }
        }
    }

    private static void loadXMLPrices(String sXMLPath) {
        try {
            Document doc = UtilsXML.loadXMLFile(sXMLPath); //$NON-NLS-1$ //$NON-NLS-2$
            NodeList nodeList = doc.getDocumentElement().getChildNodes();
            Node node;
            String sNodeName;
            String sTmp;
            int iTmp;
            for (int i = 0; i < nodeList.getLength(); i++) {
                node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    sNodeName = node.getNodeName();
                    if (sNodeName.equalsIgnoreCase("attack")) {
                        // Attack
                        sTmp = node.getChildNodes().item(0).getNodeValue();
                        iTmp = Integer.parseInt(sTmp);
                        if (iTmp < 1) {
                            iTmp = 1000;
                        }
                        attack = iTmp;
                    } else if (sNodeName.equalsIgnoreCase("defense")) {
                        // Defense
                        sTmp = node.getChildNodes().item(0).getNodeValue();
                        iTmp = Integer.parseInt(sTmp);
                        if (iTmp < 1) {
                            iTmp = 1000;
                        }
                        defense = iTmp;
                    } else if (sNodeName.equalsIgnoreCase("damage")) {
                        // Damage
                        sTmp = node.getChildNodes().item(0).getNodeValue();
                        iTmp = Integer.parseInt(sTmp);
                        if (iTmp < 1) {
                            iTmp = 1000;
                        }
                        damage = iTmp;
                    } else if (sNodeName.equalsIgnoreCase("LOS")) {
                        // LOS
                        sTmp = node.getChildNodes().item(0).getNodeValue();
                        iTmp = Integer.parseInt(sTmp);
                        if (iTmp < 1) {
                            iTmp = 1000;
                        }
                        LOS = iTmp;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.log(Log.LEVEL_ERROR, "Error reading prices.xml [" + e.toString() + "]", "PricesManager");
            Game.exit();
        }
    }

    public static int getAttackPrice() {
        if (attack == -1) {
            loadPrices();
        }
        return attack;
    }

    public static int getDefensePrice() {
        if (defense == -1) {
            loadPrices();
        }
        return defense;
    }

    public static int getDamagePrice() {
        if (damage == -1) {
            loadPrices();
        }
        return damage;
    }

    public static int getLOSPrice() {
        if (LOS == -1) {
            loadPrices();
        }
        return LOS;
    }

    /**
     * Retorna el precio basea de un item (NO militar)
     *
     * @param sIniHeader
     * @return el precio basea de un item (NO militar)
     */
    public static int getPrice(String sIniHeader) {
        if (sIniHeader == null) {
            return 0;
        }

        int value = ItemManager.getItem(sIniHeader).getValue();
        if (value < 1) {
            return 0;
        } else {
            return value;
        }
    }

    /**
     * Retorna el precio de un item, tiene en cuenta los atributos militares
     *
     * @param item
     * @return el precio de un item, tiene en cuenta los atributos militares
     */
    public static int getPrice(Item item) {
        if (item == null) {
            return 0;
        }

        if (item instanceof MilitaryItem) {
            MilitaryItem mi = (MilitaryItem) item;
            int baseValue = ItemManager.getItem(item.getIniHeader()).getValue();

            baseValue += mi.getAttackModifier() / getAttackPrice();
            baseValue += mi.getDefenseModifier() / getDefensePrice();
            baseValue += mi.getDamageModifier() / getDamagePrice();
            baseValue += mi.getLOSModifier() / getLOSPrice();

            return baseValue;
        } else {
            int value = ItemManager.getItem(item.getIniHeader()).getValue();
            if (value < 1) {
                return 0;
            } else {
                return value;
            }
        }
    }

    public static void clear() {
        attack = -1;
        defense = -1;
        damage = -1;
        LOS = -1;
    }
}
