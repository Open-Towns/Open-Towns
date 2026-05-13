package xaos.tiles.entities.living;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import xaos.Towns;
import xaos.caravans.CaravanManager;
import xaos.caravans.CaravanManagerItem;
import xaos.data.DropData;
import xaos.data.HateData;
import xaos.dungeons.MonsterData;
import xaos.main.Game;
import xaos.tiles.entities.living.heroes.HeroManager;
import xaos.utils.Log;
import xaos.utils.Messages;
import xaos.utils.Utils;
import xaos.utils.UtilsIniHeaders;
import xaos.utils.UtilsXML;

public class LivingEntityManager {

    /**
     * Lista de livingEntitiesManagerItems
     */
    private static HashMap<String, LivingEntityManagerItem> hmLivingEntities;
    private static HashMap<String, HateData> hmLivingEntitiesHates;

    /**
     * Carga los livingEntities en la hash. Usa los .xml
     */
    private static void loadLivingEntities() {
        if (hmLivingEntities == null) {
            hmLivingEntities = new HashMap<String, LivingEntityManagerItem>();

            // Cargar de fichero
            loadXML(Towns.getPropertiesString("DATA_FOLDER") + "livingentities.xml", true); //$NON-NLS-1$ //$NON-NLS-2$

            // Mods
            File fUserFolder = new File(Game.getUserFolder());
            if (!fUserFolder.exists() || !fUserFolder.isDirectory()) {
                return;
            }

            ArrayList<String> alMods = Game.getModsLoaded();
            if (alMods != null && alMods.size() > 0) {
                for (int i = 0; i < alMods.size(); i++) {
                    String sModActionsPath = fUserFolder.getAbsolutePath() + System.getProperty("file.separator") + Game.MODS_FOLDER1 + System.getProperty("file.separator") + alMods.get(i) + System.getProperty("file.separator") + Towns.getPropertiesString("DATA_FOLDER") + "livingentities.xml"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                    File fIni = new File(sModActionsPath);
                    if (fIni.exists()) {
                        loadXML(sModActionsPath, false);
                    }
                }
            }

            // Cargamos los hates y comprobamos los steallivings
            hmLivingEntitiesHates = new HashMap<String, HateData>();
            Iterator<String> iterator = hmLivingEntities.keySet().iterator();
            HateData hateData;
            String sLiving;
            LivingEntityManagerItem lemi;
            while (iterator.hasNext()) {
                sLiving = iterator.next();
                lemi = hmLivingEntities.get(sLiving);

                // Hate
                hateData = new HateData(lemi.getHate());
                hmLivingEntitiesHates.put(sLiving, hateData);

                // Steal living
                if (lemi.getStealLivings() != null && lemi.getStealLivings().length > 0) {
                    for (int i = 0; i < lemi.getStealLivings().length; i++) {
                        if (hmLivingEntities.get(UtilsIniHeaders.getStringIniHeader(lemi.getStealLivings()[i])) == null) {
                            Log.log(Log.LEVEL_ERROR, "Wrong stealLivings" + " [" + UtilsIniHeaders.getStringIniHeader(lemi.getStealLivings()[i]) + "]", "LivingEntityManager");
                            Game.exit();
                        }
                    }
                }
            }
        }
    }

    public static HashMap<String, LivingEntityManagerItem> getAllItems() {
        if (hmLivingEntities == null) {
            loadLivingEntities();
        }

        return hmLivingEntities;
    }

    public static LivingEntityManagerItem getItem(String sIniHeader) {
        if (hmLivingEntities == null) {
            loadLivingEntities();
        }

        return hmLivingEntities.get(sIniHeader);
    }

