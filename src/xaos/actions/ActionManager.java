package xaos.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import xaos.Towns;
import xaos.main.Game;
import xaos.utils.Log;
import xaos.utils.Messages;
import xaos.utils.UtilsXML;

public class ActionManager {

    private static HashMap<String, ActionManagerItem> itemList;

    public static void loadItems() {
        itemList = new HashMap<String, ActionManagerItem>();

        // Cargar de fichero
        loadXMLActions(Towns.getPropertiesString("DATA_FOLDER") + "actions.xml", true);

        // Mods
        File fUserFolder = new File(Game.getUserFolder());
        if (!fUserFolder.exists() || !fUserFolder.isDirectory()) {
            return;
        }

        ArrayList<String> alMods = Game.getModsLoaded();
        if (alMods != null && alMods.size() > 0) {
            for (int i = 0; i < alMods.size(); i++) {
                String sModActionsPath = fUserFolder.getAbsolutePath() + System.getProperty("file.separator") + Game.MODS_FOLDER1 + System.getProperty("file.separator") + alMods.get(i) + System.getProperty("file.separator") + Towns.getPropertiesString("DATA_FOLDER") + "actions.xml";
                File fIni = new File(sModActionsPath);
                if (fIni.exists()) {
                    loadXMLActions(sModActionsPath, false);
                }
            }
        }

		// Una vez todo cargado hacemos comprobaciones
        // PriorityID
        Iterator<String> iterator = itemList.keySet().iterator();
        String sAux, priorityID;
        while (iterator.hasNext()) {
            sAux = iterator.next();
            priorityID = itemList.get(sAux).getPriorityID();

            if (priorityID == null) {
                Log.log(Log.LEVEL_ERROR, Messages.getString("ActionManagerItem.4") + sAux + "]", "ActionManager"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                Game.exit();
            }

            if (priorityID != null) {
                if (ActionPriorityManager.getItem(priorityID) == null) {
                    Log.log(Log.LEVEL_ERROR, Messages.getString("ActionManagerItem.2") + sAux + "] [" + priorityID + "]", "ActionManager"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    Game.exit();
                }
            }
        }
    }

    public static ActionManagerItem getItem(String sID) {
        if (itemList == null) {
            loadItems();
        }

        return itemList.get(sID);
    }

    private static void loadXMLActions(String sPath, boolean bLoadingMain) {
        String sID = null;
        try {
            Document doc = UtilsXML.loadXMLFile(sPath);

			// Tenemos el documento XML parseado
            // Lo recorremos entero y vamos añadiendo a la hash
            NodeList nodeList = doc.getDocumentElement().getChildNodes();
            Node node;
            ActionManagerItem item;
            for (int i = 0; i < nodeList.getLength(); i++) {
                node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    String sName = node.getNodeName();
                    if (sName != null && sName.equalsIgnoreCase("DELETE")) {
                        // Miramos que ID quiere borrar
                        if (node.getAttributes() != null && node.getAttributes().getNamedItem("id") != null) {
                            String sIDToDelete = node.getAttributes().getNamedItem("id").getNodeValue();
                            if (sIDToDelete != null) {
                                itemList.remove(sIDToDelete);
                            }
                        }
                        continue;
                    }

                    sID = UtilsXML.getChildValue(node.getChildNodes(), "id"); //$NON-NLS-1$

                    boolean bExists = itemList.containsKey(sID);
                    if (bLoadingMain && bExists) {
                        Log.log(Log.LEVEL_ERROR, Messages.getString("ActionManager.1") + sID + "]", "ActionManager"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    }
                    // Mod cambiando valores de un item que ya existe?
                    boolean bModChangingValues = (bExists && !bLoadingMain);

                    if (bModChangingValues) {
                        item = itemList.get(sID);
                    } else {
                        item = new ActionManagerItem(sID);
                    }

                    // Name
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "name"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setName(sAux);
                        }
                    } else {
                        item.setName(UtilsXML.getChildValue(node.getChildNodes(), "name")); //$NON-NLS-1$
                    }

                    // Priority
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "priorityID"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setPriorityID(sAux);
                        }
                    } else {
                        item.setPriorityID(UtilsXML.getChildValue(node.getChildNodes(), "priorityID")); //$NON-NLS-1$
                    }

                    // Turns
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "turns"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setTurns(sAux);
                        }
                    } else {
                        item.setTurns(UtilsXML.getChildValue(node.getChildNodes(), "turns")); //$NON-NLS-1$
                    }

