package xaos.caravans;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import xaos.Towns;
import xaos.main.Game;
import xaos.utils.Log;
import xaos.utils.Messages;
import xaos.utils.UtilsXML;

public class CaravanManager {

    private static HashMap<String, CaravanManagerItem> caravanList;

    private static void loadItems() {
        caravanList = new HashMap<String, CaravanManagerItem>();

        String mainCaravanListPath = Towns.getPropertiesString("DATA_FOLDER") + "caravans.xml";
        loadCaravansFromXml(mainCaravanListPath, true);

        File userFolder = new File(Game.getUserFolder());
        if (!userFolder.exists() || !userFolder.isDirectory()) {
            return;
        }

        ArrayList<String> mods = Game.getModsLoaded();
        if (mods == null || mods.size() == 0) {
            return;
        }

        for (int i = 0; i < mods.size(); i++) {
            String modCaravanListPath = userFolder.getAbsolutePath()
                    + System.getProperty("file.separator")
                    + Game.MODS_FOLDER1
                    + System.getProperty("file.separator")
                    + mods.get(i)
                    + System.getProperty("file.separator")
                    + Towns.getPropertiesString("DATA_FOLDER")
                    + "caravans.xml";

            File modCaravanFile = new File(modCaravanListPath);
            if (modCaravanFile.exists()) {
                loadCaravansFromXml(modCaravanListPath, false);
            }
        }
    }

    public static CaravanManagerItem getItem(String iniHeader) {
        if (iniHeader == null) {
            return null;
        }

        if (caravanList == null) {
            loadItems();
        }

        return caravanList.get(iniHeader);
    }

    private static void loadCaravansFromXml(String caravanListPathName, boolean isMainFile) {
        try {
            Document xmlCaravanList = UtilsXML.loadXMLFile(caravanListPathName);
            NodeList caravanNodes = xmlCaravanList.getDocumentElement().getChildNodes();

            for (int i = 0; i < caravanNodes.getLength(); i++) {
                Node caravanNode = caravanNodes.item(i);

                if (caravanNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                String id = caravanNode.getNodeName();

                if (id.equalsIgnoreCase("DELETE")) {
                    deleteCaravanFromXml(caravanNode);
                    continue;
                }

                CaravanManagerItem caravanData;

                if (!isMainFile && caravanList.containsKey(id)) {
                    caravanData = caravanList.get(id);
                    applyOptionalCaravanValues(caravanData, caravanNode);
                } else {
                    caravanData = createCaravanFromXml(id, caravanNode);
                }

                caravanList.put(id, caravanData);
            }
        } catch (Exception e) {
            // message: "Error reading caravans.xml [" + e.toString() + "]"
            Log.log(Log.LEVEL_ERROR, Messages.getString("CaravanManager.0") + e.toString() + "]", "CaravanManager");
            Game.exit();
        }
    }

    private static CaravanManagerItem createCaravanFromXml(String id, Node caravanNode) throws Exception {
        CaravanManagerItem caravanData = new CaravanManagerItem();

        caravanData.setId(id);
        caravanData.setZone(UtilsXML.getChildValue(caravanNode.getChildNodes(), "zone"));
        caravanData.setPricePCT(UtilsXML.getChildValue(caravanNode.getChildNodes(), "pricePCT"));
        caravanData.setCoins(UtilsXML.getChildValue(caravanNode.getChildNodes(), "coins"));
        caravanData.setBuysString(UtilsXML.getChildValue(caravanNode.getChildNodes(), "buys"));
        caravanData.setItemList(loadCaravanItems(caravanNode.getChildNodes()));
        caravanData.setComePCT(UtilsXML.getChildValue(caravanNode.getChildNodes(), "comePCT"));

        return caravanData;
    }

    private static void applyOptionalCaravanValues(
            CaravanManagerItem caravanData,
            Node caravanNode) throws Exception {
        NodeList caravanFields = caravanNode.getChildNodes();

        setIfPresent(caravanFields, "zone", caravanData::setZone);
        setIfPresent(caravanFields, "pricePCT", caravanData::setPricePCT);
        setIfPresent(caravanFields, "coins", caravanData::setCoins);
        setIfPresent(caravanFields, "buys", caravanData::setBuysString);
        setIfPresent(caravanFields, "comePCT", caravanData::setComePCT);

        ArrayList<CaravanItemData> caravanItems = loadCaravanItems(caravanFields);
        if (caravanItems != null && !caravanItems.isEmpty()) {
            caravanData.setItemList(caravanItems);
        }
    }

    private static void setIfPresent(
            NodeList caravanFields,
            String fieldName,
            CaravanValueSetter applyValue) throws Exception {
        String fieldValue = UtilsXML.getChildValue(caravanFields, fieldName);

        if (fieldValue != null) {
            applyValue.set(fieldValue);
        }
    }

    private interface CaravanValueSetter {
        void set(String value) throws Exception;
    }

    private static void deleteCaravanFromXml(Node deleteNode) {
        if (deleteNode.getAttributes() == null || deleteNode.getAttributes().getNamedItem("id") == null) {
            return;
        }

        String idToDelete = deleteNode.getAttributes().getNamedItem("id").getNodeValue();

        if (idToDelete != null) {
            caravanList.remove(idToDelete);
        }
    }

    private static ArrayList<CaravanItemData> loadCaravanItems(NodeList caravanFields) throws Exception {
        ArrayList<CaravanItemData> itemData = new ArrayList<CaravanItemData>();

        for (int i = 0; i < caravanFields.getLength(); i++) {
            Node itemNode = caravanFields.item(i);

            if (itemNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            if (!itemNode.getNodeName().equals("item")) {
                continue;
            }

            CaravanItemData caravanItemData = new CaravanItemData();
            caravanItemData.setItemId(UtilsXML.getChildValue(itemNode.getChildNodes(), "id"));
            caravanItemData.setItemType(UtilsXML.getChildValue(itemNode.getChildNodes(), "type"));

            if (caravanItemData.getItemId() == null && caravanItemData.getItemType() == null) {
                throw new Exception(Messages.getString("CaravanManager.1"));
            }

            caravanItemData.setSpawnChancePercent(UtilsXML.getChildValue(itemNode.getChildNodes(), "PCT"));
            caravanItemData.setQuantity(UtilsXML.getChildValue(itemNode.getChildNodes(), "quantity"));

            itemData.add(caravanItemData);
        }

        return itemData;
    }

    public static void clear() {
        caravanList = null;

        PricesManager.clear();
    }
}