    /**
     * Devuelve una caravana al azar. Tiene en cuenta los comePCT
     *
     * @return
     */
    public static LivingEntityManagerItem getCaravanAtRandom() {
        if (hmLivingEntities == null) {
            loadLivingEntities();
        }

        Iterator<LivingEntityManagerItem> iterator = hmLivingEntities.values().iterator();
        ArrayList<LivingEntityManagerItem> alCaravans = new ArrayList<LivingEntityManagerItem>();
        LivingEntityManagerItem lemi;
        CaravanManagerItem cmi;

        while (iterator.hasNext()) {
            lemi = iterator.next();
            if (lemi.getCaravan() != null) {
                cmi = CaravanManager.getItem(lemi.getCaravan());
                if (cmi != null && Utils.getRandomBetween(1, 100) <= cmi.getSpawnChancePercentage()) {
                    alCaravans.add(lemi);
                }
            }
        }

        if (alCaravans.size() > 0) {
            return alCaravans.get(Utils.getRandomBetween(0, alCaravans.size() - 1));
        } else {
            return null;
        }
    }

    public static HateData getHateData(String sIniHeader) {
        if (hmLivingEntities == null) {
            loadLivingEntities();
        }

        return hmLivingEntitiesHates.get(sIniHeader);
    }

    public static LivingEntityManagerItem getItem(MonsterData monsterData, int iType) {
        if (hmLivingEntities == null) {
            loadLivingEntities();
        }

        if (monsterData.getId().equalsIgnoreCase(MonsterData.ID_RANDOM)) {
            // Enemigo a random, miramos los niveles
            return getItemByLevel(monsterData.getLevelMin(), monsterData.getLevelMax(), iType, false);
        } else {
            // Enemigo fijo
            return hmLivingEntities.get(monsterData.getId());
        }
    }

    public static LivingEntityManagerItem getRandomItemByType(int iType) {
        if (hmLivingEntities == null) {
            loadLivingEntities();
        }

        // Recorremos todas las livings y vamos metiendo en una lista las que tengan el type pasado
        ArrayList<LivingEntityManagerItem> alLivings = new ArrayList<LivingEntityManagerItem>();
        Iterator<LivingEntityManagerItem> iterator = hmLivingEntities.values().iterator();
        LivingEntityManagerItem lemi;
        while (iterator.hasNext()) {
            lemi = iterator.next();
            if (lemi.getType() == iType) {
                alLivings.add(lemi);
            }
        }

        // Ahora seleccionamos una a random
        if (alLivings.size() > 0) {
            return alLivings.get(Utils.getRandomBetween(0, alLivings.size() - 1));
        } else {
            return null;
        }
    }

    /**
     * Devuelve un enemigo del nivel indicado. En caso de no existir va
     * disminuyendo de 1 en 1 hasta encontrar algo
     *
     */
    public static LivingEntityManagerItem getItemByLevel(int iLevelMax, int iType, boolean bSteal) {
        LivingEntityManagerItem lemi;

        while (iLevelMax > 0) {
            lemi = getItemByLevel(iLevelMax, iLevelMax, iType, bSteal);
            if (lemi != null) {
                return lemi;
            } else {
                iLevelMax--;
            }
        }

        // Si llega aquí es que no hay enemigos del nivel indicado ni inferior
        return null;
    }

    /**
     * Devuelve un enemigo a random a partir de 2 niveles
     *
     * @return un enemigo a random a partir de 2 niveles
     */
    private static LivingEntityManagerItem getItemByLevel(int iLevelMin, int iLevelMax, int iType, boolean bSteal) {
        if (hmLivingEntities == null) {
            loadLivingEntities();
        }

        if (iLevelMin > iLevelMax) {
            return null;
        }

        // Obtenemos todos los del nivel requerido
        ArrayList<LivingEntityManagerItem> alLemi = new ArrayList<LivingEntityManagerItem>();
        LivingEntityManagerItem lemi;
        Iterator<String> iterator = hmLivingEntities.keySet().iterator();
        while (iterator.hasNext()) {
            lemi = hmLivingEntities.get(iterator.next());
            if (lemi.getLevel() >= iLevelMin && lemi.getLevel() <= iLevelMax && lemi.getType() == iType) {
                if (bSteal && ((lemi.getSteal() != null && lemi.getSteal().length > 0) || (lemi.getStealLivings() != null && lemi.getStealLivings().length > 0))) {
                    alLemi.add(lemi);
                } else if (!bSteal && ((lemi.getSteal() == null || lemi.getSteal().length == 0) && (lemi.getStealLivings() == null || lemi.getStealLivings().length == 0))) {
                    alLemi.add(lemi);
                }
            }
        }

        if (alLemi.size() == 0) {
            return null;
        } else {
            return alLemi.get(Utils.getRandomBetween(0, (alLemi.size() - 1)));
        }
    }


