package xaos.tiles.entities.items;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import xaos.property.PropertyFile;

import xaos.Towns;
import xaos.data.Type;
import xaos.data.Types;
import xaos.main.Game;
import xaos.tiles.Tile;
import xaos.tiles.terrain.TerrainManager;
import xaos.utils.Log;
import xaos.utils.Messages;
import xaos.utils.Utils;
import xaos.utils.UtilsXML;
import xaos.api.definitions.DefinitionId;
import xaos.internal.api.definitions.DefinitionKind;
import xaos.internal.api.definitions.DefinitionOwnershipRegistry;
import xaos.internal.api.mods.ModNamespaceResolver;

public class ItemManager {

    private static HashMap<String, ItemManagerItem> itemList;
    private static HashMap<String, Tile> miniItemList;

    public static void loadItems() {
        if (itemList == null) {
            itemList = new HashMap<String, ItemManagerItem>();

            // Cargar de fichero
            loadXMLItems(
                    Towns.getPropertiesString("DATA_FOLDER")
                            + "items.xml",
                    true,
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
                            + "items.xml"; //$NON-NLS-1$
                    File fIni = new File(sModActionsPath);
                    if (fIni.exists()) {
                        String namespace = ModNamespaceResolver.resolve(
                                alMods.get(i));

                        loadXMLItems(
                                sModActionsPath,
                                false,
                                namespace);
                    }
                }
            }

            // Comprobamos que todos los maxageItem existan
            // De paso rellenamos la lista de mini items (_block)
            // Y de paso comprobamos los habitatGroup y rellenamos los habitat que toquen
            // Y DE PASO los buryItem
            // Y DE PASO añadimos los types
            // Y DE PASO añadimos los maxAgeTerrain
            miniItemList = new HashMap<String, Tile>();
            Iterator<ItemManagerItem> itItems = itemList.values().iterator();
            ItemManagerItem imi;
            while (itItems.hasNext()) {
                imi = itItems.next();
                // maxAgeItem
                if (imi.getMaxAgeItem() != null && imi.getMaxAgeItem().length() > 0) {
                    if (itemList.get(imi.getMaxAgeItem()) == null) {
                        Log.log(Log.LEVEL_ERROR, Messages.getString("ItemManager.1") + imi.getMaxAgeItem() + "]", //$NON-NLS-1$ //$NON-NLS-2$
                                "ItemManager"); //$NON-NLS-1$
                        Game.exit();
                    }
                }

                // buryItem
                if (imi.getBuryItem() != null && imi.getBuryItem().size() > 0) {
                    for (int i = 0; i < imi.getBuryItem().size(); i++) {
                        if (itemList.get(imi.getBuryItem().get(i)) == null) {
                            Log.log(Log.LEVEL_ERROR,
                                    Messages.getString("ItemManager.3") + " [" + imi.getBuryItem().get(i) + "]", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    "ItemManager"); //$NON-NLS-1$
                            Game.exit();
                        }
                    }
                }

                // Miramos si existe
                String sMiniItem = imi.getIniHeader() + "_block"; //$NON-NLS-1$
                if (Towns.getPropertiesInt(PropertyFile.PROPERTY_FILE_GRAPHICS, "[" + sMiniItem + "]TILE_X", //$NON-NLS-1$ //$NON-NLS-2$
                        -666) != -666) {
                    Tile tile = new Tile(sMiniItem);
                    miniItemList.put(imi.getIniHeader(), tile);
                }

                // Habitats
                ArrayList<Integer> habitat = new ArrayList<Integer>();
                if (imi.getHabitatAsString() != null) {
                    for (int i = 0; i < imi.getHabitatAsString().size(); i++) {
                        if (!habitat.contains(imi.getHabitatAsString().get(i))) {
                            habitat.add(Integer
                                    .valueOf(TerrainManager.getItem(imi.getHabitatAsString().get(i)).getTerrainID()));
                            // getHabitat ().add (habitat.get (i));
                        }
                    }
                }
                imi.setHabitat(habitat);

                // Habitat group
                if (imi.getHabitatGroup() != null && imi.getHabitatGroup().length() > 0) {
                    imi.addHabitats(TerrainManager.getTerrainsByGroup(imi.getHabitatGroup()));
                }

                // TYPES
                String sMainType = Type.getMainType(imi.getType());
                if (sMainType != null) {
                    Types.addElement(sMainType, imi.getIniHeader(), imi.getName());
                }

                // maxAgeTerrain
                if (imi.getMaxAgeTerrain() != null && imi.getMaxAgeTerrain().length() > 0) {
                    if (TerrainManager.getItem(imi.getMaxAgeTerrain()) == null) {
                        Log.log(Log.LEVEL_ERROR,
                                Messages.getString("ItemManager.2") + " [" + imi.getMaxAgeTerrain() + "]", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                "ItemManager"); //$NON-NLS-1$
                        Game.exit();
                    }
                }

            }
        }
    }

    public static HashMap<String, ItemManagerItem> getAllItems() {
        if (itemList == null) {
            loadItems();
        }

        return itemList;
    }

    public static ItemManagerItem getItem(String iniHeader) {
        if (itemList == null) {
            loadItems();
        }

        return itemList.get(iniHeader);
    }

    public static Tile getMiniItem(String iniHeader) {
        if (miniItemList == null) {
            loadItems();
        }

        return miniItemList.get(iniHeader);
    }

    /**
     * Devuelve una lista con todos los items que se pueden construir en el
     * edificio pasado
     *
     * @param buildingIniHeader ID del edificio
     * @return
     */
    public static ArrayList<ItemManagerItem> getItemsByBuilding(String buildingIniHeader) {
        if (itemList == null) {
            loadItems();
        }

        ArrayList<ItemManagerItem> alReturn = new ArrayList<ItemManagerItem>();

        // Recorremos todos los items buscando el que tenga building = "parámetro
        // pasado"
        Iterator<String> it = itemList.keySet().iterator();
        ItemManagerItem imi;
        while (it.hasNext()) {
            imi = itemList.get(it.next());
            if (imi.getBuilding() != null && imi.getBuilding().equalsIgnoreCase(buildingIniHeader)) {
                alReturn.add(imi);
            }
        }

        return alReturn;
    }

    /**
     * Devuelve una lista con todos los items con el type pasado, nunca devuelve
     * null
     *
     * @return una lista con todos los items con el type pasado, nunca devuelve
     *         null
     */
    public static ArrayList<String> getItemsByType(String sType) {
        if (itemList == null) {
            loadItems();
        }

        ArrayList<String> alReturn = new ArrayList<String>();

        Iterator<String> it = itemList.keySet().iterator();
        ItemManagerItem imi;
        while (it.hasNext()) {
            imi = itemList.get(it.next());
            if (imi.getType() != null && imi.getType().equalsIgnoreCase(sType)) {
                alReturn.add(imi.getIniHeader());
            }
        }

        return alReturn;
    }

    /**
     * Devuelve el primer item con el type pasado o null
     *
     * @return el primer item con el type pasado o null
     */
    public static ItemManagerItem getFirstItemByType(String sType) {
        if (itemList == null) {
            loadItems();
        }

        Iterator<String> it = itemList.keySet().iterator();
        ItemManagerItem imi;
        while (it.hasNext()) {
            imi = itemList.get(it.next());
            if (imi.getType() != null && imi.getType().equalsIgnoreCase(sType)) {
                return imi;
            }
        }

        return null;
    }

    /**
     * Indica si hay items con el type pasado
     *
     * @return true si hay items con el type pasado
     */
    public static boolean existItemsByType(String sType) {
        if (itemList == null) {
            loadItems();
        }

        Iterator<String> it = itemList.keySet().iterator();
        ItemManagerItem imi;
        while (it.hasNext()) {
            imi = itemList.get(it.next());
            if (imi.getType() != null && imi.getType().equalsIgnoreCase(sType)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Devuelve un item a random a partir de un nivel mínimo y máximo o null si
     * no encuentra Items de level 0 no se devuelven
     *
     * @param level Nivel
     * @return un item a random a partir del nivel o null si no encuentra
     */
    public static ItemManagerItem getRandomItemByLevel(int levelMin, int levelMax) {
        if (itemList == null) {
            loadItems();
        }

        if (levelMax == 0 || levelMax < levelMin) {
            return null;
        }

        ArrayList<ItemManagerItem> alItems = new ArrayList<ItemManagerItem>();

        // Recorremos todos los items buscando los de nivel adecuado
        Iterator<String> it = itemList.keySet().iterator();
        ItemManagerItem imi;
        while (it.hasNext()) {
            imi = itemList.get(it.next());
            if (imi.getLevel() >= levelMin && imi.getLevel() <= levelMax) {
                alItems.add(imi);
            }
        }

        if (alItems.size() == 0) {
            return null;
        } else {
            return alItems.get(Utils.getRandomBetween(0, alItems.size() - 1));
        }
    }

    /**
     * Devuelve un item a random a partir de un type
     *
     * @param type Type
     * @return un item a random a partir de un type
     */
    public static ItemManagerItem getRandomItemByType(String type) {
        if (itemList == null) {
            loadItems();
        }

        if (type == null) {
            return null;
        }

        ArrayList<ItemManagerItem> alItems = new ArrayList<ItemManagerItem>();

        // Recorremos todos los items buscando los del type adecuado
        Iterator<String> it = itemList.keySet().iterator();
        ItemManagerItem imi;
        while (it.hasNext()) {
            imi = itemList.get(it.next());
            if (imi.getType() != null && imi.getType().equalsIgnoreCase(type)) {
                alItems.add(imi);
            }
        }

        if (alItems.size() == 0) {
            return null;
        } else {
            return alItems.get(Utils.getRandomBetween(0, alItems.size() - 1));
        }
    }

    private static void loadXMLItems(
            String sXMLName,
            boolean bLoadingMain,
            String namespace) {
        try {
            Document doc = UtilsXML.loadXMLFile(sXMLName);

            // Tenemos el documento XML parseado
            // Lo recorremos entero y vamos añadiendo los items (ItemManagerItem) a la hash
            NodeList nodeList = doc.getDocumentElement().getChildNodes();
            Node node;
            ItemManagerItem item;
            String sIniHeader, sType;
            for (int i = 0; i < nodeList.getLength(); i++) {
                node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    // Obtenemos el nameID, width i height
                    sIniHeader = node.getNodeName();

                    if (sIniHeader.equalsIgnoreCase("DELETE")) { //$NON-NLS-1$
                        // Miramos que ID quiere borrar
                        if (node.getAttributes() != null && node.getAttributes().getNamedItem("id") != null) { //$NON-NLS-1$
                            String sIDToDelete = node.getAttributes().getNamedItem("id").getNodeValue(); //$NON-NLS-1$
                            if (sIDToDelete != null) {

                                /*
                                 * Mods may delete base-game definitions or definitions
                                 * owned by themselves, but must not delete definitions
                                 * belonging to another mod.
                                 */
                                if (!bLoadingMain
                                        && itemList.containsKey(sIDToDelete)
                                        && DefinitionOwnershipRegistry
                                                .conflictsWithOwner(
                                                        DefinitionKind.ITEM,
                                                        sIDToDelete,
                                                        namespace)) {

                                    String existingNamespace = DefinitionOwnershipRegistry.namespaceFor(
                                            DefinitionKind.ITEM,
                                            sIDToDelete);

                                    Log.log(
                                            Log.LEVEL_ERROR,
                                            "Definition delete collision for item ["
                                                    + sIDToDelete
                                                    + "]: mod namespace ["
                                                    + namespace
                                                    + "] attempted to delete a definition owned by ["
                                                    + existingNamespace
                                                    + "]. Delete ignored.",
                                            "ItemManager");

                                    continue;
                                }

                                itemList.remove(
                                        sIDToDelete);

                                DefinitionOwnershipRegistry.unregister(
                                        DefinitionKind.ITEM,
                                        sIDToDelete);
                            }
                        }
                        continue;
                    }

                    boolean bExists = itemList.containsKey(
                            sIniHeader);

                    if (bLoadingMain && bExists) {
                        Log.log(
                                Log.LEVEL_ERROR,
                                Messages.getString("ItemManager.0")
                                        + sIniHeader
                                        + "]",
                                "ItemManager");
                    }

                    /*
                     * Mods are still allowed to override base-game definitions.
                     *
                     * However, if this ID was introduced by another mod, allowing
                     * this mod to modify it would create a namespace collision:
                     *
                     * mod-a:conveyor
                     * mod-b:conveyor
                     *
                     * both map internally to the legacy ID "conveyor".
                     */
                    if (bExists && !bLoadingMain) {

                        if (DefinitionOwnershipRegistry
                                .conflictsWithOwner(
                                        DefinitionKind.ITEM,
                                        sIniHeader,
                                        namespace)) {

                            String existingNamespace = DefinitionOwnershipRegistry.namespaceFor(
                                    DefinitionKind.ITEM,
                                    sIniHeader);

                            Log.log(
                                    Log.LEVEL_ERROR,
                                    "Definition collision for item ["
                                            + sIniHeader
                                            + "]: mod namespace ["
                                            + namespace
                                            + "] attempted to use an ID already owned by ["
                                            + existingNamespace
                                            + "]. Definition ignored.",
                                    "ItemManager");

                            continue;
                        }
                    }

                    // Mod changing values of an item that already exists?
                    boolean bModChangingValues = (bExists && !bLoadingMain);

                    // Tipo / subtipo
                    if (bModChangingValues) {
                        item = itemList.get(sIniHeader);
                        sType = UtilsXML.getChildValue(node.getChildNodes(), "type"); //$NON-NLS-1$
                        if (sType != null) {
                            item.setType(sType);
                        }
                    } else {
                        sType = UtilsXML.getChildValue(node.getChildNodes(), "type"); //$NON-NLS-1$
                        item = new ItemManagerItem(sIniHeader, sType);
                    }

                    // Level
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "level"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setLevel(sAux);
                        }
                    } else {
                        item.setLevel(UtilsXML.getChildValue(node.getChildNodes(), "level")); //$NON-NLS-1$
                    }

                    // Name / description
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "name"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setName(sAux);
                        }
                        ArrayList<String> alAux = UtilsXML.getChildValues(node.getChildNodes(), "description"); //$NON-NLS-1$
                        if (alAux != null && alAux.size() > 0) {
                            item.setDescriptions(alAux);
                        }
                    } else {
                        item.setName(UtilsXML.getChildValue(node.getChildNodes(), "name")); //$NON-NLS-1$
                        item.setDescriptions(UtilsXML.getChildValues(node.getChildNodes(), "description")); //$NON-NLS-1$
                    }

                    // Obtenemos el edificio donde se fabrica
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "building"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setBuilding(sAux);
                        }
                    } else {
                        item.setBuilding(UtilsXML.getChildValue(node.getChildNodes(), "building")); //$NON-NLS-1$
                    }

                    // Obtenemos los prerequisitos
                    if (bModChangingValues) {
                        ArrayList<String> alAux = UtilsXML.getChildValues(node.getChildNodes(), "prerequisite"); //$NON-NLS-1$
                        if (alAux != null && alAux.size() > 0) {
                            item.setPrerequisites(alAux);
                        }
                    } else {
                        item.setPrerequisites(UtilsXML.getChildValues(node.getChildNodes(), "prerequisite")); //$NON-NLS-1$
                    }

                    // Obtenemos el tiempo que tarda en fabricarse (en el caso de que no haya
                    // prerequisitos)
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "buildingTime"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setBuildingTime(sAux);
                        }
                    } else {
                        item.setBuildingTime(UtilsXML.getChildValue(node.getChildNodes(), "buildingTime")); //$NON-NLS-1$
                    }

                    // Floor walk speed
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "floorWalkSpeed"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setFloorWalkSpeed(sAux);
                        }
                    } else {
                        item.setFloorWalkSpeed(UtilsXML.getChildValue(node.getChildNodes(), "floorWalkSpeed")); //$NON-NLS-1$
                    }

                    // Habitat
                    if (bModChangingValues) {
                        ArrayList<String> alAux = UtilsXML.getChildValues(node.getChildNodes(), "habitat"); //$NON-NLS-1$
                        if (alAux != null && alAux.size() > 0) {
                            item.setHabitatAsString(alAux);
                        }
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "habitatGroup"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setHabitatGroup(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "habitatHeightMin"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setHabitatHeightMin(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "habitatHeightMax"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setHabitatHeightMax(sAux);
                        }
                    } else {
                        item.setHabitatAsString(UtilsXML.getChildValues(node.getChildNodes(), "habitat")); //$NON-NLS-1$
                        item.setHabitatGroup(UtilsXML.getChildValue(node.getChildNodes(), "habitatGroup")); //$NON-NLS-1$
                        item.setHabitatHeightMin(UtilsXML.getChildValue(node.getChildNodes(), "habitatHeightMin")); //$NON-NLS-1$
                        item.setHabitatHeightMax(UtilsXML.getChildValue(node.getChildNodes(), "habitatHeightMax")); //$NON-NLS-1$
                    }

                    // Age
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "maxAge"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setMaxAge(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "maxAgeItem"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setMaxAgeItem(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "maxAgeTerrain"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setMaxAgeTerrain(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "maxAgeNeedsWater"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setMaxAgeNeedsWater(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "maxAgeNeedsWaterRadius"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setMaxAgeNeedsWaterRadius(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "maxAgeNeedsItems"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setMaxAgeNeedsItems(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "maxAgeNeedsItemsRadius"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setMaxAgeNeedsItemsRadius(sAux);
                        }
                    } else {
                        item.setMaxAge(UtilsXML.getChildValue(node.getChildNodes(), "maxAge")); //$NON-NLS-1$
                        item.setMaxAgeItem(UtilsXML.getChildValue(node.getChildNodes(), "maxAgeItem")); //$NON-NLS-1$
                        item.setMaxAgeTerrain(UtilsXML.getChildValue(node.getChildNodes(), "maxAgeTerrain")); //$NON-NLS-1$
                        item.setMaxAgeNeedsWater(UtilsXML.getChildValue(node.getChildNodes(), "maxAgeNeedsWater")); //$NON-NLS-1$
                        item.setMaxAgeNeedsWaterRadius(
                                UtilsXML.getChildValue(node.getChildNodes(), "maxAgeNeedsWaterRadius")); //$NON-NLS-1$
                        item.setMaxAgeNeedsItems(UtilsXML.getChildValue(node.getChildNodes(), "maxAgeNeedsItems")); //$NON-NLS-1$
                        item.setMaxAgeNeedsItemsRadius(
                                UtilsXML.getChildValue(node.getChildNodes(), "maxAgeNeedsItemsRadius")); //$NON-NLS-1$
                    }

                    // Health points
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "healthPoints"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setHp(sAux);
                        }
                    } else {
                        item.setHp(UtilsXML.getChildValue(node.getChildNodes(), "healthPoints")); //$NON-NLS-1$
                    }

                    // Childs
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "spawn"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setSpawn(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "spawnMaxItems"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setSpawnMaxItems(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "spawnTurns"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setSpawnTurns(sAux);
                        }
                    } else {
                        item.setSpawn(UtilsXML.getChildValue(node.getChildNodes(), "spawn")); //$NON-NLS-1$
                        item.setSpawnMaxItems(UtilsXML.getChildValue(node.getChildNodes(), "spawnMaxItems")); //$NON-NLS-1$
                        item.setSpawnTurns(UtilsXML.getChildValue(node.getChildNodes(), "spawnTurns")); //$NON-NLS-1$
                    }

                    // Muro?
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "wall"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setWall(sAux);
                        }
                    } else {
                        item.setWall(UtilsXML.getChildValue(node.getChildNodes(), "wall")); //$NON-NLS-1$
                    }

                    // Conector de muros? (ej. puerta)
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "wallConnector"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setWallConnector(sAux);
                        }
                    } else {
                        item.setWallConnector(UtilsXML.getChildValue(node.getChildNodes(), "wallConnector")); //$NON-NLS-1$
                    }

                    // Puerta?
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "door"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setDoor(sAux);
                        }
                    } else {
                        item.setDoor(UtilsXML.getChildValue(node.getChildNodes(), "door")); //$NON-NLS-1$
                    }

                    // Modificadores de comida
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "canBeEaten"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setCanBeEaten(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "foodValue"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setFoodValue(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "foodFillPCT"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setFoodFillPCT(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "foodEatTime"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setFoodEatTime(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "foodEffects"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setFoodEffects(sAux);
                        }
                    } else {
                        item.setCanBeEaten(UtilsXML.getChildValue(node.getChildNodes(), "canBeEaten")); //$NON-NLS-1$
                        item.setFoodValue(UtilsXML.getChildValue(node.getChildNodes(), "foodValue")); //$NON-NLS-1$
                        item.setFoodFillPCT(UtilsXML.getChildValue(node.getChildNodes(), "foodFillPCT")); //$NON-NLS-1$
                        item.setFoodEatTime(UtilsXML.getChildValue(node.getChildNodes(), "foodEatTime")); //$NON-NLS-1$
                        item.setFoodEffects(UtilsXML.getChildValue(node.getChildNodes(), "foodEffects")); //$NON-NLS-1$
                    }

                    // Modificador de happiness
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "happiness"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setHappiness(sAux);
                        }
                    } else {
                        item.setHappiness(UtilsXML.getChildValue(node.getChildNodes(), "happiness")); //$NON-NLS-1$
                    }

                    // Se puede dormir ahí?
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "usedToSleep"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setCanBeUsedToSleep(sAux);
                        }
                    } else {
                        item.setCanBeUsedToSleep(UtilsXML.getChildValue(node.getChildNodes(), "usedToSleep")); //$NON-NLS-1$
                    }

                    // Se puede sentar ahí?
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "usedToSit"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setCanBeUsedToSit(sAux);
                        }
                    } else {
                        item.setCanBeUsedToSit(UtilsXML.getChildValue(node.getChildNodes(), "usedToSit")); //$NON-NLS-1$
                    }

                    // Unidores de zonas (puentes, escalera, ...)
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "zoneMergerUp"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setZoneMergerUp(sAux);
                        }
                    } else {
                        item.setZoneMergerUp(UtilsXML.getChildValue(node.getChildNodes(), "zoneMergerUp")); //$NON-NLS-1$
                    }

                    // canBeBuilt
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "canBeBuiltOnFloor"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setCanBeBuiltOnFloor(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "canBeBuiltOnHoles"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setCanBeBuiltOnHoles(sAux);
                        }
                    } else {
                        item.setCanBeBuiltOnFloor(UtilsXML.getChildValue(node.getChildNodes(), "canBeBuiltOnFloor")); //$NON-NLS-1$
                        item.setCanBeBuiltOnHoles(UtilsXML.getChildValue(node.getChildNodes(), "canBeBuiltOnHoles")); //$NON-NLS-1$
                    }

                    // canBeUnlocked
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "canBeUnlocked"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setCanBeUnlocked(sAux);
                        }
                    } else {
                        item.setCanBeUnlocked(UtilsXML.getChildValue(node.getChildNodes(), "canBeUnlocked")); //$NON-NLS-1$
                    }

                    // Glue
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "glue"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setGlue(sAux);
                        }
                    } else {
                        item.setGlue(UtilsXML.getChildValue(node.getChildNodes(), "glue")); //$NON-NLS-1$
                    }

                    // Base
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "base"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setBase(sAux);
                        }
                    } else {
                        item.setBase(UtilsXML.getChildValue(node.getChildNodes(), "base")); //$NON-NLS-1$
                    }

                    // Blocky
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "blocky"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setBlocky(sAux);
                        }
                    } else {
                        item.setBlocky(UtilsXML.getChildValue(node.getChildNodes(), "blocky")); //$NON-NLS-1$
                    }

                    // Text
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "text"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setText(sAux);
                        }
                    } else {
                        item.setText(UtilsXML.getChildValue(node.getChildNodes(), "text")); //$NON-NLS-1$
                    }

                    // Light radious and colors
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "lightRadius"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setLightRadius(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "lightRed"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setLightRed(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "lightGreen"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setLightGreen(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "lightBlue"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setLightBlue(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "translucent"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setTranslucent(sAux);
                        }
                    } else {
                        item.setLightRadius(UtilsXML.getChildValue(node.getChildNodes(), "lightRadius")); //$NON-NLS-1$
                        item.setLightRed(UtilsXML.getChildValue(node.getChildNodes(), "lightRed")); //$NON-NLS-1$
                        item.setLightGreen(UtilsXML.getChildValue(node.getChildNodes(), "lightGreen")); //$NON-NLS-1$
                        item.setLightBlue(UtilsXML.getChildValue(node.getChildNodes(), "lightBlue")); //$NON-NLS-1$
                        item.setTranslucent(UtilsXML.getChildValue(node.getChildNodes(), "translucent")); //$NON-NLS-1$
                    }

                    // Locked
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "locked"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setLocked(sAux);
                        }
                    } else {
                        item.setLocked(UtilsXML.getChildValue(node.getChildNodes(), "locked")); //$NON-NLS-1$
                    }

                    // Value
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "value"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setValue(sAux);
                        }
                    } else {
                        item.setValue(UtilsXML.getChildValue(node.getChildNodes(), "value")); //$NON-NLS-1$
                    }

                    // alwaysOperative
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "alwaysOperative"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setAlwaysOperative(sAux);
                        }
                    } else {
                        item.setAlwaysOperative(UtilsXML.getChildValue(node.getChildNodes(), "alwaysOperative")); //$NON-NLS-1$
                    }

                    // Container
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "container"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setContainer(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "containerSize"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setContainerSize(sAux);
                        }
                    } else {
                        item.setContainer(UtilsXML.getChildValue(node.getChildNodes(), "container")); //$NON-NLS-1$
                        item.setContainerSize(UtilsXML.getChildValue(node.getChildNodes(), "containerSize")); //$NON-NLS-1$
                    }

                    // Stackable
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "stackable"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setStackable(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "stackableSize"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setStackableSize(sAux);
                        }
                    } else {
                        item.setStackable(UtilsXML.getChildValue(node.getChildNodes(), "stackable")); //$NON-NLS-1$
                        item.setStackableSize(UtilsXML.getChildValue(node.getChildNodes(), "stackableSize")); //$NON-NLS-1$
                    }

                    // SpeedUP%
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "speedUpPCT"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setSpeedUpPCT(sAux);
                        }
                    } else {
                        item.setSpeedUpPCT(UtilsXML.getChildValue(node.getChildNodes(), "speedUpPCT")); //$NON-NLS-1$
                    }

                    // Modificadores militares
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "location"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setLocation(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "attack"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setAttackModifier(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "attackSpeed"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setAttackSpeedModifier(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "defense"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setDefenseModifier(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "health"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setHealthModifier(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "damage"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setDamageModifier(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "LOS"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setLOSModifier(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "movePCT"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setMovePCTModifier(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "walkSpeed"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setWalkSpeedModifier(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "ranged"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setRanged(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "rangedAmmo"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setRangedAmmo(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "rangedOneShoot"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setRangedOneShoot(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "verb"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setVerb(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "verbInfinitive"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setVerbInfinitive(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "wearEffects"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setWearEffects(sAux);
                        }
                    } else {
                        item.setLocation(UtilsXML.getChildValue(node.getChildNodes(), "location")); //$NON-NLS-1$
                        item.setAttackModifier(UtilsXML.getChildValue(node.getChildNodes(), "attack")); //$NON-NLS-1$
                        item.setAttackSpeedModifier(UtilsXML.getChildValue(node.getChildNodes(), "attackSpeed")); //$NON-NLS-1$
                        item.setDefenseModifier(UtilsXML.getChildValue(node.getChildNodes(), "defense")); //$NON-NLS-1$
                        item.setHealthModifier(UtilsXML.getChildValue(node.getChildNodes(), "health")); //$NON-NLS-1$
                        item.setDamageModifier(UtilsXML.getChildValue(node.getChildNodes(), "damage")); //$NON-NLS-1$
                        item.setLOSModifier(UtilsXML.getChildValue(node.getChildNodes(), "LOS")); //$NON-NLS-1$
                        item.setMovePCTModifier(UtilsXML.getChildValue(node.getChildNodes(), "movePCT")); //$NON-NLS-1$
                        item.setWalkSpeedModifier(UtilsXML.getChildValue(node.getChildNodes(), "walkSpeed")); //$NON-NLS-1$
                        item.setRanged(UtilsXML.getChildValue(node.getChildNodes(), "ranged")); //$NON-NLS-1$
                        item.setRangedAmmo(UtilsXML.getChildValue(node.getChildNodes(), "rangedAmmo")); //$NON-NLS-1$
                        item.setRangedOneShoot(UtilsXML.getChildValue(node.getChildNodes(), "rangedOneShoot")); //$NON-NLS-1$
                        item.setVerb(UtilsXML.getChildValue(node.getChildNodes(), "verb")); //$NON-NLS-1$
                        item.setVerbInfinitive(UtilsXML.getChildValue(node.getChildNodes(), "verbInfinitive")); //$NON-NLS-1$

                        item.setWearEffects(UtilsXML.getChildValue(node.getChildNodes(), "wearEffects")); //$NON-NLS-1$
                    }

                    // Tags
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "tags"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setTags(sAux);
                        }
                    } else {
                        item.setTags(UtilsXML.getChildValue(node.getChildNodes(), "tags")); //$NON-NLS-1$
                    }

                    // Custom actions
                    if (bModChangingValues) {
                        ArrayList<String> alAux = UtilsXML.getChildValues(node.getChildNodes(), "action"); //$NON-NLS-1$
                        if (alAux != null && alAux.size() > 0) {
                            item.setActions(alAux);
                        }
                    } else {
                        item.setActions(UtilsXML.getChildValues(node.getChildNodes(), "action")); //$NON-NLS-1$
                    }

                    // Zones
                    if (bModChangingValues) {
                        ArrayList<String> alAux = UtilsXML.getChildValues(node.getChildNodes(), "zone"); //$NON-NLS-1$
                        if (alAux != null && alAux.size() > 0) {
                            item.setZones(alAux);
                        }
                    } else {
                        item.setZones(UtilsXML.getChildValues(node.getChildNodes(), "zone")); //$NON-NLS-1$
                    }

                    // Traps
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "trap"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setTrap(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "trapEffects"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setTrapEffects(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "trapCooldown"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setTrapCooldown(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "trapOnIcon"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setTrapOnIcon(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "trapTargets"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setTrapTargets(sAux);
                        }
                    } else {
                        item.setTrap(UtilsXML.getChildValue(node.getChildNodes(), "trap")); //$NON-NLS-1$
                        item.setTrapEffects(UtilsXML.getChildValue(node.getChildNodes(), "trapEffects")); //$NON-NLS-1$
                        item.setTrapCooldown(UtilsXML.getChildValue(node.getChildNodes(), "trapCooldown")); //$NON-NLS-1$
                        item.setTrapOnIcon(UtilsXML.getChildValue(node.getChildNodes(), "trapOnIcon")); //$NON-NLS-1$
                        item.setTrapTargets(UtilsXML.getChildValue(node.getChildNodes(), "trapTargets")); //$NON-NLS-1$
                    }

                    // Block fluids
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "blockFluids"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setBlockFluids(sAux);
                        }
                    } else {
                        item.setBlockFluids(UtilsXML.getChildValue(node.getChildNodes(), "blockFluids")); //$NON-NLS-1$
                    }

                    // Fluids elevator
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "fluidsElevator"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setFluidsElevator(sAux);
                        }
                    } else {
                        item.setFluidsElevator(UtilsXML.getChildValue(node.getChildNodes(), "fluidsElevator")); //$NON-NLS-1$
                    }

                    // Allow fluids
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "allowFluids"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setAllowFluids(sAux);
                        }
                    } else {
                        item.setAllowFluids(UtilsXML.getChildValue(node.getChildNodes(), "allowFluids")); //$NON-NLS-1$
                    }

                    // Bury
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "bury"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setBury(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "buryLocked"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setBuryLocked(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "buryItem"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setBuryItem(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "buryItemPCT"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setBuryItemPCT(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "buryDestroyItem"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setBuryDestroyItem(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "buryLivings"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setBuryLivings(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "buryLivingsPCT"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setBuryLivingsPCT(sAux);
                        }
                    } else {
                        item.setBury(UtilsXML.getChildValue(node.getChildNodes(), "bury")); //$NON-NLS-1$
                        item.setBuryLocked(UtilsXML.getChildValue(node.getChildNodes(), "buryLocked")); //$NON-NLS-1$
                        item.setBuryItem(UtilsXML.getChildValue(node.getChildNodes(), "buryItem")); //$NON-NLS-1$
                        item.setBuryItemPCT(UtilsXML.getChildValue(node.getChildNodes(), "buryItemPCT")); //$NON-NLS-1$
                        item.setBuryDestroyItem(UtilsXML.getChildValue(node.getChildNodes(), "buryDestroyItem")); //$NON-NLS-1$
                        item.setBuryLivings(UtilsXML.getChildValue(node.getChildNodes(), "buryLivings")); //$NON-NLS-1$
                        item.setBuryLivingsPCT(UtilsXML.getChildValue(node.getChildNodes(), "buryLivingsPCT")); //$NON-NLS-1$
                    }

                    // Rotate
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "canBeRotated"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setCanBeRotated(sAux);
                        }
                    } else {
                        item.setCanBeRotated(UtilsXML.getChildValue(node.getChildNodes(), "canBeRotated")); //$NON-NLS-1$
                    }

                    // Build action
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "buildAction"); //$NON-NLS-1$
                        if (sAux != null) {
                            item.setBuildAction(sAux);
                        }
                    } else {
                        item.setBuildAction(UtilsXML.getChildValue(node.getChildNodes(), "buildAction")); //$NON-NLS-1$
                    }

                    /*
                     * New definitions inherit the namespace of the
                     * content source that introduced them.
                     *
                     * Overrides keep their existing owner.
                     */
                    if (!bModChangingValues) {
                        DefinitionOwnershipRegistry.register(
                                DefinitionKind.ITEM,
                                sIniHeader,
                                namespace);
                    }

                    /*
                     * Keep the legacy Towns registry authoritative.
                     *
                     * Namespace ownership is metadata layered on top;
                     * the engine continues storing definitions using
                     * their original bare IDs.
                     */
                    itemList.put(
                            sIniHeader,
                            item);
                }
            }
        } catch (Exception e) {
            Log.log(Log.LEVEL_ERROR, Messages.getString("ItemManager.6") + sXMLName //$NON-NLS-1$
                    + Messages.getString("ItemManager.7") + e.toString() + "]", "ItemManager"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            Game.exit();
        }
    }

    public static void clear() {
        itemList = null;
        miniItemList = null;
    }
}
