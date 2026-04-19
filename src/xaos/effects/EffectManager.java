package xaos.effects;

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

public class EffectManager {

    private static HashMap<String, EffectManagerItem> effectList;

    private static void loadItems() {
        effectList = new HashMap<String, EffectManagerItem>();

        // Cargar de fichero
        loadXMLEffects(Towns.getPropertiesString("DATA_FOLDER") + "effects.xml", true); //$NON-NLS-1$ //$NON-NLS-2$

        // Mods
        File fUserFolder = new File(Game.getUserFolder());
        if (!fUserFolder.exists() || !fUserFolder.isDirectory()) {
            return;
        }

        ArrayList<String> alMods = Game.getModsLoaded();
        if (alMods != null && alMods.size() > 0) {
            for (int i = 0; i < alMods.size(); i++) {
                String sModActionsPath = fUserFolder.getAbsolutePath() + System.getProperty("file.separator") + Game.MODS_FOLDER1 + System.getProperty("file.separator") + alMods.get(i) + System.getProperty("file.separator") + Towns.getPropertiesString("DATA_FOLDER") + "effects.xml"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                File fIni = new File(sModActionsPath);
                if (fIni.exists()) {
                    loadXMLEffects(sModActionsPath, false);
                }
            }
        }

        // Miramos que los afterEffects, onHitEffects, onRangedHitEffects, effectsImmune, effectsPrerequisite y castEffects existan
        Iterator<EffectManagerItem> itEffects = effectList.values().iterator();
        EffectManagerItem emi;
        while (itEffects.hasNext()) {
            emi = itEffects.next();

            if (emi.getAfterEffects() != null && emi.getAfterEffects().size() > 0) {
                for (int e = 0; e < emi.getAfterEffects().size(); e++) {
                    if (!effectList.containsKey(emi.getAfterEffects().get(e))) {
                        Log.log(Log.LEVEL_ERROR, Messages.getString("EffectManager.2") + emi.getAfterEffects().get(e) + "]", "EffectManager"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        Game.exit();
                    }
                }
            }

            if (emi.getOnHitEffects() != null && emi.getOnHitEffects().size() > 0) {
                for (int e = 0; e < emi.getOnHitEffects().size(); e++) {
                    if (!effectList.containsKey(emi.getOnHitEffects().get(e))) {
                        Log.log(Log.LEVEL_ERROR, Messages.getString("EffectManager.3") + emi.getOnHitEffects().get(e) + "]", "EffectManager"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        Game.exit();
                    }
                }
            }

            if (emi.getOnRangedHitEffects() != null && emi.getOnRangedHitEffects().size() > 0) {
                for (int e = 0; e < emi.getOnRangedHitEffects().size(); e++) {
                    if (!effectList.containsKey(emi.getOnRangedHitEffects().get(e))) {
                        Log.log(Log.LEVEL_ERROR, Messages.getString("EffectManager.4") + emi.getOnRangedHitEffects().get(e) + "]", "EffectManager"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        Game.exit();
                    }
                }
            }

            if (emi.getEffectsImmune() != null && emi.getEffectsImmune().size() > 0) {
                for (int e = 0; e < emi.getEffectsImmune().size(); e++) {
                    if (!effectList.containsKey(emi.getEffectsImmune().get(e))) {
                        Log.log(Log.LEVEL_ERROR, Messages.getString("EffectManager.1") + emi.getEffectsImmune().get(e) + "]", "EffectManager"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        Game.exit();
                    }
                }
            }

            if (emi.getEffectsPrerequisite() != null && emi.getEffectsPrerequisite().size() > 0) {
                for (int e = 0; e < emi.getEffectsPrerequisite().size(); e++) {
                    if (!effectList.containsKey(emi.getEffectsPrerequisite().get(e))) {
                        Log.log(Log.LEVEL_ERROR, Messages.getString("EffectManager.7") + " [" + emi.getEffectsPrerequisite().get(e) + "]", "EffectManager"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                        Game.exit();
                    }
                }
            }

            if (emi.getCastEffects() != null && emi.getCastEffects().size() > 0) {
                for (int e = 0; e < emi.getCastEffects().size(); e++) {
                    if (!effectList.containsKey(emi.getCastEffects().get(e))) {
                        Log.log(Log.LEVEL_ERROR, Messages.getString("EffectManager.5") + emi.getCastEffects().get(e) + "]", "EffectManager"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        Game.exit();
                    }
                }
            }
        }
    }

    public static EffectManagerItem getItem(String sIniHeader) {
        if (sIniHeader == null) {
            return null;
        }

        if (effectList == null) {
            loadItems();
        }

        return effectList.get(sIniHeader);
    }

    private static void loadXMLEffects(String sXMLName, boolean bLoadingMain) {

        try {
            Document doc = UtilsXML.loadXMLFile(sXMLName); //$NON-NLS-1$

			// Tenemos el documento XML parseado
            // Lo recorremos entero y vamos añadiendo las skills (SkillManagerItem) a la hash
            NodeList nodeList = doc.getDocumentElement().getChildNodes();
            Node node;
            EffectManagerItem effectData;
            String sIniHeader;
            for (int i = 0; i < nodeList.getLength(); i++) {
                node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    String sName = node.getNodeName();
                    if (sName != null && sName.equalsIgnoreCase("DELETE")) { //$NON-NLS-1$
                        // Miramos que ID quiere borrar
                        if (node.getAttributes() != null && node.getAttributes().getNamedItem("id") != null) { //$NON-NLS-1$
                            String sIDToDelete = node.getAttributes().getNamedItem("id").getNodeValue(); //$NON-NLS-1$
                            if (sIDToDelete != null) {
                                effectList.remove(sIDToDelete);
                            }
                        }
                        continue;
                    }

                    sIniHeader = UtilsXML.getChildValue(node.getChildNodes(), "id"); //$NON-NLS-1$
                    if (sIniHeader == null || sIniHeader.length() == 0) {
                        throw new Exception(Messages.getString("SkillManager.1")); //$NON-NLS-1$
                    }

                    boolean bExists = effectList.containsKey(sIniHeader);

                    // Mod cambiando valores de un item que ya existe?
                    boolean bModChangingValues = (bExists && !bLoadingMain);

                    if (bModChangingValues) {
                        effectData = effectList.get(sIniHeader);
                    } else {
                        effectData = new EffectManagerItem();
                        effectData.setId(sIniHeader);
                    }

                    if (bModChangingValues) {
                        String sAux = UtilsXML.getChildValue(node.getChildNodes(), "name"); //$NON-NLS-1$
                        if (sAux != null) {
                            effectData.setName(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "icon"); //$NON-NLS-1$
                        if (sAux != null) {
                            effectData.setIcon(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "damagePCT"); //$NON-NLS-1$
                        if (sAux != null) {
                            effectData.setDamagePCT(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "defensePCT"); //$NON-NLS-1$
                        if (sAux != null) {
                            effectData.setDefensePCT(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "attackPCT"); //$NON-NLS-1$
                        if (sAux != null) {
                            effectData.setAttackPCT(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "attackSpeedPCT"); //$NON-NLS-1$
                        if (sAux != null) {
                            effectData.setAttackSpeedPCT(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "DOT"); //$NON-NLS-1$
                        if (sAux != null) {
                            effectData.setDOT(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "onHitPCT"); //$NON-NLS-1$
                        if (sAux != null) {
                            effectData.setOnHitPCT(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "onHitEffects"); //$NON-NLS-1$
                        if (sAux != null) {
                            effectData.setOnHitEffects(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "onRangedHitPCT"); //$NON-NLS-1$
                        if (sAux != null) {
                            effectData.setOnRangedHitPCT(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "onRangedHitEffects"); //$NON-NLS-1$
                        if (sAux != null) {
                            effectData.setOnRangedHitEffects(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "healthPointsPCT"); //$NON-NLS-1$
                        if (sAux != null) {
                            effectData.setHealthPointsPCT(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "LOSPCT"); //$NON-NLS-1$
                        if (sAux != null) {
                            effectData.setLOSPCT(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "speedPCT"); //$NON-NLS-1$
                        if (sAux != null) {
                            effectData.setSpeedPCT(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "lasts"); //$NON-NLS-1$
                        if (sAux != null) {
                            effectData.setLasts(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "attackAllies"); //$NON-NLS-1$
                        if (sAux != null) {
                            effectData.setAttackAllies(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "removeTarget"); //$NON-NLS-1$
                        if (sAux != null) {
                            effectData.setRemoveTarget(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "flee"); //$NON-NLS-1$
                        if (sAux != null) {
                            effectData.setFlee(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "graphicChange"); //$NON-NLS-1$
                        if (sAux != null) {
                            effectData.setGraphicChange(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "raiseDead"); //$NON-NLS-1$
                        if (sAux != null) {
                            effectData.setRaiseDead(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "maxRaised"); //$NON-NLS-1$
                        if (sAux != null) {
                            effectData.setMaxRaised(sAux);
                        }

                        // Aftereffects list
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "afterEffects"); //$NON-NLS-1$
                        if (sAux != null) {
                            effectData.setAfterEffects(sAux);
                        }

                        // Casts
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "castEffects"); //$NON-NLS-1$
                        if (sAux != null) {
                            effectData.setCastEffects(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "castCooldown"); //$NON-NLS-1$
                        if (sAux != null) {
                            effectData.setCastCooldown(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "castTrigger"); //$NON-NLS-1$
                        if (sAux != null) {
                            effectData.setCastTrigger(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "castTargets"); //$NON-NLS-1$
                        if (sAux != null) {
                            effectData.setCastTargets(sAux);
                        }

                        // Immunity
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "effectsImmune"); //$NON-NLS-1$
                        if (sAux != null) {
                            effectData.setEffectsImmune(sAux);
                        }

                        // Prerequisites
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "effectsPrerequisite"); //$NON-NLS-1$
                        if (sAux != null) {
                            effectData.setEffectsPrerequisite(sAux);
                        }

                        // Messaging
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "messageWhenGain"); //$NON-NLS-1$
                        if (sAux != null) {
                            effectData.setMessageWhenGain(sAux);
                        }
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "messageWhenVanish"); //$NON-NLS-1$
                        if (sAux != null) {
                            effectData.setMessageWhenVanish(sAux);
                        }

                        // Happy
                        sAux = UtilsXML.getChildValue(node.getChildNodes(), "happy"); //$NON-NLS-1$
                        if (sAux != null) {
                            effectData.setHappy(sAux);
                        }
                    } else {
                        effectData.setName(UtilsXML.getChildValue(node.getChildNodes(), "name")); //$NON-NLS-1$
                        effectData.setIcon(UtilsXML.getChildValue(node.getChildNodes(), "icon")); //$NON-NLS-1$
                        effectData.setDamagePCT(UtilsXML.getChildValue(node.getChildNodes(), "damagePCT")); //$NON-NLS-1$
                        effectData.setDefensePCT(UtilsXML.getChildValue(node.getChildNodes(), "defensePCT")); //$NON-NLS-1$
                        effectData.setAttackPCT(UtilsXML.getChildValue(node.getChildNodes(), "attackPCT")); //$NON-NLS-1$
                        effectData.setAttackSpeedPCT(UtilsXML.getChildValue(node.getChildNodes(), "attackSpeedPCT")); //$NON-NLS-1$
                        effectData.setDOT(UtilsXML.getChildValue(node.getChildNodes(), "DOT")); //$NON-NLS-1$
                        effectData.setOnHitPCT(UtilsXML.getChildValue(node.getChildNodes(), "onHitPCT")); //$NON-NLS-1$
                        effectData.setOnHitEffects(UtilsXML.getChildValue(node.getChildNodes(), "onHitEffects")); //$NON-NLS-1$
                        effectData.setOnRangedHitPCT(UtilsXML.getChildValue(node.getChildNodes(), "onRangedHitPCT")); //$NON-NLS-1$
                        effectData.setOnRangedHitEffects(UtilsXML.getChildValue(node.getChildNodes(), "onRangedHitEffects")); //$NON-NLS-1$
                        effectData.setHealthPointsPCT(UtilsXML.getChildValue(node.getChildNodes(), "healthPointsPCT")); //$NON-NLS-1$
                        effectData.setLOSPCT(UtilsXML.getChildValue(node.getChildNodes(), "LOSPCT")); //$NON-NLS-1$
                        effectData.setSpeedPCT(UtilsXML.getChildValue(node.getChildNodes(), "speedPCT")); //$NON-NLS-1$
                        effectData.setLasts(UtilsXML.getChildValue(node.getChildNodes(), "lasts")); //$NON-NLS-1$
                        effectData.setAttackAllies(UtilsXML.getChildValue(node.getChildNodes(), "attackAllies")); //$NON-NLS-1$
                        effectData.setRemoveTarget(UtilsXML.getChildValue(node.getChildNodes(), "removeTarget")); //$NON-NLS-1$
                        effectData.setFlee(UtilsXML.getChildValue(node.getChildNodes(), "flee")); //$NON-NLS-1$
                        effectData.setGraphicChange(UtilsXML.getChildValue(node.getChildNodes(), "graphicChange")); //$NON-NLS-1$
                        effectData.setRaiseDead(UtilsXML.getChildValue(node.getChildNodes(), "raiseDead")); //$NON-NLS-1$
                        effectData.setMaxRaised(UtilsXML.getChildValue(node.getChildNodes(), "maxRaised")); //$NON-NLS-1$

                        // Aftereffects list
                        effectData.setAfterEffects(UtilsXML.getChildValue(node.getChildNodes(), "afterEffects")); //$NON-NLS-1$

                        // Casts
                        effectData.setCastEffects(UtilsXML.getChildValue(node.getChildNodes(), "castEffects")); //$NON-NLS-1$
                        effectData.setCastCooldown(UtilsXML.getChildValue(node.getChildNodes(), "castCooldown")); //$NON-NLS-1$
                        effectData.setCastTrigger(UtilsXML.getChildValue(node.getChildNodes(), "castTrigger")); //$NON-NLS-1$
                        effectData.setCastTargets(UtilsXML.getChildValue(node.getChildNodes(), "castTargets")); //$NON-NLS-1$

                        // Immunity
                        effectData.setEffectsImmune(UtilsXML.getChildValue(node.getChildNodes(), "effectsImmune")); //$NON-NLS-1$

                        // Prerequisite
                        effectData.setEffectsPrerequisite(UtilsXML.getChildValue(node.getChildNodes(), "effectsPrerequisite")); //$NON-NLS-1$

                        // Messaging
                        effectData.setMessageWhenGain(UtilsXML.getChildValue(node.getChildNodes(), "messageWhenGain")); //$NON-NLS-1$
                        effectData.setMessageWhenVanish(UtilsXML.getChildValue(node.getChildNodes(), "messageWhenVanish")); //$NON-NLS-1$

                        // Happy
                        effectData.setHappy(UtilsXML.getChildValue(node.getChildNodes(), "happy")); //$NON-NLS-1$
                    }

                    // Lo añadimos a la hash
                    effectList.put(sIniHeader, effectData);
                }
            }
        } catch (Exception e) {
            Log.log(Log.LEVEL_ERROR, Messages.getString("EffectManager.0") + e.toString() + "]", "EffectManager"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            Game.exit();
        }
    }

    public static void clear() {
        effectList = null;
    }
}
