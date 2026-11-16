package xaos.tiles.entities.buildings;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import xaos.Towns;
import xaos.main.Game;
import xaos.utils.Log;
import xaos.utils.Messages;
import xaos.utils.UtilsXML;

import xaos.api.definitions.DefinitionId;
import xaos.internal.api.definitions.DefinitionKind;
import xaos.internal.api.definitions.DefinitionOwnershipRegistry;
import xaos.internal.api.mods.ModNamespaceResolver;

public class BuildingManager {

    private static HashMap<String, BuildingManagerItem> itemList;

    public static void loadItems() {
        itemList = new HashMap<String, BuildingManagerItem>();

        // Cargar de fichero
        loadXMLBuildings(Towns.getPropertiesString("DATA_FOLDER") + "buildings.xml", true, //$NON-NLS-1$ //$NON-NLS-2$
                DefinitionId.DEFAULT_NAMESPACE);

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
                        + "buildings.xml"; //$NON-NLS-1$
                File fIni = new File(sModActionsPath);
                if (fIni.exists()) {
                    String namespace = ModNamespaceResolver.resolve(
                            alMods.get(i));

                    loadXMLBuildings(
                            sModActionsPath,
                            false,
                            namespace);
                }
            }
        }
    }

    public static Map<String, BuildingManagerItem> getAllItems() {
        if (itemList == null) {
            loadItems();
        }

        return Collections.unmodifiableMap(itemList);
    }

    public static BuildingManagerItem getItem(String sIniHeader) {
        if (itemList == null) {
            loadItems();
        }

        return itemList.get(sIniHeader);
    }

    private static void loadXMLBuildings(String sXMLName, boolean bLoadingMain, String namespace) {
        try {
            Document doc = UtilsXML.loadXMLFile(sXMLName); // $NON-NLS-1$

            // Tenemos el documento XML parseado
            // Lo recorremos entero y vamos añadiendo los buildings (BuildingManagerItem) a
            // la hash
            NodeList nodeList = doc.getDocumentElement().getChildNodes();
            Node node;
            BuildingManagerItem item;
            String sName, sIniHeader;
            short iWidth, iheight;
            for (int i = 0; i < nodeList.getLength(); i++) {
                node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    // Building
                    // Obtenemos el nameID, width i height
                    sIniHeader = node.getNodeName();

                    if (sIniHeader != null && sIniHeader.equalsIgnoreCase("DELETE")) {
                        // Miramos que ID quiere borrar
                        if (node.getAttributes() != null && node.getAttributes().getNamedItem("id") != null) {
                            String sIDToDelete = node.getAttributes().getNamedItem("id").getNodeValue();
                            if (sIDToDelete != null) {

                                if (!bLoadingMain
                                        && itemList.containsKey(sIDToDelete)
                                        && DefinitionOwnershipRegistry
                                                .conflictsWithOwner(
                                                        DefinitionKind.BUILDING,
                                                        sIDToDelete,
                                                        namespace)) {

                                    String existingNamespace = DefinitionOwnershipRegistry.namespaceFor(
                                            DefinitionKind.BUILDING,
                                            sIDToDelete);

                                    Log.log(
                                            Log.LEVEL_ERROR,
                                            "Definition delete collision for building ["
                                                    + sIDToDelete
                                                    + "]: mod namespace ["
                                                    + namespace
                                                    + "] attempted to delete a definition owned by ["
                                                    + existingNamespace
                                                    + "]. Delete ignored.",
                                            "BuildingManager");

                                    continue;
                                }

                                itemList.remove(
                                        sIDToDelete);

                                DefinitionOwnershipRegistry.unregister(
                                        DefinitionKind.BUILDING,
                                        sIDToDelete);
                            }
                        }
                        continue;
                    }

                    boolean bExists = itemList.containsKey(
                            sIniHeader);

                    /*
                     * Mods may modify base-game buildings, but must not
                     * accidentally overwrite a building owned by another mod.
                     */
                    if (bExists && !bLoadingMain) {

                        if (DefinitionOwnershipRegistry
                                .conflictsWithOwner(
                                        DefinitionKind.BUILDING,
                                        sIniHeader,
                                        namespace)) {

                            String existingNamespace = DefinitionOwnershipRegistry.namespaceFor(
                                    DefinitionKind.BUILDING,
                                    sIniHeader);

                            Log.log(
                                    Log.LEVEL_ERROR,
                                    "Definition collision for building ["
                                            + sIniHeader
                                            + "]: mod namespace ["
                                            + namespace
                                            + "] attempted to use an ID already owned by ["
                                            + existingNamespace
                                            + "]. Definition ignored.",
                                    "BuildingManager");

                            continue;
                        }
                    }

                    // Mod changing values of a building that already exists?
                    boolean bModChangingValues = (bExists && !bLoadingMain);

                    /*
                     * New definitions inherit the namespace of the
                     * content source that introduced them.
                     *
                     * Overrides keep their existing owner.
                     */
                    if (!bModChangingValues) {
                        DefinitionOwnershipRegistry.register(
                                DefinitionKind.BUILDING,
                                sIniHeader,
                                namespace);
                    }

                    if (bModChangingValues) {
                        item = itemList.get(sIniHeader);

                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "name"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setName(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "width"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setWidth(Short.parseShort(sAux));
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "height"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setHeight(Short.parseShort(sAux));
                        }
                    } else {
                        sName = UtilsXML.getChildValue(node.getChildNodes(), "name"); //$NON-NLS-1$
                        iWidth = Short.parseShort(UtilsXML.getChildValue(node.getChildNodes(), "width")); //$NON-NLS-1$
                        iheight = Short.parseShort(UtilsXML.getChildValue(node.getChildNodes(), "height")); //$NON-NLS-1$
                        item = new BuildingManagerItem(sIniHeader, sName, iWidth, iheight);
                    }

                    // Descriptions
                    if (bModChangingValues) {
                        ArrayList<String> alAux = UtilsXML.getChildValues(node.getChildNodes(), "description"); //$NON-NLS-1$
                        if (alAux != null && alAux.size() > 0) {
                            item.setDescriptions(alAux);
                        }
                    } else {
                        item.setDescriptions(UtilsXML.getChildValues(node.getChildNodes(), "description")); //$NON-NLS-1$
                    }

                    // Ground data
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "groundData"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setGroundData(sAux);
                        }
                    } else {
                        item.setGroundData(UtilsXML.getChildValue(node.getChildNodes(), "groundData")); //$NON-NLS-1$
                    }

                    // Type
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "type"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setType(sAux);
                        }
                    } else {
                        item.setType(UtilsXML.getChildValue(node.getChildNodes(), "type")); //$NON-NLS-1$
                    }

                    // canBeBuiltUnderground
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "canBeBuiltUnderground"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setCanBeBuiltUnderground(sAux);
                        }
                    } else {
                        item.setCanBeBuiltUnderground(
                                UtilsXML.getChildValue(node.getChildNodes(), "canBeBuiltUnderground")); //$NON-NLS-1$
                    }

                    // mustBeBuiltOver
                    if (bModChangingValues) {
                        ArrayList<String> alAux = UtilsXML.getChildValues(node.getChildNodes(), "mustBeBuiltOver"); //$NON-NLS-1$
                        if (alAux != null && alAux.size() > 0) {
                            item.setMustBeBuiltOver(alAux);
                        }
                    } else {
                        item.setMustBeBuiltOver(UtilsXML.getChildValues(node.getChildNodes(), "mustBeBuiltOver")); //$NON-NLS-1$
                    }

                    // mineTerrain
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "mineTerrain"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setMineTerrain(sAux);
                        }
                    } else {
                        item.setMineTerrain(UtilsXML.getChildValue(node.getChildNodes(), "mineTerrain")); //$NON-NLS-1$
                    }

                    // Obtenemos los prerequisitos
                    if (bModChangingValues) {
                        ArrayList<String> alAux = UtilsXML.getChildValues(node.getChildNodes(), "prerequisite"); //$NON-NLS-1$
                        if (alAux != null && alAux.size() > 0) {
                            item.setPrerequisites(alAux);
                        }
                        ArrayList<String> alAux2 = UtilsXML.getChildValues(node.getChildNodes(),
                                "prerequisiteFriendly"); //$NON-NLS-1$
                        if (alAux2 != null && alAux2.size() > 0) {
                            item.setPrerequisites(alAux2);
                        }
                    } else {
                        item.setPrerequisites(UtilsXML.getChildValues(node.getChildNodes(), "prerequisite")); //$NON-NLS-1$
                        item.setPrerequisitesFriendly(
                                UtilsXML.getChildValues(node.getChildNodes(), "prerequisiteFriendly")); //$NON-NLS-1$
                    }

                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "automatic"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setAutomatic(sAux);
                        }
                    } else {
                        item.setAutomatic(UtilsXML.getChildValue(node.getChildNodes(), "automatic")); //$NON-NLS-1$
                    }

                    // Lo añadimos a la hash
                    itemList.put(sIniHeader, item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.log(Log.LEVEL_ERROR, Messages.getString("BuildingManager.5") + sXMLName //$NON-NLS-1$
                    + Messages.getString("BuildingManager.6") + e.toString() + "]", "BuildingManager"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            Game.exit();
        }
    }

    public static void clear() {
        itemList = null;
    }
}
