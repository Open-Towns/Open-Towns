package xaos.dungeons;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import xaos.main.Game;
import xaos.main.World;
import xaos.utils.Log;
import xaos.utils.Messages;
import xaos.utils.Utils;
import xaos.utils.UtilsXML;

/**
 * Carga los datos de dungeons y tiene un array con los mismos
 *
 */
public class DungeonManager {

    private static ArrayList<DungeonData> alDungeons;

    private static void loadDungeons(String sCampaignID, String sMissionID) {
        alDungeons = new ArrayList<DungeonData>();

        try {
            Document doc = null;
            ArrayList<String> alPathToFiles = Utils.getPathToFile("gen_dungeons.xml", sCampaignID, sMissionID); //$NON-NLS-1$
            for (int p = 0; p < alPathToFiles.size(); p++) {
                doc = UtilsXML.loadXMLFile(alPathToFiles.get(p));

                if (doc != null) {
					// Tenemos el documento XML parseado
                    // Lo recorremos entero y vamos añadiendo los datos al array
                    NodeList nodeList = doc.getDocumentElement().getChildNodes();
                    Node node;
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        node = nodeList.item(i);
                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                            // Dungeon
                            String sID = null;
                            NamedNodeMap attributes = node.getAttributes();
                            if (attributes != null) {
                                Node nodeAttributes = attributes.getNamedItem("id"); //$NON-NLS-1$
                                if (nodeAttributes != null) {
                                    sID = nodeAttributes.getNodeValue();
                                }
                            }

                            // Miramos si el ID ya existe
                            int iIndexExists = -1;
                            if (sID != null && sID.trim().length() > 0) {
                                for (int index = 0; index < alDungeons.size(); index++) {
                                    DungeonData dd = alDungeons.get(index);
                                    if (dd.getId() != null && dd.getId().equals(sID)) {
                                        // Bingo
                                        iIndexExists = index;
                                        break;
                                    }
                                }
                            }

                            // Si ya existe lo borramos
                            if (iIndexExists != -1) {
                                if (i == 0) {
                                    Log.log(Log.LEVEL_DEBUG, Messages.getString("Generator.1") + " [" + sID + "]", "DungeonManager"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                }
                                alDungeons.remove(iIndexExists);
                            }

                            String sNodename = node.getNodeName();
                            if (sNodename != null && sNodename.equalsIgnoreCase("DELETE")) { //$NON-NLS-1$
                                // No hacemos nada pq ya se ha borrado
                            } else {
                                DungeonData dungeonData = new DungeonData();
                                dungeonData.setId(sID);
                                dungeonData.setLevel(UtilsXML.getChildValue(node.getChildNodes(), "level")); //$NON-NLS-1$
                                // Le sumamos los niveles outside al level
                                dungeonData.setLevel((short) (dungeonData.getLevel() + World.MAP_NUM_LEVELS_OUTSIDE - 1));
                                dungeonData.setType(UtilsXML.getChildValue(node.getChildNodes(), "type")); //$NON-NLS-1$

                                // Monsters
                                dungeonData.setMonsters(loadMonsters(node.getChildNodes()));

                                // Lo añadimos al array
                                if (iIndexExists != -1) {
                                    alDungeons.add(iIndexExists, dungeonData);
                                } else {
                                    alDungeons.add(dungeonData);
                                }
                            }
                        }
                    }
                } else {
                    Log.log(Log.LEVEL_ERROR, Messages.getString("DungeonManager.0"), "DungeonManager"); //$NON-NLS-1$ //$NON-NLS-2$
                    Game.exit();
                }
            }
        } catch (Exception e) {
            Log.log(Log.LEVEL_ERROR, Messages.getString("DungeonManager.0"), "DungeonManager"); //$NON-NLS-1$ //$NON-NLS-2$
            Game.exit();
        }
    }

    private static ArrayList<MonsterData> loadMonsters(NodeList nodeList) throws Exception {
        ArrayList<MonsterData> alMonsters = new ArrayList<MonsterData>();

        Node node;
        for (int i = 0; i < nodeList.getLength(); i++) {
            node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("monster")) { //$NON-NLS-1$
                MonsterData monsterData = new MonsterData();

                monsterData.setId(UtilsXML.getChildValue(node.getChildNodes(), "id")); //$NON-NLS-1$
                monsterData.setNumber(UtilsXML.getChildValue(node.getChildNodes(), "number")); //$NON-NLS-1$
                monsterData.setLevelMin(UtilsXML.getChildValue(node.getChildNodes(), "levelMin")); //$NON-NLS-1$
                monsterData.setLevelMax(UtilsXML.getChildValue(node.getChildNodes(), "levelMax")); //$NON-NLS-1$
                alMonsters.add(monsterData);
            }
        }

        return alMonsters;
    }

    public static ArrayList<DungeonData> getDungeons(String sCampaignID, String sMissionID) {
        if (alDungeons == null) {
            loadDungeons(sCampaignID, sMissionID);
        }

        return alDungeons;
    }

    public static void clear() {
        alDungeons = null;
    }
}