    /**
     * Devuelve una lista de enemigos a partir de 2 niveles
     *
     * @return una lista de enemigos a partir de 2 niveles
     */
    public static ArrayList<LivingEntityManagerItem> getItemByLevelList(int iLevelMin, int iLevelMax, int iType, boolean bSteal) {
        if (hmLivingEntities == null) {
            loadLivingEntities();
        }

        if (iLevelMin > iLevelMax) {
            return null;
        }

        // Obtenemos todos los del nivel requerido
        ArrayList<LivingEntityManagerItem> alLemi = new ArrayList<LivingEntityManagerItem>();
        LivingEntityManagerItem lemi;
        Iterator<String> iterator = hmLivingEntities.keySet().iterator();
        while (iterator.hasNext()) {
            lemi = hmLivingEntities.get(iterator.next());
            if (lemi.getLevel() >= iLevelMin && lemi.getLevel() <= iLevelMax && lemi.getType() == iType) {
                if (bSteal && ((lemi.getSteal() != null && lemi.getSteal().length > 0) || (lemi.getStealLivings() != null && lemi.getStealLivings().length > 0))) {
                    alLemi.add(lemi);
                } else if (!bSteal && ((lemi.getSteal() == null || lemi.getSteal().length == 0) && (lemi.getStealLivings() == null || lemi.getStealLivings().length == 0))) {
                    alLemi.add(lemi);
                }
            }
        }

        if (alLemi.size() == 0) {
            return null;
        } else {
            return alLemi;
        }
    }


    /**
     * Devuelve una lista con todos los living que pueden spawnear en el
     * edificio pasado
     *
     * @param buildingIniHeader ID del edificio
     * @return
     */
    public static ArrayList<LivingEntityManagerItem> getItemsByBuilding(String buildingIniHeader) {
        if (hmLivingEntities == null) {
            loadLivingEntities();
        }

        ArrayList<LivingEntityManagerItem> alReturn = new ArrayList<LivingEntityManagerItem>();

        // Recorremos todos los items buscando el que tenga building = "parámetro pasado"
        Iterator<String> it = hmLivingEntities.keySet().iterator();
        LivingEntityManagerItem lemi;
        while (it.hasNext()) {
            lemi = hmLivingEntities.get(it.next());
            if (lemi.getBuilding() != null && lemi.getBuilding().equalsIgnoreCase(buildingIniHeader)) {
                alReturn.add(lemi);
            }
        }

        return alReturn;
    }

