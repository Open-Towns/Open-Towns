package xaos.data;

import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
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

public class Type implements Externalizable {

    private static final long serialVersionUID = -6367571957315536401L;

    private static HashMap<String, String> typeNames = null;
    private static HashMap<String, String> typeIcons = null;

    private String id;
    private String name;

    private ArrayList<String> elements;
    private ArrayList<String> elementNames;

    public Type() {
    }

    public Type(String id) {
        setID(id);

        // Miramos si tiene nombre
        setName(getTypeName(id));
    }

    public ArrayList<String> getElements() {
        if (elements == null) {
            elements = new ArrayList<String>();
        }

        return elements;
    }

    public void setElements(ArrayList<String> elements) {
        this.elements = elements;
    }

    public ArrayList<String> getElementNames() {
        if (elementNames == null) {
            elementNames = new ArrayList<String>();
        }

        return elementNames;
    }

    public void setElementNames(ArrayList<String> elementNames) {
        this.elementNames = elementNames;
    }

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    public void addElement(String element, String elementName) {
        getElements().add(element);
        getElementNames().add(elementName);
    }

    public void removeElements() {
        while (getElements().size() > 0) {
            getElements().remove(0);
            getElementNames().remove(0);
        }
    }

    public void removeElement(String sElement) {
        int iIndex = getElements().indexOf(sElement);
        if (iIndex != -1) {
            getElements().remove(iIndex);
            getElementNames().remove(iIndex);
        }
    }

    public boolean contains(String sElement) {
        return getElements().contains(sElement);
    }

    public String getElementName(String sElement) {
        int iIndex = getElements().indexOf(sElement);
        if (iIndex != -1) {
            return getElementNames().get(iIndex);
        }

        return null;
    }

    public static String getIcon(String sTypeID) {
        if (typeIcons == null) {
            loadTypeNamesAndIcons();
        }

        return typeIcons.get(sTypeID);
    }

    /**
     * Devuelve el tipo principal de un nombre de tipo ej: food.meat devolvería
     * food
     *
     * @param sTypeName
     * @return el tipo principal de un nombre de tipo
     */
    public static String getMainType(String sTypeName) {
        if (sTypeName == null || sTypeName.length() == 0) {
            return null;
        }

        int iIndex = sTypeName.indexOf('.');
        if (iIndex == -1) {
            return sTypeName;
        } else {
            String sReturn = sTypeName.substring(0, iIndex);
            if (sReturn.length() > 0) {
                return sReturn;
            }

            return null;
        }
    }

    public static String getTypeName(String sTypeID) {
        if (sTypeID == null || sTypeID.length() == 0) {
            return null;
        }

        if (typeNames == null) {
            loadTypeNamesAndIcons();
        }

        String sReturn = typeNames.get(sTypeID);
        if (sReturn == null) {
            return sTypeID;
        }
        return sReturn;
    }

    private static void loadTypeNamesAndIcons() {
        typeNames = new HashMap<String, String>();
        typeIcons = new HashMap<String, String>();

        // Cargar de fichero
        loadXML(Towns.getPropertiesString("DATA_FOLDER") + "types.xml", true); //$NON-NLS-1$ //$NON-NLS-2$

        // Mods
        File fUserFolder = new File(Game.getUserFolder());
        if (!fUserFolder.exists() || !fUserFolder.isDirectory()) {
            return;
        }

        ArrayList<String> alMods = Game.getModsLoaded();
        if (alMods != null && alMods.size() > 0) {
            for (int i = 0; i < alMods.size(); i++) {
                String sModActionsPath = fUserFolder.getAbsolutePath() + System.getProperty("file.separator") + Game.MODS_FOLDER1 + System.getProperty("file.separator") + alMods.get(i) + System.getProperty("file.separator") + Towns.getPropertiesString("DATA_FOLDER") + "types.xml"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                File fIni = new File(sModActionsPath);
                if (fIni.exists()) {
                    loadXML(sModActionsPath, false);
                }
            }
        }
    }

    private static void loadXML(String sXMLPath, boolean bLoadingMain) {
        try {
            Document doc = UtilsXML.loadXMLFile(sXMLPath); //$NON-NLS-1$ //$NON-NLS-2$

			// Tenemos el documento XML parseado
            // Lo recorremos entero y vamos añadiendo los nombres a la hash
            NodeList nodeList = doc.getDocumentElement().getChildNodes();
            Node node;
            String sTypeID, sName;
            for (int i = 0; i < nodeList.getLength(); i++) {
                node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    sTypeID = node.getNodeName();
                    if (sTypeID != null && sTypeID.equalsIgnoreCase("DELETE")) {
                        // Miramos que ID quiere borrar
                        if (node.getAttributes() != null && node.getAttributes().getNamedItem("id") != null) {
                            String sIDToDelete = node.getAttributes().getNamedItem("id").getNodeValue();
                            if (sIDToDelete != null) {
                                typeNames.remove(sIDToDelete);
                                typeIcons.remove(sIDToDelete);
                            }
                        }
                        continue;
                    }

                    if (bLoadingMain && typeNames.containsKey(sTypeID)) {
                        Log.log(Log.LEVEL_ERROR, Messages.getString("Type.0") + sTypeID + "]", "Type"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    }
                    sName = UtilsXML.getChildValue(nodeList, sTypeID);

                    // Lo añadimos a la hash
                    if (sName != null) {
                        typeNames.put(sTypeID, sName);
                    } else {
                        typeNames.put(sTypeID, sTypeID);
                    }

                    // Icon
                    if (node.getAttributes() != null && node.getAttributes().getNamedItem("icon") != null && node.getAttributes().getNamedItem("icon").getNodeValue() != null) {
                        typeIcons.put(sTypeID, node.getAttributes().getNamedItem("icon").getNodeValue());
                    }
                }
            }
        } catch (Exception e) {
            Log.log(Log.LEVEL_ERROR, Messages.getString("Type.3") + e.toString() + "]", "Type"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            Game.exit();
        }
    }

    public static void clear() {
        typeNames = null;
        typeIcons = null;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        id = (String) in.readObject();
        name = (String) in.readObject();
        elements = (ArrayList<String>) in.readObject();
        elementNames = (ArrayList<String>) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(id);
        out.writeObject(name);
        out.writeObject(elements);
        out.writeObject(elementNames);
    }
}