                    // Effect
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "effect"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setEffect(sAux);
                        }
                    } else {
                        item.setEffect(UtilsXML.getChildValue(node.getChildNodes(), "effect")); //$NON-NLS-1$
                    }

                    // Killsource?
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "killsSource"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setKillsSource(sAux);
                        }
                    } else {
                        item.setKillsSource(UtilsXML.getChildValue(node.getChildNodes(), "killsSource")); //$NON-NLS-1$
                    }

                    // Inverted
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "inverted"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setInverted(sAux);
                        }
                    } else {
                        item.setInverted(UtilsXML.getChildValue(node.getChildNodes(), "inverted")); //$NON-NLS-1$
                    }

                    // Generated item
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "generatedItem"); //$NON-NLS-1$
                        if (sAux != null && sAux.length() > 0) {
                            item.setGeneratedItem(sAux);
                        }
                    } else {
                        String sGeneratedItem = UtilsXML.getChildValue(node.getChildNodes(), "generatedItem"); //$NON-NLS-1$
                        if (sGeneratedItem != null && sGeneratedItem.length() > 0) {
                            item.setGeneratedItem(sGeneratedItem);
                        }
                    }

                    // Queue
                    if (bModChangingValues) {
                        ArrayList<QueueItem> auxQueue = readQueue(node.getChildNodes());
                        if (auxQueue != null && auxQueue.size() > 0) {
                            item.setQueue(auxQueue);
                        }
                    } else {
                        item.setQueue(readQueue(node.getChildNodes()));
                    }

                    // Lo añadimos a la hash
                    itemList.put(sID, item);
                }
            }
        } catch (Exception e) {
            if (sID != null) {
                Log.log(Log.LEVEL_ERROR, Messages.getString("ActionManager.0") + sPath + "] [" + sID + "][" + e.getMessage() + "]", "ActionManager"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            } else {
                Log.log(Log.LEVEL_ERROR, Messages.getString("ActionManager.0") + sPath + "] [" + e.getMessage() + "]", "ActionManager"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            }
            Game.exit();
        }
    }

    private static ArrayList<QueueItem> readQueue(NodeList list) throws Exception {
        Node node;

        ArrayList<QueueItem> returnQueue = null;

        try {
            for (int i = 0; i < list.getLength(); i++) {
                node = list.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("queue") && node.getChildNodes().item(0) != null) { //$NON-NLS-1$
                    // Tenemos el tag "queue", lo recorremos entero y vamos creando la queue
                    returnQueue = new ArrayList<QueueItem>();
                    NodeList nl = node.getChildNodes();
                    for (int j = 0; j < nl.getLength(); j++) {
                        node = nl.item(j);
                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                            QueueItem qi = new QueueItem();
                            qi.setType(node.getNodeName());
                            if (node.getChildNodes().item(0) != null) {
                                qi.setValue(node.getChildNodes().item(0).getNodeValue());
                            }
                            if (node.getAttributes() != null) {
                                if (node.getAttributes().getNamedItem("useSource") != null) {
                                    if (node.getAttributes().getNamedItem("useSource").getNodeValue() != null && node.getAttributes().getNamedItem("useSource").getNodeValue().equalsIgnoreCase("TRUE")) {
                                        qi.setUseSource(true);
                                    }
                                }

                                if (node.getAttributes().getNamedItem("fx") != null) {
                                    qi.setFx(node.getAttributes().getNamedItem("fx").getNodeValue());
                                }

                                if (node.getAttributes().getNamedItem("fxTurns") != null) {
                                    qi.setFxTurns(node.getAttributes().getNamedItem("fxTurns").getNodeValue());
                                }

                                if (node.getAttributes().getNamedItem("godStatus") != null) {
                                    qi.setGodStatus(node.getAttributes().getNamedItem("godStatus").getNodeValue());
                                }
                            }
                            returnQueue.add(qi);
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            throw new Exception(Messages.getString("ActionManager.2") + e.getMessage() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return returnQueue;
    }

    public static void clear() {
        itemList = null;
    }
}
