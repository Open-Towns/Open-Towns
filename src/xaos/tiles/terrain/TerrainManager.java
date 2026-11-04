package xaos.tiles.terrain;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import xaos.Towns;
import xaos.main.Game;
import xaos.tiles.Tile;
import xaos.tiles.entities.items.ItemManager;
import xaos.utils.Log;
import xaos.utils.Messages;
import xaos.utils.UtilsXML;

import java.util.Collections;
import java.util.Map;

import xaos.api.definitions.DefinitionId;
import xaos.internal.api.definitions.DefinitionKind;
import xaos.internal.api.definitions.DefinitionOwnershipRegistry;
import xaos.internal.api.mods.ModNamespaceResolver;

public class TerrainManager {

    public final static String[] SLOPES_INIHEADER = { "", "_N", "_S", "_NS", "_E", "_NE", "_SE", "_W", "_NW", "_SW", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
            "_EW", "_NSE", "_NSW", "_NEW", "_SEW", "_NSEW", "_nSEW", "_NsEW", "_NSeW", "_NSEw" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
    // public final static String[] SLOPES_INIHEADER = { "", "_N", "_S", "_NS",
    // "_E", "_NE", "_SE", "_W", "_NW", "_SW", "_EW", "_NSE", "_NSW", "_NEW",
    // "_SEW", "_NSEW"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    // //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
    // //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$ //$NON-NLS-13$ //$NON-NLS-14$
    // //$NON-NLS-15$ //$NON-NLS-16$

    private static HashMap<String, TerrainManagerItem> terrainList;
    private static ArrayList<Tile> terrainTiles;
    private static ArrayList<Tile> blockTiles;
    private static HashMap<String, ArrayList<Integer>> terrainGroups;
    private static HashMap<Integer, TerrainManagerItem> terrainListByID;

    private static void loadItems() {
        terrainList = new HashMap<String, TerrainManagerItem>();

        // Cargar de fichero
        TerrainManagerItem.CURRENT_TERRAIN_ID = 0;
        loadXMLTerrain(Towns.getPropertiesString("DATA_FOLDER") + "terrain.xml", true, DefinitionId.DEFAULT_NAMESPACE); //$NON-NLS-1$ //$NON-NLS-2$

        // Mods
        File fUserFolder = new File(Game.getUserFolder());
        if (!fUserFolder.exists() || !fUserFolder.isDirectory()) {
            return;
        }

        ArrayList<String> alMods = Game.getModsLoaded();
        if (alMods != null && alMods.size() > 0) {
            for (int i = 0; i < alMods.size(); i++) {
                String sModActionsPath = fUserFolder.getAbsolutePath() + System.getProperty("file.separator") //$NON-NLS-1$
                        + Game.MODS_FOLDER1 + System.getProperty("file.separator") + alMods.get(i) //$NON-NLS-1$
                        + System.getProperty("file.separator") + Towns.getPropertiesString("DATA_FOLDER") //$NON-NLS-1$ //$NON-NLS-2$
                        + "terrain.xml"; //$NON-NLS-1$
                File fIni = new File(sModActionsPath);
                if (fIni.exists()) {

                    String namespace = ModNamespaceResolver.resolve(
                            alMods.get(i));

                    loadXMLTerrain(
                            sModActionsPath,
                            false,
                            namespace);
                }
            }
        }

        // Generamos los groups + terrains por ID
        terrainGroups = new HashMap<String, ArrayList<Integer>>();
        terrainListByID = new HashMap<Integer, TerrainManagerItem>();

        Iterator<String> iterator = terrainList.keySet().iterator();
        String key;
        String group;
        int terrainID;
        ArrayList<Integer> alTerrains;
        while (iterator.hasNext()) {
            key = iterator.next();
            group = terrainList.get(key).getGroup();

            // Grupo
            if (group != null) {
                alTerrains = terrainGroups.get(group);
                if (alTerrains == null) {
                    alTerrains = new ArrayList<Integer>();
                }
                alTerrains.add(Integer.valueOf(terrainList.get(key).getTerrainID()));

                terrainGroups.put(group, alTerrains);
            }

            // ID
            terrainID = terrainList.get(key).getTerrainID();
            terrainListByID.put(Integer.valueOf(terrainID), terrainList.get(key));
        }

        // Tiles + check ladders + blocks
        terrainTiles = new ArrayList<Tile>(terrainListByID.size() * SLOPES_INIHEADER.length);
        blockTiles = new ArrayList<Tile>(terrainListByID.size());
        TerrainManagerItem tmi;
        for (int i = 0; i < terrainListByID.size(); i++) {
            tmi = terrainListByID.get(i);
            String sHeader = tmi.getIniHeader();
            // Tiles
            for (int iSlope = 0; iSlope < SLOPES_INIHEADER.length; iSlope++) {
                Tile tile = new Tile(sHeader);
                tile.changeGraphic(sHeader + SLOPES_INIHEADER[iSlope]);
                terrainTiles.add(tile);
            }

            // Blocks
            Tile tile = new Tile(sHeader);
            tile.changeGraphic(sHeader + "_block"); //$NON-NLS-1$
            blockTiles.add(tile);

            // Comprobamos que los ladder items existan
            if (tmi.getLadderItem() != null) {
                // El item tiene que existir
                if (ItemManager.getItem(tmi.getLadderItem()) == null) {
                    Log.log(Log.LEVEL_ERROR, Messages.getString("TerrainManager.1") + tmi.getLadderItem() + "]", //$NON-NLS-1$ //$NON-NLS-2$
                            "TerrainManager"); //$NON-NLS-1$
                    Game.exit();
                }
            }
        }
    }

    public static HashMap<String, TerrainManagerItem> getTerrainList() {
        if (terrainList == null) {
            loadItems();
        }

        return terrainList;
    }

    /**
     * Returns a read-only view of all loaded terrain definitions.
     */
    public static Map<String, TerrainManagerItem> getAllItems() {
        if (terrainList == null) {
            loadItems();
        }

        return Collections.unmodifiableMap(terrainList);
    }

    public static TerrainManagerItem getItem(String sIniHeader) {
        if (terrainList == null) {
            loadItems();
        }

        return terrainList.get(sIniHeader);
    }

    public static TerrainManagerItem getItemByID(int iID) {
        if (terrainList == null) {
            loadItems();
        }

        return terrainListByID.get(Integer.valueOf(iID));
    }

    public static Tile getTileByTileID(int iID) {
        if (terrainTiles == null) {
            loadItems();
        }

        return terrainTiles.get(iID);
    }

    public static Tile getBlockByID(int iID) {
        if (blockTiles == null) {
            loadItems();
        }

        return blockTiles.get(iID);
    }

    public static ArrayList<Integer> getTerrainsByGroup(String sGroup) {
        if (terrainGroups == null) {
            loadItems();
        }

        if (sGroup == null) {
            return null;
        }

        return terrainGroups.get(sGroup);
    }

    private static void loadXMLTerrain(String sXMLName, boolean bLoadingMain, String namespace) {
        try {
            Document doc = UtilsXML.loadXMLFile(sXMLName); // $NON-NLS-1$

            // Tenemos el documento XML parseado
            // Lo recorremos entero y vamos añadiendo los terrenos (TerrainManagerItem) a la
            // hash
            NodeList nodeList = doc.getDocumentElement().getChildNodes();
            Node node;
            TerrainManagerItem item;
            String sName, sIniHeader;

            if (!terrainList.containsKey("_AIR")) {

                TerrainManagerItem tmiAir = new TerrainManagerItem(
                        TerrainManagerItem.TERRAIN_AIR_INIHEADER,
                        Messages.getString(
                                "TerrainManager.2"));

                tmiAir.setActions(
                        new ArrayList<String>());

                tmiAir.setBlocky(
                        false);

                /*
                 * AIR is an engine-owned terrain definition rather
                 * than something supplied by an XML content source.
                 */
                DefinitionOwnershipRegistry.register(
                        DefinitionKind.TERRAIN,
                        TerrainManagerItem.TERRAIN_AIR_INIHEADER,
                        DefinitionId.DEFAULT_NAMESPACE);

                terrainList.put(
                        TerrainManagerItem.TERRAIN_AIR_INIHEADER,
                        tmiAir);
            }

            for (int i = 0; i < nodeList.getLength(); i++) {
                node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    String sNodeName = node.getNodeName();
                    if (sNodeName != null && sNodeName.equalsIgnoreCase("DELETE")) {
                        // Miramos que ID quiere borrar
                        if (node.getAttributes() != null && node.getAttributes().getNamedItem("id") != null) {
                            String sIDToDelete = node.getAttributes().getNamedItem("id").getNodeValue();
                            if (sIDToDelete != null) {

                                if (!bLoadingMain
                                        && terrainList.containsKey(sIDToDelete)
                                        && DefinitionOwnershipRegistry
                                                .conflictsWithOwner(
                                                        DefinitionKind.TERRAIN,
                                                        sIDToDelete,
                                                        namespace)) {

                                    String existingNamespace = DefinitionOwnershipRegistry.namespaceFor(
                                            DefinitionKind.TERRAIN,
                                            sIDToDelete);

                                    Log.log(
                                            Log.LEVEL_ERROR,
                                            "Definition delete collision for terrain ["
                                                    + sIDToDelete
                                                    + "]: mod namespace ["
                                                    + namespace
                                                    + "] attempted to delete a definition owned by ["
                                                    + existingNamespace
                                                    + "]. Delete ignored.",
                                            "TerrainManager");

                                    continue;
                                }

                                terrainList.remove(
                                        sIDToDelete);

                                DefinitionOwnershipRegistry.unregister(
                                        DefinitionKind.TERRAIN,
                                        sIDToDelete);
                            }
                        }
                        continue;
                    }

                    // Building
                    sIniHeader = node.getNodeName();

                    boolean bExists = terrainList.containsKey(
                            sIniHeader);

                    /*
                     * Mods may modify base-game terrain definitions, but must
                     * not accidentally overwrite terrain owned by another mod.
                     */
                    if (bExists && !bLoadingMain) {

                        if (DefinitionOwnershipRegistry
                                .conflictsWithOwner(
                                        DefinitionKind.TERRAIN,
                                        sIniHeader,
                                        namespace)) {

                            String existingNamespace = DefinitionOwnershipRegistry.namespaceFor(
                                    DefinitionKind.TERRAIN,
                                    sIniHeader);

                            Log.log(
                                    Log.LEVEL_ERROR,
                                    "Definition collision for terrain ["
                                            + sIniHeader
                                            + "]: mod namespace ["
                                            + namespace
                                            + "] attempted to use an ID already owned by ["
                                            + existingNamespace
                                            + "]. Definition ignored.",
                                    "TerrainManager");

                            continue;
                        }
                    }

                    // Mod changing values of a terrain definition that already exists?
                    boolean bModChangingValues = (bExists && !bLoadingMain);

                    if (bModChangingValues) {
                        item = terrainList.get(sIniHeader);
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "name"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setName(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "mineTurns"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setMineTurns(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "drop"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setDrop(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "dropPCT"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setDropPCT(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "ladderItem"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setLadderItem(sAux);
                        }
                        ArrayList<String> alActions = UtilsXML.getChildValues(node.getChildNodes(), "action"); //$NON-NLS-1$
                        if (alActions != null && alActions.size() > 0) {
                            item.setActions(alActions);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "canBeFilled"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setCanBeFilled(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "group"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setGroup(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "blocky"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setBlocky(sAux);
                        }
                    } else {
                        sName = UtilsXML.getChildValue(node.getChildNodes(), "name"); //$NON-NLS-1$
                        item = new TerrainManagerItem(sIniHeader, sName);

                        item.setMineTurns(UtilsXML.getChildValue(node.getChildNodes(), "mineTurns")); //$NON-NLS-1$
                        item.setDrop(UtilsXML.getChildValue(node.getChildNodes(), "drop")); //$NON-NLS-1$
                        item.setDropPCT(UtilsXML.getChildValue(node.getChildNodes(), "dropPCT")); //$NON-NLS-1$
                        item.setLadderItem(UtilsXML.getChildValue(node.getChildNodes(), "ladderItem")); //$NON-NLS-1$
                        item.setActions(UtilsXML.getChildValues(node.getChildNodes(), "action")); //$NON-NLS-1$
                        item.setCanBeFilled(UtilsXML.getChildValue(node.getChildNodes(), "canBeFilled")); //$NON-NLS-1$
                        item.setGroup(UtilsXML.getChildValue(node.getChildNodes(), "group")); //$NON-NLS-1$
                        item.setBlocky(UtilsXML.getChildValue(node.getChildNodes(), "blocky")); //$NON-NLS-1$
                    }

                    /*
                     * New terrain definitions inherit the namespace of
                     * the content source that introduced them.
                     *
                     * Mods overriding an existing terrain retain the
                     * ownership of the original definition.
                     */
                    if (!bModChangingValues) {
                        DefinitionOwnershipRegistry.register(
                                DefinitionKind.TERRAIN,
                                sIniHeader,
                                namespace);
                    }

                    /*
                     * Keep the legacy TerrainManager keyed by the bare
                     * terrain ID.
                     */
                    terrainList.put(
                            sIniHeader,
                            item);
                }
            }
        } catch (Exception e) {
            Log.log(Log.LEVEL_ERROR, Messages.getString("TerrainManager.0") + e.toString() + "]", "TerrainManager"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            Game.exit();
        }
    }

    public static void clear() {
        terrainList = null;
        terrainGroups = null;
        terrainListByID = null;
        terrainTiles = null;
        blockTiles = null;
    }
}
