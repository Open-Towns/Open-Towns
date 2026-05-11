
package xaos.caravans;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;

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

public class PricesManager {

    private static boolean loaded = false;
    private static final int DEFAULT_DIVISOR = 1000;
    private static final EnumMap<PriceType, Integer> priceRules = new EnumMap<>(PriceType.class);

    public enum PriceType {
        ATTACK,
        DEFENSE,
        DAMAGE,
        LOS // Line of Sight
    }

    static {
        clear();
    }

    private static void loadPrices() {
        if (loaded) {
            return;
        }

        try {
            String pricesFilePath = Towns.getPropertiesString("DATA_FOLDER") + "prices.xml";
            // mod support: check if prices.xml exists in any loaded mod and use it instead
            // of the default one
            File fUserFolder = new File(Game.getUserFolder());
            if (fUserFolder.exists() || fUserFolder.isDirectory()) {

                ArrayList<String> alMods = Game.getModsLoaded();

                if (alMods != null && alMods.size() > 0) {
                    for (int i = 0; i < alMods.size(); i++) {
                        String sModActionsPath = fUserFolder.getAbsolutePath() + System.getProperty("file.separator") //$NON-NLS-1$
                                + Game.MODS_FOLDER1 + System.getProperty("file.separator") + alMods.get(i) //$NON-NLS-1$
                                + System.getProperty("file.separator") + Towns.getPropertiesString("DATA_FOLDER") //$NON-NLS-1$ //$NON-NLS-2$
                                + "prices.xml"; //$NON-NLS-1$
                        File fIni = new File(sModActionsPath);
                        if (fIni.exists()) {
                            pricesFilePath = sModActionsPath;
                            break;
                        }
                    }
                }
            }

            Document doc = UtilsXML.loadXMLFile(pricesFilePath);
            NodeList nodeList = doc.getDocumentElement().getChildNodes();

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);

                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                String nodeName = node.getNodeName();
                int value = parseNodeValue(node);

                applyPriceRule(nodeName, value);
            }

            loaded = true;

        } catch (Exception e) {
            e.printStackTrace();

            Log.log(
                    Log.LEVEL_ERROR,
                    "Error reading prices.xml [" + e.toString() + "]",
                    "PricesManager");

            Game.exit();
        }
    }

    private static void applyPriceRule(String nodeName, int value) {
        try {
            PriceType type = PriceType.valueOf(nodeName.toUpperCase());
            priceRules.put(type, value);

        } catch (IllegalArgumentException e) {
            Log.log(
                    Log.LEVEL_ERROR,
                    "Unknown price type in prices.xml: " + nodeName,
                    "PricesManager");
        }
    }

    private static int parseNodeValue(Node node) {
        String text = node.getTextContent();
        int value = Integer.parseInt(text.trim());

        return value < 1 ? DEFAULT_DIVISOR : value;
    }

    public static int getPriceRule(PriceType type) {
        loadPrices();
        return priceRules.getOrDefault(type, DEFAULT_DIVISOR);
    }

    /**
     * Returns the base price of a non-military item
     */
    public static int getPrice(String iniHeader) {
        if (iniHeader == null) {
            return 0;
        }

        int value = ItemManager.getItem(iniHeader).getValue();
        return value < 1 ? 0 : value;
    }

    /**
     * Returns the final item price including military modifiers
     */
    public static int getPrice(Item item) {
        int baseValue = ItemManager.getItem(item.getIniHeader()).getValue();

        if (!(item instanceof MilitaryItem)) {
            return baseValue < 1 ? 0 : baseValue;
        }

        MilitaryItem militaryItem = (MilitaryItem) item;

        return calculateMilitaryPrice(
                baseValue,
                militaryItem.getAttackModifier(),
                militaryItem.getDefenseModifier(),
                militaryItem.getDamageModifier(),
                militaryItem.getLOSModifier(),
                getPriceRule(PriceType.ATTACK),
                getPriceRule(PriceType.DEFENSE),
                getPriceRule(PriceType.DAMAGE),
                getPriceRule(PriceType.LOS));

    }

    public static void clear() {
        priceRules.clear();

        for (PriceType type : PriceType.values()) {
            priceRules.put(type, DEFAULT_DIVISOR);
        }

        loaded = false;
    }

    public static int calculateMilitaryPrice(
            int baseValue,
            int attackModifier,
            int defenseModifier,
            int damageModifier,
            int losModifier,
            int attackDivisor,
            int defenseDivisor,
            int damageDivisor,
            int losDivisor) {

        if (baseValue < 1) {
            return 0;
        }

        if (attackDivisor < 1)
            attackDivisor = DEFAULT_DIVISOR;
        if (defenseDivisor < 1)
            defenseDivisor = DEFAULT_DIVISOR;
        if (damageDivisor < 1)
            damageDivisor = DEFAULT_DIVISOR;
        if (losDivisor < 1)
            losDivisor = DEFAULT_DIVISOR;

        return baseValue
                + (attackModifier / attackDivisor)
                + (defenseModifier / defenseDivisor)
                + (damageModifier / damageDivisor)
                + (losModifier / losDivisor);
    }
}