    private static void loadXML(String sXMLName, boolean bLoadingMain) {
        try {
            Document doc = UtilsXML.loadXMLFile(sXMLName);

			// Tenemos el documento XML parseado
            // Lo recorremos entero y vamos añadiendo los livingEntities a la hash
            NodeList nodeList = doc.getDocumentElement().getChildNodes();
            Node node;
            String sIniHeader;
            for (int i = 0; i < nodeList.getLength(); i++) {
                node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    sIniHeader = node.getNodeName();
                    if (sIniHeader != null && sIniHeader.equalsIgnoreCase("DELETE")) {
                        // Miramos que ID quiere borrar
                        if (node.getAttributes() != null && node.getAttributes().getNamedItem("id") != null) {
                            String sIDToDelete = node.getAttributes().getNamedItem("id").getNodeValue();
                            if (sIDToDelete != null) {
                                LivingEntityManagerItem lemi = hmLivingEntities.remove(sIDToDelete);
                                if (lemi != null && lemi.getAltGraphics() != null && lemi.getAltGraphics().size() > 0) {
                                    String altG;
                                    for (int a = 0; a < lemi.getAltGraphics().size(); a++) {
                                        altG = lemi.getAltGraphics().get(a);
                                        hmLivingEntities.remove(altG);
                                    }
                                }
                            }
                        }
                        continue;
                    }

                    boolean bExists = hmLivingEntities.containsKey(sIniHeader);

                    // Mod cambiando valores de un item que ya existe?
                    boolean bModChangingValues = (bExists && !bLoadingMain);

                    LivingEntityManagerItem lemi;
                    if (bModChangingValues) {
                        lemi = hmLivingEntities.get(sIniHeader);
                    } else {
                        lemi = new LivingEntityManagerItem();
                        lemi.setIniHeader(sIniHeader);
                    }

                    // Type
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "type"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setType(sAux);
                        }
                    } else {
                        lemi.setType(UtilsXML.getChildValue(node.getChildNodes(), "type")); //$NON-NLS-1$
                    }

                    // Level
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "level"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setLevel(sAux);
                        }
                    } else {
                        lemi.setLevel(UtilsXML.getChildValue(node.getChildNodes(), "level")); //$NON-NLS-1$
                    }

                    // Name
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "name"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setName(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "namePoolTag"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setNamePoolTag(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "surnamePoolTag"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setSurnamePoolTag(sAux);
                        }
                    } else {
                        lemi.setName(UtilsXML.getChildValue(node.getChildNodes(), "name")); //$NON-NLS-1$
                        lemi.setNamePoolTag(UtilsXML.getChildValue(node.getChildNodes(), "namePoolTag")); //$NON-NLS-1$
                        lemi.setSurnamePoolTag(UtilsXML.getChildValue(node.getChildNodes(), "surnamePoolTag")); //$NON-NLS-1$
                    }

                    // Gender
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "female"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setFemale(sAux);
                        }
                    } else {
                        lemi.setFemale(UtilsXML.getChildValue(node.getChildNodes(), "female")); //$NON-NLS-1$
                    }

                    // Attack
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "attack"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setAttack(sAux);
                        }
                    } else {
                        lemi.setAttack(UtilsXML.getChildValue(node.getChildNodes(), "attack")); //$NON-NLS-1$
                    }

                    // Attack Speed
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "attackSpeed"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setAttackSpeed(sAux);
                        }
                    } else {
                        lemi.setAttackSpeed(UtilsXML.getChildValue(node.getChildNodes(), "attackSpeed")); //$NON-NLS-1$
                    }

                    // Defense
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "defense"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setDefense(sAux);
                        }
                    } else {
                        lemi.setDefense(UtilsXML.getChildValue(node.getChildNodes(), "defense")); //$NON-NLS-1$
                    }

                    // HPs
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "healthPoints"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setHealthPoints(sAux);
                        }
                    } else {
                        lemi.setHealthPoints(UtilsXML.getChildValue(node.getChildNodes(), "healthPoints")); //$NON-NLS-1$
                    }

                    // Attack verb and infinitive
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "attackVerb"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setAttackVerb(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "attackVerbInfinitive"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setAttackVerbInfinitive(sAux);
                        }
                    } else {
                        lemi.setAttackVerb(UtilsXML.getChildValue(node.getChildNodes(), "attackVerb")); //$NON-NLS-1$
                        lemi.setAttackVerbInfinitive(UtilsXML.getChildValue(node.getChildNodes(), "attackVerbInfinitive")); //$NON-NLS-1$
                    }

                    // Damage
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "damage"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setDamage(sAux);
                        }
                    } else {
                        lemi.setDamage(UtilsXML.getChildValue(node.getChildNodes(), "damage")); //$NON-NLS-1$
                    }

                    // LOS
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "LOS"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setLOS(sAux);
                        }
                    } else {
                        lemi.setLOS(UtilsXML.getChildValue(node.getChildNodes(), "LOS")); //$NON-NLS-1$
                    }

                    // Walk speed
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "walkSpeed"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setWalkSpeed(sAux);
                        }
                    } else {
                        lemi.setWalkSpeed(UtilsXML.getChildValue(node.getChildNodes(), "walkSpeed")); //$NON-NLS-1$
                    }

                    // Grouping
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "grouping"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setGrouping(sAux);
                        }
                    } else {
                        lemi.setGrouping(UtilsXML.getChildValue(node.getChildNodes(), "grouping")); //$NON-NLS-1$
                    }

                    // HP Regeneration
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "HPRegeneration"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setHPRegeneration(sAux);
                        }
                    } else {
                        lemi.setHPRegeneration(UtilsXML.getChildValue(node.getChildNodes(), "HPRegeneration")); //$NON-NLS-1$
                    }

                    // Age
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "maxAge"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setMaxAge(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "maxAgeLiving"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setMaxAgeLiving(sAux);
                        }
                    } else {
                        lemi.setMaxAge(UtilsXML.getChildValue(node.getChildNodes(), "maxAge")); //$NON-NLS-1$
                        lemi.setMaxAgeLiving(UtilsXML.getChildValue(node.getChildNodes(), "maxAgeLiving")); //$NON-NLS-1$
                    }

                    // Move PCT
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "movePCT"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setMovePCT(sAux);
                        }
                    } else {
                        lemi.setMovePCT(UtilsXML.getChildValue(node.getChildNodes(), "movePCT")); //$NON-NLS-1$
                    }

                    // Equip
                    if (bModChangingValues) {
                        ArrayList<String> alAux = UtilsXML.getChildValues(node.getChildNodes(), "equip"); //$NON-NLS-1$
                        if (alAux != null && alAux.size() > 0) {
                            lemi.setEquip(alAux);
                        }
                    } else {
                        lemi.setEquip(UtilsXML.getChildValues(node.getChildNodes(), "equip")); //$NON-NLS-1$
                    }

                    // Equip allowed
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "equipAllowed"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setEquipAllowed(sAux);
                        }
                    } else {
                        lemi.setEquipAllowed(UtilsXML.getChildValue(node.getChildNodes(), "equipAllowed")); //$NON-NLS-1$
                    }

                    // Habitat
                    if (bModChangingValues) {
                        ArrayList<String> alAux = UtilsXML.getChildValues(node.getChildNodes(), "habitat"); //$NON-NLS-1$
                        if (alAux != null && alAux.size() > 0) {
                            lemi.setHabitatAsString(alAux);
                        }
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "habitatHeightMin"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setHabitatHeightMin(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "habitatHeightMax"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setHabitatHeightMax(sAux);
                        }
                    } else {
                        lemi.setHabitatAsString(UtilsXML.getChildValues(node.getChildNodes(), "habitat")); //$NON-NLS-1$
                        lemi.setHabitatHeightMin(UtilsXML.getChildValue(node.getChildNodes(), "habitatHeightMin")); //$NON-NLS-1$
                        lemi.setHabitatHeightMax(UtilsXML.getChildValue(node.getChildNodes(), "habitatHeightMax")); //$NON-NLS-1$
                    }

                    // Hate
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "hate"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setHate(sAux);
                        }
                    } else {
                        lemi.setHate(UtilsXML.getChildValue(node.getChildNodes(), "hate")); //$NON-NLS-1$
                    }

                    // Obtenemos el edificio spawnea
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "building"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setBuilding(sAux);
                        }
                    } else {
                        lemi.setBuilding(UtilsXML.getChildValue(node.getChildNodes(), "building")); //$NON-NLS-1$
                    }

                    // Obtenemos el tiempo que tarda en spawnear
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "buildingTime"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setBuildingTime(sAux);
                        }
                    } else {
                        lemi.setBuildingTime(UtilsXML.getChildValue(node.getChildNodes(), "buildingTime")); //$NON-NLS-1$
                    }

                    // Drops
                    if (bModChangingValues) {
                        ArrayList<DropData> alDrops = loadDropData(node.getChildNodes());
                        if (alDrops != null && alDrops.size() > 0) {
                            lemi.setDropData(alDrops);
                        }
                    } else {
                        lemi.setDropData(loadDropData(node.getChildNodes()));
                    }

                    // Alternative graphics
                    if (bModChangingValues) {
                        ArrayList<String> alAux = UtilsXML.getChildValues(node.getChildNodes(), "altGraphics"); //$NON-NLS-1$
                        if (alAux != null && alAux.size() > 0) {
                            lemi.setAltGraphics(alAux);
                        }
                    } else {
                        lemi.setAltGraphics(UtilsXML.getChildValues(node.getChildNodes(), "altGraphics")); //$NON-NLS-1$
                    }

                    // Custom actions
                    if (bModChangingValues) {
                        ArrayList<String> alAux = UtilsXML.getChildValues(node.getChildNodes(), "action"); //$NON-NLS-1$
                        if (alAux != null && alAux.size() > 0) {
                            lemi.setActions(alAux);
                        }
                    } else {
                        lemi.setActions(UtilsXML.getChildValues(node.getChildNodes(), "action")); //$NON-NLS-1$
                    }

                    // Directions
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "facingDirections"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setFacingDirections(sAux);
                        }
                    } else {
                        lemi.setFacingDirections(UtilsXML.getChildValue(node.getChildNodes(), "facingDirections")); //$NON-NLS-1$
                    }

                    // Hungry & sleep
                    if (lemi.getType() == LivingEntity.TYPE_CITIZEN || lemi.getType() == LivingEntity.TYPE_HERO) {
                        if (bModChangingValues) {
                            String sAux = UtilsXML.getChildValue(node.getChildNodes(), "maxHungryTurns"); //$NON-NLS-1$
                            if (sAux != null) {
                                lemi.setMaxHungryTurns(sAux);
                            }
                            sAux = UtilsXML.getChildValue(node.getChildNodes(), "maxSleepTurns"); //$NON-NLS-1$
                            if (sAux != null) {
                                lemi.setMaxSleepTurns(sAux);
                            }
                        } else {
                            lemi.setMaxHungryTurns(UtilsXML.getChildValue(node.getChildNodes(), "maxHungryTurns")); //$NON-NLS-1$
                            lemi.setMaxSleepTurns(UtilsXML.getChildValue(node.getChildNodes(), "maxSleepTurns")); //$NON-NLS-1$
                        }
                    }

                    // Heroes
                    if (lemi.getType() == LivingEntity.TYPE_HERO) {
                        if (bModChangingValues) {
                            String sAux = UtilsXML.getChildValue(node.getChildNodes(), "heroComePrerequisite"); //$NON-NLS-1$
                            if (sAux != null) {
                                if (HeroManager.getComePrerequisites(sAux) != null) {
                                    lemi.setHeroComePrerequisite(sAux);
                                } else {
                                    throw new Exception(Messages.getString("LivingEntityManager.0") + sAux + "]"); //$NON-NLS-1$ //$NON-NLS-2$
                                }
                            }
                            sAux = UtilsXML.getChildValue(node.getChildNodes(), "heroStayPrerequisite"); //$NON-NLS-1$
                            if (sAux != null) {
                                if (HeroManager.getStayPrerequisites(sAux) != null) {
                                    lemi.setHeroStayPrerequisite(sAux);
                                } else {
                                    throw new Exception(Messages.getString("LivingEntityManager.0") + sAux + "]"); //$NON-NLS-1$ //$NON-NLS-2$
                                }
                            }
                            sAux = UtilsXML.getChildValue(node.getChildNodes(), "heroBehaviour"); //$NON-NLS-1$
                            if (sAux != null) {
                                if (HeroManager.getBehaviour(sAux) != null) {
                                    lemi.setHeroBehaviour(sAux);
                                } else {
                                    throw new Exception(Messages.getString("LivingEntityManager.1") + sAux + "]"); //$NON-NLS-1$ //$NON-NLS-2$
                                }
                            }

                            sAux = UtilsXML.getChildValue(node.getChildNodes(), "heroSkills"); //$NON-NLS-1$
                            if (sAux != null) {
                                lemi.setHeroSkills(sAux);
                            }
                        } else {
                            String sHeroComePrerequisite = UtilsXML.getChildValue(node.getChildNodes(), "heroComePrerequisite"); //$NON-NLS-1$
                            if (sHeroComePrerequisite == null || HeroManager.getComePrerequisites(sHeroComePrerequisite) == null) {
                                throw new Exception(Messages.getString("LivingEntityManager.0") + sHeroComePrerequisite + "]"); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                            lemi.setHeroComePrerequisite(sHeroComePrerequisite);

                            String sHeroStayPrerequisite = UtilsXML.getChildValue(node.getChildNodes(), "heroStayPrerequisite"); //$NON-NLS-1$
                            if (sHeroStayPrerequisite == null || HeroManager.getStayPrerequisites(sHeroStayPrerequisite) == null) {
                                throw new Exception(Messages.getString("LivingEntityManager.0") + sHeroStayPrerequisite + "]"); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                            lemi.setHeroStayPrerequisite(sHeroStayPrerequisite);

                            String sHeroBehaviour = UtilsXML.getChildValue(node.getChildNodes(), "heroBehaviour"); //$NON-NLS-1$
                            if (sHeroBehaviour == null || HeroManager.getBehaviour(sHeroBehaviour) == null) {
                                throw new Exception(Messages.getString("LivingEntityManager.1") + sHeroBehaviour + "]"); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                            lemi.setHeroBehaviour(sHeroBehaviour);

                            lemi.setHeroSkills(UtilsXML.getChildValue(node.getChildNodes(), "heroSkills")); //$NON-NLS-1$
                        }
                    }

                    // Moral
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "moral"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setMoral(sAux);
                        }
                    } else {
                        lemi.setMoral(UtilsXML.getChildValue(node.getChildNodes(), "moral")); //$NON-NLS-1$
                    }

                    // Eat zone
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "eatZone"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setEatZone(sAux);
                        }
                    } else {
                        lemi.setEatZone(UtilsXML.getChildValue(node.getChildNodes(), "eatZone")); //$NON-NLS-1$
                    }

                    // Follow
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "followEntity"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setFollowEntity(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "followTurns"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setFollowTurns(sAux);
                        }
                    } else {
                        lemi.setFollowEntity(UtilsXML.getChildValue(node.getChildNodes(), "followEntity")); //$NON-NLS-1$
                        lemi.setFollowTurns(UtilsXML.getChildValue(node.getChildNodes(), "followTurns")); //$NON-NLS-1$
                    }

                    // Effects
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "effects"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setEffects(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "effectsImmune"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setEffectsImmune(sAux);
                        }
                    } else {
                        lemi.setEffects(UtilsXML.getChildValue(node.getChildNodes(), "effects")); //$NON-NLS-1$
                        lemi.setEffectsImmune(UtilsXML.getChildValue(node.getChildNodes(), "effectsImmune")); //$NON-NLS-1$
                    }

                    // Evade traps
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "evadeTraps"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setEvadeTraps(sAux);
                        }
                    } else {
                        lemi.setEvadeTraps(UtilsXML.getChildValue(node.getChildNodes(), "evadeTraps")); //$NON-NLS-1$
                    }

                    // Caravan
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "caravan"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setCaravan(sAux);
                        }
                    } else {
                        lemi.setCaravan(UtilsXML.getChildValue(node.getChildNodes(), "caravan")); //$NON-NLS-1$
                    }

                    // Steals
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "steal"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setSteal(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "stealLivings"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setStealLivings(sAux);
                        }
                    } else {
                        lemi.setSteal(UtilsXML.getChildValue(node.getChildNodes(), "steal")); //$NON-NLS-1$
                        lemi.setStealLivings(UtilsXML.getChildValue(node.getChildNodes(), "stealLivings")); //$NON-NLS-1$
                    }

                    // FX
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "fxDead"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setFxDead(sAux);
                        }
                    } else {
                        lemi.setFxDead(UtilsXML.getChildValue(node.getChildNodes(), "fxDead")); //$NON-NLS-1$
                    }

                    // Food needed
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "foodNeeded"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setFoodNeeded(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "foodNeededTurns"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setFoodNeededTurns(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "foodNeededDieTurns"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setFoodNeededDieTurns(sAux);
                        }
                    } else {
                        lemi.setFoodNeeded(UtilsXML.getChildValue(node.getChildNodes(), "foodNeeded")); //$NON-NLS-1$
                        lemi.setFoodNeededTurns(UtilsXML.getChildValue(node.getChildNodes(), "foodNeededTurns")); //$NON-NLS-1$
                        lemi.setFoodNeededDieTurns(UtilsXML.getChildValue(node.getChildNodes(), "foodNeededDieTurns")); //$NON-NLS-1$
                    }

                    // Animated when idle
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "animatedWhenIdle"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setAnimatedWhenIdle(sAux);
                        }
                    } else {
                        lemi.setAnimatedWhenIdle(UtilsXML.getChildValue(node.getChildNodes(), "animatedWhenIdle")); //$NON-NLS-1$
                    }

                    // Work/Idle PCTs
                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "workCounterPCT"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setWorkCounterPCT(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "idleCounterPCT"); //$NON-NLS-1$
                        if (sAux != null) {
                            lemi.setIdleCounterPCT(sAux);
                        }
                    } else {
                        lemi.setWorkCounterPCT(UtilsXML.getChildValue(node.getChildNodes(), "workCounterPCT")); //$NON-NLS-1$
                        lemi.setIdleCounterPCT(UtilsXML.getChildValue(node.getChildNodes(), "idleCounterPCT")); //$NON-NLS-1$
                    }

                    // Lo añadimos a la hash
                    hmLivingEntities.put(sIniHeader, lemi);

					//System.out.println (lemi.getIniHeader () + "," + lemi.getName () + "," + lemi.getAttack () + "," + lemi.getAttackSpeed () + "," + lemi.getDamage () + "," + lemi.getDefense () + "," + lemi.getHealthPoints () + "," + lemi.getLevel ());
                    // Miramos si tiene gráficos alternativos, en ese caso creamos un "lemi" igual con distinto ID
                    if (lemi.getAltGraphics() != null && lemi.getAltGraphics().size() > 0) {
                        String altG;
                        for (int a = 0; a < lemi.getAltGraphics().size(); a++) {
                            altG = lemi.getAltGraphics().get(a);
                            LivingEntityManagerItem lemiCopia = new LivingEntityManagerItem(lemi);
                            lemiCopia.setIniHeader(altG);
                            hmLivingEntities.put(altG, lemiCopia);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.log(Log.LEVEL_ERROR, Messages.getString("LivingEntityManager.11") + sXMLName + Messages.getString("LivingEntityManager.12") + e.toString() + "]", "LivingEntityManager"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            Game.exit();
        }
    }

    private static ArrayList<DropData> loadDropData(NodeList nodeList) throws Exception {
        ArrayList<DropData> alDrops = new ArrayList<DropData>();

        Node node;
        for (int i = 0; i < nodeList.getLength(); i++) {
            node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("drop")) { //$NON-NLS-1$
                DropData dropData = new DropData();
                dropData.setItem(UtilsXML.getChildValue(node.getChildNodes(), "item")); //$NON-NLS-1$
                dropData.setPCT(UtilsXML.getChildValue(node.getChildNodes(), "PCT")); //$NON-NLS-1$
                dropData.setLevelMin(UtilsXML.getChildValue(node.getChildNodes(), "levelMin")); //$NON-NLS-1$
                dropData.setLevelMax(UtilsXML.getChildValue(node.getChildNodes(), "levelMax")); //$NON-NLS-1$
                alDrops.add(dropData);
            }
        }

        return alDrops;
    }

    public static void clear() {
        hmLivingEntities = null;
        hmLivingEntitiesHates = null;
    }
}
