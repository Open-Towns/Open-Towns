package xaos.actions;

import java.awt.Color;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import xaos.Towns;
import xaos.data.CitizenGroupData;
import xaos.data.CitizenGroups;
import xaos.main.Game;
import xaos.main.World;
import xaos.panels.CommandPanel;
import xaos.panels.menus.SmartMenu;
import xaos.tiles.entities.living.Citizen;
import xaos.tiles.entities.living.LivingEntity;
import xaos.tiles.entities.living.LivingEntityManager;
import xaos.tiles.entities.living.LivingEntityManagerItem;
import xaos.utils.Log;
import xaos.utils.Messages;
import xaos.utils.UtilsIniHeaders;
import xaos.utils.UtilsXML;

public class ActionPriorityManager {

    public static String PRIORITY_MINE_DIG = "_MINEDIG_"; //$NON-NLS-1$
    public static String PRIORITY_HAUL = "_HAUL_"; //$NON-NLS-1$
    public static String PRIORITY_TRADING = "_TRADING_"; //$NON-NLS-1$
    public static String PRIORITY_BUILD_BUILDINGS = "_BUILDINGS_"; //$NON-NLS-1$
    public static String PRIORITY_FEED_ANIMALS = "_FEED_"; //$NON-NLS-1$

    private static HashMap<String, ActionPriorityManagerItem> itemList;

    private static ArrayList<String> prioritiesList; // Lista ordenada con las prioridades
    private static HashMap<String, Integer> prioritiesValues; // Prioridad numérica de cada "item" (se corresponde con la posición en la lista, se usa para performance)

    public static void loadItems() {
        // Cargar de fichero
        prioritiesList = new ArrayList<String>();
        prioritiesValues = new HashMap<String, Integer>();
        itemList = new HashMap<String, ActionPriorityManagerItem>();

        // Cargar de fichero
        loadXMLPriorityActions(Towns.getPropertiesString("DATA_FOLDER") + "priorities.xml");

        // Mods
        File fUserFolder = new File(Game.getUserFolder());
        if (!fUserFolder.exists() || !fUserFolder.isDirectory()) {
            return;
        }

        ArrayList<String> alMods = Game.getModsLoaded();
        if (alMods != null && alMods.size() > 0) {
            for (int i = 0; i < alMods.size(); i++) {
                String sModActionsPath = fUserFolder.getAbsolutePath() + System.getProperty("file.separator") + Game.MODS_FOLDER1 + System.getProperty("file.separator") + alMods.get(i) + System.getProperty("file.separator") + Towns.getPropertiesString("DATA_FOLDER") + "priorities.xml";
                File fIni = new File(sModActionsPath);
                if (fIni.exists()) {
                    loadXMLPriorityActions(sModActionsPath);
                }
            }
        }
    }

    public static ActionPriorityManagerItem getItem(String sID) {
        if (itemList == null) {
            loadItems();
        }

        return itemList.get(sID);
    }

