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

        // Cargar de fichero
        loadXMLEffects(Towns.getPropertiesString("DATA_FOLDER") + "caravans.xml", true); //$NON-NLS-1$ //$NON-NLS-2$

        // Mods
        File fUserFolder = new File(Game.getUserFolder());
        if (!fUserFolder.exists() || !fUserFolder.isDirectory()) {
            return;
        }

        ArrayList<String> alMods = Game.getModsLoaded();
        if (alMods != null && alMods.size() > 0) {
            for (int i = 0; i < alMods.size(); i++) {
                String sModActionsPath = fUserFolder.getAbsolutePath() + System.getProperty("file.separator") + Game.MODS_FOLDER1 + System.getProperty("file.separator") + alMods.get(i) + System.getProperty("file.separator") + Towns.getPropertiesString("DATA_FOLDER") + "caravans.xml"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                File fIni = new File(sModActionsPath);
                if (fIni.exists()) {
                    loadXMLEffects(sModActionsPath, false);
                }
            }
        }
    }

    public static CaravanManagerItem getItem(String sIniHeader) {
        if (sIniHeader == null) {
            return null;
        }

        if (caravanList == null) {
            loadItems();
        }

        return caravanList.get(sIniHeader);
    }

    private static void loadXMLEffects(String sXMLName, boolean bLoadingMain) {
        try {
            Document doc = UtilsXML.loadXMLFile(sXMLName); //$NON-NLS-1$

			// Tenemos el documento XML parseado
            // Lo recorremos entero y vamos añadiendo las caravans a la hash
            NodeList nodeList = doc.getDocumentElement().getChildNodes();
            Node node;
            CaravanManagerItem caravanData;
            String sID;
            for (int i = 0; i < nodeList.getLength(); i++) {
                node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    String sName = node.getNodeName();
                    if (sName != null && sName.equalsIgnoreCase("DELETE")) {
                        // Miramos que ID quiere borrar
                        if (node.getAttributes() != null && node.getAttributes().getNamedItem("id") != null) {
                            String sIDToDelete = node.getAttributes().getNamedItem("id").getNodeValue();
                            if (sIDToDelete != null) {
                                caravanList.remove(sIDToDelete);
                            }
                        }
                        continue;
                    }

                    sID = node.getNodeName();

                    // Miramos si existe
                    boolean bExists = caravanList.containsKey(sID);
                    // Mod cambiando valores de una caravan que ya existe?
                    boolean bModChangingValues = (bExists && !bLoadingMain);

                    if (bModChangingValues) {
                        caravanData = caravanList.get(sID);

                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "zone"); //$NON-NLS-1$
                        if (sAux != null) {
                            caravanData.setZone(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "pricePCT"); //$NON-NLS-1$
                        if (sAux != null) {
                            caravanData.setPricePCT(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "coins"); //$NON-NLS-1$
                        if (sAux != null) {
                            caravanData.setCoins(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "buys"); //$NON-NLS-1$
                        if (sAux != null) {
                            caravanData.setBuysString(sAux);
                        }

                        ArrayList<CaravanItemData> alItems = loadCaravanItems(node.getChildNodes());
                        if (alItems != null && alItems.size() > 0) {
                            caravanData.setItemList(alItems);
                        }

                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "comePCT"); //$NON-NLS-1$
                        if (sAux != null) {
                            caravanData.setComePCT(sAux);
                        }
                        caravanData.setComePCT(UtilsXML.getChildValue(node.getChildNodes(), "comePCT")); //$NON-NLS-1$
                    } else {
                        caravanData = new CaravanManagerItem();
                        caravanData.setId(sID);
                        caravanData.setZone(UtilsXML.getChildValue(node.getChildNodes(), "zone")); //$NON-NLS-1$
                        caravanData.setPricePCT(UtilsXML.getChildValue(node.getChildNodes(), "pricePCT")); //$NON-NLS-1$
                        caravanData.setCoins(UtilsXML.getChildValue(node.getChildNodes(), "coins")); //$NON-NLS-1$
                        caravanData.setBuysString(UtilsXML.getChildValue(node.getChildNodes(), "buys")); //$NON-NLS-1$
                        caravanData.setItemList(loadCaravanItems(node.getChildNodes()));
                        caravanData.setComePCT(UtilsXML.getChildValue(node.getChildNodes(), "comePCT")); //$NON-NLS-1$
                    }

                    // Lo añadimos a la hash
                    caravanList.put(sID, caravanData);
                }
            }
        } catch (Exception e) {
            Log.log(Log.LEVEL_ERROR, Messages.getString("CaravanManager.0") + e.toString() + "]", "CaravanManager"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            Game.exit();
        }
    }

    private static ArrayList<CaravanItemData> loadCaravanItems(NodeList nodeList) throws Exception {
        ArrayList<CaravanItemData> alItemData = new ArrayList<CaravanItemData>();

        Node node;
        for (int i = 0; i < nodeList.getLength(); i++) {
            node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (node.getNodeName().equals("item")) { //$NON-NLS-1$
                    CaravanItemData cid = new CaravanItemData();
                    cid.setId(UtilsXML.getChildValue(node.getChildNodes(), "id")); //$NON-NLS-1$
                    cid.setType(UtilsXML.getChildValue(node.getChildNodes(), "type")); //$NON-NLS-1$
                    if (cid.getId() == null && cid.getType() == null) {
                        throw new Exception(Messages.getString("CaravanManager.1")); //$NON-NLS-1$
                    }

                    cid.setPCT(UtilsXML.getChildValue(node.getChildNodes(), "PCT")); //$NON-NLS-1$
                    cid.setQuantity(UtilsXML.getChildValue(node.getChildNodes(), "quantity")); //$NON-NLS-1$
                    alItemData.add(cid);
                }
            }
        }

        return alItemData;
    }

    public static void clear() {
        caravanList = null;

        PricesManager.clear();
    }
}