    private static void loadXMLPriorityActions(String sXMLName) {
        try {
            Document doc = UtilsXML.loadXMLFile(sXMLName); //$NON-NLS-1$

			// Tenemos el documento XML parseado
            // Lo recorremos entero y vamos añadiendo a la hash
            NodeList nodeList = doc.getDocumentElement().getChildNodes();
            Node node;
            ActionPriorityManagerItem item;
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
                                itemList.remove(sIDToDelete);
                                prioritiesValues.remove(sIDToDelete);
                                prioritiesList.remove(sIDToDelete);
                            }
                        }
                        continue;
                    }

                    sID = UtilsXML.getChildValue(node.getChildNodes(), "id"); //$NON-NLS-1$
                    item = new ActionPriorityManagerItem(sID);

                    item.setName(UtilsXML.getChildValue(node.getChildNodes(), "name")); //$NON-NLS-1$
                    item.setIcon(UtilsXML.getChildValue(node.getChildNodes(), "icon")); //$NON-NLS-1$

                    // Priority
                    if (sID != null && !prioritiesList.contains(sID)) {
                        prioritiesValues.put(sID, Integer.valueOf(prioritiesList.size()));
                        prioritiesList.add(sID);
                    }

                    // Lo añadimos a la hash
                    itemList.put(sID, item);
                }
            }
        } catch (Exception e) {
            Log.log(Log.LEVEL_ERROR, Messages.getString("ActionManager.0") + sXMLName + "] [" + e.toString() + "]", "ActionPriorityManager"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            Game.exit();
        }
    }

    public static ArrayList<String> getPrioritiesList() {
        if (itemList == null) {
            loadItems();
        }

        return prioritiesList;
    }

    public static int getPrioritiesListSize() {
        if (itemList == null) {
            loadItems();
        }
        return prioritiesList.size();
    }

    public static int getPriorityValueByPriorityID(String sID) {
        if (itemList == null) {
            loadItems();
        }

        Integer iValue = prioritiesValues.get(sID);
        if (iValue != null) {
            return iValue.intValue();
        }

        return prioritiesList.size();
    }

    public static int getPriorityValueByActionID(String sID) {
        if (itemList == null) {
            loadItems();
        }

        ActionManagerItem ami = ActionManager.getItem(sID);
        if (ami.getPriorityID() != null) {
            Integer iValue = prioritiesValues.get(ami.getPriorityID());
            if (iValue != null) {
                return iValue.intValue();
            }
        }

        return prioritiesList.size();
    }

    public static void setPriorityValue(String sID, int iValue) {
        if (itemList == null) {
            loadItems();
        }

        prioritiesValues.put(sID, iValue);
    }

    public static void swapPriorities(int source, int dest) {
        try {
            String sSRC = ActionPriorityManager.getPrioritiesList().get(source);
            String sDST = ActionPriorityManager.getPrioritiesList().get(dest);

            ActionPriorityManager.getPrioritiesList().set(source, sDST);
            ActionPriorityManager.getPrioritiesList().set(dest, sSRC);
            setPriorityValue(sSRC, dest);
            setPriorityValue(sDST, source);
        } catch (Exception e) {
        }
    }

    public static SmartMenu createProfessionsMenu(int iCitizenID) {
        if (iCitizenID == -1) {
            return null;
        }
        LivingEntity le = World.getLivingEntityByID(iCitizenID);
        if (le == null) {
            return null;
        }
        LivingEntityManagerItem lemi = LivingEntityManager.getItem(le.getIniHeader());
        if (lemi == null || lemi.getType() != LivingEntity.TYPE_CITIZEN) {
            return null;
        }

        ArrayList<String> alPriorities = getPrioritiesList();
        if (alPriorities == null || alPriorities.size() == 0) {
            return null;
        }

        Citizen cit = (Citizen) le;

        SmartMenu smProfessionsMenu = new SmartMenu(SmartMenu.TYPE_MENU, "Professions", null, null, null); //$NON-NLS-1$

        // Añadimos el enable todo/disable todo
        SmartMenu smAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Container.3"), null, CommandPanel.COMMAND_PROFESSIONS_ENABLE_ALL, Integer.toString(iCitizenID), null, null, Color.GREEN); //$NON-NLS-1$
        smAux.setIcon("iconenableall"); //$NON-NLS-1$
        smProfessionsMenu.addItem(smAux);
        smAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Container.4"), null, CommandPanel.COMMAND_PROFESSIONS_DISABLE_ALL, Integer.toString(iCitizenID), null, null, Color.ORANGE); //$NON-NLS-1$
        smAux.setIcon("icondisableall"); //$NON-NLS-1$
        smProfessionsMenu.addItem(smAux);

        // Añadimos 1 linea por cada priority (para poner/quitar)
        for (int i = 0; i < alPriorities.size(); i++) {
            ActionPriorityManagerItem apmi = getItem(alPriorities.get(i));
            if (apmi != null) {
                if (cit.getCitizenData().containsDeniedJob(alPriorities.get(i))) {
                    smAux = new SmartMenu(SmartMenu.TYPE_ITEM, apmi.getName(), null, CommandPanel.COMMAND_PROFESSIONS_ENABLE_ITEM, Integer.toString(iCitizenID), alPriorities.get(i), null, Color.GREEN);
                } else {
                    smAux = new SmartMenu(SmartMenu.TYPE_ITEM, apmi.getName(), null, CommandPanel.COMMAND_PROFESSIONS_DISABLE_ITEM, Integer.toString(iCitizenID), alPriorities.get(i), null, Color.ORANGE);
                }
                smAux.setIcon(apmi.getIcon().getIniHeader());
                smProfessionsMenu.addItem(smAux);
            }
        }

        return smProfessionsMenu;
    }

    public static SmartMenu createJobGroupPanelMenu(int iGroupID) {
        if (iGroupID < 0 || iGroupID >= CitizenGroups.MAX_GROUPS) {
            return null;
        }

        CitizenGroupData cgd = Game.getWorld().getCitizenGroups().getGroup(iGroupID);
        if (cgd == null) {
            return null;
        }

        ArrayList<String> alPriorities = getPrioritiesList();
        if (alPriorities == null || alPriorities.size() == 0) {
            return null;
        }

        SmartMenu smJobGroupMenu = new SmartMenu(SmartMenu.TYPE_MENU, "Job group", null, null, null); //$NON-NLS-1$

        // Añadimos el enable todo/disable todo
        SmartMenu smAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Container.3"), null, CommandPanel.COMMAND_JOB_GROUP_ENABLE_ALL, Integer.toString(iGroupID), null, null, Color.GREEN); //$NON-NLS-1$
        smAux.setIcon("iconenableall"); //$NON-NLS-1$
        smJobGroupMenu.addItem(smAux);
        smAux = new SmartMenu(SmartMenu.TYPE_ITEM, Messages.getString("Container.4"), null, CommandPanel.COMMAND_JOB_GROUP_DISABLE_ALL, Integer.toString(iGroupID), null, null, Color.ORANGE); //$NON-NLS-1$
        smAux.setIcon("icondisableall"); //$NON-NLS-1$
        smJobGroupMenu.addItem(smAux);

        // Añadimos 1 linea por cada priority (para poner/quitar)
        for (int i = 0; i < alPriorities.size(); i++) {
            ActionPriorityManagerItem apmi = getItem(alPriorities.get(i));
            if (apmi != null) {
                if (cgd.containsDeniedJob(UtilsIniHeaders.getIntIniHeader(alPriorities.get(i)))) {
                    smAux = new SmartMenu(SmartMenu.TYPE_ITEM, apmi.getName(), null, CommandPanel.COMMAND_JOB_GROUP_ENABLE_ITEM, Integer.toString(iGroupID), alPriorities.get(i), null, Color.GREEN);
                } else {
                    smAux = new SmartMenu(SmartMenu.TYPE_ITEM, apmi.getName(), null, CommandPanel.COMMAND_JOB_GROUP_DISABLE_ITEM, Integer.toString(iGroupID), alPriorities.get(i), null, Color.ORANGE);
                }
                smAux.setIcon(apmi.getIcon().getIniHeader());
                smJobGroupMenu.addItem(smAux);
            }
        }

        return smJobGroupMenu;
    }

    public static boolean regenerateProfessionsPanelMenu(SmartMenu menuProfessions, int iCitizenID) {
        if (menuProfessions == null || menuProfessions.getItems() == null) {
            return false;
        }

        if (iCitizenID == -1) {
            return false;
        }
        LivingEntity le = World.getLivingEntityByID(iCitizenID);
        if (le == null) {
            return false;
        }
        LivingEntityManagerItem lemi = LivingEntityManager.getItem(le.getIniHeader());
        if (lemi == null || lemi.getType() != LivingEntity.TYPE_CITIZEN) {
            return false;
        }

        Citizen cit = (Citizen) le;

        ArrayList<String> alPriorities = getPrioritiesList();
        if (alPriorities == null || alPriorities.size() == 0) {
            return false;
        }

        for (int i = 0; i < menuProfessions.getItems().size(); i++) {
            if (menuProfessions.getItems().get(i).getCommand() != null) {
                if (menuProfessions.getItems().get(i).getCommand().equals(CommandPanel.COMMAND_PROFESSIONS_ENABLE_ITEM)) {
                    if (!cit.getCitizenData().containsDeniedJob(menuProfessions.getItems().get(i).getParameter2())) {
                        menuProfessions.getItems().get(i).setCommand(CommandPanel.COMMAND_PROFESSIONS_DISABLE_ITEM);
                    }
                } else if (menuProfessions.getItems().get(i).getCommand().equals(CommandPanel.COMMAND_PROFESSIONS_DISABLE_ITEM)) {
                    if (cit.getCitizenData().containsDeniedJob(menuProfessions.getItems().get(i).getParameter2())) {
                        menuProfessions.getItems().get(i).setCommand(CommandPanel.COMMAND_PROFESSIONS_ENABLE_ITEM);
                    }
                }
            }
        }

        return true;
    }

    public static boolean regenerateJobGroupPanelMenu(SmartMenu menuProfessions, int iGroupID) {
        if (menuProfessions == null || menuProfessions.getItems() == null) {
            return false;
        }

        if (iGroupID < 0 || iGroupID >= CitizenGroups.MAX_GROUPS) {
            return false;
        }

        CitizenGroupData cgd = Game.getWorld().getCitizenGroups().getGroup(iGroupID);
        if (cgd == null) {
            return false;
        }

        for (int i = 0; i < menuProfessions.getItems().size(); i++) {
            if (menuProfessions.getItems().get(i).getCommand() != null) {
                if (menuProfessions.getItems().get(i).getCommand().equals(CommandPanel.COMMAND_JOB_GROUP_ENABLE_ITEM)) {
                    if (!cgd.containsDeniedJob(UtilsIniHeaders.getIntIniHeader(menuProfessions.getItems().get(i).getParameter2()))) {
                        menuProfessions.getItems().get(i).setCommand(CommandPanel.COMMAND_JOB_GROUP_DISABLE_ITEM);
                    }
                } else if (menuProfessions.getItems().get(i).getCommand().equals(CommandPanel.COMMAND_JOB_GROUP_DISABLE_ITEM)) {
                    if (cgd.containsDeniedJob(UtilsIniHeaders.getIntIniHeader(menuProfessions.getItems().get(i).getParameter2()))) {
                        menuProfessions.getItems().get(i).setCommand(CommandPanel.COMMAND_JOB_GROUP_ENABLE_ITEM);
                    }
                }
            }
        }

        return true;
    }

    public static void save(ObjectOutputStream oos) throws Exception {
        oos.writeObject(prioritiesList);
        oos.writeObject(prioritiesValues);
    }

    @SuppressWarnings("unchecked")
    public static void load(ObjectInputStream ois) throws Exception {
        loadItems();
        prioritiesList = (ArrayList<String>) ois.readObject();
        prioritiesValues = (HashMap<String, Integer>) ois.readObject();

        // Si hay más prioridades, las metemos
        if (itemList != null && prioritiesList != null && prioritiesValues != null && prioritiesList.size() == prioritiesValues.size()) {
            if (itemList.size() != prioritiesList.size()) {
                loadItems();
            }
        }
    }

    public static void clear() {
        itemList = null;
        prioritiesList = null;
        prioritiesValues = null;
    }
}
